package com.kantek.chatsdk.xmpp;

import android.support.v4.util.Consumer;

import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.kantek.chatsdk.utils.ChatExecutors;
import com.kantek.chatsdk.datasource.ChatDataSource;
import com.kantek.chatsdk.models.Contact;
import com.kantek.chatsdk.models.FriendContact;
import com.kantek.chatsdk.models.GroupContact;
import com.kantek.chatsdk.utils.PackageAnalyze;

public class ContactClient {
    private final Roster mRoster;
    private final String mId;
    private final ChatDataSource mChatDataSource;

    public ContactClient(String userName, XMPPChatConnection connection, ChatDataSource chatDataSource) {
        mId = userName;
        mRoster = connection.getRoster();
        mChatDataSource = chatDataSource;
    }

    public void loadFriends(Consumer<List<Contact>> consumer) {
        ChatExecutors.loadInBackground(() -> {
            Set<RosterEntry> rosterEntries = mRoster.getEntries();
            List<Contact> contacts = new ArrayList<>();
            for (RosterEntry rosterEntry : rosterEntries) {
                contacts.add(new FriendContact(PackageAnalyze.getId(rosterEntry.getJid()), mId, mChatDataSource));
            }
            return contacts;
        }).onMainThread(consumer);
    }

    public void loadGroup(Consumer<List<Contact>> consumer) {
        ChatExecutors.loadInBackground(() -> {
            Collection<RosterGroup> rosterEntries = mRoster.getGroups();
            List<Contact> contacts = new ArrayList<>();
            for (RosterGroup roster : rosterEntries) {
                contacts.add(new FriendContact(roster.getName(), mId, mChatDataSource));
            }
            return contacts;
        }).onMainThread(consumer);
    }

    public Contact newFriend(String text) {
        return new FriendContact(text, mId, mChatDataSource);
    }

    public Contact newGroup(String text) {
        return new GroupContact(text, mId, mChatDataSource);
    }
}
