package com.qiyi.openapi.demo.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.qiyi.apilib.KeyConstant;
import com.qiyi.apilib.model.VideoInfo;
import com.qiyi.apilib.utils.LogUtils;
import com.qiyi.openapi.demo.activity.PlayerActivity;


public class QYPlayerUtils {
    private static String TAG = "QYPlayerUtils";
    /**
     * 跳转到播放器播放
     * @param context
     * @param aid
     * @param tid
     * @param videoInfo
     */
    public static void jumpToPlayerActivity(Context context, String aid, String tid, VideoInfo videoInfo) {
        LogUtils.i(TAG, "jumpToPlayerActivity aid: " + aid + " tid: " + tid);
        Intent intent = new Intent(context, PlayerActivity.class);
        intent.putExtra(KeyConstant.AID, aid);
        intent.putExtra(KeyConstant.TID, tid);
        intent.putExtra(KeyConstant.VIDEOINFO, videoInfo);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }
}
