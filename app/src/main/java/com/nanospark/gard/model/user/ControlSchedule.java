package com.nanospark.gard.model.user;

import com.nanospark.gard.Utils;
import com.nanospark.gard.model.Day;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import roboguice.util.Ln;

/**
 * Created by Leandro on 1/10/2015.
 */
public class ControlSchedule implements Serializable {

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
    private List<Day> days = new ArrayList<>(Arrays.asList(Day.values()));
    private boolean repeatEveryOtherWeek;
    private boolean repeatWeeks;
    private Integer repeatWeeksNumber;
    private long createTimestamp;
    private Integer triggeredEvents = 0;

    public ControlSchedule() {
        this.createTimestamp = System.currentTimeMillis();
    }

    public List<Day> getDays() {
        return days;
    }
    public void setDays(List<Day> days) {
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
    public long getCreateTimestamp() {
        return createTimestamp;
    }
    public void setCreateTimestamp(long createTimestamp) {
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
        if (repeatEveryOtherWeek || (repeatWeeks && repeatWeeksNumber != null && repeatWeeksNumber == 2)) {
            sb.append("Every other ");
        } else if ((repeatWeeks && repeatWeeksNumber != null && repeatWeeksNumber > 2)) {
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
        for (Day day : days) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(day.abbr());
        }
        return sb.toString();
    }

    public boolean isAllowed() {
        Calendar today = Calendar.getInstance();
        if (isStartTimeSet()) {
            if (today.before(getStartTime())) {
                Ln.i("Today is before start time " + getStartTime());
                return false;
            }
        }
        if (isEndTimeSet()) {
            if (today.after(getEndTime())) {
                Ln.i("Today is after end time " + getEndTime());
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
                Ln.i("Today is before start date " + startYear + "/" + startMonth + "/" + startDay);
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
                Ln.i("Today is after end date " + limitYear + "/" + limitMonth + "/" + limitDay);
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

        if (days != null && !days.isEmpty() && !days.contains(Day.today())) {
            Ln.i("Day not allowed: " + Day.today().name());
            return false;
        }

        Ln.i("Allowed timeframe");
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

    public Calendar getStartDate() {
        return isStartDateSet() ? Utils.createCalendarDate(startYear, startMonth, startDay) : null;
    }

    public Calendar getEndDate() {
        return isEndDateSet() ? Utils.createCalendarDate(limitYear, limitMonth, limitDay) : null;
    }

    public Calendar getStartTime() {
        if (!isStartTimeSet()) return null;

        Calendar startTime = Utils.createCalendarTime(startHour, startMinute);

        if (isEndTimeSet()) {
            Calendar endTime = Utils.createCalendarTime(endHour, endMinute);
            if (endTime.before(startTime)) {
                Calendar today = Calendar.getInstance();
                if (today.before(endTime)) {
                    startTime.add(Calendar.DAY_OF_MONTH, -1);
                }
            }
        }

        return startTime;
    }

    public Calendar getEndTime() {
        if (!isEndTimeSet()) return null;

        Calendar endTime = Utils.createCalendarTime(endHour, endMinute);

        if (isStartTimeSet()) {
            Calendar startTime = Utils.createCalendarTime(startHour, startMinute);
            if (endTime.before(startTime)) {
                Calendar today = Calendar.getInstance();
                if (today.after(endTime)) {
                    endTime.add(Calendar.DAY_OF_MONTH, 1);
                }
            }
        }

        return endTime;
    }

}
