package com.qiyi.openapi.demo.activity;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.qiyi.apilib.ApiLib;
import com.qiyi.apilib.utils.LogUtils;
import com.qiyi.apilib.utils.StringUtils;
import com.qiyi.apilib.utils.UiUtils;
import com.qiyi.openapi.demo.R;
import com.qiyi.video.playcore.ErrorCode;
import com.qiyi.video.playcore.IQYPlayerHandlerCallBack;
import com.qiyi.video.playcore.QiyiVideoView;

import java.util.concurrent.TimeUnit;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;


public class PlayerActivity extends BaseActivity {
    private static final int PERMISSION_REQUEST_CODE = 7171;
    private static final String TAG = PlayerActivity.class.getSimpleName();

    private static final int HANDLER_MSG_UPDATE_PROGRESS = 1;
    private static final int HANDLER_MSG_CLOSE_SMALL_CONTROL = 2;
    private static final int HANDLER_DEPLAY_UPDATE_PROGRESS = 1000; // 1s
    private static final int HANDLER_DEPLAY_CLOSE_SMALL_CONTROL = 3000; // 3s

    private boolean isFullScreen = false;
    private int[] screen;

    private QiyiVideoView mVideoView;
    private SeekBar mSeekBar;
    private ImageButton mPlayPauseIb;
    private TextView mCurrentTime;
    private TextView mTotalTime;
    private LinearLayout mSmallControlLly;
    private ImageButton mPlayerFullIb;
    private String mTid;
    private String mAid;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_player;
    }

    @Override
    protected void initView() {
        super.initView();
        screen = UiUtils.getScreenWidthAndHeight(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mAid = getIntent().getStringExtra("aid");
        mTid = getIntent().getStringExtra("tid");
        if (StringUtils.isEmpty(mTid)) {
            finish();
            return;
        }
        mSmallControlLly = (LinearLayout) findViewById(R.id.small_control_lly);

        mVideoView = (QiyiVideoView) findViewById(R.id.player_vv);
        //mVideoView.setPlayData("667737400");
        mVideoView.setPlayData(mTid);
        //设置回调，监听播放器状态
        setPlayerCallback();
        mVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSmallControlLly.getVisibility() != View.VISIBLE) {
                    mSmallControlLly.setVisibility(View.VISIBLE);
                    mMainHandler.sendEmptyMessageDelayed(HANDLER_MSG_CLOSE_SMALL_CONTROL, HANDLER_DEPLAY_CLOSE_SMALL_CONTROL);
                } else {
                    mSmallControlLly.setVisibility(View.GONE);
                    mMainHandler.removeMessages(HANDLER_MSG_CLOSE_SMALL_CONTROL);
                }
            }
        });

        mCurrentTime = (TextView) findViewById(R.id.current_time_tv);
        mTotalTime = (TextView) findViewById(R.id.total_time_tv);

        mPlayPauseIb = (ImageButton) findViewById(R.id.player_pause_ib);
        mPlayPauseIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mVideoView.isPlaying()) {
                    mVideoView.pause();
                    Glide.clear(mPlayPauseIb);
                    Glide.with(ApiLib.CONTEXT).load(R.drawable.player_pause).into(mPlayPauseIb);
                    mMainHandler.removeMessages(HANDLER_MSG_UPDATE_PROGRESS);
                } else {
                    mVideoView.start();
                    Glide.clear(mPlayPauseIb);
                    Glide.with(ApiLib.CONTEXT).load(R.drawable.player_full).into(mPlayPauseIb);
                    mMainHandler.sendEmptyMessageDelayed(HANDLER_MSG_UPDATE_PROGRESS, HANDLER_DEPLAY_UPDATE_PROGRESS);
                }
            }
        });

        mPlayerFullIb = (ImageButton) findViewById(R.id.play_full_ib);
        mPlayerFullIb.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onClick(View v) {
                int current = mVideoView.getCurrentPosition();
                if (isFullScreen) {
                    isFullScreen = false;
                    mVideoView.getLayoutParams().width = screen[0];
                    mVideoView.getLayoutParams().height = getResources().getDimensionPixelSize(R.dimen.aqy_video_view_height);
                    mVideoView.requestLayout();
                    setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    isFullScreen = true;
                    mVideoView.getLayoutParams().width = screen[1];
                    mVideoView.getLayoutParams().height = screen[0];
                    mVideoView.requestLayout();
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
                mVideoView.seekTo(current);
            }
        });

        mSeekBar = (SeekBar) findViewById(R.id.progress_seekbar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private int mProgress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                LogUtils.d(TAG, "onProgressChanged, progress = " + progress + ", fromUser = " + fromUser);
                if (fromUser) {
                    mProgress = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSeekBar.setProgress(mProgress);
                mVideoView.seekTo(mProgress);
            }
        });

    }

    private void setPlayerCallback() {
        mVideoView.setPlayerCallBack(mCallBack);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (null != mVideoView) {
            mVideoView.start();
        }
        mMainHandler.sendEmptyMessageDelayed(HANDLER_MSG_UPDATE_PROGRESS, HANDLER_DEPLAY_UPDATE_PROGRESS);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != mVideoView) {
            mVideoView.pause();
        }
        mMainHandler.removeMessages(HANDLER_MSG_UPDATE_PROGRESS);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMainHandler.removeCallbacksAndMessages(null);
        mVideoView.release();
        mVideoView = null;
    }

    /**
     * Convert ms to hh:mm:ss
     *
     * @param millis
     * @return
     */
    private String ms2hms(int millis) {
        return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }

    /**
     * 每一秒查询和更新进度条
     */
    private Handler mMainHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            LogUtils.d(TAG, "handleMessage, msg.what = " + msg.what);
            switch (msg.what) {
                case HANDLER_MSG_UPDATE_PROGRESS:
                    int duration = mVideoView.getDuration();
                    int progress = mVideoView.getCurrentPosition();
                    LogUtils.d(TAG, "HANDLER_MSG_UPDATE_PROGRESS, duration = " + duration + ", currentPosition = " + progress);
                    if (duration > 0) {
                        mSeekBar.setMax(duration);
                        mSeekBar.setProgress(progress);

                        mTotalTime.setText(ms2hms(duration));
                        mCurrentTime.setText(ms2hms(progress));
                    }
                    mMainHandler.sendEmptyMessageDelayed(HANDLER_MSG_UPDATE_PROGRESS, HANDLER_DEPLAY_UPDATE_PROGRESS);
                    break;
                case HANDLER_MSG_CLOSE_SMALL_CONTROL:
                    mSmallControlLly.setVisibility(View.GONE);
                default:
                    break;
            }
        }
    };

    IQYPlayerHandlerCallBack mCallBack = new IQYPlayerHandlerCallBack() {
        /**
         * SeekTo 成功，可以通过该回调获取当前准确时间点。
         */
        @Override
        public void OnSeekSuccess(long l) {
            LogUtils.i(TAG, "OnSeekSuccess: " + l);
        }

        /**
         * 是否因数据加载而暂停播放
         */
        @Override
        public void OnWaiting(boolean b) {
            LogUtils.i(TAG, "OnWaiting: " + b);
        }

        /**
         * 播放内核发生错误
         */
        @Override
        public void OnError(ErrorCode errorCode) {
            LogUtils.i(TAG, "OnError: " + errorCode);
            mMainHandler.removeMessages(HANDLER_MSG_UPDATE_PROGRESS);
        }

        /**
         * 播放器状态码 {@link com.iqiyi.player.nativemediaplayer.MediaPlayerState}
         * 0	空闲状态
         * 1	已经初始化
         * 2	调用PrepareMovie，但还没有进入播放
         * 4    可以获取视频信息（比如时长等）
         * 8    广告播放中
         * 16   正片播放中
         * 32	一个影片播放结束
         * 64	错误
         * 128	播放结束（没有连播）
         */
        @Override
        public void OnPlayerStateChanged(int i) {
            LogUtils.i(TAG, "OnPlayerStateChanged: " + i);
        }
    };
}
