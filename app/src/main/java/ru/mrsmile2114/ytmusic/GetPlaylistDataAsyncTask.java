package ru.mrsmile2114.ytmusic;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistListResponse;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class GetPlaylistDataAsyncTask extends AsyncTask<String[], Void, PlaylistListResponse> {
    private static final String YOUTUBE_PLAYLIST_PART = "snippet";
    private static final String YOUTUBE_PLAYLIST_FIELDS = "items(id,snippet(title))";

    private final WeakReference<YouTube> mYouTubeDataApiRef;

    public GetPlaylistDataAsyncTask(YouTube api) {
        this.mYouTubeDataApiRef = new WeakReference<YouTube>(api);
    }

    @Override
    protected PlaylistListResponse doInBackground(String[]... params) {

        final String[] playlistIds = params[0];

        PlaylistListResponse playlistListResponse;
        try {
            playlistListResponse = mYouTubeDataApiRef.get().playlists()
                    .list(YOUTUBE_PLAYLIST_PART)
                    .setId(TextUtils.join(",", playlistIds))
                    .setFields(YOUTUBE_PLAYLIST_FIELDS)
                    .setKey(AppConstants.YOUTUBE_KEY) //Here you will have to provide the keys
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return playlistListResponse;
    }
}