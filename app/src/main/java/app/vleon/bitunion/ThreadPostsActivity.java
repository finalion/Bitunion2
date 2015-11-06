package app.vleon.bitunion;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import java.util.ArrayList;

import app.vleon.buapi.BuAPI;
import app.vleon.ui.DividerItemDecoration;

public class ThreadPostsActivity extends Activity implements BuAPI.OnPostsResponseListener {

    public static RequestQueue mRequestQueue;
    public static BuAPI.LoginInfo mLoginInfo;

    public BuAPI mAPI;
    private UltimateRecyclerView mPostsRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ThreadPostsAdapter mAdapter;
    ArrayList<BuAPI.PostInfo> mPostsList;
    int mFrom = 0;
    int mTo = 20;
    int mTid = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread_posts);

        mAPI = LoginActivity.mAPI;
        mPostsList = new ArrayList<>();

        mPostsRecyclerView = (UltimateRecyclerView) findViewById(R.id.posts_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mPostsRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mPostsRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new ThreadPostsAdapter(this, mPostsList);
        mPostsRecyclerView.setAdapter(mAdapter);
        mPostsRecyclerView.addItemDecoration(new DividerItemDecoration(this, null));

        Intent intent = getIntent();
        String tid = intent.getStringExtra("tid");
        if (tid != null) {
            mTid = Integer.parseInt(tid);
            mAPI.getThreadPosts(mTid, mFrom, mTo);
        } else {
            Toast.makeText(this, "获取帖子ID失败", Toast.LENGTH_SHORT).show();
        }

        mPostsRecyclerView.enableLoadmore();
        mPostsRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, int maxLastVisiblePosition) {
                mFrom = mTo + 1;
                mTo = mFrom + 20;
                mAPI.getThreadPosts(mTid, mFrom, mTo);
            }
        });
        mPostsRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPostsList.clear();
                mFrom = 0;
                mTo = 20;
                mAPI.getThreadPosts(mTid, mFrom, mTo);
            }
        });
        mAdapter.setCustomLoadMoreView(LayoutInflater.from(this).inflate(R.layout.load_more, null));
        mAPI.setOnPostsResponseListener(this);
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
            mPostsList.clear();
            mFrom = 0;
            mTo = 20;
            mAPI.getThreadPosts(mTid, mFrom, mTo);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void handlePostsGetterResponse(BuAPI.Result result, ArrayList<BuAPI.PostInfo> postsList) {
        switch (result) {
            case SUCCESS:
                mPostsList.addAll(postsList);
                mAdapter.refresh(mPostsList);
                if (postsList.size() < 20) {
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
        Toast.makeText(ThreadPostsActivity.this, "获取异常", Toast.LENGTH_SHORT).show();
    }
}



