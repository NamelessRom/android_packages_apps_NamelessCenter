package org.namelessrom.center.services.dashclock;

import android.content.Intent;
import android.content.res.Resources;
import android.text.TextUtils;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

import org.namelessrom.center.MainActivity;
import org.namelessrom.center.R;
import org.namelessrom.center.items.UpdateInfo;

import java.util.ArrayList;

/**
 * A dashclock extension for displaying rom updates
 */
public class RomUpdateDashclockExtension extends DashClockExtension {

    public static final String ACTION_DATA_UPDATE =
            "org.namelessrom.center.action.DASHCLOCK_ROM_UPDATE";
    public static final String EXTRA_UPDATES      = "extra_updates";

    private ArrayList<UpdateInfo> mUpdateList;
    private boolean mInitialized = false;

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (intent != null && TextUtils.equals(intent.getAction(), ACTION_DATA_UPDATE)) {
            if (mInitialized) {
                mUpdateList = intent.getParcelableArrayListExtra(EXTRA_UPDATES);
                onUpdateData(UPDATE_REASON_CONTENT_CHANGED);
            }
            return START_NOT_STICKY;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onInitialize(final boolean isReconnect) {
        super.onInitialize(isReconnect);
        mInitialized = true;
    }

    @Override
    protected void onUpdateData(final int i) {
        final Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(MainActivity.ACTION_UPDATES);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        if (mUpdateList == null) return;
        final StringBuilder sb = new StringBuilder();
        final Resources res = getResources();
        final int count = mUpdateList.size();

        for (final UpdateInfo info : mUpdateList) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(info.getName());
        }

        publishUpdate(new ExtensionData()
                .visible(!mUpdateList.isEmpty())
                .icon(R.drawable.ic_launcher)
                .status(res.getQuantityString(R.plurals.extension_status, count, count))
                .expandedTitle(
                        res.getQuantityString(R.plurals.extension_expandedTitle, count, count))
                .expandedBody(sb.toString())
                .clickIntent(intent));
    }
}
