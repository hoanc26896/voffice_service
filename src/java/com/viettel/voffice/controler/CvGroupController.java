/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.staff.CvGroupDAO;
import com.viettel.voffice.database.dao.staff.StaffDAO;
import com.viettel.voffice.database.entity.EntityGroupSign;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityRoleInCvGroup;
import com.viettel.voffice.database.entity.EntityStaffInCvGroup;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.User.EntityCvGroup;
import com.viettel.voffice.database.entity.User.EntityUser;
import com.viettel.voffice.database.entity.User.StaffInCvGroupResult;
import com.viettel.voffice.database.entity.User.Vof2_EntityUser;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.LogUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author vinhnq13
 */
public class CvGroupController {

    public static final String ROOT_ACTION = "CvGroupController";

    /**
     * Ten cua class bao gom ca ten package
     */
    private static final String CLASS_NAME = CvGroupController.class.getName();

    /**
     * Log file
     */
    private static final Logger LOGGER = Logger.getLogger(CvGroupController.class);

    /**
     * <b>Tim kiem danh sach nhom nhan vien</b>
     *
     * @author cuongnv
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListGroup(HttpServletRequest request,
            String data, String isSecurity) {

        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            LOGGER.error("getListGroup - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        // ID user tren he thong 2
        Long userId2 = userGroup.getUserId2();
        if (userId2 == 0L) {
            LOGGER.error("getListGroup - Loi khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Danh sach ID don vi user tren he thong 2
        List<Long> listGroupId2 = userGroup.getVof2_ItemEntityUser().getLstVhrOrgAll();
        // Lay userId tren he thong 1
        EntityUser user = userGroup.getItemEntityUser();
        Long userId1 = null;
        List<Long> listGroupId1 = new ArrayList<>();
        if (user != null) {
            // ID user tren he thong 1
            userId1 = user.getUserId();
            // Danh sach ID don vi user tren he thong 1
            listGroupId1 = userGroup.getItemEntityUser().getLstGroupAll();
        }
        try {
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[]{
                ConstantsFieldParams.KEYWORD,
                ConstantsFieldParams.GET_CV_GROUP_PRIVATE,
                ConstantsFieldParams.GET_CV_GROUP_PUBLIC,
                ConstantsFieldParams.GET_CV_GROUP,
                ConstantsFieldParams.CV_GROUP_GROUPTYPE,
                ConstantsFieldParams.START_RECORD,
                ConstantsFieldParams.PAGE_SIZE
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Tim kiem ten nhom
            String keyword = listValue.get(0);

            Long cvGroupPrivate = 0L;
            if (!CommonUtils.isEmpty(listValue.get(1))) {
                cvGroupPrivate = Long.parseLong(listValue.get(1));
            }
            Long cvGroupPublic = 0L;
            if (!CommonUtils.isEmpty(listValue.get(2))) {
                cvGroupPublic = Long.parseLong(listValue.get(2));
            }
            Long cvGroup = 0L;
            if (!CommonUtils.isEmpty(listValue.get(3))) {
                cvGroup = Long.parseLong(listValue.get(3));
            }
            Integer groupType = 0;
            if (!CommonUtils.isEmpty(listValue.get(4))) {
                groupType = Integer.parseInt(listValue.get(4));
            }
            // Vi tri lay ra
            Long startRecord = null;
            String strStartRecord = listValue.get(5);
            if (!CommonUtils.isEmpty(strStartRecord)) {
                startRecord = Long.parseLong(strStartRecord);
            }
            Long pageSize = null;
            String strPageSize = listValue.get(6);
            if (!CommonUtils.isEmpty(strPageSize)) {
                pageSize = Long.parseLong(strPageSize);
            }
            List<EntityCvGroup> rs;
            if (groupType.equals(0)) {
                StaffDAO staffDAO = new StaffDAO();
                rs = staffDAO.getListGroupByStaffId(userId1, userId2, listGroupId1,
                        listGroupId2, keyword, cvGroupPublic, cvGroupPrivate, cvGroup);
            } else {
                //Lay danh sach nhom van ban theo code cua OS
                CvGroupDAO cv = new CvGroupDAO();
                rs = cv.getListGroup(keyword, cvGroupPrivate, cvGroupPublic,
                        cvGroup, groupType, userGroup, startRecord, pageSize);
            }
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(userGroup.getKpiLog());
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, rs, userGroup.getStrAesKey());
        } catch (Exception ex) {
            LOGGER.error("getListGroup - username: " + userGroup.getCardId()
                    + " - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     *
     * <b>Tim kiem so luong nhom nhan vien</b>
     *
     * @author cuongnv
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getCountListGroup(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
//        // Lay userId tren he thong 1
//        EntityUser user = userGroup.getItemEntityUser();
        //Id user vof2
        Long userIdVof2 = userGroup.getVof2_ItemEntityUser().getUserId();
        if (userIdVof2 == null) {
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }

        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        // Lay ma nhan vien
        String cardId = user2.getStrCardNumber();
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
                ConstantsFieldParams.KEYWORD,
                ConstantsFieldParams.GET_CV_GROUP_PRIVATE,
                ConstantsFieldParams.GET_CV_GROUP_PUBLIC,
                ConstantsFieldParams.GET_CV_GROUP,
                ConstantsFieldParams.CV_GROUP_GROUPTYPE
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Tim kiem ten nhom
            String keyword = listValue.get(0);

            Long cvGroupPrivate = 0L;
            if (!CommonUtils.isEmpty(listValue.get(1))) {
                cvGroupPrivate = Long.parseLong(listValue.get(1));
            }
            Long cvGroupPublic = 0L;
            if (!CommonUtils.isEmpty(listValue.get(2))) {
                cvGroupPublic = Long.parseLong(listValue.get(2));
            }
            Long cvGroup = 0L;
            if (!CommonUtils.isEmpty(listValue.get(3))) {
                cvGroup = Long.parseLong(listValue.get(3));
            }
            Integer groupType = -1;
            if (!CommonUtils.isEmpty(listValue.get(4))) {
                groupType = Integer.parseInt(listValue.get(4));
            }

            CvGroupDAO cv = new CvGroupDAO();
            List<EntityCvGroup> rs = cv.getListGroup(keyword, cvGroupPrivate, cvGroupPublic, cvGroup, groupType, userGroup, null, null);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, rs.size(), aesKey);
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error(ex.getMessage(), ex);
            LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_ERROR, ErrorCode.INPUT_INVALID, ex.getMessage(), data, isSecurity);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     *
     * Tim kiem nhan vien theo nhom nhan vien
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListStaffOfGroup(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Lay userId tren he thong 1
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
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
                ConstantsFieldParams.TASK_GROUP_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);

            Long cvGroupId = null;
            if (!CommonUtils.isEmpty(listValue.get(0))) {
                cvGroupId = Long.parseLong(listValue.get(0));
            }

            StaffDAO staffDAO = new StaffDAO();
            Object result = staffDAO.getListStaffOfGroup(cvGroupId);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error(ex.getMessage(), ex);
            LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_ERROR, ErrorCode.INPUT_INVALID, ex.getMessage(), data, isSecurity);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * <b>Them nhom don vi ca nhan</b> `
     *
     * @author cuongnv
     *
     * @param request
     * @param strData
     * @param isSecurity
     * @return
     */
    public String addCvGroup(HttpServletRequest request,
            String strData, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Lay userId tren he thong 1
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        if (user2 == null || user2.getUserId() == null) {
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
        // Lay ma nhan vien
        String cardId = user2.getStrCardNumber();
        log.setUserName(cardId);
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        String data = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, strData);
        }
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            HashMap<String, Object> hmParams = new HashMap<>();
            hmParams.put(ConstantsFieldParams.CV_GROUP_NAME, String.class);
            hmParams.put(ConstantsFieldParams.CV_GROUP_DESCRIPTION, String.class);
            hmParams.put(ConstantsFieldParams.CV_GROUP_ISPUBLIC, Integer.class);
            hmParams.put(ConstantsFieldParams.CV_GROUP_GROUPTYPE, Integer.class);
            hmParams.put(ConstantsFieldParams.CV_GROUP_STAFFGROUPIDVOF2, Long.class);
            // Datdc add them nganh va hinh thuc van ban
            hmParams.put(ConstantsFieldParams.AREA_ID, Integer.class);
            hmParams.put(ConstantsFieldParams.CATEGORY_ID, Integer.class);
            

            HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, userGroup);
            String name = (String) ((valueParams.get(ConstantsFieldParams.CV_GROUP_NAME) != null) ? valueParams.get(ConstantsFieldParams.CV_GROUP_NAME) : "");
            String description = (String) ((valueParams.get(ConstantsFieldParams.CV_GROUP_DESCRIPTION) != null) ? valueParams.get(ConstantsFieldParams.CV_GROUP_DESCRIPTION) : "");
            Integer isPublic = (Integer) ((valueParams.get(ConstantsFieldParams.CV_GROUP_ISPUBLIC) != null) ? valueParams.get(ConstantsFieldParams.CV_GROUP_ISPUBLIC) : 0);
            Integer groupType = (Integer) ((valueParams.get(ConstantsFieldParams.CV_GROUP_GROUPTYPE) != null) ? valueParams.get(ConstantsFieldParams.CV_GROUP_GROUPTYPE) : 1);
            // Datdc add them nganh va hinh thuc van ban
            Integer areaId = (Integer) ((valueParams.get(ConstantsFieldParams.AREA_ID) != null) ? valueParams.get(ConstantsFieldParams.AREA_ID) : null);
            Integer categotyId = (Integer) ((valueParams.get(ConstantsFieldParams.CATEGORY_ID) != null) ? valueParams.get(ConstantsFieldParams.CATEGORY_ID) : null);

            Long staffGroupIdVof2;
            switch (isPublic) {
                case 1:
                    //Fill  cung voi dung chung tap doan
                    staffGroupIdVof2 = 148842L;
                    break;
                case 0:
                    staffGroupIdVof2 = (Long) ((valueParams.get(ConstantsFieldParams.CV_GROUP_STAFFGROUPIDVOF2) != null) ? valueParams.get(ConstantsFieldParams.CV_GROUP_STAFFGROUPIDVOF2) : 0);
                    break;
                default:
                    staffGroupIdVof2 = 0L;
                    break;
            }
            //Lay danh sach ca nhan theo nhom ca nhan
            List<EntityStaffInCvGroup> strStaffInCvGroup = new ArrayList<>();
            JSONArray arrStaffInCvGroup = FunctionCommon.jsonGetArray(ConstantsFieldParams.CV_GROUP_LSTSTAFFINCVGROUP, data);
            if (arrStaffInCvGroup != null && arrStaffInCvGroup.length() > 0) {
                for (int i = 0; i < arrStaffInCvGroup.length(); i++) {
                    JSONObject innerObj = (JSONObject) arrStaffInCvGroup.get(i);
                    Long groupIdVof2 = null;
                    Long staffIdVof2 = null;

                    if (innerObj.has(ConstantsFieldParams.CV_GROUP_GROUPIDVOF2)) {
                        groupIdVof2 = Long.parseLong(innerObj.getString(ConstantsFieldParams.CV_GROUP_GROUPIDVOF2).trim());
                    }
                    if (innerObj.has(ConstantsFieldParams.CV_GROUP_STAFFIDVOF2)) {
                        staffIdVof2 = Long.parseLong(innerObj.getString(ConstantsFieldParams.CV_GROUP_STAFFIDVOF2).trim());
                    }

                    EntityStaffInCvGroup sic = new EntityStaffInCvGroup();
                    sic.setGroupIdVof2(groupIdVof2);
                    sic.setStaffIdVof2(staffIdVof2);
                    strStaffInCvGroup.add(sic);
                }
            }
            //Lay danh sach don vi theo vai tro
            List<EntityRoleInCvGroup> strRoleInCvGroup = new ArrayList<>();
            JSONArray arrRoleInCvGroup = FunctionCommon.jsonGetArray(ConstantsFieldParams.CV_GROUP_LSTROLEINCVGROUP, data);
            if (arrRoleInCvGroup != null && arrRoleInCvGroup.length() > 0) {
                for (int i = 0; i < arrRoleInCvGroup.length(); i++) {
                    JSONObject innerObj = (JSONObject) arrRoleInCvGroup.get(i);
                    Long groupIdVof2 = null;
                    String roleIdVof2 = null;

                    if (innerObj.has(ConstantsFieldParams.CV_GROUP_GROUPIDVOF2)) {
                        groupIdVof2 = Long.parseLong(innerObj.getString(ConstantsFieldParams.CV_GROUP_GROUPIDVOF2).trim());
                    }
                    if (innerObj.has(ConstantsFieldParams.CV_GROUP_ROLEIDVOF2)) {
                        roleIdVof2 = innerObj.getString(ConstantsFieldParams.CV_GROUP_ROLEIDVOF2).trim();
                    }

                    EntityRoleInCvGroup ric = new EntityRoleInCvGroup();
                    ric.setGroupIdVof2(groupIdVof2);
                    ric.setRoleIdVof2(roleIdVof2);
                    strRoleInCvGroup.add(ric);
                }
            }
            // Datdc lay danh sach list nguoi ky trong nhom trinh ky
            List<EntityGroupSign> listGroupSign = new ArrayList<>();
            JSONArray arrGroupSign = FunctionCommon.jsonGetArray(ConstantsFieldParams.CV_GROUP_SIGN_PROCESS, data);
            if (arrGroupSign != null && arrGroupSign.length() > 0) {
                Type listTypeGroup = new TypeToken<ArrayList<EntityGroupSign>>() {
                }.getType();
                Gson gson = new Gson();
                listGroupSign = gson.fromJson(arrGroupSign.toString(), listTypeGroup);
            }
            
            
            CvGroupDAO cv = new CvGroupDAO();
            // Datdc add param for group sign
            Integer rs = cv.addCvGroup(name, description, isPublic, groupType,
                    staffGroupIdVof2, strStaffInCvGroup, strRoleInCvGroup, user2, areaId, categotyId, listGroupSign);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, rs, aesKey);
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, null, null);
        }
    }

    /**
     * <b>Tim kiem nhom ca nhan</b>
     *
     * @cuongnv
     * @param request
     * @param data
     * @param isSecurity
     * @return
     *
     */
    public String search(HttpServletRequest request,
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
                ConstantsFieldParams.CV_GROUP_NAME,
                ConstantsFieldParams.CV_GROUP_ISPUBLIC,
                ConstantsFieldParams.CV_GROUP_GROUPTYPE,
                ConstantsFieldParams.CV_GROUP_STAFFGROUPIDVOF2,
                // Datdc add them tham so cho search nhom trinh ky
                ConstantsFieldParams.CV_CALL_FROM_REQUISITION,
                ConstantsFieldParams.CV_CALL_IS_DEFAULT_ORG,
                ConstantsFieldParams.AREA_ID,
                ConstantsFieldParams.TYPE_ID
                
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String name = listValue.get(0);
            Integer isPublic = Integer.parseInt(listValue.get(1));
            Integer groupType = Integer.parseInt(listValue.get(2));
            Long staffGroupIdVof2;
            try {
                staffGroupIdVof2 = Long.parseLong(listValue.get(3));
            } catch (NumberFormatException ex) {
                staffGroupIdVof2 = null;
            }
            CvGroupDAO cv = new CvGroupDAO();
            // Datdc add check call tu man trinh ky hay ko? start
            Boolean isCallFromRequi = false;
            Boolean isDefaultOrg = false;
            // Datdc add Nganh
            Long areaId = null;
            // Datdc add Hinh thuc van ban
            Long typeId = null;
            if (!CommonUtils.isEmpty(listValue.get(4))) {
                isCallFromRequi = Boolean.valueOf(listValue.get(4));
                int index = 5;
                if (isCallFromRequi) {
                    // Check xem  don vi default
                    if(!CommonUtils.isEmpty(listValue.get(5))) {
                        isDefaultOrg = Boolean.valueOf(listValue.get(5));
                        index = 6;
                    }
                    // Lay id nganh
                    if (!CommonUtils.isEmpty(listValue.get(index))) {
                        areaId = Long.valueOf(listValue.get(index));
                        index ++;
                    }
                    // Lay id hinh thuc van ban
                    if (!CommonUtils.isEmpty(listValue.get(index))) {
                        typeId = Long.valueOf(listValue.get(index));
                    }
                }
            }
            List<EntityCvGroup> rs = new ArrayList<>();
            if (isCallFromRequi) {
                rs = cv.searchGrooupProcess(name, isPublic, groupType,
                        staffGroupIdVof2, userGroup, isDefaultOrg, areaId,
                        typeId);
            } else {
                rs = cv.search(name, isPublic, groupType, staffGroupIdVof2, userGroup);
            }
            // Datdc add check call tu man trinh ky hay ko? end
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, rs, aesKey);
        } catch (JSONException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, null, null);
        }
    }

    /**
     * <b>Edit nhom ca nhan</b>
     *
     * @author cuongnv
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String editCvGroup(HttpServletRequest request,
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
                ConstantsFieldParams.CV_GROUP_GROUPID,
                ConstantsFieldParams.CV_GROUP_NAME,
                ConstantsFieldParams.CV_GROUP_DESCRIPTION,
                ConstantsFieldParams.CV_GROUP_ISPUBLIC,
                ConstantsFieldParams.CV_GROUP_GROUPTYPE,
                ConstantsFieldParams.CV_GROUP_STAFFGROUPIDVOF2,
                // Datdc add them tham so
                // Datdc add them nganh va hinh thuc van ban
                ConstantsFieldParams.AREA_ID,
                ConstantsFieldParams.CATEGORY_ID
                
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long groupId = Long.parseLong(listValue.get(0));
            String name = listValue.get(1);
            String description = listValue.get(2);
            Integer isPublic = Integer.parseInt(listValue.get(3));
            Integer groupType = Integer.parseInt(listValue.get(4));
            Long staffGroupIdVof2;
            switch (isPublic) {
                case 1:
                    // fill cung voi nhom dung chung tap doan
                    staffGroupIdVof2 = 148842L;
                    break;
                case 0:
                    staffGroupIdVof2 = Long.parseLong(listValue.get(5));
                    break;
                default:
                    staffGroupIdVof2 = 0L;
                    break;
            }
            List<EntityStaffInCvGroup> strStaffInCvGroup = new ArrayList<>();
            JSONArray arrStaffInCvGroup = FunctionCommon.jsonGetArray(ConstantsFieldParams.CV_GROUP_LSTSTAFFINCVGROUP, data);
            if (arrStaffInCvGroup != null && arrStaffInCvGroup.length() > 0) {
                for (int i = 0; i < arrStaffInCvGroup.length(); i++) {
                    JSONObject innerObj = (JSONObject) arrStaffInCvGroup.get(i);
                    Long groupIdVof2 = null;
                    Long staffIdVof2 = null;

                    if (innerObj.has(ConstantsFieldParams.CV_GROUP_GROUPIDVOF2)) {
                        groupIdVof2 = Long.parseLong(innerObj.getString(ConstantsFieldParams.CV_GROUP_GROUPIDVOF2).trim());
                    }
                    if (innerObj.has(ConstantsFieldParams.CV_GROUP_STAFFIDVOF2)) {
                        staffIdVof2 = Long.parseLong(innerObj.getString(ConstantsFieldParams.CV_GROUP_STAFFIDVOF2).trim());
                    }

                    EntityStaffInCvGroup sic = new EntityStaffInCvGroup();
                    sic.setGroupIdVof2(groupIdVof2);
                    sic.setStaffIdVof2(staffIdVof2);
                    strStaffInCvGroup.add(sic);
                }
            }

            List<EntityRoleInCvGroup> strRoleInCvGroup = new ArrayList<>();
            JSONArray arrRoleInCvGroup = FunctionCommon.jsonGetArray(ConstantsFieldParams.CV_GROUP_LSTROLEINCVGROUP, data);
            if (arrRoleInCvGroup != null && arrRoleInCvGroup.length() > 0) {
                for (int i = 0; i < arrRoleInCvGroup.length(); i++) {
                    JSONObject innerObj = (JSONObject) arrRoleInCvGroup.get(i);
                    Long groupIdVof2 = null;
                    String roleIdVof2 = null;

                    if (innerObj.has(ConstantsFieldParams.CV_GROUP_GROUPIDVOF2)) {
                        groupIdVof2 = Long.parseLong(innerObj.getString(ConstantsFieldParams.CV_GROUP_GROUPIDVOF2).trim());
                    }
                    if (innerObj.has(ConstantsFieldParams.CV_GROUP_ROLEIDVOF2)) {
                        roleIdVof2 = innerObj.getString(ConstantsFieldParams.CV_GROUP_ROLEIDVOF2).trim();
                    }

                    EntityRoleInCvGroup ric = new EntityRoleInCvGroup();
                    ric.setGroupIdVof2(groupIdVof2);
                    ric.setRoleIdVof2(roleIdVof2);
                    strRoleInCvGroup.add(ric);
                }
            }
            // Datdc add and edit param phuc vu cho edit nhom trinh ky start
            Integer rs = 0;
            CvGroupDAO cv = new CvGroupDAO();
            // Neu la nhom trinh ky
            if (groupType == 3) {
                Integer areaId = null;
                Object areaOb = FunctionCommon.jsonGetItem(ConstantsFieldParams.AREA_ID, data);
                if (null != areaOb) {
                    areaId = Integer.parseInt(areaOb.toString());
                }
                // Id hinh thuc van ban
                Integer categoryId = null;
                Object cateOb = FunctionCommon.jsonGetItem(ConstantsFieldParams.CATEGORY_ID, data);
                if (null != cateOb) {
                    categoryId = Integer.parseInt(cateOb.toString());
                }
                // List nguoi ky tu view
                List<EntityGroupSign> listGroupSign = new ArrayList<>();
                JSONArray arrGroupSign = FunctionCommon.jsonGetArray(ConstantsFieldParams.CV_GROUP_SIGN_PROCESS, data);
                if (arrGroupSign != null && arrGroupSign.length() > 0) {
                    Type listTypeGroup = new TypeToken<ArrayList<EntityGroupSign>>() {
                    }.getType();
                    Gson gson = new Gson();
                    listGroupSign = gson.fromJson(arrGroupSign.toString(), listTypeGroup);
                }
                // Lay list dang co trong db
                List<EntityGroupSign> listGroupDb = cv.searchGroupSignByGroupId(groupId);
                // List emp_vhr_id tu db
                List<Long> lstIdFromDb = new ArrayList<>();
                List<Long> lstInsertEmpVhrId = new ArrayList<>();
                List<Long> lstDeleteEmpVhrId = new ArrayList<>();
                List<Long> lstIdFromView = new ArrayList<>();
                List<Long> lstTmp = new ArrayList<>();
                if(!CommonUtils.isEmpty(listGroupDb)) {
                    if (!CommonUtils.isEmpty(listGroupSign)) {
                        
                        for (EntityGroupSign egS : listGroupDb) {
                            lstIdFromDb.add(egS.getEmpVhrId());
                        }
                        for (EntityGroupSign egSv : listGroupSign) {
                            lstIdFromView.add(egSv.getEmpVhrId());
                        }
                        lstTmp.addAll(lstIdFromView);
                        lstIdFromView.removeAll(lstIdFromDb);
                        // Lay lst insert
                        lstInsertEmpVhrId = lstIdFromView;

                        lstIdFromDb.removeAll(lstTmp);
                        // Lay lst delete
                        lstDeleteEmpVhrId = lstIdFromDb;
                    }
                    
                }
                rs = cv.editCvGroupTypeProcess(groupId, name, description,
                        isPublic, groupType, staffGroupIdVof2, user,
                        listGroupSign, lstInsertEmpVhrId, lstDeleteEmpVhrId,
                        areaId, categoryId);
                
            // Cac loai khac giu nguyen luong cu
            } else {
                
                rs = cv.editCvGroup(groupId, name, description, isPublic, groupType,
                        staffGroupIdVof2, strStaffInCvGroup, strRoleInCvGroup, user);
            }
            // Datdc add param phuc vu cho edit nhom trinh ky end
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, rs, aesKey);
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, null, null);
        }
    }

    /**
     * <b>Xoa nhom ca nhan</b>
     *
     * @author cuongnv
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String deleteCvGroup(HttpServletRequest request,
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
                ConstantsFieldParams.CV_GROUP_GROUPID};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long groupId = Long.parseLong(listValue.get(0));
            CvGroupDAO cv = new CvGroupDAO();
            Integer rs = cv.deleteCvGroup(groupId, user);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalStart(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, rs, aesKey);
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, null, null);
        }
    }
    
    public String getListCvGroupByListId(HttpServletRequest request,
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
                ConstantsFieldParams.CV_GROUP_GROUPID,
                ConstantsFieldParams.TYPE};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            List<Long> groupIds = new ArrayList<>();
            if (!CommonUtils.isEmpty(listValue.get(0))) {
                JSONArray jsonArray = new JSONArray(listValue.get(0));
                for (int i = 0; i < jsonArray.length(); i++) {
                    groupIds.add(jsonArray.getLong(i));
                }
            }
            Long type = CommonUtils.isInteger(listValue.get(1)) ? Long.parseLong(listValue.get(1)) : null;
            CvGroupDAO cv = new CvGroupDAO();
            List<EntityCvGroup> rs = cv.getListCvGroupByListId(groupIds, type);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalStart(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, rs, aesKey);
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, null, null);
        }
    }
}
