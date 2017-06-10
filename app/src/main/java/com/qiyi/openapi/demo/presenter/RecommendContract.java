package com.qiyi.openapi.demo.presenter;

import com.qiyi.apilib.model.RecommendEntity;


public interface RecommendContract {
    interface IView extends IBaseContractView {
        void renderRecommendDetail(RecommendEntity recommendEntitiy);
    }

    interface IPresenter extends IBaseContrackPresenter {
        void resetPageIndex();
        void loadRecommendDetailFromServer(boolean showLoadingView);
        boolean hasMore();
    }
}
