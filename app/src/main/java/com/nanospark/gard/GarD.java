package com.nanospark.gard;

import android.app.Application;
import android.util.Log;

import com.nanospark.gard.door.BaseDoor;
import com.nanospark.gard.events.DoorActivation;
import com.nanospark.gard.events.VoiceRecognition;
import com.nanospark.gard.scheduler.Schedule;
import com.nanospark.gard.scheduler.SchedulerWizard;
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

    static {
        RoboGuice.setUseAnnotationDatabases(false);
    }

    public static GarD instance;

    @Override
    public void onCreate() {
        super.onCreate();
        Tattu.init(this, MainActivity.class);
        Tattu.bus().register(this);

        instance = this;

        BaseDoor.getInstance(1); // force initialization
        BaseDoor.getInstance(2); // force initialization
        VoiceRecognition.getInstance(); // force initialization

        //GarDService.start();

        Set<Schedule> schedules = DataStore.getInstance().getAll(Schedule.class);
        for (Schedule schedule : schedules) {
            SchedulerWizard.initializeAlarm(this, schedule);
        }

    }

    @Subscribe
    public void on(DoorActivation event) {
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
