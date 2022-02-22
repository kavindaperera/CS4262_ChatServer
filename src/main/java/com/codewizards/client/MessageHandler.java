package com.codewizards.client;

import com.codewizards.message.ClientMessage;
import com.codewizards.room.RoomManager;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.util.List;


public class MessageHandler {

    public static Logger logger = Logger.getLogger(MessageHandler.class.getName());

    //createRoom

    //deleteRoom

    public MessageHandler() {

    }

    public JSONObject respondToIdentityRequest(JSONObject receivedMessage) {
        String identity = (String) receivedMessage.get("identity");
        logger.info("Client requested for identity " + identity);

        // check with leader for the availability and send response

        JSONObject response = ClientMessage.getAllowNewIdentityResponse("true");

        return response;
    }

    public JSONObject respondToListRequest() {
        List<String> globalRoomsList = RoomManager.getGlobalRoomsListAsArrayList();
        JSONObject response = ClientMessage.getListResponse(globalRoomsList);

        return response;
    }

    public void respondToWhoRequest() {

    }

    public void respondToCreateRoomRequest() {

    }

    public void respondToJoinRoomRequest() {

    }

    public void respondToDeleteRoomRequest() {

    }

    public void respondToQuitRequest() {

    }

    public JSONObject respondToReceivedMessage(JSONObject receivedMessage, String clientId) {
        String content = (String) receivedMessage.get("content");
        logger.info(clientId + " sent message => " + content);

        JSONObject broadcastMessage = ClientMessage.getMessageBroadcast(clientId, content);

        return broadcastMessage;
    }

}
