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

package org.namelessrom.center.updater.server;

import android.text.TextUtils;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.namelessrom.center.Logger;
import org.namelessrom.center.Utils;
import org.namelessrom.center.Version;
import org.namelessrom.center.updater.Server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NamelessServer implements Server {

    private static final boolean LOCALHOST_TESTING = false;
    public static final  String  URL               = LOCALHOST_TESTING
            ? "http://192.168.43.234:3000" : "https://api.nameless-rom.org";
    public static final  String  API_URL           = URL + "/version";
    public static final  String  ROM_URL           = URL + "/update";

    @Override
    public String getUrl(final String device, final String channel, final String query) {
        String url = ROM_URL;

        // append the channel if not empty
        if (!TextUtils.isEmpty(channel)) {
            url += '/' + channel;
        }

        // append the device
        url += '/' + device;

        // append the query if not empty
        if (!TextUtils.isEmpty(query)) {
            url += query;
        }

        Logger.v(this, "getUpdateUrl(): " + url);
        return url;
    }

    @Override
    public List<Version> createPackageInfoList(JSONArray response) {
        final List<Version> list = Arrays.asList(
                new Gson().fromJson(response.toString(), Version[].class));
        int masterTimestamp;
        try {
            masterTimestamp = Integer.parseInt(Utils.getProp(Utils.BUILD_DATE));
        } catch (NumberFormatException exc) {
            masterTimestamp = 20141001;
        }

        final ArrayList<Version> updates = new ArrayList<Version>();
        for (final Version version : list) {
            if (masterTimestamp < version.timestamp) {
                updates.add(version);
            }
        }

        Collections.sort(updates, new Comparator<Version>() {

            @Override
            public int compare(Version lhs, Version rhs) {
                return Version.compare(lhs, rhs);
            }

        });
        return updates;
    }

}
