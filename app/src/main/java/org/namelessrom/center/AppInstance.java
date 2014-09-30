/*
 * <!--
 *    Copyright (C) 2014 Alexander "Evisceration" Martinz
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * -->
 */

package org.namelessrom.center;

import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;

import com.koushikdutta.ion.Ion;

import org.namelessrom.center.utils.DebugHelper;
import org.namelessrom.center.utils.Helper;
import org.namelessrom.center.utils.PreferenceHelper;

import java.io.File;

/**
 * AppInstance aka Application
 */
public class AppInstance extends Application {

    public static AppInstance sInstance;

    private static final Handler handler = new Handler(Looper.getMainLooper());

    public static AppInstance get() { return sInstance; }

    @Override public void onCreate() {
        super.onCreate();

        sInstance = this;
        Ion.getDefault(AppInstance.sInstance).configure().getResponseCache().setCaching(false);

        DebugHelper.setEnabled(Helper.isNamelessDebug());
        Logger.setEnabled(PreferenceHelper.getBoolean(PreferenceHelper.DEBUG, false));

        Helper.createDirectories();
    }

    public static PackageManager getPm() {
        return sInstance.getPackageManager();
    }

    public static String getVersionName() {
        String version;
        try {
            version = getPm().getPackageInfo(sInstance.getPackageName(), 0).versionName;
        } catch (Exception exception) {
            version = "---";
        }
        return version;
    }

    public static File getFiles() { return sInstance.getFilesDir(); }

    public static String getFilesDirectory() {
        final File tmp = getFiles();
        if (tmp != null && tmp.exists()) {
            return tmp.getPath();
        } else {
            return "/data/data/" + sInstance.getPackageName();
        }
    }

    public static String getStr(final int resId) {
        return sInstance.getString(resId);
    }

    public static String getStr(final int resId, final Object... objects) {
        return sInstance.getString(resId, objects);
    }

    public static Handler getHandler() { return handler; }
}
