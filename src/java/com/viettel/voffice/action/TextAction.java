/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.controler.DocumentController;
import com.viettel.voffice.controler.TextController;
import com.viettel.voffice.utils.LogUtils;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import org.json.JSONException;

/**
 *
 * @author vinhnq13
 */
@Path("textAction")
public class TextAction {

    public static final String ROOT_ACTION = "textAction";

    /**
     * Lay danh sach cong viec ca nhan
     *
     * @param request
     * @param data du lieu gui len
     * @author vinhnq13
     * @return
     */
    @POST
    @Path("getTextDetail")
    @Consumes("application/x-www-form-urlencoded")
    public String getTextDetail(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        TextController tc = new TextController();
        String result = tc.getTextDetailThread(request, data, isSecurity);
        return result;
    }

    /**
     * <b>Tim kiem van ban trinh ky</b><br>
     *
     * @author thanght6
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws JSONException
     */
    @POST
    @Path("searchText")
    @Consumes("application/x-www-form-urlencoded")
    public String searchText(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        TextController tc = new TextController();
        String result = tc.searchText(request, data, isSecurity);
        return result;
    }

    /**
     * Van thu tu choi xet duyet
     *
     * @author vinhnq13
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("rejectSignDocByVTAction")
    @Consumes("application/x-www-form-urlencoded")
    public String rejectSignDocByVTAction(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        TextController tc = new TextController();
        String result = tc.rejectSignDocByVTAction(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    /**
     * Chuyen ky nhay - Vinhnq13
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("transferToPreSigner")
    @Consumes("application/x-www-form-urlencoded")
    public String transferToPreSigner(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        TextController tc = new TextController();
        String result = tc.transferToPreSigner(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    /**
     * <b>Chinh sua thong tin van ban trinh ky khi van thu xet duyet</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("updateSigner")
    @Consumes("application/x-www-form-urlencoded")
    public String updateSigner(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TextController textController = new TextController();
        return textController.updateSigner(request, data, isSecurity);
    }

    /**
     * thienng1 thay nguoi ky khi dang ky
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("updateListSigner")
    @Consumes("application/x-www-form-urlencoded")
    public String updateListSigner(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TextController tc = new TextController();
        String result = tc.updateListSigner(request, data, isSecurity);
        return result;
    }

    /**
     * thienng1 Ban hanh van ban
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("documentPromulgate")
    @Consumes("application/x-www-form-urlencoded")
    public String documentPromulgate(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        TextController tc = new TextController();
        String result = tc.documentPromulgate(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    /**
     * thienng1 Huy ban hanh van ban
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("cancelDocumentPublish")
    @Consumes("application/x-www-form-urlencoded")
    public String cancelDocumentPublish(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        TextController tc = new TextController();
        String result = tc.cancelDocumentPublish(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    /**
     * hiendv2 Cap nhat trang thai sau ky hoac xet duyet khong USB tren WEb
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("updateDatabaseSign")
    @Consumes("application/x-www-form-urlencoded")
    public String updateDatabaseSign(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        TextController tc = new TextController();
        String result = tc.updateDatabaseSign(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    /**
     * vinhnq13 Tu choi ky
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("rejectSignDocument")
    @Consumes("application/x-www-form-urlencoded")
    public String rejectSignDocument(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        TextController tc = new TextController();
        String result = tc.rejectSignDocument(request, data, isSecurity);
        return result;
    }

    /**
     * Huy ban hanh van ban
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws org.json.JSONException
     */
    @POST
    @Path("getListSigner")
    @Consumes("application/x-www-form-urlencoded")
    public String getListSigner(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        TextController tc = new TextController();
        String result = tc.getListSigner(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    /**
     * Dem so luong van ban menu trai
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws org.json.JSONException
     */
    @POST
    @Path("countTextSignAll")
    @Consumes("application/x-www-form-urlencoded")
    public String countTextSignAll(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        TextController tc = new TextController();
        String result = tc.countTextSignAll(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    /**
     * Dem so luong van ban menu trai
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws org.json.JSONException
     */
    @POST
    @Path("getCountHome")
    @Consumes("application/x-www-form-urlencoded")
    public String getCountHome(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        TextController tc = new TextController();
        String result = tc.getCountHome(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    /**
     * Dem so luong van ban man hinh home wweb
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws org.json.JSONException
     */
    @POST
    @Path("getCountTextDashboard")
    @Consumes("application/x-www-form-urlencoded")
    public String getCountTextDashboard(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        TextController tc = new TextController();
        String result = tc.getCountTextDashboard(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    /**
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws JSONException
     */
    @POST
    @Path("synchonizeCertificate")
    @Consumes("application/x-www-form-urlencoded")
    public String synchonizeCertificate(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        TextController tc = new TextController();
        String result = tc.synchonizeCertificate(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    /**
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws JSONException
     */
    @POST
    @Path("getCertificateSynchronization")
    @Consumes("application/x-www-form-urlencoded")
    public String getCertificateSynchronization(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        TextController tc = new TextController();
        String result = tc.getCertificateSynchronization(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    /**
     * lay so dang ky van ban tu dong
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws JSONException
     */
    @POST
    @Path("getListRegisterNumber")
    @Consumes("application/x-www-form-urlencoded")
    public String getListRegisterNumber(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        TextController tc = new TextController();
        String result = tc.getListRegisterNumber(request, data, isSecurity);
        return result;
    }

    /**
     * lay danh sach nguoi ky tiep theo
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws JSONException
     */
    @POST
    @Path("getListSignerNext")
    @Consumes("application/x-www-form-urlencoded")
    public String getListSignerNext(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        TextController tc = new TextController();
        String result = tc.getListSignerNext(request, data, isSecurity);
        return result;
    }

    /**
     * Van thu Thay anh nguoi ky
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("updateSignImageBySecrectary")
    @Consumes("application/x-www-form-urlencoded")
    public String updateSignImageBySecrectary(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TextController tc = new TextController();
        String result = tc.updateSignImageBySecrectary(request, data, isSecurity);
        return result;
    }

    @POST
    @Path("IdentifyObjectByUser")
    @Consumes("application/x-www-form-urlencoded")
    public String identifyObjectByUser(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        TextController controller = new TextController();
        return controller.identifyObjectByUser(request, data, isSecurity);
    }

    @POST
    @Path("AddAttachmentForText")
    @Consumes("application/x-www-form-urlencoded")
    public String addAttachmentForText(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        TextController controller = new TextController();
        return controller.addAttachmentForText(request, data, isSecurity);
    }
    
    /**
     * <b>Lay lich su doi nguoi ky</b>
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    @POST
    @Path("GetHistoryOfSignerChange")
    @Consumes("application/x-www-form-urlencoded")
    public String getHistoryOfSignerChange(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        TextController controller = new TextController();
        return controller.getHistoryOfSignerChange(request, data, isSecurity);
    }
    
    /**
     * <b>Lay lich su doi nguoi ky</b>
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    @POST
    @Path("CheckTextWaitingForSignOfUser")
    @Consumes("application/x-www-form-urlencoded")
    public String checkTextWaitingForSignOfUser(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        TextController controller = new TextController();
        return controller.checkTextWaitingForSignOfUser(request, data);
    }
    
    // 201812-Pitagon: add
    @POST
    @Path("getOrgMarkedList")
    @Consumes("application/x-www-form-urlencoded")
    public String getOrgMarkedList(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        TextController controller = new TextController();
        return controller.getOrgMarkedList(request, data);
    }
    
    // 201812-Pitagon: add
    @POST
    @Path("askForSeal")
    @Consumes("application/x-www-form-urlencoded")
    public String askForSeal(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        TextController controller = new TextController();
        return controller.askForSeal(request, data);
    }
    
    // 201812-Pitagon: add
    @POST
    @Path("rejectMark")
    @Consumes("application/x-www-form-urlencoded")
    public String rejectMark(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        TextController controller = new TextController();
        return controller.rejectMark(request, data);
    }
    
    // 201812-Pitagon: add
    @POST
    @Path("markDocumentByOrg")
    @Consumes("application/x-www-form-urlencoded")
    public String markDocumentByOrg(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        TextController controller = new TextController();
        return controller.markDocumentByOrg(request, data);
    }
    
    /**
     * @author TungHD
     * Xu ly dong dau xac nhan
     * markDocumentByOrgForConfirm
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("markDocumentByOrgForConfirm")
    @Consumes("application/x-www-form-urlencoded")
    public String markDocumentByOrgForConfirm(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        TextController controller = new TextController();
        return controller.markDocumentByOrgForConfirm(request, data);
    }
    
    // 201901-Pitagon: add
    @POST
    @Path("getListOrgPermissionMark")
    @Consumes("application/x-www-form-urlencoded")
    public String getListOrgPermissionMark(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        TextController controller = new TextController();
        return controller.getListOrgPermissionMark(request, data);
    }

    /**
     * @author TungHd
     * Dong dau trong ho so
     * markDocumentByOrgForBrief
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("markDocumentByOrgForBrief")
    @Consumes("application/x-www-form-urlencoded")
    public String markDocumentByOrgForBrief(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        TextController controller = new TextController();
        return controller.markDocumentByOrgForBrief(request, data);
    }
	
    /**
     * @author MinhNQ
     * rollback con dau don vi
     * rollBackDauDonVi
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
	@POST
    @Path("rollBackDauDonVi")
    @Consumes("application/x-www-form-urlencoded")
    public String rollBackDauDonVi(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
    	TextController controller = new TextController();
        return controller.rollBackDauDonVi(request, data);
    }
	/**
	 * @author MinhNQ
	 * rollback con dau trong ho so
	 * @param request
	 * @param data
	 * @param isSecurity
	 * @return
	 */
	@POST
    @Path("rollBackBrief")
    @Consumes("application/x-www-form-urlencoded")
    public String rollBackBrief(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
    	TextController controller = new TextController();
        return controller.rollBackBrief(request, data);
    }
	
    
   
}
