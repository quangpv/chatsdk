package com.kantek.chatsdk.models;

public class ChatParameters {
    private String mWithId;
    private int mPageSize;
    private int mInitializePageSize;

    public String getWithId() {
        return mWithId;
    }

    public ChatParameters setWithId(String id) {
        mWithId=id;
        return this;
    }

    public ChatParameters setInitializePageSize(int i) {
        mInitializePageSize = i;
        return this;
    }

    public ChatParameters setPageSize(int i) {
        mPageSize = i;
        return this;
    }

    public int getPageSize() {
        return mPageSize;
    }

    public int getInitializePageSize() {
        return mInitializePageSize;
    }
}
