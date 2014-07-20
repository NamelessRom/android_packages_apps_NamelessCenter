package org.namelessrom.center.cards;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.namelessrom.center.R;

import it.gmariotti.cardslib.library.internal.Card;

import static butterknife.ButterKnife.findById;

/**
 * A simple card for displaying informations
 */
public class SimpleCard extends Card {

    private String mBody;

    public SimpleCard(final Context context) {
        this(context, R.layout.card_simple_message_inner_content);
    }

    public SimpleCard(final Context context, final int innerLayout) {
        super(context, innerLayout);
        setSwipeable(true);
    }

    public void setBody(final String body) { mBody = body; }

    public String getBody() { return mBody; }

    @Override
    public void setupInnerViewElements(final ViewGroup parent, final View view) {
        final TextView title = findById(view, R.id.simple_message_card_title);
        final TextView body = findById(view, R.id.simple_message_card_text);

        if (!TextUtils.isEmpty(getTitle())) {
            title.setText(getTitle());
        }
        if (!TextUtils.isEmpty(getBody())) {
            body.setText(getBody());
        }
    }

}
