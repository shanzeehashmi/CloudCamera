package com.infilect.infilectpro;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class MyApplication extends Application {

    private static MyApplication instance ;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this ;
        FirebaseApp.initializeApp(this) ;

    }

    public static MyApplication getInstance()
    {
        return instance ;
    }

}
