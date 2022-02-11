package com.codewizards.server;

import com.codewizards.Main;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class MessageHandler {

    //createRoom

    //deleteRoom
    public MessageHandler(){

    }

    public static JSONObject sendElectionMessage(){
        JSONObject election  = new JSONObject();
        election.put("type", "election");
        election.put("serverId", Main.SERVER_ID);
        return election;
    }

    public static JSONObject sendAnswerMessage(){
        JSONObject answer  = new JSONObject();
        answer.put("type", "answer");
        answer.put("serverId", Main.SERVER_ID);
        return answer;
    }

    public static JSONObject sendNominationMessage(){
        JSONObject nomination  = new JSONObject();
        nomination.put("type", "nomination");
        nomination.put("serverId", Main.SERVER_ID);
        return nomination;
    }

    public static JSONObject sendCoordinatorMessage(){
        JSONObject coordinator  = new JSONObject();
        coordinator.put("type", "coordinator");
        coordinator.put("serverId", Main.SERVER_ID);
        return coordinator;
    }

    public static JSONObject sendIamUpMessage(){
        JSONObject IamUp  = new JSONObject();
        IamUp.put("type", "IamUp");
        IamUp.put("serverId", Main.SERVER_ID);
        return IamUp;
    }

    public static JSONObject sendViewMessage(){
        JSONObject view  = new JSONObject();
        JSONArray processes = new JSONArray();
        // need to add list of processes
        view.put("type", "view");
        view.put("serverId", Main.SERVER_ID);
        view.put("processes", processes);
        return view;
    }

    public void respondToElectionMessage(){

    }

    public void respondToNominationMessage(){

    }

    public void respondToAnswerMessage(){

    }

    public void respondToCoordinatorMessage(){

    }

    public void respondToIamUpMessage(){

    }

    public void respondToViewMessage(){

    }

}
