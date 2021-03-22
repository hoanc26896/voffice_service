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
import com.viettel.voffice.database.dao.file.MeetingFileCommentDAO;
import com.viettel.voffice.database.dao.file.TextFileCommentDAO;
import com.viettel.voffice.database.entity.EntityAttach;
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
public class MeetingFileCommentControler {

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
    public String updateFileMeetingComment(HttpServletRequest request,
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
                        "attachId"};
                    List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
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

                    if (fileId != null && path != null
                            && (!CommonUtils.isEmpty(lstComment) || (!CommonUtils.isEmpty(lstTextNote)))) {
                        Boolean resultUpdate = false;
                        EntityAttach attachUpdate;
                        EntityAttach attach;
                        MeetingFileCommentDAO meetingFileCommentDAO = new MeetingFileCommentDAO();
                        Long userId = user2.getUserId();
                        attach = meetingFileCommentDAO.getAttachmentMeetingById(userId, fileId);
//                        LOGGER.error("attach.getStorage() " + attach.getStorage() + "attach.getPath() " + attach.getPath());
                        attachUpdate = MeetingFileCommentDAO.createCommentToFile(attach.getStorage(),
                                attach.getPath(), userId, lstComment);
//                        LOGGER.error("attachUpdate " + attachUpdate.getFilePage() + "getFileSize " + attachUpdate.getFileSize());
                        if (attachUpdate != null && attachUpdate.getFilePage() != null && attachUpdate.getFilePage() > 0
                                && attachUpdate.getFileSize() != null && attachUpdate.getFileSize() > 1000L) {
                            //neu insert comment vao file thanh cong thuc hien luu ban draff
                            attachUpdate.setDocId(attach.getDocId());
                            attachUpdate.setFileOrder(attach.getFileOrder());
                            attachUpdate.setAttachId(attach.getFileAttachmentId());
                            attachUpdate.setFileAttachmentId(attach.getFileAttachmentId());
                            attachUpdate.setName(attach.getName());
                            Long isInsert;
                            isInsert = meetingFileCommentDAO.checkExitsMeetingFileCommentByUser(userGroup.getVof2_ItemEntityUser().getUserId(),
                                    fileId);
                            if (isInsert != null) {
                                if (meetingFileCommentDAO.insertOrUpdateFileMeetingCommentDraff(userId,
                                        attachUpdate, isInsert.intValue())) {
//                                attachUpdate.setFileCommentDraffId(fileCommentDraffId);
                                    resultUpdate = true;

                                    if (lstTextNote != null && !lstTextNote.isEmpty()) {
                                        //Thuc hien luu text_note va tao file draff 
                                        if (!meetingFileCommentDAO.insertTextNote(attachUpdate.getAttachId(), userId, lstTextNote, 1L)) {
                                            resultUpdate = false;
                                            LOGGER.error("updateFileMeetingComment - loi khong insertTextNote AttachId=: " + attach.getAttachId());
                                        }
                                    }

//                                    if (!meetingFileCommentDAO.updateAttachMeeting(fileId, attachUpdate.getPath())) {
//                                        //Thuc hien update lai file lich hop sau khi comment
//                                        resultUpdate = false;
//                                        LOGGER.error("updateFileMeetingComment - loi khong updateAttachMeeting fileId=: " + fileId);
//                                    }
                                } else {
                                    LOGGER.error("updateFileMeetingComment - loi khong insertOrUpdateFileMeetingCommentDraff userId=: " + userId);
                                }
                            }

                        } else {
                            LOGGER.error("updateFileMeetingComment - loi khong insert duoc comment vao file id: " + fileId);
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
                    LOGGER.error("updateFileMeetingComment - co loi xay ra - userId2: "
                            + user2.getUserId() + " - Exception: ", ex);
                    result = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
            }
        }
        return result;
    }

    /**
     * lay danh sach textNote
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListMeetingNote(HttpServletRequest request,
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
                MeetingFileCommentDAO meetingFileCommentDAO = new MeetingFileCommentDAO();
                List<TextNoteEntity> listTextNote = meetingFileCommentDAO.getListMeetingNote(userId, lstAttach);
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
     * Xoa comment file van ban trong module trinh ky
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String resetMeetingAttachComment(HttpServletRequest request,
            String data, String isSecurity) {

        String[] keys = new String[]{"lstAttachId"};
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request,
                data, keys);
        if (userGroup.getCheckSessionOk()) {
            //danh sach cac key de lay du lieu tu client
            int result = 0;
            List<String> listValue = userGroup.getListParamsFromClient();
            String strAttach = listValue.get(0);

            if (!strAttach.isEmpty()) {
                strAttach = strAttach.replace("[", "");
                strAttach = strAttach.replace("]", "");
                String[] arrAttach = strAttach.split(",");
                List<Long> arrAttachId = new ArrayList<>();
                for (String attach : arrAttach) {
                    arrAttachId.add(Long.parseLong(attach.trim()));
                }
                MeetingFileCommentDAO meetingFileCommentDAO = new MeetingFileCommentDAO();
                result = meetingFileCommentDAO.resetMeetingAttachComment(userGroup.getVof2_ItemEntityUser().getUserId(), arrAttachId);
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
     * Luu so tay cua nguoi dung theo phien cuoc hop
     *
     * @param request
     * @param data
     *
     * @return
     */
    public String saveMeetingNoteBook(HttpServletRequest request,
            String data) {

        String[] keys = new String[]{"meetingId", "content"};
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request,
                data, keys);
        if (userGroup.getCheckSessionOk()) {
            //danh sach cac key de lay du lieu tu client
            int result = 0;
            List<String> listValue = userGroup.getListParamsFromClient();
            String meetingId = listValue.get(0);
            String content = listValue.get(1);

            if (!meetingId.isEmpty() && FunctionCommon.isNumeric(meetingId)) {

                MeetingFileCommentDAO meetingFileCommentDAO = new MeetingFileCommentDAO();

                Long numExits = meetingFileCommentDAO.checkExitsMeetingNoteBook(userGroup.getVof2_ItemEntityUser().getUserId(),
                        Long.parseLong(meetingId));
                if (numExits != null) {
                    //Ket qua thuc hien save so tay
                    result = meetingFileCommentDAO.insertOrUpdateMeetingNoteBook(userGroup.getVof2_ItemEntityUser().getUserId(),
                            Long.parseLong(meetingId), content, numExits.intValue());
                }

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
     * lay danh sach textNote
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String updateOrDelMeetingNoteFile(HttpServletRequest request,
            String data) {
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

        // Lay AES Key
        aesKey = entityUserGroup.getStrAesKey();
        // Giai ma data client gui len
        data = SecurityControler.decodeDataByAes(aesKey, data);

        int result = 0;
        // Lay ma nhan vien
        String cardId = entityUserGroup.getCardId();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{"metingNoteId", "content", "type"};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String strMetingNoteId = listValue.get(0);
            String content = listValue.get(1);
            String strType = listValue.get(2);
//            Long attachId = null;//id file dinh kem van ban trinh ky
//        

            if (!strMetingNoteId.isEmpty() && FunctionCommon.isNumeric(strMetingNoteId)) {
                Long type = null;
                Long metingNoteId = Long.parseLong(strMetingNoteId);
                if (!strType.isEmpty() && FunctionCommon.isNumeric(strType)) {
                    type = Long.parseLong(strType);
                }
                MeetingFileCommentDAO meetingFileCommentDAO = new MeetingFileCommentDAO();
                result = meetingFileCommentDAO.updateOrDelMeetingNoteFile(userId, metingNoteId, content, type);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
        } catch (JSONException ex) {
            LOGGER.error("get list TextNote - Exception:", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }
}
