/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.SyncFavoriteClientDAO;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.utils.LogUtils;
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
public class SyncFavoriteClientController {

    public static final String ROOT_ACTION = "syncFavAction";
    /**
     * Log loi
     */
    private static final Logger logger = Logger.getLogger(SyncFavoriteClientController.class);

    // Ten cua class bao gom ca ten package
    private static final String CLASS_NAME = SyncFavoriteClientController.class.getName();

    //<editor-fold defaultstate="collapsed" desc="Đồng bộ danh sách cá nhân trong favorite sang v2">
    public String syncEmployeeFav(HttpServletRequest request,
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
            String[] keys = new String[]{
                ConstantsFieldParams.LIST_EMPLOYEE_ID_SYNC,
                ConstantsFieldParams.TYPE};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
//            Object listTemp = null;
            if (listValue.get(0) != null && listValue.get(0).trim().length() > 0) {
//                listTemp = listValue.get(0);
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
            int type = 0;
            if (listValue.get(1) != null && listValue.get(1).trim().length() > 0) {
                type = Integer.parseInt(listValue.get(1));
            }
            List<Long> listUserSync = null;
            if (!json.isNull(ConstantsFieldParams.LIST_EMPLOYEE_ID_SYNC)) {
                JSONArray jsonArray = json.getJSONArray(ConstantsFieldParams.LIST_EMPLOYEE_ID_SYNC);
                if (jsonArray != null && jsonArray.length() > 0) {
                    listUserSync = new ArrayList<Long>();
                    Long jsonObject;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = jsonArray.getLong(i);
                        listUserSync.add(jsonObject);
                    }
                }
            }

            SyncFavoriteClientDAO tempDAO = new SyncFavoriteClientDAO();
            Object result = tempDAO.syncPersonFav(listUserSync, type);
            if (result == null) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            }
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException ex) {
            logger.error(ex.getMessage(), ex);
            LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_ERROR, ErrorCode.INPUT_INVALID, ex.getMessage(), data, isSecurity);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Đồng bộ danh sách đơn vị trong favorite sang v2">
    public String syncOrganizationFav(HttpServletRequest request,
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
            String[] keys = new String[]{
                ConstantsFieldParams.LIST_GROUP_ID_SYNC,
                ConstantsFieldParams.TYPE};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
//            Object listTemp = null;
            if (listValue.get(0) != null && listValue.get(0).trim().length() > 0) {
//                listTemp = listValue.get(0);
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
            int type = 0;
            if (listValue.get(1) != null && listValue.get(1).trim().length() > 0) {
                type = Integer.parseInt(listValue.get(1));
            }
            List<Long> listOrgSync = null;
            if (!json.isNull(ConstantsFieldParams.LIST_GROUP_ID_SYNC)) {
                JSONArray jsonArray = json.getJSONArray(ConstantsFieldParams.LIST_GROUP_ID_SYNC);
                if (jsonArray != null && jsonArray.length() > 0) {
                    listOrgSync = new ArrayList<Long>();
                    Long jsonObject;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = jsonArray.getLong(i);
                        listOrgSync.add(jsonObject);
                    }
                }
            }

            SyncFavoriteClientDAO tempDAO = new SyncFavoriteClientDAO();
            Object result = tempDAO.syncOrgFav(listOrgSync, type);
            if (result == null) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            }
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException ex) {
            logger.error(ex.getMessage(), ex);
            LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_ERROR, ErrorCode.INPUT_INVALID, ex.getMessage(), data, isSecurity);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    //</editor-fold>
}
