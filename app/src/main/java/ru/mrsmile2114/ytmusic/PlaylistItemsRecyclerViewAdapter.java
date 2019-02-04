package ru.mrsmile2114.ytmusic;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import ru.mrsmile2114.ytmusic.PlaylistItemsFragment.OnListFragmentInteractionListener;
import ru.mrsmile2114.ytmusic.dummy.PlaylistItems.PlaylistItem;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PlaylistItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class PlaylistItemsRecyclerViewAdapter extends RecyclerView.Adapter<PlaylistItemsRecyclerViewAdapter.ViewHolder> {

    private final List<PlaylistItem> mValues;
    private final OnListFragmentInteractionListener mListener;
    private List<CheckBox> mCheckboxList = new ArrayList<CheckBox>();

    public PlaylistItemsRecyclerViewAdapter(List<PlaylistItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_playlist_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mItem = mValues.get(position);
        //holder.mIdView.setText(mValues.get(position).id);
        holder.mContentView.setText(mValues.get(position).getTitle());
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
        Picasso.get().load(mValues.get(position).getThumbnail()).into(holder.mImageView);
        holder.mCheckBox.setChecked(mValues.get(position).getChecked());//fill checkbox
        holder.mCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mValues.get(position).setChecked(isChecked);
                //System.out.println(position+" CHANGED ON TRUE");//TODO:DELETE ON RELEASE
            }
        });
        mCheckboxList.add(holder.mCheckBox);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }


    public void setAllChecked(boolean checked){
        for(int i=0;i<getItemCount();i++){
        mCheckboxList.get(i).setChecked(checked);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public final View mView;
        //public TextView mIdView;
        public TextView mContentView;
        public CheckBox mCheckBox;
        public PlaylistItem mItem;
        public ImageView mImageView;


        public ViewHolder(View view) {
            super(view);
            mView = view;
            //mIdView = (TextView) view.findViewById(R.id.item_number);
            mContentView = (TextView) view.findViewById(R.id.content);
            mCheckBox = (CheckBox) view.findViewById(R.id.checkBox);
            mImageView = (ImageView) view.findViewById(R.id.imageView);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }


    }
}
