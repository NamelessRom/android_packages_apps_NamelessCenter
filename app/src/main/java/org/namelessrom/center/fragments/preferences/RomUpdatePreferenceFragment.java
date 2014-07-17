package org.namelessrom.center.fragments.preferences;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import org.namelessrom.center.R;

/**
 * Created by alex on 7/18/14.
 */
public class RomUpdatePreferenceFragment extends PreferenceFragment {

    public RomUpdatePreferenceFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.rom_updates);
    }
}
