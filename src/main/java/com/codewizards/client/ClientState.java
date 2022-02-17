package com.codewizards.client;

import java.net.Socket;

public class ClientState {

    private String roomId;

    private String clientId;

    private Socket socket;

    public ClientState(String roomId, String clientId, Socket socket) {
        this.roomId = roomId;
        this.clientId = clientId;
        this.socket = socket;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}
