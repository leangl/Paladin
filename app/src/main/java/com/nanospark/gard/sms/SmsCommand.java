package com.nanospark.gard.sms;

import android.support.annotation.Nullable;

import com.nanospark.gard.model.door.Door;
import com.nanospark.gard.model.user.User;

import java.util.Arrays;
import java.util.List;

import mobi.tattu.utils.StringUtils;

/**
 * Created by Leandro on 1/12/2015.
 */
class SmsCommand {

    public static final String OPEN = "open";
    public static final String CLOSE = "close";
    public static final String STATUS = "status";

    public final long timestamp;
    public final List<Door> doors;
    public final String from;
    public final String command;
    // this two can be updated after authorization process
    public User user;
    public String password;

    public SmsCommand(List<Door> doors, User user, String from, String command, String password) {
        this.timestamp = System.currentTimeMillis();
        this.doors = doors;
        this.user = user;
        this.from = from;
        this.command = command;
        this.password = password;
    }

    public static SmsCommand fromBody(User user, String from, String body) throws Exception {
        body = body.trim().toLowerCase();

        String command = getCommand(body);
        if (command == null) throw new Exception("Command not found");

        String password = null;
        if (user != null && user.isPasswordRequired()) {
            password = body.substring(body.lastIndexOf(" ") + 1); // password is last word
        }

        List<Door> doors = getDoors(body, command, password);

        return new SmsCommand(doors, user, from, command, password);
    }

    @Nullable
    private static List<Door> getDoors(String body, String command, String password) throws Exception {
        int endIdx = password != null ? body.lastIndexOf(password) : body.length();
        String doorName = body.substring(command.length(), endIdx).trim();
        if (StringUtils.isBlank(doorName)) { // No door specified
            // FUCKING special cases!!! WTF!!!
            // If command is status or only one door enabled then no need to provide door name
            if (STATUS.equals(command) || Door.getEnabledDoors().size() == 1) {
                return Door.getEnabledDoors();
            }
            throw new Exception("No door specified");
        }

        Door selectedDoor = null;
        for (Door door : Door.getEnabledDoors()) {
            if (door.getName().equalsIgnoreCase(doorName)) selectedDoor = door;
        }
        if (selectedDoor == null) throw new Exception("Door not found");

        return Arrays.asList(selectedDoor);
    }

    private static String getCommand(String body) {
        if (body.indexOf(OPEN) == 0) return OPEN;
        if (body.indexOf(CLOSE) == 0) return CLOSE;
        if (body.indexOf(STATUS) == 0) return STATUS;
        return null;
    }

    public static boolean isSmsCommand(String body) {
        return getCommand(body.toLowerCase()) != null;
    }

    public boolean is(String command) {
        return command.equalsIgnoreCase(this.command);
    }

}
