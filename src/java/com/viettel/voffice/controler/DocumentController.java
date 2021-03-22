/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

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
import com.viettel.voffice.database.dao.connectvhr.ConnectVHRDao;
import com.viettel.voffice.database.dao.document.DocumentDAO;
import com.viettel.voffice.database.dao.document.DocumentInGroupDAO;
import com.viettel.voffice.database.dao.document.DocumentInStaffDAO;
import com.viettel.voffice.database.dao.document.DocumentLibraryDAO;
import com.viettel.voffice.database.dao.document.DocumentScopeDAO;
import com.viettel.voffice.database.dao.file.AttachDAO;
import com.viettel.voffice.database.dao.file.FilesAttachmentDAO;
import com.viettel.voffice.database.dao.logAction.ActionLogMobileDAO;
import com.viettel.voffice.database.dao.meeting.MeetingDAO;
import com.viettel.voffice.database.dao.sms.SmsDAO;
import com.viettel.voffice.database.dao.staff.UserDAO;
import com.viettel.voffice.database.dao.staff.UserRoleDAO;
import com.viettel.voffice.database.dao.text.TextDAO;
import com.viettel.voffice.database.dao.text.TextSignDAO;
import com.viettel.voffice.database.entity.EntityAttach;
import com.viettel.voffice.database.entity.EntityCommonFields;
import com.viettel.voffice.database.entity.EntityDocument;
import com.viettel.voffice.database.entity.EntityDocumentPermission;
import com.viettel.voffice.database.entity.EntityFileAttachment;
import com.viettel.voffice.database.entity.EntityFilesAttachment;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityMarkAttachHistory;
import com.viettel.voffice.database.entity.EntityResult;
import com.viettel.voffice.database.entity.EntityStaff;
import com.viettel.voffice.database.entity.EntityTextMark;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.EntityUserRole;
import com.viettel.voffice.database.entity.EntityVhrEmployee;
import com.viettel.voffice.database.entity.EntityVhrOrg;
import com.viettel.voffice.database.entity.User.EntityCvGroup;
import com.viettel.voffice.database.entity.User.EntityUser;
import com.viettel.voffice.database.entity.User.GroupMapping;
import com.viettel.voffice.database.entity.User.Vof2_EntityUser;
import com.viettel.voffice.database.entity.connectvhr.EntityConnectVHR;
import com.viettel.voffice.database.entity.document.EntityDocumentCount;
import com.viettel.voffice.database.entity.document.EntityDocumentExtend;
import com.viettel.voffice.database.entity.document.EntityDocumentInGroup;
import com.viettel.voffice.database.entity.document.EntityDocumentInStaff;
import com.viettel.voffice.database.entity.document.EntityReceivedDocumentCount;
import com.viettel.voffice.database.entity.document.EntitySendDocResult;
import com.viettel.voffice.database.entity.log.EntityActionLogMobile;
import com.viettel.voffice.database.entity.meeting.EntityMeetingMember;
import com.viettel.voffice.database.entity.task.EntityMeeting;
import com.viettel.voffice.database.entity.task.EntityMission;
import com.viettel.voffice.database.entity.task.EntityTask;
import com.viettel.voffice.database.entity.text.EntityText;
import com.viettel.voffice.elasticsearch.indexdata.IndexDocumentByType;
import com.viettel.voffice.elasticsearch.search.ElasticSearchDocument;
//import com.viettel.voffice.elasticsearch.search.ElasticSearchDocument;
import com.viettel.voffice.thread.DocumentThread.DocumentCountThread;
import com.viettel.voffice.thread.DocumentThread.DocumentDetailThread;
import com.viettel.voffice.threadmanager.ThreadPoolCommon;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.LogUtils;

/**
 *
 * @author Hiendv2
 */
@SuppressWarnings("deprecation")
public class DocumentController {

    /**
     * Log file
     */
    private static final Logger LOGGER = Logger.getLogger(DocumentController.class);
    // Ten cua class bao gom ca ten package
    private static final String CLASS_NAME = DocumentController.class.getName();
 // 201901-Pitagon: bo sung gon 
    private static Gson gson =  new GsonBuilder().setDateFormat("dd/MM/yyyy hh:mm:ss").create();

    /**
     * <b>Thiet lap gia tri mac dinh khi them moi van ban</b><br/>
     *
     * @author thanght6
     * @since May 29, 2016
     * @param userId1 Id user tren he thong 1
     * @param groupId1 Id don vi tren he thong 1
     * @param userId2 Id user tren he thong 2
     * @param groupId2 Id don vi tren he thong 2
     * @param document Doi tuong van ban
     */
    private void setupValueToAddDocument(Long userId1, Long groupId1,
            Long userId2, Long groupId2, EntityDocument document) {
        document.setIsLock(1);
        document.setCreatorId(userId1);
        document.setCreatorGroupId(groupId1);
        document.setStatusNumber(0L);
        document.setStateNumber(2L);
        document.setIsFromCoporation(1);
        document.setIsKeepOfficalBook(0L);
        document.setProcessType(0L);
        document.setCreatorId2(userId2);
        document.setCreatorGroupId2(groupId2);
    }

    /**
     * <b>Them moi van ban</b><br>
     *
     * @author thanght6
     * @since May 28, 2016
     *
     * @param request Doi tuong request tu client -> server
     * @param data Gia tri data truyen theo request
     * @param isSecurity Gia tri isSecurity truyen theo request
     *
     * @return
     */
    public String addDocument(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("addDocument (Them moi cong van) - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }

        // Neu user hoac user id tren he thong 1&2 null
        // -> Tra ve thong bao khong co quyen
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        if ((user1 == null || user1.getUserId() == null)
                && (user2 == null || user2.getUserId() == null)) {
            LOGGER.error("addDocument (Them moi cong van) - user hoac userId tren"
                    + " he thong 1&2 null!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Neu khong co thong tin tren he thong 1
        // -> Gan userId1 = 0
        Long userId1 = (user1 == null || user1.getUserId() == null) ? 0L : user1.getUserId();
        Long groupId1 = (user1 == null || user1.getGroupId() == null) ? 0L : user1.getGroupId();
        // Neu khong co thong tin tren he thong 2
        // -> Gan userId2 = 0
        Long userId2 = (user2 == null || user2.getUserId() == null) ? 0L : user2.getUserId();
        Long groupId2 = (user2 == null || user2.getSysOrgId() == null) ? 0L : user2.getSysOrgId();
        // Lay ma nhan vien
        String cardId = "";
        if (user1 != null && !CommonUtils.isEmpty(user1.getStrCardNumber())) {
            cardId = user1.getStrCardNumber();
        } else if (user2 != null && !CommonUtils.isEmpty(user2.getStrCardNumber())) {
            cardId = user2.getStrCardNumber();
        }
        log.setUserName(cardId);
        // Giai ma du lieu neu ma hoa
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
                ConstantsFieldParams.DOCUMENT,
                ConstantsFieldParams.LIST_ATTACHMENT,
                "listAttachTemplate"
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String strDocument = listValue.get(0);
            String strListAttachment = listValue.get(1);
            String strListAttachTemplate = listValue.get(2);

            // Parse chuoi strDocument sang doi tuong van ban
            Gson gson = new Gson();
            EntityDocument document = gson.fromJson(strDocument, EntityDocument.class);

            // Thiet lap cac gia tri mac dinh
            setupValueToAddDocument(userId1, groupId1, userId2, groupId2, document);

            // Parse chuoi strListAttachment sang danh sach doi tuong file dinh kem
            Type listType = new TypeToken<ArrayList<EntityFilesAttachment>>() {
            }.getType();
            List<EntityFilesAttachment> listAttachment = gson.fromJson(strListAttachment, listType);

            // Danh sach file bieu mau dinh kem
            List<EntityFilesAttachment> listAttachTemplate = new ArrayList<EntityFilesAttachment>();
            if (!CommonUtils.isEmpty(strListAttachTemplate)) {
            	listAttachTemplate = gson.fromJson(strListAttachTemplate, listType);
            }

            // Them moi van ban
            DocumentDAO documentDAO = new DocumentDAO();
            int result = documentDAO.addDocument(userId2, document, listAttachment,
                    listAttachTemplate, groupId2);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error("addDocument (Them moi cong van) - Exception - username: "
                    + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Sua van ban</b></br>
     *
     * @author thanght6
     * @since May 30, 2016
     * @param request Doi tuong request tu client -> server
     * @param data Gia tri data truyen theo request
     * @param isSecurity Gia tri isSecurity truyen theo request
     *
     * @return
     */
    public String editDocument(HttpServletRequest request,
            String data, String isSecurity) {

        String[] keys = new String[]{
            ConstantsFieldParams.DOCUMENT,
            ConstantsFieldParams.LIST_ADDITIONAL_ATTACHMENT,
            ConstantsFieldParams.LIST_REMOVAL_ATTACHMENT,
            "listAttachTemplate",
            "listOldAttachment"
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("editDocument (Chinh sua cong van) - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        // Neu user hoac user id tren he thong 1&2 null
        // -> Tra ve thong bao khong co quyen
        if (!userGroup.checkUserId1() && !userGroup.checkUserId2()) {
            LOGGER.error("editDocument (Chinh sua cong van) - user hoac userId tren"
                    + " he thong 1&2 null!");
            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            // Parse chuoi strDocument sang doi tuong van ban
            String strDocument = listValue.get(0);
            Gson gson = new Gson();
            EntityDocument document = null;
            if (!CommonUtils.isEmpty(strDocument)) {
                document = gson.fromJson(strDocument, EntityDocument.class);
            }
            // Parse chuoi strListAdditionalAttachment sang danh sach doi tuong file dinh kem
            String strListAdditionalAttachment = listValue.get(1);
            Type listType = new TypeToken<ArrayList<EntityFilesAttachment>>() {
            }.getType();
            List<EntityFilesAttachment> listAdditionalAttachment = null;
            if (!CommonUtils.isEmpty(strListAdditionalAttachment)) {
                listAdditionalAttachment = gson.fromJson(strListAdditionalAttachment, listType);
            }
            // Parse chuoi strListRemovalAttachment sang danh sach doi tuong file dinh kem bi xoa bo
            String strListRemovalAttachment = listValue.get(2);
            List<EntityFilesAttachment> listRemovalAttachment = null;
            if (!CommonUtils.isEmpty(strListRemovalAttachment)) {
                listRemovalAttachment = gson.fromJson(strListRemovalAttachment, listType);
            }
            // Danh sach file bieu mau dinh kem
            String strListAttachTemplate = listValue.get(3);
            List<EntityFilesAttachment> listAttachTemplate = null;
            if (!CommonUtils.isEmpty(strListAttachTemplate)) {
                listAttachTemplate = gson.fromJson(strListAttachTemplate, listType);
            }
            // Danh sach file dinh kem cu co the bi thay doi vi tri
            String strListOldAttachment = listValue.get(4);
            List<EntityFilesAttachment> listOldAttachment = null;
            if (!CommonUtils.isEmpty(strListOldAttachment)) {
                listOldAttachment = gson.fromJson(strListOldAttachment, listType);
            }
            DocumentDAO documentDAO = new DocumentDAO();
            int result = documentDAO.editDocument(userGroup.getUserId2(), document,
                    listAdditionalAttachment, listRemovalAttachment, listAttachTemplate,
                    listOldAttachment, userGroup.getGroupId2());
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            LOGGER.error("editDocument (Chinh sua cong van) - Exception - username: "
                    + userGroup.getCardId() + "\ndata: " + data, ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    /**
     * <b>Xoa cong van</b></br>
     *
     * @author thanght6
     * @since May 30, 2016
     * @param request Doi tuong request tu client -> server
     * @param data Gia tri data truyen theo request
     * @param isSecurity Gia tri isSecurity truyen theo request
     *
     * @return
     */
    public String deleteDocument(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("deleteDocument (Xoa cong van) - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Neu user hoac user id tren he thong 1&2 null
        // -> Tra ve thong bao khong co quyen
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        if ((user1 == null || user1.getUserId() == null)
                && (user2 == null || user2.getUserId() == null)) {
            LOGGER.error("deleteDocument - user hoac userId tren he thong 1&2 null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Neu khong co thong tin tren he thong 1
        // -> Gan userId1 = 0
        Long userId1 = (user1 == null || user1.getUserId() == null) ? 0L : user1.getUserId();
        // Neu khong co thong tin tren he thong 2
        // -> Gan userId2 = 0
        Long userId2 = (user2 == null || user2.getUserId() == null) ? 0L : user2.getUserId();
        // Lay ma nhan vien
        String cardId = "";
        if (user1 != null && !CommonUtils.isEmpty(user1.getStrCardNumber())) {
            cardId = user1.getStrCardNumber();
        } else if (user2 != null && !CommonUtils.isEmpty(user2.getStrCardNumber())) {
            cardId = user2.getStrCardNumber();
        }
        log.setUserName(cardId);
        // Giai ma du lieu neu ma hoa
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
                ConstantsFieldParams.DOCUMENT_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long documentId = Long.parseLong(listValue.get(0));
            DocumentDAO documentDAO = new DocumentDAO();
            int result = documentDAO.deleteDocument(userId1, userId2, documentId);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error("deleteDocument (Xoa cong van) - Exception - username: "
                    + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     *
     * @param isSecurity
     * @param strData
     * @param req
     * @return
     */
    public String actionSearchDocViewLibrary(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            String cardId = "";
            try {
                // Lay thong tin user
                EntityUser user1 = dataSessionGR.getItemEntityUser();
                Vof2_EntityUser user2 = dataSessionGR.getVof2_ItemEntityUser();
                if (user1 != null && !CommonUtils.isEmpty(user1.getStrCardNumber())) {
                    cardId = user1.getStrCardNumber();
                } else if (user2 != null && !CommonUtils.isEmpty(user2.getStrCardNumber())) {
                    cardId = user2.getStrCardNumber();
                }
                log.setUserName(cardId);
                String strAesKeyDecode = null;
                String decryptedData = "";
                if (isSecurity != null && "1".equals(isSecurity)) {
                    strAesKeyDecode = dataSessionGR.getStrAesKey();
                    decryptedData = SecurityControler.decodeDataByAes(strAesKeyDecode,
                            strData);
                }
                // Ghi log bat dau chuc nang
                log.setParamList(decryptedData);
                LogUtils.logFunctionalStart(log);
                HashMap<String, Object> hmParams = new HashMap<String, Object>();
                hmParams.put("isCount", String.class);
                //Don vi xay dung
                hmParams.put("buildGroupId", Long.class);
                //Don vi cong bo
                hmParams.put("officePublishedId", Long.class);
                //Ma va nban
                hmParams.put("docCode", String.class);
                //Tieu de van ban
                hmParams.put("docTitle", String.class);
                //The thuc van ban
                hmParams.put("docType", Long.class);
                //Linh vuc
                hmParams.put("docArea", Long.class);
                //Nganh
                hmParams.put("docIndustryId", Long.class);
                //thoi gian bat dau hieu luc
                hmParams.put("fromPublishedDate", String.class);
                //thoi gian ket thuc hieu luc
                hmParams.put("toPublishedDate", String.class);
                //thoi gian bat dau het hieu luc
                hmParams.put("fromExpDate", String.class);
                hmParams.put("toExpDate", String.class);
                //Trang thai van ban
                hmParams.put("state", Long.class);
                //Trang thai van ban thay the/ko thay the
                hmParams.put("isDocReplace", Long.class);
                hmParams.put("senderName", String.class);
                hmParams.put("appliedPoint", String.class);
                hmParams.put("startRecord", Long.class);
                hmParams.put("pageSize", Long.class);
                hmParams.put("keyWord", String.class);
                hmParams.put(ConstantsFieldParams.DOC_SCOPE_IDS, String.class);
                //cuongnv ::them id van ban tim kiem
                hmParams.put(ConstantsFieldParams.DOCUMENT_ID, Long.class);
                // Bo sung 2 tham so ngay cong bo de TTKT dong bo theo ngay cong bo
                hmParams.put("dateSyncFrom", String.class);
                hmParams.put("dateSyncTo", String.class);
                hmParams.put("isNotLimitByOrg", String.class);
                hmParams.put("scopeOrgId", Long.class);

                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
                Long groupPublishedId = (Long) (valueParams.get("officePublishedId")
                        != null ? valueParams.get("officePublishedId") : 0L);
                String docCode = (String) (valueParams.get("docCode") != null
                        ? valueParams.get("docCode") : "");
                String docTitle = (String) (valueParams.get("docTitle") != null
                        ? valueParams.get("docTitle") : "");
                String senderName = (String) (valueParams.get("senderName")
                        != null ? valueParams.get("senderName") : "");
                String appliedPoint = (String) (valueParams.get("appliedPoint")
                        != null ? valueParams.get("appliedPoint") : "");
                Long docType = (Long) (valueParams.get("docType") != null
                        ? valueParams.get("docType") : null);
                Long docArea = (Long) (valueParams.get("docArea") != null
                        ? valueParams.get("docArea") : null);
                Long docIndustryId = (Long) (valueParams.get("docIndustryId")
                        != null ? valueParams.get("docIndustryId") : null);
                Long state = (Long) (valueParams.get("state")
                        != null ? valueParams.get("state") : null);
                Long isDocReplace = (Long) (valueParams.get("isDocReplace")
                        != null ? valueParams.get("isDocReplace") : null);
                Long buildGroupId = (Long) (valueParams.get("buildGroupId")
                        != null ? valueParams.get("buildGroupId") : null);

                String fromPublishedDate = (String) (valueParams.get("fromPublishedDate") != null
                        ? valueParams.get("fromPublishedDate") : "");
                String toPublishedDate = (String) (valueParams.get("toPublishedDate") != null ? valueParams.get("toPublishedDate") : "");
                String fromExpDate = (String) (valueParams.get("fromExpDate") != null ? valueParams.get("fromExpDate") : "");
                String toExpDate = (String) (valueParams.get("toExpDate")
                        != null ? valueParams.get("toExpDate") : "");
                Long startRecord = (Long) (valueParams.get("startRecord") != null
                        ? valueParams.get("startRecord") : 0L);
                Long pageSize = (Long) (valueParams.get("pageSize")
                        != null ? valueParams.get("pageSize") : 10L);
                String isCount = (String) valueParams.get("isCount");
                String keyWord = (String) (valueParams.get("keyWord")
                        != null ? valueParams.get("keyWord") : null);
                String docScopeIdStr = (String) (valueParams.get(ConstantsFieldParams.DOC_SCOPE_IDS)
                        != null ? valueParams.get(ConstantsFieldParams.DOC_SCOPE_IDS) : null);
                Long documentId = (Long) (valueParams.get(ConstantsFieldParams.DOCUMENT_ID) != null
                        ? valueParams.get(ConstantsFieldParams.DOCUMENT_ID) : null);
                String dateSyncFrom = (String) (valueParams.get("dateSyncFrom")
                        != null ? valueParams.get("dateSyncFrom") : null);
                String dateSyncTo = (String) (valueParams.get("dateSyncTo")
                        != null ? valueParams.get("dateSyncTo") : null);
                // Khong gioi han tim kiem theo don vi
                boolean isNotLimitByOrg = true;
                String strIsNotLimitByOrg = (String) (valueParams.get("isNotLimitByOrg")
                        != null ? valueParams.get("isNotLimitByOrg") : null);
                if (!CommonUtils.isEmpty(strIsNotLimitByOrg) && "0".equals(strIsNotLimitByOrg)) {
                    isNotLimitByOrg = false;
                }
                Long scopeOrgId = (Long) (valueParams.get("scopeOrgId") != null
                        ? valueParams.get("scopeOrgId") : null);
                List<Long> docScopeIds = null;
                if (!CommonUtils.isEmpty(docScopeIdStr)) {
                    JSONArray jsonArray = new JSONArray(docScopeIdStr);
                    if (jsonArray.length() > 0) {
                        docScopeIds = new ArrayList<Long>();
                        JSONObject jsonObject;
                        Long removalAlternativeDocumentId;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            jsonObject = jsonArray.getJSONObject(i);
                            if (!jsonObject.isNull(ConstantsFieldParams.SCOPE_ID)) {
                                removalAlternativeDocumentId = jsonObject.getLong(
                                        ConstantsFieldParams.SCOPE_ID);
                                docScopeIds.add(removalAlternativeDocumentId);
                            }
                        }
                    }
                }

                DocumentLibraryDAO documentLibDao = new DocumentLibraryDAO();
                Object result = documentLibDao.actionSearchDocViewLibrary(groupPublishedId,
                        dataSessionGR.getVof2_ItemEntityUser().getUserId(), docCode,
                        docTitle, senderName, appliedPoint, docType, docArea,
                        docIndustryId, fromPublishedDate, toPublishedDate,
                        fromExpDate, toExpDate, state, isDocReplace, buildGroupId,
                        docScopeIds, 1L, isCount, startRecord, pageSize, keyWord,
                        documentId, dateSyncFrom, dateSyncTo, isNotLimitByOrg,
                        scopeOrgId);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);

            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }
        return strResult;
    }

    /**
     * <b>Cong bo van ban</b><br/>
     *
     * @author thanght6
     * @since 2016-02-25
     * @param request Doi tuong request tu client -> server
     * @param data Gia tri data truyen theo request
     * @param isSecurity Gia tri isSecurity truyen theo request
     * @return 1 - Thanh cong
     */
    public String publish(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);

        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("publish - No session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }

        // Neu user hoac user id tren he thong 1 null
        // -> Tra ve thong bao khong co quyen
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
//        if (user == null || user.getUserId() == null) {
//            LOGGER.error("publish - user null || userId null");
//            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
//        }
        Long userId = user == null ? null : user.getUserId();
        Long orgId = userGroup.getGroupId1();
        Long orgIdVof2 = userGroup.getGroupId2();
        if (!CommonUtils.isEmpty(userGroup.getVof2_ItemEntityUser().getListManagementOrg())) {
            orgIdVof2 = userGroup.getVof2_ItemEntityUser().getListManagementOrg().get(0);
        } else if (!CommonUtils.isEmpty(userGroup.getVof2_ItemEntityUser().getListSecretaryVhrOrg())) {
            orgIdVof2 = userGroup.getVof2_ItemEntityUser().getListSecretaryVhrOrg().get(0);
        }

        // Lay ma nhan vien
        String cardId = "";
        if (user != null && !CommonUtils.isEmpty(user.getStrCardNumber())) {
            cardId = user.getStrCardNumber();
        } else if (userGroup.getVof2_ItemEntityUser() != null
                && !CommonUtils.isEmpty(userGroup.getVof2_ItemEntityUser().getStrCardNumber())) {
            cardId = userGroup.getVof2_ItemEntityUser().getStrCardNumber();
        }
        log.setUserName(cardId);
        // Giai ma du lieu neu ma hoa
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
                ConstantsFieldParams.IS_AUTO,
                ConstantsFieldParams.PUBLISHER_ID,
                ConstantsFieldParams.DOCUMENT_ID,
                ConstantsFieldParams.SUMMARY,
                ConstantsFieldParams.PUBLISHED_DATE,
                ConstantsFieldParams.EXPIRED_DATE,
                ConstantsFieldParams.BUILT_GROUP_ID,
                ConstantsFieldParams.ORG_LEVEL,
                ConstantsFieldParams.DEAD_LINE_REPUBLISH, // Lay the deadline de luu du lieu
                ConstantsFieldParams.SCOPE_NAME
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // 0: Cong bo thu cong
            // 1: Cong bo tu dong
            int isAuto = 0;
            String strIsAuto = listValue.get(0);
            if (!CommonUtils.isEmpty(strIsAuto)) {
                isAuto = Integer.parseInt(strIsAuto);
            }
            // Id nguoi cong bo
            Long publisherId = 0L;
            String strPublisherId = listValue.get(1);
            if (!CommonUtils.isEmpty(strPublisherId)) {
                publisherId = Long.parseLong(strPublisherId);
            }
            // Id van ban
            Long documentId = Long.parseLong(listValue.get(2));

            // Tom tat
            String summary = listValue.get(3);

            // Ngay cong bo
            String publishedDate = listValue.get(4);

            // Ngay het hieu luc
            String expiredDate = listValue.get(5);

            // Id don vi xay dung
            Long builtGroupId = Long.parseLong(listValue.get(6));

            // Level don vi
            Integer levelPublishedGroup = 0;
            // Han ban hanh lai van ban
            String deadLineRepublish = listValue.get(8);
            //Ten pham vi khac
            String scopeNameOrther = listValue.get(9);
            // Danh sach id van ban thay the them vao
            List<Long> listAdditionalAlternativeDocumentId = null;
            if (!json.isNull(ConstantsFieldParams.ADDITIONAL_ALTERNATIVE_DOCUMENT_IDS)) {
                JSONArray jsonArray = json.getJSONArray(ConstantsFieldParams.ADDITIONAL_ALTERNATIVE_DOCUMENT_IDS);
                if (jsonArray != null && jsonArray.length() > 0) {
                    listAdditionalAlternativeDocumentId = new ArrayList<Long>();
                    JSONObject jsonObject;
                    Long additionalAlternativeDocumentId;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = jsonArray.getJSONObject(i);
                        if (!jsonObject.isNull(ConstantsFieldParams.DOCUMENT_ID)) {
                            additionalAlternativeDocumentId = jsonObject.getLong(ConstantsFieldParams.DOCUMENT_ID);
                            listAdditionalAlternativeDocumentId.add(additionalAlternativeDocumentId);
                        }
                    }
                }
            }
            // Lay danh sach pham vi cong bo
            List<Long> docScopeIds = new ArrayList<Long>();
            if (!json.isNull(ConstantsFieldParams.DOC_SCOPE_IDS)) {
                JSONArray jsonArray = json.getJSONArray(ConstantsFieldParams.DOC_SCOPE_IDS);
                if (jsonArray != null && jsonArray.length() > 0) {
                    JSONObject jsonObject;
                    Long docScopeId;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = jsonArray.getJSONObject(i);
                        if (!jsonObject.isNull(ConstantsFieldParams.SCOPE_ID)) {
                            docScopeId = jsonObject.getLong(ConstantsFieldParams.SCOPE_ID);
//                            System.out.println("Danh sach pham vi:" + docScopeId);
                            docScopeIds.add(docScopeId);
                        }
                    }
                }
            }
            // Lay van ban goc ::cuongnv
            EntityDocument eBaseDoc = new EntityDocument();
            JSONObject inObj = new JSONObject(data);
            if (inObj.has(ConstantsFieldParams.BASE_DOCUMENT)) {
                String str = inObj.getString(ConstantsFieldParams.BASE_DOCUMENT);
                if (str != null) {
                    inObj = new JSONObject(str);
                    if (inObj.has(ConstantsFieldParams.DOCUMENT_ID) && inObj.has(ConstantsFieldParams.SYS_ORGANIZATION_ID)) {
                        eBaseDoc.setDocumentId(Long.parseLong(inObj.getString(ConstantsFieldParams.DOCUMENT_ID).trim()));
                        eBaseDoc.setSysOrganizationId(Long.parseLong(inObj.getString(ConstantsFieldParams.SYS_ORGANIZATION_ID).trim()));
                    } else {
                        eBaseDoc = null;
                    }
                } else {
                    eBaseDoc = null;
                }
            } else {
                eBaseDoc = null;
            }

            // Lay danh sach don vi pham vi ::cuongnv
            List<EntityVhrOrg> listOrgNeedRepublish = new ArrayList<>();
            JSONArray arrOrgNeedRepublish = FunctionCommon.jsonGetArray(ConstantsFieldParams.LIST_ORG_NEED_REPUBLISH, data);

            if (arrOrgNeedRepublish != null && arrOrgNeedRepublish.length() > 0) {
                for (int i = 0; i < arrOrgNeedRepublish.length(); i++) {
                    JSONObject innerObj = (JSONObject) arrOrgNeedRepublish.get(i);
                    if (innerObj.has(ConstantsFieldParams.SYS_ORGANIZATION_ID)) {
                        EntityVhrOrg eVhr = new EntityVhrOrg();
                        eVhr.setSysOrganizationId(Long.parseLong(innerObj.getString(ConstantsFieldParams.SYS_ORGANIZATION_ID).trim()));
                        listOrgNeedRepublish.add(eVhr);
                    }
                }
            }
            //Hiendv2 bo sung path don vi trong pham vi
            // Lay danh sach pham vi cong bo
            List<String> strOrgPath = new ArrayList<String>();
            List<Long> vhrOrgIds = new ArrayList<>();
            if (!json.isNull(ConstantsFieldParams.SCOPE_HAVE_ORGS)) {
                JSONArray jsonArray = json.getJSONArray(ConstantsFieldParams.SCOPE_HAVE_ORGS);
                if (jsonArray != null && jsonArray.length() > 0) {
                    JSONObject jsonObject;
                    String orgPath;
                    Long orgIds;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = jsonArray.getJSONObject(i);
                        if (!jsonObject.isNull(ConstantsFieldParams.PATH_ORG)) {
                            orgPath = jsonObject.getString(ConstantsFieldParams.PATH_ORG);
                            orgIds = Long.parseLong(jsonObject.getString(ConstantsFieldParams.SYS_ORGANIZATION_ID));

//                            System.out.println("Danh sach pham vi:" + docScopeId);
                            strOrgPath.add(orgPath);
                            vhrOrgIds.add(orgIds);
                        }
                    }
                }
            }
            // Thuc hien cong bo van ban
            DocumentDAO documentDAO = new DocumentDAO();
            int result = documentDAO.publishDocument(isAuto, userId, orgId, orgIdVof2, publisherId,
                    documentId, summary, publishedDate, expiredDate, builtGroupId,
                    levelPublishedGroup, listAdditionalAlternativeDocumentId, docScopeIds,
                    strOrgPath, vhrOrgIds, listOrgNeedRepublish, eBaseDoc, deadLineRepublish, scopeNameOrther);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            // Van ban khong ton tai
            if (result == -2) {
                LOGGER.error("publish - userId2: " + userId + " | documentId: "
                        + documentId + " - Van ban khong ton tai");
                return FunctionCommon.generateResponseJSON(ErrorCode.DOCUMENT_NOT_EXIST,
                        result, aesKey);
            } // Van ban da duoc cong bo
            else if (result == -1) {
                LOGGER.error("publish - userId2: " + userId + " | documentId: "
                        + documentId + " - Van ban da duoc cong bo");
                return FunctionCommon.generateResponseJSON(ErrorCode.DOCUMENT_WAS_PUBLISHED,
                        result, aesKey);
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
            }
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error("publish - userId2: " + userId, ex);
//            System.out.println(ex.getMessage());
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * <b>Sua thong tin cong bo tam</b><br/>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String editTmpPublicationInformation(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);

        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("publish - No session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }

        // Neu user hoac user id tren he thong 1 null
        // -> Tra ve thong bao khong co quyen
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("editTmpPublicationInformation - user null || userId null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        Long userId = user.getUserId();
        Long orgId = user.getAdOrgId();
        if (!CommonUtils.isEmpty(userGroup.getVof2_ItemEntityUser().getListManagementOrg())) {
            orgId = userGroup.getVof2_ItemEntityUser().getListManagementOrg().get(0);
        } else if (!CommonUtils.isEmpty(userGroup.getVof2_ItemEntityUser().getListSecretaryVhrOrg())) {
            orgId = userGroup.getVof2_ItemEntityUser().getListSecretaryVhrOrg().get(0);
        }
        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
        log.setUserName(cardId);
        // Giai ma du lieu neu ma hoa
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
                ConstantsFieldParams.PUBLISHER_ID,
                ConstantsFieldParams.DOCUMENT_ID,
                ConstantsFieldParams.SUMMARY,
                ConstantsFieldParams.PUBLISHED_DATE,
                ConstantsFieldParams.EXPIRED_DATE,
                ConstantsFieldParams.BUILT_GROUP_ID,
                ConstantsFieldParams.ORG_ID,
                ConstantsFieldParams.ORG_LEVEL,
                ConstantsFieldParams.DEAD_LINE_REPUBLISH,
                ConstantsFieldParams.SCOPE_NAME
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);

            // Id nguoi cong bo
            Long publisherId = null;
            String strPublisherId = listValue.get(0);
            if (!CommonUtils.isEmpty(strPublisherId)) {
                publisherId = Long.parseLong(strPublisherId);
            }

            // Id van ban
            Long documentId = Long.parseLong(listValue.get(1));

            // Tom tat
            String summary = listValue.get(2);

            // Ngay cong bo
            String publishedDate = listValue.get(3);

            // Ngay het hieu luc
            String expiredDate = listValue.get(4);

            // Id don vi xay dung
            Long builtGroupId = null;

            String strBuiltGroupId = listValue.get(5);
            if (!CommonUtils.isEmpty(strBuiltGroupId)) {
                builtGroupId = Long.parseLong(strBuiltGroupId);
            }

            // Id don vi
            Long rangePublished = null;
            String strRangePublished = listValue.get(6);
            if (!CommonUtils.isEmpty(strRangePublished)) {
                rangePublished = Long.parseLong(strRangePublished);
            }
            // lay deadline them vao
            String deadLineRepublish = listValue.get(8);
            //Ten pham vi khac
            String scopeNameOrther = listValue.get(9);

            // Level don vi
            Integer levelPublishedGroup = 0;

            // Danh sach id van ban thay the them vao
            List<Long> listAlternativeDocumentId = null;
            if (!json.isNull(ConstantsFieldParams.ALTERNATIVE_DOCUMENT_IDS)) {
                JSONArray jsonArray = json.getJSONArray(ConstantsFieldParams.ALTERNATIVE_DOCUMENT_IDS);
                listAlternativeDocumentId = new ArrayList<>();
                if (jsonArray != null && jsonArray.length() > 0) {
                    JSONObject jsonObject;
                    Long additionalAlternativeDocumentId;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = jsonArray.getJSONObject(i);
                        if (!jsonObject.isNull(ConstantsFieldParams.DOCUMENT_ID)) {
                            additionalAlternativeDocumentId = jsonObject.getLong(ConstantsFieldParams.DOCUMENT_ID);
                            listAlternativeDocumentId.add(additionalAlternativeDocumentId);
                        }
                    }
                }
            }

            // lay thong tin van ban goc
            EntityDocument eBaseDoc = new EntityDocument();
            JSONObject inObj = new JSONObject(data);
            if (inObj.has(ConstantsFieldParams.BASE_DOCUMENT)) {
                String str = inObj.getString(ConstantsFieldParams.BASE_DOCUMENT);
                if (str != null) {
                    inObj = new JSONObject(str);
                    if (inObj.has(ConstantsFieldParams.DOCUMENT_ID) && inObj.has(ConstantsFieldParams.SYS_ORGANIZATION_ID)) {
                        eBaseDoc.setDocumentId(Long.parseLong(inObj.getString(ConstantsFieldParams.DOCUMENT_ID).trim()));
                        eBaseDoc.setSysOrganizationId(Long.parseLong(inObj.getString(ConstantsFieldParams.SYS_ORGANIZATION_ID).trim()));
                    } else {
                        eBaseDoc = null;
                    }
                } else {
                    eBaseDoc = null;
                }
            } else {
                eBaseDoc = null;
            }

            // lay thong tin cac don vi them vao
            List<EntityVhrOrg> listOrgNeedRepublish = new ArrayList<>();
            JSONArray arrOrgNeedRepublish = FunctionCommon.jsonGetArray(ConstantsFieldParams.LIST_ORG_NEED_REPUBLISH, data);

            if (arrOrgNeedRepublish != null && arrOrgNeedRepublish.length() > 0) {
                for (int i = 0; i < arrOrgNeedRepublish.length(); i++) {
                    JSONObject innerObj = (JSONObject) arrOrgNeedRepublish.get(i);
                    if (innerObj.has(ConstantsFieldParams.SYS_ORGANIZATION_ID)) {
                        EntityVhrOrg eVhr = new EntityVhrOrg();
                        eVhr.setSysOrganizationId(Long.parseLong(innerObj.getString(ConstantsFieldParams.SYS_ORGANIZATION_ID).trim()));
                        listOrgNeedRepublish.add(eVhr);
                    }
                }
            }

            // Lay danh sach pham vi cong bo
            List<Long> docScopeIds = new ArrayList<>();
            if (!json.isNull(ConstantsFieldParams.DOC_SCOPE_IDS)) {
                JSONArray jsonArray = json.getJSONArray(ConstantsFieldParams.DOC_SCOPE_IDS);
                if (jsonArray != null && jsonArray.length() > 0) {
                    JSONObject jsonObject;
                    Long docScopeId;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = jsonArray.getJSONObject(i);
                        if (!jsonObject.isNull(ConstantsFieldParams.SCOPE_ID)) {
                            docScopeId = jsonObject.getLong(ConstantsFieldParams.SCOPE_ID);
                            docScopeIds.add(docScopeId);
                        }
                    }
                }
            }
            //Hiendv2 bo sung path don vi trong pham vi
            // Lay danh sach pham vi cong bo
            List<String> strOrgPath = new ArrayList<String>();
            List<Long> vhrOrgIds = new ArrayList<>();
            if (!json.isNull(ConstantsFieldParams.SCOPE_HAVE_ORGS)) {
                JSONArray jsonArray = json.getJSONArray(ConstantsFieldParams.SCOPE_HAVE_ORGS);
                if (jsonArray != null && jsonArray.length() > 0) {
                    JSONObject jsonObject;
                    String orgPath;
                    Long orgIds;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = jsonArray.getJSONObject(i);
                        if (!jsonObject.isNull(ConstantsFieldParams.PATH_ORG)) {
                            orgPath = jsonObject.getString(ConstantsFieldParams.PATH_ORG);
                            orgIds = Long.parseLong(jsonObject.getString(ConstantsFieldParams.SYS_ORGANIZATION_ID));

//                            System.out.println("Danh sach pham vi:" + docScopeId);
                            strOrgPath.add(orgPath);
                            vhrOrgIds.add(orgIds);
                        }
                    }
                }
            }

            // Thuc hien cong bo van ban
            DocumentDAO documentDAO = new DocumentDAO();
            int result = documentDAO.editTmpPublicationInformation(userId,
                    orgId, documentId, summary, publishedDate, expiredDate,
                    publisherId, builtGroupId, levelPublishedGroup,
                    rangePublished, listAlternativeDocumentId, docScopeIds,
                    listOrgNeedRepublish, eBaseDoc, deadLineRepublish,
                    vhrOrgIds, strOrgPath, scopeNameOrther);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error("editTmpPublicationInformation - userId: " + userId, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * <b>Chinh sua thong tin cong bo</b><br>
     *
     * @author thanght6
     * @since 2016-02-26
     * @param request Doi tuong request tu client -> server
     * @param data Gia tri data truyen theo request
     * @param isSecurity Gia tri isSecurity truyen theo request
     * @return
     */
    public String editPublicationInformation(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);

        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("editPublicationInformation - No session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }

        // Neu user null hoac user id null
        // -> Tra ve thong bao khong co quyen
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
//        if (user == null || user.getUserId() == null) {
//            LOGGER.error("editPublicationInformation - user hoac userId tren he thong 1 null");
//            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
//        }
        Long userId = user == null ? null : user.getUserId();
        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
        log.setUserName(cardId);

        // Giai ma du lieu neu ma hoa
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
                ConstantsFieldParams.DOCUMENT_ID,
                ConstantsFieldParams.PUBLISHED_DATE,
                ConstantsFieldParams.EXPIRED_DATE,
                ConstantsFieldParams.SUMMARY,
                ConstantsFieldParams.ORG_LEVEL,
                ConstantsFieldParams.BUILT_GROUP_ID,
                ConstantsFieldParams.DEAD_LINE_REPUBLISH,
                ConstantsFieldParams.SCOPE_NAME
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);

            // Id cong van
            Long documentId = Long.parseLong(listValue.get(0));

            // Ngay cong bo
            String publishedDate = listValue.get(1);

            // Ngay het hieu luc
            String expiredDate;
            if (json.isNull(ConstantsFieldParams.EXPIRED_DATE)) {
                expiredDate = null;
            } else {
                expiredDate = listValue.get(2);
            }

            // Tom tat
            String summary = listValue.get(3);
            Integer orgLevel = null;

            // Id don vi xay dung
            String strBuiltGroupId = listValue.get(5);
            Long builtGroupId = null;
            if (!CommonUtils.isEmpty(strBuiltGroupId)) {
                builtGroupId = Long.parseLong(strBuiltGroupId);
            }
            // lay du lieu sua thong tin cong bo:
            // lay deadline them vao
            String deadLineRepublish = listValue.get(6);
            //Ten pham vi khac
            String scopeNameOrther = listValue.get(7);
            // Danh sach id van ban thay the them vao
            List<Long> listAdditionalAlternativeDocumentId = null;
            if (!json.isNull(ConstantsFieldParams.ADDITIONAL_ALTERNATIVE_DOCUMENT_IDS)) {
                JSONArray jsonArray = json.getJSONArray(
                        ConstantsFieldParams.ADDITIONAL_ALTERNATIVE_DOCUMENT_IDS);
                if (jsonArray != null && jsonArray.length() > 0) {
                    listAdditionalAlternativeDocumentId = new ArrayList<>();
                    JSONObject jsonObject;
                    Long additionalAlternativeDocumentId;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = jsonArray.getJSONObject(i);
                        if (!jsonObject.isNull(ConstantsFieldParams.DOCUMENT_ID)) {
                            additionalAlternativeDocumentId = jsonObject.getLong(
                                    ConstantsFieldParams.DOCUMENT_ID);
                            listAdditionalAlternativeDocumentId.add(additionalAlternativeDocumentId);
                        }
                    }
                }
            }

            // Danh sach id van ban thay the loai bo
            List<Long> listRemovalAlternativeDocumentId = null;
            if (!json.isNull(ConstantsFieldParams.REMOVAL_ALTERNATIVE_DOCUMENT_IDS)) {
                JSONArray jsonArray = json.getJSONArray(
                        ConstantsFieldParams.REMOVAL_ALTERNATIVE_DOCUMENT_IDS);
                if (jsonArray != null && jsonArray.length() > 0) {
                    listRemovalAlternativeDocumentId = new ArrayList<>();
                    JSONObject jsonObject;
                    Long removalAlternativeDocumentId;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = jsonArray.getJSONObject(i);
                        if (!jsonObject.isNull(ConstantsFieldParams.DOCUMENT_ID)) {
                            removalAlternativeDocumentId = jsonObject.getLong(
                                    ConstantsFieldParams.DOCUMENT_ID);
                            listRemovalAlternativeDocumentId.add(removalAlternativeDocumentId);
                        }
                    }
                }
            }
            List<Long> docScopeIds = new ArrayList<Long>();
            if (!json.isNull(ConstantsFieldParams.DOC_SCOPE_IDS)) {
                JSONArray jsonArray = json.getJSONArray(ConstantsFieldParams.DOC_SCOPE_IDS);
                if (jsonArray != null && jsonArray.length() > 0) {
                    JSONObject jsonObject;
                    Long docScopeId;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = jsonArray.getJSONObject(i);
                        if (!jsonObject.isNull(ConstantsFieldParams.SCOPE_ID)) {
                            docScopeId = jsonObject.getLong(ConstantsFieldParams.SCOPE_ID);
//                            System.out.println("Danh sach pham vi:" + docScopeId);
                            docScopeIds.add(docScopeId);
                        }
                    }
                }
            }

            // Lay van ban goc ::cuongnv
            EntityDocument eBaseDoc = new EntityDocument();
            JSONObject inObj = new JSONObject(data);
            if (inObj.has(ConstantsFieldParams.BASE_DOCUMENT)) {
                String str = inObj.getString(ConstantsFieldParams.BASE_DOCUMENT);
                if (str != null) {
                    inObj = new JSONObject(str);
                    if (inObj.has(ConstantsFieldParams.DOCUMENT_ID) && inObj.has(ConstantsFieldParams.SYS_ORGANIZATION_ID)) {
                        eBaseDoc.setDocumentId(Long.parseLong(inObj.getString(ConstantsFieldParams.DOCUMENT_ID).trim()));
                        eBaseDoc.setSysOrganizationId(Long.parseLong(inObj.getString(ConstantsFieldParams.SYS_ORGANIZATION_ID).trim()));
                    } else {
                        eBaseDoc = null;
                    }
                } else {
                    eBaseDoc = null;
                }
            } else {
                eBaseDoc = null;
            }
            // Lay danh sach don vi pham vi ::cuongnv
            List<EntityVhrOrg> listOrgNeedRepublish = new ArrayList<>();
            JSONArray arrOrgNeedRepublish = FunctionCommon.jsonGetArray(ConstantsFieldParams.LIST_ORG_NEED_REPUBLISH, data);

            if (arrOrgNeedRepublish != null && arrOrgNeedRepublish.length() > 0) {
                for (int i = 0; i < arrOrgNeedRepublish.length(); i++) {
                    JSONObject innerObj = (JSONObject) arrOrgNeedRepublish.get(i);
                    if (innerObj.has(ConstantsFieldParams.SYS_ORGANIZATION_ID)) {
                        EntityVhrOrg eVhr = new EntityVhrOrg();
                        eVhr.setSysOrganizationId(Long.parseLong(innerObj.getString(ConstantsFieldParams.SYS_ORGANIZATION_ID).trim()));
                        listOrgNeedRepublish.add(eVhr);
                    }
                }
            }
            //Hiendv2 bo sung path don vi trong pham vi
            // Lay danh sach pham vi cong bo
            List<String> strOrgPath = new ArrayList<String>();
            List<Long> vhrOrgIds = new ArrayList<>();
            if (!json.isNull(ConstantsFieldParams.SCOPE_HAVE_ORGS)) {
                JSONArray jsonArray = json.getJSONArray(ConstantsFieldParams.SCOPE_HAVE_ORGS);
                if (jsonArray != null && jsonArray.length() > 0) {
                    JSONObject jsonObject;
                    String orgPath;
                    Long orgIds;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = jsonArray.getJSONObject(i);
                        if (!jsonObject.isNull(ConstantsFieldParams.PATH_ORG)) {
                            orgPath = jsonObject.getString(ConstantsFieldParams.PATH_ORG);
                            orgIds = Long.parseLong(jsonObject.getString(ConstantsFieldParams.SYS_ORGANIZATION_ID));

//                            System.out.println("Danh sach pham vi:" + docScopeId);
                            strOrgPath.add(orgPath);
                            vhrOrgIds.add(orgIds);
                        }
                    }
                }
            }
            DocumentDAO documentDAO = new DocumentDAO();

            int result = documentDAO.editPublicationInformation(userId,
                    userGroup.getGroupId2(), documentId, publishedDate, expiredDate,
                    summary, orgLevel, listAdditionalAlternativeDocumentId,
                    listRemovalAlternativeDocumentId, builtGroupId, docScopeIds,
                    strOrgPath, vhrOrgIds, listOrgNeedRepublish,
                    eBaseDoc, deadLineRepublish, scopeNameOrther);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);

        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error("editPublicationInformation - userId: " + userId, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * <b>Huy cong bo</b><br>
     *
     * @author thanght6
     * @since 2016-02-26
     * @param request Doi tuong request tu client -> server
     * @param data Gia tri data truyen theo request
     * @param isSecurity Gia tri isSecurity truyen theo request
     * @return 1 - Thanh cong
     */
    public String cancelPublish(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);

        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("cancelPublish - No session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }

        // Neu user hoac user id tren he thong 1 null
        // -> Tra ve thong bao khong co quyen
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
//        if (user == null || user.getUserId() == null) {
//            LOGGER.error("cancelPublish - user hoac userId tren he thong 1 null");
//            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
//        }
        Long userId = user == null ? null : user.getUserId();
        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
        log.setUserName(cardId);
        // Giai ma du lieu neu ma hoa
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
                ConstantsFieldParams.DOCUMENT_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);

            // Id van ban
            Long documentId = Long.parseLong(listValue.get(0));

            // Thuc hien huy cong bo
            DocumentDAO documentDAO = new DocumentDAO();
            int result = documentDAO.cancelPublish(userId, documentId);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);

        } catch (Exception ex) {
            LOGGER.error("cancelPublish - userId: " + userId, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * <b>Tao danh sach thread de dem</b><br/>
     *
     * @author thanght6
     * @since May 21, 2016
     * @param userId1 Id nguoi dung tren he thong 1
     * @param userId2 Id nguoi dung tren he thong 2
     * @param countType Cach dem
     * @param documentDAO Doi tuong truy van bang document
     * @return
     */
    private List<DocumentCountThread> generateListDocumentCountThread(
            Long userId1, Long userId2, Integer countType, DocumentDAO documentDAO,
            List<Long> listSecretary) {

        List<DocumentCountThread> listThread = new ArrayList<DocumentCountThread>();
        if (countType == null
                || (countType != Constants.Document.CountType.ALL
                && countType != Constants.Document.CountType.ALL_STATUS_OF_RECEIVED_DOCUMENT)) {
            LOGGER.error("generateListDocumentCountThread - Loi countType khong hop le"
                    + " - countType: " + countType);
            return listThread;
        }
        DocumentCountThread thread;
        // Neu la dem tat ca cac loai van ban
        // -> Them thread dem van ban nguoi dung tao ra va chuyen di
        if (countType == Constants.Document.CountType.ALL) {
            // Dem van ban nguoi dung chuyen di
            thread = new DocumentCountThread(userId1, userId2,
                    Constants.Document.Type.SENDER, null, documentDAO, null);
            listThread.add(thread);
        }
        // Dem van ban nguoi dung tao
        thread = new DocumentCountThread(userId1, userId2,
                Constants.Document.Type.CREATOR, null, documentDAO, listSecretary);
        listThread.add(thread);
        
        // Dem van ban nguoi dung nhan duoc
        thread = new DocumentCountThread(userId1, userId2,
                Constants.Document.Type.RECEIVER,
                Constants.Document.Status.ALL, documentDAO, null);
        listThread.add(thread);

        // Dem van ban nguoi dung nhan duoc va chua xu ly
        thread = new DocumentCountThread(userId1, userId2,
                Constants.Document.Type.RECEIVER,
                Constants.Document.Status.NEW, documentDAO, null);
        listThread.add(thread);

        // Dem van ban nguoi dung nhan duoc va chua doc
        thread = new DocumentCountThread(userId1, userId2,
                Constants.Document.Type.RECEIVER,
                Constants.Document.Status.UNREAD, documentDAO, null);
        listThread.add(thread);

        // Dem van ban nguoi dung nhan duoc va da doc
        thread = new DocumentCountThread(userId1, userId2,
                Constants.Document.Type.RECEIVER,
                Constants.Document.Status.READ, documentDAO, null);
        listThread.add(thread);

        // Dem van ban nguoi dung nhan duoc va dang xu ly
        thread = new DocumentCountThread(userId1, userId2,
                Constants.Document.Type.RECEIVER,
                Constants.Document.Status.PROCESSING, documentDAO, null);
        listThread.add(thread);

        // Dem van ban nguoi dung nhan duoc va da luu
        thread = new DocumentCountThread(userId1, userId2,
                Constants.Document.Type.RECEIVER,
                Constants.Document.Status.SAVED, documentDAO, null);
        listThread.add(thread);

        return listThread;
    }

    /**
     * <b>Dem van ban</b><br>
     *
     * @author thanght6
     * @since May 21, 2016
     * @param request Doi tuong request tu client -> server
     * @param data Gia tri data truyen theo request
     * @param isSecurity Gia tri isSecurity truyen theo request
     *
     * @return
     */
    public String countDocument(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Tao doi tuong ghi log
        String response;
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("countDocument - No session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Neu user hoac user id tren he thong 1&2 null
        // -> Tra ve thong bao khong co quyen
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        if ((user1 == null || user1.getUserId() == null)
                && (user2 == null || user2.getUserId() == null)) {
            LOGGER.error("countDocument - user hoac userId tren he thong 1&2 null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Id nguoi dung tren he thong 1
        Long userId1 = null;
        if ((user1 != null && user1.getUserId() != null)) {
            userId1 = user1.getUserId();
        }
        // Id nguoi dung tren he thong 2
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
                ConstantsFieldParams.COUNT_TYPE,
                ConstantsFieldParams.TYPE,
                ConstantsFieldParams.STATUS,};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Loai dem
            Integer countType = Integer.parseInt(listValue.get(0));

            // Loai danh sach 
            Integer type = null;
            String strType = listValue.get(1);
            if (!CommonUtils.isEmpty(strType)) {
                type = Integer.parseInt(strType);
            }

            Integer status = null;
            String strStatus = listValue.get(2);
            if (!CommonUtils.isEmpty(strStatus)) {
                status = Integer.parseInt(strStatus);
            }

            DocumentDAO documentDAO = new DocumentDAO();
            Object result;
            // Neu la dem tat ca cac loai van ban
            // hoac tat ca trang thai theo van ban nguoi dung nhan duoc
            // -> Tao multi-thread de dem dong thoi
            if (countType == Constants.Document.CountType.ALL
                    || countType == Constants.Document.CountType.ALL_STATUS_OF_RECEIVED_DOCUMENT) {
                List<DocumentCountThread> listThread = generateListDocumentCountThread(
                        userId1, userId2, countType, documentDAO, user2.getListSecretaryVhrOrg());
                // Neu khong tao duoc danh sach thread de dem tat ca dong thoi
                // -> Tra ve thong bao loi server
                if (CommonUtils.isEmpty(listThread)) {
                    LOGGER.error("countDocument - userId: " + userId1
                            + " -  listThread null hoac rong ");
                    return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                            null, null);
                }
                // Tao pool thread co size bang so luong thread
                ExecutorService pool = Executors.newFixedThreadPool(listThread.size());
                // Thuc thi tung thread trong pool
                for (DocumentCountThread thread : listThread) {
                    pool.execute(thread);
                }
                // Doi tat ca cac thread trong pool thuc hien xong thi shutdown pool
                pool.shutdown();
                // Shutdown pool neu sau 20s ma van con thread chua thuc hien xong
                pool.awaitTermination(Constants.DateTime.TIMEOUT_OF_THREAD_IN_SECOND_UNIT,
                        TimeUnit.SECONDS);
                EntityDocumentCount documentCount = new EntityDocumentCount();
                EntityReceivedDocumentCount receivedDocumentCount = new EntityReceivedDocumentCount();
                Integer count;
//                Date countStartTime;
//                Date endTime;
                for (DocumentCountThread thread : listThread) {
                    // Loai danh sach van ban
                    type = thread.getType();
                    // Trang thai van ban trong danh sach van ban nguoi dung nhan
                    status = thread.getStatus();
                    // So luong dem duoc
                    count = thread.getCount();
                    // Thoi gian bat dau
//                    countStartTime = thread.getStartTime();
                    // Thoi gian ket thuc
//                    endTime = thread.getEndTime();
                    if (type == null || (type == Constants.Document.Type.RECEIVER
                            && status == null)) {
                        break;
                    }
                    // Gan so luong van ban tuong ung voi tung loai va trang thai
                    switch (type) {
                        // Van ban nguoi dung nhan
                        case Constants.Document.Type.RECEIVER:
                            switch (status) {
                                // Tat ca
                                case Constants.Document.Status.ALL:
                                    receivedDocumentCount.setAll(count);
                                    break;
                                // Moi
                                case Constants.Document.Status.NEW:
                                    receivedDocumentCount.setNewDocument(count);
                                    break;
                                // Chua doc
                                case Constants.Document.Status.UNREAD:
                                    receivedDocumentCount.setUnread(count);
                                    break;
                                // Da doc
                                case Constants.Document.Status.READ:
                                    receivedDocumentCount.setRead(count);
                                    break;
                                // Dang xu ly
                                case Constants.Document.Status.PROCESSING:
                                    receivedDocumentCount.setProcessing(count);
                                    break;
                                // Da luu
                                case Constants.Document.Status.SAVED:
                                    receivedDocumentCount.setSaved(count);
                                    break;
                            }
                            break;
                        // Van ban nguoi dung tao ra
                        case Constants.Document.Type.CREATOR:
                            documentCount.setCreated(count);
                            break;
                        // Van ban nguoi dung chuyen di
                        case Constants.Document.Type.SENDER:
                            documentCount.setSent(count);
                            break;
                    }
                }
                documentCount.setReceived(receivedDocumentCount);
                result = documentCount;
                response = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
            } // Tim kiem theo 1 loai nhat dinh
            else if (countType == Constants.Document.CountType.ONE_TYPE) {
                result = documentDAO.countDocument(userId1, userId2, type, status, user2.getListSecretaryVhrOrg());
                response = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
            } // countType khong hop le
            else {
                LOGGER.error("countDocument - userId1: " + userId1
                        + " - userId2: " + userId2
                        + " - countType khong hop le countType: " + countType);
                response = FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
        } // Du lieu dau vao khong hop le
        catch (JSONException | NumberFormatException ex) {
            LOGGER.error(ex.getMessage(), ex);
            response = FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        } // Loi Server
        catch (Exception ex) {
            LOGGER.error("countDocument - userId: " + userId1 + " -userId2: "
                    + userId2, ex);
            response = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return response;
    }

    /**
     * <b>Tim kiem van ban<b></br>
     *
     * @author thanght6
     * @since May 17, 2016
     * @param request Doi tuong request tu client -> server
     * @param data Gia tri data truyen theo request
     * @param isSecurity Gia tri isSecurity truyen theo request
     * @return
     */
    public String search(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        Date startTime = new Date();
        String deviceName = null;
        ErrorCode errorCode;
        String response;
        EntityDocument document = null;
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("search (Tim kiem cong vang) - Sessing timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Neu user hoac user id tren he thong 1&2 null
        // -> Tra ve thong bao khong co quyen
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        if (!userGroup.checkUserId()) {
            LOGGER.error("search (Tim kiem cong van) - user hoac userId tren he"
                    + " thong 1&2 null!");
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
        String stringSearch = "";
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.TYPE,
                ConstantsFieldParams.STATUS,
                ConstantsFieldParams.ADJACENT_ID,
                ConstantsFieldParams.KEYWORD,
                ConstantsFieldParams.DOCUMENT,
                ConstantsFieldParams.STAFF_IDS,
                ConstantsFieldParams.START_RECORD,
                ConstantsFieldParams.PAGE_SIZE,
                ConstantsFieldParams.IS_SEARCH_DOC_MOBILE,
                ConstantsFieldParams.IS_FINANCE_TEXT,
                ConstantsFieldParams.DEVICE_NAME,
                ConstantsFieldParams.IS_NOT_DUPLICATE_RECEIVED_DOCUMENT,
                "isVTDocument"
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Loai
            Integer type = Integer.parseInt(listValue.get(0));
            // Trang thai
            Integer status = Integer.parseInt(listValue.get(1));
            // Tu khoa
            String keyword = listValue.get(3);
            // Chuoi doi tuong van ban
            String strDocument = listValue.get(4);
            //Loai van ban lien ke
            String strAdjacent = listValue.get(2);
            // Parse chuoi ra doi tuong EntityDocument            
            if (!CommonUtils.isEmpty(strDocument)) {
                Gson gson = new Gson();
                document = gson.fromJson(strDocument, EntityDocument.class);
            }
            // Neu keyword khac rong (Tim kiem nhanh)
            // Gan cac truong mong muon tim kiem theo tu khoa
            keyword = keyword.replaceAll("“", "\"").replaceAll("”", "\"");
            if (!CommonUtils.isEmpty(keyword)) {
                if (document == null) {
                    document = new EntityDocument();
                }
                document.setCode(keyword);
                document.setTitle(keyword);
                document.setSigner(keyword);
                document.setQuickSearch(true);
                if (CommonUtils.isInteger(keyword)) {
                    Long documentId = Long.parseLong(keyword);
                    document.setDocumentId(documentId);
                }
            }
            // Chuoi cac id nguoi gui
            String staffIds = listValue.get(5);
            // Vi tri lay ra
            Long startRecord = 0L;
            String strStartRecord = listValue.get(6);
            if (!CommonUtils.isEmpty(strStartRecord)) {
                startRecord = Long.parseLong(strStartRecord);
            }
            Long pageSize = 10L;
            String strPageSize = listValue.get(7);
            if (!CommonUtils.isEmpty(strPageSize)) {
                pageSize = Long.parseLong(strPageSize);
            }
            // Tim kiem count theo mobile
            String isSearchMobile = listValue.get(8);
            // Tim kiem van ban tai chinh
            Boolean isFinanceText = false;
            if (listValue.get(9) != null && "true".equals(listValue.get(9))) {
                isFinanceText = true;
            }
            // Ten thiet bi
            deviceName = listValue.get(10);

            // Loc trung van ban nhan duoc
            Integer isNotDuplicateReceivedDocument = null;
            String strIsNotDuplicateReceivedDocument = listValue.get(11);
            if (!CommonUtils.isEmpty(strIsNotDuplicateReceivedDocument)) {
                isNotDuplicateReceivedDocument = Integer.parseInt(strIsNotDuplicateReceivedDocument);
            }
            // null: Tim tat ca
            // 0: Tim van ban ngoai tap doan
            // 1: Tim van ban trong tap doan
            Integer isVTDocument = null;
            if (!CommonUtils.isEmpty(listValue.get(12))) {
                isVTDocument = Integer.parseInt(listValue.get(12));
            }
            // Van ban lien ke
            Integer adjacent = 0;
            if (!CommonUtils.isEmpty(strAdjacent)) {
                adjacent = Integer.parseInt(strAdjacent);
            }

            // Truy van database
            List<Long> lsSecretaryOrgIdVof2 = null;
            if (user2 != null) {
                lsSecretaryOrgIdVof2 = user2.getListSecretaryVhrOrg();
            }
            DocumentDAO documentDAO = new DocumentDAO();

            List<EntityDocument> result;
//            System.out.println("======:" + isSearchMobile);
            if (((!CommonUtils.isEmpty(isSearchMobile) && "1".equals(isSearchMobile)) || strDocument.trim().length() <= 3) && !CommonUtils.isEmpty(keyword) && (type == null || type.intValue() != Constants.Document.Type.CREATOR)) {
                ElasticSearchDocument elasticSearchDocument = new ElasticSearchDocument();
                result = elasticSearchDocument.getListSearchDocument(userId1,
                        userId2, user2, type, status,
                        adjacent, document, staffIds, startRecord, pageSize);
                stringSearch = "ELASTICSERCH";
            } else {
                result = documentDAO.search(userId1, userId2, user2,
                        type, status, adjacent, document, staffIds, startRecord, pageSize,
                        isSearchMobile, isFinanceText, lsSecretaryOrgIdVof2,
                        isNotDuplicateReceivedDocument, isVTDocument);
                stringSearch = "DATABASESERCH";
            }
            // Neu tim kiem co ket qua
            // -> Lay danh sach file dinh kem cho tung van ban tim kiem duoc            
            if (!CommonUtils.isEmpty(result)) {
                FilesAttachmentDAO filesAttachmentDAO = new FilesAttachmentDAO();
                List<Long> listDocumentId = new ArrayList<>();
                for (EntityDocument doc : result) {
                    listDocumentId.add(doc.getDocumentId());
                    // MinhNQ add lay ra don vi da dong dau start
                    // Cho nut an/hien
                    int marked = documentDAO.getMarked(doc.getDocumentId());
                    if(marked == 1){
                        doc.setMarked(1);
                    }
                    else doc.setMarked(0);
                    // MinhNQ add lay ra don vi da dong dau end
                    // Cho nut an/hien
                }

                // Lay danh sach file dinh kem cua danh sach van ban
                List<EntityFilesAttachment> listAttachment = filesAttachmentDAO
                        .getListAttachment(userId1, listDocumentId);
                // Gan danh sach file kem vao tung van ban tuong ung
                if (!CommonUtils.isEmpty(listAttachment)) {
                    List<EntityFilesAttachment> listAttachmentForDocument;
                    for (EntityDocument doc : result) {
                        listAttachmentForDocument = new ArrayList<>();
                        for (EntityFilesAttachment attachment : listAttachment) {
                            if (attachment.getDocumentId().equals(doc.getDocumentId())) {
                                listAttachmentForDocument.add(attachment);
                            }
                        }
                        doc.setListAttachment(listAttachmentForDocument);
                    }
                }
                // Lay danh sach file bieu mau cua danh sach van ban
                AttachDAO attachDAO = new AttachDAO();
                List<EntityFileAttachment> listAttachTemplate = attachDAO
                        .getListAttachTemplate(userId2, listDocumentId, false);
                // Khoi tao danh sach file bieu mau cho tung van ban
                if (!CommonUtils.isEmpty(listAttachTemplate)) {
                    List<EntityFileAttachment> listAttachTemplateForDocument;
                    for (EntityDocument doc : result) {
                        listAttachTemplateForDocument = new ArrayList<>();
                        for (EntityFileAttachment attachTemplate : listAttachTemplate) {
                            if (doc.getDocumentId().equals(attachTemplate.getDocumentId())) {
                                listAttachTemplateForDocument.add(attachTemplate);
                            }
                        }
                        doc.setListAttachTemplate(listAttachTemplateForDocument);
                    }
                }
            }
            errorCode = ErrorCode.SUCCESS;
            response = FunctionCommon.generateResponseJSON(errorCode, result, aesKey);
        } // Loi server
        catch (JSONException | NumberFormatException | JsonSyntaxException ex) {
            LOGGER.error("search (Tim kiem cong van) - Exception - username: "
                    + cardId + "\ndata: " + data, ex);
            errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
            response = FunctionCommon.generateResponseJSON(errorCode, null, null);
        }
        String functionName = "DocumentAction.search";
        String description = "SERVICE2: Lay danh sach van ban - errorCode: "
                + errorCode.getErrorCode() + ", TIMKIEM: "+ stringSearch;
        String content = "";
        EntityActionLogMobile logDB = new EntityActionLogMobile(userId1, cardId,
                startTime, new Date(), functionName, description, content, deviceName, document);
        LogUtils.insertActionLogMobile(userId2, logDB);
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return response;
    }

    /**
     * <b>Sinh danh sach thread de lay chi tiet cong van</b><br>
     *
     * @param userId1 Id user tren he thong 1
     * @param userId2 Id user tren he thong 2
     * @param senderId1 Id nguoi gui tren he thong 1
     * @param senderId2 Id nguoi gui tren he thong 2
     * @param documentId Id cong van
     * @return
     */
    private List<DocumentDetailThread> generateListDocumentDetailThread(
            Long userId1, Long userId2, Long senderId1, Long senderId2, Long documentId,
            Boolean isReceiverShow, String isVersionNew, Long documentInStaffId) {

        List<DocumentDetailThread> listThread = new ArrayList<>();
        // Kiem tra dau vao
        if (userId1 == null && userId2 == null) {
            LOGGER.error("generateListDocumentDetailThread - userId null");
            return listThread;
        }
        DocumentDAO documentDAO = new DocumentDAO();
        FilesAttachmentDAO filesAttachmentDAO = new FilesAttachmentDAO();
        DocumentInStaffDAO documentInStaffDAO = new DocumentInStaffDAO();
        DocumentInGroupDAO documentInGroupDAO = new DocumentInGroupDAO();
        TextDAO textDAO = new TextDAO();
        DocumentDetailThread thread;

        // Thread lay thong tin chi tiet van ban
        thread = new DocumentDetailThread(userId1, userId2, senderId1, senderId2,
                documentId, documentInStaffId, Constants.Document.DetailType.DETAIL,
                documentDAO, null, null, null, null);

        listThread.add(thread);

        // Thread lay danh sach file dinh kem
        thread = new DocumentDetailThread(userId1, userId2, senderId1, senderId2,
                documentId, documentInStaffId, Constants.Document.DetailType.LIST_ATTACHMENT,
                null, filesAttachmentDAO, null, null, null);
        listThread.add(thread);

        // Thread lay danh sach y kien chi dao
        thread = new DocumentDetailThread(userId1, userId2, senderId1, senderId2,
                documentId, documentInStaffId, Constants.Document.DetailType.LIST_COMMENT,
                documentDAO, null, null, null, null);

        thread.setIsReceiverShow(isReceiverShow);
        listThread.add(thread);
        if (CommonUtils.isEmpty(isVersionNew) || !"1".equals(isVersionNew)) {
            // Thread lay danh sach nguoi cung nhan van ban voi nguoi dung
            thread = new DocumentDetailThread(userId1, userId2, senderId1, senderId2,
                    documentId, documentInStaffId, Constants.Document.DetailType.LIST_RECEIVER_SAME_USER,
                    null, null, documentInStaffDAO, null, null);
            listThread.add(thread);

            // Thread lay danh sach nguoi nhan van ban tu nguoi dung
            thread = new DocumentDetailThread(userId1, userId2, senderId1, senderId2,
                    documentId, documentInStaffId, Constants.Document.DetailType.LIST_RECEIVER,
                    null, null, documentInStaffDAO, null, null);
            listThread.add(thread);
        }
        // Thread lay danh sach don vi nhan van ban do tu nguoi dung
        thread = new DocumentDetailThread(userId1, userId2, senderId1, senderId2,
                documentId, documentInStaffId, Constants.Document.DetailType.LIST_GROUP,
                null, null, null, documentInGroupDAO, null);
        listThread.add(thread);

        // Thread lay danh sach nguoi ky
        thread = new DocumentDetailThread(userId1, userId2, senderId1, senderId2,
                documentId, documentInStaffId, Constants.Document.DetailType.LIST_SIGNER,
                null, null, null, null, textDAO);
        listThread.add(thread);

        // Thread lay danh sach nhom van ban da chuyen
        thread = new DocumentDetailThread(userId1, userId2, senderId1, senderId2,
                documentId, documentInStaffId,
                Constants.Document.DetailType.LIST_CV_GROUP_RECEIVER, documentDAO,
                null, null, null, null);

        listThread.add(thread);
        return listThread;
    }

    /**
     * <b>Lay chi tiet van ban</b><br>
     *
     * @author thanght6
     * @since May 24, 2016
     * @param request Doi tuong request tu client -> server
     * @param data Gia tri data truyen theo request
     * @param isSecurity Gia tri isSecurity truyen theo request
     *
     * @return
     */
    public String getDocumentDetail(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getDocumentDetail (Lay chi tiet van ban) - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Neu user hoac user id tren he thong 1&2 null
        // -> Tra ve thong bao khong co quyen
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        if (!userGroup.checkUserId()) {
            LOGGER.error("getDocumentDetail (Lay chi tiet van ban) - user hoac"
                    + " userId tren he thong 1&2 null!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Neu user1 null thi gan userId1 bang null
        Long userId1 = user1 == null ? null : user1.getUserId();
        // Neu user2 null thi gan userId2 bang null
        Long userId2 = user2 == null ? null : user2.getUserId();

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
                ConstantsFieldParams.SENDER_ID1,
                ConstantsFieldParams.SENDER_ID2,
                ConstantsFieldParams.DOCUMENT_ID,
                "isReceiverShow",
                ConstantsFieldParams.SENDER_ID_VOF1,
                ConstantsFieldParams.SENDER_ID_VOF2,
                ConstantsFieldParams.IS_VERSION_NEW,
                ConstantsFieldParams.DOCUMENT_IN_STAFF_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Id nguoi gui tren he thong 1
            Long senderId1 = null;
            String strSenderId1 = listValue.get(0);
            if (!CommonUtils.isEmpty(strSenderId1)) {
                senderId1 = Long.parseLong(strSenderId1);
            }
            //Id nguoi gui tren he thong 2
            Long senderId2 = null;
            String strSenderId2 = listValue.get(1);
            if (!CommonUtils.isEmpty(strSenderId2)) {
                senderId2 = Long.parseLong(strSenderId2);
            }
            // Id van ban
            Long documentId = null;
            if (!CommonUtils.isEmpty(listValue.get(2))) {
                documentId = Long.parseLong(listValue.get(2));
            }
            //option hien thi thong tin nguoi nhan trong danh sach y kien chuyen van ban
            String strIsReceiverShow = listValue.get(3);
            Boolean isReceiverShow = false;
            if (!CommonUtils.isEmpty(strIsReceiverShow) && "1".equals(strIsReceiverShow)) {
                isReceiverShow = true;
            }

            Long idVof1 = null;
            Long idVof2 = null;
            if (!CommonUtils.isEmpty(listValue.get(4))) {
                try {
                    idVof1 = Long.parseLong(listValue.get(4));
                } catch (Exception ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
            if (!CommonUtils.isEmpty(listValue.get(5))) {
                try {
                    idVof2 = Long.parseLong(listValue.get(5));
                } catch (Exception ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }

            if (idVof1 != null || idVof2 != null) {
                userId1 = idVof1;
                userId2 = idVof2;
            }
            String isVersionNew = listValue.get(6);
            Long documentInStaffId = null;
            if (!CommonUtils.isEmpty(listValue.get(7))) {
                try {
                    documentInStaffId = Long.parseLong(listValue.get(7));
                } catch (Exception ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }

            // [tuantm30 - start] Bo sung them lay thong tin nguoi gui neu ko truyen len
            if (documentInStaffId != null && senderId2 == null) {
                DocumentInStaffDAO dao = new DocumentInStaffDAO();
                EntityDocument result = dao.getDocumentInStaffDetail(documentInStaffId, false);
                if (result != null) {
                    senderId2 = result.getSenderId2();
                    documentId = result.getDocumentId();
                }
            }
            // [tuantm30 - end] Bo sung them lay thong tin nguoi gui neu ko truyen len
            
            // Tao multi-thread de thuc hien lay nhieu thong tin chi tiet dong thoi
            // cho van ban
            List<DocumentDetailThread> listThread = generateListDocumentDetailThread(
                    userId1, userId2, senderId1, senderId2, documentId,
                    isReceiverShow, isVersionNew, documentInStaffId);
            // Neu khong tao duoc danh sach thread
            // -> Tra ve thong bao loi server
            if (CommonUtils.isEmpty(listThread)) {
                LOGGER.error("getDocumentDetail (Lay chi tiet van ban) - listThread"
                        + " null hoac rong - username: " + cardId + "\ndata: " + data);
                return FunctionCommon.generateResponseJSON(
                        ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            }
            // Tao pool quan ly thread co kich thuoc bang so luong thread da tao o tren
            ExecutorService pool = Executors.newFixedThreadPool(listThread.size());
            // Thuc thi tung thread trong pool
            for (DocumentDetailThread thread : listThread) {
                pool.execute(thread);
            }
            // Doi tat ca thread trong pool thuc hien xong thi shutdown pool
            // (Neu khong shutdown se dan den hien tuong cac thread trong pool van duoc duy tri
            // mac du da thuc hien xong cong viec va khi co request moi lai thuc hien tao pool
            // va thread lam cho so luong thread tang dot bien, cao tai server)
            pool.shutdown();
            // Sau 20s ma van con thread chua thuc hien xong
            // -> Thuc hien kill thread va shutdown pool
            pool.awaitTermination(Constants.DateTime.TIMEOUT_OF_THREAD_IN_SECOND_UNIT,
                    TimeUnit.SECONDS);
            // Thong tin chi tiet
            EntityDocument result = null;
            // Danh sach file dinh kem
            List<EntityFilesAttachment> listAttachment = null;
            // Danh sach y kien chi dao
            List<EntityDocumentInStaff> listComment = null;
            // Danh sach nguoi cung nhan van ban voi nguoi dung
            List<EntityDocumentInStaff> listReceiverSameUser = null;
            // Danh sach nguoi nhan van ban do tu nguoi dung
            List<EntityDocumentInStaff> listReceiver = null;
            // Danh sach don vi nhan van ban tu nguoi dung
            List<EntityDocumentInGroup> listGroup = null;
            // Danh sach nguoi ky chinh
            List<EntityText> listMainSigner = null;
            // Danh sach nguoi ky ra soat
            List<EntityText> listReviewSigner = null;
            // Danh sach nguoi ky nhay
            List<EntityText> listFlashingSigner = null;
            // Danh sach nhom van ban chuyen
            List<EntityCvGroup> listCvGroup = null;
            int type;
            for (DocumentDetailThread thread : listThread) {
                // Loai
                type = thread.getType();
                switch (type) {
                    // Thread lay thong tin chi tiet van ban
                    case Constants.Document.DetailType.DETAIL:
                        result = thread.getDocument();
                        break;
                    // Thread lay danh sach file dinh kem
                    case Constants.Document.DetailType.LIST_ATTACHMENT:
                        listAttachment = thread.getListAttachment();
                        break;
                    // Thread lay danh sach y kien chi dao cho van ban
                    case Constants.Document.DetailType.LIST_COMMENT:
                        listComment = thread.getListComment();
                        break;
                    // Thread lay danh sach nguoi cung nhan van ban voi nguoi dung
                    case Constants.Document.DetailType.LIST_RECEIVER_SAME_USER:
                        // Bo nguoi dang nhap ra khoi danh sanh
                        // Danh sach nguoi cung nhan van ban co ca user dang nhap
                        List<EntityDocumentInStaff> listReceiverSameUserWithUser = thread.getListReceiverSameUser();
                        // Danh sach nguoi cung nhan van ban ma khong co user dang nhap
                        listReceiverSameUser = new ArrayList<>();
                        // Kiem tra danh sach nguoi cung nhan co ca user dang nhap
                        // Neu khac null hoac rong
                        // -> Tien hanh duyet danh sach de loc bo nguoi dang nhap
                        if (!CommonUtils.isEmpty(listReceiverSameUserWithUser)) {
                            Long receiverId1;
                            Long receiverId2;
                            for (EntityDocumentInStaff receiver : listReceiverSameUserWithUser) {
                                receiverId1 = receiver.getReceiverId();
                                receiverId2 = receiver.getReceiverId2();
                                // Neu id nguoi nhan 1&2 cung khac user dang nhap
                                // -> Them nguoi nhan vao danh sach nguoi cung nhan
                                // ma khong co user dang nhap
                                if ((receiverId1 == null || userId1 == null
                                        || !receiverId1.equals(userId1))
                                        && (receiverId2 == null || userId2 == null
                                        || !receiverId2.equals(userId2))) {
                                    listReceiverSameUser.add(receiver);
                                }
                            }
                        }
                        break;
                    // Thread lay danh sach nguoi nhan van ban tu nguoi dung
                    case Constants.Document.DetailType.LIST_RECEIVER:
                        listReceiver = thread.getListReceiver();
                        break;
                    // Thread lay danh sach don vi nhan van ban tu nguoi dung
                    case Constants.Document.DetailType.LIST_GROUP:
                        listGroup = thread.getListGroup();
                        break;
                    case Constants.Document.DetailType.LIST_SIGNER:
                        TextSignDAO textSignDAO = new TextSignDAO();
                        listMainSigner = thread.getListMainSigner();
                        if (!CommonUtils.isEmpty(listMainSigner)) {
                            // Lay fie dinh kem nguoi ky chinh
                            for (EntityText etext : listMainSigner) {
                                etext.setLstFilesCommentSign(textSignDAO.getFilesSign(
                                        Long.parseLong(etext.getTextProcessId())));
                            }
                        }
                        listReviewSigner = thread.getListReviewSigner();
                        // Lay file dinh kem nguoi ky ra soat
                        if (!CommonUtils.isEmpty(listReviewSigner)) {
                            for (EntityText etext : listReviewSigner) {
                                etext.setLstFilesCommentSign(textSignDAO.getFilesSign(
                                        Long.parseLong(etext.getTextProcessId())));
                            }
                        }
                        listFlashingSigner = thread.getListFlashingSigner();
                        if (!CommonUtils.isEmpty(listFlashingSigner)) {
                            // Lay file dinh kem nguoi ky nhay
                            for (EntityText etext : listFlashingSigner) {
                                etext.setLstFilesCommentSign(textSignDAO.getFilesSign(
                                        Long.parseLong(etext.getTextProcessId())));
                            }
                        }
                        break;
                    // Thread lay danh sach nhom van ban da chuyen
                    case Constants.Document.DetailType.LIST_CV_GROUP_RECEIVER:
                        listCvGroup = thread.getLstCvGroup();
                        break;
                }
            }
            // Neu khong lay duoc thong tin chi tiet
            // -> Tra ve thong bao loi server
            if (result == null) {
                LOGGER.error("getDocumentDetail (Lay chi tiet van ban) - result"
                        + " = null - username: " + cardId + "\ndata: " + data);
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
            result.setListAttachment(listAttachment);
            result.setListComment(listComment);
            result.setListReceiverSameUser(listReceiverSameUser);
            result.setListReceiver(listReceiver);
            result.setListGroup(listGroup);
            result.setListMainSigner(listMainSigner);
            result.setListReviewSigner(listReviewSigner);
            result.setListFlashingSigner(listFlashingSigner);
            result.setLstCvGroup(listCvGroup);
            result.convertData();
            // Lay thong tin van ban dinh kem
            DocumentInStaffDAO documentInStaffDAO = new DocumentInStaffDAO();
            List<EntityDocument> lstDocAttach = documentInStaffDAO.getDocumentAttach(
                    userId1, userId2, documentId);
            if (lstDocAttach != null && lstDocAttach.size() > 0) {
                List<Long> lstDocumentId = new ArrayList<>();
                for (EntityDocument doc : lstDocAttach) {
                    lstDocumentId.add(doc.getDocumentId());
                }
                // 15/02/2017 Hiendv2: Neu la version 3.2.6 mobile tro len thi khong thuc hien 
                //lay danh sach file dinh kem theo van ban moi nua
                if (CommonUtils.isEmpty(isVersionNew) || !"1".equals(isVersionNew)) {
                    FilesAttachmentDAO filesAttachmentDAO = new FilesAttachmentDAO();
                    List<EntityFilesAttachment> listAttachmentFile = filesAttachmentDAO.getListAttachment(0L, lstDocumentId);
                    // Gan danh sach file kem vao tung van ban tuong ung
                    if (!CommonUtils.isEmpty(listAttachmentFile)) {
                        List<EntityFilesAttachment> listAttachmentForDocument;
                        for (EntityDocument doc : lstDocAttach) {
                            listAttachmentForDocument = new ArrayList<>();
                            for (EntityFilesAttachment attachment : listAttachmentFile) {
                                if (attachment.getDocumentId().equals(doc.getDocumentId())) {
                                    listAttachmentForDocument.add(attachment);
                                }
                            }
                            doc.setListAttachment(listAttachmentForDocument);
                        }
                    }
                    result.setLstDocAttachFile(listAttachmentFile);
                }
                result.setLstDocAttach(lstDocAttach);
            }
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error("getDocumentDetail (Lay chi tiet van ban) - Exception "
                    + "- username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Lay danh sach nguoi nhan van ban<b></br>
     *
     * @author thanght6
     * @since May 25, 2016
     * @param request Doi tuong request tu client -> server
     * @param data Gia tri data truyen theo request
     * @param isSecurity Gia tri isSecurity truyen theo request
     * @return
     */
    public String getListReceiver(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);

        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getListReceiver - No session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }

        // Neu user hoac user id tren he thong 1/2 null
        // -> Tra ve thong bao khong co quyen
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        if ((user2 == null || user2.getUserId() == null)) {
            LOGGER.error("getListReceiver - user hoac userId null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Neu user1 null thi gan userId1 bang null
        Long userId1 = user1 == null ? null : user1.getUserId();
        // Neu user2 null thi gan userId2 bang null
        Long userId2 = user2.getUserId();
        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
        log.setUserName(cardId);
        // Giai ma du lieu neu ma hoa
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
                ConstantsFieldParams.SENDER_ID1,
                ConstantsFieldParams.SENDER_ID2,
                ConstantsFieldParams.DOCUMENT_ID,
                ConstantsFieldParams.START_RECORD,
                ConstantsFieldParams.PAGE_SIZE,
                ConstantsFieldParams.SENDER_ID_VOF1,
                ConstantsFieldParams.SENDER_ID_VOF2,
                "receiverDate",
                "isNotDuplicate",
                ConstantsFieldParams.DOCUMENT_IN_STAFF_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);

            // Id nguoi gui he thong 1
            Long senderId1 = null;
            String strSenderId1 = listValue.get(0);
            if (!CommonUtils.isEmpty(strSenderId1)) {
                senderId1 = Long.parseLong(strSenderId1);
            }

            // Id nguoi gui tren he thong 2
            Long senderId2 = null;
            String strSenderId2 = listValue.get(1);
            if (!CommonUtils.isEmpty(strSenderId2)) {
                senderId2 = Long.parseLong(strSenderId2);
            }

            // Id van ban
            Long documentId = null;
            if (CommonUtils.isInteger(listValue.get(2))) {
                documentId = Long.parseLong(listValue.get(2));
            }

            // Vi tri lay ra
            Long startRecord;
//            String strStartRecord = listValue.get(3);           
//            if (!CommonUtils.isEmpty(strStartRecord)) {
//                startRecord = Long.parseLong(strStartRecord);
//            }
            //Thay doi cach lay paging
            try {
                startRecord = Long.parseLong(listValue.get(3));
            } catch (NumberFormatException e) {
                startRecord = null;
            }

            // So luong ban ghi lay ra
            Long pageSize;
//            String strPageSize = listValue.get(4);
//            if (!CommonUtils.isEmpty(strPageSize)) {
//                pageSize = Long.parseLong(strPageSize);
//            }
            try {
                pageSize = Long.parseLong(listValue.get(4));
            } catch (NumberFormatException e) {
                pageSize = null;
            }

            Long idVof1 = null;
            Long idVof2 = null;
            try {
                if (!CommonUtils.isEmpty(listValue.get(5))
                        && FunctionCommon.isNumeric(listValue.get(5))) {
                    idVof1 = Long.parseLong(listValue.get(5));
                }

            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
            try {
                if (!CommonUtils.isEmpty(listValue.get(6))
                        && FunctionCommon.isNumeric(listValue.get(6))) {
                    idVof2 = Long.parseLong(listValue.get(6));
                }
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
            if (idVof1 != null || idVof2 != null) {
                userId1 = idVof1;
                userId2 = idVof2;
            }
            //050517 them ngay nhan van ban
            String receiverDate = listValue.get(7);
            DocumentInStaffDAO documentInStaffDAO = new DocumentInStaffDAO();
            documentInStaffDAO.setStrReceiveDateTmp(receiverDate);
//            List<EntityDocumentInStaff> result = documentInStaffDAO.
//                    getListReceiver(senderId1, senderId2, documentId, startRecord, pageSize);
            //230616 bo nguoi dang nhap ra kho danh sanh
            // Loc danh sach nguoi nhan bi trung
            boolean isNotDuplicate = "1".equals(listValue.get(8));
            
            // [tuantm30 - start] Bo sung them lay thong tin nguoi gui neu ko truyen len
            Long documentInStaffId = null;
            if (CommonUtils.isInteger(listValue.get(9))) {
                documentInStaffId = Long.valueOf(listValue.get(9));
            }
            if (documentInStaffId != null && senderId2 == null) {
                DocumentInStaffDAO dao = new DocumentInStaffDAO();
                EntityDocument result = dao.getDocumentInStaffDetail(documentInStaffId, false);
                if (result != null) {
                    senderId2 = result.getSenderId2();
                    documentId = result.getDocumentId();
                }
            }
            // [tuantm30 - end] Bo sung them lay thong tin nguoi gui neu ko truyen len
            
            List<EntityDocumentInStaff> result;
            if (pageSize == null || pageSize >= 5000) {
                //loc theo nguoi nhan cung don vi len tren
                List<Long> lstVhrOrgId = user2.getLstVhrOrgIsDefault();
                result = documentInStaffDAO.getListReceiverSortByOrg(senderId1,
                        senderId2, documentId, userId2, userId1, lstVhrOrgId, isNotDuplicate);
                //Gan lai danh sach file co comment khi chuyen
                result = documentInStaffDAO.addLstFileCommentToLstReceiver(result);
            } else {
                //lay danh sach cu
                result = documentInStaffDAO.
                        getListReceiver(senderId1, senderId2, documentId, startRecord, pageSize,
                                userId2, userId1, null);
            }
//            System.out.println("-------------------------------------");
//            System.out.println(FunctionCommon.generateJSONBase(result));
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
//            System.out.println("luanvd-getListReceiver-result - " + user2 + ": \n"
//                    + FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, null));
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);

        } catch (Exception ex) {
            LOGGER.error("getListReceiver - userId1: " + userId1
                    + " - userId2: " + userId2, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * <b>Lay danh sach nguoi nhan van ban<b></br>
     *
     * @author thanght6
     * @since May 25, 2016
     *
     * @param request Doi tuong request tu client -> server
     * @param data Gia tri data truyen theo request
     * @param isSecurity Gia tri isSecurity truyen theo request
     *
     * @return
     */
    public String getListGroup(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);

        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getListGroup - No session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }

        // Neu user hoac user id tren ca 2 he thong 1&2 null
        // -> Tra ve thong bao khong co quyen
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        if ((user1 == null || user1.getUserId() == null)
                && (user2 == null || user2.getUserId() == null)) {
            LOGGER.error("getListGroup - user hoac userId tren ca 2 he thong 1&2 null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Id user tren he thong 1
        Long userId1 = user1 == null ? null : user1.getUserId();
        // Id user tren he thong 2
        Long userId2 = user2.getUserId();
        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
        log.setUserName(cardId);
        // Giai ma du lieu neu ma hoa
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
                ConstantsFieldParams.DOCUMENT_ID,
                ConstantsFieldParams.START_RECORD,
                ConstantsFieldParams.PAGE_SIZE,
                ConstantsFieldParams.SENDER_ID_VOF1,
                ConstantsFieldParams.SENDER_ID_VOF2,
                "isGetAll"
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Id van ban
            Long documentId = Long.parseLong(listValue.get(0));

            // Vi tri lay ra
            Long startRecord = null;
            String strStartRecord = listValue.get(1);
            if (!CommonUtils.isEmpty(strStartRecord)) {
                startRecord = Long.parseLong(strStartRecord);
            }

            // So luong ban ghi lay ra
            Long pageSize = null;
            String strPageSize = listValue.get(2);
            if (!CommonUtils.isEmpty(strPageSize)) {
                pageSize = Long.parseLong(strPageSize);
            }
            //Begin Lay theo nguoi gui khac tai khoan dang nhap ::cuongnv
            Long senderId1 = null;
            try {
                senderId1 = Long.parseLong(listValue.get(3));
            } catch (NumberFormatException ex) {
            }
            Long senderId2 = null;
            try {
                senderId2 = Long.parseLong(listValue.get(4));
            } catch (NumberFormatException ex) {
            }
            //End
            //230517 lay tat ca cac don vi nhan van ban
            String isGetAll = listValue.get(5);
            DocumentInGroupDAO documentInGroupDAO = new DocumentInGroupDAO();
            List<EntityDocumentInGroup> result;
            if (isGetAll != null && "1".equals(isGetAll)) {
                //neu la lay tat ca cac van ban
                result = documentInGroupDAO.
                        getListGroupReceiveFromDoc(documentId);
            } else {
                if (senderId1 != null || senderId2 != null) {
                    result = documentInGroupDAO.
                            getListGroup(senderId1, senderId2, documentId, startRecord, pageSize);

                } else {
                    result = documentInGroupDAO.
                            getListGroup(userId1, userId2, documentId, startRecord, pageSize);
                }
            }
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error("getListGroup - userId: " + userId1, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * ham chuyen van ban cho ca nhan
     *
     * @param isSecurity
     * @param strData
     * @param req
     * @return
     */
    public String sendDocumentToStaff(HttpServletRequest req, String strData,
            String isSecurity) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        DocumentDAO documentDao = new DocumentDAO();
        if (dataSessionGR.getCheckSessionOk()) {
            String cardId = "";
            String strDataClient = "";
            try {
                //lay gia tri tu session
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                // Lay ma nhan vien
                cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);
                //Lay prams client gui len de thuc hien yeu cau
                HashMap<String, Object> hmParams = new HashMap<>();
                hmParams.put(ConstantsFieldParams.TEXT_DOC_ID, Long.class);
                hmParams.put(ConstantsFieldParams.comment, String.class);
                hmParams.put(ConstantsFieldParams.PUBLISHER_ID, Long.class);
                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
                //lay du lieu
                Long docId = (Long) ((valueParams.get(ConstantsFieldParams.TEXT_DOC_ID) == null
                        || valueParams.get(ConstantsFieldParams.TEXT_DOC_ID).equals(0L)) ? null : valueParams.get(ConstantsFieldParams.TEXT_DOC_ID));
                String comment = (String) ((valueParams.get(ConstantsFieldParams.comment) != null) ? valueParams.get(ConstantsFieldParams.comment) : "");
                Long publisherId = (Long) ((valueParams.get(ConstantsFieldParams.PUBLISHER_ID) == null
                        || valueParams.get(ConstantsFieldParams.PUBLISHER_ID).equals(0L)) ? null : valueParams.get(ConstantsFieldParams.PUBLISHER_ID));
                //Lay danh nguoi nhan van ban
                List<Vof2_EntityUser> listStaffReceiver = new ArrayList<>();
                JSONArray arrStaffReceiver = FunctionCommon.jsonGetArray("listStaffReceiver", strDataClient);

                if (arrStaffReceiver != null && arrStaffReceiver.length() > 0) {
                    int arrStaffReceiverSize = arrStaffReceiver.length();
                    Long userIdVof2;
                    Long groupIdVof2;
                    Vof2_EntityUser userVof2;
                    for (int i = 0; i < arrStaffReceiverSize; i++) {
                        JSONObject innerObj = (JSONObject) arrStaffReceiver.get(i);
                        userIdVof2 = null;
                        if (innerObj.has("userId") && innerObj.getString("userId") != null) {
                            userIdVof2 = Long.parseLong(innerObj.getString("userId").trim());
                        }
                        groupIdVof2 = null;
                        if (innerObj.has("sysOrgId") && innerObj.getString("sysOrgId") != null) {
                            groupIdVof2 = Long.parseLong(innerObj.getString("sysOrgId").trim());
                        }
                        if (userIdVof2 != null && groupIdVof2 != null) {
                            userVof2 = new Vof2_EntityUser();
                            userVof2.setUserId(userIdVof2);
                            userVof2.setSysOrgId(groupIdVof2);
                            listStaffReceiver.add(userVof2);
                        }
                    }
                }
                if (docId != null && listStaffReceiver.size() > 0) {
                    int result = documentDao.sendDocumentToStaff(docId, listStaffReceiver,
                            comment, publisherId, dataSessionGR.getItemEntityUser(),
                            dataSessionGR.getVof2_ItemEntityUser(), null, null, null);
                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                            result, strAesKeyDecode);
                } else {
                    LOGGER.error("sendDocumentToStaff (Chuyen van ban cho ca nhan)"
                            + " - docId = null hoac listStaffReceiver null/rong"
                            + " - username: " + cardId + "\ndata: " + strDataClient);
                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA,
                            null, null);
                }
            } catch (Exception ex) {
                LOGGER.error("sendDocumentToStaff (Chuyen van ban cho ca nhan) -"
                        + " Exception - username: " + cardId + "\ndata: " + strDataClient, ex);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
        } else {
            LOGGER.error("sendDocumentToStaff (Chuyen van ban cho ca nhan) - Session timeout!");
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(),
                    null, null);
        }
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return strResult;
    }

    /**
     * <b>Chuyen van ban cho don vi</b><br>
     *
     * @param isSecurity
     * @param strData
     * @param req
     * @return
     */
    public String sendDocumentToGroup(HttpServletRequest req, String strData,
            String isSecurity) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        DocumentDAO documentDao = new DocumentDAO();
        // Session hop le
        if (dataSessionGR.getCheckSessionOk()) {
            String cardId = "";
            String strDataClient = "";
            try {
                // Lay key AES de giai ma du lieu
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                // Lay ma nhan vien
                cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);

                //Lay prams client gui len de thuc hien yeu cau
                HashMap<String, Object> hmParams = new HashMap<>();
                hmParams.put(ConstantsFieldParams.TEXT_DOC_ID, Long.class);
                hmParams.put(ConstantsFieldParams.comment, String.class);
                hmParams.put(ConstantsFieldParams.PUBLISHER_ID, Long.class);
                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
                //lay du lieu
                Long docId = (Long) ((valueParams.get(ConstantsFieldParams.TEXT_DOC_ID) == null
                        || valueParams.get(ConstantsFieldParams.TEXT_DOC_ID).equals(0L)) ? null : valueParams.get(ConstantsFieldParams.TEXT_DOC_ID));
                String comment = (String) ((valueParams.get(ConstantsFieldParams.comment) != null) ? valueParams.get(ConstantsFieldParams.comment) : "");
                Long publisherId = (Long) ((valueParams.get(ConstantsFieldParams.PUBLISHER_ID) == null
                        || valueParams.get(ConstantsFieldParams.PUBLISHER_ID).equals(0L)) ? null : valueParams.get(ConstantsFieldParams.PUBLISHER_ID));
                //Lay danh nguoi nhan van ban
                List<GroupMapping> lstGroup = new ArrayList<>();
                JSONArray arrGroup = FunctionCommon.jsonGetArray("lstGroup", strDataClient);

                if (arrGroup != null && arrGroup.length() > 0) {
                    int arrGroupSize = arrGroup.length();
                    Long groupIdVof2;
                    GroupMapping groupMap;
                    for (int i = 0; i < arrGroupSize; i++) {
                        JSONObject innerObj = (JSONObject) arrGroup.get(i);
                        groupIdVof2 = null;
                        if (innerObj.has("sysOrgId") && innerObj.getString("sysOrgId") != null) {
                            groupIdVof2 = Long.parseLong(innerObj.getString("sysOrgId").trim());
                        }
                        if (groupIdVof2 != null) {
                            groupMap = new GroupMapping();
                            groupMap.setGroupVof2(groupIdVof2);
                            lstGroup.add(groupMap);
                        }
                    }
                }
                if (docId != null && lstGroup.size() > 0) {
                    int result = documentDao.sendDocumentToGroup(docId, lstGroup,
                            comment, publisherId, dataSessionGR.getItemEntityUser(),
                            dataSessionGR.getVof2_ItemEntityUser(), null, null, null);
                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                } else {
                    LOGGER.error("sendDocumentToGroup (Chuyen van ban cho don vi)"
                            + " - docId = null hoac lstGroup null/rong - username: "
                            + cardId + "\ndata: " + strDataClient);
                    strResult = FunctionCommon.generateResponseJSON(
                            ErrorCode.ERR_NODATA, null, null);
                }
            } catch (Exception ex) {
                LOGGER.error("sendDocumentToGroup (Chuyen van ban cho don vi) -"
                        + " Exception - username: " + cardId + "\ndata: " + strDataClient, ex);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            LOGGER.error("sendDocumentToGroup (Chuyen van ban cho don vi) - Session timeout!");
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);

        }
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return strResult;
    }

    /**
     * <b>Chuyen cho nhom ca nhan</b><br>
     *
     * @author HanhNQ21
     * @param req
     * @param strData
     * @param isSecurity
     * @return
     */
    public String sendDocumentToListPersonalGroup(HttpServletRequest req,
            String strData, String isSecurity) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            String cardId = "";
            String strDataClient = "";
            try {
                //lay gia tri tu session
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                // Lay ma nhan vien
                cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);
                //Lay prams client gui len de thuc hien yeu cau
                HashMap<String, Object> hmParams = new HashMap<>();
                hmParams.put(ConstantsFieldParams.TEXT_DOC_ID, Long.class);
                hmParams.put(ConstantsFieldParams.comment, String.class);
                hmParams.put(ConstantsFieldParams.PUBLISHER_ID, Long.class);
                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
                //lay du lieu
                Long docId = (Long) ((valueParams.get(ConstantsFieldParams.TEXT_DOC_ID) == null
                        || valueParams.get(ConstantsFieldParams.TEXT_DOC_ID).equals(0L)) ? null : valueParams.get(ConstantsFieldParams.TEXT_DOC_ID));
                String comment = (String) ((valueParams.get(ConstantsFieldParams.comment) != null) ? valueParams.get(ConstantsFieldParams.comment) : "");
                Long publisherId = (Long) ((valueParams.get(ConstantsFieldParams.PUBLISHER_ID) == null
                        || valueParams.get(ConstantsFieldParams.PUBLISHER_ID).equals(0L)) ? null : valueParams.get(ConstantsFieldParams.PUBLISHER_ID));
                //Lay danh nguoi nhan van ban
                List<Long> lstGroupId = new ArrayList<>();
                JSONArray arrGroup = FunctionCommon.jsonGetArray("lstGroupId", strDataClient);

                if (arrGroup != null && arrGroup.length() > 0) {
                    int arrGroupSize = arrGroup.length();
                    Long groupId;;
                    for (int i = 0; i < arrGroupSize; i++) {
                        JSONObject innerObj = (JSONObject) arrGroup.get(i);
                        groupId = null;
                        if (innerObj.has("groupId") && innerObj.getString("groupId") != null) {
                            groupId = Long.parseLong(innerObj.getString("groupId").trim());
                        }
                        if (groupId != null) {
                            lstGroupId.add(groupId);
                        }
                    }
                }
                if (docId != null && lstGroupId.size() > 0) {
                    DocumentDAO documentDao = new DocumentDAO();
                    int result = documentDao.sendDocumentToListPersonalGroup(docId,
                            null, comment, publisherId, dataSessionGR.getItemEntityUser(),
                            dataSessionGR.getVof2_ItemEntityUser(), null, null, null, null);
                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                } else {
                    LOGGER.error("sendDocumentToListPersonalGroup (Chuyen van ban"
                            + " cho nhom ca nhan) - docId null hoac lstGroupId "
                            + "null/rong - username: " + cardId + "\ndata: " + strDataClient);
                    strResult = FunctionCommon.generateResponseJSON(
                            ErrorCode.ERR_NODATA, null, null);
                }
            } catch (Exception ex) {
                LOGGER.error("sendDocumentToListPersonalGroup (Chuyen van ban"
                        + " cho nhom ca nhan) - Exception - username: " + cardId
                        + "\ndata: " + strDataClient, ex);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
        } // Session timeout
        else {
            LOGGER.error("sendDocumentToListPersonalGroup (Chuyen van ban cho"
                    + " nhom ca nhan) - Session timeout!");
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(),
                    null, null);
        }
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return strResult;
    }

    /**
     * ham cap nhat trang thai xu ly van ban
     *
     * @param req
     * @param strData
     * @param isSecurity
     * @return
     */
    public String updateDocumentProcessing(HttpServletRequest req, String strData,
            String isSecurity) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        DocumentDAO documentDao = new DocumentDAO();
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //lay gia tri tu session
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);
                //Lay prams client gui len de thuc hien yeu cau
                HashMap<String, Object> hmParams = new HashMap<>();
                hmParams.put(ConstantsFieldParams.TEXT_DOC_ID, Long.class);
                hmParams.put(ConstantsFieldParams.STAFF_ID, Long.class);
                hmParams.put("isProcessing", Long.class);
                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
                //lay du lieu
                Long documentId = (Long) ((valueParams.get(ConstantsFieldParams.TEXT_DOC_ID) == null
                        || valueParams.get(ConstantsFieldParams.TEXT_DOC_ID).equals(0L)) ? null : valueParams.get(ConstantsFieldParams.TEXT_DOC_ID));
                /**
                 * Long staffId = (Long)
                 * ((valueParams.get(ConstantsFieldParams.STAFF_ID) == null ||
                 * valueParams.get(ConstantsFieldParams.STAFF_ID).equals(0L)) ?
                 * null : valueParams.get(ConstantsFieldParams.STAFF_ID)); Long
                 * staffIdVof2 = (Long) ((valueParams.get("staffIdVof2") == null
                 * || valueParams.get("staffIdVof2").equals(0L)) ? null :
                 * valueParams.get("staffIdVof2"));
                 */
                Long isProcessing = (Long) ((valueParams.get("isProcessing") == null)
                        ? null : valueParams.get("isProcessing"));
                //Lay danh nguoi nhan van ban
//                System.out.println("updateDocumentProcessing: " + strDataClient);
                Long staffIdVof2 = null;
                if (dataSessionGR.getVof2_ItemEntityUser() != null) {
                    staffIdVof2 = dataSessionGR.getVof2_ItemEntityUser().getUserId();
                }
                Long staffId = 0L;
                if (dataSessionGR.getItemEntityUser() != null) {
                    staffId = dataSessionGR.getItemEntityUser().getUserId();
                }
                if (documentId != null) {
                    if ((staffIdVof2 != null || staffId != null)) {
                        int result = documentDao.updateDocumentProcessing(documentId,
                                staffId, staffIdVof2, isProcessing);
                        // Ghi log ket thuc chuc nang
                        LogUtils.logFunctionalEnd(log);
                        strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                        //datnv5: danh index cho kho tim kiem sau khi chuyen van ban
                        IndexDocumentByType indexDocumentByType = new IndexDocumentByType(documentId);
                        ThreadPoolCommon.putRunnable(indexDocumentByType);
                    } else {
//                        System.out.println("updateDocumentProcessing: thong tin user null");
                        return FunctionCommon.generateResponseJSON(
                                ErrorCode.ERR_NODATA, null, null);
                    }
                } else {
                    return FunctionCommon.generateResponseJSON(
                            ErrorCode.ERR_NODATA, null, null);
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }
        return strResult;
    }

    /**
     * ham cap nhat trang thai thu hoi van ban
     *
     * @param req
     * @param strData
     * @param isSecurity
     * @return
     */
    public String updateStatusDocumentInStaff(HttpServletRequest req, String strData,
            String isSecurity) {
//        LOGGER.info("---DocumentController.updateStatusDocumentInStaff: begin");

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        DocumentDAO documentDao = new DocumentDAO();
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //lay gia tri tu session
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
                hmParams.put(ConstantsFieldParams.TEXT_DOC_ID, Long.class);
                hmParams.put(ConstantsFieldParams.STAFF_ID, Long.class);
                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
                //lay du lieu
                Long documentId = (Long) ((valueParams.get(ConstantsFieldParams.TEXT_DOC_ID) == null
                        || valueParams.get(ConstantsFieldParams.TEXT_DOC_ID).equals(0L)) ? null : valueParams.get(ConstantsFieldParams.TEXT_DOC_ID));

                Long staffIdVof2 = null;
                if (dataSessionGR.getVof2_ItemEntityUser() != null) {
                    staffIdVof2 = dataSessionGR.getVof2_ItemEntityUser().getUserId();
                }
                Long staffId = null;
                if (dataSessionGR.getItemEntityUser() != null) {
                    staffId = dataSessionGR.getItemEntityUser().getUserId();
                }

                //List<Vof2_EntityUser> listStaffReceiver = new ArrayList<Vof2_EntityUser>();
                JSONArray arrStaffReceiver = FunctionCommon.jsonGetArray("listReceiverIdVof2", strDataClient);
//                LOGGER.info("---listReceiverIdVof2----: " + arrStaffReceiver);
                List<Long> listReceiverId1 = new ArrayList<>();
                List<Long> listReceiverId2 = new ArrayList<>();
                if (arrStaffReceiver != null && arrStaffReceiver.length() > 0) {
                    int arrStaffReceiverSize = arrStaffReceiver.length();
                    Long userIdVof1 = null;
                    Long userIdVof2 = null;
                    for (int i = 0; i < arrStaffReceiverSize; i++) {
                        JSONObject innerObj = (JSONObject) arrStaffReceiver.get(i);
                        // Lay id người nhận ở vof1,vof2
                        if (innerObj.has("receiverId1")) {
                            try {
                                userIdVof1 = Long.parseLong(innerObj.getString("receiverId1").trim());
                            } catch (NumberFormatException e) {
                                userIdVof1 = null;
                            }
                        }
                        if (innerObj.has("receiverId2")) {
                            try {
                                userIdVof2 = Long.parseLong(innerObj.getString("receiverId2").trim());
                            } catch (NumberFormatException e) {
                                userIdVof2 = null;
                            }
                        }
                        listReceiverId1.add(userIdVof1);
                        listReceiverId2.add(userIdVof2);
                    }
                }

                if (documentId != null) {
                    if (listReceiverId2.size() > 0) {
                        int result = documentDao.updateStatusDocumentInStaff(documentId,
                                staffId, staffIdVof2, listReceiverId1, listReceiverId2);
                        // Ghi log ket thuc chuc nang
                        LogUtils.logFunctionalEnd(log);
                        strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                    } else {
                        LOGGER.info("updateStatusDocumentInStaff: thong tin user null");
                        return FunctionCommon.generateResponseJSON(
                                ErrorCode.ERR_NODATA, null, null);
                    }
                } else {
                    return FunctionCommon.generateResponseJSON(
                            ErrorCode.ERR_NODATA, null, null);
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }
//        LOGGER.info("---DocumentController.updateStatusDocumentInStaff: result -" + strResult);
        return strResult;
    }

    /**
     * Hiendv2 ham lay danh sach nhom van ban da gui
     *
     * @param req
     * @param strData
     * @param isSecurity
     * @return
     */
    public String getListReceivedCvGroup(HttpServletRequest req, String strData,
            String isSecurity) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        DocumentDAO documentDao = new DocumentDAO();
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //lay gia tri tu session
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
                hmParams.put(ConstantsFieldParams.SENDER_ID_VOF1, Long.class);
                hmParams.put(ConstantsFieldParams.SENDER_ID_VOF2, Long.class);
                hmParams.put(ConstantsFieldParams.DOCUMENT_ID, Long.class);

                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
                //lay du lieu
                Long senderIdVof1 = (Long) ((valueParams.get(ConstantsFieldParams.SENDER_ID_VOF1) == null
                        || valueParams.get(ConstantsFieldParams.SENDER_ID_VOF1).equals(0L)) ? null : valueParams.get(ConstantsFieldParams.SENDER_ID_VOF1));
                Long senderIdVof2 = (Long) ((valueParams.get(ConstantsFieldParams.SENDER_ID_VOF1) == null
                        || valueParams.get(ConstantsFieldParams.SENDER_ID_VOF1).equals(0L)) ? null : valueParams.get(ConstantsFieldParams.SENDER_ID_VOF1));

                Long documentId = (Long) ((valueParams.get(ConstantsFieldParams.DOCUMENT_ID) == null
                        || valueParams.get(ConstantsFieldParams.DOCUMENT_ID).equals(0L)) ? null : valueParams.get(ConstantsFieldParams.DOCUMENT_ID));
//                System.out.println("getListReceivedCvGroup: " + strDataClient);
                if (documentId != null && (senderIdVof1 != null || senderIdVof2 != null)) {
                    List<EntityCvGroup> lstRecever = documentDao.getListReceivedCvGroup(senderIdVof1,
                            senderIdVof2, documentId);
                    // Ghi log ket thuc chuc nang
                    LogUtils.logFunctionalEnd(log);
                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, lstRecever, strAesKeyDecode);
                } else {
                    return FunctionCommon.generateResponseJSON(
                            ErrorCode.ERR_NODATA, null, null);
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }
        return strResult;
    }

    /**
     * Hiendv2 ham lay danh sach ca nhan trong nhom
     *
     * @param req
     * @param strData
     * @param isSecurity
     * @return
     */
    public String getListReceivedStaffInPersonalGroup(HttpServletRequest req, String strData,
            String isSecurity) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        DocumentDAO documentDao = new DocumentDAO();
        if (dataSessionGR.getCheckSessionOk()) {
            try {

                // Neu user hoac user id tren he thong 1/2 null
                // -> Tra ve thong bao khong co quyen
                EntityUser user1 = dataSessionGR.getItemEntityUser();
                Vof2_EntityUser user2 = dataSessionGR.getVof2_ItemEntityUser();
                if ((user1 == null || user1.getUserId() == null)
                        && (user2 == null || user2.getUserId() == null)) {
                    LOGGER.error("getListReceiver - user hoac userId null");
                    return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
                }
                Long senderIdVof1 = user1 == null ? null : user1.getUserId();
                Long senderIdVof2 = user2 == null ? null : user2.getUserId();

                //lay gia tri tu session
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
                hmParams.put(ConstantsFieldParams.CV_GROUP_ID, Long.class);
                hmParams.put(ConstantsFieldParams.DOCUMENT_ID, Long.class);
                hmParams.put(ConstantsFieldParams.SENDER_ID_VOF1, Long.class);
                hmParams.put(ConstantsFieldParams.SENDER_ID_VOF2, Long.class);

                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
                //lay du lieu
                Long cvGroupId = (Long) ((valueParams.get(ConstantsFieldParams.CV_GROUP_ID) == null
                        || valueParams.get(ConstantsFieldParams.CV_GROUP_ID).equals(0L)) ? null : valueParams.get(ConstantsFieldParams.CV_GROUP_ID));//Lay danh nguoi nhan van ban
                Long documentId = (Long) ((valueParams.get(ConstantsFieldParams.DOCUMENT_ID) == null
                        || valueParams.get(ConstantsFieldParams.DOCUMENT_ID).equals(0L)) ? null : valueParams.get(ConstantsFieldParams.DOCUMENT_ID));
                Long idVof1 = (Long) ((valueParams.get(ConstantsFieldParams.SENDER_ID_VOF1) == null
                        || valueParams.get(ConstantsFieldParams.SENDER_ID_VOF1).equals(0L)) ? null : valueParams.get(ConstantsFieldParams.SENDER_ID_VOF1));
                Long idVof2 = (Long) ((valueParams.get(ConstantsFieldParams.SENDER_ID_VOF2) == null
                        || valueParams.get(ConstantsFieldParams.SENDER_ID_VOF2).equals(0L)) ? null : valueParams.get(ConstantsFieldParams.SENDER_ID_VOF2));

                if (idVof1 != null || idVof2 != null) {
                    senderIdVof1 = idVof1;
                    senderIdVof2 = idVof2;
                }
//                System.out.println("getListReceivedStaffInPersonalGroup: " + strDataClient);
                if (cvGroupId != null && (senderIdVof1 != null || senderIdVof2 != null)) {
                    List<EntityStaff> lstRecever = documentDao.getListReceivedStaffInPersonalGroup(
                            senderIdVof1, senderIdVof2, cvGroupId, documentId);
                    // Ghi log ket thuc chuc nang
                    LogUtils.logFunctionalEnd(log);
                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, lstRecever, strAesKeyDecode);
                } else {
                    return FunctionCommon.generateResponseJSON(
                            ErrorCode.ERR_NODATA, null, null);
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }
        return strResult;
    }

    /**
     * Hiendv2 ham lay danh sach nhom ca nhan da chuyen
     *
     * @param req
     * @param strData
     * @param isSecurity
     * @return
     */
    public String getListReceivedPersonalGroup(HttpServletRequest req, String strData,
            String isSecurity) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        DocumentDAO documentDao = new DocumentDAO();
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                // Neu user hoac user id tren he thong 1/2 null
                // -> Tra ve thong bao khong co quyen
                EntityUser user1 = dataSessionGR.getItemEntityUser();
                Vof2_EntityUser user2 = dataSessionGR.getVof2_ItemEntityUser();
                if ((user1 == null || user1.getUserId() == null)
                        && (user2 == null || user2.getUserId() == null)) {
                    LOGGER.error("getListReceiver - user hoac userId null");
                    return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
                }
                Long senderIdVof1 = user1 == null ? null : user1.getUserId();
                Long senderIdVof2 = user2 == null ? null : user2.getUserId();
                //lay gia tri tu session
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
                hmParams.put(ConstantsFieldParams.DOCUMENT_ID, Long.class);

                //Begin Lay danh sach nhom khong theo nguoi dang nhap :: cuongnv
                hmParams.put(ConstantsFieldParams.SENDER_ID_VOF1, Long.class);
                hmParams.put(ConstantsFieldParams.SENDER_ID_VOF2, Long.class);
                //230517 lay tat ca cac don vi nhan van ban
                hmParams.put("isGetAll", String.class);
                //End

                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
                Long documentId = (Long) ((valueParams.get(ConstantsFieldParams.DOCUMENT_ID) == null
                        || valueParams.get(ConstantsFieldParams.DOCUMENT_ID).equals(0L)) ? null : valueParams.get(ConstantsFieldParams.DOCUMENT_ID));

                //Begin Lay danh sach nhom khong theo nguoi dang nhap :: cuongnv
                Long sendId1 = (Long) ((valueParams.get(ConstantsFieldParams.SENDER_ID_VOF1) == null
                        || valueParams.get(ConstantsFieldParams.SENDER_ID_VOF1).equals(0L)) ? null : valueParams.get(ConstantsFieldParams.SENDER_ID_VOF1));
                Long sendId2 = (Long) ((valueParams.get(ConstantsFieldParams.SENDER_ID_VOF2) == null
                        || valueParams.get(ConstantsFieldParams.SENDER_ID_VOF2).equals(0L)) ? null : valueParams.get(ConstantsFieldParams.SENDER_ID_VOF2));
                //230517 lay tat ca cac don vi nhan van ban
                String isGetAll = null;
                if (valueParams.get("isGetAll") != null) {
                    isGetAll = (String) valueParams.get("isGetAll");
                }
                if (isGetAll != null && "1".equals(isGetAll)) {
                    //lay tat ca cac nhom nhan van ban
                    List<EntityCvGroup> lstRecever = documentDao.getListCvGroupReceiveFromDoc(documentId);
                    // Ghi log ket thuc chuc nang
                    LogUtils.logFunctionalEnd(log);
                    return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, lstRecever, strAesKeyDecode);
                } else {
                    if ((sendId1 != null || sendId2 != null)) {
                        List<EntityCvGroup> lstRecever = documentDao.getListReceivedCvGroup(sendId1, sendId2, documentId);
                        // Ghi log ket thuc chuc nang
                        LogUtils.logFunctionalEnd(log);
                        return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, lstRecever, strAesKeyDecode);
                    }
                    //End

                    if ((senderIdVof1 != null || senderIdVof2 != null)) {
                        List<EntityCvGroup> lstRecever = documentDao.getListReceivedCvGroup(
                                senderIdVof1, senderIdVof2, documentId);
                        // Ghi log ket thuc chuc nang
                        LogUtils.logFunctionalEnd(log);
                        strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, lstRecever, strAesKeyDecode);
                    } else {
                        return FunctionCommon.generateResponseJSON(
                                ErrorCode.ERR_NODATA, null, null);
                    }
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }
        return strResult;
    }

    /**
     * Hiendv2 ham lay danh sach ca nhan theo don vi da chuyen
     *
     * @param req
     * @param strData
     * @param isSecurity
     * @return
     */
    public String getListReceivedStaffInDepartment(HttpServletRequest req, String strData,
            String isSecurity) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        DocumentDAO documentDao = new DocumentDAO();
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                // Neu user hoac user id tren he thong 1/2 null
                // -> Tra ve thong bao khong co quyen
                EntityUser user1 = dataSessionGR.getItemEntityUser();
                Vof2_EntityUser user2 = dataSessionGR.getVof2_ItemEntityUser();
                if ((user1 == null || user1.getUserId() == null)
                        && (user2 == null || user2.getUserId() == null)) {
                    LOGGER.error("getListReceiver - user hoac userId null");
                    return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
                }
                Long senderIdVof1 = user1 == null ? null : user1.getUserId();
                Long senderIdVof2 = user2 == null ? null : user2.getUserId();
                //lay gia tri tu session
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
                hmParams.put(ConstantsFieldParams.DOCUMENT_ID, Long.class);
                hmParams.put(ConstantsFieldParams.RECEIVER_GROUP_VOF1, Long.class);
                hmParams.put(ConstantsFieldParams.RECEIVER_GROUP_VOF2, Long.class);

                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
                Long documentId = (Long) ((valueParams.get(ConstantsFieldParams.DOCUMENT_ID) == null
                        || valueParams.get(ConstantsFieldParams.DOCUMENT_ID).equals(0L)) ? null : valueParams.get(ConstantsFieldParams.DOCUMENT_ID));
                Long receiverGroupIdVof1 = (Long) ((valueParams.get(ConstantsFieldParams.RECEIVER_GROUP_VOF1) == null
                        || valueParams.get(ConstantsFieldParams.RECEIVER_GROUP_VOF1).equals(0L)) ? null : valueParams.get(ConstantsFieldParams.RECEIVER_GROUP_VOF1));
                Long receiverGroupIdVof2 = (Long) ((valueParams.get(ConstantsFieldParams.RECEIVER_GROUP_VOF2) == null
                        || valueParams.get(ConstantsFieldParams.RECEIVER_GROUP_VOF2).equals(0L)) ? null : valueParams.get(ConstantsFieldParams.RECEIVER_GROUP_VOF2));
//                System.out.println("Du lieu truyen khi lay danh sach ca nhan chuyen vb theo don vi: " + strDataClient);
                if ((senderIdVof1 != null || senderIdVof2 != null)) {
                    List<EntityStaff> lstRecever = documentDao.getListReceivedStaffInDepartment(senderIdVof1,
                            senderIdVof2, receiverGroupIdVof1, receiverGroupIdVof2, documentId);
                    // Ghi log ket thuc chuc nang
                    LogUtils.logFunctionalEnd(log);
                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, lstRecever, strAesKeyDecode);
                } else {
                    return FunctionCommon.generateResponseJSON(
                            ErrorCode.ERR_NODATA, null, null);
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }
        return strResult;
    }

    /**
     * Them moi pham vi cong bo
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String addDocumentScope(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);

        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("addDocumentScope - No session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }

        // Neu user hoac user id tren he thong 1&2 null
        // -> Tra ve thong bao khong co quyen
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        if ((user1 == null || user1.getUserId() == null)
                && (user2 == null || user2.getUserId() == null)) {
            LOGGER.error("addDocumentScope - user hoac userId tren he thong 1&2 null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Neu khong co thong tin tren he thong 1
        // -> Gan userId1 = 0
        Long userId1 = (user1 == null || user1.getUserId() == null) ? 0L : user1.getUserId();

        // Neu khong co thong tin tren he thong 2
        // -> Gan userId2 = 0
        Long userId2 = (user2 == null || user2.getUserId() == null) ? 0L : user2.getUserId();
        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
        log.setUserName(cardId);
        // Giai ma du lieu neu ma hoa
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
                ConstantsFieldParams.SCOPE_NAME,
                ConstantsFieldParams.SCOPE_PARENT,
                ConstantsFieldParams.SCOPE_ORG,
                ConstantsFieldParams.SCOPE_ID,
                ConstantsFieldParams.IS_UPDATE,
                ConstantsFieldParams.DOC_SCOPE_TYPE //Them loai cho don vi pham vi : 1 Toan bo 2 : Lien ke
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String scopeName = listValue.get(0);
            Long scopeParent = null;
            String strScopeParent = listValue.get(1);
            if (!"".equals(strScopeParent)) {
                scopeParent = Long.parseLong(strScopeParent);
            }
            Long scopeOrg = null;
            String strScopeOrg = listValue.get(2);
            if (!"".equals(strScopeOrg)) {
                scopeOrg = Long.parseLong(strScopeOrg);
            }
            Long scopeId = null;
            String strScopeId = listValue.get(3);
            if (!"".equals(strScopeId)) {
                scopeId = Long.parseLong(strScopeId);
            }
            String isUpdate = listValue.get(4);
            //Lay loai pham vi
            Integer type;
            try {
                type = listValue.get(5) == null ? 1 : Integer.parseInt(listValue.get(5));
            } catch (NumberFormatException ex) {
                type = 1;
            }
            List<Long> vhrOrgIds = new ArrayList<>();
            Map<Long, String> vhrOrgPath = new HashMap<>();
            JSONArray arrVhrOrgIds = FunctionCommon.jsonGetArray(ConstantsFieldParams.SCOPE_HAVE_ORGS, data);
            if (arrVhrOrgIds != null && arrVhrOrgIds.length() > 0) {
                for (int i = 0; i < arrVhrOrgIds.length(); i++) {
                    JSONObject innerObj = (JSONObject) arrVhrOrgIds.get(i);
                    Long orgId = Long.parseLong(innerObj.getString(ConstantsFieldParams.ORG_ID));
                    String path = innerObj.getString(ConstantsFieldParams.PATH_ORG);

                    vhrOrgIds.add(orgId);
                    vhrOrgPath.put(orgId, path);
                }
            }

            DocumentScopeDAO docScopeDAO = new DocumentScopeDAO();
            int result = docScopeDAO.addDocumentScope(scopeName, scopeParent, userId2,
                    scopeOrg, vhrOrgIds, vhrOrgPath, isUpdate, scopeId, type);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error("addDocumentScope - userId1: " + userId1
                    + " - userId2: " + userId2
                    + " - Exception: " + ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * Tim kiem pham vi cong bo
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String searchDocumentScope(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);

        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("searchDocumentScope - No session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }

        // Neu user hoac user id tren he thong 1&2 null
        // -> Tra ve thong bao khong co quyen
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        if ((user1 == null || user1.getUserId() == null)
                && (user2 == null || user2.getUserId() == null)) {
            LOGGER.error("searchDocumentScope - user hoac userId tren he thong 1&2 null");
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
                ConstantsFieldParams.SCOPE_NAME,
                ConstantsFieldParams.SCOPE_PARENT,
                ConstantsFieldParams.IS_ACTIVE,
                ConstantsFieldParams.IS_COUNT,
                ConstantsFieldParams.START_RECORD,
                ConstantsFieldParams.PAGE_SIZE,
                ConstantsFieldParams.DOC_SCOPE_TYPE, // Loai pham vi don vi
                ConstantsFieldParams.TEXT_ID // Id van ban ban hanh
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String scopeName = listValue.get(0);

            Long scopeParent = null;
            String strScopeParent = listValue.get(1);
            if (!CommonUtils.isEmpty(strScopeParent)) {
                scopeParent = Long.parseLong(strScopeParent);
            }

            String isActive = listValue.get(2);
            String isCount = listValue.get(3);

            Long startRecord = null;
            String strStartRecord = listValue.get(4);
            if (!CommonUtils.isEmpty(strStartRecord)) {
                startRecord = Long.parseLong(strStartRecord);
            }

            Long pageSize = null;
            String strPageSize = listValue.get(5);
            //Lay  loai pham vi don vi
            Integer type;
            try {

                type = listValue.get(6) == null ? 0 : Integer.parseInt(listValue.get(6));
            } catch (Exception ex) {
                LOGGER.info(ex);
                type = 0;
            }
            if (!CommonUtils.isEmpty(strPageSize)) {
                pageSize = Long.parseLong(strPageSize);
            }
            Long textId = null;
            try {
                textId = Long.parseLong(listValue.get(7));
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }

            List<Long> vhrOrgIds = new ArrayList<>();
            JSONArray arrVhrOrgIds = FunctionCommon.jsonGetArray(ConstantsFieldParams.LIST_ORG_ID, data);
            if (arrVhrOrgIds != null && arrVhrOrgIds.length() > 0) {
                for (int i = 0; i < arrVhrOrgIds.length(); i++) {
                    JSONObject innerObj = (JSONObject) arrVhrOrgIds.get(i);
                    Long orgId = Long.parseLong(innerObj.getString(ConstantsFieldParams.ORG_ID));

                    vhrOrgIds.add(orgId);
                }
            }

            DocumentScopeDAO docScopeDAO = new DocumentScopeDAO();
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            Object result = docScopeDAO.searchDocumentScope(userId2, scopeName,
                    scopeParent, vhrOrgIds, isActive, isCount, startRecord,
                    pageSize, type,textId);

            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error("searchDocumentScope - userId1: " + userId1
                    + " - userId2: " + userId2, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    public String deleteDocumentScope(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
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
                ConstantsFieldParams.SCOPE_ID,
                ConstantsFieldParams.IS_ACTIVE
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long scopeId = null;
            String strScopeId = listValue.get(0);
            if (!"".equals(strScopeId)) {
                scopeId = Long.parseLong(strScopeId);
            }
            String isActive = listValue.get(1);

            DocumentScopeDAO docScopeDAO = new DocumentScopeDAO();
            Object result = docScopeDAO.deleteDocumentScope(scopeId, isActive);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error("deleteDocumentScope"
                    + " - Exception: " + ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    public String findDocScopeREFByTextId(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
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
                ConstantsFieldParams.TEXT_ID,
                ConstantsFieldParams.DOCUMENT_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long textId = null;
            String strTextId = listValue.get(0);
            if (!"".equals(strTextId)) {
                textId = Long.parseLong(strTextId);
            }

            Long documentId = null;
            String strDocumentId = listValue.get(1);
            if (!"".equals(strDocumentId)) {
                documentId = Long.parseLong(strDocumentId);
            }

            DocumentScopeDAO docScopeDAO = new DocumentScopeDAO();
            Object result = docScopeDAO.findDocScopeREFByTextId(textId, documentId);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error("findDocScopeREFByTextId"
                    + " - Exception: " + ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    public String getDocScopeLibrary(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);

        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        if ((user1 == null || user1.getUserId() == null)
                && (user2 == null || user2.getUserId() == null)) {
            LOGGER.error("getDocScopeLibrary - user hoac userId tren he thong 1&2 null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Neu khong co thong tin tren he thong 1
        // -> Gan userId1 = 0
//        Long userId1 = (user1 == null || user1.getUserId() == null) ? 0L : user1.getUserId();

        // Neu khong co thong tin tren he thong 2
        // -> Gan userId2 = 0
        Long userId2 = (user2 == null || user2.getUserId() == null) ? 0L : user2.getUserId();
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
                ConstantsFieldParams.SCOPE_NAME,
                ConstantsFieldParams.IS_COUNT,
                ConstantsFieldParams.START_RECORD,
                ConstantsFieldParams.PAGE_SIZE
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String scopeName = listValue.get(0);

            String isCount = listValue.get(1);

            Long startRecord = null;
            String strStartRecord = listValue.get(2);
            if (!CommonUtils.isEmpty(strStartRecord)) {
                startRecord = Long.parseLong(strStartRecord);
            }

            Long pageSize = null;
            String strPageSize = listValue.get(3);
            if (!CommonUtils.isEmpty(strPageSize)) {
                pageSize = Long.parseLong(strPageSize);
            }

            List<Long> vhrOrgIds = new ArrayList<>();
            JSONArray arrVhrOrgIds = FunctionCommon.jsonGetArray(ConstantsFieldParams.LIST_ORG_ID, data);
            if (arrVhrOrgIds != null && arrVhrOrgIds.length() > 0) {
                for (int i = 0; i < arrVhrOrgIds.length(); i++) {
                    JSONObject innerObj = (JSONObject) arrVhrOrgIds.get(i);
                    Long orgId = Long.parseLong(innerObj.getString(ConstantsFieldParams.ORG_ID));

                    vhrOrgIds.add(orgId);
                }
            }

            DocumentScopeDAO docScopeDAO = new DocumentScopeDAO();
            Object result = docScopeDAO.getDocScopeLibrary(userId2, scopeName, isCount, startRecord, pageSize, vhrOrgIds);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            LOGGER.error("getDocScopeLibrary"
                    + " - Exception: " + ex.getMessage());
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * Kiem tra 1 van ban co phai la ban hanh lai hay khong?
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String checkIsPublished(HttpServletRequest request, String data,
            String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        String aesKey;
        DocumentDAO documentDao = new DocumentDAO();
        Object result;
        if (isSecurity != null && "1".equals(isSecurity)) {
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
            // Lay ma nhan vien
            String cardId = userGroup.getCardId();
            log.setUserName(cardId);
            // Ghi log bat dau chuc nang
            log.setParamList(data);
            LogUtils.logFunctionalStart(log);
            try {
                JSONObject json = new JSONObject(data);
                String[] keys = new String[]{
                    ConstantsFieldParams.DOCUMENT_ID
                };
                List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                Long documentId = Long.getLong(listValue.get(0));
                result = documentDao.checkIsPublished(documentId);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
            } catch (JSONException ex) {
                java.util.logging.Logger.getLogger(DocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return "";
    }

    /**
     * Kiem tra trang thai da ban hanh cua van ban
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getPublishedStatus(HttpServletRequest request, String data,
            String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
//        Integer status = 0;
        DocumentDAO documentDao = new DocumentDAO();
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        String aesKey;
        Object result;
        if (isSecurity != null && "1".equals(isSecurity)) {
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
            // Lay ma nhan vien
            String cardId = userGroup.getCardId();
            log.setUserName(cardId);
            // Ghi log bat dau chuc nang
            log.setParamList(data);
            LogUtils.logFunctionalStart(log);
            try {
                JSONObject json = new JSONObject(data);
                String[] keys = new String[]{
                    ConstantsFieldParams.DOCUMENT_ID
                };
                List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                Long documentId = Long.getLong(listValue.get(0));

                Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
                //Lay don vi theo user_role isdefaul =1
                Long sysOrgId = user2.getSysOrgId();

                result = documentDao.getPublishedStatus(documentId, sysOrgId);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
            } catch (JSONException ex) {
                java.util.logging.Logger.getLogger(DocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return "";
    }

    /**
     * Lay danh sach van ban ban hanh lai
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getAdjacentListByDocumentId(HttpServletRequest request, String data,
            String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String strResult = "";
        DocumentDAO documentDao = new DocumentDAO();
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        String aesKey;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
            // Lay ma nhan vien
            String cardId = userGroup.getCardId();
            log.setUserName(cardId);
            // Ghi log bat dau chuc nang
            log.setParamList(data);
            LogUtils.logFunctionalStart(log);
            try {
                JSONObject json = new JSONObject(data);
                String[] keys = new String[]{
                    ConstantsFieldParams.DOCUMENT_ID,
                    ConstantsFieldParams.ADJACENT_ID,
                    ConstantsFieldParams.ORDERTYPE_ID,
                    ConstantsFieldParams.START_RECORD,
                    ConstantsFieldParams.PAGE_SIZE};
                List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);

                Integer documentId = Integer.parseInt(listValue.get(0));
                Integer adjacentId = Integer.parseInt(listValue.get(1));
                Integer orderTypeId = Integer.parseInt(listValue.get(2));
                Long startRecord = null;
                String strStartRecord = listValue.get(3);
                if (!CommonUtils.isEmpty(strStartRecord)) {
                    startRecord = Long.parseLong(strStartRecord);
                }
                Long pageSize = null;
                String strPageSize = listValue.get(4);
                if (!CommonUtils.isEmpty(strPageSize)) {
                    pageSize = Long.parseLong(strPageSize);
                }

                Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
                //Lay don vi theo user_role isdefaul =1
                Long sysOrgId = user2.getSysOrgId();
                Long adOrgId = user2.getAdOrgId();

                Object result = documentDao.getAdjacentListByDocumentId(documentId,
                        sysOrgId, adOrgId, adjacentId, orderTypeId, startRecord, pageSize);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);

            } catch (JSONException ex) {
                java.util.logging.Logger.getLogger(DocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return strResult;
    }

    /**
     * Xuat bao cao van ban lien ke cuongnv: thay doi xuat bao cao . Bo xung
     * them cac tieu chi tim kiem khi xuat bao bao
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getDocumentAdjacentList(HttpServletRequest request, String data,
            String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("search - No session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Neu user hoac user id tren he thong 1&2 null
        // -> Tra ve thong bao khong co quyen
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        if ((user1 == null || user1.getUserId() == null)
                && (user2 == null || user2.getUserId() == null)) {
            LOGGER.error("search - user hoac userId tren he thong 1&2 null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Id nguoi dung tren he thong 1

        Long userId1 = null;
        if ((user1 != null && user1.getUserId() != null)) {
            userId1 = user1.getUserId();
        }
        // Id nguoi dung tren he thong 2
        Long userId2 = user2 == null ? 0L : user2.getUserId();
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
                ConstantsFieldParams.TYPE,
                ConstantsFieldParams.STATUS,
                ConstantsFieldParams.ADJACENT_ID,
                ConstantsFieldParams.KEYWORD,
                ConstantsFieldParams.DOCUMENT,
                ConstantsFieldParams.STAFF_IDS,
                ConstantsFieldParams.IS_SEARCH_DOC_MOBILE,
                ConstantsFieldParams.IS_FINANCE_TEXT
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Loai
            Integer type = Integer.parseInt(listValue.get(0));
            // Trang thai
            Integer status = Integer.parseInt(listValue.get(1));
            // Tu khoa
            String keyword = listValue.get(3);
            // Chuoi doi tuong van ban
            String strDocument = listValue.get(4);

            EntityDocument document = null;
            // Neu keyword khac rong (Tim kiem nhanh)
            // -> Tao doi tuong document theo keyword de tim kiem
            if (!CommonUtils.isEmpty(keyword)) {
                document = new EntityDocument();
                document.setCode(keyword);
                document.setTitle(keyword);
                document.setSigner(keyword);
                document.setQuickSearch(true);
                // Lay thong tin tim kiem van ban tai chinh
                if (!CommonUtils.isEmpty(strDocument)) {
                    Gson gson = new Gson();
                    EntityDocument targetDocument = gson.fromJson(strDocument,
                            EntityDocument.class);
                    if (targetDocument != null) {
                        document.setTypeId(targetDocument.getTypeId());
                        document.setAreaId(targetDocument.getAreaId());
                    }
                }
            } else {
                // Neu chuoi van ban khac rong (Tim kiem nang cao)
                // -> parse chuoi do sang doi tuong van ban
                if (!CommonUtils.isEmpty(strDocument)) {
                    Gson gson = new Gson();
                    document = gson.fromJson(strDocument, EntityDocument.class);
                }
            }
            // Chuoi cac id nguoi gui
            String staffIds = listValue.get(5);
            // Tim kiem count theo mobile
            String isSearchMobile = listValue.get(6);
            // Tim kiem van ban tai chinh
            Boolean isFinanceText = false;
            if (listValue.get(7) != null && "true".equals(listValue.get(7))) {
                isFinanceText = true;
            }

            //Van ban lien ke
            //cuongnv :: fix cung de xuat bao cao
            Integer adjacent = 1;
            DocumentDAO documentDAO = new DocumentDAO();
            // Lay cac van ban thoa man tieu chi tim kiem
            List<Long> lsSecretaryOrgIdVof2 = null;
            if (user2 != null) {
                lsSecretaryOrgIdVof2 = user2.getListSecretaryVhrOrg();
            }
            List<EntityDocument> result = documentDAO.search(userId1, userId2, user2,
                    type, status, adjacent, document, staffIds, null, null,
                    isSearchMobile, isFinanceText, lsSecretaryOrgIdVof2, null, null);
            //Lay thong tin cac don vi duoc ban hanh
            Object rs = documentDAO.getDocumentAdjacentList(result);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, rs, aesKey);
        } catch (JSONException | NumberFormatException | JsonSyntaxException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, "", "");
        }
    }

    /**
     * <b>Gui van ban truoc va sau khi ban hanh</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String tranferTextPromulgateOrNotPromulgate(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String strResult;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        DocumentInStaffDAO documentDao = new DocumentInStaffDAO();
        if (userGroup.getCheckSessionOk()) {
            String cardId = "";
            try {
                //lay gia tri tu session
                String aesKey = userGroup.getStrAesKey();
                data = SecurityControler.decodeDataByAes(aesKey, data);
                // Lay ma nhan vien
                cardId = userGroup.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(data);
                LogUtils.logFunctionalStart(log);
                // Parse du lieu
                JSONObject json = new JSONObject(data);
                String[] keys = new String[]{
                    ConstantsFieldParams.TEXT_DOC_ID,
                    ConstantsFieldParams.COMMENT,
                    ConstantsFieldParams.TEXT_ID,
                    ConstantsFieldParams.STATE,
                    ConstantsFieldParams.LIST_RECEIVER,
                    ConstantsFieldParams.LIST_GROUP,
                    ConstantsFieldParams.LIST_CV_GROUP,
                    ConstantsFieldParams.LIST_STAFF_NOT_SEND,
                    ConstantsFieldParams.SEND_SMS,
                    "isSendAndWarning"
                };
                List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                // Id cong van
                Long docId = null;
                String strDocId = listValue.get(0);
                if (!CommonUtils.isEmpty(strDocId) && !"0L".equals(strDocId)) {
                    docId = Long.parseLong(strDocId);
                }
                // Noi dung chi dao
                String comment = listValue.get(1);
                // Id van ban
                Long textId = null;
                String strTextId = listValue.get(2);
                if (!CommonUtils.isEmpty(strTextId) && !"0L".equals(strTextId)) {
                    textId = Long.parseLong(strTextId);
                }
                // Trang thai
                Long state = null;
                String strState = listValue.get(3);
                if (!CommonUtils.isEmpty(strState)) {
                    state = Long.parseLong(strState);
                }
                // Danh sach nguoi nhan van ban
                String strListReceiver = listValue.get(4);
                String strListGroup = listValue.get(5);
                String strListCvGroup = listValue.get(6);
                String strListStaffNotSend = listValue.get(7);
                Integer sendSMS = null;
                String strSendSMS = listValue.get(8);
                if ("1".equals(strSendSMS)) {
                    sendSMS = Integer.parseInt(strSendSMS);
                }
                //250417 sua canh bao chuyen van ban
                String isSendAndWarning = listValue.get(9);
                Gson gson = new Gson();
                // Lay danh nguoi nhan van ban
                List<Vof2_EntityUser> listReceiver;
                if (CommonUtils.isEmpty(strListReceiver)) {
                    listReceiver = new ArrayList<>();
                } else {
                    Type listVof2EntityUserType = new TypeToken<ArrayList<Vof2_EntityUser>>() {
                    }.getType();
                    listReceiver = gson.fromJson(strListReceiver, listVof2EntityUserType);
                }

                // Lay danh sach don vi
                List<GroupMapping> listGroup;
                if (CommonUtils.isEmpty(strListGroup)) {
                    listGroup = new ArrayList<>();
                } else {
                    Type listGroupMappingType = new TypeToken<ArrayList<GroupMapping>>() {
                    }.getType();
                    listGroup = gson.fromJson(strListGroup, listGroupMappingType);
                    for (GroupMapping groupMapping : listGroup) {
                        groupMapping.setGroupVof2(groupMapping.getSysOrgId());
                    }
                }

                // Lay danh sach nhom ca nhan
                List<EntityCvGroup> listCvGroup;
                if (CommonUtils.isEmpty(strListCvGroup)) {
                    listCvGroup = new ArrayList<>();
                } else {
                    Type listCvGroupType = new TypeToken<ArrayList<EntityCvGroup>>() {
                    }.getType();
                    listCvGroup = gson.fromJson(strListCvGroup, listCvGroupType);
                }

                // Lay danh sach nguoi loai bo chuyen nhom ca nhan
                List<Vof2_EntityUser> listStaffNotSend;
                if (CommonUtils.isEmpty(strListStaffNotSend)) {
                    listStaffNotSend = new ArrayList<>();
                } else {
                    Type listVof2EntityUserType = new TypeToken<ArrayList<Vof2_EntityUser>>() {
                    }.getType();
                    listStaffNotSend = gson.fromJson(strListStaffNotSend, listVof2EntityUserType);
                }
                if (textId != null) {
                    EntitySendDocResult sendDocResult = documentDao.tranferTextPromulgateOrNotPromulgate(
                            docId, listReceiver, comment, state, textId, listCvGroup, listGroup,
                            userGroup.getItemEntityUser(), userGroup.getVof2_ItemEntityUser(),
                            listStaffNotSend, sendSMS);
                    int result = sendDocResult.getSendResult();
                    //250417 sua canh bao chuyen van ban
                    if (isSendAndWarning != null && "1".equals(isSendAndWarning)) {
                        //neu la sua canh bao
                        strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, sendDocResult, aesKey);
                    } else {
                        //theo luong cu
                        strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
                    }
                } else {
                    LOGGER.error("tranferTextPromulgateOrNotPromulgate (Chuyen"
                            + " van ban truoc va sau khi ban hanh) - textId = null"
                            + " - username: " + cardId + "\ndata: " + data);
                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA,
                            null, null);
                }
            } catch (Exception ex) {
                LOGGER.error("tranferTextPromulgateOrNotPromulgate (Chuyen van"
                        + " ban truoc va sau khi ban hanh) - Exception - username: "
                        + cardId + "\ndata: " + data, ex);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
        } else {
            LOGGER.error("tranferTextPromulgateOrNotPromulgate (Chuyen van ban"
                    + " truoc va sau khi ban hanh) - Exception - Session timeout!");
            strResult = FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return strResult;
    }

    /**
     * <b>Chuyen van ban cho ca nhan/don vi/nhom ca nhan</b>
     *
     * @param req
     * @param data
     * @param isSecurity
     * @return
     */
    public String sendDocument(HttpServletRequest req, String data,
            String isSecurity) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(req);
        Date startTime = new Date();
        String cardId = "";
        String deviceName = null;
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        if (userGroup.getCheckSessionOk()) {
            try {
                // Giai ma du lieu
                String aesKey = userGroup.getStrAesKey();
                data = SecurityControler.decodeDataByAes(aesKey, data);
                // Lay ma nhan vien
                cardId = userGroup.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(data);
                LogUtils.logFunctionalStart(log);

                // Parse du lieu
                JSONObject json = new JSONObject(data);
                String[] keys = new String[]{
                    ConstantsFieldParams.TEXT_DOC_ID,
                    ConstantsFieldParams.COMMENT,
                    ConstantsFieldParams.DOCUMENT_IN_STAFF_ID,
                    ConstantsFieldParams.LIST_RECEIVER,
                    ConstantsFieldParams.LIST_GROUP,
                    ConstantsFieldParams.LIST_CV_GROUP,
                    ConstantsFieldParams.LIST_STAFF_NOT_SEND,
                    ConstantsFieldParams.DEVICE_NAME,
                    ConstantsFieldParams.SEND_SMS,
                    ConstantsFieldParams.IS_SEND_WARNING,
                    ConstantsFieldParams.IS_SEND_FILE_COMMENT,
                    "sendingFrom" , "lstConnectOrg"
                };
                List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                Long docId = Long.parseLong(listValue.get(0));
                String comment = listValue.get(1);
                Long documentInStaffId = null;
                String strDocumentInStaffId = listValue.get(2);
                if (!CommonUtils.isEmpty(strDocumentInStaffId)) {
                    documentInStaffId = Long.parseLong(strDocumentInStaffId);
                }
                String strListReceiver = listValue.get(3);
                String strListGroup = listValue.get(4);
                String strListPersonalGroup = listValue.get(5);
                String strListStaffNotSend = listValue.get(6);
                deviceName = listValue.get(7);
                Integer sendSMS = null;
                String strSendSMS = listValue.get(8);
                if ("1".equals(strSendSMS)) {
                    sendSMS = Integer.parseInt(strSendSMS);
                }
                Integer isSendFileComment = null;
                String isSendAndWarning = listValue.get(9);
                if ("1".equals(listValue.get(10))) {
                    isSendFileComment = Integer.parseInt(listValue.get(10));
                }
                Integer sendingFrom = null;
                if (!CommonUtils.isEmpty(listValue.get(11))) {
                    sendingFrom = Integer.parseInt(listValue.get(11));
                }
                
                // Kiem tra van ban duoc chuyen co bi thu hoi hay khong
                DocumentDAO dDao = new DocumentDAO();
                if (documentInStaffId != null && !dDao.checkStatusDocumentInStaff(documentInStaffId)) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.ERR_STATUS, "", aesKey);
                }
                Gson gson = new Gson();
                // Lay danh nguoi nhan van ban
                List<Vof2_EntityUser> listReceiver;
                if (CommonUtils.isEmpty(strListReceiver)) {
                    listReceiver = new ArrayList<>();
                } else {
                    Type listVof2EntityUserType = new TypeToken<ArrayList<Vof2_EntityUser>>() {
                    }.getType();
                    listReceiver = gson.fromJson(strListReceiver, listVof2EntityUserType);
                }

                // outsource sonnd
                // kiem tra xem van ban co gui sms cho tro lys khong?
                // begin
                SmsDAO smsDao = new SmsDAO();
                Long employeeId = 0L;
                Long leaderId = userGroup.getVof2_ItemEntityUser().getUserId();
                List<EntityDocumentInStaff> lstStaff = smsDao.getDocumentStaff(leaderId, docId, 0);
                boolean isSendSms;
                if (lstStaff == null || lstStaff.isEmpty()) {
//                    isSendSms = false;
                } else {
                    employeeId = lstStaff.get(0).getStaffId2();
                }
                if (employeeId.equals(0L)) {
                    isSendSms = false;
                } else if (smsDao.checkDocumentSendByAssistant(employeeId, leaderId, docId) != 1) {
                    isSendSms = false;
//                    System.out.println("Khong  gui sms");
                    //OS check thua
//                } else if ((smsDao.checkAssistantOfLeader(employeeId, leaderId) == 1)) {
//                    System.out.println("Co gui sms:");
//                    isSendSms = true;
                } else {
//                    System.out.println("Co gui sms");
                    isSendSms = true;
                }
                // end.

                // Lay danh sach don vi
                List<GroupMapping> listGroup;
                if (CommonUtils.isEmpty(strListGroup)) {
                    listGroup = new ArrayList<>();
                } else {
                    Type listGroupMappingType = new TypeToken<ArrayList<GroupMapping>>() {
                    }.getType();
                    listGroup = gson.fromJson(strListGroup, listGroupMappingType);
                    for (GroupMapping groupMapping : listGroup) {
                        groupMapping.setGroupVof2(groupMapping.getSysOrgId());
                    }
                }

                // Lay danh sach nhom ca nhan
                List<EntityCvGroup> listCvGroup;
                if (CommonUtils.isEmpty(strListPersonalGroup)) {
                    listCvGroup = new ArrayList<>();
                } else {
                    Type listPersonalGroupType = new TypeToken<ArrayList<EntityCvGroup>>() {
                    }.getType();
                    listCvGroup = gson.fromJson(strListPersonalGroup, listPersonalGroupType);
                }

                // Lay danh sach nguoi loai bo chuyen nhom ca nhan
                List<Vof2_EntityUser> listStaffNotSend;
                if (CommonUtils.isEmpty(strListStaffNotSend)) {
                    listStaffNotSend = new ArrayList<>();
                } else {
                    Type listVof2EntityUserType = new TypeToken<ArrayList<Vof2_EntityUser>>() {
                    }.getType();
                    listStaffNotSend = gson.fromJson(strListStaffNotSend, listVof2EntityUserType);
                }
                
                // Lay danh sach don vi lien thong
                List<EntityConnectVHR> lstConnectOrg;
                if (CommonUtils.isEmpty(listValue.get(12))) {
                    lstConnectOrg = new ArrayList<>();
                } else {
                    Type lstConnectOrgType = new TypeToken<ArrayList<EntityConnectVHR>>() {
                    }.getType();
                    lstConnectOrg = gson.fromJson(listValue.get(12), lstConnectOrgType);
                }
                
                DocumentInStaffDAO documentDao = new DocumentInStaffDAO();
                EntitySendDocResult sendDocResult = documentDao.sendDocument(docId,
                        comment, listReceiver, listCvGroup, listGroup, null,
                        userGroup.getItemEntityUser(), userGroup.getVof2_ItemEntityUser(),
                        listStaffNotSend, sendSMS, isSendFileComment, sendingFrom);
                int result = sendDocResult.getSendResult();
                
                // Chuyen van ban cho don vi lien thong
                if (!CommonUtils.isEmpty(lstConnectOrg)) {
                    ConnectVHRDao dao = new ConnectVHRDao();
                    EntityConnectVHR connectVhr = dao.getConnectVHRCode(userGroup.getVof2_ItemEntityUser().getListSecretaryVhrOrg());
                    boolean success = documentDao.sendDocumentToConnectOrg(userGroup.getUserId1(), userGroup.getUserId2(),
                            docId, lstConnectOrg, connectVhr, userGroup.getVof2_ItemEntityUser(), comment);
                    if (success) {
                        result = 1;
                        sendDocResult.setSendResult(result);
                    }
                }
                
                //250417 sua canh bao chuyen van ban
                if (isSendAndWarning != null && "1".equals(isSendAndWarning)) {
                    //neu co canh bao
                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, sendDocResult, aesKey);
                } else {
                    //theo xu li cu
                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
                }
                switch (result) {
                    // Chuyen van ban thanh cong
                    case 1:
                        // sonnd
                        // gui tin nhan cho tro ly
                        if (isSendSms) {
                            List<EntityVhrOrg> lstVhrEntity = null;
                            List<EntityCvGroup> lstGroupCv = null;
                            if (listGroup != null && !CommonUtils.isEmpty(listGroup)) {
                                List<Long> orgids = new ArrayList<Long>();
                                for (GroupMapping groupMapping : listGroup) {
                                    if (groupMapping.getGroupVof2() != null) {
                                        orgids.add(groupMapping.getGroupVof2());
                                    }
                                }
                                lstVhrEntity = smsDao.getListVHROrgToSendSms(orgids);
                            }
                            if (listCvGroup != null && !CommonUtils.isEmpty(listCvGroup)) {
                                lstGroupCv = smsDao.getListGroupPersonalToSendSms(listCvGroup);
                            }
                            EntityDocument document = smsDao.getDocument(docId);
                            if (document != null && document.getTitle() != null) {
                                smsDao.sendSmsAssistantDocument(null, listReceiver, lstVhrEntity, lstGroupCv, employeeId,
                                        comment, userGroup.getVof2_ItemEntityUser().getAliasName(), document.getTitle(),
                                        Constants.SMS_TEXT_INTERCEPT.SECRETARY_LEADERHANDLEDOC
                                );
                            }
                        }
                        // end send sms 
                        errorCode = ErrorCode.SUCCESS;
                        break;
                    // Co loi xay ra khong xac dinh duoc
                    case 0:
                        errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
                        break;
                    // Loi khong lay duoc thong tin nguoi dung
                    case DocumentInStaffDAO.SESSION_EXPIRE_ERROR:
                        errorCode = ErrorCode.ERR_NODATA;
                        break;
                    // Loi du lieu chuyen van ban khong hop le
                    case DocumentInStaffDAO.DATA_ERROR:
                        errorCode = ErrorCode.INPUT_INVALID;
                        break;
                }
            } catch (JsonSyntaxException | NumberFormatException | JSONException ex) {
                LOGGER.error("sendDocument (Chuyen van ban cho ca nhan/don vi/nhom ca nhan)"
                        + " - Exception - username: " + cardId + "\ndata: " + data, ex);
                errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
                strResult = FunctionCommon.generateResponseJSON(errorCode, null, null);
            }
        } else {
            LOGGER.error("sendDocument (Chuyen van ban cho ca nhan/don vi/nhom ca nhan)"
                    + " - Session timeout!");
            errorCode = userGroup.getEnumErrCode();
            strResult = FunctionCommon.generateResponseJSON(errorCode, null, null);
        }
        // Ghi log database
        ActionLogMobileDAO actionLogMobileDAO = new ActionLogMobileDAO();
        EntityUser user1 = userGroup.getItemEntityUser();
        Long userId1 = (user1 == null || user1.getUserId() == null) ? 0L : user1.getUserId();
        String description = "SERVICE2: Chuyen van ban - errorCode: "
                + errorCode.getErrorCode();
        actionLogMobileDAO.insert(userId1, cardId, startTime, new Date(),
                "DocumentAction.sendDocument", description, req.getLocalAddr()
                + ":" + req.getLocalPort(), null, null, deviceName, null);
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return strResult;
    }

    /**
     * <b>Chuyen van ban tai chinh</b><br>
     *
     * @param req
     * @param strData
     * @param isSecurity
     * @return
     */
    public String sendFinanceTextToStaff(HttpServletRequest req, String strData,
            String isSecurity) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        DocumentDAO documentDao = new DocumentDAO();
        if (dataSessionGR.getCheckSessionOk()) {
            String strDataClient = "";
            String cardId = "";
            try {
                //lay gia tri tu session
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                // Lay ma nhan vien
                cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);

                //Lay prams client gui len de thuc hien yeu cau
                HashMap<String, Object> hmParams = new HashMap<>();
                hmParams.put(ConstantsFieldParams.TEXT_DOC_ID, Long.class);
                hmParams.put(ConstantsFieldParams.comment, String.class);
                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
                //lay du lieu
                Long docId = (Long) ((valueParams.get(ConstantsFieldParams.TEXT_DOC_ID) == null
                        || valueParams.get(ConstantsFieldParams.TEXT_DOC_ID).equals(0L)) ? null : valueParams.get(ConstantsFieldParams.TEXT_DOC_ID));
                String comment = (String) ((valueParams.get(ConstantsFieldParams.comment) != null) ? valueParams.get(ConstantsFieldParams.comment) : "");

                //Lay danh nguoi nhan van ban
                List<Vof2_EntityUser> listStaffReceiver = new ArrayList<>();
                JSONArray arrStaffReceiver = FunctionCommon.jsonGetArray("listStaffReceiver", strDataClient);

                if (arrStaffReceiver != null && arrStaffReceiver.length() > 0) {
                    int arrStaffReceiverSize = arrStaffReceiver.length();
                    Long userIdVof2;
                    Long groupIdVof2;
                    Vof2_EntityUser userVof2;
                    for (int i = 0; i < arrStaffReceiverSize; i++) {
                        JSONObject innerObj = (JSONObject) arrStaffReceiver.get(i);
                        userIdVof2 = null;
                        if (innerObj.has("userId") && innerObj.getString("userId") != null) {
                            userIdVof2 = Long.parseLong(innerObj.getString("userId").trim());
                        }
                        groupIdVof2 = null;
                        if (innerObj.has("sysOrgId") && innerObj.getString("sysOrgId") != null) {
                            groupIdVof2 = Long.parseLong(innerObj.getString("sysOrgId").trim());
                        }
                        if (userIdVof2 != null && groupIdVof2 != null) {
                            userVof2 = new Vof2_EntityUser();
                            userVof2.setUserId(userIdVof2);
                            userVof2.setSysOrgId(groupIdVof2);
                            listStaffReceiver.add(userVof2);
                        }
                    }
                }
                if (docId != null && listStaffReceiver.size() > 0) {
                    EntityResult result = documentDao.sendFinanceTextToStaff(docId, listStaffReceiver, comment,
                            dataSessionGR.getItemEntityUser(), dataSessionGR.getVof2_ItemEntityUser());
                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);

                } else {
                    LOGGER.error("sendFinanceTextToStaff (Chuyen van ban tai chinh)"
                            + " - docId null hoac listStaffReceiver null/rong - username: "
                            + cardId + "\ndata: " + strDataClient);
                    strResult = FunctionCommon.generateResponseJSON(
                            ErrorCode.ERR_NODATA, null, null);
                }
            } catch (Exception ex) {
                LOGGER.error("sendFinanceTextToStaff (Chuyen van ban tai chinh)"
                        + " - Exception - username: " + cardId + "\ndata: " + strDataClient, ex);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
        } // Session timeout
        else {
            LOGGER.error("sendFinanceTextToStaff (Chuyen van ban tai chinh) - Session timeout!");
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return strResult;
    }

    /**
     * <b>Lay thong tin trang thai van ban dung cho phien ban web</b><br/>
     *
     * @autor hiendv2
     * @since Sep 27, 2016
     * @param request Doi tuong request tu client -> server
     * @param data Gia tri data trong request
     * @param isSecurity Gia tri isSecurity trong request
     * @return
     */
    public String getStatusDocumentInStaff(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getStatusDocumentInStaff - No session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Neu user hoac user id tren he thong 1&2 null
        // -> Tra ve thong bao khong co quyen
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        if ((user1 == null || user1.getUserId() == null)
                && (user2 == null || user2.getUserId() == null)) {
            LOGGER.error("getStatusDocumentInStaff - user hoac userId tren he thong 1&2 null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Neu user1 null thi gan userId1 bang null
        Long userId1 = user1 == null ? null : user1.getUserId();
        // Neu user2 null thi gan userId2 bang null
        Long userId2 = user2 == null ? null : user2.getUserId();

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
                ConstantsFieldParams.SENDER_ID1,
                ConstantsFieldParams.SENDER_ID2,
                ConstantsFieldParams.DOCUMENT_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Id nguoi gui tren he thong 1
            Long senderId1 = null;
            String strSenderId1 = listValue.get(0);
            if (!CommonUtils.isEmpty(strSenderId1)) {
                senderId1 = Long.parseLong(strSenderId1);
            }
            //Id nguoi gui tren he thong 2
            Long senderId2 = null;
            String strSenderId2 = listValue.get(1);
            if (!CommonUtils.isEmpty(strSenderId2)) {
                senderId2 = Long.parseLong(strSenderId2);
            }
            // Id van ban
            Long documentId = Long.parseLong(listValue.get(2));

            //Lay trang thai
            DocumentDAO docDAO = new DocumentDAO();
            EntityDocument documentBO = docDAO.getStatusDocumentInStaff(userId1,
                    userId2, senderId1, senderId2, documentId);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            // -> Tra ve thong bao loi server
            if (documentBO == null) {
                LOGGER.error("getStatusDocumentInStaff - userId1: " + userId1
                        + " - userId2: " + userId2 + " - listThread null hoac rong");

                return FunctionCommon.generateResponseJSON(
                        ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            }

            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, documentBO, aesKey);

        } catch (Exception ex) {
            LOGGER.error("getStatusDocumentInStaff - userId1: " + userId1 + " - userId2: "
                    + userId2 + " - Exception: " + ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    /**
     * Lay ds tat ca cac file cua van ban dinh kem VBTK
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListAllFileDocAttach(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getListAllFileDocAttach - No session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Neu user hoac user id tren he thong 1&2 null
        // -> Tra ve thong bao khong co quyen
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        if ((user1 == null || user1.getUserId() == null)
                && (user2 == null || user2.getUserId() == null)) {
            LOGGER.error("getListAllFileDocAttach - user hoac userId tren he thong 1&2 null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Neu user1 null thi gan userId1 bang null
        Long userId1 = user1 == null ? null : user1.getUserId();
        // Neu user2 null thi gan userId2 bang null
        Long userId2 = user2 == null ? null : user2.getUserId();

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
                ConstantsFieldParams.TEXT_ID,
                ConstantsFieldParams.DOCUMENT_ID};

            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long textId = null;
            String strTextId = listValue.get(0);
            if (!CommonUtils.isEmpty(strTextId)) {
                textId = Long.parseLong(strTextId);
            }

            Long docId = null;
            String strDocId = listValue.get(1);
            if (!CommonUtils.isEmpty(strDocId)) {
                docId = Long.parseLong(strDocId);
            }

            DocumentDAO docDAO = new DocumentDAO();
            List<EntityFileAttachment> listResult = docDAO.getListAllFileDocAttach(
                    textId, docId);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, listResult, aesKey);
        } catch (Exception ex) {
            LOGGER.error("getListAllFileDocAttach - userId1: " + userId1 + " - userId2: "
                    + userId2 + " - Exception: " + ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    /**
     * <b>xuat bao cao van ban tai chinh</b>
     *
     * @author cuongnv
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String exportFinanceText(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("exportFinanceText (Xuat bao cao van ban tai chinh) - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Neu user hoac user id tren he thong 1&2 null
        // -> Tra ve thong bao khong co quyen
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();

        if (!userGroup.checkUserId()) {
            LOGGER.error("exportFinanceText (Xuat bao cao van ban tai chinh) - "
                    + "user hoac userId tren he thong 1&2 null!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Id nguoi dung tren he thong 2
        Long userId2 = user2 == null ? 0L : user2.getUserId();
        // Id nguoi dung tren he thong 1
        Long userId1 = null;
        if ((user1 != null && user1.getUserId() != null)) {
            userId1 = user1.getUserId();
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
                ConstantsFieldParams.TYPE,
                ConstantsFieldParams.STATUS,
                ConstantsFieldParams.ADJACENT_ID,
                ConstantsFieldParams.KEYWORD,
                ConstantsFieldParams.DOCUMENT,
                ConstantsFieldParams.STAFF_IDS,
                ConstantsFieldParams.START_RECORD,
                ConstantsFieldParams.PAGE_SIZE,
                ConstantsFieldParams.IS_SEARCH_DOC_MOBILE,
                ConstantsFieldParams.IS_FINANCE_TEXT,
                ConstantsFieldParams.DEVICE_NAME,
                ConstantsFieldParams.DOCUMENT_TYPE
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Loai
            Integer type = Integer.parseInt(listValue.get(0));
            // Trang thai
            Integer status = Integer.parseInt(listValue.get(1));
            // Tu khoa
            String keyword = listValue.get(3);
            // Chuoi doi tuong van ban
            String strDocument = listValue.get(4);
            //Loai van ban lien ke
            String strAdjacent = listValue.get(2);
            // Parse chuoi ra doi tuong EntityDocument
            EntityDocument document = null;
            if (!CommonUtils.isEmpty(strDocument)) {
                Gson gson = new Gson();
                document = gson.fromJson(strDocument, EntityDocument.class);
            }
            // Neu keyword khac rong (Tim kiem nhanh)
            // Gan cac truong mong muon tim kiem theo tu khoa
            if (!CommonUtils.isEmpty(keyword)) {
                if (document == null) {
                    document = new EntityDocument();
                }
                document.setCode(keyword);
                document.setTitle(keyword);
                document.setSigner(keyword);
                document.setQuickSearch(true);
                try {
                    Long documentId = Long.parseLong(keyword);
                    document.setDocumentId(documentId);
                } catch (Exception ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
            // Chuoi cac id nguoi gui
            String staffIds = listValue.get(5);
            // Vi tri lay ra
            Long startRecord = null;
            Long pageSize = null;
            // Tim kiem count theo mobile
            String isSearchMobile = listValue.get(8);
            // Tim kiem van ban tai chinh
            Boolean isFinanceText = false;
            if (listValue.get(9) != null && "true".equals(listValue.get(9))) {
                isFinanceText = true;
            }
            // Ten thiet bi
//            deviceName = listValue.get(10);

            //Van ban lien ke
            Integer adjacent = 0;
            if (!CommonUtils.isEmpty(strAdjacent)) {
                adjacent = Integer.parseInt(strAdjacent);
            }
            // Truy van database
            List<Long> lsSecretaryOrgIdVof2 = null;
            if (user2 != null) {
                lsSecretaryOrgIdVof2 = user2.getListSecretaryVhrOrg();
            }

            DocumentLibraryDAO documentLibraryDAO = new DocumentLibraryDAO();
            List<EntityDocument> result = documentLibraryDAO.exportFinanceText(userId1, userId2, user2,
                    type, status, adjacent, document, staffIds, startRecord, pageSize,
                    isSearchMobile, isFinanceText, lsSecretaryOrgIdVof2);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } // Loi server
        catch (Exception ex) {
            LOGGER.error("exportFinanceText (Xuat bao cao van ban tai chinh) -"
                    + " Exception - username: " + cardId + "\ndata:" + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Cap nhat loai van ban don vi/ca nhan</b>
     *
     * @author cuongnv
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String saveDocumentType(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("search - No session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Neu user hoac user id tren he thong 1&2 null
        // -> Tra ve thong bao khong co quyen
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();

        if ((user1 == null || user1.getUserId() == null)
                && (user2 == null || user2.getUserId() == null)) {
            LOGGER.error("search - user hoac userId tren he thong 1&2 null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
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
            List<EntityDocument> listDocument = new ArrayList<>();
            if (json.has(ConstantsFieldParams.DOCUMENT_IN_STAFF_LIST_ID)) {
                JSONArray jsonArray = json.getJSONArray(ConstantsFieldParams.DOCUMENT_IN_STAFF_LIST_ID);
                if (jsonArray.length() > 0) {
                    EntityDocument document;
                    Long documentInStaffId;
                    Integer documentType;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        String[] keys = new String[]{
                            ConstantsFieldParams.DOCUMENT_IN_STAFF_ID,
                            ConstantsFieldParams.DOCUMENT_TYPE
                        };
                        List<String> listValue = FunctionCommon.getValuesFromJSON(obj, keys);
                        try {
                            documentInStaffId = Long.parseLong(listValue.get(0));
                            documentType = Integer.parseInt(listValue.get(1));
                            document = new EntityDocument();
                            document.setDocumentInStaffId(documentInStaffId);
                            document.setDocumentType(documentType);
                            listDocument.add(document);
                        } catch (NumberFormatException e) {
                            //
                        }
                    }
                }
            }
            if (listDocument.isEmpty()) {
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, null, null);
            } else {
                DocumentDAO d = new DocumentDAO();
                Integer rs = d.saveDocumentType(listDocument);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, rs, aesKey);
            }
        } // Du lieu dau vao khong hop le
        catch (JSONException ex) {
            LOGGER.error(ErrorCode.ERR_NODATA, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, null, null);
        }
    }

    /**
     * kiem tra van ban cung nhan van ban trong don vi
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String checkIsOnlyReceiveDoc(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String result;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            //het session
            LOGGER.error("checkIsOnlyReceiveDoc - No session");
            result = FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        } else {
            Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
            if ((user2 == null || user2.getUserId() == null)) {
                //khong ton tai tren he thong voffice 2
                LOGGER.error("checkIsOnlyReceiveDoc - user khong ton tai tren he thong 2");
                result = FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
            } else {
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
                    String[] keys = new String[]{ConstantsFieldParams.DOCUMENT_IN_STAFF_ID};
                    List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                    String strDocInStaffId = listValue.get(0);
                    Long docInStaffId = null;
                    if (!CommonUtils.isEmpty(strDocInStaffId)) {
                        docInStaffId = Long.parseLong(strDocInStaffId.trim());
                    }
                    if (docInStaffId != null) {
                        DocumentInStaffDAO documentInStaffDAO = new DocumentInStaffDAO();
                        int count = documentInStaffDAO.countReceiveDocInGroup(docInStaffId);
                        if (count >= 0) {
                            //thanh cong
                            result = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, count, aesKey);
                        } else {
                            //khong thanh cong
                            result = FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, count, aesKey);
                        }
                    } else {
                        //loi du lieu dau vao
                        result = FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                    }
                    // Ghi log ket thuc chuc nang
                    LogUtils.logFunctionalEnd(log);
                } catch (JSONException | NumberFormatException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                    //co loi xay ra
                    LOGGER.error("checkIsOnlyReceiveDoc - co loi xay ra - userId2: "
                            + user2.getUserId() + " - Exception: " + ex.getMessage());
                    result = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
            }
        }
        return result;
    }

    public String getProcessedDetailByUser(HttpServletRequest request,
            String data, String isSecurity) {
        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup entityUserGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!entityUserGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(entityUserGroup.getEnumErrCode(), null, null);
        }

        // Neu user hoac user id tren he thong 1/2 null
        // -> Tra ve thong bao khong co quyen
        EntityUser user1 = entityUserGroup.getItemEntityUser();
        Vof2_EntityUser user2 = entityUserGroup.getVof2_ItemEntityUser();
        if ((user1 == null || user1.getUserId() == null)
                && (user2 == null || user2.getUserId() == null)) {
            LOGGER.error("getListReceiver - user hoac userId null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Lay ma nhan vien
        String cardId = entityUserGroup.getVof2_ItemEntityUser().getStrCardNumber();
        log.setUserName(cardId);
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = entityUserGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        //System.out.println("luanvd-getProcessedDetailByUser-data: "+data);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.DOCUMENT_ID,
                ConstantsFieldParams.SENDER_ID_VOF1,
                ConstantsFieldParams.SENDER_ID_VOF2,
                ConstantsFieldParams.TYPE,
                ConstantsFieldParams.PAGE_SIZE,
                ConstantsFieldParams.START_RECORD,
                "receiverDate"};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long documentId;
            if (listValue.get(0) != null && listValue.get(0).trim().length() > 0) {
                documentId = Long.parseLong(listValue.get(0));
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
            Long senderIdVof1 = null;
            if (listValue.get(1) != null && listValue.get(1).trim().length() > 0) {
                senderIdVof1 = Long.parseLong(listValue.get(1));
            }
            Long senderIdVof2 = null;
            if (listValue.get(2) != null && listValue.get(2).trim().length() > 0) {
                senderIdVof2 = Long.parseLong(listValue.get(2));
            }

            if (senderIdVof1 == null && senderIdVof2 == null) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
            Long type = null;
            if (listValue.get(3) != null && listValue.get(3).trim().length() > 0) {
                type = Long.parseLong(listValue.get(3));
            }
            //Tạm thời client ko gửi 2 biến này - trả tất cả dữ liệu
            Long startRecord = 0L;
            if (listValue.get(4) != null && listValue.get(4).trim().length() > 0) {
                startRecord = Long.parseLong(listValue.get(4));
            }
            Long pageSize = null;
            if (listValue.get(5) != null && listValue.get(5).trim().length() > 0) {
                pageSize = Long.parseLong(listValue.get(5));
            }
            //050517 them ngay nhan van ban
            String receiverDate = listValue.get(6);
            DocumentInStaffDAO documentInStaffDAO = new DocumentInStaffDAO();
            documentInStaffDAO.setStrReceiveDateTmp(receiverDate);
            EntityDocumentInStaff entityDoc = new EntityDocumentInStaff();
            //Lay danh sach ca nhan da chuyen
            if (type == null || type == 1L) {
                if (senderIdVof1 != null || senderIdVof2 != null) {

                    List<EntityDocumentInStaff> listEmpReciver = documentInStaffDAO.getListReceiver(senderIdVof1, senderIdVof2,
                            documentId, startRecord, pageSize, senderIdVof2, senderIdVof1, 0L);
                    entityDoc.setListTransferedToPersonal(listEmpReciver);
                }
            }

            //Lay danh sach don vi user da chuyen
            if (type == null || type == 2L) {
                DocumentInGroupDAO documentInGroupDAO = new DocumentInGroupDAO();
                documentInGroupDAO.setStrReceiveDateTmp(receiverDate);
                List<EntityDocumentInGroup> listGroup1 = documentInGroupDAO.
                        getListGroup(senderIdVof1, senderIdVof2, documentId, startRecord, pageSize);
                entityDoc.setListTransferedToGroup(listGroup1);
            }
            //Lấy nhóm đã chuyển
            if (type == null || type == 3L) {
                DocumentDAO documentDao = new DocumentDAO();
                documentDao.setStrReceiveDateTmp(receiverDate);
                if (senderIdVof1 != null || senderIdVof2 != null) {
                    List<EntityCvGroup> lstRecever = documentDao.getListReceivedCvGroup(senderIdVof1,
                            senderIdVof2, documentId);
                    entityDoc.setListTransferedToCvGroup(lstRecever);
                }
            }

            //Lay danh sach nhiem vu da giao
            if (type == null || type == 4L) {
                List<EntityMission> listMission = documentInStaffDAO.getListMissionAssigned(senderIdVof2, documentId);
                entityDoc.setListMissionAssigned(listMission);
            }

            //Lay danh sach cong viec da giao
            if (type == null || type == 5L) {
                List<EntityTask> listTask = documentInStaffDAO.getListTaskAssigned(senderIdVof2, documentId);
                entityDoc.setListTaskAssigned(listTask);
            }

            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            if (entityDoc != null) {
            	return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, entityDoc, aesKey);
            }
            //System.out.println("luanvd-getProcessedDetailByUser-result:\n "+FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, null));
        } catch (JSONException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
    }

    /**
     * <b>Xuat bao cao luan chuyen van ban</b>
     *
     * @author cuongnv
     * @since 25/2/2017
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String exportCirculationTree(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("exportCirculationTree - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Neu user hoac user id tren he thong 1&2 null
        // -> Tra ve thong bao khong co quyen
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        if ((user1 == null || user1.getUserId() == null)
                && (user2 == null || user2.getUserId() == null)) {
            LOGGER.error("exportCirculationTree - user hoac userId tren he thong 1&2 null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Neu khong co thong tin tren he thong 1
        // -> Gan userId1 = 0
        Long userId1 = (user1 == null || user1.getUserId() == null) ? 0L : user1.getUserId();
        // Neu khong co thong tin tren he thong 2
        // -> Gan userId2 = 0
        Long userId2 = (user2 == null || user2.getUserId() == null) ? 0L : user2.getUserId();
        // Lay ma nhan vien
        String cardId = "";
        String name = "";
        String email = "";
        if (user2 != null) {
            name = user2.getFullName();
            email = user2.getStrEmail();
        } else if (user1 != null) {
            name = user1.getFullName();
            email = user1.getStrEmail();
        }
        if (user2 != null && !CommonUtils.isEmpty(user2.getStrCardNumber())) {
            cardId = user2.getStrCardNumber();
            name = user2.getFullName();
        } else if (user1 != null && !CommonUtils.isEmpty(user1.getStrCardNumber())) {
            cardId = user1.getStrCardNumber();
            name = user1.getFullName();
        }
        if (user2 != null && !CommonUtils.isEmpty(user2.getStrCardNumber())) {
            cardId = user2.getStrCardNumber();
        }
        log.setUserName(cardId);
        // Giai ma du lieu neu ma hoa
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
                ConstantsFieldParams.DOCUMENT_ID,
                "receiverDate"
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long documentId = Long.parseLong(listValue.get(0));
            //050517 them ngay nhan van ban
            String receiverDate = listValue.get(1);
            DocumentDAO documentDAO = new DocumentDAO();
            List<EntityDocumentInStaff> result = documentDAO.exportCirculationTree(userId1, userId2,
                    documentId, name, email, receiverDate);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error("exportCirculationTree - Exception - username: "
                    + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Kiem tra van ban co ton tai theo user</b><br/>
     *
     * @author hiendv2
     * @since May 25, 2017
     * @param document_id: Id van ban
     * @param userId: id nguoi tao, nhan vanban
     * @return
     */
    public String getListFileAttachDocByUserId(HttpServletRequest request,
            String data) {
        //Lay thong tin user theo sessionId cua request
        String[] keys = new String[]{"documentId"};
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request,
                data, keys);
        if (userGroup.getCheckSessionOk()) {
            //danh sach cac key de lay du lieu tu client
            List<String> listValue = userGroup.getListParamsFromClient();
            String strDocumentId = listValue.get(0);
            Long documentId = null;
            if (!CommonUtils.isEmpty(strDocumentId)) {
                documentId = Long.parseLong(strDocumentId);
            }
            Long userId1 = userGroup.getUserId1();
            Long userId2 = userGroup.getUserId2();

            //thuc hien lay trang thai chung thu cua nguoi dung bang cach kiem tra database
            DocumentInStaffDAO docInStaffDao = new DocumentInStaffDAO();
            List<EntityFilesAttachment> result = docInStaffDao.
                    getListFileAttachDocByUserId(documentId, userId1, userId2);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS,
                    result, userGroup);
        } else {
            //truong hop bị timeout
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(),
                    null, null);
        }
    }

    /**
     * lay danh sach nguoi nhan van ban
     *
     * @param req
     * @param strData
     * @param isSecurity
     * @return
     */
    public String getListStaffReceiveFromDoc(HttpServletRequest req, String strData,
            String isSecurity) {
        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        DocumentDAO documentDao = new DocumentDAO();
        if (dataSessionGR.getCheckSessionOk()) {
            String cardId = "";
            String strDataClient = "";
            try {
                //lay gia tri tu session
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                // Lay ma nhan vien
                cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);
                //Lay prams client gui len de thuc hien yeu cau
                HashMap<String, Object> hmParams = new HashMap<>();
                hmParams.put(ConstantsFieldParams.TEXT_DOC_ID, Long.class);
                hmParams.put("isGetAll", String.class);
                hmParams.put("listStaff", String.class);
                //La van ban mat
                hmParams.put("isDocSecurity", String.class);
                HashMap<String, Object> valueParams
                        = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
                //lay du lieu
                Long docId = (Long) ((valueParams.get(ConstantsFieldParams.TEXT_DOC_ID) == null
                        || valueParams.get(ConstantsFieldParams.TEXT_DOC_ID).equals(0L)) ? null : valueParams.get(ConstantsFieldParams.TEXT_DOC_ID));
                String strIsGetAll = (String) ((valueParams.get("isGetAll") != null) ? valueParams.get("isGetAll") : "");
                //Loai van ban mat hay thuong
                String isDocSecurity = (String) ((valueParams.get("isDocSecurity") != null) ? valueParams.get("isDocSecurity") : null);
                Boolean isGetAll = false;
                List<EntityVhrEmployee> listStaff = new ArrayList<>();
                if (strIsGetAll != null && "1".equals(strIsGetAll)) {
                    //neu la lay tat ca nguoi nhan van ban
                    isGetAll = true;
                } else {
                    //Lay danh sach nguoi nhan tu client truyen len
                    String strListStaff = (String) ((valueParams.get("listStaff") != null) ? valueParams.get("listStaff") : "");
                    Gson gson = new Gson();
                    Type listEntityVhrEmpType = new TypeToken<ArrayList<EntityVhrEmployee>>() {
                    }.getType();
                    listStaff = gson.fromJson(strListStaff, listEntityVhrEmpType);
                }
                if (docId != null) {
                    List<EntityVhrEmployee> result = documentDao.getListStaffReceiveFromDoc(docId, isGetAll, listStaff, isDocSecurity);
                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                            result, strAesKeyDecode);
                } else {
                    LOGGER.error("getListStaffReceiveFromDoc"
                            + " - docId = null"
                            + " - username: " + cardId + "\ndata: " + strDataClient);
                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA,
                            null, null);
                }
            } catch (JsonSyntaxException ex) {
                LOGGER.error("getListStaffReceiveFromDoc -"
                        + " Exception - username: " + cardId + "\ndata: " + strDataClient, ex);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
        } else {
            LOGGER.error("getListStaffReceiveFromDoc - Session timeout!");
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(),
                    null, null);
        }
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return strResult;
    }

    /**
     * <b>Chia nho van ban</b><br>
     *
     * @param request
     * @param data
     * @return
     */
    public String splitDocument(HttpServletRequest request, String data) {

        String[] keys = new String[]{
            ConstantsFieldParams.DOCUMENT_ID,
            ConstantsFieldParams.LIST_CHILD_DOCUMENT
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request,
                data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("splitDocument - Session timeout");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(),
                    null, null);
        }
        try {
            // Lay du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            // ID van ban
            Long documentId = Long.parseLong(listValue.get(0));
            // Danh sach van ban con duoc chia thu cong
            String strListChildDocument = listValue.get(1);
            List<EntityDocument> listChildDocument = null;
            if (!CommonUtils.isEmpty(strListChildDocument)) {
                Type type = new TypeToken<ArrayList<EntityDocument>>() {
                }.getType();
                Gson gson = new Gson();
                listChildDocument = gson.fromJson(strListChildDocument, type);
            }
            int result = 0;
            DocumentDAO documentDAO = new DocumentDAO();
            if (documentDAO.splitDocument(userGroup.getUserId2(), documentId, listChildDocument)) {
                result = 1;
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS,
                    result, userGroup);
        } catch (Exception ex) {
            LOGGER.error("splitDocument - Exception - username: " + userGroup.getCardId(), ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Lay danh sach y kien chi dao</b><br/>
     *
     * @author hiendv2
     * @param data
     * @since June 8, 2017
     * @param document_id: Id van ban
     * @param userId: id nguoi tao, nhan vanban
     * @return
     */
    public String getListCommentFromDocument(HttpServletRequest request,
            String data) throws Exception {
        //Lay thong tin user theo sessionId cua request
        String[] keys = new String[]{ConstantsFieldParams.DOCUMENT_ID};
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request,
                data, keys);
        if (userGroup.getCheckSessionOk()) {
            //danh sach cac key de lay du lieu tu client
            List<String> listValue = userGroup.getListParamsFromClient();
            String strDocumentId = listValue.get(0);
            Long documentId = null;
            if (!CommonUtils.isEmpty(strDocumentId)) {
                documentId = Long.parseLong(strDocumentId);
            }
            Long userId1 = userGroup.getUserId1();
            Long userId2 = userGroup.getUserId2();

            //thuc hien lay trang thai chung thu cua nguoi dung bang cach kiem tra database
            DocumentDAO docDao = new DocumentDAO();
            List<EntityDocumentInStaff> result = docDao.
                    getListCommentFromDocument(userId1, userId2, documentId, true);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS,
                    result, userGroup);
        } else {
            //truong hop bị timeout
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(),
                    null, null);
        }
    }

    /**
     *
     * @param request
     * @param data
     * @return
     */
    public String getRegisterNumberIndex(HttpServletRequest request, String data) {

        String[] keys = new String[]{
            ConstantsFieldParams.DOCUMENT_ID
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request,
                data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getRegisterNumberIndex - Session timeout");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(),
                    null, null);
        }
        try {
            // Lay du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            // ID van ban
            Long documentId = Long.parseLong(listValue.get(0));

            DocumentDAO documentDAO = new DocumentDAO();
            int result = documentDAO.getRegisterNumberIndex(userGroup.getUserId2(),
                    documentId, null);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS,
                    result, userGroup);
        } catch (Exception ex) {
            LOGGER.error("getRegisterNumberIndex - Exception - username: "
                    + userGroup.getCardId(), ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Bo sung van ban</b><br>
     *
     * @author thanght6
     * @since 12 Jul, 2017
     * @param request
     * @param data
     * @return
     */
    public String extendDocument(HttpServletRequest request, String data) {

        String[] keys = new String[]{
            ConstantsFieldParams.LIST_DOCUMENT_EXTEND
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("extendDocument (Bo sung van ban) - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            String strListDocumentExtend = listValue.get(0);
            if (CommonUtils.isEmpty(strListDocumentExtend)) {
                LOGGER.error("extendDocument (Bo sung van ban) - Loi du lieu dau vao - username: "
                        + userGroup.getCardId());
                return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
            }
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<EntityDocumentExtend>>() {
            }.getType();
            List<EntityDocumentExtend> listDocumentExtend = gson.fromJson(
                    strListDocumentExtend, type);
            int result = 0;
            DocumentDAO dao = new DocumentDAO();
            if (dao.extendDocument(userGroup.getVof2_ItemEntityUser(), listDocumentExtend)) {
                result = 1;
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            LOGGER.error("extendDocument (Bo sung van ban) - Exception - username: "
                    + userGroup.getCardId(), ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    /**
     * <b>Bao cao lich su chuyen van ban</b>
     *
     * @param request
     * @param data
     * @return
     */
    public String reportdocumentTransferHistory(HttpServletRequest request, String data) {

        String[] keys = new String[]{
            ConstantsFieldParams.DOCUMENT_ID
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("reportdocumentTransferHistory - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            Long documentId = Long.parseLong(listValue.get(0));
            DocumentInStaffDAO dao = new DocumentInStaffDAO();
            List<EntityDocumentInStaff> listTransferHistory = dao.getAllReceiver(
                    userGroup.getUserId2(), documentId);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, listTransferHistory, userGroup);
        } catch (Exception ex) {
            LOGGER.error("reportdocumentTransferHistory - Exception - username: "
                    + userGroup.getCardId(), ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    /**
     * <b>Cap nhat trang thai doc van ban</b>
     * Khi user dang cache file van ban nao do, va lai nha duoc van ban do
     * thi client khong tai lai file ma doc file cache len do do trang thai van
     * ban khong thay doi, nen phai goi api de thay doi trang thai
     * 
     * @param request
     * @param data
     * @return 
     */
    public String updateReadingStatus(HttpServletRequest request, String data) {
        
        String[] keys = new String[] {
            ConstantsFieldParams.DOCUMENT_ID
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("updateReadingStatus - Session timeout!");
            return FunctionCommon.responseResult(ErrorCode.SESSION_TIME_OUT, null, null);
        }
        try {
            // Parse du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            Long documentId = Long.parseLong(listValue.get(0));
            DocumentDAO dao = new DocumentDAO();
            int result = dao.updateReadDocument(documentId, userGroup.getUserId1(),
                    userGroup.getUserId2());
            // Index lai trang thai doc van ban cua user
            if (result == 1) {
                IndexDocumentByType indexDocumentByType = new IndexDocumentByType(documentId);
                ThreadPoolCommon.putRunnable(indexDocumentByType);
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception e) {
            LOGGER.error("updateReadingStatus - Exception - username: "
                    + userGroup.getCardId(), e);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    public String getDocumentViewerUserId(HttpServletRequest request, String data) {
        
        List<Long> result = new ArrayList<Long>();
        String[] keys = new String[] {
            ConstantsFieldParams.DOCUMENT_ID
        };
        Long documentId = null;
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("updateReadingStatus - Session timeout!");
            return FunctionCommon.responseResult(ErrorCode.SESSION_TIME_OUT, null, null);
        }
        try {
            // Parse du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            if (!CommonUtils.isEmpty(listValue)) {
            	documentId = Long.parseLong(listValue.get(0));
            }            
            if (documentId != null) {
                DocumentDAO dao = new DocumentDAO();
                result = dao.getDocumentViewerUserId(documentId);
            } else {
                LOGGER.error("getDocumentViewerUserId - Exception - documentId: " + documentId);
                return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
            }            
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception e) {
            LOGGER.error("getDocumentViewerUserId - Exception - documentId: "
                    + documentId, e);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
	
	/**
     * 201901-Pitagon lay danh sach van ban dinh kem lich hop
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getDocumentAttach(HttpServletRequest request,
            String data, String isSecurity) {
        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup entityUserGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!entityUserGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(entityUserGroup.getEnumErrCode(), null, null);
        }

        // Neu user hoac user id tren he thong 1/2 null
        // -> Tra ve thong bao khong co quyen
        EntityUser user1 = entityUserGroup.getItemEntityUser();
        Vof2_EntityUser user2 = entityUserGroup.getVof2_ItemEntityUser();
        if ((user1 == null || user1.getUserId() == null)
                && (user2 == null || user2.getUserId() == null)) {
            LOGGER.error("getDocumentAttach - user hoac userId null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Lay ma nhan vien
        String cardId = entityUserGroup.getVof2_ItemEntityUser().getStrCardNumber();
        log.setUserName(cardId);
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = entityUserGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        //System.out.println("luanvd-getProcessedDetailByUser-data: "+data);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                "docIds", "meetingId"};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String docIds = listValue.get(0);
            String strMeetingId = listValue.get(1);
            if (CommonUtils.isEmpty(strMeetingId)) {
            	return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, null, aesKey);
            }            
            Long meetingId = Long.valueOf(listValue.get(1));
            MeetingDAO meetingDao = new MeetingDAO();
            // Lay thong tin chi tiet lich hop
            EntityMeeting meeting = meetingDao.getMeetingByMeetingId(meetingId);
            if( meeting == null){
            	return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
            if (CommonUtils.isEmpty(strMeetingId)) {
            	docIds = meeting.getDocIds();
            }
            if (CommonUtils.isEmpty(strMeetingId)) {
            	return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, null, aesKey);
            }
            List<Long> listDocId = new ArrayList<>();
            String[] strArr = docIds.split("/");
            for (String str : strArr) {
            	if (!CommonUtils.isEmpty(str)) {
            		listDocId.add(Long.valueOf(str));
            	}
            }
            if (CommonUtils.isEmpty(listDocId)) {
            	return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, null, aesKey);
            }
            // Lay danh sach member trong 
            List<EntityMeetingMember> members = meetingDao.getMemberByMeetingId(meetingId);
            // danh sach member la nhan vien
			List<Long> userIds = new ArrayList<Long>();
			// danh sach member la don vi
			List<Long> orgIds = new ArrayList<Long>();
			if (!CommonUtils.isEmpty(members)) {
				for (EntityMeetingMember member : members) {
					if (member.getType() != null && member.getType().equals(0)) {
						userIds.add(member.getMemberId());
					} else {
						orgIds.add(member.getVhrOrgId());
					}
				}
			}
			// kiem tra quyen view van ban
			boolean permitView;
            if (user2.getUserId().toString().equals(meeting.getCreatedBy())
                    || (!CommonUtils.isEmpty(userIds) && userIds.contains(user2.getUserId()))) {
				permitView = true;
			} else {
				permitView = meetingDao.checkPermitViewDocument(user2.getUserId(), userIds, orgIds,
						meeting.getOrgApprovalId());
			}
			List<Long> permissionIds = new ArrayList<>();
			if (!permitView) {
				permissionIds = meetingDao.checkPermitViewDocument(user2.getUserId(), listDocId);
			}
			
            // Lay thong tin van ban dinh kem
			DocumentInStaffDAO documentInStaffDAO = new DocumentInStaffDAO();
			List<EntityDocument> lstDocAttach = documentInStaffDAO.getDocumentAttach(listDocId);
			if (!CommonUtils.isEmpty(lstDocAttach)) {
				for( EntityDocument doc: lstDocAttach){
					doc.setPermitView(permitView);
					if (permissionIds.contains(doc.getDocumentId())) {
						doc.setPermitView(true);
					}
				}
			}
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, lstDocAttach, aesKey);
        } catch (JSONException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }
    
    /** 
     * 201901-Pitagon: check quyen doc van ban cua lich hop
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
	public String checkPermitViewDocument(HttpServletRequest request, String data, String isSecurity) {
		EntityLog log = new EntityLog(request, CLASS_NAME);
		EntityUserGroup entityUserGroup = FunctionCommon.getStatusSession(request);
		// Khong co session
		if (!entityUserGroup.getCheckSessionOk()) {
			return FunctionCommon.generateResponseJSON(entityUserGroup.getEnumErrCode(), null, null);
		}
		// Neu user hoac user id tren he thong 1/2 null
		// -> Tra ve thong bao khong co quyen
		EntityUser user1 = entityUserGroup.getItemEntityUser();
		Vof2_EntityUser user2 = entityUserGroup.getVof2_ItemEntityUser();
		if ((user1 == null || user1.getUserId() == null) && (user2 == null || user2.getUserId() == null)) {
			LOGGER.error("checkPermitViewDocument - user hoac userId null");
			return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
		}
		// Lay ma nhan vien
		String cardId = entityUserGroup.getVof2_ItemEntityUser().getStrCardNumber();
		log.setUserName(cardId);
		// Giai ma du lieu neu ma hoa
		String aesKey = null;
		if (isSecurity != null && "1".equals(isSecurity)) {
			// Lay AES Key
			aesKey = entityUserGroup.getStrAesKey();
			// Giai ma data client gui len
			data = SecurityControler.decodeDataByAes(aesKey, data);
		}
		// System.out.println("luanvd-getProcessedDetailByUser-data: "+data);
		// Ghi log bat dau chuc nang
		log.setParamList(data);
		LogUtils.logFunctionalStart(log);
		try {
			JSONObject json = new JSONObject(data);
			String[] keys = new String[] { "sysUserId", "userIds", "orgIds", 
					"userCreatedId", "orgApprovalId", "docIds" };
			List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
//			Long sysUserId = Long.valueOf(listValue.get(0));
			Long userCreatedId = Long.valueOf(listValue.get(3));
			Long orgApprovalId = Long.valueOf(listValue.get(4));
			String docIds = listValue.get(5);
			TypeToken<List<Long>> token = new TypeToken<List<Long>>() {
			};
			// danh sach member la nhan vien
			List<Long> userIds = new ArrayList<Long>();
			// danh sach member la don vi
			List<Long> orgIds = new ArrayList<Long>();
			if (!"".equals(listValue.get(1))) {
				userIds = gson.fromJson(listValue.get(1), token.getType());
			}
			if (!"".equals(listValue.get(2))) {
				orgIds = gson.fromJson(listValue.get(2), token.getType());
			}
			if (user2.getUserId().equals(userCreatedId)) {
				return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, true, aesKey);
			}
			if (!CommonUtils.isEmpty(userIds)) {
				for (Long userId : userIds) {
					if (userId.equals(user2.getUserId())) {
						return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, true, aesKey);
					}
				}
			}
			if (CommonUtils.isEmpty(userIds) && CommonUtils.isEmpty(orgIds)) {
				return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, false, aesKey);
			}
			// Check den tro ly lanh dao, lanh dao, thu truong, quan tri lich
			// hop trong don vi
			MeetingDAO meetingDao = new MeetingDAO();
			boolean result = meetingDao.checkPermitViewDocument(user2.getUserId(), userIds, orgIds, 
					orgApprovalId);
			if (!result) {
				List<Long> listDocId = new ArrayList<>();
	            String[] strArr = docIds.split("/");
	            for (String str : strArr) {
	            	if (!CommonUtils.isEmpty(str)) {
	            		listDocId.add(Long.valueOf(str));
	            	}
	            }            
	            if (CommonUtils.isEmpty(listDocId)) {
	            	return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, false, aesKey);
	            }
	            List<Long> ids = meetingDao.checkPermitViewDocument(user2.getUserId(), listDocId);
	            if (!CommonUtils.isEmpty(ids)) {
	            	result = true;
	            }
			}
			LogUtils.logFunctionalEnd(log);
			return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
		} catch (JSONException ex) {
			LOGGER.error(ex.getMessage(), ex);
			return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
		}
	}

	public String checkPermissionDoc(HttpServletRequest request, String data) {
        String[] keys = new String[] {
            ConstantsFieldParams.DOCUMENT_ID
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("checkPermissionDoc - Session timeout!");
            return FunctionCommon.responseResult(ErrorCode.SESSION_TIME_OUT, null, null);
        }
		try {
			List<String> listValue = userGroup.getListParamsFromClient();
			if (CommonUtils.isEmpty(listValue.get(0))) {
				return FunctionCommon.responseResult(ErrorCode.SUCCESS, 0, userGroup);
			}
			Long documentId = Long.valueOf(listValue.get(0));
			
			UserRoleDAO urDao = new UserRoleDAO();
			List<EntityUserRole> userRoles = urDao.getUserByUserId(userGroup.getUserId2());
			List<Long> listSecretary = new ArrayList<>();
			List<Long> listManager = new ArrayList<>();
			if (!CommonUtils.isEmpty(userRoles)) {
				for (EntityUserRole ur : userRoles) {
					if (Constants.SYS_ROLE_VT.equals(ur.getSysRoleId())) {
						listSecretary.add(ur.getOrgId());
					} else if (Constants.SYS_ROLE_TTDV.equals(ur.getSysRoleId()) 
							|| Constants.SYS_ROLE_TL.equals(ur.getSysRoleId())
							|| Constants.SYS_ROLE_LDDV.equals(ur.getSysRoleId())) {
						listManager.add(ur.getOrgId());
					}
				}
			}
			
			EntityDocumentPermission permission = new EntityDocumentPermission();
			DocumentDAO docDao = new DocumentDAO();
			EntityDocument result = docDao.checkPermissionDoc(documentId, userGroup.getUserId1(), userGroup.getUserId2(), listSecretary);
			if (result != null) {
				if (!CommonUtils.isEmpty(listManager)) {
					permission.setPermissionMission(1);
				}
				permission.setPermissionDelete(result.getIsLock());
				permission.setPermissionEdit(result.getIsRead());
			}
			return FunctionCommon.responseResult(ErrorCode.SUCCESS, permission, userGroup);
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
		}
	}
	
	/**
	 * Lay danh sach don vi ngoai tap doan
	 * 
	 * @param request
	 * @param data
	 * @param isSecurity
	 * @return
	 */
	public String getListOfficeOutside(HttpServletRequest request, String data, String isSecurity) {
		EntityLog log = new EntityLog(request, CLASS_NAME);
		EntityUserGroup entityUserGroup = FunctionCommon.getStatusSession(request);
		// Khong co session
		if (!entityUserGroup.getCheckSessionOk()) {
			return FunctionCommon.generateResponseJSON(entityUserGroup.getEnumErrCode(), null, null);
		}
		// Neu user hoac user id tren he thong 1/2 null
		// -> Tra ve thong bao khong co quyen
		EntityUser user1 = entityUserGroup.getItemEntityUser();
		Vof2_EntityUser user2 = entityUserGroup.getVof2_ItemEntityUser();
		if ((user1 == null || user1.getUserId() == null) && (user2 == null || user2.getUserId() == null)) {
			LOGGER.error("getListOfficeOutside - user hoac userId null");
			return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
		}
		// Lay ma nhan vien
		String cardId = entityUserGroup.getVof2_ItemEntityUser().getStrCardNumber();
		log.setUserName(cardId);
		// Giai ma du lieu neu ma hoa
		String aesKey = null;
		if (isSecurity != null && "1".equals(isSecurity)) {
			// Lay AES Key
			aesKey = entityUserGroup.getStrAesKey();
			// Giai ma data client gui len
			data = SecurityControler.decodeDataByAes(aesKey, data);
		}
		// System.out.println("luanvd-getProcessedDetailByUser-data: "+data);
		// Ghi log bat dau chuc nang
		log.setParamList(data);
		LogUtils.logFunctionalStart(log);
		try {
			DocumentDAO docDao = new DocumentDAO();
			List<EntityCommonFields> result = docDao.getListOfficeOutside(user2.getUserId());
			
			LogUtils.logFunctionalEnd(log);
			return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
		}
	}
	
	/**
	 * Lay danh sach but phe cua lanh dao theo id van ban
	 * 
	 * @param request
	 * @param data
	 * @return
	 */
	public String getListCommentLeaderFromDocument(HttpServletRequest request, String data) {
		try {
			EntityLog log = new EntityLog(request, CLASS_NAME);
	        //Lay thong tin user theo sessionId cua request
	        String[] keys = new String[]{ConstantsFieldParams.DOCUMENT_ID};
	        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request,
	                data, keys);
	        if (userGroup.getCheckSessionOk()) {
	        	String cardId = userGroup.getVof2_ItemEntityUser().getStrCardNumber();
	    		log.setUserName(cardId);
	    		LogUtils.logFunctionalStart(log);
	            //danh sach cac key de lay du lieu tu client
	            List<String> listValue = userGroup.getListParamsFromClient();
	            String strDocumentId = listValue.get(0);
	            Long documentId = null;
	            if (!CommonUtils.isEmpty(strDocumentId)) {
	                documentId = Long.parseLong(strDocumentId);
	            }
	            Long userId2 = userGroup.getUserId2();
	
	            //thuc hien lay trang thai chung thu cua nguoi dung bang cach kiem tra database
	            DocumentDAO docDao = new DocumentDAO();
	            List<EntityDocumentInStaff> result = docDao.
	            		getListCommentLeaderFromDocument(userId2, documentId);
	            
	            LogUtils.logFunctionalEnd(log);
	            return FunctionCommon.responseResult(ErrorCode.SUCCESS,
	                    result, userGroup);
	        } else {
	            //truong hop bị timeout
	            return FunctionCommon.responseResult(userGroup.getEnumErrCode(),
	                    null, null);
	        }
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
		}
    }
	/**
	 * @author MinhNQ
	 * rollback dau xac nhan
	 * rollBackDauXacNhan
	 * @param request
	 * @param data
	 * @return
	 */
	public String rollBackDauXacNhan(HttpServletRequest request, String data) {
		String[] keys = new String[] {
				ConstantsFieldParams.DOCUMENT
	        };
			EntityUserGroup entityUserGroup = FunctionCommon.getStatusSession(request);
	        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
	        if (!userGroup.getCheckSessionOk()) {
	            LOGGER.error("checkPermissionDoc - Session timeout!");
	            return FunctionCommon.responseResult(ErrorCode.SESSION_TIME_OUT, null, null);
	        }
	        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
//	        if (user == null || user.getUserId() == null) {
//	            LOGGER.error("cancelPublish - user hoac userId tren he thong 1 null");
//	            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
//	        }
	        Long userId = user == null ? null : user.getUserId();
		try {
			String aesKey = null;
            // Lay AES Key
            aesKey = entityUserGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
			JSONObject json = new JSONObject(data);
	            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
	            String strDocument = listValue.get(0);
	            // Parse chuoi strDocument sang doi tuong van ban
	            Gson gson = new Gson();
	            EntityDocument document = gson.fromJson(strDocument, EntityDocument.class);
	            DocumentDAO docDao = new DocumentDAO();
//	            List<EntityText> text = docDao.getText(document.getDocumentId());
//	            Long textId = text.get(0).getTextId();
	            
	            //lay ra list text_mark cua van ban
	            List<EntityTextMark> lstTextMark = docDao.getListTextMark(document.getDocumentId(),null);
	            List<Long> lstOrgSec = new ArrayList<>();
	            if (null != user) {
	                lstOrgSec = user.getListSecretaryVhrOrg();
	            }
  	            EntityTextMark txtmark = new EntityTextMark();
  	            if(lstTextMark!=null && !CommonUtils.isEmpty(lstOrgSec)){
            	for (EntityTextMark att : lstTextMark){
            		if(lstOrgSec.contains(att.getOrgId())){
            			txtmark = att;
            		}
            		}
  	            }
  	            // lay ra dc các text mark dong dau sau
  	            List<EntityTextMark> lstTextMarkAfter = docDao.getListTextMark(document.getDocumentId(),txtmark.getActionDate());
  	            ArrayList<String> lstorgs = new ArrayList<>();
	            for(EntityTextMark tm: lstTextMarkAfter){
	              lstorgs.add(tm.getOrgId().toString());
	            }
	            List<EntityMarkAttachHistory> lstMAH = docDao.getListMAH(lstorgs,document.getDocumentId());
	            for(EntityMarkAttachHistory mah :lstMAH ){
	            	List<EntityAttach> attachmentXN = docDao.getListFileAttachment(mah.getAttachId());
	            	if(attachmentXN!= null){
	            		String markUploadStorage = attachmentXN.get(0).getStorage();
	                    String markUploadStorageFolder = com.viettel.voffice.utils.FileUtils.getStorageFolder(markUploadStorage);
		            	String pathMark = markUploadStorageFolder+ File.separator + attachmentXN.get(0).getPath();
		            	File tmp = new File(pathMark);
		            	tmp.delete();
		            	docDao.rollbackFileAttachmentXN(attachmentXN.get(0).getFileAttachmentId(), mah.getPathBefore(), mah.getStorageBefore());
		            	List<EntityAttach> attachXN = docDao.getListFileAttach(attachmentXN.get(0).getAttachId());
	  	            	if(attachXN!= null){
				            	docDao.rollbackAttachXN(attachXN.get(0).getAttachId(), mah.getPathBefore(), mah.getStorageBefore());
			            	}
	            	}
	            	docDao.deleteMAH(mah.getId());
	            	docDao.rollbackFileAttachmentXN(mah.getAttachId(), mah.getPathBefore(), mah.getStorageBefore());
	            	docDao.rollbackMarkLocation(mah.getAttachId());
	            	
  	            }
	            docDao.rollBackTextMark(document.getDocumentId(), userId);
	            //Tungh add
	            List<EntityTextMark> lstTextMarkSMS = new ArrayList<EntityTextMark>();
	            //End
	            //roll back dau xac nhan bay dau don vi
	            //check van ban da dong dau don vi
	            List<EntityText>  texts = docDao.getText(document.getDocumentId());
	            if(!CommonUtils.isEmpty(texts)){
	            	EntityText text = texts.get(0);
	            	if(docDao.checkDDDV(text.getTextId(),txtmark.getActionDate())){
	            		TextDAO textDao = new TextDAO();
	      	            List<EntityTextMark> lstTextMarkAfterCurrent = textDao.getListTextMark(text.getTextId(),txtmark.getActionDate(), true);
	      	            ArrayList<String> listorgs = new ArrayList<>();
	      	            if(!CommonUtils.isEmpty(lstTextMarkAfterCurrent)){
	      	                //Tunghd add
	                        lstTextMarkSMS.addAll(lstTextMarkAfterCurrent);
	    	  	          	for(EntityTextMark tm: lstTextMarkAfterCurrent){
	    	  	              listorgs.add(tm.getOrgId().toString());
	    	  	            }
	      	            }
	      	            if(!CommonUtils.isEmpty(listorgs)){
	      	            	List<EntityMarkAttachHistory> listMAH = textDao.getListMAH(text.getTextId());
	    	  	            if(!CommonUtils.isEmpty(listMAH)){
	    		  	            for(EntityMarkAttachHistory mah : listMAH ){
	    		  	            	// xoa storage
	    		  	            	String markUploadStorage = mah.getStorageBefore();
	    		                    String markUploadStorageFolder = com.viettel.voffice.utils.FileUtils.getStorageFolder(markUploadStorage);
	    			            	String pathMark = markUploadStorageFolder+ File.separator + mah.getPathBefore();
	    			            	File tmp = new File(pathMark);
	    			            	tmp.delete();
	    			            	textDao.deleteMAH(mah.getId());
	    		  	            	textDao.rollbackMarkLocation(mah.getAttachId());
	    		  	            	//xoa cung mark attach history
	    		  	            	textDao.deleteMAH(mah.getId());
	    		  	            	//end xoa
	    		  	            }
	    	  	            }
	      	            }
	    	  	        for(EntityTextMark tm: lstTextMarkAfterCurrent){
	    	  	             textDao.rollBackTextMark(tm.getId(),userId);
	    	  	        }
	    	  	        textDao.rollBackText(text.getTextId());
		            }
	            	//Tunghd add Gui SMS
	                if(!CommonUtils.isEmpty(lstTextMarkSMS)){
	                    List<Long> lstOrgsRollBackSMS = new ArrayList<Long>();
	                    for(EntityTextMark sms : lstTextMarkSMS){
	                        if(!lstOrgsRollBackSMS.contains(sms.getOrgId())){
	                            lstOrgsRollBackSMS.add(sms.getOrgId());
	                        }
	                    }
	                    CommonControler smsDAO = new CommonControler();
	                    UserDAO userDao = new UserDAO();
	                    List<EntityVhrEmployee> employees = userDao.getSecretaryListRollback(userId, lstOrgsRollBackSMS);
	                    List<EntityVhrEmployee> employeesTmp = new ArrayList<>();
	                    employeesTmp.addAll(employees);
	                    if(!employees.isEmpty()){
	                        for(EntityVhrEmployee emp : employees){
	                            if(emp.getEmployeeId().equals(userGroup.getUserId2()) || emp.getEmployeeId().equals(userGroup.getUserId1()))
	                                employeesTmp.remove(emp);
	                        }
	                    }
	                    if(!employeesTmp.isEmpty()){
	                        smsDAO.sentSMSRollback(text.getTitle(), userGroup.getUserId2(), Constants.SMS_TEXT_CONFIG.ROLLBACK_MARK_DOC, 101L, employeesTmp, txtmark.getOrgId());
	                    }
	                }
	            }
	            
	            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, true, aesKey);
	        
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
		}
    }
	
			 /**
	     * <b>Tim kiem van ban cho man Ho So Tai Chinh<b></br>
	     * @author Tunghd
	     * @param request Doi tuong request tu client -> server
	     * @param data Gia tri data truyen theo request
	     * @param isSecurity Gia tri isSecurity truyen theo request
	     * @return
		 * @throws Exception 
	     */
	    public String searchFinancial(HttpServletRequest request,
	            String data, String isSecurity) throws Exception {
	        EntityLog log = new EntityLog(request, CLASS_NAME);
	        Date startTime = new Date();
	        String deviceName = null;
	        ErrorCode errorCode;
	        String response;
	        EntityDocument document = null;
	        // Lay thong tin user theo sessionId cua request
	        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
	        if (!userGroup.getCheckSessionOk()) {
	            LOGGER.error("search (Tim kiem cong vang) - Sessing timeout!");
	            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
	        }
	        // Neu user hoac user id tren he thong 1&2 null
	        // -> Tra ve thong bao khong co quyen
	        EntityUser user1 = userGroup.getItemEntityUser();
	        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
	        if (!userGroup.checkUserId()) {
	            LOGGER.error("search (Tim kiem cong van) - user hoac userId tren he"
	                    + " thong 1&2 null!");
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
	        String stringSearch = "";
//	        //Tunghd kiem tra user dang nhap co phai la quan ly Ho so tai chinh or Quan tri he thong pham vi tap doan
//	        UserRoleDAO urDao = new UserRoleDAO();
//	        boolean supManager = false;
//            List<EntityUserRole> userRoles = urDao.getUserByUserId(userGroup.getUserId2());
//            if(!CommonUtils.isEmpty(userRoles)){
//                for(EntityUserRole eu : userRoles){
//                    if(eu.getOrgId().equals(148842L) && 
//                            (eu.getSysRoleId().equals(337551L) || eu.getSysRoleId().equals(336815L))){
//                        supManager = true;
//                    }
//                }
//            }
//            //End
	        try {
	            JSONObject json = new JSONObject(data);
	            String[] keys = new String[]{
	                ConstantsFieldParams.TYPE,
	                ConstantsFieldParams.STATUS,
	                ConstantsFieldParams.ADJACENT_ID,
	                ConstantsFieldParams.KEYWORD,
	                ConstantsFieldParams.DOCUMENT,
	                ConstantsFieldParams.STAFF_IDS,
	                ConstantsFieldParams.START_RECORD,
	                ConstantsFieldParams.PAGE_SIZE,
	                ConstantsFieldParams.IS_SEARCH_DOC_MOBILE,
	                ConstantsFieldParams.IS_FINANCE_TEXT,
	                ConstantsFieldParams.DEVICE_NAME,
	                ConstantsFieldParams.IS_NOT_DUPLICATE_RECEIVED_DOCUMENT,
	                "isVTDocument"
	            };
	            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
	            // Loai
	            Integer type = Integer.parseInt(listValue.get(0));
	            // Trang thai
	            Integer status = Integer.parseInt(listValue.get(1));
	            // Tu khoa
	            String keyword = listValue.get(3);
	            // Chuoi doi tuong van ban
	            String strDocument = listValue.get(4);
	            //Loai van ban lien ke
	            String strAdjacent = listValue.get(2);
	            // Parse chuoi ra doi tuong EntityDocument            
	            if (!CommonUtils.isEmpty(strDocument)) {
	                Gson gson = new Gson();
	                document = gson.fromJson(strDocument, EntityDocument.class);
	            }
	            // Neu keyword khac rong (Tim kiem nhanh)
	            // Gan cac truong mong muon tim kiem theo tu khoa
	            keyword = keyword.replaceAll("“", "\"").replaceAll("”", "\"");
	            if (!CommonUtils.isEmpty(keyword)) {
	                if (document == null) {
	                    document = new EntityDocument();
	                }
	                document.setCode(keyword);
	                document.setTitle(keyword);
	                document.setSigner(keyword);
	                document.setQuickSearch(true);
	                if (CommonUtils.isInteger(keyword)) {
	                    Long documentId = Long.parseLong(keyword);
	                    document.setDocumentId(documentId);
	                }
	            }
	            // Chuoi cac id nguoi gui
	            String staffIds = listValue.get(5);
	            // Vi tri lay ra
	            Long startRecord = 0L;
	            String strStartRecord = listValue.get(6);
	            if (!CommonUtils.isEmpty(strStartRecord)) {
	                startRecord = Long.parseLong(strStartRecord);
	            }
	            Long pageSize = 10L;
	            String strPageSize = listValue.get(7);
	            if (!CommonUtils.isEmpty(strPageSize)) {
	                pageSize = Long.parseLong(strPageSize);
	            }
	            // Tim kiem count theo mobile
	            String isSearchMobile = listValue.get(8);
	            // Tim kiem van ban tai chinh
	            Boolean isFinanceText = false;
	            if (listValue.get(9) != null && "true".equals(listValue.get(9))) {
	                isFinanceText = true;
	            }
	            // Ten thiet bi
	            deviceName = listValue.get(10);
	            

	            // Loc trung van ban nhan duoc
	            Integer isNotDuplicateReceivedDocument = null;
	            String strIsNotDuplicateReceivedDocument = listValue.get(11);
	            if (!CommonUtils.isEmpty(strIsNotDuplicateReceivedDocument)) {
	                isNotDuplicateReceivedDocument = Integer.parseInt(strIsNotDuplicateReceivedDocument);
	            }
	            // null: Tim tat ca
	            // 0: Tim van ban ngoai tap doan
	            // 1: Tim van ban trong tap doan
	            Integer isVTDocument = null;
	            if (!CommonUtils.isEmpty(listValue.get(12))) {
	                isVTDocument = Integer.parseInt(listValue.get(12));
	            }
	            // Van ban lien ke
	            Integer adjacent = 0;
	            if (!CommonUtils.isEmpty(strAdjacent)) {
	                adjacent = Integer.parseInt(strAdjacent);
	            }

	            // Truy van database
	            List<Long> lsSecretaryOrgIdVof2 = null;
	            if (user2 != null) {
	                lsSecretaryOrgIdVof2 = user2.getListSecretaryVhrOrg();
	            }
	            DocumentDAO documentDAO = new DocumentDAO();

	            List<EntityDocument> result;
//	            System.out.println("======:" + isSearchMobile);
	            if (((!CommonUtils.isEmpty(isSearchMobile) && "1".equals(isSearchMobile)) || strDocument.trim().length() <= 3) && !CommonUtils.isEmpty(keyword) && (type == null || type.intValue() != Constants.Document.Type.CREATOR)) {
	                ElasticSearchDocument elasticSearchDocument = new ElasticSearchDocument();
	                result = elasticSearchDocument.getListSearchDocument(userId1,
	                        userId2, user2, type, status,
	                        adjacent, document, staffIds, startRecord, pageSize);
	                stringSearch = "ELASTICSERCH";
	            } else {
	                result = documentDAO.searchFinancial(userId1, userId2, user2,
	                        type, status, adjacent, document, staffIds, startRecord, pageSize,
	                        isSearchMobile, isFinanceText, lsSecretaryOrgIdVof2,
	                        isNotDuplicateReceivedDocument, isVTDocument);
	                stringSearch = "DATABASESERCH";
	            }
	            // Neu tim kiem co ket qua
	            // -> Lay danh sach file dinh kem cho tung van ban tim kiem duoc            
	            if (!CommonUtils.isEmpty(result)) {
	                FilesAttachmentDAO filesAttachmentDAO = new FilesAttachmentDAO();
	                List<Long> listDocumentId = new ArrayList<>();
	                for (EntityDocument doc : result) {
	                    listDocumentId.add(doc.getDocumentId());
	                }

	                // Lay danh sach file dinh kem cua danh sach van ban
	                List<EntityFilesAttachment> listAttachment = filesAttachmentDAO
	                        .getListAttachment(userId1, listDocumentId);
	                // Gan danh sach file kem vao tung van ban tuong ung
	                if (!CommonUtils.isEmpty(listAttachment)) {
	                    List<EntityFilesAttachment> listAttachmentForDocument;
	                    for (EntityDocument doc : result) {
	                        listAttachmentForDocument = new ArrayList<>();
	                        for (EntityFilesAttachment attachment : listAttachment) {
	                            if (attachment.getDocumentId().equals(doc.getDocumentId())) {
	                                listAttachmentForDocument.add(attachment);
	                            }
	                        }
	                        doc.setListAttachment(listAttachmentForDocument);
	                    }
	                }
	                // Lay danh sach file bieu mau cua danh sach van ban
	                AttachDAO attachDAO = new AttachDAO();
	                List<EntityFileAttachment> listAttachTemplate = attachDAO
	                        .getListAttachTemplate(userId2, listDocumentId, false);
	                // Khoi tao danh sach file bieu mau cho tung van ban
	                if (!CommonUtils.isEmpty(listAttachTemplate)) {
	                    List<EntityFileAttachment> listAttachTemplateForDocument;
	                    for (EntityDocument doc : result) {
	                        listAttachTemplateForDocument = new ArrayList<>();
	                        for (EntityFileAttachment attachTemplate : listAttachTemplate) {
	                            if (doc.getDocumentId().equals(attachTemplate.getDocumentId())) {
	                                listAttachTemplateForDocument.add(attachTemplate);
	                            }
	                        }
	                        doc.setListAttachTemplate(listAttachTemplateForDocument);
	                    }
	                }
	            }
	            errorCode = ErrorCode.SUCCESS;
	            response = FunctionCommon.generateResponseJSON(errorCode, result, aesKey);
	        } // Loi server
	        catch (JSONException | NumberFormatException | JsonSyntaxException ex) {
	            LOGGER.error("search (Tim kiem cong van) - Exception - username: "
	                    + cardId + "\ndata: " + data, ex);
	            errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
	            response = FunctionCommon.generateResponseJSON(errorCode, null, null);
	        }
	        String functionName = "DocumentAction.search";
	        String description = "SERVICE2: Lay danh sach van ban - errorCode: "
	                + errorCode.getErrorCode() + ", TIMKIEM: "+ stringSearch;
	        String content = "";
	        EntityActionLogMobile logDB = new EntityActionLogMobile(userId1, cardId,
	                startTime, new Date(), functionName, description, content, deviceName, document);
	        LogUtils.insertActionLogMobile(userId2, logDB);
	        // Ghi log ket thuc chuc nang
	        LogUtils.logFunctionalEnd(log);
	        return response;
	    }
	    
	    /**
	     * Tunghd add
	     * Unfollow danh sach ho so tai chinh
	     * @param request
	     * @param data
	     * @param isSecurity
	     * @return
	     */
	    public String unfollowDocument(HttpServletRequest request,
	            String data, String isSecurity) {

	        EntityLog log = new EntityLog(request, CLASS_NAME);
	        // Lay thong tin user theo sessionId cua request
	        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
	        // Khong co session
	        if (!userGroup.getCheckSessionOk()) {
	            LOGGER.error("Unfollows Ho So tai chinh - Session timeout!");
	            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
	        }
	        // Neu user hoac user id tren he thong 1&2 null
	        // -> Tra ve thong bao khong co quyen
	        EntityUser user1 = userGroup.getItemEntityUser();
	        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
	        if ((user1 == null || user1.getUserId() == null)
	                && (user2 == null || user2.getUserId() == null)) {
	            LOGGER.error("Unfollows Ho So tai chinh - user hoac userId tren he thong 1&2 null");
	            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
	        }
	        // Neu khong co thong tin tren he thong 1
	        // -> Gan userId1 = 0
	        Long userId1 = (user1 == null || user1.getUserId() == null) ? 0L : user1.getUserId();
	        // Neu khong co thong tin tren he thong 2
	        // -> Gan userId2 = 0
	        Long userId2 = (user2 == null || user2.getUserId() == null) ? 0L : user2.getUserId();
	        // Lay ma nhan vien
	        String cardId = "";
	        if (user1 != null && !CommonUtils.isEmpty(user1.getStrCardNumber())) {
	            cardId = user1.getStrCardNumber();
	        } else if (user2 != null && !CommonUtils.isEmpty(user2.getStrCardNumber())) {
	            cardId = user2.getStrCardNumber();
	        }
	        log.setUserName(cardId);
	        // Giai ma du lieu neu ma hoa
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
	                ConstantsFieldParams.DOCUMENT_ID
	            };
	            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
	            Long documentId = Long.parseLong(listValue.get(0));
	            DocumentDAO documentDAO = new DocumentDAO();
	            int result = documentDAO.unfollowDocument(userId1, userId2, documentId);
	            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
	        } catch (Exception ex) {
	            LOGGER.error("Unfollows Ho So tai chinh (Bo theo doi 1 van ban man HO So Tai chinh) - Exception - username: "
	                    + cardId + "\ndata: " + data, ex);
	            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
	                    null, null);
	        }
	    }
	    
    public String getDocumentStaffEntity(String isSecurity, String data,
            HttpServletRequest request) throws JSONException {
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("Unfollows Ho So tai chinh - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        // Neu user hoac user id tren he thong 1&2 null
        // -> Tra ve thong bao khong co quyen
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        String[] keys = new String[]{
                "documentId"
                };
        try {
            JSONObject json = new JSONObject(data);
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long documentId = null;
            if (!CommonUtils.isEmpty(listValue.get(0))) {
                documentId = Long.parseLong(listValue.get(0));
            }
            DocumentDAO documentDAO = new DocumentDAO();
            Object result = documentDAO.getDocumentStaffEntity(documentId, user2.getUserId());
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (NumberFormatException ex) {
            LOGGER.error("getDocumentStaffEntity - Exception:", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
}