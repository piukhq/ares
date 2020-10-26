package com.bink.sdk.util;

import android.os.Build;
import android.util.Base64;

import com.bink.sdk.config.SessionConfig;

import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static com.bink.wallet.utils.ExtensionsKt.logDebug;
import static com.bink.wallet.utils.ExtensionsKt.logError;

/**
 * Bink
 * <p>
 * Created by kkulendiran on 24/08/2017.
 */
@SuppressWarnings("deprecation")
public class BinkSecurityUtil {
    private String TAG = getClass().getSimpleName();

    private static final String KEY_ALIAS = "BINKAndroidKeyStore";
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String CHAR_SET = "UTF-8";
    private static final String AES_MODE_KITKAT = "AES/GCM/NoPadding";
    private static final String AES_MODE = "AES/ECB/PKCS7Padding";
    private static final String RSA_MODE = "RSA/ECB/PKCS1Padding";
    private static final byte[] FIXED_IV = new byte[]{0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1a, 0x1b};

    private static BinkSecurityUtil instance;
    private Cipher encryptCipher;
    private Cipher decryptCipher;
    private Key secretKey;

    public static void create(SessionConfig sessionConfig) {
        if (instance == null) {
            instance = new BinkSecurityUtil();
        }

        instance.createSecretKey(sessionConfig);
    }

    private static BinkSecurityUtil getInstance() {
        return instance;
    }

    private BinkSecurityUtil() {
    }

    private void createSecretKey(SessionConfig sessionConfig) {
        if (secretKey == null) {
            try {
                secretKey = getSecretKey(sessionConfig);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    encryptCipher = Cipher.getInstance(AES_MODE_KITKAT);
                    decryptCipher = Cipher.getInstance(AES_MODE_KITKAT);
                } else {
                    encryptCipher = Cipher.getInstance(AES_MODE);
                    decryptCipher = Cipher.getInstance(AES_MODE);
                }
            } catch (Exception e) {
                logError(TAG, e.getMessage(), e);
            }
        }
    }

    private Key getSecretKey(SessionConfig sessionConfig) {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                SecureRandom random = new SecureRandom();
                RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4);
                KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
                kpg.initialize(spec, random);
                KeyPair kp = kpg.generateKeyPair();
                PublicKey publicKey = kp.getPublic();
                PrivateKey privateKey = kp.getPrivate();
                Certificate[] outChain = {createCertificate("CN=CA", publicKey, privateKey), createCertificate("CN=Client", publicKey, privateKey)};
                keyStore.setKeyEntry(KEY_ALIAS, privateKey, null, outChain);
            }

            String encryptedKeyB64 = sessionConfig.getEncryptedKey();
            if (encryptedKeyB64 == null) {
                byte[] key = new byte[16];
                SecureRandom secureRandom = new SecureRandom();
                secureRandom.nextBytes(key);
                byte[] encryptedKey = rsaEncrypt(key, keyStore);
                encryptedKeyB64 = new String(Base64.encode(encryptedKey, Base64.DEFAULT), CHAR_SET);
                sessionConfig.setEncryptedKey(encryptedKeyB64);
            }

            byte[] key = rsaDecrypt(Base64.decode(encryptedKeyB64, Base64.DEFAULT), keyStore);
            return new SecretKeySpec(key, "AES");
        } catch (Exception e) {
            logError(TAG, e.getMessage(), e);
        }
        return null;
    }

    private X509Certificate createCertificate(String dn, PublicKey publicKey, PrivateKey privateKey) throws Exception {
        X509V3CertificateGenerator certGenerator = new X509V3CertificateGenerator();
        certGenerator.setSerialNumber(BigInteger.ONE);
        certGenerator.setIssuerDN(new X509Name(dn));
        certGenerator.setSubjectDN(new X509Name(dn));
        certGenerator.setNotBefore(Calendar.getInstance().getTime());
        certGenerator.setNotAfter(Calendar.getInstance().getTime());
        certGenerator.setPublicKey(publicKey);
        certGenerator.setSignatureAlgorithm("SHA1withRSA");
        return certGenerator.generate(privateKey);
    }

    private byte[] rsaEncrypt(byte[] secret, KeyStore keyStore) throws Exception {
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(KEY_ALIAS, null);
        Cipher inputCipher = Cipher.getInstance(RSA_MODE);
        inputCipher.init(Cipher.ENCRYPT_MODE, privateKeyEntry.getCertificate().getPublicKey());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, inputCipher);
        cipherOutputStream.write(secret);
        cipherOutputStream.close();

        return outputStream.toByteArray();
    }

    private byte[] rsaDecrypt(byte[] encrypted, KeyStore keyStore) throws Exception {
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(KEY_ALIAS, null);
        Cipher output = Cipher.getInstance(RSA_MODE);
        output.init(Cipher.DECRYPT_MODE, privateKeyEntry.getPrivateKey());
        CipherInputStream cipherInputStream = new CipherInputStream(new ByteArrayInputStream(encrypted), output);
        ArrayList<Byte> values = new ArrayList<>();
        int nextByte;
        while ((nextByte = cipherInputStream.read()) != -1) {
            values.add((byte) nextByte);
        }

        byte[] bytes = new byte[values.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = values.get(i);
        }

        return bytes;
    }

    private String encryption(String input) {
        if (input == null || secretKey == null || encryptCipher == null) {
            return input;
        } else {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(128, FIXED_IV));
                } else {
                    encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey);
                }
                byte[] inputByte = input.getBytes(CHAR_SET);
                return new String(Base64.encode(encryptCipher.doFinal(inputByte), Base64.DEFAULT), CHAR_SET);
            } catch (Exception e) {
                logError(TAG, e.getMessage(), e);
            }

            return input;
        }
    }

    private String decryption(String encrypted) {
        if (encrypted == null || secretKey == null || decryptCipher == null) {
            return encrypted;
        } else {
            try {
                decryptCipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(128, FIXED_IV));
                byte[] inputByte = encrypted.getBytes(CHAR_SET);
                return new String(decryptCipher.doFinal(Base64.decode(inputByte, Base64.DEFAULT)), CHAR_SET);
            } catch (Exception e) {
                logError(TAG, e.getMessage(), e);
            }

            return encrypted;
        }
    }

    private void removeKey() {
        try {
            if (keyExists(KEY_ALIAS)) {
                KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
                keyStore.load(null);
                keyStore.deleteEntry(KEY_ALIAS);
                secretKey = null;
            }
        } catch (Exception e) {
            logError(TAG, e.getMessage(), e);
        }
    }

    private boolean keyExists(String keyName) {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            Enumeration<String> aliases = keyStore.aliases();

            while (aliases.hasMoreElements()) {
                if (keyName.equals(aliases.nextElement())) {
                    return true;
                }
            }
        } catch (Exception e) {
            logError(TAG, e.getMessage(), e);
        }

        return false;
    }

    public static String encrypt(String input) {
        if (BinkSecurityUtil.getInstance() != null) {
            return BinkSecurityUtil.getInstance().encryption(input);
        } else {
            logDebug("BinkSecurityUtil.class", "This class has given a null instance.");
            return input;
        }
    }

    public static String decrypt(String encrypted) {
        if (BinkSecurityUtil.getInstance() != null) {
            return BinkSecurityUtil.getInstance().decryption(encrypted);
        } else {
            logDebug("BinkSecurityUtil.class", "This class has given a null instance.");
            return encrypted;
        }
    }

    public static void clear() {
        BinkSecurityUtil.getInstance().removeKey();
    }
}