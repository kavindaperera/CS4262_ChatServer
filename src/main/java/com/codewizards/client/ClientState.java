package com.codewizards.client;

import lombok.Getter;
import lombok.Setter;

import java.net.Socket;

public class ClientState {

    @Getter
    @Setter
    private String roomId;

    @Getter
    @Setter
    private String clientId;

    @Getter
    @Setter
    private Socket socket;

    @Getter
    @Setter
    private String ownRoomId = "";

    public ClientState(String roomId, String clientId, Socket socket) {
        this.roomId = roomId;
        this.clientId = clientId;
        this.socket = socket;
    }

    @Override
    public String toString() {
        return "ClientState{" +
                "roomId='" + roomId + '\'' +
                ", clientId='" + clientId + '\'' +
                ", socket=" + socket +
                '}';
    }
}
