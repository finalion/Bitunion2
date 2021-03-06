package app.vleon.bitunion.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

import app.vleon.bitunion.R;
import app.vleon.bitunion.buapi.BuAPI;
import app.vleon.bitunion.buapi.BuThread;

public class ThreadsAdapter extends UltimateViewAdapter<ThreadsAdapter.ViewHolder> {
    private ArrayList<BuThread> mDataset;

    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    public ThreadsAdapter(ArrayList<BuThread> dataset) {
        mDataset = dataset;
    }

    /**
     * 单击事件监听
     */
    public void setOnItemClickedListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.thread_item, parent, false);

        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v, true);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(v, (BuThread) v.getTag());
                }
            }
        });
        return vh;
    }

    @Override
    public ViewHolder getViewHolder(View view) {
        return new ViewHolder(view, false);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position < getItemCount() && (customHeaderView != null ? position <= mDataset.size() : position < mDataset.size()) && (customHeaderView != null ? position > 0 : true)) {
            BuThread thread = (mDataset.get(customHeaderView != null ? position - 1 : position));
            try {
                holder.mAuthorTextView.setText(Html.fromHtml(URLDecoder.decode(thread.author, "UTF-8")));
                holder.mSubjectTextView.setText(Html.fromHtml(URLDecoder.decode(thread.subject, "UTF-8")));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                holder.mSubjectTextView.setText("encode error");
            }
            holder.mTimeTextView.setText(BuAPI.formatTime(thread.lastpost));
            holder.mCommentsCountTextView.setText(thread.replies);
            holder.mViewsCountTextView.setText(thread.views);
            holder.itemView.setTag(thread); //将当前帖子设置为itemview的tag，方便点击事件回调
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
        return null;
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder, int i) {

    }

    @Override
    public int getAdapterItemCount() {
        return mDataset == null ? 0 : mDataset.size();
    }

    @Override
    public long generateHeaderId(int i) {
        return 0;
    }

    public void refresh(ArrayList<BuThread> dataset) {
        mDataset = dataset;
        this.notifyDataSetChanged();
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, BuThread data);
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends UltimateRecyclerviewViewHolder {
        // each data item is just a string in this case
        public TextView mAuthorTextView;
        public TextView mSubjectTextView;
        public TextView mTimeTextView;
        public TextView mViewsCountTextView;
        public TextView mCommentsCountTextView;

        public ViewHolder(View v, boolean isItem) {
            super(v);
            if (isItem) {
                mAuthorTextView = (TextView) v.findViewById(R.id.author_textview);
                mSubjectTextView = (TextView) v.findViewById(R.id.subject_textview);
                mTimeTextView = (TextView) v.findViewById(R.id.time_textview);
                mViewsCountTextView = (TextView) v.findViewById(R.id.views_count_textview);
                mCommentsCountTextView = (TextView) v.findViewById(R.id.comments_count_textview);
            }
        }
    }
}
