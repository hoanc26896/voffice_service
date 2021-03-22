package com.viettel.voffice.callserviceother;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;
import com.viettel.voffice.utils.CommonUtils;

public class SmartRoomAPI {
    
    private static String URL = CommonUtils.getAppConfigValue("url.smartroom");

    public String sendRequest(String method, String json, String wsUrl, String etag) {
        String strResult = "";
        HttpURLConnection conn = null;
        try {
            URL url = new URL(URL + wsUrl);

            disableSslVerification();

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            if (!CommonUtils.isEmpty(etag)) {
                conn.setRequestProperty("if-match", etag);
            }
            if (!CommonUtils.isEmpty(json)) {
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Content-Length", String.valueOf(json.length()));
                conn.setDoOutput(true);
                byte[] postDataBytes = json.toString().getBytes("UTF-8");
                conn.getOutputStream().write(postDataBytes);
            }
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                strResult = response.toString();
            } else if (responseCode == HttpURLConnection.HTTP_NO_CONTENT && "DELETE".equals(method)) {
                strResult = "{\"_status\":\"OK\"}";
            } else {
                strResult = "{\"_status\":\"ERR\", \"_error\": {\"code\":\"" + responseCode  + "\", \"message\":\"Have error\"}}";
            }
        } catch (java.net.SocketTimeoutException e) {
            strResult = "{\"_status\":\"ERR\", \"_error\": {\"code\":\"203\", \"message\":\"" + e.getMessage() + "\"}}";
        } catch (IOException e) {
            strResult = "{\"_status\":\"ERR\", \"_error\": {\"code\":\"203\", \"message\":\"" + e.getMessage() + "\"}}";
        } catch (Exception e) {
            strResult = "{\"_status\":\"ERR\", \"_error\": {\"code\":\"203\", \"message\":\"" + e.getMessage() + "\"}}";
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return strResult;
    }
 
    public static void disableSslVerification() {
        try {
            TrustManager[] trustAllCerts = { new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            } };
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            HostnameVerifier allHostsValid = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (Exception ex) {
            Logger.getLogger(SmartRoomAPI.class).error("Error disable SSL: " + ex.getMessage(), ex);
        }
    }
}
