package app.vleon.bitunion;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.marshalchen.ultimaterecyclerview.ObservableScrollState;
import com.marshalchen.ultimaterecyclerview.ObservableScrollViewCallbacks;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.marshalchen.ultimaterecyclerview.ui.floatingactionbutton.JellyBeanFloatingActionButton;

import java.util.ArrayList;

import app.vleon.buapi.BuAPI;
import app.vleon.buapi.BuThread;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnForumThreadsFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ForumThreadsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ForumThreadsFragment extends Fragment implements BuAPI.OnThreadsResponseListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String FORUM_ID = "fid";
    private static final String FORUM_NAME = "name";
    private static final int DEFAULT_FORUM_ID = 14;  //默认版块
    MyApplication app;
    int mFrom = 0;
    int mTo = 20;
    ArrayList<BuThread> mThreadsList;
    JellyBeanFloatingActionButton mFloatingButton;
    private int mForumId;
    private String mForumName;
    private boolean clearFlag = false;
    private ThreadsAdapter mAdapter;
    /*Main ListView*/
    private UltimateRecyclerView mThreadsRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private ProgressBar mProgressBar;
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
        app.getAPI().setOnThreadsResponseListener(this);
        app.getAPI().getThreadsList(mForumId, mFrom, mTo);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_forum_threads, container, false);
//        mProgressBar = (ProgressBar) view.findViewById(R.id.request_forum_threads_progress);
        mThreadsRecyclerView = (UltimateRecyclerView) view.findViewById(R.id.threads_recycler_view);
        mThreadsRecyclerView.setHasFixedSize(true);
//        showProgress(true);
        //floating action button
        mFloatingButton = (JellyBeanFloatingActionButton) view.findViewById(R.id.custom_urv_add_floating_button);
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
                clearFlag = false;
                mFrom = mTo + 1;
                mTo = mFrom + 20;
                app.getAPI().getThreadsList(mForumId, mFrom, mTo);
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
                app.getAPI().getThreadsList(mForumId, mFrom, mTo);
            }
        });
        mFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "floating button clicked", Toast.LENGTH_SHORT).show();
            }
        });
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
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onForumThreadsFragmentInteraction(uri);
        }
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
//        showProgress(false);
    }

    @Override
    public void handleThreadsGetterErrorResponse(VolleyError error) {
        Toast.makeText(getActivity(), "查询异常", Toast.LENGTH_SHORT).show();
//        showProgress(false);
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

            mThreadsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
            mThreadsRecyclerView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mThreadsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mThreadsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
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
