/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.controler.SyncMenuController;
import com.viettel.voffice.utils.LogUtils;
import org.json.JSONException;

/**
 * REST Web Service
 *
 */
@Path("SyncMenu")
public class SyncMenuResource {

    private static final String ROOT_ACTION = "SyncMenu";

    /**
     * dong bo bang SYS_MENU
     *
     * @param request
     * @param data
     * @return
     * @throws JSONException
     */
    @POST
    @Path("syncMenu")
    @Consumes("application/x-www-form-urlencoded")
    public String syncMenu(@Context HttpServletRequest request, @FormParam("data") String data) throws JSONException {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, null);
        SyncMenuController syncMenuController = new SyncMenuController();
        String result = syncMenuController.syncMenu(data);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, null);
        return result;
    }

    /**
     * dong bo bang SYS_ROLE
     *
     * @param request
     * @param data
     * @return
     * @throws JSONException
     */
    @POST
    @Path("syncRole")
    @Consumes("application/x-www-form-urlencoded")
    public String syncRole(@Context HttpServletRequest request, @FormParam("data") String data) throws JSONException {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, null);
        SyncMenuController syncMenuController = new SyncMenuController();
        String result = syncMenuController.syncRole(data);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, null);
        return result;
    }

    /**
     * dong bo bang ROLE_MENU
     *
     * @param request
     * @param data
     * @return
     * @throws JSONException
     */
    @POST
    @Path("syncRoleMenu")
    @Consumes("application/x-www-form-urlencoded")
    public String syncRoleMenu(@Context HttpServletRequest request, @FormParam("data") String data) throws JSONException {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, null);
        SyncMenuController syncMenuController = new SyncMenuController();
        String result = syncMenuController.syncRoleMenu(data);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, null);
        return result;
    }

    /**
     * dong bo bang ROLE_MENU
     *
     * @param request
     * @param data
     * @return
     * @throws JSONException
     */
    @POST
    @Path("syncAllRoleMenu")
    @Consumes("application/x-www-form-urlencoded")
    public String syncAllRoleMenu(@Context HttpServletRequest request, @FormParam("data") String data) throws JSONException {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, null);
        SyncMenuController syncMenuController = new SyncMenuController();
        String result = syncMenuController.getAllRoleMenu(data);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, null);
        return result;
    }

    /**
     * dong bo bang USER_ROLE
     *
     * @param request
     * @param data
     * @return
     * @throws JSONException
     */
    @POST
    @Path("syncUserRole")
    @Consumes("application/x-www-form-urlencoded")
    public String syncUserRole(@Context HttpServletRequest request, @FormParam("data") String data) throws JSONException {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, null);
        SyncMenuController syncMenuController = new SyncMenuController();
        String result = syncMenuController.syncUserRole(data);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, null);
        return result;
    }

    /**
     * dong bo bang USER_ROLE
     *
     * @param request
     * @param data
     * @return
     * @throws JSONException
     */
    @POST
    @Path("countUserRole")
    @Consumes("application/x-www-form-urlencoded")
    public String countUserRole(@Context HttpServletRequest request, @FormParam("data") String data) throws JSONException {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, null);
        SyncMenuController syncMenuController = new SyncMenuController();
        String result = syncMenuController.getCountAllUserRole(data);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, null);
        return result;
    }

    /**
     * dong bo bang USER_ROLE
     *
     * @param request
     * @param data
     * @return
     * @throws JSONException
     */
    @POST
    @Path("syncSomeUserRole")
    @Consumes("application/x-www-form-urlencoded")
    public String syncSomeUserRole(@Context HttpServletRequest request, @FormParam("data") String data) throws JSONException {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, null);
        SyncMenuController syncMenuController = new SyncMenuController();
        String result = syncMenuController.getSomeUserRole(data);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, null);
        return result;
    }
}