package app.vleon.bitunion.buapi;

import android.content.Context;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import app.vleon.bitunion.util.MultipartRequest;


public class BuAPI {

    public static final int OUTNET = 1;
    public static final int BITNET = 0;
    private static final int RETRY_NEWTHREAD_FLAG = 5;
    // {"result":"fail","msg":"IP+logged"}
    public static String ROOTURL, BASEURL;
    public static String LOGGING_URL, FORUM_URL, THREAD_URL,
            POST_URL, PROFILE_URL, NEWPOST_URL, NEWTHREAD_URL, LATEST_URL;
    public static String URL_EMOTICON_IMAGE_PREFIX;
    // 如果返回Result为FAIL，msg字段一般为“IP+logged”，说明session失效
    // autoRefreshSession开关决定是否重新刷新session
    final boolean enableRefreshSession = true;
    final int maxRefreshCnt = 2; // 最多重试两次
    private final int RETRY_GETTHREADS_FLAG = 1;
    private final int RETRY_GETPOSTS_FLAG = 2;
    private final int RETRY_GETMEMBER_FLAG = 3;
    private final int RETRY_GETLATEST_FLAG = 4;
    public LoginInfo mLoginInfo;
    int mRetryCount = 0;
    String mUsername;
    String mPassword;
    int mForumFid;
    int mThreadsFrom;
    int mThreadsTo;
    int mThreadTid;
    int mPostsFrom;
    int mPostsTo;
    String mQueryUid;
    Context mContext;
    int mNetType;
    private String mPostSubject;
    private String mPostMessage;
    private RequestQueue mRequestQueue;
    private OnLoginResponseListener mOnLoginResponseListener = null;
    private OnThreadsResponseListener mOnThreadsResponseListener = null;
    private OnPostsResponseListener mOnPostsResponseListener = null;
    private OnMemberInfoResponseListener mOnMemberInfoResponseListener = null;
    private OnPostNewThreadResponseListener mOnPostNewThreadResponseListener = null;
    private Result mThreadsResult = Result.NULL;
    private Result mPostsResult = Result.NULL;
    private Result mMemberResult = Result.NULL;
    private Result mLatestResult = Result.NULL;
    private OnLatestResponseListener mOnLatestResponseListener = null;

    public BuAPI(Context context) {
        mContext = context;
        mRequestQueue = Volley.newRequestQueue(context);
        setNetType(OUTNET);
    }

    private static void buildUrls() {
        BASEURL = ROOTURL + "open_api/";
        LOGGING_URL = BASEURL + "bu_logging.php";
        FORUM_URL = BASEURL + "bu_forum.php";
        THREAD_URL = BASEURL + "bu_thread.php";
        PROFILE_URL = BASEURL + "bu_profile.php";
        POST_URL = BASEURL + "bu_post.php";
        NEWPOST_URL = BASEURL + "bu_newpost.php";
        NEWTHREAD_URL = BASEURL + "bu_newpost.php";
        LATEST_URL = BASEURL + "bu_home.php";
    }

    public static String getAvailableUrl(String rawUrl) {
        String url;
        if (rawUrl.startsWith("http://")) {
            url = rawUrl.replace("http://www.bitunion.org/", ROOTURL);  //// TODO: 2015/11/4
            url = url.replace("http://bitunion.org/", ROOTURL);
        } else if (rawUrl.startsWith(".../")) {
            url = rawUrl.replace("../", ROOTURL);
        } else {
            url = ROOTURL + rawUrl;
        }
        return url;
    }

    public static String formatTime(String timeStr) {
        String format = "yyyy-MM-dd HH:mm";
        return formatTime(timeStr, format);
    }

    public static String formatTime(String timeStr, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.CHINA);
        return dateFormat.format(new Date(Long.valueOf(timeStr) * 1000L));
    }

    public String getUsername() {
        return mUsername;
    }

    public String getPassword() {
        return mPassword;
    }

    public int getNetType() {
        return mNetType;
    }

    public void setNetType(int net) {
        mNetType = net;
        if (net == BITNET) {
            ROOTURL = "http://www.bitunion.org/";
        } else if (net == OUTNET) {
            ROOTURL = "http://out.bitunion.org/";
        }
        buildUrls();
    }

    public Result getLoginResult() {
        Result result = Result.UNKNOWN;
        if (mLoginInfo.result.equals("success")) {
            result = Result.SUCCESS;
        }
        if (mLoginInfo.result.equals("fail")) {
            switch (mLoginInfo.msg) {
                case "IP+logged":
                    // 用户名 密码错误都有可能返回IP+LOGGED
                    result = Result.IP_LOGGED;
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    public LoginInfo getLoginInfo() {
        return mLoginInfo;
    }

    public String getSession() {
        return mLoginInfo.session;
    }

    public HashMap<String, Object> buildPostParams(String action) {
        HashMap<String, Object> params = new HashMap<>();
        switch (action) {
            case "login":
                params.put("action", action);
                params.put("username", mUsername);
                params.put("password", mPassword);
                break;
            case "logout":
                params.put("action", action);
                params.put("username", mUsername);
                params.put("password", mPassword);
                params.put("session", getSession());
                break;
            case "profile":
                params.put("action", action);
                params.put("username", mUsername);
                params.put("session", getSession());
                params.put("uid", mQueryUid);
//        params.put("queryusername", username);
                break;
            case "thread":
                params.put("action", action);
                params.put("username", mUsername);
                params.put("session", getSession());
                params.put("fid", mForumFid + "");
                params.put("from", mThreadsFrom + "");
                params.put("to", mThreadsTo + ""); // to=100, thread+number+error
                break;
            case "post":
                params.put("action", action);
                params.put("username", mUsername);
                params.put("session", getSession());
                params.put("tid", mThreadTid + "");
                params.put("from", mPostsFrom + "");
                params.put("to", mPostsTo + ""); // to=100, thread+number+error
                break;
            case "newthread":
                params.put("action", action);
                params.put("username", mUsername);
                params.put("session", getSession());
                params.put("fid", mForumFid + "");
                break;
            case "latest":
                params.put("username", mUsername);
                params.put("session", getSession());
                break;
        }
        return params;
    }

    /**
     * 论坛登录
     *
     * @param username  用户名
     * @param password  密码
     * @param retryFlag 重试标识
     */
    public void login(String username, String password, final int retryFlag) {
        mUsername = username;
        mPassword = password;
        JsonObjectRequest loginRequest = new JsonObjectRequest(BuAPI.LOGGING_URL,
                new JSONObject(buildPostParams("login")),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        final Gson gson = new Gson();
                        mLoginInfo = gson.fromJson(response.toString(), LoginInfo.class);
                        if ((mOnLoginResponseListener != null) && (retryFlag == 0)) {
                            mOnLoginResponseListener.handleLoginResponse();
                        } else {
                            switch (retryFlag) {
                                case RETRY_GETTHREADS_FLAG:
                                    getThreadsList(mForumFid, mThreadsFrom, mThreadsTo);
                                    break;
                                case RETRY_GETPOSTS_FLAG:
                                    getThreadPosts(mThreadTid, mPostsFrom, mPostsTo);
                                    break;
                                case RETRY_GETMEMBER_FLAG:
                                    getMemberInfo(mQueryUid);
                                    break;
                                case RETRY_NEWTHREAD_FLAG:
                                    try {
                                        postNewThread(mPostSubject, mPostMessage, 0);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if ((mOnLoginResponseListener != null) && (retryFlag == 0)) {
                            mOnLoginResponseListener.handleLoginErrorResponse(error);
                        }
                    }
                });
        mRequestQueue.add(loginRequest);


    }

    public void login(String username, String password) {
        login(username, password, 0);
    }

    /**
     * 注销
     */
    public void logout() {
        final Gson gson = new Gson();
        JsonObjectRequest logoutRequest = new JsonObjectRequest(LOGGING_URL, new JSONObject(buildPostParams("logout")), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        mRequestQueue.add(logoutRequest);
    }

    /**
     * 查询用户信息
     *
     * @param uid 要查询用户的id
     */
    public void getMemberInfo(String uid) {
        mQueryUid = uid;
        final Gson gson = new Gson();
        final JsonObjectRequest memberInfoRequest = new JsonObjectRequest(PROFILE_URL,
                new JSONObject(buildPostParams("profile")), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("TAG", response.toString());
                try {
                    String result = response.getString("result");
                    if (result.equals("success")) {
                        mRetryCount = 0;
                        mMemberResult = Result.SUCCESS;
                        BuMember memberInfo = null;
                        if (response.has("memberinfo")) {
                            String tmp = response.getJSONObject("memberinfo").toString();
                            memberInfo = gson.fromJson(tmp, BuMember.class);
                            memberInfo.parse();
                        } else {
                            mMemberResult = Result.SUCCESS_EMPTY; //成功，但无数据
                        }
                        if (mOnMemberInfoResponseListener != null)
                            mOnMemberInfoResponseListener.handleMemberInfoGetterResponse(mMemberResult,
                                    memberInfo);
                    } else {
                        String msg = response.getString("msg");
                        switch (msg) {
                            case "IP+logged":
                                // session失效时，返回该msg，需要重新获取session
                                if (mRetryCount < 1) {
//                                    Toast.makeText(mContext, "retry", Toast.LENGTH_SHORT).show();
                                    login(mUsername, mPassword, RETRY_GETMEMBER_FLAG);
                                    mRetryCount++;
                                } else {
                                    //重试一次之后仍然返回IP LOGGED，不再重试
                                    mMemberResult = Result.IP_LOGGED;
                                    mRetryCount = 0;
                                }
                                break;
                            default:
                                break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        mRequestQueue.add(memberInfoRequest);
    }

    /**
     * 查询当前用户信息
     */
    public void getMyInfo() {
        getMemberInfo(mLoginInfo.uid);
    }

    /**
     * 查询论坛帖子
     *
     * @param fid  论坛版块id
     * @param from 帖子起始编号，最新帖子编号为0
     * @param to   帖子结束编号 to-from <=20
     */
    public void getThreadsList(final int fid, final int from, final int to) {
        mForumFid = fid;
        mThreadsFrom = from;
        mThreadsTo = to;
        final Gson gson = new Gson();
        JsonObjectRequest threadsRequest = new JsonObjectRequest(THREAD_URL,
                new JSONObject(buildPostParams("thread")),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("TAG", response.toString());
                        try {
                            String result = response.getString("result");
                            if (result.equals("success")) {
                                mRetryCount = 0;
                                ArrayList<BuThread> templist = null;
                                if (response.has("threadlist")) {
                                    mThreadsResult = Result.SUCCESS;
                                    String tmp = response.getJSONArray("threadlist").toString();
                                    templist = gson.fromJson(tmp, new TypeToken<List<BuThread>>() {
                                    }.getType());
                                } else {
                                    mThreadsResult = Result.SUCCESS_EMPTY;   //成功，但无数据
                                }
                                if (mOnThreadsResponseListener != null)
                                    mOnThreadsResponseListener.handleThreadsGetterResponse(mThreadsResult, templist);
                            } else {
                                String msg = response.getString("msg");
                                switch (msg) {
                                    case "thread+number+error":
                                        mThreadsResult = Result.NUMBER_ERROR;
                                        break;
                                    case "IP+logged":
                                        // session失效时，返回该msg，需要重新获取session
                                        if (mRetryCount < 1) {
//                                            Toast.makeText(mContext, "retry", Toast.LENGTH_SHORT).show();
                                            login(mUsername, mPassword, RETRY_GETTHREADS_FLAG);
                                            mRetryCount++;
                                        } else {
                                            //重试一次之后仍然返回IP LOGGED，不再重试
                                            mThreadsResult = Result.IP_LOGGED;
                                            mRetryCount = 0;
                                        }
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
                        if (mOnThreadsResponseListener != null)
                            mOnThreadsResponseListener.handleThreadsGetterErrorResponse(error);
                    }
                });
        mRequestQueue.add(threadsRequest);
    }

    /**
     * 查询论坛最新帖子
     */
    public void getLatestThreads() {
        JsonObjectRequest latestRequest = new JsonObjectRequest(BuAPI.LATEST_URL,
                new JSONObject(buildPostParams("latest")),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("TAG", response.toString());
                        try {
                            String result = response.getString("result");
                            if (result.equals("success")) {
                                mRetryCount = 0;
                                final Gson gson = new Gson();
                                ArrayList<BuLatestThread> tempList = null;
                                if (response.has("newlist")) {
                                    mLatestResult = Result.SUCCESS;
                                    String tmp = response.getJSONArray("newlist").toString();
                                    tempList = gson.fromJson(tmp, new TypeToken<List<BuLatestThread>>() {
                                    }.getType());
                                    for (BuLatestThread latestThread : tempList) {
                                        latestThread.parse();
                                    }
                                } else {
                                    mLatestResult = Result.SUCCESS_EMPTY;   //成功，但无数据
                                }
                                if (mOnLatestResponseListener != null)
                                    mOnLatestResponseListener.handleLatestThreadsGetterResponse(mLatestResult, tempList);
                            } else {
                                String msg = response.getString("msg");
                                switch (msg) {
                                    case "thread+number+error":
                                        mLatestResult = Result.NUMBER_ERROR;
                                        break;
                                    case "IP+logged":
                                        // session失效时，返回该msg，需要重新获取session
                                        if (mRetryCount < 1) {
//                                            Toast.makeText(mContext, "retry", Toast.LENGTH_SHORT).show();
                                            login(mUsername, mPassword, RETRY_GETLATEST_FLAG);
                                            mRetryCount++;
                                        } else {
                                            //重试一次之后仍然返回IP LOGGED，不再重试
                                            mLatestResult = Result.IP_LOGGED;
                                            mRetryCount = 0;
                                        }
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
                        if (mOnLatestResponseListener != null)
                            mOnLatestResponseListener.handleLatestThreadsGetterErrorResponse(error);
                    }
                });
        mRequestQueue.add(latestRequest);
    }

    /**
     * 查询帖子详情
     *
     * @param thread 主题帖子
     * @param from   帖子起始编号，最新帖子编号为0
     * @param to     帖子结束编号 to-from <=20
     */
    public void getThreadPosts(BuThread thread, final int from, final int to) {
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
        mThreadTid = tid;
        mPostsFrom = from;
        mPostsTo = to;
        final Gson gson = new Gson();
        JsonObjectRequest postsRequest = new JsonObjectRequest(BuAPI.POST_URL,
                new JSONObject(buildPostParams("post")),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("TAG", response.toString());
                        try {
                            String result = response.getString("result");
                            if (result.equals("success")) {
                                mRetryCount = 0;
                                ArrayList<BuPost> tempList = null;
                                if (response.has("postlist")) {
                                    mPostsResult = Result.SUCCESS;
                                    String tmp = response.getJSONArray("postlist").toString();
                                    tempList = gson.fromJson(tmp, new TypeToken<List<BuPost>>() {
                                    }.getType());
                                    for (BuPost post : tempList) {
                                        post.parse();
                                    }
                                } else {
                                    mPostsResult = Result.SUCCESS_EMPTY;   //成功，但无数据
                                }
                                if (mOnPostsResponseListener != null)
                                    mOnPostsResponseListener.handlePostsGetterResponse(mPostsResult, tempList);
                            } else {
                                String msg = response.getString("msg");
                                switch (msg) {
                                    case "thread+number+error":
                                        mPostsResult = Result.NUMBER_ERROR;
                                        break;
                                    case "IP+logged":
                                        // session失效时，返回该msg，需要重新获取session
                                        if (mRetryCount < 1) {
//                                            Toast.makeText(mContext, "retry", Toast.LENGTH_SHORT).show();
                                            login(mUsername, mPassword, RETRY_GETPOSTS_FLAG);
                                            mRetryCount++;
                                        } else {
                                            //重试一次之后仍然返回IP LOGGED，不再重试
                                            mPostsResult = Result.IP_LOGGED;
                                            mRetryCount = 0;
                                        }
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
                        if (mOnPostsResponseListener != null)
                            mOnPostsResponseListener.handlePostsGetterErrorResponse(error);
                    }
                });
        mRequestQueue.add(postsRequest);
    }

    /**
     * 发表新帖
     *
     * @param subject    标题
     * @param message    内容
     * @param attachment 有无附件  1，0
     * @throws IOException
     */
    public void postNewThread(String subject, String message, int attachment) throws IOException {
        mPostSubject = subject;
        mPostMessage = message;
        Map<String, Object> data = buildPostParams("newthread");
        data.put("subject", subject);
        data.put("message", message);
        data.put("attachment", 1);
        JSONObject jsonObject = new JSONObject(data);

        String url = NEWTHREAD_URL;

        final String twoHyphens = "--";
        final String lineEnd = "\r\n";
        final String boundary = "----BitunionAndroidKit";
        String mimeType = "multipart/form-data;boundary=" + boundary;
        HashMap<String, String> headers = new HashMap<String, String>() {
            {
                put("Connection", "keep-alive");
                put("Charset", "UTF-8");
                put("Content-Type", "multipart/form-data; boundary=" + boundary);
            }
        };

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        dos.writeBytes(twoHyphens + boundary + lineEnd);
        dos.writeBytes("Content-Disposition: form-data; name=\"json\"" + lineEnd);
//        dos.writeBytes("Content-Type: multipart/form-data; charset=UTF-8" + lineEnd);
//        dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\"; filename=\""
//                + fileName + "\"" + lineEnd);
        dos.writeBytes(lineEnd);
        dos.writeBytes(jsonObject.toString() + lineEnd);
        dos.writeBytes("--" + boundary + "--" + lineEnd);
        byte[] multipartBody = bos.toByteArray();
//        ByteArrayInputStream fileInputStream = new ByteArrayInputStream(fileData);
//        int bytesAvailable = fileInputStream.available();
//
//        int maxBufferSize = 1024 * 1024;
//        int bufferSize = Math.min(bytesAvailable, maxBufferSize);
//        byte[] buffer = new byte[bufferSize];
//
//        // read file and write it into form...
//        int bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//
//        while (bytesRead > 0) {
//            dos.write(buffer, 0, bufferSize);
//            bytesAvailable = fileInputStream.available();
//            bufferSize = Math.min(bytesAvailable, maxBufferSize);
//            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//        }

        MultipartRequest multipartRequest = new MultipartRequest(url, headers, mimeType, multipartBody, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                if (response.statusCode == 200) {
                    String jsonstr = new String(response.data, StandardCharsets.UTF_8).trim();
                    try {
                        JSONObject obj = new JSONObject(jsonstr);
                        if (obj.getString("result").equals("success")) {
//                            Toast.makeText(mContext, "success", Toast.LENGTH_SHORT).show();
                            if (mOnPostNewThreadResponseListener != null)
                                mOnPostNewThreadResponseListener.handlePostNewThreadResponse(Result.SUCCESS, obj.getString("tid"));
                        } else {
                            if (mRetryCount < 1) {
//                                            Toast.makeText(mContext, "retry", Toast.LENGTH_SHORT).show();
                                login(mUsername, mPassword, RETRY_NEWTHREAD_FLAG);
                                mRetryCount++;
                            } else {
                                //重试一次之后仍然返回IP LOGGED，不再重试
//                                mPostsResult = Result.IP_LOGGED;
                                if (mOnPostNewThreadResponseListener != null)
                                    mOnPostNewThreadResponseListener.handlePostNewThreadResponse(Result.FAILURE, null);
                                mRetryCount = 0;
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (mOnPostNewThreadResponseListener != null)
                    mOnPostNewThreadResponseListener.handlePostNewThreadErrorResponse(error);
            }
        });
        mRequestQueue.add(multipartRequest);
    }

    public void setOnLoginResponseListener(OnLoginResponseListener lrl) {
        mOnLoginResponseListener = lrl;
    }

    public void setOnLatestThreadsResponseListener(OnLatestResponseListener ltrl) {
        mOnLatestResponseListener = ltrl;
    }

    public void setOnMemberInfoResponseListener(OnMemberInfoResponseListener mrl) {
        mOnMemberInfoResponseListener = mrl;
    }

    public void setOnThreadsResponseListener(OnThreadsResponseListener trl) {
        mOnThreadsResponseListener = trl;
    }

    public void setOnPostsResponseListener(OnPostsResponseListener prl) {
        mOnPostsResponseListener = prl;
    }

    public void setOnPostNewThreadResponseListener(OnPostNewThreadResponseListener pntrl) {
        mOnPostNewThreadResponseListener = pntrl;
    }

    public enum Result {
        SUCCESS, // 返回数据成功，result字段为success
        FAILURE, // 返回数据失败，result字段为failure
        IP_LOGGED, //返回数据失败，msg字段为ip+logged
        SUCCESS_EMPTY, // 返回数据成功，但字段没有数据
        SESSIONLOGIN, // obsolete
        NETWRONG, // 没有返回数据
        NOTLOGIN, // api还未登录
        NUMBER_ERROR,  // TO - FROM > 20
        UNKNOWN,   //未知错误代码
        NULL   //还未返回结果
    }

    /**
     * Listener Definition below
     */
    public interface OnLoginResponseListener {
        void handleLoginResponse();

        void handleLoginErrorResponse(VolleyError error);
    }

    public interface OnMemberInfoResponseListener {
        void handleMemberInfoGetterResponse(Result result, BuMember memberInfo);

        void handleMemberInfoGetterErrorResponse(VolleyError error);
    }

    public interface OnLatestResponseListener {
        void handleLatestThreadsGetterResponse(Result result, ArrayList<BuLatestThread> latestThreadsList);

        void handleLatestThreadsGetterErrorResponse(VolleyError error);
    }

    public interface OnThreadsResponseListener {
        void handleThreadsGetterResponse(Result result, ArrayList<BuThread> threadsList);

        void handleThreadsGetterErrorResponse(VolleyError error);
    }

    public interface OnPostsResponseListener {
        void handlePostsGetterResponse(Result result, ArrayList<BuPost> postsList);

        void handlePostsGetterErrorResponse(VolleyError error);
    }

    public interface OnPostNewThreadResponseListener {
        void handlePostNewThreadResponse(Result result, String tid);

        void handlePostNewThreadErrorResponse(VolleyError error);
    }

    public class LoginInfo {
        public String result;
        public String uid;
        public String username;
        public String session;
        public String status;
        public String credit;
        public String lastactivity;
        public String msg; // TODO: 2015/11/6
    }

}

