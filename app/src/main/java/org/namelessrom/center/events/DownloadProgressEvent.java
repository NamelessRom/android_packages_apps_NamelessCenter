package org.namelessrom.center.events;

/**
 * Created by alex on 7/21/14.
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
