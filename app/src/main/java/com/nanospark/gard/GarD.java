package com.nanospark.gard;

import android.app.Application;

import com.nanospark.gard.events.VoiceRecognitionEventProducer;
import com.nanospark.gard.scheduler.Schedule;
import com.nanospark.gard.scheduler.SchedulerWizard;
import com.nanospark.gard.ui.MainActivity;

import java.util.Set;

import mobi.tattu.utils.Tattu;
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

        Door.getInstance(); // force initialization
        VoiceRecognitionEventProducer.getInstance(); // force initialization

        //GarDService.start();

        Set<Schedule> schedules = DataStore.getInstance().getAll(Schedule.class);
        for (Schedule schedule : schedules) {
            SchedulerWizard.initializeAlarm(this, schedule);
        }

    }
}
