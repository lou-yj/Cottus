package com.louyj.rhttptunnel.model.util;

import static com.google.common.base.Charsets.UTF_8;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.louyj.rhttptunnel.model.bean.Pair;

/**
 *
 * Create at 2020年7月22日
 *
 * @author Louyj
 *
 */
public class RsaUtils {

	static final Logger logger = LoggerFactory.getLogger(RsaUtils.class);

	private static final String ALGORITHM = "SHA256WITHRSA";

	public static final String CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding";

	public static Pair<Key, Key> genKeyPair() throws NoSuchAlgorithmException {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(2048);
		KeyPair kp = keyGen.genKeyPair();
		Key pub = kp.getPublic();
		Key pvt = kp.getPrivate();
		return Pair.of(pvt, pub);
	}

	public static Pair<String, String> stringKeyPair(Pair<Key, Key> pair) {
		return Pair.of(Base64.encodeBase64String(pair.getLeft().getEncoded()),
				Base64.encodeBase64String(pair.getRight().getEncoded()));
	}

	public static String sign(String body, String privateKey) throws GeneralSecurityException {
		byte[] bodyBytes = body.getBytes(UTF_8);
		byte[] privateKeyBytes = Base64.decodeBase64(privateKey);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PrivateKey key = keyFactory.generatePrivate(keySpec);

		Signature signatureChecker = Signature.getInstance(ALGORITHM);
		signatureChecker.initSign(key);
		signatureChecker.update(bodyBytes);
		return new String(signatureChecker.sign());
	}

	public static boolean verify(final String body, final String signature, final String publicKey) {
		try {
			byte[] bodyBytes = body.getBytes(UTF_8);
			byte[] signatureBytes = Base64.decodeBase64(signature);
			byte[] publicKeyBytes = Base64.decodeBase64(publicKey);
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PublicKey key = keyFactory.generatePublic(keySpec);
			Signature signatureChecker = Signature.getInstance(ALGORITHM);
			signatureChecker.initVerify(key);
			signatureChecker.update(bodyBytes);
			return signatureChecker.verify(signatureBytes);
		} catch (Exception e) {
			logger.error("", e);
			return false;
		}
	}

	public static byte[] encrypt(String data, Key publicKey) throws Exception {
		Cipher encrypt = Cipher.getInstance(CIPHER_ALGORITHM);
		encrypt.init(Cipher.ENCRYPT_MODE, publicKey);
		return encrypt.doFinal(data.getBytes(StandardCharsets.UTF_8));
	}

	public static String decrypt(byte[] bs, Key privateKey) throws Exception {
		Cipher decrypt = Cipher.getInstance(CIPHER_ALGORITHM);
		decrypt.init(Cipher.DECRYPT_MODE, privateKey);
		return new String(decrypt.doFinal(bs), StandardCharsets.UTF_8);
	}

	public static String baseEncode(byte[] bs) {
		return Base64.encodeBase64String(bs);
	}

	public static byte[] base64Decode(String data) {
		return Base64.decodeBase64(data);
	}

	public static void main(String[] args) throws Exception {
		Pair<Key, Key> pair = genKeyPair();
		Key privateKey = pair.getLeft();
		Key publicKey = pair.getRight();
		String msg = "xxx";
		byte[] encrypt2 = encrypt(msg, publicKey);
		String decrypt = decrypt(encrypt2, privateKey);
		System.out.println(decrypt);
	}

}
