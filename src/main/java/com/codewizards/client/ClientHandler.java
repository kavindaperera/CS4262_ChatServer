package com.codewizards.client;

import lombok.NonNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientHandler extends Thread{

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
                        String response = this.messageHandler.respondToIdentityRequest(receivedMessage);
                        writer.write((response + "\n").getBytes("UTF-8"));
                        writer.flush();
                        break;
                    }
                    case "roomchange": {
                        this.messageHandler.respondToRoomChangeRequest();
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

                }
            }
        } catch (ParseException e) {
            System.out.println("Message Error: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Communication Error: " + e.getMessage());
        }
    }


}
