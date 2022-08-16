package com.app.sambaaccesssmb;

import android.app.Application;

public class SMBAccess extends Application {
    private static SMBAccess instance = null;

    public static SMBAccess getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
