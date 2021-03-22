/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.google.gson.Gson;
import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.document.DocumentHandoverDAO;
import com.viettel.voffice.database.entity.EntityDocument;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.User.EntityUser;
import com.viettel.voffice.database.entity.User.Vof2_EntityUser;
import com.viettel.voffice.utils.CommonUtils;
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
 * @author cuongnv
 */
public class DocumentHandoverController {

    // Ten cua class bao gom ca ten package
    private static final String CLASS_NAME = DocumentHandoverController.class.getName();
    private static final Logger LOGGER = Logger.getLogger(DocumentHandoverController.class);

    /**
     * <b>Tim kiem van ban de ban giao</b>
     *
     * @author cuongnv
     * @param request
     * @param clientData
     * @param isSecurity
     * @return
     *
     */
    public String searchDocumentHandover(HttpServletRequest request,
            String clientData, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();

        if ((user1 == null || user1.getUserId() == null)
                && (user2 == null || user2.getUserId() == null)) {
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Id nguoi dung tren he thong 2
        Long userId2 = user2 == null ? 0L : user2.getUserId();
        // Id nguoi dung tren he thong 1

        Long userId1 = null;
        if (user1 != null && user1.getUserId() != null) {
            userId1 = user1.getUserId();
        }
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        String data = clientData;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, clientData);
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
                ConstantsFieldParams.DOCUMENT_TYPE,
                ConstantsFieldParams.FROM_DATE,
                ConstantsFieldParams.TO_DATE,
                ConstantsFieldParams.TYPE,
                ConstantsFieldParams.STATUS,
                ConstantsFieldParams.SENDER_ID,
                ConstantsFieldParams.CODE,
                ConstantsFieldParams.TITLE,
                ConstantsFieldParams.DOCUMENT_SIGNER
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            //Loai van ban
            Integer documentType = -1;
            try {
                documentType = Integer.parseInt(listValue.get(0));
            } catch (NumberFormatException e) {

            }
            // Tunghd add start
            // Lay list don vi ma user co vai tro van thu tuong ung
            List<Long> lsSecretaryOrgIdVof2 = null;
            if (user2 != null) {
                lsSecretaryOrgIdVof2 = user2.getListSecretaryVhrOrg();
            }
            // Tunghd add end
            //Thoi gia nhan vb
            String fromDate = listValue.get(1);
            String toDate = listValue.get(2);
            //Trang thai van ban
            Integer type = -1;
            try {
                type = Integer.parseInt(listValue.get(3));
            } catch (NumberFormatException e) {

            }
            // Trang thai xu ly
            Integer status = -1;
            try {
                status = Integer.parseInt(listValue.get(4));
            } catch (NumberFormatException e) {

            }
            // Nguoi gui vb
            Long senderId = -1L;
            try {
                senderId = Long.parseLong(listValue.get(5));
            } catch (NumberFormatException e) {
            }
            //So hieu vb
            String code = listValue.get(6);
            //Trich yeu noi dung
            String title = listValue.get(7);
            //Nguoi ky vb
            String documentSigner = listValue.get(8);
            DocumentHandoverDAO dh = new DocumentHandoverDAO();
            List<EntityDocument> result = dh.searchDocumentHandover(userId1, userId2, documentType,
                    fromDate, toDate, type, status, senderId, code, title, documentSigner, lsSecretaryOrgIdVof2);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    /**
     * <b>Ban giao van ban</b>
     *
     * @author cuongnv
     * @param request
     * @param clientData
     * @param isSecurity
     * @return
     *
     */
    public String handOverDocument(HttpServletRequest request,
            String clientData, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();

        if ((user1 == null || user1.getUserId() == null)
                && (user2 == null || user2.getUserId() == null)) {
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Id nguoi dung tren he thong 2
        Long userId2 = user2 == null ? 0L : user2.getUserId();
        // Id nguoi dung tren he thong 1

        Long userId1 = null;
        if (user1 != null && user1.getUserId() != null) {
            userId1 = user1.getUserId();
        }
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        String data = clientData;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, clientData);
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
                ConstantsFieldParams.MEETING_ASSISTANT_EMPLOYEE_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long employeeId = Long.parseLong(listValue.get(0));
            List<EntityDocument> listDocument = new ArrayList<>();
            if (json.has(ConstantsFieldParams.DOCUMENTS)) {
                JSONArray arrayJson = json.getJSONArray(ConstantsFieldParams.DOCUMENTS);
                Gson g = new Gson();
                if (arrayJson.length() > 0) {
                    for (int i = 0; i < arrayJson.length(); i++) {
                        JSONObject obj = arrayJson.getJSONObject(i);
                        listDocument.add(g.fromJson(obj.toString(), EntityDocument.class));
                    }
                }
            }
            DocumentHandoverDAO dh = new DocumentHandoverDAO();
            Integer result = dh.handOverDocument(userId1, userId2, employeeId, listDocument);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        } catch (NumberFormatException ex) {
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * <b>Lich su ban giao </b>
     *
     * @author cuongnv
     * @param request
     * @param clientData
     * @param isSecurity
     * @return
     *
     */
    public String historyDocumentHandover(HttpServletRequest request,
            String clientData, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();

        if ((user1 == null || user1.getUserId() == null)
                && (user2 == null || user2.getUserId() == null)) {
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Id nguoi dung tren he thong 2
        Long userId2 = user2 == null ? 0L : user2.getUserId();
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        String data = clientData;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, clientData);
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
                ConstantsFieldParams.CODE,
                ConstantsFieldParams.TITLE,
                ConstantsFieldParams.FROM_DATE,
                ConstantsFieldParams.TO_DATE,
                ConstantsFieldParams.SENDER_ID,
                ConstantsFieldParams.DOCUMENT_RECEIVER_ID,
                ConstantsFieldParams.START_RECORD,
                ConstantsFieldParams.PAGE_SIZE,
                ConstantsFieldParams.IS_COUNT
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String code = listValue.get(0);
            String title = listValue.get(1);
            String fromDate = listValue.get(2);
            String toDate = listValue.get(3);
            Long senderId = null;
            try {
                senderId = Long.parseLong(listValue.get(4));
            } catch (NumberFormatException e) {
            }

            Long receiverId = null;
            try {
                receiverId = Long.parseLong(listValue.get(5));
            } catch (NumberFormatException e) {
            }
            //vi tri bat dau ban ghi
            Long startRecord = null;
            try {
                startRecord = Long.parseLong(listValue.get(6));
            } catch (NumberFormatException e) {
            }
            //So luong ban ghi tren mot trang
            Long pageSize = null;
            try {
                pageSize = Long.parseLong(listValue.get(7));
            } catch (NumberFormatException e) {
            }
            Integer isCount = 0;
            try {
                isCount = Integer.parseInt(listValue.get(8));
            } catch (NumberFormatException e) {

            }
            DocumentHandoverDAO dh = new DocumentHandoverDAO();
            Object result = dh.historyDocumentHandover(userId2, code, title, fromDate, toDate, senderId, receiverId, startRecord, pageSize, isCount);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        } catch (NumberFormatException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * <b>Thong tin chi tiet van ban da ban giao</b>
     *
     * @author HaNH
     * @param request
     * @param clientData
     * @param isSecurity
     * @return
     *
     */
    public String getDetailDocumentHandover(HttpServletRequest request,
            String clientData, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();

        if ((user1 == null || user1.getUserId() == null)
                && (user2 == null || user2.getUserId() == null)) {
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        String data = clientData;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, clientData);
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
                ConstantsFieldParams.DOCUMENT_HANDOVER_ID,
                ConstantsFieldParams.START_RECORD,
                ConstantsFieldParams.PAGE_SIZE,
                ConstantsFieldParams.IS_COUNT
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            //Long documentId = Long.parseLong(listValue.get(0));
            Long documentHandoverId = Long.parseLong(listValue.get(0));

            //vi tri bat dau ban ghi
            Long startRecord = null;
            try {
                startRecord = Long.parseLong(listValue.get(1));
            } catch (NumberFormatException e) {
            }
            //So luong ban ghi tren mot trang
            Long pageSize = null;
            try {
                pageSize = Long.parseLong(listValue.get(2));
            } catch (NumberFormatException e) {
            }
            Integer isCount = 0;
            try {
                isCount = Integer.parseInt(listValue.get(3));
            } catch (NumberFormatException e) {

            }
            DocumentHandoverDAO dh = new DocumentHandoverDAO();
            Object result = dh.getDetailDocumentHandover(documentHandoverId,
                    startRecord, pageSize, isCount);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        } catch (NumberFormatException ex) {
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * <b>Xuat bao cao danh sach van ban di den don vi</b> </b>
     *
     * @author cuongnv
     * @param request
     * @param clientData
     * @param isSecurity
     * @return
     *
     */
    public String exportReportDocument(HttpServletRequest request,
            String clientData, String isSecurity) {

        String[] keys = new String[] {
            ConstantsFieldParams.DOCUMENT,
            ConstantsFieldParams.TYPE,
            ConstantsFieldParams.NOT_IN_APPROVAL_FLOW
        };
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, clientData, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("statisticReceivedAndSentDocument - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            String strDocument = listValue.get(0);
            int type = 0;
            if (!CommonUtils.isEmpty(listValue.get(1))) {
                type = Integer.parseInt(listValue.get(1));
            }
            String isNotInApprovalFlow = null;
            if (!CommonUtils.isEmpty(listValue.get(2))) {
            	isNotInApprovalFlow = listValue.get(2);
            }
            
            EntityDocument document;
            if (CommonUtils.isEmpty(strDocument)) {
                return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
            }
            Gson gson = new Gson();
            document = gson.fromJson(strDocument, EntityDocument.class);
            DocumentHandoverDAO documentHandoverDAO = new DocumentHandoverDAO();
            Object result;
            switch (type) {
                case 0:
                    result = documentHandoverDAO.exportReportDocument(document);
                    break;
                case 1:
                    result = documentHandoverDAO.statisticReceivedAndSentDocument(
                            userGroup.getUserId2(), document);
                    break;
                case 2:
                    result = documentHandoverDAO.statisticDocumentReadingRatio(
                            userGroup.getUserId2(), document);
                    break;
                case 3:
                    result = documentHandoverDAO.statisticDetailDocumentReadingRatio(
                            userGroup.getUserId2(), document, false, isNotInApprovalFlow);
                    break;
                case 4:
                    result = documentHandoverDAO.statisticDetailDocumentReadingRatio(
                            userGroup.getUserId2(), document, true, isNotInApprovalFlow);
                    break;
                case 5:
                    result = documentHandoverDAO.exportReportDocumentMeeting(document, 
                    		userGroup.getUserId1(), userGroup.getUserId2(),
                    		userGroup.getVof2_ItemEntityUser().getListSecretaryVhrOrg());
                    break;
                case 6:
                    result = documentHandoverDAO.exportReportDocumentReply(document, 
                    		userGroup.getUserId1(), userGroup.getUserId2(),
                    		userGroup.getVof2_ItemEntityUser().getListSecretaryVhrOrg());
                    break;
                default:
                    return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);    
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            LOGGER.error("statisticReceivedAndSentDocument - Exception - username: "
                    + userGroup.getCardId(), ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }
}
