
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import com.viettel.voffice.controler.TaskController;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import org.json.JSONException;

/**
 *
 * @author vinhnq13
 */
@Path("taskAction")
public class TaskAction {

    /**
     * Lay danh sach cong viec ca nhan
     *
     * @param request
     * @param data du lieu gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListTask")
    @Consumes("application/x-www-form-urlencoded")
    public String getListTask(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        
        TaskController controller = new TaskController();
        return controller.getListTask(request, data, isSecurity);
    }

    /**
     * Lay danh sach cong viec ca nhan duoc tao tu nhiem vu
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListTaskFromMission")
    @Consumes("application/x-www-form-urlencoded")
    public String getListTaskFromMission(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        TaskController tc = new TaskController();
        String result = tc.getListTaskFromMission(request, data, isSecurity);
        return result;
    }

    /**
     * Lay chi tiet cong viec
     *
     * @param request
     * @param data du lieu gui len
     * @return
     */
    @POST
    @Path("getTaskDetail")
    @Consumes("application/x-www-form-urlencoded")
    public String getTaskDetail(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        TaskController tc = new TaskController();
        String result = tc.getTaskDetail(request, data, isSecurity);
        return result;
    }

    /**
     * Lay chi tiet cong viec
     *
     * @param request
     * @param data du lieu gui len
     * @return
     */
    @POST
    @Path("getUpdateTaskHistory")
    @Consumes("application/x-www-form-urlencoded")
    public String getUpdateTaskHistory(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        TaskController tc = new TaskController();
        String result = tc.getUpdateTaskHistory(request, data, isSecurity);
        return result;
    }

    /**
     * Lay file dinh kem chi tiet cong viec
     *
     * @param request
     * @param data du lieu gui len
     * @return
     */
    @POST
    @Path("getFileAttachmentTask")
    @Consumes("application/x-www-form-urlencoded")
    public String getFileAttachmentTask(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        TaskController tc = new TaskController();
        String result = tc.getFileAttachmentTask(request, data, isSecurity);
        return result;
    }

    /**
     * Lay danh sach yeu cau
     *
     * @param request
     * @param data du lieu gui len
     * @return
     */
    @POST
    @Path("getRequestList")
    @Consumes("application/x-www-form-urlencoded")
    public String getRequestList(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        TaskController tc = new TaskController();
        String result = tc.getRequestList(request, data, isSecurity);
        return result;
    }

    /**
     * Lay nguon goc cong viec
     *
     * @param request
     * @param data du lieu gui len
     * @return
     */
    @POST
    @Path("getSourceTask")
    @Consumes("application/x-www-form-urlencoded")
    public String getSourceTask(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        TaskController tc = new TaskController();
        String result = tc.getSourceTask(request, data, isSecurity);
        return result;
    }

    /**
     * Lay lich su tiep nhan cong viec
     *
     * @param request
     * @param data du lieu gui len
     * @return
     */
    @POST
    @Path("getTaskReceiverHistory")
    @Consumes("application/x-www-form-urlencoded")
    public String getTaskReceiverHistory(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        TaskController tc = new TaskController();
        String result = tc.getTaskReceiverHistory(request, data, isSecurity);
        return result;
    }

    /**
     * Xoa cong viec
     *
     * @param request
     * @param data du lieu gui len
     * @return
     */
    @POST
    @Path("deleteTask")
    @Consumes("application/x-www-form-urlencoded")
    public String deleteTask(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        TaskController tc = new TaskController();
        String result = tc.deleteTask(request, data, isSecurity);
        return result;
    }

    /**
     * Tiep nhan cong viec / Từ chối tiếp nhận / Giao việc lại
     *
     * @param request
     * @param data du lieu gui len
     * @return
     */
    @POST
    @Path("receiveTaskStatus")
    @Consumes("application/x-www-form-urlencoded")
    public String receiveTaskStatus(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        TaskController tc = new TaskController();
        String result = tc.receiveTaskStatus(request, data, isSecurity);
        return result;
    }

    /**
     * Dong cong viec
     *
     * @param request
     * @param data du lieu gui len
     * @return
     */
    @POST
    @Path("closeTask")
    @Consumes("application/x-www-form-urlencoded")
    public String closeTask(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        TaskController tc = new TaskController();
        String result = tc.closeTask(request, data, isSecurity);
        return result;
    }

    /**
     * Lay chi tiet yeu cau
     *
     * @return so luong cong viec
     */
    @POST
    @Path("getDetailRequest")
    @Consumes("application/x-www-form-urlencoded")
    public String getDetailRequest(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        TaskController tc = new TaskController();
        String result = tc.getDetailRequest(request, data, isSecurity);
        return result;
    }

    /**
     * Cap nhat tien do cong viec ca nhan
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("updateTaskProcess")
    @Consumes("application/x-www-form-urlencoded")
    public String updateTaskProcess(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        TaskController tc = new TaskController();
        String result = tc.updateTaskProcess(request, data, isSecurity);
        return result;
    }

    /**
     * Lay cau hinh ty le diem danh gia
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListRatioConfig")
    @Consumes("application/x-www-form-urlencoded")
    public String getListRatioConfig(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        TaskController tc = new TaskController();
        String result = tc.getListRatioConfig(request, data, isSecurity);
        return result;
    }

    /**
     * Phe duyet/Tu choi phe duyet tien do cong viec ca nhan
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("approveOrRejectTaskProcess")
    @Consumes("application/x-www-form-urlencoded")
    public String approveOrRejectTaskProcess(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        TaskController tc = new TaskController();
        String result = tc.approveOrRejectTaskProcess(request, data, isSecurity);
        return result;
    }

    /**
     * Lay danh sach cong viec ca nhan de ky duyet phieu giao viec
     *
     */
    @POST
    @Path("getListTaskToAssign")
    @Consumes("application/x-www-form-urlencoded")
    public String getListTaskToSign(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        TaskController tc = new TaskController();
        String result = tc.getListTaskToAssign(request, data, isSecurity);
        return result;
    }

    /**
     * Lay danh sach cong viec ca nhan de ky duyet phieu giao viec
     *
     */
    @POST
    @Path("exportListTaskFile")
    @Consumes("application/x-www-form-urlencoded")
    public String exportListTaskFile(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        TaskController tc = new TaskController();
        String result = tc.exportListTaskFile(request, data, isSecurity);
        return result;
    }

    /**
     * cap nhat y kien chi dao
     *
     * @return
     */
    @POST
    @Path("updateCommentRequest")
    @Consumes("application/x-www-form-urlencoded")
    public String updateCommentRequest(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        TaskController tc = new TaskController();
        String result = tc.updateCommentRequest(request, data, isSecurity);
        return result;
    }

    /**
     * Hoan thanh kien nghi de xuat
     *
     * @return
     */
    @POST
    @Path("closeRequest")
    @Consumes("application/x-www-form-urlencoded")
    public String closeRequest(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        TaskController tc = new TaskController();
        String result = tc.closeRequest(request, data, isSecurity);
        return result;
    }

    /**
     * Lay danh sach cong viec de danh gia
     */
    @POST
    @Path("getListTaskToAssess")
    @Consumes("application/x-www-form-urlencoded")
    public String getListTaskToAssess(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        TaskController tc = new TaskController();
        String result = tc.getListTaskToAssess(request, data, isSecurity);
        return result;
    }

    /**
     * <b>Lay cau hinh ty le diem danh gia</b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListRatioConfigByOrg")
    @Consumes("application/x-www-form-urlencoded")
    public String getListRatioConfigByOrg(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        TaskController tc = new TaskController();
        String result = tc.getListRatioConfigByOrg(request, data, isSecurity);
        return result;
    }

    /**
     * <b>Lay danh sach cong viec tao ra tu cong van</b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListTaskFromDocument")
    @Consumes("application/x-www-form-urlencoded")
    public String getListTaskFromDocument(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        TaskController tc = new TaskController();
        String result = tc.getListTaskFromDocument(request, data, isSecurity);
        return result;
    }

    //Ham lay so count tren man hinh trang chu modul cong viec
    @POST
    @Path("getCountHomeTask")
    @Consumes("application/x-www-form-urlencoded")
    public String getCountHomeTask(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        TaskController tc = new TaskController();
        String result = tc.getCountHomeTask(request, data, isSecurity);
        return result;
    }

    /**
     * <b>Xoa ban ghi cu khi giao viec lai</b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @authot cuongnv
     *
     */
    @POST
    @Path("convertTaskToPDF")
    @Consumes("application/x-www-form-urlencoded")
    public String convertTaskToPDF(@Context HttpServletRequest request,
            @FormParam("data") String data, @FormParam("isSecurity") String isSecurity) {
        TaskController tc = new TaskController();
        String result = tc.convertTaskToPDF(request, data, isSecurity);
        return result;
    }

    /**
     * <b>Update cac ban ghi tao van ban giao viec</b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @authot cuongnv
     *
     */
    @POST
    @Path("updateFileAttachmentFromTask")
    @Consumes("application/x-www-form-urlencoded")
    public String updateFileAttachmentFromTask(@Context HttpServletRequest request,
            @FormParam("data") String data, @FormParam("isSecurity") String isSecurity) {
        TaskController tc = new TaskController();
        String result = tc.updateFileAttachmentFromTask(request, data, isSecurity);
        return result;
    }

    /**
     * <b>Update cac ban ghi tao van ban danh gia</b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @authot cuongnv
     *
     */
    @POST
    @Path("convertRatingTaskToPDF")
    @Consumes("application/x-www-form-urlencoded")
    public String convertRatingTaskToPDF(@Context HttpServletRequest request,
            @FormParam("data") String data, @FormParam("isSecurity") String isSecurity) {
        TaskController tc = new TaskController();
        String result = tc.convertRatingTaskToPDF(request, data, isSecurity);
        return result;
    }

    @POST
    @Path("deleteRatingTaskToPDF")
    @Consumes("application/x-www-form-urlencoded")
    public String deleteRatingTaskToPDF(@Context HttpServletRequest request,
            @FormParam("data") String data, @FormParam("isSecurity") String isSecurity) {
        TaskController tc = new TaskController();
        String result = tc.deleteRatingTaskToPDF(request, data, isSecurity);
        return result;
    }

    /**
     * <b>Lay danh sach nhan vien quan ly truc tiep trong don vi de giao
     * viec</b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("GetEmployeeListToAssign")
    @Consumes("application/x-www-form-urlencoded")
    public String getEmployeeListToAssign(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TaskController tc = new TaskController();
        return tc.getEmployeeListToAssign(request, data, isSecurity);
    }

    @POST
    @Path("GetTaskListToAssignByEmployee")
    @Consumes("application/x-www-form-urlencoded")
    public String getTaskListToAssignByEmployee(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TaskController tc = new TaskController();
        return tc.getTaskListToAssignByEmployee(request, data, isSecurity);
    }

    /**
     * <b>Lay danh sach nhan vien quan ly truc tiep trong don vi de danh gia</b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("GetEmployeeListToAssess")
    @Consumes("application/x-www-form-urlencoded")
    public String getEmployeeListToAssess(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TaskController tc = new TaskController();
        return tc.getEmployeeListToAssess(request, data, isSecurity);
    }

    /**
     * <b>Lay danh sach cong viec de danh gia theo nhan vien</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("GetTaskListToAssessByEmployee")
    @Consumes("application/x-www-form-urlencoded")
    public String getTaskListToAssessByEmployee(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TaskController tc = new TaskController();
        return tc.getTaskListToAssessByEmployee(request, data, isSecurity);
    }

    /**
     * <b>Huy bo file giao viec/danh gia da duoc ky</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("CancelSignedTaskFileByEmployee")
    @Consumes("application/x-www-form-urlencoded")
    public String cancelSignedTaskFileByEmployee(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TaskController tc = new TaskController();
        return tc.cancelSignedTaskFileByEmployee(request, data, isSecurity);
    }

    /**
     * <b>Lay danh sach ban giao/chia nho cong viec</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListSubOrTransferredTask")
    @Consumes("application/x-www-form-urlencoded")
    public String getListSubOrTransferredTask(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TaskController tc = new TaskController();
        return tc.listSubOrTransferredTask(request, data, isSecurity);
    }

    /**
     * <b>Lay danh sach nguoi phoi hop thuc hien cong viec</b><br>
     *
     * @author HaNH
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getCoordinator")
    @Consumes("application/x-www-form-urlencoded")
    public String getCoordinator(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TaskController tc = new TaskController();
        return tc.getCoordinator(request, data, isSecurity);
    }

    /**
     * <b>lấy danh sách công việc báo cáo đánh giá</b>
     *
     * @since 12/1/2017
     * @author cuongnv
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListTaskReportRating")
    @Consumes("application/x-www-form-urlencoded")
    public String getListTaskReportRating(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TaskController tc = new TaskController();
        return tc.getListTaskReportRating(request, data, isSecurity);
    }

    /**
     * <b>Lay danh sach thong ke cong viec</b>
     *
     * @since 12/1/2017
     * @author SonDN
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListTaskStatistics")
    @Consumes("application/x-www-form-urlencoded")
    public String getListTaskStatistics(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TaskController tc = new TaskController();
        return tc.getListTaskStatistics(request, data, isSecurity);
    }

    /**
     * <b>Chia nho cong viec</b>
     *
     * @since 15/1/2017
     * @author HaNH
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("subEnforcementTask")
    @Consumes("application/x-www-form-urlencoded")
    public String subEnforcementTask(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TaskController tc = new TaskController();
        return tc.subEnforcementTask(request, data, isSecurity);
    }

    /**
     * <b>Chuyen nguoi thuc hien cong viec</b>
     *
     * @since 17/1/2017
     * @author HaNH
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("transferEnforcementTask")
    @Consumes("application/x-www-form-urlencoded")
    public String transferEnforcementTask(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TaskController tc = new TaskController();
        return tc.transferEnforcementTask(request, data, isSecurity);
    }

    /**
     * <b>Ghi lai gia tri danh gia cong viec cua lãnh đạo</b>
     *
     * @since 19/1/2017
     * @author HaNH
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("saveTaskRating")
    @Consumes("application/x-www-form-urlencoded")
    public String saveTaskRating(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TaskController tc = new TaskController();
        return tc.saveTaskRating(request, data, isSecurity);
    }
    
    /**
     * <b>Ghi lai gia tri danh gia thai do lam viec cua lanh đạo</b>
     * @author DAT_DC
     * sua lai thanh
     * cap nhat tuan thu dia diem lam viec
     * noi quy lao dong
     * @author LinhLN
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("saveAverageTask")
    @Consumes("application/x-www-form-urlencoded")
    public String saveAverageTask(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TaskController tc = new TaskController();
        return tc.saveAcerageTask(request, data, isSecurity);
    }
    
        /**
     * <b>Ghi lai gia tri danh gia cong viec cua nhan vien</b>
     *
     * @since 19/1/2017
     * @author LinhLN
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("saveTaskRatingEmp")
    @Consumes("application/x-www-form-urlencoded")
    public String saveTaskRatingEmp(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TaskController tc = new TaskController();
        return tc.saveTaskRatingEmp(request, data, isSecurity);
    }
    
       /**
     * <b>Update cau hinh tieu chi danh gia</b>
     *
     * @since 20/10/2017
     * @author DuanNV
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("updateRatioDetail")
    @Consumes("application/x-www-form-urlencoded")
    public String updateRatioDetail(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TaskController tc = new TaskController();
        return tc.updateRatioDetail(request, data, isSecurity);
    }

    /**
     * <b>Xuat bao cao cong viec da phe duyet theo ky </b>
     *
     * @author SonDN
     * @since 24/1/2017
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("exportReportTaskApproved")
    @Consumes("application/x-www-form-urlencoded")
    public String exportReportTaskApproved(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TaskController tc = new TaskController();
        return tc.exportReportTaskApproved(request, data, isSecurity);
    }
   
    /**
     * getListRatioConfigDT
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListRatioConfigDT")
    @Consumes("application/x-www-form-urlencoded")
    public String getListRatioConfigDT(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TaskController tc = new TaskController();
        return tc.getListRatioConfigDT(request, data, isSecurity);
    }
    /**
     * Get danh sach Rank
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws JSONException 
     */
    @POST
    @Path("getListRank")
    @Consumes("application/x-www-form-urlencoded")
    public String getListRank(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        
        TaskController controller = new TaskController();
        return controller.getListRank(request, isSecurity, data);
    }
    
    /**
     * getListRatioConfigDT
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getTaskListToAssessByEmployeeEmp")
    @Consumes("application/x-www-form-urlencoded")
    public String getTaskListToAssessByEmployeeEmp(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TaskController tc = new TaskController();
        return tc.getTaskListToAssessByEmployeeEmp(request, data, isSecurity);
    }
    
    /**
     * Phe duyet hoac tu choi cong viec
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    @POST
    @Path("ApproveOrRejectTask")
    @Consumes("application/x-www-form-urlencoded")
    public String approveOrRejectTask(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        TaskController controller = new TaskController();
        return controller.approveOrRejectTask(request, isSecurity, data);
    }
    
    @POST
    @Path("checkStatusSignedTaskFileByEmployee")
    @Consumes("application/x-www-form-urlencoded")
    public String checkStatusSignedTaskFileByEmployee(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        TaskController controller = new TaskController();
        return controller.checkStatusSignedTaskFileByEmployee(request, isSecurity, data);
    }

}
