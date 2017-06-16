package com.qiyi.openapi.demo.fragment;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.qiyi.apilib.model.ChannelDetailEntity;
import com.qiyi.openapi.demo.R;
import com.qiyi.openapi.demo.adapter.ChannelAdapter;
import com.qiyi.openapi.demo.presenter.ChannelContract;
import com.qiyi.openapi.demo.presenter.ChannelPresenter;

import java.util.ArrayList;

public class ChannelFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, ChannelContract.IView, View.OnClickListener {

    private static final int HOT_PLAY_SORT_MODE = 11;
    private static final int GOOD_SCORE_SORT_MODE = 8;
    private static final int NEW_UPLOAD_SORT_MODE = 4;

    private String channelName;
    private String channelId;
    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private ChannelPresenter mPresenter;
    private ChannelAdapter mAdapter;
    private GridLayoutManager mLayoutManager;
    private int mLastVisiblePosition;
    private TextView mGuideHotPlayTv;
    private TextView mGuidegoodScoreTv;
    private TextView mGuideNewUploadTv;
    private ArrayList<TextView> mGuideTextViews;
    private int mSortMode = HOT_PLAY_SORT_MODE;

    public static ChannelFragment newInstance(String channelId, String channelName) {
        return new ChannelFragment(channelId, channelName);
    }

    private ChannelFragment(String channelId, String channelName) {
        this.channelId = channelId;
        this.channelName = channelName;
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.fragment_channel;
    }

    @Override
    protected void initView() {
        super.initView();
        mPresenter = new ChannelPresenter(this);
        Toolbar toolbar = (Toolbar) mRootView.findViewById(R.id.fragment_toolbar);
        toolbar.setTitle(channelName);
        setActionBar(toolbar);

        initGuideView();

        mRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipe_refresh_layout);
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
        mLayoutManager = new GridLayoutManager(mActivity, ChannelAdapter.ROW_NUM);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mAdapter = new ChannelAdapter(mActivity);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE
                        && mLastVisiblePosition + 1 == mAdapter.getItemCount()) {
                    showLoadingView();
                    mPresenter.loadChannelDetailFromServer(channelId, channelName, mSortMode);
                }

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                mLastVisiblePosition = mLayoutManager.findLastVisibleItemPosition();
            }
        });
    }

    private void initGuideView() {
        mGuideTextViews = new ArrayList<>();
        mGuideHotPlayTv = (TextView) mRootView.findViewById(R.id.guide_hot_tv);
        mGuideHotPlayTv.setOnClickListener(this);
        mGuideTextViews.add(mGuideHotPlayTv);
        mGuidegoodScoreTv = (TextView) mRootView.findViewById(R.id.guide_good_tv);
        mGuidegoodScoreTv.setOnClickListener(this);
        mGuideTextViews.add(mGuidegoodScoreTv);
        mGuideNewUploadTv = (TextView) mRootView.findViewById(R.id.guide_new_tv);
        mGuideNewUploadTv.setOnClickListener(this);
        mGuideTextViews.add(mGuideNewUploadTv);
    }

    @Override
    public void onClick(View v) {
        int sortMode = 0;
        switch (v.getId()) {
            case R.id.guide_hot_tv:
                sortMode = HOT_PLAY_SORT_MODE;
                break;
            case R.id.guide_good_tv:
                sortMode = GOOD_SCORE_SORT_MODE;
                break;
            case R.id.guide_new_tv:
                sortMode = NEW_UPLOAD_SORT_MODE;
                break;
        }
        //排序模式和上次一样不重新刷新数据
        if (mSortMode == sortMode) {
            return;
        }
        mSortMode = sortMode;
        for (TextView tv : mGuideTextViews) {
            if (tv == v) {
                tv.setBackgroundResource(R.drawable.guide_selected);
                tv.setTextColor(getResources().getColor(R.color.guide_selected));
                showLoadingView();
                mPresenter.loadChannelDetailFromServer(channelId, channelName, mSortMode);
            } else {
                tv.setBackgroundResource(R.color.trans);
                tv.setTextColor(getResources().getColor(android.R.color.black));
            }
        }

    }

    @Override
    protected void loadData() {
        mPresenter.loadChannelDetailFromServer(channelId, channelName, mSortMode);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (mAdapter.getItemCount() == 0) {
                loadData();
            }
        }
    }

    @Override
    public void showLoadingView() {
        showLoadingBar();
    }

    @Override
    public void showNotMoreView() {
        mRefreshLayout.setRefreshing(false);
        Toast.makeText(mActivity, R.string.not_more, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void dismissLoadingView() {
        hideLoadingBar();
        mRefreshLayout.setRefreshing(false);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void showNetWorkErrorView() {
        mRefreshLayout.setRefreshing(false);
        Toast.makeText(mActivity, R.string.network_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRefresh() {
        mPresenter.loadChannelDetailFromServer(channelId, channelName, mSortMode);
    }

    @Override
    public void renderChannelDetail(ChannelDetailEntity channelDetailEntity, boolean isClear) {
        if (channelDetailEntity == null) {
            Toast.makeText(mActivity, R.string.not_more, Toast.LENGTH_SHORT).show();
            return;
        }
        mRefreshLayout.setRefreshing(false);
        mAdapter.setData(channelDetailEntity, isClear);
    }

}
