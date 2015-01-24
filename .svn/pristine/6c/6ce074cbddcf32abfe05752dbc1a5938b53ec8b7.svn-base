
package com.tencent.zebra.doodle;

import java.io.InputStream;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import com.tencent.zebra.util.log.ZebraLog;

/**
 * Fast bitmap drawable. Does not support states. it only support alpha and colormatrix
 * 
 * @author alessandro
 */
public class FastBitmapDrawable extends Drawable implements IBitmapDrawable {
    public static final String TAG = "FastBitmapDrawable";
    protected Bitmap mBitmap;
    protected Paint mPaint;

    public FastBitmapDrawable(Bitmap b) {
        mBitmap = b;
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setFilterBitmap(true);
    }

    public FastBitmapDrawable(Resources res, InputStream is) {
        try {
            mBitmap = BitmapFactory.decodeStream(is);
        } catch (OutOfMemoryError err) {
            ZebraLog.e(TAG, "FastBitmapDrawable", err);
            try {
                Options opts = new Options();
                opts.inPreferredConfig = Config.RGB_565;
                mBitmap = BitmapFactory.decodeStream(is, null, opts);
            } catch (OutOfMemoryError err1) {
                ZebraLog.e(TAG, "FastBitmapDrawable", err1);
            }
        }
        
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setFilterBitmap(true);
    }

    @Override
    public void draw(Canvas canvas) {
        if (null != mBitmap) {
            canvas.drawBitmap(mBitmap, 0.0f, 0.0f, mPaint);
        }
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    @Override
    public int getIntrinsicWidth() {
        if (null != mBitmap) {
            return mBitmap.getWidth();
        } else {
            return 0;
        }
    }

    @Override
    public int getIntrinsicHeight() {
        if (null != mBitmap) {
            return mBitmap.getHeight();
        } else {
            return 0;
        }
    }

    @Override
    public int getMinimumWidth() {
        if (null != mBitmap) {
            return mBitmap.getWidth();
        } else {
            return 0;
        }
    }

    @Override
    public int getMinimumHeight() {
        if (null != mBitmap) {
            return mBitmap.getHeight();
        } else {
            return 0;
        }
    }

    public void setAntiAlias(boolean value) {
        mPaint.setAntiAlias(value);
        invalidateSelf();
    }

    @Override
    public Bitmap getBitmap() {
        return mBitmap;
    }
}
