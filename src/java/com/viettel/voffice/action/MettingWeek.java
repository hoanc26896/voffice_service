/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.controler.MeetingFileCommentControler;
import com.viettel.voffice.controler.MeetingWeekController;
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
 * @author datnv5
 */
@Path("MettingWeek")
public class MettingWeek {

    private final String ROOT_ACTION = "MettingWeek";

    @POST
    @Path("getLstMeetingWeek")
    @Consumes("application/x-www-form-urlencoded")
    public String getLstMeetingWeek(@Context HttpServletRequest req,
            @FormParam("data") String data) {

        MeetingWeekController controller = new MeetingWeekController();
        return controller.getMeetingWeek(data, req);
    }

    /**
     * <b>Lay danh sach truc chi huy</b><br>
     *
     * @param request
     * @param data
     * @return
     */
    @POST
    @Path("getMeetingComanderResult")
    @Consumes("application/x-www-form-urlencoded")
    public String getMeetingComanderResult(
            @Context HttpServletRequest request,
            @FormParam("data") String data) {

        MeetingWeekController meetingWeekController = new MeetingWeekController();
        return meetingWeekController.getMeetingComanderResult(request, data);
    }

    @POST
    @Path("getWorkMeetingDirector")
    @Consumes("application/x-www-form-urlencoded")
    public String getWorkMeetingDirector(@Context HttpServletRequest req,
            @FormParam("data") String data) {

        MeetingWeekController controller = new MeetingWeekController();
        return controller.getWorkMeetingDirector(data, req);
    }

    @POST
    @Path("get3MeetingNearestOnDashboard")
    @Consumes("application/x-www-form-urlencoded")
    public String get3MeetingNearestOnDashboard(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        MeetingWeekController controller = new MeetingWeekController();
        return controller.get3MeetingNearestOnDashboard(req, data, isSecurity);
    }

    @POST
    @Path("getMeetingDetail")
    @Consumes("application/x-www-form-urlencoded")
    public String getMeetingDetail(@Context HttpServletRequest request,
            @FormParam("data") String data) {

        MeetingWeekController controller = new MeetingWeekController();
        return controller.getMeetingDetail(request, data);
    }

    @POST
    @Path("GetListMeeting")
    @Consumes("application/x-www-form-urlencoded")
    public String getListMeeting(@Context HttpServletRequest request,
            @FormParam("data") String data) {

        MeetingWeekController controller = new MeetingWeekController();
        return controller.getListMeeting(request, data);
    }

    /**
     * thuc hien lay du lieu theo yeu cau cua Anh to
     *
     * @param request
     * @param data
     * @return
     */
    @POST
    @Path("getMeetingList")
    @Consumes("application/x-www-form-urlencoded;charset=UTF-8")
    public String getMeetingList(@Context HttpServletRequest request,
            @FormParam("data") String data) {
        MeetingWeekController controller = new MeetingWeekController();
        return controller.getMeetingList(request, data);
    }

    @POST
    @Path("GetMeetingListByText")
    @Consumes("application/x-www-form-urlencoded")
    public String getMeetingListByText(@Context HttpServletRequest request,
            @FormParam("data") String data) {

        MeetingWeekController controller = new MeetingWeekController();
        return controller.getMeetingListByText(request, data);
    }

    @POST
    @Path("GetMeetingParticipantList")
    @Consumes("application/x-www-form-urlencoded")
    public String getMeetingParticipantList(@Context HttpServletRequest request,
            @FormParam("data") String data) {
        MeetingWeekController controller = new MeetingWeekController();
        return controller.getMeetingParticipantList(request, data);
    }

    @POST
    @Path("GetVideoConferencingList")
    @Consumes("application/x-www-form-urlencoded")
    public String getVideoConferencingList(@Context HttpServletRequest request,
            @FormParam("data") String data) {

        MeetingWeekController controller = new MeetingWeekController();
        return controller.getVideoConferencingList(request, data);
    }

    @POST
    @Path("checkLeaderIdForAssistantUserInMemberList")
    @Consumes("application/x-www-form-urlencoded")
    public String checkLeaderIdForAssistantUserInMemberList(@Context HttpServletRequest request,
            @FormParam("data") String data) {

        MeetingWeekController controller = new MeetingWeekController();
        return controller.checkLeaderIdForAssistantUserInMemberList(request, data);
    }

    @POST
    @Path("getListApproveCalendar")
    @Consumes("application/x-www-form-urlencoded")
    public String getListApproveCalendar(@Context HttpServletRequest request,
            @FormParam("data") String data, @FormParam("isSecurity") String isSecurity) {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        MeetingWeekController controller = new MeetingWeekController();
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return controller.getListApproveCalendar(request, data);
    }

    @POST
    @Path("checkPermisionCalendar")
    @Consumes("application/x-www-form-urlencoded")
    public String checkPermisionCalendar(@Context HttpServletRequest request,
            @FormParam("data") String data) {

        MeetingWeekController controller = new MeetingWeekController();
        return controller.checkPermisionCalendar(request, data);
    }

    @POST
    @Path("approveCalendar")
    @Consumes("application/x-www-form-urlencoded")
    public String approveCalendar(@Context HttpServletRequest request,
            @FormParam("data") String data) {

        MeetingWeekController controller = new MeetingWeekController();
        return controller.approveCalendar(request, data);
    }

    @POST
    @Path("rejectCalendar")
    @Consumes("application/x-www-form-urlencoded")
    public String rejectCalendar(@Context HttpServletRequest request,
            @FormParam("data") String data) {

        MeetingWeekController controller = new MeetingWeekController();
        return controller.rejectCalendar(request, data);
    }

    @POST
    @Path("cancelCalendar")
    @Consumes("application/x-www-form-urlencoded")
    public String cancelCalendar(@Context HttpServletRequest request,
            @FormParam("data") String data) {

        MeetingWeekController controller = new MeetingWeekController();
        return controller.cancelCalendar(request, data);
    }

    @POST
    @Path("deleteCalendar")
    @Consumes("application/x-www-form-urlencoded")
    public String deleteCalendar(@Context HttpServletRequest request,
            @FormParam("data") String data) {

        MeetingWeekController controller = new MeetingWeekController();
        return controller.deleteCalendar(request, data);
    }

    @POST
    @Path("getListLocationFree")
    @Consumes("application/x-www-form-urlencoded")
    public String getListLocationFree(@Context HttpServletRequest request,
            @FormParam("data") String data) {

        MeetingWeekController controller = new MeetingWeekController();
        return controller.getListLocationFree(request, data);
    }

    @POST
    @Path("changeLocation")
    @Consumes("application/x-www-form-urlencoded")
    public String changeLocation(@Context HttpServletRequest request,
            @FormParam("data") String data) {

        MeetingWeekController controller = new MeetingWeekController();
        return controller.changeLocation(request, data);
    }

    @POST
    @Path("checkConflictTimeUsedRoom")
    @Consumes("application/x-www-form-urlencoded")
    public String checkConflictTimeUsedRoom(@Context HttpServletRequest request,
            @FormParam("data") String data) {
        MeetingWeekController controller = new MeetingWeekController();
        return controller.checkConflictTimeUsedRoom(request, data);
    }

    @POST
    @Path("checkChangeOrgApproval")
    @Consumes("application/x-www-form-urlencoded")
    public String checkChangeOrgApproval(@Context HttpServletRequest request,
            @FormParam("data") String data) {
        MeetingWeekController controller = new MeetingWeekController();
        return controller.checkChangeOrgApproval(request, data);
    }

    @POST
    @Path("checkDuplicateParticant")
    @Consumes("application/x-www-form-urlencoded")
    public String checkDuplicateParticant(@Context HttpServletRequest request,
            @FormParam("data") String data) {
        MeetingWeekController controller = new MeetingWeekController();
        return controller.checkDuplicateParticant(request, data);
    }

    @POST
    @Path("sendSMSMeetingWeek")
    @Consumes("application/x-www-form-urlencoded")
    public String sendSMSMeetingWeek(@Context HttpServletRequest request,
            @FormParam("data") String data) {
        MeetingWeekController controller = new MeetingWeekController();
        return controller.sendSMS(request, data);
    }

    @POST
    @Path("sendMail")
    @Consumes("application/x-www-form-urlencoded")
    public String sendMailMeetingWeek(@Context HttpServletRequest request,
            @FormParam("data") String data) {
        MeetingWeekController controller = new MeetingWeekController();
        return controller.sendMail(request, data);
    }

    @POST
    @Path("checkDuplicateVideoConferenceRoom")
    @Consumes("application/x-www-form-urlencoded")
    public String checkDuplicateMeetingVideoConference(@Context HttpServletRequest request,
            @FormParam("data") String data) {
        MeetingWeekController controller = new MeetingWeekController();
        return controller.checkDuplicateVideoConferenceRoom(request, data);
    }

    @POST
    @Path("checkDuplicateVideoConference")
    @Consumes("application/x-www-form-urlencoded")
    public String checkDuplicateVideoConference(@Context HttpServletRequest request,
            @FormParam("data") String data) {
        MeetingWeekController controller = new MeetingWeekController();
        return controller.checkDuplicateVideoConference(request, data);
    }

    @POST
    @Path("checkDuplicateRoomVideoConference")
    @Consumes("application/x-www-form-urlencoded")
    public String checkDuplicateRoomVideoConference(@Context HttpServletRequest request,
            @FormParam("data") String data) {
        MeetingWeekController controller = new MeetingWeekController();
        return controller.checkDuplicateRoomVideoConference(request, data);
    }

    @POST
    @Path("validateSaveMeeting")
    @Consumes("application/x-www-form-urlencoded")
    public String validateSaveMeeting(@Context HttpServletRequest request,
            @FormParam("data") String data) {
        MeetingWeekController controller = new MeetingWeekController();
        return controller.validateSaveMeeting(request, data);
    }

    /**
     * Lay thong tin cuoc hop dang dien ra tai phong hop phong minh
     *
     * @param request
     * @param data
    
     * @return
     */
    @POST
    @Path("getMeetingForSmartRoom")
    @Consumes("application/x-www-form-urlencoded")
    public String getMeetingForSmartRoom(@Context HttpServletRequest req,
            @FormParam("data") String data) {

        MeetingWeekController controller = new MeetingWeekController();
        return controller.getMeetingForSmartRoom(req, data);
    }

    /**
     * Luu comment
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("updateFileMeetingComment")
    @Consumes("application/x-www-form-urlencoded")
    public String updateFileMeetingComment(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        MeetingFileCommentControler fc = new MeetingFileCommentControler();
        String result = fc.updateFileMeetingComment(request, data, isSecurity);
        return result;
    }

    /**
     * Xoa comment file van ban trong module lich hop
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("resetMeetingAttachComment")
    @Consumes("application/x-www-form-urlencoded")
    public String resetMeetingAttachComment(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        MeetingFileCommentControler fc = new MeetingFileCommentControler();
        String result = fc.resetMeetingAttachComment(request, data, isSecurity);
        return result;
    }

    /**
     * lay danh sach note file lich hop
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListMeetingNote")
    @Consumes("application/x-www-form-urlencoded")
    public String getListMeetingNote(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        MeetingFileCommentControler fc = new MeetingFileCommentControler();
        String result = fc.getListMeetingNote(request, data, isSecurity);
        return result;
    }


    /**
     * lay danh sach note file lich hop
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("saveMeetingNoteBook")
    @Consumes("application/x-www-form-urlencoded")
    public String saveMeetingNoteBook(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        MeetingFileCommentControler fc = new MeetingFileCommentControler();
        String result = fc.saveMeetingNoteBook(request, data);
        return result;
    }
    
    
     /**
     * sua hoac xoa ticknote tren file
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("updateOrDelMeetingNoteFile")
    @Consumes("application/x-www-form-urlencoded")
    public String updateOrDelMeetingNoteFile(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        MeetingFileCommentControler fc = new MeetingFileCommentControler();
        String result = fc.updateOrDelMeetingNoteFile(request, data);
        return result;
    }
    
    /**
     * lay danh sach lich hop de diem danh
     * 
     * @param req
     * @param data
     * @return
     */
    @POST
    @Path("getMeetingRollCall")
    @Consumes("application/x-www-form-urlencoded")
    public String getMeetingRollCall(@Context HttpServletRequest req,
            @FormParam("data") String data) {
        MeetingWeekController controller = new MeetingWeekController();
        return controller.getMeetingRollCall(req, data);
    }

    /**
     * getNoteBookDetail
     * @param request
     * @param data
     * @return
     */
    @POST
    @Path("getNoteBookDetail")
    @Consumes("application/x-www-form-urlencoded")
    public String getNoteBookDetail(@Context HttpServletRequest request,
            @FormParam("data") String data, @FormParam("isSecurity") String isSecurity) {

        MeetingWeekController controller = new MeetingWeekController();
        return controller.getNoteBookDetail(isSecurity, data, request);
    }
    
    @POST
    @Path("getLeaderIdForAssistantUser")
    @Consumes("application/x-www-form-urlencoded")
    public String getLeaderIdForAssistantUser(@Context HttpServletRequest request,
            @FormParam("data") String data, @FormParam("isSecurity") String isSecurity) {

        MeetingWeekController controller = new MeetingWeekController();
        return controller.getLeaderIdForAssistantUser(isSecurity, data, request);
    }

    @POST
    @Path("saveMeetingComander")
    @Consumes("application/x-www-form-urlencoded")
    public String saveMeetingComander(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        MeetingWeekController fc = new MeetingWeekController();
        String result = fc.saveMeetingComander(request, data);
        return result;
    }
    
    @POST
    @Path("getListMeetingCommander")
    @Consumes("application/x-www-form-urlencoded")
    public String getListMeetingCommander(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        MeetingWeekController fc = new MeetingWeekController();
        String result = fc.getListMeetingCommander(request, data);
        return result;
    }

    /**
     * Lay trang thai ket luan
     * van ban ket luan cho phong hop
     * @author datdc
     * @param req
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getLstDocumentByLstMeeting")
    @Consumes("application/x-www-form-urlencoded")
    public String getLstDocumentByLstMeeting(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        MeetingWeekController controller = new MeetingWeekController();
        return controller.getLstDocumentByLstMeeting(data, req, isSecurity);
    }

    /**@author MINHNQ
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("checkUpdateWithoutConclusions")
    @Consumes("application/x-www-form-urlencoded")
    public String checkUpdateWithoutConclusions(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        MeetingWeekController fc = new MeetingWeekController();
        String result = fc.checkUpdateWithoutConclusions(request, data);
        return result;
    }

    /**@author MINHNQ
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("updateWithoutConclusions")
    @Consumes("application/x-www-form-urlencoded")
    public String updateWithoutConclusions(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        MeetingWeekController fc = new MeetingWeekController();
        String result = fc.updateWithoutConclusions(request, data);
        return result;
    }
    
    @POST
    @Path("filterMeetingTextByCreator")
    @Consumes("application/x-www-form-urlencoded")
    public String filterMeetingTextByCreator(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        MeetingWeekController fc = new MeetingWeekController();
        String result = fc.filterMeetingTextByCreator(data, request, isSecurity);
        return result;
    }
}