package com.codewizards.election;

import com.codewizards.Constants;
import com.codewizards.server.MessageSender;
import com.codewizards.server.Server;
import com.codewizards.server.ServerState;
import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableCompletableObserver;
import lombok.NonNull;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class FastBully {

    public static Logger logger = Logger.getLogger(FastBully.class.getName());

    private static FastBully INSTANCE;

    private Disposable viewMessageTimeoutDisposable;

    private Disposable heartbeatWaitTimeoutDisposable;

    private Disposable answerMessageTimeoutDisposable;

    private AtomicBoolean isWaitingForViewMessage = new AtomicBoolean(false);

    private AtomicBoolean viewMessagesReceived = new AtomicBoolean(false);

    private AtomicBoolean isWaitingForAnswerMessage = new AtomicBoolean(false);


    private FastBully() {
    }

    public static synchronized FastBully getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FastBully();
        }
        return INSTANCE;
    }

    public String notifyIamUp(List<Server> serverList){
        logger.info("IamUp!!!");
        startViewMessageTimeout(); // TODO - start timeout after sending
        for (Server server : serverList) {
            sendIamUpMessage(server);
        }
        return null;
    }

    private void sendIamUpMessage(Server server) {
        logger.info("Send IamUp to: " + server.getServerId());
        try {
            MessageSender.sendIamUpMessage(server);
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    public void sendViewMessage(Server server){
        logger.info("Send view message to: " + server.getServerId());
        try {
            MessageSender.sendViewMessage(server);
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    public void notifyNewCoordinator(List<Server> lowerPriorityServers){
        for (Server server : lowerPriorityServers) {
            sendCoordinatorMessage(server);
        }
        setCoordinator(ServerState.getInstance().getOwnServer());
    }

    public void sendCoordinatorMessage(Server server){
        logger.info("Send coordinator message to: " + server.getServerId());
        try {
            MessageSender.sendCoordinatorMessage(server);
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
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
                                       logger.error(error.getMessage());
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

    public synchronized void setCoordinator(Server coordinator) {

        stopHeartbeatWaitTimeout();

        if (ServerState.getInstance().getOwnServer().equals(coordinator)) {
            if (coordinator.equals(ServerState.getInstance().getCoordinator())) {
                logger.info("I'm already the coordinator");
                // do nothing
            } else {
                logger.info("I'm the coordinator");
                ServerState.getInstance().setCoordinator(coordinator);
                Leader.getInstance().startHeartbeat();
            }
        } else {
            if (ServerState.getInstance().getCoordinator() == null) { // first time
                logger.info("Set " + coordinator.getServerId() + " as coordinator");
                ServerState.getInstance().setCoordinator(coordinator);
            } else if (!ServerState.getInstance().getCoordinator().equals(coordinator)) { // update
                if (ServerState.getInstance().getOwnServer().equals(ServerState.getInstance().getCoordinator())) { // stop leader heartbeat if I'm the coordinator
                    Leader.deleteInstance();
                }
                logger.info("Update " + coordinator.getServerId() + " as coordinator");
                ServerState.getInstance().setCoordinator(coordinator);
            } else { // already the coordinator
                logger.info(coordinator.getServerId() + " is already the coordinator");
                // do nothing
            }

            startHeartbeatWaitTimeout();

        }
    }

    private synchronized void startHeartbeatWaitTimeout() {
        heartbeatWaitTimeoutDisposable = (Completable.timer(Constants.HEARTBEAT_WAIT_INTERVAL, TimeUnit.MILLISECONDS)
                .subscribeWith(new DisposableCompletableObserver() {

                                   @Override
                                   protected void onStart() {
                                       logger.info("Start Heartbeat wait timeout!");
                                   }

                                   @Override
                                   public void onComplete() {
                                       logger.info("Heartbeat wait timeout completed!");
                                       logger.info("Leader heartbeat failure, Starting election");
                                       startElection();
                                       stopHeartbeatWaitTimeout();
                                   }

                                   @Override
                                   public void onError(@NonNull Throwable error) {
                                       logger.error(error.getMessage());
                                   }
                               }
                )
        );
    }

    private synchronized void stopHeartbeatWaitTimeout() {
        if (heartbeatWaitTimeoutDisposable != null && !heartbeatWaitTimeoutDisposable.isDisposed()) {
            heartbeatWaitTimeoutDisposable.dispose();
        }
    }

    public synchronized void resetHeartbeatWaitTimeout() {
        logger.info("Reset Heartbeat wait timeout!");
        stopHeartbeatWaitTimeout();
        startHeartbeatWaitTimeout();
    }

    public void startElection() {
        logger.info("Starting Election");
        ServerState.getInstance().removeServerFromServerView(ServerState.getInstance().getCoordinator()); // remove coordinator from view

        List<Server> higherPriorityServers = ServerState.getInstance().getServersWithHigherPriority();

        startAnswerMessageTimeout(); // TODO - start timeout after sending

        for (Server server : higherPriorityServers){
            sendElectionMessage(server);
        }

    }

    private void sendElectionMessage(Server server) {
        logger.info("Send election message to: " + server.getServerId());
        try {
            MessageSender.sendElectionMessage(server);
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    private void startAnswerMessageTimeout() {
        answerMessageTimeoutDisposable = (Completable.timer(Constants.ANSWER_MESSAGE_TIMEOUT, TimeUnit.MILLISECONDS)
                .subscribeWith(new DisposableCompletableObserver() {
                                   @Override
                                   public void onStart() {
                                       logger.info("Answer message timeout started!");
                                       isWaitingForAnswerMessage.set(true);
                                   }

                                   @Override
                                   public void onError(@NonNull Throwable error) {
                                       logger.error(error.getMessage());
                                   }

                                   @Override
                                   public void onComplete() {
                                       logger.info("Answer message timeout completed!");

                                   }
                               }
                )
        );
    }

    public boolean isWaitingForAnswerMessage(){
        return isWaitingForAnswerMessage.get();
    }

    public void stopAnswerMessageTimeout() {
        if (isWaitingForAnswerMessage.get() && !answerMessageTimeoutDisposable.isDisposed()) {
            logger.info("Answer message timeout stopped!");
            isWaitingForAnswerMessage.set(false);
            answerMessageTimeoutDisposable.dispose();
        }
    }

    public void sendAnswerMessage(Server server) {
        logger.info("Send answer message to: " + server.getServerId());
        try {
            MessageSender.sendAnswerMessage(server);
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }
    }
}
