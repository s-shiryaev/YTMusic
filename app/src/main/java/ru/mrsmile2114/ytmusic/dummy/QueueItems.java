package ru.mrsmile2114.ytmusic.dummy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//simple class that implements the data model
public class QueueItems {

    /**
     * An array of sample (dummy) items.
     */
    private static final List<QueueItem> ITEMS = new ArrayList<QueueItem>();


    public static void addItem(QueueItem item) {
        ITEMS.add(item);
    }

    public static List<QueueItem> getITEMS(){
        return ITEMS;
    }

    public static List<QueueItem> getItemsByUrl(String url){
        List<QueueItem> Items = new ArrayList<>();
        for (int i=0;i<ITEMS.size();i++) {
            if (ITEMS.get(i).getUrl().equals(url)) {
                Items.add(ITEMS.get(i));
            }
        }
        return Items;
    }

    public static QueueItem getPlayingItem(){
        for (int i=0;i<ITEMS.size();i++) {
            if (ITEMS.get(i).isPlaying()){
                return ITEMS.get(i);
            }
        }
        if(ITEMS.size()>0){
            ITEMS.get(0).setPlaying(true);
            return ITEMS.get(0);
        }
       return null;
    }
    public static QueueItem setNextPlayingItem(boolean repeat, boolean shuffle){
        if (!shuffle){
            for (int i=0;i<ITEMS.size()-1;i++) {
                if (ITEMS.get(i).isPlaying()){
                    ITEMS.get(i).setPlaying(false);
                    ITEMS.get(i+1).setPlaying(true);
                    return ITEMS.get(i+1);
                }
            }
            if (repeat&&(ITEMS.size()>0)) {
                ITEMS.get(ITEMS.size() - 1).setPlaying(false);
                ITEMS.get(0).setPlaying(true);
                return ITEMS.get(0);
            }else if((!repeat)&&(ITEMS.size()>0)){
                ITEMS.get(ITEMS.size() - 1).setPlaying(false);
                return null;
            }
        } else if(ITEMS.size()>0){
            Random rand = new Random();
            int randInt = rand.nextInt(ITEMS.size());
            System.out.println("RAND INT: "+randInt);
            for (int i=0;i<ITEMS.size();i++){
                if (ITEMS.get(i).isPlaying()){
                    ITEMS.get(i).setPlaying(false);
                }
            }
            ITEMS.get(randInt).setPlaying(true);
            return ITEMS.get(randInt);
        }
        return null;
    }
    public static void setNextPlayingItem (QueueItem item){
        int index = ITEMS.indexOf(item);
        if (index!=-1){
            for (int i=0;i<ITEMS.size();i++){
                if (ITEMS.get(i).isPlaying()){
                    ITEMS.get(i).setPlaying(false);
                }
            }
            ITEMS.get(index).setPlaying(true);
        }
    }

    public static QueueItem setPrevPlayingItem(boolean repeat){
        for (int i=1;i<ITEMS.size();i++) {
            if (ITEMS.get(i).isPlaying()){
                ITEMS.get(i).setPlaying(false);
                ITEMS.get(i-1).setPlaying(true);
                return ITEMS.get(i-1);
            }
        }
        if (repeat&&(ITEMS.size()>0)){
            ITEMS.get(0).setPlaying(false);
            ITEMS.get(ITEMS.size()-1).setPlaying(true);
            return ITEMS.get(ITEMS.size()-1);
        }
        return null;
    }

    public static void removeQueueItem(QueueItem item){
        ITEMS.remove(item);
    }


    /**
     * A dummy item representing a piece of content.
     */
    public static class QueueItem {
        private final String title;
        private final String url;
        private boolean isPlaying;
        private boolean isExtracting;
        private String parsedUrl;

        public QueueItem(String title, String url) {
            this.title = title;
            this.url = url;
            this.isPlaying=false;
            this.isExtracting=true;
            this.parsedUrl="";
        }

        public String getTitle() {return title;}
        public String getUrl() {return "https://www.youtube.com/watch?v="+url;}
        public boolean isPlaying() {return isPlaying;}
        public boolean isExtracting() {return isExtracting;}
        public String getParsedUrl() {return parsedUrl;}

        public void setPlaying(boolean playing) {isPlaying = playing;}

        public void setExtracting(boolean extracting) {
            isExtracting = extracting;
        }

        public void setParsedUrl(String parsedUrl) {
            this.parsedUrl = parsedUrl;
        }

        @Override
        public String toString() {
            return title;
        }
    }
}
