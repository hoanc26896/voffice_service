/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.cm.EntityAuthentication;
import com.viettel.voffice.database.entity.cm.EntityCompany;
import com.viettel.voffice.database.entity.cm.EntityDocument;
import com.viettel.voffice.database.entity.cm.EntityFile;
import com.viettel.voffice.database.entity.cm.EntityListCompany;
import com.viettel.voffice.database.entity.cm.EntityListDocument;
import com.viettel.voffice.database.entity.cm.EntityResponse;
import com.viettel.voffice.database.entity.cm.EntitySignature;
import com.viettel.voffice.database.entity.text.EntityText;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * Ket noi API cua bo cong thuong
 * 
 * @author thanght6
 */
public class CMConnector {
    
    /** Log file */
    private static final Logger LOGGER = Logger.getLogger(CMConnector.class);
    
    /** Link service Bo cong thuong */
//    private static final String ROOT_URL = "http://10.60.106.178:8119/api/";
    private static final String ROOT_URL = "http://10.60.106.178:8061/Forwarder_Service/resources/cm/";
//    private static final String ROOT_URL = "https://api.cm.erpstore.vn/api/";
    
    /** Thoi gian dang nhap */
    private static long loginTime = 0L;
    
    /** Khoa RSA cong khai ma hoa key AES */
    private static volatile PublicKey rsaPublicKey;
    
    /** Khoa AES */
    private static volatile String aesKey;
    
    /** Khoa AES da ma hoa gui len trong request */
    private static volatile String encryptedAESKey;
    
    /** Init Vector AES */
    private static volatile String aesIV;
    
    /** Init Vector da ma hoa gui len trong request */
    private static volatile String encryptedAESIV;
    
    /** Token cho moi giao dich */
    private static volatile String token;

    private static final Gson GSON = new GsonBuilder().serializeNulls().create();
    
    /**
     * <b>Chuyen mang byte thanh chuoi hex</b>
     * 
     * @param bytes
     * @return 
     */
    private static String byteArrayToHexString(byte[] bytes) {
        
        char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * <b>Chuyen chuoi hex thanh mang byte</b>
     * @param s
     * @return 
     */
    public static byte[] hexStringToByteArray(String s) {
        
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * <b>Sinh mang byte ngau nhien</b>
     * 
     * @param length
     * @return
     * @throws Exception 
     */
    private static byte[] generateRandomBytes(int length) throws Exception {
        
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] bytes = new byte[length];
        sr.nextBytes(bytes);
        return bytes;
    }

    /**
     * <b>Sinh ngau nhien khoa AES</b>
     * 
     * @return
     * @throws Exception 
     */
    private static String[] generateSecretKey() throws Exception {
        return new String[] { byteArrayToHexString(generateRandomBytes(8)),
                byteArrayToHexString(generateRandomBytes(8)) };
    }

    /**
     * <b>Tao khoa RSA cong khai tu mang byte</b>
     * 
     * @param bytes
     * @return
     * @throws Exception 
     */
    private static PublicKey getPublicKeyFromByteArray(byte[] bytes)
            throws Exception {
        
        return KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(bytes));
    }

    /**
     * <b>Ma hoa RSA</b>
     * 
     * @param text
     * @param key
     * @return
     * @throws Exception 
     */
    private static byte[] encryptRSA(String text, PublicKey key)
            throws Exception {
        
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(text.getBytes());
    }

    /**
     * <b>Ma hoa AES</b>
     * 
     * @param key
     * @param salt
     * @param text
     * @return
     * @throws Exception 
     */
    private static byte[] encryptAES(String key, String salt, String text)
            throws Exception {
        return encryptAES(key, salt, text.getBytes("UTF-8"));
    }

    /**
     * <b>Ma hoa AES</b>
     * 
     * @param key
     * @param salt
     * @param input
     * @return
     * @throws Exception 
     */
    private static byte[] encryptAES(String key, String salt, byte[] input)
            throws Exception {
        SecretKeySpec secretKeySpecAES = new SecretKeySpec(
                key.getBytes("UTF-8"), "AES");
        IvParameterSpec iv = new IvParameterSpec(salt.getBytes("UTF-8"));
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpecAES, iv);
        return cipher.doFinal(input);
    }

    /**
     * <b>Giai ma AES</b>
     * 
     * @param key
     * @param salt
     * @param input
     * @return
     * @throws Exception 
     */
    private static byte[] decryptAES(String key, String salt, byte[] input)
            throws Exception {
        
        SecretKeySpec secretKeySpecAES = new SecretKeySpec(
                key.getBytes("UTF-8"), "AES");
        IvParameterSpec iv = new IvParameterSpec(salt.getBytes("UTF-8"));
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpecAES, iv);
        return cipher.doFinal(input);
    }
    
    public static void doTrustToCertificates() throws Exception {

        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType)
                        throws CertificateException {
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType)
                        throws CertificateException {
                }
            }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HostnameVerifier hv = new HostnameVerifier() {
            @Override
            public boolean verify(String urlHostName, SSLSession session) {
                if (!urlHostName.equalsIgnoreCase(session.getPeerHost())) {
//                    System.out.println("Warning: URL host '" + urlHostName
//                            + "' is different to SSLSession host '" + session.getPeerHost() + "'.");
                }
                return true;
            }
        };
        HttpsURLConnection.setDefaultHostnameVerifier(hv);
    }
    
    /**
     * <b>Gui request</b>
     * 
     * @param userGroup             user
     * @param function              ten chuc nang
     * @param parameters            tham so
     * @param isAuthenticate        true - request can xac thuc
     * @return 
     */
    public static String sendRequest(EntityUserGroup userGroup,
            String function, HashMap<String, Object> parameters,
            boolean isAuthenticate) {
        
        Date startTime = new Date();
        String errorDesc = "ERROR: ";        
        String responseData = null;
        String strParameters = "";
        try {
//            doTrustToCertificates();
            URL url = new URL(ROOT_URL + "fork/" + function);
//            URL url = new URL(ROOT_URL + function);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // Add header request
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("content-type", "application/x-www-form-urlencoded");
//            conn.setRequestProperty("content-type", "application/json;charset=UTF-8");
            // Request can xac thuc
            if (isAuthenticate) {
                if (CommonUtils.isEmpty(token)
                        || (new Date()).getTime() - loginTime > 600000) {
                    if (!login(userGroup)) {
                        errorDesc += "Loi khong lay duoc token!";
                        LOGGER.error("sendRequest - " + errorDesc);
                        return responseData;
                    }
                }
                conn.setRequestProperty("Authorization", "Bearer " + token);
            }
            if (!CommonUtils.isEmpty(aesKey) && !CommonUtils.isEmpty(aesIV)) {
                if (!CommonUtils.isEmpty(parameters)) {
                    strParameters = GSON.toJson(parameters);
                    // Ma hoa du lieu                    
                    String data = byteArrayToHexString(encryptAES(aesKey, aesIV,
                            strParameters));
//                    // Tao body cho request
//                    JSONObject body = new JSONObject();
//                    body.put("data", data);
//                    body.put("key", encryptedAESKey);
//                    body.put("password", encryptedAESIV);
                    
                    StringBuilder postData = new StringBuilder();
                    postData.append("data=");
                    postData.append(URLEncoder.encode(data, "UTF-8"));
                    postData.append("&key=");
                    postData.append(URLEncoder.encode(encryptedAESKey, "UTF-8"));
                    postData.append("&password=");
                    postData.append(URLEncoder.encode(encryptedAESIV, "UTF-8"));
                    
                    OutputStream os = conn.getOutputStream();
                    os.write(postData.toString().getBytes("UTF-8"));
                    os.flush();
                    os.close();
                }
            }
            int responseCode = conn.getResponseCode();
            // Success
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
		String inputLine;
                // Chuoi doi tuong tra ve
                StringBuilder strResponse = new StringBuilder();
		while ((inputLine = br.readLine()) != null) {
			strResponse.append(inputLine);
		}
		br.close();
                if (!CommonUtils.isEmpty(strResponse.toString())) {
                    EntityResponse response = GSON.fromJson(strResponse.toString(),
                            EntityResponse.class);
                    if (response.getCode() == HttpURLConnection.HTTP_OK) {
                        responseData = response.getData();
                        if (!CommonUtils.isEmpty(aesKey) && !CommonUtils.isEmpty(aesIV)) {
                            responseData = new String(decryptAES(aesKey, aesIV,
                                    hexStringToByteArray(responseData)));
                        }
                        errorDesc = "SUCCESS";
                    } else {
                        errorDesc += "Loi request - code: " + response.getCode()
                                + " - message: " + response.getMessage();
                        cleanup();
                    }
                } else {
                    errorDesc += "Loi khong co du lieu tra ve!";
                }
            } else {
                errorDesc += "Loi ket noi - http_status : " + responseCode;
            }
        } catch (Exception ex) {
            LOGGER.error("Exception: ", ex);
            errorDesc += "Exception!";
        }
        LOGGER.error("sendRequest - " + errorDesc);
//        Date endTime = new Date();
//        ActionLogMobileDAO actionLogMobileDAO = new ActionLogMobileDAO();
//        actionLogMobileDAO.insert(userGroup.getUserId1(), userGroup.getCardId(),
//                startTime, endTime, "CM." + function, strParameters, errorDesc, null,
//                null, null, null);
        return responseData;
    }
    
    /**
     * <b>Upload file</b><br>
     * 
     * @param userGroup 
     * @param file              doi tuong file upload
     * @param fileName          ten file
     * @return 
     */
    public static EntityFile upload(EntityUserGroup userGroup, File file,
            String fileName) {

        EntityFile fileInfo = null;
        String errorDesc = "ERROR: ";
        try {
//            SSLContextBuilder sslBuilder = new SSLContextBuilder();
//            sslBuilder.loadTrustMaterial(null, new TrustStrategy() {
//                @Override
//                public boolean isTrusted(X509Certificate[] chain, String authType)
//                        throws CertificateException {
//                    return true;
//                }
//            });
//            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslBuilder.build());
//            CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
            HttpPost conn = new HttpPost(ROOT_URL + "uploadFile");
            if (CommonUtils.isEmpty(token)) {
                if (!login(userGroup)) {
                    errorDesc += "Loi khong lay duoc token!";
                    LOGGER.error("upload - " + errorDesc);
                    return fileInfo;
                }
            }
            conn.addHeader("Authorization", "Bearer " + token);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            // Tao binary body cho request
            byte[] binaryBody = Files.readAllBytes(Paths.get(file.getPath()));
            binaryBody = encryptAES(aesKey, aesIV, binaryBody);
            builder.addBinaryBody("file", binaryBody, ContentType.APPLICATION_OCTET_STREAM,
                    fileName);
            JSONObject info = new JSONObject();
            info.put("fileName", fileName);
            // Tao text body cho request
            JSONObject textBody = new JSONObject();
            textBody.put("key", encryptedAESKey);
            textBody.put("password", encryptedAESIV);
            textBody.put("data", byteArrayToHexString(encryptAES(aesKey, aesIV, info.toString())));
            builder.addTextBody("data", textBody.toString(), ContentType.TEXT_PLAIN);
            HttpEntity multipart = builder.build();
            conn.setEntity(multipart);
            CloseableHttpResponse CloseableHttpResponse = HttpClients.createDefault()
                    .execute(conn);
            int statusCode = CloseableHttpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                String strResponse = EntityUtils.toString(CloseableHttpResponse.getEntity());
                if (!CommonUtils.isEmpty(strResponse)) {
                    EntityResponse response = GSON.fromJson(strResponse, EntityResponse.class);
                    if (response.getCode() == HttpURLConnection.HTTP_OK) {
                        String responseData = new String(decryptAES(aesKey, aesIV,
                                hexStringToByteArray(response.getData())));
                        fileInfo = GSON.fromJson(responseData, EntityFile.class);
                        errorDesc = "SUCCESS";
                    } else {
                        errorDesc += "Loi request - code: " + response.getCode()
                                + " - message: " + response.getMessage();
                    }
                } else {
                    errorDesc += "Loi khong co du lieu tra ve!";
                }
            } else {
                errorDesc += "Loi ket noi - http_status: " + statusCode;
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            errorDesc += "Exception!";
        }
        LOGGER.error("upload - " + errorDesc);
        return fileInfo;
    }
    
    /**
     * <b>Khoi tao gia tri key</b><br>
     * 
     * @param t0ken 
     * @param transactionKey
     */
    private static boolean initialize(String t0ken, String transactionKey) {
        
        token = t0ken;
        try {
            loginTime = (new Date()).getTime();
            rsaPublicKey = getPublicKeyFromByteArray(hexStringToByteArray(transactionKey));
            // Tao key AES
            String[] secretKey = generateSecretKey();
            aesKey = secretKey[0];
            aesIV = secretKey[1];
            // Ma hoa key AES
            encryptedAESKey = byteArrayToHexString(encryptRSA(aesKey, rsaPublicKey));
            encryptedAESIV = byteArrayToHexString(encryptRSA(aesIV, rsaPublicKey));
            return true;
        } catch (Exception ex) {
            LOGGER.error("initialize - Loi tao hoac loi ma hoa key AES!", ex);
            cleanup();
            return false;
        }
    }
    
    /**
     * <b>Don dep gia tri key cu</b><br>
     */
    private static void cleanup() {
        
        token = null;
        rsaPublicKey = null;
        aesKey = null;
        aesIV = null;
        encryptedAESKey = null;
        encryptedAESIV = null;
    }

    /**
     * <b>Lay RSA Public Key</b><br>
     * 
     * @param userGroup 
     * @return 
     */
    public static boolean createSecureTransaction(EntityUserGroup userGroup) {
        
        String fucntion = "createSecureTransaction";
        String responseData = sendRequest(userGroup, fucntion, null, false);
        if (!CommonUtils.isEmpty(responseData)) {
            return initialize(responseData, responseData);
        } else {
            LOGGER.error("createSecureTransaction - Loi khong lay duoc transactionKey!");
            return false;
        }
    }
    
    /**
     * <b>Lay token</b>
     * 
     * @param userGroup 
     * @return 
     */
    public static boolean login(EntityUserGroup userGroup) {
        
        // Lay transaction key
        if (!createSecureTransaction(userGroup)) {
            LOGGER.error("login - Loi lay transaction key!");
            return false;
        }
        String function = "auth";
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("username", "voffice");
        parameters.put("password", "123@abc");
        String responseData = sendRequest(userGroup, function, parameters, true);
        if (!CommonUtils.isEmpty(responseData)) {
            EntityAuthentication authentication = GSON.fromJson(responseData, EntityAuthentication.class);
            if (!CommonUtils.isEmpty(authentication.getToken())
                    && !CommonUtils.isEmpty(authentication.getTransactionKey())) {
                return initialize(authentication.getToken(), authentication.getTransactionKey());
            } else {
                LOGGER.error("login - Loi khong lay duoc token hoac transactionKey!");
                return false;
            }
        } else {
            LOGGER.error("login - Loi khong lay duoc thong tin xac thuc!");
            return false;
        }
    }
    
    /**
     * <b>Lay danh sach doanh nghiep</b><br>
     * 
     * @param userGroup     
     * @param keyword       tu khoa tim kiem
     * @param pageSize      so ban ghi tren mot trang
     * @param page          so trang hien tai (bat dau tu 1)
     * @return 
     */
    public static EntityListCompany listCompany(EntityUserGroup userGroup,
            String keyword, Long pageSize, Long page) {
        
        String function = "listCompany";
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("keyword", keyword);
        parameters.put("pageSize", pageSize);
        parameters.put("page", page);
        String responseData = sendRequest(userGroup, function, parameters, true);
        EntityListCompany listCompany;
        if (!CommonUtils.isEmpty(responseData)) {
            listCompany = GSON.fromJson(responseData, EntityListCompany.class);
        } else {
            LOGGER.error("listCompany - Loi khong lay duoc danh sach cong ty!");
            listCompany = new EntityListCompany();
            listCompany.setCount(0);
        }
        return listCompany;
    }
    
    /**
     * <b>Lay thong tin file</b><br>
     * 
     * @param userGroup         user
     * @param fileId            id file
     * @return 
     */
    public static EntityFile getFileInfo(EntityUserGroup userGroup, String fileId) {
        
        String function = "getFileInfo";
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("fileId", fileId);
        String responseData = sendRequest(userGroup, function, parameters, true);
        if (!CommonUtils.isEmpty(responseData)) {
            EntityFile file = GSON.fromJson(responseData, EntityFile.class);
            return file;
        } else {
            LOGGER.error("createSignDocument - Loi lay thong tin file!");
            return null;
        }
    }

    /**
     * <b>Lay thong tin file bao gom ca content cua file</b>
     * 
     * @param userGroup         user
     * @param fileId            id file
     * @param viewDetail        1: lay them trang ky
     * @return 
     */
    public static EntityFile viewFile(EntityUserGroup userGroup, String fileId,
            Integer viewDetail) {
        
        String function = "viewFile";
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("fileId", fileId);
        parameters.put("viewDetail", viewDetail);
        String responseData = sendRequest(userGroup, function, parameters, true);
        if (!CommonUtils.isEmpty(responseData)) {
            EntityFile file = GSON.fromJson(responseData, EntityFile.class);
//            // ghi file
//            try {
//                byte[] bytes = hexStringToByteArray(file.getContent());
//                FileOutputStream out = new FileOutputStream("D://55593.pdf");
//                out.write(bytes);
//                out.close();
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
            return file;
        } else {
            LOGGER.error("viewFile - Loi lay thong tin file!");
            return null;
        }
    }
    
    /**
     * <b>Chung thuc file tai len</b><br>
     * 
     * @param userGroup     user
     * @param fileId        id file
     * @return
     */
    public static EntityFile trustFile(EntityUserGroup userGroup, String fileId) {
        
        String function = "trustFile";
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("fileId", fileId);
        String responseData = sendRequest(userGroup, function, parameters, true);
        if (!CommonUtils.isEmpty(responseData)) {
            EntityFile file = GSON.fromJson(responseData, EntityFile.class);
            return file;
        } else {
            LOGGER.error("trustFile - Loi chung thuc file!");
            return null;
        }
    }
    
    /**
     * <b>Tao luong trinh ky</b><br>
     * 
     * @param userGroup             doi tuong user
     * @param fileIds               danh sach id file ky duoc tai len qua ham {@link #upload(java.io.File, java.lang.String) }
     * @param attachmentIds         danh sach id file dinh kem hoac phu luc duoc tai len qua ham {@link #upload(java.io.File, java.lang.String) }
     * @param documentName          ten van ban ky
     * @param documentType          loai van ban
     * @param signFlowObject        danh sach doanh nghiep lay tu ham {@link #listCompany(java.lang.String, java.lang.Long, java.lang.Long) }
     * @param code                  ma van ban
     * @param content               noi dung van ban
     * @param taxCode               ma so thue don vi trinh ky
     * @param image                 map chung thu voi anh ky
     * @return 
     */
    public static EntityDocument createSignDocument(EntityUserGroup userGroup,
            List<String> fileIds, List<String> attachmentIds,
            String documentName, String documentType, List<EntityCompany> signFlowObject,
            String code, String content, String taxCode, List<EntitySignature> image) {
        
        String function = "createSignDocument";
//        String function = "createNewFlow";
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("fileIds", fileIds);
        parameters.put("attachmentIds", attachmentIds);
        parameters.put("documentName", documentName);
        parameters.put("documentType", documentType);
        parameters.put("signFlowObject", signFlowObject);
        parameters.put("code", code);
        parameters.put("content", content);
        parameters.put("taxCode", taxCode);
        parameters.put("image", GSON.toJson(image));
        String responseData = sendRequest(userGroup, function, parameters, true);
        if (!CommonUtils.isEmpty(responseData)) {
            EntityDocument document = GSON.fromJson(responseData, EntityDocument.class);
            return document;
        } else {
            LOGGER.error("createSignDocument - Loi tao van ban trinh ky!");
            return null;
        }
    }
    
    /**
     * <b>Xem thong tin van ban</b><br>
     * 
     * @param userGroup         user
     * @param documentId        id van ban
     * @return 
     */
    public static EntityDocument viewDocument(EntityUserGroup userGroup, String documentId) {
        
        String function = "viewDocument";
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("documentId", documentId);
        String responseData = sendRequest(userGroup, function, parameters, true);
        if (!CommonUtils.isEmpty(responseData)) {
            EntityDocument document = GSON.fromJson(responseData, EntityDocument.class);
            return document;
        } else {
            LOGGER.error("viewDocument - Loi lay thong tin van ban!");
            return null;
        }
    }
    
    /**
     * <b>Chung thuc van ban da ky</b><br>
     * 
     * @param userGroup         user
     * @param documentId        id van ban
     * @return
     */
    public static EntityDocument trustDocument(EntityUserGroup userGroup, String documentId) {
        
        String function = "trustDocument";
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("documentId", documentId);
        String responseData = sendRequest(null, function, parameters, true);
        if (!CommonUtils.isEmpty(responseData)) {
            EntityDocument document = GSON.fromJson(responseData, EntityDocument.class);
            return document;
        } else {
            LOGGER.error("trustDocument - Loi chung thuc van ban da ky!");
            return null;
        }
    }        
    
    /**
     * <b>Ban hanh van ban</b><br>
     * 
     * @param userGroup         user
     * @param documentId        id van ban ky
     * @return
     */
    public static EntityDocument publishDocument(EntityUserGroup userGroup,
            String documentId) {
        
        String function = "publishDocument";
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("documentId", documentId);
        String responseData = sendRequest(userGroup, function, parameters, true);
        if (!CommonUtils.isEmpty(responseData)) {
            EntityDocument document = GSON.fromJson(responseData, EntityDocument.class);
            return document;
        } else {
            LOGGER.error("publishDocument - Loi ban hanh van ban!");
            return null;
        }
    }
    
    /**
     * <b>Chuyen van ban</b><br>
     * 
     * @param userGroup         user
     * @param documentId        id van ban
     * @param companies         danh sach id doanh nghiep
     * @param comment           y kien khi chuyen
     * @return 
     */
    public static boolean sendDocument(EntityUserGroup userGroup,
            String documentId, List<EntityCompany> companies, String comment) {
        
        String function = "sendDocument";
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("documentId", documentId);
        parameters.put("companies", companies);
        parameters.put("comment", comment);
        String responseData = sendRequest(userGroup, function, parameters, true);
        if (!CommonUtils.isEmpty(responseData) && responseData.equals("1")) {
            return true;
        } else {
            LOGGER.error("sendDocument - Loi chuyen van ban!");
            return false;
        }
    }
    
    /**
     * <b>Cap nhat trang thai ky</b><br>
     * 
     * @param fileIds           danh sach id file ky
     * @param attachmentIds     danh sach id file dinh kem hoac phu luc
     * @param documentId        id van ban ky
     */
    public static void updateFlowState(List<String> fileIds, List<String> attachmentIds,
            String documentId) {
        
        String function = "updateFlowState";
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("fileIds", fileIds);
        parameters.put("attachmentIds", attachmentIds);
        parameters.put("documentId", documentId);
        String s = sendRequest(null, function, parameters, true);
//        System.out.println("s = " + s);
    }
    
    /**
     * <b>Lay danh sach van ban</b><br>
     * 
     * @param userGroup
     * @param keyword       tu khoa tim kiem
     * @param type          loai
     *                      <ul>
     *                          <li>1: van ban cho ky</li>
     *                          <li>2: van ban da ky</li>
     *                          <li>3: van ban da ban hanh</li>
     *                      </ul>
     * @param dateFrom      ngay tao/ngay nhan bat dau tu (dd/MM/yyyy)
     * @param dateTo        ngay tao/ngay nhan den (dd/MM/yyyy)
     * @param pageSize      so ban ghi tren mot trang
     * @param page          so trang hien tai (bat dau tu 1)
     * @return 
     */
    public static EntityListDocument listDocument(EntityUserGroup userGroup,
            String keyword, int type, String dateFrom, String dateTo, Long pageSize,
            Long page) {
        
        String function = "listDocument";
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("keyword", keyword);
        parameters.put("status", type);
        parameters.put("dateFrom", dateFrom);
        parameters.put("dateTo", dateTo);
        parameters.put("pageSize", pageSize);
        parameters.put("page", page);
        
        String responseData = sendRequest(null, function, parameters, true);
        EntityListDocument listDocument;
        if (!CommonUtils.isEmpty(responseData)) {
            listDocument = GSON.fromJson(responseData, EntityListDocument.class);
        } else {
            LOGGER.error("listDocument - Loi khong lay duoc danh sach van ban!");
            listDocument = new EntityListDocument();
            listDocument.setCount(0);
        }
        return listDocument;
    }
    
    /**
     * <b>Cap nhat trang thai ky cua van ban</b>
     * 
     * @param userGroup         thong tin user
     * @param textId            id van ban doi tac
     * @param typeUpdate        loai cap nhat:
     *                          <ul>
     *                              <li>1: trinh ky van ban</li>
     *                              <li>2: tu choi ky van ban</li>
     *                              <li>3: dong dau van ban</li>
     *                              <li>4: ky duyet van ban</li>
     *                              <li>5: tiep nhan van ban</li>
     *                          </ul>
     * @param lastSigner        nguoi ky cuoi
     * @param image             anh ky (Base64)
     * @param fileSignId        id file ky
     * @return 
     */
    public static boolean updateStateDocument(EntityUserGroup userGroup,
            String textId, int typeUpdate, EntityText lastSigner,
            List<EntitySignature> image, String fileSignId) {
        
        String function = "updateStateDocument";
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("textId", textId);
        parameters.put("typeUpdate", typeUpdate);
        if (lastSigner != null) {
            parameters.put("comment", lastSigner.getNote());
            parameters.put("signerId", lastSigner.getEmpVhrId());
            parameters.put("signerName", lastSigner.getEmpVhrName());
            parameters.put("department", lastSigner.getDepartmentName());
        }        
        parameters.put("image", GSON.toJson(image));
        parameters.put("fileSignId", fileSignId);
        
        String responseData = sendRequest(null, function, parameters, true);
        return !CommonUtils.isEmpty(responseData) && ("1".equals(responseData) || "\"1\"".equals(responseData));
    }
    
    public static void main(String[] args) throws InterruptedException, IOException {
        
        EntityUserGroup userGroup = new EntityUserGroup(1L, 2L);
        viewDocument(userGroup, "315");
        
//        updateStateDocument(userGroup, "300", 3, null, null, null, null, null, null);
//        viewDocument(userGroup, "300");
//        viewFile(userGroup, "123468", 1);
        
//        listCompany(userGroup, "", 10l, 1l);
//        Thread.sleep(1800000);
//        listCompany(null, "", 10l, 1l);
//        List<String> s = new ArrayList<>();
//        File file = new File("D:\\Document\\Java\\Java-8-Features.pdf");
//        EntityFile fileInfo;
//        List<String> fileIds = new ArrayList<>();
//        for (int i = 0; i < 1; i++) {
//            fileInfo = upload(file, "Java-8-Features.pdf");
//            fileIds.add(fileInfo.getFileId());
//        }
//        System.out.println("fileIds: " + fileIds);
//
//        EntityFile fileInfo2 = upload(file, "Java-8-Features.pdf");
//        List<String> attachmentIds = new ArrayList<>();
//        attachmentIds.add(fileInfo2.getFileId());
//        System.out.println("attachmentId: " + fileInfo2.getFileId());
//
//        String documentName = "Hợp đồng ký kết 3 bên - " + (new Date()).getTime();
//        String documentType = "Hợp đồng";
//
//        List<Long> signFlow = new ArrayList<>();
//        signFlow.add(1L);
//        signFlow.add(2L);
//        EntityDocument document = createSignDocument(null, fileIds, attachmentIds,
//                documentName, documentType, signFlow, "code-1", "content-2");
//        System.out.println("TEST--------");
//        
//        TextPartnerDAO textPartnerDAO = new TextPartnerDAO();
//        textPartnerDAO.receiveDocument(null, document.getDocumentId());
//        viewDocument(null, "129");
//        
//        trustDocument(document.getDocumentId());
//        List<EntityCompany> listCompany = new ArrayList<>();
//        EntityCompany company = new EntityCompany();
//        company.setCompanyId(6L);
//        company.setName("Tập đoàn Công nghiệp - Viễn thông Viettel");
//        company.setTaxCode("0100109106");
//        listCompany.add(company);
//        sendDocument(userGroup, "247", listCompany, "");
//        publishDocument(null, document.getDocumentId());
//        updateFlowState(fileIds, attachmentIds, document.getDocumentId());
//        listDocument(null, "", 2, "01/01/2017", "31/12/2017", 10L, 1L);
//    
//        viewFile(null, "123196");
//        viewDocument(null, "255");
//        File file = new File("D:\\OLD_DATA\\Document\\Java\\Java-8-Features.pdf");
//        EntityFile fileInfo = upload(file, "Tính năng Java 8.pdf");
//        System.out.println("id: " + fileInfo.getFileId());
        
//        String image = Base64.encodeBase64String(Files.readAllBytes(Paths.get("D:\\default_sign.png")));
//        String image = null;
//        updateStateDocument(null, "249", 2, "OK", 387682L, "Trương Quang Việt",
//                "Trung tâm Phần mềm Viettel 1", image, null);
//        listCompany(null, "", 10L, 2L);
//        File file = new File("D:\\OLD_DATA\\Document\\Java\\Core\\corejava\\CoreJava_1.pdf");
//        String fileName = "Corejava_1.pdf";
//        upload(file, fileName);
    }
}
