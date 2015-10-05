package com.nanospark.gard.model.door;

import android.os.Handler;
import android.os.Looper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.nanospark.gard.GarD;
import com.nanospark.gard.R;
import com.nanospark.gard.events.DoorActivated;
import com.nanospark.gard.events.DoorToggled;
import com.nanospark.gard.events.VoiceRecognitionDisabled;
import com.nanospark.gard.events.VoiceRecognitionEnabled;
import com.nanospark.gard.sms.SmsManager;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.spi.Log;
import mobi.tattu.utils.Tattu;
import mobi.tattu.utils.ToastManager;
import mobi.tattu.utils.persistance.datastore.DataStore;
import roboguice.RoboGuice;

/**
 * Created by Leandro on 9/8/2015.
 */
public class Door {

    private static int sAutoCloseMillis = 0;

    private Boolean opened = true;
    private int id;
    private int inputPinNumber;
    private int outputPinNumber;
    private DigitalOutput outputPin;
    private DigitalInput inputPin;
    private boolean activatePin;
    private Boolean lastState;
    private boolean voiceEnabled;
    private Config config;
    private Handler mAutoCloseHandler;
    private Handler mActivationTimeoutHandler;
    private boolean mPendingConfirmation;

    @Inject
    private DataStore mDataStore;
    @Inject
    private SmsManager mSmsManager;

    public Door(int id, Integer outputPinNumber, Integer inputPinNumber) {
        this.id = id;
        this.outputPinNumber = outputPinNumber;
        this.inputPinNumber = inputPinNumber;
        mDataStore = DataStore.getInstance();
        mAutoCloseHandler = new Handler(Looper.getMainLooper());
        mActivationTimeoutHandler = new Handler(Looper.getMainLooper());
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

    public static void setAutoCloseTimer(int millis) {
        sAutoCloseMillis = millis;
    }

    public static int getAutoCloseTimer() {
        return sAutoCloseMillis;
    }

    public static final Door[] getDoors() {
        return new Door[]{getInstance(1), getInstance(2)};
    }

    public boolean open(String message, boolean forced) {
        Log.i(toString(), "Command received with message: " + message);
        if (mPendingConfirmation && !forced) {
            Log.i(toString(), "Another command pending, ignored...");
            return false;
        }
        if (!isOpened()) {
            Log.i(toString(), "Opening door: " + id);
            Log.i(toString(), message);
            startAutoClose();
            Tattu.post(new DoorActivated(this, true, message));
            return true;
        } else {
            Log.w(toString(), "Door already open: " + id);
            ToastManager.get().showToast("The door is already open.", 1);
            return false;
        }
    }

    public boolean close(String message, boolean forced) {
        if (isOpened()) {
            Log.i("DOOR", "Closing door: " + id);
            Log.i("DOOR", message);
            Tattu.post(new DoorActivated(this, false, message));
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
        mPendingConfirmation = false;
        mActivationTimeoutHandler.removeCallbacksAndMessages(null);
        Tattu.post(new DoorToggled(this, opened));
    }

    private void startAutoClose() {
        long startTime = System.currentTimeMillis();
        mAutoCloseHandler.removeCallbacksAndMessages(null);
        mAutoCloseHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isOpened()) {
                    close("Auto closing", false);
                } else {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - startTime < 20000) {
                        mAutoCloseHandler.removeCallbacksAndMessages(null);
                        mAutoCloseHandler.postDelayed(this, 20000);
                    } else {
                        close("Auto closing", false);
                    }
                }
            }
        }, sAutoCloseMillis + 10000);
    }

    public void toggle(String message, boolean forced) {
        Log.i("DOOR", "Toggle door: " + id);
        if (this.opened) {
            close(message, forced);
        } else {
            open(message, forced);
        }
    }

    @Subscribe
    public void on(DoorActivated event) {
        if (event.door != this) return;

        activatePin = true;
        mPendingConfirmation = true;
        mActivationTimeoutHandler.removeCallbacksAndMessages(null);
        mActivationTimeoutHandler.postDelayed(new Runnable() {

            private int increment = 0;

            @Override
            public void run() {
                increment++;
                String action = event.opened ? "open" : "close";
                if (increment < 3) {
                    Log.d(Door.this.toString(), "Retrying command: " + action);
                    activatePin = true;
                    mActivationTimeoutHandler.postDelayed(this, 20000);
                } else {
                    Log.d(Door.this.toString(), "Retry failed: " + action);
                    mSmsManager.sendDoorAlert("Paladin was unable to " + action + " your door.", event.opened);
                }
            }
        }, 20000);
    }

    public boolean isOpened() {
        return this.opened;
    }

    public boolean isClosed() {
        return !isOpened();
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

    public void disableVoiceRecognition() {
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
        return config.openPhrase;
    }

    public void setOpenPhrase(String openPhrase) {
        this.config.openPhrase = openPhrase;
        persist();
    }

    public String getClosePhrase() {
        return config.closePhrase;
    }

    public void setClosePhrase(String closePhrase) {
        this.config.closePhrase = closePhrase;
        persist();
    }

    private void restore() {
        config = mDataStore.getObject(getId(), Config.class).get();
        if (config == null) {
            config = new Config();
            config.openPhrase = GarD.instance.getString(R.string.default_open);
            config.closePhrase = GarD.instance.getString(R.string.default_close);
        }
    }

    private void persist() {
        if (config != null) {
            mDataStore.putObject(getId(), config);
        }
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

    @Override
    public String toString() {
        return "Door" + id;
    }

    @Singleton
    public static class One extends Door {
        @Inject
        private One() {
            super(1, 4, 5);
        }

        @Subscribe
        public void on(DoorActivated event) {
            super.on(event);
        }
    }

    @Singleton
    public static class Two extends Door {
        @Inject
        private Two() {
            super(2, 6, 7);
        }

        @Subscribe
        public void on(DoorActivated event) {
            super.on(event);
        }
    }

    public static class Config {
        public String openPhrase;
        public String closePhrase;
    }

}
