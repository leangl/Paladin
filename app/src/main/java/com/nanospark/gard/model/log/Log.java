package com.nanospark.gard.model.log;

import com.nanospark.gard.events.CommandProcessed;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Leandro on 1/10/2015.
 */
public class Log implements Serializable {

    public static final String EVENT_OPEN = "open";
    public static final String EVENT_CLOSE = "close";

    private int doorId;
    private String event;
    private Date date;

    public Log() {
    }

    public Log(CommandProcessed event) {
        this.doorId = event.door.getId();
        this.event = event.command.isOpen() ? EVENT_OPEN : EVENT_CLOSE;
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
    public String getEvent() {
        return event;
    }
    public void setEvent(String event) {
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
}
