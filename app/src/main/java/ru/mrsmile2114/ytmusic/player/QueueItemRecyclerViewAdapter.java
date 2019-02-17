package ru.mrsmile2114.ytmusic.player;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManagerNonConfig;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import ru.mrsmile2114.ytmusic.R;
import ru.mrsmile2114.ytmusic.player.QueueItemFragment.OnListFragmentInteractionListener;
import ru.mrsmile2114.ytmusic.dummy.QueueItems.QueueItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link QueueItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class QueueItemRecyclerViewAdapter extends RecyclerView.Adapter<QueueItemRecyclerViewAdapter.ViewHolder> {

    private final List<QueueItem> mValues;
    private final OnListFragmentInteractionListener mListener;
    private final QueueItemFragment.OnParentFragmentInteractionListener mParentListener;

    public QueueItemRecyclerViewAdapter(List<QueueItem> items,
                                        OnListFragmentInteractionListener listener,
                                        QueueItemFragment.OnParentFragmentInteractionListener parentListener) {
        mValues = items;
        mListener = listener;
        mParentListener = parentListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_queueitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mTitle.setText(mValues.get(position).getTitle());
        if (holder.mItem.isPlaying()){
            holder.mImageView.setVisibility(View.VISIBLE);
        } else{
            holder.mImageView.setVisibility(View.INVISIBLE);
        }
        if (holder.mItem.isExtracting()){
            holder.mProgressBar.setVisibility(View.VISIBLE);
        } else {
            holder.mProgressBar.setVisibility(View.INVISIBLE);
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mParentListener) {
                    mParentListener.onQueueFragmentInteraction(holder.mItem);
                }
            }
        });
        holder.mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mParentListener) {
                    mParentListener.onQueueFragmentRemove(holder.mItem);
                } else {
                    System.out.println("NULL");
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
        private TextView mTitle;
        private QueueItem mItem;
        private ProgressBar mProgressBar;
        private ImageView mImageView;
        private ImageButton mImageButton;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTitle = (TextView) view.findViewById(R.id.titleQueue);
            mProgressBar = view.findViewById(R.id.progressBarQueue);
            mImageView = view.findViewById(R.id.imageView2);
            mImageButton = view.findViewById(R.id.imageButtonRemove);
        }
    }
}
