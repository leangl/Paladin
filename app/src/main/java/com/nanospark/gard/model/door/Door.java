package com.nanospark.gard.model.door;

import android.os.Handler;
import android.os.Looper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.nanospark.gard.GarD;
import com.nanospark.gard.R;
import com.nanospark.gard.events.BoardConnected;
import com.nanospark.gard.events.BoardDisconnected;
import com.nanospark.gard.events.CommandFailed;
import com.nanospark.gard.events.CommandProcessed;
import com.nanospark.gard.events.CommandSent;
import com.nanospark.gard.events.VoiceRecognitionDisabled;
import com.nanospark.gard.events.VoiceRecognitionEnabled;
import com.nanospark.gard.model.user.User;
import com.nanospark.gard.sms.SmsManager;
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

    private State state = State.UNKNOWN;
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
    private Command mPendingCommand;

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

    public boolean send(Command command) {
        return command.apply(this);
    }

    public void confirm(boolean opened) {
        Log.i(toString(), "Confirmed door: " + id + " - opened: " + opened);
        this.state = State.from(opened);
        mActivationTimeoutHandler.removeCallbacksAndMessages(null);
        Tattu.post(new CommandProcessed(this, mPendingCommand));
        mPendingCommand = null;
    }

    private void startAutoClose() {
        long startTime = System.currentTimeMillis();
        mAutoCloseHandler.removeCallbacksAndMessages(null);
        mAutoCloseHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isOpened()) {
                    send(new Close("Auto closing", false));
                } else {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - startTime < 20000) {
                        mAutoCloseHandler.removeCallbacksAndMessages(null);
                        mAutoCloseHandler.postDelayed(this, 20000);
                    } else {
                        send(new Close("Auto closing", false));
                    }
                }
            }
        }, sAutoCloseMillis + 10000);
    }

    @Subscribe
    public void on(CommandSent event) {
        if (event.door != this) return;

        activatePin = true;
        mPendingCommand = event.command;
        mActivationTimeoutHandler.removeCallbacksAndMessages(null);
        mActivationTimeoutHandler.postDelayed(new Runnable() {

            private int increment = 0;

            @Override
            public void run() {
                increment++;
                if (increment < 3) {
                    Log.d(Door.this.toString(), "Retrying command: " + event.command.toString());
                    activatePin = true;
                    mActivationTimeoutHandler.postDelayed(this, 20000);
                } else {
                    Log.d(Door.this.toString(), "Retry failed: " + event.command.toString());
                    Tattu.post(new CommandFailed(event.door, event.command));
                    mSmsManager.sendDoorAlert("Paladin was unable to " + event.command.toString() + " your door.", event.command);
                }
            }
        }, 20000);
    }

    private void on(BoardDisconnected event) {
        this.state = State.UNKNOWN;
    }

    private void on(BoardConnected event) {
        // FIXME check door state before deciding if it's open
        this.state = State.OPEN;
    }

    public State getState() {
        return state;
    }

    public boolean isOpened() {
        return State.OPEN.equals(state);
    }

    public boolean isClosed() {
        return State.CLOSE.equals(state);
    }

    public boolean isReady() {
        return !State.UNKNOWN.equals(state);
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
        public void on(CommandSent event) {
            super.on(event);
        }

        @Subscribe
        public void on(BoardConnected event) {
            super.on(event);
        }

        @Subscribe
        public void on(BoardDisconnected event) {
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
        public void on(CommandSent event) {
            super.on(event);
        }

        @Subscribe
        public void on(BoardConnected event) {
            super.on(event);
        }

        @Subscribe
        public void on(BoardDisconnected event) {
            super.on(event);
        }

    }

    public static class Config {
        public String openPhrase;
        public String closePhrase;
    }

    public static abstract class Command {
        public final String message;
        public final boolean forced;
        public final User user;

        public Command(String message, boolean forced) {
            this(message, forced, null);
        }

        public Command(String message, boolean forced, User user) {
            this.forced = forced;
            this.message = message;
            this.user = user;
        }

        protected abstract boolean apply(Door door);

        public abstract String toString();

        public boolean isOpen() {
            return this instanceof Open;
        }

    }

    public static class Open extends Command {

        public Open(String message, boolean forced) {
            super(message, forced);
        }

        public Open(String message, boolean forced, User user) {
            super(message, forced, user);
        }

        @Override
        protected boolean apply(Door door) {
            Log.i(toString(), "Command received with message: " + message);
            if (door.mPendingCommand != null && !forced) {
                Log.i(toString(), "Another command pending, ignored...");
                return false;
            }
            if (!door.isReady()) {
                Log.w(toString(), "Door not ready: " + door.id);
                ToastManager.get().showToast("The door is not ready.", 1);
                return false;
            }
            if (!door.isOpened()) {
                Log.i(toString(), "Opening door: " + door.id);
                Log.i(toString(), message);
                door.startAutoClose();
                Tattu.post(new CommandSent(door, this, message));
                return true;
            } else {
                Log.w(toString(), "Door already open: " + door.id);
                ToastManager.get().showToast("The door is already open.", 1);
                return false;
            }
        }

        @Override
        public String toString() {
            return "open";
        }
    }

    public static class Close extends Command {

        public Close(String message, boolean forced) {
            super(message, forced);
        }

        public Close(String message, boolean forced, User user) {
            super(message, forced, user);
        }

        @Override
        protected boolean apply(Door door) {
            if (!door.isReady()) {
                Log.w(toString(), "Door not ready: " + door.id);
                ToastManager.get().showToast("The door is not ready.", 1);
                return false;
            }
            if (door.isOpened()) {
                Log.i(toString(), "Closing door: " + door.id);
                Log.i(toString(), message);
                Tattu.post(new CommandSent(door, this, message));
                return true;
            } else {
                Log.w(toString(), "Door already closed: " + door.id);
                ToastManager.get().showToast("The door is already closed.", 1);
                return false;
            }
        }

        @Override
        public String toString() {
            return "close";
        }
    }

    public static class Toggle extends Command {

        public Toggle(String message, boolean forced) {
            super(message, forced);
        }

        public Toggle(String message, boolean forced, User user) {
            super(message, forced, user);
        }

        @Override
        protected boolean apply(Door door) {
            Log.i(toString(), "Toggle door: " + door.id);
            if (door.isOpened()) {
                return door.send(new Close(message, forced));
            } else if (door.isClosed()) {
                return door.send(new Open(message, forced));
            }
            return false;
        }

        @Override
        public String toString() {
            return "toggle";
        }
    }

    public enum State {
        OPEN("Open"), CLOSE("Closed"), UNKNOWN("Unknown");

        public final String description;

        State(String description) {
            this.description = description;
        }

        public static State from(boolean opened) {
            return opened ? OPEN : CLOSE;
        }

        @Override
        public String toString() {
            return description;
        }
    }

}
