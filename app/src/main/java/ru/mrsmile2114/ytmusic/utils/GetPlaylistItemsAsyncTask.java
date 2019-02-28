package ru.mrsmile2114.ytmusic.utils;

import android.os.AsyncTask;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItemListResponse;

import java.io.IOException;
import java.lang.ref.WeakReference;

import ru.mrsmile2114.ytmusic.AppConstants;

public class GetPlaylistItemsAsyncTask extends AsyncTask<String, Void, PlaylistItemListResponse> {
    private static final String YOUTUBE_PLAYLIST_PART = "snippet,contentDetails";
    private static final String YOUTUBE_PLAYLIST_FIELDS = "items(id,snippet(title,thumbnails),contentDetails(videoId)),nextPageToken";
    private static final String YOUTUBE_MAX_RESULTS = "50";

    //no memory leak
    private final WeakReference<YouTube> mYouTubeDataApiRef;
    private WeakReference<GetPlaylistItemsCallBackInterface> callbackReference;
    private String nextPageToken;

    private IOException e;

    public GetPlaylistItemsAsyncTask(YouTube api, GetPlaylistItemsCallBackInterface callback) {
        this.mYouTubeDataApiRef = new WeakReference<YouTube>(api);
        this.callbackReference = new WeakReference<>(callback);
    }

    private GetPlaylistItemsAsyncTask(YouTube api, GetPlaylistItemsCallBackInterface callback,String nextPageToken) {
        this.mYouTubeDataApiRef = new WeakReference<YouTube>(api);
        this.callbackReference = new WeakReference<>(callback);
        this.nextPageToken = nextPageToken;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected PlaylistItemListResponse doInBackground(String... params) {

        final String playlistId = params[0];

        PlaylistItemListResponse playlistItemListResponse;
        try {
            if (nextPageToken!=null){
                playlistItemListResponse =mYouTubeDataApiRef.get().playlistItems()
                        .list(YOUTUBE_PLAYLIST_PART)
                        .setPlaylistId(playlistId)
                        .setMaxResults(Long.parseLong(YOUTUBE_MAX_RESULTS))
                        .setFields(YOUTUBE_PLAYLIST_FIELDS)
                        .setKey(AppConstants.YOUTUBE_KEY)
                        .setPageToken(nextPageToken)
                        .execute();
            } else {
                playlistItemListResponse = mYouTubeDataApiRef.get().playlistItems()
                        .list(YOUTUBE_PLAYLIST_PART)
                        .setPlaylistId(playlistId)
                        .setMaxResults(Long.parseLong(YOUTUBE_MAX_RESULTS))
                        .setFields(YOUTUBE_PLAYLIST_FIELDS)
                        .setKey(AppConstants.YOUTUBE_KEY)
                        .execute();
            }
            if (playlistItemListResponse.getNextPageToken()!=null){
                new GetPlaylistItemsAsyncTask(mYouTubeDataApiRef.get(),callbackReference.get(),playlistItemListResponse.getNextPageToken()).execute(playlistId);
            }

        } catch (IOException exc) {
            e=exc;
            e.printStackTrace();
            return null;
        }

        return playlistItemListResponse;
    }
    @Override
    protected void onPostExecute(PlaylistItemListResponse playlistListResponse) {
        super.onPostExecute(playlistListResponse);
        if (playlistListResponse==null){
            String text;
            if(e.getLocalizedMessage().contains("{")){
                text=e.getLocalizedMessage().substring(0,e.getLocalizedMessage().indexOf("{")-1);
            } else {
                text=e.getLocalizedMessage();
            }
            callbackReference.get().onUnsuccGetPlaylisItems(text);
        } else {
            callbackReference.get().onSuccGetPlaylistItems(playlistListResponse);
        }
    }

    public interface GetPlaylistItemsCallBackInterface{
        void onSuccGetPlaylistItems(PlaylistItemListResponse response);
        void onUnsuccGetPlaylisItems(String error);
    }
}
