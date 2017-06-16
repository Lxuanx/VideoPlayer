package com.qiyi.openapi.demo.activity;

import android.support.v4.app.FragmentTransaction;

import com.qiyi.apilib.KeyConstant;
import com.qiyi.openapi.demo.R;
import com.qiyi.openapi.demo.fragment.ChannelFragment;


public class ChannelActivity extends BaseActivity {

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_channel;
    }

    @Override
    protected void initView() {
        super.initView();
        String channelId = getIntent().getStringExtra(KeyConstant.CHANNELID);
        String channelName = getIntent().getStringExtra(KeyConstant.CHANNELNAME);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_channel_holder, ChannelFragment.newInstance(channelId, channelName)).commitAllowingStateLoss();
    }
}