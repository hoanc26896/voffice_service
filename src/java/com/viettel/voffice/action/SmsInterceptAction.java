/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.viettel.voffice.action;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import com.viettel.voffice.controler.SmsInterceptController;

/**
 * Configuration deny and allow send messenger to user
 * @author: DATNV5, mobile: 0986565786
 */
@Path("SmsInterceptAction")
public class SmsInterceptAction {
    /**
     * lay danh sach modul cau hinh gui tin nhan
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    @POST
    @Path("getListModulSms")
    @Produces(MediaType.APPLICATION_JSON)
    public String getListModulSms(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        SmsInterceptController smsInterceptController = new SmsInterceptController();
        return smsInterceptController.getListModulSms(request, data);
    }
    
    @POST
    @Path("getListModulInterceptSmsOfUserId")
    @Produces(MediaType.APPLICATION_JSON)
    public String getListModulInterceptSmsOfUserId(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        SmsInterceptController smsInterceptController = new SmsInterceptController();
        return smsInterceptController.getListModulInterceptSmsOfUserId(request, data);
    }
    /**
     * update blacklist received messenger
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    @POST
    @Path("addOrRemoveInterceptByUser")
    @Produces(MediaType.APPLICATION_JSON)
    public String addOrRemoveInterceptByUser(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        SmsInterceptController smsInterceptController = new SmsInterceptController();
        return smsInterceptController.addOrRemoveInterceptByUser(request, data);
    }
}