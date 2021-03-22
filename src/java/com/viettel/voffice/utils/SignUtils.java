/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.joda.time.convert.Converter;
import org.json.JSONObject;

import vn.viettel.core.sign.utils.Constant;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.viettel.digital.sign.DigitalSign;
import com.viettel.digital.sign.SimSign;
import com.viettel.digital.sign.SoftSign;
import com.viettel.digital.sign.UsbSign;
import com.viettel.digital.sign.utils.DigitalSignUtils;
import com.viettel.digital.sign.utils.SequenceNumber;
import com.viettel.digital.sign.utils.SignerInfo;
import com.viettel.signature.utils.CertUtils;
import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.constants.NumberConstants;
import com.viettel.voffice.controler.CommonControler;
import com.viettel.voffice.controler.signature.SequenceId;
import com.viettel.voffice.controler.signature.SignController;
import com.viettel.voffice.database.dao.document.DocumentSignDAO;
import com.viettel.voffice.database.dao.document.TextSearchDAO;
import com.viettel.voffice.database.dao.file.AttachDAO;
import com.viettel.voffice.database.dao.file.FileAttachmentDAO;
import com.viettel.voffice.database.dao.logAction.ActionLogMobileDAO;
import com.viettel.voffice.database.dao.sign.LogTranstionSignDAO;
import com.viettel.voffice.database.dao.sign.P12CertDAO;
import com.viettel.voffice.database.dao.staff.ImageOrgDAO;
import com.viettel.voffice.database.dao.staff.StaffDAO;
import com.viettel.voffice.database.dao.task.MissionSigningDAO;
import com.viettel.voffice.database.dao.task.PersonTaskDAO;
import com.viettel.voffice.database.dao.task.TaskApprovalDAO;
import com.viettel.voffice.database.dao.task.TaskDAO;
import com.viettel.voffice.database.dao.task.TaskRatingDAO;
import com.viettel.voffice.database.dao.text.TextDAO;
import com.viettel.voffice.database.dao.text.TextPartnerDAO;
import com.viettel.voffice.database.dao.text.TextSignDAO;
import com.viettel.voffice.database.entity.EntityAttach;
import com.viettel.voffice.database.entity.EntityFileAttachment;
import com.viettel.voffice.database.entity.EntityImageOrg;
import com.viettel.voffice.database.entity.EntityMarkInfo;
import com.viettel.voffice.database.entity.EntityStaff;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.User.EntityUser;
import com.viettel.voffice.database.entity.User.Vof2_EntityUser;
import com.viettel.voffice.database.entity.log.EntityActionLogMobile;
import com.viettel.voffice.database.entity.sign.EntityP12Cert;
import com.viettel.voffice.database.entity.sign.ErrSign;
import com.viettel.voffice.database.entity.task.EntityTask;
import com.viettel.voffice.database.entity.task.EntityTaskApproval;
import com.viettel.voffice.database.entity.text.EntityMutiSms;
import com.viettel.voffice.database.entity.text.EntityText;
import com.viettel.voffice.database.entity.text.SignHashMulti;
import com.viettel.voffice.database.entity.text.SignMultiFile;

/**
 *
 * @author thanght6
 */
public class SignUtils {

    // Log file
    private static final Logger LOGGER = Logger.getLogger(SignUtils.class);
    // Xac nhan giao cong viec ca nhan
    private static final String CONFIRM_TO_ASSIGN_TASK = "giao cong viec ca nhan cho dong chi %s";
    // Xac nhan danh gia cong viec ca nhan
    private static final String CONFIRM_TO_ASSESS_TASK = "danh gia cong viec ca nhan cho dong chi %s";
    public static final String MAIN_SIGN_TYPE = "1"; //Ky duyet van ban
    public static final String VT_SIGN_TYPE = "0"; //Xet duyet van ban
    public static final String PRE_SIGN_TYPE = "2"; //Ky nhay van ban
    // 201812-Pitagon: add
    public static final String MARK_TYPE = "3"; //Ky nhay van ban
    public static final Long SIM_VERSION = 3L; //phien ban sim ky
    // Ky bang sim CA
    private static final String SIGN_BY_CA_SIM_2 = "KYSIM2";
    // Ky bang chung thu mem
    private static final String SIGN_BY_SOFT_CERTIFICATE_2 = "KYMEM2";
    // Mo ta loi khi thuc hien ky bang chung thu mem
    private static final String ERROR_DESCRIPTION_FOR_SIGNING_BY_SOFT_CERTIFICATE = "%s - textId: %d - errorCode: %d - errorMessage: %s";
    
    private static final String ERROR_DESCRIPTION_FOR_SIGNING_TASK_FILE_BY_SOFT_CERTIFICATE = "Ky file giao viec/danh gia - %s - ErrorCode: %d";
    
    /** Buoc doc file truoc khi hash */
    private static final String READ_FILE = "READ FILE";
    
    /** Buoc hash file khi ky mem */
    private static final String HASH_FILE = "HASH FILE";
    
    /** Buoc dinh chu ky khi ky mem */
    private static final String APPEND_SIGNATURE = "APPEND SIGNATURE";
    
    /** Buoc ghi fiile sau khi dinh chu ky thanh cong */
    private static final String WRITE_FILE = "WRITE FILE";
    
    
    // Buoc dinh cap nhat du lieu sau ky
    private static final String UDPATE_DATA_AFFTER_SIGNATURE = "UPDATE_DATA_SIGNATURE";
    // Storage mac dinh
    private static final String DEFAULT_STORAGE = "storage_null";
    private static final String ERROR_DESCRIPTION_FOR_SIGNING_TASK_FILE = "Ky file giao viec/danh gia - ErrorCode: %d";
    //Begin HaNH
    //Ma loi update DB that bai
    private static final Long CODE_UPDATE_DATABASE_FAILED = -1L;
    private static final String MESS_UPDATE_DATABASE_FAILED = "Cap nhat DataBase that bai!";
    //End HaNH

    /**
     * Tao doi tuong thong tin nguoi ky theo user tren he thong 1
     *
     * @param user User tren he thong 1
     * @return
     */
    public static SignerInfo getSignerInfo(EntityUser user) {
        SignerInfo signerInfo = null;
        // Neu doi tuong user null
        // -> Tra ve null
        if (user == null) {
            LOGGER.error("getSignerInfo - user he thong 1 null");
            return signerInfo;
        }
        signerInfo = new SignerInfo();
        signerInfo.setUserName(user.getFullName());
        signerInfo.setUserId(user.getUserId());
        return signerInfo;
    }

    /**
     * Tao doi tuong thong tin nguoi ky theo user tren he thong 2 Su dung cho
     * phan ky cong viec
     *
     * @param user User tren he thong 2
     * @return
     */
    public static SignerInfo getSignerInfo(Vof2_EntityUser user) {
        SignerInfo signerInfo = null;
        // Neu doi tuong user null
        // -> Tra ve null
        if (user == null) {
            LOGGER.error("getSignerInfo - user he thong 2 null");
            return signerInfo;
        }
        signerInfo = new SignerInfo();
        signerInfo.setUserName(user.getFullName());
        signerInfo.setUserId(user.getUserId());
        return signerInfo;
    }

    /**
     * <b>KY CONG VIEC CA NHAN TREN MOBILE BANG SIM CA</b>
     *
     * @author thanght6
     * @param user1 Doi tuong user he thong 1
     * @param user2 Doi tuong user he thong 2
     * @param type Loai file ky<br> 1: File giao cong viec ca nhan dau thang<br>
     * 2: File danh gia cong viec cuoi thang<br>
     * @param fileName Ten file
     * @param filePath Duong dan file can ky
     * @param listTask Danh sach cong viec
     * @param period Ky danh gia (yyyyMM)
     * @param orgId
     * @param comment Noi dung chi dao
     * @return Tra ve ma loi sau khi ky
     */
    public static Long signTaskByCASIM(Vof2_EntityUser user2, int type,
            String fileName, String filePath, List<EntityTask> listTask, String period,
            Long orgId, String comment, String storage) {

        Date startTime = new Date();
        Long result = null;
        ActionLogMobileDAO actionLogMobileDAO = new ActionLogMobileDAO();
        // Kiem tra user va user id tren he thong 1 va 2
        // Neu user null hoac user id null
        // -> Tra ve null
        if (user2 == null || user2.getUserId() == null) {
            LOGGER.error("signByCASIM - user2 null");
            return result;
        }
        // Lay user id tren he thong 1
//        Long userId1 = user1.getUserId();
        // Lay user id tren he thong 2
        Long userId2 = user2.getUserId();
        String loginName = user2.getStrCardNumber();

        // Kiem tra xem user da duoc giao viec hay danh gia chua
        TaskDAO taskDAO = new TaskDAO();
        Long enforcementId = listTask.get(0).getEnforcementId();
        boolean isApproved = taskDAO.checkAssignTaskForEmployee(enforcementId, type, period);
        if (isApproved) {
            switch (type) {
                case 1:
                    LOGGER.error("signByCASIM - Nhan vien da duoc giao viec trong thang");
                    result = -2L;
                    break;
                case 2:
                    LOGGER.error("signByCASIM - Nhan vien da duoc danh gia cong viec trong thang");
                    result = -3L;
                    break;
            }
            return result;
        }

        // Neu ky file giao cong viec ca nhan dau thang
        // hoac file danh gia cong viec ca nhan cuoi thang
        // Kiem tra danh sach cong viec
        // Neu danh sach cong viec null hoac rong
        // -> Tra ve null
        if (type == FileUtils.TASK_ASSIGNMENT_FILE_TYPE
                || type == FileUtils.TASK_ASSESSMENT_FILE_TYPE) {
            if (CommonUtils.isEmpty(listTask)) {
                LOGGER.error("signByCASIM - Ky file giao viec hoac file danh gia"
                        + " cong viec | listTask null hoac rong");
                return result;
            }
        }

        // Kiem tra type
        String confirmText;
        String enforcementName = listTask.get(0).getEnforcementName();
        enforcementName = CommonUtils.isEmpty(enforcementName)
                ? "" : FunctionCommon.removeUnsign(enforcementName);
        switch (type) {
            // Giao cong viec ca nhan dau thang
            case FileUtils.TASK_ASSIGNMENT_FILE_TYPE:
                confirmText = CONFIRM_TO_ASSIGN_TASK;
                break;
            // Danh gia cong viec ca nhan cuoi cuoi thang
            case FileUtils.TASK_ASSESSMENT_FILE_TYPE:
                confirmText = CONFIRM_TO_ASSESS_TASK;
                break;
            default:
                LOGGER.error("signByCASIM - type khong hop le | type = " + type);
                return result;
        }
        // Them ten nguoi duoc giao viec/danh gia vao noi dung xac nhan        
        confirmText = String.format(confirmText, enforcementName);

        // Kiem tra xem user co dang thuc hien giao dich ky khong
        // Neu dang co giao dich ky SIM CA (false)
        // -> Tra ve thong bao dang co giao dich ky SIM CA
        LogTranstionSignDAO logTranstionSignDAO = new LogTranstionSignDAO();
        if (!logTranstionSignDAO.checkStatusBeforeSignByCASIM(userId2)) {
            result = -1L;
            LOGGER.error("signByCASIM - Dang thuc hien 1 giao dich ky SIM truoc do");
            return result;
        }

        SequenceNumber.getInstance(new SequenceId());
        // Lay thong tin SIM CA cua nguoi ky
        // Neu khong lay duoc thong tin SIM CA cua nhan vien
        // -> Tra ve that bai
        StaffDAO staffDAO = new StaffDAO();
        EntityStaff staff = staffDAO.getCASIMInfoOfStaff(userId2);
        if (staff == null) {
            LOGGER.error("signByCASIM - Khong lay duoc thong tin SIM CA cua nguoi ky");
            return result;
        }

        // Lay ra file can ky
        // Neu file can ky khong ton tai
        // -> Tra ve that bai
        File file = FileUtils.getFileByType(type, filePath, storage);
        if (file == null || !file.exists()) {
            LOGGER.error("signByCASIM - File ky khong ton tai - filePath: " + filePath);
            return result;
        }
        // Duong dan tuyet doi cua file goc
        String originFilePath = file.getAbsolutePath();
        // Duong dan file da ky
        String signedFilePath = FileUtils.generatePathForSignedFile(originFilePath,
                String.valueOf(userId2));
        // Duong dan tuong doi cua file sau khi ky
        String relativePath = FileUtils.getRelativePath(filePath, signedFilePath);
        if (CommonUtils.isEmpty(relativePath)) {
            LOGGER.error("signByCASIM - Khong lay duoc duong dan tuong doi cua"
                    + " file sau khi ky");
            return result;
        }
        // Tao doi tuong thong tin nguoi ky theo doi tuong user tren he thong 1
        // Neu khong tao duoc doi tuong thong tin nguoi ky
        // -> Tra ve null
        SignerInfo signerInfo = getSignerInfo(user2);
        if (signerInfo == null) {
            LOGGER.error("signByCASIM - signerInfo null");
            return result;
        }

        SimSign simSign = new SimSign(signerInfo, originFilePath, signedFilePath, false);
        // Chen log giao dich truoc khi ky
        if (!logTranstionSignDAO.insert(-1L, userId2, 0, null)) {
            LOGGER.error("signByCASIM - Chen log giao dich ky SIM CA that bai");
            return result;
        }

        // Don vi nguoi ky
        String location = user2.getAdOrgName() != null ? user2.getAdOrgName() : "";

        // Neu ky thanh cong -> Thuc hien chen ban ghi vao database                       
        if (simSign.sign(staff.getCaSerial(), staff.getSimCAVersion(),
                staff.getCaSIMPhoneNumber(), location, confirmText, comment)) {
            insertRecordAfterSignSuccess(type, userId2, fileName, relativePath,
                    listTask, period, orgId, comment, null, null);
        }

        // Unlock giao dich sau khi ky
        logTranstionSignDAO.unlockSignTransactionByUser(userId2);

        result = simSign.getErrorCode();

        actionLogMobileDAO.insert(userId2, loginName, startTime, new Date(),
                SIGN_BY_CA_SIM_2, String.format(ERROR_DESCRIPTION_FOR_SIGNING_TASK_FILE,
                        result), null, null, null, null, null);
        return result;
    }

    /**
     * <b>Lay chuoi bam cua file</b>
     *
     * @param request
     * @param user1 User tren he thong 1
     * @param user2 User tren he thong 2
     * @param publicKey Khoa cong khai
     * @param type Loai 1: File giao cong viec ca nhan dau thang
     * @param filePath Duong dan tuong doi cua file da duoc ky
     * @param comment Noi dung chi dao
     * @return
     */
    public static String getHashFileTask(HttpServletRequest request, EntityUser user1,
            Vof2_EntityUser user2, String publicKey, int type, String filePath,
            String comment) {

        ActionLogMobileDAO actionLogMobileDAO = new ActionLogMobileDAO();
        Date startTime = new Date();
        String hashString = null;
        // Kiem tra user tren he thong 1
        if (user2 == null || user2.getUserId() == null) {
            LOGGER.error("getHashFile - user1 null hoac userId1 null");
            return hashString;
        }
//        Long userId1 = user1.getUserId();
        String loginName = user2.getStrCardNumber();
        // Lay doi tuong thong tin nguoi ky theo doi tuong user tren he thong 1
        // Neu khong tao duoc doi tuong thong tin nguoi ky
        // -> Tra ve null
        SignerInfo signerInfo = getSignerInfo(user2);
        if (signerInfo == null) {
            LOGGER.error("getHashFile - SignerInfo null");
            return hashString;
        }

        // Lay ra file can ky
        // Neu file khong ton tai
        // -> Tra ve null
        File file = FileUtils.getFileByType(type, filePath);
        if (file == null || !file.exists()) {
            LOGGER.error("getHashFile - File ky khong ton tai");
            return hashString;
        }

        // Duong dan tuyet doi cua file goc
        String originFilePath = file.getAbsolutePath();

        // Duong dan file da ky
        String signedFilePath = FileUtils.generatePathForSignedFile(originFilePath,
                String.valueOf(user2.getUserId()));

        // Tao doi tuong ky mem
        SoftSign softSign = new SoftSign(signerInfo, originFilePath, signedFilePath, false);

        // Lay thong tin chung thu cua user
        // Neu co chung thu
        // -> Lay duong dan cua chung thu
        P12CertDAO p12CertDAO = new P12CertDAO();
        Long userId1 = null;
        if (user1 != null) {
            userId1 = user1.getUserId();
        }
        EntityP12Cert p12Cert = p12CertDAO.getP12CertificateOfUser(userId1,
                user2.getUserId(), publicKey);
        // 16:28 05/11/2016 - ThangHT6
        // Fix loi logic do Sonar quet
//        String certPath = null;
//        if (p12Cert == null) {
//            certPath = p12Cert.getCrtPath();
//        }
        // Neu khong lay duoc thong tin chung thu tra ve thong bao chua duoc cap chung thu
        if (p12Cert == null || p12Cert.getStatus() == null
                || CommonUtils.isEmpty(p12Cert.getCrtPath())) {
            hashString = Constant.DIGITAL_SIGN_RESULT_CODE.ERR_CODE_SEVITICATE_NOTFOUND + "";
            return hashString;
        }
        // Kiem tra xem chung thu co bi tam ngung khong
        if (p12Cert.getStatus() == Constants.P12Cert.Status.SUSPENDING) {
            hashString = Constant.DIGITAL_SIGN_RESULT_CODE.ERR_CODE_CERPENDING + "";
            return hashString;
        }
        String certPath = p12Cert.getCrtPath();
        List<String> listHashInfo = null;
        // Don vi nguoi ky
        String location = "";
        if (!CommonUtils.isEmpty(user2.getAdOrgName())) {
            location = user2.getAdOrgName();
        }
        try {
            listHashInfo = softSign.getDigest(certPath, comment, location);
        } catch (Exception ex) {
            // 16:51 05/11/2016 - ThangHT6
            // Fix loi logic do Sonar quet (Loi khong thong bao day du thong tin ngoai le)
//            LOGGER.error("getHashFile - Exception:" + ex.getMessage());
            LOGGER.error("getHashFile - Exception!", ex);
        }
        Long errorCode = softSign.getErrorCode();
        hashString = errorCode.toString();

        // Neu ky thanh cong
        // -> Luu doi tuong ky vao session
        if (errorCode.equals(Constant.DIGITAL_SIGN_RESULT_CODE.ERR_CODE_HASH_SUCSESS)) {
            // Luu doi tuong vao session that bai
            // -> Tra ve null
            if (!CommonControler.setSignSession(request, softSign)) {
                LOGGER.error("getHashFile - Luu phien ky that bai");
                return null;
            }
            // 16:56 05/11/2016 - ThangHT6
            // Fix loi logic do Sonar quet
            if (!CommonUtils.isEmpty(listHashInfo) && listHashInfo != null
                    && !CommonUtils.isEmpty(listHashInfo.get(0))) {
                hashString = listHashInfo.get(0);
            } else {
                LOGGER.error("getHashFile - Khong co choi hash");
            }
        }
        actionLogMobileDAO.insert(user2.getUserId(), loginName, startTime, new Date(),
                SIGN_BY_SOFT_CERTIFICATE_2, String.format(
                        ERROR_DESCRIPTION_FOR_SIGNING_TASK_FILE_BY_SOFT_CERTIFICATE,
                        HASH_FILE, errorCode), null, null, null, null, null);
        return hashString;
    }

    /**
     * <b>MOBILE: KY MEM CONG VIEC CA NHAN: Dinh chu ky vao file</b>
     *
     * @param request
     * @param user1
     * @param user2
     * @param type Loai<br>
     * 1: Ky phieu giao viec<br>
     * 2: Ky phieu danh gia cong viec<br>
     * @param fileName Ten file giao viec/danh gia
     * @param filePath Duong dan file giao viec/danh gia
     * @param listTask Danh sach cong viec da giao/danh gia
     * @param period Ky giao viec/danh gia
     * @param orgId Id don vi
     * @param signature Chu ky
     * @param comment Y kien cua nguoi ky
     * @return
     */
    public static String appendSignatureTask(HttpServletRequest request,
            EntityUser user1, Vof2_EntityUser user2, int type, String fileName,
            String filePath, List<EntityTask> listTask, String period, Long orgId,
            String signature, String comment) {

        Date startTime = new Date();
        ActionLogMobileDAO actionLogMobileDAO = new ActionLogMobileDAO();
        String result = null;
        // Kiem tra doi tuong user tren he thong 1 va 2
        if (user2 == null || user2.getUserId() == null) {
            LOGGER.error("appendSignature - Doi tuong user null hoac user2 null");
            return result;
        }
//        Long userId1 = user2.getUserIdVof1();
        String loginName = user2.getStrCardNumber();
        Long userId2 = user2.getUserId();

        // Lay session id tu request
        // Neu session id null hoac rong
        // -> Tra ve null
        String sessionId = CommonControler.getSessionIdFromRequest(request);
        if (CommonUtils.isEmpty(sessionId)) {
            LOGGER.error("appendSignature - session id null hoac rong");
            return result;
        }

        // Lay session theo session id
        // Neu session null
        // -> Tra ve null
        HttpSession session = HttpSessionCollector.find(sessionId);
        if (session == null) {
            LOGGER.error("appendSignature - session null");
            return result;
        }

        // Lay doi tuong ky trong session
        SoftSign softSign = (SoftSign) session.getAttribute("signObject");
        if (softSign == null) {
            LOGGER.error("appendSignature - Doi tuong ky mem SoftSign null");
            return result;
        }
        try {
            // Dinh chu ky thanh cong
            // -> Chen ban ghi vao database
            if (softSign.appendSignature(signature)) {
                // Lay ra file truoc khi ky
                File file = FileUtils.getFileByType(type, filePath);
                // Duong dan tuyet doi cua file goc
                String originFilePath = file.getAbsolutePath();
                // Duong dan file da ky
                String signedFilePath = FileUtils.generatePathForSignedFile(
                        originFilePath, String.valueOf(userId2));
                String relativePath = FileUtils.getRelativePath(filePath, signedFilePath);
                insertRecordAfterSignSuccess(type, userId2, fileName, relativePath,
                        listTask, period, orgId, comment, null, null);
            }
            Long errorCode = softSign.getErrorCode();
            if (errorCode != null) {
                result = errorCode.toString();
            }
            actionLogMobileDAO.insert(userId2, loginName, startTime, new Date(),
                    SIGN_BY_SOFT_CERTIFICATE_2, String.format(
                            ERROR_DESCRIPTION_FOR_SIGNING_TASK_FILE_BY_SOFT_CERTIFICATE,
                            APPEND_SIGNATURE, errorCode), null, null, null, null, null);
        } catch (Exception ex) {
            // 16:57 05/11/2016
            // Fix loi logic do Sonar quet (Loi khong thong bao day du thong tin ngoai le)
//            LOGGER.error("appendSignature - Exception:" + ex.getMessage());
            LOGGER.error("appendSignature - Exception!", ex);
        }
        return result;
    }
    /**
     * Buoc thuc hien bam file ra chuoi hash
     */
    public static final int STEP_HASH_FILE = 1;
    /**
     * Buoc them chu ky vao file
     */
    public static final int STEP_APPEND_SIGNATURE = 2;

    /**
     * <b>Ky mem file giao viec/danh gia cong viec ca nhan</b>
     *
     * @param request
     * @param user1 Doi tuong user tren he thong 1
     * @param user2 Doi tuong user tren he thong 2
     * @param step Buoc thu may trong qua trinh ky<br> 1: Thuc hien hash
     * file<br> 2: Dinh chu ky vao file<br>
     * @param publicKey
     * @param type Loai 1: Ky file giao cong viec ca nhan dau thang
     * @param fileName
     * @param filePath Duong dan tuong doi file
     * @param listTask
     * @param period
     * @param orgId
     * @param signature
     * @param comment
     * @return
     */
    public static String signSoftTask(HttpServletRequest request, EntityUser user1,
            Vof2_EntityUser user2, int step, String publicKey, int type, String fileName,
            String filePath, List<EntityTask> listTask, String period, Long orgId,
            String signature, String comment) {

        String result = null;
        // Kiem tra xem user da duoc giao viec hay danh gia chua
        Long enforcementId = listTask.get(0).getEnforcementId();
        TaskDAO taskDAO = new TaskDAO();
        boolean isApproved = taskDAO.checkAssignTaskForEmployee(enforcementId, type, period);
        if (isApproved) {
            switch (type) {
                case 1:
                    LOGGER.error("signSoft - Nhan vien da duoc giao viec trong thang");
                    result = "-2";
                    break;
                case 2:
                    LOGGER.error("signSoft - Nhan vien da duoc danh gia cong viec trong thang");
                    result = "-3";
                    break;
            }
            return result;
        }
        switch (step) {
            // Buoc thuc hien hash file
            case STEP_HASH_FILE:
                result = getHashFileTask(request, user1, user2, publicKey, type,
                        filePath, comment);
                break;
            // Buoc dinh chu ky vao file
            case STEP_APPEND_SIGNATURE:
                result = appendSignatureTask(request, user1, user2, type, fileName,
                        filePath, listTask, period, orgId, signature, comment);
                break;
            default:
                LOGGER.error("Step khong hop le | step = " + step);
                return result;
        }
        return result;
    }

    /**
     * Thuc hien chen ban ghi cho luong giao viec sau khi ky thanh cong
     *
     * @param signerId Id nguoi ky
     * @param fileName Ten file
     * @param filePath Duong dan file
     * @param listTask Danh sach cong viec
     * @param period Ky danh gia
     * @param orgId
     * @return
     */
    public static boolean insertRecordForTaskAssignmentFlow(Long signerId, String fileName,
            String filePath, List<EntityTask> listTask, String period, Long orgId,
            Integer signTypeWeb, String storage) {
        boolean result = false;
        if (signTypeWeb == null) {
            // Chen vao bang file
            // ??? Khong thay mo ta trang thai trong database
            int type = 0;
            int state = 0;
            int signType = 0;
            FileAttachmentDAO fileAttachmentDAO = new FileAttachmentDAO();
            Long fileId = fileAttachmentDAO.insertIntoFilesTable(type, fileName, state,
                    signerId, signerId, filePath, signType, storage);
            if (fileId == null) {
                return result;
            }

            TaskApprovalDAO taskApprovalDAO = new TaskApprovalDAO();
            // 21/11/2016 - ThangHT6
            // Cap nhat nhung ban ghi da ky truoc do gia tri del_flag = 1
            taskApprovalDAO.delete(signerId, period, new int[]{1, 2},
                    listTask.get(0).getEnforcementId());

            // Chen vao bang task_approval
            type = 2;
            if (taskApprovalDAO.insertBatchIntoTaskApprovalTable(listTask, period,
                    signerId, fileId, type, signerId, signerId)) {
                result = true;
            }
        } else {
            // Chen vao bang file
            // ??? Khong thay mo ta trang thai trong database
            int type = 0;
            int state = 0;
            int signType = 0;
            FileAttachmentDAO fileAttachmentDAO = new FileAttachmentDAO();
            Long fileId = fileAttachmentDAO.insertIntoFilesTable(type, fileName, state,
                    signerId, signerId, filePath, signType, storage);
            if (fileId == null) {
                return result;
            }

            TaskApprovalDAO taskApprovalDAO = new TaskApprovalDAO();
            // 21/11/2016 - ThangHT6
            // Cap nhat nhung ban ghi da ky truoc do gia tri del_flag = 1
//            System.out.println("---SignUtils listTask:" + FunctionCommon.generateJSONBase(listTask));
            taskApprovalDAO.delete(signerId, period, new int[]{1, 2},
                    listTask.get(0).getEnforcementId());

            // Chen vao bang task_approval
            if (signTypeWeb.equals(0)) {
                //Ky thuong
                if (taskApprovalDAO.insertBatchIntoTaskApprovalTable(listTask, period,
                        signerId, fileId, 3, signerId, signerId)) {
                    result = true;
                }
            } else if (signTypeWeb.equals(2)||signTypeWeb.equals(1)||signTypeWeb.equals(3)) {
                //Ky USB
                if (taskApprovalDAO.insertBatchIntoTaskApprovalTable(listTask, period,
                        signerId, fileId, 1, signerId, signerId)) {
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * <b>Thuc hien chen ban ghi cho luong danh gia cong viec ca nhan</b>
     *
     * @param signerId          id nguoi ky
     * @param fileName          ten file
     * @param filePath          duong dan file
     * @param listTask          danh sach cong viec
     * @param period            ky danh gia (yyyydd)
     * @param signTypeWeb 
     * @param storage 
     * @return
     */
    public static boolean insertRecordForTaskAssessmentFlow(Long signerId,
            String fileName, String filePath, List<EntityTask> listTask,
            String period, Integer signTypeWeb, String storage) {

        boolean result = false;
        String errorDesc = String.format(Constants.Common.LOG_SYNTAX,
                "insertRecordForTaskAssessmentFlow", signerId, "signType", signTypeWeb);
        //HaNH signTypeWeb: phuong thuc ky (ky USB, ky thuong, ...) - Chi danh cho client Web
        if (signTypeWeb == null) {
            //signTypeWeb = null thi xu ly tren mobile
            // Cac cong viec co status = 3 va is_complete = 0
            // -> Cap nhat thanh is_complete = 1
            TaskDAO taskDAO = new TaskDAO();
            if (!taskDAO.update(signerId, listTask)) {
                LOGGER.error(errorDesc + "Cap nhat is_completed that bai!");
                return result;
            }
            // Chen ban ghi vao bang file
            int type = 8;
            int state = 0;
            int signType = 0;
            FileAttachmentDAO fileAttachmentDAO = new FileAttachmentDAO();
            Long fileId = fileAttachmentDAO.insertIntoFilesTable(type, fileName,
                    state, signerId, signerId, filePath, signType, storage);
            if (fileId == null) {
                LOGGER.error(errorDesc + "Chen ban ghi vao bang files that bai!");
                return result;
            }

            // Chen ban ghi vao bang task_rating
            TaskRatingDAO taskRatingDAO = new TaskRatingDAO();
//            // Xoa cac ban ghi da ky chot danh gia danh gia truoc do trong bang task_rating (del_flag = 1)
//            taskRatingDAO.delete(signerId, Constants.TaskRating.Status.LEADER_SIGNED_ASSESSMENT,
//                    period, listTask.get(0).getEnforcementId());
//            if (!taskRatingDAO.insert(listTask, signerId, new int[]{
//                Constants.TaskRating.Status.LEADER_ASSESSED,
//                Constants.TaskRating.Status.LEADER_SIGNED_ASSESSMENT}, period, fileId)) {
//                LOGGER.error(errorDesc + "Chen ban ghi vao bang task_rating that bai!");
//                return result;
//            }
            if (!taskRatingDAO.merge(signerId, listTask, Constants.TaskRating.Status
                    .LEADER_SIGNED_ASSESSMENT, period, fileId)) {
                LOGGER.error(errorDesc + "Chen ban ghi vao bang task_rating that bai!");
                return result;
            }
            TaskApprovalDAO taskApprovalDAO = new TaskApprovalDAO();
            // 21/11/2016 - ThangHT6
            // Cap nhat nhung ban ghi da ky truoc do gia tri del_flag = 1
            taskApprovalDAO.delete(signerId, period, new int[]{3}, listTask.get(0).getEnforcementId());

            // Chen ban ghi vao bang task_approval
            type = 2; // Ky sim
            // Thiet lap trang thai phe duyet danh gia cho cong viec
            for (EntityTask task : listTask) {
                task.setApprovalState(Constants.TASK_APPROVAL.STATE.ASSESSED);
            }
            if (!taskApprovalDAO.insertBatchIntoTaskApprovalTable(listTask, period, signerId, fileId, type, signerId, signerId)) {
                LOGGER.error(errorDesc + "Chen ban ghi vao bang task_approval that bai!");
                return result;
            }
            // Den day la viec thuc hien chen cac ban ghi vao database thanh cong
            result = true;
        } else {
            //signTypeWeb khac null thi xu lu tren web
            // Cac cong viec co status = 3 va is_complete = 0
            // -> Cap nhat thanh is_complete = 1
            TaskDAO taskDAO = new TaskDAO();
            if (!taskDAO.update(signerId, listTask)) {
                LOGGER.error("insertRecordForTaskAssessmentFlow - Cap nhat is_completed that bai");
                return result;
            }

            // Chen ban ghi vao bang file
            int type = 0;
            int state = 0;
            int signType = 0;
            FileAttachmentDAO fileAttachmentDAO = new FileAttachmentDAO();
            Long fileId = fileAttachmentDAO.insertIntoFilesTable(type, fileName,
                    state, signerId, signerId, filePath, signType, storage);
            if (fileId == null) {
                LOGGER.error(errorDesc + "Chen ban ghi vao bang files that bai!");
                return result;
            }

            // Chen ban ghi vao bang task_rating
            TaskRatingDAO taskRatingDAO = new TaskRatingDAO();
//            // Xoa cac ban ghi da ky chot danh gia danh gia truoc do trong bang task_rating (del_flag = 1)
//            taskRatingDAO.delete(signerId, Constants.TaskRating.Status.LEADER_SIGNED_ASSESSMENT,
//                    period, listTask.get(0).getEnforcementId());
//            if (!taskRatingDAO.insert(listTask, signerId, new int[]{
//                Constants.TaskRating.Status.LEADER_ASSESSED,
//                Constants.TaskRating.Status.LEADER_SIGNED_ASSESSMENT}, period, fileId)) {
//                LOGGER.error(errorDesc + "Chen ban ghi vao bang task_rating that bai!");
//                return result;
//            }
            if (!taskRatingDAO.merge(signerId, listTask, Constants.TaskRating.Status
                    .LEADER_SIGNED_ASSESSMENT, period, fileId)) {
                LOGGER.error(errorDesc + "Chen ban ghi vao bang task_rating that bai!");
                return result;
            }
 
            TaskApprovalDAO taskApprovalDAO = new TaskApprovalDAO();
            // 21/11/2016 - ThangHT6
            // Cap nhat nhung ban ghi da ky truoc do gia tri del_flag = 1
            taskApprovalDAO.delete(signerId, period, new int[]{3}, listTask.get(0).getEnforcementId());

            // Chen ban ghi vao bang task_approval
            if (signTypeWeb.equals(0)) {
                //Ky thuong
                type = 3;
            } else if (signTypeWeb.equals(2) 
                    || signTypeWeb.equals(1) 
                    || signTypeWeb.equals(3)) {
                //Ky USB
                type = 1;
            }
            // Thiet lap trang thai phe duyet danh gia cho cong viec
            for (EntityTask task : listTask) {
                task.setApprovalState(Constants.TASK_APPROVAL.STATE.ASSESSED);
            }
            if (!taskApprovalDAO.insertBatchIntoTaskApprovalTable(listTask, period, signerId, fileId, type, signerId, signerId)) {
                LOGGER.error(errorDesc + "Chen ban ghi vao bang task_approval that bai!");
                return result;
            }
            // Den day la viec thuc hien chen cac ban ghi vao database thanh cong
            result = true;
        }
        return result;
    }

    /**
     * <b>Chen ban ghi vao database sau khi ky CONG VIEC CA NHAN thanh cong</b>
     *
     * @param type Loai<br>
     * 1: Luong giao cong viec ca nhan dau thang<br>
     * 2: Luong danh gia cong viec ca nhan cuoi thang<br>
     * @param signerId Id nguoi ky phieu giao viec/danh gia
     * @param fileName Ten file phieu giao viec/danh gia
     * @param filePath Duong dan file phieu giao viec/danh gia
     * @param listTask Danh sach cong viec duoc giao/danh gia
     * @param period Ky giao viec/danh gia
     * @param orgId Id don vi
     * @param comment Y kien cua nguoi ky
     * @param signType
     * @param storage
     * @return
     */
    public static boolean insertRecordAfterSignSuccess(int type, Long signerId,
            String fileName, String filePath, List<EntityTask> listTask, String period,
            Long orgId, String comment, Integer signType, String storage) {

        if (CommonUtils.isEmpty(storage)) {
            storage = "task_export";
        }
        TaskDAO t = new TaskDAO();
        boolean result = false;
        switch (type) {
            // Chen ban ghi cho luong giao cong viec ca nhan dau thang
            case FileUtils.TASK_ASSIGNMENT_FILE_TYPE:
                result = insertRecordForTaskAssignmentFlow(signerId, fileName,
                        filePath, listTask, period, orgId, signType, storage);
                if (result) {
                    if (signType != null && signType.equals(0)) {
                        //Ky thuong
                        t.updateFileAttachmentFromTask(orgId, comment, getListTask(
                                listTask, signerId, period), 0);
                    } else if (signType != null && signType.equals(2)) {
                        //Ky USB
                        t.updateFileAttachmentFromTask(orgId, comment, getListTask(
                                listTask, signerId, period), 1);
                    } else {
                        t.updateFileAttachmentFromTask(orgId, comment, getListTask(
                                listTask, signerId, period), 4);
                    }
                    //Gui tin nhan
                    PersonTaskDAO persionTask = new PersonTaskDAO();
                    persionTask.sendSmsPersionTask(signerId, listTask, type, period);
                }
                break;
            // Chen ban ghi cho luong danh gia cong viec ca nhan cuoi thang
            case FileUtils.TASK_ASSESSMENT_FILE_TYPE:
                result = insertRecordForTaskAssessmentFlow(signerId, fileName,
                        filePath, listTask, period, signType, storage);
                if (result) {
                    if (signType != null && signType.equals(0)) {
                        //Ky thuong
                        t.convertRatingTaskToPDF(orgId, comment, getListTask(listTask,
                                signerId, period), 0);
                    } else if (signType != null && signType.equals(2)) {
                        //Ky USB
                        t.convertRatingTaskToPDF(orgId, comment, getListTask(listTask,
                                signerId, period), 1);
                    } else {
                        //Ky mem
                        t.convertRatingTaskToPDF(orgId, comment, getListTask(listTask,
                                signerId, period), 4);
                    }
//                    System.out.println("nhay vao luong ky phieu danh gia");
                    //Gui tin nhan
                    PersonTaskDAO persionTask = new PersonTaskDAO();
                    persionTask.sendSmsPersionTask(signerId, listTask, type, period);
                }
                break;
            default:
                LOGGER.error("insertRecordAfterSignSuccess - type khong hop le, type chi nhan gia tri trong khoang (1, 2), type hien tai la:" + type);
                return result;
        }
        return result;
    }

    //=====================thuc hien ky nhieu file van ban======================
    public static ErrSign checkCer(EntityUserGroup entityUserGroup, String publicKey,
            String strCertificate) {
//        System.out.println("=====publicKey:" + publicKey);
        EntityUser user1 = entityUserGroup.getItemEntityUser();
        ErrSign result = new ErrSign();
        Date now = new Date();
        now = FunctionCommon.dateSort(now);
        if (strCertificate != null && strCertificate.trim().length() > 0) {
            // Tao doi tuong ky mem
            X509Certificate x509Cert = CertUtils.getX509Cert(strCertificate);
            if (now.compareTo(FunctionCommon.dateSort(x509Cert.getNotAfter())) > 0
                    || now.compareTo(FunctionCommon.dateSort(x509Cert.getNotBefore())) < 0) {
                //bo sung them ma loi cho het han trong vong 1 thang
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MONTH, -1);
                Date dateAfter1Month = cal.getTime();
                if(dateAfter1Month.compareTo(FunctionCommon.dateSort(x509Cert.getNotBefore())) >= 0){
                    result.setErrCode(NumberConstants.DIGITAL_SIGN_RESULT_CODE.ERR_CODE_CEREXPIRE_1MONTH);
                    result.setStrErrMess(NumberConstants.DIGITAL_SIGN_RESULT_CODE.ERR_MESS_CEREXPIRE);
                }else{
                    result.setErrCode(NumberConstants.DIGITAL_SIGN_RESULT_CODE.ERR_CODE_CEREXPIRE);
                    result.setStrErrMess(NumberConstants.DIGITAL_SIGN_RESULT_CODE.ERR_MESS_CEREXPIRE);
                }
            } else {
                result.setErrCode(NumberConstants.DIGITAL_SIGN_RESULT_CODE.ERR_CODE_INIT_SUCSESS);
            }
        } else if (publicKey != null && publicKey.trim().length() > 0) {
            try {
                P12CertDAO p12CertDAO = new P12CertDAO();
                Long userIdVof1 = (user1 != null) ? user1.getUserId() : null;
                Long userIdVof2 = entityUserGroup.getUserId2();
                EntityP12Cert p12Cert = p12CertDAO.getP12CertificateOfUser(userIdVof1,
                        userIdVof2, publicKey);
                // 17:01 05/11/2016 - ThangHT6
                // Fix loi logic do Sonar quet (Loi gan gia tri nhung khong su dung)
                if (p12Cert == null) {
                    //chung thu khong ton tai
                    result.setErrCode(NumberConstants.DIGITAL_SIGN_RESULT_CODE.ERR_CODE_FILE_SERVITICATE_NOT_EXIST);
                    result.setStrErrMess(NumberConstants.DIGITAL_SIGN_RESULT_CODE.ERR_MESS_FILE_SERVITICATE_NOT_EXIST);
                } else {
                    //check trang thai chung thu co bi thu hoi khong
                    if (p12Cert.getStatus().equals(2L)) {
                        //chung thu dang hoat dong
                        String certPath = p12Cert.getCrtPath();
                        X509Certificate x509Cert = DigitalSignUtils.getCertificateFromCerFile(
                                FunctionCommon.getPropertiesValue("sign.certpath") + certPath);
                        if (x509Cert == null) {
                            result.setErrCode(NumberConstants.DIGITAL_SIGN_RESULT_CODE.ERR_CODE_FILE_SERVITICATE_NOT_EXIST);
                            result.setStrErrMess(NumberConstants.DIGITAL_SIGN_RESULT_CODE.ERR_MESS_FILE_SERVITICATE_NOT_EXIST);
                        } else if (now.compareTo(FunctionCommon.dateSort(x509Cert.getNotAfter())) > 0
                                || now.compareTo(FunctionCommon.dateSort(x509Cert.getNotBefore())) < 0) {
                            result.setErrCode(NumberConstants.DIGITAL_SIGN_RESULT_CODE.ERR_CODE_CEREXPIRE);
                            result.setStrErrMess(NumberConstants.DIGITAL_SIGN_RESULT_CODE.ERR_MESS_CEREXPIRE);
                        } else {
                            result.setErrCode(NumberConstants.DIGITAL_SIGN_RESULT_CODE.ERR_CODE_INIT_SUCSESS);
                        }
                    } else if (p12Cert.getStatus().equals(5L)) {
                        //trang thai tam ngung
                        result.setErrCode(NumberConstants.DIGITAL_SIGN_RESULT_CODE.ERR_CODE_CERPENDING);
                        result.setStrErrMess(NumberConstants.DIGITAL_SIGN_RESULT_CODE.ERR_MESS_CERPENDING);
                    } else {
                        //chung thu bi thu hoi
                        result.setErrCode(NumberConstants.DIGITAL_SIGN_RESULT_CODE.ERR_CODE_CERREMOVE);
                        result.setStrErrMess(NumberConstants.DIGITAL_SIGN_RESULT_CODE.ERR_MESS_CERREMOVE);
                    }
                }
            } catch (CertificateException | FileNotFoundException ex) {
                result.setErrCode(NumberConstants.DIGITAL_SIGN_RESULT_CODE.ERR_CODE_FILE_SERVITICATE_NOT_EXIST);
                result.setStrErrMess(NumberConstants.DIGITAL_SIGN_RESULT_CODE.ERR_MESS_FILE_SERVITICATE_NOT_EXIST);
                java.util.logging.Logger.getLogger(SignUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            result.setErrCode(NumberConstants.DIGITAL_SIGN_RESULT_CODE.ERR_CODE_SEVITICATE_NOTFOUND);
            result.setStrErrMess(NumberConstants.DIGITAL_SIGN_RESULT_CODE.ERR_MESS_SEVITICATE_NOTFOUND);
        }
        return result;
    }
    
    /**
     * <b>Thuc hien hash file theo danh sach file</b>
     * Ham nay phuc vu cho viec file mem hoac ky usb van ban trinh ky
     * TungHD, DatDC mod cho con dau
     * @param user1
     * @param user2
     * @param publicKey
     * @param listAttachment
     * @param certificate
     * @param comment
     * @param deviceName
     * @param signType
     * @param mapTextProcessId
     * @param lstMarkInfo
     * @param lstMark
     * @return
     * @throws Throwable
     */
    public static List<SignMultiFile> hashListFile(EntityUser user1,
            Vof2_EntityUser user2, String publicKey, List<EntityAttach> listAttachment,
            String certificate, String comment, String deviceName, String signType,
            Map<Long, Long> mapTextProcessId, List<EntityMarkInfo> lstMarkInfo, List<EntityMarkInfo> lstMark) throws Throwable{

        // Tao bien luu thoi gian bat dau hash file
        Date startTime;
        List<SignMultiFile> listSignFile = new ArrayList<SignMultiFile>();
        //Kiem tra user tren he thong 1
        if (user2 == null || user2.getUserId() == null) {
            LOGGER.error("getHashFile - user1 null hoac userId1 null");
            listSignFile = null;
            return listSignFile;
        }
        Long userId2 = (user2 != null) ? user2.getUserId() : -91581L;
        Long userId1 = (user1 != null) ? user1.getUserId() : -91581L;
        String loginName = (user2 != null) ? user2.getStrCardNumber() : "";
        SignerInfo signerInfo = getSignerInfo(user2);
        if (signerInfo == null) {
            LOGGER.error("getHashFile - SignerInfo null");
            listSignFile = null;
            return listSignFile;
        }
        if (CommonUtils.isEmpty(listAttachment)) {
            listSignFile = null;
            return listSignFile;
        }
        // Lay duong dan chung thu
        String certPath = null;
        if (certificate != null && certificate.trim().length() > 0) {
        } else {
            P12CertDAO p12CertDAO = new P12CertDAO();
            LOGGER.error("========publicKey:" + publicKey);
            EntityP12Cert p12Cert = p12CertDAO.getP12CertificateOfUser(userId1,
                    userId2, publicKey);
            if (p12Cert != null) {
                certPath = p12Cert.getCrtPath();
            }
        }

        try {
            DigitalSign digitalSign = null;
            // Lay ra storage ma server duoc ghi
            String newStorage = FunctionCommon.getStorageConfigFile("storageName");
//            LOGGER.error("hashListFile - New storage: " + newStorage);

            // Lay ra thu muc tuong ung voi storage
            String newStorageFolder = FunctionCommon.getStorageConfigFile(newStorage);
//            LOGGER.error("hashListFile - New storage folder: " + newStorageFolder);

            SignMultiFile itemFileSign;
            Long textId;
            Long fileId;
            String oldStorage;
            String oldStorageFolder;
            String originFilePath;
            File file;
            String newFilePath;
            String signedFilePath;
            TextSignDAO textSignDao = new TextSignDAO();
            UsbSign usbSign;
            SoftSign softSign;
            Long codeErr;
            //Tunghd add param dau mac dinh start
            Long orgMarkId = null;
            Long typeFile = null;
            Long signLocate = null;
            Long groupTypeImage = null;
            Long documentMarkId = null;
            // Tunghd add param dau mac dinh end
            // Datdc ad param check dau mac dinh hay tuy chon
            Boolean isMarkMulti = false;
            EntityActionLogMobile log;
            List<EntityActionLogMobile> listLog;
            String functionName = SIGN_BY_SOFT_CERTIFICATE_2
                    + "_" + listAttachment.size() + "FILE";
            Date hashingStartTime, hashingEndTime;
            Map<String, List<Date>> mapSigningStep;
            List<Date> listReadingTime, listHashingTime;
            // Duyet danh sach file can ky            
            for (EntityAttach attachment : listAttachment) {
              // TungHD dong dau mac dinh start
             // Lay ra id van ban cua file ky
                textId = attachment.getTextId();
                fileId = attachment.getAttachId();
                //Tunghd add
                for(EntityMarkInfo emi : lstMarkInfo){
                    if(textId != null && textId.equals(emi.getTextId()) && fileId.equals(emi.getFileId())){
                        orgMarkId = emi.getOrgMarkId();
                        typeFile = emi.getTypeFile();
                        signLocate = emi.getSignLocate();
                        groupTypeImage = emi.getGroupTypeImage();
                        documentMarkId = emi.getDocumentId();
                        isMarkMulti = emi.getIsMarkMulti();
                    }
                }
                // 201812-Pitagon: add
                String path = null;
                List<EntityMarkInfo> lstConfigImage = new ArrayList<>();
                Long textProcessId = mapTextProcessId != null && !mapTextProcessId.isEmpty() ? mapTextProcessId.get(attachment.getTextId()) : null;
                if (SignUtils.MARK_TYPE.equals(signType)) {
                    ImageOrgDAO imageDao = new ImageOrgDAO();
                    List<EntityMarkInfo> listImagePath = imageDao.getImageOrgMarkPath(textProcessId, 1L);
                    if(!CommonUtils.isEmpty(listImagePath)){
                        path = listImagePath.get(0).getContextPath();
                        lstConfigImage = imageDao.getImageConfig(listImagePath.get(0), 1L);
                    } else {
                        LOGGER.error("getHashFile - KHONG CO ANH CON DAU MAC DINH");
                        listSignFile = null;
                        return listSignFile;
                    }
                }
                listLog = new ArrayList<>();
                mapSigningStep = new LinkedHashMap<>();
                startTime = new Date();
                // Khoi tao doi tuong luu thong tin ky file
                itemFileSign = new SignMultiFile();
                // Check neu ko co group type thi vao day
                // Datdc end Lay lst toa do tuong ung voi file
                if (!CommonUtils.isEmpty(lstMark)) {
                    groupTypeImage = lstMark.get(0).getGroupType();
                }
                // Datdc Lay lst toa do tuong ung voi file
                List<EntityMarkInfo> lstMarkFile = new ArrayList<EntityMarkInfo>();
                if (!CommonUtils.isEmpty(lstMark)) {
                    for (EntityMarkInfo entity: lstMark) {
                        if (textId.equals(entity.getObjectId()) && fileId.equals(entity.getFileId())) {
                            lstMarkFile.add(entity);
                        }
                    } 
                    if (!CommonUtils.isEmpty(lstMarkFile)) {
                        itemFileSign.setMarkFileInfo(lstMarkFile);
                    }
                }
                itemFileSign.setOrgMarkId(orgMarkId); 
                itemFileSign.setTypeFile(typeFile); 
                itemFileSign.setSignLocate(signLocate);
                itemFileSign.setGroupTypeImage(groupTypeImage);
                itemFileSign.setDocumentMarkId(documentMarkId);
                // Datdc gan xem la dong tuy chon hay tuy y
                itemFileSign.setIsMarkMulti(isMarkMulti);
                // TungHD dong dau mac dinh end
                // Gan id van ban
                itemFileSign.setTextId(textId);
                itemFileSign.setId(fileId);
                // Gan y kien ky
                itemFileSign.setStrComment(comment);
                try {
                    // Lay storage cua file
                    // Neu storage null thi gan storage mac dinh
                    oldStorage = attachment.getStorage();
                    if (CommonUtils.isEmpty(oldStorage)) {
                        oldStorage = DEFAULT_STORAGE;
                    }
//                    LOGGER.info("hashListFile - Old storage: " + oldStorage);

                    // Lay thu muc cua storage
                    oldStorageFolder = FunctionCommon.getStorageConfigFile(oldStorage);
//                    LOGGER.info("hashListFile - Old storage folder: " + oldStorageFolder);

                    // Duong dan tuyet doi cua file goc
                    originFilePath = oldStorageFolder + attachment.getPath();
//                    LOGGER.info("hashListFile - Origin file path: " + originFilePath);

                    // Kiem tra xem file can ky co ton tai khong
                    file = new File(originFilePath);
                    if (!file.exists()) {
                        itemFileSign.setCodeErr(Constant.DIGITAL_SIGN_RESULT_CODE.ERR_CODE_FILE_NOT_EXIST);
                        itemFileSign.setMssErr(Constant.DIGITAL_SIGN_RESULT_CODE.ERR_MESS_FILE_NOT_EXIST);
                        listSignFile.add(itemFileSign);
//                        LOGGER.error("hashListFile - Loi file khong ton tai - " + originFilePath);
                        // Log giao dich ky vao database
                        log = new EntityActionLogMobile(userId1, loginName, startTime,
                                new Date(), functionName, HASH_FILE, 
                                ERROR_DESCRIPTION_FOR_SIGNING_BY_SOFT_CERTIFICATE,
                                textId, itemFileSign.getCodeErr(), itemFileSign.getMssErr() + " - " + fileId,
                                deviceName);
                        String strContenLogAdressAndPort = FunctionCommon.IPPORTSERVICE 
                                + ", Storage_read: " + oldStorage + ", Storage_write: " + newStorage ;
                        log.setContent(strContenLogAdressAndPort);
                        listLog.add(log);
                        LogUtils.insertActionLogMobile(userId2, listLog);
                        continue;
                    }

                    // Duong dan tuyet doi moi cua file ky
                    newFilePath = newStorageFolder + attachment.getPath();
//                    LOGGER.info("hashListFile - New file path: " + newFilePath);

                    itemFileSign.setFileRoot(newFilePath);
                    itemFileSign.setStorageFileSign(newStorage);
                    // Duong dan file da ky
                    signedFilePath = FileUtils.generatePathForSignedFile(newFilePath, String.valueOf(userId1));
//                    LOGGER.info("hashListFile - Signed file path: " + signedFilePath);
                    itemFileSign.setFileSign(signedFilePath.substring(newStorageFolder.length()));
                    List<String> listHashInfo = null;

                    String strLocation = textSignDao.getLocationSignByUser(textId, userId1, userId2,
                                user2.getAdOrgName());
                    JSONObject jsonObj = new JSONObject();
                    // TungHD dong dau mac dinh start
                    // Add tham so truyen vao file
                    Gson configGson = new Gson();
                    if (SignUtils.MARK_TYPE.equals(signType) && groupTypeImage.equals(1L)) {
                        if (!isMarkMulti) {
                            jsonObj.put("image", path);
                        }
                        jsonObj.put("marked", "1");
                        jsonObj.put("markerEmailOrg", user2.getStrEmail());
                        jsonObj.put("signLocate", itemFileSign.getSignLocate());
                        jsonObj.put("groupType", itemFileSign.getGroupTypeImage());
                        //Tunghd add
                        if(!lstConfigImage.isEmpty()){
                            lstConfigImage.get(0).setSignLocate(itemFileSign.getSignLocate());
                            jsonObj.put("lstConfigImage", configGson.toJson(lstConfigImage.get(0)));
                        }
                    } else {
                        jsonObj.put("location", strLocation);
                    }
                    // Datdc add list dong dau tuy chon
                    if (!CommonUtils.isEmpty(lstMarkFile)) {
                        Gson gson = new Gson();
                        jsonObj.put("lstMark", gson.toJson(lstMarkFile));

                    }
                    strLocation = jsonObj.toString();
                    // TungHD dong dau mac dinh end
                    
                    // Neu certificate khac null va rong -> Ky bang USB

                    //ghi logs thoi gian bat dau ky
//                    Date dateStartHash = new Date();
//                    long diff = Math.abs(startTime.getTime() - dateStartHash.getTime());
//                    System.out.println("=====datnv5 Hash Start: " + diff);
                    if (!CommonUtils.isEmpty(certificate)) {
                        usbSign = new UsbSign(signerInfo, originFilePath, signedFilePath, true);
                        listHashInfo = usbSign.getDigest(certificate, comment, strLocation);
                        digitalSign = usbSign;
                    } // Neu certPath khac null va rong -> Ky bang chung thu mem
                    else if (!CommonUtils.isEmpty(certPath)) {
                        // Khoi tao doi tuong ky mem
                        softSign = new SoftSign(signerInfo, originFilePath, signedFilePath, true);
//                        Date NewSoft = new Date();
//                        long diff11111 = Math.abs(dateStartHash.getTime() - NewSoft.getTime());
//                        System.out.println("=====datnv5 NewSoft: " + diff11111);
                        listHashInfo = softSign.getDigest(certPath, comment, strLocation);
                        digitalSign = softSign;
                    }

//                    Date dateEndHash = new Date();
//                    long diff1 = Math.abs(dateStartHash.getTime() - dateEndHash.getTime());
//                    System.out.println("=====datnv5 Hash End: " + diff1);
                    // Gan ma loi hash file
                    // 17:09 05/11/2016 - ThangHT6
                    // Fix loi logic do Sonar quet
//                    itemFileSign.setCodeErr(digitalSign.getErrorCode());
                    codeErr = digitalSign == null ? null : digitalSign.getErrorCode();
                    itemFileSign.setCodeErr(codeErr);
                    // Neu hash file thanh cong
                    if (digitalSign != null && digitalSign.getErrorCode().equals(Constant.DIGITAL_SIGN_RESULT_CODE.ERR_CODE_INIT_SUCSESS)) {
                        // Neu khong co chuoi hash thi thuc hien gan lai ma loi
                        // la hash file that bai
                        if (CommonUtils.isEmpty(listHashInfo)) {
                            itemFileSign.setCodeErr(Constant.DIGITAL_SIGN_RESULT_CODE.ERR_CODE_HASH_NOTSUCSESS);
                            itemFileSign.setMssErr(Constant.DIGITAL_SIGN_RESULT_CODE.ERR_MESS_HASH_NOTSUCSESS);
                        } // Neu co chuoi hash
                        else {
                            if (listHashInfo != null) {
                                itemFileSign.setHash(listHashInfo.get(0));
                            }
                            itemFileSign.setSoftSign(digitalSign);
                            itemFileSign.setMssErr("Hash file thanh cong");
                        }
                    }
                } catch (Exception ex) {
                    itemFileSign.setCodeErr(Constant.DIGITAL_SIGN_RESULT_CODE.ERR_CODE_HASH_NOTSUCSESS);
                    itemFileSign.setMssErr(Constant.DIGITAL_SIGN_RESULT_CODE.ERR_MESS_HASH_NOTSUCSESS);
                    LOGGER.error("getHashFile - textId: " + textId  + " - " + fileId
                            + " - Exception!", ex);
                }
                listSignFile.add(itemFileSign);
                // Log giao dich ky vao database
                hashingStartTime = digitalSign != null && digitalSign.getHashingStartTime() != null ?
                        digitalSign.getHashingStartTime() : new Date();
                hashingEndTime = digitalSign != null && digitalSign.getHashingEndTime() != null ?
                        digitalSign.getHashingEndTime() : new Date();
                // Tao danh sach thoi gian bat dau, thoi gian ket thuc doc file
                listReadingTime = new ArrayList<>();
                listReadingTime.add(startTime);
                listReadingTime.add(hashingStartTime);
                // Tao danh sach thoi gian bat dau, thoi gian ket thuc hash file
                listHashingTime = new ArrayList<>();
                listHashingTime.add(hashingStartTime);
                listHashingTime.add(hashingEndTime);
                mapSigningStep.put(READ_FILE, listReadingTime);
                mapSigningStep.put(HASH_FILE, listHashingTime);
                listLog = EntityActionLogMobile.createLog(userId2, loginName,
                        functionName, mapSigningStep,
                        ERROR_DESCRIPTION_FOR_SIGNING_BY_SOFT_CERTIFICATE,
                        textId, itemFileSign.getCodeErr(), itemFileSign.getMssErr() + " - " + fileId,
                        deviceName);
                LogUtils.insertActionLogMobile(userId2, listLog);
            }
        } catch (IOException ex) {
            //do stuff with exception
            LOGGER.error("getHashFile - Exception!", ex);
        }
//        Date endDate = new Date();
//        long diff11 = Math.abs(endDate.getTime() - startTime.getTime());
//        System.out.println("=====datnv5==== End: " + diff11);
        return listSignFile;
    }

    /**
     * <b>Thuc hien dinh chu ky vao file (USB token hoac chung thu mem)</b><br>
     *
     * @param request
     * @param listSignHash
     * @param strListFileSign
     * @param deviceName        Ten thiet bi
     * @return
     */
    public static List<SignHashMulti> appendSignatureIntoListFile(HttpServletRequest request,
            List<SignHashMulti> listSignHash, List<EntityFileAttachment> strListFileSign,
            String deviceName) {

        Date startTime = new Date();
        List<SignHashMulti> listResult = new ArrayList<>();
        String sessionId = CommonControler.getSessionIdFromRequest(request);
        if (CommonUtils.isEmpty(sessionId)) {
            SignHashMulti itemResult = new SignHashMulti();
            itemResult.setError("15811100");
            itemResult.setMess("Loi id null");
            listResult.add(itemResult);
            LOGGER.error("appendSignatureIntoListFile - session id null hoac rong");
            return listResult;
        }

        // Lay doi tuong user trong session
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        Long userId1 = user1 == null ? 0L : user1.getUserId();
        String loginName = user2 == null ? "" : user2.getStrCardNumber();
        Long user2Id = (user2 != null && user2.getUserId()!=null)?user2.getUserId():0L;
        
        // Lay doi tuong ky trong session
        HttpSession session = HttpSessionCollector.find(sessionId);
        List<SignMultiFile> listSignMultiFile = (List<SignMultiFile>) session.getAttribute("signObject");
        // Lay thong tin signType
        String signType = (String) session.getAttribute(ConstantsFieldParams.SIGN_TYPE);
        int sendSMS = (int) session.getAttribute(ConstantsFieldParams.SEND_SMS);
        // 201812-Pitagon: add
        Map<Long, Long> mapTextProcessId = (Map<Long, Long>) session.getAttribute(ConstantsFieldParams.TEXT_PROCESS_ID);

        EntityActionLogMobile log;
        List<EntityActionLogMobile> listLog;
        String functionName = SIGN_BY_SOFT_CERTIFICATE_2;
        if (CommonUtils.isEmpty(listSignMultiFile) || CommonUtils.isEmpty(listSignHash)) {
            SignHashMulti itemResult = new SignHashMulti();
            itemResult.setError("15811101");
            itemResult.setMess("Loi khong co chu ky");
            listResult.add(itemResult);
            LOGGER.error("appendSignatureIntoListFile - Danh sach doi tuong ky mem nul hoac rong"
                    + " hoac Danh sach chu ky null hoac rong");
            // Log giao dich ky vao database
            functionName += "_0FILE";
            log = new EntityActionLogMobile(user2Id, loginName, startTime,
                    new Date(), loginName, APPEND_SIGNATURE,
                    ERROR_DESCRIPTION_FOR_SIGNING_BY_SOFT_CERTIFICATE,
                    0L, 15811101L, "Loi khong co chu ky", deviceName);
            listLog = new ArrayList<>();
            listLog.add(log);
            LogUtils.insertActionLogMobile(user2Id, listLog);
            return listResult;
        }

        try {
            Map<Long, Calendar> mapTimeSign = new LinkedHashMap<Long, Calendar>();
            Map<Long, List<Object[]>> mapTextAttach = new LinkedHashMap<Long, List<Object[]>>();
            List<Object[]> arrayNewPathSignFile = null;
            Object[] objArr = null;
            
            List<Long> listIdTextSign = new ArrayList<>();
//            List<Long> listIdAttachFileSign = new ArrayList<>();
//            List<String> listAttachFileSign = new ArrayList<>();
//            List<Calendar> listIdTextSignDate = new ArrayList<>();
            DigitalSign softSign;
            Date writingStartTime, writingEndTime;
            List<Date> listAppendingTime, listWritingTime;
            Map<String, List<Date>> mapSigningStep;
            // Duyet danh sach file ky
            functionName += "_" + listSignHash.size() + "FILE";
            for (SignMultiFile signMultiFile : listSignMultiFile) {
                startTime = new Date();
                mapSigningStep = new LinkedHashMap<>();
                softSign = signMultiFile.getSoftSign();
                boolean signSuccess;
                SignHashMulti itemResult;
                // Duyet danh sach chu ky
                for (SignHashMulti signHash : listSignHash) {
                    // Neu file ky va chu ky cua cung 1 file van ban
                    if (signMultiFile.getId().equals(signHash.getId())) {

                        // Khoi tao doi tuong ket qua ky
                        itemResult = new SignHashMulti();
                        itemResult.setId(signMultiFile.getId());
                        itemResult.setTextId(signMultiFile.getTextId());
                        // Thuc hien ky
                        signSuccess = softSign.appendSignature(signHash.getSignature());
                        itemResult.setError(softSign.getErrorCode() == null
                                ? null : softSign.getErrorCode().toString());

                        // Neu ky thanh cong
                        if (signSuccess) {
                            itemResult.setMess("Ky thanh cong");
                            itemResult.setIdFileAttachSign(signMultiFile.getIdFileSign());
                            itemResult.setStorageFileSign(signMultiFile.getStorageFileSign());
                            // Them file van ban ky thanh cong vao danh sach
                            
                            if (!listIdTextSign.contains(signMultiFile.getTextId())) {
                                listIdTextSign.add(signMultiFile.getTextId());
                            }                            
                            mapTimeSign.put(signMultiFile.getTextId(), softSign.getCalendarSign());
                            objArr = new Object[] {signMultiFile.getId(), signMultiFile.getFileSign()};
                            arrayNewPathSignFile = mapTextAttach.get(signMultiFile.getTextId());
                            if (arrayNewPathSignFile == null) {
                                arrayNewPathSignFile = new ArrayList<>();
                            }
                            arrayNewPathSignFile.add(objArr);
                            mapTextAttach.put(signMultiFile.getTextId(), arrayNewPathSignFile);
                            
//                            listIdTextSignDate.add(softSign.getCalendarSign());
//                            listAttachFileSign.add(signMultiFile.getFileSign());
//                            listIdAttachFileSign.add(signMultiFile.getIdFileSign());
                            LOGGER.info("appendSignatureIntoListFile - textId: "
                                    + signMultiFile.getId() + " - Ky thanh cong");
                        } // Neu ky that bai
                        else {
                            itemResult.setMess("Loi dinh chu ky vao file");
                            LOGGER.error("appendSignatureIntoListFile - textId: "
                                    + signMultiFile.getId() + " - Ky that bai");
                        }
                        // Them ket qua ky van ban vao danh sach
                        listResult.add(itemResult);
                        
                        // Ghi log vao database
                        writingStartTime = softSign.getWritingStartTime() != null
                                ? softSign.getWritingStartTime() : new Date();
                        writingEndTime = softSign.getWritingEndTime() != null
                                ? softSign.getWritingEndTime() : new Date();
                        // Tao danh sach thoi gian bat dau, thoi gian ket thuc dinh chu ky vao file
                        listAppendingTime = new ArrayList<>();
                        listAppendingTime.add(startTime);
                        listAppendingTime.add(writingStartTime);
                        // Tao danh sach thoi gian bat dau, thoi gian ket thuc ghi file
                        listWritingTime = new ArrayList<>();
                        listWritingTime.add(writingStartTime);
                        listWritingTime.add(writingEndTime);
                        mapSigningStep.put(APPEND_SIGNATURE, listAppendingTime);
                        mapSigningStep.put(WRITE_FILE, listWritingTime);
                        listLog = EntityActionLogMobile.createLog(user2Id,
                                loginName, functionName, mapSigningStep,
                                ERROR_DESCRIPTION_FOR_SIGNING_BY_SOFT_CERTIFICATE,
                                signMultiFile.getId(), softSign.getErrorCode() == null ?
                                        0L : softSign.getErrorCode(), itemResult.getMess(), deviceName);
                        LogUtils.insertActionLogMobile(user2Id, listLog);
                        break;
                    }
                }
            }
            
            
            startTime = new Date();
            // Lay thong tin user 2
            Long v2UserId = null;
            Long v2GroupId = null;
            Vof2_EntityUser v2User = userGroup.getVof2_ItemEntityUser();
            if (v2User != null) {
                v2UserId = v2User.getUserId();
                v2GroupId = v2User.getAdOrgId();
            }
            Long sysUserId = userId1;
            boolean isSecrectary = user1 == null ? false : user1.isIsSecrectaryVo1();
            List<Long> lstGroup;
            if (isSecrectary) {
                lstGroup = user1 == null ? new ArrayList<Long>() : user1.getListGroupIdVTVof1();
            } else {
                lstGroup = user1 == null ? new ArrayList<Long>() : user1.getListGroupIdLDVof1();
            }
            String strLstGroup = "";
            for (int i = 0; i < lstGroup.size(); i++) {
                if (i < lstGroup.size() - 1) {
                    strLstGroup += lstGroup.get(i).toString() + ",";
                } else {
                    strLstGroup += lstGroup.get(i).toString();
                }
            }

            //---------------------HIendv2---------------------
            List<Long> lstGroupSecretaryVof1 = new ArrayList<>();
            List<Long> lstGroupSecretaryVof2;
            if (user1 != null) {
                // Lay danh sach don vi ma user la lanh dao hoac thu truong don vi hoac tro ly
                sysUserId = userId1;
                lstGroupSecretaryVof1 = user1.getListGroupIdVTVof1();
            }
            Vof2_EntityUser entityUserVof2 = userGroup.getVof2_ItemEntityUser();
            lstGroupSecretaryVof2 = entityUserVof2.getListSecretaryVhrOrg();
            Long v1GroupId = user1 == null ? null : user1.getGroupId();
            //-------------------End Hiendv----------------------------
            TextSignDAO tSignDao = new TextSignDAO();
            String keySaveFileTmpText = FunctionCommon.getPropertiesValue("storageName");
            if (listSignMultiFile.size() > 0 && signType != null) {
                String strValue;
                Long text_process_id;
                Long textId = listIdTextSign.get(0);
                Calendar textIdDateSign = mapTimeSign.get(textId);
                List<EntityText> lstText;
                switch (signType) {
                    case VT_SIGN_TYPE:
                        //neu la xet duyet van ban
                        tSignDao = new TextSignDAO();
                        //kiem tra xem trang thai van ban co phai la cho ky
                        lstText = tSignDao.getInfoText(textId);
                        if (!CommonUtils.isEmpty(lstText)
                                && lstText.get(0).getStatus() != null
                                && !lstText.get(0).getStatus().equals((long) Constants.Text.State.PROCESSING)) {
                            //neu trang thai van ban khac dang xu ly thi tra ve loi va khong cho ky
                            listResult = new ArrayList<>();
                            SignHashMulti itemResult = new SignHashMulti();
                            itemResult.setError(String.valueOf(ErrorCode.TEXT_SIGN_NOT_PERMISS.getErrorCode()));
                            itemResult.setMess(ErrorCode.TEXT_SIGN_NOT_PERMISS.getMessage());
                            listResult.add(itemResult);
                            return listResult;
                        }
//                        String newPathFile = listAttachFileSign.get(0);
//                        Long attachId = listIdAttachFileSign.get(0);
                        String comment = listSignMultiFile.get(0).getStrComment();
                        text_process_id = tSignDao.addFilesSign(strListFileSign, textId, userGroup, 0);
                        if (!tSignDao.updateDatabaseByVT(textId, textIdDateSign, sysUserId,
                                entityUserVof2, lstGroupSecretaryVof1, lstGroupSecretaryVof2,
                                comment, SIM_VERSION, mapTextAttach.get(textId), keySaveFileTmpText,
                                "", sendSMS, text_process_id)) {
                            listResult = null;
                        }
                        break;
                    case MAIN_SIGN_TYPE:
                        //ky chinh
                        //Thuc hien add file dinh kem khi ky USB
                        if (listIdTextSign.size() > 1) {
                            tSignDao.addFilesMultiSign(strListFileSign, listIdTextSign, userGroup);
//                            LOGGER.info("Hiendv2- Add file dinh kem khi ky chinh text_id:=" + strResAddFile);
                        } else {
                            tSignDao.addFilesSign(strListFileSign,
                                    textId, userGroup, Integer.parseInt(signType));
//                            LOGGER.info("Hiendv2- Add file dinh kem khi ky chinh text_process_id:=" + text_process_id);
                        }
                        //End Thuc hien add file dinh kem khi ky USB
                        Boolean isSignParallel = false;//ky song song

                        try {
                            if (listIdTextSign.size() == 1) {
                                //neu la ky don
                                //kiem tra xem trang thai van ban co phai la cho ky
                                lstText = tSignDao.getInfoText(textId);
                                
                                //datnv5: kiem tra xem dieu kien neu la van ban duoc tgd xu ly thi xu ly ky lại
                                boolean isUserConfigDuringSign = isUserInDuringSing(entityUserVof2.getUserId(), textId);
                                if(!isUserConfigDuringSign){
                                    if (!CommonUtils.isEmpty(lstText)
                                            && lstText.get(0).getStatus() != null
                                            && !lstText.get(0).getStatus().equals((long) Constants.Text.State.PROCESSING)) {
                                        //neu trang thai van ban khac dang xu ly thi tra ve loi va khong cho ky
                                        listResult = new ArrayList<>();
                                        SignHashMulti itemResult = new SignHashMulti();
                                        itemResult.setError(String.valueOf(ErrorCode.TEXT_SIGN_NOT_PERMISS.getErrorCode()));
                                        itemResult.setMess(ErrorCode.TEXT_SIGN_NOT_PERMISS.getMessage());
                                        listResult.add(itemResult);
                                        return listResult;
                                    }
                                }
                                //lay trang thai ky song song
                                List<EntityText> lstStateSignTextParallel = tSignDao.getListStateSignParallel(textId);
                                if (!CommonUtils.isEmpty(lstStateSignTextParallel)) {
                                    isSignParallel = true;
                                }
                                if (isSignParallel) {
                                    //neu la ky song 
                                    Boolean isLockSignParallel = tSignDao.isLockSignParallel(lstStateSignTextParallel, v2UserId);
//                                    System.out.println("Trang thai lock ky song song:" + isLockSignParallel.toString()
//                                            + "v2UserId: " + v2UserId);
                                    if (isLockSignParallel) {
                                        //neu la khoa ky song song thi bao loi
                                        listResult = new ArrayList<>();
                                        SignHashMulti itemResult = new SignHashMulti();
                                        itemResult.setError(String.valueOf(ErrorCode.TEXT_LOCK_SIGN_STATE.getErrorCode()));
                                        itemResult.setMess(ErrorCode.TEXT_LOCK_SIGN_STATE.getMessage());
                                        listResult.add(itemResult);
                                        return listResult;
                                    } else {
                                        //Cap nhat trang thai ky duyet
                                        DocumentSignDAO DocumentSignDAO = new DocumentSignDAO();
                                        DocumentSignDAO.updateSignParallelNext(textId, v2UserId,
                                                Constants.TextSignParallel.LOCK_SIGN_STATE, null);//khoa ky song song

                                        strValue = tSignDao.updateDatabaseMultiSign(listIdTextSign, mapTimeSign, sysUserId, v2User,
                                                null, strLstGroup, listSignMultiFile.get(0).getStrComment(), SIM_VERSION,
                                                mapTextAttach, signType, keySaveFileTmpText,
                                                v1GroupId, v2UserId, v2GroupId);
                                        if (strValue.trim().length() < 1) {
                                            listResult = null;
                                        }
                                    }
                                } else {
                                    strValue = tSignDao.updateDatabaseMultiSign(listIdTextSign, mapTimeSign, sysUserId, v2User,
                                            null, strLstGroup, listSignMultiFile.get(0).getStrComment(), SIM_VERSION,
                                            mapTextAttach, signType, keySaveFileTmpText,
                                            v1GroupId, v2UserId, v2GroupId);
                                    if (strValue.trim().length() < 1) {
                                        listResult = null;
                                    }
                                }
                            } else {
                                //neu la ky nhieu
                                strValue = tSignDao.updateDatabaseMultiSign(listIdTextSign, mapTimeSign, sysUserId, v2User,
                                        null, strLstGroup, listSignMultiFile.get(0).getStrComment(), SIM_VERSION,
                                        mapTextAttach, signType, keySaveFileTmpText,
                                        v1GroupId, v2UserId, v2GroupId);
                                if (strValue.trim().length() < 1) {
                                    listResult = null;
                                }
                            }
                        } catch (Exception ex) {
                            LOGGER.error(ex.getMessage(), ex);
                            listResult = null;
                        } finally {
                            if (isSignParallel) {
                                //neu la ky song song thi mo khoa
                                tSignDao.unlockSignTextParallel(textId, v2UserId);//mo khoa ky song song
                            }
                        }
                        break;
                    case PRE_SIGN_TYPE:
                        //ky nhay 
//                        LOGGER.info("Hiendv2- Add file dinh kem khi ky nhay text_process_id:=" + text_process_id);
                        tSignDao = new TextSignDAO();
                        //kiem tra xem trang thai van ban co phai la cho ky
                        lstText = tSignDao.getInfoText(textId);
                        if (!CommonUtils.isEmpty(lstText)
                                && lstText.get(0).getStatus() != null
                                && !lstText.get(0).getStatus().equals((long) Constants.Text.State.PROCESSING)) {
                            //neu trang thai van ban khac dang xu ly thi tra ve loi va khong cho ky
                            listResult = new ArrayList<>();
                            SignHashMulti itemResult = new SignHashMulti();
                            itemResult.setError(String.valueOf(ErrorCode.TEXT_SIGN_NOT_PERMISS.getErrorCode()));
                            itemResult.setMess(ErrorCode.TEXT_SIGN_NOT_PERMISS.getMessage());
                            listResult.add(itemResult);
                            return listResult;
                        }
                        text_process_id = tSignDao.addFilesSign(strListFileSign, textId, userGroup, Integer.parseInt(signType));
                        strValue = tSignDao.updateDatabaseMultiSign(listIdTextSign, mapTimeSign, sysUserId, v2User,
                                null, strLstGroup, listSignMultiFile.get(0).getStrComment(), SIM_VERSION,
                                mapTextAttach, signType, keySaveFileTmpText,
                                v1GroupId, v2UserId, v2GroupId);
                        if (strValue.trim().length() < 1) {
                            listResult = null;
                        }
                        break;
                    // 201812-Pitagon: add
                    case MARK_TYPE:
                        TextDAO textDao = new TextDAO();
                        TextSignDAO tsDao = new TextSignDAO();
                        // TungHD, DatDC sua cho dong dau don vi start
                        TextSignDAO markAttachHistoryDao = new TextSignDAO();
                        for (int i = 0; i < listIdTextSign.size(); i++) {
                            Long textProcessId = mapTextProcessId.get(listIdTextSign.get(i));
                            Long objId = listIdTextSign.get(i);
                            Long orgMarkId = listSignMultiFile.get(i).getOrgMarkId();
                            Long groupType = listSignMultiFile.get(i).getGroupTypeImage();
                            Boolean isMarkMulti = listSignMultiFile.get(i).getIsMarkMulti();
                            int result = 0;
                            Calendar dateSign = mapTimeSign.get(listIdTextSign.get(i));
                            Date signDate = dateSign.getTime();
                            if(groupType.equals(1L)){
                                result = textDao.approveMarkDefault(listIdTextSign.get(i), textProcessId, listSignMultiFile.get(0).getStrComment(), signDate);
                            } 
                            // Datdc start update list dau vi tri
                            if (isMarkMulti) {
                                // Update Path MarkAttachHistory
                                for(SignMultiFile indexFile : listSignMultiFile){
                                    if (!CommonUtils.isEmpty(indexFile.getMarkFileInfo())) {
                                        markAttachHistoryDao.insertMarkAttachHistoryRequistionMulti(keySaveFileTmpText, mapTextAttach.get(listIdTextSign.get(i)), 
                                                v2UserId, indexFile.getMarkFileInfo());
                                        break;
                                    }
                                }
                                tsDao.updateFileSign(v2User, listIdTextSign.get(i), keySaveFileTmpText, mapTextAttach.get(listIdTextSign.get(i)));
                                // Update Path new
                                for(SignMultiFile indexFile : listSignMultiFile){
                                    if (!CommonUtils.isEmpty(indexFile.getMarkFileInfo())) {
                                        if (textDao.updateMarkLocateMultiMark(indexFile.getMarkFileInfo(), v2UserId) == 0) {
                                            LOGGER.error("Insert Mark du lieu vao bang MARK_LOCATE khong thanh cong");
                                        }
                                    }
                                }
                            } else {
                            //Tunghd add
                            markAttachHistoryDao.insertMarkAttachHistoryReq(keySaveFileTmpText, mapTextAttach.get(listIdTextSign.get(i)), 
                                     v2UserId, objId, groupType, orgMarkId, null);
                            tsDao.updateFileSign(v2User, listIdTextSign.get(i), keySaveFileTmpText, mapTextAttach.get(listIdTextSign.get(i)));
                            if(result != 0){
                                for(SignMultiFile indexFile : listSignMultiFile){
                                    if(!textDao.updateMarkLocate(v2UserId, indexFile)){
                                        LOGGER.error("Insert du lieu vao bang MARK_LOCATE khong thanh cong");
                                        }
                                    }
                                }
                            }
                            //End
                            if (result != 0 && groupType.equals(1L)) {
                                EntityText text = textDao.getTextByID(textId);
                                if (text != null) {
                                    Long orgId = textDao.getOrgIdMarkDoc(textProcessId);
                                    CommonControler smsDAO = new CommonControler();
                                    smsDAO.sentSMS(text.getTitle(), userGroup.getUserId2(), text.getCreatorId(), orgId,
                                            Constants.SMS_TEXT_CONFIG.MARK_SUCCESS, listSignMultiFile.get(0).getStrComment(), 101L);
                                    
                                    // Update file khi dong dau doi voi van ban da ban hanh
                                    if (text.getDocumentId() != null) {
                                        AttachDAO attachDAO = new AttachDAO();
                                        // TungHD sua ham cho dong dau
                                        List<EntityFileAttachment> listMainFile = attachDAO.getListMarkedFile(text.getTextId());
                                        if (!CommonUtils.isEmpty(listMainFile)) {
                                            for (EntityFileAttachment file : listMainFile) {
                                                textDao.updateFilePathDocument(text.getDocumentId(), file.getFileAttachmentId(), file.getFilePath());
                                            }
                                        }
                                    }
                                }
                            }
                            // TungHD, DatDC sua cho dong dau don vi start
                            if (result == 2) {
                                TextPartnerDAO textPartnerDAO = new TextPartnerDAO();
                                textPartnerDAO.updateStateDocument(userGroup, null, null, textId);
                            }
                        }
                        break;
                    default:
//                        LOGGER.info(" signType khong hop le: " + signType);
                        break;
                }
                List<Long> listTextId = new ArrayList<>();
                List<Integer> listClassificationTypeAssessor = new ArrayList<>();
                for (SignHashMulti signHash : listSignHash) {
                    if (signHash.getClassificationTypeAssessor() != null) {
                        listTextId.add(signHash.getId());
                        listClassificationTypeAssessor.add(signHash.getClassificationTypeAssessor());
                    }
                }
                MissionSigningDAO missionSigningDAO = new MissionSigningDAO();
                missionSigningDAO.update(user2Id, listTextId, listClassificationTypeAssessor);
                // Ghi log xu ly cap nhat du lieu sau ky
                log = new EntityActionLogMobile(entityUserVof2.getUserId(), loginName,
                        startTime, new Date(), functionName, UDPATE_DATA_AFFTER_SIGNATURE,
                        ERROR_DESCRIPTION_FOR_SIGNING_BY_SOFT_CERTIFICATE, textId,
                        v2UserId, "Update du lieu sau ky thanh cong", deviceName);
                listLog = new ArrayList<>();
                listLog.add(log);
                LogUtils.insertActionLogMobile(entityUserVof2.getUserId(), listLog);
            }
        } catch (Exception ex) {
            LOGGER.error("appendSignature - Exception:", ex);
        // TungHD add them th throwable start
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // TungHD add them th throwable end
        return listResult;
    }
    
    // Dinh dang cau mo ta loi luu trong bang log service khi thuc hien ky
    private static final String ERROR_DESCRIPTION_FOR_SIGNING = "textId: %d - errorCode: %d - %s";

    /**
     * <b>Ky van ban bang SIM CA</b><br>
     *
     * @author thanght6
     * @since Jun 18, 2016
     * @param userGroup
     * @param userId Id user
     * @param textId Id van ban
     * @param title Tieu de van ban
     * @param comment Y kien
     * @param signatureType Loai ky<br> 1: Van thu xet duyet<br> 2: Lanh dao ky
     * nhay<br> 3: Lanh dao ky duyet<br>
     * @param sendSMS Gui tin nhan khi la van thu hay khong<br> 1: Gui tin
     * nhan<br>
     * @return
     */
    public static Long signTextByCASIM(EntityUserGroup userGroup, Long userId,
            Long textId, String title, String comment, int signatureType, int sendSMS) {

        //Lay danh sach don vi ma user la lanh dao hoac thu truong don vi hoac tro ly // Lay userId tren he thong 1
        Long userId1 = null;
        String loginName = "";
//        Long orgId = null;
//        Long sysUserIdVof1 = null;
        List<Long> lstGroupSecretaryVof1 = new ArrayList<Long>();
        List<Long> lstGroupSecretaryVof2 = new ArrayList<Long>();
        if (userGroup.getItemEntityUser() != null) {
            // Lay danh sach don vi ma user la lanh dao hoac thu truong don vi hoac tro ly
            userId1 = userGroup.getUserId1() != 0L ? userGroup.getUserId1() : -15811L;
//            orgId = userGroup.getItemEntityUser().getGroupId();

//            sysUserIdVof1 = userGroup.getItemEntityUser().getUserId();
            lstGroupSecretaryVof1 = userGroup.getItemEntityUser().getListGroupIdVTVof1();
            // Ten dang nhap cua user
            loginName = userGroup.getItemEntityUser().getLoginName();
        }
        Vof2_EntityUser entityUserVof2 = userGroup.getVof2_ItemEntityUser();
        //Thong tin don vi tren vof2.0
        lstGroupSecretaryVof2 = userGroup.getVof2_ItemEntityUser().getListSecretaryVhrOrg();

        // Thoi gian bat dau de ghi log
        Date startTime = new Date();

        // Kiem tra dau vao
        if (userId == null || textId == null) {
            LOGGER.error("signTextByCASIM - Loi du lieu dau vao khong hop le"
                    + " - userId: " + userId + "  | textId: " + textId);
            return null;
        }
        LogTranstionSignDAO logTranstionSignDAO = new LogTranstionSignDAO();
        ActionLogMobileDAO actionLogMobileDAO = new ActionLogMobileDAO();
        SequenceNumber.getInstance(new SequenceId());

        // Kiem tra xem user co dang thuc hien giao dich ky khong
        // Neu dang co giao dich ky SIM CA (false)
        // -> Tra ve thong bao dang co giao dich ky SIM CA        
        if (!logTranstionSignDAO.checkStatusBeforeSignByCASIM(userId)) {
            // Log file
            LOGGER.error("signTextByCASIM - Loi dang thuc hien 1 giao dich ky"
                    + " SIM truoc do - userId: " + userId + " - textId: " + textId);
            return -1L;
        }

        // Chen log truoc khi thuc hien giao dich ky
        if (!logTranstionSignDAO.insert(textId, userId, 0, null)) {
            LOGGER.error("signTextByCASIM - Loi thuc hien chen log truoc khi bat"
                    + " dau giao dich ky");
            return null;
        }

        // Lay thong tin SIM CA cua nguoi ky
        // Neu khong lay duoc thong tin SIM CA
        // -> Tra ve loi nguoi dung khong co sim CA
        StaffDAO staffDAO = new StaffDAO();
        EntityStaff staff = staffDAO.getCASIMInfoOfStaff(userId);
        if (staff == null || CommonUtils.isEmpty(staff.getCaSIMPhoneNumber())) {
            // Log file
            LOGGER.error("signTextByCASIM - Loi khong lay duoc thong tin SIM CA"
                    + " cua nguoi ky - userId: " + userId + " - textId: " + textId);
            // Cap nhat trang thai da hoan thanh giao dich ky
            logTranstionSignDAO.update(textId, userId, 1,
                    Constants.SIGN_RESULT_CODE.NO_SIM_CA_TEXT);
            // Log service
            actionLogMobileDAO.insert(userId, loginName, startTime, new Date(),
                    SIGN_BY_CA_SIM_2, String.format(ERROR_DESCRIPTION_FOR_SIGNING,
                            textId, Constant.SIGN_RESULT_CODE.NO_SIM_CA,
                            Constant.SIGN_RESULT_CODE.NO_SIM_CA_TEXT),
                    null, null, null, null, null);
            return Constant.SIGN_RESULT_CODE.NO_SIM_CA;
        }

        // Kiem tra duong dan file chung thu sim CA
        if (CommonUtils.isEmpty(staff.getCaSerial())) {
            // Log file
            LOGGER.error("signTextByCASIM - Loi khong co chung thu sim CA - userId: "
                    + userId + " - textId: " + textId);
            // Cap nhat trang thai da hoan thanh giao dich ky
            logTranstionSignDAO.update(textId, userId, 1,
                    Constants.SIGN_RESULT_CODE.NO_SIM_SERIAL_TEXT);
            // Log service
            actionLogMobileDAO.insert(userId, loginName, startTime, new Date(),
                    SIGN_BY_CA_SIM_2, String.format(ERROR_DESCRIPTION_FOR_SIGNING,
                            textId, Constant.SIGN_RESULT_CODE.NO_SIM_SERIAL,
                            Constant.SIGN_RESULT_CODE.NO_SIM_SERIAL_TEXT),
                    null, null, null, null, null);
            return Constant.SIGN_RESULT_CODE.NO_SIM_CA;
        }

        // Kiem tra xem van ban co duoc ky khong
        // Neu van ban khong duoc phep ky
        // -> Tuc la va ban da bi tu choi hoac ky truoc do
        // -> Tra ve ma loi van ban da bi tu cho hoac ky truoc do
//        TextProcessDAO textProcessDAO = new TextProcessDAO();
//        if (!textProcessDAO.checkSigningStatus(userId, textId)) {
//            // Ghi lai log loi ky van ban da xu ly tu truoc
//            LOGGER.error("signTextByCASIM - userId: " + userId + " - textId: " + textId
//                    + " - Loi van ban da duoc xu ly tu truoc");
//            // Cap nhat trang thai da hoan thanh giao dich ky
//            logTranstionSignDAO.update(textId, userId, 1,
//                    "Lỗi! Văn bản đã được xử lý từ trước");
//            // Log service
//            actionLogMobileDAO.insert(userId, loginName, startTime, new Date(),
//                    SIGN_BY_CA_SIM_2, String.format(ERROR_DESCRIPTION_FOR_SIGNING,
//                    textId, Constant.SIGN_RESULT_CODE.DOC_IS_PROCESS, "Văn bản đã"
//                    + " được xử lý từ trước"), null, null, null,
//                    null, null);
//            return Constant.SIGN_RESULT_CODE.DOC_IS_PROCESS;
//        }
        // Kiem tra file ky chinh cua van ban co ton tai khong
        AttachDAO attachDAO = new AttachDAO();
        EntityAttach attachmentOld = attachDAO.getAttachmentByTextId(textId);
        
        // Khong co file van ban trong database
        if (attachmentOld == null) {
            // Ghi lai log loi file van ban khong ton tai
            LOGGER.error("signTextByCASIM - userId: " + userId + " - textId: " + textId
                    + " - Loi file van ban khong ton tai (Khong co trong database)");
            // Cap nhat trang thai da hoan thanh giao dich ky
            logTranstionSignDAO.update(textId, userId, 1,
                    "Lỗi! File văn bản không tồn tại");
            // Log service
            actionLogMobileDAO.insert(userId, loginName, startTime, new Date(),
                    SIGN_BY_CA_SIM_2, String.format(ERROR_DESCRIPTION_FOR_SIGNING,
                            textId, Constant.SIGN_RESULT_CODE.FILE_NOT_EXIST, "File văn bản"
                            + " không tồn tại"), null, null, null,
                    null, null);
            return Constant.SIGN_RESULT_CODE.FILE_NOT_EXIST;
        }
        SignController signController = new SignController();
        boolean isNotReadDoc = signController.getDoNotReadTextDuringSign(entityUserVof2.getUserId(), textId);
        if(isNotReadDoc){
           return Constants.SIGN_RESULT_CODE.STATUS_DELAY_SIGN;
        }
        //datnv5: kiem tra file ky lai cua tgd
        List<Long> listTextId = new ArrayList<>();
        listTextId.add(textId);
        List<EntityAttach> listAttachmentOld = new ArrayList<>();
        listAttachmentOld.add(attachmentOld);
        List<EntityAttach> listAttachment = 
                attachDAO.getListAttachDuringSignAndUpdate(entityUserVof2.getUserId(),listTextId,listAttachmentOld);
        EntityAttach attachment = listAttachment.get(0);

        // Neu file khong co storage
        // -> Tuc la file van ban trinh ky tu dong duoc luu trong thu muc ung dung
        // -> Gan storage la "storage_null"
        // (storage_null duoc cau hinh tro den thu muc luu file cua ung dung)
        String storage = attachment.getStorage();
        if (CommonUtils.isEmpty(storage)) {
            storage = DEFAULT_STORAGE;
        }

        // Kiem tra file vat ly
        // Neu khong co file vat ly
        // -> Tra ve ma loi file van ban khong ton tai
        File file = FileUtils.getTextFile(storage, attachment.getPath());
        if (!file.exists()) {
            // Ghi lai log loi file van ban khong ton tai
            LOGGER.error("signTextByCASIM - userId: " + userId + " - textId: " + textId
                    + " - Loi file van ban khong ton tai (Khong co file vat ly: "
                    + file.getAbsolutePath() + " ) ");
            // Cap nhat trang thai da hoan thanh giao dich ky
            logTranstionSignDAO.update(textId, userId, 1,
                    "Lỗi! File văn bản không tồn tại - " + file.getAbsolutePath());
            // Log service
            actionLogMobileDAO.insert(userId, loginName, startTime, new Date(),
                    SIGN_BY_CA_SIM_2, String.format(ERROR_DESCRIPTION_FOR_SIGNING, textId,
                            Constant.SIGN_RESULT_CODE.FILE_NOT_EXIST, "File văn bản"
                            + " không tồn tại"), null, null, null, null, null);
            return Constant.SIGN_RESULT_CODE.FILE_NOT_EXIST;
        }

        // Lay duong dan tuyet doi cua file goc
        String originFilePath = file.getAbsolutePath();
        // Tao duong dan de luu file sau khi ky
        String signedFilePath = FileUtils.generatePathForSignedFile(originFilePath,
                userId.toString());

        // Thu muc storage
        String storageFolder = FileUtils.getStorageFolder(storage);
        // Lay duong dan tuong doi file vua ky
        String path = signedFilePath.substring(storageFolder.length());
        attachment.setPath(path);

        TextSignDAO textSignDao = new TextSignDAO();
//        System.out.println("Thong tin dau vao lay don vi: textId" + textId + " --userId1: "
//                + userId + "-----userId2: " + entityUserVof2.getUserId()
//                + "-------entityUserVof2.getAdOrgName(): " + entityUserVof2.getAdOrgName());
        String strLocation = textSignDao.getLocationSignByUser(textId, userId1, entityUserVof2.getUserId(),
                entityUserVof2.getAdOrgName());
        // Tao doi tuong nguoi ky de thuc hien ky
//        System.out.println("Ten don vi ky: " + strLocation);
        SignerInfo signerInfo = getSignerInfo(userGroup.getVof2_ItemEntityUser());
        if (signerInfo == null) {
            LOGGER.error("getHashFile - signerInfo null");
            return null;
        }
        // Khoi tao doi tuong ky sim
        SimSign simSign = new SimSign(signerInfo, originFilePath, signedFilePath, true);
        // Bo dau tieu de van ban
        title = FunctionCommon.removeUnsign(title);
        // Neu ky thanh cong -> Thuc hien cap nhat database
        if (simSign.sign(staff.getCaSerial(), staff.getSimCAVersion(),
                staff.getCaSIMPhoneNumber(), strLocation, title, comment)) {
            // Log ra duong dan file sau khi thanh cong
//            LOGGER.info("Signed file path: " + signedFilePath);
            // Cap nhat database
            TextSignDAO textDAO = new TextSignDAO();
            //datnv5: kiem tra xem dieu kien neu la van ban duoc tgd xu ly thi xu ly ky lại
            boolean isUserConfigDuringSign = isUserInDuringSing(entityUserVof2.getUserId(), textId);
            if(!isUserConfigDuringSign){
                //kiem tra xem trang thai van ban co phai la cho ky
                List<EntityText> lstText = textDAO.getInfoText(textId);
                if (!CommonUtils.isEmpty(lstText)
                        && lstText.get(0).getStatus() != null
                        && !lstText.get(0).getStatus().equals((long) Constants.Text.State.PROCESSING)) {
                    //neu trang thai van ban khac dang xu ly thi tra ve loi va khong cho ky
                    logTranstionSignDAO.update(textId, userId, 1, ErrorCode.TEXT_SIGN_NOT_PERMISS.getMessage());
                    Integer result = ErrorCode.TEXT_SIGN_NOT_PERMISS.getErrorCode();
                    return Long.valueOf(result.toString());
                }
            }
            List<Object[]> array = null;
            switch (signatureType) {
                // Van thu xet duyet
                case Constants.TextProcess.SignatureType.APPROVE:
                	array = new ArrayList<>();
                	array.add(new Object[] {attachment.getAttachId(), attachment.getPath()});
                    if (!textDAO.updateDatabaseByVT(textId, simSign.getCalendarSign(), userId1,
                            entityUserVof2, lstGroupSecretaryVof1, lstGroupSecretaryVof2, comment,
                            SIM_VERSION, array, attachment.getStorage(), "", sendSMS, -1L)) {
                        // Cap nhat trang thai da hoan thanh giao dich ky
                        logTranstionSignDAO.update(textId, userId, 1, "Lỗi cập"
                                + "nhật database sau khi văn thư ký xong");
                        return null;
                    }
                    break;
                case Constants.TextProcess.SignatureType.INITIAL_SIGN:
                case Constants.TextProcess.SignatureType.MAIN_SIGN:
                    TextSignDAO textSignDAO = new TextSignDAO();
//                    List<Long> listTextId = new ArrayList<Long>();
//                    listTextId.add(textId);
//                    listGroup = userGroup.getItemEntityUser().getLstGroupOnlyLeader1();
//                    List<Long> listAttachId = new ArrayList<Long>();
//                    listAttachId.add(attachment.getAttachId());
//                    List<String> listPath = new ArrayList<String>();
//                    listPath.add(attachment.getPath());
                    Long userIdVof2 = null;
                    Boolean isSignParallel = false;//ky song song
                    try {

                        boolean strValue;
                        //kiem tra co bi khoa ky song song khong
                        userIdVof2 = entityUserVof2.getUserId();
                        //lay trang thai ky song song
                        List<EntityText> lstStateSignTextParallel = textSignDAO.getListStateSignParallel(textId);
                        if (!CommonUtils.isEmpty(lstStateSignTextParallel)) {
                            isSignParallel = true;
                        }
                        if (isSignParallel) {
                            //neu la ky song 
                            Boolean isLockSignParallel = textSignDAO.isLockSignParallel(lstStateSignTextParallel, userIdVof2);
                            if (isLockSignParallel) {
                                //neu la khoa ky song song thi bao loi
                                Integer result = ErrorCode.TEXT_LOCK_SIGN_STATE.getErrorCode();
                                logTranstionSignDAO.update(textId, userId, 1, ErrorCode.TEXT_LOCK_SIGN_STATE.getMessage());
                                return Long.valueOf(result.toString());
                            } else {
                                //Cap nhat trang thai ky duyet
                                DocumentSignDAO DocumentSignDAO = new DocumentSignDAO();
                                DocumentSignDAO.updateSignParallelNext(textId, userIdVof2,
                                        Constants.TextSignParallel.LOCK_SIGN_STATE, null);//khoa ky song song
                                List<EntityMutiSms> lstMutiSms = new ArrayList<EntityMutiSms>();
                                array = new ArrayList<>();
                                array.add(new Object[] {attachment.getAttachId(), attachment.getPath()});
                                strValue = textSignDAO.updateDatabaseAffterSign(textId, simSign.getCalendarSign(),
                                        userId1, entityUserVof2, comment, SIM_VERSION, array, String.valueOf(signatureType),
                                        attachment.getStorage(), lstMutiSms);

                            }
                        } else {
                            //neu la ky thuong
                            List<EntityMutiSms> lstMutiSms = new ArrayList<EntityMutiSms>();
                            array = new ArrayList<>();
                            array.add(new Object[] {attachment.getAttachId(), attachment.getPath()});
                            strValue = textSignDAO.updateDatabaseAffterSign(textId, simSign.getCalendarSign(),
                                    userId1, entityUserVof2, comment, SIM_VERSION, array, String.valueOf(signatureType),
                                    attachment.getStorage(), lstMutiSms);
                        }
                        if (!strValue) {
                            logTranstionSignDAO.update(textId, userId, 1, "Lỗi cập"
                                    + " nhật database sau khi lãnh đạo ký xong");
                            return null;
                        }
                    } catch (Exception ex) {
                        LOGGER.error("signTextByCASIM - Exception: ", ex);
                        logTranstionSignDAO.update(textId, userId, 1, "Lỗi exception cập"
                                + "nhật database sau khi lãnh đạo ký xong");
                        return null;
                    } finally {
                        if (isSignParallel) {
                            //neu la ky song song thi mo khoa
                            TextSignDAO tSignDao = new TextSignDAO();
                            tSignDao.unlockSignTextParallel(textId, userIdVof2);//mo khoa ky song song
                        }
                    }
                    break;
                default:
                    logTranstionSignDAO.update(textId, userId, 1, "Lỗi loại ký"
                            + " không hợp lệ");
                    return null;
            }
        }

        // Lay ma loi
        Long errorCode = simSign.getErrorCode();

        // Cap nhat trang thai da hoan thanh giao dich ky
        logTranstionSignDAO.update(textId, userId, 1, "errorCode: " + errorCode);

        // Ghi Log service
        actionLogMobileDAO.insert(userId, loginName, startTime, new Date(),
                SIGN_BY_CA_SIM_2, String.format(ERROR_DESCRIPTION_FOR_SIGNING,
                        textId, errorCode, null), null, null, null, null, null);
        return errorCode;
    }

    private static List<EntityTaskApproval> getListTask(List<EntityTask> lsTask, Long signerId, String period) {
        List<EntityTaskApproval> listTask = new ArrayList<>();
        TreeMap<Long, String> tr = new TreeMap<>();
        if (!CommonUtils.isEmpty(lsTask)) {
            for (EntityTask rs : lsTask) {
                tr.put(rs.getEnforcementId(), "");
            }
        }
        for (Long key : tr.keySet()) {
            EntityTaskApproval ta = new EntityTaskApproval();
            ta.setApproverId(signerId);
            ta.setEnforcementId(key);
            ta.setPeriod(period);
            listTask.add(ta);
        }
        return listTask;
    }

    /**
     * WEB: HAM KY USB CONG VIEC CA NHAN
     *
     * @param request
     * @param user2
     * @param step
     * @param publicKey
     * @param type
     * @param listFileSign
     * @param period
     * @param orgId
     * @param comment
     * @param certificate
     * @param signType
     * @param listSignHash
     * @return
     */
    public static List<SignMultiFile> signUsbAndSoftTask(HttpServletRequest request,
            Vof2_EntityUser user2, int step, String publicKey, int type,
            List<SignMultiFile> listFileSign, String period, Long orgId, String comment,
            String certificate, Integer signType, List<SignHashMulti> listSignHash) {
        //String result = null;
//        System.out.println("gia tri cua list filesign" + CommonUtils.isEmpty(listFileSign));
//        System.out.println("gia tri cua list filehash" + CommonUtils.isEmpty(listSignHash));
        if (!CommonUtils.isEmpty(listFileSign) || !CommonUtils.isEmpty(listSignHash)) {
            switch (step) {
                // Buoc thuc hien hash file
                case STEP_HASH_FILE:
                    listFileSign = hashListFileSignTask(request, user2, type,
                            listFileSign, comment, certificate, publicKey, signType);

                    break;
                case STEP_APPEND_SIGNATURE:
                    // Lay user id tren he thong 2
                    Long userId2 = user2.getUserId();
                    listFileSign = appendSignatureAssignOrAssess(request,
                            listFileSign, listSignHash, type, userId2, period,
                            orgId, comment, signType);
            }

        }
        return listFileSign;
    }

    /**
     * <b>WEB: Hash file phieu giao viec/phieu danh gia</b>
     *
     * @author HaNH
     * @param request
     * @param user1
     * @param user2
     * @param type
     * @param listFileSign Danh sach cac file ky client gui len de hash
     * @param comment Nhan xet khi ky bang USB
     * @param signType
     * @param certificate Chung thu lay tu USB token
     * @return
     */
    public static List<SignMultiFile> hashListFileSignTask(HttpServletRequest request,
            Vof2_EntityUser user2, int type, List<SignMultiFile> listFileSign,
            String comment, String certificate,String publicKey, Integer signType) {
        //Kiem tra user tren he thong 2
        if (user2 == null || user2.getUserId() == null) {
            LOGGER.error("hashListFileSign - user2 null hoac userId1 null");
            listFileSign = null;
            return listFileSign;
        }
        Long userId2 = user2.getUserId();
        SignerInfo signerInfo = getSignerInfo(user2);
        if (signerInfo == null) {
            LOGGER.error("hashListFileSign - SignerInfo null");
            listFileSign = null;
            return listFileSign;
        }
        for (int i = 0; i < listFileSign.size(); i++) {
            SignMultiFile fileSign = listFileSign.get(i);

            try {
                DigitalSign digitalSign = null;
                String storage = FunctionCommon.getConfigFile("task_export");
                String filePath = fileSign.getFilePath();
                Long codeErr;
                
                // Lay ra file can ky
                // Neu file khong ton tai
                // -> Tra ve null
                // Duong dan tuyet doi cua file goc
                String originFilePath = storage + File.separator + filePath;
                originFilePath = FileUtils.getSafePath(originFilePath);
                File file = new File(originFilePath);
                if (!file.exists()) {
                    LOGGER.error("hashListFileSign - File ky khong ton tai");
                    listFileSign = null;
                    return listFileSign;
                }

                // Duong dan file da ky
                String signedFilePath = FileUtils.generatePathForSignedFile(originFilePath, String.valueOf(userId2));
                
                List<String> listHashInfo = null;
                // Don vi nguoi ky
                String strLocation = "";
                if (!CommonUtils.isEmpty(user2.getAdOrgName())) {
                    strLocation = user2.getAdOrgName();
                }

                try {
                    // Tao doi tuong ky USB
                    if (signType == 2) {
                        UsbSign usbSign = new UsbSign(signerInfo, originFilePath, signedFilePath, false);
                        if (!CommonUtils.isEmpty(certificate)) {
                            listHashInfo = usbSign.getDigest(certificate, comment, strLocation);
                            digitalSign = usbSign;
                        }
                    } else if (signType == 1) {
                        SoftSign softSign = new SoftSign(signerInfo, originFilePath, signedFilePath, false);
                        P12CertDAO p12CertDAO = new P12CertDAO();
                        EntityP12Cert p12Cert = p12CertDAO.getP12CertificateOfUser(null,
                                user2.getUserId(), publicKey);
                        // Neu khong lay duoc thong tin chung thu tra ve thong bao chua duoc cap chung thu
                        if (p12Cert == null || p12Cert.getStatus() == null
                                || CommonUtils.isEmpty(p12Cert.getCrtPath())) {
                            fileSign.setCodeErr(Constant.DIGITAL_SIGN_RESULT_CODE.ERR_CODE_CERTIFICATENOTFOUND);
                            fileSign.setMssErr(Constant.DIGITAL_SIGN_RESULT_CODE.ERR_MESS_FILE_NOT_EXIST);
                        } else if (p12Cert.getStatus() == Constants.P12Cert.Status.SUSPENDING) {
                            fileSign.setCodeErr(Constant.DIGITAL_SIGN_RESULT_CODE.ERR_CODE_CERPENDING);
                            fileSign.setMssErr(Constant.DIGITAL_SIGN_RESULT_CODE.ERR_MESS_CERPENDING);
                        } else {
                            String certPath = p12Cert.getCrtPath();
                            listHashInfo = softSign.getDigest(certPath, comment, strLocation);
                            digitalSign = softSign;
                        }
                    }
                    codeErr = digitalSign == null ? null : digitalSign.getErrorCode();
                    fileSign.setCodeErr(codeErr);
                    // Neu hash file thanh cong
                    if (digitalSign != null
                            && digitalSign.getErrorCode().equals(Constant.DIGITAL_SIGN_RESULT_CODE.ERR_CODE_INIT_SUCSESS)) {
                        // Neu khong co chuoi hash thi thuc hien gan lai ma loi
                        // la hash file that bai
                        if (CommonUtils.isEmpty(listHashInfo)) {
                            fileSign.setCodeErr(Constant.DIGITAL_SIGN_RESULT_CODE.ERR_CODE_HASH_NOTSUCSESS);
                            fileSign.setMssErr(Constant.DIGITAL_SIGN_RESULT_CODE.ERR_MESS_HASH_NOTSUCSESS);
                        } // Neu co chuoi hash
                        else {
                            if (listHashInfo != null) {
                                fileSign.setHash(listHashInfo.get(0));
                            }
                            fileSign.setSoftSign(digitalSign);
                            fileSign.setMssErr("Hash file thanh cong");
                        }
                    }
                } catch (Exception ex) {
                    fileSign.setCodeErr(Constant.DIGITAL_SIGN_RESULT_CODE.ERR_CODE_HASH_NOTSUCSESS);
                    fileSign.setMssErr(Constant.DIGITAL_SIGN_RESULT_CODE.ERR_MESS_HASH_NOTSUCSESS);
                    LOGGER.error("hashListFileSign - Exception!", ex);
                }
            } catch (IOException ex) {
                LOGGER.error("hashListFileSign - Exception!", ex);
            }
        }
//        System.out.println("ket thuc");
        return listFileSign;
    }

    /**
     * WEB: KY CONG VIEC CA NHAN - Dinh kem chu ky vao file va cap nhat ket qua
     * ky
     *
     * @param request
     * @param listFileSign Danh sach cac thong tin cua file ky (diem, danh gia,
     * nhan xet,...) de update vao DB
     * @param listSignHash Danh sach cac file da ky client gui len de server doi
     * chieu voi cac file ky luu trong session
     * @param type Loai ky phieu (1 - Phieu giao viec, 2 - Phieu danh gia)
     * @param userId2
     * @param period Ky ky phieu
     * @param orgId ID don vi duoc ky
     * @param comment Nhan xet khi ky bang USB
     * @param signType Loai ky (0 - Ky thuong, 2 - Ky USB)
     * @return
     */
    public static List<SignMultiFile> appendSignatureAssignOrAssess(HttpServletRequest request,
            List<SignMultiFile> listFileSign, List<SignHashMulti> listSignHash,
            int type, Long userId2, String period, Long orgId, String comment, int signType) {
        
//        System.out.println("___dinh chu ky");
        String sessionId = CommonControler.getSessionIdFromRequest(request);
        // Lay doi tuong ky trong session
        HttpSession session = HttpSessionCollector.find(sessionId);
        List<SignMultiFile> listSignMultiFile = (List<SignMultiFile>) session.getAttribute("signObject");
//        System.out.println("listSignMultiFile co rong khong: " + CommonUtils.isEmpty(listSignMultiFile));
//        System.out.println("listSignHash co rong khong: " + CommonUtils.isEmpty(listSignHash));
        Boolean appendSignSuccess = false;
        if (!CommonUtils.isEmpty(listSignHash)) {
//            System.out.println("___list file sign null hoac rong");
            //Neu listFileSign client gui len la null thi day la buoc so sanh doi tuong ky luu trong session voi doi tuong client gui len
            List<SignMultiFile> listResult = new ArrayList<>();
            if (CommonUtils.isEmpty(sessionId)) {
                SignMultiFile itemResult = new SignMultiFile();
                itemResult.setCodeErr(15811100L);
                itemResult.setMssErr("Loi id null");
                listResult.add(itemResult);
                LOGGER.error("appendSignatureAssignOrAssess - session id null hoac rong");
                return listResult;
            }
            if (CommonUtils.isEmpty(listSignMultiFile) || CommonUtils.isEmpty(listSignHash)) {
                SignMultiFile itemResult = new SignMultiFile();
                itemResult.setCodeErr(15811101L);
                itemResult.setMssErr("Loi khong co chu ky");
                listResult.add(itemResult);
                LOGGER.error("appendSignatureAssignOrAssess - Danh sach doi tuong ky USB nul hoac rong"
                        + " hoac Danh sach chu ky null hoac rong");
                // Log giao dich ky vao database
                return listResult;
            }
            try {
                DigitalSign usbSign;
//                System.out.println("___bat dau ky");
                for (SignMultiFile signMultiFile : listSignMultiFile) {
                    usbSign = signMultiFile.getSoftSign();
                    boolean signSuccess;
                    // Duyet danh sach chu ky
                    for (SignHashMulti signHash : listSignHash) {
                        if (signMultiFile.getId().equals(signHash.getId())) {
                            // Khoi tao doi tuong ket qua ky
                            // Thuc hien ky
                            signSuccess = usbSign.appendSignature(signHash.getSignature());
                            signMultiFile.setCodeErr(usbSign.getErrorCode() == null
                                    ? null : usbSign.getErrorCode());
                            if (signSuccess) {
                                appendSignSuccess = true;
                                //tra ve ma loi thanh cong
                                signMultiFile.setMssErr("Ky thanh cong");
                                LOGGER.info("appendSignatureAssignOrAssess - index: "
                                        + signMultiFile.getId() + " - Ky thanh cong");
                            } else {
                                appendSignSuccess = false;
//                                System.out.println("___ky that bai");
                                signMultiFile.setMssErr("Loi dinh chu ky vao file");
                                LOGGER.error("appendSignatureAssignOrAssess - index: "
                                        + signMultiFile.getId() + " - Ky that bai");
                            }
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                appendSignSuccess = false;
                LOGGER.error("appendSignatureAssignOrAssess - Exception:", ex);
            }
//            System.out.println("ket thuc append");
        } 
        //xu ly update database sau khi day danh sach cong viec len
        String keyStore = "task_export";
        String storage;
        try {
            storage = FunctionCommon.getConfigFile(keyStore);
        } catch (IOException ex) {
            LOGGER.error("appendSignatureAssignOrAssess - Exception! Duong dan storage khong ton tai", ex);
            List<SignMultiFile> listResult = new ArrayList<>();
            return listResult;
        }
        //Neu listFileSign gui len khac null thi day la buoc update DB
        List<SignMultiFile> listResult = new ArrayList<>();
        if ((listFileSign != null && listFileSign.size() > 0
                && (listSignHash == null || listSignHash.size() <= 0))
                || (listFileSign != null && listFileSign.size() > 0
                && listSignHash != null && listSignHash.size() >= 0
                && appendSignSuccess)) {
            for (SignMultiFile fileSign : listFileSign) {
                String filePath = fileSign.getFilePath();
                String fileName = fileSign.getFileSign();
                List<EntityTask> listTask;
                listTask = fileSign.getListTask();
                // Duong dan tuyet doi cua file goc
                if(listTask!=null && listTask.size() > 0 ){
                    String originFilePath = storage + File.separator + filePath;
                    originFilePath = FileUtils.getSafePath(originFilePath);
                    // Lay ra file truoc khi ky
                    File file = new File(originFilePath);
                    if (!file.exists()) {
                        LOGGER.error("appendSignatureAssignOrAssess - File ky khong ton tai" + originFilePath);
                        return listResult;
                    }
                    // Duong dan file da ky
                    String signedFilePath = FileUtils.generatePathForSignedFile(
                            originFilePath, String.valueOf(userId2));
                    String relativePath = FileUtils.getRelativePath(filePath, signedFilePath);
                    Boolean isSuccess = SignUtils.insertRecordAfterSignSuccess(
                            type, userId2, fileName, relativePath, listTask, period,
                            orgId, comment, signType, keyStore);
                    if (!isSuccess) {
                        LOGGER.error("appendSignatureAssignOrAssess - Error! Update vao DataBase khong thanh cong");
                        fileSign.setCodeErr(CODE_UPDATE_DATABASE_FAILED);
                        fileSign.setMssErr(MESS_UPDATE_DATABASE_FAILED);
                        listResult.add(fileSign);
                    }
                }
            }
            if(listResult.size() > 0){
                return listResult;
            }
        }else{
            SignMultiFile fileSign = new SignMultiFile();
            fileSign.setCodeErr(CODE_UPDATE_DATABASE_FAILED);
            fileSign.setMssErr(MESS_UPDATE_DATABASE_FAILED);
            listResult.add(fileSign);
        }
        return listSignMultiFile;
    }

    /**
     * datnv5: kiem tra van ban neu la user dang trong qua trinh xu ly thi co the ky lai
     * @param userId
     * @param textId
     * @return 
     */
    private static boolean isUserInDuringSing(Long userId, Long textId) {
        TextSearchDAO textSearchDAO = new TextSearchDAO();
        return textSearchDAO.isUserConfigAndDuringProcess(userId, textId);
    }
    
    /**
     * @author TungHD
     * hashListFileBrief
     * Thuc hien hash file theo danh sach file cho man ho so
     * @param user1
     * @param user2
     * @param publicKey
     * @param listAttachment
     * @param certificate
     * @param comment
     * @param deviceName
     * @param signType
     * @param mapTextProcessId
     * @param lstMarkInfo
     * @param lstMark
     * @return
     * @throws Throwable
     */
    public static List<SignMultiFile> hashListFileBrief(EntityUser user1,
            Vof2_EntityUser user2, String publicKey, List<EntityAttach> listAttachment,
            String certificate, String comment, String deviceName, String signType,
            Map<Long, Long> mapTextProcessId, List<EntityMarkInfo> lstMarkInfo, List<EntityMarkInfo> lstMark) throws Throwable{

        // Tao bien luu thoi gian bat dau hash file
        Date startTime;
        List<SignMultiFile> listSignFile = new ArrayList<SignMultiFile>();
        //Kiem tra user tren he thong 1
        if (user2 == null || user2.getUserId() == null) {
            LOGGER.error("getHashFile - user1 null hoac userId1 null");
            listSignFile = null;
            return listSignFile;
        }
        Long userId2 = (user2 != null) ? user2.getUserId() : -91581L;
        Long userId1 = (user1 != null) ? user1.getUserId() : -91581L;
        String loginName = (user2 != null) ? user2.getStrCardNumber() : "";
        SignerInfo signerInfo = getSignerInfo(user2);
        if (signerInfo == null) {
            LOGGER.error("getHashFile - SignerInfo null");
            listSignFile = null;
            return listSignFile;
        }
        if (CommonUtils.isEmpty(listAttachment)) {
            listSignFile = null;
            return listSignFile;
        }
        // Lay duong dan chung thu
        String certPath = null;
        if (certificate != null && certificate.trim().length() > 0) {
        } else {
            P12CertDAO p12CertDAO = new P12CertDAO();
            LOGGER.error("========publicKey:" + publicKey);
            EntityP12Cert p12Cert = p12CertDAO.getP12CertificateOfUser(userId1,
                    userId2, publicKey);
            if (p12Cert != null) {
                certPath = p12Cert.getCrtPath();
            }
        }

        try {
            DigitalSign digitalSign = null;
            // Lay ra storage ma server duoc ghi
            String newStorage = FunctionCommon.getStorageConfigFile("storageName");
//            LOGGER.error("hashListFile - New storage: " + newStorage);

            // Lay ra thu muc tuong ung voi storage
            String newStorageFolder = FunctionCommon.getStorageConfigFile(newStorage);
//            LOGGER.error("hashListFile - New storage folder: " + newStorageFolder);

            SignMultiFile itemFileSign;
            Long textId;
            Long fileId;
            String oldStorage;
            String oldStorageFolder;
            String originFilePath;
            File file;
            String newFilePath;
            String signedFilePath;
            UsbSign usbSign;
            SoftSign softSign;
            Long codeErr;
            //Tunghd add
            Long orgMarkId = null;
            Long typeFile = null;
            Long signLocate = null;
            Long groupTypeImage = null;
            Long documentMarkId = null;
            Long briefId = null;
            EntityActionLogMobile log;
            List<EntityActionLogMobile> listLog;
            String functionName = SIGN_BY_SOFT_CERTIFICATE_2
                    + "_" + listAttachment.size() + "FILE";
            Date hashingStartTime, hashingEndTime;
            Map<String, List<Date>> mapSigningStep;
            List<Date> listReadingTime, listHashingTime;
            // Datdc add
            Boolean isMarkMulti = false;
            // Duyet danh sach file can ky            
            for (EntityAttach attachment : listAttachment) {
             // Lay ra id van ban cua file ky
                textId = attachment.getBriefDocumentId();
                fileId = attachment.getAttachId();
                //Tunghd add
                for(EntityMarkInfo emi : lstMarkInfo){
                    if(textId != null && textId.equals(emi.getTextId()) && fileId.equals(emi.getFileId())){
                        orgMarkId = emi.getOrgMarkId();
                        typeFile = emi.getTypeFile();
                        signLocate = emi.getSignLocate();
                        groupTypeImage = emi.getGroupTypeImage();
                        documentMarkId = emi.getDocumentId();
                        briefId = emi.getBriefId();
                        // Datdc add them 
                        isMarkMulti = emi.getIsMarkMulti();
                    }
                }
                // 201812-Pitagon: add
                String path = null;
                List<EntityMarkInfo> lstConfigImage = new ArrayList<>();
                Long textProcessId = mapTextProcessId != null && !mapTextProcessId.isEmpty() ? mapTextProcessId.get(attachment.getBriefDocumentId()) : null;
                if (SignUtils.MARK_TYPE.equals(signType)) {
                    ImageOrgDAO imageDao = new ImageOrgDAO();
                    List<EntityMarkInfo> listImagePath = imageDao.getImageOrgMarkPathBrief(textProcessId, 2L);
                    if(!CommonUtils.isEmpty(listImagePath)){
                        path = listImagePath.get(0).getContextPath();
                        lstConfigImage = imageDao.getImageConfig(listImagePath.get(0), 2L);
                    } else {
                        LOGGER.error("getHashFile - KHONG CO ANH CON DAU MAC DINH");
                        listSignFile = null;
                        return listSignFile;
                    }
                }
                listLog = new ArrayList<>();
                mapSigningStep = new LinkedHashMap<>();
                startTime = new Date();
                
                
                // Khoi tao doi tuong luu thong tin ky file
                itemFileSign = new SignMultiFile();
                itemFileSign.setOrgMarkId(orgMarkId); 
                itemFileSign.setTypeFile(typeFile); 
                itemFileSign.setSignLocate(signLocate);
                itemFileSign.setGroupTypeImage(groupTypeImage);
                itemFileSign.setDocumentMarkId(documentMarkId);
                itemFileSign.setBriefId(briefId);
                itemFileSign.setIsMarkMulti(isMarkMulti);
                // Gan id van ban
                itemFileSign.setTextId(textId);
                itemFileSign.setId(fileId);
                // Gan y kien ky
                itemFileSign.setStrComment(comment);
                try {
                    // Lay storage cua file
                    // Neu storage null thi gan storage mac dinh
                    oldStorage = attachment.getStorage();
                    if (CommonUtils.isEmpty(oldStorage)) {
                        oldStorage = DEFAULT_STORAGE;
                    }
//                    LOGGER.info("hashListFile - Old storage: " + oldStorage);

                    // Lay thu muc cua storage
                    oldStorageFolder = FunctionCommon.getStorageConfigFile(oldStorage);
//                    LOGGER.info("hashListFile - Old storage folder: " + oldStorageFolder);

                    // Duong dan tuyet doi cua file goc
                    originFilePath = oldStorageFolder + attachment.getPath();
//                    LOGGER.info("hashListFile - Origin file path: " + originFilePath);

                    // Kiem tra xem file can ky co ton tai khong
                    file = new File(originFilePath);
                    if (!file.exists()) {
                        itemFileSign.setCodeErr(Constant.DIGITAL_SIGN_RESULT_CODE.ERR_CODE_FILE_NOT_EXIST);
                        itemFileSign.setMssErr(Constant.DIGITAL_SIGN_RESULT_CODE.ERR_MESS_FILE_NOT_EXIST);
                        listSignFile.add(itemFileSign);
//                        LOGGER.error("hashListFile - Loi file khong ton tai - " + originFilePath);
                        // Log giao dich ky vao database
                        log = new EntityActionLogMobile(userId1, loginName, startTime,
                                new Date(), functionName, HASH_FILE, 
                                ERROR_DESCRIPTION_FOR_SIGNING_BY_SOFT_CERTIFICATE,
                                textId, itemFileSign.getCodeErr(), itemFileSign.getMssErr() + " - " + fileId,
                                deviceName);
                        String strContenLogAdressAndPort = FunctionCommon.IPPORTSERVICE 
                                + ", Storage_read: " + oldStorage + ", Storage_write: " + newStorage ;
                        log.setContent(strContenLogAdressAndPort);
                        listLog.add(log);
                        LogUtils.insertActionLogMobile(userId2, listLog);
                        continue;
                    }

                    // Duong dan tuyet doi moi cua file ky
                    newFilePath = newStorageFolder + attachment.getPath();
//                    LOGGER.info("hashListFile - New file path: " + newFilePath);

                    itemFileSign.setFileRoot(newFilePath);
                    itemFileSign.setStorageFileSign(newStorage);
                    // Duong dan file da ky
                    signedFilePath = FileUtils.generatePathForSignedFile(newFilePath, String.valueOf(userId1));
//                    LOGGER.info("hashListFile - Signed file path: " + signedFilePath);
                    itemFileSign.setFileSign(signedFilePath.substring(newStorageFolder.length()));
                    List<String> listHashInfo = null;
                    
                    String strLocation = "";
//                    String strLocation = textSignDao.getLocationSignByUser(textId, userId1, userId2,
//                                user2.getAdOrgName());
                    JSONObject jsonObj = new JSONObject();
                    Gson configGson = new Gson();
                    if (SignUtils.MARK_TYPE.equals(signType)) {
                        //Datdc Neu dong mac dinh
                        if (!isMarkMulti) {
                            jsonObj.put("imageConfirm", path);
                            if(!CommonUtils.isEmpty(lstConfigImage)){
                                jsonObj.put("lstConfigImage", configGson.toJson(lstConfigImage.get(0)));
                            }
                        }
                        jsonObj.put("markerEmailConfirm", user2.getStrEmail());
                        jsonObj.put("markedConfirm", "1");
                        jsonObj.put("groupType", itemFileSign.getGroupTypeImage());
                    } else {
                        jsonObj.put("location", strLocation);
                    }
                    // Datdc Lay lst toa do tuong ung voi file
                    List<EntityMarkInfo> lstMarkFile = new ArrayList<EntityMarkInfo>();
                    if (!CommonUtils.isEmpty(lstMark)) {
                        for (EntityMarkInfo entity: lstMark) {
                            if (textId.equals(entity.getDocInBrief()) && fileId.equals(entity.getFileId())) {
                                lstMarkFile.add(entity);
                            }
                        } 
                        itemFileSign.setMarkFileInfo(lstMarkFile);

                    }
                    // Datdc add
                    if (!CommonUtils.isEmpty(lstMarkFile)) {
                        Gson gson = new Gson();
                        jsonObj.put("lstMark", gson.toJson(lstMarkFile));

                    }
                    strLocation = jsonObj.toString();
                   
                    // Neu certificate khac null va rong -> Ky bang USB

                    //ghi logs thoi gian bat dau ky
//                    Date dateStartHash = new Date();
//                    long diff = Math.abs(startTime.getTime() - dateStartHash.getTime());
//                    System.out.println("=====datnv5 Hash Start: " + diff);
                    if (!CommonUtils.isEmpty(certificate)) {
                        usbSign = new UsbSign(signerInfo, originFilePath, signedFilePath, true);
                        listHashInfo = usbSign.getDigest(certificate, comment, strLocation);
                        digitalSign = usbSign;
                    } // Neu certPath khac null va rong -> Ky bang chung thu mem
                    else if (!CommonUtils.isEmpty(certPath)) {
                        // Khoi tao doi tuong ky mem
                        softSign = new SoftSign(signerInfo, originFilePath, signedFilePath, true);
//                        Date NewSoft = new Date();
//                        long diff11111 = Math.abs(dateStartHash.getTime() - NewSoft.getTime());
//                        System.out.println("=====datnv5 NewSoft: " + diff11111);
                        listHashInfo = softSign.getDigest(certPath, comment, strLocation);
                        digitalSign = softSign;
                    }

//                    Date dateEndHash = new Date();
//                    long diff1 = Math.abs(dateStartHash.getTime() - dateEndHash.getTime());
//                    System.out.println("=====datnv5 Hash End: " + diff1);
                    // Gan ma loi hash file
                    // 17:09 05/11/2016 - ThangHT6
                    // Fix loi logic do Sonar quet
//                    itemFileSign.setCodeErr(digitalSign.getErrorCode());
                    codeErr = digitalSign == null ? null : digitalSign.getErrorCode();
                    itemFileSign.setCodeErr(codeErr);
                    // Neu hash file thanh cong
                    if (digitalSign != null && digitalSign.getErrorCode().equals(Constant.DIGITAL_SIGN_RESULT_CODE.ERR_CODE_INIT_SUCSESS)) {
                        // Neu khong co chuoi hash thi thuc hien gan lai ma loi
                        // la hash file that bai
                        if (CommonUtils.isEmpty(listHashInfo)) {
                            itemFileSign.setCodeErr(Constant.DIGITAL_SIGN_RESULT_CODE.ERR_CODE_HASH_NOTSUCSESS);
                            itemFileSign.setMssErr(Constant.DIGITAL_SIGN_RESULT_CODE.ERR_MESS_HASH_NOTSUCSESS);
                        } // Neu co chuoi hash
                        else {
                            if (listHashInfo != null) {
                                itemFileSign.setHash(listHashInfo.get(0));
                            }
                            itemFileSign.setSoftSign(digitalSign);
                            itemFileSign.setMssErr("Hash file thanh cong");
                        }
                    }
                } catch (Exception ex) {
                    itemFileSign.setCodeErr(Constant.DIGITAL_SIGN_RESULT_CODE.ERR_CODE_HASH_NOTSUCSESS);
                    itemFileSign.setMssErr(Constant.DIGITAL_SIGN_RESULT_CODE.ERR_MESS_HASH_NOTSUCSESS);
                    LOGGER.error("getHashFile - textId: " + textId  + " - " + fileId
                            + " - Exception!", ex);
                }
                listSignFile.add(itemFileSign);
                // Log giao dich ky vao database
                hashingStartTime = digitalSign != null && digitalSign.getHashingStartTime() != null ?
                        digitalSign.getHashingStartTime() : new Date();
                hashingEndTime = digitalSign != null && digitalSign.getHashingEndTime() != null ?
                        digitalSign.getHashingEndTime() : new Date();
                // Tao danh sach thoi gian bat dau, thoi gian ket thuc doc file
                listReadingTime = new ArrayList<>();
                listReadingTime.add(startTime);
                listReadingTime.add(hashingStartTime);
                // Tao danh sach thoi gian bat dau, thoi gian ket thuc hash file
                listHashingTime = new ArrayList<>();
                listHashingTime.add(hashingStartTime);
                listHashingTime.add(hashingEndTime);
                mapSigningStep.put(READ_FILE, listReadingTime);
                mapSigningStep.put(HASH_FILE, listHashingTime);
                listLog = EntityActionLogMobile.createLog(userId2, loginName,
                        functionName, mapSigningStep,
                        ERROR_DESCRIPTION_FOR_SIGNING_BY_SOFT_CERTIFICATE,
                        textId, itemFileSign.getCodeErr(), itemFileSign.getMssErr() + " - " + fileId,
                        deviceName);
                LogUtils.insertActionLogMobile(userId2, listLog);
            }
        } catch (IOException ex) {
            //do stuff with exception
            LOGGER.error("getHashFile - Exception!", ex);
        }
//        Date endDate = new Date();
//        long diff11 = Math.abs(endDate.getTime() - startTime.getTime());
//        System.out.println("=====datnv5==== End: " + diff11);
        return listSignFile;
    }
    /**
     * @author TungHD
     * appendSignatureIntoListFileBrief
     * insert thong tin sau khi dong dau man ho so
     * @param request
     * @param listSignHash
     * @param strListFileSign
     * @param deviceName
     * @return
     */
    public static List<SignHashMulti> appendSignatureIntoListFileBrief(HttpServletRequest request,
            List<SignHashMulti> listSignHash, List<EntityFileAttachment> strListFileSign,
            String deviceName) {

        Date startTime = new Date();
        List<SignHashMulti> listResult = new ArrayList<>();
        String sessionId = CommonControler.getSessionIdFromRequest(request);
        if (CommonUtils.isEmpty(sessionId)) {
            SignHashMulti itemResult = new SignHashMulti();
            itemResult.setError("15811100");
            itemResult.setMess("Loi id null");
            listResult.add(itemResult);
            LOGGER.error("appendSignatureIntoListFileBrief - session id null hoac rong");
            return listResult;
        }

        // Lay doi tuong user trong session
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        Long userId1 = user1 == null ? 0L : user1.getUserId();
        String loginName = user2 == null ? "" : user2.getStrCardNumber();
        Long user2Id = (user2 != null && user2.getUserId()!=null)?user2.getUserId():0L;
        
        // Lay doi tuong ky trong session
        HttpSession session = HttpSessionCollector.find(sessionId);
        List<SignMultiFile> listSignMultiFile = (List<SignMultiFile>) session.getAttribute("signObject");
        // Lay thong tin signType
        String signType = (String) session.getAttribute(ConstantsFieldParams.SIGN_TYPE);
        // 201812-Pitagon: add
        Map<Long, Long> mapTextProcessId = (Map<Long, Long>) session.getAttribute(ConstantsFieldParams.TEXT_PROCESS_ID);

        EntityActionLogMobile log;
        List<EntityActionLogMobile> listLog;
        String functionName = SIGN_BY_SOFT_CERTIFICATE_2;
        if (CommonUtils.isEmpty(listSignMultiFile) || CommonUtils.isEmpty(listSignHash)) {
            SignHashMulti itemResult = new SignHashMulti();
            itemResult.setError("15811101");
            itemResult.setMess("Loi khong co chu ky");
            listResult.add(itemResult);
            LOGGER.error("appendSignatureIntoListFile - Danh sach doi tuong ky mem nul hoac rong"
                    + " hoac Danh sach chu ky null hoac rong");
            // Log giao dich ky vao database
            functionName += "_0FILE";
            log = new EntityActionLogMobile(user2Id, loginName, startTime,
                    new Date(), functionName, APPEND_SIGNATURE,
                    ERROR_DESCRIPTION_FOR_SIGNING_BY_SOFT_CERTIFICATE,
                    0L, 15811101L, "Loi khong co chu ky", deviceName);
            listLog = new ArrayList<>();
            listLog.add(log);
            LogUtils.insertActionLogMobile(user2Id, listLog);
            return listResult;
        }

        try {
            Map<Long, Calendar> mapTimeSign = new LinkedHashMap<Long, Calendar>();
            Map<Long, List<Object[]>> mapTextAttach = new LinkedHashMap<Long, List<Object[]>>();
            List<Object[]> arrayNewPathSignFile = null;
            Object[] objArr = null;
            
            List<Long> listIdTextSign = new ArrayList<>();
//            List<Long> listIdAttachFileSign = new ArrayList<>();
//            List<String> listAttachFileSign = new ArrayList<>();
//            List<Calendar> listIdTextSignDate = new ArrayList<>();
            DigitalSign softSign;
            Date writingStartTime, writingEndTime;
            List<Date> listAppendingTime, listWritingTime;
            Map<String, List<Date>> mapSigningStep;
            // Duyet danh sach file ky
            functionName += "_" + listSignHash.size() + "FILE";
            for (SignMultiFile signMultiFile : listSignMultiFile) {
                startTime = new Date();
                mapSigningStep = new LinkedHashMap<>();
                softSign = signMultiFile.getSoftSign();
                boolean signSuccess;
                SignHashMulti itemResult;
                // Duyet danh sach chu ky
                for (SignHashMulti signHash : listSignHash) {
                    // Neu file ky va chu ky cua cung 1 file van ban
                    if (signMultiFile.getId().equals(signHash.getId())) {

                        // Khoi tao doi tuong ket qua ky
                        itemResult = new SignHashMulti();
                        itemResult.setId(signMultiFile.getId());
                        itemResult.setTextId(signMultiFile.getTextId());
                        // Thuc hien ky
                        signSuccess = softSign.appendSignature(signHash.getSignature());
                        itemResult.setError(softSign.getErrorCode() == null
                                ? null : softSign.getErrorCode().toString());

                        // Neu ky thanh cong
                        if (signSuccess) {
                            itemResult.setMess("Ky thanh cong");
                            itemResult.setIdFileAttachSign(signMultiFile.getIdFileSign());
                            itemResult.setStorageFileSign(signMultiFile.getStorageFileSign());
                            // Them file van ban ky thanh cong vao danh sach
                            
                            if (!listIdTextSign.contains(signMultiFile.getTextId())) {
                                listIdTextSign.add(signMultiFile.getTextId());
                            }                            
                            mapTimeSign.put(signMultiFile.getTextId(), softSign.getCalendarSign());
                            objArr = new Object[] {signMultiFile.getId(), signMultiFile.getFileSign()};
                            arrayNewPathSignFile = mapTextAttach.get(signMultiFile.getTextId());
                            if (arrayNewPathSignFile == null) {
                                arrayNewPathSignFile = new ArrayList<>();
                            }
                            arrayNewPathSignFile.add(objArr);
                            mapTextAttach.put(signMultiFile.getTextId(), arrayNewPathSignFile);
                            
//                            listIdTextSignDate.add(softSign.getCalendarSign());
//                            listAttachFileSign.add(signMultiFile.getFileSign());
//                            listIdAttachFileSign.add(signMultiFile.getIdFileSign());
                            LOGGER.info("appendSignatureIntoListFileBrief - textId: "
                                    + signMultiFile.getId() + " - Ky thanh cong");
                        } // Neu ky that bai
                        else {
                            itemResult.setMess("Loi dinh chu ky vao file");
                            LOGGER.error("appendSignatureIntoListFileBrief - textId: "
                                    + signMultiFile.getId() + " - Ky that bai");
                        }
                        // Them ket qua ky van ban vao danh sach
                        listResult.add(itemResult);
                        
                        // Ghi log vao database
                        writingStartTime = softSign.getWritingStartTime() != null
                                ? softSign.getWritingStartTime() : new Date();
                        writingEndTime = softSign.getWritingEndTime() != null
                                ? softSign.getWritingEndTime() : new Date();
                        // Tao danh sach thoi gian bat dau, thoi gian ket thuc dinh chu ky vao file
                        listAppendingTime = new ArrayList<>();
                        listAppendingTime.add(startTime);
                        listAppendingTime.add(writingStartTime);
                        // Tao danh sach thoi gian bat dau, thoi gian ket thuc ghi file
                        listWritingTime = new ArrayList<>();
                        listWritingTime.add(writingStartTime);
                        listWritingTime.add(writingEndTime);
                        mapSigningStep.put(APPEND_SIGNATURE, listAppendingTime);
                        mapSigningStep.put(WRITE_FILE, listWritingTime);
                        listLog = EntityActionLogMobile.createLog(user2Id,
                                loginName, functionName, mapSigningStep,
                                ERROR_DESCRIPTION_FOR_SIGNING_BY_SOFT_CERTIFICATE,
                                signMultiFile.getId(), softSign.getErrorCode() == null ?
                                        0L : softSign.getErrorCode(), itemResult.getMess(), deviceName);
                        LogUtils.insertActionLogMobile(user2Id, listLog);
                        break;
                    }
                }
            }
            
            
            startTime = new Date();
            // Lay thong tin user 2
            Long v2UserId = null;
            Vof2_EntityUser v2User = userGroup.getVof2_ItemEntityUser();
            if (v2User != null) {
                v2UserId = v2User.getUserId();
            }
            //---------------------HIendv2---------------------
            Vof2_EntityUser entityUserVof2 = userGroup.getVof2_ItemEntityUser();
            //-------------------End Hiendv----------------------------
            String keySaveFileTmpText = FunctionCommon.getPropertiesValue("storageName");
            if (listSignMultiFile.size() > 0 && signType != null) {
                Long textId = listIdTextSign.get(0);
                switch (signType) {
                    case MARK_TYPE:
                        TextDAO textDao = new TextDAO();
                        TextSignDAO tsDao = new TextSignDAO();
                        TextSignDAO markAttachHistoryDao = new TextSignDAO();
                        for (int i = 0; i < listIdTextSign.size(); i++) {
                            Long textProcessId = mapTextProcessId.get(listIdTextSign.get(i));
                            Long objId = listIdTextSign.get(i);
                            Long orgMarkId = listSignMultiFile.get(i).getOrgMarkId();
                            Long groupType = listSignMultiFile.get(i).getGroupTypeImage();;
                            Long briefId = listSignMultiFile.get(i).getBriefId();
                            Boolean isMarkMulti = listSignMultiFile.get(i).getIsMarkMulti();
                            int result = 0;
                            Calendar dateSign = mapTimeSign.get(listIdTextSign.get(i));
                            Date signDate = dateSign.getTime();
                            result = textDao.approveMarkBrief(listIdTextSign.get(i), textProcessId, listSignMultiFile.get(0).getStrComment(), signDate);
                            // Datdc start update list dau vi tri
                            if (isMarkMulti) {
                                // Update MarkAttachHistoryBriefMulti
                                for(SignMultiFile indexFile : listSignMultiFile){
                                    if (!CommonUtils.isEmpty(indexFile.getMarkFileInfo())) {
                                        markAttachHistoryDao.insertMarkAttachHistoryBriefMulti(keySaveFileTmpText, mapTextAttach.get(listIdTextSign.get(i)), 
                                                v2UserId, indexFile.getMarkFileInfo());
                                        break;
                                    }
                                }
                                tsDao.updatePathNewBrief(v2User, keySaveFileTmpText, mapTextAttach.get(listIdTextSign.get(i)));
                                // Update Path New
                                for(SignMultiFile indexFile : listSignMultiFile){
                                    if (!CommonUtils.isEmpty(indexFile.getMarkFileInfo())) {
                                        if (textDao.updateMarkLocateMultiMark(indexFile.getMarkFileInfo(), v2UserId) == 0) {
                                            LOGGER.error("Insert Mark du lieu vao bang MARK_LOCATE khong thanh cong");
                                        }
                                    }
                                }
                            } else {
                                //Luu lai path cu truoc khi update path moi
                                markAttachHistoryDao.insertMarkAttachHistoryBrief(keySaveFileTmpText, mapTextAttach.get(listIdTextSign.get(i)), 
                                        v2UserId, briefId, groupType, orgMarkId, objId);
                                //End
                                tsDao.updatePathNewBrief(v2User, keySaveFileTmpText, mapTextAttach.get(listIdTextSign.get(i)));
                                if (result != 0) {
                                    for (SignMultiFile indexFile : listSignMultiFile) {
                                        if (!textDao.updateMarkLocate(v2UserId,
                                                indexFile)) {
                                            LOGGER.error("Insert du lieu vao bang MARK_LOCATE khong thanh cong");
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    default:
//                        LOGGER.info(" signType khong hop le: " + signType);
                        break;
                }

                // Ghi log xu ly cap nhat du lieu sau ky
                log = new EntityActionLogMobile(entityUserVof2.getUserId(), loginName,
                        startTime, new Date(), functionName, UDPATE_DATA_AFFTER_SIGNATURE,
                        ERROR_DESCRIPTION_FOR_SIGNING_BY_SOFT_CERTIFICATE, textId,
                        v2UserId, "Update du lieu sau ky thanh cong", deviceName);
                listLog = new ArrayList<>();
                listLog.add(log);
                LogUtils.insertActionLogMobile(entityUserVof2.getUserId(), listLog);
            }
        } catch (Exception ex) {
            LOGGER.error("appendSignature - Exception:", ex);
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            LOGGER.error("appendSignature - Exception:", e);
            e.printStackTrace();
        }
        return listResult;
    }
    
    /**
     * @author TungHD
     * hashListFileDoc
     * Thuc hien haslist file theo danh sach file man danh sach van ban
     * @param user1
     * @param user2
     * @param publicKey
     * @param listAttachment
     * @param certificate
     * @param comment
     * @param deviceName
     * @param signType
     * @param mapTextProcessId
     * @param lstMarkInfo
     * @param lstMark
     * @return
     * @throws Throwable
     */
    public static List<SignMultiFile> hashListFileDoc(EntityUser user1,
            Vof2_EntityUser user2, String publicKey, List<EntityAttach> listAttachment,
            String certificate, String comment, String deviceName, String signType,
            Map<Long, Long> mapTextProcessId, List<EntityMarkInfo> lstMarkInfo, List<EntityMarkInfo> lstMark) throws Throwable{

        // Tao bien luu thoi gian bat dau hash file
        Date startTime;
        List<SignMultiFile> listSignFile = new ArrayList<SignMultiFile>();
        //Kiem tra user tren he thong 1
        if (user2 == null || user2.getUserId() == null) {
            LOGGER.error("getHashFile - user1 null hoac userId1 null");
            listSignFile = null;
            return listSignFile;
        }
        Long userId2 = (user2 != null) ? user2.getUserId() : -91581L;
        Long userId1 = (user1 != null) ? user1.getUserId() : -91581L;
        String loginName = (user2 != null) ? user2.getStrCardNumber() : "";
        SignerInfo signerInfo = getSignerInfo(user2);
        if (signerInfo == null) {
            LOGGER.error("getHashFile - SignerInfo null");
            listSignFile = null;
            return listSignFile;
        }
        if (CommonUtils.isEmpty(listAttachment)) {
            listSignFile = null;
            return listSignFile;
        }
        // Lay duong dan chung thu
        String certPath = null;
        if (certificate != null && certificate.trim().length() > 0) {
        } else {
            P12CertDAO p12CertDAO = new P12CertDAO();
            LOGGER.error("========publicKey:" + publicKey);
            EntityP12Cert p12Cert = p12CertDAO.getP12CertificateOfUser(userId1,
                    userId2, publicKey);
            if (p12Cert != null) {
                certPath = p12Cert.getCrtPath();
            }
        }

        try {
            DigitalSign digitalSign = null;
            // Lay ra storage ma server duoc ghi
            String newStorage = FunctionCommon.getStorageConfigFile("storageName");
//            LOGGER.error("hashListFile - New storage: " + newStorage);

            // Lay ra thu muc tuong ung voi storage
            String newStorageFolder = FunctionCommon.getStorageConfigFile(newStorage);
//            LOGGER.error("hashListFile - New storage folder: " + newStorageFolder);

            SignMultiFile itemFileSign;
            Long textId;
            Long fileId;
            String oldStorage;
            String oldStorageFolder;
            String originFilePath;
            File file;
            String newFilePath;
            String signedFilePath;
            TextSignDAO textSignDao = new TextSignDAO();
            UsbSign usbSign;
            SoftSign softSign;
            Long codeErr;
            //Tunghd add
            Long orgMarkId = null;
            Long typeFile = null;
            Long signLocate = null;
            Long groupTypeImage = null;
            Boolean isMarkMulti = false;
            EntityActionLogMobile log;
            List<EntityActionLogMobile> listLog;
            String functionName = SIGN_BY_SOFT_CERTIFICATE_2
                    + "_" + listAttachment.size() + "FILE";
            Date hashingStartTime, hashingEndTime;
            Map<String, List<Date>> mapSigningStep;
            List<Date> listReadingTime, listHashingTime;
            // Duyet danh sach file can ky            
            for (EntityAttach attachment : listAttachment) {
             // Lay ra id van ban cua file ky
                textId = attachment.getDocId();
                fileId = attachment.getAttachId();
                //Tunghd add
                for(EntityMarkInfo emi : lstMarkInfo){
                    if(textId != null && textId.equals(emi.getTextId()) && fileId.equals(emi.getFileId())){
                        orgMarkId = emi.getOrgMarkId();
                        typeFile = emi.getTypeFile();
                        signLocate = emi.getSignLocate();
                        groupTypeImage = emi.getGroupTypeImage();
                        // Datdc add param check dong dau
                        isMarkMulti = emi.getIsMarkMulti();
                    }
                }
                // 201812-Pitagon: add
                String path = null;
                List<EntityMarkInfo> lstConfigImage = new ArrayList<>();
                Long textProcessId = mapTextProcessId != null && !mapTextProcessId.isEmpty() ? mapTextProcessId.get(attachment.getDocId()) : null;
                if (SignUtils.MARK_TYPE.equals(signType)) {
                    ImageOrgDAO imageDao = new ImageOrgDAO();
                    List<EntityMarkInfo> listImagePath = imageDao.getImageOrgMarkPath(textProcessId, 2L);
                    if(!CommonUtils.isEmpty(listImagePath)){
                        path = listImagePath.get(0).getContextPath();
                        lstConfigImage = imageDao.getImageConfig(listImagePath.get(0), 2L);
                    } else {
                        LOGGER.error("getHashFile - KHONG CO ANH CON DAU MAC DINH");
                        listSignFile = null;
                        return listSignFile;
                    }
                }
                listLog = new ArrayList<>();
                mapSigningStep = new LinkedHashMap<>();
                startTime = new Date();
                // Khoi tao doi tuong luu thong tin ky file
                itemFileSign = new SignMultiFile();
                // Check neu ko co group type thi vao day
                // Datdc end Lay lst toa do tuong ung voi file
                if (!CommonUtils.isEmpty(lstMark)) {
                    groupTypeImage = lstMark.get(0).getGroupType();
                }
                // Datdc Lay lst toa do tuong ung voi file
                List<EntityMarkInfo> lstMarkFile = new ArrayList<EntityMarkInfo>();
                if (!CommonUtils.isEmpty(lstMark)) {
                    for (EntityMarkInfo entity: lstMark) {
                        if (textId.equals(entity.getObjectId()) && fileId.equals(entity.getFileId())) {
                            lstMarkFile.add(entity);
                        }
                    } 
                    itemFileSign.setMarkFileInfo(lstMarkFile);

                }
                itemFileSign.setOrgMarkId(orgMarkId); 
                itemFileSign.setTypeFile(typeFile); 
                itemFileSign.setSignLocate(signLocate);
                itemFileSign.setGroupTypeImage(groupTypeImage);
                itemFileSign.setDocumentMarkId(textId);
                // Datdc add
                itemFileSign.setIsMarkMulti(isMarkMulti);
                
                // Gan id van ban
                itemFileSign.setTextId(textId);
                itemFileSign.setId(fileId);
                // Gan y kien ky
                itemFileSign.setStrComment(comment);
                try {
                    // Lay storage cua file
                    // Neu storage null thi gan storage mac dinh
                    oldStorage = attachment.getStorage();
                    if (CommonUtils.isEmpty(oldStorage)) {
                        oldStorage = DEFAULT_STORAGE;
                    }
//                    LOGGER.info("hashListFile - Old storage: " + oldStorage);

                    // Lay thu muc cua storage
                    oldStorageFolder = FunctionCommon.getStorageConfigFile(oldStorage);
//                    LOGGER.info("hashListFile - Old storage folder: " + oldStorageFolder);

                    // Duong dan tuyet doi cua file goc
                    originFilePath = oldStorageFolder + attachment.getPath();
//                    LOGGER.info("hashListFile - Origin file path: " + originFilePath);

                    // Kiem tra xem file can ky co ton tai khong
                    file = new File(originFilePath);
                    if (!file.exists()) {
                        itemFileSign.setCodeErr(Constant.DIGITAL_SIGN_RESULT_CODE.ERR_CODE_FILE_NOT_EXIST);
                        itemFileSign.setMssErr(Constant.DIGITAL_SIGN_RESULT_CODE.ERR_MESS_FILE_NOT_EXIST);
                        listSignFile.add(itemFileSign);
//                        LOGGER.error("hashListFile - Loi file khong ton tai - " + originFilePath);
                        // Log giao dich ky vao database
                        log = new EntityActionLogMobile(userId1, loginName, startTime,
                                new Date(), functionName, HASH_FILE, 
                                ERROR_DESCRIPTION_FOR_SIGNING_BY_SOFT_CERTIFICATE,
                                textId, itemFileSign.getCodeErr(), itemFileSign.getMssErr() + " - " + fileId,
                                deviceName);
                        String strContenLogAdressAndPort = FunctionCommon.IPPORTSERVICE 
                                + ", Storage_read: " + oldStorage + ", Storage_write: " + newStorage ;
                        log.setContent(strContenLogAdressAndPort);
                        listLog.add(log);
                        LogUtils.insertActionLogMobile(userId2, listLog);
                        continue;
                    }

                    // Duong dan tuyet doi moi cua file ky
                    newFilePath = newStorageFolder + attachment.getPath();
//                    LOGGER.info("hashListFile - New file path: " + newFilePath);

                    itemFileSign.setFileRoot(newFilePath);
                    itemFileSign.setStorageFileSign(newStorage);
                    // Duong dan file da ky
                    signedFilePath = FileUtils.generatePathForSignedFile(newFilePath, String.valueOf(userId1));
//                    LOGGER.info("hashListFile - Signed file path: " + signedFilePath);
                    itemFileSign.setFileSign(signedFilePath.substring(newStorageFolder.length()));
                    List<String> listHashInfo = null;

                    String strLocation = textSignDao.getLocationSignByUser(textId, userId1, userId2,
                                user2.getAdOrgName());
                    JSONObject jsonObj = new JSONObject();
                    Gson configGson = new Gson();
                    if (SignUtils.MARK_TYPE.equals(signType)){
                        // Datdc Neu dong mac dinh
                        if(!isMarkMulti) {
                            jsonObj.put("imageConfirm", path);
                            if(!CommonUtils.isEmpty(lstConfigImage)){
                                jsonObj.put("lstConfigImage", configGson.toJson(lstConfigImage.get(0)));
                            }
                        }
                        jsonObj.put("markerEmailConfirm", user2.getStrEmail());
                        jsonObj.put("markedConfirm", "1");
                        jsonObj.put("groupType", itemFileSign.getGroupTypeImage());
                        
                    } else {
                        jsonObj.put("location", strLocation);
                    }
                    if (!CommonUtils.isEmpty(lstMarkFile)) {
                        Gson gson = new Gson();
                        jsonObj.put("lstMark", gson.toJson(lstMarkFile));

                    }
                    strLocation = jsonObj.toString();
                    // Neu certificate khac null va rong -> Ky bang USB

                    //ghi logs thoi gian bat dau ky
//                    Date dateStartHash = new Date();
//                    long diff = Math.abs(startTime.getTime() - dateStartHash.getTime());
//                    System.out.println("=====datnv5 Hash Start: " + diff);
                    if (!CommonUtils.isEmpty(certificate)) {
                        usbSign = new UsbSign(signerInfo, originFilePath, signedFilePath, true);
                        listHashInfo = usbSign.getDigest(certificate, comment, strLocation);
                        digitalSign = usbSign;
                    } // Neu certPath khac null va rong -> Ky bang chung thu mem
                    else if (!CommonUtils.isEmpty(certPath)) {
                        // Khoi tao doi tuong ky mem
                        softSign = new SoftSign(signerInfo, originFilePath, signedFilePath, true);
//                        Date NewSoft = new Date();
//                        long diff11111 = Math.abs(dateStartHash.getTime() - NewSoft.getTime());
//                        System.out.println("=====datnv5 NewSoft: " + diff11111);
                        listHashInfo = softSign.getDigest(certPath, comment, strLocation);
                        digitalSign = softSign;
                    }

//                    Date dateEndHash = new Date();
//                    long diff1 = Math.abs(dateStartHash.getTime() - dateEndHash.getTime());
//                    System.out.println("=====datnv5 Hash End: " + diff1);
                    // Gan ma loi hash file
                    // 17:09 05/11/2016 - ThangHT6
                    // Fix loi logic do Sonar quet
//                    itemFileSign.setCodeErr(digitalSign.getErrorCode());
                    codeErr = digitalSign == null ? null : digitalSign.getErrorCode();
                    itemFileSign.setCodeErr(codeErr);
                    // Neu hash file thanh cong
                    if (digitalSign != null && digitalSign.getErrorCode().equals(Constant.DIGITAL_SIGN_RESULT_CODE.ERR_CODE_INIT_SUCSESS)) {
                        // Neu khong co chuoi hash thi thuc hien gan lai ma loi
                        // la hash file that bai
                        if (CommonUtils.isEmpty(listHashInfo)) {
                            itemFileSign.setCodeErr(Constant.DIGITAL_SIGN_RESULT_CODE.ERR_CODE_HASH_NOTSUCSESS);
                            itemFileSign.setMssErr(Constant.DIGITAL_SIGN_RESULT_CODE.ERR_MESS_HASH_NOTSUCSESS);
                        } // Neu co chuoi hash
                        else {
                            if (listHashInfo != null) {
                                itemFileSign.setHash(listHashInfo.get(0));
                            }
                            itemFileSign.setSoftSign(digitalSign);
                            itemFileSign.setMssErr("Hash file thanh cong");
                        }
                    }
                } catch (Exception ex) {
                    itemFileSign.setCodeErr(Constant.DIGITAL_SIGN_RESULT_CODE.ERR_CODE_HASH_NOTSUCSESS);
                    itemFileSign.setMssErr(Constant.DIGITAL_SIGN_RESULT_CODE.ERR_MESS_HASH_NOTSUCSESS);
                    LOGGER.error("getHashFileDoc - textId: " + textId  + " - " + fileId
                            + " - Exception!", ex);
                }
                listSignFile.add(itemFileSign);
                // Log giao dich ky vao database
                hashingStartTime = digitalSign != null && digitalSign.getHashingStartTime() != null ?
                        digitalSign.getHashingStartTime() : new Date();
                hashingEndTime = digitalSign != null && digitalSign.getHashingEndTime() != null ?
                        digitalSign.getHashingEndTime() : new Date();
                // Tao danh sach thoi gian bat dau, thoi gian ket thuc doc file
                listReadingTime = new ArrayList<>();
                listReadingTime.add(startTime);
                listReadingTime.add(hashingStartTime);
                // Tao danh sach thoi gian bat dau, thoi gian ket thuc hash file
                listHashingTime = new ArrayList<>();
                listHashingTime.add(hashingStartTime);
                listHashingTime.add(hashingEndTime);
                mapSigningStep.put(READ_FILE, listReadingTime);
                mapSigningStep.put(HASH_FILE, listHashingTime);
                listLog = EntityActionLogMobile.createLog(userId2, loginName,
                        functionName, mapSigningStep,
                        ERROR_DESCRIPTION_FOR_SIGNING_BY_SOFT_CERTIFICATE,
                        textId, itemFileSign.getCodeErr(), itemFileSign.getMssErr() + " - " + fileId,
                        deviceName);
                LogUtils.insertActionLogMobile(userId2, listLog);
            }
        } catch (IOException ex) {
            //do stuff with exception
            LOGGER.error("getHashFileDoc - Exception!", ex);
        }
//        Date endDate = new Date();
//        long diff11 = Math.abs(endDate.getTime() - startTime.getTime());
//        System.out.println("=====datnv5==== End: " + diff11);
        return listSignFile;
    }
    
    /**
     * @author TungHD
     * appendSignatureIntoListFileDoc
     * Insert thong tin sau khi dong dau cho man danh sach van ban
     * @param request
     * @param listSignHash
     * @param strListFileSign
     * @param deviceName
     * @return
     */
    public static List<SignHashMulti> appendSignatureIntoListFileDoc(HttpServletRequest request,
            List<SignHashMulti> listSignHash, List<EntityFileAttachment> strListFileSign,
            String deviceName) {

        Date startTime = new Date();
        List<SignHashMulti> listResult = new ArrayList<>();
        String sessionId = CommonControler.getSessionIdFromRequest(request);
        if (CommonUtils.isEmpty(sessionId)) {
            SignHashMulti itemResult = new SignHashMulti();
            itemResult.setError("15811100");
            itemResult.setMess("Loi id null");
            listResult.add(itemResult);
            LOGGER.error("appendSignatureIntoListFileDoc - session id null hoac rong");
            return listResult;
        }

        // Lay doi tuong user trong session
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        String loginName = user2 == null ? "" : user2.getStrCardNumber();
        Long user2Id = (user2 != null && user2.getUserId()!=null)?user2.getUserId():0L;
        
        // Lay doi tuong ky trong session
        HttpSession session = HttpSessionCollector.find(sessionId);
        List<SignMultiFile> listSignMultiFile = (List<SignMultiFile>) session.getAttribute("signObject");
        // Lay thong tin signType
        String signType = (String) session.getAttribute(ConstantsFieldParams.SIGN_TYPE);
        // 201812-Pitagon: add
        Map<Long, Long> mapTextProcessId = (Map<Long, Long>) session.getAttribute(ConstantsFieldParams.TEXT_PROCESS_ID);

        EntityActionLogMobile log;
        List<EntityActionLogMobile> listLog;
        String functionName = SIGN_BY_SOFT_CERTIFICATE_2;
        if (CommonUtils.isEmpty(listSignMultiFile) || CommonUtils.isEmpty(listSignHash)) {
            SignHashMulti itemResult = new SignHashMulti();
            itemResult.setError("15811101");
            itemResult.setMess("Loi khong co chu ky");
            listResult.add(itemResult);
            LOGGER.error("appendSignatureIntoListFileDoc - Danh sach doi tuong ky mem nul hoac rong"
                    + " hoac Danh sach chu ky null hoac rong");
            // Log giao dich ky vao database
            functionName += "_0FILE";
            log = new EntityActionLogMobile(user2Id, loginName, startTime,
                    new Date(), functionName, APPEND_SIGNATURE,
                    ERROR_DESCRIPTION_FOR_SIGNING_BY_SOFT_CERTIFICATE,
                    0L, 15811101L, "Loi khong co chu ky", deviceName);
            listLog = new ArrayList<>();
            listLog.add(log);
            LogUtils.insertActionLogMobile(user2Id, listLog);
            return listResult;
        }

        try {
            Map<Long, Calendar> mapTimeSign = new LinkedHashMap<Long, Calendar>();
            Map<Long, List<Object[]>> mapTextAttach = new LinkedHashMap<Long, List<Object[]>>();
            List<Object[]> arrayNewPathSignFile = null;
            Object[] objArr = null;
            
            List<Long> listIdTextSign = new ArrayList<>();
//            List<Long> listIdAttachFileSign = new ArrayList<>();
//            List<String> listAttachFileSign = new ArrayList<>();
//            List<Calendar> listIdTextSignDate = new ArrayList<>();
            DigitalSign softSign;
            Date writingStartTime, writingEndTime;
            List<Date> listAppendingTime, listWritingTime;
            Map<String, List<Date>> mapSigningStep;
            // Duyet danh sach file ky
            functionName += "_" + listSignHash.size() + "FILE";
            for (SignMultiFile signMultiFile : listSignMultiFile) {
                startTime = new Date();
                mapSigningStep = new LinkedHashMap<>();
                softSign = signMultiFile.getSoftSign();
                boolean signSuccess;
                SignHashMulti itemResult;
                // Duyet danh sach chu ky
                for (SignHashMulti signHash : listSignHash) {
                    // Neu file ky va chu ky cua cung 1 file van ban
                    if (signMultiFile.getId().equals(signHash.getId())) {

                        // Khoi tao doi tuong ket qua ky
                        itemResult = new SignHashMulti();
                        itemResult.setId(signMultiFile.getId());
                        itemResult.setTextId(signMultiFile.getTextId());
                        // Thuc hien ky
                        signSuccess = softSign.appendSignature(signHash.getSignature());
                        itemResult.setError(softSign.getErrorCode() == null
                                ? null : softSign.getErrorCode().toString());

                        // Neu ky thanh cong
                        if (signSuccess) {
                            itemResult.setMess("Ky thanh cong");
                            itemResult.setIdFileAttachSign(signMultiFile.getIdFileSign());
                            itemResult.setStorageFileSign(signMultiFile.getStorageFileSign());
                            // Them file van ban ky thanh cong vao danh sach
                            
                            if (!listIdTextSign.contains(signMultiFile.getTextId())) {
                                listIdTextSign.add(signMultiFile.getTextId());
                            }                            
                            mapTimeSign.put(signMultiFile.getTextId(), softSign.getCalendarSign());
                            objArr = new Object[] {signMultiFile.getId(), signMultiFile.getFileSign()};
                            arrayNewPathSignFile = mapTextAttach.get(signMultiFile.getTextId());
                            if (arrayNewPathSignFile == null) {
                                arrayNewPathSignFile = new ArrayList<>();
                            }
                            arrayNewPathSignFile.add(objArr);
                            mapTextAttach.put(signMultiFile.getTextId(), arrayNewPathSignFile);
                            
//                            listIdTextSignDate.add(softSign.getCalendarSign());
//                            listAttachFileSign.add(signMultiFile.getFileSign());
//                            listIdAttachFileSign.add(signMultiFile.getIdFileSign());
                            LOGGER.info("appendSignatureIntoListFileDoc - textId: "
                                    + signMultiFile.getId() + " - Ky thanh cong");
                        } // Neu ky that bai
                        else {
                            itemResult.setMess("Loi dinh chu ky vao file");
                            LOGGER.error("appendSignatureIntoListFileDoc - textId: "
                                    + signMultiFile.getId() + " - Ky that bai");
                        }
                        // Them ket qua ky van ban vao danh sach
                        listResult.add(itemResult);
                        
                        // Ghi log vao database
                        writingStartTime = softSign.getWritingStartTime() != null
                                ? softSign.getWritingStartTime() : new Date();
                        writingEndTime = softSign.getWritingEndTime() != null
                                ? softSign.getWritingEndTime() : new Date();
                        // Tao danh sach thoi gian bat dau, thoi gian ket thuc dinh chu ky vao file
                        listAppendingTime = new ArrayList<>();
                        listAppendingTime.add(startTime);
                        listAppendingTime.add(writingStartTime);
                        // Tao danh sach thoi gian bat dau, thoi gian ket thuc ghi file
                        listWritingTime = new ArrayList<>();
                        listWritingTime.add(writingStartTime);
                        listWritingTime.add(writingEndTime);
                        mapSigningStep.put(APPEND_SIGNATURE, listAppendingTime);
                        mapSigningStep.put(WRITE_FILE, listWritingTime);
                        listLog = EntityActionLogMobile.createLog(user2Id,
                                loginName, functionName, mapSigningStep,
                                ERROR_DESCRIPTION_FOR_SIGNING_BY_SOFT_CERTIFICATE,
                                signMultiFile.getId(), softSign.getErrorCode() == null ?
                                        0L : softSign.getErrorCode(), itemResult.getMess(), deviceName);
                        LogUtils.insertActionLogMobile(user2Id, listLog);
                        break;
                    }
                }
            }
            
            
            startTime = new Date();
            // Lay thong tin user 2
            Long v2UserId = null;
            Vof2_EntityUser v2User = userGroup.getVof2_ItemEntityUser();
            if (v2User != null) {
                v2UserId = v2User.getUserId();
            }
            //---------------------HIendv2---------------------
            Vof2_EntityUser entityUserVof2 = userGroup.getVof2_ItemEntityUser();
            //-------------------End Hiendv----------------------------
            String keySaveFileTmpText = FunctionCommon.getPropertiesValue("storageName");
            if (listSignMultiFile.size() > 0 && signType != null) {
                Long textId = listIdTextSign.get(0);
                switch (signType) {
                    case MARK_TYPE:
                        TextDAO textDao = new TextDAO();
                        TextSignDAO tsDao = new TextSignDAO();
                        TextSignDAO markAttachHistoryDao = new TextSignDAO();
                        for (int i = 0; i < listIdTextSign.size(); i++) {
                            Long textProcessId = mapTextProcessId.get(listIdTextSign.get(i));
                            Long objId = listIdTextSign.get(i);
                            Long orgMarkId = listSignMultiFile.get(i).getOrgMarkId();
                            Long groupType = listSignMultiFile.get(i).getGroupTypeImage();
                            Boolean isMarkMulti = listSignMultiFile.get(i).getIsMarkMulti();
                            int result = 0;
                            Calendar dateSign = mapTimeSign.get(listIdTextSign.get(i));
                            Date signDate = dateSign.getTime();
                            // Datdc start update list dau vi tri
                            if (isMarkMulti) {
                                result = textDao.approveMarkConfirm(listIdTextSign.get(i), textProcessId,
                                        listSignMultiFile.get(0).getStrComment(), signDate);
                                // Update Mark Attach History
                                for(SignMultiFile indexFile : listSignMultiFile){
                                    if (!CommonUtils.isEmpty(indexFile.getMarkFileInfo())) {
                                        markAttachHistoryDao.insertMarkAttachHistoryOption(keySaveFileTmpText,
                                                mapTextAttach.get(listIdTextSign.get(i)), v2UserId, listSignMultiFile
                                                        .get(i).getMarkFileInfo());
                                        break;
                                    }
                                }
                                tsDao.updatePathNew(v2User, keySaveFileTmpText,
                                        mapTextAttach.get(listIdTextSign.get(i)));
                                // Update Path new 
                                for(SignMultiFile indexFile : listSignMultiFile){
                                    if (!CommonUtils.isEmpty(indexFile.getMarkFileInfo())) {
                                        if (textDao.updateMarkLocateMultiMark(indexFile.getMarkFileInfo(), v2UserId) == 0) {
                                            LOGGER.error("Insert Mark du lieu vao bang MARK_LOCATE khong thanh cong");
                                        }
                                    }
                                }
                            } else {
                                result = textDao.approveMarkConfirm(listIdTextSign.get(i), textProcessId, listSignMultiFile.get(0).getStrComment(), signDate);
                            //Tunghd add cap nhat path history
                            markAttachHistoryDao.insertMarkAttachHistoryDoc(keySaveFileTmpText, mapTextAttach.get(listIdTextSign.get(i)), 
                                     v2UserId, objId, groupType, orgMarkId, null);
                            tsDao.updatePathNew(v2User, keySaveFileTmpText, mapTextAttach.get(listIdTextSign.get(i)));
                            if(result != 0){
                                for(SignMultiFile indexFile : listSignMultiFile){
                                    if(!textDao.updateMarkLocate(v2UserId, indexFile)){
                                        LOGGER.error("Insert du lieu vao bang MARK_LOCATE khong thanh cong");
                                        }
                                    }
                                }
                            }
                            //End
                            if (result != 0) {
                                EntityText text = textDao.getTextAndDocByDocumentId(objId);
                                if (text != null) {
//                                    Long orgId = textDao.getOrgIdMarkDoc(textProcessId);
//                                    CommonControler smsDAO = new CommonControler();
//                                    smsDAO.sentSMS(text.getTitle(), userGroup.getUserId2(), text.getCreatorId(), orgId,
//                                            Constants.SMS_TEXT_CONFIG.MARK_SUCCESS, listSignMultiFile.get(0).getStrComment(), 101L);
                                    
                                    // Update file Ben TRINH KY sau khi dong dau XAC NHAN BEN VAN BAN
                                    if (text.getDocumentId() != null) {
                                        AttachDAO attachDAO = new AttachDAO();
                                        List<EntityFileAttachment> listMainFile = attachDAO.getListMarkedFile(text.getTextId());
                                        if (!CommonUtils.isEmpty(listMainFile)) {
                                            for (EntityFileAttachment file : listMainFile) {
                                                textDao.updateFilePathAttach( file.getFileAttachmentId() ,mapTextAttach.get(listIdTextSign.get(i)));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    default:
//                        LOGGER.info(" signType khong hop le: " + signType);
                        break;
                }
//                List<Long> listTextId = new ArrayList<>();
//                List<Integer> listClassificationTypeAssessor = new ArrayList<>();
//                for (SignHashMulti signHash : listSignHash) {
//                    if (signHash.getClassificationTypeAssessor() != null) {
//                        listTextId.add(signHash.getId());
//                        listClassificationTypeAssessor.add(signHash.getClassificationTypeAssessor());
//                    }
//                }
//                MissionSigningDAO missionSigningDAO = new MissionSigningDAO();
//                missionSigningDAO.update(user2Id, listTextId, listClassificationTypeAssessor);
                // Ghi log xu ly cap nhat du lieu sau ky
                log = new EntityActionLogMobile(entityUserVof2.getUserId(), loginName,
                        startTime, new Date(), functionName, UDPATE_DATA_AFFTER_SIGNATURE,
                        ERROR_DESCRIPTION_FOR_SIGNING_BY_SOFT_CERTIFICATE, textId,
                        v2UserId, "Update du lieu sau ky thanh cong", deviceName);
                listLog = new ArrayList<>();
                listLog.add(log);
                LogUtils.insertActionLogMobile(entityUserVof2.getUserId(), listLog);
            }
        } catch (Exception ex) {
            LOGGER.error("appendSignature - Exception:", ex);
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            LOGGER.error("appendSignature - Exception:", e);
            e.printStackTrace();
        }
        return listResult;
    }

}
