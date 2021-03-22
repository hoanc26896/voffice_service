/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.utils;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Lop da ngon ngu
 * 
 * @author thanght6
 */
public class I18N {
    
    /** Ngon ngu mac dinh */
    private static final String DEFAULT_LANGUAGE = "vi";
    
    /** Duong dan file cau hinh da ngon nu */
    private static final String CONFIG_PATH = "message";    
    
    private ResourceBundle bundle;
    
    /**
     * <b>Khoi tao</b>
     * 
     * @param language  ngon ngu 
     */
    public I18N(String language) {
        
        if (CommonUtils.isEmpty(language)) {
            language = DEFAULT_LANGUAGE;
        }
        bundle = ResourceBundle.getBundle(CONFIG_PATH, new Locale(language));
    }
    
    /**
     * <b>Lay gia tri</b><br>
     * 
     * @param key
     * @return 
     */
    public String getString(String key) {
        
        String value = "";
        if (!CommonUtils.isEmpty(key) && bundle != null && bundle.containsKey(key)) {
            value = bundle.getString(key);
        }
        return value;
    }
    
    public String getStringHasValue(String key, Object... strings) {
    	String value = "";
        if (!CommonUtils.isEmpty(key) && bundle != null && bundle.containsKey(key)) {
            value = MessageFormat.format(bundle.getString(key), strings);
        }
        return value;
    }
    
    public class Key {
        
        public static final String DOCUMENT_CODE = "document_code";
        public static final String DOCUMENT_NUMBER = "document_number";
        public static final String PUBLISHED_DATE = "published_date";
        //Tunghd add voi truong hop van ban den thi sua ngay ban hanh thanh ngay den
        public static final String ARRIVE_DATE = "arrive_date";
        //End
        public static final String OWNERSHIP = "ownership";
        public static final String SIGNED = "signed";
        public static final String NO = "no";
        public static final String SIGNER = "signer";
        public static final String DEPARTMENT = "department";
        public static final String PROCESSING_STATUS = "processing_status";
        public static final String SIGNED_TIME = "signed_time";
        public static final String COMMENT = "comment";
        // 201811: Pitagon - signature
        public static final String SIGN_IMAGE = "sign_image";
        // End 201811: Pitagon - signature
        // datdc add
        public static final String APPROVE = "approve";
        public static final String REJECT = "reject";
        //Tunghd add for dong dau mac dinh va khung ban hanh start
        public static final String TIME_SIGN = "time_sign";
        public static final String LABEL_DOC_NUMBER = "label_doc_number";
        public static final String LABEL_EMAIL = "label_email";
        public static final String LABEL_SIGN_BY = "label_sign_by";
        public static final String LABEL_TIME_SIGN = "label_time_sign";
        public static final String NOT_ISSUED = "not_issued";
        //Tunghd add for dong dau mac dinh va khung ban hanh end
        // Datdc add for mark advance start
        // so ky hieu van ban
        public static final String MARK_CODE = "mark_headerCode";
        // Nguoi dong
        public static final String MARK_BY = "mark_mark_by";
        // Thoi gian dong
        public static final String MARK_TIME = "mark_markTime";
        // Datdc add for mark advance end
    }
}