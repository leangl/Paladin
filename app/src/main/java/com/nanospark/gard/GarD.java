package com.nanospark.gard;

import android.app.Application;
import android.util.Log;

import com.nanospark.gard.events.CommandSent;
import com.nanospark.gard.events.VoiceRecognizer;
import com.nanospark.gard.model.door.Door;
import com.nanospark.gard.model.log.LogManager;
import com.nanospark.gard.model.scheduler.ScheduleOld;
import com.nanospark.gard.model.scheduler.SchedulerWizard;
import com.nanospark.gard.model.user.UserManager;
import com.nanospark.gard.services.GarDService;
import com.nanospark.gard.sms.SmsManager;
import com.nanospark.gard.ui.activity.MainActivityNew;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.squareup.otto.Subscribe;

import java.util.Set;

import mobi.tattu.utils.StringUtils;
import mobi.tattu.utils.Tattu;
import mobi.tattu.utils.ToastManager;
import mobi.tattu.utils.persistance.datastore.DataStore;
import roboguice.RoboGuice;

/**
 * Created by Leandro on 21/7/2015.
 */
public class GarD extends Application {

    public final static int DOOR_ONE_ID = 1;
    public final static int DOOR_TWO_ID = 2;

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

        // Start service as soon as app starts
        GarDService.start();

        // Start existing schedules
        Set<ScheduleOld> schedules = DataStore.getInstance().getAll(ScheduleOld.class);
        for (ScheduleOld schedule : schedules) {
            SchedulerWizard.initializeAlarm(this, schedule);
        }

        // Initialize Universal Image Loader
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);

    }

    @Subscribe
    public void on(CommandSent event) {
        toast(event.message);
    }

    public void toast(String message) {
        if (StringUtils.isNotBlank(message)) {
            Tattu.runOnUiThread(() -> {
                Log.i("GarD", message);
                ToastManager.get().showToast(message, 10000, 1);
            });
        }
    }
}
