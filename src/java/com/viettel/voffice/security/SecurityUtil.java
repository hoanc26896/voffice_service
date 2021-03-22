/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.log4j.Logger;

public class SecurityUtil {

    private static final int AES_128 = 16;
    private static final int BUFFER_SIZE = 8120;
    private static final byte[] DEFAULT_KEY = {77, 64, 110, 72, 102, 103, -23, 67, 54, -128, 74, 57, 48, 84, 86, 51};
    private static final String AES_ALGORITHM = "AES";
    private static final String DEFAULT_PASSWORD = "dAfaUlTPaSswoRd";
    private static final Logger LOGGER = Logger.getLogger(SecurityUtil.class);
    private Cipher _cipher;
    private byte[] _hexhars;
    //vinhnq13
    private byte[] key = {(byte) 0xA1, (byte) 0xE3, (byte) 0xC2,
        (byte) 0x19, (byte) 0x19, (byte) 0xAD, (byte) 0xEE, (byte) 0xAB
    };
    private String algorithm = "DES";
    private SecretKey keyDes;

    public void getKey() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException {
        DESKeySpec dks = new DESKeySpec(key);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(algorithm);
        keyDes = skf.generateSecret(dks);
    }

    public SecurityUtil() {
        try {
            this._cipher = Cipher.getInstance("AES");
            this._hexhars = new byte[]{100, 97, 50, 102, 55, 53, 54, 52, 56, 57, 49, 98, 99, 48, 101, 51};
        } catch (NoSuchAlgorithmException nsae) {
            LOGGER.error("Sai thuat toan ma hoa", nsae);
        } catch (NoSuchPaddingException nspe) {
            LOGGER.error("A particular padding mechanism is requested but is not available in the environment", nspe);
        }
    }

    private SecretKeySpec generateSecretKeySpec(byte[] keyDefault, String algorithm)
            throws Exception {
        String _customPassword = "dAfaUlTPaSswoRd";
        byte[] keyAlgorithm = (byte[]) keyDefault.clone();
        if (_customPassword.length() > 0) {
            int i = 0;
            for (char c : _customPassword.toCharArray()) {
                keyAlgorithm[i] = ((byte) (keyDefault[i] + c));
                i++;
                if (i == 16) {
                    i = 0;
                }
            }
        }
        SecretKeySpec key = new SecretKeySpec(keyAlgorithm, algorithm);
        return key;
    }

    private byte[] _doCrypt(byte[] inputs, int mode)
            throws Exception {
        Key key = generateSecretKeySpec(DEFAULT_KEY, "AES");
        this._cipher.init(mode, key);
        return this._cipher.doFinal(inputs);
    }

    private void _doCrypt(String inputFile, String outputFile, int mode) {
        try {
            FileInputStream inputStream = new FileInputStream(inputFile);
            OutputStream out = new FileOutputStream(outputFile);
            int bytesRead;
            byte[] buffer = new byte[8120];
            while ((bytesRead = inputStream.read(buffer, 0, 8120)) != -1) {
                byte[] cloneBuffer = new byte[bytesRead];
                if (bytesRead < buffer.length) {
                    System.arraycopy(buffer, 0, cloneBuffer, 0, bytesRead);
                }
                out.write(_doCrypt(cloneBuffer, mode));
            }
            inputStream.close();
            out.close();
        } catch (FileNotFoundException fex) {
            LOGGER.error("Loi khong tim thay file dau vao", fex);
        } catch (IOException iex) {
            LOGGER.error("Loi trong qua trinh doc file dau vao", iex);
        } catch (Exception ex) {
            LOGGER.error("Loi trong qua trinh ghi file ma hoa", ex);
        }
    }

    private String bytesToString(byte[] b) {
        StringBuilder s = new StringBuilder(2 * b.length);
        for (int i = 0; i < b.length; i++) {
            int v = b[i] & 0xFF;
            s.append((char) this._hexhars[(v >> 4)]);
            s.append((char) this._hexhars[(v & 0xF)]);
        }
        return s.toString();
    }

    private byte[] stringToBytes(String s) {
        if (s.length() % 2 != 0) {
            s = "0".concat(s);
        }
        int len = s.length() / 2;
        byte[] b = new byte[len];
        for (int i = 0; i < len; i++) {
            b[i] = ((byte) (indexInHexhars(s.charAt(i * 2)) << 4));
            b[i] = ((byte) (b[i] | indexInHexhars(s.charAt(i * 2 + 1)) & 0xF));
        }
        return b;
    }

    private int indexInHexhars(char b) {
        int i = 0;
        while ((i < this._hexhars.length) && (this._hexhars[i] != (byte) b)) {
            i++;
        }
        return i;
    }

    public byte[] encrypt(byte[] inputs)
            throws Exception {
        return _doCrypt(inputs, 1);
    }

    public byte[] decrypt(byte[] inputs)
            throws Exception {
        return _doCrypt(inputs, 2);
    }

    public String encrypt(String str)
            throws Exception {
        return bytesToString(encrypt(str.getBytes()));
    }

    public String decrypt(String str)
            throws Exception {
        return new String(decrypt(stringToBytes(str)));
    }

    public void encryptFile(String originFile, String outputEncryptedFile) {
        _doCrypt(originFile, outputEncryptedFile, 1);
    }

    public String decryptFile(InputStream inputStream) {
        StringBuilder sb = new StringBuilder();
        try {
            int bytesRead;
            byte[] buffer = new byte[8120];
            while ((bytesRead = inputStream.read(buffer, 0, 8120)) != -1) {
                byte[] cloneBuffer = new byte[bytesRead];
                if (bytesRead < buffer.length) {
                    System.arraycopy(buffer, 0, cloneBuffer, 0, bytesRead);
                }
                sb.append(new String(decrypt(cloneBuffer)));
            }
            inputStream.close();
        } catch (FileNotFoundException fex) {
            LOGGER.error("Loi khong tim thay file dau vao", fex);
        } catch (IOException iex) {
            LOGGER.error("Loi trong qua trinh doc file dau vao", iex);
        } catch (Exception ex) {
            LOGGER.error("Loi trong qua trinh ghi file ma hoa", ex);
        }
        return sb.toString();
    }

    public String decryptFile(String encryptedFilePath) {
        StringBuilder sb = new StringBuilder();
        try {
            FileInputStream inputStream = new FileInputStream(encryptedFilePath);
            sb.append(decryptFile(inputStream));
        } catch (FileNotFoundException fex) {
            LOGGER.error("Loi khong tim thay file dau vao", fex);
        }
        return sb.toString();
    }

    public static String encryptFile(String pdfFileName) throws FileNotFoundException, FileNotFoundException, IOException, Exception {
        File f = new File(pdfFileName);
        InputStream stream = null;
        OutputStream out = null;

        String saveFile = pdfFileName.substring(0, pdfFileName.length() - 4) + "_.pdf";
        try {
            stream = new FileInputStream(f);
            out = new FileOutputStream(saveFile);

            SecurityUtil encryptionUtils = new SecurityUtil();
            encryptionUtils.getKey();
            try {
                encryptionUtils.encrypt(stream, out);
            } catch (Throwable ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
            stream.close();
            out.close();
        } catch (FileNotFoundException fex) {
            throw fex;
        } catch (InvalidKeyException | NoSuchAlgorithmException 
                | InvalidKeySpecException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
        return saveFile;
    }

    public static String encrypt(String source, Key key) {
        try {
            // Get our secret key
            // Key key = getKey();

            // Create the cipher
            Cipher desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");

            // Initialize the cipher for encryption
            desCipher.init(Cipher.ENCRYPT_MODE, key);

            // Our cleartext as bytes
            byte[] cleartext = source.getBytes();

            // Encrypt the cleartext
            byte[] ciphertext = desCipher.doFinal(cleartext);

            // Return a String representation of the cipher text
            return getString(ciphertext);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    private static String getString(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            sb.append((int) (0x00FF & b));
            if (i + 1 < bytes.length) {
                sb.append("-");
            }
        }
        return sb.toString();
    }

    private void encrypt(InputStream is, OutputStream os) throws Throwable {
        encryptOrDecrypt(keyDes, Cipher.ENCRYPT_MODE, is, os);
    }

    private static void encryptOrDecrypt(SecretKey key, int mode, InputStream is, OutputStream os) throws Throwable {


        Cipher cipher = Cipher.getInstance("DES"); // DES/ECB/PKCS5Padding for SunJCE

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

    private static void doCopy(InputStream is, OutputStream os) throws IOException {
        byte[] bytes = new byte[1024 * 5];
        int numBytes;
        while ((numBytes = is.read(bytes)) != -1) {
            os.write(bytes, 0, numBytes);
        }
        os.flush();
        os.close();
        is.close();
    }

    public static String decryptFile(String pdfFileName, String userName) throws FileNotFoundException, FileNotFoundException, IOException, Exception {
        File f = new File(pdfFileName);
        InputStream stream = null;
        OutputStream out = null;

        String saveFile = f.getParent() + File.separator + userName + "_" + f.getName();
        try {

            stream = new FileInputStream(f);
            out = new FileOutputStream(saveFile);

            SecurityUtil encryptionUtils = new SecurityUtil();
            encryptionUtils.getKey();
            try {
                encryptionUtils.decrypt(stream, out);
            } catch (Throwable ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
            stream.close();
            out.close();
        } catch (FileNotFoundException | InvalidKeyException 
                | NoSuchAlgorithmException | InvalidKeySpecException fex) {
            throw fex;
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
        return saveFile;
    }

    private void decrypt(InputStream is, OutputStream os) throws Throwable {
        encryptOrDecrypt(keyDes, Cipher.DECRYPT_MODE, is, os);
    }   
}
