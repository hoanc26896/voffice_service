/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.financedata.AdOrgDAO;
import com.viettel.voffice.database.dao.financedata.CChartGroupIndicatorDAO;
import com.viettel.voffice.database.dao.financedata.CIndicatorBaseDAO;
import com.viettel.voffice.database.dao.financedata.FinanceDataDAO;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityUserGroup;
//import com.viettel.voffice.database.entity.User.EntityUser;
//import com.viettel.voffice.database.entity.User.Vof2_EntityUser;
import com.viettel.voffice.database.entity.financedata.FinancialDataGridResult;
import com.viettel.voffice.database.entity.financedata.FinancialIndicator;
import com.viettel.voffice.database.entity.financedata.Indicator;
import com.viettel.voffice.database.entity.financedata.RegionResult;
import com.viettel.voffice.database.entity.financedata.Statistics;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.FileUtils;
import com.viettel.voffice.utils.LogUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * So lieu tai chinh
 *
 * @author thanght6
 * @since 3 Feb, 2017
 */
public class FinanceDataController {

    // Log file
    private static final Logger LOGGER = Logger.getLogger(DocumentController.class);

    // Ten cua class bao gom ca ten package
    private static final String CLASS_NAME = FinanceDataController.class.getName();

    /**
     * <b>Lay danh sach tieu chi co ban</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListIndicatorBase(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("[SLTC] getListIndicatorBase (Lay danh sach tieu chi co ban)"
                    + " - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }

        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
        log.setUserName(cardId);
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
        }
        // Ghi log bat dau chuc nang
        LogUtils.logFunctionalStart(log);
        try {
            // Them moi van ban
            CIndicatorBaseDAO dao = new CIndicatorBaseDAO();
            List<Indicator> listBasicIndicator = dao.getIndicatorListByStatusChart("/DTCP/CTCB/");
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, listBasicIndicator, aesKey);
        } catch (Exception ex) {
            LOGGER.error("[SLTC] getListIndicatorBase (Lay danh sach tieu chi co ban)"
                    + " - Exception - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Lay cay don vi theo id don vi tren Voffice 1.0</b><br>
     * Cay tu tinh level thu gon doi voi muc phan quyen cua tai khoan dang su
     * dung
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getRegionTreeByRoleSmart(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("[SLTC] getRegionTreeByRoleSmart (Lay cay don vi)"
                    + " - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Lay id don vi tren he thong 1
        Long groupId = userGroup.getGroupId1();
        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
        log.setUserName(cardId);
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
        }
        // Ghi log bat dau chuc nang
        LogUtils.logFunctionalStart(log);
        try {
            AdOrgDAO adOrgDAO = new AdOrgDAO();
            List<RegionResult> listOrg = adOrgDAO.getRegionTreeByRoleSmart(groupId.toString());
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, listOrg, aesKey);
        } catch (Exception ex) {
            LOGGER.error("[SLTC] getRegionTreeByRoleSmart (Lay cay don vi)"
                    + " - Exception - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Lay danh sach tieu chi theo nhom don vi</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getTreeListIndicator(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("[SLTC] getTreeListIndicator (Lay danh sach tieu chi theo nhom don vi)"
                    + " - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
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
        // Ghi log bat dau chuc nang        
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.ORG_ID,};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String orgId = listValue.get(0);
            // Lay danh sach tieu chi theo don vi
            CChartGroupIndicatorDAO cChartGroupIndicatorDAO = new CChartGroupIndicatorDAO();
            List<FinancialIndicator> listIndicator = cChartGroupIndicatorDAO
                    .getTreeListIndicator(orgId);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, listIndicator,
                    aesKey);
        } catch (Exception ex) {
            LOGGER.error("[SLTC] getTreeListIndicator (Lay danh sach tieu chi theo nhom don vi)"
                    + " - Exception - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Lay bao cao tai chinh</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getReportStatistics(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("[SLTC] getReportStatistics (Lay bao cao tai chinh)"
                    + " - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
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
                ConstantsFieldParams.REGION_ID,
                ConstantsFieldParams.KPI_ID,
                ConstantsFieldParams.STATUS_CHART,
                ConstantsFieldParams.TIME_CHART,
                ConstantsFieldParams.YEAR,
                ConstantsFieldParams.LIST_INDICATOR,
                ConstantsFieldParams.IS_CHART_COLUMN,};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Id don vi lay bieu do
            String regionId = listValue.get(0);
            // Loai bieu do the hien
            String kpiId = listValue.get(1);
            String statusChart = listValue.get(2);
            // Loai bieu do 1-thang, 2-quy, 3-nam
            int timeChart = 0;
            String strTimeChart = listValue.get(3);
            if (!CommonUtils.isEmpty(strTimeChart)) {
                timeChart = Integer.parseInt(strTimeChart);
            }
            // Nam ve bieu do
            int year = 0;
            String strYear = listValue.get(4);
            if (!CommonUtils.isEmpty(strYear)) {
                year = Integer.parseInt(strYear);
            }
            // Danh sach cac tieu chi can de ve bieu do, danh sach chuoi lay so lieu
            String strIndicatorCodes = listValue.get(5);
            String[] arrIndicatorCode = strIndicatorCodes.trim().split(",");
            List<String> listIndicatorCode = new ArrayList<>();
            for (int i = 0; i < arrIndicatorCode.length; i++) {
                if (!CommonUtils.isEmpty(arrIndicatorCode[i])) {
                    listIndicatorCode.add(arrIndicatorCode[i]);
                }
            }
            // Bieu do duong-0-false , cot-1-true
            boolean isChartColumn = false;
            String strIsChartColumn = listValue.get(6);
            if ("1".equals(strIsChartColumn)) {
                isChartColumn = true;
            }

            FinanceDataDAO financeDataDAO = new FinanceDataDAO();
            Statistics result = financeDataDAO.getReportStatistics(year, timeChart,
                    regionId, kpiId, statusChart, strYear, listIndicatorCode, isChartColumn);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error("[SLTC] getReportStatistics (Lay bao cao tai chinh)"
                    + " - Exception - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Lay bao cao bieu do chi phi bien dong</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getReportIndicatorData(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("[SLTC] getReportStatistics (Lay bao cao tai chinh)"
                    + " - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
        log.setUserName(cardId);
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        // Gan du lieu dau vao
        log.setParamList(data);
        // Ghi log bat dau chuc nang
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.YEAR,
                ConstantsFieldParams.BEFORE_YEAR,
                ConstantsFieldParams.CATEGORY_TIME,
                ConstantsFieldParams.ORG_ID,
                ConstantsFieldParams.INDICATOR_ID,
                ConstantsFieldParams.IS_CHART_COLUMN
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Nam hien tai ve bieu do
            int year = 0;
            String strYear = listValue.get(0);
            if (!CommonUtils.isEmpty(strYear)) {
                year = Integer.parseInt(strYear);
            }
            // So nam truoc do ve bieu do
            int beforeYear = 0;
            String strBeforeYear = listValue.get(1);
            if (!CommonUtils.isEmpty(strBeforeYear)) {
                beforeYear = Integer.parseInt(strBeforeYear);
            }
            // Loai bieu do: 1-thang, 2-quy, 3-nam
            int categoryTime = 0;
            String strCategoryTime = listValue.get(2);
            if (!CommonUtils.isEmpty(strCategoryTime)) {
                categoryTime = Integer.parseInt(strCategoryTime);
            }
            // Id don vi ve bieu do
            String orgId = listValue.get(3);
            // Id tieu chi
            String indicatorId = listValue.get(4);
            // Bieu do duong hay cot? 1-duong, 0-cot
            boolean isChartColumn = false;
            String strIsChartColumn = listValue.get(5);
            if ("1".equals(strIsChartColumn)) {
                isChartColumn = true;
            }

            FinanceDataDAO financeDataDAO = new FinanceDataDAO();
            Statistics result = financeDataDAO.getReportIndicatorData(year,
                    beforeYear, categoryTime, orgId, indicatorId, isChartColumn);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error("[SLTC] getReportStatistics (Lay bao cao tai chinh)"
                    + " - Exception - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Lay bao cao cua tieu chi con</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getReportIndicatorChildData(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("[SLTC] getReportStatistics (Lay bao cao tai chinh)"
                    + " - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
        log.setUserName(cardId);
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        // Gan du lieu dau vao
        log.setParamList(data);
        // Ghi log bat dau chuc nang
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.YEAR,
                ConstantsFieldParams.CATEGORY_TIME,
                ConstantsFieldParams.ORG_ID,
                ConstantsFieldParams.INDICATOR_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Nam lua chon de ve bieu do
            int year = 0;
            String strYear = listValue.get(0);
            if (!CommonUtils.isEmpty(strYear)) {
                year = Integer.parseInt(strYear);
            }
            // Loai bieu do: 1-thang, 2-quy, 3-nam
            int categoryTime = 0;
            String strCategoryTime = listValue.get(1);
            if (!CommonUtils.isEmpty(strCategoryTime)) {
                categoryTime = Integer.parseInt(strCategoryTime);
            }
            // Don vi lua chon de ve bieu do
            String orgId = listValue.get(2);
            // Tieu chi de ve bieu do
            String indicatorId = listValue.get(3);
            FinanceDataDAO financeDataDAO = new FinanceDataDAO();
            List<FinancialDataGridResult> result = financeDataDAO.getReportIndicatorChidlData(
                    year, categoryTime, orgId, indicatorId);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result,
                    aesKey);
        } catch (Exception ex) {
            LOGGER.error("[SLTC] getReportStatistics (Lay bao cao tai chinh)"
                    + " - Exception - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Download anh bieu do</b><br>
     *
     * @param request
     * @param path
     * @return
     */
    public Response downloadChartImage(HttpServletRequest request, String path) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Ghi log bat dau chuc nang
        LogUtils.logFunctionalStart(log);
        Response response = null;
        try {
            // Kiem tra duong dan anh
            if (!CommonUtils.isEmpty(path)) {
                path = new String(Base64.decodeBase64(path));
                File file = FileUtils.getFileByType(FileUtils.VIEW_IMAGE_REPORT_MOBILE, path);
                if (file == null || !file.exists()) {
                    LOGGER.error("[SLTC] downloadChartImage - file null hoac file khong ton tai"
                            + " - filePath: " + (file == null ? path : file.getPath()));
                    return response;
                }
                Response.ResponseBuilder responseBuilder = Response.ok(file, "image/jpeg");
                response = responseBuilder.build();
            }
        } catch (Exception ex) {
            LOGGER.error("downloadStaffImage - Exception:" + ex.getMessage(), ex);
        }
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return response;
    }

}
