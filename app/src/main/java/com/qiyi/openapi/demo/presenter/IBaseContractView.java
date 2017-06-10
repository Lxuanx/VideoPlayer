package com.qiyi.openapi.demo.presenter;


public interface IBaseContractView {
    void showLoadingView();
    void dismissLoadingView();
    void showEmptyView();
    void showNetWorkErrorView();
}
