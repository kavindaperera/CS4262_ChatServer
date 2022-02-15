package com.codewizards.client;

import com.codewizards.message.ClientMessage;
import org.json.simple.JSONObject;

public class MessageHandler {

    //createRoom

    //deleteRoom

    public MessageHandler() {

    }

    public JSONObject respondToIdentityRequest(JSONObject receivedMessage) {
        String identity = (String) receivedMessage.get("identity");
        System.out.println("Client requested for identity " + identity);

        // check with leader for the availability and send response

        JSONObject response = ClientMessage.getAllowNewIdentityResponse("true");

        return response;
    }

    public void respondToListRequest() {

    }

    public void respondToWhoRequest() {

    }

    public void respondToCreateRoomRequest() {

    }

    public void respondToJoinRoomRequest() {

    }

    public void respondToDeleteRoomRequest() {

    }

}
