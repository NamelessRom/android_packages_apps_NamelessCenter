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

package org.namelessrom.center.helpers.recovery;

import android.content.Context;
import android.text.TextUtils;

import org.namelessrom.center.IOUtils;
import org.namelessrom.center.Utils;

import java.util.ArrayList;
import java.util.List;

public class TwrpRecovery extends RecoveryInfo {

    public TwrpRecovery() {
        super();

        setId(Utils.TWRP);
        setName("twrp");
        setInternalSdcard("sdcard");
        setExternalSdcard("external_sd");
    }

    @Override
    public String getCommandsFile() {
        return "openrecoveryscript";
    }

    @Override
    public String[] getCommands(Context context, String[] items, String[] originalItems,
            boolean wipeData, boolean wipeCaches, String backupFolder, String backupOptions) {

        List<String> commands = new ArrayList<String>();

        int size = items.length, i = 0;

        boolean hasAndroidSecure = IOUtils.hasAndroidSecure();
        boolean hasSdExt = IOUtils.hasSdExt();

        if (backupFolder != null) {
            String str = "backup ";
            if (!TextUtils.isEmpty(backupOptions)) {
                if (backupOptions.contains("S")) {
                    str += "S";
                }
                if (backupOptions.contains("D")) {
                    str += "D";
                }
                if (backupOptions.contains("C")) {
                    str += "C";
                }
                if (backupOptions.contains("R")) {
                    str += "R";
                }
            }

            str += "123";

            if (!TextUtils.isEmpty(backupOptions)) {
                if (backupOptions.contains("B")) {
                    str += "B";
                }
                if (backupOptions.contains("A") && hasAndroidSecure) {
                    str += "A";
                }
                if (backupOptions.contains("E") && hasSdExt) {
                    str += "E";
                }
            }
            commands.add(str + "O " + backupFolder);
        }

        if (wipeData) {
            commands.add("wipe data");
        }
        if (wipeCaches) {
            commands.add("wipe cache");
            commands.add("wipe dalvik");
        }

        for (; i < size; i++) {
            commands.add("install " + items[i]);
        }

        return commands.toArray(new String[commands.size()]);

    }
}
