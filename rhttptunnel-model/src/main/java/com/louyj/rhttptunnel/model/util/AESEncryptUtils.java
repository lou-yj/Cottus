package com.louyj.rhttptunnel.model.util;

import static com.google.common.base.Charsets.UTF_8;

import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;

/**
 * Created on 2018年6月7日
 *
 * @author Louyj
 */
public class AESEncryptUtils {

	static final String KEY_ALGORITHM = "AES";
	static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
	public static final String defaultKey = "^&*() {}:LJHGFFTUIHFUT";

	public static Key toKey(byte[] key) throws Exception {
		SecretKey secretKey = new SecretKeySpec(key, KEY_ALGORITHM);
		return secretKey;
	}

	public static String md5With16bit(String content) throws NoSuchAlgorithmException {
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		byte[] bs = md5.digest(content.getBytes(UTF_8));
		String hex = Hex.encodeHexString(bs);
		return hex.substring(8, 24);
	}

	public static byte[] encrypt(byte[] content, String key) throws Exception {
		if (content == null) {
			return null;
		}
		key = md5With16bit(key);
		byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		IvParameterSpec ivspec = new IvParameterSpec(iv);
		Key k = toKey(key.getBytes());
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, k, ivspec);
		return cipher.doFinal(content);
	}

	public static byte[] decrypt(byte[] content, String key) throws Exception {
		if (key == null) {
			throw new IllegalArgumentException("Key is empty");
		}
		if (content == null) {
			return null;
		}
		key = md5With16bit(key);
		byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		IvParameterSpec ivspec = new IvParameterSpec(iv);
		Key k = toKey(key.getBytes());
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, k, ivspec);
		return cipher.doFinal(content);
	}

}
