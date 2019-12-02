package com.bink.sdk;

import android.content.Context;

import com.bink.sdk.config.PreferencesSessionConfig;
import com.bink.sdk.config.SessionConfig;

public class BinkCore {
    private static final String PREFERENCES_NAME = "BinkPreferences";

    Context context;

    SessionConfig sessionConfig;

    public BinkCore(Context context) {
        this.context = context;

        PreferencesSessionConfig preferencesSessionConfig = new PreferencesSessionConfig(PREFERENCES_NAME, context);
        sessionConfig = preferencesSessionConfig;
    }

    public SessionConfig getSessionConfig() {
        return sessionConfig;
    }

    public void logout() {
        sessionConfig.clear();
    }
}
