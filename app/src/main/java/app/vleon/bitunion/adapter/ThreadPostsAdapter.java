package app.vleon.bitunion.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

import app.vleon.bitunion.PersonalInfoActivity;
import app.vleon.bitunion.R;
import app.vleon.bitunion.buapi.BuAPI;
import app.vleon.bitunion.buapi.BuPost;
import app.vleon.bitunion.ui.CircleTransform;
import app.vleon.bitunion.ui.TextViewFixTouchConsume;
import app.vleon.bitunion.util.GlideImageGetter;
import app.vleon.bitunion.util.HtmlTagHandler;
import app.vleon.bitunion.util.Utils;

public class ThreadPostsAdapter extends UltimateViewAdapter<ThreadPostsAdapter.ViewHolder> implements View.OnClickListener {
    private ArrayList<BuPost> mDataset;
    private Context mContext;

    private OnRecyclerViewItemClickListener mOnItemClickListener = null;
    private OnRecyclerViewItemLongClickListener mOnItemLongClickListener = null;

    public ThreadPostsAdapter(Context context, ArrayList<BuPost> dataset) {
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
//        v.setOnLongClickListener(this);
        return vh;
    }

    @Override
    public ViewHolder getViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position < getItemCount() && (customHeaderView != null ? position <= mDataset.size() : position < mDataset.size()) && (customHeaderView != null ? position > 0 : true)) {
            final BuPost postInfo = (mDataset.get(customHeaderView != null ? position - 1 : position));
            if (postInfo.subject.equals("")) {
                holder.mSubjectTextView.setVisibility(View.GONE);
            } else {
                holder.mSubjectTextView.setVisibility(View.VISIBLE);
            }
            try {
                if (!postInfo.avatar.equals("")) {
                    Glide.with(mContext)
                            .load(postInfo.avatar)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .fitCenter()
                            .placeholder(R.drawable.noavatar)
                            .error(R.drawable.noavatar)
                            .transform(new CircleTransform(mContext))
                            .crossFade()
                            .into(holder.mAvatarImageView);
//                    holder.mAvatarImageView.setImageResource(R.drawable.noavatar);

                } else {
                    holder.mAvatarImageView.setImageResource(R.drawable.noavatar);
                }
                holder.mAvatarImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, PersonalInfoActivity.class);
                        intent.putExtra("uid", postInfo.uid);
                        mContext.startActivity(intent);
                    }
                });
                holder.mAuthorTextView.setText(Html.fromHtml(URLDecoder.decode(postInfo.author, "UTF-8")));
                holder.mSubjectTextView.setText(Html.fromHtml(URLDecoder.decode(postInfo.subject, "UTF-8")));
                holder.mMessageTextView.setLinksClickable(true);
                holder.mMessageTextView.setMovementMethod(TextViewFixTouchConsume.LocalLinkMovementMethod.getInstance());
                holder.mMessageTextView.setText(Utils.getClickableHtml(postInfo.content, null,
//                        new Utils.OnClickedClickableSpanListener() {
//                            @Override
//                            public void onClick(View view, URLSpan urlSpan) {
//                                Toast.makeText(mContext, urlSpan.getURL(), Toast.LENGTH_SHORT).show();
//                            }
//                        },
                        new GlideImageGetter(mContext, holder.mMessageTextView), new HtmlTagHandler(mContext)));

                holder.mQuotesTextView.setLinksClickable(true);
                holder.mQuotesTextView.setMovementMethod(LinkMovementMethod.getInstance());
                if (postInfo.quotes.size() > 0) {
                    holder.mQuotesTextView.setVisibility(View.VISIBLE);
                    // 显示引用信息部分
                    String quoteString = "";
                    BuPost.Quote tmpQuote;
                    for (int i = 0; i < postInfo.quotes.size(); i++) {
                        tmpQuote = postInfo.quotes.get(i);
                        if (i > 0)
                            quoteString += "<br/><br/>";
                        quoteString += tmpQuote.author + ":&nbsp;" + tmpQuote.content;
                    }
                    holder.mQuotesTextView.setText(Html.fromHtml(quoteString, new GlideImageGetter(mContext, holder.mQuotesTextView), null));
                } else {
                    holder.mQuotesTextView.setVisibility(View.GONE);
                }


            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                holder.mSubjectTextView.setText(R.string.encode_error);
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

    /**
     * 长按事件监听
     */
    public void setOnItemLongClickedListener(OnRecyclerViewItemLongClickListener listener) {
        this.mOnItemLongClickListener = listener;
    }

    /**
     * 实现view的单击事件接口
     */
    @Override
    public void onClick(View view) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(view, (BuPost) view.getTag());
        }
    }

    /**
     * 实现view的长按事件接口
     */
//    @Override
//    public boolean onLongClick(View v) {
//        if (mOnItemLongClickListener != null) {
//            mOnItemLongClickListener.onItemLongClick(v, (BuPost) v.getTag());
//        }return true;
//    }

    public void refresh(ArrayList<BuPost> dataset) {
        mDataset = dataset;
        this.notifyDataSetChanged();
    }
    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, BuPost data);
    }

    public interface OnRecyclerViewItemLongClickListener {
        void onItemLongClick(View view, BuPost data);
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mAuthorTextView;
        public TextView mSubjectTextView;
        public TextViewFixTouchConsume mQuotesTextView;
        public TextView mTimeTextView;
        public TextViewFixTouchConsume mMessageTextView;
        public ImageView mAvatarImageView;

        public ViewHolder(View v) {
            super(v);
            mAvatarImageView = (ImageView) v.findViewById(R.id.avatar_imageview);
            mAuthorTextView = (TextView) v.findViewById(R.id.author_textview);
            mSubjectTextView = (TextView) v.findViewById(R.id.subject_textview);
            mQuotesTextView = (TextViewFixTouchConsume) v.findViewById(R.id.quotes_textview);
            mTimeTextView = (TextView) v.findViewById(R.id.time_textview);
            mMessageTextView = (TextViewFixTouchConsume) v.findViewById(R.id.message_textview);
        }
    }
}
