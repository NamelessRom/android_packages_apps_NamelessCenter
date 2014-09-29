package org.namelessrom.center.bus;

/**
 * An event which refreshes the Update List and updates the card with the same id with the new progress
 */
public class DownloadProgressEvent {
    private final String id;
    private final int    percentage;

    public DownloadProgressEvent(final String id, final int percentage) {
        this.id = id;
        this.percentage = percentage;
    }

    public String getId() { return id; }

    public int getPercentage() { return percentage; }
}
