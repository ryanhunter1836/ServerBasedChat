package main.java.serverchat;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * Secret Key Generator
 * A class that generates a random, secret key and hashes it
 * @version 1
 * @since 1.0-SNAPSHOT
 */
public class SecretKeyGenerator {

	/**
	 * Generates a random private key
	 * @return
	 */
	public static SecretKey generateKey() {
		KeyGenerator keyGenerator = null; // Key generator to get client secret key
		
		try {
			keyGenerator = KeyGenerator.getInstance("AES"); // Using AES algorithm to start key generation
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		keyGenerator.init(256); // intialize key generator at 256 bytes
		
		SecretKey key = keyGenerator.generateKey(); // generate key
		
		return key;
	}

	/**
	 * Function to convert SecretKey to String
	 * @param key
	 * @return
	 */
	public static String keyToString(SecretKey key) {
		byte encoded[] = key.getEncoded(); // get byte array from SecretKey
		
		String encodedKey = Base64.getEncoder().encodeToString(encoded); // Converting from byte array to String
		
		return encodedKey;
	}

	/**
	 * A3: Hashing function from key
	 * @param key
	 * @return
	 */
	public static String hash1(String key)
	{
		String res = MD5.getMD5(key);

		return res;
	}

	/**
	 * A8: Hashing function from key
	 * @param encryptionKey
	 * @return
	 */
	public static String hash2(String encryptionKey)
	{
		String res;
		try {
			byte[] hash = SHA256.getSHA(encryptionKey);
			res = SHA256.hexToString(hash);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}

		return res;
	}
}
