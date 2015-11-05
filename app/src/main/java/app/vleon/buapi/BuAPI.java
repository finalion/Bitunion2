package app.vleon.buapi;

import android.content.Context;
import android.util.Log;

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
            POST_URL, REQUEST_PROFILE, NEWPOST, NEWTHREAD;

    public static String URL_EMOTICON_IMAGE_PREFIX;
//    public static String URL_EMOTICON_IMAGE_PREFIX;

    // 如果返回Result为FAIL，msg字段一般为“IP+logged”，说明session失效
    // autoRefreshSession开关决定是否重新刷新session
    final boolean enableRefreshSession = true;
    final int maxRefreshCnt = 2; // 最多重试两次
    int refreshCnt = 0;

    private RequestQueue mRequestQueue;
    private OnLoginResponseListener mOnLoginResponseListener = null;
    private OnThreadsResponseListener mOnThreadsResponseListener = null;
    private OnPostsResponseListener mOnPostsResponseListener = null;

    public Result getLoginResult() {
        if (mLoginInfo.result.equals("success")) {
            return Result.SUCCESS;
        } else {
            switch (mLoginInfo.msg) {
                default:
                    break;
            }
        }
        return Result.FAILURE;
    }

    public enum Result {
        SUCCESS, // 返回数据成功，result字段为success
        FAILURE, // 返回数据失败，result字段为failure
        IP_LOGGED, //返回数据失败，msg字段为ip+logged
        SUCCESS_EMPTY, // 返回数据成功，但字段没有数据
        SESSIONLOGIN, // obsolete
        NETWRONG, // 没有返回数据
        NOTLOGIN, // api还未登录
        UNKNOWN
    }

    String mUsername, mPassword;
    String mSession;
    boolean isLogined;

    public LoginInfo mLoginInfo;
    Context mContext;

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
        REQUEST_PROFILE = BASEURL + "bu_profile.php";
        POST_URL = BASEURL + "bu_post.php";
        NEWPOST = BASEURL + "bu_newpost.php";
        NEWTHREAD = BASEURL + "bu_newpost.php";
    }

    int flagCnt = 0;
    int mError = NONE;
    int mNetType;
    private JSONObject mForumJSON;

    public BuAPI(Context context) {
        mContext = context;
        mRequestQueue = Volley.newRequestQueue(context);
        setNetType(OUTNET);
    }

    public void setNetType(int net) {
        mNetType = net;
        if (net == BITNET) {
            ROOTURL = "http://www.bitunion.org/";
        } else if (net == OUTNET) {
            ROOTURL = "http://out.bitunion.org/";
        }
        BASEURL = ROOTURL + "open_api/";
        LOGGING_URL = BASEURL + "bu_logging.php";
        FORUM_URL = BASEURL + "bu_forum.php";
        THREAD_URL = BASEURL + "bu_thread.php";
        REQUEST_PROFILE = BASEURL + "bu_profile.php";
        POST_URL = BASEURL + "bu_post.php";
        NEWPOST = BASEURL + "bu_newpost.php";
        NEWTHREAD = BASEURL + "bu_newpost.php";
    }

    public String getSession() {
        return mLoginInfo.session;
    }

    /**
     * 论坛登录
     *
     * @param username 用户名
     * @param password 密码
     */
    public void login(String username, String password) {
        HashMap<String, String> params = new HashMap<>();
        params.put("action", "login");
        params.put("username", username);
        params.put("password", password);
        final Gson gson = new Gson();
        JsonObjectRequest mLoginRequest = new JsonObjectRequest(BuAPI.LOGGING_URL,
                new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        final Gson gson = new Gson();
                        mLoginInfo = gson.fromJson(response.toString(), LoginInfo.class);
                        if (mOnLoginResponseListener != null)
                            mOnLoginResponseListener.handleLoginResponse();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (mOnLoginResponseListener != null)
                            mOnLoginResponseListener.handleLoginErrorResponse(error);
                    }
                });
        mRequestQueue.add(mLoginRequest);
    }

    /**
     * 查询论坛帖子
     *
     * @param fid  论坛版块id
     * @param from 帖子起始编号，最新帖子编号为0
     * @param to   帖子结束编号 to-from <=20
     */
    public void getThreadsList(final int fid, final int from, final int to) {
        HashMap<String, String> params = new HashMap<>();
        params.put("action", "thread");
        params.put("username", mLoginInfo.username);
        params.put("session", mLoginInfo.session);
        params.put("fid", fid + "");
        params.put("from", from + "");
        params.put("to", to + ""); // to=100, thread+number+error
        final Gson gson = new Gson();
        JsonObjectRequest mLoginRequest = new JsonObjectRequest(THREAD_URL,
                new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    String result;
                    String msg = "success";
                    String list;

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("TAG", response.toString());
                        try {
                            result = response.getString("result");
                            if (result.equals("success")) {
                                list = response.getJSONArray("threadlist").toString();
                                ArrayList<ThreadInfo> templist = gson.fromJson(list, new TypeToken<List<ThreadInfo>>() {
                                }.getType());
                                if (mOnThreadsResponseListener != null)
                                    mOnThreadsResponseListener.handleThreadsGetterResponse(templist);
                            } else {
                                msg = response.getString("msg");
                                switch (msg) {
                                    case "thread+number+error":
                                        break;
                                    case "IP+logged":
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
        mRequestQueue.add(mLoginRequest);
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
        HashMap<String, String> params = new HashMap<>();
        params.put("action", "post");
        params.put("username", mLoginInfo.username);
        params.put("session", mLoginInfo.session);
        params.put("tid", tid + "");
        params.put("from", from + "");
        params.put("to", to + ""); // to=100, thread+number+error
        final Gson gson = new Gson();
        JsonObjectRequest mLoginRequest = new JsonObjectRequest(BuAPI.POST_URL,
                new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    String result;
                    String msg = "success";

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("TAG", response.toString());
                        try {
                            result = response.getString("result");
                            if (result.equals("success") && response.has("postlist")) {
                                String tmp = response.getJSONArray("postlist").toString();
                                ArrayList<PostInfo> tempList = gson.fromJson(tmp, new TypeToken<List<PostInfo>>() {
                                }.getType());
                                for (PostInfo post : tempList) {
                                    post.parse();
                                }
                                if (mOnPostsResponseListener != null)
                                    mOnPostsResponseListener.handlePostsGetterResponse(tempList);
                            } else {
                                msg = response.getString("msg");
                                switch (msg) {
                                    case "thread+number+error":
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
        mRequestQueue.add(mLoginRequest);
    }

    public interface OnLoginResponseListener {
        void handleLoginResponse();

        void handleLoginErrorResponse(VolleyError error);
    }

    public interface OnThreadsResponseListener {
        void handleThreadsGetterResponse(ArrayList<ThreadInfo> threadsList);

        void handleThreadsGetterErrorResponse(VolleyError error);
    }

    public interface OnPostsResponseListener {
        void handlePostsGetterResponse(ArrayList<PostInfo> postsList);

        void handlePostsGetterErrorResponse(VolleyError error);
    }

    public void setOnLoginResponseListener(OnLoginResponseListener lrl) {
        mOnLoginResponseListener = lrl;
    }

    public void setOnThreadsResponseListener(OnThreadsResponseListener lrl) {
        mOnThreadsResponseListener = lrl;
    }

    public void setOnPostsResponseListener(OnPostsResponseListener lrl) {
        mOnPostsResponseListener = lrl;
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
        String pstatus;
        public String lastedit;
        String aaid;
        String creditsrequire;
        String filetype;
        String filename;
        String attachment;
        String filesize;
        String downloads;
        public String uid;
        public String username;
        public String avatar;
        String epid;
        String maskpost;
        public String content;
        public String trueAvatar;

        public ArrayList<Quote> quotes;

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

