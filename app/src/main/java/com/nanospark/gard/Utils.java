package com.nanospark.gard;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by cristian on 11/10/15.
 */
public class Utils {

    public static final String SPACE = " ";
    public static final String COMMA = ",";
    public static final String SEPARATOR_HOUR = ":";
    public static final String NEW_LINE_FILE = "\r\n";

    public static StringBuilder getDateLog(Calendar calendar,boolean whitSeparator){
        String dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK,Calendar.SHORT, Locale.US);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        StringBuilder builder = new StringBuilder();
        if(whitSeparator){
            builder.append("<b>");
        }
        builder.append(dayOfWeek);
        builder.append(SPACE);
        builder.append(dayOfMonth);
        if(whitSeparator){
            builder.append(COMMA);
            builder.append("</b>");
        }
        builder.append(SPACE);
        builder.append(getHour(calendar));
        return builder;
    }
    private static String getHour(Calendar calendar){
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        StringBuilder builder = new StringBuilder();
        builder.append(hour > 9 ? hour : "0" + hour );
        builder.append(SEPARATOR_HOUR);
        builder.append(minute > 9 ? minute : "0" + minute);
        return builder.toString();
    }

}
