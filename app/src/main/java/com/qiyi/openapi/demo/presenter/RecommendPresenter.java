package com.qiyi.openapi.demo.presenter;

import com.qiyi.apilib.ApiLib;
import com.qiyi.apilib.model.RecommendEntity;
import com.qiyi.apilib.net.ApiClient;
import com.qiyi.apilib.net.ApiParamsGen;
import com.qiyi.apilib.net.ApiURL;
import com.qiyi.apilib.service.ApiService;
import com.qiyi.apilib.utils.NetWorkTypeUtils;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


public class RecommendPresenter implements RecommendContract.IPresenter {
    private String TAG = this.getClass().getSimpleName();
    private RecommendContract.IView mView;
    private int pageIndex = 1; //默认从第一页开始
    private boolean mHasMore = false;
    private int DEFAULT_PAGE_SIZE = 30; //每页加载30条视频数据

    public RecommendPresenter(RecommendContract.IView view) {
        this.mView = view;
    }

    @Override
    public void resetPageIndex() {
        pageIndex = 1;
        mHasMore = false;
    }

    @Override
    public void loadRecommendDetailFromServer(boolean showMore) {
        if (!NetWorkTypeUtils.isNetAvailable(ApiLib.CONTEXT)) {
            mView.showNetWorkErrorView();
            return;
        }
        if (showMore) {
            this.mView.showLoadingView();
            pageIndex++;
        }

        ApiService apiService = ApiClient.getAPiService(ApiURL.API_REALTIME_HOST);
        apiService.qiyiRecommendDetail(ApiParamsGen.genRecommendDetailParams(pageIndex, DEFAULT_PAGE_SIZE))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<RecommendEntity>() {

                    @Override
                    public void onNext(RecommendEntity recommendEntity) {
//                        mView.dismissLoadingView();
                        if (recommendEntity != null) {
                            mView.renderRecommendDetail(recommendEntity);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        //连接重试
                        loadRecommendDetailFromServer(false);
                    }

                    @Override
                    public void onComplete() {
                        mView.dismissLoadingView();
                    }
                });
    }

    @Override
    public boolean hasMore() {
        return mHasMore;
    }
}
