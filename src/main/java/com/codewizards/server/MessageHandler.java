package com.codewizards.server;


import com.codewizards.Main;
import com.codewizards.client.ClientManager;
import com.codewizards.election.FastBully;
import com.codewizards.message.ServerMessage;
import com.codewizards.room.RoomManager;
import lombok.NonNull;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MessageHandler {

    public static Logger logger = Logger.getLogger(MessageHandler.class.getName());

    public MessageHandler() {

    }

    public void respondToElectionMessage(@NonNull Server server) {
        logger.debug("Received election message from: " + server.getServerId());
        FastBully.getInstance().sendAnswerMessage(server);
    }

    public void respondToNominationMessage(@NonNull Server server) throws InterruptedException {
        logger.debug("Received nomination message from: " + server.getServerId());

        // sends a coordinator message to all the processes with lower priority numbers
        FastBully.getInstance().notifyNewCoordinator(ServerState.getInstance().getServersWithLowerPriority());

        // stops its election procedure
        FastBully.getInstance().setElectionReady(true);
    }

    public void respondToAnswerMessage(@NonNull Server server) {
        logger.debug("Received answer message from: " + server.getServerId());
        if (FastBully.getInstance().isWaitingForAnswerMessage()){
            FastBully.getInstance().setAnswerMessageReceived(server, true);
        }
    }

    public void respondToCoordinatorMessage(@NonNull Server server) {
        logger.info("Received coordinator message from " + server.getServerId());
        FastBully.getInstance().setCoordinatorMessageReceived(true);

        // admit the new coordinator.
        FastBully.getInstance().setCoordinator(server);

        // stops its election procedure
        FastBully.getInstance().setElectionReady(true);
        FastBully.getInstance().stopCoordinationMessageTimeout();
        FastBully.getInstance().stopAnswerMessageTimeout();
    }

    public void respondToIamUpMessage(@NonNull Server server) throws InterruptedException {
        logger.info("Received IamUp from " + server.getServerId());
        FastBully.getInstance().sendViewMessage(server);
        ServerState.getInstance().addServerToServerView(server);
    }

    public void respondToViewMessage(@NonNull Server server, @NonNull JSONObject message)  {
        List<String> view = (List<String>) message.get("processes");
        logger.info("Received view from " + server.getServerId() + " | view: " + view);
        FastBully.getInstance().setViewMessagesReceived(true);
        view.add(server.getServerId());
        ServerState.getInstance().compareAndSetView(view);
    }

    public void respondToRequestClientIdApprovalMessage(JSONObject receivedMessage) throws InterruptedException {
        String requestedID = (String) receivedMessage.get("identity");
        JSONObject response = null;
        if (ClientManager.checkClientIdentityAvailability(requestedID)){
            response = ServerMessage.getApproveClientIDMessage(Main.SERVER_ID,"true", requestedID);
            ClientManager.addToGlobalClientsList(requestedID, receivedMessage.get("serverId").toString());
        } else {
            response = ServerMessage.getApproveClientIDMessage(Main.SERVER_ID,"false", requestedID);
        }

        sendApproveClientIdMessage(ServerState.getInstance().getServerByServerId(receivedMessage.get("serverId").toString()), response.toJSONString());
    }

    public void respondToApproveClientIdMessage(JSONObject receivedMessage) {
        String approved = (String) receivedMessage.get("approved");
        String identity = (String) receivedMessage.get("identity");

        ClientManager.getClientHandler(identity).getMessageHandler().setIdApprovalReceived(Boolean.parseBoolean(approved));
        ClientManager.getClientHandler(identity).getMessageHandler().setWaitingForIdApproval(false);
    }

    public void respondToInformClientIdCreationMessage(JSONObject receivedMessage) {
        String serverId = (String) receivedMessage.get("serverId");
        String identity = (String) receivedMessage.get("identity");

        ClientManager.addToGlobalClientsList(identity, serverId);
    }

    public void respondToRequestRoomIdApprovalMessage(JSONObject receivedMessage) throws InterruptedException {
        String requestedID = (String) receivedMessage.get("identity");
        String clientID = (String) receivedMessage.get("clientId");

        JSONObject response = null;
        if (RoomManager.checkRoomIdAvailability(requestedID)){
            response = ServerMessage.getApproveRoomIDMessage(Main.SERVER_ID,"true", requestedID, clientID);
            RoomManager.addToGlobalRoomsList(requestedID, receivedMessage.get("serverId").toString());
        } else {
            response = ServerMessage.getApproveRoomIDMessage(Main.SERVER_ID,"false", requestedID, clientID);
        }

        sendApproveRoomIdMessage(ServerState.getInstance().getServerByServerId(receivedMessage.get("serverId").toString()), response.toJSONString());
    }

    public void respondToApproveRoomIdMessage(JSONObject receivedMessage) {
        String approved = (String) receivedMessage.get("approved");
        String clientId = (String) receivedMessage.get("clientId");

        ClientManager.getClientHandler(clientId).getMessageHandler().setIdApprovalReceived(Boolean.parseBoolean(approved));
        ClientManager.getClientHandler(clientId).getMessageHandler().setWaitingForIdApproval(false);
    }

    public void respondToInformRoomIdCreationMessage(JSONObject receivedMessage) {
        String serverId = (String) receivedMessage.get("serverId");
        String identity = (String) receivedMessage.get("identity");

        RoomManager.addToGlobalRoomsList(identity, serverId);
    }


    private void sendApproveClientIdMessage(Server server, String message) throws InterruptedException {
        logger.info("Send approveClientId to: " + server.getServerId());
        //Thread.sleep(1000L); // delay reply
        try {
            Socket socket = new Socket(server.getServerAddress(), server.getCoordinationPort());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.write((message + "\n").getBytes(StandardCharsets.UTF_8));
            dataOutputStream.flush();

        } catch (IOException e) {
            logger.error(e.getLocalizedMessage() + ": " + server.getServerId());
        }
    }

    private void sendApproveRoomIdMessage(Server server, String message) throws InterruptedException {
        logger.info("Send approveRoomId to: " + server.getServerId());
        //Thread.sleep(1000L); // delay reply
        try {
            Socket socket = new Socket(server.getServerAddress(), server.getCoordinationPort());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.write((message + "\n").getBytes(StandardCharsets.UTF_8));
            dataOutputStream.flush();

        } catch (IOException e) {
            logger.error(e.getLocalizedMessage() + ": " + server.getServerId());
        }
    }

}
