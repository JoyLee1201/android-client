package com.shixian.android.client.activities;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.PersistentCookieStore;
import com.shixian.android.client.Global;
import com.shixian.android.client.MyApplication;
import com.shixian.android.client.R;
import com.shixian.android.client.model.User;
import com.shixian.android.client.sina.AccessTokenKeeper;
import com.shixian.android.client.sina.Constants;
import com.shixian.android.client.sina.widget.LoginButton;
import com.shixian.android.client.utils.ApiUtils;
import com.shixian.android.client.utils.CommonUtil;
import com.shixian.android.client.utils.LoginUtil;
import com.shixian.android.client.utils.SharedPerenceUtil;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;
import com.umeng.analytics.MobclickAgent;

import cn.jpush.android.api.JPushInterface;


public class LoginActivity extends Activity
{
    private String TAG = "LoginActivity";
    private Oauth2AccessToken mAccessToken;

  //  private ProgressDialog progressDialog;

   ProgressDialog mProgressDialog ;


    private LoginButton loginButton;

    private AuthInfo mAuthInfo;
    private AuthListener mLoginListener = new AuthListener();



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);


        init();


        }



    private void init() {
        setContentView(R.layout.activity_login);

        initJpush();


        loginButton = (LoginButton) findViewById(R.id.login_button_default);


       // progressDialog=new ProgressDialog(this);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("正在验证登陆信息");

        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);


        // 创建授权认证信息
        mAuthInfo = new AuthInfo(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);


//        AccessTokenKeeper.clear(this);
          mAccessToken=AccessTokenKeeper.readAccessToken(LoginActivity.this);
      // Toast.makeText(this,mAccessToken.getToken(),Toast.LENGTH_LONG).show();
        //不为null 并且可用
        if(mAccessToken!=null&&mAccessToken.isSessionValid()){

            ApiUtils.init(LoginActivity.this);
          //  Log.i("AAAA",mAccessToken.getToken()+"本地保存有token的时候--------------------");
            //这里还要验证token是否可用
            //在这里需要现实进度条给用户提示

            String cookie=getSharedPreferences("userinfo", Context.MODE_PRIVATE).getString("cookie","");

            ApiUtils.client.addHeader("Cookie", cookie);
            ApiUtils.client.addHeader("user-agent", "android");

            startActivity(new Intent(this, MainActivity.class));
            finish();
        }else{
            setOnclick();
        }


    //大哥我先把这里注释掉了 到时候再打开阿
      /*  new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                // 跳过登录
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }
        }, 1000);*/


    }

    public void setOnclick() {

        loginButton.setWeiboAuthInfo(mAuthInfo, mLoginListener);
    }

    //微薄登陆回调类
    public class AuthListener implements WeiboAuthListener {

        @Override
        public void onComplete(Bundle values) {
            // 从 Bundle 中解析 Token
            mAccessToken = Oauth2AccessToken.parseAccessToken(values);

           // Log.i("AAAA2",mAccessToken.getToken());



            if (mAccessToken.isSessionValid()) {


                Log.i("AAAA",mAccessToken.getToken()+"点击登录的时候--------------------");
                // 保存 Token 到 SharedPreferences
                AccessTokenKeeper.writeAccessToken(LoginActivity.this, mAccessToken);
                CommonUtil.logDebug(TAG,mAccessToken.getToken());
                //从服务器请求cookie？？
                mProgressDialog.show();
                LoginUtil.validationToken(LoginActivity.this,mAccessToken,mProgressDialog);

            } else {
                // 以下几种情况，您会收到 Code：
                // 1. 当您未在平台上注册的应用程序的包名与签名时；
                // 2. 当您注册的应用程序包名与签名不正确时；
                // 3. 当您在平台上注册的包名和签名与您当前测试的应用的包名和签名不匹配时。
                String code = values.getString("code");
                String message = getString(R.string.weibosdk_demo_toast_auth_failed);
                if (!TextUtils.isEmpty(code)) {
                    message = message + "\nObtained the code: " + code;
                }
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onCancel() {
            Toast.makeText(LoginActivity.this,
                    R.string.weibosdk_demo_toast_auth_canceled, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Toast.makeText(LoginActivity.this,
                    "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

         loginButton.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        String userJson= SharedPerenceUtil.getUserInfo(this);
        if("".equals(userJson))
        {
            Global.user= new Gson().fromJson(userJson,User.class);
        }

        super.onDestroy();
    }

    /**
     * 初始化极光推送
     */
    private void initJpush() {

        JPushInterface.init(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        JPushInterface.onResume(this);
        MobclickAgent.onResume(this);


    }

    @Override
    protected void onPause() {
        super.onPause();
        JPushInterface.onPause(this);
        MobclickAgent.onPause(this);

    }
}
