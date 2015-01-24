
package com.tencent.zebra.crop;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.tencent.photoplus.R;
import com.tencent.zebra.editutil.Util;
import com.tencent.zebra.effect.PhotoEffectActivity;
import com.tencent.zebra.util.log.ZebraLog;

public class CropImageView extends SurfaceView implements SurfaceHolder.Callback {
    // private static final int MASK_FOUR_THREE = 0;
    // private static final int MASK_THREE_FOUR = 1;
    public static final String TAG = "CropImageView";
    private int MASK_MARGIN = 40;
    private static final int MIN_INNER_SIZE = 64;
    // private float mDownX=INVALID;
    // private float mDownY=INVALID;
    private Matrix mMatrix;
    private Bitmap mBitmap;
    private String mPath;
    private boolean isFirstIn = true;
    private int mImageViewWidth;
    private int mImageViewHeight;
    private Activity mContext;
    private float mPicScale;
    private float mPicCenterX;
    private float mPicCenterY;
    // private int currentMask;
    private Paint paint = new Paint();
    private Rect mInnerRect;
    private Rect mImageBoundary;
    private Rect mImageRect;
    private Bitmap btSave;
    private boolean isHorizontal = true;// 初始定义为横向，为true
    private boolean setInitalDegree = false;
    // private float minScale;
    private int bitmapWidth;
    private int bitmapHeight;
    private float mInitialScale;

    private int baseWidth;
    private int baseHeight;
    private boolean mIsRotating = false;

    private Drawable mResizeDrawablePointer;
    private int mResizeCornerSize;

    private int mSavedWidth = 0;
    private int mSavedHeight = 0;

    public int getSavedWidth() {
        return mSavedWidth;
    }

    public int getSavedHeight() {
        return mSavedHeight;
    }

    // 是否可以点击确认按钮
    public boolean isConfirmAvailable() {
        return confirmAvailable;
    }

    private boolean confirmAvailable = true;

    public CropImageView(Activity PhotoEffectActivity, Bitmap bitmap,
            String path) {
        super(PhotoEffectActivity);

        mContext = PhotoEffectActivity;

        // new Thread(new Runnable() {
        //
        // @Override
        // public void run() {
        // try {
        // mBitmap = Util.getOrResizeBitmap(path, true);
        // mHandler.sendEmptyMessage(0);
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        // }
        // }).start();
        mBitmap = bitmap;
        mPath = path;
        mResizeDrawablePointer = mContext.getResources().getDrawable(R.drawable.zebra_crop_pointer);
        mResizeCornerSize = mContext.getResources().getDimensionPixelSize(R.dimen.zebra_crop_corner_size);
        MASK_MARGIN = mContext.getResources().getDimensionPixelSize(R.dimen.zebra_crop_pic_margin);

        getHolder().addCallback(this);
        init();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // TODO Auto-generated method stub
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mBitmap == null) {
            return;
        }
        if (isFirstIn) {
            initDrawParms();
            // MASK_MARGIN = MASK_MARGIN * mImageViewWidth / 480;
            isFirstIn = false;
        }
        reDraw();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    private void init() {
        setOnTouchListener(new OnTouchListener() {
            float baseDistance;
            float moveBaseX;
            float moveBaseY;
            HitWhichCorner hitResizeCorner;
            boolean moveInnerRect;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    // mDownX=event.getX();
                    // mDownY=event.getY();

                    moveBaseX = event.getX();
                    moveBaseY = event.getY();
                    hitResizeCorner = isHitResizeCornerOrEdge(event);
                    moveInnerRect = hitInnerRect(event);
                    baseDistance = 0;
                    confirmAvailable = false;
                    return true;
                }
                case MotionEvent.ACTION_POINTER_1_DOWN:
                case MotionEvent.ACTION_POINTER_2_DOWN: {
                    hitResizeCorner = HitWhichCorner.none;
                    moveBaseX = 0;
                    moveBaseY = 0;
                    return true;
                }
                case MotionEvent.ACTION_POINTER_1_UP:
                case MotionEvent.ACTION_POINTER_2_UP: {
                    moveInnerRect = false;
                    return true;
                }
                case MotionEvent.ACTION_UP: {
                    // if (mInitialScale > mPicScale) {
                    // scaleFrame();
                    // }
                    // updatePositon4Rect();
                    confirmAvailable = true;
                    hitResizeCorner = HitWhichCorner.none;
                    moveInnerRect = false;
                    return true;
                }
                case MotionEvent.ACTION_MOVE: {

                    if (event.getPointerCount() == 2 && hitResizeCorner == HitWhichCorner.none) {
                        float distanceX = event.getX(0) - event.getX(1);
                        float distanceY = event.getY(0) - event.getY(1);
                        float distance = (float) Math.sqrt(distanceX * distanceX + distanceY * distanceY);// 计算两点的距离

                        float moveX = (event.getX(0) + event.getX(1)) / 2;
                        float moveY = (event.getY(0) + event.getY(1)) / 2;

                        if (baseDistance == 0) {
                            baseDistance = distance;
                        } else if (moveBaseX == 0) {
                            moveBaseX = moveX;
                            moveBaseY = moveY;
                        } else {
                            if ((distance - baseDistance >= 10 || distance - baseDistance <= -10)) {
                                // 当前两点间的距离除以手指落下时两点间的距离就是需要缩放的比例。
                                float scale = distance / baseDistance;
                                baseDistance = distance;
                                float deltaWidth = mInnerRect.width() - scale * mInnerRect.width();
                                float deltaHeight = mInnerRect.height() - scale * mInnerRect.height();
                                reSizeHighlightView(HitWhichCorner.leftTop, deltaWidth / 2, deltaHeight / 2);
                                reSizeHighlightView(HitWhichCorner.rightBottom, -deltaWidth / 2, -deltaHeight / 2);
                                reDraw();
                            }/*
                              * if ((distance - baseDistance >= 10 || distance - baseDistance <= -10)) { //
                              * 当前两点间的距离除以手指落下时两点间的距离就是需要缩放的比例。 float scale = distance / baseDistance; baseDistance =
                              * distance; mMatrix.postScale(scale, scale, mPicCenterX, mPicCenterY); mPicScale =
                              * mPicScale*scale; reDraw(); } else { float deltaX = moveX - moveBaseX; float deltaY =
                              * moveY - moveBaseY; mMatrix.postTranslate(deltaX, deltaY); mPicCenterX += deltaX;
                              * mPicCenterY += deltaY; moveBaseX = moveX; moveBaseY = moveY; reDraw(); }
                              */
                        }
                    } // point_count=2
                    else if ((event.getPointerCount() == 1)) {
                        float moveX = event.getX();
                        float moveY = event.getY();

                        // 不超出边界的X,Y坐标最大最小值
                        float deltaX = moveX - moveBaseX;
                        float deltaY = moveY - moveBaseY;

                        if (hitResizeCorner != HitWhichCorner.none) {
                            reSizeHighlightView(hitResizeCorner, deltaX, deltaY);
                        } else if (moveInnerRect) {
                            reMoveHighlightView(deltaX, deltaY);
                            // mInnerRect.offset((int)deltaX, (int)deltaY);
                        } /*
                           * else { mMatrix.postTranslate(deltaX, deltaY); mPicCenterX += deltaX; mPicCenterY += deltaY;
                           * }
                           */

                        moveBaseX = moveX;
                        moveBaseY = moveY;
                        // 重绘图像
                        reDraw();
                    }
                    return true;
                }
                }
                return false;
            }
        });
    }

    // private void updatePositon4Rect(){
    // float deltaX = 0;
    // float deltaY = 0;
    // float maxCenterX = 0;
    // float minCenterX = 0;
    // float maxCenterY = 0;
    // float minCenterY = 0;
    //
    // if (mInnerRect == null) {
    // // If is null, then re-init.
    // initInnerRect();
    // }
    //
    // maxCenterX = mInnerRect.left + baseWidth * mPicScale / 2;
    // minCenterX = mInnerRect.right - baseWidth * mPicScale / 2;
    // maxCenterY = mInnerRect.top + baseHeight * mPicScale / 2;
    // minCenterY = mInnerRect.bottom - baseHeight * mPicScale / 2;
    // if (mPicCenterX > maxCenterX) {
    // deltaX = maxCenterX - mPicCenterX;
    // }
    // if (mPicCenterX <= minCenterX) {
    // deltaX = minCenterX - mPicCenterX;
    // }
    // if (mPicCenterY > maxCenterY) {
    // deltaY = maxCenterY - mPicCenterY;
    // }
    // if (mPicCenterY <= minCenterY) {
    // deltaY = minCenterY - mPicCenterY;
    // }
    //
    // mMatrix.postTranslate(deltaX, deltaY);
    // mPicCenterX+=deltaX;
    // mPicCenterY+=deltaY;
    //
    // reDraw();
    // }

    // private boolean isSmall(){
    // if((mInnerRect.width()>baseWidth*mPicScale)||(mInnerRect.height()>baseHeight*mPicScale))
    // return true;
    // else
    // return false;
    // }

    // 创建图片
    public void initDrawParms() {
        mImageViewWidth = ((PhotoEffectActivity) mContext).getImageViewWidth();
        mImageViewHeight = ((PhotoEffectActivity) mContext).getImageViewHeight();
        // MASK_MARGIN = MASK_MARGIN * mImageViewWidth / 480;

        mMatrix = new Matrix();
        mMatrix.setScale(1, 1);

        // 防空判断
        if (mBitmap == null) {
            return;
        }

        bitmapWidth = mBitmap.getWidth();
        bitmapHeight = mBitmap.getHeight();

        if (isHorizontal) {
            baseHeight = bitmapHeight;
            baseWidth = bitmapWidth;
        } else {
            baseWidth = bitmapHeight;
            baseHeight = bitmapWidth;
        }
        // if(baseWidth>baseHeight){
        // currentMask = MASK_FOUR_THREE;
        // }else{
        // currentMask = MASK_THREE_FOUR;
        // }
        // ((PhotoEffectActivity)mContext).setRatioIcon();
        // 初始化innerrect
        setMask();

        // mInnerRect = initInnerRect();

        // 缩放
        mPicScale = calculatePicScale(baseWidth, baseHeight,
                mImageViewWidth - MASK_MARGIN, mImageViewHeight - MASK_MARGIN);

        mInnerRect = initInnerRect();
        mImageBoundary = initInnerRect();

        mMatrix.postScale(mPicScale, mPicScale);

        mInitialScale = mPicScale;
        // 平移
        mPicCenterX = mImageViewWidth / 2;
        mPicCenterY = mImageViewHeight / 2;
        float moveX = mPicCenterX - bitmapWidth * mPicScale / 2;
        float moveY = mPicCenterY - bitmapHeight * mPicScale / 2;
        mMatrix.postTranslate(moveX, moveY);
        // mInitialHoriMatrix = new Matrix(mMatrix);
        // mInitialCenterX = mPicCenterX;
        // mInitialCenterY = mPicCenterY;
    }

    private float calculatePicScale(float bm_w, float bm_h, float im_w, float im_h) {
        float scale = 0f;
        // float bm_ratio = bm_h/ bm_w;
        // float base_ratio = im_h / im_w;
        // if (bm_ratio >= base_ratio) {
        // // 优先适配宽度
        // scale = im_w / bm_w;
        // } else {
        // // 优先适配高度
        // scale = im_h / bm_h;
        // }

        float widthScale = im_w / bm_w;
        float heightScale = im_h / bm_h;

        // if(widthScale > 1)
        // widthScale = 1;
        // if(heightScale > 1)
        // heightScale = 1;

        scale = Math.min(widthScale, heightScale);

        return scale;
    }

    // 创建图片
    public void reDraw() {
        SurfaceHolder holder = getHolder();
        holder.setFormat(PixelFormat.RGB_565);
        Canvas canvas = holder.lockCanvas(null);
        if (canvas != null && mBitmap != null) {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            canvas.drawBitmap(mBitmap, mMatrix, null);
            drawMask(canvas);
            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawMask(Canvas canvas) {
        paint.setColor(Color.BLACK);
        paint.setStyle(Style.FILL);
        paint.setAlpha(160);
        Paint paint2 = new Paint();
        paint2.setColor(Color.WHITE);
        paint2.setStyle(Style.STROKE);
        paint2.setStrokeWidth(5);
        paint2.setAlpha(255);
        canvas.save();
        setMask();
        canvas.clipRect(mInnerRect, Region.Op.XOR);
        canvas.drawRect(mImageRect, paint);

        Path path = new Path();
        path.moveTo(mInnerRect.left, mInnerRect.top);
        path.lineTo(mInnerRect.right, mInnerRect.top);
        path.lineTo(mInnerRect.right, mInnerRect.bottom);
        path.lineTo(mInnerRect.left, mInnerRect.bottom);
        path.close();
        canvas.drawPath(path, paint2);
        canvas.restore();

        canvas.save();
        drawResizeCorner(canvas);
        canvas.restore();

        canvas.save();
        // PathEffect effects = new DashPathEffect(new float[] { 4, 4, 4, 4}, 1);
        // paint2.setPathEffect(effects);
        paint2.setStrokeWidth(2);
        paint2.setAlpha(150);
        float startX = mInnerRect.left;
        float startY = mInnerRect.top + mInnerRect.height() / 3;
        float stopX = mInnerRect.right;
        float stopY = startY;
        canvas.drawLine(startX, startY, stopX, stopY, paint2);
        startX = mInnerRect.left;
        startY = mInnerRect.top + mInnerRect.height() / 3 * 2;
        stopX = mInnerRect.right;
        stopY = startY;
        canvas.drawLine(startX, startY, stopX, stopY, paint2);
        startX = mInnerRect.left + mInnerRect.width() / 3;
        startY = mInnerRect.top;
        stopX = startX;
        stopY = mInnerRect.bottom;
        canvas.drawLine(startX, startY, stopX, stopY, paint2);
        startX = mInnerRect.left + mInnerRect.width() / 3 * 2;
        startY = mInnerRect.top;
        stopX = startX;
        stopY = mInnerRect.bottom;
        canvas.drawLine(startX, startY, stopX, stopY, paint2);
        canvas.restore();

        // canvas.save();
        // Paint.FontMetrics metrics = paint2.getFontMetrics();
        // int textOffset = (int) metrics.bottom;
        // // String size = getSaveSize();
        // // float width = paint2.measureText(size);
        // canvas.drawText(getSaveSize(), 0, mInnerRect.top, paint2);
        // canvas.restore();
    }

    private void drawResizeCorner(Canvas canvas) {
        int size = mResizeCornerSize / 2; // 14 * mImageViewWidth / 480;
        mResizeDrawablePointer.setBounds(mInnerRect.left - size, mInnerRect.top - size,
                mInnerRect.left + size, mInnerRect.top + size);
        mResizeDrawablePointer.draw(canvas);

        mResizeDrawablePointer.setBounds(mInnerRect.right - size, mInnerRect.top - size,
                mInnerRect.right + size, mInnerRect.top + size);
        mResizeDrawablePointer.draw(canvas);

        mResizeDrawablePointer.setBounds(mInnerRect.left - size, mInnerRect.bottom - size,
                mInnerRect.left + size, mInnerRect.bottom + size);
        mResizeDrawablePointer.draw(canvas);

        mResizeDrawablePointer.setBounds(mInnerRect.right - size, mInnerRect.bottom - size,
                mInnerRect.right + size, mInnerRect.bottom + size);
        mResizeDrawablePointer.draw(canvas);
    }

    private void setMask() {
        if (null == mImageRect) {
            mImageRect = new Rect(0, 0, mImageViewWidth, mImageViewHeight);
        }
        // initInnerRect();
    }

    /**
     * Init inner rect.
     */
    private Rect initInnerRect() {

        Rect ret = null;

        // if (currentMask == MASK_FOUR_THREE) {// 4:3
        // int tmpHeight = (mImageViewWidth - 2 * MASK_MARGIN) * 3 / 4;
        // ret = new Rect(MASK_MARGIN, mImageViewHeight / 2 - tmpHeight / 2, mImageViewWidth - MASK_MARGIN,
        // mImageViewHeight / 2 + tmpHeight / 2);
        // } else {// 3:4
        // int tmpHeight = mImageViewHeight - 2 * MASK_MARGIN;
        // int tmpWidth = tmpHeight * 3 / 4;
        // if (tmpWidth > mImageViewWidth) {// 以高为基础算出来的宽度，是否大于屏宽，如果大，则重新计算
        // tmpWidth = mImageViewWidth - 2 * MASK_MARGIN;
        // tmpHeight = tmpWidth * 4 / 3;
        // }
        // ret = new Rect(mImageViewWidth / 2 - tmpWidth / 2, mImageViewHeight / 2 - tmpHeight / 2,
        // mImageViewWidth / 2 + tmpWidth / 2, mImageViewHeight / 2 + tmpHeight / 2);
        // }
        float tmpBmWidth = baseWidth * mPicScale;
        float tmpBmHeight = baseHeight * mPicScale;
        float top = mImageViewHeight / 2 - tmpBmHeight / 2 + 0.5f;
        float bottom = mImageViewHeight / 2 + tmpBmHeight / 2;
        float left = mImageViewWidth / 2 - tmpBmWidth / 2 + 0.5f;
        float right = mImageViewWidth / 2 + tmpBmWidth / 2;
        ret = new Rect((int) left, (int) top, (int) right, (int) bottom);
        return ret;
    }

    // private String getSaveSize() {
    // int rectWidth = (int) (mInnerRect.width() / mInitialScale);
    // int rectHeight = (int) (mInnerRect.height() / mInitialScale);
    // float scale1 = Math.min(1600.0f / Math.max(rectWidth, rectHeight), 1.0f);
    // float scale2 = Math.min(1200.0f / Math.min(rectWidth, rectHeight), 1.0f);
    // float scale = Math.min(scale1, scale2);
    // int saveWidth = (int) (scale * rectWidth);
    // int saveHeight = (int) (scale * rectHeight);
    // return saveWidth + "×" + saveHeight;
    // }

    public String saveImage2File() {
        // int memSize = ((ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();

        // if (mInnerRect == null) {
        // // if null, re-init.
        // mInnerRect = initInnerRect();
        // }
        //
        //
        // btSave = Bitmap.createBitmap(mDstBmp, mInnerRect.left, mInnerRect.top, mInnerRect.right - mInnerRect.left,
        // mInnerRect.bottom - mInnerRect.top);
        // if(null!=mDstBmp){//使用结束后回收
        // mDstBmp.recycle();
        // mDstBmp = null;
        // }

        try {
            Bitmap mDstBmp = getCropBitmap();
            // String path = AppBmpMgr.getInstance().getOriginFilePath();
            String path = Util.saveOutput(((PhotoEffectActivity) mContext).getThisActivity(), mPath, mDstBmp, true);
            if (mDstBmp != null)
                mDstBmp.recycle();
            // String path = Util.saveOutput(mContext, mpath, btSave, true);
            return path;
            // return Util.saveBitmap("test", btSave);
        } catch (Exception e) {
            return null;
        } catch (OutOfMemoryError err) {
            return null;
        }
    }

    public Bitmap getCropBitmap() {
        try {
            float scale1 = Math.min(1600.0f / Math.max(baseWidth, baseHeight), 1.0f);
            float scale2 = Math.min(1200.0f / Math.min(baseWidth, baseHeight), 1.0f);
            float scale = Math.min(scale1, scale2);
            int saveWidth = (int) (scale * baseWidth);
            int saveHeight = (int) (scale * baseHeight);
            mSavedWidth = saveWidth;
            mSavedHeight = saveHeight;
            Bitmap mDstBmp = Bitmap.createBitmap(saveWidth, saveHeight, Config.ARGB_8888);
            Canvas canvas = new Canvas(mDstBmp);
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            float moveX = saveWidth / 2 - bitmapWidth * scale / 2;
            float moveY = saveHeight / 2 - bitmapHeight * scale / 2;
            matrix.postTranslate(moveX, moveY);
            matrix.postRotate(totalAngle + 0.01f, saveWidth / 2, saveHeight / 2);
            if (mBitmap != null) {
                canvas.drawBitmap(mBitmap, matrix, null);
            }

            // String path = AppBmpMgr.getInstance().getOriginFilePath();
            // String path = Util.saveOutput(mContext.getThisActivity(), mpath,
            // mDstBmp, true);
            // if (mDstBmp != null)
            // mDstBmp.recycle();
            // String path = Util.saveOutput(mContext, mpath, btSave, true);
            return mDstBmp;
            // return Util.saveBitmap("test", btSave);
        } catch (OutOfMemoryError err) {
            ZebraLog.e(TAG, "getCropBitmap", err);
            return null;
        } catch (Exception e) {
            ZebraLog.e(TAG, "getCropBitmap", e);
            return null;
        }
    }
    /**
     * @param result 输出结果，0成功，-1失败，-2内存不足
     * @return
     */
    public void saveImage2Bitmap(int[] result) {
        // int memSize = ((ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
        try {
            Bitmap mDstBmp = Bitmap.createBitmap(mImageViewWidth, mImageViewHeight, Config.ARGB_8888);
            Canvas canvas = new Canvas(mDstBmp);
            if (mBitmap != null) {
                canvas.drawBitmap(mBitmap, mMatrix, null);
            }

            if (mInnerRect == null) {
                // if null, re-init.
                mInnerRect = initInnerRect();
            }

            btSave = Bitmap.createBitmap(mDstBmp, mInnerRect.left, mInnerRect.top, mInnerRect.right - mInnerRect.left,
                    mInnerRect.bottom - mInnerRect.top);
            if (null != mDstBmp) {
                // 使用结束后回收
                mDstBmp.recycle();
                mDstBmp = null;
            }
            if (null != mBitmap && !mBitmap.isRecycled()) {
                mBitmap.recycle();
                mBitmap = null;
            }
            mBitmap = btSave;
            isHorizontal = true;
            totalAngle = 0;
            initDrawParms();
            reDraw();
        } catch (OutOfMemoryError e) {
            ZebraLog.e(TAG, "[saveImage2Bitmap]", e);
            if (null != result) {
                result[0] = -2;
            }
        } catch (Exception e) {
            ZebraLog.e(TAG, "[saveImage2Bitmap]", e);
            if (null != result) {
                result[0] = -1;
            }
        }
    }

    public void setInitalAngle(int degree) {
        if (setInitalDegree)
            return;

        totalAngle += degree;
        totalAngle %= 360;
        int d = (degree / 90) % 2;
        if (d == 1)
            isHorizontal = !isHorizontal;

        setInitalDegree = true;
    }

    public void setBitmap(Bitmap bm) {
        if (null != mBitmap && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
        }
        mBitmap = bm;
        initDrawParms();
        // scaleFrame();
        mMatrix.postRotate(totalAngle + 0.01f, mPicCenterX, mPicCenterY);
        reDraw();
        // saveImage2Bitmap();
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public int totalAngle = 0;

    public float[] getCropMargin() {
        // 0 left 1 top 2 right 3 bottom
        float[] margin = new float[4];
        if (totalAngle == 90 || totalAngle == -270) {
            margin[3] = (mInnerRect.left - mImageBoundary.left) / (float) mImageBoundary.width();
            margin[0] = (mInnerRect.top - mImageBoundary.top) / (float) mImageBoundary.height();
            margin[1] = (mImageBoundary.right - mInnerRect.right) / (float) mImageBoundary.width();
            margin[2] = (mImageBoundary.bottom - mInnerRect.bottom) / (float) mImageBoundary.height();
        } else if (totalAngle == 270 || totalAngle == -90) {
            margin[1] = (mInnerRect.left - mImageBoundary.left) / (float) mImageBoundary.width();
            margin[2] = (mInnerRect.top - mImageBoundary.top) / (float) mImageBoundary.height();
            margin[3] = (mImageBoundary.right - mInnerRect.right) / (float) mImageBoundary.width();
            margin[0] = (mImageBoundary.bottom - mInnerRect.bottom) / (float) mImageBoundary.height();
        } else if (totalAngle == 180 || totalAngle == -180) {
            margin[2] = (mInnerRect.left - mImageBoundary.left) / (float) mImageBoundary.width();
            margin[3] = (mInnerRect.top - mImageBoundary.top) / (float) mImageBoundary.height();
            margin[0] = (mImageBoundary.right - mInnerRect.right) / (float) mImageBoundary.width();
            margin[1] = (mImageBoundary.bottom - mInnerRect.bottom) / (float) mImageBoundary.height();
        } else {
            margin[0] = (mInnerRect.left - mImageBoundary.left) / (float) mImageBoundary.width();
            margin[1] = (mInnerRect.top - mImageBoundary.top) / (float) mImageBoundary.height();
            margin[2] = (mImageBoundary.right - mInnerRect.right) / (float) mImageBoundary.width();
            margin[3] = (mImageBoundary.bottom - mInnerRect.bottom) / (float) mImageBoundary.height();
        }
        return margin;
    }

    /**
     * 旋转
     */
    public void rotate(int degree) {

        mIsRotating = true;
        // mMatrix.postRotate(90,mPicCenterX,mPicCenterY);
        totalAngle += degree;
        // if(right) {
        // totalAngle += 90;
        // } else {
        // totalAngle -= 90;
        // }
        totalAngle %= 360;
        isHorizontal = !isHorizontal;
        if (isHorizontal) {
            baseHeight = bitmapHeight;
            baseWidth = bitmapWidth;
        } else {
            baseWidth = bitmapHeight;
            baseHeight = bitmapWidth;
        }

        // reDraw();
        scaleFrame();
        // updatePositon4Rect();
        mIsRotating = false;
    }

    public boolean isRotating() {
        return mIsRotating;
    }

    /**
     * 改变模版方向
     */
    // public void changeMaskType(){
    // currentMask = (currentMask == MASK_FOUR_THREE) ? MASK_THREE_FOUR:MASK_FOUR_THREE;
    // reDraw();
    // updatePositon4Rect();
    // scaleFrame();
    // }

    /**
     * 旋转和比例调整时候，缩放适应内部矩形框
     */
    private void scaleFrame() {
        mMatrix = new Matrix();
        mMatrix.setScale(1, 1);
        mPicCenterX = mImageViewWidth / 2;
        mPicCenterY = mImageViewHeight / 2;
        float moveX = mPicCenterX - bitmapWidth / 2;
        float moveY = mPicCenterY - bitmapHeight / 2;
        mMatrix.postTranslate(moveX, moveY);
        switch (totalAngle) {
        case -270:
        case 90:
            mMatrix.postRotate(90, mPicCenterX, mPicCenterY);
            break;
        case -180:
        case 180:
            mMatrix.postRotate(180.1f, mPicCenterX, mPicCenterY);
            break;
        case -90:
        case 270:
            mMatrix.postRotate(270, mPicCenterX, mPicCenterY);
            break;
        }

        // Rect origiInnerRect = initInnerRect();

        mPicScale = calculatePicScale(baseWidth, baseHeight,
                mImageViewWidth - MASK_MARGIN, mImageViewHeight - MASK_MARGIN);

        mInnerRect = initInnerRect();
        mImageBoundary = initInnerRect();

        mMatrix.postScale(mPicScale, mPicScale, mPicCenterX, mPicCenterY);

        // float scaleX = (float) origiInnerRect.width() / baseWidth;
        // float scaleY = (float) origiInnerRect.height() / baseHeight;
        // if (scaleX >= scaleY) {
        // mMatrix.postScale(scaleX, scaleX,mPicCenterX,mPicCenterY);
        // mPicScale = scaleX;
        // } else {
        // mMatrix.postScale(scaleY, scaleY,mPicCenterX,mPicCenterY);
        // mPicScale = scaleY;
        // }
        // 重置初始scale值
        mInitialScale = mPicScale;
        reDraw();
    }

    /**
     * 返回是否是4：3模式
     */
    // public boolean beFourAndThree(){
    // return (currentMask==MASK_FOUR_THREE);
    // }

    public void recycleImage() {
        if (null != mBitmap && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
        }
        if (null != btSave && !btSave.isRecycled()) {
            btSave.recycle();
            btSave = null;
        }
        System.gc();
    }

    private HitWhichCorner isHitResizeCornerOrEdge(MotionEvent event) {

        if (event.getPointerCount() == 2)
            return HitWhichCorner.none;

        int delta = MASK_MARGIN / 2;// 30;

        Rect leftTop = new Rect(mInnerRect.left - delta, mInnerRect.top - delta,
                mInnerRect.left + delta, mInnerRect.top + delta);

        Rect rightTop = new Rect(mInnerRect.right - delta, mInnerRect.top - delta,
                mInnerRect.right + delta, mInnerRect.top + delta);

        Rect leftBottom = new Rect(mInnerRect.left - delta, mInnerRect.bottom - delta,
                mInnerRect.left + delta, mInnerRect.bottom + delta);

        Rect rightBottom = new Rect(mInnerRect.right - delta, mInnerRect.bottom - delta,
                mInnerRect.right + delta, mInnerRect.bottom + delta);

        Rect left = new Rect(mInnerRect.left - 2 * delta, mInnerRect.top + delta,
                mInnerRect.left, mInnerRect.bottom - delta);
        Rect right = new Rect(mInnerRect.right, mInnerRect.top + delta,
                mInnerRect.right + 2 * delta, mInnerRect.bottom - delta);
        Rect top = new Rect(mInnerRect.left + delta, mInnerRect.top - 2 * delta,
                mInnerRect.right - delta, mInnerRect.top);
        Rect bottom = new Rect(mInnerRect.left + delta, mInnerRect.bottom,
                mInnerRect.right - delta, mInnerRect.bottom + 2 * delta);

        if (leftTop.contains((int) event.getX(), (int) event.getY())) {
            return HitWhichCorner.leftTop;
        } else if (rightTop.contains((int) event.getX(), (int) event.getY())) {
            return HitWhichCorner.rightTop;
        } else if (leftBottom.contains((int) event.getX(), (int) event.getY())) {
            return HitWhichCorner.leftBottom;
        } else if (rightBottom.contains((int) event.getX(), (int) event.getY())) {
            return HitWhichCorner.rightBottom;
        } else if (left.contains((int) event.getX(), (int) event.getY())) {
            return HitWhichCorner.left;
        } else if (right.contains((int) event.getX(), (int) event.getY())) {
            return HitWhichCorner.right;
        } else if (top.contains((int) event.getX(), (int) event.getY())) {
            return HitWhichCorner.top;
        } else if (bottom.contains((int) event.getX(), (int) event.getY())) {
            return HitWhichCorner.bottom;
        }

        return HitWhichCorner.none;
    }

    public enum HitWhichCorner {
        none, leftTop, rightTop, leftBottom, rightBottom, left, top, right, bottom;
    }

    private boolean hitInnerRect(MotionEvent event) {
        return mInnerRect.contains((int) event.getX(), (int) event.getY());
    }

    private void reSizeHighlightView(HitWhichCorner which, float delta_x, float delta_y) {

        ((PhotoEffectActivity) mContext).setRightBottomBtnEnabled(true);
        ((PhotoEffectActivity) mContext).setRightBottomBtnText(R.string.zebra_cut_pic);
        ((PhotoEffectActivity) mContext).hasEdit = true;

        if (which == HitWhichCorner.leftTop) {
            int tempX = (int) (mInnerRect.left + delta_x);
            int tempY = (int) (mInnerRect.top + delta_y);
            if (tempX + MIN_INNER_SIZE < mInnerRect.right && tempX >= mImageBoundary.left) {
                mInnerRect.left += delta_x;
            }
            if (tempY + MIN_INNER_SIZE < mInnerRect.bottom && tempY >= mImageBoundary.top) {
                mInnerRect.top += delta_y;
            }
        } else if (which == HitWhichCorner.rightTop) {
            int tempX = (int) (mInnerRect.right + delta_x);
            int tempY = (int) (mInnerRect.top + delta_y);
            if (tempX - MIN_INNER_SIZE > mInnerRect.left && tempX <= mImageBoundary.right) {
                mInnerRect.right += delta_x;
            }
            if (tempY + MIN_INNER_SIZE < mInnerRect.bottom && tempY >= mImageBoundary.top) {
                mInnerRect.top += delta_y;
            }
        } else if (which == HitWhichCorner.leftBottom) {
            int tempX = (int) (mInnerRect.left + delta_x);
            int tempY = (int) (mInnerRect.bottom + delta_y);
            if (tempX + MIN_INNER_SIZE < mInnerRect.right && tempX >= mImageBoundary.left) {
                mInnerRect.left += delta_x;
            }
            if (tempY - MIN_INNER_SIZE > mInnerRect.top && tempY <= mImageBoundary.bottom) {
                mInnerRect.bottom += delta_y;
            }
        } else if (which == HitWhichCorner.rightBottom) {
            int tempX = (int) (mInnerRect.right + delta_x);
            int tempY = (int) (mInnerRect.bottom + delta_y);
            if (tempX - MIN_INNER_SIZE > mInnerRect.left && tempX <= mImageBoundary.right) {
                mInnerRect.right += delta_x;
            }
            if (tempY - MIN_INNER_SIZE > mInnerRect.top && tempY <= mImageBoundary.bottom) {
                mInnerRect.bottom += delta_y;
            }
        } else if (which == HitWhichCorner.left) {
            int tempX = (int) (mInnerRect.left + delta_x);
            if (tempX + MIN_INNER_SIZE < mInnerRect.right && tempX >= mImageBoundary.left) {
                mInnerRect.left += delta_x;
            }
        } else if (which == HitWhichCorner.right) {
            int tempX = (int) (mInnerRect.right + delta_x);
            if (tempX - MIN_INNER_SIZE > mInnerRect.left && tempX <= mImageBoundary.right) {
                mInnerRect.right += delta_x;
            }
        } else if (which == HitWhichCorner.top) {
            int tempY = (int) (mInnerRect.top + delta_y);
            if (tempY + MIN_INNER_SIZE < mInnerRect.bottom && tempY >= mImageBoundary.top) {
                mInnerRect.top += delta_y;
            }
        } else if (which == HitWhichCorner.bottom) {
            int tempY = (int) (mInnerRect.bottom + delta_y);
            if (tempY - MIN_INNER_SIZE > mInnerRect.top && tempY <= mImageBoundary.bottom) {
                mInnerRect.bottom += delta_y;
            }
        }

    }

    private void reMoveHighlightView(float delta_x, float delta_y) {

        int tempLeft = (int) (mInnerRect.left + delta_x);
        int tempRight = (int) (mInnerRect.right + delta_x);
        int tempTop = (int) (mInnerRect.top + delta_y);
        int tempBottom = (int) (mInnerRect.bottom + delta_y);

        float x = delta_x;
        if (tempLeft <= mImageBoundary.left) {
            x = mImageBoundary.left - mInnerRect.left;
        } else if (mImageBoundary.right <= tempRight) {
            x = mImageBoundary.right - mInnerRect.right;
        }

        float y = delta_y;
        if (tempTop <= mImageBoundary.top) {
            y = mImageBoundary.top - mInnerRect.top;
        } else if (mImageBoundary.bottom <= tempBottom) {
            y = mImageBoundary.bottom - mInnerRect.bottom;
        }

        mInnerRect.offset((int) x, (int) y);
    }

}
