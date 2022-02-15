package com.codewizards.client;

import com.codewizards.message.ChatMessage;
import org.json.simple.JSONObject;

public class MessageHandler {

    //createRoom

    //deleteRoom

    public MessageHandler() {

    }

    public String respondToIdentityRequest(JSONObject receivedMessage) {
        String identity = (String) receivedMessage.get("identity");
        System.out.println("Client requested for identity " + identity);

        // check with leader for the availability and send response

        JSONObject response = ChatMessage.getAllowNewIdentityResponse("true");

        return response.toJSONString();
    }

    public void respondToRoomChangeRequest() {

    }

    public void respondToListRequest() {

    }

    public void respondToWhoRequest() {

    }

    public void respondToCreateRoomRequest() {

    }

}
