/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.document.AutoDigitalSignDAO;
import com.viettel.voffice.database.dao.document.DocumentPublishDAO;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.EntityVhrOrg;
//import com.viettel.voffice.database.entity.User.EntityUser;
import com.viettel.voffice.database.entity.User.Vof2_EntityUser;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.LogUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author hanhnq21@viettel.com.vn
 * @since 08/03/2016
 * @version 1.0
 */
public class DocumentPublishControler {

    private static final Logger LOGGER = Logger.getLogger(DocumentPublishControler.class);
    // Ten cua class bao gom ca ten package
    private static final String CLASS_NAME = DocumentPublishControler.class.getName();

    /**
     * tim kiem van ban cong bo
     *
     * @param isSecurity
     * @param strData
     * @param req
     * @return
     */
    public String actionSearchDocPublish(String isSecurity, String strData, HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk().booleanValue()) {
            try {

                String strAesKeyDecode = null;
                String decryptedData = "";
                if (isSecurity != null && "1".equals(isSecurity)) {
                    strAesKeyDecode = dataSessionGR.getStrAesKey();
                    decryptedData = SecurityControler.decodeDataByAes(strAesKeyDecode,
                            strData);
                }
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(decryptedData);
                LogUtils.logFunctionalStart(log);
                HashMap hmParams = new HashMap();
                hmParams.put("code", String.class);
                //trang thai cong bo
                //0: chua cong bo
                //1: cong bo
                //2: da huy
                hmParams.put("pubnishStatus", Long.class);
                //ngay ban hanh
                hmParams.put("fromPromulgateDate", String.class);
                hmParams.put("toPromulgateDate", String.class);
                //ngay den
                hmParams.put("fromReceiveDate", String.class);
                hmParams.put("toReceiveDate", String.class);
                //han cuoi
                hmParams.put("fromDeadlineDate", String.class);
                hmParams.put("toDeadlineDate", String.class);
                //tieu de van ban
                hmParams.put("title", String.class);
                //so dang ky
                hmParams.put("registerNumber", String.class);
                //nguoi ky
                hmParams.put("signer", String.class);
                //noi dung van ban
                hmParams.put("content", String.class);
                //id hinh thuc
                //khong truyen, tim kiem tat ca
                hmParams.put("typeId", Long.class);
                //id linh vuc van ban
                //khong truyen, tim kiem tat ca
                hmParams.put("areaId", Long.class);
                //id nganh
                //khong truyen, tim kiem tat ca
                hmParams.put("industryId", Long.class);
                //nguoi ban hanh
                //khong truyen, tim kiem theo nguoi dang nhap
                hmParams.put("publisherId", Long.class);

                //sap xep theo
                //0: so ky hieu
                //1: ngay ban hanh
                //2: ngay den
                //3: han cuoi
                //4: nguoi ky
                //5: trich yeu
                hmParams.put("orderBy", Long.class);
                //sap xep tang giam
                //0: tang,1: giam
                hmParams.put("directOrder", Long.class);
                //isQuickSearch
                //1: tim kiem nhanh, khac 1 tim kiem advance
                hmParams.put("isQuickSearch", Long.class);
                hmParams.put("startRecord", Long.class);
                hmParams.put("pageSize", Long.class);
                hmParams.put("isCount", String.class);
                //1: admin cong bo van ban
                hmParams.put("isAdminPublish", Long.class);
                //1 : tim kiem van ban thay the
                hmParams.put("selectDocumentPublish", Long.class);
                //id van ban thay the
                hmParams.put("documentId", Long.class);

                HashMap valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
                String code = (String) (valueParams.get("code") != null
                        ? valueParams.get("code") : "");
                Long pubnishStatus = (Long) (valueParams.get("pubnishStatus")
                        != null ? valueParams.get("pubnishStatus") : -1L);

                String fromPromulgateDate = (String) (valueParams.
                        get("fromPromulgateDate") != null
                        ? valueParams.get("fromPromulgateDate") : "");
                String toPromulgateDate = (String) (valueParams.
                        get("toPromulgateDate") != null
                        ? valueParams.get("toPromulgateDate") : "");

                String fromReceiveDate = (String) (valueParams.
                        get("fromReceiveDate") != null
                        ? valueParams.get("fromReceiveDate") : "");
                String toReceiveDate = (String) (valueParams.
                        get("toReceiveDate") != null
                        ? valueParams.get("toReceiveDate") : "");

                String fromDeadlineDate = (String) (valueParams.
                        get("fromDeadlineDate") != null
                        ? valueParams.get("fromDeadlineDate") : "");
                String toDeadlineDate = (String) (valueParams.
                        get("toDeadlineDate") != null
                        ? valueParams.get("toDeadlineDate") : "");

                String title = (String) (valueParams.
                        get("title") != null
                        ? valueParams.get("title") : "");
                String registerNumber = (String) (valueParams.
                        get("registerNumber") != null
                        ? valueParams.get("registerNumber") : "");
                String signer = (String) (valueParams.
                        get("signer") != null
                        ? valueParams.get("signer") : "");
                String content = (String) (valueParams.
                        get("content") != null
                        ? valueParams.get("content") : "");
                Long typeId = (Long) (valueParams.get("typeId")
                        != null ? valueParams.get("typeId") : 0L);
                Long areaId = (Long) (valueParams.get("areaId")
                        != null ? valueParams.get("areaId") : 0L);
                Long industryId = (Long) (valueParams.get("industryId")
                        != null ? valueParams.get("industryId") : 0L);
                //neu khong truyen tham so nay thi mac dinh tim kiem theo nguoi dang nhap
//                Long publisherId = (Long) (valueParams.get("publisherId")
//                        != null ? valueParams.get("publisherId") : 0L);

                Long orderBy = (Long) (valueParams.get("orderBy")
                        != null ? valueParams.get("orderBy") : 0L);
                Long directOrder = (Long) (valueParams.get("directOrder")
                        != null ? valueParams.get("directOrder") : 0L);
                Long startRecord = (Long) (valueParams.get("startRecord") != null
                        ? valueParams.get("startRecord") : 0L);
                Long pageSize = (Long) (valueParams.get("pageSize")
                        != null ? valueParams.get("pageSize") : 10L);
                String isCount = (String) (valueParams.
                        get("isCount") != null
                        ? valueParams.get("isCount") : "");
                Long isQuickSearch = (Long) (valueParams.get("isQuickSearch") != null
                        ? valueParams.get("isQuickSearch") : 0L);
                Long isAdminPublish = (Long) (valueParams.get("isAdminPublish") != null
                        ? valueParams.get("isAdminPublish") : 0L);
                Long selectDocumentPublish = (Long) (valueParams.get("selectDocumentPublish") != null
                        ? valueParams.get("selectDocumentPublish") : 0L);
                Long documentId = (Long) (valueParams.get("documentId") != null
                        ? valueParams.get("documentId") : 0L);
                //ghi log dau vao
                DocumentPublishDAO documentPublishDao = new DocumentPublishDAO();
                Object result;
                //tim kiem theo nguoi dang nhap
                if (isQuickSearch.equals(1L)) {
                    //tim kiem nhanh
                    result = documentPublishDao.actionQuickSearchDocPublish(
                            code, dataSessionGR.getVof2_ItemEntityUser().getUserId(),
                            orderBy, directOrder, isCount, startRecord, pageSize,
                            isAdminPublish, dataSessionGR.getVof2_ItemEntityUser().getListSecretaryVhrOrg());
                } else {
                    //tim kiem advance
                    result = documentPublishDao.
                            actionSearchDocPublish(code, pubnishStatus, fromPromulgateDate,
                                    toPromulgateDate, fromReceiveDate, toReceiveDate, fromDeadlineDate,
                                    toDeadlineDate, title, registerNumber, signer, content,
                                    typeId, areaId, industryId, dataSessionGR.getVof2_ItemEntityUser().getUserId(), orderBy, directOrder,
                                    isCount, startRecord, pageSize, isAdminPublish,
                                    selectDocumentPublish, dataSessionGR.getVof2_ItemEntityUser().getListSecretaryVhrOrg(), documentId);
                }
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                java.util.logging.Logger.getLogger(DocumentPublishControler.class.getName()).
                        log(Level.SEVERE, "actionSearchDocPublish error: {0}", e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }
        return strResult;
    }

    /**
     * ham danh sach van ban bi thay the khi load chi tiet van ban cong bo
     *
     * @param isSecurity
     * @param strData
     * @param req
     * @return
     */
    public String getListDocAlter(String isSecurity, String strData, HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                String strAesKeyDecode = null;
                String decryptedData = "";
                if (isSecurity != null && "1".equals(isSecurity)) {
                    strAesKeyDecode = dataSessionGR.getStrAesKey();
                    decryptedData = SecurityControler.decodeDataByAes(strAesKeyDecode,
                            strData);
                }
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(decryptedData);
                LogUtils.logFunctionalStart(log);
                HashMap hmParams = new HashMap();
                hmParams.put("documentId", Long.class);
                HashMap valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
                Long documentId = (Long) (valueParams.get("documentId")
                        != null ? valueParams.get("documentId") : -1L);
                //ghi log dau vao
//                java.util.logging.Logger.getLogger(DocumentPublishControler.class.getName()).
//                        log(Level.SEVERE, "ham danh sach van ban bi thay the khi load chi tiet van ban cong bo, id van ban", valueParams.toString());
                DocumentPublishDAO documentPublishDao = new DocumentPublishDAO();
                Object result = documentPublishDao.getListDocAlter(documentId);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
            } catch (Exception e) {
                java.util.logging.Logger.getLogger(DocumentPublishControler.class.getName()).
                        log(Level.SEVERE, "getListDocAlter error: {0}", e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }
        return strResult;
    }

    /**
     * tim kiem van ban thay the cong bo tu dong
     *
     * @param isSecurity
     * @param strData
     * @param req
     * @return
     */
    public String actionSearchAlterDocAuto(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                String strAesKeyDecode = null;
                String decryptedData = "";
                if (isSecurity != null && "1".equals(isSecurity)) {
                    strAesKeyDecode = dataSessionGR.getStrAesKey();
                    decryptedData = SecurityControler.decodeDataByAes(strAesKeyDecode,
                            strData);
                }
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(decryptedData);
                LogUtils.logFunctionalStart(log);
                HashMap hmParams = new HashMap();
                hmParams.put("code", String.class);
                //ngay ban hanh
                hmParams.put("fromPromulgateDate", String.class);
                hmParams.put("toPromulgateDate", String.class);
                //ngay den
                hmParams.put("fromReceiveDate", String.class);
                hmParams.put("toReceiveDate", String.class);
                //han cuoi
                hmParams.put("fromDeadlineDate", String.class);
                hmParams.put("toDeadlineDate", String.class);
                //tieu de van ban
                hmParams.put("title", String.class);
                //so dang ky
                hmParams.put("registerNumber", String.class);
                //nguoi ky
                hmParams.put("signer", String.class);
                //noi dung van ban
                hmParams.put("content", String.class);
                //id hinh thuc
                //khong truyen, tim kiem tat ca
                hmParams.put("typeId", Long.class);
                //id linh vuc van ban
                //khong truyen, tim kiem tat ca
                hmParams.put("areaId", Long.class);
                //id nganh
                //khong truyen, tim kiem tat ca
                hmParams.put("industryId", Long.class);
                //nguoi ban hanh

                //sap xep theo
                //0: so ky hieu
                //1: ngay ban hanh
                //2: ngay den
                //3: han cuoi
                //4: nguoi ky
                //5: trich yeu
                hmParams.put("orderBy", Long.class);
                //sap xep tang giam
                //0: tang,1: giam
                hmParams.put("directOrder", Long.class);

                hmParams.put("startRecord", Long.class);
                hmParams.put("pageSize", Long.class);
                hmParams.put("isCount", String.class);
                //pham vi cong bo
                hmParams.put("rangePublished", Long.class);
                hmParams.put(ConstantsFieldParams.DOC_SCOPE_IDS, String.class);

                HashMap valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
                String code = (String) (valueParams.get("code") != null
                        ? valueParams.get("code") : "");

                String fromPromulgateDate = (String) (valueParams.
                        get("fromPromulgateDate") != null
                        ? valueParams.get("fromPromulgateDate") : "");
                String toPromulgateDate = (String) (valueParams.
                        get("toPromulgateDate") != null
                        ? valueParams.get("toPromulgateDate") : "");

                String fromReceiveDate = (String) (valueParams.
                        get("fromReceiveDate") != null
                        ? valueParams.get("fromReceiveDate") : "");
                String toReceiveDate = (String) (valueParams.
                        get("toReceiveDate") != null
                        ? valueParams.get("toReceiveDate") : "");

                String fromDeadlineDate = (String) (valueParams.
                        get("fromDeadlineDate") != null
                        ? valueParams.get("fromDeadlineDate") : "");
                String toDeadlineDate = (String) (valueParams.
                        get("toDeadlineDate") != null
                        ? valueParams.get("toDeadlineDate") : "");

                String title = (String) (valueParams.
                        get("title") != null
                        ? valueParams.get("title") : "");
                String registerNumber = (String) (valueParams.
                        get("registerNumber") != null
                        ? valueParams.get("registerNumber") : "");
                String signer = (String) (valueParams.
                        get("signer") != null
                        ? valueParams.get("signer") : "");
                String content = (String) (valueParams.
                        get("content") != null
                        ? valueParams.get("content") : "");
                Long typeId = (Long) (valueParams.get("typeId")
                        != null ? valueParams.get("typeId") : 0L);
                Long areaId = (Long) (valueParams.get("areaId")
                        != null ? valueParams.get("areaId") : 0L);
                Long industryId = (Long) (valueParams.get("industryId")
                        != null ? valueParams.get("industryId") : 0L);

                Long orderBy = (Long) (valueParams.get("orderBy")
                        != null ? valueParams.get("orderBy") : 0L);
                Long directOrder = (Long) (valueParams.get("directOrder")
                        != null ? valueParams.get("directOrder") : 0L);
                Long startRecord = (Long) (valueParams.get("startRecord") != null
                        ? valueParams.get("startRecord") : 0L);
                Long pageSize = (Long) (valueParams.get("pageSize")
                        != null ? valueParams.get("pageSize") : 10L);
                String isCount = (String) (valueParams.
                        get("isCount") != null
                        ? valueParams.get("isCount") : "");
//                Long rangePublished = (Long) (valueParams.get("rangePublished")
//                        != null ? valueParams.get("rangePublished") : 0L);
                String docScopeIdStr = (String) (valueParams.get(ConstantsFieldParams.DOC_SCOPE_IDS)
                        != null ? valueParams.get(ConstantsFieldParams.DOC_SCOPE_IDS) : null);

                List<Long> docScopeIds = null;
                if (!CommonUtils.isEmpty(docScopeIdStr)) {
                    JSONArray jsonArray = new JSONArray(docScopeIdStr);
                    if (jsonArray.length() > 0) {
                        docScopeIds = new ArrayList<>();
                        JSONObject jsonObject;
                        Long removalAlternativeDocumentId;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            jsonObject = jsonArray.getJSONObject(i);
                            if (!jsonObject.isNull(ConstantsFieldParams.SCOPE_ID)) {
                                removalAlternativeDocumentId = jsonObject.getLong(
                                        ConstantsFieldParams.SCOPE_ID);
                                docScopeIds.add(removalAlternativeDocumentId);
                            }
                        }
                    }
                }
                //ghi log dau vao
                DocumentPublishDAO documentPublishDao = new DocumentPublishDAO();
                //tim kiem advance
                Object result = documentPublishDao.
                        actionSearchAlterDocAuto(code, fromPromulgateDate,
                                toPromulgateDate, fromReceiveDate, toReceiveDate, fromDeadlineDate,
                                toDeadlineDate, title, registerNumber, signer, content,
                                typeId, areaId, industryId, orderBy, directOrder,
                                isCount, startRecord, pageSize, docScopeIds);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
            } catch (Exception e) {
                java.util.logging.Logger.getLogger(DocumentPublishControler.class.getName()).
                        log(Level.SEVERE, "actionSearchDocPublish error: {0}", e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }
        return strResult;
    }

    /**
     * lay don vi cha don vi nguoi ky cuoi cung
     *
     * @param request
     * @param strData
     * @param isSecurity
     * @return
     */
    public String getParentOrgLastSignOrg(String isSecurity, String strData,
            HttpServletRequest request) {

        String response;
        String[] keys = new String[]{
            ConstantsFieldParams.TEXT_ID
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, strData, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            response = FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
            return FunctionCommon.returnResultAfterLog(response, null, "Session timeout");
        } 
        if (!userGroup.checkUserId2()) {
            response = FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
            return FunctionCommon.returnResultAfterLog(response, null,
                    "Khong ton tai user tren he thong 2");
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            String strTextId = listValue.get(0);
            if (!CommonUtils.isEmpty(strTextId)) {
                // Id van ban danh dau
                Long textId = Long.parseLong(strTextId);
                // Lay don vi cha cua don vi nguoi ky cuoi van ban
                AutoDigitalSignDAO autoDigitalSignDAO = new AutoDigitalSignDAO();
                EntityVhrOrg parentOrg = autoDigitalSignDAO.getParentOrgLastSignOrg(
                        userGroup.getUserId2(), textId);
                // Thanh cong
                if (parentOrg != null) {
                    return FunctionCommon.responseResult(ErrorCode.SUCCESS,
                            parentOrg, userGroup);
                } // Khong thanh cong
                else {
                    response = FunctionCommon.responseResult(ErrorCode.ERR_NODATA,
                            null, null);
                    return FunctionCommon.returnResultAfterLog(response, userGroup.getUserId2(),
                            "Loi server");
                }
            } // Loi du lieu dau vao
            else {
                response = FunctionCommon.responseResult(ErrorCode.INPUT_INVALID,
                        null, null);
                return FunctionCommon.returnResultAfterLog(response, userGroup.getUserId2(),
                        "Loi du lieu dau vao");
            }
            // Ghi log ket thuc chuc nang
        } catch (Exception ex) {
            return FunctionCommon.returnResultAfterLog(userGroup, ex);
        }
    }
}
