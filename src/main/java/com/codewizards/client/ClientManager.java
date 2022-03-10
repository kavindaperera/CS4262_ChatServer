package com.codewizards.client;

import lombok.Getter;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ClientManager {

    public static Logger logger = Logger.getLogger(ClientManager.class.getName());

    private static final ConcurrentHashMap<String, String> globalClientsList = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, ClientHandler> clientHandlersList = new ConcurrentHashMap<>();

    @Getter
    private static final ArrayList<String> localClientsList = new ArrayList<>();

    public ClientManager() {

    }

    public static List<String> getGlobalClientsListAsArrayList(){
        List<String> clientsList = new ArrayList<>(globalClientsList.keySet());
        return clientsList;
    }

    public static boolean checkClientIdentityAvailability(String requestedClientID) {
        return !globalClientsList.containsKey(requestedClientID);
    }

    public static void addToGlobalClientsList(String clientID, String serverID) {
        globalClientsList.put(clientID, serverID);
    }

    public static void addToLocalClientsList(String clientID) {
        synchronized (localClientsList) {
            localClientsList.add(clientID);
        }
    }

    public static void addToClientHandlersList(String clientID, ClientHandler clientHandler) {
        clientHandlersList.put(clientID, clientHandler);
    }

    public static ClientHandler getClientHandler(String clientID) {
        return clientHandlersList.get(clientID);
    }

    public static void removeFromClientHandlerList(String clientID) {
        clientHandlersList.remove(clientID);
    }

    public static void removeFromLocalClientList(String clientID) {
        synchronized (localClientsList) {
            localClientsList.remove(clientID);
        }
    }

    public static void removeFromGlobalClientList(String clientID) {
        globalClientsList.remove(clientID);
    }

    public static void removeClientsOnFailure(String serverID) {
        synchronized (globalClientsList) {
            globalClientsList.entrySet().removeIf(
                    entry -> entry.getValue()
                            .compareTo(serverID) == 0);
        }
    }

}
