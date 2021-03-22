/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.itextpdf.text.DocumentException;
import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.constants.InputStreamWithFileDeletion;
import com.viettel.voffice.database.dao.TemplateDAO;
import com.viettel.voffice.database.entity.EntityFileAttachment;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityTemplate;
import com.viettel.voffice.database.entity.EntityTemplateOrg;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.LogUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author luanvd
 */
public class TemplateController {

    public static final String ROOT_ACTION = "tempAction";
    /**
     * Log loi
     */
    private static final Logger logger = Logger.getLogger(CommentController.class);

    // Ten cua class bao gom ca ten package
    private static final String CLASS_NAME = TemplateController.class.getName();

    //<editor-fold defaultstate="collapsed" desc="Lấy danh sách template">
    public String getListTemplate(HttpServletRequest request,
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
                ConstantsFieldParams.TEMPLATE_TYPE};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Integer tempType;
            if (listValue.get(0) != null && listValue.get(0).trim().length() > 0) {
                tempType = Integer.parseInt(listValue.get(0));
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
            TemplateDAO cmtDAO = new TemplateDAO();
            Object result = cmtDAO.getListTemplateByUser(userId, tempType);

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

    //<editor-fold defaultstate="collapsed" desc="Them moi template">
    public String addTemplate(HttpServletRequest request,
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
                ConstantsFieldParams.TEMPLATE_NAME,
                ConstantsFieldParams.TEMPLATE_TYPE};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String tempName;
            if (listValue.get(0) != null && listValue.get(0).trim().length() > 0) {
                tempName = listValue.get(0);
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
            Integer tempType;
            if (listValue.get(1) != null && listValue.get(1).trim().length() > 0) {
                tempType = Integer.parseInt(listValue.get(1));
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }

            EntityTemplate commentE = new EntityTemplate();
            commentE.setTempName(tempName);
            commentE.setCreateBy(userId);
            commentE.setTempType(tempType);
            TemplateDAO tempDAO = new TemplateDAO();
            Object result = tempDAO.insertTemplate(userId, commentE);
            if (result == null) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            } else {
                Long n = Long.parseLong(result.toString());
                if (n == -3) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.ERROR_TEMPLATE_IS_MAX, null, null);
                }
                if (n == -2) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.IS_EXISTS_TEMPLATE_NAME, null, null);
                }
                if (n == -1) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
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

    //<editor-fold defaultstate="collapsed" desc="Sửa tem plate">
    public String editTemplate(HttpServletRequest request,
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
                ConstantsFieldParams.TEMPLATE_ID,
                ConstantsFieldParams.TEMPLATE_NAME,
                ConstantsFieldParams.TEMPLATE_TYPE};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long tempId;
            if (listValue.get(0) != null && listValue.get(0).trim().length() > 0) {
                tempId = Long.parseLong(listValue.get(0));
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
            String tempName;
            if (listValue.get(1) != null && listValue.get(1).trim().length() > 0) {
                tempName = listValue.get(1);
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
            Integer tempType;
            if (listValue.get(2) != null && listValue.get(2).trim().length() > 0) {
                tempType = Integer.parseInt(listValue.get(2));
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }

            TemplateDAO tempDAO = new TemplateDAO();
            Object result = tempDAO.updateTemplate(userId, tempId, tempName, tempType);
            if (result == null) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            } else {
                int n = Integer.parseInt(result.toString());
                if (n == 2) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.IS_EXISTS_TEMPLATE_NAME, null, null);
                }
                if (n == 0) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
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

    //<editor-fold defaultstate="collapsed" desc="Xoa tem plate">
    public String deleteTemplate(HttpServletRequest request,
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
                ConstantsFieldParams.TEMPLATE_ID,
                ConstantsFieldParams.TEMPLATE_TYPE,
                ConstantsFieldParams.TEMPLATE_CURRENT_INDEX};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long tempId;
            if (listValue.get(0) != null && listValue.get(0).trim().length() > 0) {
                tempId = Long.parseLong(listValue.get(0));
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
            Integer tempType;
            if (listValue.get(1) != null && listValue.get(1).trim().length() > 0) {
                tempType = Integer.parseInt(listValue.get(1));
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
            Integer tempCurIndex;
            if (listValue.get(2) != null && listValue.get(2).trim().length() > 0) {
                tempCurIndex = Integer.parseInt(listValue.get(2));
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
            TemplateDAO tempDAO = new TemplateDAO();
            Object result = tempDAO.deleteTemplate(userId, tempId, tempType, tempCurIndex);
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

    //<editor-fold defaultstate="collapsed" desc="Xoa danh sach tem plate">
    public String deleteListTemplate(HttpServletRequest request,
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
                ConstantsFieldParams.LIST_TEMPLATE,
                ConstantsFieldParams.TEMPLATE_TYPE};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
//            Object listTemp = null;
            if (listValue.get(0) != null && listValue.get(0).trim().length() > 0) {
//                listTemp = listValue.get(0);
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
            Integer tempType;
            if (listValue.get(1) != null && listValue.get(1).trim().length() > 0) {
                tempType = Integer.parseInt(listValue.get(1));
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
            // Danh sach id van ban thay the them vao
            List<EntityTemplate> listTemplate = null;
            if (!json.isNull(ConstantsFieldParams.LIST_TEMPLATE)) {
                JSONArray jsonArray = json.getJSONArray(ConstantsFieldParams.LIST_TEMPLATE);
                if (jsonArray != null && jsonArray.length() > 0) {
                    listTemplate = new ArrayList<EntityTemplate>();
                    JSONObject jsonObject;
                    EntityTemplate entityTemp;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        entityTemp = new EntityTemplate();
                        jsonObject = jsonArray.getJSONObject(i);
                        if (!jsonObject.isNull(ConstantsFieldParams.TEMPLATE_ID)) {
                            entityTemp.setTempId(jsonObject.getLong(ConstantsFieldParams.TEMPLATE_ID));
                            entityTemp.setTempType(tempType);
                            listTemplate.add(entityTemp);
                        }
                    }
                }
            }

            TemplateDAO tempDAO = new TemplateDAO();
            Object result = tempDAO.deleteListTemplate(userId, listTemplate, tempType);
            if (result == null) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            } else {
                int n = Integer.parseInt(result.toString());
                if (n == 2) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.IS_EXISTS_TEMPLATE_NAME, null, null);
                }
                if (n == 0) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
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

    //<editor-fold defaultstate="collapsed" desc="update index tem plate">
    public String updateIndexTemplate(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup entityUserGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!entityUserGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(entityUserGroup.getEnumErrCode(), null, null);
        }

        // Kiem tra user id tren he thong 2
        Long userId = entityUserGroup.getVof2_ItemEntityUser().getUserId();
        // System.out.println("luanvd-updateIndexTemplate-userId:" + userId);
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
                ConstantsFieldParams.TEMPLATE_ID,
                ConstantsFieldParams.TEMPLATE_CURRENT_INDEX,
                ConstantsFieldParams.TEMPLATE_TYPE,
                ConstantsFieldParams.TEMPLATE_NEW_INDEX};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long tempId;
            if (listValue.get(0) != null && listValue.get(0).trim().length() > 0) {
                tempId = Long.parseLong(listValue.get(0));
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
            Integer oldIndex;
            if (listValue.get(1) != null && listValue.get(1).trim().length() > 0) {
                oldIndex = Integer.parseInt(listValue.get(1));
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
            Integer tempType;
            if (listValue.get(2) != null && listValue.get(2).trim().length() > 0) {
                tempType = Integer.parseInt(listValue.get(2));
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
            Integer newIndex;
            if (listValue.get(3) != null && listValue.get(3).trim().length() > 0) {
                newIndex = Integer.parseInt(listValue.get(3));
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }

            TemplateDAO tempDAO = new TemplateDAO();
            Object result = tempDAO.updateIndexTemplate(userId, tempId, tempType, oldIndex, newIndex);
            if (result == null) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            }
            int n = Integer.parseInt(result.toString());
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            if (n == 1) {
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
            }
        } catch (JSONException ex) {
            logger.error(ex.getMessage(), ex);
            LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_ERROR, ErrorCode.INPUT_INVALID, ex.getMessage(), data, isSecurity);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Them moi danh sách template">
    public String addAListTemplate(HttpServletRequest request,
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
        //System.out.println("luanvd-addAListTemplate-data:" + data);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.LIST_TEMPLATE,
                ConstantsFieldParams.TEMPLATE_TYPE};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
//            Object listTemp = null;
            if (listValue.get(0) != null && listValue.get(0).trim().length() > 0) {
//                listTemp = listValue.get(0);
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
            Integer tempType;
            if (listValue.get(1) != null && listValue.get(1).trim().length() > 0) {
                tempType = Integer.parseInt(listValue.get(1));
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
            // Danh sach id van ban thay the them vao
            List<EntityTemplate> listTemplate = null;
            if (!json.isNull(ConstantsFieldParams.LIST_TEMPLATE)) {
                JSONArray jsonArray = json.getJSONArray(ConstantsFieldParams.LIST_TEMPLATE);
                if (jsonArray != null && jsonArray.length() > 0) {
                    listTemplate = new ArrayList<EntityTemplate>();
                    JSONObject jsonObject;
                    EntityTemplate entityTemp;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        entityTemp = new EntityTemplate();
                        jsonObject = jsonArray.getJSONObject(i);
                        if (!jsonObject.isNull(ConstantsFieldParams.TEMPLATE_NAME)) {
                            entityTemp.setTempName(jsonObject.getString(ConstantsFieldParams.TEMPLATE_NAME));
                            entityTemp.setTempIndex(i);
                            entityTemp.setTempType(tempType);
                            entityTemp.setCreateBy(userId);
                            listTemplate.add(entityTemp);
                        }
                    }
                }
            }

            TemplateDAO tempDAO = new TemplateDAO();
            Object result = tempDAO.insertListTemplate(userId, listTemplate, tempType);
            if (result == null) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            } else {
                int n = Integer.parseInt(result.toString());
                if (n == 2) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.IS_EXISTS_TEMPLATE_NAME, null, null);
                }
                if (n == 0) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
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

    /**
     * download file bieu mau
     *
     * @param req
     * @param data
     * @param isSecurity
     * @return
     */
    public Response downloadFile(
            HttpServletRequest req, String data, String isSecurity) {
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
            //loi du lieu dau vao
            logger.error("downloadFile - userId: "
                    + userIdVof2 + " - Loi het session");
            return response;
        }
        // Giai ma du lieu neu ma hoa
        String aesKey;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        InputStream inputStream = null;
        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
        EntityLog log = new EntityLog(req, CLASS_NAME);
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{"fileAttachId"};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String strFileAttachId = listValue.get(0);
            Long fileAttachId = null;//id file dinh kem
            if (!CommonUtils.isEmpty(strFileAttachId)) {
                fileAttachId = Long.parseLong(strFileAttachId);
            }
            if (fileAttachId != null) {
                TemplateDAO templateDAO = new TemplateDAO();
                EntityFileAttachment attach = templateDAO.downloadFile(req, fileAttachId, userIdVof2);
                if (attach == null) {
                    logger.error("downloadFile - File null hoac file khong ton tai");
                    logger.error("attachId: " + fileAttachId);
                    return response;
                }
                String tmpFilePath = attach.getFilePath();
                String fileName = attach.getFileName();
                if (tmpFilePath == null) {
                    logger.error("downloadFile - File null hoac file khong ton tai");
                    logger.error("tmpFilePath: " + tmpFilePath);
                    return response;
                }
                File file = new File(tmpFilePath);
                // File khong ton tai
                if (!file.exists()) {
                    logger.error("downloadFile - File null hoac file khong ton tai");
                    logger.error("tmpFilePath: " + tmpFilePath);
                    return response;
                }
                inputStream = new InputStreamWithFileDeletion(file);
                Response.ResponseBuilder responseBuilder = Response.ok(inputStream);
                responseBuilder.header("File-Name", fileName);
                if (file.length() > 0) {
                    responseBuilder.header("File-Size", file.length());
                } else {
                    responseBuilder.header("File-Size", 0);
                }
                response = responseBuilder.build();
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } else {
                //loi du lieu dau vao
                logger.error("downloadFile - userId: "
                        + userIdVof2 + " - Loi du lieu dau null");
                Response.ResponseBuilder responseBuilder = Response.ok();
                responseBuilder.header("filePermission", "0");
                response = responseBuilder.build();
                return response;
            }
        } catch (JSONException | NumberFormatException | IOException | DocumentException ex) {
            logger.error("downloadFile - Exception:", ex);
        }
        return response;
    }

    /**
     * xoa bieu mau
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String delete(HttpServletRequest request,
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
            String[] keys = new String[]{"tempId"};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String strTempId = listValue.get(0);
            Long tempId = null;//id file dinh kem
            if (!CommonUtils.isEmpty(strTempId)) {
                tempId = Long.parseLong(strTempId);
            }
            if (tempId != null) {
                TemplateDAO templateDAO = new TemplateDAO();
                Boolean deleteResult = templateDAO.delete(userId, tempId);
                if (deleteResult) {
                    //xoa thanh cong
                    // Ghi log ket thuc chuc nang
                    LogUtils.logFunctionalEnd(log);
                    return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, deleteResult, aesKey);
                } else {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
            } else {
                logger.error("delete template - fileAttachId is null");
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
        } catch (JSONException ex) {
            logger.error("delete template - Exception:", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * lay chi tiet bieu mau
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getTemplateDetail(HttpServletRequest request,
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
            String[] keys = new String[]{"tempId"};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String strTempId = listValue.get(0);
            Long tempId = null;//id file dinh kem
            if (!CommonUtils.isEmpty(strTempId)) {
                tempId = Long.parseLong(strTempId);
            }
            if (tempId != null) {
                TemplateDAO templateDAO = new TemplateDAO();
                EntityTemplate temp = templateDAO.getTemplateDetail(tempId, userId);
                if (temp != null) {
                    //xoa thanh cong
                    // Ghi log ket thuc chuc nang
                    LogUtils.logFunctionalEnd(log);
                    return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, temp, aesKey);
                } else {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
            } else {
                logger.error("getTemplateDetail - tempId is null");
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
        } catch (JSONException ex) {
            logger.error("getTemplateDetail - Exception:", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * tim kiem bieu mau
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String searchTemplate(HttpServletRequest request,
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
            String[] keys = new String[]{"name", "code", "typeId", "areaId", "fromDate", "toDate", "lstOrgId",
                "startRecord", "pageSize", "isCount", "keyword"};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            //ten temp
            String name = listValue.get(0);
            //ma
            String code = listValue.get(1);
            //hinh thuc
            String strTypeId = listValue.get(2);
            Long typeId = null;
            if (!CommonUtils.isEmpty(strTypeId)) {
                typeId = Long.parseLong(strTypeId);
            }
            //linh vuc
            String strAreaId = listValue.get(3);
            Long areaId = null;
            if (!CommonUtils.isEmpty(strAreaId)) {
                areaId = Long.parseLong(strAreaId);
            }
            //ngay bat dau hieu luc dd/MM/yyyy
            String fromDate = listValue.get(4);
            //ngay ket thuc hieu luc dd/MM/yyyy
            String toDate = listValue.get(5);
            //don vi tim kiem
            List<Long> lstOrgId = new ArrayList<>();
            JSONArray arrOrgId = FunctionCommon.jsonGetArray("lstOrgId", data);
            if (arrOrgId != null && arrOrgId.length() > 0) {
                int arrOrgIdSize = arrOrgId.length();
                Long orgId;
                for (int i = 0; i < arrOrgIdSize; i++) {
                    JSONObject innerObj = (JSONObject) arrOrgId.get(i);
                    orgId = null;
                    if (innerObj.has("groupId") && innerObj.getString("groupId") != null) {
                        orgId = Long.parseLong(innerObj.getString("groupId").trim());
                    }
                    if (orgId != null) {
                        lstOrgId.add(orgId);
                    }
                }
            }

            String strStartRecord = listValue.get(7);
            Long startRecord = 0L;
            if (!CommonUtils.isEmpty(strStartRecord)) {
                startRecord = Long.parseLong(strStartRecord);
            }

            String strPageSize = listValue.get(8);
            Long pageSize = 10L;
            if (!CommonUtils.isEmpty(strPageSize)) {
                pageSize = Long.parseLong(strPageSize);
            }
            //tim kiem hay lay so luong
            String strIsCount = listValue.get(9);
            Long isCount = 0L;
            if (!CommonUtils.isEmpty(strIsCount)) {
                isCount = Long.parseLong(strIsCount);
            }
            //tim kiem nhanh
            String keyword = listValue.get(10);

            if (!lstOrgId.isEmpty() || !entityUserGroup.getVof2_ItemEntityUser().getLstVhrOrgAll().isEmpty()) {
                TemplateDAO templateDAO = new TemplateDAO();
                Object lstTemp = templateDAO.searchTemplate(null, name, code,
                        typeId, areaId, fromDate,
                        toDate, lstOrgId, userId, startRecord, pageSize,
                        isCount, keyword, entityUserGroup.getVof2_ItemEntityUser().getLstVhrOrgAll());
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, lstTemp, aesKey);
            } else {
                logger.error("searchTemplate - lstOrgId is null");
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
        } catch (JSONException ex) {
            logger.error("searchTemplate - Exception:", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * them moi bieu mau
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String insertTemplate(HttpServletRequest request,
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
            EntityTemplate temp = convertDataTemplate(data);
            if (temp != null && !CommonUtils.isEmpty(temp.getName())
                    && !CommonUtils.isEmpty(temp.getFromDate())
                    && !CommonUtils.isEmpty(temp.getListFileAttachment())
                    && !CommonUtils.isEmpty(temp.getListTempOrg())) {
                String description = (temp.getDescription() != null) ? temp.getDescription().trim() : null;
                String code = (temp.getCode() != null) ? temp.getCode().trim() : null;
                TemplateDAO templateDAO = new TemplateDAO();
                Boolean addTempResult = templateDAO.addTemplate(userId, temp.getName().trim(),
                        code, description, temp.getTypeId(),
                        temp.getAreaId(), temp.getFromDate(), temp.getToDate(),
                        temp.getListFileAttachment(), temp.getListTempOrg());
                if (addTempResult) {
                    // Ghi log ket thuc chuc nang
                    LogUtils.logFunctionalEnd(log);
                    return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, addTempResult, aesKey);
                } else {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
            } else {
                logger.error("insertTemplate - data is null");
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
        } catch (JSONException ex) {
            logger.error("insertTemplate - Exception:", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * them moi bieu mau
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String updateTemplate(HttpServletRequest request,
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
            EntityTemplate temp = convertDataTemplate(data);
            if (temp != null && temp.getTempId() != null
                    && !CommonUtils.isEmpty(temp.getName())
                    && !CommonUtils.isEmpty(temp.getFromDate())) {
                String description = (temp.getDescription() != null) ? temp.getDescription().trim() : null;
                String code = (temp.getCode() != null) ? temp.getCode().trim() : null;
                TemplateDAO templateDAO = new TemplateDAO();
                Boolean editTempResult = templateDAO.editTemplate(temp.getTempId(), userId, temp.getName().trim(),
                        code, description, temp.getTypeId(),
                        temp.getAreaId(), temp.getFromDate(), temp.getToDate(),
                        temp.getListFileAttachment(), temp.getListTempOrg());
                if (editTempResult) {
                    // Ghi log ket thuc chuc nang
                    LogUtils.logFunctionalEnd(log);
                    return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, editTempResult, aesKey);
                } else {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
            } else {
                logger.error("updateTemplate - data is null");
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
        } catch (JSONException ex) {
            logger.error("updateTemplate - Exception:", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * convert data to Template
     *
     * @param data
     * @return
     */
    private EntityTemplate convertDataTemplate(String data) throws JSONException {
        JSONObject json = new JSONObject(data);
        String[] keys = new String[]{"name", "code", "typeId", "areaId", "fromDate", "toDate",
            "description", "lstFileAttach", "lstTempOrg", "tempId"};
        List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
        //ten temp
        String name = listValue.get(0);
        //ma
        String code = listValue.get(1);
        //hinh thuc
        String strTypeId = listValue.get(2);
        Long typeId = null;
        if (!CommonUtils.isEmpty(strTypeId)) {
            typeId = Long.parseLong(strTypeId);
        }
        //linh vuc
        String strAreaId = listValue.get(3);
        Long areaId = null;
        if (!CommonUtils.isEmpty(strAreaId)) {
            areaId = Long.parseLong(strAreaId);
        }
        //ngay bat dau hieu luc dd/MM/yyyy
        String fromDate = listValue.get(4);
        //ngay ket thuc hieu luc dd/MM/yyyy
        String toDate = listValue.get(5);
        //mo ta
        String description = listValue.get(6);
        //file bieu mau dinh kem
        String strLstFileAttach = listValue.get(7);
        List<EntityFileAttachment> listAttachFile = null;
        if (!CommonUtils.isEmpty(strLstFileAttach)) {
            Type listFileAttachType = new TypeToken<ArrayList<EntityFileAttachment>>() {
            }.getType();
            Gson gson = new Gson();
            listAttachFile = gson.fromJson(strLstFileAttach, listFileAttachType);
        }
        //don vi ap dung
        String strLstTempOrg = listValue.get(8);
        List<EntityTemplateOrg> lstTempOrg = null;
        if (!CommonUtils.isEmpty(strLstTempOrg)) {
            Type listTemplateOrgType = new TypeToken<ArrayList<EntityTemplateOrg>>() {
            }.getType();
            Gson gson = new Gson();
            lstTempOrg = gson.fromJson(strLstTempOrg, listTemplateOrgType);
        }
        //hinh thuc
        String strTempId = listValue.get(9);
        Long tempId = null;
        if (!CommonUtils.isEmpty(strTempId)) {
            tempId = Long.parseLong(strTempId);
        }
        EntityTemplate tem = new EntityTemplate();
        tem.setTempId(tempId);
        tem.setName(name);
        tem.setCode(code);
        tem.setDescription(description);
        tem.setTypeId(typeId);
        tem.setAreaId(areaId);
        tem.setFromDate(fromDate);
        tem.setToDate(toDate);
        tem.setListFileAttachment(listAttachFile);
        tem.setListTempOrg(lstTempOrg);
        return tem;
    }
}
