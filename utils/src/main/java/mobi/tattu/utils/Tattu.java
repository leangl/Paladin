package mobi.tattu.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;

import com.squareup.otto.Bus;

import mobi.tattu.utils.events.AppInstalled;
import mobi.tattu.utils.events.AppUpdated;
import mobi.tattu.utils.log.Logger;
import mobi.tattu.utils.persistance.datastore.DataStore;
import mobi.tattu.utils.preferences.Config;
import roboguice.util.Ln;

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

        //checkVersion();
    }

    private static void checkVersion() throws Exception {
        Integer currentVersionCode = Class.forName(context.getPackageName() + ".BuildConfig").getField("VERSION_CODE").getInt(null);
        Integer previousVersionCode = DataStore.getInstance().get("VERSION_CODE", Integer.class).get();
        if (previousVersionCode == null) {
            // First install
            DataStore.getInstance().put("VERSION_CODE", currentVersionCode);
            post(new AppInstalled());
            Ln.i("App installed: " + currentVersionCode);
        } else {
            if (previousVersionCode < currentVersionCode) {
                // App updated
                DataStore.getInstance().put("VERSION_CODE", currentVersionCode);
                post(new AppUpdated(previousVersionCode, currentVersionCode));
                Ln.i("App updated: " + previousVersionCode + " > " + currentVersionCode);
            } else if (previousVersionCode > currentVersionCode) {
                // App downgraded
                Ln.e("App downgraded: " + previousVersionCode + " > " + currentVersionCode);
            } else {
                Ln.d("Version not changed");
            }
        }
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

    public static void unregister(Object obj) {
        runOnUiThread(() -> bus().unregister(obj));
    }

    public static Handler createUiHandler() {
        return new Handler(Looper.getMainLooper());
    }

}
