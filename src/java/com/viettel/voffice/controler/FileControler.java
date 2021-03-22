/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.core.header.FormDataContentDisposition;
//import com.sun.jersey.core.header.FormDataContentDisposition;
import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.constants.InputStreamWithFileDeletion;
import com.viettel.voffice.database.dao.common.CommonDataBaseDaoVO2;
import com.viettel.voffice.database.entity.EntityFileAttachment;
import com.viettel.voffice.database.entity.EntityImageOrg;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.StringConstants;
import com.viettel.voffice.database.dao.document.DocumentDAO;
import com.viettel.voffice.database.dao.document.TextSearchDAO;
import com.viettel.voffice.database.dao.file.AttachDAO;
import com.viettel.voffice.database.dao.file.DownloadFileCommentDAO;
import com.viettel.voffice.security.DES;
import com.viettel.voffice.utils.LogUtils;

import java.io.FileOutputStream;

import com.viettel.voffice.database.dao.file.DownloadFileDocumentDAO;
import com.viettel.voffice.database.dao.file.FilesAttachmentDAO;
import com.viettel.voffice.database.dao.file.ImageDAO;
import com.viettel.voffice.database.dao.logAction.ActionLogMobileDAO;
import com.viettel.voffice.database.dao.staff.ImageOrgDAO;
import com.viettel.voffice.database.dao.staff.StaffImageSignDAO;
import com.viettel.voffice.database.entity.EntityAttachment;
import com.viettel.voffice.database.entity.EntityDocument;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityMarkInfo;
import com.viettel.voffice.database.entity.EntityStaff;
import com.viettel.voffice.database.entity.EntityStaffImageSign;
import com.viettel.voffice.database.entity.User.EntityUser;
import com.viettel.voffice.database.entity.User.Vof2_EntityUser;
import com.viettel.voffice.database.entity.file.EntityImage;
import com.viettel.voffice.database.entity.meeting.EntityMeetingMinutes;
import com.viettel.voffice.database.entity.task.EntityEmpRating;
import com.viettel.voffice.database.entity.text.EntityText;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.EditorUtils;
import com.viettel.voffice.utils.PDFSignatureUtil;

import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.xml.security.utils.Base64;
import org.json.JSONArray;
import org.json.JSONException;

/**
 *
 * @author datnv5
 */
@SuppressWarnings({"unchecked", "deprecation"})
public class FileControler {

    public static final String ROOT_ACTION = "/Files";
    // Log file
    private static final Logger LOGGER = Logger.getLogger(FileControler.class);
    private static final String NOT_ACCESS = "notAccess";
    private static final String PATH_FILE_ERROR = "0"; //loi sai duong dan file
    private static final String PERMISS_DOWNLOAD_FILE_ERROR = "1"; //loi khong co quyen download file
    private static final String NOT_EXIST_FILE_ERROR = "2"; //loi khong ton tai file
    private static final String DECRYPT_FILE_ERROR = "3"; //loi khong giai ma duoc file
    private static final String NULL_ATTACH_ID_ERROR = "4"; //loi khong ton tai attachId
    private static final String NOT_EXIST_DATA_FILE_ERROR = "5"; //loi khong ton tai du lieu file
    private static final String DECRYPT_FILE_NAME_ERROR = "6"; //loi ma hoa ten file
    // File dinh kem comment ky
    private static final int DOC_TEXT_FILE_COMMENT_SIGN = 8;
    // Ten cua class bao gom ca ten package
    private static final String CLASS_NAME = FileControler.class.getName();

    public Response getFileFromAttachId(String strData, HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        Response strResult = null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strData);
                LogUtils.logFunctionalStart(log);
                //lay thong tin file trong DB
                StringBuilder queryString = new StringBuilder();
                List<Object> params = new ArrayList<>();
                queryString.append(" select FILE_ATTACHMENT_ID fileAttachmentId,");
                queryString.append(" FILE_NAME fileName, FILE_PATH filePath,storage as storage ");
                queryString.append(" from FILE_ATTACHMENT WHERE DEL_FLAG = 0 AND FILE_ATTACHMENT_ID = ? ");
                //params.add(idAttach);
                params.add(strData);

                CommonDataBaseDaoVO2 cmd = new CommonDataBaseDaoVO2();
                String strSourdFile = FunctionCommon.getConfigFile("urlfileService");
                List<EntityFileAttachment> listItem = (List<EntityFileAttachment>) cmd.excuteSqlGetListObjOnCondition(queryString, params, null, null, EntityFileAttachment.class);
                if (listItem != null && listItem.size() > 0) {
                    String result;
                    EntityFileAttachment item = listItem.get(0);

                    if (item.getStorage() != null && !"".equals(item.getStorage().trim())) {
                        result = FunctionCommon.getPropertiesValue(item.getStorage()) + item.getFilePath();
                    } else {
                        result = strSourdFile + item.getFilePath();
                    }

//                    System.out.println("duong dan file :" + result);
                    File fileSourd = new File(result);
                    if (result != null && !result.isEmpty() && !"".equals(result) && fileSourd.exists()) {
                        //co duong dẫn file, idfile, thông tin file
                        String strAesKey = dataSessionGR.getStrAesKey();
                        //String fileName = "/u02/app/voffice/tomcat8591_mobile_service/webapps/myfile" + formatter.format(new Date()) + ".pdf";

                        String strSourdFileTem = req.getRealPath("/");//props.getProperty("urlfileTemp");
//                        System.out.println("duong dan strSourdFileTem :" + strSourdFileTem);
                        String fileName = strSourdFileTem + formatter.format(new Date()) + item.getFileName();
                        Boolean check = null;
                        if (strAesKey != null && strAesKey.trim().length() > 0) {
                            check = SecurityControler.encodeDataByAesFile(strAesKey, result, fileName);
                        }

                        if (check != null && check) {
                            File file = new File(fileName);

                            InputStream inputStream = new InputStreamWithFileDeletion(file);
                            Response.ResponseBuilder response = Response.ok(inputStream);
                            String headerKey = "Content-Disposition";
                            String headerValue = String.format("attachment; filename=\"%s\"", fileName);
                            response.header(headerKey, headerValue);
                            String hearderContenLength = "filesize";
                            response.header(hearderContenLength, String.valueOf(file.length()));
                            strResult = response.build();
                            // Ghi log ket thuc chuc nang
                            LogUtils.logFunctionalEnd(log);
                            return strResult;
                        } else {
                            return strResult;
                        }
                    } else {
                        return strResult;
                    }
                } else {
                    return strResult;
                }
            } catch (IOException ex) {
                LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_ERROR, ErrorCode.INPUT_INVALID, ex.getMessage(), strData, "");
                LOGGER.error(ex.getMessage(), ex);
                return strResult;
            }
        } else {
            return strResult;
        }
    }

    /**
     * <b>Lay dung luong file va so trang</b><br>
     *
     * @param strData
     * @param req
     * @return
     */
    public String getFileSizeAndPage(String strData, HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        EntityFileAttachment resultData = new EntityFileAttachment();
        String strResult;
        if (dataSessionGR.getCheckSessionOk()) {
            String strAesKeyDecode = dataSessionGR.getStrAesKey();
            String decryptedData = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
            // Lay ma nhan vien
            String cardId = dataSessionGR.getCardId();
            log.setUserName(cardId);
            // Ghi log bat dau chuc nang
            log.setParamList(decryptedData);
            LogUtils.logFunctionalStart(log);

            HashMap<String, Object> hmParams = new HashMap<>();
            hmParams.put("attachId", String.class);
            HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
            Object attachId = (valueParams != null) ? valueParams.get("attachId") : null;

            if (attachId != null) {
                //lay thong tin file trong DB
                StringBuilder queryString = new StringBuilder();
                List<Object> params = new ArrayList<Object>();
                queryString.append(" select FILE_ATTACHMENT_ID fileAttachmentId,");
                queryString.append("FILE_NAME fileName, FILE_PATH filePath,storage storage");
                queryString.append(" from FILE_ATTACHMENT WHERE DEL_FLAG = 0 AND FILE_ATTACHMENT_ID = ? ");
                params.add(attachId);

                CommonDataBaseDaoVO2 cmd = new CommonDataBaseDaoVO2();
                String strSourdFile = FunctionCommon.getPropertiesValue("urlfileService");
                List<EntityFileAttachment> listItem = (List<EntityFileAttachment>) cmd.excuteSqlGetListObjOnCondition(queryString, params, null, null, EntityFileAttachment.class);
                if (listItem != null && listItem.size() > 0) {
                    EntityFileAttachment item = listItem.get(0);
                    String result;
                    if (item.getStorage() != null && !"".equals(item.getStorage().trim())) {
                        result = FunctionCommon.getPropertiesValue(item.getStorage()) + item.getFilePath();
                    } else {
                        result = strSourdFile + item.getFilePath();
                    }

                    File fileSourd = new File(result);
                    if (result != null && !result.isEmpty() && !"".equals(result) && fileSourd.exists()) {
                        //co duong dẫn file, idfile, thông tin file
                        String strAesKey = dataSessionGR.getStrAesKey();
                        String strSourdFileTem = req.getRealPath("/");

                        String fileName = strSourdFileTem + formatter.format(new Date()) + item.getFileName();
                        Boolean check = null;
                        if (strAesKey != null && strAesKey.trim().length() > 0) {
                            check = SecurityControler.encodeDataByAesFile(strAesKey, result, fileName);
                        }
                        if (check != null && check) {
                            File file = new File(fileName);
                            if (file.exists()) {
                                long filesize = file.length();
                                resultData.setFileSize(String.valueOf(filesize));
                                file.delete();
                            } else {
                                resultData.setFileSize("0");
                            }
                        } else {
                            resultData.setFileSize("0");
                        }
                    } else {
                        resultData.setFileSize("0");
                    }
                } else {
                    resultData.setFileSize("0");
                }
            } else {
                resultData.setFileSize("0");
            }

            strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, resultData, strAesKeyDecode);
        } // Session timeout
        else {
            LOGGER.error("getFileSizeAndPage (Lay dung luong file va so trang) - Session timeout!");
            resultData.setFileSize("0");
            strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, resultData, null);
        }
        return strResult;
    }

    /**
     * @param req
     * @param data
     * @param isSecurity
     * @return
     */
    public Response downloadContentFileCommentSign(HttpServletRequest req,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        Response response = null;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(req);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("downloadContentFileCommentSign - Session timeout!");
            return response;
        }
        // Lay id user he thong 2
        Long userIdVof2 = 0L;
        if (userGroup.getVof2_ItemEntityUser() != null
                && userGroup.getVof2_ItemEntityUser().getUserId() != null) {
            userIdVof2 = userGroup.getVof2_ItemEntityUser().getUserId();
        }
        if (userIdVof2 == null) {
            return response;
        }
        
        Long userIdVof1 = userGroup.getUserId1();
        // Giai ma du lieu neu ma hoa
        List<Long> lstGroupVof1 = userGroup.getItemEntityUser() == null
                ? new ArrayList<Long>() : userGroup.getItemEntityUser().getLstGroupAll();
        List<Long> lstGroupVof2 = userGroup.getVof2_ItemEntityUser() == null
                ? new ArrayList<Long>() : userGroup.getVof2_ItemEntityUser().getLstVhrOrgAll();
        
        String aesKey;
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
                ConstantsFieldParams.TYPE,
                ConstantsFieldParams.FILE_NAME,
                ConstantsFieldParams.FILE_PATH,
                ConstantsFieldParams.DOCUMENT_ID,
                ConstantsFieldParams.IS_ORIGINAL,
                ConstantsFieldParams.TOKEN_READ_FILE,
                ConstantsFieldParams.STORAGE,
                "allowShowFileFormFinancial"
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Ten file
            String fileName = listValue.get(1);
            // Duong dan file
            String filePath = listValue.get(2);
            // Id van ban
            String documentId = listValue.get(3);
            // token
            String stToken = listValue.get(5);
            // Duong dan file
            String storage = listValue.get(6);
            //Tunghd set quyen de xem file van ban man ho so tai chinh
            boolean allowShowFileFormFinancial = false;
            if(!CommonUtils.isEmpty(listValue.get(7))){
                allowShowFileFormFinancial = Boolean.valueOf(listValue.get(7));
            }

            if (CommonUtils.isEmpty(documentId)) {
                Response.ResponseBuilder responseBuilder = Response.ok();
                responseBuilder.header("filePermission", "0");
                response = responseBuilder.build(); 
                return response;
            }
            DocumentDAO documentDao = new DocumentDAO();
            EntityDocument permission = documentDao
                    .getDocumentById(userIdVof1, userIdVof2,
                            lstGroupVof1, lstGroupVof2, Long.valueOf(documentId), stToken, allowShowFileFormFinancial);
            boolean permissionRead = false;
            if (permission != null && !CommonUtils.isEmpty(permission.getTitle())) {
                permissionRead = true;
            } else {
                TextSearchDAO textDao = new TextSearchDAO();
                List<EntityText> perMisText = textDao.getTextById(userIdVof1,userIdVof2, Long.valueOf(documentId), lstGroupVof1,
                        lstGroupVof2, userGroup.getVof2_ItemEntityUser().getListSecretaryVhrOrg());
                if (!CommonUtils.isEmpty(perMisText)) {
                    permissionRead = true;
                }
            }
            if (!permissionRead) {
                Response.ResponseBuilder responseBuilder = Response.ok();
                responseBuilder.header("filePermission", "0");
                response = responseBuilder.build();
                return response;
            }
            
            if (!FileUtils.checkSafeFileName(filePath)) {
                LOGGER.error("downloadContentFileCommentSign - Loi duong dan file"
                        + " - username: " + cardId + "\ndata: " + data);
                return response;
            }
//            String sessionId = FunctionCommon.getStrSessionIdByHttpRQ(req);
            // Vi file kien nghi, de xuat bi ma hoa khi upload
            // nen phai giai ma
            InputStream inputStream;
            File file = FileUtils.getFileByType(filePath, storage);

            if (file == null || !file.exists()) {
                LOGGER.error("downloadContentFileCommentSign - file null hoac"
                        + " file khong ton tai - username: " + cardId + "\ndata: " + data);
                return response;
            }
            try {
                String tmpFilePath = req.getRealPath("/") + (new Date()).getTime() + fileName;
                File decryptFile = new File(tmpFilePath);
                FileInputStream fis = new FileInputStream(file);
                FileOutputStream fos = new FileOutputStream(decryptFile);
                DES.decrypt(fis, fos);
                inputStream = new InputStreamWithFileDeletion(decryptFile);
            } catch (Throwable ex) {
                LOGGER.error("downloadContentFileCommentSign - Loi giai ma file"
                        + " - Throwable - username: " + cardId + "\ndata: " + data, ex);
                return response;
            }
            Response.ResponseBuilder responseBuilder = Response.ok(inputStream);
            responseBuilder.header("File-Name", fileName);
            if (file != null && file.length() > 0) {
                responseBuilder.header("File-Size", file.length());
            } else {
                responseBuilder.header("File-Size", 0);
            }
            response = responseBuilder.build();
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error("downloadContentFileCommentSign - Exception - username: "
                    + cardId + "\ndata: " + data, ex);
        }
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return response;
    }

    /**
     * <b>Download noi dung file</b>
     *
     * @param req
     * @param data
     * @param isSecurity
     * @return
     */
    public Response downloadContentFile(HttpServletRequest req, String data,
            String isSecurity) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        Response response = null;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(req);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("downloadContentFile (Download noi dung file) - Session timeout!");
            return response;
        }
        // Lay id user he thong 1&2
        if (!userGroup.checkUserId2()) {
            return response;
        }
        Long userIdVof1 = userGroup.getUserId1();
        Long userIdVof2 = userGroup.getUserId2();

        //Lay ten user doc van ban
        String userName = EditorUtils.unSignMore(userGroup.getCardId()) + "_"
                + EditorUtils.unSignMore(userGroup.getName2());
        // Giai ma du lieu neu ma hoa
        List<Long> lstGroupVof1 = userGroup.getItemEntityUser() == null
                ? new ArrayList<Long>() : userGroup.getItemEntityUser().getLstGroupAll();
        List<Long> lstGroupVof2 = userGroup.getVof2_ItemEntityUser() == null
                ? new ArrayList<Long>() : userGroup.getVof2_ItemEntityUser().getLstVhrOrgAll();
        String aesKey;
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
                ConstantsFieldParams.TYPE,
                ConstantsFieldParams.FILE_ID,
                ConstantsFieldParams.FILE_NAME,
                ConstantsFieldParams.FILE_PATH,
                ConstantsFieldParams.DOCUMENT_ID,
                ConstantsFieldParams.IS_ORIGINAL,
                ConstantsFieldParams.TOKEN_READ_FILE,
                ConstantsFieldParams.TASK_STORAGE,
                "deviceName",
                "filesAttachmentCommentId",
                "fileCommentDraffId",
                // Datdc add them path dau va storage dau
                "listMarkInfo",
                //tunghd add
                "isFilePhuLuc",
				"allowShowFileFormFinancial"
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);

            // Loai
            int type = -1;
            String strType = listValue.get(0);
            if (!CommonUtils.isEmpty(strType)) {
                type = Integer.parseInt(strType);
            }
            // Id file
            Long fileId = null;
            String strFileId = listValue.get(1);
            if (!CommonUtils.isEmpty(strFileId)) {
                fileId = Long.parseLong(strFileId);
            }
            // Ten file
            String fileName = listValue.get(2);
            // Duong dan file
            String filePath = listValue.get(3);
            // Id van ban
            Long documentId = 0L;
            String strDocumentId = listValue.get(4);
            if (!CommonUtils.isEmpty(strDocumentId)) {
                documentId = Long.parseLong(strDocumentId);
            }
            // User dang nhap la van thu hay ko
            String isSecretary = "0";
            if (userGroup.getVof2_ItemEntityUser().getIsSecrectaryVo2() != null
                    && userGroup.getVof2_ItemEntityUser().getIsSecrectaryVo2().equals(true)) {
                isSecretary = "1";
            }
            // In van ban danh cho doi tuong van thu
            String isOriginal = listValue.get(5);
            // Duong dan file
            String token = listValue.get(6);
            String storage = listValue.get(7);
            // Id file comment khi chuyen
            Long fileCommentId = null;
            String strFileCommentId = listValue.get(9);
            if (!CommonUtils.isEmpty(strFileCommentId)) {
                fileCommentId = Long.parseLong(strFileCommentId);
            }
            // Id file comment
            Long fileCommentDraffId = null;
            String strFileCommentDraffId = listValue.get(10);
            if (!CommonUtils.isEmpty(strFileCommentDraffId)) {
                fileCommentDraffId = Long.parseLong(strFileCommentDraffId);
            }
            // Datdc start add thong tin duong dan file anh dau
            String strRequest = listValue.get(11);
            List<EntityMarkInfo> listMarkInfo = null;
            if (!CommonUtils.isEmpty(strRequest)) {
                Gson gson =  new GsonBuilder().setDateFormat("dd/MM/yyyy hh:mm:ss").create();
                Type listMark = new TypeToken<ArrayList<EntityMarkInfo>>(){}.getType();
                listMarkInfo = gson.fromJson(strRequest, listMark);
            }
            // Datdc end add thong tin duong dan file anh dau 
            //Tunghd
            //String accessFile = listValue.get(12);
            boolean isAccessFile = false;
            if(!CommonUtils.isEmpty(listValue.get(12))){
                isAccessFile = Boolean.parseBoolean(listValue.get(12));
            }
            //Tunghd set quyen de xem file van ban man ho so tai chinh
            boolean allowShowFileFormFinancial = false;
            if(!CommonUtils.isEmpty(listValue.get(13))){
                allowShowFileFormFinancial = Boolean.valueOf(listValue.get(13));
            }
            if (!FileUtils.checkSafeFileName(filePath)) {
                LOGGER.error("downloadContentFile (Download noi dung file) -"
                        + " Loi duong dan file - username: " + cardId + "\ndata: " + data);
                return response;
            }
            File file;
            String tmpFilePath = null;
            //datnv5: kiem tra va chan doc file ky khi tong giam doc vua ky file
            boolean isNotReadDoc = getDoNotReadTextDuringSign(userIdVof2, documentId,filePath);
            if(isNotReadDoc){
                //khong duoc doc van ban vi dang trong thoi gian xu ly
                Response.ResponseBuilder responseBuilder = Response.ok();
                responseBuilder.header("filePermission", "-158110");
                response = responseBuilder.build();
                return response;
            }
            
            // Tao file giai ma, dinh thong tin trang phu luc ky, anh chu ky, watermark
            // cho file van ban, cong van
            if (type == FileUtils.DOC_TEXT_FILE_TYPE) {
                if (fileCommentId != null || fileCommentDraffId != null) {
                    //Doc file khong co noi dung comment chi dao
                    DownloadFileCommentDAO downFile = new DownloadFileCommentDAO();
                    tmpFilePath = downFile.downFileCommentDocument(req, documentId,
                            userName, userIdVof1, userIdVof2, lstGroupVof1,
                            lstGroupVof2, isSecretary, isOriginal,
                            filePath, token, storage);
                } else {
                    //Doc file khong co noi dung comment chi dao
                    DownloadFileDocumentDAO downFile = new DownloadFileDocumentDAO();
					//Tunghd add them quyen doc van ban tu man ho so tai chinh
                    if(allowShowFileFormFinancial){
                        downFile.setAllowShowFileFormFinancial(true);
                    }
                    // TungHD check neu la file phu luc end
                    // Datdc add them listMarkInfo
                    tmpFilePath = downFile.downFileDocument(req, documentId,
                            userName, userIdVof1, userIdVof2, lstGroupVof1,
                            lstGroupVof2, isSecretary, isOriginal, filePath,
                            token, userGroup.getVof2_ItemEntityUser().getListSecretaryVhrOrg(), listMarkInfo);
                }
                if (tmpFilePath != null && tmpFilePath.equals(NOT_ACCESS)
                        && CommonUtils.isEmpty(token)) {
                    // Loi khong co quyen doc file
                    LOGGER.error("downloadContentFile (Download noi dung file) -"
                            + " Loi khong co quyen download - username: " + cardId
                            + "\ndata: " + data);
                    Response.ResponseBuilder responseBuilder = Response.ok();
                    responseBuilder.header("filePermission", "0");
                    response = responseBuilder.build();
                    return response;
                }
                file = new File(tmpFilePath);
            } else if (type == FileUtils.TASK_ASSIGNMENT_FILE_TYPE
                    || type == FileUtils.TASK_ASSESSMENT_FILE_TYPE) {
                // Loai file giao cong viec ca nhan dau thang
                if (storage != null && !"".equals(storage)) {
                    String exportFolder = FunctionCommon.getPropertiesValue(storage);
                    //Duong dan tuyet doi cua phieu giao viec/Phieu danh gia
                    String originalFile = exportFolder + File.separator + filePath;
                    originalFile = FileUtils.getSafePath(originalFile);
                    file = new File(originalFile);
                } else {
                    file = FileUtils.getFileByType(type, filePath);
                }
            } else if (type == FileUtils.DOC_TEXT_FILE_KI) {
                //File bao cao danh gia ki
                //Cuongnv::4/3/2017
                //Xu dung doc file kieu storage tuy bien              
                file = FileUtils.getFileByType(type, filePath, storage);
            } else if (type == FileUtils.BRIEF_FILE_TYPE) { // pm1_os20 add download file brief
                //Doc file khong co noi dung comment chi dao
                // Datdc add them listMarkInfo
                DownloadFileDocumentDAO downFile = new DownloadFileDocumentDAO();
                tmpFilePath = downFile.downFileBriefDocument(req, documentId,
                        userName, userIdVof1, userIdVof2, lstGroupVof1,
                        lstGroupVof2, isSecretary, isOriginal,
                        filePath, token, listMarkInfo);

                file = new File(tmpFilePath);
                //file = FileUtils.getFileByType(8, filePath, storage);
            } else {
                if (storage != null && !"".equals(storage)) {
                    file = FileUtils.getFileByType(type, filePath, storage);
                } else {
                    file = FileUtils.getFileByType(type, filePath);
                }

            }
            // File khong ton tai
            if (file == null || !file.exists()) {
                LOGGER.error("downloadContentFile (Download noi dung file) -"
                        + " File null hoac file khong ton tai - username: " + cardId
                        + "\ndata: " + data + "\ntmpFilePath: " + tmpFilePath);
                return response;
            }
            InputStream inputStream;
            switch (type) {
                // File van ban, cong van
                case FileUtils.DOC_TEXT_FILE_TYPE:
                    inputStream = new InputStreamWithFileDeletion(file);
                    break;
                // File giao cong viec ca nhan dau thang                    
                case FileUtils.TASK_ASSIGNMENT_FILE_TYPE:
                // File danh gia cong viec ca nhan cuoi thang
                case FileUtils.TASK_ASSESSMENT_FILE_TYPE:
                    // Neu fileId # null -> File da duoc ky
                    // -> Thuc hien chen trang phu luc ky va anh chu ky
                    if (fileId != null) {
                        // Tao duong dan luu file tam
                        tmpFilePath = req.getRealPath("/") + "/share/tmp";
                        tmpFilePath += File.separator + userIdVof2 + "_"
                                + (new Date()).getTime() + "_tmp.pdf";
                        // 201812-Pitagon: ensure path is safe
                        tmpFilePath = FileUtils.getSafePath(tmpFilePath);
                        // End 201812-Pitagon: ensure path is safe
                        // Them trang phu luc, anh chu ky va watermark
                        if (FileUtils.insertInfoIntoSignedTaskFile(userName,
                                fileId, file, tmpFilePath)) {
                            inputStream = new InputStreamWithFileDeletion(new File(tmpFilePath));
                        } // Loi them thong tin
                        else {
                            return response;
                        }
                    } // Khong chen thong tin vao file
                    else {
                        inputStream = new FileInputStream(file);
                    }
                    break;
                // Vi file kien nghi, de xuat bi ma hoa khi upload nen phai giai ma
                // File dinh kem cong viec ca nhan
                case FileUtils.TASK_REQUEST_FILE_TYPE:
                // File dinh kem comment ky
                case FileUtils.DOC_TEXT_FILE_COMMENT_SIGN:
                    try {
                        tmpFilePath = req.getRealPath("/") + (new Date()).getTime() + fileName;
                        File decryptFile = new File(tmpFilePath);
                        FileInputStream fis = new FileInputStream(file);
                        FileOutputStream fos = new FileOutputStream(decryptFile);
                        DES.decrypt(fis, fos);
                        // Neu la file pdf va co chu ky so thi hien thi trang phu luc ky
//                        if (fileName.endsWith(".pdf") || fileName.endsWith(".PDF")) {
//                            // Lay mang byte cua file
//                            byte[] fileBytes = DownloadFileDocumentDAO.loadFile(tmpFilePath);
//                            // Them trang phu luc nguoi ky
//                            PDFSignatureUtil pdfSignatureUtil = new PDFSignatureUtil();
//                            fileBytes = pdfSignatureUtil.addSigningPage(fileBytes, tmpFilePath, null);
//                            fos = new FileOutputStream(decryptFile);
//                            fos.write(fileBytes);
//                            fos.close();
//                        }
                        inputStream = new InputStreamWithFileDeletion(decryptFile);
                    } catch (Throwable ex) {
                        LOGGER.error("downloadContentFile (Download noi dung file)"
                                + " - Loi giai ma file - Throwable - username: "
                                + cardId + "\ndata: " + data, ex);
                        return response;
                    }
                    break;
                case FileUtils.DOC_TEXT_FILE_MISSION:
                    inputStream = new FileInputStream(file);
                    break;
                case FileUtils.DOC_TEXT_FILE_MEETING_MINUTES:
                    inputStream = new FileInputStream(file);
                    break;
                case FileUtils.DOC_TEXT_FILE_KI:
                    if (fileId == null) {
                        inputStream = new FileInputStream(file);
                    } else {
                        // Tao duong dan luu file tam
                        tmpFilePath = req.getRealPath("/") + "/share/tmp";
                        tmpFilePath += File.separator + userIdVof2 + "_"
                                + (new Date()).getTime() + "_tmp.pdf";
                        // 201812-Pitagon: ensure path is safe
                        tmpFilePath = FileUtils.getSafePath(tmpFilePath);
                        // End 201812-Pitagon: ensure path is safe
                        // Them trang phu luc, anh chu ky va watermark
                        if (FileUtils.insertInfoIntoSignedKI(userName,
                                fileId, file, tmpFilePath)) {
                            inputStream = new InputStreamWithFileDeletion(new File(tmpFilePath));
                        } // Loi them thong tin
                        else {
                            return response;
                        }
                    }
                    break;
                default:
                    inputStream = new FileInputStream(file);
                    break;
            }
            Response.ResponseBuilder responseBuilder = Response.ok(inputStream);
            responseBuilder.header("File-Name", fileName);
            long fileSize = file.length();
            if (fileSize > 0) {
                responseBuilder.header("File-Size", fileSize);
            } else {
                responseBuilder.header("File-Size", 0);
            }
            response = responseBuilder.build();

            // Ghi log database doc file voi client la web
            // Kiem tra la ung dung web goi thi khong co tham so div
//            if (CommonUtils.isEmpty(listValue.get(8))) {
            ActionLogMobileDAO actionLogMobileDAO = new ActionLogMobileDAO();
            String description = "Log doc file tu Service: fileId: "
                    + fileId + "filePath: " + filePath + " fileSize:" + fileSize;

            actionLogMobileDAO.insert(userIdVof2, cardId, log.getStartTime(), new Date(),
                    "Files.DownloadContentFile", description,
                    FunctionCommon.IPPORTSERVICE + ", Storage: " + storage, null, null, "WEB", null);

//            }
        } catch (JSONException | NumberFormatException | FileNotFoundException ex) {
            LOGGER.error("downloadContentFile (Download noi dung file) - Exception"
                    + " - username: " + cardId + "\ndata: " + data, ex);
        }
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return response;
    }

    /**
     * <b>Download noi dung file van ban ky</b><br>
     *
     * @param req
     * @param data
     * @param isSecurity
     * @return
     */
    public String getInfoFile(HttpServletRequest req, String data,
            String isSecurity) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        EntityFileAttachment resultData = new EntityFileAttachment();
        String strResult = null;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(req);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getInfoFile - Session timeout!");
            return strResult;
        }
        // Lay id user he thong 2
        Long userIdVof2 = userGroup.getVof2_ItemEntityUser().getUserId();
        if (userIdVof2 == null) {
            return strResult;
        }
        // Giai ma du lieu neu ma hoa
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
                ConstantsFieldParams.TYPE,
                ConstantsFieldParams.FILE_NAME,
                ConstantsFieldParams.FILE_PATH,
                ConstantsFieldParams.DOCUMENT_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Loai
            int type = Integer.parseInt(listValue.get(0));

            // Duong dan file
            String filePath = listValue.get(2);
            //Id vanban
            Long documentId;
            if (listValue.get(3) != null && !"".equals(listValue.get(3))) {
                documentId = Long.parseLong(listValue.get(3).trim());
            } else {
                return strResult;
            }

            if (!FileUtils.checkSafeFileName(filePath)) {
                LOGGER.error("getInfoFile - Loi duong dan file - username: "
                        + cardId + "\ndata: " + data);
                return strResult;
            }

            // Vi file kien nghi, de xuat bi ma hoa khi upload
            // nen phai giai ma
            String[] arrInfoFile = new String[2];
            if (type == FileUtils.DOC_TEXT_FILE_TYPE) {
                //Neu doc file van ban ky dien tu hoac file van ban nhap
                DownloadFileDocumentDAO downFile = new DownloadFileDocumentDAO();
                arrInfoFile = downFile.getInfoFile(req, documentId,
                        userIdVof2, filePath);

            } else if (type == FileUtils.TASK_REQUEST_FILE_TYPE) {
            } else {
            }
            if (arrInfoFile != null && arrInfoFile.length > 1) {
                resultData.setFileSize(arrInfoFile[0]);
                resultData.setFilePage(arrInfoFile[1]);
                if (arrInfoFile.length > 2) {
                    resultData.setFilePath(arrInfoFile[2]);
                }
            }
            strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                    resultData, aesKey);
        } catch (NumberFormatException | JSONException ex) {
            LOGGER.error("getInfoFile - Exception - username: " + cardId + "\ndata: " + data, ex);
            strResult = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    resultData, aesKey);
        }
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return strResult;
    }

    public static String generateToken(String sessionId, Long userId, Long documentId) {
        String result = null;

        if ((sessionId != null) && (userId != null) && (documentId != null)) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                String input = sessionId + "-" + String.valueOf(userId) + "-" + String.valueOf(documentId);
                byte[] hash = digest.digest(input.getBytes());
                result = Base64.encode(hash);
            } catch (NoSuchAlgorithmException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
        return result;
    }

    /**
     * <b>Upload file tam</b><br>
     *
     * @author thanght6
     * @since Aug 29, 2016
     * @param request
     * @param data
     * @param is
     * @param fileDetail
     * @return
     */
    public String uploadTmpFile(HttpServletRequest request, String data,
            InputStream is, FormDataContentDisposition fileDetail) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String sessionId;
        // Lay sessionId tu chuoi header "session_id"
        if (!CommonUtils.isEmpty(request.getHeader(StringConstants.STR_SESSIONID))) {
            sessionId = request.getHeader(StringConstants.STR_SESSIONID);
        } // Lay sessionId tu chuoi header "cookie"
        else if (!CommonUtils.isEmpty(request.getHeader(StringConstants.STR_COOKIE))) {
            sessionId = request.getHeader(StringConstants.STR_COOKIE);
            if (sessionId.contains(StringConstants.STR_JSESSIONID)) {
                sessionId = sessionId.replace(StringConstants.STR_JSESSIONID,
                        StringConstants.STR_EMTY);
            }
            if (sessionId.contains("cookie=")) {
                sessionId = sessionId.substring(sessionId.indexOf("cookie=") + 7);
            }
        } else {
            // Lay session add vao header mac dinh
            sessionId = request.getRequestedSessionId();
        }
        EntityUserGroup userGroup = FunctionCommon.checkSessionById(sessionId);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("uploadTmpFile - No session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // User 1
        EntityUser user1 = userGroup.getItemEntityUser();
        // User 2
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        Long userId = user2 != null ? user2.getUserId() : user1.getUserId();
        if (userId == null) {
            userId = 0L;
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
                ConstantsFieldParams.TYPE
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Loai file
            int type = -1;
            String strType = listValue.get(0);
            if (!CommonUtils.isEmpty(strType)) {
                type = Integer.parseInt(strType);
            }
            // Ten file
            String fileName = fileDetail != null ? fileDetail.getFileName() : "";
            EntityFileAttachment attachment = FileUtils.saveTmpFile(userId, type,
                    fileName, is);
            if (attachment == null) {
                LOGGER.error("uploadTmpFile - Loi luu file tam");
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, attachment, null);
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error("uploadTmpFile - Loi du lieu dau vao - data: " + data);
            LOGGER.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        } catch (Exception ex) {
            LOGGER.error("uploadTmpFile - Exception: ", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }
    // Kich thuoc anh goc
    private static final String STAFF_IMAGE_ORIGIN_SIZE = "origin";

    /**
     * <b>Download anh nhan vien</b><br>
     *
     * @param request
     * @param cardId Ma nhan vien
     * @param size Kich thuoc anh
     * @return
     */
    public Response downloadStaffImage(HttpServletRequest request, String cardId,
            String size) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Ghi log bat dau chuc nang
        LogUtils.logFunctionalStart(log);
        Response response = null;
        try {
            String filePath = cardId + FileUtils.JPG_IMAGE_FILE_EXTENSION;
            // Neu khong phai anh goc thi noi them kich thuoc tuong ung
            if (!size.equals(STAFF_IMAGE_ORIGIN_SIZE)) {
                filePath = size + File.separator + filePath;
            }
            File file = FileUtils.getFileByType(FileUtils.STAFF_IMAGE_FILE_TYPE, filePath);
            if (file == null || !file.exists()) {
//                LOGGER.error("downloadStaffImage - file null hoac file khong ton tai"
//                        + " - filePath: " + (file == null ? filePath : file.getPath()));
                return response;
            }
            Response.ResponseBuilder responseBuilder = Response.ok(file, "image/jpeg");
            response = responseBuilder.build();
        } catch (Exception ex) {
            LOGGER.error("downloadStaffImage - Exception:" + ex.getMessage(), ex);
        }
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return response;
    }

    /**
     * <b>Download anh hien thi giao dien ngay le tet</b><br>
     *
     * @param request
     * @param typeImage Loai anh theo phien ban
     * @param typeScreen: Loai man hinh chuc nang the hien anh
     * @return
     */
    public Response downloadImageIconApp(HttpServletRequest request, String imageName) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Ghi log bat dau chuc nang
        LogUtils.logFunctionalStart(log);
        Response response = null;
        try {
            String filenName = imageName.trim() + ".png";
            File file = FileUtils.getFileByType(FileUtils.VIEW_IMAGE_ICON_APP_MOBILE, filenName);
            if (file == null || !file.exists()) {
                LOGGER.error("downloadImageAppIcon - file null hoac file khong ton tai"
                        + " - filePath: " + (file == null ? filenName : file.getPath()));
                return response;
            }
            Response.ResponseBuilder responseBuilder = Response.ok(file, "image/png");
            response = responseBuilder.build();
        } catch (Exception ex) {
            LOGGER.error("downloadImageAppIcon - Exception", ex);
        }
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return response;
    }

    /**
     * <b>Download anh thong ke van ban tu choi ky</b><br>
     *
     * @param request
     * @param Ma nhan vien
     * @param size Kich thuoc anh
     * @return
     */
    public Response downloadImageReport(HttpServletRequest request, String imgName) {
        LOGGER.error("downloadImageReport");
        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Ghi log bat dau chuc nang
        LogUtils.logFunctionalStart(log);
        log.setStartTime(new Date());
        Response response = null;
        try {
            // Neu khong phai anh goc thi noi them kich thuoc tuong ung
            if (!CommonUtils.isEmpty(imgName)) {
                File file = FileUtils.getFileByType(FileUtils.VIEW_IMAGE_REPORT_MOBILE, imgName);
                if (file == null || !file.exists()) {
                    LOGGER.error("downloadImageReport - file null hoac file khong ton tai"
                            + " - filePath: " + (file == null ? imgName : file.getPath()));
                    return response;
                }
                Response.ResponseBuilder responseBuilder = Response.ok(file, "image/jpg");
                response = responseBuilder.build();
            }
        } catch (Exception ex) {
            LOGGER.error("downloadStaffImage - Exception:" + ex.getMessage(), ex);
        }
        // Ghi log ket thuc chuc nang
//        log.setActionName("downloadImageReport");
//        log.setEndTime(new Date());
        LogUtils.logFunctionalEnd(log);
        return response;
    }

    /**
     * cap nhat so trang va kich thuoc file van ban
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String updateFilePageFileSize(HttpServletRequest request,
            String data, String isSecurity) {
        String result;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            //het session
            LOGGER.error("updateFilePageFileSize - No session");
            result = FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        } else {
            Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
            if ((user2 == null || user2.getUserId() == null)) {
                //khong ton tai tren he thong voffice 2
                LOGGER.error("updateFilePageFileSize - user khong ton tai tren he thong 2");
                result = FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
            } else {
                // Giai ma du lieu neu ma hoa
                String aesKey = null;
                if (isSecurity != null && "1".equals(isSecurity)) {
                    // Lay AES Key
                    aesKey = userGroup.getStrAesKey();
                    // Giai ma data client gui len
                    data = SecurityControler.decodeDataByAes(aesKey, data);
                }
                try {
                    JSONObject json = new JSONObject(data);
                    String[] keys = new String[]{"type",
                        "fileId",
                        "path",
                        "filePage",
                        "fileSize"};
                    List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                    //loai tim kiem
                    String strType = listValue.get(0);
                    Long type = null;
                    if (!CommonUtils.isEmpty(strType)) {
                        type = Long.valueOf(strType.trim());
                    }

                    //id file dinh kem
                    String strFileId = listValue.get(1);
                    Long fileId = null;
                    if (!CommonUtils.isEmpty(strFileId)) {
                        fileId = Long.valueOf(strFileId.trim());
                    }

                    //duong dan file
                    String path = listValue.get(2);

                    //so trang
                    String strFilePage = listValue.get(3);
                    Long filePage = null;
                    if (!CommonUtils.isEmpty(strFilePage)) {
                        filePage = Long.valueOf(strFilePage.trim());
                    }

                    //kich thuoc file
                    String strFileSize = listValue.get(4);
                    Long fileSize = null;
                    if (!CommonUtils.isEmpty(strFileSize)) {
                        fileSize = Long.valueOf(strFileSize.trim());
                    }
                    if (type != null && fileId != null && path != null
                            && filePage != null && fileSize != null && fileSize > 1000L
                            && filePage > 0L) {
                        Boolean resultUpdate;
                        if (type.equals(0L)) {
                            //neu la van ban trinh ky
                            AttachDAO attachDAO = new AttachDAO();
                            resultUpdate = attachDAO.updateFilePageSizeAttach(fileId, path, filePage, fileSize);
                        } else {
                            //neu la cong van
                            FilesAttachmentDAO filesAttachmentDAO = new FilesAttachmentDAO();
                            resultUpdate = filesAttachmentDAO.updateFilePageSizeFilesAttach(fileId, path, filePage, fileSize);
                        }
                        if (resultUpdate) {
                            //neu thanh cong
                            result = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, resultUpdate, aesKey);
                        } else {
                            //neu khong thanh cong
                            result = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, resultUpdate, aesKey);
                        }
                    } else {
                        result = FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                    }
                } catch (JSONException | NumberFormatException ex) {
                    //co loi xay ra
                    LOGGER.error("updateFilePageFileSize - co loi xay ra - userId2: "
                            + user2.getUserId() + " - Exception: ", ex);
                    result = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
            }
        }
        return result;
    }

    /**
     * <b>Xem truoc file bien ban hop tu sinh</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public Response previewMeetingMinutes(HttpServletRequest request,
            String data, String isSecurity) {

        Response response = null;
        StringBuilder errorMessage = new StringBuilder("previewMeetingMinutes");
        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            errorMessage.append(" - ").append(userGroup.getEnumErrCode().toString());
            LOGGER.error(errorMessage);
            return response;
        }
        errorMessage.append(" - username: ").append(userGroup.getCardId());
        try {
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[]{
                ConstantsFieldParams.MEETING_MINUTES
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Thong tin bien ban hop
            String strMeetingMinutes = listValue.get(0);
            Gson gson = new Gson();
            EntityMeetingMinutes meetingMinutes = gson.fromJson(strMeetingMinutes,
                    EntityMeetingMinutes.class);
            EntityFileAttachment fileAttachment = FileUtils.exportMeetingMinutesToPdfFile(
                    userGroup, meetingMinutes, true);
            // Sinh file bien ban hop khong thanh cong
            if (fileAttachment == null) {
                errorMessage.append(" - Loi sinh file bien ban hop!");
                LOGGER.error(errorMessage);
                return response;
            }
            File file = new File(fileAttachment.getAttachment());
            // File bien ban hop khong ton tai
            if (!file.exists()) {
                errorMessage.append(" - Loi file bien ban hop khong ton tai - path: ")
                        .append(fileAttachment.getAttachment());
                LOGGER.error(errorMessage);
                return response;
            }
            InputStream inputStream = new InputStreamWithFileDeletion(file);
            Response.ResponseBuilder responseBuilder = Response.ok(inputStream);
            response = responseBuilder.build();
            return response;
        } catch (Exception ex) {
            errorMessage.append(" - Exception!");
            LOGGER.error(errorMessage, ex);
            return response;
        }
    }

    /**
     * <b>Xem truoc file ki</b><br>
     *
     * @author pm1_os20
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @since 28/10/2017
     */
    public Object previewEmpRatingReport(HttpServletRequest request,
            String data, String isSecurity) {

        Response response = null;
        StringBuilder errorMessage = new StringBuilder("previewEmpRatingReport");
        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            errorMessage.append(" - ").append(userGroup.getEnumErrCode().toString());
            LOGGER.error(errorMessage);
            return response;
        }
        errorMessage.append(" - username: ").append(userGroup.getCardId());
        try {
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[]{
                ConstantsFieldParams.CONTRACT,
                ConstantsFieldParams.TASK_ORG_ID,
                ConstantsFieldParams.PERIOD,
                ConstantsFieldParams.TEXT_TITLE,
                ConstantsFieldParams.TEXT_LST_STAFF,
                ConstantsFieldParams.KI,
                ConstantsFieldParams.TYPE_TASK,
                ConstantsFieldParams.LIST_KI,
                ConstantsFieldParams.FLAG
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Thong tin ki
            //String contractType = listValue.get(0);
            String strOrgId = listValue.get(1);
            String period = listValue.get(2);
            String title = listValue.get(3);
            String strStaff = listValue.get(4);
            String strKi = listValue.get(5);
            String strTypeTask = listValue.get(6);
            String strListKI = listValue.get(7);
            String flag = listValue.get(8);
            Long orgId = null;
            if (strOrgId != null) {
                orgId = Long.valueOf(strOrgId);
            }

            List<EntityStaff> listStaff = new ArrayList<>();
            Gson gson = new Gson();

            if (!CommonUtils.isEmpty(strStaff)) {
                JSONArray arrJsonStaff = new JSONArray(strStaff);
                if (arrJsonStaff.length() > 0) {
                    for (int i = 0; i < arrJsonStaff.length(); i++) {
                        JSONObject job = arrJsonStaff.getJSONObject(i);
                        EntityStaff staff = gson.fromJson(String.valueOf(job), EntityStaff.class);
                        if (staff != null) {
                            listStaff.add(staff);
                        }
                    }
                }
            }

            List<EntityEmpRating> listEmpRating = new ArrayList<>();
            if (!CommonUtils.isEmpty(strListKI)) {
                JSONArray arrJson = new JSONArray(strListKI);
                if (arrJson.length() > 0) {
                    for (int i = 0; i < arrJson.length(); i++) {
                        JSONObject job = arrJson.getJSONObject(i);
                        EntityEmpRating empRating = gson.fromJson(String.valueOf(job), EntityEmpRating.class);
                        if (empRating != null) {
                            listEmpRating.add(empRating);
                        }
                    }
                }
            }

            List<String> lstKi = new ArrayList<>();
            if (!CommonUtils.isEmpty(strKi)) {
                JSONArray arrJson = new JSONArray(strKi);
                if (arrJson.length() > 0) {
                    for (int i = 0; i < arrJson.length(); i++) {
                        if (arrJson.get(i) != null) {
                            lstKi.add(arrJson.getString(i));
                        }
                    }
                }
            }

            List<String> lstTypeTask = new ArrayList<>();
            if (!CommonUtils.isEmpty(strTypeTask)) {
                JSONArray arrJson = new JSONArray(strTypeTask);
                if (arrJson.length() > 0) {
                    for (int i = 0; i < arrJson.length(); i++) {
                        if (arrJson.get(i) != null) {
                            lstTypeTask.add(arrJson.getString(i));
                        }
                    }
                }
            }

            EntityFileAttachment fileAttachment = FileUtils.exportKiToPdfFile(userGroup, listStaff, title, orgId, period, lstKi, lstTypeTask, listEmpRating, true);
            // Sinh file bien ban hop khong thanh cong
            if (fileAttachment == null) {
                errorMessage.append(" - Loi sinh file bien ban hop!");
                LOGGER.error(errorMessage);
                return response;
            }
            File file = new File(fileAttachment.getAttachment());
            // File bien ban hop khong ton tai
            if (!file.exists()) {
                errorMessage.append(" - Loi file bien ban hop khong ton tai - path: ")
                        .append(fileAttachment.getAttachment());
                LOGGER.error(errorMessage);
                return response;
            }
            if ("".equals(flag)) {
                InputStream inputStream = new InputStreamWithFileDeletion(file);
                Response.ResponseBuilder responseBuilder = Response.ok(inputStream);
                response = responseBuilder.build();
                return response;
            } else {
                return FunctionCommon.responseResult(ErrorCode.SUCCESS, fileAttachment, userGroup);
            }
        } catch (Exception ex) {
            errorMessage.append(" - Exception!");
            LOGGER.error(errorMessage, ex);
            return response;
        }
    }

    /**
     * <b>Download anh chu ky</b><br>
     *
     * @param request
     * @param staffImageSignId id anh chu ky
     * @return
     */
    public Response downloadSignatureImage(HttpServletRequest request, String staffImageSignId) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Ghi log bat dau chuc nang
        LogUtils.logFunctionalStart(log);
        Response response = null;
        try {
            // Kiem tra ID anh chu ky
            if (CommonUtils.isEmpty(staffImageSignId)) {
                LOGGER.error("downloadSignatureImage - Loi du lieu dau vao"
                        + " - staffImageSignId: " + staffImageSignId);
                return response;
            }
            // Lay thong tin anh chu ky
            StaffImageSignDAO staffImageSignDAO = new StaffImageSignDAO();
            EntityStaffImageSign staffImageSign = staffImageSignDAO.getSignatureImageById(
                    Long.parseLong(staffImageSignId));
            if (staffImageSign == null) {
                LOGGER.error("downloadSignatureImage - Loi lay thong tin anh chu ky!");
                return response;
            }
            // Lay doi tuong file
            File file = FileUtils.getFileByType(FileUtils.SIGNATURE_IMAGE_FILE_TYPE,
                    staffImageSign.getPath(), staffImageSign.getStorage());
            if (file == null || !file.exists()) {
                LOGGER.error("downloadSignatureImage - file null hoac file khong ton tai"
                        + " - filePath: " + staffImageSign.getPath());
                return response;
            }
            Response.ResponseBuilder responseBuilder = Response.ok(file, "image/jpeg");
            responseBuilder.header("content-disposition", "filename=\""
                    + staffImageSign.getName() + "\"");
            response = responseBuilder.build();
        } catch (Exception ex) {
            LOGGER.error("downloadStaffImage - Exception:" + ex.getMessage(), ex);
        }
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return response;
    }

    /**
     * <b>Xem truoc file phu luc chat trong van ban</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public Response previewAppendixChat(HttpServletRequest request,
            String data, String isSecurity) {

        Response response = null;
        StringBuilder errorMessage = new StringBuilder("previewAppendixChat");
        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            errorMessage.append(" - ").append(userGroup.getEnumErrCode().toString());
            LOGGER.error(errorMessage);
            return response;
        }
        errorMessage.append(" - username: ").append(userGroup.getCardId());
        try {
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[]{
                ConstantsFieldParams.TEXT
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Thong tin bien ban hop
            String strText = listValue.get(0);
            Gson gson = new Gson();
            EntityText text = gson.fromJson(strText,
                    EntityText.class);
            EntityFileAttachment fileAttachment = FileUtils.exportAppendixChatToPdfFile(
                    userGroup, text);
            // Sinh file bien ban hop khong thanh cong
            if (fileAttachment == null) {
                errorMessage.append(" - Loi sinh file phu luc chat!");
                LOGGER.error(errorMessage);
                return response;
            }
            File file = new File(fileAttachment.getAttachment());
            // File bien ban hop khong ton tai
            if (!file.exists()) {
                errorMessage.append(" - Loi file phu luc chat khong ton tai - path: ")
                        .append(fileAttachment.getAttachment());
                LOGGER.error(errorMessage);
                return response;
            }
            InputStream inputStream = new InputStreamWithFileDeletion(file);
            Response.ResponseBuilder responseBuilder = Response.ok(inputStream);
            responseBuilder.header("file-path", fileAttachment.getFilePath());
            responseBuilder.header("file-name", fileAttachment.getFileName());
            responseBuilder.header("file-length", file.length());
            responseBuilder.header("storage", fileAttachment.getStorage());
            response = responseBuilder.build();
            LogUtils.logFunctionalEnd(userGroup.getKpiLog());
            return response;
        } catch (Exception ex) {
            errorMessage.append(" - Exception!");
            LOGGER.error(errorMessage, ex);
            return response;
        }
    }

    /**
     * <b>Download anh luu trong bang image cua database 2</b>
     *
     * @param request
     * @param imageId id anh
     * @return
     */
    public Response downloadImage(HttpServletRequest request, Long imageId) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Ghi log bat dau chuc nang
        LogUtils.logFunctionalStart(log);
        Response response = null;
        try {
            // Kiem tra ID anh chu ky
            if (imageId == null) {
                LOGGER.error("downloadImage - Loi du lieu dau vao!");
                return response;
            }
            // Lay thong tin anh chu ky
            ImageDAO imageDAO = new ImageDAO();
            EntityImage imageInfo = imageDAO.getImageById(imageId);
            if (imageInfo == null) {
                LOGGER.error("downloadImage - Loi lay thong tin anh - imageId: "
                        + imageId);
                return response;
            }
            // Lay doi tuong file
            File file = FileUtils.getFileByType(imageInfo.getImagePath(), imageInfo.getStorage());
            if (file == null || !file.exists()) {
                LOGGER.error("downloadImage - file null hoac file khong ton tai"
                        + " - imagePath: " + imageInfo.getImagePath());
                return response;
            }
            Response.ResponseBuilder responseBuilder = Response.ok(file, "image/jpeg");
            responseBuilder.header("content-disposition", "filename=\""
                    + imageInfo.getImageName() + "\"");
            response = responseBuilder.build();
        } catch (Exception ex) {
            LOGGER.error("downloadImage - Exception!", ex);
        }
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return response;
    }

    /**
     * <b>Download anh chu ky</b><br>
     *
     * @param request
     * @param cardId id anh chu ky
     * @param signedDate ngay ky
     * @return
     */
    public Response downloadSignatureImageByCardId(HttpServletRequest request,
            String cardId, String signedDate) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String errorDesc = "downloadSignatureImageByCardId - cardId: " + cardId + " - ";
        // Ghi log bat dau chuc nang
        LogUtils.logFunctionalStart(log);
        Response response = null;
        try {
            // Kiem tra ID anh chu ky
            if (CommonUtils.isEmpty(cardId)) {
                LOGGER.error(errorDesc + "Loi du lieu dau vao!");
                return response;
            }
            // Lay thong tin anh chu ky
            StaffImageSignDAO staffImageSignDAO = new StaffImageSignDAO();
            EntityStaffImageSign staffImageSign = staffImageSignDAO.getSignatureImageByCardId(
                    cardId, signedDate);
            if (staffImageSign == null) {
                LOGGER.error(errorDesc + "Loi lay thong tin anh chu ky!");
                return response;
            }
            // Lay doi tuong file
            File file = FileUtils.getFileByType(FileUtils.SIGNATURE_IMAGE_FILE_TYPE,
                    staffImageSign.getPath(), staffImageSign.getStorage());
            if (file == null || !file.exists()) {
                LOGGER.error(errorDesc + "file null hoac file khong ton tai - filePath: "
                        + staffImageSign.getPath());
                return response;
            }
            Response.ResponseBuilder responseBuilder = Response.ok(file, "image/jpeg");
            responseBuilder.header("content-disposition", "filename=\""
                    + staffImageSign.getName() + "\"");
            response = responseBuilder.build();
        } catch (Exception ex) {
            LOGGER.error(errorDesc + "Exception!", ex);
        }
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return response;
    }

    /**
     * Pitagon-2018
     * <b>Download anh chu ky</b><br>
     *
     * @param request
     * @param cardId id anh chu ky
     * @return
     */
    public Response downloadImageOrg(HttpServletRequest request,
            String cardId) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String errorDesc = "downloadImageOrg - cardId: " + cardId + " - ";
        // Ghi log bat dau chuc nang
        LogUtils.logFunctionalStart(log);
        Response response = null;
        try {
            // Kiem tra ID anh chu ky
            if (CommonUtils.isEmpty(cardId)) {
                LOGGER.error(errorDesc + "Loi du lieu dau vao!");
                return response;
            }
            // Lay thong tin condau donv i
            ImageOrgDAO imageOrgDao = new ImageOrgDAO();
            EntityImageOrg imageOrg = new EntityImageOrg();
            imageOrg.setImageOrgId(Long.valueOf(cardId));
            List<EntityImageOrg> imageOrgs = imageOrgDao.findByConditionImageOrg(imageOrg, 0, 0);
            if (imageOrgs == null || imageOrgs.size() < 1) {
                LOGGER.error(errorDesc + "Loi lay thong tin anh con dau don vi!");
                return response;
            }
            imageOrg = imageOrgs.get(0);
            // Lay doi tuong file
            File file = FileUtils.getFileByType(FileUtils.SIGNATURE_IMAGE_FILE_TYPE,
                    imageOrg.getPath(), imageOrg.getStorage());
            if (file == null || !file.exists()) {
                LOGGER.error(errorDesc + "file null hoac file khong ton tai - filePath: "
                        + imageOrg.getPath());
                return response;
            }
            Response.ResponseBuilder responseBuilder = Response.ok(file, "image/jpeg");
            responseBuilder.header("content-disposition", "filename=\""
                    + imageOrg.getName() + "\"");
            response = responseBuilder.build();
        } catch (Exception ex) {
            LOGGER.error(errorDesc + "Exception!", ex);
        }
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return response;
    }

    /**
     * datnv5:check status text in During Time sign of CEO
     *
     * @param documentID
     * @param isDoc
     * @return: true - van ban khong duoc view, false - la van ban duoc view
     */
    private boolean getDoNotReadTextDuringSign(Long userIdVof2, Long documentID, String filePath) {
        TextSearchDAO textDao = new TextSearchDAO();
        boolean isDoc = false;
        if(filePath!= null && filePath.trim().length() > 0){
            DocumentDAO documentDao = new DocumentDAO();
            List<EntityAttachment> lstAttachResult = documentDao
                        .getLstAttachment(documentID);
            if (lstAttachResult != null && lstAttachResult.size() > 0) {
                int sizeLstAttach = lstAttachResult.size();
                for (int i = 0; i < sizeLstAttach; i++) {
                    EntityAttachment attachOb = lstAttachResult.get(i);
                    if (attachOb.getAttachment().equals(filePath.trim())) {
                        isDoc = true;
                        break;
                    }
                }
            }
        }
        Long timeDuringSign = textDao.getTimeDuringSignTextOfCEO(userIdVof2, documentID, isDoc);
        //lay cau hinh mac dinh
        String valueTimeConfig = FunctionCommon.getPropertiesValue("text.signed.intimeprocess");
        double valueConfig = 15;
        if (valueTimeConfig != null && valueTimeConfig.trim().length() > 0) {
            try {
                valueConfig = Double.valueOf(valueTimeConfig);
            } catch (NumberFormatException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        //neu timeDuring nho hon cau hinh thi tra ve true nguoc lai tra ve false
        return timeDuringSign < valueConfig;
    }
}
