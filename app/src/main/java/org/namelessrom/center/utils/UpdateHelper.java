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

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Environment;
import android.os.FileUtils;
import android.os.PowerManager;
import android.os.Process;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import org.namelessrom.center.AppInstance;
import org.namelessrom.center.Constants;
import org.namelessrom.center.R;
import org.namelessrom.center.events.DownloadErrorEvent;
import org.namelessrom.center.events.DownloadProgressEvent;
import org.namelessrom.center.items.UpdateInfo;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Another helper for updates
 */
public class UpdateHelper {

    public static AlertDialog getDeleteDialog(final Context context, final UpdateInfo updateInfo) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(R.string.delete_update);
        builder.setMessage(context.getString(
                R.string.delete_update_message, updateInfo.getReadableName()));
        builder.setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }
        );
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialogInterface, int i) {
                UpdateHelper.deleteUpdate(updateInfo.getZipName());
                BusProvider.getBus().post(new DownloadProgressEvent("0", 101));
            }
        });

        return builder.create();
    }

    public static AlertDialog getDownloadErrorDialog(final Context context, final int reason) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        final int title, message;
        switch (reason) {
            default:
            case DownloadErrorEvent.REASON_UNKNOWN:
                title = R.string.error;
                message = R.string.error_unknown;
                break;
            case DownloadErrorEvent.REASON_OFFLINE:
                title = R.string.error;
                message = R.string.error_offline;
                break;
            case DownloadErrorEvent.REASON_METERED:
                title = R.string.error;
                message = R.string.error_metered;
                break;
            case DownloadErrorEvent.REASON_METERED_WARN:
                title = R.string.warning;
                message = R.string.warning_metered;
                break;
            case DownloadErrorEvent.REASON_ROAMING:
                title = R.string.error;
                message = R.string.error_roaming;
                break;
        }
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        return builder.create();
    }

    private static String getStorageMountpoint() {
        final StorageManager sm = (StorageManager) AppInstance.applicationContext
                .getSystemService(Context.STORAGE_SERVICE);
        final StorageVolume[] volumes = sm.getVolumeList();
        final String primaryStoragePath =
                Environment.getExternalStorageDirectory().getAbsolutePath();
        final boolean alternateIsInternal = AppInstance.applicationContext
                .getResources().getBoolean(R.bool.alternateIsInternal);

        if (volumes.length <= 1) {
            // single storage, assume only /sdcard exists
            return "/sdcard";
        }

        for (StorageVolume v : volumes) {
            if (v.getPath().equals(primaryStoragePath)) {
                if (!v.isRemovable() && alternateIsInternal) {
                    return "/emmc";
                }
            }
        }
        // Not found, assume non-alternate
        return "/sdcard";
    }

    public static void runShellCommands(final String... cmds) throws IOException {
        final java.lang.Process p = Runtime.getRuntime().exec("sh");
        final DataOutputStream os = new DataOutputStream(p.getOutputStream());
        try {
            for (final String s : cmds) {
                writeString(os, s);
            }
            writeString(os, "exit\n");
        } finally {
            os.flush();
            os.close();
        }
    }

    private static void writeString(final OutputStream os, final String s) throws IOException {
        os.write((s + "\n").getBytes("UTF-8"));
    }

    private static List<String> getFlashAfterUpdateZIPs() {
        final List<String> extras = new ArrayList<String>();
        final File[] files = (new File(Constants.UPDATE_FOLDER_ADDITIONAL)).listFiles();
        String filename;

        if (files != null) {
            for (final File f : files) {
                if (f.getName().toLowerCase(Locale.ENGLISH).endsWith(".zip")) {
                    filename = f.getAbsolutePath();
                    if (filename.startsWith(Constants.UPDATE_FOLDER_FULL)) {
                        extras.add(filename.replace(Constants.UPDATE_FOLDER_FULL + "/", ""));
                    }
                }
            }
            Collections.sort(extras);
        }

        return extras;
    }

    private static void createOpenRecoveryScript(final String root, final String filename,
            final List<String> files)
            throws IOException {
        final FileOutputStream os = new FileOutputStream("/cache/recovery/openrecoveryscript",
                false);
        try {
            writeString(os, "set tw_signed_zip_verify 0");
            writeString(os, String.format("install %s", filename));

            for (final String file : files) {
                writeString(os, String.format("install %s", root + file));
            }
            writeString(os, "wipe cache");
        } finally {
            os.close();
        }

        FileUtils.setPermissions("/cache/recovery/openrecoveryscript", 0644,
                android.os.Process.myUid(), 2001);
    }

    private static void createCwmScript(final String root, final String filename,
            final List<String> files) throws IOException {
        final FileOutputStream os = new FileOutputStream("/cache/recovery/extendedcommand", false);
        try {
            writeString(os, String.format("install_zip(\"%s\");", filename));

            for (String file : files) {
                writeString(os, String.format("install_zip(\"%s\");", root + file));
            }
            writeString(os, "run_program(\"/sbin/busybox\", \"rm\", \"-rf\", \"/cache/*\");");
        } finally {
            os.close();
        }

        FileUtils.setPermissions("/cache/recovery/extendedcommand", 0644, Process.myUid(), 2001);
    }

    public static void triggerUpdate(final String updateFileName)
            throws IOException {
        // Add the update folder/file name
        // Emulated external storage moved to user-specific paths in 4.2
        final String userPath = Environment.isExternalStorageEmulated()
                ? ("/" + UserHandle.myUserId())
                : "";

        final String root = getStorageMountpoint() + userPath + "/" +
                Constants.UPDATE_FOLDER + "/";
        final String flashFilename = root + updateFileName;

        final List<String> extras = getFlashAfterUpdateZIPs();

        runShellCommands("mkdir -p /cache/recovery/;\n");

        final int flashType = PreferenceHelper
                .getInt(Constants.PREF_RECOVERY_TYPE, Constants.RECOVERY_TYPE_BOTH);
        if (Constants.RECOVERY_TYPE_CWM == flashType) {
            createCwmScript(root, flashFilename, extras);
        } else if (Constants.RECOVERY_TYPE_OPEN == flashType) {
            createOpenRecoveryScript(root, flashFilename, extras);
        } else {
            createCwmScript(root, flashFilename, extras);
            createOpenRecoveryScript(root, flashFilename, extras);
        }

        // Trigger the reboot
        final PowerManager powerManager = (PowerManager)
                AppInstance.applicationContext.getSystemService(Context.POWER_SERVICE);
        powerManager.reboot("recovery");
    }

    public static boolean deleteUpdate(final String filename) {
        final File update = getUpdateFile(filename);
        return (update.exists() && update.delete());
    }

    public static File getUpdateFile(final String filename) {
        return new File(Constants.UPDATE_FOLDER_FULL + File.separator + filename);
    }

    public static boolean isUpdateDownloaded(final String filename) {
        return getUpdateFile(filename).exists();
    }

    public static long downloadUpdate(final Context context, final UpdateInfo info) {
        // build our request
        final DownloadManager.Request req = new DownloadManager.Request(Uri.parse(info.getUrl()));

        req.setTitle(info.getName());
        req.setDescription(info.getUrl());
        req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        req.setVisibleInDownloadsUi(false);
        req.setAllowedOverMetered(PreferenceHelper)
    }

}
