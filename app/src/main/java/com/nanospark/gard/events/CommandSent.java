package com.nanospark.gard.events;

import com.nanospark.gard.model.door.Door;

/**
 * Door command sent but not yet confirmed
 * Created by Leandro on 9/8/2015.
 */
public class CommandSent {

    public final Door door;
    public final Door.Command command;
    public final String message;

    public CommandSent(Door door, Door.Command command, String message) {
        this.door = door;
        this.command = command;
        this.message = message;
    }

}
