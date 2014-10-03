/*
 * Copyright 2014 ParanoidAndroid Project
 *
 * This file is part of Paranoid OTA.
 *
 * Paranoid OTA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Paranoid OTA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Paranoid OTA.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.namelessrom.center.activities;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import org.namelessrom.center.R;
import org.namelessrom.center.Utils;
import org.namelessrom.center.Version;
import org.namelessrom.center.updater.RomUpdater;
import org.namelessrom.center.updater.Updater.UpdaterListener;

public class SystemActivity extends Activity implements UpdaterListener {

    private RomUpdater mRomUpdater;

    private Version mRom;

    private TextView mTitle;
    private TextView mMessage;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_system);

        Utils.setRobotoThin(this, findViewById(R.id.mainlayout));

        mTitle = (TextView) findViewById(R.id.title);
        mMessage = (TextView) findViewById(R.id.message);
        mButton = (Button) findViewById(R.id.button);
        mButton.setVisibility(View.GONE);

        mButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mRomUpdater.check(true);
            }

        });

        mRom = null;

        mRomUpdater = new RomUpdater(this, true);
        mRomUpdater.addUpdaterListener(this);

        mRomUpdater.check(true);
    }

    @Override
    public void startChecking() {
        setMessages(null);
    }

    @Override
    public void versionFound(Version[] info) {
        setMessages(info);
    }

    @Override
    public void checkError(String cause) {
    }

    private void setMessages(Version[] info) {
        if (info != null && info.length > 0) {
                mRom = info.length > 0 ? info[0] : null;
        }
        Resources res = getResources();
        boolean checking = mRomUpdater.isScanning();
        if (checking) {
            mTitle.setText(R.string.all_up_to_date);
            mMessage.setText(R.string.rom_scanning);
            mButton.setVisibility(View.GONE);
        } else {
            mButton.setVisibility(View.VISIBLE);
            /*if (mRom != null) {
                mTitle.setText(R.string.rom_gapps_new_version);
                mMessage.setText(res.getString(R.string.system_update_found,
                        new Object[] {
                            mRom.getFilename() + "\n" + mGapps.getFilename()
                        }));
            } else*/ if (mRom != null) {
                mTitle.setText(R.string.rom_new_version);
                mMessage.setText(res.getString(R.string.system_update_found,
                        mRom.getReadableName()));
            } else {
                mTitle.setText(R.string.all_up_to_date);
                mMessage.setText(R.string.no_updates);
            }
        }
    }
}