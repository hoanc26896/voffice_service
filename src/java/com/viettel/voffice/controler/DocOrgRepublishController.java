/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.document.DocOrgRepublishDAO;
import com.viettel.voffice.database.dao.text.TextDAO;
import com.viettel.voffice.database.entity.EntityDocument;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.EntityVhrOrg;
import com.viettel.voffice.database.entity.User.EntityUser;
import com.viettel.voffice.database.entity.User.Vof2_EntityUser;
import com.viettel.voffice.database.entity.text.EntityText;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.LogUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author voffice_guest04
 */
public class DocOrgRepublishController {

    public static final String ROOT_ACTION = "DocumentOrgRePublicAction";
    private static final Logger LOGGER = Logger.getLogger(DocOrgRepublishController.class);

    // Ten class bao gom ca ten package
    private static final String CLASS_NAME = DocOrgRepublishController.class.getName();

    /**
     *
     * @author cuongnv
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getBaseDocument(HttpServletRequest request, String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        if ((user1 == null || user1.getUserId() == null)
                && (user2 == null || user2.getUserId() == null)) {
            LOGGER.error("getBaseDocument - user hoac userId tren he thong 1&2 null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Lay ra ma nhan vien
        String cardId = "";
        if (user1 != null && !CommonUtils.isEmpty(user1.getStrCardNumber())) {
            cardId = user1.getStrCardNumber();
        } else if (user2 != null && !CommonUtils.isEmpty(user2.getStrCardNumber())) {
            cardId = user2.getStrCardNumber();
        }
        log.setUserName(cardId);
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
        }
        // Ghi log bat dau chuc nag
        LogUtils.logFunctionalStart(log);
        List<String> sysIds = new ArrayList<>();
        if (user2 != null) {
            sysIds = user2.getListOrgPath();
        }

        DocOrgRepublishDAO dorDAO = new DocOrgRepublishDAO();
        List<EntityDocument> listDoc = null;
        if (user2 != null) {
            listDoc = dorDAO.getBaseDocument(sysIds, String.valueOf(user2.getSysOrgId()));
        }
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, listDoc, aesKey);
    }

    /**
     * <b>Ham lay cac don vi da duoc added vao</b><br>
     *
     * author outsourceTeam/sonnd
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getOganizationAdded(HttpServletRequest request, String data,
            String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(request);
        if ((user1 == null || user1.getUserId() == null)
                && (user2 == null || user2.getUserId() == null)) {
            LOGGER.error("getBaseDocument - user hoac userId tren he thong 1&2 null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Lay ma nhan vien
        String cardId = "";
        if (user1 != null && !CommonUtils.isEmpty(user1.getStrCardNumber())) {
            cardId = user1.getStrCardNumber();
        } else if (user2 != null && !CommonUtils.isEmpty(user2.getStrCardNumber())) {
            cardId = user2.getStrCardNumber();
        }
        log.setUserName(cardId);
        String aesKey = null;
        String decryptedData = "";
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            decryptedData = SecurityControler.decodeDataByAes(aesKey, data);
        }
        // Ghi log bat dau chuc nang
        log.setParamList(decryptedData);
        LogUtils.logFunctionalStart(log);
        HashMap hmParams = new HashMap();
        hmParams.put("textId", Long.class);

        HashMap valueParams = FunctionCommon.getListParamsClient(hmParams, data, dataSessionGR);
        Long textRootId = (Long) valueParams.get("textId");

        DocOrgRepublishDAO dorDAO = new DocOrgRepublishDAO();
        List<EntityVhrOrg> listVhrOrg = dorDAO.getOganizationAdded(textRootId,null);
        // Log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, listVhrOrg, aesKey);
    }

    /**
     * *
     * author outsourceTeam/sonnd ham lay gia tri cua van ban goc
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getBaseTextDataFromRupublishTextId(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(request);

        if ((user1 == null || user1.getUserId() == null)
                && (user2 == null || user2.getUserId() == null)) {
            LOGGER.error("getBaseDocument - user hoac userId tren he thong 1&2 null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Lay ma nhan vien
        String cardId = "";
        if (user1 != null && !CommonUtils.isEmpty(user1.getStrCardNumber())) {
            cardId = user1.getStrCardNumber();
        } else if (user2 != null && !CommonUtils.isEmpty(user2.getStrCardNumber())) {
            cardId = user2.getStrCardNumber();
        }
        log.setUserName(cardId);
        String aesKey = null;
        String decryptedData = "";
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            decryptedData = SecurityControler.decodeDataByAes(aesKey, data);
        }
        // Ghi log bat dau chuc nang
        log.setParamList(decryptedData);
        LogUtils.logFunctionalStart(log);
        HashMap hmParams = new HashMap();
        hmParams.put("republishTextId", String.class);

        HashMap valueParams = FunctionCommon.getListParamsClient(hmParams, data, dataSessionGR);
        Long textRootId = (Long) valueParams.get("republishTextId");

        DocOrgRepublishDAO dorDAO = new DocOrgRepublishDAO();
        List<EntityText> result = dorDAO.getBaseTextDataFromRupublishTextId(textRootId);
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
    }

    /**
     * author OutsourceTeam/sonnd
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String updateRootText(HttpServletRequest request, String data,
            String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(request);

        if ((user1 == null || user1.getUserId() == null)
                && (user2 == null || user2.getUserId() == null)) {
            LOGGER.error("getBaseDocument - user hoac userId tren he thong 1&2 null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Lay ma nhan vien
        String cardId = "";
        if (user1 != null && !CommonUtils.isEmpty(user1.getStrCardNumber())) {
            cardId = user1.getStrCardNumber();
        } else if (user2 != null && !CommonUtils.isEmpty(user2.getStrCardNumber())) {
            cardId = user2.getStrCardNumber();
        }
        log.setUserName(cardId);
        String aesKey = null;
        String decryptedData = "";
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            decryptedData = SecurityControler.decodeDataByAes(aesKey, data);
        }
        // Ghi log bat dau chuc nang
        log.setParamList(decryptedData);
        LogUtils.logFunctionalStart(log);
        HashMap hmParams = new HashMap();
        hmParams.put("republishTextId", Long.class);
        hmParams.put("textId", Long.class);

        HashMap valueParams = FunctionCommon.getListParamsClient(hmParams, data, dataSessionGR);
        Long republishTextId = (Long) valueParams.get("republishTextId");
        Long textIdNew = (Long) valueParams.get("textId");
        Long sysId = (Long) valueParams.get("sysid");

        DocOrgRepublishDAO dorDAO = new DocOrgRepublishDAO();
        boolean result = dorDAO.updateRootText(republishTextId, textIdNew, sysId);
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
    }

    public String getListOrganization(HttpServletRequest request, String data,
            String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String strResult = "";
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(request);
        if (dataSessionGR.getCheckSessionOk()) {
            String cardId;
            try {
                // Session time out
                EntityUserGroup entityUserGroup = dataSessionGR;
                if (entityUserGroup.getVof2_ItemEntityUser() == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.SESSION_TIME_OUT, null, null);
                }
                // Lay ma nhan vien
                cardId = entityUserGroup.getVof2_ItemEntityUser().getStrCardNumber();
                log.setUserName(cardId);
                // Lay danh sach don vi ma user la lanh dao hoac thu truong don vi hoac tro ly
                // Lay userId tren he thong 1
                Long sysUserIdVof1 = null;
                List<Long> lstGroupSecretaryVof1 = new ArrayList<>();
                List<Long> lstGroupSecretaryVof2;
                if (entityUserGroup.getItemEntityUser() != null) {
                    sysUserIdVof1 = entityUserGroup.getItemEntityUser().getUserId();
                    lstGroupSecretaryVof1 = entityUserGroup.getItemEntityUser().getListGroupIdVTVof1();
                }
                Vof2_EntityUser entityUserVof2 = entityUserGroup.getVof2_ItemEntityUser();
                //Thong tin don vi tren vof2.0
                boolean isSecrectary = entityUserGroup.getVof2_ItemEntityUser().getIsSecrectaryVo2();
                // Kiem tra xem co ma hoa du lieu ko
                String strAesKeyDecode = null;
                if (isSecurity != null && "1".equals(isSecurity)) {
                    // Lay AES Key
                    strAesKeyDecode = entityUserGroup.getStrAesKey();
                    // Giai ma data client gui len
                    data = SecurityControler.decodeDataByAes(strAesKeyDecode, data);
                }
                // Ghi log bat dau chuc nang
                log.setParamList(data);
                LogUtils.logFunctionalStart(log);
                JSONObject json = new JSONObject(data);
                String[] keys = new String[]{ConstantsFieldParams.TEXT_ID, ConstantsFieldParams.comment,
                    ConstantsFieldParams.IS_SEARCH_TEXT_ALL};
                List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                Long textId = Long.parseLong(listValue.get(0));

                if (isSecrectary) {
                    lstGroupSecretaryVof2 = entityUserGroup.getVof2_ItemEntityUser().getListSecretaryVhrOrg();
                    String comment = listValue.get(1);
                    String isSearchAll = listValue.get(2);
                    TextDAO tDAO = new TextDAO();
                    String result = tDAO.rejectSignDocByVTAction(textId, sysUserIdVof1,
                            entityUserVof2, lstGroupSecretaryVof1, lstGroupSecretaryVof2,
                            comment, isSearchAll, -1L, null);// Load -1 là không dùng tác dụng file đính kèm
                    // Ghi log ket thuc chuc nang
                    LogUtils.logFunctionalEnd(log);
                    return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                }
            } catch (JSONException | NumberFormatException ex) {
                LOGGER.error(ex.getMessage(), ex);
                LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_ERROR, ErrorCode.INPUT_INVALID, ex.getMessage(), data, isSecurity);
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }
        return strResult;

    }
}
