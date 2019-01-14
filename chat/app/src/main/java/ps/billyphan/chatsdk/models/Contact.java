package ps.billyphan.chatsdk.models;

import android.arch.lifecycle.Observer;
import android.support.v4.util.Consumer;

import java.io.Serializable;

import ps.billyphan.chatsdk.datasource.ChatDataSource;
import ps.billyphan.chatsdk.filter.UnreadFilter;
import ps.billyphan.chatsdk.xmpp.XMPPClient;

public class Contact extends Observable implements Serializable {
    private transient ChatDataSource mDataSource;
    private transient Consumer<Integer> mOnUnreadChanged;

    public String name;
    private String mId;
    private String mNumOfUnread;

    public Contact(String name, XMPPClient client) {
        this.name = name;
        if (client != null) {
            mId = client.getMyId();
            mDataSource = client.getDataSource();
            mNumOfUnread = mDataSource.getUnreadSizeOfPair(mId, name) + "";
            mDataSource.addOnMessageUnreadChangedListener(mOnUnreadChanged = integer -> {
                mNumOfUnread = integer + "";
                notifyChanged();
            }, new UnreadFilter(mId, name));
        }
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
