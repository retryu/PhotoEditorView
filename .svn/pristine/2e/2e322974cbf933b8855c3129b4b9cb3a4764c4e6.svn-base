
package com.tencent.zebra.util.log;

import com.tencent.qphone.base.util.QLog;

//import com.qzone.global.util.log.QZLog;

/**
 * @author zatezhang
 */
public class ZebraLog {
    public static void d(String tag, String msg) {
        // QZLog.d(tag, msg);
        if (QLog.isDevelopLevel()) {
            QLog.d(tag, QLog.DEV, msg);
        }
        // Log.d(tag, msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        // QZLog.e(tag, msg, tr);
        if (QLog.isColorLevel()) {
            QLog.e(tag, QLog.CLR, msg, tr);
        }
        // Log.e(tag, msg, tr);
    }

    public static void e(String tag, String msg) {
        // QZLog.e(tag, msg);
        if (QLog.isColorLevel()) {
            QLog.e(tag, QLog.CLR, msg);
        }
        // Log.e(tag, msg);
    }

    public static void w(String tag, String msg) {
        // QZLog.w(tag, msg);
        if (QLog.isDevelopLevel()) {
            QLog.w(tag, QLog.DEV, msg);
        }
        // Log.w(tag, msg);
    }

    public static void i(String tag, String msg) {
        // QZLog.i(tag, msg);
        if (QLog.isDevelopLevel()) {
            QLog.i(tag, QLog.DEV, msg);
        }
        // Log.i(tag, msg);
    }

    public static void v(String tag, String msg) {
        // QZLog.v(tag, msg);
        // QLog.v(tag, msg);
        // Log.v(tag, msg);
    }
}
