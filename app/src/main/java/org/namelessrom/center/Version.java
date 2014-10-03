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

package org.namelessrom.center;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Version implements Parcelable, Serializable {

    private static final long serialVersionUID = 5499890003569313403L;

    public static final int CHANNEL_UNKNOWN   = -2;
    public static final int CHANNEL_EMPTY     = -1;
    public static final int CHANNEL_NIGHTLY   = 1;
    public static final int CHANNEL_WEEKLY    = 2;
    public static final int CHANNEL_MILESTONE = 3;
    public static final int CHANNEL_RC        = 4;
    public static final int CHANNEL_STABLE    = 5;

    @SerializedName("channel") public      String channel      = "-";
    @SerializedName("channelShort") public String channelShort = "-";
    @SerializedName("channelType") public  int    channelType  = -2;
    @SerializedName("filename") public     String name         = "-";
    @SerializedName("md5sum") public       String md5          = "-";
    @SerializedName("downloadurl") public  String url          = "-";
    @SerializedName("timestamp") public    int    timestamp    = -1;

    public Version() { }

    private Version(final Parcel in) { readFromParcel(in); }

    public Version(final String updateChannel, final String updateName, final String updateMd5,
            final String updateUrl, final int updateTimeStamp) {
        channel = updateChannel;
        name = updateName.replace(".zip", "");
        md5 = updateMd5;
        url = updateUrl;
        timestamp = updateTimeStamp;

        if (channel.equals("NIGHTLY")) {
            channelShort = "N";
            channelType = CHANNEL_NIGHTLY;
        } else if (channel.equals("WEEKLY")) {
            channelShort = "W";
            channelType = CHANNEL_WEEKLY;
        } else if (channel.equals("MILESTONE")) {
            channelShort = "M";
            channelType = CHANNEL_MILESTONE;
        } else if (channel.equals("RELEASECANDIDATE")) {
            channelShort = "RC";
            channelType = CHANNEL_RC;
        } else if (channel.equals("STABLE")) {
            channelShort = "S";
            channelType = CHANNEL_STABLE;
        } else if (channel.equals("---")) {
            channelShort = "";
            channelType = CHANNEL_EMPTY;
        } else if (!channel.isEmpty()) {
            channelShort = channel.substring(0, 1);
            channelType = CHANNEL_UNKNOWN;
        } else {
            channelShort = "?";
            channelType = CHANNEL_UNKNOWN;
        }
    }

    public static String getReadableName(final String name) {
        final String[] splitted = name.split("-");
        if (splitted.length > 2) {
            return splitted[0] + "-" + splitted[1] + "-" + splitted[2];
        }
        return name;
    }

    public String getReadableName() {
        return Version.getReadableName(name);
    }

    @Override public String toString() { return "Version: " + name; }

    @Override public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Version)) return false;

        final Version ui = (Version) o;
        return TextUtils.equals(channel, ui.channel)
                && TextUtils.equals(channelShort, ui.channelShort)
                && TextUtils.equals(name, ui.name)
                && TextUtils.equals(md5, ui.md5)
                && TextUtils.equals(url, ui.url)
                && timestamp == ui.timestamp
                && channelType == ui.channelType;
    }

    public static final Creator<Version> CREATOR = new Creator<Version>() {
        public Version createFromParcel(final Parcel in) { return new Version(in); }

        public Version[] newArray(final int size) { return new Version[size]; }
    };

    @Override public int describeContents() { return 0; }

    @Override public void writeToParcel(final Parcel parcel, final int i) {
        parcel.writeString(channel);
        parcel.writeString(channelShort);
        parcel.writeInt(channelType);
        parcel.writeString(name);
        parcel.writeString(md5);
        parcel.writeString(url);
        parcel.writeInt(timestamp);
    }

    private void readFromParcel(final Parcel in) {
        channel = in.readString();
        channelShort = in.readString();
        channelType = in.readInt();
        name = in.readString();
        md5 = in.readString();
        url = in.readString();
        timestamp = in.readInt();
    }

    public static int compare(final Version lhsVersion, final Version rhsVersion) {
        return lhsVersion.timestamp > rhsVersion.timestamp ? 1 : 0;
    }
}
