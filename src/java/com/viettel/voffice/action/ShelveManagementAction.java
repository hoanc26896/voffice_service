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
import com.viettel.voffice.controler.ShelveManagementController;

/**
 * REST Web Service
 *
 */
@Path("Shelve")
public class ShelveManagementAction {

    /**
     * Tim kiem/ lay danh sach
     *
     * @param request
     * @param data du lieu gui len
     * @param isSecurity 1 - Co ma hoa du lieu, 0 - Khong ma hoa du lieu
     * @return
     */
    @POST
    @Path("getListShelve")
    @Consumes("application/x-www-form-urlencoded")
    public String getListShelve(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        ShelveManagementController oc = new ShelveManagementController();
        return oc.getShelve(isSecurity, data, request);
    }

    /**
     * Xoa du lieu
     *
     * @param request
     * @param data du lieu gui len
     * @param isSecurity 1 - Co ma hoa du lieu, 0 - Khong ma hoa du lieu
     * @return
     */
    @POST
    @Path("deleteShelve")
    @Consumes("application/x-www-form-urlencoded")
    public String deleteShelve(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        ShelveManagementController oc = new ShelveManagementController();
        return oc.deleteShelve(request, data, isSecurity);
    }

    /**
     * tao moi hoac sua du lieu
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("addOrEditShelve")
    @Consumes("application/x-www-form-urlencoded")
    public String addOrEditShelve(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        ShelveManagementController oc = new ShelveManagementController();
        return oc.addOrEditShelve(request, data, isSecurity);
    }
    /**
     * check ton tai ke
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("checkShelveExist")
    @Consumes("application/x-www-form-urlencoded")
    public String checkShelveExist(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        ShelveManagementController oc = new ShelveManagementController();
        return oc.checkShelveExist(request, data, isSecurity);
    }
    /**
     * check ton tai ke
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("checkShelveNumFloor")
    @Consumes("application/x-www-form-urlencoded")
    public String checkShelveNumFloor(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        ShelveManagementController oc = new ShelveManagementController();
        return oc.checkShelveNumFloor(request, data, isSecurity);
    }
}