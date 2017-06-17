package com.qiyi.openapi.demo.activity;

import android.content.Intent;
import android.os.Handler;
import android.view.MotionEvent;

import com.qiyi.openapi.demo.HomeActivity;
import com.qiyi.openapi.demo.R;

public class SplashActivity extends BaseActivity {

    private Handler mHandler;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_splash;
    }


    @Override
    public void loadData() {
        delayedEnterHome();
    }

    private void delayedEnterHome() {
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                enterHome();
            }
        }, 3000);
    }

    private void enterHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * 点击屏幕可以立即进入首页
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mHandler.removeCallbacksAndMessages(null);
                enterHome();
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        // 按返回键什么事情都不做
    }
}
