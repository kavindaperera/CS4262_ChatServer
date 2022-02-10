package com.codewizards;

import oracle.jrockit.jfr.StringConstantPool;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    private static String SERVER_ID;
    private static String SERVER_CONF;

    public static void main(String[] args) throws IOException {

        SERVER_ID = args[0];
        SERVER_CONF = args[1];

        int[] portDetails = getServerConfiguration();

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

}
