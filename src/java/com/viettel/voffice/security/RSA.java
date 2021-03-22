package com.viettel.voffice.security;

/**
 *
 * @author datnv5
 */
import com.viettel.voffice.utils.FileUtils;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.logging.Level;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.apache.log4j.Logger;

/**
 * RSA
 *
 * @author datnv5
 */
public class RSA {

    private PublicKey public_Key;
    private PrivateKey private_Key;

    public PrivateKey getPrivate_Key() {
        return private_Key;
    }

    public PublicKey getPublic_Key() {
        return public_Key;
    }

    public RSA() {
        try {
//            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA/NONE/OAEPWithSHA1AndMGF1Padding", "BC");
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048); //2048 used for normal securities
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            public_Key = keyPair.getPublic();
            private_Key = keyPair.getPrivate();
        } catch (NoSuchAlgorithmException ex) {
            LOGGER.error("Loi tao khoa he mat RSA", ex);
        }
//        } catch (NoSuchProviderException ex) {
//            java.util.logging.Logger.getLogger(RSA.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
    private static final Logger LOGGER = Logger.getLogger(RSA.class);

    /**
     * Encrypt Data
     *
     * @param data
     * @throws IOException
     */
    public static byte[] encryptData(String data, PublicKey keyPL) throws IOException {
        byte[] dataToEncrypt = data.getBytes();
        byte[] encryptedData = null;
        try {
            PublicKey pubKey = keyPL;
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            encryptedData = cipher.doFinal(dataToEncrypt);
        } catch (Exception e) {
            LOGGER.error("RSA: encryptData", e);
        }
        return encryptedData;
    }

    /**
     * Encrypt Data
     *
     * @param data
     * @param keyPr
     * @param isPaddingIos
     * @return
     * @throws IOException
     */
    public static String decryptData(byte[] data, PrivateKey keyPr,
            Boolean isPaddingIos) throws IOException {
        String strDecreate = null;
        byte[] descryptedData;
        try {
            PrivateKey privateKey = keyPr;
            Cipher cipher;
            if (isPaddingIos) {
                Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
                cipher = Cipher.getInstance("RSA/NONE/OAEPWithSHA1AndMGF1Padding", "BC");
            } else {
                cipher = Cipher.getInstance("RSA");
            }
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            descryptedData = cipher.doFinal(data);
            strDecreate = new String(descryptedData);

        } catch (NoSuchAlgorithmException | NoSuchProviderException
                | NoSuchPaddingException | InvalidKeyException
                | IllegalBlockSizeException | BadPaddingException e) {
            LOGGER.error("RSA: decryptData", e);
        }
        return strDecreate;
    }

    /**
     * <b>Chuyen mang byte thanh PublicKey</b><br>
     *
     * @param publicKeyBytes
     * @return
     */
    public static PublicKey convertBytesToPublicKey(byte[] publicKeyBytes) {
        PublicKey publicKey = null;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException ex) {
            LOGGER.error("RSA: convertByteToPublicKey", ex);
        }
        return publicKey;
    }

    /**
     * ham thuc hien: convertByteToPrivateKey
     *
     * @param privateKeyBytes
     * @return
     */
    public static PrivateKey convertByteToPrivateKey(byte[] privateKeyBytes) {
        PrivateKey privateKey = null;
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException ex) {
            LOGGER.error("convertByteToPrivateKey", ex);
        }
        return privateKey;
    }

    /**
     * <b>Giai ma du lieu dua vao khoa bi mat</b><br>
     *
     * @param privateKey Khoa bi mat
     * @param encryptedDataBytes Mang Byte du lieu da ma hoa bang khoa cong khai
     * @return
     */
//    public static String decrypt(PrivateKey privateKey, byte[] encryptedDataBytes) {
//        String decryptedData = null;
//        try {
//            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
//            Cipher cipher = Cipher.getInstance("RSA/NONE/OAEPWithSHA1AndMGF1Padding", "BC");
//            cipher.init(Cipher.DECRYPT_MODE, privateKey);
//            byte[] decryptedDataBytes = cipher.doFinal(encryptedDataBytes);
//            decryptedData = new String(decryptedDataBytes);
//        } catch (Exception ex) {
//            LOGGER.error("decrypt", ex);
//        }
//        return decryptedData;
//    }
    /**
     * <b>Load PublicKey tu file der</b>
     *
     * @param derFile
     * @return
     */
    public static PublicKey loadPublicKeyFromDerFile(String derFile) throws IOException {
        return convertBytesToPublicKey(FileUtils.loadFile(derFile));
    }

}
