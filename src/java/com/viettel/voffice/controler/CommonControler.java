/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;
import org.json.JSONObject;

import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.constants.StringConstants;
import com.viettel.voffice.database.dao.SystemParameterDAO;
import com.viettel.voffice.database.dao.common.CommonDataBaseDaoVO2;
import com.viettel.voffice.database.dao.sms.SmsDAO;
import com.viettel.voffice.database.dao.staff.OrgDAO;
import com.viettel.voffice.database.dao.staff.UserDAO;
import com.viettel.voffice.database.dao.staff.UserRoleDAO;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntitySystemParameter;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.EntityVhrEmployee;
import com.viettel.voffice.database.entity.EntityVhrOrg;
import com.viettel.voffice.database.entity.User.EntityUser;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.HttpSessionCollector;
import com.viettel.voffice.utils.LogUtils;
import com.viettel.voffice.utils.MessageUtils;
import com.viettel.voffice.utils.SignUtils;

import net.spy.memcached.MemcachedClient;

/**
 * Dieu khien chung thao tac voi du lieu cache
 * 
 * @author datnv5
 */
@SuppressWarnings({"deprecation", "unchecked"})
public class CommonControler {

    // Log file
    private static final Logger logger = Logger
            .getLogger(CommonControler.class);
    SignUtils signUtil = new SignUtils();
    // Key lay thoi gian het han trong memcached
    private static final String EXPIRATION_TIME_MEMCACHED_KEY = "memcached.time.expiration";
    private static int expirationTime = -1;
    // Ten class bao gom ca ten package
    private static final String CLASS_NAME = CommonControler.class.getName();

    /**
     * hash chuoi
     * 
     * @param base
     * @return
     */
    private static String hashSha256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            logger  .error(ex.getMessage(), ex);
            return "";
        }
    }

    /**
     * setData mem
     * 
     * @param key
     * @param value
     * @return
     */
    public static Boolean setDataCache_Mem(String key, String value) {
        key = hashSha256(key);
        if (CommonUtils.isEmpty(key) || CommonUtils.isEmpty(value)) {
            return false;
        }
        // Base64 value
        value = new String(Base64.encode(value.getBytes()));
        MemcachedClient cache = FunctionCommon.getCache();
        List<MemcachedClient> listCache = FunctionCommon.getListCache();
        if (cache != null) {
            if (key.length() > 230) {
                key = key.substring(0, 230);
            }
            try {
                if (expirationTime < 0) {
                    expirationTime = Integer.parseInt(CommonUtils
                            .getAppConfigValue(EXPIRATION_TIME_MEMCACHED_KEY));
                }
                try {
                    cache.set(key, expirationTime, value);
                    if (listCache != null && listCache.size() > 0) {
                        for (MemcachedClient memcachedClient : listCache) {
                            memcachedClient.set(key, expirationTime, value);
                        }
                    }
                    getDataCache_Mem(key);
                } catch (Exception e) {
                    logger.error("Loi!setDataCache_Mem_KHOITAOLAIKETNOIMEM: "
                            + e.getMessage());
                    cache = FunctionCommon.getReCache();
                    cache.set(key, expirationTime, value);

                    listCache = FunctionCommon.getReListCache();
                    if (listCache != null && listCache.size() > 0) {
                        for (MemcachedClient memcachedClient : listCache) {
                            memcachedClient.set(key, expirationTime, value);
                        }
                    }

                    getDataCache_Mem(key);
                }
                return true;
            } catch (NumberFormatException e) {
                logger.error("Loi! setDataCache_Mem");
                logger.error(e.getMessage(), e);
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 
     * 
     * @param key
     * @param value
     * @return
     */
    public static Boolean replaceCachedData(String key, String value) {
        key = hashSha256(key);
        if (CommonUtils.isEmpty(key) || CommonUtils.isEmpty(value)) {
            return false;
        }
        // Base64 value
        value = new String(Base64.encode(value.getBytes()));
        MemcachedClient cache = FunctionCommon.getCache();
        if (cache != null) {
            if (key.length() > 230) {
                key = key.substring(0, 230);
            }
            try {
                if (expirationTime < 0) {
                    expirationTime = Integer.parseInt(CommonUtils
                            .getAppConfigValue(EXPIRATION_TIME_MEMCACHED_KEY));
                }
                cache.replace(key, expirationTime, value);
                return true;
            } catch (NumberFormatException e) {
                logger.error("Loi! replaceCachedData");
                logger.error(e.getMessage(), e);
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * get data memcache
     * 
     * @param keyGetData
     * @return
     */
    public static String getDataCache_Mem(String keyGetData) {
        MemcachedClient cache = FunctionCommon.getCache();
        if (cache != null) {
            String strKey = hashSha256(keyGetData);
            if (strKey.length() > 230) {
                strKey = strKey.substring(0, 230);
            }
            if (strKey.length() <= 0) {
                return null;
            }
            String strStore;
            try {
                strStore = (String) cache.get(strKey);
            } catch (Exception e) {
                logger.error("Loi! getDataCache_Mem");
                logger.error(e.getMessage(), e);
                cache = FunctionCommon.getReCache();
                strStore = (String) cache.get(strKey);
            }
            String strData = (strStore != null && strStore.trim().length() > 0)
                    ? new String(Base64.decode(strStore.getBytes()))
                    : null;
            return strData;
        } else {
            return null;
        }
    }

    /**
     * get data memcache
     * 
     * @param keyGetData
     * @return
     */
    public static Object getDataCacheSign_Mem(String keyGetData) {
        MemcachedClient cache = FunctionCommon.getCache();
        if (cache != null) {
            String strKey = hashSha256(keyGetData);
            if (strKey.length() > 230) {
                strKey = strKey.substring(0, 230);
            }
            if (strKey.length() <= 0) {
                return null;
            }
            return cache.get(strKey);
        } else {
            return null;
        }
    }

    public static void removeDataCache_Mem(String keyGetData) {
        MemcachedClient cache = FunctionCommon.getCache();
        String strKey = hashSha256(keyGetData);
        if (strKey.length() > 230) {
            strKey = strKey.substring(0, 230);
        }
        cache.delete(strKey);
    }

    /**
     * Lay session id tu request
     * 
     * @param request
     * @return
     */
    public static String getSessionIdFromRequest(HttpServletRequest request) {
        String sessionId = null;
        // Kiem tra request
        // Neu request null
        // -> Tra ve null
        if (request == null) {
            logger.error("getSessionIdFromRequest -  request null");
            return sessionId;
        }
        // Gan sessionId bang session id trong header request
        // New session id khac null hoac rong
        // -> Tra ve session id
        sessionId = request.getHeader(StringConstants.STR_SESSIONID);
        if (!CommonUtils.isEmpty(sessionId)) {
            return sessionId;
        }

        // Gan sessionId bang cookie trong header request
        // Neu session id khac null hoac rong
        // Chuoi co dinh dang JSESSIONID=aaaaaaaaaaaaaaaaaaaaaaaa -> Remove
        // JSESSIONID=
        // -> Tra ve session id
        sessionId = request.getHeader(StringConstants.STR_COOKIE);
        if (!CommonUtils.isEmpty(sessionId)) {
            sessionId = sessionId.replace(StringConstants.STR_JSESSIONID,
                    StringConstants.STR_EMTY);
            return sessionId;
        }

        sessionId = request.getRequestedSessionId();
        return sessionId;
    }

    /**
     * Thiet lap phien ky
     * 
     * @param request
     */
    public static boolean setSignSession(HttpServletRequest request,
            Object signObject) {

        boolean result = false;
        // Neu request null hoac doi tuong ky null
        // -> Tra ve false
        if (request == null || signObject == null) {
            logger.error("setSignSession - request null || sign object null");
            return result;
        }

        // Lay session id trong request
        // Neu session id null hoac rong
        // -> Tra ve false
        String sessionId = getSessionIdFromRequest(request);
        if (CommonUtils.isEmpty(sessionId)) {
            logger.error("setSignSession - session id null");
            return result;
        }

        // Lay session trong request
        HttpSession session = request.getSession(true);
        session.setAttribute("signObject", signObject);
        // Dat session vao bo quan ly
        HttpSessionCollector.putSession(sessionId, session);
        result = true;
        return result;
    }

    /**
     * Lay session theo session id
     * 
     * @param sessionId
     * @return
     */
    public static HttpSession getSignSession(String sessionId) {

        HttpSession session = null;
        // Kiem tra session id
        if (CommonUtils.isEmpty(sessionId)) {
            logger.error("getSignSession -  sessionId null hoac rong");
            return session;
        }
        session = HttpSessionCollector.find(sessionId);
        return session;
    }

    // ==============Thuc hien gui tin nhan chung cho he thong================
    /**
     * goi cau hinh tin nhan tra ve chuoi config
     * 
     * @param listObjectSendMess
     *            : danh sach cac doi tuong can gui tin nhan va noi dung vi du:
     *            String1: ngu?i g?i, str2 = ngu?i nh?n, str3 tin nh?n =>
     *            list.add(str1),list.add(str2),list.add(str3)
     * @param type
     *            : 1: ky dien tu, 2: cong van, 3: lich hop
     * @param category
     *            : theo type neu type =1 tuong ung voi: - category: 1 - gui tin
     *            nhan ky dien tu cho ca nhan (vi du: D/c Nguyen van Dat vua
     *            trinh ky cho d/c van ban co tieu de:.....) - category: 2 - gui
     *            tin nhan ky dien tu cho don vi
     * @param userIdVof2
     * @return
     */
    public String getStrMessConfig(List<String> listObjectSendMess, Long type,
            Long category, Long userIdVof2) {

        SmsDAO smsDAO = new SmsDAO();
        String strMessConfig;
        // tin nhan ky dien tu cho ca nhan: d/c Nguyen van Dat vua trinh ky cho
        // dong chi van ban co tieu de: ....
        strMessConfig = smsDAO.getStrConfigMess(type, category, userIdVof2);
        // sua loi cat toi da 400 ky tu
        // con khong thi cat nhu cu
        List<String> listSendMess = new ArrayList<>();
        int maxLength = 400;
        String stri;
        for (int i = 0; i < listObjectSendMess.size(); i++) {
            stri = listObjectSendMess.get(i);
            if (stri != null && stri.length() >= maxLength) {
                //neu dai hon max length thi trim va cong them dau 3 cham
                stri = stri.substring(0, maxLength) + "...";
            }
            listSendMess.add(stri);
        }
        strMessConfig = String.format(strMessConfig, listSendMess.toArray());
        strMessConfig = FunctionCommon.removeUnsign(strMessConfig);
        return strMessConfig;
    }

    /**
     * Thuc hien gui tin nhan ky dien tu
     * 
     * @param strConten
     * @param userSentIdVof1
     * @param userRecevedIdVof1
     * @param category
     *            loai tin nhan can gui
     * @param comment
     *            Y kien
     * @param linkType
     *            Loai link mo app tuong ung cho tung tab
     * @param id
     *            Id van ban trinh trinh ky hoac cong van
     * @return
     */
    public Boolean sentMessToTextSign(String strConten, Long userSentIdVof1,
            Long userRecevedIdVof1, Long category, String comment,
            String linkType, Long id) {
        try {
            if (userSentIdVof1 != null && userRecevedIdVof1 != null) {
                // vinhnq13 thuc hien map ID nguoi gui va nguoi nhan sms tu VO1
                // sang VO2

                UserRoleDAO userRoleDao = new UserRoleDAO();
                EntityUser itemSentVof1 = userRoleDao
                        .getUserInforVof1(userSentIdVof1);
                // EntityUser itemSentVof2 =
                // userRoleDao.getUserInforVof1(userRecevedIdVof1);

                // thuc hien gui tin nhan cho so dien thoai tren VHR
                if (itemSentVof1 != null) {
                    // lay thong tin string tin nhan

                    List<String> listMess = new ArrayList<String>();
                    String strNameSent = (itemSentVof1.getFullName() != null && itemSentVof1
                            .getFullName().trim().length() > 0) ? itemSentVof1
                            .getFullName() : "";
                    // Neu la tu choi ky chinh, tu choi ky nhay, van thu xet
                    // duyet,
                    // van thu tu choi xet duyet thi them thong tin LY DO
                    Long categoryUpdate = category;
                    if (category == Constants.SMS_TEXT_CONFIG.LEADER_REJECT_TEXT
                            || category == Constants.SMS_TEXT_CONFIG.LEADER_REJECT_FLASH_SIGN_TEXT
                            || category == Constants.SMS_TEXT_CONFIG.SECRETARY_REJECT_TEXT
                            || category == Constants.SMS_TEXT_CONFIG.LEADER_REJECT_TEXT_WARNING_TO_CREATOR
                            || category == Constants.SMS_TEXT_CONFIG.SECRETARY_REJECT_TEXT_WARNING_TO_CREATOR) {
                        listMess.add(strNameSent);
                        listMess.add(strConten);
                        listMess.add(comment);
                    } else if (category == Constants.SMS_TEXT_CONFIG.AUTO_PUBLIC_DOC
                            || category == Constants.SMS_TEXT_CONFIG.AUTO_SEND_DOC
                            || category == Constants.SMS_TEXT_CONFIG.SEND_DOC_TO_STAFF) {
                        listMess.add(strConten);
                    } else if (category == Constants.SMS_TEXT_CONFIG.MESSAGE_LANGUAGE_MUTI_SMS) {
                        // 130416 neu la gop tin nhan
                        listMess.add(strNameSent);
                        listMess.add(strConten);
                        if (comment != null && comment.trim().length() > 0) {
                            // 130416 neu ghi chu co thong tin thi bo sung y
                            // kien chi dao
                            listMess.add(comment.trim());
                            // loai tin nhan co bo sung y kien chi dao
                            categoryUpdate = Constants.SMS_TEXT_CONFIG.MESSAGE_LANGUAGE_MUTI_ADD_COMMENT_SMS;
                        }
                    } else {
                        listMess.add(strNameSent);
                        listMess.add(strConten);
                        if (comment != null
                                && comment.trim().length() > 0
                                && (category == Constants.SMS_TEXT_CONFIG.LEADER_MAIN_SIGN_TEXT
                                        || category == Constants.SMS_TEXT_CONFIG.LEADER_FLASH_SIGN_TEXT || category == Constants.SMS_TEXT_CONFIG.SECRETARY_SIGN_TEXT)) {
                            listMess.add(comment.trim());
                            if (category == Constants.SMS_TEXT_CONFIG.LEADER_MAIN_SIGN_TEXT) {
                                // 130416 neu la ky duyet va co ghi chu thi bo
                                // sung y kien chi dao
                                categoryUpdate = Constants.SMS_TEXT_CONFIG.LEADER_MAIN_SIGN_TEXT_ADD_COMMENT;
                            }
                            if (category == Constants.SMS_TEXT_CONFIG.LEADER_FLASH_SIGN_TEXT) {
                                // 130416 neu la ky nhay va co ghi chu thi bo
                                // sung y kien chi dao
                                categoryUpdate = Constants.SMS_TEXT_CONFIG.LEADER_FLASH_SIGN_TEXT_ADD_COMMENT;
                            }
                            if (category == Constants.SMS_TEXT_CONFIG.SECRETARY_SIGN_TEXT) {
                                // Neu la van thu xet duyet va co comment
                                categoryUpdate = Constants.SMS_TEXT_CONFIG.SECRETARY_SIGN_TEXT_ADD_COMMENT;
                            }
                        } else if (category == Constants.SMS_TEXT_CONFIG.LEADER_MAIN_SIGN_TEXT) {
                            // Gui tin nhan da ky duyet khi khong co comment
                            categoryUpdate = Constants.SMS_TEXT_CONFIG.LEADER_MAIN_SIGN_TEXT;
                        } else if (category == Constants.SMS_TEXT_CONFIG.SECRETARY_SIGN_TEXT) {
                            // Gui tin nhan xet duyet khogn co comment

                            categoryUpdate = Constants.SMS_TEXT_CONFIG.SECRETARY_SIGN_TEXT;
                        } else if (category == Constants.SMS_TEXT_CONFIG.LEADER_FLASH_SIGN_TEXT) {
                            // Gui tin nhan da ky nhay khong co comment
                            categoryUpdate = Constants.SMS_TEXT_CONFIG.LEADER_FLASH_SIGN_TEXT;
                        } else if (category == Constants.SMS_TEXT_CONFIG.CREATE_NEW_TEXT) {
                            // Gui tin nhan trinh ky toi nguoi ky tiep theo
                            categoryUpdate = Constants.SMS_TEXT_CONFIG.CREATE_NEW_TEXT;
                        }
                    }
                    CommonControler smsDAO = new CommonControler();
                    String strMess = smsDAO.getStrMessConfig(listMess, 1L,
                            categoryUpdate, 0L);
                    // 21/12/2016 - ThangHT6 - Bo sung link mo app trong sms
                    strMess = MessageUtils.addLinkIntoMessage(strMess,
                            linkType, id);
                    String strMobilePhone = "0";
                    // if (itemSentVof2 != null) {
                    // strMobilePhone = itemSentVof2.getMobileNumber();
                    // }

                    SmsDAO sms_DAO = new SmsDAO();
                    return sms_DAO.addMessToTableMess(userSentIdVof1,
                            userRecevedIdVof1, strMobilePhone, strMess, 1);
                }
            }
            return false;
        } catch (Exception ex) {
            logger.error("sentMessToTextSign (Gui tin nhan theo he thong"
                    + " VOFFICE 1) - Exception!", ex);
            return false;
        }
    }

    /**
     * <b>Thong tin CSKH</b><br>
     * 
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getSupportCustomerInfo(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon
                .getStatusSession(request);
        if (dataSessionGR.getCheckSessionOk()) {
            String cardId = "";
            try {
                // Session time out
                EntityUserGroup entityUserGroup = dataSessionGR;
                if (entityUserGroup.getItemEntityUser() == null
                        && entityUserGroup.getVof2_ItemEntityUser() == null) {
                    return FunctionCommon.generateResponseJSON(
                            ErrorCode.SESSION_TIME_OUT, null, null);
                }
                if (entityUserGroup.getItemEntityUser() != null
                        && !CommonUtils.isEmpty(entityUserGroup
                                .getItemEntityUser().getStrCardNumber())) {
                    cardId = entityUserGroup.getItemEntityUser()
                            .getStrCardNumber();
                } else if (entityUserGroup.getVof2_ItemEntityUser() != null
                        && !CommonUtils.isEmpty(entityUserGroup
                                .getVof2_ItemEntityUser().getStrCardNumber())) {
                    cardId = entityUserGroup.getVof2_ItemEntityUser()
                            .getStrCardNumber();
                }
                log.setUserName(cardId);
                // Kiem tra xem co ma hoa du lieu ko
                String strAesKeyDecode = null;
                if (isSecurity != null && "1".equals(isSecurity)) {
                    // Lay AES Key
                    strAesKeyDecode = entityUserGroup.getStrAesKey();
                    // Giai ma data client gui len
                    data = SecurityControler.decodeDataByAes(strAesKeyDecode,
                            data);
                }
                // Ghi log bat dau chuc nang
                log.setParamList(data);
                LogUtils.logFunctionalStart(log);
                StringBuilder sql = new StringBuilder();
                sql.append("select sp.name as name, sp.value as value from SYSTEM_PARAMETER sp where sp.code in (?,?) order by sp.system_parameter_id asc");
                CommonDataBaseDaoVO2 cmd = new CommonDataBaseDaoVO2();
                List<Object> arrParam = new ArrayList<Object>();
                arrParam.add(ConstantsFieldParams.CUSTOMER_CARE_PHONE_NUMBER_VN);
                arrParam.add(ConstantsFieldParams.CUSTOMER_CARE_PHONE_NUMBER_US);
                List<EntitySystemParameter> lstResult = (List<EntitySystemParameter>) cmd
                        .excuteSqlGetListObjOnCondition(sql, arrParam, null,
                                null, EntitySystemParameter.class);
                EntitySystemParameter result = new EntitySystemParameter();
                if (lstResult != null && lstResult.size() > 0) {
                    result.setInfo_support_vn(lstResult.get(0).getName());
                    result.setSupport_phone_vn(lstResult.get(0).getValue());
                    result.setInfo_support_en(lstResult.get(1).getName());
                    result.setSupport_phone_en(lstResult.get(1).getValue());
                }
                // Log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                        result, strAesKeyDecode);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
                return FunctionCommon.generateResponseJSON(
                        ErrorCode.INPUT_INVALID, null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(
                    ErrorCode.NO_SESSION, null, null);
        }
        return strResult;
    }

    /**
     * <b>Thiet lap thuoc tinh vao session</b><br>
     * 
     * @author thanght6
     * @since Jul 8, 2016
     * @param request
     * @param attName
     *            Ten thuoc tinh
     * @param attValue
     *            Gia tri thuoc tinh
     * @return
     */
    public static boolean setSessionAttribute(HttpServletRequest request,
            String attName, Object attValue) {

        // Neu request null hoac ten thuoc tinh null hoac gia tri thuoc tinh
        // null
        // -> Tra ve false
        if (request == null || CommonUtils.isEmpty(attName) || attValue == null) {
            logger.error("setSessionAttribute - Loi du lieu dau vao khong hop le");
            return false;
        }

        // Lay session id trong request
        // Neu session id null hoac rong
        // -> Tra ve false
        String sessionId = getSessionIdFromRequest(request);
        if (CommonUtils.isEmpty(sessionId)) {
            logger.error("setSessionAttribute - session id null");
            return false;
        }

        // Lay session trong request
        HttpSession session = request.getSession(true);
        session.setAttribute(attName, attValue);
        // Dat session vao bo quan ly
        HttpSessionCollector.putSession(sessionId, session);
        return true;
    }

    /**
     * <b>Thuc hien gui tin nhan ky dien tu theo he thong VOFFICE 2.0</b><br>
     * 
     * @param userSentIdVof2
     * @param strConten
     * @param userRecevedIdVof2
     * @param category
     *            : loai tin nhan can gui
     * @param comment
     *            Y kien
     * @param linkType
     *            Loai link de mo app tu tin nhan
     * @param id
     *            id van ban trinh ky hoac cong van
     * @param config_sms_module_id
     *            id cau hinh modul tin nhan
     * @return
     */
    public Boolean sentMessToTextSignVof2(String strConten,
            Long userSentIdVof2, Long userRecevedIdVof2, Long category,
            String comment, String linkType, Long id, Long config_sms_module_id) {
        try {
            if (userSentIdVof2 != null && userRecevedIdVof2 != null) {
                // thuc hien lay thong tin nguoi dung tu 2 he thong
                UserDAO userDAO = new UserDAO();
                EntityVhrEmployee userInforSent = userDAO
                        .getEmployeeById(userSentIdVof2);
                EntityVhrEmployee userInforReceved = userDAO
                        .getEmployeeById(userRecevedIdVof2);
                // thuc hien gui tin nhan cho so dien thoai tren VHR
                if (userInforReceved != null && userInforSent != null) {
                    // lay thong tin string tin nhan
                    List<String> listMess = new ArrayList<>();
                    String strNameSent = (userInforSent.getFullName() != null && userInforSent
                            .getFullName().trim().length() > 0) ? userInforSent
                            .getFullName() : "";
                    // Neu la tu choi ky chinh, tu choi ky nhay, van thu xet
                    // duyet,
                    // van thu tu choi xet duyet thi them thong tin LY DO
                    Long categoryUpdate = category;
                    if (Objects.equals(category,
                            Constants.SMS_TEXT_CONFIG.LEADER_REJECT_TEXT)
                            || Objects
                                    .equals(category,
                                            Constants.SMS_TEXT_CONFIG.LEADER_REJECT_FLASH_SIGN_TEXT)
                            || Objects
                                    .equals(category,
                                            Constants.SMS_TEXT_CONFIG.SECRETARY_REJECT_TEXT)
                            || Objects
                                    .equals(category,
                                            Constants.SMS_TEXT_CONFIG.LEADER_REJECT_TEXT_WARNING_TO_CREATOR)
                            || Objects
                                    .equals(category,
                                            Constants.SMS_TEXT_CONFIG.SECRETARY_REJECT_TEXT_WARNING_TO_CREATOR)
                            || Objects
                                    .equals(category,
                                            Constants.SMS_TEXT_CONFIG.LEADER_REJECT_TEXT_COMMENT_FILE)
                            || Objects
                                    .equals(category,
                                            Constants.SMS_TEXT_CONFIG.SECRETARY_REJECT_COMMENT_FILE)
                            || Objects
                                    .equals(category,
                                            Constants.SMS_TEXT_CONFIG.LEADER_REJECT_WARNING_TO_CREATOR_COMMENT_FILE)
                            || Objects
                                    .equals(category,
                                            Constants.SMS_TEXT_CONFIG.SECRETARY_REJECT_WARNING_TO_CREATOR_COMMENT_FILE)
                            || Objects
                                    .equals(category,
                                            Constants.SMS_TEXT_CONFIG.LEADER_REJECT_TEXT_COMMENT_FILE_1)
                            || Objects
                                    .equals(category,
                                            Constants.SMS_TEXT_CONFIG.SECRETARY_REJECT_COMMENT_FILE_1)
                            || Objects
                                    .equals(category,
                                            Constants.SMS_TEXT_CONFIG.LEADER_REJECT_WARNING_TO_CREATOR_COMMENT_FILE_1)
                            || Objects
                                    .equals(category,
                                            Constants.SMS_TEXT_CONFIG.SECRETARY_REJECT_WARNING_TO_CREATOR_COMMENT_FILE_1)) {
                        listMess.add(strNameSent);
                        listMess.add(strConten);
                        listMess.add(comment);

                    } else if (Objects.equals(category,
                            Constants.SMS_TEXT_CONFIG.SEND_DOC_TO_STAFF)) {
                        listMess.add(strConten);
                    } else if (Objects.equals(category,
                            Constants.SMS_TEXT_CONFIG.AUTO_PUBLIC_DOC)) {
                        // Gui tin nhan ban hanh tu dong
                        listMess.add(strConten);
                        listMess.add(comment);
                    } else if (Objects.equals(category,
                            Constants.SMS_TEXT_CONFIG.AUTO_SEND_DOC)) {
                        // Gui tin nhan tu dong chuyen
                        listMess.add(strNameSent);
                        listMess.add(comment);
                        listMess.add(strConten);
                    } else if (Objects
                            .equals(category,
                                    Constants.SMS_TEXT_CONFIG.MESSAGE_LANGUAGE_MUTI_SMS)) {
                        // 130416 neu la gop tin nhan
                        listMess.add(strNameSent);
                        listMess.add(strConten);
                        if (comment != null && comment.trim().length() > 0) {
                            // 130416 neu ghi chu co thong tin thi bo sung y
                            // kien chi dao
                            listMess.add(comment.trim());
                            // loai tin nhan co bo sung y kien chi dao
                            categoryUpdate = Constants.SMS_TEXT_CONFIG.MESSAGE_LANGUAGE_MUTI_ADD_COMMENT_SMS;
                        }
                    } else {
                        listMess.add(strNameSent);
                        listMess.add(strConten);
                        if (comment != null
                                && comment.trim().length() > 0
                                && (Objects
                                        .equals(category,
                                                Constants.SMS_TEXT_CONFIG.LEADER_MAIN_SIGN_TEXT)
                                        || Objects
                                                .equals(category,
                                                        Constants.SMS_TEXT_CONFIG.LEADER_FLASH_SIGN_TEXT) || Objects
                                            .equals(category,
                                                    Constants.SMS_TEXT_CONFIG.SECRETARY_SIGN_TEXT))) {
                            listMess.add(comment.trim());
                            if (Objects
                                    .equals(category,
                                            Constants.SMS_TEXT_CONFIG.LEADER_MAIN_SIGN_TEXT)) {
                                // 130416 neu la ky duyet va co ghi chu thi bo
                                // sung y kien chi dao
                                categoryUpdate = Constants.SMS_TEXT_CONFIG.LEADER_MAIN_SIGN_TEXT_ADD_COMMENT;
                            }
                            if (Objects
                                    .equals(category,
                                            Constants.SMS_TEXT_CONFIG.LEADER_FLASH_SIGN_TEXT)) {
                                // 130416 neu la ky nhay va co ghi chu thi bo
                                // sung y kien chi dao
                                categoryUpdate = Constants.SMS_TEXT_CONFIG.LEADER_FLASH_SIGN_TEXT_ADD_COMMENT;

                            }
                            if (Objects
                                    .equals(category,
                                            Constants.SMS_TEXT_CONFIG.SECRETARY_SIGN_TEXT)) {
                                // Neu la van thu xet duyet va co comment
                                categoryUpdate = Constants.SMS_TEXT_CONFIG.SECRETARY_SIGN_TEXT_ADD_COMMENT;
                            }
                        } else if (Objects
                                .equals(category,
                                        Constants.SMS_TEXT_CONFIG.LEADER_MAIN_SIGN_TEXT)) {
                            // Gui tin nhan da ky duyet khi khong co comment
                            categoryUpdate = Constants.SMS_TEXT_CONFIG.LEADER_MAIN_SIGN_TEXT;
                        } else if (Objects.equals(category,
                                Constants.SMS_TEXT_CONFIG.SECRETARY_SIGN_TEXT)) {
                            // Gui tin nhan xet duyet khogn co comment
                            categoryUpdate = Constants.SMS_TEXT_CONFIG.SECRETARY_SIGN_TEXT;
                        } else if (Objects
                                .equals(category,
                                        Constants.SMS_TEXT_CONFIG.LEADER_FLASH_SIGN_TEXT)) {
                            // Gui tin nhan da ky nhay khong co comment
                            categoryUpdate = Constants.SMS_TEXT_CONFIG.LEADER_FLASH_SIGN_TEXT;
                        }
                    }
                    CommonControler smsDAO = new CommonControler();
                    String strMess = smsDAO.getStrMessConfig(listMess, 1L,
                            categoryUpdate, userRecevedIdVof2);
                    // 21/12/2016 - ThangHT6 - Bo sung link mo app trong sms
                    strMess = MessageUtils.addLinkIntoMessage(strMess,
                            linkType, id);
                    String strMobilePhone = "";
                    if (userInforReceved.getMobilePhone() != null) {
                        strMobilePhone = getSMSMobile(userInforReceved
                                .getMobilePhone());
                    }
                    SmsDAO sms_DAO = new SmsDAO();
                    if (strMobilePhone != null
                            && strMobilePhone.trim().length() > 0) {
                        return sms_DAO.addMessToTableMessVof2(userSentIdVof2,
                                userRecevedIdVof2, strMobilePhone, strMess, 1,
                                config_sms_module_id);
                    }
                }
            }
            return false;
        } catch (Exception ex) {
            logger.error("sentMessToTextSignVof2 (Gui tin nhan theo he thong"
                    + " VOFFICE 2) - Exception!", ex);
            return false;
        }
    }

    // 2018-Pitagon: add nghiep vu gui sms nghiep vu dong dau van ban
    public Boolean sentSMS(String content, Long senderId, Long receiverId,
            Long orgId, Long category, String comment, Long config_sms_module_id) {
        try {
            if (senderId != null && (receiverId != null || orgId != null)) {
                UserDAO userDAO = new UserDAO();
                EntityVhrEmployee userInforSent = userDAO
                        .getEmployeeById(senderId);
                EntityVhrEmployee userInforReceved = userDAO
                        .getEmployeeById(receiverId);
                OrgDAO orgDAO = new OrgDAO();
                EntityVhrOrg orgReceiver = null;
                if (orgId != null) {
                    orgReceiver = orgDAO.getOrganizationById(orgId);
                }

                if (userInforReceved != null) {
                    List<String> listMess = new ArrayList<>();
                    String strNameSent = (userInforSent.getFullName() != null && userInforSent
                            .getFullName().trim().length() > 0) ? userInforSent
                            .getFullName() : "";

                    Long categoryUpdate = category;
                    if (Objects.equals(category,
                            Constants.SMS_TEXT_CONFIG.ASK_FOR_REAL)) {
                        listMess.add(strNameSent);
                        listMess.add(content);
                    } else if (Objects.equals(category,
                            Constants.SMS_TEXT_CONFIG.MARK_SUCCESS)
                            || Objects.equals(category,
                                    Constants.SMS_TEXT_CONFIG.REJECT_MARK)) {
                        listMess.add(orgReceiver != null
                                && !CommonUtils.isEmpty(orgReceiver.getName())
                                ? orgReceiver.getName()
                                : "");
                        listMess.add(content);
                        listMess.add(comment);
                    } else if (Objects.equals(category,
                            Constants.SMS_TEXT_CONFIG.ASK_FOR_REPLY)
                            || Objects.equals(category,
                                    Constants.SMS_TEXT_CONFIG.WRITTEN_RESPONSE)) {
                        listMess.add(orgReceiver.getAbbreviation());
                        listMess.add(content);
                    } else if (Objects.equals(category,
                            Constants.SMS_TEXT_CONFIG.CANCEL_ASK_FOR_REPLY)) {
                        listMess.add(getNameEmail(userInforSent.getFullName(),
                                userInforSent.getEmail()));
                        listMess.add(orgReceiver.getAbbreviation());
                        listMess.add(content);
                    }
                    CommonControler smsDAO = new CommonControler();
                    String strMess = smsDAO.getStrMessConfig(listMess, 1L,
                            categoryUpdate, receiverId);

                    String strMobilePhone = "";
                    if (userInforReceved.getMobilePhone() != null) {
                        strMobilePhone = getSMSMobile(userInforReceved
                                .getMobilePhone());
                    }
                    SmsDAO sms_DAO = new SmsDAO();
                    if (strMobilePhone != null
                            && strMobilePhone.trim().length() > 0) {
                        return sms_DAO.addMessToTableMessVof2(senderId,
                                receiverId, strMobilePhone, strMess, 1,
                                config_sms_module_id);
                    }
                }
            }
            return false;
        } catch (Exception ex) {
            logger.error(
                    "sentSMS (Gui tin nhan theo he thong VOFFICE 2) - Exception!",
                    ex);
            return false;
        }
    }

    public static String getSMSMobile(String strMobile) {
        String mobile = "";
        if (strMobile != null) {
            String result = strMobile.replaceAll("[^0-9/]", "");
            String[] resultArray = result.split("/");
            if (resultArray != null && resultArray.length > 0) {
                for (int i = 0; i < resultArray.length; i++) {
                    String unit = resultArray[i];
                    if (unit.length() >= 9) {
                        String header1 = unit.substring(0, 1);
                        String header2 = unit.substring(0, 2);
                        String header3 = unit.substring(0, 3);
                        if ("856".equals(header3) || "855".equals(header3)
                                || "258".equals(header3)
                                || "509".equals(header3)
                                || "670".equals(header3)
                                || "257".equals(header3)
                                || "00".equals(header2) || "84".equals(header2)
                                || "51".equals(header2)
                                || "255".equals(header3)
                                || "237".equals(header3)
                                || "95".equals(header2)) {
                            mobile = unit;
                            break;
                        } else if ("0".equals(header1)) {
                            unit = unit.substring(1, unit.length());
                            mobile = "84" + unit;
                            break;
                        } else {
                            mobile = "84" + unit;
                            break;
                        }
                    }
                }
            }
        }
        return mobile;
    }

    /**
     * check chan tin nhan o cac muc chung
     * 
     * @param userRecevedIdVof2
     * @param idModul
     * @return
     */
    public static boolean checkSmsInterceptGlobal(Long userRecevedIdVof2,
            Long idModul) {
        List<Long> listModulIntercept = new ArrayList<>();
        listModulIntercept.add(idModul);
        SmsDAO sdaoCheckSendMess = new SmsDAO();
        Boolean resultCheck = sdaoCheckSendMess.checkInterceptSendSms(
                userRecevedIdVof2, listModulIntercept);
        return resultCheck;
    }

    public static String getNameEmail(String name, String email) {
        String result = name;
        if ((!CommonUtils.isEmpty(name)) && (!CommonUtils.isEmpty(email))
                && (email.lastIndexOf('@') > 0)) {
            result += "_" + email.substring(0, email.lastIndexOf('@'));
        }
        return result;
    }

    /**
     * @author Tunghd add
     * add nghiep vu gui sms rollback dong dau van ban
     * sentSMSRollback
     * @param content
     * @param senderId
     * @param category
     * @param config_sms_module_id
     * @param userInfoReceved
     * @param orgSenderId
     */
    public void sentSMSRollback(String content, Long senderId,
            Long category, Long config_sms_module_id,
            List<EntityVhrEmployee> userInfoReceved, Long orgSenderId) {
        try {
            if (senderId != null && !userInfoReceved.isEmpty()) {
                UserDAO userDAO = new UserDAO();
                EntityVhrEmployee userInforSent = userDAO.getEmployeeById(senderId);
                OrgDAO orgDAO = new OrgDAO();
                EntityVhrOrg orgSenderEntity = null;
                if (orgSenderId != null) {
                    orgSenderEntity = orgDAO.getOrganizationById(orgSenderId);
                }
                //Tunghd add get ten don vi
                String strOrgSent = (orgSenderEntity.getAbbreviation() != null && orgSenderEntity.getAbbreviation().trim().length() > 0) ? orgSenderEntity .getAbbreviation() : "";
                String orgNameSplited = " - " + strOrgSent;
                //End
                for (EntityVhrEmployee emp : userInfoReceved) {
                    List<String> listMess = new ArrayList<>();
                    String strNameSent = (userInforSent.getFullName() != null && userInforSent .getFullName().trim().length() > 0) ? userInforSent .getFullName() : "";
                    Long categoryUpdate = category;
                    if (Objects.equals(category,
                        Constants.SMS_TEXT_CONFIG.ROLLBACK_MARK_DOC)) {
                        listMess.add(strNameSent + orgNameSplited);
                        listMess.add(content);
                        // listMess.add(comment);
                    } else if (Objects.equals(category, Constants.SMS_TEXT_CONFIG.MARK_SUCCESS)
                            || Objects.equals(category, Constants.SMS_TEXT_CONFIG.REJECT_MARK)) {
//                        listMess.add(orgReceiver != null
//                                && !CommonUtils.isEmpty(orgReceiver.getName())
//                                ? orgReceiver.getName()
//                                : "");
                        listMess.add(content);
                        // listMess.add(comment);
                    }
                    CommonControler smsDAO = new CommonControler();
                    String strMess = smsDAO.getStrMessConfig(listMess, 1L,
                            categoryUpdate, emp.getEmployeeId());

                    String strMobilePhone = "";
                    if (emp.getMobilePhone() != null) {
                        strMobilePhone = getSMSMobile(emp.getMobilePhone());
                    }
                    SmsDAO sms_DAO = new SmsDAO();
                    if (strMobilePhone != null
                            && strMobilePhone.trim().length() > 0) {
                        boolean status = sms_DAO.addMessToTableMessVof2(senderId,
                                emp.getEmployeeId(), strMobilePhone, strMess, 1,
                                config_sms_module_id);
                        if(!status){
                            logger.error(
                                    "sentSMS (Gui tin nhan theo he thong VOFFICE 2) - Loi gui tin nhan insert DATABASE!");
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.error(
                    "sentSMS (Gui tin nhan theo he thong VOFFICE 2) - Exception!",
                    ex);
        }
    }
    
    public String getSystemParameter(HttpServletRequest request,
            String data, String isSecurity) {

        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            logger.error("getSystemParameter - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        try {
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[] { ConstantsFieldParams.CODE };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String code = listValue.get(0);
            if (CommonUtils.isEmpty(code)) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null); 
            }
            SystemParameterDAO dao = new SystemParameterDAO();
            EntitySystemParameter value = dao.getConfigValueByCode(code);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, value, userGroup.getStrAesKey());
        } catch (Exception ex) {
            logger.error("getSystemParameter - username: " + userGroup.getCardId() + " - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }
}
