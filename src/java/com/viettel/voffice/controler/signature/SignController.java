/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler.signature;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import vn.viettel.core.sign.utils.Constant;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.controler.CommonControler;
import com.viettel.voffice.controler.SecurityControler;
import com.viettel.voffice.database.dao.document.TextSearchDAO;
import com.viettel.voffice.database.dao.file.AttachDAO;
import com.viettel.voffice.database.dao.sign.LogTranstionSignDAO;
import com.viettel.voffice.database.dao.sign.P12CertDAO;
import com.viettel.voffice.database.dao.sms.SmsDAO;
import com.viettel.voffice.database.dao.task.MissionSigningDAO;
import com.viettel.voffice.database.entity.EntityAttach;
import com.viettel.voffice.database.entity.EntityFileAttachment;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.User.EntityUser;
import com.viettel.voffice.database.entity.User.Vof2_EntityUser;
import com.viettel.voffice.database.entity.sign.EntityP12Cert;
import com.viettel.voffice.database.entity.sign.ErrSign;
import com.viettel.voffice.database.entity.task.EntityTask;
import com.viettel.voffice.database.entity.text.SignHashMulti;
import com.viettel.voffice.database.entity.text.SignMultiFile;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.FileUtils;
import com.viettel.voffice.utils.LogUtils;
import com.viettel.voffice.utils.SignUtils;
import com.viettel.voffice.database.entity.EntityMarkInfo;

/**
 *
 * @author thanght6
 */
public class SignController {

    // Log file
    private static final Logger LOGGER = Logger.getLogger(SignController.class);
    //pham vi xu ly van ban
    public static final Long TEXT_SCOPE = 2L;
    //TUnghd add pham vi DOng Dau van ban
    public static final Long DOC_SCOPE = 1L;
    public static final Long BRIEF_SCOPE = 1024L;
    // Ten cua class bao gom ca ten package
    private static final String CLASS_NAME = SignController.class.getName();

    /**
     * <b>Kiem tra trang thai ky van ban truoc khi ky<b></br>
     * Phuc vu cho viec ky bang SIM CA do ky bang SIM CA cham
     *
     * @author thanght6
     * @since May 17, 2016
     * @param request Doi tuong request~ tu client -> server
     * @param data Gia tri data truyen theo request
     * @param isSecurity Gia tri isSecurity truyen theo request
     *
     * @return
     */
    public String checkSigningStatusForText(HttpServletRequest request,
            String data, String isSecurity) {

        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);

        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("checkSigningStatusForText - No session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }

        // Neu user hoac user id tren he thong 1&2 null
        // -> Tra ve thong bao khong co quyen
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
//        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();

        // Id nguoi dung tren he thong 2
//        Long userId2 = user2.getUserId();
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        if ((user2 == null || user2.getUserId() == null)) {
            LOGGER.error("checkSigningStatusForText - user hoac userId tren he thong 1&2 null");
//            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, 1, aesKey);
        }
        // Id nguoi dung tren he thong 1
        Long userId2 = user2.getUserId();
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.TEXT_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Id van ban
            Long textId = Long.parseLong(listValue.get(0));

            LogTranstionSignDAO logTranstionSignDAO = new LogTranstionSignDAO();
            int result = 0;
            if (logTranstionSignDAO.checkSigningStatusForText(userId2, textId)) {
                result = 1;
            }
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);

        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error("search - userId: " + userId2, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * Thuc hien ky bang SIM CA cho file cong viec ca nhan
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String signTaskByCASIM(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String response;
        // Lay thong tin nguoi dung tu memcached theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("signByCASIM (Ky file giao viec/danh gia bang sim CA)"
                    + " - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Lay thong user tren he thong 1 va 2
        // Neu user null hoac user id null
        // -> Tra ve khong co quyen
//        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        if (user2 == null || user2.getUserId() == null) {
            LOGGER.error("signByCASIM (Ky file giao viec/danh gia bang sim CA)"
                    + " - user2 null hoac user2 id null");
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
            String[] keys = new String[]{
                ConstantsFieldParams.TYPE,
                ConstantsFieldParams.FILE_NAME,
                ConstantsFieldParams.FILE_PATH,
                ConstantsFieldParams.LIST_TASK,
                ConstantsFieldParams.PERIOD,
                ConstantsFieldParams.ORG_ID,
                ConstantsFieldParams.COMMENT,
                ConstantsFieldParams.STORAGE
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            int type = Integer.parseInt(listValue.get(0));
            // Ten file
            String fileName = listValue.get(1);
            String filePath = listValue.get(2);
            // Danh cong viec duoc ky
            String strListTask = listValue.get(3);
            List<EntityTask> listTask = null;
            if (!CommonUtils.isEmpty(strListTask)) {
                Gson gson = new Gson();
                Type listType = new TypeToken<ArrayList<EntityTask>>() {
                }.getType();
                listTask = gson.fromJson(strListTask, listType);
            }
            // Ky danh gia
            String period = listValue.get(4);
            // Id don vi nguoi duoc ky
            Long orgId = null;
            String srtOrgId = listValue.get(5);
            if (!CommonUtils.isEmpty(srtOrgId)) {
                orgId = Long.parseLong(srtOrgId);
            }
            // Y kien cua nguoi ky
            String comment = listValue.get(6);
            //Storate
            String storage = listValue.get(7);
            // Luu y kien cua nguoi ky vao tung cong viec
//            if (!CommonUtils.isEmpty(comment) && !CommonUtils.isEmpty(listTask)) {
//                for (EntityTask task : listTask) {
//                    task.setApprovalComment(comment);
//                }
//            }
            Long result = SignUtils.signTaskByCASIM(user2, type, fileName,
                    filePath, listTask, period, orgId, comment, storage);
            // Loi server
            if (result == null) {
                LOGGER.error("signByCASIM (Ky file giao viec/danh gia bang sim CA)"
                        + " - result = null - username: " + cardId);
                response = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            } // Dang thuc hien mot giao dich ky SIM CA khac
            else if (result.equals(-1L)) {
                LOGGER.error("signByCASIM (Ky file giao viec/danh gia bang sim CA)"
                        + " - result = -1 - Dang thuc hien mot giao dich ky SIM CA khac - username: " + cardId);
                response = FunctionCommon.generateResponseJSON(ErrorCode.EXIST_CA_SIM_SIGN_TRANSACTION, null, null);
            } // Da giao viec cho nhan vien trong thang
            else if (result.equals(-2L)) {
                LOGGER.error("signByCASIM (Ky file giao viec/danh gia bang sim CA)"
                        + " - result = -2 - Da giao viec cho nhan vien trong thang - username: " + cardId);
                response = FunctionCommon.generateResponseJSON(ErrorCode.ASSIGNED_TASK_FOR_USER, null, null);
            } // Da danh gia cong viec cho nhan vien trong thang
            else if (result.equals(-3L)) {
                LOGGER.error("signByCASIM (Ky file giao viec/danh gia bang sim CA)"
                        + " - result = -3 - Da danh gia cong viec cho nhan vien trong thang - username: " + cardId);
                response = FunctionCommon.generateResponseJSON(ErrorCode.ASSESSED_TASK_FOR_USER, null, null);
            } else {
                response = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
            }
        } catch (JSONException | NumberFormatException | JsonSyntaxException ex) {
            LOGGER.error("signByCASIM (Ky file giao viec/danh gia bang sim CA)"
                    + " - Exception - username: " + cardId, ex);
            response = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return response;
    }

    /**
     * Thuc hien ky mem cho phan cong viec
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String signSoftTask(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String response;
        // Lay thong tin user tu memcached theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("signSoft (Ky mem file giao viec/danh gia) - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Lay thong tin user tren he thong 1
        // Neu thong tin user la null
        // -> Tra ve thong bao khong co quyen
        EntityUser user1 = userGroup.getItemEntityUser();
//        if (user1 == null || user1.getUserId() == null) {
//            LOGGER.error("signSoft (Ky mem file giao viec/danh gia) - Khong lay"
//                    + " duoc thong tin user tren he thong 1");
//            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
//        }
        // Lay thong tin user tren he thong 2
        // Neu thong tin user la null
        // -> Tra ve thong bao khong co quyen
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        if (user2 == null || user2.getUserId() == null) {
            LOGGER.error("signSoft (Ky mem file giao viec/danh gia) - Khong lay"
                    + " duoc thong tin user tren he thong 2");
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
            String[] keys = new String[]{
                ConstantsFieldParams.STEP,
                ConstantsFieldParams.PUBLIC_KEY,
                ConstantsFieldParams.TYPE,
                ConstantsFieldParams.FILE_NAME,
                ConstantsFieldParams.FILE_PATH,
                ConstantsFieldParams.SIGNATURE,
                ConstantsFieldParams.LIST_TASK,
                ConstantsFieldParams.PERIOD,
                ConstantsFieldParams.ORG_ID,
                ConstantsFieldParams.COMMENT
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Buoc
            // 1: Lay hash file
            // 2: Dinh kem chu ky vao file
            int step = Integer.parseInt(listValue.get(0));
            // Public key de tim thong tin file cer cua user
            String publicKey = listValue.get(1);
            int type = Integer.parseInt(listValue.get(2));
            // Ten file
            String fileName = listValue.get(3);
            // Duong dan tuong doi cua file
            String filePath = listValue.get(4);
            // Chu ky
            String signature = listValue.get(5);
            // Danh sach cong viec duoc ky
            String strListTask = listValue.get(6);
            List<EntityTask> listTask = null;
            if (!CommonUtils.isEmpty(strListTask)) {
                Gson gson = new Gson();
                Type listType = new TypeToken<ArrayList<EntityTask>>() {
                }.getType();
                listTask = gson.fromJson(strListTask, listType);
            }
            // Ky danh gia
            String period = listValue.get(7);
            Long orgId = null;
            String srtOrgId = listValue.get(8);
            if (!CommonUtils.isEmpty(srtOrgId)) {
                orgId = Long.parseLong(srtOrgId);
            }
            // Y kien cua nguoi ky
            String comment = listValue.get(9);
            // Luu y kien cua nguoi ky vao tung cong viec            
//            if (!CommonUtils.isEmpty(comment) && !CommonUtils.isEmpty(listTask)) {
//                for (EntityTask task : listTask) {
//                    task.setApprovalComment(comment);
//                }
//            }
            // Check han chung thu va trang thai chung thu
            if (step == 1) {
                ErrSign itemCheckCer = SignUtils.checkCer(userGroup, publicKey, "");
                if (itemCheckCer.getErrCode() != null && !itemCheckCer.getErrCode().equals(1L)) {
                    String strErr = String.valueOf(itemCheckCer.getErrCode());
                    return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, strErr, aesKey);
                }
            }
            String result = SignUtils.signSoftTask(request, user1, user2, step,
                    publicKey, type, fileName, filePath, listTask, period, orgId,
                    signature, comment);
            if (result == null) {
                LOGGER.error("signSoft - result = null - Loi server");
                response = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            } else if ("-2".equals(result)) {
                LOGGER.error("signSoft - result = -2 - Da giao viec cho nhan vien trong thang");
                response = FunctionCommon.generateResponseJSON(ErrorCode.ASSIGNED_TASK_FOR_USER, null, null);
            } else if ("-3".equals(result)) {
                LOGGER.error("signSoft - result = -3 - Da danh gia cong viec cho nhan vien trong thang");
                response = FunctionCommon.generateResponseJSON(ErrorCode.ASSESSED_TASK_FOR_USER, null, null);
            } else {
                response = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
            }
        } catch (JSONException | NumberFormatException | JsonSyntaxException ex) {
            LOGGER.error("signSoft (Ky mem file giao viec/danh gia) - Exception"
                    + " - username: " + cardId, ex);
            response = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return response;
    }

    //========================thuc hien ky nhieu file===========================
    /**
     * <b>Hash danh sach file</b><br>
     *
     * @author datnv5
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws Throwable 
     */
    public String hashListFile(HttpServletRequest request, String data,
            String isSecurity) throws Throwable {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user tu memcached theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("hashListFile - No session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Neu user hoac user id tren he thong 1&2 null
        // -> Tra ve thong bao khong co quyen
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        if ((user1 == null || user1.getUserId() == null)
                && (user2 == null || user2.getUserId() == null)) {
            LOGGER.error("hashListFile - user hoac userId tren he thong 1&2 null");
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
                ConstantsFieldParams.DOCUMENTS,
                ConstantsFieldParams.CERTIFICATE,
                ConstantsFieldParams.COMMENT,
                ConstantsFieldParams.SIGN_TYPE,
                ConstantsFieldParams.SCOPE,
                ConstantsFieldParams.PUBLIC_KEY,
                ConstantsFieldParams.SEND_SMS,
                ConstantsFieldParams.DEVICE_NAME,
                ConstantsFieldParams.TEXT_PROCESS_ID,
                //Luu tru thong tin con dau
                "lstMarkInfo",
                "lstMark"
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Chuoi id
            String strDocuments = listValue.get(0);
            if (CommonUtils.isEmpty(strDocuments)) {
                LOGGER.error("hashListFile - userId1: " + userId1 + " - userId2: "
                        + userId2 + " - Chuoi id null hoac rong");
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
            // Chung thu
            String certificate = listValue.get(1);
            // Y kien
            String comment = listValue.get(2);
            // Loai ky
            String signType = listValue.get(3);
            // Pham vi
            Long scope = -1L;
            String strScope = listValue.get(4);
            if (!CommonUtils.isEmpty(strScope)) {
                scope = Long.parseLong(strScope);
            }
            String publicKey = listValue.get(5);
            // Co gui tin nhan cho lanh dao sau khi van thu xet duyet hay khong
            // Do Website Voffice luon gui tin nhan cho lanh dao sau khi van thu xet duyet
            int sendSMS = 1;
            String strSendSMS = listValue.get(6);
            if (!CommonUtils.isEmpty(strSendSMS)) {
                sendSMS = Integer.parseInt(strSendSMS);
            }
            // Ten thiet bi
            String deviceName = listValue.get(7);
            // 201812-Pitagon: add
            String textProcessId = listValue.get(8);
           
            //Tunghd add Thong tin con dau
            List<EntityMarkInfo> lstMarkInfo = new ArrayList<EntityMarkInfo>();
            String markInfo = listValue.get(9);
            if (!CommonUtils.isEmpty(markInfo)) {
                Gson gson =  new GsonBuilder().create();
                Type listMark = new TypeToken<ArrayList<EntityMarkInfo>>(){}.getType();
                lstMarkInfo = gson.fromJson(markInfo, listMark);
            }
            //End
            // Datdc start lstMark (toa do, thong tin dau)
            List<EntityMarkInfo> lstMarkDau = new ArrayList<EntityMarkInfo>();
            String mark = listValue.get(10);
            if (!CommonUtils.isEmpty(mark)) {
                Gson gson =  new GsonBuilder().create();
                Type listMarkTmp = new TypeToken<ArrayList<EntityMarkInfo>>(){}.getType();
                lstMarkDau = gson.fromJson(mark, listMarkTmp);
            }
            // Datdc end lstMark (toa do, thong tin dau)
            JSONObject objMapTextProcessId = new JSONObject();
            Map<Long, Long> mapTextProcessId = new HashMap<Long, Long>();
            if (!CommonUtils.isEmpty(textProcessId)) {
                objMapTextProcessId = new JSONObject(textProcessId);
            }

            // Kiem tra chung thu va thoi han
            ErrSign itemCheckCer = SignUtils.checkCer(userGroup, publicKey, certificate);
//            System.out.println("itemCheckCer:" + itemCheckCer);
            if (itemCheckCer.getErrCode() != null && itemCheckCer.getErrCode().equals(1L)) {
                // Pham vi la ky file van ban
                if (scope.equals(TEXT_SCOPE )|| scope.equals(DOC_SCOPE)) {
                    // Tao danh sach id van ban tu chuoi id
                    String[] arrTextId = strDocuments.split(Constants.Common.COMMA_CHAR);
                    Long textId;
                    // Duyet mang id van ban
                    Set<Long> textIdSet = new HashSet<Long>();
                    for (String strTextId : arrTextId) {
                        strTextId = strTextId.trim();
                        if (!CommonUtils.isEmpty(strTextId)) {
                            textId = Long.parseLong(strTextId);
                            textIdSet.add(textId);
                            // 201812-Pitagon: add
                            if (!CommonUtils.isEmpty(textProcessId)) {
                                mapTextProcessId.put(textId, objMapTextProcessId.getLong(strTextId));
                            }
                        }
                    }
                    List<Long> listTextId_Cache = new ArrayList<>(textIdSet);
                    if (CommonUtils.isEmpty(listTextId_Cache)) {
                        LOGGER.error("hashListFile - userId1: " + userId1 + " - userId2: "
                                + userId2 + " - listTextId null hoac rong");
                        return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                    }
                    //datnv5:check them dieu kien neu van ban do dang trong qua trinh ky lai
                    //thuc hien loai bo van ban dang trong qua trinh ky
                    List<Long>  listTextId = filterTextIdCeoDuringSign(userId2, listTextId_Cache);
                    if(listTextId==null || listTextId.size()<=0){
                        return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                                Constants.SIGN_RESULT_CODE.STATUS_DELAY_SIGN.toString(), aesKey);
                    }
					List<EntityAttach> listAttachment = new ArrayList<EntityAttach>();
                    AttachDAO attachDAO = new AttachDAO();
                    //Tunghd lay ra list file trinh ky + dinh kem
                    if (!CommonUtils.isEmpty(lstMarkInfo) && lstMarkInfo.get(0).getGroupTypeImage() != null) {
                        if (lstMarkInfo.get(0).getGroupTypeImage().equals(2L)) {
                            Long docId = lstMarkInfo.get(0).getDocumentId();
                            listAttachment = attachDAO.getEntityAttachInfo(userId2, docId);
                        } else if (lstMarkInfo.get(0).getGroupTypeImage().equals(1L)) {
                            listAttachment = attachDAO.getListAttachmentByTextId(listTextId, true);
                        }
                        // Tunghd end
                    } else {
                        List<EntityAttach> listAttachmentOld = attachDAO.getListAttachmentByTextId(listTextId, false);
                        
                        //datnv5: lay van ban da luu o bang luu tru neu la tong giam doc ky lai va chuan hoa file goc dau vao
                         listAttachment = 
                                attachDAO.getListAttachDuringSignAndUpdate(userId2,listTextId,listAttachmentOld);
                    }
                    // Datdc add lstMarkDau
                    List<SignMultiFile> resultHash = SignUtils.hashListFile(user1,
                            user2, publicKey, listAttachment, certificate, comment,
                            deviceName, signType, mapTextProcessId, lstMarkInfo, lstMarkDau);
                    if (!CommonControler.setSignSession(request, resultHash)) {
                        LOGGER.error("hashListFile - userId1: " + userId1 + " - userId2: "
                                + userId2 + " - Luu phien ky that bai");
                        return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                    } else {
                        // Luu thong tin signType len server thanh cong
                        if (CommonControler.setSessionAttribute(request,
                                ConstantsFieldParams.SIGN_TYPE, signType)) {
                        	// 201812-Pitagon: add
                            if (mapTextProcessId != null && !mapTextProcessId.isEmpty()) {
                                CommonControler.setSessionAttribute(request, ConstantsFieldParams.TEXT_PROCESS_ID, mapTextProcessId);
                            }
                        	
                            // Luu thong tin gui tin nhan cho lanh dao sau khi van thu xet duyet
                            CommonControler.setSessionAttribute(request,
                                    ConstantsFieldParams.SEND_SMS, sendSMS);
                            String strIpServer = "http://" + request.getLocalAddr()
                                    + ":" + String.valueOf(request.getLocalPort())
                                    + "/ServiceMobile_V02/resources/";
                            List<SignHashMulti> resultHashResponse = new ArrayList<SignHashMulti>();
                            SignHashMulti itemset;
                            for (SignMultiFile item : resultHash) {
                                itemset = new SignHashMulti();
                                itemset.setId(item.getId());
                                itemset.setHash(item.getHash());
                                itemset.setStrUrl(strIpServer);
                                itemset.setError(item.getCodeErr() == null
                                        ? null : item.getCodeErr().toString());
//                                System.out.println("textId: " + itemset.getId()
//                                        + " - hash: " + itemset.getHash()
//                                        + " - errorCode: " + itemset.getError());
                                resultHashResponse.add(itemset);
                                listTextId.remove(item.getTextId());
                            }
                            //datnv5: thuc hien set loi van ban dang trong qua trinh ky nen khong thuc hien hash
                            for (Long itemLongIdText : listTextId) {
                                itemset = new SignHashMulti();
                                itemset.setId(itemLongIdText);
                                itemset.setHash("");
                                itemset.setStrUrl(strIpServer);
                                itemset.setError("Loi file dang trong qua trinh xu ly cua lanh dao");
                                resultHashResponse.add(itemset);
                            }
                            // Ghi log ket thuc chuc nang
                            LogUtils.logFunctionalEnd(log);
                            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                                    resultHashResponse, aesKey);
                        } // Khong luu signType vao session duoc
                        else {
                            LOGGER.error("hashListFile - userId1: " + userId1
                                    + " - userId2: " + userId2 + " - Loi luu signType vao session");
                            return FunctionCommon.generateResponseJSON(
                                    ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                        }
                    }
                } // scope khong hop le
                else {
                    LOGGER.error("hashListFile - userId1: " + userId1
                            + " - userId2: " + userId2 + " - Loi scope khong hop le");
                    return FunctionCommon.generateResponseJSON(
                            ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
            } // Chung thu khong hop le hoac da het han
            else {
                LOGGER.error("hashListFile - userId1: " + userId1
                        + " - userId2: " + userId2 + " - Chung thu khong hop le hoac da het han");
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                        itemCheckCer.getErrCode(), aesKey);
            }
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error("hashListFile - userId1: " + userId1 + " - userId2: "
                    + userId2, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID,
                    null, null);
        } catch (Exception ex) {
            LOGGER.error("hashListFile - userId1: " + userId1 + " - userId2: "
                    + userId2 + " - Exception: " + ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Dinh chu ky vao danh sach file theo textid</b><br>
     *
     * @param request
     * @param strData
     * @param isSecurity
     * @return
     */
    public String appendSignatureIntoListFile(HttpServletRequest request,
            String strData, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Tao doi tuong ghi log
        String response;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        Boolean isCheckSs = userGroup.getCheckSessionOk();
        if (isCheckSs) {
            // Lay AES key
            String strAesKeyDecode = userGroup.getStrAesKey();
            // Lay gia tri client gui len
            HashMap<String, Object> hmParams = new HashMap<String, Object>();
            hmParams.put("signatures", String.class);
            hmParams.put(ConstantsFieldParams.DEVICE_NAME, String.class);
            HashMap<String, Object> valueParams = FunctionCommon
                    .getListParamsClient(hmParams, strData, userGroup);
            String strListSignHash = (String) valueParams.get("signatures");

            //Begin::Lay danh sach file duoc dinh kem khi ky::cuongnv
            if (isSecurity != null && "1".equals(isSecurity)) {
                // Lay AES Key
                strAesKeyDecode = userGroup.getStrAesKey();
                // Giai ma data client gui len
                strData = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
            }
            // Lay ma nhan vien
            String cardId = userGroup.getCardId();
            log.setUserName(cardId);
            // Ghi log bat dau chuc nang
            log.setParamList(strData);
            LogUtils.logFunctionalStart(log);
            List<EntityFileAttachment> strListFileSign = new ArrayList<>();
            JSONArray arrListFileSign = FunctionCommon.jsonGetArray(ConstantsFieldParams.REQ_LST_ATTACHMENT_FILES, strData);

            if (arrListFileSign != null && arrListFileSign.length() > 0) {
                for (int i = 0; i < arrListFileSign.length(); i++) {
                    try {
                        JSONObject innerObj = (JSONObject) arrListFileSign.get(i);
                        Long fileAttachmentId = null;
                        String name = "";
                        String filePath = "";

//                        if (innerObj.has(ConstantsFieldParams.TEXT_ATTACH_ID)) {
//                            fileAttachmentId = Long.parseLong(innerObj.getString(ConstantsFieldParams.TEXT_ATTACH_ID).trim());
//                        }
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
                    } catch (Exception ex) {
                        LOGGER.error("Error appendSignatureIntoListFile", ex);
                    }

                }
            }
            //End

            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<SignHashMulti>>() {
            }.getType();
            List<SignHashMulti> listSignHash = gson.fromJson(strListSignHash, listType);

            String deviceName = valueParams.get(ConstantsFieldParams.DEVICE_NAME) == null
                    ? "" : (String) valueParams.get(ConstantsFieldParams.DEVICE_NAME);
            List<SignHashMulti> result = SignUtils.appendSignatureIntoListFile(
                    request, listSignHash, strListFileSign, deviceName);
            if (result != null && result.size() > 0 && result.get(0).getError() != null
                    && result.get(0).getError().equals(String.valueOf(ErrorCode.TEXT_LOCK_SIGN_STATE.getErrorCode()))) {
                //neu la loi khoa ky song song
                response = FunctionCommon.generateResponseJSON(ErrorCode.TEXT_LOCK_SIGN_STATE, result, strAesKeyDecode);
            } else {
                response = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
            }
        } else {
            response = FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return response;
    }

    /**
     * <b>Ky sim CA cho file van ban trinh ky</b><br>
     *
     * @author thanght6
     * @since Jun 21, 2016
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String signTextByCASIM(HttpServletRequest request,
            String data, String isSecurity) {
        String[] keys = new String[]{
            ConstantsFieldParams.TEXT_ID,
            ConstantsFieldParams.TITLE,
            ConstantsFieldParams.COMMENT,
            ConstantsFieldParams.SIGNATURE_TYPE,
            ConstantsFieldParams.SEND_SMS,
            "classificationTypeAssessor"
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request,
                data, keys);
        if (!userGroup.getCheckSessionOk()) {
            //truong hop bị timeout
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        List<String> listValue = userGroup.getListParamsFromClient();
        // Id van ban
        Long textId = Long.parseLong(listValue.get(0));
        // Tieu de
        String title = listValue.get(1);
        // Y kien
        String comment = listValue.get(2);
        // Loai ky
        int signatureType = Integer.parseInt(listValue.get(3));
        // Gui tin nhan
        int sendSMS = 1;
        String strSendSMS = listValue.get(4);
        if (!CommonUtils.isEmpty(strSendSMS)) {
            sendSMS = Integer.parseInt(strSendSMS);
        }
        Integer classificationTypeAssessor = null;
        String strClassification = listValue.get(5);
        if (!CommonUtils.isEmpty(strClassification)) {
            classificationTypeAssessor = Integer.parseInt(strClassification);
        }
        Long userId2 = userGroup.getVof2_ItemEntityUser().getUserId();
        Long result = SignUtils.signTextByCASIM(userGroup, userId2, textId,
                title, comment, signatureType, sendSMS);
        if (result == null) {
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, userGroup);
        } else {
            // Khoa ky song song
            if (result.intValue() == ErrorCode.TEXT_LOCK_SIGN_STATE.getErrorCode()) {                
                return FunctionCommon.responseResult(ErrorCode.TEXT_LOCK_SIGN_STATE,
                        null, userGroup);
            } else {
                // Ky thanh cong
                if (result.equals(Constant.SIGN_RESULT_CODE.SUCCESS)) {
                    MissionSigningDAO missionSigningDAO = new MissionSigningDAO();
                    missionSigningDAO.update(userId2, textId, classificationTypeAssessor);
                }                
                return FunctionCommon.responseResult(ErrorCode.SUCCESS, result,
                        userGroup);
            }
        }
    }

    /**
     * <b>Lay thong tin chung thu: trang thai/co ma kich hoat hay chua</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getP12CertInformation(HttpServletRequest request,
            String data, String isSecurity) {
        String[] keys = new String[]{
            ConstantsFieldParams.PUBLIC_KEY
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request,
                data, keys);
        if (!userGroup.getCheckSessionOk()) {
            //truong hop bị timeout
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        List<String> listValue = userGroup.getListParamsFromClient();
        // Public key
        String publicKey = listValue.get(0);

        if (CommonUtils.isEmpty(publicKey)) {
            LOGGER.error("getP12CertInformation - sai tham so dau vao, client ko gui len public key");
            return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, userGroup);
        }
        EntityUser user1 = userGroup.getItemEntityUser();
        Long userId1 = null;
        if (user1 != null) {
            userId1 = user1.getUserId();
        }
        P12CertDAO certDao = new P12CertDAO();
        EntityP12Cert result = certDao.getP12CertificateOfUser(userId1,
                userGroup.getUserId2(), publicKey);
        if (result == null) {
            LOGGER.error("getP12CertInformation - result = null - Loi server");
            return FunctionCommon.responseResult(ErrorCode.ERR_NODATA, null, userGroup);
        }
        return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
    }

    /**
     * <b>Yeu cau thiet lap lai mat khau chung thu</b>
     *
     * @author thanght6
     * @since Aug 17, 2016
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String requestResetCertificatePassword(HttpServletRequest request,
            String data, String isSecurity) {
        String[] keys = new String[]{
            ConstantsFieldParams.PUBLIC_KEY
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request,
                data, keys);
        if (!userGroup.getCheckSessionOk()) {
            //truong hop bị timeout
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        List<String> listValue;
        listValue = userGroup.getListParamsFromClient();
        // Public key cua chung thu
        String publicKey = listValue.get(0);
        Long userId1 = userGroup.getUserId1();
        Long userId2 = userGroup.getUserId2();
        // Lay thong tin chung thu
        P12CertDAO p12CertDAO = new P12CertDAO();
        EntityP12Cert p12Cert = p12CertDAO.getP12CertificateOfUser(userId1,
                userId2, publicKey);
        // Neu doi tuong chung thu null hoac id null
        // -> Tra ve loi chung thu khong ton tai
        if (p12Cert == null || p12Cert.getP12CertId() == null) {
            LOGGER.error("requestResetCertificatePassword - userId1: " + userId1
                    + " - Loi chung thu khong ton tai");
            return FunctionCommon.responseResult(ErrorCode.CERTIFICATE_NOT_EXIST,
                    null, userGroup);
        }
        // Sinh ma OTP
        int otpCode = CommonUtils.generateOTPCode();
        // Neu cap nhat Database that bai -> Tra ve loi server
        if (!p12CertDAO.updateResetPasswordOTPAndCount(p12Cert.getP12CertId(),
                String.valueOf(otpCode), 0)) {
            LOGGER.error("requestResetCertificatePassword - userId1: " + userId1
                    + " - Loi cap nhat ma OTP de thiet lap lai mat khau cho chung thu");
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, userGroup);
        }
        // Lay noi dung tin nhan tu bang cau hinh
        CommonControler commonControler = new CommonControler();
        List<String> params = new ArrayList<>();
        params.add(otpCode + "");
        String message = commonControler.getStrMessConfig(params, 1L,
                Constants.SMS_TEXT_CONFIG.OTP_CODE_FOR_RESET_CERTIFICATE_PASSWORD, userId2);
        // Loai bo dau trong noi dung tin nhan
        message = FunctionCommon.removeUnsign(message);
        // Luu vao bang tin nhan
        SmsDAO smsDAO = new SmsDAO();
        String strMobile = (userGroup.getVof2_ItemEntityUser() != null)
                ? userGroup.getVof2_ItemEntityUser().getMobileNumber() : "";
        //smsDAO.addMessToTableMess(0L, userId1, null, message, 1);
        if (!smsDAO.addMessToTableMessVof2(0L, userId2, CommonControler.getSMSMobile(strMobile), message, 1, 700L)) {
            LOGGER.error("requestResetCertificatePassword + userId1: " + userId2
                    + " - Loi gui tin nhan ma OTP de reset mat khau chung thu");
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, userGroup);
        }
        return FunctionCommon.responseResult(ErrorCode.SUCCESS,
                p12Cert.getP12CertId(), userGroup);
    }

    /**
     * <b>Xac nhan ma OTP de reset mat khau chung thu</b><br>
     *
     * @author thanght6
     * @since Aug 19, 2016
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String confirmOTPCodeToResetCertificatePassword(HttpServletRequest request,
            String data, String isSecurity) {

        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("confirmOTPCodeToResetCertificatePassword - No session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Neu user hoac user id tren he thong 1&2 null
        // -> Tra ve thong bao khong co quyen
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        if ((user1 == null || user1.getUserId() == null)
                && (user2 == null || user2.getUserId() == null)) {
            LOGGER.error("confirmOTPCodeToResetCertificatePassword - user hoac "
                    + "userId tren he thong 1&2 null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Id nguoi dung tren he thong 1
        Long userId1 = userGroup.getUserId1();
        // Id nguoi dung tren he thong 2
        Long userId2 = userGroup.getUserId2();
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.PUBLIC_KEY,
                ConstantsFieldParams.OTP_CODE
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Public key
            String publicKey = listValue.get(0);
            // Ma OTP
            String otpCode = listValue.get(1);
            // Lay thong tin chung thu
            P12CertDAO p12CertDAO = new P12CertDAO();
            EntityP12Cert p12Cert = p12CertDAO.getP12CertificateOfUser(userId1,
                    userId2, publicKey);
            if (p12Cert == null || p12Cert.getP12CertId() == null) {
                LOGGER.error("confirmOTPCodeToResetCertificatePassword - userId1: "
                        + userId1 + " - Loi chung thu khong ton tai");
                return FunctionCommon.generateResponseJSON(ErrorCode.CERTIFICATE_NOT_EXIST,
                        null, null);
            }
            Long p12CertId = p12Cert.getP12CertId();
            String resetPasswordOTP = p12Cert.getResetPasswordOTP();
            Integer resetPasswordCount = p12Cert.getResetPasswordCount() == null
                    ? 0 : p12Cert.getResetPasswordCount();
            // Neu khong co ma OTP -> Yeu cau nguoi dung lay ma OTP
            if (CommonUtils.isEmpty(resetPasswordOTP)) {
                LOGGER.error("confirmOTPCodeToResetCertificatePassword - userId1: " + userId1
                        + " - Loi chua co ma OTP");
                return FunctionCommon.generateResponseJSON(ErrorCode.CERTIFICATE_OTP_NOT_EXIST,
                        null, null);
            }
            // Ma OTP dung va so lan nhap sai nho hon 5
            if (!CommonUtils.isEmpty(otpCode) && otpCode.equals(resetPasswordOTP)
                    && resetPasswordCount < 5) {
                // Sinh mat khau ngau nhien
                String password = CommonUtils.randomPassword();
                if (CommonUtils.isEmpty(password)) {
                    LOGGER.error("confirmOTPCodeToResetCertificatePassword - userId1: "
                            + userId1 + " - Loi sinh mat khau chung thu");
                    return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                            null, null);
                }
                // Xoa ma OTP trong database
                if (!p12CertDAO.updateResetPasswordOTPAndCount(p12CertId, "", 0)) {
                    LOGGER.error("confirmOTPCodeToResetCertificatePassword - userId1: "
                            + userId1 + " - Loi xoa ma OTP reset mat khau chung thu");
                    return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                            null, null);
                }
                // Lay noi dung tin nhan tu bang cau hinh
                CommonControler commonControler = new CommonControler();
                List<String> params = new ArrayList<>();
                params.add(password);
                String message = commonControler.getStrMessConfig(params, 1L,
                        Constants.SMS_TEXT_CONFIG.NEW_CERTIFICATE_PASSWORD, userId2);
                // Loai bo dau trong noi dung tin nhan
                message = FunctionCommon.removeUnsign(message);
                // Luu vao bang tin nhan
                SmsDAO smsDAO = new SmsDAO();
                String strMobile = (userGroup.getVof2_ItemEntityUser() != null)
                        ? userGroup.getVof2_ItemEntityUser().getMobileNumber() : "";
                //smsDAO.addMessToTableMess(0L, userId1, null, message, 1)
                if (!smsDAO.addMessToTableMessVof2(0L, userId2, CommonControler.getSMSMobile(strMobile), message, 1, 700L)) {
                    LOGGER.error("confirmOTPCodeToResetCertificatePassword - userId1: "
                            + userId1 + " - Loi gui tin nhan mat khau moi");
                    return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                            null, null);
                }
                // Tra ve mat khau moi
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                        password, aesKey);
            } // Ma OTP sai hoac so lan nhap sai bang 5
            else {
                // Tang so lan nhap sai len 1
                resetPasswordCount++;
                // Cap nhat so lan nhap sai ma OTP
                p12CertDAO.updateResetPasswordOTPAndCount(p12CertId, null,
                        resetPasswordCount);
                // So lan nhap sai nho hon 5
                if (resetPasswordCount < 5) {
                    // Thong bao nhap sai ma OTP
                    return FunctionCommon.generateResponseJSON(ErrorCode.CERTIFICATE_OTP_INCORRECT, resetPasswordCount, aesKey);
                } // So lan nhap sai lon hon hoac bang 5
                else {
                    // Cap nhat trang thai chung thu da bi thu hoi
                    p12CertDAO.updateStatus(p12CertId, new Long(Constants.P12Cert.Status.REVOKED));
                    // Thong bao chung thu bi thu hoi do nhap sai ma OTP 5 lan
                    return FunctionCommon.generateResponseJSON(ErrorCode.CERTIFICATE_OTP_EXCEED_INCORRECT_LIMIT, null, null);
                }
            }
        } // Dinh dang data khong phai JSONObject
        // Hoac value trong doi tuong JSON khong dung dinh dang quy dinh
        catch (JSONException | NumberFormatException ex) {
            LOGGER.error("confirmOTPCodeToResetCertificatePassword - userId1: "
                    + userId1, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID,
                    null, null);
        } // Loi Server
        catch (Exception ex) {
            LOGGER.error("confirmOTPCodeToResetCertificatePassword - userId: "
                    + userId1, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    /**
     * <b>WEB: Ky phieu giao viec/Ky phieu danh gia</b>
     *
     * @author HaNH
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
//    public String signMultiFileTask(HttpServletRequest request,
//            String data) {
//        String isSecurity = "1";
//        EntityLog log = new EntityLog(request, CLASS_NAME);
//        String response = null;
//        // Lay thong tin user tu memcached theo sessionId cua request
//        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
//        // Khong co session
//        if (!userGroup.getCheckSessionOk()) {
//            LOGGER.error("signMultiFileTask (Ky file giao viec/danh gia tren WEB) - Session timeout!");
//            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
//        }
//        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
//        if (user2 == null || user2.getUserId() == null) {
//            LOGGER.error("signMultiFileTask (Ky file giao viec/danh gia tren WEB) - Khong lay"
//                    + " duoc thong tin user tren he thong 2");
//            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
//        }
//        //ID user tren he thong 2
//        Long userId2 = user2.getUserId();
//        // Giai ma du lieu neu ma hoa
//        String aesKey = null;
//        if (isSecurity != null && "1".equals(isSecurity)) {
//            // Lay AES Key
//            aesKey = userGroup.getStrAesKey();
//            // Giai ma data client gui len
//            data = SecurityControler.decodeDataByAes(aesKey, data);
//        }
//        // Lay ma nhan vien
//        String cardId = userGroup.getCardId();
//        log.setUserName(cardId);
//        // Ghi log bat dau chuc nang
//        log.setParamList(data);
//        LogUtils.logFunctionalStart(log);
//        try {
//            JSONObject json = new JSONObject(data);
//            String[] keys = new String[]{
//                ConstantsFieldParams.STEP,
//                ConstantsFieldParams.PUBLIC_KEY,
//                ConstantsFieldParams.TYPE,
//                ConstantsFieldParams.SIGN_TYPE,
//                ConstantsFieldParams.PERIOD,
//                ConstantsFieldParams.ORG_ID,
//                ConstantsFieldParams.COMMENT,
//                ConstantsFieldParams.CERTIFICATE,
//                ConstantsFieldParams.SIGNATURES
//            };
//            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
//            // Buoc
//            // 1: Lay hash file
//            // 2: Dinh kem chu ky vao file
//            int step = Integer.parseInt(listValue.get(0));
//            // Public key de tim thong tin file cer cua user
//            String publicKey = listValue.get(1);
//            int type = Integer.parseInt(listValue.get(2));
//            // Loai ky
//            Integer signType = Integer.parseInt(listValue.get(3));
//            // Ky danh gia
//            String period = listValue.get(4);
//            Long orgId = null;
//            if (FunctionCommon.isNumeric(listValue.get(5))) {
//                orgId = Long.parseLong(listValue.get(5));
//            }
//            // Y kien ky
//            String comment = listValue.get(6);
//            //Chung thu
//            String certificate = listValue.get(7);
//            //Chu ky
//            String signature = listValue.get(8);
//            JSONArray arrFileSign = FunctionCommon.jsonGetArray("listFileSign", data);
//            System.out.println("arrFileSignarrFileSignarrFileSign="+arrFileSign.toString());
//            List<SignMultiFile> listFileSign = new LinkedList<>();
//            List<SignHashMulti> listSignHash = new LinkedList<>();
//            if (step == 1) {
//                if (arrFileSign != null && arrFileSign.length() > 0) {
//                    SignMultiFile fileSignItem;
//                    for (int i = 0; i < arrFileSign.length(); i++) {
//                        //Get object cua tung doi tuong mang
//                        JSONObject innerObj = (JSONObject) arrFileSign.get(i);
//                        String[] keysObj = new String[]{
//                            "index",
//                            "fileName",
//                            "enforcementId",
//                            "filePath"
//                        };
//                        List<String> listValueObj = FunctionCommon.getValuesFromJSON(innerObj, keysObj);
//                        fileSignItem = new SignMultiFile();
//                        
//                        Long index = null;
//                        if(FunctionCommon.isNumeric(listValueObj.get(0))){
//                            index = Long.parseLong(listValueObj.get(0));
//                            fileSignItem.setId(index);
//                        }
//                        String fileName = listValueObj.get(1);
//                        fileSignItem.setFileSign(fileName);
//                        Long enforcementId = null;
//                        if(FunctionCommon.isNumeric(listValueObj.get(2))){
//                            enforcementId = Long.parseLong(listValueObj.get(2));
//                            fileSignItem.setEnforcementId(enforcementId);
//                        }
//                        String filePath = listValueObj.get(3);
//                        fileSignItem.setFilePath(filePath);
//                        listFileSign.add(fileSignItem);
//                    }
//                }
//            } else if (step == 2) {
////                System.out.println("signature: "+signature);
//                if (signature != null && !"".equals(signature)) {
////                    System.out.println("Signature is not null");
//                    Gson gson = new Gson();
//                    Type listType = new TypeToken<ArrayList<SignHashMulti>>() {
//                    }.getType();
//                    listSignHash = gson.fromJson(signature, listType);
//                } else {
////                    System.out.println("Signature is null");
//                    if (arrFileSign != null && arrFileSign.length() > 0) {
//                        SignMultiFile fileSignItem;
//                        for (int i = 0; i < arrFileSign.length(); i++) {
//                            //Get object cua tung doi tuong mang
//                            JSONObject innerObj = (JSONObject) arrFileSign.get(i);
//                            String[] keysObj = new String[]{
//                                "index",
//                                "fileName",
//                                "enforcementId",
//                                "filePath"
//                            };
//                            List<String> listValueObj = FunctionCommon.getValuesFromJSON(innerObj, keysObj);
//                            fileSignItem = new SignMultiFile();
//                            try {
//                                Long index = null;
//                                if(FunctionCommon.isNumeric(listValueObj.get(0))){
//                                    index = Long.parseLong(listValueObj.get(0));
//                                    fileSignItem.setId(index);
//                                }
//                                String fileName = listValueObj.get(1);
//                                fileSignItem.setFileSign(fileName);
//                                Long enforcementId = null;
//                                if(FunctionCommon.isNumeric(listValueObj.get(2))){
//                                    enforcementId = Long.parseLong(listValueObj.get(2));
//                                    fileSignItem.setEnforcementId(enforcementId);
//                                }
//                                String filePath = listValueObj.get(3);
//                                fileSignItem.setFilePath(filePath);
//                                List<EntityTask> listTask = new LinkedList<>();
//                                if (innerObj.has("listTask")) {
//                                    JSONArray arrTask = FunctionCommon.jsonGetArray("listTask", innerObj.toString());
//                                    if (arrTask != null && arrTask.length() > 0) {
//                                        EntityTask taskItem;
//                                        for (int j = 0; j < arrTask.length(); j++) {
//                                            //Get object cua tung doi tuong mang
//                                            JSONObject objTask = (JSONObject) arrTask.get(j);
//                                            String[] keysTaskRating = new String[]{
//                                                "taskId",
//                                                "approvalState",
//                                                "ratingPoint",
//                                                "completedStatus",
//                                                "ratingQuality",
//                                                "ratingType",
//                                                "ratingPointEmp",
//                                                "ratingQualityEmp",
//                                                "commanderId",
//                                                "enforcementId"
//                                            };
//                                            List<String> listValueTaskRating = FunctionCommon.getValuesFromJSON(objTask, keysTaskRating);
//                                            taskItem = new EntityTask();
//                                            Long taskId = null;
//                                            if(FunctionCommon.isNumeric(listValueTaskRating.get(0))){
//                                                taskId = Long.parseLong(listValueTaskRating.get(0));
//                                                taskItem.setTaskId(taskId);
//                                            }
//                                            Long approvalState = null;
//                                            if(FunctionCommon.isNumeric(listValueTaskRating.get(1))){
//                                                approvalState = Long.parseLong(listValueTaskRating.get(1));
//                                                taskItem.setApprovalState(approvalState);
//                                                if (objTask.has("ratingPoint")) {
//                                                    Double ratingPoint = objTask.getDouble("ratingPoint");
//                                                    taskItem.setRatingPoint(ratingPoint);
//                                                }
//                                                if (objTask.has("completedStatus")) {
//                                                    Long completeStatus = objTask.getLong("completedStatus");
//                                                    taskItem.setCompletedStatus(completeStatus);
//                                                }
//                                                if (objTask.has("ratingQuality")) {
//                                                    Long ratingQuality = objTask.getLong("ratingQuality");
//                                                    taskItem.setRatingQuality(ratingQuality);
//                                                }
//                                                if (objTask.has("ratingType")) {
//                                                    Long ratingType = objTask.getLong("ratingType");
//                                                    taskItem.setRatingType(ratingType);
//                                                }
//                                                if (objTask.has("ratingPointEmp")) {
//                                                    Double ratingPointEmp = objTask.getDouble("ratingPointEmp");
//                                                    taskItem.setRatingPointEmp(ratingPointEmp);
//                                                }
//                                                 if (objTask.has("ratingQualityEmp")) {
//                                                    Long ratingQualityEmp = objTask.getLong("ratingQualityEmp");
//                                                    taskItem.setRatingQualityEmp(ratingQualityEmp);
//                                                }
//                                                if (objTask.has("commanderId")) {
//                                                   Long commanderId = objTask.getLong("commanderId");
//                                                   taskItem.setCommanderId(commanderId);
//                                                }
//                                                
//                                            }
//                                            Double ratingPoint;
//                                            if(FunctionCommon.isNumeric(listValueTaskRating.get(2))){
//                                                ratingPoint = Double.parseDouble(listValueTaskRating.get(2));
//                                                taskItem.setRatingPoint(ratingPoint);
//                                            }
//                                            Long completedStatus = null;
//                                            if(FunctionCommon.isNumeric(listValueTaskRating.get(3))){
//                                                completedStatus = Long.parseLong(listValueTaskRating.get(3));
//                                                taskItem.setCompletedStatus(completedStatus);
//                                            }
//                                            Long ratingQuality = null;
//                                            if(FunctionCommon.isNumeric(listValueTaskRating.get(4))){
//                                                ratingQuality = Long.parseLong(listValueTaskRating.get(4));
//                                                taskItem.setRatingQuality(ratingQuality);
//                                            }
//                                            Long ratingType = null;
//                                            if(FunctionCommon.isNumeric(listValueTaskRating.get(5))){
//                                                ratingType = Long.parseLong(listValueTaskRating.get(5));
//                                                taskItem.setRatingType(ratingType);
//                                            }
//                                            Double ratingPointEmp = null;
//                                            if(FunctionCommon.isNumeric(listValueTaskRating.get(6))){
//                                                ratingPointEmp = Double.parseDouble(listValueTaskRating.get(6));
//                                                taskItem.setRatingPointEmp(ratingPointEmp);
//                                            }
//                                            Long ratingQualityEmp = null;
//                                            if(FunctionCommon.isNumeric(listValueTaskRating.get(7))){
//                                                ratingQualityEmp = Long.parseLong(listValueTaskRating.get(7));
//                                                taskItem.setRatingQualityEmp(ratingQualityEmp);
//                                            }
//                                            Long commanderId = null;
//                                            if(FunctionCommon.isNumeric(listValueTaskRating.get(8))){
//                                                commanderId = Long.parseLong(listValueTaskRating.get(8));
//                                                taskItem.setCommanderId(commanderId);
//                                            }
//                                            Long enforcementIdTask = null;
//                                            if(FunctionCommon.isNumeric(listValueTaskRating.get(9))){
//                                                enforcementIdTask = Long.parseLong(listValueTaskRating.get(9));
//                                                taskItem.setEnforcementId(enforcementIdTask);
//                                            }
//                                            listTask.add(taskItem);
//                                        }
//                                    }
//                                }
//                                fileSignItem.setListTask(listTask);
//                                listFileSign.add(fileSignItem);
//                            } catch (JSONException e) {
//                                LOGGER.error("Loi! JSONException", e);
//                            }
//                        }
//                    }
//                }
//            }
//            if (signType.equals(0)) {
//                //Neu la ky thuong thi update truc tiep vao DB
////                System.out.println("Ky thuong");
//                if (CommonUtils.isEmpty(listFileSign)) {
//                    LOGGER.error("signMultiFileTask (Ky file giao viec/danh gia tren WEB) - Error"
//                            + " listFileSign bi null ");
//                    response = FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
//                } else {
////                    System.out.println("Ky thuong phieu danh gia:");
//                    for (SignMultiFile fileSign : listFileSign) {
//                        String filePath = fileSign.getFilePath();
//                        String fileName = fileSign.getFileSign();
//                        List<EntityTask> listTask = fileSign.getListTask();
//                        //Duong dan storage luu tru file
//                        String keyStore = "task_export";
//                        String storage = FunctionCommon.getPropertiesValue(keyStore);
//                        fileSign.setStorageFileSign(storage);
//                        // Duong dan tuyet doi cua file goc
//                        String originFilePath = storage + File.separator + filePath;
//                        originFilePath = FileUtils.getSafePath(originFilePath);
//                        // Lay ra file truoc khi ky
//                        File file = new File(originFilePath);
//                        if (!file.exists()) {
//                            LOGGER.error("signMultiFileTask (Ky file giao viec/danh gia tren WEB) - Error"
//                                    + " File ky khong ton tai " + originFilePath);
//                            return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, null, null);
//                        }
//                        Boolean result = SignUtils.insertRecordAfterSignSuccess(type, userId2, fileName, filePath,
//                                listTask, period, orgId, comment, signType, keyStore);
//                        response = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
//                    }
//                }
//            } else if (signType.equals(2)) {
////                System.out.println("Ky USB");
////                System.out.println("step "+step );
//                List<SignMultiFile> result = SignUtils.signUsbAndSoftTask(request, user2, step,
//                        publicKey, type, listFileSign, period, orgId, comment,
//                        certificate, signType, listSignHash);
//                if (result == null) {
//                    LOGGER.error("signUsb - result = null - Ky USB that bai!");
//                    response = FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, null, null);
//                } else {
//                    if (step == 1) {
//                        if (!CommonControler.setSignSession(request, result)) {
//                            LOGGER.error("hashListFile - userId2: "
//                                    + userId2 + " - Luu phien ky that bai");
//                            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
//                        } else {
//                            // Luu thong tin signType len server thanh cong
//                            if (CommonControler.setSessionAttribute(request,
//                                    ConstantsFieldParams.SIGN_TYPE, signType)) {
//                                String strIpServer = "http://" + request.getLocalAddr()
//                                        + ":" + String.valueOf(request.getLocalPort())
//                                        + "/ServiceMobile_V02/resources/";
//                                List<SignHashMulti> resultHashResponse = new ArrayList<SignHashMulti>();
//                                SignHashMulti itemset;
//                                for (SignMultiFile item : result) {
//                                    itemset = new SignHashMulti();
//                                    itemset.setId(item.getId());
//                                    itemset.setHash(item.getHash());
//                                    itemset.setStrUrl(strIpServer);
//                                    itemset.setError(item.getCodeErr() == null
//                                            ? null : item.getCodeErr().toString());
//                                    resultHashResponse.add(itemset);
//                                }
//                                // Ghi log ket thuc chuc nang
//                                LogUtils.logFunctionalEnd(log);
//                                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
//                                        resultHashResponse, aesKey);
//                            } // Khong luu signType vao session duoc
//                            else {
////                                LOGGER.error("hashListFile - userId2: " + userId2 + " - Loi luu signType vao session");
//                                return FunctionCommon.generateResponseJSON(
//                                        ErrorCode.INTERNAL_SERVER_ERROR, null, null);
//                            }
//                        }
//                    } else if (step == 2) {
//                        if (CommonUtils.isEmpty(listFileSign)) {
//                            //Buoc tra ve doi tuong ky sau khi dinh kem chu ky
//                            List<SignHashMulti> resultHashResponse = new ArrayList<>();
//                            SignHashMulti itemset;
//                            for (SignMultiFile item : result) {
//
//                                itemset = new SignHashMulti();
//                                itemset.setId(item.getId());
//                                itemset.setError(item.getCodeErr() == null
//                                        ? null : item.getCodeErr().toString());
//                                itemset.setMess(item.getMssErr());
//                                resultHashResponse.add(itemset);
//                            }
//                            // Ghi log ket thuc chuc nang
//                            LogUtils.logFunctionalEnd(log);
//                            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, resultHashResponse, aesKey);
//                        } else {
//                            Boolean isSucess = true;
//                            //Neu ma loi tra ve la -1 thi buoc update databse bi that bai
//                            for (SignMultiFile item : result) {
//                                if (item.getCodeErr().equals(-1L)) {
//                                    isSucess = false;
//                                    break;
//                                }
//                            }
//                            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, isSucess, aesKey);
//                        }
//                    }
//                }
//
//            } else {
////                LOGGER.error("signMultiFileTask (Ky file giao viec/danh gia tren WEB) - Error"
////                        + " Loai ky signType khong hop le ");
//                response = FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
//            }
//        } catch (JSONException | NumberFormatException | JsonSyntaxException ex) {
//            LOGGER.error("signMultiFileTask (Ky file giao viec/danh gia tren WEB) - Exception"
//                    + " - username: " + cardId, ex);
//            response = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
//        }
//        // Ghi log ket thuc chuc nang
//        LogUtils.logFunctionalEnd(log);
//        return response;
//    }
    
    
    
    /**
     * ky nhieu file cong viec
     * @param request
     * @param data
     * @return 
     */
    public String signMultiFileTask(HttpServletRequest request,
            String data) {
        String[] keys = new String[]{
                ConstantsFieldParams.STEP,
                ConstantsFieldParams.PUBLIC_KEY,
                ConstantsFieldParams.TYPE,
                ConstantsFieldParams.SIGN_TYPE,
                ConstantsFieldParams.PERIOD,
                ConstantsFieldParams.ORG_ID,
                ConstantsFieldParams.COMMENT,
                ConstantsFieldParams.CERTIFICATE,
                ConstantsFieldParams.SIGNATURES,
                "listFileSign"}; 
         
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request,
                data, keys);
        
        if (!userGroup.getCheckSessionOk()) {
            //truong hop khong co session
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(),
                    null, null);
        }
        
        //lay du lieu client truyen len
        List<String> listValue = userGroup.getListParamsFromClient();
        //cac buoc ky
        int step = (FunctionCommon.isNumeric(listValue.get(0)))? 
                Integer.parseInt(listValue.get(0)): 2;
        String publicKey = listValue.get(1);
        int type = Integer.parseInt(listValue.get(2));
        //loai ky: 0 - khong su dung thiet bi, 1 usb , 2 ky mem , 3 ky sim
        int signType = FunctionCommon.isNumeric(listValue.get(3))?
                Integer.parseInt(listValue.get(3)):0;
        // Ky danh gia
        String period = listValue.get(4);
        Long orgId = null;
        if (FunctionCommon.isNumeric(listValue.get(5))) {
            orgId = Long.parseLong(listValue.get(5));
        }
        // Y kien ky
        String comment = listValue.get(6);
        String certificate = listValue.get(7);
        //Chu ky
        String signature = listValue.get(8);
        String strJsonListFileSign = listValue.get(9);
        //thuc hien lay thong tin nguoi dung trong he thong
        Long userId2 = userGroup.getUserId2();
        
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        String response = "";
        Boolean result = false;
        List<SignMultiFile> listFileSign = getListFileSignFromClient(
                        strJsonListFileSign, true);
        switch (signType) {
            // Ky thuong (Chi cap nhat database, khong them chung thu vao file ky)
            case 0:
                //Duong dan storage luu tru file
                String keyStore = "task_export";
                String storage = FunctionCommon.getPropertiesValue(keyStore);
                //thuc hien ky khong su dung thiet bi ky dien tu
                for (SignMultiFile fileSign : listFileSign) {
                    String filePath = fileSign.getFilePath();
                    String fileName = fileSign.getFileSign();
                    List<EntityTask> listTask = fileSign.getListTask();
                    //kiem tra xem file co ton tai khong
                    // Duong dan tuyet doi cua file goc
                    String originFilePath = storage + File.separator + filePath;
                    originFilePath = FileUtils.getSafePath(originFilePath);
                    File file = new File(originFilePath);
                    if (file.exists()) {
                        result = SignUtils.insertRecordAfterSignSuccess(type, userId2, fileName, filePath,
                                listTask, period, orgId, comment, signType, keyStore);
                    }else{
                        //loi trong qua trinh phe duyet cong viec
                        result = false;
                    }
                }
                response = FunctionCommon.responseResult(ErrorCode.SUCCESS,
                    result, userGroup);
                //thuc hien tra ket qua 
                break;
            // Thuc hien ky mem
            case 1:
            // Thuc hien ky usb    
            case 2:
                List<SignHashMulti> listSignHash; 
                if(signature.trim().length() > 0){
                    listSignHash =  (List<SignHashMulti>) 
                            FunctionCommon.convertJsonToListObject(signature, SignHashMulti.class);
                }else{
                    listSignHash = null;
                }
                List<SignMultiFile> listResultSign = SignUtils.signUsbAndSoftTask(
                        request, user2, step, publicKey, type, listFileSign, period,
                        orgId, comment, certificate, signType, listSignHash);
                if (step == 1) {
                    // Luu doi tuong ky vao session that bai
                    if (!CommonControler.setSignSession(request, listResultSign)) {
                        result = false;
                        response = FunctionCommon.responseResult(ErrorCode.SUCCESS,
                        result, userGroup);
                    } // Luu doi tuong ky vao session thanh cong
                    else {
                        String strIpServer = "http://" + request.getLocalAddr()
                                        + ":" + String.valueOf(request.getLocalPort())
                                        + "/ServiceMobile_V02/resources/";
                        List<SignHashMulti> resultHashResponse = new ArrayList<>();
                        SignHashMulti itemset;
                        for (SignMultiFile item : listResultSign) {
                            itemset = new SignHashMulti();
                            itemset.setId(item.getId());
                            itemset.setHash(item.getHash());
                            itemset.setStrUrl(strIpServer);
                            itemset.setError(item.getCodeErr() == null
                                    ? null : item.getCodeErr().toString());
                            resultHashResponse.add(itemset);
                        }
                        response = FunctionCommon.responseResult(ErrorCode.SUCCESS,
                        resultHashResponse, userGroup);
                    }
                } // Thuc hien append chu ky vao file
                else if (step == 2) {
                    
                    Boolean haveTask = true;
                    for (SignMultiFile signMultiFile : listFileSign) {
                        //trong danh sach file ky co cong viec thi danh dau la update cong viec
                        if(CommonUtils.isEmpty(signMultiFile.getListTask())){
                            haveTask = false;
                            break;
                        }else{
                            haveTask = true;
                        }
                    }                    
                    if (!CommonUtils.isEmpty(listSignHash) && !haveTask) {
                        //thuc hien tra ket qua khi dinh chu ky ve cho web
                        List<SignHashMulti> resultHashResponse = new ArrayList<>();
                        SignHashMulti itemset;
                        for (SignMultiFile item : listResultSign) {

                            itemset = new SignHashMulti();
                            itemset.setId(item.getId());
                            itemset.setError(item.getCodeErr() == null
                                    ? null : item.getCodeErr().toString());
                            itemset.setMess(item.getMssErr());
                            resultHashResponse.add(itemset);
                        }
                        // Ghi log ket thuc chuc nang
                        return FunctionCommon.responseResult(ErrorCode.SUCCESS,
                                resultHashResponse, userGroup);
                    }
                    if (haveTask && CommonUtils.isEmpty(listSignHash)) {
                        Boolean isSucess = true;
                        // Neu ma loi tra ve la -1 thi buoc update databse bi that bai
                        for (SignMultiFile item : listResultSign) {
                            if (item.getCodeErr().equals(-1L)) {
                                isSucess = false;
                                break;
                            }
                        }
                        return FunctionCommon.responseResult(ErrorCode.SUCCESS,
                                isSucess, userGroup);
                    } // Tra ve cho mobile
                    else {                        
                        Long value = 1L;
                        return FunctionCommon.responseResult(ErrorCode.SUCCESS,
                                value, userGroup);
                    }
                }
                break;
            // Ky sim
            case 3:
                //Duong dan storage luu tru file
                keyStore = "task_export";
                storage = FunctionCommon.getPropertiesValue(keyStore);
                //thuc hien ky khong su dung thiet bi ky dien tu
                listFileSign = getListFileSignFromClient(
                        strJsonListFileSign, true);
                if (listFileSign != null && listFileSign.size() == 1) {
                    SignMultiFile fileSign = listFileSign.get(0);
                    String filePath = fileSign.getFilePath();
                    String fileName = fileSign.getFileSign();
                    List<EntityTask> listTask = fileSign.getListTask();
                    //kiem tra xem file co ton tai khong
                    // Duong dan tuyet doi cua file goc
                    String originFilePath = storage + File.separator + filePath;
                    originFilePath = FileUtils.getSafePath(originFilePath);
                    File file = new File(originFilePath);
                    // Thuc hien ky sim
                    if (file.exists()) {
                        Long resultSignSim = SignUtils.signTaskByCASIM(user2, type, fileName,
                                filePath, listTask, period, orgId, comment, keyStore);
                        response = FunctionCommon.responseResult(ErrorCode.SUCCESS,
                                resultSignSim, userGroup);
                    } // Loi trong qua trinh phe duyet cong viec
                    else {
                        result = false;
                        response = FunctionCommon.responseResult(ErrorCode.SUCCESS,
                                result, userGroup);
                        LOGGER.error("==Loi trong qua trinh ky sim==");
                    }
                } else {
                    result = false;
                    response = FunctionCommon.responseResult(ErrorCode.SUCCESS,
                            result, userGroup);
                    LOGGER.error("==Loi co nhieu cong viec khong the thuc hien ky sim==");
                }
                break;
            default:
                break;
        }
        return response;
    }
    
    /**
     * lay danh sach file ky tu client
     * @param strJsonListFileSign
     * @return 
     */
    private List<SignMultiFile> getListFileSignFromClient(
            String strJsonListFileSign, boolean isGetListTask) {
        
//        System.out.println("=======strJsonListFileSign:"+strJsonListFileSign);
        List<SignMultiFile> listResult = null;
        try {
            JSONArray arrFileSign = new JSONArray(strJsonListFileSign);
            if (arrFileSign.length() > 0) {
                SignMultiFile fileSignItem;
                listResult = new ArrayList<>();
                for (int i = 0; i < arrFileSign.length(); i++) {
                    //Get object cua tung doi tuong mang
                    JSONObject innerObj = (JSONObject) arrFileSign.get(i);
                    String[] keysObj = new String[]{
                        "index",
                        "fileName",
                        "enforcementId",
                        "filePath",
                        "listTask"
                    };
                    List<String> listValueObj = FunctionCommon.getValuesFromJSON(
                            innerObj, keysObj);
                    fileSignItem = new SignMultiFile();
                    Long index;
                    if(FunctionCommon.isNumeric(listValueObj.get(0))){
                        index = Long.parseLong(listValueObj.get(0));
                        fileSignItem.setId(index);
                    }
                    String fileName = listValueObj.get(1);
                    fileSignItem.setFileSign(fileName);
                    Long enforcementId;
                    if(FunctionCommon.isNumeric(listValueObj.get(2))){
                        enforcementId = Long.parseLong(listValueObj.get(2));
                        fileSignItem.setEnforcementId(enforcementId);
                    }
                    String filePath = listValueObj.get(3);
                    fileSignItem.setFilePath(filePath);
                    
                    //lay danh sach cong viec
                    String strListTask = listValueObj.get(4);
                    if(strListTask.trim().length()>0 && isGetListTask){
                        //thuc hien lay list cong viec
                        List<EntityTask> listTask = (List<EntityTask>)
                             FunctionCommon.convertJsonToListObject(
                                     strListTask, EntityTask.class);
                        fileSignItem.setListTask(listTask);
                    }
                    listResult.add(fileSignItem);
                }
            }
        } catch (JSONException ex) {
            LOGGER.error("Err! getListFileSignFromClient",ex);
            listResult = null;
        }
        return listResult;
    }

    /**
     * check loc danh sach van ban trong qua trinh nguoi khac dang ky va trong thoi gian co the tu choi
     * @param userId2
     * @param listTextId_Cache
     * @return 
     */
    private List<Long> filterTextIdCeoDuringSign(Long userId2, List<Long> listTextId_Cache) {
        List<Long> listResult = null;
        if(listTextId_Cache != null && listTextId_Cache.size() > 0){
            listResult = new ArrayList<>();
            //kiem tra van ban xem co dang trong thoi gian ky cua nguoi truoc ko
            for (int i = 0; i < listTextId_Cache.size(); i++) {
                boolean isNotReadDoc = getDoNotReadTextDuringSign(userId2, listTextId_Cache.get(i));
                if(!isNotReadDoc){
                   listResult.add(listTextId_Cache.get(i));
                }
            }
        }
        return listResult;
    }
    
    
    /**
     * datnv5:check status text in During Time sign of CEO
     * @param userIdVof2
     * @param textId
     * @return 
     */
    public boolean getDoNotReadTextDuringSign(Long userIdVof2, Long textId) {
        TextSearchDAO textDao = new TextSearchDAO();
        Long timeDuringSign = textDao.getTimeDuringSignTextOfCEO(userIdVof2, textId, false);
        //lay cau hinh mac dinh
        String valueTimeConfig = FunctionCommon.getPropertiesValue("text.signed.intimeprocess");
        double valueConfig = 15;
        if (valueTimeConfig != null && valueTimeConfig.trim().length() > 0) {
            try {
                valueConfig = Double.valueOf(valueTimeConfig);
            } catch (NumberFormatException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        //neu timeDuring nho hon cau hinh thi tra ve true nguoc lai tra ve false
//        System.out.println("userIdVof2:" + userIdVof2 + "====" + timeDuringSign+"/"+valueConfig);
        return timeDuringSign < valueConfig;
    }
    
    /**
     * @author Tunghd
     * hash list file cho man ho so
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws Throwable
     */
    public String hashListFileBrief(HttpServletRequest request, String data,
            String isSecurity) throws Throwable {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user tu memcached theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("hashListFile - No session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Neu user hoac user id tren he thong 1&2 null
        // -> Tra ve thong bao khong co quyen
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        if ((user1 == null || user1.getUserId() == null)
                && (user2 == null || user2.getUserId() == null)) {
            LOGGER.error("hashListFile - user hoac userId tren he thong 1&2 null");
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
                ConstantsFieldParams.DOCUMENTS,
                ConstantsFieldParams.CERTIFICATE,
                ConstantsFieldParams.COMMENT,
                ConstantsFieldParams.SIGN_TYPE,
                ConstantsFieldParams.SCOPE,
                ConstantsFieldParams.PUBLIC_KEY,
                ConstantsFieldParams.SEND_SMS,
                ConstantsFieldParams.DEVICE_NAME,
                ConstantsFieldParams.TEXT_PROCESS_ID,
                "lstMarkInfo",
                "lstMark"
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Chuoi id
            String strDocuments = listValue.get(0);
            if (CommonUtils.isEmpty(strDocuments)) {
                LOGGER.error("hashListFile - userId1: " + userId1 + " - userId2: "
                        + userId2 + " - Chuoi id null hoac rong");
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
            // Chung thu
            String certificate = listValue.get(1);
            // Y kien
            String comment = listValue.get(2);
            // Loai ky
            String signType = listValue.get(3);
            // Pham vi
            Long scope = -1L;
            String strScope = listValue.get(4);
            if (!CommonUtils.isEmpty(strScope)) {
                scope = Long.parseLong(strScope);
            }
            String publicKey = listValue.get(5);
            // Co gui tin nhan cho lanh dao sau khi van thu xet duyet hay khong
            // Do Website Voffice luon gui tin nhan cho lanh dao sau khi van thu xet duyet
            int sendSMS = 1;
            String strSendSMS = listValue.get(6);
            if (!CommonUtils.isEmpty(strSendSMS)) {
                sendSMS = Integer.parseInt(strSendSMS);
            }
            // Ten thiet bi
            String deviceName = listValue.get(7);
            // 201812-Pitagon: add
            String textProcessId = listValue.get(8);
           
            //Tunghd add
            List<EntityMarkInfo> lstMarkInfo = new ArrayList<EntityMarkInfo>();
            String markInfo = listValue.get(9);
            if (!CommonUtils.isEmpty(markInfo)) {
                Gson gson =  new GsonBuilder().create();
                Type listMark = new TypeToken<ArrayList<EntityMarkInfo>>(){}.getType();
                lstMarkInfo = gson.fromJson(markInfo, listMark);
            }
            //End
            // Datdc start lstMark (toa do, thong tin dau)
            List<EntityMarkInfo> lstMarkDau = new ArrayList<EntityMarkInfo>();
            String mark = listValue.get(10);
            if (!CommonUtils.isEmpty(mark)) {
                Gson gson =  new GsonBuilder().create();
                Type listMarkTmp = new TypeToken<ArrayList<EntityMarkInfo>>(){}.getType();
                lstMarkDau = gson.fromJson(mark, listMarkTmp);
            }
            // Datdc end lstMark (toa do, thong tin dau)
            JSONObject objMapTextProcessId = new JSONObject();
            Map<Long, Long> mapTextProcessId = new HashMap<Long, Long>();
            if (!CommonUtils.isEmpty(textProcessId)) {
                objMapTextProcessId = new JSONObject(textProcessId);
            }

            // Kiem tra chung thu va thoi han
            ErrSign itemCheckCer = SignUtils.checkCer(userGroup, publicKey, certificate);
//            System.out.println("itemCheckCer:" + itemCheckCer);
            if (itemCheckCer.getErrCode() != null && itemCheckCer.getErrCode().equals(1L)) {
                // Pham vi la ky file van ban
                if (scope.equals(BRIEF_SCOPE)) {
                    // Tao danh sach id van ban tu chuoi id
                    String[] arrTextId = strDocuments.split(Constants.Common.COMMA_CHAR);
                    Long textId;
                    // Duyet mang id van ban
                    Set<Long> textIdSet = new HashSet<Long>();
                    for (String strTextId : arrTextId) {
                        strTextId = strTextId.trim();
                        if (!CommonUtils.isEmpty(strTextId)) {
                            textId = Long.parseLong(strTextId);
                            textIdSet.add(textId);
                            // 201812-Pitagon: add
                            if (!CommonUtils.isEmpty(textProcessId)) {
                                mapTextProcessId.put(textId, objMapTextProcessId.getLong(strTextId));
                            }
                        }
                    }
                    List<Long> listTextId = new ArrayList<>(textIdSet);
                    if (CommonUtils.isEmpty(listTextId)) {
                        LOGGER.error("hashListFile - userId1: " + userId1 + " - userId2: "
                                + userId2 + " - listTextId null hoac rong");
                        return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                    }
                    //datnv5:check them dieu kien neu van ban do dang trong qua trinh ky lai
                    //thuc hien loai bo van ban dang trong qua trinh ky
//                    List<Long>  listTextId = filterTextIdCeoDuringSign(userId2, listTextId_Cache);
//                    if(listTextId==null || listTextId.size()<=0){
//                        return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
//                                Constants.SIGN_RESULT_CODE.STATUS_DELAY_SIGN.toString(), aesKey);
//                    }
                    //Lay thong tin file
                    List<EntityAttach> listAttachments = new ArrayList<EntityAttach>();
                    AttachDAO attachDAO = new AttachDAO();
//                    Long docId = lstMarkInfo.get(0).getDocumentId();
                     listAttachments = attachDAO.getEntityAttachInfoBrief(userId2,listTextId);
                    // Datdc add lstMarkDau
                    List<SignMultiFile> resultHash = SignUtils.hashListFileBrief(user1,
                            user2, publicKey, listAttachments, certificate, comment,
                            deviceName, signType, mapTextProcessId, lstMarkInfo, lstMarkDau);
                    if (!CommonControler.setSignSession(request, resultHash)) {
                        LOGGER.error("hashListFileBrief - userId1: " + userId1 + " - userId2: "
                                + userId2 + " - Luu phien ky that bai");
                        return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                    } else {
                        // Luu thong tin signType len server thanh cong
                        if (CommonControler.setSessionAttribute(request,
                                ConstantsFieldParams.SIGN_TYPE, signType)) {
                            // 201812-Pitagon: add
                            if (mapTextProcessId != null && !mapTextProcessId.isEmpty()) {
                                CommonControler.setSessionAttribute(request, ConstantsFieldParams.TEXT_PROCESS_ID, mapTextProcessId);
                            }
                            
                            // Luu thong tin gui tin nhan cho lanh dao sau khi van thu xet duyet
                            CommonControler.setSessionAttribute(request,
                                    ConstantsFieldParams.SEND_SMS, sendSMS);
                            String strIpServer = "http://" + request.getLocalAddr()
                                    + ":" + String.valueOf(request.getLocalPort())
                                    + "/ServiceMobile_V02/resources/";
                            List<SignHashMulti> resultHashResponse = new ArrayList<SignHashMulti>();
                            SignHashMulti itemset;
                            for (SignMultiFile item : resultHash) {
                                itemset = new SignHashMulti();
                                itemset.setId(item.getId());
                                itemset.setHash(item.getHash());
                                itemset.setStrUrl(strIpServer);
                                itemset.setError(item.getCodeErr() == null
                                        ? null : item.getCodeErr().toString());
//                                System.out.println("textId: " + itemset.getId()
//                                        + " - hash: " + itemset.getHash()
//                                        + " - errorCode: " + itemset.getError());
                                resultHashResponse.add(itemset);
                                listTextId.remove(item.getTextId());
                            }
                            //datnv5: thuc hien set loi van ban dang trong qua trinh ky nen khong thuc hien hash
                            for (Long itemLongIdText : listTextId) {
                                itemset = new SignHashMulti();
                                itemset.setId(itemLongIdText);
                                itemset.setHash("");
                                itemset.setStrUrl(strIpServer);
                                itemset.setError("Loi file dang trong qua trinh xu ly cua lanh dao");
                                resultHashResponse.add(itemset);
                            }
                            // Ghi log ket thuc chuc nang
                            LogUtils.logFunctionalEnd(log);
                            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                                    resultHashResponse, aesKey);
                        } // Khong luu signType vao session duoc
                        else {
                            LOGGER.error("hashListFileBrief - userId1: " + userId1
                                    + " - userId2: " + userId2 + " - Loi luu signType vao session");
                            return FunctionCommon.generateResponseJSON(
                                    ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                        }
                    }
                } // scope khong hop le
                else {
                    LOGGER.error("hashListFileBrief - userId1: " + userId1
                            + " - userId2: " + userId2 + " - Loi scope khong hop le");
                    return FunctionCommon.generateResponseJSON(
                            ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
            } // Chung thu khong hop le hoac da het han
            else {
                LOGGER.error("hashListFileBrief - userId1: " + userId1
                        + " - userId2: " + userId2 + " - Chung thu khong hop le hoac da het han");
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                        itemCheckCer.getErrCode(), aesKey);
            }
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error("hashListFileBrief - userId1: " + userId1 + " - userId2: "
                    + userId2, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID,
                    null, null);
        } catch (Exception ex) {
            LOGGER.error("hashListFileBrief - userId1: " + userId1 + " - userId2: "
                    + userId2 + " - Exception: " + ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    /**
     * @author Tunghd
     * Append anh ky vao file + insert thong tin dong dau vao database man Ho So
     * @param request
     * @param strData
     * @param isSecurity
     * @return
     */
    public String appendSignatureIntoListFileBrief(HttpServletRequest request,
            String strData, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Tao doi tuong ghi log
        String response;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        Boolean isCheckSs = userGroup.getCheckSessionOk();
        if (isCheckSs) {
            // Lay AES key
            String strAesKeyDecode = userGroup.getStrAesKey();
            // Lay gia tri client gui len
            HashMap<String, Object> hmParams = new HashMap<String, Object>();
            hmParams.put("signatures", String.class);
            hmParams.put(ConstantsFieldParams.DEVICE_NAME, String.class);
            HashMap<String, Object> valueParams = FunctionCommon
                    .getListParamsClient(hmParams, strData, userGroup);
            String strListSignHash = (String) valueParams.get("signatures");

            //Begin::Lay danh sach file duoc dinh kem khi ky::cuongnv
            if (isSecurity != null && "1".equals(isSecurity)) {
                // Lay AES Key
                strAesKeyDecode = userGroup.getStrAesKey();
                // Giai ma data client gui len
                strData = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
            }
            // Lay ma nhan vien
            String cardId = userGroup.getCardId();
            log.setUserName(cardId);
            // Ghi log bat dau chuc nang
            log.setParamList(strData);
            LogUtils.logFunctionalStart(log);
            List<EntityFileAttachment> strListFileSign = new ArrayList<>();
            JSONArray arrListFileSign = FunctionCommon.jsonGetArray(ConstantsFieldParams.REQ_LST_ATTACHMENT_FILES, strData);

            if (arrListFileSign != null && arrListFileSign.length() > 0) {
                for (int i = 0; i < arrListFileSign.length(); i++) {
                    try {
                        JSONObject innerObj = (JSONObject) arrListFileSign.get(i);
                        Long fileAttachmentId = null;
                        String name = "";
                        String filePath = "";

//                        if (innerObj.has(ConstantsFieldParams.TEXT_ATTACH_ID)) {
//                            fileAttachmentId = Long.parseLong(innerObj.getString(ConstantsFieldParams.TEXT_ATTACH_ID).trim());
//                        }
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
                    } catch (Exception ex) {
                        LOGGER.error("Error appendSignatureIntoListFileBrief", ex);
                    }

                }
            }
            //End

            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<SignHashMulti>>() {
            }.getType();
            List<SignHashMulti> listSignHash = gson.fromJson(strListSignHash, listType);

            String deviceName = valueParams.get(ConstantsFieldParams.DEVICE_NAME) == null
                    ? "" : (String) valueParams.get(ConstantsFieldParams.DEVICE_NAME);
            List<SignHashMulti> result = SignUtils.appendSignatureIntoListFileBrief(
                    request, listSignHash, strListFileSign, deviceName);
            if (result != null && result.size() > 0 && result.get(0).getError() != null
                    && result.get(0).getError().equals(String.valueOf(ErrorCode.TEXT_LOCK_SIGN_STATE.getErrorCode()))) {
                //neu la loi khoa ky song song
                response = FunctionCommon.generateResponseJSON(ErrorCode.TEXT_LOCK_SIGN_STATE, result, strAesKeyDecode);
            } else {
                response = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
            }
        } else {
            response = FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return response;
    }
    
    /**
     * @author Tunghd
     * hash list file cho man VAN BAN
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws Throwable
     */
    public String hashListFileDoc(HttpServletRequest request, String data,
            String isSecurity) throws Throwable {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user tu memcached theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("hashListFile - No session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Neu user hoac user id tren he thong 1&2 null
        // -> Tra ve thong bao khong co quyen
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        if ((user1 == null || user1.getUserId() == null)
                && (user2 == null || user2.getUserId() == null)) {
            LOGGER.error("hashListFile - user hoac userId tren he thong 1&2 null");
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
                ConstantsFieldParams.DOCUMENTS,
                ConstantsFieldParams.CERTIFICATE,
                ConstantsFieldParams.COMMENT,
                ConstantsFieldParams.SIGN_TYPE,
                ConstantsFieldParams.SCOPE,
                ConstantsFieldParams.PUBLIC_KEY,
                ConstantsFieldParams.SEND_SMS,
                ConstantsFieldParams.DEVICE_NAME,
                ConstantsFieldParams.TEXT_PROCESS_ID,
                "lstMarkInfo",
                "lstMark"
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Chuoi id
            String strDocuments = listValue.get(0);
            if (CommonUtils.isEmpty(strDocuments)) {
                LOGGER.error("hashListFile - userId1: " + userId1 + " - userId2: "
                        + userId2 + " - Chuoi id null hoac rong");
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
            // Chung thu
            String certificate = listValue.get(1);
            // Y kien
            String comment = listValue.get(2);
            // Loai ky
            String signType = listValue.get(3);
            // Pham vi
            Long scope = -1L;
            String strScope = listValue.get(4);
            if (!CommonUtils.isEmpty(strScope)) {
                scope = Long.parseLong(strScope);
            }
            String publicKey = listValue.get(5);
            // Co gui tin nhan cho lanh dao sau khi van thu xet duyet hay khong
            // Do Website Voffice luon gui tin nhan cho lanh dao sau khi van thu xet duyet
            int sendSMS = 1;
            String strSendSMS = listValue.get(6);
            if (!CommonUtils.isEmpty(strSendSMS)) {
                sendSMS = Integer.parseInt(strSendSMS);
            }
            // Ten thiet bi
            String deviceName = listValue.get(7);
            // 201812-Pitagon: add
            String textProcessId = listValue.get(8);
           
            //Tunghd add
            List<EntityMarkInfo> lstMarkInfo = new ArrayList<EntityMarkInfo>();
            String markInfo = listValue.get(9);
            if (!CommonUtils.isEmpty(markInfo)) {
                Gson gson =  new GsonBuilder().create();
                Type listMark = new TypeToken<ArrayList<EntityMarkInfo>>(){}.getType();
                lstMarkInfo = gson.fromJson(markInfo, listMark);
            }
            //End
            // Datdc start lstMark (toa do, thong tin dau)
            List<EntityMarkInfo> lstMarkDau = new ArrayList<EntityMarkInfo>();
            String mark = listValue.get(10);
            if (!CommonUtils.isEmpty(mark)) {
                Gson gson =  new GsonBuilder().create();
                Type listMarkTmp = new TypeToken<ArrayList<EntityMarkInfo>>(){}.getType();
                lstMarkDau = gson.fromJson(mark, listMarkTmp);
            }
            // Datdc end lstMark (toa do, thong tin dau)
            JSONObject objMapTextProcessId = new JSONObject();
            Map<Long, Long> mapTextProcessId = new HashMap<Long, Long>();
            if (!CommonUtils.isEmpty(textProcessId)) {
                objMapTextProcessId = new JSONObject(textProcessId);
            }

            // Kiem tra chung thu va thoi han
            ErrSign itemCheckCer = SignUtils.checkCer(userGroup, publicKey, certificate);
//            System.out.println("itemCheckCer:" + itemCheckCer);
            if (itemCheckCer.getErrCode() != null && itemCheckCer.getErrCode().equals(1L)) {
                // Pham vi la ky file van ban
                if (scope.equals(DOC_SCOPE)) {
                    // Tao danh sach id van ban tu chuoi id
                    String[] arrTextId = strDocuments.split(Constants.Common.COMMA_CHAR);
                    Long textId;
                    // Duyet mang id van ban
                    Set<Long> textIdSet = new HashSet<Long>();
                    for (String strTextId : arrTextId) {
                        strTextId = strTextId.trim();
                        if (!CommonUtils.isEmpty(strTextId)) {
                            textId = Long.parseLong(strTextId);
                            textIdSet.add(textId);
                            // 201812-Pitagon: add
                            if (!CommonUtils.isEmpty(textProcessId)) {
                                mapTextProcessId.put(textId, objMapTextProcessId.getLong(strTextId));
                            }
                        }
                    }
                    List<Long> listTextId = new ArrayList<>(textIdSet);
//                    if (CommonUtils.isEmpty(listTextId_Cache)) {
//                        LOGGER.error("hashListFileDoc - userId1: " + userId1 + " - userId2: "
//                                + userId2 + " - listTextId null hoac rong");
//                        return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
//                    }
                    //datnv5:check them dieu kien neu van ban do dang trong qua trinh ky lai
                    //thuc hien loai bo van ban dang trong qua trinh ky
//                    List<Long>  listTextId = filterTextIdCeoDuringSign(userId2, listTextId);
                    if(listTextId==null || listTextId.size()<=0){
                        return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                                Constants.SIGN_RESULT_CODE.STATUS_DELAY_SIGN.toString(), aesKey);
                    }
                    List<EntityAttach> listAttachment = new ArrayList<EntityAttach>();
                    AttachDAO attachDAO = new AttachDAO();
                    Long docId = lstMarkInfo.get(0).getTextId();
                    listAttachment = attachDAO.getEntityAttachInfo(userId2,docId);
                    
                    // Datdc add lstMarkDau
                    List<SignMultiFile> resultHash = SignUtils.hashListFileDoc(user1,
                            user2, publicKey, listAttachment, certificate, comment,
                            deviceName, signType, mapTextProcessId, lstMarkInfo, lstMarkDau);
                    if (!CommonControler.setSignSession(request, resultHash)) {
                        LOGGER.error("hashListFile - userId1: " + userId1 + " - userId2: "
                                + userId2 + " - Luu phien ky that bai");
                        return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                    } else {
                        // Luu thong tin signType len server thanh cong
                        if (CommonControler.setSessionAttribute(request,
                                ConstantsFieldParams.SIGN_TYPE, signType)) {
                            // 201812-Pitagon: add
                            if (mapTextProcessId != null && !mapTextProcessId.isEmpty()) {
                                CommonControler.setSessionAttribute(request, ConstantsFieldParams.TEXT_PROCESS_ID, mapTextProcessId);
                            }
                            
                            // Luu thong tin gui tin nhan cho lanh dao sau khi van thu xet duyet
                            CommonControler.setSessionAttribute(request,
                                    ConstantsFieldParams.SEND_SMS, sendSMS);
                            String strIpServer = "http://" + request.getLocalAddr()
                                    + ":" + String.valueOf(request.getLocalPort())
                                    + "/ServiceMobile_V02/resources/";
                            List<SignHashMulti> resultHashResponse = new ArrayList<SignHashMulti>();
                            SignHashMulti itemset;
                            for (SignMultiFile item : resultHash) {
                                itemset = new SignHashMulti();
                                itemset.setId(item.getId());
                                itemset.setHash(item.getHash());
                                itemset.setStrUrl(strIpServer);
                                itemset.setError(item.getCodeErr() == null
                                        ? null : item.getCodeErr().toString());
//                                System.out.println("textId: " + itemset.getId()
//                                        + " - hash: " + itemset.getHash()
//                                        + " - errorCode: " + itemset.getError());
                                resultHashResponse.add(itemset);
                                listTextId.remove(item.getTextId());
                            }
                            //datnv5: thuc hien set loi van ban dang trong qua trinh ky nen khong thuc hien hash
                            for (Long itemLongIdText : listTextId) {
                                itemset = new SignHashMulti();
                                itemset.setId(itemLongIdText);
                                itemset.setHash("");
                                itemset.setStrUrl(strIpServer);
                                itemset.setError("Loi file dang trong qua trinh xu ly cua lanh dao");
                                resultHashResponse.add(itemset);
                            }
                            // Ghi log ket thuc chuc nang
                            LogUtils.logFunctionalEnd(log);
                            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                                    resultHashResponse, aesKey);
                        } // Khong luu signType vao session duoc
                        else {
                            LOGGER.error("hashListFileDoc - userId1: " + userId1
                                    + " - userId2: " + userId2 + " - Loi luu signType vao session");
                            return FunctionCommon.generateResponseJSON(
                                    ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                        }
                    }
                } // scope khong hop le
                else {
                    LOGGER.error("hashListFileDoc - userId1: " + userId1
                            + " - userId2: " + userId2 + " - Loi scope khong hop le");
                    return FunctionCommon.generateResponseJSON(
                            ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
            } // Chung thu khong hop le hoac da het han
            else {
                LOGGER.error("hashListFileDoc - userId1: " + userId1
                        + " - userId2: " + userId2 + " - Chung thu khong hop le hoac da het han");
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                        itemCheckCer.getErrCode(), aesKey);
            }
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error("hashListFileDoc - userId1: " + userId1 + " - userId2: "
                    + userId2, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID,
                    null, null);
        } catch (Exception ex) {
            LOGGER.error("hashListFileDoc - userId1: " + userId1 + " - userId2: "
                    + userId2 + " - Exception: " + ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    /**
     * @author Tunghd
     * Append anh ky vao file + insert thong tin dong dau vao database man Van ban
     * @param request
     * @param strData
     * @param isSecurity
     * @return
     */
    public String appendSignatureIntoListFileDoc(HttpServletRequest request,
            String strData, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Tao doi tuong ghi log
        String response;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        Boolean isCheckSs = userGroup.getCheckSessionOk();
        if (isCheckSs) {
            // Lay AES key
            String strAesKeyDecode = userGroup.getStrAesKey();
            // Lay gia tri client gui len
            HashMap<String, Object> hmParams = new HashMap<String, Object>();
            hmParams.put("signatures", String.class);
            hmParams.put(ConstantsFieldParams.DEVICE_NAME, String.class);
            HashMap<String, Object> valueParams = FunctionCommon
                    .getListParamsClient(hmParams, strData, userGroup);
            String strListSignHash = (String) valueParams.get("signatures");

            //Begin::Lay danh sach file duoc dinh kem khi ky::cuongnv
            if (isSecurity != null && "1".equals(isSecurity)) {
                // Lay AES Key
                strAesKeyDecode = userGroup.getStrAesKey();
                // Giai ma data client gui len
                strData = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
            }
            // Lay ma nhan vien
            String cardId = userGroup.getCardId();
            log.setUserName(cardId);
            // Ghi log bat dau chuc nang
            log.setParamList(strData);
            LogUtils.logFunctionalStart(log);
            List<EntityFileAttachment> strListFileSign = new ArrayList<>();
            JSONArray arrListFileSign = FunctionCommon.jsonGetArray(ConstantsFieldParams.REQ_LST_ATTACHMENT_FILES, strData);

            if (arrListFileSign != null && arrListFileSign.length() > 0) {
                for (int i = 0; i < arrListFileSign.length(); i++) {
                    try {
                        JSONObject innerObj = (JSONObject) arrListFileSign.get(i);
                        Long fileAttachmentId = null;
                        String name = "";
                        String filePath = "";

//                        if (innerObj.has(ConstantsFieldParams.TEXT_ATTACH_ID)) {
//                            fileAttachmentId = Long.parseLong(innerObj.getString(ConstantsFieldParams.TEXT_ATTACH_ID).trim());
//                        }
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
                    } catch (Exception ex) {
                        LOGGER.error("Error appendSignatureIntoListFileDOC", ex);
                    }

                }
            }
            //End

            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<SignHashMulti>>() {
            }.getType();
            List<SignHashMulti> listSignHash = gson.fromJson(strListSignHash, listType);

            String deviceName = valueParams.get(ConstantsFieldParams.DEVICE_NAME) == null
                    ? "" : (String) valueParams.get(ConstantsFieldParams.DEVICE_NAME);
            List<SignHashMulti> result = SignUtils.appendSignatureIntoListFileDoc(
                    request, listSignHash, strListFileSign, deviceName);
            if (result != null && result.size() > 0 && result.get(0).getError() != null
                    && result.get(0).getError().equals(String.valueOf(ErrorCode.TEXT_LOCK_SIGN_STATE.getErrorCode()))) {
                //neu la loi khoa ky song song
                response = FunctionCommon.generateResponseJSON(ErrorCode.TEXT_LOCK_SIGN_STATE, result, strAesKeyDecode);
            } else {
                response = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
            }
        } else {
            response = FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return response;
    }

}
