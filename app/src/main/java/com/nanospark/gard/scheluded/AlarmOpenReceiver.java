package com.nanospark.gard.scheluded;

import com.nanospark.gard.events.DoorToggled;

import mobi.tattu.utils.Tattu;

public class AlarmOpenReceiver extends BaseAlarmReceiver {
    public AlarmOpenReceiver() {
    }

    @Override
    public void launcherEvent() {
        Tattu.post(new DoorToggled(Boolean.TRUE));

    }


}
