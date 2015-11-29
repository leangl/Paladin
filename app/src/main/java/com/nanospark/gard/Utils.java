package com.nanospark.gard;

import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

import mobi.tattu.utils.ToastManager;

/**
 * Created by cristian on 11/10/15.
 */
public class Utils {

    public static final String SPACE = " ";
    public static final String COMMA = ",";
    public static final String SEPARATOR_HOUR = ":";

    public static String getHour(int hour, int minute) {
        return getHour(createCalendarTime(hour, minute));
    }

    public static String getHour(Calendar calendar) {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        StringBuilder builder = new StringBuilder();
        builder.append(hour > 9 ? hour : "0" + hour);
        builder.append(SEPARATOR_HOUR);
        builder.append(minute > 9 ? minute : "0" + minute);
        builder.append(SPACE);
        builder.append(calendar.getDisplayName(Calendar.AM_PM, Calendar.SHORT, Locale.US));
        return builder.toString();
    }

    public static Calendar createCalendarTime(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    public static Calendar createCalendarDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        return calendar;
    }

    public static void saveLogcat() {
        final File path = new File(Environment.getExternalStorageDirectory(), "paladin_" + System.currentTimeMillis() + ".log");
        try {
            if (path.exists()) {
                path.delete();
            }
            Runtime.getRuntime().exec("logcat -d -f " + path);
            ToastManager.show("Log saved: " + path);
        } catch (IOException e) {
            ToastManager.show("Error saving log: " + path);
        }
    }


}
