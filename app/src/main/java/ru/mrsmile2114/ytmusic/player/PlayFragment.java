package ru.mrsmile2114.ytmusic.player;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.view.LayoutInflater;
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
        QueueItemFragment.OnParentFragmentInteractionListener,
        PlayService.CallBack,
        ServiceConnection {


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

    private boolean isPaused,isShuffle;
    private int isRepeat=0;
    private int mediaFileLengthInMilliseconds;
    private final Handler handler = new Handler();

    private PlayService playService;

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
        SendIntentActivity.updateExtractCallBack(mExtractCallBackInterface);
        queueList = new QueueItemFragment();
        mYoutubeDataApi = new YouTube.Builder(mTransport, mJsonFactory, null)
                .setApplicationName(getString(R.string.app_name))
                .build();

        Intent serviceIntent = new Intent(getActivity().getApplicationContext(), PlayService.class);
        serviceIntent.setAction("ru.mrsmile2114.ytmusic.player.action.init");
        //getActivity().startService(serviceIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(getActivity().getApplicationContext(),serviceIntent);
        } else {
            getActivity().getApplicationContext().startService(serviceIntent);
        }
        getActivity().bindService(serviceIntent, this,Context.BIND_AUTO_CREATE);
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
        mButtonPlay=mView.findViewById(R.id.imageButtonPlay);
        mButtonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playService.Play();
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
               playService.Next();
            }
        });
        mButtonPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playService.Prev();
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
                        break;
                    }
                }
                playService.isRepeat=isRepeat;
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
                playService.isShuffle=isShuffle;
            }
        });

        if (playService==null){
            Intent serviceIntent = new Intent(getContext(), PlayService.class);
            serviceIntent.setAction("ru.mrsmile2114.ytmusic.player.action.bind");
            getActivity().bindService(serviceIntent, this,Context.BIND_IMPORTANT);
        } else {
            initFromService();
        }
        return mView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent serviceIntent = new Intent(getContext(), PlayService.class);
        serviceIntent.setAction("ru.mrsmile2114.ytmusic.player.action.close");
        //getActivity().stopService(serviceIntent);
        getActivity().unbindService(this);
        getActivity().getApplicationContext().stopService(serviceIntent);
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

    @Override
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
                        s=s.substring(s.indexOf("=")+1);
                        mListener.SetMainProgressDialogVisible(true);
                        new GetPlaylistItemsAsyncTask(mYoutubeDataApi, mGetPlaylistItemsCallBack)
                                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, s);
                    } else {
                        if(s.contains("youtube.com/watch?v=")){
                            s=s.substring(s.indexOf("=")+1);
                        }else {
                            s=s.substring(s.indexOf("/")+1);
                        }
                        mListener.SetMainProgressDialogVisible(true);
                        new GetVideoDataAsyncTask(mYoutubeDataApi, mGetVideoDataCallBackInterface)
                                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, s);
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

    @Override
    public void onQueueFragmentInteraction(QueueItems.QueueItem item) {
        playService.QueueItemInteraction(item);

    }

    @Override
    public void onQueueFragmentRemove(QueueItems.QueueItem item) {
       playService.QueueItemRemove(item);
    }

    @Override
    public void PressPlay(boolean play) {
        if(play){
            mButtonPlay.setImageResource(R.drawable.ic_media_pause);
        } else {
            mButtonPlay.setImageResource(R.drawable.ic_menu_play);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        playService = ((PlayService.MyBinder)service).getService();
        playService.setCallBack(this);
        initFromService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        playService=null;
    }

    private void initFromService(){
        isPaused=playService.isPaused;
        isRepeat=playService.isRepeat;
        isShuffle=playService.isShuffle;
        PressPlay(playService.isMediaPlayerPlaying());

        playService.setProgressBar(mSeekBar);
        switch (isRepeat){
            case 0: mButtonRepeat.setImageResource(R.drawable.ic_media_repeat); break;
            case 1: mButtonRepeat.setImageResource(R.drawable.ic_media_repeat_one); break;
            case 2: mButtonRepeat.setImageResource(R.drawable.ic_media_repeat_all); break;
        }
        if (isShuffle) {
            mButtonShuffle.setImageResource(R.drawable.ic_media_shuffle_enabled);
        }
    }


    public interface OnFragmentInteractionListener {
        void SetMainProgressDialogVisible(boolean visible);
        void SetTitle(String title);
    }

    private GetVideoDataAsyncTask.GetVideoDataCallBackInterface mGetVideoDataCallBackInterface = new GetVideoDataAsyncTask.GetVideoDataCallBackInterface() {
        @Override
        public void onSuccGetVideoData(VideoListResponse response) {
            mListener.SetMainProgressDialogVisible(false);
            QueueItems.QueueItem item = new QueueItems.QueueItem(
                    response.getItems().get(0).getSnippet().getTitle(),
                    response.getItems().get(0).getId(),
                    response.getItems().get(0).getSnippet().getThumbnails().getMedium().getUrl());
            QueueItems.addItem(item);
            UpdateQueue();
            new YTExtract(getActivity(), mExtractCallBackInterface)
                    .extract(item.getUrl(),140);
        }

        @Override
        public void onUnsuccGetPlaylisItems(int id, String error) {
            mListener.SetMainProgressDialogVisible(false);
            if (id==1){
                Snackbar.make(mView,getString(R.string.video_not_found)+error, Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .show();
            } else {
                Snackbar.make(mView,getString(R.string.api_error)+error, Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .show();
            }

        }
    };

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
            UpdateQueue();
        }

        @Override
        public void onUnsuccExtractTryAgain(int attempt) {
            mListener.SetMainProgressDialogVisible(false);
            Snackbar.make(mView, String.format(
                    getString(R.string.extraction_error_try_again),
                    attempt),
                    Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

        @Override
        public void onUnsuccExtract(String url) {
            mListener.SetMainProgressDialogVisible(false);
            List<QueueItems.QueueItem> Items  = QueueItems.getItemsByUrl(url);
            if(!(Items.isEmpty())){
                for(int i=0;i<Items.size();i++){
                    QueueItems.getITEMS().remove(Items.get(i));
                }
            }
            UpdateQueue();
        }
    };

    private GetPlaylistItemsAsyncTask.GetPlaylistItemsCallBackInterface mGetPlaylistItemsCallBack
            = new GetPlaylistItemsAsyncTask.GetPlaylistItemsCallBackInterface() {
        @Override
        public void onSuccGetPlaylistItems(PlaylistItemListResponse response) {
            mListener.SetMainProgressDialogVisible(false);
            for(int i=0;i<response.getItems().size();i++) {
                QueueItems.QueueItem item = new QueueItems.QueueItem(
                        response.getItems().get(i).getSnippet().getTitle(),
                        response.getItems().get(i).getContentDetails().getVideoId(),
                        response.getItems().get(i).getSnippet().getThumbnails().getMedium().getUrl());
                QueueItems.addItem(item);
                new YTExtract(getActivity(), mExtractCallBackInterface)
                        .extract(item.getUrl(),140);
            }
            UpdateQueue();
        }

        @Override
        public void onUnsuccGetPlaylisItems(String error) {
            mListener.SetMainProgressDialogVisible(false);
            Snackbar.make(mView,getString(R.string.api_error)+error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .show();
        }
    };


}
