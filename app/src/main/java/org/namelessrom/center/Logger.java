package org.namelessrom.center;

import android.util.Log;

/**
 * A Logging utility
 */
public class Logger {

    private static final boolean DEBUG = false;

    public static void d(final Object object, final String msg) {
        if (!DEBUG) return;

        Log.d(object.getClass().getSimpleName(), "--> " + msg);
    }

}
