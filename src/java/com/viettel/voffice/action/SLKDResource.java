/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import com.viettel.voffice.controler.BusinessDataController;
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
@Path("SLKD")
public class SLKDResource {

    @POST
    @Path("/getListDepartFromMapFN")
    @Consumes("application/x-www-form-urlencoded")
    public String getListDepartFromMapFN(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        BusinessDataController controller = new BusinessDataController();
        return controller.getListDepartFromMapFN(request, data, isSecurity);
    }
    
    @POST
    @Path("/getListDepartFolowFunc")
    @Consumes("application/x-www-form-urlencoded")
    public String getListDepartFolowFunc(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        BusinessDataController controller = new BusinessDataController();
        return controller.getListDepartFolowFunc(request, data, isSecurity);
    }
    
    @POST
    @Path("/getListIndicatorFolowDepartFunc")
    @Consumes("application/x-www-form-urlencoded")
    public String getListIndicatorFolowDepartFunc(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        BusinessDataController controller = new BusinessDataController();
        return controller.getListIndicatorFolowDepartFunc(request, data, isSecurity);
    }
    
    @POST
    @Path("/getListImgPath")
    @Consumes("application/x-www-form-urlencoded")
    public String getListImgPath(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        BusinessDataController controller = new BusinessDataController();
        return controller.getListImgPath(request, data, isSecurity);
    }
    
    @GET
    @Path("/DownloadReportImage/{path}")
    @Consumes("application/x-www-form-urlencoded")
    public Response downloadReportImage(
            @Context HttpServletRequest request,
            @PathParam("path") String path) {
        
        BusinessDataController controller = new BusinessDataController();
        return controller.downloadReportImage(request, path);
    }
    
    @POST
    @Path("/GetDepartment")
    @Consumes("application/x-www-form-urlencoded")
    public String getDepartment(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        BusinessDataController controller = new BusinessDataController();
        return controller.getDepartment(request, data, isSecurity);
    }
    
    @POST
    @Path("/GetListReport")
    @Consumes("application/x-www-form-urlencoded")
    public String getListReport(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        BusinessDataController controller = new BusinessDataController();
        return controller.getListReport(request, data, isSecurity);
    }
    
    @GET
    @Path("/DownloadImageReport/{reportType}/{imageId}/{date}/{departmentCode}")
    @Consumes("application/x-www-form-urlencoded")
    public Response downloadImageReport(
            @Context HttpServletRequest request,
            @PathParam("reportType") Long reportType,
            @PathParam("imageId") Long imageId,
            @PathParam("date") String date,
            @PathParam("departmentCode") String departmentCode) {
        
        BusinessDataController controller = new BusinessDataController();
        return controller.downloadImageReport(request, reportType, imageId, date, departmentCode);
    }
    
    @POST
    @Path("/GetListSlide")
    @Consumes("application/x-www-form-urlencoded")
    public String getListSlide(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        BusinessDataController controller = new BusinessDataController();
        return controller.getListSilde(request, data, isSecurity);
    }
    
    @POST
    @Path("/GetImageReportDesc")
    @Consumes("application/x-www-form-urlencoded")
    public String getImageReportDesc(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        BusinessDataController controller = new BusinessDataController();
        return controller.getImageReportDesc(request, data, isSecurity);
    }
}
