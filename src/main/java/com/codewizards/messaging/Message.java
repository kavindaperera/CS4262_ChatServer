package com.codewizards.messaging;

import org.json.simple.JSONObject;

import java.util.List;

public class Message {

    public static JSONObject getAllowNewIdentityResponse(String approve){
        JSONObject allowIdentity = new JSONObject();
        allowIdentity.put("type", "newidentity");
        allowIdentity.put("approved", approve);
        return allowIdentity;
    }

    public static JSONObject getRoomChangeBroadcast(String identity, String formerRoomId, String roomId){
        JSONObject roomChange  = new JSONObject();
        roomChange.put("type", "roomchange");
        roomChange.put("identity", identity);
        roomChange.put("former", formerRoomId);
        roomChange.put("roomid", roomId);
        return roomChange;
    }

    public static JSONObject getListResponse(List<String> roomsList){
        JSONObject getList  = new JSONObject();
        getList.put("type", "roomlist");
        getList.put("rooms", roomsList);
        return getList;
    }



}
