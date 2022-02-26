package com.codewizards.election;

import com.codewizards.Constants;
import com.codewizards.message.ServerMessage;
import com.codewizards.server.Server;
import com.codewizards.server.ServerState;
import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableCompletableObserver;
import lombok.NonNull;
import org.apache.log4j.Logger;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class FastBully {

    public static Logger logger = Logger.getLogger(FastBully.class.getName());

    private static FastBully INSTANCE;

    private Disposable viewMessageTimeoutDisposable;

    private AtomicBoolean isWaitingForViewMessage = new AtomicBoolean(false);

    private AtomicBoolean viewMessagesReceived = new AtomicBoolean(false);

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
        startViewMessageTimeout();
        return null;
    }

    private void sendIamUpMessage(Server server) {
        logger.info("Send IamUp to: " + server.getServerId());
        try {
            Socket socket = new Socket(server.getServerAddress(), server.getCoordinationPort());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.write((ServerMessage.getIamUpMessage(ServerState.getInstance().getOwnServer().getServerId()) + "\n").getBytes(StandardCharsets.UTF_8));
            dataOutputStream.flush();

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
                    ServerState.getInstance().getServerViewAsArrayList()) + "\n").getBytes(StandardCharsets.UTF_8));
            dataOutputStream.flush();
        } catch (Exception e) {
            logger.error(e.getMessage() + ": " + server.getServerId());
        }
    }

    public void notifyNewCoordinator(List<Server> lowerPriorityServers) {
        for (Server server : lowerPriorityServers) {
            sendCoordinatorMessage(server);
        }
        setCoordinator(ServerState.getInstance().getOwnServer());
    }

    public void sendCoordinatorMessage(Server server) {
        logger.info("Send coordinator message to: " + server.getServerId());
        try {
            Socket socket = new Socket(server.getServerAddress(), server.getCoordinationPort());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.write((ServerMessage.getCoordinatorMessage(
                    ServerState.getInstance().getOwnServer().getServerId()) + "\n").getBytes(StandardCharsets.UTF_8));
            dataOutputStream.flush();
        } catch (Exception e) {
            logger.error(e.getMessage() + ": " + server.getServerId());
        }
    }

    private void startViewMessageTimeout() {
        viewMessageTimeoutDisposable = (Completable.timer(Constants.VIEW_MESSAGE_TIMEOUT, TimeUnit.MILLISECONDS)
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
                                       if (!viewMessagesReceived.get()) {
                                           logger.info("Self set new coordinator");
                                           setCoordinator(ServerState.getInstance().getOwnServer());
                                       }
                                       stopViewMessageTimeout();
                                   }
                               }
                )
        );
    }

    public void stopViewMessageTimeout() {
        if (isWaitingForViewMessage.get() && !viewMessageTimeoutDisposable.isDisposed()) {
            logger.info("View message timeout stopped!");
            isWaitingForViewMessage.set(false);
            viewMessageTimeoutDisposable.dispose();
        }
    }

    public void setViewMessagesReceived(@NonNull Boolean z) {
        viewMessagesReceived.set(z);
    }

    public void setCoordinator(Server coordinator) {
        logger.info("Set " + coordinator.getServerId() + " as coordinator");
        ServerState.getInstance().setCoordinator(coordinator);
    }

}
