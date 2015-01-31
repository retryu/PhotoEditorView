package com.retryu.zebra.effect.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.tencent.util.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Version 1.0
 * <p/>
 * <p/>
 * Date: 2014-10-31 17:07
 * Author: retryu
 * <p/>
 * <p/>
 * Copyright © 1998-2014 Tencent Technology (Shenzhen) Company Ltd.
 */

public class RotateView extends CropView {


    private Matrix mMatrix = new Matrix();
    private static final String tag = "rotateView";
    private static final int DEALY_CROP_TIME = 1000;

    private float initDrawbleWidth;
    private float initDrawbleHeigt;
    public float originWidth;
    public float originHeight;


    CropListenner cropListenner;
    private boolean isRotateing = false;


    private static boolean debug = false;

    public RotateView(Context context, AttributeSet attrs) {
        super(context, attrs);


        TouchListener touchListener = new TouchListener();
        touchListener.setImageView(this);
        setOnTouchListener(touchListener);


        init();
        Log.e("debug", "");
    }

    float touchScale;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void init() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                setScaleType(ScaleType.MATRIX);
                mMatrix.set(getImageMatrix());
                centernPoint = getCropCenterPoint();
                mMatrix.postScale(oldScale, oldScale, centernPoint.x, centernPoint.y);
                Log.e("debug", "[onSizeChanged] centter x:" + centernPoint.x + " y:" + centernPoint.y);
                mInitScale = oldScale;
//                MAX_SCALE = mInitScale * MAX_SCALE;

                MIN_SCALE = mInitScale * MIN_SCALE;
                touchScale = oldScale;
                MIN_SCALE = touchScale;
                totalScale = mInitScale;
                mScaleDrawableWidth = mScaleDrawableWidth * oldScale;
                mScaleDrawableHeight = mScaleDrawableHeight * oldScale;
                initDrawbleWidth = mScaleDrawableWidth;
                initDrawbleHeigt = mScaleDrawableHeight;
                setImageMatrix(mMatrix);
                getImageRect();
                printMatrix(getImageMatrix());
                MAX_SCALE = Math.min(photoBounds.width(), photoBounds.height()) * getImageScale() / MIN_CROP_WIDTH_HEIGHT;
                Log.e("debug", "[init]  sacle:" + oldScale);
            }
        }, 600);
        //TODO
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        scale = 1f;
        setBounderChecker(new BounderChecker() {

            @Override
            public boolean containInBounder(float[] checkDelta) {
                return isContain(checkDelta);
            }

            @Override
            public boolean checkCropBounder(RectF cropped) {
                return checkCropBound(cropped);
            }
        });


    }


    /**
     * 设置原始图片的宽和高
     *
     * @param width
     * @param height
     */
    public void setOriginal(float width, float height) {
        originWidth = width;
        originHeight = height;
        originalRatio = originWidth / originHeight;
    }

    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        setImageWidthHeight();
        mBitmap = bm.copy(bm.getConfig(), true);
        originWidth = mBitmap.getWidth();
        originHeight = mBitmap.getHeight();
    }

    private float mImageW;
    private float mImageH;
    private float mRotatedImageW;
    private float mRotatedImageH;

    private boolean mCanInit = true;
    private Matrix mScaleMatrix = new Matrix();
    private Matrix mTmpMatrix = new Matrix();

    private PointF mPointCenter = new PointF();

    float[] scaleValue = new float[9];

    private void setImageWidthHeight() {
        Drawable d = getDrawable();
        if (d == null) {
            return;
        }
        mImageW = mRotatedImageW = d.getIntrinsicWidth();
        mImageH = mRotatedImageH = d.getIntrinsicHeight();
        Log.e(tag, "getIntrinsic  w:" + mImageW);
        Log.e(tag, "getIntrinsic  h:" + mImageH);


        mMatrix.setScale(0, 0);
        // fixScale();
        // bgIv.setImageMatrix(matrix);
        initImage();
    }


    private RectF mImageRect;
    private Rect mInnerRect;
    private float mParentW;
    private float mParentH;


    float mScaleDrawableHeight;
    float mScaleDrawableWidth;
    boolean isFatRadio;
    float im_ratio;
    float view_ratio;


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        Log.e("debug", "RotateView [onSizeChanged]  w:" + w + "  h:" + h);
        mViewW = w;
        mViewH = h;

//        if (oldw == 0 || oldh == 0) {
        mParentW = getWidth();
        mParentH = getHeight();
//        }

        view_ratio = (float) mParentW / (float) mParentH;
        im_ratio = mImageW / mImageH;


        //胖图片
        if (im_ratio >= view_ratio) {
            // 横向铺满
            isFatRadio = true;
            newViewW = mParentW;
            newViewH = newViewW * mImageH / mImageW;

        } else if (im_ratio < view_ratio) {
            // 纵向铺满
            isFatRadio = false;
            newViewH = mParentH;
            newViewW = newViewH * mImageW / mImageH;
        }


//        if(mInitMatrix == null){
        mInitMatrix = new Matrix();
        mInitMatrix.set(displayMatrix);
        setImageMatrix(mInitMatrix);

//        }
        initImage();
        mImageRect = new RectF(0f, 0f, mViewW, mViewH);

        mScaleDrawableHeight = newViewH;
        mScaleDrawableWidth = newViewW;
        rectF = new RectF();
        rectF.left = 0;
        rectF.top = 0;
        rectF.right = mImageW;
        rectF.bottom = mImageH;
        calRotate();
    }

    public void checkFatRadio() {
        //胖图片
        if (im_ratio >= view_ratio) {
            // 横向铺满
            isFatRadio = true;
        } else if (im_ratio < view_ratio) {
            // 纵向铺满
            isFatRadio = false;
        }
    }

    /*

     */
    Matrix mInitMatrix;
    PointF centernPoint;

    private void initImage() {
        if (mViewW <= 0 || mViewH <= 0 || mImageW <= 0 || mImageH <= 0 || !mCanInit) {
            Log.e(tag, "[initImage] retrun:");

            return;
        }
        mCanInit = false;
        mMatrix.set(getImageMatrix());
        fixScale();
        mPointCenter.set(mViewW / 2, mViewH / 2);
        mScaleMatrix.set(mMatrix);
        rotateR = new RotateRect(mViewW, mViewH);
        imageRect = new RotateRect(mViewW, mViewH);
        centernPoint = getCropCenterPoint();
        pathImg = new Path();

    }

    private void fixScale() {
        float p[] = new float[9];
        mMatrix.getValues(p);

        oldScale = displayBounds.width() / newViewW;

        Log.e(tag, "[fixScale] after  scale:" + oldScale);
    }


    /**
     * 用于记录图片要进行拖拉时候的坐标位置
     */
    private Matrix currentMatrix = new Matrix();

    private RectF tmpCropped;

    private final class TouchListener implements OnTouchListener {


        public void setImageView(ImageView imageView) {
            this.imageView = imageView;
        }

        private ImageView imageView;


        /**
         * 记录是拖拉照片模式还是放大缩小照片模式
         */
        private int mode = 0;// 初始状态
        /**
         * 拖拉照片模式
         */
        private static final int MODE_DRAG = 1;
        /**
         * 放大缩小照片模式
         */
        private static final int MODE_ZOOM = 2;

        /**
         * 用于记录开始时候的坐标位置
         */
        private PointF startPoint = new PointF();
        /**
         * 用于记录拖拉图片移动的坐标位置
         */
        private Matrix matrix = new Matrix();


        /**
         * 两个手指的开始距离
         */
        private float startDis;
        /**
         * 两个手指的中间点
         */
        private PointF midPoint;
        /**
         * 标记UP操作后是否进行裁剪操作变量*
         */
        private boolean cancelCropped = false;
        /**
         * 标记上次裁剪操作是否完成的变量*
         */
        private boolean finishCropped = true;
        /**
         * 标记最后一次UP操作的时间*
         */
        private long lastUpTime;
        /**
         * 标记上次Up时间的间隔*
         */
        float timeDiff;


        @Override
        public boolean onTouch(View v, final MotionEvent event) {
            /** 通过与运算保留最后八位 MotionEvent.ACTION_MASK = 255 */
//            getImageOutPath();
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                // 手指压下屏幕
                case MotionEvent.ACTION_DOWN:
                    tmpCropped = new RectF();
                    tmpCropped.set(cropped);
                    cancelCropped = true;
                    mode = MODE_DRAG;
                    // 记录ImageView当前的移动位置
                    currentMatrix.set(imageView.getImageMatrix());
                    startPoint.set(event.getX(), event.getY());
                    Log.e(tag, "ACTION_DOWN: lockImageInScreen brefore:" + matrix);
//                    matrix.postScale(0.5f,0.5f,360f,540f);
                    Log.e(tag, "ACTION_DOWN: lockImageInScreen after:" + matrix);
                    matrix.set(currentMatrix);
                    imageView.setImageMatrix(matrix);
                    mGestureDector.onTouchEvent(event);
                    break;
                // 手指在屏幕上移动，改事件会被不断触发
                case MotionEvent.ACTION_MOVE:

                    Log.e(tag, "onScoll [ACTION_MOVE]: " + "  ImageScale:" + getImageScale() + "  cropSclae:" + getCropScale() + " MAX:" + MAX_SCALE);
                    if (movingEdges == 16 || movingEdges == 0) {
                        // 拖拉图片
                        if (mode == MODE_DRAG && !isRotateing) {
                            operateListenner.hasOprated();
                            float dx = event.getX() - startPoint.x; // 得到x轴的移动距离
                            float dy = event.getY() - startPoint.y; // 得到x轴的移动距离
                            // 在没有移动之前的位置上进行移动
                            matrix.set(currentMatrix);
                            matrix.postTranslate(dx, dy);
                            imageView.setImageMatrix(matrix);

                        }
                        // 放大缩小图片
                        else if (mode == MODE_ZOOM && !isRotateing) {
                            calRotate();
                            showSize = false;
                            float endDis = distance(event);// 结束距离
                            if (endDis > 10f) { // 两个手指并拢在一起的时候像素大于10
                                operateListenner.hasOprated();
                                scale = endDis / startDis;// 得到缩放倍数
                                matrix.set(currentMatrix);
                                totalScale = touchScale * scale;
//                                if(dstScale<MAX_SCALE && dstScale>MIN_SCALE) {
                                matrix.postScale(scale, scale, midPoint.x, midPoint.y);
//                                }
                            }
                            float[] value = new float[9];
                            matrix.getValues(value);
                            float mScale = value[Matrix.MSCALE_X];
                            Log.e("debug", "  [move_up]  " + getImageMatrix());
                            Log.d("debug", "onScoll[mapPhotoRect] postScale:" + scale + "  mScale:" + mScale + "  totalScale:" + totalScale + "  touchScale:" + touchScale + "  MAX:" + MAX_SCALE);
//                            if(scale <3) {
                            mScaleDrawableHeight = initDrawbleHeigt * scale;
                            mScaleDrawableWidth = initDrawbleWidth * scale;
                            imageView.setImageMatrix(matrix);
//                            }
                        }
                        operationState = MOVE_STATE;
                    } else if (!isRotateing) {
                        if (isEnabled()) {
                            Log.e(tag, "onTouch: lockImageInScreen brefore:");
                            calRotate();
                            showSize = true;
                            mGestureDector.onTouchEvent(event);
                            if (cropListenner != null) {
                                cropListenner.onCropping();
                            }
                        }
                    }
                    isContain(null);
                    Log.e(tag, "[checkScale]" + " scale:" + getImageScale());
//                    updateCropBound();
//                    invalidate();
                    break;
                // 手指离开屏幕
                case MotionEvent.ACTION_UP:

                    Log.e(tag, "onScoll [ACTION_MOVE] Up: " + " movingEdges" + movingEdges + "  mode:" + mode);
                    // 当触点离开屏幕，但是屏幕上还有触点(手指)
                    cancelCropped = false;

                    //如果是拖动裁剪边框或者上次松手后进行的裁剪未完成。
                    if ((movingEdges != MOVE_BLOCK && movingEdges != MOVE_SCALE) || finishCropped == false) {
                        finishCropped = false;

                        Log.e(tag, "onScoll [ACTION_MOVE] Up: " + timeDiff);
                        if (mode == MODE_DRAG) {
                            Log.d(tag, "onScoll [ACTION_MOVE] Up: rung");
                            postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //判断是松开手后，延迟一秒以上才能进行裁剪操作
                                    timeDiff = (System.currentTimeMillis() - lastUpTime) / 1000f;
                                    Log.d(tag, "onScoll [ACTION_MOVE] Up: running timeDiff:" + timeDiff);
                                    if (cancelCropped == false && timeDiff >= 1f) {
                                        mGestureDector.onTouchEvent(event);
                                        operationState = NORMAL_STATE;
                                        updateCropBound();
                                        invalidate();
                                        finishCropped = true;
                                        showSize = false;
                                        if (cropListenner != null) {
                                            cropListenner.cropFinished();
                                        }
                                    }
                                }
                            }, DEALY_CROP_TIME);
                        } else {
                            showSize = false;
                        }
                    }
                    if(movingEdges == MOVE_BLOCK){
                        showSize = false;
                    }

                    touchScale = totalScale;
                    lastUpTime = System.currentTimeMillis();
                    calRotate();
                    checkImageBound();
                    checkBound(mPhotoBoundRect, cropped);
                    calRotate();
                    getCropImageSize();
                    invalidate();
                    operationState = NORMAL_STATE;

                case MotionEvent.ACTION_POINTER_UP:
                    Log.d(tag, "ACTION_POINTER_UP: " + imageView.getImageMatrix());
                    mode = 0;
                    break;
                // 当屏幕上已经有触点(手指)，再有一个触点压下屏幕
                case MotionEvent.ACTION_POINTER_DOWN:
                    Log.e(tag, "ACTION_POINTER_DOWN: " + matrix);
                    mode = MODE_ZOOM;
                    /** 计算两个手指间的距离 */
                    startDis = distance(event);
                    /** 计算两个手指间的中间点 */
                    if (startDis > 10f) { // 两个手指并拢在一起的时候像素大于10
                        midPoint = mid(event);
                        //记录当前ImageView的缩放倍数
                        currentMatrix.set(imageView.getImageMatrix());
                    }
                    imageView.setImageMatrix(matrix);
                    break;
                case MotionEvent.ACTION_CANCEL:
                    break;
            }

            return true;
        }

        public void checkImageBound() {
            float heightScale = Float.MIN_VALUE;
            float widthScale = Float.MIN_VALUE;
            float scale = 1f;
            if (imageRect.getHeight() < rotateR.getHeight()) {
                heightScale = rotateR.getHeight() / imageRect.getHeight();
            }
            if (imageRect.getWidth() < rotateR.getWidth()) {
                widthScale = rotateR.getWidth() / imageRect.getWidth();
            }
            scale = Math.max(heightScale, widthScale);

            if (getCropScale() > MAX_SCALE) {
                scale = MAX_SCALE / getCropScale();
            }
            if (scale != Float.MIN_VALUE) {
                if (scale != Float.MIN_VALUE) {
                    if (midPoint != null) {
                        mMatrix.set(getImageMatrix());
                        mMatrix.postScale(scale, scale, midPoint.x, midPoint.y);
                        setImageMatrix(mMatrix);
                    }
                }
                Log.e("debug", "[checkImageBound]:" + scale + " MIN:" + MIN_SCALE + "  MAX:" + MAX_SCALE + "  s:" + getImageScale());
            }
        }


        private void checkScale() {

            PointF pointF = getCropCenterPoint();
            if (getImageScale() > MAX_SCALE) {
                float scale = MAX_SCALE / getImageScale();
                totalScale = MAX_SCALE;
                touchScale = MAX_SCALE;
                mMatrix.set(getImageMatrix());
                mMatrix.postScale(scale, scale, pointF.x, pointF.y);
                setImageMatrix(mMatrix);
            } else if (getImageScale() < MIN_SCALE) {
                float scale = MIN_SCALE / getImageScale();
                totalScale = MIN_SCALE;
                touchScale = MIN_SCALE;

                mMatrix.set(getImageMatrix());
                mMatrix.postScale(scale, scale, midPoint.x, midPoint.y);
                setImageMatrix(mMatrix);
            }
            Log.e(tag, "[checkScale]" + " totalScale" + totalScale + "  MAX:" + MAX_SCALE + "  MIN:" + MIN_SCALE + "  getImageSize::" + getImageScale() + "  initScale:" + mInitScale);

        }


        /**
         * 计算两个手指间的距离
         */
        private float distance(MotionEvent event) {
            float dx = event.getX(1) - event.getX(0);
            float dy = event.getY(1) - event.getY(0);
            /** 使用勾股定理返回两点之间的距离 */
            return FloatMath.sqrt(dx * dx + dy * dy);
        }

        /**
         * 计算两个手指间的中间点
         */
        private PointF mid(MotionEvent event) {
            float midX = (event.getX(1) + event.getX(0)) / 2;
            float midY = (event.getY(1) + event.getY(0)) / 2;
            return new PointF(midX, midY);
        }

    }


    public float checkBound(RectF rectInner, RectF rectOut) {
        float offsetX = 0f;
        float offsetY = 0f;
        if (cropped == null) {
            return 0;
        }
        mMatrix.mapRect(mPhotoBoundRect, rectF);
        if (!isContain(null)) {
//            getCheckBound(true);
            checkImageBound();
            calRotate();
            invalidate();

        }
        return offsetY;
    }


    public float[] printMatrix(Matrix matrix) {
        float[] value = new float[9];
        matrix.getValues(value);
        float scale = value[Matrix.MSCALE_X];
        float tranX = value[Matrix.MTRANS_X];
        float tranY = value[Matrix.MTRANS_Y];
        Log.e("debug", "[printMatrix]" + matrix);
        Log.e("debug", "[mapPhotoRect] [printMatrix] scale:" + scale + "  transX:" + tranX + "  transY:" + tranY + " photoBound" + mPhotoBoundRect + "  crop" + cropped);
        return value;
    }


    private boolean checkImageBound() {
        rotate.reset();
        rotate.postRotate(-degree, 0, 0);
        mBackImageRect.angle = -1;
        mBackRotateRect.angle = -1;
        imageRect.rotateDegree = degree;
        rotateR.rotateDegree = degree;
        calRotate(cropped);
        getOut(rotate, imageRect, mBackImageRect);
        getImageOutPath();

        getOut(rotate, rotateR, mBackRotateRect);
        getRotateOutPath();

        Log.e("debug", "[getCheckBound] [contain] imageWidth:" + mBackRotateRect.getWidth() + "   rotateWidth:" + mBackImageRect.getWidth());
        Log.e("debug", "[getCheckBound] [contain] image:" + mBackRotateRect.p4() + "   rotate:" + mBackImageRect.p4());
        if (checkDelta == null) {
            checkDelta = new float[2];
        }

        float diffX = 0;
        float diffY = 0;
        float offsetX = 0f;
        float offsetY = 0f;
        if (mBackRotateRect.p1().x < mBackImageRect.p1().x) {
            diffX += mBackRotateRect.p1().x - mBackImageRect.p1().x;
        }
        if (mBackRotateRect.p1().y < mBackImageRect.p1().y) {
            diffY += mBackRotateRect.p1().y - mBackImageRect.p1().y;
        }


        if (mBackRotateRect.p4().x > mBackImageRect.p4().x) {
            diffX += mBackRotateRect.p4().x - mBackImageRect.p4().x;
        }
        if (mBackRotateRect.p4().y > mBackImageRect.p4().y) {
            diffY += mBackRotateRect.p4().y - mBackImageRect.p4().y;
        }
        double angle = Math.toRadians(degree);
        offsetY = (float) (diffX * Math.sin(angle));
        offsetX = -(float) (diffY * Math.sin(angle));
        if (angle != 0f) {
            diffX = offsetX + (float) (diffX * Math.cos(angle));

        }
        diffY = offsetY + (float) (diffY * Math.cos(angle));
        translateImage(diffX, diffY);
        return true;


//        Log.d("debug","[getCheckBound]  before cropped:"+cropped);
//        if((int)(mBackRotateRect.p1().x)<(int)(mBackImageRect.p1().x)||(int)(mBackRotateRect.p1().y)<(int)(mBackImageRect.p1().y)){
////            Log.e("debug","[getCheckBound] [contain]  image"+mBackImageRect);
////            Log.e("debug","[getCheckBound] [contain]  rotate"+mBackRotateRect);
//            Log.e("debug","[getCheckBound] [contain]  flase1");
//
//            float diffX = (mBackRotateRect.p1().x) - (mBackImageRect.p1().x);
//            if (diffX < 0) {
//                checkDelta[0] = -diffX;
//            }
//            float diffY = mBackRotateRect.p1().y - mBackImageRect.p1().y;
//            if (diffY < 0) {
//                checkDelta[1] = -diffY;
//            }
////            cropped.left = cropped.left +checkDelta[0];
////            cropped.top = cropped.top +checkDelta[1];
//            Log.d("debug","[getCheckBound]  after cropped:"+cropped);
//            Log.e("debug","[getCheckBound]  dX:"+checkDelta[0]+"  dY:"+checkDelta[1]);
//            return false;
//        }
//        if((int)(mBackRotateRect.p2().x)>(int)(mBackImageRect.p2().x)|| (int)(mBackRotateRect.p2().y)<(int)(mBackImageRect.p2().y)){
//            Log.e("debug","[getCheckBound] [contain]  flase2");
//            float diffX = (mBackRotateRect.p2().x) - (mBackImageRect.p2().x);
//            if (diffX > 0) {
//                checkDelta[0] = -diffX;
//            }
//            float diffY = mBackRotateRect.p2().y - mBackImageRect.p2().y;
//            if (diffY < 0) {
//                checkDelta[1] = -diffY;
//            }
////            cropped.right = cropped.right +checkDelta[0];
////            cropped.top = cropped.top +checkDelta[1];
//            Log.d("debug","[getCheckBound]  after cropped:"+cropped);
//            Log.e("debug","[getCheckBound]  dX:"+checkDelta[0]+"  dY:"+checkDelta[1]);
//            return  false;
//        }
//        if((int)(mBackRotateRect.p3().x)<(int)(mBackImageRect.p3().x)||(int)(mBackRotateRect.p3().y)>(int)(mBackImageRect.p3().y)){
//            Log.e("debug","[getCheckBound] [contain]  flase3");
//            float diffX = (mBackRotateRect.p3().x) - (mBackImageRect.p3().x);
//            if (diffX < 0) {
//                checkDelta[0] = -diffX;
//            }
//            float diffY = mBackRotateRect.p3().y - mBackImageRect.p3().y;
//            if (diffY > 0) {
//                checkDelta[1] = -diffY;
//            }
////            cropped.left = cropped.left +checkDelta[0];
////            cropped.bottom = cropped.bottom +checkDelta[1];
//            Log.e("debug","[getCheckBound]  dX:"+checkDelta[0]+"  dY:"+checkDelta[1]);
//            return false;
//        }
//        if((int)(mBackRotateRect.p4().x)>(int)(mBackImageRect.p4().x)|| (int)(mBackRotateRect.p4().y)>(int)(mBackImageRect.p4().y)){
//            float diffX = (mBackRotateRect.p4().x) - (mBackImageRect.p4().x);
//            if (diffX > 0) {
//                checkDelta[0] = -diffX;
//            }
//            float diffY = mBackRotateRect.p4().y - mBackImageRect.p4().y;
//            if (diffY > 0) {
//                checkDelta[1] = -diffY;
//            }
////            cropped.right = cropped.right +checkDelta[0];
////            cropped.bottom = cropped.bottom+checkDelta[1];
//            Log.e("debug","[getCheckBound]  dX:"+checkDelta[0]+"  dY:"+checkDelta[1]);
//            return false;
//        }
//        return true;
    }


    float p2;

    /**
     * @param fixBound 如否裁剪框查出边界，是否需要做修正操作。或者只是返回是否超出的结果
     * @return
     */
    private boolean getCheckBound(boolean fixBound) {
        float distanceX = Float.MIN_VALUE;
        float distanceY = Float.MIN_VALUE;
//       裁剪框是否内嵌与图片
        boolean contained = true;

        rotateR.rotateDegree = degree;
        imageRect.rotateDegree = degree;
        Log.e("debug", "[getCheckBound]     :");
        if (imageRect.p1().x > rotateR.p1().x) {
            Log.e("debug", "[getCheckBoundOffsetX] imageRect.p1().x > rotateR.p1().x:" + distanceX);
            //如果纵方向也在框内
            if (imageRect.p1().y < rotateR.p1().y) {

                contained = false;
                Log.d("debug", "[getCheckBound] i.x>r.x i.y<r.y " + "  i.p1:" + imageRect.p1());
                distanceX = rotateR.p1().x - imageRect.p1().x;
                float distanceTopY = Math.abs(rotateR.p1().y - imageRect.p1().y);
                double angle = Math.toRadians(imageRect.angle);
                float difY = (float) (Math.tan(angle) * distanceTopY);
                distanceX = distanceX + difY;
                distanceY = 0f;
            } else {
                Log.d("debug", "[getCheckBound] i.x>r.x i.y>r.y " + "  i.p1:" + imageRect.p1());
                distanceX = rotateR.p1().x - imageRect.p1().x;
                distanceY = rotateR.p1().y - imageRect.p1().y;

            }
        } else {
            if (imageRect.p1().y > rotateR.p1().y) {
                Log.d("debug", "[getCheckBound] i.x<r.x i.y>r.y ");
                Log.d("debug", "[getCheckBound]  image p1:" + imageRect.p1() + "  p2:" + imageRect.p2() + "  p3:" + imageRect.p3() + "  p4:" + imageRect.p4());
                distanceX = 0f;
                float distanceTopY = Math.abs(rotateR.p1().x - imageRect.p1().x);
                double angle = Math.toRadians(imageRect.angle);
                float difY = (float) (Math.tan(angle) * distanceTopY);
                distanceY = rotateR.p1().y - imageRect.p1().y;
                distanceY = distanceY - difY;
            } else {

            }

        }

        Log.d("debug", "[getCheckBound]    LT x:" + distanceX + "  y:" + distanceY);
        if (fixBound == true) {
            translateImage(distanceX, distanceY);
        }
        if (distanceX != Float.MIN_VALUE && distanceY != Float.MIN_VALUE) {
            contained = false;
        }
        getImageRect();


        distanceX = Float.MIN_VALUE;
        distanceY = Float.MIN_VALUE;
        double angle = Math.toRadians(imageRect.angle);
        if (imageRect.p4().x < rotateR.p4().x) {
            if (imageRect.p4().y < rotateR.p4().y) {
                Log.d("debug", "[getCheckBound] i4.x<r.x i4.y<r.y ");
                Log.d("debug", "[getCheckBound]  image p1:" + imageRect.p1() + "  p2:" + imageRect.p2() + "  p3:" + imageRect.p3() + "  p4:" + imageRect.p4());

                distanceX = rotateR.p4().x - imageRect.p4().x;
                distanceY = rotateR.p4().y - imageRect.p4().y;
            } else {
                Log.d("debug", "[getCheckBound] i4.x<r.x i4.y>r.y ");
                Log.d("debug", "[getCheckBound]  image p1:" + imageRect.p1() + "  p2:" + imageRect.p2() + "  p3:" + imageRect.p3() + "  p4:" + imageRect.p4());

                float diffP2 = Math.abs(imageRect.p2().x - rotateR.p2().x);
                float diff = (float) (Math.tan(angle) * diffP2);
                p2 = rotateR.p2().y + diff;

                Log.e("debyg", "[getCheckBound]  p2:" + diff + "  p2:" + p2 + "   i2:" + imageRect.p2().y + "  r2.y" + rotateR.p2().y);
                if (p2 < imageRect.p2().y) {
                    Log.d("debug", "[getCheckBound] p2 < imageRect.p2().y ");
                    Log.d("debug", "[getCheckBound]  image p1:" + imageRect.p1() + "  p2:" + imageRect.p2() + "  p3:" + imageRect.p3() + "  p4:" + imageRect.p4());

                    distanceX = rotateR.p2().x - imageRect.p2().x;
                    distanceY = rotateR.p2().y - imageRect.p2().y;
                } else {
                    Log.d("debug", "[getCheckBound] p2 > imageRect.p2().y ");
                    Log.d("debug", "[getCheckBound]  image p1:" + imageRect.p1() + "  p2:" + imageRect.p2() + "  p3:" + imageRect.p3() + "  p4:" + imageRect.p4());

                    distanceY = 0f;
                    distanceX = rotateR.p4().x - imageRect.p4().x;
                    float distanceTopY = Math.abs(rotateR.p4().y - imageRect.p4().y);
                    float difY = (float) (Math.tan(angle) * distanceTopY);
                    distanceX = distanceX - difY;
                }

            }
        } else {
            if (imageRect.p4().y < rotateR.p4().y) {
                distanceX = 0f;
                distanceY = rotateR.p4().y - imageRect.p4().y;
                float distanceTopX = Math.abs(rotateR.p4().x - imageRect.p4().x);
                float difY = (float) (Math.tan(angle) * distanceTopX);
                distanceY = distanceY + difY;
                Log.d("debug", "[getCheckBound] i4.x>r.x i4.y<r.y ");
            } else {
                distanceX = 0f;
                distanceY = imageRect.p4().y - rotateR.p4().y;
                float distanceTopX = Math.abs(rotateR.p4().x - imageRect.p4().x);
                float difY = (float) (distanceTopX / Math.tan(angle));
                distanceY = difY;
                Log.e("debug", "[getCheckBound] i4.x>r.x i4.y>r.y ");
            }

        }

        if (distanceX != Float.MIN_VALUE && distanceY != Float.MIN_VALUE) {
            contained = false;
        }
        if (fixBound == true) {
            translateImage(distanceX, distanceY);
        }
        Log.d("debug", "[getCheckBound]    RB x:" + distanceX + "  y:" + distanceY);
        invalidate();
        return contained;
    }

    public void translateImage(float x, float y) {
        mMatrix.set(getImageMatrix());
        mMatrix.postTranslate(x, y);
        setImageMatrix(mMatrix);
    }


    Matrix rotate = new Matrix();
    RotateRect mBackImageRect = new RotateRect(mViewW, mViewH);
    RotateRect mBackRotateRect = new RotateRect(mViewW, mViewH);

//    float [] checkDetal =new float[2];

    /**
     * 裁剪框时候包含了图片外框
     *
     * @return
     */
    public boolean isContain(float[] checkDetal) {
        rotate.reset();
        rotate.postRotate(-degree, 0, 0);
        mBackImageRect.angle = -1;
        mBackRotateRect.angle = -1;
        rotateR.rotateDegree = degree;
        imageRect.rotateDegree = degree;
        getOut(rotate, imageRect, mBackImageRect);
        getImageOutPath();
        getOut(rotate, rotateR, mBackRotateRect);
        getRotateOutPath();

        Log.e("debug", "[isContain] [contain] imageWidth:" + mBackRotateRect.getWidth() + "   rotateWidth:" + mBackImageRect.getWidth());
        Log.e("debug", "[isContain] [contain] image:" + mBackRotateRect.p4() + "   rotate:" + mBackImageRect.p4());
        if ((int) (mBackRotateRect.p1().x) < (int) (mBackImageRect.p1().x) || (int) (mBackRotateRect.p1().y) < (int) (mBackImageRect.p1().y)) {
            Log.e("debug", "[isContain] [contain]  image" + mBackImageRect);
            Log.e("debug", "[isContain] [contain]  rotate" + mBackRotateRect);
            Log.e("debug", "[isContain] [contain]  flase1");
//            if(checkDetal == null) {
//              checkDetal = new  float[2];
//            }
//                float diffX = (mBackRotateRect.p1().x) - (mBackImageRect.p1().x);
//                if (diffX < 0) {
//                    checkDetal[0] = -diffX;
//                }
//                float diffY = mBackRotateRect.p1().y - mBackImageRect.p1().y;
//                if (diffY < 0) {
//                    checkDetal[1] = -diffY;
//                }
            return false;
        }
        if ((int) (mBackRotateRect.p2().x) > (int) (mBackImageRect.p2().x) || (int) (mBackRotateRect.p2().y) < (int) (mBackImageRect.p2().y)) {
            Log.e("debug", "[isContain] [contain]  flase2");
            return false;
        }
        if ((int) (mBackRotateRect.p3().x) < (int) (mBackImageRect.p3().x) || (int) (mBackRotateRect.p3().y) > (int) (mBackImageRect.p3().y)) {
            Log.e("debug", "[isContain] [contain]  flase3");
            return false;
        }
        if ((int) (mBackRotateRect.p4().x) > (int) (mBackImageRect.p4().x) || (int) (mBackRotateRect.p4().y) > (int) (mBackImageRect.p4().y)) {
            Log.e("debug", "[isContain] [contain]  flase4");
            return false;
        }
        return true;
    }


    /**
     * 检测裁剪框是否超出了图像的范围，并进行微调
     *
     * @param cropped
     * @return
     */
    public boolean checkCropBound(RectF cropped) {
        rotate.reset();
        rotate.postRotate(-degree, 0, 0);
        mBackImageRect.angle = -1;
        mBackRotateRect.angle = -1;
        imageRect.rotateDegree = degree;
        rotateR.rotateDegree = degree;
        calRotate(cropped);
        getOut(rotate, imageRect, mBackImageRect);
        getImageOutPath();

        getOut(rotate, rotateR, mBackRotateRect);
        getRotateOutPath();

        Log.e("debug", "[getCheckBound] [contain] imageWidth:" + mBackRotateRect.getWidth() + "   rotateWidth:" + mBackImageRect.getWidth());
        Log.e("debug", "[getCheckBound] [contain] image:" + mBackRotateRect.p4() + "   rotate:" + mBackImageRect.p4());
        if (checkDelta == null) {
            checkDelta = new float[2];
        }


        Log.d("debug", "[getCheckBound]  before cropped:" + cropped);
        if ((int) (mBackRotateRect.p1().x) < (int) (mBackImageRect.p1().x) || (int) (mBackRotateRect.p1().y) < (int) (mBackImageRect.p1().y)) {
//            Log.e("debug","[getCheckBound] [contain]  image"+mBackImageRect);
//            Log.e("debug","[getCheckBound] [contain]  rotate"+mBackRotateRect);
            Log.e("debug", "[getCheckBound] [contain]  flase1");

            float diffX = (mBackRotateRect.p1().x) - (mBackImageRect.p1().x);
            if (diffX < 0) {
                checkDelta[0] = -diffX;
            }
            float diffY = mBackRotateRect.p1().y - mBackImageRect.p1().y;
            if (diffY < 0) {
                checkDelta[1] = -diffY;
            }
//            cropped.left = cropped.left +checkDelta[0];
//            cropped.top = cropped.top +checkDelta[1];
            Log.d("debug", "[getCheckBound]  after cropped:" + cropped);
            Log.e("debug", "[getCheckBound]  dX:" + checkDelta[0] + "  dY:" + checkDelta[1]);
            return false;
        }
        if ((int) (mBackRotateRect.p2().x) > (int) (mBackImageRect.p2().x) || (int) (mBackRotateRect.p2().y) < (int) (mBackImageRect.p2().y)) {
            Log.e("debug", "[getCheckBound] [contain]  flase2");
            float diffX = (mBackRotateRect.p2().x) - (mBackImageRect.p2().x);
            if (diffX > 0) {
                checkDelta[0] = -diffX;
            }
            float diffY = mBackRotateRect.p2().y - mBackImageRect.p2().y;
            if (diffY < 0) {
                checkDelta[1] = -diffY;
            }
//            cropped.right = cropped.right +checkDelta[0];
//            cropped.top = cropped.top +checkDelta[1];
            Log.d("debug", "[getCheckBound]  after cropped:" + cropped);
            Log.e("debug", "[getCheckBound]  dX:" + checkDelta[0] + "  dY:" + checkDelta[1]);
            return false;
        }
        if ((int) (mBackRotateRect.p3().x) < (int) (mBackImageRect.p3().x) || (int) (mBackRotateRect.p3().y) > (int) (mBackImageRect.p3().y)) {
            Log.e("debug", "[getCheckBound] [contain]  flase3");
            float diffX = (mBackRotateRect.p3().x) - (mBackImageRect.p3().x);
            if (diffX < 0) {
                checkDelta[0] = -diffX;
            }
            float diffY = mBackRotateRect.p3().y - mBackImageRect.p3().y;
            if (diffY > 0) {
                checkDelta[1] = -diffY;
            }
//            cropped.left = cropped.left +checkDelta[0];
//            cropped.bottom = cropped.bottom +checkDelta[1];
            Log.e("debug", "[getCheckBound]  dX:" + checkDelta[0] + "  dY:" + checkDelta[1]);
            return false;
        }
        if ((int) (mBackRotateRect.p4().x) > (int) (mBackImageRect.p4().x) || (int) (mBackRotateRect.p4().y) > (int) (mBackImageRect.p4().y)) {
            float diffX = (mBackRotateRect.p4().x) - (mBackImageRect.p4().x);
            if (diffX > 0) {
                checkDelta[0] = -diffX;
            }
            float diffY = mBackRotateRect.p4().y - mBackImageRect.p4().y;
            if (diffY > 0) {
                checkDelta[1] = -diffY;
            }
//            cropped.right = cropped.right +checkDelta[0];
//            cropped.bottom = cropped.bottom+checkDelta[1];
            Log.e("debug", "[getCheckBound]  dX:" + checkDelta[0] + "  dY:" + checkDelta[1]);
            return false;
        }
        return true;
    }

    private void getOut(Matrix matrix, RotateRect rotateR, RotateRect recoverRect) {
        float[] ori = new float[]{0, 0};
        float[] dst = new float[2];

        ori[0] = rotateR.p1.x;
        ori[1] = rotateR.p1.y;
        matrix.mapPoints(dst, ori);
        recoverRect.p1.set(dst[0], dst[1]);


        ori[0] = rotateR.p2.x;
        ori[1] = rotateR.p2.y;
        matrix.mapPoints(dst, ori);
        recoverRect.p2.set(dst[0], dst[1]);


        ori[0] = rotateR.p3.x;
        ori[1] = rotateR.p3.y;
        matrix.mapPoints(dst, ori);
        recoverRect.p3.set(dst[0], dst[1]);

        ori[0] = rotateR.p4.x;
        ori[1] = rotateR.p4.y;
        matrix.mapPoints(dst, ori);
        recoverRect.p4.set(dst[0], dst[1]);

    }

    RectF imageOutRect = new RectF();


    Path pathImg;


    public void getImageRect() {
        float[] ori = new float[]{0, 0};
        float[] dst = new float[2];
        mMatrix.mapPoints(dst, ori);
        imageRect.p1.set(dst[0], dst[1]);


        ori[0] = originWidth;
        mMatrix.mapPoints(dst, ori);
        imageRect.p2.set(dst[0], dst[1]);


        ori[0] = 0;
        ori[1] = originHeight;
        mMatrix.mapPoints(dst, ori);
        imageRect.p3.set(dst[0], dst[1]);

        ori[0] = originWidth;
        ori[1] = originHeight;
        mMatrix.mapPoints(dst, ori);
        imageRect.p4.set(dst[0], dst[1]);


    }


    private void getImageOutPath() {
        if (pathImg == null) {
            pathImg = new Path();
        } else {
            pathImg.reset();
        }
        pathImg.moveTo(imageRect.p1.x, imageRect.p1.y);
        pathImg.lineTo(imageRect.p2.x, imageRect.p2.y);
        pathImg.lineTo(imageRect.p4.x, imageRect.p4.y);
        pathImg.lineTo(imageRect.p3.x, imageRect.p3.y);
        pathImg.close();
    }

    private void getRotateOutPath() {
        if (path1 == null) {
            path1 = new Path();
        } else {
            path1.reset();
        }
        path1.moveTo(rotateR.p1.x, rotateR.p1.y);
        path1.lineTo(rotateR.p2.x, rotateR.p2.y);
        path1.lineTo(rotateR.p4.x, rotateR.p4.y);
        path1.lineTo(rotateR.p3.x, rotateR.p3.y);
        path1.close();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);
        mMatrix = getImageMatrix();


        float[] ori = new float[]{0, 0};

        float[] dst = new float[2];
        mMatrix.mapPoints(dst, ori);

        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#ff00ff"));
        Log.e("debug", "[onDraw]  dst.x:" + dst[0] + "  dst.y:" + dst[1]);


//            getImageRect();
        if (debug == true) {

            mMatrix.mapRect(mPhotoBoundRect, rectF);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.parseColor("#55cccccc"));
            canvas.drawRect(mPhotoBoundRect, paint);

        /*绘制裁剪框的中间点*/
            paint.setColor(Color.BLUE);
            canvas.drawCircle(centernPoint.x, centernPoint.y, 10, paint);

            paint.setColor(Color.RED);
            canvas.drawCircle(mPhotoBoundRect.centerX(), mPhotoBoundRect.centerY(), 10, paint);


            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);


            if (path1 != null) {
                //回执路径
                paint.setColor(Color.RED);
                canvas.drawPath(path1, paint);
                paint.setColor(Color.BLUE);
                canvas.drawPath(pathImg, paint);
            }

            paint.setColor(Color.WHITE);
            canvas.drawRect(imageOutRect, paint);

            if (rotateR.p4 != null && rotateR.p3 != null) {
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                paint.setColor(Color.RED);
                canvas.drawCircle(rotateR.p1().x, rotateR.p1().y, 10f, paint);
                paint.setColor(Color.YELLOW);
                canvas.drawCircle(rotateR.p2().x, rotateR.p2().y, 10f, paint);
                paint.setColor(Color.BLUE);
                canvas.drawCircle(rotateR.p3().x, rotateR.p3().y, 10f, paint);
                paint.setColor(Color.GREEN);
                canvas.drawCircle(rotateR.p4().x, rotateR.p4().y, 10f, paint);
            }


            if (imageRect != null) {
                drawRotateRect(canvas, paint, imageRect);
            }
//            if(mBackImageRect != null){
//
//                paint.setStyle(Paint.Style.STROKE);
//                drawRotateRect(canvas, paint, mBackImageRect);
////                Path  path = getPathFromRect(mBackImageRect);
//                paint.setColor(Color.YELLOW);
////                canvas.drawPath(path,paint);
//            }
//            if(mBackRotateRect != null){
//                paint.setStyle(Paint.Style.STROKE);
//                drawRotateRect(canvas, paint,mBackRotateRect);
////                Path  path = getPathFromRect(mBackRotateRect);
//                paint.setColor(Color.GREEN);
////                canvas.drawPath(path,paint);
//            }
        }


        canvas.restore();

    }

    private void drawRotateRect(Canvas canvas, Paint paint, RotateRect rotateRect) {

        paint.setColor(Color.RED);
        canvas.drawCircle(rotateRect.p1().x, rotateRect.p1().y, 10f, paint);
        paint.setColor(Color.YELLOW);
        canvas.drawCircle(rotateRect.p2().x, rotateRect.p2().y, 10f, paint);
        paint.setColor(Color.BLUE);
        canvas.drawCircle(rotateRect.p3().x, rotateRect.p3().y, 10f, paint);
        paint.setColor(Color.GREEN);
        canvas.drawCircle(rotateRect.p4().x, rotateRect.p4().y, 10f, paint);
    }

    Path path1 = new Path();
    float cropBoundHeight;

    /**
     * 计算旋转后的最小外切框的矩形
     *
     * @return
     */
    public Path calRotate() {
        cropped = getCropBoundsDisplayed();
        return calRotate(cropped);

    }

    public Path calRotate(RectF cropped) {

        RectF rotateRect = new RectF();


        double angle;
        double angleDouble = 0;

        Log.e("debug", "[calRotate] degree:" + degree);
        if (degree < 0) {
            angleDouble = 90 + degree;
        } else {
            angleDouble = degree;
        }


        angle = Math.toRadians(angleDouble);


        Log.e("debug", "[calRotate]  flip:" + flipAngle + " angle:" + angleDouble + "  gedree:" + degree);
//        cropped = getCropBoundsDisplayed();
        getImageRect();
        getImageOutPath();
        float croppedWidth = cropped.width();
        float croppedHeigth = cropped.height();

        rotateR.angle = degree;
        imageRect.angle = degree;
        imageRect.rotateDegree = degree;
        rotateR.rotateDegree = degree;
        float a = Math.abs((float) (croppedWidth * Math.sin(angle)));
        /** 计算第1个点的位置 **/
        rotateR.p1.x = cropped.left + (float) (a * Math.sin(angle));
        rotateR.p1.y = cropped.top - (float) (a * Math.cos(angle));

        /** 计算第4个点的位置 **/
        rotateR.p4.x = cropped.right - (float) (a * Math.sin(angle));
        rotateR.p4.y = cropped.bottom + (float) (a * Math.cos(angle));

        rotateRect.set(rotateR.p1.x, rotateR.p1.y, rotateR.p4.y, rotateR.p4.x);

        /** 连接第1和第4个点  **/
        path1 = new Path();
        path1.moveTo(rotateR.p1.x, rotateR.p1.y);
        float a2 = Math.abs((float) (croppedHeigth * Math.cos(angle)));

        /** 计算第2个点的位置 **/
        rotateR.p2.x = (float) (cropped.right + a2 * Math.sin(angle));
        rotateR.p2.y = (float) (cropped.bottom - a2 * Math.cos(angle));
        path1.lineTo(rotateR.p2.x, rotateR.p2.y);
//        path1.lineTo(rotateR.p1.x, rotateR.p1.y);

        /** 计算第3点的位置 **/
        rotateR.p3.x = (float) (cropped.left - a2 * Math.sin(angle));
        rotateR.p3.y = (float) (cropped.top + a2 * Math.cos(angle));
        path1.lineTo(rotateR.p4.x, rotateR.p4.y);
        path1.lineTo(rotateR.p3.x, rotateR.p3.y);

        path1.close();

        float l, r, t, b;
        l = rotateR.p1.x < rotateR.p3.x ? rotateR.p1.x : rotateR.p3.x;
        r = rotateR.p4.x > rotateR.p2.x ? rotateR.p4.y : rotateR.p2.x;
        t = rotateR.p1.y < rotateR.p2.y ? rotateR.p1.y : rotateR.p2.y;
        b = rotateR.p4.y > rotateR.p3.y ? rotateR.p4.y : rotateR.p3.y;
        imageOutRect.set(l, t, r, b);
        if (degree >= 0) {
            float cropWidth = getDistance(rotateR.p4.x, rotateR.p4.y, rotateR.p3.x, rotateR.p3.y);
            cropBoundHeight = rotateR.getHeight();
            Log.e("debug", "[cropBound]  width:" + cropWidth + " height:" + cropBoundHeight + "  radio" + (cropBoundHeight / cropWidth));
        } else {
            cropBoundHeight = rotateR.getHeight();
            float cropWidth = getDistance(rotateR.p2.x, rotateR.p2.y, rotateR.p4.x, rotateR.p4.y);
            Log.e("debug", "[cropBound]  width:" + cropWidth + " height:" + cropBoundHeight + "  radio" + (cropBoundHeight / cropWidth));

        }
        invalidate();
        return path1;

    }

    public float getDistance(float x1, float y1, float x2, float y2) {
        float d1 = Math.abs(x1 - x2);
        float d2 = Math.abs(y1 - y2);
        float t = Math.abs(d1 * d1 + d2 * d2);
        float r1 = (float) Math.sqrt(t);
        return r1;
    }

    public float getDistance(PointF p1, PointF p2) {
        return getDistance(p1.x, p1.y, p2.x, p2.y);
    }


    public void drawMinContainCroppedBound(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#33ff00ff"));
        RectF cropped = getCropBoundsDisplayed();
        canvas.drawRect(cropped, paint);
    }

    public float getScale() {
        float scale = 0f;

        mMatrix.getValues(scaleValue);
        scale = scaleValue[0];
        return scale;
    }


    private float scale = 0f;


    private float degree = 0f;
    float oldDegree = 0f;

    public float getCurrentDegree() {
        return oldDegree;
    }

    float rurningDegere = 0f;

    public void trunningRotate() {
        rurningDegere -= 90f;
        cropped = getCropBoundsDisplayed();
        float imWidth = (float) cropped.width();
        float imHeight = (float) cropped.height();

        float hTmp, wTmp;

        hTmp = cropped.height();
        wTmp = cropped.width();

        float ratio_view = hTmp / wTmp;
        float ratio_parent = mParentH / mParentW;
        float scale = 0f;
        float newWidth = 0f;
        float newHeight = 0f;

        float degree = -90;
        centernPoint = getCropCenterPoint();
        Log.e("debug", "[trunningRotate]  degree:" + degree + "  runingDegre:" + rurningDegere);

        float cropWidth = cropped.width();
        float cropHeight = cropped.height();


        float newCropWidth = 0;
        float newCropHeight = 0;

        if (ratio_parent >= ratio_view) {
//           if(ratio_view < 1f ){
            // 胖图片，始终是宽度适配屏幕
            isFatRadio = true;
//            newWidth = mParentW;
//            newHeight = newWidth * hTmp / wTmp;
            newHeight = mParentH;
            scale = (newHeight - 2 * mRectPadding) / wTmp;
            Log.e("debug", "[trunningRotate]  scale:" + scale);


            float ratio;
            //竖向铺满
            newCropHeight = (mParentH - mRectPadding * 2);
            if (mRatio == 0) {
                ratio = cropped.width() / cropped.height();
                newCropWidth = newCropHeight / ratio;
            } else {
                mRatio = 1f / mRatio;
                ratio = mRatio;
                newCropWidth = newCropHeight / mRatio;
            }

            cropped.left = (mViewW - newCropWidth) / 2;
            cropped.right = cropped.left + newCropWidth;
            cropped.top = mRectPadding;
            cropped.bottom = cropped.top + newCropHeight;
            // 如果拉升后宽度大于屏幕，则横向铺满，纵向留边
            if (newCropWidth > mParentW) {
                newCropWidth = (mParentW - 2 * mRectPadding);
                scale = newCropWidth / hTmp;
                newCropHeight = newCropWidth * ratio;


                cropped.left = mRectPadding;
                cropped.right = mViewW - mRectPadding;
                cropped.top = (mViewH - newCropHeight) / 2;
                cropped.bottom = cropped.top + newCropHeight;
            }
            mMatrix = getImageMatrix();
            mMatrix.postRotate(degree, centernPoint.x, centernPoint.y);
            mMatrix.postScale(scale, scale, centernPoint.x, centernPoint.y);
            turningMatrix.postRotate(degree, centernPoint.x, centernPoint.y);
            turningMatrix.postScale(scale, scale, centernPoint.x, centernPoint.y);


        } else {
            isFatRadio = false;
            scale = (mParentW - 2 * mRectPadding) / hTmp;
            float ratio;
            //横向铺满
            newCropWidth = (mViewW - mRectPadding * 2);
            if (mRatio == 0) {
                ratio = cropped.width() / cropped.height();
                newCropHeight = newCropWidth * ratio;
            } else {
                mRatio = 1f / mRatio;
                ratio = mRatio;
                newCropHeight = newCropWidth * mRatio;
            }
            cropped.left = mRectPadding;
            cropped.right = mViewW - mRectPadding;
            cropped.top = (mViewH - newCropHeight) / 2;
            cropped.bottom = cropped.top + newCropHeight;
            // 如果拉升后，高度大于屏幕，纵向平铺，横向留边
            if (newHeight > mParentW) {
                newCropHeight = (mParentH - 2 * mRectPadding);
                scale = newCropHeight / wTmp;
                newCropWidth = newCropHeight * ratio;

                cropped.left = (mViewW - newCropWidth) / 2;
                cropped.right = cropped.left + newCropWidth;
                cropped.top = mRectPadding;
                cropped.bottom = cropped.top + newCropHeight;
            }


            mMatrix = getImageMatrix();
            mMatrix.postRotate(degree, centernPoint.x, centernPoint.y);
            mMatrix.postScale(scale, scale, centernPoint.x, centernPoint.y);
            turningMatrix.postRotate(degree, centernPoint.x, centernPoint.y);
            turningMatrix.postScale(scale, scale, centernPoint.x, centernPoint.y);

        }


        truningPhotoBound();


        Matrix matrix = new Matrix();
        RectF rectF = new RectF(mRectPadding, mRectPadding, mViewW - mRectPadding, mViewH - mRectPadding - mRectPaddingBtmExtra);
        if (matrix.setRectToRect(photoBounds, rectF, Matrix.ScaleToFit.CENTER)) {
            matrix.mapRect(displayBounds, photoBounds);
            displayMatrix.setRectToRect(photoBounds, displayBounds, Matrix.ScaleToFit.CENTER);
        }
        matrix.invert(photoMatrix);
        mapPhotoRect(cropped, cropBounds);
        Log.e("debug", "[trunningRotate]  crop:" + getCropBoundsDisplayed());
        calRotate();
        mScaleDrawableHeight = rotateR.getHeight();
        mScaleDrawableWidth = rotateR.getWidth();
        setImageMatrix(mMatrix);
        invalidate();
        printMatrix(displayMatrix);
        isTruning = !isTruning;
        MIN_SCALE = scale * mInitScale;
        updateMaxScale();

    }

    public void truningPhotoBound() {
        float tmp = photoBounds.right;
        photoBounds.right = photoBounds.bottom;
        photoBounds.bottom = tmp;
    }


    public void rotate(float d, Matrix matrix) {
        this.degree = d;
        if (flipAngle == true) {
            degree = -d;
        }
        operateListenner.hasOprated();
        operationState = ROTATE_STATE;
        matrix.set(getImageMatrix());
        PointF cropPoint = getCropCenterPoint();
        matrix.postRotate(-oldDegree, cropPoint.x, cropPoint.y);
        matrix.postRotate(this.degree, cropPoint.x, cropPoint.y);
        Log.e(tag, "[rotate] degree:" + this.degree + "  oldDegree:" + oldDegree + "  flip:" + flipAngle);
        setImageMatrix(matrix);
        oldDegree = degree;
        showSize = true;
        isRotateing = true;
    }
    public  void setShowSize(boolean show){
        showSize = show;
        invalidate();
    }


    public void scaleImage() {

        PointF cropPoint = getCropCenterPoint();


        Log.e("debug", "[scale]:  i.h()" + imageRect.getHeight() + "  r.h()" + rotateR.getHeight());

        if ((int) (imageRect.getHeight()) == (int) (rotateR.getHeight()) || (int) (imageRect.getWidth()) == (int) (rotateR.getWidth())) {
            calRotate();
//            checkBound(mPhotoBoundRect,cropped);
            mMatrix.set(getImageMatrix());
            Log.v("debug", "[scale]:  i.h()" + imageRect.getHeight() + "  r.h()" + rotateR.getHeight());
            float ratioRotate = rotateR.getHeight() / rotateR.getWidth();
            float ratioImage = imageRect.getHeight() / imageRect.getWidth();
            if (ratioRotate > ratioImage) {
                oldScale = rotateR.getHeight() / imageRect.getHeight();
                mScaleDrawableHeight = mScaleDrawableHeight * oldScale;
                mScaleDrawableWidth = mScaleDrawableWidth * oldScale;
            } else {
                oldScale = rotateR.getWidth() / imageRect.getWidth();
                mScaleDrawableWidth = mScaleDrawableWidth * oldScale;
                mScaleDrawableHeight = mScaleDrawableHeight * oldScale;
            }
            Log.e("debug", "[scale]:  isFatRadio" + isFatRadio + " oldScale" + oldScale);
            mMatrix.postScale(oldScale, oldScale, cropPoint.x, cropPoint.y);
            setImageMatrix(mMatrix);
        } else {
            Log.d("debug", "[scale]:  i.h()" + imageRect.getHeight() + "  r.h()" + rotateR.getHeight());

        }
        calRotate();
        checkImageSCale();
        checkBound(mPhotoBoundRect, cropped);
    }


    public void checkImageSCale() {
        float heightScale = Float.MAX_VALUE;
        float widthScale = Float.MAX_VALUE;
        float scale = 1f;
        if (imageRect.getHeight() < rotateR.getHeight()) {
            heightScale = rotateR.getHeight() / imageRect.getHeight();
        }
        if (imageRect.getWidth() < rotateR.getWidth()) {
            widthScale = rotateR.getWidth() / imageRect.getWidth();
        }
        scale = Math.min(heightScale, widthScale);
        if (scale == Float.MAX_VALUE) {
            scale = 1f;
        } else {
            mMatrix.set(getImageMatrix());
            mMatrix.postScale(scale, scale, cropped.centerX(), cropped.centerY());
            setImageMatrix(mMatrix);
        }
    }


    /**
     * 获得裁剪框的中心点
     *
     * @return
     */
    public PointF getCropCenterPoint() {
        RectF cropped = getCropBoundsDisplayed();
        float x = (cropped.right - cropped.left) / 2 + cropped.left;
        float y = (cropped.bottom - cropped.top) / 2 + cropped.top;
        PointF pointF = new PointF();
        pointF.set(x, y);
        return pointF;
    }


    public class RotateRect {
        PointF p1, p2, p3, p4;
        ArrayList<PointF> points;
        Matrix m = new Matrix();
        public float angle;
        PointF ltPoint, rtPoint, lbPoint, rbPoint;

        public float rotateDegree;
        Comparator<PointF> compareX;
        Comparator<PointF> compareY;

        public RotateRect(float maxW, float maxH) {
            p1 = new PointF();
            p2 = new PointF();
            p3 = new PointF();
            p4 = new PointF();

            points = new ArrayList();
            points.add(p1);
            points.add(p2);
            points.add(p3);
            points.add(p4);

            ltPoint = new PointF(0, 0);
            rtPoint = new PointF(10 * maxW, 0);
            lbPoint = new PointF(0, 10 * maxH);
            rbPoint = new PointF(10 * maxW, 10 * maxH);
            angle = 0f;

            compareX = new Comparator<PointF>() {
                @Override
                public int compare(PointF lhs, PointF rhs) {
                    if ((int) lhs.x < (int) rhs.x) {
                        return -1;
                    } else if ((int) lhs.x > (int) rhs.x) {
                        return 1;
                    } else {
                        if ((int) lhs.y < (int) rhs.y) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }

                }
            };
            compareY = new Comparator<PointF>() {
                @Override
                public int compare(PointF lhs, PointF rhs) {
                    if ((int) lhs.y < (int) rhs.y) {
                        return -1;
                    } else if ((int) lhs.y > (int) rhs.y) {
                        return 1;
                    } else {
                        if ((int) lhs.x < (int) rhs.x) {
                            return -1;
                        } else {
                            return 1;
                        }

                    }
                }
            };
        }


        public float getWidth() {
//            if(angle>=0){
            return getDistance(p4().x, p4().y, p3().x, p3().y);
//            }
//            else{
//                return getDistance(p4.x, p4.y, p2.x, p2.y);
//
//            }
        }

        public float getHeight() {
//            if(angle>=0){
            return getDistance(p2().x, p2().y, p4().x, p4().y);
//            }
//            else{
//                return getDistance(p1().x, p1().y, p2().x, p2().y);
//
//            }
        }

        public PointF getClose(PointF dst) {
            PointF p = p1;
            float d1 = getDistance(p1, dst);
            float d2 = getDistance(p2, dst);
            float d3 = getDistance(p3, dst);
            float d4 = getDistance(p4, dst);
            float min = d1;
            if (d2 < min) {
                p = p2;
                min = d2;
            }
            if (d3 < min) {
                p = p3;
                min = d3;
            }
            if (d4 < min) {
                p = p4;
                min = d4;
            }
            return p;
        }

        public PointF p1() {


            if (angle == -1) {
                Collections.sort(points, compareY);
                return points.get(0);
            }
            if (rotateDegree < 0) {
                Collections.sort(points, compareX);
                return points.get(0);
            } else if (rotateDegree > 0) {
                Collections.sort(points, compareY);
                return points.get(0);
            } else {
                Collections.sort(points, compareY);
                return points.get(0);
            }

        }

        public PointF p2() {
            if (angle == -1) {
                Collections.sort(points, compareY);
                return points.get(1);
            }

            if (rotateDegree < 0) {
                Collections.sort(points, compareY);
                return points.get(0);
            } else if (rotateDegree > 0) {
                Collections.sort(points, compareX);
                return points.get(3);
            } else {
                Collections.sort(points, compareY);
                return points.get(1);
            }
        }

        public PointF p3() {
            if (angle == -1) {
                Collections.sort(points, compareY);
                return points.get(2);
            }
            if (rotateDegree < 0) {
                Collections.sort(points, compareY);
                return points.get(3);
            } else if (rotateDegree > 0) {
                Collections.sort(points, compareX);
                return points.get(0);
            } else {
                Collections.sort(points, compareY);
                return points.get(2);
            }
        }

        public PointF p4() {
            if (angle == -1) {
                Collections.sort(points, compareY);
                return points.get(3);
            }
            if (rotateDegree < 0) {
                Collections.sort(points, compareX);
                return points.get(3);
            } else if (rotateDegree > 0) {
                Collections.sort(points, compareY);
                return points.get(3);
            } else {
                Collections.sort(points, compareY);
                return points.get(3);
            }
        }

        @Override
        public String toString() {
            return "RotateRect{" +
                    "p1=" + p1() +
                    ", p2=" + p2() +
                    ", p3=" + p3() +
                    ", p4=" + p4() +
                    '}';
        }
    }

    public void stopRaotae() {
        operationState = NORMAL_STATE;
        isRotateing = false;
        showSize = false;
        invalidate();
    }

    /**
     * 还原图像至原始装填
     */
    public void reset() {
        fixScale();
        degree = 0;
        rotateR.angle = 0;
        imageRect.angle = 0;
        oldDegree = 0;
        totalScale = touchScale = mInitScale;
        resetCropBound();
        setImageMatrix(mInitMatrix);
        printMatrix(mInitMatrix);
        setCanvasRotate(0);

        photoBounds.right = originWidth;
        photoBounds.bottom = originHeight;


        //效果需要
        calRotate();
        initCrop(mViewW, mViewH);
        sizeChanged();
        cropped = getCropBoundsDisplayed();
        mScaleDrawableHeight = cropped.height();
        mScaleDrawableWidth = cropped.width();
        //设置成默认的Matrix

        //框内风格线效果
        operationState = NORMAL_STATE;
//        //恢复默认宽廋图片
        checkFatRadio();
        invalidate();
        isTruning = false;
        MIN_SCALE = mInitScale;
        isRotateing = false;
        updateMaxScale();
    }


    private boolean flipAngle = false;

    /**
     * 设置图片内容横向镜像
     *
     * @param matrix
     * @param oldDegree
     */
    public void verticalFlip(Matrix matrix, float oldDegree) {
        mMatrix.set(getImageMatrix());
        mMatrix.mapRect(mPhotoBoundRect, rectF);
        matrix.set(getImageMatrix());
//        degree = 90 -degree;
        this.oldDegree = -this.oldDegree;
        matrix.postScale(-1, 1, mPhotoBoundRect.centerX(), mPhotoBoundRect.centerY());
        setImageMatrix(matrix);
        flipAngle = !flipAngle;
        calRotate();
        Log.e(tag, "verticalFlip [rotate] degree:" + degree + "  oldDegree:" + oldDegree);
        updateMaxScale();
        checkBound(mPhotoBoundRect, cropped);
    }

    /**
     * 检查边界的类
     */
    public interface BounderChecker {

        public boolean containInBounder(float[] checkDelta);

        public boolean checkCropBounder(RectF cropped);

    }


    //** 裁剪裁剪框区域内的图片
    public void cropImage() {
        Bitmap croppedBitmap;
        try {
            float scale = getCropScale();
            if (scale <= MAX_SCALE && scale >= MIN_SCALE) {
                croppedBitmap = process();
                if (croppedBitmap != null) {
                    mCanInit = true;
                    initImage();
                    setCropBounds(new RectF(0, 0, croppedBitmap.getWidth(), croppedBitmap.getHeight()));
                    setImageBitmap(croppedBitmap);
                    reset();
                    mInitMatrix.set(displayMatrix);
                    setImageMatrix(mInitMatrix);
                    //TODO  临时保存，检验结果
//                    saveBitmap(croppedBitmap, "/storage/emulated/0/Download/1.jpg");
                }
            } else {
                Toast.makeText(this.getContext(), "选中区域太小，无法裁剪", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this.getContext(), "选中区域太小，无法裁剪", Toast.LENGTH_SHORT).show();
        }


    }

    public static int saveBitmap(Bitmap bitmap, String path) {
        FileOutputStream out = null;
        try {
            File file = new File(path);
            File parent = file.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            out = new FileOutputStream(path);
            if (bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, Utils.JPEG_QUALITY, out);
                out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
            return 1;
        } finally {
            IOUtils.closeQuietly(out);
        }

        return 1;
    }


    public Bitmap process() {
        if (mViewW <= 0 || mViewH <= 0 || mMatrix == null) {
            return null;
        }
        if (mBitmap == null) {
            return null;
        }
        calRotate();
        float scale = mBitmap.getWidth() / imageRect.getWidth();
        // count actual matrix
        Matrix rotateMatrix = new Matrix(getImageMatrix());
        rotateMatrix.postScale(scale, scale, cropped.centerX(), cropped.centerY());

        RectF srcRectF = new RectF();
        rotateMatrix.mapRect(srcRectF);
        Bitmap bmpSrc = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), rotateMatrix, true);

        mMatrix.mapRect(mPhotoBoundRect, rectF);

        calRotate();
        //在bitmap被矩阵作用之后，再次做裁剪
        int diffX = (int) (cropped.left - mPhotoBoundRect.left);
        int diffY = (int) (cropped.top - mPhotoBoundRect.top);
        int x = (int) (diffX / mPhotoBoundRect.width() * bmpSrc.getWidth());
        int y = (int) (diffY / mPhotoBoundRect.height() * bmpSrc.getHeight());
        int cropWidth = (int) ((cropped.width() / mPhotoBoundRect.width()) * bmpSrc.getWidth());
        int cropHeight = (int) ((cropped.height() / mPhotoBoundRect.height()) * bmpSrc.getHeight());
        Bitmap bmpCropped = Bitmap.createBitmap(bmpSrc, x, y, cropWidth, cropHeight);
        mBitmap.recycle();
        bmpSrc.recycle();
        return bmpCropped;
    }


    public void saveImage2Bitmap(int[] result) {
        // int memSize = ((ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
        try {
//            Bitmap mDstBmp = Bitmap.createBitmap((int)mViewW, (int)mViewH, Bitmap.Config.ARGB_8888);
//            Canvas canvas = new Canvas(mDstBmp);
//            if (mBitmap != null) {
//                canvas.drawBitmap(mBitmap, mMatrix, null);
//            }

            if (mInnerRect == null) {
                // if null, re-init.
                mInnerRect = new Rect();
                RectF rect = getCropBoundsDisplayed();
                rect.roundOut(mInnerRect);
            }
            int width = (int) ((float) mInnerRect.width() / totalScale);
            int height = (int) ((float) mInnerRect.height() / totalScale);


            Bitmap btSave = Bitmap.createBitmap(mBitmap, 0, 0, width,
                    height, getImageMatrix(), true);

            if (null != mBitmap && !mBitmap.isRecycled()) {
                mBitmap.recycle();
                mBitmap = null;
            }
            setImageBitmap(btSave);
            setCropBounds(new RectF(0, 0, btSave.getWidth(), btSave.getHeight()));
        } catch (OutOfMemoryError e) {
            if (null != result) {
                result[0] = -2;
            }
        } catch (Exception e) {
            if (null != result) {
                result[0] = -1;
            }
        }
    }

    /**
     * 更新裁剪框的长宽比，更新裁剪框，并且重新设置图片r
     *
     * @param r
     */
    public void updateCropRatio(float r) {
        Log.e("", "");
        setRectRatio(r);
        reset();
        updateCropBound();
        calRotate();
//        getImageRect();
        getCropBoundsDisplayed();
        checkImageSCale();
        getImageRect();
        calRotate();
        invalidate();
        MIN_SCALE = getImageScale();
        //TODO
        updateMaxScale();
        operateListenner.hasOprated();
        isRotateing = false;
        showSize =  true;
    }

    /**
     * 为了使最大放大像素为70*70，更具裁剪框的大小改变，更新最大的放大系数
     */
    private void updateMaxScale() {
        calRotate();
        float width = cropped.width();
        float height = cropped.height();
        MAX_SCALE = Math.min(width, height) / MIN_CROP_WIDTH_HEIGHT;
        Log.d("debug", "maxScale:" + MAX_SCALE);

    }

    public interface CropListenner {
        public void onCropping();

        public void cropFinished();
    }

    public void setCropListenenr(CropListenner cropListenner) {
        this.cropListenner = cropListenner;
    }

}
