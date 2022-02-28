package com.codewizards.server;

import com.codewizards.election.FastBully;
import lombok.NonNull;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

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
                    serverSocket.getInputStream(), StandardCharsets.UTF_8));

            JSONObject message = (JSONObject) parser.parse(reader.readLine());
            String type = (String) message.get("type");
            Server server = ServerState.getInstance().getServerByServerId((String)message.get("serverId"));

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
                    this.messageHandler.respondToCoordinatorMessage(server);
                    break;
                }
                case "IamUp": {
                    this.messageHandler.respondToIamUpMessage(server);
                    break;
                }
                case "view": {
                    this.messageHandler.respondToViewMessage(server, message);
                    break;
                }
                case "requestClientIdApproval": {
                    this.messageHandler.respondToRequestClientIdApprovalMessage(message);
                    break;
                }
                case "approveClientId": {
                    this.messageHandler.respondToApproveClientIdMessage(message);
                    break;
                }
                case "heartbeat":{
                    logger.debug("Received heartbeat message from: " + message);
                    if (server.equals(ServerState.getInstance().getCoordinator()) && !server.equals(ServerState.getInstance().getOwnServer())){
                        FastBully.getInstance().resetHeartbeatWaitTimeout();
                    }
                    break;
                }
            }
            serverSocket.close();

        } catch (ParseException e) {
            logger.error("Message Error: " + e.getMessage());
        } catch (IOException e) {
            logger.error("Communication Error: " + e.getMessage());
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }
    }
}
