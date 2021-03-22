/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.viettel.servicesso.Response;
import com.viettel.voffice.callserviceother.SSOResfullCallHttpConnect;
import com.viettel.voffice.callserviceother.SSORunnable;
import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.staff.UserDAO;
import com.viettel.voffice.database.entity.EntityLoginSso;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.User.EntityUser;
import com.viettel.voffice.database.entity.User.Vof2_EntityUser;
import com.viettel.voffice.security.RSA;
import com.viettel.voffice.utils.HttpSessionCollector;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.database.dao.ConfigParameterDAO;
import com.viettel.voffice.database.dao.SystemParameterDAO;
import com.viettel.voffice.database.dao.common.CommonDataBaseDaoVO2;
import com.viettel.voffice.database.dao.document.VHROrgDAO;
import com.viettel.voffice.database.dao.file.ImageDAO;
import com.viettel.voffice.database.dao.logAction.LogActionDao;
import com.viettel.voffice.database.dao.staff.FavouriteDAO;
import com.viettel.voffice.database.dao.staff.OrgDAO;
import com.viettel.voffice.database.dao.staff.StaffDAO;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityPreventClientRemote;
import com.viettel.voffice.database.entity.EntityResult;
import com.viettel.voffice.database.entity.EntityStaff;
import com.viettel.voffice.database.entity.EntityVhrEmployee;
import com.viettel.voffice.database.entity.User.EntityApproveCalDept;
import com.viettel.voffice.database.entity.User.EntityFavourite;
import com.viettel.voffice.database.entity.User.UserDataSsoEntity;
import com.viettel.voffice.database.entity.file.EntityImage;
import com.viettel.voffice.database.entity.log.EntityActionLogMobile;
import com.viettel.voffice.security.AES;
import com.viettel.voffice.security.LoginSecurity;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.LogUtils;
import com.viettel.voffice.utils.PassportConnector;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author datnv5
 */
@SuppressWarnings("deprecation")
public class UserControler {

    public static final String ROOT_ACTION = "Authenticate";
    private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(UserControler.class);
    //==================danh sach ham giao tiep voi service tra  ve============
    // Ten cua class bao gom ca ten package
    private static final String CLASS_NAME = UserControler.class.getName();

    /**
     * <b>Kiem tra trang thai server co dang bao tri hay khong</b><br>
     *
     * @param request
     * @return
     */
    public String checkServerStatus(HttpServletRequest request) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Ghi log bat dau chuc nang
        LogUtils.logFunctionalStart(log);
        int code = CommonUtils.getServerStatusCode();
        ErrorCode errorCode = ErrorCode.getErrorCode(code);
        Object obj;
        if (errorCode == null) {
            errorCode = ErrorCode.SUCCESS;
        }
        // Neu server hoat dong -> Tra ve link LB
        if (errorCode == ErrorCode.SUCCESS) {
            obj = CommonUtils.getLBLinkHashMap();
        } // Neu server khong hoat dong -> Tra ve thong bao loi
        else {
            obj = CommonUtils.getServerStatusDescription();
        }
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return FunctionCommon.generateResponseJSON(errorCode, obj, null);
    }

    /**
     * 1. thuc hien goi truoc khi login de lay khoa cong khai dung de van chuyen
     * khoa aes
     *
     * @param req
     * @return
     */
    public String getTokenPublicKeyRSA(HttpServletRequest req) {
        EntityLog log = new EntityLog(req, CLASS_NAME);
        // Ghi log bat dau chuc nang
        LogUtils.logFunctionalStart(log);
        //tao he mat ma RSA bao ve khoa aes
        Date startDate = new Date();
        RSA keyRsa = SecurityControler.getKeyRsa();
        String strPublicKeyRsa = FunctionCommon.bytesToHex(keyRsa.getPublic_Key().getEncoded());
        String strPrivateKeyRsa = FunctionCommon.bytesToHex(keyRsa.getPrivate_Key().getEncoded());

        EntityUserGroup entityUserGR = new EntityUserGroup();
        entityUserGR.setStrPublicKey(strPublicKeyRsa);
        entityUserGR.setStrPrivateKey(strPrivateKeyRsa);
        //luu tru session truoc khi dang nhap chi bao gom thong tin khoa rsa
        String strJson = FunctionCommon.generateJSONBase(entityUserGR);
        //CommonControler.setDataCache(strPublicKeyRsa,strJson);
        LogActionControler aclog = new LogActionControler();
        if(!CommonControler.setDataCache_Mem(strPublicKeyRsa, strJson)){
           aclog.insertActionLog(-112L, "CREATERSA", "CREATERSA", req, "Loi Set Mem Khi Create RSAKEY", startDate, "", "");
           return FunctionCommon.generateResponseJSON(ErrorCode.SERVICE_UNAVAILABLE, null, null);
        }
        //tra ve client
        EntityUserGroup entityUserGRReponse = new EntityUserGroup();
        entityUserGRReponse.setStrPublicKey(strPublicKeyRsa);
        String strAesKeyGroup = AES.createAesKey();
        entityUserGRReponse.setStrAesKey(strAesKeyGroup);
        
        aclog.insertActionLog(-11L, "CREATERSA", "CREATERSA", req, "RSA SET MEM THANH CONG", startDate, "", "");
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, entityUserGRReponse, null);
    }

    /**
     * <b>Dang nhap</b><br>
     *
     * 1. Lay khoa RSA bi mat trong Memcached theo khoa RSA cong khai (Vi truoc
     * khi dang nhap, client phai goi 1 ham sinh cap khoa RSA, sau do cap khoa
     * RSA se duoc luu vao Memcached voi khoa cong khai la key con khoa bi mat
     * la value) 2. Giai ma khoa AES client gui len bang khoa RSA bi mat 3. Giai
     * ma du lieu bang khoa AES o tren 4. Kiem tra thong tin account: 4.1. Kiem
     * tra theo Voffice 1.0 4.2. Neu that bai thi kiem tra theo SSO 5. Lay thong
     * tin con lai theo ma nhan vien co duoc tu buoc tren
     *
     * @param request
     * @param rsaPublicKey
     * @param encodedAESKey
     * @param data
     * @param ios: kiem tra co phai ban ios khong
     * @return
     */
    public String login(HttpServletRequest request, String rsaPublicKey,
            String encodedAESKey, String data, String ios) {
        LOGGER.info("----------------- LOGIN -----------------");
        EntityLog log = new EntityLog(request);
        LogActionControler aclog = new LogActionControler();
        Date startDate = new Date();
        ErrorCode errorCode = null;
        String result = null;
        try {
            String cachedData = CommonControler.getDataCache_Mem(rsaPublicKey);
            Date endTime1 = new Date();
            if (cachedData != null) {
                EntityLoginSso itemLoginSso = null;
                EntityUserGroup itemEntityGroup = (EntityUserGroup) FunctionCommon
                        .convertJsonToObject(cachedData, EntityUserGroup.class);
                // Dung private key RSA de giai ma AES key
                Boolean isIos = false;
                if (ios != null && "1".trim().equals(ios)) {
                    isIos = true;
                }
                String aesKey = SecurityControler.decodeDataByRSA(itemEntityGroup
                        .getStrPrivateKey(), encodedAESKey, isIos);
                // Giai ma dataClient gui len
                data = SecurityControler.decodeDataByAes(aesKey, data);
                JSONObject json = new JSONObject(data);
                String[] keys = new String[]{
                    ConstantsFieldParams.STR_LOGINNAME,
                    ConstantsFieldParams.STR_LOGINPASS,
                    ConstantsFieldParams.LANGUAGE,
                    ConstantsFieldParams.VOF2_KEY,
                    ConstantsFieldParams.DEVICE_NAME,
                    "isNeedToken",
                    "image"
                };
                List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                // Ten dang nhap
                String loginName = listValue.get(0);
                log.setUserName(loginName);
                // Mat khau da ma hoa
                String password = listValue.get(1);
                // Ngon ngu
                String languageCode = listValue.get(2);
                if (CommonUtils.isEmpty(languageCode)) {
                    languageCode = "vi";
                }
                // Mat khau chua ma hoa
                String vof2Key = listValue.get(3);
                String deviceName = listValue.get(4);
                Integer isNeedToken = null;
                if (!CommonUtils.isEmpty(listValue.get(5))) {
                    isNeedToken = Integer.parseInt(listValue.get(5));
                }
                // Lay thong tin anh gioi thieu
                String strImage = listValue.get(6);
                EntityImage image = null;
                if (!CommonUtils.isEmpty(strImage)) {
                    Gson gson = new Gson();
                    image = gson.fromJson(strImage, EntityImage.class);
                    image.setLanguage(languageCode);
                }
                
                //datnv5: kiem duyet thong tin dang nhap tu cac he thong tu dong
                //loc va chan nhung he thong khong hop le
                Boolean isPreventClientRemote = isPreventClientRemote(request,listValue);
                if(isPreventClientRemote){
                    //tra ve thong tin nguoi dung bi chan
                    return FunctionCommon.generateResponseJSON(ErrorCode.ACCOUNT_CLIENTREMOTE_PREVENT, null, null);
                }
                
                EntityUser itemUserLogin;
                Vof2_EntityUser vof2_ItemUser;
                itemEntityGroup.setStrAesKey(aesKey);
                itemEntityGroup.setStrSessionId(null);
                //gan lai de so sanh pass
                itemEntityGroup.setStrPassVof1(LoginSecurity.encrypt(vof2Key));
                itemEntityGroup.setStrPassVof2(vof2Key);
                // Login 1.0
                itemUserLogin = getUserInforByLoginNameAndPass(loginName, password);
                Date endTime2 = new Date();
                LOGGER.error(loginName + " - LOGIN 1.0 ---------- "
                        + (endTime2.getTime() - endTime1.getTime()));
                HttpSession session = request.getSession(true);
                String strSessionId = session.getId();
                // Login 1.0 thanh cong
                if (itemUserLogin != null && itemUserLogin.getUserId() != null) {
                    LOGGER.error(" ---------- 1.0 Thanh cong");
                    CommonControler.removeDataCache_Mem(rsaPublicKey);
                    itemUserLogin.setIsLoginCardId(itemUserLogin.getStrCardNumber());
                    vof2_ItemUser = vof2_getUserInforByCarId(itemUserLogin.getStrCardNumber());
                    if (itemUserLogin.getStrCardNumber() == null || vof2_ItemUser == null
                            || vof2_ItemUser.getLstVhrOrgAll() == null) {

                        //truong hop khong co tai khoan tren 2 theo ma nhan vien
                        errorCode = ErrorCode.ACCOUNT_SSO_INACTIVATED_OR_LOCKED;

                        result = FunctionCommon.generateResponseJSON(errorCode, null, null);
                        LOGGER.error(" --Danh nhap 1.0 thanh cong, 2.0 KHONG TON TAI---- result=" + result);
                    } else {
                        vof2_ItemUser.setUserIdVof1(itemUserLogin.getUserId());
                        vof2_ItemUser.setStrCardNumber(itemUserLogin.getStrCardNumber());
                        vof2_ItemUser.setLanguageCode(languageCode);
                        itemEntityGroup.setItemEntityUser(itemUserLogin);
                        VHROrgDAO VHROrgDAO = new VHROrgDAO();
                        vof2_ItemUser.setIsLeafGroup(VHROrgDAO.isLeafGroup(vof2_ItemUser.getSysOrgId()));
                        itemEntityGroup.setVof2_ItemEntityUser(vof2_ItemUser);
                        itemEntityGroup.setStrSessionId(strSessionId);
                        itemEntityGroup.setLoginBySSo("0");
                        // Set data into memcached
                        String strJson = FunctionCommon.generateJSONBase(itemEntityGroup);
                        CommonControler.setDataCache_Mem(strSessionId, strJson);
                        aclog.insertActionLog(vof2_ItemUser.getUserId(), loginName,
                                "UserControler.login", request, "VOFFICE1_OK", startDate, deviceName, "");
                    }
                } // Login 1.0 that bai
                else {
                    LOGGER.error(" ---------- 1.0 That bai");

                    itemLoginSso = new EntityLoginSso();
                    try {
                        Date loginSSOTime = new Date();
                        itemLoginSso = vof2_CheckLoginVHR(loginName, vof2Key);

                        if (itemLoginSso != null && itemLoginSso.getUserId() != null
                                && loginName != null) {
                            aclog.insertActionLog(itemLoginSso.getUserId(), loginName,
                                    "UserControler.vof2_CheckLoginVHR", request, "SSO_REQUEST_OK",
                                    loginSSOTime, deviceName, "");
                        }
                    } catch (Exception e) {
                        LOGGER.error("Loi! thuc hien goi service sso ", e);
                        itemLoginSso = null;
                    }

                    // Login SSO thanh cong
                    if (itemLoginSso != null && itemLoginSso.getIsLoginSso()) {
                        LOGGER.error(" ---------- SSO Thanh cong");
//                        itemEntityGroup.setStrSessionId(strSessionId);
                        vof2_ItemUser = vof2_getUserInforByCarId(itemLoginSso.getStaffCode());
                        if (vof2_ItemUser == null || vof2_ItemUser.getLstVhrOrgAll() == null) {
                            //truong hop khong co tai khoan tren 2 theo ma nhan vien
                            errorCode = ErrorCode.ACCOUNT_SSO_INACTIVATED_OR_LOCKED;
                            itemEntityGroup = null;
                            result = FunctionCommon.generateResponseJSON(errorCode, null, null);
                            LOGGER.error("User bi khoa tren 2.0" + result);
//                            LOGGER.error(" --THONG TIN TAI KHOAN 2.0 KHONG TON TAI---- result=" + result);
                        } else {
                            //Lay thong tin Id user 1.0 neu het hieu luc tren 1.0
                            UserDAO userD = new UserDAO();
                            EntityUser entityUser = userD.getUserNotEnableVof1ByCardId(itemLoginSso.getStaffCode());
                            if (entityUser != null) {
                                vof2_ItemUser.setUserIdVof1(entityUser.getUserId());
                            }
                            itemEntityGroup.setStrSessionId(strSessionId);
                            vof2_ItemUser.setStrCardNumber(itemLoginSso.getStaffCode());
                            vof2_ItemUser.setLanguageCode(languageCode);
                            vof2_ItemUser.setTimeToDateExpire(itemLoginSso.getTimeToPassExpire());
                            if (vof2_ItemUser.getSysOrgId() != null) {
                                VHROrgDAO VHROrgDAO = new VHROrgDAO();
                                vof2_ItemUser.setIsLeafGroup(VHROrgDAO.isLeafGroup(vof2_ItemUser.getSysOrgId()));
                            }
                            itemEntityGroup.setVof2_ItemEntityUser(vof2_ItemUser);
                            // Lay thong tin 1.0 theo ma nhan vien
                            itemUserLogin = getUserInforByCarId(itemLoginSso.getStaffCode());

                            // Co thong tin nguoi dung tren he thong 1.0 va cardId khong rong
                            if (itemUserLogin != null && !itemUserLogin.getStrCardNumber().isEmpty()) {
                                itemUserLogin.setIsLoginCardId(itemLoginSso.getStaffCode());
                                HttpSessionCollector.remove(rsaPublicKey);
                                itemEntityGroup.setStrSessionId(strSessionId);
                                itemEntityGroup.setItemEntityUser(itemUserLogin);
                                itemEntityGroup.setVof2_ItemEntityUser(vof2_ItemUser);
//                                aclog.insertActionLog(vof2_ItemUser.getUserId(), loginName,
//                                        "UserControler.login", request, "SSO_OK", startDate, deviceName, "");
                            } else {
                                LOGGER.error("User ko ton tai tren 1.0" + itemLoginSso.getStaffCode());
                            }
                            // Set data into memcached
                            itemEntityGroup.setLoginBySSo("1");
                            String strJson = FunctionCommon.generateJSONBase(itemEntityGroup);
                            CommonControler.setDataCache_Mem(strSessionId, strJson);
                        }

                    } // Login SSO that bai
                    else {
                        LOGGER.error(" ---------- SSO That bai");
                        if (loginName != null) {
                            aclog.insertActionLog(-10L, loginName, "UserControler.login",
                                    request, "SSO_SAITAIKHOAN", startDate, deviceName, "");
                        }
                        itemEntityGroup = null;
                    }
                }
                if (itemEntityGroup != null) {
                    // Gan token
                    generateToken(itemEntityGroup, isNeedToken);
                    // Lay thong tin anh gioi thieu
                    ImageDAO imageDAO = new ImageDAO();
                    Map<String, List<EntityImage>> mapImage = imageDAO.getImageList(
                            itemEntityGroup.getUserId2(), image);
                    itemEntityGroup.setMapImage(mapImage);
                }
                
                // Xet du lieu tra ve client
                EntityUser resultEntity;
                if (!CommonUtils.isEmpty(ios) && ("3".trim().equals(ios) || "1".trim().equals(ios))) {
                    //tra ve cho mobile
                    if (CommonUtils.isEmpty(result)) {
                        errorCode = ErrorCode.SUCCESS;
                        EntityUserGroup resutlFullResponse = itemEntityGroup;
                        if (resutlFullResponse != null) {
                            resutlFullResponse.setStrPrivateKey(null);
                            resutlFullResponse.setStrPassVof1(null);
                            resutlFullResponse.setStrPassVof2(null);
                            resutlFullResponse.setStrBlackListPass(getStringBlackListPass());
                            result = FunctionCommon.generateResponseJSON(errorCode, resutlFullResponse, aesKey);
                        } else {
                            EntityLoginSso entityLoginSso = null;
                            if (itemLoginSso != null && itemLoginSso.getErrCode() != null) {
                                entityLoginSso = new EntityLoginSso();
                                entityLoginSso.setErrCode(itemLoginSso.getErrCode());
                                entityLoginSso.setDetailErr(itemLoginSso.getDetailErr());
                            }
                            errorCode = ErrorCode.ERR_NOSESSION;
                            result = FunctionCommon.generateResponseJSON(errorCode, entityLoginSso, null);
                        }
                    }

                } else if (itemEntityGroup != null
                        && itemEntityGroup.getStrSessionId() != null) {
                    resultEntity = itemEntityGroup.getItemEntityUser();
                    Vof2_EntityUser entityUserVof2 = itemEntityGroup.getVof2_ItemEntityUser();
                    if (resultEntity == null) {
                        resultEntity = new EntityUser();
                        resultEntity.setStrCardNumber(itemEntityGroup.getVof2_ItemEntityUser().getStrCardNumber());
                        resultEntity.setLoginName(itemEntityGroup.getVof2_ItemEntityUser().getLoginName());                        
                        resultEntity.setMobileNumber(itemEntityGroup.getVof2_ItemEntityUser().getMobileNumber());                        
                    }
                    // Do user2 bang null thi user_group bi gan lai gia tri null
                    // nen user_group ma # null thi user2 cung # null
                    resultEntity.setStrSessionId(itemEntityGroup.getStrSessionId());
                    resultEntity.setListManagementVhrOrg(entityUserVof2.getListManagementVhrOrg());
                    resultEntity.setListAssistantVhrOrg(entityUserVof2.getListAssistantVhrOrg());
                    resultEntity.setListSecretaryOrg(entityUserVof2.getListSecretaryOrg());
                    // Lay don vi mac dinh thi truong nuoc ngoai
                    resultEntity.setForeignVhrOrg(entityUserVof2.getForeignVhrOrg());
                    // Loai van ban danh dau dong bo ve ERP
                    resultEntity.setTypeTextMarkSync(entityUserVof2.getTypeTextMarkSync());
                    // Lay danh sach don vi user lam TT, LD co is_default in 1,2
                    resultEntity.setListManagementVhrOrgIsDefault(entityUserVof2.getListManagementVhrOrgIsDefault());
                    resultEntity.setListSecretaryVhrOrg(entityUserVof2.getListSecretaryVhrOrg());
                    // Danh sach don vi co vai tro is_default in 1,2
                    resultEntity.setLstVhrOrgIsDefault(entityUserVof2.getLstVhrOrgIsDefault());
                    resultEntity.setConfigMeetingRoom(entityUserVof2.getConfigMeetingRoom());
                    resultEntity.setListOrgPersonReport(entityUserVof2.getListOrgPersonReport());
                    errorCode = ErrorCode.SUCCESS;
                    result = FunctionCommon.generateResponseJSON(errorCode, resultEntity, aesKey);
                } else {
                    EntityLoginSso entityLoginSso = null;
                    if (itemLoginSso != null && itemLoginSso.getErrCode() != null) {
                        entityLoginSso = new EntityLoginSso();
                        entityLoginSso.setErrCode(itemLoginSso.getErrCode());
                        entityLoginSso.setDetailErr(itemLoginSso.getDetailErr());
                    }

                    LOGGER.error("Khong lay dc session cua dang nhap");
                    errorCode = ErrorCode.ERR_NOSESSION;
                    result = FunctionCommon.generateResponseJSON(errorCode, entityLoginSso, null);
                }
            } else {
                LOGGER.error(" ---------- Cache data = null");
                Date loginSSOTime = new Date();
                aclog.insertActionLog(-13L, "datnv5",
                                    "UserControler.vof2_CheckLoginVHR", request, "LOGINERRMEMCACHE",
                                    loginSSOTime, "", "");
                errorCode = ErrorCode.ERR_NOSESSION;
                result = FunctionCommon.generateResponseJSON(errorCode, null, null);
            }
        } // Du lieu gui len khong dung dinh dang JSON
        catch (JSONException ex) {
            LOGGER.error(ex.getMessage(), ex);
            errorCode = ErrorCode.INPUT_INVALID;
            result = FunctionCommon.generateResponseJSON(errorCode, null, null);
        } // Loi Server
        catch (NumberFormatException | JsonSyntaxException ex) {
            LOGGER.error(ex.getMessage(), ex);
            errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
            result = FunctionCommon.generateResponseJSON(errorCode, null, null);
        }
        if (errorCode != null && errorCode == ErrorCode.SUCCESS) {
            log.setDescription("LOGIN_SUCCESS");
        } else {
            log.setDescription("LOGIN_FAIL");
        }
        LogUtils.logLogin(log);
        Date loginSSOTime = new Date();
        aclog.insertActionLog(-14L, "ENDLOGIN",
                                    "UserControler.login", request, result,
                                    loginSSOTime, "", "");
        return result;
    }

    /**
     * Thuc hien logout he thong voi tung user
     *
     * @param req
     * @return
     */
    public String logOut(HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        // Ghi log bat dau chuc nang
        LogUtils.logFunctionalStart(log);
        String strResult = "";
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            String strKeyLogin = dataSessionGR.getStrSessionId();
            CommonControler.removeDataCache_Mem(strKeyLogin);
        }
        // Ghi log ket thuc chuc nang
        LogUtils.logFunctionalEnd(log);
        return strResult;
    }

    //==================danh sach ham ho tro==================================
    /**
     * lay thong tin nguoi dung thong qua id nguoi dung
     *
     * @param userId
     * @return
     */
    public EntityUser getUserInfor(Long userId) {
        UserDAO userD = new UserDAO();
        return userD.getUserInfor(userId);
    }

    /**
     * lay thong tin nguoi dung thong qua ma nhan vien nguoi dung
     *
     * @param strCarId
     * @return
     */
    public EntityUser getUserInforByCarId(String strCarId) {
        UserDAO userD = new UserDAO();
        return userD.getUserInforByCardId(strCarId);
    }

    /**
     * Lay thong tin thong qua dang nhap
     *
     * @param strLoginName
     * @param strPass
     * @return
     */
    private EntityUser getUserInforByLoginNameAndPass(Object strLoginName, Object strPass) {
        UserDAO userD = new UserDAO();
        return userD.getUserByLoginNameAndPass(strLoginName, strPass);
    }

    /**
     * lay danh sach blackList
     *
     * @return
     */
    private String getStringBlackListPass() {
        UserDAO userD = new UserDAO();
        return userD.getStringBlackListPass();
    }

    /**
     * lay thong tin ca nhan dang nhap tu voffice data 2.0
     *
     * @param strCarId
     * @return
     */
    public static Vof2_EntityUser vof2_getUserInforByCarId(String strCarId) {
        if (strCarId == null || strCarId.trim().length() <= 0) {
            return null;
        }
        UserDAO userD = new UserDAO();
        Vof2_EntityUser result = userD.vof2_GetUserInforByCardId(strCarId);
        return result;
    }
    
    /**
     * Thuc hien check dang nhap tren he thong VHR
     *
     * @param strIdCardUser
     * @param strPass
     * @return
     */
    public static EntityLoginSso vof2_CheckLoginVHR(Object strIdCardUser, Object strPass) {
        //check cau hinh dang nhap theo sso cu hay service resfull moi
        String jsonItemConfigLogin = FunctionCommon.getValueFromConfigDataBase("LOGIN_SSO");
        Boolean isLoginResfull = false;
        String strUrlResfull = "";
        String signingKey = "";
        String encryptionKey = "";
        if(jsonItemConfigLogin!=null && jsonItemConfigLogin.trim().length()>5){
            Object valueItem = FunctionCommon.convertJsonToObject(
                    jsonItemConfigLogin, EntityLoginSso.class);
            EntityLoginSso itemLoginSso = null;
            if(valueItem != null){
                itemLoginSso = (EntityLoginSso) valueItem;
            }
            if(itemLoginSso!=null && itemLoginSso.getIsResfull() == 1){
                //thuc hien login bang resfull
                isLoginResfull = true;
                strUrlResfull = itemLoginSso.getUrlSsoRes();
                signingKey = itemLoginSso.getSigningKey();
                encryptionKey = itemLoginSso.getEncryptionKey();
            }
        }
        EntityLoginSso result = null;
        if(isLoginResfull){
            //thuc hien login bang resfull api
            EntityLoginSso itemResult = SSOResfullCallHttpConnect.postRequestToServerLogin(
                    strUrlResfull, strIdCardUser.toString(),strPass.toString(),signingKey,encryptionKey);
            return itemResult;
        }
        
        //truong hop login cu
        try {
            SSORunnable ssoRunnable = new SSORunnable(strIdCardUser.toString(),
                    strPass.toString());
            Response responseRequest = ssoRunnable.getLoginServiceSso();
            Integer intCheckLogin;
            if (responseRequest != null && responseRequest.getErrorCode() != null) {
                intCheckLogin = responseRequest.getErrorCode().getCode();
                if (intCheckLogin == 0) {
                    //login  thanh cong
                    String strXmlData = responseRequest.getErrorCode().getDescription();
                    result = new EntityLoginSso();
                    if (strXmlData != null) {
                        String strResults = getTagValue(strXmlData, "Results");
                        String strUserData = getTagValue(strResults, "UserData");
                        UserDataSsoEntity userDataSsoEntity
                                = convertXmlDataToObjUserSso(strUserData);

                        result.setIdentytiCard(userDataSsoEntity.getIDENTITY_CARD());
                        result.setStaffCode(userDataSsoEntity.getSTAFF_CODE());
                        result.setUserId(userDataSsoEntity.getUSER_ID());
                        result.setUserName(userDataSsoEntity.getUSER_NAME());
                        result.setIsLoginSso(true);
                        result.setTimeToPassExpire(userDataSsoEntity.getTIME_TO_PASSWORD_EXPIRE());
                        result.setErrCode(intCheckLogin);//them ma loi tra ve
                    } else {
                        //no data
                        result = new EntityLoginSso();
                        result.setIsLoginSso(false);
                        result.setErrCode(intCheckLogin);//them ma loi tra ve
                    }
                } else {
                    //login loi
                    result = new EntityLoginSso();
                    result.setIsLoginSso(false);
                    result.setErrCode(intCheckLogin);//them ma loi tra ve
                    String strDetailerr = responseRequest.getErrorCode().getDescription() != null
                            ? responseRequest.getErrorCode().getDescription() : "";
                    result.setDetailErr(strDetailerr);
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Loi! thuc hien goi service sso ", ex);
        }
        return result;
    }

    /**
     * Convert dataxml to obj UserDataSsoEntity
     *
     * @param strXml
     * @return
     */
    private static UserDataSsoEntity convertXmlDataToObjUserSso(String strXml) {

        UserDataSsoEntity userDataSsoEntity;
        try {
            JAXBContext jc = JAXBContext.newInstance(UserDataSsoEntity.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            StreamSource streamSource = new StreamSource(new StringReader(strXml));

            JAXBElement<UserDataSsoEntity> je = unmarshaller.unmarshal(streamSource,
                    UserDataSsoEntity.class);

            userDataSsoEntity = (UserDataSsoEntity) je.getValue();
//            System.out.println("First Name:- " + userDataSsoEntity.getEMAIL());
        } catch (JAXBException ex) {
            userDataSsoEntity = new UserDataSsoEntity();
            LOGGER.error("Loi! UserDataSsoEntity: ", ex);
        }
        return userDataSsoEntity;
    }

    public static String getTagValue(String xml, String tagName) {
        try {
            return xml.split("<" + tagName + ">")[1].split("</" + tagName + ">")[0];
        } catch (Exception e) {
            LOGGER.error("getTagValue - tagName: " + tagName, e);
            return "";
        }
    }

    /**
     * Lay thong tin ca nhan dang nhap
     *
     * @return
     */
    public String getUserInfor(HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                LogUtils.logFunctionalStart(log);

                Vof2_EntityUser userVof2 = dataSessionGR.getVof2_ItemEntityUser();
                //them thong tin la don vi nut la
                //cho chuc nang chuyen van ban ca nhan
                if (userVof2 != null) {
                    VHROrgDAO VHROrgDAO = new VHROrgDAO();
                    userVof2.setIsLeafGroup(VHROrgDAO.isLeafGroup(userVof2.getSysOrgId()));
                    UserDAO userDAO = new UserDAO();
                    userVof2.setManager(userDAO.getManager(userVof2.getUserId()));
                    userVof2.setTgd(userDAO.getTGD());
                    //vanpn check manager meeting
                    List<EntityVhrEmployee> listMeetingManager = userDAO.checkMeetingManager(userVof2.getUserId());
                    if (listMeetingManager != null && listMeetingManager.size() > 0) {
                        userVof2.setIsMeetingManager(1);
                    } else {
                        userVof2.setIsMeetingManager(0);
                    }
                    List<EntityApproveCalDept> listApproveCalDept = new ArrayList<>();
                    Long orgId = userVof2.getSysOrgId();
                    if (listMeetingManager != null) {
                        for (EntityVhrEmployee emp : listMeetingManager) {
                            EntityApproveCalDept entity = new EntityApproveCalDept();
                            entity.setDeptId(emp.getOrganizationId());
                            entity.setDeptName(emp.getOrgName());
                            if (emp.getOrganizationId().equals(orgId)) {
                                entity.setIsDefault(1);
                            } else {
                                entity.setIsDefault(0);
                            }
                            listApproveCalDept.add(entity);
                        }
                    }
                    userVof2.setListApproveCalDept(listApproveCalDept);
                }
                
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                        userVof2, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }
        return strResult;
    }

    /**
     * <b>Lay ca nhan</b><br>
     *
     * @param req
     * @param strData
     * @param isSecurity
     * @return
     */
    public String getListUser(HttpServletRequest req, String strData,
            String isSecurity) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(req);
        String cardId = "";
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getListUser (Lay ca nhan) - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        try {
            String strAesKeyDecode = userGroup.getStrAesKey();
            String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
            // Lay ma nhan vien
            cardId = userGroup.getCardId();
            log.setUserName(cardId);
            // Ghi log bat dau chuc nang
            log.setParamList(strDataClient);
            LogUtils.logFunctionalStart(log);
            //Lay prams client gui len de thuc hien yeu cau
            HashMap<String, Object> hmParams = new HashMap<>();
            hmParams.put(ConstantsFieldParams.START_RECORD, Long.class);
            hmParams.put(ConstantsFieldParams.PAGE_SIZE, Long.class);
            hmParams.put(ConstantsFieldParams.KEYWORD, String.class);
            hmParams.put(ConstantsFieldParams.TYPE, Long.class);
            hmParams.put(ConstantsFieldParams.IS_COUNT, Long.class);
            // Tham so lay nguoi chi trong don vi
            hmParams.put("onlyParentGroup", Long.class);
            // 220517 bo sung canh bao chuyen van ban, them documentId
            hmParams.put("documentId", Long.class);
            hmParams.put(ConstantsFieldParams.SEARCH_TYPE, Integer.class);

            HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, userGroup);
            Long startRecord = (Long) ((valueParams.get(ConstantsFieldParams.START_RECORD) != null) ? valueParams.get(ConstantsFieldParams.START_RECORD) : 0L);
            Long pageSize = (Long) ((valueParams.get(ConstantsFieldParams.PAGE_SIZE) != null) ? valueParams.get(ConstantsFieldParams.PAGE_SIZE) : 15L);
            String keywords = (String) ((valueParams.get(ConstantsFieldParams.KEYWORD) != null) ? valueParams.get(ConstantsFieldParams.KEYWORD) : "");
            Long type = (Long) ((valueParams.get(ConstantsFieldParams.TYPE) != null) ? valueParams.get(ConstantsFieldParams.TYPE) : 0L);
            Long isCount = (Long) ((valueParams.get(ConstantsFieldParams.IS_COUNT) != null) ? valueParams.get(ConstantsFieldParams.IS_COUNT) : 0L);
            Long onlyParentGroup = (Long) ((valueParams.get("onlyParentGroup") != null)
                    ? valueParams.get("onlyParentGroup") : 0L);
            // 220517 bo sung canh bao chuyen van ban, them documentId
            Long documentId = null;
            if (valueParams.get("documentId") != null) {
                documentId = (Long) valueParams.get("documentId");
            }
            // 12/01/2017 - ThangHT6
            // Bo sung bien de tim kiem nhan vien co vai tro trong don vi
            Integer searchType = (Integer) ((valueParams.get(ConstantsFieldParams.SEARCH_TYPE) != null)
                    ? valueParams.get(ConstantsFieldParams.SEARCH_TYPE) : 0);

//                System.out.println("getListUser: " + strDataClient);
            JSONObject json = new JSONObject(strDataClient);
            String[] keys = new String[]{"user"};
            Gson gson = new Gson();
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Vof2_EntityUser userVof2 = new Vof2_EntityUser();
            userVof2.setSysOrgId(148842L);
            if (listValue != null && listValue.size() > 0) {
                String strUser = listValue.get(0);
//                    System.out.println("user: " + strUser);
                userVof2 = gson.fromJson(strUser, Vof2_EntityUser.class);
                if (userVof2 != null) {
                    if (userVof2.getSysOrgId() == null) {
                        userVof2.setSysOrgId(148842L);
                    }
                } else {
                    userVof2 = new Vof2_EntityUser();
                    userVof2.setSysOrgId(148842L);
                }
            }
            //Begin Lay danh sach don vi lay lanh dao thu truong ::cuongnv
            List<Long> listOrg = new ArrayList<>();
            if (json.has(ConstantsFieldParams.LIST_ORG_ID)) {
                JSONArray listOrgId = FunctionCommon.jsonGetArray(ConstantsFieldParams.LIST_ORG_ID, strDataClient);
                if (listOrgId != null && listOrgId.length() > 0) {
                    for (int i = 0; i < listOrgId.length(); i++) {
                        try {
                            listOrg.add(Long.parseLong(listOrgId.get(i).toString()));
                        } catch (NumberFormatException e) {
//                            System.out.println(e.toString());
                        }
                    }
                }
            }
            //End
            StaffDAO staffDAO = new StaffDAO();
            if (Objects.equals(type, StaffDAO.GET_LIST_USER_CVGROUP)) {
                //Begin :: Lay danh dach nv : cuongnv
                if (isCount.equals(0L)) {
                    List<Vof2_EntityUser> result = (List<Vof2_EntityUser>) staffDAO.getListUserCvGroup(userVof2, startRecord,
                            pageSize, isCount, keywords, false, null);

                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                } else {
                    Long result = (Long) staffDAO.getListUserCvGroup(userVof2, startRecord,
                            pageSize, isCount, keywords, false, null);
                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                }
                //End
            } else if (Objects.equals(type, StaffDAO.GET_LIST_USER_MEETING_ASSISTANT)) {
                //Begin :: Lay danh dach nv : cuongnv
                if (isCount.equals(0L)) {
                    List<Vof2_EntityUser> result = (List<Vof2_EntityUser>) staffDAO.getListUserMeetingAssistant(userVof2, startRecord,
                            pageSize, isCount, keywords, true, listOrg);

                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                } else {
                    Long result = (Long) staffDAO.getListUserMeetingAssistant(userVof2, startRecord,
                            pageSize, isCount, keywords, true, listOrg);
                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                }
                //End
            } else if (Objects.equals(type, StaffDAO.GET_LIST_USER_MEETING_CONFIG)) {
                //Begin :: Lay danh dach lanh dao thu truong nv : cuongnv
                if (isCount.equals(0L)) {
                    List<Vof2_EntityUser> result = (List<Vof2_EntityUser>) staffDAO.getListUserMeetingConfig(userVof2, startRecord,
                            pageSize, isCount, keywords, true, listOrg);

                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                } else {
                    Long result = (Long) staffDAO.getListUserMeetingConfig(userVof2, startRecord,
                            pageSize, isCount, keywords, true, listOrg);
                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                }
                //End
            } else if (Objects.equals(type, StaffDAO.GET_LIST_STAFF_CALENDAR)) {
                //lay danh sach nhan vien cho chuc nang gan thanh phan tham gia lich hop
                //bo sung tim kiem nhieu don vi tren web
                List<Long> lstGroupId = new ArrayList<>();
                JSONArray arrGroup = FunctionCommon.jsonGetArray("lstGroupId", strDataClient);
                if (arrGroup != null && arrGroup.length() > 0) {
                    int arrGroupSize = arrGroup.length();
                    Long groupId;
                    for (int i = 0; i < arrGroupSize; i++) {
                        JSONObject innerObj = (JSONObject) arrGroup.get(i);
                        groupId = null;
                        if (innerObj.has("groupId") && innerObj.getString("groupId") != null) {
                            groupId = Long.parseLong(innerObj.getString("groupId").trim());
                        }
                        if (groupId != null) {
                            lstGroupId.add(groupId);
                        }
                    }
                }
                if (isCount.equals(0L)) {
                    List<EntityVhrEmployee> result = (List<EntityVhrEmployee>) staffDAO.getListUserForCalendar(userVof2, startRecord, pageSize,
                            isCount, keywords, lstGroupId);

                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                } else {
                    Long result = (Long) staffDAO.getListUserForCalendar(userVof2, startRecord, pageSize,
                            isCount, keywords, lstGroupId);
                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                }
            } // Bo sung tim kiem nhieu don vi tren web
            else {
                List<Long> lstGroupId = new ArrayList<>();
                JSONArray arrGroup = FunctionCommon.jsonGetArray("lstGroupId", strDataClient);
                if (arrGroup != null && arrGroup.length() > 0) {
                    int arrGroupSize = arrGroup.length();
                    Long groupId;
                    for (int i = 0; i < arrGroupSize; i++) {
                        JSONObject innerObj = (JSONObject) arrGroup.get(i);
                        groupId = null;
                        if (innerObj.has("groupId") && innerObj.getString("groupId") != null) {
                            groupId = Long.parseLong(innerObj.getString("groupId").trim());
                        }
                        if (groupId != null) {
                            lstGroupId.add(groupId);
                        }
                    }
                }
                Boolean isSelMutiGroup = false;
                if (!lstGroupId.isEmpty()) {
                    //la tim kiem theo nhieu don vi
                    isSelMutiGroup = true;
                }
                if (isCount.equals(0L)) {
                    List<EntityVhrEmployee> result = (List<EntityVhrEmployee>) staffDAO.getListUserAdvance(userVof2, startRecord,
                            pageSize, type, isCount, keywords, onlyParentGroup,
                            isSelMutiGroup, lstGroupId, searchType, false);
                    // 220517 bo sung canh bao chuyen van ban, them documentId
                    if (documentId != null && result != null && !result.isEmpty()) {
                        staffDAO.getListStaffReceiverDoc(documentId, result);
                    }
                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                } else {
                    Long result = (Long) staffDAO.getListUserAdvance(userVof2,
                            startRecord, pageSize, type, isCount, keywords,
                            onlyParentGroup, isSelMutiGroup, lstGroupId, searchType, false);
                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                }
            }
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
        } catch (Exception ex) {
            LOGGER.error("getListUser (Lay ca nhan) - Exception - username: "
                    + cardId, ex);
            strResult = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
        return strResult;
    }

    /**
     *
     * Tim kiem nhan vien theo nhom nhan vien
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getOrgInfoById(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        //Id user vof2
        Long userIdVof2 = userGroup.getVof2_ItemEntityUser().getUserId();
        if (userIdVof2 == null) {
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
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
                ConstantsFieldParams.TASK_GROUP_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);

            Long groupId = null;
            if (!CommonUtils.isEmpty(listValue.get(0))) {
                groupId = Long.parseLong(listValue.get(0));
            }
            if (groupId != null) {
                OrgDAO orgDAO = new OrgDAO();
                Object result = orgDAO.getOrgInfoById(groupId);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_ERROR, ErrorCode.INPUT_INVALID, ex.getMessage(), data, isSecurity);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * <b>Lay ca nhan nhieu don vi</b><br>
     *
     * @param req
     * @param strData
     * @param isSecurity
     * @return
     */
    public String getListUserMutiGroup(HttpServletRequest req, String strData,
            String isSecurity) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(req);
        String cardId = "";
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getListUserMutiGroup (Lay ca nhan nhieu don vi) - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        try {
            String strAesKeyDecode = userGroup.getStrAesKey();
            String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
            // Lay ma nhan vien
            cardId = userGroup.getCardId();
            log.setUserName(cardId);
            // Ghi log bat dau chuc nang
            log.setParamList(strDataClient);
            LogUtils.logFunctionalStart(log);
            //Lay prams client gui len de thuc hien yeu cau
            HashMap<String, Object> hmParams = new HashMap<>();
            hmParams.put(ConstantsFieldParams.START_RECORD, Long.class);
            hmParams.put(ConstantsFieldParams.PAGE_SIZE, Long.class);
            hmParams.put(ConstantsFieldParams.KEYWORD, String.class);
            hmParams.put(ConstantsFieldParams.TYPE, Long.class);
            hmParams.put(ConstantsFieldParams.IS_COUNT, Long.class);
            hmParams.put(ConstantsFieldParams.SEARCH_TYPE, Integer.class);
            //hmParams.put("onlyParentGroup", Long.class); //tham so lay nguoi chi trong don vi
            hmParams.put("isReplaceSigner", Integer.class);
            HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, userGroup);
            Long startRecord = (Long) ((valueParams.get(ConstantsFieldParams.START_RECORD) != null) ? valueParams.get(ConstantsFieldParams.START_RECORD) : 0L);
            Long pageSize = (Long) ((valueParams.get(ConstantsFieldParams.PAGE_SIZE) != null) ? valueParams.get(ConstantsFieldParams.PAGE_SIZE) : 15L);
            String keywords = (String) ((valueParams.get(ConstantsFieldParams.KEYWORD) != null) ? valueParams.get(ConstantsFieldParams.KEYWORD) : "");
            Long type = (Long) ((valueParams.get(ConstantsFieldParams.TYPE) != null) ? valueParams.get(ConstantsFieldParams.TYPE) : 0L);
            Long isCount = (Long) ((valueParams.get(ConstantsFieldParams.IS_COUNT) != null) ? valueParams.get(ConstantsFieldParams.IS_COUNT) : 0L);
            Integer searchType = (Integer) ((valueParams.get(ConstantsFieldParams.SEARCH_TYPE) != null) ? valueParams.get(ConstantsFieldParams.SEARCH_TYPE) : 0);
            boolean isReplaceSigner = valueParams.get("isReplaceSigner") != null
                    && (Integer) valueParams.get("isReplaceSigner") == 1;
            //Long onlyParentGroup = (Long) ((valueParams.get("onlyParentGroup") != null)
            //        ? valueParams.get("onlyParentGroup") : 0L);
//                System.out.println("getListUserMutiGroup: " + strDataClient);
            List<Long> lstGroupId = new ArrayList<>();
            JSONArray arrGroup = FunctionCommon.jsonGetArray("lstGroupId", strDataClient);

            if (arrGroup != null && arrGroup.length() > 0) {
                int arrGroupSize = arrGroup.length();
                Long groupId;
                JSONObject obj;
                for (int i = 0; i < arrGroupSize; i++) {
                    obj = arrGroup.getJSONObject(i);
                    groupId = null;
                    if (obj.has("groupId") && obj.getString("groupId") != null) {
                        groupId = Long.parseLong(obj.getString("groupId").trim());
                    }
                    if (groupId != null) {
                        lstGroupId.add(groupId);
                    }
                }
            }
            StaffDAO staffDAO = new StaffDAO();
            Boolean isSelMutiGroup = false;
            if (!lstGroupId.isEmpty()) {
                //la tim kiem theo nhieu don vi
                isSelMutiGroup = true;
            }
            if (isCount.equals(0L)) {
                List<Vof2_EntityUser> result = (List<Vof2_EntityUser>) staffDAO
                        .getListUserAdvance(null, startRecord, pageSize, type,
                                isCount, keywords, 0L, isSelMutiGroup, lstGroupId,
                                searchType, isReplaceSigner);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
            } else {
                Long result = (Long) staffDAO.getListUserAdvance(null, startRecord,
                        pageSize, type, isCount, keywords, 0L, isSelMutiGroup,
                        lstGroupId, searchType, isReplaceSigner);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
            }
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
        } catch (Exception ex) {
            LOGGER.error("getListUserMutiGroup (Lay ca nhan nhieu don vi)"
                    + " - Exception - username: " + cardId, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
        return strResult;
    }
    
    /** Dinh dang token ben SmartOffice */
    private static final String SMART_OFFICE_TOKEN = "{\"username\":\"%s\",\"password\":\"%s\",\"timestamp\":\"%s\"}";
    
    /** Key luu tru duong dan file der PublicKey SmartOffice */
    private static final String SMARTOFFICE_RSA_PUBLIC_KEY_DER_FILE = "smartoffice.rsa.public.key.der.file";
    
    /** Dinh dang token Voffice truyen cho SmartOffice */
    private static final String VOFFICE_TOKEN = "{\"username\":\"%s\",\"expires\":%d}";

    /**
     * <b>Ma hoa account SSO de giao tiep voi he thong SmartOffice</b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String encryptAccountSSOForSmartOffice(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        if ((user1 == null || user1.getUserId() == null)
                && (user2 == null || user2.getUserId() == null)) {
            LOGGER.error("encryptAccountSSOForSmartOffice - user hoac userId tren he thong 1&2 null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
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
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.USERNAME,
                ConstantsFieldParams.PASSWORD
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String username = listValue.get(0);
            String password = listValue.get(1);
            String timeStamp = (new Date()).getTime() + "";
            String token = String.format(SMART_OFFICE_TOKEN, username, password, timeStamp);
            String derFile = CommonUtils.getAppConfigValue(SMARTOFFICE_RSA_PUBLIC_KEY_DER_FILE);
            byte[] encryptBytes = RSA.encryptData(token, RSA.loadPublicKeyFromDerFile(derFile));
            String encryptToken = DatatypeConverter.printHexBinary(encryptBytes);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, encryptToken, aesKey);
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error("encryptAccountSSOForSmartOffice - Exception: ", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        } catch (Exception ex) {
            LOGGER.error("encryptAccountSSOForSmartOffice - Exception: ", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    /**
     * <b>Dong bo danh sach yeu thich</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String syncFavouriteList(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Sesstion timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("syncFavouriteList (Dong bo danh sach yeu thich) - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra thong tin userId tren he thong 2
        if (!userGroup.checkUserId2()) {
            LOGGER.error("syncFavouriteList (Dong bo danh sach yeu thich) -"
                    + " Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Lay id nguoi dung
        Long userId = userGroup.getVof2_ItemEntityUser().getUserId();
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
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.ADDITIONAL_FAVOURITE_LIST};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String strAdditionalFavouriteList = listValue.get(0);
            // Parse chuoi strAdditionalFavouriteList sang danh sach favourite thay doi
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<EntityFavourite>>() {
            }.getType();
            List<EntityFavourite> additionalFavouriteList = gson.fromJson(
                    strAdditionalFavouriteList, listType);
            FavouriteDAO favouriteDAO = new FavouriteDAO();
            List<EntityFavourite> result = favouriteDAO.syncFavouriteList(
                    userId, additionalFavouriteList);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            // Neu danh sach yeu thich da duoc dong bo la null
            // -> Tra ve thong bao loi server
            if (result == null) {
                LOGGER.error("syncFavouriteList (Dong bo danh sach yeu thich) -"
                        + " result = null - username: " + cardId + "\ndata: " + data);
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } // Loi server
        catch (Exception ex) {
            LOGGER.error("syncFavourite (Dong bo danh sach yeu thich) - Exception"
                    + " - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * @author HaNH
     * <b> Lay danh sach Lanh dao, thu truong cua cac don vi </b>
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getLeaderByOrg(HttpServletRequest request,
            String data, String isSecurity) {
        
        String[] keys = new String[] {
            ConstantsFieldParams.LIST_ORG_ID
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.returnResultAfterLogSessionTimeout(userGroup);
        }
        // Khong co thong tin tren he thong 2
        if (!userGroup.checkUserId2()) {
            return FunctionCommon.returnResultAfterLogNoInfo(userGroup.getUserId2());
        }
        List<Long> listPoliticalOrgId = userGroup.getVof2_ItemEntityUser()
                .getListPoliticalAssistantOrgId();
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            String strOrgId = listValue.get(0);
            List<Long> listOrgId = new ArrayList<>();
            if (!CommonUtils.isEmpty(strOrgId)) {
                JSONArray jaOrgId = new JSONArray(strOrgId);
                if (jaOrgId.length() > 0) {
                    for (int i = 0; i < jaOrgId.length(); i++) {
                        listOrgId.add(jaOrgId.getLong(i));
                    }
                }
            } else {
                return FunctionCommon.returnResultAfterLogInputInvalid(
                        FunctionCommon.responseResult(ErrorCode.ERR_NODATA, null, null),
                        userGroup.getUserId2(), ConstantsFieldParams.LIST_ORG_ID);
            }
            StaffDAO staffDAO = new StaffDAO();
            List<EntityVhrEmployee> listUser = null;
            if (!CommonUtils.isEmpty(listOrgId)) {
                Long userId = null;
                if (!CommonUtils.isEmpty(listPoliticalOrgId)
                        && listPoliticalOrgId.contains(listOrgId.get(0))) {
                    userId = -1L;
                }
                listUser = staffDAO.getLeaderByOrg(userId, listOrgId);
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, listUser, userGroup);
        } catch (Exception ex) {
            return FunctionCommon.returnResultAfterLog(userGroup, ex);
        }
    }

    /**
     * <b>Xac thuc qua Passport</b><br>
     *
     * @param request
     * @param rsaPublicKey
     * @param encodedAESKey
     * @param data
     * @return
     */
    public String loginViaPassport(HttpServletRequest request, String rsaPublicKey,
            String encodedAESKey, String data) {

        Date startTime = new Date();
        LOGGER.info("----------------- loginViaPassport -----------------");
        EntityLog log = new EntityLog(request);
        log.setDescription("LOGIN_FAIL");
        String username = "";
        String result;
        try {
            String cachedData = CommonControler.getDataCache_Mem(rsaPublicKey);
            Date endTime1 = new Date();
            Long timeToGetDataFromMemcached = endTime1.getTime() - startTime.getTime();
            if (cachedData != null) {
                LOGGER.info("1.Lay du lieu trong Memcached thanh cong: "
                        + timeToGetDataFromMemcached + " ms");
                LogActionControler aclog = new LogActionControler();
                EntityUserGroup userGroup = (EntityUserGroup) FunctionCommon
                        .convertJsonToObject(cachedData, EntityUserGroup.class);
                // Dung private key RSA de giai ma AES key
                String aesKey = SecurityControler.decodeDataByRSA(userGroup
                        .getStrPrivateKey(), encodedAESKey, false);
                userGroup.setStrAesKey(aesKey);
                // Giai ma dataClient gui len
                data = SecurityControler.decodeDataByAes(aesKey, data);
                JSONObject json = new JSONObject(data);
                String[] keys = new String[]{
                    ConstantsFieldParams.TICKET,
                    ConstantsFieldParams.DEVICE_NAME,
                    ConstantsFieldParams.WEB_URL
                };
                List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                // Chuoi ticket passport tra ve
                String ticket = listValue.get(0);
                // Thiet bi
                String deviceName = listValue.get(1);
                // Lay link ung dung web
                String webUrl = listValue.get(2);
                // Lay session id tu request
                HttpSession session = request.getSession(true);
                String strSessionId = session.getId();
                // Xac thuc ticket qua passport
                username = PassportConnector.authenticate(ticket, webUrl);
                log.setUserName(username);
                Date endTime11 = new Date();
                Long timeToValidateTicket = endTime11.getTime() - endTime1.getTime();
                // Xac thuc ticket thanh cong tren passport
                if (!CommonUtils.isEmpty(username)) {
                    LOGGER.info("1.1.Validate ticket thanh cong: "
                            + timeToValidateTicket + " ms");
                    userGroup.setStrSessionId(strSessionId);
                    // Lay thong tin 1.0 theo ma nhan vien
                    EntityUser user1 = getUserInforByCarId(username);
                    Date endTime111 = new Date();
                    Long timeToGetInfo1 = endTime111.getTime() - endTime11.getTime();
                    LOGGER.info("1.1.1.Username: " + username + " - Lay thong tin 1.0: "
                            + timeToGetInfo1 + " ms");
                    // Lay thong tin 2.0 theo ma nhan vien
                    Vof2_EntityUser user2 = vof2_getUserInforByCarId(username);
                    Date endTime112 = new Date();
                    Long timeToGetInfo2 = endTime112.getTime() - endTime111.getTime();
                    LOGGER.info("1.1.2.Username: " + username + " - Lay thong tin 2.0: "
                            + timeToGetInfo2 + " ms");
                    user2.setStrCardNumber(username);
//                    user2.setLanguageCode("vi");
                    //Set userId1

                    userGroup.setVof2_ItemEntityUser(user2);
                    // Co thong tin nguoi dung tren he thong 1.0 va cardId khong rong
                    if (user1 != null && !CommonUtils.isEmpty(user1.getStrCardNumber())) {
                        user2.setUserIdVof1(user1.getUserId());
                        user1.setIsLoginCardId(username);
                        HttpSessionCollector.remove(rsaPublicKey);
                        userGroup.setItemEntityUser(user1);
                        userGroup.setVof2_ItemEntityUser(user2);
                    } else if (user1 == null) {
                        user1 = new EntityUser();
                    }
                    // Luu du lieu vao Memcached
                    String strJson = FunctionCommon.generateJSONBase(userGroup);
                    CommonControler.setDataCache_Mem(strSessionId, strJson);
                    // Xet du lieu tra ve client                    
                    user1.setStrSessionId(strSessionId);
                    user1.setStrCardNumber(user2.getStrCardNumber());
                    user1.setLoginName(user2.getLoginName());
                    user1.setMobileNumber(user2.getMobileNumber());
                    user1.setListManagementVhrOrg(user2.getListManagementVhrOrg());
                    user1.setListAssistantVhrOrg(user2.getListAssistantVhrOrg());
                    user1.setListSecretaryOrg(user2.getListSecretaryOrg());
                    // Lay don vi mac dinh thi truong nuoc ngoai
                    user1.setForeignVhrOrg(user2.getForeignVhrOrg());
                    user1.setTypeTextMarkSync(user2.getTypeTextMarkSync());
                    //lay danh sach don vi user lam TT, LD co is_default in 1,2
                    user1.setListManagementVhrOrgIsDefault(user2.getListManagementVhrOrgIsDefault());
                    user1.setListSecretaryVhrOrg(user2.getListSecretaryVhrOrg());
                    user1.setConfigMeetingRoom(user2.getConfigMeetingRoom());
                    user1.setListOrgPersonReport(user2.getListOrgPersonReport());
                    result = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                            user1, aesKey);
                    log.setDescription("LOGIN_SUCESS");
                    aclog.insertActionLog(user2.getUserId(), username, "UserControler.loginViaPassport",
                            request, "VALIDATE_TICKET_SUCCESS", startTime, deviceName, "");
                } // Xac thuc ticket that bai
                else {
                    result = FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION,
                            null, null);
                    LOGGER.info("1.2.Validate ticket that bai: "
                            + timeToValidateTicket + " ms");
                    aclog.insertActionLog(-12L, "", "UserControler.loginViaPassport",
                            request, "VALIDATE_TICKET_FAIL", startTime, deviceName, "");
                }
            } // Khong lay duoc du lieu trong Memcached
            else {
                LOGGER.info("2.Loi lay du lieu tu Memcached: "
                        + timeToGetDataFromMemcached + " ms");
                result = FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION,
                        null, null);
            }
        } // Loi Server
        catch (Exception ex) {
            LOGGER.error("loginViaPassport (Login qua Passport) - Exception"
                    + " - username: " + username, ex);
            result = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
        LogUtils.logLogin(log);
        return result;
    }

    /**
     * ham lay ma loi dang nhap tap trung
     *
     * @param request
     * @param rsaPublicKey
     * @param encodedAESKey
     * @param data : user, password
     * @return
     */
    public String checkLockAccountSSO(HttpServletRequest request, String rsaPublicKey,
            String encodedAESKey, String data) {
        if (!CommonUtils.isEmpty(data)) {
            try {
                String cachedData = CommonControler.getDataCache_Mem(rsaPublicKey);
                CommonControler.removeDataCache_Mem(rsaPublicKey);//xoa khoi memcache
                if (cachedData != null) {
                    data = SecurityControler.decodeDataByAes(encodedAESKey, data);
                    //System.out.println("checkLockAccountSSO data: " + data);
                    JSONObject json = new JSONObject(data);
                    String[] keys = new String[]{ConstantsFieldParams.STR_LOGINNAME,
                        "password",
                        ConstantsFieldParams.DEVICE_NAME};
                    List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                    String userName = listValue.get(0);
                    String userPass = listValue.get(1);
                    String deviceName = listValue.get(2);
                    if (userName != null && userPass != null) {
                        Date startDate = new Date();
                        EntityLoginSso itemLoginSso;
                        try {
                            itemLoginSso = vof2_CheckLoginVHR(userName, userPass);
                        } catch (Exception e) {
                            LOGGER.error("Loi! thuc hien goi service sso ", e);
                            itemLoginSso = null;
                        }
                        Integer errCodeLoginSSO = (itemLoginSso != null
                                && itemLoginSso.getErrCode() != null) ? itemLoginSso.getErrCode() : -1;
                        LogActionControler aclog = new LogActionControler();
                        aclog.insertActionLog(-10L, userName, "Authenticate.checkLockAccountSSO",
                                request, "SSO_SAITAIKHOAN - MA LOI: " + errCodeLoginSSO,
                                startDate, deviceName, "");
                        return FunctionCommon.generateResponseJSON(getErrorCodeSSO(errCodeLoginSSO), null, null);
                    } else {
                        return FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
                    }
                } else {
                    LOGGER.info(" ---------- Cache data = null");
                    LOGGER.info(" Loi khong co strDataMemC");
                    return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NOSESSION, null, null);
                }
            } catch (JSONException | NumberFormatException ex) {
                //ex.printStackTrace();
                LOGGER.error("checkLockAccountSSO -  Exception: ", ex);
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            } // Loi server
            catch (Exception ex) {
                //ex.printStackTrace();
                LOGGER.error("checkLockAccountSSO -  Exception: ", ex);
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            }
        } else {
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * ham map ma loi
     *
     * @param errCodeLoginSSO
     * @return
     */
    ErrorCode getErrorCodeSSO(int errCodeLoginSSO) {
        ErrorCode errorCode;
        switch (errCodeLoginSSO) {
            case 4:
                //tai khoan sso bi khoa hoac chua kich hoat
                errorCode = ErrorCode.ACCOUNT_SSO_INACTIVATED_OR_LOCKED;
                break;
            case 5:
                //tai khoan bi khoa do nhap thong tin sai nhieu lan
                errorCode = ErrorCode.ACCOUNT_SSO_LOCKED_INFOR_INCORECT;
                break;
            case 7:
                //tai khoan bi admin khoa
                errorCode = ErrorCode.ACCOUNT_SSO_LOCKED_BY_ADMIN;
                break;
            case 8:
                //tai khoan bi khoa di mat khau het hieu luc
                errorCode = ErrorCode.ACCOUNT_SSO_LOCKED_PASSWORD_EXPIRED;
                break;
            default:
                errorCode = ErrorCode.ERR_NOSESSION;
        }
        return errorCode;
    }

    /**
     * Thay doi mat khau
     *
     * @param request
     * @param data
     * @return
     */
    public String changePass(HttpServletRequest request, String data) {
        String[] keys = new String[]{
            "oldPassWord", "newPass", "renewPass"};
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request,
                data, keys);
        if (!userGroup.getCheckSessionOk()) {
            //truong hop bị timeout
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        List<String> listValue = userGroup.getListParamsFromClient();
        String oldPassWord = LoginSecurity.encrypt(listValue.get(0));

        String newPass = listValue.get(1);
        String renewPass = listValue.get(2);
        EntityResult result = new EntityResult();
        //1.Check pass cu xem co dung khong
        UserDAO userDAO = new UserDAO();
        String oldPassDb1 = userGroup.getStrPassVof1();
        if (oldPassWord.equals(oldPassDb1)) {
            //truong hop pass cu nguoi dung nhap trung voi pass moi
            //1.1 check pass moi co thoa man dieu kien trung nhau khong
            if (newPass.equals(renewPass) && isStrong(newPass)) {
                //neu nhap dung du lieu va du manh thi thuc hien doi pass
                String strPassEncode = LoginSecurity.encrypt(renewPass);
                boolean resultUpdate = userDAO.updatePass(userGroup, strPassEncode);
                if (resultUpdate) {
                    result.setId("1");
                    result.setMessage("Thay doi thanh cong");
                } else {
                    result.setId("-1");
                    result.setMessage("Loi update pass");
                }
            } else {
                result.setId("-2");
                result.setMessage("mat khau khong du manh hoac nam trong danh sach blacklist");
            }
        } else {
            //loi pass cu nhap khong dung
            result.setId("-3");
            result.setMessage("Pass cu nhap khong hop le");
        }
        return FunctionCommon.responseResult(ErrorCode.SUCCESS,
                result, userGroup);
    }

    /**
     * Kiem tra mat khau manh
     *
     * @param password
     * @return
     */
    private boolean isStrong(String password) {
        boolean result = false;
        String regexCheck = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])"
                + "(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
        //kiem tra khong nam trong blacklist pass
        //du manh
        UserDAO staffDAO = new UserDAO();
        //kiem tra khong nam trong blackList
        boolean checkPass = staffDAO.checkPassInBlackList(password);
        if (password.matches(regexCheck) && !checkPass) {
            //thoa man dieu kien doi
            result = true;
        }
        return result;
    }

    /**
     * <b>Doi ngon ngu trong phien nguoi dung</b><br>
     *
     * @param request
     * @param data
     * @return
     */
    public String changeLanguageInSession(HttpServletRequest request, String data) {

        String[] keys = new String[]{
            ConstantsFieldParams.LANGUAGE
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("changeLanguageInSession - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            String language = listValue.get(0);
            Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
            if (user == null) {
                return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
            }
            user.setLanguageCode(language);
            String info = FunctionCommon.generateJSONBase(userGroup);
            CommonControler.replaceCachedData(FunctionCommon.getStrSessionIdByHttpRQ(request),
                    info);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, 1, userGroup);
        } catch (Exception ex) {
            LOGGER.error("changeLanguageInSession - Exception - username: "
                    + userGroup.getCardId(), ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Lay danh sach account Vip</b><br>
     *
     * @param request
     * @return
     */
    public String getLstUserVip(HttpServletRequest request) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(request);
        
        if (dataSessionGR.getCheckSessionOk()) {
            try {
//                log.setActionName("getLstUserVip");
                LogUtils.logFunctionalStart(log);
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                UserDAO userDao = new UserDAO();
                List<EntityStaff> lstUser = userDao.getLstUserVip();
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                        lstUser, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }
        return strResult;
    }

    /**
     * <b>Tao token</b>
     * 
     * @param userGroup             thong tin nhan vien
     * @param isNeedToken           1 - can sinh token
     */
    public void generateToken(EntityUserGroup userGroup, Integer isNeedToken) {

        if (userGroup == null || CommonUtils.isEmpty(userGroup.getCardId())) {
            LOGGER.error("generateToken - Loi du lieu dau vao!");
            return;
        }
        if (isNeedToken == null || isNeedToken != 1) {
            return;
        }
        try {
            // Tao thoi gian het han la sau 30 phut
            Long expirationTime = (new Date()).getTime() + 1800 * 1000;
            // Gan cac gia tri vao chuoi token voi dinh dang JSON
            String token = String.format(VOFFICE_TOKEN, userGroup.getCardId(), expirationTime);
            // Load file khoa cong khai
            String derFile = CommonUtils.getAppConfigValue(SMARTOFFICE_RSA_PUBLIC_KEY_DER_FILE);
            // Ma hoa token
            byte[] encryptBytes = RSA.encryptData(token, RSA.loadPublicKeyFromDerFile(derFile));
            token = DatatypeConverter.printHexBinary(encryptBytes);
            userGroup.setToken(token);
        } catch (Exception ex) {
            LOGGER.error("generateToken - Exception!", ex);
        }
    }

    public String getListUserConfigAssistant(HttpServletRequest request, String data) {

        String[] keys = new String[]{
            ConstantsFieldParams.MEETING_ID
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getListUserConfigAssistant - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            String strMeetingId = listValue.get(0);
            if (CommonUtils.isEmpty(strMeetingId)) {
            	return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
            }
            
            Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
            if (user == null) {
                return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
            }
            UserDAO userDao = new UserDAO();
            List<EntityVhrEmployee> result = userDao.getListUserConfigAssistant(Long.valueOf(strMeetingId));
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            LOGGER.error("getListUserConfigAssistant - Exception - username: "
                    + userGroup.getCardId(), ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    /**
     * ghi lai log nguoi dung login vao tu he thong nao
     * @param request
     * @param listValue
     * @return 
     */
    private Boolean isPreventClientRemote(HttpServletRequest request,List<String> listValue) {
        String strIpRemote = FunctionCommon.getClientIpAddressRemote(request);
        
        // Ten dang nhap
        String loginName = listValue.get(0);
//        System.out.println("loginName=="+loginName);
        // Mat khau da ma hoa
        String password = listValue.get(1);
        // Mat khau chua ma hoa
        String vof2Key = listValue.get(3);
        String deviceName = listValue.get(4);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String strDate = sdf.format(new Date());
        //thuc hien chan nguoi dung theo cau hinh
        ConfigParameterDAO configParameterDAO = new ConfigParameterDAO();
        String strConfig = configParameterDAO.getValueFromConfigDataBase("PREVENTCLIENTREMOTE");
        if(strConfig!=null && strConfig.trim().length()>0){
            List<? extends Object> listResult =  FunctionCommon.convertJsonToListObject(
                    strConfig, EntityPreventClientRemote.class);
           
            if(listResult!=null){
                List<EntityPreventClientRemote> resultPrevent = (List<EntityPreventClientRemote>) listResult;
                for (EntityPreventClientRemote entityPreventClientRemote : resultPrevent) {
                    if(entityPreventClientRemote.getUsername().equals(loginName)){
                        //co ten cau hinh bang voi ten dang nhap
                        //kiem tra xem dia chi ip co chan ko
                        if(entityPreventClientRemote.getIpPrevent().contains(strIpRemote)){
                            //ip nay co cau hinh chan theo user dang nhap
                            return true;
                        }
                    }
                    if(entityPreventClientRemote.getUsername().equals("all")){
                        //chan toan bo user remote tu dia chi nay
                        if(entityPreventClientRemote.getIpPrevent().contains(strIpRemote)){
                            //ip nay co cau hinh chan theo user dang nhap
                            return true;
                        }
                    }
                }
            }
        }
        
//         System.out.println("insert logs==");
        //thuc hien insert logs nguoi dung
        EntityActionLogMobile logDB = new EntityActionLogMobile(158111L, loginName,strDate, strDate,
                "ClientRemote.login", "Dia chi Client Remote: " + strIpRemote, 
                "Server=" , 5,"",
                deviceName,0,5, strDate);
        LogUtils.insertActionLogMobile(158111L, logDB);
        return false;
    }
    public static void main(String[] args) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String strDate = sdf.format(new Date());
         EntityActionLogMobile logDB = new EntityActionLogMobile(158111L, "Aaaaaaaaaaaaaa",strDate, strDate,
                "ClientRemote.login", "Dia chi Client Remote: " + "aaaaaaaaaa", 
                "Dia chi server: " + FunctionCommon.IPPORTSERVICE + ", user_pass=" + "aaaaaaaaaa", 158,"",
                "aaaaaaaaaaaa",1,5, strDate);
        LogUtils.insertActionLogMobile(158111L, logDB);
    }
}
