package app.vleon.bitunion;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.ArrayList;

import app.vleon.buapi.BuAPI;
import app.vleon.buapi.BuForum;

public class ThreadsActivity extends AppCompatActivity implements BuAPI.OnThreadsResponseListener {

    public BuAPI mAPI;
    // ExpandableListView的分组信息
    String[] groupList;
    ArrayList<BuAPI.ThreadInfo> mThreadsList;
    BuForum mCurrentForum;
    int mCurrentForumId;
    int mFrom = 0;
    int mTo = 20;
    /*Left Drawer*/
    private ActionBarDrawerToggle mDrawerToggle;
    /*Main ListView*/
    private UltimateRecyclerView mThreadsRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ThreadsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_threads);

        mAPI = LoginActivity.mAPI;
        mThreadsList = new ArrayList<>();
        mCurrentForumId = 14;    //默认论坛

        //设置toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(mToolbar);

        //设置主界面
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

        LoginActivity.mAPI.getThreadsList(mCurrentForumId, mFrom, mTo);

        mThreadsRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, int maxLastVisiblePosition) {
                mFrom = mTo + 1;
                mTo = mFrom + 20;
                mAPI.getThreadsList(mCurrentForumId, mFrom, mTo);
                Log.d("avatar", LoginActivity.mMyInfo.getTrueAvatar());
                Toast.makeText(ThreadsActivity.this, LoginActivity.mMyInfo.getTrueAvatar(), Toast.LENGTH_SHORT).show();
            }
        });

        mThreadsRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mThreadsList.clear();
                mFrom = 0;
                mTo = 20;
                mAPI.getThreadsList(mCurrentForumId, mFrom, mTo);
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

        //设置left drawer
        // Create the AccountHeader
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(
                        new ProfileDrawerItem()
                                .withName(LoginActivity.mMyInfo.username)
                                .withEmail(LoginActivity.mMyInfo.postnum)
                                .withIcon(LoginActivity.mMyInfo.getTrueAvatar())
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })

                .build();

        Drawer mDrawerResult = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(mToolbar)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("最新帖子").withTag(0),
                        new DividerDrawerItem(),
                        new SectionDrawerItem().withName("收藏夹"),
                        new DividerDrawerItem(),
                        new SectionDrawerItem().withName("苦中作乐区").withTag(13),
                        new SecondaryDrawerItem().withName("游戏人生").withTag(22).withLevel(2),
                        new SecondaryDrawerItem().withName("影视天地").withTag(23).withLevel(2),
                        new SecondaryDrawerItem().withName("音乐殿堂").withTag(25).withLevel(2),
                        new SecondaryDrawerItem().withName("灌水乐园").withTag(14).withLevel(2),
                        new SecondaryDrawerItem().withName("贴图欣赏").withTag(24).withLevel(2),
                        new SecondaryDrawerItem().withName("动漫天空").withTag(27).withLevel(2),
                        new SecondaryDrawerItem().withName("体坛风云").withTag(115).withLevel(2),
                        new SecondaryDrawerItem().withName("职场生涯").withTag(124).withLevel(2),
                        new SectionDrawerItem().withName("技术讨论区").withTag(16),
                        new SectionDrawerItem().withName("直通理工区").withTag(129),
                        new SectionDrawerItem().withName("时尚生活区").withTag(166),
                        new SectionDrawerItem().withName("系统管理区").withTag(2),
                        new SectionDrawerItem().withName("其他功能").withTag(5)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        Log.d("tag", drawerItem.getTag().toString());
                        mThreadsList.clear();
                        mCurrentForumId = Integer.parseInt(String.valueOf(drawerItem.getTag()));
                        LoginActivity.mAPI.getThreadsList(mCurrentForumId, mFrom, mTo);
                        getSupportActionBar().setTitle(drawerItem.getTag().toString());
                        return false;
                    }
                })
                .build();
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mDrawerResult.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
//        mAPI.getForumsList();
    }

//    @Override
//    protected void onPostCreate(Bundle savedInstanceState) {
//        super.onPostCreate(savedInstanceState);
//        // Sync the toggle state after onRestoreInstanceState has occurred.
//        mDrawerToggle.syncState();
//    }
//
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        mDrawerToggle.onConfigurationChanged(newConfig);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_threads, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            mThreadsList.clear();
            mFrom = 0;
            mTo = 20;
            mAPI.getThreadsList(mCurrentForum.getFid(), mFrom, mTo);
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
