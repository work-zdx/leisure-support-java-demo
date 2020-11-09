package com.starmedia.leisure_support_java;

import android.app.Application;

import com.starmedia.tinysdk.StarMedia;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        StarMedia.init(this, "2");
    }
}
