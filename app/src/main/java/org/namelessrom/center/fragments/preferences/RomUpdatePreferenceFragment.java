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
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import org.namelessrom.center.Constants;
import org.namelessrom.center.R;
import org.namelessrom.center.utils.Helper;
import org.namelessrom.center.utils.PreferenceHelper;

public class RomUpdatePreferenceFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    public RomUpdatePreferenceFragment() { }

    private ListPreference mUpdateChannel;
    private ListPreference mUpdateCheck;
    private ListPreference mRecoveryType;

    @Override public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.rom_updates);

        int tmp;

        mUpdateChannel = (ListPreference) findPreference(Constants.PREF_UPDATE_CHANNEL);
        if (mUpdateChannel != null) {
            tmp = PreferenceHelper
                    .getInt(Constants.PREF_UPDATE_CHANNEL, Constants.UPDATE_CHANNEL_ALL);
            mUpdateChannel.setValue(String.valueOf(tmp));
            setSummary(mUpdateChannel, tmp);
            mUpdateChannel.setOnPreferenceChangeListener(this);
        }

        mUpdateCheck = (ListPreference) findPreference(Constants.UPDATE_CHECK_PREF);
        if (mUpdateCheck != null) {
            tmp = PreferenceHelper
                    .getInt(Constants.UPDATE_CHECK_PREF, Constants.UPDATE_FREQ_WEEKLY);
            mUpdateCheck.setValue(String.valueOf(tmp));
            setSummary(mUpdateCheck, tmp);
            mUpdateCheck.setOnPreferenceChangeListener(this);
        }

        mRecoveryType = (ListPreference) findPreference(Constants.PREF_RECOVERY_TYPE);
        if (mRecoveryType != null) {
            tmp = PreferenceHelper
                    .getInt(Constants.PREF_RECOVERY_TYPE, Constants.RECOVERY_TYPE_BOTH);
            mRecoveryType.setValue(String.valueOf(tmp));
            setSummary(mRecoveryType, tmp);
            mRecoveryType.setOnPreferenceChangeListener(this);
        }
    }

    private void setSummary(final Preference preference, final int value) {
        int resId = R.string.unknown;
        if (mUpdateChannel == preference) {
            switch (value) {
                case Constants.UPDATE_CHANNEL_ALL:
                    resId = R.string.all;
                    break;
                case Constants.UPDATE_CHANNEL_NIGHTLY:
                    resId = R.string.nightly;
                    break;
                case Constants.UPDATE_CHANNEL_WEEKLY:
                    resId = R.string.weekly;
                    break;
            }
        } else if (mUpdateCheck == preference) {
            switch (value) {
                case Constants.UPDATE_FREQ_NONE:
                    resId = R.string.check_manual;
                    break;
                case Constants.UPDATE_FREQ_AT_APP_START:
                    resId = R.string.check_on_app_start;
                    break;
                case Constants.UPDATE_FREQ_AT_BOOT:
                    resId = R.string.check_on_boot;
                    break;
                case Constants.UPDATE_FREQ_TWICE_DAILY:
                    resId = R.string.check_twice_daily;
                    break;
                case Constants.UPDATE_FREQ_DAILY:
                    resId = R.string.check_daily;
                    break;
                case Constants.UPDATE_FREQ_WEEKLY:
                    resId = R.string.check_weekly;
                    break;
                case Constants.UPDATE_FREQ_BI_WEEKLY:
                    resId = R.string.check_bi_weekly;
                    break;
                case Constants.UPDATE_FREQ_MONTHLY:
                    resId = R.string.check_monthly;
                    break;
            }
        } else if (mRecoveryType == preference) {
            switch (value) {
                case Constants.RECOVERY_TYPE_BOTH:
                    resId = R.string.recovery_type_both;
                    break;
                case Constants.RECOVERY_TYPE_CWM:
                    resId = R.string.recovery_type_cwm;
                    break;
                case Constants.RECOVERY_TYPE_OPEN:
                    resId = R.string.recovery_type_open;
                    break;
            }
        }

        preference.setSummary(resId);
    }

    @Override public boolean onPreferenceChange(final Preference preference, final Object o) {
        if (mUpdateChannel == preference) {
            final int value = Integer.valueOf(String.valueOf(o));
            PreferenceHelper.setInt(Constants.PREF_UPDATE_CHANNEL, value);
            setSummary(preference, value);
            return true;
        } else if (mUpdateCheck == preference) {
            final int value = Integer.valueOf(String.valueOf(o));
            PreferenceHelper.setInt(Constants.UPDATE_CHECK_PREF, value);
            Helper.scheduleUpdateService(value * 1000);
            setSummary(preference, value);
            return true;
        } else if (mRecoveryType == preference) {
            final int value = Integer.valueOf(String.valueOf(o));
            PreferenceHelper.setInt(Constants.PREF_RECOVERY_TYPE, value);
            setSummary(preference, value);
            return true;
        }

        return false;
    }
}
