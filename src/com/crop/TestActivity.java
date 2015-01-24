package com.crop;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.tencent.photoplus.R;

/**
 * Version 1.0
 * <p/>
 * <p/>
 * Date: 2015-01-16 20:08
 * Author: retryu
 * <p/>
 * <p/>
 * Copyright © 1998-2014 Tencent Technology (Shenzhen) Company Ltd.
 */
public class TestActivity  extends Activity  implements View.OnClickListener{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_effect_crop);
        Log.e("debug"," test");
        Log.e("debug"," test");
        Log.e("debug"," test");
        Log.e("debug"," test");

    }

    @Override
    public void onClick(View v) {
        Log.e("debug"," test");
        Log.e("debug"," test");
        Log.e("debug"," test");
        Log.e("debug"," test");

    }
}
