package com.nanospark.gard.model.scheduler;

import com.nanospark.gard.model.door.Door;

import java.util.Calendar;
import java.util.List;

/**
 * Created by cristian on 09/08/15.
 */
public class Schedule {

    public static String ACTION_OPEN_DOOR = "action_open_door";
    public static String ACTION_CLOSE_DOOR = "action_close_door";

    public String name;
    public String action;
    public List<Integer> days;
    public List<String> dayNameSelecteds;
    public int hourOfDay;
    public int minute;
    public long timeStamp;
    public int doorId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Schedule schedule = (Schedule) o;

        return name.equals(schedule.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public boolean isNow() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        return this.days.contains(day) && this.hourOfDay == hour && this.minute == minute;
    }

    public boolean trigger() {
        if (isNow()) {
            if (action.equals(ACTION_OPEN_DOOR)) {
                return Door.getInstance(doorId).send(new Door.Open("Scheduled action taken, door is in motion", false));
            } else if (action.equals(ACTION_CLOSE_DOOR)) {
                return Door.getInstance(doorId).send(new Door.Close("Scheduled action taken, door is in motion", false));
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return name;
    }
}
