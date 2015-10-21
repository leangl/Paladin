package com.nanospark.gard;

import java.util.Calendar;
import java.util.Locale;

import mobi.tattu.utils.StringUtils;

/**
 * Created by cristian on 11/10/15.
 */
public class Utils {



    public static StringBuilder getDateLog(Calendar calendar,boolean whitSeparator){
        String dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK,Calendar.SHORT, Locale.US);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        StringBuilder builder = new StringBuilder();
        if(whitSeparator){
            builder.append("<b>");
        }
        builder.append(dayOfWeek);
        builder.append(StringUtils.SPACE);
        builder.append(dayOfMonth);
        if(whitSeparator){
            builder.append(StringUtils.COMMA);
            builder.append("</b>");
        }
        builder.append(StringUtils.SPACE);
        builder.append(getHour(calendar));
        return builder;
    }
    public  static String getHour(Calendar calendar){
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        StringBuilder builder = new StringBuilder();
        builder.append(hour > 9 ? hour : "0" + hour );
        builder.append(StringUtils.SEPARATOR_HOUR);
        builder.append(minute > 9 ? minute : "0" + minute);
        builder.append(StringUtils.SPACE);
        builder.append(calendar.getDisplayName(Calendar.AM_PM,Calendar.SHORT,Locale.US));
        return builder.toString();
    }

}
