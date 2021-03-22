/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import com.viettel.voffice.controler.LogController;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * REST Web Service
 *
 * @author thanght6
 */
@Path("Log")
public class LogResource {

    /**
     * <b>Thuc hien chen log client vao bang action_log_mobile</b><br>
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    @POST
    @Path("InsertActionLogMobile")
    @Consumes("application/x-www-form-urlencoded")
    public String insertActionLogMobile(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        LogController logController = new LogController();
        return logController.insertActionLogMobile(request, data, isSecurity);
    }
    
    /**
     * <b>Ghi log hanh vi nguoi dung</b><br>
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    @POST
    @Path("InsertUserActivityLog")
    @Consumes("application/x-www-form-urlencoded")
    public String insertUserActivityLog(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
                
        LogController logController = new LogController();
        return logController.insertUserActivityLog(request, data, isSecurity);
    }
}