/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.storagemanagement.StorageManagementDAO;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.EntityVhrOrg;
import com.viettel.voffice.utils.CommonUtils;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;

/**
 *
 * @author pm1_os30
 */
public class StorageManagementController {

    /**
     * Log loi
     */
    private static final Logger logger = Logger.getLogger(StorageManagementController.class);

    private static final int RETURN_TRUE = 1;

    /*Lay danh sach kho*/
    public String getStorages(String isSecurity, String strData, HttpServletRequest req) {
        String[] keys = new String[]{
            ConstantsFieldParams.STR_KEYWORDS,
            ConstantsFieldParams.ORG_ID,
            ConstantsFieldParams.STORAGE_NAME,
            ConstantsFieldParams.LOCATIONS,
            ConstantsFieldParams.DESCRIPTION,
            ConstantsFieldParams.IS_ADVANCE,
            ConstantsFieldParams.IS_COUNT,
            ConstantsFieldParams.START_RECORD,
            ConstantsFieldParams.PAGE_SIZE
        };
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(req, strData, keys);
        StorageManagementDAO storages = new StorageManagementDAO();

        // Session timeout
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("getStorages - Session timeout!");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        try {
            //lay gia tri luu trong seasion
            Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
            List<Long> orgIdsByRole = new ArrayList<>();

            // Lay danh sach don vi sach don vi user co quyen van thu
            List<Long> listSecretaryVhrOrg = storages.checkSysRole(userId);
            if (listSecretaryVhrOrg != null && listSecretaryVhrOrg.size() > 0) {
                orgIdsByRole.addAll(listSecretaryVhrOrg);
            }

            List<String> listValue = dataSessionGR.getListParamsFromClient();

            String dataSearch = listValue.get(0);
            
            /*Long orgId = null;
            if (!CommonUtils.isEmpty(listValue.get(1))) {
                orgId = Long.parseLong(listValue.get(1));
            }*/
            String orgId = listValue.get(1);
            String storageName = listValue.get(2);
            String locations = listValue.get(3);
            String description = listValue.get(4);
            Long isAdvance = null;
            if (!CommonUtils.isEmpty(listValue.get(5))) {
                isAdvance = Long.parseLong(listValue.get(5));
            }
            Integer isCount = 0;
            if (!CommonUtils.isEmpty(listValue.get(6))) {
                isCount = Integer.valueOf(listValue.get(6));
            }
            Long startRecord = 0L;
            if (!CommonUtils.isEmpty(listValue.get(7))) {
                startRecord = Long.parseLong(listValue.get(7));
            }
            Long pageSize = null;
            if (!CommonUtils.isEmpty(listValue.get(8))) {
                pageSize = Long.parseLong(listValue.get(8));
            }
            
            
            Object result = storages.getStorages(userId, orgIdsByRole, storageName, locations, orgId, description, isAdvance, dataSearch, isCount, startRecord, pageSize);

            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
        } catch (Exception ex) {
            logger.error("getStorages - Exception:", ex);
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
    }
    /*Lay danh don vi*/
    public String getOrgList(String isSecurity, String strData, HttpServletRequest req) {
        String[] keys = new String[]{
            ConstantsFieldParams.STR_KEYWORDS,
        };
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(req, strData, keys);
        StorageManagementDAO storages = new StorageManagementDAO();
        // Session timeout
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("getOrgList - Session timeout!");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        try {
            //lay gia tri luu trong seasion
            Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
             List<Long> orgIdsByRole = new ArrayList<>();
            // Lay danh sach don vi sach don vi user co quyen van thu
            List<Long> listSecretaryVhrOrg = storages.checkSysRole(userId);
            if (listSecretaryVhrOrg != null && listSecretaryVhrOrg.size() > 0) {
                orgIdsByRole.addAll(listSecretaryVhrOrg);
            }
            List<String> listValue = dataSessionGR.getListParamsFromClient();

            String dataSearch = listValue.get(0);

            Object result = storages.getOrgList(userId, orgIdsByRole, dataSearch);

            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
        } catch (Exception ex) {
            logger.error("getOrgList - Exception:", ex);
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
    }
    /**
     * <b>Xoa kho</b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String deleteStorages(HttpServletRequest request,
            String data, String isSecurity) {
        String[] keys = new String[]{ConstantsFieldParams.STORAGE_ID};
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);
        // Khong co session
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("deleteStorages - No session");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            logger.error("deleteStorages - userId2 null");
            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
        }
        try {
            List<String> listValue = dataSessionGR.getListParamsFromClient();
            Long storageId = Long.parseLong(listValue.get(0));
            StorageManagementDAO m = new StorageManagementDAO();
            Integer result = m.deleteStorages(storageId, userId);

            if (RETURN_TRUE == result) {
                return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
            }
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        } catch (Exception ex) {
            logger.error("deleteStorages - Exception:", ex);
            return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String addOrEditStorages(HttpServletRequest request, String data, String isSecurity) {

        String[] keys = new String[]{
            ConstantsFieldParams.STORAGE_ID,
            ConstantsFieldParams.STORAGE_NAME,
            ConstantsFieldParams.LOCATIONS,
            ConstantsFieldParams.ORG_ID,
            ConstantsFieldParams.DESCRIPTION,
            ConstantsFieldParams.STATUS,};

        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);
        // Khong co session
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("addOrEditStorages - No session");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            logger.error("addOrEditStorages - userId2 null");
            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
        }

        try {
            List<String> listValue = dataSessionGR.getListParamsFromClient();
            Long storageId = null;
            String orgId = "";
            Integer status = 0;

            try {
                if (!CommonUtils.isEmpty(listValue.get(0))) {
                    storageId = Long.parseLong(listValue.get(0));
                }
                if (!CommonUtils.isEmpty(listValue.get(3))) {
                    orgId = listValue.get(3);
                }
                if (!CommonUtils.isEmpty(listValue.get(5))) {
                    status = Integer.valueOf(listValue.get(5));
                }
            } catch (NumberFormatException ex) {
                logger.error("addOrEditStorages - Exception:", ex);
            }
            String storageName = listValue.get(1);
            String locations = listValue.get(2);
            String description = listValue.get(4);
            StorageManagementDAO storage = new StorageManagementDAO();
            Integer rs;
            // Them moi kho
            if (storageId == null) {
                rs = storage.addStorages(storageName, locations, orgId, description,
                        status, userId);
            }// Chinh sua kho
            else {
                rs = storage.editStorages(storageId, storageName, locations, orgId,
                        description, status, userId);
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, rs, dataSessionGR);
        } catch (Exception ex) {
            logger.error("addOrEditStorages - Exception:", ex);
            return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
        }
    }
     /*Check ten kho*/
    public String checkStorageExist(HttpServletRequest req, String strData, String isSecurity) {

        String[] keys = new String[]{
            ConstantsFieldParams.STORAGE_ID,
            ConstantsFieldParams.STORAGE_NAME,};

        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(req, strData, keys);
        // Khong co session
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("checkStorageExist - No session");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            logger.error("checkStorageExist - userId null");
            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
        }

            List<String> listValue = dataSessionGR.getListParamsFromClient();
            Long storageId = 0L;
           

            try {
                if (!CommonUtils.isEmpty(listValue.get(0))) {
                    storageId = Long.parseLong(listValue.get(0));
                }
                
            } catch (NumberFormatException ex) {
                logger.error("addOrEditBoxs - Exception:", ex);
            }

            String storageName = listValue.get(1);
            StorageManagementDAO storage = new StorageManagementDAO();
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, storage.checkStorageExist(storageId, storageName), dataSessionGR);
    }

    /*Lay danh sach bao cao kho*/
    public String getStorageReport(String isSecurity, String strData, HttpServletRequest req) {
        String[] keys = new String[]{
            ConstantsFieldParams.STR_KEYWORDS,
            ConstantsFieldParams.ORG_ID,
            ConstantsFieldParams.STORAGE_NAME,
            ConstantsFieldParams.LOCATIONS,
            ConstantsFieldParams.DESCRIPTION,
            ConstantsFieldParams.IS_ADVANCE,
            ConstantsFieldParams.IS_COUNT,
            ConstantsFieldParams.START_RECORD,
            ConstantsFieldParams.PAGE_SIZE
        };
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(req, strData, keys);
        StorageManagementDAO storages = new StorageManagementDAO();
        // Session timeout
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("getStorageReport - Session timeout!");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        try {
            //lay gia tri luu trong seasion
            Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
             List<Long> orgIdsByRole = new ArrayList<>();
            // Lay danh sach don vi sach don vi user co quyen van thu
            List<Long> listSecretaryVhrOrg = storages.checkSysRole(userId);
            if (listSecretaryVhrOrg != null && listSecretaryVhrOrg.size() > 0) {
                orgIdsByRole.addAll(listSecretaryVhrOrg);
            }
            List<String> listValue = dataSessionGR.getListParamsFromClient();

            String dataSearch = listValue.get(0);
            String orgId = listValue.get(1);
            String storageName = listValue.get(2);
            String locations = listValue.get(3);
            String description = listValue.get(4);
            Long isAdvance = null;
            if (!CommonUtils.isEmpty(listValue.get(5))) {
                isAdvance = Long.parseLong(listValue.get(5));
            }
            Object result = storages.getStorageReport(userId, orgIdsByRole, storageName, locations, orgId, description, isAdvance, dataSearch);

            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
        } catch (Exception ex) {
            logger.error("getStorageReport - Exception:", ex);
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
    }
    
     /*Check quyen luu tru*/
    public String checkStoragePermissions(HttpServletRequest req, String strData, String isSecurity) {
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(req, strData, null);
        // Khong co session
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("checkStorageExist - No session");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }

        // Kiem tra user id tren he thong 2
        Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            logger.error("checkStorageExist - userId null");
            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
        }

        StorageManagementDAO storage = new StorageManagementDAO();
        List<Long> roles = storage.checkSysRole(userId);
        int result = 0;
        if (roles != null && !roles.isEmpty()) {           
           result = RETURN_TRUE;           
        }
        return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
    }

     /*danh sach don vi quyen luu tru*/
    public String getListSysRoleOrg(HttpServletRequest req, String strData, String isSecurity) {
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(req, strData, null);
        // Khong co session
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("getListSysRoleOrg - No session");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }

        // Kiem tra user id tren he thong 2
        Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            logger.error("getListSysRoleOrg - userId null");
            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
        }

        StorageManagementDAO storage = new StorageManagementDAO();
        List<EntityVhrOrg> result = storage.getOrgListRoleByUserId(userId);       
        return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
    }
    // DuanNV them Danh sach don vi danh gia don vi
      /*danh sach don vi quyen luu tru*/
    public String getListSysRoleEvaluate(HttpServletRequest req, String strData, String isSecurity) {
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(req, strData, null);
        // Khong co session
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("getListSysRoleOrg - No session");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }

        // Kiem tra user id tren he thong 2
        Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            logger.error("getListSysRoleOrg - userId null");
            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
        }

        StorageManagementDAO storage = new StorageManagementDAO();
        List<EntityVhrOrg> result = storage.getListSysRoleEvaluate(userId);       
        return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
    }
    // End.
}
