package ru.mrsmile2114.ytmusic;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItemListResponse;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class GetPlaylistItemsAsyncTask extends AsyncTask<String, Void, PlaylistItemListResponse> {
    private static final String YOUTUBE_PLAYLIST_PART = "snippet,contentDetails";
    private static final String YOUTUBE_PLAYLIST_FIELDS = "items(id,snippet(title,thumbnails),contentDetails(videoId))";
    private static final String YOUTUBE_MAX_RESULTS = "25";

    private final WeakReference<ProgressDialog> progressDialogRef;  //no memory leak?
    private final WeakReference<YouTube> mYouTubeDataApiRef;        //
    private WeakReference<MainActivity> activityReference;

    private IOException e;

    public GetPlaylistItemsAsyncTask(YouTube api, ProgressDialog dialog, MainActivity context) {
        this.mYouTubeDataApiRef = new WeakReference<YouTube>(api);
        this.progressDialogRef = new WeakReference<ProgressDialog>(dialog);
        this.activityReference = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialogRef.get().setTitle("Please wait.....");
        progressDialogRef.get().show();

    }

    @Override
    protected PlaylistItemListResponse doInBackground(String... params) {

        final String playlistIds = params[0];

        PlaylistItemListResponse playlistItemListResponse;
        try {

            playlistItemListResponse = mYouTubeDataApiRef.get().playlistItems()
                    .list(YOUTUBE_PLAYLIST_PART)
                    .setPlaylistId(playlistIds)
                    .setMaxResults(Long.parseLong(YOUTUBE_MAX_RESULTS))
                    .setFields(YOUTUBE_PLAYLIST_FIELDS)
                    .setKey(AppConstants.YOUTUBE_KEY)
                    .execute();
            System.out.println(playlistItemListResponse);//TODO:DELETE
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
            progressDialogRef.get().dismiss();
            Snackbar.make(activityReference.get().getCurrentFocus(),
                    "Youtube API error: "+e.getLocalizedMessage().substring(0,e.getLocalizedMessage().indexOf("{")-1),
                    Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .show();
            return;
        }
        PlaylistItems.clearItems();//delete data
        for(int i=0;i<playlistListResponse.getItems().size();i++){
            PlaylistItems.addItem(PlaylistItems.createDummyItem(i,
                    playlistListResponse.getItems().get(i).getSnippet().getTitle(),
                    playlistListResponse.getItems().get(i).getContentDetails().getVideoId(),
                    playlistListResponse.getItems().get(i).getSnippet().getThumbnails().getMedium().getUrl()));
        }

        progressDialogRef.get().dismiss();
        Fragment fragment;
        if (activityReference.get().getSupportFragmentManager().findFragmentByTag("FRAGMENT_DOWNLOAD_MANAGE")==null) {
            fragment = new PlaylistItemsFragment();
        } else {
            fragment = activityReference.get().getSupportFragmentManager().findFragmentByTag("FRAGMENT_DOWNLOAD_MANAGE");
        }
        FragmentTransaction transaction = activityReference.get().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment, "FRAGMENT_DOWNLOAD_MANAGE");
        transaction.addToBackStack("FRAGMENT_DOWNLOAD_MANAGE");
        transaction.commit();
        activityReference.get().SetCheckedItem(R.id.nav_manage);//check nav item//TODO: DELETE
    }
}