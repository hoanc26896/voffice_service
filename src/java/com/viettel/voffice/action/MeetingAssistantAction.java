/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import com.viettel.voffice.controler.MeetingAssistantController;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

/**
 *
 * @author cuongnv
 *
 */
@Path("MeetingAssistantAction")
public class MeetingAssistantAction {

    /**
     * <b>Thêm sửa cấu hình trợ lý</b>
     *
     * @author cuongnv
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("addAssistant")
    @Consumes("application/x-www-form-urlencoded")
    public String addAssistant(@Context HttpServletRequest request, @FormParam("data") String data, @FormParam("isSecurity") String isSecurity) {
        MeetingAssistantController mac = new MeetingAssistantController();
        return mac.addAssistant(isSecurity, data, request);
    }

    /**
     * <b>Xóa cấu hình trợ lý</b>
     *
     * @author cuongnv
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("deleteAssistant")
    @Consumes("application/x-www-form-urlencoded")
    public String deleteAssistant(@Context HttpServletRequest request, @FormParam("data") String data, @FormParam("isSecurity") String isSecurity) {
        MeetingAssistantController mac = new MeetingAssistantController();
        return mac.deleteAssistant(isSecurity, data, request);
    }

    /**
     * <b>tìm kiếm nhanh cấu hình trợ lý</b>
     *
     * @author cuongnv
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("searchAssistant")
    @Consumes("application/x-www-form-urlencoded")
    public String searchAssistant(@Context HttpServletRequest request, @FormParam("data") String data, @FormParam("isSecurity") String isSecurity) {
        MeetingAssistantController mac = new MeetingAssistantController();
        return mac.searchAssistant(isSecurity, data, request);
    }

    /**
     * <b>tìm kiếm nang cao cấu hình trợ lý</b>
     *
     * @author cuongnv
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("searchAdvancedAssistant")
    @Consumes("application/x-www-form-urlencoded")
    public String searchAdvancedAssistant(@Context HttpServletRequest request, @FormParam("data") String data, @FormParam("isSecurity") String isSecurity) {
        MeetingAssistantController mac = new MeetingAssistantController();
        return mac.searchAdvancedAssistant(isSecurity, data, request);
    }

    /**
     * <b>Xem chi tiết cấu hình trợ lý</b>
     *
     * @author cuongnv
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getDetailMeetingAssistant")
    @Consumes("application/x-www-form-urlencoded")
    public String getDetailMeetingAssistant(@Context HttpServletRequest request, @FormParam("data") String data, @FormParam("isSecurity") String isSecurity) {
        MeetingAssistantController mac = new MeetingAssistantController();
        return mac.getDetailMeetingAssistant(isSecurity, data, request);
    }

    /**
     * <b>Thêm lãnh đạo không nhận tin nhắn/email</b>
     *
     * @author cuongnv
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("addBlockEmailSms")
    @Consumes("application/x-www-form-urlencoded")
    public String addBlockEmailSms(@Context HttpServletRequest request, @FormParam("data") String data, @FormParam("isSecurity") String isSecurity) {
        MeetingAssistantController mac = new MeetingAssistantController();
        return mac.addBlockEmailSms(isSecurity, data, request);
    }

    /**
     * <b>Tìm kiếm lãnh đạo không nhận tin nhắn/email</b>
     *
     * @author cuongnv
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("searchBlockEmailSms")
    @Consumes("application/x-www-form-urlencoded")
    public String searchBlockEmailSms(@Context HttpServletRequest request, @FormParam("data") String data, @FormParam("isSecurity") String isSecurity) {
        MeetingAssistantController mac = new MeetingAssistantController();
        return mac.searchBlockEmailSms(isSecurity, data, request);
    }

    /**
     * <b>Tìm kiếm nâng cao lãnh đạo không nhận tin nhắn/email</b>
     *
     * @author cuongnv
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("searchAdvancedBlockEmailSms")
    @Consumes("application/x-www-form-urlencoded")
    public String searchAdvancedBlockEmailSms(@Context HttpServletRequest request, @FormParam("data") String data, @FormParam("isSecurity") String isSecurity) {
        MeetingAssistantController mac = new MeetingAssistantController();
        return mac.searchAdvancedBlockEmailSms(isSecurity, data, request);
    }

    /**
     * <b>Xóa lãnh đạo không nhận tin nhắn/email</b>
     *
     * @author cuongnv
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("deleteAdvancedBlockEmailSms")
    @Consumes("application/x-www-form-urlencoded")
    public String deleteBlockEmailSms(@Context HttpServletRequest request, @FormParam("data") String data, @FormParam("isSecurity") String isSecurity) {
        MeetingAssistantController mac = new MeetingAssistantController();
        return mac.deleteBlockEmailSms(isSecurity, data, request);
    }

    /**
     * <b>edit lãnh đạo không nhận tin nhắn/email</b>
     *
     * @author sonnd
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("editBlockEmailSms")
    @Consumes("application/x-www-form-urlencoded")
    public String editBlockEmailSms(@Context HttpServletRequest request, @FormParam("data") String data, @FormParam("isSecurity") String isSecurity) {
        MeetingAssistantController mac = new MeetingAssistantController();
        return mac.editBlockEmailSms(isSecurity, data, request);
    }

    /**
     * <b>get lấy tất cả lãnh đạo của user</b>
     *
     * @author tuanld
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getLeaderByEmployee")
    @Consumes("application/x-www-form-urlencoded")
    public String getLeaderByEmployee(@Context HttpServletRequest request, @FormParam("data") String data, @FormParam("isSecurity") String isSecurity) {
        MeetingAssistantController mac = new MeetingAssistantController();
        return mac.getLeaderByEmployee(isSecurity, data, request);
    }

    /**
     * <b>Tim kiem van ban cua lanh dao duoc theo doi</b>
     *
     * @author cuongnv
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("searchFollowLeader")
    @Consumes("application/x-www-form-urlencoded")
    public String searchFollowLeader(@Context HttpServletRequest request, @FormParam("data") String data, @FormParam("isSecurity") String isSecurity) {
        MeetingAssistantController mac = new MeetingAssistantController();
        return mac.searchFollowLeader(isSecurity, data, request);
    }
    
    @POST
    @Path("getMeetingAssistantList")
    @Consumes("application/x-www-form-urlencoded")
    public String getMeetingAssistantList(@Context HttpServletRequest request, @FormParam("data") String data, @FormParam("isSecurity") String isSecurity) {
        MeetingAssistantController mac = new MeetingAssistantController();
        return mac.getMeetingAssistantList(isSecurity, data, request);
    }

    /**
     * Lay cac lich hop co thay doi thanh phan tham gia
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("findMeetingChangeReplate")
    @Consumes("application/x-www-form-urlencoded")
    public String findMeetingChangeReplate(@Context HttpServletRequest request, @FormParam("data") String data, @FormParam("isSecurity") String isSecurity) {
        MeetingAssistantController mac = new MeetingAssistantController();
        return mac.findMeetingChangeReplate(isSecurity, data, request);
    }

    /**
     * Lay chi tiet thanh phan yeu cau thay doi
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getMeetingChangeReplate")
    @Consumes("application/x-www-form-urlencoded")
    public String getMeetingChangeReplate(@Context HttpServletRequest request, @FormParam("data") String data, @FormParam("isSecurity") String isSecurity) {
        MeetingAssistantController mac = new MeetingAssistantController();
        return mac.getMeetingChangeReplate(isSecurity, data, request);
    }
    

    /**
     * Phe duyet/tu choi thanh phan tham gia
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("approveReplateMember")
    @Consumes("application/x-www-form-urlencoded")
    public String approveReplateMember(@Context HttpServletRequest request, @FormParam("data") String data, @FormParam("isSecurity") String isSecurity) {
        MeetingAssistantController mac = new MeetingAssistantController();
        return mac.approveReplateMember(isSecurity, data, request);
    }

    /**
     * Lay employeeId cua ong dang nhap
     * co vai tro thay doi thanh phan lich hop
     * @author datdc
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getExistAssistantChangeMeetByEmployeeId")
    @Consumes("application/x-www-form-urlencoded")
    public String getExistAssistantChangeMeetByEmployeeId(@Context HttpServletRequest request, @FormParam("data") String data, @FormParam("isSecurity") String isSecurity) {
        MeetingAssistantController mac = new MeetingAssistantController();
        return mac.getExistAssistantChangeMeetByEmployeeId(isSecurity, data, request);
    }
}