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

import android.os.Environment;

import java.io.File;

/**
 * Constants, what else?
 */
public class Constants {

    public static final boolean LOCALHOST_TESTING = false; // TODO: set to false

    //==============================================================================================
    // MENUS
    //==============================================================================================
    public static final int MENU_ID_HOME        = 100;
    public static final int MENU_ID_UPDATES     = 200;
    public static final int MENU_ID_PREFERENCES = 300;

    //==============================================================================================
    // URLS
    //==============================================================================================
    public static final String URL             = LOCALHOST_TESTING
            ? "http://192.168.43.234:3000"
            : "https://api.nameless-rom.org";
    public static final String API_URL         = URL + "/version";
    public static final String ROM_URL         = URL + "/update";
    //----------------------------------------------------------------------------------------------
    //public static final String APP_URL         = URL + "/app";
    //public static final String APP_URL_QUERY   = "?skip=%s&count=10";
    //public static final String APP_COUNT_URL   = APP_URL + "/count";
    //public static final String APP_DETAILS_URL = APP_URL + "/%s";
    //public static final String APP_PACKAGE_URL = APP_DETAILS_URL + "/apk";
    //public static final String APP_IMAGE_URL   = APP_DETAILS_URL + "/icon";
    //----------------------------------------------------------------------------------------------
    public static final String CHANNEL_ALL     = "";
    public static final String CHANNEL_NIGHTLY = "NIGHTLY";
    public static final String CHANNEL_WEEKLY  = "WEEKLY";

    //==============================================================================================
    // PATHS
    //==============================================================================================
    public static final String SD_ROOT_DIR              =
            Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String UPDATE_FOLDER            = "Nameless/NamelessCenter";
    public static final String UPDATE_FOLDER_CHANGELOG  =
            SD_ROOT_DIR + File.separator + UPDATE_FOLDER + File.separator + "Changelogs";
    public static final String UPDATE_FOLDER_FULL       =
            SD_ROOT_DIR + File.separator + UPDATE_FOLDER;
    public static final String UPDATE_FOLDER_ADDITIONAL = UPDATE_FOLDER_FULL + "/FlashAfterUpdate";
    public static final String DOWNLOAD_ID              = "download_id";

    //==============================================================================================
    // PREFERENCES
    //==============================================================================================
    public static final String PREF_UPDATE_CHANNEL              = "pref_update_channel";
    public static final int    UPDATE_CHANNEL_ALL               = 0;
    public static final int    UPDATE_CHANNEL_NIGHTLY           = 1;
    public static final int    UPDATE_CHANNEL_WEEKLY            = 2;
    //----------------------------------------------------------------------------------------------
    public static final String UPDATE_CHECK_PREF                = "pref_update_check_interval";
    public static final String UPDATE_TYPE_PREF                 = "pref_update_types";
    public static final String LAST_UPDATE_CHECK_PREF           = "pref_last_update_check";
    public static final String LAST_AUTO_UPDATE_CHECK_PREF      = "pref_last_auto_update_check";
    //----------------------------------------------------------------------------------------------
    public static final String PREF_RECOVERY_TYPE               = "pref_recovery_type";
    public static final int    RECOVERY_TYPE_BOTH               = 0;
    public static final int    RECOVERY_TYPE_CWM                = 1;
    public static final int    RECOVERY_TYPE_OPEN               = 2;
    //----------------------------------------------------------------------------------------------
    public static final String BOOT_CHECK_COMPLETED             = "boot_check_completed";
    public static final int    UPDATE_FREQ_NONE                 = -1;
    public static final int    UPDATE_FREQ_AT_APP_START         = -2;
    public static final int    UPDATE_FREQ_AT_BOOT              = -3;
    public static final int    UPDATE_FREQ_TWICE_DAILY          = 43200;
    public static final int    UPDATE_FREQ_DAILY                = 86400;
    public static final int    UPDATE_FREQ_WEEKLY               = 604800;
    public static final int    UPDATE_FREQ_BI_WEEKLY            = 1209600;
    public static final int    UPDATE_FREQ_MONTHLY              = 2419200;
    //----------------------------------------------------------------------------------------------
    public static final String PREF_UPDATE_METERED              = "pref_update_metered";
    public static final String PREF_UPDATE_METERED_SKIP_WARNING = "pref_update_metered_skip_warn";
    public static final String PREF_UPDATE_ROAMING              = "pref_update_roaming";

}
