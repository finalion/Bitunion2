package app.vleon.bitunion;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.volley.VolleyError;

import java.util.Map;

import app.vleon.bitunion.buapi.BuAPI;

public class SplashActivity extends AppCompatActivity implements BuAPI.OnLoginResponseListener {
    MyApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        app = (MyApplication) getApplicationContext();
        app.getAPI().setOnLoginResponseListener(this);
        Map<String, ?> msg = getSharedPreferences("lastlogin", Context.MODE_PRIVATE).getAll();
        // 如果读取用户信息成功，登录；否则跳转LoginActivity
        if (msg.get("username") != null && msg.get("password") != null) {
            int net;
            if (msg.get("net") == null)
                net = 1;
            else
                net = (Integer) msg.get("net");
            login(msg.get("username").toString(), msg.get("password").toString(), net);
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    public void login(String username, String password, int net) {
        app.getAPI().setNetType(net);
        app.getAPI().login(username, password);
    }

    @Override
    public void handleLoginResponse() {
        switch (app.getAPI().getLoginResult()) {
            case SUCCESS:
                app.getAPI().getMyInfo();
//                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
                break;
            case IP_LOGGED:
                Intent intent1 = new Intent(this, LoginActivity.class);
//                intent1.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent1.putExtra("autologin_result", "ip_logged");
                startActivity(intent1);
                break;
            default:
                Intent intent2 = new Intent(this, LoginActivity.class);
                intent2.putExtra("autologin_result", "unknown");
                startActivity(intent2);
                break;
        }
    }

    @Override
    public void handleLoginErrorResponse(VolleyError error) {
        Log.d("TAG", error.getMessage(), error);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("autologin_result", "error_response");
        startActivity(intent);
    }
}
