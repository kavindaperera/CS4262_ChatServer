package com.codewizards.server;


import com.codewizards.client.ClientManager;
import com.codewizards.election.FastBully;
import com.codewizards.message.ServerMessage;
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

    public void respondToCoordinatorMessage(@NonNull Server server) {
        logger.info("Received coordinator message from " + server.getServerId());
        FastBully.getInstance().setCoordinator(server);
    }

    public void respondToIamUpMessage(@NonNull Server server) throws InterruptedException {
        logger.info("Received IamUp from " + server.getServerId());
        Thread.sleep(3000L); // delay reply
        FastBully.getInstance().sendViewMessage(server);
        ServerState.getInstance().addServerToServerView(server);
    }

    public void respondToViewMessage(@NonNull Server server, @NonNull JSONObject message)  {
        List<String> view = (List<String>) message.get("processes");
        logger.info("Received view from " + server.getServerId() + " | view: " + view);
        FastBully.getInstance().setViewMessagesReceived(true);
        ServerState.getInstance().compareAndSetView(view);
        Server highestPriorityServer = ServerState.getInstance().getHighestPriorityServer();
        if (highestPriorityServer.getServerId().equalsIgnoreCase(ServerState.getInstance().getOwnServer().getServerId())){
            logger.info("I am the highest priority numbered process");
            // send coordinator message to lower priority servers
            FastBully.getInstance().notifyNewCoordinator(ServerState.getInstance().getServersWithLowerPriority());
        } else{
            FastBully.getInstance().setCoordinator(highestPriorityServer);
            // TODO - stop election
        }

    }

    public JSONObject respondToClientIdApprovalMessage(JSONObject receivedMessage) {
        String requestedID = (String) receivedMessage.get("identity");
        JSONObject response = null;
        if (ClientManager.checkClientIdentityAvailability(requestedID)){
            response = ServerMessage.getApproveClientIDMessage("true");
            ClientManager.addToGlobalClientsList(requestedID, receivedMessage.get("serverId").toString());
        } else {
            response = ServerMessage.getApproveClientIDMessage("false");
        }
        return response;
    }

}
