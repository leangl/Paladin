package com.nanospark.gard.scheluded;

import com.nanospark.gard.events.DoorState;

public class AlarmOpenReceiver extends BaseAlarmReceiver {

    public AlarmOpenReceiver() {
    }

    @Override
    public void launcherEvent() {
        DoorState.getInstance().open();
    }

}
