/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.connectvhr.ConnectVHRDao;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.User.Vof2_EntityUser;
import com.viettel.voffice.database.entity.connectvhr.EntityConnectVHR;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.LogUtils;

/**
 *
 * @author Hoanhv6
 */
@SuppressWarnings("deprecation")
public class ConnectVHRController {

    private static final Logger LOGGER = Logger.getLogger(ConnectVHRController.class);
    private static final String CLASS_NAME = ConnectVHRController.class.getName();
    
    private static final String PARENT_ID = "parentId";
    public static final int INSERT_TYPE = 1;
    public static final int UPDATE_TYPE =2;
    
       /**
     * <b>Kiem tra ma code co ton tai</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String checkCodeExist(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getConnectVHRList (Lay danh sach don vi lien thong - khong co session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("getListTaskFromMission (Lay danh sach cong viec ca nhan"
                    + " tao tu nhiem vu) - Khong co thong tin user tren he thong 2!");
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
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.CODE,
                ConstantsFieldParams.SYS_ORGANIZATION_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String code = listValue.get(0);
            Long orgId = CommonUtils.isInteger(listValue.get(1)) ? Long.valueOf(listValue.get(1)) : null;
            
            ConnectVHRDao dao = new ConnectVHRDao();
            boolean result = dao.checkCodeExist(code, orgId);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result ? 1 : 0, aesKey);
        } catch (Exception ex) {
            LOGGER.error("getConnectVHR (Lay danh sach don vi lien thong - Exception - username: "
                    + cardId + "\ndata: " + data);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
       /**
     * <b>Lay gia tri order cao nhat cua don vi cha</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getMaxChildSortOrder(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getMaxChildSortOrder (Lay danh sach don vi lien thong - khong co session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("getMaxChildSortOrder (Lay danh sach cong viec ca nhan"
                    + " tao tu nhiem vu) - Khong co thong tin user tren he thong 2!");
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
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                PARENT_ID};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long result = null;
            Long parentId = null;
            if(!CommonUtils.isEmpty(listValue.get(0))){
               parentId = Long.parseLong(listValue.get(0));
            }
            ConnectVHRDao dao = new ConnectVHRDao();
            result = dao.getMaxOrder(parentId);
            if (result == null) {
                LOGGER.error("getMaxChildSortOrder (Lay danh sach don vi lien thong - result = null - username: "
                        + cardId + "\ndata: " + data);
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        }
        catch (Exception ex) {
            LOGGER.error("getMaxChildSortOrder (Lay danh sach don vi lien thong - Exception - username: "
                    + cardId + "\ndata: " + data);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    /**
     * <b>Lay danh sach ban ghi theo dieu kien </b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String findByCondition(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("findByCondition (Lay danh sach don vi lien thong - khong co session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("findByCondition (Lay danh sach cong viec ca nhan"
                    + " tao tu nhiem vu) - Khong co thong tin user tren he thong 2!");
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
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[] {
                    ConstantsFieldParams.STR_KEYWORDS,
                    ConstantsFieldParams.DATA_SEARCH,
                    ConstantsFieldParams.START_RECORD,
                    ConstantsFieldParams.PAGE_SIZE,
                    ConstantsFieldParams.IS_COUNT,
                    ConstantsFieldParams.STEP
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String search = listValue.get(0);
            String objStr = listValue.get(1);
            Long firstResult = CommonUtils.isInteger(listValue.get(2)) ? Long.valueOf(listValue.get(2)): null;
            Long maxResult = CommonUtils.isInteger(listValue.get(3)) ? Long.valueOf(listValue.get(3)): null;
            EntityConnectVHR connectVHR = new EntityConnectVHR();
            if (!CommonUtils.isEmpty(objStr)) {
                Gson gson = new Gson();
                connectVHR = gson.fromJson(objStr, EntityConnectVHR.class);
            }
            boolean isCount = "1".equals(listValue.get(4));
            boolean isRoot = "1".equals(listValue.get(5));
            
            ConnectVHRDao dao = new ConnectVHRDao();
            Object result = dao.getListConnectVHR(search, connectVHR, firstResult, maxResult, isCount,
                    isRoot, null, true);
            if (result == null) {
                LOGGER.error("findByCondition (Lay danh sach don vi lien thong - result = null - username: "
                        + cardId + "\ndata: " + data);
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } // Xay ra ngoai le trong qua trinh xu ly
        catch (Exception ex) {
            LOGGER.error("findByCondition (Lay danh sach don vi lien thong - Exception - username: "
                    + cardId + "\ndata: " + data);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Xoa don vi lien thong</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String deleteConnectVHR(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getConnectVHRList (Lay danh sach don vi lien thong - khong co session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("getListTaskFromMission (Lay danh sach cong viec ca nhan"
                    + " tao tu nhiem vu) - Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        Long userId = user.getUserId();
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
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.SYS_ORGANIZATION_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long connectVHRId = Long.parseLong(listValue.get(0));
            ConnectVHRDao dao = new ConnectVHRDao();
            Boolean result = dao.deleteConnectVHR(connectVHRId, userId);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error("deleteTask (Xoa cong viec) - Exception - username: "
                    + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
     /**
     * <b>Them moi don vi lien thong</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String createNewConnectVHR(HttpServletRequest request,
            String data, String isSecurity) {
        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("createNemConnectVHR (them moi mot don vi lien thong) - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("createNemConnectVHR (them moi mot don vi lien thong) - "
                    + "Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Lay userId
        Long userId = user.getUserId();
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
        // Thiet lap danh sach tham so va ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.CONNECT_VHR_CODE,
                ConstantsFieldParams.CONNECT_VHR_NAME,
                ConstantsFieldParams.CONNECT_VHR_ORG_PARENT_ID,
                ConstantsFieldParams.CONNECT_VHR_ORDER_NUMBER,
                ConstantsFieldParams.CONNECT_VHR_ADDRESS,
                ConstantsFieldParams.CONNECT_VHR_PHONE,
                ConstantsFieldParams.CONNECT_VHR_FAX,
                ConstantsFieldParams.CONNECT_VHR_EMAIL,
                ConstantsFieldParams.CONNECT_VHR_WEBSITE,
                "vofOrgId"
                
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            EntityConnectVHR entity = new EntityConnectVHR();
            String code = listValue.get(0);
            entity.setCode(code);
            String name = listValue.get(1);
            entity.setName(name);
            Long orgParentId = !CommonUtils.isEmpty(listValue.get(2)) ? Long.parseLong(listValue.get(2)) : null;
            entity.setOrgParentId(orgParentId);
            Long orderNumber = !CommonUtils.isEmpty(listValue.get(3)) ? Long.parseLong(listValue.get(3)) : null;
            entity.setOrderNumber(orderNumber);
            entity.setCreatedBy(userId);
            entity.setDelFlag(0L);
            String address = listValue.get(4);
            entity.setAddress(address);
            String phone = listValue.get(5);
            entity.setPhone(phone);
            String fax = listValue.get(6);
            entity.setFax(fax);
            String email = listValue.get(7);
            entity.setEmail(email);
            String website = listValue.get(8);
            entity.setWebsite(website);
            entity.setVofOrgId(listValue.get(9));
            
            ConnectVHRDao dao = new ConnectVHRDao();
            Long result = dao.createConnectVHR(0l, entity);
            // Log ket thu chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);

        } // Loi server
        catch (JSONException | NumberFormatException | JsonSyntaxException ex) {
            LOGGER.error("createNemConnectVHR (them moi mot don vi lien thong) - "
                    + "Exception - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    /**
     * <b>Cap nhat don vi lien thong</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String updateConnectVHR(HttpServletRequest request,
            String data, String isSecurity) {
        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("createNemConnectVHR (them moi mot don vi lien thong) - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("createNemConnectVHR (them moi mot don vi lien thong) - "
                    + "Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Lay userId
        Long userId = user.getUserId();
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
        // Thiet lap danh sach tham so va ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.CONNECT_VHR_CODE,
                ConstantsFieldParams.CONNECT_VHR_NAME,
                ConstantsFieldParams.CONNECT_VHR_ORG_PARENT_ID,
                ConstantsFieldParams.CONNECT_VHR_ORDER_NUMBER,
                ConstantsFieldParams.CONNECT_VHR_PATH_NAME,
                ConstantsFieldParams.CONNECT_VHR_ADDRESS,
                ConstantsFieldParams.CONNECT_VHR_PHONE,
                ConstantsFieldParams.CONNECT_VHR_FAX,
                ConstantsFieldParams.CONNECT_VHR_EMAIL,
                ConstantsFieldParams.CONNECT_VHR_WEBSITE,
                ConstantsFieldParams.SYS_ORGANIZATION_ID,
                "vofOrgId"
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            EntityConnectVHR entity = new EntityConnectVHR();
            String code = listValue.get(0);
            entity.setCode(code);
            String name = listValue.get(1);
            entity.setName(name);
            if (CommonUtils.isInteger(listValue.get(2))) {
                Long orgParentId = Long.parseLong(listValue.get(2));
                entity.setOrgParentId(orgParentId);
            }
            if (CommonUtils.isInteger(listValue.get(3))) {
                Long orderNumber = Long.parseLong(listValue.get(3));
                entity.setOrderNumber(orderNumber);
            }
            entity.setUpdateBy(userId);
            entity.setDelFlag(0L);
            String pathName = listValue.get(4);
            entity.setPathName(pathName);
            String address = listValue.get(5);
            entity.setAddress(address);
            String phone = listValue.get(6);
            entity.setPhone(phone);
            String fax = listValue.get(7);
            entity.setFax(fax);
            String email = listValue.get(8);
            entity.setEmail(email);
            String website = listValue.get(9);
            entity.setWebsite(website);
            Long sysId = null;
            if (CommonUtils.isInteger(listValue.get(10))) {
                sysId = Long.parseLong(listValue.get(10));
            }
            entity.setVofOrgId(listValue.get(11));
            
            ConnectVHRDao dao = new ConnectVHRDao();
            boolean result = dao.updateConnectVHR(sysId, entity);
            // Log ket thu chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);

        } // Loi server
        catch (JSONException | NumberFormatException | JsonSyntaxException ex) {
            LOGGER.error("createNemConnectVHR (them moi mot don vi lien thong) - "
                    + "Exception - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    public String updateSyncOrg(HttpServletRequest request, String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("updateSyncOrg (Lay danh sach don vi lien thong - khong co session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("updateSyncOrg - Khong co thong tin user tren he thong 2!");
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
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.POINT
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            boolean check = "1".equals(listValue.get(0));

            ConnectVHRDao dao = new ConnectVHRDao();
            boolean result = dao.updateSyncOrg(check);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result ? 1 : 0, aesKey);
        } catch (Exception ex) {
            LOGGER.error("updateSyncOrg - Exception - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
}
