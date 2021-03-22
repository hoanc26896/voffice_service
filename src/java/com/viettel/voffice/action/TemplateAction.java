/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.controler.TemplateController;
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
@Path("tempAction")
public class TemplateAction {

    public static final String ROOT_ACTION = "tempAction";

    /**
     * Lay danh sach template
     *
     * @param request
     * @param data : du lieu client gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListTemplate")
    @Consumes("application/x-www-form-urlencoded")
    public String getListTemplate(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        TemplateController tc = new TemplateController();
        String result = tc.getListTemplate(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    /**
     * Them moi template
     *
     * @param request
     * @param data : du lieu client gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("addTemplate")
    @Consumes("application/x-www-form-urlencoded")
    public String addTemplate(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        TemplateController tc = new TemplateController();
        String result = tc.addTemplate(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    /**
     * Sua template
     *
     * @param request
     * @param data : du lieu client gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("editTemplate")
    @Consumes("application/x-www-form-urlencoded")
    public String editTemplate(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        TemplateController tc = new TemplateController();
        String result = tc.editTemplate(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    /**
     * Xoa template
     *
     * @param request
     * @param data : du lieu client gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("deleteTemplate")
    @Consumes("application/x-www-form-urlencoded")
    public String deleteTemplate(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        TemplateController tc = new TemplateController();
        String result = tc.deleteTemplate(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    /**
     * Xoa một danh sách template
     *
     * @param request
     * @param data : du lieu client gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("deleteListTemplate")
    @Consumes("application/x-www-form-urlencoded")
    public String deleteListTemplate(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        TemplateController tc = new TemplateController();
        String result = tc.deleteListTemplate(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    /**
     * Update index of template
     *
     * @param request
     * @param data : du lieu client gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("updateIndexTemplate")
    @Consumes("application/x-www-form-urlencoded")
    public String updateIndexTemplate(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        TemplateController tc = new TemplateController();
        String result = tc.updateIndexTemplate(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    /**
     * Thêm một danh sách template
     *
     * @param request
     * @param data : du lieu client gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("addListTemplate")
    @Consumes("application/x-www-form-urlencoded")
    public String addListTemplate(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        TemplateController tc = new TemplateController();
        String result = tc.addAListTemplate(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    /**
     * xoa bieu mau
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("delete")
    @Consumes("application/x-www-form-urlencoded")
    public String delete(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TemplateController tc = new TemplateController();
        String result = tc.delete(request, data, isSecurity);
        return result;
    }

    /**
     * lay chi tiet bieu mau
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getTemplateDetail")
    @Consumes("application/x-www-form-urlencoded")
    public String getTemplateDetail(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TemplateController tc = new TemplateController();
        String result = tc.getTemplateDetail(request, data, isSecurity);
        return result;
    }

    /**
     * tim kiem bieu mau
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("searchTemplate")
    @Consumes("application/x-www-form-urlencoded")
    public String searchTemplate(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TemplateController tc = new TemplateController();
        String result = tc.searchTemplate(request, data, isSecurity);
        return result;
    }

    /**
     * them moi bieu mau
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("insertTemplate")
    @Consumes("application/x-www-form-urlencoded")
    public String insertTemplate(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TemplateController tc = new TemplateController();
        String result = tc.insertTemplate(request, data, isSecurity);
        return result;
    }

    /**
     * sua bieu mau
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("updateTemplate")
    @Consumes("application/x-www-form-urlencoded")
    public String updateTemplate(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TemplateController tc = new TemplateController();
        String result = tc.updateTemplate(request, data, isSecurity);
        return result;
    }
}