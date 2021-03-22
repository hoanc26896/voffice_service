/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.controler.LogController;
import com.viettel.voffice.controler.UserControler;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * REST Web Service
 *
 * @author datnv5
 */
@Path("Authenticate")
public class AuthenticateResource {
    
    /**
     * <b>Tra ve phan hoi de client do toc do mang </b><br>
     *
     * @return
     */
    @POST
    @Path("PingNetwork")
    @Produces(MediaType.APPLICATION_JSON)
    public String pingNetwork() {
        return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, null, null);
    }

    @POST
    @Path("CheckServerStatus")
    @Produces(MediaType.APPLICATION_JSON)
    public String checkServerStatus(@Context HttpServletRequest request) {
        UserControler userControler = new UserControler();
        return userControler.checkServerStatus(request);
    }

    @POST
    @Path("getRsaKeyPublic")
    @Produces(MediaType.APPLICATION_JSON)
    public String getRsaKeyPublic(@Context HttpServletRequest req) {
        UserControler userClt = new UserControler();
        String strDataResponse = userClt.getTokenPublicKeyRSA(req);
        return strDataResponse;
    }

    /**
     * <b>Dang nhap</b><br>
     *
     * @param request
     * @param rsaPublicKey Key RSA cong khai
     * @param encodedAESKey Key AES da bi ma hoa bang key RSA cong khai
     * @param data Du lieu dang nhap
     * @param ios
     * @return
     */
    @POST
    @Path("login")
    @Consumes("application/x-www-form-urlencoded")
    public String login(@Context HttpServletRequest request,
            @FormParam("publicRsaKey") String rsaPublicKey,
            @FormParam("aesKey") String encodedAESKey,
            @FormParam("data") String data,
            @FormParam("isIos") String ios) {
        UserControler userClt = new UserControler();
        return userClt.login(request, rsaPublicKey, encodedAESKey, data, ios);
    }


    @POST
    @Path("logOut")
    @Consumes("application/x-www-form-urlencoded")
    public String logOut(@Context HttpServletRequest req) {
        String strDataResponse;
        UserControler userClt = new UserControler();
        strDataResponse = userClt.logOut(req);
        return strDataResponse;
    }

    /**
     * <b>Ma hoa account SSO cho SmartOffice</b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("EncryptAccountSSOForSmartOffice")
    @Consumes("application/x-www-form-urlencoded")
    public String encryptAccountSSOForSmartOffice(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        UserControler userControler = new UserControler();
        return userControler.encryptAccountSSOForSmartOffice(request, data, isSecurity);
    }

    /**
     * <b>Dong bo danh sach yeu thich</b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("SyncFavouriteList")
    @Consumes("application/x-www-form-urlencoded")
    public String syncFavouriteList(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        UserControler userControler = new UserControler();
        return userControler.syncFavouriteList(request, data, isSecurity);
    }

    /**
     * <b>Dang nhap qua Passport</b><br>
     *
     * @param request
     * @param rsaPublicKey Key RSA cong khai
     * @param encodedAESKey Key AES da bi ma hoa bang key RSA cong khai
     * @param data Du lieu dang nhap
     * @return
     */
    @POST
    @Path("LoginViaPassport")
    @Consumes("application/x-www-form-urlencoded")
    public String loginViaPassport(@Context HttpServletRequest request,
            @FormParam("publicRsaKey") String rsaPublicKey,
            @FormParam("aesKey") String encodedAESKey,
            @FormParam("data") String data) {
        UserControler userClt = new UserControler();
        return userClt.loginViaPassport(request, rsaPublicKey, encodedAESKey, data);
    }

    /**
     * Lay so luong dang nhap trong khoang thoi gian hien tai
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("checkThresholdPersonLogin")
    @Consumes("application/x-www-form-urlencoded")
    public String checkThresholdPersonLogin(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        LogController logController = new LogController();
        return logController.checkThresholdPersonLogin(request, data, isSecurity);
    }

    /**
     * ham lay ma loi dang nhap tap trung
     *
     * @param request
     * @param rsaPublicKey
     * @param encodedAESKey
     * @param data
     * @return
     */
    @POST
    @Path("checkLockAccountSSO")
    @Consumes("application/x-www-form-urlencoded")
    public String checkLockAccountSSO(@Context HttpServletRequest request,
            @FormParam("publicRsaKey") String rsaPublicKey,
            @FormParam("aesKey") String encodedAESKey,
            @FormParam("data") String data) {
        UserControler userClt = new UserControler();
        return userClt.checkLockAccountSSO(request, rsaPublicKey, encodedAESKey, data);
    }
    
    @POST
    @Path("changePass")
    @Consumes("application/x-www-form-urlencoded")
    public String changePass(@Context HttpServletRequest request,
            @FormParam("data") String data) {
        UserControler userClt = new UserControler();
        return userClt.changePass(request, data);
    }
    
    @POST
    @Path("ChangeLanguageInSession")
    @Consumes("application/x-www-form-urlencoded")
    public String changeLanguageInSession(@Context HttpServletRequest request,
            @FormParam("data") String data) {
        
        UserControler controller = new UserControler();
        return controller.changeLanguageInSession(request, data);
    }
    }
