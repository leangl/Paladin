package com.nanospark.gard.model.log;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.nanospark.gard.GarD;
import com.nanospark.gard.events.DoorStateChanged;
import com.nanospark.gard.model.door.Door;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mobi.tattu.utils.Tattu;
import mobi.tattu.utils.persistance.datastore.DataStore;
import roboguice.RoboGuice;

/**
 * Created by Leandro on 1/10/2015.
 */
@Singleton
public class LogManager {

    @Inject
    private DataStore mDataStore;

    public LogManager() {
        Tattu.register(this);
    }

    public static LogManager getInstance() {
        return RoboGuice.getInjector(GarD.instance).getInstance(LogManager.class);
    }

    public List<Log> getLogs() {
        List<Log> logs = new ArrayList<>(mDataStore.getAll(Log.class));
        Collections.sort(logs, (l1, l2) -> l1.getDate().compareTo(l2.getDate()));
        Collections.reverse(logs);
        return logs;
    }

    public Log getLastLog(Door door) {
        List<Log> logList = getLogs();
        for (int i = 0; i < logList.size(); i++) {
            Log log = logList.get(i);
            if (door.getId() == log.getDoorId()) {
                return log;
            }
        }
        return null;
    }

    @Subscribe
    public void on(DoorStateChanged event) {
        if (event.state != Door.State.UNKNOWN) {
            mDataStore.put(new Log(event));
        }
    }

}
