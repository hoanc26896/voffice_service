/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import com.viettel.voffice.controler.CommentController;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.utils.LogUtils;
import org.json.JSONException;

/**
 *
 * @author luanvd
 */
@Path("commentAction")
public class CommentAction {

    public static final String ROOT_ACTION = "commentAction";

    /**
     * Lay danh sach comment
     *
     * @param request
     * @param data : du lieu client  gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListComment")
    @Consumes("application/x-www-form-urlencoded")
    public String getListComment(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        CommentController tc = new CommentController();
        String result = tc.getListComments(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    /**
     * Them moi comment
     *
     * @param request
     * @param data : du lieu client gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("addComment")
    @Consumes("application/x-www-form-urlencoded")
    public String addComment(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        CommentController tc = new CommentController();
        String result = tc.addComment(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    @POST
    @Path("supportFinacialText")
    @Consumes("application/x-www-form-urlencoded")
    public String supportFinacialText(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        CommentController tc = new CommentController();
        String result = tc.supportFinacialText(request, data, isSecurity);
        return result;
    }

    @POST
    @Path("supportSystem")
    @Consumes("application/x-www-form-urlencoded")
    public String supportSystem(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        CommentController tc = new CommentController();
        String result = tc.supportSystem(request, data, isSecurity);
        return result;
    }
    
    @POST
    @Path("savePersonalStorage")
    @Consumes("application/x-www-form-urlencoded")
    public String savePersonalStorage(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        CommentController tc = new CommentController();
        String result = tc.savePersonalStorage(request, data, isSecurity);
        return result;
    }
    
    @POST
    @Path("searchPeronalStorage")
    @Consumes("application/x-www-form-urlencoded")
    public String searchPeronalStorage(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        CommentController tc = new CommentController();
        String result = tc.searchPeronalStorage(request, data, isSecurity);
        return result;
    }
    
    @POST
    @Path("checkSavedPersonalStorage")
    @Consumes("application/x-www-form-urlencoded")
    public String checkSavedPersonalStorage(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        CommentController tc = new CommentController();
        String result = tc.checkSavedPersonalStorage(request, data, isSecurity);
        return result;
    }
}
