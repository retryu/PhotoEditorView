///*
// * Copyright (C) 2010 The Android Open Source Project
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.tencent.zebra.effect;
//
//import android.content.Context;
//import android.content.res.TypedArray;
//import android.graphics.Matrix;
//import android.graphics.PointF;
//import android.graphics.RectF;
//import android.util.AttributeSet;
//import android.view.View;
//
//
//
///**
// * Full-screen tool view that gets photo display bounds and maps positions on photo display bounds
// * back to exact coordinates on photo.
// */
//public abstract class FullscreenToolView extends View {
//    private static final String TAG = FullscreenToolView.class.getSimpleName();
//
//    protected final RectF displayBounds = new RectF();
//    final Matrix photoMatrix = new Matrix();
//    final Matrix displayMatrix = new Matrix();
//
//    protected RectF photoBounds;
//    private int mOriginalHeight = 0;
//    private int mOriginalWidth = 0;
//    private int mRectPadding;
//    private int mRectPaddingBtmExtra;
//
//    public FullscreenToolView(Context context, AttributeSet attrs) {
//        super(context, attrs);
//
//        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.FullscreenToolView, 0, 0);
//
//        mRectPadding= 10;
////        mRectPaddingBtmExtra
////        mRectPadding = array.getDimensionPixelSize(R.styleable.FullscreenToolView_rectPadding, 0);
////        mRectPaddingBtmExtra = array.getDimensionPixelSize(R.styleable.FullscreenToolView_rectPaddingBtmExtra, 0);
//        array.recycle();
//
//    }
//
//    /**
//     * Photo bounds must be set before onSizeChanged() and all other instance methods are invoked.
//     */
//    public void setPhotoBounds(RectF photoBounds) {
//        this.photoBounds = photoBounds;
//    }
//
//    public void setOriginalPhotoBounds(int width, int height) {
//        mOriginalWidth = width;
//        mOriginalHeight = height;
//    }
//
//    @Override
//    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//        super.onSizeChanged(w, h, oldw, oldh);
//
//        displayBounds.setEmpty();
//        photoMatrix.reset();
//        displayMatrix.reset();
//        if (photoBounds == null || photoBounds.isEmpty()) {
//
//            return;
//        }
//
//        // Assumes photo-view is also full-screen as this tool-view and centers/scales photo to fit.
//        Matrix matrix = new Matrix();
//        if (matrix.setRectToRect(photoBounds, /*new RectF(0, 0, w, h)*/new RectF(mRectPadding, mRectPadding, w - mRectPadding, h - mRectPadding - mRectPaddingBtmExtra), Matrix.ScaleToFit.CENTER)) {
//            matrix.mapRect(displayBounds, photoBounds);
//            displayMatrix.setRectToRect(photoBounds, displayBounds, Matrix.ScaleToFit.CENTER);
//        }
//        matrix.invert(photoMatrix);
//        sizeChanged();
//    }
//
//    public float getPhotoWidth() {
//        return photoBounds.width();
//    }
//
//    public float getPhotoHeight() {
//        return photoBounds.height();
//    }
//
//    protected float getOriginalWidth() {
//        return mOriginalWidth == 0 ? photoBounds.width() : mOriginalWidth;
//    }
//
//    protected float getOriginalHeight() {
//        return mOriginalHeight == 0 ? photoBounds.height() : mOriginalHeight;
//    }
//
//    protected void mapPhotoPoint(float x, float y, PointF dst) {
//        if (photoBounds.isEmpty()) {
//            dst.set(0, 0);
//        } else {
//            float[] point = new float[]{x, y};
//            photoMatrix.mapPoints(point);
//            dst.set(point[0] / photoBounds.width(), point[1] / photoBounds.height());
//        }
//    }
//
//    protected void mapPhotoPoint2(float x, float y, PointF dst) {
//        if (photoBounds.isEmpty()) {
//            dst.set(0, 0);
//        } else {
//            float[] point = new float[]{x, y};
//            photoMatrix.mapPoints(point);
//            dst.set(point[0], point[1]);
//        }
//    }
//
//    protected void mapDisplayPoint(float x, float y, PointF dst) {
//        if (displayBounds.isEmpty()) {
//            dst.set(0, 0);
//        } else {
//            float[] point = new float[]{x, y};
//            displayMatrix.mapPoints(point);
//            dst.set(point[0], point[1]);
//        }
//    }
//
//    protected void mapPhotoRect(RectF src, RectF dst) {
//        if (photoBounds.isEmpty()) {
//            dst.setEmpty();
//        } else {
//            photoMatrix.mapRect(dst, src);
//            dst.set(dst.left / photoBounds.width(), dst.top / photoBounds.height(),
//                    dst.right / photoBounds.width(), dst.bottom / photoBounds.height());
//        }
//    }
//
//    protected void mapPhotoRect2(RectF src, RectF dst) {
//        if (photoBounds.isEmpty()) {
//            dst.setEmpty();
//        } else {
//            photoMatrix.mapRect(dst, src);
//        }
//    }
//
//    public RectF getDisplayBounds() {
//        return displayBounds;
//    }
//
//    public void sizeChanged() {
//
//    }
//}
