package org.namelessrom.center.cards;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.namelessrom.center.AppInstance;
import org.namelessrom.center.Constants;
import org.namelessrom.center.R;
import org.namelessrom.center.items.UpdateInfo;
import org.namelessrom.center.utils.Helper;

import it.gmariotti.cardslib.library.internal.Card;

import static butterknife.ButterKnife.findById;

/**
 * A simple card for displaying informations
 */
public class SimpleCard extends Card {

    private String mTitle;
    private String mBody;

    public SimpleCard(final Context context) {
        this(context, R.layout.card_simple_message_inner_content);
    }

    public SimpleCard(final Context context, final int innerLayout) {
        super(context, innerLayout);
        mTitle = String.format("%s: %s", AppInstance.getStr(R.string.installed_version),
                UpdateInfo
                        .getReadableName("nameless-" + Helper.readBuildProp("ro.nameless.version"))
        );
        setSwipeable(true);
    }

    public void setBody(final String body) { mBody = body; }

    public String getBody() { return mBody; }

    @Override
    public void setupInnerViewElements(final ViewGroup parent, final View view) {
        final TextView title = findById(view, R.id.simple_message_card_title);
        final TextView body = findById(view, R.id.simple_message_card_text);

        title.setText(mTitle);
        body.setText(String.format("%s\n%s: %s", AppInstance.getStr(R.string.swipe_refresh),
                AppInstance.getStr(R.string.size_used),
                Helper.humanReadableByteCount(Helper.dirSize(Constants.UPDATE_FOLDER_FULL))));
    }

}
