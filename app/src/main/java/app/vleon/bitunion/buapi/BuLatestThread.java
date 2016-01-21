package app.vleon.bitunion.buapi;

/**
 * Created by vleon on 2016/1/13.
 */
public class BuLatestThread {

    /**
     * pname : %E6%88%91%E5%B0%B1%E6%83%B3%E9%97%AE%E9%97%AE%E5%90%84%E4%BD%8D%E5%9C%A8%E5%A4%96%E9%9D%A2%E5%92%8C%E6%9C%8B%E5%8F%8B%E4%BB%AC%E5%90%83%E9%A5%AD%E7%9A%84%E6%97%B6%E5%80%99%E8%83%BD%E6%8E%A5%E7%94%B5%E8%AF%9D%E4%B9%88%EF%BC%9F
     * fname : %E7%81%8C%E6%B0%B4%E4%B9%90%E5%9B%AD
     * author : lilybit
     * tid : 10608717
     * tid_sum : 29
     * fid : 14
     * fid_sum : 252760
     * lastreply : {"when":"2016-1-19+23%3A10","who":"Vlanes","what":"%E6%88%91%E5%92%8B%E8%A7%89%E5%BE%97%E4%BD%A0%E8%80%81%E5%85%AC%E5%A4%AA%E8%BF%87%E5%88%86%E4%BA%86%E5%91%A2+%E6%8E%A5%E4%B8%AA%E7%94%B5%E8%AF%9D%E8%87%B3%E4%BA%8E%E5%90%97+%E4%BC%9A%E6%B2%A1%E9%9D%A2%E5%AD%90%3F%3F%3F..%E5%8F%AF%E8%83%BD%E6%81%8B%E7%88%B1%E5%92%8C%E7%BB%93%E5%A9%9A%E8%BF%98%E6%98%AF%E4%B8%8D%E4%B8%80%E6%A0%B7%3F"}
     */

    private String pname;
    private String fname;
    private String author;
    private String tid;
    private String tid_sum;
    private String fid;
    private String fid_sum;
    /**
     * when : 2016-1-19+23%3A10
     * who : Vlanes
     * what : %E6%88%91%E5%92%8B%E8%A7%89%E5%BE%97%E4%BD%A0%E8%80%81%E5%85%AC%E5%A4%AA%E8%BF%87%E5%88%86%E4%BA%86%E5%91%A2+%E6%8E%A5%E4%B8%AA%E7%94%B5%E8%AF%9D%E8%87%B3%E4%BA%8E%E5%90%97+%E4%BC%9A%E6%B2%A1%E9%9D%A2%E5%AD%90%3F%3F%3F..%E5%8F%AF%E8%83%BD%E6%81%8B%E7%88%B1%E5%92%8C%E7%BB%93%E5%A9%9A%E8%BF%98%E6%98%AF%E4%B8%8D%E4%B8%80%E6%A0%B7%3F
     */

    private LastReplyEntity lastreply;

    public void parse() {

    }

    public String getPname() {
        return pname;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getTid_sum() {
        return tid_sum;
    }

    public void setTid_sum(String tid_sum) {
        this.tid_sum = tid_sum;
    }

    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public String getFid_sum() {
        return fid_sum;
    }

    public void setFid_sum(String fid_sum) {
        this.fid_sum = fid_sum;
    }

    public LastReplyEntity getLastreply() {
        return lastreply;
    }

    public void setLastreply(LastReplyEntity lastreply) {
        this.lastreply = lastreply;
    }

    public static class LastReplyEntity {
        private String when;
        private String who;
        private String what;

        public String getWhen() {
            return when;
        }

        public void setWhen(String when) {
            this.when = when;
        }

        public String getWho() {
            return who;
        }

        public void setWho(String who) {
            this.who = who;
        }

        public String getWhat() {
            return what;
        }

        public void setWhat(String what) {
            this.what = what;
        }
    }
}
