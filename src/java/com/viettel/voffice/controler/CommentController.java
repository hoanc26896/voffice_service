/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.comments.CommentDAO;
import com.viettel.voffice.database.dao.document.DocumentInStaffDAO;
import com.viettel.voffice.database.dao.meeting.DemoDAO;
import com.viettel.voffice.database.dao.meeting.MeetingDAO;
import com.viettel.voffice.database.entity.EntityDocument;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityPersonalStorage;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.User.EntityUser;
import com.viettel.voffice.database.entity.User.Vof2_EntityUser;
import com.viettel.voffice.database.entity.comments.CommentEntity;
import com.viettel.voffice.database.entity.task.EntityMeeting;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.LogUtils;

/**
 *
 * @author luanvd
 */
@SuppressWarnings("deprecation")
public class CommentController {

    /**
     * Log loi
     */
    private static final Logger logger = Logger.getLogger(CommentController.class);

    // Lay ra ten class bao gom ca ten package
    private static final String CLASS_NAME = CommentController.class.getName();

    /**
     * <b>Lay danh sach comment</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListComments(HttpServletRequest request,
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
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.CMT_REFERENCE_ID,
                ConstantsFieldParams.CMT_OBJECT_TYPE,
                ConstantsFieldParams.CMT_START_ID,
                ConstantsFieldParams.CMT_END_ID,
                ConstantsFieldParams.CMT_PAGE_SIZE,
                ConstantsFieldParams.CMT_TYPE_LOAD};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long referenceId;
            if (listValue.get(0) != null && listValue.get(0).trim().length() > 0) {
                referenceId = Long.parseLong(listValue.get(0));
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
            Integer objectType;
            if (listValue.get(1) != null && listValue.get(1).trim().length() > 0) {
                objectType = Integer.parseInt(listValue.get(1));
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
            Long startId = 0L;
            if (listValue.get(2) != null && listValue.get(2).trim().length() > 0) {
                startId = Long.parseLong(listValue.get(2));
            }
            Long endId = 0L;
            if (listValue.get(3) != null && listValue.get(3).trim().length() > 0) {
                endId = Long.parseLong(listValue.get(3));
            }
            Long pageSize = 0L;
            if (listValue.get(4) != null && listValue.get(4).trim().length() > 0) {
                pageSize = Long.parseLong(listValue.get(4));
            }
            Integer typeLoad = 0;
            if (listValue.get(5) != null && listValue.get(5).trim().length() > 0) {
                typeLoad = Integer.parseInt(listValue.get(5));
            }
            CommentDAO cmtDAO = new CommentDAO();
            Object result = cmtDAO.getListComment(userId, referenceId, objectType, typeLoad, startId, endId, pageSize);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            if (result == null) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            }
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException ex) {
            logger.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * <b>Them moi comment</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String addComment(HttpServletRequest request,
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
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.CMT_REFERENCE_ID,
                ConstantsFieldParams.CMT_OBJECT_TYPE,
                ConstantsFieldParams.CMT_CONTENT};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long referenceId;
            if (listValue.get(0) != null && listValue.get(0).trim().length() > 0) {
                referenceId = Long.parseLong(listValue.get(0));
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
            Integer objectType;
            if (listValue.get(1) != null && listValue.get(1).trim().length() > 0) {
                objectType = Integer.parseInt(listValue.get(1));
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
            String content = listValue.get(2).toString().trim();

            CommentEntity commentE = new CommentEntity();
            commentE.setContent(content);
            commentE.setCreateBy(userId);
            commentE.setObjectType(objectType);
            commentE.setReferentId(referenceId);
            CommentDAO cmtDAO = new CommentDAO();
            Object result = cmtDAO.insertComment(userId, commentE);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            if (result == null) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            }
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException ex) {
            logger.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String supportFinacialText(HttpServletRequest request, String data,
            String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);

        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            logger.error("supportFinacialText - No session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }

        // Neu user hoac user id tren he thong 1&2 null
        // -> Tra ve thong bao khong co quyen
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        if ((user1 == null || user1.getUserId() == null)
                && (user2 == null || user2.getUserId() == null)) {
            logger.error("supportFinacialText - user hoac userId tren he thong 1&2 null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        String cardId = (user1 != null && CommonUtils.isEmpty(user1.getStrCardNumber()))
                ? user1.getStrCardNumber() : user2.getStrCardNumber();
        log.setUserName(cardId);
        // Id nguoi dung tren he thong 1
        Long userId1 = null;
        if (user1 != null) {
            userId1 = user1.getUserId();
        }
        // Id nguoi dung tren he thong 2
        Long userId2 = user2 == null ? null : user2.getUserId();

        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        // Log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.TEXT_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String id = listValue.get(0);

            DemoDAO textDAO = new DemoDAO();
            boolean result = textDAO.supportFinacialText(id);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException | NumberFormatException ex) {
            logger.error("supportFinacialText - userId1: " + userId1 + " - userId2: "
                    + userId2 + " - Exception: ", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        } catch (Exception ex) {
            logger.error("supportFinacialText - userId1: " + userId1 + " - userId2: "
                    + userId2 + " - Exception: ", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    public String supportSystem(HttpServletRequest request, String data,
            String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);

        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            logger.error("No session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }

        // Neu user hoac user id tren he thong 1&2 null
        // -> Tra ve thong bao khong co quyen
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        if ((user1 == null || user1.getUserId() == null)
                && (user2 == null || user2.getUserId() == null)) {
            logger.error("user hoac userId tren he thong 1&2 null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        String cardId = (user1 != null && CommonUtils.isEmpty(user1.getStrCardNumber()))
                ? user1.getStrCardNumber() : user2.getStrCardNumber();
        log.setUserName(cardId);
        // Id nguoi dung tren he thong 1
        Long userId1 = null;
        if (user1 != null) {
            userId1 = user1.getUserId();
        }
        // Id nguoi dung tren he thong 2
        Long userId2 = user2 == null ? null : user2.getUserId();

        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        // Log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.TEXT_ID,
                ConstantsFieldParams.FUNCTION_TYPE,
                ConstantsFieldParams.DATA_EXCUTE
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String text = listValue.get(0);
            String function = listValue.get(1);
            String execute = listValue.get(2);

            DemoDAO textDAO = new DemoDAO();
            boolean result = textDAO.supportSystem(text, (function != null && !function.isEmpty()), (execute != null && !execute.isEmpty()));
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException | NumberFormatException ex) {
            logger.error("userId1: " + userId1 + " - userId2: "
                    + userId2 + " - Exception: ", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        } catch (Exception ex) {
            logger.error("userId1: " + userId1 + " - userId2: "
                    + userId2 + " - Exception: ", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }
    
    /**
     * Insert lich su luu tru tai lieu ca nhan
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String savePersonalStorage(HttpServletRequest request,
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
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[] { "type", "note", "objectId", "isDelete" };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long objectType = CommonUtils.isInteger(listValue.get(0)) ? Long.valueOf(listValue.get(0)) : null;
            String note = listValue.get(1);
            Long objectId = CommonUtils.isInteger(listValue.get(2)) ? Long.valueOf(listValue.get(2)) : null;
            boolean isDeleted = "1".equals(listValue.get(3));

            if (objectType == null) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
            
            String code = null;
            String content = null;
            String comment = null;
            Integer type = null;
            
            if (objectType.intValue() == 1) {
                DocumentInStaffDAO disDAO = new DocumentInStaffDAO();
                EntityDocument doc = disDAO.getDocumentInStaffDetail(objectId, false);
                if (doc != null) {
                   code = doc.getCode();
                   content = doc.getTitle();
                   comment = doc.getCommentContent();
                   type = doc.getSendType();
                }
            }
            
            if (objectType.intValue() == 3) {
                DocumentInStaffDAO disDAO = new DocumentInStaffDAO();
                EntityDocument doc = disDAO.getDocumentInStaffDetail(objectId, true);
                if (doc != null) {
                   code = doc.getCode();
                   content = doc.getTitle();
                   comment = doc.getCommentContent();
                   type = doc.getSendType();
                }
            }
            
            if (objectType.intValue() == 2) {
                MeetingDAO meetingDAO = new MeetingDAO();
                EntityMeeting obj = meetingDAO.getMeetingByMeetingId(objectId);
                if (obj != null) {
                    content = obj.getTitle();
                }
            }
            
            CommentDAO cmtDAO = new CommentDAO();
            boolean result = cmtDAO.savePersonalStorage(objectType, note, objectId, userId, isDeleted, content, comment, code, type);
            
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result ? 1 : 0, aesKey);
        } catch (JSONException ex) {
            logger.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }
    
    /**
     * Kiem tra trang thai da luu vao kho luu tru ca nhan
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String checkSavedPersonalStorage(HttpServletRequest request,
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
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[] { "type", "objectId" };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long type = CommonUtils.isInteger(listValue.get(0)) ? Long.valueOf(listValue.get(0)) : null;
            Long objectId = CommonUtils.isInteger(listValue.get(1)) ? Long.valueOf(listValue.get(1)) : null;

            if (type == null || objectId == null) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
            
            CommentDAO cmtDAO = new CommentDAO();
            Long result = cmtDAO.checkSavedPersonalStorage(type, objectId, 0L, userId);
            
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result != null ? 1 : 0, aesKey);
        } catch (JSONException ex) {
            logger.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }
    
    /**
     * Tim kiem lich su tai lieu ca nhan
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String searchPeronalStorage(HttpServletRequest request,
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
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[] { "search", "type", "typeDetail", "startPage", "pageLoad" , "isMobile" };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            
            String search = listValue.get(0);
            Long type = CommonUtils.isInteger(listValue.get(1)) ? Long.valueOf(listValue.get(1)) : null;
            Long typeDetail = CommonUtils.isInteger(listValue.get(2)) ? Long.valueOf(listValue.get(2)) : null;
            Long startPage = CommonUtils.isInteger(listValue.get(3)) ? Long.valueOf(listValue.get(3)) : null;
            Long pageLoad = CommonUtils.isInteger(listValue.get(4)) ? Long.valueOf(listValue.get(4)) : null;
            boolean isMobile = "1".equals(listValue.get(5));

            CommentDAO cmtDAO = new CommentDAO();
            List<EntityPersonalStorage> result = cmtDAO.searchPeronalStorage(search, type, typeDetail, userId,
                    startPage, pageLoad, isMobile);
            
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException ex) {
            logger.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }
}
