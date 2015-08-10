package com.nanospark.gard.scheluded;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Set;

import mobi.tattu.utils.persistance.datastore.DataStore;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("alarm", Context.MODE_PRIVATE);
            Set<String> keys = sharedPreferences.getAll().keySet();
            for (String key : keys) {
                try {
                    Scheluded scheluded = DataStore.getInstance().getObject(key, Scheluded.class);
                    BuilderWizardScheluded.initializeAlarm(context, scheluded);
                } catch (DataStore.ObjectNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
