package com.shixian.android.client.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.shixian.android.client.Global;
import com.shixian.android.client.MyApplication;
import com.shixian.android.client.R;
import com.shixian.android.client.activities.base.BaseActivity;
import com.shixian.android.client.activities.fragment.DiscoryProjectFragment;
import com.shixian.android.client.activities.fragment.IndexFragment;
import com.shixian.android.client.activities.fragment.MyUserIndexFragment;
import com.shixian.android.client.activities.fragment.NewsFragment;
import com.shixian.android.client.activities.fragment.SpotlightFragment;
import com.shixian.android.client.activities.fragment.base.BaseFragment;
import com.shixian.android.client.contants.AppContants;
import com.shixian.android.client.controller.IndexOnClickController;
import com.shixian.android.client.engine.CommonEngine;
import com.shixian.android.client.engine.JPushEngine;
import com.shixian.android.client.model.NewsSataus;
import com.shixian.android.client.model.SimpleProject;
import com.shixian.android.client.model.User;
import com.shixian.android.client.utils.ApiUtils;
import com.shixian.android.client.utils.CommonUtil;
import com.shixian.android.client.utils.DisplayUtil;
import com.shixian.android.client.utils.SharedPerenceUtil;
import com.umeng.update.UmengUpdateAgent;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.jpush.android.api.JPushInterface;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBarUtils;
import fr.castorflex.android.smoothprogressbar.SmoothProgressDrawable;
import android.util.TypedValue;


/**
 * Created by doom on 15/2/2.
 * 这里是首页activity  里面可以切换导航栏的那些activity  并且管理导航栏（导航栏并没有交给fragment托管）
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {

    private String TAG = "MainActivity";




    //这是那个恶心人的回复框
    private LinearLayout commonEnterRoot;



    /**
     * 如果是主页的话该值为true
     */
    private boolean isIndex;

    private boolean onBackQuit = false;

    private DrawerLayout drawerLayout;

    private Drawable drawable;

    //抽屉里的子listView
    private List<SimpleProject> projectList= new ArrayList<>();
    private MenuAdapter projectAdapter;


    //抽屉里的主ListView
    private ListView lv_menu;


    //touxiang头像
    private ImageView iv_icon;
    //昵称
    private TextView tv_uname;

    //存放我的项目
    private String myProjectjson;


    //userinfo本地缓存
    private String userInfo;

    /**
     * 丑陋的进度条 搞好逻辑之后替换
     */
    private SmoothProgressBar mProgressBar;


    private LinearLayout ll_descory;
    private LinearLayout ll_index;
    private RelativeLayout ll_msg;
 //   private LinearLayout ll_addproject;
    private LinearLayout ll_spotlight;

    //用于记录 当前属于哪个
    private BaseFragment currentFeed;

    //private RedPointView titleImgPoint;
    private TextView tv_msg_count;
    private ImageView iv_msg;

    private User user;

    private TextView toastView;

    private TextView tv_caogao;

    private RelativeLayout rl_title;

    /**
     * Toast 的Params
     */
    private WindowManager.LayoutParams mParams;

    private WindowManager windowManager;

    private SwicthFrageReveiver swicthFrageReveiver;

    public static  final int REFREST_CODE=10088;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        UmengUpdateAgent.update(this);

        setContentView(R.layout.activity_main);
        initReceiver();
        Global.MAIN = this;
        Global.context = this;
        Global.screenWidth = CommonUtil.getScreenWidth(this);
        initUI();
        addFragment();
        initDate();

      //  checkUpdate();

    }

    /**
     *作废了
     */
    @Deprecated
    private void checkUpdate() {

        if(SharedPerenceUtil.checkNeedUpdate(this.getApplicationContext()))
        {


            //检查更新
            ApiUtils.get(MainActivity.this,"http://dev.shixian.com:3000/androidupdate.json",null, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int position, Header[] headers, byte[] bytes) {

                    Toast.makeText(MainActivity.this,new String(bytes),Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onFailure(int position, Header[] headers, byte[] bytes, Throwable throwable) {

                    Toast.makeText(MainActivity.this,new String(bytes),Toast.LENGTH_SHORT).show();

                }


            });
        }
    }


    /**
     *初始化jpush的广播接收者
     */
    private void initReceiver() {

        swicthFrageReveiver = new SwicthFrageReveiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(AppContants.ACTION_JPUSHACTIVITY);
        registerReceiver(swicthFrageReveiver, filter);
    }


    /**
     * 初始化数据
     */
    private void initDate() {

        //初始化用户头像id
        initUserInfo();
        //初始化用户的项目
        initUserProjects();




    }

    /**
     * 初始化消息状态
     */
    public void initMsgStatus() {
        ApiUtils.get(MainActivity.this,AppContants.MSG_STATUS_URL, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, final byte[] bytes) {
                Gson gson = new Gson();
                final NewsSataus status = gson.fromJson(new String(bytes), NewsSataus.class);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {



                        // settingMsgCount(1);

                        if (status.total != 0)
                            settingMsgCount(status.total);

                        else {

                            hideMsg();

                        }

                    }
                });
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
            }
        });
    }


    /**
     * 初始化侧边栏的项目
     */
    private void initUserProjects() {

        if(projectList==null)
            projectList = new ArrayList<>();

        myProjectjson = SharedPerenceUtil.getMyProject(this.getApplicationContext());

        //获取用户项目
        ApiUtils.get(MainActivity.this,AppContants.URL_MY_PROJECT_INFO, null, new AsyncHttpResponseHandler() {
            @Override
            public void onFinish() {
                super.onFinish();
            }

            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {


                String temp = new String(bytes);
                if(!TextUtils.isEmpty(temp))
                {
                    myProjectjson=temp;
                }
                if (!AppContants.errorMsg.equals(myProjectjson)) {
                    projectList.clear();
                    Gson gson = new Gson();
                    try {
                        JSONArray array = new JSONArray(myProjectjson);
                        for (int j = 0; j < array.length(); j++) {
                            SimpleProject sp = gson.fromJson(array.getString(j), SimpleProject.class);


                            projectList.add(sp);

                            SharedPerenceUtil.putMyProject(MainActivity.this.getApplicationContext(), myProjectjson);

                            if (projectAdapter == null) {
                                projectAdapter = new MenuAdapter();


                                lv_menu.setAdapter(projectAdapter);


                            } else {
                                projectAdapter.notifyDataSetChanged();
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }


            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {

            }
        });


    }

    /**
     * 初始化左侧抽屉user 信息
     */
    private void initUserInfo() {
        userInfo = SharedPerenceUtil.getUserInfo(this.getApplicationContext());

        CommonEngine.getMyUserInfo(MainActivity.this,new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {

//                CommonUtil.logDebug(TAG, new String(bytes));


                userInfo = new String(bytes);
                //出错
                if (!AppContants.errorMsg.equals(userInfo)) {
                    Gson gson = new Gson();
                    User user = gson.fromJson(userInfo, User.class);
                    MainActivity.this.user = user;
                    Global.USER_ID = user.id;
                    Global.USER_NAME = user.username;


                    //发送推送需要的信息
                    JPushEngine.sendJpushData(MainActivity.this,true);

                    if(!TextUtils.isEmpty(user.username))
                     tv_uname.setText(user.username);

                    SharedPerenceUtil.putUserInfo(MainActivity.this.getApplicationContext(), userInfo);

                    //异步下载图片(头像)
                    ApiUtils.get(MainActivity.this,AppContants.ASSET_DOMAIN + user.avatar.small.url, null, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int i, Header[] headers, final byte[] bytes) {
                            Bitmap icon = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            iv_icon.setImageBitmap(icon);

                            //将投向保存在本地
                            new Thread() {
                                public void run() {
                                    File file = new File(MainActivity.this.getFilesDir().getAbsolutePath() + AppContants.USER_ICON_NAME);
                                    try {
                                        FileOutputStream fos = new FileOutputStream(file);
                                        fos.write(bytes, 0, bytes.length);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }.start();


                        }

                        @Override
                        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {

                        }
                    });

                }

            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
//                CommonUtil.logDebug(TAG, new String(bytes));
            }
        });
    }


    /**
     * 初始化UI
     */
    private void initUI() {


        windowManager = getWindowManager();


        Global.iv_conte_size = DisplayUtil.dip2px(this, 200);

        rl_title= (RelativeLayout) findViewById(R.id.rl_title);

        drawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        commonEnterRoot= (LinearLayout) findViewById(R.id.commonEnterRoot);


        drawable = toolbar.getNavigationIcon();

        // bt_msg_count= (Button) findViewById(R.id.bt_msg_count);


        setSupportActionBar(toolbar);

//        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem menuItem) {
//                switch (menuItem.getItemId()) {
////                    case R.id.action_news_search:
////                        Toast.makeText(MainActivity.this, "Search", Toast.LENGTH_LONG).show();
////                        break;
////                    case R.id.action_quit:
////                        logout();
////                        break;
//                }
//                return true;
//            }
//        });
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name) {


            @Override
            public void onDrawerOpened(View drawerView) {
                //              Toast.makeText(MainActivity.this, "打开", Toast.LENGTH_LONG).show();

                if (toastView != null) {
                    toastView.setVisibility(View.GONE);
                }


                if(lv_menu.getFirstVisiblePosition()==0)
                {
                    if(((MyApplication)getApplication()).getHasCaogao())
                    {
                        if(tv_caogao!=null)
                        tv_caogao.setVisibility(View.VISIBLE);
                    }else{

                        if(tv_caogao!=null)
                        tv_caogao.setVisibility(View.GONE);
                    }

                }



                super.onDrawerOpened(drawerView);

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                //    Toast.makeText(MainActivity.this, "关闭", Toast.LENGTH_LONG).show();
                super.onDrawerClosed(drawerView);
                if (toastView != null) {
                    toastView.setVisibility(View.VISIBLE);
                }
                initUserProjects();
                initUserInfo();

            }
        };

        mProgressBar = (SmoothProgressBar) findViewById(R.id.pocket);
        mProgressBar.setSmoothProgressDrawableBackgroundDrawable(
                SmoothProgressBarUtils.generateDrawableWithColors(
                        getResources().getIntArray(R.array.pocket_background_colors),
                        ((SmoothProgressDrawable) mProgressBar.getIndeterminateDrawable()).getStrokeWidth()));

        drawerToggle.syncState();
        drawerLayout.setDrawerListener(drawerToggle);


        tv_uname = (TextView) findViewById(R.id.tv_uname);
        iv_icon = (ImageView) findViewById(R.id.iv_icon);
        if (!TextUtils.isEmpty(Global.USER_NAME)) {
              tv_uname.setText(Global.USER_NAME);

        }


        iv_icon.setImageBitmap(BitmapFactory.decodeFile(getFilesDir().getAbsolutePath() + AppContants.USER_ICON_NAME));

        lv_menu = (ListView) findViewById(R.id.lv_menu);


        lv_menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(position==0)
                {
                    Intent intent=new Intent(MainActivity.this,NewProjectActivity.class);

                    startActivityForResult(intent,REFREST_CODE);
                }else{
                    SimpleProject project = projectList.get(position-1);
                    if (project != null) {
                        Intent intent = new Intent(MainActivity.this, ProjectActivity.class);
                        intent.putExtra("project_id", project.getId() + "");
                        MainActivity.this.startActivity(intent);

                    }
                }


            }
        });

        /**
         * 抽屉中的三个选项
         */
        ll_descory = (LinearLayout) findViewById(R.id.ll_descory);
        ll_msg = (RelativeLayout) findViewById(R.id.ll_msg);
        ll_index = (LinearLayout) findViewById(R.id.ll_index);
    //    ll_addproject= (LinearLayout) findViewById(R.id.ll_addproject);
        ll_spotlight= (LinearLayout) findViewById(R.id.ll_spotlight);


        ll_descory.setOnClickListener(this);
        ll_msg.setOnClickListener(this);
        ll_index.setOnClickListener(this);
   //     ll_addproject.setOnClickListener(this);
        ll_spotlight.setOnClickListener(this);


        //设置左侧抽屉的宽度等于屏幕宽度减去Toolbar的高度
        LinearLayout ll_left = (LinearLayout) findViewById(R.id.ll_left);
//        int actionBarHeight = 0;
//        TypedValue tv = new TypedValue();
//        if (this.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
//            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, this.getResources().getDisplayMetrics());
//        }

        ll_left.getLayoutParams().width = Global.screenWidth - DisplayUtil.dip2px(this,56);


  //      rl_title.getLayoutParams().height= actionBarHeight;

        //现实消息
        // showMsg(5);
        iv_msg = (ImageView) findViewById(R.id.iv_msg);

        //  titleImgPoint=new RedPointView(this,toolbar);
        tv_msg_count = (TextView) findViewById(R.id.tv_msg_count);


        //头像的点击事件 和用户名的点击事件
        View.OnClickListener userOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (user != null) {
                    Bundle bundle = new Bundle();

                    bundle.putInt("type", IndexOnClickController.USER_FRAGMENT);

                    bundle.putSerializable("user", user);


                    MyUserIndexFragment fragment=new MyUserIndexFragment();
                    fragment.setArguments(bundle);

                    currentFeed=fragment;

                    switchFragment( fragment,null);
                }

            }
        };

        tv_uname.setOnClickListener(userOnClickListener);
        iv_icon.setOnClickListener(userOnClickListener);

        projectAdapter = new MenuAdapter();
        lv_menu.setAdapter(projectAdapter);




    }

    /**
     * 天街fragment
     */
    protected void addFragment() {


        int addFragmentTag = getIntent().getIntExtra("what", 0);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        BaseFragment fragment = null;
        switch (addFragmentTag) {
            case 0:
                fragment = new IndexFragment();
                break;
            case 1:
                fragment = new NewsFragment();
                break;
            case 2:
                fragment = new DiscoryProjectFragment();
                break;
        }


        fragmentTransaction.replace(R.id.main_fragment_layout, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public void onClick(View v) {

        onBackQuit = false;

        switch (v.getId()) {
            case R.id.ll_descory:

                if (currentFeed instanceof DiscoryProjectFragment) {
                    drawerLayout.closeDrawers();
                    return;
                }
                switchFragment(new DiscoryProjectFragment(), "ll_descory");
                isIndex = false;

                break;
            case R.id.ll_index:
                isIndex = true;
                if (currentFeed instanceof IndexFragment) {
                    drawerLayout.closeDrawers();
                    return;
                }
                switchFragment(new IndexFragment(), "ll_index");

                break;
            case R.id.ll_msg:
                if (currentFeed instanceof NewsFragment) {
                    drawerLayout.closeDrawers();
                    return;
                }

                switchFragment(new NewsFragment(), "ll_msg");
                isIndex = false;

                break;

            //本周启动
            case R.id.ll_spotlight:

                if (currentFeed instanceof SpotlightFragment) {
                    drawerLayout.closeDrawers();
                    return;
                }

                switchFragment(new SpotlightFragment(), "ll_spotlight");
                isIndex = false;
                break;

        }

    }


    @Override
    public void finish() {

     //  sendJpushData(false);
        super.finish();
    }

    private static  final int TYPE_CREATE_PROJECT=0;
    private static final int TYPE_PROJECT_ITEM=1;

    private class MenuAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return projectList.size()+1;
        }

        @Override
        public Object getItem(int position) {
            return projectList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            if(position==0)
             return TYPE_CREATE_PROJECT;
            return TYPE_PROJECT_ITEM;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view=null;
            switch (getItemViewType(position))
            {
                case TYPE_CREATE_PROJECT:

                    view=View.inflate(MainActivity.this,R.layout.create_project_item,null);
                    LinearLayout ll_addproject= (LinearLayout) view.findViewById(R.id.ll_addproject);

                    tv_caogao= (TextView) view.findViewById(R.id.tv_caogao);
                    if(((MyApplication)getApplication()).getHasCaogao())
                    {
                        tv_caogao.setVisibility(View.VISIBLE);
                    }else{
                        tv_caogao.setVisibility(View.GONE);
                    }




                    break;

                case TYPE_PROJECT_ITEM:

                    ProjectHolder holder;
                    if (convertView == null) {
                        view = View.inflate(MainActivity.this, R.layout.lv_project_item, null);
                        holder = new ProjectHolder();
                        holder.tv_title = (TextView) view.findViewById(R.id.tv_title);
                        holder.iv_sit = (ImageView) view.findViewById(R.id.iv_sit);

                        view.setTag(holder);

                    } else {
                        view = convertView;
                        holder = (ProjectHolder) view.getTag();
                    }


                    holder.iv_sit.setAlpha(0.54f);

                    holder.tv_title.setText(projectList.get(position-1).getTitle());

                    break;


            }


            return view;



        }
    }


    class ProjectHolder {
        TextView tv_title;
        ImageView iv_sit;
    }


    public void showProgress() {
        mProgressBar.progressiveStart();
        mProgressBar.setVisibility(View.VISIBLE);
    }

    public void dissProgress() {
        mProgressBar.progressiveStop();
        mProgressBar.setVisibility(View.GONE);
    }


    public void switchFragment(BaseFragment fragment, String key) {

        onBackQuit = false;
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        fragmentTransaction.replace(R.id.main_fragment_layout, fragment);
        // fragmentTransaction.replace(R.id.main_fragment_layout, fragment);

        fragmentTransaction.commitAllowingStateLoss();
        drawerLayout.closeDrawers();

    }

    public void setLable(String lable) {
        toolbar.setTitle(lable);
    }


    public void settingMsgCount(int count) {


        if (count > 99)
            count = 99;

        showMsg(count);


    }

    @Override
    public void onBackPressed() {

        if (onBackQuit) {

            super.onBackPressed();
        } else {
            onBackQuit = true;
            Toast.makeText(this, "再次按返回键退出", Toast.LENGTH_SHORT).show();
        }


    }


/*****************************************************************/
    /**
     * 现实消息数量
     */
    public void showMsg(int count) {

//        titleImgPoint.setContent(count);
//        titleImgPoint.setSizeContent(16);
//        titleImgPoint.setColorContent(Color.WHITE);
//        titleImgPoint.setColorBg(Color.RED);
//        titleImgPoint.setPosition(Gravity.CENTER, Gravity.CENTER);


        tv_msg_count.setVisibility(View.VISIBLE);


        tv_msg_count.setText(count + "");

        //       showMsg(count);

        showMyToast(count);


    }


    public void hideMsg() {
        //  titleImgPoint.hide();
        tv_msg_count.setVisibility(View.GONE);

        if (toastView != null) {
            getWindowManager().removeView(toastView);
            toastView = null;
        }
    }

    private void hideMyToast() {
        if (toastView != null) {
            toastView.setVisibility(View.GONE);
        }

    }


    @Override
    public void onPause() {
        super.onPause();
        drawerLayout.closeDrawers();
    }


    /**
     * 自定义Toast显示我们的消息数量
     *
     * @param count
     */
    public void showMyToast(int count) {
        if (toastView != null && toastView.getVisibility() == View.VISIBLE) {
            getWindowManager().removeView(toastView);
            toastView = null;
        }

        toastView = new TextView(this);
        toastView.setTextColor(Color.WHITE);
        toastView.setTextSize(11);
        toastView.setBackgroundResource(R.drawable.cicle_msg);

        toastView.setText(count + "");
        toastView.setGravity(Gravity.CENTER);

        mParams = new WindowManager.LayoutParams();
        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;


        mParams.gravity = Gravity.LEFT + Gravity.TOP;

        mParams.x = DisplayUtil.dip2px(this, 30);
        mParams.y = DisplayUtil.dip2px(this, 10);
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//				| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        mParams.format = PixelFormat.TRANSLUCENT;
//		mParams.type = WindowManager.LayoutParams.TYPE_TOAST; Õ¡Àæ¥∞ÃÂÃÏ…˙≤ªœÏ”¶¥•√˛ ¬º˛
        mParams.type = WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;


        windowManager.addView(toastView, mParams);

        // ∏¯view∂‘œÛ…Ë÷√“ª∏ˆ¥•√˛ ¬º˛°£
//        toastView.setOnTouchListener(new View.OnTouchListener() {
//            int startX  ;
//            int startY  ;
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//
//                        startX = (int) event.getRawX();
//                        startY = (int) event.getRawY();
//
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//
//                        int newX = (int) event.getRawX();
//                        int newY = (int) event.getRawY();
//
//                        int dx = newX - startX;
//                        int dy = newY - startY;
//
//
//                        mParams.x +=dx;
//                        mParams.y +=dy;
//                        if(mParams.x<0){
//                            mParams.x = 0;
//                        }
//                        if(mParams.y<0){
//                            mParams.y = 0;
//                        }
//                        if(mParams.x>(windowManager.getDefaultDisplay().getWidth()-toastView.getWidth())){
//                            mParams.x=(windowManager.getDefaultDisplay().getWidth()-toastView.getWidth());
//                        }
//                        if(mParams.y>(windowManager.getDefaultDisplay().getHeight()-toastView.getHeight())){
//                            mParams.y=(windowManager.getDefaultDisplay().getHeight()-toastView.getHeight());
//                        }
//                        windowManager.updateViewLayout(toastView, mParams);
//                        startX = (int) event.getRawX();
//                        startY = (int) event.getRawY();
//                        break;
//
//                    case MotionEvent.ACTION_UP:
//
//                        SharedPreferences.Editor editor = sp.edit();
//                        editor.putInt("lastx", mParams.x);
//                        editor.putInt("lasty", mParams.y);
//                        editor.commit();
//                        break;
//                }
//                return true;
//            }
//        });
    }



    /**
     * ******重写的生命周期方法***************
     */
    @Override
    protected void onDestroy() {

        if (toastView != null) {
            getWindowManager().removeView(toastView);
        }

        if (swicthFrageReveiver != null) {
            unregisterReceiver(swicthFrageReveiver);
            swicthFrageReveiver=null;
        }

        super.onDestroy();
    }


    @Override
    public void onResume() {
        super.onResume();
        //     initMsgStatus();
   //     hideReponse();

    }








    public void setCurrentFragment(BaseFragment fragment) {
        this.currentFeed = fragment;
    }


    class SwicthFrageReveiver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {



            if (currentFeed instanceof NewsFragment) {
                drawerLayout.closeDrawers();
                return;
            }

            switchFragment(new NewsFragment(), "ll_msg");
            isIndex = false;
            hideMsg();

        }
    }



    public void hideReponse()
    {
        commonEnterRoot.setVisibility(View.GONE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==MainActivity.REFREST_CODE)

            if(resultCode== Activity.RESULT_OK)
            {

                View view=View.inflate(this,R.layout.toastmy,null);


                Toast toast = new Toast(this);
                toast.setGravity(Gravity.CENTER, 12, 40);
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setView(view);

                toast.show();
                if(currentFeed.needRefresh())
                    currentFeed.initFirstData();
            }
    }




}
