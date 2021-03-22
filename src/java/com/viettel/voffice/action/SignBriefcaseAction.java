/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import com.viettel.voffice.controler.SignBriefcaseControler;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

/**
 *
 * @author hanhnq21
 */
@Path("signBriefcaseAction")
public class SignBriefcaseAction {

    /**
     * them moi va sua cap trinh ky
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("addOrEditSignBriefcase")
    @Consumes("application/x-www-form-urlencoded")
    public String addOrEditSignBriefcase(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        SignBriefcaseControler dc = new SignBriefcaseControler();
        return dc.addOrEditSignBriefcase(request, data, isSecurity);
    }

    /**
     * xoa cap trinh ky
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("deleteSignBriefcase")
    @Consumes("application/x-www-form-urlencoded")
    public String deleteSignBriefcase(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        SignBriefcaseControler dc = new SignBriefcaseControler();
        return dc.deleteSignBriefcase(request, data, isSecurity);
    }

    /**
     * lay ma vach
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getBarcode")
    @Consumes("application/x-www-form-urlencoded")
    public String getBarcode(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        SignBriefcaseControler dc = new SignBriefcaseControler();
        return dc.getBarcode(request, data, isSecurity);
    }

    /**
     * lay danh sach lanh dao
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getLeaderOfAssitant")
    @Consumes("application/x-www-form-urlencoded")
    public String getLeaderOfAssitant(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        SignBriefcaseControler dc = new SignBriefcaseControler();
        return dc.getLeaderOfAssitant(request, data, isSecurity);
    }

    /**
     * lay danh sach cap trinh ky
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListSignBriefcase")
    @Consumes("application/x-www-form-urlencoded")
    public String getListSignBriefcase(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        SignBriefcaseControler dc = new SignBriefcaseControler();
        return dc.getListSignBriefcase(request, data, isSecurity);
    }

    /**
     * lay danh sach trang thai
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListSignBriefcaseStatus")
    @Consumes("application/x-www-form-urlencoded")
    public String getListSignBriefcaseStatus(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        SignBriefcaseControler dc = new SignBriefcaseControler();
        return dc.getListSignBriefcaseStatus(request, data, isSecurity);
    }

    /**
     * lay chi tiet cap trinh ky
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getSignBriefcaseDetail")
    @Consumes("application/x-www-form-urlencoded")
    public String getSignBriefcaseDetail(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        SignBriefcaseControler dc = new SignBriefcaseControler();
        return dc.getSignBriefcaseDetail(request, data, isSecurity);
    }

    /**
     * thay nguoi ky
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("updateSigner")
    @Consumes("application/x-www-form-urlencoded")
    public String updateSigner(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        SignBriefcaseControler dc = new SignBriefcaseControler();
        return dc.updateSigner(request, data, isSecurity);
    }

    /**
     * chuyen trang thai nguoi ky
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("updateStatusSignBriefcase")
    @Consumes("application/x-www-form-urlencoded")
    public String updateStatusSignBriefcase(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        SignBriefcaseControler dc = new SignBriefcaseControler();
        return dc.updateStatusSignBriefcase(request, data, isSecurity);
    }
}