/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.constants.InputStreamWithFileDeletion;
import com.viettel.voffice.database.dao.SourceMapDAO;
import com.viettel.voffice.database.dao.document.DocumentDAO;
import com.viettel.voffice.database.dao.document.VHROrgDAO;
import com.viettel.voffice.database.dao.meeting.MeetingDAO;
import com.viettel.voffice.database.dao.sms.SmsDAO;
import com.viettel.voffice.database.dao.staff.OrgDAO;
import com.viettel.voffice.database.dao.staff.StaffDAO;
import com.viettel.voffice.database.dao.staff.UserDAO;
import com.viettel.voffice.database.dao.staff.UserRoleDAO;
import com.viettel.voffice.database.dao.task.MissionDAO;
import com.viettel.voffice.database.dao.task.TaskCommonDAO;
import com.viettel.voffice.database.entity.EntityConfigUser;
import com.viettel.voffice.database.entity.EntityFileAttachment;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityMissionStatus;
import com.viettel.voffice.database.entity.EntitySearchMission;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.EntityUserRole;
import com.viettel.voffice.database.entity.EntityVhrEmployee;
import com.viettel.voffice.database.entity.EntityVhrOrg;
import com.viettel.voffice.database.entity.User.EntityUser;
import com.viettel.voffice.database.entity.User.Vof2_EntityUser;
import com.viettel.voffice.database.entity.task.EntityMission;
import com.viettel.voffice.database.entity.task.EntityMissionApproved;
import com.viettel.voffice.database.entity.task.EntityMissionCount;
//import com.viettel.voffice.database.entity.task.EntityCombination;
//import com.viettel.voffice.database.entity.task.EntityMissionChild;
import com.viettel.voffice.database.entity.task.EntityMissionDetail;
import com.viettel.voffice.database.entity.task.EntityMissionLog;
import com.viettel.voffice.database.entity.task.EntityMissionProcess;
import com.viettel.voffice.database.entity.task.EntityMissionProcessReport;
import com.viettel.voffice.database.entity.task.EntityMissionSigning;
import com.viettel.voffice.database.entity.task.EntityMissionWarning;
import com.viettel.voffice.database.entity.task.EntityOrgCombinationMap;
import com.viettel.voffice.database.entity.task.EntitySourceMap;
import com.viettel.voffice.database.entity.task.RequestUpdateProcess;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.DateUtils;
import com.viettel.voffice.utils.LogUtils;




/**
 *
 * @author thanght6
 */
@SuppressWarnings("deprecation")
public class MissionControler {

    public static final String ROOT_ACTION = "Mission";
    /**
     * Log loi
     */
    private static final Logger logger = Logger.getLogger(MissionControler.class);
    // Ten class bao gom ca ten package
    private static final String CLASS_NAME = MissionControler.class.getName();

    /**
     * <b>Lay danh sach nhiem vu</b><br>
     *
     * @author thanght6
     * @param request
     * @param data
     * @param isSecurity
     * @return Tra ve danh sach nhiem vu
     */
    public String getListMission(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        if (!userGroup.getCheckSessionOk()) {
            logger.error("getListMission (Lay danh sach nhiem vu) - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Neu user null hoac user id null
        // -> Tra ve thong bao khong co quyen
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            logger.error("getListMission (Lay danh sach nhiem vu) - Khong co"
                    + " thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        Long userId = user.getUserId();
        // Lay danh sach don vi ma user co vai tro thu truong/lanh dao/tro ly
        List<Long> listSysOrgId = new ArrayList<>();
        // Lay danh sach don vi user co vai tro lanh dao/thu truong
        List<Long> listManagementOrgId = user.getListManagementOrg();
        if (!CommonUtils.isEmpty(listManagementOrgId)) {
            listSysOrgId.addAll(listManagementOrgId);
        }
        // Lay danh sach don vi user co vai tro tro ly
        List<Long> listAssistantOrgId = user.getListAssistantOrg();
        if (!CommonUtils.isEmpty(listAssistantOrgId)) {
            listSysOrgId.addAll(listAssistantOrgId);
        }
        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
        log.setUserName(cardId);
        // Neu user khong co vai tro thu truong/lanh dao/tro ly
        // -> Tra ve thong bao khong co quyen
        if (CommonUtils.isEmpty(listSysOrgId)) {
            logger.error("getListMission (Lay danh sach nhiem vu)- User khong co"
                    + " vai tro thu truong/lanh dao - username: " + cardId);
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // HashMap don vi user co vai tro tro ly va cac don vi chuyen huong tuong ung
        HashMap<Long, List<Long>> hmSpecializedOrgId = user.getHmSpecializedOrgId();
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.IS_COUNT,
                ConstantsFieldParams.KEYWORD,
                ConstantsFieldParams.TYPE,
                ConstantsFieldParams.ORG_ID,
                ConstantsFieldParams.STATUS,
                ConstantsFieldParams.MISSION_START_DATE,
                ConstantsFieldParams.MISSION_COMPLETE_DATE,
                ConstantsFieldParams.START_RECORD,
                ConstantsFieldParams.PAGE_SIZE
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Dem so luong hay khong
            String isCount = listValue.get(0);
            // Tu khoa tim kiem
            String keyword = listValue.get(1);
            // Loai:
            // 1: Nhiem vu giao di
            // 2: Nhiem vu duoc giao
            Integer type = Integer.parseInt(listValue.get(2));
            // Id don vi
            Long orgId = null;
            String strOrgId = listValue.get(3);
            if (!CommonUtils.isEmpty(strOrgId)) {
                orgId = Long.parseLong(strOrgId);
            }
            // Trang thai
            Long status = 0L;
            String strStatus = listValue.get(4);
            if (!CommonUtils.isEmpty(strStatus)) {
                status = Long.parseLong(strStatus);
            }
            // Ngay bat dau tu
            String startDateFrom = listValue.get(5);
            // Ngay bat dau den
            String startDateTo = listValue.get(6);
            // Vi tri ban ghi lay ra
            Long startRecord = 0L;
            String strStartRecord = listValue.get(7);
            if (!CommonUtils.isEmpty(strStartRecord)) {
                startRecord = Long.parseLong(strStartRecord);
            }
            // So ban ghi lay ra
            Long pageSize = 10L;
            String strPageSize = listValue.get(8);
            if (!CommonUtils.isEmpty(strPageSize)) {
                pageSize = Long.parseLong(strPageSize);
            }
            MissionDAO missionDAO = new MissionDAO();
            Object result = missionDAO.getListMission(isCount, type,
                    userId, listSysOrgId, hmSpecializedOrgId, orgId, keyword,
                    status, startDateFrom, startDateTo, startRecord, pageSize);
            if (result == null) {
                logger.error("getListMission (Lay danh sach nhiem vu) - result = null"
                        + " - username: " + cardId + "\ndata: " + data);
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            }
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            logger.error("getListMission (Lay danh sach nhiem vu) - Exception -"
                    + " userId: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    /**
     * <b>Lay chi tiet nhiem vu theo id</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getMissionDetail(HttpServletRequest request,
            String data, String isSecurity) {

        String response;
        String[] keys = new String[]{
            ConstantsFieldParams.MISSION_ID,
            ConstantsFieldParams.LANGUAGE,
            ConstantsFieldParams.IS_WEB
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.returnResultAfterLogSessionTimeout(userGroup);
        }
        // Neu user null hoac user id null
        // -> Tra ve thong bao khong co quyen
        if (!userGroup.checkUserId2()) {
            return FunctionCommon.returnResultAfterLogNoInfo(userGroup.getUserId2());
        }
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        List<Long> listOrgId = new ArrayList<>();
        // Danh sach don vi user co vai tro lanh dao/thu truong don vi
        List<Long> listManagementOrgId = user.getListManagementOrg();
        if (!CommonUtils.isEmpty(listManagementOrgId)) {
            listOrgId.addAll(listManagementOrgId);
        }
        // Danh sach don vi user co vai tro tro ly, tro ly chinh tri
        List<Long> listAssistantOrgId = user.getListAssistantOrgIdForMission();
        if (!CommonUtils.isEmpty(listAssistantOrgId)) {
            listOrgId.addAll(listAssistantOrgId);
        }
        // Map don vi tro ly voi don vi chuyen huong
        HashMap<Long, List<Long>> hmSpecializedOrgId = user.getHmSpecializedOrgId();
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            Long missionId = Long.parseLong(listValue.get(0));
            // Lay ma ngon ngu client can dung
            String langCode = user.getLanguageCode() == null ? "vi" : user.getLanguageCode();
            if (!CommonUtils.isEmpty(listValue.get(1))) {
                langCode = listValue.get(1);
            }
            //Begin::cuongnv::24/2/2017
            //Kiem tra co phai web gui len ko
            Integer isWeb = null;
            Integer sourceType = null;
            try {
                isWeb = Integer.parseInt(listValue.get(2));
            } catch (NumberFormatException e) {
            }
            //End
            MissionDAO missionDAO = new MissionDAO();
            EntityMission mission = missionDAO.getMissionDetail(missionId,
                    userGroup.getUserId2(), listManagementOrgId, listAssistantOrgId,
                    listOrgId, langCode, isWeb, hmSpecializedOrgId);
            // Neu khong lay duoc nhiem vu
            // -> Tra ve thong bao loi server
            if (mission == null) {
                
                response = FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
                return FunctionCommon.returnResultAfterLogResultNull(response,
                        userGroup.getUserId2());
            }
            // Lay danh sach nguon goc cua nhiem vu
            SourceMapDAO sourceMapDAO = new SourceMapDAO();
            List<EntitySourceMap> listSource = sourceMapDAO.getListSourceByObject(missionId,
                    Constants.SourceMap.ObjectType.MISSION, false);
            mission.setListSource(listSource);
            
            //lay ngay gia han toi da
            if (listSource != null && listSource.size() > 0) {
                sourceType = listSource.get(0).getSourceType();
                for (EntitySourceMap sm : listSource) {
                    if (sm.getSourceType() == 8 || sm.getSourceType() == 9) {
                        sourceType = sm.getSourceType();
                    }
                }
            }
            Object extendDate = missionDAO.checkExtendable(missionId, sourceType);
//            System.out.println("missionId: "+missionId+" - sourceType:" +sourceType+" - extendDate "+extendDate);
            if (extendDate != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                mission.setExtendableDate(sdf.format((Date)extendDate));
            } else {
                mission.setExtendableDate(null);
            }
//            System.out.println("mission.getExtendableDate(): "+mission.getExtendableDate());
            //beign::cuongnv::15/12/2016::Lay danh sach vb tham chieu
            listSource = sourceMapDAO.getListSourceByObject(missionId,
                    Constants.SourceMap.ObjectType.MISSION_DOCUMENT_REF, false);
            mission.setListDocumentRef(listSource);
            
            // neu la tu mobile
            if (null == isWeb) {
                List<EntitySourceMap> lstDoc = missionDAO.getListDocReportAllChild(missionId);
                mission.setListReportChild(lstDoc);
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, mission, userGroup);
        } catch (Exception ex) {
            return FunctionCommon.returnResultAfterLog(userGroup, ex);
        }
    }

//    public static void main(String[] args) {
//        Integer sourceType = null;
//        MissionDAO missionDAO = new MissionDAO();
//        EntityMission mission = missionDAO.getMissionDetail(34047L,
//                470544L, null, null,
//                null, "vi", null, null);
//         SourceMapDAO sourceMapDAO = new SourceMapDAO();
//        List<EntitySourceMap> listSource = sourceMapDAO.getListSourceByObject(34047L,
//                Constants.SourceMap.ObjectType.MISSION);
//        for (EntitySourceMap sm : listSource) {
//            if (sm.getSourceType() == 8 || sm.getSourceType() == 9) {
//                sourceType = sm.getSourceType();
//            }
//        }
//        Object extendDate = missionDAO.checkExtendable(34047L, sourceType);
//        if (extendDate != null) {
//            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
//            mission.setExtendableDate(sdf.format((Date)extendDate));
//        } else {
//            mission.setExtendableDate(null);
//        }
//        System.out.println(mission.getExtendableDate());
//    }
    
    /**
     * <b>Lay danh sach file dinh kem cua nhiem vu</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListFileAttachment(HttpServletRequest request,
            String data, String isSecurity) {

        String response;
        String[] keys = new String[]{
            ConstantsFieldParams.IS_COUNT,
            ConstantsFieldParams.MISSION_ID,
            ConstantsFieldParams.START_RECORD,
            ConstantsFieldParams.PAGE_SIZE
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.returnResultAfterLogSessionTimeout(userGroup);
        }
        // Lay danh sach don vi ma user la lanh dao hoac thu truong don vi hoac tro ly
        List<Long> listSysOrgId = userGroup.getVof2_ItemEntityUser()
                .getListOrgWhichHasThePermissionToMission();
        if (CommonUtils.isEmpty(listSysOrgId)) {
            return FunctionCommon.returnResultAfterLogNoInfo(userGroup.getUserId2());
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            // Dem so luong hay lay danh sach
            String isCount = listValue.get(0);
            // Id nhiem vu
            Long missionId = Long.parseLong(listValue.get(1));
            // Vi tri ban ghi lay ra
            String strStartRecord = listValue.get(2);
            Long startRecord = 0L;
            if (!"".equals(strStartRecord)) {
                startRecord = Long.parseLong(strStartRecord);
            }
            // So ban ghi lay ra
            String strPageSize = listValue.get(3);
            Long pageSize = 10L;
            if (!"".equals(strPageSize)) {
                pageSize = Long.parseLong(strPageSize);
            }
            TaskCommonDAO tcd = new TaskCommonDAO();
            Object result = tcd.getListFileAttachment(isCount, Constants.OBJECT_TYPE_MISSION,
                    missionId, startRecord, pageSize);
            // Loi server
            if (result == null) {
                response = FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
                return FunctionCommon.returnResultAfterLogResultNull(response,
                        userGroup.getUserId2());
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            return FunctionCommon.returnResultAfterLog(userGroup, ex);
        }
    }

    /**
     * <b>Lay lich su cap nhat tien do theo id nhiem vu</b><br>
     *
     * @author thanght6
     *
     * @param request
     * @param data
     * @param isSecurity
     *
     * @return
     */
    public String getMissionProcessHistory(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        if (!userGroup.getCheckSessionOk()) {
            logger.error("getListMission - No session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }

        // Neu user null hoac user id null
        // -> Tra ve thong bao khong co quyen
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            logger.error("getListMission - user null || user id null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        Long userId = user.getUserId();

        // Lay danh sach don vi ma user la lanh dao hoac thu truong don vi hoac tro ly hoac dau moi thuc hien
        List<Long> listOrgId = user.getListOrgWhichHasThePermissionToMission();

        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            // Parse du lieu gui len theo key
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.IS_COUNT,
                ConstantsFieldParams.MISSION_ID,
                ConstantsFieldParams.MISSION_ORG_TYPE,
                ConstantsFieldParams.START_RECORD,
                ConstantsFieldParams.PAGE_SIZE,
                //HaNH Dau moi thuc hien va don vi thuc hien
                ConstantsFieldParams.MISSION_PERFORM_ID,
                ConstantsFieldParams.MISSION_ORG_PERFORM_ID,
                // datdc add dk bo qua check don vi
                ConstantsFieldParams.MISSION_SKIP_CHECK_ORG
            //End HaNH
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Lay so luong hay lay danh sach
            String isCount = listValue.get(0);

            // Id nhiem vu
            Long missionId = Long.parseLong(listValue.get(1));

            // Lay theo don vi giao nhiem vu hoac don vi thuc hien hoac don vi phoi hop
            String strOrgType = listValue.get(2);
            int orgType = -1;
            if (!CommonUtils.isEmpty(strOrgType)) {
                orgType = Integer.parseInt(strOrgType);
            }

            // Vi tri ban ghi lay ra
            String strStartRecord = listValue.get(3);
            Long startRecord = null;
            try {
                startRecord = Long.parseLong(strStartRecord);
            } catch (NumberFormatException ex) {
            }
//            Long startRecord = 0L;
//            if (!CommonUtils.isEmpty(strStartRecord)) {
//                startRecord = Long.parseLong(strStartRecord);
//            }

            // So ban ghi lay ra
            String strPageSize = listValue.get(4);
            Long pageSize = null;
            try {
                pageSize = Long.parseLong(strPageSize);
            } catch (NumberFormatException ex) {
            }
            //HaNH: Phuc vu cho WEB, chi tren WEB moi co dau moi thuc hien. Dau moi thuc hien 
            //co quyen xem danh sach lich su nhu tro ly,lanh dao, thu truong cua don vi thuc hien
            //Lay ID dau moi thuc hien
            Long performId = null;
            try {
                performId = Long.parseLong(listValue.get(5));
            } catch (NumberFormatException e) {
            }
            //Neu nhiem vu co dau moi thuc hien va trung voi user dang nhap thi
            //gan don vi cua dau moi thuc hien (chinh la don vi thuc hien) vao list
            if (performId != null && userId.equals(performId)) {
                Long orgPerformId = Long.parseLong(listValue.get(6));
                listOrgId.add(orgPerformId);
            }
//            Long pageSize = 10L;
//            if (!CommonUtils.isEmpty(strPageSize)) {
//                pageSize = Long.parseLong(strPageSize);
//            }
            // datdc add
            Boolean isSkipCheck = false;
            if (!CommonUtils.isEmpty(listValue.get(7))) {
                isSkipCheck = Boolean.valueOf(listValue.get(7));
            }
            MissionDAO missionDAO = new MissionDAO();
            Object result = missionDAO.getMissionProcessHistory(isCount,
                    missionId, orgType, listOrgId, isSkipCheck, startRecord, pageSize);
            // datdc end
            // Neu result = null -> Tra ve thong bao loi server
            if (result == null) {
                logger.error("getMissionProcessHistory - userId = " + userId
                        + " - result null");
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            }
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            logger.error("getMissionProcessHistory - userId = " + userId
                    + " - Exception: ", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }
    
    /**
     * Lay cap nhat tien do cuoi cung cua moi nhiem vu con
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getLastMissionProcessOfSubMissions(HttpServletRequest request,
            String data, String isSecurity) {

        String response;
        String[] keys = new String[]{
            ConstantsFieldParams.IS_COUNT,
            ConstantsFieldParams.MISSION_ID,
            ConstantsFieldParams.START_RECORD,
            ConstantsFieldParams.PAGE_SIZE,
            "isChild"
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.returnResultAfterLogSessionTimeout(userGroup);
        }
        // Lay danh sach don vi ma user la lanh dao hoac thu truong don vi hoac tro ly
        List<Long> listSysOrgId = userGroup.getVof2_ItemEntityUser().getListOrgWhichHasThePermissionToMission();
        if (CommonUtils.isEmpty(listSysOrgId)) {
            return FunctionCommon.returnResultAfterLogNoInfo(userGroup.getUserId2());
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            // Lay so luong hay lay danh sach
            String isCount = listValue.get(0);
            // Id nhiem vu
            Long missionId = CommonUtils.isInteger(listValue.get(1)) ? Long.parseLong(listValue.get(1)) : null;
            //Begin::cuongnv::Xu ly phan trang
            Long startRecord = null;
            try {
                startRecord = Long.parseLong(listValue.get(2));
            } catch (NumberFormatException ex) {
            }
            Long pageSize = null;
            try {
                pageSize = Long.parseLong(listValue.get(3));
            } catch (NumberFormatException ex) {
            }
            boolean isApprove = !"1".equals(listValue.get(4));
            //End
            MissionDAO missionDAO = new MissionDAO();
            Object result = missionDAO.getLastMissionProcessOfSubMissions(isCount,
                    missionId, startRecord, pageSize, isApprove);
            // Loi server
            if (result == null) {
                response = FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
                return FunctionCommon.returnResultAfterLogResultNull(response, userGroup.getUserId2());
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            return FunctionCommon.returnResultAfterLog(userGroup, ex);
        }
    }

    /**
     * Lay danh sach don vi phoi hop
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListCombinationOrg(HttpServletRequest request,
            String data, String isSecurity) {

        String[] keys = new String[]{
            ConstantsFieldParams.IS_COUNT,
            ConstantsFieldParams.MISSION_ID,
            ConstantsFieldParams.START_RECORD,
            ConstantsFieldParams.PAGE_SIZE
        };
        String response;
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.returnResultAfterLogSessionTimeout(userGroup);
        }
        // Lay danh sach don vi user co vai tro thu truong/lanh dao/tro ly/tro ly chinh tri
        List<Long> listSysOrgId = userGroup.getVof2_ItemEntityUser()
                .getListOrgWhichHasThePermissionToMission();
        if (CommonUtils.isEmpty(listSysOrgId)) {
            return FunctionCommon.returnResultAfterLogNoInfo(userGroup.getUserId2());
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            // Lay danh sach hay lay so luong
            String isCount = listValue.get(0);
            // Id nhiem vu
            Long missionId = Long.parseLong(listValue.get(1));
            // Vi tri ban ghi lay ra
            String strStartRecord = listValue.get(2);
            Long startRecord = 0L;
            if (!"".equals(strStartRecord)) {
                startRecord = Long.parseLong(strStartRecord);
            }
            // So ban ghi lay ra
            String strPageSize = listValue.get(3);
            Long pageSize = 10L;
            if (!"".equals(strPageSize)) {
                pageSize = Long.parseLong(strPageSize);
            }
            MissionDAO missionDAO = new MissionDAO();
            Object result = missionDAO.getListCombinationOrg(isCount, missionId,
                    startRecord, pageSize, listSysOrgId);
            // Loi server
            if (result == null) {
                response = FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
                return FunctionCommon.returnResultAfterLogResultNull(response,
                        userGroup.getUserId2());
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            return FunctionCommon.returnResultAfterLog(userGroup, ex);
        }
    }

    /**
     * <b>Lay danh nhiem vu da chuyen</b>
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    public String getListTransferredMission(HttpServletRequest request,
            String data, String isSecurity) {

        String response;
        String[] keys = new String[]{
            ConstantsFieldParams.IS_COUNT,
            ConstantsFieldParams.MISSION_ID,
            ConstantsFieldParams.START_RECORD,
            ConstantsFieldParams.PAGE_SIZE
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.returnResultAfterLogSessionTimeout(userGroup);
        }
        // Lay danh sach don vi ma user la lanh dao hoac thu truong don vi hoac tro ly
        List<Long> listSysOrgId = userGroup.getVof2_ItemEntityUser().getListOrgWhichHasThePermissionToMission();
        if (CommonUtils.isEmpty(listSysOrgId)) {
            return FunctionCommon.returnResultAfterLogNoInfo(userGroup.getUserId2());
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            // Lay so luong hay lay danh sach
            String isCount = listValue.get(0);
            // Id nhiem vu
            Long missionId = Long.parseLong(listValue.get(1));
            // Vi tri ban ghi lay ra
            String strStartRecord = listValue.get(2);
            Long startRecord = 0L;
            if (!"".equals(strStartRecord)) {
                startRecord = Long.parseLong(strStartRecord);
            }
            // So ban ghi lay ra
            String strPageSize = listValue.get(3);
            Long pageSize = 10L;
            if (!"".equals(strPageSize)) {
                pageSize = Long.parseLong(strPageSize);
            }
            MissionDAO missionDAO = new MissionDAO();
            Object result = missionDAO.getListTransferredMission(isCount, missionId,
                    listSysOrgId, startRecord, pageSize);
            // Loi server
            if (result == null) {
                response = FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
                return FunctionCommon.returnResultAfterLogResultNull(response, userGroup.getUserId2());
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            return FunctionCommon.returnResultAfterLog(userGroup, ex);
        }
    }

    /**
     * Chinh sua nhiem vu
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String updateMission(HttpServletRequest request,
            String data, String isSecurity) {
        
        String[] keys = new String[]{
            ConstantsFieldParams.MISSION_ID,
            ConstantsFieldParams.MISSION_ORG_ASSIGN_ID,
            ConstantsFieldParams.MISSION_ASSIGN_ID,
            ConstantsFieldParams.MISSION_NAME,
            ConstantsFieldParams.MISSION_CONTENT,
            ConstantsFieldParams.MISSION_TARGET,
            ConstantsFieldParams.MISSION_FIELD_ID,
            ConstantsFieldParams.MISSION_FREQUENCE_UPDATE,
            ConstantsFieldParams.MISSION_START_DATE,
            ConstantsFieldParams.MISSION_COMPLETE_DATE,
            //cuongnv::bo xung cho web
            ConstantsFieldParams.MISSION_ORG_PERFORM_ID,
            ConstantsFieldParams.MISSION_LEVEL_IMPORTANCE,
            ConstantsFieldParams.MISSION_MISSION_GROUP,
            ConstantsFieldParams.MISSION_PERFORM_ID,
            ConstantsFieldParams.MISSION_IS_DOC_REPORT,
            "weight",
            "missionType",
            "isEdit",
            "missionResource",
            "missionNormId",
            // datdc Them request reason
            ConstantsFieldParams.REQ_REASON,
            // datdc them check tu man nao
            ConstantsFieldParams.REQ_IS_FROM_MISSION,
            "missionClass"
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.returnResultAfterLogSessionTimeout(userGroup);
        }
        Long sysUserId = userGroup.getUserId2();            
        List<Long> listSysOrgId = new ArrayList<>();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        // Lay danh sach don vi user co vai tro thu truong, lanh dao
        List<Long> listManagementOrgId = user2.getListManagementOrg();
        if (!CommonUtils.isEmpty(listManagementOrgId)) {
            listSysOrgId.addAll(listManagementOrgId);
        }
        // Lay danh sach don vi user co vai tro tro ly, tro ly chinh tri
        List<Long> listAssistantOrgId = user2.getListAssistantOrgIdForMission();
        if (!CommonUtils.isEmpty(listAssistantOrgId)) {
            listSysOrgId.addAll(listAssistantOrgId);
        }
        if (CommonUtils.isEmpty(listSysOrgId)) {
            return FunctionCommon.returnResultAfterLogNotAllow(sysUserId);
        }
        // Cac vai tro tro ly chuyen huong
        HashMap<Long, List<Long>> hmSpecializedOrgId = user2.getHmSpecializedOrgId();
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            Long missionId = Long.parseLong(listValue.get(0));
            String srtOrgAssignId = listValue.get(1);
            Long orgAssignId = null;
            if (!"".equals(srtOrgAssignId)) {
                orgAssignId = Long.parseLong(srtOrgAssignId);
            }
            String strAssignId = listValue.get(2);
            Long assignId = null;
            if (!"".equals(strAssignId)) {
                assignId = Long.parseLong(strAssignId);
            }
            String missionName = listValue.get(3);
            String content = listValue.get(4);
            String target = listValue.get(5);

            String strFieldId = listValue.get(6);
            Long fieldId = null;
            if (!"".equals(strFieldId)) {
                fieldId = Long.parseLong(strFieldId);
            }
            String strFrequenceUpdate = listValue.get(7);
            Long frequenceUpdate = null;
            if (!"".equals(strFrequenceUpdate)) {
                frequenceUpdate = Long.parseLong(strFrequenceUpdate);
            }
            String startDate = listValue.get(8);
            String completeDate = listValue.get(9);

            //begin::cuongnv::22/12/2016
            Long orgPerformId = null;
            try {
                orgPerformId = Long.parseLong(listValue.get(10));
            } catch (NumberFormatException ex) {
            }

            Integer levelImportance = null;
            try {
                levelImportance = Integer.parseInt(listValue.get(11));
            } catch (NumberFormatException ex) {
            }

            Integer missionGroup = null;
            try {
                missionGroup = Integer.parseInt(listValue.get(12));
            } catch (NumberFormatException ex) {
            }

            Long performId = null;
            try {
                performId = Long.parseLong(listValue.get(13));
            } catch (NumberFormatException ex) {
            }

            Integer isDocReport = null;
            try {
                isDocReport = Integer.parseInt(listValue.get(14));
            } catch (NumberFormatException ex) {
            }
            //end
            Integer weight = null;
            if (!CommonUtils.isEmpty(listValue.get(15))) {
                weight = Integer.parseInt(listValue.get(15));
            }
            Integer missionType = null;
            if (!CommonUtils.isEmpty(listValue.get(16))) {
                missionType = Integer.parseInt(listValue.get(16));
            }
            // Bo sung them key cho man hinh danh gia nhiem vu de nguoi thuc
            // hien duoc quyen sua
            Integer isEdit = null;
            if (!CommonUtils.isEmpty(listValue.get(17))) {
                isEdit = Integer.parseInt(listValue.get(17));
            }
            String missionResource = listValue.get(18);
            Long missionNormId = null;
            String strMissionNormId = listValue.get(19);
            if (!CommonUtils.isEmpty(strMissionNormId)) {
                missionNormId = Long.parseLong(strMissionNormId);
            }
            // ly do
            String reason = null;
            if (!CommonUtils.isEmpty(listValue.get(20))) {
                reason = listValue.get(20);
            }
            // neu la tu man edit qldv hoac tu mobile
            // thi la null
            Integer isFromMission = null;
            if (!CommonUtils.isEmpty(listValue.get(21))) {
            	isFromMission = Integer.parseInt(listValue.get(21));
            }
            // [tuantm30] Bo sung them tham so
            Long missionClass = CommonUtils.isInteger(listValue.get(22)) ? Long.valueOf(listValue.get(22)) : 1L;
            
            JSONObject json = new JSONObject(userGroup.getInputData());
            // Xoa don vi phoi hop
            List<EntityOrgCombinationMap> listRemoveOrgCombination = new ArrayList<>();
            if (!json.isNull(ConstantsFieldParams.MISSION_REMOVE_ORG_COMBINATION_IDS)) {
                JSONArray removeOrgCombinationArray = json.getJSONArray(ConstantsFieldParams.MISSION_REMOVE_ORG_COMBINATION_IDS);
                if (removeOrgCombinationArray != null && removeOrgCombinationArray.length() > 0) {
                    for (int i = 0; i < removeOrgCombinationArray.length(); i++) {
                        JSONObject j = removeOrgCombinationArray.getJSONObject(i);
                        if (!j.isNull("orgId")) {
                            Long orgId = j.getLong("orgId");
                            EntityOrgCombinationMap orgCombination = new EntityOrgCombinationMap();
                            orgCombination.setOrgCombinationId(orgId);
                            listRemoveOrgCombination.add(orgCombination);
                        }
                    }
                }
            }

            // Them moi don vi phoi hop
            List<EntityOrgCombinationMap> listAddOrgCombination = new ArrayList<>();
            if (!json.isNull(ConstantsFieldParams.MISSION_ADD_ORG_COMBINATION_IDS)) {
                JSONArray addOrgCombinationArray = json.getJSONArray(ConstantsFieldParams.MISSION_ADD_ORG_COMBINATION_IDS);
                if (addOrgCombinationArray != null && addOrgCombinationArray.length() > 0) {
                    for (int i = 0; i < addOrgCombinationArray.length(); i++) {
                        JSONObject j = addOrgCombinationArray.getJSONObject(i);
                        if (!j.isNull("orgId")) {
                            Long orgId = j.getLong("orgId");
                            String orgContent = null;
                            try {
                                orgContent = j.getString("content");
                            } catch (JSONException e) {
                                logger.error(e.getMessage(), e);
                            }
                            EntityOrgCombinationMap orgCombination = new EntityOrgCombinationMap();
                            orgCombination.setOrgCombinationId(orgId);
                            orgCombination.setContent(orgContent);
                            listAddOrgCombination.add(orgCombination);
                        }
                    }
                }
            }
            // Chinh sua don vi phoi hop
            List<EntityOrgCombinationMap> listEditOrgCombination = new ArrayList<>();
            if (!json.isNull(ConstantsFieldParams.MISSION_EDIT_ORG_COMBINATION_IDS)) {
                JSONArray editOrgCombinationArray = json.getJSONArray(ConstantsFieldParams.MISSION_EDIT_ORG_COMBINATION_IDS);
                if (editOrgCombinationArray != null && editOrgCombinationArray.length() > 0) {
                    for (int i = 0; i < editOrgCombinationArray.length(); i++) {
                        JSONObject j = editOrgCombinationArray.getJSONObject(i);
                        if (!j.isNull("orgId")) {
                            Long orgId = j.getLong("orgId");
                            String orgContent = null;
                            try {
                                orgContent = j.getString("content");
                            } catch (JSONException e) {
                                logger.error(e.getMessage(), e);
                            }
                            EntityOrgCombinationMap orgCombination = new EntityOrgCombinationMap();
                            orgCombination.setOrgCombinationId(orgId);
                            orgCombination.setContent(orgContent);
                            listEditOrgCombination.add(orgCombination);
                        }
                    }
                }
            }
            //Begin::cuongnv::22/12/2016
            Gson gson = new Gson();
            // danh sách file đính kèm
            List<EntityFileAttachment> listAttachFile = new ArrayList<>();
            if (json.has(ConstantsFieldParams.LIST_FILE_ATTACH)) {
                JSONArray arr = json.getJSONArray(ConstantsFieldParams.LIST_FILE_ATTACH);
                if (arr != null && arr.length() > 0) {
                    EntityFileAttachment tmp;
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject jSONObject = arr.getJSONObject(i);
                        try {
                            tmp = gson.fromJson(jSONObject.toString(), EntityFileAttachment.class);
                            if (tmp != null) {
                                listAttachFile.add(tmp);
                            }
                        } catch (Exception ex) {
                            logger.error("updateMission: ", ex);
                        }
                    }
                }
            }
            //danh sách nguồn gốc nhiệm vụ
            List<EntitySourceMap> listSourceMap = new ArrayList<>();
            if (json.has(ConstantsFieldParams.LIST_SOURCE_MAP)) {
                JSONArray arr = json.getJSONArray(ConstantsFieldParams.LIST_SOURCE_MAP);
                if (arr != null && arr.length() > 0) {
                    EntitySourceMap tmp;
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject jSONObject = arr.getJSONObject(i);
                        try {
                            tmp = gson.fromJson(jSONObject.toString(), EntitySourceMap.class);
                            if (tmp != null) {
                                listSourceMap.add(tmp);
                            }
                        } catch (Exception ex) {
                            logger.error("updateMission: ", ex);
                        }
                    }
                }
            }
            //danh sách văn bản tham chiều
            List<EntitySourceMap> listDocumentRef = new ArrayList<>();
            if (json.has(ConstantsFieldParams.MISSION_LIST_DOCUMENT_REF)) {
                JSONArray arr = json.getJSONArray(ConstantsFieldParams.MISSION_LIST_DOCUMENT_REF);
                if (arr != null && arr.length() > 0) {
                    EntitySourceMap tmp;
                    Long documentId;
                    String documentName;
                    Integer lvlConfidential;
                    Long sourceMapId;
                    for (int i = 0; i < arr.length(); i++) {
                        tmp = new EntitySourceMap();
                        JSONObject jSONObject = arr.getJSONObject(i);
                        try {
                            documentName = jSONObject.getString("documentName");
                            documentId = jSONObject.getLong("documentId");
                            lvlConfidential = jSONObject.getInt("confidential");
                            sourceMapId = null;
                            try {
                                sourceMapId = jSONObject.getLong("sourceMapId");
                            } catch (JSONException ex) {
                                logger.error(ex.getMessage(), ex);
                            }
                            tmp.setSourceMapId(sourceMapId);
                            tmp.setSourceId(documentId);
                            tmp.setSourceName(documentName);
                            tmp.setConfidential(lvlConfidential);
                            //100517 them thong tin co chuyen van ban hay khong
                            if (jSONObject.has("isSend")) {
                                tmp.setIsSend(jSONObject.getLong("isSend"));
                            }
                            listDocumentRef.add(tmp);
                        } catch (Exception ex) {
                            logger.error("updateMission: ", ex);
                        }
                    }
                }
            }
            //End;
            // Kiem tra quyen sua nhiem vu
            MissionDAO missionDAO = new MissionDAO();
            if (isEdit == null || isEdit != 1) {
                EntityMission mission = missionDAO.checkUserPermissionForMission(
                        sysUserId, listManagementOrgId, listAssistantOrgId, 
                        listSysOrgId, missionId, hmSpecializedOrgId);
                if (!mission.getPermissionOfAssignOrg().isEdit()) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW,
                            null, null);
                }
            }
            int result = missionDAO.updateMission(sysUserId, missionId,
                    orgAssignId, assignId, missionName, content, target,
                    fieldId, frequenceUpdate, startDate, completeDate,
                    listRemoveOrgCombination, listAddOrgCombination,
                    listEditOrgCombination, orgPerformId, levelImportance,
                    missionGroup, performId, isDocReport, listAttachFile,
                    listSourceMap, listDocumentRef, userGroup.getCardId(), weight,
                    missionType, missionResource, missionNormId, isFromMission,
                    missionClass);
            /* HaNH: Gui tin nhan cho don vi phoi hop moi add them sau khi sua nhiem vu thanh cong */
            //Begin
            if (result == 1) {
                //100517 chuyen van ban cho TT, LD, VT don vi
                //neu sua viec co nguon goc tu van ban
                try {
                    //lay danh sach id don vi thuc hien cong viec
                    List<Long> lstOrgId = new ArrayList<>();
                    if (orgPerformId != null) {
                        lstOrgId.add(orgPerformId);
                    }
                    Long tempPerformId;
                    //lay don vi phoi hop them vao
                    if (listAddOrgCombination.size() > 0) {
                        for (EntityOrgCombinationMap combination : listAddOrgCombination) {
                            tempPerformId = combination.getOrgCombinationId();
                            if (tempPerformId != null && !lstOrgId.contains(tempPerformId)) {
                                lstOrgId.add(tempPerformId);
                            }
                        }
                    }
                    //lay don vi phoi hop
                    List<EntityOrgCombinationMap> lstOrgCombination = (List<EntityOrgCombinationMap>) missionDAO.getListCombinationOrg(null,
                            missionId, null, null, listSysOrgId);
                    if (lstOrgCombination != null && lstOrgCombination.isEmpty()) {
                        for (EntityOrgCombinationMap combination : lstOrgCombination) {
                            tempPerformId = combination.getOrgCombinationId();
                            if (tempPerformId != null && !lstOrgId.contains(tempPerformId)) {
                                lstOrgId.add(tempPerformId);
                            }
                        }
                    }
                    if (!CommonUtils.isEmpty(listSourceMap)) {
                        //neu co thay doi nguon goc
                        Long isSend;
                        Integer sourceType;
                        Integer objectType;
                        for (EntitySourceMap objSource : listSourceMap) {
                            sourceType = objSource.getSourceType();
                            objectType = objSource.getObjectType();
                            if (sourceType != null && sourceType == 2
                                    && objectType != null && objectType == 2) {
                                isSend = objSource.getIsSend();
                                //neu la danh dau chuyen va khong phai la bi xoa
                                if ((isSend == null || isSend == 1L) && objSource.getSourceMapId() == null) {
                                    //thuc hien chuyen van ban
                                    DocumentDAO docDAO = new DocumentDAO();
                                    docDAO.sendDocumentToStaffFromMission(objSource.getSourceId(), lstOrgId, null,
                                            userGroup.getItemEntityUser(), userGroup.getVof2_ItemEntityUser(),
                                            assignId, sysUserId, performId, orgAssignId);
                                }
                            }
                        }
                    } else {
                        SourceMapDAO sourceMapDAO = new SourceMapDAO();
                        List<EntitySourceMap> listSourceMap1 = sourceMapDAO.getListSourceByObject(missionId, 2, false);
                        if (!CommonUtils.isEmpty(listSourceMap1)) {
                            Integer sourceType;
                            Integer objectType;
                            for (EntitySourceMap objSource : listSourceMap1) {
                                sourceType = objSource.getSourceType();
                                objectType = objSource.getObjectType();
                                if (sourceType != null && sourceType == 2
                                        && objectType != null && objectType == 2) {
                                    //thuc hien chuyen van ban
                                    DocumentDAO docDAO = new DocumentDAO();
                                    docDAO.sendDocumentToStaffFromMission(objSource.getSourceId(), lstOrgId, null,
                                            userGroup.getItemEntityUser(), userGroup.getVof2_ItemEntityUser(),
                                            assignId, sysUserId, performId, orgAssignId);
                                }
                            }
                        }
                    }
                    //chuyen van ban tham chieu
                    if (!CommonUtils.isEmpty(listDocumentRef)) {
                        //neu co thay doi van ban tham chieu
                        Long isSend;
                        for (EntitySourceMap objSource : listSourceMap) {
                            isSend = objSource.getIsSend();
                            //neu la danh dau chuyen va khong phai la bi xoa
                            if ((isSend == null || isSend == 1L) && objSource.getSourceMapId() == null) {
                                //thuc hien chuyen van ban
                                DocumentDAO docDAO = new DocumentDAO();
                                docDAO.sendDocumentToStaffFromMission(objSource.getSourceId(), lstOrgId, null,
                                        userGroup.getItemEntityUser(), userGroup.getVof2_ItemEntityUser(),
                                        assignId, sysUserId, performId, orgAssignId);
                            }
                        }
                    } else {
                        SourceMapDAO sourceMapDAO = new SourceMapDAO();
                        List<EntitySourceMap> listSourceMap1 = sourceMapDAO.getListSourceByObject(missionId, 6, false);
                        if (!CommonUtils.isEmpty(listSourceMap1)) {
                            for (EntitySourceMap objSource : listSourceMap1) {
                                //thuc hien chuyen van ban
                                DocumentDAO docDAO = new DocumentDAO();
                                docDAO.sendDocumentToStaffFromMission(objSource.getSourceId(), lstOrgId, null,
                                        userGroup.getItemEntityUser(), userGroup.getVof2_ItemEntityUser(),
                                        assignId, sysUserId, performId, orgAssignId);
                            }
                        }
                    }
                } catch (Exception ex) {
                    logger.error("sendDocToPersonInGroup: ", ex);
                }

                SmsDAO sms = new SmsDAO();
                StaffDAO staff = new StaffDAO();
                VHROrgDAO org = new VHROrgDAO();
                //Lay thong tin nguoi giao nhiem vu
                List<Long> listAssignTmp = new ArrayList<>();
                listAssignTmp.add(orgAssignId);
                EntityVhrEmployee userAssign;
                List<EntityVhrEmployee> lstVhrEmployee = staff.getLeaderByOrg(assignId, listAssignTmp);
                if (!CommonUtils.isEmpty(lstVhrEmployee)
                        && orgPerformId != null) {
                    userAssign = lstVhrEmployee.get(0);
                    //Lay thong tin don vi thuc hien
                    EntityVhrOrg orgPerform = org.getVHROrg(orgPerformId);
                    //Lay danh sach ID don vi phoi hop

                    List<Long> listCombinateOrgId = new ArrayList<>();
                    //Khoi tao danh sach tham so chen vao noi dung tin nhan (Cau hinh tin nhan CATEGORY = 2)
                    //List listObjectSendMess2Final gom 5 phan tu: Chuc vu nguoi giao, ten nguoi giao, ma don vi phoi hop, ma don vi thuc hien, ten nhiem vu
                    List<String> listObjectSendMess2 = new ArrayList<>();
                    List<String> listObjectSendMess2Tmp = new ArrayList<>();
                    List<String> listObjectSendMess2Final = new ArrayList<>();
                    listObjectSendMess2.add(userAssign.getPosition());
                    //050417 sua gui tin nhan lay fullName thay displayName
                    listObjectSendMess2.add(userAssign.getFullName());
                    for (EntityOrgCombinationMap obj : listAddOrgCombination) {
                        listCombinateOrgId.clear();
                        Long orgCombinateId = obj.getOrgCombinationId();
                        listCombinateOrgId.add(orgCombinateId);
                        //Lay danh sach thu truong, lanh dao, tro ly cua don vi phoi hop
                        List<EntityVhrEmployee> staffCombinateOrgs = staff.getLeaderAndAssistantByOrg(null, listCombinateOrgId);
                        EntityVhrOrg orgCombinate = org.getVHROrg(orgCombinateId);
                        listObjectSendMess2Tmp.clear();
                        listObjectSendMess2Final.clear();
                        listObjectSendMess2Tmp.add(orgCombinate.getAbbreviation());
                        listObjectSendMess2Tmp.add(orgPerform.getAbbreviation());
                        listObjectSendMess2Tmp.add(missionName);
                        listObjectSendMess2Final.addAll(listObjectSendMess2);
                        listObjectSendMess2Final.addAll(listObjectSendMess2Tmp);
                        if (CommonUtils.isEmpty(staffCombinateOrgs)) {
                            logger.error("Khong co ca nhan thoa man dieu kien gui tin nhan!");
                        } else {
                            for (EntityVhrEmployee person : staffCombinateOrgs) {
                                CommonControler common = new CommonControler();
                                String phone = person.getMobilePhone();
                                //Check dieu kien so dien thoai nguoi nhan khong nam trong Black List
                                if (phone != null) {//&& sms.isNotInBlackList(phone)
                                    //Chen cac thong tin vao mau noi dung tin nhan
                                    String contentMsg = common.getStrMessConfig(listObjectSendMess2Final, 4L, 2L, person.getEmployeeId());
                                    if (!sms.addMsgToSmsMaster(phone, contentMsg, sysUserId, sysUserId, person.getEmployeeId(), 5L, -1L,
                                            Constants.SMS_TEXT_INTERCEPT.LEADER_MESSGIVEMISSION)) {
                                        logger.error("Loi! Khong insert duoc tin nhan vao DataBase");
                                    }
                                }
                            }
                        }
                    }
                }
            }
            //End HaNH
            // datdc forward Mission neu co du lieu reason va isFromMission null
            // co thay doi orgPerformId
            boolean isUpdatePerFormId = false;
            EntityMission eMission = missionDAO.getMission(missionId);
            Long orgPerformIdOld = eMission.getOrgPerformId();
            if (null != orgPerformId) {
                if (!orgPerformIdOld.equals(orgPerformId)) {
                    isUpdatePerFormId = true;
                }
            }
            if (!CommonUtils.isEmpty(reason) && null == isFromMission && isUpdatePerFormId) {
                MeetingDAO dAO = new MeetingDAO();
                List<Long> listPerform = new ArrayList<Long>();
                listPerform.add(orgPerformId);
                int resultFw = dAO.forwardMission(missionId, listPerform, userGroup.getUserId2(),reason);
                if (resultFw == 1) {
                    fowardAfterUpdate(userGroup, listPerform, missionId, orgPerformIdOld, performId, reason);
                }
                return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
            }
            // datdc end
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            return FunctionCommon.returnResultAfterLog(userGroup, ex);
        }
    }
    
    /**
     * <b>Xoa nhiem vu</b>
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    public String deleteMission(HttpServletRequest request,
            String data, String isSecurity) {

        String[] keys = new String[]{
            ConstantsFieldParams.MISSION_ID
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.returnResultAfterLogSessionTimeout(userGroup);
        }
        // Lay danh sach don vi ma user la lanh dao hoac thu truong don vi hoac tro ly
        List<Long> listSysOrgId = new ArrayList<>();
        List<Long> listManagementOrgId = userGroup.getVof2_ItemEntityUser().getListManagementOrg();
        if (listManagementOrgId != null && !listManagementOrgId.isEmpty()) {
            listSysOrgId.addAll(listManagementOrgId);
        }
        List<Long> listAssistantOrgId = userGroup.getVof2_ItemEntityUser().getListAssistantOrg();
        if (listAssistantOrgId != null && !listAssistantOrgId.isEmpty()) {
            listSysOrgId.addAll(listAssistantOrgId);
        }
        if (CommonUtils.isEmpty(listSysOrgId)) {
            return FunctionCommon.returnResultAfterLogNotAllow(userGroup.getUserId2());
        }
        // Map don vi tro ly voi don vi tro ly chuyen huong
        HashMap<Long, List<Long>> hmSpecializedOrgId = userGroup.getVof2_ItemEntityUser()
                .getHmSpecializedOrgId();
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            Long missionId = Long.parseLong(listValue.get(0));

            MissionDAO missionDAO = new MissionDAO();
            EntityMission mission = missionDAO.checkUserPermissionForMission(
                    userGroup.getUserId2(), listManagementOrgId, listAssistantOrgId,
                    listSysOrgId, missionId, hmSpecializedOrgId);
            // Nguoi tao thi co quyen xoa nhiem vu
            EntityMission missionInfo = missionDAO.getMission(missionId);
            Boolean result;
            if (mission.getPermissionOfAssignOrg().isDelete()
                    || (missionInfo.getCreatedBy() != null
                    && missionInfo.getCreatedBy().equals(userGroup.getUserId2()))) {
                result = missionDAO.deleteMission(missionId, userGroup.getUserId2());
            } else {
                return FunctionCommon.returnResultAfterLogNotAllow( userGroup.getUserId2());
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            return FunctionCommon.returnResultAfterLog(userGroup, ex);
        }
    }

    /**
     * <b>Don vi thuc hien cap nhat tien do nhiem vu</b>
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    public String updateProcess(HttpServletRequest request,
            String data, String isSecurity) {
        
        String[] keys = new String[]{
            ConstantsFieldParams.MISSION_ID,
            ConstantsFieldParams.MISSION_PROCESS_STATUS,
            ConstantsFieldParams.MISSION_PROCESS_ACTION,
            ConstantsFieldParams.MISSION_PROCESS_DIFFICULT,
            ConstantsFieldParams.MISSION_PROCESS_PROPOSE,
            "missionProcessId",
            "newDeadline",
            "extendReason",
            "oldDeadline",
            "actualDate"
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.returnResultAfterLogSessionTimeout(userGroup);
        }
        try {
            // Lay danh sach don vi ma user la lanh dao hoac thu truong don vi hoac tro ly
            Long sysUserId = userGroup.getUserId2();
            List<Long> listSysOrgId = new ArrayList<>();
            List<Long> listManagementOrgId = userGroup.getVof2_ItemEntityUser().getListManagementOrg();
            if (!CommonUtils.isEmpty(listManagementOrgId)) {
                listSysOrgId.addAll(listManagementOrgId);
            }
            List<Long> listAssistantOrgId = userGroup.getVof2_ItemEntityUser().getListAssistantOrg();
            if (!CommonUtils.isEmpty(listAssistantOrgId)) {
                listSysOrgId.addAll(listAssistantOrgId);
            }
            List<EntityVhrOrg> listPoliticalAssistantOrg = userGroup
                    .getVof2_ItemEntityUser().getListPoliticalAssistantOrg();
            List<Long> listPoliticalAssistantOrgId = new ArrayList<>();
            if (!CommonUtils.isEmpty(listPoliticalAssistantOrg)) {
                for (EntityVhrOrg org : listPoliticalAssistantOrg) {
                    listPoliticalAssistantOrgId.add(org.getSysOrganizationId());
                }
            }
            HashMap<Long, List<Long>> hmSpecializedOrgId = userGroup.getVof2_ItemEntityUser()
                    .getHmSpecializedOrgId();
            List<String> listValue = userGroup.getListParamsFromClient();
            Long missionId = Long.parseLong(listValue.get(0));
            Long status = Long.parseLong(listValue.get(1));
            String action = listValue.get(2);
            String difficult = listValue.get(3);
            String propose = listValue.get(4);
            Long missionProcessId = null;
            try {
                missionProcessId = Long.parseLong(listValue.get(5));
            } catch (NumberFormatException e) {
            }
            //vanpn: them du lieu gia han
            String newDeadline = listValue.get(6);
            String extendReason = listValue.get(7);
            String oldDeadline = listValue.get(8);
            //vanpn -end
            // [tuantm30] Bo sung them tham so Ngay hoan thanh thuc te
            String actualDate = listValue.get(9);
            if (!CommonUtils.isEmpty(actualDate)) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, -4);
                Date currentDate = DateUtils.truncDate(cal.getTime());
                Date compareDate = DateUtils.string2Date(actualDate);
                if (compareDate.before(currentDate)) {
                    return FunctionCommon.responseResult(ErrorCode.SUCCESS, 2,
                            userGroup);
                }
            }

            Gson gson = new Gson();
            RequestUpdateProcess entityRequest = gson.fromJson(userGroup.getInputData(),
                    RequestUpdateProcess.class);
            List<EntityFileAttachment> listFileAttach = entityRequest.getListFileAttach();
            List<EntitySourceMap> listSourceMap = entityRequest.getListSourceMap();
            MissionDAO missionDAO = new MissionDAO();
            // Lay dau moi thuc hien nhiem vu
            EntityMission mission;
            mission = missionDAO.getMission(missionId);
            Long performId = mission.getPerformId();
            //kiem tra dau moi thuc hien
            if (performId != null && Objects.equals(sysUserId, performId)) {
                
                Long orgPerformId = mission.getOrgPerformId();
                //add perfromId vao list tro ly
                if (CommonUtils.isEmpty(listAssistantOrgId)) {
                    listAssistantOrgId = new ArrayList<>();
                }
                listAssistantOrgId.add(orgPerformId);
                int result = missionDAO.updateProcess(sysUserId, missionId,
                        status, action, difficult, propose, missionProcessId,
                        listFileAttach, listSourceMap, listManagementOrgId,
                        listAssistantOrgId, userGroup.getCardId(), orgPerformId,
//                        listPoliticalAssistantOrgId);
                        listPoliticalAssistantOrgId,newDeadline,extendReason,
                        oldDeadline, actualDate);
                //chuyen van ban toi nguoi giao viec va tro li
//                    if (result > 0) {
//                        sendDocUpdateProcess(mission.getAssignId(), mission.getOrgAssignId(),
//                                orgPerformId, listSourceMaps, entityUserGroup.getItemEntityUser(),
//                                entityUserGroup.getVof2_ItemEntityUser());
//                    }
                return FunctionCommon.responseResult(ErrorCode.SUCCESS, result,
                        userGroup);
            } else {
                
                List<Long> listAllAssistantOrgId = new ArrayList<>();
                List<Long> listAllSysOrgId = new ArrayList<>();
                listAllSysOrgId.addAll(listSysOrgId);
                if (!CommonUtils.isEmpty(listAssistantOrgId)) {
                    listAllAssistantOrgId.addAll(listAssistantOrgId);
                }
                if (!CommonUtils.isEmpty(listPoliticalAssistantOrgId)) {
                    listAllAssistantOrgId.addAll(listPoliticalAssistantOrgId);
                    listAllSysOrgId.addAll(listPoliticalAssistantOrgId);
                }
                mission = missionDAO.checkUserPermissionForMission(sysUserId,
                        listManagementOrgId, listAllAssistantOrgId, listAllSysOrgId,
                        missionId, hmSpecializedOrgId);
                if (!mission.getPermissionOfPerformOrg().isUpdate()) {                    
                    return FunctionCommon.returnResultAfterLogNotAllow(userGroup.getUserId2());
                }
                Long orgPerformId = mission.getOrgPerformId();
                int result = missionDAO.updateProcess(sysUserId, missionId,
                        status, action, difficult, propose, missionProcessId,
                        listFileAttach, listSourceMap, listManagementOrgId,
                        listAssistantOrgId, userGroup.getCardId(), orgPerformId,
                        listPoliticalAssistantOrgId, newDeadline, extendReason,
                        oldDeadline, actualDate);
                //chuyen van ban toi nguoi giao viec va tro li
//                    if (result > 0) {
//                        sendDocUpdateProcess(mission.getAssignId(), mission.getOrgAssignId(),
//                                orgPerformId, listSourceMaps, entityUserGroup.getItemEntityUser(),
//                                entityUserGroup.getVof2_ItemEntityUser());
//                    }
                return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
            }
        } catch (Exception ex) {
            return FunctionCommon.returnResultAfterLog(userGroup, ex);
        }
    }

    /**
     * <b>Don vi giao phe duyet hay tu choi tien do nhiem vu don vi</b>
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    public String approveOrRejectProcess(HttpServletRequest request,
            String data, String isSecurity) {

        String[] keys = new String[]{ConstantsFieldParams.MISSION_ID,
            ConstantsFieldParams.MISSION_PROCESS_IS_ORG_ASSIGN,
            ConstantsFieldParams.MISSION_PROCESS_IS_APPROVE,
            ConstantsFieldParams.MISSION_PROCESS_COMMENT};
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.returnResultAfterLogSessionTimeout(userGroup);
        }
        try {
            // Lay danh sach don vi ma user la lanh dao hoac thu truong don vi hoac tro ly
            Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
            List<Long> listSysOrgId = new ArrayList<>();
            List<Long> listManagementOrgId = user.getListManagementOrg();
            if (!CommonUtils.isEmpty(listManagementOrgId)) {
                listSysOrgId.addAll(listManagementOrgId);
            }
            List<Long> listAssistantOrgId = user.getListAssistantOrgIdForMission();
            if (!CommonUtils.isEmpty(listAssistantOrgId)) {
                listSysOrgId.addAll(listAssistantOrgId);
            }
            if (listSysOrgId.isEmpty()) {
                return FunctionCommon.returnResultAfterLogNotAllow(userGroup.getUserId2());
            }
            HashMap<Long, List<Long>> hmSpecializedOrgId =
                    userGroup.getVof2_ItemEntityUser().getHmSpecializedOrgId();
            List<String> listValue = userGroup.getListParamsFromClient();
            Long missionId = Long.parseLong(listValue.get(0));
            String strIsOrgAssign = listValue.get(1);
            Boolean isOrgAssign = false;
            if ("1".equals(strIsOrgAssign)) {
                isOrgAssign = true;
            }
            String strIsApprove = listValue.get(2);
            Boolean isApprove = false;
            if ("1".equals(strIsApprove)) {
                isApprove = true;
            }
            String comment = listValue.get(3);
            MissionDAO missionDAO = new MissionDAO();
            EntityMission mission = missionDAO.checkUserPermissionForMission(
                    userGroup.getUserId2(), listManagementOrgId, listAssistantOrgId,
                    listSysOrgId, missionId, hmSpecializedOrgId);
            if ((isOrgAssign && !mission.getPermissionOfAssignOrg().isGuide())
                    || (!isOrgAssign && !mission.getPermissionOfPerformOrg().isApprove())) {
                return FunctionCommon.returnResultAfterLogNotAllow(userGroup.getUserId2());
            }
            Long missionProcessId;
            Long missionProcessStatus;
            String missionProcessAction;
            String newDeadline = null;
            String dateComplete = null;
//            System.out.println("isOrgAssign: "+isOrgAssign + " - mission.getMissionProcessId2(): " + mission.getMissionProcessId2());
//            System.out.println("mission.getMissionProcessStatus2(): " + mission.getMissionProcessStatus2());
//            System.out.println(" mission.getMissionProcessAction2: "+ mission.getMissionProcessAction2());
//            System.out.println("mission.getNewDeadline(): " + mission.getNewDeadline());
//            System.out.println("mission.getDateComplete(): " + mission.getDateComplete());
            if (isOrgAssign) {
                missionProcessId = mission.getMissionProcessId2();
                missionProcessStatus = mission.getMissionProcessStatus2();
                missionProcessAction = mission.getMissionProcessAction2();
                if (missionProcessStatus == 7) {
                    newDeadline = mission.getNewDeadline();
                    dateComplete = mission.getDateComplete();
                }
            } else {
                missionProcessId = mission.getMissionProcessId();
                missionProcessStatus = mission.getMissionProcessStatus();
                missionProcessAction = mission.getMissionProcessAction();
            }
            Integer missionType = mission.getMissionType() == null ? null
                    : mission.getMissionType().intValue();
            int result = 0;
            if (missionProcessStatus == 7 && isOrgAssign && isApprove) {
                result = missionDAO.approveExtendProcess(userGroup.getUserId2(),
                    missionId, missionProcessId, missionProcessStatus,
                    missionProcessAction, isOrgAssign, isApprove, comment, missionType, 
                    newDeadline, dateComplete);
            } else {
                result = missionDAO.approveOrRejectProcess(userGroup.getUserId2(),
                    missionId, missionProcessId, missionProcessStatus,
                    missionProcessAction, isOrgAssign, isApprove, comment, missionType);
            }
            
            if (result == 1) {
                    if (isOrgAssign) {
                        SmsDAO sms = new SmsDAO();
                        StaffDAO staff = new StaffDAO();
                        UserDAO userDao = new UserDAO();
                        EntityMission missionDetail = missionDAO.getMissionDetail(
                                missionId, userGroup.getUserId2(), null, null, null, "vi", null, null);
                        //Lay ID nguoi giao, don vi giao
                        //Long assignId = missionDetail.getAssignId();
                        Long orgAssignId = missionDetail.getOrgAssignId();
                        //Lay thong tin nguoi giao nhiem vu
                        List<Long> listAssignTmp = new ArrayList<>();
                        listAssignTmp.add(orgAssignId);
                        EntityVhrEmployee userAssign;
                        List<EntityVhrEmployee> listAssigner = staff.getLeaderAndAssistantByOrg(
                                userGroup.getUserId2(), listAssignTmp);
                        if (!CommonUtils.isEmpty(listAssigner)) {
                            userAssign = listAssigner.get(0);
                            //Lay thong tin cua nguoi cap nhat tien do gan nhat da duoc don vi giao phe duyet hoac tu choi
                            EntityMissionProcess missionProcessLatest = missionDAO.getMissionProcessLatest(missionId);
                            Long createdId = missionProcessLatest.getCreatedBy();
                            EntityVhrEmployee createdUser = userDao.getEmployeeById(createdId);
                            //Khoi tao cac tham so de chen vao noi dung tin nhan
                            //List listObjectSendMess1Final gom 3 phan tu: Chuc vu nguoi giao, ten nguoi giao, ten nhiem vu
                            List<String> listObjectSendMess = new ArrayList<>();
                            listObjectSendMess.add(userAssign.getPosition());
                            listObjectSendMess.add(userAssign.getDisplayName());
                            listObjectSendMess.add(missionDetail.getMissionName());
                            //Neu trang thai tien do la "Dang thuc hien" thi gui tin nhan cho nguoi cap nhat
//                            if (missionProcessLatest.getStatus().equals(2) && createdUser != null) {
                            if (createdUser != null) {
                                CommonControler common = new CommonControler();
                                String phone = createdUser.getMobilePhone();
                                //Check dieu kien so dien thoai nguoi nhan khong nam trong Black List
                                if (phone != null && sms.isNotInBlackList(phone)) {
                                    //Chen cac thong tin vao mau noi dung tin nhan
                                    String content = null;
//                                    if (isApprove) {
//                                        content = common.getStrMessConfig(listObjectSendMess, 4L, 3L, createdUser.getEmployeeId());
//                                    } else {
//                                        content = common.getStrMessConfig(listObjectSendMess, 4L, 4L, createdUser.getEmployeeId());
//                                    }
                                    if (!isApprove) {
                                        content = common.getStrMessConfig(listObjectSendMess,
                                                4L, 4L, createdUser.getEmployeeId());
                                    }
                                    if (!sms.addMsgToSmsMaster(phone, content,
                                            userGroup.getUserId2(), userGroup.getUserId2(),
                                            createdUser.getEmployeeId(), 5L,
                                            Constants.SMS_TEXT_INTERCEPT.RECEIVED_UPDATEMISSION)) {
                                        logger.error("Loi! Khong insert duoc tin nhan vao DataBase");
                                    }
                                }
                            }
//                            //Lay danh sach thu truong, lanh dao tro ly cua don vi thuc hien
//                            List<Long> listPerformIdTmp = new ArrayList<>();
//                            listPerformIdTmp.add(missionDetail.getOrgPerformId());
//                            List<EntityVhrEmployee> staffPerformOrgs = staff.getLeaderAndAssistantByOrg(null, listPerformIdTmp);
//                            //Neu nhiem vu co dau moi thuc hien thi add them dau moi vao danh sach
//                            if (missionDetail.getPerformId() != null) {
//                                EntityVhrEmployee userPerform = userDao.getEmployeeById(missionDetail.getPerformId());
//                                staffPerformOrgs.add(userPerform);
//                            }
//                            if (CommonUtils.isEmpty(staffPerformOrgs)) {
//                                logger.error("Khong co ca nhan thoa man dieu kien gui tin nhan!");
//                            } else {
//                                //Neu tien do la "Da hoan thanh" hoac "Yeu cau dong" thi gui tin nhan
//                                //cho danh sach lanh dao, thu truong, tro ly, dau moi thuc hien cua don vi thuc hien
//                                if (missionProcessLatest.getStatus().equals(3) || missionProcessLatest.getStatus().equals(5)) {
//                                    for (EntityVhrEmployee person : staffPerformOrgs) {
//                                        CommonControler common = new CommonControler();
//                                        String phone = person.getMobilePhone();
//                                        //Check dieu kien so dien thoai nguoi nhan khong nam trong Black List
//                                        if (phone != null && sms.isNotInBlackList(phone)) {
//                                            //Chen cac thong tin vao mau noi dung tin nhan
//                                            String content = null;
//                                            if (isApprove) {
//                                                content = common.getStrMessConfig(listObjectSendMess, 4L, 3L, person.getEmployeeId());
//                                            } else {
//                                                content = common.getStrMessConfig(listObjectSendMess, 4L, 4L, person.getEmployeeId());
//                                            }
//                                            if (!sms.addMsgToSmsMaster(phone, content, sysUserId, sysUserId, person.getEmployeeId(), 5L, -1L)) {
//                                                logger.error("Loi! Khong insert duoc tin nhan vao DataBase");
//                                            }
//                                        }
//                                    }
//                                }
//                            }
                        }
                    }
                }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            return FunctionCommon.returnResultAfterLog(userGroup, ex);
        }
    }

    /**
     * <b>Cap nhat noi dung hoac ket qua cho don vi phoi hop</b>
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    public String updateContentOrResultOfCombinationOrg(HttpServletRequest request,
            String data, String isSecurity) {

        String[] keys = new String[]{
            ConstantsFieldParams.MISSION_ID,
            ConstantsFieldParams.MISSION_ORG_TYPE,
            ConstantsFieldParams.ORG_COMBINATION_ID,
            ConstantsFieldParams.ORG_COMBINATION_MAP_CONTENT,
            ConstantsFieldParams.ORG_COMBINATION_MAP_ACTION
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.returnResultAfterLogSessionTimeout(userGroup);
        }
        try {
            // Lay danh sach don vi ma user la lanh dao hoac thu truong don vi hoac tro ly
            Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
            Long userId = user.getUserId();
            List<Long> listSysOrgId = new ArrayList<>();
            List<Long> listManagementOrgId = user.getListManagementOrg();
            if (!CommonUtils.isEmpty(listManagementOrgId)) {
                listSysOrgId.addAll(listManagementOrgId);
            }
            List<Long> listAssistantOrgId = user.getListAssistantOrgIdForMission();
            if (!CommonUtils.isEmpty(listAssistantOrgId)) {
                listSysOrgId.addAll(listAssistantOrgId);
            }
            if (CommonUtils.isEmpty(listSysOrgId)) {
                return FunctionCommon.returnResultAfterLogNotAllow(userId);
            }
            HashMap<Long, List<Long>> hmSpecializedOrgId =
                    userGroup.getVof2_ItemEntityUser().getHmSpecializedOrgId();
            // Lay gia tri client truyen len
            List<String> listValue = userGroup.getListParamsFromClient();
            Long missionId = Long.parseLong(listValue.get(0));
            int orgType = Integer.parseInt(listValue.get(1));
            Long orgCombinationId = Long.parseLong(listValue.get(2));
            String content = listValue.get(3);
            String action = listValue.get(4);
            MissionDAO missionDAO = new MissionDAO();
            EntityMission mission = missionDAO.checkUserPermissionForMission(userId,
                    listManagementOrgId, listAssistantOrgId, listSysOrgId, missionId,
                    hmSpecializedOrgId);
            if (!mission.getPermissionOfCombinationOrg().isUpdate()) {
                return FunctionCommon.returnResultAfterLogNotAllow(userId);
            }
            int result = missionDAO.updateContentOrResultOfCombinationOrg(
                    userId, missionId, orgCombinationId, orgType, content, action);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            return FunctionCommon.returnResultAfterLog(userGroup, ex);
        }
    }

    /**
     * <b>Dem so luong nhiem vu</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getCountMission(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        if (userGroup.getCheckSessionOk()) {
            String cardId = "";
            try {
                // Kiem tra thong tin user
                if (!userGroup.checkUserId()) {
                    logger.error("getCountMission (Dem so luong nhiem vu) - user"
                            + " hoac userId tren he thong 1&2 null!");
                    return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW,
                            null, null);
                }
                // Kiem tra xem co ma hoa du lieu ko
                String strAesKeyDecode = null;
                if (isSecurity != null && "1".equals(isSecurity)) {
                    // Lay AES Key
                    strAesKeyDecode = userGroup.getStrAesKey();
                    // Giai ma data client gui len
                    data = SecurityControler.decodeDataByAes(strAesKeyDecode, data);
                }
                // Lay ma nhan vien
                cardId = userGroup.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(data);
                LogUtils.logFunctionalStart(log);
                // Parse du lieu gui len theo key
                JSONObject json = new JSONObject(data);
                String[] keys = new String[]{
                    ConstantsFieldParams.STR_ORG_PERFORM_ID,
                    ConstantsFieldParams.STR_ASSIGN_ID,
                    ConstantsFieldParams.STR_SUB_ASSIGN_ID,
                    ConstantsFieldParams.STR_ORG_SUB_ASSISTANT
                };
                List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                String orgPerformId = listValue.get(0);
                String assignId = listValue.get(1);
//                String userId = userGroup.getItemEntityUser().getUserId().toString();
                String userId = userGroup.getVof2_ItemEntityUser().getUserId().toString();
                String subAssignId = listValue.get(2);
                String orgSubAssistant = listValue.get(3);
                MissionDAO missionDAO = new MissionDAO();
                List<EntityMissionCount> lstEntityMissionCount = new ArrayList<>();
                // Lay so luong nhiem vu ban lanh dao thuc hien
                List<EntityMissionCount> lstEntityMissionCountOrgPerform = missionDAO.getCountMissionOrgPerForm(userId, orgPerformId, assignId, "3");
                lstEntityMissionCount.addAll(lstEntityMissionCountOrgPerform);

                // Lay so luong nhiem vu ban lanh dao don vi giao di
                List<EntityMissionCount> lstEntityMissionCountOrgAssign = missionDAO.getCountMissionOrgPerForm(userId, orgSubAssistant, subAssignId, "6");
                lstEntityMissionCount.addAll(lstEntityMissionCountOrgAssign);

                // Lay so luong nhiem vu theo phong ban - trang thai
                List<EntityMissionCount> lstEntityMissionCountByDept = missionDAO.getCountMissionByDept(userId, orgPerformId, orgSubAssistant);
                lstEntityMissionCount.addAll(lstEntityMissionCountByDept);

                // Lay so luong nhiem vu theo don vi dang ky
                List<EntityMissionCount> lstEntityMissionCountByRegister = missionDAO.getCountMissionByRegistered(userId, orgPerformId);
                lstEntityMissionCount.addAll(lstEntityMissionCountByRegister);

                // Lay so luong nhiem vu don vi phoi hop
                List<EntityMissionCount> lstEntityMissionCountByCordination = missionDAO.getCountMissionCordination(userId, orgPerformId);
                lstEntityMissionCount.addAll(lstEntityMissionCountByCordination);
                
                // hieuhk1 start - Lay so luong nhiem vu don vi toi duoc giao 
                List<EntityMissionCount> lstEntityMissionCountByMyJob = missionDAO.getCountMissionMyJob(userId);
                lstEntityMissionCount.addAll(lstEntityMissionCountByMyJob);
                // hieuhk1 end- Lay so luong nhiem vu don vi toi duoc giao
                
                // Lay so luong nhiem vu KH nam can phai thuc hien
                // List<EntityMissionCount> lstEntityYearPlanMissionCountOrgPerform = missionDAO.getCountMissionYearPlan(userId, orgPerformId, orgSubAssistant);
                // lstEntityMissionCount.addAll(lstEntityYearPlanMissionCountOrgPerform);
                // Lay so luong nhiem vu sap den han KH nam va nhiem vu don vi can phai thuc hien
                // type = 8: So luong nhiem vu don vi
                // type = 9: so luong nhiem vu ke hoach nam
                List<EntityMissionCount> lstEntityUpcomingDeadline = missionDAO.getCountMissionUpcomingDeadline(userId,
                        orgPerformId, orgSubAssistant);
                lstEntityMissionCount.addAll(lstEntityUpcomingDeadline);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                        lstEntityMissionCount, strAesKeyDecode);
            } // Loi Server
            catch (Exception ex) {
                logger.error("getCountMission (Dem so luong nhiem vu) - Exception"
                        + " - username: " + cardId + "\ndata: " + data, ex);
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
        } // Session timeout
        else {
            logger.error("getCountMission (Dem so luong nhiem vu) - Session timeout!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }
    }

    /**
     *
     *
     */
    public String getInputForCountMission(HttpServletRequest request,
            String data, String isSecurity) {
        EntityLog log = new EntityLog(request, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(request);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                // Lay session
                EntityUserGroup entityUserGroup = dataSessionGR;
                if (entityUserGroup.getItemEntityUser() == null && entityUserGroup.getVof2_ItemEntityUser() == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.SESSION_TIME_OUT, null, null);
                }

//                Long userId = entityUserGroup.getVof2_ItemEntityUser().getUserId();
                // Lay danh sach don vi ma user la lanh dao hoac thu truong don vi hoac tro ly
                List<Long> listSysOrgId = new ArrayList<Long>();
                // Lay danh sach don vi user co vai tro lanh dao/thu truong
                List<Long> listManagementOrgId = entityUserGroup.getVof2_ItemEntityUser().getListManagementOrg();
                if (listManagementOrgId != null && !listManagementOrgId.isEmpty()) {
                    listSysOrgId.addAll(listManagementOrgId);
                }
                // Lay danh sach don vi user co vai tro tro ly
                List<Long> listAssistantOrgId = entityUserGroup.getVof2_ItemEntityUser().getListAssistantOrg();
                if (listAssistantOrgId != null && !listAssistantOrgId.isEmpty()) {
                    listSysOrgId.addAll(listAssistantOrgId);
                }
                if (listSysOrgId.isEmpty()) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
                }
                // HashMap don vi user co vai tro tro ly va cac don vi chuyen huong tuong ung
                HashMap<Long, List<Long>> hmSpecializedOrgId = entityUserGroup.getVof2_ItemEntityUser().getHmSpecializedOrgId();
                // Kiem tra xem co ma hoa du lieu ko
                String strAesKeyDecode = null;
                if (isSecurity != null && "1".equals(isSecurity)) {
                    // Lay AES Key
                    strAesKeyDecode = entityUserGroup.getStrAesKey();
                    // Giai ma data client gui len
                    data = SecurityControler.decodeDataByAes(strAesKeyDecode, data);
                }
                // Lay ma nhan vien
                String cardId = entityUserGroup.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(data);
                LogUtils.logFunctionalStart(log);
//                JSONObject json = new JSONObject(data);

//                Long orgId = null;
                MissionDAO missionDAO = new MissionDAO();
                EntityMissionCount missionCount = new EntityMissionCount();
                // lay ds chi huy don vi giao theo don vi ma user lam LD/TT
                missionCount.setListEmployeeAssign(missionDAO.getListEmployeeAssign(listManagementOrgId));
                // lay ds chi huy don vi giao theo don vi ma user lam tro ly
                missionCount.setListEmployeeAssignAssistant(missionDAO.getListEmployeeAssign(listAssistantOrgId));
                //lay ds chi huy trong don vi ma user lam LD/TT
                missionCount.setListEmployeeInOrg(missionDAO.getListEmployeeInOrgs(listManagementOrgId));
                //lay danh sach chi huy trong don vi ma user lam Tro ly
                missionCount.setListEmployeeInOrgAssistant(missionDAO.getListEmployeeInOrgs(listAssistantOrgId));
                //lay thong tin don vi ma user lam LD/TT
                OrgDAO orgDao = new OrgDAO();
                List<EntityVhrOrg> listOrg = orgDao.getOrgById(listManagementOrgId);
                missionCount.setListManagementOrg(listOrg);
                //lay thong tin don vi ma user lam TL
                missionCount.setListAssistantOrg(orgDao.getOrgById(listAssistantOrgId));
                //Lay thong tin don vi ma user chuyen huong den
                List<Long> listSubAssistantOrgIds = new ArrayList<>();
                List<Long> listSubAssistantOrgId;
                if (hmSpecializedOrgId.size() > 0 && !hmSpecializedOrgId.isEmpty()) {
                    for (Long orgAss : listAssistantOrgId) {
                        if (hmSpecializedOrgId.containsKey(orgAss)) {
                            listSubAssistantOrgId = hmSpecializedOrgId.get(orgAss);
                            if (listSubAssistantOrgId.size() > 0 && !listSubAssistantOrgId.isEmpty()) {
                                for (Long long1 : listSubAssistantOrgId) {
                                    listSubAssistantOrgIds.add(long1);
                                }
                            }
                        }
                    }
                }
                missionCount.setListSubAssistantOrg(orgDao.getOrgById(listSubAssistantOrgIds));

                //System.out.println("getInputForCountMission-result: "+FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, missionCount, null));
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, missionCount, strAesKeyDecode);

            } catch (Exception ex) {
                logger.error("getMissionCommanderList - Exception:", ex);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return strResult;
    }
    //web 2.0

    /**
     * Tim kiem nhiem vu
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getMissionCommanderList(HttpServletRequest request, String data,
            String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(request);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                // Lay session
                EntityUserGroup entityUserGroup = dataSessionGR;
                if (entityUserGroup.getItemEntityUser() == null && entityUserGroup.getVof2_ItemEntityUser() == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.SESSION_TIME_OUT, null, null);
                }

                Long userId = entityUserGroup.getVof2_ItemEntityUser().getUserId();

                // Lay danh sach don vi ma user la lanh dao hoac thu truong don vi hoac tro ly
                List<Long> listSysOrgId = new ArrayList<Long>();
                // Lay danh sach don vi user co vai tro lanh dao/thu truong
                List<Long> listManagementOrgId = entityUserGroup.getVof2_ItemEntityUser().getListManagementOrg();
                if (listManagementOrgId != null && !listManagementOrgId.isEmpty()) {
                    listSysOrgId.addAll(listManagementOrgId);
                }
                // Lay danh sach don vi user co vai tro tro ly
                List<Long> listAssistantOrgId = entityUserGroup.getVof2_ItemEntityUser().getListAssistantOrg();
                if (listAssistantOrgId != null && !listAssistantOrgId.isEmpty()) {
                    listSysOrgId.addAll(listAssistantOrgId);
                }
                if (listSysOrgId.isEmpty()) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
                }
                // HashMap don vi user co vai tro tro ly va cac don vi chuyen huong tuong ung
                HashMap<Long, List<Long>> hmSpecializedOrgId = entityUserGroup.getVof2_ItemEntityUser().getHmSpecializedOrgId();
                // Kiem tra xem co ma hoa du lieu ko
                String strAesKeyDecode = null;
                if (isSecurity != null && "1".equals(isSecurity)) {
                    // Lay AES Key
                    strAesKeyDecode = entityUserGroup.getStrAesKey();
                    // Giai ma data client gui len
                    data = SecurityControler.decodeDataByAes(strAesKeyDecode, data);
                }
                // Lay ma nhan vien
                String cardId = entityUserGroup.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(data);
                LogUtils.logFunctionalStart(log);
                JSONObject json = new JSONObject(data);
                String[] keys = new String[]{
                    ConstantsFieldParams.IS_COUNT,
                    ConstantsFieldParams.MISSION_KEYWORD,
                    ConstantsFieldParams.MISSION_NAME,
                    ConstantsFieldParams.MISSION_CONTENT,
                    ConstantsFieldParams.MISSION_TARGET,
                    ConstantsFieldParams.MISSION_ORG_PERFORM_ID,
                    ConstantsFieldParams.MISSION_STATUS,
                    ConstantsFieldParams.MISSION_APPROVED_STATUS,
                    ConstantsFieldParams.START_RECORD,
                    ConstantsFieldParams.PAGE_SIZE,
                    ConstantsFieldParams.ORG_ID
                };
                List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);

                // Dem so luong hay khong
                String isCount = listValue.get(0);
                // Tu khoa
                String keyword = listValue.get(1);
                // Loai
                Integer type = Integer.parseInt(listValue.get(2));
                // Trang thai
                String strStatus = listValue.get(3);
                Long status = null;
                if (!"".equals(strStatus)) {
                    status = Long.parseLong(strStatus);
                }
                // Ngay bat dau
                String startDate = listValue.get(4);
                // Ngay ket thuc
                String completeDate = listValue.get(5);
                // Vi tri ban ghi lay ra
                String strStartRecord = listValue.get(6);
                Long startRecord = 0L;
                if (!"".equals(strStartRecord)) {
                    startRecord = Long.parseLong(strStartRecord);
                }
                // So ban ghi lay ra
                String strPageSize = listValue.get(7);
                Long pageSize = 10L;
                if (!"".equals(strPageSize)) {
                    pageSize = Long.parseLong(strPageSize);
                }

                Long orgId = null;

                MissionDAO missionDAO = new MissionDAO();
                Object result = missionDAO.getListMission(isCount, type,
                        userId, listSysOrgId, hmSpecializedOrgId, orgId, keyword,
                        status, startDate, completeDate, startRecord, pageSize);

                // Loi server
                if (result == null) {
                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                } else {
                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                }
            } catch (JSONException ex) {
                logger.error("getMissionCommanderList - Exception:", ex);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return strResult;
    }

    /**
     * Tim kiem nhiem vu phe duyet cua chi huy
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListMissionApproved(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(request);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                // Lay session
                EntityUserGroup entityUserGroup = dataSessionGR;
                if (entityUserGroup.getItemEntityUser() == null && entityUserGroup.getVof2_ItemEntityUser() == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.SESSION_TIME_OUT, null, null);
                }

                // Kiem tra xem co ma hoa du lieu ko
                String strAesKeyDecode = null;
                if (isSecurity != null && "1".equals(isSecurity)) {
                    // Lay AES Key
                    strAesKeyDecode = entityUserGroup.getStrAesKey();
                    // Giai ma data client gui len
                    data = SecurityControler.decodeDataByAes(strAesKeyDecode, data);
                }
                // Lay ma nhan vien
                String cardId = entityUserGroup.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(data);
                LogUtils.logFunctionalStart(log);
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                EntityMissionApproved ema = gson.fromJson(data, EntityMissionApproved.class);

                MissionDAO missionDAO = new MissionDAO();
                Object result = missionDAO.findDefaultMissionByConditionService(ema.getIsCount(), ema.getKeyWord(), ema.getOrgAssignId(), ema.getOrgPerformId(), ema.getContent(),
                        ema.getMissionName(), ema.getTarget(), ema.getApproved(), ema.getStatus(), ema.getFirstResult(), ema.getMaxResult(), ema.getUserId(), ema.getOrgId(), ema.getOrgIdlistOrgAssistant(), ema.getMissionId());

                // Loi server
                if (result == null) {
                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                } else {
                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                }
            } catch (Exception ex) {
                logger.error("getListMissionApproved - Exception:", ex);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return strResult;
    }

    /**
     * Phe duyet cua chi huy
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String approveMissionByCommander(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(request);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                // Lay session
                EntityUserGroup entityUserGroup = dataSessionGR;
                if (entityUserGroup.getItemEntityUser() == null && entityUserGroup.getVof2_ItemEntityUser() == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.SESSION_TIME_OUT, null, null);
                }

                // Kiem tra xem co ma hoa du lieu ko
                String strAesKeyDecode = null;
                if (isSecurity != null && "1".equals(isSecurity)) {
                    // Lay AES Key
                    strAesKeyDecode = entityUserGroup.getStrAesKey();
                    // Giai ma data client gui len
                    data = SecurityControler.decodeDataByAes(strAesKeyDecode, data);
                }
                // Lay ma nhan vien
                String cardId = entityUserGroup.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(data);
                LogUtils.logFunctionalStart(log);

                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                EntityMissionApproved ema = gson.fromJson(data, EntityMissionApproved.class);

                MissionDAO missionDAO = new MissionDAO();
                Object result = missionDAO.approveMissionByCommander(ema.getFlagStatus(), ema.getMissionId(), ema.getDateCompleteNew(), ema.getDateCompleteOld(), ema.getSysUserId(), ema.getCommentOrgAssign());
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                // Loi server
                if (result == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                } else {
                    return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                }
            } catch (Exception ex) {
                logger.error("approveMissionByCommander - Exception:", ex);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }
        return strResult;
    }
    //

    /**
     * Phe duyet cua chi huy
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String rejectMissionByCommander(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(request);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                // Lay session
                EntityUserGroup entityUserGroup = dataSessionGR;
                if (entityUserGroup.getItemEntityUser() == null && entityUserGroup.getVof2_ItemEntityUser() == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.SESSION_TIME_OUT, null, null);
                }

                // Kiem tra xem co ma hoa du lieu ko
                String strAesKeyDecode = null;
                if (isSecurity != null && "1".equals(isSecurity)) {
                    // Lay AES Key
                    strAesKeyDecode = entityUserGroup.getStrAesKey();
                    // Giai ma data client gui len
                    data = SecurityControler.decodeDataByAes(strAesKeyDecode, data);
                }
                // Lay ma nhan vien
                String cardId = entityUserGroup.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(data);
                LogUtils.logFunctionalStart(log);

                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                EntityMissionApproved ema = gson.fromJson(data, EntityMissionApproved.class);

                MissionDAO missionDAO = new MissionDAO();
                Object result = missionDAO.rejectMissionByCommander(ema.getFlagStatus(), ema.getMissionId(), ema.getSysUserId(), ema.getCommentOrgAssign());
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                // Loi server
                if (result == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                } else {
                    return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                }
            } catch (Exception ex) {
                logger.error("rejectMissionByCommander - Exception:", ex);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }
        return strResult;
    }

    /**
     * Phe duyet cua chi huy
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListOrg(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(request);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                // Lay session
                EntityUserGroup entityUserGroup = dataSessionGR;
                if (entityUserGroup.getItemEntityUser() == null && entityUserGroup.getVof2_ItemEntityUser() == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.SESSION_TIME_OUT, null, null);
                }
                // Kiem tra xem co ma hoa du lieu ko
                String strAesKeyDecode = null;
                if (isSecurity != null && "1".equals(isSecurity)) {
                    // Lay AES Key
                    strAesKeyDecode = entityUserGroup.getStrAesKey();
                    // Giai ma data client gui len
                    data = SecurityControler.decodeDataByAes(strAesKeyDecode, data);
                }
                // Lay ma nhan vien
                String cardId = entityUserGroup.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(data);
                LogUtils.logFunctionalStart(log);

//                GsonBuilder builder = new GsonBuilder();
//                Gson gson = builder.create();
//                EntityMissionApproved ema = gson.fromJson(data, EntityMissionApproved.class);
//                MissionDAO missionDAO = new MissionDAO();
                Object result = true;
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);

                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);

            } catch (Exception ex) {
                logger.error("getListOrg - Exception:", ex);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }
        return strResult;
    }

    /**
     * @author thienng1 Tim kiem nhiem vu
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String findMissionResovleIssueList(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(request);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                // Lay session
                EntityUserGroup entityUserGroup = dataSessionGR;
                if (entityUserGroup.getItemEntityUser() == null && entityUserGroup.getVof2_ItemEntityUser() == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.SESSION_TIME_OUT, null, null);
                }

                // Lay danh sach don vi ma user la lanh dao hoac thu truong don vi hoac tro ly
                List<Long> listSysOrgId = new ArrayList<Long>();
                // Lay danh sach don vi user co vai tro lanh dao/thu truong
                List<Long> listManagementOrgId = entityUserGroup.getVof2_ItemEntityUser().getListManagementOrg();
                if (listManagementOrgId != null && !listManagementOrgId.isEmpty()) {
                    listSysOrgId.addAll(listManagementOrgId);
                }
                // Lay danh sach don vi user co vai tro tro ly
                List<Long> listAssistantOrgId = entityUserGroup.getVof2_ItemEntityUser().getListAssistantOrg();
                if (listAssistantOrgId != null && !listAssistantOrgId.isEmpty()) {
                    listSysOrgId.addAll(listAssistantOrgId);
                }
                if (listSysOrgId.isEmpty()) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
                }
                // HashMap don vi user co vai tro tro ly va cac don vi chuyen huong tuong ung
//                HashMap<Long, List<Long>> hmSpecializedOrgId = entityUserGroup.getVof2_ItemEntityUser().getHmSpecializedOrgId();
                // Kiem tra xem co ma hoa du lieu ko
                String strAesKeyDecode = null;
                if (isSecurity != null && "1".equals(isSecurity)) {
                    // Lay AES Key
                    strAesKeyDecode = entityUserGroup.getStrAesKey();
                    // Giai ma data client gui len
                    data = SecurityControler.decodeDataByAes(strAesKeyDecode, data);
                }
                // Lay ma nhan vien
                String cardId = entityUserGroup.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(data);
                LogUtils.logFunctionalStart(log);

//                JSONObject json = new JSONObject(data);
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                EntityMissionApproved ema = gson.fromJson(data, EntityMissionApproved.class);

                // Dem so luong hay khong
                MissionDAO missionDAO = new MissionDAO();
                Object result = missionDAO.findMissionResovleIssueList(ema);
                //fix san code
                // Loi server
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                if (result == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                } else {
                    return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                }
            } catch (Exception ex) {
                logger.error("findMissionResovleIssueList - Exception:", ex);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }
        return strResult;
    }

    /**
     * Tamcd
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String doUpdatePercentService(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(request);

        if (!dataSessionGR.getCheckSessionOk()) {
            strResult = FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        } else {
            try {
                // Session time out
                EntityUserGroup entityUserGroup = dataSessionGR;
                if (entityUserGroup.getItemEntityUser() == null && entityUserGroup.getVof2_ItemEntityUser() == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.SESSION_TIME_OUT, null, null);
                }
                // Kiem tra xem co ma hoa du lieu ko
                String strAesKeyDecode = null;
                if (isSecurity != null && "1".equals(isSecurity)) {
                    // Lay AES Key
                    strAesKeyDecode = entityUserGroup.getStrAesKey();
                    // Giai ma data client gui len
                    data = SecurityControler.decodeDataByAes(strAesKeyDecode, data);
                }
                // Lay ma nhan vien
                String cardId = entityUserGroup.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(data);
                LogUtils.logFunctionalStart(log);

                JSONObject json = new JSONObject(data);
                String[] keys = new String[]{
                    ConstantsFieldParams.MISSION_PROCESS_ID,
                    ConstantsFieldParams.SOURCE_MISSION_ID,
                    ConstantsFieldParams.CONTENT,
                    ConstantsFieldParams.REFERENCE_ID,
                    ConstantsFieldParams.REFERENCE_TYPE,
                    ConstantsFieldParams.CREATED_DATE,
                    ConstantsFieldParams.CREATED_BY};
                List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                Long missionProcessId = Long.parseLong(listValue.get(0));
                Long sourceMissionId = Long.parseLong(listValue.get(1));
                String content = listValue.get(2);
                Long referenceId = "".equals(listValue.get(3)) ? null : Long.parseLong(listValue.get(3));
                Long referenceType = "".equals(listValue.get(4)) ? null : Long.parseLong(listValue.get(4));
                String createdDate = listValue.get(5);
                Long createdBy = Long.parseLong(listValue.get(6));
                MissionDAO missionDAO = new MissionDAO();
                Boolean result = missionDAO.addContentProposeDifficult(missionProcessId, sourceMissionId, content, referenceId, referenceType, createdDate, createdBy);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                        result, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (JSONException | NumberFormatException ex) {
                logger.error(ex.getMessage(), ex);
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
        }
        return strResult;
    }

    /**
     * <b>Dem so luong nhiem vu de hien thi tren menu</b>
     *
     * @author thanght6
     * @since 2016-01-08
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String countMission(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup entityUserGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!entityUserGroup.getCheckSessionOk()) {
            logger.error("countMission - No session");
            return FunctionCommon.generateResponseJSON(entityUserGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = entityUserGroup.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            logger.error("countMission - user id tren he thong 2 null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }

        // Lay danh sach don vi ma user la lanh dao hoac thu truong don vi hoac tro ly
        List<Long> listOrgId = new ArrayList<Long>();
        // Lay danh sach don vi user co vai tro thu truong/lanh dao
        List<Long> listManagementOrgId = entityUserGroup.getVof2_ItemEntityUser().getListManagementOrg();
        if (!CommonUtils.isEmpty(listManagementOrgId)) {
            listOrgId.addAll(listManagementOrgId);
        }
        // Lay danh sach don vi user co vai tro tro ly
        List<Long> listAssistantOrgId = entityUserGroup.getVof2_ItemEntityUser().getListAssistantOrg();
        if (!CommonUtils.isEmpty(listAssistantOrgId)) {
            listOrgId.addAll(listAssistantOrgId);
        }
        // Neu user khong co vai tro thu truong/lanh dao/tro ly
        // -> Tra ve khong co quyen
        if (CommonUtils.isEmpty(listOrgId)) {
            logger.error("countMission - Khong co vai tro thu truong/lanh dao/tro ly");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // HashMap don vi user co vai tro tro ly va cac don vi chuyen huong tuong ung
        HashMap<Long, List<Long>> hmSpecializedOrgId = entityUserGroup.getVof2_ItemEntityUser().getHmSpecializedOrgId();
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = entityUserGroup.getStrAesKey();
        }
        // Lay ma nhan vien
        String cardId = entityUserGroup.getCardId();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);

        MissionDAO missionDAO = new MissionDAO();
        Long result = missionDAO.countMission(userId, listOrgId, hmSpecializedOrgId);
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
    }

    /**
     * <b>Lay danh sach don vi, loc nhiem vu</b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListMissionGroup(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(request);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                // Lay session
                EntityUserGroup entityUserGroup = dataSessionGR;
                if (entityUserGroup.getItemEntityUser() == null && entityUserGroup.getVof2_ItemEntityUser() == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.SESSION_TIME_OUT, null, null);
                }
                String strAesKeyDecode = entityUserGroup.getStrAesKey();
                String decryptedData = SecurityControler.decodeDataByAes(strAesKeyDecode, data);
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(decryptedData);
                LogUtils.logFunctionalStart(log);

                //Them đơn vị tất cả chuyên hướng vào list
                List<Long> lstSpecialized = new ArrayList<>();
                HashMap<Long, List<Long>> hmSpecialized = dataSessionGR.getVof2_ItemEntityUser().getHmSpecializedOrgId();
                Iterator<Long> keySetIterator = hmSpecialized.keySet().iterator();

                while (keySetIterator.hasNext()) {
                    Long key = keySetIterator.next();
                    lstSpecialized.addAll(hmSpecialized.get(key));
                }

                HashMap<String, Object> hmParams = new HashMap<String, Object>();
                hmParams.put(ConstantsFieldParams.MISSION_TYPE, Long.class);

                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, data, dataSessionGR);

                Long type = (Long) ((valueParams.get(ConstantsFieldParams.MISSION_TYPE) != null) ? valueParams.get(ConstantsFieldParams.MISSION_TYPE) : 1L);
                Long userId = entityUserGroup.getVof2_ItemEntityUser().getUserId();

                MissionDAO missionDAO = new MissionDAO();
                List<EntityVhrOrg> result = missionDAO.getListMissionGroup(userId, type.intValue(), lstSpecialized);

                // Lay don vi duoc cau hinh giao viec
                MissionDAO missionD = new MissionDAO();
                List<Long> listConfigUserId = new ArrayList<Long>();
                List<Long> listUser = new ArrayList<Long>();
                listUser.add(userId);
                // Lay danh sach lanh dao cua don vi
                List<Long> listAssistantOrg = dataSessionGR.getVof2_ItemEntityUser().getListAssistantOrg();
                if (!CommonUtils.isEmpty(listAssistantOrg)) {
                   // Properties props = Utils.readProperties("com/viettel/voffice/database/config/config.properties");
                    String roleLeader =CommonUtils.getAppConfigValue("roleCodeLeader");
                    UserRoleDAO urDao = new UserRoleDAO();
                    for (Long obj : listAssistantOrg) {
                        List<EntityUserRole> listUserRole = urDao.getUserByOrgId(obj.toString(), roleLeader);
                        if (!CommonUtils.isEmpty(listUserRole)) {
                            for (EntityUserRole ur : listUserRole) {
                                listUser.add(ur.getSysUserId());
                            }
                        }
                    }
                }
                List<EntityConfigUser> listConfigUser = (List<EntityConfigUser>) missionD.getConfigMissionByListUser(listUser, 1);
                if (listConfigUser != null && !listConfigUser.isEmpty()) {
                    for (EntityConfigUser obj : listConfigUser) {
                        listConfigUserId.add(obj.getSysOrgId());
                    }

                    List<EntityVhrOrg> orgConfig = missionD.getOrgConfig(listConfigUserId);
                    if (!CommonUtils.isEmpty(orgConfig)) {
                        if (!CommonUtils.isEmpty(result)) {
                            for (EntityVhrOrg obj : orgConfig) {
                                if (!result.contains(obj)) {
                                    result.add(obj);
                                }
                            }
                        } else {
                            result.addAll(orgConfig);
                        }
                    }
                }
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                // Loi server
                if (result == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                } else {
                    return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                }
            } catch (Exception ex) {
                logger.error("Exception:", ex);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }
        return strResult;
    }

    /**
     * lay ra danh sach lịch sử tác động
     *
     * @param userId
     * @return
     */
    public String getListMissionLog(String isSecurity, String data,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //lay key Aes
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, data);
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);

                HashMap<String, Object> hmParams = new HashMap<String, Object>();
                hmParams.put(ConstantsFieldParams.MISSION_ID, Long.class);
                hmParams.put(ConstantsFieldParams.LANGUAGE, String.class);

                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, data, dataSessionGR);

                Long missionId = (Long) ((valueParams.get(ConstantsFieldParams.MISSION_ID) != null) ? valueParams.get(ConstantsFieldParams.MISSION_ID) : null);
                String language = (String) ((valueParams.get(ConstantsFieldParams.LANGUAGE) != null) ? valueParams.get(ConstantsFieldParams.LANGUAGE) : "");

                if (language == null || language.length() <= 0) {
                    language = dataSessionGR.getVof2_ItemEntityUser().getUserLanguage();
                }

                //System.out.println("data:" + language + ", mission: " + missionId);
                //kiem tra du lieu dau vao
                if (missionId == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                }

                MissionDAO missionDao = new MissionDAO();

                List<EntityMissionLog> listItem = missionDao.getListMissionLog(missionId, language);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, listItem, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                //System.out.println("Error: " + e.toString());
                logger.error(e.getMessage(), e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }

        return strResult;
    }

    /**
     * <b>Count nhiem vu chuyen quan</b>
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    public String getCountByOrgPerformSpecialized(HttpServletRequest request,
            String data, String isSecurity) {

        String[] keys = new String[]{
            ConstantsFieldParams.STATUS,
            ConstantsFieldParams.TYPE,
            ConstantsFieldParams.CMT_OBJECT_TYPE,
            ConstantsFieldParams.MISSION_START_DATE,
            ConstantsFieldParams.MISSION_COMPLETE_DATE,
            ConstantsFieldParams.IS_VERSION_NEW
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.returnResultAfterLogSessionTimeout(userGroup);
        }
        // Kiem tra user id tren he thong 2
        Long userId = userGroup.getVof2_ItemEntityUser().getUserId();
        if (!userGroup.checkUserId2()) {
            return FunctionCommon.returnResultAfterLogNoInfo(userGroup.getUserId2());
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            // Trang thai
            String strStatus = listValue.get(0);
            Integer status = null;
            if (strStatus != null && !strStatus.isEmpty()) {
                status = Integer.valueOf(strStatus);
            }
            // Loai:
            // 1: Giao việc
            // 2: Trợ lý chuyên hướng
            // 3: Chuyên quản
            Integer type = 3;
            String strType = listValue.get(1);
            if (!CommonUtils.isEmpty(strType)) {
                type = Integer.parseInt(strType);
            }
            Integer objectType = null;
            String strObjectType = listValue.get(2);
            if (!CommonUtils.isEmpty(strObjectType)) {
                objectType = Integer.parseInt(strObjectType);
            }
            // Ngay bat dau tu
            String startDateFrom = listValue.get(3);
            // Ngay bat dau den
            String startDateTo = listValue.get(4);
            String isVersionNew = listValue.get(5);
            boolean isNew = false;
            if (isVersionNew != null && !isVersionNew.isEmpty()) {
                isNew = true;
            }
            // Lay id LD,TT,TL
            List<Long> listOrgId = new ArrayList<>();
            List<Long> listAssistantOrgId = userGroup.getVof2_ItemEntityUser().getListAssistantOrg();
            List<Long> listManagementOrgId = userGroup.getVof2_ItemEntityUser().getListManagementOrg();
            if (!CommonUtils.isEmpty(listManagementOrgId)) {
                listOrgId.addAll(listManagementOrgId);
            }
            if (!CommonUtils.isEmpty(listAssistantOrgId)) {
                for (Long obj : listAssistantOrgId) {
                    if (!listOrgId.contains(obj)) {
                        listOrgId.add(obj);
                    }
                }
            }
            MissionDAO missionDAO = new MissionDAO();
            Object result = missionDAO.getCountByOrgPerformSpecialized(userId,
                    status, type, objectType, startDateFrom, startDateTo, listOrgId, isNew);
            if (result == null) {
                return FunctionCommon.returnResultAfterLogResultNull(
                        FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null),
                        userGroup.getUserId2());
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            return FunctionCommon.returnResultAfterLog(userGroup, ex);
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Lấy danh sách đơn vị chuyên quản">
    public String getListOrgSpecialized(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup entityUserGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!entityUserGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(entityUserGroup.getEnumErrCode(), null, null);
        }

        // Kiem tra user id tren he thong 2
        Long userId = entityUserGroup.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }

        // Lay danh sach don vi ma user la lanh dao hoac thu truong don vi 
        List<Long> listSysOrgId = new ArrayList<Long>();
        // Lay danh sach don vi user co vai tro lanh dao/thu truong
        List<Long> listManagementOrgId = entityUserGroup.getVof2_ItemEntityUser().getListManagementOrg();
        if (listManagementOrgId != null && !listManagementOrgId.isEmpty()) {
            listSysOrgId.addAll(listManagementOrgId);
        }
        if (listSysOrgId.isEmpty()) {
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = entityUserGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        // Lay ma nhan vien
        String cardId = entityUserGroup.getCardId();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.TYPE};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Integer tempType;
            if (listValue.get(0) != null && listValue.get(0).trim().length() > 0) {
                tempType = Integer.parseInt(listValue.get(0));
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
            MissionDAO cmtDAO = new MissionDAO();
            Object result = cmtDAO.getListOrgSpecialized(userId, tempType);
            if (result == null) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            }
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException ex) {
            logger.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Lấy danh sách cấu hình">
    public String getConfigMissionByUser(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup entityUserGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!entityUserGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(entityUserGroup.getEnumErrCode(), null, null);
        }

        // Kiem tra user id tren he thong 2
        Long userId = entityUserGroup.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }

        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = entityUserGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        // Lay ma nhan vien
        String cardId = entityUserGroup.getCardId();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);

        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.TYPE};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Integer tempType = 0;
            if (listValue.get(0) != null && listValue.get(0).trim().length() > 0) {
                tempType = Integer.parseInt(listValue.get(0));
            }
            MissionDAO cmtDAO = new MissionDAO();
            Object result = cmtDAO.getConfigMissionByUser(userId, tempType);

            if (result == null) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException ex) {
            logger.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Them moi cấu hình">
    public String configSpecializedManagement(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup entityUserGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!entityUserGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(entityUserGroup.getEnumErrCode(), null, null);
        }

        // Kiem tra user id tren he thong 2
        Long userId = entityUserGroup.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }

//        List<Long> listSysOrgId = new ArrayList<Long>();
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = entityUserGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        // Lay ma nhan vien
        String cardId = entityUserGroup.getCardId();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);

        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.TYPE,
                ConstantsFieldParams.LIST_ORG_ID};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);

            Integer tempType;
            if (listValue.get(1) != null && listValue.get(1).trim().length() > 0) {
                tempType = Integer.parseInt(listValue.get(1));
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
//            Object listTemp = null;
            if (listValue.get(1) != null && listValue.get(1).trim().length() > 0) {
//                listTemp = listValue.get(1);
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
            if (tempType == 3) {
                // Lay danh sach don vi user co vai tro lanh dao/thu truong
                List<Long> listManagementOrgId = entityUserGroup.getVof2_ItemEntityUser().getListManagementOrg();
                if (CommonUtils.isEmpty(listManagementOrgId)) {// Nếu null thì không có quyền cấu hình chuyên quản
                    return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
                }
            } else {
                if (tempType == 2) {
                    List<Long> listAssistantOrgId = entityUserGroup.getVof2_ItemEntityUser().getListAssistantOrg();
                    if (CommonUtils.isEmpty(listAssistantOrgId)) {// nếu không fai trợ lý thì ko đc cấu hình trợ lý
                        return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
                    }
                }
            }
            List<Long> listOrgSync = null;
            if (!json.isNull(ConstantsFieldParams.LIST_ORG_ID)) {
                JSONArray jsonArray = json.getJSONArray(ConstantsFieldParams.LIST_ORG_ID);
                if (jsonArray != null && jsonArray.length() > 0) {
                    listOrgSync = new ArrayList<Long>();
                    Long jsonObject;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = jsonArray.getLong(i);
                        listOrgSync.add(jsonObject);
                    }
                }
            }
            MissionDAO tempDAO = new MissionDAO();
            Object result = tempDAO.configSpecializedManagement(userId, listOrgSync, tempType);
            if (result == null) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            }
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException ex) {
            logger.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }
    //</editor-fold>

    /**
     * <b>Dem so luong nhiem vu chuyen quan</b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getCountMissionSpecialized(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup entityUserGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!entityUserGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(entityUserGroup.getEnumErrCode(), null, null);
        }

        // Kiem tra user id tren he thong 2
        Long userId = entityUserGroup.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }

        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = entityUserGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        // Lay ma nhan vien
        String cardId = entityUserGroup.getCardId();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);

        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.TYPE,
                ConstantsFieldParams.CMT_OBJECT_TYPE};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Integer tempType = 3;
            if (listValue.get(0) != null && listValue.get(0).trim().length() > 0) {
                tempType = Integer.parseInt(listValue.get(0));
            }
            Integer objType = 1;
            if (listValue.get(1) != null && listValue.get(1).trim().length() > 0) {
                objType = Integer.parseInt(listValue.get(1));
            }
            MissionDAO cmtDAO = new MissionDAO();
            Object result = cmtDAO.getCountMissionSpecialized(userId, tempType, objType);

            if (result == null) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            }
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException ex) {
            logger.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * <b>Lay nhiem vu sap toi han</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListMissionUpcomingDeadline(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup entityUserGroup = FunctionCommon.getStatusSession(request);
        if (!entityUserGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(entityUserGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = entityUserGroup.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = entityUserGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        // Lay ma nhan vien
        String cardId = entityUserGroup.getCardId();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.IS_COUNT,
                ConstantsFieldParams.TYPE,
                ConstantsFieldParams.START_RECORD,
                ConstantsFieldParams.PAGE_SIZE,
                ConstantsFieldParams.MISSION_START_DATE,
                ConstantsFieldParams.MISSION_COMPLETE_DATE,
                ConstantsFieldParams.KEYWORD
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String isCount = "";
            if (listValue.get(0) != null && listValue.get(0).trim().length() > 0) {
                isCount = listValue.get(0);
            }
            String objType = "";
            if (listValue.get(1) != null && listValue.get(1).trim().length() > 0) {
                objType = listValue.get(1);
            }
            Long startRecord = 0L;
            if (listValue.get(2) != null && listValue.get(2).trim().length() > 0) {
                startRecord = Long.parseLong(listValue.get(2));
            }
            Long pageSize = 20L;
            if (listValue.get(3) != null && listValue.get(3).trim().length() > 0) {
                pageSize = Long.parseLong(listValue.get(3));
            }
            // Ngay bat dau tu
            String startDateFrom = listValue.get(4);

            // Ngay bat dau den
            String startDateTo = listValue.get(5);

            // Tu khoa tim kiem
            String keyword = listValue.get(6);
            MissionDAO cmtDAO = new MissionDAO();
            Object result = cmtDAO.getListMissionUpcomingDeadline(userId, isCount, objType, startRecord, pageSize, keyword, startDateFrom, startDateTo);
            if (result == null) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            }
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException ex) {
            logger.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

//    /**
//     * <b>Cap nhat nhiem vu don vi phoi hop</b>
//     *
//     * @author SonDN
//     * @param request
//     * @param data
//     * @param isSecurity
//     * @return
//     */
//    public String updateProcessCombinationOrg(HttpServletRequest request,
//            String data, String isSecurity) {
//        EntityUserGroup entityUserGroup = FunctionCommon.getStatusSession(request);
//        // Khong co session
//        if (!entityUserGroup.getCheckSessionOk()) {
//            logger.error("updateProcessCombinationOrg - No session");
//            return FunctionCommon.generateResponseJSON(entityUserGroup.getEnumErrCode(), null, null);
//        }
//        // Kiem tra user id tren he thong 2
//        Long userId = entityUserGroup.getVof2_ItemEntityUser().getUserId();
//        if (userId == null) {
//            logger.error("updateProcessCombinationOrg - userId2 null");
//            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
//        }
//        // Giai ma du lieu neu ma hoa
//        String aesKey = null;
//        if (isSecurity != null && "1".equals(isSecurity)) {
//            // Lay AES Key
//            aesKey = entityUserGroup.getStrAesKey();
//            // Giai ma data client gui len
//            data = SecurityControler.decodeDataByAes(aesKey, data);
//        }
//        try {
//            JSONObject json = new JSONObject(data);
//            String[] keys = new String[]{
//                ConstantsFieldParams.ORG_COMBINATION_MAP_ID,
//                ConstantsFieldParams.ORG_COMBINATION_MAP_CONTENT};
//            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
//            Long orgCombinationMapId = Long.parseLong(listValue.get(0));
//            String content = listValue.get(1);
//            MissionDAO m = new MissionDAO();
//            Integer result = m.updateProcessCombinationOrg(orgCombinationMapId, content, userId);
//            if (result == 1) {
//                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
//            }
//            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
//
//        } catch (JSONException | NumberFormatException ex) {
//            LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_ERROR, ErrorCode.INPUT_INVALID, ex.getMessage(), data, isSecurity);
//            logger.error("updateProcessCombinationOrg:", ex);
//            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
//        }
//    }
    //</editor-fold>
    /**
     * <b>Bo sung thong tin nhiem vu</b>
     *
     * @author HaNH
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String addInformationMission(HttpServletRequest request,
            String data, String isSecurity) {
        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup entityUserGroup = FunctionCommon.getStatusSession(request);
        if (!entityUserGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(entityUserGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = entityUserGroup.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = entityUserGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        // Lay ma nhan vien
        String cardId = entityUserGroup.getCardId();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            //Danh sach nguon goc
            List<EntitySourceMap> listSourceMap = new LinkedList<>();
            Long missionId = Long.parseLong(FunctionCommon.jsonGetItem("missionId", data).toString());
            //Noi dung bo sung
            String content = FunctionCommon.jsonGetItem("content", data).toString();
            //Muc tieu
            String target = FunctionCommon.jsonGetItem("target", data).toString();
            JSONArray arrlistSourceMap = FunctionCommon.jsonGetArray("listSourceMap", data);
            if (arrlistSourceMap != null && arrlistSourceMap.length() > 0) {
                EntitySourceMap sourceMapItem;
                for (int i = 0; i < arrlistSourceMap.length(); i++) {
                    JSONObject objSource = (JSONObject) arrlistSourceMap.get(i);
                    sourceMapItem = new EntitySourceMap();
                    try {
                        sourceMapItem.setSourceType(objSource.getInt("sourceType"));
                        sourceMapItem.setSourceName(objSource.getString("sourceName"));
                        //Neu la nguon goc khac (sourceType = 1) thi khong co sourceId
                        if (objSource.has("sourceId")) {
                            sourceMapItem.setSourceId(objSource.getLong("sourceId"));
                        }
                        //Neu la nguon goc van ban (sourceType = 2) thi moi co confidential
                        if (objSource.has("confidential")) {
                            sourceMapItem.setConfidential(objSource.getInt("confidential"));
                        }

                        listSourceMap.add(sourceMapItem);
                    } catch (JSONException e) {
                        logger.error("Loi! JSONException", e);
                    }
                }
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
            MissionDAO m = new MissionDAO();
            Integer result = m.addInformationMission(missionId, target, content, listSourceMap, userId);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            if (result == 1) {
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            }
        } catch (JSONException ex) {
            logger.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * @author HaNH Dong nhiem vu
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String closeMission(HttpServletRequest request,
            String data, String isSecurity) {
        EntityUserGroup entityUserGroup = FunctionCommon.getStatusSession(request);
        if (!entityUserGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(entityUserGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = entityUserGroup.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = entityUserGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.MISSION_ID,
                ConstantsFieldParams.REQ_REASON
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long missionId = Long.parseLong(listValue.get(0));
            String reason = listValue.get(1);
            MissionDAO m = new MissionDAO();
            Integer result = m.closeMission(missionId, reason, userId);
            if (result == 1) {
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            }
        } catch (JSONException ex) {
            logger.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
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
    public String getListAddionalMission(HttpServletRequest request,
            String data, String isSecurity) {
        EntityUserGroup entityUserGroup = FunctionCommon.getStatusSession(request);
        if (!entityUserGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(entityUserGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = entityUserGroup.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = entityUserGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.MISSION_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long missionId = Long.parseLong(listValue.get(0));
            MissionDAO m = new MissionDAO();
            List<EntityMissionDetail> result = m.getListAddionalMission(missionId);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException ex) {
            logger.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * <b>Check fill tien do con moi nhat</b>
     *
     * @author HaNH
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getSubMissionNewestToFill(HttpServletRequest request,
            String data, String isSecurity) {
        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup entityUserGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!entityUserGroup.getCheckSessionOk()) {
            logger.error("updateProcessCombinationOrg - No session");
            return FunctionCommon.generateResponseJSON(entityUserGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = entityUserGroup.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            logger.error("updateProcessCombinationOrg - userId2 null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = entityUserGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        //Hiendv bo sung ghi log
        String cardId = entityUserGroup.getCardId();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.MISSION_ID};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long missionId = Long.parseLong(listValue.get(0));
            MissionDAO m = new MissionDAO();
            EntityMissionProcess result = m.getSubMissionNewestToFill(missionId);
            // Ghi log ket thuc chuc nang
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException | NumberFormatException ex) {
            LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_ERROR, ErrorCode.INPUT_INVALID, ex.getMessage(), data, isSecurity);
            logger.error("getSubMissionNewestToFill:", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
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
    public String findMissionByCondition(HttpServletRequest request,
            String data, String isSecurity) {
        
        String response;
        String[] keys = new String[]{
            ConstantsFieldParams.IS_COUNT,
            ConstantsFieldParams.IS_EXPORT,
            ConstantsFieldParams.IS_SEARCH_ADVANCED,
            ConstantsFieldParams.START_RECORD,
            ConstantsFieldParams.PAGE_SIZE,
            ConstantsFieldParams.TYPE,
            "isApprove",
            ConstantsFieldParams.MISSION_MISSION_GROUP,
            ConstantsFieldParams.MISSION_CLASS
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.returnResultAfterLogSessionTimeout(userGroup);
        }
        // Kiem tra user id tren he thong 2
        if (!userGroup.checkUserId2()) {
            return FunctionCommon.returnResultAfterLogNoInfo(userGroup.getUserId2());
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            Integer isCount = null;
            try {
                isCount = Integer.parseInt(listValue.get(0));
            } catch (NumberFormatException ex) {
            }
            Integer isExport = null;
            try {
                isExport = Integer.parseInt(listValue.get(1));
            } catch (NumberFormatException ex) {
            }
            Integer isSearchAdvanced = null;
            try {
                isSearchAdvanced = Integer.parseInt(listValue.get(2));
            } catch (NumberFormatException ex) {
            }
            Long startRecord = null;
            try {
                startRecord = Long.parseLong(listValue.get(3));
            } catch (NumberFormatException ex) {
            }
            Long pageSize = null;
            try {
                pageSize = Long.parseLong(listValue.get(4));
            } catch (NumberFormatException ex) {
            }
            String strType = listValue.get(5);
            Integer type = null;
            if (strType != null && !strType.isEmpty()) {
                type = Integer.valueOf(strType);
            }
            Integer missionGroup = null;
            if (!CommonUtils.isEmpty(listValue.get(7))) {
                missionGroup = Integer.parseInt(listValue.get(7));
            }
            // datdc Them search theo mission_class
            Integer missionClass = null;
            if (!CommonUtils.isEmpty(listValue.get(8))) {
                missionClass = Integer.parseInt(listValue.get(8));
            }
            
            // Lay danh sach id don vi user co vai tro thu truong, lanh dao, tro ly, tro ly chinh tri
            List<Long> listOrgId = new ArrayList<>();
            List<Long> listAssistantOrg = userGroup.getVof2_ItemEntityUser().getListAssistantOrg();
            List<Long> listManagementOrg = userGroup.getVof2_ItemEntityUser().getListManagementOrg();
            List<Long> listPoliticalAssistantOrgId = userGroup.getVof2_ItemEntityUser().getListPoliticalAssistantOrgId();
//            System.out.println("listAssistantOrg: " + listAssistantOrg);
//            System.out.println("listManagementOrg: " + listManagementOrg);
//            System.out.println("listPoliticalAssistantOrgId: " + listPoliticalAssistantOrgId);
            if (listManagementOrg != null && !listManagementOrg.isEmpty()) {
                for (Long obj : listManagementOrg) {
                    if (!listOrgId.contains(obj)) {
                        listOrgId.add(obj);
                    }
                }
            }
            if (listAssistantOrg != null && !listAssistantOrg.isEmpty()) {
                for (Long obj : listAssistantOrg) {
                    if (!listOrgId.contains(obj)) {
                        listOrgId.add(obj);
                    }
                }
            }
            Gson gson = new Gson();
            EntitySearchMission searchMission = gson.fromJson(userGroup.getKpiLog().getParamList(),
                    EntitySearchMission.class);
            if (searchMission == null) {
                response = FunctionCommon.responseResult(ErrorCode.ERR_NODATA, null, null);
                return FunctionCommon.returnResultAfterLogInputInvalid(response,
                        userGroup.getUserId2(), "searchMission");
            }
            MissionDAO m = new MissionDAO();
            Object result = m.findMissionByCondition(isCount, isExport,
                    isSearchAdvanced, startRecord, pageSize, searchMission,
                    userGroup.getUserId2(), listOrgId, listAssistantOrg, type,
                    missionGroup, listPoliticalAssistantOrgId, missionClass);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            return FunctionCommon.returnResultAfterLog(userGroup, ex);
        }
    }

    /**
     * <b>Sua dau moi thuc hien nhiem vu</b>
     *
     * @author HaNH
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String editPerformIdMission(HttpServletRequest request,
            String data, String isSecurity) {
        EntityUserGroup entityUserGroup = FunctionCommon.getStatusSession(request);
        if (!entityUserGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(entityUserGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = entityUserGroup.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = entityUserGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }

        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.MISSION_ID,
                ConstantsFieldParams.MISSION_PERFORM_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long missionId = Long.parseLong(listValue.get(0));
            Long performId = Long.parseLong(listValue.get(1));
            MissionDAO m = new MissionDAO();
            Integer result = m.editPerformIdMission(missionId, performId, userId);
            if (result == 1) {
                //100517 chuyen van ban cho TT, LD, VT don vi
                //neu sua viec co nguon goc tu van ban
                SourceMapDAO sourceMapDAO = new SourceMapDAO();
                MeetingController meetingCtl = new MeetingController();
                //chuyen van ban nguon goc
                List<EntitySourceMap> listSourceMap = sourceMapDAO.getListSourceByObject(missionId, 2, false);
                if (!CommonUtils.isEmpty(listSourceMap)) {
                    meetingCtl.sendDocToPersonInGroupMission(listSourceMap, entityUserGroup.getItemEntityUser(),
                            entityUserGroup.getVof2_ItemEntityUser(), null, null, null, 2, null, performId);
                }
                //chuyen van ban tham chieu
                listSourceMap = sourceMapDAO.getListSourceByObject(missionId, 6, false);
                if (!CommonUtils.isEmpty(listSourceMap)) {
                    meetingCtl.sendDocToPersonInGroupMission(listSourceMap, entityUserGroup.getItemEntityUser(),
                            entityUserGroup.getVof2_ItemEntityUser(), null, null, null, 6, null, performId);
                }
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            }
        } catch (JSONException | NumberFormatException ex) {
            LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_ERROR, ErrorCode.INPUT_INVALID, ex.getMessage(), data, isSecurity);
            logger.error("editPerformIdMission:", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * <b>Lay danh sach nguon goc</b>
     *
     * @author SonDN
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListSourceMaps(HttpServletRequest request,
            String data, String isSecurity) {
        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup entityUserGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!entityUserGroup.getCheckSessionOk()) {
            logger.error("getListSource - No session");
            return FunctionCommon.generateResponseJSON(entityUserGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = entityUserGroup.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            logger.error("getListSource - userId2 null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = entityUserGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        // Lay ma nhan vien
        String cardId = entityUserGroup.getCardId();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.STR_OBJECT_ID,
                ConstantsFieldParams.CMT_OBJECT_TYPE,
                ConstantsFieldParams.STR_SOURCE_TYPE
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long objectId = Long.parseLong(listValue.get(0));
            Integer objectType = Integer.parseInt(listValue.get(1));
            Integer sourceType = null;
            try {
                sourceType = Integer.parseInt(listValue.get(2));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            MissionDAO missionDAO = new MissionDAO();
            List<EntitySourceMap> result = missionDAO.getListSourceMaps(objectId, objectType, sourceType);
            // Neu danh sach nguon goc null
            // -> Tra ve thong bao loi
            if (result == null) {
                logger.error("getListSourceMaps - result null");
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            }
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException | NumberFormatException ex) {
            LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_ERROR, ErrorCode.INPUT_INVALID, ex.getMessage(), data, isSecurity);
            logger.error("getListSourceMaps - Exception:", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    public String getListMissionStatus(HttpServletRequest request,
            String data, String isSecurity) {
        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup entityUserGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!entityUserGroup.getCheckSessionOk()) {
            logger.error("getListSource - No session");
            return FunctionCommon.generateResponseJSON(entityUserGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = entityUserGroup.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            logger.error("getListSource - userId2 null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = entityUserGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        // Lay ma nhan vien
        String cardId = entityUserGroup.getCardId();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.LOCALE,
                ConstantsFieldParams.STATUS_TYPE,
                ConstantsFieldParams.IS_WEB,
                "isReceiveOrg"
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String locale = listValue.get(0);
            String strStatusType = listValue.get(1);
            String strWeb = listValue.get(2);
            String strReceiver = listValue.get(3);
            
            Long statusType = null;
            if (strStatusType != null && !strStatusType.isEmpty()) {
                statusType = Long.valueOf(strStatusType);
            }
            
            Integer isWeb = null;
            if (strWeb != null && !strWeb.isEmpty()) {
                try {
                    isWeb = Integer.parseInt(strWeb);
                } catch (NumberFormatException nfe) {
                    logger.error("getListMissionStatus - wrong value isWeb " + strWeb);
                }
            }
            
            Integer isReceive = null;
            if (strReceiver != null && !strReceiver.isEmpty()) {
                try {
                    isReceive = Integer.parseInt(strReceiver);
                } catch (NumberFormatException nfe) {
                    logger.error("getListMissionStatus - wrong value strReceiver " + strReceiver);
                }
            }
            boolean isAll = true;
            if (isWeb != null && isWeb.equals(1)) {
                isAll = false;
            } else if (!(isReceive != null && isReceive.equals(1))) {
                isAll = false;
            }
            MissionDAO missionDAO = new MissionDAO();
            List<EntityMissionStatus> result = missionDAO.getListMissionStatus(locale, statusType, isAll);
            // Neu danh sach nguon goc null
            // -> Tra ve thong bao loi
            if (result == null) {
                logger.error("getListMissionStatus - result null");
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            }
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException | NumberFormatException ex) {
            LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_ERROR, ErrorCode.INPUT_INVALID, ex.getMessage(), data, isSecurity);
            logger.error("getListMissionStatus - Exception:", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * chuyen van ban toi nguoi giao viec va tro li don vi
     *
     * @param assignId
     * @param orgAssignId
     * @param orgPerformId
     * @param listSourceMap
     * @param userVof1
     * @param userVof2
     */
    public void sendDocUpdateProcess(Long assignId, Long orgAssignId, Long orgPerformId,
            List<EntitySourceMap> listSourceMap, EntityUser userVof1, Vof2_EntityUser userVof2) {
        if (listSourceMap == null || listSourceMap.isEmpty() || assignId == null) {
            return;
        }
        try {
            //lay danh sach nguoi chuyen van van ban
            List<Vof2_EntityUser> listStaff = new ArrayList<>();
            //lay nguoi giao viec
            Vof2_EntityUser vof2EntityUser1 = new Vof2_EntityUser();
            vof2EntityUser1.setUserId(assignId);
            listStaff.add(vof2EntityUser1);
            //lay tro ly don vi
            UserDAO userDAO = new UserDAO();
            List<EntityVhrEmployee> lstEmp = userDAO.getUserAssistantOrg(orgAssignId);
            if (lstEmp != null && !lstEmp.isEmpty()) {
                Vof2_EntityUser vof2EntityUserTmp;
                for (EntityVhrEmployee emp1 : lstEmp) {
                    vof2EntityUserTmp = new Vof2_EntityUser();
                    vof2EntityUserTmp.setUserId(emp1.getEmployeeId());
                    listStaff.add(vof2EntityUserTmp);
                }
            }
            for (EntitySourceMap objSource : listSourceMap) {
                if (objSource.getSourceId() != null) {
                    //thuc hien chuyen van ban
                    DocumentDAO docDAO = new DocumentDAO();
                    docDAO.sendDocumentToStaffFromTask(objSource.getSourceId(), listStaff, null,
                            userVof1, userVof2);
                }
            }
        } catch (Exception ex) {
            logger.error("sendDocUpdateProcess: ", ex);
        }
    }
    
    /**
     * <b>Tao van ban tu danh sach nhiem vu</b>
     * 
     * @param request
     * @param data
     * @return 
     */
    public Object createTextFromMissionList(HttpServletRequest request, String data) {
        
        String[] keys = new String[] {
            "missionSigning",
            ConstantsFieldParams.STATUS
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        String errorDesc = "createTextFromMissionList - username: " + userGroup.getCardId();
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            errorDesc += " - Session timeout!";
            logger.error(errorDesc);
            return null;
        }
        try {
            // Parse du lieu tu client
            List<String> listValue = userGroup.getListParamsFromClient();
            // Doi tuong chua thong tin danh sach nhiem vu giao
            // va danh sach nhiem vu danh gia de ky thanh van ban
            String strMissionSigning = listValue.get(0);
            Gson gson = new Gson();
            EntityMissionSigning missionSigning = gson.fromJson(strMissionSigning,
                    EntityMissionSigning.class);
            // 0 - Save thong tin
            // 1 - Xem truoc file
            // 2 - Trinh ky
            // 3 - Xem van ban trinh ky
            Integer status = Integer.parseInt(listValue.get(1));
            // null - trinh ky van ban luon, 1 - xem truoc file trinh ky
            boolean isPreview = status == 1;
            MissionDAO missionDAO = new MissionDAO();
            Object result = null;
            switch (status) {
                // Save thong tin
                case 0:
                    if (missionDAO.updateAssignmentAndAssessmentInfo(userGroup.getUserId2(),
                            missionSigning.getListAssignment(), missionSigning.getListAssessment(),
                            null, null, null, null)) {
                        result = 1;
                    } else {
                        result = 0;
                    }
                    break;
                // Xem truoc file
                case 1:
                // Trinh ky
                case 2:
                    result = missionDAO.createTextFromMissionList(userGroup,
                        missionSigning, isPreview);
                    break;
                // Xem van ban trinh ky
                case 3:
                    result = missionDAO.getTextInfo(userGroup.getUserId2(), missionSigning);
                    break;
            }
            // Loi server
            if (result == null) {
                errorDesc += " - Loi tao van ban tu nhiem vu result = null!";
                logger.error(errorDesc);
                return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
            // Xem truoc file trinh ky
            if (isPreview) {
                List<EntityFileAttachment> listFile = (List<EntityFileAttachment>) result;
                // Chi co 1 file --> Tra ve noi dung file
                if (listFile.size() == 1) {
                    Response response = null;
                    File file = new File(listFile.get(0).getAttachment());
                    // File bien ban hop khong ton tai
                    if (!file.exists()) {
                        errorDesc += " - Loi file nhiem vu khong ton tai - path: "
                                + listFile.get(0).getAttachment();
                        logger.error(errorDesc);
                        return response;
                    }
                    InputStream inputStream = new InputStreamWithFileDeletion(file);
                    Response.ResponseBuilder responseBuilder = Response.ok(inputStream);
                    responseBuilder.header("File-Name", listFile.get(0).getName());
                    response = responseBuilder.build();
                    return response;
                } else {
                    return FunctionCommon.responseResult(ErrorCode.SUCCESS, listFile, userGroup);
                }
            } else {
                return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
            }            
        } catch (Exception ex) {
            logger.error("createTextFromMissionList - username: " + userGroup.getCardId()
                    + " - Exception!", ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }        
    }

    /**
     * <b>Bao cao tien do thuc hien nhiem vu</b>
     * 
     * @param request
     * @param appCode
     * @param appPass
     * @param data
     * @return 
     */
    public String reportMissionProcess(HttpServletRequest request, String appCode,
            String appPass, String data) {
        
        try {
            // Kiem tra du lieu dau vao
            if (CommonUtils.isEmpty(appCode) || CommonUtils.isEmpty(appPass)
                    || CommonUtils.isEmpty(data)) {
                logger.error("reportMissionProcess - Loi du lieu dau vao!");
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID,
                        null, null);
            }
            // Kiem tra thong tin ung dung
            UserDAO userDAO = new UserDAO();
            if (!userDAO.checkAppInfo(appCode, appPass)) {
                logger.error("reportMissionProcess - Thong tin ung dung khong dung!");
                return FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION,
                        null, null);
            }
            Gson gson = new Gson();
            EntityMissionProcessReport missionProcessReport = gson.fromJson(data,
                    EntityMissionProcessReport.class);
            // Khong co don vi giao nhiem vu
            if (missionProcessReport.getAssignmentOrgId() == null) {
                // Gan don vi giao mac dinh la tap doan
                missionProcessReport.setAssignmentOrgId(148842L);
            }
            MissionDAO missionDAO = new MissionDAO();
//            List<EntityMissionProcessReport> result = missionDAO.reportMissionProcessGiveSystemSLGB(
//                     missionProcessReport);
            List<EntityMissionProcessReport> result = missionDAO.reportMissionProcessGiveSystemSLGB(
                     missionProcessReport);
            
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, null);
        } catch (JsonSyntaxException ex) {
            logger.error("reportMissionProcess - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Luu thu tu nhiem vu</b>
     * 
     * @param request
     * @param data
     * @return 
     */
    public String saveMissionOrder(HttpServletRequest request, String data) {
        
        String[] keys = new String[] {
            "listMission"
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        String errorDesc = "saveMissionOrder - username: " + userGroup.getCardId() + " - ";
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            logger.error(errorDesc + "Session timeout!");
            return null;
        }
        try {
            // Parse du lieu tu client
            List<String> listValue = userGroup.getListParamsFromClient();
            // Danh sach nhiem vu
            String strListMission = listValue.get(0);
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<EntityMission>>() {
            }.getType();
            List<EntityMission> listMission = gson.fromJson(strListMission, type);
            MissionDAO missionDAO = new MissionDAO();
            int result = missionDAO.saveMissionOrder(userGroup.getUserId2(), listMission) ? 1 : 0;
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            logger.error("saveMissionOrder - username: " + userGroup.getCardId()
                    + " - Exception!", ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }        
    }
    
    /**
     * <b>Lay danh sach Ban Tong giam doc</b>
     * 
     * @return 
     */
    public String getListGeneralManager() {
        
        MissionDAO missionDAO = new MissionDAO();
        List<EntityVhrEmployee> listGeneralManager = missionDAO.getListGeneralManager();
        return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, listGeneralManager, null);
    }
    
    /**
     * <b>Canh bao nhiem vu</b>
     * 
     * @param assignId      id nguoi giao
     * @param orgId         id don vi
     * @param type          loai canh bao
     * @return 
     */
    public String getMissionWarning(Long assignId, Long orgId, Integer type) {
        
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
        String warningTime = sdf.format(date);
        MissionDAO missionDAO = new MissionDAO();
        EntityMissionWarning missionWarning = missionDAO.getMissionWarning(
                warningTime, assignId, orgId, type);
        return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, missionWarning, null);
    }
    
    /**
     * <b>Lay danh sach don vi thuc hien trong Khoi co quan</b>
     * 
     * @return 
     */
    public String getListPerformingOrg() {
        
        MissionDAO missionDAO = new MissionDAO();
        List<EntityVhrOrg> listPerformingOrg = missionDAO.getListPerformingOrg();
        return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, listPerformingOrg, null);
    }
    
    /**
     * <b>Kiem tra so lan gia han duoc phep cua mission</b>
     * @return 
     */
    public String checkExtendable(HttpServletRequest request,
            String data, String isSecurity) {
        String[] keys = new String[] {
            "missionId",
            "sourceType"
        };
        Object result = null;
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            logger.error("checkExtendable - Session timeout!");
            return FunctionCommon.responseResult(ErrorCode.SESSION_TIME_OUT, null, null);
        }
        Long missionId = null;
        try {
            // Parse du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            missionId = Long.parseLong(listValue.get(0));
            Integer sourceType = Integer.parseInt(listValue.get(1));
            if (missionId != null) {
                MissionDAO dao = new MissionDAO();
                result = dao.checkExtendable(missionId, sourceType);
            } else {
                logger.error("checkExtendable - Exception - missionId: " + missionId);
                return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception e) {
            logger.error("checkExtendable - Exception - missionId: "
                    + missionId, e);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    public String getCountMissionNeedCompleted(HttpServletRequest request,
            String data, String isSecurity) {
        String[] keys = new String[] {
            "userId"
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            logger.error("getCountMissionNeedCompleted - Session timeout!");
            return FunctionCommon.responseResult(ErrorCode.SESSION_TIME_OUT, null, null);
        }
        try {
            Long userId = userGroup.getUserId2();
            //Lay danh sach don vi user lam thu truong, lanh dao, tro ly
            List<Long> listOrgId = new ArrayList<>();
            List<Long> listAssistantOrg = userGroup.getVof2_ItemEntityUser().getListAssistantOrg();
            List<Long> listManagementOrg = userGroup.getVof2_ItemEntityUser().getListManagementOrg();
            List<Long> listPoliticalAssistantOrgId = userGroup.getVof2_ItemEntityUser().getListPoliticalAssistantOrgId();
            if (listManagementOrg != null && !listManagementOrg.isEmpty()) {
                listOrgId.addAll(listManagementOrg);
            }
            if (listAssistantOrg != null && !listAssistantOrg.isEmpty()) {
                for (Long id : listAssistantOrg) {
                    if (!listOrgId.contains(id)) {
                        listOrgId.add(id);
                    }
                }
            }
            EntitySearchMission searchEn = new EntitySearchMission();
            List<Integer> searchStatus = new ArrayList<>();
            //nhiem vu phai hoan thanh
            searchStatus.add(MissionDAO.STATUS_MISSION_NEED_COMPLETED_WITHIN_WEEK);
            searchEn.setListStatus(searchStatus);
            Integer type = 5;
            searchEn.setTypeMission(8);
//            System.out.println("userId: " + userId + " - listOrg: " + listOrgId);
            MissionDAO md = new MissionDAO();
            Long countAll = (Long)md.findMissionByCondition(1, null, 1, null, null, searchEn, 
                userId, listOrgId, listAssistantOrg, type, null, listPoliticalAssistantOrgId, null);
            List<Long> result = new ArrayList<>();
            
            
            //nhiem vu cham tiend o
            searchStatus.clear();
            searchStatus.add(MissionDAO.STATUS_MISSION_DELAY_PERFORM_ORG_ONLY);
            searchEn.setListStatus(searchStatus);
            Long countLate = (Long)md.findMissionByCondition(1, null, 1, null, null, searchEn, 
                userId, listOrgId, listAssistantOrg, type, null, listPoliticalAssistantOrgId, null);
            result.add(countLate);
            result.add(countAll - countLate);
//            List<Long> result = md.getCountMissionNeedCompleted(listOrgId,userId);
            EntityMission mission = new EntityMission();
            if (result.size() > 1) {
                mission.setCountOverDual(result.get(0));
                mission.setCountDueDate(result.get(1));
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, mission, userGroup);
        } catch (Exception e) {
            logger.error("getCountMissionNeedCompleted - error!");
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
        
    }
    
    public String findDueDateMission(HttpServletRequest request,
            String data, String isSecurity) {
        String[] keys = new String[] {
            ConstantsFieldParams.TYPE
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            logger.error("findDueDateMission - Session timeout!");
            return FunctionCommon.responseResult(ErrorCode.SESSION_TIME_OUT, null, null);
        }
        try {
            Long userId = userGroup.getUserId2();
            //Lay danh sach don vi user lam thu truong, lanh dao, tro ly
            List<Long> listOrgId = new ArrayList<>();
            List<Long> listAssistantOrg = userGroup.getVof2_ItemEntityUser().getListAssistantOrg();
            List<Long> listManagementOrg = userGroup.getVof2_ItemEntityUser().getListManagementOrg();
            List<Long> listPoliticalAssistantOrgId = userGroup.getVof2_ItemEntityUser().getListPoliticalAssistantOrgId();
            if (listManagementOrg != null && !listManagementOrg.isEmpty()) {
                listOrgId.addAll(listManagementOrg);
            }
            if (listAssistantOrg != null && !listAssistantOrg.isEmpty()) {
                for (Long id : listAssistantOrg) {
                    if (!listOrgId.contains(id)) {
                        listOrgId.add(id);
                    }
                }
            }
            EntitySearchMission searchMission = new EntitySearchMission();
            List<String> listValue = userGroup.getListParamsFromClient();
            String strType = listValue.get(0);
            Integer type = null;
            if (strType != null && !strType.isEmpty()) {
                try {
                    type = Integer.parseInt(strType);
                } catch (NumberFormatException nfe) {
                    logger.error("findDueDateMission - type is not number!");
                }
            }
//            System.out.println("userId: " + userId + " - listOrg: " + listOrgId);
            MissionDAO md = new MissionDAO();
            
            List<EntityMission> result = (List<EntityMission>)md.findDueDateMission(searchMission, userId, listOrgId, type);
            
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception e) {
            logger.error("findDueDateMission - fail!");
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
        
    }
    
    public String getFieldIdFromOrgPerform(HttpServletRequest request,
            String data, String isSecurity) {
        String[] keys = new String[] { "performIds" };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            logger.error("getFieldIdFromOrgPerform - Session timeout!");
            return FunctionCommon.responseResult(ErrorCode.SESSION_TIME_OUT, null, null);
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            String performListId = listValue.get(0);
            if (CommonUtils.isEmpty(performListId)) {
                logger.error("getFieldIdFromOrgPerform - performListId null or empty!");
                return null;
            }
//            System.out.println("userId: " + userId + " - listOrg: " + listOrgId);
            MissionDAO md = new MissionDAO();
            
            Long result = md.getFieldIdFromOrgPerform(performListId);
            
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception e) {
            logger.error("getFieldIdFromOrgPerform - fail!");
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }
    
    /**
     * Lay danh sach nguon goc phieu giao nhiem vu
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getViewSourceMap(HttpServletRequest request,
            String data, String isSecurity) {

        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            logger.error("getListFileMeeting - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        try {
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[] { ConstantsFieldParams.MEETING_ID };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String strList = listValue.get(0);
            if (CommonUtils.isEmpty(strList)) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null); 
            }
            List<Long> ids = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(strList);
            for (int i = 0; i < jsonArray.length(); i++) {
                ids.add(jsonArray.getLong(i));
            }
            MissionDAO dao = new MissionDAO();
            List<EntitySourceMap> results = dao.getViewSourceMap(ids);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, results, userGroup.getStrAesKey());
        } catch (Exception ex) {
            logger.error("updateMemberReplate - username: " + userGroup.getCardId() + " - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    private void fowardAfterUpdate(EntityUserGroup userGroup, List<Long> listPerform,
        Long missionId, Long orgPerformIdOld, Long performId, String reason) {
        //100517 chuyen van ban cho TT, LD, VT don vi
        //neu sua viec co nguon goc tu van ban
        SourceMapDAO sourceMapDAO = new SourceMapDAO();
        MeetingController mC = new MeetingController();
        //chuyen van ban nguon goc
        List<EntitySourceMap> listSourceMap = sourceMapDAO.getListSourceByObject(missionId, 2, false);
        if (!CommonUtils.isEmpty(listSourceMap)) {
            mC.sendDocToPersonInGroupMission(listSourceMap, userGroup.getItemEntityUser(),
                        userGroup.getVof2_ItemEntityUser(), listPerform, null, null, 2, null, null);
        }
        // Chuyen van ban tham chieu
        listSourceMap = sourceMapDAO.getListSourceByObject(missionId, 6, false);
        if (!CommonUtils.isEmpty(listSourceMap)) {
            mC.sendDocToPersonInGroupMission(listSourceMap, userGroup.getItemEntityUser(),
                        userGroup.getVof2_ItemEntityUser(), listPerform, null, null, 6, null, null);
        }
        // Lay bao cao
        MissionDAO m = new MissionDAO();
        listSourceMap = m.getListDocReport(missionId);
        if (!CommonUtils.isEmpty(listSourceMap)) {
            mC.sendDocToPersonInGroupMission(listSourceMap, userGroup.getItemEntityUser(),
                        userGroup.getVof2_ItemEntityUser(), listPerform, null, null, 7, null, null);
        }

        SmsDAO sms = new SmsDAO();
        StaffDAO staffDAO = new StaffDAO();
        VHROrgDAO org = new VHROrgDAO();
        UserDAO userDao = new UserDAO();
        EntityMission mission = m.getMissionDetail(missionId,
                userGroup.getUserId2(), null, null, null, "vi", null, null);
        //Lay ID nguoi giao, don vi giao

        Long assignId = mission.getAssignId();
        Long orgAssignId = mission.getOrgAssignId();
        // Lay danh sach thu truong, lanh dao tro ly cua don vi thuc hien cu
        List<Long> listPerformIdOldTmp = new ArrayList<>();
        listPerformIdOldTmp.add(orgPerformIdOld);
        List<EntityVhrEmployee> staffPerformOrgs;
        if (mission.getMissionGroup() != null && mission.getMissionGroup() == 3) {
            staffPerformOrgs = staffDAO.getListPoliticalAssistant(null, listPerformIdOldTmp);
        } else {
            staffPerformOrgs = staffDAO.getLeaderAndAssistantByOrg(null, listPerformIdOldTmp);
        }
        // Neu nhiem vu co dau moi thuc hien thi add them dau moi vao danh sach
        if (performId != null) {
            EntityVhrEmployee userPerform = userDao.getEmployeeById(performId);
            staffPerformOrgs.add(userPerform);
        }
        //Lay thong tin nguoi giao nhiem vu
        List<Long> listAssignTmp = new ArrayList<>();
        listAssignTmp.add(orgAssignId);
        EntityVhrEmployee userAssign;
        List<EntityVhrEmployee> lstVhrEmployee = staffDAO.getLeaderByOrg(assignId, listAssignTmp);
        if (!CommonUtils.isEmpty(lstVhrEmployee)) {
            userAssign = lstVhrEmployee.get(0);
            //Khoi tao cac tham so de chen vao noi dung tin nhan
            //List listObjectSendMess1Final gom 4 phan tu: Chuc vu nguoi giao, ten nguoi giao, ten viet tat don vi thuc hien moi, ten nhiem vu
            List<String> listObjectSendMess1 = new ArrayList<>();
            List<String> listObjectSendMess1Tmp = new ArrayList<>();
            List<String> listObjectSendMess1Final = new ArrayList<>();
            // Them lst string cho sms send don vi moi start
            List<String> listObjectSendMess1FinalNewOrg = new ArrayList<>();
            List<String> listObjectSendMess1TmpNew = new ArrayList<>();
            // Them lst string cho sms send don vi moi end
            listObjectSendMess1.add(userAssign.getPosition());
            //050417 sua gui tin nhan lay fullName thay displayName
            listObjectSendMess1.add(userAssign.getFullName());
            //Lay ten viet tat cua don vi thuc hien moi
            
            for (Long orgPerformIdNew : listPerform) {
                EntityVhrOrg orgPerform = org.getVHROrg(orgPerformIdNew);
                // tao them noi dung rieng gui sms cho don vi moi start
                listObjectSendMess1Tmp.clear();
                listObjectSendMess1Final.clear();
                listObjectSendMess1TmpNew.clear();
                listObjectSendMess1FinalNewOrg.clear();
                listObjectSendMess1Tmp.add(orgPerform.getAbbreviation());
                listObjectSendMess1Tmp.add(mission.getMissionName());
                listObjectSendMess1Final.addAll(listObjectSendMess1);
                listObjectSendMess1Final.addAll(listObjectSendMess1Tmp);
                // add them reason start
                listObjectSendMess1TmpNew = listObjectSendMess1Tmp;
                listObjectSendMess1TmpNew.add(reason);
                // add them reason end
                listObjectSendMess1FinalNewOrg.addAll(listObjectSendMess1);
                listObjectSendMess1FinalNewOrg.addAll(listObjectSendMess1TmpNew);
                // tao them noi dung rieng gui sms cho don vi moi end
                
                if (CommonUtils.isEmpty(staffPerformOrgs)) {
                    logger.error("Khong co ca nhan thoa man dieu kien gui tin nhan!");
                } else {
                    for (EntityVhrEmployee person : staffPerformOrgs) {
                        CommonControler common = new CommonControler();
                        String phone = person.getMobilePhone();
                        //Check dieu kien so dien thoai nguoi nhan khong nam trong Black List
                        if (phone != null) {//&& sms.isNotInBlackList(phone)
                            //Chen cac thong tin vao mau noi dung tin nhan
                            String content = common.getStrMessConfig(listObjectSendMess1Final, 4L, 5L, person.getEmployeeId());
                            if (!sms.addMsgToSmsMaster(phone,
                                    content, userGroup.getUserId2(),
                                    userGroup.getUserId2(), person.getEmployeeId(), 5L, -1L,
                                    Constants.SMS_TEXT_INTERCEPT.CHANGEMISS_TODEPART)) {
                                logger.error("Loi! Khong insert duoc tin nhan vao DataBase");
                            }
                        }
                    }
                }

                //for (Long orgPerformIdNew : listPerform) {
                //Lay danh sach thu truong, lanh dao tro ly cua don vi thuc hien moi
                List<Long> listPerformIdNewTmp = new ArrayList<>();
                listPerformIdNewTmp.add(orgPerformIdNew);
                List<EntityVhrEmployee> staffPerformNewOrgs;
                if (mission.getMissionGroup() != null && mission.getMissionGroup() == 3) {
                    staffPerformNewOrgs = staffDAO.getListPoliticalAssistant(null, listPerformIdNewTmp);
                } else {
                    staffPerformNewOrgs = staffDAO.getLeaderAndAssistantByOrg(null, listPerformIdNewTmp);
                }
                if (CommonUtils.isEmpty(staffPerformNewOrgs)) {
                    logger.error("Khong co ca nhan thoa man dieu kien gui tin nhan!");
                } else {
                    for (EntityVhrEmployee person : staffPerformNewOrgs) {
                        CommonControler common = new CommonControler();
                        String phone = person.getMobilePhone();
                        //Check dieu kien so dien thoai nguoi nhan khong nam trong Black List
                        if (phone != null && sms.isNotInBlackList(phone)) {
                            //Chen cac thong tin vao mau noi dung tin nhan
                            // Truyen them reason
                            // doi sang type la 57
                            String content = common.getStrMessConfig(listObjectSendMess1FinalNewOrg, 4L, 57L, person.getEmployeeId());
                            if (!sms.addMsgToSmsMaster(phone, content,
                                    userGroup.getUserId2(), userGroup.getUserId2(),
                                    person.getEmployeeId(), 5L, -1L,
                                    Constants.SMS_TEXT_INTERCEPT.LEADER_MESSGIVEMISSION)) {
                                logger.error("Loi! Khong insert duoc tin nhan vao DataBase");
                            }
                        }
                    }
                }
            }
        }
    }
}
