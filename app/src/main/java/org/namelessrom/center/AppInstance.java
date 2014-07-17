package org.namelessrom.center;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;

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

    public static PackageManager getPm() {
        return AppInstance.applicationContext.getPackageManager();
    }

    public static String getVersionName() {
        String version;
        try {
            version = getPm().getPackageInfo(
                    AppInstance.applicationContext.getPackageName(), 0).versionName;
        } catch (Exception exception) {
            version = "---";
        }
        return version;
    }
}
