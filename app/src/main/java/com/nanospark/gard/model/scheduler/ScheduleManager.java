package com.nanospark.gard.model.scheduler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.nanospark.gard.GarD;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mobi.tattu.utils.F;
import mobi.tattu.utils.Tattu;
import mobi.tattu.utils.persistance.datastore.DataStore;
import roboguice.RoboGuice;

/**
 * Created by Leandro on 1/10/2015.
 */
@Singleton
public class ScheduleManager {

    @Inject
    private DataStore mDataStore;

    public ScheduleManager() {
        Tattu.register(this);
    }

    public static ScheduleManager getInstance() {
        return RoboGuice.getInjector(GarD.instance).getInstance(ScheduleManager.class);
    }

    public List<Schedule> getAll() {
        List<Schedule> all = new ArrayList<>(mDataStore.getAll(Schedule.class));
        Collections.sort(all);
        return all;
    }

    public void add(Schedule schedule) {
        mDataStore.putObject(schedule);
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
        return mDataStore.getObject(id, Schedule.class).get();
    }

    public void delete(Schedule schedule) {
        mDataStore.delete(Schedule.class, schedule);
    }

}
