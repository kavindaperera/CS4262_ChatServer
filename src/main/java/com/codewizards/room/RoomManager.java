package com.codewizards.room;

import com.codewizards.Main;
import com.codewizards.server.ServerState;
import lombok.Getter;
import lombok.NonNull;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class RoomManager {

    public static Logger logger = Logger.getLogger(RoomManager.class.getName());

    public static String MAINHALL_ID;

    @Getter
    private static ConcurrentHashMap<String, String> globalRoomsList = new ConcurrentHashMap<>(); // <roomID, serverID>

    private static ConcurrentHashMap<String, Room> localRoomsList = new ConcurrentHashMap<>();

    public RoomManager() {

    }

    /**
     * @param roomId unique room identity
     * @param clientId client identity
     */
    public static void createChatRoom(@NonNull String roomId, @NonNull String clientId) {
        if (clientId.equalsIgnoreCase("")){
            MAINHALL_ID = roomId;
        }
        Room chatRoom = new Room(roomId, clientId);

        localRoomsList.put(roomId, chatRoom);
        globalRoomsList.put(roomId, ServerState.getInstance().getOwnServer().getServerId());
    }

    /**
     * @param roomId unique room identity
     * @param sender sender identity
     * @param message message
     */
    public static void broadcastToChatRoom(@NonNull String roomId, @NonNull String sender, @NonNull String message) {
        Room room = localRoomsList.get(roomId);
        room.sendBroadcast(sender, message);
    }

    public static ConcurrentHashMap<String, Room> getLocalRoomsList() {
        return localRoomsList;
    }

    public static List<String> getGlobalRoomsListAsArrayList(){
        return new ArrayList<>(globalRoomsList.keySet());
    }

    public static boolean checkRoomIdAvailability(String requestedRoomID) {
        return !globalRoomsList.containsKey(requestedRoomID);
    }

    public static boolean checkLocalRoomIdAvailability(String requestedRoomID) {
        return !localRoomsList.containsKey(requestedRoomID);
    }

   public static String getServerOfRoom(String roomId) {
        return globalRoomsList.get(roomId);
    }

    public static void addToGlobalRoomsList(String roomID, String serverID) {
        globalRoomsList.put(roomID, serverID);
    }

    public static void removeFromGlobalRoomsList(String roomId) {
        globalRoomsList.remove(roomId);
    }

    public static void initializeGlobalRoomsList() {
        Iterator<String> serverList = ServerState.getInstance().getServerList().keySet().iterator();
        while (serverList.hasNext()) {
            String serverId = serverList.next();
            if (!serverId.equalsIgnoreCase(ServerState.getInstance().getOwnServer().getServerId())) {
                String mainHallId = "MainHall-" + serverId;
                globalRoomsList.put(mainHallId, serverId);
                logger.info("Initialized: " + mainHallId);
            }
        }
    }

    public static void removeRoomsOnFailure(String serverID) {
        synchronized (globalRoomsList) {
            globalRoomsList.entrySet().removeIf(
                    entry -> entry.getValue()
                            .compareTo(serverID) == 0);
        }
    }
}
