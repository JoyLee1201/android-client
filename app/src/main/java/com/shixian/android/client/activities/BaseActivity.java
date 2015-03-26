package com.shixian.android.client.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;

import com.shixian.android.client.activities.fragment.base.BaseFragment;
import com.shixian.android.client.contants.AppContants;
import com.shixian.android.client.utils.ApiUtils;
import com.shixian.android.client.utils.CommonUtil;

/**
 * Created by s0ng on 2015/2/22.
 */
public abstract  class BaseActivity extends UmengActivity {

    private String COOKIE_KEY="COOKIE_KEY";


    /**
     * 添加第一个activity
     */
    protected abstract void addFragment();

    /**
     * 切换fragment
     * @param fragment   要切换的fragment
     * @param key     放入回退栈的key
     */
    public abstract void switchFragment(BaseFragment fragment,String key);

    public abstract  void  setLable(String lable);

    public abstract  void showProgress();

    public abstract void dissProgress();

    protected  FinishActivityReceiver receiver;


    //写个广播接受着 用于关闭activity


    protected static final String LABEL="LABLE";

    protected Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {



        if(null != savedInstanceState)
        {

            String label = savedInstanceState.getString(LABEL);
            this.setTitle(label);
            String cookie=savedInstanceState.getString(COOKIE_KEY);

            if(!TextUtils.isEmpty(cookie))
            {
                ApiUtils.client.addHeader("Cookie", cookie);

            }


        }


        super.onCreate(savedInstanceState);
        receiver=new FinishActivityReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction(AppContants.ACTION_FINISHACTIVITY);
        registerReceiver(receiver,filter);
    }

    protected class FinishActivityReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

            CommonUtil.logDebug("AAAAAA","广播接受着已经其起到＝用");
            finish();
        }
    }


    @Override
    protected void onDestroy() {
        if(receiver!=null)
        {
            unregisterReceiver(receiver);
            receiver=null;
        }

        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save away the original text, so we still have it if the activity
        // needs to be killed while paused.

        String cookie=getSharedPreferences("userinfo", Context.MODE_PRIVATE).getString("cookie","");
        if(!TextUtils.isEmpty(cookie))
        {
            savedInstanceState.putString(COOKIE_KEY,cookie);
        }


        savedInstanceState.putString(LABEL,getTitle().toString());

        super.onSaveInstanceState(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    public  Toolbar getToolbar()
    {
        return toolbar;
    }


    public void setToolbarOnClickListener(View.OnClickListener onClickListener)
    {
        toolbar.setOnClickListener(onClickListener);
    }
}
