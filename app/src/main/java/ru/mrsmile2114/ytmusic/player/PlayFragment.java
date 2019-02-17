package ru.mrsmile2114.ytmusic.player;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.VideoListResponse;

import java.util.List;

import ru.mrsmile2114.ytmusic.MainActivity;
import ru.mrsmile2114.ytmusic.R;
import ru.mrsmile2114.ytmusic.dummy.QueueItems;
import ru.mrsmile2114.ytmusic.utils.GetPlaylistItemsAsyncTask;
import ru.mrsmile2114.ytmusic.utils.GetVideoDataAsyncTask;
import ru.mrsmile2114.ytmusic.utils.YTExtract;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PlayFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PlayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlayFragment extends Fragment implements
        QueueItemFragment.OnParentFragmentInteractionListener {


    private OnFragmentInteractionListener mListener;
    private View mView;
    private ImageButton mButtonPlay;
    private ImageButton mButtonAdd;
    private ImageButton mButtonShuffle;
    private ImageButton mButtonRepeat;
    private ImageButton mButtonNext;
    private ImageButton mButtonPrev;
    private SeekBar mSeekBar;
    private Fragment queueList;

    private YouTube mYoutubeDataApi;
    private final GsonFactory mJsonFactory = new GsonFactory();
    private final HttpTransport mTransport = AndroidHttp.newCompatibleTransport();

    private MediaPlayer mediaPlayer;
    private boolean isPaused,isShuffle;
    private int isRepeat=0;
    private int mediaFileLengthInMilliseconds;
    private final Handler handler = new Handler();

    public PlayFragment() {
        // Required empty public constructor
    }

    public static PlayFragment newInstance(String param1, String param2) {
        PlayFragment fragment = new PlayFragment();
        Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queueList = new QueueItemFragment();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build());
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
                UpdateQueue();
                primarySeekBarProgressUpdater();
            }
        });
        mYoutubeDataApi = new YouTube.Builder(mTransport, mJsonFactory, null)
                .setApplicationName(getString(R.string.app_name))
                .build();
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView=inflater.inflate(R.layout.fragment_play, container, false);
        mButtonNext = mView.findViewById(R.id.imageButtonNext);
        mButtonPrev = mView.findViewById(R.id.imageButtonPrev);
        mButtonRepeat = mView.findViewById(R.id.imageButtonRepeat);
        mButtonShuffle = mView.findViewById(R.id.imageButtonShuffle);
        mListener.SetTitle(getString(R.string.action_play));
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout_play,queueList);
        transaction.commit();
        mSeekBar=mView.findViewById(R.id.seekBar);
        mSeekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v.getId() == R.id.seekBar) {
                    if (mediaPlayer.isPlaying()) {
                        SeekBar sb = (SeekBar) v;
                        int playPositionInMilliseconds = (mediaFileLengthInMilliseconds / 100)
                                * sb.getProgress();
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
        mButtonPlay=mView.findViewById(R.id.imageButtonPlay);
        if(mediaPlayer.isPlaying()){
            mButtonPlay.setImageResource(R.drawable.ic_media_pause);
        }
        mButtonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (QueueItems.getPlayingItem()!=null){
                    if(!mediaPlayer.isPlaying()){
                        PlayBackMusic();
                    } else {
                        mediaPlayer.pause();
                        isPaused=true;
                        mButtonPlay.setImageResource(R.drawable.ic_menu_play);
                    }
                    primarySeekBarProgressUpdater();
                }
            }
        });
        mButtonAdd=mView.findViewById(R.id.imageButtonAdd);
        mButtonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogAdd();
            }
        });
        mButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                if (QueueItems.setNextPlayingItem(false, false)!=null) {
                    PlayBackMusic();
                } else {
                mButtonPlay.setImageResource(R.drawable.ic_menu_play);
                }
                UpdateQueue();
            }
        });
        mButtonPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                if (QueueItems.setPrevPlayingItem(false)!=null) {
                    PlayBackMusic();
                } else {
                    mButtonPlay.setImageResource(R.drawable.ic_menu_play);
                }
                UpdateQueue();
            }
        });
        mButtonRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (isRepeat){
                    case 0: {
                        isRepeat=1;
                        mButtonRepeat.setImageResource(R.drawable.ic_media_repeat_one);
                        break;
                    }
                    case 1:{
                        isRepeat=2;
                        mButtonRepeat.setImageResource(R.drawable.ic_media_repeat_all);
                        break;
                    }
                    case 2:{
                        isRepeat=0;
                        mButtonRepeat.setImageResource(R.drawable.ic_media_repeat);
                    }
                }
            }
        });
        mButtonShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isShuffle){
                    isShuffle=true;
                    mButtonShuffle.setImageResource(R.drawable.ic_media_shuffle_enabled);
                } else {
                    isShuffle=false;
                    mButtonShuffle.setImageResource(R.drawable.ic_media_shuffle);
                }
            }
        });
        primarySeekBarProgressUpdater();
        return mView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void primarySeekBarProgressUpdater() {
        mSeekBar.setProgress((int) (((float) mediaPlayer
                .getCurrentPosition() / mediaFileLengthInMilliseconds) * 100));
        if (mediaPlayer.isPlaying()) {
            Runnable notification = new Runnable() {
                public void run() {
                    primarySeekBarProgressUpdater();
                }
            };
            handler.postDelayed(notification, 1000);
        }
    }

    public void UpdateQueue(){
        ((QueueItemFragment)queueList).RefreshRecyclerView();
    }


    private void DialogAdd(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.input_link);
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String s = input.getText().toString();
                if ((s.contains("youtube.com/watch?v=")||
                        s.contains("www.youtube.com/playlist?list=")||
                        s.contains("youtu.be/"))){
                    if (s.contains("www.youtube.com/playlist?list=")){
                        s=s.copyValueOf(s.toCharArray(),s.indexOf("=")+1,
                                s.length()-s.indexOf("=")-1);//get playlist id
                        new GetPlaylistItemsAsyncTask(mYoutubeDataApi,(MainActivity) getActivity(),
                                mGetPlaylistItemsCallBackInterface).execute(s);
                    } else {
                        if(s.contains("youtube.com/watch?v=")){
                            s=s.copyValueOf(s.toCharArray(),s.indexOf("=")+1,
                                    s.length()-s.indexOf("=")-1);
                        }else {
                            s=s.copyValueOf(s.toCharArray(),s.lastIndexOf("/")+1,
                                    s.length()-s.lastIndexOf("/")-1);
                        }
                        new GetVideoDataAsyncTask(mYoutubeDataApi, (MainActivity) getActivity(),
                                mGetVideoDataCallBackInterface).execute(s);
                    }

                }else {
                    Snackbar.make(mView, getString(R.string.incorrect_url),
                            Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void PlayBackMusic(){
        try{
            mButtonPlay.setImageResource(R.drawable.ic_media_pause);
            if (isPaused){
                mediaPlayer.start();
            } else {
                QueueItems.QueueItem item = QueueItems.getPlayingItem();
                if (item!=null){
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(item.getParsedUrl());
                    //System.out.println("DEBUG "+item.getTitle());
                    mediaPlayer.prepareAsync();
                } else {
                    mButtonPlay.setImageResource(R.drawable.ic_menu_play);
                }

            }
        }catch(Exception e){
            mButtonPlay.setImageResource(R.drawable.ic_menu_play);
            e.printStackTrace();
        }
    }


    public void endOfTheSong() {
        if (isRepeat == 1) {
            mediaPlayer.seekTo(0);
            mediaPlayer.start();
        } else {
            nextSong();
        }
    }

    public void nextSong(){
        if (isRepeat==2){
            if (QueueItems.setNextPlayingItem(true, isShuffle)!=null){
                PlayBackMusic();
            } else {
                mButtonPlay.setImageResource(R.drawable.ic_menu_play);
            }
        } else {
            if (QueueItems.setNextPlayingItem(false, isShuffle)!=null){
                PlayBackMusic();
            } else {
                mButtonPlay.setImageResource(R.drawable.ic_menu_play);
            }
        }
        UpdateQueue();
    }

    @Override
    public void onQueueFragmentInteraction(QueueItems.QueueItem item) {
        if (!item.isPlaying()){
            QueueItems.setNextPlayingItem(item);
            UpdateQueue();
            if (isPaused){
                mediaPlayer.stop();
                isPaused=false;
            }
            PlayBackMusic();
        }
    }

    @Override
    public void onQueueFragmentRemove(QueueItems.QueueItem item) {
        QueueItems.removeQueueItem(item);
        if (item.isPlaying()){
            mediaPlayer.stop();
            mButtonPlay.setImageResource(R.drawable.ic_menu_play);
        }
        UpdateQueue();
    }


    public interface OnFragmentInteractionListener {
        void SetTitle(String title);
    }

    private MainActivity.GetVideoDataCallBackInterface mGetVideoDataCallBackInterface = new MainActivity.GetVideoDataCallBackInterface() {
        @Override
        public void onSuccGetVideoData(VideoListResponse response) {
            QueueItems.QueueItem item = new QueueItems.QueueItem(
                    response.getItems().get(0).getSnippet().getTitle(),
                    response.getItems().get(0).getId());
            QueueItems.addItem(item);
            UpdateQueue();
            new YTExtract(getActivity(),item.getUrl(), 1, 6, mExtractCallBackInterface)
                    .execute(item.getUrl());
        }
    };

    private MainActivity.ExtractCallBackInterface mExtractCallBackInterface = new MainActivity.ExtractCallBackInterface() {
        @Override
        public void onSuccExtract(String url, String parsedUrl, String title) {
            List<QueueItems.QueueItem> Items  = QueueItems.getItemsByUrl(url);
            if(!(Items.isEmpty())){
                for(int i=0;i<Items.size();i++){
                    Items.get(i).setParsedUrl(parsedUrl);
                    Items.get(i).setExtracting(false);
                }
            }
            UpdateQueue();
        }
    };

    private MainActivity.GetPlaylistItemsCallBackInterface mGetPlaylistItemsCallBackInterface = new MainActivity.GetPlaylistItemsCallBackInterface() {
        @Override
        public void onSuccGetPlaylistItems(PlaylistItemListResponse response) {
            for(int i=0;i<response.getItems().size();i++) {
                QueueItems.QueueItem item = new QueueItems.QueueItem(
                        response.getItems().get(i).getSnippet().getTitle(),
                        response.getItems().get(i).getContentDetails().getVideoId());
                QueueItems.addItem(item);
                new YTExtract(getActivity(), item.getUrl(),1,6,mExtractCallBackInterface)
                        .execute(item.getUrl());
            }
            UpdateQueue();
        }
    };


}
