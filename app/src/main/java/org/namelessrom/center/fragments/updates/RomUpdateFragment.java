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

package org.namelessrom.center.fragments.updates;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.namelessrom.center.AppInstance;
import org.namelessrom.center.R;
import org.namelessrom.center.cards.RomUpdateCard;
import org.namelessrom.center.cards.SimpleCard;
import org.namelessrom.center.interfaces.OnFragmentLoadedListener;
import org.namelessrom.center.items.UpdateInfo;
import org.namelessrom.center.services.UpdateCheckService;
import org.namelessrom.center.utils.AnimationHelper;
import org.namelessrom.center.utils.DebugHelper;

import java.util.ArrayList;

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

    @Override public void onResume() {
        super.onResume();
        AppInstance.applicationContext.registerReceiver(updateCheckReceiver,
                new IntentFilter(UpdateCheckService.ACTION_CHECK_FINISHED));
    }

    @Override public void onPause() {
        super.onPause();
        try {
            AppInstance.applicationContext.unregisterReceiver(updateCheckReceiver);
        } catch (final Exception ignored) { }
    }

    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_rom_update, container, false);

        mCardListView = findById(v, R.id.rom_updates_cards_list);

        mCardArrayAdapter = new RomUpdateCardArrayAdapter(getActivity(), new ArrayList<Card>());
        mCardListView.setAdapter(mCardArrayAdapter);

        return v;
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Activity activity = getActivity();
        if (activity != null && activity instanceof OnFragmentLoadedListener) {
            ((OnFragmentLoadedListener) activity).onFragmentLoaded();
        }

        // add our refresh card
        new UpdateCardTask(null).execute();
    }

    @Override public void onSwipe(final Card card) {
        // return if its not our simple card or the listview is null (wtf?)
        if (!(card instanceof SimpleCard) || mCardListView == null) return;

        // fade out the listview and ...
        final ObjectAnimator animator = AnimationHelper.alpha(mCardListView, 1f, 0f);
        animator.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(final Animator animation) { }

            @Override public void onAnimationEnd(final Animator animation) {
                final Intent i = new Intent(
                        AppInstance.applicationContext, UpdateCheckService.class);
                i.setAction(UpdateCheckService.ACTION_CHECK_UI);
                AppInstance.applicationContext.startService(i);
            }

            @Override public void onAnimationCancel(final Animator animation) { }

            @Override public void onAnimationRepeat(final Animator animation) { }
        });
        animator.start();
    }

    private void addCards(final ArrayList<Card> result) {
        // add our "master card"
        final SimpleCard card = new SimpleCard(getActivity());
        card.setTitle(String.format("Hey, i am card #0"));
        card.setBody(String.format("SystemClock.elapsedRealtime(): %s",
                SystemClock.elapsedRealtime()));
        card.setOnSwipeListener(this);
        mCardArrayAdapter.insert(card, 0);

        // add our updates, if they are not null
        if (result != null && result.size() != 0) mCardArrayAdapter.addAll(result);
    }

    private final class UpdateCardTask extends AsyncTask<Void, Void, ArrayList<Card>> {
        private final ArrayList<UpdateInfo> updates;

        public UpdateCardTask(final ArrayList<UpdateInfo> updates) { this.updates = updates; }

        @Override protected ArrayList<Card> doInBackground(final Void... params) {
            // we do not have updates, get out of here!
            if (updates == null || updates.size() <= 0 || getActivity() == null) return null;

            final Activity activity = getActivity();

            final ArrayList<Card> cards = new ArrayList<Card>(updates.size());
            for (final UpdateInfo info : updates) {
                cards.add(new RomUpdateCard(activity, info));
            }

            if (DebugHelper.getEnabled()) {
                for (int i = 0; i < 10; i++) {
                    cards.add(new RomUpdateCard(activity, DebugHelper.getDummyUpdateInfo()));
                }
            }

            return cards;
        }

        @Override protected void onPostExecute(final ArrayList<Card> result) {
            addCards(result);
            // fade in our list view
            if (mCardListView != null) AnimationHelper.alpha(mCardListView, 0f, 1f).start();
        }
    }

    private final BroadcastReceiver updateCheckReceiver = new BroadcastReceiver() {
        @Override public void onReceive(final Context context, final Intent intent) {
            if (intent == null || !intent.getBooleanExtra("success", false)) return;
            final ArrayList<UpdateInfo> updates = intent.getParcelableArrayListExtra("updates");
            new UpdateCardTask(updates).execute();
        }
    };

    private class RomUpdateCardArrayAdapter extends CardArrayAdapter {
        public RomUpdateCardArrayAdapter(final Context context, final ArrayList<Card> cards) {
            super(context, cards);
            // TODO: load cards with already downloaded updates (hint: database + file check)
        }
    }
}
