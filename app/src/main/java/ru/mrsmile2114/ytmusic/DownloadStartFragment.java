package ru.mrsmile2114.ytmusic;

import android.content.Context;
import android.os.AsyncTask;
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
                .setApplicationName(getResources().getString(R.string.app_name))
                .build();
        mListener.SetMainFabListener(FabList);
        mListener.SetMainFabImage(R.drawable.ic_menu_search);
        mListener.SetMainFabVisible(true);
        mListener.SetTitle(getResources().getString(R.string.action_download));
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
        void StartAsyncYtExtraction(String url);
        void SetTitle(String title);
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
                    new GetPlaylistItemsAsyncTask(mYoutubeDataApi,(MainActivity)getActivity()).execute(s);
                } else {
                    if (mListener.HaveStoragePermission()){
                        mListener.SetMainProgressDialogVisible(true);
                        mListener.StartAsyncYtExtraction(s);
                    }
                }
            }else {
                Snackbar.make(mView, "Please insert correct link to the playlist/video!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }

        }
    };

}
