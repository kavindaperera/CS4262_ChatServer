package com.codewizards.room;

import com.codewizards.client.ClientState;
import lombok.NonNull;

import java.util.HashMap;

public class Room {

    private String roomId;

    private String creatorId;

    private final HashMap<String, ClientState> clientHashMap = new HashMap<>();

    // used to create other ChatRooms
    public Room(@NonNull String roomId, @NonNull String creatorId) {
        this.roomId = roomId;
        this.creatorId = creatorId;
    }

    // used to create MainHall
    public Room(@NonNull String roomId) {
        this.roomId = roomId;
    }

    public void sendBroadcast(String message) {

    }

}
