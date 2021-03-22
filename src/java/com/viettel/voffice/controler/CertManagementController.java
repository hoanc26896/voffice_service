/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.viettel.digital.sign.DigitalSign;
import com.viettel.digital.sign.SoftSign;
import com.viettel.digital.sign.utils.SignerInfo;
import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import static com.viettel.voffice.constants.FunctionCommon.convertJsonToObject;
import com.viettel.voffice.constants.NumberConstants;
import com.viettel.voffice.constants.StringConstants;
import com.viettel.voffice.database.dao.logAction.ActionLogMobileDAO;
import com.viettel.voffice.database.dao.sign.CertManagementDAO;
import com.viettel.voffice.database.dao.sign.P12CertDAO;
import com.viettel.voffice.database.dao.sms.SmsDAO;
import com.viettel.voffice.database.dao.staff.StaffDAO;
import com.viettel.voffice.database.entity.EntityStaff;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.VtPayEntity;
import com.viettel.voffice.database.entity.sign.EntityP12Cert;
import com.viettel.voffice.database.entity.sign.ErrSign;
import com.viettel.voffice.database.entity.text.SignHashMulti;
import com.viettel.voffice.database.entity.text.SignMultiFile;
import com.viettel.voffice.elasticsearch.connectserver.ConnectServer;
import com.viettel.voffice.security.LoginSecurity;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.HttpSessionCollector;
import com.viettel.voffice.utils.SignUtils;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.json.JSONObject;

/**
 * Dieu khien viec cap phat va quan ly chung thu tren mobile
 *
 * @author DATNV5, mobile: 0986565786, class:CertManagementController
 */
public class CertManagementController {

    // Log file
    private static final org.apache.log4j.Logger logger
            = org.apache.log4j.Logger.getLogger(CertManagementController.class);

    //=======check chung thu 
    public EntityP12Cert checkCertOfSim(EntityUserGroup userGroup) {
        EntityP12Cert entityP12Cert = new EntityP12Cert();
//        UserDAO userDAO = new UserDAO();
//        EntityUser useriEntity = userDAO.getUserInfor(userGroup.getUserId1());
        StaffDAO staffDAO = new StaffDAO();
        EntityStaff staff = staffDAO.getCASIMInfoOfStaff(userGroup.getUserId2());
        String strPathCert = FunctionCommon.getCertFullPath(staff.getCaSerial(), true);
        X509Certificate x509Cert = FunctionCommon.getX509FromFile(strPathCert);

        if (x509Cert != null && staff.getCaSIMPhoneNumber() != null
                && staff.getCaSIMPhoneNumber().trim().length() > 0) {
            Date notAfter = FunctionCommon.dateSort(x509Cert.getNotAfter());
            Date notBefore = x509Cert.getNotBefore();
            Date nowDate = new Date();
            Date nowDateFm = FunctionCommon.dateSort(nowDate);
            long diff = notAfter.getTime() - nowDateFm.getTime();
            long diffDays = diff / (24 * 60 * 60 * 1000);
            entityP12Cert.setCrtTrusted("1");
            entityP12Cert.setMobile(staff.getCaSIMPhoneNumber());
            entityP12Cert.setDaysOfCertActive(diffDays);
            entityP12Cert.setValidFrom(FunctionCommon.dateShow(notBefore, false));
            entityP12Cert.setValidTo(FunctionCommon.dateShow(notAfter, false));
        } else {
            entityP12Cert.setCrtTrusted(
                    String.valueOf(Constants.P12Cert.Status.INVALID));
        }
        return entityP12Cert;
    }

    /**
     * check cert cua chung thu mem
     *
     * @param userGroup
     * @param strPublicKey
     * @return
     */
    private EntityP12Cert checkCertOfSoft(EntityUserGroup userGroup,
            String strPublicKey, String strDeviceSerial) {

        EntityP12Cert itemCertOfUser;
        CertManagementDAO certManagementDAO = new CertManagementDAO();
        itemCertOfUser = certManagementDAO.getCertStateNow(userGroup, strPublicKey,
                strDeviceSerial);
        if (itemCertOfUser == null) {
            itemCertOfUser = new EntityP12Cert();
            // Kiem tra dieu kien cap phat chung thu
            int checkCertByUser = certManagementDAO.checkStateCertForUser(userGroup);
            if (checkCertByUser == 1) {
                //du dieu kien cap moi
                itemCertOfUser.setStatus(NumberConstants.REGISTERCERT.DATAWAIT_NOTFOUND);
            } else {
                itemCertOfUser.setStatus(NumberConstants.REGISTERCERT.DATAWAIT_FULL);
            }
        }
        return itemCertOfUser;
    }

    //==============danh sach cac function ho tro cap phat chung thu===========
    /**
     * Crete file pem
     *
     * @param csr
     * @return
     */
    private PKCS10CertificationRequest parserCsr(String csr) {
        try {
            PEMParser pem = new PEMParser(new InputStreamReader(
                    new ByteArrayInputStream(csr.getBytes()), "8859_1"));
            return (PKCS10CertificationRequest) pem.readObject();
        } catch (Exception ex) {
            logger.error("Loi parserCsr: ", ex);
        }
        return null;
    }

    /**
     * create file csr cho nguoi moi xin cap chung thu
     *
     * @param staffId
     * @param csr
     * @return
     */
    private String saveScrFile(Long staffId, String csr) {
        String filePath = "";
        OutputStream os = null;
        try {
            Calendar cal = Calendar.getInstance();
            String subNameFile = "_" + cal.get(Calendar.YEAR)
                    + (cal.get(Calendar.MONTH) + 1) + cal.get(Calendar.DATE)
                    + cal.get(Calendar.HOUR) + cal.get(Calendar.MINUTE)
                    + cal.get(Calendar.SECOND);
            String contextPath = FunctionCommon
                    .getPropertiesValue(StringConstants.STR_PROPERTIES_CERTPATH);
            String saveName = String.valueOf(staffId.intValue()) + subNameFile + ".csr";
            filePath = File.separator + saveName;
            filePath = filePath.replace("/", File.separator);
            filePath = filePath.replace("\\", File.separator);
            logger.info("datnv5: full path cert for user: " + String.valueOf(staffId.intValue())
                    + ":" + contextPath + filePath);
            os = new FileOutputStream(contextPath + filePath);
            os.write(csr.getBytes());
            os.flush();
            FunctionCommon.writeLogsNewfile(contextPath + filePath);
        } catch (Exception ex) {
            logger.error("Loi write file csr: ", ex);
            filePath = null;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (Exception ex) {
                    logger.error("Loi close file csr: ", ex);
                }
            }
        }
        return filePath;
    }
    //=========================================================================

    /**
     * 1. tra ve trang thai cua chung thu nguoi dung o thoi diem hien tai
     *
     * @param request
     * @param data
     * @return
     */
    public String getCertStateNow(HttpServletRequest request,
            String data) {

        //Lay thong tin user theo sessionId cua request        
        String[] keys = new String[]{
            "publicKey",
            "versionCert",
            "strDeviceSerial"
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request,
                data, keys);
        if (userGroup.getCheckSessionOk()) {
            EntityP12Cert itemCertOfUser;
            // Danh sach cac key de lay du lieu tu client
            List<String> listValue = userGroup.getListParamsFromClient();
            // publicKey cho chung thu mem
            String strPublicKey = listValue.get(0);
            // 0: tat ca, 1: chung thu sim, 2: chung thu mem
            String strVersionCert = listValue.get(1);
            // Doi voi may android bi mat chung thu thi phai gui imei de tim kiem
            String strDeviceSerial = listValue.get(2);
            //thuc hien lay trang thai chung thu cua nguoi dung bang cach kiem tra database
            if ("0".equals(strVersionCert.trim())) {
                // Check ca hai truong hop chung thu
                List<EntityP12Cert> listCertOfUser = new ArrayList<>();
                // Cert on sim
                itemCertOfUser = checkCertOfSim(userGroup);
                itemCertOfUser.setIsCertInSim(1L);
                listCertOfUser.add(itemCertOfUser);
                // Cert on device
                itemCertOfUser = checkCertOfSoft(userGroup, strPublicKey, strDeviceSerial);
                itemCertOfUser.setIsCertInSim(2L);
                listCertOfUser.add(itemCertOfUser);
                return FunctionCommon.responseResult(ErrorCode.SUCCESS,
                        listCertOfUser, userGroup);
            } // Check chung thu cua sim
            else if ("1".equals(strVersionCert.trim())) {
                itemCertOfUser = checkCertOfSim(userGroup);
            } // Check chung thu mem
            else {
                itemCertOfUser = checkCertOfSoft(userGroup, strPublicKey, strDeviceSerial);
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS,
                    itemCertOfUser, userGroup);
        } // Truong hop bị timeout
        else {
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(),
                    null, null);
        }
    }

    /**
     * tao file certificate
     *
     * @param request
     * @param data
     * @return
     */
    public String makeCert(HttpServletRequest request, String data) {
        // Lay thong tin user theo sessionId cua request
        String[] keys = new String[]{
            "caCsr", "strVersionOsClient", "strDeviceSerial"};
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request,
                data, keys);
        if (userGroup.getCheckSessionOk()) {
            List<String> listValue = userGroup.getListParamsFromClient();
            String strCaCsr = listValue.get(0);
            String strVersionOsClient = listValue.get(1);
            String strDeviceSerial = listValue.get(2);

            //thuc hien lay trang thai chung thu cua nguoi dung bang cach kiem tra database
            CertManagementDAO certManagementDAO = new CertManagementDAO();
            logger.info("datnv5: === create certificate give userId1:"
                    + userGroup.getUserId1());
            //1. check state request certificate for user login
            int checkStateCert = certManagementDAO.checkStateCertForUser(
                    userGroup);
            Integer resultResponse = 0;
            if (checkStateCert == 1 && strCaCsr.trim().length() > 0
                    && strVersionOsClient.trim().length() > 0
                    && strDeviceSerial.trim().length() > 0) {
                try {
                    //trang thai tao chung thu
                    //a. create pkcs10
                    PKCS10CertificationRequest pkcs10 = parserCsr(strCaCsr);
                    RSAKeyParameters rsa = (RSAKeyParameters) PublicKeyFactory.createKey(pkcs10.getSubjectPublicKeyInfo());
                    //b. tao file csr tren server luu tru lai de cho cap chung thu
                    String path = saveScrFile(userGroup.getUserId1(), strCaCsr);
                    EntityP12Cert itemNewCert = new EntityP12Cert();
                    itemNewCert.setCrtPath(path);
                    itemNewCert.setPublicKey(rsa.getModulus().toString(16).toLowerCase());
                    itemNewCert.setCsrFile(strCaCsr);
                    itemNewCert.setCommentDevice(strVersionOsClient);
                    String strNewOtpTokenRandom = String.valueOf(FunctionCommon.genTokenCode());
                    itemNewCert.setOtpToken(strNewOtpTokenRandom);
                    itemNewCert.setSerialDevice(strDeviceSerial);
                    //thuc hien chen du lieu tao moi nguoi dung vao database
                    boolean resultInsertCert = certManagementDAO.insertNewCert(
                            userGroup, itemNewCert);
                    if (resultInsertCert) {
                        //neu chen du lieu ok thuc hien gui tin nhan otp cho nguoi dung
                        String strMobileSent = CommonControler.getSMSMobile(
                                userGroup.getVof2_ItemEntityUser().getMobileNumber());
                        SmsDAO sendOtpToUser = new SmsDAO();
                        sendOtpToUser.addMessToTableMessVof2(userGroup.getUserId2(),
                                userGroup.getUserId2(),
                                strMobileSent,
                                "OTP code: " + strNewOtpTokenRandom, 1581,
                                Constants.SMS_TEXT_INTERCEPT.CERTDEVICE_SMS);
                        resultResponse = 1;
                    } else {
                        resultResponse = 0;
                    }
                } catch (IOException ex) {
                    logger.error("loi parse csr ra key RSA:", ex);
                }
            } else {
                logger.error("Du lieu client gui len chua du thong tin co "
                        + "the thieu (caCsr, strVersionOsClient,strDeviceSerial)");
                resultResponse = 0;
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS,
                    resultResponse, userGroup);
        } else {
            //truong hop bị timeout
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
    }

    /**
     * xac nhan giao dich thong qua ma xac nhan OPT
     *
     * @param request
     * @param data
     * @return
     */
    public String confirmTransactionOtp(HttpServletRequest request, String data) {
        // Lay thong tin user theo sessionId cua request
        String[] keys = new String[]{"otpToken", "publicKey"};
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        EntityP12Cert itemCertOfUser = new EntityP12Cert();
        if (userGroup.getCheckSessionOk()) {
            //danh sach cac key de lay du lieu tu client
            List<String> listValue = userGroup.getListParamsFromClient();
            String otpToken = listValue.get(0);
            String publicKey = listValue.get(1);
            //thuc hien lay trang thai chung thu cua nguoi dung bang cach kiem tra database
            CertManagementDAO certManagementDAO = new CertManagementDAO();
            EntityP12Cert itemP12Cert = certManagementDAO.getDataCertByPublickey(userGroup, publicKey, -1);
            if (itemP12Cert == null) {
                //khong lay duoc gia tri theo publicKey
                itemCertOfUser.setStatus(NumberConstants.REGISTERCERT.PUBLICKEY_NOTFOUND);
            } else if (!itemP12Cert.getOtpToken().trim().equals(otpToken)) {
                //MaOTP nhap vao khong hop le, khong khop voi publicKey
                Long countOTP = itemP12Cert.getOtpCount() - 1;
                if (countOTP >= 0) {
                    certManagementDAO.updateOtpCountByPublicKey(userGroup,
                            publicKey, countOTP);
                }
                if (countOTP <= 0) {
                    certManagementDAO.updateStateCert(userGroup, publicKey, 4);
                }
                itemCertOfUser = certManagementDAO.getDataCertByPublickey(userGroup, publicKey, -1);
                itemCertOfUser.setOtpToken("");
                itemCertOfUser.setActivationCode("");
                itemCertOfUser.setStatus(NumberConstants.REGISTERCERT.WRONG_OTP);
                if (countOTP <= 0) {
                    itemCertOfUser.setStatus(NumberConstants.REGISTERCERT.OVERLOAD_OTP);
                }
            } else if (!itemP12Cert.getStatus().equals(0L)) {
                //Khong phai trang thai cap moi cho chung thu
                itemCertOfUser.setStatus(NumberConstants.REGISTERCERT.NEWCSR_NOTFOUND);
            } else if (itemP12Cert.getOtpCount() <= 0) {
                //qua han nhap chung thu
                certManagementDAO.updateStateCert(userGroup, publicKey, 4);
                itemCertOfUser.setStatus(NumberConstants.REGISTERCERT.OVERLOAD_OTP);
            } else if (otpToken.trim().length() > 0
                    && publicKey.trim().length() > 0
                    && itemP12Cert.getOtpCount() > 0) {
                //thoa man dieu kien
                //thuc hien update trang thai thanh trang thai cho cap chung thu
                if (itemP12Cert.getMinusteOtp() > 5) {
                    logger.info("Datnv5: Qua han nhap ma otp:" + userGroup.getUserId1());
                    itemCertOfUser.setStatus(NumberConstants.REGISTERCERT.OVERTIME);
                } else {
                    boolean resultUpdate = certManagementDAO.updateStateCert(userGroup,
                            publicKey, Constants.P12Cert.Status.CONFIRMED_OTP);
                    if (resultUpdate) {
                        itemCertOfUser = certManagementDAO.getDataCertByPublickey(userGroup, publicKey, -1);
                        itemCertOfUser.setOtpToken("");
                        itemCertOfUser.setActivationCode("");
                    } else {
                        itemCertOfUser.setStatus(NumberConstants.REGISTERCERT.WRONG_OTP);
                    }
                }
            } else {
                logger.info("Datnv5: token KeyNull or PublickeyNull:" + userGroup.getUserId1());
                itemCertOfUser.setStatus(NumberConstants.REGISTERCERT.PUBLICKEY_NOTFOUND);
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS,
                    itemCertOfUser, userGroup);
        } else {
            //truong hop bị timeout
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
    }

    /**
     * active chung thu de dua vao su dung
     *
     * @param request
     * @param data
     * @return
     */
    public String activeCert(HttpServletRequest request, String data) {
        // Lay thong tin user theo sessionId cua request
        String[] keys = new String[]{"activationCode", "publicKey"};
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (userGroup.getCheckSessionOk()) {
            EntityP12Cert itemResult = new EntityP12Cert();

            List<String> listValue = userGroup.getListParamsFromClient();
            String activationCode = listValue.get(0);
            String publicKey = listValue.get(1);
            CertManagementDAO certManagementDAO = new CertManagementDAO();
            EntityP12Cert itemP12Cert = certManagementDAO.getDataCertByPublickey(
                    userGroup, publicKey, 1);
            if (itemP12Cert != null && itemP12Cert.getActivationCode() != null
                    && activationCode.trim().length() > 0 && publicKey.trim().length() > 0) {
                //co ban ghi dang cho cap chung thu dung voi publickey
                String crtData = FunctionCommon.readCertFile(itemP12Cert.getCrtPath());
                if (itemP12Cert.getActivationCode().trim().equals(activationCode.trim())
                        && itemP12Cert.getActivationCount() > 0) {

                    if (crtData == null) {
                        //cert file khong ton tai
                        itemResult.setStatus(NumberConstants.REGISTERCERT.CERTFILE_NOTFOUD);
                    } else //active thanh cong
                    //kiem tra xem ma active co dung trong thoi gian khong
                    {
                        if (itemP12Cert.getMinusteActive() > 5) {
                            //vuot qua thoi gian nhap
                            //khong co du lieu thoa man
                            itemResult.setStatus(NumberConstants.REGISTERCERT.OVERTIME);
                        } else {
                            //kiem tra va update ban ghi gia han dong thoi chuyen trang thai chung thu gia han
                            boolean resultUpdate = certManagementDAO.updateActiveCert(userGroup, publicKey);
                            if (resultUpdate) {
                                itemResult = certManagementDAO.getDataCertByPublickey(
                                        userGroup, publicKey, 2);
                                itemResult.setCrtTrusted(crtData);
                            } else {
                                itemResult.setStatus(NumberConstants.REGISTERCERT.UPDATEFAILSTATE_CERT);
                            }
                        }
                    }
                } else {
                    //activeCode khong dung,update giam countActive
                    Long countActiveCode = itemP12Cert.getActivationCount() - 1;
                    if (countActiveCode > -1) {
                        boolean checkUpdateCountAct
                                = certManagementDAO.updateActivateCountByIdCert(
                                        itemP12Cert.getP12CertId(), countActiveCode);
                        if (countActiveCode <= 0) {
                            certManagementDAO.updateStateCertById(itemP12Cert.getP12CertId(), 4);
                        }
                        itemResult = certManagementDAO.getDataCertByIdCert(itemP12Cert.getP12CertId());
                        if (itemResult != null) {
                            itemResult.setActivationCode("");
                            itemResult.setOtpToken("");
                        } else {
                            itemResult = new EntityP12Cert();
                        }
                        if (checkUpdateCountAct) {
                            itemResult.setStatus(NumberConstants.REGISTERCERT.WRONG_ACTIVATE);
                        } else {
                            itemResult.setStatus(NumberConstants.REGISTERCERT.WRONG);
                        }
                        if (countActiveCode == 0) {
                            itemResult.setStatus(NumberConstants.REGISTERCERT.OVERLOAD_ACTIVATE);
                        }
                    } else {
                        certManagementDAO.updateStateCertById(itemP12Cert.getP12CertId(), 4);
                        itemResult.setStatus(NumberConstants.REGISTERCERT.OVERLOAD_ACTIVATE);
                    }
                }
            } else {
                //khong co du lieu thoa man
                itemResult.setStatus(NumberConstants.REGISTERCERT.DATA_NOTFOUND);
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS,
                    itemResult, userGroup);
        } else {
            //truong hop bị timeout
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(),
                    null, null);
        }
    }

    /**
     * thay doi so chung minh thu
     *
     * @param request
     * @param data
     * @return
     */
    public String alterIdentification(HttpServletRequest request, String data) {
        String[] keys = new String[]{"identification"};
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request,
                data, keys);
        if (userGroup.getCheckSessionOk()) {

            List<String> listValue = userGroup.getListParamsFromClient();
            String identification = listValue.get(0);
            CertManagementDAO certManagementDAO = new CertManagementDAO();
            Long result = 0L;
            boolean checkResultUpdate = certManagementDAO.updateIdentification(
                    userGroup, identification);
            if (checkResultUpdate) {
                result = 1L;
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS,
                    result, userGroup);
        } else {
            //truong hop bị timeout
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(),
                    null, null);
        }
    }

    /**
     * Xu ly trong cac truong hop 1. gia han chung thu 2. huy chung thu tren
     * client
     *
     * @param request
     * @param data
     * @return
     */
    public String addExtendCert(HttpServletRequest request, String data) {
        String[] keys = new String[]{"publicKey", "serialDevice"};
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request,
                data, keys);
        if (userGroup.getCheckSessionOk()) {

            List<String> listValue = userGroup.getListParamsFromClient();
            String publicKey = listValue.get(0);
            String serialDevice = listValue.get(1);
            Long result;
            CertManagementDAO certManagementDAO = new CertManagementDAO();
            //trang thai gia han chung thu
            if (certManagementDAO.checkStateCertForUser(userGroup) == 1) {
                //du dieu kien cap chung thu
                if (certManagementDAO.addExtendCert(userGroup, publicKey, serialDevice)) {
                    //co ban ghi dang hoat dong de gia han
                    result = 1L;
                } else {
                    //khong co ban ghi dang hoat dong de gia han
                    result = 0L;
                }
            } else {
                //khong du dieu kien gia han
                result = Long.valueOf(
                        NumberConstants.DIGITAL_SIGN_RESULT_CODE.ERR_CODE_NEWSCREATEFILECER);
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS,
                    result, userGroup);
        } else {
            //truong hop bị timeout
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(),
                    null, null);
        }
    }

    /**
     * lay trang thai gia han cua cua chung thu
     *
     * @param request
     * @param data
     * @return
     */
    public String getExtendCertStatus(HttpServletRequest request, String data) {
        String[] keys = new String[]{"publicKey"};
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request,
                data, keys);
        if (!userGroup.getCheckSessionOk()) {
            //khong co session
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(),
                    null, null);
        }
        List<String> listParams = userGroup.getListParamsFromClient();
        String strPublicKey = listParams.get(0);
        CertManagementDAO certManagementDAO = new CertManagementDAO();
        EntityP12Cert itemCertOfUser = new EntityP12Cert();
        if (strPublicKey.trim().length() <= 0) {
            //khong co publicKey
            itemCertOfUser.setStatus(NumberConstants.REGISTERCERT.DATAWAIT_FULL);
        } else {
            //co public key
            strPublicKey = "NEWEXTENDCERT" + strPublicKey;
            itemCertOfUser = certManagementDAO.getCertStateNow(userGroup,
                    strPublicKey, null);
            if (itemCertOfUser == null) {
                itemCertOfUser = new EntityP12Cert();
                itemCertOfUser.setStatus(NumberConstants.REGISTERCERT.DATA_NOTFOUND);
            }
        }
        return FunctionCommon.responseResult(ErrorCode.SUCCESS,
                itemCertOfUser, userGroup);
    }

    /**
     * Huy gia han chung thu
     *
     * @param request
     * @param data
     * @return
     */
    public String cancelExtendCert(HttpServletRequest request, String data) {
        String[] keys = new String[]{"publicKey"};
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request,
                data, keys);
        if (!userGroup.getCheckSessionOk()) {
            //khong co session
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(),
                    null, null);
        }
        List<String> listParams = userGroup.getListParamsFromClient();
        String strPublicKey = listParams.get(0);
        CertManagementDAO certManagementDAO = new CertManagementDAO();
        Long result;
        if (strPublicKey.trim().length() <= 0) {
            //khong co publicKey
            result = 0L;
        } else {
            //co public key
//            System.out.println("cancelExtendCert === publicKey:" + strPublicKey);
            boolean checkCancel = certManagementDAO.cancelExtendCert(userGroup,
                    strPublicKey);
            if (checkCancel) {
                //du dieu kien cap moi
                result = 1L;
            } else {
                //ko du dieu kien cap moi
                result = 0L;
            }
        }
//        System.out.println("resultresultresultresult=====" + result);
        return FunctionCommon.responseResult(ErrorCode.SUCCESS,
                result, userGroup);
    }

    /**
     * Huy chung thu tu thiet bi mobile
     *
     * @param request
     * @param data
     * @return
     */
    public String cancelCert(HttpServletRequest request, String data) {

        String[] keys = new String[]{"publicKey", "passWord"};
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request,
                data, keys);
        if (!userGroup.getCheckSessionOk()) {
            //khong co session
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(),
                    null, null);
        }
        List<String> listParams = userGroup.getListParamsFromClient();
        String strPublicKey = listParams.get(0);
        String strPass = listParams.get(1).trim().toLowerCase();
        CertManagementDAO certManagementDAO = new CertManagementDAO();
        Long result;
        boolean checkPass = false;
        String strPassEncode = LoginSecurity.encrypt(strPass);
        if (strPassEncode.equals(userGroup.getStrPassVof1().trim().toLowerCase())
                || strPass.equals(userGroup.getStrPassVof2().trim().toLowerCase())) {
            checkPass = true;
        }
        if (strPublicKey.trim().length() <= 0) {
            //khong co publicKey
            result = NumberConstants.REGISTERCERT.PUBLICKEY_NOTFOUND;
        } else if (!checkPass) {
            result = NumberConstants.REGISTERCERT.PASSLOGIN_ERR;
        } else {
            boolean checkCancel = certManagementDAO.cancelCert(userGroup, strPublicKey);
            if (checkCancel) {
                result = NumberConstants.REGISTERCERT.SUCSESS;
            } else {
                result = NumberConstants.REGISTERCERT.CANCELCERT_ERR;
            }
        }
        return FunctionCommon.responseResult(ErrorCode.SUCCESS,
                result, userGroup);
    }

    /**
     * <b>Sao luu chung thu</b>
     *
     * @param request
     * @param data
     * @return
     */
    public String backupCert(HttpServletRequest request, String data) {

        String[] keys = new String[]{
            ConstantsFieldParams.P12_CERT_ID,
            "crtBackup"
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request,
                data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(),
                    null, null);
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            Long p12CertId = Long.parseLong(listValue.get(0));
            String crtBackup = listValue.get(1);
            CertManagementDAO dao = new CertManagementDAO();
            int result = dao.backupCert(userGroup.getUserId2(), p12CertId, crtBackup) ? 1 : 0;
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            logger.error("backupCert - Exception - username: " + userGroup.getCardId(),
                    ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * thuc hien donwload file ho so gia han chu ky so
     *
     * @param request
     * @param data
     * @return
     */
    public Response DownloadFileInfoUser(HttpServletRequest request, String data) {
        //Lay thong tin user theo sessionId cua request        
        String[] keys = new String[]{
            "serialDevice",
            "publicKey"
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request,
                data, keys);
        if (userGroup.getCheckSessionOk()) {
            Response response;
            List<String> listValue = userGroup.getListParamsFromClient();
            String serialDevice = listValue.get(0);
//            System.out.println("serialDevice: " + serialDevice);
            String publickey = listValue.get(1);
//            System.out.println("publickey: " + publickey);
            CertManagementDAO certManagementDAO = new CertManagementDAO();
            EntityP12Cert entityP12Cert = certManagementDAO.getPathFileInfoUser(userGroup, serialDevice, publickey);
            try {
                if (entityP12Cert != null) {
                    String strStorage = entityP12Cert.getStoragefileinfo();
                    String strFullPath = FunctionCommon.getPropertiesValue(strStorage) + entityP12Cert.getPathfileinfouser();
//                    System.out.println("pathFull file = " + strFullPath);
                    File file = new File(strFullPath);
                    if (!file.exists()) {
                        return null;
                    }
                    InputStream inputStream = new FileInputStream(file);
                    Response.ResponseBuilder responseBuilder = Response.ok(inputStream);
                    response = responseBuilder.build();
                    return response;
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            return null;
        } else {
            return null;
        }
    }

    /**
     * Thuc hien ky file ho so gia han chung thu so
     *
     * @param req
     * @param data
     * @return
     */
    public String signFileExtentCa(HttpServletRequest req, String data) {
        //Lay thong tin user theo sessionId cua request        
        String[] keys = new String[]{
            "serialDevice",
            "publicKey",
            "stepSign",
            "signature"
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(req,
                data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(),
                    null, null);
        }
        List<String> listValue = userGroup.getListParamsFromClient();
        String serialDevice = listValue.get(0);
        String publicKey = listValue.get(1);
        String stepSign = listValue.get(2);
        String signature = listValue.get(3);

        if (stepSign.trim().length() > 0 && "1".equals(stepSign)) {
            String strResult = hashFileUserCAInfo(req, userGroup, serialDevice, publicKey);
            return strResult;
        } else {
            //thuc hien sign file
            String strResult = attachSignHash(req, userGroup, serialDevice, publicKey, signature);
            return strResult;
        }
    }

    /**
     * thuc hien hash file thong tin gia han chung thu
     *
     * @param req
     * @param userGroup
     * @param serialDevice
     * @param publicKey
     * @return
     */
    private String hashFileUserCAInfo(HttpServletRequest req, EntityUserGroup userGroup,
            String serialDevice, String publicKey) {
        //thuc hien hash file
        SoftSign softSign;
        DigitalSign digitalSign;
        List<SignHashMulti> ListResultHashResponse = new ArrayList<>();
        SignHashMulti resultHashResponse = new SignHashMulti();
        ErrSign itemCheckCer = SignUtils.checkCer(userGroup, publicKey, "");
        //datnv5: bo sung yeu cau ky voi chung thu het han trong vong 1 thang
        if (itemCheckCer.getErrCode() != null
                && (itemCheckCer.getErrCode().equals(1L) || itemCheckCer.getErrCode().equals(158111181L))) {
            //SignerInfo
            SignerInfo signerInfo = SignUtils.getSignerInfo(userGroup.getVof2_ItemEntityUser());
            //lay duong dan file  goc de  tien hanh hash file
            CertManagementDAO certManagementDAO = new CertManagementDAO();
            EntityP12Cert entityP12Cert = certManagementDAO.getPathFileInfoUser(userGroup, serialDevice, publicKey);
            if (entityP12Cert != null) {
                String strStorage = entityP12Cert.getStoragefileinfo();
                String strFullPath = FunctionCommon.getPropertiesValue(strStorage) + entityP12Cert.getPathfileinfouser();
                if (strFullPath != null && strFullPath.trim().length() > 0) {
                    String signedFilePath = StringUtils.removeEnd(strFullPath, ".pdf") + "_Signed.pdf";
                    String signedFileSortPath = StringUtils.removeEnd(entityP12Cert.getPathfileinfouser(), ".pdf") + "_Signed.pdf";
                    softSign = new SoftSign(signerInfo, strFullPath, signedFilePath, false);

                    P12CertDAO p12CertDAO = new P12CertDAO();
                    EntityP12Cert p12Cert = p12CertDAO.getP12CertificateOfUser(userGroup.getUserId1(), userGroup.getUserId2(), publicKey);
                    if (p12Cert != null) {
                        try {
                            String certPath = p12Cert.getCrtPath();
                            String strLocation = userGroup.getVof2_ItemEntityUser().getAdOrgName() + "::0";
                            JSONObject jsonObj = new JSONObject();
                            jsonObj.put("location", strLocation);
                            strLocation = jsonObj.toString();
                            List<String> listHashInfo = softSign.getDigestExtendCert(certPath, "", strLocation);
                            digitalSign = softSign;
                            String strIpServer = "http://" + req.getLocalAddr()
                                    + ":" + String.valueOf(req.getLocalPort())
                                    + "/ServiceMobile_V02/resources/";
                            if (listHashInfo != null && listHashInfo.size() > 0) {
                                resultHashResponse.setHash(listHashInfo.get(0));
                                resultHashResponse.setStrUrl(strIpServer);
                                resultHashResponse.setError("1");
                                resultHashResponse.setMess("Hash file thanh cong");
                                SignMultiFile itemMultiFile = new SignMultiFile();
                                itemMultiFile.setSoftSign(digitalSign);
                                itemMultiFile.setCodeErr(1L);
                                itemMultiFile.setStorageFileSign(strStorage);
                                itemMultiFile.setHash(listHashInfo.get(0));
                                itemMultiFile.setId(entityP12Cert.getP12CertId());
                                itemMultiFile.setFileSign(signedFileSortPath);
                                if (!CommonControler.setSignSession(req, itemMultiFile)) {
                                    resultHashResponse.setError("11");
                                    resultHashResponse.setMess("Luu phien ky bi loi");
                                }
                            } else {
                                resultHashResponse.setHash("");
                                resultHashResponse.setError("2");
                                resultHashResponse.setMess("Khong co hash file");
                            }
                        } catch (Exception ex) {
                            resultHashResponse.setHash("");
                            resultHashResponse.setError("3");
                            resultHashResponse.setMess("Khong co hash file:" + ex.getMessage());
                            logger.error("signFileExtentCa", ex);
                        }
                    } else {
                        resultHashResponse.setHash("");
                        resultHashResponse.setError("4");
                        resultHashResponse.setMess("Khong co thong tin chung thu p12");
                    }
                } else {
                    resultHashResponse.setHash("");
                    resultHashResponse.setError("5");
                    resultHashResponse.setMess("Duong dan file ky khong co");
                }
            } else {
                resultHashResponse.setHash("");
                resultHashResponse.setError("6");
                resultHashResponse.setMess("Duong dan file chung thu khong co");
            }
            ListResultHashResponse.add(resultHashResponse);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS,
                    ListResultHashResponse, userGroup);
        } else {
            return FunctionCommon.responseResult(ErrorCode.SUCCESS,
                    itemCheckCer.getErrCode(), userGroup);
        }
    }

    /**
     * thuc hien attach chu ky dien tu vao file pdf
     *
     * @param req
     * @param userGroup
     * @param serialDevice
     * @param publicKey
     * @return
     */
    private String attachSignHash(HttpServletRequest req, EntityUserGroup userGroup,
            String serialDevice, String publicKey, String signature) {
        SignHashMulti itemResult = new SignHashMulti();
        try {
            String sessionId = CommonControler.getSessionIdFromRequest(req);
            if (CommonUtils.isEmpty(sessionId)) {
                itemResult.setError("15811100");
                itemResult.setMess("Loi id null");
                return FunctionCommon.responseResult(ErrorCode.SUCCESS,
                        itemResult, userGroup);
            }
            DigitalSign softSign;
            // Lay doi tuong ky trong session
            HttpSession session = HttpSessionCollector.find(sessionId);
            SignMultiFile signMultiFile = (SignMultiFile) session.getAttribute("signObject");
            softSign = signMultiFile.getSoftSign();
            boolean signSuccess = softSign.appendSignature(signature);
            if (signSuccess) {
                String strDateFull = FunctionCommon.dateShow(new Date(), true).trim().replace("/", "").replace(" ", "").replace(":", "");
                Random random = new Random();
                int x = random.nextInt(900) + 100;
                String strToken = "CERT" + userGroup.getUserId2().toString() + strDateFull + x;
                String strConfigWebVtPay = FunctionCommon.getValueFromConfigDataBase("CERT_EXTEND_VTPAY_CONFIG");
                VtPayEntity itemConfigPay = (VtPayEntity) convertJsonToObject(strConfigWebVtPay, VtPayEntity.class);
                VtPayEntity vtPayEntity = FunctionCommon.createUrlVtPay(strToken, "ThanhToanTienCert",
                        userGroup.getVof2_ItemEntityUser().getMobileNumber(), itemConfigPay);
                if (vtPayEntity != null && vtPayEntity.getStrUrlFullWeb().trim().length() > 0) {
                    //thuc hien update file ky dien tu vao duong dan
                    //id chung thu so dang gia han
                    P12CertDAO p12CertDAO = new P12CertDAO();
                    //thuc hien base 64 file da ky Ca
                    String strFullPathFileSign = FunctionCommon.getPropertiesValue(signMultiFile.getStorageFileSign())
                            + signMultiFile.getFileSign();
                    String strBase64File = FunctionCommon.convertPdfToBase64String(strFullPathFileSign);
                    vtPayEntity.setProfile(strBase64File);
                    Boolean resultOrder = callOrderPayFromBccsCa(userGroup, vtPayEntity, publicKey);
                    if (resultOrder) {
                        Boolean valueUpdate = p12CertDAO.updateUrlSignFileUserInfoAfterSign(signMultiFile.getId(),
                                signMultiFile.getFileSign(), vtPayEntity);
                        if (!valueUpdate) {
                            itemResult.setError("4");
                            itemResult.setMess("Ky file thanh cong, Khong update duoc du lieu");
                            itemResult.setStrUrl(vtPayEntity.getStrUrlFullWeb());
                        } else {
                            itemResult.setError("1");
                            itemResult.setMess("Ky file thanh cong: " + signSuccess);
                            itemResult.setStrUrl(vtPayEntity.getStrUrlFullWeb());
                        }
                    } else {
                        itemResult.setError("5");
                        itemResult.setMess("Call service bccsca add extend error");
                    }
                } else {
                    itemResult.setError("3");
                    itemResult.setMess("Khong tao duoc duong dan web thanh toan");
                }
            } else {
                itemResult.setError("2");
                itemResult.setMess("Add chu ky file khong thanh cong: " + signSuccess);
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS,
                    itemResult, userGroup);
        } catch (Exception ex) {
            logger.error("attachSignHash", ex);
            itemResult.setError("1");
            itemResult.setMess("Loi khi attach sign");
            return FunctionCommon.responseResult(ErrorCode.SUCCESS,
                    itemResult, userGroup);
        }
    }

    /**
     * Yeu cau thanh toan viettel pay
     *
     * @param userGroup
     * @param vtPayEntity
     * @param publicKey
     * @return
     */
    public Boolean callOrderPayFromBccsCa(EntityUserGroup userGroup, VtPayEntity vtPayEntity, String publicKey) {
        //thuc hien lay thong tin serial chung thu so
        P12CertDAO p12CertDAO = new P12CertDAO();
        EntityP12Cert entityP12Cert = p12CertDAO.getP12CertificateOfUser(userGroup.getUserId1(), userGroup.getUserId2(), publicKey);
        StringBuilder stringBuilderXml = new StringBuilder();
        stringBuilderXml.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"");
        stringBuilderXml.append(" xmlns:web=\"http://WebService.DAO.database.cm.bccs.viettel.com/\">");
        stringBuilderXml.append("<soapenv:Header/>");
        stringBuilderXml.append("<soapenv:Body>");
        stringBuilderXml.append("<web:savePaymentRequestInfo>");
        stringBuilderXml.append("<savePaymentRequestInfo>");
        stringBuilderXml.append("<billCode>");
        stringBuilderXml.append(vtPayEntity.getBillCode());
        stringBuilderXml.append("</billCode>");
        stringBuilderXml.append("<checkSum>");
        stringBuilderXml.append(vtPayEntity.getStrCheckSum());
        stringBuilderXml.append("</checkSum>");
        stringBuilderXml.append("<idNo>");
        stringBuilderXml.append(userGroup.getVof2_ItemEntityUser().getStrIdentification());
        stringBuilderXml.append("</idNo>");
        stringBuilderXml.append("<merchantCode>");
        stringBuilderXml.append(vtPayEntity.getMerchant_code());
        stringBuilderXml.append("</merchantCode>");
        stringBuilderXml.append("<orderId>");
        stringBuilderXml.append(vtPayEntity.getBillCode());
        stringBuilderXml.append("</orderId>");
        stringBuilderXml.append("<profile>");
        stringBuilderXml.append(vtPayEntity.getProfile());
        stringBuilderXml.append("</profile>");
        stringBuilderXml.append("<serialCts>");
        stringBuilderXml.append(entityP12Cert.getSerial());
        stringBuilderXml.append("</serialCts>");
        stringBuilderXml.append("<transAmount>");
        stringBuilderXml.append(vtPayEntity.getTrans_amount());
        stringBuilderXml.append("</transAmount>");
        stringBuilderXml.append("<custName>");
        stringBuilderXml.append(userGroup.getVof2_ItemEntityUser().getFullName());
        stringBuilderXml.append("</custName>");
        stringBuilderXml.append("<custAddress>");
        stringBuilderXml.append(userGroup.getVof2_ItemEntityUser().getStrAddress());
        stringBuilderXml.append("</custAddress>");
        stringBuilderXml.append("<custPhone>");
        stringBuilderXml.append(userGroup.getVof2_ItemEntityUser().getMobileNumber());
        stringBuilderXml.append("</custPhone>");
        stringBuilderXml.append("<custEmail>");
        stringBuilderXml.append(userGroup.getVof2_ItemEntityUser().getStrEmail());
        stringBuilderXml.append("</custEmail>");
        stringBuilderXml.append("</savePaymentRequestInfo>");
        stringBuilderXml.append("</web:savePaymentRequestInfo>");
        stringBuilderXml.append("</soapenv:Body>");
        stringBuilderXml.append("</soapenv:Envelope>");
        String strLogs = vtPayEntity.getBillCode() + "," + userGroup.getVof2_ItemEntityUser().getFullName();
        Date startDate = new Date();
        String strDataResponse = ConnectServer.sendRequestToServerSoap(vtPayEntity.getUrlBccs(), stringBuilderXml.toString());
//        System.out.println("callOrderPayFromBccsCa strDataResponse=" + strDataResponse);
        String errorCode = UserControler.getTagValue(strDataResponse, "errorCode");
        String description = UserControler.getTagValue(strDataResponse, "description");
        ActionLogMobileDAO actionLogMobileDAO = new ActionLogMobileDAO();
        String strLogData = errorCode + "," + description + ",strDataResponse= " + strDataResponse.trim();
        if(strLogData!=null && strLogData.trim().length()>800){
            strLogData = strLogData.trim().substring(0, 800);
        }
        actionLogMobileDAO.insert(userGroup.getUserId2(), userGroup.getCardId(), startDate, new Date(),
                "CertManagementAction.callOrderPayFromBccsCa", strLogs,strLogData ,
                null, null, "", null);
        return "00".equals(errorCode);
    }

    /**
     * lay file pdf tu he thong bccs
     *
     * @param vtPayEntity
     * @return
     */
    private static Boolean getFilePdfFromBccsCA(EntityUserGroup userGroup, VtPayEntity vtPayEntity, String publicKey, String pathSavePdf) {
        P12CertDAO p12CertDAO = new P12CertDAO();
        EntityP12Cert entityP12Cert = p12CertDAO.getP12CertificateOfUser(userGroup.getUserId1(), userGroup.getUserId2(), publicKey);
        String strXml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://WebService.DAO.database.cm.bccs.viettel.com/\">"
                + "<soapenv:Header/>"
                + "<soapenv:Body>"
                + "<web:getPdfFormExtend>"
                + "<getPdfFormExtend>"
                + "<idNo>" + userGroup.getVof2_ItemEntityUser().getStrIdentification() + "</idNo>"
                + "<publicKey></publicKey>"
                + "<serialCts>" + entityP12Cert.getSerial() + "</serialCts>"
                + "<custName>" + userGroup.getVof2_ItemEntityUser().getFullName() + "</custName>"
                + "<custAddress>" + userGroup.getVof2_ItemEntityUser().getStrAddress() + "</custAddress>"
                + "<custPhone>" + userGroup.getVof2_ItemEntityUser().getMobileNumber() + "</custPhone>"
                + "<custEmail>" + userGroup.getVof2_ItemEntityUser().getStrEmail() + "</custEmail>"
                + "</getPdfFormExtend>"
                + "</web:getPdfFormExtend>"
                + "</soapenv:Body>"
                + "</soapenv:Envelope>";
        Date startDate = new Date();
//        System.out.println("strXmlstrXml=" + strXml);
        String strDataResponse = ConnectServer.sendRequestToServerSoap(vtPayEntity.getUrlBccs(), strXml);
        String errorCode = UserControler.getTagValue(strDataResponse, "errorCode");
//        System.out.println("getFilePdfFromBccsCA:errorCode = " + errorCode);
        String description = UserControler.getTagValue(strDataResponse, "description");
        ActionLogMobileDAO actionLogMobileDAO = new ActionLogMobileDAO();
        String strLogs = vtPayEntity.getBillCode() + "," + userGroup.getVof2_ItemEntityUser().getFullName();
        String strLogData = errorCode + "," + description + ",strDataResponse= " + strDataResponse.trim();
        if(strLogData!=null && strLogData.trim().length()>800){
            strLogData = strLogData.trim().substring(0, 800);
        }
        actionLogMobileDAO.insert(userGroup.getUserId2(), userGroup.getCardId(), startDate, new Date(),
                "CertManagementAction.getFilePdfFromBccsCA", strLogs, strLogData, null, null, "", null);
        if ("00".equals(errorCode)) {
            String strUserData = UserControler.getTagValue(strDataResponse, "profileContent");
            return FunctionCommon.convertStringBase64ToPdf(strUserData, pathSavePdf);
        } else {
            return false;
        }
    }

    /**
     * Thuc hien lay chung thu tu bccsca
     *
     * @param userGroup
     * @param itemP12Extend
     * @param vtPayEntity
     * @param publicKey
     * @return
     */
    public EntityP12Cert getCertFromBccsCA(EntityUserGroup userGroup, EntityP12Cert itemP12Extend,
            VtPayEntity vtPayEntity, String publicKey) {
        //thuc hien lay thong tin serial chung thu so
        P12CertDAO p12CertDAO = new P12CertDAO();
        CertManagementDAO certManagementDAO = new CertManagementDAO();
        String strPublicKey = publicKey.toLowerCase().replace("newextendcert", "");
        EntityP12Cert entityP12Cert = p12CertDAO.getP12CertificateOfUser(userGroup.getUserId1(), userGroup.getUserId2(), strPublicKey);
        String strIdentification = userGroup.getVof2_ItemEntityUser().getStrIdentification();
        StringBuilder stringBuilderXml = new StringBuilder();
        stringBuilderXml.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"");
        stringBuilderXml.append(" xmlns:web=\"http://WebService.DAO.database.cm.bccs.viettel.com/\">");
        stringBuilderXml.append("<soapenv:Header/>");
        stringBuilderXml.append("<soapenv:Body>");
        stringBuilderXml.append("<web:getCertVoffice>");
        stringBuilderXml.append("<getCertVoffice>");
        stringBuilderXml.append("<csr>");
        stringBuilderXml.append(entityP12Cert.getCsrFile()
                .replace("-----BEGIN CERTIFICATE REQUEST-----", "")
                .replace("-----END CERTIFICATE REQUEST-----", ""));
        stringBuilderXml.append("</csr>");
        stringBuilderXml.append("<serialCts>");
        stringBuilderXml.append(entityP12Cert.getSerial());
        stringBuilderXml.append("</serialCts>");
        stringBuilderXml.append("<idNo>");
        stringBuilderXml.append(strIdentification);
        stringBuilderXml.append("</idNo>");
        stringBuilderXml.append("<merchantCode>");
        stringBuilderXml.append(vtPayEntity.getMerchant_code());
        stringBuilderXml.append("</merchantCode>");
        stringBuilderXml.append("<orderId>");
        stringBuilderXml.append(vtPayEntity.getBillCode());
        stringBuilderXml.append("</orderId>");
        stringBuilderXml.append("<custName>");
        stringBuilderXml.append(userGroup.getVof2_ItemEntityUser().getFullName());
        stringBuilderXml.append("</custName>");
        stringBuilderXml.append("<custAddress>");
        stringBuilderXml.append(userGroup.getVof2_ItemEntityUser().getStrAddress());
        stringBuilderXml.append("</custAddress>");
        stringBuilderXml.append("<custPhone>");
        stringBuilderXml.append(userGroup.getVof2_ItemEntityUser().getMobileNumber());
        stringBuilderXml.append("</custPhone>");
        stringBuilderXml.append("<custEmail>");
        stringBuilderXml.append(userGroup.getVof2_ItemEntityUser().getStrEmail());
        stringBuilderXml.append("</custEmail>");
        stringBuilderXml.append("</getCertVoffice>");
        stringBuilderXml.append("</web:getCertVoffice>");
        stringBuilderXml.append("</soapenv:Body>");
        stringBuilderXml.append("</soapenv:Envelope>");
        Date startDate = new Date();
//        System.out.println("stringBuilderXml===" + stringBuilderXml.toString());
        String strDataResponse = ConnectServer.sendRequestToServerSoap(vtPayEntity.getUrlBccs(), stringBuilderXml.toString());
        String description = UserControler.getTagValue(strDataResponse, "description");
        String errorCode = UserControler.getTagValue(strDataResponse, "errorCode");
        String strLogs = vtPayEntity.getBillCode() + "," + userGroup.getVof2_ItemEntityUser().getFullName();
        String strLogData = errorCode + "," + description + ",strDataResponse= " + strDataResponse.trim();
        if(strLogData!=null && strLogData.trim().length()>800){
            strLogData = strLogData.trim().substring(0, 800);
        }
        ActionLogMobileDAO actionLogMobileDAO = new ActionLogMobileDAO();
        actionLogMobileDAO.insert(userGroup.getUserId2(), userGroup.getCardId(), startDate, new Date(),
                "CertManagementAction.GetCertFromBccsCA", strLogs,strLogData, null, null, "", null);
//        System.out.println("getCertFromBccsCA:strDataResponse = " + strDataResponse);
        if ("00".equals(errorCode)) {
            try {
                BufferedWriter output;
                String strCert = "-----BEGIN CERTIFICATE-----\n"
                        + UserControler.getTagValue(strDataResponse, "cert")
                        + "\n-----END CERTIFICATE-----";
                //thuc hien luu tru lai file chung thu so cho nguoi dung
                String fileName = "/" + strIdentification + "_"
                        + FunctionCommon.dateShow(new Date(), true).replace("/", "").replace(" ", "").replace(":", "") + ".cer";
                String parthFileCer = FunctionCommon.getPropertiesValue("sign.certpath") + fileName;
//                System.out.println("======parthFileCer:" + parthFileCer);
                File fileAttachment = new File(parthFileCer);
                output = new BufferedWriter(new FileWriter(fileAttachment));
                output.write(strCert);
                try {
                    output.close();
                } catch (IOException ex) {
                    logger.error("getCertFromBccsCA", ex);
                }
                //thuc hien kiem tra va save file
                if (fileAttachment.exists()) {
                    X509Certificate x509Cert;
                    try {
                        CertificateFactory cf = CertificateFactory.getInstance("X.509");
                        x509Cert = (X509Certificate) cf.generateCertificate(new FileInputStream(fileAttachment));
                    } catch (CertificateException | FileNotFoundException e) {
                        logger.error("getCertFromBccsCA", e);
                        x509Cert = null;
                    }
                    if (x509Cert != null) {
                        RSAKeyParameters rsa = (RSAKeyParameters) PublicKeyFactory.createKey(x509Cert.getPublicKey().getEncoded());
                        String crtPubKey = rsa.getModulus().toString(16).toLowerCase();
                        if (crtPubKey.equals(publicKey
                                .replaceFirst("NEWEXTENDCERT", "")
                                .replaceFirst("newextendcert", "").toLowerCase())) {
                            //dieu kien cap chung thu thoa man
                            //luu file chung thu, gui tin nhan, thay doi trang thai cap nhat
                            Boolean statusExtend = certManagementDAO.updateDataBaseCer(userGroup, x509Cert, itemP12Extend.getP12CertId(), fileName);
                            if (statusExtend) {
                                entityP12Cert = new EntityP12Cert();
                                entityP12Cert.setP12CertId(itemP12Extend.getP12CertId());
                                String serial = x509Cert.getSerialNumber().toString(16).toLowerCase();
                                entityP12Cert.setSerial(serial);
                                entityP12Cert.setStatusPay(2L);
                            } else {
                                entityP12Cert = new EntityP12Cert();
                                entityP12Cert.setErrorCode("2");
                            }
                        } else {
                            //file chung thu cap phat khong phu hop co the sai nguoi yeu cau
                            File file = new File(parthFileCer);
                            file.delete();
                            entityP12Cert = new EntityP12Cert();
                            entityP12Cert.setErrorCode("3");
                            entityP12Cert.setErrorMess("Chung thu gia han khong dung voi publickey");
                        }
                    }
                }
            } catch (IOException ex) {
                logger.error("getCertFromBccsCA", ex);
                entityP12Cert = null;
            }
        } else {
            //bi loi goi chung thu
            entityP12Cert = new EntityP12Cert();
            entityP12Cert.setErrorCode(errorCode);
            entityP12Cert.setErrorMess(description);
        }
        return entityP12Cert;
    }

    /**
     *
     * @param userGroup
     * @param serialdevice
     * @param publickey
     * @return
     */
    public EntityP12Cert getFileUserInfoFromBccs(EntityUserGroup userGroup,
            String serialdevice, String publickey) {
        //Lay thong tin ban ghi chung thu so can gia han
        //lay thong tin file ca tu he thong bccs
        CertManagementDAO certManagementDAO = new CertManagementDAO();
        EntityP12Cert entityP12Cert = certManagementDAO.getInforCert(userGroup, serialdevice, publickey);
        if (entityP12Cert != null) {
            //goi lay file pdf ho so gia han tu he thong bccs
//            System.out.println("scmt=" + entityP12Cert.getIdentification());
//            System.out.println("serial=" + entityP12Cert.getSerial());
//            System.out.println("publicKey=" + entityP12Cert.getPublicKey());
            //doc noi dung file cert
//            System.out.println("cert=" + entityP12Cert.getCrtPath());
            //thuc hien tao duong dan file pdf
            String strStorageSaveFile = FunctionCommon.getPropertiesValue("storage_saveFile");
            String strPathFolder = "/FileCAInfo/" + FunctionCommon.dateShow(new Date(), false).replace("/", "_");
            File dir = new File(FunctionCommon.getPropertiesValue(strStorageSaveFile) + "/" + strPathFolder);
            dir.mkdirs();
            String strPathFile = strPathFolder + "/" + userGroup.getUserId2() + ".pdf";
            entityP12Cert.setPathfileinfouser(strPathFile);
            entityP12Cert.setStoragefileinfo(strStorageSaveFile);
            String strFullPath = FunctionCommon.getPropertiesValue(strStorageSaveFile) + strPathFile;
            String strConfigWebVtPay = FunctionCommon.getValueFromConfigDataBase("CERT_EXTEND_VTPAY_CONFIG");
            VtPayEntity itemPayEntity = (VtPayEntity) FunctionCommon.convertJsonToObject(strConfigWebVtPay, VtPayEntity.class);
            Boolean resultGetFilePdf = getFilePdfFromBccsCA(userGroup, itemPayEntity, publickey, strFullPath);
            if (resultGetFilePdf) {
                if (certManagementDAO.updatePathFileCaInfo(userGroup, strPathFile, strStorageSaveFile, serialdevice, publickey)) {
                    return entityP12Cert;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
        return null;
    }

    /**
     * tra ve ma otp va ma active code
     *
     * @param req
     * @param data
     * @return
     */
    public String getCodeCert(HttpServletRequest req, String data) {
        String[] keys = new String[]{"isExtend", "status", "publicKey"};
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(req,
                data, keys);
        if (!userGroup.getCheckSessionOk()) {
            //khong co session
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(),
                    null, null);
        }
        Date start = new Date();
        List<String> listParams = userGroup.getListParamsFromClient();
        String isExtend = listParams.get(0);
        String status = listParams.get(1);
        String publicKey = listParams.get(2);
        CertManagementDAO certManagementDAO = new CertManagementDAO();
        if (isExtend.trim().length() > 0 && "1".equals(isExtend.trim())) {
            publicKey = "NEWEXTENDCERT" + publicKey;
        }

        EntityP12Cert itemResult;
        if (status.trim().length() > 0 && "1".equals(status.trim())) {
            //thuc hien lay ma active chung thu
            Boolean resultCode = certManagementDAO.updateNewCodeCert(userGroup, publicKey, 1);
//            System.out.println("resultCode,updateNewCodeCert=" + resultCode);
            itemResult = certManagementDAO.getDataCertByPublickey(userGroup, publicKey, 1);
        } else {
            //Thuc hien lay ma otp chung thu
            Boolean resultCode = certManagementDAO.updateNewCodeCert(userGroup, publicKey, 0);
//            System.out.println("resultCode,updateNewCodeCert=" + resultCode);
            itemResult = certManagementDAO.getDataCertByPublickey(userGroup, publicKey, 0);
        }
        Date end = new Date();
        long value = end.getTime() - start.getTime();
//        System.out.println("thoi gian thuc hien=" + value);
        return FunctionCommon.responseResult(ErrorCode.SUCCESS, itemResult, userGroup);
    }
}
