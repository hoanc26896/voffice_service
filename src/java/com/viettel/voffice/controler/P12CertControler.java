/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.User.EntityUser;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONObject;
import com.viettel.voffice.constants.ErrorCode;
import static com.viettel.voffice.controler.UserControler.vof2_CheckLoginVHR;
import com.viettel.voffice.database.dao.sign.P12CertDAO;
import com.viettel.voffice.database.dao.staff.UserDAO;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityLoginSso;
import com.viettel.voffice.database.entity.User.Vof2_EntityUser;
import com.viettel.voffice.database.entity.sign.EntityP12Cert;
import com.viettel.voffice.utils.CommonUtils;
//import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.LogUtils;
import java.util.Date;
import org.apache.log4j.Logger;
import org.json.JSONException;
import java.util.logging.Level;

/**
 *
 * @author hanhnq21
 */
public class P12CertControler {

    // Log file
    private static final Logger LOGGER = Logger.getLogger(P12CertControler.class);

    // Ten cua class bao gom ca ten package
    private static final String CLASS_NAME = P12CertControler.class.getName();

    /**
     * tim kiem chung thu
     *
     * @param req
     * @param strData
     * @param isSecurity
     * @return
     */
    public String actionSearch(HttpServletRequest req, String strData,
            String isSecurity) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        // Kiem tra session
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(req);
        // Kiem tra xem co ma hoa du lieu ko
        String aesKey = null;
        Boolean isManagement = false;
        if (isSecurity != null && "1".equals(isSecurity) && userGroup != null) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            strData = SecurityControler.decodeDataByAes(aesKey, strData);
            isManagement = true;
        }
        // Lay ma nhan vien
        String userCardId = userGroup.getCardId();
        log.setUserName(userCardId);
        // Ghi log bat dau chuc nang
        log.setParamList(strData);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(strData);
            String[] keys = new String[]{
                "status",
                "staffId",
                "fromDate",
                "toDate",
                "startRecord",
                "pageSize",
                "isCount",
                "cardId",
                "staffId2"};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long status = null; //trang thai cap phat chung thu
            Long staffId1 = null; //id nhan vien tren con 1
            String fromDate = null; //ngay yeu cau tu ngay
            String toDate = null; //ngay yeu cau den ngay
            Long startRecord = null; //ban ghi bat dau
            Long pageSize = null; //so ban ghi trong trang
            Boolean isGetCount = false; //lau thong tin so luong ban ghi
            String cardId = null;// ma nhan vien
            Long staffId2 = null; //id nhan vien tren con 2
            String strStatus = listValue.get(0);
            if (strStatus != null && strStatus.trim().length() > 0) {
                status = Long.valueOf(strStatus.trim());
            }
            String strStaffId = listValue.get(1);
            if (strStaffId != null && strStaffId.trim().length() > 0) {
                staffId1 = Long.valueOf(strStaffId.trim());
            }
            String strStaffId2 = listValue.get(8);
            if (!CommonUtils.isEmpty(strStaffId2)) {
                staffId2 = Long.valueOf(strStaffId2.trim());
            }
            String strFromDate = listValue.get(2);
            if (strFromDate != null && strFromDate.trim().length() > 0) {
                fromDate = strFromDate.trim();
            }
            String strToDate = listValue.get(3);
            if (strToDate != null && strToDate.trim().length() > 0) {
                toDate = strToDate.trim();
            }
            String strStartRecord = listValue.get(4);
            if (strStartRecord != null && strStartRecord.trim().length() > 0) {
                startRecord = Long.valueOf(strStartRecord);
            }
            String strPageSize = listValue.get(5);
            if (strPageSize != null && strPageSize.trim().length() > 0) {
                pageSize = Long.valueOf(strPageSize);
            }
            String strIsCount = listValue.get(6);
            if (strIsCount != null && strIsCount.trim().length() > 0 && "1".equals(strIsCount.trim())) {
                isGetCount = true;
            }
            String strCardId = listValue.get(7);
            if (strCardId != null && strCardId.trim().length() > 0) {
                cardId = strCardId.trim();
                UserDAO userDao = new UserDAO();
                EntityUser user1 = userDao.getUserNotEnableVof1ByCardId(cardId);
                if (user1 != null) {
                    staffId1 = user1.getUserId();
                }
            }
            LOGGER.error("staffId1: " + staffId1);
            P12CertDAO dao = new P12CertDAO();
            List<EntityP12Cert> result = dao.search(status, staffId1, staffId2, cardId, fromDate, toDate,
                    isGetCount, startRecord, pageSize, isManagement);
            strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                    result, aesKey);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            java.util.logging.Logger.getLogger(DocumentPublishControler.class.getName()).
                    log(Level.SEVERE, "actionSearchDocPublish error: {0}", e.getMessage());
            strResult = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
        return strResult;
    }

    /**
     * huy cap phat chung thu
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String actionCancelRegCert(HttpServletRequest request, String data,
            String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("actionCancelRegCert - No session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Neu user hoac user id tren he thong 1&2 null
        // -> Tra ve thong bao khong co quyen
        EntityUser user1 = userGroup.getItemEntityUser();
//        if (user1 == null || user1.getUserId() == null) {
//            LOGGER.error("actionCancelRegCert - user1 null");
//            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
//        }
        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        LogUtils.logFunctionalStart(log);

        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        Long userId2 = null;
        if (user2 != null) {
            userId2 = user2.getUserId();
        }
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return actionCancelRegCertProcess(request, data, isSecurity, user1,
                userGroup.getStrAesKey(), userId2);
    }

    /**
     * xu ly huy chung thu
     *
     * @param request
     * @param data
     * @param isSecurity
     * @param user1
     * @param aesKey
     * @param userId2
     * @return
     */
    public String actionCancelRegCertProcess(HttpServletRequest request, String data,
            String isSecurity, EntityUser user1, String aesKey, Long userId2) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        Date startDate = new Date();
        Long userId1 = user1.getUserId();// Id nguoi dung tren he thong 1
        String loginName = user1.getLoginName().trim();
        // Giai ma du lieu neu ma hoa
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        // Lay ma nhan vien
        String cardId = user1.getStrCardNumber().trim();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                "publicKey",
                "passWordEnc",
                "passWordDec"
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String publicKey = listValue.get(0);// Public key
            String passWordEnc = listValue.get(1);// Mat khau ma hoa
            String passWordDec = listValue.get(2);// Mat khau khong ma hoa
            if (publicKey == null || "".equals(publicKey.trim())) {
                //loi du lieu dau vao khong hop le
                LogActionControler aclog = new LogActionControler();
                aclog.insertActionLog(userId1, loginName,
                        "actionCancelRegCert", request, "Loi publicKey null", new Date(), "", "");
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID,
                        null, null);
            }
            if ((passWordEnc == null || "".equals(passWordEnc.trim()))
                    && (passWordDec == null || "".equals(passWordDec.trim()))) {
                //loi du lieu dau vao khong hop le
                LogActionControler aclog = new LogActionControler();
                aclog.insertActionLog(userId1, loginName,
                        "actionCancelRegCert", request, "Loi passWord null", new Date(), "", "");
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID,
                        null, null);
            }
            publicKey = publicKey.trim();
            passWordEnc = (passWordEnc != null) ? passWordEnc.trim() : "";
            passWordDec = (passWordDec != null) ? passWordDec.trim() : "";
            boolean checkLoginOk = false;

            if (!"".equals(passWordEnc) && !"".equals(passWordDec)) {
                //check dang nhap tren ca 2
                //login tren voffice 1.0
                UserDAO userD = new UserDAO();
                EntityUser itemUserLogin = userD.getUserByLoginNameAndPass(loginName, passWordEnc);
                if (itemUserLogin != null && itemUserLogin.getUserId() != null) {
                    //dang nhap thanh cong voffice 1.0
                    checkLoginOk = true;
                }
                if (!checkLoginOk) {
                    //neu dang nhap con 1 khong thanh cong
                    //check dang nhap tap trung SSO
                    try {
//                        String cardId = user1.getStrCardNumber().trim();
                        java.util.logging.Logger.getLogger(P12CertControler.class.getName()).
                                log(Level.SEVERE, "actionCancelRegCert. Bat dau check SSO - cardId:"
                                        + cardId + " ; startDate:{0}", new Date());
                        //goi ham dang nhap SSO
                        Date startDateLoginSSO = new Date();
                        EntityLoginSso itemLoginSso = vof2_CheckLoginVHR(cardId, passWordDec);
                        //ghi log DB
                        LogActionControler aclog = new LogActionControler();
                        aclog.insertActionLog(userId1, cardId,
                                "actionCancelRegCert", request, "Bat dau login SSO", startDateLoginSSO, "", "");
                        java.util.logging.Logger.getLogger(P12CertControler.class.getName()).
                                log(Level.SEVERE, "actionCancelRegCert. Ket thuc check SSO - cardId:"
                                        + cardId + " ; endDate:{0}", new Date());
                        if (itemLoginSso != null && itemLoginSso.getUserId() != null
                                && loginName != null) {
                            checkLoginOk = true;
                        }
                    } catch (Exception e) {
                        LOGGER.error("Loi! thuc hien goi service sso ", e);
                    }
                }
            } else if (!"".equals(passWordEnc)) {
                //login tren voffice 1.0
                UserDAO userD = new UserDAO();
                EntityUser itemUserLogin = userD.getUserByLoginNameAndPass(loginName, passWordEnc);
                if (itemUserLogin != null && itemUserLogin.getUserId() != null) {
                    //dang nhap thanh cong voffice 1.0
                    checkLoginOk = true;
                }
            } else if (!"".equals(passWordDec)) {
                //check dang nhap tap trung SSO
                try {
//                    String cardId = user1.getStrCardNumber().trim();
                    java.util.logging.Logger.getLogger(P12CertControler.class.getName()).
                            log(Level.SEVERE, "actionCancelRegCert. Bat dau check SSO - cardId:"
                                    + cardId + " ; startDate:{0}", new Date());
                    //goi ham dang nhap SSO
                    Date startDateLoginSSO = new Date();
                    EntityLoginSso itemLoginSso = vof2_CheckLoginVHR(cardId, passWordDec);
                    //ghi log DB
                    LogActionControler aclog = new LogActionControler();
                    aclog.insertActionLog(userId1, cardId,
                            "actionCancelRegCert", request, "Bat dau login SSO", startDateLoginSSO, "", "");
                    java.util.logging.Logger.getLogger(P12CertControler.class.getName()).
                            log(Level.SEVERE, "actionCancelRegCert. Ket thuc check SSO - cardId:"
                                    + cardId + " ; endDate:{0}", new Date());
                    if (itemLoginSso != null && itemLoginSso.getUserId() != null
                            && loginName != null) {
                        checkLoginOk = true;
                    }
                } catch (Exception e) {
                    LOGGER.error("Loi! thuc hien goi service sso ", e);
                }
            }

            if (checkLoginOk) {
                //neu check dang nhap thanh cpng
                //Lay thong tin chung thu
                P12CertDAO p12CertDAO = new P12CertDAO();
                EntityP12Cert p12Cert = p12CertDAO.getP12CertificateOfUser(userId1,
                        userId2, publicKey);
                if (p12Cert == null || p12Cert.getP12CertId() == null) {
                    LOGGER.error("actionCancelRegCert - userId1: "
                            + userId1 + " - Loi chung thu khong ton tai");
                    return FunctionCommon.generateResponseJSON(ErrorCode.CERTIFICATE_NOT_EXIST,
                            null, null);
                }
                //cap nhat huy chung thu
                if (p12CertDAO.updateCancelRegCertStatus(p12Cert.getP12CertId(), userId1, userId2)) {
                    //huy chung thu thanh cong
                    //ghi log DB
                    LogActionControler aclog = new LogActionControler();
                    aclog.insertActionLog(userId1, loginName,
                            "actionCancelRegCert", request, "Huy chung thu thanh cong", startDate, "", "");
                    // Ghi log ket thuc chuc nang
                    LogUtils.logFunctionalEnd(log);
                    return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, null, null);
                } else {
                    //huy chung thu khong thanh cong
                    LogActionControler aclog = new LogActionControler();
                    aclog.insertActionLog(userId1, loginName,
                            "actionCancelRegCert", request, "Huy chung thu khong thanh cong", new Date(), "", "");
                    return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                            null, null);
                }
            } else {
                //loi dang nhap khong thanh cong
                LogActionControler aclog = new LogActionControler();
                aclog.insertActionLog(userId1, loginName,
                        "actionCancelRegCert", request, "Loi dang nhap", new Date(), "", "");
                return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NOSESSION, null, null);
            }
        } catch (JSONException | NumberFormatException ex) {
            // Hoac value trong doi tuong JSON khong dung dinh dang quy dinh
            // Dinh dang data khong phai JSONObject
            LOGGER.error("actionCancelRegCert - userId1: "
                    + userId1 + " -  Exception: ", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID,
                    null, null);
        } catch (Exception ex) {
            // Loi Server
            LOGGER.error("actionCancelRegCert - userId: "
                    + userId1 + " -  Exception: ", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * huy cap phat chung thu
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String actionCancelRegCertWeb(HttpServletRequest request, String data,
            String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("actionCancelRegCertWeb - No session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Neu user hoac user id tren he thong 1&2 null
        // -> Tra ve thong bao khong co quyen
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        if ((user1 == null || user1.getUserId() == null) && (user2 == null || user2.getUserId() == null)) {
            LOGGER.error("actionCancelRegCertWeb - user null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        Long userId2 = null;
        String userName2 = "";
        if (user2 != null) {
            userId2 = user2.getUserId();
            userName2 = user2.getFullName();
        }
        Long userId1 = null;
        if (user1 != null) {
            userId1 = user1.getUserId();
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
                "p12CertId"
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String strP12CertId = listValue.get(0);// id chung thu

            if (strP12CertId == null || "".equals(strP12CertId.trim())) {
                //loi du lieu dau vao khong hop le
                LogActionControler aclog = new LogActionControler();
                aclog.insertActionLog(userId2, userName2,
                        "actionCancelRegCertWeb", request, "Loi p12CertId null", new Date(), "", "");
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID,
                        null, null);
            }
            Long p12CertId = Long.valueOf(strP12CertId.trim());
            //cap nhat huy chung thu
            P12CertDAO p12CertDAO = new P12CertDAO();
            if (p12CertDAO.updateCancelRegCertStatus(p12CertId, userId1, userId2)) {
                //huy chung thu thanh cong
                //ghi log DB
                LogActionControler aclog = new LogActionControler();
                aclog.insertActionLog(userId2, userName2,
                        "actionCancelRegCertWeb", request, "Huy chung thu thanh cong", new Date(), "", "");
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, 1, aesKey);
            } else {
                //huy chung thu khong thanh cong
                LogActionControler aclog = new LogActionControler();
                aclog.insertActionLog(userId2, userName2,
                        "actionCancelRegCertWeb", request, "Huy chung thu khong thanh cong", new Date(), "", "");
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
        } catch (JSONException | NumberFormatException ex) {
            // Hoac value trong doi tuong JSON khong dung dinh dang quy dinh
            // Dinh dang data khong phai JSONObject
            LOGGER.error("actionCancelRegCertWeb - userId1: "
                    + userId1 + " -  Exception: ", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID,
                    null, null);
        } catch (Exception ex) {
            // Loi Server
            LOGGER.error("actionCancelRegCertWeb - userId: "
                    + userId1 + " -  Exception: ", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

//    public static void main(String[] avg) throws Exception {
//        P12CertControler ct = new P12CertControler();
//        JSONObject obj = new JSONObject();
//        //obj.put("status", "1");
//        //obj.put("staffId", "26201");
//        //obj.put("fromDate", "18/09/2016");
//        //obj.put("toDate", "20/09/2016");
//        obj.put("startRecord", "0");
//        obj.put("pageSize", "10");
//        obj.put("isCount", "1");
//        ct.actionSearch(null, obj.toString(), "");
//
//        JSONObject obj1 = new JSONObject();
//        obj1.put("publicKey", "d73375d1061b5b1d9c635031bdf5c880d4b1c7f99d1e3182582ed94ce01df13144bc2eb9e4e063c354ef4e2b4cbb0db35bb6edb162fdeff5e9cb8a0290a5b26010e28a58a37e5d9b9ec5133b0f2994d598bf470505f7186c9b2bdd43a39830dbc95c22d50b53e8df1ed4a10d2b9626d4a6ee73c037d744e5b56310770c654101");
//        //obj1.put("passWordEnc", "mGcVhFiM3J4ugMvaDPzRO2RHGPg=");
//        obj1.put("passWordDec", "222222a@");
//        EntityUser user1 = new EntityUser();
//        user1.setUserId(26201L);
//        user1.setLoginName("010993");
//        user1.setStrCardNumber("010993");
//        ct.actionCancelRegCertProcess(null, obj1.toString(),
//                "", user1, "");
//    }
}
