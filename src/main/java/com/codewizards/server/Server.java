package com.codewizards.server;

import lombok.Getter;
import lombok.Setter;

public class Server {

    @Getter @Setter
    private String serverId;

    @Getter @Setter
    private String serverAddress;

    @Getter @Setter
    private int clientPort;

    @Getter @Setter
    private int coordinationPort;

    public Server(String serverId, String serverAddress, int clientPort, int coordinationPort) {
        this.serverId = serverId;
        this.serverAddress = serverAddress;
        this.clientPort = clientPort;
        this.coordinationPort = coordinationPort;
    }
}
