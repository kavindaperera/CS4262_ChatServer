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

    private String clientId;
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

                            this.clientId = receivedMessage.get("identity").toString();  // setting the clientId after it has been approved

                            JSONObject broadcast = ClientMessage.getRoomChangeBroadcast(receivedMessage.get("identity").toString(), "", RoomManager.MAINHALL_ID);
                            writer.write((broadcast.toJSONString() + "\n").getBytes("UTF-8"));

                            RoomManager.broadcastToChatRoom(RoomManager.MAINHALL_ID, broadcast.toJSONString());
                        }
                        writer.flush();
                        break;
                    }
                    case "list": {
                        this.messageHandler.respondToListRequest();
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
                        JSONObject broadcastMessage = this.messageHandler.respondToReceivedMessage(receivedMessage, this.clientId);
                        RoomManager.broadcastToChatRoom(RoomManager.MAINHALL_ID, broadcastMessage.toJSONString());
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
