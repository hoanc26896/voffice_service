/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.elasticsearch.connectserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.viettel.voffice.constants.FunctionCommon;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.DatatypeConverter;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openide.util.Exceptions;

/**
 *
 * @author datnv5
 */
public class ConnectServer {

    private static final Logger LOGGER = Logger.getLogger(ConnectServer.class);

    /**
     * Thuc hien request server de lay du lieu
     *
     * @param urlServer
     * @param strQuery
     * @param strUserPass
     * @return
     */
    public static String sendRequestToServer(String urlServer,
            String strQuery, String strUserPass) {
//        System.out.println("urlServer:" + urlServer);
//        System.out.println("strQuery:" + strQuery);
//        System.out.println("strUserPass:" + strUserPass);
        String strResult = "";
        try {
            String strUrl = urlServer;
            URL url = new URL(strUrl);
            StringBuilder postData = new StringBuilder();

            postData.append(strQuery);
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty(
                    "Content-Type", "application/json;charset=utf-8");
            conn.setRequestProperty("Accept-Charset", "UTF-8");
            conn.setRequestProperty(
                    "Content-Length", String.valueOf(postDataBytes.length));
            //thuc hien set pass authen
            if (strUserPass != null && strUserPass.trim().length() > 0) {
                byte[] message = strUserPass.getBytes("UTF-8");
                String encoded = DatatypeConverter.printBase64Binary(message);
                conn.setRequestProperty("Authorization", "Basic " + encoded);
            }
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
                    break;
                case HttpURLConnection.HTTP_CREATED:
                    strResult = "1";
                    break;
                default:
                    strResult = String.valueOf(responseCode);
                    break;
            }
        } catch (java.net.SocketTimeoutException e) {
            LOGGER.error("sendRequestToServer", e);
        } catch (IOException e) {
            LOGGER.error("sendRequestToServer", e);
        }
        return strResult;
    }

    /**
     * Thuc hien request server de lay du lieu
     *
     * @param urlServer
     * @return
     */
    public static String sendRequestToServerGet(String urlServer) {

        String strResult = "";
        try {
            String strUrl = urlServer;
            URL url = new URL(strUrl);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept-Charset", "UTF-8");
            conn.setDoOutput(true);
            conn.setConnectTimeout(12000);
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
                    break;
                case HttpURLConnection.HTTP_CREATED:
                    strResult = "1";
                    break;
                default:
                    strResult = "";
                    break;
            }
        } catch (java.net.SocketTimeoutException e) {
            LOGGER.error("sendRequestToServer", e);
        } catch (IOException e) {
            LOGGER.error("sendRequestToServer", e);
        }
        return strResult;
    }

    /**
     * Tra ve so luong hoac objectArr khi tim kiem du lieu tren elastic
     *
     * @param jsonServerResponse
     * @param isCount
     * @return
     */
    public static Object getJsonDataRequest(String jsonServerResponse,
            boolean isCount) {
        try {
            JSONObject json = new JSONObject(jsonServerResponse);
            if (json.isNull("hits")) {
                return null;
            }
            JSONObject jsonhits = (JSONObject) json.get("hits");
            if (isCount) {
                //tra ve so luong ban ghi
                if (!jsonhits.isNull("total")) {
                    String count = jsonhits.getString("total");
                    if (FunctionCommon.isNumeric(count)) {
                        return Long.valueOf(count);
                    } else {
                        return 0L;
                    }
                } else {
                    return 0L;
                }
            }
            if (jsonhits.isNull("hits")) {
                return null;
            }
            JSONArray array = jsonhits.getJSONArray("hits");
            return array;
        } catch (JSONException ex) {
            LOGGER.error("getJsonDataRequest", ex);
        }
        return null;
    }

    /**
     * Tra ve so luong hoac objectArr khi tim kiem du lieu tren elastic
     *
     * @param jsonServerResponse
     * @param classOfT
     * @return
     */
    public static Object getJsonFromDataGet(String jsonServerResponse, Class<?> classOfT) {
        try {
            JSONObject json = new JSONObject(jsonServerResponse);
            if (json.isNull("_source")) {
                return null;
            }
            String jsonData = json.getString("_source");

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            Object result = gson.fromJson(jsonData, classOfT);
            return result;
        } catch (JSONException ex) {
            LOGGER.error("getJsonDataRequest", ex);
        }
        return null;
    }

    /**
     * lay danh sach tu hightlight
     *
     * @param strHightlight
     * @return
     */
    public static List<String> getArrHightlight(String strHightlight) {
        List<String> listResult = new ArrayList<>();
        Pattern pattern = Pattern.compile("<b>(.*?)</b>");
        Matcher matcher = pattern.matcher(strHightlight);
        boolean have = false;
        while (matcher.find()) {
            have = true;
            listResult.add(matcher.group(1));
        }
        if (!have) {
            listResult = null;
        }
        return listResult;
    }

//    public static void main(String[] args) {
//        
//
//        String aaa = sendRequestToServerSoap("http://10.60.108.20:8022/WS_CA/CaServiceForVoffice", "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://WebService.DAO.database.cm.bccs.viettel.com/\"><soapenv:Header/><soapenv:Body><web:getCertVoffice><getCertVoffice><csr>\n" +
//        "MIIBpDCCAQ0CAQEwZDEWMBQGA1UEChMNVmlldHRlbCBHcm91cDELMAkGA1UECBMC\n" +
//        "SE4xCzAJBgNVBAYTAlZOMQ4wDAYDVQQLEwVTdGFmZjEPMA0GA1UEAxMGMTk1MjU2\n" +
//        "MQ8wDQYDVQQHEwZIYSBOb2kwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAKnq\n" +
//        "Sbkey9zUU4EcMeU5SKUqEPaYUcAwb/2VGcWXbikvnsgeiwoYi4waNJ8q2x130m8F\n" +
//        "qsHzvvGFQWRLyuBYTcPvN4msJYR+rtiKOV9h4PWaJ1a13C9j3qmmGUmsqQjdaZlk\n" +
//        "nbyvL+mXy+Wdc7iRX0KhgsVUJAYu9Y45H2w/14uFAgMBAAGgADANBgkqhkiG9w0B\n" +
//        "AQUFAAOBgQBdvW2m9L1XWZ0CUeDpstrt+M5lMam13VlKVYAvYkjPKQtXVSAEEdjQ\n" +
//        "9Om32XvY9wFJOlqxgiKqR/AL+V0C/HawnUFY0bpQpmujFlAmke2odHHnmOGKvbty\n" +
//        "8QUjmkGnSvHK+IEN/tHiPbghrEiPWI87yL7SgI+l6xa73RuiHI5kGA==\n" +
//        "</csr><serialCts>540101050000000059a4fbb61d540a79</serialCts><idNo>095126988</idNo><merchantCode>VOFFICEVTG</merchantCode><orderId>CERT49195503022020151133165</orderId><custName>Lê Thị Hà</custName><custAddress>Mỹ Đình - Hà Nội</custAddress><custPhone>84983725525</custPhone><custEmail>test@viettel.com.vn</custEmail></getCertVoffice></web:getCertVoffice></soapenv:Body></soapenv:Envelope>");
//        
//        
//        System.out.println("aaaa=" + aaa);
//    }

    public static String sendRequestToServerSoap(String urlServer, String strQuery) {
        final String urlServer1 = urlServer;
        final String strQuery1 = strQuery;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(new Callable() {
            @Override
            public String call() throws Exception {
                String strResult = "";
                try {
                    String strUrl = urlServer1;
                    URL url = new URL(strUrl);
                    StringBuilder postData = new StringBuilder();

                    postData.append(strQuery1);
                    byte[] postDataBytes = postData.toString().getBytes("UTF-8");

                    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty(
                            "Content-Type", "text/xml;charset=utf-8");
                    conn.setRequestProperty("Accept-Charset", "UTF-8");
                    conn.setRequestProperty(
                            "Content-Length", String.valueOf(postDataBytes.length));
                    conn.setDoOutput(true);
                    conn.getOutputStream().write(postDataBytes);
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(25000);
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
                                    response.append("\n");
                                }
                            }   // print result
                            strResult = response.toString();
                            break;
                        case HttpURLConnection.HTTP_CREATED:
                            strResult = "1";
                            break;
                        default:
                            strResult = String.valueOf(responseCode);
                            break;
                    }
                } catch (java.net.SocketTimeoutException e) {
                    LOGGER.error("sendRequestToServer", e);
                } catch (IOException e) {
                    LOGGER.error("sendRequestToServer", e);
                }
                return strResult;
            }
        });
        
        try {
            return future.get(30, TimeUnit.SECONDS); //timeout is in 2 seconds
        } catch (TimeoutException| InterruptedException | ExecutionException ex) {
            LOGGER.error("sendRequestToServer11", ex);
        }
        executor.shutdownNow();
        return "-1";
    }
}
