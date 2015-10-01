package com.nanospark.gard.events;

import com.nanospark.gard.model.door.Door;

/**
 * Created by Leandro on 9/8/2015.
 */
public class DoorToggled {

    public final Door door;
    public final boolean opened;

    public DoorToggled(Door door, boolean opened) {
        this.door = door;
        this.opened = opened;
    }

}
