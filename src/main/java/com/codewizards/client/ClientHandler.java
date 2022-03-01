package com.codewizards.client;

import com.codewizards.Main;
import com.codewizards.message.ClientMessage;
import com.codewizards.message.ServerMessage;
import com.codewizards.room.RoomManager;
import com.codewizards.server.Server;
import com.codewizards.server.ServerHandler;

import com.codewizards.server.ServerState;
import lombok.Getter;
import lombok.NonNull;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandler extends Thread{

    public static Logger logger = Logger.getLogger(ClientHandler.class.getName());

    private ClientState clientState;

    private final Socket clientSocket;

    @Getter
    private MessageHandler messageHandler;

    private JSONParser parser;

    private DataOutputStream writer;

    private BufferedReader reader;

    public ClientHandler(@NonNull Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.messageHandler = new MessageHandler();
        this.parser = new JSONParser();
    }

    public void doUpdatesForClientId(String identity){
        this.clientState = new ClientState(RoomManager.MAINHALL_ID, identity, clientSocket);
        RoomManager.getLocalRoomsList().get(RoomManager.MAINHALL_ID).getClientHashMap().put(identity, this.clientState);
        ClientManager.addToLocalClientsList(identity);
        ClientManager.addToGlobalClientsList(identity, Main.SERVER_ID);
    }

    public void informServersNewClientId(String identity) {
        for (Server server : ServerState.getInstance().getServerListAsArrayList()) {
            if (!server.equals(ServerState.getInstance().getCoordinator())) {
                logger.info("Send informClientIdCreation to: " + server.getServerId());
                try {
                    Socket socket = new Socket(server.getServerAddress(), server.getCoordinationPort());
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    dataOutputStream.write((ServerMessage.getInformClientIdCreationMessage(ServerState.getInstance().getOwnServer().getServerId(), identity) + "\n").getBytes(StandardCharsets.UTF_8));
                    dataOutputStream.flush();

                } catch (IOException e) {
                    logger.error(e.getLocalizedMessage() + ": " + server.getServerId());
                }
            }
        }
    }

    @Override
    public void run() {
        try {
            this.writer = new DataOutputStream(clientSocket.getOutputStream());

            this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));

            boolean isRunning = true;
            JSONObject receivedMessage;
            String type;

            while(isRunning) {
                receivedMessage = (JSONObject) parser.parse(reader.readLine());
                type = (String) receivedMessage.get("type");

                switch (type) {
                    case "newidentity": {
                        String identity = (String) receivedMessage.get("identity");
                        ClientManager.addToClientHandlersList(identity, this);

                        JSONObject response = this.messageHandler.respondToIdentityRequest(identity);
                        writer.write((response.toJSONString() + "\n").getBytes(StandardCharsets.UTF_8));

                        if (response.get("approved").toString().equalsIgnoreCase("true")) {

                            doUpdatesForClientId(identity);
                            informServersNewClientId(identity);

                            JSONObject broadcast = ClientMessage.getRoomChangeBroadcast(identity, "", RoomManager.MAINHALL_ID);
                            writer.write((broadcast.toJSONString() + "\n").getBytes(StandardCharsets.UTF_8));

                            RoomManager.broadcastToChatRoom(clientState.getRoomId(), clientState.getClientId(), broadcast.toJSONString());
                        } else {
                            ClientManager.removeFromClientHandlerList(identity);
                            isRunning = false;
                        }
                        writer.flush();
                        break;
                    }
                    case "list": {
                        JSONObject response = this.messageHandler.respondToListRequest();
                        writer.write((response.toJSONString() + "\n").getBytes(StandardCharsets.UTF_8));
                        writer.flush();
                        break;
                    }
                    case "who": {
                        String currentRoom = clientState.getRoomId();
                        JSONObject response = this.messageHandler.respondToWhoRequest(currentRoom);
                        writer.write((response.toJSONString() + "\n").getBytes(StandardCharsets.UTF_8));
                        writer.flush();
                        break;
                    }
                    case "createroom": {
                        this.messageHandler.respondToCreateRoomRequest();
                        break;
                    }
                    case "joinroom": {
                        this.messageHandler.respondToJoinRoomRequest();
                        break;
                    }
                    case "deleteroom": {
                        this.messageHandler.respondToDeleteRoomRequest();
                        break;
                    }
                    case "quit": {
                        this.messageHandler.respondToQuitRequest();
                        break;
                    }
                    case "message": {
                        JSONObject broadcastMessage = this.messageHandler.respondToReceivedMessage(receivedMessage, clientState.getClientId());
                        RoomManager.broadcastToChatRoom(clientState.getRoomId(), clientState.getClientId(), broadcastMessage.toJSONString());
                        break;
                    }
                }
            }
        } catch (ParseException e) {
            logger.error("Message Error: " + e.getMessage());
        } catch (IOException e) {
            logger.error("Communication Error: " + e.getMessage());
        }
    }


}
