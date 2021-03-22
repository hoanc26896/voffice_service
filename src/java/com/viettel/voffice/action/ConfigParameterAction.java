/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.controler.ConfigParameterController;
import com.viettel.voffice.utils.LogUtils;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

/**
 *
 * @author luanvd
 */
@Path("configParamAction")
public class ConfigParameterAction {

    public static final String ROOT_ACTION = "configParamAction";

    @POST
    @Path("getConfigParamMultiSign")
    @Consumes("application/x-www-form-urlencoded")
    public String getConfigParamMultiSign(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        ConfigParameterController tc = new ConfigParameterController();
        String result = tc.getConfigParamMultiSign(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    @POST
    @Path("getListConfigBackList")
    @Consumes("application/x-www-form-urlencoded")
    public String getListConfigBackList(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        ConfigParameterController tc = new ConfigParameterController();
        String result = tc.getListConfigBackList(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    @POST
    @Path("findConfigBackList")
    @Consumes("application/x-www-form-urlencoded")
    public String findConfigBackList(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        ConfigParameterController tc = new ConfigParameterController();
        String result = tc.findConfigBackList(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    @POST
    @Path("deleteConfigBackList")
    @Consumes("application/x-www-form-urlencoded")
    public String deleteConfigBackList(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        ConfigParameterController tc = new ConfigParameterController();
        String result = tc.deleteConfigBackList(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    @POST
    @Path("insertConfigBackList")
    @Consumes("application/x-www-form-urlencoded")
    public String insertConfigBackList(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        ConfigParameterController tc = new ConfigParameterController();
        String result = tc.insertConfigBackList(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }
    
    @POST
    @Path("GetAppConfig")
    @Consumes("application/x-www-form-urlencoded")
    public String getAppConfig(@Context HttpServletRequest request,
            @FormParam("data") String data) {
        
        ConfigParameterController controller = new ConfigParameterController();
        return controller.getAppConfig(request, data);
    }
}
