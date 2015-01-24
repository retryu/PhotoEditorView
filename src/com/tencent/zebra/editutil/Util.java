package com.tencent.zebra.editutil;

/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *r
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.view.View.OnClickListener;

import com.tencent.zebra.ui.MonitoredActivity;
//import com.tencent.zebra.util.SdkUtils;
import com.tencent.zebra.util.ZebraProgressDialog;
import com.tencent.zebra.util.log.ZebraLog;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;


/**
 * Collection of utility functions used in this package.
 */
public class Util {
    private static final String    TAG = "Util";
    
    private static final String SDCARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final String PIC_STORAGE_ROOT_PATH = File.separator + "tencent" + File.separator + "zebra";
//    private static final String PIC_STORAGE_ROOT_PATH = File.separator + "tencent" + File.separator + "QQ\u5F71\u50CF";
    private static final String PIC_CACHE_PATH = SDCARD_PATH + PIC_STORAGE_ROOT_PATH + File.separator + "cache";
    private static final String PIC_SAVE_PATH = SDCARD_PATH + PIC_STORAGE_ROOT_PATH;

    private static boolean debugmode = true;
    
    private static OnClickListener sNullOnClickListener;

    private Util() {
    }
    
	public static void DisplayInfo(String info){
		if (debugmode){
			ZebraLog.d("QPik", info);
		}

	}
    
//    public static Bitmap getBitmap(Context mContext, String path) {
//        Uri uri = getImageUri(path);
//        InputStream in = null;
//        try {
//            in = mContext.getContentResolver().openInputStream(uri);
//            return BitmapFactory.decodeStream(in);
//        } catch (FileNotFoundException e) {
//            Log.e(TAG, "file " + path + " not found");
//        }
//        return null;
//    }

    // Rotates the bitmap by the specified degree.
    // If a new bitmap is created, the original bitmap is recycled.
    public static Bitmap rotate(Bitmap b, int degrees) {
        if (b != null) {
            Matrix matrix = new Matrix();
            matrix.setRotate(degrees, (float) b.getWidth() / 2, (float) b.getHeight() / 2);
            try {
                Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
                if (b != b2) {
                    b.recycle();
                    b = b2;
                }
            } catch (OutOfMemoryError ex) {
                // We have no memory to rotate. Return the original bitmap.
            	ZebraLog.e(TAG, "OutOfMemoryError. ", ex);
            }
        }
        return b;
    }


    public static Bitmap rotateNotRecyle(Bitmap b, int degrees) {
        Bitmap b2 = null;
        if (b != null) {
            Matrix matrix = new Matrix();
            matrix.setRotate(degrees, (float) b.getWidth() / 2, (float) b.getHeight() / 2);
            try {
                 b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);

            } catch (OutOfMemoryError ex) {
                // We have no memory to rotate. Return the original bitmap.
                ZebraLog.e(TAG, "OutOfMemoryError. ", ex);
            }
        }
        return b2;
    }
    
    /*
     * Compute the sample size as a function of minSideLength and
     * maxNumOfPixels. minSideLength is used to specify that minimal width or
     * height of a bitmap. maxNumOfPixels is used to specify the maximal size in
     * pixels that are tolerable in terms of memory usage.
     * 
     * The function returns a sample size based on the constraints. Both size
     * and minSideLength can be passed in as IImage.UNCONSTRAINED, which
     * indicates no care of the corresponding constraint. The functions prefers
     * returning a sample size that generates a smaller bitmap, unless
     * minSideLength = IImage.UNCONSTRAINED.
     */

    public static Bitmap transform(Matrix scaler, Bitmap source, int targetWidth, int targetHeight, boolean scaleUp) {
        int deltaX = source.getWidth() - targetWidth;
        int deltaY = source.getHeight() - targetHeight;
        if (!scaleUp && (deltaX < 0 || deltaY < 0)) {
            /*
             * In this case the bitmap is smaller, at least in one dimension, than the target. Transform it by placing
             * as much of the image as possible into the target and leaving the top/bottom or left/right (or both)
             * black.
             */
            Bitmap b2 = null;
            try {
                b2 = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
            } catch (OutOfMemoryError e) {
                ZebraLog.e(TAG, "transform", e);
                try {
                    b2 = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.RGB_565);
                } catch (OutOfMemoryError e1) {
                    ZebraLog.e(TAG, "transform", e1);
                    return null;
                }
            }
            
            Canvas c = new Canvas(b2);
            int deltaXHalf = Math.max(0, deltaX / 2);
            int deltaYHalf = Math.max(0, deltaY / 2);
            Rect src = new Rect(deltaXHalf, deltaYHalf, deltaXHalf + Math.min(targetWidth, source.getWidth()),
                    deltaYHalf + Math.min(targetHeight, source.getHeight()));
            int dstX = (targetWidth - src.width()) / 2;
            int dstY = (targetHeight - src.height()) / 2;
            Rect dst = new Rect(dstX, dstY, targetWidth - dstX, targetHeight - dstY);
            c.drawBitmap(source, src, dst, null);
            return b2;
        }
        float bitmapWidthF = source.getWidth();
        float bitmapHeightF = source.getHeight();

        float bitmapAspect = bitmapWidthF / bitmapHeightF;
        float viewAspect = (float) targetWidth / targetHeight;

        if (bitmapAspect > viewAspect) {
            float scale = targetHeight / bitmapHeightF;
            if (scale < .9F || scale > 1F) {
                scaler.setScale(scale, scale);
            } else {
                scaler = null;
            }
        } else {
            float scale = targetWidth / bitmapWidthF;
            if (scale < .9F || scale > 1F) {
                scaler.setScale(scale, scale);
            } else {
                scaler = null;
            }
        }

        Bitmap b1;
        if (scaler != null) {
            // this is used for minithumb and crop, so we want to filter here.
            b1 = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), scaler, true);
        } else {
            b1 = source;
        }

        int dx1 = Math.max(0, b1.getWidth() - targetWidth);
        int dy1 = Math.max(0, b1.getHeight() - targetHeight);

        Bitmap b2 = Bitmap.createBitmap(b1, dx1 / 2, dy1 / 2, targetWidth, targetHeight);

        if (b1 != source) {
            b1.recycle();
        }

        return b2;
    }

    public static Bitmap extractMiniThumb(Bitmap source, int width, int height, boolean recycle) {
        if (source == null) {
            return null;
        }

        float scale;
        if (source.getWidth() < source.getHeight()) {
            scale = width / (float) source.getWidth();
        } else {
            scale = height / (float) source.getHeight();
        }
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        Bitmap miniThumbnail = transform(matrix, source, width, height, false);

        if (recycle && miniThumbnail != source) {
            source.recycle();
        }
        return miniThumbnail;
    }

    public static void closeSilently(Closeable c) {
        if (c == null)
            return;
        try {
            c.close();
        } catch (Throwable t) {
            // do nothing
        }
    }

    private static class BackgroundJob extends MonitoredActivity.LifeCycleAdapter implements Runnable {

        private final MonitoredActivity mActivity;
        private final ProgressDialog    mDialog;
        private final Runnable          mJob;
        private final Handler           mHandler;
        private final Runnable          mCleanupRunner = new Runnable() {
                                                           public void run() {
                                                               mActivity.removeLifeCycleListener(BackgroundJob.this);
                                                               if (mDialog.getWindow() != null)
                                                                   mDialog.dismiss();
                                                           }
                                                       };

        public BackgroundJob(MonitoredActivity activity, Runnable job, ProgressDialog dialog, Handler handler) {
            mActivity = activity;
            mDialog = dialog;
            mJob = job;
            mActivity.addLifeCycleListener(this);
            mHandler = handler;
        }

        public void run() {
            try {
                mJob.run();
            } finally {
                mHandler.post(mCleanupRunner);
            }
        }

        @Override
        public void onActivityDestroyed(MonitoredActivity activity) {
            // We get here only when the onDestroyed being called before
            // the mCleanupRunner. So, run it now and remove it from the queue
            mCleanupRunner.run();
            mHandler.removeCallbacks(mCleanupRunner);
        }

        @Override
        public void onActivityStopped(MonitoredActivity activity) {
            mDialog.hide();
        }

        @Override
        public void onActivityStarted(MonitoredActivity activity) {
            mDialog.show();
        }
    }

    public static void startBackgroundJob(MonitoredActivity activity, String title, String message, Runnable job,
            Handler handler) {
        // Make the progress dialog uncancelable, so that we can gurantee
        // the thread will be done before the activity getting destroyed.
        ProgressDialog dialog;
//        if (message != null) {
            dialog = ZebraProgressDialog.show(activity.getThisActivity(), title, message, true, false);
//        } else {
//            dialog = new QPickPrgsDialog(activity);
//            dialog.show();
//        }
        new Thread(new BackgroundJob(activity, job, dialog, handler)).start();
    }
    
    // 大图最大长边
    private static final int MAX_BIG_BMP_LONG_SIDE = 1600;
    // 小图最大短边
    private static final int MAX_BIG_BMP_SHORT_SIDE = 1200;
    // 大图最大长边
    private static final int MAX_SMALL_BMP_LONG_SIDE = 640;
    // 小图最大短边
    private static final int MAX_SMALL_BMP_SHORT_SIDE = 480;
    
    
    public static int getExifDegree(String path) {
    	int ret = 0;
        try{
	        ExifInterface exif = new ExifInterface(path);
			if ( exif != null ) {
				int orientation_rotate = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
				switch(orientation_rotate) {
	    			case ExifInterface.ORIENTATION_ROTATE_90:
	    				ret = 90;
	    				break;
	    			case ExifInterface.ORIENTATION_ROTATE_180:
	    				ret = 180;
	    				break;
	    			case ExifInterface.ORIENTATION_ROTATE_270:
	    				ret = 270;//270;经研究表明270系统自动帮助翻转？
	    				break;
				}
			}
        }catch(Exception e){
        	
        }
        return ret;
    }
    
    /**
     * @param path
     * @param bigBitmap
     * @param result 存放返回值，0成功，-1失败，-2内存不足
     * @return
     */
    public static Bitmap getOrResizeBitmap(String path, boolean bigBitmap, int[] result) {
        // Log.i("wjy","------------------------------------------------");
        Size size = getBmpSize(path);
        int degree = getExifDegree(path);

        float longSideRatio = ((float) size.height / (float) size.width);
        // 确定长短边
        int longSide, shortSide = 0;
        if (longSideRatio >= 1.0) {
            longSide = size.height;
            shortSide = size.width;
        } else {
            longSide = size.width;
            shortSide = size.height;
        }
        boolean shouldResize = false;
        Bitmap.Config myInPreferredConfig;
        if (bigBitmap) {
            if (shortSide < MAX_BIG_BMP_SHORT_SIDE && longSide < MAX_BIG_BMP_LONG_SIDE) {
                shouldResize = false;
            } else {
                shouldResize = true;
                size = getNewSize(shortSide, longSide, MAX_BIG_BMP_SHORT_SIDE, MAX_BIG_BMP_LONG_SIDE);
            }
            // myInPreferredConfig=Config.ARGB_8888;
            myInPreferredConfig = Config.RGB_565;
        } else {
            if (shortSide < MAX_SMALL_BMP_SHORT_SIDE && longSide < MAX_SMALL_BMP_LONG_SIDE) {
                shouldResize = false;
            } else {
                shouldResize = true;
                size = getNewSize(shortSide, longSide, MAX_SMALL_BMP_SHORT_SIDE, MAX_SMALL_BMP_LONG_SIDE);
            }
            myInPreferredConfig = Config.RGB_565;
        }

        File file = new File(path);
        if (!file.exists()) {
            return null;
        }

        Bitmap bitmap = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            int sampleSize = 1;
            // ZebraLog.d(TAG, "[getOrResizeBitmap] ### options.width=" + options.outWidth + " options.height="
            // + options.outHeight);
            // ZebraLog.d(TAG, "[getOrResizeBitmap] ### size.width=" + size.width + " size.height=" + size.height);
            if (shouldResize) {
                sampleSize = calculateInSampleSize(options, size.width, size.height);
            } else {
                // 不要resize
                sampleSize = 1;
            }
            // 获取内存总量
            long totalMem = 1000;
            try {
                totalMem = getTotalMemory();
            } catch (Exception e) {
            }
            // 获取内存总量_复查
            long totalMem_doubleCheck = 1000;
            try {
                totalMem_doubleCheck = getTotalInternalMemorySize();
            } catch (Exception e) {
            }
            // 获取CPU主频
            int cpuFreq = 2000;
            try {
                cpuFreq = Integer.parseInt(getMaxCpuFreq()) / 1000;
            } catch (Exception e) {
            }
            // 获取CPU核心数
            int cpuNum = 4;
            try {
                getNumCores();
            } catch (Exception e) {
            }
            // Log.i("CheckInfo","wjy:% cpuNum ="+cpuNum+",cpuFreq="+cpuFreq+",mem1="+totalMem+",mem2="+totalMem_doubleCheck);

            options.inPreferredConfig = myInPreferredConfig;
            options.inJustDecodeBounds = false;
            options.inSampleSize = sampleSize;

            // ZebraLog.d(TAG, "[getOrResizeBitmap] options.inSampleSize =" + options.inSampleSize + "; path=" + path);

            bitmap = BitmapFactory.decodeFile(path, options);
            if (degree != 0) {
                bitmap = rotate(bitmap, degree);
            }

            int tmpLongSide = Math.max(bitmap.getWidth(), bitmap.getHeight());
            int tmpShortSide = Math.min(bitmap.getWidth(), bitmap.getHeight());
            int baseLong = tmpLongSide;
            int baseShort = tmpShortSide;
            if (totalMem < 500 || (totalMem_doubleCheck < 500) || (cpuFreq < 800) || (cpuNum < 2))
            {
                // Log.i("wjy", "wjy:::low memory");
                baseLong = MAX_SMALL_BMP_LONG_SIDE;
                baseShort = MAX_SMALL_BMP_SHORT_SIDE;
            }
            else
            {
                // Log.i("wjy", "wjy:::high memory");
                baseLong = MAX_BIG_BMP_LONG_SIDE;
                baseShort = MAX_BIG_BMP_SHORT_SIDE;
            }
            if (tmpLongSide > baseLong || tmpShortSide > baseShort) {
                float scale = Math.min((float) baseLong / tmpLongSide, (float) baseShort / tmpShortSide);
                Matrix matrix = new Matrix();
                matrix.setScale(scale, scale);
                Bitmap dstBmp = Bitmap.createBitmap((int) (scale * bitmap.getWidth()),
                        (int) (scale * bitmap.getHeight()), myInPreferredConfig);
                Canvas canvas = new Canvas(dstBmp);
                if (bitmap != null) {
                    canvas.drawBitmap(bitmap, matrix, null);
                    if (!bitmap.isRecycled())
                    {
                        bitmap.recycle();
                        bitmap = null;
                    }
                }
                if (dstBmp.getConfig() == Config.RGB_565) {
                    // Log.i("wjy","wjy:dstBmp Config.RGB_565");
                } else if (dstBmp.getConfig() == Config.ARGB_8888) {
                    // Log.i("wjy","wjy:dstBmp Config.ARGB_8888");
                }
                // Log.i("wjy","wjy$$$ dstBmp.getWidth()="+dstBmp.getWidth()+" dstBmp.getHeight()="+dstBmp.getHeight());
                if (null != result) {
                    result[0] = 0;
                }
                return dstBmp;
            } else {
                if (bitmap.getConfig() == Config.RGB_565) {
                    // Log.i("wjy","wjy:bitmap Config.RGB_565");
                } else if (bitmap.getConfig() == Config.ARGB_8888) {
                    // Log.i("wjy","wjy:bitmap Config.ARGB_8888");
                }
                // Log.i("wjy","wjy$$$ bitmap.getWidth()="+bitmap.getWidth()+" bitmap.getHeight()="+bitmap.getHeight());
                if (null != result) {
                    result[0] = 0;
                }
                return bitmap;
            }
        } catch (Exception e) {
            ZebraLog.e(TAG, "Error in decode bitmap", e);
            if (null != result) {
                result[0] = -1;
            }
            return null;
        } catch (java.lang.OutOfMemoryError outOfMemory) {
            ZebraLog.e(TAG, "OutOfMemoryError in decode bitmap", outOfMemory);
            if (null != result) {
                result[0] = -2;
            }
            return null;
        }
    }
    
    //计算samplesize缩放比例
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = calculateInSampleSize(width, height, reqWidth, reqHeight);
        return inSampleSize;
    }
    
    public static int calculateInSampleSize(int width, int height, int reqWidth, int reqHeight) {
        // Raw height and width of image
//        final int height = options.outHeight;
//        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger
            // inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down
            // further.
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }
    
    static Size getNewSize(int orgWidth, int orgHeight, int destWidth, int destHeight) {
        float simpleSize = Math.min(destWidth / (float) orgWidth, destHeight / (float) orgHeight);
        if (simpleSize < 1.0) {
            return new Size((int) ((float) orgWidth * simpleSize), (int) ((float) orgHeight * simpleSize));
        } else {
            return new Size(orgWidth, orgHeight);
        }
    }
    
    public static Size getBmpSize(String path) {
        if (path == null) {
            return null;
        }
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        return new Size(options.outWidth, options.outHeight);
    }

    public static Boolean  isExist(String path){
        File file = new File(path);
        return file.exists();
    }
    
    public static class Size {
        public int width;

        public int height;

        public Size(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }
    
    // sd卡是否装载
    public boolean isSdCardExist() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }
    
    // 获取图片cache路径
    public static File getPicCachePath() {
        File file = new File(PIC_CACHE_PATH);
        if (!file.exists()) {
            try {
                File parentFile = file.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                file.mkdirs();
            } catch (Exception e) {
            	ZebraLog.e(TAG, "Make dir failed.");
                return null;
            }
        }
        return file;
    }
    
    // 获取图片存储路径
    public static File getPicSavePath(Context mContext) {
    	String savePath = getSavePath(mContext);
        File file = new File(savePath);
        if (!file.exists()) {
            try {
                File parentFile = file.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                file.mkdirs();
            } catch (Exception e) {
            	ZebraLog.e(TAG, "Make dir failed.");
                return null;
            }
        }
        return file;
    }
    
    // 取出路径中的文件名
    public static String getFileName(String imagePath) {
        if (isStringEmpty(imagePath)) {
            return null;
        }
        int index = imagePath.lastIndexOf(File.separator);
        String fileName = imagePath.substring(index);
        return fileName;
    }
    
    public static boolean isStringEmpty(String str) {
        if (str != null && str.trim().length() != 0) {
            return false;
        }
        return true;
    }
    
    /**
     * 保存图片到指定目录
     * 注：此处的mImagePath应该是原始图片的路径, 以获取原始文件名称。 
     * 可以通过AppBmpMgr.getInstance().getOriginFilePath()获取.
     * @param context
     * @param mImagePath 
     * @param bitmap
     * @param forCache
     */
    public static String saveOutput(Context context, String mImagePath, Bitmap bitmap, boolean forCache) {
//        if (mImagePath == null) {
//        	mImagePath = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
//        }
        
        // for renren lower bound
        int srcWidth, srcHeight;
        Bitmap scaledBmp;
        
        srcWidth = bitmap.getWidth();
        srcHeight = bitmap.getHeight();
        
        if( srcWidth <= 50 ) {
            scaledBmp = Bitmap.createScaledBitmap(bitmap, 60, srcHeight, true);
        } else {
            scaledBmp = bitmap;
        }
        
        srcWidth = scaledBmp.getWidth();
        srcHeight = scaledBmp.getHeight();
        
        if( srcHeight <= 50 ) {
            scaledBmp = Bitmap.createScaledBitmap(scaledBmp, srcWidth, 60, true);
        } else {
            ;
        }
        
        String fileName = getFileName(mImagePath);
        if(fileName == null)
        	fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        int index = fileName.lastIndexOf('.');
        String prefix;
        String suffix;

//        if (forCache) {
//            prefix = fileName.substring(0, index - 1);
//            suffix = "";
//            // Rename file
//            fileName = prefix + suffix;
//        } else {
//            prefix = fileName.substring(0, index - 1);
//            suffix = fileName.substring(index);
//        }
        if (-1 == index){
            prefix = fileName;
        } else {
            prefix = fileName.substring(0, index);
        }
        suffix = "png";
        
        File cacheFolder = null;
        if (forCache) {
            cacheFolder = getPicCachePath();
        } else {
            cacheFolder = getPicSavePath(context);
        }
        File file = new File(cacheFolder, fileName);
        int counter = 1;

        while (file.exists()) {
            
//            Log.d(TAG, "while...");
//            if(forCache){
//                fileName = fileName + "_" + counter + suffix;
//            }else{
//                fileName = prefix + "_" + counter + suffix;
//            }
            
            fileName = prefix + "_" + counter + "." + suffix;
            file = new File(cacheFolder, fileName);
            counter++;
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
        	ZebraLog.e(TAG, "create file error", e);
        }
        
//        if (forCache) {
//            AppBmpMgr.getInstance().saveCacheFilePath(file.getAbsolutePath());
//            AppBmpMgr.getInstance().cacheBmp(file.getAbsolutePath(), scaledBmp);
//        }
        
        Uri saveUri = Uri.fromFile(file);
        if (saveUri != null) {
            OutputStream outputStream = null;
            try {
                outputStream = context.getContentResolver().openOutputStream(saveUri);
                if (outputStream != null) {
                    scaledBmp.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
                }
            } catch (IOException ex) {
                // TODO: report error to caller
            	ZebraLog.e(TAG, "Cannot open file: " + saveUri, ex);
            } finally {
                Util.closeSilently(outputStream);
            }
        } else {
        	ZebraLog.e(TAG, "neni definovana adresa pro ulozeni");
        }
        
        if( !forCache ) {
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+Environment.getExternalStorageDirectory())));
        }
        
        return saveUri.getPath();

    }

    /**
	 * 获取保存路径 add by zhenhai
	 * @param mContext
	 * @return
	 */
	public static String getSavePath(Context mContext){
//		SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(mContext);
//		return preference.getString(SettingsActivity.SAVE_PATH, PIC_SAVE_PATH);
		return PIC_SAVE_PATH;
	}
	
    /**
     * 获取app vesioncode add by zhenhaiwu
     * @return
     */
//	public static int getAppVersionCode(Context mContext) {
//		try {
////			PackageInfo pinfo = mContext.getPackageManager().getPackageInfo(
////					"com.tencent.qpik", PackageManager.GET_CONFIGURATIONS);
////			return pinfo.versionCode;
//		    return SdkUtils.SDK_VERSION_CODE;
//		} catch (Exception e) {
//
//		}
//		return 0;
//	}
	
	/**
	 * 获取app vesionName add by zhenhaiwu
	 * @param context
	 * @return
	 */
//	public static String getAppVersionName(Context context){
//		try{
////			PackageInfo pinfo = context.getPackageManager().getPackageInfo("com.tencent.qpik", 0);
////			return pinfo.versionName;
//		    return SdkUtils.SDK_VERSION;
//		}catch(Exception e){
//			
//		}
//		return null;
//	}
	
	public static int getTotalMemory() {  
        String str1 = "/proc/meminfo";  
        String str2="";  
        String[] arrayOfString;
        int initial_memory=0;
        try {  
            FileReader fr = new FileReader(str1);  
            BufferedReader localBufferedReader = new BufferedReader(fr, 8192);  
            if ((str2 = localBufferedReader.readLine()) != null) {  
            	ZebraLog.i(TAG, "---" + str2);  
                
                arrayOfString=str2.split("\\s+");
                for(String num:arrayOfString){
                	ZebraLog.i(str2,num+"\t");
                }
                initial_memory=Integer.valueOf(arrayOfString[1]).intValue()>>10;
            }  
        } catch (IOException e) {  
        }  
        return initial_memory;
    }  
    
    /**
     * Gets the number of cores available in this device, across all processors.
     * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
     * @return The number of cores, or 1 if failed to get result
     */
    public static int getNumCores() {
        //Private Class to display only CPU devices in the directory listing
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                //Check if filename is "cpu", followed by a single digit number
                if(Pattern.matches("cpu[0-9]", pathname.getName())) {
                    return true;
                }
                return false;
            }      
        }

        try {
            //Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            //Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            //Return the number of cores (virtual CPU devices)
            return files.length;
        } catch(Exception e) {
            //Default to return 1 core
            return 1;
        }
    }

    //手机内存的总空间大小
    public static long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return (totalBlocks * blockSize)>>20;
    }
    //手机CPU主频大小
    public static String getMaxCpuFreq() {
        String result = "";
        ProcessBuilder cmd;
        try {
            String[] args = { "/system/bin/cat",
                    "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq" };
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            while (in.read(re) != -1) {
                result = result + new String(re);
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            result = "";
        }
        return result.trim();
    }
    
//    // 判断是否到时检查更新
//    public static boolean isTimeToCheckUpdate(){
//        String lastCheckTime = GlobalConfig.getLastCheckTime(GlobalConfig.KEY_CHECK_UPDATE);
//        if (null != lastCheckTime){
//            if (Util.isTimeToCheck(lastCheckTime)){
//                return true;
//            }
//        }
//        return false;
//    }
//    
//    // 判断是否到时提交上报数据
//    public static boolean isTimeToReportData(){
//        String lastCheckTime = GlobalConfig.getLastCheckTime(GlobalConfig.KEY_CHECK_DATA_REPORT);
//        if (null != lastCheckTime){
//            if (Util.isTimeToCheck(lastCheckTime)){
//                return true;
//            }
//        }    
//        return false;
//    }
    
	public static Bitmap getBitmap(Context context, Uri url, Bundle extras) {
		Size size_src = getBmpSizeFromUri(context,url);
        if (size_src == null || size_src.width <= 0 || size_src.height <= 0) {
            return null;
        }
        if (extras != null) {
            String orgSize = size_src.width + "*" + size_src.height;
            ZebraLog.d("PixDpUtil", "ord_size=" + orgSize);
            extras.putString("org_size", orgSize);
        }

		int memSize = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
		int inSampleSize = memSize >= 36 ? 1 : 2;
		
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        
        if(inSampleSize == 1){
	        if((size_src.width*size_src.height)>(1800*2400)){
	        	options.inSampleSize = 2;
	        }
	        else{
	        	options.inSampleSize = 1;
	        }
        }
        else if(inSampleSize == 2){
        	if((size_src.width*size_src.height)>(1500*2000)){
	        	options.inSampleSize = 2;
	        }
	        else{
	        	options.inSampleSize = 1;
	        }
        }
        
		InputStream is = null;
		
		try {
			is = context.getContentResolver().openInputStream(url);

			return BitmapFactory.decodeStream(is,null,options);
			
		} catch (Exception ex) {

			ex.printStackTrace();
			return null;
			
		} finally {
			if(is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
    static Size getBmpSizeFromUri(Context context, Uri url) {
        if (url == null) {
            return null;
        }
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            InputStream is = context.getContentResolver().openInputStream(url);

            BitmapFactory.decodeStream(is, null, options);
            return new Size(options.outWidth, options.outHeight);
        } catch (Exception e) {
            e.printStackTrace();
            return new Size(0, 0);
        }
    }
}
