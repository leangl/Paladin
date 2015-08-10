package com.nanospark.gard.scheluded;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.Calendar;

import mobi.tattu.utils.persistance.datastore.DataStore;

public abstract class BaseAlarmReceiver extends BroadcastReceiver {

    public static final String ACTION_OPEN = "mobi.tattu.garagescheluder.ALARM_LAUNCHED_OPEN";
    public static final String ACTION_CLOSE = "mobi.tattu.garagescheluder.ALARM_LAUNCHED_CLOSE";
    public static final String KEY_EXTRA_ACTION = "extra_action";
    public static final String KEY_EXTRA_TIMESTAMP ="extra_timestamp";

    public BaseAlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Calendar calendar = Calendar.getInstance();
        String key = intent.getStringExtra(KEY_EXTRA_ACTION);
        long timeStamp = intent.getLongExtra(KEY_EXTRA_TIMESTAMP,0);
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        try {
            Scheluded scheluded = DataStore.getInstance().getObject(key, Scheluded.class);
            if (scheluded.days.contains(day)) {
                System.out.println("Disparar una accion ");
                launcherEvent();
            }
        } catch (DataStore.ObjectNotFoundException e) {
            Toast.makeText(context, "Error " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public abstract void launcherEvent();
}
