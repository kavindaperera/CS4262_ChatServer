package com.codewizards;

import lombok.NonNull;

public class Constants {

    // timeouts in milliseconds
    public static final int SOCKET_TIMEOUT = 60000;
    public static final long VIEW_MESSAGE_TIMEOUT = 6000L;              // T2
    public static final long ANSWER_MESSAGE_TIMEOUT = 6000L;            // T2
    public static final long COORDINATOR_MESSAGE_TIMEOUT = 7000L;       // T3
    public static final long HEARTBEAT_SND_INTERVAL = 10000L;
    public static final long REQUEST_APPROVAL_TIMEOUT = 8000L;
    public static final long HEARTBEAT_WAIT_INTERVAL = HEARTBEAT_SND_INTERVAL * 2;

    public static long getT4(@NonNull Integer priorityNumber) {
        return 20000L / (1L * priorityNumber);
    }

}
