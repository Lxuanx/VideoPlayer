package com.qiyi.openapi.demo.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.qiyi.apilib.ApiLib;
import com.qiyi.apilib.model.BaseEntity;
import com.qiyi.apilib.model.ChannelDetailEntity;
import com.qiyi.apilib.model.VideoInfo;
import com.qiyi.apilib.utils.ImageUtils;
import com.qiyi.apilib.utils.StringUtils;
import com.qiyi.openapi.demo.R;
import com.qiyi.openapi.demo.utils.QYPlayerUtils;

import java.util.ArrayList;
import java.util.List;


public class ChannelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static int ROW_NUM = 3;
    private int TYPE_VIDEO = 0x003;

    Activity context;

    private List<BaseEntity> entityList = new ArrayList<>();

    public ChannelAdapter(Activity context) {
        this.context = context;
    }


    public void setData(ChannelDetailEntity channelDetailEntity, boolean isClear) {
        if (isClear) {
            entityList.clear();
        }
        reAssembleData(channelDetailEntity);
        this.notifyDataSetChanged();
    }

    /**
     * 重新组装数据，以适配RecyclerView
     */
    private void reAssembleData(ChannelDetailEntity channelDetailEntity) {
        if (channelDetailEntity == null) {
            return;
        }

        List<VideoInfo> videoInfoList = channelDetailEntity.dataEntity.videoInfoList;

        if (videoInfoList == null || videoInfoList.size() < 1) {
            return;
        }

        for (VideoInfo videoInfo : videoInfoList) {
            entityList.add(videoInfo);
        }
    }


    abstract class BaseViewHolder extends RecyclerView.ViewHolder {
        public BaseViewHolder(View view) {
            super(view);
        }

        abstract void setData(int position);
    }


    /**
     * 视频的ViewHolder
     */
    class VideoInfoViewHolder extends BaseViewHolder implements View.OnClickListener {
        ImageView cover;
        TextView name;
        TextView playCount;
        TextView snsScore;

        public VideoInfoViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.item_image_name);
            playCount = (TextView) itemView.findViewById(R.id.item_image_play_count);
            snsScore = (TextView) itemView.findViewById(R.id.item_image_score);
            cover = (ImageView) itemView.findViewById(R.id.item_image_img);
            itemView.setOnClickListener(this);
            resizeImageView(cover);
        }

        /**
         * 视频图片为竖图展示取分辨率为：_120_160
         *
         * @param cover
         */
        private void resizeImageView(ImageView cover) {
            int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            int coverWidth = (screenWidth - context.getResources().getDimensionPixelSize(R.dimen.video_card_margin_margin_horizontal) * 2 * 3) / 3;
            int coverHeight = (int) (160.0f / 120.0f * coverWidth);
            cover.setMinimumHeight(coverHeight);
            cover.setMaxHeight(coverHeight);
            cover.setMaxHeight(coverWidth);
            cover.setMinimumWidth(coverWidth);
            cover.setAdjustViewBounds(false);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) cover.getLayoutParams();
            lp.height = coverHeight;
            lp.width = coverWidth;
            cover.setLayoutParams(lp);
        }

        public void setData(int position) {
            VideoInfo video = (VideoInfo) entityList.get(position);
            if (!StringUtils.isEmpty(video.shortTitle)) {
                name.setText(video.shortTitle);
            } else {
                name.setText("");
            }

            if (!StringUtils.isEmpty(video.playCountText)) {
                playCount.setText(context.getString(R.string.play_count, video.playCountText));
            } else {
                playCount.setText("");
            }

            Glide.clear(cover); //清除缓存
            Glide.with(ApiLib.CONTEXT).load(ImageUtils.getRegImage(video.img, ImageUtils.IMG_260_360)).animate(R.anim.alpha_on).into(cover);

        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            BaseEntity dataObj = entityList.get(position);
            if (dataObj instanceof VideoInfo) {
                VideoInfo videoInfo = (VideoInfo) dataObj;
                QYPlayerUtils.jumpToPlayerActivity(context, videoInfo.aId, videoInfo.tId, videoInfo);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_VIDEO;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_video_info_item, parent, false);
        return new VideoInfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((BaseViewHolder) holder).setData(position);
    }

    @Override
    public int getItemCount() {
        return entityList.size();
    }
}
