package com.nanospark.gard.model.scheduler;

import com.nanospark.gard.model.Day;
import com.nanospark.gard.model.door.Door;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import mobi.tattu.utils.ResourceUtils;

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
    private Repeat repeat = Repeat.DAILY;
    private List<Day> days = Arrays.asList(Day.values()); // All days by default

    public Schedule() {
        this.id = UUID.randomUUID().toString();
        createDate = System.currentTimeMillis();
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

    public List<Day> getDays() {
        return days;
    }
    public void setDays(List<Day> days) {
        this.days = days;
    }
    public Repeat getRepeat() {
        return repeat;
    }
    public void setRepeat(Repeat repeat) {
        this.repeat = repeat;
    }

    public boolean isOpenTimeSet() {
        return openMinute != null && openHour != null;
    }

    public boolean isCloseTimeSet() {
        return closeMinute != null && closeHour != null;
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
    public int compareTo(Schedule another) {
        return this.createDate.compareTo(another.createDate);
    }

    public enum Repeat {
        DAILY, WEEKLY;

        @Override
        public String toString() {
            return ResourceUtils.toString(this);
        }
    }


    public boolean isNow(Integer scheduleHour, Integer scheduleMinute) {
        if (scheduleHour == null || scheduleMinute == null) return false;

        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        return days.contains(Day.fromCalendar(day)) && scheduleHour == hour && scheduleMinute == minute;
    }

    public boolean trigger() {
        if (isNow(openHour, openMinute)) {
            for (Integer doorId : doors) {
                return Door.getInstance(doorId).send(new Door.Open("Scheduled action taken, door is in motion", false));
            }
        } else if (isNow(closeHour, closeMinute)) {
            for (Integer doorId : doors) {
                return Door.getInstance(doorId).send(new Door.Close("Scheduled action taken, door is in motion", false));
            }
        }
        return false;
    }
}
