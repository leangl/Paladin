package com.nanospark.gard;

import android.app.Application;
import android.util.Log;

import com.nanospark.gard.events.DoorActivated;
import com.nanospark.gard.events.VoiceRecognizer;
import com.nanospark.gard.model.door.Door;
import com.nanospark.gard.model.scheduler.Schedule;
import com.nanospark.gard.model.scheduler.SchedulerWizard;
import com.nanospark.gard.services.GarDService;
import com.nanospark.gard.sms.SmsManager;
import com.nanospark.gard.ui.MainActivity;
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
        Tattu.init(this, MainActivity.class);
        Tattu.register(this);

        instance = this;

        // force initialization of singletons TODO find a cleaner way
        Door.getInstance(DOOR_ONE_ID);
        Door.getInstance(DOOR_TWO_ID);
        VoiceRecognizer.getInstance();
        SmsManager.getInstance();

        GarDService.start();

        Set<Schedule> schedules = DataStore.getInstance().getAll(Schedule.class);
        for (Schedule schedule : schedules) {
            SchedulerWizard.initializeAlarm(this, schedule);
        }

    }

    @Subscribe
    public void on(DoorActivated event) {
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
