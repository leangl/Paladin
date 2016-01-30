package com.nanospark.gard.events;

import com.nanospark.gard.model.CommandSource;
import com.nanospark.gard.model.door.Door;

import mobi.tattu.utils.annotations.Nullable;

public class DoorStateChanged {

    public final Door door;
    public final Door.State state;
    @Nullable public final CommandSource source;

    public DoorStateChanged(Door door, Door.State state, @Nullable CommandSource source) {
        this.door = door;
        this.state = state;
        this.source = source;
    }

}
