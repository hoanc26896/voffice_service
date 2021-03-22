/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import com.viettel.voffice.controler.BoxManagementController;
import com.viettel.voffice.controler.MissionReportController;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

/**
 *
 * @author pm1_os06
 */
/**
 * REST Web Service
 *
 */
@Path("MissionReport")
public class MissionReportAction {

    /**
     * DANH SACHS NGUOI TAO NHIEM VU vaf nhiem vu
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListEmployee")
    @Consumes("application/x-www-form-urlencoded")
    public String getListEmployee(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        MissionReportController oc = new MissionReportController();
        return oc.getListEmployee(isSecurity, data, request);
    }

    /**
     * Danh sach nguoi tao theo don vi
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListCreatBy")
    @Consumes("application/x-www-form-urlencoded")
    public String getListCreatBy(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        MissionReportController oc = new MissionReportController();
        return oc.getListCreatBy(isSecurity, data, request);
    }

    /**
     * LIST don vi theo user dang nhap va sys_role code
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListGroup")
    @Consumes("application/x-www-form-urlencoded")
    public String getListGroup(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        MissionReportController oc = new MissionReportController();
        return oc.getListGroup(isSecurity, data, request);
    }

    /**
     * danh sach don vi thuc hien
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListGroupPerform")
    @Consumes("application/x-www-form-urlencoded")
    public String getListPerformGroup(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        MissionReportController oc = new MissionReportController();
        return oc.getListPerformGroup(isSecurity, data, request);
    }

    /**
     * Danh sach don vi thuc hien va nhiem vu
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListMissionOfGroup")
    @Consumes("application/x-www-form-urlencoded")
    public String getListMissionOfGroup(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        MissionReportController oc = new MissionReportController();
        return oc.getListMissionOfGroup(isSecurity, data, request);
    }
    
    /**
     * Danh sach don vi thuc hien va nhiem vu theo Quy
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListQuarterMissionOfGroup")
    @Consumes("application/x-www-form-urlencoded")
    public String getListQuarterMissionOfGroup(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        MissionReportController oc = new MissionReportController();
        return oc.getListQuarterMissionOfGroup(isSecurity, data, request);
    }

    /**
     * Chi tiet nhung nhiem vu chua hoan thanh
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("viewMissionReport")
    @Consumes("application/x-www-form-urlencoded")
    public String viewMissionReport(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        MissionReportController oc = new MissionReportController();
        return oc.viewMissionReport(isSecurity, data, request);
    }
    
    /**
     * Chi tiet nhung nhiem vu chua hoan thanh trong quy
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("viewQuarterMissionReport")
    @Consumes("application/x-www-form-urlencoded")
    public String viewQuarterMissionReport(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        MissionReportController oc = new MissionReportController();
        return oc.viewQuarterMissionReport(isSecurity, data, request);
    }
    
    /**
     * lay danh sach cong viec bao cao
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    @POST
    @Path("getListMissionReport")
    @Consumes("application/x-www-form-urlencoded")
    public String getListMissionReport(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        MissionReportController oc = new MissionReportController();
        return oc.getListMissionReport(isSecurity, data, request);
    }
    
    /**
     * lay chi tiet danh sach cong viec bao cao theo quy
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    @POST
    @Path("getListQuarterMissionReport")
    @Consumes("application/x-www-form-urlencoded")
    public String getListQuarterMissionReport(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        MissionReportController oc = new MissionReportController();
        return oc.getListQuarterMissionReport(isSecurity, data, request);
    }

    @POST
    @Path("findMissionNorm")
    @Consumes("application/x-www-form-urlencoded")
    public String findMissionNorm(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        MissionReportController oc = new MissionReportController();
        return oc.findMissionNorm(isSecurity, data, request);
    }
    
    @POST
    @Path("insertMissionNorm")
    @Consumes("application/x-www-form-urlencoded")
    public String insertMissionNorm(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        MissionReportController oc = new MissionReportController();
        return oc.insertMissionNorm(isSecurity, data, request);
    }
    
    @POST
    @Path("updateMissionNorm")
    @Consumes("application/x-www-form-urlencoded")
    public String updateMissionNorm(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        MissionReportController oc = new MissionReportController();
        return oc.updateMissionNorm(isSecurity, data, request);
    }
}
