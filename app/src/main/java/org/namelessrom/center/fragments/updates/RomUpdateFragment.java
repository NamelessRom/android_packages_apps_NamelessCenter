package org.namelessrom.center.fragments.updates;


import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.namelessrom.center.Logger;
import org.namelessrom.center.R;
import org.namelessrom.center.cards.RomUpdateCard;
import org.namelessrom.center.cards.SimpleCard;
import org.namelessrom.center.items.UpdateInfo;
import org.namelessrom.center.utils.DebugHelper;

import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;

import static butterknife.ButterKnife.findById;

/**
 * A simple {@link Fragment} subclass.
 */
public class RomUpdateFragment extends Fragment {

    private RomUpdateCardArrayAdapter mCardArrayAdapter;

    public RomUpdateFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_rom_update, container, false);

        final CardListView cardListView = findById(v, R.id.rom_updates_cards_list);
        final List<Card> cards = new ArrayList<Card>();

        Logger.setEnabled(true);
        if (Logger.getEnabled()) { addDummyCards(cards); }

        mCardArrayAdapter = new RomUpdateCardArrayAdapter(getActivity(), cards);
        cardListView.setAdapter(mCardArrayAdapter);

        return v;
    }

    private void addDummyCards(final List<Card> cards) {
        SimpleCard card;
        RomUpdateCard romUpdateCard;
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                card = new SimpleCard(getActivity());
                card.setTitle(String.format("Hey, i am card #%s", i));
                card.setBody(String.format("SystemClock.elapsedRealtime(): %s",
                        SystemClock.elapsedRealtime()));
                cards.add(card);
            } else {
                romUpdateCard = new RomUpdateCard(getActivity(), DebugHelper.getDummyUpdateInfo());

                cards.add(romUpdateCard);
            }
        }
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private class RomUpdateCardArrayAdapter extends CardArrayAdapter {
        public RomUpdateCardArrayAdapter(final Context context, final List<Card> cards) {
            super(context, cards);
        }
    }
}
