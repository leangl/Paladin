package mobi.tattu.utils.log;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import mobi.tattu.utils.Utils;

public class Logger {

    private Logger() {
    }

    public static void init(Application app) {
        Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            Logger.e("ERROR", ex);
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, ex);
            }
        });
    }



    public static void e(Object tag, String message) {
        e(tag, message, null);
    }

    public static void e(Object tag, Throwable tr) {
        e(tag, null, tr);
    }

    public static void e(Object tag, String msg, Throwable tr) {
        String data = getMessage(msg, tr);
        String key = "[E]" + System.currentTimeMillis();
        String tagString = convertTag(tag);
//        ACRA.log.e(tagString, data);
        Log.e(tagString, data, tr);
//        putCustomData(key, data);
//        flush(tr);
    }

    public static void i(Object tag, String message) {
        i(tag, message, null);
    }

    public static void i(Object tag, Throwable tr) {
        i(tag, null, tr);
    }

    public static void i(Object tag, String msg, Throwable tr) {
        String data = getMessage(msg, tr);
        String key = "[I]" + System.currentTimeMillis();
        String tagString = convertTag(tag);
//        ACRA.log.i(tagString, data);
        Log.i(tagString, data, tr);
//        putCustomData(key, data);
    }

    public static void v(Object tag, String message) {
        v(tag, message, null);
    }

    public static void v(Object tag, Throwable tr) {
        v(tag, null, tr);
    }

    public static void v(Object tag, String msg, Throwable tr) {
        String data = getMessage(msg, tr);
        String key = "[V]" + System.currentTimeMillis();
        String tagString = convertTag(tag);
//        ACRA.log.v(tagString, data);
        Log.v(tagString, data, tr);
//        putCustomData(key, data);
    }

    public static void w(Object tag, String message) {
        w(tag, message, null);
    }

    public static void w(Object tag, Throwable tr) {
        w(tag, null, tr);
    }

    public static void w(Object tag, String msg, Throwable tr) {
        String data = getMessage(msg, tr);
        String key = "[W]" + System.currentTimeMillis();
        String tagString = convertTag(tag);
//        ACRA.log.w(tagString, data);
        Log.w(tagString, data, tr);
//        putCustomData(key, data);
    }

    public static void d(Object tag, String message) {
        d(tag, message, null);
    }

    public static void d(Object tag, Throwable tr) {
        d(tag, null, tr);
    }

    public static void d(Object tag, String msg, Throwable tr) {
        if (Utils.isDebug()) {
            String data = getMessage(msg, tr);
            String key = "[D]" + System.currentTimeMillis();
            String tagString = convertTag(tag);
//            ACRA.log.d(tagString, data);
            Log.d(tagString, data, tr);
//            putCustomData(key, data);
        }
    }

    public static void flush() {
        flush(new Exception("Log flushed without exception!"));
    }

    public static synchronized void flush(Throwable e) {
//        ACRA.getErrorReporter().handleSilentException(e);
    }

    private static void putCustomData(String key, String data) {
//        ACRA.getErrorReporter().putCustomData(key, data);
    }

    private static String getMessage(String msg, Throwable tr) {
        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(msg)) {
            sb.append(msg);
        }
        if (tr != null) {
            sb.append(Log.getStackTraceString(tr));
        }
        return sb.toString();
    }

    @SuppressWarnings("rawtypes")
    private static String convertTag(Object tag) {
        if (tag instanceof String) {
            return (String) tag;
        }
        if (tag instanceof Class) {
            return ((Class) tag).getSimpleName();
        }
        return tag.getClass().getSimpleName();
    }


























}