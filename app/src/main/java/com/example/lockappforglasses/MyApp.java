package com.example.lockappforglasses;

import android.app.Application;

public class MyApp extends Application {

    private String currentClassCode = null;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void setCurrentClassCode(String value) {
        this.currentClassCode = value;
    }

    public String getCurrentClassCode() {
        return currentClassCode;
    }

    public void clearTempString() {
        this.currentClassCode = null;
    }
}
