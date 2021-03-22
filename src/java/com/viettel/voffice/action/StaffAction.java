/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import com.viettel.voffice.controler.UserControler;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import org.json.JSONException;

/**
 * REST Web Service
 *
 * @author datnv5
 */
@Path("staffAction")
public class StaffAction {

    public static final String ROOT_ACTION = "staffAction";

    @POST
    @Path("getUserInfor")
    @Consumes("application/x-www-form-urlencoded")
    public String getUserInfor(@Context HttpServletRequest req,
            @FormParam("data") String data) throws JSONException {
        UserControler userControler = new UserControler();
        String response = userControler.getUserInfor(req);
        return response;
    }

    @POST
    @Path("getListUser")
    @Consumes("application/x-www-form-urlencoded")
    public String getListUser(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        UserControler userControler = new UserControler();
        String strDataResponse = userControler.getListUser(req, data, strSecurity);
        return strDataResponse;
    }

    @POST
    @Path("getOrgInfoById")
    @Consumes("application/x-www-form-urlencoded")
    public String getOrgInfoById(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        UserControler userControler = new UserControler();
        String strDataResponse = userControler.getOrgInfoById(req, data, strSecurity);
        return strDataResponse;
    }

    @POST
    @Path("getListUserMutiGroup")
    @Consumes("application/x-www-form-urlencoded")
    public String getListUserMutiGroup(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        UserControler userControler = new UserControler();
        return userControler.getListUserMutiGroup(req, data, strSecurity);
    }

    /**
     * @author HaNH
     * <b> Lay danh sach Lanh dao, thu truong cua cac don vi</b>
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getLeaderByOrg")
    @Consumes("application/x-www-form-urlencoded")
    public String getLeaderByOrg(@Context HttpServletRequest request,
            @FormParam("data") String data, @FormParam("isSecurity") String isSecurity) {
        UserControler userControler = new UserControler();
        String strDataResponse = userControler.getLeaderByOrg(request, data, isSecurity);
        return strDataResponse;
    }
    
        /**
     * @author Hiendv2
     * <b> Lay danh sach account vip</b>
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getLstUserVip")
    @Consumes("application/x-www-form-urlencoded")
    public String getLstUserVip(@Context HttpServletRequest request,
            @FormParam("data") String data, @FormParam("isSecurity") String isSecurity) {
        UserControler userControler = new UserControler();
        String strDataResponse = userControler.getLstUserVip(request);
        return strDataResponse;
    }
    
    @POST
    @Path("getListUserConfigAssistant")
    @Consumes("application/x-www-form-urlencoded")
    public String getListUserConfigAssistant(@Context HttpServletRequest request,
            @FormParam("data") String data, @FormParam("isSecurity") String isSecurity) {
        UserControler userControler = new UserControler();
        String strDataResponse = userControler.getListUserConfigAssistant(request, data);
        return strDataResponse;
    }
}