/*
 * DateTimeUtils.java
 *
 * Created on August 6, 2007, 3:37 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.viettel.voffice.utils;

import com.viettel.voffice.database.dao.common.CommonDataBaseDaoVO1;
import com.viettel.voffice.database.entity.EntityTimeConfig;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.springframework.dao.DataAccessException;

/**
 *
 * @author Vu Thi Thu Huong
 */
public class DateTimeUtils {

//    final private static SimpleDateFormat RFC3339_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Creates a new instance of DateTimeUtils
     */
    public DateTimeUtils() {
    }

    public static Date getNextNDayOfDate(Date input, Long nDay) {
        Calendar objCal = Calendar.getInstance();
        objCal.setTime(input);
//        objCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        objCal.add(Calendar.DAY_OF_MONTH, nDay.intValue());
        return objCal.getTime();
    }
    // vinhnq13 21/07/2014 Lay ngay truoc do

    public static Date getPreviousNDayOfDate(Date input, Long nDay) {
        Calendar objCal = Calendar.getInstance();
        objCal.setTime(input);
        objCal.add(Calendar.DAY_OF_MONTH, -nDay.intValue());
        return objCal.getTime();
    }

    public static Date convertStringToTime(String date, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        dateFormat.setLenient(false);
        try {
            return dateFormat.parse(date);

        } catch (ParseException e) {
//            System.out.println("Date ParseException, string value:" + date);
            return null;
        }
    }

    public static Date convertStringToDate(String date) throws Exception {
        String pattern = "dd/MM/yyyy";
        return convertStringToTime(date, pattern);
    }

//    public static Date convertStringToDateTime(String date) throws Exception
//    {
//        String pattern = "dd/MM/yyyy hh24:mi:ss";
//        return convertStringToTime(date, pattern);
//    }
    public static String convertDateToString(Date date) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        try {
            if (date != null) {
                return dateFormat.format(date);
            } else {
                return "";
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public static String convertDateToString(Date date, String format) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        try {
            if (date != null) {
                return dateFormat.format(date);
            } else {
                return "";
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public static String convertDateTimePickerToString(Date date) throws Exception {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        if (date == null) {
            return "";
        }
        try {
            return dateFormat.format(date);
        } catch (Exception e) {
            throw e;
        }
    }

    /*
     *  @author: dungnt
     *  @todo: get sysdate
     *  @return: String sysdate
     */
    public static String getSysdate() throws Exception {
        Calendar calendar = Calendar.getInstance();
        return convertDateToString(calendar.getTime());
    }

    public static String getSysdatePicker() throws Exception {
        Calendar calendar = Calendar.getInstance();
        return convertDateTimePickerToString(calendar.getTime());
    }

    public static Date getDate() throws Exception {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }

    /*
     *  @author: dungnt
     *  @todo: get sysdate detail
     *  @return: String sysdate
     */
    public static String getSysDateTime() throws Exception {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try {
            return dateFormat.format(calendar.getTime());
        } catch (Exception e) {
            throw e;
        }
    }

    /*
     *  @author: dungnt
     *  @todo: convert from String to DateTime detail
     *  @param: String date
     *  @return: Date
     */
    public static Date convertStringToDateTime(String date) throws Exception {
        String pattern = "dd/MM/yyyy HH:mm:ss";
        return convertStringToTime(date, pattern);
    }

    public static Date convertDateTimeStringToDateTime(String date) throws Exception {
        String pattern = "dd/MM/yyyy HH:mm";
        return convertStringToTime(date, pattern);
    }

    public static Date convertStringToDateTimePicker(String date) throws Exception {
        String pattern = "yyyy-MM-dd HH:mm:ss";
        return convertStringToTime(date, pattern);
    }

    public static String convertDateTimeToString(Date date) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try {
            return dateFormat.format(date);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Convert from date time to string
     *
     * @author CongLT
     * @param date
     * @param pattern
     * @return
     * @throws java.lang.Exception
     */
    public static String convertTimeToString(Date date, String pattern) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        try {
            return dateFormat.format(date);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * get first day of next week
     *
     * @author Namld
     * @param Date
     * @return Date
     */
    public static Date getNextFirstDayOfWeek(Date input) {
        Calendar objCal = Calendar.getInstance();
        objCal.setTime(input);
        objCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        objCal.set(Calendar.HOUR_OF_DAY, 0);
        objCal.set(Calendar.MINUTE, 0);
        objCal.set(Calendar.SECOND, 0);
        objCal.add(Calendar.DAY_OF_YEAR, 7);
        return objCal.getTime();
    }

    /**
     * get last day of next week
     *
     * @author Namld
     * @param Date
     * @return Date
     */
    public static Date getNextLastDayOfWeek(Date input) {
        Calendar objCal = Calendar.getInstance();
        objCal.setTime(input);
        objCal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        objCal.set(Calendar.HOUR_OF_DAY, 23);
        objCal.set(Calendar.MINUTE, 59);
        objCal.set(Calendar.SECOND, 59);
        objCal.add(Calendar.DAY_OF_YEAR, 7);
        return objCal.getTime();
    }

    /**
     * get this day of next week
     *
     * @author Namld
     * @param Date
     * @return Date
     */
    public static Date getNextDayOfWeek(Date input) {
        Calendar objCal = Calendar.getInstance();
        objCal.setTime(input);
        objCal.add(Calendar.DAY_OF_YEAR, 7);
        return objCal.getTime();
    }

//    public static Date convertDateRFC3339FromString(String strDate) {
//        Date result = new Date();
//        RFC3339_FORMAT.setLenient(false);
//        try {
//            return RFC3339_FORMAT.parse(strDate);
//
//        } catch (ParseException e) {
////            System.out.println("Date ParseException, string value:" + strDate);
//        }
//        return result;
//    }

    public static Date convertStringDDMMYYYYHHMMToDateTime(String date) throws Exception {
        return DateTimeUtils.convertStringToDateTime(date + ":00");
    }

    /**
     * Lay ngay dau tien cua thang hien tai
     *
     * @return
     */
    public static Date getFirstDateOfCurrentMonth() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new java.util.Date());
        cal.set(Calendar.DATE, 1); //set the date to start of
        return cal.getTime();
    }

    /**
     * Lay ngay cuoi cung cua thang hien tai
     *
     * @return
     */
    public static Date getLastDateOfCurrentMonth() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new java.util.Date());
        cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DAY_OF_MONTH)); //set the date to start of
        return cal.getTime();
    }

    public static int getDayOfWeek() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.DAY_OF_WEEK);
    }

    /**
     * Lay ngay am vo cung
     */
    public static Date getOldestDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 1900);
        return cal.getTime();
    }

    /**
     * Lay ngay tuong lai vo cung
     */
    public static Date getFarestDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2200);
        return cal.getTime();
    }

    /**
     * con vert Date tim sang gio viet nam hanhnq21@viettel.com.vn
     * @param date
     * @return
     * @throws Exception
     */
    public static String convertDateTimeToStringVN(Date date) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
        try {
            return dateFormat.format(date);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * hanhnq21@viettel.com.vn convert sysdate co ca milisecond
     * @return
     * @throws Exception
     * @since 12/03/2015
     */
    public static String getSysDateTimeAddMil() throws Exception {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS");
        try {
            return dateFormat.format(calendar.getTime());
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * hiendv2 Ham lay thoi gian hien tai cua DB
     *
     * @return
     * @throws Exception
     * @since 16/12/2015
     */
    public static String getSysdateFromDB(CommonDataBaseDaoVO1 cmd) throws DataAccessException {
        StringBuilder strBuilder = new StringBuilder();
        String currentDate = "";
        strBuilder.append("SELECT TO_CHAR(SYSDATE, 'dd/MM/yyyy hh24:mi:ss') as currentDate FROM DUAL");
        List<EntityTimeConfig> listResult = (List<EntityTimeConfig>) cmd.excuteSqlGetListObjOnCondition(strBuilder, null, null, null, EntityTimeConfig.class);
        if (listResult != null && listResult.size() > 0) {
            currentDate = listResult.get(0).getCurrentDate();
        }
        return currentDate;
    }

    public static Date getWorkingDaysAfterDate(Date input, int numDate) {
        
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(input);
        int c = 0;
        if (startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY
                && startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            numDate = numDate + 1;
        }
        do {
            if (startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY
                    && startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                c++;
            }
            startCal.add(Calendar.DAY_OF_MONTH, 1);
        } while (c < numDate);

        while (startCal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
                || startCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            startCal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return startCal.getTime();
    }
    
    /**
     * @author TungHD
     * convertDateToStringMark
     * convert Datetime to string for markDate
     * @param date
     * @return
     * @throws Exception
     */
    public static String convertDateToStringMark(Date date) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try {
            if (date != null) {
                return dateFormat.format(date);
            } else {
                return "";
            }
        } catch (Exception e) {
            throw e;
        }
    }
}
