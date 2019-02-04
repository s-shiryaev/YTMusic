package ru.mrsmile2114.ytmusic.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DownloadsItems {

    /**
     * An array of sample (dummy) items.
     */
    private static final List<DownloadsItem> ITEMS = new ArrayList<DownloadsItem>();



    public static void addItem(DownloadsItem item) {
        ITEMS.add(item);
    }

    public static DownloadsItem createDummyItem(String title, String downloadId ) {
        return new DownloadsItem(title, downloadId);
    }

    public static List<DownloadsItem> getITEMS() {
        return ITEMS;
    }
    public static DownloadsItem getITEMbyId(int id){return ITEMS.get(id);}

    /**
     * A dummy item representing a piece of content.
     */
    public static class DownloadsItem {
        private String title;
        private String downloadId;

        public DownloadsItem(String title, String downloadId) {
            this.title = title;
            this.downloadId = downloadId;
        }

        public String getTitle() {return this.title;}
        public String getDownloadId() {return this.downloadId;}
        public void setDownloadId(String id){this.downloadId=id;}


        @Override
        public String toString() {
            return title;
        }
    }
}
