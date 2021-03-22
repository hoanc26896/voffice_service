/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.briefmanagement.BriefManagementDAO;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.EntityVhrOrg;
import com.viettel.voffice.database.entity.task.EntityBrief;
import com.viettel.voffice.database.entity.task.EntityBriefDocument;
import com.viettel.voffice.database.entity.task.EntityBriefFilesAttachment;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.DateUtils;
import com.viettel.voffice.utils.LogUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author pm1_os20
 */
public class BriefManagementController {

    /**
     * Log loi
     */
    private static final Logger logger = Logger.getLogger(BriefManagementController.class);

    private static final int RETURN_TRUE = 1;


    /*Lay danh sach ho so*/
    public String getListBrief(String isSecurity, String strData, HttpServletRequest req) {
        String[] keys;
        keys = new String[]{
            ConstantsFieldParams.STR_KEYWORDS,
            ConstantsFieldParams.START_RECORD,
            ConstantsFieldParams.PAGE_SIZE,
            ConstantsFieldParams.IS_COUNT,
            ConstantsFieldParams.IS_ADVANCE,
            ConstantsFieldParams.NAME,
            ConstantsFieldParams.BRIEF_AUTHOR,
            ConstantsFieldParams.PATH_NAME,
            ConstantsFieldParams.STATUS,
            ConstantsFieldParams.START_TIME,
            ConstantsFieldParams.END_TIME,
            ConstantsFieldParams.TIME_STORAGE_TYPE,
            ConstantsFieldParams.TIME_STORAGE,
            ConstantsFieldParams.CODE,
            ConstantsFieldParams.STORAGE_ID_LIST,
            ConstantsFieldParams.STYPE,
            ConstantsFieldParams.BOX_ID,
            ConstantsFieldParams.BRIEF_ID,
        };
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(req, strData, keys);

        // Session timeout
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("getListBrief - Session timeout!");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        try {
            //lay gia tri luu trong seasion
            Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
            List<Long> orgIdsByRole = new ArrayList<>();

            // Lay danh sach don vi sach don vi user co quyen van thu
            List<Long> listSecretaryVhrOrg = dataSessionGR.getVof2_ItemEntityUser().getListSecretaryVhrOrg();
            if (listSecretaryVhrOrg != null && listSecretaryVhrOrg.size() > 0) {
                orgIdsByRole.addAll(listSecretaryVhrOrg);
            }

            List<String> listValue = dataSessionGR.getListParamsFromClient();

            String strSearch = listValue.get(0);
            Long startRecord = 0L;
            if (!CommonUtils.isEmpty(listValue.get(1))) {
                startRecord = Long.parseLong(listValue.get(1));
            }
            Long pageSize = null;
            if (!CommonUtils.isEmpty(listValue.get(2))) {
                pageSize = Long.parseLong(listValue.get(2));
            }
            Integer isCount = 0;
            if (!CommonUtils.isEmpty(listValue.get(3))) {
                isCount = Integer.valueOf(listValue.get(3));
            }
            Integer isAdvance = 0;
            if (!CommonUtils.isEmpty(listValue.get(4))) {
                isAdvance = Integer.valueOf(listValue.get(4));
            }
            String name = listValue.get(5);
            String author = listValue.get(6);
            String orgName = listValue.get(7);            
            Long status = 0L;
            if (!CommonUtils.isEmpty(listValue.get(8))) {
                status = Long.parseLong(listValue.get(8));
            }
            String startTime = listValue.get(9);
            String endTime = listValue.get(10);
            Long timeStorageType = 0L;
            if (!CommonUtils.isEmpty(listValue.get(11))) {
                timeStorageType = Long.parseLong(listValue.get(11));
            }
            String timeStorage = listValue.get(12);
            String code = listValue.get(13);            
            String storageIdList = listValue.get(14);

            Long stype = 0L;
            if (!CommonUtils.isEmpty(listValue.get(15))) {
                stype = Long.parseLong(listValue.get(15));
            }
            Long boxId = 0L;
            if (!CommonUtils.isEmpty(listValue.get(16))) {
                boxId = Long.parseLong(listValue.get(16));
            }
            Long briefId = 0L;
            if (!CommonUtils.isEmpty(listValue.get(17))) {
                briefId = Long.parseLong(listValue.get(17));
            }
            BriefManagementDAO brief = new BriefManagementDAO();
            Object result = brief.getBrief(userId, orgIdsByRole, strSearch, startRecord, pageSize, isCount, isAdvance, name, author, orgName, status, startTime,
                    endTime, timeStorageType, timeStorage, code, storageIdList, stype, boxId, briefId);

            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
        } catch (NumberFormatException ex) {
            logger.error("getListBrief - Exception:", ex);
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
    }

    /**
     * <b>Xoa ho so</b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String deleteBriefs(HttpServletRequest request,
            String data, String isSecurity) {
        String[] keys = new String[]{ConstantsFieldParams.BRIEF_ID};
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);
        // Khong co session
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("deleteBrief - No session");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            logger.error("deleteBrief - userId2 null");
            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
        }
        try {
            List<String> listValue = dataSessionGR.getListParamsFromClient();
            Long briefId = Long.parseLong(listValue.get(0));
            BriefManagementDAO m = new BriefManagementDAO();
            Integer result = m.deleteBriefs(briefId, userId);

            if (RETURN_TRUE == result) {
                return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
            }
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);

        } catch (Exception ex) {
            logger.error("deleteBrief - Exception:", ex);
            return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * add Or Edit ho so
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String addOrEditBrief(HttpServletRequest request,
            String data, String isSecurity) {

        String[] keys = new String[]{
            ConstantsFieldParams.PARAM_BRIEF,
            ConstantsFieldParams.PARAM_BRIEF_DOC,
            ConstantsFieldParams.PARAM_BRIEF_DOC_DELETE};

        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);
        // Khong co session
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("addOrEditBrief - No session");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
        String userCode = dataSessionGR.getVof2_ItemEntityUser().getStrCardNumber();
        if (userId == null) {
            logger.error("addOrEditBrief - userId null");
            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
        }
        try {
            List<String> listObject = dataSessionGR.getListParamsFromClient();
            String briefStr = listObject.get(0);
            String briefDocStr = listObject.get(1);
            String briefDocStrDelete = listObject.get(2);

            if (!CommonUtils.isEmpty(briefStr)) {
                keys = new String[]{
                    ConstantsFieldParams.BRIEF_ID,
                    ConstantsFieldParams.NAME,
                    ConstantsFieldParams.BRIEF_DESCRIPTION,
                    ConstantsFieldParams.BRIEF_REGISTER_NUMBER,
                    ConstantsFieldParams.BRIEF_CODE,
                    ConstantsFieldParams.BRIEF_ORG_ID,
                    ConstantsFieldParams.BRIEF_ORG_NAME,
                    ConstantsFieldParams.BRIEF_STYPE,
                    ConstantsFieldParams.STORAGE_ID,
                    ConstantsFieldParams.SHELVE_ID,
                    ConstantsFieldParams.BOX_ID,
                    ConstantsFieldParams.FLOOR_ID,
                    ConstantsFieldParams.BRIEF_NUM_PAPER,
                    ConstantsFieldParams.BRIEF_FIELD_ID,
                    ConstantsFieldParams.BRIEF_COMMENT,
                    ConstantsFieldParams.BRIEF_START_TIME,
                    ConstantsFieldParams.BRIEF_END_TIME,
                    ConstantsFieldParams.BRIEF_STATUS,
                    ConstantsFieldParams.TIME_STORAGE_TYPE,
                    ConstantsFieldParams.TIME_STORAGE,
                    ConstantsFieldParams.BRIEF_LANGUAGE,
                    ConstantsFieldParams.BRIEF_AUTHOR};

                JSONObject json = new JSONObject(briefStr);

                List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);

                Long briefId = null;
                Long shelveId = null;
                Long floorId = null;
                Long storageId = null;
                Long boxId = null;
                Long orgId = null;
                Integer stype = null;
                Integer numPaper = null;
                Integer fieldId = null;
                Integer status = null;
                Integer timeStorageType = null;
                Integer language = 0;

                try {
                    if (!CommonUtils.isEmpty(listValue.get(0))) {
                        briefId = Long.parseLong(listValue.get(0));
                    }
                    if (!CommonUtils.isEmpty(listValue.get(5))) {
                        orgId = Long.parseLong(listValue.get(5));
                    }
                    if (!CommonUtils.isEmpty(listValue.get(7))) {
                        stype = Integer.valueOf(listValue.get(7));
                    }
                    if (!CommonUtils.isEmpty(listValue.get(8))) {
                        storageId = Long.parseLong(listValue.get(8));
                    }
                    if (!CommonUtils.isEmpty(listValue.get(9))) {
                        shelveId = Long.parseLong(listValue.get(9));
                    }
                    if (!CommonUtils.isEmpty(listValue.get(10))) {
                        boxId = Long.parseLong(listValue.get(10));
                    }
                    if (!CommonUtils.isEmpty(listValue.get(11))) {
                        floorId = Long.parseLong(listValue.get(11));
                    }
                    if (!CommonUtils.isEmpty(listValue.get(12))) {
                        numPaper = Integer.valueOf(listValue.get(12));
                    }
                    if (!CommonUtils.isEmpty(listValue.get(13))) {
                        fieldId = Integer.valueOf(listValue.get(13));
                    }
                    if (!CommonUtils.isEmpty(listValue.get(17))) {
                        status = Integer.valueOf(listValue.get(17));
                    }
                    if (!CommonUtils.isEmpty(listValue.get(18))) {
                        timeStorageType = Integer.valueOf(listValue.get(18));
                    }
                    if (!CommonUtils.isEmpty(listValue.get(20))) {
                        language = Integer.valueOf(listValue.get(20));
                    }
                } catch (NumberFormatException ex) {
                    logger.error("addOrEditBrief - Exception:", ex);
                }

                String name = listValue.get(1);
                String description = listValue.get(2);
                String registerNumber = listValue.get(3);
                String code = listValue.get(4);
                String orgName = listValue.get(6);
                String comment = listValue.get(14);

                String startTime = null;
                String endTime = null;
                String timeStorage = null;
                if (!CommonUtils.isEmpty(listValue.get(15))) {
                    startTime = DateUtils.formatddMMyyyyHHmmssString(listValue.get(15));
                }
                if (!CommonUtils.isEmpty(listValue.get(16))) {
                    endTime = DateUtils.formatddMMyyyyHHmmssString(listValue.get(16));
                }
                if (!CommonUtils.isEmpty(listValue.get(19))) {
                    timeStorage = DateUtils.formatddMMyyyyHHmmssString(listValue.get(19));
                }

                String author = listValue.get(21);

                BriefManagementDAO brief = new BriefManagementDAO();
                Integer rs = 0;
                if (briefId == null) {
                    //Them moi
                    briefId = brief.getBriefId();
                    if (briefId != null) {
                        rs = brief.addBrief(briefId, name, description, registerNumber, code, orgId, orgName, stype, storageId, shelveId, boxId, floorId, numPaper, fieldId, comment, startTime, endTime, status, timeStorageType, timeStorage, language, author, userId);
                    }
                } else {
                    //update
                    rs = brief.editBrief(briefId, name, description, registerNumber, code, orgId, orgName, stype, storageId, shelveId, boxId, floorId, numPaper, fieldId, comment, startTime, endTime, status, timeStorageType, timeStorage, language, author, userId);
                }

                if (RETURN_TRUE == rs) {
                    Gson gson = new Gson();

                    if (!CommonUtils.isEmpty(briefDocStr)) {
                        JSONArray arrJsonDoc = new JSONArray(briefDocStr);

                        if (arrJsonDoc.length() > 0) {
                            for (int i = 0; i < arrJsonDoc.length(); i++) {
                                JSONObject job = arrJsonDoc.getJSONObject(i);
                                Type typeData = new TypeToken<EntityBriefDocument>() {
                                }.getType();
                                EntityBriefDocument briefDocument = gson.fromJson(String.valueOf(job), typeData);
                                if (briefDocument != null) {
                                    //format date
                                    if (!CommonUtils.isEmpty(briefDocument.getPromulgateDate())) {
                                        briefDocument.setPromulgateDate(DateUtils.formatddMMyyyyString(briefDocument.getPromulgateDate()));
                                    }
                                    if (!CommonUtils.isEmpty(briefDocument.getReceiveDate())) {
                                        briefDocument.setReceiveDate(DateUtils.formatddMMyyyyString(briefDocument.getReceiveDate()));
                                    }
                                    if (!CommonUtils.isEmpty(briefDocument.getDeadlineDate())) {
                                        briefDocument.setDeadlineDate(DateUtils.formatddMMyyyyString(briefDocument.getDeadlineDate()));
                                    }
                                    //update BriefDocument
                                    if (briefDocument.getBriefDocumentId() != null) {
                                        brief.updateBriefDocument(briefDocument, userId, userCode);
                                    } else {
                                        // Coppy van ban trong bang document
                                        if (briefDocument.getDocumentId() != null) {
                                            Long documentId = briefDocument.getDocumentId();
                                            Long briefDocumentId = brief.addBriefDocumentByDocument(documentId, briefDocument, briefId, userId, userCode);
                                            // copy file attachment
                                            if (briefDocumentId != null && briefDocumentId > 0) {
                                                brief.copyFileAttachment(documentId, briefDocumentId, userId);
                                            }
                                        } else {
                                            //Them moi van ban  
                                            briefDocument.setIsLock(1);
                                            briefDocument.setStatusNumber(0);
                                            briefDocument.setIsFromCoporation(1);
                                            briefDocument.setCreatorId2(userId);
                                            briefDocument.setCreatorGroupId2(dataSessionGR.getVof2_ItemEntityUser().getSysOrgId());
                                            brief.insertBriefDocument(briefDocument, briefId, userId, userCode);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    //Delete BriefDocument
                    if (!CommonUtils.isEmpty(briefDocStrDelete)) {
                        JSONArray arrJsonDocDelete = new JSONArray(briefDocStrDelete);
                        if (arrJsonDocDelete.length() > 0) {
                            List<Long> listDocId = new ArrayList<>();
                            Long docId;
                            for (int i = 0; i < arrJsonDocDelete.length(); i++) {
                                if (arrJsonDocDelete.get(i) != null) {
                                    docId = arrJsonDocDelete.getLong(i);
                                    listDocId.add(docId);
                                }
                            }
                            brief.deleteBriefDocument(listDocId, userId);
                        }
                    }
                }

                return FunctionCommon.responseResult(ErrorCode.SUCCESS, rs, dataSessionGR);
            } else {
                return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
            }

        } catch (Exception ex) {
            logger.error("addOrEditBrief - Exception:", ex);
            return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * getListBriefInfo
     *
     * @param request
     * @param isSecurity
     * @param data
     * @return
     */
    public String getListBriefInfo(HttpServletRequest request,
            String data, String isSecurity) {
        String[] keys = new String[]{
            ConstantsFieldParams.START_RECORD,
            ConstantsFieldParams.PAGE_SIZE,
            ConstantsFieldParams.IS_COUNT,
            ConstantsFieldParams.TYPE,
            ConstantsFieldParams.BRIEFID,
            ConstantsFieldParams.BRIEF_DATA_SEARCH
        };
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);

        // Session timeout
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("getListBriefInfo - Session timeout!");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        try {
            //lay gia tri luu trong seasion
            Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
            List<Long> orgIdsByRole = new ArrayList<>();

            // Lay danh sach don vi sach don vi user co quyen van thu
            List<Long> listSecretaryVhrOrg = dataSessionGR.getVof2_ItemEntityUser().getListSecretaryVhrOrg();
            if (listSecretaryVhrOrg != null && listSecretaryVhrOrg.size() > 0) {
                orgIdsByRole.addAll(listSecretaryVhrOrg);
            }

            List<String> listValue = dataSessionGR.getListParamsFromClient();
            Long type = 0L;
            Long briefId = null;
            Long startRecord = 0L;
            if (!CommonUtils.isEmpty(listValue.get(0))) {
                startRecord = Long.parseLong(listValue.get(0));
            }
            Long pageSize = null;
            if (!CommonUtils.isEmpty(listValue.get(1))) {
                pageSize = Long.parseLong(listValue.get(1));
            }
            Integer isCount = 0;
            if (!CommonUtils.isEmpty(listValue.get(2))) {
                isCount = Integer.valueOf(listValue.get(2));
            }
            if (!CommonUtils.isEmpty(listValue.get(3))) {
                type = Long.parseLong(listValue.get(3));
            }
            if (!CommonUtils.isEmpty(listValue.get(4))) {
                briefId = Long.parseLong(listValue.get(4));
            }
            String dataSearch = listValue.get(5);
            BriefManagementDAO brief = new BriefManagementDAO();
            Object result = brief.getListBriefInfo(userId, startRecord, pageSize, isCount, type, briefId, orgIdsByRole, dataSearch);

            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
        } catch (NumberFormatException ex) {
            logger.error("getListBriefInfo - Exception:", ex);
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
    }

    /**
     * processBrief
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws org.json.JSONException
     */
    public String processBrief(HttpServletRequest request, String data, String isSecurity) throws JSONException {
        String[] keys = new String[]{
            ConstantsFieldParams.EMPLOYEE_ID,
            ConstantsFieldParams.RECEIVER_ID,
            ConstantsFieldParams.BRIEF_CONTENT,
            ConstantsFieldParams.BRIEF_ID,
            ConstantsFieldParams.TYPE
        };
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);
        // Khong co session
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("processBrief - No session");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            logger.error("processBrief - userId2 null");
            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
        }
        try {
            List<String> listValue = dataSessionGR.getListParamsFromClient();

            String employeeIdStr = listValue.get(0);
            Long receiverId = 0L;           
            if (!CommonUtils.isEmpty(listValue.get(1))) {
                receiverId = Long.parseLong(listValue.get(1));
            }
           String content = listValue.get(2);  
           String briefIdStr = listValue.get(3);
           Long type = 0L;
           if (!CommonUtils.isEmpty(listValue.get(4))) {
                type = Long.parseLong(listValue.get(4));
            }
           List<Long> listBriefId = new ArrayList<>();

            if (!CommonUtils.isEmpty(briefIdStr)) {
                JSONArray arrJson = new JSONArray(briefIdStr);
                if (arrJson.length() > 0) {                    
                    Long briefId;
                    for (int i = 0; i < arrJson.length(); i++) {
                        if (arrJson.get(i) != null) {
                            briefId = arrJson.getLong(i);
                            listBriefId.add(briefId);
                        }
                    }                   
                }
            }
            
            List<Long> listEmployeeId = new ArrayList<>();

            if (!CommonUtils.isEmpty(employeeIdStr)) {
                JSONArray arrJson = new JSONArray(employeeIdStr);
                if (arrJson.length() > 0) {                    
                    Long employeeId;
                    for (int i = 0; i < arrJson.length(); i++) {
                        if (arrJson.get(i) != null) {
                            employeeId = arrJson.getLong(i);
                            listEmployeeId.add(employeeId);
                        }
                    }                   
                }
            }
            
            BriefManagementDAO m = new BriefManagementDAO();
            
            Integer result = m.processBriefs(listBriefId, userId, listEmployeeId, receiverId, content, type);

            if (RETURN_TRUE == result) {
                return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
            }
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);

        } catch (NumberFormatException ex) {
            logger.error("processBrief - Exception:", ex);
            return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
        }
    }
    
    /**
     * processBriefBorrow
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws org.json.JSONException
     */
    public String processBriefBorrow(HttpServletRequest request, String data, String isSecurity) throws JSONException {
        String[] keys = new String[]{
            ConstantsFieldParams.BRIEF_ID,
            ConstantsFieldParams.BRIEF_BORROW_ID,
            ConstantsFieldParams.STATUS,
            ConstantsFieldParams.EMPLOYEE_ID,           
            ConstantsFieldParams.BRIEF_CONTENT,
            ConstantsFieldParams.PROCESS_TYPE,
            ConstantsFieldParams.TYPE,
            ConstantsFieldParams.TIME_COMPLETED
        };
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);
        // Khong co session
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("processBriefBorrow - No session");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            logger.error("processBriefBorrow - userId2 null");
            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
        }
        try {
            List<String> listValue = dataSessionGR.getListParamsFromClient();
           
            Long briefId = 0L;
            Long briefBorrowId = 0L;
            Integer status = 0;
            Long employeeId = 0L;
            String content = listValue.get(4);
            Long processType = 0L;
            Long type = 0L;
            String timeCompleted = null;
            if (!CommonUtils.isEmpty(listValue.get(0))) {
                briefId = Long.parseLong(listValue.get(0));
            }
            if (!CommonUtils.isEmpty(listValue.get(1))) {
                briefBorrowId = Long.parseLong(listValue.get(1));
            }
            if (!CommonUtils.isEmpty(listValue.get(2))) {
                status = Integer.valueOf(listValue.get(2));
            }
            if (!CommonUtils.isEmpty(listValue.get(3))) {
                employeeId = Long.parseLong(listValue.get(3));
            }
            if (!CommonUtils.isEmpty(listValue.get(5))) {
                processType = Long.parseLong(listValue.get(5));
            }
            if (!CommonUtils.isEmpty(listValue.get(6))) {
                type = Long.parseLong(listValue.get(6));
            }
            if (!CommonUtils.isEmpty(listValue.get(7))) {
                timeCompleted = listValue.get(7);
            }
 
            BriefManagementDAO m = new BriefManagementDAO();
            
            Integer result = m.processBriefBorrow(briefId, briefBorrowId, userId, status, employeeId, userId, content, processType, type, timeCompleted);

            if (RETURN_TRUE == result) {
                return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
            }
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);

        } catch (NumberFormatException ex) {
            logger.error("processBriefBorrow - Exception:", ex);
            return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * getBriefForUpdate
     *
     * @param request
     * @param isSecurity
     * @param data
     * @return
     */
    public String getBriefForUpdate(HttpServletRequest request,
            String data, String isSecurity) {
        String[] keys = new String[]{
            ConstantsFieldParams.BRIEFID
        };
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);

        // Session timeout
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("getBriefForUpdate - Session timeout!");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        try {
            List<String> listValue = dataSessionGR.getListParamsFromClient();

            Long briefId = null;

            if (!CommonUtils.isEmpty(listValue.get(0))) {
                briefId = Long.parseLong(listValue.get(0));
            }

            BriefManagementDAO brief = new BriefManagementDAO();
            EntityBrief result = brief.getBriefForUpdate(briefId);
            if (result != null) {
                List<EntityBriefDocument> listDoc = brief.getLisBriefDocument(briefId);
                result.setBriefDocumentList(listDoc);
            }

            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
        } catch (NumberFormatException ex) {
            logger.error("getBriefForUpdate - Exception:", ex);
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
    }

    /**
     * briefBorrow
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String briefBorrow(HttpServletRequest request,
            String data, String isSecurity) {

        String[] keys = new String[]{
            ConstantsFieldParams.BRIEF_ID,
            ConstantsFieldParams.EMPLOYEE_ID,
            ConstantsFieldParams.RECEIVER_ID,
            ConstantsFieldParams.BORROW_STATUS,
            ConstantsFieldParams.REASON,
            ConstantsFieldParams.TIME_COMPLETED,
            ConstantsFieldParams.HARD_TYPE,
            ConstantsFieldParams.SOFT_TYPE,
            ConstantsFieldParams.BRIEF_NAME,};

        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);
        // Khong co session
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("briefBorrow - No session");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            logger.error("briefBorrow - userId null");
            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
        }
        try {
            List<String> listValue = dataSessionGR.getListParamsFromClient();
            Long briefId = null;
            Long employeeId = null;
            Long receiveId = null;
            Long borrowStatus = null;
            String reason = "";
            String timeCompleted = "";
            boolean hardType = true;
            boolean softType = true;

            try {
                if (!CommonUtils.isEmpty(listValue.get(0))) {
                    briefId = Long.parseLong(listValue.get(0));
                }
                if (!CommonUtils.isEmpty(listValue.get(1))) {
                    employeeId = Long.parseLong(listValue.get(1));
                }
                if (!CommonUtils.isEmpty(listValue.get(2))) {
                    receiveId = Long.parseLong(listValue.get(2));
                }
                if (!CommonUtils.isEmpty(listValue.get(3))) {
                    borrowStatus = Long.parseLong(listValue.get(3));
                }
                if (!CommonUtils.isEmpty(listValue.get(4))) {
                    reason = listValue.get(4);
                }
                
                timeCompleted = listValue.get(5);

                if (!CommonUtils.isEmpty(listValue.get(6))) {
                    hardType = Boolean.parseBoolean(listValue.get(6));
                }
                if (!CommonUtils.isEmpty(listValue.get(7))) {
                    softType = Boolean.parseBoolean(listValue.get(7));
                }

            } catch (NumberFormatException ex) {
                logger.error("briefBorrow - Exception:", ex);
            }
            String briefName =listValue.get(8);
            BriefManagementDAO brief = new BriefManagementDAO();
            Integer rs = brief.addbriefBorrow(briefName, briefId, employeeId,
                    receiveId, borrowStatus, reason, timeCompleted, hardType, softType);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, rs, dataSessionGR);

        } catch (Exception ex) {
            logger.error("briefBorrow - Exception:", ex);
            return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * get BriefDocument For Update
     *
     * @param request
     * @param isSecurity
     * @param data
     * @return
     */
    public String getBriefDocumentForUpdate(HttpServletRequest request,
            String data, String isSecurity) {
        String[] keys = new String[]{
            ConstantsFieldParams.BRIEF_DOCUMENT_ID
        };
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);

        // Session timeout
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("getBriefDocumentForUpdate - Session timeout!");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        try {
            List<String> listValue = dataSessionGR.getListParamsFromClient();

            Long briefDocumentId = null;

            if (!CommonUtils.isEmpty(listValue.get(0))) {
                briefDocumentId = Long.parseLong(listValue.get(0));
            }

            BriefManagementDAO brief = new BriefManagementDAO();
            EntityBriefDocument result = brief.getBriefDocumentById(briefDocumentId);
            if (result != null) {
                List<EntityBriefFilesAttachment> listFiles = brief.getListBriefFileAttachment(briefDocumentId);
                if (!CommonUtils.isEmpty(listFiles)) {
                    result.setFileAttachment(listFiles.get(0));
                    List<EntityBriefFilesAttachment> listFileAppendix = new ArrayList<>();
                    if (listFiles.size() > 1) {
                        for (int i = 1; i < listFiles.size(); i++) {
                            listFileAppendix.add(listFiles.get(i));
                        }
                    }
                    result.setFileAttachmentAppendix(listFileAppendix);
                }
            }

            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
        } catch (NumberFormatException ex) {
            logger.error("getBriefDocumentForUpdate - Exception:", ex);
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
    }
    /*Lay danh sach ho so muon*/

    public String getListBriefBorrow(String isSecurity, String strData, HttpServletRequest req) {
        String[] keys;
        keys = new String[]{
            ConstantsFieldParams.STR_KEYWORDS,
            ConstantsFieldParams.START_RECORD,
            ConstantsFieldParams.PAGE_SIZE,
            ConstantsFieldParams.IS_COUNT,
            ConstantsFieldParams.IS_ADVANCE,
            ConstantsFieldParams.BRIEF_NAME,
            ConstantsFieldParams.START_TIME,
            ConstantsFieldParams.END_TIME,
            ConstantsFieldParams.APPROVAL_STATUS,
            ConstantsFieldParams.BORROW_STATUS,
            ConstantsFieldParams.BRIEF_TYPE,
            ConstantsFieldParams.EMPLOYEE_ID,
            ConstantsFieldParams.TYPE_LIST};
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(req, strData, keys);

        // Session timeout
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("getListBriefBorrow - Session timeout!");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        try {
            //lay gia tri luu trong seasion
            Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
            List<Long> orgIdsByRole = new ArrayList<>();

            // Lay danh sach don vi sach don vi user co quyen van thu
            List<Long> listSecretaryVhrOrg = dataSessionGR.getVof2_ItemEntityUser().getListSecretaryVhrOrg();
            if (listSecretaryVhrOrg != null && listSecretaryVhrOrg.size() > 0) {
                orgIdsByRole.addAll(listSecretaryVhrOrg);
            }

            List<String> listValue = dataSessionGR.getListParamsFromClient();

            String strSearch = listValue.get(0);
            Long startRecord = 0L;
            if (!CommonUtils.isEmpty(listValue.get(1))) {
                startRecord = Long.parseLong(listValue.get(1));
            }
            Long pageSize = null;
            if (!CommonUtils.isEmpty(listValue.get(2))) {
                pageSize = Long.parseLong(listValue.get(2));
            }
            Integer isCount = 0;
            if (!CommonUtils.isEmpty(listValue.get(3))) {
                isCount = Integer.valueOf(listValue.get(3));
            }
            Integer isAdvance = 0;
            if (!CommonUtils.isEmpty(listValue.get(4))) {
                isAdvance = Integer.valueOf(listValue.get(4));
            }
            String briefName = listValue.get(5);
            String startTime = listValue.get(6);
            String endTime = listValue.get(7);
            Long approvalStatus = 0L;
            if (!CommonUtils.isEmpty(listValue.get(8))) {
                approvalStatus = Long.parseLong(listValue.get(8));
            }
             Long borrowStatus = 0L;
            if (!CommonUtils.isEmpty(listValue.get(9))) {
                borrowStatus = Long.parseLong(listValue.get(9));
            }
            Long briefType = 0L;
            if (!CommonUtils.isEmpty(listValue.get(10))) {
                briefType = Long.parseLong(listValue.get(10));
            }
            Long employeeId = 0L;
            if (!CommonUtils.isEmpty(listValue.get(11))) {
                employeeId = Long.parseLong(listValue.get(11));
            } 
            Integer typeList = 0;
            if (!CommonUtils.isEmpty(listValue.get(12))) {
                typeList = Integer.valueOf(listValue.get(12));
            }
            BriefManagementDAO brief = new BriefManagementDAO();
            Object result = brief.getBriefBorrow(userId, orgIdsByRole, strSearch, startRecord,
                    pageSize, isCount, isAdvance, briefName, startTime, endTime, approvalStatus, borrowStatus, briefType, employeeId, typeList);

            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
        } catch (NumberFormatException ex) {
            logger.error("getListBriefBorrow - Exception:", ex);
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
    }

     public String getListReceivedBrief(String isSecurity, String strData, HttpServletRequest req) {
        String[] keys;
        keys = new String[]{
            ConstantsFieldParams.STR_KEYWORDS,
            ConstantsFieldParams.START_RECORD,
            ConstantsFieldParams.PAGE_SIZE,
            ConstantsFieldParams.IS_COUNT,
            ConstantsFieldParams.IS_ADVANCE,
            ConstantsFieldParams.NAME,
            ConstantsFieldParams.BRIEF_AUTHOR,
            ConstantsFieldParams.STATUS,
            ConstantsFieldParams.START_TIME,
            ConstantsFieldParams.END_TIME,
            ConstantsFieldParams.CODE
        };
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(req, strData, keys);

        // Session timeout
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("getListReceivedBrief - Session timeout!");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        try {
            //lay gia tri luu trong seasion
            Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
            List<Long> orgIdsByRole = new ArrayList<>();

            // Lay danh sach don vi sach don vi user co quyen van thu
            List<Long> listSecretaryVhrOrg = dataSessionGR.getVof2_ItemEntityUser().getListSecretaryVhrOrg();
            if (listSecretaryVhrOrg != null && listSecretaryVhrOrg.size() > 0) {
                orgIdsByRole.addAll(listSecretaryVhrOrg);
            }

            List<String> listValue = dataSessionGR.getListParamsFromClient();

            String strSearch = listValue.get(0);
            Long startRecord = 0L;
            if (!CommonUtils.isEmpty(listValue.get(1))) {
                startRecord = Long.parseLong(listValue.get(1));
            }
            Long pageSize = null;
            if (!CommonUtils.isEmpty(listValue.get(2))) {
                pageSize = Long.parseLong(listValue.get(2));
            }
            Integer isCount = 0;
            if (!CommonUtils.isEmpty(listValue.get(3))) {
                isCount = Integer.valueOf(listValue.get(3));
            }
            Integer isAdvance = 0;
             if (!CommonUtils.isEmpty(listValue.get(4))) {
                isAdvance = Integer.valueOf(listValue.get(4));
            }
            String name = listValue.get(5);
            String author = listValue.get(6);
            
            Long status = 0L;
            if (!CommonUtils.isEmpty(listValue.get(7))) {
                status = Long.parseLong(listValue.get(7));
            }
            String startTime = listValue.get(8);
            String endTime = listValue.get(9);
           
            String code = listValue.get(10);

            BriefManagementDAO brief = new BriefManagementDAO();
            Object result = brief.getListReceivedBrief(userId, orgIdsByRole, strSearch, startRecord, pageSize, isCount, isAdvance, name, author, status, startTime,
                    endTime, code);

            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
        } catch (NumberFormatException ex) {
            logger.error("getListReceivedBrief - Exception:", ex);
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
    }
     /**
     * lay thong tin ten nganh dua vao id nganh
     *
     * @param request
     * @param isSecurity
     * @param data
     * @return
     */
    public String getAreaNameById(String isSecurity,
            String data, HttpServletRequest request) {
         String[] keys = new String[]{           
            ConstantsFieldParams.AREA_ID            
        };
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);

        // Session timeout
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("getAreaNameById - Session timeout!");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        try {
            List<String> listValue = dataSessionGR.getListParamsFromClient();
    
            Long areaId = null;
   
            if (!CommonUtils.isEmpty(listValue.get(0))) {
                areaId = Long.parseLong(listValue.get(0));
            }
 
            BriefManagementDAO brief = new BriefManagementDAO();
            String result = brief.getAreaNameById(areaId);
           
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
        } catch (NumberFormatException ex) {
            logger.error("getAreaNameById - Exception:", ex);
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
    }
    
    /**
     * lay thong tin don vi con va don vi cha
     *
     * @param request
     * @param isSecurity
     * @param data
     * @return
     */
    public String getAbbreviationChildAndParentOrg(HttpServletRequest request,
            String data, String isSecurity) {
         String[] keys = new String[]{           
            ConstantsFieldParams.SYS_ORGANIZATION_ID            
        };
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);

        // Session timeout
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("getAbbreviationChildAndParentOrg - Session timeout!");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        try {
            List<String> listValue = dataSessionGR.getListParamsFromClient();
    
            Long sysOrganizationId = null;
   
            if (!CommonUtils.isEmpty(listValue.get(0))) {
                sysOrganizationId = Long.parseLong(listValue.get(0));
            }
 
            BriefManagementDAO brief = new BriefManagementDAO();
            List<EntityVhrOrg> result = brief.getAbbreviationChildAndParentOrg(sysOrganizationId);
           
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
        } catch (NumberFormatException ex) {
            logger.error("getAbbreviationChildAndParentOrg - Exception:", ex);
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
    }
    /*getDataToExport*/
    public String getDataToExport(String isSecurity, String strData, HttpServletRequest req) {
        String[] keys;
        keys = new String[]{
            ConstantsFieldParams.STR_KEYWORDS,
            ConstantsFieldParams.START_RECORD,
            ConstantsFieldParams.PAGE_SIZE,
            ConstantsFieldParams.IS_COUNT,
            ConstantsFieldParams.IS_ADVANCE,
            ConstantsFieldParams.NAME,
            ConstantsFieldParams.BRIEF_AUTHOR,
            ConstantsFieldParams.PATH_NAME,
            ConstantsFieldParams.STATUS,
            ConstantsFieldParams.START_TIME,
            ConstantsFieldParams.END_TIME,
            ConstantsFieldParams.TIME_STORAGE_TYPE,
            ConstantsFieldParams.TIME_STORAGE,
            ConstantsFieldParams.CODE,
            ConstantsFieldParams.STORAGE_ID_LIST,
            ConstantsFieldParams.STYPE,
            ConstantsFieldParams.BOX_ID,
            ConstantsFieldParams.BRIEF_ID,
        };
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(req, strData, keys);

        // Session timeout
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("getListBrief - Session timeout!");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        try {
            //lay gia tri luu trong seasion
            Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
            List<Long> orgIdsByRole = new ArrayList<>();

            // Lay danh sach don vi sach don vi user co quyen van thu
            List<Long> listSecretaryVhrOrg = dataSessionGR.getVof2_ItemEntityUser().getListSecretaryVhrOrg();
            if (listSecretaryVhrOrg != null && listSecretaryVhrOrg.size() > 0) {
                orgIdsByRole.addAll(listSecretaryVhrOrg);
            }

            List<String> listValue = dataSessionGR.getListParamsFromClient();

            String strSearch = listValue.get(0);
            Long startRecord = 0L;
            if (!CommonUtils.isEmpty(listValue.get(1))) {
                startRecord = Long.parseLong(listValue.get(1));
            }
            Long pageSize = null;
            if (!CommonUtils.isEmpty(listValue.get(2))) {
                pageSize = Long.parseLong(listValue.get(2));
            }
            Integer isCount = 0;
            if (!CommonUtils.isEmpty(listValue.get(3))) {
                isCount = Integer.valueOf(listValue.get(3));
            }
            Integer isAdvance = 0;
            if (!CommonUtils.isEmpty(listValue.get(4))) {
                isAdvance = Integer.valueOf(listValue.get(4));
            }
            String name = listValue.get(5);
            String author = listValue.get(6);
            String orgName = listValue.get(7);            
            Long status = 0L;
            if (!CommonUtils.isEmpty(listValue.get(8))) {
                status = Long.parseLong(listValue.get(8));
            }
            String startTime = listValue.get(9);
            String endTime = listValue.get(10);
            Long timeStorageType = 0L;
            if (!CommonUtils.isEmpty(listValue.get(11))) {
                timeStorageType = Long.parseLong(listValue.get(11));
            }
            String timeStorage = listValue.get(12);
            Long code = 0L;
            if (!CommonUtils.isEmpty(listValue.get(13))) {
                code = Long.parseLong(listValue.get(13));
            }
            String storageIdList = listValue.get(14);

            Long stype = 0L;
            if (!CommonUtils.isEmpty(listValue.get(15))) {
                stype = Long.parseLong(listValue.get(15));
            }
            Long boxId = 0L;
            if (!CommonUtils.isEmpty(listValue.get(16))) {
                boxId = Long.parseLong(listValue.get(16));
            }
            Long briefId = 0L;
            if (!CommonUtils.isEmpty(listValue.get(17))) {
                briefId = Long.parseLong(listValue.get(17));
            }
            BriefManagementDAO brief = new BriefManagementDAO();
            Object result = brief.getDataToExport(userId, orgIdsByRole, strSearch, startRecord, pageSize, isCount, isAdvance, name, author, orgName, status, startTime,
                    endTime, timeStorageType, timeStorage, code, storageIdList, stype, boxId, briefId);

            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
        } catch (NumberFormatException ex) {
            logger.error("getListBrief - Exception:", ex);
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
    }
    /**
     * get HardStatus By BriefId
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    public String getHardStatusByBriefId(HttpServletRequest request,
            String data, String isSecurity) {
        String[] keys = new String[]{ConstantsFieldParams.BRIEF_ID};
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);
        // Khong co session
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("getHardStatusByBriefId - No session");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            logger.error("getHardStatusByBriefId - userId2 null");
            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
        }
        try {
            List<String> listValue = dataSessionGR.getListParamsFromClient();
            Long briefId = Long.parseLong(listValue.get(0));
            BriefManagementDAO m = new BriefManagementDAO();
            Long result = m.getHardStatusByBriefId(briefId);
            
           return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);            

        } catch (Exception ex) {
            logger.error("getHardStatusByBriefId - Exception:", ex);
            return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
        }
    }  
    
     /**
     * check exist document in brief
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    public String checkExistDocument(HttpServletRequest request,
            String data, String isSecurity) {
        String[] keys = new String[]{ConstantsFieldParams.BRIEF_ID,
                                    ConstantsFieldParams.PARAM_BRIEF_DOCUMENT_ID};
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);
        // Khong co session
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("checkExistDocument - No session");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            logger.error("checkExistDocument - userId2 null");
            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
        }
        try {
            List<String> listValue = dataSessionGR.getListParamsFromClient();
            Long briefId = Long.parseLong(listValue.get(0));
            Long documentId = Long.parseLong(listValue.get(1));
            
            BriefManagementDAO m = new BriefManagementDAO();
            Long result = m.checkExistDocument(briefId, documentId);
            
           return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);            

        } catch (Exception ex) {
            logger.error("deleteBrief - Exception:", ex);
            return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
        }
    } 
    
    /**
     * check HardStatusBrief
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    public String checkHardStatusBrief(HttpServletRequest request,
            String data, String isSecurity) {
        String[] keys = new String[]{ConstantsFieldParams.BRIEF_ID};
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);
        // Khong co session
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("checkHardStatusBrief - No session");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            logger.error("checkHardStatusBrief - userId2 null");
            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
        }
        try {
            List<String> listValue = dataSessionGR.getListParamsFromClient();
            String briefIds = listValue.get(0);
            Object result = null;
            if (!CommonUtils.isEmpty(briefIds)) {
                JSONArray arrJson = new JSONArray(briefIds);
                if (arrJson.length() > 0) {
                    List<Long> listIds = new ArrayList<>();
                    Long docId;
                    for (int i = 0; i < arrJson.length(); i++) {
                        if (arrJson.get(i) != null) {
                            docId = arrJson.getLong(i);
                            listIds.add(docId);
                        }
                    }
                    BriefManagementDAO m = new BriefManagementDAO();
                    result = m.checkHardStatusBrief(listIds);
                }
            }
           return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);            

        } catch (Exception ex) {
            logger.error("deleteBrief - Exception:", ex);
            return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
        }
    } 
    
    /**
     * kiem tra quyen xem chi tiet ho so
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    public String checkViewBriefInfoDetail(HttpServletRequest request,
            String data, String isSecurity) {
        String[] keys = new String[]{ConstantsFieldParams.BRIEF_ID};
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);
        // Khong co session
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("checkExistDocument - No session");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            logger.error("checkExistDocument - userId2 null");
            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
        }
        try {
            List<String> listValue = dataSessionGR.getListParamsFromClient();
            Long briefId = Long.parseLong(listValue.get(0));
                        
            BriefManagementDAO m = new BriefManagementDAO();
            Long result = m.checkViewBriefInfoDetail(briefId, userId);
            
           return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);            

        } catch (Exception ex) {
            logger.error("deleteBrief - Exception:", ex);
            return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
        }
    }
    
    /**
     * kiem tra quyen muon ho so
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    public String checkViewBorrowBrief(HttpServletRequest request,
            String data, String isSecurity) {
        String[] keys = new String[]{ConstantsFieldParams.BRIEF_ID};
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);
        // Khong co session
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("checkExistDocument - No session");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            logger.error("checkExistDocument - userId2 null");
            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
        }
        try {
            List<String> listValue = dataSessionGR.getListParamsFromClient();
            Long briefId = Long.parseLong(listValue.get(0));
                        
            BriefManagementDAO m = new BriefManagementDAO();
            Long result = m.checkViewBorrowBrief(briefId, userId);
            
           return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);            

        } catch (Exception ex) {
            logger.error("deleteBrief - Exception:", ex);
            return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
        }
    }
    
    /**
     * getListDocumentHistorys
     * @param isSecurity
     * @param strData
     * @param req
     * @return 
     */
    public String getListDocumentHistorys(String isSecurity, String strData, HttpServletRequest req) {
        String[] keys;
        keys = new String[]{
            ConstantsFieldParams.STR_KEYWORDS,
            ConstantsFieldParams.IS_COUNT,
            ConstantsFieldParams.BRIEFID,
            ConstantsFieldParams.RECEIVER_ID
        };
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(req, strData, keys);

        // Session timeout
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("getListDocumentHistory - Session timeout!");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        try {
            //lay gia tri luu trong seasion
            Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
            List<Long> orgIdsByRole = new ArrayList<>();

            // Lay danh sach don vi sach don vi user co quyen van thu
            List<Long> listSecretaryVhrOrg = dataSessionGR.getVof2_ItemEntityUser().getListSecretaryVhrOrg();
            if (listSecretaryVhrOrg != null && listSecretaryVhrOrg.size() > 0) {
                orgIdsByRole.addAll(listSecretaryVhrOrg);
            }

            List<String> listValue = dataSessionGR.getListParamsFromClient();

            String strSearch = listValue.get(0);
            Integer isCount = 0;
            if (!CommonUtils.isEmpty(listValue.get(1))) {
                isCount = Integer.valueOf(listValue.get(1));
            }
            Long briefId = 0L;
             if (!CommonUtils.isEmpty(listValue.get(2))) {
                briefId = Long.valueOf(listValue.get(2));
            }
            Long receiverId = 0L;
             if (!CommonUtils.isEmpty(listValue.get(3))) {
                receiverId = Long.valueOf(listValue.get(3));
            }

            BriefManagementDAO brief = new BriefManagementDAO();
            Object result = brief.getListDocumentHistory(userId, orgIdsByRole, strSearch, isCount, briefId, receiverId);

            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
        } catch (NumberFormatException ex) {
            logger.error("getListDocumentHistory - Exception:", ex);
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
     }
     
     /**
      * getBriefFileAttachmentDocument
      * @param request
      * @param data
      * @param isSecurity
      * @return 
      */
     public String getBriefFileAttachmentDocument(HttpServletRequest request,
            String data, String isSecurity) {
        String[] keys = new String[]{
            ConstantsFieldParams.BRIEF_DOCUMENT_ID
        };
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);

        // Session timeout
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("getBriefFileAttachmentDocument - Session timeout!");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        try {
            List<String> listValue = dataSessionGR.getListParamsFromClient();

            Long briefDocumentId = null;

            if (!CommonUtils.isEmpty(listValue.get(0))) {
                briefDocumentId = Long.parseLong(listValue.get(0));
            }

            BriefManagementDAO brief = new BriefManagementDAO();
           
            List<EntityBriefFilesAttachment> listFiles = brief.getListBriefFileAttachment(briefDocumentId);

            return FunctionCommon.responseResult(ErrorCode.SUCCESS, listFiles, dataSessionGR);
        } catch (NumberFormatException ex) {
            logger.error("getBriefFileAttachmentDocument - Exception:", ex);
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
    }
     
      /*danh sach don vi mac dinh*/
    public String getOrgListDefaultByUserId(HttpServletRequest req, String strData, String isSecurity) {
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(req, strData, null);
        // Khong co session
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("getListSysRoleOrg - No session");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }

        // Kiem tra user id tren he thong 2
        Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            logger.error("getListSysRoleOrg - userId null");
            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
        }

        BriefManagementDAO brief = new BriefManagementDAO();
        List<EntityVhrOrg> result = brief.getOrgListDefaultByUserId(userId);       
        return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
    }
     
     /**
      * Canh bao ho so sap het han
      * @return true insert thanh cong, fasle insert khong thanh cong
      */
    public boolean alertsBriefExpriring() {
        boolean result = false;
        try {
            BriefManagementDAO brief = new BriefManagementDAO();
            result = brief.alertsBriefExpriring();
        } catch (NumberFormatException ex) {
            logger.error("alertsBriefExpriring - Exception:", ex);
        }
        return result;
    }
    /**
      * Canh bao tra ban cung
      * @return 
      */
    public boolean alertReturnHardCopy() {
        boolean result = false;
        try {
            BriefManagementDAO brief = new BriefManagementDAO();
            result = brief.alertReturnHardCopy();
        } catch (NumberFormatException ex) {
            logger.error("alertsBriefExpriring - Exception:", ex);
        }
        return result;
    }
    
    /**
     * @author TungHD
     * 
     * <b>Download noi dung file van ban tao trong ho so</b><br>
     * getInfoFileBrief
     * @param req
     * @param data
     * @param isSecurity
     * @return
     */
    public String getInfoFileBrief(HttpServletRequest req, String data,
            String isSecurity) {
        EntityLog log = new EntityLog(req, BriefManagementController.class.getName());
        EntityBriefFilesAttachment resultData = new EntityBriefFilesAttachment();
        String strResult = null;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(req);
        if (!userGroup.getCheckSessionOk()) {
            logger.error("getInfoFileBrief - Session timeout!");
            return strResult;
        }
        // Lay id user he thong 2
        Long userIdVof2 = userGroup.getVof2_ItemEntityUser().getUserId();
        if (userIdVof2 == null) {
            return strResult;
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
                ConstantsFieldParams.BRIEF_DOCUMENT_ID,
                "briefFileAttachmentId"
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long briefDocumentId = 0L;
            if (!CommonUtils.isEmpty(listValue.get(0))) {
                briefDocumentId = Long.parseLong(listValue.get(0).trim());
            }
            Long briefFileAttachmentId = 0L;
            if (!CommonUtils.isEmpty(listValue.get(1))) {
                briefFileAttachmentId = Long.parseLong(listValue.get(1).trim());
            }
            if (CommonUtils.isEmpty(listValue.get(0)) || CommonUtils.isEmpty(listValue.get(1))) {
                return strResult;
            }
            String[] arrInfoFile = new String[2];
            BriefManagementDAO brief = new BriefManagementDAO();
            arrInfoFile = brief.getInfoFileBrief(req, briefDocumentId, userIdVof2, briefFileAttachmentId);
            if (arrInfoFile != null && arrInfoFile.length > 1) {
                resultData.setFileSize(Long.valueOf(arrInfoFile[0]));
                resultData.setFilePage(Long.valueOf(arrInfoFile[1]));
            }
            strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                    resultData, aesKey);
        } catch (NumberFormatException | JSONException ex) {
            logger.error("getInfoFile - Exception - username: " + cardId + "\ndata: " + data, ex);
            strResult = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    resultData, aesKey);
        }
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return strResult;
        
    }
}
