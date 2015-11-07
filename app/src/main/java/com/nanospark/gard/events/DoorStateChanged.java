package com.nanospark.gard.events;

import com.nanospark.gard.model.door.Door;

public class DoorStateChanged {

    public final Door door;
    public final Door.State state;

    public DoorStateChanged(Door door, Door.State state) {
        this.door = door;
        this.state = state;
    }

}
