package app.vleon.bitunion;

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

import app.vleon.bitunion.buapi.BuLatestThread;

/**
 * Created by vleon on 2016/1/20.
 */
public class LatestThreadsAdapter extends UltimateViewAdapter<LatestThreadsAdapter.ViewHolder> {
    private ArrayList<BuLatestThread> mDataset;

    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    public LatestThreadsAdapter(ArrayList<BuLatestThread> dataset) {
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.latest_thread_item, parent, false);

        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v, true);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(v, (BuLatestThread) v.getTag());
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
            BuLatestThread thread = (mDataset.get(customHeaderView != null ? position - 1 : position));
            try {
                holder.mAuthorTextView.setText(Html.fromHtml(URLDecoder.decode(thread.getAuthor(), "UTF-8")));
                holder.mSubjectTextView.setText(Html.fromHtml(URLDecoder.decode(thread.getPname(), "UTF-8")));
                holder.mForumTextView.setText(Html.fromHtml(URLDecoder.decode(thread.getFname(), "UTF-8")));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                holder.mAuthorTextView.setText(R.string.encode_error);
                holder.mSubjectTextView.setText(R.string.encode_error);
                holder.mForumTextView.setText(R.string.encode_error);
            }
            holder.mTidTotalCounts.setText(thread.getTid_sum());
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

    public void refresh(ArrayList<BuLatestThread> dataset) {
        mDataset = dataset;
        this.notifyDataSetChanged();
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, BuLatestThread data);
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends UltimateRecyclerviewViewHolder {
        // each data item is just a string in this case
        public TextView mAuthorTextView;
        public TextView mSubjectTextView;
        public TextView mForumTextView;
        public TextView mTidTotalCounts;

        public ViewHolder(View v, boolean isItem) {
            super(v);
            if (isItem) {
                mAuthorTextView = (TextView) v.findViewById(R.id.author_textview);
                mSubjectTextView = (TextView) v.findViewById(R.id.subject_textview);
                mForumTextView = (TextView) v.findViewById(R.id.forum_in_textview);
                mTidTotalCounts = (TextView) v.findViewById(R.id.counts_textview);
            }
        }
    }
}
