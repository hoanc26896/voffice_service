/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.document.VHROrgDAO;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntitySysRole;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.EntityVhrOrg;
import com.viettel.voffice.utils.LogUtils;
import java.util.List;
import java.util.logging.Level;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author voffice_guest04
 */
public class VHROrgController {

    public static final String ROOT_ACTION = "VHROrgAction";
    private static final Logger LOGGER = Logger.getLogger(VHROrgController.class);

    // Ten cua class bao gom ca ten package
    private static final String CLASS_NAME = VHROrgController.class.getName();

    /**
     * <b> Lay danh sach don vi pham vi theo document_scope_id</b>
     *
     * @author cuongnv
     * @param request
     * @param data
     * @param isSecurity
     * @return
     *
     */
    public String getVHROrg(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);

        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }

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
                ConstantsFieldParams.DOC_SCOPE_ID
            };

            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long docScopeId = Long.parseLong(listValue.get(0));
            VHROrgDAO vdao = new VHROrgDAO();
            List<EntityVhrOrg> results = vdao.getVHROrgs(docScopeId);
            String ad = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, results, aesKey);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return ad;
        } catch (JSONException ex) {
            java.util.logging.Logger.getLogger(VHROrgController.class.getName()).log(Level.SEVERE, null, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * <b>Danh sach vai tro</b>
     *
     * @author cuongnv
     * @param request
     * @param data
     * @param isSecurity
     * @return
     *
     */
    public String getSysRole(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
        }
        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        LogUtils.logFunctionalStart(log);

        VHROrgDAO vdao = new VHROrgDAO();
        List<EntitySysRole> rs = vdao.getSysRole();
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, rs, aesKey);
    }

    /**
     * author: outsourceTeam/sonnd lay don vi voi vai tro lanh dao, thu truong
     * cua user
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getVhrLeaderByUserId(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }
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
                ConstantsFieldParams.MEETING_ASSISTANT_EMPLOYEE_ID
            };

            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long employeeId = Long.parseLong(listValue.get(0));
            VHROrgDAO vdao = new VHROrgDAO();
            List<EntityVhrOrg> lstVhr = null;
            String result;
            if (0 < employeeId) {
                lstVhr = vdao.getVhrLeaderByUserId(employeeId, ConstantsFieldParams.MEETING_ASSISTANT_SYS_ROLE_LEADER_DIRECTOR);
            }
            result = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, lstVhr, aesKey);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return result;
        } catch (JSONException ex) {
            java.util.logging.Logger.getLogger(VHROrgController.class.getName()).log(Level.SEVERE, null, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * author: outsourceTeam/sonnd lay don vi voi vai tro admin lich hop,admin
     * don vi cua user cho viec tao tree cho popup lua chon ca nhan
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getVhrOrgUserAdminSchedule(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }
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
                ConstantsFieldParams.SCHEDULE_TYPE_SEARCH,};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Integer typeSearch = Integer.parseInt(listValue.get(0));
            Long userId_vof2 = userGroup.getVof2_ItemEntityUser().getUserId();
            VHROrgDAO vdao = new VHROrgDAO();
            List<EntityVhrOrg> lstVhr = null;
            String result;
            if (userId_vof2 != null && 0 < userId_vof2) {
                lstVhr = vdao.getVhrOrgUserAdminSchedule(userId_vof2, typeSearch);
            }
            result = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, lstVhr, aesKey);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return result;
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(VHROrgController.class.getName()).log(Level.SEVERE, null, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * author: outsourceTeam/sonnd
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String validateAddScheduleLeader(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }
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
                ConstantsFieldParams.MEETING_ASSISTANT_EMPLOYEE_ID,
                ConstantsFieldParams.SCHEDULE_LEADER_ORG
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long employeeId = Long.parseLong(listValue.get(0));
            Long orgId = Long.parseLong(listValue.get(1));
            VHROrgDAO vdao = new VHROrgDAO();
            int isValidate = 0;
            String result;
            if (0 < employeeId) {
                isValidate = vdao.validateAddScheduleLeader(employeeId, orgId);
            }
            result = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, isValidate, aesKey);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return result;
        } catch (JSONException ex) {
            java.util.logging.Logger.getLogger(VHROrgController.class.getName()).log(Level.SEVERE, null, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

}
