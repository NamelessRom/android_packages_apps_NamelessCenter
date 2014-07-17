package org.namelessrom.center.utils;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import org.namelessrom.center.AppInstance;

import java.util.List;

/**
 * Class for helping interacting with other applications
 */
public class AppHelper {

    /**
     * Checks if a specific action exists.
     *
     * @param actionName The action as string
     * @return if the action exists.
     */
    public static boolean actionExists(final String actionName) {
        final Intent i = new Intent();
        i.setAction(actionName);
        return AppInstance.applicationContext.getPackageManager()
                .queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY).size() > 0;
    }

}
