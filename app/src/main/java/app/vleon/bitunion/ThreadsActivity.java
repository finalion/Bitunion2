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

    MyApplication app;

    ArrayList<BuAPI.ThreadInfo> mThreadsList;
    BuForum mCurrentForum;
    int mCurrentForumId;
    int mFrom = 0;
    int mTo = 20;
    Drawer mDrawerResult = null;
    /*Left Drawer*/
    private ActionBarDrawerToggle mDrawerToggle;
    /*Main ListView*/
    private UltimateRecyclerView mThreadsRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ThreadsAdapter mAdapter;

    private boolean opened = false;
    private boolean clearFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_threads);
        app = (MyApplication) getApplicationContext();
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


        mThreadsRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, int maxLastVisiblePosition) {
                clearFlag = false;
                mFrom = mTo + 1;
                mTo = mFrom + 20;
                app.getAPI().getThreadsList(mCurrentForumId, mFrom, mTo);
//                Log.d("avatar", app.getMyInfo().getTrueAvatar());
            }
        });

        mThreadsRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                mThreadsRecyclerView.setRefreshing(false);
                mLayoutManager.scrollToPosition(0);
                clearFlag = true;
                mFrom = 0;
                mTo = 20;
                app.getAPI().getThreadsList(mCurrentForumId, mFrom, mTo);
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
        app.getAPI().setOnThreadsResponseListener(this);

        //设置left drawer
        // Create the AccountHeader
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(
                        new ProfileDrawerItem()
                                .withName(app.getMyInfo().username)
                                .withEmail(app.getMyInfo().postnum)
                                .withIcon(app.getMyInfo().getTrueAvatar())
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                        Intent intent = new Intent(ThreadsActivity.this, PersonalInfoActivity.class);
                        intent.putExtra("uid", "me");
                        startActivity(intent);
                        return false;
                    }
                })
                .build();

        mDrawerResult = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(mToolbar)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("最新帖子").withTag(0),
                        new SectionDrawerItem().withName("收藏夹"),
                        new SectionDrawerItem().withName("论坛版块"),
                        new SecondaryDrawerItem().withName("苦中作乐区").withIdentifier(13).withSelectable(false),
                        new SecondaryDrawerItem().withName("技术讨论区").withIdentifier(16).withSelectable(false),
                        new SecondaryDrawerItem().withName("直通理工区").withIdentifier(129).withSelectable(false),
                        new SecondaryDrawerItem().withName("时尚生活区").withIdentifier(166).withSelectable(false),
                        new SecondaryDrawerItem().withName("系统管理区").withIdentifier(2).withSelectable(false)
//                        new SecondaryDrawerItem().withName("其他功能").withTag(5)
                )
                .withOnDrawerItemClickListener(
                        new Drawer.OnDrawerItemClickListener() {
                            @Override
                            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                                if (drawerItem != null) {
                                    Log.d("identifier", drawerItem.getIdentifier() + "");
                                    switch (drawerItem.getIdentifier()) {
                                        case 13:
                                            if (opened) {
                                                mDrawerResult.removeItems(22, 23, 25, 14, 24, 27, 115, 124);
                                            } else {
                                                int curPos = mDrawerResult.getPosition(drawerItem);
                                                mDrawerResult.addItemsAtPosition(curPos,
                                                        new SecondaryDrawerItem().withName("游戏人生").withIdentifier(22).withLevel(2).withTag("游戏人生"),
                                                        new SecondaryDrawerItem().withName("影视天地").withIdentifier(23).withLevel(2).withTag("影视天地"),
                                                        new SecondaryDrawerItem().withName("音乐殿堂").withIdentifier(25).withLevel(2).withTag("音乐殿堂"),
                                                        new SecondaryDrawerItem().withName("灌水乐园").withIdentifier(14).withLevel(2).withTag("灌水乐园"),
                                                        new SecondaryDrawerItem().withName("贴图欣赏").withIdentifier(24).withLevel(2).withTag("贴图欣赏"),
                                                        new SecondaryDrawerItem().withName("动漫天空").withIdentifier(27).withLevel(2).withTag("动漫天空"),
                                                        new SecondaryDrawerItem().withName("体坛风云").withIdentifier(115).withLevel(2).withTag("体坛风云"),
                                                        new SecondaryDrawerItem().withName("职场生涯").withIdentifier(124).withLevel(2).withTag("职场生涯"));
                                            }
                                            opened = !opened;
                                            return true;
                                        case 16:
                                            return true;
                                        case 129:
                                            return true;
                                        case 166:
                                            return true;
                                        case 2:
                                            return true;
                                        default:
                                            mThreadsList.clear();
                                            mCurrentForumId = Integer.parseInt(String.valueOf(drawerItem.getIdentifier()));
                                            app.getAPI().getThreadsList(mCurrentForumId, mFrom, mTo);
                                            if (drawerItem.getTag() != null) {
                                                getSupportActionBar().setTitle(drawerItem.getTag().toString());
                                            } else {
                                                getSupportActionBar().setTitle("北理FTP联盟");
                                            }
                                            break;
                                    }
                                }
                                return false;
                            }
                        }

                )
                .build();

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mDrawerResult.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
        if (savedInstanceState == null) {
            mDrawerResult.setSelection(mCurrentForumId);
        }
        app.getAPI().getThreadsList(mCurrentForumId, mFrom, mTo);
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
            app.getAPI().getThreadsList(mCurrentForum.getFid(), mFrom, mTo);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void handleThreadsGetterResponse(BuAPI.Result
                                                    result, ArrayList<BuAPI.ThreadInfo> threadsList) {
        switch (result) {
            case SUCCESS:
                break;
            case IP_LOGGED:
                // session失效，重新获取
                break;
        }
        if (clearFlag)
            mThreadsList.clear();
        mThreadsList.addAll(threadsList);
        mAdapter.refresh(mThreadsList);
    }

    @Override
    public void handleThreadsGetterErrorResponse(VolleyError error) {
        Toast.makeText(ThreadsActivity.this, "查询异常", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        outState = mDrawerResult.saveInstanceState(outState);
        //add the values which need to be saved from the accountHeader to the bundle
        outState = mDrawerResult.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (mDrawerResult != null && mDrawerResult.isDrawerOpen()) {
            mDrawerResult.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

}
