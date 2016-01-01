package app.vleon.bitunion;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import java.util.ArrayList;

import app.vleon.buapi.BuAPI;
import app.vleon.buapi.BuForum;

public class ThreadsActivity extends AppCompatActivity implements BuAPI.OnThreadsResponseListener {

    public BuAPI mAPI;
    // 所有论坛列表数据
    ArrayList<ArrayList<BuForum>> fArrayList = new ArrayList<>();
    // ExpandableListView的分组信息
    String[] groupList;
    ArrayList<BuAPI.ThreadInfo> mThreadsList;
    int mFrom = 0;
    int mTo = 20;
    /*Left Drawer*/
    private DrawerLayout mDrawLayout;
    private RecyclerView mDrawerRecyclerView;
    private ActionBarDrawerToggle mDrawerToggle;
    private RecyclerView.LayoutManager mDrawerLayoutManager;
    private RecyclerView.Adapter mDrawerAdapter;
    private RecyclerView.Adapter mDrawerWrappedAdapter;
    private RecyclerViewExpandableItemManager mDrawerRecyclerViewExpandableItemManager;
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

        //设置toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(mToolbar);

        //设置left drawer
        groupList = getResources().getStringArray(R.array.forum_group);
        getForumArrayListFromResources();
        mDrawLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerRecyclerView = (RecyclerView) findViewById(R.id.left_drawer);
        mDrawerLayoutManager = new LinearLayoutManager(this);
        mDrawerRecyclerViewExpandableItemManager = new RecyclerViewExpandableItemManager(null);
        ForumListAdapter itemAdapter = new ForumListAdapter();
        mDrawerAdapter = itemAdapter;
        mDrawerWrappedAdapter = mDrawerRecyclerViewExpandableItemManager.createWrappedAdapter(itemAdapter);

        final GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();
        mDrawerRecyclerView.setLayoutManager(mDrawerLayoutManager);
        mDrawerRecyclerView.setAdapter(mDrawerWrappedAdapter);  // requires *wrapped* adapter
        mDrawerRecyclerView.setItemAnimator(animator);
        mDrawerRecyclerView.setHasFixedSize(false);
        mDrawerRecyclerViewExpandableItemManager.attachRecyclerView(mDrawerRecyclerView);

//        String[] strings = {"122", "dff", "333", "444"};
//        mDrawerRecyclerView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, strings));
//        mDrawerRecyclerView.setOnItemClickListener(null);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawLayout, R.string.abc_action_bar_home_description, R.string.abc_action_bar_up_description) {
            /**
             * Called when a drawer has settled in a completely closed state.
             */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
//                getSupportActionBar().setTitle("灌水乐园");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /**
             * Called when a drawer has settled in a completely open state.
             */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("北理FTP联盟");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
//        getWindow().getDecorView().setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

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

//        mAPI.getForumsList();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

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

    public ArrayList<ArrayList<BuForum>> getForumArrayListFromResources() {
        // 单组论坛列表数据
        ArrayList<BuForum> forumList = new ArrayList<BuForum>();
//        // 所有论坛列表数据
//        ArrayList<ArrayList<BuForum>> fArrayList = new ArrayList<ArrayList<BuForum>>();
//        // 分组数据
//        String[] groupList;
//        // ExpandableListView的分组信息
//        groupList = getResources().getStringArray(R.array.forum_group);

        // 读取论坛列表信息
        String[] forumNames = getResources().getStringArray(R.array.forums);
        int[] forumFids = getResources().getIntArray(R.array.fids);
        int[] forumTypes = getResources().getIntArray(R.array.types);
        for (int i = 0; i < forumNames.length; i++)
            forumList.add(new BuForum(forumNames[i], forumFids[i], forumTypes[i]));
        // 转换论坛列表信息为二维数组，方便ListViewAdapter读入
        for (int i = 0; i < groupList.length; i++) {
            ArrayList<BuForum> forums = new ArrayList<BuForum>();
            for (BuForum forum : forumList)
                if (i == forum.getType())
                    forums.add(forum);
            fArrayList.add(forums);
        }
        return fArrayList;
    }

    private static class GroupViewHolder extends AbstractExpandableItemViewHolder {
        ImageView indicator;
        TextView groupName;

        private GroupViewHolder(View itemView) {
            super(itemView);
            indicator = (ImageView) itemView.findViewById(R.id.imgVw_group_expand_indicator);
            groupName = (TextView) itemView.findViewById(R.id.txtVw_group_title);
        }
    }

    private static class ChildViewHolder extends AbstractExpandableItemViewHolder {
        TextView childTitle;

        private ChildViewHolder(View itemView) {
            super(itemView);
            childTitle = (TextView) itemView.findViewById(R.id.txtVw_forum_title);
        }
    }

    private class ForumListAdapter extends AbstractExpandableItemAdapter<GroupViewHolder, ChildViewHolder> {

        private ForumListAdapter() {
            setHasStableIds(true);
        }

        @Override
        public int getGroupCount() {
            return groupList.length;
        }

        @Override
        public int getChildCount(int groupPosition) {
            return fArrayList.get(groupPosition).size();
        }

        @Override
        public long getGroupId(int groupPos) {
            return groupPos;
        }

        @Override
        public long getChildId(int groupPos, int childPos) {
            return childPos;
        }

        @Override
        public int getGroupItemViewType(int i) {
            return 0;
        }

        @Override
        public int getChildItemViewType(int i, int i1) {
            return 0;
        }

        @Override
        public GroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_forum_group, parent, false);
            return new GroupViewHolder(view);
        }

        @Override
        public ChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_forum_title, parent, false);
            return new ChildViewHolder(view);
        }

        @Override
        public void onBindGroupViewHolder(GroupViewHolder groupVH, int groupPosition, int viewType) {
            groupVH.groupName.setText(groupList[groupPosition]);
            groupVH.itemView.setClickable(true);
        }

        @Override
        public void onBindChildViewHolder(ChildViewHolder childViewHolder, int groupPos, int childPos, int viewType) {
            if (fArrayList.get(groupPos).get(childPos).getName().contains("--"))
                childViewHolder.childTitle.setTextSize(16);
            else
                childViewHolder.childTitle.setTextSize(18);
            childViewHolder.childTitle.setText(fArrayList.get(groupPos).get(childPos).getName());
            final BuForum forum = fArrayList.get(groupPos).get(childPos);
            childViewHolder.childTitle.setOnClickListener(new View.OnClickListener() {

                @Override
                @SuppressWarnings("NewApi")
                public void onClick(View v) {
                    mThreadsList.clear();
                    mFrom = 0;
                    mTo = 20;
//                    Toast.makeText(ThreadsActivity.this, "click", Toast.LENGTH_SHORT).show();
//                    if (BUApi.isUserLoggedin()) {
                    if (forum.getFid() == -1) {
//                        LoginActivity.mAPI.getThreadsList(forum.getFid(), mFrom, mTo);
                    } else if (forum.getFid() == -2) {
                        // TODO 收藏夹
//                        ToastUtil.showToast("功能暂时无法使用");
                    } else {
                        LoginActivity.mAPI.getThreadsList(forum.getFid(), mFrom, mTo);
                        getSupportActionBar().setTitle(forum.getName());
                        mDrawLayout.closeDrawer(Gravity.LEFT);
                    }
//                    } else
//                        ToastUtil.showToast("请先登录");
                }
            });
        }

        @Override
        public boolean onCheckCanExpandOrCollapseGroup(GroupViewHolder groupVH, int groupPosition, int x, int y, boolean expand) {
            groupVH.indicator.setImageResource(expand ? R.drawable.ic_expand_more_grey600_48dp : R.drawable.ic_expand_less_grey600_48dp);
            return true;
        }
    }
}
