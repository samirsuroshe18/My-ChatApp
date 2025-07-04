package com.example.mychatapp.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateFormatter {

    public static String formatMessageTime(Date messageDate) {
        Calendar msgCal = Calendar.getInstance();
        msgCal.setTime(messageDate);

        Calendar nowCal = Calendar.getInstance();

        // Same day
        if (isSameDay(msgCal, nowCal)) {
            return new SimpleDateFormat("h:mm a").format(messageDate);
        }

        // Yesterday
        nowCal.add(Calendar.DATE, -1);
        if (isSameDay(msgCal, nowCal)) {
            return "Yesterday";
        }

        // Same year
        nowCal = Calendar.getInstance(); // Reset
        if (msgCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)) {
            return new SimpleDateFormat("dd/MM").format(messageDate);
        }

        // Different year
        return new SimpleDateFormat("dd/MM/yyyy").format(messageDate);
    }

    private static boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}
