package com.codewizards.room;

import com.codewizards.client.ClientState;

import java.util.HashMap;

public class Room {

    private final String roomId;

    private final String creatorId;

    private final HashMap<String, ClientState> clientHashMap = new HashMap<>();

    public Room(String roomId, String creatorId) {
        this.roomId = roomId;
        this.creatorId = creatorId;
    }

}
