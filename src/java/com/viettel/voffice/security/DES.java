/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * Encrypt/Decrypt by DES Algorithm
 * 
 * @author thanght6
 * @since Apr 26, 2016
 */
public class DES {
    
    // Key
    private static final byte[] KEY = {(byte) 0xA1, (byte) 0xE3, (byte) 0xC2, 
        (byte) 0x19, (byte) 0x19, (byte) 0xAD, (byte) 0xEE, (byte) 0xAB};
    
    // Algorithm
    private static final String ALGORITHM = "DES";
    
    public static SecretKey getKey() throws InvalidKeyException, NoSuchAlgorithmException,
            InvalidKeySpecException {
        DESKeySpec dks = new DESKeySpec(KEY);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
        return skf.generateSecret(dks);
    }

    public static void encrypt(InputStream is, OutputStream os)
            throws Throwable {
        encryptOrDecrypt(getKey(), Cipher.ENCRYPT_MODE, is, os);
    }

    public static void decrypt(InputStream is, OutputStream os)
            throws Throwable {
        encryptOrDecrypt(getKey(), Cipher.DECRYPT_MODE, is, os);
    }

    public static void encryptOrDecrypt(SecretKey key, int mode, InputStream is,
            OutputStream os) throws Throwable {

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        if (mode == Cipher.ENCRYPT_MODE) {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            CipherInputStream cis = new CipherInputStream(is, cipher);
            doCopy(cis, os);
        } else if (mode == Cipher.DECRYPT_MODE) {
            cipher.init(Cipher.DECRYPT_MODE, key);
            CipherOutputStream cos = new CipherOutputStream(os, cipher);
            doCopy(is, cos);
        }
    }

    public static void doCopy(InputStream is, OutputStream os) throws IOException {
        byte[] bytes = new byte[1024 * 8];
        int numBytes;
        while ((numBytes = is.read(bytes)) != -1) {
            os.write(bytes, 0, numBytes);
        }
        os.flush();
        os.close();
        is.close();
    }
}
