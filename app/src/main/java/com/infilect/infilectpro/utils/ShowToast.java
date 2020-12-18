package com.infilect.infilectpro.utils;

import android.widget.Toast;

import com.infilect.infilectpro.MyApplication;

public class ShowToast {

    public static void showToastMessage(String message)
    {
        Toast.makeText(MyApplication.getInstance(),message,Toast.LENGTH_SHORT).show();
    }
}
