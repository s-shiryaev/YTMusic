package ru.mrsmile2114.ytmusic.player;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.squareup.picasso.Picasso;

import java.util.List;

import ru.mrsmile2114.ytmusic.R;
import ru.mrsmile2114.ytmusic.dummy.QueueItems;
import ru.mrsmile2114.ytmusic.utils.InternetCheck;
import ru.mrsmile2114.ytmusic.utils.PageCheck;
import ru.mrsmile2114.ytmusic.utils.YTExtract;


public class PlayService extends Service {

    private static final String ACTION_PLAY = "ru.mrsmile2114.ytmusic.player.action.play";
    private static final String ACTION_NEXT = "ru.mrsmile2114.ytmusic.player.action.next";
    private static final String ACTION_PREV = "ru.mrsmile2114.ytmusic.player.action.prev";
    private static final String ACTION_CLOSE = "ru.mrsmile2114.ytmusic.player.action.close";
    private static final String START_NOTIFICATION = "ru.mrsmile2114.ytmusic.player.action.startnotification";
    private static final String INIT_ACTION = "ru.mrsmile2114.ytmusic.player.action.init";
    private static final String BIND_ACTION = "ru.mrsmile2114.ytmusic.player.action.bind";

    private static final int NOTIFICATION_ID = 112345;
    private static final String NOTIFICATION_CHANNEL_ID = "ytmusic_channel_playback";


    private Notification mNotification;
    private NotificationChannel mChannel;
    private CallBack mCallBack;
    private MyBinder mLocalBinder = new MyBinder();
    private MediaPlayer mediaPlayer;
    private int mediaFileLengthInMilliseconds;
    private YouTube mYoutubeDataApi;
    private final GsonFactory mJsonFactory = new GsonFactory();
    private final HttpTransport mTransport = AndroidHttp.newCompatibleTransport();
    public boolean isPaused;
    public int isRepeat = 0;
    public boolean isShuffle;
    private boolean isNotify;
    private boolean waitExtract;
    private final Handler handler = new Handler();
    private SeekBar mSeekBar;

    private RemoteViews views;
    private RemoteViews bigViews;

    public PlayService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build());
        }
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                endOfTheSong();
            }
        });
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaFileLengthInMilliseconds = mp.getDuration();
                mp.start();
                mCallBack.UpdateQueue();
                primarySeekBarProgressUpdater();
            }
        });

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                if ((what==MediaPlayer.MEDIA_ERROR_UNKNOWN)&&(extra==MediaPlayer.MEDIA_ERROR_IO)){
                    Toast.makeText(getApplicationContext(),R.string.player_connection_error,Toast.LENGTH_LONG)
                           .show();
                    return true;
                } else if  ((what==MediaPlayer.MEDIA_ERROR_UNKNOWN)&&(extra==-2147483648)){
                    new InternetCheck(new InternetCheck.Consumer() {
                        @Override
                        public void accept(Boolean internet) {
                            if (internet){
                                Toast.makeText(getApplicationContext(),R.string.player_datasource_error,Toast.LENGTH_LONG)
                                        .show();
                                QueueItems.QueueItem item = QueueItems.getPlayingItem();
                                if (item!=null){
                                    item.setExtracting(true);
                                    new YTExtract(getContext(),mExtractCallBackInterface)
                                            .extract_now(item.getUrl(),140);
                                    Next();
                                    mCallBack.UpdateQueue();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(),R.string.player_connection_error,Toast.LENGTH_LONG)
                                .show();
                            }
                        }
                    });
                    return true;
                } else if (what==-38){
                    //isPaused=false;
                    //isNotify=false;
                    //stopForeground(true);
                    return true;
                }
                return false;
            }
        });
        mYoutubeDataApi = new YouTube.Builder(mTransport, mJsonFactory, null)
                .setApplicationName(getString(R.string.app_name))
                .build();
        initNotification();
    }


    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if((intent!=null)&&(intent.getAction().equals(INIT_ACTION))){

        } else if ((intent!=null)&&(intent.getAction().equals(START_NOTIFICATION))){
            updateNotification();
        } else if ((intent!=null)&&(intent.getAction().equals(ACTION_PLAY))){
            Play();
        } else if ((intent!=null)&&(intent.getAction().equals(ACTION_NEXT))){
            Next();
        } else if ((intent!=null)&&(intent.getAction().equals(ACTION_PREV))){
            Prev();
        } else if ((intent!=null)&&(intent.getAction().equals(ACTION_CLOSE))){
            removeNotification();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("PlayService","onDestroy");
        super.onDestroy();
        removeNotification();
        mediaPlayer.release();
        mediaPlayer=null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mLocalBinder;
    }

    public void setCallBack(CallBack callBack) {
        mCallBack = callBack;
    }

    private Context getContext(){
        return this;
    }

    public void endOfTheSong() {
        if (isRepeat == 1) {
            mediaPlayer.seekTo(0);
            mediaPlayer.start();
        } else {
            Next();
        }
    }

    private void pressPlay(boolean play){
        mCallBack.PressPlay(play);
        if (play){
            views.setImageViewResource(R.id.status_bar_play,R.drawable.ic_media_pause);
            bigViews.setImageViewResource(R.id.status_bar_play,R.drawable.ic_media_pause);
        }else {
            views.setImageViewResource(R.id.status_bar_play,R.drawable.ic_menu_play);
            bigViews.setImageViewResource(R.id.status_bar_play,R.drawable.ic_menu_play);
        }
        updateNotification();
    }

    private void initNotification(){
        // Using RemoteViews to bind custom layouts into Notification
        bigViews = new RemoteViews(getPackageName(), R.layout.status_bar_expanded);
        views = new RemoteViews(getPackageName(), R.layout.status_bar);
        //Play:
        Intent playIntent = new Intent(this, PlayService.class);
        playIntent.setAction(ACTION_PLAY);
        PendingIntent pPlayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);
        bigViews.setOnClickPendingIntent(R.id.status_bar_play,pPlayIntent);
        views.setOnClickPendingIntent(R.id.status_bar_play,pPlayIntent);
        //Next:
        Intent nextIntent = new Intent(this, PlayService.class);
        nextIntent.setAction(ACTION_NEXT);
        PendingIntent pNextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);
        bigViews.setOnClickPendingIntent(R.id.status_bar_next,pNextIntent);
        views.setOnClickPendingIntent(R.id.status_bar_next,pNextIntent);
        //Prev:
        Intent prevIntent = new Intent(this, PlayService.class);
        prevIntent.setAction(ACTION_PREV);
        PendingIntent pPrevIntent = PendingIntent.getService(this,0,
                prevIntent,0);
        bigViews.setOnClickPendingIntent(R.id.status_bar_prev,pPrevIntent);
        views.setOnClickPendingIntent(R.id.status_bar_prev,pPrevIntent);
        //Close:
        Intent closeIntent = new Intent(this, PlayService.class);
        closeIntent.setAction(ACTION_CLOSE);
        PendingIntent pCloseIntent = PendingIntent.getService(this, 0,
                closeIntent, 0);
        bigViews.setOnClickPendingIntent(R.id.status_bar_collapse,pCloseIntent);

        NotificationManager mgr = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,getResources().getString(R.string.channel_name),NotificationManager.IMPORTANCE_LOW);
            mChannel.setShowBadge(false);
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            mChannel.setDescription(getResources().getString(R.string.channel_description));
            mgr.createNotificationChannel(mChannel);
        }
        NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        mNotificationBuilder.setSmallIcon(R.drawable.ic_menu_play);
        mNotificationBuilder.setCustomContentView(views);
        mNotificationBuilder.setCustomBigContentView(bigViews);
        mNotificationBuilder.setOngoing(true);
        mNotification=mNotificationBuilder.build();
    }

    public void updateNotification(){
        NotificationManager mgr = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (!isNotify){
            startForeground(NOTIFICATION_ID, mNotification);
            isNotify=true;
        } else {
            mgr.notify(NOTIFICATION_ID, mNotification);
        }
    }

    public void removeNotification(){
        NotificationManager mgr = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        //mgr.cancel(NOTIFICATION_ID);
        //if (mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        //}
        isPaused=false;
        waitExtract=false;
        QueueItems.QueueItem item = QueueItems.getPlayingItem();
        if (item!=null){
            item.setPlaying(false);
        }
        if (mCallBack!=null){
            mCallBack.PressPlay(false);
            mCallBack.UpdateQueue();
        }
        isNotify=false;
        stopForeground(true);
    }

    private void PlayBackMusic(){
        try{
            pressPlay(true);
            if (isPaused){
                mediaPlayer.start();
                isPaused=false;
            } else {
                QueueItems.QueueItem item = QueueItems.getPlayingItem();
                if (item!=null){
                    initNotification();//needs for prevent RemoteViews cache overflow
                    bigViews.setTextViewText(R.id.status_bar_track_name,item.getTitle());
                    views.setTextViewText(R.id.status_bar_track_name,item.getTitle());
                    Picasso.get().load(item.getThumbnail()).into(bigViews,
                            R.id.status_bar_album_art,NOTIFICATION_ID,
                            mNotification);
                    Picasso.get().load(item.getThumbnail()).into(views,
                            R.id.status_bar_album_art,NOTIFICATION_ID,
                            mNotification);
                    mNotification.tickerText=item.getTitle();
                    updateNotification();
                    prepareMediaPlayer(item);
                } else {
                    pressPlay(false);
                    removeNotification();
                }

            }
        }catch(Exception e){
            pressPlay(false);
            e.printStackTrace();
        }
    }

    private void prepareMediaPlayer(final QueueItems.QueueItem item){
        new InternetCheck(new InternetCheck.Consumer() {//check internet connection
            @Override
            public void accept(Boolean internet) {
                if(internet){
                    new PageCheck(new PageCheck.Consumer() {//check file for accessibility
                        @Override
                        public void accept(Integer code) {
                            if (code==200){
                                try {
                                    mediaPlayer.reset();
                                    mediaPlayer.setDataSource(item.getParsedUrl());
                                    mediaPlayer.prepareAsync();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }else{
                                Toast.makeText(getApplicationContext(),R.string.player_datasource_error,Toast.LENGTH_LONG)
                                        .show();
                                waitExtract=true;//start playing file after get url (if the user still stayed on this file)
                                item.setExtracting(true);
                                new YTExtract(getContext(),mExtractCallBackInterface)
                                            .extract_now(item.getUrl(),140);
                                mCallBack.UpdateQueue();
                            }
                        }
                    }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,item.getParsedUrl());
                }else{
                    Toast.makeText(getApplicationContext(),R.string.player_connection_error, Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
    }

    public void Play(){
        if (QueueItems.getPlayingItem()!=null&&!waitExtract){
                    if(!mediaPlayer.isPlaying()){
                        PlayBackMusic();
                    } else {
                        mediaPlayer.pause();
                        isPaused=true;
                        pressPlay(false);
                    }
                    primarySeekBarProgressUpdater();
                }
    }
    public void Next(){
        isPaused=false;
        waitExtract=false;
        boolean repeat = false;
        if (isRepeat==2){
            repeat=true;
        }
        if (QueueItems.setNextPlayingItem(repeat, isShuffle)!=null) {
            PlayBackMusic();
        } else {
            pressPlay(false);
            removeNotification();
        }
        mCallBack.UpdateQueue();
    }

    public void Prev(){
        isPaused=false;
        waitExtract=false;
        boolean repeat = false;
        if (isRepeat==2){
            repeat=true;
        }
        if (QueueItems.setPrevPlayingItem(repeat)!=null) {
            PlayBackMusic();
        } else {
            mediaPlayer.stop();
            pressPlay(false);
        }
        mCallBack.UpdateQueue();
    }

    public void primarySeekBarProgressUpdater() {
        if (mSeekBar!=null){
            mSeekBar.setProgress((int) (((float) mediaPlayer.getCurrentPosition()
                    / mediaFileLengthInMilliseconds) * 100));
        }
        if (mediaPlayer.isPlaying()) {
            Runnable notif = new Runnable() {
                public void run() {
                    if (mediaPlayer!=null){
                        primarySeekBarProgressUpdater();
                    }
                }
            };

            handler.postDelayed(notif, 1000);
        }
    }

    public void setProgressBar(SeekBar sb){
        mSeekBar = sb;
        mSeekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v.getId() == R.id.seekBar) {
                    SeekBar sb = (SeekBar) v;
                    if (mediaPlayer.isPlaying()) {
                        int playPositionInMilliseconds = (mediaFileLengthInMilliseconds / 100)
                                * mSeekBar.getProgress();
                        mediaPlayer.seekTo(playPositionInMilliseconds);
                    }
                }
                return false;
            }
        });
        mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                mSeekBar.setSecondaryProgress(percent);
            }
        });
        if (mediaPlayer.isPlaying()){
            primarySeekBarProgressUpdater();
        }
    }

    public void QueueItemInteraction(QueueItems.QueueItem item){
        if (!item.isPlaying()&&!item.isExtracting()) {
            isPaused=false;
            waitExtract=false;
            QueueItems.setNextPlayingItem(item);
            mCallBack.UpdateQueue();
            PlayBackMusic();
        }
    }

    public void QueueItemRemove(QueueItems.QueueItem item){
        QueueItems.removeQueueItem(item);
        if (item.isPlaying()){
            isPaused=false;
            waitExtract=false;
            mediaPlayer.stop();
            pressPlay(false);
            removeNotification();
        }
        mCallBack.UpdateQueue();
    }


    public interface CallBack{
        void PressPlay(boolean play);
        void UpdateQueue();
    }



    private YTExtract.ExtractCallBackInterface mExtractCallBackInterface = new YTExtract.ExtractCallBackInterface() {
        @Override
        public void onSuccExtract(String url, String parsedUrl, String title) {
            List<QueueItems.QueueItem> Items  = QueueItems.getItemsByUrl(url);
            if(!(Items.isEmpty())){
                for(int i=0;i<Items.size();i++){
                    Items.get(i).setParsedUrl(parsedUrl);
                    Items.get(i).setExtracting(false);
                }
            }
            mCallBack.UpdateQueue();
            if (waitExtract){
                prepareMediaPlayer(QueueItems.getPlayingItem());
                waitExtract=false;
            }
        }

        @Override
        public void onUnsuccExtractTryAgain(int attempt) {
            //TODO:implement method
        }

        @Override
        public void onUnsuccExtract(String url) {
            List<QueueItems.QueueItem> Items  = QueueItems.getItemsByUrl(url);
            if(!(Items.isEmpty())){
                for(int i=0;i<Items.size();i++){
                    QueueItems.getITEMS().remove(Items.get(i));
                }
            }
            mCallBack.UpdateQueue();
        }
    };


    public boolean isMediaPlayerPlaying(){
        return mediaPlayer.isPlaying();
    }

    //Custom Binder class
    public class MyBinder extends Binder {
        public PlayService getService() {
            return PlayService.this;
        }
    }

}

