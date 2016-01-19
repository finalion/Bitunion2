package app.vleon.bitunion;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import java.util.ArrayList;

import app.vleon.buapi.BuAPI;
import app.vleon.buapi.BuPost;
import app.vleon.ui.DividerItemDecoration;

public class ThreadPostsActivity extends AppCompatActivity implements BuAPI.OnPostsResponseListener {

    public static RequestQueue mRequestQueue;
    public static BuAPI.LoginInfo mLoginInfo;
    MyApplication app;
    ArrayList<BuPost> mPostsList;
    int mFrom = 0;
    int mTo = 20;
    int mTid = 0;
    private UltimateRecyclerView mPostsRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ProgressBar mProgressBar;
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

        mProgressBar = (ProgressBar) findViewById(R.id.request_posts_progress);
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
            showProgress(true);
            mTid = Integer.parseInt(tid);
            app.getAPI().getThreadPosts(mTid, mFrom, mTo);
        } else {
            Toast.makeText(this, "获取帖子ID失败", Toast.LENGTH_SHORT).show();
        }

        mPostsRecyclerView.enableLoadmore();
        mPostsRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, int maxLastVisiblePosition) {
                clearFlag = false;
                mFrom = mTo + 1;
                mTo = mFrom + 20;
                app.getAPI().getThreadPosts(mTid, mFrom, mTo);
            }
        });
        mPostsRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                clearFlag = true;
                mFrom = 0;
                mTo = 20;
                app.getAPI().getThreadPosts(mTid, mFrom, mTo);
            }
        });
        mAdapter.setCustomLoadMoreView(LayoutInflater.from(this).inflate(R.layout.load_more, null));
        app.getAPI().setOnPostsResponseListener(this);
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
            app.getAPI().getThreadPosts(mTid, mFrom, mTo);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void handlePostsGetterResponse(BuAPI.Result result, ArrayList<BuPost> postsList) {
        if (clearFlag) {
            mPostsList.clear();
        }
        switch (result) {
            case SUCCESS:
                mPostsList.addAll(postsList);
                mAdapter.refresh(mPostsList);
                //  TODO: 2016/1/18 恰好20个posts怎么处理？
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
        showProgress(false);
    }

    @Override
    public void handlePostsGetterErrorResponse(VolleyError error) {
        Toast.makeText(ThreadPostsActivity.this, "获取异常", Toast.LENGTH_SHORT).show();
        showProgress(false);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mPostsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
            mPostsRecyclerView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mPostsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressBar.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            mPostsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}



