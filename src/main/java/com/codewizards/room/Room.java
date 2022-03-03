package com.codewizards.room;

import com.codewizards.client.ClientState;
import lombok.Getter;
import lombok.NonNull;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Room {

    @Getter
    private String roomId;

    @Getter
    private String creatorId;

    private final ConcurrentHashMap<String, ClientState> clientHashMap = new ConcurrentHashMap<>();


    /**
     * used to create ChatRooms
     * @param roomId
     * @param creatorId
     */
    public Room(@NonNull String roomId, @NonNull String creatorId) {
        this.roomId = roomId;
        this.creatorId = creatorId;
    }

    public ConcurrentHashMap<String, ClientState> getClientHashMap() {
        return clientHashMap;
    }

    public List<String> getClientsAsList() {
        synchronized (clientHashMap) {
            Iterator<ClientState> clientStateList = clientHashMap.values().iterator();
            List<String> clientList = new ArrayList<>();
            while (clientStateList.hasNext()) {
                clientList.add(clientStateList.next().getClientId());
            }
            return clientList;
        }
    }

    /**
     * @param sender
     * @param message
     */
    public void sendBroadcast(String sender, String message) {
        synchronized (clientHashMap) {
            Iterator<String> clientList = clientHashMap.keySet().iterator();
            DataOutputStream writer;
            String clientId;
            ClientState clientState;
            while (clientList.hasNext()) {
                try {
                    clientId = clientList.next();
                    if (!clientId.equalsIgnoreCase(sender)) {
                        clientState = clientHashMap.get(clientId);
                        writer = new DataOutputStream(clientState.getSocket().getOutputStream());
                        writer.write((message + "\n").getBytes(StandardCharsets.UTF_8));
                        writer.flush();
                        writer = null;
                    }
                } catch (IOException e) {
                    System.out.println("Communication Error: " + e.getMessage());
                }
            }
        }
    }

}
