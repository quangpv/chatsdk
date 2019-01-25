package com.kantek.chatsdk.models;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class PageAdapter<T extends Searchable> extends RecyclerView.Adapter<PageAdapter.PageHolder> {
    private PageList<T> mPageList;

    public void submitList(PageList<T> items) {
        if (mPageList == items) return;
        mPageList = items;
        onSubmit(items);
        notifyDataSetChanged();
    }

    private void onSubmit(PageList<T> items) {
        items.setOnAddedListener(this::notifyItemInserted);
        items.setOnAddMoreListener(this::notifyItemRangeInserted);
        items.setOnRemovedListener(this::notifyItemRemoved);
        items.setOnItemChangedListener(this::notifyItemChanged);
    }

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
