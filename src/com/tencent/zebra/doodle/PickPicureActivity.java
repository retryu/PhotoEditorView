package com.tencent.zebra.doodle;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.tencent.photoplus.R;
import com.tencent.zebra.crop.CropImageActivity;
import com.tencent.zebra.effect.CropActivity;
import com.tencent.zebra.effect.PhotoEffectActivity;
import com.tencent.zebra.effect.utils.GPUImageFilterTools;
import com.tencent.zebra.ui.ZebraBaseActivity;

/**
 * Version 1.0
 * <p/>
 * <p/>
 * Date: 2014-10-09 18:48
 * Author: retryu
 * <p/>
 * <p/>
 * 模拟照片跳转的Activity
 *
 *
 * Copyright © 1998-2014 Tencent Technology (Shenzhen) Company Ltd.
 */
public class PickPicureActivity extends Activity implements View.OnClickListener{
    private  static  int REQUEST_CODE_IMAGE=2;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick);
        findViewById(R.id.btn_pick).setOnClickListener(this);

//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  WindowManager.LayoutParams.FLAG_FULLSCREEN);

//        Intent intent = new Intent(this,CropActivity.class);
//        intent.putExtra("image_path","/storage/sdcard0/DCIM/100ANDRO/3264×4928.jpg");
//
//        startActivity(intent);

    }

    @Override
    public void onClick(View view) {

        int  id =  view.getId();
        switch (id){
            case  R.id.btn_pick:
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_CODE_IMAGE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_IMAGE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            Cursor cursor = getContentResolver().query(uri, null, null, null,null);
            if (cursor != null && cursor.moveToFirst()) {
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                Intent intent = new Intent(this,CropActivity.class);
                intent.putExtra("image_path",path);
                Log.e("debug","path"+path);
                startActivity(intent);

            }
        }
    }
}
