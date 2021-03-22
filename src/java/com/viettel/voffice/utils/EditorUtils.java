/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.utils;

import org.apache.log4j.Logger;

/**
 *
 * @author Nguyen Tien Dung
 */
public class EditorUtils {

    private static final Logger logger = Logger.getLogger(EditorUtils.class);

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

    /* Method convert VietNamese String with sign charactor to
     * VietNamese String with unsign charactor. This method is
     * use when convert message to send to subcriber by sms
     * which many device can't display sms as VietNamese.
     * @param orgStr
     * @return String with all sign character is converted to unsign
     */
    public static String toUnSign(String orgStr) {
        if (orgStr == null || orgStr.length() == 0) {
            //thaida sua lai truong hop nay
            //return null;
            return "";
        }
        orgStr = unSignMore(orgStr);
        StringBuilder buf = new StringBuilder();
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
            StringBuilder strBf = new StringBuilder();
            char[] array = file.toCharArray();
            for (char ch : array) {
                if ((int) ch >= 256) {
                    ch = ' ';
                }
                strBf.append(ch);
            }
            file = strBf.toString();
        }
        return file;
    }

    public static String unSignUpperCase(String file) {
        if (file != null) {
            file = file.replace('À', 'à');
            file = file.replace('Á', 'á');
            file = file.replace('Ả', 'ả');
            file = file.replace('Ã', 'ã');
            file = file.replace('Ạ', 'ạ');
            file = file.replace('Ă', 'ă');
            file = file.replace('Ằ', 'ằ');
            file = file.replace('Ắ', 'ắ');
            file = file.replace('Ẳ', 'ẳ');
            file = file.replace('Ẵ', 'ẵ');
            file = file.replace('Ặ', 'ặ');
            file = file.replace('Â', 'â');
            file = file.replace('Ầ', 'ầ');
            file = file.replace('Ấ', 'ấ');
            file = file.replace('Ẩ', 'ẩ');
            file = file.replace('Ẫ', 'ẫ');
            file = file.replace('Ậ', 'ậ');
            file = file.replace('È', 'è');
            file = file.replace('É', 'é');
            file = file.replace('Ẻ', 'ẻ');
            file = file.replace('Ẽ', 'ẽ');
            file = file.replace('Ẹ', 'ẹ');
            file = file.replace('Ê', 'ê');
            file = file.replace('Ề', 'ề');
            file = file.replace('Ế', 'ế');
            file = file.replace('Ể', 'ể');
            file = file.replace('Ễ', 'ễ');
            file = file.replace('Ệ', 'ệ');
            file = file.replace('Ì', 'ì');
            file = file.replace('Í', 'í');
            file = file.replace('Ỉ', 'ỉ');
            file = file.replace('Ĩ', 'ĩ');
            file = file.replace('Ị', 'ị');
            file = file.replace('Ò', 'ò');
            file = file.replace('Ó', 'ó');
            file = file.replace('Ỏ', 'ỏ');
            file = file.replace('Õ', 'õ');
            file = file.replace('Ọ', 'ọ');
            file = file.replace('Ô', 'ô');
            file = file.replace('Ồ', 'ồ');
            file = file.replace('Ố', 'ố');
            file = file.replace('Ổ', 'ổ');
            file = file.replace('Ỗ', 'ỗ');
            file = file.replace('Ộ', 'ộ');
            file = file.replace('Ơ', 'ơ');
            file = file.replace('Ờ', 'ờ');
            file = file.replace('Ớ', 'ớ');
            file = file.replace('Ở', 'ở');
            file = file.replace('Ỡ', 'ỡ');
            file = file.replace('Ợ', 'ợ');
            file = file.replace('Ù', 'ù');
            file = file.replace('Ú', 'ú');
            file = file.replace('Ủ', 'ủ');
            file = file.replace('Ũ', 'ũ');
            file = file.replace('Ụ', 'ụ');
            file = file.replace('Ư', 'ư');
            file = file.replace('Ừ', 'ừ');
            file = file.replace('Ứ', 'ứ');
            file = file.replace('Ử', 'ử');
            file = file.replace('Ữ', 'ữ');
            file = file.replace('Ự', 'ự');
            file = file.replace('Ỳ', 'ỳ');
            file = file.replace('Ý', 'ý');
            file = file.replace('Ỷ', 'ỷ');
            file = file.replace('Ỹ', 'ỹ');
            file = file.replace('Ỵ', 'ỵ');
            file = file.replaceAll("“", "\"");
            file = file.replaceAll("”", "\"");

        }
        return file;
    }

    /**
     * decode a sign char to unsign char
     *
     * @param c
     * @return unsign character
     */
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

    public static int getIntValue(String s, int defaultValue) throws Exception {
        try {
            return Integer.parseInt(s);
        } catch (Exception ex) {
            logger.error(ex);
            return defaultValue;
        }
    }

    public static void main(String[] arg) {
//        String a = "Hôm nay trời đẹp tuyệt với! Lòng tôi, phới “hong hon”/fdg/%, ^,&";
//        System.out.println("Chuoi hien thi la " + unSignMore(a));
    }

    public static String sliptStringTitleSign(String st, int len) {
        String tmpSt;
        int lengthSt = st.length();
        if (lengthSt > len) {

            tmpSt = st.substring(0, len);
            int idx = 0;
            for (int i = len - 1; i >= 0; i--) {

                if (" ".equals(tmpSt.substring(i, tmpSt.length() - idx))) {

                    tmpSt = tmpSt.substring(0, i).trim();
                    break;
                }
                idx++;
            }

        } else {
            tmpSt = st;
        }

        return tmpSt;
    }
}
