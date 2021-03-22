/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.constants;

//import com.whalin.MemCached.MemCachedClient;
//import com.whalin.MemCached.SockIOPool;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.Query;
import javax.servlet.http.HttpServletRequest;
import javax.sql.rowset.serial.SerialBlob;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.viettel.voffice.controler.CommonControler;
import com.viettel.voffice.controler.SecurityControler;
import com.viettel.voffice.database.dao.ConfigParameterDAO;
import com.viettel.voffice.database.dao.logAction.LogActionDao;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.VtPayEntity;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.LogUtils;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;
/**
 *
 * @author datnv5
 */
public class FunctionCommon {

    private static final Logger LOGGER = Logger.getLogger(FunctionCommon.class);
    private static final ResourceBundle RESOURCE_BUNDLE = getResourceBundle();
    private static MemcachedClient CACHECLIENT = getMemcachedClientCf();
    private static List<MemcachedClient> LISTCACHECLIENT = getListMemcachedClientCf();
    public static final String IPPORTSERVICE = getIpAddressAndPort();

    /**
     * ************************************************************************
     *****======HAM HO TRO THAO TAC GOI CAU HINH STATIC FINAL============****
     * ******************************DATNV5***********************************
     *
     * /**
     * lay file cau hinh properties
     *
     * @return
     */
    private static ResourceBundle getResourceBundle() {
        ResourceBundle appConfigRB = ResourceBundle.getBundle(
                StringConstants.STR_FILE_CONFIG);
        return appConfigRB;
    }

    /**
     * Khoi tao goi server mem
     *
     * @return
     */
    private static MemcachedClient getMemcachedClientCf() {
        MemcachedClient memcachedClient;
        try {
            System.setProperty("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.SunLogger");
            Logger.getLogger("net.spy.memcached").setLevel(Level.WARN);
            String strServerCacheMem
                    = FunctionCommon.getPropertiesValue("serverMem");
            memcachedClient
                    = new MemcachedClient(AddrUtil.getAddresses(strServerCacheMem));
        } catch (IOException ex) {
            LOGGER.error("Loi! getCache: ", ex);
            memcachedClient = null;
        }
        return memcachedClient;
    }
    
    /**
     * Khoi tao goi server mem
     *
     * @return
     */
    private static List<MemcachedClient> getListMemcachedClientCf() {
        List<MemcachedClient> listResult = null;
        String strServerCacheMem
                    = FunctionCommon.getPropertiesValue("serverMem").trim();
        if(strServerCacheMem.length() >0 ){
            String[] listServerMem = strServerCacheMem.split(" ");
            if(listServerMem.length>0){
                System.setProperty("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.SunLogger");
                Logger.getLogger("net.spy.memcached").setLevel(Level.WARN);
                listResult = new ArrayList<>();
                for (String stringServerMem : listServerMem) {
                    MemcachedClient memcachedClient;
                    try {
                        String strIpMem = stringServerMem.trim();
                        if(strIpMem.length()>5){
                            memcachedClient
                                    = new MemcachedClient(AddrUtil.getAddresses(strIpMem));
                            listResult.add(memcachedClient);
                        }
                    } catch (IOException ex) {
                        LOGGER.error("Loi! getListMemcachedClientCf: " + stringServerMem, ex);
                    }
                }
            }
        }
        return listResult;
    }
    /**
     * ***********************************************************************
     *****===========HAM CHUNG GOI THAO TAC HE THONG===============****
     * *******************************DATNV5***********************************
     */
    /**
     * loi mem thi thuc hien ket noi lai
     *
     * @return
     */
    public static MemcachedClient getReCache() {
        if(CACHECLIENT != null){
            CACHECLIENT.shutdown();
        }
        CACHECLIENT = getMemcachedClientCf();
        return CACHECLIENT;
    }

    /**
     * Khoi tao lai ket noi
     * @return 
     */
    public static List<MemcachedClient> getReListCache() {
        if(LISTCACHECLIENT != null && LISTCACHECLIENT.size()>0){
            for (MemcachedClient memcachedClient : LISTCACHECLIENT) {
                memcachedClient.shutdown();
            }
        }
        LISTCACHECLIENT = getListMemcachedClientCf();
        return LISTCACHECLIENT;
    }
    
    /**
     * lay cache connect
     *
     * @return
     */
    public static MemcachedClient getCache() {
        if(CACHECLIENT == null){
            getReCache();
        }
        return CACHECLIENT;
    }
/**
     * lay cache connect
     *
     * @return
     */
    public static List<MemcachedClient> getListCache() {
        if(LISTCACHECLIENT == null){
            getReListCache();
        }
        return LISTCACHECLIENT;
    }
    /**
     * lay du lieu theo key từ client truyen len
     *
     * @param item
     * @param strJsonData
     * @return
     */
    public static Object jsonGetItem(String item, String strJsonData) {
        Object result = null;
        try {
            JSONObject obj = new JSONObject(strJsonData);
            if (!obj.isNull(item)) {
                result = obj.get(item);
            }
        } catch (Exception e) {
            LOGGER.error("Loi! jsonGetItem ", e);
        }
        return result;
    }

    /**
     * convert json to object
     *
     * @param strJsonData
     * @param classOfT
     * @return
     */
    public static Object convertJsonToObject(String strJsonData, Class<?> classOfT) {
        Object result = null;
        try {
//            GsonBuilder builder = new GsonBuilder();
//            Gson gson = builder.create();
            Gson gson
                    = new GsonBuilder()
                    .registerTypeAdapter(int.class, new GsonEmptyStringToNumber.EmptyStringToNumberTypeAdapter())
                    .registerTypeAdapter(Integer.class, new GsonEmptyStringToNumber.EmptyStringToNumberTypeAdapter())
                    .registerTypeAdapter(long.class, new GsonEmptyStringToNumber.EmptyStringToLongTypeAdapter())
                    .registerTypeAdapter(Long.class, new GsonEmptyStringToNumber.EmptyStringToLongTypeAdapter())
                    .registerTypeAdapter(double.class, new GsonEmptyStringToNumber.EmptyStringToDoubleTypeAdapter())
                    .registerTypeAdapter(Double.class, new GsonEmptyStringToNumber.EmptyStringToDoubleTypeAdapter())
                    .create();
            result = gson.fromJson(strJsonData, classOfT);
        } catch (Exception e) {
            LOGGER.error("Loi! convertJsonToObject ", e);
        }
        return result;
    }

    /**
     * convert json to listObject
     *
     * @param json
     * @param classOfT
     * @return
     */
    public static List<? extends Object> convertJsonToListObject(
            String json, Class<?> classOfT) {
        List<Object> result = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject explrObject = jsonArray.getJSONObject(i);
                result.add(convertJsonToObject(explrObject.toString(), classOfT));
            }
        } catch (JSONException ex) {
            LOGGER.error("Loi! convertJsonToObject ", ex);
        }
        if (result.size() <= 0) {
            result = null;
        }
        return result;
    }

    /**
     * Kiem tra xem cot select trong database va class co cot tuong ung hay
     * khong
     *
     * @param rs
     * @param columnName
     * @return
     * @throws SQLException
     */
    public static boolean hasColumn(ResultSet rs, String columnName)
            throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columns = rsmd.getColumnCount();
        for (int x = 1; x <= columns; x++) {
            String cl = rsmd.getColumnName(x);
            if (columnName.toLowerCase().equals(cl.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    //=========Nhom ham thao tac database==========================
    /**
     * Thuc hien convert date to date sql de day vao dieu kien
     *
     * @param date
     * @return
     */
    public static Timestamp convertDateToSql(Date date) {
        Timestamp result = null;
        try {
            result = new Timestamp(date.getTime());
        } catch (Exception e) {
            LOGGER.error("Loi! convertDateToSql ", e);
        }
        return result;
    }

    /**
     * Loai bo khoang trang du thua
     *
     * @param str
     * @return
     */
    public static String trimspace(String str) {
        str = str.replaceAll("\\s+", " ");
        str = str.replaceAll("(^\\s+|\\s+$)", "");
        return str;
    }

    /**
     * loai bo ky tu dac biet tim kiem sql
     *
     * @param input
     * @return
     */
    public static String escapeSql(String input) {
        String result = input.trim().replace("/", "//").replace("_", "/_").replace("%", "/%");
        return result;
    }

    /**
     * lay ra khoang replace va ban ghi can thay the chu y: cau truc sql thay
     * the se phai thoa man dang where staffid in(:ListStaffId) and : cac ky tu
     * trong chuoi in phai sat canh nhau
     *
     * @param strSql
     * @param variale
     * @return
     */
    public static int[] getReplaceSqlInArr(String strSql, String variale) {
        int[] result = new int[4];
        int indext = strSql.indexOf(variale);
        int i = indext;
        int spase = 0;
        int end = indext + variale.length() + 1;
        //ket thuc ten cot can ghep dieu kien
        int charEnd = 0;
        int strInOrNotin = 0;
        int start = 0;
        while (true) {
            char a_char = strSql.charAt(i);

            if (a_char == ' ') {
                spase++;
            }
            if (spase == 1) {
                //gap khoang trong dau thi lay luon vi tri khoang trong
                charEnd = i;
                spase = 2;
            }
            if (spase == 3) {
                //gap khoang trong tiep theo thi danh dau vi tri dau can thay
                start = i;
                break;
            }
            i--;
            if (a_char == '(') {
                strInOrNotin = i;
            }
        }
        result[0] = start;
        result[1] = charEnd;
        result[2] = end;
        result[3] = strInOrNotin;
        return result;
    }

    /**
     * sap xep thu tu Hmap
     *
     * @param hmap
     * @return
     */
    public static List<String> sortHmap(HashMap<Integer, Object> hmap) {
        List<String> result = new ArrayList<>();
        if (hmap != null) {
            Map<Integer, Object> map = new TreeMap<>(hmap);
            Set set2 = map.entrySet();
            Iterator iterator2 = set2.iterator();

            while (iterator2.hasNext()) {
                Map.Entry me2 = (Map.Entry) iterator2.next();
                result.add(me2.getValue().toString());
            }

        }
        return result;
    }

    /**
     * ham thuc hien select sql theo dang like string
     *
     * @param strColumn
     * @return
     */
    public static String sql_SelectLike(String strColumn) {
        StringBuilder strSqlLike = new StringBuilder();
        strSqlLike.append(String.format(" TRANSLATE(lower(%s),'", strColumn));
        strSqlLike.append(StringConstants.strSpec);
        strSqlLike.append("', '");
        strSqlLike.append(StringConstants.strRepl);
        strSqlLike.append(String.format("') like :%S ESCAPE '/' ", strColumn));
        return strSqlLike.toString();
    }

    /**
     * ham thuc hien select sql theo dang like string
     *
     * @param strColumn
     * @return
     */
    public static String sql_SelectLikeAsk(String strColumn) {
        StringBuilder strSqlLike = new StringBuilder();
        strSqlLike.append(String.format(" TRANSLATE(lower(%s),'", strColumn));
        strSqlLike.append(StringConstants.strSpec);
        strSqlLike.append("', '");
        strSqlLike.append(StringConstants.strRepl);
        strSqlLike.append("') like ? ESCAPE '/' ");
        return strSqlLike.toString();
    }

    /**
     * remove dau ca chuoi text
     *
     * @param s
     * @return
     */
    public static String removeUnsign(String s) {
        StringBuilder sb = new StringBuilder(s);
        Character ch;
        for (int i = 0; i < sb.length(); i++) {
            sb.setCharAt(i, convertUnsign(sb.charAt(i)));
            ch = sb.charAt(i);
            if (ch.hashCode() < 31 || ch.hashCode() >= 127) {
                sb.deleteCharAt(i);
            }
        }

        return sb.toString();
    }

    /**
     * convert unsign 1 ky tu
     *
     * @param ch
     * @return
     */
    private static char convertUnsign(char ch) {
        int index = StringConstants.strSpec.indexOf(ch);
        if (index >= 0) {
            ch = StringConstants.replaceUnsign()[index];
        }
        return ch;
    }

    //======================Nhom ham thao tac ma hoa giai ma du lieu=========
    /**
     * Convert tu hexString ra byte[]
     *
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
     * Convert tu bytes ra Hex
     *
     * @param bytes
     * @return
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String createTokenRandom() {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[20];
        random.nextBytes(bytes);
        String token = Arrays.toString(bytes);
        return token;
    }

    //============Nhom ham xu li du lieu tra ve client va lay du lieu client=========
    /**
     * Sinh chuoi json tra ve
     *
     * @param errorCode
     * @param obj Du lieu tra ve
     * @param aesKey Key AES ma hoa du lieu tra ve
     * @return
     * @deprecated use {@link #responseResult} instead.
     */
    public static String generateResponseJSON(ErrorCode errorCode, Object obj,
            String aesKey) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ErrorCode.class,
                new ErrorCodeEnumAdapterTypeAdapter<ErrorCode>());
        Gson gson = gsonBuilder.create();
        String response;
        String mess = gson.toJson(errorCode);
        String data;
        if (obj != null) {
            response = StringConstants.STR_RESULT_RETURN_FULL;
            data = gson.toJson(obj).replace("\\u003c\\\\", "\u003c");
            if (!CommonUtils.isEmpty(aesKey)) {
                data = "\"" + SecurityControler.encodeDataByAes(aesKey, data) + "\"";
            }
            response = String.format(response, mess, data);
        } else {
            response = StringConstants.STR_RESULT_RETURN_MESS;
            response = String.format(response, mess);
        }
        return response;
    }

    /**
     * convert data to Json
     *
     * @param obj
     * @return
     */
    public static String generateJSONBase(Object obj) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        String data;
        try {
            data = gson.toJson(obj);
        } catch (Exception e) {
            data = null;
            LOGGER.error("Loi! generateJSONBase", e);
        }
        return data;
    }

    /**
     * Lay ra danh sach value theo key tu chuoi json
     *
     * @param json
     * @param keys
     * @return
     * @throws JSONException
     * @deprecated use {@link #getDataFromClient} instead.
     */
    @Deprecated
    public static List<String> getValuesFromJSON(JSONObject json, String[] keys)
            throws JSONException {
        List<String> listValue = new ArrayList<>();
        if (json != null && keys != null && keys.length > 0) {
            for (int i = 0; i < keys.length; i++) {
                if (json.isNull(keys[i])) {
                    listValue.add("");
                } else {
                    listValue.add(json.getString(keys[i]).trim());
                }
            }
        }
        return listValue;
    }

    /**
     * Thuc hien chuc nang kiem tra session nguoi dung trong he thong
     *
     * @param httpRequest
     * @return
     * @deprecated use {@link #getDataFromClient} instead.
     */
    @Deprecated
    public static EntityUserGroup getStatusSession(HttpServletRequest httpRequest) {
        EntityUserGroup result;
        String sessionId = getStrSessionIdByHttpRQ(httpRequest);
        if (sessionId != null && sessionId.trim().length() > 0) {
            //Lay data theo session trong cache
            //String data = CommonControler.getDataCache(sessionId);
            String data = CommonControler.getDataCache_Mem(sessionId);
            if (data == null || data.trim().length() == 0) {
                data = CommonControler.getDataCache_Mem(sessionId);
            }
            EntityUserGroup entityUserGroup = null;
            if (data != null && data.trim().length() > 0) {
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                entityUserGroup = gson.fromJson(data, EntityUserGroup.class);
            }
            // - (1) Khong ton doi tuong user voi session tuong ung
            // - (2) Ton tai user + (21) Khong co thong tin user tren he thong 1
            //                    + (22) Khong co thong tin user tren he thong 2
            if (entityUserGroup == null || (entityUserGroup.getItemEntityUser() == null
                    && entityUserGroup.getVof2_ItemEntityUser() == null)) {
                result = new EntityUserGroup();
                result.setCheckSessionOk(false);
                result.setEnumErrCode(ErrorCode.ERR_SESSION_TIME_OUT);
            } else {
                result = entityUserGroup;
                result.setCheckSessionOk(true);
                result.setEnumErrCode(ErrorCode.SUCCESS);
            }
        } else {
            //Khong lay duoc session trong request
            result = new EntityUserGroup();
            result.setCheckSessionOk(false);
            result.setEnumErrCode(ErrorCode.ERR_NOSESSION);
        }
        return result;
    }

    /**
     * kiem tra du lieu va tra ve cho client
     *
     * @param result
     * @return
     */
    public static String getResultForClient(String result) {
        String strResult;
        if (result != null) {
            strResult = result;
        } else {
            strResult = FunctionCommon.generateResponseJSON(
                    ErrorCode.ERR_NODATA, null, null);
        }
        return strResult;
    }

    /**
     * Lay danh sach doi tu client gui len trong khoi ma hoa du lieu
     *
     * @param parameter
     * @param data
     * @param entityGr
     * @return
     */
    public static HashMap<String, Object> getListParamsClient(
            HashMap<String, Object> parameter, String data,
            EntityUserGroup entityGr) {
        HashMap<String, Object> hmapResult = new HashMap<>();
        try {
            //giai ma data tu client gui len
            String strAesKey = entityGr.getStrAesKey();

            String dataParams = null;
            if (strAesKey != null && strAesKey.trim().length() > 0) {
                dataParams = SecurityControler.decodeDataByAes(strAesKey, data);
            }
            JSONObject json = new JSONObject(dataParams);
            if (parameter != null) {
                //duyet mang params
                for (Map.Entry<String, Object> entry : parameter.entrySet()) {
                    //tu khoa lay gia tri params
                    String key = entry.getKey().trim();
                    //kieu bien cua prams
                    Object valueType = entry.getValue();
                    if (key != null) {
                        Object objValueParams = getValuesFromItemJSONToObj(json,
                                key, valueType);
                        hmapResult.put(key, objValueParams);
                    }
                }
            }

        } catch (JSONException ex) {
            hmapResult = null;
            LOGGER.error("Loi! doi tuong truyen len server khong phai Json", ex);
        }
        return hmapResult;
    }

    /**
     * Ho tro: Lay tu json tra ve gia tri kieu Object tuong ung can convert
     *
     * @param json
     * @param keys
     * @param type
     * @return
     * @throws JSONException
     */
    private static Object getValuesFromItemJSONToObj(JSONObject json,
            String keys, Object type) {
        Object result = null;
        Object valueParams = jsonGetItem(keys, json.toString());
        if (type.equals(Integer.class)) {
            //du lieu kieu int
            try {
                if (valueParams != null && valueParams.toString().length() > 0) {
                    Integer valueInt = Integer.valueOf(valueParams.toString());
                    result = valueInt;
                }
            } catch (Exception e) {
                result = null;
                LOGGER.error("Loi! lay du lieu client truyen len", e);
            }

        }
        if (type.equals(Long.class)) {
            //du lieu kieu Long
            try {
                if (valueParams != null && valueParams.toString().length() > 0) {
                    Long valueLong = Long.valueOf(valueParams.toString());
                    result = valueLong;
                }
            } catch (Exception e) {
                result = null;
                LOGGER.error("Loi! lay du lieu client truyen len", e);
            }
        }
        if (type.equals(String.class)) {
            //du lieu kieu String
            try {
                String valueString = "";
                if (valueParams != null && valueParams.toString().length() > 0) {
                    valueString = String.valueOf(valueParams);
                }
                result = valueString;
            } catch (Exception e) {
                result = null;
                LOGGER.error("Loi! lay du lieu client truyen len", e);
            }
        }
        if (type.equals(Date.class)) {
            try {
                //du lieu kieu int
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                String dateInString = String.valueOf(valueParams);
                if (dateInString != null && dateInString.trim().length() > 0) {
                    Date valueDate = formatter.parse(dateInString);
                    result = valueDate;
                }
            } catch (ParseException ex) {
                LOGGER.error("Loi! convert doi tuong dang ngay "
                        + "o params client truyen len", ex);
                result = null;
            }
        }
        // Datdc them kieu boolean
        if (type.equals(boolean.class)) {
            //du lieu kieu boolean
            try {
                if (valueParams != null && valueParams.toString().length() > 0) {
                    Boolean valueB = Boolean.valueOf(valueParams.toString());
                    result = valueB;
                }
            } catch (Exception e) {
                result = null;
                LOGGER.error("Loi! lay du lieu client truyen len", e);
            }
        }
        return result;
    }

    /**
     * Ho tro: kiem tra trang thai session thuc hien lay chuoi sessionid theo
     * trang thai dang nhap cua ca nhan
     *
     * @param httpRequest
     * @return
     */
    public static String getStrSessionIdByHttpRQ(HttpServletRequest httpRequest) {
        String sessionId;
        if (httpRequest.getHeader(StringConstants.STR_SESSIONID) != null
                && !httpRequest.getHeader(StringConstants.STR_SESSIONID).equals(
                StringConstants.STR_EMTY)) {
            //lay session tu chuoi header "session_id"
            sessionId = httpRequest.getHeader(StringConstants.STR_SESSIONID);
        } else if (httpRequest.getHeader(StringConstants.STR_COOKIE) != null
                && !httpRequest.getHeader(StringConstants.STR_COOKIE).equals(
                StringConstants.STR_EMTY)) {
            //lay session tu chuoi header "session_id"
            sessionId = httpRequest.getHeader(StringConstants.STR_COOKIE).
                    replace(StringConstants.STR_JSESSIONID, StringConstants.STR_EMTY);
        } else {
            //lay session add vao header mac dinh
            sessionId = httpRequest.getRequestedSessionId();
        }
        return sessionId;
    }
//    /**
//     * thuc hien nhieu thread chạy song song
//     *
//     * @param listRunAble
//     */
//    private static ExecutorService executorService;
//    public static void startListThread(List<Runnable> listRunAble) {
//        try {
//            if (executorService == null) {
//                executorService = Executors.newFixedThreadPool(NumberConstants.I_SUM_THREAD);
//            }
//            for (int i = 0; i < listRunAble.size(); i++) {
//                executorService.execute(listRunAble.get(i));
//            }
//            executorService.awaitTermination(NumberConstants.L_TIMESECON_WAITINGTHREAD, TimeUnit.SECONDS);
//        } catch (InterruptedException ex) {
//            log.error("Loi! Khong thuc hien duoc cac tien trinh song song: " + ex.getMessage());
//        }
//    }

    /**
     * lay du lieu theo key từ client truyen len
     *
     * @param item
     * @param strJsonData
     * @return
     */
    public static JSONArray jsonGetArray(String item, String strJsonData) {
        JSONArray result = null;
        try {
            JSONObject obj = new JSONObject(strJsonData);
            if (!obj.isNull(item)) {
                result = obj.getJSONArray(item);
            }
        } catch (Exception e) {
            LOGGER.error("Loi! jsonGetArray ", e);
        }
        return result;
    }

    /**
     * doc file cau hinh
     *
     * @param strConfig
     * @return
     * @throws java.io.IOException
     * @deprecated use {@link #getPropertiesValue} instead.
     */
    @Deprecated
    public static String getConfigFile(String strConfig) throws IOException {
        String strSourdFile = getPropertiesValue(strConfig);
        return strSourdFile;
    }

    /**
     * doc file cau hinh storage
     *
     * @param strConfig
     * @return
     * @throws java.io.IOException
     * @deprecated use {@link #getPropertiesValue} instead.
     */
    @Deprecated
    public static String getStorageConfigFile(String strConfig)
            throws IOException {
        String strStorageFile = getPropertiesValue(strConfig);
        return strStorageFile;
    }

    /**
     * doc file properties trong cau hinh thu muc default
     *
     * @param key
     * @return
     */
    public static String getPropertiesValue(String key) {
        String value = FunctionCommon.RESOURCE_BUNDLE.containsKey(key)
                ? RESOURCE_BUNDLE.getString(key)
                : StringConstants.STR_EMTY;
        if (value.trim().length() <= 0) {
            LOGGER.error("Not value with key:" + key + ", in file properties");
        }
        return value;
    }

    /**
     * loc ky tu dac biet chen the cho lich hop hiendv2
     *
     * @param search
     * @return
     */
    public static String removeSpecial(String search) {
        String res = "";
        try {
            search = search.replaceAll("<b>", "");
            search = search.replaceAll("</b>", "");
            search = search.replaceAll("<br>", "");
            search = search.replaceAll("</br>", ";");
            search = search.replaceAll("BTGĐ:", "-");
            search = search.replaceAll("Cá nhân:", "-");
            search = search.replaceAll("Đơn vị:", "-");
            search = search.replace(';', '\n');
            //res = res.replaceAll("(?=[]\\[;,^\"~*?\\\\])", "\\\\");
            if ("".equals(res.trim())) {
                res = search;
            }
        } catch (Exception ex) {
            LOGGER.error("ERR: removeSpecial", ex);
        }
        return res;
    }

    /**
     * Sinh ra dieu kien sql khi tim kiem 1 truong theo text
     *
     * @param columnName
     * @return
     */
    public static String generateSQLConditionForSearchText(String columnName) {
        StringBuilder condition = new StringBuilder();
        condition.append(String.format(" translate(lower(%s),'", columnName));
        condition.append(StringConstants.strSpec);
        condition.append("', '");
        condition.append(StringConstants.strRepl);
        condition.append("') like ? escape '/' ");
        return condition.toString();
    }
    
    public static String searchLikeSpecialCharacter(String columnName) {
        StringBuilder condition = new StringBuilder();
        condition.append(String.format(" REPLACE(Convert(translate(lower(%s),'", columnName));
        condition.append(StringConstants.strSpec);
        condition.append("', '");
        condition.append(StringConstants.strRepl);
        condition.append("'),'US7ASCII'), '?', '') like ? escape '/' ");
        return condition.toString();
    }

    /**
     * Sinh cac dau hoi cham (?) de noi vao cau SQL
     *
     * @param size So luong dau hoi
     * @return
     */
    public static String generateQuestionMark(int size) {

        if (size == 0) {
            return "";
        }
        StringBuilder questionMarks = new StringBuilder();
        for (int i = 0; i < size - 1; i++) {
            questionMarks.append(" ?, ");
        }
        questionMarks.append(" ? ");
        return questionMarks.toString();
    }

    /**
     * Lay ky danh gia hien tai
     *
     * @author thanght6
     * @return
     */
    public static String getCurrentPeriod() {
        String result;
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        result = sdf.format(date);
        return result;
    }

    /**
     * Lay danh sach id vai tro lanh dao tu file cau hinh
     *
     * @return
     */
    public static List<Long> getListLeaderRoleId() {
        List<Long> result = new ArrayList<>();
        try {
            String configValue = FunctionCommon.getConfigFile("roleLeaderIds");
            if (configValue == null) {
                return result;
            }
            if (configValue.trim().length() == 0) {
                return result;
            }
            String[] ids = configValue.split(",");
            if (ids.length == 0) {
                return result;
            }
            for (String str : ids) {
                Long id = Long.parseLong(str);
                result.add(id);
            }
        } catch (IOException ex) {
            LOGGER.error("Loi! jsonGetArray: ", ex);
        }
        return result;
    }

    public static String removeStringDateSymb(String strDate) {
        return strDate.replace("\\/", "\\");
    }

    /**
     * thuc hien ghi log he thong
     *
     * @param status 1: log err, 2: log infor
     * @param ex: exception
     */
    public static void writeLog(int status, Exception ex) {
        if (status == 1) {
            LOGGER.error("Loi! getCache: " + ex.getMessage());
        } else {
            LOGGER.info("Loi! getCache: " + ex.getMessage());
        }
    }

    /**
     * thuc hien ghi log text
     *
     * @param status
     * @param strFunction
     * @param strLog
     */
    public static void writeLog(int status, String strFunction, String strLog) {
        if (status == 1) {
            LOGGER.error("Loi! getCache  " + strFunction + ":" + strLog);
        } else {
            LOGGER.info("Loi! getCache: " + strFunction + ":" + strLog);
        }
    }

    /**
     * check chuoi co phai la so hay khong
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    /**
     * <b>Kiem tra phien lam viec theo sessionId</b><br>
     *
     * @author thanght6
     * @since Aug 29, 2016
     * @param sessionId Id session
     * @return
     */
    public static EntityUserGroup checkSessionById(String sessionId) {

        EntityUserGroup result;
        if (sessionId != null && sessionId.trim().length() > 0) {
            //Lay data theo session trong cache
            //String data = CommonControler.getDataCache(sessionId);
            String data = CommonControler.getDataCache_Mem(sessionId);
            if (data == null || data.trim().length() == 0) {
                data = CommonControler.getDataCache_Mem(sessionId);
            }
            EntityUserGroup entityUserGroup = null;
            if (data != null && data.trim().length() > 0) {
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                entityUserGroup = gson.fromJson(data, EntityUserGroup.class);
            }
            // - (1) Khong ton doi tuong user voi session tuong ung
            // - (2) Ton tai user + (21) Khong co thong tin user tren he thong 1
            //                    + (22) Khong co thong tin user tren he thong 2
            if (entityUserGroup == null || (entityUserGroup.getItemEntityUser() == null
                    && entityUserGroup.getVof2_ItemEntityUser() == null)) {
                result = new EntityUserGroup();
                result.setCheckSessionOk(false);
                result.setEnumErrCode(ErrorCode.ERR_SESSION_TIME_OUT);
            } else {
                result = entityUserGroup;
                result.setCheckSessionOk(true);
                result.setEnumErrCode(ErrorCode.SUCCESS);
            }
        } else {
            //Khong lay duoc session trong request
            result = new EntityUserGroup();
            result.setCheckSessionOk(false);
            result.setEnumErrCode(ErrorCode.ERR_NOSESSION);
        }
        return result;
    }

    /**
     * tao token ngau nhien voi 8 ky tu
     *
     * @return tra ve gia tri token random
     */
    public static int genTokenCode() {
        Random generator = new Random(new Date().getTime());
        return (10000000 + generator.nextInt(90000000 - 1));
    }

    /**
     * Lay ra danh sach value theo key tu chuoi json
     *
     * @param userGroup
     * @param data
     * @param keys
     * @return
     * @deprecated use {@link #getDataFromClient} instead.
     */
    @Deprecated
    public static List<String> getParamsClientSend(EntityUserGroup userGroup,
            String data, String[] keys) {
        //kiem tra neu ton tai session dang nhap
        List<String> listValue = null;
        String dataDecode = "";
        if (userGroup.getCheckSessionOk()) {
            String aesKey = userGroup.getStrAesKey();
            //giai ma client gui len
            if (aesKey != null && aesKey.trim().length() > 0) {
                try {
                    dataDecode = SecurityControler.decodeDataByAes(aesKey, data);
                    JSONObject json = new JSONObject(dataDecode);
                    listValue = new ArrayList<>();
                    if (keys != null && keys.length > 0) {
                        for (String key : keys) {
                            if (json.isNull(key)) {
                                listValue.add("");
                            } else {
                                listValue.add(json.getString(key).trim());
                            }
                        }
                    }
                } catch (JSONException ex) {
                    LOGGER.error("Loi du lieu client gui len: MaHoa(" + data + ") "
                            + ", GiaiMa(" + dataDecode + ")", ex);
                }
            }
        }
        return listValue;
    }

    /**
     * Lay va xu ly thong tin tu client gui len datnv5[FunctionCommon] bo sung
     * moi de gom cac chuc nang quan ly chung
     *
     * @param request
     * @param data
     * @param keys
     * @return
     */
    public static EntityUserGroup getDataFromClient(HttpServletRequest request,
            String data, String[] keys) {
        //kiem tra neu ton tai session dang nhap
//        Date startDate = new Date();
        String dataDecode = "";
        EntityUserGroup userGroup = getStatusSession(request);
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        String className = "com.viettel.voffice.constants";
        if (stackTraceElements != null && stackTraceElements.length > 1) {
//            int lineNumber = stackTraceElements[2].getLineNumber();
            className = stackTraceElements[2].getClassName();
//            String methodName = stackTraceElements[2].getMethodName();
        }
        EntityLog kpiLog = new EntityLog(request, className);
        if (userGroup.getCheckSessionOk()) {
            String aesKey = userGroup.getStrAesKey();
            //giai ma client gui len
            List<String> listValue = null;
            if (aesKey != null && aesKey.trim().length() > 0) {
                try {
                    dataDecode = SecurityControler.decodeDataByAes(aesKey, data);
//                    insertLogsStep(userGroup.getUserId2(), "STEP", startDate, 3000L, dataDecode);
                    //dat logKPI
                    kpiLog.setUserName(userGroup.getCardId());
                    kpiLog.setParamList(dataDecode);
                    userGroup.setKpiLog(kpiLog);
                    LogUtils.logFunctionalStart(kpiLog);

                    JSONObject json = new JSONObject(dataDecode);
                    listValue = new ArrayList<>();
                    if (keys != null && keys.length > 0) {
                        for (String key : keys) {
                            if (json.isNull(key)) {
                                listValue.add("");
                            } else {
                                listValue.add(json.getString(key).trim());
                            }
                        }
                    }
                } catch (JSONException ex) {
                    LOGGER.error("Loi du lieu client gui len: MaHoa(" + data + ") "
                            + ", GiaiMa(" + dataDecode + "):" + ex);
                }
            } else if (keys != null && keys.length > 0) {
                listValue = new ArrayList<>();
                for (String key : keys) {
                    if (key.length() > 0) {
                        listValue.add("");
                    }
                }
            }
            userGroup.setListParamsFromClient(listValue);
        }
        return userGroup;
    }

    /**
     * Tra du lieu ve cho client
     *
     * @param errorCode
     * @param obj
     * @param userGroup
     * @return
     */
    public static String responseResult(ErrorCode errorCode, Object obj,
            EntityUserGroup userGroup) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ErrorCode.class,
                new ErrorCodeEnumAdapterTypeAdapter<ErrorCode>());
        Gson gson = gsonBuilder.create();
        String response;
        String mess = gson.toJson(errorCode);
        String data;
        if (obj != null) {
            response = StringConstants.STR_RESULT_RETURN_FULL;
            data = gson.toJson(obj);
            String aesKey = userGroup.getStrAesKey();
            if (!CommonUtils.isEmpty(aesKey)) {
                data = "\"" + SecurityControler.encodeDataByAes(aesKey, data) + "\"";
            }
            LogUtils.logFunctionalEnd(userGroup.getKpiLog());
            FunctionCommon.insertLogsActionService(userGroup.getUserId2(),
                    userGroup.getName2(), userGroup.getKpiLog().getFunction(),
                    userGroup.getKpiLog().getIpAddress(), "",
                    userGroup.getKpiLog().getStartTime(), "", "", 2000L);
            response = String.format(response, mess, data);
        } else {
            response = StringConstants.STR_RESULT_RETURN_MESS;
            response = String.format(response, mess);
        }
        return response;
    }

    /**
     * Doc FileCert
     *
     * @param path
     * @return
     */
    public static String readCertFile(String path) {
        String filePath = "";
        BufferedReader reader;
        try {
            String contextPath = FunctionCommon.getStorageConfigFile(
                    StringConstants.STR_PROPERTIES_CERTPATH);
            filePath = contextPath + File.separator + path;
            reader = new BufferedReader(new FileReader(filePath));
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            String ls = System.getProperty("line.separator");

            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }
            reader.close();
            return stringBuilder.toString();
        } catch (Exception e) {
            LOGGER.error("Cap chung thu loi doc file cert:" + filePath,
                    e);
        }
        return null;
    }

    /**
     * thuc hien insert log cac tinh nang thuc hien
     *
     * @param userId2
     * @param strLoginName
     * @param strFunction
     * @param ipServer
     * @param strConten
     * @param startTime
     * @param strDeviceOs
     * @param strVofficeVs
     * @param overOutLogs
     */
    public static void insertLogsActionService(Long userId2, String strLoginName,
            String strFunction, String ipServer, String strConten, Date startTime,
            String strDeviceOs, String strVofficeVs, Long overOutLogs) {

        Date endActionDate = new Date();
        Long subTime = endActionDate.getTime() - startTime.getTime();
        if (subTime > overOutLogs) {
            LogActionDao logActionDao = new LogActionDao();
            logActionDao.insertActionLog(userId2, strLoginName, strFunction, ipServer,
                    strConten, startTime, endActionDate, strDeviceOs, strVofficeVs, subTime);
        }
    }

    /**
     * chen log check tung buoc
     *
     * @param userId2
     * @param strConten
     * @param startTime
     * @param timeOver
     * @param strLogsFileSql
     */
    public static void insertLogsStep(Long userId2, String strConten, Date startTime,
            Long timeOver, String strLogsFileSql) {
        try {
            Date endActionDate = new Date();
            Long subTime = endActionDate.getTime() - startTime.getTime();
            if (subTime >= timeOver) {
                StackTraceElement[] stackTraceElements = Thread.currentThread()
                        .getStackTrace();
                String methodName = StringConstants.STR_EMTY;
                String className = StringConstants.STR_EMTY;
                String methodName1 = StringConstants.STR_EMTY;
                String className1 = StringConstants.STR_EMTY;
                int lineNumber = 0;
                int lineNumber1 = 0;
                if (stackTraceElements != null && stackTraceElements.length > 1) {
                    lineNumber = stackTraceElements[2].getLineNumber();
                    className = stackTraceElements[2].getClassName();
                    methodName = stackTraceElements[2].getMethodName();
                }
                if (stackTraceElements != null && stackTraceElements.length > 2) {
                    lineNumber1 = stackTraceElements[3].getLineNumber();
                    className1 = stackTraceElements[3].getClassName();
                    methodName1 = stackTraceElements[3].getMethodName();
                }
                strConten = String.format("%s.%s=line:%s===%s.%s=line:%s",
                        className, methodName, lineNumber,
                        className1, methodName1, lineNumber1);
                LogActionDao logActionDao = new LogActionDao();
                logActionDao.insertActionLogSqlCommand(userId2, "STEP", methodName, "",
                        strConten, startTime, endActionDate, "STEP", "STEP",
                        subTime, strLogsFileSql);
                if (strLogsFileSql != null) {
                    LOGGER.error("==========datnv5:TakeTime(" + subTime.toString()
                            + ")==sql==:" + strLogsFileSql);
                }
            }
        } catch (Exception e) {
            LOGGER.error("==========datnv5: loi ghi log" + e);
        }
    }

    /**
     * thuc hien lay ra danh sach chuoi id client gui len
     *
     * @param strLstUser
     * @return
     */
    public static List<Long> getListIdFromString(String strLstUser) {
        String[] arrId = strLstUser.split(StringConstants.STR_COMMA);
        List<Long> lstResult = new ArrayList<>();
        boolean isHaveId = false;
        for (int i = 0; i < arrId.length; i++) {
            String strNumber = arrId[i].trim();
            if (strNumber.length() > 0 && isNumeric(strNumber)) {
                isHaveId = true;
                lstResult.add(Long.parseLong(strNumber));
            }
        }
        if (!isHaveId) {
            lstResult = null;
        }
        return lstResult;
    }

    /**
     * get list String from string by symboy comma
     *
     * @param strLstUser
     * @return
     */
    public static List<String> getListStringFromString(String strLstUser) {
        String[] arrId = strLstUser.split(StringConstants.STR_COMMA);
        List<String> lstResult = new ArrayList<>();
        boolean isHaveId = false;
        for (String arrId1 : arrId) {
            String strItem = arrId1.trim();
            if (strItem.length() > 0) {
                isHaveId = true;
                lstResult.add(strItem);
            }
        }
        if (!isHaveId) {
            lstResult = null;
        }
        return lstResult;
    }

    /**
     * lay duong dan file cert
     *
     * @param nameCert
     * @param isSim: true la duong dan sim, false la duong dan soft
     * @return tra ve duong dan day du cua file cert
     */
    public static String getCertFullPath(String nameCert, Boolean isSim) {
        StringBuilder strPathCertFull = new StringBuilder();
        String certSimPath = FunctionCommon
                .getPropertiesValue(StringConstants.STR_PROPERTIES_CERTPATH);
        strPathCertFull.append(certSimPath);
        if (isSim) {
            //lay path file full cua sim
            strPathCertFull.append(nameCert);
            if (strPathCertFull.toString().endsWith(".cer") == false) {
                strPathCertFull.append(".cer");
            }

        } else {
            strPathCertFull.append("/");
            strPathCertFull.append(nameCert);
        }
        return strPathCertFull.toString();
    }

    /**
     * lay du lieu file x509
     *
     * @param pathFileCert
     * @return
     */
    public static X509Certificate getX509FromFile(String pathFileCert) {
        File fileAttachment = new File(pathFileCert);
        X509Certificate x509Cert = null;
        if (fileAttachment.exists()) {
            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                x509Cert = (X509Certificate) cf.generateCertificate(
                        new FileInputStream(fileAttachment));
            } catch (CertificateException | FileNotFoundException e) {
                x509Cert = null;
                LOGGER.error("==========datnv5: getDataX509FromFile null" + e);
            }
        }
        return x509Cert;
    }

    /**
     * chuoi ngay hien thi cho client
     *
     * @param notAfter
     * @param isFullDateTime:true: full date time, false: date sort
     * @return
     */
    public static String dateShow(Date notAfter, Boolean isFullDateTime) {
        String date;
        if (notAfter == null) {
            return "";
        }
        if (isFullDateTime) {
            date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(notAfter);
        } else {
            date = new SimpleDateFormat("dd/MM/yyyy").format(notAfter);
        }
        return date;
    }

    /**
     * loai bo gio trong date
     *
     * @param dateCv
     * @return
     */
    public static Date dateSort(Date dateCv) {
        Date date = null;
        String strDate = dateShow(dateCv, false);
        try {

            date = new SimpleDateFormat("dd/MM/yyyy").parse(strDate);
        } catch (ParseException ex) {
            LOGGER.error("==========datnv5: dateSort null", ex);
        }
        return date;
    }

    /**
     * doc noi dung file
     *
     * @param rootFileDec
     * @return
     */
    public static String readContenFile(String rootFileDec) {
        String stringRe = "";
        try {
            PdfReader reader = new PdfReader(rootFileDec);
            //String page = PdfTextExtractor.getTextFromPage(reader, 1);
            for (int pageN = 1; pageN <= reader.getNumberOfPages(); pageN++) {
                SimpleTextExtractionStrategy strategy = new SimpleTextExtractionStrategy();
                String page1 = PdfTextExtractor.getTextFromPage(reader, pageN, strategy);
                byte[] bytes = page1.getBytes("UTF-16");
                String question = new String(bytes, "UTF-16");
                stringRe += question;
            }
        } catch (IOException ex) {
            LOGGER.error("Loi! readContenFile: ", ex);
        }
        return stringRe.replace("\n", "                             ");
    }

    /**
     * loai bo ky tu dac biet khi dua vao elastic
     *
     * @param strInput
     * @return
     */
    public static String replaceTextGiveElastic(String strInput) {
        String strOutput = strInput.replace("\"", "\\\"").replace("“", "\\\"")
                .replace("”", "\\\"").replace("\n", "                             ");
        return strOutput;
    }

    public static String replaceTextElasticSearchKey(String strInput) {
        String strOutput = strInput.replace("“", "").replace("”", "").replace("\"", "")
                .replace("", "").replace("\"\"", "\"")
                .replace("\\", "").replace("[", "").replace("]", "")
                .replace("(", "").replace(")", "")
                .replace("{", "").replace("}", "").toLowerCase();
        return strOutput;
    }

    /**
     * Go bo dau tieng viet
     *
     * @param s
     * @return
     */
    public static String removeAccent(String s) {
        if (s == null) {
            return "";
        }
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replace("đ", "d").replace("Đ", "D");
    }

    /**
     * Convert Object To String Json
     *
     * @param object
     * @return
     */
    public static String convertObjectToStringJson(Object object) {
        String strMess = "";
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(ErrorCode.class, new ErrorCodeEnumAdapterTypeAdapter<>());
            Gson gson = gsonBuilder.create();
            strMess = gson.toJson(object);
        } catch (Exception e) {
            LOGGER.error("Loi! FuctionCommon.convertObjectToStringJson", e);
        }

        return strMess;
    }

    public static String returnResultAfterLogSessionTimeout(EntityUserGroup userGroup) {

        String result = responseResult(userGroup.getEnumErrCode(), null, null);
        return returnResultAfterLog(result, userGroup.getUserId2(), null, null,
                "Session timeout", null);
    }

    public static String returnResultAfterLogNoInfo(Long userId) {

        String result = responseResult(ErrorCode.NOT_ALLOW, null, null);
        return returnResultAfterLog(result, userId, null, null,
                "Khong co thong tin tren he thong 2", null);
    }

    public static String returnResultAfterLogNotAllow(Long userId) {

        String result = responseResult(ErrorCode.NOT_ALLOW, null, null);
        return returnResultAfterLog(result, userId, null, null,
                "Khong co quyen thuc hien", null);
    }

    public static <T> T returnResultAfterLogResultNull(T obj, Long userId) {
        return returnResultAfterLog(obj, userId, null, null, "Result null", null);
    }

    public static <T> T returnResultAfterLogInputInvalid(T obj, Long userId,
            String key) {
        return returnResultAfterLog(obj, userId, key, null,
                "Dau vao khong hop le", null);
    }

    public static <T> T returnResultAfterLog(T obj, Long userId, String errorDesc) {
        return returnResultAfterLog(obj, userId, null, null, errorDesc, null);
    }

    public static <T> T returnResultAfterLog(T obj, Long userId, String key,
            Long value, String errorDesc) {
        return returnResultAfterLog(obj, userId, key, String.valueOf(value), errorDesc, null);
    }

    public static <T> T returnResultAfterLog(T obj, Long userId, String data,
            Throwable t) {
        return returnResultAfterLog(obj, userId, "data", data, "Exception", t);
    }

    public static String returnResultAfterLog(EntityUserGroup userGroup,
            Throwable t) {

        String result = responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        return returnResultAfterLog(result, userGroup.getUserId2(), userGroup.getInputData(), t);
    }

    /**
     * <b>Thuc hien ghi log truoc khi tra ket qua</b>
     *
     * @param <T>
     * @param obj doi tuong tra ve
     * @param userId id user
     * @param key tu khoa
     * @param value gia tri cho tu khoa
     * @param errorDesc mo ta loi
     * @param t exception
     * @return
     */
    private static <T> T returnResultAfterLog(T obj, Long userId, String key,
            String value, String errorDesc, Throwable t) {

        if (!CommonUtils.isEmpty(errorDesc)) {
            // Truy nguon goc goi ham
            String prefix = "";
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            if (!CommonUtils.isEmpty(stackTraceElements) && stackTraceElements.length > 3) {
                StackTraceElement trace = stackTraceElements[3];
                prefix = String.format("[%s:%s:%d] ", trace.getFileName(),
                        trace.getMethodName(), trace.getLineNumber());
            }
            String log;
            if (CommonUtils.isEmpty(key)) {
                log = prefix + String.format("userId: %d - %s!", userId, errorDesc);
            } else {
                log = prefix + String.format("userId: %d - %s: %s - %s!", userId,
                        key, value, errorDesc);
            }
            if (t != null) {
                LOGGER.error(log, t);
            } else {
                LOGGER.error(log);
            }
        }
        return obj;
    }

    /**
     * lay dia chi tomcat dang chay
     *
     * @return
     */
    private static String getIpAddressAndPort() {
        String strIpAdress = "";
        try {

            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface netint : Collections.list(nets)) {
                strIpAdress += displayInterfaceInformation(netint);
            }
            MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
            Set<ObjectName> objectNames = beanServer.queryNames(new ObjectName("*:type=Connector,*"),
                    Query.match(Query.attr("protocol"), Query.value("HTTP/1.1")));
            if (objectNames != null && objectNames.size() > 0) {
                String port = objectNames.iterator().next().getKeyProperty("port");
                strIpAdress += "/:Port= " + port;
            }
//            System.out.println("strIpAdress of tomcat server : " + strIpAdress);
        } catch (MalformedObjectNameException | SocketException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return FunctionCommon.getNumberAndDotFromString(strIpAdress);
    }

    static String displayInterfaceInformation(NetworkInterface netint) {
        String strIp = "";
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            strIp += ", " + inetAddress;
        }
        return strIp;
    }

    /**
     * thuc hien ghi log vao he thong
     *
     * @param stringData
     */
    public static void writeLogsNewfile(String stringData) {
        ThreadWriteLogsNewFile threadwrite = new ThreadWriteLogsNewFile(stringData);
        threadwrite.start();
    }

    /**
     * lay so va dau cham tu chuoi dua vao
     *
     * @param strInput
     * @return
     */
    public static String getNumberAndDotFromString(String strInput) {
        String text = strInput;//"-jaskdh2367sd.27askjdfh23";
        String digits = text.replaceAll("[^0-9./:]", "");
//        System.out.println(digits);
        return digits;
    }

    /**
     * convert String to Blob
     *
     * @param strValue
     * @return
     */
    public static Blob stringToBlob(String strValue) {
        try {
            byte byte_string[] = strValue.getBytes();
            Blob blob = new SerialBlob(byte_string);
            return blob;
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return null;
    }

    /**
     * convert Blob to string
     *
     * @param blob
     * @return
     */
    public static String blobToString(Blob blob) {
        try {
            if (blob == null) {
                return null;
            }
            return new String(blob.getBytes((long) 1, (int) blob.length()));
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Lay thong tin cau hinh trong database
     *
     * @param strKey
     * @return
     */
    public static String getValueFromConfigDataBase(String strKey) {
        ConfigParameterDAO configParameterDAO = new ConfigParameterDAO();
        String strValue = configParameterDAO.getValueFromConfigDataBase(strKey);
        return strValue;
    }

    /**
     * Tao chuoi checksum de thanh toan viettel pay
     *
     * @param data
     * @param key
     * @return
     */
    public static String createCheckSumVtPay(String data, String key) {
        //key do viettel cung cap
        String result = null;
        try {
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(data.getBytes());
            result = new String(Base64.encodeBase64(rawHmac));
        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalStateException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * tao link vtPay
     *
     * @param billcode
     * @param textComment
     * @param login_msisdn
     * @param itemConfigPay: cau hinh file config
     * @return
     */
    public static VtPayEntity createUrlVtPay(String billcode, String textComment, String login_msisdn,VtPayEntity itemConfigPay) {
        VtPayEntity itemResult = null;
        if (itemConfigPay != null) {
            itemResult = itemConfigPay;
            String strMakeChecksum = itemConfigPay.getAccess_code() + billcode + itemConfigPay.getCommand()
                    + itemConfigPay.getMerchant_code() + billcode
                    + itemConfigPay.getTrans_amount() + itemConfigPay.getVersion();
            String check_sum = createCheckSumVtPay(strMakeChecksum, itemConfigPay.getCheck_sum_key());
            itemResult.setStrCheckSum(check_sum);
            itemResult.setBillCode(billcode);
            //Tao Url vtPay
            StringBuilder urlWebPayBuilder = new StringBuilder();
            urlWebPayBuilder.append(itemConfigPay.getUrlweb());
            urlWebPayBuilder.append("?billcode=");
            urlWebPayBuilder.append(billcode);

            urlWebPayBuilder.append("&command=");
            urlWebPayBuilder.append(itemConfigPay.getCommand());

            urlWebPayBuilder.append("&desc=");
            urlWebPayBuilder.append(textComment);

            urlWebPayBuilder.append("&locale=");
            urlWebPayBuilder.append(itemConfigPay.getLocale());

            urlWebPayBuilder.append("&merchant_code=");
            urlWebPayBuilder.append(itemConfigPay.getMerchant_code());

            urlWebPayBuilder.append("&order_id=");
            urlWebPayBuilder.append(billcode);

            urlWebPayBuilder.append("&cancel_url=");
            urlWebPayBuilder.append(itemConfigPay.getCancel_url());

            urlWebPayBuilder.append("&return_url=");
            urlWebPayBuilder.append(itemConfigPay.getReturn_url());

            urlWebPayBuilder.append("&trans_amount=");
            urlWebPayBuilder.append(itemConfigPay.getTrans_amount());

            urlWebPayBuilder.append("&version=");
            urlWebPayBuilder.append(itemConfigPay.getVersion());

            urlWebPayBuilder.append("&login_msisdn=");
            urlWebPayBuilder.append(login_msisdn);

            urlWebPayBuilder.append("&check_sum=");
            urlWebPayBuilder.append(check_sum);
            itemResult.setStrUrlFullWeb(urlWebPayBuilder.toString());
            System.out.println("setStrUrlFullWeb===" + urlWebPayBuilder.toString());
        }
        return itemResult;
    }

    /**
     * lay dia chi client remote vao goi du lieu
     *
     * @param request
     * @return
     */
    public static String getClientIpAddressRemote(HttpServletRequest request) {
        String[] HEADERS_TO_TRY = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"};
        String strIpArr = "/";
        for (String header : HEADERS_TO_TRY) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                strIpArr += ip + "/";
            }
        }
        strIpArr += request.getRemoteAddr() + "/";
        return strIpArr;
    }

    /**
     * tao chuoi ngau nhien voi n ky tu
     *
     * @param n
     * @return
     */
    public static String getAlphaNumericString(int n) {
        // chose a Character random from this String 
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz";
        // create StringBuffer size of AlphaNumericString 
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            // generate a random number between 
            // 0 to AlphaNumericString variable length 
            int index = (int) (AlphaNumericString.length() * Math.random());
            // add Character one by one in end of sb 
            sb.append(AlphaNumericString.charAt(index));
        }
        return sb.toString();
    }

    /**
     * thuc hien convert chuoi base64 to file pdf
     *
     * @param strBase64
     * @param strPathFileSave
     * @return
     */
    public static Boolean convertStringBase64ToPdf(String strBase64, String strPathFileSave) {
        File file = new File(strPathFileSave);//D:\\test.pdf"
        try (FileOutputStream fos = new FileOutputStream(file)) {
            String b64 = strBase64;
            byte[] decoder = Base64.decodeBase64(b64);

            fos.write(decoder);
//            System.out.println("PDF File Saved");
            return true;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * convert file to base64
     *
     * @param fileName
     * @return
     */
    public static String convertPdfToBase64String(String fileName) {
        try {
            byte[] input_file = Files.readAllBytes(Paths.get(fileName));
            byte[] encodedBytes = Base64.encodeBase64(input_file);
            String encodedString = new String(encodedBytes);
            return encodedString;
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return "";
    }

    /**
     * gen gia han tu dong chung thu so
     * @return 
     */
    public static int genActivationCode() {
        Random generator = new Random(new Date().getTime());
        return (10000000 + generator.nextInt(90000000 - 1));
    }
    
    public static void addInQuery(StringBuilder query, List<Object> params, String name ,List<Long> ids) {
        if (ids.size() < 1000) {
            query.append(" and ").append(name).append(" in( ");
            query.append(generateQuestionMark(ids.size()));
            query.append(") ");
            for (Long id : ids) {
                if (id != null) {
                    params.add(id);
                } else {
                    params.add(-1L);
                }
            }
        } else {
            int PARAMETER_LIMIT = 999;
            query.append(" and (").append(name).append(" in( ");
            query.append(generateQuestionMark(PARAMETER_LIMIT));
            query.append(") ");
            for (int j = 0; j < PARAMETER_LIMIT; j++) {
                if (ids.get(j) != null) {
                    ids.get(j);
                } else {
                    params.add(-1L);
                }
            }
            int finalIndex = 0;
            for (int i = PARAMETER_LIMIT; i < ids.size(); i += PARAMETER_LIMIT) {
                if (ids.size() > i + PARAMETER_LIMIT) {
                    query.append(" or").append(name).append(" in( ");
                    query.append(generateQuestionMark(PARAMETER_LIMIT));
                    query.append(") ");
                    for (int j = i; j < i + PARAMETER_LIMIT; j++) {
                        if (ids.get(j) != null) {
                            ids.get(j);
                        } else {
                            params.add(-1L);
                        }
                    }
                } else {
                    finalIndex = i;
                    break;
                }
            }
            query.append(" or").append(name).append(" in( ");
            query.append(generateQuestionMark(ids.size() - finalIndex));
            query.append(") ");
            for (int j = finalIndex; j < ids.size(); j++) {
                if (ids.get(j) != null) {
                    ids.get(j);
                } else {
                    params.add(-1L);
                }
            }
            query.append(") ");
        }
    }
    
    public static Long mapPriorityVPCP(Long id) {
        if (id != null) {
            switch (id.intValue()) {
            case 1:
                return Constants.CONNECT_DOCUMENT.PRIORITY.NORMAL;
            case 2:
                return Constants.CONNECT_DOCUMENT.PRIORITY.URGENT;
            case 3:
                return Constants.CONNECT_DOCUMENT.PRIORITY.EXPRESS;
            case 4:
                return Constants.CONNECT_DOCUMENT.PRIORITY.VERY_URGENT;
            default:
                return Constants.CONNECT_DOCUMENT.PRIORITY.NORMAL;
            }
        } else {
            return Constants.CONNECT_DOCUMENT.PRIORITY.NORMAL;
        }
    }
}
