package com.nanospark.gard;

import com.google.inject.Singleton;
import com.nanospark.gard.events.DoorActivation;
import com.nanospark.gard.events.DoorToggled;
import com.squareup.otto.Produce;

import ioio.lib.spi.Log;
import mobi.tattu.utils.Tattu;
import mobi.tattu.utils.ToastManager;
import roboguice.RoboGuice;

/**
 * Created by Leandro on 9/8/2015.
 */
@Singleton
public class Door {

    private Boolean opened = true;

    public Door() {
        Tattu.register(this);
    }

    public static final Door getInstance() {
        return RoboGuice.getInjector(GarD.instance).getInstance(Door.class);
    }

    public boolean open(String message) {
        if (!isOpened()) {
            Tattu.post(new DoorActivation(true, message));
            return true;
        } else {
            Log.w("DOOR", "Door already open");
            ToastManager.get().showToast("The door is already open.", 1);
            return false;
        }
    }

    public boolean close(String message) {
        if (isOpened()) {
            Tattu.post(new DoorActivation(false, message));
            return true;
        } else {
            Log.w("DOOR", "Door already closed");
            ToastManager.get().showToast("The door is already closed.", 1);
            return false;
        }
    }

    public void confirm(boolean opened) {
        this.opened = opened;
        Tattu.post(new DoorToggled(opened));
    }

    public void toggle(String message) {
        if (this.opened) {
            close(message);
        } else {
            open(message);
        }
    }

    public boolean isOpened() {
        return this.opened;
    }

    public boolean isReady() {
        return this.opened != null;
    }

    @Produce
    public DoorToggled produce() {
        if (isReady()) {
            return new DoorToggled(this.opened);
        } else {
            return null;
        }
    }

}
