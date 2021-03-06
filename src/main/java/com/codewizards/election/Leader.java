package com.codewizards.election;

import com.codewizards.Constants;
import com.codewizards.client.ClientManager;
import com.codewizards.room.RoomManager;
import com.codewizards.server.MessageSender;
import com.codewizards.server.Server;
import com.codewizards.server.ServerState;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.schedulers.Schedulers;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Leader {

    public static Logger logger = Logger.getLogger(Leader.class.getName());

    private static Leader INSTANCE;

    private static Observable<Server> serverObservable;

    private static CompositeDisposable heartbeatDisposables = new CompositeDisposable();

    private static AtomicBoolean heartbeatRunning = new AtomicBoolean(false);

    private Leader() {

    }

    public static synchronized Leader getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Leader();
        }
        return INSTANCE;
    }

    public static synchronized void deleteInstance() {
        heartbeatRunning.set(false);
        serverObservable = null;
        heartbeatDisposables.clear();
        INSTANCE = null;
        logger.info("Leader Heartbeat Stopped");
    }

    public void startHeartbeat() {

        if (serverObservable != null) return;

        serverObservable = Observable.create((ObservableOnSubscribe<Server>) emitter -> {

                    for (Server server : ServerState.getInstance().getServerViewAsServerArrayList()) {
                        if (!emitter.isDisposed()) {
                            emitter.onNext(server);
                        }
                    }

                    if (!emitter.isDisposed()) {
                        emitter.onComplete();
                    }

                })
                .subscribeOn(Schedulers.io())
                .repeatWhen(throwableObservable -> throwableObservable.delay(Constants.HEARTBEAT_SND_INTERVAL, TimeUnit.MILLISECONDS))
                .filter(server -> heartbeatRunning.get());

        serverObservable.subscribe(new Observer<Server>() {
            @Override
            public void onSubscribe(@NonNull Disposable disposable) {
                logger.info("onSubscribe: Starting Leader Heartbeat");
                heartbeatRunning.set(true);
                heartbeatDisposables.add(disposable);
            }

            @Override
            public void onNext(@NonNull Server server) {
                logger.info("onNext: Send Leader Heartbeat to " + server.getServerId());
                Leader.getInstance().sendHeartbeatMessage(server);
            }

            @Override
            public void onError(@NonNull Throwable throwable) {
                logger.error("onError: " + throwable.getMessage());
            }

            @Override
            public void onComplete() {
                logger.info("onComplete: ");
            }
        });

    }

    public void sendHeartbeatMessage(Server server) {
        MessageSender.sendHeartbeatMessage(server, () -> handleHeartbeatSendFailure(server));
    }

    private void handleHeartbeatSendFailure(Server server) {
        logger.info("Server " + server.getServerId() + " is down!");
        ServerState.getInstance().removeServerFromServerView(server);
        // self update rooms & clients
        ClientManager.removeClientsOnFailure(server.getServerId());
        RoomManager.removeRoomsOnFailure(server.getServerId());
        // broadcast inform server failure message
        for (Server server1 : ServerState.getInstance().getServerViewAsServerArrayList()){
            try {
                MessageSender.sendInformServerFailureMessage(server1, server.getServerId());
            } catch (IOException e) {
                logger.error("handleHeartbeatSendFailure(): " + e.getLocalizedMessage());
            }
        }
    }

}
