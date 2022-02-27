package com.codewizards.client;

import com.codewizards.Constants;
import com.codewizards.message.ClientMessage;
import com.codewizards.message.ServerMessage;
import com.codewizards.room.RoomManager;
import com.codewizards.server.Server;
import com.codewizards.server.ServerState;
import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableCompletableObserver;
import lombok.NonNull;
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

    private Disposable idApprovalTimeoutDisposable;

    private AtomicBoolean isWaitingForIdApproval = new AtomicBoolean(false);

    private AtomicBoolean idApprovalReceived = new AtomicBoolean(false);

    //createRoom

    //deleteRoom

    public MessageHandler() {

    }

    public JSONObject respondToIdentityRequest(JSONObject receivedMessage) {
        String identity = (String) receivedMessage.get("identity");
        logger.info("Client requested for identity " + identity);

        /*
        sendRequestApprovalMessage(ServerState.getInstance().getCoordinator(), identity);
        startIdApprovalTimeout();
        JSONObject response = ClientMessage.getAllowNewIdentityResponse((idApprovalReceived.toString());
        */

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



    private void sendRequestApprovalMessage(Server server, String identity) {
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

}
