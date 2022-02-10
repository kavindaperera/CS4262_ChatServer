package com.codewizards.room;

import java.util.concurrent.ConcurrentHashMap;

public class RoomManager {

    Room mainHall;

    private final ConcurrentHashMap<String, Room> roomHashMap = new ConcurrentHashMap<>();

    

}
