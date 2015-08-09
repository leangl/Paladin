package mobi.tattu.utils;

import android.content.Context;
import android.os.Handler;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import mobi.tattu.utils.log.Logger;

public class ToastManager {

    private static ToastManager instance;
    private Handler mHandler;
    private int mToastLayoutResId;
    private int mToastTextId;
    public static final int INFINITE_DURATION = -1; // 3.5 seconds
    public static final int LONG_DURATION = 3500; // 3.5 seconds
    public static final int SHORT_DURATION = 2000; // 2 seconds
    public static final int NOT_GROUPED = -1;
    private SparseArray<ToastReference> groups;

    public static synchronized ToastManager get() {
        if (instance == null) {
            instance = new ToastManager();
        }
        return instance;
    }

    public void init(int toastLayoutResId, int toastTextId) {
        mToastLayoutResId = toastLayoutResId;
        mToastTextId = toastTextId;
    }

    private ToastManager() {
        mHandler = Tattu.createUiHandler();
        groups = new SparseArray<>();
    }

    public ToastReference showToast(int toastText, int groupId) {
        return showToast(Tattu.context.getString(toastText), groupId);
    }

    public ToastReference showToast(String toastText) {
        return showToast(toastText, NOT_GROUPED);
    }

    public ToastReference showToast(String toastText, int groupId) {
        return showToast(toastText, SHORT_DURATION, groupId);
    }

    public ToastReference showToast(int toastText) {
        return showToast(Tattu.context.getString(toastText), toastText);
    }

    public ToastReference showToast(int toastText, long duration) {
        return showToast(Tattu.context.getString(toastText), duration, toastText);
    }

    public ToastReference showToast(int toastText, long duration, int groupId) {
        return showToast(Tattu.context.getString(toastText), duration, groupId);
    }

    public ToastReference showToast(final String toastText, final long duration, int groupId) {

        final ToastReference reference = new ToastReference();
        reference.group = groupId;

        // Because toast has to be instantiated inside a looper
        mHandler.postAtFrontOfQueue(() -> showToast(reference, toastText, duration));

        return reference;
    }

    private synchronized void showToast(ToastReference reference, String toastText, long duration) {
        if (mToastLayoutResId != 0) {
            LayoutInflater inflater = (LayoutInflater) Tattu.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(mToastLayoutResId, null);
            TextView text = (TextView) layout.findViewById(mToastTextId);
            text.setText(toastText);
            Toast toast = new Toast(Tattu.context);
            toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 40);
            toast.setDuration(duration >= LONG_DURATION ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
            toast.setView(layout);
            reference.toast = toast;
        } else {
            reference.toast = Toast.makeText(Tattu.context, toastText, Toast.LENGTH_SHORT);
        }

        if (reference.group != NOT_GROUPED) {
            ToastReference otherToastGroup = groups.get(reference.group);
            if (otherToastGroup != null) {
                otherToastGroup.cancel();
            }
            groups.put(reference.group, reference);
        }
        long refreshTick = duration < SHORT_DURATION && duration != INFINITE_DURATION ? duration : SHORT_DURATION;
        long startTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
        for (int i = 0; i <= (duration != INFINITE_DURATION ? duration : 0); i += refreshTick) {
            mHandler.postDelayed(new ShowToastTask(reference, startTime, duration), i);
        }
    }

    private class ShowToastTask implements Runnable {
        private ToastReference mToast;
        private long mStartTime;
        private long mDuration;

        public ShowToastTask(ToastReference toast, long startTime, long duration) {
            mToast = toast;
            mStartTime = startTime;
            mDuration = duration;
        }

        public void run() {
            long currentTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
            if (!mToast.canceled && (mDuration == INFINITE_DURATION || mStartTime + mDuration >= currentTime)) {
                if (mDuration == INFINITE_DURATION) {
                    mHandler.postDelayed(new ShowToastTask(mToast, mStartTime, mDuration), SHORT_DURATION);
                }
                mToast.show();
            } else {
                mToast.cancel();
                Logger.d(this, "Queued Toast ignored.");
            }
        }
    }

    public class ToastReference {

        private Toast toast;
        private int group;
        private boolean canceled = false;

        public void cancel() {
            if (!canceled) {
                canceled = true;
                if (group != NOT_GROUPED) {
                    synchronized (ToastManager.this) {
                        groups.remove(group);
                    }
                }
                if (toast != null) { // If toast has been set
                    toast.cancel();
                }
                toast = null;
            }
        }

        public void show() {
            if (!canceled) {
                toast.show();
            }
        }
    }
}
