package app.vleon.bitunion;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.vleon.buapi.BuAPI;
import app.vleon.ui.DividerItemDecoration;

public class ThreadPostsActivity extends Activity {

    public static RequestQueue mRequestQueue;
    public static LoginInfo mLoginInfo;

    private UltimateRecyclerView mPostsRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ThreadPostsAdapter mAdapter;
    ArrayList<PostInfo> mPostsList;
    int mFrom = 0;
    int mTo = 20;
    int mTid = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread_posts);

        mRequestQueue = LoginActivity.mRequestQueue;
        mLoginInfo = LoginActivity.mLoginInfo;
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
            getThreadPosts(mTid, mFrom, mTo);
        } else {
            Toast.makeText(this, "获取帖子ID失败", Toast.LENGTH_SHORT).show();
        }

        mPostsRecyclerView.enableLoadmore();
        mPostsRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, int maxLastVisiblePosition) {
                mFrom = mTo + 1;
                mTo = mFrom + 20;
                getThreadPosts(mTid, mFrom, mTo);
            }
        });
        mPostsRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPostsList.clear();
                mFrom = 0;
                mTo = 20;
                getThreadPosts(mTid, mFrom, mTo);
            }
        });
        mAdapter.setCustomLoadMoreView(LayoutInflater.from(this).inflate(R.layout.load_more, null));
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
            getThreadPosts(mTid, mFrom, mTo);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 查询帖子详情
     *
     * @param thread 主题帖子
     * @param from   帖子起始编号，最新帖子编号为0
     * @param to     帖子结束编号 to-from <=20
     */
    public void getThreadPosts(ThreadInfo thread, final int from, final int to) {
        getThreadPosts(Integer.parseInt(thread.tid), from, to);
    }

    /**
     * 查询帖子详情
     *
     * @param tid  主题帖子id
     * @param from 帖子起始编号，最新帖子编号为0
     * @param to   帖子结束编号 to-from <=20
     */
    public void getThreadPosts(final int tid, final int from, final int to) {
        HashMap<String, String> params = new HashMap<>();
        params.put("action", "post");
        params.put("username", mLoginInfo.username);
        params.put("session", mLoginInfo.session);
        params.put("tid", tid + "");
        params.put("from", from + "");
        params.put("to", to + ""); // to=100, thread+number+error
        final Gson gson = new Gson();
        JsonObjectRequest mLoginRequest = new JsonObjectRequest(BuAPI.POST_URL,
                new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    String result;
                    String msg = "success";

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("TAG", response.toString());
                        try {
                            result = response.getString("result");
                            if (result.equals("success") && response.has("postlist")) {
                                String tmp = response.getJSONArray("postlist").toString();
                                ArrayList<PostInfo> tempList = gson.fromJson(tmp, new TypeToken<List<PostInfo>>() {
                                }.getType());
                                for (PostInfo post : tempList) {
                                    post.parse();
                                }
                                mPostsList.addAll(tempList);
                                mAdapter.refresh(mPostsList);
                                if (tempList.size() < 20) {
                                    mPostsRecyclerView.disableLoadmore();
                                }
//                                Toast.makeText(ThreadPostsActivity.this, String.format("fid %d from %d to %d: %d posts", tid, from, to, mPostsList.size()), Toast.LENGTH_LONG).show();
                            } else {
                                msg = response.getString("msg");
                                Toast.makeText(ThreadPostsActivity.this, msg, Toast.LENGTH_LONG).show();
                                switch (msg) {
                                    case "thread+number+error":
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
                        Toast.makeText(ThreadPostsActivity.this, "获取异常", Toast.LENGTH_SHORT).show();
                    }
                });
        mRequestQueue.add(mLoginRequest);
    }
}

class PostInfo {
    String pid;
    String fid;
    String tid;
    String aid;
    String icon;
    String author;
    String authorid;
    String subject;
    String dateline;
    String message;
    String usesig;
    String bbcodeoff;
    String smileyoff;
    String parseurloff;
    String score;
    String rate;
    String ratetimes;
    String pstatus;
    String lastedit;
    String aaid;
    String creditsrequire;
    String filetype;
    String filename;
    String attachment;
    String filesize;
    String downloads;
    String uid;
    String username;
    String avatar;
    String epid;
    String maskpost;
    String content;
    String trueAvatar;

    public ArrayList<Quote> quotes = new ArrayList<>();

    public void parse() {
        try {
            message = URLDecoder.decode(message, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.content = parseQuotes(removeBlankLines(message));
        this.trueAvatar = getTrueAvatar();
    }

    //得到头像真实的URL
    public String getTrueAvatar() {
        try {
            avatar = URLDecoder.decode(avatar, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
        Pattern p = Pattern.compile("<img src=\"(.*?)\"  border=\"0\">", Pattern.DOTALL);
        Matcher m = p.matcher(avatar);
        String finder;
        while (m.find()) {
            finder = m.group(1);
            if (finder.startsWith("http://")) {
                finder = finder.replace("http://www.bitunion.org", "http://out.bitunion.org");  //// TODO: 2015/11/4
                finder = finder.replace("http://bitunion.org", "http://out.bitunion.org");
                return finder;
            }
            return BuAPI.ROOTURL + finder;
        }
        return "";
    }

    // 去除段前段后的换行符
    private String removeBlankLines(String content) {
        content = content.trim();
        while (content.startsWith("<br>")) {
            content = content.substring(4).trim();
        }
        while (content.startsWith("<br />")) {
            content = content.substring(6).trim();
        }
        while (content.endsWith("<br />")) {
            content = content.substring(0, content.length() - 6).trim();
        }
        return content;
    }

    // 解析帖子的引用部分
    public String parseQuotes(String message) {
        quotes.clear();
        Pattern p = Pattern
                .compile(
                        "<center><table border=\"0\" width=\"90%\".*?bgcolor=\"ALTBG2\"><b>(.*?)</b> (\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2})<br />(.*?)</td></tr></table></td></tr></table></center><br>",
                        Pattern.DOTALL);
        Matcher m = p.matcher(message);
        while (m.find()) {
            // 1: author; 2:time; 3:content
            quotes.add(new Quote(m.group(1), m.group(2), removeBlankLines(m.group(3))));
            message = message.replace(m.group(0), "");
        }
        return message;
    }

}

class Quote {
    public String quoteAuthor;
    public String quoteTime;
    public String quoteContent;

    public Quote(String author, String time, String content) {
        quoteAuthor = author;
        quoteTime = time;
        quoteContent = content;
    }
}