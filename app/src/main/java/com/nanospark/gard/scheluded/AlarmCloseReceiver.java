package com.nanospark.gard.scheluded;

import com.nanospark.gard.events.DoorToggled;

import mobi.tattu.utils.Tattu;

public class AlarmCloseReceiver extends BaseAlarmReceiver {
    public AlarmCloseReceiver() {
    }

    @Override
    public void launcherEvent() {
        Tattu.post(new DoorToggled(Boolean.FALSE));

    }


}
