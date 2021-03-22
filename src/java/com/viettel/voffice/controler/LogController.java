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
import com.viettel.voffice.database.dao.logAction.ActionLogMobileDAO;
import com.viettel.voffice.database.dao.logAction.UserActivityLogDAO;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.User.EntityUser;
import com.viettel.voffice.database.entity.User.Vof2_EntityUser;
import com.viettel.voffice.database.entity.log.EntityActionLogMobile;
import com.viettel.voffice.database.entity.log.EntityUserActivityLog;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.LogUtils;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Xu ly log
 *
 * @author thanght6
 */
public class LogController {

    // Ghi log
    private static final Logger LOGGER = Logger.getLogger(LogController.class);

    // Ten cua class bao gom ca ten package
    private static final String CLASS_NAME = LogController.class.getName();

    /**
     * <b>Thuc hien chen log client vao bang action_log_mobile</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String insertActionLogMobile(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String currentNode = request.getLocalAddr() + ":" + request.getLocalPort();
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("insertActionLogMobile - No session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Neu user hoac user id tren he thong 1&2 null
        // -> Tra ve thong bao khong co quyen
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        if ((user1 == null || user1.getUserId() == null)
                && (user2 == null || user2.getUserId() == null)) {
            LOGGER.error("insertActionLogMobile - user hoac userId tren he thong 1&2 null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        Long userId = user2 != null ? user2.getUserId() : user1.getUserId();
        if (userId == null) {
            userId = 0L;
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
                ConstantsFieldParams.START_TIME,
                ConstantsFieldParams.END_TIME,
                ConstantsFieldParams.FUNCTION_NAME,
                ConstantsFieldParams.DESCRIPTION,
                ConstantsFieldParams.TEMPTIME,
                ConstantsFieldParams.DEVICE_NAME,
                ConstantsFieldParams.SUB_TIME,
                ConstantsFieldParams.FUNCTION_TYPE,
                ConstantsFieldParams.DATA_EXCUTE
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String startTime = listValue.get(0);
            String endTime = listValue.get(1);
            String functionName = listValue.get(2);
            String description = listValue.get(3);
            String tempTime = listValue.get(4);
            String deviceName = listValue.get(5);
            Integer subTime = null;
            String strSubTime = listValue.get(6);
            if (!CommonUtils.isEmpty(strSubTime)) {
                subTime = Integer.parseInt(strSubTime);
            }
            Integer functionType = null;
            String strFunctionType = listValue.get(7);
            if (!CommonUtils.isEmpty(strFunctionType)) {
                functionType = Integer.parseInt(strFunctionType);
            }
            String dataExcute = listValue.get(8);
            EntityActionLogMobile actionLogMobile = new EntityActionLogMobile(userId, cardId,
                    startTime, endTime, functionName, description, currentNode, 2,
                    tempTime, deviceName, subTime, functionType, dataExcute);
            int result = 0;
            ActionLogMobileDAO actionLogMobileDAO = new ActionLogMobileDAO();
            if (actionLogMobileDAO.insert(actionLogMobile)) {
                result = 1;
            }
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } // Du lieu dau vao khong hop le
        catch (JSONException | NumberFormatException ex) {
            LOGGER.error("insertActionLogMobile - cardId: " + cardId
                    + " -  Exception: ", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        } // Loi server
        catch (Exception ex) {
            LOGGER.error("insertActionLogMobile - cardId: " + cardId
                    + " -  Exception: ", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    /**
     * Lay so luong dang nhap trong khoang thoi gian hien tai
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String checkThresholdPersonLogin(HttpServletRequest request,
            String data, String isSecurity) {
        if (!CommonUtils.isEmpty(data)) {
            try {
                JSONObject json = new JSONObject(data);
                String[] keys = new String[]{"userName", "userPass"};
                List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                String userName = listValue.get(0);
                String userPass = listValue.get(1);
                if (userName != null && userPass != null
                        && "admin".equals(userName.trim()) && "admin".equals(userPass.trim())) {
                    ActionLogMobileDAO actionLogMobileDAO = new ActionLogMobileDAO();
                    Long countLogin = actionLogMobileDAO.getCountPersonLogin();
                    if (countLogin == null) {
                        //neu co loi xay ra
                        return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, 1, null);
                    } else {
                        //tra ve so luong 
                        Long thresholdCountLogin = 200000L;
                        String thresholdCountLoginConf = CommonUtils.getAppConfigValue(ActionLogMobileDAO.THRESHOLD_COUNT_LOGIN);
                        if (!CommonUtils.isEmpty(thresholdCountLoginConf)) {
                            thresholdCountLogin = Long.valueOf(thresholdCountLoginConf);
                        }
                        int result = 0;
                        if (countLogin < thresholdCountLogin) {
                            //so luong dang nhap van duoi nguong
                            result = 1;
                        } else {
                            //gui tin nhan canh bao
                            actionLogMobileDAO.sendSmsWarningPersonLogin();
                        }
                        return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, null);
                    }
                } else {
                    return FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
                }
            } catch (JSONException | NumberFormatException ex) {
                LOGGER.error("getCountPersonLogin -  Exception: ", ex);
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            } // Loi server
            catch (Exception ex) {
                LOGGER.error("getCountPersonLogin -  Exception: ", ex);
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            }
        } else {
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * <b>Ghi log hanh vi nguoi dung</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String insertUserActivityLog(HttpServletRequest request,
            String data, String isSecurity) {

        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            LOGGER.error("insertUserActivityLog - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        try {
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[]{
                ConstantsFieldParams.LIST_USER_ACTIVITY_LOG
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Danh sach log
            String strListUserActivityLog = listValue.get(0);
            if (CommonUtils.isEmpty(strListUserActivityLog)) {
                LOGGER.error("insertUserActivityLog - username: "
                        + userGroup.getCardId() + " - Loi khong co danh sach log!");
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID,
                        null, null);
            }
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<EntityUserActivityLog>>() {
            }.getType();
            List<EntityUserActivityLog> listLog = gson.fromJson(strListUserActivityLog, listType);
            UserActivityLogDAO dao = new UserActivityLogDAO();
            int result = 0;
            if (dao.insert(userGroup.getUserId2(), listLog)) {
                result = 1;
            }
            LogUtils.logFunctionalEnd(userGroup.getKpiLog());
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result,
                    userGroup.getStrAesKey());
        } catch (Exception ex) {
            LOGGER.error("insertUserActivityLog - username: "
                    + userGroup.getCardId() + " - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
}
