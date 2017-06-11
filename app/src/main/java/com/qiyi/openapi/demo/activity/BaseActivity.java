package com.qiyi.openapi.demo.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.qiyi.openapi.demo.R;


public abstract class BaseActivity extends AppCompatActivity {
    protected View mRootView;
    protected ProgressBar mLoadingView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //状态栏和导航栏透明化
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(getResources().getColor(android.R.color.transparent));
            }
        }

        mRootView = LayoutInflater.from(this).inflate(getLayoutResourceId(), null, false);
        setContentView(mRootView);
        initView();
        loadData();
    }

    protected void initView() {
        View mProgressBar = mRootView.findViewById(R.id.loading_bar);
        if (mProgressBar != null) {
            mLoadingView = (ProgressBar) mProgressBar;
        }
    }

    protected void loadData() {

    }

    protected abstract int getLayoutResourceId();

    protected void showLoadingBar() {
        if (mLoadingView != null) {
            mLoadingView.setVisibility(View.VISIBLE);
        }
    }

    protected void hideLoadingBar() {
        if (mLoadingView != null) {
            mLoadingView.setVisibility(View.GONE);
        }
    }

}
