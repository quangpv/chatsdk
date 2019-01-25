package com.kantek.chatsdk.filter.entry;

public interface ChatFilter<T> {
    boolean accept(T t);
}
