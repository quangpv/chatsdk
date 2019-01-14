package ps.billyphan.chatsdk.datasource;

import android.support.v4.util.Consumer;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.offline.OfflineMessageHeader;
import org.jivesoftware.smackx.offline.OfflineMessageManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ps.billyphan.chatsdk.filter.StateEntryFilter;
import ps.billyphan.chatsdk.filter.UnreadFilter;
import ps.billyphan.chatsdk.filter.entry.MessageFilter;
import ps.billyphan.chatsdk.listeners.MessageListenerWrapper;
import ps.billyphan.chatsdk.listeners.StateListenerWrapper;
import ps.billyphan.chatsdk.models.MessageEntry;
import ps.billyphan.chatsdk.models.PairHashMap;
import ps.billyphan.chatsdk.models.ReceiptState;
import ps.billyphan.chatsdk.models.StateEntry;

public class ChatDataSource {
    private final ChatConversation mUnread;
    private final ChatSending mSending;
    private final PairHashMap<StateEntry> mState;
    private OfflineMessageManager mOfflineMessageManager;
    private XMPPTCPConnection mConnection;
    private Set<MessageListenerWrapper<MessageEntry>> mOnMessageComingListeners = new HashSet<>();
    private Set<MessageListenerWrapper<Integer>> mOnMessageUnreadChangedListeners = new HashSet<>();
    private Set<MessageListenerWrapper<MessageEntry>> mOnMessageOutGoingListeners = new HashSet<>();
    private Set<StateListenerWrapper<StateEntry>> mOnStateListeners = new HashSet<>();
    private ChatArchived mChatArchived = new ChatArchived();

    public XMPPTCPConnection getConnection() {
        return mConnection;
    }

    public ChatDataSource() {
        mUnread = new ChatConversation();
        mSending = new ChatSending();
        mState = new PairHashMap<>();
    }

    public void fetchOfflineMessages() {
        if (mOfflineMessageManager == null)
            throw new RuntimeException("Not set offline message manager");
        try {
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

    public void setConnection(XMPPTCPConnection connection) {
        mConnection = connection;
        mOfflineMessageManager = new OfflineMessageManager(connection);
    }

    public List<MessageEntry> getMessages(MessageFilter messageFilter) {
        List<MessageEntry> data = mChatArchived.getByPairChat(messageFilter.getFrom(), messageFilter.getTo());
        Collections.sort(data, MessageEntry::compareTime);
        return data;
    }

    public void addInComing(Message message) {
        MessageEntry messageEntry = mUnread.push(message);
        messageEntry.setSendFromFriend(true);
        mChatArchived.save(messageEntry);
        for (MessageListenerWrapper<MessageEntry> wrapper : mOnMessageComingListeners) {
            if (wrapper.filter.accept(messageEntry)) {
                wrapper.listener.accept(messageEntry);
            }
        }
        notifyUnreadChanged(messageEntry);
    }

    public int getUnreadSizeOfPair(String id1, String id2) {
        return mUnread.getUnreadSizeOfPair(id1, id2);
    }

    public void updateRead(Message message) {
        MessageEntry messageEntry = mUnread.popSibling(message);
        if (messageEntry != null)
            notifyUnreadChanged(messageEntry);
    }

    private void notifyUnreadChanged(MessageEntry messageEntry) {
        for (MessageListenerWrapper<Integer> wrapper : mOnMessageUnreadChangedListeners) {
            if (wrapper.filter.accept(messageEntry)) {
                wrapper.listener.accept(mUnread.getUnreadSizeOfPair(messageEntry));
            }
        }
    }

    public void addOutGoing(Message message) {
        MessageEntry messageEntry = mSending.push(message);
        mChatArchived.save(messageEntry);
        for (MessageListenerWrapper<MessageEntry> wrapper : mOnMessageOutGoingListeners) {
            if (wrapper.filter.accept(messageEntry)) {
                wrapper.listener.accept(messageEntry);
            }
        }
    }

    public void updateReceipt(Message message, int state) {
        if (state == ReceiptState.READ) {
            mChatArchived.saves(mSending.updateRead(message));
        } else mChatArchived.save(mSending.updateReceipt(message, state));
    }

    public void updateState(Message message) {
        StateEntry stateEntry = mState.getOrDefault(message, new StateEntry(message));
        stateEntry.setState(message);
        for (StateListenerWrapper<StateEntry> wrapper : mOnStateListeners) {
            if (wrapper.filter.accept(stateEntry)) {
                wrapper.listener.accept(stateEntry);
            }
        }
    }

    public void addOnMessageUnreadChangedListener(Consumer<Integer> onUnreadChangeListener, UnreadFilter unreadFilter) {
        mOnMessageUnreadChangedListeners.add(new MessageListenerWrapper<>(onUnreadChangeListener, unreadFilter));
    }

    public void addOnMessageComingListener(Consumer<MessageEntry> onMessageComingListener, MessageFilter messageFilter) {
        mOnMessageComingListeners.add(new MessageListenerWrapper<>(onMessageComingListener, messageFilter));
    }

    public void addOnMessageOutGoingListener(Consumer<MessageEntry> listener, MessageFilter messageFilter) {
        mOnMessageOutGoingListeners.add(new MessageListenerWrapper<>(listener, messageFilter));
    }

    public void removeOnMessageComingListener(Consumer<MessageEntry> onMessageComingListener) {
        for (MessageListenerWrapper messageComingListener : mOnMessageComingListeners) {
            if (messageComingListener.listener == onMessageComingListener) {
                mOnMessageComingListeners.remove(messageComingListener);
                break;
            }
        }
    }

    public void removeOnMessageOutGoingListener(Consumer<MessageEntry> listener) {
        for (MessageListenerWrapper wrapper : mOnMessageOutGoingListeners) {
            if (wrapper.listener == listener) {
                mOnMessageOutGoingListeners.remove(wrapper);
                break;
            }
        }
    }

    public void addOnStateListeners(Consumer<StateEntry> onStateListener, StateEntryFilter filter) {
        mOnStateListeners.add(new StateListenerWrapper<>(onStateListener, filter));
    }

    public void removeOnStateListener(Consumer<StateEntry> listener) {
        for (StateListenerWrapper<StateEntry> wrapper : mOnStateListeners) {
            if (wrapper.listener == listener) {
                mOnStateListeners.remove(wrapper);
                break;
            }
        }
    }

    public void removeUnreadChangedListener(Consumer<Integer> listener) {
        for (MessageListenerWrapper<Integer> wrapper : mOnMessageUnreadChangedListeners) {
            if (wrapper.listener == listener) {
                mOnMessageUnreadChangedListeners.remove(wrapper);
                break;
            }
        }
    }
}
