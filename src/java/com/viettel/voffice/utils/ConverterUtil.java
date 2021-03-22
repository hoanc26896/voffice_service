package com.viettel.voffice.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;




/**
 * 
 * @author 201812-Pitagon
 *
 */

public class ConverterUtil {
    public static final int DOUBLE_ACCURACY_1 = 1;
    public static final int DOUBLE_ACCURACY_2 = 2;
    public static final int DOUBLE_ACCURACY_3 = 3;

    private static final Map<String, String[]> MAP_SIGNED_CHAR = new HashMap<String, String[]>();
    private static Set<String> KEY_SET_MAP_SIGNED_CHAR;

    private SimpleDateFormat yearMonthSdf = new SimpleDateFormat("yyyyMM");

    static {
        MAP_SIGNED_CHAR.put("a",
                new String[] { "à", "á", "ạ", "ả", "ã", "â", "ầ", "ấ", "ậ", "ẩ", "ẫ", "ă", "ằ", "ắ", "ặ", "ẳ", "ẵ" });
        MAP_SIGNED_CHAR.put("e", new String[] { "è", "é", "ẹ", "ẻ", "ẽ", "ê", "ề", "ế", "ệ", "ể", "ễ" });
        MAP_SIGNED_CHAR.put("i", new String[] { "ì", "í", "ị", "ỉ", "ĩ" });
        MAP_SIGNED_CHAR.put("o",
                new String[] { "ò", "ó", "ọ", "ỏ", "õ", "ô", "ồ", "ố", "ộ", "ổ", "ỗ", "ơ", "ờ", "ớ", "ợ", "ở", "ỡ" });
        MAP_SIGNED_CHAR.put("u", new String[] { "ù", "ú", "ụ", "ủ", "ũ", "ư", "ừ", "ứ", "ự", "ử", "ữ" });
        MAP_SIGNED_CHAR.put("y", new String[] { "ỳ", "ý", "ỵ", "ỷ", "ỹ" });
        MAP_SIGNED_CHAR.put("d", new String[] { "đ" });
        MAP_SIGNED_CHAR.put("A",
                new String[] { "À", "Á", "Ạ", "Ả", "Ã", "Â", "Ầ", "Ấ", "Ậ", "Ẩ", "Ẫ", "Ă", "Ằ", "Ắ", "Ặ", "Ẳ", "Ẵ" });
        MAP_SIGNED_CHAR.put("E", new String[] { "È", "É", "Ẹ", "Ẻ", "Ẽ", "Ê", "Ề", "Ế", "Ệ", "Ể", "Ễ" });
        MAP_SIGNED_CHAR.put("I", new String[] { "Ì", "Í", "Ị", "Ỉ", "Ĩ" });
        MAP_SIGNED_CHAR.put("O",
                new String[] { "Ò", "Ó", "Ọ", "Ỏ", "Õ", "Ô", "Ồ", "Ố", "Ộ", "Ổ", "Ỗ", "Ơ", "Ờ", "Ớ", "Ợ", "Ở", "Ỡ" });
        MAP_SIGNED_CHAR.put("U", new String[] { "Ù", "Ú", "Ụ", "Ủ", "Ũ", "Ư", "Ừ", "Ứ", "Ự", "Ử", "Ữ" });
        MAP_SIGNED_CHAR.put("Y", new String[] { "Ỳ", "Ý", "Ỵ", "Ỷ", "Ỹ" });
        MAP_SIGNED_CHAR.put("D", new String[] { "Đ" });
        KEY_SET_MAP_SIGNED_CHAR = MAP_SIGNED_CHAR.keySet();
    }

    public static String getMapSignedCharKey(char c) {
        String[] values;
        for (String key : KEY_SET_MAP_SIGNED_CHAR) {
            values = MAP_SIGNED_CHAR.get(key);
            for (String value : values) {
                if (value.equals(c)) {
                    return key;
                }
            }
        }
        return null;
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

    public static String toUnsignedCharSMSMission(String s) {
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
            if (s.codePointAt(i) >= 160) {
                s = s.replace(s.charAt(i), ' ');
            }
        }
        return s;
    }

    /**
     * Convert format abcDefGhi to abc_def_ghi
     * 
     * @param entityName
     * @return
     */
    public static String entityNameToTableName(String entityName) {
        if ((entityName == null) || (entityName.length() <= 1)) {
            return entityName;
        }
        StringBuffer tmp = new StringBuffer(entityName);
        StringBuffer rs = new StringBuffer();
        rs.append(tmp.charAt(0));
        for (int i = 1; i < tmp.length(); i++) {
            char c = tmp.charAt(i);
            if (Character.isUpperCase(c)) {
                rs.append("_");
            }
            rs.append(c);
        }
        return rs.toString().toLowerCase();
    }

    /**
     * Convert Object to ByteArray
     * 
     * @param o
     * @return
     */
    public static byte[] toByteArray(Object o) {
        if (o == null) {
            return null;
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(o);
            byte[] buf = baos.toByteArray();
            return buf;
        } catch (Exception e) {
            Logger.getLogger(ConverterUtil.class).error(e);
            return null;
        }
    }

    /**
     * Convert ByteArray to Object
     * 
     * @param b
     * @return
     */
    public static Object toObject(byte[] b) {
        if (b == null) {
            return null;
        }
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(b));
            Object object = ois.readObject();
            ois.close();
            return object;
        } catch (Exception e) {
            Logger.getLogger(ConverterUtil.class).error(e);
            return null;
        }
    }

    /**
     * Convert first letter to lower case
     * 
     * @param s
     * @return
     */
    public static String toLowerCaseFirstLetter(String s) {
        if (s == null) {
            return null;
        }
        String firstLetter = s.substring(0, 1).toLowerCase();
        return firstLetter + s.substring(1, s.length());
    }

    /**
     * Convert first letter to upper case
     * 
     * @param s
     * @return
     */
    public static String toUppperCaseFirstLetter(String s) {
        if (s == null) {
            return null;
        }
        String firstLetter = s.substring(0, 1).toUpperCase();
        return firstLetter + s.substring(1, s.length());
    }
    /**
     * Convert date time to string by year month
     * 
     * @param date
     * @return
     */
    public static String toStringYearMonth(Date date) {
        ConverterUtil constructor = new ConverterUtil();
        if (date == null) {
            return "";
        }
        return constructor.yearMonthSdf.format(date);
    }
    
    
    
}
