package ru.mrsmile2114.ytmusic.dummy;

import java.util.ArrayList;
import java.util.List;

//simple class that implements the data model
public class PlaylistItems {

    /**
     * An array of sample (dummy) items.
     */
    private static final List<PlaylistItem> ITEMS = new ArrayList<PlaylistItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */


    public static void addItem(PlaylistItem item) {
        ITEMS.add(item);
    }

    public static PlaylistItem createDummyItem(int position, String title, String url, String thumbnail) {//!!!!
        return new PlaylistItem(String.valueOf(position), title, url, thumbnail,false);
    }

    public static List<PlaylistItem> getITEMS() {
        return ITEMS;
    }

    public static List<PlaylistItem> getCheckedItems() {
        List<PlaylistItem> CheckedItems = new ArrayList<PlaylistItem>();
        for (int i=0;i<ITEMS.size();i++){
            if (ITEMS.get(i).getChecked()){
                CheckedItems.add(ITEMS.get(i));
            }
        }
        return CheckedItems;
    }

    public static void clearItems(){
        ITEMS.clear();
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class PlaylistItem {
        public final String id;
        private String title;
        private String url;
        private String thumbnail;
        private boolean isChecked;

        public PlaylistItem(String id, String title, String url, String thumbnail, boolean isChecked) {
            this.id = id;
            this.title = title;
            this.url = url;
            this.thumbnail=thumbnail;
            this.isChecked = isChecked;
        }


        public void setChecked(boolean temp){ this.isChecked=temp;}
        public boolean getChecked() { return this.isChecked;}
        public String getTitle() {return this.title;}
        public String getThumbnail() {return this.thumbnail;}
        public String getUrl() {return "https://www.youtube.com/watch?v="+this.url;}
    }

}
