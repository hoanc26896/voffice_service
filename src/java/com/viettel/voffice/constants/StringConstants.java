/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.constants;

/**
 *
 * @author datnv5
 */
public class StringConstants {

    public static final String STR_RESULT_RETURN_FULL = "{\"result\":{\"mess\":%s,\"data\": %s}}";
    public static final String STR_RESULT_RETURN_MESS = "{\"result\":{\"mess\":%s,\"data\":{}}}";
    public static final String STR_EMTY = "";
    //chuoi xu ly sql
    public static final String STR_SQLPARAM = ":%s";
    public static final String STR_SQLASKPARAM = "?";
    //key tách khoa aes thanh 2 phan aesKey va Vi key
    public static final String STR_VIAESKEYSPACE = "VIAESKEYSPACE";
    //key lay session tu header
    public static final String STR_SESSIONID = "session_id";
    public static final String STR_COOKIE = "cookie";
    public static final String STR_JSESSIONID = "JSESSIONID=";
    public static final String STR_URL_SSO = "";
    public static final String STR_FILE_CONFIGPROPERTI = "config.properties";
    public static final String STR_FILE_CONFIG = "config";
    public static final String STR_REPLACE_KEYMESS = "DATNV5";
    public static final String STR_COMMA = ",";
    //========Nhom khai bao tim kiem trong SQL ==================
    public static final String strSpec = "áàảãạăắằẳẵặâấầẩẫậđéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúù"
            + "ủũụưứừửữựýỳỷỹỵÁÀẢÃẠĂẮẰẲẴẶÂẤẦẨẪẬĐÉÈẺẼẸÊẾỀỂỄỆÍÌỈĨỊÓÒỎÕỌÔỐỒỔỖỘƠỚỜỞỠỢÚÙỦŨỤƯỨỪỬỮỰÝỲỶỸỴ";
    public static final String strRepl = "aaaaaaaaaaaaaaaaadeeeeeeeeeeeiiiiiooooooooooooooooouu"
            + "uuuuuuuuuyyyyyAAAAAAAAAAAAAAAAADEEEEEEEEEEEIIIIIOOOOOOOOOOOOOOOOOUUUUUUUUUUUYYYYY";
    //======dinh nghia danh sach key lay tu file config.properties
    public static final String STR_PROPERTIES_CERTPATH = "sign.certpath";
    public  static  char[] replaceUnsign(){
        return strRepl.toCharArray();
    }
}
