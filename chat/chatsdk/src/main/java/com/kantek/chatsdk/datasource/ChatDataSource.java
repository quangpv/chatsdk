package com.kantek.chatsdk.datasource;

import android.content.Context;
import android.support.v4.util.Consumer;
import android.util.Log;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.offline.OfflineMessageHeader;
import org.jivesoftware.smackx.offline.OfflineMessageManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kantek.chatsdk.filter.StateEntryFilter;
import com.kantek.chatsdk.filter.UnreadFilter;
import com.kantek.chatsdk.filter.entry.ChatFilter;
import com.kantek.chatsdk.models.MessageEntry;
import com.kantek.chatsdk.models.PairHashMap;
import com.kantek.chatsdk.models.ReceiptState;
import com.kantek.chatsdk.models.StateEntry;
import com.kantek.chatsdk.utils.PackageAnalyze;
import com.kantek.chatsdk.xmpp.XMPPChatConnection;

public class ChatDataSource {
    private final ChatConversation mUnread;
    private final ChatSending mSending;
    private final PairHashMap<StateEntry> mState;
    private OfflineMessageManager mOfflineMessageManager;
    private XMPPChatConnection mConnection;
    private Map<Consumer<MessageEntry>, ChatFilter<MessageEntry>> mOnComingListeners = new HashMap<>();
    private Map<Consumer<MessageEntry>, ChatFilter<MessageEntry>> mOnOutGoingListeners = new HashMap<>();
    private Map<Consumer<StateEntry>, ChatFilter<StateEntry>> mOnStateListeners = new HashMap<>();
    private Map<Consumer<Integer>, UnreadFilter> mOnUnreadChangedListeners = new HashMap<>();
    private ChatDatabase.MessageDao mChatArchived;

    public XMPPTCPConnection getConnection() {
        return mConnection;
    }

    public ChatDataSource(Context context) {
        mUnread = new ChatConversation();
        mSending = new ChatSending();
        mState = new PairHashMap<>();
        mChatArchived = ChatDatabase.getInstance(context).messageDao();
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
            for (Message message : mOfflineMessageManager.getMessages(nodes)) {
                MessageEntry messageEntry = mUnread.push(message);
                messageEntries.add(messageEntry);
            }
            mChatArchived.addAll(messageEntries);
            mOfflineMessageManager.deleteMessages();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setConnection(XMPPChatConnection connection) {
        mConnection = connection;
        mOfflineMessageManager = new OfflineMessageManager(connection);
    }

    public List<MessageEntry> getMessages(String id1, String id2) {
        return mChatArchived.getByPairChat(id1, id2);
    }

    public void addInComing(Message message) {
        MessageEntry messageEntry = mUnread.push(message);
        messageEntry.setFriendMessage(true);
        mChatArchived.add(messageEntry);
        notifyChanged(mOnComingListeners, messageEntry);
        notifyUnreadChanged(messageEntry);
        Log.e("CHAT_RECEIVED", message.getBody());
    }

    public int getUnreadSizeOfPair(String id1, String id2) {
        return mUnread.getUnreadSizeOfPair(id1, id2);
    }

    public void updateRead(Message message) {
        MessageEntry messageEntry = mUnread.popSibling(message);
        if (messageEntry != null)
            notifyUnreadChanged(messageEntry);
    }

    public void addOutGoing(Message message) {
        MessageEntry messageEntry = mSending.push(message);
        messageEntry.setReceipt(ReceiptState.SENDING);
        mChatArchived.add(messageEntry);
        notifyChanged(mOnOutGoingListeners, messageEntry);
        Log.e("CHAT_SEND", message.getBody());
    }

    public void updateReceipt(Message message, int state) {
        if (state == ReceiptState.READ) {
            mChatArchived.update(mSending.updateRead(message));
        } else mChatArchived.update(mSending.updateReceipt(message, state));
        Log.e("CHAT_RECEIPT", message.getStanzaId());
    }

    public void updateState(Message message) {
        StateEntry stateEntry = mState.getOrDefault(
                PackageAnalyze.getFromId(message),
                PackageAnalyze.getToId(message), new StateEntry(message));
        stateEntry.setState(message);
        notifyStateChanged(stateEntry);
    }

    @SuppressWarnings("all")
    private void notifyStateChanged(StateEntry stateEntry) {
        for (Consumer<StateEntry> listener : mOnStateListeners.keySet()) {
            if (mOnStateListeners.get(listener).accept(stateEntry))
                listener.accept(stateEntry);
        }
    }

    @SuppressWarnings("all")
    private void notifyChanged(Map<Consumer<MessageEntry>, ChatFilter<MessageEntry>> listeners, MessageEntry messageEntry) {
        for (Consumer<MessageEntry> listener : listeners.keySet()) {
            if (listeners.get(listener).accept(messageEntry))
                listener.accept(messageEntry);
        }
    }

    @SuppressWarnings("all")
    private void notifyUnreadChanged(MessageEntry messageEntry) {
        for (Consumer<Integer> listener : mOnUnreadChangedListeners.keySet()) {
            if (mOnUnreadChangedListeners.get(listener).accept(messageEntry))
                listener.accept(mUnread.getUnreadSizeOfPair(messageEntry));
        }
    }

    public void addOnUnreadChangedListener(Consumer<Integer> listener, UnreadFilter filter) {
        if (!mOnUnreadChangedListeners.containsKey(listener))
            mOnUnreadChangedListeners.put(listener, filter);
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

    public void removeOnStateListener(Consumer<StateEntry> listener) {
        mOnStateListeners.remove(listener);
    }

    public void removeUnreadChangedListener(Consumer<Integer> listener) {
        mOnUnreadChangedListeners.remove(listener);
    }

    public void removeOnMessageComingListener(Consumer<MessageEntry> onMessageComingListener) {
        mOnComingListeners.remove(onMessageComingListener);
    }

    public void removeOnMessageOutGoingListener(Consumer<MessageEntry> listener) {
        mOnOutGoingListeners.remove(listener);
    }
}
