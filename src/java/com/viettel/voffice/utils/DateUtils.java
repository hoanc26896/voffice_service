/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

/**
 *
 * @author thienng1
 */
public class DateUtils {
    /**
     *
     * @param value Date
     * @return String
     */
    public static String date2ddMMyyyyString(Date value) {
        if (value != null) {
            SimpleDateFormat ddMMyyyy = new SimpleDateFormat("dd/MM/yyyy");
            return ddMMyyyy.format(value);
        }
        return "";
    }
    
    public static String date2ddMMyyyyHHmmssString(Date value) {
        if (value != null) {
            SimpleDateFormat ddMMyyyy = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            return ddMMyyyy.format(value);
        }
        return "";
    }
    
    public static Date string2Date(String value) throws ParseException {
        if (!isNullOrEmpty(value)) {
            SimpleDateFormat dateTime = new SimpleDateFormat(
                    "dd/MM/yyyy");
            return dateTime.parse(value);
        }
        return null;
    }
    public static Date stringToDate(String value) throws ParseException {
        try{
            if (!isNullOrEmpty(value)) {
                SimpleDateFormat dateTime = new SimpleDateFormat(
                        "yyyyMMdd");
                return dateTime.parse(value);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String getLastDayOfTheMonth(String date) {
        String lastDayOfTheMonth = "";

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        try{
            java.util.Date dt= formatter.parse(date);
            Calendar calendar = Calendar.getInstance();  
            calendar.setTime(dt);  

            calendar.add(Calendar.MONTH, 1);  
            calendar.set(Calendar.DAY_OF_MONTH, 1);  
            calendar.add(Calendar.DATE, -1);  

            java.util.Date lastDay = calendar.getTime();  

            lastDayOfTheMonth = formatter.format(lastDay);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return lastDayOfTheMonth;
    }
    /**
     *
     * @return Date
     */
    public static Date sysDate() {
        return new Date();
    }
    
    public static boolean isNullOrEmpty(String obj1) {
        return (obj1 == null || "".equals(obj1.trim()));
    }

    public static boolean isStringNullOrEmpty(Object obj1) {
        return obj1 == null || "".equals(obj1.toString().trim());
    }

    public static boolean isListNullOrEmpty(List<?> lst) {
        return lst == null || lst.isEmpty();
    }
    /**
     * pm1_os20 format Date 2 ddMMyyyy String
     * 
     * @param value Date
     * @return String
     */
    public static String formatddMMyyyyString(String value) {
        if (!isNullOrEmpty(value)) {            
            return date2ddMMyyyyString(new Date(value));
        }
        return null;
    }
    
    /**
     * pm1_os20 format Date 2 ddMMyyyyHHmmss String
     * 
     * @param value Date
     * @return String
     */
    public static String formatddMMyyyyHHmmssString(String value) {
        if (!isNullOrEmpty(value)) {            
            return date2ddMMyyyyHHmmssString(new Date(value));
        }
        return null;
    }
    
    public static Date truncDate(Date date) {
        if (date == null) {
            return null;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }
    public static boolean inWeek(Date date) {
        if (date == null) {
            return false;
        }
        Date startDay = getMondayThisWeek();
        Date lastDay = getSundayThisWeek();
        if (startDay.after(date)) {
            return false;
        }
        if (lastDay.before(date)) {
            return false;
        }
        return true;
    }
    
    public static Date getMondayThisWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return new Date(calendar.getTimeInMillis());
    }

    public static Date getSundayThisWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return new Date(calendar.getTimeInMillis());
    }
    
    public static Date toDate(String s, String format) {
        if (CommonUtils.isEmpty(s)) {
            return null;
        }
        try {
            SimpleDateFormat df = new SimpleDateFormat(format);
            return df.parse(s);
        } catch (ParseException e) {
            Logger.getLogger(ConverterUtil.class).error(e);
            return null;
        }
    }
    
    public static String toString(Date date, String format) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }
    
    public static Date stringToDate(String date, String format) {
        try {
            if (date == null) {
                return null;
            }
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.parse(date);
        } catch (ParseException e) {
            Logger.getLogger(ConverterUtil.class).error(e);
        }
        return null;
    }
    
    public static int compareDate(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        if(date1 != null){
        	cal1.setTime(date1);
        }
        Calendar cal2 = Calendar.getInstance();
        if(date2 != null){
        	cal2.setTime(date2);
        }
        int year1 = cal1.get(Calendar.YEAR);
        int year2 = cal2.get(Calendar.YEAR);
        int month1 = cal1.get(Calendar.MONTH);
        int month2 = cal2.get(Calendar.MONTH);
        int day1 = cal1.get(Calendar.DAY_OF_MONTH);
        int day2 = cal2.get(Calendar.DAY_OF_MONTH);
        if (year1 > year2) {
            return 1;
        } else if (year1 < year2) {
            return -1;
        }
        if (month1 > month2) {
            return 1;
        } else if (month1 < month2) {
            return -1;
        }
        if (day1 > day2) {
            return 1;
        } else if (day1 < day2) {
            return -1;
        }
        return 0;
    }
    
    public static String concatUtcPlace(String place) {
        if (CommonUtils.isEmpty(place)) {
            return "UTC+07:00 - Bangkok, Hanoi, Jakarta";
        }
        return place.replaceFirst("\\(", "").replaceFirst("\\)", " -");
    }
    
    public static String getStringDayMonth(Date date) {
        String sMonth;
        String sDay = "";
        if (date == null) {
            return sDay;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        if (day < 10) {
            sDay = "0" + day;
        } else {
            sDay = day + "";
        }
        cal.setTime(date);
        int month = cal.get(Calendar.MONTH) + 1;
        if (month < 10) {
            sMonth = "0" + month;
        } else {
            sMonth = month + "";
        }
        return sDay + sMonth;
    }
    
    public static int getHour(Date date) {
        if (date == null) {
            return 0;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        return hour;
    }

    public static int getMinute(Date date) {
        if (date == null) {
            return 0;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int minute = cal.get(Calendar.MINUTE);
        return minute;
    }
    
    public static int getBetweenTwoDay(Date startDate, Date endDate) {
    	if (startDate != null && endDate != null) {
    		long difference = DateUtils.truncDate(endDate).getTime() - DateUtils.truncDate(startDate).getTime();
    		float daysBetween = (difference / (1000*60*60*24)) + 1;
    		return (int) daysBetween;
    	}
    	return 0;
    }

    public static Date strinToDateTime(String value) throws ParseException {
        if (!isNullOrEmpty(value)) {
            SimpleDateFormat dateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            return dateTime.parse(value);
        }
        return null;
    }
    
    public static String convertDateSmartRoom(Date value) {
        if (value != null) {
            SimpleDateFormat str = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
            String result = str.format(value);
            if (result != null && result.contains("ICT")) {
                result = result.replaceAll("ICT", "GMT");
            }
            return result;
        }
        return "";
    }
}
