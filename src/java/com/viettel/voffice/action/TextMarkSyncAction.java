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
import com.viettel.voffice.controler.TextMarkSyncControler;
import javax.ws.rs.core.Response;

/**
 * danh dau dong bo van ban
 *
 * @author hanhnq21
 */
@Path("textMarkSyncAction")
public class TextMarkSyncAction {

    /**
     * danh dau van ban dong bo
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("addTextMarkSync")
    @Consumes("application/x-www-form-urlencoded")
    public String addTextMarkSync(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TextMarkSyncControler controler = new TextMarkSyncControler();
        return controler.addTextMarkSync(request, data, isSecurity);
    }

    /**
     * lay thong tin danh dau van ban
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getTextMarkSync")
    @Consumes("application/x-www-form-urlencoded")
    public String getTextMarkSync(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TextMarkSyncControler controler = new TextMarkSyncControler();
        return controler.getTextMarkSync(request, data, isSecurity);
    }

    /**
     * lay danh sach dong bo van ban
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListDocumentSync")
    @Consumes("application/x-www-form-urlencoded")
    public String getListDocumentSync(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TextMarkSyncControler controler = new TextMarkSyncControler();
        return controler.getListDocumentSync(request, data, isSecurity);
    }

    /**
     * lay chi tiet van ban dong bo
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getTextDetailSync")
    @Consumes("application/x-www-form-urlencoded")
    public String getTextDetailSync(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TextMarkSyncControler controler = new TextMarkSyncControler();
        return controler.getTextDetailSync(request, data, isSecurity);
    }
}