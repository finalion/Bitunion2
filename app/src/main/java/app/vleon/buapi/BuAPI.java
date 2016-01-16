package app.vleon.buapi;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class BuAPI {
    public static final int NETERROR = -1;
    public static final int SESSIONERROR = 0;
    public static final int NONE = 1;
    public static final int UNKNOWNERROR = -2;
    public static final int OUTNET = 1;
    public static final int BITNET = 0;
    // {"result":"fail","msg":"IP+logged"}
    public static String ROOTURL, BASEURL;
    public static String LOGGING_URL, FORUM_URL, THREAD_URL,
            POST_URL, PROFILE_URL, NEWPOST, NEWTHREAD, LATEST_URL;
    public static String URL_EMOTICON_IMAGE_PREFIX;
    // 如果返回Result为FAIL，msg字段一般为“IP+logged”，说明session失效
    // autoRefreshSession开关决定是否重新刷新session
    final boolean enableRefreshSession = true;
    final int maxRefreshCnt = 2; // 最多重试两次
    private final int RETRY_GETTHREADS_FLAG = 1;
    private final int RETRY_GETPOSTS_FLAG = 2;
    private final int RETRY_GETINFO_FLAG = 3;
    public LoginInfo mLoginInfo;
    int mRetryCount = 0;
    String mUsername, mPassword;
    String mSession;
    boolean isLogined;
    int mThreadsFid;
    int mThreadsFrom;
    int mThreadsTo;
    int mPostsTid;
    int mPostsFrom;
    int mPostsTo;
    Context mContext;
    int flagCnt = 0;
    int mError = NONE;
    int mNetType;
    private RequestQueue mRequestQueue;
    private OnLoginResponseListener mOnLoginResponseListener = null;
    private OnThreadsResponseListener mOnThreadsResponseListener = null;
    private OnPostsResponseListener mOnPostsResponseListener = null;
    private OnMemberInfoResponseListener mOnMemberInfoResponseListener = null;
    private Result mThreadsResult = Result.NULL;
    private Result mPostsResult = Result.NULL;
    private Result mLatestResult = Result.NULL;
    private OnLatestResponseListener mOnLatestResponseListener;

    public BuAPI(Context context) {
        mContext = context;
        mRequestQueue = Volley.newRequestQueue(context);
        setNetType(OUTNET);
    }

    public static void setInnerNet() {
        ROOTURL = "http://www.bitunion.org/";
        buildUrls();
    }

    public static void setOuterNet() {
        ROOTURL = "http://out.bitunion.org/";
        buildUrls();
    }

    private static void buildUrls() {
        BASEURL = ROOTURL + "open_api/";
        LOGGING_URL = BASEURL + "bu_logging.php";
        FORUM_URL = BASEURL + "bu_forum.php";
        THREAD_URL = BASEURL + "bu_thread.php";
        PROFILE_URL = BASEURL + "bu_profile.php";
        POST_URL = BASEURL + "bu_post.php";
        NEWPOST = BASEURL + "bu_newpost.php";
        NEWTHREAD = BASEURL + "bu_newpost.php";
        LATEST_URL = BASEURL + "bu_home.php";
    }

    public static String getImageAbsoluteUrl(String shortUrl) {
        String path;
        path = shortUrl;
        path = path.replaceAll("(http://)?(www|v6|kiss|out).bitunion.org/", ROOTURL);
        path = path.replaceAll("^images/", ROOTURL + "images/");
        path = path.replaceAll("^attachments/", ROOTURL + "attachments/");
        return path;
    }

    public static String formatTime(String timeStr) {
        String format = "yyyy-MM-dd HH:mm";
        return formatTime(timeStr, format);
    }

    public static String formatTime(String timeStr, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.CHINA);
        return dateFormat.format(new Date(Long.valueOf(timeStr) * 1000L));
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

    public Result getThreadsResult() {
        return mThreadsResult;
    }

    public LoginInfo getLoginInfo() {
        return mLoginInfo;
    }

    public void updateSession() {

    }

    public String getSession() {
        return mLoginInfo.session;
    }

    private HashMap<String, String> getPostParams(String action) {
        HashMap<String, String> params = new HashMap<>();
        params.put("action", action);
        switch (action) {
            case "login":
//                params.put("username", username);
//                params.put("password", password);
                break;
            case "logout":
                break;
            case "profile":
                break;
            case "thread":
                break;
            case "post":
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
        HashMap<String, String> params = new HashMap<>();
        params.put("action", "login");
        params.put("username", username);
        params.put("password", password);
        JsonObjectRequest loginRequest = new JsonObjectRequest(BuAPI.LOGGING_URL,
                new JSONObject(params),
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
                                    getThreadsList(mThreadsFid, mThreadsFrom, mThreadsTo);
                                    break;
                                case RETRY_GETPOSTS_FLAG:
                                    getThreadPosts(mPostsTid, mPostsFrom, mPostsTo);
                                    break;
                                case RETRY_GETINFO_FLAG:
//                                    getMemberInfo();
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
        HashMap<String, String> params = new HashMap<>();
        params.put("action", "logout");
        params.put("username", mUsername);
        params.put("password", mPassword);
        params.put("session", getSession());
        final Gson gson = new Gson();
        JsonObjectRequest logoutRequest = new JsonObjectRequest(LOGGING_URL, new JSONObject(params), new Response.Listener<JSONObject>() {
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
        HashMap<String, String> params = new HashMap<>();
        params.put("action", "profile");
        params.put("username", mLoginInfo.username);
        params.put("session", mLoginInfo.session);
        params.put("uid", uid);
//        params.put("queryusername", username);
        final Gson gson = new Gson();
        final JsonObjectRequest memberInfoRequest = new JsonObjectRequest(PROFILE_URL,
                new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("TAG", response.toString());
                try {
                    String result = response.getString("result");
                    if (result.equals("success")) {
                        mRetryCount = 0;
                        MemberInfo memberInfo = null;
                        if (response.has("memberinfo")) {
                            String tmp = response.getJSONObject("memberinfo").toString();
                            memberInfo = gson.fromJson(tmp, MemberInfo.class);
                            memberInfo.parse();
                        } else {
//                                    mThreadsResult = Result.SUCCESS_EMPTY;   //成功，但无数据
                        }
                        if (mOnMemberInfoResponseListener != null)
                            mOnMemberInfoResponseListener.handleMemberInfoGetterResponse(Result.SUCCESS,
                                    memberInfo);
                    } else {
                        String msg = response.getString("msg");
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
        mThreadsFid = fid;
        mThreadsFrom = from;
        mThreadsTo = to;
        HashMap<String, String> params = new HashMap<>();
        params.put("action", "thread");
        params.put("username", mLoginInfo.username);
        params.put("session", mLoginInfo.session);
        params.put("fid", fid + "");
        params.put("from", from + "");
        params.put("to", to + ""); // to=100, thread+number+error
        final Gson gson = new Gson();
        JsonObjectRequest threadsRequest = new JsonObjectRequest(THREAD_URL,
                new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("TAG", response.toString());
                        try {
                            String result = response.getString("result");
                            if (result.equals("success")) {
                                mRetryCount = 0;
                                ArrayList<ThreadInfo> templist = null;
                                if (response.has("threadlist")) {
                                    mThreadsResult = Result.SUCCESS;
                                    String tmp = response.getJSONArray("threadlist").toString();
                                    templist = gson.fromJson(tmp, new TypeToken<List<ThreadInfo>>() {
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
                                            Toast.makeText(mContext, "retry", Toast.LENGTH_SHORT).show();
                                            login(mUsername, mPassword, RETRY_GETTHREADS_FLAG);
                                        } else {
                                            //重试一次之后仍然返回IP LOGGED，不再重试
                                            mThreadsResult = Result.IP_LOGGED;
                                            mRetryCount = 0;
                                        }
                                        mRetryCount++;
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
        HashMap<String, String> params = new HashMap<>();
        params.put("username", mLoginInfo.username);
        params.put("session", mLoginInfo.session);
        JsonObjectRequest latestRequest = new JsonObjectRequest(BuAPI.LATEST_URL,
                new JSONObject(params),
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
                                            Toast.makeText(mContext, "retry", Toast.LENGTH_SHORT).show();
                                            login(mUsername, mPassword, RETRY_GETPOSTS_FLAG);
                                        } else {
                                            //重试一次之后仍然返回IP LOGGED，不再重试
                                            mLatestResult = Result.IP_LOGGED;
                                            mRetryCount = 0;
                                        }
                                        mRetryCount++;
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
        mPostsTid = tid;
        mPostsFrom = from;
        mPostsTo = to;
        HashMap<String, String> params = new HashMap<>();
        params.put("action", "post");
        params.put("username", mLoginInfo.username);
        params.put("session", mLoginInfo.session);
        params.put("tid", tid + "");
        params.put("from", from + "");
        params.put("to", to + ""); // to=100, thread+number+error
        final Gson gson = new Gson();
        JsonObjectRequest postsRequest = new JsonObjectRequest(BuAPI.POST_URL,
                new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("TAG", response.toString());
                        try {
                            String result = response.getString("result");
                            if (result.equals("success")) {
                                mRetryCount = 0;
                                ArrayList<PostInfo> tempList = null;
                                if (response.has("postlist")) {
                                    mPostsResult = Result.SUCCESS;
                                    String tmp = response.getJSONArray("postlist").toString();
                                    tempList = gson.fromJson(tmp, new TypeToken<List<PostInfo>>() {
                                    }.getType());
                                    for (PostInfo post : tempList) {
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
                                            Toast.makeText(mContext, "retry", Toast.LENGTH_SHORT).show();
                                            login(mUsername, mPassword, RETRY_GETPOSTS_FLAG);
                                        } else {
                                            //重试一次之后仍然返回IP LOGGED，不再重试
                                            mPostsResult = Result.IP_LOGGED;
                                            mRetryCount = 0;
                                        }
                                        mRetryCount++;
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

    public void setOnLoginResponseListener(OnLoginResponseListener lrl) {
        mOnLoginResponseListener = lrl;
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

    public interface OnLoginResponseListener {
        void handleLoginResponse();

        void handleLoginErrorResponse(VolleyError error);
    }

    public interface OnMemberInfoResponseListener {
        void handleMemberInfoGetterResponse(Result result, MemberInfo memberInfo);

        void handleMemberInfoGetterErrorResponse(VolleyError error);
    }

    public interface OnLatestResponseListener {
        void handleLatestThreadsGetterResponse(Result result, ArrayList<BuLatestThread> latestThreadsList);

        void handleLatestThreadsGetterErrorResponse(VolleyError error);
    }

    public interface OnThreadsResponseListener {
        void handleThreadsGetterResponse(Result result, ArrayList<ThreadInfo> threadsList);

        void handleThreadsGetterErrorResponse(VolleyError error);
    }

    public interface OnPostsResponseListener {
        void handlePostsGetterResponse(Result result, ArrayList<PostInfo> postsList);

        void handlePostsGetterErrorResponse(VolleyError error);
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

    public class MemberInfo {
        public String uid;
        public String status;
        public String username;
        public String avatar;
        public String credit;
        public String regdate;
        public String lastvisit;
        public String bday;
        public String signature;
        public String postnum;
        public String threadnum;
        public String email;
        public String oicq;
        public String msn;

        public void parse() {
            formatTime(regdate);
            formatTime(lastvisit);
            try {
                username = URLDecoder.decode(username, "utf-8");
                email = URLDecoder.decode(email, "utf-8");
                signature = URLDecoder.decode(signature, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        //得到头像真实的URL
        public String getTrueAvatar2() {
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

        //得到头像真实的URL
        public String getTrueAvatar() {
            try {
                avatar = URLDecoder.decode(avatar, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return "";
            }
            if (avatar.startsWith("http://")) {
                avatar = avatar.replace("http://www.bitunion.org", "http://out.bitunion.org");  //// TODO: 2015/11/4
                avatar = avatar.replace("http://bitunion.org", "http://out.bitunion.org");
                return avatar;
            } else {
                return BuAPI.ROOTURL + avatar;
            }
        }
    }

    public class ThreadInfo {
        public String tid;
        public String author;
        public String authorid;
        public String subject;
        public String dateline;
        public String lastpost;
        public String lastposter;
        public String views;
        public String replies;
    }

    public class PostInfo {
        public String pid;
        public String fid;
        public String tid;
        public String aid;
        public String author;
        public String authorid;
        public String subject;
        public String dateline;
        public String message;
        public String usesig;
        public String bbcodeoff;
        public String smileyoff;
        public String parseurloff;
        public String score;
        public String rate;
        public String ratetimes;
        public String lastedit;
        public String uid;
        public String username;
        public String avatar;
        public String content;
        public String trueAvatar;
        public ArrayList<Quote> quotes;
        String pstatus;
        String aaid;
        String creditsrequire;
        String filetype;
        String filename;
        String attachment;
        String filesize;
        String downloads;
        String epid;
        String maskpost;

        public void parse() {
            try {
                message = URLDecoder.decode(message, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            this.quotes = new ArrayList<>();
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

    public class Quote {
        public String quoteAuthor;
        public String quoteTime;
        public String quoteContent;

        public Quote(String author, String time, String content) {
            quoteAuthor = author;
            quoteTime = time;
            quoteContent = content;
        }
    }

}

