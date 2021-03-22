/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.callserviceother;

import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.entity.EntityLoginSso;
import com.viettel.voffice.database.entity.User.UserDataResSsoEntity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.log4j.Logger;
import org.apache.commons.codec.binary.Base64;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.keys.AesKey;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;
import org.jose4j.lang.JoseException;

/**
 * Dang nhap sso bang he thong sso moi cho mobile
 * @author datnv5
 */
public class SSOResfullCallHttpConnect {
    private static final Logger LOGGER = Logger.getLogger(SSOResfullCallHttpConnect.class);
    
    /**
     * thuc hien goi server day thong tin nguoi dung len de thuc hien dang nhap
     * @param urlServer
     * @param userName
     * @param strPass
     * @param signingKey
     * @param encryptionKey
     * @return 
     */
    public static EntityLoginSso postRequestToServerLogin(String urlServer,
            String userName,String strPass,String signingKey,String encryptionKey) {
        try {
            StringBuilder paramsQuery = new StringBuilder();
            paramsQuery.append("username=");
            paramsQuery.append(URLEncoder.encode(userName, "UTF-8"));
            paramsQuery.append("&password=");
            paramsQuery.append(URLEncoder.encode(strPass, "UTF-8"));
            paramsQuery.append("&token=true");
            EntityLoginSso itemResult;
            disableCertificateValidation();
            String strResult;
            try {
                String strUrl = urlServer;
                URL url = new URL(strUrl);
                StringBuilder postData = new StringBuilder();
                
                postData.append(paramsQuery.toString());
                byte[] postDataBytes = postData.toString().getBytes("UTF-8");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty(
                        "Content-Type","application/x-www-form-urlencoded;charset=utf-8");
                conn.setRequestProperty("Accept-Charset", "UTF-8");
                conn.setRequestProperty(
                        "Content-Length", String.valueOf(postDataBytes.length));
                conn.setDoOutput(true);
                conn.getOutputStream().write(postDataBytes);
                conn.setConnectTimeout(1000);
                int responseCode = conn.getResponseCode();
                switch (responseCode) {
                    case HttpURLConnection.HTTP_OK:
                        StringBuffer response;
                        try (BufferedReader in = new BufferedReader(
                                new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                            String inputLine;
                            response = new StringBuffer();
                            while ((inputLine = in.readLine()) != null) {
                                response.append(inputLine);
                            }
                        }   // print result
                        strResult = response.toString();
                        itemResult = new EntityLoginSso();
                        itemResult.setIsLoginSso(false);
                        itemResult.setDataFull(strResult);
                        itemResult.setErrCode(responseCode);
                        break;
                    case HttpURLConnection.HTTP_CREATED:
                        StringBuffer response2;
                        try (BufferedReader in = new BufferedReader(
                                new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                            String inputLine;
                            response2 = new StringBuffer();
                            while ((inputLine = in.readLine()) != null) {
                                response2.append(inputLine);
                            }
                        }
                        strResult = response2.toString();
                        strResult = decodeDataReiceved(strResult, signingKey, encryptionKey);
                        itemResult = new EntityLoginSso();
                        itemResult.setDataFull(strResult);
                        itemResult.setErrCode(responseCode);
                        //thuc hien chuan hoa du lieu tra ve client
                        itemResult = dictionaryDataUser(itemResult);
                        break;
                    case HttpURLConnection.HTTP_UNAUTHORIZED:
                        //truong hop ket qua dang nhap sai tra ve
                        StringBuffer response3;
                        try (BufferedReader in = new BufferedReader(
                                new InputStreamReader(conn.getErrorStream(), "UTF-8"))) {
                            String inputLine;
                            response3 = new StringBuffer();
                            while ((inputLine = in.readLine()) != null) {
                                response3.append(inputLine);
                            }
                        }
                        strResult = response3.toString();
                        itemResult = new EntityLoginSso();
                        itemResult.setDataFull(strResult);
                        itemResult.setErrCode(responseCode);
                        //thuc hien set ma loi va thong tin loi tra ve
                        itemResult = dictionaryDataUser(itemResult);
                        break;
                    default:
                        itemResult = new EntityLoginSso();
                        itemResult.setDataFull("");
                        itemResult.setIsLoginSso(false);
                        itemResult.setErrCode(responseCode);
                        break;
                }
            } catch (java.net.SocketTimeoutException e) {
                itemResult = new EntityLoginSso();
                itemResult.setDataFull(e.toString());
                itemResult.setIsLoginSso(false);
                itemResult.setErrCode(111555);
                LOGGER.error("sendRequestToServer", e);
            } catch (IOException e) {
                itemResult = new EntityLoginSso();
                itemResult.setDataFull(e.toString());
                itemResult.setIsLoginSso(false);
                itemResult.setErrCode(111556);
                LOGGER.error("sendRequestToServer", e);
            }
            return itemResult;
        } catch (UnsupportedEncodingException ex) {
            LOGGER.error("sendRequestToServer", ex);
            return null;
        }
    }
    
    
    /**
     * thuc hien xac thuc ssl truoc khi goi du lieu
     */
     private static void disableCertificateValidation() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] { 
          new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() { 
              return new X509Certificate[0]; 
            }
            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {}
            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {}
        }};

        // Ignore differences between given hostname and certificate hostname
        HostnameVerifier hv = new HostnameVerifier() {
          @Override
          public boolean verify(String hostname, SSLSession session) { return true; }
        };
        // Install the all-trusting trust manager
        try {
          SSLContext sc = SSLContext.getInstance("SSL");
          sc.init(null, trustAllCerts, new SecureRandom());
          HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
          HttpsURLConnection.setDefaultHostnameVerifier(hv);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            LOGGER.error("disableCertificateValidation", e);
        }
      }
     
     
     /**
      * giai ma du lieu nhan duoc tu server sso
      * @param strData
      * @return 
      */
     private static String decodeDataReiceved(String strData,String signingKey,String encryptionKey){
        try {
//            final String signingKey = "ahN47WHSA3-_I7wAcfQ7W2qyTKMeQrbDBYJQoENpGeTs8xLWddVPaMfqgC_e_UboPB9wJluMVC3M8CtoBKt7Ow";
//            final String encryptionKey = "rle6pMmf5eWeix5LHm2sil_aP8WWl3IB8RtMWsRw1vs";
            final Key key = new AesKey(signingKey.getBytes(StandardCharsets.UTF_8));
            final JsonWebSignature jws = new JsonWebSignature();
            jws.setCompactSerialization(strData);
            jws.setKey(key);
            if (!jws.verifySignature()) {
                throw new Exception("JWT verification failed");
            }
            
            final byte[] decodedBytes = Base64.decodeBase64(jws.getEncodedPayload().getBytes(StandardCharsets.UTF_8));
            final String decodedPayload = new String(decodedBytes, StandardCharsets.UTF_8);
            
            final JsonWebEncryption jwe = new JsonWebEncryption();
            final JsonWebKey jsonWebKey = JsonWebKey.Factory
                    .newJwk("\n" + "{\"kty\":\"oct\",\n" + " \"k\":\"" + encryptionKey + "\"\n" + "}");
            jwe.setCompactSerialization(decodedPayload);
            jwe.setKey(new AesKey(jsonWebKey.getKey().getEncoded()));
            return jwe.getPlaintextString();
        } catch (JoseException ex) {
            LOGGER.error("decodeDataReiceved 1:", ex);
        } catch (Exception ex) {
             LOGGER.error("decodeDataReiceved:", ex);
        }
        return "";
     }
     
     /**
      * thuc hien convert du lieu tra ve thanh dinh dang  chuan  cua voffice
      * @param itemResult
      * @return 
      */
    private static EntityLoginSso dictionaryDataUser(EntityLoginSso itemResult) {
        EntityLoginSso result = null;
        if(itemResult!=null && itemResult.getErrCode()!=null && itemResult.getErrCode()==201){
            //du lieu login tra ve thanh cong
            String strDataJson = itemResult.getDataFull();
            Object object = FunctionCommon.convertJsonToObject(strDataJson, UserDataResSsoEntity.class);
            if(object!=null){
                result = new EntityLoginSso();
                UserDataResSsoEntity userDataResSsoEntity = (UserDataResSsoEntity) object;
                List<String> strUserId = userDataResSsoEntity.getUserId();
                if(strUserId!=null && strUserId.size()>0){
                    result.setUserId(Long.valueOf(strUserId.get(0)));
                }
                List<String> strStaffCode = userDataResSsoEntity.getStaffCode();
                if(strStaffCode!=null && strStaffCode.size()>0){
                    result.setStaffCode(strStaffCode.get(0));
                }
                List<String> strUserName = userDataResSsoEntity.getUserName();
                if(strUserName!=null && strUserName.size()>0){
                    result.setUserName(strUserName.get(0));
                }
                result.setIsLoginSso(true);
                result.setErrCode(0);
            }
        }else if(itemResult!=null && itemResult.getErrCode()!=null && itemResult.getErrCode()==401){
            result = new EntityLoginSso();
            //truong hop loi tra ve
            String strSsoRespose = itemResult.getDataFull().toLowerCase().trim();
            result.setIsLoginSso(false);
            result.setDetailErr(itemResult.getDataFull());
            if(strSsoRespose.contains("UnresolvedPrincipalException".toLowerCase())){
                //khong the dang nhap trong thoi gian nay
                result.setErrCode(208);
            }else if(strSsoRespose.contains("AccountExpiredException".toLowerCase())){
                //khong the dang nhap do da qua han doi mat khau, lien he voi admin
                result.setErrCode(203);
            }else if(strSsoRespose.contains("InvalidKeySpecException".toLowerCase())
                    ||strSsoRespose.contains("CredentialException".toLowerCase())
                    ||strSsoRespose.contains("AccountExpireException".toLowerCase())){
                //Khong the dang nhap do user da lau khong su dung
                result.setErrCode(202);
            }else if(strSsoRespose.contains("FailedLoginException".toLowerCase())){
                //Tai khoan hoac mat khau khong chinh xac
                result.setErrCode(202);
            }else if(strSsoRespose.contains("NoSuchProviderException".toLowerCase())){
                //Tai khoan chua den thoi gian co hieu luc
                result.setErrCode(204);
            }else if(strSsoRespose.contains("InvalidLoginLocationException".toLowerCase()) 
                    || strSsoRespose.contains("CredentialNotFoundException".toLowerCase())){
                //Tai khoan chua co thong tin ngay bat dau co hieu luc
                result.setErrCode(204);
            }else if(strSsoRespose.contains("SignatureException".toLowerCase())
                    ||strSsoRespose.contains("MaxTempLockException".toLowerCase())){
                //Tai khoan bi khoa do nhap sai so lan cho phep
                result.setErrCode(205);
            }else if(strSsoRespose.contains("AccountException".toLowerCase())){
                //Can thay doi pass de dang nhap
                result.setErrCode(204);
            }else if(strSsoRespose.contains("AccountNotFoundException".toLowerCase())||
                    strSsoRespose.contains("AccountNotActiveException".toLowerCase())){
                //Tai khoan chua kich hoat hoac bi khoa trong qua trinh su dung
                result.setErrCode(204);
            }else if(strSsoRespose.contains("PasswordExpiredException".toLowerCase())){
                //Tai khoan bi khoa do mat khau het han
                result.setErrCode(208);
            }else{
                 result.setErrCode(209);
            }
        }else{
            result = new EntityLoginSso();
            result.setIsLoginSso(false);
            if(itemResult!=null && itemResult.getErrCode() != null){
                result.setErrCode(itemResult.getErrCode());
            }else{
                result.setErrCode(111555);
            }
        }
        return result;
    }
}
