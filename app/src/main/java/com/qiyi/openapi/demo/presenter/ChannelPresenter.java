package com.qiyi.openapi.demo.presenter;

import com.qiyi.apilib.ApiLib;
import com.qiyi.apilib.model.ChannelDetailEntity;
import com.qiyi.apilib.net.ApiClient;
import com.qiyi.apilib.net.ApiParamsGen;
import com.qiyi.apilib.net.ApiURL;
import com.qiyi.apilib.service.ApiService;
import com.qiyi.apilib.utils.NetWorkTypeUtils;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Administrator on 2017/6/15.
 */

public class ChannelPresenter implements ChannelContract.IPresenter {

    private final ChannelContract.IView mView;
    private int pageIndex = 1; //默认从第一页开始
    private int DEFAULT_PAGE_SIZE = 15; //每页加载30条视频数据
    private int mLastSortMode = -1;

    public ChannelPresenter(ChannelContract.IView view) {
        this.mView = view;
    }

    @Override
    public void loadChannelDetailFromServer(final String channelId, final String channelName, final int sortMode) {
        boolean isClearOldData = false;

        if (!NetWorkTypeUtils.isNetAvailable(ApiLib.CONTEXT)) {
            mView.showNetWorkErrorView();
            return;
        }

        //假如排序方式发生改变，则重新从第一页开始
        if(sortMode != mLastSortMode){
            mLastSortMode = sortMode;
            isClearOldData = true;
            pageIndex = 1;
        }

        ApiService apiService = ApiClient.getAPiService(ApiURL.API_REALTIME_HOST);
        final boolean finalIsClearOldData = isClearOldData;
        apiService.qiyiChannelDetail(ApiParamsGen.genChannelDetailParams(channelId, channelName, sortMode, pageIndex, DEFAULT_PAGE_SIZE))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<ChannelDetailEntity>() {

                    @Override
                    public void onNext(ChannelDetailEntity channelDetailEntity) {
                        if (channelDetailEntity != null) {
                            mView.renderChannelDetail(channelDetailEntity, finalIsClearOldData);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        loadChannelDetailFromServer(channelId, channelName, sortMode);
                    }

                    @Override
                    public void onComplete() {
                        mView.dismissLoadingView();
                    }
                });
        pageIndex++;
    }

}
