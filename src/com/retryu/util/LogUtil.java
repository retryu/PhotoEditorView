package com.retryu.util;

import android.os.SystemClock;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by kesenhu on 5/28/2014.
 */
public class LogUtil {
    public final static boolean ENABLE_LOG = true;
    public final static boolean ENABLE_LOG_PROFILE = true;
    public final static boolean ENABLE_LOG_FILE = false;
    public final static String TAG = "CameraSDK";
    public final static String PROFILE_TAG = ":Profile";

    private static long mLastTime;
    private static long mInitTime;

    public static int i(Object o, String msg) {
        if (ENABLE_LOG) {
            String tag = getTag(o);
            if (msg.contains("BEGIN")) {
                LogUtil.initTime();
            } else if (msg.contains("END")) {
                msg = genenateLogPrefix(tag) + msg;
                long currentTime = SystemClock.currentThreadTimeMillis();
                return Log.i(tag, msg + " -> cost time = " + (currentTime - mLastTime) + ", total time = " + (currentTime - mInitTime));
            }
            msg = genenateLogPrefix(tag) + msg;
            return Log.i(tag, msg);
        }
        return 0;
    }

    public static int v(Object o, String msg) {
        if (ENABLE_LOG) {
            String tag = getTag(o);
            msg = genenateLogPrefix(tag) + msg;
            return Log.v(tag, msg);
        }
        return 0;
    }

    public static int d(Object o, String msg) {
        if (ENABLE_LOG) {
            String tag = getTag(o);
            msg = genenateLogPrefix(tag) + msg;
            return Log.d(tag, msg);
        }
        return 0;
    }

    public static int w(Object o, String msg) {
        if (ENABLE_LOG) {
            String tag = getTag(o);
            msg = genenateLogPrefix(tag) + msg;
            return Log.w(tag, msg);
        }
        return 0;
    }

    public static int e(Object o, String msg) {
        if (ENABLE_LOG) {
            String tag = getTag(o);
            msg = genenateLogPrefix(tag) + msg;
            return Log.e(tag, msg);
        }
        return 0;
    }

    public static int e(Object o, String msg, Throwable tr) {
        if (ENABLE_LOG) {
            String tag = getTag(o);
            msg = genenateLogPrefix(tag) + msg;
            return Log.e(tag, msg, tr);
        }
        return 0;
    }

    public static String getTag(Object o) {
        if (o == null) {
            return TAG;
        }
        if (o instanceof String) {
            return (String) o;
        }
        return o.getClass().getSimpleName();
    }

    // 初始化记录消耗的系统初始时间
    public static long initTime() {
        if (ENABLE_LOG && ENABLE_LOG_PROFILE) {
            mLastTime = SystemClock.currentThreadTimeMillis();
            mInitTime = mLastTime;
            return mLastTime;
        }
        return 0;
    }

//    // 更新记录的系统时间，用于后续统计消耗时间
//    public static long updateTime() {
//        if (ENABLE_LOG && ENABLE_LOG_PROFILE) {
//            mLastTime = SystemClock.currentThreadTimeMillis();
//            return mLastTime;
//        }
//        return 0;
//    }

    // 打印距离上次记录系统时间至今的消耗时间
    public static void printTime(Object o, String msg) {
        if (ENABLE_LOG && ENABLE_LOG_PROFILE) {
            long currentTime = SystemClock.currentThreadTimeMillis();
            String tag = getTag(o);
            i(tag, msg + " -> cost time = " + (currentTime - mLastTime) + ", total time = " + (currentTime - mInitTime));
            mLastTime = SystemClock.currentThreadTimeMillis();
        }
    }

    /**
     * 生成Log日志的前缀信息。如下格式：
     * 当前线程名+文件名+行号+方法名
     *
     * @param simpleClassName
     * @return
     */
    private static String genenateLogPrefix(String simpleClassName) {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts == null) {
            return "";
        }
        for (StackTraceElement st : sts) {
            if (st.isNativeMethod()) {
                continue;
            }
            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }
            if (st.getClassName().endsWith(simpleClassName)) {
                //return "[" + Thread.currentThread().getName() + "][" + st.getFileName() + ":" + st.getLineNumber() + "] ";
                return "[" + Thread.currentThread().getName() + "][" + st.getFileName() + ":" + st.getLineNumber() + "][" + st.getMethodName() + "] ";
            }
        }
        return "";
    }

    public static int writeLog(Object o, String msg) {
        if (ENABLE_LOG && ENABLE_LOG_PROFILE && ENABLE_LOG_FILE) {
            String tag = getTag(o);
            try {
                msg += "\n";
                long currentTime = System.currentTimeMillis();
                FileOutputStream file = new FileOutputStream("/mnt/sdcard/log.txt", true);
                String time = String.valueOf(currentTime) + "--\t";
                file.write(time.getBytes());
                file.write(tag.getBytes());
                file.write(new String("\t").getBytes());
                file.write(msg.getBytes());

                file.flush();
                file.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }
}
