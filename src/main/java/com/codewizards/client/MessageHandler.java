package com.codewizards.client;

import com.codewizards.Constants;
import com.codewizards.election.FastBully;
import com.codewizards.message.ClientMessage;
import com.codewizards.message.ServerMessage;
import com.codewizards.room.Room;
import com.codewizards.room.RoomManager;
import com.codewizards.server.Server;
import com.codewizards.server.ServerState;
import com.codewizards.utils.Utils;
import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableCompletableObserver;
import lombok.NonNull;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


public class MessageHandler {

    public static Logger logger = Logger.getLogger(MessageHandler.class.getName());

    @Setter
    private boolean isWaitingForIdApproval = false;

    @Setter
    private boolean idApprovalReceived = false;

    public MessageHandler() {

    }

    public JSONObject respondToIdentityRequest(String identity) {
        logger.info("Client requested for identity => " + identity);

        if (Utils.validateIdentifier(identity)) {
            if (ClientManager.checkClientIdentityAvailability(identity)) {
                while (ServerState.getInstance().getCoordinator() == null) {}
                idApprovalReceived = false;
                while (!idApprovalReceived) {
                    if (!ServerState.getInstance().getOwnServer().equals(ServerState.getInstance().getCoordinator())) {
                        long lastUpdatedTs = ServerState.getInstance().getCoordinatorChangedTs();
                        sendRequestClientIdApprovalMessage(ServerState.getInstance().getCoordinator(), identity);
                        startIdApprovalTimeout();

                        if (isWaitingForIdApproval) {
                            FastBully.getInstance().startElection();
                            while (lastUpdatedTs == ServerState.getInstance().getCoordinatorChangedTs()) {
                                //stay in a while loop checking whether last updated time of coordinator field is changed
                            }
                        }

                    } else {
                        idApprovalReceived = true;
                    }
                }
            }
        } else {
            idApprovalReceived = false;
        }

        JSONObject response = ClientMessage.getAllowNewIdentityResponse(String.valueOf(idApprovalReceived));

        return response;
    }

    public JSONObject respondToListRequest() {
        List<String> globalRoomsList = RoomManager.getGlobalRoomsListAsArrayList();
        JSONObject response = ClientMessage.getListResponse(globalRoomsList);

        return response;
    }

    public JSONObject respondToWhoRequest(String currentRoomId) {
        Room currentRoom = RoomManager.getLocalRoomsList().get(currentRoomId);
        JSONObject response = ClientMessage.getRoomContents(currentRoomId, currentRoom.getCreatorId(), currentRoom.getClientsAsList());

        return response;
    }

    public JSONObject respondToCreateRoomRequest(String roomId, ClientState clientState) {
        logger.info("Client requested to create new Room => " + roomId);

        if (Utils.validateIdentifier(roomId)) {
            if (clientState.getOwnRoomId().equalsIgnoreCase("") && RoomManager.checkRoomIdAvailability(roomId)) {
                idApprovalReceived = false;
                while (!idApprovalReceived) {
                    if (!ServerState.getInstance().getOwnServer().equals(ServerState.getInstance().getCoordinator())) {
                        long lastUpdatedTs = ServerState.getInstance().getCoordinatorChangedTs();
                        sendRequestRoomIdApprovalMessage(ServerState.getInstance().getCoordinator(), roomId, clientState.getClientId());
                        startIdApprovalTimeout();

                        if (isWaitingForIdApproval) {
                            FastBully.getInstance().startElection();
                            while (lastUpdatedTs == ServerState.getInstance().getCoordinatorChangedTs()) {
                                //stay in a while loop checking whether last updated time of coordinator field is changed
                            }
                        }

                    } else {
                        idApprovalReceived = true;
                    }
                }

            } else {
                idApprovalReceived = false;
            }
        } else {
            idApprovalReceived = false;
        }

        JSONObject response = ClientMessage.getCreateRoomResponse(roomId, String.valueOf(idApprovalReceived));

        return response;
    }

    public JSONObject respondToJoinRoomRequest(String roomId, ClientState clientState) {
        logger.info("Client requested to join Room => " + roomId);
        JSONObject response;
        if (clientState.getOwnRoomId().equalsIgnoreCase("") && !RoomManager.checkRoomIdAvailability(roomId)) {
            if (!RoomManager.checkLocalRoomIdAvailability(roomId)) {
                response = ClientMessage.getRoomChangeBroadcast(clientState.getClientId(), clientState.getRoomId(), roomId);
            } else {
                Server server = ServerState.getInstance().getServerByServerId(RoomManager.getServerOfRoom(roomId));
                response = ClientMessage.getRouteResponse(roomId, server.getServerAddress(), String.valueOf(server.getClientPort()));
            }
        } else {
            response = ClientMessage.getRoomChangeBroadcast(clientState.getClientId(), roomId, roomId);
        }

        return response;
    }

    public JSONObject respondToMoveJoinRequest(String former, String roomId, String identity){
        JSONObject response;
        if (!RoomManager.checkLocalRoomIdAvailability(roomId)) {
            response = ClientMessage.getRoomChangeBroadcast(identity, former, roomId);
        } else {
            response = ClientMessage.getRoomChangeBroadcast(identity, former, RoomManager.MAINHALL_ID);
        }

        return response;
    }

    public JSONObject respondToDeleteRoomRequest(String roomId, ClientState clientState) {
        logger.info("Client requested to delete Room => " + roomId);
        JSONObject response;
        if (clientState.getOwnRoomId().equalsIgnoreCase(roomId)) {
            response = ClientMessage.getDeleteRoomResponse(roomId, "true");
        } else {
            response = ClientMessage.getDeleteRoomResponse(roomId, "false");
        }

        return response;
    }

    public void respondToQuitRequest() {

    }

    public JSONObject respondToReceivedMessage(JSONObject receivedMessage, String clientId) {
        String content = (String) receivedMessage.get("content");
        logger.info(clientId + " sent message => " + content);

        JSONObject broadcastMessage = ClientMessage.getMessageBroadcast(clientId, content);

        return broadcastMessage;
    }

    private void sendRequestClientIdApprovalMessage(Server server, String identity) {
        logger.info("Send requestClientIdApproval to: " + server.getServerId());
        try {
            Socket socket = new Socket(server.getServerAddress(), server.getCoordinationPort());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.write((ServerMessage.getRequestClientIdApprovalMessage(ServerState.getInstance().getOwnServer().getServerId(), identity) + "\n").getBytes(StandardCharsets.UTF_8));
            dataOutputStream.flush();

        } catch (IOException e) {
            logger.error(e.getLocalizedMessage() + ": " + server.getServerId());
        }
    }

    private void sendRequestRoomIdApprovalMessage(Server server, String identity, String clientId) {
        logger.info("Send requestRoomIdApproval to: " + server.getServerId());
        try {
            Socket socket = new Socket(server.getServerAddress(), server.getCoordinationPort());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.write((ServerMessage.getRequestRoomIdApprovalMessage(ServerState.getInstance().getOwnServer().getServerId(), identity, clientId) + "\n").getBytes(StandardCharsets.UTF_8));
            dataOutputStream.flush();

        } catch (IOException e) {
            logger.error(e.getLocalizedMessage() + ": " + server.getServerId());
        }
    }

    /*
    private void startIdApprovalTimeout() {
        idApprovalTimeoutDisposable = (Completable.timer(Constants.REQUEST_APPROVAL_TIMEOUT, TimeUnit.MILLISECONDS)
                .subscribeWith(new DisposableCompletableObserver() {
                                   @Override
                                   public void onStart() {
                                       logger.info("request identity approval timeout started!");
                                       isWaitingForIdApproval.set(true);
                                   }

                                   @Override
                                   public void onError(@NonNull Throwable error) {
                                       error.printStackTrace();
                                   }

                                   @Override
                                   public void onComplete() {
                                       logger.info("request identity approval timeout completed!");
                                       if (!idApprovalReceived.get()) {
                                           logger.info("Started a new Election");
                                           //start new election
                                       }
                                       stopIdApprovalTimeout();
                                   }
                               }
                )
        );
    }

    public void stopIdApprovalTimeout() {
        if (isWaitingForIdApproval.get() && !idApprovalTimeoutDisposable.isDisposed()) {
            logger.info("request identity approval timeout stopped!");
            isWaitingForIdApproval.set(false);
            idApprovalTimeoutDisposable.dispose();
        }
    }

    public void setIdApprovalReceived(@NonNull Boolean approval) {
        idApprovalReceived.set(approval);
    }

    */

    private void startIdApprovalTimeout() {
        isWaitingForIdApproval = true;
        long start = System.currentTimeMillis();
        long elapsedTime = 0L;

        while (isWaitingForIdApproval && (elapsedTime < Constants.REQUEST_APPROVAL_TIMEOUT)) {
            elapsedTime = System.currentTimeMillis() - start;
        }
    }

}
