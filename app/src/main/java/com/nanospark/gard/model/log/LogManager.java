package com.nanospark.gard.model.log;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.nanospark.gard.events.DoorToggled;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import mobi.tattu.utils.Tattu;
import mobi.tattu.utils.persistance.datastore.DataStore;

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

    public List<Log> getLogs() {
        // TODO descomentar esto y borrar lo de abajo
        //List<Log> logs = new ArrayList<>(mDataStore.getAll(Log.class));
        //Collections.sort(logs, (l1, l2) -> l1.getDate().compareTo(l2.getDate()));
        //Collections.reverse(logs);
        //return logs;

        ArrayList<Log> resLogs = new ArrayList<>();

        resLogs.add(createLog(1, Log.EVENT_OPEN, getDate(Calendar.HOUR, 2)));
        resLogs.add(createLog(1, Log.EVENT_CLOSE, getDate(Calendar.HOUR, 4)));
        resLogs.add(createLog(1, Log.EVENT_OPEN, getDate(Calendar.HOUR, 4)));
        resLogs.add(createLog(1, Log.EVENT_CLOSE, getDate(Calendar.HOUR, 5)));
        resLogs.add(createLog(1, Log.EVENT_OPEN, getDate(Calendar.HOUR, 6)));

        return resLogs;
    }

    private Log createLog(int id, String event, Date date) {
        Log log = new Log();
        log.setDate(date);
        log.setDoorId(id);
        log.setEvent(event);

        return log;

    }
    
    private Date getDate(int fields, int value) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(fields, value);
        calendar.set(Calendar.MINUTE, value);
        return calendar.getTime();
    }

    @Subscribe
    public void on(DoorToggled event) {
        mDataStore.putObject(new Log(event));
    }

}
