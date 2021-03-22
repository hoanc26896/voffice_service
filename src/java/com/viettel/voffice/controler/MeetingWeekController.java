/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import java.lang.reflect.Type;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.file.FileAttachmentDAO;
import com.viettel.voffice.database.dao.logAction.ActionLogMobileDAO;
import com.viettel.voffice.database.dao.meeting.MeetingWeekDAO;
import com.viettel.voffice.database.dao.staff.OrgDAO;
import com.viettel.voffice.database.dao.staff.UserDAO;
import com.viettel.voffice.database.dao.staff.UserRoleDAO;
import com.viettel.voffice.database.entity.EntityAttach;
import com.viettel.voffice.database.entity.EntityFiles;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.EntityUserRole;
import com.viettel.voffice.database.entity.EntityVhrEmployee;
import com.viettel.voffice.database.entity.EntityVhrOrg;
import com.viettel.voffice.database.entity.User.EntityUser;
import com.viettel.voffice.database.entity.User.Vof2_EntityUser;
import com.viettel.voffice.database.entity.calendar.MeetingApproveResult;
import com.viettel.voffice.database.entity.calendar.MeetingCommanderResult;
import com.viettel.voffice.database.entity.meeting.EntityMeetingCommander;
import com.viettel.voffice.database.entity.meeting.EntityMeetingMember;
import com.viettel.voffice.database.entity.task.EntityMeeting;
import com.viettel.voffice.database.entity.task.EntityMeetingAssistant;
import com.viettel.voffice.database.entity.task.EntityMeetingResource;
import com.viettel.voffice.database.entity.text.EntityText;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.DateUtils;
import com.viettel.voffice.utils.LogUtils;

/**
 *
 * @author kiennt45
 */
@SuppressWarnings("deprecation")
public class MeetingWeekController {

    // Log file
    private static final Logger LOGGER = Logger.getLogger(MeetingWeekController.class);

    // Ten cua class bao gom ca ten package
    private static final String CLASS_NAME = MeetingWeekController.class.getName();

    /**
     * <b>Lay lich hop tuan</b><br>
     *
     * @param data
     * @param request
     * @return
     */
    public String getMeetingWeek(String data, HttpServletRequest request) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String response;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        ErrorCode errorCode;
        String deviceName = null;
        Long userId = null;
        // Session khong hop le
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getMeetingWeek (Lay lich hop tuan) - Session timeout");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        // Lay key AES -> Giai ma du lieu
        String strAesKeyDecode = userGroup.getStrAesKey();
        data = SecurityControler.decodeDataByAes(strAesKeyDecode, data);
        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            // Parse du lieu
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.TYPE,
                ConstantsFieldParams.WEEK_AMOUNT,
                ConstantsFieldParams.DEVICE_NAME,
                "getAllPresident"
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Loai
            int type = 0;
            String strType = listValue.get(0);
            if (!CommonUtils.isEmpty(strType)) {
                type = Integer.parseInt(strType);
            }
            // So luong tuan dich chuyen
            int weekAmount = 0;
            String strWeekAmount = listValue.get(1);
            if (!CommonUtils.isEmpty(strWeekAmount)) {
                weekAmount = Integer.parseInt(strWeekAmount);
            }
            deviceName = listValue.get(2);
            String getAllPresident = listValue.get(3);
            userId = userGroup.getVof2_ItemEntityUser().getUserId();
            Long orgIdsByRole = userGroup.getVof2_ItemEntityUser().getAdOrgId();

            List<Long> orgPermissionIds = new ArrayList<Long>();
            if (!CommonUtils.isEmpty(userGroup.getVof2_ItemEntityUser().getListManagementOrg())) {
                orgPermissionIds.addAll(userGroup.getVof2_ItemEntityUser().getListManagementOrg());
            }
            if (!CommonUtils.isEmpty(userGroup.getVof2_ItemEntityUser().getListMeetingManagerOrg())) {
                orgPermissionIds.addAll(userGroup.getVof2_ItemEntityUser().getListMeetingManagerOrg());
            }

            MeetingWeekDAO meetingWeekDAO = new MeetingWeekDAO();
            List<MeetingApproveResult> listItem = meetingWeekDAO.getMeetingApprove(
                    userGroup, orgIdsByRole, type, weekAmount, null,
                    null, null, null, null, "", null, null, orgPermissionIds);
            //datnv5: thuc hien lay danh sach lanh dao
            if ("1".equals(getAllPresident) && type == 5) {
                UserDAO userDAO = new UserDAO();
                List<EntityUser> listPresident = userDAO.getListPresident();
                if (listItem != null && listItem.size() > 0) {
                    listItem.get(0).setListPresident(listPresident);
                } else {
                    listItem = new ArrayList<>();
                    MeetingApproveResult item = new MeetingApproveResult();
                    item.setListPresident(listPresident);
                    listItem.add(item);
                }
            }
            errorCode = ErrorCode.SUCCESS;
            response = FunctionCommon.generateResponseJSON(errorCode, listItem, strAesKeyDecode);
        } catch (Exception ex) {
            LOGGER.error("getMeetingWeek (Lay lich hop tuan) - Exception -"
                    + " username: " + cardId + "\ndata: " + data, ex);
            errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
            response = FunctionCommon.generateResponseJSON(errorCode, null, null);
        }
        // Ghi log database
        ActionLogMobileDAO actionLogMobileDAO = new ActionLogMobileDAO();
//        EntityUser user1 = userGroup.getItemEntityUser();
//        Long userId1 = (user1 == null || user1.getUserId() == null) ? 0L : user1.getUserId();
        String description = "SERVICE2: Lấy danh sách lịch họp - errorCode: "
                + errorCode.getErrorCode();
        actionLogMobileDAO.insert(userId, cardId, log.getStartTime(), new Date(),
                "MettingWeek.getLstMeetingWeek", description, log.getPath(),
                null, null, deviceName, null);
        return response;
    }

    /**
     * <b>Lay danh sach truc chi huy</b><br>
     *
     * @param request
     * @param data
     * @return
     */
    public String getMeetingComanderResult(HttpServletRequest request,
            String data) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Session khong hop le
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getMeetingComanderResult (Lay danh sach truc chi huy)"
                    + " - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        // Lay thong tin user tren he thong 2
        if (!userGroup.checkUserId2()) {
            LOGGER.error("getMeetingComanderResult (Lay danh sach truc chi huy)"
                    + " - Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Lay key AES de giai ma du lieu
        String aesKey = userGroup.getStrAesKey();
        data = SecurityControler.decodeDataByAes(aesKey, data);
        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        LogUtils.logFunctionalStart(log);
        // Lay id don vi cua user
        Long orgId = userGroup.getVof2_ItemEntityUser().getAdOrgId();
        try {
            // Parse du lieu
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.WEEK_AMOUNT,
                ConstantsFieldParams.DEVICE_NAME
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // So luong tuan dich chuyen
            int weekAmount = 0;
            String strWeekAmount = listValue.get(0);
            if (!CommonUtils.isEmpty(strWeekAmount)) {
                weekAmount = Integer.parseInt(strWeekAmount);
            }
            // Ten thiet bi
            String deviceName = listValue.get(1);
            MeetingWeekDAO meetingWeekDAO = new MeetingWeekDAO();
            List<MeetingCommanderResult> listItem = meetingWeekDAO.getMeetingComanderResult(
                    orgId, weekAmount);
            List<MeetingCommanderResult> lstOldData = meetingWeekDAO.getMeetingComanderResultOldData(orgId, weekAmount);
            for(MeetingCommanderResult obj : lstOldData){
            	listItem.add(obj);
            }
            // Ghi log vao database
            LogActionControler aclog = new LogActionControler();
            Vof2_EntityUser itemUser = userGroup.getVof2_ItemEntityUser();
            aclog.insertActionLog(itemUser.getUserId(), itemUser.getStrCardNumber(),
                    "MeetingWeekController.getMeetingComanderResult", request,
                    "Lấy danh sách trực chỉ huy", log.getStartTime(), deviceName, "");
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, listItem, aesKey);
        } catch (Exception ex) {
            LOGGER.error("getMeetingComanderResult (Lay danh sach truc chi huy)"
                    + " - Exception - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Lay danh sach lich cong tac</b><br>
     *
     * @param strData
     * @param req
     * @return
     */
    public String getWorkMeetingDirector(String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        Date startDate = new Date();
        LogActionControler aclog = new LogActionControler();
        String actionName = "MettingWeek.getWorkMeetingDirector";
        ErrorCode errorCode;
        String cardId = "";
        String deviceName = null;
        // Session hep le
        if (dataSessionGR.getCheckSessionOk()) {
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
                //lay gia tri client gui len
                int type;

//                Long userId = null;
//                String strUserId = dataSessionGR.getVof2_ItemEntityUser().getUserId().toString();
//                String orgIdsByRole = dataSessionGR.getVof2_ItemEntityUser().getAdOrgId().toString();
//                String strType = (String) FunctionCommon.jsonGetItem("type", strDataClient);
//                if (strUserId != null && !"".equals(strUserId)) {
//                    userId = Long.valueOf(strUserId);
//                }
//                if (strType != null && !"".equals(strType)) {
//                    type = Integer.valueOf(strType);
//                }
                Long userId = null;
                String strUserId = dataSessionGR.getVof2_ItemEntityUser().getUserId().toString();
                String orgIdsByRole = dataSessionGR.getVof2_ItemEntityUser().getAdOrgId().toString();
                if (strUserId != null && !"".equals(strUserId)) {
                    userId = Long.valueOf(strUserId);
                }
                String isBtgd = "";
                if (FunctionCommon.jsonGetItem("isBtgd", strDataClient) != null) {
                    isBtgd = String.valueOf(FunctionCommon.jsonGetItem("isBtgd", strDataClient));
                }

                String getAllPresident = "";
                if (FunctionCommon.jsonGetItem("getAllPresident", strDataClient) != null) {
                    getAllPresident = String.valueOf(FunctionCommon.jsonGetItem("getAllPresident", strDataClient));
                }
                //Mac dinh la lay lich cong tac cua muc BGD 
                String strType;

                //Neu tham gia truyen len la BGD
                if (isBtgd != null && "0".equals(isBtgd)) {
//                    System.out.println("Luan gui tham so lich cong tac:" + isBtgd);
                    strType = "3";
                } else if (isBtgd != null && "1".equals(isBtgd)) {
                    //Tham so truyen vao la lich Ban TGD
//                    System.out.println("Luan gui tham so lich cong tac:" + isBtgd);
                    strType = "5";
                } else {
                    strType = (String) FunctionCommon.jsonGetItem("type", strDataClient);
                }
                deviceName = (String) FunctionCommon.jsonGetItem(ConstantsFieldParams.DEVICE_NAME, strDataClient);
                type = Integer.valueOf(strType);
                
                List<Long> orgPermissionIds = new ArrayList<Long>();
                if (!CommonUtils.isEmpty(dataSessionGR.getVof2_ItemEntityUser().getListManagementOrg())) {
                    orgPermissionIds.addAll(dataSessionGR.getVof2_ItemEntityUser().getListManagementOrg());
                }
                if (!CommonUtils.isEmpty(dataSessionGR.getVof2_ItemEntityUser().getListMeetingManagerOrg())) {
                    orgPermissionIds.addAll(dataSessionGR.getVof2_ItemEntityUser().getListMeetingManagerOrg());
                }
                
                MeetingWeekDAO meetingD = new MeetingWeekDAO();
                List<MeetingApproveResult> listItem = meetingD.getWorkMeetingDirector(dataSessionGR, Long.valueOf(orgIdsByRole),
                        type, orgPermissionIds);
                errorCode = ErrorCode.SUCCESS;
                //datnv5: thuc hien lay danh sach lanh dao
                if (getAllPresident != null && "1".equals(getAllPresident.trim())
                        && "5".equals(strType)) {
                    UserDAO userDAO = new UserDAO();
                    List<EntityUser> listPresident = userDAO.getListPresident();
                    if (listPresident != null && listPresident.size() > 0 && listItem != null && listItem.size() > 0) {
                        listItem.get(0).setListPresident(listPresident);
                    } else if (listPresident != null && listPresident.size() > 0) {
                        listItem = new ArrayList<>();
                        MeetingApproveResult item = new MeetingApproveResult();
                        item.setListPresident(listPresident);
                        listItem.add(item);
                    }
                }
                strResult = FunctionCommon.generateResponseJSON(errorCode, listItem,
                        strAesKeyDecode);
                Vof2_EntityUser itemUser = dataSessionGR.getVof2_ItemEntityUser();
                aclog.insertActionLog(itemUser.getUserId(), itemUser.getStrCardNumber(),
                        "MeetingWeekController.getWorkMeetingDirector", req,
                        "Danh sách id là lãnh đạo", startDate, "", "");
            } catch (Exception ex) {
                LOGGER.error("getWorkMeetingDirector (Lay danh sach lich cong tac)"
                        + " - Exception - username: " + cardId + "\ndata: " + strDataClient, ex);
                errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
                strResult = FunctionCommon.generateResponseJSON(errorCode, null, null);
            }
        } // Session timeout
        else {
            LOGGER.error("getWorkMeetingDirector (Lay danh sach lich cong tac) - Session timeout!");
            errorCode = dataSessionGR.getEnumErrCode();
            strResult = FunctionCommon.generateResponseJSON(errorCode, null, null);
        }
        // Ghi log database
        ActionLogMobileDAO actionLogMobileDAO = new ActionLogMobileDAO();
        EntityUser user1 = dataSessionGR.getItemEntityUser();
        Long userId1 = (user1 == null || user1.getUserId() == null) ? 0L : user1.getUserId();
        String description = "SERVICE2: Lấy danh sách lịch công tác - errorCode: "
                + errorCode.getErrorCode();
        actionLogMobileDAO.insert(userId1, cardId, startDate, new Date(),
                actionName, description, req.getLocalAddr() + ":" + req.getLocalPort(),
                null, null, deviceName, null);
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return strResult;
    }

    /**
     * <b>Lay 3 lich hop gan nhat</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String get3MeetingNearestOnDashboard(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("get3MeetingNearestOnDashboard (Lay 3 lich hop gan nhat)"
                    + " - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        if (user2 == null || user2.getUserId() == null) {
            LOGGER.error("get3MeetingNearestOnDashboard (Lay 3 lich hop gan nhat)"
                    + " - Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        Long userId2 = user2.getUserId();
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
//            System.out.println("luanvd-get3MeetingNearestOnDashboard-data: \n"+data);
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
                ConstantsFieldParams.FROM_DATE,
                ConstantsFieldParams.TYPE,
                ConstantsFieldParams.COUNT
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String dateFrom = listValue.get(0);
            String type = listValue.get(1);
            Integer typeGetData = null;
            if (!CommonUtils.isEmpty(type)) {
                try {
                    typeGetData = Integer.parseInt(type.trim());
                } catch (Exception ex) {
                    typeGetData = null;
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
            String count = listValue.get(2);
            Integer countMax = null;
            if (!CommonUtils.isEmpty(count)) {
                try {
                    countMax = Integer.parseInt(count.trim());
                } catch (Exception ex) {
                    countMax = null;
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
            
            MeetingWeekDAO cmtDAO = new MeetingWeekDAO();
            Object result = cmtDAO.get3MeetingNearestOnDashboard(userGroup, dateFrom, typeGetData, countMax);
            if (result == null) {
                LOGGER.error("get3MeetingNearestOnDashboard (Lay 3 lich hop gan nhat)"
                        + " - result = null - username: " + cardId + "\ndata: " + data);
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
            LogUtils.logFunctionalEnd(log);
//            Object  obj =  FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, null);
//            System.out.println("luanvd-get3MeetingNearestOnDashboard-result: \n"+obj);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error("get3MeetingNearestOnDashboard (Lay 3 lich hop gan nhat)"
                    + " - Exception - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    /**
     * <b>Lay chi tiet cuoc hop</b>
     *
     * @param request
     * @param data
     * @return
     */
    public String getMeetingDetail(HttpServletRequest request, String data) {

        String[] keys = new String[]{
            ConstantsFieldParams.MEETING_ID
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getMeetingDetail [Lay chi tiet cuoc hop] - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            // Parse du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            String strMeetingId = listValue.get(0);
            Long meetingId = null;
            if (strMeetingId != null && !"".equals(strMeetingId)) {
                meetingId = Long.parseLong(strMeetingId);
            }
            MeetingWeekDAO meetingWeekDAO = new MeetingWeekDAO();
            MeetingApproveResult result = meetingWeekDAO.getMeetingDetail(userGroup, meetingId);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception e) {
            LOGGER.error("getMeetingDetail - Exception!", e);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Lay danh sach cuoc hop</b>
     *
     * @param request
     * @param data
     * @return
     */
    public String getListMeeting(HttpServletRequest request, String data) {
        String[] keys = new String[]{
            "employeeCode",
            ConstantsFieldParams.TYPE,
            ConstantsFieldParams.START_TIME_FROM,
            ConstantsFieldParams.START_TIME_TO,
            "listIdPresident",
            "listIdPrepare",
            "listIdParticipate",
            ConstantsFieldParams.START_RECORD,
            ConstantsFieldParams.PAGE_SIZE,
            ConstantsFieldParams.KEYWORD
        };
        
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getListMeeting [Lay danh sach cuoc hop] - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        
        try {
            // Parse du lieu client gui len
            JSONObject json = new JSONObject(data);
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            int type = -1;
            String startTimeFrom = listValue.get(2);
            String startTimeTo = listValue.get(3);

            String listIdPresident = listValue.get(4);
            String listIdPrepare = listValue.get(5);
            String listIdParticipate = listValue.get(6);
            // Vi tri ban ghi lay ra
            Long startRecord = 0L;
            String strStartRecord = listValue.get(7);
            if (!CommonUtils.isEmpty(strStartRecord) && FunctionCommon.isNumeric(strStartRecord)) {
                startRecord = Long.parseLong(strStartRecord);
            }
            // So ban ghi lay ra
            Long pageSize = 10L;
            String strPageSize = listValue.get(8);
            if (!CommonUtils.isEmpty(strPageSize) && FunctionCommon.isNumeric(strPageSize)) {
                pageSize = Long.parseLong(strPageSize);
            }
            String keyWord = listValue.get(9);
            //1.get listId user from listCardId
            List<Long> listLgIdPresident = getListIdFromListCard(listIdPresident);
            List<Long> listLgIdPrepare = getListIdFromListCard(listIdPrepare);
            List<Long> listLgIdParticipate = getListIdFromListCard(listIdParticipate);

            List<Long> orgPermissionIds = new ArrayList<Long>();
            if (!CommonUtils.isEmpty(userGroup.getVof2_ItemEntityUser().getListManagementOrg())) {
                orgPermissionIds.addAll(userGroup.getVof2_ItemEntityUser().getListManagementOrg());
            }
            if (!CommonUtils.isEmpty(userGroup.getVof2_ItemEntityUser().getListMeetingManagerOrg())) {
                orgPermissionIds.addAll(userGroup.getVof2_ItemEntityUser().getListMeetingManagerOrg());
            }
            
            MeetingWeekDAO meetingWeekDAO = new MeetingWeekDAO();
            List<MeetingApproveResult> listMeeting = meetingWeekDAO.getMeetingApprove(
                    userGroup, 0L, type, 0, startTimeFrom,
                    startTimeTo, listLgIdPresident, listLgIdPrepare, listLgIdParticipate,
                    keyWord, startRecord, pageSize, orgPermissionIds);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, listMeeting, null);
        } catch (JSONException | NumberFormatException e) {
            LOGGER.error("getMeetingDetail - Exception!", e);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     *
     * @param listIdPresident
     * @return
     */
    private List<Long> getListIdFromListCard(String listIdPresident) {
        List<String> listStrIdPresident
                = FunctionCommon.getListStringFromString(listIdPresident);
        UserDAO udao = new UserDAO();
        List<Long> listResult = null;
        if (listStrIdPresident != null) {
            listResult = udao.getListUserVoffice2FromListCardId(listStrIdPresident);
        }
        return listResult;
    }

    public String getMeetingList(HttpServletRequest request, String data) {
        String[] keys = new String[]{
            "employeeCode",
            "meetingId",
            "fromTime",
            "toTime",
            "title",
            "presidentId",
            "preparerId",
            "participantId",
            ConstantsFieldParams.START_RECORD,
            ConstantsFieldParams.PAGE_SIZE
        };
        try {
            // Parse du lieu client gui len
            List<String> listValue;
            if (data != null) {
                JSONObject json = new JSONObject(data);
                listValue = FunctionCommon.getValuesFromJSON(json, keys);
            } else {
                listValue = new ArrayList<>();
                for (int i = 0; i < keys.length; i++) {
                    listValue.add("");
                }
            }

            String employeeCode = listValue.get(0);
            String meetingId = listValue.get(1);
            String startTimeFrom = listValue.get(2);
            String startTimeTo = listValue.get(3);
            String titleSearch = listValue.get(4);

            String listIdPresident = listValue.get(5);
            String listIdPrepare = listValue.get(6);
            String listIdParticipate = listValue.get(7);
            // Vi tri ban ghi lay ra
            Long startRecord = 0L;
            String strStartRecord = listValue.get(8);
            if (!CommonUtils.isEmpty(strStartRecord) && FunctionCommon.isNumeric(strStartRecord)) {
                startRecord = Long.parseLong(strStartRecord);
            }
            // So ban ghi lay ra
            Long pageSize = 10L;
            String strPageSize = listValue.get(9);
            if (!CommonUtils.isEmpty(strPageSize) && FunctionCommon.isNumeric(strPageSize)) {
                pageSize = Long.parseLong(strPageSize);
            }
            //1.get listId user from listCardId
            List<Long> listLgIdPresident = getListIdFromListCard(listIdPresident);
            List<Long> listLgIdPrepare = getListIdFromListCard(listIdPrepare);
            List<Long> listLgIdParticipate = getListIdFromListCard(listIdParticipate);
            //lay id nguoi dung tu ma nhan vien
            Long userId = 0L;
            if (employeeCode != null && employeeCode.trim().length() > 0) {
                UserDAO udao = new UserDAO();
                Vof2_EntityUser vof2_EntityUser = udao.vof2_GetUserInforVHRByCardId(employeeCode);
                if (vof2_EntityUser != null) {
                    userId = vof2_EntityUser.getUserId();
                }
            }
            //convert data from timesteam to date time
            Format format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String strDateFrom = "";
            if (startTimeFrom.trim().length() > 0 && FunctionCommon.isNumeric(startTimeFrom)) {
                Long timeFrom = Long.valueOf(startTimeFrom);
                Date dateFrom = new Date(timeFrom);
                strDateFrom = format.format(dateFrom);
            }
            String strDateTo = "";
            if (startTimeTo.trim().length() > 0 && FunctionCommon.isNumeric(startTimeTo)) {
                Long timeTo = Long.valueOf(startTimeTo);
                Date dateTo = new Date(timeTo);
                strDateTo = format.format(dateTo);
            }

//            System.out.println("strDateFrom: " + strDateFrom);
//            System.out.println("strDateTo: " + strDateTo);
            MeetingWeekDAO meetingWeekDAO = new MeetingWeekDAO();
            List<MeetingApproveResult> listMeeting = meetingWeekDAO.getMeetingList(
                    userId, titleSearch, meetingId, strDateFrom,
                    strDateTo, listLgIdPresident, listLgIdPrepare,
                    listLgIdParticipate, startRecord, pageSize);

            //add danh sach file
            for (MeetingApproveResult meetingApproveResult : listMeeting) {
                FileAttachmentDAO fileAttachmentDAO = new FileAttachmentDAO();
                List<EntityAttach> listFile = fileAttachmentDAO.getListFileByMettingId(
                        meetingApproveResult.getMeetingId());
                meetingApproveResult.setAttachment(listFile);

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Date parsedDate;
                try {
                    String aa = meetingApproveResult.getStartTime();
                    parsedDate = dateFormat.parse(aa);
                    meetingApproveResult.setStartTime(String.valueOf(parsedDate.getTime()));

                    parsedDate = dateFormat.parse(meetingApproveResult.getEndTime());
                    meetingApproveResult.setEndTime(String.valueOf(parsedDate.getTime()));
                } catch (ParseException ex) {
                    LOGGER.error("getMeetingList!", ex);
                }

            }
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, listMeeting, null);
        } catch (JSONException | NumberFormatException e) {
            LOGGER.error("getMeetingDetail - Exception!", e);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Lay chi tiet cuoc hop</b>
     *
     * @param request
     * @param data
     * @return
     */
    public String getMeetingListByText(HttpServletRequest request, String data) {

        String[] keys = new String[]{
            ConstantsFieldParams.KEYWORD,
            "presidentOrgId",
            "presidentId",
            ConstantsFieldParams.START_TIME_FROM,
            ConstantsFieldParams.START_TIME_TO,
            "isNotAddToText",
            ConstantsFieldParams.MEETING_ORG_ID_DIRECTOR_OTHER,
            ConstantsFieldParams.MEETING_DIRECTOR_ORG_ID_OTHER
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getMeetingListByText [Lay ds cuoc hop theo van ban] - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            // Parse du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            String keyword = listValue.get(0);
            // Id don vi nguoi chu tri
            Long presidentOrgId = null;
            String strPresidentOrgId = listValue.get(1);
            if (!CommonUtils.isEmpty(strPresidentOrgId)) {
                presidentOrgId = Long.parseLong(strPresidentOrgId);
            }
            // ID nguoi chu tri
            Long presidentId = null;
            String strPresidentId = listValue.get(2);
            if (!CommonUtils.isEmpty(strPresidentId)) {
                presidentId = Long.parseLong(strPresidentId);
            }
            String startTimeFrom = listValue.get(3);
            String startTimeTo = listValue.get(4);
            // isNotAddToText = 1 nghia la cac cuoc hop chua duoc gan vao van ban
            Integer isNotAddToText = null;
            String strIsNotAddToText = listValue.get(5);
            if (!CommonUtils.isEmpty(strIsNotAddToText)) {
                isNotAddToText = Integer.parseInt(strIsNotAddToText);
            }
            // datdc add them tham so tim kiem start
            // don vi ca nhan tham gia
            Long orgIdDirectorOther = null;
            if (!CommonUtils.isEmpty(listValue.get(6))) {
                orgIdDirectorOther = Long.valueOf(listValue.get(6));
            }
            List<Long> directorOrgOther = new ArrayList<Long>();
            if (!CommonUtils.isEmpty(listValue.get(7))) {
                directorOrgOther = FunctionCommon.getListIdFromString(listValue.get(7));
            }
            MeetingWeekDAO meetingWeekDAO = new MeetingWeekDAO();
            List<MeetingApproveResult> result = meetingWeekDAO.getMeetingListByText(
                    userGroup, keyword, presidentOrgId, presidentId,
                    startTimeFrom, startTimeTo, isNotAddToText, null, orgIdDirectorOther, directorOrgOther);
            // datdc add them tham so tim kiem end
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception e) {
            LOGGER.error("getMeetingListByText [Lay ds cuoc hop theo van ban] - Exception!", e);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Lay danh sach nguoi tham gia cuoc hop</b>
     *
     * @param request
     * @param data
     * @return
     */
    public String getMeetingParticipantList(HttpServletRequest request, String data) {

        String[] keys = new String[]{
            ConstantsFieldParams.MEETING_ID,};
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getMeetingParticipantList [Lay ds nguoi tham gia cuoc hop]"
                    + " - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            // Parse du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            Long meetingId = Long.parseLong(listValue.get(0));
            MeetingWeekDAO meetingWeekDAO = new MeetingWeekDAO();
            Map<String, String> result = meetingWeekDAO.getMeetingParticipantList(
                    userGroup.getUserId2(), meetingId);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception e) {
            LOGGER.error("getMeetingParticipantList [Lay ds nguoi tham gia cuoc hop]"
                    + " - Exception!", e);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Lay danh sach cau truyen hinh</b>
     *
     * @param request
     * @param data
     * @return
     */
    public String getVideoConferencingList(HttpServletRequest request, String data) {

        String[] keys = new String[]{
            ConstantsFieldParams.MEETING_ID,};
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getVideoConferencingList [Lay ds nguoi tham gia cuoc hop]"
                    + " - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            // Parse du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            Long meetingId = Long.parseLong(listValue.get(0));
            MeetingWeekDAO meetingWeekDAO = new MeetingWeekDAO();
            List<EntityMeetingResource> result = meetingWeekDAO.getVideoConferencingList(
                    userGroup.getUserId2(), meetingId);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception e) {
            LOGGER.error("getVideoConferencingList [Lay ds nguoi tham gia cuoc hop]"
                    + " - Exception!", e);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    public String checkLeaderIdForAssistantUserInMemberList(HttpServletRequest request, String data) {
        String[] keys = new String[]{
            ConstantsFieldParams.MEETING_ASSISTANT_EMPLOYEE_ID,
            ConstantsFieldParams.MEETING_ID
        };
//        String result = null;
//        int sqlResult = 0;
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("checkLeaderIdForAssistantUserInMemberList [Lay lanh dao cua user tro ly]"
                    + " - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            // Parse du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            if (CommonUtils.isEmpty(listValue.get(0)) || CommonUtils.isEmpty(listValue.get(1))) {
            	return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
            }
            Long assistantId = Long.parseLong(listValue.get(0));
            Long meetingId = Long.parseLong(listValue.get(1));
            MeetingWeekDAO meetingWeekDAO = new MeetingWeekDAO();
            List<Integer> assiType = meetingWeekDAO.checkLeaderInMemberList(assistantId, meetingId);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, assiType, userGroup);

        } catch (Exception e) {
            LOGGER.error("checkLeaderIdForAssistantUserInMemberList [Lay lanh dao cua user tro ly]"
                    + " - fail!");
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    public String getListApproveCalendar(HttpServletRequest request, String data) {
        String[] keys = new String[]{
            ConstantsFieldParams.KEYWORD,
            ConstantsFieldParams.TYPE,
            "loaded",
            "perload",
            ConstantsFieldParams.START_TIME_FROM,
            ConstantsFieldParams.START_TIME_TO
        };
//        String result = null;
//        int sqlResult = 0;
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getListApproveCalendar [Lay danh sach cuoc hop duyet lich mobile]"
                    + " - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            // Parse du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            String keyWord = listValue.get(0).trim();
            String strType = listValue.get(1).trim();
            String strLoaded = listValue.get(2).trim();
            String strPerload = listValue.get(3).trim();
            String strStartTimeFrom = listValue.get(4).trim();
            String strStartTimeTo = listValue.get(5).trim();

            Integer type = null;
            if (strType != null && !strType.equals("")) {
                try {
                    type = Integer.parseInt(strType);
                } catch (NumberFormatException nfe) {
                    LOGGER.error("getListApproveCalendar [convert number wrong value]"
                            + " - type " + strType);
                }
            }

            Long loaded = null, perload = null;
            if (strLoaded != null && !strLoaded.equals("")) {
                try {
                    loaded = Long.parseLong(strLoaded);
                } catch (NumberFormatException nfe) {
                    LOGGER.error("getListApproveCalendar [convert number wrong value]"
                            + " - load " + strLoaded);
                }
            }

            if (strPerload != null && !strPerload.equals("")) {
                try {
                    perload = Long.parseLong(strPerload);
                } catch (NumberFormatException nfe) {
                    LOGGER.error("getListApproveCalendar [convert number wrong value]"
                            + " - load " + strPerload);
                }
            }

            MeetingWeekDAO mwd = new MeetingWeekDAO();
            UserDAO userDAO = new UserDAO();
            List<EntityVhrEmployee> lstEmp = (List<EntityVhrEmployee>) userDAO.checkMeetingManager(userGroup.getUserId2());
            List<Long> approveMeetingOrg = new ArrayList<>();
            for (EntityVhrEmployee emp : lstEmp) {
                approveMeetingOrg.add(emp.getOrganizationId());
            }
            List<MeetingApproveResult> result = mwd.getListApproveCalendar(userGroup,
                    keyWord, type, loaded, perload, approveMeetingOrg, strStartTimeFrom, strStartTimeTo);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);

        } catch (Exception e) {
            LOGGER.error("getListApproveCalendar [Lay danh sach lich hop]"
                    + " - fail!");
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    public String checkPermisionCalendar(HttpServletRequest request, String data) {
        String[] keys = new String[]{
            ConstantsFieldParams.MEETING_ID
        };
        MeetingApproveResult result = null;
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("checkPermisionCalendar [check phan quyen cho button]"
                    + " - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            // Parse du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            String strMeetingId = listValue.get(0);
//            System.out.println("Userid: "+userGroup.getUserId2() + " - strMeetiingId: " + strMeetingId);
            Long meetingId = null;
            if (strMeetingId != null && !strMeetingId.equals("")) {
                try {
                    meetingId = Long.parseLong(strMeetingId);
                } catch (NumberFormatException nfe) {
                    LOGGER.error("checkPermisionCalendar [check phan quyen cho button]"
                            + " - meetingId " + strMeetingId);
                    return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, userGroup);
                }
            }

            MeetingWeekDAO mwd = new MeetingWeekDAO();
            result = mwd.checkPermisionCalendar(userGroup.getUserId2(), meetingId);

            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);

        } catch (Exception e) {
            LOGGER.error("checkPermisionCalendar [check phan quyen cho button]"
                    + " - fail!");
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    public String approveCalendar(HttpServletRequest request, String data) {
        String[] keys = new String[]{
            ConstantsFieldParams.MEETING_ID
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("approveCalendar [phe duyet lich hop]"
                    + " - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            // Parse du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            String strMeetingId = listValue.get(0);

            Long meetingId = null;
            if (strMeetingId != null && !strMeetingId.equals("")) {
                try {
                    meetingId = Long.parseLong(strMeetingId);
                } catch (NumberFormatException nfe) {
                    LOGGER.error("approveCalendar [phe duyet lich hop]"
                            + " - meetingId " + strMeetingId);
                    return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, userGroup);
                }
            }
            MeetingWeekDAO mwd = new MeetingWeekDAO();
            MeetingApproveResult meetingDetail = mwd.getMeetingDetail(userGroup, meetingId);
            Long oldStatus = meetingDetail.getStatus();
            // update trang thai lich hop
            Long result = mwd.updateMeetingState(userGroup.getUserId2(), meetingId, 2, null);
            if (!result.equals(1L)) {
                return FunctionCommon.responseResult(ErrorCode.SUCCESS, 0, userGroup);
            }
            meetingDetail.setStatus(2L);
            mwd.sendNotiApproveMeeting(oldStatus, meetingDetail, userGroup, null);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception e) {
            LOGGER.error("approveCalendar [phe duyet lich hop] - fail!");
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    public String rejectCalendar(HttpServletRequest request, String data) {
        String[] keys = new String[]{
            ConstantsFieldParams.MEETING_ID,
            ConstantsFieldParams.COMMENT
        };
        Long result = null;
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("rejectCalendar [tu choi lich hop] - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            // Parse du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            String strMeetingId = listValue.get(0);
            String comment = listValue.get(1);
            Long meetingId = null;
            if (strMeetingId != null && !strMeetingId.equals("")) {
                try {
                    meetingId = Long.parseLong(strMeetingId);
                } catch (NumberFormatException nfe) {
                    LOGGER.error("rejectCalendar [tu choi lich hop] - meetingId " + strMeetingId);
                    return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, userGroup);
                }
            }

            MeetingWeekDAO mwd = new MeetingWeekDAO();
            MeetingApproveResult meetingDetail = mwd.getMeetingDetail(userGroup, meetingId);
            Long oldStatus = meetingDetail.getStatus();
            result = mwd.updateMeetingState(userGroup.getUserId2(), meetingId, 3, comment);
            meetingDetail.setStatus(3L);
            if (result != null && result.intValue() == 1) {
                mwd.sendNotiApproveMeeting(oldStatus, meetingDetail, userGroup, comment);
            }

            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);

        } catch (Exception e) {
            LOGGER.error("rejectCalendar [tu choi lich hop] - fail!");
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    public String cancelCalendar(HttpServletRequest request, String data) {
        String[] keys = new String[]{
            ConstantsFieldParams.MEETING_ID,
            ConstantsFieldParams.COMMENT
        };
        Long result = null;
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("cancelCalendar [huy lich hop] - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            // Parse du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            String strMeetingId = listValue.get(0);
            String comment = listValue.get(1);
            Long meetingId = null;
            if (strMeetingId != null && !strMeetingId.equals("")) {
                try {
                    meetingId = Long.parseLong(strMeetingId);
                } catch (NumberFormatException nfe) {
                    LOGGER.error("cancelCalendar [huy lich hop] - meetingId " + strMeetingId);
                    return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, userGroup);
                }
            }

            MeetingWeekDAO mwd = new MeetingWeekDAO();
            MeetingApproveResult meetingDetail = mwd.getMeetingDetail(userGroup, meetingId);
            Long oldStatus = meetingDetail.getStatus();
            result = mwd.updateMeetingState(userGroup.getUserId2(), meetingId, 4, comment);
            meetingDetail.setStatus(4L);
            if (result != null && result.intValue() == 1) {
                mwd.sendNotiApproveMeeting(oldStatus, meetingDetail, userGroup, comment);
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);

        } catch (Exception e) {
            LOGGER.error("cancelCalendar [huy lich hop] - fail!");
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    public String deleteCalendar(HttpServletRequest request, String data) {
        String[] keys = new String[]{
            ConstantsFieldParams.MEETING_ID,
            ConstantsFieldParams.COMMENT
        };
        Integer result = null;
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("deleteCalendar [xoa lich hop]"
                    + " - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            // Parse du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            String strMeetingId = listValue.get(0);
            String comment = listValue.get(1);
            Long meetingId = null;
            if (strMeetingId != null && !strMeetingId.equals("")) {
                try {
                    meetingId = Long.parseLong(strMeetingId);
                } catch (NumberFormatException nfe) {
                    LOGGER.error("deleteCalendar [xoa lich hop]"
                            + " - meetingId " + strMeetingId);
                    return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, userGroup);
                }
            }


            MeetingWeekDAO mwd = new MeetingWeekDAO();
            MeetingApproveResult meetingDetail = mwd.getMeetingDetail(userGroup, meetingId);
            result = mwd.deleteCalendar(userGroup.getUserId2(), meetingId, comment);
            if (result != null && result.intValue() == 1) {
                mwd.sendNotiDeleteMeeting(meetingDetail, userGroup, comment);
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);

        } catch (Exception e) {
            LOGGER.error("deleteCalendar [xoa lich hop]"
                    + " - fail!");
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    public String getListLocationFree(HttpServletRequest request, String data) {
        String[] keys = new String[]{
            ConstantsFieldParams.KEYWORD,
            "loaded",
            "perload",
            "listDeptId",
            "startDate",
            "endDate",
            "type"
        };
        List<EntityMeetingResource> result = null;
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getListLocationFree [lay danh sach phong hop]"
                    + " - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            // Parse du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            String keyword = listValue.get(0);
            String strLoad = listValue.get(1);
            String strPerload = listValue.get(2);
            String strListDepId = listValue.get(3);
            String strStartDate = listValue.get(4);
            String strEndDate = listValue.get(5);
            String type = listValue.get(6);

            Long loaded = 0L, perload = null;
            List<Long> listDepId = new ArrayList<>();
            try {
                if (!CommonUtils.isEmpty(strLoad)) {
                    loaded = Long.parseLong(strLoad);
                }
                if (!CommonUtils.isEmpty(strPerload)) {
                    perload = Long.parseLong(strPerload);
                }
                if (!CommonUtils.isEmpty(strListDepId)) {
                    StringTokenizer st = new StringTokenizer(strListDepId, ";");
                    while (st.hasMoreTokens()) {
                        String orId = st.nextToken();
                        if (orId != null && !orId.equals("")) {
                            Long val = Long.parseLong(orId);
                            listDepId.add(val);
                        }
                    }
                }
            } catch (NumberFormatException nfe) {
                LOGGER.error("getListLocationFree [lay danh sach phong hop]"
                        + " - Wrong value number: " + nfe.toString());
            }
            // Them lstDonvi quan ly meeting_resource
            // Xu ly giong web
            UserRoleDAO urDao = new UserRoleDAO();
            List<EntityUserRole> userRoles = urDao.getUserByUserId(userGroup.getUserId2());
            List<Long> listOrgManager = new ArrayList<>();
            // Neu co truyen type
            if (null!= type & !CommonUtils.isEmpty(type)) {
                if (!CommonUtils.isEmpty(userRoles)) {
                    for (EntityUserRole ur : userRoles) {
                        if (Constants.SYS_ROLE_QLLH.equals(ur.getSysRoleId()) 
                                || Constants.SYS_ROLE_ADMIN.equals(ur.getSysRoleId())
                                || Constants.SYS_ROLE_SUB_ADMIN.equals(ur.getSysRoleId())
                                || Constants.SYS_ROLE_QLCTH.equals(ur.getSysRoleId())) {
                            listOrgManager.add(ur.getOrgId());
                        }
                    }
                }
            }
            // Xu ly giong web
            
            MeetingWeekDAO meetingWeekDAO = new MeetingWeekDAO();
            result = meetingWeekDAO.getListLocationFree(userGroup.getUserId2(),
                    keyword, listDepId, strStartDate, strEndDate, loaded, perload, listOrgManager);

            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);

        } catch (Exception e) {
            LOGGER.error("getListLocationFree [lay danh sach phong hop]"
                    + " - fail!");
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    public String changeLocation(HttpServletRequest request, String data) {
        String[] keys = new String[]{
            ConstantsFieldParams.MEETING_ID,
            "roomId",
            "otherRooms",
            "reason",};
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("changeLocation [thay doi phong hop] - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            String strMeetingId = listValue.get(0);
            String strRoomId = listValue.get(1);
            String otherRooms = listValue.get(2);
            String reason = listValue.get(3);
            Long meetingId = 0L, roomId = null;
            try {
                if (!CommonUtils.isEmpty(strMeetingId)) {
                    meetingId = Long.parseLong(strMeetingId);
                }
                if (!CommonUtils.isEmpty(strRoomId)) {
                    roomId = Long.parseLong(strRoomId);
                }
            } catch (NumberFormatException nfe) {
                LOGGER.error("changeLocation [thay doi phong hop] - Wrong value number: " + nfe.toString());
            }
            MeetingWeekDAO meetingWeekDAO = new MeetingWeekDAO();
            MeetingApproveResult meetingDetail = meetingWeekDAO.getMeetingDetail(userGroup, meetingId);
            Long approvalId = findOrgApproval(meetingDetail, roomId, userGroup.getUserId2());
            if (meetingDetail != null && approvalId != null) {
                boolean isApprove = meetingDetail.getStatus() == 2 ? true : false;
                boolean isSendMail = meetingDetail.getIsSendMailMeeting() != null && meetingDetail.getIsSendMailMeeting().intValue() > 0 ? true : false;
                boolean changeOrgApproval = !approvalId.equals(meetingDetail.getOrgApprovalId());
                if (isApprove && CommonUtils.isEmpty(reason) && isSendMail) {
                    return FunctionCommon.responseResult(ErrorCode.SUCCESS, Constants.MEETING.MESSAGE.REASON_IS_NULL, userGroup);
                }
                //doi phong hop duoc quan ly
                if (roomId != null && CommonUtils.isEmpty(otherRooms)) {
                    if (!meetingWeekDAO.changeLocationWithRoomManaged(userGroup.getUserId2(), meetingId, roomId, approvalId,
                            reason, isApprove, changeOrgApproval)) {
                        return FunctionCommon.responseResult(ErrorCode.SUCCESS, 0, userGroup);
                    }
                    MeetingApproveResult meetingNewDetail = meetingWeekDAO.getMeetingDetail(userGroup, meetingId);
                    meetingWeekDAO.sendNotiUpdateMeeting(meetingDetail.getStatus(), meetingNewDetail, changeOrgApproval, userGroup, reason);
                    if (changeOrgApproval) {
                        meetingWeekDAO.updateStatusSendMeetingEmail(meetingId, null);
                    }
                }

                return FunctionCommon.responseResult(ErrorCode.SUCCESS, 1, userGroup);
            } else {
                if (meetingDetail == null) {
                    LOGGER.error("changeLocation [thay doi phong hop] khong lay duoc thong tin lich hop !");
                }
                if (approvalId == null) {
                    LOGGER.error("changeLocation [thay doi phong hop] khong lay duoc don vi duyet lich !");
                }
                return FunctionCommon.responseResult(ErrorCode.ERR_NODATA, null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("changeLocation [thay doi phong hop]"
                    + " - fail!");
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    private String getContentSmsToApproval(String empCode, String fullName, MeetingApproveResult meetingDetail) {
        String content = "";
        // Check them neu la lich dao tao start
        switch (meetingDetail.getStatus().intValue()) {
            case 1:
                content = "(LICH HOP) D/c " + empCode + "_" + fullName + " DE NGHI PHE DUYET cuoc hop: " + meetingDetail.getSubject()
                        + ". Thoi gian: " + meetingDetail.getStartHour() + ":" + meetingDetail.getStartMinute();
                if (null != meetingDetail.getTypeEducate()
                        && !meetingDetail.getEndDay().equals(meetingDetail.getDate())) {
                    content += " ngay " + meetingDetail.getEndDay();
                }
                content += " den " + meetingDetail.getEndHour() + ":" + meetingDetail.getEndMinute()
                        + " ngay " + meetingDetail.getDate()
                        + ". Dia diem: " + meetingDetail.getRoomName();
                break;
            case 2:
                content = "(LICH HOP) D/c " + empCode + "_" + fullName + " da DUYET cuoc hop: " + meetingDetail.getSubject()
                        + ". Thoi gian: " + meetingDetail.getStartHour() + ":" + meetingDetail.getStartMinute();
                if (null != meetingDetail.getTypeEducate()
                        && !meetingDetail.getEndDay().equals(meetingDetail.getDate())) {
                    content += " ngay " + meetingDetail.getEndDay();
                }
                content += " den " + meetingDetail.getEndHour() + ":" + meetingDetail.getEndMinute()
                        + " ngay " + meetingDetail.getDate()
                        + ". Dia diem: " + meetingDetail.getRoomName();
            case 3:
                content = "(LICH HOP) D/c " + empCode + "_" + fullName + " da HUY duyet cuoc hop: " + meetingDetail.getSubject()
                        + ". Thoi gian: " + meetingDetail.getStartHour() + ":" + meetingDetail.getStartMinute();
                if (null != meetingDetail.getTypeEducate()
                        && !meetingDetail.getEndDay().equals(meetingDetail.getDate())) {
                    content += " ngay " + meetingDetail.getEndDay();
                }
                content += " den " + meetingDetail.getEndHour() + ":" + meetingDetail.getEndMinute()
                        + " ngay " + meetingDetail.getDate()
                        + ". Ly do: " + meetingDetail.getReason();
            case 4:
                content = "(LICH HOP) D/c " + empCode + "_" + fullName + " da TU CHOI duyet cuoc hop: " + meetingDetail.getSubject()
                        + ". Thoi gian: " + meetingDetail.getStartHour() + ":" + meetingDetail.getStartMinute();
                if (null != meetingDetail.getTypeEducate()
                        && !meetingDetail.getEndDay().equals(meetingDetail.getDate())) {
                    content += " ngay " + meetingDetail.getEndDay();
                }
                content += " den " + meetingDetail.getEndHour() + ":" + meetingDetail.getEndMinute()
                        + " ngay " + meetingDetail.getDate()
                        + ". Ly do: " + meetingDetail.getReason();
        }
        // Check them neu la lich dao tao end
        return content;
    }

    public String sendSMS(HttpServletRequest request, String data) {
        String[] keys = new String[]{
            ConstantsFieldParams.MEETING_ID,
            ConstantsFieldParams.COMMENT
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("sendSMS [gửi sms] - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            // Parse du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            String strMeetingId = listValue.get(0);
            String comment = listValue.get(1);

            Long meetingId = null;
            try {
                if (!CommonUtils.isEmpty(strMeetingId)) {
                    meetingId = Long.parseLong(strMeetingId);
                }
            } catch (NumberFormatException nfe) {
                LOGGER.error("sendSMS [gửi sms] - Wrong value meetingId: " + strMeetingId);
            }

            MeetingWeekDAO meetwDAO = new MeetingWeekDAO();
            MeetingApproveResult meeting = meetwDAO.getMeetingDetail(userGroup, meetingId);
            if (meeting != null) {
                List<EntityMeetingMember> lstMember = meetwDAO.getAllMemberInMeeting(meetingId);
                List<Long> memberIds = new ArrayList<>();
                List<Long> orgIds = new ArrayList<>();
                Map<Long, EntityMeetingMember> mapOrg = new HashMap<>();
                for (EntityMeetingMember mem : lstMember) {
                    if (mem.getType() == null) {
                        continue;
                    }
                    if (mem.getType() == 0 && mem.getEmployeeId() != null) {
                        memberIds.add(mem.getEmployeeId());
                        meetwDAO.sendSmsUserMemberAssign(meeting, mem, userGroup, comment);
                    } else if (mem.getType() == 1 && mem.getOrgId() != null) {
                        orgIds.add(mem.getOrgId());
                        mapOrg.put(mem.getOrgId(), mem);
                    }
                    meetwDAO.updateMeetingMember(mem.getNumberReceiveSms(), mem.getId());
                }
                if (!CommonUtils.isEmpty(orgIds)) {
                    List<EntityVhrEmployee> lstEmp = meetwDAO.getListEmpWithPermissionApproval(orgIds, Arrays.asList(new String[]{"TTDV", "LDDV"}));
                    List<Long> listUserId = new ArrayList<>();
                    if (!CommonUtils.isEmpty(lstEmp)) {
                        Map<Long, List<Long>> mapConfig = meetwDAO.getUsersNoReceiveNotify(null);
                        List<Long> userIds = null;
                        for (EntityVhrEmployee member : lstEmp) {
                            userIds = mapConfig.get(member.getSysOrganizationId());
                            if (!listUserId.contains(member.getEmployeeId()) && (userIds == null || !userIds.contains(member.getEmployeeId()))) {
                                listUserId.add(member.getEmployeeId());
                                meetwDAO.sendSmsOrgMemberAssign(meeting, mapOrg.get(member.getSysOrganizationId()), userGroup, member.getAbbreviation(), comment, member);
                            }
                        }
                    }

                    lstEmp = meetwDAO.getListEmpWithPermissionApproval(orgIds, Arrays.asList(new String[]{"QLLH"}));
                    if (!CommonUtils.isEmpty(lstEmp)) {
                        for (EntityVhrEmployee member : lstEmp) {
                            if (!listUserId.contains(member.getEmployeeId())) {
                                listUserId.add(member.getEmployeeId());
                                meetwDAO.sendSmsOrgMemberAssign(meeting, mapOrg.get(member.getSysOrganizationId()), userGroup, member.getAbbreviation(), comment, member);
                            }
                        }
                    }
                }
                if (!CommonUtils.isEmpty(memberIds)) {
                    List<EntityMeetingMember> listUser = meetwDAO.getMeetingMemberAssistant(memberIds, meetingId);
                    if (!CommonUtils.isEmpty(listUser)) {
                        for (EntityMeetingMember member : listUser) {
                            meetwDAO.sendSmsUserMemberAssign(meeting, member, userGroup, comment);
                        }
                    }
                }
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, 1, userGroup);

        } catch (Exception e) {
            LOGGER.error("sendSMS [gửi sms] - fail!");
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    public String sendMail(HttpServletRequest request, String data) {
        String[] keys = new String[]{
            ConstantsFieldParams.MEETING_ID,
            ConstantsFieldParams.COMMENT
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("sendMail [gửi mail] - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            // Parse du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            String strMeetingId = listValue.get(0);
            String comment = listValue.get(1);

            Long meetingId = null;
            try {
                if (!CommonUtils.isEmpty(strMeetingId)) {
                    meetingId = Long.parseLong(strMeetingId);
                }
            } catch (NumberFormatException nfe) {
                LOGGER.error("sendMail [gửi mail] - Wrong value meetingId: " + strMeetingId);
            }

            MeetingWeekDAO meetingWeekDAO = new MeetingWeekDAO();
            MeetingApproveResult meeting = meetingWeekDAO.getMeetingDetail(userGroup, meetingId);
            if (meeting != null) {
                List<EntityMeetingMember> lstMember = meetingWeekDAO.getAllMemberInMeeting(meetingId);
                if (!CommonUtils.isEmpty(lstMember)) {
                    meetingWeekDAO.sendRequestEmail(lstMember, meeting, comment, userGroup, "REQUEST", "LHCAL-01", false, true, false);
                }
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, 1, userGroup);

        } catch (Exception e) {
            LOGGER.error("sendMail [gửi mail] - fail!");
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    public String checkConflictTimeUsedRoom(HttpServletRequest request, String data) {
        String[] keys = new String[]{
            "meetingId",
            "roomId",};
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("checkConflictTimeUsedRoom [check trung thoi gian su dung phong hop] - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            // Parse du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            String strMeetingId = listValue.get(0);
            String strRoomId = listValue.get(1);
            Long roomId = null;
            Long meetingId = null;
            try {
                if (!CommonUtils.isEmpty(strRoomId)) {
                    roomId = Long.parseLong(strRoomId);
                }
                if (!CommonUtils.isEmpty(strMeetingId)) {
                    meetingId = Long.parseLong(strMeetingId);
                }
            } catch (NumberFormatException nfe) {
                LOGGER.error("checkConflictTimeUsedRoom [thay doi phong hop] - Wrong value number: " + nfe.toString());
            }
            MeetingWeekDAO meetingWeekDAO = new MeetingWeekDAO();
            MeetingApproveResult meetingDetail = meetingWeekDAO.getMeetingDetail(userGroup, meetingId);
            if (meetingDetail != null) {
                Long checkConflictRoom = meetingWeekDAO.checkConflictTimeUsedRoom(meetingId, Arrays.asList(roomId),
                        meetingDetail.getStartTime(), meetingDetail.getEndTime());
                if (checkConflictRoom != null && checkConflictRoom.intValue() == 1) {
                    return FunctionCommon.responseResult(ErrorCode.SUCCESS, Constants.MEETING.MESSAGE.CONFLICT_ROOM, userGroup);
                }
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, 1, userGroup);
        } catch (Exception ex) {
            LOGGER.error("checkConflictTimeUsedRoom [check trung thoi gian su dung phong hop] - fail!");
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    public String checkDuplicateRoomVideoConference(HttpServletRequest request, String data) {
        String[] keys = new String[]{
            "meetingId"
        };
        Long result = null;
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("checkDuplicateMeetingVideoConference [check trung phong hop voi CTH]"
                    + " - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            // Parse du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            String strMeetingId = listValue.get(0);
            Long meetingId = null;
            try {
                if (!CommonUtils.isEmpty(strMeetingId)) {
                    meetingId = Long.parseLong(strMeetingId);
                }
            } catch (NumberFormatException nfe) {
                LOGGER.error("checkDuplicateRoomVideoConference [check trung phong hop voi CTH]"
                        + " - Wrong value number: " + nfe.toString());
            }
            MeetingWeekDAO meetWeekDAO = new MeetingWeekDAO();
            MeetingApproveResult meetingDetail = meetWeekDAO.getMeetingDetail(userGroup, meetingId);
            result = meetWeekDAO.checkDuplicateMeetingVideoConference(meetingId, Arrays.asList(meetingDetail.getMeetingResourceId()),
                    meetingDetail.getStartTime(), meetingDetail.getEndTime());
            if (result > 0L) {
                return FunctionCommon.responseResult(ErrorCode.SUCCESS, Constants.MEETING.MESSAGE.CONFLICT_VIDEO_CONFERENCE, userGroup);
            } else {
                return FunctionCommon.responseResult(ErrorCode.SUCCESS, 1, userGroup);
            }
        } catch (Exception ex) {
            LOGGER.error("checkDuplicateRoomVideoConference [check trung phong hop voi CTH]"
                    + " - fail!");
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    public String checkDuplicateVideoConferenceRoom(HttpServletRequest request, String data) {
        String[] keys = new String[]{
            "meetingId",};
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("checkDuplicateVideoConferenceRoom [check trung CTH voi phong hop] - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            // Parse du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            String strMeetingId = listValue.get(0);
            Long meetingId = null;
            try {
                if (!CommonUtils.isEmpty(strMeetingId)) {
                    meetingId = Long.parseLong(strMeetingId);
                }
            } catch (NumberFormatException nfe) {
                LOGGER.error("checkDuplicateVideoConferenceRoom [check trung CTH voi phong hop] - Wrong value number: " + nfe.toString());
            }
            MeetingWeekDAO meetingWeekDAO = new MeetingWeekDAO();
            MeetingApproveResult meetingDetail = meetingWeekDAO.getMeetingDetail(userGroup, meetingId);
            if (meetingDetail != null) {
                List<EntityMeetingResource> videoConferencingList = meetingWeekDAO.getVideoConferencingList(userGroup.getUserId2(), meetingId);
                if (!CommonUtils.isEmpty(videoConferencingList)) {
                    List<Long> ids = new ArrayList<>();
                    for (EntityMeetingResource obj : videoConferencingList) {
                        ids.add(obj.getMeetingResourceId());
                    }
                    Long checkConflictRoom = meetingWeekDAO.checkConflictTimeUsedRoom(meetingId, ids,
                            meetingDetail.getStartTime(), meetingDetail.getEndTime());
                    if (checkConflictRoom != null && checkConflictRoom.intValue() == 1) {
                        return FunctionCommon.responseResult(ErrorCode.SUCCESS, Constants.MEETING.MESSAGE.CONFLICT_ROOM, userGroup);
                    }
                }
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, 1, userGroup);
        } catch (Exception ex) {
            LOGGER.error("checkDuplicateVideoConferenceRoom [check trung CTH voi phong hop] - fail!");
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    public String checkDuplicateVideoConference(HttpServletRequest request, String data) {
        String[] keys = new String[]{
            "meetingId"
        };
        Long result = null;
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("checkDuplicateVideoConference [check trung CTH voi CTH]"
                    + " - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            // Parse du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            String strMeetingId = listValue.get(0);
            Long meetingId = null;
            try {
                if (!CommonUtils.isEmpty(strMeetingId)) {
                    meetingId = Long.parseLong(strMeetingId);
                }
            } catch (NumberFormatException nfe) {
                LOGGER.error("checkDuplicateVideoConference [check trung CTH voi CTH]"
                        + " - Wrong value number: " + nfe.toString());
            }
            MeetingWeekDAO meetWeekDAO = new MeetingWeekDAO();
            MeetingApproveResult meetingDetail = meetWeekDAO.getMeetingDetail(userGroup, meetingId);
            List<EntityMeetingResource> videoConferencingList = meetWeekDAO.getVideoConferencingList(userGroup.getUserId2(), meetingId);
            if (!CommonUtils.isEmpty(videoConferencingList)) {
                List<Long> ids = new ArrayList<>();
                for (EntityMeetingResource obj : videoConferencingList) {
                    ids.add(obj.getMeetingResourceId());
                }
                result = meetWeekDAO.checkDuplicateMeetingVideoConference(meetingId, ids, meetingDetail.getStartTime(), meetingDetail.getEndTime());
                if (result > 0L) {
                    return FunctionCommon.responseResult(ErrorCode.SUCCESS, Constants.MEETING.MESSAGE.CONFLICT_VIDEO_CONFERENCE, userGroup);
                }
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, 1, userGroup);
        } catch (Exception ex) {
            LOGGER.error("checkDuplicateVideoConference [check trung CTH voi CTH]"
                    + " - fail!");
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * Kiem tra truoc khi thay doi phong hop
     *
     * @param request
     * @param data
     * @return
     */
    public String checkChangeOrgApproval(HttpServletRequest request, String data) {
        String[] keys = new String[]{
            "meetingId",
            "roomId"
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("checkChangeOrgApproval [Kiem tra truoc khi thay doi phong hop] - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            String strMeetingid = listValue.get(0);
            String strNewRoomId = listValue.get(1);
            Long meetingId = Long.parseLong(strMeetingid);
            Long newRoomId = Long.parseLong(strNewRoomId);

            Long orgIdOld = null;
            Long orgIdNew = null;
            MeetingWeekDAO meetingWeekDAO = new MeetingWeekDAO();
            MeetingApproveResult meetingDetail = meetingWeekDAO.getMeetingDetail(userGroup, meetingId);
            if (meetingDetail != null) {
                if (meetingDetail.getStatus() != null && meetingDetail.getStatus().intValue() == 2) {
                    // Kiem tra thay doi don vi duyet lich
                    orgIdOld = meetingDetail.getOrgApprovalId();
                    orgIdNew = findOrgApproval(meetingDetail, newRoomId, userGroup.getUserId2());
                    if (orgIdNew != null && !orgIdNew.equals(orgIdOld)) {
                        return FunctionCommon.responseResult(ErrorCode.SUCCESS, Constants.MEETING.MESSAGE.CHANGE_ORG_APPROVAL, userGroup);
                    }
                }
                return FunctionCommon.responseResult(ErrorCode.SUCCESS, 1, userGroup);
            } else {
                return FunctionCommon.responseResult(ErrorCode.ERR_NODATA, null, null);
            }
        } catch (Exception ex) {
            LOGGER.error("checkChangeOrgApproval [Kiem tra truoc khi thay doi phong hop] - fail!", ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    public String checkDuplicateParticant(HttpServletRequest request, String data) {
        String[] keys = new String[]{
            "meetingId"
        };
        Long result = null;
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("checkDuplicateParticant [check trung thanh phan tham gia]"
                    + " - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            // Parse du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            String strMeetingId = listValue.get(0);

            Long meetingId;
            try {
                if (!CommonUtils.isEmpty(strMeetingId)) {
                    meetingId = Long.parseLong(strMeetingId);
                } else {
                    LOGGER.error("checkDuplicateParticant [check trung thanh phan tham gia]"
                            + " - meetingId null!");
                    return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
                }
            } catch (Exception ex) {
                LOGGER.error("checkDuplicateParticant [check trung thanh phan tham gia]"
                        + " Wrong value meetingId: " + strMeetingId);
                return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
            }
            List<Long> lstMemberId = new ArrayList<>();

            MeetingWeekDAO meetWeekDAO = new MeetingWeekDAO();
            MeetingApproveResult meeting = meetWeekDAO.getMeetingDetail(userGroup, meetingId);
            if (meeting != null) {
                List<EntityMeetingMember> lstMember = meetWeekDAO.getAllMemberInMeeting(meetingId);
                for (EntityMeetingMember member : lstMember) {
                    lstMemberId.add(member.getId());
                }
                result = meetWeekDAO.checkDuplicateParticant(lstMemberId, meeting.getStartTime(), meeting.getEndTime(), meetingId);
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            LOGGER.error("checkDuplicateParticant [check trung thanh phan tham gia]"
                    + " - fail!");
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    public String validateSaveMeeting(HttpServletRequest request, String data) {
        String[] keys = new String[]{
            "meetingId",
            "roomId"
        };
        Long result = null;
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("validateSaveMeeting [check lich hop] - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            Long meetingId = Long.parseLong(listValue.get(0));
            Long roomId = null;
            if (!CommonUtils.isEmpty(listValue.get(1))) {
                roomId = Long.parseLong(listValue.get(1));
            }
            MeetingWeekDAO meetWeekDAO = new MeetingWeekDAO();
            MeetingApproveResult meetingDetail = meetWeekDAO.getMeetingDetail(userGroup, meetingId);
            if (meetingDetail != null) {
                boolean check = true;
                List<Integer> results = new ArrayList<>();
                if (roomId != null) {
                    // check doi phong hop co cau truyen hinh
                    if (roomId != meetingDetail.getMeetingResourceId() && meetingDetail.getMeetingResourceId() != null
                            && meetingDetail.getHasVideoConf().intValue() == 1) {
                        EntityMeetingResource room = meetWeekDAO.getDetailMeetingResource(roomId);
                        if (room == null || room.getHasVideoConference() == null || room.getHasVideoConference().intValue() != 1) {
                            results.add(Constants.MEETING.MESSAGE.ROOM_HAS_NOT_VIDEO_CONFERENCE);
                            check = false;
                        }
                    }
                    // check trung phong hop voi phong hop
                    if (check) {
                        result = meetWeekDAO.checkConflictTimeUsedRoom(meetingId, Arrays.asList(roomId),
                                meetingDetail.getStartTime(), meetingDetail.getEndTime());
                        if (result > 0L) {
                            results.add(Constants.MEETING.MESSAGE.CONFLICT_ROOM);
                            check = false;
                        }
                    }
                    // check trung phong hop voi CTH
                    if (check) {
                        result = meetWeekDAO.checkDuplicateMeetingVideoConference(meetingId, Arrays.asList(roomId),
                                meetingDetail.getStartTime(), meetingDetail.getEndTime());
                        if (result > 0L) {
                            results.add(Constants.MEETING.MESSAGE.CONFLICT_ROOM_VIDEO_CONFERENCE);
                        }
                    }
                }
                if (check) {
                    // check trung thanh phan tham gia
                    List<Long> lstMemberId = new ArrayList<>();
                    List<EntityMeetingMember> lstMember = meetWeekDAO.getAllMemberInMeeting(meetingId);
                    if (!CommonUtils.isEmpty(lstMember)) {
                        for (EntityMeetingMember member : lstMember) {
                            lstMemberId.add(member.getMemberId());
                        }
                        result = meetWeekDAO.checkDuplicateParticant(lstMemberId, meetingDetail.getStartTime(), meetingDetail.getEndTime(), meetingId);
                        if (result > 0L) {
                            results.add(Constants.MEETING.MESSAGE.CONFLICT_PARTICANT);
                        }
                    }
                    // check trung CTH
                    if (meetingDetail.getHasVideoConf() != null && meetingDetail.getHasVideoConf().intValue() == 1) {
                        List<EntityMeetingResource> videoConferencingList = meetWeekDAO.getVideoConferencingList(userGroup.getUserId2(), meetingId);
                        if (!CommonUtils.isEmpty(videoConferencingList)) {
                            List<Long> ids = new ArrayList<>();
                            for (EntityMeetingResource obj : videoConferencingList) {
                                ids.add(obj.getMeetingResourceId());
                            }
                            // check trung CTH voi CTH
                            result = meetWeekDAO.checkDuplicateMeetingVideoConference(meetingId, ids, meetingDetail.getStartTime(), meetingDetail.getEndTime());
                            if (result > 0L) {
                                results.add(Constants.MEETING.MESSAGE.CONFLICT_VIDEO_CONFERENCE);
                            }
                            // check trung CTH voi phong hop
                            result = meetWeekDAO.checkConflictTimeUsedRoom(meetingId, ids, meetingDetail.getStartTime(), meetingDetail.getEndTime());
                            if (result > 0L) {
                                results.add(Constants.MEETING.MESSAGE.CONFLICT_VIDEO_CONFERENCE_ROOM);
                            }
                        }
                    }
                    // Kiem tra thay doi don vi duyet lich
                    if (roomId != null && meetingDetail.getStatus() != null && meetingDetail.getStatus().intValue() == 2) {
                        Long orgIdOld = meetingDetail.getOrgApprovalId();
                        Long orgIdNew = findOrgApproval(meetingDetail, roomId, userGroup.getUserId2());
                        if (orgIdNew != null && !orgIdNew.equals(orgIdOld)) {
                            results.add(Constants.MEETING.MESSAGE.CHANGE_ORG_APPROVAL);
                        }
                    }
                }
                if (CommonUtils.isEmpty(results)) {
                    if (meetingDetail.getStatus() != null && meetingDetail.getStatus().intValue() == 2
                            && meetingDetail.getSendEmailStatus() != null) {
                        results.add(Constants.MEETING.MESSAGE.REASON_IS_NULL);
                    } else {
                        results.add(1);
                    }
                } else {
                    if (meetingDetail.getStatus() != null && meetingDetail.getStatus().intValue() == 2
                            && meetingDetail.getSendEmailStatus() != null) {
                        results.add(Constants.MEETING.MESSAGE.REASON_IS_NULL);
                    }
                }
                return FunctionCommon.responseResult(ErrorCode.SUCCESS, results, userGroup);
            } else {
                return FunctionCommon.responseResult(ErrorCode.ERR_NODATA, null, userGroup);
            }
        } catch (Exception ex) {
            LOGGER.error("validateSaveMeeting [check lich hop] - fail!");
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    private Long findOrgApproval(MeetingApproveResult meetingDetail, Long roomId, Long userId) {
        Long orgApprovalId = null;

        MeetingWeekDAO meetingWeekDAO = new MeetingWeekDAO();
        UserDAO userDAO = new UserDAO();
        EntityMeetingResource meetingResource = new EntityMeetingResource();
        if (roomId != null) {
            meetingResource = meetingWeekDAO.getDetailMeetingResource(roomId);
        }
        List<EntityMeetingMember> lstMeetingMember = meetingWeekDAO.getAllMemberInMeeting(meetingDetail.getMeetingId());
        EntityVhrEmployee userLogin = userDAO.getEmployeeById(userId);
        if (meetingDetail.getCreatorId() != null) {
            EntityVhrEmployee creatorMeeting = userDAO.getEmployeeById(meetingDetail.getCreatorId());
            // TÀI NGUYÊN KHÔNG THUỘC QUẢN LÝ
            if (roomId == null) {
                // THE OTHER PRESIDENT IS EMPTY
                // KHI KHONG MOI THANH VIEN NAO CHI MOI THANH PHAN THAM GIA KHAC
                if (CommonUtils.isEmpty(lstMeetingMember)) {
                    // DON VI BOOK LICH LEVEL NHO HON DON DON VI CAP TAI NGUYEN

                }
                // CHECK THANH PHAN THAM GIA CO BAN GIAM DOC TAP DOAN
                List<EntityVhrOrg> orgsPatitcipant = getOrgPaticipant(lstMeetingMember);
                orgApprovalId = checkParticipantInDirectorGroup(orgsPatitcipant);
                if (orgApprovalId != null) {
                    return orgApprovalId;
                }
                // CHECK THANH PHAN THAM GIA CO THUOC CHI NHANH VTT
                orgApprovalId = checkParticipantInBranchVTT(orgsPatitcipant, lstMeetingMember, meetingDetail, meetingResource, userLogin);
                if (orgApprovalId != null) {
                    return orgApprovalId;
                }

                EntityVhrOrg maxOrgLevelOrg1 = orgsPatitcipant.get(0);
                if (orgsPatitcipant.size() > 1) {
                    // maxOrgLevelOrg2 = user2.getVhrOrg();
                    orgApprovalId = findOrgAppNoResource(orgsPatitcipant, maxOrgLevelOrg1, lstMeetingMember, creatorMeeting, userLogin);
                    return orgApprovalId;
                } else {
                    orgApprovalId = creatorMeeting.getSysOrganizationId();
                    return orgApprovalId;
                }
                // TÀI NGUYÊN THUỘC QUẢN LÝ
            } else {
                // KHI KHONG MOI THANH VIEN NAO CHI MOI THANH PHAN THAM GIA KHAC
                if (CommonUtils.isEmpty(lstMeetingMember)) {
                    // PHÒNG HỌP TRÊN CROWNE
                    orgApprovalId = checkRoomInCrowne(meetingResource);
                    if (orgApprovalId != null) {
                        return orgApprovalId;
                    }
                    orgApprovalId = meetingResource.getSysOrgId();
                    return orgApprovalId;
                } // CO MOI THANH PHAN THAM GIA
                else {
                    List<EntityVhrOrg> orgsPatitcipant = getOrgPaticipant(lstMeetingMember);
                    // CHECK CHO TH BUG CO THANH PHAN THAM GIA KO O DON VI NAO
                    if (CommonUtils.isEmpty(orgsPatitcipant)) {
                        return -1l;
                    }
                    // LAY BAN GIAM DOC
                    orgApprovalId = checkParticipantInDirectorGroup(orgsPatitcipant);
                    if (orgApprovalId != null) {
                        return orgApprovalId;
                        // TAI NGUYEN TREN CROWN
                    }

                    // CHECK THANH PHAN THAM GIA CO THUOC CHI NHANH VTT
                    orgApprovalId = checkParticipantInBranchVTT(orgsPatitcipant, lstMeetingMember, meetingDetail, meetingResource, userLogin);
                    if (orgApprovalId != null) {
                        return orgApprovalId;
                    }

                    // PHÒNG HỌP TRÊN CROWNE
                    orgApprovalId = checkRoomInCrowne(meetingResource);
                    if (orgApprovalId != null) {
                        return orgApprovalId;
                    }
                    /*
                     * // DON VI CHUA TAI NGUYEN DUYET approvalOrgId =
                     * dataSelected.getMeetingResource().getSysOrgId(); return
                     * approvalOrgId;
                     */
                    // CHECK DIEU KIEN TIM DK DUYET THEO LEVEL
                    EntityVhrOrg maxOrgLevelOrg1 = orgsPatitcipant.get(0);
                    if (orgsPatitcipant.size() > 1) {
                        // maxOrgLevelOrg2 = user2.getVhrOrg();
                        orgApprovalId = findOrgAppHasResource(orgsPatitcipant, maxOrgLevelOrg1, meetingResource, lstMeetingMember, creatorMeeting, userLogin);
                        return orgApprovalId;
                    } else {
                        orgApprovalId = meetingResource.getSysOrgId();
                        return orgApprovalId;
                    }
                }

            }

        }

        return orgApprovalId;
    }

    /**
     * LAY DON VI TU THANH PHAN THAM GIA
     *
     * @param lstAttend
     * @return
     */
    private List<EntityVhrOrg> getOrgPaticipant(List<EntityMeetingMember> lstAttend) {

        MeetingWeekDAO meetingWeekDAO = new MeetingWeekDAO();
        OrgDAO orgDAO = new OrgDAO();

        List<Long> lstEmployeeId = new ArrayList<>();
        List<Long> lstOrgId = new ArrayList<>();

        List<EntityVhrOrg> lstOrgByUserAttend = new ArrayList<EntityVhrOrg>();
        List<EntityVhrOrg> lstOrgAttendAll = new ArrayList<EntityVhrOrg>();
        for (int i = 0; i < lstAttend.size(); i++) {
            //doi tuong tham gia la ca nhan thi type = 0
            if (lstAttend.get(i).getType().equals(0)) {
                lstEmployeeId.add(lstAttend.get(i).getMemberId());
            } else {
                lstOrgId.add(lstAttend.get(i).getVhrOrgId());
            }
        }
        // LAY DOI TUONG USERROLE CUA THANH PHAN THAM GIA LA CA NHAN
        if (!CommonUtils.isEmpty(lstEmployeeId)) {
            List<EntityVhrEmployee> lstEmployee = meetingWeekDAO.getInfoMemberMeeting(lstEmployeeId);
            // LOC DANH SACH TRUNG LEVEL DON VI MOI THAM GIA
            List<Long> orgIdList = new ArrayList<Long>();

            for (EntityVhrEmployee empl : lstEmployee) {
                if (empl.getSysOrganizationId() != null) {
                    if (!orgIdList.contains(empl.getSysOrganizationId())) {
                        EntityVhrOrg org = orgDAO.getOrganizationById(empl.getSysOrganizationId());
                        if (org != null) {
                            lstOrgByUserAttend.add(org);
                            orgIdList.add(empl.getSysOrganizationId());
                        }
                    }
                }
            }
        }

        // LAY DOI TUONG VHRORG DON VI DC MOI THAM GIA
        List<EntityVhrOrg> lstOrgAttend = new ArrayList<EntityVhrOrg>();

        if (!CommonUtils.isEmpty(lstOrgId)) {
            lstOrgAttend = orgDAO.getOrgById(lstOrgId);
        }
        if (!CommonUtils.isEmpty(lstOrgAttend)) {
            lstOrgAttendAll.addAll(lstOrgAttend);
        }
        if (!CommonUtils.isEmpty(lstOrgByUserAttend)) {
            for (EntityVhrOrg organization : lstOrgByUserAttend) {
                if (!lstOrgAttendAll.contains(organization)) {
                    lstOrgAttendAll.add(organization);
                }
            }
        }
        return lstOrgAttendAll;
    }

    /**
     * KIEM TRA THANH PHAN THAM GIA CO BAN TONG GIAM DOC
     *
     * @param orgsPatitcipant
     * @return
     */
    private Long checkParticipantInDirectorGroup(List<EntityVhrOrg> orgsPatitcipant) {
        if (CommonUtils.isEmpty(orgsPatitcipant)) {
            return null;
        }
        for (EntityVhrOrg org : orgsPatitcipant) {
            if (Constants.MEETING.GROUP_ROLE.IS_GROUP.equalsIgnoreCase(org.getCode())) {
                return Constants.VHR_ORG.ID.GDTD;
            }
        }
        // NEU CO BAN GIAM DOC TAO DOAN (MOI CA THEO DON VI)

        List<String> ordIdDirectGroup = new ArrayList<>();
        String[] arrayOrg = Constants.VHR_ORG.ID.DIRECTOR_GROUP_ID.split(",");
        for (String orgId : arrayOrg) {
            ordIdDirectGroup.add(orgId);
        }
        if (!CommonUtils.isEmpty(ordIdDirectGroup)) {
            for (EntityVhrOrg orgDir : orgsPatitcipant) {
                if (ordIdDirectGroup.contains(orgDir.getSysOrganizationId().toString())) {
                    return Long.valueOf(ordIdDirectGroup.get(0));
                }
            }
        }
        return null;
    }

    // KIEM TRA CHI NHANH VTT
    private Long checkParticipantInBranchVTT(List<EntityVhrOrg> orgsPatitcipant, List<EntityMeetingMember> members, MeetingApproveResult meetingDetail, EntityMeetingResource meetingResource, EntityVhrEmployee userLogin) {
        MeetingWeekDAO meetingWeekDAO = new MeetingWeekDAO();
        if (CommonUtils.isEmpty(orgsPatitcipant)) {
            return null;
        }
        // LAY ID DON VI THAM GIA LICH HOP
        List<Long> orgParIds = new ArrayList<Long>();
        for (EntityVhrOrg org : orgsPatitcipant) {
            if (!orgParIds.contains(org.getSysOrganizationId())) {
                orgParIds.add(org.getSysOrganizationId());
            }
        }
        Long orgIdTVT = Constants.VHR_ORG.ID.TVT;
        Long orgIdTVTBranch = Constants.VHR_ORG.ID.TVT_BRANCH;
        List<Long> checkOrgIdTVTBranch = meetingWeekDAO.checkOrgInSysOrg(orgParIds, orgIdTVTBranch);
        if (!CommonUtils.isEmpty(checkOrgIdTVTBranch)) {
            if (checkOrgIdTVTBranch.size() == orgParIds.size()) {
                if (meetingDetail.getMeetingResourceId() != null) {
                    Long orgId = checkResourceInPaticipant(orgsPatitcipant, meetingResource.getSysOrgId());
                    if (meetingResource.getSysOrgId().equals(orgId)) {
                        return orgId;
                    } else {
                        /*
                         * for (MeetingMember mm : members) { if
                         * (mm.getIsPresident()) return mm.getOrgId(); }
                         */
                        return meetingResource.getSysOrgId();
                    }
                } else {
                    for (EntityMeetingMember mm : members) {
                        if (mm.getIsPresident() != 1) {
                            return mm.getVhrOrgId();
                        }
                    }
                    return userLogin.getSysOrganizationId();
                }
            } else {
                return orgIdTVT;
            }
        }
        return null;
    }

    /**
     * KIEM TRA XEM TAI NGUYEN THUOC DON VI THAM GIA HAY KO
     *
     * @param lstOrgAttendAll
     * @param orgIdResource
     * @return
     */
    private Long checkResourceInPaticipant(List<EntityVhrOrg> lstOrgAttendAll, Long orgIdResource) {
        if (CommonUtils.isEmpty(lstOrgAttendAll) || (orgIdResource == null)) {
            return null;
        }
        if (!CommonUtils.isEmpty(lstOrgAttendAll)) {
            for (EntityVhrOrg org : lstOrgAttendAll) {
                if (orgIdResource.equals(org.getSysOrganizationId())) {
                    return orgIdResource;
                }
            }
            return null;
        }
        return null;
    }

    // TAI NGUYEN KO QUAN LY
    private Long findOrgAppNoResource(List<EntityVhrOrg> orgsPart, EntityVhrOrg orgFirstMax, List<EntityMeetingMember> lstMeetingMember, EntityVhrEmployee creatorMeeting, EntityVhrEmployee userLogin) {
        EntityVhrOrg orgEndInList = orgsPart.get(orgsPart.size() - 1);
        Integer levelFirstMax = Integer.parseInt(orgFirstMax.getOrgLevel());
        Integer levelEndInList = Integer.parseInt(orgEndInList.getOrgLevel());
        // BOOK LICH VUOT CAP
        if (levelFirstMax < levelEndInList) {
            // CO DON VI MOI VUOT CAP
            if (orgsPart.size() > 2) {
                EntityVhrOrg maxOrgLevelOrg2 = orgsPart.get(1);
                Integer levelOrgLevelOrg2 = Integer.parseInt(maxOrgLevelOrg2.getOrgLevel());
                if (levelFirstMax < levelOrgLevelOrg2) {
                    // KIEM TRA DON VI LEVEL CAO NHAT THI DC
                    // DUYET LICH
                    return orgFirstMax.getSysOrganizationId();
                } else if (orgFirstMax.getOrgLevel().equals(maxOrgLevelOrg2.getOrgLevel())) {
                    // DON VI CHU TRI DUYET
                    for (EntityMeetingMember mm : lstMeetingMember) {
                        if ((mm.getIsPresident() != null) && mm.getIsPresident() == 1) {
                            return mm.getVhrOrgId();
                        }
                    }
                    // NEU CHU TRI KHAC THI DON VI DAT LICH DUYET
                    return creatorMeeting.getSysOrganizationId();
                }
            }
            // TRUONG HOP CHI CO 1 DON VI CAO NHAT VA 1 DON
            // VI THAP NHAT THI LAY DON VI DAU TIEN
            return orgFirstMax.getSysOrganizationId();

        } // BOOK NGANG CAP
        else {
            // LAY DON VI CHUA TAI NGUYEN PHONG HOP DC DUYET
            // LICH
            // DON VI CHU TRI DUYET
            for (EntityMeetingMember mm : lstMeetingMember) {
                if ((mm.getIsPresident() != null) && mm.getIsPresident() == 1) {
                    return mm.getVhrOrgId();
                }
            }
            // NEU CHU TRI KHAC THI DON VI DAT LICH DUYET
            return userLogin.getSysOrganizationId();
        }
    }

    /**
     * CHECK TAI NGUYEN TREN CROWNE
     *
     * @param meeting
     * @return
     */
    private Long checkRoomInCrowne(EntityMeetingResource meetingResource) {
        OrgDAO orgDAO = new OrgDAO();
        MeetingWeekDAO meetingWeekDAO = new MeetingWeekDAO();
        List<EntityVhrOrg> organization = orgDAO.getListOrgByCode(Constants.MEETING.GROUP_ROLE.IS_VGO);
        Long orgIdVIG = organization.get(0).getSysOrganizationId();
        if (meetingWeekDAO.getResoureInCrowne(orgIdVIG, meetingResource.getMeetingResourceId())) {
            return meetingResource.getSysOrgId();
        }
        return null;
    }

    // TAI NGUYEN THUOC QUAN LY
    private Long findOrgAppHasResource(List<EntityVhrOrg> orgsPart, EntityVhrOrg orgFirstMax, EntityMeetingResource meetingResource, List<EntityMeetingMember> lstMeetingMember, EntityVhrEmployee creatorMeeting, EntityVhrEmployee userLogin) {
        Long checkResourceId = checkResourceInPaticipant(orgsPart, meetingResource.getSysOrgId());
        if (checkResourceId != null) {
            // TAI NGUYEN PHONG HOP THUOC DON VI THANH PHAN THAM
            // GIA
            EntityVhrOrg orgEndInList = orgsPart.get(orgsPart.size() - 1);
            Integer levelFirstMax = Integer.parseInt(orgFirstMax.getOrgLevel());
            Integer levelEndInList = Integer.parseInt(orgEndInList.getOrgLevel());
            // BOOK LICH VUOT CAP
            if (levelFirstMax < levelEndInList) {
                // CO DON VI MOI VUOT CAP
                if (orgsPart.size() > 2) {
                    EntityVhrOrg maxOrgLevelOrg2 = orgsPart.get(1);
                    Integer levelOrgLevelOrg2 = Integer.parseInt(maxOrgLevelOrg2.getOrgLevel());
                    if (levelFirstMax < levelOrgLevelOrg2) {
                        // KIEM TRA DON VI LEVEL CAO NHAT THI DC
                        // DUYET LICH
                        return orgFirstMax.getSysOrganizationId();
                    } else if (orgFirstMax.getOrgLevel().equals(maxOrgLevelOrg2.getOrgLevel())) {
                        // --> DON VI TAI NGUYEN DUYET LICH HOP
                        return meetingResource.getSysOrgId();
                    }
                }
                // TRUONG HOP CHI CO 1 DON VI CAO NHAT VA 1 DON
                // VI THAP NHAT THI LAY DON VI DAU TIEN
                return orgFirstMax.getSysOrganizationId();

            } // BOOK NGANG CAP
            else {
                // --> DON VI TAI NGUYEN DUYET LICH HOP
                return meetingResource.getSysOrgId();
            }
        }
        // NEU TAI NGUYEN KHONG THUOC DON VI THANH PHAN THAM GIA
        // --> DON VI TAI NGUYEN DUYET LICH HOP
        return meetingResource.getSysOrgId();
    }

    /**
     * <b>Lay cuoc hop cho phong hop thong minh</b>
     *
     * @param request
     * @param data
     * @return
     */
    public String getMeetingForSmartRoom(HttpServletRequest request, String data) {
        EntityLog log = new EntityLog(request, CLASS_NAME);
        String response;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        ErrorCode errorCode;
        String deviceName = null;
        Long userId = null;
        // Session khong hop le
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getMeetingForSmartRoom (Lay lich hop phong hop thong minh) - Session timeout");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        // Lay key AES -> Giai ma du lieu
        String strAesKeyDecode = userGroup.getStrAesKey();
        data = SecurityControler.decodeDataByAes(strAesKeyDecode, data);
        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            // Parse du lieu
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.TYPE,
                ConstantsFieldParams.WEEK_AMOUNT,
                ConstantsFieldParams.DEVICE_NAME,
                "getAllPresident", "meetingResourceId"
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Loai
            int type = 0;
            String strType = listValue.get(0);
            if (!CommonUtils.isEmpty(strType)) {
                type = Integer.parseInt(strType);
            }
            // So luong tuan dich chuyen
            int weekAmount = 0;
            String strWeekAmount = listValue.get(1);
            if (!CommonUtils.isEmpty(strWeekAmount)) {
                weekAmount = Integer.parseInt(strWeekAmount);
            }
            deviceName = listValue.get(2);
            userId = userGroup.getVof2_ItemEntityUser().getUserId();
            Long orgIdsByRole = userGroup.getVof2_ItemEntityUser().getAdOrgId();
            Long meetingResourceId = CommonUtils.isInteger(listValue.get(4)) ? Long.valueOf(listValue.get(4)) : null;

            MeetingWeekDAO meetingWeekDAO = new MeetingWeekDAO();
            List<MeetingApproveResult> listItem = meetingWeekDAO.getMeetingForSmartRoom(
                    userGroup, orgIdsByRole, type, weekAmount, null,
                    null, null, null, null, "", null, null, meetingResourceId);

            errorCode = ErrorCode.SUCCESS;
            response = FunctionCommon.generateResponseJSON(errorCode, listItem, strAesKeyDecode);
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error("getMeetingForSmartRoom (Lay lich hop phong hop thong minh) - Exception -"
                    + " username: " + cardId + "\ndata: " + data, ex);
            errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
            response = FunctionCommon.generateResponseJSON(errorCode, null, null);
        }
        // Ghi log database
        ActionLogMobileDAO actionLogMobileDAO = new ActionLogMobileDAO();

        String description = "SERVICE2: Lấy danh sách lịch họp - errorCode: "
                + errorCode.getErrorCode();
        actionLogMobileDAO.insert(userId, cardId, log.getStartTime(), new Date(),
                "MettingWeek.getMeetingForSmartRoom", description, log.getPath(),
                null, null, deviceName, null);
        return response;
    }

    public String getMeetingRollCall(HttpServletRequest request, String data) {
        EntityLog log = new EntityLog(request, CLASS_NAME);
        String response;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        ErrorCode errorCode;
        String deviceName = null;
        // Session khong hop le
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getMeetingRollCall - Session timeout");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        // Lay key AES -> Giai ma du lieu
        String strAesKeyDecode = userGroup.getStrAesKey();
        data = SecurityControler.decodeDataByAes(strAesKeyDecode, data);
        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            // Parse du lieu
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.WEEK_AMOUNT
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Loai
            int weekAmount = 0;
            String str = listValue.get(0);
            if (!CommonUtils.isEmpty(str)) {
                weekAmount = Integer.parseInt(str);
            }
            
            MeetingWeekDAO meetingWeekDAO = new MeetingWeekDAO();
            List<MeetingApproveResult> listItem = meetingWeekDAO.getMeetingRollCall(userGroup, weekAmount);
            errorCode = ErrorCode.SUCCESS;
            response = FunctionCommon.generateResponseJSON(errorCode, listItem, strAesKeyDecode);
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error("getMeetingRollCall - Exception -"
                    + " username: " + cardId + "\ndata: " + data, ex);
            errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
            response = FunctionCommon.generateResponseJSON(errorCode, null, null);
        }
        // Ghi log database
        ActionLogMobileDAO actionLogMobileDAO = new ActionLogMobileDAO();

        String description = "SERVICE2: Lấy danh sách lịch họp - errorCode: "
                + errorCode.getErrorCode();
        actionLogMobileDAO.insert(userGroup.getUserId2(), cardId, log.getStartTime(), new Date(),
                "MettingWeek.getMeetingRollCall", description, log.getPath(),
                null, null, deviceName, null);
        return response;
    }
    
    /**
     * 
     * @param isSecurity
     * @param data
     * @param request
     * @return
     */
    public String getNoteBookDetail(String isSecurity, String data,
            HttpServletRequest request) {
         EntityLog log = new EntityLog(request, CLASS_NAME);
         EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
         // Khong co session
         if (!userGroup.getCheckSessionOk()) {
             return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
         }
         //Id user vof2
         Long userIdVof2 = userGroup.getVof2_ItemEntityUser().getUserId();
         if (userIdVof2 == null) {
             return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
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
                 ConstantsFieldParams.MEETING_ID
             };
             List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);

             String strMeetingId = listValue.get(0);
             Long meetingId = null;
             if (strMeetingId != null && !"".equals(strMeetingId)) {
                 meetingId = Long.parseLong(strMeetingId);
             }

             MeetingWeekDAO mwDAO = new MeetingWeekDAO();
             MeetingApproveResult result = new MeetingApproveResult();

             result = mwDAO.getNoteBookDetail(userGroup, meetingId);

             LogUtils.logFunctionalEnd(log);
             return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
         } catch (JSONException | NumberFormatException ex) {
             LOGGER.error(ex.getMessage(), ex);
             return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, "", aesKey);
         }
    }

    /**
     * Lay danh sach lanh dao cua tro ly
     * 
     * @param isSecurity
     * @param data
     * @param request
     * @return
     */
    public String getLeaderIdForAssistantUser(String isSecurity, String data,
            HttpServletRequest request) {
         EntityLog log = new EntityLog(request, CLASS_NAME);
         EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
         // Khong co session
         if (!userGroup.getCheckSessionOk()) {
             return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
         }
         //Id user vof2
         Long userIdVof2 = userGroup.getVof2_ItemEntityUser().getUserId();
         if (userIdVof2 == null) {
             return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
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
                 ConstantsFieldParams.STAFF_ID
             };
             List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);

             String strStaffId = listValue.get(0);
             Long staffId = null;
             if (strStaffId != null && !"".equals(strStaffId)) {
                 staffId = Long.parseLong(strStaffId);
             }

             MeetingWeekDAO mwDAO = new MeetingWeekDAO();
             List<EntityMeetingAssistant> result = mwDAO.getLeaderIdForAssistantUser(staffId);

             LogUtils.logFunctionalEnd(log);
             return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
         } catch (JSONException | NumberFormatException ex) {
             LOGGER.error(ex);
             return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, "", aesKey);
         }
    }

    public String saveMeetingComander(HttpServletRequest request, String data) {
        EntityLog log = new EntityLog(request, CLASS_NAME);
        String response;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        ErrorCode errorCode;
        String deviceName = null;
        Long userId = null;
        // Session khong hop le
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("saveMeetingComander - Session timeout");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        // Lay key AES -> Giai ma du lieu
        String strAesKeyDecode = userGroup.getStrAesKey();
        data = SecurityControler.decodeDataByAes(strAesKeyDecode, data);
        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
        userId = userGroup.getUserId2();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            // Parse du lieu
            JSONObject json = new JSONObject(data);
            String[] keys = new String[] { "startTime", "endTime", "listSave", "orgId" };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Loai
            Date startTime = null;
            String strType = listValue.get(0);
            if (!CommonUtils.isEmpty(strType)) {
                startTime = DateUtils.string2Date(strType);
            }
            Date endTime = null;
            strType = listValue.get(1); 
            if (!CommonUtils.isEmpty(strType)) {
                endTime = DateUtils.string2Date(strType);
            }
            List<EntityMeetingCommander> listSave = new ArrayList<EntityMeetingCommander>();
            strType = listValue.get(2);
            if (!CommonUtils.isEmpty(strType)) {
                Type listType = new TypeToken<ArrayList<EntityMeetingCommander>>() {
                }.getType();
                Gson gson = new Gson();
                listSave = gson.fromJson(strType, listType);
            }
            Long orgId = null;
            strType = listValue.get(3);
            if (CommonUtils.isInteger(strType)) {
                orgId = Long.valueOf(strType);
            }
            if (startTime == null || endTime == null || orgId == null || CommonUtils.isEmpty(listSave)) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
            
            MeetingWeekDAO meetingWeekDAO = new MeetingWeekDAO();
            boolean result = meetingWeekDAO.saveMeetingComander(startTime, endTime, listSave, userId, orgId);

            errorCode = ErrorCode.SUCCESS;
            response = FunctionCommon.generateResponseJSON(errorCode, result ? 1 : 0, strAesKeyDecode);
        } catch (Exception ex) {
            LOGGER.error("saveMeetingComander - Exception -"
                    + " username: " + cardId + "\ndata: " + data, ex);
            errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
            response = FunctionCommon.generateResponseJSON(errorCode, null, null);
        }
        return response;
    }
    
    public String getListMeetingCommander(HttpServletRequest request, String data) {
        EntityLog log = new EntityLog(request, CLASS_NAME);
        String response;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        ErrorCode errorCode;
        // Session khong hop le
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getListMeetingCommander - Session timeout");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        // Lay key AES -> Giai ma du lieu
        String strAesKeyDecode = userGroup.getStrAesKey();
        data = SecurityControler.decodeDataByAes(strAesKeyDecode, data);
        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            // Parse du lieu
            JSONObject json = new JSONObject(data);
            String[] keys = new String[] { "startTime", "endTime", "orgId" };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Loai
            String startTime = listValue.get(0);
            String endTime = listValue.get(1);
            Long orgId = null;
            String strType = listValue.get(2);
            if (CommonUtils.isInteger(strType)) {
                orgId = Long.valueOf(strType);
            }
            if (CommonUtils.isEmpty(startTime) || CommonUtils.isEmpty(endTime) || orgId == null) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
            
            MeetingWeekDAO meetingWeekDAO = new MeetingWeekDAO();
            List<EntityMeetingCommander> results = meetingWeekDAO.getListMeetingCommander(startTime, endTime, orgId);
            List<EntityMeetingCommander> resultsOldData = meetingWeekDAO.getListMeetingCommanderOldData(startTime, endTime, orgId);
            if(!CommonUtils.isEmpty(resultsOldData)){
            	for(EntityMeetingCommander obj : resultsOldData){
            		results.add(obj);
            	}
            }
            errorCode = ErrorCode.SUCCESS;
            response = FunctionCommon.generateResponseJSON(errorCode, results, strAesKeyDecode);
        } catch (Exception ex) {
            LOGGER.error("getListMeetingCommander - Exception -"
                    + " username: " + cardId + "\ndata: " + data, ex);
            errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
            response = FunctionCommon.generateResponseJSON(errorCode, null, null);
        }
        return response;
    }

    /**
     * <b>Lay lich hop tuan</b><br>
     *
     * @param data
     * @param request
     * @param isSecurity
     * @return
     */
    public String getLstDocumentByLstMeeting(String data, HttpServletRequest request, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String response;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        ErrorCode errorCode;
        // Session khong hop le
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getLstDocumentByLstMeeting (Lay bao cao ket luan theo phong hop)"
                    + " - Session timeout");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        // Lay key AES -> Giai ma du lieu
        String strAesKeyDecode = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            strAesKeyDecode = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(strAesKeyDecode, data);
        }
        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            // Parse du lieu
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.MEETING_LST_DOCUMENT,
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            
            List<Long> lstMeetid = new ArrayList<Long>();
            if (!CommonUtils.isEmpty(listValue.get(0))) {
            	lstMeetid = FunctionCommon.getListIdFromString(listValue.get(0));
            }
            MeetingWeekDAO meetingWeekDAO = new MeetingWeekDAO();
            List<EntityText> listItem = meetingWeekDAO.getLstDocumentByLstMeeting(lstMeetid);
          
            errorCode = ErrorCode.SUCCESS;
            response = FunctionCommon.generateResponseJSON(errorCode, listItem, strAesKeyDecode);
        } catch (Exception ex) {
            LOGGER.error("getLstDocumentByLstMeeting (Lay bao cao ket luan theo phong hop) - Exception -"
                    + " username: " + cardId + "\ndata: " + data, ex);
            errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
            response = FunctionCommon.generateResponseJSON(errorCode, null, null);
        }
        return response;
    }
    
    public String filterMeetingTextByCreator(String data, HttpServletRequest request, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String response;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        ErrorCode errorCode;
        // Session khong hop le
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("filterMeetingTextByCreator - Session timeout");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Lay key AES -> Giai ma du lieu
        String strAesKeyDecode = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            strAesKeyDecode = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(strAesKeyDecode, data);
        }
        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            // Parse du lieu
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.TEXT_ID,
                ConstantsFieldParams.STAFF_IDS
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            
            Type typeOfListLong = new TypeToken<ArrayList<Long>>() {
            }.getType();
            Gson gson = new Gson();
            List<Long> textIds = gson.fromJson(listValue.get(0), typeOfListLong);
            List<Long> userIds = gson.fromJson(listValue.get(1), typeOfListLong);
            
            MeetingWeekDAO meetingWeekDAO = new MeetingWeekDAO();
            List<Long> listItem = meetingWeekDAO.filterMeetingTextByCreator(textIds, userIds);
          
            errorCode = ErrorCode.SUCCESS;
            response = FunctionCommon.generateResponseJSON(errorCode, listItem, strAesKeyDecode);
        } catch (Exception ex) {
            LOGGER.error("filterMeetingTextByCreator username: " + cardId + "\ndata: " + data, ex);
            errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
            response = FunctionCommon.generateResponseJSON(errorCode, null, null);
        }
        return response;
    }
    
    /**
     * @author MINHNQ
     * 
     *         check cuoc hop co ket luan chua
     * @param request
     * @param data
     * @return
     */
    public String checkUpdateWithoutConclusions(HttpServletRequest request,
            String data) {
        EntityLog log = new EntityLog(request, CLASS_NAME);
        String response;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        ErrorCode errorCode;
        Long userId = null;
        // Session khong hop le
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("checkUpdateWithoutConclusions - Session timeout");
            return FunctionCommon.generateResponseJSON(
                    userGroup.getEnumErrCode(), null, null);
        }
        // Lay key AES -> Giai ma du lieu
        String strAesKeyDecode = userGroup.getStrAesKey();
        data = SecurityControler.decodeDataByAes(strAesKeyDecode, data);
        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
        userId = userGroup.getUserId2();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            // Parse du lieu
            JSONObject json = new JSONObject(data);
            String[] keys = new String[] { "meetingId" };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json,
                    keys);
            Long meetingId = null;
            String strType = listValue.get(0);
            if (CommonUtils.isInteger(strType)) {
                meetingId = Long.valueOf(strType);
            }
            if(meetingId==null){
                LOGGER.error("checkUpdateWithoutConclusions - Exception -"
                        + "meetingId null ");
                errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
               return response = FunctionCommon.generateResponseJSON(errorCode, null,
                        null);
            }
            boolean result = false;
            MeetingWeekDAO meetingWeekDAO = new MeetingWeekDAO();
            // nguoi dang nhap co la nguoi viet ket luan
            boolean checkUser = meetingWeekDAO.checkUser(meetingId, userId);
            if (checkUser) {
                result = true;
            }
            // check nguoi dang nhap co la lanh dao quan ly hop don vi viet ket
            // luan
            EntityMeeting meeting = meetingWeekDAO
                    .getMeetingOrgNoteConclusionsById(meetingId);
            if (null != meeting) {
                if (null != meeting.getOrgNoteConclusions()) {
                    boolean checkRole = meetingWeekDAO.checkRole(
                            meeting.getOrgNoteConclusions(), userId, meetingId);
                    if (checkRole) {
                        result = true;
                    }
                }
            }
            //check thoi gian
            errorCode = ErrorCode.SUCCESS;
            response = FunctionCommon.generateResponseJSON(errorCode,
                    result ? 1 : 0, strAesKeyDecode);
        } catch (Exception ex) {
            LOGGER.error("checkUpdateWithoutConclusions - Exception -"
                    + " username: " + cardId + "\ndata: " + data, ex);
            errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
            response = FunctionCommon.generateResponseJSON(errorCode, null,
                    null);
        }
        return response;
    }

    /**
     * @author MINHNQ
     * 
     *         update cuoc hop khong ket luan
     * @param request
     * @param data
     * @return
     */
    public String updateWithoutConclusions(HttpServletRequest request,
            String data) {
        EntityLog log = new EntityLog(request, CLASS_NAME);
        String response;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        ErrorCode errorCode;
        Long userId = null;
        // Session khong hop le
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("checkUpdateWithoutConclusions - Session timeout");
            return FunctionCommon.generateResponseJSON(
                    userGroup.getEnumErrCode(), null, null);
        }
        // Lay key AES -> Giai ma du lieu
        String strAesKeyDecode = userGroup.getStrAesKey();
        data = SecurityControler.decodeDataByAes(strAesKeyDecode, data);
        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
        userId = userGroup.getUserId2();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            // Parse du lieu
            JSONObject json = new JSONObject(data);
            String[] keys = new String[] { "meetingId" };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json,
                    keys);
            Long meetingId = null;
            String strType = listValue.get(0);
            if (CommonUtils.isInteger(strType)) {
                meetingId = Long.valueOf(strType);
            }
            boolean result = false;
            MeetingWeekDAO meetingWeekDAO = new MeetingWeekDAO();
            // nguoi dang nhap co la nguoi viet ket luan
            boolean update = meetingWeekDAO.updateWithoutConclusions(meetingId,
                    userId);
            if (update) {
                result = true;
            }
            errorCode = ErrorCode.SUCCESS;
            response = FunctionCommon.generateResponseJSON(errorCode,
                    result ? 1 : 0, strAesKeyDecode);
        } catch (Exception ex) {
            LOGGER.error("checkUpdateWithoutConclusions - Exception -"
                    + " username: " + cardId + "\ndata: " + data, ex);
            errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
            response = FunctionCommon.generateResponseJSON(errorCode, null,
                    null);
        }
        return response;
    }
}