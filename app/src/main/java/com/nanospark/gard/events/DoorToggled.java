package com.nanospark.gard.events;

import com.nanospark.gard.door.BaseDoor;

/**
 * Created by Leandro on 9/8/2015.
 */
public class DoorToggled {

    public final BaseDoor door;
    public final boolean opened;

    public DoorToggled(BaseDoor door, boolean opened) {
        this.door = door;
        this.opened = opened;
    }

}
