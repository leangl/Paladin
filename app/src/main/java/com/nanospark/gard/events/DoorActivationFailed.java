package com.nanospark.gard.events;

import com.nanospark.gard.model.door.Door;

/**
 * Created by Leandro on 5/10/2015.
 */
public class DoorActivationFailed {

    public final Door door;
    public final boolean opened;

    public DoorActivationFailed(Door door, boolean opened) {
        this.door = door;
        this.opened = opened;
    }

}
