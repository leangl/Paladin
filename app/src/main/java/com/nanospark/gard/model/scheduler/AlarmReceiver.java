package com.nanospark.gard.model.scheduler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import roboguice.util.Ln;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String EXTRA_ACTION = "extra_action";
    public static final String EXTRA_ID = "extra_id";

    public AlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String key = intent.getStringExtra(EXTRA_ID);
        Schedule schedule = ScheduleManager.getInstance().getSchedule(key);
        if (schedule != null) {
            schedule.trigger();
        } else {
            Ln.i("Schedule not found");
        }
    }


}
