/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.google.gson.Gson;
import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.orientation.OrientationDAO;
import com.viettel.voffice.database.entity.EntityFileAttachment;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.User.Vof2_EntityUser;
import com.viettel.voffice.database.entity.task.EntityOrgReceiveOrientation;
import com.viettel.voffice.database.entity.task.EntitySourceMap;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.LogUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author SonDN
 */
public class OrientationController {

    public static final String ROOT_ACTION = "Orientation";
    /**
     * Log loi
     */
    private static final Logger logger = Logger.getLogger(OrientationController.class);

    // Ten class bao gom ca ten package
    private static final String CLASS_NAME = OrientationController.class.getName();

    /*Lay danh sach tim kiem dinh huong*/
    public String getOrientations(String isSecurity, String strData,
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
                Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
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
                hmParams.put(ConstantsFieldParams.IS_ADVANCE, Long.class);
                hmParams.put(ConstantsFieldParams.ORG_ID, Long.class);
                hmParams.put(ConstantsFieldParams.FROM_DATE, String.class);
                hmParams.put(ConstantsFieldParams.TO_DATE, String.class);
                hmParams.put(ConstantsFieldParams.EMP_ID, Long.class);
                hmParams.put(ConstantsFieldParams.ORIENTATION_FIELD_ID, Long.class);
                hmParams.put(ConstantsFieldParams.ORIENTATION_CONTENT, String.class);
                hmParams.put(ConstantsFieldParams.TYPE, Long.class);
                hmParams.put(ConstantsFieldParams.MEETING_MINUTES_ID, Long.class);
                hmParams.put(ConstantsFieldParams.DOCUMENT_ID, Long.class);
                hmParams.put(ConstantsFieldParams.IS_CHECK, Integer.class);
                hmParams.put(ConstantsFieldParams.IS_COUNT, Integer.class);
                hmParams.put(ConstantsFieldParams.INSTRUCTION_CONTENT, String.class);
                hmParams.put(ConstantsFieldParams.ORG_RECEIVER_ORIENTATION_ID, Long.class);
                hmParams.put(ConstantsFieldParams.STR_SOURCE_TYPE, Long.class);

                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);

                String strSearch = (String) valueParams.get(ConstantsFieldParams.STR_KEYWORDS);
                Long startRecord = (Long) ((valueParams.get(ConstantsFieldParams.START_RECORD) != null) ? valueParams.get(ConstantsFieldParams.START_RECORD) : 0L);
                Long pageSize = (Long) ((valueParams.get(ConstantsFieldParams.PAGE_SIZE) != null) ? valueParams.get(ConstantsFieldParams.PAGE_SIZE) : 10L);
                Long isAdvance = (Long) ((valueParams.get(ConstantsFieldParams.IS_ADVANCE) != null) ? valueParams.get(ConstantsFieldParams.IS_ADVANCE) : 0L);
                Long orgId = (Long) ((valueParams.get(ConstantsFieldParams.ORG_ID) != null) ? valueParams.get(ConstantsFieldParams.ORG_ID) : 0L);
                String fromDate = (String) valueParams.get(ConstantsFieldParams.FROM_DATE);
                String toDate = (String) valueParams.get(ConstantsFieldParams.TO_DATE);
                Long empId = (Long) (valueParams.get(ConstantsFieldParams.EMP_ID) != null ? valueParams.get(ConstantsFieldParams.EMP_ID) : 0L);
                Long fieldId = (Long) (valueParams.get(ConstantsFieldParams.MISSION_FIELD_ID) != null ? valueParams.get(ConstantsFieldParams.MISSION_FIELD_ID) : 0L);
                String content = (String) valueParams.get(ConstantsFieldParams.MISSION_CONTENT);
                Long type = (Long) (valueParams.get(ConstantsFieldParams.TYPE) != null ? valueParams.get(ConstantsFieldParams.TYPE) : null);
                Long meetingMinutesId = (Long) (valueParams.get(ConstantsFieldParams.MEETING_MINUTES_ID) != null ? valueParams.get(ConstantsFieldParams.MEETING_MINUTES_ID) : 0L);
                Long documentId = (Long) (valueParams.get(ConstantsFieldParams.DOCUMENT_ID) != null ? valueParams.get(ConstantsFieldParams.DOCUMENT_ID) : 0L);
                Integer isCheck = (Integer) ((valueParams.get(ConstantsFieldParams.IS_CHECK) != null) ? valueParams.get(ConstantsFieldParams.IS_CHECK) : 0);
                Integer isCount = (Integer) ((valueParams.get(ConstantsFieldParams.IS_COUNT) != null) ? valueParams.get(ConstantsFieldParams.IS_COUNT) : 0);
                String instructionContent = (String) valueParams.get(ConstantsFieldParams.INSTRUCTION_CONTENT);
                Long orgReceiverOrientationId = (Long) (valueParams.get(ConstantsFieldParams.ORG_RECEIVER_ORIENTATION_ID) != null ? valueParams.get(ConstantsFieldParams.ORG_RECEIVER_ORIENTATION_ID) : 0L);
                Long sourceType = (Long) (valueParams.get(ConstantsFieldParams.STR_SOURCE_TYPE) != null ? valueParams.get(ConstantsFieldParams.STR_SOURCE_TYPE) : null);
                if (CommonUtils.isEmpty(orgIdsByRole)) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
                }

                OrientationDAO orientation = new OrientationDAO();
                Object listItem = orientation.getOrientations(userId, orgIdsByRole, strSearch,
                        startRecord, pageSize, orgId, empId, fieldId, content, type, fromDate, toDate,
                        meetingMinutesId, documentId, isCheck, isCount, isAdvance, instructionContent, orgReceiverOrientationId, sourceType);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, listItem, strAesKeyDecode);
                Vof2_EntityUser itemUser = dataSessionGR.getVof2_ItemEntityUser();
                aclog.insertActionLog(itemUser.getUserId(), itemUser.getStrCardNumber(),
                        "OrientationController.getOrientations", req, "", startDate, "", "");
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

    /**
     * <b>Xoa dinh huong</b>
     *
     * @author SonDN
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String deleteOrientations(HttpServletRequest request,
            String data, String isSecurity) {
        EntityUserGroup entityUserGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!entityUserGroup.getCheckSessionOk()) {
            logger.error("deleteOrientations - No session");
            return FunctionCommon.generateResponseJSON(entityUserGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = entityUserGroup.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            logger.error("deleteOrientations - userId2 null");
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
            String[] keys = new String[]{ConstantsFieldParams.ORIENTATION_ID};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long orientationId = Long.parseLong(listValue.get(0));
            OrientationDAO m = new OrientationDAO();
            Integer result = m.deleteOrientations(orientationId, userId);
            if (result == 1) {
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
            }
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);

        } catch (JSONException | NumberFormatException ex) {
            LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_ERROR, ErrorCode.INPUT_INVALID, ex.getMessage(), data, isSecurity);
            logger.error("deleteOrientations - Exception:", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * <b>Thêm mới/sửa định hướng </b>
     *
     * @author SonDN
     * @param request
     * @param data
     * @param isSecurity
     * @since 29/11/2016
     * @return
     */
    public String addOrEditOrientations(HttpServletRequest request,
            String data, String isSecurity) {
        EntityUserGroup entityUserGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!entityUserGroup.getCheckSessionOk()) {
            logger.error("addOrEditOrientations - No session");
            return FunctionCommon.generateResponseJSON(entityUserGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = entityUserGroup.getVof2_ItemEntityUser().getUserId();
        String cardId = entityUserGroup.getVof2_ItemEntityUser().getStrCardNumber();
        Gson gson = new Gson();
        if (userId == null) {
            logger.error("addOrEditOrientations - userId2 null");
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
                ConstantsFieldParams.ORIENTATION_ID,
                ConstantsFieldParams.ORG_ID,
                ConstantsFieldParams.EMP_ID,
                ConstantsFieldParams.ORIENTATION_DATE,
                ConstantsFieldParams.ORIENTATION_FIELD_ID,
                ConstantsFieldParams.ORIENTATION_TYPE,
                ConstantsFieldParams.ORIENTATION_CONTENT
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long orientationId = null;
            try {
                orientationId = Long.parseLong(listValue.get(0));
            } catch (NumberFormatException ex) {

            }
            Long orgId = Long.parseLong(listValue.get(1));
            Long empId = Long.parseLong(listValue.get(2));
            String orientationDate = listValue.get(3);
            Long orientationFieldId = Long.parseLong(listValue.get(4));
            Integer orientationType = Integer.parseInt(listValue.get(5));
            String content = listValue.get(6);
            //Lay danh sach file dinh kem
            List<EntityFileAttachment> lstFileAttachment = new ArrayList<>();
            if (json.has(ConstantsFieldParams.LIST_FILE_ATTACH)) {
                JSONArray arrFiles = json.getJSONArray(ConstantsFieldParams.LIST_FILE_ATTACH);
                if (arrFiles.length() > 0) {
                    EntityFileAttachment tmp;
                    for (int i = 0; i < arrFiles.length(); i++) {
                        try {
                            JSONObject innerObj = arrFiles.getJSONObject(i);
                            tmp = gson.fromJson(innerObj.toString(), EntityFileAttachment.class);
                            lstFileAttachment.add(tmp);
                        } catch (JSONException ex) {
                            logger.error("addOrEditOrientations:lstFileAttachment - Exception:", ex);
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
                            logger.error("addOrEditOrientations:listSourceMap - Exception:", ex);
                        }
                    }
                }
            }

            //Lấy danh sách đơn vị nhận định hướng
            List<EntityOrgReceiveOrientation> listOrgReceive = new ArrayList<>();
            if (json.has(ConstantsFieldParams.LIST_ORG_RECEIVE_ORIENTATION)) {
                JSONArray arrOrgReceive = json.getJSONArray(ConstantsFieldParams.LIST_ORG_RECEIVE_ORIENTATION);
                if (arrOrgReceive.length() > 0) {
                    EntityOrgReceiveOrientation tmp;
                    for (int i = 0; i < arrOrgReceive.length(); i++) {

                        try {
                            JSONObject innerObj = arrOrgReceive.getJSONObject(i);
                            tmp = gson.fromJson(innerObj.toString(), EntityOrgReceiveOrientation.class);
                            listOrgReceive.add(tmp);
                        } catch (JSONException ex) {
                            logger.error("addOrEditOrientations:listOrgReceive - Exception:", ex);
                        }
                    }
                }
            }
            OrientationDAO orientation = new OrientationDAO();
            if (orientationId == null) {
                //Thêm mới định hướng
                Integer rs = orientation.addOrientations(orgId, empId, orientationDate, orientationFieldId, content, orientationType,
                        lstFileAttachment, listSourceMap, listOrgReceive, userId, cardId);
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, rs, aesKey);
            } else {
                //Sửa định hướng
                Integer rs = orientation.editOrientations(orientationId, orgId, empId, orientationDate, orientationFieldId, content, orientationType,
                        lstFileAttachment, listSourceMap, listOrgReceive, userId, cardId);
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, rs, aesKey);
            }
        } catch (JSONException | NumberFormatException ex) {
            LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_ERROR, ErrorCode.INPUT_INVALID, ex.getMessage(), data, isSecurity);
            logger.error("addOrEditOrientations - Exception:", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * <b>Lay danh sach don vi nhan dinh huong</b>
     *
     * @author SonDN
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListOrientReceiveOrgs(HttpServletRequest request,
            String data, String isSecurity) {
        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup entityUserGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!entityUserGroup.getCheckSessionOk()) {
            logger.error("getListOrientReceiveOrgs - No session");
            return FunctionCommon.generateResponseJSON(entityUserGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = entityUserGroup.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            logger.error("getListOrientReceiveOrgs - userId2 null");
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
                ConstantsFieldParams.ORIENTATION_ID,
                ConstantsFieldParams.START_RECORD,
                ConstantsFieldParams.PAGE_SIZE,
                ConstantsFieldParams.IS_COUNT
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long orientationId = Long.parseLong(listValue.get(0));
            Long startRecord = null;
            try {
                startRecord = Long.parseLong(listValue.get(1));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            Long pageSize = null;
            try {
                pageSize = Long.parseLong(listValue.get(2));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            String isCount = listValue.get(3);

            OrientationDAO orientationDAO = new OrientationDAO();
            List<EntityOrgReceiveOrientation> result = orientationDAO.getListOrientReceiveOrgs(orientationId, startRecord, pageSize, isCount);
            // Neu danh sach don vi nhan dinh huong null
            // -> Tra ve thong bao loi
            if (result == null) {
                logger.error("getListOrientReceiveOrgs - result null");
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            }
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_ERROR, ErrorCode.INPUT_INVALID, ex.getMessage(), data, isSecurity);
            logger.error("getListOrientReceiveOrgs - Exception:", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

}
