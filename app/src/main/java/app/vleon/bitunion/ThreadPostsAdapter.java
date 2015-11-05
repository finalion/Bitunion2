package app.vleon.bitunion;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

import app.vleon.buapi.BuAPI;
import app.vleon.util.GlideImageGetter;

public class ThreadPostsAdapter extends UltimateViewAdapter<ThreadPostsAdapter.ViewHolder> implements View.OnClickListener {
    private ArrayList<BuAPI.PostInfo> mDataset;
    private Context mContext;

    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, BuAPI.PostInfo data);
    }

    public ThreadPostsAdapter(Context context, ArrayList<BuAPI.PostInfo> dataset) {
        mDataset = dataset;
        mContext = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        v.setOnClickListener(this);
        return vh;
    }

    @Override
    public ViewHolder getViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position < getItemCount() && (customHeaderView != null ? position <= mDataset.size() : position < mDataset.size()) && (customHeaderView != null ? position > 0 : true)) {
            BuAPI.PostInfo postInfo = (mDataset.get(customHeaderView != null ? position - 1 : position));
            if (postInfo.subject.equals("")) {
                holder.mSubjectTextView.setVisibility(View.GONE);
            } else {
                holder.mSubjectTextView.setVisibility(View.VISIBLE);
            }
            try {
                if (!postInfo.avatar.equals("")) {
                    Glide.with(mContext).load(postInfo.trueAvatar).fitCenter().into(holder.mAvatarImageView);
//                    holder.mAvatarImageView.setImageResource(R.drawable.noavatar);

                } else {
                    holder.mAvatarImageView.setImageResource(R.drawable.noavatar);
                }
                holder.mAuthorTextView.setText(Html.fromHtml(URLDecoder.decode(postInfo.author, "UTF-8")));
                holder.mSubjectTextView.setText(Html.fromHtml(URLDecoder.decode(postInfo.subject, "UTF-8")));
                holder.mMessageTextView.setText(Html.fromHtml(postInfo.content, new GlideImageGetter(mContext, holder.mMessageTextView), null));

                if (postInfo.quotes.size() > 0) {
                    holder.mQuotesTextView.setVisibility(View.VISIBLE);
                    // 显示引用信息部分
                    String quoteString = "";
                    BuAPI.Quote tmpQuote;
                    for (int i = 0; i < postInfo.quotes.size(); i++) {
                        tmpQuote = postInfo.quotes.get(i);
                        if (i > 0)
                            quoteString += "<br/><br/>";
                        quoteString += tmpQuote.quoteAuthor + ":&nbsp;" + tmpQuote.quoteContent;
                    }
                    holder.mQuotesTextView.setText(Html.fromHtml(quoteString, new GlideImageGetter(mContext, holder.mQuotesTextView), null));
                } else {
                    holder.mQuotesTextView.setVisibility(View.GONE);
                }


            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                holder.mSubjectTextView.setText("encode error");
            }
            holder.mTimeTextView.setText(BuAPI.formatTime(postInfo.lastedit));
            holder.itemView.setTag(postInfo); //将当前帖子设置为itemview的tag，方便点击事件回调

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

    /**
     * 单击事件监听
     */
    public void setOnItemClickedListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mAuthorTextView;
        public TextView mSubjectTextView;
        public TextView mQuotesTextView;
        public TextView mTimeTextView;
        public TextView mMessageTextView;
        public ImageView mAvatarImageView;

        public ViewHolder(View v) {
            super(v);
            mAvatarImageView = (ImageView) v.findViewById(R.id.avatar_imageview);
            mAuthorTextView = (TextView) v.findViewById(R.id.author_textview);
            mSubjectTextView = (TextView) v.findViewById(R.id.subject_textview);
            mQuotesTextView = (TextView) v.findViewById(R.id.quotes_textview);
            mTimeTextView = (TextView) v.findViewById(R.id.time_textview);
            mMessageTextView = (TextView) v.findViewById(R.id.message_textview);
        }
    }

    /**
     * 实现view的单击事件接口
     */
    @Override
    public void onClick(View view) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(view, (BuAPI.PostInfo) view.getTag());
        }
    }

    public void refresh(ArrayList<BuAPI.PostInfo> dataset) {
        mDataset = dataset;
        this.notifyDataSetChanged();
    }
}
