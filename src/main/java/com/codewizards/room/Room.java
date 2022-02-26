package com.codewizards.room;

import com.codewizards.client.ClientState;
import lombok.NonNull;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;

public class Room {

    private String roomId;

    private String creatorId;

    private final HashMap<String, ClientState> clientHashMap = new HashMap<>();


    /**
     * used to create ChatRooms
     * @param roomId
     * @param creatorId
     */
    public Room(@NonNull String roomId, @NonNull String creatorId) {
        this.roomId = roomId;
        this.creatorId = creatorId;
    }

    public HashMap<String, ClientState> getClientHashMap() {
        return clientHashMap;
    }

    /**
     * @param sender
     * @param message
     */
    public void sendBroadcast(String sender, String message) {
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
