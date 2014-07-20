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
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Parcelable;
import android.text.TextUtils;

import org.namelessrom.center.AppInstance;
import org.namelessrom.center.Logger;
import org.namelessrom.center.cards.RomUpdateCard;
import org.namelessrom.center.events.DownloadProgressEvent;
import org.namelessrom.center.items.UpdateInfo;
import org.namelessrom.center.utils.BusProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DownloadService extends Service {
    public static final BlockingQueue<Runnable> mWorkQueue = new LinkedBlockingQueue<Runnable>();

    public static ThreadPoolExecutor mThreadPool;

    public static final String ACTION_STOP = "action_stop";

    private static final String EXTRA_ID   = "extra_id";
    private static final String EXTRA_INFO = "extra_info";

    private static final ArrayList<DownloadRunnable> mRunnables = new ArrayList<DownloadRunnable>();

    private static final Object LOCK = new Object();

    private static NotificationManager notificationManager = null;

    public static synchronized void start(final UpdateInfo updateInfo) {
        final Intent i = new Intent(AppInstance.applicationContext, DownloadService.class);
        i.putExtra(EXTRA_ID, updateInfo.getTimestamp());
        i.putExtra(EXTRA_INFO, (Parcelable) updateInfo);
        AppInstance.applicationContext.startService(i);
    }

    public static synchronized void stop() {
        final Intent i = new Intent(AppInstance.applicationContext, DownloadService.class);
        i.setAction(ACTION_STOP);
        AppInstance.applicationContext.startService(i);
    }

    @Override public IBinder onBind(final Intent intent) { return null; }

    @Override public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (intent == null && mWorkQueue.size() <= 0) {
            tearDown();
            return START_NOT_STICKY;
        }
        if (intent != null) {
            final String action = intent.getAction();
            if (TextUtils.equals(action, ACTION_STOP)) {
                tearDown();
                return START_NOT_STICKY;
            }

            final String id = intent.getStringExtra(EXTRA_ID);
            if (!TextUtils.isEmpty(id)) {
                final UpdateInfo info = intent.getParcelableExtra(EXTRA_INFO);
                final DownloadRunnable runnable = new DownloadRunnable(this, id, info);
                mRunnables.add(runnable);
                getThreadPoolExecutor().execute(runnable);
            }
        }
        return START_STICKY;
    }

    private void tearDown() {
        cancelDownload("");
        final List<Runnable> canceledRunnables = getThreadPoolExecutor().shutdownNow();
        Logger.v(this, String.format("canceled %s tasks", canceledRunnables.size()));
    }

    public static synchronized ThreadPoolExecutor getThreadPoolExecutor() {
        if (mThreadPool == null) {
            mThreadPool = new ThreadPoolExecutor(4, 4, 1, TimeUnit.SECONDS, mWorkQueue);
        }
        return mThreadPool;
    }

    private NotificationManager getNotificationManager() {
        if (notificationManager == null) {
            notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }

    public void updateNotification(final int notificationId, final Notification notification) {
        getNotificationManager().notify(notificationId, notification);
    }

    public void cancelNotification(final int notificationId) {
        getNotificationManager().cancel(notificationId);
    }

    public static void cancelDownload(final String id) {
        Logger.v(DownloadService.class, "canceling id: " + id);
        final DownloadRunnable[] runnableArray = new DownloadRunnable[mWorkQueue.size()];
        mWorkQueue.toArray(runnableArray);
        final int len = runnableArray.length;

        synchronized (LOCK) {
            for (final DownloadRunnable downloadRunnable : mRunnables) {
                if (TextUtils.isEmpty(id) || TextUtils.equals(downloadRunnable.getId(), id)) {
                    downloadRunnable.fileResponseFuture.cancel(true);
                    try {
                        downloadRunnable.mThread.interrupt();
                    } catch (Exception ignored) { }
                    mRunnables.remove(downloadRunnable);
                }
            }

            Thread thread;
            for (int i = 0; i < len; i++) {
                thread = runnableArray[i].mThread;

                if (thread != null && (TextUtils.isEmpty(id)
                        || TextUtils.equals(runnableArray[i].getId(), id))) {
                    try {
                        runnableArray[i].fileResponseFuture.cancel(true);
                        thread.interrupt();
                        mWorkQueue.remove(runnableArray[i]);
                        getThreadPoolExecutor().remove(runnableArray[i]);
                    } catch (Exception ignored) { }
                }
            }
            getThreadPoolExecutor().purge();
        }

        AppInstance.getHandler().post(new Runnable() {
            @Override public void run() {
                BusProvider.getBus().post(new DownloadProgressEvent("-1", 0));
            }
        });
    }
}
