package com.kantek.chatsdk.models;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public abstract class OnLoadMoreListener extends RecyclerView.OnScrollListener {

    private final int mThreshold;
    private final boolean mRevert;

    public OnLoadMoreListener(int threshold, boolean isRevert) {
        mThreshold = threshold;
        mRevert = isRevert;
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int total = layoutManager.getItemCount();
        if (!mRevert) {
            if (layoutManager.findLastVisibleItemPosition() + mThreshold >= total) onLoadMore();
        } else if (layoutManager.findFirstVisibleItemPosition() - mThreshold <= 0) onLoadMore();
    }

    protected abstract void onLoadMore();
}
