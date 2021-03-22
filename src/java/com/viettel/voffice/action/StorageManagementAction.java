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
import com.viettel.voffice.controler.StorageManagementController;

/**
 * REST Web Service
 * 
 * @author pm1_os30
 */
@Path("Storages")
public class StorageManagementAction {

    /**
     * Tim kiem/ lay danh sach kho
     *
     * @param request
     * @param data du lieu gui len
     * @param isSecurity 1 - Co ma hoa du lieu, 0 - Khong ma hoa du lieu
     * @return
     */
    @POST
    @Path("getListStorages")
    @Consumes("application/x-www-form-urlencoded")
    public String getListStorages(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        StorageManagementController oc = new StorageManagementController();
        return oc.getStorages(isSecurity, data, request);
    }

    /**
     * Xoa kho
     *
     * @param request
     * @param data du lieu gui len
     * @param isSecurity 1 - Co ma hoa du lieu, 0 - Khong ma hoa du lieu
     * @return
     */
    @POST
    @Path("deleteStorage")
    @Consumes("application/x-www-form-urlencoded")
    public String deleteStorages(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        StorageManagementController oc = new StorageManagementController();
        return oc.deleteStorages(request, data, isSecurity);
    }
    
    /**
     * tao moi hoac sua kho
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    @POST
    @Path("addOrEditStorage")
    @Consumes("application/x-www-form-urlencoded")
    public String addOrEditStorage(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        StorageManagementController oc = new StorageManagementController();
        return oc.addOrEditStorages(request, data, isSecurity);
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
    @Path("checkStorageExist")
    @Consumes("application/x-www-form-urlencoded")
    public String checkStorageExist(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        StorageManagementController oc = new StorageManagementController();
        return oc.checkStorageExist(request, data, isSecurity);
    }
    /**
     * lay dan sach don vi
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getOrgList")
    @Consumes("application/x-www-form-urlencoded")
    public String getOrgList(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        StorageManagementController oc = new StorageManagementController();
        return oc.getOrgList(isSecurity, data, request);
    }
    /**
     * lay du lieu bao cao kho
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getStorageReport")
    @Consumes("application/x-www-form-urlencoded")
    public String getStorageReport(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        StorageManagementController oc = new StorageManagementController();
        return oc.getStorageReport(isSecurity, data, request);
    }
    
    /**
     * check quyen luu tru
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("checkStoragePermissions")
    @Consumes("application/x-www-form-urlencoded")
    public String checkStoragePermissions(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        StorageManagementController oc = new StorageManagementController();
        return oc.checkStoragePermissions(request, data, isSecurity);
    }

    /**
     * danh sach don vi quyen luu tru
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListSysRoleOrg")
    @Consumes("application/x-www-form-urlencoded")
    public String getListSysRoleOrg(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        StorageManagementController oc = new StorageManagementController();
        return oc.getListSysRoleOrg(request, data, isSecurity);
        
    }
    //DuanNV them   danh sach don vi danh gia nhan vien
      /**
     * danh sach don vi danh gia nhan vien
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListSysRoleEvaluate")
    @Consumes("application/x-www-form-urlencoded")
    public String getListSysRoleEvaluate(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        StorageManagementController oc = new StorageManagementController();
        return oc.getListSysRoleEvaluate(request, data, isSecurity);
        
    }
}
