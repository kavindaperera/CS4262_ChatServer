package com.codewizards.election;

import com.codewizards.Constants;
import com.codewizards.message.ServerMessage;
import com.codewizards.server.Server;
import com.codewizards.server.ServerState;
import io.reactivex.Completable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import org.apache.log4j.Logger;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class FastBully {

    public static Logger logger = Logger.getLogger(FastBully.class.getName());

    private static FastBully INSTANCE;

    private CompositeDisposable viewMessageTimeoutDisposable;

    private AtomicBoolean isWaitingForViewMessage = new AtomicBoolean(false);

    private FastBully() {
    }

    public static synchronized FastBully getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FastBully();
        }
        return INSTANCE;
    }

    public String notifyIamUp(List<Server> serverList) {
        logger.info("IamUp!!!");
        for (Server server : serverList) {
            sendIamUpMessage(server);
        }
        return null;
    }

    private void sendIamUpMessage(Server server) {
        logger.info("Send IamUp to: " + server.getServerId());
        try {
            Socket socket = new Socket(server.getServerAddress(), server.getCoordinationPort());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.write((ServerMessage.getIamUpMessage(ServerState.getInstance().getOwnServer().getServerId()) + "\n").getBytes("UTF-8"));
            dataOutputStream.flush();
            startViewMessageTimeout();
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage() + ": " + server.getServerId());
        }
    }

    public void sendViewMessage(Server server) {
        logger.info("Send view message to: " + server.getServerId());
        try {
            Socket socket = new Socket(server.getServerAddress(), server.getCoordinationPort());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.write((ServerMessage.getViewMessage(
                    ServerState.getInstance().getOwnServer().getServerId(),
                    ServerState.getInstance().getServerIdList()) + "\n").getBytes("UTF-8"));
            dataOutputStream.flush();
        } catch (Exception e) {
            logger.error(e.getMessage() + ": " + server.getServerId());
        }

    }

    private void startViewMessageTimeout() {

        if (viewMessageTimeoutDisposable == null) {
            viewMessageTimeoutDisposable = new CompositeDisposable();
        }

        viewMessageTimeoutDisposable.add(Completable.timer(Constants.VIEW_MESSAGE_TIMEOUT, TimeUnit.MILLISECONDS)
                .subscribeWith(new DisposableCompletableObserver() {
                                   @Override
                                   public void onStart() {
                                       logger.info("View message timeout started!");
                                       isWaitingForViewMessage.set(true);
                                   }

                                   @Override
                                   public void onError(@NonNull Throwable error) {
                                       error.printStackTrace();
                                   }

                                   @Override
                                   public void onComplete() {
                                       logger.info("View message timeout completed!");
                                       logger.info("No view message received");
                                       stopViewMessageTimeout();
                                   }
                               }
                ));

    }

    public void stopViewMessageTimeout() {
        if (isWaitingForViewMessage.get() && !viewMessageTimeoutDisposable.isDisposed()) {
            logger.info("View message timeout stopped!");
            viewMessageTimeoutDisposable.dispose();
        }
    }

}
