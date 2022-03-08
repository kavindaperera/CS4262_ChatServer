package com.codewizards.client;

import com.codewizards.Main;
import com.codewizards.message.ClientMessage;
import com.codewizards.message.ServerMessage;
import com.codewizards.room.RoomManager;
import com.codewizards.server.Server;

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
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.List;

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

    public String doUpdatesForRoomId(String roomId, String clientId){
        String previousRoom = this.clientState.getRoomId();
        RoomManager.getLocalRoomsList().get(previousRoom).getClientHashMap().remove(clientId);
        RoomManager.createChatRoom(roomId, clientId);
        this.clientState.setRoomId(roomId);
        this.clientState.setOwnRoomId(roomId);
        RoomManager.getLocalRoomsList().get(roomId).getClientHashMap().put(clientId, this.clientState);

        return previousRoom;
    }

    public void informServersNewRoomId(String roomId) {
        for (Server server : ServerState.getInstance().getServerListAsArrayList()) {
            if (!server.equals(ServerState.getInstance().getCoordinator())) {
                logger.info("Send informRoomIdCreation to: " + server.getServerId());
                try {
                    Socket socket = new Socket(server.getServerAddress(), server.getCoordinationPort());
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    dataOutputStream.write((ServerMessage.getInformRoomIdCreationMessage(ServerState.getInstance().getOwnServer().getServerId(), roomId) + "\n").getBytes(StandardCharsets.UTF_8));
                    dataOutputStream.flush();

                } catch (IOException e) {
                    logger.error(e.getLocalizedMessage() + ": " + server.getServerId());
                }
            }
        }
    }

    public void doUpdatesForMoveJoin(JSONObject tempMessage){
        this.clientState = new ClientState(tempMessage.get("roomid").toString(), tempMessage.get("identity").toString(), clientSocket);
        RoomManager.getLocalRoomsList().get(tempMessage.get("roomid").toString()).getClientHashMap().put(tempMessage.get("identity").toString(), this.clientState);
        ClientManager.addToLocalClientsList(tempMessage.get("identity").toString());
        ClientManager.addToGlobalClientsList(tempMessage.get("identity").toString(), Main.SERVER_ID);
    }

    public void doUpdatesWhenDeletingRoom(String roomId) throws IOException {
        List<String> clientsOfDeletingRoom = RoomManager.getLocalRoomsList().get(roomId).getClientsAsList();
        for (String client: clientsOfDeletingRoom) {
            JSONObject roomChange = ClientMessage.getRoomChangeBroadcast(client, roomId, RoomManager.MAINHALL_ID);
            RoomManager.broadcastToChatRoom(roomId, clientState.getClientId(), roomChange.toJSONString());
            RoomManager.broadcastToChatRoom(RoomManager.MAINHALL_ID, clientState.getClientId(), roomChange.toJSONString());
            writer.write((roomChange.toJSONString() + "\n").getBytes(StandardCharsets.UTF_8));
            writer.flush();
        }

        RoomManager.removeFromGlobalRoomsList(roomId);
        RoomManager.getLocalRoomsList().remove(roomId).changeRoomOfClients(clientState.getClientId());
    }

    public void doUpdatesWhenQuiting() {
        if (!clientState.getOwnRoomId().equalsIgnoreCase("")) {
            List<String> clientsOfDeletingRoom = RoomManager.getLocalRoomsList().get(clientState.getOwnRoomId()).getClientsAsList();
            JSONObject roomChange;
            for (String client: clientsOfDeletingRoom) {
                if (client.equalsIgnoreCase(clientState.getClientId())) {
                    roomChange = ClientMessage.getRoomChangeBroadcast(client, clientState.getOwnRoomId(), "");
                } else {
                    roomChange = ClientMessage.getRoomChangeBroadcast(client, clientState.getOwnRoomId(), RoomManager.MAINHALL_ID);
                }
                RoomManager.broadcastToChatRoom(clientState.getOwnRoomId(), clientState.getClientId(), roomChange.toJSONString());
                RoomManager.broadcastToChatRoom(RoomManager.MAINHALL_ID, clientState.getClientId(), roomChange.toJSONString());
                if (!client.equalsIgnoreCase(clientState.getClientId())) {
                    try {
                        writer.write((roomChange.toJSONString() + "\n").getBytes(StandardCharsets.UTF_8));
                        writer.flush();
                    } catch (IOException e) {
                        logger.error("Communication Error: " + e.getMessage());
                    }
                }
            }
            RoomManager.removeFromGlobalRoomsList(clientState.getOwnRoomId());
            RoomManager.getLocalRoomsList().remove(clientState.getOwnRoomId()).changeRoomOfClients(clientState.getClientId());

            informServersRoomDeletion(clientState.getOwnRoomId());
        }

        JSONObject myRoomChange = ClientMessage.getRoomChangeBroadcast(clientState.getClientId(), clientState.getRoomId(), "");
        if (clientState.getOwnRoomId().equalsIgnoreCase("")) {
            RoomManager.broadcastToChatRoom(clientState.getRoomId(), clientState.getClientId(), myRoomChange.toJSONString());
            RoomManager.getLocalRoomsList().get(clientState.getRoomId()).getClientHashMap().remove(clientState.getClientId());
        }
        try {
            writer.write((myRoomChange.toJSONString() + "\n").getBytes(StandardCharsets.UTF_8));
            writer.flush();
        } catch (IOException e) {
            logger.error("Communication Error: " + e.getMessage());
        }

        ClientManager.removeFromGlobalClientList(clientState.getClientId());
        ClientManager.removeFromLocalClientList(clientState.getClientId());

        informServersClientDeletion(clientState.getClientId());
    }

    public void informServersRoomDeletion(String roomId) {
        for (Server server : ServerState.getInstance().getServerListAsArrayList()) {
            logger.info("Send deleteroom to: " + server.getServerId());
            try {
                Socket socket = new Socket(server.getServerAddress(), server.getCoordinationPort());
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.write((ServerMessage.getInformRoomIdDeletionMessage(ServerState.getInstance().getOwnServer().getServerId(), roomId) + "\n").getBytes(StandardCharsets.UTF_8));
                dataOutputStream.flush();

            } catch (IOException e) {
                logger.error(e.getLocalizedMessage() + ": " + server.getServerId());
            }

        }
    }

    public void informServersClientDeletion(String clientId) {
        for (Server server : ServerState.getInstance().getServerListAsArrayList()) {
            logger.info("Send deleteclient to: " + server.getServerId());
            try {
                Socket socket = new Socket(server.getServerAddress(), server.getCoordinationPort());
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.write((ServerMessage.getInformClientIdDeletionMessage(ServerState.getInstance().getOwnServer().getServerId(), clientId) + "\n").getBytes(StandardCharsets.UTF_8));
                dataOutputStream.flush();

            } catch (IOException e) {
                logger.error(e.getLocalizedMessage() + ": " + server.getServerId());
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
                        String roomId = (String) receivedMessage.get("roomid");
                        JSONObject response = this.messageHandler.respondToCreateRoomRequest(roomId, clientState);
                        writer.write((response.toJSONString() + "\n").getBytes(StandardCharsets.UTF_8));

                        if (response.get("approved").toString().equalsIgnoreCase("true")) {

                            String previousRoom = doUpdatesForRoomId(roomId, clientState.getClientId());
                            informServersNewRoomId(roomId);

                            JSONObject broadcast = ClientMessage.getRoomChangeBroadcast(clientState.getClientId(), previousRoom, roomId);
                            writer.write((broadcast.toJSONString() + "\n").getBytes(StandardCharsets.UTF_8));

                            RoomManager.broadcastToChatRoom(previousRoom, clientState.getClientId(), broadcast.toJSONString());
                        }
                        writer.flush();
                        break;
                    }
                    case "joinroom": {
                        String roomId = (String) receivedMessage.get("roomid");
                        JSONObject response = this.messageHandler.respondToJoinRoomRequest(roomId, clientState);
                        writer.write((response.toJSONString() + "\n").getBytes(StandardCharsets.UTF_8));

                        if (!response.get("type").toString().equalsIgnoreCase("route")) {
                            if (!response.get("former").toString().equalsIgnoreCase(response.get("roomid").toString())) {
                                // broadcast to previous room & remove from it
                                RoomManager.broadcastToChatRoom(clientState.getRoomId(), clientState.getClientId(), response.toJSONString());
                                RoomManager.getLocalRoomsList().get(clientState.getRoomId()).getClientHashMap().remove(clientState.getClientId());
                                // add to new room & broadcast to it
                                clientState.setRoomId(roomId);
                                RoomManager.getLocalRoomsList().get(roomId).getClientHashMap().put(clientState.getClientId(), clientState);
                                RoomManager.broadcastToChatRoom(clientState.getRoomId(), clientState.getClientId(), response.toJSONString());
                            }
                        } else {
                            JSONObject broadcast = ClientMessage.getRoomChangeBroadcast(clientState.getClientId(), clientState.getRoomId(), roomId);
                            RoomManager.broadcastToChatRoom(clientState.getRoomId(), clientState.getClientId(), broadcast.toJSONString());

                            // remove from previous room's client list and server's client list
                            RoomManager.getLocalRoomsList().get(clientState.getRoomId()).getClientHashMap().remove(clientState.getClientId());
                            ClientManager.removeFromLocalClientList(clientState.getClientId());

                            ClientManager.removeFromClientHandlerList(clientState.getClientId());
                            isRunning = false;
                        }
                        break;
                    }
                    case "movejoin": {
                        String former = (String) receivedMessage.get("former");
                        String roomId = (String) receivedMessage.get("roomid");
                        String identity = (String) receivedMessage.get("identity");
                        ClientManager.addToClientHandlersList(identity, this);

                        JSONObject response = this.messageHandler.respondToMoveJoinRequest(former, roomId, identity);

                        doUpdatesForMoveJoin(response);
                        RoomManager.broadcastToChatRoom(clientState.getRoomId(), clientState.getClientId(), response.toJSONString());

                        if (response.get("roomid").toString().equalsIgnoreCase(roomId)) {
                            JSONObject serverChange = ClientMessage.getServerChange("true", Main.SERVER_ID);
                            writer.write((serverChange.toJSONString() + "\n").getBytes(StandardCharsets.UTF_8));
                            writer.write((response.toJSONString() + "\n").getBytes(StandardCharsets.UTF_8));
                        }
                        writer.flush();
                        break;
                    }
                    case "deleteroom": {
                        String roomId = (String) receivedMessage.get("roomid");
                        JSONObject response = this.messageHandler.respondToDeleteRoomRequest(roomId, clientState);

                        if (response.get("approved").toString().equalsIgnoreCase("true")) {
                            doUpdatesWhenDeletingRoom(roomId);
                            informServersRoomDeletion(roomId);
                            clientState.setRoomId(RoomManager.MAINHALL_ID);
                            RoomManager.getLocalRoomsList().get(RoomManager.MAINHALL_ID).getClientHashMap().put(clientState.getClientId(), clientState);
                        }
                        writer.write((response.toJSONString() + "\n").getBytes(StandardCharsets.UTF_8));
                        writer.flush();
                        break;
                    }
                    case "quit": {
                        doUpdatesWhenQuiting();
                        ClientManager.removeFromClientHandlerList(clientState.getClientId());
                        isRunning = false;
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
        } catch (SocketException e) {
            logger.error("Connection Error: " + e.getMessage());
            doUpdatesWhenQuiting();
            ClientManager.removeFromClientHandlerList(clientState.getClientId());
        } catch (IOException e) {
            logger.error("Communication Error: " + e.getMessage());
        }
    }


}
