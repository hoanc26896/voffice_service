package com.viettel.voffice.security;

import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.constants.StringConstants;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.Security;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class AES {

    private static final Logger LOGGER = Logger.getLogger(AES.class);

    private enum EncryptMode {

        ENCRYPT, DECRYPT;
    }
    Cipher _cx;
    static byte[] _key = new byte[16];
    static byte[] _iv = new byte[16];

    public AES() throws NoSuchAlgorithmException, NoSuchPaddingException {
        _cx = Cipher.getInstance("AES/CBC/PKCS5Padding");
        Security.addProvider(new BouncyCastleProvider());
//        _key ; //256 bit key space
//        _iv = ; //128 bit IV
    }

    public static final String md5(final String inputString) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance(MD5);
            digest.update(inputString.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2) {
                    h = "0" + h;
                }
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Sai thuat toan ma hoa md5:", e);
        }
        return "";
    }

    /**
     * thuc hien ma hoa du lieu dau vao
     *
     * @param _inputText
     * @param _encryptionKey
     * @param _mode
     * @param _initVector
     * @return
     * @throws UnsupportedEncodingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    private String encryptDecrypt(String _inputText, String _encryptionKey,
            EncryptMode _mode, String _initVector) throws UnsupportedEncodingException,
            InvalidKeyException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException {
        String _out = "";
        int len = _encryptionKey.getBytes("UTF-8").length; // length of the key	provided

        if (_encryptionKey.getBytes("UTF-8").length > _key.length) {
            len = _key.length;
        }

        int ivlen = _initVector.getBytes("UTF-8").length;

        if (_initVector.getBytes("UTF-8").length > _iv.length) {
            ivlen = _iv.length;
        }

        System.arraycopy(_encryptionKey.getBytes("UTF-8"), 0, _key, 0, len);
        System.arraycopy(_initVector.getBytes("UTF-8"), 0, _iv, 0, ivlen);
        //KeyGenerator _keyGen = KeyGenerator.getInstance("AES");
        //_keyGen.init(128);

        SecretKeySpec keySpec = new SecretKeySpec(_key, "AES"); // Create a new SecretKeySpec
        // for the
        // specified key
        // data and
        // algorithm
        // name.

        IvParameterSpec ivSpec = new IvParameterSpec(_iv); // Create a new
        // IvParameterSpec
        // instance with the
        // bytes from the
        // specified buffer
        // iv used as
        // initialization
        // vector.

        // encryption
        if (_mode.equals(EncryptMode.ENCRYPT)) {
            // Potentially insecure random numbers on Android 4.3 and older.
            // Read
            // https://android-developers.blogspot.com/2013/08/some-securerandom-thoughts.html
            // for more info.
            _cx.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);// Initialize this cipher instance
            byte[] results = _cx.doFinal(_inputText.getBytes("UTF-8")); // Finish
            // multi-part
            // transformation
            // (encryption)
            _out = FunctionCommon.bytesToHex(results); // ciphertext
            // output
        }

        // decryption
        if (_mode.equals(EncryptMode.DECRYPT)) {
            _cx.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);// Initialize this ipher instance

            byte[] decodedValue = padInputToMultipleOf8(FunctionCommon.hexStringToByteArray(_inputText));

            byte[] decryptedVal = _cx.doFinal(decodedValue); // Finish
            // multi-part
            // transformation
            // (decryption)
            _out = new String(decryptedVal, "UTF-8");
        }
        return _out; // return encrypted/decrypted string
    }

    /**
     * *
     * This function computes the SHA256 hash of input string
     *
     * @param text input text whose SHA256 hash has to be computed
     * @param length length of the text to be returned
     * @return returns SHA256 hash of input text
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public static String SHA256(String text, int length) throws NoSuchAlgorithmException, UnsupportedEncodingException {

        String resultStr;
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        md.update(text.getBytes("UTF-8"));
        byte[] digest = md.digest();

        StringBuilder result = new StringBuilder();
        for (byte b : digest) {
            result.append(String.format("%02x", b)); //convert to hex
        }
        //return result.toString();

        if (length > result.toString().length()) {
            resultStr = result.toString();
        } else {
            resultStr = result.toString().substring(0, length);
        }

        return resultStr;

    }

    /**
     * *
     * This function encrypts the plain text to cipher text using the key
     * provided. You'll have to use the same key for decryption
     *
     * @param _plainText Plain text to be encrypted
     * @param _key Encryption Key. You'll have to use the same key for
     * decryption
     * @param _iv initialization Vector
     * @return returns encrypted (cipher) text
     * @throws InvalidKeyException
     * @throws UnsupportedEncodingException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public String encrypt(String _plainText, String _key, String _iv)
            throws InvalidKeyException, UnsupportedEncodingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException,
            BadPaddingException {
        return encryptDecrypt(_plainText, _key, EncryptMode.ENCRYPT, _iv);
    }

    /**
     * *
     * This funtion decrypts the encrypted text to plain text using the key
     * provided. You'll have to use the same key which you used during
     * encryprtion
     *
     * @param _encryptedText Encrypted/Cipher text to be decrypted
     * @param _key Encryption key which you used during encryption
     * @param _iv initialization Vector
     * @return encrypted value
     * @throws InvalidKeyException
     * @throws UnsupportedEncodingException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public String decrypt(String _encryptedText, String _key, String _iv)
            throws InvalidKeyException, UnsupportedEncodingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException,
            BadPaddingException {
        return encryptDecrypt(_encryptedText, _key, EncryptMode.DECRYPT, _iv);
    }

    /**
     * this function generates random string for given length
     *
     * @param length Desired length
     * * @return
     */
    public static String generateRandomIV(int length) {
        SecureRandom ranGen = new SecureRandom();
        byte[] aesKey = new byte[16];
        ranGen.nextBytes(aesKey);
        StringBuilder result = new StringBuilder();
        for (byte b : aesKey) {
            result.append(String.format("%02x", b)); //convert to hex
        }
        if (length > result.toString().length()) {
            return result.toString();
        } else {
            return result.toString().substring(0, length);
        }
    }

    /**
     * ma hoa file dinh kem
     *
     * @param in
     * @param out
     * @param aesKey
     * @param viKey
     * @return
     */
    public void encryptFile(InputStream in, OutputStream out, String aesKey, String viKey)
            throws InvalidKeyException, UnsupportedEncodingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException,
            BadPaddingException, IOException, NoSuchAlgorithmException, NoSuchPaddingException {

        int ivlen = viKey.getBytes("UTF-8").length;
        if (viKey.getBytes("UTF-8").length > _iv.length) {
            ivlen = _iv.length;
        }
        System.arraycopy(viKey.getBytes("UTF-8"), 0, _iv, 0, ivlen);

        int len = aesKey.getBytes("UTF-8").length;
        if (len > _key.length) {
            len = _key.length;
        }
        System.arraycopy(aesKey.getBytes("UTF-8"), 0, _key, 0, len);
        SecretKeySpec keySpec = new SecretKeySpec(_key, "AES"); // Create a new SecretKeySpec
        IvParameterSpec ivSpec = new IvParameterSpec(_iv);
        out.flush();
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        out = new CipherOutputStream(out, cipher);
        byte[] buf = new byte[1024];
        int numRead;
        while ((numRead = in.read(buf)) >= 0) {
            out.write(buf, 0, numRead);
        }
        out.close();
    }

    /**
     * giai ma file
     *
     * @param in
     * @param out
     * @param aesKey
     * @param viKey
     * @throws InvalidKeyException
     * @throws UnsupportedEncodingException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     */
    public static void decryptFile(InputStream in, OutputStream out, String aesKey, String viKey)
            throws InvalidKeyException, UnsupportedEncodingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException,
            BadPaddingException, IOException, NoSuchAlgorithmException, NoSuchPaddingException {
        int ivlen = viKey.getBytes("UTF-8").length;
        if (viKey.getBytes("UTF-8").length > _iv.length) {
            ivlen = _iv.length;
        }
        System.arraycopy(viKey.getBytes("UTF-8"), 0, _iv, 0, ivlen);
        int len = aesKey.getBytes("UTF-8").length;
        if (len > _key.length) {
            len = _key.length;
        }
        System.arraycopy(aesKey.getBytes("UTF-8"), 0, _key, 0, len);
        SecretKeySpec keySpec = new SecretKeySpec(_key, "AES"); // Create a new SecretKeySpec
        IvParameterSpec ivSpec = new IvParameterSpec(_iv);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); //"DES/ECB/PKCS5Padding";"AES/CBC/PKCS5Padding"
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        in = new CipherInputStream(in, cipher);
        byte[] buf = new byte[1024];
        int numRead;
        while ((numRead = in.read(buf)) >= 0) {
            out.write(buf, 0, numRead);
        }
        out.close();
    }

    /**
     * Tao key random
     *
     * @return
     */
    public static String createAesKey() {
        String result = "";
        try {
            String key = AES.SHA256(FunctionCommon.createTokenRandom(), 16);
            String iv = AES.generateRandomIV(16);
            result = key + StringConstants.STR_VIAESKEYSPACE + iv;
        } catch (NoSuchAlgorithmException e) {
            result = null;
            LOGGER.error("Loi trong qua trinh tao key", e);
        } catch (UnsupportedEncodingException e) {
            result = null;
            LOGGER.error("Loi trong qua trinh tao key", e);
        }

        return result;
    }

    byte[] padInputToMultipleOf8(byte[] inputBytes) {
        int size = (inputBytes.length / 8 ) + ( (inputBytes.length % 8 == 0) ? 0 : 1 ) ;
        int newLength = size * 8;
        int lengthOfPad = newLength - inputBytes.length;
        
        // set the pad character according to PKCS5Padding algorithm
        byte padByte = 0;
        switch (lengthOfPad) {
            case 7 : padByte = (byte) 0x07; break;
            case 6 : padByte = (byte) 0x06; break;
            case 5 : padByte = (byte) 0x05; break;
            case 4 : padByte = (byte) 0x04; break;         
            case 3 : padByte = (byte) 0x03; break;        
            case 2 : padByte = (byte) 0x02; break;      
            case 1 : padByte = (byte) 0x01; break;               
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(newLength);
        byteBuffer.put(inputBytes);
        byte[] paddedUp = new byte[size * 8];
        
        for (int j=0;j<inputBytes.length;j++) {
            paddedUp[j]=byteBuffer.get(j);
        }
        for (int k=inputBytes.length;k<paddedUp.length;k++) {
            paddedUp[k]=padByte;
        }        
        return paddedUp;
    }

}
