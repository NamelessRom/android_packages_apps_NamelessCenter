package org.namelessrom.center.utils;

import android.content.Context;
import android.os.SystemClock;

import org.namelessrom.center.cards.RomUpdateCard;
import org.namelessrom.center.cards.SimpleCard;
import org.namelessrom.center.items.UpdateInfo;

import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;

/**
 * Helper for debugging
 */
public class DebugHelper {

    private static boolean ENABLED = false;

    public static synchronized boolean getEnabled() { return ENABLED; }

    public static synchronized void setEnabled(final boolean enabled) { ENABLED = enabled; }

    public static UpdateInfo getDummyUpdateInfo() {
        return new UpdateInfo("NIGHTLY", "nameless-4.4.4-20140101-p970-NIGHTLY",
                "00cb8a1cb8e90df20945250a0d749233", /* no URL */ "", "20140101");
    }

    public static void addDummyCards(final Context context, final CardArrayAdapter adapter,
            final Card.OnSwipeListener swipeListener) {
        final ArrayList<Card> cards = new ArrayList<Card>();
        SimpleCard card;
        RomUpdateCard romUpdateCard;
        for (int i = 0; i < 10; i++) {
            if (i == 0) {
                card = new SimpleCard(context);
                card.setTitle(String.format("Hey, i am card #%s", i));
                card.setBody(String.format("SystemClock.elapsedRealtime(): %s",
                        SystemClock.elapsedRealtime()));
                card.setOnSwipeListener(swipeListener);
                adapter.insert(card, 0);
            } else {
                romUpdateCard = new RomUpdateCard(context, DebugHelper.getDummyUpdateInfo());
                cards.add(romUpdateCard);
            }
        }
        adapter.addAll(cards);
    }

}
