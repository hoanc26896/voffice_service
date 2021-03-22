/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.logAction.ActionLogMobileDAO;
import com.viettel.voffice.database.dao.text.TextPartnerDAO;
import com.viettel.voffice.database.dao.text.TextPartnerLogDAO;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.cm.EntityCompany;
import com.viettel.voffice.database.entity.cm.EntityListCompany;
import com.viettel.voffice.database.entity.text.EntityTextAttachPartner;
import com.viettel.voffice.database.entity.text.EntityTextPartner;
import com.viettel.voffice.utils.CMConnector;
import com.viettel.voffice.utils.CommonUtils;

/**
 *
 * @author thanght6
 */
public class CMController {
    
    /** Log file */
    private static final Logger LOGGER = Logger.getLogger(CMController.class);
    
    /**
     * <b>Lay danh sach doanh nghiep</b><br>
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    public String listCompany(HttpServletRequest request, String data,
            String isSecurity) {

        String[] keys = new String[]{
            "keyword",
            "type",
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        String errorDesc = "[CM] listCompany (Lay danh sach doanh nghiep) - ";
        if (!userGroup.getCheckSessionOk()) {
            errorDesc += userGroup.getEnumErrCode().toString();
            LOGGER.error(errorDesc);
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            // Lay du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            String keyword = listValue.get(0);
            Integer type = null;
            if (!CommonUtils.isEmpty(listValue.get(1))) {
                type = Integer.parseInt(listValue.get(1));
            }
            EntityListCompany result;
            // Lay lich su trinh ky, chuyen van ban den doi tac
            if (type != null) {
                TextPartnerLogDAO textPartnerLogDAO = new TextPartnerLogDAO();
                List<EntityCompany> listCompany = textPartnerLogDAO.getListLog(
                        userGroup.getUserId2(), type);
                result = new EntityListCompany();
                result.setCount(CommonUtils.isEmpty(listCompany) ? 0 : listCompany.size());
                result.setCompanies(listCompany);
            } // Tim kiem doi tac
            else {
                result = CMConnector.listCompany(userGroup, keyword, null, null);
            }            
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            errorDesc += "username: " + userGroup.getCardId() + " - Exception!";
            LOGGER.error(errorDesc, ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    /**
     * <b>Trinh ky van ban sang bo cong thuong</b><br>
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    public String createSignDocument(HttpServletRequest request, String data,
            String isSecurity) {

        String[] keys = new String[]{
            ConstantsFieldParams.TEXT_ID,
            "listCompany"
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        String errorDesc = "[CM] createSignDocument (Trinh ky van ban) - ";
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            errorDesc += userGroup.getEnumErrCode().toString();
            LOGGER.error(errorDesc);
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            // Lay du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            Long textId = Long.parseLong(listValue.get(0));
            String strListCompany = listValue.get(1);
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<EntityCompany>>(){}.getType();
            List<EntityCompany> listCompany = gson.fromJson(strListCompany, type);
            TextPartnerDAO textPartnerDAO = new TextPartnerDAO();
            int result = textPartnerDAO.createSignDocument(userGroup, textId, listCompany) ? 1 : 0;
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            errorDesc += "username: " + userGroup.getCardId() + " - Exception!";
            LOGGER.error(errorDesc, ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    /**
     * <b>Tim kiem van ban doi tac</b>
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    public String search(HttpServletRequest request, String data,
            String isSecurity) {

        String[] keys = new String[]{
            ConstantsFieldParams.KEYWORD,
            "textPartner",
            ConstantsFieldParams.START_RECORD,
            ConstantsFieldParams.PAGE_SIZE
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        String errorDesc = "[CM] createSignDocument (Trinh ky van ban) - ";
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            errorDesc += userGroup.getEnumErrCode().toString();
            LOGGER.error(errorDesc);
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        // Lay danh sach don vi co vai tro van thu
        List<Long> listSecretaryOrgId = userGroup.getVof2_ItemEntityUser().getListSecretaryVhrOrg();
//        if (CommonUtils.isEmpty(listSecretaryOrgId)) {
//            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
//        }
        try {
            // Lay du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            // Tu khoa tim kiem nhanh
            String keyword = listValue.get(0);
            // Doi tuong van ban muc tieu de tim kiem
            String strTextPartner = listValue.get(1);
            EntityTextPartner textPartner;
            Gson gson = new Gson();
            textPartner = gson.fromJson(strTextPartner, EntityTextPartner.class);
            if (!CommonUtils.isEmpty(keyword)) {
                textPartner.setIsQuickSearch(true);
                textPartner.setCmTextId(keyword);
                textPartner.setTitle(keyword);
                textPartner.setTypeName(keyword);
            }            
            // Vi tri lay ra
            String strStartRecord = listValue.get(2);
            Long startRecord = 0L;
            if (!CommonUtils.isEmpty(strStartRecord)) {
                startRecord = Long.parseLong(strStartRecord);
            }
            // So ban ghi lay ra
            String strPageSize = listValue.get(3);
            Long pageSize = 10L;
            if (!CommonUtils.isEmpty(strPageSize)) {
                pageSize = Long.parseLong(strPageSize);
            }
            TextPartnerDAO textPartnerDAO = new TextPartnerDAO();
            List<EntityTextPartner> listTextPartner = textPartnerDAO.search(userGroup.getUserId2(),
                    textPartner, startRecord, pageSize, listSecretaryOrgId, false);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, listTextPartner, userGroup);
        } catch (Exception ex) {
            errorDesc += "username: " + userGroup.getCardId() + " - Exception!";
            LOGGER.error(errorDesc, ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    /**
     * <b>Nhan thong bao</b>
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    public String notify(HttpServletRequest request, String data,
            String isSecurity) {

        Date startTime = new Date();
        String[] keys = new String[]{
            ConstantsFieldParams.TYPE,
            ConstantsFieldParams.TEXT_ID,
            "taxCode"
        };
        String response;
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        String errorDesc = "[CM] notify (Thong bao) - ";
        if (!userGroup.getCheckSessionOk()) {
            errorDesc += userGroup.getEnumErrCode().toString();
            LOGGER.error(errorDesc);
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            // Lay du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            int type = Integer.parseInt(listValue.get(0));
            String documentId = listValue.get(1);
            String taxCode = listValue.get(2);
            int result = 0;
            TextPartnerDAO textPartnerDAO = new TextPartnerDAO();
            switch (type) {
                // Co van ban trinh ky den
                case 1:
                // Co van ban chuyen den
                case 3:
                    result = textPartnerDAO.receiveDocument(userGroup, documentId,
                            type, taxCode) ? 1 : 0;
                    break;
                // Co van ban duoc cap nhat trang thai
                case 2:
                    result = textPartnerDAO.updateStateWhenPartnerNotified(userGroup, documentId) ? 1 : 0;
                    break;
            }
            response = FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            errorDesc += "username: " + userGroup.getCardId() + " - Exception!";
            LOGGER.error(errorDesc, ex);
            response = FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
        Date endTime = new Date();
        ActionLogMobileDAO actionLogMobileDAO = new ActionLogMobileDAO();
        actionLogMobileDAO.insert(userGroup.getUserId1(), userGroup.getCardId(),
                startTime, endTime, "CM.notify", userGroup.getInputData(),
                null, null, null, null, null);
        return response;
    }
    
    /**
     * <b>Cap nhat trang thai van ban doi tac trinh sang Voffice sau khi van ban ket thuc luong</b>
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    public String updateStateDocument(HttpServletRequest request, String data,
            String isSecurity) {

        String[] keys = new String[]{
            "textPartnerId",
            "typeUpdate"
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        String errorDesc = "[CM] updateStateDocument (Cap nhat trang thai van ban) - ";
        if (!userGroup.getCheckSessionOk()) {
            errorDesc += userGroup.getEnumErrCode().toString();
            LOGGER.error(errorDesc);
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            // Lay du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            Long textPartnerId = Long.parseLong(listValue.get(0));
            Integer typeUpdate = null;
            if (!CommonUtils.isEmpty(listValue.get(1))) {
                typeUpdate = Integer.parseInt(listValue.get(1));
            }
            TextPartnerDAO textPartnerDAO = new TextPartnerDAO();
            int result = textPartnerDAO.updateStateDocument(userGroup, textPartnerId, typeUpdate, null) ? 1 : 0;
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            errorDesc += "username: " + userGroup.getCardId() + " - Exception!";
            LOGGER.error(errorDesc, ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Chuyen van ban</b>
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    public String sendDocument(HttpServletRequest request, String data,
            String isSecurity) {

        String[] keys = new String[]{
            "textPartnerId",
            "cmTextId",
            "listCompany",
            "comment"
        };        
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        String errorDesc = "[CM] sendDocument (Chuyen van ban) - ";
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            errorDesc += userGroup.getEnumErrCode().toString();
            LOGGER.error(errorDesc);
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            // Lay du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            Long textPartnerId = 0L;
            if (!CommonUtils.isEmpty(listValue.get(0))) {
                textPartnerId = Long.parseLong(listValue.get(0));
            }
            String cmTextId = listValue.get(1);
            String strListCompany = listValue.get(2);
            String comment = listValue.get(3);
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<EntityCompany>>(){}.getType();
            List<EntityCompany> listCompany = gson.fromJson(strListCompany, type);
            // Thuc hien chuyen van ban
            TextPartnerDAO textPartnerDAO = new TextPartnerDAO();
            int result = textPartnerDAO.sendDocument(userGroup, textPartnerId,
                    cmTextId, listCompany, comment) ? 1 : 0;
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            errorDesc += "username: " + userGroup.getCardId() + " - Exception!";
            LOGGER.error(errorDesc, ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Xac thuc van ban</b>
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    public String trustFile(HttpServletRequest request, String data,
            String isSecurity) {
        
        String[] keys = new String[] {
            "textPartnerId"
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        String errorDesc = "[CM] trustFile (Xac thuc van ban) - ";
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            errorDesc += userGroup.getEnumErrCode().toString();
            LOGGER.error(errorDesc);
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            // Lay du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            Long textPartnerId = Long.parseLong(listValue.get(0));
            // Thuc hien chuyen van ban
            TextPartnerDAO textPartnerDAO = new TextPartnerDAO();
            int result = textPartnerDAO.trustFile(userGroup, textPartnerId) ? 1 : 0;
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            errorDesc += "username: " + userGroup.getCardId() + " - Exception!";
            LOGGER.error(errorDesc, ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    /**
     * <b>Copy file ra thu muc tam</b>
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    public String copyToTmpFolder(HttpServletRequest request, String data,
            String isSecurity) {
        
        String[] keys = new String[] {
            "textPartnerId",
            "isDocument"
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        String errorDesc = "[CM] copyToTmpFolder (Copy file ra thu muc tam) - ";
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            errorDesc += userGroup.getEnumErrCode().toString();
            LOGGER.error(errorDesc);
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            // Lay du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            Long textPartnerId = Long.parseLong(listValue.get(0));
            boolean isDocument = "1".equals(listValue.get(1));
            // Thuc hien chuyen van ban
            TextPartnerDAO textPartnerDAO = new TextPartnerDAO();
            List<EntityTextAttachPartner> listAttachment = textPartnerDAO.copyToTmpFolder(
                    userGroup.getUserId2(), textPartnerId, isDocument);
            if (CommonUtils.isEmpty(listAttachment)) {
                return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, listAttachment, userGroup);
        } catch (Exception ex) {
            errorDesc += "username: " + userGroup.getCardId() + " - Exception!";
            LOGGER.error(errorDesc, ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    /**
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    public String checkPromulgation(HttpServletRequest request, String data,
            String isSecurity) {
        
        String[] keys = new String[] {
            "vofficeTextId"
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        String errorDesc = "[CM] checkPromulgation (Kiem tra de ban hanh van ban) - ";
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            errorDesc += userGroup.getEnumErrCode().toString();
            LOGGER.error(errorDesc);
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            // Lay du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            Long vofficeTextId = Long.parseLong(listValue.get(0));
            // Thuc hien chuyen van ban
            TextPartnerDAO textPartnerDAO = new TextPartnerDAO();
            String registerNumber = textPartnerDAO.checkPromulgation(userGroup.getUserId2(),
                    vofficeTextId);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, registerNumber, userGroup);
        } catch (Exception ex) {
            errorDesc += "username: " + userGroup.getCardId() + " - Exception!";
            LOGGER.error(errorDesc, ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

}
