package com.nanospark.gard.events;

import com.nanospark.gard.model.door.Door;

/**
 * Created by Leandro on 5/10/2015.
 */
public class CommandFailed {

    public final Door door;
    public final Door.Command command;

    public CommandFailed(Door door, Door.Command command) {
        this.door = door;
        this.command = command;
    }

}
