package com.kantek.chatsdk.models;

public interface Searchable {
    String getSearchPair();

    void onChanged(Object item);
}
