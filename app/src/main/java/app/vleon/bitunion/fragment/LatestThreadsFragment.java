package app.vleon.bitunion.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.android.volley.VolleyError;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import java.util.ArrayList;

import app.vleon.bitunion.MyApplication;
import app.vleon.bitunion.R;
import app.vleon.bitunion.ThreadPostsActivity;
import app.vleon.bitunion.adapter.LatestThreadsAdapter;
import app.vleon.bitunion.buapi.BuAPI;
import app.vleon.bitunion.buapi.BuLatestThread;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnLatestThreadsFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LatestThreadsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LatestThreadsFragment extends Fragment implements BuAPI.OnLatestResponseListener {

    MyApplication app;
    ArrayList<BuLatestThread> mLatestThreadsList;
    private LatestThreadsAdapter mAdapter;
    private UltimateRecyclerView mLatestThreadsRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private ProgressBar mProgressBar;
    private OnLatestThreadsFragmentInteractionListener mListener;

    public LatestThreadsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LatestThreadsFragment.
     */
    public static LatestThreadsFragment newInstance() {
        return new LatestThreadsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (MyApplication) getActivity().getApplicationContext();
        mLatestThreadsList = new ArrayList<>();

        // specify an adapter (see also next example)
        mAdapter = new LatestThreadsAdapter(mLatestThreadsList);
        mAdapter.setOnItemClickedListener(new LatestThreadsAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, BuLatestThread thread) {
                Intent intent = new Intent(getActivity(), ThreadPostsActivity.class);
                intent.putExtra("tid", thread.getTid());
                startActivity(intent);
            }
        });
        app.getAPI().setOnLatestThreadsResponseListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_latest_threads, container, false);
        mProgressBar = (ProgressBar) view.findViewById(R.id.request_latest_threads_progress);
        mLatestThreadsRecyclerView = (UltimateRecyclerView) view.findViewById(R.id.latest_threads_recycler_view);
        mLatestThreadsRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLatestThreadsRecyclerView.setLayoutManager(mLayoutManager);
        mLatestThreadsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mLatestThreadsRecyclerView.setAdapter(mAdapter);
        mLatestThreadsRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mLayoutManager.scrollToPosition(0);
                app.getAPI().getLatestThreads();
            }
        });
        // 设置滚动条颜色变化
        mLatestThreadsRecyclerView.setDefaultSwipeToRefreshColorScheme(R.color.colorAccent);

        showRefreshingProgress(true);
        app.getAPI().getLatestThreads();
        return view;
    }

    /**
     * 显示SwipeRefreshLayout的进度指示，bug，不能直接调用setRefreshing
     * https://github.com/cymcsg/UltimateRecyclerView/issues/192
     *
     * @param show whether to show indicator
     */
    public void showRefreshingProgress(final boolean show) {
        mLatestThreadsRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                mLatestThreadsRecyclerView.setRefreshing(show);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnLatestThreadsFragmentInteractionListener) {
            mListener = (OnLatestThreadsFragmentInteractionListener) context;
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
    public void handleLatestThreadsGetterResponse(BuAPI.Result result, ArrayList<BuLatestThread> latestThreadsList) {
        showRefreshingProgress(false);
//        mProgressBar.clearAnimation();
//        mProgressBar.setVisibility(View.INVISIBLE);
        switch (result) {
            case SUCCESS:
                mLatestThreadsList.clear();
                mLatestThreadsList.addAll(latestThreadsList);
                mAdapter.refresh(latestThreadsList);
                break;
            case IP_LOGGED:
                // session失效，重新获取
                break;
            case SUCCESS_EMPTY:
                break;
        }
    }

    @Override
    public void handleLatestThreadsGetterErrorResponse(VolleyError error) {
        showRefreshingProgress(false);
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
    public interface OnLatestThreadsFragmentInteractionListener {
        // TODO: Update argument type and name
        void onLatestThreadsFragmentInteraction(Uri uri);
    }
}
