/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.json.JSONException;

import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.controler.ConnectVHRController;
import com.viettel.voffice.utils.LogUtils;

/**
 *
 * @author Hoanhv6
 */
@Path("connectVHRAction")
public class ConnectVHRAction {

    public static final String ROOT_ACTION = "connectVHRAction";

    /**
     * lay don vi lien thong
     *
     * @param request
     * @param data    du lieu gui len
     * @return
     */
    @POST
    @Path("findByCondition")
    @Consumes("application/x-www-form-urlencoded")
    public String findByCondition(@Context HttpServletRequest request, @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        ConnectVHRController cvc = new ConnectVHRController();
        String result = cvc.findByCondition(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }
    
    @POST
    @Path("getMaxChildSortOrder")
    @Consumes("application/x-www-form-urlencoded")
    public String getMaxChildSortOrder(@Context HttpServletRequest request, @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        ConnectVHRController cvc = new ConnectVHRController();
        String result = cvc.getMaxChildSortOrder(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }
    
    @POST
    @Path("updateConnectVHR")
    @Consumes("application/x-www-form-urlencoded")
    public String updateConnectVHR(@Context HttpServletRequest request, @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        ConnectVHRController cvc = new ConnectVHRController();
        String result = cvc.updateConnectVHR(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }
    
    @POST
    @Path("createNewConnectVHR")
    @Consumes("application/x-www-form-urlencoded")
    public String createNewConnectVHR(@Context HttpServletRequest request, @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        ConnectVHRController cvc = new ConnectVHRController();
        String result = cvc.createNewConnectVHR(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }
    
    @POST
    @Path("checkCodeExist")
    @Consumes("application/x-www-form-urlencoded")
    public String checkCodeExist(@Context HttpServletRequest request, @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        ConnectVHRController cvc = new ConnectVHRController();
        String result = cvc.checkCodeExist(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }
    
    @POST
    @Path("deleteConnectVHR")
    @Consumes("application/x-www-form-urlencoded")
    public String deleteConnectVHR(@Context HttpServletRequest request, @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        ConnectVHRController cvc = new ConnectVHRController();
        String result = cvc.deleteConnectVHR(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }
    
    @POST
    @Path("updateSyncOrg")
    @Consumes("application/x-www-form-urlencoded")
    public String updateSyncOrg(@Context HttpServletRequest request, @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        ConnectVHRController cvc = new ConnectVHRController();
        String result = cvc.updateSyncOrg(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }
}
