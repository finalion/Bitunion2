package app.vleon.bitunion;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.List;
import java.util.Map;

import app.vleon.bitunion.buapi.BuAPI;
import app.vleon.bitunion.buapi.BuMember;
import app.vleon.bitunion.fragment.ForumThreadsFragment;
import app.vleon.bitunion.fragment.LatestThreadsFragment;
import app.vleon.bitunion.util.Utils;

public class MainActivity extends AppCompatActivity implements BuAPI.OnMemberInfoResponseListener,
        LatestThreadsFragment.OnLatestThreadsFragmentInteractionListener, ForumThreadsFragment.OnForumThreadsFragmentInteractionListener {

    final int PROFILE_START_FLAG = 1000;
    final int LOGOUT_FLAG = 2000;
    MyApplication app;
    FragmentManager mFragmentManager;
    Toolbar mToolbar;
    Drawer mDrawerResult = null;
    IProfile mMyProfile;
    AccountHeader mHeaderResult;
    Map<String, List<Map<String, String>>> mForumsList = null;
    SparseBooleanArray openStatus = null;
    private int mForumId = 0;


    private void removeDrawerForumItems(int identifier) {
        if (mForumsList != null) {
            List<Map<String, String>> items = mForumsList.get(identifier + "");
            for (int i = 0; i < items.size(); i++) {
                Map<String, String> item = items.get(i);
                mDrawerResult.removeItem(Integer.parseInt(item.get("fid")));
            }
        }
    }

    private void addDrawerForumItems(int curPos, int identifier) {
        if (mForumsList != null) {
            List<Map<String, String>> items = mForumsList.get(identifier + "");
            for (int i = 0; i < items.size(); i++) {
                Map<String, String> item = items.get(i);
                mDrawerResult.addItemsAtPosition(curPos,
                        new SecondaryDrawerItem()
                                .withName(item.get("name"))
                                .withIdentifier(Integer.parseInt(item.get("fid")))
                                .withLevel(2)
                                .withTag(item.get("name")));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        app = (MyApplication) getApplicationContext();
        mFragmentManager = getSupportFragmentManager();
        try {
            mForumsList = Utils.readJsonFromFile(getAssets().open("forums.json"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        openStatus = new SparseBooleanArray() {
            {
                put(13, false);
                put(16, false);
                put(129, false);
                put(166, false);
                put(2, false);
            }
        };

        //设置toolbar
        mToolbar = (Toolbar) findViewById(R.id.activity_thread_toolbar);
        setSupportActionBar(mToolbar);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //设置left drawer
        // Create the AccountHeader
        mMyProfile = new ProfileDrawerItem().withEmail(app.getAPI().getUsername()).withIcon(R.drawable.noavatar).withIdentifier(PROFILE_START_FLAG);
        mHeaderResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(
                        mMyProfile,
                        new ProfileSettingDrawerItem().withName("注销").withIcon(CommunityMaterial.Icon.cmd_logout).withIdentifier(LOGOUT_FLAG)
                )
                .withOnAccountHeaderProfileImageListener(new AccountHeader.OnAccountHeaderProfileImageListener() {
                    @Override
                    public boolean onProfileImageClick(View view, IProfile profile, boolean current) {
                        Intent intent = new Intent(MainActivity.this, PersonalInfoActivity.class);
                        intent.putExtra("uid", "me");
                        startActivity(intent);
                        return true;
                    }

                    @Override
                    public boolean onProfileImageLongClick(View view, IProfile profile, boolean current) {
                        return false;
                    }
                })
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                        if ((profile instanceof IDrawerItem) && ((IDrawerItem) profile).getIdentifier() == LOGOUT_FLAG) {
                            app.getAPI().logout();
                            getSharedPreferences("lastlogin", MODE_PRIVATE).edit().clear().apply();
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            intent.putExtra("from", "logout_menu");
                            startActivity(intent);
                        }
                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .build();

        mDrawerResult = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(mToolbar)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .withAccountHeader(mHeaderResult)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("最新帖子").withIdentifier(0).withTag("最新帖子"),
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
                                    int identifier = (int) drawerItem.getIdentifier();
                                    String tag = (String) drawerItem.getTag();
                                    switch (identifier) {
                                        case 13:
                                        case 16:
                                        case 129:
                                        case 166:
                                        case 2:
                                            removeDrawerForumItems(13);
                                            removeDrawerForumItems(16);
                                            removeDrawerForumItems(129);
                                            removeDrawerForumItems(166);
                                            removeDrawerForumItems(2);
                                            boolean opened = openStatus.get(identifier);
                                            //全部项置于关闭状态
                                            openStatus.put(13, false);
                                            openStatus.put(16, false);
                                            openStatus.put(129, false);
                                            openStatus.put(166, false);
                                            openStatus.put(2, false);
                                            if (opened) {
                                                removeDrawerForumItems(identifier);
                                            } else {
                                                int curPos = mDrawerResult.getPosition(drawerItem);
                                                addDrawerForumItems(curPos, identifier);
                                            }
                                            openStatus.put(identifier, !opened);
                                            mForumId = identifier;
                                            return true;
                                        case 0:
                                            showLatest();
                                            break;
                                        default:
                                            showForum(identifier, tag);
//                                            mLayoutManager.scrollToPosition(0);
                                            break;
                                    }
                                }
                                return false;
                            }
                        }
                )
                .withSavedInstance(savedInstanceState)
                .withShowDrawerOnFirstLaunch(true)
                .build();

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//        mDrawerResult.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
        if (savedInstanceState == null) {
            mDrawerResult.setSelection(mForumId);
        }
        app.getAPI().setOnMemberInfoResponseListener(this);
        app.getAPI().getMyInfo();
        showLatest();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerResult.getActionBarDrawerToggle().syncState();
        Log.d("null check", "post create");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the accountHeader to the bundle
        outState = mDrawerResult.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
        Log.d("null check", "save state");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("null check", "destroy");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("null check", "stop");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("null check", "resume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("null check", "pause");
    }

    public void showLatest() {
        getSupportActionBar().setTitle("最新帖子");
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        LatestThreadsFragment latestThreadsFragment = LatestThreadsFragment.newInstance();
        transaction.replace(R.id.threads_fragment, latestThreadsFragment);
        transaction.commit();
    }

    public void showForum(int fid, String name) {
        if (name != null) {
            getSupportActionBar().setTitle(name);
        } else {
            getSupportActionBar().setTitle("北理FTP联盟");
        }
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        ForumThreadsFragment forumThreadsFragment = ForumThreadsFragment.newInstance(fid, name);
        transaction.replace(R.id.threads_fragment, forumThreadsFragment);
        transaction.commit();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerResult.getActionBarDrawerToggle().onConfigurationChanged(newConfig);
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
//        if (mDrawerToggle.onOptionsItemSelected(item)) {
//            return true;
//        }

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
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

    @Override
    public void handleMemberInfoGetterResponse(BuAPI.Result result, BuMember memberInfo) {
        switch (result) {
            case SUCCESS:
                app.setMyInfo(memberInfo);
                mMyProfile.withIcon(memberInfo.avatar);
                mHeaderResult.updateProfile(mMyProfile);
                break;
            default:
                Toast.makeText(this, "未知登录错误: " + app.getLoginInfo().msg, Toast.LENGTH_SHORT).show();
                break;
        }
        mDrawerResult.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);

    }

    @Override
    public void handleMemberInfoGetterErrorResponse(VolleyError error) {
        mDrawerResult.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);

    }

    @Override
    public void onForumThreadsFragmentInteraction(Uri uri) {

    }

    @Override
    public void onLatestThreadsFragmentInteraction(Uri uri) {

    }
}
