/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.meeting.MeetingAssistantDAO;
import com.viettel.voffice.database.dao.meeting.MeetingDAO;
import com.viettel.voffice.database.dao.meeting.MeetingWeekDAO;
import com.viettel.voffice.database.dao.sms.SmsDAO;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.EntityVhrEmployee;
import com.viettel.voffice.database.entity.calendar.MeetingApproveResult;
import com.viettel.voffice.database.entity.meeting.EntityMeetingMember;
import com.viettel.voffice.database.entity.task.EntityMeetingAssistant;
import com.viettel.voffice.database.entity.task.EntityMeetingConfig;
import com.viettel.voffice.database.entity.task.EntityMeetingReplace;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.I18N;
import com.viettel.voffice.utils.LogUtils;

/**
 *
 * @author cuongnv
 */
@SuppressWarnings({"unchecked", "deprecation"})
public class MeetingAssistantController {

    // Ten cua class bao gom ca ten package
    private static final String CLASS_NAME = MeetingAssistantController.class.getName();
    private static final Logger LOGGER = Logger.getLogger(MeetingAssistantController.class);

    /**
     * <b>Them moi tro ly</b>
     *
     * @author cuongnv
     * @param isSecurity
     * @param data
     * @param request
     * @return
     */
    public String addAssistant(String isSecurity, String data,
            HttpServletRequest request) {

        String[] keys = new String[] {
            ConstantsFieldParams.MEETING_ASSISTANT_LEADER_ID,
            ConstantsFieldParams.LST_MEETING_ASSISTANT
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("addAssistant - Session timeout!");
            return null;
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            // ID lanh dao
            Long leaderId = Long.parseLong(listValue.get(0));
            // Danh sach vai tro them moi
            List<EntityMeetingAssistant> listAdditional = new ArrayList<>();
            // Danh sach vai tro khong thay doi
            List<EntityMeetingAssistant> listImmutability = new ArrayList<>();
            // Danh sach tro ly voi cac vai tro
            String strListMeetingAssistant = listValue.get(1);
            if (!CommonUtils.isEmpty(strListMeetingAssistant)) {
                JSONArray jaAssistant = new JSONArray(strListMeetingAssistant);
                JSONObject joAssistant;
                Long employeeId;
                String strMeetingAssistantId;
                String[] meetingAssistantIds;
                Long meetingAssistantId;
                EntityMeetingAssistant meetingAssistant;
                for (int i = 0; i < jaAssistant.length(); i++) {
                    joAssistant = jaAssistant.getJSONObject(i);
                    // ID tro ly
                    employeeId = joAssistant.getLong(
                            ConstantsFieldParams.MEETING_ASSISTANT_EMPLOYEE_ID);
                    // Danh sach cac id ban ghi vai tro, moi id co gia tri nhu sau
                    // -1: Xoa vai tro
                    // id: Giu nguyen vai tro
                    //  0: Them moi vai tro day
                    strMeetingAssistantId = joAssistant.getString(
                            ConstantsFieldParams.LST_MEETING_ASSISTANT_ID);
                    meetingAssistantIds = strMeetingAssistantId.split(",");
                    for (int j = 0; j < meetingAssistantIds.length; j++) {
                        // ID ban ghi vai tro
                        meetingAssistantId = Long.parseLong(meetingAssistantIds[j]);
                        meetingAssistant = new EntityMeetingAssistant();
                        meetingAssistant.setMeetingAssistantId(meetingAssistantId);
                        meetingAssistant.setLeaderId(leaderId);
                        meetingAssistant.setEmployeeId(employeeId);
                        meetingAssistant.setAssiType(j + 1);                        
                        // Them moi
                        if (meetingAssistantId == 0) {
                            listAdditional.add(meetingAssistant);
                        } // Giu nguyen
                        else if (meetingAssistantId > 0) {
                            listImmutability.add(meetingAssistant);
                        }
                    }
                }
            }
            MeetingAssistantDAO meetingAssistantDAO = new MeetingAssistantDAO();
            int result = meetingAssistantDAO.addAssistant(userGroup.getUserId2(),
                    leaderId, listAdditional, listImmutability) ? 1 : 0;
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            LOGGER.error("addAssistant - Exception - username: "
                    + userGroup.getCardId(), ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
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
    public String searchAssistant(String isSecurity, String data,
            HttpServletRequest request) {

        String[] keys = new String[]{
            ConstantsFieldParams.MEETING_ASSISTANT_KEY_WORD,
            ConstantsFieldParams.MEETING_ASSISTANT_KEY_WORD_LEADER,
            ConstantsFieldParams.MEETING_ASSISTANT_KEY_WORD_EMPLOYEE,
            "listAssiType",
            ConstantsFieldParams.MEETING_ASSISTANT_TYPE_SEARCH
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("searchAssistant - Session timeout!");
            return null;
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            // Tu khoa tim kiem nhanh
            String keyword = listValue.get(0);
            // Tu khoa tim kiem lanh dao
            String keywordForLeader = listValue.get(1);
            // Tu khoa tim kiem nhan vien
            String keywordForEmployee = listValue.get(2);
            // Neu la tim kiem nhanh
            boolean isQuickSearch = false;
            if (!CommonUtils.isEmpty(keyword)) {
                keywordForLeader = keyword;
                keywordForEmployee = keyword;
                isQuickSearch = true;
            }
            // Loai tro ly
            List<Integer> listAssiType = new ArrayList<>();
            String strListAssiType = listValue.get(3);
            if (!CommonUtils.isEmpty(strListAssiType)) {
                Gson gson = new Gson();
                Type type = new TypeToken<ArrayList<Integer>>() {
                }.getType();
                listAssiType = gson.fromJson(strListAssiType, type);
            }
            // Loai tim kiem
            Integer searchType = null;
            String strSearchType = listValue.get(4);
            if (!CommonUtils.isEmpty(strSearchType)) {
                searchType = Integer.parseInt(strSearchType);
            }
            MeetingAssistantDAO meetingAssistantDAO = new MeetingAssistantDAO();
            List<EntityMeetingAssistant> result = meetingAssistantDAO.searchAssistant(
                    userGroup.getUserId2(), keywordForLeader, keywordForEmployee,
                    isQuickSearch, listAssiType, searchType);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            LOGGER.error("searchAssistant - Exception - username: "
                    + userGroup.getCardId(), ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
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
    public String searchAdvancedAssistant(String isSecurity, String data,
            HttpServletRequest request) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        //Id user vof2
        Long userIdVof2 = userGroup.getVof2_ItemEntityUser().getUserId();
        if (userIdVof2 == null) {
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
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
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.MEETING_ASSISTANT_KEY_WORD_LEADER,
                ConstantsFieldParams.MEETING_ASSISTANT_KEY_WORD_EMPLOYEE,
                ConstantsFieldParams.MEETING_ASSISTANT_ASSI_TYPE,
                ConstantsFieldParams.MEETING_ASSISTANT_TYPE_SEARCH
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String keywordLeader = listValue.get(0);
            String keywordEmployee = listValue.get(1);
            Integer assiType = 7;
            try {
                assiType = Integer.parseInt(listValue.get(2));
            } catch (NumberFormatException e) {
                //Khong chuyen du lieu
            }
            Integer typeSearch = 0;
            try {
                typeSearch = Integer.parseInt(listValue.get(3));
            } catch (NumberFormatException e) {
                //Khong chuyen du lieu
            }
            MeetingAssistantDAO mad = new MeetingAssistantDAO();
            List<EntityMeetingAssistant> rs = mad.searchAdvancedAssistant(keywordLeader,
                    keywordEmployee, assiType, typeSearch, userIdVof2);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, rs, aesKey);
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, "", aesKey);
        }
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
    public String getDetailMeetingAssistant(String isSecurity, String data,
            HttpServletRequest request) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        //Id user vof2
        Long userIdVof2 = userGroup.getVof2_ItemEntityUser().getUserId();
        if (userIdVof2 == null) {
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
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
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.MEETING_ASSISTANT_LEADER_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long leaderId = Long.parseLong(listValue.get(0));

            MeetingAssistantDAO mad = new MeetingAssistantDAO();
            EntityMeetingAssistant rs = mad.getDetailMeetingAssistant(leaderId);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, rs, aesKey);
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, "", aesKey);
        }
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
    public String deleteAssistant(String isSecurity, String data,
            HttpServletRequest request) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        //Id user vof2
        Long userIdVof2 = userGroup.getVof2_ItemEntityUser().getUserId();
        if (userIdVof2 == null) {
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
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
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.MEETING_ASSISTANT_LEADER_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long leaderId = Long.parseLong(listValue.get(0));

            List<Long> listAssistantId = new ArrayList<Long>();
            if (json.has(ConstantsFieldParams.LST_MEETING_ASSISTANT)) {
                JSONArray listMeetingAssistant = FunctionCommon.jsonGetArray(ConstantsFieldParams.LST_MEETING_ASSISTANT, data);
                if (listMeetingAssistant != null && listMeetingAssistant.length() > 0) {
                    for (int i = 0; i < listMeetingAssistant.length(); i++) {
                        JSONObject innerObj = (JSONObject) listMeetingAssistant.get(i);
                        keys = new String[]{
                            ConstantsFieldParams.LST_MEETING_ASSISTANT_ID,
                            ConstantsFieldParams.MEETING_ASSISTANT_EMPLOYEE_ID,
                            ConstantsFieldParams.MEETING_ASSISTANT_ASSI_TYPE
                        };

                        listValue = FunctionCommon.getValuesFromJSON(innerObj, keys);
                        listAssistantId.add(Long.parseLong(listValue.get(1)));
                    }
                }
            }

            MeetingAssistantDAO mad = new MeetingAssistantDAO();
            Integer rs = mad.deleteAssistant(leaderId, listAssistantId, userIdVof2);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, rs, aesKey);
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, "", aesKey);
        }
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
    public String addBlockEmailSms(String isSecurity, String data,
            HttpServletRequest request) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        //Id user vof2
        Long userIdVof2 = userGroup.getVof2_ItemEntityUser().getUserId();
        if (userIdVof2 == null) {
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
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
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.MEETING_ASSISTANT_LEADER_ID,
                ConstantsFieldParams.MEETING_CONFIG_ORG_ID,
                ConstantsFieldParams.MEETING_CONFIG_EMAIL,
                ConstantsFieldParams.MEETING_CONFIG_SMS,
                ConstantsFieldParams.MEETING_CONFIG_MOBILE,
                ConstantsFieldParams.MEETING_CONFIG_MAIL_CONFERENCE_CODE
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String strLeaderId = listValue.get(0);
            Long leaderId = null;
            if (strLeaderId != null && !strLeaderId.isEmpty()) {
                leaderId = Long.parseLong(strLeaderId);
            }
            String strOrg = listValue.get(1);
            Long sysOrganizationId = null;
            if (strOrg != null && !strOrg.isEmpty()) {
                sysOrganizationId = Long.parseLong(strOrg);
            }
            String strEmail = listValue.get(2);
            Integer email = null;
            if (strEmail != null && !strEmail.isEmpty()) {
                email = Integer.parseInt(strEmail);
            }
            String strSMS = listValue.get(3);
            Integer sms = null;
            if (strSMS != null && !strSMS.isEmpty()) {
                sms = Integer.parseInt(strSMS);
            }
            String strMobile = listValue.get(4);
            Integer mobile = null;
            if (strMobile != null && !strMobile.isEmpty()) {
                mobile = Integer.parseInt(strMobile);
            }
            String strConferenceCode = listValue.get(5);
            Integer conferenceCode = null;
            if (strConferenceCode != null && !strConferenceCode.isEmpty()) {
                conferenceCode = Integer.parseInt(strConferenceCode);
            }

            MeetingAssistantDAO mad = new MeetingAssistantDAO();
            Integer rs = mad.addBlockEmailSms(leaderId, sysOrganizationId, email, sms, mobile, conferenceCode, userIdVof2);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, rs, aesKey);
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, "", aesKey);
        }
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
    public String searchBlockEmailSms(String isSecurity, String data,
            HttpServletRequest request) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        //Id user vof2
        Long userIdVof2 = userGroup.getVof2_ItemEntityUser().getUserId();
        if (userIdVof2 == null) {
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
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
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.MEETING_ASSISTANT_KEY_WORD
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String keyword = listValue.get(0);
            MeetingAssistantDAO mad = new MeetingAssistantDAO();
            List<EntityMeetingConfig> rs = mad.searchBlockEmailSms(keyword, userIdVof2);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, rs, aesKey);
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, "", aesKey);
        }
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
    public String searchAdvancedBlockEmailSms(String isSecurity, String data,
            HttpServletRequest request) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        //Id user vof2
        Long userIdVof2 = userGroup.getVof2_ItemEntityUser().getUserId();
        if (userIdVof2 == null) {
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
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
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.MEETING_ASSISTANT_KEY_WORD,
                ConstantsFieldParams.MEETING_CONFIG_ORG_ID,
                ConstantsFieldParams.MEETING_CONFIG_EMAIL,
                ConstantsFieldParams.MEETING_CONFIG_SMS,
                ConstantsFieldParams.MEETING_CONFIG_MOBILE,
                ConstantsFieldParams.MEETING_CONFIG_MAIL_CONFERENCE_CODE
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String keyword = listValue.get(0);
            String strOrg = listValue.get(1);
            Long sysOrganizationId = null;
            if (strOrg != null && !strOrg.isEmpty()) {
                sysOrganizationId = Long.parseLong(strOrg);
            }
            String strEmail = listValue.get(2);
            Integer email = null;
            if (strEmail != null && !strEmail.isEmpty()) {
                email = Integer.parseInt(strEmail);
            }
            String strSMS = listValue.get(3);
            Integer sms = null;
            if (strSMS != null && !strSMS.isEmpty()) {
                sms = Integer.parseInt(strSMS);
            }
            String strMobile = listValue.get(4);
            Integer mobile = null;
            if (strMobile != null && !strMobile.isEmpty()) {
                mobile = Integer.parseInt(strMobile);
            }
            String strConferenceCode = listValue.get(5);
            Integer conferenceCode = null;
            if (strConferenceCode != null && !strConferenceCode.isEmpty()) {
                conferenceCode = Integer.parseInt(strConferenceCode);
            }
            MeetingAssistantDAO mad = new MeetingAssistantDAO();
            List<EntityMeetingConfig> rs = mad.searchAdvancedBlockEmailSms(keyword, sysOrganizationId, email, sms, mobile, conferenceCode, userIdVof2);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, rs, aesKey);
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, "", aesKey);
        }
    }

    public String deleteBlockEmailSms(String isSecurity, String data,
            HttpServletRequest request) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        //Id user vof2
        Long userIdVof2 = userGroup.getVof2_ItemEntityUser().getUserId();
        if (userIdVof2 == null) {
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
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
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.MEETING_CONFIG_ID};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long meetingConfigId = Long.parseLong(listValue.get(0));
            MeetingAssistantDAO mad = new MeetingAssistantDAO();
            Integer rs = mad.deleteBlockEmailSms(meetingConfigId, userIdVof2);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, rs, aesKey);
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, "", aesKey);
        }
    }

    /**
     * edit block email sms
     *
     * @param isSecurity
     * @param data
     * @param request
     * @return
     */
    public String editBlockEmailSms(String isSecurity, String data,
            HttpServletRequest request) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        //Id user vof2
        Long userIdVof2 = userGroup.getVof2_ItemEntityUser().getUserId();
        if (userIdVof2 == null) {
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
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
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.MEETING_CONFIG_ID,
                ConstantsFieldParams.MEETING_CONFIG_EMAIL,
                ConstantsFieldParams.MEETING_CONFIG_SMS,
                ConstantsFieldParams.MEETING_CONFIG_MOBILE,
                ConstantsFieldParams.MEETING_CONFIG_MAIL_CONFERENCE_CODE
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long meetingConfigId = Long.parseLong(listValue.get(0));
            String strEmail = listValue.get(1);
            Integer email = null;
            if (strEmail != null && !strEmail.isEmpty()) {
                email = Integer.parseInt(strEmail);
            }
            String strSMS = listValue.get(2);
            Integer sms = null;
            if (strSMS != null && !strSMS.isEmpty()) {
                sms = Integer.parseInt(strSMS);
            }
            String strMobile = listValue.get(3);
            Integer mobile = null;
            if (strMobile != null && !strMobile.isEmpty()) {
                mobile = Integer.parseInt(strMobile);
            }
            String strConferenceCode = listValue.get(4);
            Integer conferenceCode = null;
            if (strConferenceCode != null && !strConferenceCode.isEmpty()) {
                conferenceCode = Integer.parseInt(strConferenceCode);
            }
            MeetingAssistantDAO mad = new MeetingAssistantDAO();
            Integer rs = mad.editBlockEmailSms(meetingConfigId, email, sms, mobile, conferenceCode, userIdVof2);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, rs, aesKey);
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, "", aesKey);
        }
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
    public String getLeaderByEmployee(String isSecurity, String data,
            HttpServletRequest request) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        //Id user vof2
        Long userIdVof2 = userGroup.getVof2_ItemEntityUser().getUserId();
        if (userIdVof2 == null) {
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
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
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.MEETING_ASSISTANT_LEADER_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long leaderId = Long.parseLong(listValue.get(0));
            MeetingAssistantDAO mad = new MeetingAssistantDAO();
            List<EntityVhrEmployee> rs = mad.getLeaderByEmployee(leaderId);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, rs, aesKey);
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, "", aesKey);
        }
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
    public String searchFollowLeader(String isSecurity, String data,
            HttpServletRequest request) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        //Id user vof2
        Long userIdVof2 = userGroup.getVof2_ItemEntityUser().getUserId();
        if (userIdVof2 == null) {
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
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
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.MEETING_ASSISTANT_LEADER_ID,
                ConstantsFieldParams.MEETING_ASSISTANT_EMPLOYEE_ID,
                ConstantsFieldParams.TITLE,
                ConstantsFieldParams.CODE,
                ConstantsFieldParams.MEETING_ASSISTANT_STATR_DATE,
                ConstantsFieldParams.MEETING_ASSISTANT_END_DATE,
                ConstantsFieldParams.MEETING_ASSISTANT_IS_READ,
                ConstantsFieldParams.MEETING_ASSISTANT_IS_PROCESS,
                ConstantsFieldParams.IS_COUNT,
                ConstantsFieldParams.START_RECORD,
                ConstantsFieldParams.PAGE_SIZE,
                ConstantsFieldParams.IS_EXPORT
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long leaderId = null;
            try {
                leaderId = Long.parseLong(listValue.get(0));
            } catch (NumberFormatException e) {
            }
            Long employeeId = null;
            try {
                employeeId = Long.parseLong(listValue.get(1));
            } catch (NumberFormatException e) {
            }
            String title = listValue.get(2);
            String code = listValue.get(3);
            String sendDate = listValue.get(4);
            String recvDate = listValue.get(5);
            Integer isRead = null;
            try {
                isRead = Integer.parseInt(listValue.get(6));
            } catch (NumberFormatException e) {
            }
            Integer isProcess = null;
            try {
                isProcess = Integer.parseInt(listValue.get(7));
            } catch (NumberFormatException e) {
            }
            Integer isCount = 0;
            try {
                isCount = Integer.parseInt(listValue.get(8));
            } catch (NumberFormatException e) {
            }
            Long startRecord = null;
            try {
                startRecord = Long.parseLong(listValue.get(9));
            } catch (NumberFormatException e) {
            }

            Long pageSize = null;
            try {
                pageSize = Long.parseLong(listValue.get(10));
            } catch (NumberFormatException e) {
            }
            Integer isExport = null;
            try {
                isExport = Integer.parseInt(listValue.get(11));
            } catch (NumberFormatException e) {
            }
            MeetingAssistantDAO mad = new MeetingAssistantDAO();
            Object rs = mad.searchFollowLeader(leaderId, employeeId, title, code,
                    sendDate, recvDate, isRead, isProcess, isCount, startRecord,
                    pageSize, userIdVof2, isExport);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, rs, aesKey);
        } catch (JSONException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, "", aesKey);
        }
    }
    
    public String getMeetingAssistantList(String isSecurity, String data,
            HttpServletRequest request) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        //Id user vof2
        Long userIdVof2 = userGroup.getVof2_ItemEntityUser().getUserId();
        if (userIdVof2 == null) {
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
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
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.MEETING_ASSISTANT_ASSI_TYPE,
                ConstantsFieldParams.STAFF_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String strList = listValue.get(0);
            Long userId = Long.valueOf(listValue.get(1));
            List<Integer> typeIds = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(strList);
            for (int i = 0; i < jsonArray.length(); i++) {
                typeIds.add(jsonArray.getInt(i));
            }
            
            MeetingAssistantDAO mad = new MeetingAssistantDAO();
            List<EntityMeetingAssistant> rs = mad.getMeetingAssistantList(userId, typeIds);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, rs, aesKey);
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, "", aesKey);
        }
    }

    /**
     * 
     * @param isSecurity
     * @param data
     * @param request
     * @return
     */
    public String findMeetingChangeReplate(String isSecurity, String data,
            HttpServletRequest request) {
        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        //Id user vof2
        Long userIdVof2 = userGroup.getVof2_ItemEntityUser().getUserId();
        if (userIdVof2 == null) {
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
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
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                    ConstantsFieldParams.TITLE,
                    ConstantsFieldParams.KEYWORD,
                    ConstantsFieldParams.STATE,
                    ConstantsFieldParams.START_RECORD,
                    ConstantsFieldParams.PAGE_SIZE,
                    "isMobile"
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);

            // tieu de
            String title = "";
            if (!CommonUtils.isEmpty(listValue.get(0))) {
                title = listValue.get(0);
            }

            // ten, ma nhan vien, email
            String keyWord = listValue.get(1);

            // trang thai
            Long state = null;
            String stateString = listValue.get(2);
            if (!CommonUtils.isEmpty(stateString)) {
                state = Long.parseLong(stateString);
            }

            // Vi tri lay ra
            Long startRecord = null;
            String strStartRecord = listValue.get(3);
            if (!CommonUtils.isEmpty(strStartRecord)) {
                startRecord = Long.parseLong(strStartRecord);
            }
            // So luong lay ra
            Long pageSize = null;
            String strPageSize = listValue.get(4);
            if (!CommonUtils.isEmpty(strPageSize)) {
                pageSize = Long.parseLong(strPageSize);
            }
            boolean isMobile = false;
            if (!CommonUtils.isEmpty(listValue.get(5))) {
               isMobile = "1".equals(listValue.get(5));
            }

            MeetingAssistantDAO mad = new MeetingAssistantDAO();
            List<EntityMeetingReplace> result = new ArrayList<EntityMeetingReplace>();

            result = mad.findMeetingChangeReplate(title, keyWord, state, userIdVof2, startRecord, pageSize, isMobile);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, "", aesKey);
        }
    }

    /**
     * 
     * @param isSecurity
     * @param data
     * @param request
     * @return
     */
    public String getMeetingChangeReplate(String isSecurity, String data,
            HttpServletRequest request) {
        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        //Id user vof2
        Long userIdVof2 = userGroup.getVof2_ItemEntityUser().getUserId();
        if (userIdVof2 == null) {
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
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
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                    ConstantsFieldParams.MEETING_ID,
                    ConstantsFieldParams.STATE
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);

            // meetingId
            Long meetingId = Long.parseLong(listValue.get(0));

            // Trang thai
            Long state = null;
            if (!CommonUtils.isEmpty(listValue.get(1))) {
                state = Long.parseLong(listValue.get(1));
            }

            MeetingAssistantDAO mad = new MeetingAssistantDAO();
            List<EntityMeetingReplace> result = new ArrayList<EntityMeetingReplace>();

            result = mad.getMeetingChangeReplate(meetingId, state);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, "", aesKey);
        }
    }
    
    /**
     * 
     * @param isSecurity
     * @param data
     * @param request
     * @return
     */
    public String approveReplateMember(String isSecurity, String data,
            HttpServletRequest request) {
        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        //Id user vof2
        Long userIdVof2 = userGroup.getVof2_ItemEntityUser().getUserId();
        if (userIdVof2 == null) {
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
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
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                    ConstantsFieldParams.MEETING_ID,
                    ConstantsFieldParams.LIST_MEET_REPLATE
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);

            // meetingId
            Long meetingId = Long.parseLong(listValue.get(0));

            // lst EntityMeetingReplace sau thay doi
            List<EntityMeetingReplace> listMeetReplate = new ArrayList<EntityMeetingReplace>();
            if (!CommonUtils.isEmpty(listValue.get(1))) {
                listMeetReplate = (List<EntityMeetingReplace>)
                    FunctionCommon.convertJsonToListObject(listValue.get(1), EntityMeetingReplace.class);
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, 0, aesKey);
            }


            MeetingAssistantDAO mad = new MeetingAssistantDAO();
            MeetingDAO med = new MeetingDAO();
            MeetingWeekDAO mwDao = new MeetingWeekDAO();
            Boolean checkSuccess = false;
            // Cap nhat du lieu
            for (EntityMeetingReplace emR : listMeetReplate) {
                // Lay thong tin
                EntityVhrEmployee newUser = new EntityVhrEmployee();
                // Lay thong tin trc khi thay
                EntityMeetingMember member = mwDao.getMeetingMember(emR.getMemberId(), meetingId);
                newUser = med.getInfoUser(emR.getMeetingReplateId());
                // Neu la phe duyet
                if (emR.getStatusApproval() != null) {
                    if (emR.getStatusApproval().equals(2L)) {
                        if (med.changeMemberMeeting(meetingId, emR.getMemberId(), emR.getMeetingReplateId(),
                                emR.getReasonApproval(), userGroup, 5L, false) == 0) {
                            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, 0, aesKey);
                        } else {
                            if (!mad.updateMeetingReplate(userIdVof2, emR)) {
                                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, 0, aesKey);
                            } else {
                                checkSuccess = true;
                                // lay thong tin chi tiet
                                MeetingApproveResult meetingDetail = mwDao.getMeetingDetail(userGroup, meetingId);
                                // update memeeting
                                med.updateMemberString(meetingId, meetingDetail,
                                        !CommonUtils.isEmpty(newUser.getLanguageUser()) ? newUser.getLanguageUser() : null);
                                sendSmsAfterAprove(newUser, member, emR, userGroup);
                            }
                        }
                    }
                    // Neu la tu choi
                    if (emR.getStatusApproval().equals(3L)) {
                        if (!mad.updateMeetingReplate(userIdVof2, emR)) {
                            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, 0, aesKey);
                        } else {
                            checkSuccess = true;
                            sendSmsAfterAprove(newUser, member, emR, userGroup);
                        }
                    }
                }
             }

             LogUtils.logFunctionalEnd(log);
             if (checkSuccess) {
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, 1, aesKey);
             }
             return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, 0, aesKey);
         } catch (JSONException | NumberFormatException | CloneNotSupportedException ex) {
             LOGGER.error(ex.getMessage(), ex);
             return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, "", aesKey);
         }
     }
	

    /**
     * sendSmsAfterAprove
     * @param newUser
     * @param member
     * @param emR
     * @param userGroup
     * @param meetingDetail
     */
    private void sendSmsAfterAprove (EntityVhrEmployee newUser,  EntityMeetingMember member, 
            EntityMeetingReplace emR, EntityUserGroup userGroup) {
        SmsDAO smsDao = new SmsDAO();
        MeetingDAO med = new MeetingDAO();
        MeetingWeekDAO mwDao = new MeetingWeekDAO();
        EntityVhrEmployee assitant = new EntityVhrEmployee();
        assitant = med.getInfoUser(emR.getCreatedBy());
        I18N i18n = new I18N(assitant.getLanguageUser());
        String text = "";
        if (emR.getStatusApproval().equals(2L)) {
            text = i18n.getString(I18N.Key.APPROVE);
            // Gui cho nguoi  dc thay the tham gia lich hop
            MeetingApproveResult meetingDetail = mwDao.getMeetingDetail(userGroup, emR.getId());
            MeetingAssistantDAO maD = new MeetingAssistantDAO();
            List<EntityMeetingMember> newUserSms = maD.getInfoMeetMemId(emR.getId(), newUser.getEmployeeId());
            if (!CommonUtils.isEmpty(newUserSms)) {
                mwDao.sendSmsUserChangeAssign(meetingDetail, newUserSms.get(0), userGroup);
            }
        } else {
            text = i18n.getString(I18N.Key.REJECT);
        }
        // Gui cho nguoi yeu cau thay doi
        String smsContentToMember = "";
        if (!CommonUtils.isEmpty(emR.getReasonApproval())) {
            smsContentToMember = i18n.getStringHasValue("meeting.notify.lhsms11.userRequire", emR.getTitle()
                    , text, " " + i18n.getStringHasValue("reason") + ": " + emR.getReasonApproval());
        } else {
            smsContentToMember = i18n.getStringHasValue("meeting.notify.lhsms11.userRequire", emR.getTitle()
                    , text, "");
        }
        smsDao.addMsgToSmsMaster(assitant.getMobilePhone(), CommonUtils.toUnsignedChar(smsContentToMember),
                ConstantsFieldParams.LHSMS11, userGroup.getUserId2(), userGroup.getUserId2(), assitant.getEmployeeId(), 12L,
                ConstantsFieldParams.MEETING_SEND_USER_CHANGE_REPLATE);
    }

    /**
     * getExistAssistantChangeMeetByEmployeeId
     * @author datdc
     * @param isSecurity
     * @param data
     * @param request
     * @return
     */
    public String getExistAssistantChangeMeetByEmployeeId(String isSecurity, String data,
            HttpServletRequest request) {
        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        //Id user vof2
        Long userIdVof2 = userGroup.getVof2_ItemEntityUser().getUserId();
        if (userIdVof2 == null) {
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
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
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                    ConstantsFieldParams.MEETING_ASSISTANT_EMPLOYEE_ID,
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);

            // employeeId
            Long employeeId = Long.parseLong(listValue.get(0));
            MeetingAssistantDAO mad = new MeetingAssistantDAO();

            Long result = mad.getExistAssistantChangeMeetByEmployeeId(employeeId);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
         } catch (JSONException | NumberFormatException ex) {
             LOGGER.error(ex.getMessage(), ex);
             return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, "", aesKey);
         }
     }
}
