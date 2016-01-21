package app.vleon.bitunion.buapi;

/**
 * Created by vleon on 2016/1/1.
 */

import android.os.Parcel;
import android.os.Parcelable;

public class BuForum implements Parcelable {

    public static final Creator<BuForum> CREATOR = new Creator<BuForum>() {
        @Override
        public BuForum createFromParcel(Parcel source) {
            return new BuForum(source);
        }

        @Override
        public BuForum[] newArray(int size) {
            return new BuForum[size];
        }
    };
    private final String name;
    private final int fid;
    private final int type;

    private BuForum(Parcel in) {
        name = in.readString();
        fid = in.readInt();
        type = in.readInt();
    }

    public BuForum(String name, int fid, int type) {
        this.name = name;
        this.fid = fid;
        this.type = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(fid);
        dest.writeInt(type);
    }

    public String getName() {
        return name;
    }

    public int getFid() {
        return fid;
    }

    public int getType() {
        return type;
    }

}
