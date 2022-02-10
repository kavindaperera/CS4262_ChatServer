package com.codewizards.server;

import java.net.Socket;

public class ServerHandler extends Thread{

    private final Socket serverSocket;

    public ServerHandler(Socket serverSocket) {
        this.serverSocket = serverSocket;
    }

}
