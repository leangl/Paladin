package com.nanospark.gard.scheluded;

import java.util.List;

/**
 * Created by cristian on 09/08/15.
 */
public class Scheluded {
    public String id;
    public String action;
    //Se puso un integer para comparar los dias con Calendar.SUNDAY
    public List<Integer> days;
    public List<String> dayNameSelecteds;
    public int hourOfDay;
    public int minute;
    //Es para indicarle si pertenece a scheluded one o two
    public String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Scheluded scheluded = (Scheluded) o;

        if (hourOfDay != scheluded.hourOfDay) return false;
        if (minute != scheluded.minute) return false;
        if (!id.equals(scheluded.id)) return false;
        if (!action.equals(scheluded.action)) return false;
        if (!days.equals(scheluded.days)) return false;
        return dayNameSelecteds.equals(scheluded.dayNameSelecteds);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + action.hashCode();
        result = 31 * result + days.hashCode();
        result = 31 * result + dayNameSelecteds.hashCode();
        result = 31 * result + hourOfDay;
        result = 31 * result + minute;
        return result;
    }
}
