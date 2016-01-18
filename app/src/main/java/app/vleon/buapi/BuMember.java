package app.vleon.buapi;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

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
        regdate = BuAPI.formatTime(regdate);
        lastvisit = BuAPI.formatTime(lastvisit);
        try {
            username = URLDecoder.decode(username, "utf-8");
            email = URLDecoder.decode(email, "utf-8");
            signature = URLDecoder.decode(signature, "utf-8");
            msn = URLDecoder.decode(msn, "utf-8");
            avatar = URLDecoder.decode(avatar, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        avatar = BuAPI.getAvailableUrl(avatar);
    }

}