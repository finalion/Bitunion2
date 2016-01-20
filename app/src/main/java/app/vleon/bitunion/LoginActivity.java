package app.vleon.bitunion;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import java.util.Map;

import app.vleon.buapi.BuAPI;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements BuAPI.OnLoginResponseListener {

    MyApplication app;
    Toolbar mToolbar;
    boolean mDirectLoginFlag = false;
    // UI references.
    private EditText mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
//    @TargetApi(19)
//    private void setTranslucentStatus(boolean on) {
//        Window win = getWindow();
//        WindowManager.LayoutParams winParams = win.getAttributes();
//        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
//        if (on) {
//            winParams.flags |= bits;
//        } else {
//            winParams.flags &= ~bits;
//        }
//        win.setAttributes(winParams);
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        app = (MyApplication) getApplicationContext();
        app.getAPI().setOnLoginResponseListener(this);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        //设置toolbar
        mToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(mToolbar);

        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);

        Intent intent = getIntent();
        String from = intent.getStringExtra("from");
        if (from == null) {
            Map<String, ?> msg = getSavedMsg();
            if (msg.get("username") != null && msg.get("password") != null) {
                mUsernameView.clearFocus();
                mPasswordView.clearFocus();
                showProgress(true);
                mToolbar.setVisibility(View.GONE);
                mDirectLoginFlag = true;
                login(msg.get("username").toString(), msg.get("password").toString(), BuAPI.OUTNET);
                return;
            }
        } else if (from.equals("logout_menu")) {
            mUsernameView.setText(app.getAPI().getUsername());
            mPasswordView.setText(app.getAPI().getPassword());
        }

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError("请输入密码");
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username address.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError("请输入用户名");
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            login(username, password, BuAPI.OUTNET);
        }
    }

    public void login(String username, String password, int net) {
        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        showProgress(true);
        app.getAPI().setNetType(net);
        app.getAPI().login(username, password);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void handleLoginResponse() {
        switch (app.getAPI().getLoginResult()) {
            case SUCCESS:
                //登录成功保存信息，下次使用
                saveMsg(app.getAPI().getUsername(), app.getAPI().getPassword());
                app.getAPI().getMyInfo();
//                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                if (mDirectLoginFlag) {
                    finish();
                    overridePendingTransition(0, 0);
                    return;
                }
                break;
            case IP_LOGGED:
//                Toast.makeText(this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                mUsernameView.requestFocus();
                mPasswordView.setError("用户名或密码错误");
                break;
            default:
                Toast.makeText(this, "未知登录错误: " + app.getAPI().getLoginInfo().msg, Toast.LENGTH_SHORT).show();
                break;
        }
        mToolbar.setVisibility(View.VISIBLE);
        showProgress(false);
    }

    @Override
    public void handleLoginErrorResponse(VolleyError error) {
        Log.d("TAG", error.getMessage(), error);
        Toast.makeText(LoginActivity.this, "登录异常: " + error.getMessage(), Toast.LENGTH_SHORT).show();
        showProgress(false);
    }

    public void saveMsg(String username, String password) {
        SharedPreferences sharedPreferences = getSharedPreferences("lastlogin", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", username).putString("password", password).apply();
    }

    public Map<String, ?> getSavedMsg() {
        SharedPreferences sharedPreferences = getSharedPreferences("lastlogin", Context.MODE_PRIVATE);
//        String username = sharedPreferences.getString("username", null);
//        String password = sharedPreferences.getString("password",null);
        return sharedPreferences.getAll();
    }

}

