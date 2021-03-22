/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import com.viettel.voffice.controler.FinanceDataController;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 * REST Web Service
 *
 * @author thanght6
 */
@Path("SLTC")
public class SLTCResource {

    @POST
    @Path("/getListIndicatorBase")
    @Consumes("application/x-www-form-urlencoded")
    public String getBasicIndicatorList(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        FinanceDataController financeDataController = new FinanceDataController();
        return financeDataController.getListIndicatorBase(request, data, isSecurity);
    }
    
    @POST
    @Path("/getRegionTreeByRoleSmart")
    @Consumes("application/x-www-form-urlencoded")
    public String getRegionTreeByRoleSmart(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        FinanceDataController financeDataController = new FinanceDataController();
        return financeDataController.getRegionTreeByRoleSmart(request, data, isSecurity);
    }
    
    @POST
    @Path("/getTreeListIndicator")
    @Consumes("application/x-www-form-urlencoded")
    public String getTreeListIndicator(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        FinanceDataController financeDataController = new FinanceDataController();
        return financeDataController.getTreeListIndicator(request, data, isSecurity);
    }
    
    @POST
    @Path("/getReportStatistics")
    @Consumes("application/x-www-form-urlencoded")
    public String getReportStatistics(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        FinanceDataController financeDataController = new FinanceDataController();
        return financeDataController.getReportStatistics(request, data, isSecurity);
    }
    
    @POST
    @Path("/getReportIndicatorData")
    @Consumes("application/x-www-form-urlencoded")
    public String getReportIndicatorData(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        FinanceDataController financeDataController = new FinanceDataController();
        return financeDataController.getReportIndicatorData(request, data, isSecurity);
    }
    
    @POST
    @Path("/getReportIndicatorChildData")
    @Consumes("application/x-www-form-urlencoded")
    public String getReportIndicatorChildData(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        FinanceDataController financeDataController = new FinanceDataController();
        return financeDataController.getReportIndicatorChildData(request, data, isSecurity);
    }
    
    @GET
    @Path("/DownloadChartImage/{path}")
    @Consumes("application/x-www-form-urlencoded")
    public Response downloadChartImage(
            @Context HttpServletRequest request,
            @PathParam("path") String path) {
        
        FinanceDataController financeDataController = new FinanceDataController();
        return financeDataController.downloadChartImage(request, path);
    }
}
