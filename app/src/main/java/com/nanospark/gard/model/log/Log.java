package com.nanospark.gard.model.log;

import android.text.Html;

import com.nanospark.gard.events.DoorStateChanged;
import com.nanospark.gard.model.door.Door;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Leandro on 1/10/2015.
 */
public class Log implements Serializable {

    private int doorId;
    private Door.State event;
    private Date date;

    public Log() {
    }

    public Log(DoorStateChanged event) {
        this.doorId = event.door.getId();
        this.event = event.state;
        this.date = new Date();
    }

    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    public int getDoorId() {
        return doorId;
    }
    public void setDoorId(int doorId) {
        this.doorId = doorId;
    }
    public Door.State getEvent() {
        return event;
    }
    public void setEvent(Door.State event) {
        this.event = event;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Log log = (Log) o;

        if (doorId != log.doorId) return false;
        if (!event.equals(log.event)) return false;
        return date.equals(log.date);

    }
    @Override
    public int hashCode() {
        int result = doorId;
        result = 31 * result + event.hashCode();
        result = 31 * result + date.hashCode();
        return result;
    }

    public String getDateString(boolean withSeparator) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        String dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        StringBuilder builder = new StringBuilder();

        if (withSeparator) {
            builder.append("<b>");
        }
        builder.append(dayOfWeek);
        builder.append(" ");
        builder.append(dayOfMonth);
        builder.append(", ");
        if (withSeparator) {
            builder.append("</b>");
        }
        builder.append(com.nanospark.gard.Utils.getHour(calendar));

        return Html.fromHtml(builder.toString()).toString();
    }


}
