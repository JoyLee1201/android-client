package com.shixian.android.client.activities.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.shixian.android.client.R;
import com.shixian.android.client.activities.fragment.base.BaseFragment;
import com.shixian.android.client.contants.AppContants;
import com.shixian.android.client.model.News;
import com.shixian.android.client.utils.ApiUtils;
import com.shixian.android.client.utils.DisplayUtil;
import com.shixian.android.client.utils.ImageCache;
import com.shixian.android.client.utils.ImageCallback;
import com.shixian.android.client.utils.ImageDownload;
import com.shixian.android.client.utils.JsonUtils;
import com.shixian.android.client.utils.SharedPerenceUtil;
import com.shixian.android.client.utils.TimeUtil;
import com.shixian.android.client.views.pulltorefreshlist.PullToRefreshBase;
import com.shixian.android.client.views.pulltorefreshlist.PullToRefreshListView;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by doom on 15/2/8.
 */
public class NewsFragment extends BaseFragment
{
    private static final String TAG = "NewsFragment";
    private ImageCallback callback;
    private PullToRefreshListView pullToRefreshListView;
    private List<News> newsList;
    private String firstPageData;

    private NewsAdapter adapter;

    private int page=1;



    @Override
    public View initView(LayoutInflater inflater) {

        View view=inflater.inflate(R.layout.fragment_index,null,false);

        context.setLable("消息");

        pullToRefreshListView = (PullToRefreshListView) view.findViewById(R.id.lv_index);
        // 滚动到底自动加载可用
        pullToRefreshListView.setScrollLoadEnabled(true);

        // 设置下拉刷新的listener
        pullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {

            //下拉舒心完成
            @Override
            public void onPullDownToRefresh(
                    PullToRefreshBase<ListView> refreshView) {

                //上啦刷新
                Log.i("AAAA", "1111-------------------------------------------------------------------");
                initFirst();
            }

            @Override
            public void onPullUpToRefresh(
                    PullToRefreshBase<ListView> refreshView) {
                //getNewsList(moreUrl, false);
                //下拉加载更多
                Log.i("AAAA","-------------------------------------------------------------------");
                getNextData();



            }
        });

        newsList = new ArrayList<News>();


        initCacheData();

        return view;
    }

    private void initCacheData() {

        newsList=JsonUtils.parseNews(SharedPerenceUtil.getNews(context)) ;


        if (adapter == null) {
            adapter = new NewsAdapter();
            pullToRefreshListView.getListView().setAdapter(adapter);

        } else {
            adapter.notifyDataSetChanged();
        }


    }

    @Override
    public void initDate(Bundle savedInstanceState) {

        initImageCallBack();
        if(newsList!=null&&newsList.size()>0)
        {
            if (adapter == null) {
                adapter = new NewsAdapter();


                pullToRefreshListView.getRefreshableView().setAdapter(adapter);
            } else {
                pullToRefreshListView.getRefreshableView().setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }


        }else{

            initFirst();
        }

    }

    private void initFirst() {
        initImageCallBack();
        context.showProgress();
        ApiUtils.get(AppContants.NOTIFICATION_URL,null,new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {

                final String temp=new String(bytes);
                if(!AppContants.errorMsg.equals(temp))
                {
                    firstPageData=temp;

                    new Thread(){
                        public  void run()
                        {
                            newsList= JsonUtils.parseNews(temp);


                            SharedPerenceUtil.putNews(context,temp);
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(adapter==null)
                                    {
                                        adapter=new NewsAdapter();
                                        pullToRefreshListView.getListView().setAdapter(adapter);
                                    }else{
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                    }.start();
                    page=1;

                    pullToRefreshListView.onPullDownRefreshComplete();
                    context.dissProgress();


                }

            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                Toast.makeText(context, R.string.check_net, Toast.LENGTH_SHORT);
                pullToRefreshListView.onPullDownRefreshComplete();
                context.dissProgress();
            }
        });
    }


    public void getNextData()
    {
        page+=1;
        RequestParams params=new RequestParams();
        params.add("page",page+"");
        ApiUtils.get(AppContants.NOTIFICATION_URL,params,new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {

                final String temp=new String(bytes);
                if(!AppContants.errorMsg.equals(temp))
                {
                    firstPageData=temp;

                    new Thread(){
                        public  void run()
                        {
                            newsList.addAll(JsonUtils.parseNews(temp));


                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(adapter==null)
                                    {
                                        adapter=new NewsAdapter();
                                        pullToRefreshListView.getListView().setAdapter(adapter);
                                    }else{
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                    }.start();


                    pullToRefreshListView.onPullUpRefreshComplete();


                }

            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                Toast.makeText(context, R.string.check_net, Toast.LENGTH_SHORT);
                pullToRefreshListView.onPullUpRefreshComplete();
                page-=1;
            }
        });
    }





    protected void initImageCallBack() {
        this.callback = new ImageCallback() {

            @Override
            public void imageLoaded(Bitmap bitmap, Object tag) {
                ImageView imageView = (ImageView) pullToRefreshListView.getListView()
                        .findViewWithTag(tag);

                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        };
    }


    class NewsAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return newsList.size();
        }

        @Override
        public Object getItem(int position) {
            return newsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view=null;
            final NewsHolder holder;
            if(convertView==null)
            {
                view=View.inflate(context,R.layout.news_item,null);
                holder=new NewsHolder();
                view.setTag(holder);
                holder.iv_icon= (ImageView) view.findViewById(R.id.iv_icon);
                holder.tv_name= (TextView) view.findViewById(R.id.tv_name);
                holder.tv_type= (TextView) view.findViewById(R.id.tv_type);
                holder.tv_project= (TextView) view.findViewById(R.id.tv_project);
                holder.tv_time= (TextView) view.findViewById(R.id.tv_time);
                holder.tv_content= (TextView) view.findViewById(R.id.tv_content);
                holder.bt_accept= (Button) view.findViewById(R.id.bt_accept);
                holder.tv_add= (TextView) view.findViewById(R.id.tv_add);
                holder.tv_add_pri= (TextView) view.findViewById(R.id.tv_addpri);
                holder.tv_post_type=(TextView)view.findViewById(R.id.tv_post_type);

            }else{
                view=convertView;
                holder= (NewsHolder) view.getTag();

            }

            final News news=newsList.get(position);

            holder.bt_accept.setVisibility(View.GONE);
            holder.tv_content.setVisibility(View.VISIBLE);
            holder.tv_time.setText(TimeUtil.getDistanceTime(news.created_at));
            holder.tv_project.setVisibility(View.VISIBLE);
            holder.tv_add.setVisibility(View.VISIBLE);
            holder.tv_add_pri.setVisibility(View.GONE);
            holder.tv_post_type.setVisibility(View.GONE);

            //头像图片处理
            String keys[] = news.user.avatar.small.url.split("/");
            String key = keys[keys.length - 1];
            Bitmap bm = ImageCache.getInstance().get(key);
            if (bm != null) {
                holder.iv_icon.setImageBitmap(bm);
            } else {
                holder.iv_icon.setImageResource(R.drawable.ic_launcher);
                if (callback != null) {
                    new ImageDownload(callback).execute(AppContants.DOMAIN + news.user.avatar.small.url, key, ImageDownload.CACHE_TYPE_LRU);
                }
            }

            ViewGroup.LayoutParams params = holder.iv_icon.getLayoutParams();
            int imageSize= DisplayUtil.dip2px(context, 40);
            params.height=imageSize;
            params.width =imageSize;
            holder.iv_icon.setLayoutParams(params);
            holder.iv_icon.setTag(key);



            /**
             *
             * type 分为Notification 和 Request
             *
             */
            if("Notification".equals(news.type))
            {
                switch (news.noti_type)
                {
                    case "invit_follow":
                        holder.tv_content.setVisibility(View.GONE);
                        holder.tv_project.setText(news.project.getTitle());
                        holder.tv_name.setText(news.user.username);
                        holder.tv_type.setText(R.string.please_fllow);
                        holder.tv_add.setVisibility(View.GONE);
                        break;
                    case "join_accept":
                        holder.tv_content.setVisibility(View.GONE);
                        holder.tv_project.setText(Html.fromHtml(getResources().getString(R.string.project_join_accept).replace("{project_title}", news.project.getTitle())));
                        holder.tv_name.setText(news.user.username);
                        holder.tv_type.setText(R.string.join_accept);
                        holder.tv_add.setText("请求");
                        holder.tv_add_pri.setVisibility(View.VISIBLE);
                        holder.tv_add_pri.setText("的");
                        break;
                    case "join_reject":
                        holder.tv_content.setVisibility(View.GONE);
                        holder.tv_project.setText(Html.fromHtml(getResources().getString(R.string.project_join_accept).replace("{project_title}", news.project.getTitle())));
                        holder.tv_name.setText(news.user.username);
                        holder.tv_type.setText(R.string.join_reject);
                        holder.tv_add.setText("请求");
                        holder.tv_add_pri.setVisibility(View.VISIBLE);
                        holder.tv_add_pri.setText("的");
                        break;
                    case "new_comment":
                        holder.tv_content.setVisibility(View.VISIBLE);
                        holder.tv_project.setText(news.project.getTitle());
                        holder.tv_name.setText(news.user.username);
                        holder.tv_type.setText(Html.fromHtml(getResources().getString(R.string.addcomment)));
                        holder.tv_content.setText(Html.fromHtml(news.data.content_html));
                        holder.tv_post_type.setVisibility(View.VISIBLE);
                         holder.tv_post_type.setText("回复");
                        holder.tv_add.setVisibility(View.GONE);
                        break;
                    case "new_entity":
                        switch (news.notifiable_type)
                        {
                            case "Attachment":
                                holder.tv_content.setVisibility(View.VISIBLE);
                                holder.tv_project.setText(Html.fromHtml(getResources().getString(R.string.project_attachment).replace("{project_title}",news.project.getTitle())));
                                holder.tv_name.setText(news.user.username);
                                holder.tv_type.setText(R.string.attachment);
                                holder.tv_content.setText(Html.fromHtml(news.data.content_html));
                                holder.tv_add.setText("文件");
                                holder.tv_add_pri.setVisibility(View.VISIBLE);
                                holder.tv_add_pri.setText("添加了新的");

                                break;
                            case "Homework":
                                holder.tv_content.setVisibility(View.VISIBLE);
                                holder.tv_project.setText(Html.fromHtml(getResources().getString(R.string.project_task).replace("{project_title}",news.project.getTitle())));
                                holder.tv_name.setText(news.user.username);
                                holder.tv_type.setText(R.string.attachment);
                                holder.tv_content.setText(Html.fromHtml(news.data.content_html));
                                holder.tv_add.setText("任务提交");
                                holder.tv_add_pri.setVisibility(View.VISIBLE);
                                holder.tv_add_pri.setText("添加了新的");

                                break;
                            case "Idea":
                                holder.tv_content.setVisibility(View.VISIBLE);
                                holder.tv_project.setText(Html.fromHtml(getResources().getString(R.string.project_idea).replace("{project_title}",news.project.getTitle())));
                                holder.tv_name.setText(news.user.username);
                                holder.tv_type.setText(R.string.attachment);
                                holder.tv_add.setText("想法");
                                holder.tv_content.setText(Html.fromHtml(news.data.content_html));
                                holder.tv_add_pri.setVisibility(View.VISIBLE);
                                holder.tv_add_pri.setText("添加了新的");

                                break;

                            case "Image":
                                holder.tv_content.setVisibility(View.VISIBLE);
                                holder.tv_project.setVisibility(View.VISIBLE);
                                holder.tv_project.setText(Html.fromHtml(getResources().getString(R.string.project_image).replace("{project_title}",news.project.getTitle())));
                                holder.tv_name.setText(news.user.username);
                                holder.tv_type.setText(R.string.attachment);
                                holder.tv_content.setText(Html.fromHtml(news.data.content_html));
                                holder.tv_add.setText("图片");
                                holder.tv_add_pri.setVisibility(View.VISIBLE);
                                holder.tv_add_pri.setText("添加了新的");
                                break;
                            case "Plan":
                                holder.tv_content.setVisibility(View.VISIBLE);
                                holder.tv_project.setVisibility(View.VISIBLE);
                                holder.tv_project.setText(Html.fromHtml(getResources().getString(R.string.project_plan).replace("{project_title}",news.project.getTitle())));
                                holder.tv_name.setText(news.user.username);
                                holder.tv_type.setText(R.string.attachment);
                                holder.tv_add.setText("计划");
                                holder.tv_add_pri.setVisibility(View.VISIBLE);
                                holder.tv_add_pri.setText("添加了新的");
                                if(news.data!=null)
                                    holder.tv_content.setText(news.data.content);
                                break;
                            case "Vote":
                                holder.tv_content.setVisibility(View.VISIBLE);
                                holder.tv_project.setText(Html.fromHtml(getResources().getString(R.string.project_vote).replace("{project_title}",news.project.getTitle())));
                                holder.tv_name.setText(news.user.username);
                                holder.tv_type.setText(R.string.attachment);
                                holder.tv_content.setText(Html.fromHtml(news.data.content_html));
                                holder.tv_add.setText("投票");
                                holder.tv_add_pri.setVisibility(View.VISIBLE);
                                holder.tv_add_pri.setText("添加了新的");
                                break;



                        }


                        break;

                    case "UserRelation":
                        holder.tv_content.setVisibility(View.GONE);
                        holder.tv_project.setVisibility(View.GONE);
                        holder.tv_name.setText(news.user.username);
                        holder.tv_type.setText("关注了你");
                        holder.tv_add.setVisibility(View.GONE);
                        break;

                    case "new_follow":
                        holder.tv_add.setVisibility(View.GONE);
                        holder.tv_content.setVisibility(View.GONE);
                        holder.tv_project.setVisibility(View.GONE);
                        holder.tv_name.setText(news.user.username);
                        holder.tv_type.setText("关注了你");

                        break;

                    case "new_homework":
                        holder.tv_content.setVisibility(View.VISIBLE);
                        holder.tv_project.setText(Html.fromHtml(getResources().getString(R.string.project_homework).replace("{project_title}",news.project.getTitle())));
                        holder.tv_name.setText(news.user.username);
                        holder.tv_type.setText(R.string.attachment);
                        holder.tv_content.setText(Html.fromHtml(news.data.content_html));
                        holder.tv_add.setText("任务");
                        holder.tv_add_pri.setVisibility(View.VISIBLE);
                        holder.tv_add_pri.setText("完成了您分配的");
                        break;
                    case "new_mention":

                        holder.tv_add.setVisibility(View.GONE);
                        switch (news.notifiable_type)
                        {
                            case "Attachment":
                                holder.tv_project.setVisibility(View.GONE);

                                holder.tv_content.setVisibility(View.VISIBLE);
                                holder.tv_name.setText(news.user.username);
                                holder.tv_type.setText(Html.fromHtml(getResources().getString(R.string.new_mention_attachment)));
                                holder.tv_content.setText(Html.fromHtml(news.data.content_html));


                                break;
                            case "Comment":
                                holder.tv_project.setVisibility(View.GONE);

                                holder.tv_content.setVisibility(View.VISIBLE);
                                holder.tv_name.setText(news.user.username);
                                holder.tv_type.setText(Html.fromHtml(getResources().getString(R.string.new_mention_comment)));
                                holder.tv_content.setText(Html.fromHtml(news.data.content_html));
                                break;

                            case "Homework":
                                holder.tv_project.setVisibility(View.GONE);

                                holder.tv_content.setVisibility(View.VISIBLE);
                                holder.tv_name.setText(news.user.username);
                                holder.tv_type.setText(Html.fromHtml(getResources().getString(R.string.new_mention_homework)));
                                holder.tv_content.setText(Html.fromHtml(news.data.content_html));
                                break;

                            case "Idea":
                                holder.tv_project.setVisibility(View.GONE);

                                holder.tv_content.setVisibility(View.VISIBLE);
                                holder.tv_name.setText(news.user.username);
                                holder.tv_type.setText(Html.fromHtml(getResources().getString(R.string.new_mention_idea)));
                                holder.tv_content.setText(Html.fromHtml(news.data.content_html));
                                break;

                            case "Image":
                                holder.tv_project.setVisibility(View.GONE);

                                holder.tv_content.setVisibility(View.VISIBLE);
                                holder.tv_name.setText(news.user.username);
                                holder.tv_type.setText(Html.fromHtml(getResources().getString(R.string.new_mention_image)));
                                holder.tv_content.setText(Html.fromHtml(news.data.content_html));
                                break;

                            case "task":
                                holder.tv_project.setVisibility(View.GONE);

                                holder.tv_content.setVisibility(View.VISIBLE);
                                holder.tv_name.setText(news.user.username);
                                holder.tv_type.setText(Html.fromHtml(getResources().getString(R.string.new_mention_task)));
                                holder.tv_content.setText(Html.fromHtml(news.data.content_html));
                                break;

                        }
                        break;

                    case "new_reply":
                        holder.tv_project.setText(news.project.getTitle());
                        holder.tv_content.setVisibility(View.VISIBLE);
                        holder.tv_name.setText(news.user.username);
                        holder.tv_type.setText(Html.fromHtml(getResources().getString(R.string.new_reply)));
                        holder.tv_content.setText(Html.fromHtml(news.data.content_html));
                        holder.tv_post_type.setVisibility(View.VISIBLE);
                        holder.tv_post_type.setText("回复");
                        holder.tv_add.setVisibility(View.GONE);


                        break;

                    case "new_task":

                        holder.tv_project.setVisibility(View.VISIBLE);

                        holder.tv_content.setVisibility(View.VISIBLE);
                        holder.tv_name.setText(news.user.username);
                        holder.tv_type.setText(Html.fromHtml("在"));

                        holder.tv_content.setText(Html.fromHtml(news.data.content_html));
                        holder.tv_project.setText(Html.fromHtml(getResources().getString(R.string.project_new_task).replace("{project_title}",news.project.getTitle())));
                        holder.tv_add.setText("任务");
                        holder.tv_add_pri.setVisibility(View.VISIBLE);
                        holder.tv_add_pri.setText("给你分配了");
                        break;

                }
            }else {
                holder.tv_project.setVisibility(View.VISIBLE);
                holder.bt_accept.setVisibility(View.GONE);

                holder.tv_content.setVisibility(View.VISIBLE);

                holder.tv_name.setText(news.user.username);
                holder.tv_type.setText("请求加入项目 ");
                holder.tv_content.setText(Html.fromHtml(news.data.content_html));
                holder.tv_project.setText(news.project.getTitle());
                holder.tv_add.setVisibility(View.GONE);

            }


            View.OnClickListener onClickListener=new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    MsgDetialFragment fragment=new MsgDetialFragment();
                    Bundle bundle=new Bundle();
                    fragment.setArguments(bundle);
                    bundle.putSerializable("news",news);
                    context.switchFragment(fragment,null);
                }
            };



            if(holder.tv_add.VISIBLE==View.VISIBLE)
            {
                holder.tv_add.setOnClickListener(onClickListener);
            }
            if(holder.tv_type.VISIBLE==View.VISIBLE)
            {
                holder.tv_type.setOnClickListener(onClickListener);
            }

            if(holder.tv_content.VISIBLE==View.VISIBLE)
            {
                holder.tv_content.setOnClickListener(onClickListener);
            }

            if(holder.tv_post_type.VISIBLE==View.VISIBLE)
            {
                holder.tv_post_type.setOnClickListener(onClickListener);
            }












            return view;
        }





    }

    class NewsHolder{
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_type;
        TextView tv_project;
        TextView tv_time;
        TextView tv_content;
        Button bt_accept;
        TextView tv_add;
        TextView tv_add_pri;
        TextView tv_post_type;
    }

}
