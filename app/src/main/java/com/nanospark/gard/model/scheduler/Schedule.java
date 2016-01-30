package com.nanospark.gard.model.scheduler;

import com.nanospark.gard.Utils;
import com.nanospark.gard.model.CommandSource;
import com.nanospark.gard.model.door.Door;
import com.nanospark.gard.model.user.ControlSchedule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import ioio.lib.spi.Log;

/**
 * Created by Leandro on 29/10/2015.
 */
public class Schedule implements Serializable, Comparable<Schedule> {

    private String id;
    private String name;
    private Long createDate;
    private List<Integer> doors = new ArrayList<>();
    private Integer openHour;
    private Integer openMinute;
    private Integer closeHour;
    private Integer closeMinute;
    private ControlSchedule controlSchedule;

    public Schedule() {
        this.id = UUID.randomUUID().toString();
        createDate = System.currentTimeMillis();
        controlSchedule = new ControlSchedule();
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Long getCreateDate() {
        return createDate;
    }
    public void setCreateDate(Long createDate) {
        this.createDate = createDate;
    }
    public List<Integer> getDoors() {
        return doors;
    }
    public void setDoors(List<Integer> doors) {
        this.doors = doors;
    }
    public Integer getCloseHour() {
        return closeHour;
    }
    public void setCloseHour(Integer closeHour) {
        this.closeHour = closeHour;
    }
    public Integer getCloseMinute() {
        return closeMinute;
    }
    public void setCloseMinute(Integer closeMinute) {
        this.closeMinute = closeMinute;
    }
    public Integer getOpenHour() {
        return openHour;
    }
    public void setOpenHour(Integer openHour) {
        this.openHour = openHour;
    }
    public Integer getOpenMinute() {
        return openMinute;
    }
    public void setOpenMinute(Integer openMinute) {
        this.openMinute = openMinute;
    }
    public ControlSchedule getControlSchedule() {
        return controlSchedule;
    }
    public void setControlSchedule(ControlSchedule controlSchedule) {
        this.controlSchedule = controlSchedule;
    }

    public boolean isOpenTimeSet() {
        return openMinute != null && openHour != null;
    }

    public boolean isCloseTimeSet() {
        return closeMinute != null && closeHour != null;
    }

    public boolean isNow(Integer scheduleHour, Integer scheduleMinute) {
        if (scheduleHour == null || scheduleMinute == null) return false;

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        return hour == scheduleHour && minute == scheduleMinute && getControlSchedule().isAllowed();
    }

    public void trigger() {
        Door.Command command = null;
        if (isNow(openHour, openMinute)) {
            command = new Door.Open(CommandSource.SCHEDULED_ACTION, "Scheduled action taken, door is in motion", false);
        } else if (isNow(closeHour, closeMinute)) {
            command = new Door.Close(CommandSource.SCHEDULED_ACTION, "Scheduled action taken, door is in motion", false);
        }
        if (command != null) {
            for (Integer doorId : doors) {
                Door door = Door.getInstance(doorId);
                if (door.send(command)) {
                    Log.i("Scheduler", "Schedule " + this + " triggered on " + door);
                } else {
                    Log.i("Scheduler", "Schedule " + this + " NOT triggered on " + door);
                }
            }
        } else {
            Log.i("Scheduler", "Schedule " + this + " NOT triggered (not allowed?)");
        }
    }

    public Calendar getOpenTime() {
        return isOpenTimeSet() ? Utils.createCalendarTime(openHour, openMinute) : null;
    }

    public Calendar getCloseTime() {
        return isCloseTimeSet() ? Utils.createCalendarTime(closeHour, closeMinute) : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Schedule schedule = (Schedule) o;

        return id.equals(schedule.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(Schedule another) {
        return this.createDate.compareTo(another.createDate);
    }

}
