package com.nanospark.gard;

import android.app.Application;

import mobi.tattu.utils.Tattu;

/**
 * Created by Leandro on 21/7/2015.
 */
public class GarD extends Application {

    public static GarD instance;

    @Override
    public void onCreate() {
        super.onCreate();
        Tattu.init(this, MainActivity.class);
        Tattu.bus().register(this);

        instance = this;

    }
}
