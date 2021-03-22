/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import com.viettel.voffice.controler.SystemManagerController;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * Phuong thuc quan tri he thong
 * @author datnv5
 */
@Path("systemManager")
public class SystemManager {
    /**
     * lay danh sach menu chinh
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    @POST
    @Path("getMenuMain")
    @Produces(MediaType.APPLICATION_JSON)
    public String getMenuMain(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        SystemManagerController systemManagerController = new SystemManagerController();
        return systemManagerController.getMenuMain(request, data);
    }
}
