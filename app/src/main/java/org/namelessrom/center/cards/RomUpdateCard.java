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

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.namelessrom.center.R;
import org.namelessrom.center.items.UpdateInfo;
import org.namelessrom.center.utils.DebugHelper;
import org.namelessrom.center.utils.DrawableHelper;

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

    private final UpdateInfo mUpdateInfo;

    private ProgressBar mDownloadProgress;
    private TextView    mStateTextView;

    public RomUpdateCard(final Context context, final UpdateInfo info) {
        this(context, R.layout.card_rom_update_inner_content, info);
    }

    public RomUpdateCard(final Context context, final int innerLayout, final UpdateInfo info) {
        super(context, innerLayout);
        mUpdateInfo = info;
        init();
    }

    private void init() {
        // Set the channel as header
        final CardHeader header = new CardHeader(getContext());
        header.setTitle(mUpdateInfo.getChannel());
        addCardHeader(header);

        // Add something eyecandy
        addCardIcon();

        // Setup actions which are available when expanding (pressing on) the card
        final RomUpdateCardExpand cardExpand = new RomUpdateCardExpand(getContext());
        // TODO: setup stuffs
        addCardExpand(cardExpand);

        // Do not allow to swipe it away... yet
        setSwipeable(false);

        // Set the timestamp, which is unique, as ID
        setId(mUpdateInfo.getTimestamp());
    }

    private void addCardIcon() {
        // If we do not have a thumbnail, add one
        if (getCardThumbnail() == null) {
            final CardThumbnail thumbnail = new RomUpdateThumbnail(mContext);
            thumbnail.setCustomSource(new RomUpdateThumbnailCustomSource(mUpdateInfo));
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

        //TODO: setup progress, state etc
        if (DebugHelper.getEnabled()) {
            final SecureRandom secureRandom = new SecureRandom();
            mDownloadProgress.setProgress(secureRandom.nextInt(100));
        } else {
            mDownloadProgress.setVisibility(View.INVISIBLE);
        }

        final String title = mUpdateInfo.getReadableName();
        final String status = mUpdateInfo.getMd5();

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

    private static class RomUpdateThumbnail extends CardThumbnail {
        private static int[] sIconBackgroundColors;

        private static int sCurrentIconColorIndex = 0;
        private        int mIconColorIndex        = -1;

        public RomUpdateThumbnail(final Context context) {
            super(context);
            sIconBackgroundColors = context.getResources()
                    .getIntArray(R.array.icon_background_colors);

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

    private static class RomUpdateThumbnailCustomSource implements CardThumbnail.CustomSource {
        private final UpdateInfo updateInfo;

        public RomUpdateThumbnailCustomSource(final UpdateInfo updateInfo) {
            this.updateInfo = updateInfo;
        }

        @Override public Bitmap getBitmap() {
            return DrawableHelper.drawableToBitmap(R.drawable.ic_launcher);
        }

        @Override public String getTag() { return updateInfo.getTimestamp(); }
    }

    //TODO: setup actions based on what we can do :derp:
    private static class RomUpdateCardExpand extends CardExpand {

        public RomUpdateCardExpand(final Context context) {
            super(context, R.layout.card_rom_update_expand_inner_content);
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {
            if (view == null) return;

            final Button install = findById(view, R.id.rom_update_expand_button_install);
            install.setText("Install");

            final Button delete = findById(view, R.id.rom_update_expand_button_delete);
            delete.setText("Delete");
        }

    }
}
