package com.louyj.rhttptunnel.model.util;

import static com.google.common.base.Charsets.UTF_8;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;

import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

/**
 * Created on 2018年6月7日
 *
 * @author Louyj
 */
public class AESEncryptUtils {

	static final String KEY_ALGORITHM = "AES";
	static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

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

	public static String encrypt(String content, String key) throws Exception {
		if (content == null) {
			return null;
		}
		key = md5With16bit(key);
		byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		IvParameterSpec ivspec = new IvParameterSpec(iv);
		Key k = toKey(key.getBytes());
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, k, ivspec);
		return encodeBase64String(cipher.doFinal(content.getBytes(UTF_8)));
	}

	public static String decrypt(String content, String key) throws Exception {
		if (content == null) {
			return null;
		}
		key = md5With16bit(key);
		byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		IvParameterSpec ivspec = new IvParameterSpec(iv);
		Key k = toKey(key.getBytes());
		byte[] bs = Base64.decodeBase64(content);
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, k, ivspec);
		return new String(cipher.doFinal(bs), UTF_8);
	}

}
