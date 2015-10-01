package com.nanospark.gard.scheduler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ioio.lib.spi.Log;
import mobi.tattu.utils.persistance.datastore.DataStore;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String ACTION = "alarm_fired";

    public static final String KEY_EXTRA_NAME = "extra_name";
    //public static final String KEY_EXTRA_TIMESTAMP = "extra_timestamp";

    public AlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String key = intent.getStringExtra(KEY_EXTRA_NAME);
        Schedule schedule = DataStore.getInstance().getObject(key, Schedule.class).get();
        if (schedule != null) {
            if (schedule.trigger()) {
                Log.i("Scheduler", "Schedule triggered: " + schedule);
            } else {
                Log.e("Scheduler", "Schedule not triggered: " + schedule);
            }
        }
    }


}
