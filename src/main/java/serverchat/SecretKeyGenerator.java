package main.java.serverchat;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class SecretKeyGenerator {

	public static SecretKey generateKey() {
		KeyGenerator keyGenerator = null; // Key generator to get client secret key
		
		try {
			// Debating using DES instead of AES for simplicity
			keyGenerator = KeyGenerator.getInstance("AES"); // Using AES algorithm to start key generation
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		keyGenerator.init(256); // intialize key generator at 256 bytes
		
		SecretKey key = keyGenerator.generateKey(); // generate key
		
		return key;
	}
	
	// Function to convert SecretKey to String
	public static String keyToString(SecretKey key) {
		byte encoded[] = key.getEncoded(); // get byte array from SecretKey
		
		String encodedKey = Base64.getEncoder().encodeToString(encoded); // Converting from byte array to String
		
		return encodedKey;
	}

	// A3: Hashing function from key
	public static String hash1(String key)
	{
		// super basic implementation, will probably change to account for collision
		MD5 m = new MD5();
		String res = m.getMD5(key);

		return res;
	}
}
