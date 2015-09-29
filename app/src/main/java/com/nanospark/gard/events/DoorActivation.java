package com.nanospark.gard.events;

import com.nanospark.gard.door.BaseDoor;

/**
 * Created by Leandro on 9/8/2015.
 */
public class DoorActivation {

    public final BaseDoor door;
    public final boolean opened;
    public final String message;

    public DoorActivation(BaseDoor door, boolean opened, String message) {
        this.door = door;
        this.opened = opened;
        this.message = message;
    }

}
