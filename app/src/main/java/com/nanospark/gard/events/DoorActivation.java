package com.nanospark.gard.events;

import com.nanospark.gard.door.Door;

/**
 * Created by Leandro on 9/8/2015.
 */
public class DoorActivation {

    public final Door door;
    public final boolean opened;
    public final String message;

    public DoorActivation(Door door, boolean opened, String message) {
        this.door = door;
        this.opened = opened;
        this.message = message;
    }

}
