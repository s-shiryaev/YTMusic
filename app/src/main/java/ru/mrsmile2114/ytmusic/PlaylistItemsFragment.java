package ru.mrsmile2114.ytmusic;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.List;

import ru.mrsmile2114.ytmusic.dummy.PlaylistItems;
import ru.mrsmile2114.ytmusic.dummy.PlaylistItems.PlaylistItem;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class PlaylistItemsFragment extends Fragment {

    private OnListFragmentInteractionListener mListener;
    private PlaylistItemsRecyclerViewAdapter recyclerViewAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PlaylistItemsFragment() {
    }


    @SuppressWarnings("unused")
    public static PlaylistItemsFragment newInstance() {
        PlaylistItemsFragment fragment = new PlaylistItemsFragment();
        //Bundle args = new Bundle();
        //args.putInt(ARG_COLUMN_COUNT, columnCount);
        //fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_playlist_item_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerViewAdapter = new PlaylistItemsRecyclerViewAdapter(PlaylistItems.getITEMS(), mListener);
            recyclerView.setAdapter(recyclerViewAdapter);
        }
        ((MainActivity)getActivity()).SetMainFabListener(FabList);
        ((MainActivity)getActivity()).SetMainFabImage(R.drawable.ic_menu_download);
        ((MainActivity)getActivity()).SetMainFabVisible(true);
        ((MainActivity)getActivity()).SetMainCheckBoxVisible(true);
        ((MainActivity)getActivity()).SetMainCheckBoxListener(CheckBoxListener);
        //System.out.println("SUCCESSSSS");//TODO:DELETE
        ((MainActivity)getActivity()).setTitle("Select videos to download");
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerViewAdapter=null;
        ((MainActivity)getActivity()).SetMainCheckBoxVisible(false);
        ((MainActivity)getActivity()).SetMainFabVisible(false);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void RefreshRecyclerView() {
        recyclerViewAdapter.notifyDataSetChanged();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(PlaylistItem item);
    }

    public final View.OnClickListener FabList = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (haveStoragePermission()) {
                ((MainActivity)getActivity()).setMainProgressDialogVisible(true);
                List<PlaylistItem> CHECKEDITEMS = PlaylistItems.getCheckedItems();
                for (int i = 0; i < CHECKEDITEMS.size(); i++) {
                    ((MainActivity)getActivity()).StartAsyncYtExtraction(CHECKEDITEMS.get(i).getUrl());
                }
            }
        }
    };



    public final CheckBox.OnCheckedChangeListener CheckBoxListener = new CheckBox.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PlaylistItems.setAllChecked(isChecked);
                recyclerViewAdapter.notifyDataSetChanged();
            }
        };

    public  boolean haveStoragePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.e("Permission error","You have permission");
                return true;
            } else {
                Log.e("Permission error","You have asked for permission");
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else {
            Log.e("Permission error","You already have the permission");
            return true;
        }
    }

}
