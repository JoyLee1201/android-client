package com.shixian.android.client.activities.base;

import android.support.v7.app.ActionBarActivity;

import com.umeng.analytics.MobclickAgent;

/**
 * Created by tangtang on 15/3/16.
 * 友盟统计
 */
public class UmengActivity extends ActionBarActivity {


    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
