/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.thread;

import com.viettel.voffice.database.dao.logAction.ActionLogMobileDAO;
import com.viettel.voffice.database.entity.log.EntityActionLogMobile;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * Thread ghi log
 * 
 * @author thanght6
 * @since Nov 06, 2017
 */
public class LogThread {

    /**
     * Thread ghi log vao bang action_log_mobile tren database 1
     */
    public static class ActionLogMobileThread extends Thread {

        /** Log file */
        private static final Logger LOGGER = Logger.getLogger(ActionLogMobileThread.class);
        
        /** ID user */
        private Long userId;
        
        /** Danh sach log can ghi */
        private List<EntityActionLogMobile> listLog;

        public ActionLogMobileThread(Long userId, List<EntityActionLogMobile> listLog) {
            
            this.userId = userId;
            this.listLog = listLog;
        }

        @Override
        public void run() {
            
            try {
                ActionLogMobileDAO dao = new ActionLogMobileDAO();
                dao.insert(userId, listLog);
            } catch (Exception e) {
                LOGGER.error("Loi chen log vao bang action_log_mobie!", e);
            }
        }
    }
}
