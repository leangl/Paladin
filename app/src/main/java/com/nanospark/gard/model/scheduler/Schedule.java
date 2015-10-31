package com.nanospark.gard.model.scheduler;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Created by Leandro on 29/10/2015.
 */
public class Schedule implements Serializable, Comparable<Schedule> {

    private String id;
    private String name;
    private Long createDate;
    private List<Integer> doors;

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
}
