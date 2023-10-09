package hk.zdl.crypto.pearlet.lock;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;

import hk.zdl.crypto.pearlet.util.Util;

public class LockImpl {
	private static final String WALLET_LOCK_DATA = "WALLET_LOCK_DATA", IV = "WALLET_LOCK_IV";
	private static final String MY_SALT = "WALLET_LOCK_MY_SALT", MD5_HASH = "MD5_HASH", SHA_512_HASH = "SHA_512_HASH", AES_256 = "AES_256";
	private char[] tmp_pw = null;

	static {
		var p = Util.getUserSettings();
		synchronized (LockImpl.class) {
			var iv = p.getByteArray(IV, null);
			var salt = p.getByteArray(MY_SALT, null);
			if (iv == null || salt == null) {
				try {
					iv = new byte[16];
					salt = new byte[32];
					var r = new Random();
					r.nextBytes(iv);
					r.nextBytes(salt);
					p.putByteArray(IV, iv);
					p.putByteArray(MY_SALT, salt);
					p.flush();
				} catch (Exception e) {
				}
			}
		}
	}

	static final boolean hasPassword() {
		var s = Util.getUserSettings().get(WALLET_LOCK_DATA, null);
		if (s == null || s.isBlank()) {
			return false;
		}
		try {
			new JSONObject(s);
		} catch (Throwable t) {
			return false;
		}
		return true;
	}

	static boolean validete_password(char[] password) {
		var s = Util.getUserSettings().get(WALLET_LOCK_DATA, null);
		if (s == null || s.isBlank()) {
			return false;
		}
		try {
			var jobj = new JSONObject(s);
			var enc = Base64.getEncoder();
			var barr = Charset.defaultCharset().encode(CharBuffer.wrap(password)).array();
			var md5_hash = enc.encodeToString(MessageDigest.getInstance("MD5").digest(barr));
			if (!md5_hash.equals(jobj.getString(MD5_HASH))) {
				return false;
			}
			var sha_hash = enc.encodeToString(MessageDigest.getInstance("SHA-256").digest(barr));
			if (!sha_hash.equals(jobj.getString(SHA_512_HASH))) {
				return false;
			}
			var aes_hash = enc.encodeToString(aes_encrypt(password, barr));
			if (!aes_hash.equals(jobj.getString(AES_256))) {
				return false;
			}
		} catch (Exception x) {
			return false;
		}
		return true;
	}

	static boolean change_password(char[] old_pw, char[] new_pw) throws Exception {
		var enc = Base64.getEncoder();
		var barr = Charset.defaultCharset().encode(CharBuffer.wrap(new_pw)).array();
		var md5_hash = enc.encodeToString(MessageDigest.getInstance("MD5").digest(barr));
		var sha_hash = enc.encodeToString(MessageDigest.getInstance("SHA-256").digest(barr));
		var aes_hash = enc.encodeToString(aes_encrypt(new_pw, barr));
		var jobj = new JSONObject();
		jobj.put(MD5_HASH, md5_hash);
		jobj.put(SHA_512_HASH, sha_hash);
		jobj.put(AES_256, aes_hash);
		var p = Util.getUserSettings();
		p.put(WALLET_LOCK_DATA, jobj.toString());
		return true;
	}

	private static final byte[] aes_encrypt(char[] pw, byte[] input) throws Exception {
		var p = Util.getUserSettings();
		var iv = p.getByteArray(IV, null);
		var salt = p.getByteArray(MY_SALT, null);
		var ivspec = new IvParameterSpec(iv);
		var factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		var spec = new PBEKeySpec(pw, salt, 65536, 256);
		var secretKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
		var cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
		return cipher.doFinal(input);
	}

	private static final byte[] aes_decrypt(char[] pw, byte[] input) throws Exception {
		var p = Util.getUserSettings();
		var iv = p.getByteArray(IV, null);
		var salt = p.getByteArray(MY_SALT, null);
		var ivspec = new IvParameterSpec(iv);
		var factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		var spec = new PBEKeySpec(pw, salt, 65536, 256);
		var secretKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
		var cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
		return cipher.doFinal(input);
	}
}
