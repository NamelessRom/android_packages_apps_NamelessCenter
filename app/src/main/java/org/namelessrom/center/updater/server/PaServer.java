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

import com.google.gson.Gson;

import org.json.JSONObject;
import org.namelessrom.center.Constants;
import org.namelessrom.center.Logger;
import org.namelessrom.center.Version;
import org.namelessrom.center.updater.Server;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PaServer implements Server {

    @Override
    public String getUrl(final String device) {
        String url = "";
        String query = "";
        /*final int updateChannel = new SettingsHelper().getInt(
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
        }*/

        if (!url.isEmpty()) {
            url = Constants.ROM_URL + "/" + url;
        } else {
            url = Constants.ROM_URL;
        }

        url += '/' + device + query;
        Logger.v(this, "getUpdateUrl(): " + url);

        return url;
    }

    @Override
    public List<Version> createPackageInfoList(JSONObject response) throws Exception {
        final List<Version> list = Arrays.asList(
                new Gson().fromJson(response.toString(), Version[].class));
        for (final Version version : list) {
            // TODO: check if newer
        }

        Collections.sort(list, new Comparator<Version>() {

            @Override
            public int compare(Version lhs, Version rhs) {
                return Version.compare(lhs, rhs);
            }

        });
        Collections.reverse(list);
        return list;
    }

}
