package com.codewizards.message;

import lombok.NonNull;
import org.json.simple.JSONObject;

import java.util.List;


public class ClientMessage {

    @SuppressWarnings("unchecked")
    public static JSONObject getAllowNewIdentityResponse(@NonNull String approve) {
        JSONObject allowIdentity = new JSONObject();
        allowIdentity.put("type", "newidentity");
        allowIdentity.put("approved", approve);
        return allowIdentity;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getRoomChangeBroadcast(@NonNull String identity, @NonNull String formerRoomId, @NonNull String roomId) {
        JSONObject roomChange = new JSONObject();
        roomChange.put("type", "roomchange");
        roomChange.put("identity", identity);
        roomChange.put("former", formerRoomId);
        roomChange.put("roomid", roomId);
        return roomChange;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getListResponse(@NonNull List<String> roomsList) {
        JSONObject getList = new JSONObject();
        getList.put("type", "roomlist");
        getList.put("rooms", roomsList);
        return getList;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getCreateRoomResponse(@NonNull String roomId, @NonNull String approved) {
        JSONObject createRoom = new JSONObject();
        createRoom.put("type", "createroom");
        createRoom.put("roomid", roomId);
        createRoom.put("approved", approved);
        return createRoom;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getServerChange(@NonNull String approved, @NonNull String serverId) {
        JSONObject serverChange = new JSONObject();
        serverChange.put("type", "serverchange");
        serverChange.put("approved", approved);
        serverChange.put("serverid", serverId);
        return serverChange;
    }

    public static JSONObject getDeleteRoomResponse(@NonNull String roomId, @NonNull String approved) {
        JSONObject deleteRoom = new JSONObject();
        deleteRoom.put("type", "deleteroom");
        deleteRoom.put("roomid", roomId);
        deleteRoom.put("approved", approved);
        return deleteRoom;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getMessageBroadcast(@NonNull String identity, @NonNull String content) {
        JSONObject broadcastMessage = new JSONObject();
        broadcastMessage.put("type", "message");
        broadcastMessage.put("identity", identity);
        broadcastMessage.put("content", content);
        return broadcastMessage;
    }
}
