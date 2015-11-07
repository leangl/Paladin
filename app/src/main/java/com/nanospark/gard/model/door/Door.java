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
import com.nanospark.gard.events.DoorStateChanged;
import com.nanospark.gard.events.VoiceRecognitionDisabled;
import com.nanospark.gard.events.VoiceRecognitionEnabled;
import com.nanospark.gard.events.VoiceRecognizer;
import com.nanospark.gard.model.user.User;
import com.nanospark.gard.sms.SmsManager;
import com.squareup.otto.Subscribe;

import java.util.concurrent.TimeUnit;

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

    private int mId;
    private State mState = State.UNKNOWN;
    private int mControlPinNumber;
    private int mClosedPinNumber;
    private int mOpenPinNumber;
    private DigitalOutput mOutputPin;
    private DigitalInput mClosedPin;
    private DigitalInput mOpenPin;
    private boolean mActivatePin;
    private Config mConfig;
    private Handler mAutoCloseHandler;
    private Handler mActivationTimeoutHandler;
    private Command mPendingCommand;
    private boolean mBoardConnected = false;
    private boolean mVoiceEnabled;

    @Inject
    private DataStore mDataStore;
    @Inject
    private SmsManager mSmsManager;

    public Door(int id, Integer controlPinNumber, Integer closedPinNumber, Integer openPinNumber) {
        mId = id;
        mControlPinNumber = controlPinNumber;
        mClosedPinNumber = closedPinNumber;
        mOpenPinNumber = openPinNumber;
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

    public static void setAutoClose(TimeUnit unit, long value) {
        AutoCloseConfig config = getAutoCloseConfig();
        config.unit = unit;
        config.value = value;
        persist(config);
    }

    public static TimeUnit getAutoCloseUnit() {
        return getAutoCloseConfig().unit;
    }

    public static long getAutoCloseValue() {
        return getAutoCloseConfig().value;
    }

    public static void enableAutoClose() {
        AutoCloseConfig config = getAutoCloseConfig();
        config.enabled = true;
        persist(config);
    }

    public static void disableAutoClose() {
        AutoCloseConfig config = getAutoCloseConfig();
        config.enabled = false;
        persist(config);
    }

    public static boolean isAutoCloseEnabled() {
        return getAutoCloseConfig().enabled;
    }

    private static AutoCloseConfig getAutoCloseConfig() {
        if (sAutoCloseConfig == null) {
            sAutoCloseConfig = DataStore.getInstance().getObject(AutoCloseConfig.class.getSimpleName(), AutoCloseConfig.class).get();
            if (sAutoCloseConfig == null) {
                sAutoCloseConfig = new AutoCloseConfig();
            }
        }
        return sAutoCloseConfig;
    }

    public static void persist(AutoCloseConfig config) {
        DataStore.getInstance().putObject(AutoCloseConfig.class.getSimpleName(), config);
    }

    public static AutoCloseConfig sAutoCloseConfig;

    public static class AutoCloseConfig {
        public TimeUnit unit = TimeUnit.MINUTES;
        public long value = 10;
        public boolean enabled = false;
    }

    public static final Door[] getDoors() {
        return new Door[]{getInstance(1), getInstance(2)};
    }

    public boolean send(Command command) {
        return command.apply(this);
    }

    public void confirm(State state) {
        Log.i(toString(), "Confirmed door: " + mId + " - state: " + state);
        mState = state;
        Tattu.post(new DoorStateChanged(this, mState));
        if (mState != State.UNKNOWN) {
            mActivationTimeoutHandler.removeCallbacksAndMessages(null);
            Tattu.post(new CommandProcessed(this, mState, mPendingCommand));
            mPendingCommand = null;
        }
    }

    private void startAutoClose() {
        if (!isAutoCloseEnabled()) return;

        long startTime = System.currentTimeMillis();
        mAutoCloseHandler.removeCallbacksAndMessages(null);
        mAutoCloseHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isOpen()) {
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
        }, getAutoCloseUnit().toMillis(getAutoCloseValue()) + 10000); // as per wireframe: Wait {Auto-Close Interval} + 10s
    }

    @Subscribe
    public void on(CommandSent event) {
        if (event.door != this) return;

        mActivatePin = true;
        mPendingCommand = event.command;
        mActivationTimeoutHandler.removeCallbacksAndMessages(null);
        mActivationTimeoutHandler.postDelayed(new Runnable() {

            private int increment = 0;

            @Override
            public void run() {
                increment++;
                if (increment < 3) {
                    Log.d(Door.this.toString(), "Retrying command: " + event.command.toString());
                    mActivatePin = true;
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
        mBoardConnected = false;
    }

    private void on(BoardConnected event) {
        mBoardConnected = true;
    }

    @Subscribe
    public void on(VoiceRecognizer.StateChanged event) {
        if (event.door.equals(this)) {
            mVoiceEnabled = event.state == VoiceRecognizer.State.STARTED;
        } else {
            mVoiceEnabled = false;
        }
    }

    public boolean isVoiceEnabled() {
        return mVoiceEnabled;
    }

    public State getState() {
        return !mBoardConnected ? State.UNKNOWN : mState;
    }

    public boolean isOpen() {
        return State.OPEN.equals(mState);
    }

    public boolean isClosed() {
        return State.CLOSED.equals(mState);
    }

    public boolean isReady() {
        return !State.UNKNOWN.equals(mState);
    }

    public int getId() {
        return mId;
    }

    public void disableVoiceRecognition() {
        Tattu.post(new VoiceRecognitionDisabled(this));
    }

    public void enableVoiceRecognition() {
        Tattu.post(new VoiceRecognitionEnabled(this));
    }

    public String getName() {
        return mConfig.name;
    }

    public void setName(String name) {
        mConfig.name = name;
        persist();
    }

    public String getOpenPhrase() {
        return mConfig.openPhrase;
    }

    public void setOpenPhrase(String openPhrase) {
        mConfig.openPhrase = openPhrase.toLowerCase();
        persist();
    }

    public String getClosePhrase() {
        return mConfig.closePhrase;
    }

    public void setClosePhrase(String closePhrase) {
        mConfig.closePhrase = closePhrase.toLowerCase();
        persist();
    }

    public boolean isEnabled() {
        return mConfig.enabled;
    }

    public void setEnabled(boolean enabled) {
        mConfig.enabled = enabled;
        persist();
    }

    public boolean isOpenSwitchEnabled() {
        return mConfig.openSwitchEnabled;
    }

    public void setOpenSwitchEnabled(boolean enabled) {
        mConfig.openSwitchEnabled = enabled;
        persist();
    }

    public boolean isCloseSwitchEnabled() {
        return mConfig.closeSwitchEnabled;
    }

    public void setCloseSwitchEnabled(boolean enabled) {
        mConfig.closeSwitchEnabled = enabled;
        persist();
    }

    private void restore() {
        mConfig = mDataStore.getObject(getId(), Config.class).get();
        if (mConfig == null) {
            mConfig = new Config();
            mConfig.name = "Door " + getId();
            mConfig.openPhrase = GarD.instance.getString(R.string.default_open);
            mConfig.closePhrase = GarD.instance.getString(R.string.default_close);
        }
    }

    private void persist() {
        mDataStore.putObject(getId(), mConfig);
    }

    public void setup(IOIO ioio) throws ConnectionLostException {
        mOutputPin = ioio.openDigitalOutput(mControlPinNumber, false);
        mClosedPin = ioio.openDigitalInput(mClosedPinNumber, DigitalInput.Spec.Mode.PULL_DOWN);
        mOpenPin = ioio.openDigitalInput(mOpenPinNumber, DigitalInput.Spec.Mode.PULL_DOWN);
    }

    public void loop() throws ConnectionLostException, InterruptedException {
        if (mActivatePin) {
            mActivatePin = false;
            // high for 2 seconds and then low again
            mOutputPin.write(true);
            Thread.sleep(2000);
            mOutputPin.write(false);
        } else {
            if (isOpenSwitchEnabled() && isCloseSwitchEnabled()) {
                boolean isClosed = mClosedPin.read(); // true is closed
                boolean isOpen = mOpenPin.read(); // true is open
                Boolean triState = null;
                if (isClosed != isOpen) { // both pin LOW or HIGH means state is UNKNOWN
                    triState = isOpen;
                }
                State state = State.from(triState);
                if (!mState.equals(state)) {
                    confirm(state);
                }
            } else if (isCloseSwitchEnabled()) {
                State state = State.from(!mClosedPin.read()); // true is closed
                if (!mState.equals(state)) {
                    confirm(state);
                }
            } else if (isOpenSwitchEnabled()) {
                State state = State.from(mOpenPin.read()); // true is open
                if (!mState.equals(state)) {
                    confirm(state);
                }
            }
            Thread.sleep(100);
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    @Singleton
    public static class One extends Door {
        @Inject
        private One() {
            super(1, 2, 4, 5);
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

        @Subscribe
        public void on(VoiceRecognizer.StateChanged event) {
            super.on(event);
        }

    }

    @Singleton
    public static class Two extends Door {
        @Inject
        private Two() {
            super(2, 3, 6, 7);
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

        @Subscribe
        public void on(VoiceRecognizer.StateChanged event) {
            super.on(event);
        }
    }

    public static class Config {
        public String name;
        public String openPhrase;
        public String closePhrase;
        public boolean enabled = true;
        public boolean openSwitchEnabled = true;
        public boolean closeSwitchEnabled = true;
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
                Log.w(toString(), "Door not ready: " + door.mId);
                ToastManager.get().showToast("The door is not ready.", 1);
                return false;
            }
            if (!door.isOpen()) {
                Log.i(toString(), "Opening door: " + door.mId);
                Log.i(toString(), message);
                door.startAutoClose();
                Tattu.post(new CommandSent(door, this, message));
                return true;
            } else {
                Log.w(toString(), "Door already open: " + door.mId);
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
                Log.w(toString(), "Door not ready: " + door.mId);
                ToastManager.get().showToast("The door is not ready.", 1);
                return false;
            }
            if (door.isOpen()) {
                Log.i(toString(), "Closing door: " + door.mId);
                Log.i(toString(), message);
                Tattu.post(new CommandSent(door, this, message));
                return true;
            } else {
                Log.w(toString(), "Door already closed: " + door.mId);
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
            Log.i(toString(), "Toggle door: " + door.mId);
            if (door.isOpen()) {
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
        OPEN("Open"), CLOSED("Closed"), UNKNOWN("Unknown");

        public final String description;

        State(String description) {
            this.description = description;
        }

        public static State from(Boolean opened) {
            return opened == null ? UNKNOWN : opened ? OPEN : CLOSED;
        }

        public Boolean toBoolean() {
            switch (this) {
                case OPEN:
                    return true;
                case CLOSED:
                    return false;
                case UNKNOWN:
                default:
                    return null;
            }
        }

        @Override
        public String toString() {
            return description;
        }
    }

}
