package org.namelessrom.center.cards;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.namelessrom.center.AppInstance;
import org.namelessrom.center.R;
import org.namelessrom.center.utils.DrawableHelper;

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

    public RomUpdateCard(final Context context) {
        this(context, R.layout.card_rom_update_inner_content);
    }

    public RomUpdateCard(final Context context, final int innerLayout) {
        super(context, innerLayout);
        init();
    }

    private void init() {
        //Add Header
        final CardHeader header = new CardHeader(getContext());
        header.setTitle(/* TODO: get from update info */ "Rom Update Card");
        addCardHeader(header);

        addCardIcon();

        RomUpdateCardExpand cardExpand = new RomUpdateCardExpand(getContext());
        // TODO: setup stuffs
        addCardExpand(cardExpand);

        setSwipeable(false);

        setId(/* TODO: set an id */ "20140101");
    }

    private void addCardIcon() {
        if (getCardThumbnail() == null) {
            final CardThumbnail thumbnail = new RomUpdateThumbnail(mContext);
            thumbnail.setCustomSource(new RomUpdateThumbnailCustomSource());
            addCardThumbnail(thumbnail);
        }
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        setupInnerView(view);
    }

    public void setupInnerView(View view) {
        TextView titleTextView = (TextView) view.findViewById(R.id.dashclock_card_inner_title_text);
        TextView statusTextView =
                (TextView) view.findViewById(R.id.dashclock_card_inner_status_text);
        TextView bodyTextView = (TextView) view.findViewById(R.id.dashclock_card_inner_body_text);

        String title = "I am a rom update title";
        String status = "And i am the status";
        String body = "Haha, you are worthless as I AM THE BODY";

        if (TextUtils.isEmpty(title) && !TextUtils.isEmpty(status)) {
            titleTextView.setText(status);
            statusTextView.setVisibility(View.GONE);
        } else {
            titleTextView.setText(title);
            statusTextView.setText(status);
            statusTextView.setVisibility(View.VISIBLE);
        }

        bodyTextView.setText(body);

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
        public void setupInnerViewElements(ViewGroup parent, View viewImage) {
            ImageView image = (ImageView) viewImage;

            // Pick the next background color for the icon.
            // Choose the color in the order they appear in ICON_BACKGROUND_COLORS.
            int color = sIconBackgroundColors[mIconColorIndex];
            image.setBackgroundColor(color);
        }
    }

    private static class RomUpdateThumbnailCustomSource implements CardThumbnail.CustomSource {
        @Override public Bitmap getBitmap() {
            return DrawableHelper.drawableToBitmap(R.drawable.ic_launcher);
        }

        @Override public String getTag() {
            return "";
        }
    }

    private class RomUpdateCardExpand extends CardExpand {
        //TODO: setup actions based on what we can do :derp:
        Context mContext;

        public RomUpdateCardExpand(Context context) {
            super(context, R.layout.card_rom_update_expand_inner_content);
            mContext = context;
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {
            if (view == null) return;

            Button install = findById(view, R.id.rom_update_expand_button_install);
            install.setText("Install");

            Button delete = findById(view, R.id.rom_update_expand_button_delete);
            delete.setText("Delete");
        }

    }
}
