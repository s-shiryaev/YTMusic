package ru.mrsmile2114.ytmusic.utils;

import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.VideoListResponse;

import java.io.IOException;
import java.lang.ref.WeakReference;

import ru.mrsmile2114.ytmusic.AppConstants;
import ru.mrsmile2114.ytmusic.MainActivity;
import ru.mrsmile2114.ytmusic.R;

public class GetVideoDataAsyncTask extends AsyncTask<String, Void, VideoListResponse> {
    private static final String YOUTUBE_PLAYLIST_PART = "snippet,contentDetails";
    private static final String YOUTUBE_PLAYLIST_FIELDS = "items(id,snippet(title,thumbnails),id)";
    private static final String YOUTUBE_MAX_RESULTS = "50";

    //no memory leak
    private final WeakReference<YouTube> mYouTubeDataApiRef;
    private WeakReference<GetVideoDataCallBackInterface> callbackReference;


    private IOException e;

    public GetVideoDataAsyncTask(YouTube api, GetVideoDataCallBackInterface callback) {
        this.mYouTubeDataApiRef = new WeakReference<>(api);
        this.callbackReference = new WeakReference<>(callback);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected VideoListResponse doInBackground(String... params) {

        final String Id = params[0];

        VideoListResponse videoListResponse;
        try {
            videoListResponse = mYouTubeDataApiRef.get().videos()
                    .list(YOUTUBE_PLAYLIST_PART)
                    .setId(Id)
                    .setMaxResults(Long.parseLong(YOUTUBE_MAX_RESULTS))
                    .setFields(YOUTUBE_PLAYLIST_FIELDS)
                    .setKey(AppConstants.YOUTUBE_KEY)
                    .execute();
        } catch (IOException exc) {
            e=exc;
            e.printStackTrace();
            return null;
        }

        return videoListResponse;
    }
    @Override
    protected void onPostExecute(VideoListResponse videoListResponse) {
        super.onPostExecute(videoListResponse);
        if (videoListResponse==null){
            String text;
            if(e.getLocalizedMessage().contains("{")){
                text=e.getLocalizedMessage().substring(0,e.getLocalizedMessage().indexOf("{")-1);
            } else {
                text=e.getLocalizedMessage();
            }
            callbackReference.get().onUnsuccGetPlaylisItems(0, text);
        } else if (videoListResponse.getItems().isEmpty()) {
            callbackReference.get().onUnsuccGetPlaylisItems(1, "");
        } else {
            callbackReference.get().onSuccGetVideoData(videoListResponse);
        }

    }

    public interface GetVideoDataCallBackInterface {
        void onSuccGetVideoData(VideoListResponse response);
        void onUnsuccGetPlaylisItems(int id, String error);
    }
}
