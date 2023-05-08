package com.autolink.app.downloadmanager;

import android.util.Log;

public class Logger {

    private static final String TAG = "DownloadManager";

    public static boolean loggerEnabled = true;

    public static void debug(String msg, Throwable tr) {
        if (loggerEnabled) {
            Log.d(TAG, msg, tr);
        }
    }

    public static void debug(String msg) {
        if (loggerEnabled) {
            Log.d(TAG, msg);
        }
    }

    public static void error(String msg, Throwable tr) {
        if (loggerEnabled) {
            Log.e(TAG, msg, tr);
        }
    }

    public static void error(String msg) {
        if (loggerEnabled) {
            Log.e(TAG, msg);
        }
    }

}
