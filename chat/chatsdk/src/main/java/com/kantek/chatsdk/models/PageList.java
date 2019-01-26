package com.kantek.chatsdk.models;

import android.support.v4.util.Consumer;

import com.kantek.chatsdk.listeners.BiConsumer;
import com.kantek.chatsdk.utils.ChatExecutors;

import java.util.ArrayList;
import java.util.List;

public class PageList<T extends ItemPaging> {

    private int mPageSize;
    private int mInitSize;
    private Consumer<Integer> mOnAddListener;
    private Consumer<Integer> mOnRemoveListener;
    private Consumer<Integer> mOnItemChangedListener;
    private BiConsumer<Integer, Integer> mOnAddMoreListener;
    private List<T> mItems = new ArrayList<>();
    private PairHashMap<T> mCache = new PairHashMap<>();
    private OnRequestLoadMoreListener<T> mOnLoadMoreListener;
    private boolean mLoading = false;
    private boolean mOutOfArchived = false;

    public PageList(int initSize, int pageSize) {
        mInitSize = initSize;
        mPageSize = pageSize;
    }

    public T get(int position) {
        return mItems.get(position);
    }

    private int indexOf(T item) {
        return mItems.indexOf(mCache.get(item.getSearchPair()));
    }

    public int size() {
        return mItems.size();
    }

    public void remove(T item) {
        int pos = indexOf(item);
        mItems.remove(pos);
        mCache.remove(item.getSearchPair());
        if (mOnRemoveListener != null) {
            ChatExecutors.onMainThread(() -> mOnRemoveListener.accept(pos));
        }
    }

    public void add(T item) {
        int pos = size();
        mItems.add(item);
        mCache.put(item.getSearchPair(), item);
        if (mOnAddListener != null) {
            ChatExecutors.onMainThread(() -> mOnAddListener.accept(pos));
        }
    }

    public void update(T item) {
        int pos = indexOf(item);
        mItems.get(pos).onChanged(item);
        if (mOnItemChangedListener != null) {
            ChatExecutors.onMainThread(() -> mOnItemChangedListener.accept(pos));
        }
    }

    public void addAll(List<T> items) {
        addAll(items, false);
    }

    public void addAll(List<T> items, boolean atTop) {
        if (items == null || items.size() == 0) return;
        int addingSize = items.size();
        int first, last;
        if (atTop) {
            first = 0;
            last = addingSize == 1 ? first : addingSize - 1 + first;
            mItems.addAll(first, items);
        } else {
            first = size();
            last = addingSize == 1 ? first : addingSize - 1 + first;
            mItems.addAll(items);
        }
        for (T item : items) {
            mCache.put(item.getSearchPair(), item);
        }
        ChatExecutors.onMainThread(() -> {
            if (last == first) {
                if (mOnAddListener != null) mOnAddListener.accept(first);
            } else {
                if (mOnAddMoreListener != null) mOnAddMoreListener.accept(first, last);
            }
        });
    }

    public void addAllAtTop(List<T> items) {
        addAll(items, true);
    }

    public void requestLoadMore() {
        if (mLoading || mOutOfArchived) return;
        if (mOnLoadMoreListener != null) {
            synchronized (this) {
                mLoading = true;
            }
            ChatExecutors.inBackground(() -> {
                int page = getIndex() + 1;
                int pageSize = page == 1 ? mInitSize : mPageSize;
                List<T> values = mOnLoadMoreListener.onLoad(page, pageSize);
                if (!values.isEmpty()) addAllAtTop(values);
                synchronized (PageList.this) {
                    mOutOfArchived = values.size() == 0 || values.size() < pageSize;
                    mLoading = false;
                }
            });
        }
    }

    public boolean isLoading() {
        return mLoading;
    }

    public void setOnAddedListener(Consumer<Integer> listener) {
        mOnAddListener = listener;
    }

    public void setOnRemovedListener(Consumer<Integer> listener) {
        mOnRemoveListener = listener;
    }

    public void setOnItemChangedListener(Consumer<Integer> listener) {
        mOnItemChangedListener = listener;
    }

    public void setOnAddMoreListener(BiConsumer<Integer, Integer> listener) {
        mOnAddMoreListener = listener;
    }

    public void setOnLoadMoreListener(OnRequestLoadMoreListener<T> listener) {
        mOnLoadMoreListener = listener;
    }

    public int getIndex() {
        if (mInitSize == 0 || mPageSize == 0) return 0;
        int size = size();
        if (size == 0) return 0;
        int totalPageSize = size - mInitSize;
        if (totalPageSize < 0) return 1;
        return 1 + totalPageSize / mPageSize + (totalPageSize % mPageSize > 0 ? 1 : 0);
    }

    public int getInitSize() {
        return mInitSize;
    }

    public int getPageSize() {
        return mPageSize;
    }

    public void clear() {
        mItems.clear();
        mCache.clear();
        mOutOfArchived = false;
    }

    public interface OnRequestLoadMoreListener<T> {
        List<T> onLoad(int page, int size);
    }
}
