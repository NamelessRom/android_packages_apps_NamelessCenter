package org.namelessrom.center.fragments.updates;


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.namelessrom.center.R;
import org.namelessrom.center.cards.SimpleCard;
import org.namelessrom.center.utils.AnimationHelper;
import org.namelessrom.center.utils.DebugHelper;

import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;

import static butterknife.ButterKnife.findById;

/**
 * A fragment showcasing our rom updates
 */
public class RomUpdateFragment extends Fragment implements Card.OnSwipeListener {

    private CardListView              mCardListView;
    private RomUpdateCardArrayAdapter mCardArrayAdapter;

    public RomUpdateFragment() { }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_rom_update, container, false);

        mCardListView = findById(v, R.id.rom_updates_cards_list);

        mCardArrayAdapter = new RomUpdateCardArrayAdapter(getActivity(), new ArrayList<Card>());
        mCardListView.setAdapter(mCardArrayAdapter);

        return v;
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (DebugHelper.getEnabled()) { new UpdateCardTask().execute(); }
    }

    @Override public void onSwipe(final Card card) {
        // return if its not our simple card or the listview is null (wtf?)
        if (!(card instanceof SimpleCard) || mCardListView == null) return;

        // fade out the listview and ...
        final ObjectAnimator animator = AnimationHelper.alpha(mCardListView, 1f, 0f);
        animator.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(final Animator animation) { }

            @Override public void onAnimationEnd(final Animator animation) {
                // ... once ended update the list
                new UpdateCardTask().execute();
            }

            @Override public void onAnimationCancel(final Animator animation) { }

            @Override public void onAnimationRepeat(final Animator animation) { }
        });
        animator.start();
    }

    private final class UpdateCardTask extends AsyncTask<Void, Void, Void> {
        @Override protected void onPreExecute() { }

        @Override protected Void doInBackground(final Void... params) {
            // TODO: dummy, remove!
            getActivity().runOnUiThread(new Runnable() {
                @Override public void run() {
                    DebugHelper.addDummyCards(getActivity(), mCardArrayAdapter,
                            RomUpdateFragment.this);
                }
            });
            return null;
        }

        @Override protected void onPostExecute(final Void result) {
            // fade in our list view
            if (mCardListView != null) AnimationHelper.alpha(mCardListView, 0f, 1f).start();
        }
    }

    private class RomUpdateCardArrayAdapter extends CardArrayAdapter {
        public RomUpdateCardArrayAdapter(final Context context, final List<Card> cards) {
            super(context, cards);
            // TODO: load cards with already downloaded updates (hint: database + file check)
        }
    }
}
