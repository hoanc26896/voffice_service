/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.shelvemanagement.ShelveManagementDAO;
import com.viettel.voffice.database.dao.storagemanagement.StorageManagementDAO;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.utils.CommonUtils;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;

/**
 *
 * @author pm1_os20
 */
public class ShelveManagementController {

    /**
     * Log loi
     */
    private static final Logger logger = Logger.getLogger(ShelveManagementController.class);

    private static final int RETURN_TRUE = 1;


    /*Lay danh sach du lieu*/
    public String getShelve(String isSecurity, String strData, HttpServletRequest req) {
        String[] keys = new String[]{
            ConstantsFieldParams.STR_KEYWORDS,
            ConstantsFieldParams.START_RECORD,
            ConstantsFieldParams.PAGE_SIZE,
            ConstantsFieldParams.IS_COUNT,
            ConstantsFieldParams.STORAGE_ID
        };
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(req, strData, keys);
        StorageManagementDAO storages = new StorageManagementDAO();
        // Session timeout
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("getShelve - Session timeout!");
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

            String strSearch = listValue.get(0);
            Long startRecord = 0L;
            if (!CommonUtils.isEmpty(listValue.get(1))) {
                startRecord = Long.parseLong(listValue.get(1));
            }
            Long pageSize = null;
            if (!CommonUtils.isEmpty(listValue.get(2))) {
                pageSize = Long.parseLong(listValue.get(2));
            }
            Integer isCount = 0;
            if (!CommonUtils.isEmpty(listValue.get(3))) {
                isCount = Integer.valueOf(listValue.get(3));
            }
            
            String storageId = listValue.get(4);

            ShelveManagementDAO bhelve = new ShelveManagementDAO();
            Object result = bhelve.getShelve(userId, orgIdsByRole, strSearch, startRecord, pageSize, isCount, storageId);

            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
        } catch (Exception ex) {
            logger.error("getShelve - Exception:", ex);
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
    }

    /**
     * <b>Xoa du lieu</b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String deleteShelve(HttpServletRequest request,
            String data, String isSecurity) {
        String[] keys = new String[]{ConstantsFieldParams.SHELVE_ID};
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);
        // Khong co session
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("deleteShelve - No session");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            logger.error("deleteShelve - userId2 null");
            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
        }
        try {
            List<String> listValue = dataSessionGR.getListParamsFromClient();
            Long boxId = Long.parseLong(listValue.get(0));
            ShelveManagementDAO m = new ShelveManagementDAO();
            Integer result = m.deleteShelve(boxId, userId);

            if (RETURN_TRUE == result) {
                return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
            }
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);

        } catch (Exception ex) {
            logger.error("deleteShelve - Exception:", ex);
            return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * add Or Edit Shelve
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String addOrEditShelve(HttpServletRequest request,
            String data, String isSecurity) {

        String[] keys = new String[]{
            ConstantsFieldParams.SHELVE_ID,
            ConstantsFieldParams.SHELVE_NAME,
            ConstantsFieldParams.DESCRIPTION,
            ConstantsFieldParams.STORAGE_ID,
            ConstantsFieldParams.NUMBER_FLOOR,            
            ConstantsFieldParams.STATUS};

        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);
        // Khong co session
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("addOrEditShelve - No session");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            logger.error("addOrEditShelve - userId null");
            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
        }
        try {
            List<String> listValue = dataSessionGR.getListParamsFromClient();
            Long shelveId = null;            
            Long storageId = null;
            Long numberFloor = null;
            Integer status = 0;

            try {
                if (!CommonUtils.isEmpty(listValue.get(0))) {
                    shelveId = Long.parseLong(listValue.get(0));
                }               
                if (!CommonUtils.isEmpty(listValue.get(3))) {
                    storageId = Long.parseLong(listValue.get(3));
                }
                if (!CommonUtils.isEmpty(listValue.get(4))) {
                    numberFloor = Long.parseLong(listValue.get(4));
                }
                if (!CommonUtils.isEmpty(listValue.get(5))) {
                    status = Integer.valueOf(listValue.get(5));
                }
            } catch (NumberFormatException ex) {
                logger.error("addOrEditShelve - Exception:", ex);
            }

            String shelveName = listValue.get(1);
            String description = listValue.get(2);

            ShelveManagementDAO box = new ShelveManagementDAO();
            Integer rs;
            // Them moi hop
            if (shelveId == null) {
                rs = box.addShelve(shelveName, description, storageId, numberFloor,
                        status, userId);
            } // Chinh sua hop
            else {
                rs = box.editShelve(shelveId, shelveName, description, storageId,
                        numberFloor, status, userId);
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, rs, dataSessionGR);

        } catch (Exception ex) {
            logger.error("addOrEditShelve - Exception:", ex);
            return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
        }
    }
     /*Check ke*/
    public String checkShelveExist(HttpServletRequest req, String strData, String isSecurity) {

        String[] keys = new String[]{
            ConstantsFieldParams.SHELVE_ID,
            ConstantsFieldParams.SHELVE_NAME,
            ConstantsFieldParams.STORAGE_ID,};

        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(req, strData, keys);
        // Khong co session
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("checkShelveExist - No session");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            logger.error("checkShelveExist - userId null");
            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
        }

            List<String> listValue = dataSessionGR.getListParamsFromClient();
            Long shelveId = 0L;
            Long storageId =0L;

            try {
                if (!CommonUtils.isEmpty(listValue.get(0))) {
                    shelveId = Long.parseLong(listValue.get(0));
                }
                if (!CommonUtils.isEmpty(listValue.get(2))) {
                    storageId = Long.parseLong(listValue.get(2));
                }
                
            } catch (NumberFormatException ex) {
                logger.error("addOrEditBoxs - Exception:", ex);
            }

            String shelveName = listValue.get(1);
            ShelveManagementDAO shelve = new ShelveManagementDAO();
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, shelve.checkShelveExist(shelveId, shelveName, storageId), dataSessionGR);

    }
     /*Check so tang ke*/
    public String checkShelveNumFloor(HttpServletRequest req, String strData, String isSecurity) {

        String[] keys = new String[]{
            ConstantsFieldParams.SHELVE_ID,
            ConstantsFieldParams.NUMBER_FLOOR,};

        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(req, strData, keys);
        // Khong co session
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("checkShelveExist - No session");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            logger.error("checkShelveExist - userId null");
            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
        }

            List<String> listValue = dataSessionGR.getListParamsFromClient();
            Long shelveId = 0L;
            Long numberFloor =0L;

            try {
                if (!CommonUtils.isEmpty(listValue.get(0))) {
                    shelveId = Long.parseLong(listValue.get(0));
                }
                if (!CommonUtils.isEmpty(listValue.get(1))) {
                    numberFloor = Long.parseLong(listValue.get(1));
                }
                
            } catch (NumberFormatException ex) {
                logger.error("addOrEditBoxs - Exception:", ex);
            }

            ShelveManagementDAO shelve = new ShelveManagementDAO();
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, shelve.checkShelveNumFloor(shelveId, numberFloor), dataSessionGR);

    }
}
