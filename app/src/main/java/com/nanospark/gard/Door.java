package com.nanospark.gard;

import com.google.inject.Singleton;
import com.nanospark.gard.events.DoorActivation;
import com.nanospark.gard.events.DoorToggled;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.spi.Log;
import mobi.tattu.utils.Tattu;
import mobi.tattu.utils.ToastManager;
import roboguice.RoboGuice;

/**
 * Created by Leandro on 9/8/2015.
 */
public abstract class Door {

    private Boolean opened = true;
    private int id;
    private int inputPinNumber;
    private int outputPinNumber;
    private DigitalOutput outputPin;
    private DigitalInput inputPin;
    private boolean activatePin;
    private Boolean lastState;
    private boolean voiceEnabled;
    private String openPhrase;
    private String closePhrase;

    public Door(int id, Integer outputPinNumber, Integer inputPinNumber) {
        this.id = id;
        this.outputPinNumber = outputPinNumber;
        this.inputPinNumber = inputPinNumber;
        restore();
        Tattu.register(this);
    }

    public static final Door getInstance(Integer id) {
        switch (id) {
            case 1:
                return RoboGuice.getInjector(GarD.instance).getInstance(One.class);
            case 2:
                return RoboGuice.getInjector(GarD.instance).getInstance(Two.class);
        }
        throw new IllegalArgumentException("No door with id " + id);
    }

    public static final Door[] getDoors() {
        return new Door[]{getInstance(1), getInstance(2)};
    }

    public boolean open(String message) {
        if (!isOpened()) {
            Log.i("DOOR", "Opening door: " + id);
            Log.i("DOOR", message);
            Tattu.post(new DoorActivation(this, true, message));
            return true;
        } else {
            Log.w("DOOR", "Door already open: " + id);
            ToastManager.get().showToast("The door is already open.", 1);
            return false;
        }
    }

    public boolean close(String message) {
        if (isOpened()) {
            Log.i("DOOR", "Closing door: " + id);
            Log.i("DOOR", message);
            Tattu.post(new DoorActivation(this, false, message));
            return true;
        } else {
            Log.w("DOOR", "Door already closed: " + id);
            ToastManager.get().showToast("The door is already closed.", 1);
            return false;
        }
    }

    public void confirm(boolean opened) {
        Log.i("DOOR", "Confirmed door: " + id + " - opened: " + opened);
        this.opened = opened;
        Tattu.post(new DoorToggled(this, opened));
    }

    public void toggle(String message) {
        Log.i("DOOR", "Toggle door: " + id);
        if (this.opened) {
            close(message);
        } else {
            open(message);
        }
    }

    @Subscribe
    public void on(DoorActivation event) {
        activatePin = true;
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
            return new DoorToggled(this, this.opened);
        } else {
            return null;
        }
    }

    public int getId() {
        return id;
    }

    private void disableVoiceRecognition() {
        this.voiceEnabled = false;
        Tattu.post(new VoiceRecognitionDisabled(this));
        persist();
    }

    public void enableVoiceRecognition() {
        this.voiceEnabled = true;
        Tattu.post(new VoiceRecognitionEnabled(this));
        persist();
    }

    public String getOpenPhrase() {
        return openPhrase;
    }

    public void setOpenPhrase(String openPhrase) {
        this.openPhrase = openPhrase;
        persist();
    }

    public String getClosePhrase() {
        return closePhrase;
    }

    public void setClosePhrase(String closePhrase) {
        this.closePhrase = closePhrase;
        persist();
    }

    private void restore() {
        // TODO
    }

    private void persist() {
        // TODO save state
    }

    public void setup(IOIO ioio) throws ConnectionLostException {
        outputPin = ioio.openDigitalOutput(outputPinNumber, false);
        inputPin = ioio.openDigitalInput(inputPinNumber, DigitalInput.Spec.Mode.PULL_DOWN);
    }

    public void loop() throws ConnectionLostException, InterruptedException {
        if (activatePin) {
            activatePin = false;
            // high for 2 seconds and then low again
            outputPin.write(true);
            Thread.sleep(2000);
            outputPin.write(false);
        } else {
            boolean state = inputPin.read(); // true is closed
            if (lastState == null || !lastState.equals(state)) {
                lastState = state;
                confirm(!state);
            }
            Thread.sleep(100);
        }
    }

    @Singleton
    public class One extends Door {
        private One() {
            super(1, 4, 5);
        }
    }

    @Singleton
    public class Two extends Door {
        private Two() {
            super(2, 6, 7);
        }
    }

    public class VoiceRecognitionEnabled {
        public final Door door;

        private VoiceRecognitionEnabled(Door door) {
            this.door = door;
        }
    }

    public class VoiceRecognitionDisabled {
        public final Door door;

        private VoiceRecognitionDisabled(Door door) {
            this.door = door;
        }
    }

    @Override
    public String toString() {
        return id + "";
    }
}
