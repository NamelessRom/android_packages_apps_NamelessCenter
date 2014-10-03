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

package org.namelessrom.center.updater;

import android.content.Context;
import android.text.TextUtils;

import org.namelessrom.center.R;
import org.namelessrom.center.Utils;
import org.namelessrom.center.updater.server.NamelessServer;

public class RomUpdater extends Updater {

    public RomUpdater(Context context, boolean fromAlarm) {
        super(context, new Server[]{
                new NamelessServer()
        }, fromAlarm);
    }

    @Override
    public String getDevice() {
        final String device = Utils.getProp(PROPERTY_DEVICE);
        return TextUtils.isEmpty(device) ? "" : device.toLowerCase();
    }

    @Override
    public int getErrorStringId() {
        return R.string.check_rom_updates_error;
    }

}
