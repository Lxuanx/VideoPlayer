package com.qiyi.openapi.demo;

import android.app.Application;

import com.qiyi.apilib.ApiLib;
import com.qiyi.video.playcore.QiyiVideoView;


public class ApiDemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ApiLib.CONTEXT = this.getApplicationContext();
        QiyiVideoView.init(this);
    }
}
