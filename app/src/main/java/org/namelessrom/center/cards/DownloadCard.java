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

package org.namelessrom.center.cards;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.namelessrom.center.MainActivity;
import org.namelessrom.center.R;
import org.namelessrom.center.Version;
import org.namelessrom.center.helpers.DownloadHelper;
import org.namelessrom.center.widget.Card;
import org.namelessrom.center.widget.Item;
import org.namelessrom.center.widget.Item.OnItemClickListener;

public class DownloadCard extends Card implements DownloadHelper.DownloadCallback {

    private static final String DOWNLOADING       = "DOWNLOADING";
    private static final String DOWNLOAD_PROGRESS = "DOWNLOAD_PROGRESS";

    private MainActivity mActivity;
    private ProgressBar  mWaitProgressBar;
    private ProgressBar  mProgressBar;
    private TextView     mProgress;
    private Item         mCancel;
    private Version[]    mDownloading;
    private int mDownloadProgress = -1;

    public DownloadCard(Context context, AttributeSet attrs, Version[] infos,
            Bundle savedInstanceState) {
        super(context, attrs, savedInstanceState);

        mActivity = (MainActivity) context;
        mActivity.setDownloadCallback(this);

        setTitle(R.string.download_card_title);
        setLayoutId(R.layout.card_download);

        mWaitProgressBar = (ProgressBar) findLayoutViewById(R.id.wait_progressbar);
        mProgressBar = (ProgressBar) findLayoutViewById(R.id.progressbar);
        mProgress = (TextView) findLayoutViewById(R.id.progress);
        mCancel = (Item) findLayoutViewById(R.id.cancel);

        mWaitProgressBar.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mProgress.setVisibility(View.GONE);

        mCancel.setEnabled(false);
        mCancel.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onClick(int id) {
                DownloadHelper.clearDownloads();
            }

        });

        setInitialInfos(infos);

        if (infos == null && savedInstanceState != null) {
            infos = (Version[]) savedInstanceState.getSerializable(DOWNLOADING);
            mDownloadProgress = savedInstanceState.getInt(DOWNLOAD_PROGRESS);
            setInfos(infos, mDownloadProgress);
        } else {
            mDownloading = infos;
            if (infos == null) {
                mDownloadProgress = -1;
                setInfos(infos, mDownloadProgress);
                mCancel.setEnabled(true);
            }
        }
    }

    public void setInitialInfos(Version[] infos) {
        Context context = getContext();

        String names = "";
        for (int i = 0; infos != null && i < infos.length; i++) {
            names += infos[i].getReadableName() + "\n";
            if (DownloadHelper.isDownloading()) {
                int resId = R.string.already_downloading_rom;
                Toast.makeText(context, resId, Toast.LENGTH_LONG).show();
                ((MainActivity) context).setState(MainActivity.STATE_UPDATES,
                        true, null, null, null, false);
                return;
            }
        }

        TextView infoView = (TextView) findViewById(R.id.info);
        infoView.setText(context.getResources().getString(R.string.downloading_info, names));

        for (int i = 0; infos != null && i < infos.length; i++) {
            DownloadHelper.registerCallback(mActivity);
            DownloadHelper.downloadFile(infos[i].url, infos[i].name, infos[i].md5);
        }
    }

    @Override
    public void saveState(Bundle outState) {
        super.saveState(outState);
        outState.putSerializable(DOWNLOADING, mDownloading);
        outState.putInt(DOWNLOAD_PROGRESS, mDownloadProgress);
    }

    private void setInfos(Version[] infos, int progress) {
        Context context = getContext();
        String names = "";
        for (int i = 0; infos != null && i < infos.length; i++) {
            names += infos[i].getReadableName() + "\n";
        }

        TextView infoView = (TextView) findViewById(R.id.info);
        infoView.setText(context.getResources().getString(R.string.downloading_info, names));

        DownloadHelper.registerCallback(mActivity);

        onDownloadProgress(progress);
    }

    @Override
    protected boolean canExpand() {
        return false;
    }

    @Override
    public void onDownloadStarted() {
        onDownloadProgress(-1);
    }

    @Override
    public void onDownloadError(String reason) {
        mCancel.setEnabled(false);
        mDownloadProgress = -1;
        mWaitProgressBar.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mProgress.setVisibility(View.GONE);
    }

    @Override
    public void onDownloadProgress(int progress) {
        mCancel.setEnabled(progress >= 0);
        mDownloadProgress = progress;
        if (progress < 0) {
            mWaitProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
            mProgress.setVisibility(View.GONE);
        } else if (progress > 100) {
            mWaitProgressBar.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.GONE);
            mProgress.setVisibility(View.GONE);
        } else {
            mWaitProgressBar.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.VISIBLE);
            mProgressBar.setProgress(progress);
            mProgress.setText(progress + "%");
        }
    }

    @Override
    public void onDownloadFinished(Uri uri, final String md5) {
        mCancel.setEnabled(false);
        mDownloadProgress = -1;
        mWaitProgressBar.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
    }

}