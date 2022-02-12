package com.codewizards.client;

import lombok.NonNull;

import java.net.Socket;

public class ClientHandler extends Thread{

    private final Socket clientSocket;

    public ClientHandler(@NonNull Socket clientSocket) {
        this.clientSocket = clientSocket;
    }




}
