package com.shixian.android.client.activities.fragment.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shixian.android.client.activities.BaseActivity;

/**
 * Created by s0ng on 2015/2/9.
 */
public abstract class BaseFragment extends Fragment  {


    protected BaseActivity context;





    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        context= (BaseActivity) getActivity();

        View view=initView(inflater);
        initDate(savedInstanceState);
        return view;
    }

    public abstract View initView(LayoutInflater inflater);
    public abstract void initDate(Bundle savedInstanceState);

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }


    public abstract  void setCurrentPosition(int position);
}
