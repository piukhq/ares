package com.bink.sdk;

import android.content.Context;

import com.bink.sdk.config.PreferencesSessionConfig;
import com.bink.sdk.config.SessionConfig;

public class BinkCore {
    private static final String PREFERENCES_NAME = "BinkPreferences";

    Context context;

    PreferencesSessionConfig sessionConfig;

    public BinkCore(Context context) {
        this.context = context;

        sessionConfig = new PreferencesSessionConfig(PREFERENCES_NAME, context);
    }

    public SessionConfig getSessionConfig() {
        return sessionConfig;
    }

    public void logout() {
        sessionConfig.clear();
    }
}
