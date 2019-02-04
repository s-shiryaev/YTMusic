package ru.mrsmile2114.ytmusic;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import ru.mrsmile2114.ytmusic.PlaylistItems.PlaylistItem;

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
            Snackbar.make(v, "ALMOST DONE!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    };

    public final CheckBox.OnCheckedChangeListener CheckBoxListener = new CheckBox.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                recyclerViewAdapter.setAllChecked(isChecked);
                if (isChecked) {
                   System.out.println("CLICK CHECK 0->*");
               } else {
                   System.out.println("CLICK CHECK *->0");
               }
            }
        };
}
