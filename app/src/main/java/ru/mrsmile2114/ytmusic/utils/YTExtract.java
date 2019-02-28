package ru.mrsmile2114.ytmusic.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import java.lang.ref.WeakReference;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

public class YTExtract extends YouTubeExtractor {

    private WeakReference<Context> con;
    private int attempt;
    private int maxAttempts;
    private String videoUrl;
    private WeakReference<ExtractCallBackInterface> callbackReference;

    public YTExtract(@NonNull Context con, String videoUrl, int attempt, int maxAttempts,
                     ExtractCallBackInterface callBack) {
        super(con);
        this.con=new WeakReference<>(con);
        this.attempt=attempt;
        this.videoUrl=videoUrl;
        this.maxAttempts=maxAttempts;
        this.callbackReference=new WeakReference<>(callBack);
    }

    @Override
    protected void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
        int itag = 140;
        if (ytFiles!=null){
            String parsedUrl = ytFiles.get(itag).getUrl();
            callbackReference.get().onSuccExtract(videoUrl,parsedUrl,vMeta.getTitle());
        } else {
            callbackReference.get().onUnsuccExtractTryAgain(attempt);
            if (attempt<maxAttempts){
                new YTExtract(con.get(),videoUrl,attempt+1,maxAttempts,callbackReference.get()).execute(videoUrl);
            } else {
                callbackReference.get().onUnsuccExtract(videoUrl);
            }
        }
    }

    public interface ExtractCallBackInterface {
        void onSuccExtract(String url, String parsedUrl, String title);
        void onUnsuccExtractTryAgain(int attempt);
        void onUnsuccExtract(String url);
    }
}
