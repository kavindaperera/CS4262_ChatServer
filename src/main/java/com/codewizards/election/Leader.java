package com.codewizards.election;

import com.codewizards.Constants;
import com.codewizards.message.ServerMessage;
import com.codewizards.server.Server;
import com.codewizards.server.ServerState;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.apache.log4j.Logger;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Leader {

    public static Logger logger = Logger.getLogger(Leader.class.getName());

    private static Leader INSTANCE;

    private static Observable<Server> serverObservable;

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
        INSTANCE = null;
        logger.info("Leader Heartbeat Stopped");
    }

    public void startHeartbeat() {

        if (serverObservable != null) return;

        serverObservable = Observable
                .create((ObservableOnSubscribe<Server>) emitter -> {

                    for (Server server : ServerState.getInstance().getServerViewAsServerArrayList()) {
                        if (!emitter.isDisposed()) {
                            emitter.onNext(server);
                        }
                    }

                    if (!emitter.isDisposed()) {
                        emitter.onComplete();
                    }

                }).subscribeOn(Schedulers.io())
                .repeatWhen(throwableObservable -> throwableObservable.delay(Constants.HEARTBEAT_SND_INTERVAL, TimeUnit.MILLISECONDS))
                .filter(server -> heartbeatRunning.get());

        serverObservable.subscribe(new Observer<Server>() {
            @Override
            public void onSubscribe(@NonNull Disposable disposable) {
                logger.info("onSubscribe: Starting Leader Heartbeat");
                heartbeatRunning.set(true);
            }

            @Override
            public void onNext(@NonNull Server server) {
                logger.info("onNext: Send Leader Heartbeat to " + server.getServerId());
                Leader.getInstance().sendHeartbeatMessage(server);
            }

            @Override
            public void onError(@NonNull Throwable throwable) {
                logger.error("onError: ");
            }

            @Override
            public void onComplete() {
                logger.info("onComplete: ");
            }
        });

    }

    public void sendHeartbeatMessage(@NonNull Server server){
        try {
            Socket socket = new Socket(server.getServerAddress(), server.getCoordinationPort());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.write((ServerMessage.getHeartbeatMessage(ServerState.getInstance().getOwnServer().getServerId()) + "\n").getBytes(StandardCharsets.UTF_8));
            dataOutputStream.flush();

        } catch (IOException e) {
            logger.error(e.getLocalizedMessage() + ": " + server.getServerId());
        }
    }

}
