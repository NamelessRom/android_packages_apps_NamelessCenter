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

package org.namelessrom.center.cards;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.namelessrom.center.AppInstance;
import org.namelessrom.center.Logger;
import org.namelessrom.center.R;
import org.namelessrom.center.events.ChangelogEvent;
import org.namelessrom.center.items.UpdateInfo;
import org.namelessrom.center.receivers.UpdateCheckReceiver;
import org.namelessrom.center.utils.BusProvider;
import org.namelessrom.center.utils.DebugHelper;
import org.namelessrom.center.utils.DrawableHelper;
import org.namelessrom.center.utils.Helper;
import org.namelessrom.center.utils.MD5;
import org.namelessrom.center.utils.UpdateHelper;

import java.security.SecureRandom;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardExpand;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.CardThumbnail;
import it.gmariotti.cardslib.library.internal.ViewToClickToExpand;

import static butterknife.ButterKnife.findById;

/**
 * This class provides a card that will represent a Rom Update
 */
public class RomUpdateCard extends Card {

    public UpdateInfo updateInfo;

    private ProgressBar mDownloadProgress;
    private TextView    mStateTextView;

    public RomUpdateCard(final Context context, final UpdateInfo info) {
        this(context, R.layout.card_rom_update_inner_content, info);
    }

    public RomUpdateCard(final Context context, final int innerLayout, final UpdateInfo info) {
        super(context, innerLayout);
        updateInfo = info;
        init();
    }

    private void init() {
        // Set the channel as header
        final CardHeader header = new CardHeader(getContext());
        header.setTitle(updateInfo.getChannel());
        addCardHeader(header);

        // Add something eyecandy
        addCardIcon();

        // Setup actions which are available when expanding (pressing on) the card
        final RomUpdateCardExpand cardExpand = new RomUpdateCardExpand(getContext());
        addCardExpand(cardExpand);

        // Do not allow to swipe it away... yet
        setSwipeable(false);

        // Set the timestamp, which is unique, as ID
        setId(updateInfo.getTimestamp());
    }

    private void addCardIcon() {
        // If we do not have a thumbnail, add one
        if (getCardThumbnail() == null) {
            final CardThumbnail thumbnail = new RomUpdateThumbnail(mContext);
            thumbnail.setCustomSource(new RomUpdateThumbnailCustomSource());
            addCardThumbnail(thumbnail);
        }
    }

    @Override
    public void setupInnerViewElements(final ViewGroup parent, final View view) {
        setupInnerView(view);
    }

    public void setupInnerView(final View view) {
        final TextView titleTextView = findById(view, R.id.rom_update_inner_title);
        final TextView statusTextView = findById(view, R.id.rom_update_inner_status);

        mStateTextView = findById(view, R.id.rom_update_inner_state);
        mDownloadProgress = findById(view, R.id.rom_update_inner_progress);

        if (DebugHelper.getEnabled()) {
            final SecureRandom secureRandom = new SecureRandom();
            mDownloadProgress.setProgress(secureRandom.nextInt(100));
        } else if (updateInfo.isDownloading()) {
            mDownloadProgress.setVisibility(View.VISIBLE);
        } else {
            mDownloadProgress.setVisibility(View.INVISIBLE);
        }

        final String title = updateInfo.getReadableName();
        final String status = updateInfo.getMd5();

        if (TextUtils.isEmpty(title) && !TextUtils.isEmpty(status)) {
            titleTextView.setText(status);
            statusTextView.setVisibility(View.GONE);
        } else {
            titleTextView.setText(title);
            statusTextView.setText(status);
            statusTextView.setVisibility(View.VISIBLE);
        }

        final ViewToClickToExpand viewToClickToExpand =
                ViewToClickToExpand.builder().setupView(getCardView());
        setViewToClickToExpand(viewToClickToExpand);
    }

    public void setState(final String state) {
        if (mStateTextView != null) mStateTextView.setText(state);
    }

    public ProgressBar getDownloadProgress() { return mDownloadProgress; }

    public void setDownloading(final boolean downloading) {
        this.updateInfo = updateInfo.setDownloading(downloading);
    }

    private static class RomUpdateThumbnail extends CardThumbnail {
        private static int[] sIconBackgroundColors = null;

        private static int sCurrentIconColorIndex = 0;
        private        int mIconColorIndex        = -1;

        public RomUpdateThumbnail(final Context context) {
            super(context);

            if (sIconBackgroundColors == null) {
                sIconBackgroundColors = context.getResources()
                        .getIntArray(R.array.icon_background_colors);
            }

            // Assign this card a color, incrementing the static ongoing color index
            if (mIconColorIndex == -1) {
                mIconColorIndex = sCurrentIconColorIndex++ % sIconBackgroundColors.length;
            }
        }

        @Override
        public void setupInnerViewElements(final ViewGroup parent, final View viewImage) {
            // Pick the next background color for the icon.
            // Choose the color in the order they appear in ICON_BACKGROUND_COLORS.
            viewImage.setBackgroundColor(sIconBackgroundColors[mIconColorIndex]);
        }
    }

    private class RomUpdateThumbnailCustomSource implements CardThumbnail.CustomSource {

        public RomUpdateThumbnailCustomSource() { }

        @Override public Bitmap getBitmap() {
            return DrawableHelper.drawableToBitmap(R.drawable.ic_launcher);
        }

        @Override public String getTag() { return updateInfo.getTimestamp(); }
    }

    private class RomUpdateCardExpand extends CardExpand {

        public RomUpdateCardExpand(final Context context) {
            super(context, R.layout.card_rom_update_expand_inner_content);
        }

        @Override
        public void setupInnerViewElements(final ViewGroup parent, final View view) {
            if (view == null) return;

            final LinearLayout topContainer = findById(view, R.id.rom_updates_expand_top);
            // XXX: top_left is unused currently
            final Button top_left = findById(view, R.id.rom_update_expand_button_top_left);
            final Button top_right = findById(view, R.id.rom_update_expand_button_top_right);

            final Button left = findById(view, R.id.rom_update_expand_button_left);
            final Button right = findById(view, R.id.rom_update_expand_button_right);

            if (updateInfo.isDownloading()) {
                topContainer.setVisibility(View.GONE);

                left.setVisibility(View.VISIBLE);
                left.setText(R.string.changelog);
                right.setVisibility(View.VISIBLE);
                right.setText(android.R.string.cancel);

                if (getDownloadProgress() != null) {
                    getDownloadProgress().setVisibility(View.VISIBLE);
                }
                setState(AppInstance.getStr(R.string.downloading));
            } else if (updateInfo.isDownloaded()) {
                top_left.setVisibility(View.INVISIBLE);
                top_right.setVisibility(View.VISIBLE);
                top_right.setText(R.string.changelog);
                topContainer.setVisibility(View.VISIBLE);

                left.setVisibility(View.VISIBLE);
                left.setText(R.string.delete_update);
                right.setVisibility(View.VISIBLE);
                right.setText(R.string.install);

                if (getDownloadProgress() != null) {
                    getDownloadProgress().setVisibility(View.INVISIBLE);
                }
                if (Helper.parseDate(updateInfo.getTimestamp()) == Helper.getBuildDate()) {
                    setState(AppInstance.getStr(R.string.installed));
                } else {
                    setState(AppInstance.getStr(R.string.downloaded));
                }
            } else {
                topContainer.setVisibility(View.GONE);

                left.setVisibility(View.VISIBLE);
                left.setText(R.string.changelog);
                right.setVisibility(View.VISIBLE);
                right.setText(R.string.download);

                if (getDownloadProgress() != null) {
                    getDownloadProgress().setVisibility(View.INVISIBLE);
                }
                if (Helper.parseDate(updateInfo.getTimestamp()) == Helper.getBuildDate()) {
                    setState(AppInstance.getStr(R.string.installed));
                } else {
                    setState("");
                }
            }

            top_right.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    BusProvider.getBus().post(new ChangelogEvent(updateInfo));
                }
            });

            left.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (!updateInfo.isDownloading() && updateInfo.isDownloaded()) {
                        UpdateHelper.getDeleteDialog(getContext(), updateInfo).show();
                    } else {
                        BusProvider.getBus().post(new ChangelogEvent(updateInfo));
                    }
                }
            });

            right.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (updateInfo.isDownloading()) {
                        UpdateHelper.cancelDownload(mContext, updateInfo);
                    } else if (updateInfo.isDownloaded()) {
                        new InstallTask(getContext(), updateInfo).execute();
                    } else {
                        updateInfo = UpdateHelper.downloadUpdate(mContext, updateInfo);
                    }
                }
            });
        }

    }

    private static class InstallTask extends AsyncTask<Void, Void, Boolean> {
        private final Context        context;
        private final UpdateInfo     updateInfo;
        private final ProgressDialog progressDialog;

        public InstallTask(final Context context, final UpdateInfo updateInfo) {
            this.context = context;
            this.updateInfo = updateInfo;
            this.progressDialog = new ProgressDialog(this.context);
        }

        @Override protected void onPreExecute() {
            progressDialog.setTitle(R.string.checking_md5);
            progressDialog.setMessage(AppInstance.getStr(R.string.please_wait));
            progressDialog.setIndeterminate(true);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override protected Boolean doInBackground(Void... params) {
            return MD5.checkMD5(updateInfo.getMd5(), updateInfo.getPath());
        }

        @Override protected void onPostExecute(Boolean result) {
            progressDialog.dismiss();
            final int title;
            final String message;

            // setup title and message depending on the md5sum check result
            if (result) {
                Logger.v(this, "md5 sum does match");
                title = R.string.reboot_and_install;
                message = AppInstance.getStr(
                        R.string.download_install_notice, updateInfo.getReadableName());
            } else {
                Logger.e(this, "md5 sum does not match!");
                title = R.string.warning;
                message = AppInstance.getStr(
                        R.string.md5sum_warning, updateInfo.getReadableName());
            }
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title);
            builder.setMessage(message);
            builder.setNegativeButton(
                    android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }
            );

            // only show "reboot and install" if the md5sum matches
            if (result) {
                builder.setPositiveButton(R.string.reboot_and_install,
                        new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialogInterface, int i) {
                                final Intent installIntent = new Intent(
                                        AppInstance.get(), UpdateCheckReceiver.class);
                                installIntent.setAction(UpdateCheckReceiver.ACTION_INSTALL_UPDATE);
                                installIntent.putExtra(UpdateCheckReceiver.EXTRA_FILE,
                                        updateInfo.getZipName());
                                AppInstance.get().sendBroadcast(installIntent);
                                dialogInterface.dismiss();
                            }
                        }
                );
            }

            builder.show();
        }
    }
}
