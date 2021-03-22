/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;

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
import com.viettel.voffice.database.dao.ConnectDocumentDAO;
import com.viettel.voffice.database.dao.MappingOrgDAO;
import com.viettel.voffice.database.dao.document.DocumentDAO;
import com.viettel.voffice.database.dao.document.DocumentSignDAO;
import com.viettel.voffice.database.dao.file.AttachDAO;
import com.viettel.voffice.database.dao.staff.UserDAO;
import com.viettel.voffice.database.entity.EntityConnectDocument;
import com.viettel.voffice.database.entity.EntityFileAttachment;
import com.viettel.voffice.database.entity.EntityListFields;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityTextAttachment;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.EntityUserRole;
import com.viettel.voffice.database.entity.EntityVhrEmployee;
import com.viettel.voffice.database.entity.EntityVhrOrg;
import com.viettel.voffice.database.entity.User.EntityUser;
import com.viettel.voffice.database.entity.User.Vof2_EntityUser;
import com.viettel.voffice.database.entity.calendar.MeetingApproveResult;
import com.viettel.voffice.database.entity.text.EntityText;
import com.viettel.voffice.database.entity.text.EntityTextReceiverGroup;
import com.viettel.voffice.database.entity.text.EntityTextRejectBefor;
import com.viettel.voffice.elasticsearch.connectserver.ConnectServer;
import com.viettel.voffice.elasticsearch.search.ElasticCommon;
import com.viettel.voffice.elasticsearch.search.ElasticTextControllerIndexData;
import com.viettel.voffice.elasticsearch.search.StrElasticConstants;
import com.viettel.voffice.elasticsearch.search.entity.ConfigEntity;
import com.viettel.voffice.thread.TextThread.UpdateTextListUserReject;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.EncryptDecryptSignDocument;
import com.viettel.voffice.utils.FileUtils;
import com.viettel.voffice.utils.LogUtils;

/**
 *
 * @author kiennt45
 */
@SuppressWarnings("deprecation")
public class DocumentSignController {

    // Log loi
    private static final Logger logger = Logger.getLogger(DocumentSignController.class);
    public static final String ROOT_ACTION = "DocumentService";
    // Ten cua class bao gom ca ten package
    private static final String CLASS_NAME = DocumentSignController.class.getName();

    /**
     * <b>Lay danh sach thuoc tinh</b><br>
     * <ul>
     * <li>0: tat ca</li>
     * <li>1: hinh thuc van ban</li>
     * <li>2: do mat</li>
     * <li>3: linh vuc</li>
     * <li>4: do khan</li>
     * </ul>
     *
     * @param isSecurity
     * @param data
     * @param request
     * @return
     */
    public String getListFields(String isSecurity, String data,
            HttpServletRequest request) {

        String[] keys = new String[]{
            ConstantsFieldParams.TYPE,
            ConstantsFieldParams.IS_EXPIRE,
            "orgIds"
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            logger.error("getListFields - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            Long type = 0L;
            String strType = listValue.get(0);
            if (!CommonUtils.isEmpty(strType)) {
                type = Long.parseLong(strType);
            }
            Integer isExpire = null;
            String strIsExpire = listValue.get(1);
            if (!CommonUtils.isEmpty(strIsExpire)) {
                isExpire = Integer.parseInt(strIsExpire);
            }
            
            List<Long> orgIds = null;
            if (!CommonUtils.isEmpty(listValue.get(2))) {
            	GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                String value = listValue.get(2);
                Type listType = new TypeToken<ArrayList<Long>>() {
                }.getType();
                orgIds = gson.fromJson(value, listType);
            }
            
            DocumentSignDAO documentDao = new DocumentSignDAO();
            EntityListFields result = documentDao.getListFields(type, isExpire, orgIds);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            logger.error("getListFields - Exception - username: " + userGroup.getCardId(), ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    /**
     * lay ra danh sach nganh theo linh vuc
     *
     * @param userId
     * @return
     */
    public String getListIndustry(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //lay key Aes
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String decryptedData = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(decryptedData);
                LogUtils.logFunctionalStart(log);
                //lay gia tri client gui len
                HashMap<String, Object> hmParams = new HashMap<String, Object>();
                hmParams.put(ConstantsFieldParams.AREA_ID, Long.class);

                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);

                Long type = (Long) ((valueParams.get(ConstantsFieldParams.AREA_ID) != null) ? valueParams.get(ConstantsFieldParams.AREA_ID) : 0L);

                DocumentSignDAO documentDao = new DocumentSignDAO();

                EntityListFields result = documentDao.getListIndustry(type);

                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_ERROR, ErrorCode.INPUT_INVALID, e.getMessage(), strData, isSecurity);
                logger.error(e.getMessage(), e);
//                strResult = "Error: " + e.toString();
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }

        return strResult;
    }

    /**
     * Lay ra danh sach cay don vi full VOffice 2
     *
     * @param parentId
     * @return
     */
    public String getTreeDepartSign(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String decryptedData = SecurityControler.decodeDataByAes(strAesKeyDecode,
                        strData);
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(decryptedData);
                LogUtils.logFunctionalStart(log);
                //Lay prams client gui len de thuc hien yeu cau
                HashMap<String, Object> hmParams = new HashMap<String, Object>();
                hmParams.put(ConstantsFieldParams.STR_PARENT_ID, Long.class);
                hmParams.put(ConstantsFieldParams.STATUS, String.class);
                hmParams.put(ConstantsFieldParams.KEYWORD, String.class);
                hmParams.put(ConstantsFieldParams.START_RECORD, Long.class);
                hmParams.put(ConstantsFieldParams.PAGE_SIZE, Long.class);
                hmParams.put(ConstantsFieldParams.TYPE, Long.class);
                hmParams.put(ConstantsFieldParams.IS_COUNT, Long.class);

                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);

                Long parentId = (Long) valueParams.get(ConstantsFieldParams.STR_PARENT_ID);
                String status = (String) ((valueParams.get(ConstantsFieldParams.STATUS) != null) ? valueParams.get(ConstantsFieldParams.STATUS) : "1");
                String keywords = (String) ((valueParams.get(ConstantsFieldParams.KEYWORD) != null) ? valueParams.get(ConstantsFieldParams.KEYWORD) : "");
                Long startRecord = (Long) ((valueParams.get(ConstantsFieldParams.START_RECORD) != null) ? valueParams.get(ConstantsFieldParams.START_RECORD) : 0L);
                Long pageSize = (Long) ((valueParams.get(ConstantsFieldParams.PAGE_SIZE) != null) ? valueParams.get(ConstantsFieldParams.PAGE_SIZE) : 15L);
                Long type = (Long) ((valueParams.get(ConstantsFieldParams.TYPE) != null) ? valueParams.get(ConstantsFieldParams.TYPE) : 0L);
                Long isCount = (Long) ((valueParams.get(ConstantsFieldParams.IS_COUNT) != null) ? valueParams.get(ConstantsFieldParams.IS_COUNT) : 0L);

                DocumentSignDAO documentDao = new DocumentSignDAO();

                if (isCount.equals(0L)) {
                    List<EntityVhrOrg> result = (List<EntityVhrOrg>) documentDao.getTreeDepartSign(parentId, status, keywords, type, startRecord, pageSize, isCount);
                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                } else {
                    Long result = (Long) documentDao.getTreeDepartSign(parentId, status, keywords, type, startRecord, pageSize, isCount);

                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                }

            } catch (Exception e) {
                LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_ERROR, ErrorCode.INPUT_INVALID, e.getMessage(), strData, isSecurity);
                logger.error(e.getMessage(), e);
//                strResult = "Error: " + e.toString();
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return strResult;
    }

    /**
     * Lay ra danh sach chon ca nhan theo cay don vi VOffice 2
     *
     * @param parentId
     * @return
     */
    public String getListUserSign(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);

                //Lay prams client gui len de thuc hien yeu cau
                HashMap<String, Object> hmParams = new HashMap<String, Object>();
                hmParams.put(ConstantsFieldParams.STR_PARENT_ID, Long.class);
                hmParams.put(ConstantsFieldParams.START_RECORD, Long.class);
                hmParams.put(ConstantsFieldParams.PAGE_SIZE, Long.class);
                hmParams.put(ConstantsFieldParams.STATUS, String.class);
                hmParams.put(ConstantsFieldParams.KEYWORD, String.class);
                hmParams.put(ConstantsFieldParams.TYPE, Long.class);
                hmParams.put(ConstantsFieldParams.IS_COUNT, Long.class);

                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);

                Long parentId = (Long) ((valueParams.get(ConstantsFieldParams.STR_PARENT_ID) != null) ? valueParams.get(ConstantsFieldParams.STR_PARENT_ID) : 148842L);
                Long startRecord = (Long) ((valueParams.get(ConstantsFieldParams.START_RECORD) != null) ? valueParams.get(ConstantsFieldParams.START_RECORD) : 0L);
                Long pageSize = (Long) ((valueParams.get(ConstantsFieldParams.PAGE_SIZE) != null) ? valueParams.get(ConstantsFieldParams.PAGE_SIZE) : 15L);
                String status = (String) ((valueParams.get(ConstantsFieldParams.STATUS) != null) ? valueParams.get(ConstantsFieldParams.STATUS) : "1");
                String keywords = (String) ((valueParams.get(ConstantsFieldParams.KEYWORD) != null) ? valueParams.get(ConstantsFieldParams.KEYWORD) : "");
                Long type = (Long) ((valueParams.get(ConstantsFieldParams.TYPE) != null) ? valueParams.get(ConstantsFieldParams.TYPE) : 0L);
                Long isCount = (Long) ((valueParams.get(ConstantsFieldParams.IS_COUNT) != null) ? valueParams.get(ConstantsFieldParams.IS_COUNT) : 0L);

                DocumentSignDAO documentDao = new DocumentSignDAO();

                if (isCount.equals(0L)) {
                    List<EntityVhrEmployee> result = (List<EntityVhrEmployee>) documentDao.getListUserSign(parentId, startRecord, pageSize, status, keywords, type, isCount);
                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                } else {
                    Long result = (Long) documentDao.getListUserSign(parentId, startRecord, pageSize, status, keywords, type, isCount);

                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                }
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_ERROR, ErrorCode.INPUT_INVALID, e.getMessage(), strData, isSecurity);
                logger.error(e.getMessage(), e);
//                strResult = "Error: " + e.toString();
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }

        return strResult;
    }

    /**
     * Lay ra danh sach don vi trinh ky VOffice 1
     *
     * @param
     * @return
     */
//    public String getListDepartSign(String isSecurity, String strData,
//            HttpServletRequest req) {
//        String strResult = "";
//        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
//        if (dataSessionGR.getCheckSessionOk()) {
//            try {
//                //lay gia tri tu session
//                String strAesKeyDecode = dataSessionGR.getStrAesKey();
//                Long staffId = dataSessionGR.getItemEntityUser().getUserId();
//
//                DocumentSignDAO documentDao = new DocumentSignDAO();
//                List<EntityVhrOrg> result = documentDao.getListDepartSign(staffId);
//                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
//
//            } catch (Exception e) {
//                LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_ERROR, ErrorCode.INPUT_INVALID, e.getMessage(), strData, isSecurity);
//                System.out.println("Error: " + e.toString());
//                strResult = "Error: " + e.toString();
//                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
//            }
//        } else {
//            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
//        }
//
//        return strResult;
//    }
    /**
     * <b>Them moi van ban trinh ky</b><br>
     *
     * @param isSecurity
     * @param data
     * @param request
     * @return
     */
    public String addText(String isSecurity, String data,
            HttpServletRequest request) {

        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            logger.error("addText - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        String strResult;
        DocumentSignDAO documentDao = new DocumentSignDAO();
        try {
            Long creatorIdVof1 = userGroup.getUserId1();
            Long creatorIdVof2 = userGroup.getUserId2();
            String creatorNameVof2 = userGroup.getName2();
            // Lay prams client gui len de thuc hien yeu cau
            HashMap<String, Object> hmParams = new HashMap<>();
            hmParams.put(ConstantsFieldParams.TEXT_TITLE, String.class);
            hmParams.put(ConstantsFieldParams.TEXT_REG_NUMBER, String.class);
            hmParams.put(ConstantsFieldParams.TEXT_CODE, String.class);
            hmParams.put(ConstantsFieldParams.TEXT_DESCRIPTION, String.class);
            hmParams.put(ConstantsFieldParams.TEXT_OFFICE_SENDER, String.class);
            hmParams.put(ConstantsFieldParams.TEXT_OFFICE_SENDER_ID, Long.class);
            hmParams.put(ConstantsFieldParams.TEXT_OFFICE_PUBLISHED_ID, Long.class);
            hmParams.put(ConstantsFieldParams.TEXT_TYPE_ID, Long.class);
            hmParams.put(ConstantsFieldParams.TEXT_STYPE, Long.class);
            hmParams.put(ConstantsFieldParams.TEXT_AREA, Long.class);
            hmParams.put(ConstantsFieldParams.TEXT_PRIORITY, Long.class);
            hmParams.put(ConstantsFieldParams.TEXT_IS_VOFFICE_1, Long.class);
            hmParams.put(ConstantsFieldParams.TEXT_ID, Long.class);
            hmParams.put(ConstantsFieldParams.AUTO_PUBLIC_TEXT, Long.class);
            hmParams.put(ConstantsFieldParams.AUTO_PROMULGATE_TEXT, Long.class);
            hmParams.put(ConstantsFieldParams.AUTO_SEND_TEXT, Long.class);
            hmParams.put(ConstantsFieldParams.TEXT_IS_LIENKE, Integer.class);
            //110416 bo sung them don vi tien te
            hmParams.put("unitMoney", Long.class);
            //110416 so tien chuyen
            hmParams.put("moneyTranfer", Long.class);
            hmParams.put(ConstantsFieldParams.TEXT_LST_FILE_SIGN, String.class);
            hmParams.put(ConstantsFieldParams.TEXT_LST_FILE_SIGN_OTHER, String.class);
            hmParams.put(ConstantsFieldParams.TEXT_LST_FILE_SIGN_DOC, String.class);
            hmParams.put(ConstantsFieldParams.IS_SECRET_MODE, Integer.class);
            hmParams.put("listAttachTemplate", String.class);
            hmParams.put("isNew", String.class);
            hmParams.put("textIdResign", Long.class);
            hmParams.put("isVofficeWeb", String.class);
            hmParams.put("listReceivingGroup", String.class);
            hmParams.put("textPartnerId", Long.class);
            hmParams.put("listMeeting", String.class);
            hmParams.put("isActive", Integer.class);
            // 201812-Pitagon: add
            hmParams.put("signWithCompany", Integer.class);
            hmParams.put("connectDocId", Long.class);
            
            HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, data, userGroup);
            // ID van ban
            Long textId = (Long) ((valueParams.get(ConstantsFieldParams.TEXT_ID) == null
                    || valueParams.get(ConstantsFieldParams.TEXT_ID).equals(0L))
                    ? null : valueParams.get(ConstantsFieldParams.TEXT_ID));
            // ID don vi trinh ky tren he thong 1 la ID don vi tren he thong 1 cua user
            Long officeSenderId = userGroup.getGroupId1();
            // ID don vi ban hanh tren he thong 1
            Long officePublishedId = (Long) ((valueParams.get(ConstantsFieldParams.TEXT_OFFICE_PUBLISHED_ID) != null)
                    ? valueParams.get(ConstantsFieldParams.TEXT_OFFICE_PUBLISHED_ID) : null);
            // ID hinh thuc van ban
            Long typeId = (Long) ((valueParams.get(ConstantsFieldParams.TEXT_TYPE_ID) != null)
                    ? valueParams.get(ConstantsFieldParams.TEXT_TYPE_ID) : null);
            // ID do mat van ban
            Long sTypeId = (Long) ((valueParams.get(ConstantsFieldParams.TEXT_STYPE) != null)
                    ? valueParams.get(ConstantsFieldParams.TEXT_STYPE) : null);
            // ID linh vuc
            Long areaId = (Long) ((valueParams.get(ConstantsFieldParams.TEXT_AREA) != null)
                    ? valueParams.get(ConstantsFieldParams.TEXT_AREA) : null);
            // ID do khan
            Long priorityId = (Long) ((valueParams.get(ConstantsFieldParams.TEXT_PRIORITY) != null)
                    ? valueParams.get(ConstantsFieldParams.TEXT_PRIORITY) : null);
            // Trich yeu noi dung
            String title = (String) ((valueParams.get(ConstantsFieldParams.TEXT_TITLE) != null)
                    ? valueParams.get(ConstantsFieldParams.TEXT_TITLE) : "");
            if (CommonUtils.isEmpty(title)) {
                logger.error("addText - username: " + userGroup.getCardId()
                        + " - Loi khong co tieu de van ban!");
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID,
                        null, null);
            }
            // So dang ky
            String registerNumber = (String) ((valueParams.get(ConstantsFieldParams.TEXT_REG_NUMBER) != null)
                    ? valueParams.get(ConstantsFieldParams.TEXT_REG_NUMBER) : "");
            // Ma van ban
            String code = (String) ((valueParams.get(ConstantsFieldParams.TEXT_CODE) != null)
                    ? valueParams.get(ConstantsFieldParams.TEXT_CODE) : "");
            // Noi dung
            String description = (String) ((valueParams.get(ConstantsFieldParams.TEXT_DESCRIPTION) != null)
                    ? valueParams.get(ConstantsFieldParams.TEXT_DESCRIPTION) : "");
            Long isDeleted = 0L;
            // Ten don vi trinh ky
            String officeSender = (String) ((valueParams.get(ConstantsFieldParams.TEXT_OFFICE_SENDER) != null)
                    ? valueParams.get(ConstantsFieldParams.TEXT_OFFICE_SENDER) : "");
            Long strIsVoffice1 = (Long) ((valueParams.get(ConstantsFieldParams.TEXT_IS_VOFFICE_1) != null)
                    ? valueParams.get(ConstantsFieldParams.TEXT_IS_VOFFICE_1) : 0L);
            // Tu dong cong bo van ban
            Long autoPublicText = (Long) ((valueParams.get(ConstantsFieldParams.AUTO_PUBLIC_TEXT) != null)
                    ? valueParams.get(ConstantsFieldParams.AUTO_PUBLIC_TEXT) : 0L);
            // Tu dong ban hanh van ban
            Long autoPromulgateText = (Long) ((valueParams.get(ConstantsFieldParams.AUTO_PROMULGATE_TEXT) != null)
                    ? valueParams.get(ConstantsFieldParams.AUTO_PROMULGATE_TEXT) : 0L);
            // Tu dong chuyen van ban
            Long autoSendText = (Long) ((valueParams.get(ConstantsFieldParams.AUTO_SEND_TEXT) != null)
                    ? valueParams.get(ConstantsFieldParams.AUTO_SEND_TEXT) : 0L);
            // ID don vi trinh ky tren he thong 2
            Long officeSenderId2 = userGroup.getVof2_ItemEntityUser().getAdOrgId();
            // Ten don vi trinh ky tren he thong 2
            String officeSenderName2 = userGroup.getVof2_ItemEntityUser().getAdOrgName();
            // Van ban lien ke
            Integer isLienKe = (Integer) ((valueParams.get(ConstantsFieldParams.TEXT_IS_LIENKE) != null)
                    ? valueParams.get(ConstantsFieldParams.TEXT_IS_LIENKE) : 0);
            //110416 bo sung them don vi tien te
            Long unitMoney = (Long) ((valueParams.get("unitMoney") == null) ? null : valueParams.get("unitMoney"));
            //110416 so tien chuyen
            Long moneyTranfer = (Long) ((valueParams.get("moneyTranfer") == null
                    || valueParams.get("moneyTranfer").equals(0L)) ? null : valueParams.get("moneyTranfer"));
            Integer isSecretMode = (Integer) ((valueParams.get(ConstantsFieldParams.IS_SECRET_MODE) != null)
                    ? valueParams.get(ConstantsFieldParams.IS_SECRET_MODE) : 0);
            // Chuoi file ky chinh
            String strListMainFile = (String) ((valueParams.get(ConstantsFieldParams.TEXT_LST_FILE_SIGN) != null)
                    ? valueParams.get(ConstantsFieldParams.TEXT_LST_FILE_SIGN) : "");
            // Chuoi file dinh kem            
            String strListOtherFile = (String) ((valueParams.get(ConstantsFieldParams.TEXT_LST_FILE_SIGN_OTHER) != null)
                    ? valueParams.get(ConstantsFieldParams.TEXT_LST_FILE_SIGN_OTHER) : "");
//            String strListAttachment = (String) ((valueParams.get(ConstantsFieldParams.TEXT_LST_FILE_SIGN_DOC) != null)
//                    ? valueParams.get(ConstantsFieldParams.TEXT_LST_FILE_SIGN_DOC) : "");
            Boolean isVoffice1 = strIsVoffice1.equals(1L);
            // Chuoi danh sach bieu mau dinh kem
            String strListAttachTemplate = (String) ((valueParams.get("listAttachTemplate") != null)
                    ? valueParams.get("listAttachTemplate") : "");
            Long textIdResign = (Long) ((valueParams.get("textIdResign") == null)
                    ? null : valueParams.get("textIdResign"));
            // Danh sach don vi, nhom ca nhan tu dong chuyen sau khi van ban duoc ban hanh
            String strListReceivingGroup = (String) ((valueParams.get("listReceivingGroup") != null)
                    ? valueParams.get("listReceivingGroup") : "");
            Long textPartnerId = (Long) ((valueParams.get("textPartnerId") != null)
                    ? valueParams.get("textPartnerId") : null);
            String strListMeeting = (String) ((valueParams.get("listMeeting") != null)
                    ? valueParams.get("listMeeting") : "");
            Integer isActive = (Integer) ((valueParams.get("isActive") != null)
                    ? valueParams.get("isActive") : null);
            // 201812-Pitagon: add
            Integer signWithCompany = (Integer) ((valueParams.get("signWithCompany") != null)
                    ? valueParams.get("signWithCompany") : null);
            Long connectDocId = (Long) ((valueParams.get("connectDocId") != null)
                    ? valueParams.get("connectDocId") : null);
            
            // Danh sach nguoi ky van ban
            List<Long> listUserId = new ArrayList<>();
            List<EntityUserGroup> listSigner = new ArrayList<>();
            JSONArray jsonArraySigner = FunctionCommon.jsonGetArray(ConstantsFieldParams.TEXT_LST_STAFF, userGroup.getData());
            if (jsonArraySigner != null && jsonArraySigner.length() > 0) {
                int length = jsonArraySigner.length();
                JSONObject jsonSigner;
                for (int i = 0; i < length; i++) {
                    //Get object cua tung doi tuong mang
                    jsonSigner = jsonArraySigner.getJSONObject(i);
                    Long staffId = Long.parseLong(jsonSigner.getString(ConstantsFieldParams.TEXT_STAFF_ID).trim());
                    listUserId.add(staffId);
                }
                UserDAO userDao = new UserDAO();
                listSigner = userDao.selectAndMapUser(listUserId, isVoffice1);
                Long groupId;
                String groupName;
                MappingOrgDAO mapping;
                for (int j = 0; j < length; j++) {
                    //Get object cua tung doi tuong mang
                    jsonSigner = jsonArraySigner.getJSONObject(j);
                    // thanght6-16/04/2016
                    // Luu don vi trinh ky theo client gui len
                    groupId = null;
                    groupName = null;
                    if (!jsonSigner.isNull(ConstantsFieldParams.DEPARTMENT_SIGN_ID)) {
                        String strGroupId = jsonSigner.getString(ConstantsFieldParams.DEPARTMENT_SIGN_ID);
                        if (!CommonUtils.isEmpty(strGroupId)) {
                            groupId = Long.parseLong(strGroupId);
                        }
                    }
                    if (!jsonSigner.isNull(ConstantsFieldParams.DEPARTMENT_NAME)) {
                        groupName = jsonSigner.getString(ConstantsFieldParams.DEPARTMENT_NAME);
                    }
                    if (!isVoffice1) {
                        if (listSigner.get(j).getVof2_ItemEntityUser() != null) {
                            // Thuc hien add lai don vi nguoi ky tren voffice 2.0
                            if (groupId != null) {
                                listSigner.get(j).getVof2_ItemEntityUser().setAdOrgId(groupId);
                                listSigner.get(j).getVof2_ItemEntityUser().setSysOrgId(groupId);
                                // Thuc hien mapping ten don vi 2.0 --> 1.0
                                // Map don vi 2 sang don vi 1.0            
                                mapping = new MappingOrgDAO();
                                Long orgReceiverVof1 = mapping.getGroupVof1ByMapping(groupId);

                                if (orgReceiverVof1 != null) {
//                                            EntityUser userVof1 = mapping.getExitsUserVoffice1(listStaff.get(j).getItemEntityUser().getUserId(), orgReceiverVof1);
//                                            if (userVof1 == null) {
//                                                logger.error("addText 1 - Loi trinh ky khong co thong tin tren "
//                                                        + "he thong 1 cho user: " + listStaff.get(j).getVof2_ItemEntityUser().getStrCardNumber() + "Tieu de van ban: " + docTitle + ", Nguoi tao: " + creatorId);
//                                            }
                                    // Gan lai Id don vi cua user ky tren 1.0
                                    listSigner.get(j).getItemEntityUser().setGroupId(orgReceiverVof1);
                                } else {
                                    logger.error("addText 2 - Loi trinh ky khong co thong tin tren "
                                            + "he thong 1 cho user: " + listSigner.get(j).getVof2_ItemEntityUser().getStrCardNumber() + "Tieu de van ban: " + title + ", Nguoi tao: " + creatorIdVof1);
                                }
                            }
                            // Thuc hien add lai  ten don vi nguoi ky tren voffice 2.0
                            if (!CommonUtils.isEmpty(groupName)) {
                                listSigner.get(j).getVof2_ItemEntityUser().setAdOrgName(groupName);
                            }
                        }
                    }
                }

                // Kiem tra trang thai van thu cua nguoi trinh ky
                // 030317 sua bo van thu xet duyet cho van ban DeNghiChuyenTien (DNCT)
//                List<EntityVhrEmployee> listVt = null;
                // Neu khong phai la DNCT se kiem tra co can van thu xet duyet
//                if (!DocumentSignDAO.TYPE_DOC_TRANFER_MONEY.equals(typeId)) {
//                    listVt = documentDao.buildIndexSecretary(listSigner, sTypeId);
//                }
                // Ngay 12/10 Hiendv2 bo dieu kien ngay 030217 o tren
                List<EntityVhrEmployee> listVt = documentDao.buildIndexSecretary(
                        listSigner, sTypeId);

                Long parallelSignLevel;//level ky song
                Long signLevel;
                Long signImage;
                Long signImageId;
                for (int i = 0; i < length; i++) {
                    //Get object cua tung doi tuong mang
                    jsonSigner = jsonArraySigner.getJSONObject(i);
                    signLevel = 0L;
                    signImage = null;
                    signImageId = null;
                    if (jsonSigner.has(ConstantsFieldParams.TEXT_SIGN_LEVEL)) {
                        signLevel = Long.parseLong(jsonSigner.getString(
                                ConstantsFieldParams.TEXT_SIGN_LEVEL).trim());
                    }
                    if (jsonSigner.has(ConstantsFieldParams.TEXT_SIGN_IMAGE)) {
                        signImage = Long.parseLong(jsonSigner.getString(
                                ConstantsFieldParams.TEXT_SIGN_IMAGE).trim());
                    }
                    if (jsonSigner.has(ConstantsFieldParams.SIGN_IMAGE_ID)) {
                        signImageId = Long.parseLong(jsonSigner.getString(
                                ConstantsFieldParams.SIGN_IMAGE_ID).trim());
                    }
                    // Cap nhat level ky song song
                    if (jsonSigner.has(ConstantsFieldParams.SIGN_LEVEL_PARALLEL)) {
                        parallelSignLevel = Long.parseLong(jsonSigner.getString(
                                ConstantsFieldParams.SIGN_LEVEL_PARALLEL).trim());
                        listSigner.get(i).getItemEntityUser().setParallelSignLevel(parallelSignLevel);
                    }

                    listSigner.get(i).getItemEntityUser().setSignImage(signImage);
                    listSigner.get(i).getItemEntityUser().setSignLevel(signLevel);
                    listSigner.get(i).getItemEntityUser().setSignImageId(signImageId);
                    //030317 sua bo van thu xet duyet cho van ban DeNghiChuyenTien(DNCT)
//                        if (DocumentSignDAO.TYPE_DOC_TRANFER_MONEY.equals(typeId)) {
//                            //neu la DNCT se khong qua van thu xet duyet
//                            listSigner.get(i).getItemEntityUser().setReviewLevel(2L);
//                        } else //con khong thi nhu nghiep vu cu
//                        if (listVt != null) {
//                            listSigner.get(i).getItemEntityUser().setReviewLevel(listVt.get(i).getOrganizationId());
//                        }
                    if (listVt != null) {
                        listSigner.get(i).getItemEntityUser().setReviewLevel(listVt.get(i).getOrganizationId());
                    }
                }
            }

            // Danh sach ca nhan duoc nhan van ban sau khi van ban duoc ban hanh
            List<EntityUserGroup> listReceiverAfterPromulgatedText = new ArrayList<>();
            JSONArray arrStaffSend = FunctionCommon.jsonGetArray(
                    ConstantsFieldParams.TEXT_LST_STAFF_SEND, userGroup.getData());
            if (arrStaffSend != null && arrStaffSend.length() > 0) {
                // reset lai listUserId
                listUserId.clear();
                JSONObject obj;
                Long staffId;
                for (int i = 0; i < arrStaffSend.length(); i++) {
                    // Get object cua tung doi tuong mang
                    obj = arrStaffSend.getJSONObject(i);
                    staffId = Long.parseLong(obj.getString(ConstantsFieldParams.TEXT_STAFF_ID).trim());
                    listUserId.add(staffId);
                }
                UserDAO userDao = new UserDAO();
                listReceiverAfterPromulgatedText = userDao.selectAndMapUser(listUserId, isVoffice1);
                // Gan lai sendType cho tung user
                Vof2_EntityUser user2;
                for (int i = 0; i < listReceiverAfterPromulgatedText.size(); i++) {
                    obj = arrStaffSend.getJSONObject(i);
                    if (!obj.isNull("sendType")) {
                        user2 = listReceiverAfterPromulgatedText.get(i).getVof2_ItemEntityUser();
                        if (user2 != null) {
                            user2.setSendType(obj.getInt("sendType"));
                        }
                    }
                }
            }

            // Lay danh sach file ky chinh
            List<EntityFileAttachment> listMainFile = null;
            Gson gson = new Gson();
            Type listFileAttachmentType = new TypeToken<ArrayList<EntityFileAttachment>>() {
            }.getType();
            if (!CommonUtils.isEmpty(strListMainFile)) {
                listMainFile = gson.fromJson(strListMainFile, listFileAttachmentType);
            }

            // Lay danh sach file ky khac
            List<EntityFileAttachment> listOtherFile = null;
            if (!CommonUtils.isEmpty(strListOtherFile)) {
                listOtherFile = gson.fromJson(strListOtherFile, listFileAttachmentType);
            }

            // Lay danh sach cong van dinh kem
            List<EntityTextAttachment> listAttachment = new ArrayList<>();
            JSONArray jsonArrayDoc = FunctionCommon.jsonGetArray(
                    ConstantsFieldParams.TEXT_LST_FILE_SIGN_DOC, userGroup.getData());
            if (jsonArrayDoc != null && jsonArrayDoc.length() > 0) {
                Long docId;
                Long docInStaffId;
                JSONObject jsonDoc;
                EntityTextAttachment textAttachment;
                for (int i = 0; i < jsonArrayDoc.length(); i++) {
                    jsonDoc = jsonArrayDoc.getJSONObject(i);
                    docId = 0L;
                    docInStaffId = null;
                    if (jsonDoc.has(ConstantsFieldParams.TEXT_DOC_ID)) {
                        docId = Long.parseLong(jsonDoc.getString(ConstantsFieldParams.TEXT_DOC_ID).trim());
                    }
                    if (jsonDoc.has(ConstantsFieldParams.TEXT_DOC_IN_STAFF)) {
                        docInStaffId = Long.parseLong(jsonDoc.getString(ConstantsFieldParams.TEXT_DOC_IN_STAFF).trim());
                    }
                    textAttachment = new EntityTextAttachment();
                    textAttachment.setDocInStaffId(docInStaffId);
                    textAttachment.setTextAttachId(docId);
                    listAttachment.add(textAttachment);
                }
            }

            // Lay danh sach file bieu mau
            List<EntityFileAttachment> listAttachTemplate = null;
            if (!CommonUtils.isEmpty(strListAttachTemplate)) {
                listAttachTemplate = gson.fromJson(strListAttachTemplate, listFileAttachmentType);
            }
            // Lay danh sach don vi/nhom ca nhan nhan van ban sau khi ban hanh
            Type listTextReceiverGroupType = new TypeToken<ArrayList<EntityTextReceiverGroup>>() {
            }.getType();
            List<EntityTextReceiverGroup> listReceivingGroup = null;
            if (!CommonUtils.isEmpty(strListReceivingGroup)) {
                listReceivingGroup = gson.fromJson(strListReceivingGroup, listTextReceiverGroupType);
            }
            // Danh sach cuoc hop
            Type meetingListType = new TypeToken<ArrayList<MeetingApproveResult>>() {
            }.getType();
            List<MeetingApproveResult> listMeeting = null;
            if (!CommonUtils.isEmpty(strListMeeting)) {
                listMeeting = gson.fromJson(strListMeeting, meetingListType);
            }
            //datnv5: check dieu kien add text va dua ra canh bao 
            //neu co van ban bi tu choi trung noi dung
            List<EntityFileAttachment> listMainFileCheck = listMainFile;
            if (textId != null && !textId.equals(0L)
                    && (listMainFileCheck == null || listMainFileCheck.size() <= 0)) {
                //truong hop chinh sua khong dinh lai file ky chinh
                //thuc hien lay lai file ky chinh
                AttachDAO attachDAO = new AttachDAO();
                listMainFileCheck = attachDAO.getListMainSigningFile(textId);
            } else {
                //trong truong hop client chon file moi
                String strStorageTemWeb = FunctionCommon.getPropertiesValue("storageName_saveFileTmp");
                for (int i = 0; i < listMainFileCheck.size(); i++) {
                    if (!listMainFileCheck.get(i).isCopy()) {
                        listMainFileCheck.get(i).setStorage(strStorageTemWeb);
                    }
                }
            }
            List<Long> listIdTextReject1 = checkHeaderTextReject(request, creatorIdVof2, 
                    title, description, areaId.toString(), priorityId.toString(), typeId.toString(), 
                    sTypeId.toString(), code, listSigner);
            List<Long> listIdTextReject2 = checkExitsTextReject(
                    request, title, registerNumber, code,
                    description, creatorIdVof2, listMainFileCheck);
            List<Long> listIdTextReject = new ArrayList<>();
            if(listIdTextReject1 != null){
                listIdTextReject.addAll(listIdTextReject1);
            }
            if(listIdTextReject2 != null){
                listIdTextReject.addAll(listIdTextReject2);
            }
            
            // 201812-Pitagon: Lay danh sach don vi dong dau
            List<EntityVhrOrg> orgMarkList = new ArrayList<EntityVhrOrg>();
            JSONArray arrOrgMarkList = FunctionCommon.jsonGetArray("orgMarkList", userGroup.getData());
            if (arrOrgMarkList != null && arrOrgMarkList.length() > 0) {
                JSONObject obj;
                EntityVhrOrg org;
                for (int i = 0; i < arrOrgMarkList.length(); i++) {
                    obj = arrOrgMarkList.getJSONObject(i);
                    org = gson.fromJson(obj.toString(), EntityVhrOrg.class);
                    orgMarkList.add(org);
                }
            }
            
            boolean addText;
            String isNew = (String) ((valueParams.get("isNew") != null)
                    ? valueParams.get("isNew") : "");
            //neu la truong hop add vao cua web
            String isVofficeWeb = (String) ((valueParams.get("isVofficeWeb") != null)
                    ? valueParams.get("isVofficeWeb") : "");
            String strReturnWhenAlert = "";
            if (textId == null || textId.equals(0L)) {
                if ("1".equals(isNew.trim()) || !"1".equals(isVofficeWeb)) {
                    addText = true;
                } else if (listIdTextReject.size() > 0) {
                    //truong hop co van ban trinh ky lai trong csdl
                    List<EntityTextRejectBefor> listTextRejectBefor
                            = documentDao.getListTextReject(userGroup, listIdTextReject);
                    if (listTextRejectBefor != null && listTextRejectBefor.size() > 0) {
                        addText = false;
                        EntityText entityText = new EntityText();
                        entityText.setStatus(0L);
                        entityText.setLstTextRejectBefor(listTextRejectBefor);
//                        System.out.println("strReturnWhenAlert:"
//                                + listTextRejectBefor.get(0).getTextId()
//                                + "   //  " + listTextRejectBefor.get(0).getTitle());
                        strReturnWhenAlert = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                                entityText, userGroup.getStrAesKey());
                    } else {
                        addText = true;
                    }
                } else {
                    addText = true;
                }
                if (addText) {
                    Long result = documentDao.addText(title, registerNumber,
                            code, description, typeId, sTypeId, areaId,
                            priorityId, officeSender, officeSenderId, creatorIdVof1,
                            creatorIdVof2, officePublishedId, isDeleted, isVoffice1,
                            listSigner, listMainFile, listOtherFile,
                            listAttachment, officeSenderId2, officeSenderName2,
                            autoPublicText, autoPromulgateText, autoSendText,
                            listReceiverAfterPromulgatedText, unitMoney, moneyTranfer,
                            creatorNameVof2, isLienKe, null, listAttachTemplate,
                            isSecretMode,textIdResign, listReceivingGroup,
                            textPartnerId, null , listMeeting, isActive,
                            signWithCompany, orgMarkList);
                    EntityText entityText = new EntityText();
                    entityText.setStatus(1L);
                    entityText.setTextId(result);
                    // Nghiep vu xu ly doi voi van ban duoc tao tu van ban VPCP
                    if (connectDocId != null && result != null && result.intValue() > 0) {
                        ConnectDocumentDAO connectDocDAO = new ConnectDocumentDAO();
                        EntityConnectDocument obj = new EntityConnectDocument();
                        obj.setConnectType(Constants.CONNECT_DOCUMENT.DOC_IN);
                        List<EntityConnectDocument> listDoc = (List<EntityConnectDocument>) connectDocDAO.getListConnectDocument(obj,
                                null, null, false, connectDocId);
                        if (!CommonUtils.isEmpty(listDoc)) {
                            EntityConnectDocument connectDoc = listDoc.get(0);
                            connectDocDAO.updateConnectDoc(connectDocId, null, result);
                            boolean checkFinish = connectDocDAO.checkFinishDocProcessIn(connectDocId);
                            if (connectDoc.getDocumentId() == null && connectDoc.getTextId() == null && !checkFinish) {
                                connectDocDAO.addStateConnectDocument(connectDoc.getDocId(), Constants.CONNECT_DOCUMENT.PROCESS_TYPE.PROCESSING,
                                        Constants.CONNECT_DOCUMENT.DOC_IN, null, null, null);
                            }
                        }
                    }
                    if ("1".equals(isVofficeWeb)) {
                        strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                                entityText, userGroup.getStrAesKey());
                    } else {
                        strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                                result, userGroup.getStrAesKey());
                    }
                } else {
                    strResult = strReturnWhenAlert;
                }
            } else {
                //truong hop chinh sua lai file
                if ("1".equals(isNew.trim()) || !"1".equals(isVofficeWeb)) {
                    addText = true;
                } else if (listIdTextReject.size() > 0) {
                    //truong hop co van ban trinh ky lai trong csdl
                    List<EntityTextRejectBefor> listTextRejectBefor
                            = documentDao.getListTextReject(userGroup, listIdTextReject);
                    if (listTextRejectBefor != null && listTextRejectBefor.size() > 0) {
                        addText = false;
                        EntityText entityText = new EntityText();
                        entityText.setStatus(0L);
                        entityText.setLstTextRejectBefor(listTextRejectBefor);
                        strReturnWhenAlert = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                                entityText, userGroup.getStrAesKey());
                    } else {
                        addText = true;
                    }
                } else {
                    addText = true;
                }

                if (addText) {
                    Integer result = documentDao.editText(textId, title,
                            registerNumber, code, description, typeId,
                            sTypeId, areaId, priorityId, officeSender, officeSenderId,
                            creatorIdVof1, officePublishedId, isDeleted, isVoffice1,
                            listSigner, listMainFile, listOtherFile,
                            listAttachment, autoPublicText, autoPromulgateText,
                            autoSendText, listReceiverAfterPromulgatedText, unitMoney,
                            moneyTranfer, creatorIdVof2, creatorNameVof2,
                            listAttachTemplate, isSecretMode, textIdResign, 
                            listReceivingGroup, listMeeting, signWithCompany, orgMarkList);
                    EntityText entityText = new EntityText();
                    entityText.setTextId(textId);
                    if (result == 1) {
                        entityText.setStatus(1L);
                    } else {
                        entityText.setStatus(0L);
                    }
                    if("1".equals(isVofficeWeb)){
                        strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                                entityText, userGroup.getStrAesKey());
                    }else{
                         strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                                result, userGroup.getStrAesKey());
                    }
                } else {
                    strResult = strReturnWhenAlert;
                }
            }
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(userGroup.getKpiLog());
        } catch (JSONException | NumberFormatException | JsonSyntaxException ex) {
            logger.error("addText - username: " + userGroup.getCardId()
                    + " - Exception!", ex);
            strResult = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
        return strResult;
    }
    
    /**
     * Them moi van ban trinh ky
     *
     * @param isSecurity
     * @param strData
     * @param req
     * @return
     */
    public String resignText(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        DocumentSignDAO documentDao = new DocumentSignDAO();

        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //lay gia tri tu session
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String decryptedData = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                Long creatorIdVof1 = 0L;
                if (dataSessionGR.getItemEntityUser() != null) {
                    creatorIdVof1 = dataSessionGR.getItemEntityUser().getUserId();
                }

                Long creatorIdVof2 = dataSessionGR.getVof2_ItemEntityUser().getUserId();
                String creatorNameVof2 = dataSessionGR.getVof2_ItemEntityUser().getFullName();
                String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);

                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(decryptedData);
                LogUtils.logFunctionalStart(log);
                //Lay prams client gui len de thuc hien yeu cau
                HashMap<String, Object> hmParams = new HashMap<String, Object>();
                hmParams.put(ConstantsFieldParams.TEXT_TITLE, String.class);
                hmParams.put(ConstantsFieldParams.TEXT_REG_NUMBER, String.class);
                hmParams.put(ConstantsFieldParams.TEXT_CODE, String.class);
                hmParams.put(ConstantsFieldParams.TEXT_DESCRIPTION, String.class);
                hmParams.put(ConstantsFieldParams.TEXT_OFFICE_SENDER, String.class);
                hmParams.put(ConstantsFieldParams.TEXT_OFFICE_SENDER_ID, Long.class);
                hmParams.put(ConstantsFieldParams.TEXT_OFFICE_PUBLISHED_ID, Long.class);
                hmParams.put(ConstantsFieldParams.TEXT_TYPE_ID, Long.class);
                hmParams.put(ConstantsFieldParams.TEXT_STYPE, Long.class);
                hmParams.put(ConstantsFieldParams.TEXT_AREA, Long.class);
                hmParams.put(ConstantsFieldParams.TEXT_PRIORITY, Long.class);
                hmParams.put(ConstantsFieldParams.TEXT_IS_VOFFICE_1, Long.class);
                hmParams.put(ConstantsFieldParams.TEXT_ID, Long.class);
                hmParams.put(ConstantsFieldParams.AUTO_PUBLIC_TEXT, Long.class);
                hmParams.put(ConstantsFieldParams.AUTO_PROMULGATE_TEXT, Long.class);
                hmParams.put(ConstantsFieldParams.AUTO_SEND_TEXT, Long.class);
                hmParams.put(ConstantsFieldParams.TEXT_IS_LIENKE, Integer.class);
                //110416 bo sung them don vi tien te
                hmParams.put("unitMoney", Long.class);
                //110416 so tien chuyen
                hmParams.put("moneyTranfer", Long.class);
                hmParams.put("listAttachTemplate", String.class);
                hmParams.put("listReceivingGroup", String.class);
                hmParams.put("listMeeting", String.class);
                hmParams.put(ConstantsFieldParams.TEXT_LST_FILE_SIGN_OTHER, String.class);
                hmParams.put("isActive", Integer.class);
                // 201812-Pitagon: add
                hmParams.put("signWithCompany", Integer.class);
                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
                Long textId = (Long) ((valueParams.get(ConstantsFieldParams.TEXT_ID) != null) ? valueParams.get(ConstantsFieldParams.TEXT_ID) : null);
                Long officeSenderId = 0L;
                if (dataSessionGR.getItemEntityUser() != null) {
                    officeSenderId = dataSessionGR.getItemEntityUser().getGroupId();
                }

                Long officePublishedId = (Long) ((valueParams.get(ConstantsFieldParams.TEXT_OFFICE_PUBLISHED_ID) != null) ? valueParams.get(ConstantsFieldParams.TEXT_OFFICE_PUBLISHED_ID) : null);
                Long typeId = (Long) ((valueParams.get(ConstantsFieldParams.TEXT_TYPE_ID) != null) ? valueParams.get(ConstantsFieldParams.TEXT_TYPE_ID) : null);
                Long sTypeId = (Long) ((valueParams.get(ConstantsFieldParams.TEXT_STYPE) != null) ? valueParams.get(ConstantsFieldParams.TEXT_STYPE) : null);              //Độ mật
                Long areaId = (Long) ((valueParams.get(ConstantsFieldParams.TEXT_AREA) != null) ? valueParams.get(ConstantsFieldParams.TEXT_AREA) : null);
                Long priorityId = (Long) ((valueParams.get(ConstantsFieldParams.TEXT_PRIORITY) != null) ? valueParams.get(ConstantsFieldParams.TEXT_PRIORITY) : null);
                String docTitle = (String) ((valueParams.get(ConstantsFieldParams.TEXT_TITLE) != null) ? valueParams.get(ConstantsFieldParams.TEXT_TITLE) : "");
                String registerNumber = (String) ((valueParams.get(ConstantsFieldParams.TEXT_REG_NUMBER) != null) ? valueParams.get(ConstantsFieldParams.TEXT_REG_NUMBER) : "");
                String code = (String) ((valueParams.get(ConstantsFieldParams.TEXT_CODE) != null) ? valueParams.get(ConstantsFieldParams.TEXT_CODE) : "");
                String description = (String) ((valueParams.get(ConstantsFieldParams.TEXT_DESCRIPTION) != null) ? valueParams.get(ConstantsFieldParams.TEXT_DESCRIPTION) : "");
                Long isDeleted = 0L;
                String officeSender = (String) ((valueParams.get(ConstantsFieldParams.TEXT_OFFICE_SENDER) != null) ? valueParams.get(ConstantsFieldParams.TEXT_OFFICE_SENDER) : "");
                Long autoPublicText = (Long) ((valueParams.get(ConstantsFieldParams.AUTO_PUBLIC_TEXT) != null) ? valueParams.get(ConstantsFieldParams.AUTO_PUBLIC_TEXT) : 0L);
                Long autoPromulgateText = (Long) ((valueParams.get(ConstantsFieldParams.AUTO_PROMULGATE_TEXT) != null) ? valueParams.get(ConstantsFieldParams.AUTO_PROMULGATE_TEXT) : 0L);
                Long autoSendText = (Long) ((valueParams.get(ConstantsFieldParams.AUTO_SEND_TEXT) != null) ? valueParams.get(ConstantsFieldParams.AUTO_SEND_TEXT) : 0L);
                Long officeSenderId2 = dataSessionGR.getVof2_ItemEntityUser().getAdOrgId();
                String officeSenderName2 = dataSessionGR.getVof2_ItemEntityUser().getAdOrgName();
                Long strIsVoffice1 = (Long) ((valueParams.get(ConstantsFieldParams.TEXT_IS_VOFFICE_1) != null) ? valueParams.get(ConstantsFieldParams.TEXT_IS_VOFFICE_1) : 0L);
                Integer isLienKe = (Integer) ((valueParams.get(ConstantsFieldParams.TEXT_IS_LIENKE) != null) ? valueParams.get(ConstantsFieldParams.TEXT_IS_LIENKE) : 0);
                Boolean isVoffice1;
                if (strIsVoffice1.equals(1L)) {
                    isVoffice1 = true;
                } else {
                    isVoffice1 = false;
                }
                //110416 bo sung them don vi tien te
                Long unitMoney = (Long) ((valueParams.get("unitMoney") == null) ? null : valueParams.get("unitMoney"));
                //110416 so tien chuyen
                Long moneyTranfer = (Long) ((valueParams.get("moneyTranfer") == null
                        || valueParams.get("moneyTranfer").equals(0L)) ? null : valueParams.get("moneyTranfer"));
                // Chuoi danh sach file bieu mau dinh kem
                String strListAttachTemplate = (String) (valueParams.get("listAttachTemplate") == null
                        ? "" : valueParams.get("listAttachTemplate"));
                String strListReceivingGroup = (String) (valueParams.get("listReceivingGroup") == null
                        ? "" : valueParams.get("listReceivingGroup"));
                String strListMeeting = (String) (valueParams.get("listMeeting") == null
                        ? "" : valueParams.get("listMeeting"));
                // Chuoi file dinh kem
                String strListOtherFile = (String) ((valueParams.get(ConstantsFieldParams.TEXT_LST_FILE_SIGN_OTHER) != null)
                    ? valueParams.get(ConstantsFieldParams.TEXT_LST_FILE_SIGN_OTHER) : "");
                Integer isActive = (Integer) ((valueParams.get("isActive") != null)
                    ? valueParams.get("isActive") : null);
                // 201812-Pitagon: add
                Integer signWithCompany = (Integer) ((valueParams.get("signWithCompany") != null)
                        ? valueParams.get("signWithCompany") : null);
                
                //Lay danh sach ca nhan trinh ky gui len
                List<Long> listUserId = new ArrayList<>();
                List<EntityUserGroup> listStaff = new ArrayList<EntityUserGroup>();
                //Danh sach ca nhan ban hanh
                List<EntityUserGroup> listStaffSend = new ArrayList<EntityUserGroup>();
                JSONArray arrStaff = FunctionCommon.jsonGetArray(ConstantsFieldParams.TEXT_LST_STAFF, strDataClient);
                JSONArray arrStaffSend = FunctionCommon.jsonGetArray(ConstantsFieldParams.TEXT_LST_STAFF_SEND, strDataClient);

                if (arrStaff != null && arrStaff.length() > 0) {
                    JSONObject jsonStaff;
                    int length = arrStaff.length();
                    for (int i = 0; i < length; i++) {
                        //Get object cua tung doi tuong mang
                        jsonStaff = (JSONObject) arrStaff.get(i);

                        Long staffId = Long.parseLong(jsonStaff.getString(ConstantsFieldParams.TEXT_STAFF_ID).trim());

                        listUserId.add(staffId);
                    }

                    UserDAO userDao = new UserDAO();
                    listStaff = userDao.selectAndMapUser(listUserId, isVoffice1);

                    // Kiem tra danh sach nguoi trinh ky
//                    Long userId2 = null;
//                    for (EntityUserGroup userGroup : listStaff) {
//                        // Nguoi duoc trinh ky khong co tren he thong 1
//                        if (userGroup.getItemEntityUser() == null) {
//                            // Lay id nguoi duoc trinh ky tren he thong 2
//                            if (userGroup.getVof2_ItemEntityUser() != null) {
//                                userId2 = userGroup.getVof2_ItemEntityUser().getUserId();
//                            }
//                            logger.error("resignText - Loi khong co thong tin tren "
//                                    + "he thong 1 cho user: " + userId2);
//                            return FunctionCommon.generateResponseJSON(
//                                    ErrorCode.TEXT_NOT_EXIST_SIGNER, null, null);
//                        }
//                    }
                    Long groupId;
                    String groupName;
                    MappingOrgDAO mapping;
                    for (int j = 0; j < length; j++) {
                        //Get object cua tung doi tuong mang
                        jsonStaff = arrStaff.getJSONObject(j);
                        // thanght6-16/04/2016
                        // Luu don vi trinh ky theo client gui len
                        groupId = null;
                        groupName = null;
                        if (!jsonStaff.isNull(ConstantsFieldParams.DEPARTMENT_SIGN_ID)) {
                            String strGroupId = jsonStaff.getString(ConstantsFieldParams.DEPARTMENT_SIGN_ID);
                            if (!CommonUtils.isEmpty(strGroupId)) {
                                groupId = Long.parseLong(strGroupId);
                            }
                        }
                        if (!jsonStaff.isNull(ConstantsFieldParams.DEPARTMENT_NAME)) {
                            groupName = jsonStaff.getString(ConstantsFieldParams.DEPARTMENT_NAME);
                        }
                        if (!isVoffice1) {
                            if (listStaff.get(j).getVof2_ItemEntityUser() != null) {
                                if (groupId != null) {
                                    //Thuc hien add lai don vi nguoi ky tren voffice 2.0
                                    listStaff.get(j).getVof2_ItemEntityUser().setAdOrgId(groupId);
                                    listStaff.get(j).getVof2_ItemEntityUser().setSysOrgId(groupId);
                                    //Thuc hien mapping ten don vi 2.0 --> 1.0
                                    // mapp don vi 2 sang don vi 1.0            
                                    mapping = new MappingOrgDAO();
                                    Long orgReceiverVof1 = mapping.getGroupVof1ByMapping(groupId);

                                    if (orgReceiverVof1 != null) {
//                                            EntityUser userVof1 = mapping.getExitsUserVoffice1(listStaff.get(j).getItemEntityUser().getUserId(), orgReceiverVof1);
//                                            if (userVof1 == null) {
//                                                logger.error("addText 1 - Loi trinh ky khong co thong tin tren "
//                                                        + "he thong 1 cho user: " + listStaff.get(j).getVof2_ItemEntityUser().getStrCardNumber() + "Tieu de van ban: " + docTitle + ", Nguoi tao: " + creatorId);
//                                            }
                                        //Gan lai Id don vi cua user ky tren 1.0
                                        listStaff.get(j).getItemEntityUser().setGroupId(orgReceiverVof1);
                                    } else {
                                        logger.error("trinh ky lai 2 - Loi trinh ky khong co thong tin tren "
                                                + "he thong 1 cho user: "
                                                + listStaff.get(j).getVof2_ItemEntityUser().getStrCardNumber()
                                                + "Tieu de van ban: " + docTitle + ", Nguoi tao: " + creatorIdVof1);

                                    }
                                }
                                if (!CommonUtils.isEmpty(groupName)) {
                                    //Thuc hien add lai  ten don vi nguoi ky tren voffice 2.0
                                    listStaff.get(j).getVof2_ItemEntityUser().setAdOrgName(groupName);
                                }
                            }

                        }
                    }
                    //kiểm tra trạng thái văn thư của người trình ký
                    //030317 sua bo van thu xet duyet cho van ban DeNghiChuyenTien(DNCT)
//                    List<EntityVhrEmployee> listVt = null;
//                    if (!DocumentSignDAO.TYPE_DOC_TRANFER_MONEY.equals(typeId)) {
//                        //neu khong phai la DNCT se kiem tra co can van thu xet duyet
//                        listVt = documentDao.buildIndexSecretary(listStaff, sTypeId);
//                    }
                    List<EntityVhrEmployee> listVt = documentDao.buildIndexSecretary(
                            listStaff, sTypeId);
                    Long parallelSignLevel;//level ky song
                    Long signImageId;
                    for (int i = 0; i < arrStaff.length(); i++) {
                        //Get object cua tung doi tuong mang
                        jsonStaff = (JSONObject) arrStaff.get(i);

                        Long signLevel = 0L;
                        Long signImage = null;
                        signImageId = null;

//                        signLevel = (Long) ((innerObj.get(ConstantsFieldParams.TEXT_SIGN_LEVEL) != null) ? innerObj.get(ConstantsFieldParams.TEXT_SIGN_LEVEL) : 0L);
//                        signImage = (Long) ((innerObj.get(ConstantsFieldParams.TEXT_SIGN_IMAGE) != null) ? innerObj.get(ConstantsFieldParams.TEXT_SIGN_IMAGE) : null);
                        if (jsonStaff.has(ConstantsFieldParams.TEXT_SIGN_LEVEL)) {
                            signLevel = Long.parseLong(jsonStaff.getString(ConstantsFieldParams.TEXT_SIGN_LEVEL).trim());
                        }

                        if (jsonStaff.has(ConstantsFieldParams.TEXT_SIGN_IMAGE)) {
                            signImage = Long.parseLong(jsonStaff.getString(ConstantsFieldParams.TEXT_SIGN_IMAGE).trim());
                        }

                        if (jsonStaff.has(ConstantsFieldParams.SIGN_IMAGE_ID)) {
                            signImageId = Long.parseLong(jsonStaff.getString(
                                    ConstantsFieldParams.SIGN_IMAGE_ID).trim());
                        }

                        //cap nhat level ky song song
//                        parallelSignLevel = null;
                        if (jsonStaff.has(ConstantsFieldParams.SIGN_LEVEL_PARALLEL)) {
                            parallelSignLevel = Long.parseLong(jsonStaff.getString(ConstantsFieldParams.SIGN_LEVEL_PARALLEL).trim());
                            listStaff.get(i).getItemEntityUser().setParallelSignLevel(parallelSignLevel);
                        }

                        listStaff.get(i).getItemEntityUser().setSignImage(signImage);
                        listStaff.get(i).getItemEntityUser().setSignLevel(signLevel);
                        listStaff.get(i).getItemEntityUser().setSignImageId(signImageId);
                        //030317 sua bo van thu xet duyet cho van ban DeNghiChuyenTien(DNCT)
//                        if (DocumentSignDAO.TYPE_DOC_TRANFER_MONEY.equals(typeId)) {
//                            //neu la DNCT se khong qua van thu xet duyet
//                            listStaff.get(i).getItemEntityUser().setReviewLevel(2L);
//                        } else {
//                            //con khong thi nhu nghiep vu cu
//                            if (listVt != null) {
//                                listStaff.get(i).getItemEntityUser().setReviewLevel(listVt.get(i).getOrganizationId());
//                            }
//                        }
                        if (listVt != null) {
                            listStaff.get(i).getItemEntityUser().setReviewLevel(listVt.get(i).getOrganizationId());
                        }

                    }
                }

                if (arrStaffSend != null && arrStaffSend.length() > 0) {
                    //reset lai listUserId
                    listUserId.clear();
                    JSONObject obj;
                    for (int i = 0; i < arrStaffSend.length(); i++) {
                        // Get object cua tung doi tuong mang
                        obj = arrStaffSend.getJSONObject(i);
                        Long staffId = Long.parseLong(obj.getString(
                                ConstantsFieldParams.TEXT_STAFF_ID).trim());
                        listUserId.add(staffId);
                    }

                    UserDAO userDao = new UserDAO();
                    listStaffSend = userDao.selectAndMapUser(listUserId, isVoffice1);
                    // Gan lai sendType cho tung user
                    Vof2_EntityUser user2;
                    for (int i = 0; i < listStaffSend.size(); i++) {
                        obj = arrStaffSend.getJSONObject(i);
                        if (!obj.isNull("sendType")) {
                            user2 = listStaffSend.get(i).getVof2_ItemEntityUser();
                            if (user2 != null) {
                                user2.setSendType(obj.getInt("sendType"));
                            }
                        }
                    }
                }
                //Lay danh sach file ky chinh
                List<EntityFileAttachment> strListFileSign = new ArrayList<EntityFileAttachment>();
                JSONArray arrFileSign = FunctionCommon.jsonGetArray(ConstantsFieldParams.TEXT_LST_FILE_SIGN, strDataClient);

                if (arrFileSign != null && arrFileSign.length() > 0) {
                    for (int i = 0; i < arrFileSign.length(); i++) {
                        JSONObject innerObj = (JSONObject) arrFileSign.get(i);

                        Long fileAttachmentId = null;
                        String name = "";
                        String filePath = "";

                        if (innerObj.has(ConstantsFieldParams.TEXT_ATTACH_ID)) {
                            fileAttachmentId = Long.parseLong(innerObj.getString(ConstantsFieldParams.TEXT_ATTACH_ID).trim());
                        }

                        if (innerObj.has(ConstantsFieldParams.TEXT_ATTACH_NAME)) {
                            name = innerObj.getString(ConstantsFieldParams.TEXT_ATTACH_NAME).trim();
                        }

                        if (innerObj.has(ConstantsFieldParams.TEXT_ATTACH_PATH)) {
                            filePath = innerObj.getString(ConstantsFieldParams.TEXT_ATTACH_PATH).trim();
                        }

                        EntityFileAttachment file = new EntityFileAttachment();
                        file.setFilePath(filePath);
                        file.setName(name);
                        file.setFileAttachmentId(fileAttachmentId);
                        strListFileSign.add(file);
                    }
                }                
                // Lay danh sach file ky khac
                Gson gson = new Gson();
                Type listFileAttachmentType = new TypeToken<ArrayList<EntityFileAttachment>>() {
                }.getType();
                List<EntityFileAttachment> listOtherFile = null;
                if (!CommonUtils.isEmpty(strListOtherFile)) {
                    listOtherFile = gson.fromJson(strListOtherFile, listFileAttachmentType);
                }
                
                // Lay danh sach file trinh ky tu cong van
                List<EntityTextAttachment> strListFileSignDoc = new ArrayList<>();
                JSONArray arrFileSignDoc = FunctionCommon.jsonGetArray(ConstantsFieldParams.TEXT_LST_FILE_SIGN_DOC, strDataClient);

                if (arrFileSignDoc != null && arrFileSignDoc.length() > 0) {
                    for (int i = 0; i < arrFileSignDoc.length(); i++) {
                        JSONObject innerObj = (JSONObject) arrFileSignDoc.get(i);

                        Long docId = 0L;
                        Long docInStaffId = null;

                        if (innerObj.has(ConstantsFieldParams.TEXT_DOC_ID)) {
                            docId = Long.parseLong(innerObj.getString(ConstantsFieldParams.TEXT_DOC_ID).trim());
                        }

                        if (innerObj.has(ConstantsFieldParams.TEXT_DOC_IN_STAFF)) {
                            docInStaffId = Long.parseLong(innerObj.getString(ConstantsFieldParams.TEXT_DOC_IN_STAFF).trim());
                        }

                        EntityTextAttachment text = new EntityTextAttachment();
                        text.setDocInStaffId(docInStaffId);
                        text.setTextAttachId(docId);

                        strListFileSignDoc.add(text);
                    }
                }
                // Danh sach file bieu mau dinh kem
                List<EntityFileAttachment> listAttachTemplate = null;
                if (!CommonUtils.isEmpty(strListAttachTemplate)) {
                    listAttachTemplate = gson.fromJson(strListAttachTemplate, listFileAttachmentType);
                }
                // Danh sach don vi/nhom ca nhan nhan van ban
                List<EntityTextReceiverGroup> listReceivingGroup = null;
                if (!CommonUtils.isEmpty(strListReceivingGroup)) {
                    Type listTextReceiverGroupType = new TypeToken<ArrayList<EntityTextReceiverGroup>>() {
                    }.getType();
                    listReceivingGroup = gson.fromJson(strListReceivingGroup, listTextReceiverGroupType);
                }
                List<MeetingApproveResult> listMeeting = null;
                if (!CommonUtils.isEmpty(strListMeeting)) {
                    Type meetingListType = new TypeToken<ArrayList<MeetingApproveResult>>() {
                    }.getType();
                    listMeeting = gson.fromJson(strListMeeting, meetingListType);
                }
                if (textId == null || docTitle.isEmpty() || listStaff == null || officeSenderId == null || textId.equals(0L)) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                }
                // 201812-Pitagon: Lay danh sach don vi dong dau
                List<EntityVhrOrg> orgMarkList = new ArrayList<EntityVhrOrg>();
                JSONArray arrOrgMarkList = FunctionCommon.jsonGetArray("orgMarkList", strDataClient);
                if (arrOrgMarkList != null && arrOrgMarkList.length() > 0) {
                    JSONObject obj;
                    EntityVhrOrg org;
                    for (int i = 0; i < arrOrgMarkList.length(); i++) {
                        obj = arrOrgMarkList.getJSONObject(i);
                        org = gson.fromJson(obj.toString(), EntityVhrOrg.class);
                        orgMarkList.add(org);
                    }
                }
                
                Long result = documentDao.resignText(textId, docTitle, registerNumber,
                        code, description, typeId, sTypeId, areaId, priorityId,
                        officeSender, officeSenderId, creatorIdVof1, creatorIdVof2,
                        officePublishedId, isDeleted, isVoffice1, listStaff, strListFileSign,
                        listOtherFile, strListFileSignDoc, officeSenderId2,
                        officeSenderName2, autoPublicText, autoPromulgateText,
                        autoSendText, listStaffSend, unitMoney, moneyTranfer,
                        creatorNameVof2, isLienKe, listAttachTemplate,
                        listReceivingGroup, listMeeting, isActive, signWithCompany, orgMarkList);
                if(!result.equals(0L)){
                    //datnv5: update van ban co nguon goc tu choi ve trang thai duoc trinh ky lai
                    ElasticTextControllerIndexData.updateTextRejectToReSign(textId,7);
                }
                
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (JSONException | NumberFormatException e) {
                logger.error("ERROR ResignText", e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }
        return strResult;
    }

    public String sendAndSign(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //lay gia tri tu session
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String decryptedData = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(decryptedData);
                LogUtils.logFunctionalStart(log);
                //Lay prams client gui len de thuc hien yeu cau
                HashMap<String, Object> hmParams = new HashMap<String, Object>();
                hmParams.put(ConstantsFieldParams.TEXT_ID, Long.class);
                hmParams.put(ConstantsFieldParams.APP_CODE, String.class);
                hmParams.put(ConstantsFieldParams.TRANSACTION_CODE, String.class);

                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);

                Long textId = (Long) ((valueParams.get(ConstantsFieldParams.TEXT_ID) != null) ? valueParams.get(ConstantsFieldParams.TEXT_ID) : null);

                if (textId == null || textId.equals(0L)) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                }
                // bien cho ung dung ngoai vof goi trinh ky
                String appCode = (String) (valueParams.get(ConstantsFieldParams.APP_CODE));
                String transCode = (String) (valueParams.get(ConstantsFieldParams.TRANSACTION_CODE));
                // Giai ma du lieu neu ma hoa
                String aesKey;
                if (isSecurity != null && "1".equals(isSecurity)) {
                    // Lay AES Key
                    aesKey = dataSessionGR.getStrAesKey();
                    // Giai ma data client gui len
                    strData = SecurityControler.decodeDataByAes(aesKey, strData);
                }
                DocumentSignDAO documentDao = new DocumentSignDAO();
                int result = documentDao.sendAndSign(textId, appCode, transCode);
                if(result == 1){
                    //datnv5: update danh sach nguoi ky xem van ban nay co bi tu choi tu truoc khong
                    UpdateTextListUserReject updateTextListUserReject = new UpdateTextListUserReject(textId) ;
                    updateTextListUserReject.start();
                    //datnv5: update van ban co nguon goc tu choi ve trang thai duoc trinh ky lai
                    ElasticTextControllerIndexData.updateTextRejectToReSign(textId,7);
                }
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_ERROR, dataSessionGR.getEnumErrCode(), e.getMessage(), strData, isSecurity);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }

        return strResult;
    }

    public String changeStateSign(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                EntityUserGroup entityUserGroup = dataSessionGR;
                if (entityUserGroup.getVof2_ItemEntityUser() == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.SESSION_TIME_OUT, null, null);
                }
                //lay gia tri tu session
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                String email = entityUserGroup.getVof2_ItemEntityUser().getStrEmail();
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);
                // Lay prams client gui len de thuc hien yeu cau
                HashMap<String, Object> hmParams = new HashMap<String, Object>();
                hmParams.put(ConstantsFieldParams.TEXT_ID, Long.class);
                hmParams.put(ConstantsFieldParams.STATE, Long.class);

                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);

                Long textId = (Long) ((valueParams.get(ConstantsFieldParams.TEXT_ID) != null) ? valueParams.get(ConstantsFieldParams.TEXT_ID) : null);
                Long state = (Long) ((valueParams.get(ConstantsFieldParams.STATE) != null) ? valueParams.get(ConstantsFieldParams.STATE) : 6L);

                if (textId == null || textId.equals(0L)) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                }

                DocumentSignDAO documentDao = new DocumentSignDAO();
                int result = documentDao.changeStateSign(email, textId, state);
                if(result == 1 && state.equals(6L)){
                    ElasticTextControllerIndexData.updateTextRejectToReSign(textId,2);
                }
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
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

    public String getPublicDocumentTypeIdConfig(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //lay gia tri tu session
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                LogUtils.logFunctionalStart(log);
                DocumentDAO documentDao = new DocumentDAO();
                List<Long> result = documentDao.getPublicDocumentTypeIdConfig();
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
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

    public String getLitsUserSignWithRole(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        //Toan bo cac ham khi thuc hien deu phai chay qua ham nay tru ham login va get RSA key
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //Lay prams client gui len de thuc hien yeu cau
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);
                DocumentSignDAO dao = new DocumentSignDAO();
                List<Long> lstStaff = new ArrayList<Long>();

                JSONArray arrStaff = FunctionCommon.jsonGetArray(ConstantsFieldParams.TEXT_LST_STAFF, strDataClient);

                if (arrStaff != null && arrStaff.length() > 0) {
                    for (int i = 0; i < arrStaff.length(); i++) {
                        JSONObject innerObj = (JSONObject) arrStaff.get(i);
                        Long staffId = Long.parseLong(innerObj.getString(ConstantsFieldParams.STAFF_ID));

                        lstStaff.add(staffId);
                    }
                }

                if (lstStaff.isEmpty()) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                }

                List<EntityUserRole> result = dao.getLitsUserSignWithRole(lstStaff);

                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                        result, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
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
     * ham lay danh sach don vi tien te
     *
     * @param isSecurity
     * @param strData
     * @param req
     * @return
     */
    public String getListMoneyUnit(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                LogUtils.logFunctionalStart(log);
                //lay gia tri tu session
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                DocumentSignDAO docSignDAO = new DocumentSignDAO();
                Object result = docSignDAO.getListMoneyUnit();
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                        result, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                java.util.logging.Logger.getLogger(DocumentPublishControler.class.getName()).
                        log(Level.SEVERE, "getListMoneyUnit error: {0}", e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }

        return strResult;
    }

    /**
     * ham lay danh sach don vi tien te
     *
     * @param isSecurity
     * @param strData
     * @param req
     * @return
     */
    public String transferMoneyAction(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //lay gia tri tu session
                EntityUser user = dataSessionGR.getItemEntityUser();
                Long creatorId = 0L;
                if (dataSessionGR.getItemEntityUser() != null) {
                    creatorId = user.getUserId();
                }
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String decryptedData = SecurityControler.decodeDataByAes(strAesKeyDecode,
                        strData);
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(decryptedData);
                LogUtils.logFunctionalStart(log);
                HashMap hmParams = new HashMap();
                //kieu cap nhat
                //1: chuyen tien, 0:  cap nhat anh chu ky
                hmParams.put("actionType", Long.class);
                //id doc
                hmParams.put("documentId", Long.class);

                HashMap valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
                Long actionType = (Long) (valueParams.get("actionType")
                        != null ? valueParams.get("actionType") : null);
                Long documentId = (Long) (valueParams.get("documentId")
                        != null ? valueParams.get("documentId") : null);
                //ghi log dau vao
                DocumentSignDAO docSignDAO = new DocumentSignDAO();
                Object result = docSignDAO.transferMoneyAction(actionType, documentId, creatorId);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                        result, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                java.util.logging.Logger.getLogger(DocumentPublishControler.class.getName()).
                        log(Level.SEVERE, "getListMoneyUnit error: {0}", e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }
        return strResult;
    }

     /**
     * datnv5: 1.get List Check header text the same with add text new
     *
     * @param title
     * @param registerNumber
     * @param code
     * @param description
     * @param creatorIdVof2
     * @param listMainFile
     * @return
     */
    private List<Long> checkHeaderTextReject(HttpServletRequest req,
            Long creatorIdVof2, String title,String conten,
            String area, String priority_id,String type_id,String stype_id
            ,String code, List<EntityUserGroup> listIdSign) {
        String strDateBefor30 = dateShow(new Date(), true);
        String strDateNow = dateShow(new Date(), false);

        StringBuilder queryElastic = new StringBuilder();
        queryElastic.append("{\"query\":{\"bool\":{");
        queryElastic.append("\"must\" : [{ \"match\": {\"creatorId\" : ");
        queryElastic.append(creatorIdVof2.toString());
        queryElastic.append("}}]");
        queryElastic.append(",\"should\":[");
        queryElastic.append("{\"range\": {\"dateCreate\": {\"gte\": \"");
        queryElastic.append(strDateBefor30);
        queryElastic.append("\",\"lte\": \"");
        queryElastic.append(strDateNow);
        queryElastic.append("\"}}}");
        queryElastic.append(",{\"match\":{\"title\":\"%s\"}}");
        queryElastic.append(",{\"match\":{\"conten\":\"%s\"}}");
        queryElastic.append(",{\"match\":{\"code\":\"%s\"}}");
        queryElastic.append(",{\"terms\" : {\"listUserSign\" :%s}}");
        
        queryElastic.append(",{\"match\":{\"priorityId\":\"%s\"}}");
        queryElastic.append(",{\"match\":{\"typeId\":\"%s\"}}");
        queryElastic.append(",{\"match\":{\"stypeId\":\"%s\"}}");
        queryElastic.append(",{\"match\":{\"areaId\":\"%s\"}}");
        queryElastic.append("],\"minimum_should_match\":6}},\"min_score\": 6}");
       
        if (title.trim().length() > 0) {
            title = FunctionCommon.replaceTextGiveElastic(title);
        }
        if (conten.trim().length() > 0) {
            conten = FunctionCommon.replaceTextGiveElastic(conten);
        }
        List<Long> listUserSigner = new ArrayList<>();
        if(listIdSign!=null){
            for(int i=0; i< listIdSign.size();++i){
                listUserSigner.add(listIdSign.get(i).getUserId2());
            }
        }
        String sql = String.format(queryElastic.toString(), title, conten,code
                , listUserSigner.toString(),priority_id,type_id,stype_id,area);
        List<Long> listId = getListTextReject(sql);
        return listId;
    }
    
    /**
     * datnv5: 2.getList Text check conten file sign the same file sign new 
     *
     * @param title
     * @param registerNumber
     * @param code
     * @param description
     * @param creatorIdVof2
     * @param listMainFile
     * @return
     */
    private List<Long> checkExitsTextReject(HttpServletRequest req,
            String title, String registerNumber, String code,
            String description, Long creatorIdVof2,
            List<EntityFileAttachment> listMainFile) {
        String strDateBefor30 = dateShow(new Date(), true);
        String strDateNow = dateShow(new Date(), false);
        String strContenFile = readFileGetConten(req, creatorIdVof2, listMainFile);

        StringBuilder queryElastic = new StringBuilder();
        queryElastic.append("{\"query\":{\"bool\":{\"must\":[{\"match\":{\"creatorId\":");
        queryElastic.append(creatorIdVof2.toString());
        queryElastic.append("}},{\"range\": {\"dateCreate\": {\"gte\": \"");
        queryElastic.append(strDateBefor30);
        queryElastic.append("\",\"lte\": \"");
        queryElastic.append(strDateNow);
        queryElastic.append("\"}}}],\"should\":[");
        queryElastic.append("{\"match\":{\"contenFileSign\":\"%s\"}}],\"minimum_should_match\":1}}");
        queryElastic.append(",\"sort\": [{ \"_score\": { \"order\": \"desc\" }}],\"from\":0,\"size\":55,");
        int length = strContenFile.length();
        if (length > 2000) {
            queryElastic.append("\"min_score\": 300");
        } else if (length <= 2000 && length > 1000) {
            queryElastic.append("\"min_score\": 250");
        } else if (length <= 1000 && length > 500) {
            queryElastic.append("\"min_score\": 180");
        } else {
            queryElastic.append("\"min_score\": 50");
        }
        queryElastic.append("}");
        
//        if (title.trim().length() > 0) {
//            title = FunctionCommon.replaceTextGiveElastic(title);
//        }
//        if (description.trim().length() > 0) {
//            description = FunctionCommon.replaceTextGiveElastic(description);
//        }
        if (strContenFile.trim().length() > 0) {
            strContenFile = FunctionCommon.replaceTextGiveElastic(strContenFile);
        }
        String sql = String.format(queryElastic.toString(), strContenFile);
        List<Long> listId = getListTextReject(sql);
        return listId;
    }

    /**
     * lay ngay tru di 30
     *
     * @param date
     * @return
     */
    private String dateShow(Date date, boolean befor30) {
        if (date == null) {
            return "2017-09-01";
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        if (befor30) {
            cal.add(Calendar.DATE, -30);
        }
        Date dateBefore30Days = cal.getTime();
        String strDate = new SimpleDateFormat("yyyy-MM-dd").format(dateBefore30Days);
        return strDate;
    }

    /**
     * doc noi dung file
     *
     * @param listFileAtt
     * @return
     */
    private static String readFileGetConten(HttpServletRequest req, Long userId2, List<EntityFileAttachment> listFileAtt) {
        if (listFileAtt != null && listFileAtt.size() > 0) {
            try {
                EntityFileAttachment itemFileAt = listFileAtt.get(0);
                String strStorage = itemFileAt.getStorage();
                String exportFolder;
                if (strStorage != null) {
                    exportFolder = FunctionCommon.getPropertiesValue(strStorage);
                } else {
                    exportFolder = FunctionCommon.getPropertiesValue("storage_null");
                }
                String realPath = exportFolder + File.separator + itemFileAt.getFilePath();

//                String keySaveFileTmp = FunctionCommon.getPropertiesValue("storageName_saveFileTmp");
//                String uploadPathTmp = FunctionCommon.getPropertiesValue(keySaveFileTmp);
//                realPath = uploadPathTmp + File.separator + itemFileAt.getFilePath();
                String temFile = req.getRealPath("/") + "share/tmp"
                        + File.separator + String.valueOf(userId2) + "_"
                        + (new Date()).getTime() + "_tmp.pdf";
                // 201812-Pitagon: ensure path is safe
                temFile = FileUtils.getSafePath(temFile);
                // End-201812-Pitagon: ensure path is safe
                EncryptDecryptSignDocument ef = new EncryptDecryptSignDocument();
                SecretKey key = ef.getKey();
                try (FileInputStream fis = new FileInputStream(realPath);
                        FileOutputStream fos = new FileOutputStream(temFile)) {
                    ef.decrypt(key, fis, fos);
                }

                File decryptedFile = new File(temFile);
                if (decryptedFile.exists()) {
                    //file da duoc giai ma
                    //doc noi dung file va dua vao thanh noi dung van ban
                    String strContenFile = FunctionCommon.readContenFile(temFile);
                    decryptedFile.delete();
                    int lengthConten = strContenFile.length();
                    String strContenResult = "";
                    if (lengthConten > 3033) {
                        int subLength = lengthConten / 3;
                        strContenResult += strContenFile.substring(1, 1000);
                        strContenResult += strContenFile.substring(subLength + 1, subLength + 1001);
                        strContenResult += strContenFile.substring(subLength * 2 + 1, subLength * 2 + 1001);
                    } else {
                        strContenResult = strContenFile;
                    }
                    return strContenResult;
                }
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            } catch (Throwable ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
        return "";
    }

    /**
     * lay danh sach van ban co cung noi dung
     *
     * @param jsonObject
     * @return
     */
    private static List<Long> getListTextReject(String jsonSearch) {
        List<Long> listIdText = null;
        try {
            String strResult = "";
            ConfigEntity itemConfig = ElasticCommon.getConfigSearchElastic();
            if(itemConfig!=null && itemConfig.getStatus()==1){
                // cau hinh theo tim kiem moi
                List<String> listUrl = itemConfig.getListIpElastic();
                for (String ipElastic : listUrl) {
                    //neu khong cau hinh thi tim kiem theo elasticu
                    String url = "http://" + ipElastic
                            + StrElasticConstants.STR_INDEXTEXT_REJECT
                            + "_search?_source=textId,content,title,creatorId,createDate";
                    strResult = ConnectServer.sendRequestToServer(url, jsonSearch,null);
                    if(strResult != null && strResult.trim().length() > 0){
                        break;
                    }
                }
            }else{
                //neu khong cau hinh thi tim kiem theo elasticu
                String url = FunctionCommon.getPropertiesValue(
                        "url.elasticsearch.server")
                        + StrElasticConstants.STR_INDEXTEXT_REJECT
                        + "_search?_source=textId,content,title,creatorId,createDate";
                strResult = ConnectServer.sendRequestToServer(url, jsonSearch,null);
            }
            
            if (!CommonUtils.isEmpty(strResult) && CommonUtils.isJSON(strResult)) {
                JSONObject json = new JSONObject(strResult);
                if (json.isNull("hits")) {
                    return null;
                }
                JSONObject jsonhits = (JSONObject) json.get("hits");
                if (jsonhits.isNull("hits")) {
                    return null;
                }
                JSONArray array = jsonhits.getJSONArray("hits");
                if (array != null) {
                    listIdText = new ArrayList<>();
                    String strScore = "";
                    int curentCount = 0;
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject jsonItem = (JSONObject) array.get(i);
                        String curentScore = jsonItem.getString("_score");
                        if (strScore.trim().length() == 0) {
                            strScore = curentScore;
                        }
                        if(strScore.trim().length() > 0 
                                && strScore.equals(curentScore)){
                             ++curentCount;
                              strScore = curentScore;
                        }
                        if (curentCount < 5) {
                            String idText = jsonItem.getString("_id");
                            if (FunctionCommon.isNumeric(idText)) {
                                listIdText.add(Long.valueOf(idText));
                            }
                        } else {
                            break;
                        }
                    }
                }
            }
        } catch (JSONException ex) {
            listIdText = null;
            logger.error(ex.getMessage(), ex);
        }
        return listIdText;
    }
}
