package com.kantek.chatsdk.models;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class PageAdapter<T extends ItemPaging> extends RecyclerView.Adapter<PageAdapter.PageHolder> {
    private PageList<T> mPageList;
//    private OnLoadMoreListener mOnLoadMoreListener;

    public void submitList(PageList<T> items) {
        if (mPageList == items) return;
        mPageList = items;
        onSubmit(items);
        notifyDataSetChanged();
    }

    private void onSubmit(PageList<T> items) {
        items.setOnAddedListener(integer -> notifyItemInserted(getItemCount()));
        items.setOnAddMoreListener((from, to) -> notifyDataSetChanged());
        items.setOnRemovedListener(integer -> notifyDataSetChanged());
        items.setOnItemChangedListener(this::notifyItemChanged);
    }

//    @Override
//    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
//        super.onAttachedToRecyclerView(recyclerView);
//        recyclerView.addOnScrollListener(mOnLoadMoreListener = new OnLoadMoreListener(5, true) {
//            @Override
//            protected void onLoadMore() {
//                if (mPageList != null) mPageList.requestLoadMore();
//            }
//        });
//    }
//
//    @Override
//    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
//        super.onDetachedFromRecyclerView(recyclerView);
//        recyclerView.removeOnScrollListener(mOnLoadMoreListener);
//    }

    @Override
    public void onBindViewHolder(@NonNull PageHolder pageHolder, int i) {
        pageHolder.bind(getItem(i));
    }

    public T getItem(int position) {
        return mPageList.get(position);
    }

    @Override
    public int getItemCount() {
        return mPageList != null ? mPageList.size() : 0;
    }

    @Override
    public void onViewRecycled(@NonNull PageHolder holder) {
        super.onViewRecycled(holder);
        holder.onRecycled();
    }

    public static class PageHolder<T> extends RecyclerView.ViewHolder {

        protected T item;

        public PageHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void bind(T item) {
            this.item = item;
        }

        public void onRecycled() {

        }
    }
}
