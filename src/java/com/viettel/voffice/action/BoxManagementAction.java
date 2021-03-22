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
import com.viettel.voffice.controler.BoxManagementController;

/**
 * REST Web Service
 *
 */
@Path("Boxs")
public class BoxManagementAction {

    /**
     * Tim kiem / lay danh sach hop
     *
     * @param request
     * @param data du lieu gui len
     * @param isSecurity 1 - Co ma hoa du lieu, 0 - Khong ma hoa du lieu
     * @return
     */
    @POST
    @Path("getListBoxs")
    @Consumes("application/x-www-form-urlencoded")
    public String getListBoxs(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        BoxManagementController oc = new BoxManagementController();
        return oc.getBoxs(isSecurity, data, request);
    }

    /**
     * Xoa hop
     *
     * @param request
     * @param data du lieu gui len
     * @param isSecurity 1 - Co ma hoa du lieu, 0 - Khong ma hoa du lieu
     * @return
     */
    @POST
    @Path("deleteBox")
    @Consumes("application/x-www-form-urlencoded")
    public String deleteBoxs(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        BoxManagementController oc = new BoxManagementController();
        return oc.deleteBoxs(request, data, isSecurity);
    }

    /**
     * tao moi hoac sua hop
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("addOrEditBox")
    @Consumes("application/x-www-form-urlencoded")
    public String addOrEditBox(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        BoxManagementController oc = new BoxManagementController();
        return oc.addOrEditBoxs(request, data, isSecurity);
    }

    /**
     * Lay danh sach hien thi combobox
     *
     * @param request
     * @param data du lieu gui len
     * @param isSecurity 1 - Co ma hoa du lieu, 0 - Khong ma hoa du lieu
     * @return
     */
    @POST
    @Path("getListForCombobox")
    @Consumes("application/x-www-form-urlencoded")
    public String getListForCombobox(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        BoxManagementController oc = new BoxManagementController();
        return oc.getListForCombobox(isSecurity, data, request);
    }
    
    /**
     * check trung ten hop
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("checkBoxExist")
    @Consumes("application/x-www-form-urlencoded")
    public String checkBoxExist(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        BoxManagementController oc = new BoxManagementController();
        return oc.checkBoxExist(request, data, isSecurity);
    }
}
