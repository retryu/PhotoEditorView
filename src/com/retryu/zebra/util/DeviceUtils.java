package com.retryu.zebra.util;
/**
 * Version 1.0
 *
 * Date: 2013-10-29 15:45
 * Author: yonnielu
 *
 * Copyright © 1998-2013 Tencent Technology (Shenzhen) Company Ltd.
 *
 */

import java.io.File;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;


/**
 * 与设备参数读取相关的工具类
 */
public class DeviceUtils {
    private static final String TAG = DeviceUtils.class.getSimpleName();

    public static final int MOBILE_NETWORK_2G = 1;
    public static final int MOBILE_NETWORK_3G = 2;
    public static final int MOBILE_NETWORK_4G = 3;
    public static final int MOBILE_NETWORK_UNKNOWN = 4;
    public static final int MOBILE_NETWORK_DISCONNECT = 5;

    /** 网络环境 */
    public static final int NET_NONE = 0; // 无网
    public static final int NET_WIFI = 1; // WIFI
    public static final int NET_2G = 2; // 2G
    public static final int NET_3G = 3; // 3G
    public static final int NET_4G = 4; // 3G
    public static final int NET_OTHER = 5; // 其他

    private static int sTotalMemory = 0;
    private static long sTotalInternalMemory = 0;
    private static long sMaxCpuFreq = 0;
    private static int sCpuCount = 0;

    // 适配低端手机的相关参数
    private static final long SMALL_DISPLAYPIXELS = 480 * 320;
    private static final long NORMAL_MIN_CPU = 800000;
    private static final long NORMAL_MIN_MEMORY = 512;
    private static final long NORMAL_MIN_INTERNAL_MEMORY = 512;

    public static final int MIN_STORAGE_SIZE = 50 * 1024 * 1024; // 50MB

//    public static String ROOT = "";
    public static String ROOT;
    public static String DIRECTORY;
    static {
//        generateDirectory();
        ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
        DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Pitu";
    	/*try {
			ROOT = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
			DIRECTORY = ROOT + "/Camera";
    	} catch (Exception e) {
			ROOT = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ROOT";
			DIRECTORY = ROOT + "/Camera";
		} catch (java.lang.NoSuchFieldError error) {
			ROOT = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ROOT";
			DIRECTORY = ROOT + "/Camera";
		}*/
    }
    public static String generateAppDataDir(String root) {
        return root + File.separator + "Pitu";
    }

    public static boolean checkJniLibsFolder(Context context) {
        File file = context.getFilesDir();
        if (file != null) {
            String path = file.getAbsolutePath().replace("files", "lib");
            file = new File(path);
            if (file != null && file.exists() && file.isDirectory()) {
                String[] libs = file.list();
                if (libs != null && libs.length > 0) {
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * 获取IMEI号 add by zhenhaiwu
     * @param context
     * @return
     */
    public static String getImei(Context context) {
        try {
            TelephonyManager telephonyManager=(TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String imei = telephonyManager.getDeviceId();
            if(null != imei)
                return imei;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * 获取app vesionName add by zhenhaiwu
     * @param context
     * @return
     */
    public static String getVersionName(Context context){
        try{
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        }catch(Exception e){

        }
        return null;
    }
    
    public static int getVersionInt(Context context){
        try{
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String versionName = packageInfo.versionName;
            int index = versionName.lastIndexOf(".");
            versionName = versionName.substring(0, index);
            versionName = versionName.replace(".", "");
            return Integer.valueOf(versionName);
        }catch(Exception e){

        }
        return -1;
    }

    /**
     * 获取系统号 add by zhenhaiwu
     * @return
     */
    public static String getOSVersion(){
    	return  android.os.Build.VERSION.RELEASE;
    }
    
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {

        } else {
            final NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
                return true;
            }
        //获取所有网络连接信息
//            NetworkInfo[] info = connectivity.getAllNetworkInfo();
//            if (info != null) {//逐一查找状态为已连接的网络
//                for (int i = 0; i < info.length; i++) {
//                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
//                        return true;
//                    }
//                }
//            }
        }
        return false;
    }
    
    public static int getScreenWidth(Context context) {
    	DisplayMetrics display = context.getResources().getDisplayMetrics();
    	return display.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics display = context.getResources().getDisplayMetrics();
        return display.heightPixels;
    }

}
