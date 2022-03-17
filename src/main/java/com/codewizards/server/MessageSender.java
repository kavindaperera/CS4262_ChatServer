package com.codewizards.server;

import com.codewizards.client.ClientManager;
import com.codewizards.message.ServerMessage;
import com.codewizards.room.RoomManager;
import lombok.NonNull;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MessageSender {

    public static void sendViewMessage(@NonNull Server server) throws IOException {
        Socket socket = new Socket(server.getServerAddress(), server.getCoordinationPort());
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataOutputStream.write((
                ServerMessage.getViewMessage(ServerState.getInstance().getOwnServer().getServerId(),
                ServerState.getInstance().getServerViewAsArrayList(),
                RoomManager.getGlobalRoomsList(),
                ClientManager.getGlobalClientsList()) + "\n").getBytes(StandardCharsets.UTF_8));
        dataOutputStream.flush();
    }

    public static void sendCoordinatorMessage(@NonNull Server server) throws IOException {
            Socket socket = new Socket(server.getServerAddress(), server.getCoordinationPort());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.write((ServerMessage.getCoordinatorMessage(ServerState.getInstance().getOwnServer().getServerId()) + "\n").getBytes(StandardCharsets.UTF_8));
            dataOutputStream.flush();
    }

    public static void sendIamUpMessage(@NonNull Server server) throws IOException {
            Socket socket = new Socket(server.getServerAddress(), server.getCoordinationPort());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.write((ServerMessage.getIamUpMessage(ServerState.getInstance().getOwnServer().getServerId()) + "\n").getBytes(StandardCharsets.UTF_8));
            dataOutputStream.flush();
    }

    public static void sendHeartbeatMessage(@NonNull Server server, @NonNull Runnable failure){
        try {
            Socket socket = new Socket(server.getServerAddress(), server.getCoordinationPort());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.write((ServerMessage.getHeartbeatMessage(ServerState.getInstance().getOwnServer().getServerId()) + "\n").getBytes(StandardCharsets.UTF_8));
            dataOutputStream.flush();
        } catch (IOException e) {
            failure.run();
        }
    }

    public static void sendElectionMessage(@NonNull Server server) throws IOException {
        Socket socket = new Socket(server.getServerAddress(), server.getCoordinationPort());
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataOutputStream.write((ServerMessage.getElectionMessage(ServerState.getInstance().getOwnServer().getServerId()) + "\n").getBytes(StandardCharsets.UTF_8));
        dataOutputStream.flush();
    }

    public static void sendAnswerMessage(@NonNull Server server) throws IOException {
        Socket socket = new Socket(server.getServerAddress(), server.getCoordinationPort());
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataOutputStream.write((ServerMessage.getAnswerMessage(ServerState.getInstance().getOwnServer().getServerId()) + "\n").getBytes(StandardCharsets.UTF_8));
        dataOutputStream.flush();
    }

    public static void sendNominationMessage(@NonNull Server server) throws IOException {
        Socket socket = new Socket(server.getServerAddress(), server.getCoordinationPort());
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataOutputStream.write((ServerMessage.getNominationMessage(ServerState.getInstance().getOwnServer().getServerId()) + "\n").getBytes(StandardCharsets.UTF_8));
        dataOutputStream.flush();
    }

    public static void sendInformServerFailureMessage(@NonNull Server server, @NonNull String failedServerId) throws IOException {
        Socket socket = new Socket(server.getServerAddress(), server.getCoordinationPort());
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataOutputStream.write((ServerMessage.getInformServerFailureMessage(failedServerId) + "\n").getBytes(StandardCharsets.UTF_8));
        dataOutputStream.flush();
    }
}
