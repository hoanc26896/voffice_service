/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.ConfigParameterDAO;
import com.viettel.voffice.database.dao.SystemParameterDAO;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntitySystemParameter;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.utils.CommonUtils;
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
 * @author luanvd
 */
public class ConfigParameterController {

    // Log file
    private static final Logger logger = Logger.getLogger(ConfigParameterController.class);

    // Ten cua class bao gom ca ten package
    private static final String CLASS_NAME = ConfigParameterController.class.getName();

    /**
     * <b>Lay cau hinh co duoc ky hang loat hay khong</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getConfigParamMultiSign(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(request);
        if (dataSessionGR.getCheckSessionOk()) {
            String cardId = "";
            try {
                // Lay session
                EntityUserGroup entityUserGroup = dataSessionGR;
//                if (entityUserGroup.getItemEntityUser() == null && entityUserGroup.getVof2_ItemEntityUser() == null) {
//                    return FunctionCommon.generateResponseJSON(ErrorCode.SESSION_TIME_OUT, null, null);
//                }

                // Lay ma nhan vien
                if (entityUserGroup.getItemEntityUser() != null
                        && !CommonUtils.isEmpty(entityUserGroup.getItemEntityUser()
                                .getStrCardNumber())) {
                    cardId = entityUserGroup.getItemEntityUser().getStrCardNumber();
                } else if (entityUserGroup.getVof2_ItemEntityUser() != null
                        && !CommonUtils.isEmpty(entityUserGroup.getVof2_ItemEntityUser()
                                .getStrCardNumber())) {
                    cardId = entityUserGroup.getVof2_ItemEntityUser().getStrCardNumber();
                }
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
                JSONObject json = new JSONObject(data);
                String[] keys = new String[]{
                    ConstantsFieldParams.TEXT_TYPE_ID
                };
                List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                Long typeId = 0L;
                if (listValue.get(0) != null && listValue.get(0).trim().length() > 0) {
                    typeId = Long.parseLong(listValue.get(0));
                }

                ConfigParameterDAO cpDao = new ConfigParameterDAO();
                Object result = cpDao.getConfigParamMultiSign(typeId);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                if (result == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);

            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
        } else {
            return FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }
    }

    public String findConfigBackList(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(request);
        if (dataSessionGR.getCheckSessionOk()) {
            String cardId = "";
            try {
                // Lay session
                EntityUserGroup entityUserGroup = dataSessionGR;
                if (entityUserGroup.getItemEntityUser() == null && entityUserGroup.getVof2_ItemEntityUser() == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.SESSION_TIME_OUT, null, null);
                }

                // Lay ma nhan vien
                if (entityUserGroup.getItemEntityUser() != null
                        && !CommonUtils.isEmpty(entityUserGroup.getItemEntityUser()
                                .getStrCardNumber())) {
                    cardId = entityUserGroup.getItemEntityUser().getStrCardNumber();
                } else if (entityUserGroup.getVof2_ItemEntityUser() != null
                        && !CommonUtils.isEmpty(entityUserGroup.getVof2_ItemEntityUser()
                                .getStrCardNumber())) {
                    cardId = entityUserGroup.getVof2_ItemEntityUser().getStrCardNumber();
                }
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
                JSONObject json = new JSONObject(data);
                String[] keys = new String[]{
                    ConstantsFieldParams.IS_COUNT,
                    ConstantsFieldParams.KEYWORD,
                    ConstantsFieldParams.TYPE,
                    ConstantsFieldParams.PAGE_SIZE,
                    ConstantsFieldParams.START_RECORD
                };
                List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                String isCount = listValue.get(0);
                String keyword = listValue.get(1);
                Long type = null;
                String strType = listValue.get(2);
                if (!CommonUtils.isEmpty(strType)) {
                    type = Long.parseLong(strType);
                }
                Long startRecord = null;
                String strStartRecord = listValue.get(3);
                if (!CommonUtils.isEmpty(strStartRecord)) {
                    startRecord = Long.parseLong(strStartRecord);
                }
                Long pageSize = null;
                String strPageSize = listValue.get(4);
                if (!CommonUtils.isEmpty(strPageSize)) {
                    pageSize = Long.parseLong(strPageSize);
                }

                ConfigParameterDAO cpDao = new ConfigParameterDAO();
                Object result = cpDao.findConfigBackList(isCount, pageSize, startRecord, keyword, entityUserGroup.getVof2_ItemEntityUser().getUserId(), type);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                if (result == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);

            } catch (NumberFormatException | JSONException ex) {
                logger.error(ex.getMessage(), ex);
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
        } else {
            return FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }
    }

    public String getListConfigBackList(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(request);
        if (dataSessionGR.getCheckSessionOk()) {
            String cardId = "";
            try {
                // Lay session
                EntityUserGroup entityUserGroup = dataSessionGR;
                // Lay ma nhan vien
                if (entityUserGroup.getItemEntityUser() != null
                        && !CommonUtils.isEmpty(entityUserGroup.getItemEntityUser()
                                .getStrCardNumber())) {
                    cardId = entityUserGroup.getItemEntityUser().getStrCardNumber();
                } else if (entityUserGroup.getVof2_ItemEntityUser() != null
                        && !CommonUtils.isEmpty(entityUserGroup.getVof2_ItemEntityUser()
                                .getStrCardNumber())) {
                    cardId = entityUserGroup.getVof2_ItemEntityUser().getStrCardNumber();
                }
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
                ConfigParameterDAO cpDao = new ConfigParameterDAO();
                Object result = cpDao.getListConfigBackList();
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                if (result == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);

            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
        } else {
            return FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }
    }

    public String insertConfigBackList(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup entityUserGroup = FunctionCommon.getStatusSession(request);
        if (!entityUserGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(entityUserGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = entityUserGroup.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        String cardId = entityUserGroup.getVof2_ItemEntityUser().getStrCardNumber();
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = entityUserGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        log.setUserName(cardId);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.TYPE
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long type = null;
            String strType = listValue.get(0);
            if (!CommonUtils.isEmpty(strType)) {
                type = Long.parseLong(strType);
            }
            List<Long> listInsert = null;
            if (!json.isNull(ConstantsFieldParams.LIST_INSERT)) {
                JSONArray jsonArray = json.getJSONArray(ConstantsFieldParams.LIST_INSERT);
                if (jsonArray != null && jsonArray.length() > 0) {
                    listInsert = new ArrayList<Long>();
                    Long jsonObject;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = jsonArray.getLong(i);
                        listInsert.add(jsonObject);
                    }
                }
            }
            LogUtils.logFunctionalStart(log);
            ConfigParameterDAO cpDao = new ConfigParameterDAO();
            Integer result = cpDao.insertConfigBackList(listInsert, userId, type);
            LogUtils.logFunctionalEnd(log);
            if (result == 1) {
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            }
        } catch (JSONException | NumberFormatException ex) {
            logger.error("insertConfigBackList:", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    public String deleteConfigBackList(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup entityUserGroup = FunctionCommon.getStatusSession(request);
        if (!entityUserGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(entityUserGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = entityUserGroup.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        String cardId = entityUserGroup.getVof2_ItemEntityUser().getStrCardNumber();
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = entityUserGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        log.setUserName(cardId);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.TYPE,
                ConstantsFieldParams.STR_OBJECT_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long type = null;
            String strType = listValue.get(0);
            if (!CommonUtils.isEmpty(strType)) {
                type = Long.parseLong(strType);
            }
            Long configId = null;
            String strConfigId = listValue.get(1);
            if (!CommonUtils.isEmpty(strConfigId)) {
                configId = Long.parseLong(strConfigId);
            }
            LogUtils.logFunctionalStart(log);
            ConfigParameterDAO cpDao = new ConfigParameterDAO();
            Integer result = cpDao.deleteConfigBackList(configId, userId, type);
            LogUtils.logFunctionalEnd(log);
            if (result == 1) {
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            }
        } catch (JSONException | NumberFormatException ex) {
            logger.error("deleteConfigBackList:", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }
    
    /**
     * <b>Lay cau hinh ung dung</b>
     * 
     * @param request
     * @param data
     * @return 
     */
    public String getAppConfig(HttpServletRequest request, String data) {

        String[] keys = new String[]{
            "listCode"
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            logger.error("getAppConfig - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            // Parse du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            String strListCode = listValue.get(0);
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<String>>() {
            }.getType();
            List<String> listCode = gson.fromJson(strListCode, type);
            SystemParameterDAO systemParameterDAO = new SystemParameterDAO();
            List<EntitySystemParameter> result = systemParameterDAO.getConfigValueByCode(listCode);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            logger.error("getAppConfig - Exception - username: " + userGroup.getCardId(), ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }
}
