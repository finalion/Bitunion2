package app.vleon.bitunion;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import app.vleon.buapi.BuAPI;

public class ThreadsActivity extends Activity {


    private UltimateRecyclerView mThreadsRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ThreadsAdapter mAdapter;

    public static RequestQueue mRequestQueue;
    public static LoginInfo mLoginInfo;

    ArrayList<ThreadInfo> mThreadsList;

    int mFrom = 0;
    int mTo = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_threads);

        mRequestQueue = LoginActivity.mRequestQueue;
        mLoginInfo = LoginActivity.mLoginInfo;
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

        getThreadsList(14, mFrom, mTo);

        mThreadsRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, int maxLastVisiblePosition) {
                mFrom = mTo + 1;
                mTo = mFrom + 20;
                getThreadsList(14, mFrom, mTo);
            }
        });

        mThreadsRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mThreadsList.clear();
                mFrom = 0;
                mTo = 20;
                getThreadsList(14, mFrom, mTo);
            }
        });
        mAdapter.setOnItemClickedListener(new ThreadsAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, ThreadInfo threadInfo) {
                Intent intent = new Intent(ThreadsActivity.this, ThreadPostsActivity.class);
                intent.putExtra("tid", threadInfo.tid);
                startActivity(intent);
            }
        });
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
            getThreadsList(14, mFrom, mTo);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 查询论坛帖子
     *
     * @param fid  论坛版块id
     * @param from 帖子起始编号，最新帖子编号为0
     * @param to   帖子结束编号 to-from <=20
     */
    public void getThreadsList(final int fid, final int from, final int to) {
        HashMap<String, String> params = new HashMap<>();
        params.put("action", "thread");
        params.put("username", mLoginInfo.username);
        params.put("session", mLoginInfo.session);
        params.put("fid", fid + "");
        params.put("from", from + "");
        params.put("to", to + ""); // to=100, thread+number+error
        final Gson gson = new Gson();
        JsonObjectRequest mLoginRequest = new JsonObjectRequest(BuAPI.THREAD_URL,
                new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    String result;
                    String msg = "success";
                    String list;

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("TAG", response.toString());
                        try {
                            result = response.getString("result");
                            if (result.equals("success")) {
                                list = response.getJSONArray("threadlist").toString();
                                ArrayList<ThreadInfo> templist = gson.fromJson(list, new TypeToken<List<ThreadInfo>>() {
                                }.getType());
                                mThreadsList.addAll(templist);
//                                mAdapter.notifyDataSetChanged();
                                mAdapter.refresh(mThreadsList);
//                                Toast.makeText(ThreadsActivity.this, String.format("tid %d from %d to %d: %d threads", fid, from, to, mThreadsList.size()), Toast.LENGTH_LONG).show();
//                                getThreadDetail(10604709, 1, 10);
                            } else {
                                msg = response.getString("msg");
                                Toast.makeText(ThreadsActivity.this, msg, Toast.LENGTH_LONG).show();
                                switch (msg) {
                                    case "thread+number+error":
                                        break;
                                    case "IP+logged":
                                        break;
                                    default:
                                        break;
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("TAG", error.getMessage(), error);
                        Toast.makeText(ThreadsActivity.this, "查询异常", Toast.LENGTH_SHORT).show();
                    }
                });
        mRequestQueue.add(mLoginRequest);
    }
}


class ThreadInfo {
    String tid;
    String author;
    String authorid;
    String subject;
    String dateline;
    String lastpost;
    String lastposter;
    String views;
    String replies;
}

