/*
 *  Copyright (C) 2013 - 2014 Alexander "Evisceration" Martinz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.namelessrom.center.utils;

import org.namelessrom.center.database.DatabaseHandler;
import org.namelessrom.center.database.NamelessTable;

public class PreferenceHelper {
    //==============================================================================================
    // Fields
    //==============================================================================================
    private static PreferenceHelper ourInstance;

    public static final String DEBUG = "pref_debug";

    //==============================================================================================
    // Initialization
    //==============================================================================================

    private PreferenceHelper() { }

    //==============================================================================================
    // Generic
    //==============================================================================================

    public static void remove(final String name) {
        DatabaseHandler.getInstance().deleteItemByName(name, NamelessTable.TABLE);
    }

    public static int getInt(final String name) { return getInt(name, 0); }

    public static int getInt(final String name, final int defaultValue) {
        final String value = DatabaseHandler.getInstance()
                .getValueByName(name, NamelessTable.TABLE);
        return (value == null || value.isEmpty() ? defaultValue : Integer.parseInt(value));
    }

    public static String getString(final String key) { return PreferenceHelper.getString(key, ""); }

    public static String getString(final String name, final String defaultValue) {
        final String value = DatabaseHandler.getInstance()
                .getValueByName(name, NamelessTable.TABLE);
        return (value == null || value.isEmpty() ? defaultValue : value);
    }

    public static boolean getBoolean(final String name) {
        return PreferenceHelper.getBoolean(name, false);
    }

    public static boolean getBoolean(final String name, final boolean defaultValue) {
        final String value = DatabaseHandler.getInstance()
                .getValueByName(name, NamelessTable.TABLE);
        return (value == null || value.isEmpty() ? defaultValue : value.equals("1"));
    }

    public static void setString(final String name, final String value) {
        DatabaseHandler.getInstance().insertOrUpdate(name, value, NamelessTable.TABLE);
    }

    public static void setInt(final String name, final int value) {
        DatabaseHandler.getInstance()
                .insertOrUpdate(name, String.valueOf(value), NamelessTable.TABLE);
    }

    public static void setBoolean(final String name, final boolean value) {
        DatabaseHandler.getInstance()
                .insertOrUpdate(name, (value ? "1" : "0"), NamelessTable.TABLE);
    }

}
