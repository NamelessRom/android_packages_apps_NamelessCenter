package org.namelessrom.center;

import android.app.Application;
import android.content.Context;

/**
 * AppInstance aka Application
 */
public class AppInstance extends Application {

    public static Context applicationContext;

    @Override
    public void onCreate() {
        super.onCreate();

        applicationContext = getApplicationContext();
    }
}
