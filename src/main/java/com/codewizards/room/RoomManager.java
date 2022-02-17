package com.codewizards.room;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class RoomManager {

    public static String MAINHALL_ID;
    private static ArrayList<String> globalRoomsList = new ArrayList<>();
    private static ConcurrentHashMap<String, Room> localRoomsList = new ConcurrentHashMap<>();

    public RoomManager() {

    }

    /**
     * @param roomId
     * @param clientId
     */
    public static void createChatRoom(@NonNull String roomId, @NonNull String clientId) {
        if (clientId.equalsIgnoreCase("")){
            MAINHALL_ID = roomId;
        }
        Room chatRoom = new Room(roomId, clientId);
        localRoomsList.put(roomId, chatRoom);
        globalRoomsList.add(roomId);
    }

    /**
     * @param roomId Room Identifier
     * @param message
     */
    public static void broadcastToChatRoom(@NonNull String roomId, @NonNull String sender, @NonNull String message) {
        Room room = localRoomsList.get(roomId);
        room.sendBroadcast(sender, message);
    }

    public static ConcurrentHashMap<String, Room> getLocalRoomsList() {
        return localRoomsList;
    }
}
