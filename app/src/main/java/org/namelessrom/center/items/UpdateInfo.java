/*
 * Copyright (C) 2014 Alexander "Evisceration" Martinz
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses
 */

package org.namelessrom.center.items;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Describes our Update Information returned by the API
 */
public class UpdateInfo implements Parcelable, Serializable {

    private static final long serialVersionUID = 5499890003569313403L;

    public static final int CHANNEL_UNKNOWN   = -2;
    public static final int CHANNEL_EMPTY     = -1;
    public static final int CHANNEL_NIGHTLY   = 1;
    public static final int CHANNEL_WEEKLY    = 2;
    public static final int CHANNEL_MILESTONE = 3;
    public static final int CHANNEL_RC        = 4;
    public static final int CHANNEL_STABLE    = 5;

    @SerializedName("channel") private     String mChannel   = "-";
    @SerializedName("filename") private    String mName      = "-";
    @SerializedName("md5sum") private      String mMd5       = "-";
    @SerializedName("downloadurl") private String mUrl       = "-";
    @SerializedName("timestamp") private   String mTimestamp = "-";

    private String mChannelShort = "-";
    private int    mChannelType  = -2;

    private boolean mIsDownloading = false;

    public UpdateInfo() { }

    private UpdateInfo(final Parcel in) { readFromParcel(in); }

    public UpdateInfo(final String updateChannel, final String updateName, final String updateMd5,
            final String updateUrl, final String updateTimeStamp) {
        mChannel = updateChannel;
        mName = updateName;
        mMd5 = updateMd5;
        mUrl = updateUrl;
        mTimestamp = updateTimeStamp;

        if (mChannel.equals("NIGHTLY")) {
            mChannelShort = "N";
            mChannelType = CHANNEL_NIGHTLY;
        } else if (mChannel.equals("WEEKLY")) {
            mChannelShort = "W";
            mChannelType = CHANNEL_WEEKLY;
        } else if (mChannel.equals("MILESTONE")) {
            mChannelShort = "M";
            mChannelType = CHANNEL_MILESTONE;
        } else if (mChannel.equals("RELEASECANDIDATE")) {
            mChannelShort = "RC";
            mChannelType = CHANNEL_RC;
        } else if (mChannel.equals("STABLE")) {
            mChannelShort = "S";
            mChannelType = CHANNEL_STABLE;
        } else if (mChannel.equals("---")) {
            mChannelShort = "";
            mChannelType = CHANNEL_EMPTY;
        } else if (!mChannel.isEmpty()) {
            mChannelShort = mChannel.substring(0, 1);
            mChannelType = CHANNEL_UNKNOWN;
        } else {
            mChannelShort = "?";
            mChannelType = CHANNEL_UNKNOWN;
        }
    }

    public String getReadableName() {
        final String[] splitted = mName.split("-");
        if (splitted.length > 2) {
            return splitted[0] + "-" + splitted[1] + "-" + splitted[2];
        }
        return mName;
    }

    public String getChannel() { return mChannel; }

    public String getChannelShort() { return mChannelShort; }

    public int getChannelType() { return mChannelType; }

    public String getName() { return mName; }

    public String getMd5() { return mMd5; }

    public String getUrl() { return mUrl; }

    public String getTimestamp() { return mTimestamp; }

    public boolean isDownloading() { return mIsDownloading; }

    public UpdateInfo setChannel(final String mChannel) {
        this.mChannel = mChannel;
        return this;
    }

    public UpdateInfo setChannelShort(final String mChannelShort) {
        this.mChannelShort = mChannelShort;
        return this;
    }

    public UpdateInfo setChannelType(final int mChannelType) {
        this.mChannelType = mChannelType;
        return this;
    }

    public UpdateInfo setName(final String mName) {
        this.mName = mName;
        return this;
    }

    public UpdateInfo setMd5(final String mMd5) {
        this.mMd5 = mMd5;
        return this;
    }

    public UpdateInfo setUrl(final String mUrl) {
        this.mUrl = mUrl;
        return this;
    }

    public UpdateInfo setTimestamp(final String mTimestamp) {
        this.mTimestamp = mTimestamp;
        return this;
    }

    public UpdateInfo setDownloading(final boolean isDownloading) {
        mIsDownloading = isDownloading;
        return this;
    }

    @Override public String toString() { return "UpdateInfo: " + mName; }

    @Override public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof UpdateInfo)) return false;

        final UpdateInfo ui = (UpdateInfo) o;
        return TextUtils.equals(mChannel, ui.mChannel)
                && TextUtils.equals(mChannelShort, ui.mChannelShort)
                && TextUtils.equals(mName, ui.mName)
                && TextUtils.equals(mMd5, ui.mMd5)
                && TextUtils.equals(mUrl, ui.mUrl)
                && TextUtils.equals(mTimestamp, ui.mTimestamp)
                && mChannelType == ui.mChannelType
                && mIsDownloading == ui.isDownloading();
    }

    public static final Creator<UpdateInfo> CREATOR = new Creator<UpdateInfo>() {
        public UpdateInfo createFromParcel(final Parcel in) { return new UpdateInfo(in); }

        public UpdateInfo[] newArray(final int size) { return new UpdateInfo[size]; }
    };

    @Override public int describeContents() { return 0; }

    @Override public void writeToParcel(final Parcel parcel, final int i) {
        parcel.writeString(mChannel);
        parcel.writeString(mChannelShort);
        parcel.writeString(mName);
        parcel.writeString(mMd5);
        parcel.writeString(mUrl);
        parcel.writeString(mTimestamp);
        parcel.writeString(mIsDownloading ? "1" : "0");
    }

    private void readFromParcel(final Parcel in) {
        mChannel = in.readString();
        mChannelShort = in.readString();
        mName = in.readString();
        mMd5 = in.readString();
        mUrl = in.readString();
        mTimestamp = in.readString();
        mIsDownloading = in.readString().equals("1");
    }
}
