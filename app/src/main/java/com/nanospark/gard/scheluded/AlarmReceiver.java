package com.nanospark.gard.scheluded;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.Calendar;

import mobi.tattu.utils.persistance.datastore.DataStore;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String ACTION = "mobi.tattu.garagescheluder.ALARM_LAUNCHED";
    public static final String KEY_EXTRA_ACTION = "extra_action";

    public AlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Calendar calendar = Calendar.getInstance();
        String key = intent.getStringExtra(KEY_EXTRA_ACTION);
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        try {
            Scheluded scheluded = DataStore.getInstance().getObject(key, Scheluded.class);
            if (scheluded.days.contains(day)) {
                if (BuilderWizardScheluded.ACTION_OPEN_DOOR.equals(key)) {
                    System.out.println("Disparar Accion Abrir Porton");
                    Toast.makeText(context, "Disparar Accion Abrir Porton", Toast.LENGTH_SHORT).show();
                } else {
                    System.out.println("Disparar Accion Cerrar Porton");
                    Toast.makeText(context, "Disparar Accion Cerrar Porton", Toast.LENGTH_SHORT).show();
                }

            }
        } catch (DataStore.ObjectNotFoundException e) {
            Toast.makeText(context, "Error " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }
}
