package org.namelessrom.center.receivers;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

/**
 * Created by alex on 29.09.14.
 */
public class DownloadReceiver extends BroadcastReceiver {

    @Override public void onReceive(final Context context, final Intent intent) {
        // return if intent is null
        if (intent == null) return;

        // get the action
        final String action = intent.getAction();

        // return if action is empty or null
        if (TextUtils.isEmpty(action)) return;

        if (TextUtils.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE, action)) {
            // get the id of the completed download
            final long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (id == -1) return;

            // TODO: handle download
        }

    }
}
