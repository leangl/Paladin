package mobi.tattu.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

import mobi.tattu.utils.log.Logger;
import mobi.tattu.utils.preferences.Config;

/**
 * Created by Leandro on 16/02/2015.
 */
public class Tattu {

    public static Application context;
    public static Class<? extends Activity> mainActivity;

    public static final Handler uiHandler = new Handler();
    public static final Bus bus = new Bus();

    public static final void init(Context ctx, Class<? extends Activity> mainActivity) {
        Tattu.context = (Application) ctx.getApplicationContext();
        Tattu.mainActivity = mainActivity;
        Logger.init(context);
        //ToastManager.get().init(R.layout.toast, R.id.toast_text);
        Config.get().setDefaultPreferences(false, null);
    }

    public static final void runOnUiThread(Runnable r) {
        uiHandler.post(r);
    }

    public static final void runOnUiThread(Runnable r, long delay) {
        uiHandler.postDelayed(r, delay);
    }

    public static final Bus bus() {
        return bus;
    }

    public static void post(Object event) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            bus().post(event);
        } else {
            uiHandler.post(() -> bus().post(event));
        }
    }

    public static void register(Object obj) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            bus().register(obj);
        } else {
            uiHandler.post(() -> bus().register(obj));
        }
    }

}
