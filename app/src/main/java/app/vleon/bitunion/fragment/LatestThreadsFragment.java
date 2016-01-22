package app.vleon.bitunion.fragment;

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

import com.android.volley.VolleyError;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import java.util.ArrayList;

import app.vleon.bitunion.LatestThreadsAdapter;
import app.vleon.bitunion.MyApplication;
import app.vleon.bitunion.R;
import app.vleon.bitunion.ThreadPostsActivity;
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
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    MyApplication app;
    ArrayList<BuLatestThread> mLatestThreadsList;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
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
        LatestThreadsFragment fragment = new LatestThreadsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
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
        app.getAPI().getLatestThreads();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_latest_threads, container, false);
//        mProgressBar = (ProgressBar)view.findViewById(R.id.request_latest_threads_progress);
        mLatestThreadsRecyclerView = (UltimateRecyclerView) view.findViewById(R.id.latest_threads_recycler_view);
        mLatestThreadsRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLatestThreadsRecyclerView.setLayoutManager(mLayoutManager);
        mLatestThreadsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mLatestThreadsRecyclerView.setAdapter(mAdapter);
        mLatestThreadsRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                mThreadsRecyclerView.setRefreshing(false);
                mLayoutManager.scrollToPosition(0);
                app.getAPI().getLatestThreads();
            }
        });
//        showProgress(true);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onLatestThreadsFragmentInteraction(uri);
        }
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
//        if (clearFlag)
//            mThreadsList.clear();
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
//        showProgress(false);
    }

    @Override
    public void handleLatestThreadsGetterErrorResponse(VolleyError error) {
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

            mLatestThreadsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLatestThreadsRecyclerView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLatestThreadsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mLatestThreadsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
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
    public interface OnLatestThreadsFragmentInteractionListener {
        // TODO: Update argument type and name
        void onLatestThreadsFragmentInteraction(Uri uri);
    }
}
