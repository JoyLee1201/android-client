package com.shixian.android.client.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

/**
 * Created by sllt on 15/1/30.
 */
public class ApiUtils {

    public static AsyncHttpClient client=new AsyncHttpClient();
    public static PersistentCookieStore cookieStore;

    public static String cookie;


    public static  AsyncHttpClient uploadclient=new AsyncHttpClient();

    public static void uploadPost(Context context,String url, RequestParams params, AsyncHttpResponseHandler handler) {
        initupload(context).post(url, params, handler);
    }


    public static AsyncHttpClient initupload(Context context)
    {


        if(uploadclient==null) {
            uploadclient = new AsyncHttpClient();
        }

        if(TextUtils.isEmpty(cookie))
        {
            SharedPreferences sp = context.getSharedPreferences("userinfo", Context.MODE_PRIVATE);
            cookie=sp.getString("cookie", "");
        }

        uploadclient.addHeader("Cookie",cookie);

        return uploadclient;

    }



    public static AsyncHttpClient init(Context context)
    {

        if(client==null) {
            client = new AsyncHttpClient();
            cookieStore=new PersistentCookieStore(context);
            client.setCookieStore(cookieStore);
        }


        if(cookieStore==null)
        {
            cookieStore=new PersistentCookieStore(context);
            client.setCookieStore(cookieStore);
        }

        return client;

    }



    public static void get(Context context,String url, RequestParams params, AsyncHttpResponseHandler handler) {

           init(context).get(url, params, handler);
    }

    public static void post(Context context,String url, RequestParams params, AsyncHttpResponseHandler handler) {
        init(context).post(url, params, handler);
    }

    public static void delete(Context context,String url, AsyncHttpResponseHandler handler) {
        init(context).delete(url, handler);
    }

    public static void put(Context context,String url, RequestParams params, AsyncHttpResponseHandler handler) {
        init(context).put(url, params, handler);
    }
}
