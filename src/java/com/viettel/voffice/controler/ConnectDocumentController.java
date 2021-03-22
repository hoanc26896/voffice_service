package com.viettel.voffice.controler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.ConnectDocumentDAO;
import com.viettel.voffice.database.entity.EntityConnectDocOutDetail;
import com.viettel.voffice.database.entity.EntityConnectDocument;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.utils.CommonUtils;

public class ConnectDocumentController {

    private static final Logger logger = Logger.getLogger(ConnectDocumentController.class);

    public String getListConnectDocument(HttpServletRequest request, String data) {
        
        String[] keys = new String[] { "obj", "isCount", "pageSize", "startRecord" };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            logger.error("getListConnectDocument - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            String strObj = listValue.get(0);
            EntityConnectDocument obj = new EntityConnectDocument();
            Gson gson = new Gson();
            if (!CommonUtils.isEmpty(strObj)) {
                obj = gson.fromJson(strObj, EntityConnectDocument.class);
            }
            boolean isCount = "1".equals(listValue.get(1));
            Long pageLoad = !CommonUtils.isEmpty(listValue.get(2)) ? Long.valueOf(listValue.get(2)) : null;
            Long startPage = !CommonUtils.isEmpty(listValue.get(3)) ? Long.valueOf(listValue.get(3)) : null;
            
            ConnectDocumentDAO dao = new ConnectDocumentDAO();
            Object result = dao.getListConnectDocument(obj, startPage, pageLoad, isCount, null);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            logger.error("getListConnectDocument - Exception - username: " + userGroup.getCardId(), ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }
    
    public String addStateConnectDocument(HttpServletRequest request, String data) {
        
        String[] keys = new String[] { "docId", "processType", "docType", "comment", "id" };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            logger.error("getListConnectDocument - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            String comment = listValue.get(3);
            Long docType = !CommonUtils.isEmpty(listValue.get(2)) ? Long.valueOf(listValue.get(2)) : null;
            Long processType = !CommonUtils.isEmpty(listValue.get(1)) ? Long.valueOf(listValue.get(1)) : null;
            String docId = listValue.get(0);
            Long id = !CommonUtils.isEmpty(listValue.get(4)) ? Long.valueOf(listValue.get(4)) : null;
            
            ConnectDocumentDAO dao = new ConnectDocumentDAO();
            boolean result = dao.addStateConnectDocument(docId, processType, docType, comment, id, userGroup.getVof2_ItemEntityUser());
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result ? 1 : 0, userGroup);
        } catch (Exception ex) {
            logger.error("getListConnectDocument - Exception - username: " + userGroup.getCardId(), ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }
    
    public String getListConnectDocOutDetail(HttpServletRequest request, String data) {
        
        String[] keys = new String[] { "id" };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            logger.error("getListConnectDocOutDetail - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            Long id = !CommonUtils.isEmpty(listValue.get(0)) ? Long.valueOf(listValue.get(0)) : null;
            if (id == null) {
                return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
            }
            ConnectDocumentDAO dao = new ConnectDocumentDAO();
            List<EntityConnectDocOutDetail> result = dao.getListConnectDocOutDetail(new ArrayList<Long>(Arrays.asList(id)), null, true);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            logger.error("getListConnectDocOutDetail - Exception - username: " + userGroup.getCardId(), ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }
}