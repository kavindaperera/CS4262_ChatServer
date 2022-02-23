package com.codewizards.server;

import lombok.NonNull;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ServerHandler extends Thread{

    public static Logger logger = Logger.getLogger(ServerHandler.class.getName());

    private final Socket serverSocket;

    private MessageHandler messageHandler;

    private JSONParser parser;

    public ServerHandler(@NonNull Socket serverSocket) {
        this.serverSocket = serverSocket;
        this.messageHandler = new MessageHandler();
        this.parser = new JSONParser();
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    serverSocket.getInputStream(), "UTF-8"));

            JSONObject receivedMessage;
            String type;
            while(true) {
                receivedMessage = (JSONObject) parser.parse(reader.readLine());
                type = (String) receivedMessage.get("type");
                logger.debug("Received: " + receivedMessage);
                switch (type) {
                    case "election": {
                        this.messageHandler.respondToElectionMessage();
                        break;
                    }
                    case "answer": {
                        this.messageHandler.respondToAnswerMessage();
                        break;
                    }
                    case "nomination": {
                        this.messageHandler.respondToNominationMessage();
                        break;
                    }
                    case "coordinator": {
                        this.messageHandler.respondToCoordinatorMessage();
                        break;
                    }
                    case "IamUp": {
                        this.messageHandler.respondToIamUpMessage();
                        break;
                    }
                    case "view": {
                        this.messageHandler.respondToViewMessage();
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
