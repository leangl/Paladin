package com.nanospark.gard.events;

/**
 * Created by Leandro on 9/8/2015.
 */
public class DoorActivation {

    public final boolean opened;
    public final String message;

    public DoorActivation(boolean opened, String message) {
        this.opened = opened;
        this.message = message;
    }

}
