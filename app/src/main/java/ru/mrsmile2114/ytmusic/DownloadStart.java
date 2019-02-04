package ru.mrsmile2114.ytmusic;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
 * {@link DownloadStart.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DownloadStart#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DownloadStart extends Fragment {

    private EditText editText;
    private View mView;
    private YouTube mYoutubeDataApi;
    private ProgressDialog progressDialog;
    private AsyncTask<String, Void, PlaylistItemListResponse> task;

    private final GsonFactory mJsonFactory = new GsonFactory();
    private final HttpTransport mTransport = AndroidHttp.newCompatibleTransport();

    private OnFragmentInteractionListener mListener;

    public DownloadStart() {
        // Required empty public constructor
    }


    public static DownloadStart newInstance() {//add parameter if need
        DownloadStart fragment = new DownloadStart();
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
        progressDialog = new ProgressDialog(getActivity());
        ((MainActivity)getActivity()).SetMainFabListener(FabList);
        ((MainActivity)getActivity()).SetMainFabImage(R.drawable.ic_menu_search);
        ((MainActivity)getActivity()).SetMainFabVisible(true);
        ((MainActivity)getActivity()).setTitle(R.string.nav_header_title);
        return mView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mView = null;
        editText = null;
        task = null;
        progressDialog = null;
        mYoutubeDataApi = null;
        ((MainActivity)getActivity()).SetMainFabVisible(false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void FillDownloadContent(PlaylistItemListResponse response) {

        for(int i=0;i<response.getItems().size();i++){
            //System.out.println("FILL ITEM "+i);
            PlaylistItems.addItem(PlaylistItems.createDummyItem(i,
                    response.getItems().get(i).getSnippet().getTitle(),
                    response.getItems().get(i).getContentDetails().getVideoId(),
                    response.getItems().get(i).getSnippet().getThumbnails().getMedium().getUrl()));
        }
    }

    public final View.OnClickListener FabList = new View.OnClickListener() {
        @Override
        public void onClick(View v) {//fab button click
            String s = editText.getText().toString();
            if ((s.length()!=0)&&(s.contains("www.youtube.com/watch?v=")||s.contains("www.youtube.com/playlist?list=")||s.contains("youtu.be/"))){
                if (s.contains("www.youtube.com/playlist?list=")){
                    s=s.copyValueOf(s.toCharArray(),s.indexOf("=")+1,s.length()-s.indexOf("=")-1);//get playlist id
                    //STARTING REQUEST TO API
                    task = new GetPlaylistItemsAsyncTask(mYoutubeDataApi,progressDialog,(MainActivity)getActivity()).execute(s);//no memory leak
                }
            }else {
                Snackbar.make(mView, "Please insert correct link to the playlist/video!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }

        }
    };

}
