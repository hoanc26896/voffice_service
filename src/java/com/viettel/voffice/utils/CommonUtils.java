/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.SystemParameterDAO;
import com.viettel.voffice.database.entity.EntitySystemParameter;

/**
 *
 * @author thanght6
 */
public class CommonUtils {

    private static final Logger LOGGER = Logger.getLogger(CommonUtils.class);
    private static final Map<String, String[]> MAP_SIGNED_CHAR = new HashMap<String, String[]>();
    private static Set<String> KEY_SET_MAP_SIGNED_CHAR;

    static {
        MAP_SIGNED_CHAR.put("a", new String[] { "à", "á", "ạ", "ả", "ã", "â", "ầ", "ấ", "ậ", "ẩ", "ẫ", "ă", "ằ", "ắ", "ặ", "ẳ", "ẵ" });
        MAP_SIGNED_CHAR.put("e", new String[] { "è", "é", "ẹ", "ẻ", "ẽ", "ê", "ề", "ế", "ệ", "ể", "ễ" });
        MAP_SIGNED_CHAR.put("i", new String[] { "ì", "í", "ị", "ỉ", "ĩ" });
        MAP_SIGNED_CHAR.put("o", new String[] { "ò", "ó", "ọ", "ỏ", "õ", "ô", "ồ", "ố", "ộ", "ổ", "ỗ", "ơ", "ờ", "ớ", "ợ", "ở", "ỡ" });
        MAP_SIGNED_CHAR.put("u", new String[] { "ù", "ú", "ụ", "ủ", "ũ", "ư", "ừ", "ứ", "ự", "ử", "ữ" });
        MAP_SIGNED_CHAR.put("y", new String[] { "ỳ", "ý", "ỵ", "ỷ", "ỹ" });
        MAP_SIGNED_CHAR.put("d", new String[] { "đ" });
        MAP_SIGNED_CHAR.put("A", new String[] { "À", "Á", "Ạ", "Ả", "Ã", "Â", "Ầ", "Ấ", "Ậ", "Ẩ", "Ẫ", "Ă", "Ằ", "Ắ", "Ặ", "Ẳ", "Ẵ" });
        MAP_SIGNED_CHAR.put("E", new String[] { "È", "É", "Ẹ", "Ẻ", "Ẽ", "Ê", "Ề", "Ế", "Ệ", "Ể", "Ễ" });
        MAP_SIGNED_CHAR.put("I", new String[] { "Ì", "Í", "Ị", "Ỉ", "Ĩ" });
        MAP_SIGNED_CHAR.put("O", new String[] { "Ò", "Ó", "Ọ", "Ỏ", "Õ", "Ô", "Ồ", "Ố", "Ộ", "Ổ", "Ỗ", "Ơ", "Ờ", "Ớ", "Ợ", "Ở", "Ỡ" });
        MAP_SIGNED_CHAR.put("U", new String[] { "Ù", "Ú", "Ụ", "Ủ", "Ũ", "Ư", "Ừ", "Ứ", "Ự", "Ử", "Ữ" });
        MAP_SIGNED_CHAR.put("Y", new String[] { "Ỳ", "Ý", "Ỵ", "Ỷ", "Ỹ" });
        MAP_SIGNED_CHAR.put("D", new String[] { "Đ" });
        KEY_SET_MAP_SIGNED_CHAR = MAP_SIGNED_CHAR.keySet();
    }
    
    /**
     * <b>Check empty for String</b>
     *
     * @param s
     * @return
     */
    public static boolean isEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }

    /**
     * <b>Checks if an array of Objects is empty or null</b>
     *
     * @param array the array to test
     * @return true if the array is empty or null
     */
    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * <b>Check empty for List</b>
     *
     * @param l
     * @return
     */
    public static boolean isEmpty(List<?> l) {
        return l == null || l.isEmpty();
    }

    /**
     * <b>Check empty for HashMap</b>
     *
     * @param h
     * @return
     */
    public static boolean isEmpty(HashMap<?, ?> h) {
        return h == null || h.isEmpty();
    }

    /**
     * <b>Lay gia tri cau hinh theo key</b>
     *
     * @param key Tu khoa trong file cau hinh
     * @return Tra ve gia tri kieu String
     */
    public static String getAppConfigValue(String key) {
        return FunctionCommon.getPropertiesValue(key);
    }

    /**
     * <b>Lay gia tri cau hinh theo key</b>
     *
     * @param key Tu khoa trong file cau hinh
     * @return Tra ve danh sach gia tri kieu String
     */
    public static List<String> getListAppConfigValue(String key) {
        String strValue = FunctionCommon.getPropertiesValue(key);
        List<String> listValue = new ArrayList<String>();
        if (!isEmpty(strValue)) {
            String[] arrValue = strValue.split(Constants.Common.COMMA_CHAR);
            if (!isEmpty(arrValue)) {
                listValue = Arrays.asList(arrValue);
            }
        }
        return listValue;
    }

    /**
     * Lay ky danh gia hien tai
     *
     * @return
     */
    public static String getCurrentPeriod() {
        String result;
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        result = sdf.format(date);
        return result;
    }

    public String[] strIdToList(String strId) {
        String[] lstId = new String[]{"0"};
        if (strId != null && !"".equals(strId.trim())) {
            String[] lstStringId = strId.trim().split(",");
            if (lstStringId.length > 0) {
                lstId = lstStringId;
            }
        }
        return lstId;
    }

    /**
     * <b>Sinh ngau nhien ma OTP co 8 chu so</b><br>
     *
     * @author thanght6
     * @since Aug 16, 2016
     * @return
     */
    synchronized public static int generateOTPCode() {
        Random generator = new Random(new Date().getTime());
        return (10000000 + generator.nextInt(90000000 - 1));
    }

    /**
     * <b>Sinh ngau nhien ti le cac thanh phan sao cho tong bang la mot so cho
     * truoc</b><br>
     *
     * @author thanght6
     * @since Aug 18, 2016
     * @param sum Tong
     * @param count So luog cac thanh phan
     * @return
     */
    public static int[] randomRatio(int sum, int count) {

        // Kiem tra dau vao
        if (sum == 0 || count == 0 || sum < count) {
            return null;
        }
        try {
            Random random = new Random();
            int value;
            int[] arrLength = new int[count];
            for (int i = 1; i < count; i++) {
                value = random.nextInt(sum - count + i - 1) + 1;
                sum -= value;
                arrLength[i - 1] = value;
            }
            arrLength[count - 1] = sum;
            return arrLength;
        } catch (Exception e) {
            LOGGER.error("ERR:randomRatio", e);
            return null;
        }
    }

    /**
     * <b>Sinh vi tri ngau nhieu</b><br>
     *
     * @author thanght6
     * @since Aug 18, 2016
     * @param length
     * @return
     */
    public static int[] randomPosition(int length) {

        if (length <= 0) {
            return null;
        }
        // Khoi tao danh sach vi tri tang tu 1 -> length
        List<Integer> listPosition = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            listPosition.add(i);
        }
        // Tao mang cac vi tri duoc sap xep ngau nhien
        int size;
        int index;
        int value;
        Random random = new Random();
        int[] arrPosition = new int[length];
        for (int i = 0; i < length; i++) {
            size = listPosition.size();
            index = random.nextInt(size);
            value = listPosition.get(index);
            arrPosition[i] = value;
            listPosition.remove(index);
        }
        return arrPosition;
    }

    /**
     * <b>Sinh mat khau ngau nhien</b><br>
     *
     * @author thanght6
     * @since Aug 18, 2016
     * @return
     */
    public static String randomPassword() {

        try {
            // Sinh ngau nhien do dai cua mat khau (8-12)
            Random random = new Random();
            int length = random.nextInt(5) + 8;
            // Sinh ngau nhien ty le cac ky tu
            int[] arrLength = randomRatio(length, 4);
            // Sinh vi tri ngau nhien
            int[] arrPosition = randomPosition(length);
            int count = 0;
            char[] arrPasswordChar = new char[length];
            // Lay ngau nhien ky tu chu thuong
            int charsLength = arrLength[0];
            int templateCharsLength = Constants.Common.LOWERCASE_CHARS.length();
            for (int i = 1; i <= charsLength; i++) {
                arrPasswordChar[arrPosition[count]] = Constants.Common.LOWERCASE_CHARS
                        .charAt(random.nextInt(templateCharsLength));
                count++;
            }
            // Lay ngau nhien ky tu chu hoa
            charsLength = arrLength[1];
            templateCharsLength = Constants.Common.UPPERCASE_CHARS.length();
            for (int i = 1; i <= charsLength; i++) {
                arrPasswordChar[arrPosition[count]] = Constants.Common.UPPERCASE_CHARS
                        .charAt(random.nextInt(templateCharsLength));
                count++;
            }
            // Lay ngau nhien chu so
            charsLength = arrLength[2];
            templateCharsLength = Constants.Common.DIGITS.length();
            for (int i = 1; i <= charsLength; i++) {
                arrPasswordChar[arrPosition[count]] = Constants.Common.DIGITS
                        .charAt(random.nextInt(templateCharsLength));
                count++;
            }
            // Lay ngau nhien chu so
            charsLength = arrLength[3];
            templateCharsLength = Constants.Common.SPECIAL_CHARS.length();
            for (int i = 1; i <= charsLength; i++) {
                arrPasswordChar[arrPosition[count]] = Constants.Common.SPECIAL_CHARS
                        .charAt(random.nextInt(templateCharsLength));
                count++;
            }
            return new String(arrPasswordChar);
        } catch (Exception e) {
            LOGGER.error("ERR:randomPassword", e);
            return null;
        }
    }

    // Lay ma trang thai server
    public static int getServerStatusCode() {
        int server_status_code = 0;
        if (server_status_code == 0) {
            try {
                String strCode = getAppConfigValue("server.status.code");
                if (!CommonUtils.isEmpty(strCode)) {
                    server_status_code = Integer.parseInt(strCode);
                }
            } catch (Exception e) {
                LOGGER.error("ERR:getServerStatusCode", e);
                server_status_code = 0;
            }
        }
        return server_status_code;
    }

    // Mo ta trang thai server
    // Lay mo ta trang thai server
    public static String getServerStatusDescription() {
        String server_status_descripttion;
        try {
            server_status_descripttion = getAppConfigValue("server.status.description");
        } catch (Exception e) {
            LOGGER.error("ERR:getServerStatusDescription", e);
            server_status_descripttion = "";
        }
        return server_status_descripttion;
    }
    // Chi so ngau nhien
    private static int randomIndex = (new Random()).nextInt(100);
    // Ma danh sach LB thuong
    private static final String NORMAL_LB_LINKS_CODE = "NORMAL_LB_LINKS";
    // Key link LB thuong
    private static final String NORMAL_LB_LINK_KEY = "normalLBLink";
    // Ma danh sach LB VIP
    private static final String VIP_LB_LINKS_CODE = "VIP_LB_LINKS";
    // Key link LB VIP
    private static final String VIP_LB_LINK_KEY = "vipLBLink";

    /**
     * <b>Lay link LB thuong va link LB VIP ngau nhien</b><br>
     *
     * @return
     */
    public static HashMap<String, String> getLBLinkHashMap() {

        HashMap<String, String> lbLinkHM = new HashMap<>();
        try {
            // Tang chi so ngau nhien
            int random = randomIndex++;
            SystemParameterDAO systemParameterDAO;
            EntitySystemParameter systemParameter;
            String value;
            // Link LB thuong ngau nhien
            String normalLBLink = null;
            String[] normal_link_lb = null;
            String[] vip_lb_link = null;
            // Lay danh sach link LB thuong
            systemParameterDAO = new SystemParameterDAO();
            systemParameter = systemParameterDAO.getConfigValueByCode(NORMAL_LB_LINKS_CODE);
            if (systemParameter != null
                    && !CommonUtils.isEmpty(systemParameter.getValue())) {
                value = systemParameter.getValue();
                normal_link_lb = value.split(Constants.Common.COMMA_CHAR);
            }
            if (normal_link_lb != null && !CommonUtils.isEmpty(normal_link_lb)) {
                normalLBLink = normal_link_lb[random % normal_link_lb.length].trim();
            }
            lbLinkHM.put(NORMAL_LB_LINK_KEY, normalLBLink);

            // Link LB VIP ngau nhien
            String vipLBLink = null;

            systemParameter = systemParameterDAO.getConfigValueByCode(VIP_LB_LINKS_CODE);
            if (systemParameter != null
                    && !CommonUtils.isEmpty(systemParameter.getValue())) {
                value = systemParameter.getValue();
                vip_lb_link = value.split(Constants.Common.COMMA_CHAR);
            }
            if (vip_lb_link != null && !CommonUtils.isEmpty(vip_lb_link)) {
                vipLBLink = vip_lb_link[random % vip_lb_link.length].trim();
            }
            lbLinkHM.put(VIP_LB_LINK_KEY, vipLBLink);
        } catch (Exception e) {
            LOGGER.error("ERR:getLBLinkHashMap", e);
        }
        return lbLinkHM;
    }
     public static String NVL(String value, String defaultValue) {
        return NVL(value, defaultValue, value);
    }
    public static String NVL(String value) {
        return NVL(value, "");
    }
    public static String NVL(String value, String nullValue, String notNullValue) {
        return value == null ? nullValue : notNullValue;
    }
    // 201812-Pitagon: add
    public static boolean isJSON(String str) {
    	try {
    		JSONObject obj = new JSONObject(str);
    		return true;
    	} catch (Exception e) {
    		try {
    			JSONArray array = new JSONArray(str);
    			return true;
    		} catch (Exception ex) {
    			return false;
    		}
    	}
    }
    
    public static boolean isInteger(String str) {
        if (isEmpty(str) || !str.matches("[0-9]+$")) {
            return false;
        }
        return true;
    }
    
    public static String getEmailName(String email) {
        if (email == null) {
            return null;
        }
        email = CommonUtils.NVL(email);
        return email.split("@")[0];
    }
    
    public static String getSMSMobile(String strMobile) {
        String mobile = "";
        if (strMobile != null) {
            String result = strMobile.replaceAll("[^0-9/]", "");
            String[] resultArray = result.split("/");
            if ((resultArray != null) && (resultArray.length > 0)) {
                for (String element : resultArray) {
                    String unit = element;
                    if (unit.length() >= 3) {
                        String header1 = unit.substring(0, 1);
                        String header2 = unit.substring(0, 2);
                        String header3 = unit.substring(0, 3);
                        if ("856".equals(header3) || "855".equals(header3) || "258".equals(header3) || "509".equals(header3) || "670".equals(header3)
                                || "257".equals(header3) || "00".equals(header2) || "84".equals(header2) || "51".equals(header2) || "255".equals(header3)
                                || "237".equals(header3) || "95".equals(header2)) {
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
                    } else {
                        mobile = "84" + unit;
                        break;
                    }
                }
            }
        }
        return mobile;
    }
    
    public static String toUnsignedChar(String s) {
        if (s == null) {
            return null;
        }
        if (s.length() == 0) {
            return "";
        }
        String[] values;
        for (String key : KEY_SET_MAP_SIGNED_CHAR) {
            values = MAP_SIGNED_CHAR.get(key);
            for (String value : values) {
                s = s.replace(value, key);
            }
        }
        for (int i = 0; i < s.length(); i++) {
            if (s.codePointAt(i) > 255) {
                s = s.replace(s.charAt(i), ' ');
            }
        }
        return s;
    }
    
    public static String getNameEmail(String name, String email) {
        String result = name;
        if ((!isEmpty(name)) && (!isEmpty(email)) && (email.lastIndexOf('@') > 0)) {
            result += " (" + email.substring(0, email.lastIndexOf('@')) + ")";
        }
        return result;
    }

    public static String removeSpecial(String field) {
        String res = "";
        try {
            field = field.replaceAll("</br>", ";");
            if ("".equals(res.trim())) {
                res = field;
            }
        } catch (Exception e) {
            Logger.getLogger(CommonUtils.class).error(e);
        }
        return res;
    }
    
    public static String checkSameStr(Set<String> setStr, String str) {
        if (!isEmpty(str) && setStr != null && setStr.size() > 0) {
            for (String obj : setStr) {
                if (!isEmpty(obj) && obj.toLowerCase().trim().equals(str.toLowerCase().trim())) {
                    return obj;
                }
            }
        }
        return null;
    }
    /**
     * @author DAT_DC
     * Lay quy theo thang hien tai
     * @param month
     * @return
     */
    public static Integer getQuarter(Integer month) {
        int result = 1;
        if (month != null) {
            month += 1;
            if (1 <= month && month <= 3) {
                result = 1;
            } else if (4 <= month && month <= 6) {
                result = 2;
            } else if (7 <= month && month <= 9) {
                result = 3;
            } else {
                result = 4;
            }
        }
        return result;
    }
    /**
     * @author DAT_DC
     * Lay max thang theo quy
     * Thang start = 0
     * @param month
     * @return
     */
    public static Integer getMaxMonth(Integer quarter) {
        int result = 11;
        if (quarter != null) {
            if (quarter == 1) {
                result = 2;
            } else if (quarter == 2) {
                result = 5;
            } else if (quarter == 3) {
                result = 8;
            } else {
                result = 11;
            }
        }
        return result;
    }
    
    public static boolean checkConnectDocument(String filePath) {
        if (!CommonUtils.isEmpty(filePath)) {
            String[] arrayPath = filePath.split("/");
            if (!CommonUtils.isEmpty(arrayPath)) {
                for (String str : arrayPath) {
                    if ("textrec".equalsIgnoreCase(str)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
