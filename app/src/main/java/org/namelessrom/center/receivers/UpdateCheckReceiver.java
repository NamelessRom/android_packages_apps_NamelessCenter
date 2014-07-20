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

package org.namelessrom.center.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.text.TextUtils;
import android.widget.Toast;

import org.namelessrom.center.Constants;
import org.namelessrom.center.Logger;
import org.namelessrom.center.R;
import org.namelessrom.center.database.DatabaseHandler;
import org.namelessrom.center.items.UpdateInfo;
import org.namelessrom.center.services.DownloadService;
import org.namelessrom.center.services.UpdateCheckService;
import org.namelessrom.center.utils.Helper;
import org.namelessrom.center.utils.PreferenceHelper;
import org.namelessrom.center.utils.UpdateHelper;

import java.io.IOException;

public class UpdateCheckReceiver extends BroadcastReceiver {
    public static final String ACTION_DOWNLOAD_UPDATE = "org.namelessrom.center.DOWNLOAD_UPDATE";
    public static final String ACTION_INSTALL_UPDATE  = "org.namelessrom.center.INSTALL_UPDATE";

    public static final String EXTRA_FILE        = "extra_file";
    public static final String EXTRA_ID          = "extra_id";
    public static final String EXTRA_UPDATE_INFO = "extra_update_info";

    @Override public void onReceive(final Context context, final Intent intent) {
        if (intent == null || TextUtils.isEmpty(intent.getAction())) return;

        final String action = intent.getAction();
        Logger.i(this, action);

        // Check if we want to install the update
        if (TextUtils.equals(action, ACTION_INSTALL_UPDATE)) {
            Helper.collapseStatusbar();
            final String fileName = intent.getStringExtra(EXTRA_FILE);
            try {
                UpdateHelper.triggerUpdate(fileName);
            } catch (IOException e) {
                Logger.e(this, "Unable to reboot into recovery mode: " + e.getMessage());
                Toast.makeText(context, R.string.unable_to_reboot_toast, Toast.LENGTH_SHORT).show();
                Helper.cancelNotification(intent.getIntExtra(EXTRA_ID, -1000));
            }
            return;
        } else if (TextUtils.equals(action, ACTION_DOWNLOAD_UPDATE)) {
            final UpdateInfo updateInfo = intent.getParcelableExtra(EXTRA_UPDATE_INFO);
            DownloadService.start(updateInfo);
            return;
        }

        final int updateFrequency = PreferenceHelper.getInt(Constants.UPDATE_CHECK_PREF,
                Constants.UPDATE_FREQ_WEEKLY);

        // Check if we are set to manual updates and don't do anything
        if (updateFrequency == Constants.UPDATE_FREQ_NONE) return;

        // Not set to manual updates, parse the received action
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            // Connectivity has changed
            final boolean hasConnection =
                    !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            Logger.v(this, "Got connectivity change, has connection: " + hasConnection);
            if (!hasConnection) return;
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            // We just booted. Store the boot check state
            PreferenceHelper.setBoolean(Constants.BOOT_CHECK_COMPLETED, false);
        }

        // Handle the actual update check based on the defined frequency
        if (updateFrequency == Constants.UPDATE_FREQ_AT_BOOT) {
            final boolean bootCheckCompleted =
                    PreferenceHelper.getBoolean(Constants.BOOT_CHECK_COMPLETED, false);
            if (!bootCheckCompleted) {
                Logger.v(this, "Start an on-boot check");
                Intent i = new Intent(context, UpdateCheckService.class);
                i.setAction(UpdateCheckService.ACTION_CHECK);
                context.startService(i);
            } else {
                // Nothing to do
                Logger.v(this, "On-boot update check was already completed.");
            }
        } else if (updateFrequency > 0) {
            Logger.v(this, "Scheduling future, repeating update checks.");
            Helper.scheduleUpdateService(updateFrequency * 1000);
        }

        // Tear down database
        DatabaseHandler.tearDown();
    }
}
