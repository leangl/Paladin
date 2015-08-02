package mobi.tattu.utils.events;

import android.os.Looper;

import com.squareup.otto.Bus;

import mobi.tattu.utils.Tattu;

/**
 * Created by Leandro on 29/6/2015.
 */
public class BusHolder {

    private static final Bus bus = new Bus();

    public static Bus bus() {
        return bus;
    }

    public static void post(Object event) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            bus().post(event);
        } else {
            Tattu.runOnUiThread(() -> bus().post(event));
        }
    }

}
