/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.utils;

import java.io.IOException;
import java.io.InputStream;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author datnv5
 */
public class Utils {

    protected static final char[][] signChars = {
        {
            97, 224, 225, 7843, 227, 7841, 259, 7857, 7855, 7859, 7861, 7863, 226, 7855, 7847, 7845, 7849, 7851, 7853
        },
        {
            111, 242, 243, 7887, 245, 7885, 244, 7891, 7889, 7893, 7895, 7897, 417, 7901, 7899, 7903, 7905, 7907
        },
        {
            101, 232, 233, 7867, 7869, 7865, 234, 7873, 7871, 7875, 7875, 7879
        },
        {
            117, 249, 250, 7911, 361, 7909, 432, 7915, 7913, 7917, 7919, 7921
        },
        {
            105, 236, 237, 7881, 297, 7883
        },
        {
            121, 7923, 253, 7927, 7929, 7925
        },
        {
            100, 273
        },
        {68, 272}
    };

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
    
    /**
     * Read a properties file from the classpath and return a Properties object
     *
     * @param filename
     * @return
     * @throws IOException
     */
    static public Properties readProperties(String filename) throws IOException {
        Properties props = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream(filename);

        props.load(stream);
        return props;
    }

    public static String[] parseInputList(String input) {
        String[] parts = input.trim().split(",");
        return parts;
    }

    public static Long[] parseInputList2ArrayListLong(String input) {
        String[] parts = input.trim().split(",");
        Long[] arrLong = new Long[parts.length];
        for (int i = 0; i < parts.length; i++) {
            arrLong[i] = Long.parseLong(parts[i]);
        }
        return arrLong;
    }

    public static String toUnSign(String orgStr) {
        if (orgStr == null || orgStr.length() == 0) {
            //thaida sua lai truong hop nay
            //return null;
            return "";
        }
        orgStr = unSignMore(orgStr);
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < orgStr.length(); i++) {
            buf.append(toUnsign(orgStr.charAt(i)));
        }
        String result = buf.toString();
        return result;
    }

    public static String unSignMore(String file) {
        if (file != null) {
            file = file.replace('à', 'a');
            file = file.replace('á', 'a');
            file = file.replace('ả', 'a');
            file = file.replace('ã', 'a');
            file = file.replace('ạ', 'a');
            file = file.replace('ă', 'a');
            file = file.replace('ằ', 'a');
            file = file.replace('ắ', 'a');
            file = file.replace('ẳ', 'a');
            file = file.replace('ẵ', 'a');
            file = file.replace('ặ', 'a');
            file = file.replace('â', 'a');
            file = file.replace('ầ', 'a');
            file = file.replace('ấ', 'a');
            file = file.replace('ẩ', 'a');
            file = file.replace('ẫ', 'a');
            file = file.replace('ậ', 'a');
            file = file.replace('À', 'A');
            file = file.replace('Á', 'A');
            file = file.replace('Ả', 'A');
            file = file.replace('Ã', 'A');
            file = file.replace('Ạ', 'A');
            file = file.replace('Ă', 'A');
            file = file.replace('Ằ', 'A');
            file = file.replace('Ắ', 'A');
            file = file.replace('Ẳ', 'A');
            file = file.replace('Ẵ', 'A');
            file = file.replace('Ặ', 'A');
            file = file.replace('Â', 'A');
            file = file.replace('Ầ', 'A');
            file = file.replace('Ấ', 'A');
            file = file.replace('Ẩ', 'A');
            file = file.replace('Ẫ', 'A');
            file = file.replace('Ậ', 'A');
            file = file.replace('đ', 'd');
            file = file.replace('Đ', 'D');
            file = file.replace('è', 'e');
            file = file.replace('é', 'e');
            file = file.replace('ẻ', 'e');
            file = file.replace('ẽ', 'e');
            file = file.replace('ẹ', 'e');
            file = file.replace('ê', 'e');
            file = file.replace('ề', 'e');
            file = file.replace('ế', 'e');
            file = file.replace('ể', 'e');
            file = file.replace('ễ', 'e');
            file = file.replace('ệ', 'e');
            file = file.replace('È', 'E');
            file = file.replace('É', 'E');
            file = file.replace('Ẻ', 'E');
            file = file.replace('Ẽ', 'E');
            file = file.replace('Ẹ', 'E');
            file = file.replace('Ê', 'E');
            file = file.replace('Ề', 'E');
            file = file.replace('Ế', 'E');
            file = file.replace('Ể', 'E');
            file = file.replace('Ễ', 'E');
            file = file.replace('Ệ', 'E');
            file = file.replace('ì', 'i');
            file = file.replace('í', 'i');
            file = file.replace('ỉ', 'i');
            file = file.replace('ĩ', 'i');
            file = file.replace('ị', 'i');
            file = file.replace('Ì', 'I');
            file = file.replace('Í', 'I');
            file = file.replace('Ỉ', 'I');
            file = file.replace('Ĩ', 'I');
            file = file.replace('Ị', 'I');
            file = file.replace('ò', 'o');
            file = file.replace('ó', 'o');
            file = file.replace('ỏ', 'o');
            file = file.replace('õ', 'o');
            file = file.replace('ọ', 'o');
            file = file.replace('ô', 'o');
            file = file.replace('ồ', 'o');
            file = file.replace('ố', 'o');
            file = file.replace('ổ', 'o');
            file = file.replace('ỗ', 'o');
            file = file.replace('ộ', 'o');
            file = file.replace('ơ', 'o');
            file = file.replace('ờ', 'o');
            file = file.replace('ớ', 'o');
            file = file.replace('ở', 'o');
            file = file.replace('ỡ', 'o');
            file = file.replace('ợ', 'o');
            file = file.replace('Ò', 'O');
            file = file.replace('Ó', 'O');
            file = file.replace('Ỏ', 'O');
            file = file.replace('Õ', 'O');
            file = file.replace('Ọ', 'O');
            file = file.replace('Ô', 'O');
            file = file.replace('Ồ', 'O');
            file = file.replace('Ố', 'O');
            file = file.replace('Ổ', 'O');
            file = file.replace('Ỗ', 'O');
            file = file.replace('Ộ', 'O');
            file = file.replace('Ơ', 'O');
            file = file.replace('Ờ', 'O');
            file = file.replace('Ớ', 'O');
            file = file.replace('Ở', 'O');
            file = file.replace('Ỡ', 'O');
            file = file.replace('Ợ', 'O');
            file = file.replace('ù', 'u');
            file = file.replace('ú', 'u');
            file = file.replace('ủ', 'u');
            file = file.replace('ũ', 'u');
            file = file.replace('ụ', 'u');
            file = file.replace('ư', 'u');
            file = file.replace('ừ', 'u');
            file = file.replace('ứ', 'u');
            file = file.replace('ử', 'u');
            file = file.replace('ữ', 'u');
            file = file.replace('ự', 'u');
            file = file.replace('Ù', 'U');
            file = file.replace('Ú', 'U');
            file = file.replace('Ủ', 'U');
            file = file.replace('Ũ', 'U');
            file = file.replace('Ụ', 'U');
            file = file.replace('Ư', 'U');
            file = file.replace('Ừ', 'U');
            file = file.replace('Ứ', 'U');
            file = file.replace('Ử', 'U');
            file = file.replace('Ữ', 'U');
            file = file.replace('Ự', 'U');
            file = file.replace('ỳ', 'y');
            file = file.replace('ý', 'y');
            file = file.replace('ỷ', 'y');
            file = file.replace('ỹ', 'y');
            file = file.replace('ỵ', 'y');
            file = file.replace('Y', 'Y');
            file = file.replace('Ỳ', 'Y');
            file = file.replace('Ý', 'Y');
            file = file.replace('Ỷ', 'Y');
            file = file.replace('Ỹ', 'Y');
            file = file.replace('Ỵ', 'Y');
        }
        return file;
    }

    public static char toUnsign(char c) {
        for (char[] signChar : signChars) {
            for (char aSignChar : signChar) {
                if (aSignChar == c) {
                    return signChar[0];
                }
            }
        }
        return c;
    }

    /**
     *
     * @param unescapeChars
     * @return
     */
    public static String escapeChars(String unescapeChars) {
        String escaped = unescapeChars;
        if (unescapeChars != null) {
            StringBuilder stringBuilder = new StringBuilder();
            StringCharacterIterator iterator = new StringCharacterIterator(unescapeChars);
            char character = iterator.current();
            while (character != CharacterIterator.DONE) {
                if (character == '+') {
                    stringBuilder.append("_0");
                } else if (character == '/') {
                    stringBuilder.append("_1");
                } else if (character == '=') {
                    stringBuilder.append("_2");
                } else if (character == ':') {
                    stringBuilder.append("_3");
                } else {
                    stringBuilder.append(character);
                }
                character = iterator.next();
            }
            escaped = stringBuilder.toString();
        }
        return escaped;
    }

    /**
     *
     * @param scapeChars
     * @return
     */
    public static String unescapeChars(String scapeChars) {
        String escaped = scapeChars;
        if (scapeChars != null) {
            escaped = escaped.replaceAll("_0", "+");
            escaped = escaped.replaceAll("_1", "/");
            escaped = escaped.replaceAll("_2", "=");
            escaped = escaped.replaceAll("_3", ":");
        }
        return escaped;
    }
}
