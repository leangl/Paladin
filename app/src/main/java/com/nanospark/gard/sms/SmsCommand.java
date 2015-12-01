package com.nanospark.gard.sms;

import com.nanospark.gard.model.door.Door;
import com.nanospark.gard.model.user.User;

/**
 * Created by Leandro on 1/12/2015.
 */
class SmsCommand {

    public static final String OPEN = "open";
    public static final String CLOSE = "close";
    public static final String STATUS = "status";

    public final long timestamp;
    public final Door door;
    public final String from;
    public final String command;
    // this two can be updated after authorization process
    public User user;
    public String password;

    public SmsCommand(Door door, User user, String from, String command, String password) {
        this.timestamp = System.currentTimeMillis();
        this.door = door;
        this.user = user;
        this.from = from;
        this.command = command;
        this.password = password;
    }

    public static SmsCommand fromBody(User user, String from, String body) throws Exception {
        String[] bodyParts = body.split(" ");
        int doorNumber = Integer.parseInt(bodyParts[0]);
        String command = bodyParts[1];

        String password = null;
        if (user != null && user.isPasswordRequired() && bodyParts.length > 2) {
            password = bodyParts[2];
        }

        return new SmsCommand(Door.getInstance(doorNumber), user, from, command, password);
    }

    public static boolean isSmsCommand(String body) throws Exception {
        String[] bodyParts = body.split(" ");
        if (bodyParts.length < 2) {
            return false;
        }
        try {
            Integer.parseInt(bodyParts[0]);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean is(String command) {
        return command.equalsIgnoreCase(this.command);
    }

}
