package com.nanospark.gard.events;

import com.squareup.otto.Produce;

import ioio.lib.spi.Log;
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
        if (!isOpened()) {
            this.opened = true;
            Tattu.post(new DoorToggled(opened));
        } else {
            Log.w("DOOR", "Door already open");
        }
    }

    public void close() {
        if (isOpened()) {
            this.opened = false;
            Tattu.post(new DoorToggled(opened));
        } else {
            Log.w("DOOR", "Door already closed");
        }
    }

    public void set(boolean open) {
        if (open) {
            open();
        } else {
            close();
        }
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
