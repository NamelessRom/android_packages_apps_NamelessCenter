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
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.squareup.otto.Subscribe;

import org.namelessrom.center.AppInstance;
import org.namelessrom.center.Logger;
import org.namelessrom.center.R;
import org.namelessrom.center.cards.RomUpdateCard;
import org.namelessrom.center.cards.SimpleCard;
import org.namelessrom.center.events.ChangelogEvent;
import org.namelessrom.center.events.DownloadErrorEvent;
import org.namelessrom.center.events.DownloadProgressEvent;
import org.namelessrom.center.interfaces.OnBackPressedListener;
import org.namelessrom.center.interfaces.OnFragmentLoadedListener;
import org.namelessrom.center.items.UpdateInfo;
import org.namelessrom.center.services.UpdateCheckService;
import org.namelessrom.center.utils.AnimationHelper;
import org.namelessrom.center.utils.BusProvider;
import org.namelessrom.center.utils.DebugHelper;
import org.namelessrom.center.utils.FileUtils;
import org.namelessrom.center.utils.UpdateHelper;

import java.io.File;
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
public class RomUpdateFragment extends Fragment implements Card.OnSwipeListener,
        OnBackPressedListener {

    private View                      mProgressView;
    private CardListView              mCardListView;
    private RomUpdateCardArrayAdapter mCardArrayAdapter;

    private WebView     mChangelog;
    private FrameLayout mChangelogContainer;
    private boolean isChangelogShowing = false;

    public RomUpdateFragment() { }

    @Override public void onResume() {
        super.onResume();

        // update cards
        mCardArrayAdapter.notifyDataSetChanged();
    }

    @Override public void onStart() {
        super.onStart();
        BusProvider.getBus().register(this);
    }

    @Override public void onStop() {
        super.onStop();
        BusProvider.getBus().unregister(this);
    }

    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_rom_update, container, false);

        mProgressView = findById(v, R.id.rom_updates_progress_view);
        mProgressView.setAlpha(0.0f);
        mCardListView = findById(v, R.id.rom_updates_cards_list);

        mChangelogContainer = findById(v, R.id.rom_updates_changelog_container);
        mChangelog = findById(v, R.id.rom_updates_changelog);
        // enable javascript for our sweet scripts
        mChangelog.getSettings().setJavaScriptEnabled(true);

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

        // move the changelog container out of view
        mChangelogContainer.post(new Runnable() {
            @Override public void run() {
                mChangelogContainer.setY(-mChangelogContainer.getHeight());
            }
        });

        // load updates
        refreshUpdates();
    }

    @Override public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.rom_update, menu);
    }

    @Override public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();

        switch (id) {
            case R.id.menu_refresh:
                if (isChangelogShowing) {
                    dismissChangelog(true);
                } else {
                    refreshUpdates();
                }
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
            }

            @Override public void onAnimationEnd(final Animator animation) {
                mCardListView.setLayerType(View.LAYER_TYPE_NONE, null);
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
        card.setOnSwipeListener(this);
        mCardArrayAdapter.insert(card, 0);

        // add our updates, if they are not null
        if (result != null && result.size() != 0) mCardArrayAdapter.addAll(result);

        // and update it
        mCardArrayAdapter.notifyDataSetChanged();
    }

    private boolean isAnimating = false;

    @Override public boolean onBackPressed() {
        if (isChangelogShowing) {
            dismissChangelog(false);
            return true;
        } else if (isAnimating) {
            return true;
        }
        return false;
    }

    @Subscribe public void onChangelogEvent(final ChangelogEvent event) {
        if (event == null) return;

        // Move in the changelog and progressview
        final DecelerateInterpolator interpolator = new DecelerateInterpolator();
        final ObjectAnimator moveY = ObjectAnimator.ofFloat(mChangelogContainer, "translationY",
                -mChangelogContainer.getHeight(), 0);
        moveY.setDuration(500);
        moveY.setInterpolator(interpolator);

        final ObjectAnimator alpha = ObjectAnimator.ofFloat(mChangelogContainer, "alpha", 0f, 1f);
        alpha.setDuration(500);
        alpha.setInterpolator(interpolator);

        final ObjectAnimator progAnim = AnimationHelper.alpha(mProgressView, 0f, 1f, 500);
        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(alpha).with(moveY).with(progAnim);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(final Animator animator) {
                isAnimating = true;
            }

            @Override public void onAnimationEnd(final Animator animator) {
                isAnimating = false;
                isChangelogShowing = true;
                loadChangelog(event.getUpdateInfo());
            }

            @Override public void onAnimationCancel(final Animator animator) { }

            @Override public void onAnimationRepeat(final Animator animator) { }
        });
        animatorSet.start();
    }

    private void dismissChangelog(final boolean refresh) {
        if (isAnimating) return;
        final AccelerateInterpolator interpolator = new AccelerateInterpolator();
        final ObjectAnimator moveY = ObjectAnimator.ofFloat(mChangelogContainer, "translationY",
                0, -mChangelogContainer.getHeight());
        moveY.setDuration(500);
        moveY.setInterpolator(interpolator);

        final ObjectAnimator alpha =
                ObjectAnimator.ofFloat(mChangelogContainer, "alpha", 1f, 0f);
        alpha.setDuration(500);
        alpha.setInterpolator(interpolator);

        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(alpha).with(moveY);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(final Animator animator) {
                isAnimating = true;
            }

            @Override public void onAnimationEnd(final Animator animator) {
                isChangelogShowing = false;
                isAnimating = false;
                mChangelog.loadUrl("about:blank");
                if (refresh) refreshUpdates();
            }

            @Override public void onAnimationCancel(final Animator animator) { }

            @Override public void onAnimationRepeat(final Animator animator) { }
        });
        animatorSet.start();
    }

    private final class UpdateCardTask extends AsyncTask<Void, Void, ArrayList<Card>> {
        private final ArrayList<UpdateInfo> updates;

        /**
         * Pass in the updates, can also be null if an error occurred or none are existing
         *
         * @param updates
         */
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
                    }

                    @Override public void onAnimationEnd(final Animator animator) {
                        mCardListView.setLayerType(View.LAYER_TYPE_NONE, null);
                    }

                    @Override public void onAnimationCancel(final Animator animator) { }

                    @Override public void onAnimationRepeat(final Animator animator) { }
                });
                animatorSet.start();
            }
        }
    }

    @Subscribe public void onDownloadProgressEvent(final DownloadProgressEvent event) {
        if (event == null) return;

        mCardArrayAdapter.getDownloading().remove(event.getId());
        mCardArrayAdapter.getDownloading().put(event.getId(), event.getPercentage());

        mCardArrayAdapter.notifyDataSetChanged();
    }

    @Subscribe public void onDownloadErrorEvent(final DownloadErrorEvent event) {
        if (event == null) return;

        UpdateHelper.getDownloadErrorDialog(getActivity(), event.getReason()).show();

        mCardArrayAdapter.notifyDataSetChanged();
    }

    @Subscribe public void onUpdateCheckDoneEvent(final ArrayList<UpdateInfo> updates) {
        new UpdateCardTask(updates).execute();
    }

    private class RomUpdateCardArrayAdapter extends CardArrayAdapter {
        private final HashMap<String, Integer> mDownloading = new HashMap<String, Integer>();

        public RomUpdateCardArrayAdapter(final Context context, final ArrayList<Card> cards) {
            super(context, cards);
            // TODO: load cards with already downloaded updates (hint: database + file check)
        }

        @Override public boolean hasStableIds() { return true; }

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

    private void loadChangelog(final UpdateInfo updateInfo) {
        final RomUpdateFragment fragment = this;
        final File changelog = new File(AppInstance.getFilesDirectory() + "/changelogs"
                + '/' + updateInfo.getZipName() + ".changelog");

        // Create a new AsyncTask for loading the changelog
        new AsyncTask<Void, Void, String>() {

            @Override protected String doInBackground(final Void... voids) {
                // If the changelog exists, load it
                if (changelog.exists()) {
                    try {
                        return FileUtils.readFromFile(changelog);
                    } catch (Exception exc) {
                        Logger.e(this, exc.getMessage());
                    }
                }
                return null;
            }

            @Override protected void onPostExecute(final String s) {
                // else load it with ion
                if (s == null || s.isEmpty()) {
                    Ion.with(fragment)
                            .load(updateInfo.getUrl().replace("/download", ".changelog/download"))
                            .asString().setCallback(new FutureCallback<String>() {
                        @Override public void onCompleted(final Exception e, String result) {
                            if (e != null) {
                                loadData(e.getLocalizedMessage());
                                return;
                            }
                            result = result.replace("/css/bootstrap.min.css",
                                    "file:///android_asset/css/bootstrap.min.css")
                                    .replace("/js/jquery.min.js",
                                            "file:///android_asset/js/jquery.min.js")
                                    .replace("/js/main.js", "file:///android_asset/js/main.js");
                            try {
                                FileUtils.writeToFile(changelog, result);
                            } catch (Exception exc) { Logger.e(this, exc.getMessage()); }
                            loadData(result);
                        }
                    });
                } else {
                    loadData(s);
                }
            }
        }.execute();
    }

    private void loadData(final String data) {
        if (mChangelog != null && data != null) {
            // animate the progress out
            AnimationHelper.alpha(mProgressView, 1f, 0f, 500).start();
            // and give the webview something to do
            mChangelog
                    .loadDataWithBaseURL("file:///android_asset/", data, "text/html", "UTF-8", "");
        }
    }
}
