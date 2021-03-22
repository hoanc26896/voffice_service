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

import com.viettel.voffice.controler.BriefManagementController;
import com.viettel.voffice.controler.CvGroupController;
import com.viettel.voffice.controler.DocumentController;
import com.viettel.voffice.controler.StoreTypeConfigController;

import org.json.JSONException;

/**
 * REST Web Service
 *
 */
@Path("TypeConfigAction")
public class StoreTypeConfigAction {

    /**
     * lay danh sach Store TYPE CONFIG
     *
     * @param request
     * @param data du lieu gui len
     * @param isSecurity 1 - Co ma hoa du lieu, 0 - Khong ma hoa du lieu
     * @return
     */
    @POST
    @Path("getListConfig")
    @Consumes("application/x-www-form-urlencoded")
    public String getListConfig(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        StoreTypeConfigController stc = new StoreTypeConfigController();
        return stc.getListConfig(isSecurity, data, request);
    }
    
    /**
     * lay danh sach Store TYPE CONFIG
     *
     * @param request
     * @param data du lieu gui len
     * @param isSecurity 1 - Co ma hoa du lieu, 0 - Khong ma hoa du lieu
     * @return
     * @throws JSONException 
     */
    @POST
    @Path("getListDocType")
    @Consumes("application/x-www-form-urlencoded")
    public String getListDocType(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        StoreTypeConfigController stc = new StoreTypeConfigController();
        return stc.getListDocType(isSecurity, data, request);
    }
    
    
    /**
     * insert user dc phan quyen doc ho so tai chinh vao db
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("addRolesDocType")
    @Consumes("application/x-www-form-urlencoded")
    public String addRolesDocType(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        StoreTypeConfigController stc = new StoreTypeConfigController();
        return stc.addRolesDocType(request, data, isSecurity);
    }
    
    /**
     * Update user dc phan quyen
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("updateRolesDocType")
    @Consumes("application/x-www-form-urlencoded")
    public String updateRolesDocType(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        StoreTypeConfigController stc = new StoreTypeConfigController();
        return stc.updateRolesDocType(request, data, isSecurity);
    }
    /**
     * Tim kiem / lay danh sach user dc phan quyen
     *
     * @param request
     * @param data du lieu gui len
     * @param isSecurity 1 - Co ma hoa du lieu, 0 - Khong ma hoa du lieu
     * @return
     * @throws JSONException 
     */
    @POST
    @Path("getListUserDocTypeRoles")
    @Consumes("application/x-www-form-urlencoded")
    public String getListUserDocTypeRoles(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        StoreTypeConfigController stc = new StoreTypeConfigController();
        return stc.getListUserDocTypeRoles(isSecurity, data, request);
    }

    /**
     * Xoa user dc phan quyen
     * @param req
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("deleteUserDocRoles")
    @Consumes("application/x-www-form-urlencoded")
    public String deleteUserDocRoles(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        StoreTypeConfigController stc = new StoreTypeConfigController();
        return stc.deleteUserDocRoles(req, data, isSecurity);
    }
    
    /**
     * lay thong tin chi tiet user roles cho man edit
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws JSONException 
     */
    @POST
    @Path("getUserEntity")
    @Consumes("application/x-www-form-urlencoded")
    public String getUserEntity(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        
        StoreTypeConfigController dc = new StoreTypeConfigController();
        return dc.getUserEntity(isSecurity, data, request);
    }
    
//    /**
//     * List danh sach ho so tai chinh
//     * @param request
//     * @param data
//     * @param isSecurity
//     * @return
//     * @throws JSONException
//     */
//    @POST
//    @Path("getListFinancialDoc")
//    @Consumes("application/x-www-form-urlencoded")
//    public String getListFinancialDoc(@Context HttpServletRequest request,
//            @FormParam("data") String data,
//            @FormParam("isSecurity") String isSecurity) throws JSONException {
//        StoreTypeConfigController stc = new StoreTypeConfigController();
//        return stc.getListFinancialDoc(isSecurity, data, request);
//    }
    
    
    /**
     * Lay danh sach RolesBy employeeId
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getUserDocRolesByEmpId")
    @Consumes("application/x-www-form-urlencoded")
    public String getUserDocRolesByEmpId(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        StoreTypeConfigController stc = new StoreTypeConfigController();
        return stc.getUserDocRolesByEmpId(isSecurity, data, request);
    }
    
    
    
   

   
}