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

import org.namelessrom.center.items.UpdateInfo;

/**
 * Helper for debugging
 */
public class DebugHelper {

    private static boolean ENABLED = false;

    public static synchronized boolean getEnabled() { return ENABLED; }

    public static synchronized void setEnabled(final boolean enabled) { ENABLED = enabled; }

    public static UpdateInfo getDummyUpdateInfo() {
        return new UpdateInfo("NIGHTLY", "nameless-4.4.4-20140101-p970-NIGHTLY",
                "00cb8a1cb8e90df20945250a0d749233", /* no URL */ "", "20140101");
    }

}
