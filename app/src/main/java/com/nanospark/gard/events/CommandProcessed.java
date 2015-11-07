package com.nanospark.gard.events;

import com.nanospark.gard.model.door.Door;

/**
 * Door command received and processed
 * Created by Leandro on 9/8/2015.
 */
public class CommandProcessed {

    public final Door door;
    public final Door.State state;
    public final Door.Command command;

    public CommandProcessed(Door door, Door.State state, Door.Command command) {
        this.door = door;
        this.state = state;
        this.command = command;
    }

}
