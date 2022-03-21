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
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class FastBully {

    public static Logger logger = Logger.getLogger(FastBully.class.getName());

    private static FastBully INSTANCE;

    private final ConcurrentNavigableMap<Server, Boolean> answerStatus = new ConcurrentSkipListMap<>();

    private Disposable viewMessageTimeoutDisposable;

    private Disposable heartbeatWaitTimeoutDisposable;

    private Disposable answerMessageTimeoutDisposable;

    private Disposable coordinatorMessageTimeoutDisposable;

    private Disposable nominationOrCoordinationMessageTimeoutDisposable;

    private AtomicBoolean isWaitingForViewMessage = new AtomicBoolean(false);
    private AtomicBoolean viewMessagesReceived = new AtomicBoolean(false);

    private AtomicBoolean isWaitingForAnswerMessage = new AtomicBoolean(false);
    private AtomicBoolean answerMessageReceived = new AtomicBoolean(false);

    private AtomicBoolean electionReady = new AtomicBoolean(true);

    private AtomicBoolean isWaitingForCoordinatorMessage = new AtomicBoolean(false);
    private AtomicBoolean coordinatorMessageReceived = new AtomicBoolean(false);

    private AtomicBoolean isWaitingForNominationOrCoordinatorMessage = new AtomicBoolean(false);
    private AtomicBoolean nominationOrCoordinatorMessageReceived = new AtomicBoolean(false);

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
        isWaitingForViewMessage.set(true);
        viewMessagesReceived.set(false);
        for (Server server : serverList) {
            sendIamUpMessage(server);
        }
        startViewMessageTimeout();
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

    public void sendViewMessage(Server server) {
        logger.info("Send view message to: " + server.getServerId());
        try {
            MessageSender.sendViewMessage(server);
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
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
            MessageSender.sendCoordinatorMessage(server);
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    public boolean isWaitingForViewMessage() {
        return isWaitingForViewMessage.get();
    }

    private void startViewMessageTimeout() {
        viewMessageTimeoutDisposable = (Completable.timer(Constants.VIEW_MESSAGE_TIMEOUT, TimeUnit.MILLISECONDS)
                .subscribeWith(new DisposableCompletableObserver() {
                                   @Override
                                   public void onStart() {
                                       logger.info("View message timeout started!");
                                   }

                                   @Override
                                   public void onError(@NonNull Throwable error) {
                                       logger.error(error.getMessage());
                                   }

                                   @Override
                                   public void onComplete() {
                                       logger.info("View message timeout completed!");
                                       if (!viewMessagesReceived.get()) {
                                           // stops the procedure
                                           logger.info("No view messages received, self set new coordinator");
                                           setCoordinator(ServerState.getInstance().getOwnServer());

                                       } else {
                                           Server highestPriorityServer = ServerState.getInstance().getHighestPriorityServer();
                                           if (highestPriorityServer.getServerId().equalsIgnoreCase(ServerState.getInstance().getOwnServer().getServerId())) {
                                               logger.info("I am the highest priority numbered process");
                                               // send coordinator message to lower priority servers
                                               FastBully.getInstance().notifyNewCoordinator(ServerState.getInstance().getServersWithLowerPriority());
                                               // stops the procedure
                                           } else {
                                               // admit the highest priority numbered process as the coordinator
                                               FastBully.getInstance().setCoordinator(highestPriorityServer);
                                               // stop election procedure
                                           }
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
                Leader.getInstance().startHeartbeat(); // start leader heartbeat
            }
        } else {
            if (ServerState.getInstance().getCoordinator() == null) { // first time
                logger.info("Set " + coordinator.getServerId() + " as coordinator");
                ServerState.getInstance().setCoordinator(coordinator);
            } else if (!ServerState.getInstance().getCoordinator().equals(coordinator)) { // update coordinator
                if (ServerState.getInstance().getOwnServer().equals(ServerState.getInstance().getCoordinator())) { // stop leader heartbeat if own server the current coordinator
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
                                       FastBully.getInstance().handleLeaderHeartbeatFailure();
                                       logger.info("Leader heartbeat failure, Starting election");
                                       FastBully.getInstance().startElection();
                                       FastBully.getInstance().stopHeartbeatWaitTimeout();
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
            logger.info("Stop Heartbeat wait timeout!");
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

        isWaitingForAnswerMessage.set(true);
        answerMessageReceived.set(false);
        electionReady.set(false);

        startAnswerMessageTimeout(); // TODO - start timeout after sending

        for (Server server : higherPriorityServers) {
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
                                   }

                                   @Override
                                   public void onError(@NonNull Throwable error) {
                                       logger.error(error.getMessage());
                                   }

                                   @Override
                                   public void onComplete() {
                                       logger.info("Answer message timeout completed!");
                                       if (!answerMessageReceived.get()) {
                                           logger.info("No answer messages received");
                                           // sends a coordinator message to other processes with lower priority number
                                           FastBully.getInstance().notifyNewCoordinator(ServerState.getInstance().getServersWithLowerPriority());

                                           // stops its election procedure
                                           FastBully.getInstance().setElectionReady(true);
                                       } else {

                                           isWaitingForCoordinatorMessage.set(true);
                                           coordinatorMessageReceived.set(false);

                                           // determines the highest priority number of the answering processes
                                           // and sends a nomination message to that process.
                                           FastBully.getInstance().sendNominationMessage(answerStatus.pollFirstEntry().getKey());

                                           // waits for a coordinator message for the interval T3.
                                           FastBully.getInstance().startCoordinatorMessageTimeout();
                                       }
                                       stopAnswerMessageTimeout();
                                   }
                               }
                )
        );
    }

    public boolean isWaitingForAnswerMessage() {
        return isWaitingForAnswerMessage.get();
    }

    public synchronized void setAnswerMessageReceived(@NonNull Server server, @NonNull Boolean z) {
        answerMessageReceived.set(z);
        answerStatus.put(server, Boolean.TRUE);
    }

    public synchronized void stopAnswerMessageTimeout() {
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

    public void sendNominationMessage(Server server) {
        logger.info("Send nomination message to: " + server.getServerId());
        try {
            MessageSender.sendNominationMessage(server);
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    public synchronized void setElectionReady(@NonNull Boolean z) {
        electionReady.set(z);

        if (z) {
            // TODO - stop all timeouts
            FastBully.getInstance().stopCoordinationMessageTimeout();
            FastBully.getInstance().stopAnswerMessageTimeout();
            FastBully.getInstance().stopNominationOrCoordinationMessageTimeout();
        }
    }

    private synchronized void startCoordinatorMessageTimeout() {
        coordinatorMessageTimeoutDisposable = (Completable.timer(Constants.COORDINATOR_MESSAGE_TIMEOUT, TimeUnit.MILLISECONDS)
                .subscribeWith(new DisposableCompletableObserver() {
                                   @Override
                                   public void onStart() {
                                       logger.info("Coordinator message timeout started!");

                                   }

                                   @Override
                                   public void onError(@NonNull Throwable error) {
                                       logger.error(error.getMessage());
                                   }

                                   @Override
                                   public void onComplete() {
                                       logger.info("Coordinator message timeout completed!");
                                       if (!coordinatorMessageReceived.get()) {
                                           logger.info("No coordinator messages received, Repeat");
                                           // send nomination to the next highest priority numbered process
                                           Server nextHighestPriorityServer = answerStatus.pollFirstEntry().getKey();
                                           if (nextHighestPriorityServer != null) {
                                               FastBully.getInstance().sendNominationMessage(answerStatus.pollFirstEntry().getKey());
                                           } else {
                                               // If no process left to choose, restarts the election procedure
                                               FastBully.getInstance().startElection();
                                           }
                                       }
                                       stopCoordinationMessageTimeout();
                                   }
                               }
                )
        );
    }

    public boolean isWaitingForCoordinatorMessage() {
        return isWaitingForCoordinatorMessage.get();
    }

    public void setCoordinatorMessageReceived(@NonNull Boolean z) {
        coordinatorMessageReceived.set(z);
    }

    public synchronized void stopCoordinationMessageTimeout() {
        if (isWaitingForCoordinatorMessage.get() && !coordinatorMessageTimeoutDisposable.isDisposed()) {
            logger.info("Coordinator message timeout stopped!");
            isWaitingForCoordinatorMessage.set(false);
            coordinatorMessageTimeoutDisposable.dispose();
        }
    }

    public synchronized void waitForNominationOrCoordinationMessage() {
        if (!isWaitingForNominationOrCoordinatorMessage.get()) {
            isWaitingForNominationOrCoordinatorMessage.set(true);
            nominationOrCoordinatorMessageReceived.set(false);
            startNominationOrCoordinationMessageTimeout();
        }
    }

    private void startNominationOrCoordinationMessageTimeout() {
        nominationOrCoordinationMessageTimeoutDisposable = (Completable.timer(Constants.getT4(ServerState.getInstance().getOwnServerPriority()), TimeUnit.MILLISECONDS)
                .subscribeWith(new DisposableCompletableObserver() {
                                   @Override
                                   public void onStart() {
                                       logger.info("Nomination or coordination message timeout started!");
                                   }

                                   @Override
                                   public void onError(@NonNull Throwable error) {
                                       logger.error(error.getMessage());
                                   }

                                   @Override
                                   public void onComplete() {
                                       logger.info("Nomination or coordination message timeout completed!");
                                       if (!nominationOrCoordinatorMessageReceived.get()) {
                                           logger.info("No nomination or coordination messages received, Restart");
                                           if (electionReady.get()){ // no election is running
                                               // restart election procedure
                                               FastBully.getInstance().startElection();
                                           }
                                       }
                                       stopNominationOrCoordinationMessageTimeout();
                                   }
                               }
                )
        );
    }

    public boolean isWaitingForNominationOrCoordinationMessage() {
        return isWaitingForNominationOrCoordinatorMessage.get();
    }

    public void setNominationOrCoordinationMessageReceived(@NonNull Boolean z) {
        nominationOrCoordinatorMessageReceived.set(z);
    }

    public synchronized void stopNominationOrCoordinationMessageTimeout() {
        if (isWaitingForNominationOrCoordinatorMessage.get() && !nominationOrCoordinationMessageTimeoutDisposable.isDisposed()) {
            logger.info("Nomination or Coordinator message timeout stopped!");
            isWaitingForNominationOrCoordinatorMessage.set(false);
            nominationOrCoordinationMessageTimeoutDisposable.dispose();
        }
    }

    private void handleLeaderHeartbeatFailure() {
        logger.info("Leader is down!");
        //TODO - handle failure
    }

}
