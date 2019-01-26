package com.kantek.chatsdk.xmpp;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.v4.util.Consumer;

import com.kantek.chatsdk.datasource.ChatDataSource;
import com.kantek.chatsdk.filter.entry.ContactFilter;
import com.kantek.chatsdk.models.Contact;
import com.kantek.chatsdk.models.PageList;
import com.kantek.chatsdk.utils.ChatExecutors;
import com.kantek.chatsdk.utils.PackageAnalyze;

import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ContactClient implements LifecycleObserver {
    private final Roster mRoster;
    private final String mId;
    private final ChatDataSource mChatDataSource;
    private MutableLiveData<PageList<Contact>> mLiveData = new MutableLiveData<>();
    private PageList<Contact> mPageList;
    private Lifecycle mLifecycle;
    private Consumer<Contact> mPrivateContactFilter;

    public ContactClient(String userName, XMPPChatConnection connection, ChatDataSource chatDataSource) {
        mId = userName;
        mRoster = connection.getRoster();
        mChatDataSource = chatDataSource;
        mPageList = new PageList<>(0, 0);
    }

    public void setLifecycle(Lifecycle lifecycle) {
        mLifecycle = lifecycle;
        mLifecycle.addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private void onCreate() {
        mChatDataSource.addOnUnReadChangedListener(mPrivateContactFilter = contact ->
                mPageList.update(contact), new ContactFilter(true));
        ChatExecutors.inBackground(() -> {
            Set<RosterEntry> rosterEntries = mRoster.getEntries();
            List<Contact> contacts = new ArrayList<>();
            for (RosterEntry rosterEntry : rosterEntries) {
                contacts.add(new Contact(PackageAnalyze.getId(rosterEntry.getJid()), mId));
            }
            mPageList.addAll(mChatDataSource.addContacts(contacts));
            mLiveData.postValue(mPageList);
        });
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private void onDestroy() {
        mChatDataSource.removeOnUnReadChangedListener(mPrivateContactFilter);
        mLifecycle.removeObserver(this);
        mLifecycle = null;
    }

    public Contact newFriend(String text) {
        return new Contact(text, mId);
    }

    public LiveData<PageList<Contact>> asLiveData() {
        return mLiveData;
    }
}
