package ru.mrsmile2114.ytmusic;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.util.SparseArray;

import java.lang.ref.WeakReference;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;
import ru.mrsmile2114.ytmusic.dummy.DownloadsItems;

public class YTExtractor extends YouTubeExtractor {

    private final WeakReference<MainActivity> mActivity;

    public YTExtractor(@NonNull Context con) {
        super(con);
        mActivity=new WeakReference<MainActivity>((MainActivity)con);
    }

    @Override
    protected void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
        int itag = 140;
        if (ytFiles!=null){
            String downloadUrl = ytFiles.get(itag).getUrl();
            YtFile ytFile = ytFiles.get(itag);
            YtFile audioFile = ytFile;
            String downloadIds = "";
            String filename;
            String videoTitle = vMeta.getTitle();
            if (videoTitle.length() > 55) {
                filename = videoTitle.substring(0, 55);
            } else {
                filename = videoTitle;
            }
            filename = filename.replaceAll("[\\\\><\"|*?%:#/]", "");
            filename += "." + audioFile.getFormat().getExt();
            downloadIds+=mActivity.get().downloadFromUrl(downloadUrl ,videoTitle, filename,false);
            DownloadsItems.addItem(DownloadsItems.createDummyItem(videoTitle, downloadIds));
            Log.w("DEBUG:", downloadIds);
            mActivity.get().SetMainProgressDialogVisible(false);
            mActivity.get().GoToFragment(DownloadsFragment.class);
        } else {
            mActivity.get().SetMainProgressDialogVisible(false);
            Snackbar.make(mActivity.get().findViewById(R.id.sample_content_fragment),
                    "Please insert correct link to the playlist/video!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }
}
