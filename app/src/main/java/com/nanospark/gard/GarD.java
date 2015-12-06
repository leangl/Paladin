package com.nanospark.gard;

import android.app.Application;
import android.util.Log;

import com.nanospark.gard.events.BoardConnected;
import com.nanospark.gard.events.BoardDisconnected;
import com.nanospark.gard.events.CommandSent;
import com.nanospark.gard.model.door.Door;
import com.nanospark.gard.model.log.LogManager;
import com.nanospark.gard.model.scheduler.ScheduleManager;
import com.nanospark.gard.model.user.UserManager;
import com.nanospark.gard.sms.SmsManager;
import com.nanospark.gard.ui.activity.MainActivityNew;
import com.nanospark.gard.voice.VoiceRecognizer;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.squareup.otto.Subscribe;

import mobi.tattu.utils.StringUtils;
import mobi.tattu.utils.Tattu;
import mobi.tattu.utils.ToastManager;
import roboguice.RoboGuice;

/**
 * Created by Leandro on 21/7/2015.
 */
public class GarD extends Application {

    public final static int DOOR_ONE_ID = 1;
    public final static int DOOR_TWO_ID = 2;

    private static boolean sBoardConnected;

    static {
        RoboGuice.setUseAnnotationDatabases(false);
    }

    public static GarD instance;

    @Override
    public void onCreate() {
        super.onCreate();
        Tattu.init(this, MainActivityNew.class);
        Tattu.register(this);

        instance = this;

        // force initialization of singletons TODO find a cleaner way
        Door.getInstance(DOOR_ONE_ID);
        Door.getInstance(DOOR_TWO_ID);
        VoiceRecognizer.getInstance();
        SmsManager.getInstance();
        LogManager.getInstance();
        UserManager.getInstance();
        ScheduleManager.getInstance().init();

        // Start service as soon as app starts
        //GarDService.start();

        // Initialize Universal Image Loader
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);

    }

    @Subscribe
    public void on(CommandSent event) {
        toast(event.message);
    }

    @Subscribe
    public void on(BoardConnected event) {
        sBoardConnected = true;
    }

    @Subscribe
    public void on(BoardDisconnected event) {
        sBoardConnected = false;
    }

    public static boolean isBoardConnected() {
        return sBoardConnected;
    }

    public void toast(String message) {
        if (StringUtils.isNotBlank(message)) {
            Tattu.runOnUiThread(() -> {
                Log.i("GarD", message);
                ToastManager.get().show(message, 10000, 1);
            });
        }
    }
}
