package com.codewizards;

import com.codewizards.client.ClientHandler;
import com.codewizards.room.RoomManager;
import com.codewizards.server.ServerHandler;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static String SERVER_ID;
    public static String SERVER_CONF;

    public static void main(String[] args) throws IOException {

        SERVER_ID = args[0];
        SERVER_CONF = args[1];

        initialize();
        final int[] portDetails = getServerConfiguration();

        // listen to server connections
        final ServerSocket serverSocket = new ServerSocket(portDetails[1]);
        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Socket socket = serverSocket.accept();
                        System.out.println("Server Connected.....");
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
        ServerSocket clientSocket = new ServerSocket(portDetails[0]);
        while(true){
            try {
                Socket socket = clientSocket.accept();
                System.out.println("Client Connected.....");
                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandler.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private static int[] getServerConfiguration() {

        int[] portDetails = new int[2]; // index 0 for client port & index 1 for server port
        try {
            BufferedReader reader = new BufferedReader(new FileReader(SERVER_CONF));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] details = line.split("\t");
                if (details[0].equalsIgnoreCase(SERVER_ID)) {
                    portDetails[0] = Integer.parseInt(details[2]);
                    portDetails[1] = Integer.parseInt(details[3]);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return portDetails;
    }

    private static void initialize() {
        String roomId = "MainHall-" + SERVER_ID;
        RoomManager.createMainHall(roomId);
    }

}
