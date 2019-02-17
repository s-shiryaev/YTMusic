package ru.mrsmile2114.ytmusic.player;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.mrsmile2114.ytmusic.R;
import ru.mrsmile2114.ytmusic.dummy.QueueItems;
import ru.mrsmile2114.ytmusic.dummy.QueueItems.QueueItem;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class QueueItemFragment extends Fragment {

    private QueueItemRecyclerViewAdapter mAdapter;
    private OnListFragmentInteractionListener mListener;
    private OnParentFragmentInteractionListener mParentListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public QueueItemFragment() {
    }

    @SuppressWarnings("unused")
    public static QueueItemFragment newInstance(int columnCount) {
        QueueItemFragment fragment = new QueueItemFragment();
        Bundle args = new Bundle();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onAttachToParentFragment(getParentFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_queueitem_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView mRecyclerView = (RecyclerView) view;
            mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            mAdapter = new QueueItemRecyclerViewAdapter(QueueItems.getITEMS(), mListener, mParentListener);
            mRecyclerView.setAdapter(mAdapter);
        }
        return view;
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

    public void onAttachToParentFragment(Fragment fragment)
    {
        try
        {
            mParentListener = (OnParentFragmentInteractionListener) fragment;

        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(
                    fragment.toString() + " must implement OnParentFragmentInteractionListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mParentListener=null;
    }

    public void RefreshRecyclerView(){
        mAdapter.notifyDataSetChanged();
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
    }
    public interface OnParentFragmentInteractionListener{
        void onQueueFragmentInteraction(QueueItem item);
        void onQueueFragmentRemove(QueueItem item);
    }
}
