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

package org.namelessrom.center.utils;

import android.content.Intent;
import android.content.pm.PackageManager;

import org.namelessrom.center.AppInstance;

/**
 * Class for helping interacting with other applications
 */
public class AppHelper {

    /**
     * Checks if a specific action exists.
     *
     * @param actionName The action as string
     * @return if the action exists.
     */
    public static boolean actionExists(final String actionName) {
        final Intent i = new Intent();
        i.setAction(actionName);
        return AppInstance.get().getPackageManager()
                .queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY).size() > 0;
    }

}
