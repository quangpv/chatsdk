package com.kantek.chatsdk.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.support.annotation.NonNull;

import java.io.Serializable;

@Entity(primaryKeys = {"mMyId", "mContactId"})
public class Contact implements Serializable, ItemPaging {

    @NonNull
    private String mMyId;

    @NonNull
    private String mContactId;

    private int mNumOfUnread;

    public Contact() {
    }

    @Ignore
    public Contact(String contactId, String myId) {
        mContactId = contactId;
        mMyId = myId;
        mNumOfUnread = 0;
    }

    public void setMyId(String myId) {
        mMyId = myId;
    }

    public void setContactId(String contactId) {
        mContactId = contactId;
    }

    public void setNumOfUnread(int numOfUnread) {
        mNumOfUnread = numOfUnread;
    }

    public String getContactId() {
        return mContactId;
    }

    public String getMyId() {
        return mMyId;
    }

    public int getNumOfUnread() {
        return mNumOfUnread;
    }

    public boolean isPrivate() {
        return true;
    }

    @Override
    public String getSearchPair() {
        return PairHashMap.encode(mMyId, mContactId);
    }

    @Override
    public void onChanged(Object item) {
        Contact contact = (Contact) item;
        mNumOfUnread = contact.getNumOfUnread();
    }
}
