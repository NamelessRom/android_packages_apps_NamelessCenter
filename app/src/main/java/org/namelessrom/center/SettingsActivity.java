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

package org.namelessrom.center;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import org.namelessrom.center.fragments.preferences.RomUpdatePreferenceFragment;

import java.util.List;

import static butterknife.ButterKnife.findById;
import static org.namelessrom.center.utils.AppHelper.actionExists;

public class SettingsActivity extends PreferenceActivity {

    private static final String ACTION_NAMELESS_PROVIDER =
            "org.namelessrom.providers.activities.Preferences";

    private static final String[] ENTRY_FRAGMENTS = new String[]{
            RomUpdatePreferenceFragment.class.getName()
    };

    private List<Header> mHeaders;

    @Override public boolean isValidFragment(final String fragmentName) {
        for (final String ENTRY_FRAGMENT : ENTRY_FRAGMENTS) {
            if (ENTRY_FRAGMENT.equals(fragmentName)) return true;
        }
        return false;
    }

    @Override protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            finish();
            overridePendingTransition(R.anim.exit_right, R.anim.exit_left);
        }
    }

    @Override public void onBuildHeaders(final List<Header> headers) {
        loadHeadersFromResource(R.xml.settings, headers);
        mHeaders = updateHeaderList(headers);
    }

    private List<Header> updateHeaderList(final List<Header> headers) {
        int i = 0;
        while (i < headers.size()) {
            final Header header = headers.get(i);
            final int id = ((int) header.id);

            if (id == R.id.nameless_provider) {
                if (actionExists(ACTION_NAMELESS_PROVIDER)) {
                    final Intent intent = new Intent();
                    intent.setAction(ACTION_NAMELESS_PROVIDER);
                    header.intent = intent;
                } else {
                    headers.remove(i);
                }
            } else if (id == R.id.build_info) {
                header.summary = getString(R.string.version, AppInstance.getVersionName());
            }

            i++;
        }

        return headers;
    }

    @Override public void setListAdapter(final ListAdapter adapter) {
        if (adapter == null) {
            super.setListAdapter(null);
        } else {
            super.setListAdapter(new HeaderAdapter(this, mHeaders));
        }
    }

    private static class HeaderAdapter extends ArrayAdapter<Header> {
        private static final int HEADER_TYPE_CATEGORY = 0;
        private static final int HEADER_TYPE_INFORMAL = 1;
        private static final int HEADER_TYPE_NORMAL   = 2;
        private static final int HEADER_TYPE_SWITCH   = 3;
        private static final int HEADER_TYPE_COUNT    = HEADER_TYPE_SWITCH + 1;

        private final LayoutInflater mInflater;

        private static class HeaderViewHolder {
            ImageView icon;
            TextView  title;
            TextView  summary;
            //Switch    switch_;
        }

        private static int getHeaderType(final Header header) {
            if (header.fragment == null && header.intent == null && header.summary == null) {
                return HEADER_TYPE_CATEGORY;
            } else if (header.fragmentArguments != null
                    && header.fragmentArguments.getBoolean("informal", false)) {
                return HEADER_TYPE_INFORMAL;
            /*} else if (header.id == a_cool_id) {
                return HEADER_TYPE_SWITCH;*/
            } else {
                return HEADER_TYPE_NORMAL;
            }
        }

        @Override public boolean hasStableIds() { return true; }

        @Override public int getViewTypeCount() { return HEADER_TYPE_COUNT; }

        @Override
        public int getItemViewType(final int position) { return getHeaderType(getItem(position)); }

        @Override public boolean areAllItemsEnabled() { return false; }

        @Override public boolean isEnabled(final int position) {
            final int type = getItemViewType(position);
            return (type != HEADER_TYPE_CATEGORY && type != HEADER_TYPE_INFORMAL);
        }

        public HeaderAdapter(final Context context, final List<Header> objects) {
            super(context, 0, objects);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override public View getView(final int position, final View convertView,
                final ViewGroup parent) {
            HeaderViewHolder holder;
            Header header = getItem(position);
            View view = null;
            final int headerType = getHeaderType(header);

            if (convertView == null || headerType == HEADER_TYPE_SWITCH) {
                holder = new HeaderViewHolder();
                switch (headerType) {
                    case HEADER_TYPE_CATEGORY:
                        view = new TextView(getContext(), null,
                                android.R.attr.listSeparatorTextViewStyle);
                        holder.title = (TextView) view;
                        break;

                    case HEADER_TYPE_SWITCH:
                        /*view = mInflater.inflate(R.layout.preference_header_switch_item, parent,
                                false);
                        holder.icon = (ImageView) view.findViewById(R.id.icon);
                        holder.title = (TextView)
                                view.findViewById(com.android.internal.R.id.title);
                        holder.summary = (TextView)
                                view.findViewById(com.android.internal.R.id.summary);
                        holder.switch_ = (Switch) view.findViewById(R.id.switchWidget);
                        break;*/

                    case HEADER_TYPE_INFORMAL:
                    case HEADER_TYPE_NORMAL:
                        view = mInflater.inflate(R.layout.preference_header_item, parent, false);
                        holder.icon = findById(view, R.id.icon);
                        holder.title = findById(view, android.R.id.title);
                        holder.summary = findById(view, android.R.id.summary);
                        break;
                }
                assert (view != null);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (HeaderViewHolder) view.getTag();
            }

            // All view fields must be updated every time, because the view may be recycled
            switch (headerType) {
                case HEADER_TYPE_CATEGORY:
                    holder.title.setText(header.getTitle(getContext().getResources()));
                    break;

                case HEADER_TYPE_SWITCH:
                    // Would need a different treatment if the main menu had more switches
                    /*if (header.id == a_cool_id) {
                        mAnEnabler.setSwitch(holder.switch_);
                    }*/
                    updateCommonHeaderView(header, holder);
                    break;

                case HEADER_TYPE_INFORMAL:
                case HEADER_TYPE_NORMAL:
                    updateCommonHeaderView(header, holder);
                    break;
            }

            return view;
        }

        private void updateCommonHeaderView(final Header header, final HeaderViewHolder holder) {
            if (header.iconRes == 0) {
                holder.icon.setVisibility(View.GONE);
            } else {
                holder.icon.setImageResource(header.iconRes);
                holder.icon.setVisibility(View.VISIBLE);
            }
            holder.title.setText(header.getTitle(getContext().getResources()));

            final CharSequence summary = header.getSummary(getContext().getResources());
            if (!TextUtils.isEmpty(summary)) {
                holder.summary.setVisibility(View.VISIBLE);
                holder.summary.setText(summary);
            } else {
                holder.summary.setVisibility(View.GONE);
            }
        }
    }
}
