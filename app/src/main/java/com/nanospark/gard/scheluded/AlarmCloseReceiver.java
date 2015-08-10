package com.nanospark.gard.scheluded;

import com.nanospark.gard.events.DoorState;

public class AlarmCloseReceiver extends BaseAlarmReceiver {

    public AlarmCloseReceiver() {
    }

    @Override
    public void launcherEvent() {
        DoorState.getInstance().close();
    }

}
