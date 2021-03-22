/**
 *
 */
package com.viettel.voffice.utils;

import java.util.Locale;
import java.util.ResourceBundle;
import org.apache.log4j.Logger;
import org.apache.xml.security.utils.Base64;

public class MessageUtils {

    private static final String RESOURCE_FILE_NAME = "core";

    // Log file
    private static final Logger LOGGER = Logger.getLogger(MessageUtils.class);

    public MessageUtils() {
        // TODO Auto-generated constructor stub
    }

    /**
     * Gets the resource string.
     *
     * @param language the language
     * @param key the key
     * @param params the params
     * @return the resource string
     */
    public static String getResourceString(String language, String key,
            Object... params) {
        ResourceBundle rs = ResourceBundle.getBundle(RESOURCE_FILE_NAME,
                new Locale(language));
        String text = "";
        if (rs != null) {
            text = rs.getString(key);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    if (params[i] != null) {
                        text = text
                                .replace("{" + i + "}", params[i].toString());
                    }
                }
            }
        }
        return text;
    }

    /**
     *
     * @param language
     * @param key
     * @return
     */
    public static String getResourceString(String language, String key) {
        ResourceBundle rs = ResourceBundle.getBundle(RESOURCE_FILE_NAME,
                new Locale(language));
        String text = "";
        if (rs != null) {
            text = rs.getString(key);
        }
        return text;
    }

    // Cau hinh them link mo app vao tin nhan
//    public static Integer CONFIG_ADD_LINK_INTO_MESSAGE;
    /**
     * <b>Lay cau hinh them link mo app vao tin nhan</b><br>
     *
     * @return
     */
    public static Integer getConfigAddLinkIntoMessage() {
        Integer result;
//        if (CONFIG_ADD_LINK_INTO_MESSAGE == null) {
//            
//        }
        try {
            result = Integer.parseInt(CommonUtils
                    .getAppConfigValue("message.add.link"));
        } catch (Exception ex) {
            LOGGER.error("getConfigAddLinkIntoMessage (Lay cau hinh them"
                    + " link mo app vao tin nhan) - Exception!", ex);
            result = 0;
        }
        return result;
    }

    // Url tro vao file index
    //public static String INDEX_FILE_URL;
    /**
     * <b>Lay url tro vao file index</b><br>
     *
     * @author ThangHT6
     * @since Dec 21, 2016
     * @return
     */
//    public static String getIndexFileUrl() {
//        if (INDEX_FILE_URL == null) {
//            try {
//                INDEX_FILE_URL = CommonUtils.getAppConfigValue("url.file.index");
//            } catch (Exception ex) {
//                LOGGER.error("getIndexFileUrl (Lay url tro vao file index) - Exception!", ex);
//            }
//        }
//        return INDEX_FILE_URL;        
//    }
    // Cu phap truy van trong duong link Url
    public static final String QUERY_STRING_SYNTAX = "type=%s&id=%d";

    // Loai cong van
    public static final String DOCUMENT_LINK_TYPE = "vb";

    // Loai link van ban trinh ky
    public static final String TEXT_LINK_TYPE = "ky";

    /**
     * <b>Them link vao tin nhan</b><br>
     *
     * @param message
     * @param type
     * @param id
     * @return
     */
    public static String addLinkIntoMessage(String message, String type, Long id) {

        // Kiem tra dau vao
        if (CommonUtils.isEmpty(message) || CommonUtils.isEmpty(type) || id == null) {
            return message;
        }
        try {
            Integer configAddLinkIntoMessage = getConfigAddLinkIntoMessage();
            if (configAddLinkIntoMessage == 1) {
                // Lay url tro vao file index
                String indexFileUrl = CommonUtils.getAppConfigValue("url.file.index");
                // Neu khong lay duoc -> Tra ve tin nhan nhu cu
                if (CommonUtils.isEmpty(indexFileUrl)) {
                    return message;
                }
                // Chen gia tri vao chuoi truy van
                String queryString = String.format(QUERY_STRING_SYNTAX, type, id);
                // Base64 chuoi truy van
                queryString = Base64.encode(queryString.getBytes());
                message += "\n" + indexFileUrl + "?" + queryString;
            }
        } catch (Exception ex) {
            LOGGER.error("addLinkIntoMessage (Them link vao tin nhan) - Exception!", ex);
        }
        return message;
    }
}
