package com.codewizards;

import com.codewizards.client.ClientHandler;
import com.codewizards.election.FastBully;
import com.codewizards.room.RoomManager;
import com.codewizards.server.Server;
import com.codewizards.server.ServerHandler;
import com.codewizards.server.ServerState;
import org.apache.log4j.Logger;
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
                        logger.info("Server Connected | local port: " + socket.getLocalPort()  + " | remote port: " + socket.getPort());
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
                socket.setSoTimeout(Constants.SOCKET_TIMEOUT);
                logger.debug("Client Connected.....");
                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandler.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void selectCoordinator() {
        logger.info("Selecting coordinator");
        FastBully.getInstance().notifyIamUp(ServerState.getInstance().getServerListAsArrayList());
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
        String mainHallId = "MainHall-" + SERVER_ID;
        RoomManager.createChatRoom(mainHallId, "");
    }

}
