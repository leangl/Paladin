package com.nanospark.gard.model.user;

import com.nanospark.gard.model.Day;
import com.nanospark.gard.model.door.Door;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import roboguice.util.Ln;

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
    private boolean repeatEveryOtherWeek;
    private boolean repeatWeeks;
    private Integer repeatWeeksNumber;
    private Long createTimestamp;
    private Integer triggeredEvents;

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
    public boolean isRepeatEveryOtherWeek() {
        return repeatEveryOtherWeek;
    }
    public void setRepeatEveryOtherWeek(boolean repeatEveryOtherWeek) {
        this.repeatEveryOtherWeek = repeatEveryOtherWeek;
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
    public Long getCreateTimestamp() {
        return createTimestamp;
    }
    public void setCreateTimestamp(Long createTimestamp) {
        this.createTimestamp = createTimestamp;
    }
    public Integer getTriggeredEvents() {
        return triggeredEvents;
    }
    public void setTriggeredEvents(Integer triggeredEvents) {
        this.triggeredEvents = triggeredEvents;
    }
    public void incrementEvents() {
        setTriggeredEvents(getTriggeredEvents() + 1);
    }

    @Override
    public String toString() {
        return getHourRangeString() + "\n" + getDayLimitString();
    }

    public String getHourRangeString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getHourString(startHour)).append("-").append(getHourString(endHour));
        return sb.toString();
    }

    private String getHourString(Integer hour) {
        if (hour != null) {
            if (hour == 12) {
                return "12p";
            } else if (hour == 24) {
                return "0a";
            } else if (hour > 12) {
                return Math.abs(12 - hour) + "p";
            } else {
                return hour + "a";
            }
        } else {
            return "0a";
        }
    }

    public String getDayLimitString() {
        if (days == null || days.isEmpty() || days.size() == 7) {
            if (repeatWeeks || repeatEveryOtherWeek) {
                StringBuilder sb = new StringBuilder();
                if (repeatEveryOtherWeek || (repeatWeeks && repeatWeeksNumber == 2)) {
                    sb.append("Every other week");
                } else if ((repeatWeeks && repeatWeeksNumber > 2)) {
                    if (repeatWeeksNumber == 3) {
                        sb.append("Every 3rd week");
                    } else if (repeatWeeksNumber == 4) {
                        sb.append("Every 4th week");
                    } else {
                        sb.append("Every " + repeatWeeksNumber + " weeks");
                    }
                }
                return sb.toString();
            } else {
                return "Daily";
            }
        }
        StringBuilder sb = new StringBuilder();
        if (repeatEveryOtherWeek || (repeatWeeks && repeatWeeksNumber == 2)) {
            sb.append("Every other ");
        } else if ((repeatWeeks && repeatWeeksNumber > 2)) {
            if (repeatWeeksNumber == 3) {
                sb.append("Every 3rd ");
            } else if (repeatWeeksNumber == 4) {
                sb.append("Every 4th ");
            } else {
                sb.append("Every " + repeatWeeksNumber + " ");
            }
        }
        boolean first = true;
        Collections.sort(days);
        for (int day : days) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(Day.fromCalendar(day).abbr());
        }
        return sb.toString();
    }

    public boolean isAllowed(Door door) {
        Calendar today = Calendar.getInstance();
        if (isStartTimeSet()) {
            int minute = today.get(Calendar.MINUTE);
            int hour = today.get(Calendar.HOUR_OF_DAY);
            if (hour < startHour || (hour == startHour && minute < startMinute)) {
                Ln.i("Today is before end hour: HH:mm", hour, minute);
                return false;
            }
        }
        if (isEndTimeSet()) {
            int minute = today.get(Calendar.MINUTE);
            int hour = today.get(Calendar.HOUR_OF_DAY);
            if (hour > endHour || (hour == endHour && minute > endMinute)) {
                Ln.i("Today is after end hour: HH:mm", hour, minute);
                return false;
            }
        }

        if (isStartDateSet()) {
            int day = today.get(Calendar.DAY_OF_MONTH);
            int month = today.get(Calendar.MONTH);
            int year = today.get(Calendar.YEAR);
            if (year < startYear ||
                    (year == startYear &&
                            (month < startMonth || (month == startMonth && day < startDay)))) {
                Ln.i("Today is before start date: yyyy/MM/dd", year, month, day);
                return false;
            }
        }

        if (isEndDateSet()) {
            int day = today.get(Calendar.DAY_OF_MONTH);
            int month = today.get(Calendar.MONTH);
            int year = today.get(Calendar.YEAR);
            if (year > limitYear ||
                    (year == limitYear &&
                            (month > limitMonth || (month == limitMonth && day > limitDay)))) {
                Ln.i("Today is after end date: yyyy/MM/dd", year, month, day);
                return false;
            }
        }

        int skipWeeks = 1;
        if (repeatEveryOtherWeek) {
            skipWeeks = 2;
        } else if (repeatWeeks) {
            skipWeeks = repeatWeeksNumber;
        }

        Ln.i("Skipping weeks: " + skipWeeks);

        if (skipWeeks > 1) {
            Calendar start = Calendar.getInstance();
            start.setTime(new Date(createTimestamp));

            while (start.get(Calendar.WEEK_OF_YEAR) < today.get(Calendar.WEEK_OF_YEAR)) { // FIXME take year into account
                start.set(Calendar.WEEK_OF_YEAR, start.get(Calendar.WEEK_OF_YEAR) + skipWeeks);
            }
            if (start.get(Calendar.WEEK_OF_YEAR) != today.get(Calendar.WEEK_OF_YEAR)) {
                Ln.i("Week skipped");
                return false;
            }
        }

        if (Limit.EVENTS.equals(limit)) {
            if (triggeredEvents >= limitEvents) {
                Ln.i("Events limit exceeded: " + triggeredEvents);
                return false;
            }
        }

        if (days != null && !days.isEmpty() && !days.contains(today.get(Calendar.DAY_OF_WEEK))) {
            Ln.i("Day not allowed: " + today.get(Calendar.DAY_OF_WEEK));
            return false;
        }

        Ln.i("Allowed time.");
        return true;
    }

    public boolean isStartTimeSet() {
        return startMinute != null && startHour != null;
    }

    public boolean isStartDateSet() {
        return startDay != null && startMonth != null && startYear != null;
    }

    public boolean isEndTimeSet() {
        return endMinute != null && endHour != null;
    }

    public boolean isEndDateSet() {
        return Limit.DATE.equals(limit) && limitDay != null && limitMonth != null && limitYear != null;
    }

}
