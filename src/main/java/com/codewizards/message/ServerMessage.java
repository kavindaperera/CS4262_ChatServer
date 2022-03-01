package com.codewizards.message;

import com.codewizards.server.Server;
import lombok.NonNull;
import org.json.simple.JSONObject;

import java.util.List;

public class ServerMessage {

    /**
     * the request message to start election procedure
     * @param serverId server identifier
     * @return election message
     */
    @SuppressWarnings("unchecked")
    public static JSONObject getElectionMessage(@NonNull String serverId) {
        JSONObject election = new JSONObject();
        election.put("type", "election");
        election.put("serverId", serverId);
        return election;
    }

    /**
     * the response message to the election message
     * @param serverId server identifier
     * @return answer message
     */
    @SuppressWarnings("unchecked")
    public static JSONObject getAnswerMessage(@NonNull String serverId) {
        JSONObject answer = new JSONObject();
        answer.put("type", "answer");
        answer.put("serverId", serverId);
        return answer;
    }

    /**
     * the message sent to the highest numbered process to notify that it is
     * a candidate for the coordinator
     * @param serverId server identifier
     * @return nomination message
     */
    @SuppressWarnings("unchecked")
    public static JSONObject getNominationMessage(@NonNull String serverId) {
        JSONObject nomination = new JSONObject();
        nomination.put("type", "nomination");
        nomination.put("serverId", serverId);
        return nomination;
    }

    /**
     * the message that claims that the sender is the coordinator
     * @param serverId server identifier
     * @return coordinator message
     */
    @SuppressWarnings("unchecked")
    public static JSONObject getCoordinatorMessage(@NonNull String serverId) {
        JSONObject coordinator = new JSONObject();
        coordinator.put("type", "coordinator");
        coordinator.put("serverId", serverId);
        return coordinator;
    }

    /**
     * the message that is sent by the recovered process
     * @param serverId server identifier
     * @return IamUp message
     */
    @SuppressWarnings("unchecked")
    public static JSONObject getIamUpMessage(@NonNull String serverId) {
        JSONObject IamUp = new JSONObject();
        IamUp.put("type", "IamUp");
        IamUp.put("serverId", serverId);
        return IamUp;
    }

    /**
     * the response message to the IamUp message containing a list of the processes
     * in the group
     * @param serverId server identifier
     * @param processesList a list of the servers
     * @return view message
     */
    @SuppressWarnings("unchecked")
    public static JSONObject getViewMessage(@NonNull String serverId, @NonNull List<String> processesList) {
        JSONObject view = new JSONObject();
        view.put("type", "view");
        view.put("serverId", serverId);
        view.put("processes", processesList);
        return view;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getApproveClientIDMessage(@NonNull String serverId, @NonNull String approved, @NonNull String requestedId) {
        JSONObject approveClientID = new JSONObject();
        approveClientID.put("type", "approveClientId");
        approveClientID.put("identity", requestedId);
        approveClientID.put("approved", approved);
        approveClientID.put("serverId", serverId);
        return approveClientID;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getRequestClientIdApprovalMessage(@NonNull String serverId, @NonNull String requestedId) {
        JSONObject requestClientIdApproval = new JSONObject();
        requestClientIdApproval.put("type", "requestClientIdApproval");
        requestClientIdApproval.put("serverId", serverId);
        requestClientIdApproval.put("identity", requestedId);
        return requestClientIdApproval;
    }

    public static JSONObject getApproveRoomIDMessage(@NonNull String serverId, @NonNull String approved, @NonNull String requestedId, @NonNull String clientId) {
        JSONObject approveRoomID = new JSONObject();
        approveRoomID.put("type", "approveRoomId");
        approveRoomID.put("identity", requestedId);
        approveRoomID.put("approved", approved);
        approveRoomID.put("serverId", serverId);
        approveRoomID.put("clientId", clientId);
        return approveRoomID;
    }

    public static JSONObject getRequestRoomIdApprovalMessage(@NonNull String serverId, @NonNull String requestedId, @NonNull String clientId) {
        JSONObject requestRoomIdApproval = new JSONObject();
        requestRoomIdApproval.put("type", "requestRoomIdApproval");
        requestRoomIdApproval.put("serverId", serverId);
        requestRoomIdApproval.put("identity", requestedId);
        requestRoomIdApproval.put("clientId", clientId);
        return requestRoomIdApproval;
    }

    public static JSONObject getInformClientIdCreationMessage(@NonNull String serverId, @NonNull String requestedId) {
        JSONObject informClientIdCreation = new JSONObject();
        informClientIdCreation.put("type", "informClientIdCreation");
        informClientIdCreation.put("serverId", serverId);
        informClientIdCreation.put("identity", requestedId);
        return informClientIdCreation;
    }

    public static JSONObject getInformRoomIdCreationMessage(@NonNull String serverId, @NonNull String requestedId) {
        JSONObject informRoomIdCreation = new JSONObject();
        informRoomIdCreation.put("type", "informRoomIdCreation");
        informRoomIdCreation.put("serverId", serverId);
        informRoomIdCreation.put("identity", requestedId);
        return informRoomIdCreation;
    }

    /**
     * @param serverId server identifier
     * @return heartbeat message
     */
    @SuppressWarnings("unchecked")
    public static JSONObject getHeartbeatMessage(@NonNull String serverId){
        JSONObject heartbeat = new JSONObject();
        heartbeat.put("type", "heartbeat");
        heartbeat.put("serverId", serverId);
        return heartbeat;
    }

}
