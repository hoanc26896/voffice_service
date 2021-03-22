/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.StoreTypeConfigDAO;
import com.viettel.voffice.database.dao.briefmanagement.BriefManagementDAO;
import com.viettel.voffice.database.dao.meeting.MeetingMinutesDAO;
import com.viettel.voffice.database.dao.staff.CvGroupDAO;
import com.viettel.voffice.database.entity.EntityDocument;
import com.viettel.voffice.database.entity.EntityFinancialRecordsRoles;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.EntityVhrEmployee;
import com.viettel.voffice.database.entity.EntityVhrOrg;
import com.viettel.voffice.database.entity.User.EntityCvGroup;
import com.viettel.voffice.database.entity.User.Vof2_EntityUser;
import com.viettel.voffice.database.entity.meeting.EntityMeetingMinutes;
import com.viettel.voffice.database.entity.task.EntityBriefDocument;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.DateUtils;
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
 * @author pm1_os20
 */
public class StoreTypeConfigController {

    /**
     * Log loi
     */
    private static final Logger logger = Logger.getLogger(StoreTypeConfigController.class);
    private static final String CLASS_NAME = StoreTypeConfigController.class.getName();

    private static final int RETURN_TRUE = 1;
    private static Gson gson =  new GsonBuilder().setDateFormat("dd/MM/yyyy hh:mm:ss").create();
    /**
     * getListConfig
     *
     * @param request
     * @param isSecurity
     * @param data
     * @return
     */
    public String getListConfig(String isSecurity,
            String data, HttpServletRequest request) {
        String[] keys = new String[]{};
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);

        // Session timeout
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("getListConfigDao - Session timeout!");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        try {
//            //lay gia tri luu trong seasion
//            List<Long> orgIdsByRole = new ArrayList<>();
//
//            // Lay danh sach don vi sach don vi user co quyen van thu
//            List<Long> listSecretaryVhrOrg = dataSessionGR.getVof2_ItemEntityUser().getListSecretaryVhrOrg();
//            if (listSecretaryVhrOrg != null && listSecretaryVhrOrg.size() > 0) {
//                orgIdsByRole.addAll(listSecretaryVhrOrg);
//            }
            StoreTypeConfigDAO stc = new StoreTypeConfigDAO();
            Object result = stc.getListConfigDao();
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
        } catch (NumberFormatException ex) {
            logger.error("getListConfigDao - Exception:", ex);
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
    }
    
    /**
     * getListConfig
     *
     * @param request
     * @param isSecurity
     * @param data
     * @return
     * @throws JSONException 
     */
    public String getListDocType(String isSecurity,
            String data, HttpServletRequest request) throws JSONException {
        String[] keys = new String[]{
                "docTypes"
        };
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
        try {
            JSONObject json = new JSONObject(data);
            TypeToken<List<Long>> token = new TypeToken<List<Long>>() {
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            List<Long> docTypeIds = new ArrayList<Long>();
            String strList = listValue.get(0);
            JSONArray jsonArray = new JSONArray(strList);
            for (int i = 0; i < jsonArray.length(); i++) {
                docTypeIds.add(jsonArray.getLong(i));
            }
            StoreTypeConfigDAO stc = new StoreTypeConfigDAO();
            Object result = stc.getListDocType(docTypeIds);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (NumberFormatException ex) {
            logger.error("getListDocType - Exception:", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, "", aesKey);
        }
    }
    
    /**
     * them moi user dc phan quyen
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String addRolesDocType(HttpServletRequest request,
            String data, String isSecurity) {

        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            logger.error("addRolesDocType - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        try {
            Long userId = userGroup.getVof2_ItemEntityUser().getUserId();
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[]{
                "listUserRolesDocType"
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Thong tin bien ban hop
            String strLstDocRoles = listValue.get(0);
            GsonBuilder builder = new GsonBuilder().setDateFormat("dd/MM/yyyy");
            Gson gson = builder.create();
            List<EntityFinancialRecordsRoles> lstDocTypeRoles = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(strLstDocRoles);
            for(int i = 0; i < jsonArray.length(); i++){
                lstDocTypeRoles.add(gson.fromJson(jsonArray.getString(i), EntityFinancialRecordsRoles.class));
            }
            StoreTypeConfigDAO dao = new StoreTypeConfigDAO();
            boolean result = dao.insertDocTypeRoles(userId, lstDocTypeRoles);
//            LogUtils.logFunctionalEnd(userGroup.getKpiLog());
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result,
                    userGroup.getStrAesKey());
        } catch (Exception ex) {
            logger.error("addRolesDocType - username: "
                    + userGroup.getCardId() + " - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    /**
     * Update user dc phan quyen
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String updateRolesDocType(HttpServletRequest request,
            String data, String isSecurity) {

        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            logger.error("addRolesDocType - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        try {
            Long userId = userGroup.getVof2_ItemEntityUser().getUserId();
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[]{
                "listUserRolesDocType"
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Thong tin bien ban hop
            String strLstDocRoles = listValue.get(0);
            GsonBuilder builder = new GsonBuilder().setDateFormat("dd/MM/yyyy");
            Gson gson = builder.create();
            List<EntityFinancialRecordsRoles> lstDocTypeRoles = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(strLstDocRoles);
            for(int i = 0; i < jsonArray.length(); i++){
                lstDocTypeRoles.add(gson.fromJson(jsonArray.getString(i), EntityFinancialRecordsRoles.class));
            }
            StoreTypeConfigDAO dao = new StoreTypeConfigDAO();
            boolean result = dao.updateDocTypeRoles(userId, lstDocTypeRoles);
//            LogUtils.logFunctionalEnd(userGroup.getKpiLog());
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result,
                    userGroup.getStrAesKey());
        } catch (Exception ex) {
            logger.error("addRolesDocType - username: "
                    + userGroup.getCardId() + " - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    /*Lay danh sach user dc phan quyen*/
    public String getListUserDocTypeRoles(String isSecurity, String strData, HttpServletRequest req) throws JSONException {
        String[] keys;
        keys = new String[]{
            "fullname",
            "sysOrgId",
            "email",
            "startTime",
            "endTime",
            "empId",
            "docTypeId",
            "isSearch",
            "employeeCode",
            "strQuickSearch",
            "isRead",
            "isDown"
        };
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(req, strData, keys);
        // Session timeout
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("getListBrief - Session timeout!");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        try {
            //lay gia tri luu trong seasion
            Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
            List<String> listValue = dataSessionGR.getListParamsFromClient();
            
            String fullname = listValue.get(0);
            String sysOrgId = listValue.get(1);
            String email = listValue.get(2);   
            String startTime = listValue.get(3);
            String endTime = listValue.get(4);
            
            Long empId = null;
            if(!CommonUtils.isEmpty(listValue.get(5))){
                empId = Long.valueOf(listValue.get(5));
            }
            //Hinh thuc van ban
            Long docTypeId = null;
            if(!CommonUtils.isEmpty(listValue.get(6))){
                if(!CommonUtils.isEmpty(listValue.get(6))){
                    docTypeId = Long.valueOf(listValue.get(6));
                }
            }
            
            Integer isSearch = null;
            if(!CommonUtils.isEmpty(listValue.get(7))){
                isSearch = Integer.valueOf(listValue.get(7));
            }
            String employeeCode = listValue.get(8);
            String strQuickSearch = listValue.get(9);
            //isRead
            Long isRead = null;
            if(!CommonUtils.isEmpty(listValue.get(10))){
                isRead = Long.valueOf(listValue.get(10));
            }
            //isDown
            Long isDown = null;
            if(!CommonUtils.isEmpty(listValue.get(11))){
                isDown = Long.valueOf(listValue.get(11));
            }
           
            StoreTypeConfigDAO dao = new StoreTypeConfigDAO();
            List<EntityFinancialRecordsRoles> result = dao.getUserDocTypeRoles(userId, fullname, sysOrgId, email, startTime,
                    endTime, empId, docTypeId ,isSearch, employeeCode, strQuickSearch, isRead, isDown);

            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
        } catch (NumberFormatException ex) {
            logger.error("getListUserDocTypeRoles - Exception:", ex);
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
    }
    
    
    /**
     * Xoa user dc phan quyen
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String deleteUserDocRoles(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Lay thong tin user
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
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
                    "storeDocumentRoleId"
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long storeDocumentRoleId = Long.parseLong(listValue.get(0));
            StoreTypeConfigDAO dao = new StoreTypeConfigDAO();
            Integer rs = dao.deleteUserDocRoles(storeDocumentRoleId, user);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalStart(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, rs, aesKey);
        } catch (JSONException | NumberFormatException ex) {
            logger.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, null, null);
        }
    }
    
    public String getUserEntity(String isSecurity,
            String data, HttpServletRequest request) throws JSONException {
        String[] keys = new String[]{
                "sysUserId"
        };
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
        try {
            JSONObject json = new JSONObject(data);
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long sysUserId = Long.parseLong(listValue.get(0));
            
            StoreTypeConfigDAO stc = new StoreTypeConfigDAO();
            List<EntityVhrEmployee> result = stc.getUserEntity(sysUserId);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (NumberFormatException ex) {
            logger.error("getListDocType - Exception:", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, "", aesKey);
        }
    }
    
//    public String getListFinancialDoc(String isSecurity, String strData, HttpServletRequest req) throws JSONException {
//        String[] keys;
//        keys = new String[]{
////            "fullname",
////            "sysOrgId",
////            "email",
////            "startTime",
////            "endTime",
////            "empId",
////            "docTypeId",
//            "isSearch"
////            "employeeCode",
////            "strQuickSearch",
////            "isRead",
////            "isDown"
//        };
//        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(req, strData, keys);
//        // Session timeout
//        if (!dataSessionGR.getCheckSessionOk()) {
//            logger.error("getListBrief - Session timeout!");
//            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
//        }
//        try {
//            //lay gia tri luu trong seasion
//            Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
//            List<String> listValue = dataSessionGR.getListParamsFromClient();
//            
////            String fullname = listValue.get(0);
////            String sysOrgId = listValue.get(1);
////            String email = listValue.get(2);   
////            String startTime = listValue.get(3);
////            String endTime = listValue.get(4);
////            
////            Long empId = null;
////            if(!CommonUtils.isEmpty(listValue.get(5))){
////                empId = Long.valueOf(listValue.get(5));
////            }
////            //Hinh thuc van ban
////            List<Long> docTypeId = new ArrayList<>();
////            if(!CommonUtils.isEmpty(listValue.get(6))){
////                JSONArray jsonArray = new JSONArray(listValue.get(6));
////                for (int i = 0; i < jsonArray.length(); i++) {
////                    docTypeId.add(jsonArray.getLong(i));
////                }
////            }
//            
//            Integer isSearch = null;
//            if(!CommonUtils.isEmpty(listValue.get(0))){
//                isSearch = Integer.valueOf(listValue.get(0));
//            }
////            String employeeCode = listValue.get(8);
////            String strQuickSearch = listValue.get(9);
////            //isRead
////            Long isRead = null;
////            if(!CommonUtils.isEmpty(listValue.get(10))){
////                isRead = Long.valueOf(listValue.get(10));
////            }
////            //isDown
////            Long isDown = null;
////            if(!CommonUtils.isEmpty(listValue.get(11))){
////                isDown = Long.valueOf(listValue.get(11));
////            }
////           
//            StoreTypeConfigDAO dao = new StoreTypeConfigDAO();
//            List<EntityDocument> result = dao.getListFinancialDoc(userId,isSearch);
//
//            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
//        } catch (NumberFormatException ex) {
//            logger.error("getListUserDocTypeRoles - Exception:", ex);
//            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
//        }
//    }
    
    public String getUserDocRolesByEmpId(String isSecurity,
            String data, HttpServletRequest request) {
        String[] keys = new String[]{
                "employeeId"
        };
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);

        // Session timeout
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("getListUserDocRoles - Session timeout!");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        try {
          List<String> listValue = dataSessionGR.getListParamsFromClient();
          Long employeeId = null;
          if(!CommonUtils.isEmpty(listValue.get(0))){
              employeeId = Long.valueOf(listValue.get(0));
          }
            StoreTypeConfigDAO stc = new StoreTypeConfigDAO();
            Object result = stc.getUserDocRolesByEmpId(employeeId);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
        } catch (NumberFormatException ex) {
            logger.error("getListConfigDao - Exception:", ex);
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
    }

}
