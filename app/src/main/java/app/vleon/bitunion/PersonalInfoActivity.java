package app.vleon.bitunion;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import app.vleon.buapi.BuAPI;
import de.hdodenhof.circleimageview.CircleImageView;

public class PersonalInfoActivity extends AppCompatActivity {

    MyApplication app;
    BuAPI.MemberInfo myinfo;
    CircleImageView mAvatarImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);
        mAvatarImageView = (CircleImageView) findViewById(R.id.avatar_page_imageview);
        app = (MyApplication) getApplicationContext();
        myinfo = app.getMyInfo();
        showMyInfo();
    }

    void setProfileView(int resId, String str) {
        TextView view = (TextView) findViewById(resId);
        view.setText(Html.fromHtml(str));
    }

    void showMyInfo() {
        if (myinfo != null) {
            setProfileView(R.id.username_page_textview, myinfo.username);
            setProfileView(R.id.uidView, myinfo.uid);
            setProfileView(R.id.creditView, myinfo.credit);
            setProfileView(R.id.regdateView, myinfo.regdate);
            setProfileView(R.id.lastvisitView, myinfo.lastvisit);
            setProfileView(R.id.postcntView, myinfo.threadnum + "/"
                    + myinfo.postnum);
            setProfileView(R.id.bdayView, myinfo.bday);
            setProfileView(R.id.msnView, myinfo.msn);
            setProfileView(R.id.qqView, myinfo.oicq);
            setProfileView(R.id.emailView, myinfo.email);
            setProfileView(R.id.signView, myinfo.signature);
//            setAvatarView(R.id.quickContactBadge1, myinfo.getTrueAvatar());
            Glide.with(this).load(myinfo.getTrueAvatar()).fitCenter().into(mAvatarImageView);
        }
    }
}
