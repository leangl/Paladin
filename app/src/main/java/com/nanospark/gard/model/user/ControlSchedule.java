package com.nanospark.gard.model.user;

import java.util.List;

/**
 * Created by Leandro on 1/10/2015.
 */
public class ControlSchedule {

    private Integer startMinute;
    private Integer startHour;
    private Integer endMinute;
    private Integer endHour;
    private Integer startDay;
    private Integer startMonth;
    private Integer startYear;
    private Limit limit;
    private Integer limitDay;
    private Integer limitMonth;
    private Integer limitYear;
    private Integer limitEvents;
    private List<Integer> days;
    private boolean repeatEveryOtherDay;
    private boolean repeatWeeks;
    private Integer repeatWeeksNumber;

    public List<Integer> getDays() {
        return days;
    }
    public void setDays(List<Integer> days) {
        this.days = days;
    }
    public Integer getEndHour() {
        return endHour;
    }
    public void setEndHour(Integer endHour) {
        this.endHour = endHour;
    }
    public Integer getEndMinute() {
        return endMinute;
    }
    public void setEndMinute(Integer endMinute) {
        this.endMinute = endMinute;
    }
    public Limit getLimit() {
        return limit;
    }
    public void setLimit(Limit limit) {
        this.limit = limit;
    }
    public Integer getLimitDay() {
        return limitDay;
    }
    public void setLimitDay(Integer limitDay) {
        this.limitDay = limitDay;
    }
    public Integer getLimitEvents() {
        return limitEvents;
    }
    public void setLimitEvents(Integer limitEvents) {
        this.limitEvents = limitEvents;
    }
    public Integer getLimitMonth() {
        return limitMonth;
    }
    public void setLimitMonth(Integer limitMonth) {
        this.limitMonth = limitMonth;
    }
    public Integer getLimitYear() {
        return limitYear;
    }
    public void setLimitYear(Integer limitYear) {
        this.limitYear = limitYear;
    }
    public boolean isRepeatEveryOtherDay() {
        return repeatEveryOtherDay;
    }
    public void setRepeatEveryOtherDay(boolean repeatEveryOtherDay) {
        this.repeatEveryOtherDay = repeatEveryOtherDay;
    }
    public boolean isRepeatWeeks() {
        return repeatWeeks;
    }
    public void setRepeatWeeks(boolean repeatWeeks) {
        this.repeatWeeks = repeatWeeks;
    }
    public Integer getRepeatWeeksNumber() {
        return repeatWeeksNumber;
    }
    public void setRepeatWeeksNumber(Integer repeatWeeksNumber) {
        this.repeatWeeksNumber = repeatWeeksNumber;
    }
    public Integer getStartDay() {
        return startDay;
    }
    public void setStartDay(Integer startDay) {
        this.startDay = startDay;
    }
    public Integer getStartHour() {
        return startHour;
    }
    public void setStartHour(Integer startHour) {
        this.startHour = startHour;
    }
    public Integer getStartMinute() {
        return startMinute;
    }
    public void setStartMinute(Integer startMinute) {
        this.startMinute = startMinute;
    }
    public Integer getStartMonth() {
        return startMonth;
    }
    public void setStartMonth(Integer startMonth) {
        this.startMonth = startMonth;
    }
    public Integer getStartYear() {
        return startYear;
    }
    public void setStartYear(Integer startYear) {
        this.startYear = startYear;
    }

    @Override
    public String toString() {
        return "Monday and Tuesday, 8:00am - 10:00am, 2 times";
    }

    public enum Limit {
        FOREVER, DATE, EVENTS
    }
}
