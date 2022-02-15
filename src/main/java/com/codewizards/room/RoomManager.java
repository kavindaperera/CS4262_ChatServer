package com.codewizards.room;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class RoomManager {

    private static ArrayList<String> globalRoomsList = new ArrayList<>();
    private static ConcurrentHashMap<String, Room> localRoomsList = new ConcurrentHashMap<>();

    public RoomManager() {

    }

    public static void createMainHall(String roomId) {
        Room mainHall = new Room(roomId);
        localRoomsList.put(roomId, mainHall);
        globalRoomsList.add(roomId);
    }

    public void createChatRoom(String roomId, String clientId) {
        Room chatRoom = new Room(roomId, clientId);
        localRoomsList.put(roomId, chatRoom);
        globalRoomsList.add(roomId);
    }

    public static void broadcastToChatRoom(String roomId, String message) {
        Room room = localRoomsList.get(roomId);
        room.sendBroadcast(message);
    }

}
