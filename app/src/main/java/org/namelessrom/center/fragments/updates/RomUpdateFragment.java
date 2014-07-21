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
import android.animation.AnimatorSet;
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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import org.namelessrom.center.AppInstance;
import org.namelessrom.center.Logger;
import org.namelessrom.center.R;
import org.namelessrom.center.cards.RomUpdateCard;
import org.namelessrom.center.cards.SimpleCard;
import org.namelessrom.center.events.DownloadErrorEvent;
import org.namelessrom.center.events.DownloadProgressEvent;
import org.namelessrom.center.interfaces.OnFragmentLoadedListener;
import org.namelessrom.center.items.UpdateInfo;
import org.namelessrom.center.services.UpdateCheckService;
import org.namelessrom.center.utils.AnimationHelper;
import org.namelessrom.center.utils.BusProvider;
import org.namelessrom.center.utils.DebugHelper;
import org.namelessrom.center.utils.UpdateHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;

import static butterknife.ButterKnife.findById;

/**
 * A fragment showcasing our rom updates
 */
public class RomUpdateFragment extends Fragment implements Card.OnSwipeListener {

    private View                      mProgressView;
    private CardListView              mCardListView;
    private RomUpdateCardArrayAdapter mCardArrayAdapter;

    public RomUpdateFragment() { }

    @Override public void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UpdateCheckService.ACTION_CHECK_FINISHED);
        AppInstance.applicationContext.registerReceiver(updateCheckReceiver, intentFilter);

        // update cards
        refreshUpdates();
    }

    @Override public void onPause() {
        super.onPause();
        BusProvider.getBus().unregister(this);
        try {
            AppInstance.applicationContext.unregisterReceiver(updateCheckReceiver);
        } catch (final Exception ignored) { }
    }

    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_rom_update, container, false);

        mProgressView = findById(v, R.id.romUpdates_progress_view);
        mProgressView.setAlpha(0.0f);
        mCardListView = findById(v, R.id.rom_updates_cards_list);

        mCardArrayAdapter = new RomUpdateCardArrayAdapter(getActivity(), new ArrayList<Card>());
        mCardListView.setAdapter(mCardArrayAdapter);

        return v;
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        final Activity activity = getActivity();
        if (activity != null && activity instanceof OnFragmentLoadedListener) {
            ((OnFragmentLoadedListener) activity).onFragmentLoaded();
        }
    }

    @Override public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.rom_update, menu);
    }

    @Override public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();

        switch (id) {
            case R.id.menu_refresh:
                refreshUpdates();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override public void onSwipe(final Card card) {
        // return if its not our simple card or the listview is null (wtf?)
        if (!(card instanceof SimpleCard) || mCardListView == null) return;
        refreshUpdates();
    }

    private void refreshUpdates() {
        // cross fade the listview and progress view and ...
        final AnimatorSet animatorSet = new AnimatorSet();
        final ObjectAnimator listAnim = AnimationHelper.alpha(mCardListView, 1f, 0f, 300);
        final ObjectAnimator progAnim = AnimationHelper.alpha(mProgressView, 0f, 1f, 300);
        animatorSet.play(listAnim).with(progAnim);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(final Animator animation) {
                mCardListView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                mProgressView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            }

            @Override public void onAnimationEnd(final Animator animation) {
                mCardListView.setLayerType(View.LAYER_TYPE_NONE, null);
                mProgressView.setLayerType(View.LAYER_TYPE_NONE, null);
                final Intent i = new Intent(
                        AppInstance.applicationContext, UpdateCheckService.class);
                i.setAction(UpdateCheckService.ACTION_CHECK_UI);
                AppInstance.applicationContext.startService(i);
            }

            @Override public void onAnimationCancel(final Animator animation) { }

            @Override public void onAnimationRepeat(final Animator animation) { }
        });
        animatorSet.start();
    }

    private void addCards(final ArrayList<Card> result) {
        // remove all previous cards
        mCardArrayAdapter.clear();

        // add our "master card"
        final SimpleCard card = new SimpleCard(getActivity());
        card.setTitle(String.format("Hey, i am card #0"));
        card.setBody(String.format("SystemClock.elapsedRealtime(): %s",
                SystemClock.elapsedRealtime()));
        card.setOnSwipeListener(this);
        mCardArrayAdapter.insert(card, 0);

        // add our updates, if they are not null
        if (result != null && result.size() != 0) mCardArrayAdapter.addAll(result);

        // and update it
        updateCards();
    }

    private void updateCards() {
        Logger.v(this, "updateCards()");
        mCardArrayAdapter.notifyDataSetChanged();
        mCardListView.invalidate();
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
            // cross fade our list view and progress view again
            if (mCardListView != null) {
                final AnimatorSet animatorSet = new AnimatorSet();
                final ObjectAnimator listAnim = AnimationHelper.alpha(mCardListView, 0f, 1f, 300);
                final ObjectAnimator progAnim = AnimationHelper.alpha(mProgressView, 1f, 0f, 300);
                animatorSet.play(listAnim).with(progAnim);
                animatorSet.addListener(new Animator.AnimatorListener() {
                    @Override public void onAnimationStart(final Animator animator) {
                        mCardListView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                        mProgressView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                    }

                    @Override public void onAnimationEnd(final Animator animator) {
                        mCardListView.setLayerType(View.LAYER_TYPE_NONE, null);
                        mProgressView.setLayerType(View.LAYER_TYPE_NONE, null);
                    }

                    @Override public void onAnimationCancel(final Animator animator) { }

                    @Override public void onAnimationRepeat(final Animator animator) { }
                });
                animatorSet.start();
            }
        }
    }

    private final BroadcastReceiver updateCheckReceiver = new BroadcastReceiver() {
        @Override public void onReceive(final Context context, final Intent intent) {
            if (intent == null || TextUtils.isEmpty(intent.getAction())) return;
            final String action = intent.getAction();

            if (UpdateCheckService.ACTION_CHECK_FINISHED.equals(action)) {
                if (!intent.getBooleanExtra("success", false)) return;
                final ArrayList<UpdateInfo> updates = intent.getParcelableArrayListExtra("updates");
                new UpdateCardTask(updates).execute();
            }
        }
    };

    @Subscribe public void onDownloadProgressEvent(final DownloadProgressEvent event) {
        if (event == null) return;

        mCardArrayAdapter.getDownloading().remove(event.getId());
        mCardArrayAdapter.getDownloading().put(event.getId(), event.getPercentage());

        updateCards();
    }

    @Subscribe public void onDownloadErrorEvent(final DownloadErrorEvent event) {
        if (event == null) return;

        UpdateHelper.getDownloadErrorDialog(getActivity(), event.getReason()).show();

        updateCards();
    }

    private class RomUpdateCardArrayAdapter extends CardArrayAdapter {
        private final HashMap<String, Integer> mDownloading = new HashMap<String, Integer>();

        public RomUpdateCardArrayAdapter(final Context context, final ArrayList<Card> cards) {
            super(context, cards);
            // TODO: load cards with already downloaded updates (hint: database + file check)
        }

        @Override public View getView(int position, View convertView, ViewGroup parent) {
            if (mDownloading.size() > 0 && getItem(position) instanceof RomUpdateCard) {
                final RomUpdateCard card = (RomUpdateCard) getItem(position);
                for (final Map.Entry<String, Integer> entry : mDownloading.entrySet()) {
                    if (TextUtils.equals(entry.getKey(), card.getId())) {
                        card.setDownloading(entry.getValue() != 101);
                        if (card.getDownloadProgress() != null) {
                            card.getDownloadProgress().setProgress(entry.getValue());
                        }
                    }
                }
            }

            return super.getView(position, convertView, parent);
        }

        public HashMap<String, Integer> getDownloading() { return mDownloading; }
    }
}
