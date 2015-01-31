package com.retryu.zebra.effect.utils;

/*
 * Copyright (C) 2009 The Android Open Source Project
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

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.text.TextUtils;
import android.util.FloatMath;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.Inflater;


/**
 * Collection of utility functions used in this package.
 */
public class Utils {
    private static final String TAG = "Util";

    public static final String RES_PREFIX_ASSETS = "assets://";
    public static final String RES_PREFIX_STORAGE = "/";
    public static final String RES_PREFIX_HTTP = "http://";
    public static final String RES_PREFIX_HTTPS = "https://";

    //@Kesen:因为在BrowserActivity会出现load同一张图的情况(key相同)，根据水印相机之前遇到的情况，有可能出现问题，故加前缀进行区分
    public static final String PREFIX_FOR_LOADER = "SELECT_KEY";

    // 拼图模板解析线程名
    public static final String THREAD_NAME_COLLAGE_PROCESS = "cpT";
    // 地图更新线程名
    public static final String THREAD_NAME_MAP = "mapT";
    // 闪屏线程
    public static final String THREAD_NAME_SPLASH = "splashT";
    // 内存管理的后台线程
    public static final String THREAD_NAME_MEM = "mT";

    public static final int JPEG_QUALITY = 95;

    public static final int PAUSED_RESOURCE_DELAY = 300;

    // 模板最大长边
    public static final int COLLAGE_BMP_MAX_LONG_SIDE = 1800;
    // 模板最大短边
    public static final int COLLAGE_BMP_MAX_SHORT_SIDE = 720;

    public static final int COLLAGE_READ_MAX_SIDE = 720;

    public static final int COLLAGE_READ_LOW_MAX_SIDE = 640;

    public static final int COLLAGE_MATERIAL_READ_MAX_SIDE = 720;

    // collage save level, base width
    public static final int[] LONG_COLLAGE_SAVE_MAX_SIDE = {640, 560, 480, 400, 320};
    // add 960 for landscape collage
    public static final int[] STORY_COLLAGE_SAVE_MAX_SIDE = {960, 720, 640, 560, 480, 400, 320};

    // The brightness setting used when it is set to automatic in the system.
    // The reason why it is set to 0.7 is just because 1.0 is too bright.
    // Use the same setting among the Camera, VideoCamera and Panorama modes.
    private static final float DEFAULT_CAMERA_BRIGHTNESS = 0.7f;

//    public static final String EXTERNAL_STORAGE_PUBLIC_DIRECTORY = parseExternalStorageDirectory();

    // 大图最大长边
    public static final int BIG_BMP_MAX_LONG_SIDE = 1800;
    // 大图最大短边
    public static final int BIG_BMP_MAX_SHORT_SIDE = 1800;
    // 小图最大长边
    public static final int SMALL_BMP_MAX_LONG_SIDE = 960;
    // 小图最大短边
    public static final int SMALL_BMP_MAX_SHORT_SIDE = 960;

    // 正式发布
    public static final int APP_TYPE_RELEASE = 0;
    // 内部测试
    public static final int APP_TYPE_RDM = 1;
    // 外团
    public static final int APP_TYPE_ALPHA = 2;
    // 码包灰度
    public static final int APP_TYPE_HDBM = 3;

    public static final String QQCAMERA_PACKAGE_NAME = "com.tencent.qqcamera"; // 魅拍的包名
    public static final String QQCAMERA_KEY_ENABEL_FACE_DETECT = "enable_face_detect"; // 是否打开人脸识别框
    public static final String QQCAMERA_KEY_FACE_RECTF = "face_rectf"; // 人脸识别结果的区域
    public static final String QQCAMERA_KEY_LAUNCH_REFER = "launch_refer";
    public static final String QQCAMERA_FLAG_REFER_TTPU_KATONG = "ttpu_katong"; // 卡通人像
    public static final String QQCAMERA_FLAG_REFER_TTPU_QQCAMERA = "ttpu_qqcamera"; // 特效相机
    public static final String QQCAMERA_KEY_FACE_DETECT_HINT = "face_detect_hint"; // 人脸检测提示文字
    public static final String QQCAMERA_KEY_FACE_DETECT_FAIL = "face_detect_fail"; // 人脸检测失败时的提示文字
    public static final String QQCAMERA_KEY_PICK_PICTURE_PATH = "pick_picture_path"; // 回传给天天P图的选图路径
    public static final int FACE_DETECT_MIN_SIZE = 50;

    public static final float MAX_SCALE = 6.0f; // 放大最大允许系数
    public static final float MIN_SCALE = 1.0f; // 缩小最小允许系数
    public static final float INIT_SCALE = 1.0f; // 缩小最小允许系数

    private static final String[] IMAGE_PROJECTION = new String[]{
            MediaStore.Images.ImageColumns.DATE_TAKEN, MediaStore.Images.ImageColumns.LATITUDE,
            MediaStore.Images.ImageColumns.LONGITUDE,};

    public static boolean hasEclairMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR_MR1;
    }

    public static boolean hasFroyo() {
        // Can use static final constants like FROYO, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasIcs() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean hasJellyBeanMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    public static boolean hasJellyBeanMR2() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static final boolean HAS_MEDIA_COLUMNS_WIDTH_AND_HEIGHT =
            hasField(MediaColumns.class, "WIDTH");

    private static boolean hasField(Class<?> klass, String fieldName) {
        try {
            klass.getDeclaredField(fieldName);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }



//    private static String parseExternalStorageDirectory(){
//        String mEnd = "ROOT";
//        String mBasic =
//                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
//        if(mBasic!=null){
//            String[] result =  mBasic.split("/");
//            if(result.length>0){
//                mEnd = result[result.length - 1];
//            }
//        }
//        return mEnd;
//    }

    public static String getImagePathByUri(Context context, Uri uri) {
        String path = uri.getPath();

        String finalPath = null;
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            if (file.exists()) {
                finalPath = path;
            }
        }

        //Read from
        if (TextUtils.isEmpty(finalPath)) {
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
                if (cursor.getCount() == 0) {
                    finalPath = null;
                } else {
                    cursor.moveToFirst();
                    finalPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

        }
        return finalPath;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px 的单位 转成为 dp(像素)
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * Convert px to sp
     *
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * Convert sp to px
     *
     * @param context
     * @param spValue
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    private static final long ONE_DAY_THRESHOLD = 24 * 60 * 60l;

    //    private static final long ONE_DAY_THRESHOLD = 30l; // 测试用30秒
    public static boolean isTimeToCheck(Date lastDate) {
        Date nowDate = Calendar.getInstance().getTime();
        long diffSeconds = Math.abs(nowDate.getTime() - lastDate.getTime()) / 1000L;
        if (diffSeconds >= ONE_DAY_THRESHOLD) {
            return true;
        }
        return false;
    }



    // 取出路径中的文件名
    public static String getFileName(String imagePath) {
        if (TextUtils.isEmpty(imagePath)) {
            return null;
        }
        int index = imagePath.lastIndexOf(File.separator);
        String fileName = imagePath.substring(index);
        return fileName;
    }

    public static String path2Name(String path) {
        if (path == null) {
            return null;
        }
        int lastIndex = path.lastIndexOf("/");
        if (lastIndex == -1) {
            return "/";
        }
        return path.substring(lastIndex + 1);
    }

    public static String path2Dir(String path) {
        if (path == null) {
            return null;
        }
        int lastIndex = path.lastIndexOf("/");
        if (lastIndex == -1) {
            return "/";
        }
        return path.substring(0, lastIndex);
    }

    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }

    public static Uri getUriFromPath(String path) {
        return Uri.fromFile(new File(path));
    }

//    /**
//     * InputStream转Bytes
//     *
//     * @param is
//     * @return
//     * @throws IOException
//     */
//    public static byte[] InputStreamToBytes(InputStream is) throws IOException {
//        byte[] result = null;
//        ByteArrayOutputStream byteStream = null;
//        try {
//            byteStream = new ByteArrayOutputStream();
//            int ch;
//            while ((ch = is.read()) != -1) {
//                byteStream.write(ch);
//            }
//            result = byteStream.toByteArray();
//            byteStream.close();
//        } catch (IOException e) {
//            throw e;
//        } finally {
//            if (is != null) {
//                is.close();
//            }
//            if (byteStream != null) {
//                byteStream.close();
//            }
//        }
//        return result;
//    }

//    /**
//     * Bytes转InputStream
//     *
//     * @param data
//     * @return
//     */
//    public static InputStream BytesToInputStream(byte[] data) {
//        return data == null ? null : new ByteArrayInputStream(data);
//    }

    /*public static void initializeScreenBrightness(Window win,
                                                  ContentResolver resolver) {
        // Overright the brightness settings if it is automatic
        int mode = Settings.System.getInt(resolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            WindowManager.LayoutParams winParams = win.getAttributes();
            winParams.screenBrightness = DEFAULT_CAMERA_BRIGHTNESS;
            win.setAttributes(winParams);
        }

        //TODO: temporary comment
        Boolean mScreenBrightness = false; *//*(Boolean)(Setting.instance().getSettings(Setting.SETTING_SCREEN_BRIGHTNESS))*//*;
        WindowManager.LayoutParams winParams = win.getAttributes();
        if(mScreenBrightness){
            winParams.screenBrightness = DEFAULT_CAMERA_BRIGHTNESS;
        }else{
            try {
                winParams.screenBrightness = Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS) / 255f;
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
        }
        win.setAttributes(winParams);
    }*/

    public static void saveURItoClipText(Context context, Uri uri) {
        String path = getImagePathByUri(context, uri);
        @SuppressWarnings("deprecation")
        android.text.ClipboardManager clipboard = (android.text.ClipboardManager)
                context.getSystemService(Context.CLIPBOARD_SERVICE);
        try {
            if (path != null && clipboard != null)
                clipboard.setText(path);
        } catch (Exception e) {
            // nothing to be done if some devices failed
        }
    }

//    public static String getDICMCamera() {
//        String directory = /*Tool.file.getCurrentStorageDir()*/ Environment.getExternalStorageDirectory()  + "/" + EXTERNAL_STORAGE_PUBLIC_DIRECTORY+"/Camera";
//        return directory;
//    }








    /**
     * 根据QUA获取当前发布的版本类型
     *
     * @param qua
     * @return
     */
    public static int getAppType(String qua) {
        if (!TextUtils.isEmpty(qua)) {
            qua = qua.toLowerCase();
            if (qua.contains("_rdm")) {
                return APP_TYPE_RDM;
            } else if (qua.contains("_alpha")) {
                return APP_TYPE_ALPHA;
            } else if (qua.contains("_hdbm")) {
                return APP_TYPE_HDBM;
            }
        }
        return APP_TYPE_RELEASE;
    }

    /**
     * 是否测试版本
     *
     * @param qua
     * @return
     */
    public static boolean isTestVersion(String qua) {
        int appType = getAppType(qua);
        switch (appType) {
            case Utils.APP_TYPE_HDBM:
            case Utils.APP_TYPE_ALPHA:
            case Utils.APP_TYPE_RDM:
                return true;
        }
        return false;
    }

    /**
     * 获得去除 {@link #RES_PREFIX_ASSETS} 前缀的资源路径
     *
     * @param path
     * @return
     */
    public static String getRealPath(String path) {
        return TextUtils.isEmpty(path) ? path : path.startsWith(RES_PREFIX_ASSETS) ? path.substring(RES_PREFIX_ASSETS.length()) : path;
    }



    /**
     * zip解包工具
     *
     * @param data zip字节数组
     * @return 返回解压后的字节数组
     */
    public static byte[] unZip(byte[] data) throws IOException {
        if (null == data) return null;

        Inflater decompresser = new Inflater();
        decompresser.reset();
        decompresser.setInput(data);
        byte[] output = new byte[0];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[1024];
            while (!decompresser.finished()) {
                int i = decompresser.inflate(buf);
                byteArrayOutputStream.write(buf, 0, i);
            }
            output = byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            output = data;
            e.printStackTrace();
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        decompresser.end();
        return output;
    }

    /**
     * 获取app是否已安装
     *
     * @param context
     * @param pakName 包名
     * @return
     */
    public static boolean isAppInstalled(Context context, String pakName) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(pakName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
            e.printStackTrace();
        }
        if (packageInfo == null) {
            return false;
        }
        return true;
    }



    /**
     * 调用已安装的拍照应用
     */
    /*public static void captureImage(final Activity activity, final Bundle extras, final CaptureDialogListener listener, final int captureCode, final int pickPhotoCode, boolean markMeipai, boolean isFaceDetect, final int cartoonType) {
        final Dialog dialog = createCaptureDialog(activity, markMeipai, isFaceDetect);
        final Intent[] intent = {null};
        try {
            dialog.show();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (listener != null) {
                        listener.onDialogDismissed(intent[0]);
                    }
                }
            });
            ListView lvApps = (ListView) dialog.getWindow().getDecorView().findViewById(R.id.lv_apps);
            lvApps.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        Adapter adapter = parent.getAdapter();
                        if (adapter != null && position > -1 && position < adapter.getCount()) {
                            Object item = adapter.getItem(position);
                            if (!(item instanceof ResolveInfo)) {
                                return;
                            }
                            ResolveInfo info = (ResolveInfo) item;
                            if (info != null) {
                                if (info.activityInfo == null) {
                                    Intent intent = new Intent(activity, BrowserActivity.class);
                                    intent.putExtra(IntentUtils.KEY_BROWSER_SINGLE, true);
                                    intent.putExtra(IntentUtils.KEY_NEED_FACE_DETECT, true);
                                    intent.putExtra(IntentUtils.KEY_NEED_PIC_CONFIRM, true);
                                    intent.putExtra(IntentUtils.KEY_TO_MODULE, IntentUtils.MODULE_CARTOON);
//                                    intent.putExtra(CartoonUtils.KEY_TYPE, cartoonType);
                                    activity.startActivityForResult(intent, pickPhotoCode);
                                    DataReport.getInstance().report(ReportInfo.create(ReportConfig.OPL1_CARTOON_CLICK, ReportConfig.CARTOON_CLICK.OPL2_LOCAL_PIC));
                                } else {
                                    Intent loadIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    ComponentName comp = new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
                                    loadIntent.setComponent(comp);
                                    if (extras != null) {
                                        loadIntent.putExtras(extras);
                                    }
                                    loadIntent.putExtra("android.intent.extras.CAMERA_FACING", Camera.CameraInfo.CAMERA_FACING_FRONT);
                                    intent[0] = loadIntent;
                                    activity.startActivityForResult(loadIntent, captureCode);
                                    activity.overridePendingTransition(0, 0);
                                    DataReport.getInstance().report(ReportInfo.create(ReportConfig.OPL1_CARTOON_CLICK, ReportConfig.CARTOON_CLICK.OPL2_START_OTHER_CAMERA));
                                }
                            }
                        }
                    } catch (Exception e) {
                        ExToast.makeText(GlobalContext.getContext(), R.string.capture_fail_tips, Toast.LENGTH_SHORT);
                        e.printStackTrace();
                    }
                    try {
                        dialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            ExToast.makeText(GlobalContext.getContext(), R.string.capture_fail_tips, Toast.LENGTH_SHORT);
            e.printStackTrace();
        }
    }*/

    public interface CaptureDialogListener {
        public void onDialogDismissed(Intent loadIntent);
    }


    private static class ViewHolder {
        public TextView name;
    }

    /*private static class IconTextAdapter extends ArrayAdapter<ResolveInfo> {

        private boolean mMarkMeipai;

        public IconTextAdapter(Context context, List<ResolveInfo> objects, boolean markMeipai) {
            super(context, -1, objects);
            mMarkMeipai = markMeipai;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = new TextView(getContext());
                holder.name = (TextView) convertView;
                int padding = Utils.dip2px(getContext(), 7.5f);
                holder.name.setPadding(0, padding, padding, padding);
                holder.name.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                holder.name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                holder.name.setTextColor(getContext().getResources().getColor(R.color.capture_app_name_color));
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ResolveInfo info = getItem(position);
            if (info != null) {
                if (info.activityInfo == null) {
                    holder.name.setText(R.string.capture_pick_photo);
                    Drawable icon = getContext().getResources().getDrawable(R.drawable.ic_album);
                    int size = Utils.dip2px(getContext(), 30);
                    icon.setBounds(0, 0, size, size);
                    holder.name.setCompoundDrawablePadding(Utils.dip2px(getContext(), 15));
                    holder.name.setCompoundDrawables(icon, null, null, null);
                } else {
                    PackageManager pManager = getContext().getPackageManager();
                    CharSequence name = info.activityInfo.loadLabel(pManager);
                    if (QQCAMERA_PACKAGE_NAME.equals(info.activityInfo.packageName) && mMarkMeipai) {
                        holder.name.setText(name + "(旧版)");
                    } else {
                        holder.name.setText(name);
                    }
                    Drawable icon = info.activityInfo.loadIcon(pManager);
                    int size = Utils.dip2px(getContext(), 30);
                    icon.setBounds(0, 0, size, size);
                    holder.name.setCompoundDrawablePadding(Utils.dip2px(getContext(), 15));
                    holder.name.setCompoundDrawables(icon, null, null, null);
                }
            }

            return convertView;
        }
    }*/




    /**
     * 获得调用者的软件包名
     *
     * @param activity
     * @return
     */
    public static String getCallerPackage(Activity activity) {
        ComponentName cm = activity.getCallingActivity();
        return cm != null ? cm.getPackageName() : null;
    }

    public static Point toPoint(PointF pointF) {
        return new Point((int) pointF.x, (int) pointF.y);
    }

    public static float calDistance(PointF a, PointF b) {
        float dx = b.x - a.x;
        float dy = b.y - a.y;
        return FloatMath.sqrt(dx * dx + dy * dy);
    }
}
