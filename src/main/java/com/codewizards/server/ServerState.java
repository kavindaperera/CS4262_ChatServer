package com.codewizards.server;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
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
    private final ConcurrentHashMap<String, Server> serverView = new ConcurrentHashMap<>();

    @Getter
    private Server ownServer;

    @Getter
    @Setter
    private Server coordinator;

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

    public void addServerToServerView(@NonNull Server server) {
        serverView.put(server.getServerId(), server);
        logger.info("Server " + server.getServerId() + " added to view");
    }

    public void removeServerFromServerView(@NonNull Server server) {
        serverView.remove(server.getServerId());
        logger.info("Server " + server.getServerId() + " removed from view");
    }

    public List<Server> getServerListAsArrayList() {
        return new ArrayList<>(serverList.values());
    }

    public List<String> getServerIdList() {
        return new ArrayList<>(serverList.keySet());
    }

    public Server getServerByServerId(@NonNull String serverId) {
        if (ownServer.getServerId().equalsIgnoreCase(serverId)) {
            return ownServer;
        }
        return serverList.get(serverId);
    }

    public List<String> getServerViewAsArrayList() {
        return new ArrayList<>(serverView.keySet());
    }

    public List<Server> getServerViewAsServerArrayList() {
        return new ArrayList<>(serverView.values());
    }

    public void compareAndSetView(@NonNull List<String> view) {
        for (String server : view) {
            if (server.equalsIgnoreCase(ownServer.getServerId())) continue;
            addServerToServerView(getServerByServerId(server));
        }
        logger.info("New view: " + getServerViewAsArrayList());
    }

    public synchronized List<Server> getServersWithHigherPriority() {
        Iterator<Server> servers = serverList.values().iterator();
        List<Server> higherPriorityServerList = new ArrayList<>(); // change to class level list
        while (servers.hasNext()) {
            Server server = servers.next();
            if (ownServer.compareTo(server) > 0) {
                logger.info("Higher priority server found: " + server.getServerId());
                higherPriorityServerList.add(server);
            }
        }
        return higherPriorityServerList;
    }

    public synchronized Server getHighestPriorityServer() {
        Server highestPriorityServer = ownServer;
        for (Server server : serverView.values()) {
            if (highestPriorityServer.compareTo(server) > 0) {
                highestPriorityServer = server;
            }
        }
        return highestPriorityServer;
    }

    public synchronized List<Server> getServersWithLowerPriority() {
        List<Server> lowerPriorityServerList = new ArrayList<>(); // change to class level list
        for (Server server : serverList.values()) {
            if (ownServer.compareTo(server) < 0) {
                logger.info("Lower priority server found: " + server.getServerId());
                lowerPriorityServerList.add(server);
            }
        }
        return lowerPriorityServerList;
    }

    public int getOwnServerPriority(){
        return Integer.parseInt(ownServer.getServerId().substring(1));
    }

}
