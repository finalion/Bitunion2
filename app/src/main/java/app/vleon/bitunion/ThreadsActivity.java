package app.vleon.bitunion;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import java.util.ArrayList;

import app.vleon.buapi.BuAPI;

public class ThreadsActivity extends Activity implements BuAPI.OnThreadsResponseListener {

    private UltimateRecyclerView mThreadsRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ThreadsAdapter mAdapter;
    ArrayList<BuAPI.ThreadInfo> mThreadsList;
    public BuAPI mAPI;

    int mFrom = 0;
    int mTo = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_threads);

        mAPI = LoginActivity.mAPI;
        mThreadsList = new ArrayList<>();

        mThreadsRecyclerView = (UltimateRecyclerView) findViewById(R.id.threads_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mThreadsRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mThreadsRecyclerView.setLayoutManager(mLayoutManager);
        mThreadsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mThreadsRecyclerView.enableLoadmore();
        // specify an adapter (see also next example)
        mAdapter = new ThreadsAdapter(mThreadsList);
        mAdapter.setCustomLoadMoreView(LayoutInflater.from(this).inflate(R.layout.load_more, null));
        mThreadsRecyclerView.setAdapter(mAdapter);

        LoginActivity.mAPI.getThreadsList(14, mFrom, mTo);

        mThreadsRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, int maxLastVisiblePosition) {
                mFrom = mTo + 1;
                mTo = mFrom + 20;
                mAPI.getThreadsList(14, mFrom, mTo);
            }
        });

        mThreadsRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mThreadsList.clear();
                mFrom = 0;
                mTo = 20;
                mAPI.getThreadsList(14, mFrom, mTo);
            }
        });
        mAdapter.setOnItemClickedListener(new ThreadsAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, BuAPI.ThreadInfo threadInfo) {
                Intent intent = new Intent(ThreadsActivity.this, ThreadPostsActivity.class);
                intent.putExtra("tid", threadInfo.tid);
                startActivity(intent);
            }
        });
        mAPI.setOnThreadsResponseListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_threads, menu);
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
            mThreadsList.clear();
            mFrom = 0;
            mTo = 20;
            mAPI.getThreadsList(14, mFrom, mTo);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void handleThreadsGetterResponse(BuAPI.Result result, ArrayList<BuAPI.ThreadInfo> threadsList) {
        switch (result) {
            case SUCCESS:
                break;
            case IP_LOGGED:
                // session失效，重新获取
                break;
        }
        mThreadsList.addAll(threadsList);
        mAdapter.refresh(mThreadsList);
    }

    @Override
    public void handleThreadsGetterErrorResponse(VolleyError error) {
        Toast.makeText(ThreadsActivity.this, "查询异常", Toast.LENGTH_SHORT).show();
    }
}


