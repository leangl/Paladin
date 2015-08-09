package com.nanospark.gard.events;

import com.squareup.otto.Produce;

import mobi.tattu.utils.Tattu;

/**
 * Created by Leandro on 9/8/2015.
 */
public class DoorState {

    private static DoorState instance;
    private boolean opened = false;

    private DoorState() {
        Tattu.register(this);
    }

    public static final DoorState getInstance() {
        if (instance == null) {
            instance = new DoorState();
        }
        return instance;
    }

    public void open() {
        this.opened = true;
        Tattu.post(new DoorToggled(opened));
    }

    public void close() {
        this.opened = false;
        Tattu.post(new DoorToggled(opened));
    }

    public void toggle() {
        if (this.opened) {
            close();
        } else {
            open();
        }
    }

    public boolean isOpened() {
        return this.opened;
    }

    @Produce
    public DoorToggled produce() {
        return new DoorToggled(this.opened);
    }

}
