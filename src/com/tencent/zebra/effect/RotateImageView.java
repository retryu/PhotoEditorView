package com.tencent.zebra.effect;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;



public class RotateImageView extends ImageView {


    private  static  final  String Tag= "RotateImageView";
    private static final int NONE = 0; // 初始状态
    private static final int ROTATE = 1; // 旋转
    // 动画时间200ms
    private static final int ZOOM_ANIM_DURATION = 200;
    private static final int ZOOM_IN_ANIM = 0X1001;// 放大
    private static final int ZOOM_OUT_ANIM = 0X1002;// 缩小

    private static final float ROTATE_SCALE = 7.2f;
    private static final float MIN_DEGREES = 25f;
    private static final float MAX_DEGREES = 335f;

    private static final float GRID_LINE_WIDTH = 2;

    private float mImageW;
    private float mImageH;
    private float mRotatedImageW;
    private float mRotatedImageH;
    private float mViewW;
    private float mViewH;
    private Matrix mMatrix = new Matrix();
    private Matrix mSavedMatrix = new Matrix();
    private int mMode;
    private PointF mPointB = new PointF();
    private PointF mPointCenter = new PointF();
    private double mRotation = 0.0;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Rect mInnerRect;
    private Rect mImageRect;
    private RotateBgImageView mBgImageView;
    private Matrix mTmpMatrix = new Matrix();
    private Matrix mScaleMatrix = new Matrix();
    // 限制在touch down的动画时候不能旋转
    private boolean mEnableFreeRotate = false;
    // Record the touch down time.
    private long mFirstTouchDownTime;
    // Record the touch up time.
    private long mFirstTouchUpTime;
    // 放大动画
    private ScaleAnimation mScaleAnim;
    // Record current animation type.
    private int mCurAnimType;
    private Matrix mFinalMatrix = new Matrix();
    private float mParentW;
    private float mParentH;
    private float last_delta_x = 0f;
    private float last_delta_y = 0f;
    private boolean hasRotate90Degrees = false;
    private HandleRotateListener mHandleRotateListener;
    private float mPreviousRotateDegrees;
    private PointF mPreviousPoint = new PointF();
    private float mActualPreviousRotateDegrees;
    private float mPreDegreesAfterUp;

    private boolean mCanInit = true;

    public interface HandleRotateListener {

        public void onRotate(float degrees);
    }

    private Handler mHandler = new Handler() {
        public void dispatchMessage(android.os.Message msg) {
            switch (msg.what) {
                case ZOOM_IN_ANIM:
                    mHandler.removeMessages(ZOOM_OUT_ANIM);
                    mCurAnimType = ZOOM_IN_ANIM;
                    startZoomIn();
                    break;
                case ZOOM_OUT_ANIM:
                    mCurAnimType = ZOOM_OUT_ANIM;
                    startZoomOut();
                    break;
            }
        };
    };

    class RotateAnimListener implements AnimationListener {

        @Override
        public void onAnimationEnd(Animation animation) {
            switch (mCurAnimType) {
                case ZOOM_IN_ANIM: // 放大
                    // 替换为原始matrix
                    mBgImageView.setAlpha(255);
                    mScaleMatrix.set(mMatrix);
                    setAlpha(0);
                    // 点击速度太快不做放大处理
                    if (mFirstTouchUpTime - mFirstTouchDownTime < 300) {
                        // 恢复innerRect大小
                        mInnerRect.left = 0;
                        mInnerRect.right = (int) mViewW;
                        mInnerRect.top = 0;
                        mInnerRect.bottom = (int) mViewH;
                        invalidate();
                        break;
                    }
                    int innerW = mInnerRect.width();
                    int innerH = mInnerRect.height();
                    float minScale = Math.min(mViewW / innerW, mViewH / innerH);
                    mScaleMatrix.postScale(minScale, minScale, mPointCenter.x, mPointCenter.y);
                    mBgImageView.setImageMatrix(mScaleMatrix);
                    int w = mInnerRect.width();
                    int h = mInnerRect.height();
                    w *= minScale;
                    h *= minScale;
                    mInnerRect.left = (int) (mViewW - w) / 2;
                    mInnerRect.right = (int) (mViewW + w) / 2;
                    mInnerRect.top = (int) (mViewH - h) / 2;
                    mInnerRect.bottom = (int) (mViewH + h) / 2;
                    invalidate();
                    break;
                case ZOOM_OUT_ANIM: // 缩小
                    setAlpha(255);
                    mBgImageView.setAlpha(0);
                    // 放大后的matrix
                    updateInnerRect(mMatrix);
                    setImageMatrix(mMatrix);
                    mBgImageView.setImageMatrix(mMatrix);
                    break;
            }
            mEnableFreeRotate = true;
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }

        @Override
        public void onAnimationStart(Animation animation) {
            mEnableFreeRotate = false;
        }
    }

    public RotateImageView(Context context) {
        super(context);
        init();
    }

    public RotateImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RotateImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void init() {
        setScaleType(ImageView.ScaleType.MATRIX);

    }

    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        setImageWidthHeight();
    }

    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        setImageWidthHeight();
    }

    public void setImageResource(int resId) {
        super.setImageResource(resId);
        setImageWidthHeight();
    }

    private void setImageWidthHeight() {
        Drawable d = getDrawable();
        if (d == null) {
            return;
        }
        mImageW = mRotatedImageW = d.getIntrinsicWidth();
        mImageH = mRotatedImageH = d.getIntrinsicHeight();

        mMatrix.setScale(0, 0);
        // fixScale();
        // bgIv.setImageMatrix(matrix);
        initImage();
    }

    public void setBgImage(RotateBgImageView bgIv) {
        mBgImageView = bgIv;
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mViewW = w;
        mViewH = h;
        if (oldw == 0 || oldh == 0) {
            mParentW = ((RelativeLayout) this.getParent()).getWidth();
            mParentH = ((RelativeLayout) this.getParent()).getHeight();
        }

        float view_ratio = (float) mParentW / (float) mParentH;
        float im_ratio = mImageW / mImageH;

        int l, t, r, b;
        if (im_ratio >= view_ratio) {
            // 横向铺满
            float newViewW = (mImageW <= mParentW ? mParentW : mViewW);
            float newViewH = newViewW * mImageH / mImageW;
            l = Math.round((mParentW - newViewW) / 2);
            t = Math.round((mParentH - newViewH) / 2);
            r = Math.round((mParentW + newViewW) / 2);
            b = Math.round((mParentH + newViewH) / 2);
//            mBgImageView.manulSetFrame(l, t, r, b);
//            setFrame(l, t, r, b);
        } else if (im_ratio < view_ratio) {
            // 纵向铺满
            float newViewH = mParentH;
            float newViewW = newViewH * mImageW / mImageH;
            l = Math.round((mParentW - newViewW) / 2);
            ;
            t = Math.round((mParentH - newViewH) / 2);
            r = Math.round((mParentW + newViewW) / 2);
            b = Math.round((mParentH + newViewH) / 2);
//            mBgImageView.manulSetFrame(l, t, r, b);
//            setFrame(l, t, r, b
        }
        initImage();
        mImageRect = new Rect(0, 0, (int) mViewW, (int) mViewH);
        mInnerRect = new Rect(0, 0, (int) mViewW, (int) mViewH);
    }

    private void initImage() {
        if (mViewW <= 0 || mViewH <= 0 || mImageW <= 0 || mImageH <= 0 || !mCanInit) {
            return;
        }
        mCanInit = false;

        float[] matrixValues = new float[9];
        mMatrix.getValues(matrixValues);
        double curAngle = Math.atan(matrixValues[3] / matrixValues[4]);
        mMode = NONE;
        fixScale();
        // fixTranslation();

        mBgImageView.setImageMatrix(mTmpMatrix);
        setImageMatrix(mMatrix);

        mBgImageView.setAlpha(255);
        setAlpha(0);
        mPointCenter.set(mViewW / 2, mViewH / 2);

        mScaleMatrix.set(mMatrix);
    }

    private void fixScale() {
        float p[] = new float[9];
        mMatrix.getValues(p);
        float curScale = Math.abs(p[0]) + Math.abs(p[1]);

        int viewW = getWidth();
        int viewH = getHeight();

        float minScale = Math.min((float) viewW / (float) mRotatedImageW, (float) viewH / (float) mRotatedImageH);
        if (curScale < minScale) {
            if (curScale > 0) {
            } else {
                mMatrix.setScale(minScale, minScale);
                mTmpMatrix.setScale(minScale, minScale);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:

                // 限制双击，保证动画完整。
                if (mFirstTouchDownTime != 0) {
                    long firstTouchDownTimeTmp = System.currentTimeMillis();
                    if (firstTouchDownTimeTmp - mFirstTouchDownTime < 400) {
                        return false;
                    }
                }
                mFirstTouchDownTime = System.currentTimeMillis();
                mSavedMatrix.set(mMatrix);
                // 旋转图层可见，展示图层不可见
                setAlpha(0);
                mBgImageView.setAlpha(255);
                // 展示缩小动画
                mHandler.sendEmptyMessage(ZOOM_OUT_ANIM);
                mPointB.set(event.getX(), event.getY());
                mPreviousPoint = new PointF(mPointB.x, mPointB.y);
                mMode = ROTATE;
                break;
            case MotionEvent.ACTION_UP:
                mFirstTouchUpTime = System.currentTimeMillis();
                mMode = NONE;
                mPreDegreesAfterUp = mActualPreviousRotateDegrees;
                mPreviousRotateDegrees = 0;
                mActualPreviousRotateDegrees = 0;
//            mHandler.sendEmptyMessage(ZOOM_IN_ANIM);
                break;
            case MotionEvent.ACTION_MOVE:
                // 限制旋转
                if (!mEnableFreeRotate) {
                    break;
                }

                if (mMode == ROTATE) {
                    PointF pC = new PointF(event.getX(), event.getY());
                    double a = spacing(mPointB.x, mPointB.y, pC.x, pC.y);
                    double b = spacing(mPointCenter.x, mPointCenter.y, pC.x, pC.y);
                    double c = spacing(mPointCenter.x, mPointCenter.y, mPointB.x, mPointB.y);

                    if (b > 10) {
                        double cosA = (b * b + c * c - a * a) / (2 * b * c);
                        double angleA = Math.acos(cosA);
                        double ta = mPointB.y - mPointCenter.y;
                        double tb = mPointCenter.x - mPointB.x;
                        double tc = mPointB.x * mPointCenter.y - mPointCenter.x * mPointB.y;
                        double td = ta * pC.x + tb * pC.y + tc;
                        if (td > 0) {
                            angleA = 2 * Math.PI - angleA;
                        }
                        mRotation = angleA;
                        double angle = mRotation * 180;
                        mMatrix.set(mSavedMatrix);
                        mMatrix.postRotate(-mPreDegreesAfterUp, mPointCenter.x, mPointCenter.y); // 先恢复原角度
                        float rotateDegrees = (float) (angle / Math.PI);

                        boolean clockwise = isClockwise(mPointCenter, mPreviousPoint, pC);
                        if (clockwise) {
                            float deltaDegree = getDegree(mPointCenter, mPreviousPoint, pC);
                            float curDegree = mPreviousRotateDegrees + deltaDegree;
                            if (curDegree >= 360) {
                                curDegree = curDegree % 360;
                            }
                            float curScaleDegree = 0f;
                            if (curDegree < 180) {
                                curScaleDegree = curDegree / ROTATE_SCALE;
                            } else {
                                if (mPreviousRotateDegrees < 180) {
                                    curScaleDegree = curDegree / ROTATE_SCALE;
                                } else {
                                    curScaleDegree = 360 - (360 - curDegree) / ROTATE_SCALE;
                                }
                            }
                            if (mActualPreviousRotateDegrees == MIN_DEGREES || Math.floor(mActualPreviousRotateDegrees) == MIN_DEGREES) {
                                rotateDegrees = MIN_DEGREES;
                                mPreviousRotateDegrees = 180;
                            } else if (curScaleDegree > MIN_DEGREES && curScaleDegree < 180) {
                                rotateDegrees = MIN_DEGREES;
                                mPreviousRotateDegrees = 180;
                            } else {
                                rotateDegrees = curScaleDegree;
                                mPreviousRotateDegrees = curDegree;
                            }
                            mActualPreviousRotateDegrees = rotateDegrees;
                        } else {
                            float deltaDegree = getDegree(mPointCenter, pC, mPreviousPoint);
                            float curDegree = mPreviousRotateDegrees - deltaDegree;
                            if (curDegree < 0) {
                                curDegree = 360 - curDegree;
                            }
                            float curScaleDegree = 0f;
                            if (curDegree < 180) {
                                if (mPreviousRotateDegrees > 180) {
                                    curScaleDegree = 360 - (360 - curDegree) / ROTATE_SCALE;
                                } else {
                                    curScaleDegree = curDegree / ROTATE_SCALE;
                                }
                            } else {
                                curScaleDegree = 360 - (360 - curDegree) / ROTATE_SCALE;
                            }
                            if (mActualPreviousRotateDegrees == MAX_DEGREES || Math.ceil(mActualPreviousRotateDegrees) == MAX_DEGREES) {
                                rotateDegrees = MAX_DEGREES;
                                mPreviousRotateDegrees = 180;
                            } else if (curScaleDegree < MAX_DEGREES && curScaleDegree > 180) {
                                rotateDegrees = MAX_DEGREES;
                                mPreviousRotateDegrees = 180;
                            } else {
                                rotateDegrees = curScaleDegree;
                                mPreviousRotateDegrees = curDegree;
                            }
                            mActualPreviousRotateDegrees = rotateDegrees;
                        }
                        if (mHandleRotateListener != null) {
                            mHandleRotateListener.onRotate(rotateDegrees);
                        }

                        mPreviousPoint.set(pC);


                        mMatrix.postRotate(rotateDegrees, mPointCenter.x, mPointCenter.y);
                        setImageMatrix(mMatrix);

                        updateInnerRect(mMatrix);

                        updateFinalMatrix(mInnerRect);
                    }

                }
                break;
        }
        return true;
    }

    /**
     * 两点的距离
     */
    private float spacing(float x1, float y1, float x2, float y2) {
        float x = x1 - x2;
        float y = y1 - y2;
        return FloatMath.sqrt(x * x + y * y);
    }

    private void updateInnerRect(Matrix matrix) {
        updateInnerRect(matrix, mViewW, mViewH);
        invalidate(mInnerRect);
    }

    private void updateInnerRect(Matrix matrix, float viewW, float viewH) {
        float[] matrixValues = new float[9];
        matrix.getValues(matrixValues);
        double curAngle = Math.atan(matrixValues[3] / matrixValues[4]);
        double angleAlpha = Math.abs(curAngle);

        float width;
        float height;
        float offsetX;
        float offsetY;
        if (hasRotate90Degrees) {
            angleAlpha = Math.PI / 2 - angleAlpha;
        }

        if (viewH > viewW) {
            width = viewW;
            height = viewH;
            offsetX = (viewW - width) / 2;
            offsetY = (viewH - height) / 2;
        } else {
            width = viewH;
            height = viewW;
            offsetX = (viewW - height) / 2;
            offsetY = (viewH - width) / 2;
        }
        double angleBeta = Math.atan(width / height);

        if (viewH > viewW) {
            calculateInnerRect(height, width, angleBeta, angleAlpha, offsetX, offsetY);
        } else {
            calculateInnerRect(width, height, angleBeta, angleAlpha, offsetX, offsetY);
        }
    }

    private void calculateInnerRect(float height, float width, double angleBeta, double angleAlpha, float offsetX, float offsetY) {

        double divider = Math.sin(angleAlpha + angleBeta);
        double param = Math.sin(angleBeta) / divider;

        double innerWidth = Math.abs(width * param);
        double innerHeight = Math.abs(height * param);
        int left = (int) ((width - innerWidth) / 2 + offsetX);
        int top = (int) ((height - innerHeight) / 2 + offsetY);
        int right = (int) (left + innerWidth);
        int bottom = (int) (top + innerHeight);
        mInnerRect = new Rect(left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(0x222631);
        mPaint.setStyle(Style.FILL);
        mPaint.setAlpha(178);
        canvas.save();
        canvas.clipRect(mInnerRect, Op.XOR);
        if (mInnerRect.left != 0 && mInnerRect.top != 0) {
            canvas.drawRect(mImageRect, mPaint);
        }
        canvas.restore();

        if (mMode == ROTATE) {
            mPaint.setColor(Color.WHITE);
            mPaint.setAlpha(255);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(GRID_LINE_WIDTH);
            float intervalX = (mInnerRect.width() - GRID_LINE_WIDTH * 6) / 5;
            float intervalY = (mInnerRect.height() - GRID_LINE_WIDTH * 6) / 5;
            for (int i = 0; i < 6; i++) {
                if (i == 5) {
                    canvas.drawLine(
                            mInnerRect.left,
                            mInnerRect.bottom,
                            mInnerRect.right,
                            mInnerRect.bottom,
                            mPaint);
                } else {
//                    canvas.drawLine(
//                            mInnerRect.left,
//                            mInnerRect.top + (intervalY + GRID_LINE_WIDTH) * i,
//                            mInnerRect.right,
//                            mInnerRect.top + (intervalY + GRID_LINE_WIDTH) * i,
//                            mPaint);
                }
            }
            for (int i = 0; i < 6; i++) {
                if (i == 5) {
                    canvas.drawLine(
                            mInnerRect.right,
                            mInnerRect.top,
                            mInnerRect.right,
                            mInnerRect.bottom,
                            mPaint);
                } else {
//                    canvas.drawLine(
//                            mInnerRect.left + (intervalX + GRID_LINE_WIDTH) * i,
//                            mInnerRect.top,
//                            mInnerRect.left + (intervalX + GRID_LINE_WIDTH) * i,
//                            mInnerRect.bottom,
//                            mPaint);
                }
            }
        }
    }

    public Rect getRotateInnerRect() {
        return mInnerRect;
    }

    public void rotate(int degrees) {
        float ratio_parent = mParentH / mParentW;
        float imWidth = (float) this.getWidth();
        float imHeight = (float) this.getHeight();

        float scale = 0f;
        float newWidth = 0f;
        float newHeight = 0f;
        float hTmp, wTmp;

        // swap w & h
        hTmp = imWidth;
        wTmp = imHeight;
        float ratio_view = hTmp / wTmp;

        int mid_x = 0;
        int mid_y = 0;

        float delta_x = 0;
        float delta_y = 0;

        mTmpMatrix.set(mMatrix);

        mTmpMatrix.postTranslate(-last_delta_x, -last_delta_y);
        mMatrix.postTranslate(-last_delta_x, -last_delta_y);

        if (ratio_parent >= ratio_view) {
            // 胖图片，始终是宽度适配屏幕
            newWidth = mParentW;
            newHeight = newWidth * hTmp / wTmp;

            scale = newWidth / wTmp;

            // 横向铺满，纵向留边
            if (newHeight < mParentH) {
                if (imHeight < mParentH) {
                    // 说明旋转之前也是胖图片，宽度适应屏幕
                    last_delta_x = delta_x = (mParentW - imWidth) / 2;
                    if (last_delta_y == 0) {
                        last_delta_y = delta_y = (newHeight - imHeight) / 2;
                        ;
                    } else {
                        last_delta_y = delta_y = 0;
                    }
                } else {
                    // 旋转前为瘦图片
                    if (mRotatedImageH < mRotatedImageW) {
                        last_delta_y = delta_y = 0;
                        last_delta_x = delta_x = /* (parentW - imWidth) / 2 */0;
                    } else if (mRotatedImageH > mRotatedImageW) {
                        last_delta_x = delta_x = (mParentW - imWidth) / 2;
                        last_delta_y = delta_y = -(mParentH - newHeight) / 2;
                    }
                }
            } else {
                last_delta_x = delta_x = (mParentW - imWidth) / 2;
                last_delta_y = delta_y = -(mParentH - newHeight) / 2;
            }
        } else {
            // 瘦图片处理，高度始终为屏幕高度
            newHeight = mParentH;
            newWidth = newHeight * wTmp / hTmp;

            scale = newHeight / hTmp;

            if (last_delta_x == 0 && last_delta_y == 0) {
                last_delta_x = delta_x = -(mParentW - newWidth) / 2;
                last_delta_y = delta_y = (mParentH - imHeight) / 2;
            } else {
                last_delta_x = delta_x = 0;
                last_delta_y = delta_y = 0;
            }
        }

        mid_x = (int) newWidth / 2;
        mid_y = (int) newHeight / 2;

        // real image widthheight
        mImageH = newHeight;
        mImageW = newWidth;

        int l, t, r, b;
//        l = 0;
//        r = (int) mParentW;
//        t = 0;
//        b = (int) mParentH;
        l = (int) (mParentW - newWidth) / 2;
        t = (int) (mParentH - newHeight) / 2;
        r = (int) (mParentW + newWidth) / 2;
        b = (int) (mParentH + newHeight) / 2;

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mBgImageView.setLayoutParams(params);
        // bgIv.layout(l, t, r, b);
        mBgImageView.manulSetFrame(l, t, r, b);
        // layout(l, t, r, b);
        setFrame(l, t, r, b);
        mPointCenter.set(newWidth / 2, newHeight / 2);

        /************************************************************/
        // 平移到中心
        mTmpMatrix.postTranslate(delta_x, delta_y);
        mMatrix.postTranslate(delta_x, delta_y);
        /******************************/
        // 旋转角度
        mTmpMatrix.postRotate(degrees, mid_x, mid_y);
        mMatrix.postRotate(degrees, mid_x, mid_y);
        /******************************/
        // 缩放
        mTmpMatrix.postScale(scale, scale, mid_x, mid_y);
        mMatrix.postScale(scale, scale, mid_x, mid_y);

        /************************************************************/
        // 如果做了自由旋转，有图片缩放，这里需要处理图片缩放
        hasRotate90Degrees = !hasRotate90Degrees;
        // 更新innerRect大小，做缩放准备
        updateInnerRect(mMatrix, newWidth, newHeight);
        float innerScale = Math.min(newWidth / mInnerRect.width(), newHeight / mInnerRect.height());
        mTmpMatrix.postScale(innerScale, innerScale, mid_x, mid_y);

        setImageMatrix(mMatrix);

        mBgImageView.setImageMatrix(mTmpMatrix);

        updateFinalMatrix(mInnerRect);

        // 重置innerRect大小
        mInnerRect.left = 0;
        mInnerRect.right = (int) newWidth;
        mInnerRect.top = 0;
        mInnerRect.bottom = (int) newHeight;
    }

    // 翻转
    public void flip(boolean vertical) {
        mTmpMatrix.set(mMatrix);

        if (vertical) {
            // 横向翻转
            mTmpMatrix.postScale(1, -1, mPointCenter.x, mPointCenter.y);
        } else {
            // 纵向翻转
            mTmpMatrix.postScale(-1, 1, mPointCenter.x, mPointCenter.y);
        }
        // 放大之前设置旋转imageview的matrix
        mMatrix.set(mTmpMatrix);

        updateInnerRect(mMatrix, mViewW, mViewH);
        float innerScale = Math.min(mViewW / mInnerRect.width(), mViewH / mInnerRect.height());
        mTmpMatrix.postScale(innerScale, innerScale, mPointCenter.x, mPointCenter.y);

        setImageMatrix(mMatrix);
        mBgImageView.setImageMatrix(mTmpMatrix);

        updateFinalMatrix(mInnerRect);

        // 重置innerRect大小
        mInnerRect.left = 0;
        mInnerRect.right = (int) mViewW;
        mInnerRect.top = 0;
        mInnerRect.bottom = (int) mViewH;
    }

    // 放大
    private void startZoomIn() {
        updateInnerRect(mMatrix);
        int innerW = mInnerRect.width();
        int innerH = mInnerRect.height();

        float minScale = Math.min(mViewW / innerW, mViewH / innerH);
        mScaleAnim = new ScaleAnimation(1.0f, minScale, 1.0f, minScale, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mScaleAnim.setInterpolator(new DecelerateInterpolator());
        mScaleAnim.setDuration(ZOOM_ANIM_DURATION);
        mScaleAnim.setAnimationListener(new RotateAnimListener());
        this.startAnimation(mScaleAnim);
    }

    // 缩小
    private void startZoomOut() {
        updateInnerRect(mMatrix);
        int innerW = mInnerRect.width();
        int innerH = mInnerRect.height();

        float minScale = Math.min(mViewW / innerW, mViewH / innerH);
        mScaleAnim = new ScaleAnimation(1.0f, 1 / minScale, 1.0f, 1 / minScale, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mScaleAnim.setInterpolator(new DecelerateInterpolator());
        mScaleAnim.setDuration(ZOOM_ANIM_DURATION);
        mScaleAnim.setAnimationListener(new RotateAnimListener());
        mBgImageView.startAnimation(mScaleAnim);
    }

    private void updateFinalMatrix(Rect innerRect) {
        mFinalMatrix.set(mMatrix);
        int innerW = innerRect.width();
        int innerH = innerRect.height();
        float minScale = Math.min(mViewW / innerW, mViewH / innerH);
        mFinalMatrix.postScale(minScale, minScale, mPointCenter.x, mPointCenter.y);
    }

    public Matrix getFinalMatrix() {
        return mFinalMatrix;
    }

    public void turningRotate(float degrees, float oldDegrees) {
        mMatrix.postRotate(-oldDegrees, mPointCenter.x, mPointCenter.y);
        mMatrix.postRotate(degrees, mPointCenter.x, mPointCenter.y);
        setImageMatrix(mMatrix);
        updateInnerRect(mMatrix);
        Rect innerRect = new Rect(mInnerRect);
        update();
        updateFinalMatrix(innerRect);
        mPreDegreesAfterUp = degrees;
        invalidate();
    }

    private void update() {
        // 替换为原始matrix
        mBgImageView.setAlpha(255);
        mScaleMatrix.set(mMatrix);
        setAlpha(0);
        int innerW = mInnerRect.width();
        int innerH = mInnerRect.height();
        float minScale = Math.min(mViewW / innerW, mViewH / innerH);
//        mScaleMatrix.postScale(minScale, minScale, mPointCenter.x, mPointCenter.y);
        mBgImageView.setImageMatrix(mScaleMatrix);
        int w = mInnerRect.width();
        int h = mInnerRect.height();
        w *= minScale;
        h *= minScale;
        mInnerRect.left = (int) (mViewW - w) / 2;
        mInnerRect.right = (int) (mViewW + w) / 2;
        mInnerRect.top = (int) (mViewH - h) / 2;
        mInnerRect.bottom = (int) (mViewH + h) / 2;
        invalidate();
    }

    public void setHandleRotateListener(HandleRotateListener listener) {
        mHandleRotateListener = listener;
    }

    private boolean isClockwise(PointF a, PointF b, PointF c) {
        return ((b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x)) > 0;
    }

    private float getDegree(PointF pointCenter, PointF pointB, PointF pointC) {
        double a = spacing(pointB.x, pointB.y, pointC.x, pointC.y);
        double b = spacing(pointCenter.x, pointCenter.y, pointC.x, pointC.y);
        double c = spacing(pointCenter.x, pointCenter.y, pointB.x, pointB.y);
        if (b > 10) {
            double cosA = (b * b + c * c - a * a) / (2 * b * c);
            double angleA = Math.acos(cosA);
            double ta = pointB.y - pointCenter.y;
            double tb = pointCenter.x - pointB.x;
            double tc = pointB.x * pointCenter.y - pointCenter.x * pointB.y;
            double td = ta * pointC.x + tb * pointC.y + tc;
            if (td > 0) {
                angleA = 2 * Math.PI - angleA;
            }
            double angle = angleA * 180;
            float rotateDegrees = (float) (angle / Math.PI);
            return rotateDegrees;
        }
        return 0;
    }

    public void resetPreviousDegrees() {
        mPreDegreesAfterUp = 0;
    }
}
