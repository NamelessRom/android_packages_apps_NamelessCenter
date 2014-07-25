package org.namelessrom.center.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.namelessrom.center.services.dashclock.RomUpdateDashclockExtension;

/**
 * Broadcastreceiver for notifying dashclock when rom updates are found
 */
public class DashclockUpdateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        // build an intent for the dashclock extension service
        final Intent i = new Intent(context, RomUpdateDashclockExtension.class);
        i.setAction(RomUpdateDashclockExtension.ACTION_DATA_UPDATE);
        i.putParcelableArrayListExtra(RomUpdateDashclockExtension.EXTRA_UPDATES,
                intent.getParcelableArrayListExtra(RomUpdateDashclockExtension.EXTRA_UPDATES));
        context.startService(i);
    }
}
