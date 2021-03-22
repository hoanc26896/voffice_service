/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.utils;

/**
 *
 * @author tuantm18
 */
import com.viettel.voffice.security.SecurityUtil;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import sun.misc.BASE64Encoder;

/**
 * Ma hoa file Viettel.
 *
 * @author HuyenNV
 * @version 1.0
 * @since 1.0
 */
public class Encryption {

    private final static byte KEYS[] = {-95, -29, -62, 25, 25, -83, -18, -85};
    private final static String ALGORITHM = "DES";
    private final static SecretKeySpec SECRET_KEY_SPEC = new SecretKeySpec(KEYS, ALGORITHM);
    private static final String dCase = "abcdefghijklmnopqrstuvwxyz";
    private static final String uCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String sChar = "!@#$%^&*";
    private static final String intChar = "0123456789";
    private static Random r = new Random();

    private static byte[] encrypt(byte arrByte[]) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(1, SECRET_KEY_SPEC);
        return cipher.doFinal(arrByte);
    }

    private static byte[] decrypt(byte arrByte[]) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(2, SECRET_KEY_SPEC);
        return cipher.doFinal(arrByte);
    }

    /**
     * Ma hoa file.
     *
     * @param originalFilePath File goc
     * @param encryptedFilePath File da duoc ma hoa
     * @throws Exception
     */
    public static void encryptFile(String originalFilePath, String encryptedFilePath) throws Exception {
        FileInputStream stream = new FileInputStream(originalFilePath);
        OutputStream out = new FileOutputStream(encryptedFilePath);
        int bytesRead;
        byte buffer[] = new byte[8192];
        byte cloneBuffer[];
        for (; (bytesRead = stream.read(buffer, 0, 8192)) != -1; out.write(encrypt(cloneBuffer))) {
            cloneBuffer = new byte[bytesRead];
            if (bytesRead >= buffer.length) {
                continue;
            }
            System.arraycopy(buffer, 0, cloneBuffer, 0, bytesRead);
        }
        stream.close();
        out.close();
    }

    /**
     * Giai ma file.
     *
     * @param encryptedFilePath File da ma hoa
     * @return Xau da duoc giai ma
     * @throws Exception
     */
    public static String decryptFile(String encryptedFilePath) throws Exception {
        String returnValue = "";
        FileInputStream stream = new FileInputStream(encryptedFilePath);
        int bytesRead;
        byte buffer[] = new byte[8192];
        byte cloneBuffer[];
        for (; (bytesRead = stream.read(buffer, 0, 8192)) != -1; returnValue = (new StringBuilder()).append(returnValue).append(new String(decrypt(cloneBuffer))).toString()) {
            cloneBuffer = new byte[bytesRead];
            if (bytesRead >= buffer.length) {
                continue;
            }
            System.arraycopy(buffer, 0, cloneBuffer, 0, bytesRead);
        }
        stream.close();
        return returnValue;
    }

    /**
     * Ma hoa du lieu nhay cam (password).
     *
     * @param plainText Xau thuong
     * @param salt Xau duoc cong them vao xau thuong cho phuc tap
     * @return Xau ma hoa
     * @throws Exception
     */
    public static String encrypt(String plainText, String salt) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update((plainText + salt).getBytes("UTF-8"));
        return (new BASE64Encoder()).encode(md.digest());
    }

    /**
     * Gia ma xau sau khi ma hoa
     *
     * @param filePath
     * @return
     * @throws java.io.IOException
     */
    public static Map<String, String> getDataDecryptFile(String filePath) throws IOException, Exception {
        SecurityUtil securityUtil = new SecurityUtil();
        Map<String, String> result = new HashMap<String, String>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line != null && !line.isEmpty()) {
                String[] temp = securityUtil.decrypt(line).split("=", 2);
                if (temp.length == 2) {
                    result.put(temp[0].trim(), temp[1].trim());
                }
            }
        }
        reader.close();
        return result;
    }
    
    public static String getStrDataDecrypt(String str) throws IOException, Exception {
        if (str != null && !str.isEmpty()) {
            SecurityUtil securityUtil = new SecurityUtil();
            String[] temp = securityUtil.decrypt(str).split("=", 2);
            return temp[1].trim();
        }
        return null;
    }

    /**
     * Gen new password.
     *
     * @param length
     * @return
     */
    public static String genNewPass(int length) {
        String pass = "";
        while (pass.length() != length) {
            int rPick = r.nextInt(4);
            if (rPick == 0) {
                int spot = r.nextInt(25);
                pass += dCase.charAt(spot);
            } else if (rPick == 1) {
                int spot = r.nextInt(25);
                pass += uCase.charAt(spot);
            } else if (rPick == 2) {
                int spot = r.nextInt(7);
                pass += sChar.charAt(spot);
            } else if (rPick == 3) {
                int spot = r.nextInt(9);
                pass += intChar.charAt(spot);
            }
        }
        return pass;
    }
}

