/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.constants.InputStreamWithFileDeletion;
import com.viettel.voffice.database.dao.text.TextMarkSyncDAO;
import com.viettel.voffice.database.entity.EntityDocumentSync;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityTextMarkSync;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.User.Vof2_EntityUser;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.EditorUtils;
import com.viettel.voffice.utils.FileUtils;
import com.viettel.voffice.utils.LogUtils;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * danh dau dong bo van ban
 *
 * @author hanhnq21
 */
public class TextMarkSyncControler {

    private static final Logger LOGGER = Logger.getLogger(TextMarkSyncControler.class);
    private static final String REGISTER_NUMBER = "registerNumber";//so dang ky
    private static final String CODE = "code";//ma van ban
    private static final String TEXT_ID = "textId";//id
    private static final String CREATE_DATE_FROM = "createDateFrom";//ngay tao tu
    private static final String CREATE_DATE_TO = "createDateTo";//ngay tao den
    private static final String START_RECORD = "startRecord";//
    private static final String PAGE_SIZE = "pageSize";//so luong ban ghi trong trang
    private static final String IS_COUNT = "isCount";//lay so luong van ban
    private static final String APP_CODE = "appCode";//ma ung dung trinh ky tu dong
    private static final String IS_AUTO_DIGITAL_SIGN = "isAutoDigitalSign";//dong bo van ban trinh ky tu dong
    private static final String TEXT_MARK_SYNC_ID = "textMarkSyncId";//id danh dau van ban

    public static final String ROOT_ACTION = "/Files";
    private static final String PATH_FILE_ERROR = "0"; //loi sai duong dan file
    private static final String PERMISS_DOWNLOAD_FILE_ERROR = "1"; //loi khong co quyen download file
    private static final String NOT_EXIST_FILE_ERROR = "2"; //loi khong ton tai file

    // Ten cua class bao gom ca ten package
    private static final String CLASS_NAME = TextMarkSyncControler.class.getName();

    /**
     * them moi danh dau dong bo van ban
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String addTextMarkSync(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String result;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            //het session
            LOGGER.error("insertTextMarkSync - No session");
            result = FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        } else {
            Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
            if ((user2 == null || user2.getUserId() == null)) {
                //khong ton tai tren he thong voffice 2
                LOGGER.error("insertTextMarkSync - user khong ton tai tren he thong 2");
                result = FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
            } else {
                Long userId = user2.getUserId();
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
                    if (user2.getTypeTextMarkSync() != null) {
                        //neu la user duoc phep danh dau dong bo
                        JSONObject json = new JSONObject(data);
                        String[] keys = new String[]{TEXT_ID};
                        List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                        String strTextId = listValue.get(0);
                        if (!CommonUtils.isEmpty(strTextId)) {
                            Long textId = Long.parseLong(strTextId);//id van ban danh dau
                            EntityTextMarkSync textMarkSync = new EntityTextMarkSync();
                            textMarkSync.setTextId(textId);
                            textMarkSync.setCreateById(userId);
                            textMarkSync.setType(user2.getTypeTextMarkSync());
                            //them moi
                            TextMarkSyncDAO textMarkSyncDAO = new TextMarkSyncDAO();
                            int insertResult = textMarkSyncDAO.insertTextMarkSync(textMarkSync);
                            if (insertResult == 1) {
                                //thanh cong
                                result = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, insertResult, aesKey);
                            } else {
                                //khong thanh cong
                                result = FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, insertResult, aesKey);
                            }
                        } else {
                            //loi du lieu dau vao
                            result = FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                        }
                    } else {
                        //khong duoc phep insert
                        result = FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
                    }
                    // Ghi log ket thuc chuc nang
                    LogUtils.logFunctionalEnd(log);
                } catch (JSONException | NumberFormatException ex) {
                    //co loi xay ra
                    LOGGER.error("insertTextMarkSync - co loi xay ra - userId2: "
                            + user2.getUserId() + " - Exception: ", ex);
                    result = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
            }
        }
        return result;
    }

    /**
     * lay thong tin danh dau van ban dong bo
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getTextMarkSync(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String result;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            //het session
            LOGGER.error("getTextMarkSync - No session");
            result = FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        } else {
            Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
            if ((user2 == null || user2.getUserId() == null)) {
                //khong ton tai tren he thong voffice 2
                LOGGER.error("getTextMarkSync - user khong ton tai tren he thong 2");
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
                    String[] keys = new String[]{TEXT_ID};
                    List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                    String strTextId = listValue.get(0);
                    if (!CommonUtils.isEmpty(strTextId)) {
                        Long textId = Long.parseLong(strTextId);//id van ban danh dau
                        //lay thong tin danh dau
                        TextMarkSyncDAO textMarkSyncDAO = new TextMarkSyncDAO();
                        EntityTextMarkSync textMarkSync = textMarkSyncDAO.getTextMarkSync(textId);
                        if (textMarkSync != null) {
                            //thanh cong
                            result = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, textMarkSync, aesKey);
                        } else {
                            //khong thanh cong
                            result = FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, null, null);
                        }
                    } else {
                        //loi du lieu dau vao
                        result = FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                    }
                } catch (JSONException | NumberFormatException ex) {
                    //co loi xay ra
                    LOGGER.error("getTextMarkSync - co loi xay ra - userId2: "
                            + user2.getUserId() + " - Exception: ", ex);
                    result = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            }
        }
        return result;
    }

    /**
     * lay danh sach van ban danh dau hoac trinh ky tu dong
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListDocumentSync(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String result;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            //het session
            LOGGER.error("getListDocumentSync - No session");
            result = FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        } else {
            Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
            if ((user2 == null || user2.getUserId() == null)) {
                //khong ton tai tren he thong voffice 2
                LOGGER.error("getListDocumentSync - user khong ton tai tren he thong 2");
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
                    String[] keys = new String[]{REGISTER_NUMBER, CODE,
                        TEXT_ID, CREATE_DATE_FROM, CREATE_DATE_TO, START_RECORD,
                        PAGE_SIZE, IS_COUNT, APP_CODE, IS_AUTO_DIGITAL_SIGN, TEXT_MARK_SYNC_ID};
                    List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                    String registerNumber = "";//so dang ky
                    if (!CommonUtils.isEmpty(listValue.get(0))) {
                        registerNumber = listValue.get(0);
                    }
                    String code = "";//ma van ban
                    if (!CommonUtils.isEmpty(listValue.get(1))) {
                        code = listValue.get(1);
                    }
                    Long textId = null;//id
                    if (!CommonUtils.isEmpty(listValue.get(2))) {
                        textId = Long.parseLong(listValue.get(2));
                    }
                    String createDateFrom = "";//ngay tao tu
                    if (!CommonUtils.isEmpty(listValue.get(3))) {
                        createDateFrom = listValue.get(3);
                    }
                    String createDateTo = "";//ngay tao den
                    if (!CommonUtils.isEmpty(listValue.get(4))) {
                        createDateTo = listValue.get(4);
                    }
                    Long startRecord = null;
                    if (!CommonUtils.isEmpty(listValue.get(5))) {
                        startRecord = Long.parseLong(listValue.get(5));
                    }
                    Long pageSize = null;
                    if (!CommonUtils.isEmpty(listValue.get(6))) {
                        pageSize = Long.parseLong(listValue.get(6));
                    }
                    Long isCount = 0L;
                    if (!CommonUtils.isEmpty(listValue.get(7))) {
                        isCount = Long.parseLong(listValue.get(7));
                    }
                    String appCode = "";//ngay tao den
                    if (!CommonUtils.isEmpty(listValue.get(8))) {
                        appCode = listValue.get(8);
                    }
                    Long isAutoDigitalSign = null;//dong bo van ban trinh ky tu dong
                    if (!CommonUtils.isEmpty(listValue.get(9))) {
                        isAutoDigitalSign = Long.parseLong(listValue.get(9));
                    }
                    Long textMarkSyncId = null;//dong bo van ban trinh ky tu dong
                    if (!CommonUtils.isEmpty(listValue.get(10))) {
                        textMarkSyncId = Long.parseLong(listValue.get(10));
                    }
                    TextMarkSyncDAO textMarkSyncDAO = new TextMarkSyncDAO();
                    Long type = textMarkSyncDAO.checkUserTextMarkSync(user2.getUserId());
                    if (isCount.equals(1L)) {
                        //neu la lay so luong
                        Long count = textMarkSyncDAO.countDocumentSync(type, registerNumber, code,
                                textId, createDateFrom, createDateTo, textMarkSyncId);
                        result = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, count, aesKey);
                    } else {
                        List<EntityTextMarkSync> lstDocumentSync;
                        if (isAutoDigitalSign != null && isAutoDigitalSign.equals(1L)) {
                            //la tim kiem van ban trinh ky tu dong
                            lstDocumentSync
                                    = textMarkSyncDAO.getListDocumentAutoDigitalSign(type, registerNumber, code, textId, createDateFrom,
                                            createDateTo, startRecord, pageSize, appCode);
                        } else {
                            //la tim kiem van ban danh dau
                            lstDocumentSync = textMarkSyncDAO.getListDocumentSync(type, registerNumber, code,
                                    textId, createDateFrom, createDateTo, startRecord, pageSize, textMarkSyncId);
                        }
                        result = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, lstDocumentSync, aesKey);
                    }
                    // Ghi log ket thuc chuc nang
                    LogUtils.logFunctionalEnd(log);
                } catch (JSONException | NumberFormatException ex) {
                    //co loi xay ra
                    LOGGER.error("getListDocumentSync - co loi xay ra - userId2: "
                            + user2.getUserId() + " - Exception: ", ex);
                    result = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
            }
        }
        return result;
    }

    /**
     * lay chi tiet van ban dong bo
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getTextDetailSync(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String result;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            //het session
            LOGGER.error("getTextDetailSync - No session");
            result = FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        } else {
            Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
            if ((user2 == null || user2.getUserId() == null)) {
                //khong ton tai tren he thong voffice 2
                LOGGER.error("getTextDetailSync - user khong ton tai tren he thong 2");
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
                    String[] keys = new String[]{TEXT_ID, "appCode", "tranCode"};
                    List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                    String strTextId = listValue.get(0);
                    String appCode = listValue.get(1);
                    String tranCode = listValue.get(2);
                    if (!CommonUtils.isEmpty(strTextId)
                            || (!CommonUtils.isEmpty(appCode) && !CommonUtils.isEmpty(tranCode))) {
                        Long textId = Long.parseLong(strTextId);//id van ban danh dau
                        //lay thong tin chi tiet van ban
                        TextMarkSyncDAO textMarkSyncDAO = new TextMarkSyncDAO();
                        Long type = textMarkSyncDAO.checkUserTextMarkSync(user2.getUserId());
                        if (type != null
                                && (textMarkSyncDAO.isTextMarkSync(textId, type)
                                || textMarkSyncDAO.isDocumentAutoDigitalSign(textId, type, appCode, tranCode))) {
                            //neu la danh dau hoac trinh ky tu dong sang
                            EntityDocumentSync documentSync = textMarkSyncDAO.getTextDetailSync(textId);
                            if (documentSync != null) {
                                //thanh cong
                                result = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, documentSync, aesKey);
                            } else {
                                //khong thanh cong
                                result = FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, null, null);
                            }
                        } else {
                            //khong co quyen xem chi tiet
                            result = FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
                        }
                    } else {
                        //loi du lieu dau vao
                        result = FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                    }
                    // Ghi log ket thuc chuc nang
                    LogUtils.logFunctionalEnd(log);
                } catch (JSONException | NumberFormatException ex) {
                    //co loi xay ra
                    LOGGER.error("getTextDetailSync - co loi xay ra - userId2: "
                            + user2.getUserId() + " - Exception: ", ex);
                    result = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
            }
        }
        return result;
    }

    /**
     * <b>Download noi dung file</b>
     *
     * @param req
     * @param data
     * @param isSecurity
     * @return
     */
    public Response downloadTextMarkContentFile(
            HttpServletRequest req, String data, String isSecurity) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        Response response = null;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(req);
        if (!userGroup.getCheckSessionOk()) {
            return response;
        }
        // Lay id user he thong 2
        Long userIdVof2 = 0L;
        if (userGroup.getVof2_ItemEntityUser() != null
                && userGroup.getVof2_ItemEntityUser().getUserId() != null) {
            userIdVof2 = userGroup.getVof2_ItemEntityUser().getUserId();
        }
        if (userIdVof2 == null) {
            return response;
        }

        // Lay ten user doc van ban
        String userName = EditorUtils.unSignMore(userGroup.getVof2_ItemEntityUser().getStrCardNumber())
                + "_"
                + EditorUtils.unSignMore(userGroup.getVof2_ItemEntityUser().getFullName());
        // Giai ma du lieu neu ma hoa
        String aesKey;
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
                ConstantsFieldParams.FILE_NAME,
                ConstantsFieldParams.FILE_PATH,
                ConstantsFieldParams.TEXT_ID,
                ConstantsFieldParams.IS_ORIGINAL
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);

            // Ten file
            String fileName = listValue.get(0);
            // Duong dan file
            String filePath = listValue.get(1);
            // Id van ban
            Long textId = 0L;
            String strTextId = listValue.get(2);
            if (!CommonUtils.isEmpty(strTextId)) {
                textId = Long.parseLong(strTextId);
            }
            // In van ban danh cho doi tuong van thu
            String isOriginal = listValue.get(3);
            if (!FileUtils.checkSafeFileName(filePath)) {
                LOGGER.error("downloadTextMarkContentFile - Loi duong dan file");
                return response;
            }
            File file;
            String tmpFilePath;
            // Tao file giai ma, dinh thong tin trang phu luc ky, anh chu ky, watermark
            // cho file van ban, cong van
            TextMarkSyncDAO textMarkSyncDAO = new TextMarkSyncDAO();
            // Check phan quyen doc file vanban
            Long type = textMarkSyncDAO.checkUserTextMarkSync(userIdVof2);
            if (type != null
                    && (textMarkSyncDAO.isTextMarkSync(textId, type)
                    || textMarkSyncDAO.isDocumentAutoDigitalSign(textId, type, null, null))) {
                tmpFilePath = textMarkSyncDAO.downFileDocument(req, textId, userName,
                        userIdVof2, isOriginal, filePath, type);
                file = new File(tmpFilePath);
                // File khong ton tai
                if (!file.exists()) {
                    LOGGER.error("downloadTextMarkContentFile - File null hoac file khong ton tai");
                    LOGGER.error("filePath: " + filePath);
                    LOGGER.error("tmpFilePath: " + tmpFilePath);
                    return response;
                }
                InputStream inputStream = new InputStreamWithFileDeletion(file);
                Response.ResponseBuilder responseBuilder = Response.ok(inputStream);
                responseBuilder.header("File-Name", fileName);
                if (file.length() > 0) {
                    responseBuilder.header("File-Size", file.length());
                } else {
                    responseBuilder.header("File-Size", 0);
                }
                response = responseBuilder.build();
            } else {
                // Loi khong co quyen doc file
                LOGGER.error("downloadTextMarkContentFile - userId: "
                        + userIdVof2 + " | textId: " + textId
                        + " - Loi khong co quyen download");
                Response.ResponseBuilder responseBuilder = Response.ok();
                responseBuilder.header("filePermission", "0");
                response = responseBuilder.build();
                return response;
            }
        } catch (Exception ex) {
            LOGGER.error("downloadTextMarkContentFile - Exception:", ex);
        }
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return response;
    }
}
