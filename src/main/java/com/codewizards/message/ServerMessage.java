package com.codewizards.message;

import lombok.NonNull;
import org.json.simple.JSONObject;

import java.util.List;

public class ServerMessage {

    @SuppressWarnings("unchecked")
    public static JSONObject getElectionMessage(@NonNull String serverId) {
        JSONObject election = new JSONObject();
        election.put("type", "election");
        election.put("serverId", serverId);
        return election;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getAnswerMessage(@NonNull String serverId) {
        JSONObject answer = new JSONObject();
        answer.put("type", "answer");
        answer.put("serverId", serverId);
        return answer;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getNominationMessage(@NonNull String serverId) {
        JSONObject nomination = new JSONObject();
        nomination.put("type", "nomination");
        nomination.put("serverId", serverId);
        return nomination;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getCoordinatorMessage(@NonNull String serverId) {
        JSONObject coordinator = new JSONObject();
        coordinator.put("type", "coordinator");
        coordinator.put("serverId", serverId);
        return coordinator;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getIamUpMessage(@NonNull String serverId) {
        JSONObject IamUp = new JSONObject();
        IamUp.put("type", "IamUp");
        IamUp.put("serverId", serverId);
        return IamUp;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getViewMessage(@NonNull String serverId, @NonNull List<String> processesList) {
        JSONObject view = new JSONObject();
        view.put("type", "view");
        view.put("serverId", serverId);
        view.put("processes", processesList);
        return view;
    }

}
