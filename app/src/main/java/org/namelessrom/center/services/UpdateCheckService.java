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
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.os.IBinder;
import android.os.Parcelable;
import android.text.TextUtils;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.namelessrom.center.AppInstance;
import org.namelessrom.center.Constants;
import org.namelessrom.center.Logger;
import org.namelessrom.center.MainActivity;
import org.namelessrom.center.R;
import org.namelessrom.center.items.UpdateInfo;
import org.namelessrom.center.receivers.UpdateCheckReceiver;
import org.namelessrom.center.services.dashclock.RomUpdateDashclockExtension;
import org.namelessrom.center.utils.BusProvider;
import org.namelessrom.center.utils.Helper;
import org.namelessrom.center.utils.PreferenceHelper;
import org.namelessrom.center.utils.UpdateHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class UpdateCheckService extends Service {

    // request actions
    public static final String ACTION_CHECK        = "org.namelessrom.center.action.CHECK";
    public static final String ACTION_CHECK_UI     = "org.namelessrom.center.action.CHECK_UI";
    public static final String ACTION_CANCEL_CHECK = "org.namelessrom.center.action.CANCEL_CHECK";

    // max. number of updates listed in the expanded notification
    private static final int EXPANDED_NOTIF_UPDATE_COUNT = 4;

    private String mAction;

    @Override public IBinder onBind(final Intent intent) { return null; }

    @Override public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (intent == null) return START_NOT_STICKY;
        mAction = intent.getAction();

        if (TextUtils.equals(mAction, ACTION_CANCEL_CHECK) || !Helper.isOnline()) {
            // Only check for updates if the device is actually connected to a network
            postBus(null);
            return START_NOT_STICKY;
        }

        Ion.with(this).load(getUpdateUrl()).noCache().as(UpdateInfo[].class).setCallback(mCallBack);

        return super.onStartCommand(intent, flags, startId);
    }

    private String getUpdateUrl() {
        String url = "";
        String query = "";
        final int updateChannel = PreferenceHelper.getInt(
                Constants.PREF_UPDATE_CHANNEL, Constants.UPDATE_CHANNEL_ALL);
        switch (updateChannel) {
            case Constants.UPDATE_CHANNEL_ALL:
                url = Constants.CHANNEL_ALL;
                break;
            case Constants.UPDATE_CHANNEL_NIGHTLY:
                url = Constants.CHANNEL_NIGHTLY;
                break;
            case Constants.UPDATE_CHANNEL_WEEKLY:
                url = Constants.CHANNEL_WEEKLY;
                query = "?display=full";
                break;
        }

        if (!url.isEmpty()) {
            url = Constants.ROM_URL + "/" + url;
        } else {
            url = Constants.ROM_URL;
        }

        url += '/' + Helper.readBuildProp("ro.nameless.device") + query;
        Logger.v(this, "getUpdateUrl(): " + url);

        return url;
    }

    private final FutureCallback<UpdateInfo[]> mCallBack = new FutureCallback<UpdateInfo[]>() {
        @Override public void onCompleted(Exception e, UpdateInfo[] result) {
            if (result == null || e != null) {
                // post back null, the receiver can handle null
                postBus(null);
                return;
            }

            final List<UpdateInfo> list = Arrays.asList(result);
            final ArrayList<UpdateInfo> updates = new ArrayList<UpdateInfo>();
            final int currentDate = Helper.getBuildDate();

            for (final UpdateInfo info : list) {
                final String channel = info.getChannel();
                final String name = info.getName();
                final String md5sum = info.getMd5();
                final String urlFile = info.getUrl();
                final String timeStamp = info.getTimestamp();
                if (currentDate < Helper.parseDate(timeStamp) || info.isDownloaded()) {
                    updates.add(new UpdateInfo(channel, name, md5sum, urlFile, timeStamp));
                }
            }

            final Intent updateIntent =
                    new Intent(RomUpdateDashclockExtension.ACTION_DATA_UPDATE);
            updateIntent.putParcelableArrayListExtra(RomUpdateDashclockExtension.EXTRA_UPDATES,
                    updates);
            sendBroadcast(updateIntent);

            if (ACTION_CHECK_UI.equals(mAction)) {
                // post back the result, do not build notifications
                postBus(updates);
                return;
            }

            int realUpdateCount = 0;
            for (final UpdateInfo ui : updates) {
                if (Helper.parseDate(ui.getTimestamp()) > Helper.getBuildDate()) {
                    realUpdateCount++;
                }
            }

            // Store the last update check time and ensure boot check completed is true
            PreferenceHelper.setString(
                    Constants.LAST_UPDATE_CHECK_PREF, String.valueOf(new Date().getTime()));
            PreferenceHelper.setBoolean(Constants.BOOT_CHECK_COMPLETED, true);

            if (realUpdateCount != 0) {
                // There are updates available
                // The notification should launch the main app
                Intent i = new Intent(AppInstance.get(), MainActivity.class);
                i.setAction(MainActivity.ACTION_UPDATES);
                final PendingIntent contentIntent = PendingIntent.getActivity(
                        UpdateCheckService.this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

                final Resources res = getResources();
                final String text = res.getQuantityString(R.plurals.not_new_updates_found_body,
                        realUpdateCount, realUpdateCount);

                // Get the notification ready
                Notification.Builder builder = new Notification.Builder(UpdateCheckService.this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setWhen(System.currentTimeMillis())
                        .setTicker(res.getString(R.string.new_updates_found_ticker))
                        .setContentTitle(res.getString(R.string.new_updates_found_title))
                        .setContentText(text)
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true)
                        .setOngoing(false);

                final Notification.InboxStyle inbox = new Notification.InboxStyle(builder)
                        .setBigContentTitle(text);
                int added = 0;

                for (final UpdateInfo ui : updates) {
                    if (added < EXPANDED_NOTIF_UPDATE_COUNT) {
                        if (Helper.parseDate(ui.getTimestamp()) > Helper.getBuildDate()) {
                            inbox.addLine(ui.getName());
                            added++;
                        }
                    }
                }
                if (added != realUpdateCount) {
                    inbox.setSummaryText(res.getQuantityString(R.plurals.not_additional_count,
                            realUpdateCount - added, realUpdateCount - added));
                }
                builder.setStyle(inbox);
                builder.setNumber(realUpdateCount);

                if (realUpdateCount == 1) {
                    final UpdateInfo updateInfo = updates.get(0);

                    if (!UpdateHelper.isUpdateDownloaded(updateInfo.getZipName())) {
                        i = new Intent(UpdateCheckService.this, UpdateCheckReceiver.class);
                        i.setAction(UpdateCheckReceiver.ACTION_DOWNLOAD_UPDATE);
                        i.putExtra(UpdateCheckReceiver.EXTRA_UPDATE_INFO, (Parcelable) updateInfo);
                        final PendingIntent pendingIntent = PendingIntent.getBroadcast(
                                UpdateCheckService.this, 0, i,
                                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT);
                        builder.addAction(R.drawable.ic_stat_notify_download,
                                res.getString(R.string.download), pendingIntent);
                    }
                }

                // Trigger the notification
                final NotificationManager nm =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                nm.notify(R.string.new_updates_found_title, builder.build());
            }

            // post back the result
            postBus(updates);
        }
    };

    private void postBus(final ArrayList<UpdateInfo> updates) {
        BusProvider.getBus().post(updates == null ? new ArrayList<UpdateInfo>(0) : updates);
        stopSelf();
    }
}
