/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.controler.SyncFavoriteClientController;
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
@Path("syncFavAction")
public class SyncFavoriteAction {

    public static final String ROOT_ACTION = "syncFavAction";

    /**
     * Đồng bộ danh sách cá nhân fav
     *
     * @param request
     * @param data : du lieu client gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("syncEmployeeFav")
    @Consumes("application/x-www-form-urlencoded")
    public String syncEmployeeFav(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        SyncFavoriteClientController tc = new SyncFavoriteClientController();
        String result = tc.syncEmployeeFav(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    /**
     * Đồng bộ danh sách đơn vị fav
     *
     * @param request
     * @param data : du lieu client gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("syncOrganizationFav")
    @Consumes("application/x-www-form-urlencoded")
    public String syncOrganizationFav(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        SyncFavoriteClientController tc = new SyncFavoriteClientController();
        String result = tc.syncOrganizationFav(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }
}