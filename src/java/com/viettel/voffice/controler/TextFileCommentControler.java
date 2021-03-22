/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.file.FilesCommentDraffDAO;
import com.viettel.voffice.database.dao.file.TextFileCommentDAO;
import com.viettel.voffice.database.dao.utils.DatabaseUtils;
import com.viettel.voffice.database.entity.EntityAttach;
import com.viettel.voffice.database.entity.EntityListTextNoteComment;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.User.Vof2_EntityUser;
import com.viettel.voffice.database.entity.text.EntityTextFileComment;
import com.viettel.voffice.database.entity.text.TextNoteEntity;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.LogUtils;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author hanhnq21
 */
public class TextFileCommentControler {

    private static final Logger LOGGER = Logger.getLogger(TextFileCommentControler.class);
    private static final String CLASS_NAME = TemplateController.class.getName();

    /**
     * cap nhat comment
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String updateTextFileComment(HttpServletRequest request,
            String data, String isSecurity) {
        String result;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            //het session
            LOGGER.error("updateTextFileComment - No session");
            result = FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        } else {
            Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
            if ((user2 == null || user2.getUserId() == null)) {
                //khong ton tai tren he thong voffice 2
                LOGGER.error("updateTextFileComment - user khong ton tai tren he thong 2");
                result = FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
            } else {
                EntityLog log = new EntityLog(request, CLASS_NAME);
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
                    String[] keys = new String[]{"type",
                        "fileId",
                        "path", "lstTextNote",
                        "fileCommentDraffId", "attachId"};
                    List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                    //loai tim kiem
                    String strType = listValue.get(0);
                    Long type = null;
                    Long fileCommentDraffId = null;
                    if (!CommonUtils.isEmpty(strType)) {
                        type = Long.valueOf(strType.trim());
                    }

                    //id file dinh kem
                    String strFileId = listValue.get(1);
                    Long fileId = null;
                    if (!CommonUtils.isEmpty(strFileId)) {
                        fileId = Long.valueOf(strFileId.trim());
                    }

                    //duong dan file
                    String path = listValue.get(2);
                    //file bieu mau dinh kem
                    String strLstTextNote = listValue.get(3);
                    String strFileCommentDraff = listValue.get(4);
                    if (!CommonUtils.isEmpty(strFileCommentDraff)) {
                        fileCommentDraffId = Long.valueOf(strFileCommentDraff.trim());
                    }
                    List<TextNoteEntity> lstTextNote = null;
                    if (!CommonUtils.isEmpty(strLstTextNote)) {
                        Type listTextNoteType = new TypeToken<ArrayList<TextNoteEntity>>() {
                        }.getType();
                        Gson gson = new Gson();
                        lstTextNote = gson.fromJson(strLstTextNote, listTextNoteType);
                    }

                    //lay thong tin comment
                    JSONArray arrComment = FunctionCommon.jsonGetArray("listComment", data);
                    List<EntityTextFileComment> lstComment = new ArrayList<>();
                    if (arrComment != null && arrComment.length() > 0) {
                        int arrCommentSize = arrComment.length();
                        EntityTextFileComment t;
                        for (int i = 0; i < arrCommentSize; i++) {
                            JSONObject innerObj = (JSONObject) arrComment.get(i);
                            Long page = null;
                            if (innerObj.has("page") && innerObj.getString("page") != null) {
                                page = Long.parseLong(innerObj.getString("page").trim());
                            }
                            String content = null;
                            if (innerObj.has("content") && innerObj.getString("content") != null) {
                                content = innerObj.getString("content").trim();
                            }
                            if (page != null && content != null) {
                                t = new EntityTextFileComment();
                                t.setPage(page);
                                byte[] decoded = Base64.decodeBase64(content.getBytes());
                                t.setArrContent(decoded);
                                lstComment.add(t);
                            }
                        }
                    }

                    if (type != null && fileId != null && path != null
                            && (!CommonUtils.isEmpty(lstComment) || (lstTextNote != null && !lstTextNote.isEmpty()))) {
                        Boolean resultUpdate = false;
                        EntityAttach attachUpdate;
                        EntityAttach attach;
                        TextFileCommentDAO textFileCommentDAO = new TextFileCommentDAO();
                        Long userId = user2.getUserId();
                        if (type.equals(0L)) {
                            //neu la van ban trinh ky
                            attach = textFileCommentDAO.getAttachTextById(fileId);
                            //them note
                            if (lstTextNote != null && !lstTextNote.isEmpty()) {
                                resultUpdate = false;
                                if (textFileCommentDAO.insertTextNote(fileId, userId, lstTextNote, type)) {
                                    resultUpdate = true;
                                } else {
                                    LOGGER.error("updateTextFileComment - loi khong insert duoc note vao DB id: " + fileId);
                                }
                            }
                            attachUpdate = TextFileCommentDAO.createCommentToFile(attach.getStorage(),
                                    attach.getPath(), userId, lstComment);
                        } else {
                            //neu la cong van
                            //neu la van ban trinh ky                         
                            attach = textFileCommentDAO.getAttachmentDocById(fileId);
                            attachUpdate = TextFileCommentDAO.createCommentToFile(attach.getStorage(),
                                    path, userId, lstComment);
                        }

                        if (attach != null) {

                            if (attachUpdate != null && attachUpdate.getFilePage() != null && attachUpdate.getFilePage() > 0
                                    && attachUpdate.getFileSize() != null && attachUpdate.getFileSize() > 1000L) {
                                //neu insert comment vao file thanh cong thuc hien luu ban draff
                                //neu insert comment vao file thanh cong
                                if (type.equals(0L)) {
                                    //Neu la van ban ky
                                    if (textFileCommentDAO.updateAttach(fileId, path, attachUpdate.getFilePage(),
                                            attachUpdate.getFileSize(), attachUpdate.getPath())) {
                                        resultUpdate = true;
                                    } else {
                                        LOGGER.error("updateTextFileComment - loi khong update duoc attach vao DB id: " + fileId);
                                    }
                                } else {
                                    //Neu la van ban phan danh sach van ban
                                    int isInsert = 0;
                                    if (fileCommentDraffId == null) {
                                        fileCommentDraffId = DatabaseUtils.getSeqId("attach_seq", 1);
                                        isInsert = 1;
                                    }
                                    FilesCommentDraffDAO fileCommentDraffDao = new FilesCommentDraffDAO();
                                    attachUpdate.setDocId(attach.getDocId());
                                    attachUpdate.setFileOrder(attach.getFileOrder());
                                    attachUpdate.setAttachId(attach.getAttachId());
                                    attachUpdate.setFileAttachmentId(attach.getFileAttachmentId());
                                    attachUpdate.setName(attach.getName());
                                    if (fileCommentDraffDao.insertOrUpdateFileCommentDraff(userId,
                                            attachUpdate, fileCommentDraffId, isInsert)) {
                                        attachUpdate.setFileCommentDraffId(fileCommentDraffId);
                                        resultUpdate = true;
                                        //Thuc hien luu text_note va tao file draff 
                                        if (lstTextNote != null && !lstTextNote.isEmpty()) {
                                            resultUpdate = false;
                                            if (textFileCommentDAO.insertTextNote(fileCommentDraffId, userId, lstTextNote, type)) {
                                                resultUpdate = true;
                                            } else {
                                                LOGGER.error("updateTextFileComment - loi khong insert duoc note vao DB id: " + fileId);
                                            }
                                        }
                                    } else {
                                        LOGGER.error("updateTextFileComment - loi khong insertOrUpdateFileCommentDraff: " + fileId);
                                    }
                                }

                            } else {
                                LOGGER.error("updateTextFileComment - loi khong insert duoc comment vao file id: " + fileId);
                            }
                        }
                        if (resultUpdate) {
                            //neu thanh cong
                            // Ghi log ket thuc chuc nang
                            LogUtils.logFunctionalEnd(log);
                            result = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, attachUpdate, aesKey);
                        } else {
                            //neu khong thanh cong
                            result = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, aesKey);
                        }
                    } else {
                        result = FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                    }
                } catch (JsonSyntaxException | NumberFormatException | JSONException ex) {
                    //co loi xay ra
                    LOGGER.error("updateTextFileComment - co loi xay ra - userId2: "
                            + user2.getUserId() + " - Exception: ", ex);
                    result = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
            }
        }
        return result;
    }

    /**
     * xoa bieu mau
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String deleteTextNote(HttpServletRequest request,
            String data, String isSecurity) {
        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup entityUserGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!entityUserGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(entityUserGroup.getEnumErrCode(), null, null);
        }

        // Kiem tra user id tren he thong 2
        Long userId = entityUserGroup.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
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
            String[] keys = new String[]{"textNoteId"};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String strTextNoteId = listValue.get(0);
            Long textNoteId = null;//id file dinh kem
            if (!CommonUtils.isEmpty(strTextNoteId)) {
                textNoteId = Long.parseLong(strTextNoteId);
            }
            if (textNoteId != null) {
                TextFileCommentDAO textFileCommentDAO = new TextFileCommentDAO();
                Boolean deleteResult = textFileCommentDAO.deleteTextNote(textNoteId, userId);
                if (deleteResult) {
                    //xoa thanh cong
                    // Ghi log ket thuc chuc nang
                    LogUtils.logFunctionalEnd(log);
                    return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, deleteResult, aesKey);
                } else {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
        } catch (JSONException ex) {
            LOGGER.error("delete textNote - Exception:", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * xoa bieu mau
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListTextNote(HttpServletRequest request,
            String data, String isSecurity) {
        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup entityUserGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!entityUserGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(entityUserGroup.getEnumErrCode(), null, null);
        }

        // Kiem tra user id tren he thong 2
        Long userId = entityUserGroup.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
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
            String[] keys = new String[]{"attachId", "fileAttachmentId"};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String strAttachId = listValue.get(0);
            String strAttachmentId = listValue.get(1);
//            Long attachId = null;//id file dinh kem van ban trinh ky
//            Long attachmentId = null; //id file van ban da ban hanh
            List<Long> lstAttach = new ArrayList<>();
            if (!CommonUtils.isEmpty(strAttachId)) {
                lstAttach.add(Long.parseLong(strAttachId));
            }
            if (!CommonUtils.isEmpty(strAttachmentId)) {
                lstAttach.add(Long.parseLong(strAttachmentId));
            }

            if (lstAttach.size() > 0) {
                TextFileCommentDAO textFileCommentDAO = new TextFileCommentDAO();
                List<TextNoteEntity> listTextNote = textFileCommentDAO.getListTextNote(lstAttach);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, listTextNote, aesKey);
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
        } catch (JSONException ex) {
            LOGGER.error("get list TextNote - Exception:", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * them moi note file
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String insertTextNote(HttpServletRequest request,
            String data, String isSecurity) {
        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup entityUserGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!entityUserGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(entityUserGroup.getEnumErrCode(), null, null);
        }

        // Kiem tra user id tren he thong 2
        Long userId = entityUserGroup.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
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
            String[] keys = new String[]{"attachId", "type", "lstTextNote"};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            //id file
            String strAttachId = listValue.get(0);
            Long attachId = null;
            if (!CommonUtils.isEmpty(strAttachId)) {
                attachId = Long.parseLong(strAttachId);
            }
            //linh vuc
            String strType = listValue.get(1);
            Long type = 1L;
            if (!CommonUtils.isEmpty(strType)) {
                type = Long.parseLong(strType);
            }
            //file bieu mau dinh kem
            String strLstTextNote = listValue.get(2);
            List<TextNoteEntity> lstTextNote = null;
            if (!CommonUtils.isEmpty(strLstTextNote)) {
                Type listTextNoteType = new TypeToken<ArrayList<TextNoteEntity>>() {
                }.getType();
                Gson gson = new Gson();
                lstTextNote = gson.fromJson(strLstTextNote, listTextNoteType);
            }
            if (attachId != null && lstTextNote != null && !lstTextNote.isEmpty()) {
                TextFileCommentDAO textFileCommentDAO = new TextFileCommentDAO();
                Boolean insertResult = textFileCommentDAO.insertTextNote(attachId, userId, lstTextNote, type);
                if (insertResult) {
                    // Ghi log ket thuc chuc nang
                    LogUtils.logFunctionalEnd(log);
                    return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, insertResult, aesKey);
                } else {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
        } catch (JSONException ex) {
            LOGGER.error("insertTextNote - Exception:", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * Xoa comment file van ban
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String resetFileCommentDraff(HttpServletRequest request,
            String data, String isSecurity) {
        //Lay thong tin user theo sessionId cua request
        //publicKey: neu la chung thu mem, versionCert: 1 check cua sim
        String[] keys = new String[]{"fileCommentDraffId"};
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request,
                data, keys);
        if (userGroup.getCheckSessionOk()) {
            //danh sach cac key de lay du lieu tu client
            int result = 0;
            List<String> listValue = userGroup.getListParamsFromClient();
            String strFileCommentDraff = listValue.get(0);

            //thuc hien lay trang thai chung thu cua nguoi dung bang cach kiem tra database
            if (!strFileCommentDraff.isEmpty()) {
                strFileCommentDraff = strFileCommentDraff.replace("[", "");
                strFileCommentDraff = strFileCommentDraff.replace("]", "");
                String[] arrFileCommentDraff = strFileCommentDraff.split(",");
                List<Long> arrFileCommentDraffId = new ArrayList<>();
                for (String fileCommentDraff : arrFileCommentDraff) {
                    arrFileCommentDraffId.add(Long.parseLong(fileCommentDraff.trim()));
                }
                // Long fileCommentDraffId = Long.parseLong(strFileCommentDraff);
                FilesCommentDraffDAO filesCommetDAO = new FilesCommentDraffDAO();
                result = filesCommetDAO.resetFileCommentDraff(userGroup.getUserId2(), arrFileCommentDraffId);
            } else {
                return FunctionCommon.responseResult(ErrorCode.ERR_NODATA,
                        result, userGroup);
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result,
                    userGroup);
        } else {
            //truong hop bị timeout
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(),
                    null, null);
        }
    }

    /**
     * cap nhat comment
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String updateListTextFileComment(HttpServletRequest request,
            String data, String isSecurity) {
        String result;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            //het session
            LOGGER.error("updateListTextFileComment - No session");
            result = FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        } else {
            Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
            if ((user2 == null || user2.getUserId() == null)) {
                //khong ton tai tren he thong voffice 2
                LOGGER.error("updateListTextFileComment - user khong ton tai tren he thong 2");
                result = FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
            } else {
                Long userId = user2.getUserId();
                EntityLog log = new EntityLog(request, CLASS_NAME);
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
                    String[] keys = new String[]{"type",
                        "listTextNoteComment",};
                    List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                    //loai tim kiem
                    String strType = listValue.get(0);
                    Long type = null;
                    Long fileCommentDraffId;
                    if (!CommonUtils.isEmpty(strType)) {
                        type = Long.valueOf(strType.trim());
                    }
                    //List danh sach comment
                    //file bieu mau dinh kem
                    String strLstTextNoteComment = listValue.get(1);
                    List<EntityListTextNoteComment> lstTextNoteComment = null;
                    if (!CommonUtils.isEmpty(strLstTextNoteComment)) {
                        Type lstTextNoteCommentType = new TypeToken<ArrayList<EntityListTextNoteComment>>() {
                        }.getType();
                        Gson gson = new Gson();
                        lstTextNoteComment = gson.fromJson(strLstTextNoteComment, lstTextNoteCommentType);
                    }
                    //id file dinh kem
                    String strFileId;
                    Long fileId;
                    //duong dan file
                    String path;
                    String strFileCommentDraff;
                    //Id file tu van ban trinh ky
                    List<TextNoteEntity> lstTextNote;
                    List<EntityTextFileComment> lstComment = null;
                    List<EntityTextFileComment> arrComment;
                    EntityTextFileComment t;
                    Boolean resultUpdate = false;
                    EntityAttach attachUpdate = null;
                    List<EntityAttach> lstAttachUpdate = new ArrayList<>();
                    for (EntityListTextNoteComment lstNoteComment : lstTextNoteComment) {

                        fileId = null;
                        fileCommentDraffId = null;
                        strFileId = lstNoteComment.getFileId();
                        if (!CommonUtils.isEmpty(strFileId)) {
                            fileId = Long.valueOf(strFileId.trim());
                        }
                        path = lstNoteComment.getPath();
                        strFileCommentDraff = lstNoteComment.getFileCommentDraffId();
                        if (!CommonUtils.isEmpty(strFileCommentDraff)) {
                            fileCommentDraffId = Long.valueOf(strFileCommentDraff.trim());
                        }
                        //List add note pdf
                        lstTextNote = lstNoteComment.getLstTextNote();
                        //List comment pdf
                        arrComment = lstNoteComment.getListComment();
                        if (!CommonUtils.isEmpty(arrComment)) {
                            lstComment = new ArrayList<>();
                            int arrCommentSize = arrComment.size();
                            for (int i = 0; i < arrCommentSize; i++) {

                                if (arrComment.get(i).getPage() != null
                                        && !CommonUtils.isEmpty(arrComment.get(i).getContent())) {
                                    t = new EntityTextFileComment();
                                    t.setPage(arrComment.get(i).getPage());
                                    byte[] decoded = Base64.decodeBase64(arrComment.get(i).getContent().getBytes());
                                    t.setArrContent(decoded);
                                    lstComment.add(t);
                                }
                            }
                        }
                        if (type != null && fileId != null && path != null
                                && (!CommonUtils.isEmpty(lstComment) || !CommonUtils.isEmpty(lstTextNote))) {

                            EntityAttach attach;
                            TextFileCommentDAO textFileCommentDAO = new TextFileCommentDAO();

                            if (type.equals(0L)) {
                                //neu la van ban trinh ky
                                attach = textFileCommentDAO.getAttachTextById(fileId);
                                //them note
                                if (lstTextNote != null && !lstTextNote.isEmpty()) {
                                    resultUpdate = false;
                                    if (textFileCommentDAO.insertTextNote(fileId, userId, lstTextNote, type)) {
                                        resultUpdate = true;
                                    } else {
                                        LOGGER.error("updateListTextFileComment - loi khong insert duoc note vao DB id: " + fileId);
                                    }
                                }
                                attachUpdate = TextFileCommentDAO.createCommentToFile(attach.getStorage(),
                                        attach.getPath(), userId, lstComment);
                                attachUpdate.setAttachId(fileId);
                            } else {
                                //neu la cong van
                                attach = textFileCommentDAO.getAttachmentDocById(fileId);
                                attachUpdate = TextFileCommentDAO.createCommentToFile(attach.getStorage(),
                                        path, userId, lstComment);
                            }

                            if (attach != null) {
                                if (attachUpdate != null && attachUpdate.getFilePage() != null && attachUpdate.getFilePage() > 0
                                        && attachUpdate.getFileSize() != null && attachUpdate.getFileSize() > 1000L) {
                                    //neu insert comment vao file thanh cong thuc hien luu ban draff
                                    //neu insert comment vao file thanh cong
                                    if (type.equals(0L)) {
                                        //Neu la van ban ky
                                        if (textFileCommentDAO.updateAttach(fileId, path, attachUpdate.getFilePage(),
                                                attachUpdate.getFileSize(), attachUpdate.getPath())) {
                                            resultUpdate = true;
                                        } else {
                                            LOGGER.error("updateListTextFileComment - loi khong update duoc attach vao DB id: " + fileId);
                                        }
                                    } else {
                                        //Neu la van ban phan danh sach van ban
                                        int isInsert = 0;
                                        if (fileCommentDraffId == null) {
                                            fileCommentDraffId = DatabaseUtils.getSeqId("attach_seq", 1);
                                            isInsert = 1;
                                        }
                                        FilesCommentDraffDAO fileCommentDraffDao = new FilesCommentDraffDAO();
                                        attachUpdate.setDocId(attach.getDocId());
                                        attachUpdate.setFileOrder(attach.getFileOrder());
                                        attachUpdate.setAttachId(attach.getAttachId());
                                        attachUpdate.setFileAttachmentId(attach.getFileAttachmentId());
                                        attachUpdate.setName(attach.getName());
                                        if (fileCommentDraffDao.insertOrUpdateFileCommentDraff(userId,
                                                attachUpdate, fileCommentDraffId, isInsert)) {
                                            attachUpdate.setFileCommentDraffId(fileCommentDraffId);
                                            resultUpdate = true;
                                            //Thuc hien luu text_note va tao file draff 
                                            if (lstTextNote != null && !lstTextNote.isEmpty()) {
                                                resultUpdate = false;
                                                if (textFileCommentDAO.insertTextNote(fileCommentDraffId, userId, lstTextNote, type)) {
                                                    resultUpdate = true;
                                                } else {
                                                    LOGGER.error("updateListTextFileComment - loi khong insert duoc note vao DB id: " + fileId);
                                                }
                                            }
                                        } else {
                                            LOGGER.error("updateListTextFileComment - loi khong insertOrUpdateFileCommentDraff: " + fileId);
                                        }
                                    }

                                } else {
                                    LOGGER.error("updateListTextFileComment - loi khong insert duoc comment vao file id: " + fileId);
                                }
                            }

                        }
                        if (attachUpdate != null) {
                            lstAttachUpdate.add(attachUpdate);
                        }
                    }
                    if (resultUpdate) {
                        //neu thanh cong
                        // Ghi log ket thuc chuc nang
                        LogUtils.logFunctionalEnd(log);
                        result = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, lstAttachUpdate, aesKey);
                    } else {
                        //neu khong thanh cong
                        result = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, aesKey);
                    }

                } catch (JsonSyntaxException | NumberFormatException | JSONException ex) {
                    //co loi xay ra
                    LOGGER.error("updateListTextFileComment - co loi xay ra - userId2: "
                            + user2.getUserId() + " - Exception: ", ex);
                    result = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
            }
        }
        return result;
    }

    /**
     * Xoa comment file van ban trong module trinh ky
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String resetTextAttachComment(HttpServletRequest request,
            String data, String isSecurity) {
        //Lay thong tin user theo sessionId cua request
        //publicKey: neu la chung thu mem, versionCert: 1 check cua sim
        String[] keys = new String[]{"lstAttachId", "textId"};
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request,
                data, keys);
        if (userGroup.getCheckSessionOk()) {
            //danh sach cac key de lay du lieu tu client
            int result = 0;
            List<String> listValue = userGroup.getListParamsFromClient();
            String strAttach = listValue.get(0);
            String strTextId = listValue.get(1);
            Long textId = null;
            if (!CommonUtils.isEmpty(strTextId)) {
                textId = Long.valueOf(strTextId.trim());
            }
            //thuc hien lay trang thai chung thu cua nguoi dung bang cach kiem tra database
            if (!strAttach.isEmpty()) {
                strAttach = strAttach.replace("[", "");
                strAttach = strAttach.replace("]", "");
//                System.out.println("strFileCommentDraff: " + strAttach);
                String[] arrAttach = strAttach.split(",");
                List<Long> arrAttachId = new ArrayList<>();
                for (String attach : arrAttach) {
                    arrAttachId.add(Long.parseLong(attach.trim()));
                }
                // Long fileCommentDraffId = Long.parseLong(strFileCommentDraff);
                TextFileCommentDAO textFileCommentDAO = new TextFileCommentDAO();
                result = textFileCommentDAO.resetTextAttachComment(arrAttachId, textId);
            } else {
                return FunctionCommon.responseResult(ErrorCode.ERR_NODATA,
                        result, userGroup);
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result,
                    userGroup);
        } else {
            //truong hop bị timeout
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(),
                    null, null);
        }
    }
}
