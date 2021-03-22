/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.constants.InputStreamWithFileDeletion;
import com.viettel.voffice.database.dao.businessdata.BusinessDepartmentDAO;
import com.viettel.voffice.database.dao.businessdata.BusinessDepartmentFunctionDAO;
import com.viettel.voffice.database.dao.businessdata.BusinessImgReportDAO;
import com.viettel.voffice.database.dao.businessdata.BusinessReportTypeGroupDAO;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.User.EntityUser;
import com.viettel.voffice.database.entity.businessdata.EntityDepartment;
import com.viettel.voffice.database.entity.businessdata.EntityImage;
import com.viettel.voffice.database.entity.businessdata.EntityReport;
import com.viettel.voffice.database.entity.financedata.BusinessReportType;
import com.viettel.voffice.database.entity.financedata.FinancialImgReport;
import com.viettel.voffice.database.entity.financedata.RegionResult;
import com.viettel.voffice.utils.BusinessDataConnector;
import com.viettel.voffice.utils.FileUtils;
import com.viettel.voffice.utils.LogUtils;
import java.io.File;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 *
 * @author thanght6
 */
public class BusinessDataController {

    /** Log file */
    private static final Logger LOGGER = Logger.getLogger(BusinessDataController.class);

    // Ten cua class bao gom ca ten package
    private static final String CLASS_NAME = BusinessDataController.class.getName();

    public String getListDepartFromMapFN(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("[SLKD] getListDepartFromMapFN (Lay danh sach tieu chi co ban)"
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
            BusinessDepartmentFunctionDAO dao = new BusinessDepartmentFunctionDAO();
            String userId = userGroup.getUserId1().toString();
            String groupId = userGroup.getGroupId1().toString();
            List<RegionResult> result = dao.getListDepartFromMapFN(groupId, userId);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error("[SLKD] getListIndicatorBase (Lay danh sach tieu chi co ban)"
                    + " - Exception - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    public String getListDepartFolowFunc(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("[SLKD] getListDepartFolowFunc (Lay cay don vi theo phong ban chuc nang)"
                    + " - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
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
                "departmentFnCode",};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String departmentFnCode = listValue.get(0);
            String userId = userGroup.getUserId1().toString();
            String groupId = userGroup.getGroupId1().toString();
            BusinessDepartmentDAO dao = new BusinessDepartmentDAO();
            List<RegionResult> result = dao.getListDepartFolowFunc(groupId, userId, departmentFnCode);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error("[SLKD] getListDepartFolowFunc (Lay cay don vi theo phong ban chuc nang)"
                    + " - Exception - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Lay danh sach tieu chi theo phong ban chuc nang va don vi tuong
     * ung</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListIndicatorFolowDepartFunc(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("[SLKD] getListIndicatorFolowDepartFunc (Lay danh sach tieu chi)"
                    + " - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
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
                "departmentFnCode",
                "departmentCode",};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Ma phong ban chuc nang
            String departmentFnCode = listValue.get(0);
            // Ma don vi
            String departmentCode = listValue.get(1);
            // Lay danh sach tieu chi tu database
            BusinessReportTypeGroupDAO dao = new BusinessReportTypeGroupDAO();
            List<BusinessReportType> result = dao.getListIndicatorFolowDepartFunc(
                    departmentCode, departmentFnCode);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error("[SLKD] getListIndicatorFolowDepartFunc (Lay danh sach tieu chi)"
                    + " - Exception - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Lay danh sach tieu chi theo phong ban chuc nang va don vi tuong
     * ung</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListImgPath(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("[SLKD] getListImgPath (Lay danh sach duong dan anh cua tieu chi)"
                    + " - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
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
                "strDate",
                "departCode",
                "reportType",};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Ngay
            String date = listValue.get(0);
            // Ma phong ban chuc nang
            String departmentCode = listValue.get(1);
            // Ma don vi
            String reportTypeGroupCode = listValue.get(2);
            // Lay danh sach tieu chi tu database
            BusinessImgReportDAO dao = new BusinessImgReportDAO();
            List<FinancialImgReport> result = dao.getImageReportList(date,
                    departmentCode, reportTypeGroupCode);

            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error("[SLKD] getListImgPath (Lay danh sach duong dan anh cua tieu chi)"
                    + " - Exception - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    public Response downloadReportImage(HttpServletRequest request, String path) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Ghi log bat dau chuc nang
        LogUtils.logFunctionalStart(log);
        Response response = null;
        try {
            // Tao file tam luu anh download tu server BI ve
            File tmpFile = new File(request.getRealPath("/")
                    + (new Date()).getTime() + FileUtils.JPG_IMAGE_FILE_EXTENSION);
            BusinessImgReportDAO dao = new BusinessImgReportDAO();
            if (dao.downloadImage(path, tmpFile.getPath())) {
                Response.ResponseBuilder responseBuilder = Response.ok(
                        new InputStreamWithFileDeletion(tmpFile), "image/jpeg");
                response = responseBuilder.build();
            }
        } catch (Exception ex) {
            LOGGER.error("[SLKD] downloadReportImage - Exception:" + ex.getMessage(), ex);
        }
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return response;
    }

    /**
     * <b>Lay cay don vi</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getDepartment(HttpServletRequest request,
            String data, String isSecurity) {

        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, null);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("[SLKD] getDepartment (Lay danh sach don vi) - "
                    + userGroup.getEnumErrCode().toString());
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            List<EntityDepartment> listDepartment = BusinessDataConnector.getDepartment(userGroup);
            List<RegionResult> listRegion = RegionResult.copy(listDepartment);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, listRegion, userGroup);
        } catch (Exception ex) {
            LOGGER.error("[SLKD] getDepartment (Lay danh sach don vi) - username: "
                    + userGroup.getCardId() + " - Exception!", ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Lay danh sach tieu chi theo don vi</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListReport(HttpServletRequest request,
            String data, String isSecurity) {

        String[] keys = new String[]{
            ConstantsFieldParams.DEPARTMENT_ID
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("[SLKD] getListReport (Lay danh sach tieu chi) - "
                    + userGroup.getEnumErrCode().toString());
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            Long departmentId = Long.parseLong(userGroup.getListParamsFromClient().get(0));
            List<EntityReport> listReport = BusinessDataConnector.getListReport(
                    userGroup, departmentId);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, listReport, userGroup);
        } catch (Exception ex) {
            LOGGER.error("[SLKD] getListReport (Lay danh sach tieu chi) - username: "
                    + userGroup.getCardId() + " - Exception!", ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Download anh</b><br>
     *
     * @param request
     * @param reportType
     * @param imageId
     * @param date
     * @param departmentCode
     * @return
     */
    public Response downloadImageReport(HttpServletRequest request,
            Long reportType, Long imageId, String date, String departmentCode) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Ghi log bat dau chuc nang
        LogUtils.logFunctionalStart(log);
        Response response = null;
        try {
            // Tao file tam luu anh download tu server BI ve
            EntityUserGroup userGroup = new EntityUserGroup();
            EntityUser user1 = new EntityUser();
            user1.setUserId(-12L);
            userGroup.setItemEntityUser(user1);
            Date startDownloadImage = new Date();
            EntityImage image = BusinessDataConnector.downloadImageReport(
                    userGroup, reportType, imageId, date, departmentCode);
            Date endDownloadImage = new Date();
            
            log.setDescription("Thoi gian download anh:"+(endDownloadImage.getTime() - startDownloadImage.getTime()));
            if (image == null) {
                LOGGER.error("[SLKD] downloadImageReport - Loi khong tai duoc anh!");
                return response;
            }
            Response.ResponseBuilder responseBuilder;
            if (image.getContent() == null) {
                responseBuilder = Response.ok();
            } else {
                responseBuilder = Response.ok(image.getContent(), "image/jpeg");
            }
            responseBuilder.header("error-code", image.getErrorCode());
            responseBuilder.header("error-description", image.getErrorDescription());
            response = responseBuilder.build();
        } catch (Exception ex) {
            LOGGER.error("[SLKD] downloadImageReport - Exception!", ex);
        }
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return response;
    }

    /**
     * <b>Lay danh sach anh slide</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListSilde(HttpServletRequest request,
            String data, String isSecurity) {

        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, null);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("[SLKD] getListSilde (Lay danh sach anh slide) - "
                    + userGroup.getEnumErrCode().toString());
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            List<EntityImage> listImage = BusinessDataConnector
                    .getListSlide(userGroup);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, listImage, userGroup);
        } catch (Exception ex) {
            LOGGER.error("[SLKD] getListSilde (Lay danh sach anh slide) - username: "
                    + userGroup.getCardId() + " - Exception!", ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    /**
     * <b>Lay mo ta anh</b>
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     */
    public String getImageReportDesc(HttpServletRequest request,
            String data, String isSecurity) {

        String[] keys = new String[]{
            "reportType",
            "imageId"
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("[SLKD] getImageReportDesc (Lay mo ta anh) - "
                    + userGroup.getEnumErrCode().toString());
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            Long reportType = Long.parseLong(listValue.get(0));
            Long imageId = Long.parseLong(listValue.get(1));
            EntityImage image = BusinessDataConnector.getImageReportDesc(userGroup, reportType, imageId);
//            if (image == null) {
//                LOGGER.error("[SLKD] getImageReportDesc (Lay mo ta anh) - image null - username: "
//                        + userGroup.getCardId());
//                return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
//            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, image, userGroup);
        } catch (Exception ex) {
            LOGGER.error("[SLKD] getImageReportDesc (Lay mo ta anh) - username: "
                    + userGroup.getCardId() + " - Exception!", ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
 
}