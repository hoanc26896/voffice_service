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
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.controler.MeetingController;
import com.viettel.voffice.utils.LogUtils;

/**
 * REST Web Service
 *
 * @author datnv5
 */
@Path("Meeting")
public class MettingResource {

    public static final String ROOT_ACTION = "Meeting";

    @POST
    @Path("getListMeetingMinutes")
    @Consumes("application/x-www-form-urlencoded")
    public String getListMeetingMinutes(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, strSecurity);
        MeetingController controller = new MeetingController();
        String strDataResponse = controller.getMeetingMinutes(strSecurity, data, req);
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, strSecurity);
        return strDataResponse;
    }

    @POST
    @Path("getMissionByMeetingId")
    @Produces("application/x-www-form-urlencoded")
    public String getMissionByMeetingId(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, strSecurity);
        MeetingController controller = new MeetingController();

        String strDataResponse = controller.getMissionByMeetingId(strSecurity, data, req);
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, strSecurity);
        return strDataResponse;
    }

    @POST
    @Path("getMeetingMinutesById")
    @Produces("application/x-www-form-urlencoded")
    public String getMeetingMinutesById(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, strSecurity);
        MeetingController controller = new MeetingController();

        String strDataResponse = controller.getMeetingMinutesById(strSecurity, data, req);
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, strSecurity);
        return strDataResponse;
    }

    @POST
    @Path("countListMeetingMinutes")
    @Produces("application/x-www-form-urlencoded")
    public String countListMeetingMinutes(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, strSecurity);
        MeetingController controller = new MeetingController();
        String strDataResponse = controller.countMeetingMinutes(strSecurity, data, req);
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, strSecurity);
        return strDataResponse;
    }

    @POST
    @Path("countMissionByMeetingId")
    @Produces("application/x-www-form-urlencoded")
    public String countMissionByMeetingId(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, strSecurity);
        MeetingController controller = new MeetingController();

        String strDataResponse = controller.countMissionByMeetingId(strSecurity, data, req);
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, strSecurity);
        return strDataResponse;
    }

    @POST
    @Path("getListFileAttachment")
    @Produces("application/x-www-form-urlencoded")
    public String getListFileAttachment(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, strSecurity);
        MeetingController controller = new MeetingController();

        String strDataResponse = controller.getListFileAttachment(strSecurity, data, req);
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, strSecurity);
        return strDataResponse;
    }

    @POST
    @Path("getListOrganizationByUser")
    @Produces("application/x-www-form-urlencoded")
    public String getListOrganizationByUser(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, strSecurity);
        MeetingController controller = new MeetingController();

        String strDataResponse = controller.getListOrganizationByUser(strSecurity, data, req);
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, strSecurity);
        return strDataResponse;
    }

    @POST
    @Path("getListOrganization")
    @Produces("application/x-www-form-urlencoded")
    public String getListOrganization(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, strSecurity);
        MeetingController controller = new MeetingController();

        String strDataResponse = controller.getListOrganization(strSecurity, data, req);
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, strSecurity);
        return strDataResponse;
    }

    @POST
    @Path("getListOrganizationAssign")
    @Produces("application/x-www-form-urlencoded")
    public String getListOrganizationAssign(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, strSecurity);
        MeetingController controller = new MeetingController();

        String strDataResponse = controller.getListOrganizationAssign(strSecurity, data, req);
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, strSecurity);
        return strDataResponse;
    }

    @POST
    @Path("getListField")
    @Produces("application/x-www-form-urlencoded")
    public String getListField(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, strSecurity);
        MeetingController controller = new MeetingController();

        String strDataResponse = controller.getListField(strSecurity, data, req);
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, strSecurity);
        return strDataResponse;
    }

    @POST
    @Path("addMission")
    @Produces("application/x-www-form-urlencoded")
    public String addMission(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, strSecurity);
        MeetingController controller = new MeetingController();

        String strDataResponse = controller.addMission(strSecurity, data, req);
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, strSecurity);
        return strDataResponse;
    }

    @POST
    @Path("forwardMission")
    @Produces("application/x-www-form-urlencoded")
    public String forwardMission(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, strSecurity);
        MeetingController controller = new MeetingController();

        String strDataResponse = controller.forwardMission(strSecurity, data, req);
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, strSecurity);
        return strDataResponse;
    }

    @POST
    @Path("getSourceMapMission")
    @Produces("application/x-www-form-urlencoded")
    public String getSourceMapMission(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, strSecurity);
        MeetingController controller = new MeetingController();

        String strDataResponse = controller.getSourceMapMission(strSecurity, data, req);
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, strSecurity);
        return strDataResponse;
    }

    /**
     * <b>Lay danh sach nguon goc cua bien ban hop</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("GetListSource")
    @Produces("application/x-www-form-urlencoded")
    public String getListSource(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        MeetingController mc = new MeetingController();
        String result = mc.getListSource(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    /**
     * <b>Thông tin chi tiết phòng họp</b>
     *
     * @author cuongnv
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getDetailMeetingResource")
    @Produces("application/x-www-form-urlencoded")
    public String getDetailMeetingResource(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        MeetingController mc = new MeetingController();
        return mc.getDetailMeetingResource(request, data, isSecurity);
    }

    /**
     * <b>Xoa bien ban hop</b><br>
     *
     * @author SonDN
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("deleteMeetingMinutes")
    @Produces("application/x-www-form-urlencoded")
    public String deleteMeetingMinutes(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        MeetingController mc = new MeetingController();
        String result = mc.deleteMeetingMinutes(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    /**
     * <b>Thêm mới/sửa biên bản họp</b>
     *
     * @author cuongnv
     * @param request
     * @param data
     * @param isSecurity
     * @since 29/11/2016
     * @return
     */
    @POST
    @Path("addOrEditMeetingMinutes")
    @Produces("application/x-www-form-urlencoded")
    public String addOrEditMeetingMinutes(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        MeetingController mc = new MeetingController();
        String result = mc.addOrEditMeetingMinutes(request, data, isSecurity);
        return result;
    }

    /**
     * <b>Lay danh sach don vi thuc hien dung tren web</b>
     *
     * @author cuongnv
     * @param req
     * @param data
     * @param strSecurity
     * @return
     * @since 7/12/2016
     */
    @POST
    @Path("getListOrganizationExecute")
    @Produces("application/x-www-form-urlencoded")
    public String getListOrganizationExecute(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, strSecurity);
        MeetingController controller = new MeetingController();

        String strDataResponse = controller.getListOrganizationExecute(strSecurity, data, req);
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, strSecurity);
        return strDataResponse;
    }

    /**
     * <b>Lay danh sach don vi giao nhiem vu dung tren web</b>
     *
     * @author HaNH
     * @param req
     * @param data
     * @param strSecurity
     * @return
     */
    @POST
    @Path("getListOrganizationsAssign")
    @Produces("application/x-www-form-urlencoded")
    public String getListOrganizationsAssign(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, strSecurity);
        MeetingController controller = new MeetingController();

        String strDataResponse = controller.getListOrganizationsAssign(strSecurity, data, req);
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, strSecurity);
        return strDataResponse;
    }

    /**
     * <b>Lay danh sach don vi phoi hop trong tim kiem nang cao dung tren
     * web</b>
     *
     * @author HaNH
     * @param req
     * @param data
     * @param strSecurity
     * @return
     */
    @POST
    @Path("getListOrgPerformInAdvanceSearch")
    @Produces("application/x-www-form-urlencoded")
    public String getListOrgPerformInAdvanceSearch(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, strSecurity);
        MeetingController controller = new MeetingController();
        String strDataResponse = controller.getListOrgPerformInAdvanceSearch(strSecurity, data, req);
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, strSecurity);
        return strDataResponse;
    }

    /**
     * <b>Thêm mới/sửa biên bản họp</b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("updateMeetingMinutes")
    @Produces("application/x-www-form-urlencoded")
    public String updateMeetingMinutes(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        MeetingController mc = new MeetingController();
        return mc.updateMeetingMinutes(request, data, isSecurity);
    }

    /**
     * <b>Trinh ky van ban tao tu bien ban hop</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("requestForSigningMeetingMinutes")
    @Produces("application/x-www-form-urlencoded")
    public String requestForSigningMeetingMinutes(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        MeetingController controller = new MeetingController();
        return controller.requestForSigningMeetingMinutes(request, data, isSecurity);
    }
    
    @POST
    @Path("getMeetingsByDocId")
    @Produces("application/x-www-form-urlencoded")
    public String getMeetingsByDocId(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        MeetingController mc = new MeetingController();
        return mc.getMeetingsByDocId(request, data, isSecurity);
    }
    
    @POST
    @Path("getListUserMeeting")
    @Produces("application/x-www-form-urlencoded")
    public String getListUserMeeting(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        MeetingController mc = new MeetingController();
        return mc.getListUserMeeting(request, data, isSecurity);
    }
    
    @POST
    @Path("manualRollCallMeeting")
    @Produces("application/x-www-form-urlencoded")
    public String manualRollCallMeeting(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        MeetingController mc = new MeetingController();
        return mc.manualRollCallMeeting(request, data, isSecurity);
    }

    @POST
    @Path("autoRollCallMeeting")
    @Produces("application/x-www-form-urlencoded")
    public String autoRollCallMeeting(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        MeetingController mc = new MeetingController();
        return mc.autoRollCallMeeting(request, data, isSecurity);
    }
    
    @POST
    @Path("changeMemberMeeting")
    @Produces("application/x-www-form-urlencoded")
    public String changeMemberMeeting(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        MeetingController mc = new MeetingController();
        return mc.changeMemberMeeting(request, data, isSecurity);
    }
    
    @POST
    @Path("checkAttendMeeting")
    @Produces("application/x-www-form-urlencoded")
    public String checkAttendMeeting(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        MeetingController mc = new MeetingController();
        return mc.checkAttendMeeting(request, data, isSecurity);
    }
    
    @POST
    @Path("updateStateFiles")
    @Produces("application/x-www-form-urlencoded")
    public String updateStateFiles(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        MeetingController mc = new MeetingController();
        return mc.updateStateFiles(request, data, isSecurity);
    }
    
    @POST
    @Path("permissionRollCall")
    @Produces("application/x-www-form-urlencoded")
    public String permissionRollCall(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        MeetingController mc = new MeetingController();
        return mc.permissionRollCall(request, data, isSecurity);
    }
    
    @POST
    @Path("getContentMeetingMember")
    @Produces("application/x-www-form-urlencoded")
    public String getContentMeetingMember(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        MeetingController mc = new MeetingController();
        return mc.getContentMeetingMember(request, data, isSecurity);
    }

    @POST
    @Path("getListAbsenceMember")
    @Produces("application/x-www-form-urlencoded")
    public String getListAbsenceMember(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        MeetingController mc = new MeetingController();
        return mc.getListAbsenceMember(request, data, isSecurity);
    }

    @POST
    @Path("getDirectorConfig")
    @Produces("application/x-www-form-urlencoded")
    public String getDirectorConfig(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        MeetingController mc = new MeetingController();
        return mc.getDirectorConfig(request, data, isSecurity);
    }
    
    @POST
    @Path("updateMemberReplate")
    @Produces("application/x-www-form-urlencoded")
    public String updateMemberReplate(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        MeetingController mc = new MeetingController();
        return mc.updateMemberReplate(request, data, isSecurity);
    }
    
    @POST
    @Path("getListFileMeeting")
    @Produces("application/x-www-form-urlencoded")
    public String getListFileMeeting(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        MeetingController mc = new MeetingController();
        return mc.getListFileMeeting(request, data, isSecurity);
    }

    @POST
    @Path("insertMeetingFiles")
    @Produces("application/x-www-form-urlencoded")
    public String insertMeetingFiles(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        MeetingController mc = new MeetingController();
        return mc.insertMeetingFiles(request, data, isSecurity);
    }
    
    @POST
    @Path("removePermissionViewFile")
    @Produces("application/x-www-form-urlencoded")
    public String removePermissionViewFile(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        MeetingController mc = new MeetingController();
        return mc.removePermissionViewFile(request, data, isSecurity);
    }
    
    @POST
    @Path("getListUserViewFile")
    @Produces("application/x-www-form-urlencoded")
    public String getListUserViewFile(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        MeetingController mc = new MeetingController();
        return mc.getListUserViewFile(request, data, isSecurity);
    }

    @POST
    @Path("updateBoardNameSmartRoom")
    @Produces("application/x-www-form-urlencoded")
    public String updateBoardNameSmartRoom(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        MeetingController mc = new MeetingController();
        return mc.updateBoardNameSmartRoom(request, data, isSecurity);
    }

    @POST
    @Path("getMeetingMemberPermissionFile")
    @Produces("application/x-www-form-urlencoded")
    public String getMeetingMemberPermissionFile(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        MeetingController mc = new MeetingController();
        return mc.getMeetingMemberPermissionFile(request, data, isSecurity);
    }
    
    @POST
    @Path("getDirectorConfigById")
    @Produces("application/x-www-form-urlencoded")
    public String getDirectorConfigById(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        MeetingController mc = new MeetingController();
        return mc.getDirectorConfigById(request, data, isSecurity);
    }
    
    @POST
    @Path("getMeetingMemberOrderView")
    @Produces("application/x-www-form-urlencoded")
    public String getMeetingMemberOrderView(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        MeetingController mc = new MeetingController();
        return mc.getMeetingMemberOrderView(request, data, isSecurity);
    }
}