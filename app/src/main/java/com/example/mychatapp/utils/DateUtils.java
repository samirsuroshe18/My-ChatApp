package com.example.mychatapp.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    public static String getDateText(long timestamp) {
        Calendar messageCalendar = Calendar.getInstance();
        messageCalendar.setTimeInMillis(timestamp);

        Calendar todayCalendar = Calendar.getInstance();

        // Check if it's today
        if (isSameDay(messageCalendar, todayCalendar)) {
            return "Today";
        }

        // Check if it's yesterday
        todayCalendar.add(Calendar.DAY_OF_YEAR, -1);
        if (isSameDay(messageCalendar, todayCalendar)) {
            return "Yesterday";
        }

        // Return formatted date
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private static boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isDifferentDay(long timestamp1, long timestamp2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(timestamp1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(timestamp2);

        return !isSameDay(cal1, cal2);
    }
}