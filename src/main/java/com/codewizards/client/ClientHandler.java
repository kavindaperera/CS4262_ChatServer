package com.codewizards.client;

import java.net.Socket;

public class ClientHandler extends Thread{

    private final Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }




}
