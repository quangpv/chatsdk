package ps.billyphan.chatsdk.models;

import android.arch.lifecycle.Observer;
import android.support.v4.util.Consumer;

import java.io.Serializable;

import ps.billyphan.chatsdk.datasource.ChatDataSource;
import ps.billyphan.chatsdk.filter.UnreadFilter;

abstract public class Contact extends Observable implements Serializable {
    private transient ChatDataSource mDataSource;
    private transient Consumer<Integer> mOnUnreadChanged;

    public String contactId;
    private String mId;
    private String mNumOfUnread;

    public Contact(String contactId, String myId, ChatDataSource chatDataSource) {
        this.contactId = contactId;
        mId = myId;
        mDataSource = chatDataSource;
        mNumOfUnread = mDataSource.getUnreadSizeOfPair(mId, contactId) + "";
        mDataSource.addOnUnreadChangedListener(mOnUnreadChanged = integer -> {
            mNumOfUnread = integer + "";
            notifyChanged();
        }, new UnreadFilter(mId, contactId));
    }

    @Override
    public <T extends Observable> void removeObserver(Observer<T> observer) {
        super.removeObserver(observer);
        if (mDataSource != null)
            mDataSource.removeUnreadChangedListener(mOnUnreadChanged);
    }

    @Override
    public void removeObservers() {
        super.removeObservers();
        if (mDataSource != null)
            mDataSource.removeUnreadChangedListener(mOnUnreadChanged);
    }

    public String getNumOfUnread() {
        return mNumOfUnread;
    }
}
