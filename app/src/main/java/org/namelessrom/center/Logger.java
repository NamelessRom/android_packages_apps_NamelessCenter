package org.namelessrom.center;

import android.util.Log;

/**
 * Created by alex on 7/17/14.
 */
public class Logger {

    private static final boolean DEBUG = true;

    public static void d(final Object object, final String msg) {
        if (!DEBUG) return;

        Log.d(object.getClass().getSimpleName(), "--> " + msg);
    }

}
