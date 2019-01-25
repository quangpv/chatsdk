package com.kantek.chatsdk.datasource;

import android.content.Context;
import android.support.v4.util.Consumer;
import android.util.Log;

import com.kantek.chatsdk.filter.entry.StateEntryFilter;
import com.kantek.chatsdk.filter.entry.ChatFilter;
import com.kantek.chatsdk.models.AtomicUnRead;
import com.kantek.chatsdk.models.Contact;
import com.kantek.chatsdk.models.MessageEntry;
import com.kantek.chatsdk.models.PairHashMap;
import com.kantek.chatsdk.models.ReceiptState;
import com.kantek.chatsdk.models.StateEntry;
import com.kantek.chatsdk.utils.PackageAnalyze;
import com.kantek.chatsdk.xmpp.XMPPChatConnection;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.offline.OfflineMessageHeader;
import org.jivesoftware.smackx.offline.OfflineMessageManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatDataSource {
    private final PairHashMap<StateEntry> mState;
    private OfflineMessageManager mOfflineMessageManager;
    private XMPPChatConnection mConnection;
    private Map<Consumer<MessageEntry>, ChatFilter<MessageEntry>> mOnComingListeners = new HashMap<>();
    private Map<Consumer<MessageEntry>, ChatFilter<MessageEntry>> mOnOutGoingListeners = new HashMap<>();
    private Map<Consumer<MessageEntry>, ChatFilter<MessageEntry>> mOnReceiptListeners = new HashMap<>();
    private Map<Consumer<Contact>, ChatFilter<Contact>> mOnUnReadChangedListener = new HashMap<>();
    private Map<Consumer<StateEntry>, ChatFilter<StateEntry>> mOnStateListeners = new HashMap<>();
    private ChatDatabase.MessageDao mMessageStorage;
    private ChatDatabase.ContactDao mContactStorage;

    public XMPPTCPConnection getConnection() {
        return mConnection;
    }

    public ChatDataSource(Context context) {
        mState = new PairHashMap<>();
        mMessageStorage = ChatDatabase.getInstance(context).messageDao();
        mContactStorage = ChatDatabase.getInstance(context).contactDao();
    }

    public void fetchOfflineMessages() {
        if (mOfflineMessageManager == null)
            throw new RuntimeException("Not set offline message manager");
        try {
            if (mOfflineMessageManager.getMessageCount() <= 0) return;
            List<String> nodes = new ArrayList<>();
            for (OfflineMessageHeader offlineMessageHeader : mOfflineMessageManager.getHeaders()) {
                nodes.add(offlineMessageHeader.getStamp());
            }
            List<MessageEntry> messageEntries = new ArrayList<>();
            AtomicUnRead unread = new AtomicUnRead();
            for (Message message : mOfflineMessageManager.getMessages(nodes)) {
                MessageEntry messageEntry = new MessageEntry(message);
                messageEntries.add(messageEntry);
                unread.putOrIncrease(messageEntry.getFromId(), messageEntry.getToId(), 1);
            }
            mMessageStorage.addAll(messageEntries);
            unread.forEach((pair, number) -> mContactStorage.addUnRead(pair.first, pair.second, number));
            mOfflineMessageManager.deleteMessages();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setConnection(XMPPChatConnection connection) {
        mConnection = connection;
        mOfflineMessageManager = new OfflineMessageManager(connection);
    }

    public void addInComing(Message message) {
        MessageEntry messageEntry = new MessageEntry(message);
        messageEntry.setFriendMessage(true);
        mMessageStorage.add(messageEntry);
        mContactStorage.increaseUnRead(messageEntry.getFromId(), messageEntry.getToId());
        notifyChanged(mOnComingListeners, messageEntry);
        notifyChanged(mOnUnReadChangedListener, mContactStorage.get(messageEntry.getFromId(), messageEntry.getToId()));
        Log.e("CHAT_RECEIVED", message.getBody());
    }

    public void addOutGoing(Message message) {
        MessageEntry messageEntry = new MessageEntry(message);
        messageEntry.setReceipt(ReceiptState.SENDING);
        mMessageStorage.add(messageEntry);
        notifyChanged(mOnOutGoingListeners, messageEntry);
        Log.e("CHAT_SEND", message.getBody());
    }

    /**
     * Update message state of message received has to read by me, and decrease num of unread
     * between me and friend or group
     *
     * @param message from me sent to friend to notify i have to read it
     *                to their received by them
     */
    public void updateReadMessageReceived(Message message) {
        String from = PackageAnalyze.getFromId(message);
        String to = PackageAnalyze.getToId(message);
        mMessageStorage.updateRead(message.getStanzaId(), to, from);
        mContactStorage.markToRead(PackageAnalyze.getFromId(message),
                PackageAnalyze.getToId(message));
        notifyChanged(mOnUnReadChangedListener, mContactStorage.get(from, to));
        Log.e("CHAT_READ_RECEIVED", message.getStanzaId());
    }

    /**
     * Update message receipt state sent from me to friend or group
     *
     * @param message from friend sent to me to notify this message same id sent from me
     *                to their received by them
     * @param state   Receipt state of message @see {@link ReceiptState}
     */
    public void updateReceipt(Message message, int state) {
        String id = message.getStanzaId();
        String from = PackageAnalyze.getFromId(message);
        String to = PackageAnalyze.getToId(message);
        if (state == ReceiptState.READ) {
            mMessageStorage.updateRead(id, to, from);
        } else mMessageStorage.updateReceipt(id, to, from, state);
        MessageEntry messageEntry = mMessageStorage.get(id);
        notifyChanged(mOnReceiptListeners, messageEntry);
        Log.e("CHAT_RECEIPT", message.getStanzaId());
    }

    public void updateState(Message message) {
        StateEntry stateEntry = mState.getOrDefault(
                PackageAnalyze.getFromId(message),
                PackageAnalyze.getToId(message), new StateEntry(message));
        stateEntry.setState(message);
        notifyStateChanged(stateEntry);
    }

    public int getUnreadSizeOfPair(String myId, String withId) {
        return mContactStorage.getNumOfUnread(withId, myId);
    }

    @SuppressWarnings("all")
    private void notifyStateChanged(StateEntry stateEntry) {
        for (Consumer<StateEntry> listener : mOnStateListeners.keySet()) {
            if (mOnStateListeners.get(listener).accept(stateEntry))
                listener.accept(stateEntry);
        }
    }

    @SuppressWarnings("all")
    private <T> void notifyChanged(Map<Consumer<T>, ChatFilter<T>> listeners, T item) {
        for (Consumer<T> listener : listeners.keySet()) {
            if (listeners.get(listener).accept(item))
                listener.accept(item);
        }
    }

    public void addOnUnReadChangedListener(Consumer<Contact> listener, ChatFilter<Contact> messageFilter) {
        if (!mOnUnReadChangedListener.containsKey(listener))
            mOnUnReadChangedListener.put(listener, messageFilter);
    }

    public void addOnInComingListener(Consumer<MessageEntry> listener, ChatFilter<MessageEntry> messageFilter) {
        if (!mOnComingListeners.containsKey(listener))
            mOnComingListeners.put(listener, messageFilter);
    }

    public void addOnOutGoingListener(Consumer<MessageEntry> listener, ChatFilter<MessageEntry> messageFilter) {
        if (!mOnOutGoingListeners.containsKey(listener))
            mOnOutGoingListeners.put(listener, messageFilter);
    }

    public void addOnStateListeners(Consumer<StateEntry> onStateListener, StateEntryFilter filter) {
        if (!mOnStateListeners.containsKey(onStateListener)) {
            mOnStateListeners.put(onStateListener, filter);
        }
    }

    public void addOnReceiptListeners(Consumer<MessageEntry> onStateListener, ChatFilter<MessageEntry> filter) {
        if (!mOnReceiptListeners.containsKey(onStateListener)) {
            mOnReceiptListeners.put(onStateListener, filter);
        }
    }

    public void removeOnStateListener(Consumer<StateEntry> listener) {
        mOnStateListeners.remove(listener);
    }

    public void removeOnUnReadChangedListener(Consumer<Contact> listener) {
        mOnUnReadChangedListener.remove(listener);
    }

    public void removeOnReceiptListener(Consumer<MessageEntry> listener) {
        mOnReceiptListeners.remove(listener);
    }

    public void removeOnMessageComingListener(Consumer<MessageEntry> onMessageComingListener) {
        mOnComingListeners.remove(onMessageComingListener);
    }

    public void removeOnMessageOutGoingListener(Consumer<MessageEntry> listener) {
        mOnOutGoingListeners.remove(listener);
    }

    public List<Contact> addContacts(List<Contact> contacts) {
        mContactStorage.addAll(contacts);
        return mContactStorage.getPrivate();
    }

    public List<MessageEntry> getByPair(String myId, String withId) {
        return mMessageStorage.getByPair(myId, withId);
    }
}
