package com.viettel.voffice.controler;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.staff.OrgCriteriaDAO;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityOrgCriteriaConfig;
import com.viettel.voffice.database.entity.EntityOrgCriteriaHistory;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.LogUtils;

@SuppressWarnings("deprecation")
public class OrgCriteriaController {
    private static final Logger LOGGER = Logger.getLogger(OrgCriteriaController.class);
    private static final String CLASS_NAME = OrgCriteriaController.class.getName();

    public String insertOrgCriteriaConfig(String security, String data, HttpServletRequest request) {
        try {
            EntityLog log = new EntityLog(request, CLASS_NAME);
            String[] keys = new String[] { "orgCriteria" };
            EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
            if (userGroup.getCheckSessionOk()) {
                String cardId = userGroup.getVof2_ItemEntityUser().getStrCardNumber();
                Long userId = userGroup.getVof2_ItemEntityUser().getUserId();
                log.setUserName(cardId);
                LogUtils.logFunctionalStart(log);
                List<String> listValue = userGroup.getListParamsFromClient();

                String orgCriteriaStr = listValue.get(0);
                Gson gson = new GsonBuilder().setDateFormat("dd/MM/yyyy hh:mm:ss").create();
                EntityOrgCriteriaConfig orgCriteria = null;
                if (!CommonUtils.isEmpty(orgCriteriaStr)) {
                    orgCriteria = gson.fromJson(orgCriteriaStr, EntityOrgCriteriaConfig.class);
                }
                if (orgCriteria == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                }

                OrgCriteriaDAO dao = new OrgCriteriaDAO();
                Long result = dao.insertOrgCriteriaConfig(orgCriteria, userId);
                LogUtils.logFunctionalEnd(log);
                return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
            } else {
                return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    public String getDetailOrgCriteriaConfig(String security, String data, HttpServletRequest request) {
        try {
            EntityLog log = new EntityLog(request, CLASS_NAME);
            String[] keys = new String[] { "orgId" };
            EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
            if (userGroup.getCheckSessionOk()) {
                String cardId = userGroup.getVof2_ItemEntityUser().getStrCardNumber();
                log.setUserName(cardId);
                LogUtils.logFunctionalStart(log);
                List<String> listValue = userGroup.getListParamsFromClient();

                String orgIdStr = listValue.get(0);
                Long orgId = null;
                if (CommonUtils.isInteger(orgIdStr)) {
                    orgId = Long.valueOf(orgIdStr);
                }
                if (orgId == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                }

                OrgCriteriaDAO dao = new OrgCriteriaDAO();
                EntityOrgCriteriaConfig result = dao.getDetailOrgCriteriaConfig(orgId);
                LogUtils.logFunctionalEnd(log);
                return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
            } else {
                return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    public String getListOrgCriteriaConfig(String security, String data, HttpServletRequest request) {
        try {
            EntityLog log = new EntityLog(request, CLASS_NAME);
            String[] keys = new String[] {};
            EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
            if (userGroup.getCheckSessionOk()) {
                OrgCriteriaDAO dao = new OrgCriteriaDAO();
                List<EntityOrgCriteriaConfig> result = dao.getListOrgCriteriaConfig();
                LogUtils.logFunctionalEnd(log);
                return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
            } else {
                return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    public String getListOrgCriteriaHistory(String security, String data, HttpServletRequest request) {
        try {
            EntityLog log = new EntityLog(request, CLASS_NAME);
            String[] keys = new String[] { "orgCriteriaMapId", "type", "period", "mapType" };
            EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
            if (userGroup.getCheckSessionOk()) {
                List<String> listValue = userGroup.getListParamsFromClient();

                String orgCriteriaMapIdStr = listValue.get(0);
                Long orgCriteriaMapId = null;
                if (CommonUtils.isInteger(orgCriteriaMapIdStr)) {
                    orgCriteriaMapId = Long.valueOf(orgCriteriaMapIdStr);
                }
                String typeStr = listValue.get(1);
                Integer type = null;
                if (CommonUtils.isInteger(typeStr)) {
                    type = Integer.valueOf(typeStr);
                }
                String period = listValue.get(2);
                Integer mapType = null;
                if (CommonUtils.isInteger(listValue.get(3))) {
                    mapType = Integer.valueOf(listValue.get(3));
                }
                if (orgCriteriaMapId == null || type == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                }

                OrgCriteriaDAO dao = new OrgCriteriaDAO();
                List<EntityOrgCriteriaHistory> result = dao.getListOrgCriteriaHistory(orgCriteriaMapId, type, period, mapType);
                LogUtils.logFunctionalEnd(log);
                return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
            } else {
                return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }
}
