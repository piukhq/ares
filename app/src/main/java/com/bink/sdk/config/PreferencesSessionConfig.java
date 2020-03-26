package com.bink.sdk.config;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.bink.sdk.util.BinkSecurityUtil;

import java.util.Set;

/**
 * Created by jm on 19/07/16.
 */
@SuppressLint("CommitPrefEdits")
public class PreferencesSessionConfig implements SessionConfig {

    private static final String PREF_CLIENT_ID = "client_id";
    private static final String PREF_PACKAGE_NAME = "package_name";
    private static final String PREF_USER_TOKEN = "user_token";
    private static final String PREF_USER_EMAIL = "user_email";
    private static final String PREF_USER_ID = "user_id";
    private static final String PREF_WALLET_LAST_UPDATED = "wallet_last_updated";
    private static final String PREF_SCHEMES_LAST_UPDATED = "schemes_last_updated";
    private static final String PREF_PAYMENT_CARDS_LAST_UPDATED = "payment_cards_last_updated";
    private static final String PREF_USER_LAST_UPDATED = "user_last_updated";
    private static final String PREF_ORDERS_PENDING_SYNC = "orders_pending_sync";
    private static final String PREF_REGISTERED_KITS = "registered_kits";
    private static final String PREF_CURRENT_SDK_VERSION = "current_sdk_version";
    private static final String PREF_ENCRYPTION_KEY = "encrypted_key";

    private SharedPreferences preferences;

    private String clientId;
    private String packageName;
    private Set<String> registeredKits;
    private String userToken;
    private String userEmail;
    private String userId;
    private long walletLastUpdated;
    private long schemesLastUpdated;
    private long paymentCardsLastUpdated;
    private long userLastUpdated;
    private boolean ordersPendingSync;
    private int currentSdkVersion;
    private String encryptedKey;

    public PreferencesSessionConfig(String preferencesName, Context context) {
        preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
        setupEncryption(context);
        loadPreferences();
    }

    private void setupEncryption(Context context) {
        encryptedKey = preferences.getString(PREF_ENCRYPTION_KEY, null);
        BinkSecurityUtil.create(this, context);
    }

    private void loadPreferences() {
        currentSdkVersion = preferences.getInt(PREF_CURRENT_SDK_VERSION, 0);
        packageName = preferences.getString(PREF_PACKAGE_NAME, null);
        registeredKits = preferences.getStringSet(PREF_REGISTERED_KITS, null);
        walletLastUpdated = preferences.getLong(PREF_WALLET_LAST_UPDATED, 0);
        schemesLastUpdated = preferences.getLong(PREF_SCHEMES_LAST_UPDATED, 0);
        paymentCardsLastUpdated = preferences.getLong(PREF_PAYMENT_CARDS_LAST_UPDATED, 0);
        userLastUpdated = preferences.getLong(PREF_USER_LAST_UPDATED, 0);
        ordersPendingSync = preferences.getBoolean(PREF_ORDERS_PENDING_SYNC, false);

        //Added encryption on SDK_VERSION 5
        if (currentSdkVersion < 5) {
            clientId = preferences.getString(PREF_CLIENT_ID, null);
            userToken = preferences.getString(PREF_USER_TOKEN, null);
            userEmail = preferences.getString(PREF_USER_EMAIL, null);
            setAPIKey(userToken);
            setUserEmail(userEmail);
            setClientId(clientId);
        }
        clientId = BinkSecurityUtil.decrypt(preferences.getString(PREF_CLIENT_ID, null));
        userToken = BinkSecurityUtil.decrypt(preferences.getString(PREF_USER_TOKEN, null));
        userEmail = BinkSecurityUtil.decrypt(preferences.getString(PREF_USER_EMAIL, null));
        userId = BinkSecurityUtil.decrypt(preferences.getString(PREF_USER_ID, null));
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public void setClientId(String clientId) {
        this.clientId = clientId;

        preferences.edit()
                .putString(PREF_CLIENT_ID, BinkSecurityUtil.encrypt(clientId))
                .apply();
    }

    @Override
    public String getApplicationId() {
        return packageName;
    }

    @Override
    public void setApplicationId(String applicationId) {
        this.packageName = applicationId;

        preferences.edit()
                .putString(PREF_PACKAGE_NAME, applicationId)
                .apply();
    }

    @Override
    public Set<String> getRegisteredKits() {
        return registeredKits;
    }

    @Override
    public void setRegisteredKits(Set<String> registeredKits) {
        this.registeredKits = registeredKits;

        preferences.edit()
                .putStringSet(PREF_REGISTERED_KITS, registeredKits)
                .apply();
    }

    @Override
    public String getAPIKey() {
        return userToken;
    }

    @Override
    public void setAPIKey(String userToken) {
        this.userToken = userToken;

        preferences.edit()
                .putString(PREF_USER_TOKEN, BinkSecurityUtil.encrypt(userToken))
                .apply();
    }

    @Override
    public String getUserEmail() {
        return userEmail;
    }

    @Override
    public void setUserEmail(String email) {
        userEmail = email;

        preferences.edit()
                .putString(PREF_USER_EMAIL, BinkSecurityUtil.encrypt(userEmail))
                .apply();
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public void setUserId(String userId) {
        this.userId = userId;

        preferences.edit()
                .putString(PREF_USER_ID, BinkSecurityUtil.encrypt(userId))
                .apply();
    }

    @Override
    public long getWalletLastUpdated() {
        return walletLastUpdated;
    }

    @Override
    public void setWalletLastUpdated(long lastUpdated) {
        walletLastUpdated = lastUpdated;

        preferences.edit()
                .putLong(PREF_WALLET_LAST_UPDATED, walletLastUpdated)
                .apply();
    }

    @Override
    public long getSchemesLastUpdated() {
        return schemesLastUpdated;
    }

    @Override
    public void setSchemesLastUpdated(long lastUpdated) {
        schemesLastUpdated = lastUpdated;

        preferences.edit()
                .putLong(PREF_SCHEMES_LAST_UPDATED, schemesLastUpdated)
                .apply();
    }

    @Override
    public long getPaymentCardsLastUpdated() {
        return paymentCardsLastUpdated;
    }

    @Override
    public void setPaymentCardsLastUpdated(long lastUpdated) {
        paymentCardsLastUpdated = lastUpdated;

        preferences.edit()
                .putLong(PREF_PAYMENT_CARDS_LAST_UPDATED, paymentCardsLastUpdated)
                .apply();
    }

    @Override
    public void setUserLastUpdated(long lastUpdated) {
        userLastUpdated = lastUpdated;

        preferences.edit()
                .putLong(PREF_USER_LAST_UPDATED, userLastUpdated)
                .apply();
    }

    @Override
    public long getUserLastUpdated() {
        return userLastUpdated;
    }

    @Override
    public void setOrdersPendingSync(boolean ordersPendingSync) {
        this.ordersPendingSync = ordersPendingSync;

        preferences.edit()
                .putBoolean(PREF_ORDERS_PENDING_SYNC, ordersPendingSync)
                .apply();
    }

    public boolean areOrdersPendingSync() {
        return ordersPendingSync;
    }

    @Override
    public int getCurrentSdkVersion() {
        return currentSdkVersion;
    }

    @Override
    public void setCurrentSdkVersion(int currentSdkVersion) {
        this.currentSdkVersion = currentSdkVersion;

        preferences.edit()
                .putInt(PREF_CURRENT_SDK_VERSION, currentSdkVersion)
                .apply();
    }

    @Override
    public void setEncryptedKey(String encryptedKey) {
        this.encryptedKey = encryptedKey;

        preferences.edit()
                .putString(PREF_ENCRYPTION_KEY, encryptedKey)
                .apply();
    }

    @Override
    public String encryptSomething(Context context,  String string, String publicKey) {
        return BinkSecurityUtil.getEncryptedMessage(context, string, publicKey);
    }

    public String getEncryptedKey() {
        return encryptedKey;
    }

    @Override
    public void clear() {
        preferences.edit()
                .clear()
                .apply();

        loadPreferences();
    }
}
