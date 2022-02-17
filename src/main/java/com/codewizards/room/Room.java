package com.codewizards.room;

import com.codewizards.client.ClientState;
import lombok.NonNull;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

public class Room {

    private String roomId;

    private String creatorId;

    private final HashMap<String, ClientState> clientHashMap = new HashMap<>();


    /**
     * used to create other ChatRooms
     * @param roomId
     * @param creatorId
     */
    public Room(@NonNull String roomId, @NonNull String creatorId) {
        this.roomId = roomId;
        this.creatorId = creatorId;
    }

    /**
     * used to create MainHall
     * @param roomId
     */
    public Room(@NonNull String roomId) {
        this.roomId = roomId;
    }

    /**
     * @param message
     */
    public void sendBroadcast(String message) {
        Iterator<ClientState> clientList = clientHashMap.values().iterator();
        DataOutputStream writer;
        while (clientList.hasNext()) {
            try {
                ClientState clientState = clientList.next();
                writer = new DataOutputStream(clientState.getSocket().getOutputStream());
                writer.write((message + "\n").getBytes("UTF-8"));
                writer.flush();
                writer = null;
            } catch (IOException e) {
                System.out.println("Communication Error: " + e.getMessage());
            }
        }
    }

}
