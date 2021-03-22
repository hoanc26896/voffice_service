/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import com.viettel.voffice.controler.CMController;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * REST Web Service
 *
 * @author  thanght6
 */
@Path("CM")
public class CMResource {

    @POST
    @Path("/listCompany")
    @Consumes("application/x-www-form-urlencoded")
    public String listCompany(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        CMController controller = new CMController();
        return controller.listCompany(request, data, isSecurity);
    }
    
    @POST
    @Path("/createSignDocument")
    @Consumes("application/x-www-form-urlencoded")
    public String createSignDocument(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        CMController controller = new CMController();
        return controller.createSignDocument(request, data, isSecurity);
    }
    
    @POST
    @Path("/search")
    @Consumes("application/x-www-form-urlencoded")
    public String search(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        CMController controller = new CMController();
        return controller.search(request, data, isSecurity);
    }
    
    @POST
    @Path("/notify")
    @Consumes("application/x-www-form-urlencoded")
    public String notify(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        CMController controller = new CMController();
        return controller.notify(request, data, isSecurity);
    }
    
    @POST
    @Path("/updateStateDocument")
    @Consumes("application/x-www-form-urlencoded")
    public String updateStateDocument(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        CMController controller = new CMController();
        return controller.updateStateDocument(request, data, isSecurity);
    }
    
    @POST
    @Path("/sendDocument")
    @Consumes("application/x-www-form-urlencoded")
    public String sendDocument(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        CMController controller = new CMController();
        return controller.sendDocument(request, data, isSecurity);
    }
    
    @POST
    @Path("/trustFile")
    @Consumes("application/x-www-form-urlencoded")
    public String trustFile(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        CMController controller = new CMController();
        return controller.trustFile(request, data, isSecurity);
    }
    
    @POST
    @Path("/copyToTmpFolder")
    @Consumes("application/x-www-form-urlencoded")
    public String copyToTmpFolder(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        CMController controller = new CMController();
        return controller.copyToTmpFolder(request, data, isSecurity);
    }
    
    @POST
    @Path("/checkPromulgation")
    @Consumes("application/x-www-form-urlencoded")
    public String checkPromulgation(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        CMController controller = new CMController();
        return controller.checkPromulgation(request, data, isSecurity);
    }
    
}
