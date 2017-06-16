package com.qiyi.openapi.demo.presenter;

import com.qiyi.apilib.model.ChannelDetailEntity;

public interface ChannelContract {
    interface IView extends IBaseContractView {
        void renderChannelDetail(ChannelDetailEntity channelDetailEntity, boolean isClear);
    }

    interface IPresenter extends IBaseContrackPresenter {
        void loadChannelDetailFromServer(String channelId, String channelName, int sortMode);
    }
}
