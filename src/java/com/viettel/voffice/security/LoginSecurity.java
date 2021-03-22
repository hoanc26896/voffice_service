/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.apache.log4j.Logger;
import org.apache.xml.security.utils.Base64;

public class LoginSecurity {

    private static final Logger LOGGER = Logger.getLogger(LoginSecurity.class);

    public static final String SALT = "voffice@)!$2017@)!$";
    public static final String AES_SALT = "VOffice@)!$2017.";
    private static SecretKeySpec secretKeySpecAES;

    public static final class ALGORITHM {

        public static final String SHA1 = "SHA-1";
        public static final String AES = "AES";
    }

    public static final class TRANSFORMATION {

        public static final String AES_ECB_PKCS5 = "AES/ECB/PKCS5Padding";
    }

    public static final class PROVIDER {

        public static final String JCE = "SunJCE";
    }

    public static final class CHARSET {

        public static final String UTF_8 = "UTF-8";
    }

    static {
        try {
            secretKeySpecAES = new SecretKeySpec(AES_SALT.getBytes(CHARSET.UTF_8), ALGORITHM.AES);
        } catch (Exception e) {
            LOGGER.error("Could not get AES SecretKeySpec", e);
        }
    }

    /**
     * Encrypt AES
     *
     * @param message
     * @return
     * @throws Exception
     */
    public static byte[] encryptAES(String message) throws Exception {
        if (secretKeySpecAES == null) {
            return null;
        }
        Cipher cipher = Cipher.getInstance(TRANSFORMATION.AES_ECB_PKCS5, PROVIDER.JCE);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpecAES);
        return cipher.doFinal(message.getBytes(CHARSET.UTF_8));
    }

    /**
     * Encrypt AES to string
     *
     * @param message
     * @return
     * @throws Exception
     */
    public static String encryptToStringAES(String message) throws Exception {
        if (secretKeySpecAES == null) {
            return null;
        }
        return Base64.encode(encryptAES(message));
    }

    /**
     * Encode password using SHA and SALT
     *
     * @param password
     * @return
     */
    public static String encodePassword(String password) {
        String passwordAndSalt = password + SALT;
        try {
            return encryptDigest(passwordAndSalt);
        } catch (Exception e) {
            LOGGER.error(null, e);
            return "";
        }
    }

    /**
     * Encrypt text by SHA-1
     *
     * @param plaintext
     * @return
     * @throws Exception
     */
    public static String encryptDigest(String plaintext) throws Exception {
        String hash = null;
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(ALGORITHM.SHA1);
            if (md != null) {
                md.update(plaintext.getBytes(CHARSET.UTF_8));
                byte[] raw = md.digest();
                hash = Base64.encode(raw);
            }
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            LOGGER.error(null, e);
        }
        return hash;
    }
    
    /**
     * ma hoa pass 1.0
     * @param plaintext
     * @return 
     */
    public synchronized static String encrypt(String plaintext) {
        MessageDigest mdMessageDigest = null;
        String hash = null;
        try {
            mdMessageDigest = MessageDigest.getInstance("SHA-1"); // step 2
            mdMessageDigest.update(plaintext.getBytes("UTF-8")); // step 3
            byte raw[] = mdMessageDigest.digest(); // step 4
            hash = Base64.encode(raw); 
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
              LOGGER.error("encrypt", e);
        } 
        return hash; // step 6
    }

}
