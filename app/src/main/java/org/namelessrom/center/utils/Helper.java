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

package org.namelessrom.center.utils;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.StatusBarManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.namelessrom.center.AppInstance;
import org.namelessrom.center.Constants;
import org.namelessrom.center.Logger;
import org.namelessrom.center.services.UpdateCheckService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * A helper class which helps me helping you
 */
public class Helper {

    private static final int FILE_BUFFER = 512;

    public static boolean isNameless() {
        return !readBuildProp("ro.nameless.version").equals("NULL");
    }

    public static boolean isNamelessDebug() {
        return !readBuildProp("ro.nameless.debug").equals("NULL");
    }

    public static boolean isOnline() {
        final ConnectivityManager cm = (ConnectivityManager)
                AppInstance.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public static boolean isMetered() {
        final ConnectivityManager cm = (ConnectivityManager)
                AppInstance.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.isActiveNetworkMetered();
    }

    public static String readBuildProp(final String prop) {
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;

        try {
            fileReader = new FileReader("/system/build.prop");
            bufferedReader = new BufferedReader(fileReader, FILE_BUFFER);
            String tmp;
            while ((tmp = bufferedReader.readLine()) != null) {
                if (tmp.contains(prop)) return tmp.replace(prop + "=", "");
            }
        } catch (final Exception e) {
            Logger.e(Helper.class, "Error: " + e.getMessage());
        } finally {
            try {
                if (bufferedReader != null) bufferedReader.close();
                if (fileReader != null) fileReader.close();
            } catch (Exception ignored) { }
        }

        return "NULL";
    }

    public static int getBuildDate() { return parseDate(Helper.readBuildProp("ro.nameless.date")); }

    public static int parseDate(final String timeStampString) {
        try {
            return Integer.parseInt(timeStampString);
        } catch (Exception exc) {
            Logger.e(Helper.class, String.format("parseDate: %s", exc.getMessage()));
            return 20140101;
        }
    }

    public static void createDirectories() {
        File f = new File(Constants.UPDATE_FOLDER_FULL);
        if (!f.exists()) {
            Logger.v(Helper.class,
                    "Created: " + f.getAbsolutePath() + ": " + (f.mkdirs() ? "true" : "false"));
        }
        f = new File(Constants.UPDATE_FOLDER_ADDITIONAL);
        if (!f.exists()) {
            Logger.v(Helper.class,
                    "Created: " + f.getAbsolutePath() + ": " + (f.mkdirs() ? "true" : "false"));
        }
        f = new File(Constants.UPDATE_FOLDER_CHANGELOG);
        if (!f.exists()) {
            Logger.v(Helper.class,
                    "Created: " + f.getAbsolutePath() + ": " + (f.mkdirs() ? "true" : "false"));
        } else {
            Logger.v(Helper.class, "Cleaning changelogs.");
            //cleanChangelogs(f);
        }
    }

    public static void scheduleUpdateService(final int updateFreq) {
        // Load the required settings from preferences
        final long lastCheck = Long.parseLong(
                PreferenceHelper.getString(Constants.LAST_UPDATE_CHECK_PREF, "0"));

        // Get the intent ready
        final Intent i = new Intent(AppInstance.applicationContext, UpdateCheckService.class);
        i.setAction(UpdateCheckService.ACTION_CHECK);
        final PendingIntent pi = PendingIntent.getService(AppInstance.applicationContext, 0, i,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Clear any old alarms and schedule the new alarm
        final AlarmManager am = (AlarmManager) AppInstance.applicationContext
                .getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);

        if (Constants.UPDATE_FREQ_NONE != updateFreq) {
            am.setRepeating(AlarmManager.RTC_WAKEUP, lastCheck + updateFreq, updateFreq, pi);
        }
    }

    public static void cancelNotification(final int notificationId) {
        if (notificationId == -1000) return;
        final NotificationManager nm = (NotificationManager) AppInstance.applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(notificationId);
    }

    public static void collapseStatusbar() {
        if (isNameless()) {
            final StatusBarManager sb = (StatusBarManager)
                    AppInstance.applicationContext.getSystemService(Context.STATUS_BAR_SERVICE);
            sb.collapsePanels();
        }
    }

}
