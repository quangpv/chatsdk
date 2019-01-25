package com.kantek.chatsdk.models;

import android.support.v4.util.Consumer;

import com.kantek.chatsdk.listeners.BiConsumer;
import com.kantek.chatsdk.utils.ChatExecutors;

import java.util.ArrayList;
import java.util.List;

public class PageList<T extends Searchable> {

    private Consumer<Integer> mOnAddListener;
    private Consumer<Integer> mOnRemoveListener;
    private Consumer<Integer> mOnItemChangedListener;
    private BiConsumer<Integer, Integer> mOnAddMoreListener;
    private List<T> mItems = new ArrayList<>();
    private PairHashMap<T> mCache = new PairHashMap<>();

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
        if (items == null || items.size() == 0) return;
        int addingSize = items.size();
        int first = size();
        int last = addingSize == 1 ? first : addingSize - 1 + first;
        mItems.addAll(items);
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
}
