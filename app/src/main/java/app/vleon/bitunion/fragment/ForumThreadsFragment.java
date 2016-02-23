package app.vleon.bitunion.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.marshalchen.ultimaterecyclerview.ObservableScrollState;
import com.marshalchen.ultimaterecyclerview.ObservableScrollViewCallbacks;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import java.util.ArrayList;

import app.vleon.bitunion.MyApplication;
import app.vleon.bitunion.R;
import app.vleon.bitunion.ThreadPostsActivity;
import app.vleon.bitunion.adapter.ThreadsAdapter;
import app.vleon.bitunion.buapi.BuAPI;
import app.vleon.bitunion.buapi.BuThread;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnForumThreadsFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ForumThreadsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ForumThreadsFragment extends Fragment implements BuAPI.OnThreadsResponseListener, BuAPI.OnPostNewThreadResponseListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String FORUM_ID = "fid";
    private static final String FORUM_NAME = "name";
    private static final int DEFAULT_FORUM_ID = 14;  //默认版块
    MyApplication app;
    int mFrom = 0;
    int mTo = 20;
    ArrayList<BuThread> mThreadsList;
    FloatingActionButton mFloatingButton;
    private int mForumId;
    private String mForumName;
    private boolean clearFlag = false;
    private ThreadsAdapter mAdapter;
    /*Main ListView*/
    private UltimateRecyclerView mThreadsRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private OnForumThreadsFragmentInteractionListener mListener;

    public ForumThreadsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param fid       论坛id.
     * @param forumName 论坛名称.
     * @return A new instance of fragment ForumThreadsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ForumThreadsFragment newInstance(int fid, String forumName) {
        ForumThreadsFragment fragment = new ForumThreadsFragment();
        Bundle args = new Bundle();
        args.putInt(FORUM_ID, fid);
        args.putString(FORUM_NAME, forumName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mForumId = getArguments().getInt(FORUM_ID, DEFAULT_FORUM_ID);
            mForumName = getArguments().getString(FORUM_NAME);
        }

        app = (MyApplication) getActivity().getApplicationContext();
        mThreadsList = new ArrayList<>();

        // specify an adapter (see also next example)
        mAdapter = new ThreadsAdapter(mThreadsList);
        mAdapter.setOnItemClickedListener(new ThreadsAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, BuThread threadInfo) {
                Intent intent = new Intent(getActivity(), ThreadPostsActivity.class);
                intent.putExtra("tid", threadInfo.tid);
                startActivity(intent);
            }
        });
        app.getAPI().setOnPostNewThreadResponseListener(this);
        app.getAPI().setOnThreadsResponseListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_forum_threads, container, false);
        mThreadsRecyclerView = (UltimateRecyclerView) view.findViewById(R.id.threads_recycler_view);
        mThreadsRecyclerView.setHasFixedSize(true);
        //floating action button
        mFloatingButton = (FloatingActionButton) view.findViewById(R.id.custom_urv_add_floating_button);
        mFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PostDialogFragment pdf = new PostDialogFragment();
                pdf.show(getActivity().getSupportFragmentManager(), "post_dialog");
            }
        });

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mThreadsRecyclerView.setLayoutManager(mLayoutManager);

        mThreadsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mThreadsRecyclerView.enableLoadmore();
        mAdapter.setCustomLoadMoreView(LayoutInflater.from(getActivity()).inflate(R.layout.load_more, null));
        mThreadsRecyclerView.setAdapter(mAdapter);
        mThreadsRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, int maxLastVisiblePosition) {
                loadMoreData();
            }
        });

        mThreadsRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                mThreadsRecyclerView.setRefreshing(false);
                mLayoutManager.scrollToPosition(0);
                refreshData();
            }
        });
        // 设置滚动条颜色变化
        mThreadsRecyclerView.setDefaultSwipeToRefreshColorScheme(R.color.colorAccent);
        mThreadsRecyclerView.setScrollViewCallbacks(new ObservableScrollViewCallbacks() {
            @Override
            public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {

            }

            @Override
            public void onDownMotionEvent() {

            }

            @Override
            public void onUpOrCancelMotionEvent(ObservableScrollState observableScrollState) {
//                int screenHeight = findViewById(android.R.id.content).getHeight();
//                if (observableScrollState == ObservableScrollState.DOWN) {
//                    mThreadsRecyclerView.showToolbar(mToolbar, mThreadsRecyclerView, screenHeight);
//                    mThreadsRecyclerView.showView(floatingButton, mThreadsRecyclerView, screenHeight);
//                } else if (observableScrollState == ObservableScrollState.UP) {
//                    mThreadsRecyclerView.hideToolbar(mToolbar, mThreadsRecyclerView, screenHeight);
//                    mThreadsRecyclerView.hideView(floatingButton, mThreadsRecyclerView, screenHeight);
//                }
            }
        });

        showRefreshingProgress(true);
        app.getAPI().getThreadsList(mForumId, mFrom, mTo);

        return view;
    }

    /**
     * 显示SwipeRefreshLayout的进度指示，bug，不能直接调用setRefreshing
     * https://github.com/cymcsg/UltimateRecyclerView/issues/192
     *
     * @param show whether to show indicator
     */
    public void showRefreshingProgress(final boolean show) {
        mThreadsRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                mThreadsRecyclerView.setRefreshing(show);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnForumThreadsFragmentInteractionListener) {
            mListener = (OnForumThreadsFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnForumThreadsFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void handleThreadsGetterResponse(BuAPI.Result result, ArrayList<BuThread> threadsList) {
        showRefreshingProgress(false);
        if (clearFlag)
            mThreadsList.clear();
        switch (result) {
            case SUCCESS:
                mThreadsList.addAll(threadsList);
                mAdapter.refresh(mThreadsList);
                if (threadsList.size() < 20) {
                    mThreadsRecyclerView.disableLoadmore();
                }
                break;
            case IP_LOGGED:
                // session失效，重新获取
                break;
            case SUCCESS_EMPTY:
                mThreadsRecyclerView.disableLoadmore();
                break;
        }
    }

    @Override
    public void handleThreadsGetterErrorResponse(VolleyError error) {
        showRefreshingProgress(false);
        Toast.makeText(getActivity(), "查询异常", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void handlePostNewThreadResponse(BuAPI.Result result, String tid) {
        Toast.makeText(getActivity(), "发表成功", Toast.LENGTH_SHORT).show();
        if (result == BuAPI.Result.SUCCESS && tid != null) {
            Intent intent = new Intent(getActivity(), ThreadPostsActivity.class);
            intent.putExtra("tid", tid);
            startActivity(intent);
        }
    }

    @Override
    public void handlePostNewThreadErrorResponse(VolleyError error) {
        Toast.makeText(getActivity(), "发表失败", Toast.LENGTH_SHORT).show();
    }

    public void loadMoreData() {
        clearFlag = false;
        mFrom = mTo;
        mTo = mFrom + 20;
        app.getAPI().getThreadsList(mForumId, mFrom, mTo);
    }

    public void refreshData() {
        clearFlag = true;
        mFrom = 0;
        mTo = 20;
        app.getAPI().getThreadsList(mForumId, mFrom, mTo);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnForumThreadsFragmentInteractionListener {
        // TODO: Update argument type and name
        void onForumThreadsFragmentInteraction(Uri uri);
    }

}
