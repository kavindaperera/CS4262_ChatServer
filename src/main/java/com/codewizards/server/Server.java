package com.codewizards.server;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public class Server implements Comparable{

    @Getter @Setter
    private String serverId;

    @Getter @Setter
    private String serverAddress;

    @Getter @Setter
    private int clientPort;

    @Getter @Setter
    private int coordinationPort;

    public Server(@NonNull String serverId, @NonNull String serverAddress, @NonNull int clientPort, @NonNull int coordinationPort) {
        this.serverId = serverId;
        this.serverAddress = serverAddress;
        this.clientPort = clientPort;
        this.coordinationPort = coordinationPort;
    }

    @Override
    public String toString() {
        return "Server{" +
                "serverId='" + serverId + '\'' +
                ", serverAddress='" + serverAddress + '\'' +
                ", clientPort=" + clientPort +
                ", coordinationPort=" + coordinationPort +
                '}';
    }


    @Override
    public int compareTo(Object o) {
        if (o instanceof Server) {
            return Integer.parseInt(serverId.substring(1)) - Integer.parseInt(((Server) o).getServerId().substring(1));
        }
        throw new IllegalArgumentException(o.getClass().getName() + " is not a " + this.getClass().getName());
    }
}
