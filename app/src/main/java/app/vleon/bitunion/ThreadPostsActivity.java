package app.vleon.bitunion;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import java.util.ArrayList;

import app.vleon.bitunion.adapter.ThreadPostsAdapter;
import app.vleon.bitunion.buapi.BuAPI;
import app.vleon.bitunion.buapi.BuPost;
import app.vleon.bitunion.fragment.PostDialogFragment;
import app.vleon.bitunion.ui.DividerItemDecoration;

public class ThreadPostsActivity extends AppCompatActivity implements BuAPI.OnPostsResponseListener, BuAPI.OnPostNewReplyResponseListener {

    MyApplication app;
    ArrayList<BuPost> mPostsList;
    int mFrom = 0;
    int mTo = 20;
    int mTid = 0;
    FloatingActionButton mFloatingButton;
    private UltimateRecyclerView mPostsRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ThreadPostsAdapter mAdapter;
    private boolean clearFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread_posts);
        app = (MyApplication) getApplicationContext();
        mPostsList = new ArrayList<>();

        //设置toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.my_toolbar2);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPostsRecyclerView = (UltimateRecyclerView) findViewById(R.id.posts_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mPostsRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mPostsRecyclerView.setLayoutManager(mLayoutManager);

        mFloatingButton = (FloatingActionButton) findViewById(R.id.custom_urv_add_floating_button_reply);
        mFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PostDialogFragment pdf = new PostDialogFragment();
                pdf.setLaunchType(1); // hide subject textview
                pdf.show(getSupportFragmentManager(), "reply_dialog");
            }
        });

        // specify an adapter (see also next example)
        mAdapter = new ThreadPostsAdapter(this, mPostsList);
        mAdapter.setOnItemClickedListener(new ThreadPostsAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, BuPost data) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ThreadPostsActivity.this);
                final BuPost postInfo = data;
                builder.setItems(new String[]{"@作者", "回复帖子"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String preMessage = "";
                        switch (which) {
                            case 0:
                                preMessage = String.format("[@]%s[/@]", postInfo.author);
                                break;
                            case 1:
                                String shortPre = postInfo.content;
                                if (postInfo.content.length() > 100) {
                                    shortPre = shortPre.substring(0, 100) + " ... ";
                                }
                                preMessage = String.format("[quote=%s][b]%s[/b] %s\n%s[/quote]",
                                        postInfo.pid, postInfo.author, postInfo.lastedit, shortPre);
                                break;
                            default:
                                break;
                        }
                        PostDialogFragment pdf = new PostDialogFragment();
                        pdf.setLaunchType(1); // hide subject textview
                        pdf.setPreMessage(preMessage);
                        pdf.show(getSupportFragmentManager(), "reply_dialog");
                    }
                }).show();
            }
        });
        mPostsRecyclerView.setAdapter(mAdapter);
        mPostsRecyclerView.addItemDecoration(new DividerItemDecoration(this, null));
        mPostsRecyclerView.enableLoadmore();
        mPostsRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, int maxLastVisiblePosition) {
                loadMoreData();
            }
        });
        mPostsRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });
        // 设置滚动条颜色变化
        mPostsRecyclerView.setDefaultSwipeToRefreshColorScheme(R.color.colorAccent);
        mAdapter.setCustomLoadMoreView(LayoutInflater.from(this).inflate(R.layout.load_more, null));

        app.getAPI().setOnPostsResponseListener(this);
        app.getAPI().setOnPostNewReplyResponseListener(this);

        Intent intent = getIntent();
        String tid = intent.getStringExtra("tid");
        if (tid != null) {
            mTid = Integer.parseInt(tid);
            showRefreshingProgress(true);
            app.getAPI().getThreadPosts(mTid, mFrom, mTo);
        } else {
            Toast.makeText(this, "获取帖子ID失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 显示SwipeRefreshLayout的进度指示，bug，不能直接调用setRefreshing
     * https://github.com/cymcsg/UltimateRecyclerView/issues/192
     *
     * @param show whether to show indicator
     */
    public void showRefreshingProgress(final boolean show) {
        mPostsRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                mPostsRecyclerView.setRefreshing(show);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_thread_posts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            refreshData();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void handlePostsGetterResponse(BuAPI.Result result, ArrayList<BuPost> postsList, int replyCount) {
        showRefreshingProgress(false);
        if (clearFlag) {
            mPostsList.clear();
        }
        switch (result) {
            case SUCCESS:
                mPostsList.addAll(postsList);
                mAdapter.refresh(mPostsList);
                // 如果当前回复数目大于replyCount,禁用上拉刷新
                if (mPostsList.size() - 1 >= replyCount) {
                    mPostsRecyclerView.disableLoadmore();
                }
                break;
            case SUCCESS_EMPTY:
                mPostsRecyclerView.disableLoadmore();
                break;

            default:
                break;
        }
    }

    @Override
    public void handlePostsGetterErrorResponse(VolleyError error) {
        showRefreshingProgress(false);
        Toast.makeText(ThreadPostsActivity.this, "获取异常", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void handlePostNewReplyResponse(BuAPI.Result result, String pid) {
        refreshData();
        Toast.makeText(this, "回复成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void handlePostNewReplyErrorResponse(VolleyError error) {
        Toast.makeText(this, "回复失败", Toast.LENGTH_SHORT).show();
    }

    public void loadMoreData() {
        clearFlag = false;
        mFrom = mTo;
        mTo = mFrom + 20;
        app.getAPI().getThreadPosts(mTid, mFrom, mTo);
    }

    public void refreshData() {
        clearFlag = true;
        mFrom = 0;
        mTo = 20;
        app.getAPI().getThreadPosts(mTid, mFrom, mTo);
    }

}



