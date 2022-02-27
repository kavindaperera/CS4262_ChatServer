package com.codewizards.client;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ClientManager {

    public static Logger logger = Logger.getLogger(ClientManager.class.getName());

    private static final ConcurrentHashMap<String, String> globalClientsList = new ConcurrentHashMap<>();

    private static final ArrayList<String> localClientsList = new ArrayList<>();

    public ClientManager() {

    }

    public static ArrayList<String> getLocalClientsList() {
        return localClientsList;
    }

    public static List<String> getGlobalClientsListAsArrayList(){
        List<String> clientsList = new ArrayList<>(globalClientsList.keySet());
        return clientsList;
    }

    public static boolean checkClientIdentityAvailability(String requestedClientID) {
        synchronized (globalClientsList) {
            return !globalClientsList.containsKey(requestedClientID);
        }
    }

    public static void addToGlobalClientsList(String clientID, String serverID) {
        synchronized (globalClientsList) {
            globalClientsList.put(clientID, serverID);
        }
    }

    public static void addToLocalClientsList(String clientID) {
        synchronized (localClientsList) {
            localClientsList.add(clientID);
        }
    }

}
