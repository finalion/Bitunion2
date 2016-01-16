package app.vleon.buapi;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vleon on 2016/1/16.
 */
public class BuPostInfo {
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
            avatar = URLDecoder.decode(avatar, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.avatar = getTrueAvatar();
        this.quotes = new ArrayList<>();
        this.content = parseQuotes(removeBlankLines(message));
    }

    //得到头像真实的URL
    public String getTrueAvatar() {
        Pattern p = Pattern.compile("<img src=\"(.*?)\"  border=\"0\">", Pattern.DOTALL);
        Matcher m = p.matcher(avatar);
        String finder;
        while (m.find()) {
            finder = m.group(1);
            return BuAPI.getImageAbsoluteUrl(finder);
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