package app.vleon.buapi;

import android.content.Context;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


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

    public enum Result {
        SUCCESS, // 返回数据成功，result字段为success
        FAILURE, // 返回数据失败，result字段为failure
        SUCCESS_EMPTY, // 返回数据成功，但字段没有数据
        SESSIONLOGIN, // obsolete
        NETWRONG, // 没有返回数据
        NOTLOGIN, // api还未登录
        UNKNOWN
        // 根据ordinal值获得枚举类型
        // public static Result valueOf(int ordinal) {
        // if (ordinal < 0 || ordinal >= values().length)
        // return UNKNOWN;
        // return values()[ordinal];
        // }
    }

    String mUsername, mPassword;
    String mSession;
    boolean isLogined;

    public static LoginInfo loginInfo;

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

    public BuAPI() {
    }

    public BuAPI(Context context, String username, String password) {
        this(context, username, password, OUTNET);
    }

    public BuAPI(Context context, String username, String password, int net) {
        this.mUsername = username;
        this.mPassword = password;
        this.isLogined = false;
        setNetType(net);
    }

    public BuAPI(String username, String password, String session) {
        this.mUsername = username;
        this.mPassword = password;
        this.mSession = session;
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

    public static String getImageAbsoluteUrl(String shortUrl) {
        String path;
        path = shortUrl;
        path = path.replaceAll("(http://)?(www|v6|kiss|out).bitunion.org/", ROOTURL);
        path = path.replaceAll("^images/", ROOTURL + "images/");
        path = path.replaceAll("^attachments/", ROOTURL + "attachments/");
        return path;
    }

    public int getError() {
        return mError;
    }

    public int getNetType() {
        return mNetType;
    }

    public static String formatTime(String timeStr) {
        String format = "yyyy-MM-dd HH:mm";
        return formatTime(timeStr, format);
    }

    public static String formatTime(String timeStr, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.CHINA);
        return dateFormat.format(new Date(Long.valueOf(timeStr) * 1000L));
    }
}

class LoginInfo {
    String result;
    String uid;
    String username;
    String session;
    String status;
    String credit;
    String lastactivity;
}
