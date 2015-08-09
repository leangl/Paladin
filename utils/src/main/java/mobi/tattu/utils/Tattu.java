package mobi.tattu.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;

import com.squareup.otto.Bus;

import mobi.tattu.utils.log.Logger;
import mobi.tattu.utils.preferences.Config;

/**
 * Created by Leandro on 16/02/2015.
 */
public class Tattu {

    public static Application context;
    public static Class<? extends Activity> mainActivity;

    public static final Handler uiHandler = createUiHandler();
    public static final Bus bus = new Bus();
    private static HandlerThread workerThread;

    public static final void init(Context ctx, Class<? extends Activity> mainActivity) {
        Tattu.context = (Application) ctx.getApplicationContext();
        Tattu.mainActivity = mainActivity;
        Tattu.workerThread = new HandlerThread("WORKER", Process.THREAD_PRIORITY_BACKGROUND);
        Tattu.workerThread.start();

        Logger.init(context);

        //ToastManager.get().init(R.layout.toast, R.id.toast_text);
        Config.get().setDefaultPreferences(false, null);
    }

    public static final void runOnUiThread(Runnable r) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            r.run();
        } else {
            uiHandler.post(r);
        }
    }

    public static final void runOnUiThread(Runnable r, long delay) {
        uiHandler.postDelayed(r, delay);
    }

    public static final Bus bus() {
        return bus;
    }

    public static final HandlerThread getWorker() {
        return Tattu.workerThread;
    }

    public static void post(Object event) {
        runOnUiThread(() -> bus().post(event));
    }

    public static void register(Object obj) {
        runOnUiThread(() -> bus().register(obj));
    }

    public static Handler createUiHandler() {
        return new Handler(Looper.getMainLooper());
    }

}
