package com.cocode.notifyx.storage;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.nio.ByteBuffer;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * AndroidKeyStore + AES/GCM helper.
 */
public final class CryptoManager {
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "popup_sdk_master_key_v1";
    private CryptoManager() {
    }

    public static byte[] encrypt(byte[] plain) throws Exception {
        SecretKey key = getOrCreateKey();
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] iv = cipher.getIV();
        byte[] ct = cipher.doFinal(plain);
        ByteBuffer bb = ByteBuffer.allocate(4 + iv.length + ct.length);
        bb.putInt(iv.length);
        bb.put(iv);
        bb.put(ct);
        return bb.array();
    }

    public static byte[] decrypt(byte[] data) throws Exception {
        ByteBuffer bb = ByteBuffer.wrap(data);
        int ivLen = bb.getInt();
        if (ivLen <= 0 || ivLen > 32) throw new IllegalArgumentException("Invalid IV");
        byte[] iv = new byte[ivLen];
        bb.get(iv);
        byte[] ct = new byte[bb.remaining()];
        bb.get(ct);
        SecretKey key = getOrCreateKey();
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        return cipher.doFinal(ct);
    }

    private static SecretKey getOrCreateKey() throws Exception {
        KeyStore ks = KeyStore.getInstance(ANDROID_KEYSTORE);
        ks.load(null);
        if (ks.containsAlias(KEY_ALIAS)) {
            KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) ks.getEntry(KEY_ALIAS, null);
            return entry.getSecretKey();
        }
        KeyGenerator kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
        KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setUserAuthenticationRequired(false)
                .build();
        kg.init(spec);
        return kg.generateKey();
    }

    // Helper base64
    public static String toBase64(byte[] b) {
        return Base64.encodeToString(b, Base64.NO_WRAP);
    }

    public static byte[] fromBase64(String s) {
        return Base64.decode(s, Base64.NO_WRAP);
    }
}
