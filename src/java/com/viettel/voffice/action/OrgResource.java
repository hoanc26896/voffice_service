/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import com.viettel.voffice.controler.OrgController;

/**
 * REST Web Service
 *
 * @author thanght6
 */
@Path("Org")
public class OrgResource {

    @POST
    @Path("getListEmployeeOfOrganization")
    @Consumes("application/x-www-form-urlencoded")
    public String getListEmployeeOfOrganization(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        OrgController controller = new OrgController();
        return controller.getListEmployeeOfOrganization(request, data, isSecurity);
    }

    /**
     * Lay danh sach chi tiet kien nghi de xuat
     *
     * @param request
     * @param data :  du lieu client gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("checkSecretaryByGroupId")
    @Consumes("application/x-www-form-urlencoded")
    public String checkSecretaryByGroupId(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        OrgController controller = new OrgController();
        return controller.checkSecretaryByGroupId(isSecurity, data, request);
    }
    
    @POST
    @Path("UpdateOrgCriteria")
    @Consumes("application/x-www-form-urlencoded")
    public String updateOrgCriteria(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        OrgController controller = new OrgController();
        return controller.updateOrgCriteria(request, data);
    }
    
    @POST
    @Path("GetOrgCriteriaList")
    @Consumes("application/x-www-form-urlencoded")
    public String getOrgCriteriaList(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        OrgController controller = new OrgController();
        return controller.getOrgCriteriaList(request, data);
    }
    
    @POST
    @Path("GetOrgCriteriaDetail")
    @Consumes("application/x-www-form-urlencoded")
    public String getOrgCriteriaDetail(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        OrgController controller = new OrgController();
        return controller.getOrgCriteriaDetail(request, data);
    }
    
    @POST
    @Path("UpdateOrgCriteriaMap")
    @Consumes("application/x-www-form-urlencoded")
    public String updateOrgCriteriaMap(
            @Context HttpServletRequest request,
            @FormParam("data") String data) {
        
        OrgController controller = new OrgController();
        return controller.updateOrgCriteriaMap(request, data);
    }
    
    @POST
    @Path("GetOrgListWhichHaveCriteria")
    @Consumes("application/x-www-form-urlencoded")
    public String getOrgListWhichHaveCriteria(
            @Context HttpServletRequest request,
            @FormParam("data") String data) {
        
        OrgController controller = new OrgController();
        return controller.getOrgListWhichHaveCriteria(request, data);
    }
    
    @POST
    @Path("UpdateOrgCriteriaRating")
    @Consumes("application/x-www-form-urlencoded")
    public String updateOrgCriteriaRating(
            @Context HttpServletRequest request,
            @FormParam("data") String data) {
        
        OrgController controller = new OrgController();
        return controller.updateOrgCriteriaRating(request, data);
    }
    
    @POST
    @Path("GetOrgCriteriaRatingTotalList")
    @Consumes("application/x-www-form-urlencoded")
    public String getOrgCriteriaRatingTotalList(
            @Context HttpServletRequest request,
            @FormParam("data") String data) {
        
        OrgController controller = new OrgController();
        return controller.getOrgCriteriaRatingTotalList(request, data);
    }
    
    @POST
    @Path("CreateTextFromOrgCriteriaRatingTotalList")
    @Consumes("application/x-www-form-urlencoded")
    public Object createTextFromOrgCriteriaRatingTotalList(
            @Context HttpServletRequest request,
            @FormParam("data") String data) {
        
        OrgController controller = new OrgController();
        return controller.createTextFromOrgCriteriaRatingTotalList(request, data);
    }
    
    @POST
    @Path("getOrgRatingId")
    @Consumes("application/x-www-form-urlencoded")
    public Object getOrgRatingId(
            @Context HttpServletRequest request,
            @FormParam("data") String data) {
        
        OrgController controller = new OrgController();
        return controller.getOrgRatingId(request, data);
    }

    @POST
    @Path("importOrgCriteria")
    @Consumes("application/x-www-form-urlencoded")
    public Object importOrgCriteria(@Context HttpServletRequest request, @FormParam("data") String data) {

        OrgController controller = new OrgController();
        return controller.importOrgCriteria(request, data);
    }

    @POST
    @Path("getAllOrgCriteria")
    @Consumes("application/x-www-form-urlencoded")
    public Object getAllOrgCriteria(@Context HttpServletRequest request, @FormParam("data") String data) {

        OrgController controller = new OrgController();
        return controller.getAllOrgCriteria(request, data);
    }
}