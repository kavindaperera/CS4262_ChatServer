package com.codewizards.server;


import com.codewizards.election.FastBully;
import lombok.NonNull;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

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
        logger.info("Received IamUp message from " + server.getServerId());
        Thread.sleep(3000L); // delay reply
        FastBully.getInstance().sendViewMessage(server);
    }

    public void respondToViewMessage(@NonNull Server server)  {
        logger.info("Received view message from " + server.getServerId());
        FastBully.getInstance().stopViewMessageTimeout();
    }

}
