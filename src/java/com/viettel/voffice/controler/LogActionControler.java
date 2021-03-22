/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.viettel.voffice.database.dao.logAction.LogActionDao;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author datnv5
 */
public class LogActionControler {
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LogActionControler.class);
    public Boolean insertActionLog(final Long staffId, final String strLoginName,
            final String strFunction, final HttpServletRequest req, final String strConten, final Date startTime,
            final String strDevice, final String strVoffice) {
        try {

            String strIpServer = req.getLocalAddr() +":"+ String.valueOf(req.getLocalPort());
            Date endTime = new Date();
            Long subTime = endTime.getTime() - startTime.getTime();
            LogActionDao lg = new LogActionDao();
            lg.insertActionLog(staffId, strLoginName, strFunction, strIpServer, strConten,
                    startTime, endTime, strDevice, strVoffice, subTime);

        } catch (Exception e) {
             log.error("Loi! LogActionControler: ",e);
        }
        return true;
    }
}
