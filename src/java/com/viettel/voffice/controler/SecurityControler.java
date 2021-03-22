/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.constants.StringConstants;
import com.viettel.voffice.security.AES;
import com.viettel.voffice.security.RSA;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author datnv5
 */
public class SecurityControler {

    private static final Logger log = Logger.getLogger(SecurityControler.class);

    /**
     * tra ve khoa cong khai va bi mat cua he RSA
     *
     * @return
     */
    public static RSA getKeyRsa() {
        return new RSA();
    }

    /**
     * decode data RSA
     *
     * @param strPublicKey
     * @param strData
     * @param isIos
     * @return
     */
    public static String decodeDataByRSA(String strPublicKey, String strData,
            Boolean isIos) {
        String strResult = "";
        try {
            PrivateKey prvKey = RSA.convertByteToPrivateKey(
                    FunctionCommon.hexStringToByteArray(strPublicKey));
            strResult = RSA.decryptData(FunctionCommon.hexStringToByteArray(strData),
                    prvKey, isIos);
        } catch (IOException ex) {
            log.error("Loi! decodeDataRSA: ", ex);
        }
        return strResult;
    }

    /**
     * giai ma aes
     *
     * @param keyAES
     * @param data
     * @return
     */
    public synchronized static String decodeDataByAes(String keyAES, String data) {
        String result = null;
        int loop = 3;
        SecurityControler securityControler = new SecurityControler();
        for (int i = 0; i < loop; i++) {
            result = securityControler.decodeDataByAesCheckError(keyAES, data);
            if (result != null && result.trim().length() > 0) {
                try {
                    new JSONObject(result);
                    //neu du lieu hop le thi break;
                    //con khong thi giai ma lai
                    break;
                } catch (Exception ex) {
                    log.error(ex);
                    try {
                        new JSONArray(result);
                        break;
                    } catch (JSONException ex1) {
                        log.error(ex1);
                        log.error("=====Co loi gia ma du lieu dau vao:"
                                + result + ", aesKey:" + keyAES);
                    }

                }
            }
        }
        return result;
    }

    /**
     * giai ma aes
     *
     * @param keyAES
     * @param data
     * @return
     */
    private String decodeDataByAesCheckError(String keyAES, String data) {
        String strDecodeData;
        try {
            AES aesSecurity = new AES();

            String[] strKey = keyAES.split(StringConstants.STR_VIAESKEYSPACE);
            if (strKey.length == 2 && data != null && data.trim().length() > 0) {
                strDecodeData = aesSecurity.decrypt(data, strKey[0], strKey[1]);
            } else {
                strDecodeData = null;
            }
        } catch (InvalidKeyException | UnsupportedEncodingException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException ex) {
            strDecodeData = null;
            log.error("Loi! decodeDataByAes: keyAES =" + keyAES + ", data=" + data, ex);
        }
        return strDecodeData;
    }

    /**
     * ma hoa aes key
     *
     * @param data
     * @param keyAES
     * @return
     */
    public static String encodeDataByAes(String keyAES, String data) {
        String strDecodeData;
        try {
            AES aesSecurity = new AES();
            String[] strKey = keyAES.split(StringConstants.STR_VIAESKEYSPACE);
            if (strKey.length == 2) {
                strDecodeData = aesSecurity.encrypt(data, strKey[0], strKey[1]);
            } else {
                strDecodeData = "";
            }
        } catch (InvalidKeyException | UnsupportedEncodingException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException ex) {
            strDecodeData = null;
            log.error("Loi! encodeDataByAes: ", ex);
        }
        return strDecodeData;
    }

    //======================ma hoa va giai ma file tren duong truyen======
    /**
     * ma hoa aes key
     *
     * @param keyAES
     * @param strFileIn
     * @param strFileOut
     * @return
     */
    public static Boolean encodeDataByAesFile(String keyAES, String strFileIn, String strFileOut) {
        Boolean strDecodeData = false;
        {
            BufferedInputStream is = null;
            try {
                AES aesSecurity = new AES();
                String[] strKey = keyAES.split(StringConstants.STR_VIAESKEYSPACE);
                if (strKey.length == 2) {
                    is = new BufferedInputStream(new FileInputStream(strFileIn));
                    BufferedOutputStream os = new BufferedOutputStream(
                            new FileOutputStream(strFileOut));
                    aesSecurity.encryptFile(is, os, strKey[0], strKey[1]);
                    strDecodeData = true;
                } else {
                    strDecodeData = false;
                }
            } catch (IOException ex) {
                log.error("Loi! encodeDataByAes: ", ex);
            } catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException ex) {
                strDecodeData = null;
                log.error("Loi! encodeDataByAes: ", ex);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException ex) {
                    log.error("Loi! encodeDataByAes: ", ex);
                }
            }
        }
        return strDecodeData;
    }

    /**
     *
     * @param strPrivateKey
     * @param strData
     * @return
     */
//    public static String decryptDataByRSA(String strPrivateKey, String strData) {
//        String strResult = "";
//        try {
//            PrivateKey privateKey = RSA.convertByteToPrivateKey(FunctionCommon
//                    .hexStringToByteArray(strPrivateKey));
//            strResult = RSA.decrypt(privateKey, FunctionCommon.hexStringToByteArray(strData));
//        } catch (Exception ex) {
//            log.error("decryptDataByRSA - Exception: ", ex);
//        }
//        return strResult;
//    }
}
