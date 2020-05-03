package main.java.serverchat;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

/**
 * AES
 * A class which focuses on abstracting AES encryption/decryption functions.
 * @author Eric Van
 * @version 1
 * @since 1.0-SNAPSHOT
 */
public class AES {

    private SecretKey secretKey;

    /**
     * Creates an AES object that holds the given key as a private key
     * @param key The key represented by a string
     */
    public AES(String key)
    {
        try
        {
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            byte[] keyBytes = sha.digest(key.getBytes());
            keyBytes = Arrays.copyOf(keyBytes, 16); // use only first 128 bit
            secretKey = new SecretKeySpec(keyBytes, "AES");
        } catch(NoSuchAlgorithmException e) {}
    }

    /**
     * Encrypts a given message with the secret key.
     * @param message The message to encrypt
     * @return The message in a base64 encrypted format
     */
    public String encrypt(String message) {
        byte[] encryptedMessage;

        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            encryptedMessage = cipher.doFinal(message.getBytes());
        } catch (Exception e) {
            System.out.println("Ran into issues encrypting via AES.");
            return null;
        }

        return Base64.getEncoder().encodeToString(encryptedMessage);
    }

    /**
     * Decrypts a given message with the secret key
     * @param message The message to decrypt
     * @return The message in plaintext
     */
    public String decrypt(String message) {
        byte[] decryptedMessage;

        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            decryptedMessage = cipher.doFinal(Base64.getDecoder().decode(message));
        } catch (Exception e) {
            System.out.println("Ran into issues decrypting via AES.");
            return null;
        }

        return new String(decryptedMessage);
    }

}
