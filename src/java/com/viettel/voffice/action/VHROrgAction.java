/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import com.viettel.voffice.controler.VHROrgController;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import org.apache.log4j.Logger;

/**
 * REST Web Service
 *
 * @author voffice_guest04
 */
@Path("VHROrgAction")
public class VHROrgAction {

    private static final Logger LOGGER = Logger.getLogger(VHROrgAction.class);
    @Context
    private UriInfo context;

    /**
     * Creates a new instance of DocumentScopeDetailAction
     */
    public VHROrgAction() {
    }

    /**
     * Retrieves representation of an instance of
     * com.viettel.voffice.action.VHROrgAction
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return an instance of java.lang.String
     */
    // Lay pham vi don vi con truc thuoc cap lien ke ::cuongnv
    @POST
    @Path("getVHROrg")
    @Consumes("application/x-www-form-urlencoded")
    public String getVHROrg(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        VHROrgController vhr = new VHROrgController();
        return vhr.getVHROrg(request, data, isSecurity);
    }

    /**
     * <b>Danh sach vai tro</b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getSysRole")
    @Consumes("application/x-www-form-urlencoded")
    public String getSysRole(@Context HttpServletRequest request,
            @FormParam("data") String data, @FormParam("isSecurity") String isSecurity) {
        VHROrgController vhr = new VHROrgController();
        return vhr.getSysRole(request, data, isSecurity);
    }

    //edit by sonnd
    @POST
    @Path("getVhrLeaderByUserId")
    @Consumes("application/x-www-form-urlencoded")
    public String getVhrLeaderByUserId(@Context HttpServletRequest request,
            @FormParam("data") String data, @FormParam("isSecurity") String isSecurity) {
        VHROrgController vhrController = new VHROrgController();
        return vhrController.getVhrLeaderByUserId(request, data, isSecurity);
    }
    
    @POST
    @Path("validateAddScheduleLeader")
    @Consumes("application/x-www-form-urlencoded")
    public String validateAddScheduleLeader(@Context HttpServletRequest request,
            @FormParam("data") String data, @FormParam("isSecurity") String isSecurity) {
        VHROrgController vhrController = new VHROrgController();
        return vhrController.validateAddScheduleLeader(request, data, isSecurity);
    }
    
    @POST
    @Path("getVhrOrgUserAdminSchedule")
    @Consumes("application/x-www-form-urlencoded")
    public String getVhrOrgUserAdminSchedule(@Context HttpServletRequest request,
            @FormParam("data") String data, @FormParam("isSecurity") String isSecurity) {
        VHROrgController vhrController = new VHROrgController();
        return vhrController.getVhrOrgUserAdminSchedule(request, data, isSecurity);
    }
}