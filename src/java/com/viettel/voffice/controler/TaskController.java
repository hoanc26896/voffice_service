/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.document.DocumentDAO;
import com.viettel.voffice.database.dao.sms.SmsDAO;
import com.viettel.voffice.database.dao.staff.OrgDAO;
import com.viettel.voffice.database.dao.staff.StaffDAO;
import com.viettel.voffice.database.dao.task.TaskDAO;
import com.viettel.voffice.database.dao.task.TaskDataBaseDao;
import com.viettel.voffice.database.entity.EntityFileAttachment;
import com.viettel.voffice.database.entity.EntityRatioConfigDetail;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.EntityVhrEmployee;
import com.viettel.voffice.database.entity.EntityVhrOrg;
import com.viettel.voffice.database.entity.User.Vof2_EntityUser;
import com.viettel.voffice.database.entity.task.EntityRequest;
import com.viettel.voffice.database.entity.task.EntitySourceMap;
import com.viettel.voffice.database.entity.task.EntityTask;
import com.viettel.voffice.database.entity.task.EntityTaskAssessment;
import com.viettel.voffice.database.entity.task.EntityTaskAssignment;
import com.viettel.voffice.database.entity.task.EntityTaskProcess;
import com.viettel.voffice.database.entity.task.EntityTaskReceiver;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.FileUtils;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import com.viettel.voffice.utils.LogUtils;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.User.EntityUser;
import com.viettel.voffice.database.entity.task.EntityAverageTask;
import com.viettel.voffice.database.entity.task.EntityEmpRating;
import com.viettel.voffice.database.entity.task.EntityEnforcement;
import com.viettel.voffice.database.entity.task.EntityRatio;
import com.viettel.voffice.database.entity.task.EntityTaskApproval;
import com.viettel.voffice.database.entity.task.RatioConfigDetail;
import java.lang.reflect.Type;
import java.util.LinkedList;
import org.json.JSONArray;

/**
 *
 * @author vinhnq13
 */
public class TaskController {

    // Log file
    private static final Logger LOGGER = Logger.getLogger(TaskController.class);
    // Lay ra ten class bao gom ca ten package
    private static final String CLASS_NAME = TaskController.class.getName();

    /**
     * <b>Lay danh sach cong viec ca nhan</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListTask(HttpServletRequest request, String data,
            String isSecurity) {

        String[] keys = new String[]{
            ConstantsFieldParams.IS_COUNT,
            ConstantsFieldParams.KEYWORD,
            ConstantsFieldParams.TYPE,
            ConstantsFieldParams.STATUS,
            ConstantsFieldParams.START_DATE_FROM,
            ConstantsFieldParams.START_DATE_TO,
            ConstantsFieldParams.START_RECORD,
            ConstantsFieldParams.PAGE_SIZE,
            ConstantsFieldParams.ENFORCEMENT_ID,
            ConstantsFieldParams.IS_WEB,
            ConstantsFieldParams.PERIOD,
            ConstantsFieldParams.STATE_APPROVAL,
            ConstantsFieldParams.ORG_COMMANDER_ID,
            ConstantsFieldParams.TASK_NAME,
            ConstantsFieldParams.TASK_CONTENT,
            ConstantsFieldParams.TASK_FIELD_ID,
            ConstantsFieldParams.TASK_TYPE,
            ConstantsFieldParams.TASK_TYPE2,
            ConstantsFieldParams.TASK_COMMANDER_ID,
            ConstantsFieldParams.START_TIME_FROM,
            ConstantsFieldParams.START_TIME_TO,
            ConstantsFieldParams.END_TIME_FROM,
            ConstantsFieldParams.END_TIME_TO,
            ConstantsFieldParams.MISSION_FREQUENCE_UPDATE,
            ConstantsFieldParams.STATUS_CLOSE,
            ConstantsFieldParams.STR_SOURCE_TYPE,
            ConstantsFieldParams.IS_SEARCH_ADVANCED,
            ConstantsFieldParams.DOCUMENT_ID,
            ConstantsFieldParams.IS_EXPORT,
            ConstantsFieldParams.REQ_LIST_STATUS,
            ConstantsFieldParams.TASK_OBJECT_TYPE
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getListTask (Lay danh sach cong viec) - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        if (!userGroup.checkUserId2()) {
            LOGGER.error("getListTask (Lay danh sach cong viec) - Khong co thong"
                    + " tin user tren he thong 2!");
            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
        }
        List<Long> listManagementOrgId = userGroup.getVof2_ItemEntityUser().getListManagementOrg();
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
//             isCount = 1: Lay so luong cong viec ca nhan
            String isCount = listValue.get(0);
            String keyword = listValue.get(1);
            // Loai
            int type = 0;
            String strType = listValue.get(2);
            if (!CommonUtils.isEmpty(strType)) {
                type = Integer.parseInt(strType);
            }
            // Trang thai
            int status = 0;
            String strStatus = listValue.get(3);
            if (!CommonUtils.isEmpty(strStatus)) {
                status = Integer.parseInt(strStatus);
            }
            // Ngay bat dau tu ngay
            String startDateFrom = listValue.get(4);
            // Ngay bat dau den ngay
            String startDateTo = listValue.get(5);
            // Vi tri lay ra
            Long startRecord = null;
            String strStartRecord = listValue.get(6);
            if (!CommonUtils.isEmpty(strStartRecord)) {
                startRecord = Long.parseLong(strStartRecord);
            }
            // So luong ban ghi lay ra
            Long pageSize = null;
            String strPageSize = listValue.get(7);
            if (!CommonUtils.isEmpty(strPageSize)) {
                pageSize = Long.parseLong(strPageSize);
            }
            // Id nguoi thuc hien
            Long enforcementId = 0L;
            String strEnforcementId = listValue.get(8);
            if (!CommonUtils.isEmpty(strEnforcementId)) {
                enforcementId = Long.parseLong(strEnforcementId);
            }
            //Begin::cuongnv::5/1/2017
            Integer isWeb = 0;
            try {
                isWeb = Integer.parseInt(listValue.get(9));
            } catch (NumberFormatException ex) {
            }
            //Ky danh gia phe duyet
            String period = listValue.get(10);
            if(CommonUtils.isEmpty(period)){
                period = null;
            }
            // Trang thai duyet
            Integer stateApproval = null;
            try {
                stateApproval = Integer.parseInt(listValue.get(11));
            } catch (NumberFormatException ex) {
            }
            //Id don vi nguoi giao
            Long orgCommanderId = null;
            try {
                orgCommanderId = Long.parseLong(listValue.get(12));
            } catch (NumberFormatException ex) {
            }
            // tên công việc
            String taskName = listValue.get(13);
            // nội dung công việc
            String content = listValue.get(14);
            // id ngành
            Long fieldId = null;
            try {
                fieldId = Long.parseLong(listValue.get(15));
            } catch (NumberFormatException ex) {
            }
            // 1 - công việc chức năng; 2 - công việc nề nếp
            Integer taskType = null;
            try {
                taskType = Integer.parseInt(listValue.get(16));
            } catch (NumberFormatException ex) {
            }
            // 1 - công việc thường xuyên; 2 - công việc đột xuất
            Integer taskType2 = null;
            try {
                taskType2 = Integer.parseInt(listValue.get(17));
            } catch (NumberFormatException ex) {
            }
            // id người giao việc
            Long commanderId = null;
            try {
                commanderId = Long.parseLong(listValue.get(18));
            } catch (NumberFormatException ex) {
            }
            // ngày bat dau từ
            String startTimeFrom = listValue.get(19);
            // ngày bat dau đến
            String startTimeTo = listValue.get(20);
            // ngày hoàn thành từ
            String endTimeFrom = listValue.get(21);
            // ngày hoàn thành đến
            String endTimeTo = listValue.get(22);
            // tần suất cập nhật (1 - theo ngày; 2 - theo tuần; 3 - theo tháng)
            Integer frequenceUpdate = null;
            try {
                frequenceUpdate = Integer.parseInt(listValue.get(23));
            } catch (NumberFormatException ex) {
            }
            // trạng thái đóng (0- chưa đóng; 1 - đă đóng; null or #(0,1) - tất cả)
            Integer statusClose = null;
            try {
                statusClose = Integer.parseInt(listValue.get(24));
            } catch (NumberFormatException ex) {
            }
            // loại nguồn gốc (1: Theo kế hoạch cá nhân (Không link)
            Integer sourceType = null;
            try {
                sourceType = Integer.parseInt(listValue.get(25));
            } catch (NumberFormatException ex) {
            }
            Integer isSearchAdvanced = null;
            try {
                isSearchAdvanced = Integer.parseInt(listValue.get(26));
            } catch (NumberFormatException ex) {
            }
            Long documentId = null;
            try {
                documentId = Long.parseLong(listValue.get(27));
            } catch (NumberFormatException ex) {
            }
            Integer isExport = 0;
            try {
                isExport = Integer.parseInt(listValue.get(28));
            } catch (NumberFormatException ex) {
            }
            Long objectType = null;
            try {
                objectType = Long.parseLong(listValue.get(30));
            } catch (NumberFormatException ex) {
            }
            //danh sách trạng thai công việc
            List<Integer> listStatus = new ArrayList<>();
            if (!CommonUtils.isEmpty(listValue.get(29))) {
                JSONArray jsonArray = new JSONArray(listValue.get(29));
                if (jsonArray != null && jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            listStatus.add(Integer.parseInt(jsonArray.get(i).toString()));
                        } catch (NumberFormatException e) {
                        }
                    }
                }
            }
            TaskDAO taskDAO = new TaskDAO();
            Object result = taskDAO.getListTask(userGroup.getUserId2(), isCount, keyword, type, status,
                    startDateFrom, startDateTo, startRecord, pageSize, documentId, enforcementId,
                    isWeb, period, stateApproval, orgCommanderId, taskName, content, fieldId,
                    taskType, taskType2, commanderId, startTimeFrom, startTimeTo, endTimeFrom,
                    endTimeTo,objectType, frequenceUpdate, statusClose, sourceType, listStatus, isSearchAdvanced, listManagementOrgId, isExport);
            if (result == null) {
                LOGGER.error("getListTask (Lay danh sach cong viec) - result = null"
                        + " - username: " + userGroup.getCardId() + "\ndata: " + data);
                return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            LOGGER.error("getListTask (Lay danh sach cong viec) - Exception - username: "
                    + userGroup.getCardId() + "\ndata: " + data, ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    /**
     * <b>Lay danh sach cong viec ca nhan duoc tao tu nhiem vu</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListTaskFromMission(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getListTaskFromMission (Lay danh sach cong viec ca nhan"
                    + " tao tu nhiem vu) - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("getListTaskFromMission (Lay danh sach cong viec ca nhan"
                    + " tao tu nhiem vu) - Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        String cardId = user.getStrCardNumber();
        log.setUserName(cardId);
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.MISSION_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long missionId = Long.parseLong(listValue.get(0));
            TaskDAO taskDAO = new TaskDAO();
            List<EntityTask> result = taskDAO.getListTaskFromMission(missionId);
            LogUtils.logFunctionalEnd(log);
            // Ket qua tra ve null -> Co loi trong qua trinh xu ly
            if (result == null) {
                LOGGER.error("getListTaskFromMission (Lay danh sach cong viec ca"
                        + " nhan tao tu nhiem vu) - result = null - username: "
                        + cardId + "\ndata: " + data);
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } // Xay ra ngoai le trong qua trinh xu ly
        catch (Exception ex) {
            LOGGER.error("getListTaskFromMission (Lay danh sach cong viec ca nhan"
                    + " tao tu nhiem vu) - Exception - username: " + cardId
                    + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Lay chi tiet cong viec</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getTaskDetail(HttpServletRequest request, String data,
            String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getTaskDetail (Lay chi tiet cong viec) - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("getTaskDetail (Lay chi tiet cong viec) - Khong co thong"
                    + " tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        Long userId = user.getUserId();
        String cardId = user.getStrCardNumber();
        log.setUserName(cardId);
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.TASK_ID,
                ConstantsFieldParams.LANGUAGE
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long taskId = Long.parseLong(listValue.get(0));
            // Lay ma ngon ngu client can dung
            String langCode = user.getLanguageCode();
            if (CommonUtils.isEmpty(langCode)) {
                langCode = "vi";
            }
            // Neu client co gui len ma ngon ngu thi tra theo ma ngon ngu cua client
            if (!CommonUtils.isEmpty(listValue.get(1))) {
                langCode = listValue.get(1);
            }
            TaskDAO taskDAO = new TaskDAO();
            EntityTask result = taskDAO.getTaskDetail(taskId, userId, langCode);
            LogUtils.logFunctionalEnd(log);
            if (result == null) {
                LOGGER.error("getTaskDetail (Lay chi tiet cong viec) - result = null"
                        + " - username: " + cardId + "\ndata: " + data);
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error("getTaskDetail (Lay chi tiet cong viec) - Exception - "
                    + "username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Lay lich su cap nhat cong viec</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getUpdateTaskHistory(HttpServletRequest request, String data,
            String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getUpdateTaskHistory (Lay lich su cap nhat cong viec) "
                    + "- Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("getUpdateTaskHistory (Lay lich su cap nhat cong viec) "
                    + "- Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Lay ra ma nhan vien
        String cardId = user.getStrCardNumber();
        log.setUserName(cardId);
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.TASK_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long taskId = Long.parseLong(listValue.get(0));
            TaskDAO taskDAO = new TaskDAO();
            List<EntityTaskProcess> result = taskDAO.getTaskProcessUpdateHistory(taskId);
            LogUtils.logFunctionalEnd(log);
            // Neu ket qua tra ve null -> Co loi xay ra trong qua trinh xu ly
            if (result == null) {
                LOGGER.error("getUpdateTaskHistory (Lay lich su cap nhat cong viec)"
                        + " - result = null - username: " + cardId + "\ndata: " + data);
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error("getUpdateTaskHistory (Lay lich su cap nhat cong viec) "
                    + "- Exception - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Lay file dinh kem chi tiet cong viec</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getFileAttachmentTask(HttpServletRequest request, String data,
            String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        if (userGroup.getCheckSessionOk()) {
            String cardId = null;
            try {
                // Lay thong tin nguoi dung
                Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
                if (user == null || user.getUserId() == null) {
                    LOGGER.error("getFileAttachmentTask (Lay danh sach file kem "
                            + "chi tiet cong viec) - Khong co quyen!");
                    return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW,
                            null, null);
                }
                // Lay ra ma nhan vien
                cardId = user.getStrCardNumber();
                log.setUserName(cardId);
                // Kiem tra xem co ma hoa du lieu ko
                String aesKey = null;
                if (isSecurity != null && "1".equals(isSecurity)) {
                    // Lay AES Key
                    aesKey = userGroup.getStrAesKey();
                    // Giai ma data client gui len
                    data = SecurityControler.decodeDataByAes(aesKey, data);
                }
                log.setParamList(data);
                LogUtils.logFunctionalStart(log);
                // Parse du lieu gui len theo key
                JSONObject json = new JSONObject(data);
                String[] keys = new String[]{
                    ConstantsFieldParams.TASK_ID
                };
                List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                // Id cong viec
                Long taskId = Long.parseLong(listValue.get(0));

                TaskDAO taskDAO = new TaskDAO();
                List<EntityFileAttachment> result = taskDAO.getListTaskFile(taskId);
                LogUtils.logFunctionalEnd(log);
                // Loi server
                if (result == null) {
                    LOGGER.error("getFileAttachmentTask (Lay danh sach file kem "
                            + "chi tiet cong viec) - result = null - username: "
                            + cardId + "\ndata: " + data);
                    return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                            null, null);
                }
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                        result, aesKey);
            } catch (Exception ex) {
                LOGGER.error("getFileAttachmentTask (Lay danh sach file kem chi "
                        + "tiet cong viec) - Exception - username: " + cardId
                        + "\ndata: " + data, ex);
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
        } else {
            LOGGER.error("getFileAttachmentTask (Lay danh sach file kem chi tiet"
                    + " cong viec) - Session timeout!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }
    }

    /**
     * <b>Lay danh sach yeu cau tu cong viec</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getRequestList(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getRequestList (Lay danh sach yeu cau tu cong viec) -"
                    + " Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        // Kiem tra user id tren he thong 2
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("getRequestList (Lay danh sach yeu cau tu cong viec) -"
                    + " Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Lay ra ma nhan vien
        String cardId = user.getStrCardNumber();
        log.setUserName(cardId);
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.TASK_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long taskId = Long.parseLong(listValue.get(0));

            TaskDAO taskDAO = new TaskDAO();
            List<EntityRequest> result = taskDAO.getListRequestFromTask(taskId);
            LogUtils.logFunctionalEnd(log);
            if (result == null) {
                LOGGER.error("getRequestList (Lay danh sach yeu cau tu cong viec)"
                        + " - result = null - username: " + cardId + "\ndata: " + data);
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error("getRequestList (Lay danh sach yeu cau tu cong viec) - "
                    + "Exception - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Lay nguon goc chi tiet cong viec</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getSourceTask(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getSourceTask - (Lay nguon goc chi tiet cong viec) - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("getSourceTask - (Lay nguon goc chi tiet cong viec) - "
                    + "Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Lay ra ma nhan vien
        String cardId = user.getStrCardNumber();
        log.setUserName(cardId);
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        // Thiet lap danh sach tham so va ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.TASK_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long taskId = Long.parseLong(listValue.get(0));
            TaskDAO taskDAO = new TaskDAO();
            List<EntitySourceMap> result = (List<EntitySourceMap>) taskDAO
                    .getListSourceOfTask(taskId);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            if (result == null) {
                LOGGER.error("getSourceTask - (Lay nguon goc chi tiet cong viec)"
                        + " - result = null - username: " + cardId + "\ndata: " + data);
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error("getSourceTask - (Lay nguon goc chi tiet cong viec) - "
                    + "Exception - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Lay lich su tiep nhan cong viec</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getTaskReceiverHistory(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        if (userGroup.getCheckSessionOk()) {
            String cardId = null;
            try {
                // Lay thong tin user
                Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
                if (user == null || user.getUserId() == null) {
                    LOGGER.error("getTaskReceiverHistory (Lay lich su tiep nhan "
                            + "cong viec) - Khong co thong tin user tren he thong 2!");
                    return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW,
                            null, null);
                }
                // Lay ra userId tren he thong 2
                Long userId = user.getUserId();
                // Lay ra ma nhan vien
                cardId = user.getStrCardNumber();
                log.setUserName(cardId);
                // Kiem tra xem co ma hoa du lieu ko
                String strAesKeyDecode = null;
                if (isSecurity != null && "1".equals(isSecurity)) {
                    // Lay AES Key
                    strAesKeyDecode = userGroup.getStrAesKey();
                    // Giai ma data client gui len
                    data = SecurityControler.decodeDataByAes(strAesKeyDecode, data);
                }
                // Thiet lap danh sach tham so va ghi log bat dau chuc nang
                log.setParamList(data);
                LogUtils.logFunctionalStart(log);
                // Parse du lieu gui len theo key
                JSONObject json = new JSONObject(data);
                String[] keys = new String[]{
                    ConstantsFieldParams.TASK_ID
                };
                List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                // Id cong viec
                Long taskId = Long.parseLong(listValue.get(0));
                TaskDataBaseDao taskDBDao = new TaskDataBaseDao();
                List<EntityTaskReceiver> result = taskDBDao.getTaskReceiverHistory(request, taskId, userId);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                // Loi server
                if (result == null) {
                    LOGGER.error("getTaskReceiverHistory (Lay lich su tiep nhan "
                            + "cong viec) - result = null - username: " + cardId + "\ndata: " + data);
                    return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                            null, null);
                }
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
            } catch (Exception ex) {
                LOGGER.error("getTaskReceiverHistory (Lay lich su tiep nhan cong"
                        + " viec) - Exception - username: " + cardId + "\ndata: " + data, ex);
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
        } else {
            LOGGER.error("getTaskReceiverHistory (Lay lich su tiep nhan cong viec)"
                    + " - Session timeout!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }
    }

    /**
     * <b>Xoa cong viec</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String deleteTask(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        if (userGroup.getCheckSessionOk()) {
            String cardId = "";
            try {
                // Lay thong tin user
                Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
                if (user == null || user.getUserId() == null) {
                    LOGGER.error("deleteTask (Xoa cong viec) - Khong co thong tin"
                            + " user tren he thong 2!");
                    return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW,
                            null, null);
                }
                // Lay userId
                Long userId = user.getUserId();
                // Lay ma nhan vien
                cardId = user.getStrCardNumber();
                log.setUserName(cardId);
                // Kiem tra xem co ma hoa du lieu ko
                String aesKey = null;
                if (isSecurity != null && "1".equals(isSecurity)) {
                    // Lay AES Key
                    aesKey = userGroup.getStrAesKey();
                    // Giai ma data client gui len
                    data = SecurityControler.decodeDataByAes(aesKey, data);
                }
                // Thiet lap danh sach tham so vao log va ghi log bat dau chuc nang
                log.setParamList(data);
                LogUtils.logFunctionalStart(log);
                JSONObject json = new JSONObject(data);
                String[] keys = new String[]{
                    ConstantsFieldParams.TASK_ID
                };
                List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                Long taskId = Long.parseLong(listValue.get(0));
                TaskDataBaseDao taskDBDAO = new TaskDataBaseDao();
                Boolean result = taskDBDAO.deleteTask(taskId, userId);
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
            } catch (Exception ex) {
                LOGGER.error("deleteTask (Xoa cong viec) - Exception - username: "
                        + cardId + "\ndata: " + data, ex);
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
        } else {
            LOGGER.error("deleteTask (Xoa cong viec) - Session timeout!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }
    }

    /**
     * <b>Tiep nhan cong viec</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String receiveTaskStatus(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        if (userGroup.getCheckSessionOk()) {
            String cardId = "";
            try {
                // Lay thong tin user
                Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
                if (user == null || user.getUserId() == null) {
                    LOGGER.error("receiveTaskStatus (Tiep nhan cong viec) - "
                            + "Khong co thong tin user tren he thong 2!");
                    return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW,
                            null, null);
                }
                // Lay userId
                Long userId = user.getUserId();
                // Lay ma nhan vien
                cardId = user.getStrCardNumber();
                log.setUserName(cardId);
                // Kiem tra xem co ma hoa du lieu ko
                String strAesKeyDecode = null;
                if (isSecurity != null && "1".equals(isSecurity)) {
                    // Lay AES Key
                    strAesKeyDecode = userGroup.getStrAesKey();
                    // Giai ma data client gui len
                    data = SecurityControler.decodeDataByAes(strAesKeyDecode, data);
                }
                // Thie lap danh sach tham so va ghi log bat dau chuc nang
                log.setParamList(data);
                LogUtils.logFunctionalStart(log);
                JSONObject json = new JSONObject(data);
                String[] keys = new String[]{
                    ConstantsFieldParams.TASK_ID,
                    ConstantsFieldParams.TASK_RECEIVER_COMMENT,
                    ConstantsFieldParams.TASK_COMMANDER_ID,
                    ConstantsFieldParams.TASK_ENFORCEMENT_ID,
                    ConstantsFieldParams.TASK_RECEIVER_TYPE
                };
                List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                Long taskId = Long.parseLong(listValue.get(0));
                String comment = listValue.get(1);
                Long commanderId = Long.parseLong(listValue.get(2));
                Long enforcementId = Long.parseLong(listValue.get(3));
                String receiverType = listValue.get(4);

                TaskDataBaseDao taskDBDAO = new TaskDataBaseDao();
                Boolean result = taskDBDAO.receiveTaskStatus(taskId, receiverType, comment, commanderId, enforcementId, userId);
                LogUtils.logFunctionalEnd(log);
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
            } catch (Exception ex) {
                LOGGER.error("receiveTaskStatus (Tiep nhan cong viec) - Exception"
                        + " - username: " + cardId + "\ndata: " + data, ex);
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
        } else {
            LOGGER.error("receiveTaskStatus (Tiep nhan cong viec) - Session timeout!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }
    }

    /**
     * <b>Dong cong viec</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String closeTask(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("closeTask (Dong cong viec) - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        // Kiem tra user id tren he thong 2
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("closeTask (Dong cong viec) - Khong co thong tin user "
                    + "tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        Long userId = user.getUserId();
        // Lay ma nhan vien
        String cardId = user.getStrCardNumber();
        log.setUserName(cardId);
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        // Thiet lap danh sach tham so va ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.TASK_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long taskId = Long.parseLong(listValue.get(0));

            TaskDAO taskDAO = new TaskDAO();
            EntityTask task = taskDAO.checkUserPermissionForTask(userId, taskId);
            if (task == null
                    || task.getPermissionsForTask() == null
                    || task.getPermissionsForTask().getClose() == 0) {
                LOGGER.error("closeTask (Dong cong viec) - User khong co quyen "
                        + "dong cong viec - username: " + cardId + "\ndata: " + data);
                return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
            }
            int result = taskDAO.closeTask(userId, taskId);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error("closeTask (Dong cong viec) - Exception - username: "
                    + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Lay chi tiet yeu cau</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getDetailRequest(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        if (userGroup.getCheckSessionOk()) {
            String cardId = "";
            try {
                // Lay thong tin user
                Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
                if (user == null || user.getUserId() == null) {
                    LOGGER.error("getDetailRequest (Lay chi tiet yeu cau) - "
                            + "Khong co thong tin user tren he thong 2!");
                    return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW,
                            null, null);
                }
                // Lay userId
                Long userId = user.getUserId();
                // Lay ma nhan vien
                cardId = user.getStrCardNumber();
                log.setUserName(cardId);
                // Kiem tra xem co ma hoa du lieu ko
                String aesKey = null;
                if (isSecurity != null && "1".equals(isSecurity)) {
                    // Lay AES Key
                    aesKey = userGroup.getStrAesKey();
                    // Giai ma data client gui len
                    data = SecurityControler.decodeDataByAes(aesKey, data);
                }
                // Thiet lap danh sach tham vao ghi log bat dau chuc nang
                log.setParamList(data);
                LogUtils.logFunctionalStart(log);
                // Parse du lieu gui len theo key
                JSONObject json = new JSONObject(data);
                String[] keys = new String[]{
                    ConstantsFieldParams.REQUEST_ID
                };
                List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                // Id yeu cau
                Long requestId = Long.parseLong(listValue.get(0));
                TaskDataBaseDao taskDBDao = new TaskDataBaseDao();
                List<EntityRequest> result = taskDBDao.getDetailRequest(requestId, userId);
                // Log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                // Loi server
                if (result == null) {
                    LOGGER.error("getDetailRequest (Lay chi tiet yeu cau) - "
                            + "result = null - username: " + cardId + "\ndata: " + data);
                    return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                            null, null);
                }
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
            } catch (Exception ex) {
                LOGGER.error("getDetailRequest (Lay chi tiet yeu cau) - Exception"
                        + " - username: " + cardId + "\ndata: " + data, ex);
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
        } else {
            return FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }
    }

    /**
     * <b>Cap nhat tien do cong viec ca nhan</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String updateTaskProcess(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        Gson gson = new Gson();
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("updateTaskProcess (Cap nhat tien do cong viec) - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("updateTaskProcess (Cap nhat tien do cong viec) - "
                    + "Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Lay userId
        Long userId = user.getUserId();
        // Lay ma nhan vien
        String cardId = user.getStrCardNumber();
        log.setUserName(cardId);
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        // Thiet lap danh sach tham so va ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.TASK_ID,
                ConstantsFieldParams.STATUS,
                ConstantsFieldParams.TASK_RESULT,
                ConstantsFieldParams.COMPLETED_PERCENT,
                ConstantsFieldParams.RATING_POINT,
                ConstantsFieldParams.PERIOD,
                "entityPointEvaluation",
                ConstantsFieldParams.LIST_SOURCE_MAP
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long taskId = Long.parseLong(listValue.get(0));
            int status = Integer.parseInt(listValue.get(1));
            String taskResult = listValue.get(2);
            int completedPercent = Integer.parseInt(listValue.get(3));
            int ratingPoint = Integer.parseInt(listValue.get(4));
            // Ky danh gia
            String period = listValue.get(5);
            // Danh sach tieu chi danh gia
            String strEntityPointEvaluation = listValue.get(6);
            List<EntityTask> listEvaluation = null;
            if (!CommonUtils.isEmpty(strEntityPointEvaluation)) {
                Type tokenType = new TypeToken<ArrayList<EntityTask>>() {
                }.getType();
                listEvaluation = gson.fromJson(strEntityPointEvaluation,
                        tokenType);
            }
            // Danh sach nguon goc cua tien do cong viec
            String strListSourceMap = listValue.get(7);
            List<EntitySourceMap> listSourceMap = null;
            if (!CommonUtils.isEmpty(strListSourceMap)) {
                Type tokenType = new TypeToken<ArrayList<EntitySourceMap>>() {
                }.getType();
                listSourceMap = gson.fromJson(strListSourceMap, tokenType);
            }
            // Lay danh sach file dinh kem
            List<EntityFileAttachment> lstFileAttachment = new ArrayList<>();
            if (json.has(ConstantsFieldParams.LIST_FILE_ATTACH)) {
                JSONArray arrFiles = json.getJSONArray(ConstantsFieldParams.LIST_FILE_ATTACH);
                if (arrFiles.length() > 0) {
                    EntityFileAttachment tmp;
                    for (int i = 0; i < arrFiles.length(); i++) {
                        try {
                            JSONObject innerObj = arrFiles.getJSONObject(i);
                            tmp = gson.fromJson(innerObj.toString(), EntityFileAttachment.class);
                            lstFileAttachment.add(tmp);
                        } catch (JSONException ex) {
                            LOGGER.error("updateTaskProcess:lstFileAttachment - Exception:", ex);
                        }
                    }
                }
            }
            
            TaskDAO taskDAO = new TaskDAO();
            EntityTask task = taskDAO.checkUserPermissionForTask(userId, taskId);
            if (task == null) {
                // 16:30 03/11/2016 - Thanght6
                // Fix loi logic do Sonar quet duoc
                LOGGER.error("updateTaskProcess (Cap nhat tien do cong viec) - "
                        + "User khong co quyen cap nhat tien do cong viec - username: "
                        + cardId + "\ndata: " + data);
                return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
            }
            Long isMajor = task.getIsMajor();
            Long commanderId = task.getCommanderId();
            Long enforcementId = task.getEnforcementId();
            String content = task.getContent();
            String startTime = task.getStartTime();
            String endTime = task.getEndTime();
            String partitionBy = task.getPartitionBy();
            Long orgId = task.getOrgId();
            int result = taskDAO.updateTaskProcess(userId, taskId, status,
                    taskResult, completedPercent, ratingPoint, isMajor,
                    lstFileAttachment, cardId, commanderId, enforcementId,
                    content, startTime, endTime, partitionBy, orgId, period,
                    listEvaluation, listSourceMap);
            // Log ket thu chuc nang
            // Datdc add them tham so end
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);

        } // Loi server
        catch (JSONException | NumberFormatException | JsonSyntaxException ex) {
            LOGGER.error("updateTaskProcess (Cap nhat tien do cong viec) - "
                    + "Exception - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    /**
     * <b>Lay cau hinh ty le diem danh gia</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListRatioConfig(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getListRatioConfig (Lay cau hinh ty le diem danh gia) - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("getListRatioConfig (Lay cau hinh ty le diem danh gia) "
                    + "- Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Lay userId
        Long userId = user.getUserId();
        // Lay ma nhan vien
        String cardId = user.getStrCardNumber();
        log.setUserName(cardId);
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
//        System.out.println("data=========:"+data);
        // Thiet lap danh sach tham so va ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.TASK_ID,
                ConstantsFieldParams.TYPE,
                ConstantsFieldParams.ORG_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long taskId = 0L;
            try {
                taskId = Long.parseLong(listValue.get(0));
            } catch (NumberFormatException ex) {
            }
            //Begin::cuongnv
            Integer type = 2;//Mac dinh
            try {
                //phần lấy tỷ lệ điểm nề nếp neu client gửi lên type = 5
                type = Integer.parseInt(listValue.get(1));
            } catch (NumberFormatException e) {
            }
            Long orgId = null;
            try {
                orgId = Long.parseLong(listValue.get(2));
            } catch (NumberFormatException e) {
            }
            //End
            TaskDAO taskDAO = new TaskDAO();
            if (orgId != null) {
                Integer result = taskDAO.getListRatioConfig(orgId, type);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);

            } else {
                // Lay ra duong dan don vi cua nguoi thuc hien cong viec ca nhan
                EntityTask task = taskDAO.checkUserPermissionForTask(userId, taskId);
                String orgPath = task.getOrgPath();
                List<EntityRatioConfigDetail> result = taskDAO.getListRatioConfig(orgPath, type);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                if (result == null) {
                    LOGGER.error("getListRatioConfig (Lay cau hinh ty le diem danh gia)"
                            + " - result = null - username: " + cardId + "\ndata: " + data);
                    return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
            }

        } catch (JSONException ex) {
            LOGGER.error("getListRatioConfig (Lay cau hinh ty le diem danh gia) "
                    + "- Exception - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }
   
    /**
     * <b>Phe duyet/Tu choi phe duyet tien do cong viec ca nhan</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String approveOrRejectTaskProcess(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("approveOrRejectTaskProcess (Phe duyet/Tu choi phe duyet"
                    + " tien do cong viec ca nhan) - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra thong tin user tren he thong 2
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("approveOrRejectTaskProcess (Phe duyet/Tu choi phe duyet"
                    + " tien do cong viec ca nhan) - Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Lay id user
        Long userId = user.getUserId();
        // Lay ma nhan vien
        String cardId = user.getStrCardNumber();
        log.setUserName(cardId);
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        // Thiet lap danh sach tham so va ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.IS_APPROVE,
                ConstantsFieldParams.TASK_ID,
                ConstantsFieldParams.COMPLETED_STATUS,
                ConstantsFieldParams.RATING_QUALITY,
                ConstantsFieldParams.RATING_POINT,
                ConstantsFieldParams.TASK_RESULT,
                ConstantsFieldParams.PERIOD,
                "entityPointEvaluation",
                "assignComment"
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            int isApprove = Integer.parseInt(listValue.get(0));
            Long taskId = Long.parseLong(listValue.get(1));
            // Muc do hoan thanh
            int completedStatus = 0;
            String strCompletedStatus = listValue.get(2);
            if (!CommonUtils.isEmpty(strCompletedStatus)) {
                completedStatus = Integer.parseInt(strCompletedStatus);
            }
            // Danh gia chat luong
            int ratingQuality = 0;
            String strRatingQuality = listValue.get(3);
            if (!CommonUtils.isEmpty(strRatingQuality)) {
                ratingQuality = Integer.parseInt(strRatingQuality);
            }
            // Diem danh gia
            int trRatingPoint = 0;
            String strTrRatingPoint = listValue.get(4);
            if (!CommonUtils.isEmpty(strTrRatingPoint)) {
                trRatingPoint = Integer.parseInt(strTrRatingPoint);
            }
            String taskResult = listValue.get(5);
            String period = listValue.get(6);
            // Danh sach tieu chi danh gia
            String strEntityPointEvaluation = listValue.get(7);
            List<EntityTask> listEvaluation = null;
            Gson gson = new Gson();
            if (!CommonUtils.isEmpty(strEntityPointEvaluation)) {
                Type tokenType = new TypeToken<ArrayList<EntityTask>>() {
                }.getType();
                listEvaluation = gson.fromJson(strEntityPointEvaluation,
                        tokenType);
            }
            // Y kien danh gia
            String assignComment = listValue.get(8);
            TaskDAO taskDAO = new TaskDAO();
            EntityTask task = taskDAO.checkUserPermissionForTask(userId, taskId);
            // +: AND
            // -: OR
            // - (1) Khong lay duoc cong viec tuong ung: task = null
            // - (2) + (21) Thuc hien phe duyet: isApprove = 1
            //       + (22) Khong co quyen phe duyet: task.permissionForTask.approve = 0
            // - (3) + (31) Thuc hien tu choi phe duyet: isApprove = 0
            //       + (32) Khong co quyent tu choi phe duyet: task.permissionForTask.rejectApprove = 0
            if (task == null
                    || (isApprove == 1 && task.getPermissionsForTask().getApprove() == 0)
                    || (isApprove == 0 && task.getPermissionsForTask().getRejectApprove() == 0)) {
                LOGGER.error("approveOrRejectTaskProcess (Phe duyet/Tu choi phe duyet"
                        + " tien do cong viec ca nhan) - Khong co quyen phe duyet/tu choi"
                        + " - username: " + cardId + "\ndata: " + data);
                return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
            }
            Long commanderId = task.getCommanderId();
            Long enforcementId = task.getEnforcementId();
            String content = task.getContent();
            String startTime = task.getStartTime();
            String endTime = task.getEndTime();
            String partitionBy = task.getPartitionBy();
            Long status = task.getStatus();
            Long orgId = task.getOrgId();
            Double ratingPoint = task.getRatingPoint();
            int result = taskDAO.approveOrRejectTaskProcess(userId, isApprove,
                    taskId, commanderId, enforcementId, content, startTime, endTime,
                    taskResult, partitionBy, status, ratingPoint, orgId, completedStatus,
                    ratingQuality, trRatingPoint, period, listEvaluation, assignComment);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);

        } catch (Exception ex) {
//            ex.printStackTrace();
            LOGGER.error("approveOrRejectTaskProcess (Phe duyet/Tu choi phe duyet"
                    + " tien do cong viec ca nhan) - Exception - username: "
                    + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * <b>Cap nhat y kien chi dao kien nghi de xuat</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String updateCommentRequest(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String strResult;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        if (userGroup.getCheckSessionOk()) {
            String cardId = "";
            try {
                // Lay thong tin user
                Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
                if (user == null || user.getUserId() == null) {
                    LOGGER.error("updateCommentRequest (Cap nhat y kien chi dao "
                            + "kien nghi de xuat) - Khong co thong tin user tren he thong 2!");
                    return FunctionCommon.generateResponseJSON(ErrorCode.SESSION_TIME_OUT, null, null);
                }
                // Lay userId
                Long userId = user.getUserId();
                // Lay ma nhan vien
                cardId = user.getStrCardNumber();
                log.setUserName(cardId);
                // Kiem tra xem co ma hoa du lieu ko
                String strAesKeyDecode = null;
                if (isSecurity != null && "1".equals(isSecurity)) {
                    // Lay AES Key
                    strAesKeyDecode = userGroup.getStrAesKey();
                    // Giai ma data client gui len
                    data = SecurityControler.decodeDataByAes(strAesKeyDecode, data);
                }
                // Thiet lap danh sach tham so va ghi log bat dau chuc nang
                log.setParamList(data);
                LogUtils.logFunctionalStart(log);
                JSONObject json = new JSONObject(data);
                String[] keys = new String[]{
                    ConstantsFieldParams.REQUEST_ID,
                    ConstantsFieldParams.SOLUTION
                };
                List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                Long requestId = Long.parseLong(listValue.get(0));
                String solution = listValue.get(1);

                TaskDataBaseDao taskDBDAO = new TaskDataBaseDao();
                Boolean result = taskDBDAO.updateCommentRequest(solution, requestId, userId);
                // Ghi log ket thuc chuc nang
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
            } catch (Exception ex) {
                LOGGER.error("updateCommentRequest (Cap nhat y kien chi dao kien"
                        + " nghi de xuat) - Exception - username: " + cardId + "\ndata: " + data, ex);
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
        } else {
            LOGGER.error("updateCommentRequest (Cap nhat y kien chi dao kien nghi"
                    + " de xuat) - Session timeout!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }
    }

    /**
     * <b>Lay danh sach cong viec ca nhan de ky duyet phieu giao viec</b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListTaskToAssign(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getListTaskToAssign (Lay danh sach cong viec ca nhan "
                    + "de ky duyet phieu giao viec) - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("getListTaskToAssign (Lay danh sach cong viec ca nhan "
                    + "de ky duyet phieu giao viec) - Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Lay userId
        Long userId = user.getUserId();
        // Lay ma nhan vien
        String cardId = user.getStrCardNumber();
        log.setUserName(cardId);
        // Lay danh sach don vi co vai tro thu truong/lanh dao
        List<Long> listManagementOrgId = userGroup.getVof2_ItemEntityUser().getListManagementOrg();
        // Khong phai thu truong/lanh dao
        // --> Tra ve thong bao ko co quyen
        if (listManagementOrgId == null || listManagementOrgId.isEmpty()) {
            LOGGER.error("getListTaskToAssign - (Lay danh sach cong viec ca nhan "
                    + "de ky duyet phieu giao viec) - User khong co vai tro thu truong/lanh dao");
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
        // Thiet lap danh sach tham so va ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.ORG_ID,
                // Datdc them period
                ConstantsFieldParams.PERIOD
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long orgId = Long.parseLong(listValue.get(0));
            // Id don vi khong thuoc don vi user quan ly
            // --> Tra ve thong bao khong co quyen
            if (!listManagementOrgId.contains(orgId)) {
                LOGGER.error("getListTaskToAssign - (Lay danh sach cong viec ca "
                        + "nhan de ky duyet phieu giao viec) - User khong co vai"
                        + " tro thu truong/lanh dao doi voi don vi nay - username: "
                        + cardId + "\ndata: " + data);
                return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
            }
            // Datdc them period
            String period = "";
            if (listValue.size() >= 2) {
                period = listValue.get(1);
            }
            TaskDAO taskDAO = new TaskDAO();
            EntityTaskAssignment result = taskDAO.getListTaskToAssign(userId, orgId, period);
            LogUtils.logFunctionalEnd(log);
            if (result == null) {
                LOGGER.error("getListTaskToAssign - (Lay danh sach cong viec ca "
                        + "nhan de ky duyet phieu giao viec) - resul = null - "
                        + "username: " + cardId + "\ndata: " + data);
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error("getListTaskToAssign - (Lay danh sach cong viec ca nhan"
                    + " de ky duyet phieu giao viec) - Exception - username: "
                    + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    
    /**
     * <b>Xuat file cong viec</b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
     public String exportListTaskFile(HttpServletRequest request,
            String data, String isSecurity) {
        String[] keys = new String[]{
            ConstantsFieldParams.TYPE,
            ConstantsFieldParams.ORG_ID,
            ConstantsFieldParams.ORG_NAME,
            ConstantsFieldParams.LIST_EMPLOYEE,
            ConstantsFieldParams.PERIOD}; 
         
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request,
                data, keys);
        
        if (!userGroup.getCheckSessionOk()) {
            //truong hop khong co session
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(),
                    null, null);
        }
        //=========lay danh sach bien truyen tu client len======================
        //thuc hien trong truong hop co session
        List<String> listValue = userGroup.getListParamsFromClient();
        int type = -1;
        //type =1: cong viec ca nhan dau thang
        //type =2: cong viec ca nhan cuoi thang
        if(FunctionCommon.isNumeric(listValue.get(0))){
            type = Integer.parseInt(listValue.get(0));
        }
        //id don vi danh gia
        Long orgId = 0L;
        if(FunctionCommon.isNumeric(listValue.get(1))){
            orgId = Long.parseLong(listValue.get(1));
        }
        //ten don vi
        String orgName = listValue.get(2);
        
        //danh sach ca nhan can danh gia
        String strListEmployee = listValue.get(3);
        String period = listValue.get(4);
        //======================Xu ly nghiep vu======================
        //lay thong tin nguoi dung tren voffice 2
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        
        OrgDAO orgDAO = new OrgDAO();
        EntityVhrOrg parentOrg = orgDAO.getParentOrg(orgId);
        //don vi cha
        String orgParentName = "Tập đoàn Quân đội";
        if (parentOrg != null && !CommonUtils.isEmpty(parentOrg.getOrgName())) {
            orgParentName = parentOrg.getOrgName();
        }
        
        EntityVhrEmployee assigner = new EntityVhrEmployee();
        assigner.setEmployeeId(user2.getUserId());
        assigner.setFullName(user2.getFullName());
        assigner.setPosition(user2.getJobTile());
        
        EntityVhrOrg assignmentOrg = new EntityVhrOrg();
        assignmentOrg.setSysOrganizationId(orgId);
        assignmentOrg.setName(orgName);
        assignmentOrg.setOrgParentName(orgParentName);
        //danh sach ca nhan can danh gia
        List<EntityVhrEmployee> listEmployee = (List<EntityVhrEmployee>) 
                FunctionCommon.convertJsonToListObject(
                strListEmployee, EntityVhrEmployee.class);
        
        List<EntityVhrEmployee> result = FileUtils.exportListTaskFile(type,
                    assignmentOrg, assigner, listEmployee, period);
         
        return FunctionCommon.responseResult(ErrorCode.SUCCESS,
                    result, userGroup);
    }
    
    /**
     * <b>Hoan thanh giai quyet kien nghi de xuat</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String closeRequest(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        if (userGroup.getCheckSessionOk()) {
            String cardId = "";
            try {
                // Lay thong tin user
                Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
                if (user == null || user.getUserId() == null) {
                    LOGGER.error("closeRequest (Hoan thanh giai quyet kien nghi "
                            + "de xuat) - Khong co thong tin user tren he thong 2!");
                    return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW,
                            null, null);
                }
                // Lay userId
                Long userId = user.getUserId();
                // Lay ma nhan vien
                cardId = user.getStrCardNumber();
                log.setUserName(cardId);
                // Kiem tra xem co ma hoa du lieu ko
                String aesKey = null;
                if (isSecurity != null && "1".equals(isSecurity)) {
                    // Lay AES Key
                    aesKey = userGroup.getStrAesKey();
                    // Giai ma data client gui len
                    data = SecurityControler.decodeDataByAes(aesKey, data);
                }
                // Thiet lap danh sach tham so va ghi log bat dau chuc nang
                log.setParamList(data);
                LogUtils.logFunctionalStart(log);
                JSONObject json = new JSONObject(data);
                String[] keys = new String[]{
                    ConstantsFieldParams.REQUEST_ID,
                    ConstantsFieldParams.SOLUTION
                };
                List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                Long requestId = Long.parseLong(listValue.get(0));
                String solution = listValue.get(1);

                TaskDataBaseDao taskDBDAO = new TaskDataBaseDao();

                Boolean result = taskDBDAO.closeRequest(solution, requestId, userId);
                // Log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
            } catch (Exception ex) {
                LOGGER.error("closeRequest (Hoan thanh giai quyet kien nghi de xuat)"
                        + " - Exception - username: " + cardId + "\ndata: " + data, ex);
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
        } else {
            LOGGER.error("closeRequest (Hoan thanh giai quyet kien nghi de xuat)"
                    + " - Session timeout!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }
    }

    /**
     * <b>Lay danh sach cong viec de danh gia</b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListTaskToAssess(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getListTaskToAssess (Lay danh sach cong viec de danh gia)"
                    + " - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Lay thong tin user
        Vof2_EntityUser userV2 = userGroup.getVof2_ItemEntityUser();
        if (userV2 == null || userV2.getUserId() == null) {
            LOGGER.error("getListTaskToAssess - (Lay danh sach cong viec de danh gia)"
                    + " - Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Lay userId
        Long userId = userV2.getUserId();
        // Lay ma nhan vien
        String cardId = userV2.getStrCardNumber();
        log.setUserName(cardId);
        // Lay danh sach don vi co vai tro thu truong/lanh dao
        List<Long> listManagementOrgId = userV2.getListManagementOrg();
        // Khong phai thu truong/lanh dao
        // --> Tra ve thong bao ko co quyen
        if (CommonUtils.isEmpty(listManagementOrgId)) {
            LOGGER.error("getListTaskToAssess (Lay danh sach cong viec de danh gia)"
                    + " - username: " + cardId + " - User khong co vai tro thu truong/lanh dao");
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
        // Thiet lap danh sach tham so va ghi log bat dau chuc nang
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.TYPE,
                ConstantsFieldParams.IS_COUNT,
                ConstantsFieldParams.ORG_ID,
                ConstantsFieldParams.ENFORCEMENT_ID,
                // Datdc them period
                ConstantsFieldParams.PERIOD
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Loai
            // 0: Tat ca
            // 1: Cong viec dang ky trong thang
            // 2: Cong viec bo sung
            int type = 0;
            String strType = listValue.get(0);
            if (!CommonUtils.isEmpty(strType)) {
                type = Integer.parseInt(strType);
            }
            // Dem so luong
            String isCount = listValue.get(1);
            // Id don vi
            Long orgId = Long.parseLong(listValue.get(2));
            // Neu don vi gui len khong thuoc quyen quan ly cua user
            // --> Tra ve thong bao khong co quyen
            if (!listManagementOrgId.contains(orgId)) {
                LOGGER.error("getListTaskToAssess - (Lay danh sach cong viec de "
                        + "danh gia) - User khong co vai tro thu truong/lanh dao"
                        + " don vi nay - username: " + cardId + "\ndata: " + data);
                return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW,
                        null, null);
            }
            // Id nguoi thuc hien
            Long enforcementId = null;
            String strEnforcementId = listValue.get(3);
            if (!CommonUtils.isEmpty(strEnforcementId)) {
                enforcementId = Long.parseLong(strEnforcementId);
            }
            String period = "";
            // Datdc them period
            if (listValue.size() >= 5) {
                period = listValue.get(4);
            }
            TaskDAO taskDAO = new TaskDAO();
            // Datdc them tham so period
            EntityTaskAssessment result = taskDAO.getListTaskToAssess(userId, isCount,
                    type, orgId, enforcementId, period);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error("getListTaskToAssess - (Lay danh sach cong viec de danh"
                    + " gia) - Exception - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Lay danh sach cau hinh ty le theo don vi</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListRatioConfigByOrg(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getListRatioConfigByOrg (Lay danh sach cau hinh ty le"
                    + " theo don vi) - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        // Kiem tra user id tren he thong 2
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("getListRatioConfigByOrg (Lay danh sach cau hinh ty le"
                    + " theo don vi) - Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Lay ma nhan vien
        String cardId = user.getStrCardNumber();
        log.setUserName(cardId);
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        // Thiet lap danh sach tham so va ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.ORG_ID,
                ConstantsFieldParams.TYPE
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long orgId = Long.parseLong(listValue.get(0));
            int type = 2;
            if (!CommonUtils.isEmpty(listValue.get(1))) {
                type = Integer.parseInt(listValue.get(1));
            }
            TaskDAO taskDAO = new TaskDAO();
            // Lay ra don vi theo id
            // Neu don vi null -> Tra ve loi server
            // Neu don vi khac null -> Lay ra path cua don vi
            OrgDAO orgDAO = new OrgDAO();
            EntityVhrOrg org = orgDAO.getOrganizationById(orgId);
            if (org == null) {
                LOGGER.error("getListRatioConfigByOrg (Lay danh sach cau hinh ty"
                        + " le theo don vi) - org = null - username: " + cardId
                        + "\ndata: " + data);
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
            String orgPath = org.getPath();
            List<EntityRatioConfigDetail> result = taskDAO.getListRatioConfig(orgPath, type);
            if (result == null) {
                LOGGER.error("getListRatioConfigByOrg (Lay danh sach cau hinh ty"
                        + " le theo don vi) - resul = null - username: " + cardId + "\ndata: " + data);
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            }
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);

        } catch (Exception ex) {
            LOGGER.error("getListRatioConfigByOrg (Lay danh sach cau hinh ty le "
                    + "theo don vi) - Exception - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Lay danh sach cong viec duoc tao tu cong van</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListTaskFromDocument(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getListTaskFromDocument (Lay danh sach cong viec duoc "
                    + "tao tu cong van) - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }

        // Kiem tra user id tren he thong 2
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("getListTaskFromDocument (Lay danh sach cong viec duoc "
                    + "tao tu cong van) - Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        Long userId = user.getUserId();
        // Lay ma nhan vien
        String cardId = user.getStrCardNumber();
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
                ConstantsFieldParams.SENDER_ID_VOF2};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long docId = Long.parseLong(listValue.get(0));
            //Begin :: cuongnv
            Long sendId2 = null;
            try {
                sendId2 = Long.parseLong(listValue.get(1));
            } catch (NumberFormatException ex) {
            }
            if (sendId2 != null) {
                TaskDAO taskDAO = new TaskDAO();
                Object result = taskDAO.getListTaskFromDocument(sendId2, docId);
                // Log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                if (result == null) {
                    LOGGER.error("getListTaskFromDocument (Lay danh sach cong viec"
                            + " duoc tao tu cong van) - result = null - username: "
                            + cardId + "\ndata: " + data);
                    return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                            null, null);
                }
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
            }
            //End
            TaskDAO taskDAO = new TaskDAO();
            Object result = taskDAO.getListTaskFromDocument(userId, docId);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            if (result == null) {
                LOGGER.error("getListTaskFromDocument (Lay danh sach cong viec duoc"
                        + " tao tu cong van) - result = null - username: " + cardId
                        + "\ndata: " + data);
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error("getListTaskFromDocument (Lay danh sach cong viec duoc"
                    + " tao tu cong van) - Exception - username: " + cardId
                    + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Dem so luong cong viec cho man hinh home</b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getCountHomeTask(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getCountHomeTask - No session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Neu user null hoac user id null
        // -> Tra ve thong bao khong co quyen
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("getCountHomeTask - user null || user id null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        Long userId = user.getUserId();
        // Lay danh sach don vi ma user co vai tro thu truong/lanh dao/tro ly
        List<Long> listSysOrgId = new ArrayList<>();
        // Lay danh sach don vi user co vai tro lanh dao/thu truong
        List<Long> listManagementOrgId = user.getListManagementOrg();
        if (!CommonUtils.isEmpty(listManagementOrgId)) {
            listSysOrgId.addAll(listManagementOrgId);
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
                ConstantsFieldParams.TYPE};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            int type = -1;
            if (listValue.get(0) != null && listValue.get(0).trim().length() > 0) {
                type = Integer.parseInt(listValue.get(0));
            }
            TaskDAO taskDAO = new TaskDAO();
            Object result = taskDAO.getCountHomeTask(userId, type, listSysOrgId);

            if (result == null) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            }
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error("getCountHomeTask - userId2: " + userId + " - data: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        } catch (Exception ex) {
            LOGGER.error("getCountHomeTask - userId2: " + userId + " - data: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    /**
     * <b>Xoa ban ghi cu khi giao viec lai</b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @authot cuongnv
     *
     */
    public String convertTaskToPDF(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("convertTaskToPDF (Xoa ban ghi cu khi giao viec lai) -"
                    + " Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }

        // Neu user null hoac user id null
        // -> Tra ve thong bao khong co quyen
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("getCountHomeTask (Xoa ban ghi cu khi giao viec lai) -"
                    + " Khong co thong tin user tren he thong 2");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Lay ma nhan vien
        String cardId = user.getStrCardNumber();
        log.setUserName(cardId);
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        // Thiet lap danh sach tham so va ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            List<String> listValue;
            List<EntityTaskApproval> lstTask = new ArrayList<>();
            EntityTaskApproval task;
            if (json.has(ConstantsFieldParams.LIST_TASK)) {
                JSONArray arrJson = FunctionCommon.jsonGetArray(
                        ConstantsFieldParams.LIST_TASK, data);
                if (arrJson != null && arrJson.length() > 0) {
                    for (int i = 0; i < arrJson.length(); i++) {
                        JSONObject object = arrJson.getJSONObject(i);
                        String[] values = new String[]{
                            ConstantsFieldParams.TASKSMS_APPROVAL_ENFORCEMENT_ID,
                            ConstantsFieldParams.TASK_APPROVER_ID,
                            ConstantsFieldParams.TASKSMS_APPROVAL_PREDIO
                        };
                        listValue = FunctionCommon.getValuesFromJSON(object, values);
                        task = new EntityTaskApproval();
                        task.setEnforcementId(Long.parseLong(listValue.get(0)));
                        task.setApproverId(Long.parseLong(listValue.get(1)));
                        task.setPeriod(listValue.get(2));
                        lstTask.add(task);
                    }
                    TaskDAO taskDAO = new TaskDAO();
                    Integer rs = taskDAO.deleteTaskToPDF(lstTask);
                    // Log ket thuc chuc nang
                    LogUtils.logFunctionalEnd(log);
                    return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                            rs, aesKey);

                } else {
                    LOGGER.error("getCountHomeTask (Xoa ban ghi cu khi giao viec"
                            + " lai) - Loi du lieu dau vao - username: " + cardId + "\ndata: " + data);
                    return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA,
                            null, null);
                }
            } else {
                LOGGER.error("getCountHomeTask (Xoa ban ghi cu khi giao viec lai)"
                        + " - Loi du lieu dau vao - username: " + cardId + "\ndata: " + data);
                return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA,
                        null, null);
            }
        } catch (Exception ex) {
            LOGGER.error("getCountHomeTask (Xoa ban ghi cu khi giao viec lai) - "
                    + "Exception - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Update cac ban ghi tao van ban giao viec</b>
     *
     * @author cuongnv
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String updateFileAttachmentFromTask(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("updateFileAttachmentFromTask (Update cac ban ghi tao van"
                    + " ban giao viec) - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Neu user null hoac user id null
        // -> Tra ve thong bao khong co quyen
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("updateFileAttachmentFromTask (Update cac ban ghi tao van"
                    + " ban giao viec) - Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Lay ma nhan vien
        String cardId = user.getStrCardNumber();
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
                ConstantsFieldParams.ORG_ID,
                ConstantsFieldParams.COMMENT,
                ConstantsFieldParams.TASK_USB
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long orgId = Long.parseLong(listValue.get(0));
            String comment = listValue.get(1) == null ? "" : listValue.get(1);
            Integer usb = 4;
            try {
                usb = Integer.parseInt(listValue.get(2));
            } catch (NumberFormatException e) {
            }
            List<EntityTaskApproval> lstTask = new ArrayList<>();
            EntityTaskApproval task;
            if (json.has(ConstantsFieldParams.LIST_TASK)) {
                JSONArray arrJson = FunctionCommon.jsonGetArray(ConstantsFieldParams.LIST_TASK, data);
                if (arrJson != null && arrJson.length() > 0) {
                    for (int i = 0; i < arrJson.length(); i++) {
                        JSONObject object = arrJson.getJSONObject(i);
                        String[] values = new String[]{
                            ConstantsFieldParams.TASKSMS_APPROVAL_ENFORCEMENT_ID,
                            ConstantsFieldParams.TASK_APPROVER_ID,
                            ConstantsFieldParams.TASKSMS_APPROVAL_PREDIO
                        };
                        listValue = FunctionCommon.getValuesFromJSON(object, values);
                        task = new EntityTaskApproval();
                        task.setEnforcementId(Long.parseLong(listValue.get(0)));
                        task.setApproverId(Long.parseLong(listValue.get(1)));
                        task.setPeriod(listValue.get(2));
                        lstTask.add(task);
                    }
                    TaskDAO taskDAO = new TaskDAO();
                    Integer rs = taskDAO.updateFileAttachmentFromTask(orgId, comment, lstTask, usb);
                    return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, rs, aesKey);

                } else {
                    LOGGER.error("updateFileAttachmentFromTask (Update cac ban ghi"
                            + " tao van ban giao viec) - Loi du lieu dau vao - username: "
                            + cardId + "\ndata: " + data);
                    return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, null, null);
                }
            } else {
                LOGGER.error("updateFileAttachmentFromTask (Update cac ban ghi"
                        + " tao van ban giao viec) - Loi du lieu dau vao - username: "
                        + cardId + "\ndata: " + data);
                return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA,
                        null, null);
            }
        } catch (Exception ex) {
            LOGGER.error("updateFileAttachmentFromTask (Update cac ban ghi tao van"
                    + " ban giao viec) - Exception - username: " + cardId + "\ndata: "
                    + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    /**
     * <b>Update cac ban ghi tao van ban danh gia</b>
     *
     * @author cuongnv
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String convertRatingTaskToPDF(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("convertRatingTaskToPDF (Update cac ban ghi tao van ban"
                    + " danh gia) - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }

        // Neu user null hoac user id null
        // -> Tra ve thong bao khong co quyen
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("convertRatingTaskToPDF (Update cac ban ghi tao van ban"
                    + " danh gia) - Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Lay ma nhan vien
        String cardId = user.getStrCardNumber();
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
                ConstantsFieldParams.ORG_ID,
                ConstantsFieldParams.COMMENT,
                ConstantsFieldParams.TASK_USB
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long orgId = Long.parseLong(listValue.get(0));
            String comment = listValue.get(1) == null ? "" : listValue.get(1);
            Integer usb = 4;
            try {
                usb = Integer.parseInt(listValue.get(2));
            } catch (NumberFormatException e) {
            }
            List<EntityTaskApproval> lstTask = new ArrayList<>();
            EntityTaskApproval task;
            if (json.has(ConstantsFieldParams.LIST_TASK)) {
                JSONArray arrJson = FunctionCommon.jsonGetArray(ConstantsFieldParams.LIST_TASK, data);
                if (arrJson != null && arrJson.length() > 0) {
                    for (int i = 0; i < arrJson.length(); i++) {
                        JSONObject object = arrJson.getJSONObject(i);
                        String[] values = new String[]{
                            ConstantsFieldParams.TASKSMS_APPROVAL_ENFORCEMENT_ID,
                            ConstantsFieldParams.TASK_APPROVER_ID,
                            ConstantsFieldParams.TASKSMS_APPROVAL_PREDIO
                        };
                        listValue = FunctionCommon.getValuesFromJSON(object, values);
                        task = new EntityTaskApproval();
                        task.setEnforcementId(Long.parseLong(listValue.get(0)));
                        task.setApproverId(Long.parseLong(listValue.get(1)));
                        task.setPeriod(listValue.get(2));
                        lstTask.add(task);
                    }
                    TaskDAO taskDAO = new TaskDAO();
                    Integer rs = taskDAO.convertRatingTaskToPDF(orgId, comment, lstTask, usb);
                    // Ghi log ket thuc chuc nang
                    LogUtils.logFunctionalStart(log);
                    return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, rs, aesKey);
                } else {
                    LOGGER.error("convertRatingTaskToPDF (Update cac ban ghi tao"
                            + " van ban danh gia) - Loi du lieu dau vao - username: "
                            + cardId + "\ndata: " + data);
                    return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA,
                            null, null);
                }
            } else {
                LOGGER.error("convertRatingTaskToPDF (Update cac ban ghi tao van"
                        + " ban danh gia) - Loi du lieu dau vao - username: "
                        + cardId + "\ndata: " + data);
                return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA,
                        null, null);
            }
        } catch (Exception ex) {
            LOGGER.error("convertRatingTaskToPDF (Update cac ban ghi tao van ban"
                    + " danh gia) - Exception - username: " + cardId + "\ndata: "
                    + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String deleteRatingTaskToPDF(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("deleteRatingTaskToPDF - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Neu user null hoac user id null
        // -> Tra ve thong bao khong co quyen
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("deleteRatingTaskToPDF - Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Lay ma nhan vien
        String cardId = user.getStrCardNumber();
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
        log.setParamList(cardId);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
//            String[] keys = new String[]{ConstantsFieldParams.ORG_ID,
//                ConstantsFieldParams.COMMENT
//            };
            List<String> listValue;
            List<EntityTaskApproval> lstTask = new ArrayList<>();
            EntityTaskApproval task;
            if (json.has(ConstantsFieldParams.LIST_TASK)) {
                JSONArray arrJson = FunctionCommon.jsonGetArray(ConstantsFieldParams.LIST_TASK, data);
                if (arrJson != null && arrJson.length() > 0) {
                    for (int i = 0; i < arrJson.length(); i++) {
                        JSONObject object = arrJson.getJSONObject(i);
                        String[] values = new String[]{
                            ConstantsFieldParams.TASKSMS_APPROVAL_ENFORCEMENT_ID,
                            ConstantsFieldParams.TASK_APPROVER_ID,
                            ConstantsFieldParams.TASKSMS_APPROVAL_PREDIO
                        };
                        listValue = FunctionCommon.getValuesFromJSON(object, values);
                        task = new EntityTaskApproval();
                        task.setEnforcementId(Long.parseLong(listValue.get(0)));
                        task.setApproverId(Long.parseLong(listValue.get(1)));
                        task.setPeriod(listValue.get(2));
                        lstTask.add(task);
                    }
                    TaskDAO taskDAO = new TaskDAO();
                    Integer rs = taskDAO.deleteRatingTaskToPDF(lstTask);
                    // Ghi log ket thuc chuc nang
                    LogUtils.logFunctionalEnd(log);
                    return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, rs, aesKey);
                } else {
                    LOGGER.error("deleteRatingTaskToPDF - Loi du lieu dau vao - "
                            + "username: " + cardId + "\ndata: " + data);
                    return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, null, null);
                }
            } else {
                LOGGER.error("deleteRatingTaskToPDF - Loi du lieu dau vao - username: "
                        + cardId + "\ndata: " + data);
                return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, null, null);
            }
        } catch (Exception ex) {
            LOGGER.error("deleteRatingTaskToPDF - Exception - username: " + cardId
                    + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Lay danh sach nhan vien trong don vi</b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getEmployeeListToAssign(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getEmployeeListToAssign (Lay danh sach nhan vien trong"
                    + " don vi de giao viec) - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        // Kiem tra user id tren he thong 2
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("getEmployeeListToAssign (Lay danh sach nhan vien trong"
                    + " don vi de giao viec) - Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        Long userId = user.getUserId();
        // Lay ma nhan vien
        String cardId = user.getStrCardNumber();
        log.setUserName(cardId);
        // Lay danh sach don vi co vai tro thu truong/lanh dao
        List<Long> listManagementOrgId = user.getListManagementOrg();
        // Khong phai thu truong/lanh dao
        // --> Tra ve thong bao ko co quyen
        if (CommonUtils.isEmpty(listManagementOrgId)) {
            LOGGER.error("getEmployeeListToAssign (Lay danh sach nhan vien trong"
                    + " don vi de giao viec) - User khong co vai tro thu truong/"
                    + "lanh dao - username: " + cardId);
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
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.ORG_ID,
                ConstantsFieldParams.PERIOD,"isYear"
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long orgId = Long.parseLong(listValue.get(0));
            String period = listValue.get(1);
            // Datdc la nam hay la quy
            String isYearString = listValue.get(2);
            Boolean isYear = false;
            if (!CommonUtils.isEmpty(isYearString)) {
                isYear = Boolean.valueOf(isYearString);
            }
            // Id don vi khong thuoc don vi user quan ly
            // --> Tra ve thong bao khong co quyen
            if (!listManagementOrgId.contains(orgId)) {
                LOGGER.error("getEmployeeListToAssign (Lay danh sach nhan vien trong"
                        + " don vi de giao viec) - User khong co vai tro thu truong/lanh"
                        + " dao doi voi don vi nay - username: " + cardId + "\ndata: " + data);
                return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
            }
            TaskDAO taskDAO = new TaskDAO();
            List<EntityVhrEmployee> result = taskDAO.getEmployeeListToAssign(userId, orgId, period,isYear);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error("getEmployeeListToAssign (Lay danh sach nhan vien trong"
                    + " don vi de giao viec) - Exception - username: " + cardId
                    + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Lay danh sach giao viec cho tung nhan vien</b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getTaskListToAssignByEmployee(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, this.getClass().getName());
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getTaskListToAssignByEmployee (Lay danh sach giao viec"
                    + " cho tung nhan vien) - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        // Kiem tra user id tren he thong 2
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("getTaskListToAssignByEmployee (Lay danh sach giao viec"
                    + "cho tung nhan vien) - Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Lay ma nhan vien
        String cardId = user.getStrCardNumber();
        log.setUserName(cardId);
        // Lay danh sach don vi co vai tro thu truong/lanh dao
        List<Long> listManagementOrgId = user.getListManagementOrg();
        // Khong phai thu truong/lanh dao
        // --> Tra ve thong bao ko co quyen
        if (CommonUtils.isEmpty(listManagementOrgId)) {
            LOGGER.error("getTaskListToAssignByEmployee (Lay danh sach giao viec"
                    + " cho tung nhan vien) - User khong co vai tro thu truong/lanh"
                    + " dao - username: " + cardId);
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
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.ENFORCEMENT_ID,
                ConstantsFieldParams.PERIOD,
                ConstantsFieldParams.IS_YEAR
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Id nguoi thuc hien
            Long enforcementId = null;
            try {
                enforcementId = Long.parseLong(listValue.get(0));
            } catch (NumberFormatException e) {
            }
            String period = listValue.get(1);
            // Datdc la nam hay la quy
            String isYearString = listValue.get(2);
            Boolean isYear = false;
            if (!CommonUtils.isEmpty(isYearString)) {
                isYear = Boolean.valueOf(isYearString);
            }

            //cuongnv::16/1/2017
            List<Long> listEnforcementId = new ArrayList<>();
            if (json.has(ConstantsFieldParams.TASK_LIST_ENFORCEMENT)) {
                try {
                    JSONArray nArray = json.getJSONArray(ConstantsFieldParams.TASK_LIST_ENFORCEMENT);
                    if (nArray != null && nArray.length() > 0) {
                        for (int i = 0; i < nArray.length(); i++) {
                            try {
                                listEnforcementId.add(Long.parseLong(nArray.get(i).toString()));
                            } catch (NumberFormatException e) {
                            }
                        }
                    }
                } catch (JSONException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            //end
            if (enforcementId != null) {
                //Dung cho mobile voi 1 nv
                TaskDAO taskDAO = new TaskDAO();
                // Datdc them,
                EntityTaskAssignment taskAssignment = taskDAO.getTaskListToAssignByEmployee(enforcementId, period, isYear);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, taskAssignment, aesKey);
            } else if (!CommonUtils.isEmpty(listEnforcementId)) {
                //Dung cho web voi 1 danh sach nv
                TaskDAO taskDAO = new TaskDAO();
                EntityTaskAssignment result = null;
                EntityTaskAssignment taskAssignment;
                List<EntityVhrEmployee> getListEmployee = new ArrayList<>();
                for (Long o : listEnforcementId) {
                    taskAssignment = taskDAO.getTaskListToAssignByEmployee(o, period, isYear);
                    if (result == null) {
                        result = taskAssignment;
                        getListEmployee.addAll(taskAssignment.getListEmployee());
                    } else {
                        getListEmployee.addAll(taskAssignment.getListEmployee());
                        result.setListEmployee(getListEmployee);
                    }
                }
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
            } else {
                LOGGER.error("getTaskListToAssignByEmployee (Lay danh sach giao viec"
                        + " cho tung nhan vien) - Exception - username: " + cardId
                        + "\ndata: " + data);
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
        } catch (Exception ex) {
            LOGGER.error("getTaskListToAssignByEmployee (Lay danh sach giao viec"
                    + " cho tung nhan vien) - Exception - username: " + cardId
                    + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Lay danh sach nhan vien quan ly truc tiep trong don vi de danh gia</b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getEmployeeListToAssess(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getEmployeeListToAssess (Lay danh sach nhan vien de danh"
                    + " gia) - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        // Kiem tra user id tren he thong 2
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("getEmployeeListToAssess (Lay danh sach nhan vien de danh"
                    + " gia) - Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Lay userId
        Long userId = user.getUserId();
        // Lay ma nhan vien
        String cardId = user.getStrCardNumber();
        log.setUserName(cardId);
        // Lay danh sach don vi co vai tro thu truong/lanh dao
        List<Long> listManagementOrgId = user.getListManagementOrg();
        // Khong phai thu truong/lanh dao
        // --> Tra ve thong bao ko co quyen
        if (CommonUtils.isEmpty(listManagementOrgId)) {
            LOGGER.error("getEmployeeListToAssess (Lay danh sach nhan vien de danh"
                    + " gia) - User khong co vai tro thu truong/lanh dao - username: "
                    + cardId);
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
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.ORG_ID,
                ConstantsFieldParams.PERIOD,
                // Datdc them tham so search
                // Them tham so check theo quy hay nam
                ConstantsFieldParams.IS_YEAR
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long orgId = Long.parseLong(listValue.get(0));
            String period = listValue.get(1);
            // Datdc la nam hay la quy
            String isYearString = listValue.get(2);
            Boolean isYear = false;
            if (!CommonUtils.isEmpty(isYearString)) {
                isYear = Boolean.valueOf(isYearString);
            }
            // Id don vi khong thuoc don vi user quan ly
            // --> Tra ve thong bao khong co quyen
            if (!listManagementOrgId.contains(orgId)) {
                LOGGER.error("getEmployeeListToAssess (Lay danh sach nhan vien de"
                        + " danh gia)- User khong co vai tro thu truong/lanh dao"
                        + " doi voi don vi nay - username: " + cardId + "\ndata: " + data);
                return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
            }
            TaskDAO taskDAO = new TaskDAO();
            List<EntityVhrEmployee> result = taskDAO.getEmployeeListToAssess(userId, orgId, period, 0, isYear);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error("getEmployeeListToAssess (Lay danh sach nhan vien de danh"
                    + " gia) - Exception - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    /**
     * <b>Lay danh sach cong viec de danh gia theo nhan vien</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getTaskListToAssessByEmployee(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getTaskListToAssessByEmployee (Lay danh sach cong viec"
                    + " de danh gia) - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        // Kiem tra user id tren he thong 2
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("getTaskListToAssessByEmployee (Lay danh sach cong viec"
                    + " de danh gia - Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        Long userId = user.getUserId();
        Long orgId = user.getSysOrgId();
        // Lay ma nhan vien
        String cardId = user.getStrCardNumber();
        log.setUserName(cardId);
        // Lay danh sach don vi co vai tro thu truong/lanh dao
        List<Long> listManagementOrgId = user.getListManagementOrg();
        // Khong phai thu truong/lanh dao
        // --> Tra ve thong bao ko co quyen
        if (CommonUtils.isEmpty(listManagementOrgId)) {
            LOGGER.error("getTaskListToAssessByEmployee (Lay danh sach cong viec"
                    + " de danh gia) - User khong co vai tro thu truong/lanh dao"
                    + " - username: " + cardId + "\ndata: " + data);
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
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.ENFORCEMENT_ID,
                ConstantsFieldParams.PERIOD,
                // Datdc them tham so search theo 
                // Them tham so check theo quy hay nam
                ConstantsFieldParams.IS_YEAR
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Id nguoi thuc hien
            Long enforcementId = null;
            try {
                enforcementId = Long.parseLong(listValue.get(0));
            } catch (NumberFormatException e) {
            }
            // Ky danh gia (yyyyMM)
            String period = listValue.get(1);
            // Datdc la nam hay la quy
            String isYearString = listValue.get(2);
            Boolean isYear = false;
            if (!CommonUtils.isEmpty(isYearString)) {
                isYear = Boolean.valueOf(isYearString);
            }
            TaskDAO taskDAO = new TaskDAO();
            //begin::cuongnv::18/1/2017
            List<Long> listEnforcement = new ArrayList<>();
            if (json.has(ConstantsFieldParams.TASK_LIST_ENFORCEMENT)) {
                try {
                    JSONArray nArray = json.getJSONArray(ConstantsFieldParams.TASK_LIST_ENFORCEMENT);
                    if (nArray != null && nArray.length() > 0) {
                        for (int i = 0; i < nArray.length(); i++) {
                            try {
                                listEnforcement.add(Long.parseLong(nArray.get(i).toString()));
                            } catch (NumberFormatException e) {
                            }
                        }
                    }
                } catch (JSONException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            //end
//            System.out.println("========================");
            if (enforcementId != null) {
//            System.out.println("========================111111111111");
//                System.out.println("enforcementId:"+enforcementId);
//                System.out.println("period:"+period);
//                System.out.println("userId:"+userId);
//                System.out.println("orgId:"+orgId);
                // Datdc them tham 
                // them tham so isYear la nam hay quy
                EntityTaskAssessment taskAssessment = taskDAO.getTaskListToAssessByEmployee(
                        enforcementId, period, userId, orgId, isYear);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, taskAssessment, aesKey);
            } else if (!CommonUtils.isEmpty(listEnforcement)) {
//                 System.out.println("========================222222222222222");
                List<EntityTaskAssessment> result = new ArrayList<>();
                EntityTaskAssessment taskAssessment;
                for (Long o : listEnforcement) {
//                    System.out.println("o:" + o);
//                    System.out.println("period:" + period);
//                    System.out.println("userId:" + userId);
//                    System.out.println("orgId:" + orgId);
                    // Datdc  them tham so isYear la nam hay quy
                    taskAssessment = taskDAO.getTaskListToAssessByEmployee(o,
                            period, userId, orgId, isYear);
                    taskAssessment.setEnforcementId(o);
                    result.add(taskAssessment);
                }
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
            } else {
//                System.out.println("========================3333333");
                LOGGER.error("getTaskListToAssessByEmployee (Lay danh sach cong viec"
                        + " de danh gia) - Exception - username: " + cardId + "\ndata: "
                        + data);
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            }
        } catch (Exception ex) {
            LOGGER.error("getTaskListToAssessByEmployee (Lay danh sach cong viec"
                    + " de danh gia) - Exception - username: " + cardId + "\ndata: "
                    + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    /**
     * <b>Huy bo file giao viec/danh gia da duoc ky</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String cancelSignedTaskFileByEmployee(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("cancelSignedTaskFile (Huy bo file giao viec/danh gia da"
                    + " duoc ky) - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        // Kiem tra user id tren he thong 2
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("cancelSignedTaskFile (Huy bo file giao viec/danh gia da"
                    + " duoc ky) - Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Lay ma nhan vien
        String cardId = user.getStrCardNumber();
        log.setUserName(cardId);
        // Lay danh sach don vi co vai tro thu truong/lanh dao
        List<Long> listManagementOrgId = user.getListManagementOrg();
        // Khong phai thu truong/lanh dao
        // --> Tra ve thong bao ko co quyen
        if (CommonUtils.isEmpty(listManagementOrgId)) {
            LOGGER.error("cancelSignedTaskFile (Huy bo file giao viec/danh gia da"
                    + " duoc ky) - User khong co vai tro thu truong/lanh dao - username: "
                    + cardId + "\ndata: " + data);
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
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.ENFORCEMENT_ID,
                ConstantsFieldParams.TYPE,
                // Datdc them period
                ConstantsFieldParams.PERIOD
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Id nguoi thuc hien
            Long enforcementId = Long.parseLong(listValue.get(0));
            int type = Integer.parseInt(listValue.get(1));
            String period = "";
            if (listValue.size() >= 3) {
                period = listValue.get(2);
            }
            TaskDAO taskDAO = new TaskDAO();
            int result = taskDAO.cancelSignedTaskFileByEmployee(enforcementId, type, period);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error("cancelSignedTaskFile (Huy bo file giao viec/danh gia da"
                    + " duoc ky - Exception - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    /**
     * <b>Lay danh sach cong viec ban giao/chia nho</b><br>
     *
     * @author SonDN
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String listSubOrTransferredTask(HttpServletRequest request, String data,
            String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("listSubOrTransferredTask (Lay danh sach cong viec ban giao) "
                    + "- Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("listSubOrTransferredTask (Lay danh sach cong viec ban giao) "
                    + "- Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        Long userId = user.getUserId();
        // Lay ra ma nhan vien
        String cardId = user.getStrCardNumber();
        log.setUserName(cardId);
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.TASK_ID,
                ConstantsFieldParams.TASK_PARENT_ID,
                ConstantsFieldParams.TASK_CREATED_DATE,
                ConstantsFieldParams.TYPE
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long taskId = null;
            Long parentId = null;
            taskId = CommonUtils.isInteger(listValue.get(0)) ? Long.parseLong(listValue.get(0)) : null;
            parentId = CommonUtils.isInteger(listValue.get(1)) ? Long.parseLong(listValue.get(1)) : null;
            String createdDate = listValue.get(2);
            Integer type = Integer.parseInt(listValue.get(3));
            TaskDAO taskDAO = new TaskDAO();
            EntityTask task = taskDAO.checkUserPermissionForTask(userId, taskId);
            if (type.equals(0)) {
                if (task == null
                        || task.getPermissionsForTask() == null
                        || task.getPermissionsForTask().getTransferredTask() == 0) {
                    LOGGER.error("listSubOrTransferredTask (Lay danh sach ban giao cong viec) - User khong co quyen "
                            + "dong cong viec - username: " + cardId + "\ndata: " + data);
                    return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
                }
            }
            List<EntityTask> result = taskDAO.getListSubOrTransferredTasks(taskId, parentId, createdDate, type);
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error("listSubOrTransferredTask (Lay danh sach ban giao cong viec) "
                    + "- Exception - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Lay danh sach nguoi phoi hop thuc hien cong viec</b>
     *
     * @author HaNH
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getCoordinator(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getCoordinator (Lay danh sach nguoi phoi hop thuc hien cong viec)"
                    + "Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }

        // Neu user null hoac user id null
        // -> Tra ve thong bao khong co quyen
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("getCoordinator (Lay danh sach nguoi phoi hop thuc hien cong viec)"
                    + "Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Lay ma nhan vien
        String cardId = user.getStrCardNumber();
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
                ConstantsFieldParams.MOBILE_PHONE
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String mobilePhone = listValue.get(0);
            StaffDAO staffDao = new StaffDAO();
            List<EntityVhrEmployee> result = staffDao.getCoordinator(mobilePhone);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error("getCoordinator (Lay danh sach nguoi phoi hop thuc hien cong viec)"
                    + " - Exception - username: " + cardId + "\ndata: "
                    + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    /**
     * <b>lấy danh sách công việc báo cáo đánh giá</b>
     *
     * @since 12/1/2017
     * @author cuongnv
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListTaskReportRating(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getListTaskReportRating - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }

        // Neu user null hoac user id null
        // -> Tra ve thong bao khong co quyen
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("getListTaskReportRating - Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Lay ma nhan vien
        String cardId = user.getStrCardNumber();
        log.setUserName(cardId);
        List<Long> listManagementOrgId = user.getListManagementOrg();
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
                ConstantsFieldParams.IS_COUNT,
                ConstantsFieldParams.START_RECORD,
                ConstantsFieldParams.PAGE_SIZE,
                ConstantsFieldParams.KEYWORD,
                ConstantsFieldParams.PERIOD,
                ConstantsFieldParams.ORG_ID,
                ConstantsFieldParams.TYPE,
                ConstantsFieldParams.ENFORCEMENT_ID,
                ConstantsFieldParams.LIST_ORG_ID,
                // Datdc add them la nam hay ko
                ConstantsFieldParams.IS_YEAR,
                "isEmp"
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Integer isCount = null;
            try {
                isCount = Integer.parseInt(listValue.get(0));
            } catch (NumberFormatException e) {
            }
            Long startRecord = null;
            try {
                startRecord = Long.parseLong(listValue.get(1));
            } catch (NumberFormatException e) {
            }
            Long pageSize = null;
            try {
                pageSize = Long.parseLong(listValue.get(2));
            } catch (NumberFormatException e) {
            }
            String keyword = listValue.get(3);
            String period = listValue.get(4);
            Long orgId = null;
            try {
                orgId = Long.parseLong(listValue.get(5));
            } catch (NumberFormatException e) {
            }
            Integer type = 0;
            try {
                type = Integer.parseInt(listValue.get(6));
            } catch (NumberFormatException e) {
            }
            Long enforcementId = null;
            try {
                enforcementId = Long.parseLong(listValue.get(7));
            } catch (NumberFormatException e) {
            }
            Type typeOfListLong = new TypeToken<ArrayList<Long>>() {
            }.getType();
            Gson gson = new Gson();
            List<Long> listOrgId = gson.fromJson(listValue.get(8), typeOfListLong);
            if (CommonUtils.isEmpty(listOrgId)) {
                listOrgId = listManagementOrgId;
            }
            TaskDAO taskDAO = new TaskDAO();
            
            // Datdc them tham so isYear la ki nam hay ki quy
            String isYearString  = "";
            if (!CommonUtils.isEmpty(listValue)) {
                isYearString = listValue.get(9);
            }
            Boolean isYear = false;
            if (!CommonUtils.isEmpty(isYearString)) {
                isYear = Boolean.valueOf(isYearString);
            }
            // Them isEmp de phan biet lay nhiem vu hay lay nhan vien
            Integer isEmp = null;
            try {
                isEmp = Integer.parseInt(listValue.get(10));
            } catch (NumberFormatException e) {
            }
            Object result = taskDAO.getListTaskReportRating(user.getUserId(),
                    isCount, startRecord, pageSize, keyword, period, orgId, type,
                    enforcementId, listOrgId, isYear, isEmp);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error("getListTaskReportRating-Data: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }
    
    /**
     * <b>Lay danh sach thong ke cong viec</b><br>
     *
     * @author SonDN
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListTaskStatistics(HttpServletRequest request, String data,
            String isSecurity) {

        String[] keys = new String[]{
            ConstantsFieldParams.START_TIME,
            ConstantsFieldParams.END_TIME,
            ConstantsFieldParams.TASK_LIST_ENFORCEMENT,
            ConstantsFieldParams.LIST_ORG_ID
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getListTaskStatistics (Thong ke cong viec) "
                    + "- Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("getListTaskStatistics (Thong ke cong viec) "
                    + "- Khong co thong tin user tren he thong 2!");
            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
        }
        // Lay danh sach don vi ma user co vai tro thu truong/lanh dao
        List<Long> listManagementOrgId = user.getListManagementOrg();
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            String startTime = listValue.get(0);
            String endTime = listValue.get(1);
            // Danh sach nguoi thuc hien
            Gson gson = new Gson();
            Type type1 = new TypeToken<ArrayList<EntityEnforcement>>() {
            }.getType();
            List<EntityEnforcement> listEnforcement = gson.fromJson(listValue.get(2), type1);
            TaskDAO taskDAO = new TaskDAO();
            // Danh sach don vi
            Type type2 = new TypeToken<ArrayList<Long>>() {
            }.getType();
            List<Long> listOrgId = gson.fromJson(listValue.get(3), type2);
            boolean isGetByUser = false;
            if (CommonUtils.isEmpty(listOrgId)) {                
                listOrgId = listManagementOrgId;
                isGetByUser = true;
            }
            List<EntityVhrEmployee> result = taskDAO.listTaskStatistics(startTime,
                    endTime, listEnforcement, listOrgId, userGroup.getUserId2(), isGetByUser);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            LOGGER.error("getListTaskStatistics (Thong ke cong viec) "
                    + "- Exception - username: " + userGroup.getCardId() + "\ndata: " + data, ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    /**
     * <b>Chia nho cong viec</b>
     *
     * @author HaNH
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String subEnforcementTask(HttpServletRequest request, String data,
            String isSecurity) {
        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("subEnforcementTask (Chia nho cong viec) "
                    + "- Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("subEnforcementTask (Chia nho cong viec) "
                    + "- Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        Long userId = user.getUserId();
        // Lay ra ma nhan vien
        String cardId = user.getStrCardNumber();
        log.setUserName(cardId);
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.TASK_COMMANDER_ID,
                ConstantsFieldParams.TASK_COMPLETED_PERCENT,
                ConstantsFieldParams.TASK_CONTENT,
                ConstantsFieldParams.TASK_END_TIME,
                ConstantsFieldParams.TASK_ENFORCEMENT_ID,
                ConstantsFieldParams.TASK_ORG_ID,
                ConstantsFieldParams.TASK_RATING_POINT,
                ConstantsFieldParams.TASK_START_TIME,
                ConstantsFieldParams.TASK_STATUS,
                ConstantsFieldParams.TASK_NAME,
                ConstantsFieldParams.TASK_RESULT,
                ConstantsFieldParams.TASK_UPDATE_FREQUENCY,
                ConstantsFieldParams.TASK_ID,
                ConstantsFieldParams.TASK_PATH,
                ConstantsFieldParams.TASK_PARATITION_BY,
                //minhnq them khi split
                ConstantsFieldParams.TASK_PERIOD,
                ConstantsFieldParams.TASK_PERIOD_TYPE,
                ConstantsFieldParams.TASK_OBJECT_TYPE,
                ConstantsFieldParams.TASK_KPI_INDEX,
                ConstantsFieldParams.TASK_RECIPE,
                ConstantsFieldParams.TASK_UNIT,
                ConstantsFieldParams.TASK_WORK_LOCATION,
                ConstantsFieldParams.TASK_PERCENT,
                ConstantsFieldParams.TASK_TARGET_MIN,
                ConstantsFieldParams.TASK_TARGET_EXPECT,
                ConstantsFieldParams.TASK_TARGET_CHALLENGE
                //minhnq end
                
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long commanderId = Long.parseLong(listValue.get(0));
            Long completedPercent = 0L;
            Long ratingPoint = null;
            try {
                completedPercent = Long.parseLong(listValue.get(1));
                ratingPoint = Long.parseLong(listValue.get(6));
            } catch (NumberFormatException e) {
            }
            String content = listValue.get(2);
            String endTime = listValue.get(3);
            Long enforcementId = Long.parseLong(listValue.get(4));
            Long orgId = Long.parseLong(listValue.get(5));
            String startTime = listValue.get(7);
            Long status = Long.parseLong(listValue.get(8));
            String taskName = listValue.get(9);
            String taskResult = listValue.get(10);
            Long updateFrequency = Long.parseLong(listValue.get(11));
            Long taskId = Long.parseLong(listValue.get(12));
            String taskPath = listValue.get(13);
            String partitionBy = listValue.get(14);
            Long isCompleted = 0L;
            String period = listValue.get(15);
            Long periodType = Long.parseLong(listValue.get(16));
            Long objectType = Long.parseLong(listValue.get(17));
            String kpiIndex = listValue.get(18);
            String recipe = listValue.get(19);
            String unit = listValue.get(20);
            String workLocation = listValue.get(21);
            Long percent = Long.parseLong(listValue.get(22));
            String targetMin = listValue.get(23);
            String targetExpect = listValue.get(24);
            String targetChallenge = listValue.get(25);
            //Chia nho cong viec,lua chon hoan thanh cung khong cap nhat is_complete = 1
//            if (commanderId.equals(userId)) {
//                if (completedPercent.equals(100L)) {
//                    isCompleted = 1L;
//                }
//            }
            TaskDAO taskDAO = new TaskDAO();
            Integer result = taskDAO.subEnforcementTask(commanderId, completedPercent,
                    content, endTime, enforcementId, orgId,
                    ratingPoint, startTime, status, taskName, taskResult,
                    updateFrequency, taskId, taskPath, userId, partitionBy, isCompleted,
                    period,objectType,workLocation,kpiIndex,recipe,unit,targetMin,targetExpect,targetChallenge,percent,periodType);
            if (result > 0) {
                //100517 chuyen van ban cho TT, LD, VT don vi
                //neu chia viec co nguon goc tu van ban
                List<Vof2_EntityUser> listStaff = new ArrayList<>();
                Vof2_EntityUser vof2EntityUser1 = new Vof2_EntityUser();
                vof2EntityUser1.setUserId(enforcementId);
                listStaff.add(vof2EntityUser1);
                sendDocToPersonTask(taskId, listStaff, userGroup.getItemEntityUser(),
                        userGroup.getVof2_ItemEntityUser(), taskPath);
                //Gui tin nhan chi viec viec
                //<editor-fold defaultstate="collapsed" desc="Gui tin nhan chia nho viec">
                //Lay config noi dung tin nhan multi language
                CommonControler controler = new CommonControler();
                List<String> listMess = new ArrayList<>();
                String strMess;
                SmsDAO smsDao = new SmsDAO();
                listMess.add(smsDao.gettAliasNameByUserId(commanderId)); //Ten nguoi giao
                listMess.add(taskName);

                //Duyet tung nguoi thuc hien cong viec
                String receipent = smsDao.getPhoneNumberRecv(enforcementId);
                strMess = controler.getStrMessConfig(listMess, 3L,
                        Constants.SMS_TEXT_CONFIG.PERSON_TASK_ASSGIN, enforcementId);

                //Insert vao bang sms
                smsDao.addMsgToSmsMaster(receipent, strMess,
                        userId, userId, enforcementId, 11L,
                        Constants.SMS_TEXT_INTERCEPT.RECEIVEDTASK_DOTASK);
            }
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error("subEnforcementTask (Chia nho cong viec) "
                    + "- Exception - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Chuyen nguoi thuc hien cong viec</b>
     *
     * @author HaNH
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String transferEnforcementTask(HttpServletRequest request, String data,
            String isSecurity) {
        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("transferEnforcementTask (Chuyen nguoi thuc hien cong viec) "
                    + "- Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("transferEnforcementTask (Chuyen nguoi thuc hien cong viec) "
                    + "- Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        Long userId = user.getUserId();
        // Lay ra ma nhan vien
        String cardId = user.getStrCardNumber();
        log.setUserName(cardId);
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.TASK_ID,
                ConstantsFieldParams.ENFORCEMENT_ID,
                ConstantsFieldParams.TASK_ORG_ID,
                ConstantsFieldParams.TASK_NAME,
                ConstantsFieldParams.TASK_COMMANDER_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            //ID cong viec chuan bi chuyen
            Long taskId = Long.parseLong(listValue.get(0));
            //ID nguoi thuc hien moi
            Long enforcementIdNew = Long.parseLong(listValue.get(1));
            //ID don vi cua nguoi thuc hien moi
            Long orgIdNew = Long.parseLong(listValue.get(2));
            //Ten cong viec
            String taskName = listValue.get(3);
            //Nguoi thuc hien chuyen cong viec
            Long commanderId = 0L;
            try {
                commanderId = Long.parseLong(listValue.get(4));
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            if (commanderId == 0L) {
                commanderId = userId;
            }

            TaskDAO taskDAO = new TaskDAO();
            Integer result = taskDAO.transferEnforcementTask(taskId, enforcementIdNew, userId, orgIdNew);
            if (result > 0) {
                //100517 chuyen van ban cho TT, LD, VT don vi
                //neu chia viec co nguon goc tu van ban
                EntityTask taskObj = taskDAO.getTaskDetail(taskId, userId, "vi");
                if (taskObj != null) {
                    List<Vof2_EntityUser> listStaff = new ArrayList<>();
                    Vof2_EntityUser vof2EntityUser1 = new Vof2_EntityUser();
                    vof2EntityUser1.setUserId(enforcementIdNew);
                    listStaff.add(vof2EntityUser1);
                    sendDocToPersonTask(taskId, listStaff, userGroup.getItemEntityUser(),
                            userGroup.getVof2_ItemEntityUser(), taskObj.getTaskPath());
                }

                //Gui tin nhan chuyen nguoi thuc hien cong viec
                //<editor-fold defaultstate="collapsed" desc="Gui tin nhan chuyen nguoi thuc hien cong viec">
                //Lay config noi dung tin nhan multi language
                CommonControler controler = new CommonControler();
                String strMess;
                SmsDAO smsDao = new SmsDAO();
                List<String> listMess = new ArrayList<>();
                listMess.add(smsDao.gettAliasNameByUserId(commanderId)); //Ten nguoi giao
                listMess.add(taskName);

                //Duyet tung nguoi thuc hien cong viec
                String receipent = smsDao.getPhoneNumberRecv(enforcementIdNew);
                strMess = controler.getStrMessConfig(listMess, 3L,
                        Constants.SMS_TEXT_CONFIG.PERSON_TASK_ASSGIN, enforcementIdNew);
//                System.out.print("Hiendv2 SMS Chuyen nguoi thuc hien cong viec: " + receipent + " Noi dung: " + strMess);

                //Insert vao bang sms
                smsDao.addMsgToSmsMaster(receipent, strMess,
                        userId, userId, enforcementIdNew, 11L, 
                        Constants.SMS_TEXT_INTERCEPT.RECEIVEDTASK_DOTASK);
//                System.out.print("Ket qua gui sms Chuyen nguoi thuc hien cong viec: " + smsReuslt);
                //</editor-fold>
            }
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error("transferEnforcementTask (Chuyen nguoi thuc hien cong viec) "
                    + "- Exception - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }


    /**
     * <b>Ghi lai gia tri danh gia cong viec</b>
     *
     * @author HaNH
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String saveTaskRating(HttpServletRequest request, String data,
            String isSecurity) {
        
        String response;
        String[] keys = new String[]{
            ConstantsFieldParams.LIST_TASK
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            response = FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
            return FunctionCommon.returnResultAfterLog(response, userGroup.getUserId2(),
                    "Session timeout");
        }
        // Kiem tra user id tren he thong 2
        if (!userGroup.checkUserId2()) {
            response = FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
            return FunctionCommon.returnResultAfterLog(response, userGroup.getUserId2(),
                    "Khong co thong tin user tren he thong 2");
        }
        try {
            // Parse du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            // Danh sach nhiem vu
            String strListTask = listValue.get(0);
            Type type = new TypeToken<ArrayList<EntityTask>>() {
            }.getType();
            Gson gson = new Gson();
            List<EntityTask> listTask = gson.fromJson(strListTask, type);
            if (CommonUtils.isEmpty(listTask)) {
                response = FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
                return FunctionCommon.returnResultAfterLog(response, userGroup.getUserId2(),
                        "Loi du lieu dau vao");
            }
            TaskDAO taskDAO = new TaskDAO();
            Integer result = taskDAO.saveTaskRating(userGroup.getUserId2(), listTask,
                    Constants.TaskRating.Status.LEADER_ASSESSED);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            return FunctionCommon.returnResultAfterLog(userGroup, ex);
        }
    }
    
    /**
     * <b>Ghi lai gia tri danh gia thai do lam viec</b>
     *
     * @author LinhLN
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String saveAcerageTask(HttpServletRequest request, String data,
            String isSecurity) {
        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("saveTaskRating (Ghi lai gia tri danh gia cong viec) "
                    + "- Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("saveTaskRating (Ghi lai gia tri danh gia cong viec) "
                    + "- Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        Long userId = user.getUserId();
        // Lay ra ma nhan vien
        String cardId = user.getStrCardNumber();
        log.setUserName(cardId);
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONArray arrAverageTask = FunctionCommon.jsonGetArray("listAverageTask", data);
            List<EntityAverageTask> listAverageTask = new LinkedList<>();
            if (arrAverageTask != null && arrAverageTask.length() > 0) {
                EntityAverageTask taskItem;
                for (int i = 0; i < arrAverageTask.length(); i++) {
                    //Get object cua tung doi tuong mang
                    JSONObject innerObj = (JSONObject) arrAverageTask.get(i);
                    String[] keysAverage = new String[]{
                        "avergeTask",
                        "attitudeRating",
                        "attitudePoint",
                        "commentAverage",
                        "empId",
                        "avergeTaskRatingId",
                        "period",
                        // Datdc them tham so
                        // noi quy lao dong
                        "complyRule",
                        // Ghi chu NQLD
                        "complyRuleNote",
                        // tuan thu DDLV
                        "complyLocation",
                        // Ghi chu tuan thu DDLV
                        "complyLocationNote",
                        "periodType"
                        
                    };
                    List<String> listValueAverage = FunctionCommon.getValuesFromJSON(innerObj, keysAverage);
                    taskItem = new EntityAverageTask();
                    Double avergeTask = null;
                    if(FunctionCommon.isNumeric(listValueAverage.get(0))){
                        avergeTask = Double.parseDouble(listValueAverage.get(0));
                        taskItem.setAvergeTask(avergeTask);
                    }
                    Long attitudeRating = null;
                    if(FunctionCommon.isNumeric(listValueAverage.get(1))){
                        attitudeRating = Long.parseLong(listValueAverage.get(1));
                        taskItem.setAttitudeRating(attitudeRating);
                    }
                    Long attitudePoint = null;
                    if(FunctionCommon.isNumeric(listValueAverage.get(2))){
                        attitudePoint = Long.parseLong(listValueAverage.get(2));
                        taskItem.setAttitudePoint(attitudePoint);
                    }
                    String commentAverage = listValueAverage.get(3);
                    taskItem.setCommentAverage(commentAverage);
                    Long empId = null;
                    if(FunctionCommon.isNumeric(listValueAverage.get(4))){
                        empId = Long.parseLong(listValueAverage.get(4));
                        taskItem.setEmpId(empId);
                    }
                    Long avergeTaskRatingId = null;
                    if(FunctionCommon.isNumeric(listValueAverage.get(5))){
                        avergeTaskRatingId = Long.parseLong(listValueAverage.get(5));
                        taskItem.setAvergeTaskRatingId(avergeTaskRatingId);
                    } else {
                        taskItem.setAvergeTaskRatingId(0L);
                    }
                    String period = listValueAverage.get(6).trim();
                    taskItem.setRatingPriod(period);
                    
                    // Datdc them tham so start
                    // noi quy lao dong
                    Long complyRule = null;
                    if(FunctionCommon.isNumeric(listValueAverage.get(7))){
                        complyRule = Long.parseLong(listValueAverage.get(7));
                        taskItem.setComplyRule(complyRule);
                    }
                    // Ghi chu NQLD complyRuleNote
                    String complyRuleNote = listValueAverage.get(8);
                    taskItem.setComplyRuleNote(complyRuleNote);
                    // Tuan thu dia diem lam viec
                    Long complyLocation = null;
                    if(FunctionCommon.isNumeric(listValueAverage.get(9))){
                        complyLocation = Long.parseLong(listValueAverage.get(9));
                        taskItem.setComplyLocation(complyLocation);
                    }
                    // Ghi chu Tuan thu dia diem lam viec
                    String complyLocationNote = listValueAverage.get(10);
                    taskItem.setComplyLocationNote(complyLocationNote);
                    Long periodType = null;
                    if(FunctionCommon.isNumeric(listValueAverage.get(11))){
                        periodType = Long.parseLong(listValueAverage.get(11));
                        taskItem.setPeriodType(periodType);
                    }
                    // Datdc them tham so end
                    listAverageTask.add(taskItem);
                }
            }
            if (CommonUtils.isEmpty(listAverageTask)) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
            TaskDAO taskDAO = new TaskDAO();
            Integer result = taskDAO.saveAverageTask(listAverageTask, userId);
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error("saveAverageTask (Ghi lai gia tri danh gia thai do lam viec) "
                    + "- Exception - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    /**
     * <b>Ghi lai gia tri danh gia cong viec</b>
     *
     * @author HaNH
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String saveTaskRatingEmp(HttpServletRequest request, String data,
            String isSecurity) {
        
        String response;
        String[] keys = new String[]{
            ConstantsFieldParams.LIST_TASK
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            response = FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
            return FunctionCommon.returnResultAfterLog(response, userGroup.getUserId2(),
                    "Session timeout");
        }
        // Kiem tra user id tren he thong 2
        if (!userGroup.checkUserId2()) {
            response = FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
            return FunctionCommon.returnResultAfterLog(response, userGroup.getUserId2(),
                    "Khong co thong tin user tren he thong 2");
        }
        try {
            // Parse du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            // Danh sach nhiem vu
            String strListTask = listValue.get(0);
            Type type = new TypeToken<ArrayList<EntityTask>>() {
            }.getType();
            Gson gson = new Gson();
            List<EntityTask> listTask = gson.fromJson(strListTask, type);
            if (CommonUtils.isEmpty(listTask)) {
                response = FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
                return FunctionCommon.returnResultAfterLog(response, userGroup.getUserId2(),
                        "Loi du lieu dau vao");
            }
            TaskDAO taskDAO = new TaskDAO();
            Integer result = taskDAO.saveTaskRating(userGroup.getUserId2(), listTask,
                    Constants.TaskRating.Status.EMPLOYEE_SELF_ASSESSED);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            return FunctionCommon.returnResultAfterLog(userGroup, ex);
        }
    }
    
        /**
     * <b>Ghi lai gia tri danh gia cong viec</b>
     *
     * @author HaNH
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String updateRatioDetail(HttpServletRequest request, String data,
            String isSecurity) {
        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("updateRatioDetail (Ghi lai cau hinh tieu chi danh gia) "
                    + "- Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("updateRatioDetail (Ghi lai cau hinh tieu chi danh gia) "
                    + "- Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        Long userId = user.getUserId();
        // Lay ra ma nhan vien
        String cardId = user.getStrCardNumber();
        log.setUserName(cardId);
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONArray arrTask = FunctionCommon.jsonGetArray("listRatioDetail", data);
            List<RatioConfigDetail> listRatioDetail = new LinkedList<>();
            if (arrTask != null && arrTask.length() > 0) {
                RatioConfigDetail ratioConfigDetail;
                for (int i = 0; i < arrTask.length(); i++) {
                    //Get object cua tung doi tuong mang
                    JSONObject innerObj = (JSONObject) arrTask.get(i);
                    ratioConfigDetail = new RatioConfigDetail();
                    try {
                        Long ratioConfigId = innerObj.getLong("ratioConfigId");
                        ratioConfigDetail.setRatioConfigId(ratioConfigId);
                        Double ratio = innerObj.getDouble("ratio");
                        ratioConfigDetail.setRatio(ratio);
                        Double ratioPointMin = innerObj.getDouble("ratioPointMin");
                        ratioConfigDetail.setRatioPointMin(ratioPointMin);
                        Double ratioPointDefault = innerObj.getDouble("ratioPointDefault");
                        ratioConfigDetail.setRatioPointDefault(ratioPointDefault);
                        Double ratioPointMax = innerObj.getDouble("ratioPointMax");
                        ratioConfigDetail.setRatioPointMax(ratioPointMax);
                        Long yAxisId = innerObj.getLong("yAxisId");
                        ratioConfigDetail.setyAxisId(yAxisId);
                        Long xAxisId = innerObj.getLong("xAxisId");
                        ratioConfigDetail.setxAxisId(xAxisId);
                        listRatioDetail.add(ratioConfigDetail);
                    } catch (JSONException e) {
                        LOGGER.error("Loi! JSONException", e);
                    }
                }
            }
            if (CommonUtils.isEmpty(listRatioDetail)) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
            TaskDAO taskDAO = new TaskDAO();
            Integer result = taskDAO.updateRatioDetail(listRatioDetail, userId);
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error("saveTaskRating (Ghi lai gia tri danh gia cong viec) "
                    + "- Exception - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    /**
     * <b>Xuat bao cao cong viec da phe duyet theo ky</b><br>
     *
     * @author SonDN
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String exportReportTaskApproved(HttpServletRequest request, String data,
            String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("exportReportTaskApproved (Xuat bao cao cong viec da phe duyet theo ky) "
                    + "- Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("exportReportTaskApproved (Xuat bao cao cong viec da phe duyet theo ky) "
                    + "- Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        Gson gson = new Gson();
        Long userId = user.getUserId();
        // Lay ra ma nhan vien
        String cardId = user.getStrCardNumber();
        log.setUserName(cardId);
        // Lay danh sach don vi ma user co vai tro thu truong/lanh dao
        List<Long> listManagementOrgId = user.getListManagementOrg();
        // Khong phai thu truong/lanh dao
        if (CommonUtils.isEmpty(listManagementOrgId)) {
            LOGGER.error("exportReportTaskApproved - User khong co vai tro thu truong/lanh dao - username: "
                    + cardId);
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
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.ORG_ID,
                ConstantsFieldParams.PERIOD,
                ConstantsFieldParams.STATE
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long orgId = null;
            try {
                orgId = Long.parseLong(listValue.get(0));
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            String period = listValue.get(1);
            Integer state = 1;
            try {
                state = Integer.parseInt(listValue.get(2));
            } catch (NumberFormatException e) {
            }
            //Lay danh sach nguoi thuc hien
            List<EntityEnforcement> listEnforcement = new ArrayList<>();
            if (json.has(ConstantsFieldParams.TASK_LIST_ENFORCEMENT)) {
                JSONArray arrFiles = json.getJSONArray(ConstantsFieldParams.TASK_LIST_ENFORCEMENT);
                if (arrFiles.length() > 0) {
                    EntityEnforcement tmp;
                    for (int i = 0; i < arrFiles.length(); i++) {
                        try {
                            JSONObject innerObj = arrFiles.getJSONObject(i);
                            tmp = gson.fromJson(innerObj.toString(), EntityEnforcement.class);
                            listEnforcement.add(tmp);
                        } catch (JSONException ex) {
                            LOGGER.error("exportReportTaskApproved:listEnforcement - Exception:", ex);
                        }
                    }
                }
            }
            TaskDAO taskDAO = new TaskDAO();
            List<EntityTask> result = taskDAO.exportReportTaskApproved(orgId, period, listEnforcement, state, userId);
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException | JsonSyntaxException ex) {
            LOGGER.error("exportReportTaskApproved (Xuat bao cao da phe duyet theo ky) "
                    + "- Exception - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * chuyen van ban cho ca nhan thuc hien cong viec
     *
     * @param taskId
     * @param enforcementId
     * @param userVof1
     * @param userVof2
     */
    void sendDocToPersonTask(Long taskId, List<Vof2_EntityUser> listStaff, EntityUser userVof1,
            Vof2_EntityUser userVof2, String taskPath) {
        try {
            //lay id cong viec goc
            Long taskOrgId = -1L;
            if (taskPath != null) {
                String[] arrTaskId = taskPath.split("/");
                if (arrTaskId.length >= 2) {
                    taskOrgId = Long.valueOf(arrTaskId[1]);
                }
            }
            TaskDAO taskDAO = new TaskDAO();
            List<EntitySourceMap> listSourceMap;
            if (taskId.equals(taskOrgId)) {
                //la chinh cong viec goc
                listSourceMap = (List<EntitySourceMap>) taskDAO
                        .getListSourceOfTask(taskId);
            } else {
                //lay nguon goc tu cong viec goc
                listSourceMap = (List<EntitySourceMap>) taskDAO
                        .getListSourceOfTask(taskOrgId);
            }
            if (listSourceMap != null && !listSourceMap.isEmpty()) {
                Long sourceTypeTmp;
                Long objectTypeTmp;
                for (EntitySourceMap sm : listSourceMap) {
                    sourceTypeTmp = sm.getSourceType().longValue();
                    objectTypeTmp = sm.getObjectType().longValue();
                    if (sourceTypeTmp == 4L && objectTypeTmp == 1L) {
                        //thuc hien chuyen van ban
                        //thuc hien chuyen van ban
                        DocumentDAO docDAO = new DocumentDAO();
                        docDAO.sendDocumentToStaffFromTask(sm.getSourceId(), listStaff, null,
                                userVof1, userVof2);
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.error("sendDocToPersonTask: ", ex);
        }
    }
    
    /**
     * getListRatioConfigDT
     *
     * @author LinhLN
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListRatioConfigDT(HttpServletRequest request, String data,
            String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getListRatioConfigDT (Xuat bao cao cong viec da phe duyet theo ky) "
                    + "- Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("getListRatioConfigDT (Xuat bao cao cong viec da phe duyet theo ky) "
                    + "- Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        Long orgId = user.getSysOrgId();
        // Lay ra ma nhan vien
        String cardId = user.getStrCardNumber();
        log.setUserName(cardId);
        // Lay danh sach don vi ma user co vai tro thu truong/lanh dao
        List<Long> listManagementOrgId = user.getListManagementOrg();
        // Khong phai thu truong/lanh dao
        if (CommonUtils.isEmpty(listManagementOrgId)) {
            LOGGER.error("exportReportTaskApproved - User khong co vai tro thu truong/lanh dao - username: "
                    + cardId);
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
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.PERIOD
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String period = listValue.get(0);

            TaskDAO taskDAO = new TaskDAO();
            // Datdc them tham so isYear
            List<EntityRatio> result = taskDAO.getListRatioConfigDT( period, orgId, false);
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException | JsonSyntaxException ex) {
            LOGGER.error("exportReportTaskApproved (Xuat bao cao da phe duyet theo ky) "
                    + "- Exception - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    /**
     * Get danh sach rank
     * @param request
     * @param isSecurity
     * @param data
     * @return 
     */
    public String getListRank(HttpServletRequest request, String isSecurity, String data) {
        String[] keys = new String[]{
            ConstantsFieldParams.ORG_ID,
            ConstantsFieldParams.PERIOD
        };
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);
        TaskDAO taskDAO = new TaskDAO();
    
        try {
                       
            List<String> listValue = dataSessionGR.getListParamsFromClient();
            Long orgId = 0L;
            if (!CommonUtils.isEmpty(listValue.get(0))) {
                orgId = Long.parseLong(listValue.get(0));
            }
            String period = listValue.get(1);
            List<EntityEmpRating> result = taskDAO.getListRank(orgId,period);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
        } catch (Exception ex) {
            LOGGER.error("getListForCombobox - Exception - username: " + dataSessionGR.getCardId(), ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }
    
    /**
     * getTaskListToAssessByEmployeeEmp
     *
     * @author LinhLN
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getTaskListToAssessByEmployeeEmp(HttpServletRequest request, String data,
            String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getListRatioConfigDT (Xuat bao cao cong viec da phe duyet theo ky) "
                    + "- Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("getListRatioConfigDT (Xuat bao cao cong viec da phe duyet theo ky) "
                    + "- Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        Gson gson = new Gson();
        Long userId = user.getUserId();
        Long orgId = user.getSysOrgId();
        // Lay ra ma nhan vien
        String cardId = user.getStrCardNumber();
        log.setUserName(cardId);
        // Lay danh sach don vi ma user co vai tro thu truong/lanh dao
        List<Long> listManagementOrgId = user.getListManagementOrg();
        // Khong phai thu truong/lanh dao
        if (CommonUtils.isEmpty(listManagementOrgId)) {
          listManagementOrgId.add(user.getSysOrgId());
        }
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.PERIOD,
                ConstantsFieldParams.DATA_SEARCH,
                // Datdc Them tham so check theo quy hay nam
                ConstantsFieldParams.IS_YEAR
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String period = listValue.get(0);
            String dataSearch = listValue.get(1);
            TaskDAO taskDAO = new TaskDAO();
            // Datdc la nam hay la quy
            String isYearString = listValue.get(2);
            Boolean isYear = false;
            if (!CommonUtils.isEmpty(isYearString)) {
                isYear = Boolean.valueOf(isYearString);
            }
            // Datdc them tham so quy va la nam hay ko
            EntityTaskAssessment result = taskDAO.getTaskListToAssessByEmployeeEmp(
                    userId, period, dataSearch, orgId, isYear);
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException | JsonSyntaxException ex) {
            LOGGER.error("exportReportTaskApproved (Xuat bao cao da phe duyet theo ky) "
                    + "- Exception - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * Phe duyet hoac tu choi cong viec
     * 
     * @param request
     * @param isSecurity
     * @param data
     * @return 
     */
    public String approveOrRejectTask(HttpServletRequest request, String isSecurity,
            String data) {
        
        String[] keys = new String[]{
            ConstantsFieldParams.TASK_ID,
            ConstantsFieldParams.IS_APPROVE,
            ConstantsFieldParams.REASON
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("approveOrRejectTask - Session timeout!");
            return FunctionCommon.responseResult(ErrorCode.NO_SESSION, null, null);
        }        
        try {
            // Parse du lieu tu client
            List<String> listValue = userGroup.getListParamsFromClient();
            // Id cong viec
            Long taskId = Long.parseLong(listValue.get(0));
            // 1: phe duyet, 0: Tu choi
            boolean isApprove = "1".equals(listValue.get(1));
            // Ly do tu choi
            String reason = listValue.get(2);
            // Cap nhat database
            TaskDAO taskDAO = new TaskDAO();
            int result = taskDAO.approveOrRejectTask(userGroup.getUserId2(),
                    userGroup.getName2(), taskId, isApprove, reason) ? 1 : 0;
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            LOGGER.error("approveOrRejectTask - Exception - username: "
                    + userGroup.getCardId(), ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }
    
    public String checkStatusSignedTaskFileByEmployee(HttpServletRequest request, String isSecurity,
            String data) {
        
        String[] keys = new String[]{
            ConstantsFieldParams.PERIOD,
            ConstantsFieldParams.TYPE,
            ConstantsFieldParams.EMPLOYEE_ID
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("checkStatusSignedTaskFileByEmployee - Session timeout!");
            return FunctionCommon.responseResult(ErrorCode.NO_SESSION, null, null);
        }        
        try {
            // Parse du lieu tu client
            List<String> listValue = userGroup.getListParamsFromClient();
            if (CommonUtils.isEmpty(listValue.get(2)) || CommonUtils.isEmpty(listValue.get(1))
                    || CommonUtils.isEmpty(listValue.get(0))) {
                return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
            }
            Integer type = Integer.parseInt(listValue.get(1));
            String period = listValue.get(0);
            Long employeeId = Long.parseLong(listValue.get(2));
            
            TaskDAO taskDAO = new TaskDAO();
            boolean result = taskDAO.checkStatusSignedTaskFileByEmployee(period, employeeId, type);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result ? 1 : 0, userGroup);
        } catch (Exception ex) {
            LOGGER.error("checkStatusSignedTaskFileByEmployee - Exception - username: "
                    + userGroup.getCardId(), ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }
}
