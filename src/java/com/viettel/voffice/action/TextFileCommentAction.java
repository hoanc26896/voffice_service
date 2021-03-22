/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import com.viettel.voffice.controler.MeetingFileCommentControler;
import com.viettel.voffice.controler.TextFileCommentControler;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

/**
 *
 * @author hanhnq21
 */
@Path("TextFileCommentAction")
public class TextFileCommentAction {

    /**
     * <b>Them moi van ban</b><br/>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("updateTextFileComment")
    @Consumes("application/x-www-form-urlencoded")
    public String updateTextFileComment(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TextFileCommentControler fc = new TextFileCommentControler();
        String result = fc.updateTextFileComment(request, data, isSecurity);
        return result;
    }

    /**
     * <b>Luu comment khi mo all file</b><br/>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("updateListTextFileComment")
    @Consumes("application/x-www-form-urlencoded")
    public String updateListTextFileComment(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TextFileCommentControler fc = new TextFileCommentControler();
        String result = fc.updateListTextFileComment(request, data, isSecurity);
        return result;
    }

    /**
     * xoa note file
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("deleteTextNote")
    @Consumes("application/x-www-form-urlencoded")
    public String deleteTextNote(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TextFileCommentControler fc = new TextFileCommentControler();
        String result = fc.deleteTextNote(request, data, isSecurity);
        return result;
    }

    /**
     * lay danh sach note file
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListTextNote")
    @Consumes("application/x-www-form-urlencoded")
    public String getListTextNote(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TextFileCommentControler fc = new TextFileCommentControler();
        String result = fc.getListTextNote(request, data, isSecurity);
        return result;
    }

    /**
     * them moi note file
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("insertTextNote")
    @Consumes("application/x-www-form-urlencoded")
    public String insertTextNote(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TextFileCommentControler fc = new TextFileCommentControler();
        String result = fc.insertTextNote(request, data, isSecurity);
        return result;
    }

    /**
     * <b>Xoa comment van ban</b><br/>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("resetFileCommentDraff")
    @Consumes("application/x-www-form-urlencoded")
    public String resetFileCommentDraff(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TextFileCommentControler fc = new TextFileCommentControler();
        String result = fc.resetFileCommentDraff(request, data, isSecurity);
        return result;
    }

    /**
     * Xoa comment file van ban trong module trinh ky
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("resetTextAttachComment")
    @Consumes("application/x-www-form-urlencoded")
    public String resetTextAttachComment(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TextFileCommentControler fc = new TextFileCommentControler();
        String result = fc.resetTextAttachComment(request, data, isSecurity);
        return result;
    }


}
