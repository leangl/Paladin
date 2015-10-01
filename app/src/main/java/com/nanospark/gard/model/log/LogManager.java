package com.nanospark.gard.model.log;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.nanospark.gard.events.DoorToggled;
import com.squareup.otto.Subscribe;

import java.util.Set;

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

    public Set<Log> getLogs() {
        return mDataStore.getAll(Log.class);
    }

    @Subscribe
    public void on(DoorToggled event) {
        mDataStore.putObject(new Log(event));
    }

}
