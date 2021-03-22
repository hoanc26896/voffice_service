/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.utils;

import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.log.EntityActionLogMobile;
import com.viettel.voffice.thread.LogThread.ActionLogMobileThread;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;

/**
 *
 * @author vinhnq13
 */
public class LogUtils {

    private static final Logger logger = Logger.getLogger(LogUtils.class);

    // KPI Logger
    private static final Logger KPI_LOGGER = Logger.getLogger("kpiLogger");

    // Log chuc nang dang nhap
    public static final String LOGIN_LOG_TYPE = "Login";

    // Log bat dau chuc nang nghiep vu
    public static final String START_ACTION_LOG_TYPE = "start_action";

    // Log ket thuc chuc nang nghiep
    public static final String END_ACTION_LOG_TYPE = "end_action";

    // Ma ung dung
    private static final String APP_CODE = "VOFFICE";

    // Dau phan cach
    private static final String SEPARATOR = "|";

    // Dinh dang ngay
    private static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss:SSS";

    public static void writeLog(HttpServletRequest request, String rootAction,
            int actionCode, ErrorCode errorCode, String message, String data, String isSecurity) {

    }

    /**
     * <b>Ghi log xac thuc</b>
     *
     * @param log
     */
    public static void logAuthentication(EntityLog log) {
        
        try {
            // Format thoi gian thanh chuoi theo dinh dang
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            String strStartTime = sdf.format(log.getStartTime());

            // Khoi tao ban ghi
            StringBuilder record = new StringBuilder();
            record.append(log.getLogType()).append(SEPARATOR);
            record.append(strStartTime).append(SEPARATOR);
            record.append(APP_CODE).append(SEPARATOR);
            record.append(log.getUserName()).append(SEPARATOR);
            record.append(log.getTransactionTime()).append(SEPARATOR);
            record.append(log.getIpAddress()).append(SEPARATOR);
            record.append(log.getPath()).append(SEPARATOR);
            record.append(log.getDescription()).append(SEPARATOR);
            record.append(log.getDuration());

            // Ghi ra file
            KPI_LOGGER.info(record.toString());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void logLogin(EntityLog log) {
        
        try {
            // Thiet lap loai log
            log.setLogType(LOGIN_LOG_TYPE);
            // Tinh thoi gian thuc hien
            Date startTime = log.getStartTime();
            Date endTime = new Date();
            log.setDuration(endTime.getTime() - startTime.getTime());
            logAuthentication(log);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * <b>Ghi log chuc nang nghiep vu</b><br>
     *
     * @param log
     */
    public static void logFunction(EntityLog log) {
        
        try {
            // Format thoi gian thanh chuoi theo dinh dang
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            String strStartTime = sdf.format(log.getStartTime());

            // Khoi tao ban ghi
            StringBuilder record = new StringBuilder();
            record.append(log.getLogType()).append(SEPARATOR);
            record.append(APP_CODE).append(SEPARATOR);
            record.append(strStartTime).append(SEPARATOR);
            record.append(log.getUserName()).append(SEPARATOR);
            record.append(log.getTransactionTime()).append(SEPARATOR);
            record.append(log.getIpAddress()).append(SEPARATOR);
            record.append(log.getPath()).append(SEPARATOR);
            record.append(log.getFunction()).append(SEPARATOR);
            // Loai bo ky tu xuong dong va dau |
            String paramList = log.getParamList();
            if (!CommonUtils.isEmpty(paramList)) {
                paramList = paramList.replaceAll("\n", "").replaceAll("\r", "")
                        .replaceAll("\\|", "");
            } else {
                paramList = "";
            }
            record.append(paramList).append(SEPARATOR);
            record.append(log.getClazz()).append(SEPARATOR);
            record.append(log.getDuration() != null ? log.getDuration() : "")
                    .append(SEPARATOR);
            record.append(log.getDescription());

            // Ghi ra file
            KPI_LOGGER.info(record.toString());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * <b>Log bat dau chuc nang nghiep vu</b><br>
     *
     * @param log
     */
    public static void logFunctionalStart(EntityLog log) {
        
        log.setLogType(START_ACTION_LOG_TYPE);
        logFunction(log);
    }

    /**
     * <b>Log ket thuc chuc nang nghiep vu</b><br>
     *
     * @param log
     */
    public static void logFunctionalEnd(EntityLog log) {
        
        try {
            // Thiet lap loai log
            log.setLogType(END_ACTION_LOG_TYPE);
            // Thiet lap lai thoi gian ghi log
            Date startTime = log.getStartTime();
            Date endTime = new Date();
            log.setStartTime(endTime);
            // Tinh thoi gian thuc hien
            log.setDuration(endTime.getTime() - startTime.getTime());
            logFunction(log);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * <b>Chen log vao bang action_log_mobile</b>
     * 
     * @param userId            id user
     * @param log           danh sach log can ghi
     */
    public static void insertActionLogMobile(Long userId, EntityActionLogMobile log) {

        List<EntityActionLogMobile> listLog = new ArrayList<>();
        listLog.add(log);
        insertActionLogMobile(userId, listLog);
    }
    
    /**
     * <b>Chen log vao bang action_log_mobile</b>
     * 
     * @param userId            id user
     * @param listLog           danh sach log can ghi
     */
    public static void insertActionLogMobile(Long userId, List<EntityActionLogMobile> listLog) {

        try {
            ExecutorService pool = Executors.newFixedThreadPool(1);
            ActionLogMobileThread thread = new ActionLogMobileThread(userId, listLog);
            pool.execute(thread);
            pool.shutdown();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
