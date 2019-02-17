package ru.mrsmile2114.ytmusic.utils;

import android.os.AsyncTask;
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
    private static final String YOUTUBE_MAX_RESULTS = "25";

    //no memory leak
    private final WeakReference<YouTube> mYouTubeDataApiRef;
    private WeakReference<MainActivity> activityReference;
    private WeakReference<MainActivity.GetVideoDataCallBackInterface> callbackReference;


    private IOException e;

    public GetVideoDataAsyncTask(YouTube api, MainActivity context, MainActivity.GetVideoDataCallBackInterface callback) {
        this.mYouTubeDataApiRef = new WeakReference<>(api);
        this.activityReference = new WeakReference<>(context);
        this.callbackReference = new WeakReference<>(callback);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        activityReference.get().SetMainProgressDialogVisible(true);
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
        activityReference.get().SetMainProgressDialogVisible(false);
        if (videoListResponse==null){
            if (activityReference.get().getCurrentFocus()!=null) {
                Snackbar.make(activityReference.get().getCurrentFocus(),
                        activityReference.get().getString(R.string.api_error) + e.getLocalizedMessage().substring(0, e.getLocalizedMessage().indexOf("{") - 1),
                        Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .show();
            }
        } else if (videoListResponse.getItems().isEmpty()) {
            if (activityReference.get().getCurrentFocus()!=null){
                Snackbar.make(activityReference.get().getCurrentFocus(),
                        activityReference.get().getString(R.string.video_not_found),
                        Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .show();
            }
        } else {
            callbackReference.get().onSuccGetVideoData(videoListResponse);
        }

    }
}
