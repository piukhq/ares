package com.bink.sdk.config;

import java.util.Set;

/**
 * Provides persistable configuration / information used by various components of the Bink SDK.
 */
public interface SessionConfig {

    String getClientId();

    void setClientId(String clientId);

    String getApplicationId();

    void setApplicationId(String applicationId);

    Set<String> getRegisteredKits();

    void setRegisteredKits(Set<String> registeredKits);

    /**
     * The most recent API key from the Bink API. This will be updated when the user authorizes, or registers. It will
     * not be updated otherwise.
     */
    String getAPIKey();

    String getUserEmail();

    String getUserId();

    void setAPIKey(String apiKey);

    void setUserEmail(String email);

    void setUserId(String userId);

    long getWalletLastUpdated();

    void setWalletLastUpdated(long lastUpdated);

    long getSchemesLastUpdated();

    void setSchemesLastUpdated(long lastUpdated);

    long getPaymentCardsLastUpdated();

    void setPaymentCardsLastUpdated(long lastUpdated);

    void setUserLastUpdated(long lastUpdated);

    long getUserLastUpdated();

    void setOrdersPendingSync(boolean pendingSync);

    boolean areOrdersPendingSync();

    int getCurrentSdkVersion();

    void setCurrentSdkVersion(int currentSdkVersion);

    void setEncryptedKey(String encryptedKey);

    String getEncryptedKey();

    void clear();

}
