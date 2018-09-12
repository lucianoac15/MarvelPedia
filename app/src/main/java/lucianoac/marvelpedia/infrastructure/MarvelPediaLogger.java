package lucianoac.marvelpedia.infrastructure;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import lucianoac.marvelpedia.BuildConfig;



public class MarvelPediaLogger {

    private static final String DEFAULT_TAG = MarvelPediaLogger.class.getSimpleName();


    private static final int LOG_LEVEL_DEBUG = 4;
    private static final int LOG_LEVEL_INFO = 3;
    private static final int LOG_LEVEL_WARN = 2;
    private static final int LOG_LEVEL_ERROR = 1;

    public static final String SEPARATOR = "===================================================";

    private MarvelPediaLogger() {

    }

    public static void debug(String tag, String msg) {

        boolean shouldLog = BuildConfig.DEBUG || (BuildConfig.LOG_LEVEL >= LOG_LEVEL_DEBUG && !TextUtils.isEmpty(msg));
        if (shouldLog) {
            Log.d(tag, msg);
        }
    }

    public static void debug(String msg) {

        debug(DEFAULT_TAG, msg);
    }

    public static void info(String tag, String msg) {

        boolean shouldLog = BuildConfig.DEBUG || (BuildConfig.LOG_LEVEL >= LOG_LEVEL_INFO && !TextUtils.isEmpty(msg));
        if (shouldLog) {
            Log.i(tag, msg);
        }
    }

    public static void info(String msg) {

        info(DEFAULT_TAG, msg);
    }

    public static void warn(String tag, String msg) {

        boolean shouldLog = BuildConfig.DEBUG || (BuildConfig.LOG_LEVEL >= LOG_LEVEL_WARN && !TextUtils.isEmpty(msg));
        if (shouldLog) {
            Log.w(tag, msg);
        }
    }

    public static void warn(String msg) {

        warn(DEFAULT_TAG, msg);
    }

    public static void error(String tag, String msg) {

        boolean shouldLog = BuildConfig.DEBUG || (BuildConfig.LOG_LEVEL >= LOG_LEVEL_ERROR && !TextUtils.isEmpty(msg));
        if (shouldLog) {
            Log.e(tag, msg);
        }
    }

    public static void error(String msg) {

        error(DEFAULT_TAG, msg);
    }

    public static void error(String tag, String msg, Throwable ex) {

        boolean shouldLog = BuildConfig.DEBUG || (BuildConfig.LOG_LEVEL >= LOG_LEVEL_ERROR && !TextUtils.isEmpty(msg));
        if (shouldLog) {
            Log.e(tag, msg, ex);
        }
    }

    public static void error(String tag, Throwable ex) {

        error(tag, ex.getMessage(), ex);
    }

    public static void error(Throwable ex) {

        error(ex.getMessage(), ex);
    }

    public static void logToFile(String message) {

        File log = new File(Environment.getExternalStorageDirectory(), "smartCanvasLogFile.txt");
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(log.getAbsolutePath(), log.exists()));
            out.write(message);
            out.close();
        } catch (IOException e) {
            // silent
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // silent
                }
            }
        }
    }
}
