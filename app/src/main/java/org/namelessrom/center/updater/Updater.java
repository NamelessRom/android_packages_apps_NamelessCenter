/*
 * Copyright 2014 ParanoidAndroid Project
 *
 * This file is part of Paranoid OTA.
 *
 * Paranoid OTA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Paranoid OTA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Paranoid OTA.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.namelessrom.center.updater;

import android.app.Activity;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;
import org.namelessrom.center.R;
import org.namelessrom.center.Utils;
import org.namelessrom.center.Version;
import org.namelessrom.center.helpers.SettingsHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Updater implements Response.Listener<JSONArray>, Response.ErrorListener {

    public static final String PROPERTY_DEVICE     = "ro.nameless.device";

    public static final int NOTIFICATION_ID = 122303224;

    public static interface UpdaterListener {

        public void startChecking();

        public void versionFound(Version[] info);

        public void checkError(String cause);
    }

    private Context  mContext;
    private Server[] mServers;
    private Version[]         mLastUpdates = new Version[0];
    private List<UpdaterListener> mListeners   = new ArrayList<UpdaterListener>();
    private RequestQueue   mQueue;
    private SettingsHelper mSettingsHelper;
    private Server         mServer;
    private boolean mScanning = false;
    private boolean mFromAlarm;
    private boolean mServerWorks   = false;
    private int     mCurrentServer = -1;

    public Updater(Context context, Server[] servers, boolean fromAlarm) {
        mContext = context;
        mServers = servers;
        mFromAlarm = fromAlarm;
        mQueue = Volley.newRequestQueue(context);
    }

    public abstract String getDevice();

    public abstract boolean isRom();

    public abstract int getErrorStringId();

    protected Context getContext() {
        return mContext;
    }

    public SettingsHelper getSettingsHelper() {
        return mSettingsHelper;
    }

    public Version[] getLastUpdates() {
        return mLastUpdates;
    }

    public void setLastUpdates(Version[] infos) {
        if (infos == null) {
            infos = new Version[0];
        }
        mLastUpdates = infos;
    }

    public void addUpdaterListener(UpdaterListener listener) {
        mListeners.add(listener);
    }

    public void check() {
        check(false);
    }

    public void check(boolean force) {
        if (mScanning) {
            return;
        }
        if (mSettingsHelper == null) {
            mSettingsHelper = new SettingsHelper(getContext());
        }
        if (mFromAlarm) {
            if (!force && mSettingsHelper.getCheckTime() < 0) {
                return;
            }
        }
        mServerWorks = false;
        mScanning = true;
        fireStartChecking();
        nextServerCheck();
    }

    protected void nextServerCheck() {
        mScanning = true;
        mCurrentServer++;
        mServer = mServers[mCurrentServer];
        final JsonArrayRequest req = new JsonArrayRequest(mServer.getUrl(getDevice()), this, this);
        mQueue.add(req);
    }

    @Override
    public void onResponse(JSONArray response) {
        mScanning = false;
        try {
            setLastUpdates(null);
            final List<Version> list = mServer.createPackageInfoList(response);
            final Version[] lastUpdates = list.toArray(new Version[list.size()]);
            if (lastUpdates.length > 0) {
                mServerWorks = true;
                if (mFromAlarm) {
                    Utils.showNotification(getContext(), lastUpdates);
                }
            } else {
                // TODO: server check?
                mServerWorks = true;
                if (mCurrentServer < mServers.length - 1) {
                    nextServerCheck();
                    return;
                }
            }
            mCurrentServer = -1;
            setLastUpdates(lastUpdates);
            fireCheckCompleted(lastUpdates);
        } catch (Exception ex) {
            System.out.println(response.toString());
            ex.printStackTrace();
            versionError(null);
        }
    }

    @Override
    public void onErrorResponse(VolleyError ex) {
        mScanning = false;
        ex.printStackTrace();
        versionError(null);
    }

    private boolean versionError(String error) {
        if (mCurrentServer < mServers.length - 1) {
            nextServerCheck();
            return true;
        }
        if (!mFromAlarm && !mServerWorks) {
            int id = getErrorStringId();
            if (error != null) {
                Utils.showToastOnUiThread(getContext(), getContext().getResources().getString(id)
                        + ": " + error);
            } else {
                if (id != R.string.check_gapps_updates_error) {
                    Utils.showToastOnUiThread(getContext(), id);
                }
            }
        }
        mCurrentServer = -1;
        fireCheckCompleted(null);
        fireCheckError(error);
        return false;
    }

    public boolean isScanning() {
        return mScanning;
    }

    public void removeUpdaterListener(UpdaterListener listener) {
        mListeners.remove(listener);
    }

    protected void fireStartChecking() {
        if (mContext instanceof Activity) {
            ((Activity) mContext).runOnUiThread(new Runnable() {

                public void run() {
                    for (UpdaterListener listener : mListeners) {
                        listener.startChecking();
                    }
                }
            });
        }
    }

    protected void fireCheckCompleted(final Version[] info) {
        if (mContext instanceof Activity) {
            ((Activity) mContext).runOnUiThread(new Runnable() {

                public void run() {
                    for (UpdaterListener listener : mListeners) {
                        listener.versionFound(info);
                    }
                }
            });
        }
    }

    protected void fireCheckError(final String cause) {
        if (mContext instanceof Activity) {
            ((Activity) mContext).runOnUiThread(new Runnable() {

                public void run() {
                    for (UpdaterListener listener : mListeners) {
                        listener.checkError(cause);
                    }
                }
            });
        }
    }
}
