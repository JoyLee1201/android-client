package com.shixian.android.client.controller;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.shixian.android.client.R;
import com.shixian.android.client.activities.BaseActivity;
import com.shixian.android.client.activities.DetailActivity;
import com.shixian.android.client.contants.AppContants;
import com.shixian.android.client.model.Comment;
import com.shixian.android.client.model.Feed2;
import com.shixian.android.client.model.News;

/**
 * Created by tangtang on 15/2/28.
 */
public class NewsOnClickController implements View.OnClickListener{



    private News news;
    private BaseActivity context;

    public NewsOnClickController(BaseActivity context,News news)
    {
        this.news=news;
        this.context=context;
    }





    @Override
    public void onClick(View v) {


        Intent intent=new Intent(context, DetailActivity.class);
        switch (v.getId())
        {
            case R.id.iv_icon:
            case R.id.tv_name:
                String userid;

                //跳转到项目主页
                Bundle bundle=new Bundle();

                bundle.putSerializable("user", news.user);
                userid = news.user.id;

                bundle.putInt("type", IndexOnClickController.USER_FRAGMENT);
                intent.putExtras(bundle);

                context.startActivity(intent);


//                context.switchFragment(uf,null);

//                Toast.makeText(context,"tv_name",Toast.LENGTH_SHORT).show();
                break;




            case R.id.tv_project:

                //跳转到项目主页

               bundle=new Bundle();




                bundle.putInt("type", IndexOnClickController.PROJECT_FRAGMENT);
                bundle.putString("project_id", news.project.getId()+"");

                intent.putExtras(bundle);

                context.startActivity(intent);


//                context.switchFragment(uf,null);

//                Toast.makeText(context,"tv_name",Toast.LENGTH_SHORT).show();

                break;
        }
    }
}
