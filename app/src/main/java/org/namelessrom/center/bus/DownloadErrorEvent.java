package org.namelessrom.center.bus;

/**
 * An event which tells the user, that he / she / it cannot download because of various reasons
 */
public class DownloadErrorEvent {
    public static final int REASON_UNKNOWN      = -1;
    public static final int REASON_OFFLINE      = 0;
    public static final int REASON_METERED      = 1;
    public static final int REASON_METERED_WARN = 2;
    public static final int REASON_ROAMING      = 3;

    private final int reason;

    public DownloadErrorEvent(final int reason) {
        this.reason = reason;
    }

    public int getReason() { return reason; }
}
