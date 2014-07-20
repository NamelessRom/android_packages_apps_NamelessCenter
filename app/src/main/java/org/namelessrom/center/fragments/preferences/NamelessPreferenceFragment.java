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

package org.namelessrom.center.fragments.preferences;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;

import org.namelessrom.center.Logger;
import org.namelessrom.center.R;
import org.namelessrom.center.utils.PreferenceHelper;

public class NamelessPreferenceFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private SwitchPreference mDebugMode;

    public NamelessPreferenceFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml._nameless);

        mDebugMode = (SwitchPreference) findPreference(PreferenceHelper.DEBUG);
        if (mDebugMode != null) {
            mDebugMode.setChecked(PreferenceHelper.getBoolean(PreferenceHelper.DEBUG, false));
            mDebugMode.setOnPreferenceChangeListener(this);
        }
    }

    @Override public boolean onPreferenceChange(final Preference preference, final Object o) {
        if (mDebugMode == preference) {
            final boolean value = ((Boolean) o);
            mDebugMode.setChecked(value);
            PreferenceHelper.setBoolean(PreferenceHelper.DEBUG, value);
            Logger.setEnabled(value);
            return true;
        }
        return false;
    }
}
