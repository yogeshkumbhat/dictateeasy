package com.dictateeasy;


import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;

import androidx.annotation.Nullable;

public class MyDialog extends Activity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_dialog);
        DisplayMetrics displayMetrics=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int screenWidth=displayMetrics.widthPixels;
        int screenHeight=displayMetrics.heightPixels;
        getWindow().setLayout((int)(screenWidth*0.5),(int)(screenHeight*0.4));

    }
}