package com.codewizards.client;

import com.codewizards.message.ClientMessage;
import com.codewizards.room.RoomManager;
import com.codewizards.server.ServerHandler;

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

public class ClientHandler extends Thread{

    public static Logger logger = Logger.getLogger(ClientHandler.class.getName());

    private ClientState clientState;
    private final Socket clientSocket;
    private MessageHandler messageHandler;
    private JSONParser parser;
    private DataOutputStream writer;
    private BufferedReader reader;

    public ClientHandler(@NonNull Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.messageHandler = new MessageHandler();
        this.parser = new JSONParser();
    }

    @Override
    public void run() {
        try {
            this.writer = new DataOutputStream(clientSocket.getOutputStream());

            this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));

            JSONObject receivedMessage;
            String type;
            while(true) {
                receivedMessage = (JSONObject) parser.parse(reader.readLine());
                type = (String) receivedMessage.get("type");

                switch (type) {
                    case "newidentity": {
                        JSONObject response = this.messageHandler.respondToIdentityRequest(receivedMessage);
                        writer.write((response.toJSONString() + "\n").getBytes("UTF-8"));

                        if (response.get("approved").toString().equalsIgnoreCase("true")) {

                            this.clientState = new ClientState(RoomManager.MAINHALL_ID, receivedMessage.get("identity").toString(), clientSocket);
                            RoomManager.getLocalRoomsList().get(RoomManager.MAINHALL_ID).getClientHashMap().put(receivedMessage.get("identity").toString(), this.clientState);

                            JSONObject broadcast = ClientMessage.getRoomChangeBroadcast(receivedMessage.get("identity").toString(), "", RoomManager.MAINHALL_ID);
                            writer.write((broadcast.toJSONString() + "\n").getBytes("UTF-8"));

                            RoomManager.broadcastToChatRoom(clientState.getRoomId(), clientState.getClientId(), broadcast.toJSONString());
                        }
                        writer.flush();
                        break;
                    }
                    case "list": {
                        JSONObject response = this.messageHandler.respondToListRequest();
                        writer.write((response.toJSONString() + "\n").getBytes("UTF-8"));
                        writer.flush();
                        break;
                    }
                    case "who": {
                        this.messageHandler.respondToWhoRequest();
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
