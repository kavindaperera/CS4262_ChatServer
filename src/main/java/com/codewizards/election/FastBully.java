package com.codewizards.election;

import com.codewizards.Constants;
import com.codewizards.message.ServerMessage;
import com.codewizards.server.Server;
import com.codewizards.server.ServerState;
import io.reactivex.Completable;
import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DisposableCompletableObserver;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FastBully {

    public static Logger logger = Logger.getLogger(FastBully.class.getName());

    private static FastBully INSTANCE;

    @Getter
    @Setter
    private static boolean electionReady = true;

    private FastBully() {
    }

    public static synchronized FastBully getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FastBully();
        }
        return INSTANCE;
    }

    public String startElection(List<Server> higherPriorityServers){ // bully test
        logger.info("You started the election!!");
        setElectionReady(false);

        Iterator<Server> it = higherPriorityServers.iterator();
        while (it.hasNext()) {
            Server higherPriorityServer = it.next();
            logger.debug("Sending election message to " + higherPriorityServer.getServerId() + " server");
            try {
                Socket socket = new Socket(higherPriorityServer.getServerAddress(), higherPriorityServer.getCoordinationPort());
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.write((ServerMessage.getElectionMessage(ServerState.getInstance().getOwnServer().getServerId()) + "\n").getBytes("UTF-8"));
                dataOutputStream.flush();
            } catch (Exception e) {
                logger.error(e.getMessage() + ": " + higherPriorityServer.getServerId());
            }
        }

        Completable.timer(Constants.ANSWER_MSG_TIMEOUT, TimeUnit.MILLISECONDS)
                .subscribeWith(new DisposableCompletableObserver() {
                                   @Override
                                   public void onStart() {
                                       logger.info("Answer message timeout started!");
                                   }

                                   @Override
                                   public void onError(@NonNull Throwable error) {
                                       error.printStackTrace();
                                   }

                                   @Override
                                   public void onComplete() {
                                       logger.info("Answer message timeout completed!");
                                   }
                               }
                );

        return null;
    }




}
