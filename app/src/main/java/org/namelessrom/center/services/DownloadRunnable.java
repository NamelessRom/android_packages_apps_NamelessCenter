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

package org.namelessrom.center.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.koushikdutta.ion.future.ResponseFuture;

import org.namelessrom.center.AppInstance;
import org.namelessrom.center.Constants;
import org.namelessrom.center.Logger;
import org.namelessrom.center.MainActivity;
import org.namelessrom.center.R;
import org.namelessrom.center.database.UpdateTable;
import org.namelessrom.center.events.DownloadErrorEvent;
import org.namelessrom.center.events.DownloadProgressEvent;
import org.namelessrom.center.items.UpdateInfo;
import org.namelessrom.center.receivers.UpdateCheckReceiver;
import org.namelessrom.center.utils.BusProvider;
import org.namelessrom.center.utils.Helper;
import org.namelessrom.center.utils.PreferenceHelper;
import org.namelessrom.center.utils.UpdateHelper;

import java.io.File;
import java.security.SecureRandom;

/**
 * A runnable for downloading rom updates
 */
public class DownloadRunnable implements Runnable, ProgressCallback {
    public static final int RESULT_OK    = 0;
    public static final int RESULT_ERROR = 1;

    public Thread               mThread;
    public ResponseFuture<File> fileResponseFuture;

    private final int NOTIFICATION_ID;

    private final DownloadService downloadService;
    private final String          id;
    private final UpdateInfo      updateInfo;

    private final Notification.Builder notificationBuilder;

    public DownloadRunnable(final DownloadService downloadService, final String id,
            final UpdateInfo info) {
        this.NOTIFICATION_ID = new SecureRandom().nextInt(Integer.MAX_VALUE);
        this.downloadService = downloadService;
        this.id = id;
        this.updateInfo = info;

        final Intent i = new Intent(AppInstance.applicationContext, MainActivity.class);
        i.setAction(MainActivity.ACTION_UPDATES);
        final PendingIntent pendingIntent = PendingIntent.getActivity(
                AppInstance.applicationContext, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        this.notificationBuilder = new Notification.Builder(downloadService)
                .setTicker(AppInstance.getStr(R.string.downloading_update))
                .setContentTitle(AppInstance.getStr(R.string.downloading_update))
                .setContentText(AppInstance.getStr(R.string.download_in_progress))
                .setSmallIcon(R.drawable.ic_launcher)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setProgress(0, 0, true)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false);

        Logger.v(this, "Starting download, id: " + this.id);
    }

    @Override public void run() {
        // Store current thread for interrupting if needed
        mThread = Thread.currentThread();
        if (updateInfo != null) {
            Logger.i(this, String.format("downloading: %s", updateInfo.getUrl()));
            UpdateTable.insertOrUpdate(updateInfo.getName(), updateInfo);
            final int result = enqueueDownload(updateInfo);

            if (result == RESULT_OK) {
                postProgress(0);
                downloadService.updateNotification(NOTIFICATION_ID, notificationBuilder.build());
                try {
                    fileResponseFuture.setCallback(new FutureCallback<File>() {
                        @Override public void onCompleted(final Exception e, final File result) {
                            if (e != null) {
                                showError();
                                return;
                            }
                            final String success = AppInstance.getStr(R.string.download_success);
                            notificationBuilder
                                    .setOngoing(false)
                                    .setContentTitle(success)
                                    .setTicker(success)
                                    .setContentText(updateInfo.getReadableName());

                            final Notification.BigTextStyle style = new Notification.BigTextStyle();
                            style.setBigContentTitle(success);
                            style.bigText(AppInstance.getStr(R.string.download_install_notice,
                                    updateInfo.getReadableName()));
                            notificationBuilder.setStyle(style);

                            final Intent installIntent = new Intent(AppInstance.applicationContext,
                                    UpdateCheckReceiver.class);
                            installIntent.setAction(UpdateCheckReceiver.ACTION_INSTALL_UPDATE);
                            installIntent.putExtra(
                                    UpdateCheckReceiver.EXTRA_FILE, updateInfo.getZipName());
                            installIntent.putExtra(
                                    UpdateCheckReceiver.EXTRA_ID, NOTIFICATION_ID);

                            final PendingIntent installPi = PendingIntent.getBroadcast(
                                    AppInstance.applicationContext, 0, installIntent,
                                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT
                            );
                            notificationBuilder.addAction(R.drawable.ic_stat_notify_install,
                                    AppInstance.getStr(R.string.reboot_and_install), installPi);

                            downloadService.updateNotification(
                                    NOTIFICATION_ID, notificationBuilder.build());
                        }
                    }).get();
                } catch (Exception exc) {
                    Logger.e(this, exc.getMessage());
                } finally {
                    // Tell we are done
                    postProgress(101);
                }
            } else {
                showError();
            }
        }
    }

    private int enqueueDownload(final UpdateInfo updateInfo) {
        // Check if we are online...
        if (!Helper.isOnline()) {
            postError(DownloadErrorEvent.REASON_OFFLINE);
            return RESULT_ERROR;
        }
        // Check if we are metered
        if (Helper.isMetered()) {
            boolean metered = !PreferenceHelper.getBoolean(Constants.PREF_UPDATE_METERED, true);
            // Post error if the user has forbidden it
            if (metered) {
                postError(DownloadErrorEvent.REASON_METERED);
                return RESULT_ERROR;
            } else {
                metered = !PreferenceHelper.getBoolean(
                        Constants.PREF_UPDATE_METERED_SKIP_WARNING, false);

                if (metered) {
                    // User is not skipping warnings, lets tell him that something is fishy
                    postError(DownloadErrorEvent.REASON_METERED_WARN);
                }
            }
        }
        // Check if we roam
        if (Helper.isRoaming()) {
            final boolean roaming = !PreferenceHelper.getBoolean(
                    Constants.PREF_UPDATE_ROAMING, false);
            if (roaming) {
                // we are roaming and user does not want it, jump out of here, FAST!!!
                postError(DownloadErrorEvent.REASON_ROAMING);
                return RESULT_ERROR;
            }
        }
        fileResponseFuture = Ion.with(downloadService)
                .load(updateInfo.getUrl())
                .progress(this)
                .write(UpdateHelper.getUpdateFile(updateInfo.getZipName()));

        return RESULT_OK;
    }

    private int oldPercentage = -1;

    @Override public void onProgress(final long downloaded, final long total) {
        final int percentage = (int) (downloaded * 100.0 / total + 0.5);
        if (downloadService != null && oldPercentage != percentage) {
            Logger.v(this, "updating");
            oldPercentage = percentage;
            notificationBuilder.setProgress(100, percentage, false);
            downloadService.updateNotification(NOTIFICATION_ID, notificationBuilder.build());

            postProgress(percentage);
        }
    }

    private void postProgress(final int percentage) {
        AppInstance.getHandler().post(new Runnable() {
            @Override public void run() {
                BusProvider.getBus().post(new DownloadProgressEvent(id, percentage));
            }
        });
    }

    private void postError(final int reason) {
        AppInstance.getHandler().post(new Runnable() {
            @Override public void run() {
                BusProvider.getBus().post(new DownloadErrorEvent(reason));
            }
        });
    }

    private void showError() {
        notificationBuilder
                .setOngoing(false)
                .setContentTitle(AppInstance.getStr(R.string.download_failure))
                .setContentText(AppInstance.getStr(R.string.unable_to_download_file))
                .setTicker(AppInstance.getStr(R.string.download_failure));

        downloadService.updateNotification(NOTIFICATION_ID, notificationBuilder.build());
    }

    public String getId() { return this.id; }
}
