package com.qiyi.openapi.demo.presenter;


public interface IBaseContractView {
    void showLoadingView();
    void showNotMoreView();
    void dismissLoadingView();
    void showNetWorkErrorView();
}
