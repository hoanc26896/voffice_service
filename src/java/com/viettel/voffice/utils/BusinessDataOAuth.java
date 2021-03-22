/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.utils;

import com.google.gson.Gson;
import com.viettel.voffice.database.dao.logAction.ActionLogMobileDAO;
import com.viettel.voffice.database.entity.EntityUserGroup;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author thanght6
 */
public class BusinessDataOAuth {
    
    /** Ghi log file */
    private static final Logger LOGGER = Logger.getLogger(BusinessDataOAuth.class);
    
    /** URL service */
    private static final String ROOT_URL = CommonUtils.getAppConfigValue(
            "business.data.oauth.url");
    
    /** ID client */
    private static final String CLIENT_ID = CommonUtils.getAppConfigValue(
            "business.data.oauth.client.id");
    
    /** Chuoi bi mat */
    private static final String CLIENT_SECRET = CommonUtils.getAppConfigValue(
            "business.data.oauth.client.secret");
    
    /** Ten dang nhap */
    private static final String USERNAME = CommonUtils.getAppConfigValue(
            "business.data.oauth.username");
    
    /** Mat khau */
    private static final String PASSWORD = CommonUtils.getAppConfigValue(
            "business.data.oauth.password");
    
    /** Ma nhan vien cau hinh lay du lieu */
    private static final String STAFF_CODE = CommonUtils.getAppConfigValue(
            "business.data.oauth.staffcode");
    
    /** Thong tin xac thuc */
    private static volatile OAuthEntity authentication;
    
    /**
     * <b>Tao tham so cho url</b><br>
     * 
     * @param map 
     * @return
     */
    public static String generateQueryString(Map<String, String> map) {
        
        String queryString = null;
        if (map != null && !map.isEmpty()) {
            queryString = "";
            for (Map.Entry<String, String> entry : map.entrySet()) {
                try {
                    queryString += "&" + entry.getKey() + "=" + URLEncoder.encode(
                            entry.getValue(), "UTF-8");
                } catch (UnsupportedEncodingException ex) {
                    LOGGER.error("generateQueryString - value: " + entry.getValue(), ex);
                }
            }
            // Bo dau "&" dau tien
            if (!CommonUtils.isEmpty(queryString)) {
                queryString = queryString.substring(1);
            }            
        }
        return queryString;
    }
    
    /**
     * <b>Gui 1 request co phuong thuc GET</b>
     * 
     * @param <T>
     * @param userGroup         thong tin user
     * @param method            la phuong thuc POST
     * @param function          ten chuc nang
     * @param parameter         tham so
     * @param classOfT          kieu du lieu tra ve
     * @return 
     */
    public static <T extends Object> T sendRequest(EntityUserGroup userGroup,
            String method, String function, Map<String, String> parameter,
            Class<T> classOfT) {
        
        Date startTime = new Date();
        T response = null;
        String queryString = null;
        StringBuilder strResponse = new StringBuilder();
        try {
            String strUrl = ROOT_URL + function;
            URL url = new URL(strUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // La phuong thuc POST
            conn.setRequestMethod(method);
            // Tao chuoi truy van trong link
            queryString = generateQueryString(parameter);
            if (!CommonUtils.isEmpty(queryString)) {
                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                os.write(queryString.getBytes());
                os.flush();
                os.close();
            }
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
		String inputLine;                
		while ((inputLine = br.readLine()) != null) {
			strResponse.append(inputLine);
		}
		br.close();
                if (!CommonUtils.isEmpty(strResponse.toString())) {
                    Gson gson = new Gson();
                    response = gson.fromJson(strResponse.toString(), classOfT);
                } else {
                    LOGGER.error("sendGETRequest - Khong co du lieu tra ve!");
                }
            } else {
                LOGGER.error("sendGETRequest - Loi ket noi - responseCode: " + responseCode);
            }
        } catch (Exception ex) {
            LOGGER.error("sendGETRequest - Exception!", ex);
        }
        if (userGroup != null) {
            Date endTime = new Date();
            ActionLogMobileDAO actionLogMobileDAO = new ActionLogMobileDAO();
            actionLogMobileDAO.insert(userGroup.getUserId1(), userGroup.getCardId(),
                    startTime, endTime, "SLKD." + function, queryString,
                    strResponse.toString(), null, null, null, null);
        }
        return response;
    }
    
    /**
     * <b>Lay refresh token</b>
     * 
     * @return 
     */
    public static String getRefreshToken() {
        
        String function = "oauth/token";
        Map<String, String> parameter = new HashMap<>();
        parameter.put("grant_type", "password");
        parameter.put("client_id", CLIENT_ID);
        parameter.put("client_secret", CLIENT_SECRET);
        parameter.put("username", USERNAME);
        parameter.put("password", PASSWORD);
        OAuthEntity response = sendRequest(null, "GET", function, parameter, OAuthEntity.class);
        if (response == null || CommonUtils.isEmpty(response.getRefresh_token())) {
            LOGGER.error("getRefreshToken - Loi lay refresh token!");
            return null;
        }
        LOGGER.info("getRefreshToken - Lay refresh token thanh cong!");
        return response.getRefresh_token();
    }
    
    /**
     * <b>Lay access token</b>
     * 
     * @return 
     */
    public static String getAccessToken() {
        
        // Lay thoi gian hien tai
        Calendar cal = Calendar.getInstance();
        // Khong co thong tin xac thuc hoac da qua thoi gian het han
        if (authentication == null || authentication.getExpireTime() == null
                || authentication.getExpireTime().before(cal.getTime())) {
            // Lay refresh token
            String refreshToken = getRefreshToken();
            if (CommonUtils.isEmpty(refreshToken)) {
                LOGGER.error("getAccessToken - refresh token null hoac rong!");
                return null;
            }
            String function = "oauth/token";
            Map<String, String> parameter = new HashMap<>();
            parameter.put("grant_type", "refresh_token");
            parameter.put("client_id", CLIENT_ID);
            parameter.put("client_secret", CLIENT_SECRET);
            parameter.put("refresh_token", refreshToken);
            authentication = sendRequest(null, "GET", function, parameter, OAuthEntity.class);
            if (authentication == null || CommonUtils.isEmpty(authentication.getAccess_token())) {
                LOGGER.error("getAccessToken - Loi lay access token!");
                return null;
            }
            if (authentication.getExpires_in() != null) {
                cal.add(Calendar.SECOND, authentication.getExpires_in().intValue());
            }
            authentication.setExpireTime(cal.getTime());
        }
        LOGGER.info("getRefreshToken - Lay access token thanh cong!");
        return authentication.getAccess_token();
    }
    
    /**
     * <b>Lay du lieu kinh doanh</b>
     * 
     * @param userGroup         thong tin user
     * @param period            thang lay du lieu (yyyyMM)
     * @param deptCode          ma don vi
     * @param serviceId         ma tieu chi
     * @return 
     */
    public static List<Float> getDataTimePrd(EntityUserGroup userGroup,
            String period, String deptCode, String serviceId) {
        
        if (CommonUtils.isEmpty(period) || period.trim().length() != 6
                || CommonUtils.isEmpty(deptCode)
                || CommonUtils.isEmpty(serviceId)) {
            LOGGER.error("getDataTimePrd - Loi du lieu dau vao!");
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        Date date;
        try {
            // Parse ky danh gia sang kieu date
            date = sdf.parse(period);            
        } catch (ParseException ex) {
            LOGGER.error("ParseException - period: " + period + " - pattern: "
                    + sdf.toPattern(), ex);
            return null;
        }
        // Lay ngay dau thang va cuoi thang (yyyyMMdd)
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        String frmPrdId = period + "01";
        String toPrdId = period + calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        String function = "api/chatbot/getDataTimePrd";
        Map<String, String> map = new HashMap<>();
        map.put("prd_type", "MON");
        map.put("frm_prd_id", frmPrdId);
        map.put("to_prd_id", toPrdId);
        map.put("dept_code", deptCode);
        map.put("service_id", serviceId);
        map.put("staff_code", STAFF_CODE);
        map.put("accumulated", "0");
        map.put("access_token", getAccessToken());
        Response response = sendRequest(userGroup, "POST", function, map,
                Response.class);
        if (response != null && response.check()) {
            LOGGER.info("getDataTimePrd - Lay du lieu thanh cong!");
            return response.getEntity().getListValue();
        } else {
            LOGGER.info("getDataTimePrd - Lay du lieu that bai!");
            return null;
        }
    }
    
//    public static void main(String[] args) {
//        
//        getDataTimePrd(null, "201712", "VTT", "KD-1712");
//    }
    
    /**
     * Thong tin xac thuc
     */
    public class OAuthEntity {
        
        private String access_token;
        private String token_type;
        private String refresh_token;
        private Long expires_in;
        private String scope;
        private Date expireTime;
        
        public String getAccess_token() {
            return access_token;
        }

        public void setAccess_token(String access_token) {
            this.access_token = access_token;
        }

        public String getToken_type() {
            return token_type;
        }

        public void setToken_type(String token_type) {
            this.token_type = token_type;
        }

        public String getRefresh_token() {
            return refresh_token;
        }

        public void setRefresh_token(String refresh_token) {
            this.refresh_token = refresh_token;
        }

        public Long getExpires_in() {
            return expires_in;
        }

        public void setExpires_in(Long expires_in) {
            this.expires_in = expires_in;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public Date getExpireTime() {
            return expireTime;
        }

        public void setExpireTime(Date expireTime) {
            this.expireTime = expireTime;
        }
        
    }

    public class ErrorCode {
        
        /**
         * Ma loi
         * <ul>
         *  <li>0: thanh cong</li>
         *  <li>1: loi thieu tham so dau vao</li>
         *  <li>2: loi he thong</li>
         *  <li>3: loi xac thuc</li>
         * </ul>
         */
        public Integer errCode;
        
        private String errDesc;

        public Integer getErrCode() {
            return errCode;
        }

        public void setErrCode(Integer errCode) {
            this.errCode = errCode;
        }

        public String getErrDesc() {
            return errDesc;
        }

        public void setErrDesc(String errDesc) {
            this.errDesc = errDesc;
        }
    }
    
    public class Entity {
        
        /**
         * Loai chu ky
         * <ul>
         *  <li>DAY</li>
         *  <li>MON</li>
         *  <li>QUAR</li>
         *  <li>YEAR</li>
         * </ul>
         */
        private String prdType;
        
        /** Tu ngay (Timestamp) */
        private Long frmPrdId;
        
        /** Den ngay (Timestamp) */
        private Long toPrdId;
        
        /** Ma don vi */
        private String deptCode;
        
        /** Ma tieu chi */
        private String serviceId;
        
        /** So thuc hien */
        private Float fValue;
        
        /** Ke hoach don vi */
        private Float fSchedule;
        
        /** So muc tieu */
        private Float fTarget;
        
        /** Ty le */
        private Float fPercent;
        
        /** Ten don vi */
        private String deptName;
        
        /** Don vi cua so thuc hien */
        private String unit;

        public String getPrdType() {
            return prdType;
        }

        public void setPrdType(String prdType) {
            this.prdType = prdType;
        }

        public Long getFrmPrdId() {
            return frmPrdId;
        }

        public void setFrmPrdId(Long frmPrdId) {
            this.frmPrdId = frmPrdId;
        }

        public Long getToPrdId() {
            return toPrdId;
        }

        public void setToPrdId(Long toPrdId) {
            this.toPrdId = toPrdId;
        }

        public String getDeptCode() {
            return deptCode;
        }

        public void setDeptCode(String deptCode) {
            this.deptCode = deptCode;
        }

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        public Float getfValue() {
            return fValue;
        }

        public void setfValue(Float fValue) {
            this.fValue = fValue;
        }

        public Float getfSchedule() {
            return fSchedule;
        }

        public void setfSchedule(Float fSchedule) {
            this.fSchedule = fSchedule;
        }

        public Float getfTarget() {
            return fTarget;
        }

        public void setfTarget(Float fTarget) {
            this.fTarget = fTarget;
        }

        public Float getfPercent() {
            return fPercent;
        }

        public void setfPercent(Float fPercent) {
            this.fPercent = fPercent;
        }

        public String getDeptName() {
            return deptName;
        }

        public void setDeptName(String deptName) {
            this.deptName = deptName;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }
        
        public List<Float> getListValue() {
            List<Float> listValue = new ArrayList<>();
            listValue.add(fValue);
            listValue.add(fSchedule);
            return listValue;
        }
    }
    
    public class Response {
        
        /** Ma loi */
        private ErrorCode errorCode;
        
        /** Du lieu */
        private Entity entity;

        public ErrorCode getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(ErrorCode errorCode) {
            this.errorCode = errorCode;
        }

        public Entity getEntity() {
            return entity;
        }

        public void setEntity(Entity entity) {
            this.entity = entity;
        }
 
        public boolean check() {
            
            if (errorCode != null && errorCode.getErrCode() != null
                    && errorCode.getErrCode() == 0 && entity != null) {
                LOGGER.info("check - Response tra ve thanh cong!");
                return true;
            } else {
                String message;
                if (errorCode == null) {
                    message = "errorCode null";
                } else {
                    message = "errCode: " + errorCode.getErrCode()
                            + " - errDesc: " + errorCode.getErrDesc();
                }
                if (entity == null) {
                    message += " - entity null";
                }
                LOGGER.error("check - Response tra ve loi - " + message);
                return false;
            }
        }
    }
}
