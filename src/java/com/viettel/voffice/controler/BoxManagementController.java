/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.boxmanagement.BoxManagementDAO;
import com.viettel.voffice.database.dao.storagemanagement.StorageManagementDAO;
import com.viettel.voffice.database.entity.EntityCommonFields;
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
public class BoxManagementController {

    /**
     * Log loi
     */
    private static final Logger logger = Logger.getLogger(BoxManagementController.class);

    private static final int RETURN_TRUE = 1;


    /*Lay danh sach hop*/
    public String getBoxs(String isSecurity, String strData, HttpServletRequest req) {
        String[] keys = new String[]{
            ConstantsFieldParams.STR_KEYWORDS,
            ConstantsFieldParams.START_RECORD,
            ConstantsFieldParams.PAGE_SIZE,
            ConstantsFieldParams.IS_COUNT,
            ConstantsFieldParams.SHELVE_ID
        };
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(req, strData, keys);
        StorageManagementDAO storages = new StorageManagementDAO();
        // Session timeout
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("getBoxs - Session timeout!");
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
            String shelveId = listValue.get(4);

            BoxManagementDAO boxs = new BoxManagementDAO();
            Object result = boxs.getBoxs(userId, orgIdsByRole, strSearch, startRecord, pageSize, isCount, shelveId);

            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
        } catch (Exception ex) {
            logger.error("getBoxs - Exception:", ex);
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
    }

    /**
     * <b>Xoa hop</b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String deleteBoxs(HttpServletRequest request,
            String data, String isSecurity) {
        String[] keys = new String[]{ConstantsFieldParams.BOX_ID};
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);
        // Khong co session
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("deleteBox - No session");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            logger.error("deleteBox - userId2 null");
            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
        }
        try {
            List<String> listValue = dataSessionGR.getListParamsFromClient();
            Long boxId = Long.parseLong(listValue.get(0));
            BoxManagementDAO m = new BoxManagementDAO();
            Integer result = m.deleteBoxs(boxId, userId);

            if (RETURN_TRUE == result) {
                return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
            }
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);

        } catch (Exception ex) {
            logger.error("deleteBox - Exception:", ex);
            return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * add Or Edit Boxs
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String addOrEditBoxs(HttpServletRequest request,
            String data, String isSecurity) {

        String[] keys = new String[]{
            ConstantsFieldParams.BOX_ID,
            ConstantsFieldParams.BOX_NAME,
            ConstantsFieldParams.SHELVE_ID,
            ConstantsFieldParams.FLOOR_ID,
            ConstantsFieldParams.STORAGE_ID,
            ConstantsFieldParams.DESCRIPTION,
            ConstantsFieldParams.STATUS,};

        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);
        // Khong co session
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("addOrEditBox - No session");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            logger.error("addOrEditBoxs - userId null");
            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
        }
        try {
            List<String> listValue = dataSessionGR.getListParamsFromClient();
            Long boxId = null;
            Long shelveId = null;
            Long floorId = null;
            Long storageId = null;
            Integer status = 0;

            try {
                if (!CommonUtils.isEmpty(listValue.get(0))) {
                    boxId = Long.parseLong(listValue.get(0));
                }
                if (!CommonUtils.isEmpty(listValue.get(2))) {
                    shelveId = Long.parseLong(listValue.get(2));
                }
                if (!CommonUtils.isEmpty(listValue.get(3))) {
                    floorId = Long.parseLong(listValue.get(3));
                }
                if (!CommonUtils.isEmpty(listValue.get(4))) {
                    storageId = Long.parseLong(listValue.get(4));
                }
                if (!CommonUtils.isEmpty(listValue.get(6))) {
                    status = Integer.valueOf(listValue.get(6));
                }
            } catch (NumberFormatException ex) {
                logger.error("addOrEditBoxs - Exception:", ex);
            }

            String boxName = listValue.get(1);
            String description = listValue.get(5);

            BoxManagementDAO box = new BoxManagementDAO();
            Integer rs;
            if (boxId == null) {
                //Thêm mới hop
                rs = box.addBoxs(boxName, shelveId, floorId, storageId, description, status, userId);
            } else {
                //Sửa định hop
                rs = box.editBoxs(boxId, boxName, shelveId, floorId, storageId, description, status, userId);
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, rs, dataSessionGR);

        } catch (Exception ex) {
            logger.error("addOrEditBoxs - Exception:", ex);
            return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * <b>Lay danh sach thuoc tinh</b><br>
     * <ul>
     * <li>0: tat ca</li>
     * <li>1: hinh thuc van ban</li>
     * <li>2: do mat</li>
     * <li>3: linh vuc</li>
     * <li>4: do khan</li>
     * </ul>
     *
     * @param isSecurity
     * @param data
     * @param request
     * @return
     */
    public String getListForCombobox(String isSecurity, String data,
            HttpServletRequest request) {
        String[] keys = new String[]{
            ConstantsFieldParams.TYPE,
            ConstantsFieldParams.TYPE_ID
        };
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);
        StorageManagementDAO storages = new StorageManagementDAO();
        Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
        // Session timeout
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("getListForCombobox - Session timeout!");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        try {
            List<Long> orgIdsByRole = new ArrayList<>();
            // Lay danh sach don vi sach don vi user co quyen van thu
            List<Long> listSecretaryVhrOrg = storages.checkSysRole(userId);
            if (listSecretaryVhrOrg != null && listSecretaryVhrOrg.size() > 0) {
                orgIdsByRole.addAll(listSecretaryVhrOrg);
            }
            
            List<String> listValue = dataSessionGR.getListParamsFromClient();
            Long type = 0L;
            if (!CommonUtils.isEmpty(listValue.get(0))) {
                type = Long.parseLong(listValue.get(0));
            }
            Integer typeId = null;
            if (!CommonUtils.isEmpty(listValue.get(1))) {
                typeId = Integer.parseInt(listValue.get(1));
            }
            BoxManagementDAO box = new BoxManagementDAO();
            List<EntityCommonFields> result = box.getListForCombobox(userId, type, typeId, orgIdsByRole);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
        } catch (Exception ex) {
            logger.error("getListForCombobox - Exception - username: " + dataSessionGR.getCardId(), ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }
    
    /*Check ten hop*/
    public String checkBoxExist(HttpServletRequest req, String strData, String isSecurity) {

        String[] keys = new String[]{
            ConstantsFieldParams.BOX_ID,
            ConstantsFieldParams.BOX_NAME,
            ConstantsFieldParams.SHELVE_ID,
            ConstantsFieldParams.STORAGE_ID,};

        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(req, strData, keys);
        // Khong co session
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("checkBoxExist - No session");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            logger.error("checkBoxExist - userId null");
            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
        }

        List<String> listValue = dataSessionGR.getListParamsFromClient();
        Long boxId = 0L;
        Long shelveId = 0L;   
        Long storageId = 0L;
        try {
            if (!CommonUtils.isEmpty(listValue.get(0))) {
                boxId = Long.parseLong(listValue.get(0));
            }
            if (!CommonUtils.isEmpty(listValue.get(2))) {
                shelveId = Long.parseLong(listValue.get(2));
            }
            if (!CommonUtils.isEmpty(listValue.get(3))) {
                storageId = Long.parseLong(listValue.get(3));
            }
                
         } catch (NumberFormatException ex) {
                logger.error("checkBoxExist - Exception:", ex);
         }

         String boxName = listValue.get(1);
         BoxManagementDAO box = new BoxManagementDAO();
         return FunctionCommon.responseResult(ErrorCode.SUCCESS, box.checkBoxExist(boxId, boxName, shelveId, storageId), dataSessionGR);
    }
    
}
