package app.vleon.bitunion.buapi;

/**
 * Created by vleon on 2016/1/1.
 */

import android.os.Parcel;
import android.os.Parcelable;

public class BuForumGroup implements Parcelable {
    public static final Parcelable.Creator<BuForumGroup> CREATOR = new Creator<BuForumGroup>() {
        @Override
        public BuForumGroup createFromParcel(Parcel source) {
            return new BuForumGroup(source);
        }

        @Override
        public BuForumGroup[] newArray(int size) {
            return new BuForumGroup[size];
        }
    };
    public final int gid;
    public final String groupName;

    private BuForumGroup(Parcel in) {
        this.gid = in.readInt();
        this.groupName = in.readString();
    }

    public BuForumGroup(int gid, String groupName) {
        this.gid = gid;
        this.groupName = groupName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(gid);
        dest.writeString(groupName);
    }
}
