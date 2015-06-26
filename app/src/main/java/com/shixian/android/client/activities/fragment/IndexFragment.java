package com.shixian.android.client.activities.fragment;


import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.shixian.android.client.Global;
import com.shixian.android.client.R;
import com.shixian.android.client.activities.MainActivity;
import com.shixian.android.client.activities.fragment.base.BaseFeedFragment;
import com.shixian.android.client.contants.AppContants;
import com.shixian.android.client.controller.ArgeeOnClickController;
import com.shixian.android.client.controller.IndexOnClickController;
import com.shixian.android.client.engine.CommonEngine;
import com.shixian.android.client.handler.feed.BaseFeedHandler;
import com.shixian.android.client.model.Comment;
import com.shixian.android.client.model.Feed2;
import com.shixian.android.client.model.feeddate.BaseFeed;
import com.shixian.android.client.utils.CommonUtil;
import com.shixian.android.client.utils.JsonUtils;
import com.shixian.android.client.utils.SharedPerenceUtil;
import org.apache.http.Header;

import java.util.List;

/**
 * Created by s0ng on 2015/2/10.
 */
public class IndexFragment extends BaseFeedFragment {

    private String TAG = "IndexFragment";

    private int page = 1;


    private String firstPageDate;




    protected void initCacheData() {

        firstPageDate = SharedPerenceUtil.getIndexFeed(context.getApplicationContext());
        feedList = JsonUtils.ParseFeeds(firstPageDate);
        if (adapter == null) {
            adapter = new FeedAdapter();
            pullToRefreshListView.getListView().setAdapter(adapter);

        } else {
           pullToRefreshListView.getListView().setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }


    }

    @Override
    public void initDate(Bundle savedInstanceState) {

        if (feedList != null && feedList.size() > 0) {
            if (adapter == null) {
                adapter = new FeedAdapter();
                pullToRefreshListView.getRefreshableView().setAdapter(adapter);
            } else {
             //   pullToRefreshListView.getRefreshableView().setAdapter(adapter);
                //adapter.notifyDataSetChanged();

            }



        } else {

            initFirst();
        }
    }

    /*********************************************获取数据*******************************/
    /**
     * 初始化第一页数据
     */
    public void initFirstData() {


        ((MainActivity)context).initMsgStatus();

        page = 1;
        context.showProgress();
        CommonEngine.getFeedData(context,AppContants.INDEX_URL, page, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, final byte[] bytes) {
                final String temp = new String(bytes);
                if (!AppContants.errorMsg.equals(temp)) {

                    new Thread() {
                        public void run() {
                            //获取第一页数据
                            firstPageDate = temp;
                            //数据格式
                            CommonUtil.logDebug(TAG, new String(bytes));


                            final List<BaseFeed> tempList = JsonUtils.ParseFeeds(firstPageDate);


                            //保存数据到本地
                            page = 1;


                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    feedList=tempList;
                                    if (adapter == null) {
                                        adapter = new FeedAdapter();


                                        pullToRefreshListView.getRefreshableView().setAdapter(adapter);
                                    } else {
                                        adapter.notifyDataSetChanged();
                                    }

                                    pullToRefreshListView.onPullDownRefreshComplete();
                                    context.dissProgress();

                                }
                            });


                            SharedPerenceUtil.putIndexFeed(context.getApplicationContext(), firstPageDate);


                        }
                    }.start();


                } else {
                    pullToRefreshListView.onPullDownRefreshComplete();
                }
            }


            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {


                if(isAdded())
                {
                    Toast.makeText(context, getString(R.string.check_net), Toast.LENGTH_SHORT).show();
                    pullToRefreshListView.onPullDownRefreshComplete();
                    context.dissProgress();
                }


            }
        });
    }

    @Override
    public boolean needRefresh() {
        return true;
    }


    /**
     * 获取其他页数据
     */
    public void getNextData() {
        ((MainActivity)context).initMsgStatus();
        page += 1;
        CommonEngine.getFeedData(context,AppContants.INDEX_URL, page, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, final byte[] bytes) {

                final String temp = new String(bytes);
                if (!AppContants.errorMsg.equals(temp)) {

                    new Thread() {
                        public void run() {
                            //获取第一页数据

                            //数据格式
                            CommonUtil.logDebug(TAG, new String(temp));


                            feedList.addAll(JsonUtils.ParseFeeds(temp));


                            //保存数据到本地

                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (adapter == null) {
                                        adapter = new FeedAdapter();

                                        pullToRefreshListView.getRefreshableView().setAdapter(adapter);
                                    } else {
                                        adapter.notifyDataSetChanged();
                                    }

                                    pullToRefreshListView.onPullUpRefreshComplete();


                                }
                            });


                        }

                    }.start();


                } else {
                    pullToRefreshListView.onPullUpRefreshComplete();
                }
            }


            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {

//                Log.i("AAAA", new String(bytes));

                //TODO 错误可能定义的不是太准确  最后一天调整
                if (isAdded()) {
                    Toast.makeText(context, getString(R.string.check_net), Toast.LENGTH_SHORT).show();
                    pullToRefreshListView.onPullUpRefreshComplete();
                }
                page -= 1;
            }
        });
    }

    @Override
    protected void initFirst() {
        initCacheData();

        initFirstData();

    }



    /**
     * **************************************************************************************
     */
    @Override
    protected void initLable() {
        context.setLable(getString(R.string.label_index));
    }





    /************************************Adapter**********************************************/

    /**
     * adapter对象
     */
    class FeedAdapter extends BaseFeedAdapter {


        @Override
        public int getCount() {
            return feedList.size();
        }

        @Override
        public Object getItem(int position) {
            return feedList.get(position);
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
            return feedList.get(position).type;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {


            View view = null;


            int itemType = getItemViewType(position);
            switch (itemType) {
                case BaseFeed.TYPE_FEED:
                    view=BaseFeedHandler.initFeedItemView2(context, convertView);
                    FeedHolder feedHolder= (FeedHolder) view.getTag();

/******************************************************************/
                    Feed2 feed = (Feed2) feedList.get(position );
                    feed.position=position;
                    BaseFeedHandler.initFeedItemViewData(context, feed, feedHolder, animateFirstListener);
/**************************************************/
                    initFeedItemOnClick(feed, feedHolder);
                    BaseFeedHandler.setFeedCommonClick(feed.data.user,context,feed,feedHolder);
                  // setFeedCommonClick(feed,feedHolder);

                    BaseFeedHandler.setTypeAgreeFeed(feedList,adapter,context,feed,feedHolder);

                    break;

                case BaseFeed.TYPE_COMMENT:
                    view= BaseFeedHandler.initCommentItem(context, convertView);
                    CommentHolder commentHolder = (CommentHolder) view.getTag();


/**************************************************************/
                    Comment comment = (Comment) feedList.get(position);
                    comment.position=position;
                    BaseFeedHandler.initCommentItemData(comment, commentHolder, animateFirstListener);


                    BaseFeedHandler.setCommentLogClickListener(commentHolder.tv_content,adapter,feedList,comment);
/******************************************/

                    initCommentItemOnClick(comment, commentHolder, adapter,feedList);


                    break;
            }

            return view;

        }
    }






    /*****************************Adapter的监听事件函数********************/
    protected void initFeedItemOnClick(final Feed2 feed,final FeedHolder feedHolder)
    {
        IndexOnClickController controller = new IndexOnClickController(context, feed);
        feedHolder.iv_icon.setOnClickListener(controller);
        feedHolder.tv_name.setOnClickListener(controller);
        //项目
        feedHolder.tv_proect.setOnClickListener(controller);

//        if (feedHolder.tv_content.getVisibility() == View.VISIBLE) {
//
//            feedHolder.tv_content.setOnClickListener(controller);
//
//        }


        if (feedHolder.iv_content.getVisibility() == View.VISIBLE) {

            if ( "Attachment".equals(feed.feedable_type)) {
                feedHolder.iv_content.setOnClickListener(controller);
            }

        }

        if (feedHolder.tv_response.getVisibility() == View.VISIBLE) {
            feedHolder.tv_response.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //这里我需要得到最后一条评论的位置  该如何是好呢 ？
                    //是否要在feed中增加一条纪录该feed所有的评论数  还是有其他更好的方法  增加评论数到不难
                    //但是这肯定不是优雅的做法  由于一开始没有好好的构思 现在可能考虑投机取巧的方法去解决

                        popComment(v, feed, listView,1);

                    //也只能这么做了

                }


            });

        }
    }


    /**
     * 这里又又非常恶心的改需求  简直是反人类的设计
     * @param comment
     * @param commentHolder
     */
    protected void initCommentItemOnClick(final Comment comment,CommentHolder commentHolder,BaseAdapter adapter,List<BaseFeed> feedList) {

        IndexOnClickController controller = new IndexOnClickController(context, comment);
        commentHolder.iv_icon.setOnClickListener(controller);
        commentHolder.tv_name.setOnClickListener(controller);




        if (commentHolder.tv_content.getVisibility() == View.VISIBLE) {

            if(comment.user.username.equals(Global.USER_NAME))
            {
                BaseFeedHandler.setCommentOnCliekListener(commentHolder.tv_content,adapter,feedList,comment);
            }else{
                commentHolder.tv_content.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popComment(v, comment, listView,0);
                    }
                });
            }


        }




        if (commentHolder.tv_response.getVisibility() == View.VISIBLE) {
            commentHolder.tv_response.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //这里我需要得到最后一条评论的位置  该如何是好呢 ？
                    //是否要在feed中增加一条纪录该feed所有的评论数  还是有其他更好的方法  增加评论数到不难
                    //但是这肯定不是优雅的做法  由于一开始没有好好的构思 现在可能考虑投机取巧的方法去解决

                        popComment(v, comment.parent, listView,1);
                    }
                    //也只能这么做了



            });

        }
    }


    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)context).initMsgStatus();
        ((MainActivity)context).setCurrentFragment(this);

    }




}
