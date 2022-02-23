package com.codewizards;

import com.codewizards.client.ClientHandler;
import com.codewizards.election.FastBully;
import com.codewizards.room.RoomManager;
import com.codewizards.server.Server;
import com.codewizards.server.ServerHandler;
import com.codewizards.server.ServerState;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static Logger logger = null;

    public static String SERVER_ID;
    public static String SERVER_CONF;

    public static void main(String[] args) throws IOException {

        // initialize log4j
        loadLog4J();
        logger = Logger.getLogger(Main.class.getName());

        //load command line args
        CmdLineValues values = new CmdLineValues();
        CmdLineParser parser = new CmdLineParser(values);

        try {
            parser.parseArgument(args);
            SERVER_ID = values.getServerId();
            SERVER_CONF = values.getServersConf();
        } catch (CmdLineException e) {
            logger.error("Error while parsing cmd line arguments: " + e.getLocalizedMessage());
        }

        initialize();

        // listen to server connections
        final ServerSocket serverSocket = new ServerSocket(ServerState.getInstance().getOwnServer().getCoordinationPort());
        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
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

        selectCoordinator(); // bully test

        // listen to client connections
        ServerSocket clientSocket = new ServerSocket(ServerState.getInstance().getOwnServer().getClientPort());
        while (true) {
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

    private static void selectCoordinator() throws IOException { // bully test
        logger.info("Selecting coordinator");
        if (ServerState.getInstance().getServersWithHigherPriority().isEmpty()){
            logger.debug("I am the highest priority server");
        } else {
            FastBully.getInstance().startElection(ServerState.getInstance().getServersWithHigherPriority());
        }
    }

    private static void getServerConfigurations() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(SERVER_CONF));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] details = line.split("\t");
                Server server = new Server(details[0], details[1], Integer.parseInt(details[2]), Integer.parseInt(details[3]));
                ServerState.getInstance().addServerToServerList(server, SERVER_ID);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initialize() {
        getServerConfigurations();
        RoomManager.initializeGlobalRoomsList();
        String mainHallId = "MainHall-" + SERVER_ID;
        RoomManager.createChatRoom(mainHallId, "");
    }

    public static void loadLog4J() {
        String log4j_path = System.getProperty("user.dir") + "/log4j.properties";
        PropertyConfigurator.configure(log4j_path);
    }

}
