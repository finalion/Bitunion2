package app.vleon.bitunion;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;

import app.vleon.buapi.BuAPI;
import app.vleon.util.GlideImageGetter;
import de.hdodenhof.circleimageview.CircleImageView;

public class PersonalInfoActivity extends AppCompatActivity {

    MyApplication app;
    CircleImageView mAvatarImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);
        //设置toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.my_toolbar3);
        setSupportActionBar(mToolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAvatarImageView = (CircleImageView) findViewById(R.id.avatar_page_imageview);
        app = (MyApplication) getApplicationContext();

        Intent intent = getIntent();
        String uid = intent.getStringExtra("uid");
        if (uid.equals("me")) {
            showInfo(app.getMyInfo());
        } else {
            app.getAPI().getMemberInfo(uid);
        }

        app.getAPI().setOnMemberInfoResponseListener(new BuAPI.OnMemberInfoResponseListener() {
            @Override
            public void handleMemberInfoGetterResponse(BuAPI.Result result, BuAPI.MemberInfo memberInfo) {
                switch (result) {
                    case SUCCESS:
                        if (memberInfo != null)
                            showInfo(memberInfo);
                        break;
                    case IP_LOGGED:
//                Toast.makeText(this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                        break;
                    default:
//                Toast.makeText(this, "未知登录错误: " + mAPI.getLoginInfo().msg, Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void handleMemberInfoGetterErrorResponse(VolleyError error) {

            }
        });
    }

    void setProfileView(int resId, String str, int withImage) {
        TextView view = (TextView) findViewById(resId);
        if (0 == withImage) {
            view.setText(Html.fromHtml(str));
        } else {
            view.setText(Html.fromHtml(str, new GlideImageGetter(this, view), null));
        }
    }

    void setProfileView(int resId, String str) {
        setProfileView(resId, str, 0);
    }

    public void showInfo(BuAPI.MemberInfo memberInfo) {
        setProfileView(R.id.username_page_textview, memberInfo.username);
        setProfileView(R.id.uidView, memberInfo.uid);
        setProfileView(R.id.creditView, memberInfo.credit);
        setProfileView(R.id.regdateView, memberInfo.regdate);
        setProfileView(R.id.lastvisitView, memberInfo.lastvisit);
        setProfileView(R.id.postcntView, memberInfo.threadnum + "/"
                + memberInfo.postnum);
        setProfileView(R.id.bdayView, memberInfo.bday);
        setProfileView(R.id.msnView, memberInfo.msn);
        setProfileView(R.id.qqView, memberInfo.oicq);
        setProfileView(R.id.emailView, memberInfo.email);
        setProfileView(R.id.signView, memberInfo.signature, 1);
//            setAvatarView(R.id.quickContactBadge1, myinfo.getTrueAvatar());
        Glide.with(this).load(memberInfo.getTrueAvatar()).fitCenter().into(mAvatarImageView);
    }
//
//
//    @Override
//    public void handleMemberInfoGetterResponse(BuAPI.Result result, BuAPI.MemberInfo memberInfo) {
//        switch (result) {
//            case SUCCESS:
//                if (memberInfo != null)
//                    showInfo(memberInfo);
//                break;
//            case IP_LOGGED:
////                Toast.makeText(this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
//                break;
//            default:
////                Toast.makeText(this, "未知登录错误: " + mAPI.getLoginInfo().msg, Toast.LENGTH_SHORT).show();
//                break;
//        }
//    }
//
//    @Override
//    public void handleMemberInfoGetterErrorResponse(VolleyError error) {
//
//    }
}
