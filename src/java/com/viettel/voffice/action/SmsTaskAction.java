/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.controler.SmsTaskController;
import com.viettel.voffice.utils.LogUtils;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * REST Web Service
 *
 * @author voffice_guest1
 */
@Path("smsTask")
public class SmsTaskAction {

    public static final String ROOT_ACTION = "smsTask";

    @POST
    @Path("sendSmsSignAfterMonth")
    @Consumes("application/x-www-form-urlencoded")
    public String sendSmsSignAfterMonth(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        SmsTaskController smsController = new SmsTaskController();
        String result = smsController.sendSmsSignAfterMonth(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    @POST
    @Path("sendMulSmsSignAfterMonth")
    @Consumes("application/x-www-form-urlencoded")
    public String sendMulSmsSignAfterMonth(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        SmsTaskController smsController = new SmsTaskController();
        String result = smsController.sendSmsMulSignAfterMonth(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    @POST
    @Path("sendSmsMeetingAssistant")
    @Consumes("application/x-www-form-urlencoded")
    public String sendSmsMeetingAssistant(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        SmsTaskController smsController = new SmsTaskController();
        String result = smsController.sendSmsMeetingAssistant(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    @POST
    @Path("checkDocumentToSendSms")
    @Consumes("application/x-www-form-urlencoded")
    public String checkDocumentToSendSms(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        SmsTaskController smsController = new SmsTaskController();
        String result = smsController.checkDocumentToSendSms(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }
}
