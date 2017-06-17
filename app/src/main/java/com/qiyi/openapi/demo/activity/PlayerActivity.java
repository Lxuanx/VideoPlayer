package com.qiyi.openapi.demo.activity;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.qiyi.apilib.ApiLib;
import com.qiyi.apilib.KeyConstant;
import com.qiyi.apilib.model.VideoInfo;
import com.qiyi.apilib.utils.LogUtils;
import com.qiyi.apilib.utils.StringUtils;
import com.qiyi.apilib.utils.UiUtils;
import com.qiyi.openapi.demo.R;
import com.qiyi.video.playcore.ErrorCode;
import com.qiyi.video.playcore.IQYPlayerHandlerCallBack;
import com.qiyi.video.playcore.QiyiVideoView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class PlayerActivity extends BaseActivity {
    private static final int PERMISSION_REQUEST_CODE = 7171;
    private static final String TAG = PlayerActivity.class.getSimpleName();

    private static final int HANDLER_MSG_UPDATE_PROGRESS = 1;
    private static final int HANDLER_MSG_CLOSE_SMALL_CONTROL = 2;
    private static final int HANDLER_DEPLAY_UPDATE_PROGRESS = 1000; // 1s
    private static final int HANDLER_DEPLAY_CLOSE_SMALL_CONTROL = 3000; // 3s

    private boolean isFullScreen;

    private int[] screen;

    private QiyiVideoView mVideoView;
    private SeekBar mSeekBar;
    private ImageButton mPlayPauseIb;
    private TextView mCurrentTimeTv;
    private TextView mTotalTimeTv;
    private LinearLayout mBottomControlLly;
    private String mTid;
    private String mAid;
    private VideoInfo mVideoInfo;
    private LinearLayout mTopControlLly;
    private boolean mIsPlayFinish;
    private static int sCurrentPosition;
    private TextView mCurrentSystemTimeTv;
    private SimpleDateFormat mSdf;
    private View mPlayLoadingLly;


    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_player;
    }

    @Override
    protected void initView() {
        super.initView();
        screen = UiUtils.getScreenWidthAndHeight(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mAid = getIntent().getStringExtra(KeyConstant.AID);
        mTid = getIntent().getStringExtra(KeyConstant.TID);
        mVideoInfo = (VideoInfo) getIntent().getSerializableExtra(KeyConstant.VIDEOINFO);
        if (StringUtils.isEmpty(mTid)) {
            finish();
            return;
        }

        mVideoView = (QiyiVideoView) findViewById(R.id.player_vv);
        mVideoView.setPlayData(mTid);
        //设置回调，监听播放器状态
        setPlayerCallback();
        mVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayControlLly();
            }
        });

        mPlayLoadingLly = findViewById(R.id.play_loading_lly);

        isFullScreen = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        if (isFullScreen) {
            initTopControlView();
        } else {
            initNonPlayerView();
        }
        initBottomControlView();

    }

    private void initNonPlayerView() {
        ((TextView) findViewById(R.id.video_info_title_tv)).setText(mVideoInfo.title);
        ((TextView) findViewById(R.id.video_info_play_count)).setText(ApiLib.CONTEXT.getString(R.string.play_count, mVideoInfo.playCountText));
        if (!mVideoInfo.dateFormat.equals("1970-01-01")) {
            ((TextView) findViewById(R.id.video_info_date_format)).setText(ApiLib.CONTEXT.getString(R.string.upload_time, mVideoInfo.dateFormat));
        }

        LinearLayout episodeLly = (LinearLayout) findViewById(R.id.episode_lly);
        RelativeLayout episodeRly = (RelativeLayout) findViewById(R.id.episode_rly);
        // 1: 单视频专辑, 2: 电视剧, 3: 综艺
        if ("2".equals(mVideoInfo.pType)) {
            episodeRly.setVisibility(View.VISIBLE);
            if (mVideoInfo.updateNum.equals(mVideoInfo.totalNum)) {
                ((TextView) findViewById(R.id.episode_more_tv)).setText(ApiLib.CONTEXT.getString(R.string.episode_more_finished, mVideoInfo.totalNum));
            } else {
                ((TextView) findViewById(R.id.episode_more_tv)).setText(ApiLib.CONTEXT.getString(R.string.episode_more, mVideoInfo.updateNum, mVideoInfo.totalNum));
            }
            int currentUpdateNum = Integer.valueOf(mVideoInfo.updateNum);
            for (int i = 0; i < currentUpdateNum; i++) {
                Button button = new Button(this);
                button.setText(i + 1 + "");
                button.setBackgroundResource(R.drawable.episodu_button);
                episodeLly.addView(button);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) button.getLayoutParams();
                lp.width = getResources().getDimensionPixelSize(R.dimen.card_episode_button);
                lp.height = getResources().getDimensionPixelSize(R.dimen.card_episode_button);
                lp.setMargins(getResources().getDimensionPixelSize(R.dimen.card_episode), getResources().getDimensionPixelSize(R.dimen.card_episode), getResources().getDimensionPixelSize(R.dimen.card_episode), getResources().getDimensionPixelSize(R.dimen.card_episode));
                button.requestLayout();
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(ApiLib.CONTEXT, "爱奇艺程序猿哥哥给的API接口不足，未实现ing", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            findViewById(R.id.separate_line).setVisibility(View.GONE);
        }
    }

    private void initTopControlView() {
        mTopControlLly = (LinearLayout) findViewById(R.id.top_control_lly);

        ImageButton playerBackIb = (ImageButton) findViewById(R.id.player_back_ib);
        playerBackIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openOrCloseFullScreen();
            }
        });

        ImageButton playerSetIb = (ImageButton) findViewById(R.id.player_set_ib);
        playerSetIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ApiLib.CONTEXT, "setting", Toast.LENGTH_SHORT).show();
            }
        });

        ((TextView) findViewById(R.id.video_title_tv)).setText(mVideoInfo.title);
        mSdf = new SimpleDateFormat("HH:mm", Locale.CHINA);
        mCurrentSystemTimeTv = (TextView) findViewById(R.id.system_time_tv);
        mCurrentSystemTimeTv.setText(mSdf.format(new Date(System.currentTimeMillis())));
    }

    private void initBottomControlView() {
        mBottomControlLly = (LinearLayout) findViewById(R.id.bottom_control_lly);

        mCurrentTimeTv = (TextView) findViewById(R.id.current_time_tv);
        mTotalTimeTv = (TextView) findViewById(R.id.total_time_tv);

        mPlayPauseIb = (ImageButton) findViewById(R.id.player_pause_ib);
        mPlayPauseIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickPlay();
            }
        });
        Glide.with(ApiLib.CONTEXT).load(R.drawable.player_pause).into(mPlayPauseIb);

        ImageButton playerFullIb = (ImageButton) findViewById(R.id.play_full_ib);
        playerFullIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openOrCloseFullScreen();
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

    private void clickPlay() {
        if (mVideoView.isPlaying()) {
            mVideoView.pause();
            Glide.with(ApiLib.CONTEXT).load(R.drawable.player_play).into(mPlayPauseIb);
            mMainHandler.removeMessages(HANDLER_MSG_UPDATE_PROGRESS);
        } else if (!mIsPlayFinish) {
            mVideoView.start();
            Glide.with(ApiLib.CONTEXT).load(R.drawable.player_pause).into(mPlayPauseIb);
            mMainHandler.sendEmptyMessageDelayed(HANDLER_MSG_UPDATE_PROGRESS, HANDLER_DEPLAY_UPDATE_PROGRESS);
        } else {
            mIsPlayFinish = false;
            mVideoView.seekTo(0);
            Glide.with(ApiLib.CONTEXT).load(R.drawable.player_pause).into(mPlayPauseIb);
        }
    }

    private void displayControlLly() {
        if (mBottomControlLly.getVisibility() != View.VISIBLE) {
            if (isFullScreen) {
                mTopControlLly.setVisibility(View.VISIBLE);
            }
            mBottomControlLly.setVisibility(View.VISIBLE);
            mMainHandler.sendEmptyMessageDelayed(HANDLER_MSG_CLOSE_SMALL_CONTROL, HANDLER_DEPLAY_CLOSE_SMALL_CONTROL);
        } else {
            if (isFullScreen) {
                mTopControlLly.setVisibility(View.GONE);
            }
            mBottomControlLly.setVisibility(View.GONE);
            mMainHandler.removeMessages(HANDLER_MSG_CLOSE_SMALL_CONTROL);
        }
    }

    private void openOrCloseFullScreen() {
        sCurrentPosition = mVideoView.getCurrentPosition();
        if (isFullScreen) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
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

                        mTotalTimeTv.setText(ms2hms(duration));
                        mCurrentTimeTv.setText(ms2hms(progress));
                    }
                    if(isFullScreen) {
                        mCurrentSystemTimeTv.setText(mSdf.format(new Date(System.currentTimeMillis())));
                    }
                    mMainHandler.sendEmptyMessageDelayed(HANDLER_MSG_UPDATE_PROGRESS, HANDLER_DEPLAY_UPDATE_PROGRESS);
                    break;
                case HANDLER_MSG_CLOSE_SMALL_CONTROL:
                    if (isFullScreen) {
                        mTopControlLly.setVisibility(View.GONE);
                    }
                    mBottomControlLly.setVisibility(View.GONE);
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
            if (i == 1) {
                mPlayLoadingLly.setVisibility(View.VISIBLE);
            } else if (i == 16) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //点击全屏按钮后三秒后也自动隐藏布局
                        mMainHandler.sendEmptyMessageDelayed(HANDLER_MSG_CLOSE_SMALL_CONTROL, HANDLER_DEPLAY_CLOSE_SMALL_CONTROL);
                        mPlayLoadingLly.setVisibility(View.GONE);
                        if (isFullScreen) {
                            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
                        }
                        mVideoView.seekTo(sCurrentPosition);
                    }
                });
            } else if (i == 128) {
                mIsPlayFinish = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.clear(mPlayPauseIb);
                        Glide.with(ApiLib.CONTEXT).load(R.drawable.player_play).into(mPlayPauseIb);
                    }
                });
            }
            LogUtils.i(TAG, "   OnPlayerStateChanged: " + i);
        }
    };
}
