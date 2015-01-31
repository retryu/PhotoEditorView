package com.retryu.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import com.retryu.view.RotateView;
import com.tencent.photoplus.R;

/**
 * Version 1.0r
 * <p/>
 * <p/>
 * Date: 2014-09-24 16:58 Author: retryu
 * <p/>
 * <p/>
 * Copyright © 1998-2014 Tencent Technology (Shenzhen) Company Ltd.
 */
public class CropActivity extends Activity implements View.OnClickListener

{

    private static final String tag = CropActivity.class.getSimpleName();

    RotateView rotateView;
    Matrix matrix = new Matrix();
    private float oldDegree;
    private float oldScale;
    private boolean needApply = true;
    SeekBar seekBar;


    private Button btnRatioFree;
    private Button btnRatioOrigin;
    private Button btnRatio1_1;
    private Button btnRatio3_4;
    private Button btnRatio4_3;
    private Button btnRatio9_16;
    private Button btnRatio16_9;
    private float orignRatio = 1f;
    private float centerPrecent = 50f;
    RectF rectF;
    float w, h;

    private Button
            btnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_view);
        String path = getIntent().getStringExtra("image_path");

        int[] result = new int[1];
        Log.e("debu", "path" + path);
//           /storage/emulated/0/Download/1~01.jpg
//        path = "/storage/emulated/0/Download/IMG_20150104_153003.jpg";
//        path ="/storage/emulated/0/image/aadajiang.jpg";
        //  /storage/emulated/0/image/aadajiang.jpg
        //  /storage/emulated/0/DCIM/Camera/MYXJ_20141222160138_org.jpg

        final BitmapFactory.Options options = new BitmapFactory.Options();
        // 当inJustDecodeBounds设为true时,不会加载图片仅获取图片尺寸信息
        options.inJustDecodeBounds = false;
        // 此时仅会将图片信息会保存至options对象内,decode方法不会返回bitmap对象
        Bitmap original = BitmapFactory.decodeFile(path, options);
        orignRatio = ((float) options.outHeight) / ((float) options.outWidth);

        btnRatioFree = (Button) findViewById(R.id.btn_free);
        btnRatio1_1 = (Button) findViewById(R.id.btn_1_1);
        btnRatio3_4 = (Button) findViewById(R.id.btn_3_4);
        btnRatio4_3 = (Button) findViewById(R.id.btn_4_3);
        btnRatio9_16 = (Button) findViewById(R.id.btn_9_16);
        btnRatio16_9 = (Button) findViewById(R.id.btn_16_9);
        btnRatioOrigin = (Button) findViewById(R.id.btn_origin);

        btnRatioFree.setOnClickListener(this);
        btnRatioOrigin.setOnClickListener(this);
        btnRatio1_1.setOnClickListener(this);
        btnRatio3_4.setOnClickListener(this);
        btnRatio4_3.setOnClickListener(this);
        btnRatio16_9.setOnClickListener(this);
        btnRatio9_16.setOnClickListener(this);


        rotateView = (RotateView) findViewById(R.id.rotate);
        //TODO  初始化图片宽度和高度
        rotateView.setOriginal(original.getWidth(), original.getHeight());
        rotateView.setRectPadding(20);
        w = original.getWidth();
        h = original.getHeight();
        rectF = new RectF(0, 0, w, h);
        rotateView.setPhotoBounds(rectF);
        rotateView.setImageBitmap(original);

        rotateView.setImageMatrix(matrix);
        rotateView.setOperatedListenner(new RotateView.OperateListenner() {
            @Override
            public void hasOprated() {
                btnReset.setVisibility(View.VISIBLE);
            }
        });
        rotateView.setCropListenenr(new RotateView.CropListenner() {
            @Override
            public void onCropping() {
                seekBar.setVisibility(View.GONE);
            }

            @Override
            public void cropFinished() {
                seekBar.setVisibility(View.VISIBLE);
            }
        });

        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        seekBar.setMax(100);
        seekBar.setProgress(50);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                if (needApply == true) {
                    float p = (float) i;
                    float degree = (p - centerPrecent) / centerPrecent * (centerPrecent - 5);
                    rotateView.rotate(degree, matrix);
                    rotateView.scaleImage();

                } else {
                    needApply = true;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.e("debug", "  ionStartTrackingTouch");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.e("debug", "  onStopTrackingTouch  seekBar");
                rotateView.stopRaotae();
            }
        });
        btnReset = (Button) findViewById(R.id.btn_reset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnReset.setVisibility(View.GONE);
                oldDegree = 0;
                needApply = false;
                rotateView.setRectRatio(0f);
                rotateView.reset();
                rotateView.updateCropBound();
                rotateView.calRotate();

                seekBar.setProgress(50);
            }
        });

        findViewById(R.id.btn_flip_moriior).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateView.verticalFlip(matrix, oldDegree);
                oldDegree = -oldDegree;
            }
        });

        findViewById(R.id.btn_flip_90).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateView.trunningRotate();
            }
        });

        findViewById(R.id.btn_invalide).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                rotateView.isContain();
                rotateView.checkBound(null, null);
            }
        });

        findViewById(R.id.btn_crop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateView.cropImage();
            }
        });


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        Log.e("debug", "  x:" + x + "   y:" + y);
        return super.onTouchEvent(event);

    }

    private float ratio = 0f;

    @Override
    public void onClick(View v) {
        seekBar.setProgress(50);

        int id = v.getId();
        switch (id) {
            case R.id.btn_origin:
                ratio = orignRatio;

                rotateView.updateCropRatio(ratio);

                break;
            case R.id.btn_free:
                ratio = 0f;
                rotateView.updateCropRatio(ratio);


                break;
            case R.id.btn_1_1:
                ratio = 1f;
                rotateView.updateCropRatio(ratio);

                break;
            case R.id.btn_3_4:
                ratio = 4f / 3f;
                rotateView.updateCropRatio(ratio);

                break;
            case R.id.btn_4_3:
                ratio = 3f / 4f;
                rotateView.updateCropRatio(ratio);


                break;
            case R.id.btn_16_9:
                ratio = 9f / 16f;
                rotateView.updateCropRatio(ratio);

                break;
            case R.id.btn_9_16:
                ratio = 16f / 9f;
                rotateView.updateCropRatio(ratio);

                break;
        }
    }


}
