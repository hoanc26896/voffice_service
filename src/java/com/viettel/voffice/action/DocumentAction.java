package com.viettel.voffice.action;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.controler.DocumentController;
import com.viettel.voffice.controler.TextController;
import com.viettel.voffice.controler.StoreTypeConfigController;
import com.viettel.voffice.utils.LogUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.json.JSONException;

/**
 *
 * @author kiennt45
 */
@Path("DocumentAction")
public class DocumentAction {

    /**
     * <b>Them moi van ban</b><br/>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("AddDocument")
    @Consumes("application/x-www-form-urlencoded")
    public String addDocument(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.addDocument(request, data, isSecurity);
    }

    /**
     * <b>Sua van ban</b><br/>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("EditDocument")
    @Consumes("application/x-www-form-urlencoded")
    public String editDocument(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.editDocument(request, data, isSecurity);
    }

    /**
     * <b>Xoa van ban</b><br/>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("DeleteDocument")
    @Consumes("application/x-www-form-urlencoded")
    public String deleteDocument(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.deleteDocument(request, data, isSecurity);
    }

    /**
     * Thay doi trang thai trinh kys
     *
     * @param req
     * @param data
     * @param strSecurity
     * @return
     */
    @POST
    @Path("actionSearchDocViewLibrary")
    @Consumes("application/x-www-form-urlencoded")
    public String actionSearchDocViewLibrary(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        
        DocumentController controller = new DocumentController();
        return controller.actionSearchDocViewLibrary(strSecurity, data, req);
    }

    /**
     * Cong bo thu cong
     *
     * @author thanght6
     * @since 2016-02-25
     * @param request Doi tuong request tu client -> server
     * @param data Gia tri data trong request
     * @param isSecurity Gia tri isSecurity trong request
     * @return
     */
    @POST
    @Path("publish")
    @Consumes("application/x-www-form-urlencoded")
    public String publish(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.publish(request, data, isSecurity);
    }

    /**
     * Sua thong tin cong bo
     *
     * @author thanght6
     * @since 2016-02-25
     * @param request Doi tuong request tu client -> server
     * @param data Gia tri data trong request
     * @param isSecurity Gia tri isSecurity trong request
     * @return
     */
    @POST
    @Path("editPublicationInformation")
    @Consumes("application/x-www-form-urlencoded")
    public String editPublicationInformation(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        DocumentController dc = new DocumentController();
        return dc.editPublicationInformation(request, data, isSecurity);
    }

    @POST
    @Path("editTmpPublicationInformation")
    @Consumes("application/x-www-form-urlencoded")
    public String editTmpPublicationInformation(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.editTmpPublicationInformation(request, data, isSecurity);
    }

    /**
     * Huy cong bo van ban
     *
     * @autor thanght6
     * @since 2016-02-25
     * @param request Doi tuong request tu client -> server
     * @param data Gia tri data trong request
     * @param isSecurity Gia tri isSecurity trong request
     * @return
     */
    @POST
    @Path("cancelPublish")
    @Consumes("application/x-www-form-urlencoded")
    public String cancelPublish(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        DocumentController dc = new DocumentController();
        return dc.cancelPublish(request, data, isSecurity);
    }

    /**
     * <b>Tim kiem van ban</b><br/>
     *
     * @autor thanght6
     * @since May 20, 2016
     * @param request Doi tuong request tu client -> server
     * @param data Gia tri data trong request
     * @param isSecurity Gia tri isSecurity trong request
     * @return
     */
    @POST
    @Path("countDocument")
    @Consumes("application/x-www-form-urlencoded")
    public String countDocument(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.countDocument(request, data, isSecurity);
    }

    /**
     * <b>Tim kiem van ban</b><br/>
     *
     * @autor thanght6
     * @since May 20, 2016
     * @param request Doi tuong request tu client -> server
     * @param data Gia tri data trong request
     * @param isSecurity Gia tri isSecurity trong request
     * @return
     */
    @POST
    @Path("search")
    @Consumes("application/x-www-form-urlencoded")
    public String search(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.search(request, data, isSecurity);
    }

    /**
     * <b>Lay chi tiet van ban</b><br/>
     *
     * @autor thanght6
     * @since May 24, 2016
     * @param request Doi tuong request tu client -> server
     * @param data Gia tri data trong request
     * @param isSecurity Gia tri isSecurity trong request
     * @return
     */
    @POST
    @Path("getDocumentDetail")
    @Consumes("application/x-www-form-urlencoded")
    public String getDocumentDetail(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.getDocumentDetail(request, data, isSecurity);
    }

    /**
     * <b>Lay danh sach nguoi nhan van ban</b><br/>
     *
     * @autor thanght6
     * @since May 25, 2016
     * @param request Doi tuong request tu client -> server
     * @param data Gia tri data trong request
     * @param isSecurity Gia tri isSecurity trong request
     * @return
     */
    @POST
    @Path("getListReceiver")
    @Consumes("application/x-www-form-urlencoded")
    public String getListReceiver(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        DocumentController dc = new DocumentController();
        String result = dc.getListReceiver(request, data, isSecurity);
        return result;
    }

    /**
     * <b>Lay danh sach don vi nhan van ban</b><br/>
     *
     * @autor thanght6
     * @since May 25, 2016
     * @param request Doi tuong request tu client -> server
     * @param data Gia tri data trong request
     * @param isSecurity Gia tri isSecurity trong request
     * @return
     */
    @POST
    @Path("getListGroup")
    @Consumes("application/x-www-form-urlencoded")
    public String getListGroup(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        DocumentController dc = new DocumentController();
        String result = dc.getListGroup(request, data, isSecurity);
        return result;
    }

    @POST
    @Path("sendDocumentToStaff")
    @Consumes("application/x-www-form-urlencoded")
    public String sendDocumentToStaff(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.sendDocumentToStaff(request, data, isSecurity);
    }

    @POST
    @Path("sendDocumentToGroup")
    @Consumes("application/x-www-form-urlencoded")
    public String sendDocumentToGroup(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.sendDocumentToGroup(request, data, isSecurity);
    }

    @POST
    @Path("sendDocumentToListPersonalGroup")
    @Consumes("application/x-www-form-urlencoded")
    public String sendDocumentToListPersonalGroup(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.sendDocumentToListPersonalGroup(request, data, isSecurity);
    }

    @POST
    @Path("updateDocumentProcessing")
    @Consumes("application/x-www-form-urlencoded")
    public String updateDocumentProcessing(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.updateDocumentProcessing(request, data, isSecurity);
    }

    /* cap nhat trang thai thu hoi van ban
     *
     */
    @POST
    @Path("updateStatusDocumentInStaff")
    @Consumes("application/x-www-form-urlencoded")
    public String updateStatusDocumentInStaff(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.updateStatusDocumentInStaff(request, data, isSecurity);
    }


    /*
     * Hien thi danh sach nhan vien trong nhom da chuyen van ban
     */
    @POST
    @Path("getListReceivedStaffInPersonalGroup")
    @Consumes("application/x-www-form-urlencoded")
    public String getListReceivedStaffInPersonalGroup(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.getListReceivedStaffInPersonalGroup(request, data, isSecurity);
    }

    /*
     * Hien thi danh sach nhom ca nhan da nhan van ban
     */

    @POST
    @Path("getListReceivedPersonalGroup")
    @Consumes("application/x-www-form-urlencoded")
    public String getListReceivedPersonalGroup(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.getListReceivedPersonalGroup(request, data, isSecurity);
    }

    /*
     * Hien thi danh sach ca nhan trong don vi da chuyen
     */

    @POST
    @Path("getListReceivedStaffInDepartment")
    @Consumes("application/x-www-form-urlencoded")
    public String getListReceivedStaffInDepartment(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.getListReceivedStaffInDepartment(request, data, isSecurity);
    }

    /**
     * <b>Them moi pham vi cong bo</b><br/>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("addDocumentScope")
    @Consumes("application/x-www-form-urlencoded")
    public String addDocumentScope(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.addDocumentScope(request, data, isSecurity);
    }

    /**
     * <b>Tim kiem pham vi cong bo</b><br/>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("searchDocumentScope")
    @Consumes("application/x-www-form-urlencoded")
    public String searchDocumentScope(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        DocumentController dc = new DocumentController();
        return dc.searchDocumentScope(request, data, isSecurity);
    }

    /**
     * <b>Xoa pham vi cong bo</b><br/>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("deleteDocumentScope")
    @Consumes("application/x-www-form-urlencoded")
    public String deleteDocumentScope(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.deleteDocumentScope(request, data, isSecurity);
    }

    @POST
    @Path("findDocScopeREFByTextId")
    @Consumes("application/x-www-form-urlencoded")
    public String findDocScopeREFByTextId(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.findDocScopeREFByTextId(request, data, isSecurity);
    }

    @POST
    @Path("getDocScopeLibrary")
    @Consumes("application/x-www-form-urlencoded")
    public String getDocScopeLibrary(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.getDocScopeLibrary(request, data, isSecurity);
    }

    @POST
    @Path("tranferTextPromulgateOrNotPromulgate")
    @Consumes("application/x-www-form-urlencoded")
    public String tranferTextPromulgateOrNotPromulgate(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.tranferTextPromulgateOrNotPromulgate(request, data, isSecurity);
    }

    @POST
    @Path("checkIsPublished")
    @Consumes("application/x-www-form-urlencoded")
    public String checkIsPublished(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.checkIsPublished(request, data, isSecurity);
    }

    @POST
    @Path("getPublishedStatus")
    @Consumes("application/x-www-form-urlencoded")
    public String getPublishedStatus(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.getPublishedStatus(request, data, isSecurity);
    }

    @POST
    @Path("getAdjacentListByDocumentId")
    @Consumes("application/x-www-form-urlencoded")
    public String getAdjacentListByDocumentId(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.getAdjacentListByDocumentId(request, data, isSecurity);
    }

    @POST
    @Path("getDocumentAdjacentList")
    @Consumes("application/x-www-form-urlencoded")
    public String getDocumentAdjacentList(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.getDocumentAdjacentList(request, data, isSecurity);
    }

    @POST
    @Path("sendDocument")
    @Consumes("application/x-www-form-urlencoded")
    public String sendDocument(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.sendDocument(request, data, isSecurity);
    }

    @POST
    @Path("sendFinanceTextToStaff")
    @Consumes("application/x-www-form-urlencoded")
    public String sendFinanceTextToStaff(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.sendFinanceTextToStaff(request, data, isSecurity);
    }

    /**
     * <b>Lay thong tin trang thai van ban dung cho phien ban web</b><br/>
     *
     * @autor hiendv2
     * @since Sep 27, 2016
     * @param request Doi tuong request tu client -> server
     * @param data Gia tri data trong request
     * @param isSecurity Gia tri isSecurity trong request
     * @return
     */
    @POST
    @Path("getStatusDocumentInStaff")
    @Consumes("application/x-www-form-urlencoded")
    public String getStatusDocumentInStaff(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.getStatusDocumentInStaff(request, data, isSecurity);
    }

    /**
     * <b>xuat bao cao van ban tai chinh</b>
     *
     * @author cuongnv
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("exportFinanceText")
    @Consumes("application/x-www-form-urlencoded")
    public String exportFinanceText(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.exportFinanceText(request, data, isSecurity);
    }

    /**
     * <b>Cap nhat loai van ban don vi/ca nhan</b>
     *
     * @author cuongnv
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("saveDocumentType")
    @Consumes("application/x-www-form-urlencoded")
    public String saveDocumentType(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.saveDocumentType(request, data, isSecurity);
    }

    @POST
    @Path("getListAllFileDocAttach")
    @Consumes("application/x-www-form-urlencoded")
    public String getListAllFileDocAttach(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.getListAllFileDocAttach(request, data, isSecurity);
    }

    /**
     * kiem tra van ban cung nhan van ban trong don vi
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("checkIsOnlyReceiveDoc")
    @Consumes("application/x-www-form-urlencoded")
    public String checkIsOnlyReceiveDoc(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.checkIsOnlyReceiveDoc(request, data, isSecurity);
    }

    /**
     * Ham lay chi tiet xu ly van ban cua user cung nhan
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getProcessedDetailByUser")
    @Consumes("application/x-www-form-urlencoded")
    public String getProcessedDetailByUser(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.getProcessedDetailByUser(request, data, isSecurity);
    }

    /**
     * <b>Xuat bao cao luan chuyen van ban</b>
     *
     * @author cuongnv
     * @since 25/2/2017
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("exportCirculationTree")
    @Consumes("application/x-www-form-urlencoded")
    public String exportCirculationTree(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.exportCirculationTree(request, data, isSecurity);
    }

    /**
     * <b>Kiem tra van ban co ton tai theo user</b><br/>
     *
     * @author hiendv2
     * @since May 25, 2017
     * @param document_id: Id van ban
     * @param userId: id nguoi tao, nhan vanban
     * @return
     */
    @POST
    @Path("getListFileAttachDocByUserId")
    @Consumes("application/x-www-form-urlencoded")
    public String getListFileAttachDocByUserId(@Context HttpServletRequest request,
            @FormParam("data") String data) {
        DocumentController dc = new DocumentController();
        String result = dc.getListFileAttachDocByUserId(request, data);
        return result;
    }

/**
     * <b>Lay danh sach nguoi nhan van ban</b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListStaffReceiveFromDoc")
    @Consumes("application/x-www-form-urlencoded")
    public String getListStaffReceiveFromDoc(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.getListStaffReceiveFromDoc(request, data, isSecurity);
    }

    
    /**
     * <b>Chia nho van ban</b><br>
     * 
     * @param request
     * @param data
     * @return 
     */
    @POST
    @Path("SplitDocument")
    @Consumes("application/x-www-form-urlencoded")
    public String splitDocument(
            @Context HttpServletRequest request,
            @FormParam("data") String data) {
        
        DocumentController controller = new DocumentController();
        return controller.splitDocument(request, data);
    }

    /**
     * <b>Danh sach comment chuyen van ban</b><br>
     * 
     * @param request
     * @param data
     * @return
     * @throws Exception 
     */
    @POST
    @Path("getListCommentFromDocument")
    @Consumes("application/x-www-form-urlencoded")
    public String getListCommentFromDocument(@Context HttpServletRequest request,
            @FormParam("data") String data) throws Exception {
        
        DocumentController dc = new DocumentController();
        return dc.getListCommentFromDocument(request, data);
    }
    
    @POST
    @Path("GetRegisterNumberIndex")
    @Consumes("application/x-www-form-urlencoded")
    public String getRegisterNumberIndex(
            @Context HttpServletRequest request,
            @FormParam("data") String data) throws Exception {
        
        DocumentController controller = new DocumentController();
        return controller.getRegisterNumberIndex(request, data);
    }

    /**
     * <b>Bo sung van ban</b><br>
     * 
     * @param request
     * @param data
     * @return 
     */
    @POST
    @Path("ExtendDocument")
    @Consumes("application/x-www-form-urlencoded")
    public String extendDocument(
            @Context HttpServletRequest request,
            @FormParam("data") String data) {
        
        DocumentController controller = new DocumentController();
        return controller.extendDocument(request, data);
    }
    
    @POST
    @Path("ReportdocumentTransferHistory")
    @Consumes("application/x-www-form-urlencoded")
    public String reportdocumentTransferHistory(
            @Context HttpServletRequest request,
            @FormParam("data") String data) {
        
        DocumentController controller = new DocumentController();
        return controller.reportdocumentTransferHistory(request, data);
    }
    
    @POST
    @Path("UpdateReadingStatus")
    @Consumes("application/x-www-form-urlencoded")
    public String updateReadingStatus(
            @Context HttpServletRequest request,
            @FormParam("data") String data) {
        
        DocumentController controller = new DocumentController();
        return controller.updateReadingStatus(request, data);
    }
    
    @POST
    @Path("getDocumentAttach")
    @Consumes("application/x-www-form-urlencoded")
    public String getDocumentAttach(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        DocumentController dc = new DocumentController();
        return dc.getDocumentAttach(request, data, isSecurity);
    }
    
    
    @POST
    @Path("checkPermitViewDocument")
    @Consumes("application/x-www-form-urlencoded")
    public String checkPermitViewDocument(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        DocumentController dc = new DocumentController();
        return dc.checkPermitViewDocument(request, data, isSecurity);
    }

    
    @POST
    @Path("getDocumentViewerUserId")
    @Consumes("application/x-www-form-urlencoded")
    public String getDocumentViewerUserId(
            @Context HttpServletRequest request,
            @FormParam("data") String data) {
        
        DocumentController controller = new DocumentController();
        return controller.getDocumentViewerUserId(request, data);
    }
    
    @POST
    @Path("checkPermissionDoc")
    @Consumes("application/x-www-form-urlencoded")
    public String checkPermissionDoc(
            @Context HttpServletRequest request,
            @FormParam("data") String data) {
        
        DocumentController controller = new DocumentController();
        return controller.checkPermissionDoc(request, data);
    }

    @POST
    @Path("getListOfficeOutside")
    @Consumes("application/x-www-form-urlencoded")
    public String getListOfficeOutside(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        DocumentController controller = new DocumentController();
        return controller.getListOfficeOutside(request, data, isSecurity);
    }
    
    @POST
    @Path("getListCommentLeaderFromDocument")
    @Consumes("application/x-www-form-urlencoded")
    public String getListCommentLeaderFromDocument(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        DocumentController controller = new DocumentController();
        return controller.getListCommentLeaderFromDocument(request, data);
    }
    
    /**
     * TungHd
     * Lay textID tu documentId
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getTextIdByDocumentId")
    @Consumes("application/x-www-form-urlencoded")
    public String getTextIdByDocumentId(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TextController tc = new TextController();
        String result = tc.getTextIdByDocumentId(request, data);
        return result;
    }
	
    /**
     * MinhNQ rollback dau xac nhan
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
	@POST
    @Path("rollBackDauXacNhan")
    @Consumes("application/x-www-form-urlencoded")
    public String rollBackDauXacNhan(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        DocumentController controller = new DocumentController();
        return controller.rollBackDauXacNhan(request, data);
    }

    /**
     * Tunghd add Tim kiem cho man Ho So Tai Chinh
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws Exception 
     */
    @POST
    @Path("searchFinancial")
    @Consumes("application/x-www-form-urlencoded")
    public String searchFinancial(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws Exception {
        
        DocumentController dc = new DocumentController();
        return dc.searchFinancial(request, data, isSecurity);
    }
    
    /**
     * Tunghd add unfollow Danh sach ho so tai chinh
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("unfollowDocument")
    @Consumes("application/x-www-form-urlencoded")
    public String unfollowDocument(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        DocumentController dc = new DocumentController();
        return dc.unfollowDocument(request, data, isSecurity);
    }
    
    @POST
    @Path("getDocumentStaffEntity")
    @Consumes("application/x-www-form-urlencoded")
    public String getDocumentStaffEntity(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        DocumentController dc = new DocumentController();
        return dc.getDocumentStaffEntity(isSecurity, data, request);
    }

}
