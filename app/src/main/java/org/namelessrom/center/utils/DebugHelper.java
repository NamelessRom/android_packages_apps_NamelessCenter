package org.namelessrom.center.utils;

import org.namelessrom.center.items.UpdateInfo;

/**
 * Helper for debugging
 */
public class DebugHelper {

    public static UpdateInfo getDummyUpdateInfo() {
        return new UpdateInfo("NIGHTLY", "nameless-4.4.4-20140101-p970-NIGHTLY",
                "00cb8a1cb8e90df20945250a0d749233", /* no URL */ "", "20140101");
    }

}
