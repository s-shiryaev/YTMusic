package ru.mrsmile2114.ytmusic;

import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItemListResponse;

import java.io.IOException;
import java.lang.ref.WeakReference;

import ru.mrsmile2114.ytmusic.dummy.PlaylistItems;

public class GetPlaylistItemsAsyncTask extends AsyncTask<String, Void, PlaylistItemListResponse> {
    private static final String YOUTUBE_PLAYLIST_PART = "snippet,contentDetails";
    private static final String YOUTUBE_PLAYLIST_FIELDS = "items(id,snippet(title,thumbnails),contentDetails(videoId))";
    private static final String YOUTUBE_MAX_RESULTS = "25";

    //no memory leak
    private final WeakReference<YouTube> mYouTubeDataApiRef;
    private WeakReference<MainActivity> activityReference;

    private IOException e;

    public GetPlaylistItemsAsyncTask(YouTube api, MainActivity context) {
        this.mYouTubeDataApiRef = new WeakReference<YouTube>(api);
        this.activityReference = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        activityReference.get().SetMainProgressDialogVisible(true);
    }

    @Override
    protected PlaylistItemListResponse doInBackground(String... params) {

        final String playlistId = params[0];

        PlaylistItemListResponse playlistItemListResponse;
        try {

            playlistItemListResponse = mYouTubeDataApiRef.get().playlistItems()
                    .list(YOUTUBE_PLAYLIST_PART)
                    .setPlaylistId(playlistId)
                    .setMaxResults(Long.parseLong(YOUTUBE_MAX_RESULTS))
                    .setFields(YOUTUBE_PLAYLIST_FIELDS)
                    .setKey(AppConstants.YOUTUBE_KEY)
                    .execute();
            //System.out.println(playlistItemListResponse);//TODO:DELETE
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
        activityReference.get().SetMainProgressDialogVisible(false);
        if (playlistListResponse==null){
            Snackbar.make(activityReference.get().getCurrentFocus(),
                    "Youtube API error: "+e.getLocalizedMessage().substring(0,e.getLocalizedMessage().indexOf("{")-1),
                    Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .show();
        } else {
            PlaylistItems.clearItems();//delete data
            for(int i=0;i<playlistListResponse.getItems().size();i++){
                PlaylistItems.addItem(PlaylistItems.createDummyItem(
                        playlistListResponse.getItems().get(i).getSnippet().getTitle(),
                        playlistListResponse.getItems().get(i).getContentDetails().getVideoId(),
                        playlistListResponse.getItems().get(i).getSnippet().getThumbnails().getMedium().getUrl()));
            }
            activityReference.get().GoToFragment(PlaylistItemsFragment.class);
        }

    }
}
