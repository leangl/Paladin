package com.nanospark.gard.model.scheduler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.nanospark.gard.GarD;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import mobi.tattu.utils.F;
import mobi.tattu.utils.Tattu;
import mobi.tattu.utils.annotations.Cache;
import mobi.tattu.utils.persistance.datastore.DataStore;
import roboguice.RoboGuice;

/**
 * Created by Leandro on 1/10/2015.
 */
@Singleton
public class ScheduleManager {

    private final AlarmManager mAlarmManager;
    @Inject
    private DataStore mDataStore;
    @Cache
    private List<Schedule> mCached;

    public ScheduleManager() {
        Tattu.register(this);
        mAlarmManager = (AlarmManager) GarD.instance.getSystemService(Context.ALARM_SERVICE);
    }

    public void init() {
        for (Schedule schedule : getAll()) {
            initializeSchedule(schedule);
        }
    }

    public static ScheduleManager getInstance() {
        return RoboGuice.getInjector(GarD.instance).getInstance(ScheduleManager.class);
    }

    public List<Schedule> getAll() {
        if (mCached == null) {
            mCached = new ArrayList<>(mDataStore.getAll(Schedule.class));
            Collections.sort(mCached);
        }
        return mCached;
    }

    public void add(Schedule schedule) {
        mDataStore.put(schedule);
        initializeSchedule(schedule);
        mCached = null;
    }

    public void update(Schedule schedule) {
        add(schedule);
    }

    public Schedule find(F.Predicate<Schedule> p) {
        for (Schedule schedule : getAll()) {
            if (p.test(schedule)) {
                return schedule;
            }
        }
        return null;
    }

    public Schedule getSchedule(String id) {
        // TODO get from cache
        return mDataStore.get(id, Schedule.class).get();
    }

    public void delete(Schedule schedule) {
        mDataStore.delete(schedule, Schedule.class);
        mCached = null;
    }

    private void initializeSchedule(Schedule schedule) {
        if (schedule.isOpenTimeSet()) {
            schedule(schedule, schedule.getOpenHour(), schedule.getOpenMinute(), "open");
        }
        if (schedule.isCloseTimeSet()) {
            schedule(schedule, schedule.getCloseHour(), schedule.getCloseMinute(), "close");
        }
    }

    private void schedule(Schedule schedule, int hour, int minute, String action) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        long timeInMillis = calendar.getTimeInMillis();

        PendingIntent alarmIntent = getPendingIntent(schedule, action);
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, timeInMillis, TimeUnit.HOURS.toMillis(24), alarmIntent);
    }

    private PendingIntent getPendingIntent(Schedule schedule, String action) {
        Intent intent = new Intent(GarD.instance, AlarmReceiver.class);
        intent.setAction(schedule.getId() + "/" + action); // unique name for this schedule
        intent.putExtra(AlarmReceiver.EXTRA_ID, schedule.getId());
        intent.putExtra(AlarmReceiver.EXTRA_ACTION, action);
        return PendingIntent.getBroadcast(GarD.instance, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void override(Schedule schedule) {
        schedule.override();
    }
}
