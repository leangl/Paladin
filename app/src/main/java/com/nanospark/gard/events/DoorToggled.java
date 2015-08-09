package com.nanospark.gard.events;

/**
 * Created by Leandro on 9/8/2015.
 */
public class DoorToggled {

    public final boolean opened;

    public DoorToggled(boolean opened) {
        this.opened = opened;
    }

}
