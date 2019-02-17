package ru.mrsmile2114.ytmusic.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.SparseArray;

import java.lang.ref.WeakReference;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;
import ru.mrsmile2114.ytmusic.MainActivity;
import ru.mrsmile2114.ytmusic.R;

public class YTExtract extends YouTubeExtractor {

    private final WeakReference<MainActivity> mActivity;
    private int attempt;
    private int maxAttempts;
    private String videoUrl;
    private WeakReference<MainActivity.ExtractCallBackInterface> callbackReference;

    public YTExtract(@NonNull Context con, String videoUrl, int attempt, int maxAttempts,
                     MainActivity.ExtractCallBackInterface callBack) {
        super(con);
        mActivity=new WeakReference<MainActivity>((MainActivity)con);
        this.attempt=attempt;
        this.videoUrl=videoUrl;
        this.maxAttempts=maxAttempts;
        this.callbackReference=new WeakReference<>(callBack);
    }

    @Override
    protected void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
        int itag = 140;
        mActivity.get().SetMainProgressDialogVisible(false);
        if (ytFiles!=null){
            String parsedUrl = ytFiles.get(itag).getUrl();
            callbackReference.get().onSuccExtract(videoUrl,parsedUrl,vMeta.getTitle());
        } else {
            Snackbar.make(mActivity.get().findViewById(R.id.sample_content_fragment),
                    String.format(
                            mActivity.get().getString(R.string.extraction_error_try_again),
                            attempt),
                    Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            if (attempt<maxAttempts){
                new YTExtract(mActivity.get(),videoUrl,attempt+1,maxAttempts,callbackReference.get()).execute(videoUrl);
            }
        }
    }
}
