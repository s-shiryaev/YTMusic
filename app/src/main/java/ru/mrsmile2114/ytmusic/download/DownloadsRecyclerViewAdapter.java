package ru.mrsmile2114.ytmusic.download;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import ru.mrsmile2114.ytmusic.R;
import ru.mrsmile2114.ytmusic.download.DownloadsFragment.OnListFragmentInteractionListener;
import ru.mrsmile2114.ytmusic.dummy.DownloadsItems.DownloadsItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DownloadsItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 *
 */
public class DownloadsRecyclerViewAdapter extends RecyclerView.Adapter<DownloadsRecyclerViewAdapter.ViewHolder> {

    private final List<DownloadsItem> mValues;
    private final OnListFragmentInteractionListener mListener;

    public DownloadsRecyclerViewAdapter(List<DownloadsItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_downloads, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mContentView.setText(mValues.get(position).getTitle());
//        holder.mView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (null != mListener) {
//                    // Notify the active callbacks interface (the activity, if the
//                    // fragment is attached to one) that an item has been selected.
//                    mListener.onListFragmentInteraction(holder.mItem);
//                }
//            }
//        });
        holder.mButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.RemoveItemByDownloadId(holder.mItem.getDownloadId(),false);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final View mView;
        private final TextView mContentView;
        private final ImageButton mButton;
        private DownloadsItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mContentView = (TextView) view.findViewById(R.id.content);
            mButton = (ImageButton) view.findViewById(R.id.imageButton);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
