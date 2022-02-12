package com.codewizards.message;

import org.json.simple.JSONObject;

import java.util.List;

public class ServerMessage {

    public static JSONObject getElectionMessage(String serverId) {
        JSONObject election = new JSONObject();
        election.put("type", "election");
        election.put("serverId", serverId);
        return election;
    }

    public static JSONObject getAnswerMessage(String serverId) {
        JSONObject answer = new JSONObject();
        answer.put("type", "answer");
        answer.put("serverId", serverId);
        return answer;
    }

    public static JSONObject getNominationMessage(String serverId) {
        JSONObject nomination = new JSONObject();
        nomination.put("type", "nomination");
        nomination.put("serverId", serverId);
        return nomination;
    }

    public static JSONObject getCoordinatorMessage(String serverId) {
        JSONObject coordinator = new JSONObject();
        coordinator.put("type", "coordinator");
        coordinator.put("serverId", serverId);
        return coordinator;
    }

    public static JSONObject getIamUpMessage(String serverId) {
        JSONObject IamUp = new JSONObject();
        IamUp.put("type", "IamUp");
        IamUp.put("serverId", serverId);
        return IamUp;
    }

    public static JSONObject getViewMessage(String serverId, List<String> processesList) {
        JSONObject view = new JSONObject();
        view.put("type", "view");
        view.put("serverId", serverId);
        view.put("processes", processesList);
        return view;
    }

}
