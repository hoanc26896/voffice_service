/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.ErrorCodeEnumAdapterTypeAdapter;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.constants.StringConstants;
import com.viettel.voffice.database.dao.SourceMapDAO;
import com.viettel.voffice.database.dao.document.DocumentDAO;
import com.viettel.voffice.database.dao.document.VHROrgDAO;
import com.viettel.voffice.database.dao.meeting.MeetingDAO;
import com.viettel.voffice.database.dao.meeting.MeetingMinutesDAO;
import com.viettel.voffice.database.dao.meeting.MeetingWeekDAO;
import com.viettel.voffice.database.dao.sms.SmsDAO;
import com.viettel.voffice.database.dao.staff.OrgDAO;
import com.viettel.voffice.database.dao.staff.StaffDAO;
import com.viettel.voffice.database.dao.staff.UserDAO;
import com.viettel.voffice.database.dao.task.MissionDAO;
import com.viettel.voffice.database.entity.EntityConfigUser;
import com.viettel.voffice.database.entity.EntityDetailDocument;
import com.viettel.voffice.database.entity.EntityDocument;
import com.viettel.voffice.database.entity.EntityFields;
import com.viettel.voffice.database.entity.EntityFileAttachment;
import com.viettel.voffice.database.entity.EntityFiles;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.EntityUserMeeting;
import com.viettel.voffice.database.entity.EntityVhrEmployee;
import com.viettel.voffice.database.entity.EntityVhrOrg;
import com.viettel.voffice.database.entity.User.EntityUser;
import com.viettel.voffice.database.entity.User.Vof2_EntityUser;
import com.viettel.voffice.database.entity.calendar.MeetingApproveResult;
import com.viettel.voffice.database.entity.document.EntityDocumentInStaff;
import com.viettel.voffice.database.entity.meeting.EntityMeetingMember;
import com.viettel.voffice.database.entity.meeting.EntityMeetingMinutes;
import com.viettel.voffice.database.entity.task.EntityCombination;
import com.viettel.voffice.database.entity.task.EntityMeeting;
import com.viettel.voffice.database.entity.task.EntityMeetingResource;
import com.viettel.voffice.database.entity.task.EntityMission;
import com.viettel.voffice.database.entity.task.EntityMissionChild;
import com.viettel.voffice.database.entity.task.EntitySourceMap;
import com.viettel.voffice.database.entity.task.EntitySourceTypeMeeting;
import com.viettel.voffice.database.entity.task.EntitySourceTypeOrientation;
import com.viettel.voffice.elasticsearch.indexdata.IndexDocumentByType;
import com.viettel.voffice.thread.ThreadUpdateBoardNameSmartRoom;
import com.viettel.voffice.threadmanager.ThreadPoolCommon;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.DateUtils;
import com.viettel.voffice.utils.LogUtils;

/**
 *
 * @author kiennt45
 */
@SuppressWarnings("deprecation")
public class MeetingController {

    public static final String ROOT_ACTION = "Meeting";

    /**
     * Log file
     */
    private static final Logger logger = Logger.getLogger(MeetingController.class);

    /**
     * Ten cua class bao gom ca ten package
     */
    private static final String CLASS_NAME = MeetingController.class.getName();

    public String getMeetingMinutes(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        Date startDate = new Date();
        LogActionControler aclog = new LogActionControler();
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //lay key Aes
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String decryptedData = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                //lay gia tri luu trong seasion
                String userId = dataSessionGR.getVof2_ItemEntityUser().getUserId().toString();
                List<Long> orgIdsByRole = new ArrayList<>();
                List<Long> listManagement = dataSessionGR.getVof2_ItemEntityUser().getListManagementOrg();
                List<Long> listAssistant = dataSessionGR.getVof2_ItemEntityUser().getListAssistantOrg();

                if (listManagement != null && listManagement.size() > 0) {
                    orgIdsByRole.addAll(listManagement);
                }

                if (listAssistant != null && listAssistant.size() > 0) {
                    orgIdsByRole.addAll(listAssistant);
                }
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(decryptedData);
                LogUtils.logFunctionalStart(log);
                //lay gia tri client gui len
                HashMap<String, Object> hmParams = new HashMap<>();
                hmParams.put(ConstantsFieldParams.STR_KEYWORDS, String.class);
                hmParams.put(ConstantsFieldParams.START_RECORD, Long.class);
                hmParams.put(ConstantsFieldParams.PAGE_SIZE, Long.class);
                //Begin:SonDN
                hmParams.put(ConstantsFieldParams.CODE, String.class);
                hmParams.put(ConstantsFieldParams.TITLE, String.class);
                hmParams.put(ConstantsFieldParams.FROM_DATE, String.class);
                hmParams.put(ConstantsFieldParams.TO_DATE, String.class);
                hmParams.put(ConstantsFieldParams.CONCLUSION_ID, Long.class);
                hmParams.put(ConstantsFieldParams.TYPE, Long.class);
                hmParams.put(ConstantsFieldParams.DOCUMENT_ID, Long.class);
                hmParams.put(ConstantsFieldParams.IS_COUNT, Integer.class);
                //End
                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);

                String strSearch = (String) valueParams.get(ConstantsFieldParams.STR_KEYWORDS);
                Long startRecord = (Long) ((valueParams.get(ConstantsFieldParams.START_RECORD) != null) ? valueParams.get(ConstantsFieldParams.START_RECORD) : 0L);
                Long pageSize = (Long) ((valueParams.get(ConstantsFieldParams.PAGE_SIZE) != null) ? valueParams.get(ConstantsFieldParams.PAGE_SIZE) : 10L);
                // Bengin:SonDN
                String textCode = (String) valueParams.get(ConstantsFieldParams.CODE);
                String textTitle = (String) valueParams.get(ConstantsFieldParams.TITLE);
                String fromDate = (String) valueParams.get(ConstantsFieldParams.FROM_DATE);
                String toDate = (String) valueParams.get(ConstantsFieldParams.TO_DATE);
                Long conclusionId = (Long) (valueParams.get(ConstantsFieldParams.CONCLUSION_ID) != null ? valueParams.get(ConstantsFieldParams.CONCLUSION_ID) : 0L);
                Long type = (Long) (valueParams.get(ConstantsFieldParams.TYPE) != null ? valueParams.get(ConstantsFieldParams.TYPE) : 0L);
                Long documentId = (Long) (valueParams.get(ConstantsFieldParams.DOCUMENT_ID) != null ? valueParams.get(ConstantsFieldParams.DOCUMENT_ID) : 0L);
                Integer isCount = (Integer) ((valueParams.get(ConstantsFieldParams.IS_COUNT) != null) ? valueParams.get(ConstantsFieldParams.IS_COUNT) : 0);
                //End
                //Kiem tra gia tri dau vao truoc khi thuc hien
                // 16:24 03/11/2016 - Thanght6
                // Fix loi logic ma bi Sonnar quet ra
                if (CommonUtils.isEmpty(orgIdsByRole)) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
                }

                MeetingDAO meetingD = new MeetingDAO();
                Object listItem = meetingD.getMeetingMinutes(userId, orgIdsByRole, strSearch,
                        startRecord, pageSize, textCode, textTitle, fromDate, toDate, conclusionId, type, documentId, isCount);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, listItem, strAesKeyDecode);
                Vof2_EntityUser itemUser = dataSessionGR.getVof2_ItemEntityUser();
                aclog.insertActionLog(itemUser.getUserId(), itemUser.getStrCardNumber(),
                        "MeetingController.getMeetingMinutes", req, "", startDate, "", "");
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_ERROR, dataSessionGR.getEnumErrCode(), e.getMessage(), strData, isSecurity);
                logger.error(e.getMessage(), e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }

        return strResult;
    }

    public String countMeetingMinutes(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        Date startDate = new Date();
        LogActionControler aclog = new LogActionControler();
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //check sessionOk
                //lay key Aes
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String decryptedData = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                //lay gia tri tu session
                String userId = dataSessionGR.getVof2_ItemEntityUser().getUserId().toString();
                List<Long> orgIdsByRole = new ArrayList<>();
                List<Long> listManagement = dataSessionGR.getVof2_ItemEntityUser().getListManagementOrg();
                List<Long> listAssistant = dataSessionGR.getVof2_ItemEntityUser().getListAssistantOrg();
                if (listManagement != null && listManagement.size() > 0) {
                    orgIdsByRole.addAll(listManagement);
                }
                if (listAssistant != null && listAssistant.size() > 0) {
                    orgIdsByRole.addAll(listAssistant);
                }
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(decryptedData);
                LogUtils.logFunctionalStart(log);
                //lay gia tri client gui len
                HashMap<String, Object> hmParams = new HashMap<>();
                hmParams.put(ConstantsFieldParams.STR_KEYWORDS, String.class);
                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);

                String strSearch = (String) valueParams.get(ConstantsFieldParams.STR_KEYWORDS);

                MeetingDAO meetingD = new MeetingDAO();
                Long count = meetingD.countMeetingMinutes(userId, orgIdsByRole, strSearch);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, count, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_ERROR, dataSessionGR.getEnumErrCode(), e.getMessage(), strData, isSecurity);
//                System.out.println("Error: " + e.toString());
                logger.error(e.getMessage(), e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
            Vof2_EntityUser itemUser = dataSessionGR.getVof2_ItemEntityUser();
            aclog.insertActionLog(itemUser.getUserId(), itemUser.getStrCardNumber(), "MeetingController.countMeetingMinutes", req, "a", startDate, "", "");
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }

        return strResult;
    }

    public String getMissionByMeetingId(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String decryptedData = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                //lay gia tri tu session
                Long employeeId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(decryptedData);
                LogUtils.logFunctionalStart(log);
                //lay gia tri client gui len
                HashMap<String, Object> hmParams = new HashMap<>();
                hmParams.put(ConstantsFieldParams.STR_SOURCEID, String.class);
                hmParams.put(ConstantsFieldParams.START_RECORD, Long.class);
                hmParams.put(ConstantsFieldParams.PAGE_SIZE, Long.class);
                hmParams.put(ConstantsFieldParams.TYPE, String.class);
                //Begin  :: cuongnv
                hmParams.put(ConstantsFieldParams.SENDER_ID_VOF2, Long.class);
                hmParams.put(ConstantsFieldParams.IS_COUNT, Integer.class);
                //End

                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);

                String sourceId = (String) valueParams.get(ConstantsFieldParams.STR_SOURCEID);
                Long startRecord = (Long) ((valueParams.get(ConstantsFieldParams.START_RECORD) != null) ? valueParams.get(ConstantsFieldParams.START_RECORD) : null);
                Long pageSize = (Long) ((valueParams.get(ConstantsFieldParams.PAGE_SIZE) != null) ? valueParams.get(ConstantsFieldParams.PAGE_SIZE) : null);
                String sourceType = (String) ((valueParams.get(ConstantsFieldParams.TYPE) != null) ? valueParams.get(ConstantsFieldParams.TYPE) : "");
                //System.out.println("luanvd-getMissionByMeetingId-data:\n type : "+sourceType+"\nsource_id:"+sourceId);
                //kiem tra du lieu dau vao
                if (sourceId == null || "".equals(sourceId)) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                }
                Integer isCount = (Integer) ((valueParams.get(ConstantsFieldParams.IS_COUNT) != null) ? valueParams.get(ConstantsFieldParams.IS_COUNT) : 0);
                //Begin  :: cuongnv
                Long sendId2 = (Long) ((valueParams.get(ConstantsFieldParams.SENDER_ID_VOF2) != null) ? valueParams.get(ConstantsFieldParams.SENDER_ID_VOF2) : null);
                if (sendId2 != null) {
                    MeetingDAO meetingD = new MeetingDAO();
                    Object listItem = meetingD.getMissionByMeetingId(sourceId, startRecord, pageSize, sendId2, sourceType, isCount);
                    return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, listItem, strAesKeyDecode);
                }
                //End
                MeetingDAO meetingD = new MeetingDAO();
                Object listItem = meetingD.getMissionByMeetingId(sourceId, startRecord, pageSize, employeeId, sourceType, isCount);

                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                        listItem, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_ERROR, dataSessionGR.getEnumErrCode(), e.getMessage(), strData, isSecurity);

                logger.error(e.getMessage(), e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }

        return strResult;
    }

    public String countMissionByMeetingId(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //check sessionOk
                //lay key Aes
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);
                //lay gia tri client gui len
                String sourceId = (String) FunctionCommon.jsonGetItem(ConstantsFieldParams.STR_SOURCEID, strDataClient);

                //kiem tra du lieu dau vao
                if (sourceId == null || "".equals(sourceId)) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                }

                MeetingDAO meetingD = new MeetingDAO();
                Long listItem = meetingD.countMissionByMeetingId(sourceId);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                        listItem, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_ERROR, dataSessionGR.getEnumErrCode(), e.getMessage(), strData, isSecurity);
//                System.out.println("Error: " + e.toString());
                logger.error(e.getMessage(), e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }

        return strResult;
    }

    /**
     * <b>Lay chi tiet bien ban hop</b>
     *
     * @param isSecurity
     * @param data
     * @param request
     * @return
     */
    public String getMeetingMinutesById(String isSecurity, String data,
            HttpServletRequest request) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(request);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String decryptedData = SecurityControler.decodeDataByAes(strAesKeyDecode, data);
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(decryptedData);
                LogUtils.logFunctionalStart(log);
                //lay gia tri client gui len
                HashMap<String, Object> hmParams = new HashMap<>();
                hmParams.put(ConstantsFieldParams.STR_SOURCEID, String.class);
                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, data, dataSessionGR);

                String sourceId = (String) valueParams.get(ConstantsFieldParams.STR_SOURCEID);

                //kiem tra du lieu dau vao
                if (sourceId == null || "".equals(sourceId)) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                }

                MeetingDAO meetingD = new MeetingDAO();
                EntityMeeting listItem = meetingD.getMeetingMinutesById(sourceId, dataSessionGR);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                        listItem, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception ex) {
                logger.error("getMeetingMinutesById (Lay chi tiet bien ban hop) - Exception!", ex);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }

        return strResult;
    }

    public String getListFileAttachment(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            String strAesKeyDecode = dataSessionGR.getStrAesKey();
            try {
                String decryptedData = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(decryptedData);
                LogUtils.logFunctionalStart(log);
                HashMap<String, Object> hmParams = new HashMap<>();
                hmParams.put(ConstantsFieldParams.STR_OBJECT_TYPE, Long.class);
                hmParams.put(ConstantsFieldParams.STR_OBJECT_ID, Long.class);
                hmParams.put(ConstantsFieldParams.START_RECORD, Long.class);
                hmParams.put(ConstantsFieldParams.PAGE_SIZE, Long.class);
                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);

                //lay gia tri client gui len
                Long startRecord = (Long) ((valueParams.get(ConstantsFieldParams.START_RECORD) != null) ? valueParams.get(ConstantsFieldParams.START_RECORD) : 0L);
                Long pageSize = (Long) ((valueParams.get(ConstantsFieldParams.PAGE_SIZE) != null) ? valueParams.get(ConstantsFieldParams.PAGE_SIZE) : 10L);
                Long objectType = (Long) valueParams.get(ConstantsFieldParams.STR_OBJECT_TYPE);
                Long objectId = (Long) valueParams.get(ConstantsFieldParams.STR_OBJECT_ID);

                //kiem tra du lieu dau vao
                if (objectType == null || objectId == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                }

                MeetingDAO meetingD = new MeetingDAO();
                List<EntityFileAttachment> listItem = meetingD.getListFileAttachment(objectType, objectId, startRecord, pageSize);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                        listItem, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                logger.error("Error: MeetingController.getListFileAttachment", e);
//                System.out.println("Error: " + e.toString());
//                strResult = "Error: " + e.toString();
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }

        return strResult;
    }

    /**
     * <b>Lay danh sach don vi giao nhiem vu</b>
     * 
     * @param isSecurity
     * @param strData
     * @param req
     * @return 
     */
    public String getListOrganizationByUser(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(req, strData, null);
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.returnResultAfterLogSessionTimeout(userGroup);
        }
        try {
            // ID don vi tap doan
            Long tdOrgId = Long.valueOf(CommonUtils.getAppConfigValue("tdOrgId"));
            // Lay danh sach id don vi co vai tro thu truong, lanh dao
            // tro ly, tro ly chinh tri
            List<Long> listOrgId = userGroup.getVof2_ItemEntityUser()
                    .getListOrgWhichHasThePermissionToMission();
//            boolean checkVIG = false;
//            if (tdOrgId != null && !CommonUtils.isEmpty(listOrgId)
//                    && listOrgId.contains(tdOrgId)) {
//                checkVIG = true;
//            }
            // Thuc hien chen code lay du lieu va thao tac tra ve o day
            MeetingDAO meetingDAO = new MeetingDAO();
            List<EntityVhrEmployee> listEmployee = meetingDAO.getListOrganizationByUser(
                    userGroup.getUserId2());
            //vanpn: bo doan code check cap cha la tap doan (ban dau cap cha la tap doan khong duoc add vao)
//            if (tdOrgId != null && !checkVIG && !CommonUtils.isEmpty(listEmployee)) {
//                List<EntityVhrEmployee> listFiltered = new ArrayList<>();
//                for (EntityVhrEmployee emp : listEmployee) {
//                    if (!tdOrgId.equals(emp.getSysOrganizationId())) {
//                        listFiltered.add(emp);
//                    }
//                }
//                listEmployee = listFiltered;
//            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, listEmployee, userGroup);
        } catch (Exception ex) {
            return FunctionCommon.returnResultAfterLog(userGroup, ex);
        }
    }

    /**
     * Lay danh sach don vi phoi hop: lay tat ca cay don vi trong he thong
     */
    public String getListOrganization(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        //Toan bo cac ham khi thuc hien deu phai chay qua ham nay tru ham login va get RSA key
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            String strAesKeyDecode = dataSessionGR.getStrAesKey();
            try {
                String decryptedData = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(decryptedData);
                LogUtils.logFunctionalStart(log);
                //Lay prams client gui len de thuc hien yeu cau
                HashMap<String, Object> hmParams = new HashMap<>();
                hmParams.put(ConstantsFieldParams.STR_PARENT_ID, Long.class);
                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);

                Long parentId = (Long) valueParams.get(ConstantsFieldParams.STR_PARENT_ID);

                //Thuc hien chen code lay du lieu va thao tac tra ve o day
                MeetingDAO meetingD = new MeetingDAO();
                List<EntityVhrOrg> listItem = meetingD.getListOrganization(parentId);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                        listItem, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_ERROR, dataSessionGR.getEnumErrCode(), e.getMessage(), strData, isSecurity);
                logger.error(e.getMessage(), e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            //cac truong hop loi say ra trong qua trinh thao tac
            //bao gom: mat session, khong ton tai doi tuong, session timeout
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }

        return FunctionCommon.getResultForClient(strResult);
    }

    /**
     * Lay danh sach don vi giao viec: lay chinh no va duoi no 1 cap
     */
    public String getListOrganizationAssign(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        //Toan bo cac ham khi thuc hien deu phai chay qua ham nay tru ham login va get RSA key
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            //sesion cua ca nhan dang nhap la hop ly
            try {
                //Lay prams client gui len de thuc hien yeu cau
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);
                HashMap<String, Object> hmParams = new HashMap<>();
                hmParams.put(ConstantsFieldParams.STR_IS_FIRST, String.class);
                hmParams.put(ConstantsFieldParams.STR_PARENT_ID, Long.class);
                hmParams.put(ConstantsFieldParams.STR_ASSIGN_ID, Long.class);
                hmParams.put(ConstantsFieldParams.ORG_ID, Long.class);
                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);

                String isFirst = (String) ((valueParams.get(ConstantsFieldParams.STR_IS_FIRST) != null) ? valueParams.get(ConstantsFieldParams.STR_IS_FIRST) : "1");
                Long parentId = (Long) valueParams.get(ConstantsFieldParams.STR_PARENT_ID);
                Long assignId = (Long) valueParams.get(ConstantsFieldParams.STR_ASSIGN_ID);
                Object objOrgAssignId = valueParams.get(ConstantsFieldParams.ORG_ID);
                Long orgAssignId = null;
                if (objOrgAssignId != null) {
                    orgAssignId = (Long) objOrgAssignId;
                }

                //kiem tra du lieu dau vao
                if ("0".equals(isFirst) && parentId == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                }

                List<Long> listOrgId = new ArrayList<>();
                List<Long> listConfigUserId = new ArrayList<>();
                List<Long> listAssistantOrgId = dataSessionGR.getVof2_ItemEntityUser().getListAssistantOrgIdForMission();
                List<Long> listManagementOrg = dataSessionGR.getVof2_ItemEntityUser().getListManagementOrg();
                List<EntityVhrOrg> listManagementVhrOrg = dataSessionGR.getVof2_ItemEntityUser().getListManagementVhrOrg();
                List<EntityVhrOrg> listAssistantVhrOrg = dataSessionGR.getVof2_ItemEntityUser().getListAssistantVhrOrg();
                List<EntityVhrOrg> listPoliticalAssistantOrg = dataSessionGR.getVof2_ItemEntityUser().getListPoliticalAssistantOrg();
                if (listAssistantVhrOrg == null) {
                    listAssistantVhrOrg = new ArrayList<>();
                }
                if (!CommonUtils.isEmpty(listPoliticalAssistantOrg)) {
                    listAssistantVhrOrg.addAll(listPoliticalAssistantOrg);
                }
                if (listManagementOrg != null && !listManagementOrg.isEmpty()) {
                    listOrgId.addAll(listManagementOrg);
                }
                if (listAssistantOrgId != null && !listAssistantOrgId.isEmpty()) {
                    for (Long obj : listAssistantOrgId) {
                        if (!listOrgId.contains(obj)) {
                            listOrgId.add(obj);
                        }
                    }
                }

                boolean selectOrg = false;
                if (orgAssignId != null) {
                    if (!listOrgId.contains(orgAssignId)) {
                        selectOrg = true;
                    }
                }

                if (!selectOrg) {
                    MissionDAO missionD = new MissionDAO();
                    List<EntityConfigUser> listConfigUser = (List<EntityConfigUser>) missionD.getConfigMissionByUser(assignId, 1);
                    if (listConfigUser != null && !listConfigUser.isEmpty()) {
                        for (EntityConfigUser obj : listConfigUser) {
                            listConfigUserId.add(obj.getSysOrgId());

                            if (!listOrgId.contains(obj.getSysOrgId())) {
                                listOrgId.add(obj.getSysOrgId());
                            }
                        }
                    }
                }

                List<Long> parentIds = new ArrayList<>();
                if ("1".equals(isFirst)) {
                    if (orgAssignId != null) {
                        parentIds.add(orgAssignId);
                    } else {
                        parentIds.addAll(listOrgId);
                    }
                } else {
                    parentIds.add(parentId);
                }
                //Thuc hien chen code lay du lieu va thao tac tra ve o day
                MeetingDAO meetingD = new MeetingDAO();
                List<EntityVhrOrg> listItem = meetingD.getListOrganizationAssign(parentId, userId, parentIds, isFirst);
                List<Long> listLongItem = new ArrayList<>();
                for (EntityVhrOrg entity : listItem) {
                    listLongItem.add(entity.getOrgId());
                    if (!parentIds.contains(entity.getOrgId())) {
                        if ("0".equals(entity.getIsRoot())) {
                            entity.setIsLeaf("0");
                        }
                    }
                }
                OrgDAO orgD = new OrgDAO();
                List<EntityVhrOrg> listItemConfig = orgD.getPathOrgIds(listConfigUserId, parentIds, isFirst);
                List<Long> listItemConfigId = new ArrayList<>();
                if (!CommonUtils.isEmpty(listLongItem) && !CommonUtils.isEmpty(listItemConfig)) {
                    for (EntityVhrOrg entity : listItemConfig) {
                        listItemConfigId.add(entity.getOrgId());
                        if (!listLongItem.contains(entity.getOrgId()) && !parentIds.contains(entity.getOrgId())) {
                            if (!listConfigUserId.contains(entity.getOrgId())) {
                                entity.setIsRoot("1");
                            }
                            listLongItem.add(entity.getOrgId());
                            listItem.add(entity);
                        }
                    }
                    for (EntityVhrOrg entity : listItem) {
                        // tuantm18 xoa nho tren may thanght6
                        if (listItemConfigId.contains(entity.getOrgId())) {
                            entity.setIsLeaf("1");
                        }
                    }
                }

                // Begin-TuanTM18: Neu chon don vi cha de giao viec thi chi chon don vi thuc hien la cac don vi duoc cau hinh la LD/TT/TL
                if (selectOrg) {
                    List<EntityVhrOrg> listResult = new ArrayList<>();
                    for (EntityVhrOrg entity : listItem) {
                        if (checkOrgPath(listManagementVhrOrg, listAssistantVhrOrg, entity.getOrgId()) || orgAssignId.equals(entity.getOrgId())) {
                            listResult.add(entity);
                        }
                    }
                    listItem = new ArrayList<>();
                    if (!CommonUtils.isEmpty(listResult)) {
                        listItem.addAll(listResult);
                    }
                }
                // End-TuanTM18: Neu chon don vi cha de giao viec thi chi chon don vi thuc hien la cac don vi duoc cau hinh la LD/TT/TL

                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, listItem, strAesKeyDecode);

                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_ERROR, dataSessionGR.getEnumErrCode(), e.getMessage(), strData, isSecurity);
                logger.error(e.getMessage(), e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            //cac truong hop loi say ra trong qua trinh thao tac
            //bao gom: mat session, khong ton tai doi tuong, session timeout
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }

        return FunctionCommon.getResultForClient(strResult);
    }

    /**
     * Kiem tra don vi co thuoc duong dan path cua cac don vi duoc cau hinh
     * LD/TT/TL cua nguoi dung
     *
     * @param listManagementVhrOrg
     * @param listAssistantVhrOrg
     * @param orgid
     * @return
     */
    private boolean checkOrgPath(List<EntityVhrOrg> listManagementVhrOrg, List<EntityVhrOrg> listAssistantVhrOrg, Long orgId) {
        if (orgId != null) {
            if (!CommonUtils.isEmpty(listManagementVhrOrg)) {
                for (EntityVhrOrg org : listManagementVhrOrg) {
                    if (org.getPath() != null && org.getPath().contains(orgId.toString())) {
                        return true;
                    }
                }
            }
            if (!CommonUtils.isEmpty(listAssistantVhrOrg)) {
                for (EntityVhrOrg org : listAssistantVhrOrg) {
                    if (org.getPath() != null && org.getPath().contains(orgId.toString())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Lay danh sach linh vuc
     */
    public String getListField(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        //Demo: Thuc hien goi sesion cua ca nhan dang nhap
        //Toan bo cac ham khi thuc hien deu phai chay qua ham nay tru ham login va get RSA key
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            //sesion cua ca nhan dang nhap la hop ly
            try {
                // Kiem tra user id tren he thong 2
                Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
                if (userId == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
                }

                // Giai ma du lieu neu ma hoa
                String aesKey;
                if (isSecurity != null && "1".equals(isSecurity)) {
                    // Lay AES Key
                    aesKey = dataSessionGR.getStrAesKey();
                    // Giai ma data client gui len
                    strData = SecurityControler.decodeDataByAes(aesKey, strData);
                }
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strData);
                LogUtils.logFunctionalStart(log);
                //Lay prams client gui len de thuc hien yeu cau             
                //System.out.println("luanvd-getListField-data: "+strData);
                // lay ma ngon ngu client can dung
                String langCode = dataSessionGR.getVof2_ItemEntityUser().getLanguageCode();// lay ngon ngu dang sai

                if (langCode == null) {
                    langCode = "vi";
                }
                // Lấy tham số client gửi lên
                JSONObject json = new JSONObject(strData);
                String[] keys = new String[]{ConstantsFieldParams.TYPE_DEL, ConstantsFieldParams.LANGUAGE, ConstantsFieldParams.TYPE_FIELD};
                List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                int typeDel = 0;
                if (listValue.get(0) != null && !"".equals(listValue.get(0))) {
                    typeDel = Integer.parseInt(listValue.get(0));
                }
                //Neu client co gui len ma ngon ngu thi tra theo ma ngon ngu cua client
                if (listValue.get(1) != null && !"".equals(listValue.get(1))) {
                    langCode = listValue.get(1);
                }
                int fieldType = 1;// fix 1: lay linh vuc trong nhiem vu/cong viec
                if (listValue.get(2) != null && !"".equals(listValue.get(2))) {
                    fieldType = Integer.parseInt(listValue.get(2));
                }
                //System.out.println("luanvd-langcode: "+langCode);
                //Thuc hien chen code lay du lieu va thao tac tra ve o day
                MeetingDAO meetingD = new MeetingDAO();
                List<EntityFields> listItem = meetingD.getListField(langCode, fieldType, typeDel);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                        listItem, dataSessionGR.getStrAesKey());
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_ERROR, dataSessionGR.getEnumErrCode(), e.getMessage(), strData, isSecurity);
                logger.error(e.getMessage(), e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            //cac truong hop loi say ra trong qua trinh thao tac
            //bao gom: mat session, khong ton tai doi tuong, session timeout
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }

        return FunctionCommon.getResultForClient(strResult);
    }

    /**
     * <b>Lenh insert vao bang mission</b><br>
     *
     * @param isSecurity
     * @param strData
     * @param request
     * @return
     */
    public String addMission(String isSecurity, String strData,
            HttpServletRequest request) {

        String[] keys = new String[] {
            "orgAssignId",
            "assignId",
            "missionPath",
            "isSend",
            "listMission",
            "isTransferOrgPerform",
            "weight",
            "frequenceUpdate",
            "approved",
            "missionParentId",
            "fieldId",
            "missionGroup",
            "listFileAttach",
            "listSourceMap",
            "sourceId",
            "sourceName",
            "sourceType",
            "objectType",
            "oldMissionId",
            // datdc add
            ConstantsFieldParams.MISSION_CLASS
        };
        String strResult;
        // Lay thong tin user trong memcached
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, strData, keys);
        String errorDesc = "addMission (Them nhiem vu) - username: " + userGroup.getCardId();
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            errorDesc += " - Session timeout!";
            logger.error(errorDesc);
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        String strDataClient = "";
        try {
            //Lay prams client gui len de thuc hien yeu cau
            String strAesKeyDecode = userGroup.getStrAesKey();
            strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
            List<String> listValue = userGroup.getListParamsFromClient();
            // Thuc hien khai bao cac params can lay
            // Id don vi giao viec
            Long orgAssignId = Long.parseLong(listValue.get(0));
            // Id nguoi giao viec
            Long assignId = Long.parseLong(listValue.get(1));
            // Id don vi theo doi
            Long orgTrackingId = null;
            // Id nguoi theo doi
            Long trackingId = null;
            // Id nguoi thuc hien
//            Long performId = null;
            // Id nguon goc
            Long sourceId = null;
            Long userId = userGroup.getUserId2();
            // Ngay ket thuc
            Date dateFinish = null;
            Long sortOrder = null;
            // Begin HaNH: Bo sung cho WEB - Ten nguon goc va Nhom nhiem vu
            String sourceName = null;
            //End HaNH
            String missionPath = listValue.get(2);
            // Thong tin co chuyen van ban hay khong
            // 0: khong chuyen; 1 hoac null: chuyen
            Long isSend = null;
            if (!CommonUtils.isEmpty(listValue.get(3))) {
                isSend = Long.parseLong(listValue.get(3));
            }
            Long percent = 0L;
            Long status = 1L;
            Long delFlag = 0L;
            Long completed = 0L;
            Long transferOrgPerform = 0L;
            if (!CommonUtils.isEmpty(listValue.get(5))) {
                transferOrgPerform = Long.parseLong(listValue.get(5));
            }            
            // Trong so nhiem vu
            Long weight = 1L;
             if (!CommonUtils.isEmpty(listValue.get(6))) {
                weight = Long.parseLong(listValue.get(6));
            }
            // Tan suat bao cao nhiem vu
            Long frequenceUpdate = 2L;
            if (!CommonUtils.isEmpty(listValue.get(7))) {
                frequenceUpdate = Long.parseLong(listValue.get(7));
            }
            Long approved = 1L;
            if (!CommonUtils.isEmpty(listValue.get(8))) {
                approved = Long.parseLong(listValue.get(8));
            }
            // ID nhiem vu cha
            Long missionParentId = null;
            if (!CommonUtils.isEmpty(listValue.get(9))) {
                missionParentId = Long.parseLong(listValue.get(9));
            }
            // ID linh vuc
            // ID ko bat buoc chon start
            Long fieldId = null;
            if (!CommonUtils.isEmpty(listValue.get(10)) && null != listValue.get(10)) {
                fieldId = Long.parseLong(listValue.get(10));
            }
            // ID ko bat buoc chon end
            // Nhom nhiem vu
            Integer missionGroup = 1;
            if (!CommonUtils.isEmpty(listValue.get(11))) {
                try {
                    missionGroup = Integer.parseInt(listValue.get(11));
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    missionGroup = 1;
                }
                
            }
            Long sourceType = 1L;
            Long objectType = 1L;
            //End HaNH
            Gson gson = new Gson();
            String strListMission = listValue.get(4);
            Type listMissionType = new TypeToken<ArrayList<EntityMissionChild>>() {
            }.getType();
            List<EntityMissionChild> listMission = gson.fromJson(strListMission, listMissionType);
            List<EntitySourceMap> listRefDoc;
            // Duyet tung nhiem vu moi
            for (EntityMissionChild mission : listMission) {
                // Neu nhiem vu co van ban tham chieu
                listRefDoc = mission.getListDocumentRef();
                if (!CommonUtils.isEmpty(listRefDoc)) {
                    // Gan lai ID nguon goc theo id van ban tham chieu
                    // Ten nguon goc theo ten van ban tham chieu
                    for (EntitySourceMap refDoc : listRefDoc) {
                        refDoc.setSourceId(refDoc.getDocumentId());
                        refDoc.setSourceName(refDoc.getDocumentName());
                    }
                }
                // Kiem tra xem don vi thuc hien co trung don vi giao khong
                for (String orgPerformId : mission.getListPerform()) {
                    if (orgPerformId.equals(orgAssignId.toString())) {
                        errorDesc += " - Loi don vi thuc hien trung don vi giao - orgPerformId: "
                                + orgPerformId + " - orgAssignId: " + orgAssignId;
                        logger.error(errorDesc);
                        return FunctionCommon.responseResult(ErrorCode.DUPLICATE_ASSIGN_ERROR, null, null);
                    }
                }
                // Nhiem vu co don vi phoi hop
                if (!CommonUtils.isEmpty(mission.getListCombination())) {
                    for (EntityCombination orgCombination : mission.getListCombination()) {
                        // Don vi thuc hien trung voi don vi phoi hop
                        if (mission.getListPerform().contains(orgCombination.getOrgCombinationId())) {
                            errorDesc += " - Loi don vi thuc hien trung don vi phoi hop - listPerform: "
                                    + mission.getListPerform() + " - orgCombinationId: "
                                    + orgCombination.getOrgCombinationId();
                            logger.error(errorDesc);
                            return FunctionCommon.responseResult(ErrorCode.DUPLICATE_PERFORM_COMBINATION_ERROR,
                                    null, null);
                        }
                    }
                }
                // Kiem tra thong tin nhiem vu
                if (CommonUtils.isEmpty(mission.getMissionName())
                        || CommonUtils.isEmpty(mission.getContent())
                        || CommonUtils.isEmpty(mission.getTarget())) {
                        errorDesc += " - Loi khong con thong tin ten nhiem vu hoac noi dung hoac muc tieu";
                        logger.error(errorDesc);
                        return FunctionCommon.responseResult(ErrorCode.INPUT_NOT_EMPTY, null, null);
                }
                if (mission.getMissionGroup() == null) {
                	mission.setMissionGroup(1);
                }
            }
            // Danh sach file dinh kem
            List<EntityFileAttachment> listAttachFile = new LinkedList<>();
            if (!CommonUtils.isEmpty(listValue.get(12))) {
                Type listFileAttachmentType = new TypeToken<ArrayList<EntityFileAttachment>>() {
                }.getType();
                listAttachFile = gson.fromJson(listValue.get(12), listFileAttachmentType);
            }
            // Danh sach nguon goc
            List<EntitySourceMap> listSourceMap = new LinkedList<>();
            // Co nhieu nguon goc duoc gop lai thanh 1 list
            if (!CommonUtils.isEmpty(listValue.get(13))) {
                Type listSourceMapType = new TypeToken<ArrayList<EntitySourceMap>>() {
                }.getType();
                listSourceMap = gson.fromJson(listValue.get(13), listSourceMapType);        
            } // Chi co 1 nguon goc cung cap voi thong tin nhiem vu
            else {
                // ID nguon goc
                if (!CommonUtils.isEmpty(listValue.get(14))) {
                    sourceId = Long.parseLong(listValue.get(14));
                }
                // Ten nguon goc
                sourceName = listValue.get(15);
                // Loai nguon goc
                if (!CommonUtils.isEmpty(listValue.get(16))) {
                    sourceType = Long.parseLong(listValue.get(16));
                }
                // Loai doi tuong
                if (!CommonUtils.isEmpty(listValue.get(17))) {
                    objectType = Long.parseLong(listValue.get(17));
                }
            }
            // oldMissionId la ID nhiem vu bi sao chep
            Long oldMissionId = null;
            if (!CommonUtils.isEmpty(listValue.get(18))) {
                oldMissionId = Long.parseLong(listValue.get(18));
            }
            // datdc them mission_class start
            String missionClass = null;
            if (!CommonUtils.isEmpty(listValue.get(19))) {
                missionClass = listValue.get(19);
            }
            //Thuc hien chen code lay du lieu va thao tac tra ve o day
            MeetingDAO meetingD = new MeetingDAO();
            Integer result = meetingD.addMission(missionParentId, sourceId, missionPath,
                    orgAssignId, assignId, orgTrackingId, trackingId, frequenceUpdate,
                    fieldId, percent, approved, status, userId, delFlag,
                    completed, weight, sortOrder, transferOrgPerform, dateFinish,
                    sourceName, sourceType, objectType, listMission,
                    missionGroup, listAttachFile, listSourceMap, userGroup.getCardId(),
                    oldMissionId, missionClass);
            // datdc them mission_class end
            if (result == 1) {
                //datnv5:
                if(listSourceMap != null && listSourceMap.size() > 0){
                    for (EntitySourceMap entitySourceMap : listSourceMap) {
                        if(entitySourceMap.getSourceType() == 2){
                            IndexDocumentByType indexDocumentByType 
                                    = new IndexDocumentByType(entitySourceMap.getSourceId());
                            ThreadPoolCommon.putRunnable(indexDocumentByType);
                        }
                    }
                }
                if(sourceType == 2){
                    IndexDocumentByType indexDocumentByType 
                                    = new IndexDocumentByType(sourceId);
                            ThreadPoolCommon.putRunnable(indexDocumentByType);
                }
                // Loai bo cac nhiem vu duoc tao dua vao phieu giao viec
                // va chi tieu san xuat kinh doanh vi day la cac nhiem vu dang cho
                // phai duoc ky xong phieu giao viec moi hien thi len
                for (Iterator<EntityMissionChild> iterator = listMission.iterator(); iterator.hasNext();) {
                    EntityMissionChild mission = iterator.next();
                    if (mission.getIsApprove() != null && mission.getIsApprove() == 1) {
                        iterator.remove();
                    }
                }
                if (CommonUtils.isEmpty(listMission)) {
                    return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
                }
                //100517 chuyen van ban tham chieu cho dau moi thuc hien cong viec
                //neu sua viec co nguon goc tu van ban
                sendDocRefToPersonInGroupMission(listMission, userGroup.getItemEntityUser(),
                        userGroup.getVof2_ItemEntityUser(), assignId, userId, orgAssignId);
                /* HaNH: Gui tin nhan cho don vi thuc hien, phoi hop */
                SmsDAO sms = new SmsDAO();
                StaffDAO staffDAO = new StaffDAO();
                VHROrgDAO org = new VHROrgDAO();
                UserDAO userDao = new UserDAO();
                //Lay thong tin nguoi giao nhiem vu
                List<Long> listAssignTmp = new ArrayList<>();
                listAssignTmp.add(orgAssignId);
                EntityVhrEmployee userAssign;
                List<EntityVhrEmployee> lstVhrEmployee = staffDAO.getLeaderByOrg(assignId, listAssignTmp);
                if (!CommonUtils.isEmpty(lstVhrEmployee)) {
                    userAssign = lstVhrEmployee.get(0);
                    List<Long> listTmpPerformId;
                    List<String> listPerform;
                    Long performId;
                    List<EntityVhrEmployee> listSmsReceiver;
                    EntityVhrEmployee performer;
                    CommonControler common = new CommonControler();
                    for (EntityMissionChild mission : listMission) {
                        listPerform = mission.getListPerform();
                        for (String perform : listPerform) {
                            performId = Long.parseLong(perform);
                            listTmpPerformId = new ArrayList<>();
                            listTmpPerformId.add(performId);
                            // Nhiem vu chinh tri chi gui tin nhan cho tro ly chinh tri trong don vi
                            if (mission.getMissionGroup() != null && mission.getMissionGroup() == 3) {
                                listSmsReceiver = staffDAO.getListPoliticalAssistant(null, listTmpPerformId);
                            } else {
                                // Lay danh sach thu truong, lanh dao tro ly cua don vi thuc hien
                                listSmsReceiver = staffDAO.getLeaderAndAssistantByOrg(null, listTmpPerformId);
                                // Lay danh sach cac tro ly chuyen huong cua don vi thuc hien theo don vi giao de gui sms
                                List<EntityVhrEmployee> listAssistantIds = userDao.getUserAssistantInfo(orgAssignId, performId);
                                if (!CommonUtils.isEmpty(listAssistantIds)) {
                                    listSmsReceiver.addAll(listAssistantIds);
                                }
                            }
                            // Neu nhiem vu co dau moi thuc hien thi lay thong tin ca nhan
                            if (mission.getPerformId() != null) {
                                performer = userDao.getEmployeeById(mission.getPerformId());
                                listSmsReceiver.add(performer);
                            }                            
                            // Begin: Khoi tao danh sach cac doi tuong can dien vao mau cu phap tin nhan
                            // List gom 4 phan tu: Chuc vu nguoi giao, ten nguoi giao, ten viet tat don vi thuc hien, ten nhiem vu
                            List<String> listObjectSendMess1 = new ArrayList<>();
                            listObjectSendMess1.add(userAssign.getPosition());
                            //050417 sua gui tin nhan lay fullName thay displayName
                            listObjectSendMess1.add(userAssign.getFullName());
                            // Lay ten viet tat cua don vi thuc hien
                            EntityVhrOrg orgPerform = org.getVHROrg(performId);
                            listObjectSendMess1.add(orgPerform.getAbbreviation());
                            listObjectSendMess1.add(mission.getMissionName());
                            //End
                            if (CommonUtils.isEmpty(listSmsReceiver)) {
                                logger.error("Khong co ca nhan thoa man dieu kien gui tin nhan!");
                            } else {
                                for (EntityVhrEmployee person : listSmsReceiver) {
                                    String phone = person.getMobilePhone();
                                    //Check dieu kien so dien thoai nguoi nhan khong nam trong Black List
//                                    logger.error("Chuan bi Gui smm :" + person.getEmployeeId() + " phone:" + phone);
                                    if (phone != null && sms.isNotInBlackList(phone)) {
//                                        logger.error("Gui smm :" + person.getEmployeeId() + " phone:" + phone);
                                        //Chen cac thong tin vao mau noi dung tin nhan
                                        String content = common.getStrMessConfig(listObjectSendMess1, 4L, 1L, person.getEmployeeId());
                                        if (!sms.addMsgToSmsMaster(phone, content, userId, userId, person.getEmployeeId(), 5L, -1L,
                                                Constants.SMS_TEXT_INTERCEPT.LEADER_MESSGIVEMISSION)) {
                                            logger.error("Loi! Khong insert duoc tin nhan vao DataBase");
                                        }
                                    }
                                }
                            }
                            //Lay danh sach ID don vi phoi hop

                            List<EntityCombination> listCombinateOrg = mission.getListCombination();                            
                            //Khoi tao danh sach tham so chen vao noi dung tin nhan
                            //List listObjectSendMess2Final gom 5 phan tu: Chuc vu nguoi giao, ten nguoi giao, ma don vi phoi hop, ma don vi thuc hien, ten nhiem vu
                            List<String> listObjectSendMess2 = new ArrayList<>();
                            List<String> listObjectSendMess2Tmp = new ArrayList<>();
                            List<String> listObjectSendMess2Final = new ArrayList<>();
                            listObjectSendMess2.add(userAssign.getPosition());
                            //050417 sua gui tin nhan lay fullName thay displayName
                            listObjectSendMess2.add(userAssign.getFullName());
                            //listObjectSendMess2.add(orgPerform.getAbbreviation());
                            Long combinationOrgId;
                            List<Long> listCombinationOrgId;
                            EntityVhrOrg combinationOrg;
                            for (EntityCombination obj : listCombinateOrg) {
                                combinationOrgId = Long.parseLong(obj.getOrgCombinationId());
                                listCombinationOrgId = new ArrayList<>();
                                listCombinationOrgId.add(combinationOrgId);
                                // Nhiem vu chinh tri -> Gui tin nhan cho tro ly chinh tri don vi phoi hop
                                if (mission.getMissionGroup() != null && mission.getMissionGroup() == 3) {
                                    listSmsReceiver = staffDAO.getListPoliticalAssistant(null, listCombinationOrgId);
                                } // Gui tin nhan cho thu truong, lanh dao, tro ly cua don vi phoi hop
                                else {
                                    listSmsReceiver = staffDAO.getLeaderAndAssistantByOrg(null, listCombinationOrgId);
                                }
                                combinationOrg = org.getVHROrg(combinationOrgId);
                                listObjectSendMess2Tmp.clear();
                                listObjectSendMess2Final.clear();
                                listObjectSendMess2Tmp.add(combinationOrg.getAbbreviation());
                                listObjectSendMess2Tmp.add(orgPerform.getAbbreviation());
                                listObjectSendMess2Tmp.add(mission.getMissionName());
                                listObjectSendMess2Final.addAll(listObjectSendMess2);
                                listObjectSendMess2Final.addAll(listObjectSendMess2Tmp);
                                if (CommonUtils.isEmpty(listSmsReceiver)) {
                                    logger.error("Khong co ca nhan thoa man dieu kien gui tin nhan!");
                                } else {
                                    for (EntityVhrEmployee person : listSmsReceiver) {
                                        String phone = person.getMobilePhone();
                                        //Check dieu kien so dien thoai nguoi nhan khong nam trong Black List
                                        if (phone != null && sms.isNotInBlackList(phone)) {
                                            //Chen cac thong tin vao mau noi dung tin nhan
                                            String content = common.getStrMessConfig(listObjectSendMess2Final, 4L, 2L, person.getEmployeeId());
                                            if (!sms.addMsgToSmsMaster(phone, content, userId, userId, person.getEmployeeId(), 5L, -1L,
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
                //Xu ly neu tao nhiem vu co nguon goc tu van ban tren Mobile
                if (CommonUtils.isEmpty(listSourceMap)) {
                    //<editor-fold defaultstate="collapsed" desc="Xu ly neu tao nhiem vu co nguon goc tu van bản thì chuyển văn bản đó sang trạng thái đã xử lý">
                    if (sourceType == 2L && objectType == 2L) {
                        if (isSend == null || isSend == 1L) {
                            //100517 chuyen van ban cho dau moi thuc hien cong viec
                            sendDocToPersonInGroupMission(sourceId, listMission,
                                    userGroup.getItemEntityUser(), userGroup.getVof2_ItemEntityUser(),
                                    assignId, userId, orgAssignId);
                        }
                        // Lay userId tren he thong 1
                        Long userIdVof1 = null;
                        DocumentDAO doc = new DocumentDAO();

                        // <b> kiem tra xem van ban co gui sms cho tro lys khong?</b>
                        // outsource sonnd
                        // begin
                        SmsDAO smsDao = new SmsDAO();
                        Long employeeId = 0L;
                        Long leaderId = userGroup.getVof2_ItemEntityUser().getUserId();
                        List<EntityDocumentInStaff> lstStaff = smsDao.getDocumentStaff(leaderId, sourceId, 0);
                        boolean isSendSms;
                        if (lstStaff != null && !CommonUtils.isEmpty(lstStaff)) {
                            employeeId = lstStaff.get(0).getStaffId2();
                        } else {
//                                    isSendSms = false;
                        }
                        if (employeeId.equals(0L)) {
                            isSendSms = false;
                        } else if (smsDao.checkDocumentSendByAssistant(employeeId, leaderId, sourceId) != 1) {
                            isSendSms = false;
//                                } else if ((smsDao.checkAssistantOfLeader(employeeId, leaderId) == 1)) {
//                                    isSendSms = true;
                        } else {
                            isSendSms = true;
                        }
                        // end.
                        //Cap nhat trang thai xu ly
                        // <b> sau khi cap nhat thanh cong, se thuc hien gui sms </b>
                        // author: OS/sonnd
                        // begin
                        if (doc.updateDocumentProcessing(sourceId, userIdVof1, userId, null) == 1) {
                            EntityDocument documentEntity = smsDao.getDocument(sourceId);
                            List<Long> lstPerFormId = new ArrayList();
                            String missionName = "";
                            for (EntityMissionChild mission : listMission) {
                                List<String> listPerform = mission.getListPerform();
                                if (listPerform != null && listPerform.size() > 0) {
                                    for (int p = 0; p < listPerform.size(); p++) {
                                        Long tempPerformId = Long.parseLong(listPerform.get(p));
                                        lstPerFormId.add(tempPerformId);
                                    }
                                }
                                if (mission.getMissionName() != null) {
                                    missionName = mission.getMissionName();
                                }
                            }
                            if (!lstPerFormId.isEmpty()) {
                                List<EntityVhrOrg> lstVhrOrg = smsDao.getListVHROrgToSendSms(lstPerFormId);
                                if (lstVhrOrg != null && !CommonUtils.isEmpty(lstVhrOrg) && isSendSms && documentEntity != null) {
                                    String nameLeader = userGroup.getVof2_ItemEntityUser().getAliasName();
                                    String nameDoc = documentEntity.getTitle();
                                    smsDao.sendSmsAssistantDocument(null, null, lstVhrOrg, null, employeeId, missionName, nameLeader, nameDoc,
                                            Constants.SMS_TEXT_INTERCEPT.SECRETARY_LEADERHANDLEDOC);

                                }
                            }
                        }
                        // end cap nhat va gui tin nhan.
                    }
                } //HaNH: Xu ly neu tao nhiem vu co nguon goc tu van ban tren WEB
                //phan nay tam thoi pending cho giai phap, chi xu ly update document_in_staff
                else {
//                            //<editor-fold defaultstate="collapsed" desc="Xu ly neu tao nhiem vu co nguon goc tu van bản thì chuyển văn bản đó sang trạng thái đã xử lý">
                    for (EntitySourceMap objSource : listSourceMap) {
                        if (objSource.getSourceType() == 2 && objSource.getObjectType() == 2) {
//                                    // Lay userId tren he thong 1
                            Long userIdVof1 = null;
                            if (userGroup.getItemEntityUser() != null) {
                                userIdVof1 = userGroup.getItemEntityUser().getUserId();
                            }
                            if (userIdVof1 != null && userId == 0L) {
                                userIdVof1 = null;
                            }
                            DocumentDAO doc = new DocumentDAO();
                            doc.updateDocumentProcessing(objSource.getSourceId(), userIdVof1, userId, null);
                            //100517 chuyen van ban cho TT, LD, VT don vi
                            isSend = objSource.getIsSend();
                            if (isSend == null || isSend == 1L) {
                                sendDocToPersonInGroupMission(objSource.getSourceId(), listMission,
                                        userGroup.getItemEntityUser(), userGroup.getVof2_ItemEntityUser(),
                                        assignId, userId, orgAssignId);
                            }
//                                    // <b> kiem tra xem van ban co gui sms cho tro lys khong?</b>
//                                    // outsource sonnd
//                                    // begin
//                                    SmsDAO smsDao = new SmsDAO();
//                                    Long employeeId = 0L;
//                                    Long leaderId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
//                                    List<EntityDocumentInStaff> lstStaff = smsDao.getDocumentStaff(leaderId, objSource.getSourceId(), 0);
//                                    boolean isSendSms = false;
//                                    if (lstStaff != null && !CommonUtils.isEmpty(lstStaff)) {
//                                        employeeId = lstStaff.get(0).getStaffId2();
//                                    } else {
//                                        isSendSms = false;                                
//                                    }
//                                    if (employeeId.equals(0L)) {
//                                        isSendSms = false;
//                                    } else if (smsDao.checkDocumentSendByAssistant(employeeId, leaderId, objSource.getSourceId()) != 1) {
//                                        isSendSms = false;
//                                    } else if ((smsDao.checkAssistantOfLeader(employeeId, leaderId) == 1)) {
//                                        isSendSms = true;
//                                    } else {
//                                        isSendSms = false;
//                                    }
//                                    // end.
//                                    //Cap nhat trang thai xu ly
//                                    // <b> sau khi cap nhat thanh cong, se thuc hien gui sms </b>
//                                    // author: OS/sonnd
//                                    // begin
//                                    if (doc.updateDocumentProcessing(objSource.getSourceId(), userIdVof1, userId, null) == 1) {
//                                        EntityDocument documentEntity = smsDao.getDocument(objSource.getSourceId());
//                                        List<Long> lstPerFormId = new ArrayList();
//                                        String missionName = "";
//                                        for (EntityMissionChild mission : listMission) {
//                                            List<String> listPerform = mission.getListPerform();
//                                            if (listPerform != null && listPerform.size() > 0) {
//                                                for (int p = 0; p < listPerform.size(); p++) {
//                                                    Long tempPerformId = Long.parseLong(listPerform.get(p));
//                                                    lstPerFormId.add(tempPerformId);
//                                                }
//                                            }
//                                            if (mission.getMissionName() != null) {
//                                                missionName = mission.getMissionName();
//                                            }
//                                        }
//                                        if (!lstPerFormId.isEmpty()) {
//                                            List<EntityVhrOrg> lstVhrOrg = smsDao.getListVHROrgToSendSms(lstPerFormId);
//                                            if (lstVhrOrg != null && !CommonUtils.isEmpty(lstVhrOrg) && isSendSms && documentEntity != null) {
//                                                String nameLeader = dataSessionGR.getVof2_ItemEntityUser().getAliasName();
//                                                String nameDoc = documentEntity.getTitle();
//                                                smsDao.sendSmsAssistantDocument(null, null, lstVhrOrg, null, employeeId, missionName, nameLeader, nameDoc);
//
//                                            }
//                                        }
//                                    }
//                                    // end cap nhat va gui tin nhan.
                        }
                    }
                }
                //</editor-fold>
            }
            strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
        } catch (Exception e) {
            logger.error("addMission - Exception - username: " + userGroup.getCardId()
                    + "\ndata: " + strDataClient, e);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
        // Ghi log ket thuc chuc nang
        return FunctionCommon.getResultForClient(strResult);
    }
    
    /**
     * Chuyen don vi thuc hien nhiem vu
     *
     * @author cuongnv
     * @param isSecurity
     * @param data
     * @param request
     * @return
     */
    public String forwardMission(String isSecurity, String data,
            HttpServletRequest request) {

        String[] keys = new String[]{
            ConstantsFieldParams.MISSION_ID,
            ConstantsFieldParams.LIST_PERFORM,
            // Them request reason
            ConstantsFieldParams.REQ_REASON
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request,
                data, keys);
        String response;
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.returnResultAfterLogSessionTimeout(userGroup);
        }
        try {
            // Parse du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            Long missionId = Long.parseLong(listValue.get(0));
            JSONArray arrJson = new JSONArray(listValue.get(1));
            // Them ly do chuyen start
            String reason = listValue.get(2);
            // Them ly do chuyen end
            if (arrJson.length() == 0) {
                response = FunctionCommon.responseResult(ErrorCode.ERR_NODATA,
                        null, null);
                return FunctionCommon.returnResultAfterLog(response,
                        userGroup.getUserId2(), "Khong co don vi thuc hien");
            }
            // Lay thong tin nhiem vu
            Boolean isDuplicate = false;
            MissionDAO missionDAO = new MissionDAO();
            EntityMission eMission = missionDAO.getMission(missionId);
            Long orgPerformIdOld = eMission.getOrgPerformId();
            Long performId = eMission.getPerformId();
            
            List<Long> listPerform = new ArrayList<>();
            Long orgPerformId;
            for (int i = 0; i < arrJson.length(); i++) {
                orgPerformId = Long.parseLong(arrJson.get(i).toString());
                if (orgPerformId.equals(orgPerformIdOld)) {
                    isDuplicate = true;
                    break;
                }
                listPerform.add(orgPerformId);
            }
            if (isDuplicate) {
                response = FunctionCommon.responseResult(ErrorCode.DUPLICATE_PERFORM_ERROR,
                        null, null);
                return FunctionCommon.returnResultAfterLog(response,
                        userGroup.getUserId2(), "Don vi thuc hien moi trung voi don vi cu");
            }
            MeetingDAO dAO = new MeetingDAO();
            //Gui tin nhan cho don vi moi va don vi cu khi chuyen thanh cong
            int result = dAO.forwardMission(missionId, listPerform, userGroup.getUserId2(),reason);
            if (result == 1) {
                //100517 chuyen van ban cho TT, LD, VT don vi
                //neu sua viec co nguon goc tu van ban
                SourceMapDAO sourceMapDAO = new SourceMapDAO();
                //chuyen van ban nguon goc
                List<EntitySourceMap> listSourceMap = sourceMapDAO.getListSourceByObject(missionId, 2, false);
                if (!CommonUtils.isEmpty(listSourceMap)) {
                    sendDocToPersonInGroupMission(listSourceMap, userGroup.getItemEntityUser(),
                            userGroup.getVof2_ItemEntityUser(), listPerform, null, null, 2, null, null);
                }
                // Chuyen van ban tham chieu
                listSourceMap = sourceMapDAO.getListSourceByObject(missionId, 6, false);
                if (!CommonUtils.isEmpty(listSourceMap)) {
                    sendDocToPersonInGroupMission(listSourceMap, userGroup.getItemEntityUser(),
                            userGroup.getVof2_ItemEntityUser(), listPerform, null, null, 6, null, null);
                }
                // Lay bao cao
                MissionDAO m = new MissionDAO();
                listSourceMap = m.getListDocReport(missionId);
                if (!CommonUtils.isEmpty(listSourceMap)) {
                    sendDocToPersonInGroupMission(listSourceMap, userGroup.getItemEntityUser(),
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
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            return FunctionCommon.returnResultAfterLog(userGroup, ex);
        }
    }
    
    /**
     * Lấy nguồn gốc công việc 1: Theo nguồn gốc khác 2: Theo văn bản 3: Theo
     * biên bản họp 4: Theo đề xuất của phòng kế hoạch 5: Theo định hướng
     *
     */
    public String getSourceMapMission(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        //Toan bo cac ham khi thuc hien deu phai chay qua ham nay tru ham login va get RSA key
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //Lay prams client gui len de thuc hien yeu cau
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String decryptedData = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(decryptedData);
                LogUtils.logFunctionalStart(log);
                HashMap<String, Object> hmParams = new HashMap<>();
                hmParams.put(ConstantsFieldParams.STR_SOURCE_MAP, Long.class);
                hmParams.put(ConstantsFieldParams.STR_OBJECT_ID, Long.class);
                hmParams.put(ConstantsFieldParams.STR_SOURCE_TYPE, Long.class);
                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);

                Long sourceMapId = (Long) valueParams.get(ConstantsFieldParams.STR_SOURCE_MAP);
                Long objectId = (Long) valueParams.get(ConstantsFieldParams.STR_OBJECT_ID);
                Long sourceType = (Long) valueParams.get(ConstantsFieldParams.STR_SOURCE_TYPE);

                if (sourceMapId == null || objectId == null || sourceType == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                }

                //Thuc hien chen code lay du lieu va thao tac tra ve o day
                MeetingDAO meetingD = new MeetingDAO();

                if (sourceType.equals(1L) || sourceType.equals(4L)) {
                    EntitySourceMap sourceItem = (EntitySourceMap) meetingD.getSourceMapMission(sourceMapId, objectId, 
                            sourceType, dataSessionGR);

                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, sourceItem, strAesKeyDecode);
                } else if (sourceType.equals(3L)) {
                    EntitySourceTypeMeeting sourceItem = (EntitySourceTypeMeeting) meetingD.getSourceMapMission(sourceMapId, 
                            objectId, sourceType, dataSessionGR);

                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, sourceItem, strAesKeyDecode);
                } else if (sourceType.equals(5L)) {
                    EntitySourceTypeOrientation sourceItem = (EntitySourceTypeOrientation) meetingD.getSourceMapMission(sourceMapId, 
                            objectId, sourceType, dataSessionGR);

                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, sourceItem, strAesKeyDecode);
                } else if (sourceType.equals(2L)) {
                    EntityDetailDocument sourceItem = (EntityDetailDocument) meetingD.getSourceMapMission(sourceMapId, 
                            objectId, sourceType, dataSessionGR);

                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, sourceItem, strAesKeyDecode);
                } else {
                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, null, null);
                }
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_ERROR, dataSessionGR.getEnumErrCode(), e.getMessage(), strData, isSecurity);
                logger.error(e.getMessage(), e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            //cac truong hop loi say ra trong qua trinh thao tac
            //bao gom: mat session, khong ton tai doi tuong, session timeout
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }

        return FunctionCommon.getResultForClient(strResult);
    }

    /**
     * <b>Lay danh sach nguon goc cua bien ban</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListSource(HttpServletRequest request,
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
            String[] keys = new String[] {
                    ConstantsFieldParams.MEETING_MINUTES_ID,
                    ConstantsFieldParams.CMT_OBJECT_TYPE,
                    "isHidden"};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long meetingMinutesId = Long.parseLong(listValue.get(0));
            //Begin::Cuongnv::3/2/2017
            Integer objectType = Constants.SourceMap.ObjectType.MEETING_MINUTE;
            if (CommonUtils.isInteger(listValue.get(1))) {
                objectType = Integer.parseInt(listValue.get(1));
            }
            boolean isHidden = "1".equals(listValue.get(2));
            //End
            SourceMapDAO sourceMapDAO = new SourceMapDAO();
            List<EntitySourceMap> result = sourceMapDAO.getListSourceByObject(meetingMinutesId, objectType, isHidden);
            // Neu danh sach nguon goc null
            // -> Tra ve thong bao loi
            if (result == null) {
                logger.error("getListSource - result null");
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            }
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_ERROR, ErrorCode.INPUT_INVALID, ex.getMessage(), data, isSecurity);
            logger.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
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
    public String getDetailMeetingResource(HttpServletRequest request,
            String data, String isSecurity) {
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
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{"meetingResourceId"};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long meetingResourceId = Long.parseLong(listValue.get(0));
            MeetingWeekDAO m = new MeetingWeekDAO();
            EntityMeetingResource result = m.getDetailMeetingResource(meetingResourceId);
            if (result == null) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            }
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);

        } catch (JSONException | NumberFormatException ex) {
            LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_ERROR, ErrorCode.INPUT_INVALID, ex.getMessage(), data, isSecurity);
            logger.error("getListSource - Exception:", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * <b>Xoa bien ban hop</b>
     *
     * @author SonDN
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String deleteMeetingMinutes(HttpServletRequest request,
            String data, String isSecurity) {
        EntityUserGroup entityUserGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!entityUserGroup.getCheckSessionOk()) {
            logger.error("deleteMeetingMinutes - No session");
            return FunctionCommon.generateResponseJSON(entityUserGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = entityUserGroup.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            logger.error("deleteMeetingMinutes - userId2 null");
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
            String[] keys = new String[]{ConstantsFieldParams.MEETING_MINUTES_ID};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long meetingMinutesId = Long.parseLong(listValue.get(0));
            MeetingDAO m = new MeetingDAO();
            Integer result = m.deleteMeetingMinutes(meetingMinutesId, userId);
            if (result == 1) {
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
            }
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);

        } catch (JSONException | NumberFormatException ex) {
            LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_ERROR, ErrorCode.INPUT_INVALID, ex.getMessage(), data, isSecurity);
            logger.error("deleteMeetingMinutes - Exception:" + ex.getMessage());
            logger.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
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
    public String addOrEditMeetingMinutes(HttpServletRequest request,
            String data, String isSecurity) {
        EntityUserGroup entityUserGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!entityUserGroup.getCheckSessionOk()) {
            logger.error("addOrEditMeetingMinutes - No session");
            return FunctionCommon.generateResponseJSON(entityUserGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = entityUserGroup.getVof2_ItemEntityUser().getUserId();
        String cardId = entityUserGroup.getVof2_ItemEntityUser().getStrCardNumber();
        Gson gson = new Gson();
        if (userId == null) {
            logger.error("addOrEditMeetingMinutes - userId2 null");
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
                ConstantsFieldParams.MEETING_MINUTES_ID,
                ConstantsFieldParams.CODE,
                ConstantsFieldParams.TITLE,
                ConstantsFieldParams.MISSION_TARGET,
                ConstantsFieldParams.CONCLUSION_DATE,
                ConstantsFieldParams.CONCLUSION_ID,
                ConstantsFieldParams.ORG_CONCLUSION_ID,
                ConstantsFieldParams.TYPE
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long meetingMinutesId = null;
            try {
                meetingMinutesId = Long.parseLong(listValue.get(0));
            } catch (NumberFormatException ex) {
            }
            String code = listValue.get(1);
            String title = listValue.get(2);
            String target = listValue.get(3);
            String conclusionDate = listValue.get(4);
            Long conclusionId = Long.parseLong(listValue.get(5));
            Long orgConclusionId = Long.parseLong(listValue.get(6));
            Integer type = Integer.parseInt(listValue.get(7));
            //Lay danh sach file dinh kem
            List<EntityFileAttachment> lstFileAttachment = new ArrayList<>();
            if (json.has(ConstantsFieldParams.REQ_LST_FILE_ATTACHMENT)) {
                JSONArray arrFiles = json.getJSONArray(ConstantsFieldParams.REQ_LST_FILE_ATTACHMENT);
                if (arrFiles.length() > 0) {
                    EntityFileAttachment tmp;
                    for (int i = 0; i < arrFiles.length(); i++) {
                        try {
                            JSONObject innerObj = arrFiles.getJSONObject(i);
                            tmp = gson.fromJson(innerObj.toString(), EntityFileAttachment.class);
                            lstFileAttachment.add(tmp);
                        } catch (JSONException ex) {
                            logger.error("addOrEditMeetingMinutes:lstFileAttachment - Exception:", ex);
                        }
                    }
                }
            }
            //Lay danh sach nguon goc van ban
            List<EntitySourceMap> listSourceMap = new ArrayList<>();
            if (json.has(ConstantsFieldParams.LIST_SOURCE_MAP)) {
                JSONArray arrSourceMap = json.getJSONArray(ConstantsFieldParams.LIST_SOURCE_MAP);
                if (arrSourceMap.length() > 0) {
                    EntitySourceMap tmp;
                    for (int i = 0; i < arrSourceMap.length(); i++) {

                        try {
                            JSONObject innerObj = arrSourceMap.getJSONObject(i);
                            tmp = gson.fromJson(innerObj.toString(), EntitySourceMap.class);
                            listSourceMap.add(tmp);
                        } catch (JSONException ex) {
                            logger.error("addOrEditMeetingMinutes:listSourceMap - Exception:", ex);
                        }
                    }
                }
            }
            MeetingDAO m = new MeetingDAO();
            if (meetingMinutesId == null) {
                //Thêm mới biên bản họp
                Integer rs = m.addMeetingMinutes(code, title, target, conclusionDate, conclusionId,
                        orgConclusionId, type, lstFileAttachment, listSourceMap, userId, cardId);
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, rs, aesKey);
            } else {
                //Sửa biên bản họp
                Integer rs = m.editMeetingMinutes(meetingMinutesId, code, title, target, conclusionDate,
                        conclusionId, orgConclusionId, type, lstFileAttachment, listSourceMap, userId, cardId);
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, rs, aesKey);
            }
        } catch (JSONException | NumberFormatException ex) {
            LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_ERROR, ErrorCode.INPUT_INVALID, ex.getMessage(), data, isSecurity);
            logger.error("addOrEditMeetingMinutes - Exception:", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * <b>Lay danh sach don vi thuc hien dung tren web</b>
     *
     * @author cuongnv
     * @param isSecurity
     * @param data
     * @param request
     * @return
     * @since 7/12/2016
     */
    public String getListOrganizationExecute(String isSecurity, String data,
            HttpServletRequest request) {
        
        String[] keys = new String[] {
            ConstantsFieldParams.STR_PARENT_ID,
            ConstantsFieldParams.STR_ASSIGN_ID,
            ConstantsFieldParams.MISSION_TYPE,
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.returnResultAfterLogSessionTimeout(userGroup);
        }
        // Khong co user tren he thong 2
        if (!userGroup.checkUserId2()) {
            return FunctionCommon.returnResultAfterLogNoInfo(userGroup.getUserId2());
        }
        // Lay danh sach don vi user co vai tro thu truong/lanh dao/tro ly/tro ly chinh tri
        List<Long> listSysOrgId = userGroup.getVof2_ItemEntityUser().getListOrgWhichHasThePermissionToMission();
        if (CommonUtils.isEmpty(listSysOrgId)) {
            return FunctionCommon.returnResultAfterLogNotAllow(userGroup.getUserId2());
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            Long parentId = null;
            if (!CommonUtils.isEmpty(listValue.get(0))) {
                parentId = Long.parseLong(listValue.get(0));
            }
            Long assignId = null;
            if (!CommonUtils.isEmpty(listValue.get(1))) {
                assignId = Long.parseLong(listValue.get(1));
            }
            Integer type = null;
            if (!CommonUtils.isEmpty(listValue.get(2))) {
                type = Integer.parseInt(listValue.get(2));
            }
            // Kiem tra xem don vi giao dang chon co phai la don vi cha 
            // va user do khong phai la LD, TT, TL cua don vi nay
            Boolean isParent = true;
            for (Long obj : listSysOrgId) {
                if (obj.equals(parentId)) {
                    isParent = false;
                    break;
                }
            }
            MissionDAO missionD = new MissionDAO();
            List<EntityConfigUser> listConfigUser = (List<EntityConfigUser>)
                    missionD.getConfigMissionByUser(assignId, 1);
            List<Long> listConfigUserId = new ArrayList<>();
            if (listConfigUser != null && !listConfigUser.isEmpty()) {
                for (EntityConfigUser obj : listConfigUser) {
                    listConfigUserId.add(obj.getSysOrgId());
                }
            }
            // Thuc hien chen code lay du lieu va thao tac tra ve o day
            MeetingDAO meetingD = new MeetingDAO();
            Object listItem = meetingD.getListOrganizationExecute(listConfigUserId,
                    parentId, isParent, listSysOrgId, type);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, listItem, userGroup);
        } catch (Exception e) {
            return FunctionCommon.returnResultAfterLog(userGroup, e);
        }
    }

    /**
     * <b>Lay danh sach don vi giao viec dung tren web</b>
     * 
     * @param isSecurity
     * @param data
     * @param request
     * @return 
     */
    public String getListOrganizationsAssign(String isSecurity, String data, HttpServletRequest request) {
        
        String response;
        String[] keys = new String[]{ConstantsFieldParams.IS_ORG_BY_ME};
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.returnResultAfterLogSessionTimeout(userGroup);
        }
        // Kiem tra user id tren he thong 2
        if (!userGroup.checkUserId2()) {
            return FunctionCommon.returnResultAfterLogNoInfo(userGroup.getUserId2());
        }
        // Lay danh sach don vi ma user co vai tro
        // + Thu truong/lanh dao
        // + Tro ly/Tro ly chinh tri
        List<Long> listSysOrgId = userGroup.getVof2_ItemEntityUser()
                .getListOrgWhichHasThePermissionToMission();
        if (listSysOrgId.isEmpty()) {
            response = FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
            return FunctionCommon.returnResultAfterLog(response, userGroup.getUserId2(),
                    "Khong co vai tro thu truong/lanh dao/tro ly/tro ly chinh tri");
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            //SonDN : lay danh sach don vi ma user dang nhap la TR/LD/TL hoac lay ca tren 1 cap
            Integer isOrgByMe = 0;
            try {
            	if (!CommonUtils.isEmpty(listValue.get(0))) {
            		isOrgByMe = Integer.parseInt(listValue.get(0));
            	}
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            MeetingDAO meetingDao = new MeetingDAO();
            List<EntityVhrOrg> listOrgAssign = meetingDao.getListOrganizationsAssign(
                    userGroup.getUserId2(), listSysOrgId, isOrgByMe);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, listOrgAssign, userGroup);
        } catch (Exception e) {
            return FunctionCommon.returnResultAfterLog(userGroup, e);
        }
    }

    /**
     * <b>Lay danh sach don vi phoi hop trong tim kiem nang cao dung tren
     * web</b>
     *
     * @author HaNH
     * @param isSecurity
     * @param strData
     * @param req
     * @return
     */
    public String getListOrgPerformInAdvanceSearch(String isSecurity, String data, HttpServletRequest request) {
        String strResult;
        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup entityUserGroup = FunctionCommon.getStatusSession(request);
        if (!entityUserGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(entityUserGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long sysUserId = entityUserGroup.getVof2_ItemEntityUser().getUserId();
        if (sysUserId == null) {
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        //Danh sach chua ID don vi user duoc cau hinh gan giao viec
        // hoac cong them danh sach tat ca don vi cua user (neu chua chon don vi giao viec)
        List<Long> listSysOrgId = new ArrayList<>();
        // Lay danh sach tat ca don vi ma user co vai tro TT, LD, TL
        List<Long> listUserOrgId = new ArrayList<>();
        List<Long> listManagementOrgId = entityUserGroup.getVof2_ItemEntityUser().getListManagementOrg();
        if (listManagementOrgId != null && !listManagementOrgId.isEmpty()) {
            listUserOrgId.addAll(listManagementOrgId);
        }

        List<Long> listAssistantOrgId = entityUserGroup.getVof2_ItemEntityUser().getListAssistantOrg();
        if (listAssistantOrgId != null && !listAssistantOrgId.isEmpty()) {
            listUserOrgId.addAll(listAssistantOrgId);
        }

//        if (listUserOrgId != null && !listUserOrgId.isEmpty()) {
//            listSysOrgId.addAll(listUserOrgId);
//        }
        //Lay danh sach ID don vi ma user duoc cau hinh gan giao viec
        MissionDAO missionD = new MissionDAO();
        List<EntityConfigUser> listConfigUser = (List<EntityConfigUser>) missionD.getConfigMissionByUser(sysUserId, 1);
        List<Long> listConfigUserId = new ArrayList<>();
        if (listConfigUser != null && !listConfigUser.isEmpty()) {
            for (EntityConfigUser obj : listConfigUser) {
                listConfigUserId.add(obj.getSysOrgId());
            }
        }
        listSysOrgId.addAll(listConfigUserId);
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = entityUserGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
            log.setParamList(data);

        }
        // Lay ma nhan vien
        String cardId = entityUserGroup.getCardId();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.ORG_ID,
                ConstantsFieldParams.MISSION_TYPE
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long assignOrgId = null;
            Integer type = null;
            try {
                assignOrgId = Long.parseLong(listValue.get(0));
            } catch (NumberFormatException ex) {
            }
            try {
                type = Integer.parseInt(listValue.get(1));
            } catch (NumberFormatException ex) {
            }
            try {
                type = Integer.parseInt(listValue.get(1));
            } catch (NumberFormatException ex) {
            }
            //Neu chua chon don vi giao viec thi listSysOrgId = listConfigUserId + listUserOrgId
            if (assignOrgId == null) {
                if (!listUserOrgId.isEmpty()) {
                    listSysOrgId.addAll(listUserOrgId);
                }
            }
            MeetingDAO meetingDao = new MeetingDAO();
            Object listOrgPerform = meetingDao.getListOrgPerformInAdvanceSearch(listSysOrgId, assignOrgId, type);
            // Ket thuc ghi log 
            LogUtils.logFunctionalEnd(log);
            strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, listOrgPerform, aesKey);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_ERROR, entityUserGroup.getEnumErrCode(), e.getMessage(), data, isSecurity);
            strResult = FunctionCommon.generateResponseJSON(entityUserGroup.getEnumErrCode(), null, null);
        }
        return FunctionCommon.getResultForClient(strResult);
    }

    /**
     * <b>Cap nhat bien ban hop</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String updateMeetingMinutes(HttpServletRequest request,
            String data, String isSecurity) {

        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            logger.error("addOrEditMeetingMinutes - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        try {
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[]{
                ConstantsFieldParams.MEETING_MINUTES
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Thong tin bien ban hop
            String strMeetingMinutes = listValue.get(0);
            Gson gson = new Gson();
            EntityMeetingMinutes meetingMinutes = gson.fromJson(strMeetingMinutes,
                    EntityMeetingMinutes.class);
            MeetingMinutesDAO dao = new MeetingMinutesDAO();
            Long result = dao.updateMeetingMinutes(userGroup, meetingMinutes);
            LogUtils.logFunctionalEnd(userGroup.getKpiLog());
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result,
                    userGroup.getStrAesKey());
        } catch (Exception ex) {
            logger.error("addOrEditMeetingMinutes - username: "
                    + userGroup.getCardId() + " - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Trinh ky van ban duoc tao tu bien ban hop</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String requestForSigningMeetingMinutes(HttpServletRequest request,
            String data, String isSecurity) {

        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            logger.error("requestForSigningMeetingMinutes - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        try {
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[]{
                ConstantsFieldParams.MEETING_MINUTES_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // ID bien ban hop
            Long meetingMinutesId = Long.parseLong(listValue.get(0));
            MeetingMinutesDAO dao = new MeetingMinutesDAO();
            int result = 0;
            if (dao.requestForSigningMeetingMinutes(userGroup.getUserId2(), meetingMinutesId)) {
                result = 1;
            }
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result,
                    userGroup.getStrAesKey());
        } catch (Exception ex) {
            logger.error("requestForSigningMeetingMinutes - username: "
                    + userGroup.getCardId() + " - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * chuyen cong van den nguoi trong don vi
     *
     * @param sourceId
     * @param listMission
     * @param userVof1
     * @param userVof2
     * @param assignId
     * @param creatorId
     * @param assignOrgId
     */
    void sendDocToPersonInGroupMission(Long sourceId, List<EntityMissionChild> listMission,
            EntityUser userVof1, Vof2_EntityUser userVof2, Long assignId, Long creatorId,
            Long assignOrgId) {
        if (sourceId == null || listMission == null || listMission.isEmpty()) {
            return;
        }
        try {
            //lay danh sach id don vi thuc hien cong viec
            List<String> listPerform;//don vi thuc hien
            List<EntityCombination> listCombination;//don vi phoi hop
            List<Long> lstOrgId;
            Long tempPerformId;
            Long performId;//id dau moi thuc hien
            DocumentDAO docDAO = new DocumentDAO();
            for (EntityMissionChild mission : listMission) {
                lstOrgId = new ArrayList<>();
                performId = mission.getPerformId();
                listPerform = mission.getListPerform();
                listCombination = mission.getListCombination();
                if (listPerform != null && listPerform.size() > 0) {
                    for (String strPerformId : listPerform) {
                        if (strPerformId != null) {
                            tempPerformId = Long.parseLong(strPerformId);
                            if (!lstOrgId.contains(tempPerformId)) {
                                lstOrgId.add(tempPerformId);
                            }
                        }
                    }
                }
                if (listCombination != null && listCombination.size() > 0) {
                    for (EntityCombination combination : listCombination) {
                        if (combination.getOrgCombinationId() != null) {
                            tempPerformId = Long.parseLong(combination.getOrgCombinationId());
                            if (!lstOrgId.contains(tempPerformId)) {
                                lstOrgId.add(tempPerformId);
                            }
                        }
                    }
                }
                //thuc hien chuyen van ban
                docDAO.sendDocumentToStaffFromMission(sourceId, lstOrgId, null,
                        userVof1, userVof2, assignId, creatorId, performId, assignOrgId);
            }
        } catch (Exception ex) {
            logger.error("sendDocToPersonInGroup: ", ex);
        }
    }

    /**
     * chuyen cong van tham chieu den nguoi trong don vi
     *
     * @param listMission
     * @param userVof1
     * @param userVof2
     * @param assignId
     * @param creatorId
     * @param assignOrgId don vi giao viec
     */
    void sendDocRefToPersonInGroupMission(List<EntityMissionChild> listMission,
            EntityUser userVof1, Vof2_EntityUser userVof2, Long assignId, Long creatorId,
            Long assignOrgId) {
        if (listMission == null || listMission.isEmpty()) {
            return;
        }
        try {
            //lay danh sach id don vi thuc hien cong viec
            List<String> listPerform;//don vi thuc hien
            List<EntityCombination> listCombination;//don vi phoi hop
            List<Long> lstOrgId;
            Long tempPerformId;
            List<EntitySourceMap> lstRefDoc;
            Long isSend;
            Long performId;//dau moi thuc hien
            DocumentDAO docDAO = new DocumentDAO();
            for (EntityMissionChild mission : listMission) {
                lstOrgId = new ArrayList<>();
                performId = mission.getPerformId();
                listPerform = mission.getListPerform();
                listCombination = mission.getListCombination();
                lstRefDoc = mission.getListRefDoc();
                if (lstRefDoc != null && !lstRefDoc.isEmpty()) {
                    if (listPerform != null && listPerform.size() > 0) {
                        for (String strPerformId : listPerform) {
                            if (strPerformId != null) {
                                tempPerformId = Long.parseLong(strPerformId);
                                if (!lstOrgId.contains(tempPerformId)) {
                                    lstOrgId.add(tempPerformId);
                                }
                            }
                        }
                    }
                    if (listCombination != null && listCombination.size() > 0) {
                        for (EntityCombination combination : listCombination) {
                            if (combination.getOrgCombinationId() != null) {
                                tempPerformId = Long.parseLong(combination.getOrgCombinationId());
                                if (!lstOrgId.contains(tempPerformId)) {
                                    lstOrgId.add(tempPerformId);
                                }
                            }
                        }
                    }
                    for (EntitySourceMap docRef : lstRefDoc) {
                        isSend = docRef.getIsSend();
                        if (isSend == null || isSend == 1) {
                            //thuc hien chuyen van ban
                            docDAO.sendDocumentToStaffFromMission(docRef.getSourceId(), lstOrgId, null,
                                    userVof1, userVof2, assignId, creatorId, performId, assignOrgId);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("sendDocRefToPersonInGroupMission: ", ex);
        }
    }

    /**
     * chuyen van ban toi don vi thuc hien nhiem vu
     *
     * @param listSourceMap
     * @param userVof1
     * @param userVof2
     * @param listPerform
     * @param assignId
     * @param creatorId
     * @param type 2: tu nhiem vu, 6: van ban tham chieu, 7: bao cao
     * @param assignOrgId don vi giao viec
     * @param performerId id dau moi thuc hien
     */
    void sendDocToPersonInGroupMission(List<EntitySourceMap> listSourceMap,
            EntityUser userVof1, Vof2_EntityUser userVof2, List<Long> listPerform,
            Long assignId, Long creatorId, Integer type, Long assignOrgId, Long performerId) {
        if (listSourceMap == null || listSourceMap.isEmpty()) {
            return;
        }
        try {
            Integer sourceType;
            Integer objectType;
            for (EntitySourceMap objSource : listSourceMap) {
                sourceType = objSource.getSourceType();
                objectType = objSource.getObjectType();
                if (sourceType != null && sourceType == 2
                        && objectType != null && objectType == type) {
                    //thuc hien chuyen van ban
                    DocumentDAO docDAO = new DocumentDAO();
                    docDAO.sendDocumentToStaffFromMission(objSource.getSourceId(), listPerform, null,
                            userVof1, userVof2, assignId, creatorId, performerId, assignOrgId);
                }
            }
        } catch (Exception ex) {
            logger.error("sendDocToPersonInGroupMission: ", ex);
        }
    }
    
    public String getMeetingsByDocId(HttpServletRequest request,
            String data, String isSecurity) {

        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            logger.error("getMeetingsByDocId - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        try {
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[]{
                ConstantsFieldParams.DOCUMENT_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long docId = Long.parseLong(listValue.get(0));
            MeetingDAO dao = new MeetingDAO();
            List<EntityMeeting> result = dao.getMeetingsByDocId(docId);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result,
                    userGroup.getStrAesKey());
        } catch (Exception ex) {
            logger.error("getMeetingsByDocId - username: "
                    + userGroup.getCardId() + " - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    public String getListUserMeeting(HttpServletRequest request,
            String data, String isSecurity) {

        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            logger.error("getListUserMeeting - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        try {
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[]{
                ConstantsFieldParams.MEETING_ID,
                "isMobile"
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long meetingId = Long.parseLong(listValue.get(0));
            boolean isMobile = "1".equals(listValue.get(1));
            MeetingDAO dao = new MeetingDAO();
            List<EntityUserMeeting> result = dao.getListUserMeeting(meetingId, isMobile);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result,
                    userGroup.getStrAesKey());
        } catch (Exception ex) {
            logger.error("getListUserMeeting - username: "
                    + userGroup.getCardId() + " - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    public String manualRollCallMeeting(HttpServletRequest request,
            String data, String isSecurity) {

        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            logger.error("manualRollCallMeeting - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        try {
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[]{
                ConstantsFieldParams.MEETING_ID,
                ConstantsFieldParams.LIST_INSERT,
                "listInsertOrg"
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long meetingId = CommonUtils.isInteger(listValue.get(0)) ? Long.parseLong(listValue.get(0)) : null;
            String strList = listValue.get(1);
            String strOrgList = listValue.get(2);
            if (meetingId == null) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID,
                        null, null); 
            }
            List<Long> userIds = new ArrayList<>();
            if (!CommonUtils.isEmpty(strList)) {
                JSONArray jsonArray = new JSONArray(strList);
                for (int i = 0; i < jsonArray.length(); i++) {
                    userIds.add(jsonArray.getLong(i));
                }
            }
            List<Long> orgIds = new ArrayList<>();
            if (!CommonUtils.isEmpty(strOrgList)) {
                JSONArray jsonOrgArray = new JSONArray(strOrgList);
                for (int i = 0; i < jsonOrgArray.length(); i++) {
                    orgIds.add(jsonOrgArray.getLong(i));
                }
            }
            MeetingDAO dao = new MeetingDAO();
            boolean result = dao.manualRollCallMeeting(meetingId, userIds, orgIds, userGroup.getUserId2());
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result ? 1: 0,
                    userGroup.getStrAesKey());
        } catch (Exception ex) {
            logger.error("manualRollCallMeeting - username: "
                    + userGroup.getCardId() + " - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    public String autoRollCallMeeting(HttpServletRequest request,
            String data, String isSecurity) {

        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            logger.error("autoRollCallMeeting - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        try {
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[]{ "meetingResourceId" };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long meetingResourceId = CommonUtils.isInteger(listValue.get(0)) ? Long.parseLong(listValue.get(0)) : null;
            if (meetingResourceId == null) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID,
                        null, null); 
            }
            MeetingDAO dao = new MeetingDAO();
            boolean result = dao.autoRollCallMeeting(meetingResourceId, userGroup.getUserId2());
            
            LogActionControler aclog = new LogActionControler();
            aclog.insertActionLog(userGroup.getVof2_ItemEntityUser().getUserId(), userGroup.getVof2_ItemEntityUser().getLoginName(),
                    "MeetingController.autoRollCallMeeting", request, "result: " + result + "; meetingResourceId: " + meetingResourceId, 
                    new Date(), "", "");            
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result ? 1: 0,
                    userGroup.getStrAesKey());
        } catch (Exception ex) {
            logger.error("autoRollCallMeeting - username: "
                    + userGroup.getCardId() + " - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    public String changeMemberMeeting(HttpServletRequest request,
            String data, String isSecurity) {

        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            logger.error("changeMemberMeeting - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        try {
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[]{ "meetingId", "reason", "oldUserId", "newUserId", "type", "permissionFile" };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long meetingId = CommonUtils.isInteger(listValue.get(0)) ? Long.parseLong(listValue.get(0)) : null;
            String reason = listValue.get(1);
            Long oldUserId = CommonUtils.isInteger(listValue.get(2)) ? Long.parseLong(listValue.get(2)) : null;
            Long newUserId = CommonUtils.isInteger(listValue.get(3)) ? Long.parseLong(listValue.get(3)) : null;
            Long type = CommonUtils.isInteger(listValue.get(4)) ? Long.parseLong(listValue.get(4)) : null;
            boolean permissionFile = "1".equals(listValue.get(5));
            if (meetingId == null || oldUserId == null || !oldUserId.equals(userGroup.getUserId2()) || newUserId.equals(userGroup.getUserId2())) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null); 
            }
            MeetingDAO dao = new MeetingDAO();
            int result = dao.changeMemberMeeting(meetingId, oldUserId, newUserId, reason, userGroup, type, permissionFile);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, userGroup.getStrAesKey());
        } catch (Exception ex) {
            logger.error("changeMemberMeeting - username: "
                    + userGroup.getCardId() + " - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    public String checkAttendMeeting(HttpServletRequest request,
            String data, String isSecurity) {

        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            logger.error("checkAttendMeeting - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        try {
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[]{ "meetingId" };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long meetingId = CommonUtils.isInteger(listValue.get(0)) ? Long.parseLong(listValue.get(0)) : null;
            if (meetingId == null) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null); 
            }
            boolean checkAddMember = false;
            if (!CommonUtils.isEmpty(userGroup.getVof2_ItemEntityUser().getListManagementOrg())) {
                checkAddMember = true;
            }
            int result = 0;
            
            MeetingWeekDAO mwDao = new MeetingWeekDAO();
            MeetingApproveResult meeting = mwDao.getMeetingById(userGroup, meetingId);
            Calendar cal = Calendar.getInstance();
            if (meeting == null || cal.getTime().after(DateUtils.stringToDate(meeting.getEndTime(), "dd/MM/yyyy HH:mm:ss"))) {
                result =  0;
            } else {
                MeetingDAO dao = new MeetingDAO();
                // datdc add start
                boolean checkWaitChangeMember = dao.checkWaitChangeMember(userGroup.getUserId2(), meetingId);
                boolean checkAddBonusMember = dao.checkPermissionAddMember(userGroup.getUserId2(), meetingId);
                boolean checkMember = dao.checkAttendMeeting(userGroup.getUserId2(), meetingId);
                if (checkMember) {
                    if (checkAddMember || checkAddBonusMember) {
                        if (checkWaitChangeMember) {
                            result = 3;
                        } else {
                            result = 1;
                        }
                    } else {
                        if (checkWaitChangeMember) {
                            result = 0;
                        } else {
                            result = 2;
                        }
                    }
                } else {
                    if (checkAddBonusMember) {
                        result = 3;
                    }
                }
            }
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, userGroup.getStrAesKey());
        } catch (Exception ex) {
            logger.error("checkAttendMeeting - username: "
                    + userGroup.getCardId() + " - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    public String updateStateFiles(HttpServletRequest request,
            String data, String isSecurity) {

        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            logger.error("updateStateFiles - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        try {
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[] {
                ConstantsFieldParams.TYPE,
                ConstantsFieldParams.LIST_INSERT
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long type = !CommonUtils.isEmpty(listValue.get(0)) ? Long.parseLong(listValue.get(0)) : 0L;
            String strList = listValue.get(1);
            if (CommonUtils.isEmpty(strList)) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID,
                        null, null); 
            }
            List<Long> ids = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(strList);
            for (int i = 0; i < jsonArray.length(); i++) {
                ids.add(jsonArray.getLong(i));
            }
            MeetingDAO dao = new MeetingDAO();
            boolean result = dao.updateStateFiles(ids, type);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result ? 1: 0,
                    userGroup.getStrAesKey());
        } catch (Exception ex) {
            logger.error("updateStateFiles - username: "
                    + userGroup.getCardId() + " - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    public String permissionRollCall(HttpServletRequest request,
            String data, String isSecurity) {

        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            logger.error("permissionRollCall - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        try {
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[]{
                ConstantsFieldParams.MEETING_ID,
                ConstantsFieldParams.TYPE
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long meetingId = !CommonUtils.isEmpty(listValue.get(0)) ? Long.parseLong(listValue.get(0)) : null;
            boolean check = "1".equals(listValue.get(1));
            if (meetingId == null) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID,
                        null, null); 
            }
            MeetingDAO dao = new MeetingDAO();
            boolean result = dao.permissionRollCall(meetingId, userGroup.getUserId2(), check);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result ? 1: 0,
                    userGroup.getStrAesKey());
        } catch (Exception ex) {
            logger.error("permissionRollCall - username: "
                    + userGroup.getCardId() + " - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    public String getContentMeetingMember(HttpServletRequest request,
            String data, String isSecurity) {

        EntityUserGroup userGroup = EntityUserGroup
                .getUserGroupBySessionIdOfRequest(request, data, isSecurity,
                        CLASS_NAME);
        //minhnq lay them id user
        Long userId = null;
        userId = userGroup.getUserId2();
        //minhnq end
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            logger.error("getContentMeetingMember - "
                    + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(
                    userGroup.getEnumErrCode(), null, null);
        }
        try {
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[] { ConstantsFieldParams.MEETING_ID,
                    ConstantsFieldParams.LOCALE };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json,
                    keys);
            Long meetingId = !CommonUtils.isEmpty(listValue.get(0)) ? Long
                    .parseLong(listValue.get(0)) : null;
            String locale = listValue.get(1);
            if (meetingId == null) {
                return FunctionCommon.generateResponseJSON(
                        ErrorCode.INPUT_INVALID, null, null);
            }
            MeetingWeekDAO mWDao = new MeetingWeekDAO();
            MeetingDAO dao = new MeetingDAO();
            MeetingApproveResult result = dao.getContentMeetingMember(
                    meetingId, userGroup, locale);
            //minhnq check an hien nut cap nhat khong ket luan
            boolean check = false;
            if (userId != null && result != null) {
                boolean checkUser = mWDao.checkUser(meetingId, userId);
                if (checkUser) {
                    check = true;
                }
                // check nguoi dang nhap co la lanh dao quan ly hop don vi viet
                // ket
                // luan
                EntityMeeting meeting = mWDao
                        .getMeetingOrgNoteConclusionsById(meetingId);
                if (null != meeting) {
                    if (null != meeting.getOrgNoteConclusions()) {
                        boolean checkRole = mWDao.checkRole(
                                meeting.getOrgNoteConclusions(), userId, meetingId);
                        if (checkRole) {
                            check = true;
                        }
                    }
                }
                if (check) {
                    result.setIsUpdateWithoutConclusions(1L);
                } else {
                    result.setIsUpdateWithoutConclusions(0L);
                }
                //minhnq end
            }
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                    result, userGroup.getStrAesKey());
        } catch (Exception ex) {
            logger.error(
                    "getContentMeetingMember - username: "
                            + userGroup.getCardId() + " - Exception!", ex);
            return FunctionCommon.generateResponseJSON(
                    ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }
    
    public String getListAbsenceMember(HttpServletRequest request,
            String data, String isSecurity) {

        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            logger.error("getListAbsenceMember - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        try {
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[]{
                ConstantsFieldParams.MEETING_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long meetingId = !CommonUtils.isEmpty(listValue.get(0)) ? Long.parseLong(listValue.get(0)) : null;
            if (meetingId == null) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID,
                        null, null); 
            }
            MeetingDAO dao = new MeetingDAO();
            List<EntityMeetingMember> result = dao.getListAbsenceMember(meetingId);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, userGroup.getStrAesKey());
        } catch (Exception ex) {
            logger.error("getListAbsenceMember - username: "
                    + userGroup.getCardId() + " - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    public String getDirectorConfig(HttpServletRequest request,
            String data, String isSecurity) {

        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            logger.error("getDirectorConfig - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        try {
            MeetingDAO dao = new MeetingDAO();
            List<EntityVhrOrg> result = dao.getDirectorConfig();
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, userGroup.getStrAesKey());
        } catch (Exception ex) {
            logger.error("getDirectorConfig - username: "
                    + userGroup.getCardId() + " - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    public String updateMemberReplate(HttpServletRequest request,
            String data, String isSecurity) {

        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            logger.error("updateMemberReplate - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        try {
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[] { ConstantsFieldParams.MEETING_ID, "listMemberReplate" };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long meetingId = !CommonUtils.isEmpty(listValue.get(0)) ? Long.parseLong(listValue.get(0)) : null; 
            String strList = listValue.get(1);
            if (meetingId == null || CommonUtils.isEmpty(strList)) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID,
                        null, null); 
            }
            
            List<EntityMeetingMember> listMember = new ArrayList<>();
            Gson gson = new Gson();
            JSONArray jsonArray = new JSONArray(strList);
            for (int i = 0; i < jsonArray.length(); i++) {
                listMember.add(gson.fromJson(jsonArray.getString(i), EntityMeetingMember.class));
            }
            
            MeetingDAO dao = new MeetingDAO();
            boolean result = dao.updateMemberReplate(meetingId, listMember, userGroup);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result ? 1 : 0, userGroup.getStrAesKey());
        } catch (Exception ex) {
            logger.error("updateMemberReplate - username: "
                    + userGroup.getCardId() + " - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    public String getListFileMeeting(HttpServletRequest request,
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
            Long meetingId = !CommonUtils.isEmpty(listValue.get(0)) ? Long.parseLong(listValue.get(0)) : null; 
            if (meetingId == null) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID,
                        null, null); 
            }
            
            String response = StringConstants.STR_RESULT_RETURN_FULL;
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(ErrorCode.class,
                    new ErrorCodeEnumAdapterTypeAdapter<ErrorCode>());
            Gson gson = gsonBuilder.create();
            JSONObject jsonObj = new JSONObject();
            MeetingWeekDAO dao = new MeetingWeekDAO();
            boolean checkMeetingManager = false;
            if (!CommonUtils.isEmpty(userGroup.getVof2_ItemEntityUser().getListMeetingManagerOrg())) {
                MeetingApproveResult meeting = dao.getMeetingById(userGroup, meetingId);
                if (meeting != null) {
                    if (userGroup.getVof2_ItemEntityUser().getListMeetingManagerOrg().contains(meeting.getOrgApprovalId())) {
                        checkMeetingManager = true;
                    }
                }
            }
            List<EntityFileAttachment> results = dao.getListFileAttachmentById(meetingId, userGroup.getUserId2(), checkMeetingManager);
            jsonObj.put("check", "1");
            if (!CommonUtils.isEmpty(results)) {
                // Neu la QLLH thi kiem tra them quyen doc file
                if (checkMeetingManager) {
                    List<EntityFileAttachment> listPermisionFile = dao.getListFileAttachmentById(meetingId, userGroup.getUserId2(), false);
                    if (!CommonUtils.isEmpty(listPermisionFile)) {
                        List<Long> permissionIds = new ArrayList<>();
                        for (EntityFileAttachment file : listPermisionFile) {
                            permissionIds.add(file.getFileAttachmentId());
                        }
                        for (EntityFileAttachment file : results) {
                            if (!permissionIds.contains(file.getFileAttachmentId())) {
                                file.setCanRead(0);
                            }
                        }
                    } else {
                        for (EntityFileAttachment file : results) {
                            file.setCanRead(0);
                        }
                    }
                }
                jsonObj.put("listAttach", gson.toJson(results));
            }
            data = "\"" + SecurityControler.encodeDataByAes(userGroup.getStrAesKey(), jsonObj.toString()) + "\"";
            response = String.format(response, gson.toJson(ErrorCode.SUCCESS), data);
            return response;
        } catch (Exception ex) {
            logger.error("updateMemberReplate - username: "
                    + userGroup.getCardId() + " - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    public String insertMeetingFiles(HttpServletRequest request,
            String data, String isSecurity) {

        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            logger.error("insertMeetingFiles - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        try {
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[] { ConstantsFieldParams.LIST_FILE_ATTACH,
                    ConstantsFieldParams.MEETING_ID,
                    ConstantsFieldParams.FILE_ID };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String str = listValue.get(0);
            Long meetingId = CommonUtils.isInteger(listValue.get(1)) ? Long.valueOf(listValue.get(1)) : null;
            if (CommonUtils.isEmpty(str) || meetingId == null) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null); 
            }

            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            Type listType = new TypeToken<ArrayList<EntityFiles>>() {
            }.getType();
            List<EntityFiles> listFile = gson.fromJson(str, listType);
            
            String strList = listValue.get(2);
            List<Long> fileIds = new ArrayList<>();
            if (!CommonUtils.isEmpty(strList)) {
                JSONArray jsonArray = new JSONArray(strList);
                for (int i = 0; i < jsonArray.length(); i++) {
                    fileIds.add(jsonArray.getLong(i));
                }
            }
            
            if (CommonUtils.isEmpty(listFile)) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null); 
            }

            MeetingDAO dao = new MeetingDAO();
            boolean result = dao.insertMeetingFiles(listFile, meetingId, fileIds, userGroup);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result ? 1 : 0, userGroup.getStrAesKey());
        } catch (Exception ex) {
            logger.error("insertMeetingFiles - username: "
                    + userGroup.getCardId() + " - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }
    
    public String removePermissionViewFile(HttpServletRequest request,
            String data, String isSecurity) {

        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            logger.error("removePermissionViewFile - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        try {
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[] {
                ConstantsFieldParams.MEETING_ID,
                ConstantsFieldParams.LIST_INSERT
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long meetingId = !CommonUtils.isEmpty(listValue.get(0)) ? Long.parseLong(listValue.get(0)) : 0L;
            String strList = listValue.get(1);
            if (CommonUtils.isEmpty(strList)) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID,
                        null, null); 
            }
            List<Long> ids = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(strList);
            for (int i = 0; i < jsonArray.length(); i++) {
            	Object obj = jsonArray.get(i);
            	if (CommonUtils.isInteger(obj.toString())) {
            		ids.add(Long.valueOf(obj.toString()));
            	}
            }
            MeetingDAO dao = new MeetingDAO();
            boolean result = dao.removePermissionViewFile(ids, meetingId, userGroup.getUserId2(), 0, null);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result ? 1: 0,
                    userGroup.getStrAesKey());
        } catch (Exception ex) {
            logger.error("removePermissionViewFile - username: "
                    + userGroup.getCardId() + " - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    public String getListUserViewFile(HttpServletRequest request,
            String data, String isSecurity) {

        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            logger.error("getListUserViewFile - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        try {
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[] { ConstantsFieldParams.FILE_ID, ConstantsFieldParams.MEETING_ID, ConstantsFieldParams.TYPE };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long fileId = !CommonUtils.isEmpty(listValue.get(0)) ? Long.parseLong(listValue.get(0)) : null; 
            Long meetingId = !CommonUtils.isEmpty(listValue.get(1)) ? Long.parseLong(listValue.get(1)) : null;
            Long type = !CommonUtils.isEmpty(listValue.get(2)) ? Long.parseLong(listValue.get(2)) : null; 
            if (meetingId == null || fileId == null) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null); 
            }
            MeetingDAO dao = new MeetingDAO();
            List<Long> results = dao.getListUserViewFile(meetingId, fileId, type);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, results, userGroup.getStrAesKey());
        } catch (Exception ex) {
            logger.error("getListUserViewFile - username: " + userGroup.getCardId() + " - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    public String updateBoardNameSmartRoom(HttpServletRequest request,
            String data, String isSecurity) {

        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            logger.error("updateBoardNameSmartRoom - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        try {
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[] { ConstantsFieldParams.MEETING_ID , "method"};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long meetingId = !CommonUtils.isEmpty(listValue.get(0)) ? Long.parseLong(listValue.get(0)) : null; 
            String method = listValue.get(1);
            if (meetingId == null || CommonUtils.isEmpty(method)) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null); 
            }
            MeetingWeekDAO dao = new MeetingWeekDAO();
            MeetingApproveResult meeting = dao.getMeetingForSmartRoom(meetingId);
            if (meeting != null && ((meeting.getRealRoomId() != null && "INSERT".equals(method)) 
                    || (meeting.getRealMeetingId() != null && "DELETE".equals(method))
                    || ((meeting.getRealRoomId() != null || meeting.getRealMeetingId() != null) && "EDIT".equals(method)))) {
                ThreadUpdateBoardNameSmartRoom thread = new ThreadUpdateBoardNameSmartRoom(meeting, method);
                ThreadPoolCommon.putRunnable(thread);
            }
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, 1, userGroup.getStrAesKey());
        } catch (Exception ex) {
            logger.error("updateBoardNameSmartRoom - username: " + userGroup.getCardId() + " - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    /**
     * Lay danh sach nguoi tham gia hop o man hinh phan quyen file
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getMeetingMemberPermissionFile(HttpServletRequest request,
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
            String[] keys = new String[] { "meetingId", "ids" };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long meetingId = !CommonUtils.isEmpty(listValue.get(0)) ? Long.parseLong(listValue.get(0)) : null; 
            String strList = listValue.get(1);
            if (CommonUtils.isEmpty(strList)) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID,
                        null, null); 
            }
            List<Long> ids = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(strList);
            for (int i = 0; i < jsonArray.length(); i++) {
                ids.add(jsonArray.getLong(i));
            }
            MeetingDAO dao = new MeetingDAO();
            List<EntityVhrEmployee> results = dao.getMeetingMemberPermissionFile(ids, meetingId);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, results, userGroup.getStrAesKey());
        } catch (Exception ex) {
            logger.error("updateMemberReplate - username: " + userGroup.getCardId() + " - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }
    
    /**
     * Kiem tra user co duoc cau hinh Truong don vi tham gia hop
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getDirectorConfigById(HttpServletRequest request,
            String data, String isSecurity) {

        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            logger.error("getDirectorConfigById - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        try {
            MeetingDAO dao = new MeetingDAO();
            Long results = dao.getOrgMeetingId(userGroup.getUserId2(), false);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, results, userGroup.getStrAesKey());
        } catch (Exception ex) {
            logger.error("getDirectorConfigById - username: " + userGroup.getCardId() + " - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }
    
    public String getMeetingMemberOrderView(HttpServletRequest request,
            String data, String isSecurity) {

        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            logger.error("getMeetingMemberOrderView - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        try {
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[] { "meetingId" };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long meetingId = !CommonUtils.isEmpty(listValue.get(0)) ? Long.parseLong(listValue.get(0)) : null; 
            
            MeetingDAO dao = new MeetingDAO();
            List<EntityVhrEmployee> results = dao.getMeetingMemberOrderView(meetingId);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, results, userGroup.getStrAesKey());
        } catch (Exception ex) {
            logger.error("getMeetingMemberOrderView - username: " + userGroup.getCardId() + " - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }
}