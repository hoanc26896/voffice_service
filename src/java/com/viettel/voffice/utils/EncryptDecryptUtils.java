package com.viettel.voffice.utils;

import java.util.Vector;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class EncryptDecryptUtils {

    private byte[] key = {(byte) 0xA1, (byte) 0xE3, (byte) 0xC2,
        (byte) 0x19, (byte) 0x19, (byte) 0xAD, (byte) 0xEE, (byte) 0xAB
    };
    private String algorithm = "DES";
    private SecretKeySpec keySpec;

    public EncryptDecryptUtils() {
        keySpec = new SecretKeySpec(key, algorithm);
    }

    public EncryptDecryptUtils(byte[] k, String alg) {
        key = k;
        algorithm = alg;
        keySpec = new SecretKeySpec(key, algorithm);
    }

    public String encrypt(String text) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] data = cipher.doFinal(text.getBytes());

        StringBuilder value = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            int b = data[i];
            if (b < 0) {
                value.append('-');
                b = -b;
            }
            if (b < 16) {
                value.append('0');
            }
            value.append(Integer.toHexString(b).toUpperCase());
        }
        return value.toString();
    }

    public byte[] encrypt(byte[] arrByte) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] data = cipher.doFinal(arrByte);

        return data;
    }

    public String decrypt(String text) throws Exception {
        Vector<Byte> res = new Vector<Byte>();
        int pos = 0;
        int len;
        while (pos < text.length()) {
            len = (("-".equals(text.substring(pos, pos + 1))) ? 3 : 2);
            int test = Integer.parseInt(text.substring(pos, pos + len), 16);
            pos += len;
            res.add(new Byte((byte) test));
        }
        if (res.size() > 0) {
            byte[] data = new byte[res.size()];
            for (int i = 0; i < res.size(); i++) {
                Byte b = res.elementAt(i);
                data[i] = b.byteValue();
            }
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            return new String(cipher.doFinal(data));
        }
        return null;
    }

    public byte[] decrypt(byte[] arrByte) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        return cipher.doFinal(arrByte);
    }
}
