package com.codewizards.server;


import com.codewizards.election.FastBully;
import lombok.NonNull;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.util.List;

public class MessageHandler {

    public static Logger logger = Logger.getLogger(MessageHandler.class.getName());

    public MessageHandler() {

    }

    public void respondToElectionMessage() {

    }

    public void respondToNominationMessage() {

    }

    public void respondToAnswerMessage() {

    }

    public void respondToCoordinatorMessage() {

    }

    public void respondToIamUpMessage(@NonNull Server server) throws InterruptedException {
        logger.info("Received IamUp from " + server.getServerId());
        Thread.sleep(3000L); // delay reply
        FastBully.getInstance().sendViewMessage(server);
    }

    public void respondToViewMessage(@NonNull Server server, @NonNull JSONObject message)  {
        List<String> view = (List<String>) message.get("processes");
        logger.info("Received view from " + server.getServerId() + " | view: " + view);
        FastBully.getInstance().setViewMessagesReceived(true);
        ServerState.getInstance().compareAndSetView(view);
        Server highestPriorityServer = ServerState.getInstance().getHighestPriorityServer();
        if (highestPriorityServer.getServerId().equalsIgnoreCase(ServerState.getInstance().getOwnServer().getServerId())){
            logger.info("I am the highest priority numbered process");
            FastBully.getInstance().setCoordinator(highestPriorityServer);
        } else{
            FastBully.getInstance().setCoordinator(highestPriorityServer);
        }

    }

}
