package app.vleon.buapi;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vleon on 2016/1/16.
 */
public class BuMember {
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
        BuAPI.formatTime(regdate);
        BuAPI.formatTime(lastvisit);
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