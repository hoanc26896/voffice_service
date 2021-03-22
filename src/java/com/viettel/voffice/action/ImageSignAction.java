/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.controler.ImageSignController;
import com.viettel.voffice.utils.LogUtils;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * @author outsourceTeam/Sonnd
 * @version 1.0
 * @since
 */
@Path("imageSignAction")
public class ImageSignAction {

    public static final String ROOT_ACTION = "imageSignAction";

    /**
     * them moi anh chu ky
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("addSignImage")
    @Consumes("application/x-www-form-urlencoded")
    public String addSignalImage(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        ImageSignController imageController = new ImageSignController();
        String result = imageController.addSignImage(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    /**
     * sua anh chu ky
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("editSignImage")
    @Consumes("application/x-www-form-urlencoded")
    public String editSignalImage(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        ImageSignController imageController = new ImageSignController();
        String result = imageController.editSignImage(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    /**
     * tim kiem tat ca anh chu ky
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("search")
    @Consumes("application/x-www-form-urlencoded")
    public String search(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        ImageSignController imageController = new ImageSignController();
        String result = imageController.search(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    /**
     * review file chu ky pdf
     *
     * @param req
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("/reviewFilePdf")
    @Consumes("application/x-www-form-urlencoded")
    public Response reviewFilePdf(
            @Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        ImageSignController isc = new ImageSignController();
        Response result = isc.reviewImage(req, data, isSecurity);
        return result;
    }

    /**
     * review file chu ky pdf
     *
     * @param req
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getImageSignByCardId")
    @Consumes("application/x-www-form-urlencoded")
    public String getImageSignByCardId(
            @Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        ImageSignController isc = new ImageSignController();
        String result = isc.getImageSignByCardId(req, data, isSecurity);
        return result;
    }
}