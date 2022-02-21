package com.codewizards;

import com.codewizards.client.ClientHandler;
import com.codewizards.room.RoomManager;
import com.codewizards.server.ServerHandler;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Main {

    public static Logger logger = null;

    public static String SERVER_ID;
    public static String SERVER_CONF;
    public static HashMap<String, List<String>> serverConfigs = new HashMap<>();

    public static void main(String[] args) throws IOException {

        SERVER_ID = args[0];
        SERVER_CONF = args[1];

        loadLog4J(); // initialize log4j

        initialize();

        logger = Logger.getLogger(Main.class.getName());

        // listen to server connections
        final ServerSocket serverSocket = new ServerSocket(Integer.parseInt(serverConfigs.get(SERVER_ID).get(3)));
        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Socket socket = serverSocket.accept();
                        logger.debug("Server Connected.....");
                        ServerHandler serverHandler = new ServerHandler(socket);
                        serverHandler.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        serverThread.start();


        // listen to client connections
        ServerSocket clientSocket = new ServerSocket(Integer.parseInt(serverConfigs.get(SERVER_ID).get(2)));
        while(true){
            try {
                Socket socket = clientSocket.accept();
                logger.debug("Client Connected.....");
                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandler.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private static void getServerConfigurations() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(SERVER_CONF));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] details = line.split("\t");
                serverConfigs.put(details[0], Arrays.asList(details));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initialize() {
        getServerConfigurations();
        RoomManager.initializeGlobalRoomsList();
        String roomId = "MainHall-" + SERVER_ID;
        RoomManager.createChatRoom(roomId, "");
    }

    public static void loadLog4J(){
        String log4j_path = System.getProperty("user.dir") + "/log4j.properties";
        PropertyConfigurator.configure(log4j_path);
    }

}
