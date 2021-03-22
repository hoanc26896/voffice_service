/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import com.viettel.voffice.controler.DocumentPublishControler;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

/**
 *
 * @author hanhnq21@viettel.com.vn
 * @since 08/03/2016
 * @version 1.0
 */
@Path("DocumentPublishAction")
public class DocumentPublishAction {

    /**
     * tim kiem van ban cong bo
     *
     * @param req
     * @param data
     * @param strSecurity
     * @return
     */
    @POST
    @Path("actionSearchDocPublish")
    @Consumes("application/x-www-form-urlencoded")
    public String actionSearchDocPublish(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        DocumentPublishControler controller = new DocumentPublishControler();
        String strDataResponse = controller.actionSearchDocPublish(strSecurity, data, req);
        return strDataResponse;
    }

    /**
     * lay danh sach van ban thay the
     *
     * @param req
     * @param data
     * @param strSecurity
     * @return
     */
    @POST
    @Path("getListDocAlter")
    @Consumes("application/x-www-form-urlencoded")
    public String getListDocAlter(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        DocumentPublishControler controller = new DocumentPublishControler();
        String strDataResponse = controller.getListDocAlter(strSecurity, data, req);
        return strDataResponse;
    }
    
    /**
     * tim kiem van ban thay the tu dong
     *
     * @param req
     * @param data
     * @param strSecurity
     * @return
     */
    @POST
    @Path("actionSearchAlterDocAuto")
    @Consumes("application/x-www-form-urlencoded")
    public String actionSearchAlterDocAuto(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        DocumentPublishControler controller = new DocumentPublishControler();
        String strDataResponse = controller.actionSearchAlterDocAuto(strSecurity, data, req);
        return strDataResponse;
    }
    
    @POST
    @Path("getParentOrgLastSignOrg")
    @Consumes("application/x-www-form-urlencoded")
    public String getParentOrgLastSignOrg(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        DocumentPublishControler controller = new DocumentPublishControler();
        String strDataResponse = controller.getParentOrgLastSignOrg(strSecurity, data, req);
        return strDataResponse;
    }
    
}
