/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tencent.zebra.effect.utils;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.tencent.util.LogUtil;
import com.tencent.view.Photo;
import com.tencent.view.RendererUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Renders and displays photo in the surface view.
 */
public class PhotoView extends GLSurfaceView {

    private final static String TAG = "PhotoView";
    private final PhotoRenderer renderer;
    public boolean mRenderEnabled = false;
    private Callback mCallback;

    ScaleGestureDetector mScaleGestureDetector;
    private Object animScaleLock = new Object();
    private Object animAlphaLock = new Object();

    private static float zoom_init = 1.0f; //初始化放大系数
    private static float zoom_min = 1.0f;  //缩小时最小允许系数
    private static float zoom_min_pad = 0.5f;//缩小最小系数

    private static float zoom_max = 3.0f; //放大最大允许系数
    private static float zoom_max_pad = 4.5f;//放大最大系数

    private final static float ZOOM_MIN_PARM = 1.0f;  //缩小时最小允许系数
    private final static float ZOOM_MIN_PAD_PARM = 0.5f;//缩小最小系数

    private final static float ZOOM_MAX_PARM = 4.0f; //放大最大允许系数
    private final static float ZOOM_MAX_PAD_PARM = 6.0f;//放大最大系数

    private boolean mEnableScaleGesture = false;
    private int mAnimationType = 0;

    private int mBackgroundColor = -1;

    public void setEnableScaleGesture(boolean enable) {
        if (mEnableScaleGesture != enable && !enable) {
            startResetAnimotion();
        }

        mEnableScaleGesture = enable;
    }

    ScaleGestureDetector.SimpleOnScaleGestureListener mSimpleOnScaleGestureListener = new  ScaleGestureDetector.SimpleOnScaleGestureListener() {

        private int mFocusBitmapX;
        private int mFocusBitmapY;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            RectF srcR = getCurrentPhotoBound();
            float scale = detector.getScaleFactor();

            if (Float.isNaN(scale) || Float.isInfinite(scale)) return true;

            float focusX = detector.getFocusX();
            float focusY = detector.getFocusY();

            float newZoom = renderer.mZoom * scale;

            // We want to keep the focus point (on the bitmap) the same as when
            // we begin the scale guesture, that is,
            //
            // mCurrentX' + (focusX - mViewW / 2f) / scale = mFocusBitmapX


            if (newZoom < zoom_min_pad) {
                renderer.mZoom = zoom_min_pad;
            } else if (newZoom > zoom_max_pad) {
                renderer.mZoom = zoom_max_pad;
            } else {
                renderer.mZoom = newZoom;
            }

            renderer.currentX = Math.round(mFocusBitmapX - (focusX - renderer.viewWidth / 2f) / newZoom);
            renderer.currentY = Math.round(mFocusBitmapY - (focusY - renderer.viewHeight / 2f) / newZoom);


            if (renderer.currentX > renderer.photo.width() * 3 / 4) {
                renderer.currentX = renderer.photo.width() * 3 / 4;
            } else if (renderer.currentX < renderer.photo.width() / 4) {
                renderer.currentX = renderer.photo.width() / 4;
            }

            if (renderer.currentY > renderer.photo.height() * 3 / 4) {
                renderer.currentY = renderer.photo.height() * 3 / 4;
            } else if (renderer.currentY < renderer.photo.height() / 4) {
                renderer.currentY = renderer.photo.height() / 4;
            }

            if (renderer.mZoom * renderer.photo.height() <= renderer.viewHeight) {
                renderer.currentY = renderer.photo.height() / 2;
            }

            if (renderer.mZoom * renderer.photo.width() <= renderer.viewWidth) {
                renderer.currentX = renderer.photo.width() / 2;
            }

            RectF desR = getCurrentPhotoBound();


            starTransformAnim(srcR, desR, 30);

            return true;
        }

        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mFocusBitmapX = Math.round(renderer.currentX + (detector.getFocusX() - renderer.viewWidth / 2f) / renderer.mZoom);
            mFocusBitmapY = Math.round(renderer.currentY + (detector.getFocusY() - renderer.viewHeight / 2f) / renderer.mZoom);
            return true;
        }

        ;

        public void onScaleEnd(ScaleGestureDetector detector) {

            startResetAnimotion();
        }

        ;
    };

    private RectF getCurrentPhotoBound() {
        final RectF desR = new RectF();

        float vW = renderer.viewWidth;
        float vH = renderer.viewHeight;
        float zoom = renderer.mZoom;

        float panX = (vW / 2 - renderer.currentX * zoom + renderer.photo.width() * zoom / 2) / vW;
        float panY = (vH / 2 - renderer.currentY * zoom + renderer.photo.height() * zoom / 2) / vH;

        float bitmapW = renderer.photo.width() * renderer.mZoom / renderer.viewWidth;
        float bitmapH = renderer.photo.height() * renderer.mZoom / renderer.viewHeight;

        desR.left = panX - bitmapW / 2;
        desR.top = panY - bitmapH / 2;

        desR.right = bitmapW + desR.left;
        desR.bottom = bitmapH + desR.top;

        return desR;
    }

    private float getBitmapLeft() {
        return renderer.viewWidth / 2 - renderer.mZoom * renderer.currentX;
    }

    private float getBitmapTop() {
        return renderer.viewHeight / 2 - renderer.mZoom * renderer.currentY;
    }

    private float getBitmapRight() {
        return getBitmapLeft() + renderer.photo.width() * renderer.mZoom;
    }

    private float getBitmapBottom() {
        return getBitmapTop() + renderer.photo.height() * renderer.mZoom;
    }

    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
    }

    private void startResetAnimotion() {

        RectF srcR = getCurrentPhotoBound();
        int duration = 100;

        if (renderer.mZoom < zoom_min) {
            renderer.mZoom = zoom_min;
            renderer.currentX = renderer.photo.width() / 2;
            renderer.currentY = renderer.photo.height() / 2;
        } else {

            if (renderer.mZoom > zoom_max) {
                renderer.mZoom = zoom_max;
            }

            if (renderer.mZoom * renderer.photo.width() <= renderer.viewWidth) {
                renderer.currentX = renderer.photo.width() / 2;
            } else if (getBitmapLeft() > 0) {
                renderer.currentX = (int) (renderer.viewWidth / (2 * renderer.mZoom));
            } else if (getBitmapRight() < renderer.viewWidth) {
                renderer.currentX = (int) (renderer.photo.width() - renderer.viewWidth / (2 * renderer.mZoom));
            }


            if (renderer.mZoom * renderer.photo.height() <= renderer.viewHeight) {
                renderer.currentY = renderer.photo.height() / 2;
            } else if (getBitmapTop() > 0) {
                renderer.currentY = (int) (renderer.viewHeight / (2 * renderer.mZoom));
            } else if (getBitmapBottom() < renderer.viewHeight) {
                renderer.currentY = (int) (renderer.photo.height() - renderer.viewHeight / (2 * renderer.mZoom));
            }
        }


        RectF desR = getCurrentPhotoBound();
        starTransformAnim(srcR, desR, duration);
    }

    private void initZoom() {
        float initZoom;
        float wsacle = (float) renderer.viewWidth / (float) (renderer.photo.width());
        float hsacle = (float) renderer.viewHeight / (float) (renderer.photo.height());
        if (wsacle > hsacle) {
            initZoom = hsacle;
        } else {
            initZoom = wsacle;
        }

        zoom_init = initZoom;
        zoom_min = initZoom * ZOOM_MIN_PARM;
        zoom_min_pad = initZoom * ZOOM_MIN_PAD_PARM;
        zoom_max = initZoom * ZOOM_MAX_PARM;
        zoom_max_pad = initZoom * ZOOM_MAX_PAD_PARM;

        renderer.mZoom = zoom_init;
        renderer.currentX = renderer.photo.width() / 2;
        renderer.currentY = renderer.photo.height() / 2;
    }


    public interface Callback {
        void onPhotoViewChange(RectF rect);
    }

    public PhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);

        renderer = new PhotoRenderer();
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 0, 0);// fixed:
//        setZOrderOnTop(false);
        setRenderer(renderer);
        getHolder().setFormat(PixelFormat.RGBA_8888);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        mScaleGestureDetector = new ScaleGestureDetector(context,
                mSimpleOnScaleGestureListener);
    }

    public RectF getPhotoBounds() {
        RectF photoBounds;
        synchronized (renderer.photoBounds) {
            photoBounds = new RectF(renderer.photoBounds);
        }
        return photoBounds;
    }

    public RectF getAnimationBounds() {
        RectF animBounds;
        synchronized (renderer.animPhotoBounds) {
            animBounds = new RectF(renderer.animPhotoBounds);
        }

        return animBounds;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    /**
     * Queues a runnable and renders a frame after execution. Queued runnables
     * could be later removed by remove() or flush().
     */
    public void queue(Runnable r) {
        renderer.queue.add(r);
        requestRender();
    }

    /**
     * Removes the specified queued runnable.
     */
    public void remove(Runnable runnable) {
        renderer.queue.remove(runnable);
    }

    /**
     * Flushes all queued runnables to cancel their execution.
     */
    public void flush() {
        renderer.queue.clear();
    }

    /**
     * Sets photo for display; this method must be queued for GL thread.
     */
    public void setPhoto(Photo photo, boolean clearTransform) {
        renderer.setPhoto(photo, clearTransform);
    }

    /**
     * Rotates displayed photo; this method must be queued for GL thread.
     */
    public void rotatePhoto(float degrees) {
        renderer.rotatePhoto(degrees);
    }

    public void scalePhoto(RectF rect) {
        renderer.scalePhoto(rect);
    }

    public void alphaPhoto(int alpha) {
        renderer.alphaPhoto(alpha);
    }

    public boolean updatePhotoBounds(int width, int height) {
        return renderer.updatePhotoBounds(width, height);
    }

    public void setAnimationPhoto(Photo photo) {
        renderer.setAnimationPhoto(photo);
    }

    public void starTransformAnim(RectF srcR, RectF desR, long duration) {

        synchronized (animScaleLock) {

            if (renderer.animScaleStartTime <= 0) {
                renderer.animSrcR.set(srcR);
            } else {
                renderer.animSrcR.set(renderer.tempR);
            }


            renderer.animDesR.set(desR);
            renderer.animScaleStartTime = SystemClock.uptimeMillis();
            renderer.animScaleDuration = duration;
        }

        //scalePhoto(srcR);

        requestRender();
    }

    public void startAlphaAnim(float start, float end, long duration, Runnable callback) {
        synchronized (animAlphaLock) {
            renderer.mStartAlpha = start;
            renderer.mEndAlpha = end;
            renderer.animAlphaStartTime = SystemClock.uptimeMillis();
            renderer.animAlphaDuration = duration;
            renderer.alphaPhoto((int) (start * 255));
            renderer.mAlphaCallback = callback;
        }

        requestRender();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (mEnableScaleGesture)
            mScaleGestureDetector.onTouchEvent(event);
        else
            return false;
        return true;

    }

    private void andvanceScaleAnim(float progress) {

        float left = renderer.animSrcR.left + progress * (renderer.animDesR.left - renderer.animSrcR.left);
        float right = renderer.animSrcR.right + progress * (renderer.animDesR.right - renderer.animSrcR.right);
        float top = renderer.animSrcR.top + progress * (renderer.animDesR.top - renderer.animSrcR.top);
        float bottom = renderer.animSrcR.bottom + progress * (renderer.animDesR.bottom - renderer.animSrcR.bottom);

        renderer.tempR.set(left, top, right, bottom);


        renderer.scalePhoto(renderer.tempR);
    }

    private void advanceAlphaAnim(float progress) {
        renderer.alphaPhoto((int) (((renderer.mEndAlpha - renderer.mStartAlpha) * progress + renderer.mStartAlpha) * 255));
    }

    /**
     * Flips displayed photo; this method must be queued for GL thread.
     */
    public void flipPhoto(float horizontalDegrees, float verticalDegrees) {
        renderer.flipPhoto(horizontalDegrees, verticalDegrees);
    }

    public Photo getPhoto() {
        return renderer.photo;
    }

    /**
     * Renderer that renders the GL surface-view and only be called from the GL
     * thread.
     */
    private class PhotoRenderer implements Renderer {

        final Vector<Runnable> queue = new Vector<Runnable>();
        final RectF photoBounds = new RectF();
        final RectF animPhotoBounds = new RectF();
        RendererUtils.RenderContext renderContext;
        Photo photo;
        Photo animationPhoto;
        int viewWidth;
        int viewHeight;
        float rotatedDegrees;
        float flippedHorizontalDegrees;
        float flippedVerticalDegrees;
        List<RectF> mRectList = new ArrayList<RectF>();

        RectF animSrcR = new RectF();
        RectF animDesR = new RectF();
        RectF tempR = new RectF();

        long animScaleStartTime = -1;
        long animScaleDuration;

        long animAlphaStartTime = -1;
        long animAlphaDuration = -1;
        Runnable mAlphaCallback;

        RectF currentRect = new RectF();

        float mZoom = 1.0f;
        int currentX = 0;
        int currentY = 0;

        float mStartAlpha;
        float mEndAlpha;

        void setAnimationPhoto(Photo animPhoto) {
            if (animPhoto == null) {
                animationPhoto = null;
            } else {
                animationPhoto = animPhoto;
                animPhotoBounds.set(0, 0, animPhoto.width(), animPhoto.height());
                RendererUtils.setRenderToFit(renderContext, animPhoto.width(),
                        animPhoto.height(), viewWidth, viewHeight);
            }
        }

        void setPhoto(Photo photo, boolean clearTransform) {
            int width = (photo != null) ? photo.width() : 0;
            int height = (photo != null) ? photo.height() : 0;

            boolean changed = updatePhotoBounds(width, height);

            this.photo = photo;
            updateSurface(clearTransform, changed);

            if (photo != null) {
                initZoom();
            }
        }

        boolean updatePhotoBounds(int width, int height) {
            boolean changed;
            synchronized (photoBounds) {
                changed = (photoBounds.width() != width)
                        || (photoBounds.height() != height);
                LogUtil.d(TAG, "setPhoto(), photoBounds before = "+ photoBounds.toString());
                if (changed) {
                    photoBounds.set(0, 0, width, height);
                }
                LogUtil.d(TAG, "setPhoto(), photoBounds after = "+ photoBounds.toString());
            }
            if (mCallback != null) {
                mCallback.onPhotoViewChange(photoBounds);
            }
            return changed;
        }

        void updateSurface(boolean clearTransform, boolean sizeChanged) {
            boolean flipped = (flippedHorizontalDegrees != 0)
                    || (flippedVerticalDegrees != 0);
            boolean transformed = (rotatedDegrees != 0) || flipped || mRectList.size() > 0;
            if ((clearTransform && transformed)
                    || (sizeChanged && !transformed)) {
                // Fit photo when clearing existing transforms or changing
                // surface/photo sizes.
                if (photo != null) {
                    RendererUtils.setRenderToFit(renderContext, photo.width(),
                            photo.height(), viewWidth, viewHeight);
                    rotatedDegrees = 0;
                    flippedHorizontalDegrees = 0;
                    flippedVerticalDegrees = 0;
                    mRectList.clear();
                }
            } else {
                // Restore existing transformations for orientation changes or
                // awaking from sleep.
                if (rotatedDegrees != 0) {
                    rotatePhoto(rotatedDegrees);
                } else if (flipped) {
                    flipPhoto(flippedHorizontalDegrees, flippedVerticalDegrees);
                } else if (mRectList.size() > 0) {
                    scalePhoto(mRectList.get(0));
                    synchronized (mRectList) {
                        mRectList.remove(0);
                    }
                }
            }
        }

        void rotatePhoto(float degrees) {
            if (photo != null) {
                RendererUtils.setRenderToRotate(renderContext, photo.width(),
                        photo.height(), viewWidth, viewHeight, degrees);
                rotatedDegrees = degrees;
            }
        }

        void flipPhoto(float horizontalDegrees, float verticalDegrees) {
            if (photo != null) {
                RendererUtils.setRenderToFlip(renderContext, photo.width(),
                        photo.height(), viewWidth, viewHeight,
                        horizontalDegrees, verticalDegrees);
                flippedHorizontalDegrees = horizontalDegrees;
                flippedVerticalDegrees = verticalDegrees;
            }
        }

        void scalePhoto(RectF dstRect) {
            if (animationPhoto != null) {
                RendererUtils.setRenderToScale(renderContext, /*animationPhoto.width(),
                        animationPhoto.height(), viewWidth, viewHeight,*/
                        dstRect);
            } else if (photo != null) {
//				synchronized(mRectList) {
//					mRectList.add(new RectF(dstRect));
//				}
                RendererUtils.setRenderToScale(renderContext, /*photo.width(),
                        photo.height(), viewWidth, viewHeight,*/
                        dstRect);
            }
            requestRender();
        }

        void alphaPhoto(int alpha) {
            if (animationPhoto != null) {
                RendererUtils.setRenderToAlpha(renderContext, alpha);
            } else if (photo != null) {
                RendererUtils.setRenderToAlpha(renderContext, alpha);
            }
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            Runnable r = null;
            synchronized (queue) {
                if (!queue.isEmpty()) {
                    r = queue.remove(0);
                }
            }
            if (r != null) {
                r.run();
            }
            if (!queue.isEmpty()) {
                requestRender();
            }

            RendererUtils.renderBackground();
            if (animationPhoto != null && mRenderEnabled) {
                RendererUtils.renderTexture(renderContext,
                        animationPhoto.texture(), viewWidth, viewHeight);
            } else if (photo != null && mRenderEnabled) {
                RendererUtils.renderTexture(renderContext,
                        photo.texture(), viewWidth, viewHeight);
            }

            boolean needRender = false;

            synchronized (queue) {
                if (!queue.isEmpty()) {
                    needRender = true;
                }
            }

            synchronized (animScaleLock) {
                if (renderer.animScaleStartTime > 0) {
                    float f = (SystemClock.uptimeMillis() - renderer.animScaleStartTime) / (float) renderer.animScaleDuration;

                    if (f > 1.0f) {
                        f = 1.0f;
                        renderer.animScaleStartTime = -1;
                    }

                    float progress = 1 - (1 - f) * (1 - f);

                    andvanceScaleAnim(progress);

                    needRender = true;
                }
            }

            synchronized (animAlphaLock) {
                if (renderer.animAlphaStartTime > 0) {
                    float f = (SystemClock.uptimeMillis() - renderer.animAlphaStartTime) / (float) renderer.animAlphaDuration;

                    if (f > 1.0f) {
                        f = 1.0f;
                        renderer.animAlphaStartTime = -1;
                        if (mAlphaCallback != null) {
                            post(mAlphaCallback);
                        }
                    }

                    float progress = 1 - (1 - f) * (1 - f);

                    advanceAlphaAnim(progress);

                    needRender = true;
                }
            }

            if (needRender) {
                requestRender();
            }
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            viewWidth = width;
            viewHeight = height;
            updateSurface(false, true);
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            // GLSLRender.nativeRenderInit();
            RendererUtils.createFrame();
            renderContext = RendererUtils.createProgram();
        }
    }
}
