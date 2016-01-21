package app.vleon.bitunion.buapi;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vleon on 2016/1/16.
 */
public class BuPost {
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
    // define
    public ArrayList<Quote> quotes;
    public String content;
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

    public boolean hasAttachment() {
        return attachment != null;
    }

    public void parse() {
        try {
            message = URLDecoder.decode(message, "UTF-8");
            avatar = URLDecoder.decode(avatar, "UTF-8");
            if (hasAttachment()) {
                filetype = URLDecoder.decode(filetype, "UTF-8");
                attachment = BuAPI.getAvailableUrl(URLDecoder.decode(attachment, "UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.avatar = getTrueAvatar();
        this.quotes = new ArrayList<>();
        message = prettify(message);
        this.content = parseQuotes(message);
        if (hasAttachment()) {
//            if (filetype.toLowerCase().equals("image/gif") || filetype.equals("image/jpeg") || filetype.equals("image/jpg") || filetype.equals("image/png") || filetype.equals("image/x-png"))
            if (filetype.toLowerCase().startsWith("image/"))
                this.content += String.format("<br /><br /><i>附件</i><br /><img src=\"%s\">", attachment);
        }
    }

    //得到头像真实的URL
    public String getTrueAvatar() {
        Pattern p = Pattern.compile("<img src=\"(.*?)\"  border=\"0\">", Pattern.DOTALL);
        Matcher m = p.matcher(avatar);
        String finder;
        if (m.find()) {
            finder = m.group(1);
            return BuAPI.getAvailableUrl(finder);
        }
        return "";
    }

    public String prettify(String message) {
        return removeBlankLines(message);
    }

    // 去除段前段后的换行符
    private String removeBlankLines(String message) {
        message = message.trim();
        while (message.startsWith("<br>")) {
            message = message.substring(4).trim();
        }
        while (message.startsWith("<br />")) {
            message = message.substring(6).trim();
        }
        while (message.endsWith("<br />")) {
            message = message.substring(0, message.length() - 6).trim();
        }
        return message;
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
        public String author;
        public String time;
        public String content;

        public Quote(String author, String time, String content) {
            this.author = author;
            this.time = time;
            this.content = content;
        }
    }

}