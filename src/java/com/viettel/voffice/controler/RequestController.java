/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.common.CommonDataBaseDaoVO2;
import com.viettel.voffice.database.dao.request.RequestDAO;
import com.viettel.voffice.database.dao.request.RequestEmpConfigDAO;
import com.viettel.voffice.database.entity.EntityFileAttachment;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityRequestDetail;
import com.viettel.voffice.database.entity.EntityRequestRole;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.request.EntityRequestEmpConfig;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.DateTimeUtils;
import com.viettel.voffice.utils.LogUtils;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author kiennt45
 */
public class RequestController {

    // Ten cua class bao gom ca ten package
    private static final String CLASS_NAME = RequestController.class.getName();
    
    /** Log file */
    private static final Logger LOGGER = Logger.getLogger(TaskServiceController.class);

    /**
     * <b>Tim kiem nang cao kien nghi/yeu cau (kho khan/vuong mac)</b>
     * 
     * @param isSecurity
     * @param data
     * @param request
     * @return 
     */
    public String getListRequestAdvance(String isSecurity, String data,
            HttpServletRequest request) {
        
        String[] keys = new String[] {
            ConstantsFieldParams.IS_COUNT,
            ConstantsFieldParams.KEYWORD,
            ConstantsFieldParams.TYPE,
            ConstantsFieldParams.TITLE,
            ConstantsFieldParams.REQ_CONTENT,
            ConstantsFieldParams.REQ_START_CREATE_DATE,
            ConstantsFieldParams.REQ_END_CREATE_DATE,
            ConstantsFieldParams.REQ_START_EFFECTIVE_DATE,
            ConstantsFieldParams.REQ_END_EFFECTIVE_DATE,
            ConstantsFieldParams.REQ_START_EXPIRED_DATE,
            ConstantsFieldParams.REQ_END_EXPIRED_DATE,
            ConstantsFieldParams.REQ_LIST_REQUEST,
            ConstantsFieldParams.REQ_LIST_RECIPIENT,
            ConstantsFieldParams.REQ_LIST_GROUP_REQUEST,
            ConstantsFieldParams.REQ_LEVEL,
            ConstantsFieldParams.REQ_REASON,
            ConstantsFieldParams.REQ_LIST_STATUS,
            ConstantsFieldParams.START_RECORD,
            ConstantsFieldParams.PAGE_SIZE,
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        String errorDesc = String.format(Constants.Common.LOG_SYNTAX, "getListRequestAdvance",
                userGroup.getUserId2(), "userId1", userGroup.getUserId1());
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error(errorDesc + "Session timeout!");
            return null;
        }
        try {
            // Parse du lieu tu client
            List<String> listValue = userGroup.getListParamsFromClient();
            // 0: Lay danh sach, 1: Lay so luong
            Long isCount = 0L;
            String strIsCount = listValue.get(0);
            if (!CommonUtils.isEmpty(strIsCount)) {
                isCount = Long.parseLong(strIsCount);
            }
            // Tu khoa tim kiem
            String keyword = listValue.get(1);
            // Loai 0: tat ca, 1: nhan, 2: gui
            Long type = 0L;
            String strType = listValue.get(2);
            if (!CommonUtils.isEmpty(strType)) {
                type = Long.parseLong(strType);
            }
            // Tieu de
            String title = listValue.get(3);
            // Noi dung
            String content = listValue.get(4);
            // Ngay bat dau tu
            String startCreateDate = listValue.get(5);
            // Ngay bat dau den
            String endCreateDate = listValue.get(6);
            // Ngay co hieu luc tu
            String startEffectiveDate = listValue.get(7);
            // Ngay co hieu luc den
            String endEffectiveDate = listValue.get(8);
            // Ngay het han tu
            String startExpiredDate = listValue.get(9);
            // Ngay het han den
            String endExpiredDate = listValue.get(10);
            // Danh sach id nguoi yeu cau
            List<Long> listRequest = new ArrayList<>();
            String strListRequest = listValue.get(11);
            if (!CommonUtils.isEmpty(strListRequest)) {
                JSONArray jaRequest = new JSONArray(strListRequest);
                JSONObject joRequest;
                if (jaRequest.length() > 0) {
                    for (int i = 0; i < jaRequest.length(); i++) {
                        joRequest = jaRequest.getJSONObject(i);
                        if (joRequest.has(ConstantsFieldParams.STAFF_ID)) {
                            listRequest.add(joRequest.getLong(ConstantsFieldParams.STAFF_ID));
                        }
                    }
                }
            }
            // Danh sach id nguoi nhan
            List<Long> listRecipient = new ArrayList<>();
            String strListRecipient = listValue.get(12);
            if (!CommonUtils.isEmpty(strListRecipient)) {
                JSONArray jaRecipient = new JSONArray(strListRecipient);
                JSONObject joRecipient;
                if (jaRecipient.length() > 0) {
                    for (int i = 0; i < jaRecipient.length(); i++) {
                        joRecipient = jaRecipient.getJSONObject(i);
                        if (joRecipient.has(ConstantsFieldParams.STAFF_ID)) {
                            listRecipient.add(joRecipient.getLong(ConstantsFieldParams.STAFF_ID));
                        }
                    }
                }
            }
            // Danh sach don vi yeu cau
            List<Long> listGroupRequest = new ArrayList<>();
            String strListGroupRequest = listValue.get(13);
            if (!CommonUtils.isEmpty(strListGroupRequest)) {
                JSONArray jaGroupRequest = new JSONArray(strListGroupRequest);
                JSONObject joGroupRequest;
                if (jaGroupRequest.length() > 0) {
                    for (int i = 0; i < jaGroupRequest.length(); i++) {
                        joGroupRequest = jaGroupRequest.getJSONObject(i);
                        if (joGroupRequest.has(ConstantsFieldParams.TASK_GROUP_ID)) {
                             listGroupRequest.add(joGroupRequest.getLong(ConstantsFieldParams.TASK_GROUP_ID));
                        }
                    }
                }
            }
            // Level
            Long level = null;
            String strLevel = listValue.get(14);
            if (!CommonUtils.isEmpty(strLevel)) {
                level = Long.parseLong(strLevel);
            }
            // Ly do
            String reason = listValue.get(15);
            // Trang thai
            List<Long> listStatus = new ArrayList<>();
            String strListStatus = listValue.get(16);
            if (!CommonUtils.isEmpty(strListStatus)) {
                JSONArray jaStatus = new JSONArray(strListStatus);
                JSONObject joStatus;
                if (jaStatus.length() > 0) {
                    for (int i = 0; i < jaStatus.length(); i++) {
                        joStatus = jaStatus.getJSONObject(i);
                        if (joStatus.has(ConstantsFieldParams.STATUS)) {
                            listStatus.add(joStatus.getLong(ConstantsFieldParams.STATUS));
                        }
                    }
                }
            }
            // Vi tri lay ra
            Long startRecord = 0L;
            String strStartRecord = listValue.get(17);
            if (!CommonUtils.isEmpty(strStartRecord)) {
                startRecord = Long.parseLong(strStartRecord);
            }
            // So luong lay ra
            Long pageSize = 10L;
            String strPageSize = listValue.get(18);
            if (!CommonUtils.isEmpty(strPageSize)) {
                pageSize = Long.parseLong(strPageSize);
            }            
            RequestDAO requestDAO = new RequestDAO();
            Object result = requestDAO.getListRequestAdvance(type, isCount, userGroup.getUserId2(),
                    startRecord, pageSize, title, content, reason, startCreateDate,
                    endCreateDate, startEffectiveDate, endEffectiveDate, startExpiredDate,
                    endExpiredDate, listRequest, listRecipient, listGroupRequest,
                    level, listStatus, keyword);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            LOGGER.error(errorDesc + "Exception!", ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Lay chi tiet kien nghi de xuat</b>
     * 
     * @param isSecurity
     * @param strData
     * @param req
     * @return 
     */
    public String getRequestDetail(String isSecurity, String strData,
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

                RequestDAO reqDao = new RequestDAO();
                HashMap<String, Object> hmParams = new HashMap<String, Object>();
                hmParams.put(ConstantsFieldParams.REQUEST_ID, Long.class);

                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);

                Long requestId = (Long) ((valueParams.get(ConstantsFieldParams.REQUEST_ID) != null) ? valueParams.get(ConstantsFieldParams.REQUEST_ID) : null);

                if (requestId == null) {
                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, strAesKeyDecode);
                } else {
                    EntityRequestDetail detail = reqDao.getRequestDetail(requestId);
                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, detail, strAesKeyDecode);
                }
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LOGGER.error("Exception: ", e);
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
     * gui kien nghi, de xuat
     *
     * @param
     * @return
     */
    public String addRequest(String isSecurity, String strData, HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        //Toan bo cac ham khi thuc hien deu phai chay qua ham nay tru ham login va get RSA key
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //Lay prams client gui len de thuc hien yeu cau
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
                String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);

                HashMap<String, Object> hmParams = new HashMap<String, Object>();
                hmParams.put(ConstantsFieldParams.TYPE, Long.class);
                hmParams.put(ConstantsFieldParams.TITLE, String.class);
                hmParams.put(ConstantsFieldParams.REQUEST_REASON, String.class);
                hmParams.put(ConstantsFieldParams.REQUEST_CONTENT, String.class);
                hmParams.put(ConstantsFieldParams.EXPIRED_DATE, String.class);
                hmParams.put(ConstantsFieldParams.RECIPIENT_ID, Long.class);
                hmParams.put(ConstantsFieldParams.REQUEST_ID, Long.class);
                hmParams.put(ConstantsFieldParams.RECIPIENT_ORG_ID, Long.class);

                SimpleDateFormat formatShortDate = new SimpleDateFormat("dd/MM/yyyy");

                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);

                Long requestId = (Long) ((valueParams.get(ConstantsFieldParams.REQUEST_ID) != null) ? valueParams.get(ConstantsFieldParams.REQUEST_ID) : 0L);
                Long addType = (Long) ((valueParams.get(ConstantsFieldParams.TYPE) != null) ? valueParams.get(ConstantsFieldParams.TYPE) : 0L);
                Long requestLevel;
                Long status;
                Long recipientId = (Long) ((valueParams.get(ConstantsFieldParams.RECIPIENT_ID) != null) ? valueParams.get(ConstantsFieldParams.RECIPIENT_ID) : 0L);
                String title = (String) ((valueParams.get(ConstantsFieldParams.TITLE) != null) ? valueParams.get(ConstantsFieldParams.TITLE) : "");
                String reason = (String) ((valueParams.get(ConstantsFieldParams.REQUEST_REASON) != null) ? valueParams.get(ConstantsFieldParams.REQUEST_REASON) : "");
                String content = (String) ((valueParams.get(ConstantsFieldParams.REQUEST_CONTENT) != null) ? valueParams.get(ConstantsFieldParams.REQUEST_CONTENT) : "");
                String expiredDate = (String) ((valueParams.get(ConstantsFieldParams.EXPIRED_DATE) != null) ? valueParams.get(ConstantsFieldParams.EXPIRED_DATE) : "");
                Long recipientOrgId = (Long) ((valueParams.get(ConstantsFieldParams.RECIPIENT_ORG_ID) != null)
                        ? valueParams.get(ConstantsFieldParams.RECIPIENT_ORG_ID) : 0L);
                //addType = 0 -> Luu kien nghi de xuat, addType = 1 -> Luu va gui kien nghi
                if (addType.equals(0L)) {
                    status = 1L;
                    requestLevel = null;
                } else {
                    status = 2L;
                    requestLevel = 1L;
                }

                //Lay danh sach file dinh kem
                List<EntityFileAttachment> lstFileAttachment = new ArrayList<EntityFileAttachment>();
                List<Long> lstFileDelete = new ArrayList<Long>();

                JSONArray arrFileAttachment = FunctionCommon.jsonGetArray(ConstantsFieldParams.REQ_LST_FILE_ATTACHMENT, strDataClient);

                if (arrFileAttachment != null && arrFileAttachment.length() > 0) {
                    for (int i = 0; i < arrFileAttachment.length(); i++) {
                        JSONObject innerObj = (JSONObject) arrFileAttachment.get(i);

                        String name = "";
                        String filePath = "";

                        if (innerObj.has(ConstantsFieldParams.TEXT_ATTACH_NAME)) {
                            name = innerObj.getString(ConstantsFieldParams.TEXT_ATTACH_NAME).trim();
//                            System.out.println(name);
                        }

                        if (innerObj.has(ConstantsFieldParams.REQ_ATTACHMENT)) {
                            filePath = innerObj.getString(ConstantsFieldParams.REQ_ATTACHMENT).trim();
//                            System.out.println(filePath);
                        }

                        EntityFileAttachment file = new EntityFileAttachment();
                        file.setFilePath(filePath);
                        file.setName(name);

                        lstFileAttachment.add(file);
                    }
                }

                JSONArray arrFileDelete = FunctionCommon.jsonGetArray(ConstantsFieldParams.REQ_LST_FILE_DELETE, strDataClient);

                if (arrFileDelete != null && arrFileDelete.length() > 0) {
                    for (int i = 0; i < arrFileDelete.length(); i++) {
                        JSONObject innerObj = (JSONObject) arrFileDelete.get(i);
                        Long fileAttachmentId = Long.parseLong(innerObj.getString(ConstantsFieldParams.TEXT_ATTACH_ID));

                        lstFileDelete.add(fileAttachmentId);
                    }
                }

                if (title.isEmpty() || expiredDate.isEmpty() || recipientId == 0L) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                }

                String expiredDateServer = formatShortDate.format(DateTimeUtils.getWorkingDaysAfterDate(new Date(), 2));

                Date date1 = (Date) formatShortDate.parse(expiredDateServer);
                Date date2 = (Date) formatShortDate.parse(expiredDate);

                if (date2.before(date1) && addType.equals(1L)) {
//                    System.out.print("Kiennt45 Han ngay xu ly phai >= 3");
                    return FunctionCommon.generateResponseJSON(ErrorCode.ERR_DATE_VAILD, null, null);
                }

                RequestDAO reqDao = new RequestDAO();
                Long result = reqDao.addRequest(requestId, addType, title, reason,
                        content, expiredDate, userId, requestLevel, status, recipientId,
                        lstFileAttachment, lstFileDelete, recipientOrgId);

                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                        result, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
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
     * xoa kien nghi de xuat
     *
     * @param
     * @return
     */
    public String deleteRequest(String isSecurity, String strData, HttpServletRequest req) {

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

                Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
                RequestDAO reqDao = new RequestDAO();
                HashMap<String, Object> hmParams = new HashMap<String, Object>();
                hmParams.put(ConstantsFieldParams.REQUEST_ID, Long.class);

                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);

                Long requestId = (Long) ((valueParams.get(ConstantsFieldParams.REQUEST_ID) != null) ? valueParams.get(ConstantsFieldParams.REQUEST_ID) : null);

                Boolean delete = reqDao.deleteRequest(requestId, 1L, userId);
                Long result = 0L;

                if (delete) {
                    result = 1L;
                }

                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                        result, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
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
     * Tu giai quyet kien nghi, de xuat cap 1
     *
     * @param
     * @return
     */
    public String addResolve(String isSecurity, String strData, HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        //Toan bo cac ham khi thuc hien deu phai chay qua ham nay tru ham login va get RSA key
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //Lay prams client gui len de thuc hien yeu cau
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
                String email = dataSessionGR.getVof2_ItemEntityUser().getStrEmail();
                String fullname = dataSessionGR.getVof2_ItemEntityUser().getFullName();
                String contact = fullname + "_" + email;
                String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);

                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);

                HashMap<String, Object> hmParams = new HashMap<String, Object>();
                hmParams.put(ConstantsFieldParams.SOLUTION, String.class);
                hmParams.put(ConstantsFieldParams.REQUEST_ID, Long.class);

                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);

                Long requestId = (Long) ((valueParams.get(ConstantsFieldParams.REQUEST_ID) != null) ? valueParams.get(ConstantsFieldParams.REQUEST_ID) : 0L);
                Long action = 2L;
                Long requestLevel = 1L;
                Long status = 4L;
                String expiredDate;
                String solution = (String) ((valueParams.get(ConstantsFieldParams.SOLUTION) != null) ? valueParams.get(ConstantsFieldParams.SOLUTION) : "");

                //Lay danh sach file dinh kem
                List<EntityFileAttachment> lstFileAttachment = new ArrayList<EntityFileAttachment>();

                JSONArray arrFileAttachment = FunctionCommon.jsonGetArray(ConstantsFieldParams.REQ_LST_FILE_ATTACHMENT, strDataClient);

                if (arrFileAttachment != null && arrFileAttachment.length() > 0) {
                    for (int i = 0; i < arrFileAttachment.length(); i++) {
                        JSONObject innerObj = (JSONObject) arrFileAttachment.get(i);

                        String name = "";
                        String filePath = "";

                        if (innerObj.has(ConstantsFieldParams.TEXT_ATTACH_NAME)) {
                            name = innerObj.getString(ConstantsFieldParams.TEXT_ATTACH_NAME).trim();
//                            System.out.println(name);
                        }

                        if (innerObj.has(ConstantsFieldParams.REQ_ATTACHMENT)) {
                            filePath = innerObj.getString(ConstantsFieldParams.REQ_ATTACHMENT).trim();
//                            System.out.println(filePath);
                        }

                        EntityFileAttachment file = new EntityFileAttachment();
                        file.setFilePath(filePath);
                        file.setName(name);

                        lstFileAttachment.add(file);
                    }
                }

                //Lay current expired date
                CommonDataBaseDaoVO2 cmd = new CommonDataBaseDaoVO2();
                List<Object> params = new ArrayList<Object>();

                StringBuilder reqQuery = new StringBuilder();
                reqQuery.append(" select TO_CHAR(rp.CURRENT_EXPIRED_DATE,'dd/MM/yyyy') expiredDate ");
                reqQuery.append(" from REQUEST_PROCESS rp ");
                reqQuery.append(" where rp.REQUEST_ID = ? and rp.CURRENT_EXPIRED_DATE is not null ");
                reqQuery.append(" and rownum = 1 ");
                reqQuery.append(" order by rp.REQUEST_LEVEL desc ");

                params.add(requestId);

                expiredDate = cmd.excuteSqlGetValOnConditionListParams(reqQuery, params).toString();

                if (expiredDate.isEmpty() || requestId.equals(0L)) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                }

                RequestDAO reqDao = new RequestDAO();
                Long result = reqDao.addResolve(requestId, status, userId,
                        expiredDate, action, solution,
                        requestLevel, lstFileAttachment, contact);

                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                        result, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
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
     * Chu dong chuyen len cap tren
     *
     * @param
     * @return
     */
    public String forwardLevel(String isSecurity, String strData, HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        //Toan bo cac ham khi thuc hien deu phai chay qua ham nay tru ham login va get RSA key
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //Lay prams client gui len de thuc hien yeu cau
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
                String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);

                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);

                SimpleDateFormat formatShortDate = new SimpleDateFormat("dd/MM/yyyy");

//                System.out.println("Kiennt45 Data forwardLevel:" + strDataClient);

                HashMap<String, Object> hmParams = new HashMap<String, Object>();
                hmParams.put(ConstantsFieldParams.SOLUTION, String.class);
                hmParams.put(ConstantsFieldParams.REQUEST_ID, Long.class);
                hmParams.put(ConstantsFieldParams.EXPIRED_DATE, String.class);
                hmParams.put(ConstantsFieldParams.RECIPIENT_ID, Long.class);
                hmParams.put(ConstantsFieldParams.RECIPIENT_ORG_ID, Long.class);

                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);

                Long requestId = (Long) ((valueParams.get(ConstantsFieldParams.REQUEST_ID) != null) ? valueParams.get(ConstantsFieldParams.REQUEST_ID) : 0L);
                Long recipientId = (Long) ((valueParams.get(ConstantsFieldParams.RECIPIENT_ID) != null) ? valueParams.get(ConstantsFieldParams.RECIPIENT_ID) : 0L);
                Long recipientOrgId = (Long) ((valueParams.get(ConstantsFieldParams.RECIPIENT_ORG_ID) != null)
                        ? valueParams.get(ConstantsFieldParams.RECIPIENT_ORG_ID) : 0L);
                Long action = 3L;
                Long status = 2L;
                String expiredDate = formatShortDate.format(DateTimeUtils.getWorkingDaysAfterDate(new Date(), 2));
                String solution = (String) ((valueParams.get(ConstantsFieldParams.SOLUTION) != null) ? valueParams.get(ConstantsFieldParams.SOLUTION) : "");

                //Lay danh sach file dinh kem
                List<EntityFileAttachment> lstFileAttachment = new ArrayList<EntityFileAttachment>();

                JSONArray arrFileAttachment = FunctionCommon.jsonGetArray(ConstantsFieldParams.REQ_LST_FILE_ATTACHMENT, strDataClient);

                if (arrFileAttachment != null && arrFileAttachment.length() > 0) {
                    for (int i = 0; i < arrFileAttachment.length(); i++) {
                        JSONObject innerObj = (JSONObject) arrFileAttachment.get(i);

                        String name = "";
                        String filePath = "";

                        if (innerObj.has(ConstantsFieldParams.TEXT_ATTACH_NAME)) {
                            name = innerObj.getString(ConstantsFieldParams.TEXT_ATTACH_NAME).trim();
//                            System.out.println(name);
                        }

                        if (innerObj.has(ConstantsFieldParams.REQ_ATTACHMENT)) {
                            filePath = innerObj.getString(ConstantsFieldParams.REQ_ATTACHMENT).trim();
//                            System.out.println(filePath);
                        }

                        EntityFileAttachment file = new EntityFileAttachment();
                        file.setFilePath(filePath);
                        file.setName(name);

                        lstFileAttachment.add(file);
                    }
                }

                RequestDAO reqDao = new RequestDAO();
                Long result = reqDao.forwardLevel(requestId, status, userId, expiredDate,
                        recipientId, action, solution, lstFileAttachment, recipientOrgId);

                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                        result, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LOGGER.error("Error: ", e);
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
     * <b>Tu choi hoac dong y cach giai quyet</b>
     * 
     * @param isSecurity
     * @param strData
     * @param req
     * @return 
     */
    public String confirmSolutionRequest(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        //Toan bo cac ham khi thuc hien deu phai chay qua ham nay tru ham login va get RSA key
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //Lay prams client gui len de thuc hien yeu cau
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
                String email = dataSessionGR.getVof2_ItemEntityUser().getStrEmail();
                String fullname = dataSessionGR.getVof2_ItemEntityUser().getFullName();
                String contact = fullname + "_" + email;
                String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);

                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);

                SimpleDateFormat formatShortDate = new SimpleDateFormat("dd/MM/yyyy");

                LOGGER.info("Kiennt45 Data confirmSolutionRequest:" + strDataClient);

                HashMap<String, Object> hmParams = new HashMap<String, Object>();
                hmParams.put(ConstantsFieldParams.SOLUTION, String.class);
                hmParams.put(ConstantsFieldParams.REQUEST_ID, Long.class);
                hmParams.put(ConstantsFieldParams.EXPIRED_DATE, String.class);
                hmParams.put(ConstantsFieldParams.TYPE, Long.class);
                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
                Long type = (Long) ((valueParams.get(ConstantsFieldParams.TYPE) != null) ? valueParams.get(ConstantsFieldParams.TYPE) : 0L);
                Long requestId = (Long) ((valueParams.get(ConstantsFieldParams.REQUEST_ID) != null) ? valueParams.get(ConstantsFieldParams.REQUEST_ID) : 0L);
                Long recipientId = (Long) ((valueParams.get(ConstantsFieldParams.RECIPIENT_ID) != null) ? valueParams.get(ConstantsFieldParams.RECIPIENT_ID) : 0L);
                String solution = (String) ((valueParams.get(ConstantsFieldParams.SOLUTION) != null) ? valueParams.get(ConstantsFieldParams.SOLUTION) : "");
                String expiredDate = formatShortDate.format(DateTimeUtils.getWorkingDaysAfterDate(new Date(), 2));
                Long action;
                Long status;

                if (type.equals(0L)) {
                    action = 7L;
                    status = 5L;
                    expiredDate = "";
                } else if (type.equals(1L)) {
                    action = 8L;
                    status = 3L;
                } else {
                    action = 8L;
                    status = 2L;
                }

                RequestDAO reqDao = new RequestDAO();
                Long result = reqDao.confirmSolutionRequest(type, requestId, status, userId,
                        expiredDate, recipientId, action, solution, contact);

                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                        result, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LOGGER.error("Error: ", e);
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
     * Dong kien nghi de xuat
     *
     * @param
     * @return
     */
    public String closeRequest(String isSecurity, String strData, HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        //Toan bo cac ham khi thuc hien deu phai chay qua ham nay tru ham login va get RSA key
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //Lay prams client gui len de thuc hien yeu cau
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
                String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);

                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);

//                System.out.println("Kiennt45 Data closeRequest:" + strDataClient);

                HashMap<String, Object> hmParams = new HashMap<String, Object>();
                hmParams.put(ConstantsFieldParams.SOLUTION, String.class);
                hmParams.put(ConstantsFieldParams.REQUEST_ID, Long.class);

                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);

                Long requestId = (Long) ((valueParams.get(ConstantsFieldParams.REQUEST_ID) != null) ? valueParams.get(ConstantsFieldParams.REQUEST_ID) : 0L);
                String solution = (String) ((valueParams.get(ConstantsFieldParams.SOLUTION) != null) ? valueParams.get(ConstantsFieldParams.SOLUTION) : "");

                RequestDAO reqDao = new RequestDAO();
                Long result = reqDao.closeRequest(requestId, solution, userId);

                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                        result, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LOGGER.error("Error: ", e);
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
     * Giao viec cho ca nhan hoac don vi kien nghi de xuat
     *
     * @param
     * @return
     */
    public String assignRequest(String isSecurity, String strData, HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        //Toan bo cac ham khi thuc hien deu phai chay qua ham nay tru ham login va get RSA key
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //Lay prams client gui len de thuc hien yeu cau
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
                String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);

                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);

                HashMap<String, Object> hmParams = new HashMap<String, Object>();
                hmParams.put(ConstantsFieldParams.TYPE, Long.class);
                hmParams.put(ConstantsFieldParams.REQUEST_ID, Long.class);
                hmParams.put(ConstantsFieldParams.STR_OBJECT_ID, Long.class);

                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);

                Long requestId = (Long) ((valueParams.get(ConstantsFieldParams.REQUEST_ID) != null) ? valueParams.get(ConstantsFieldParams.REQUEST_ID) : 0L);
                Long typeId = (Long) ((valueParams.get(ConstantsFieldParams.TYPE) != null) ? valueParams.get(ConstantsFieldParams.TYPE) : 0L);
                //Long objectId = (Long) ((valueParams.get(ConstantsFieldParams.STR_OBJECT_ID) != null) ? valueParams.get(ConstantsFieldParams.STR_OBJECT_ID) : 0L);                
                List<Long> listObject = new ArrayList<>();
                //Parse list request
                JSONArray arrRequest = FunctionCommon.jsonGetArray(ConstantsFieldParams.REQ_LST_OBJECT, strDataClient);

                if (arrRequest != null && arrRequest.length() > 0) {
                    for (int i = 0; i < arrRequest.length(); i++) {
                        JSONObject innerObj = (JSONObject) arrRequest.get(i);

                        Long objectId = null;

                        if (innerObj.has(ConstantsFieldParams.STR_OBJECT_ID)) {
                            objectId = Long.parseLong(innerObj.getString(ConstantsFieldParams.STR_OBJECT_ID).trim());
//                            System.out.println(objectId);
                        }

                        listObject.add(objectId);
                    }
                }

                RequestDAO reqDao = new RequestDAO();
                Long result = 1L;

                if (listObject.size() > 0) {
                    result = reqDao.assignRequest(requestId, userId, typeId, listObject);
                }

                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                        result, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LOGGER.error("Error: ", e);
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
     * <b>Gui kien nghi de xuat</b>
     * 
     * @param isSecurity
     * @param data
     * @param request
     * @return 
     */
    public String sendRequest(String isSecurity, String data, HttpServletRequest request) {
        
        String[] keys = new String[] {
            ConstantsFieldParams.REQUEST_ID
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        String errorDesc = "sendRequest - username: " + userGroup.getCardId() + " - ";
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error(errorDesc + "Session timeout!");
            return null;
        }
        try {
            // Parse du lieu tu client
            List<String> listValue = userGroup.getListParamsFromClient();
            Long requestId = Long.parseLong(listValue.get(0));
            RequestDAO requestDAO = new RequestDAO();
            Long result = requestDAO.sendRequest(requestId, userGroup.getUserId2());
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            LOGGER.error(errorDesc + "Exception!", ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * Lay quyen kien nghi de xuat
     *
     * @param
     * @return
     */
    public String getRoleRequest(String isSecurity, String strData, HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        //Toan bo cac ham khi thuc hien deu phai chay qua ham nay tru ham login va get RSA key
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //Lay prams client gui len de thuc hien yeu cau
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
                String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);

                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);

                HashMap<String, Object> hmParams = new HashMap<String, Object>();
                hmParams.put(ConstantsFieldParams.REQUEST_ID, Long.class);

                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);

                Long requestId = (Long) ((valueParams.get(ConstantsFieldParams.REQUEST_ID) != null) ? valueParams.get(ConstantsFieldParams.REQUEST_ID) : 0L);

                RequestDAO reqDao = new RequestDAO();
                EntityRequestRole result = reqDao.getRoleRequest(requestId, userId);

                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                        result, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LOGGER.error("Error: ", e);
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
     * Lay nguon goc tu lich su xu ly
     *
     * @param
     * @return
     */
    public String getRequestId(String isSecurity, String strData, HttpServletRequest req) {

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
                LOGGER.info("Kiennt45 Data getRequestId:" + strDataClient);

                HashMap<String, Object> hmParams = new HashMap<String, Object>();
                hmParams.put(ConstantsFieldParams.REQ_PROCESS_ID, Long.class);

                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);

                Long requestId = (Long) ((valueParams.get(ConstantsFieldParams.REQ_PROCESS_ID) != null) ? valueParams.get(ConstantsFieldParams.REQ_PROCESS_ID) : 0L);

                RequestDAO reqDao = new RequestDAO();
                Long result = reqDao.getRequestId(requestId);

                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                        result, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LOGGER.error("Error: ", e);
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
     * <b>Cap nhat thong tin</b>
     * 
     * @param request
     * @param data
     * @return 
     */
    public String updateRequestEmpConfig(HttpServletRequest request, String data) {
        
        String[] keys = new String[] {
            "listRequestEmpConfig"
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("updateRequestEmpConfig - Session timeout!");
            return null;
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            // Danh sach tieu chi cap nhat
            String strListRequestEmpConfig = listValue.get(0);
            Type type = new TypeToken<ArrayList<EntityRequestEmpConfig>>() {
            }.getType();
            Gson gson = new Gson();
            List<EntityRequestEmpConfig> listRequestEmpConfig = gson.fromJson(
                    strListRequestEmpConfig, type);
            RequestEmpConfigDAO requestEmpConfigDAO = new RequestEmpConfigDAO();
            int result = requestEmpConfigDAO.updateRequestEmpConfig(userGroup.getUserId2(),
                    listRequestEmpConfig) ? 1 : 0;
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            LOGGER.error("updateRequestEmpConfig - Exception - username: "
                    + userGroup.getCardId(), ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Lay danh sach cau hinh nguoi nhan kien nghi/de xuat</b>
     * 
     * @param request
     * @param data
     * @return 
     */
    public String getListRequestEmpConfig(HttpServletRequest request, String data) {
        
        String[] keys = new String[] {
            "keywordForOrg",
            "keywordForEmp",
            ConstantsFieldParams.START_RECORD,
            ConstantsFieldParams.PAGE_SIZE
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getListRequestEmpConfig - Session timeout!");
            return null;
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            // Tu khoa tim kiem cho don vi
            String keywordForOrg = listValue.get(0);
            // Tu khoa tim kiem cho nhan vien
            String keywordForEmp = listValue.get(1);
            // Vi tri lay ra
            Long startRecord = null;
            String strStartRecord = listValue.get(2);
            if (!CommonUtils.isEmpty(strStartRecord)) {
                startRecord = Long.parseLong(strStartRecord);
            }
            // So luong lay ra
            Long pageSize = null;
            String strPageSize = listValue.get(3);
            if (!CommonUtils.isEmpty(strPageSize)) {
                pageSize = Long.parseLong(strPageSize);
            }
            RequestEmpConfigDAO requestEmpConfigDAO = new RequestEmpConfigDAO();
            List<EntityRequestEmpConfig> listRequestEmpConfig = requestEmpConfigDAO
                    .getListRequestEmpConfig(userGroup.getUserId2(), keywordForOrg,
                            keywordForEmp, startRecord, pageSize);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, listRequestEmpConfig, userGroup);
        } catch (Exception ex) {
            LOGGER.error("getListRequestEmpConfig - Exception - username: "
                    + userGroup.getCardId(), ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
}
