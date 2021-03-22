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
import org.json.JSONException;

/**
 * REST Web Service
 *
 */
@Path("Brief")
public class BriefManagementAction {

    /**
     * Tim kiem / lay danh sach ho so
     *
     * @param request
     * @param data du lieu gui len
     * @param isSecurity 1 - Co ma hoa du lieu, 0 - Khong ma hoa du lieu
     * @return
     */
    @POST
    @Path("getListBrief")
    @Consumes("application/x-www-form-urlencoded")
    public String getListBrief(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        BriefManagementController oc = new BriefManagementController();
        return oc.getListBrief(isSecurity, data, request);
    }

    /**
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListBriefInfo")
    @Consumes("application/x-www-form-urlencoded")
    public String getListBriefInfo(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        BriefManagementController oc = new BriefManagementController();
        return oc.getListBriefInfo(request, data, isSecurity);
    }

    /**
     * Xoa ho so
     *
     * @param request
     * @param data du lieu gui len
     * @param isSecurity 1 - Co ma hoa du lieu, 0 - Khong ma hoa du lieu
     * @return
     */
    @POST
    @Path("deleteBrief")
    @Consumes("application/x-www-form-urlencoded")
    public String deleteBoxs(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        BriefManagementController oc = new BriefManagementController();
        return oc.deleteBriefs(request, data, isSecurity);
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
    @Path("addOrEditBrief")
    @Consumes("application/x-www-form-urlencoded")
    public String addOrEditBrief(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        BriefManagementController oc = new BriefManagementController();
        return oc.addOrEditBrief(request, data, isSecurity);
    }

    /**
     * Cho muon ho so
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("briefBorrow")
    @Consumes("application/x-www-form-urlencoded")
    public String briefBorrow(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        BriefManagementController oc = new BriefManagementController();
        return oc.briefBorrow(request, data, isSecurity);
    }

    /**
     * ban giao
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws org.json.JSONException
     */
    @POST
    @Path("processBrief")
    @Consumes("application/x-www-form-urlencoded")
    public String processBrief(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        BriefManagementController oc = new BriefManagementController();
        return oc.processBrief(request, data, isSecurity);
    }
    
     /**
     * muon phe duyet, tu choi
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws org.json.JSONException
     */
    @POST
    @Path("processBriefBorrow")
    @Consumes("application/x-www-form-urlencoded")
    public String processBriefBorrow(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        BriefManagementController oc = new BriefManagementController();
        return oc.processBriefBorrow(request, data, isSecurity);
    }

    /**
     * get Brief info For Update
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getBriefForUpdate")
    @Consumes("application/x-www-form-urlencoded")
    public String getBriefForUpdate(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        BriefManagementController oc = new BriefManagementController();
        return oc.getBriefForUpdate(request, data, isSecurity);
    }

    /**
     * get BriefDocument For Update
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getBriefDocumentForUpdate")
    @Consumes("application/x-www-form-urlencoded")
    public String getBriefDocumentForUpdate(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        BriefManagementController oc = new BriefManagementController();
        return oc.getBriefDocumentForUpdate(request, data, isSecurity);
    }
    
    /**
     * get Abbreviation Child And ParentOrg
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    @POST
    @Path("getAbbreviationChildAndParentOrg")
    @Consumes("application/x-www-form-urlencoded")
    public String getAbbreviationChildAndParentOrg(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        BriefManagementController oc = new BriefManagementController();
        return oc.getAbbreviationChildAndParentOrg(request, data, isSecurity);
    }

    /**
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListBriefBorrow")
    @Consumes("application/x-www-form-urlencoded")
    public String getListBriefBorrow(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        BriefManagementController oc = new BriefManagementController();
        return oc.getListBriefBorrow(isSecurity, data, request);
    }
    /**
     * Tim ten nganh
     *
     * @param request
     * @param data du lieu gui len
     * @param isSecurity 1 - Co ma hoa du lieu, 0 - Khong ma hoa du lieu
     * @return
     */
    @POST
    @Path("getAreaNameById")
    @Consumes("application/x-www-form-urlencoded")
    public String getAreaNameById(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        BriefManagementController oc = new BriefManagementController();
        return oc.getAreaNameById(isSecurity, data, request);
    }
    /**
     * Tim kiem/ lay danh sach ho so ban giao
     *
     * @param request
     * @param data du lieu gui len
     * @param isSecurity 1 - Co ma hoa du lieu, 0 - Khong ma hoa du lieu
     * @return
     */
    @POST
    @Path("getListReceivedBrief")
    @Consumes("application/x-www-form-urlencoded")
    public String getListReceivedBrief(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        BriefManagementController oc = new BriefManagementController();
        return oc.getListReceivedBrief(isSecurity, data, request);
    }
    /**
     * Get du lieu bao cao ho so
     *
     * @param request
     * @param data du lieu gui len
     * @param isSecurity 1 - Co ma hoa du lieu, 0 - Khong ma hoa du lieu
     * @return
     */
    @POST
    @Path("getDataToExport")
    @Consumes("application/x-www-form-urlencoded")
    public String getDataToExport(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        BriefManagementController oc = new BriefManagementController();
        return oc.getDataToExport(isSecurity, data, request);
    }
    
    
    /**
     * get HardStatus By BriefId
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws org.json.JSONException
     */
    @POST
    @Path("getHardStatusByBriefId")
    @Consumes("application/x-www-form-urlencoded")
    public String getHardStatusByBriefId(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        BriefManagementController oc = new BriefManagementController();
        return oc.getHardStatusByBriefId(request, data, isSecurity);
    }
    
    /**
     * get List Document History
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws org.json.JSONException
     */
    @POST
    @Path("getListDocumentHistory")
    @Consumes("application/x-www-form-urlencoded")
    public String getListDocumentHistory(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        BriefManagementController oc = new BriefManagementController();
        return oc.getListDocumentHistorys(isSecurity, data, request);
    }
    
    /**
     * check Exist Document
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws org.json.JSONException
     */
    @POST
    @Path("checkExistDocument")
    @Consumes("application/x-www-form-urlencoded")
    public String checkExistDocument(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        BriefManagementController oc = new BriefManagementController();
        return oc.checkExistDocument(request, data, isSecurity);
    }
    
    /**
     * check HardStatusBrief
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws org.json.JSONException
     */
    @POST
    @Path("checkHardStatusBrief")
    @Consumes("application/x-www-form-urlencoded")
    public String checkHardStatusBrief(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        BriefManagementController oc = new BriefManagementController();
        return oc.checkHardStatusBrief(request, data, isSecurity);
    }
    
    /**
     * kiem tra quyen xem chi tiet ho so
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws org.json.JSONException
     */
    @POST
    @Path("checkViewBriefInfoDetail")
    @Consumes("application/x-www-form-urlencoded")
    public String checkViewBriefInfoDetail(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        BriefManagementController oc = new BriefManagementController();
        return oc.checkViewBriefInfoDetail(request, data, isSecurity);
    }
    
    /**
     * kiem tra quyen muon ho so
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws org.json.JSONException
     */
    @POST
    @Path("checkViewBorrowBrief")
    @Consumes("application/x-www-form-urlencoded")
    public String checkViewBorrowBrief(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        BriefManagementController oc = new BriefManagementController();
        return oc.checkViewBorrowBrief(request, data, isSecurity);
    }
    
    /**
     * lay danh sach file dinh kem
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws org.json.JSONException
     */
    @POST
    @Path("getBriefFileAttachmentDocument")
    @Consumes("application/x-www-form-urlencoded")
    public String getBriefFileAttachmentDocument(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        BriefManagementController oc = new BriefManagementController();
        return oc.getBriefFileAttachmentDocument(request, data, isSecurity);
    }
    
     /**
     * lay danh sach don vi mac dinh
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws org.json.JSONException
     */
    @POST
    @Path("getOrgListDefaultByUserId")
    @Consumes("application/x-www-form-urlencoded")
    public String getOrgListDefaultByUserId(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        BriefManagementController oc = new BriefManagementController();
        return oc.getOrgListDefaultByUserId(request, data, isSecurity);
    }

    /**
     * Datdc Download noi dung file van ban tao trong ho so
     * getInfoFileBrief
     * @param req
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getInfoFileBrief")
    @Consumes("application/x-www-form-urlencoded")
    public String getInfoFileBrief(
            @Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        BriefManagementController fc = new BriefManagementController();
        String result = fc.getInfoFileBrief(req, data, isSecurity);
        return result;
    }
}