/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import com.viettel.voffice.controler.signature.SignController;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.Path;
import javax.ws.rs.POST;

/**
 * REST Web Service
 *
 * @author thanght6
 */
@Path("Sign")
public class SignResource {

    public static final String ROOT_ACTION = "Sign";

    /**
     * <b>Kiem tra trang thai ky van ban truoc khi ky</b><br>
     * Phuc vu cho viec ky bang SIM CA do ky bang SIM CA cham
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("CheckSigningStatusForText")
    @Consumes("application/x-www-form-urlencoded")
    public String checkSigningStatusForText(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        SignController sc = new SignController();
        return sc.checkSigningStatusForText(request, data, isSecurity);
    }

    /**
     * Thuc hien ky bang SIM CA
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("SignByCASIM")
    @Consumes("application/x-www-form-urlencoded")
    public String signByCASIM(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        SignController sc = new SignController();
        return sc.signTaskByCASIM(request, data, isSecurity);
    }

    /**
     * Thuc hien ky mem
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("SignSoft")
    @Consumes("application/x-www-form-urlencoded")
    public String signSoft(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        SignController sc = new SignController();
        return sc.signSoftTask(request, data, isSecurity);
    }

    //==================thuc hien ky nhieu file======================
    /**
     * Thuc hien hash nhieu file
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws Throwable 
     */
    @POST
    @Path("SignSoftHashMutiFile")
    @Consumes("application/x-www-form-urlencoded")
    public String hashMutiFile(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws Throwable {
        SignController sc = new SignController();
        return sc.hashListFile(request, data, isSecurity);
    }

    @POST
    @Path("SignSoftAttachMutiFile")
    @Consumes("application/x-www-form-urlencoded")
    public String signSoftAttachMutiFile(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        SignController sc = new SignController();
        return sc.appendSignatureIntoListFile(request, data, isSecurity);
    }

    /**
     * <b>Ky van ban bang sim CA</b><br>
     *
     * @author thanght6
     * @since Jun 21, 2016
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("SignTextByCASIM")
    @Consumes("application/x-www-form-urlencoded")
    public String signTextByCASIM(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        SignController sc = new SignController();
        return sc.signTextByCASIM(request, data, isSecurity);
    }

    /**
     * <b>Lay thong tin chung thu</b><br>
     *
     * @author luanvd
     * @since Jul 12, 2016
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getP12CertInformation")
    @Consumes("application/x-www-form-urlencoded")
    public String getP12CertInformation(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        SignController sc = new SignController();
        return sc.getP12CertInformation(request, data, isSecurity);
    }

    /**
     * <b>Yeu cau reset mat khau chung thu</b><br>
     *
     * @author thanght6
     * @since Aug 18, 2016
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("RequestResetCertificatePassword")
    @Consumes("application/x-www-form-urlencoded")
    public String requestResetCertificatePassword(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        SignController sc = new SignController();
        return sc.requestResetCertificatePassword(request, data, isSecurity);
    }

    /**
     * <b>Reset mat khau chung thu</b><br>
     *
     * @author thanght6
     * @since Aug 19, 2016
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("ConfirmOTPCodeToResetCertificatePassword")
    @Consumes("application/x-www-form-urlencoded")
    public String confirmOTPCodeToResetCertificatePassword(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        SignController sc = new SignController();
        return sc.confirmOTPCodeToResetCertificatePassword(request, data, isSecurity);
    }

    /**
     * <b>Ky phieu giao viec/Phieu danh gia</b><br>
     *
     * @since Feb 03, 2017
     * @author hanh
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("signMultiFileTask")
    @Consumes("application/x-www-form-urlencoded")
    public String signMultiFileTask(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        SignController sc = new SignController();
        return sc.signMultiFileTask(request, data);
    }
    /***
     * TungHD dong dau file trong man ho so
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws Throwable
     */
    @POST
    @Path("SignSoftHashMutiFileBrief")
    @Consumes("application/x-www-form-urlencoded")
    public String hashMutiFileBrief(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws Throwable {
        SignController sc = new SignController();
        return sc.hashListFileBrief(request, data, isSecurity);
    }
    
    /**
     * TungHD insert thong tin sau khi dong dau man ho so
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("SignSoftAttachMutiFileBrief")
    @Consumes("application/x-www-form-urlencoded")
    public String signSoftAttachMutiFileBrief(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        SignController sc = new SignController();
        return sc.appendSignatureIntoListFileBrief(request, data, isSecurity);
    }
    
    /**
     * TungHD dong dau file trong man danh sach van ban
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws Throwable
     */
    @POST
    @Path("SignSoftHashMutiFileDoc")
    @Consumes("application/x-www-form-urlencoded")
    public String hashMutiFileDoc(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws Throwable {
        SignController sc = new SignController();
        return sc.hashListFileDoc(request, data, isSecurity);
    }
    
    /**
     * TungHD insert thong tin sau khi dong dau man van ban
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("SignSoftAttachMutiFileDoc")
    @Consumes("application/x-www-form-urlencoded")
    public String signSoftAttachMutiFileDoc(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        SignController sc = new SignController();
        return sc.appendSignatureIntoListFileDoc(request, data, isSecurity);
    }
}