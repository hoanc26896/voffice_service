package com.viettel.voffice.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import org.apache.log4j.Logger;

public class EncryptDecryptSignDocument {

    private static final Logger logger = Logger.getLogger(EncryptDecryptSignDocument.class);

    public EncryptDecryptSignDocument() {
    }

    public void encrypt(SecretKey key, InputStream is, OutputStream os)
            throws Throwable {
        encryptOrDecrypt(key, Cipher.ENCRYPT_MODE, is, os);
    }

    public void decrypt(SecretKey key, InputStream is, OutputStream os)
            throws Throwable {
        encryptOrDecrypt(key, Cipher.DECRYPT_MODE, is, os);
    }

    public void encryptOrDecrypt(SecretKey key, int mode, InputStream is,
            OutputStream os) throws Throwable {

        Cipher cipher = Cipher.getInstance("DES");

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

    public void doCopy(InputStream is, OutputStream os) throws IOException {
        byte[] bytes = new byte[1024 * 8];
        int numBytes;
        while ((numBytes = is.read(bytes)) != -1) {
            os.write(bytes, 0, numBytes);
        }
        os.flush();
        os.close();
        is.close();
    }

    public SecretKey getKey() throws Exception {
        byte[] key = {(byte) 0xA1, (byte) 0xE3, (byte) 0xC2, (byte) 0x19,
            (byte) 0x19, (byte) 0xAD, (byte) 0xEE, (byte) 0xAB};
        SecretKey keySpec = null;
        String algorithm = "DES";
        try {
            DESKeySpec dks = new DESKeySpec(key);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(algorithm);
            keySpec = skf.generateSecret(dks);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return keySpec;
    }
}
