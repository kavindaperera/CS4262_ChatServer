package com.codewizards.server;

import lombok.Getter;
import lombok.NonNull;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ServerState {

    public static Logger logger = Logger.getLogger(ServerState.class.getName());

    private static ServerState INSTANCE;

    @Getter
    private final ConcurrentHashMap<String, Server> serverList = new ConcurrentHashMap<>();

    @Getter
    private Server ownServer;

    private ServerState() {

    }

    public static synchronized ServerState getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ServerState();
        }
        return INSTANCE;
    }

    public synchronized void addServerToServerList(@NonNull Server server, @NonNull String ownId) {
        if (server.getServerId().equalsIgnoreCase(ownId)) {
            logger.info("Own Server added: " + server.toString());
            this.ownServer = server;
        } else {
            logger.info("Server added: " + server.toString());
            serverList.put(server.getServerId(), server);
        }
    }

    public List<Server> getServerListAsArrayList(){
        List<Server> returnList = new ArrayList<>();
        returnList.addAll(serverList.values());
        return returnList;
    }

    public Server getServerByServerId(String serverId){
        return serverList.get(serverId);
    }

    public synchronized List<Server> getServersWithHigherPriority() { // TODO - have a class level higher priority server list
        Iterator<Server> servers = this.getServerList().values().iterator();
        List<Server> higherPriorityServerList = new ArrayList<>();
        while (servers.hasNext()) {
            Server server = servers.next();
            if (ownServer.compareTo(server) > 0) {
                logger.info("Higher priority server found: " + server.toString());
                higherPriorityServerList.add(server);
            }
        }
        return higherPriorityServerList;
    }

}
