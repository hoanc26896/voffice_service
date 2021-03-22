/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import com.viettel.voffice.controler.MissionControler;

/**
 * REST Web Service
 *
 * @author Thanght6
 */
@Path("missionAction")
public class MissionAction {

    /**
     * Lay danh sach nhiem vu don vi
     *
     * @param request
     * @param data du lieu gui len
     * @param isSecurity 1 - Co ma hoa du lieu, 0 - Khong ma hoa du lieu
     * @return
     */
    @POST
    @Path("getListMissionGroup")
    @Consumes("application/x-www-form-urlencoded")
    public String getListMissionGroup(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler mc = new MissionControler();
        String result = mc.getListMissionGroup(request, data, isSecurity);
        return result;
    }

    @POST
    @Path("getListMission")
    @Consumes("application/x-www-form-urlencoded")
    public String getListMission(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler mc = new MissionControler();
        String result = mc.getListMission(request, data, isSecurity);
        return result;
    }

    /**
     * Lay chi tiet nhiem vu don vi
     */
    @POST
    @Path("getMissionDetail")
    @Consumes("application/x-www-form-urlencoded")
    public String getMissionDetail(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler mc = new MissionControler();
        String result = mc.getMissionDetail(request, data, isSecurity);
        return result;
    }

    /**
     * Lay danh sach file dinh kem cua nhiem vu
     */
    @POST
    @Path("getListFileAttachment")
    @Consumes("application/x-www-form-urlencoded")
    public String getListFileAttachment(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler mc = new MissionControler();
        String result = mc.getListFileAttachment(request, data, isSecurity);
        return result;
    }

    /**
     * Lay lich su cap nhat tien do
     */
    @POST
    @Path("getMissionProcessHistory")
    @Consumes("application/x-www-form-urlencoded")
    public String getMissionProcessHistory(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler mc = new MissionControler();
        String result = mc.getMissionProcessHistory(request, data, isSecurity);
        return result;
    }

    /**
     * Lay cap nhat tien do cuoi cung cua moi nhiem vu con
     */
    @POST
    @Path("getLastMissionProcessOfSubMissions")
    @Consumes("application/x-www-form-urlencoded")
    public String getLastMissionProcessOfSubMissions(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler mc = new MissionControler();
        String result = mc.getLastMissionProcessOfSubMissions(request, data, isSecurity);
        return result;
    }

    /**
     * Lay danh sach don vi phoi hop
     */
    @POST
    @Path("getListCombinationOrg")
    @Consumes("application/x-www-form-urlencoded")
    public String getListCombinationOrg(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler mc = new MissionControler();
        String result = mc.getListCombinationOrg(request, data, isSecurity);
        return result;
    }

    /**
     * Lay danh sach nhiem vu da chuyen
     */
    @POST
    @Path("getListTransferredMission")
    @Consumes("application/x-www-form-urlencoded")
    public String getListTransferredMission(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler mc = new MissionControler();
        String result = mc.getListTransferredMission(request, data, isSecurity);
        return result;
    }

    /**
     * Chinh sua nhiem vu
     */
    @POST
    @Path("updateMission")
    @Consumes("application/x-www-form-urlencoded")
    public String updateMission(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler mc = new MissionControler();
        String result = mc.updateMission(request, data, isSecurity);
        return result;
    }

    @POST
    @Path("getCounMission")
    @Consumes("application/x-www-form-urlencoded")
    public String getCounMission(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler mc = new MissionControler();
        String result = mc.getCountMission(request, data, isSecurity);
        return result;
    }

    @POST
    @Path("getInputForCountMission")
    @Consumes("application/x-www-form-urlencoded")
    public String getInputForCountMission(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler mc = new MissionControler();
        String result = mc.getInputForCountMission(request, data, isSecurity);
        return result;
    }

    /**
     * Xoa nhiem vu
     */
    @POST
    @Path("deleteMission")
    @Consumes("application/x-www-form-urlencoded")
    public String deleteMission(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler mc = new MissionControler();
        String result = mc.deleteMission(request, data, isSecurity);
        return result;
    }

    /**
     * Cap nhat tien do nhiem vu
     */
    @POST
    @Path("updateProcess")
    @Consumes("application/x-www-form-urlencoded")
    public String updateProcess(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler mc = new MissionControler();
        String result = mc.updateProcess(request, data, isSecurity);
        return result;
    }

    /**
     * Phe duyet/tu choi tien do nhiem vu
     */
    @POST
    @Path("approveOrRejectProcess")
    @Consumes("application/x-www-form-urlencoded")
    public String approveOrRejectProcess(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler mc = new MissionControler();
        String result = mc.approveOrRejectProcess(request, data, isSecurity);
        return result;
    }

    /**
     * Sua noi dung/ket qua thuc hien phoi hop nhiem vu
     */
    @POST
    @Path("updateContentOrResultOfCombinationOrg")
    @Consumes("application/x-www-form-urlencoded")
    public String updateContentOrResultOfCombinationOrg(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler mc = new MissionControler();
        String result = mc.updateContentOrResultOfCombinationOrg(request, data, isSecurity);
        return result;
    }

    //code phuc vu web 2.0
    /**
     * tiepnv6 add
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getMissionCommanderList")
    @Consumes("application/x-www-form-urlencoded")
    public String getMissionCommanderList(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler mc = new MissionControler();
        String result = mc.getListMissionApproved(request, data, isSecurity);
        return result;
    }

    /**
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("approvedMissionByCommander")
    @Consumes("application/x-www-form-urlencoded")
    public String approvedMissionByCommander(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler mc = new MissionControler();
        String result = mc.approveMissionByCommander(request, data, isSecurity);
        return result;
    }

    /**
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("rejectMissionByCommander")
    @Consumes("application/x-www-form-urlencoded")
    public String rejectMissionByCommander(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler mc = new MissionControler();
        String result = mc.rejectMissionByCommander(request, data, isSecurity);
        return result;
    }

    /**
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListOrg")
    @Consumes("application/x-www-form-urlencoded")
    public String getListOrg(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler mc = new MissionControler();
        String result = mc.getListOrg(request, data, isSecurity);
        return result;
    }

    /**
     * @author thienng1
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getMissionResovleIssueList")
    @Consumes("application/x-www-form-urlencoded")
    public String getMissionResovleIssueList(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler mc = new MissionControler();
        String result = mc.findMissionResovleIssueList(request, data, isSecurity);
        return result;
    }

    /**
     * tamcd add
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("doUpdatePercentService")
    @Consumes("application/x-www-form-urlencoded")
    public String doUpdatePercentService(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler mc = new MissionControler();
        String result = mc.doUpdatePercentService(request, data, isSecurity);
        return result;
    }

    /**
     * <b>Dem so luong nhiem vu hien thi tren menu</b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("CountMission")
    @Consumes("application/x-www-form-urlencoded")
    public String countMission(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler mc = new MissionControler();
        String result = mc.countMission(request, data, isSecurity);
        return result;
    }

    /**
     * <b>Lay ra danh sach lich su tac dong nhiem vu</b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListMissionLog")
    @Consumes("application/x-www-form-urlencoded")
    public String getListMissionLog(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler mc = new MissionControler();
        String result = mc.getListMissionLog(isSecurity, data, request);
        return result;
    }

    /**
     * Lay count nhiệm vụ chuyên quản luanvd
     *
     * @param request
     * @param data du lieu gui len
     * @param isSecurity 1 - Co ma hoa du lieu, 0 - Khong ma hoa du lieu
     * @return
     */
    @POST
    @Path("getCountByOrgPerformSpecialized")
    @Consumes("application/x-www-form-urlencoded")
    public String getCountByOrgPerformSpecialized(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler mc = new MissionControler();
        String result = mc.getCountByOrgPerformSpecialized(request, data, isSecurity);
        return result;
    }

    /**
     * Lay danh sach ddown vi chuyen quan theo cau hinh
     *
     * @param request
     * @param data : du lieu client gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListOrgSpecialized")
    @Consumes("application/x-www-form-urlencoded")
    public String getListOrgSpecialized(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler tc = new MissionControler();
        String result = tc.getListOrgSpecialized(request, data, isSecurity);
        return result;
    }

    /**
     * cấu hình
     *
     * @param request
     * @param data : du lieu client gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("configSpecializedManagement")
    @Consumes("application/x-www-form-urlencoded")
    public String configSpecializedManagement(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler tc = new MissionControler();
        String result = tc.configSpecializedManagement(request, data, isSecurity);
        return result;
    }

    /**
     * Lấy danh sách đơn vị cấu hình
     *
     * @param request
     * @param data : du lieu client gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getConfigMissionByUser")
    @Consumes("application/x-www-form-urlencoded")
    public String getConfigMissionByUser(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler tc = new MissionControler();
        String result = tc.getConfigMissionByUser(request, data, isSecurity);
        return result;
    }

    //getCountMissionSpecialized
    /**
     * Lấy count danh sách đơn vị cấu hình
     *
     * @param request
     * @param data : du lieu client gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getCountMissionSpecialized")
    @Consumes("application/x-www-form-urlencoded")
    public String getCountMissionSpecialized(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler tc = new MissionControler();
        String result = tc.getCountMissionSpecialized(request, data, isSecurity);
        return result;
    }

    //getListMissionUpcomingDeadline
    /**
     * Lấy danh sach nhiem vu sap den han
     *
     * @param request
     * @param data : du lieu client gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListMissionUpcomingDeadline")
    @Consumes("application/x-www-form-urlencoded")
    public String getListMissionUpcomingDeadline(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler tc = new MissionControler();
        String result = tc.getListMissionUpcomingDeadline(request, data, isSecurity);
        return result;
    }

//    /**
//     * @Author SonDN Cập nhật tiến độ nhiệm vụ phối hợp
//     * @param request
//     * @param data : du lieu client gui len
//     * @param isSecurity
//     * @return
//     */
//    @POST
//    @Path("updateProcessCombinationOrg")
//    @Consumes("application/x-www-form-urlencoded")
//    public String updateProcessCombinationOrg(@Context HttpServletRequest request,
//            @FormParam("data") String data,
//            @FormParam("isSecurity") String isSecurity) {
//        
//        MissionControler tc = new MissionControler();
//        String result = tc.updateProcessCombinationOrg(request, data, isSecurity);
//        
//        return result;
//    }
    /**
     * @Author HaNH Bo sung thong tin nhiem vu
     * @param request
     * @param data : du lieu client gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("addInformationMission")
    @Consumes("application/x-www-form-urlencoded")
    public String addInformationMission(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler tc = new MissionControler();
        String result = tc.addInformationMission(request, data, isSecurity);
        
        return result;
    }

    /**
     * @Author HaNH Dong nhiem vu
     * @param request
     * @param data : du lieu client gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("closeMission")
    @Consumes("application/x-www-form-urlencoded")
    public String closeMission(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler tc = new MissionControler();
        String result = tc.closeMission(request, data, isSecurity);
        return result;
    }

    /**
     * <b>lấy danh sách nội dung bổ sung nhiệm vụ</b>
     *
     * @Author cuongnv
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListAddionalMission")
    @Consumes("application/x-www-form-urlencoded")
    public String getListAddionalMission(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler tc = new MissionControler();
        String result = tc.getListAddionalMission(request, data, isSecurity);
        return result;
    }

    /**
     * <b>lấy danh sách nhiệm vụ</b>
     *
     * @Author cuongnv
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("findMissionByCondition")
    @Consumes("application/x-www-form-urlencoded")
    public String findMissionByCondition(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler tc = new MissionControler();
        String result = tc.findMissionByCondition(request, data, isSecurity);
        return result;
    }

    /**
     * @Author HaNH Check fill tien do con moi nhat
     * @param request
     * @param data : du lieu client gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getSubMissionNewestToFill")
    @Consumes("application/x-www-form-urlencoded")
    public String getSubMissionNewestToFill(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler tc = new MissionControler();
        String result = tc.getSubMissionNewestToFill(request, data, isSecurity);
        return result;
    }

    /**
     * * @Author HaNH Sua dau moi thuc hien
     * * @param request
     * @param data : du lieu client gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("editPerformIdMission")
    @Consumes("application/x-www-form-urlencoded")
    public String editPerformIdMission(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler tc = new MissionControler();
        String result = tc.editPerformIdMission(request, data, isSecurity);
        return result;
    }

    /**
     * @Author SonDN lay danh sach nguon goc
     * @param request
     * @param data : du lieu client gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListSourceMap")
    @Consumes("application/x-www-form-urlencoded")
    public String getListSourceMap(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler tc = new MissionControler();
        String result = tc.getListSourceMaps(request, data, isSecurity);
        return result;
    }

    /**
     * Lay danh sach trang thai nhiem vu
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListMissionStatus")
    @Consumes("application/x-www-form-urlencoded")
    public String getListMissionStatus(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler tc = new MissionControler();
        String result = tc.getListMissionStatus(request, data, isSecurity);
        return result;
    }

    @POST
    @Path("CreateTextFromMissionList")
    @Consumes("application/x-www-form-urlencoded")
    public Object createTextFromMissionList(
            @Context HttpServletRequest request,
            @FormParam("data") String data) {
        
        MissionControler controller = new MissionControler();
        return controller.createTextFromMissionList(request, data);
    }
    
    @POST
    @Path("ReportMissionProcess")
    @Consumes("application/x-www-form-urlencoded")
    public Object reportMissionProcess(
            @Context HttpServletRequest request,
            @FormParam("appCode") String appCode,
            @FormParam("appPass") String appPass,
            @FormParam("data") String data) {
        
        MissionControler controller = new MissionControler();
        return controller.reportMissionProcess(request, appCode, appPass, data);
    }
    
    @POST
    @Path("SaveMissionOrder")
    @Consumes("application/x-www-form-urlencoded")
    public Object saveMissionOrder(
            @Context HttpServletRequest request,
            @FormParam("data") String data) {
        
        MissionControler controller = new MissionControler();
        return controller.saveMissionOrder(request, data);
    }

    @GET
    @Path("getListGeneralManager")
    @Consumes("application/x-www-form-urlencoded")
    public Object getListGeneralManager(
            @Context HttpServletRequest request) {
        
        MissionControler controller = new MissionControler();
        return controller.getListGeneralManager();
    }
    
    @GET
    @Path("GetMissionWarning/{assignId}")
    public Object getMissionWarning(
            @Context HttpServletRequest request,
            @PathParam("assignId") Long assignId) {
        
        MissionControler controller = new MissionControler();
        return controller.getMissionWarning(assignId, null, 0);
    }
    
    @GET
    @Path("GetMissionWarningByOrg/{orgId}/{type}")
    public Object getMissionWarningByOrg(
            @Context HttpServletRequest request,
            @PathParam("orgId") Long orgId,
            @PathParam("type") Integer type) {
        
        MissionControler controller = new MissionControler();
        return controller.getMissionWarning(null, orgId, type);
    }

    @GET
    @Path("getListPerformingOrg")
    @Consumes("application/x-www-form-urlencoded")
    public Object getListPerformingOrg(
            @Context HttpServletRequest request) {
        
        MissionControler controller = new MissionControler();
        return controller.getListPerformingOrg();
    }
    
    @POST
    @Path("checkExtendable")
    @Consumes("application/x-www-form-urlencoded")
    public String checkExtendable(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler tc = new MissionControler();
        String result = tc.checkExtendable(request, data, isSecurity);
        return result;
    }
    
    @POST
    @Path("getCountMissionNeedCompleted")
    @Consumes("application/x-www-form-urlencoded")
    public String getCountMissionNeedCompleted(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler tc = new MissionControler();
        String result = tc.getCountMissionNeedCompleted(request, data, isSecurity);
        return result;
    }
    
    @POST
    @Path("findDueDateMission")
    @Consumes("application/x-www-form-urlencoded")
    public String findDueDateMission(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler tc = new MissionControler();
        String result = tc.findDueDateMission(request, data, isSecurity);
        return result;
    }
    
    @POST
    @Path("getFieldIdFromOrgPerform")
    @Consumes("application/x-www-form-urlencoded")
    public String getFieldIdFromOrgPerform(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        
        MissionControler tc = new MissionControler();
        String result = tc.getFieldIdFromOrgPerform(request, data, isSecurity);
        return result;
    }

    @POST
    @Path("getViewSourceMap")
    @Produces("application/x-www-form-urlencoded")
    public String getViewSourceMap(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        MissionControler mc = new MissionControler();
        return mc.getViewSourceMap(request, data, isSecurity);
    }
}
