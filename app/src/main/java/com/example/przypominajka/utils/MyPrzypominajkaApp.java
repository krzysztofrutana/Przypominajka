package com.example.przypominajka.utils;

import android.app.Application;

// class added to androidManifest to return Application instance in other class
public class MyPrzypominajkaApp extends Application {

    private static MyPrzypominajkaApp sInstance;

    public static MyPrzypominajkaApp get() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        sInstance = this;
        super.onCreate();
    }

}