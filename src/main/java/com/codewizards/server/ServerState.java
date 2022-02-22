package com.codewizards.server;

import lombok.Getter;
import lombok.NonNull;
import org.apache.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;

public class ServerState {

    public static Logger logger = Logger.getLogger(ServerState.class.getName());

    private static ServerState INSTANCE;

    @Getter private Server ownServer;

    @Getter private final ConcurrentHashMap<String, Server> serverList = new ConcurrentHashMap<>();

    public static synchronized ServerState getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ServerState();
        }
        return INSTANCE;
    }

    private ServerState() {

    }

    public synchronized void addServerToServerList(@NonNull Server server, @NonNull String ownId){
        if (server.getServerId().equalsIgnoreCase(ownId)){
            logger.info("Own Server added: " + server.toString());
            this.ownServer = server;
        } else {
            logger.info("Server added: " + server.toString());
            serverList.put(server.getServerId(), server);
        }
    }

}
