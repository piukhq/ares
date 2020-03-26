package com.bink.sdk.util;

import android.content.Context;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.room.util.FileUtil;

import com.bink.sdk.config.SessionConfig;
import com.bink.wallet.R;

import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
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
    private static final String RSA_MODE = "RSA/ECB/OAEPPADDING";
    private static final byte[] FIXED_IV = new byte[]{0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1a, 0x1b};

    private static BinkSecurityUtil instance;
    private Cipher encryptCipher;
    private Cipher decryptCipher;
    private Key secretKey;
    private  Context context;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void create(SessionConfig sessionConfig, Context context) {
        if (instance == null) {
            instance = new BinkSecurityUtil();
        }

        instance.createSecretKey(sessionConfig, context);
    }

    private static BinkSecurityUtil getInstance() {
        return instance;
    }

    private BinkSecurityUtil() {
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createSecretKey(SessionConfig sessionConfig, Context context) {
        this.context = context;
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Key getSecretKey(SessionConfig sessionConfig) {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                Log.e("ConnorDebug","using my key");
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

//            String encryptedKeyB64 =
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

    public static String getEncryptedMessage(Context context, String messageToEncrypt, String publicKeyString) {

        /**
         * You can't use Cipher.getInstance("RSA") in Android
         * See https://stackoverflow.com/a/31401015/3405101
         */
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        PublicKey publicKey = null;
        try {
            publicKey = loadPublicKey(context, publicKeyString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        byte[] bytes = new byte[0];
        try {
            bytes = cipher.doFinal(messageToEncrypt.getBytes());
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        byte[] androidEncode = Base64.encode(bytes, Base64.NO_WRAP);
        return new String(androidEncode, StandardCharsets.UTF_8);
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

//    @RequiresApi(api = Build.VERSION_CODES.O)
//    public static PublicKey get(String filename)
//            throws Exception {
//        String lol="dev_public_key.der";
//        Path pathToFile = Paths.get(lol);
//        Log.e("ConnorDebug", "pathToFile: " + pathToFile.toAbsolutePath());
////        URL path = getResource("myFile.txt");
//        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));
//
//        X509EncodedKeySpec spec =
//                new X509EncodedKeySpec(keyBytes);
//        KeyFactory kf = KeyFactory.getInstance("RSA");
//        return kf.generatePublic(spec);
//    }

    private static PublicKey loadPublicKey(Context context, String publicKeyString) throws Exception {
//        // Loading Public key from resources Raw folder
//        InputStream resourceAsStream = context.getResources().openRawResource(R.raw.dev_public_key);
//        byte[] keyBytes = toByteArray(resourceAsStream);
//
//        X509EncodedKeySpec spec =
//                new X509EncodedKeySpec(keyBytes);
//        KeyFactory kf = KeyFactory.getInstance("RSA");
//        return kf.generatePublic(spec);


            try{
                byte[] byteKey = Base64.decode(publicKeyString.getBytes(), Base64.DEFAULT);
                X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
                KeyFactory kf = KeyFactory.getInstance("RSA");

                return kf.generatePublic(X509publicKey);
            }
            catch(Exception e){
                e.printStackTrace();
            }

            return null;
    }

    public static byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int len;

        // read bytes from the input stream and store them in buffer
        while ((len = in.read(buffer)) != -1) {
            // write bytes from the buffer into output stream
            os.write(buffer, 0, len);
        }

        return os.toByteArray();
    }
}