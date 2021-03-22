package com.viettel.voffice.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TimeZoneUtil
 * 
 * @author CHU QUANG VI
 */
public class TimeZoneUtil {

    final private static String ROOT_TIMEZONE = "Asia/Bangkok";
    final private static String TIMEZONE_PATTERN = "dd/MM/yyyy HH:mm";

    /**
     * CONVERT 1 DATE TU THOI GIAN DIA PHUONG VE THOI GIAN ROOT
     */
    public static Date convertToRootTimezone(Date localDate, String localTimeZone) {
        Date result = null;
        if (localDate != null) {

            String orgTime = null;
            try {
                orgTime = DateUtils.toString(localDate, TIMEZONE_PATTERN);
                // orgTime = convertDateToString(localDate, TIMEZONE_PATTERN);
            } catch (Exception ex) {
                Logger.getLogger(TimeZoneUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
            String desTime = convTimeZone(orgTime, localTimeZone, ROOT_TIMEZONE, TIMEZONE_PATTERN);
            result = DateUtils.toDate(desTime, TIMEZONE_PATTERN);
        }
        return result;
    }

    public static String convertToRootTimezone(String localDate, String localTimeZone) {
        String result = null;
        if (localDate != null) {
            result = convTimeZone(localDate, localTimeZone, ROOT_TIMEZONE, TIMEZONE_PATTERN);
        }
        return result;
    }

    /**
     * CONVERT 1 DATE TU THOI GIAN ROOT VE THOI GIAN DIA PHUONG
     */
    public static Date convertToLocalTimezone(Date rootDate, String localTimeZone) {
        Date result = null;
        if (rootDate != null) {
            String orgTime = null;
            try {
                orgTime = DateUtils.toString(rootDate, TIMEZONE_PATTERN);
            } catch (Exception ex) {
                Logger.getLogger(TimeZoneUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
            String desTime = convTimeZone(orgTime, ROOT_TIMEZONE, localTimeZone, TIMEZONE_PATTERN);
            result = DateUtils.toDate(desTime, TIMEZONE_PATTERN);
        }
        return result;
    }

    /**
     * CONVERT 1 DATE TU THOI GIAN ROOT VE THOI GIAN DIA PHUONG
     */
    public static String convertToLocalTimezone(String rootDate, String localTimeZone) {
        String result = null;
        if (rootDate != null) {
            result = convTimeZone(rootDate, ROOT_TIMEZONE, localTimeZone, TIMEZONE_PATTERN);
        }
        return result;
    }

    private static String convTimeZone(String time, String sourceTZ, String destTZ, String partern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(partern);
        Date specifiedTime = new Date();
        try {
            if (sourceTZ != null) {
                dateFormat.setTimeZone(TimeZone.getTimeZone(sourceTZ));
            } else {
                dateFormat.setTimeZone(TimeZone.getTimeZone(ROOT_TIMEZONE)); // default to server's timezone
            }
            specifiedTime = dateFormat.parse(time);
        } catch (Exception e1) {
            Logger.getLogger(TimeZoneUtil.class.getName()).log(Level.SEVERE, null, e1);
            return time;
        }

        // switch timezone
        if (destTZ != null) {
            dateFormat.setTimeZone(TimeZone.getTimeZone(destTZ));
        } else {
            dateFormat.setTimeZone(TimeZone.getTimeZone(ROOT_TIMEZONE)); // default to server's timezone
        }
        return dateFormat.format(specifiedTime);
    }
}
