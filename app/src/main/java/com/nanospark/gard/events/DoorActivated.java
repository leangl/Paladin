package com.nanospark.gard.events;

import com.nanospark.gard.model.door.Door;

/**
 * Door command sent but not yet confirmed
 * Created by Leandro on 9/8/2015.
 */
public class DoorActivated {

    public final Door door;
    public final boolean opened;
    public final String message;

    public DoorActivated(Door door, boolean opened, String message) {
        this.door = door;
        this.opened = opened;
        this.message = message;
    }

}
