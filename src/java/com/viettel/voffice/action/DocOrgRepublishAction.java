/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.controler.DocOrgRepublishController;
import com.viettel.voffice.utils.LogUtils;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;

import javax.ws.rs.Path;
import javax.ws.rs.POST;

/**
 * REST Web Service
 *
 * @author voffice_guest1
 */
@Path("DocOrgRepublish")
public class DocOrgRepublishAction {

    public static final String ROOT_ACTION = "DocOrgRepublish";

    @POST
    @Path("getListOrganization")
    @Consumes("application/x-www-form-urlencoded")
    public String getListOrganization(@Context HttpServletRequest request, @FormParam("data") String data, @FormParam("isSecurity") String isSecurity) {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        DocOrgRepublishController dorc = new DocOrgRepublishController();
        return dorc.getListOrganization(request, data, isSecurity);
    }

    // Lay van ban goc ::cuongnv
    @POST
    @Path("getBaseDocument")
    @Consumes("application/x-www-form-urlencoded")
    public String getBaseDocument(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        DocOrgRepublishController dorc = new DocOrgRepublishController();
        return dorc.getBaseDocument(request, data, isSecurity);
    }
    
}
