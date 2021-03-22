/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import com.viettel.voffice.controler.CertManagementController;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Tao va quan ly chung thu mem tren mobile
 * @author DATNV5, mobile:  0986565786
 */
@Path("CertManagementAction")
public class CertManagementAction {
    
    /**
     * lay trang thai chung thu hien tai cua nguoi dung dang nhap vao ung dung
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    @POST
    @Path("getCertStateNow")
    @Produces(MediaType.APPLICATION_JSON)
    public String getCertStateNow(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        CertManagementController certManagementController = new CertManagementController();
        return certManagementController.getCertStateNow(request, data);
    }
    
    /**
     * 2. tạo file certificate
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    @POST
    @Path("makeCert")
    @Produces(MediaType.APPLICATION_JSON)
    public String makeCert(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        CertManagementController certManagementController = new CertManagementController();
        return certManagementController.makeCert(request, data);
    }
    
    /**
     * 3. xac nhan giao dich dang ky chu ky dien tu thong qua ma xac thuc OTP
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    @POST
    @Path("confirmTransactionOtp")
    @Produces(MediaType.APPLICATION_JSON)
    public String confirmTransactionOtp(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        CertManagementController certManagementController = new CertManagementController();
        return certManagementController.confirmTransactionOtp(request, data);
    }
    
    /**
     * 3. xac nhan giao dich dang ky chu ky dien tu thong qua ma xac thuc OTP
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    @POST
    @Path("activateCert")
    @Produces(MediaType.APPLICATION_JSON)
    public String activateCert(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        CertManagementController certManagementController = new CertManagementController();
        return certManagementController.activeCert(request, data);
    }
    
    /**
     * 3. xac nhan giao dich dang ky chu ky dien tu thong qua ma xac thuc OTP
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    @POST
    @Path("alterIdentification")
    @Produces(MediaType.APPLICATION_JSON)
    public String alterIdentification(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        CertManagementController certManagementController = new CertManagementController();
        return certManagementController.alterIdentification(request, data);
    }
    
    /**
     * xu ly trong cac truong hop
     * 1. quen mat khau chung thu 
     * 2. gia han chung thu
     * 3. huy chung thu tren client
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    @POST
    @Path("extendCert")
    @Produces(MediaType.APPLICATION_JSON)
    public String extendCert(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        CertManagementController certManagementController = new CertManagementController();
        return certManagementController.addExtendCert(request, data);
    }
    /**
     * huy gia han
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    @POST
    @Path("cancelExtendCert")
    @Produces(MediaType.APPLICATION_JSON)
    public String cancelExtendCert(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        CertManagementController certManagementController = new CertManagementController();
        return certManagementController.cancelExtendCert(request, data);
    }
    /**
     * kiem tra co du dieu kien gia han chung thu khong
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    @POST
    @Path("getExtendCertStatus")
    @Produces(MediaType.APPLICATION_JSON)
    public String getExtendCertStatus(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        CertManagementController certManagementController = new CertManagementController();
        return certManagementController.getExtendCertStatus(request, data);
    }
    
    /**
     * Huy chung thu tu thiet bi mobile
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    @POST
    @Path("cancelCert")
    @Produces(MediaType.APPLICATION_JSON)
    public String cancelCert(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        CertManagementController certManagementController = new CertManagementController();
        return certManagementController.cancelCert(request, data);
    }
    
    /**
     * <b>Backup</b>
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    @POST
    @Path("BackupCert")
    @Produces(MediaType.APPLICATION_JSON)
    public String backupCert(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        CertManagementController controller = new CertManagementController();
        return controller.backupCert(request, data);
    }
    
    
    //=========Bo sung gia han chung thu tu dong==============
    /**
     * Thuc hien download file pdf thong tin 
     * @param req
     * @param data
     * @param isSecurity
     * @return 
     */
    @POST
    @Path("/DownloadFileInfoUser")
    @Consumes("application/x-www-form-urlencoded")
    public Response DownloadFileInfoUser(
            @Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
//        System.out.println("==============1=================");
        CertManagementController certManagementController = new CertManagementController();
        Response result = certManagementController.DownloadFileInfoUser(req, data);
        return result;
    }
    
    /**
     * Ky file ho so
     * @param req
     * @param data
     * @param isSecurity
     * @return 
     */
    @POST
    @Path("/signFileExtentCa")
    @Consumes("application/x-www-form-urlencoded")
    public String signFileExtentCa(
            @Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        CertManagementController certManagementController = new CertManagementController();
        String result = certManagementController.signFileExtentCa(req, data);
        return result;
    }
    
    /**
     * 
     * @param req
     * @param data
     * @param isSecurity
     * @return 
     */
    @POST
    @Path("/getCodeCert")
    @Consumes("application/x-www-form-urlencoded")
    public String getCodeCert(
            @Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        CertManagementController certManagementController = new CertManagementController();
        String result = certManagementController.getCodeCert(req, data);
        return result;
    }
}
