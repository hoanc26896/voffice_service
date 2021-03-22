/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.entity.EntityVhrOrg;
import com.viettel.voffice.database.dao.sms.SmsDAO;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.EntityVhrEmployee;
import com.viettel.voffice.database.entity.User.EntityCvGroup;
import com.viettel.voffice.database.entity.User.EntityUser;
import com.viettel.voffice.database.entity.User.Vof2_EntityUser;
import com.viettel.voffice.database.entity.document.EntityDocumentInStaff;
import com.viettel.voffice.database.entity.task.EntityTaskApproval;
import com.viettel.voffice.utils.LogUtils;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author voffice_guest1
 */
public class SmsTaskController {

    public static final String ROOT_ACTION = "smsTask";
    // Log file
    private static final Logger LOGGER = Logger.getLogger(DocumentController.class);
    // Ten cua class bao gom ca ten package
    private static final String CLASS_NAME = SmsTaskController.class.getName();

    public String sendSmsSignAfterMonth(HttpServletRequest request, String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);

        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("editDocument - No session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }

        // Neu user hoac user id tren he thong 1&2 null
        // -> Tra ve thong bao khong co quyen
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        if ((user1 == null || user1.getUserId() == null)
                && (user2 == null || user2.getUserId() == null)) {
            LOGGER.error("editDocument - user hoac userId tren he thong 1&2 null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Neu khong co thong tin tren he thong 1
        // -> Gan userId1 = 0
        Long userId1 = (user1 == null || user1.getUserId() == null) ? 0L : user1.getUserId();

        // Neu khong co thong tin tren he thong 2
        // -> Gan userId2 = 0
        Long userId2 = (user2 == null || user2.getUserId() == null) ? 0L : user2.getUserId();

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
                ConstantsFieldParams.TASKSMS_RECV_USER,
                ConstantsFieldParams.TASKSMS_APPROVAL_PREDIO_MONTH,
                ConstantsFieldParams.TASKSMS_APPROVAL_PREDIO_YEAR
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);

            Long recvUserId2 = Long.parseLong(listValue.get(0));
            String monthTask = listValue.get(1);
            String yearTask = listValue.get(2);
            SmsDAO smsDao = new SmsDAO();
            String result;
            if (smsDao.sendSMSToARecv(userId2, recvUserId2, monthTask, yearTask,
                    Constants.SMS_TEXT_INTERCEPT.SIGNTASK_ENDMONTH)) {
                result = "1";
            } else {
                result = "0";
            }
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error("editDocument - userId1: " + userId1
                    + " - userId2: " + userId2
                    + " - Exception: ", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    public String sendSmsMulSignAfterMonth(HttpServletRequest request, String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);

        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("editDocument - No session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }

        // Neu user hoac user id tren he thong 1&2 null
        // -> Tra ve thong bao khong co quyen
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        if ((user1 == null || user1.getUserId() == null)
                && (user2 == null || user2.getUserId() == null)) {
            LOGGER.error("editDocument - user hoac userId tren he thong 1&2 null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Neu khong co thong tin tren he thong 1
        // -> Gan userId1 = 0
        Long userId1 = (user1 == null || user1.getUserId() == null) ? 0L : user1.getUserId();

        // Neu khong co thong tin tren he thong 2
        // -> Gan userId2 = 0
        Long userId2 = (user2 == null || user2.getUserId() == null) ? 0L : user2.getUserId();

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
            // lay thong tin cac don vi them vao
            List<EntityTaskApproval> listTaskApproval = new ArrayList<>();
            JSONArray jArrayRecvUser = FunctionCommon.jsonGetArray(ConstantsFieldParams.TASKSMS_TASK_APPROVED, data);
            if (jArrayRecvUser != null && jArrayRecvUser.length() > 0) {
                int lengthArray = jArrayRecvUser.length();
                for (int i = 0; i < lengthArray; i++) {
                    JSONObject jObj = (JSONObject) jArrayRecvUser.get(i);
                    if (jObj.has(ConstantsFieldParams.TASKSMS_TASK_APPROVAL_ID)) {
                        EntityTaskApproval taskApproval = new EntityTaskApproval();
                        taskApproval.setEnforcementId(Long.parseLong(jObj.getString(ConstantsFieldParams.TASKSMS_APPROVAL_ENFORCEMENT_ID).trim()));
                        taskApproval.setPeriod(jObj.getString(ConstantsFieldParams.TASKSMS_APPROVAL_PREDIO).trim());
                        taskApproval.setState(Long.parseLong(jObj.getString(ConstantsFieldParams.TASKSMS_APPROVAL_STATE).trim()));
                        taskApproval.setTaskApprovalId(Long.parseLong(jObj.getString(ConstantsFieldParams.TASKSMS_TASK_APPROVAL_ID).trim()));
                        taskApproval.setTaskId(Long.parseLong(jObj.getString(ConstantsFieldParams.TASK_ID).trim()));
                        listTaskApproval.add(taskApproval);
                    }
                }
            }

            SmsDAO smsDao = new SmsDAO();
            String result;
            if (smsDao.sendSMSMultiRecv(userId2, listTaskApproval)) {
                result = "1";
            } else {
                result = "0";
            }
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);

        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error("sendSmsMulSignAfterMonth - userId1: " + userId1
                    + " - userId2: " + userId2, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * <b> gui tin nhan sau khi thuc hien chuyen </b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String sendSmsMeetingAssistant(HttpServletRequest request, String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);

        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("editDocument - No session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Neu user hoac user id tren he thong 1&2 null
        // -> Tra ve thong bao khong co quyen
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        if ((user1 == null || user1.getUserId() == null)
                && (user2 == null || user2.getUserId() == null)) {
            LOGGER.error("editDocument - user hoac userId tren he thong 1&2 null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        Long userId1 = (user1 == null || user1.getUserId() == null) ? 0L : user1.getUserId();
        Long userId2 = (user2 == null || user2.getUserId() == null) ? 0L : user2.getUserId();
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            aesKey = userGroup.getStrAesKey();
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
                ConstantsFieldParams.DOCUMENT_ID,
                ConstantsFieldParams.MEETING_ASSISTANT_EMPLOYEE_ID,
                ConstantsFieldParams.COMMENT,
                ConstantsFieldParams.MEETING_CONFIG_SMS_USER_NAME,
                ConstantsFieldParams.MEETING_CONFIG_SMS_TITLE_DOC
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long leaderId = Long.parseLong(listValue.get(0));
            Long documentId = Long.parseLong(listValue.get(1));
            Long employeeId;
            try {
                employeeId = Long.parseLong(listValue.get(2));
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
                employeeId = 0L;
            }
            String comment = listValue.get(3);
            String leaderName = listValue.get(4);
            String titleDoc = listValue.get(5);
            SmsDAO smsDao = new SmsDAO();
            Integer result;
            if (employeeId.equals(0L)) {
                List<EntityDocumentInStaff> lstStaff = smsDao.getDocumentStaff(leaderId, documentId, 0);
                if (lstStaff.isEmpty()) {
                    result = 0;
                    return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
                } else {
                    employeeId = lstStaff.get(0).getStaffId2();
                }
            }
            // lay gia tri cua danh sach user
            List<EntityVhrEmployee> listUser = new ArrayList();
            JSONArray jArrayLstUser = FunctionCommon.jsonGetArray(ConstantsFieldParams.MEETING_CONFIG_SMS_LIST_USER, data);
            if (jArrayLstUser != null && jArrayLstUser.length() > 0) {
                int lengthArray = jArrayLstUser.length();
                for (int i = 0; i < lengthArray; i++) {
                    JSONObject jObj = (JSONObject) jArrayLstUser.get(i);
                    if (jObj.has(ConstantsFieldParams.MEETING_ASSISTANT_EMPLOYEE_ID)) {
                        EntityVhrEmployee userTemp = new EntityVhrEmployee();
                        userTemp.setEmployeeId(Long.parseLong(jObj.getString(ConstantsFieldParams.MEETING_ASSISTANT_EMPLOYEE_ID)));
                        listUser.add(userTemp);
                    }
                }
            }
            // lay gia tri cua danh sach org
            List<EntityVhrOrg> listOrg = new ArrayList();
            JSONArray jArrayLstOrg = FunctionCommon.jsonGetArray(ConstantsFieldParams.MEETING_CONFIG_SMS_LIST_ORG, data);
            if (jArrayLstOrg != null && jArrayLstOrg.length() > 0) {
                int lengthArray = jArrayLstOrg.length();
                for (int i = 0; i < lengthArray; i++) {
                    JSONObject jObj = (JSONObject) jArrayLstOrg.get(i);
                    if (jObj.has(ConstantsFieldParams.MEETING_CONFIG_ORG_ID)) {
                        EntityVhrOrg orgTemp = new EntityVhrOrg();
                        orgTemp.setSysOrganizationId(Long.parseLong(jObj.getString(ConstantsFieldParams.MEETING_CONFIG_ORG_ID)));
                        orgTemp.setAbbreviation(jObj.getString(ConstantsFieldParams.MEETING_CONFIG_SMS_ABBEREVIATION));
                        listOrg.add(orgTemp);
                    }
                }
            }
            // lay gia tri cua danh sach personal group
            List<EntityCvGroup> listGroup = new ArrayList();
            JSONArray jArrayLstCvGroup = FunctionCommon.jsonGetArray(ConstantsFieldParams.MEETING_CONFIG_SMS_LIST_GROUP, data);
            if (jArrayLstCvGroup != null && jArrayLstCvGroup.length() > 0) {
                int lengthArray = jArrayLstCvGroup.length();
                for (int i = 0; i < lengthArray; i++) {
                    JSONObject jObj = (JSONObject) jArrayLstCvGroup.get(i);
                    if (jObj.has(ConstantsFieldParams.MEETING_CONFIG_SMS_GROUP_ID)) {
                        EntityCvGroup groupTemp = new EntityCvGroup();
                        groupTemp.setGroupId(Long.parseLong(jObj.getString(ConstantsFieldParams.MEETING_CONFIG_SMS_GROUP_ID)));
                        groupTemp.setName(jObj.getString(ConstantsFieldParams.MEETING_CONFIG_SMS_NAME_GROUP));
                        listGroup.add(groupTemp);
                    }
                }
            }
            if (smsDao.sendSmsAssistantDocument(listUser, null, listOrg, listGroup, employeeId, comment, leaderName, titleDoc,
                    Constants.SMS_TEXT_INTERCEPT.SECRETARY_LEADERHANDLEDOC)) {
//                smsDao.updateDataAfterSendSms(leaderId, employeeId, documentId);
                result = 1;
            } else {
                result = 0;
            }
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);

        } catch (Exception ex) {
            LOGGER.error("editDocument - userId1: " + userId1
                    + " - userId2: " + userId2
                    + " - Exception: ", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /*
     * <b> ham kiem tra dieu kien de gui sms  </b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String checkDocumentToSendSms(HttpServletRequest request, String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);

        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("editDocument - No session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Neu user hoac user id tren he thong 1&2 null
        // -> Tra ve thong bao khong co quyen
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        if ((user1 == null || user1.getUserId() == null)
                && (user2 == null || user2.getUserId() == null)) {
            LOGGER.error("editDocument - user hoac userId tren he thong 1&2 null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        Long userId1 = (user1 == null || user1.getUserId() == null) ? 0L : user1.getUserId();
        Long userId2 = (user2 == null || user2.getUserId() == null) ? 0L : user2.getUserId();
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            aesKey = userGroup.getStrAesKey();
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
                ConstantsFieldParams.DOCUMENT_ID,
                ConstantsFieldParams.MEETING_ASSISTANT_EMPLOYEE_ID,
                ConstantsFieldParams.MEETING_CONFIG_TYPE_SMS
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long leaderId = Long.parseLong(listValue.get(0));
            Long documentId = Long.parseLong(listValue.get(1));
            Integer typeSms = 0;
            try {
                typeSms = Integer.parseInt(listValue.get(3));
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
            Long employeeId = 0L;

            try {
                employeeId = Long.parseLong(listValue.get(2));
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
                employeeId = 0L;
            }

            Integer result = 0;
            SmsDAO dao = new SmsDAO();

            if (employeeId.equals(0L)) {
                List<EntityDocumentInStaff> lstStaff;
                lstStaff = dao.getDocumentStaff(leaderId, documentId, 0);
                if (typeSms == 0 && (lstStaff == null || lstStaff.isEmpty())) {
                    result = 0;
                    return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
                } else if (typeSms == 1 && (lstStaff == null || lstStaff.isEmpty())) {
                    result = 1;
                    return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
                } else {
                    employeeId = lstStaff.get(0).getStaffId2();
                }
            }

            if (typeSms == 0) {
                if (dao.checkDocumentSendByAssistant(employeeId, leaderId, documentId) != 1) {
                    result = 0;
                } 
//                else if ((dao.checkAssistantOfLeader(employeeId, leaderId) == 1)) {
//                    result = 1;
//                }
                else {
                     result = 1;
                }
            }
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error("editDocument - userId1: " + userId1
                    + " - userId2: " + userId2
                    + " - Exception: ", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }
}
