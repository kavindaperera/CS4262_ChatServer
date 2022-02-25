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
            addServerToServerView(server);
        } else {
            logger.info("Server added: " + server.toString());
            serverList.put(server.getServerId(), server);
        }
    }

    private synchronized void addServerToServerView(@NonNull Server server) {
        serverView.put(server.getServerId(), server);
        logger.info("Server " + server.getServerId() + " added to view");
    }

    public synchronized void removeServerFromServerView(@NonNull Server server) {
        serverView.remove(server.getServerId());
        logger.info("Server " + server.getServerId() + " removed from view");
    }

    public List<Server> getServerListAsArrayList(){
        List<Server> returnList = new ArrayList<>();
        returnList.addAll(serverList.values());
        return returnList;
    }

    public List<String> getServerIdList(){
        List<String> returnList = new ArrayList<>();
        returnList.addAll(serverList.keySet());
        return returnList;
    }

    public Server getServerByServerId(String serverId){
        if (ownServer.getServerId().equalsIgnoreCase(serverId)){
            return ownServer;
        }
        return serverList.get(serverId);
    }

    public List<String> getServerViewAsArrayList(){
        List<String> returnList = new ArrayList<>();
        returnList.addAll(serverView.keySet());
        return returnList;
    }

    public void compareAndSetView(@NonNull List<String> view){
        for (String server : view) {
            addServerToServerView(getServerByServerId(server));
        }
        logger.info("New view: " + getServerViewAsArrayList());
    }

    public synchronized List<Server> getServersWithHigherPriority() {
        Iterator<Server> servers = this.getServerView().values().iterator();
        List<Server> higherPriorityServerList = new ArrayList<>();
        while (servers.hasNext()) {
            Server server = servers.next();
            if (ownServer.compareTo(server) > 0) {
                logger.info("Higher priority server found: " + server.getServerId());
                higherPriorityServerList.add(server);
            }
        }
        return higherPriorityServerList;
    }

    public synchronized Server getHighestPriorityServer(){
        Server highestPriorityServer = ownServer;
        for (Server server : serverView.values()) {
            if (highestPriorityServer.compareTo(server) > 0) {
                highestPriorityServer = server;
            }
        }
        return highestPriorityServer;
    }

}
