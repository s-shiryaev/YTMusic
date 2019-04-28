package ru.mrsmile2114.ytmusic.player;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.VideoListResponse;

import java.lang.ref.WeakReference;
import java.util.List;

import ru.mrsmile2114.ytmusic.MainActivity;
import ru.mrsmile2114.ytmusic.R;
import ru.mrsmile2114.ytmusic.dummy.QueueItems;
import ru.mrsmile2114.ytmusic.utils.GetPlaylistItemsAsyncTask;
import ru.mrsmile2114.ytmusic.utils.GetVideoDataAsyncTask;
import ru.mrsmile2114.ytmusic.utils.YTExtract;

public class SendIntentActivity extends Activity {

    private static WeakReference<MainActivity> currentActivityRef;
    private static WeakReference<YTExtract.ExtractCallBackInterface> extractCallBackRef;

    private YouTube mYoutubeDataApi;
    private final GsonFactory mJsonFactory = new GsonFactory();
    private final HttpTransport mTransport = AndroidHttp.newCompatibleTransport();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mYoutubeDataApi = new YouTube.Builder(mTransport, mJsonFactory, null)
                .setApplicationName(getString(R.string.app_name))
                .build();
        if (savedInstanceState == null && Intent.ACTION_SEND.equals(getIntent().getAction())
                && getIntent().getType() != null && "text/plain".equals(getIntent().getType())) {

            String ytLink = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            if (ytLink != null
                    && (ytLink.contains("://youtu.be/") || ytLink.contains("youtube.com/watch?v=")
                        || ytLink.contains("youtube.com/playlist?list="))) {
                // We have a valid link
                if (ytLink.contains("://youtu.be/") || ytLink.contains("youtube.com/watch?v=")){
                    String id;
                    if(ytLink.contains("://youtu.be/")){
                        id = ytLink.substring(ytLink.indexOf(".be/")+4);
                    } else {
                        id = ytLink.substring(ytLink.indexOf("v=")+2);
                    }
                        //Checks for instance MainActivity, so as not to create unnecessary Activity
                        if ((currentActivityRef == null)||(currentActivityRef.get()==null)){
                            Intent intent = new Intent(this, MainActivity.class);
                            intent.setAction("android.intent.action.MAIN");
                            startActivity(intent);
                        }
                        new GetVideoDataAsyncTask(mYoutubeDataApi ,mGetVideoDataCallBackInterface)
                              .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, id);
                } else {
                    String id = ytLink.substring(ytLink.indexOf("list=")+5);
                    new GetPlaylistItemsAsyncTask(mYoutubeDataApi, playlistCallBack)
                            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, id);
                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.incorrect_url, Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            finish();
        }
    }

    public static void updateActivity(MainActivity activity){
        currentActivityRef = new WeakReference<MainActivity>(activity);
    }

    public static void updateExtractCallBack(YTExtract.ExtractCallBackInterface callback){
        extractCallBackRef = new WeakReference<>(callback);
    }

    private GetVideoDataAsyncTask.GetVideoDataCallBackInterface mGetVideoDataCallBackInterface =
            new GetVideoDataAsyncTask.GetVideoDataCallBackInterface() {
        @Override
        public void onSuccGetVideoData(VideoListResponse response) {
            QueueItems.QueueItem item = new QueueItems.QueueItem(
                    response.getItems().get(0).getSnippet().getTitle(),
                    response.getItems().get(0).getId(),
                    response.getItems().get(0).getSnippet().getThumbnails().getMedium().getUrl());
            QueueItems.addItem(item);
            //check the possibility of using the fragment callback
            if (extractCallBackRef!=null){
                new YTExtract(getApplicationContext(), extractCallBackRef.get())
                        .extract(item.getUrl(), 140);
            } else {
                //If there is no callback, we use standard (without updating the play queue)
                new YTExtract(getApplicationContext(), oneExtractCallBack)
                        .extract(item.getUrl(),140);
            }
            finish();
        }

        @Override
        public void onUnsuccGetPlaylisItems(int id, String error) {
            Toast.makeText(getApplicationContext(),R.string.api_error + error,Toast.LENGTH_LONG)
                    .show();
            finish();
        }
    };

    private GetPlaylistItemsAsyncTask.GetPlaylistItemsCallBackInterface playlistCallBack =
            new GetPlaylistItemsAsyncTask.GetPlaylistItemsCallBackInterface(){

                @Override
                public void onSuccGetPlaylistItems(PlaylistItemListResponse response) {
                    for(int i=0;i<response.getItems().size();i++) {
                        QueueItems.QueueItem item = new QueueItems.QueueItem(
                                response.getItems().get(i).getSnippet().getTitle(),
                                response.getItems().get(i).getContentDetails().getVideoId(),
                                response.getItems().get(i).getSnippet().getThumbnails().getMedium().getUrl());
                        QueueItems.addItem(item);
                        //check the possibility of using the fragment callback
                        if (extractCallBackRef!=null){
                            new YTExtract(getApplicationContext(), extractCallBackRef.get())
                                    .extract(item.getUrl(), 140);
                        } else {
                            //If there is no callback, we use standard (without updating the play queue)
                            new YTExtract(getApplicationContext(), oneExtractCallBack)
                                    .extract(item.getUrl(),140);
                        }
                    }
                    finish();
                }

                @Override
                public void onUnsuccGetPlaylisItems(String error) {
                    Toast.makeText(getApplicationContext(),R.string.api_error + error,Toast.LENGTH_LONG)
                            .show();
                }
            };


        private static YTExtract.ExtractCallBackInterface oneExtractCallBack =
                new YTExtract.ExtractCallBackInterface() {
        @Override
        public void onSuccExtract(String url, String parsedUrl, String title) {
            List<QueueItems.QueueItem> Items  = QueueItems.getItemsByUrl(url);
            if(!(Items.isEmpty())){
                for(int i=0;i<Items.size();i++){
                    Items.get(i).setParsedUrl(parsedUrl);
                    Items.get(i).setExtracting(false);
                }
            }
        }

        @Override
        public void onUnsuccExtractTryAgain(int attempt) {
            //TODO:implement method
        }

        @Override
        public void onUnsuccExtract(String url) {//delete items if extracting failed
            List<QueueItems.QueueItem> Items  = QueueItems.getItemsByUrl(url);
            if(!(Items.isEmpty())){
                for(int i=0;i<Items.size();i++){
                    QueueItems.getITEMS().remove(Items.get(i));
                }
            }
        }
    };

}
