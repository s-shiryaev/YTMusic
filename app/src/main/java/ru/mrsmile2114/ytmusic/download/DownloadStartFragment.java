package ru.mrsmile2114.ytmusic.download;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItemListResponse;

import ru.mrsmile2114.ytmusic.MainActivity;
import ru.mrsmile2114.ytmusic.R;
import ru.mrsmile2114.ytmusic.dummy.PlaylistItems;
import ru.mrsmile2114.ytmusic.utils.GetPlaylistItemsAsyncTask;
import ru.mrsmile2114.ytmusic.utils.YTExtract;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DownloadStartFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DownloadStartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DownloadStartFragment extends Fragment {

    private EditText editText;
    private View mView;
    private YouTube mYoutubeDataApi;

    private final GsonFactory mJsonFactory = new GsonFactory();
    private final HttpTransport mTransport = AndroidHttp.newCompatibleTransport();

    private OnFragmentInteractionListener mListener;

    public DownloadStartFragment() {
        // Required empty public constructor
    }


    public static DownloadStartFragment newInstance() {//add parameter if need
        DownloadStartFragment fragment = new DownloadStartFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_download_start,container,false);
        editText = mView.findViewById(R.id.editText);
        mYoutubeDataApi = new YouTube.Builder(mTransport, mJsonFactory, null)
                .setApplicationName(getString(R.string.app_name))
                .build();
        mListener.SetMainFabListener(FabList);
        mListener.SetMainFabImage(R.drawable.ic_menu_search);
        mListener.SetMainFabVisible(true);
        mListener.SetTitle(getString(R.string.action_download));
        return mView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mView = null;
        editText = null;
        mYoutubeDataApi = null;
        mListener.SetMainFabVisible(false);
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {

        void SetMainFabVisible(boolean visible);
        void SetMainFabImage(int imageResource);
        void SetMainFabListener(View.OnClickListener listener);
        void SetMainProgressDialogVisible(boolean visible);
        void SetTitle(String title);
        long downloadFromUrl(String youtubeDlUrl, String downloadTitle, String fileName, boolean hide);
        boolean HaveStoragePermission();
    }


    public final View.OnClickListener FabList = new View.OnClickListener() {
        @Override
        public void onClick(View v) {//fab button click
            String s = editText.getText().toString();
            if ((s.length()!=0)&&(s.contains("youtube.com/watch?v=")||s.contains("www.youtube.com/playlist?list=")||s.contains("youtu.be/"))){
                if (s.contains("www.youtube.com/playlist?list=")){
                    s=s.copyValueOf(s.toCharArray(),s.indexOf("=")+1,s.length()-s.indexOf("=")-1);//get playlist id
                    //STARTING REQUEST TO API
                    mListener.SetMainProgressDialogVisible(true);
                    new GetPlaylistItemsAsyncTask(mYoutubeDataApi,
                            mGetPlaylistItemsCallBackInterface).execute(s);
                } else {
                    if (mListener.HaveStoragePermission()){
                        mListener.SetMainProgressDialogVisible(true);
                        new YTExtract(getActivity(),s,1,6,
                                ((MainActivity)getActivity()).DownloadExtractCallBackInterface)
                                .execute(s);
                    }
                }
            }else {
                Snackbar.make(mView, getString(R.string.incorrect_url), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }

        }
    };

    private GetPlaylistItemsAsyncTask.GetPlaylistItemsCallBackInterface mGetPlaylistItemsCallBackInterface
            = new GetPlaylistItemsAsyncTask.GetPlaylistItemsCallBackInterface() {
        @Override
        public void onSuccGetPlaylistItems(PlaylistItemListResponse response) {
                mListener.SetMainProgressDialogVisible(false);
                PlaylistItems.clearItems();//delete data
                for(int i=0;i<response.getItems().size();i++) {
                    PlaylistItems.addItem(PlaylistItems.createDummyItem(
                            response.getItems().get(i).getSnippet().getTitle(),
                            response.getItems().get(i).getContentDetails().getVideoId(),
                            response.getItems().get(i).getSnippet().getThumbnails().getMedium().getUrl()));
                }
            ((MainActivity)getActivity()).GoToFragment(PlaylistItemsFragment.class);
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
