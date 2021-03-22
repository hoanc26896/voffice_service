/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
//import com.itextpdf.text.pdf.PdfReader;
import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.staff.ImageSignDao;
import com.viettel.voffice.database.dao.file.DownloadFileDocumentDAO;
import com.viettel.voffice.database.dao.staff.StaffImageSignDAO;
import com.viettel.voffice.database.dao.staff.UserDAO;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.EntityStaffImageSign;
import com.viettel.voffice.database.entity.User.EntityUser;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.FileUtils;
import com.viettel.voffice.utils.LogUtils;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author outsourceTeam/Sonnd
 * @version 1.0
 * @since
 */
public class ImageSignController {

    private static final Logger LOGGER = Logger.getLogger(ImageSignController.class);
    // Ten cua class bao gom ca ten package
    private static final String CLASS_NAME = ImageSignController.class.getName();

    /**
     * <b>Them anh chu ky</b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String addSignImage(HttpServletRequest request,
            String data, String isSecurity) {

        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            LOGGER.error("addSignImage - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        try {
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[]{
                ConstantsFieldParams.LIST_STAFF_IMAGE_SIGN
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String strListStaffImageSign = listValue.get(0);
            if (CommonUtils.isEmpty(strListStaffImageSign)) {
                LOGGER.error("addSignImage - username: " + userGroup.getCardId()
                        + " - Loi khong co danh sach anh chu ky them moi!");
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID,
                        null, null);
            }
            Type listStaffImageSignType = new TypeToken<ArrayList<EntityStaffImageSign>>() {
            }.getType();
            Gson gson = new Gson();
            List<EntityStaffImageSign> listStaffImageSign = gson.fromJson(
                    strListStaffImageSign, listStaffImageSignType);
            StaffImageSignDAO dao = new StaffImageSignDAO();
            int result = 0;
            if (dao.addSignatureImage(userGroup.getUserId2(), listStaffImageSign)) {
                result = 1;
            }
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result,
                    userGroup.getStrAesKey());
        } catch (JSONException | JsonSyntaxException ex) {
            LOGGER.error("addSignImage - username: " + userGroup.getCardId()
                    + " - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Chinh sua anh chu ky</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String editSignImage(HttpServletRequest request, String data, String isSecurity) {

        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            LOGGER.error("editSignImage (Chinh sua anh chu ky) - "
                    + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        try {
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[]{
                ConstantsFieldParams.SIGN_STAFF_IMAGE_ID,
                ConstantsFieldParams.SIGN_IMAGE_PATH,
                "name",
                ConstantsFieldParams.SIGN_TIME_ACTIVE_FROM,
                ConstantsFieldParams.STATUS,
                ConstantsFieldParams.STORAGE,
                ConstantsFieldParams.SIGN_TIME_ACTIVE_TO,
                ConstantsFieldParams.TYPE,
                ConstantsFieldParams.CODE
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // ID anh chu ky
            Long staffImageId = Long.parseLong(listValue.get(0));
            // Duong dan anh chu ky
            String path = listValue.get(1);
            // Ten cua anh ky
            String name = listValue.get(2);
            // Thoi gian bat dau hieu luc
            String fromDateActive = listValue.get(3);
            // Trang thai su dung cua anh chu ky
            int status = Integer.parseInt(listValue.get(4));
            // Storage 
            String storage = listValue.get(5);
            // Thoi gian het hieu luc
            String toDateActive = listValue.get(6);
            // Loai anh chu ky
            int type = Integer.parseInt(listValue.get(7));
            // Ma nhan vien cua nguoi so huu anh chu ky
            String code = listValue.get(8);
            // Tao doi tuong anh chu ky
            EntityStaffImageSign staffImageSign = new EntityStaffImageSign();
            staffImageSign.setStaffImageSignId(staffImageId);
            staffImageSign.setPath(path);
            staffImageSign.setName(name);
            staffImageSign.setFromDateActive(fromDateActive);
            staffImageSign.setStatus(status);
            staffImageSign.setStorage(storage);
            staffImageSign.setToDateActive(toDateActive);
            staffImageSign.setType(type);
            staffImageSign.setCode(code);

            StaffImageSignDAO dao = new StaffImageSignDAO();
            int result = 0;
            if (dao.editSignatureImage(userGroup.getUserId2(), staffImageSign)) {
                result = 1;
            }
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(userGroup.getKpiLog());
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result,
                    userGroup.getStrAesKey());
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error("editSignImage (Chinh sua anh chu ky) - username: "
                    + userGroup.getCardId() + " - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Tim kiem anh chu ky</b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String search(HttpServletRequest request, String data, String isSecurity) {

        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            LOGGER.error("search (Tim kiem anh chu ky) - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        try {
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[]{
                ConstantsFieldParams.SIGN_IMAGE_STAFF_CODE,
                ConstantsFieldParams.SIGN_IMAGE_STAFF_VOFF2_ID,
                ConstantsFieldParams.IS_REQUEST_TO_SIGN_TEXT,
                ConstantsFieldParams.TEXT_CREATED_DATE,
                ConstantsFieldParams.TEXT_SIGNED_DATE,
                "imageSignId"
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Ma nhan vien
            String employeeCode = listValue.get(0);
            // ID nhan vien tren he thong 2
            Long staffId2 = Long.parseLong(listValue.get(1));
            // Lay danh sach anh chu ky tu man hinh trinh ky van ban
            Integer isRequestToSignText = null;
            if (!CommonUtils.isEmpty(listValue.get(2))) {
                isRequestToSignText = Integer.parseInt(listValue.get(2));
            }
            // Ngay tao van ban trinh ky neu dang chinh sua van ban va chon lai
            // anh chu ky cua nguoi ky
            String textCreatedDate = listValue.get(3);
            // Ngay ky van ban khi lay danh sach anh chu ky de thay anh cua nguoi da ky van ban
            String textSignedDate = listValue.get(4);
            // Id anh ky duoc gan truoc do
            Long staffImageSignId = null;
            if (!CommonUtils.isEmpty(listValue.get(5))) {
                staffImageSignId = Long.parseLong(listValue.get(5));
            }
            UserDAO userDao = new UserDAO();
            EntityUser user = userDao.getUserInforByCardId(employeeCode);
            // ID cua nhan vien lay anh tren he thong 1
            Long staffId1 = 0L;
            if (user != null) {
                staffId1 = user.getUserId();
            }
            StaffImageSignDAO staffImageSignDAO = new StaffImageSignDAO();
            List<EntityStaffImageSign> listSignatureImage = staffImageSignDAO
                    .getSignatureImageList(userGroup.getUserId2(), staffId1,
                            staffId2, isRequestToSignText, textCreatedDate, textSignedDate, staffImageSignId);
            if (listSignatureImage == null) {
                LOGGER.error("search (Tim kiem anh chu ky) - username: "
                        + userGroup.getCardId() + " - Loi server!");
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(userGroup.getKpiLog());
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, listSignatureImage,
                    userGroup.getStrAesKey());
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error("search (Tim kiem anh chu ky) - username: " + userGroup.getCardId()
                    + " - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Download noi dung file</b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public Response reviewImage(HttpServletRequest request, String data,
            String isSecurity) {

        Response response = null;
        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            LOGGER.error("reviewImage (Xem anh chu ky) - " + userGroup.getEnumErrCode().toString());
            return response;
        }
        try {
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[]{
                ConstantsFieldParams.SIGN_IMAGE_PATH,
                ConstantsFieldParams.STORAGE
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Duong dan file anh
            String imagePath = listValue.get(0);
            // Storage luu tru
            String storage = listValue.get(1);
            // Ten file
            String fileName = "template_review.pdf";
            // Lay file anh theo duong dan va storage
            File file = FileUtils.getFileByType(FileUtils.SIGNATURE_IMAGE_FILE_TYPE,
                    imagePath, storage);
            if (file != null && file.exists()) {
                imagePath = file.getPath();
            }
            InputStream inputStream = null;
            ImageSignDao dao = new ImageSignDao();
            // Llay duong dan file template trong config
            String pathFileTemplate = CommonUtils.getAppConfigValue("pathImageTemplate");
            byte[] bytes = DownloadFileDocumentDAO.loadFile(pathFileTemplate);
            byte[] bytesAfterAddWaterMark = null;
            // Them anh chu ky vao file template
            if (bytes != null) {
//                PdfReader pdfReader = new PdfReader(bytes);
                StringBuilder userInfo = new StringBuilder();
                userInfo.append(userGroup.getCardId());
                userInfo.append("_");
                userInfo.append(FunctionCommon.removeUnsign(userGroup.getName2()));
                bytesAfterAddWaterMark = dao.writeWaterMarkPdf(
                        bytes, userInfo.toString(), "", imagePath);
                inputStream = new ByteArrayInputStream(bytesAfterAddWaterMark);
            }
            Response.ResponseBuilder responseBuilder = Response.ok(inputStream);
            responseBuilder.header("File-Name", fileName);
            if (bytesAfterAddWaterMark != null && bytesAfterAddWaterMark.length > 0) {
                responseBuilder.header("File-Size", bytesAfterAddWaterMark.length);
            } else {
                responseBuilder.header("File-Size", 0);
            }
            response = responseBuilder.build();
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(userGroup.getKpiLog());
        } catch (JSONException | IOException ex) {
            LOGGER.error("reviewImage (Xem anh chu ky) - Exception!", ex);
        }
        return response;
    }

    /**
     * <b>Lay anh chu ky </b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getImageSignByCardId(HttpServletRequest request, String data,
            String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String response = null;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        if (!userGroup.getCheckSessionOk()) {
            return response;
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
                "empCode", "empId", "actionDate"};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);

            // duong dan file anh chen vao pdf
            String empCode = listValue.get(0);
            Long empId = null;
            String actionDate = listValue.get(2);
            try {
                empId = Long.parseLong(listValue.get(1));
            } catch (Exception e) {
                LOGGER.error(e);
            }
            ImageSignDao dao = new ImageSignDao();
            EntityStaffImageSign imageEmp = dao.getImageSignByCardId(empCode, empId, actionDate);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, imageEmp, aesKey);
        } catch (JSONException ex) {
            LOGGER.error("getImageSignByCardId - Exception:", ex);
        }
        return response;
    }
}
