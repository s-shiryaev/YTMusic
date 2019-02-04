package ru.mrsmile2114.ytmusic.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DownloadsItems {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<DownloadsItem> ITEMS = new ArrayList<DownloadsItem>();



    public static void addItem(DownloadsItem item) {
        ITEMS.add(item);
    }

    public static DownloadsItem createDummyItem(int position, String title, String downloadId ) {
        return new DownloadsItem(String.valueOf(position), title, downloadId);
    }


    /**
     * A dummy item representing a piece of content.
     */
    public static class DownloadsItem {
        public final String id;
        private String title;
        private String downloadId;

        public DownloadsItem(String id, String title, String downloadId) {
            this.id = id;
            this.title = title;
            this.downloadId = downloadId;
        }

        public String getTitle() {return this.title;}
        public String getDownloadId() {return this.downloadId;}


        @Override
        public String toString() {
            return title;
        }
    }
}
