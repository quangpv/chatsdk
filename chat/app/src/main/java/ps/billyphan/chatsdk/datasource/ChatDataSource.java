package ps.billyphan.chatsdk.datasource;

import android.support.v4.util.Consumer;
import android.util.Log;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.offline.OfflineMessageHeader;
import org.jivesoftware.smackx.offline.OfflineMessageManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ps.billyphan.chatsdk.AppExecutors;
import ps.billyphan.chatsdk.ChatConversation;
import ps.billyphan.chatsdk.filter.MessageFilter;
import ps.billyphan.chatsdk.filter.StateEntryFilter;
import ps.billyphan.chatsdk.filter.UnreadFilter;
import ps.billyphan.chatsdk.listeners.MessageListenerWrapper;
import ps.billyphan.chatsdk.listeners.StateListenerWrapper;
import ps.billyphan.chatsdk.models.MessageEntry;
import ps.billyphan.chatsdk.models.StateEntry;
import ps.billyphan.chatsdk.utils.PackageAnalyze;

public class ChatDataSource {
    private final ChatConversation mUnreadConversation;
    private final Map<String, MessageEntry> mSendingConversation;
    private final Map<String, StateEntry> mState;
    private OfflineMessageManager mOfflineMessageManager;
    private XMPPTCPConnection mConnection;
    private Set<MessageListenerWrapper<MessageEntry>> mOnMessageComingListeners = new HashSet<>();
    private Set<MessageListenerWrapper<Integer>> mOnMessageUnreadChangedListeners = new HashSet<>();
    private Set<MessageListenerWrapper<MessageEntry>> mOnMessageOutGoingListeners = new HashSet<>();
    private Set<MessageListenerWrapper<MessageEntry>> mOnReceiptListeners = new HashSet<>();
    private Set<StateListenerWrapper<StateEntry>> mOnStateListeners = new HashSet<>();
    private ChatDao mChatDao = new ChatDao();

    public XMPPTCPConnection getConnection() {
        return mConnection;
    }

    public ChatDataSource() {
        mUnreadConversation = new ChatConversation();
        mSendingConversation = new HashMap<>();
        mState = new HashMap<>();
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
                MessageEntry messageEntry = mUnreadConversation.push(message);
                messageEntries.add(messageEntry);
            }
            mChatDao.addAll(messageEntries);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setConnection(XMPPTCPConnection connection) {
        mConnection = connection;
        mOfflineMessageManager = new OfflineMessageManager(connection);
    }

    public void addInComing(Message message) {
        MessageEntry messageEntry = mUnreadConversation.push(message);
        mChatDao.add(messageEntry);
        notifyInComing(messageEntry);
        Log.e("DEBUG", "Unread message: " + mUnreadConversation.getUnreadSizeOfPair(messageEntry));
    }

    public void updateRead(Message message) {
        MessageEntry messageEntry = mUnreadConversation.pop(message);
        notifyNumOfUnreadChanged(messageEntry);
    }

    public List<MessageEntry> getMessages(MessageFilter messageFilter) {
        List<MessageEntry> data = mChatDao.getByPrivateChat(messageFilter.getFrom(), messageFilter.getTo());
        Collections.sort(data, MessageEntry::compareTime);
        return data;
    }

    public void addOutGoing(Message message) {
        MessageEntry messageEntry = new MessageEntry(message);
        mSendingConversation.put(message.getStanzaId(), messageEntry);
        mChatDao.add(messageEntry);
        notifyOutGoing(messageEntry);
    }

    public void updateReceipt(Message message, int sending) {
        MessageEntry oldMessage = mSendingConversation.get(message.getStanzaId());
        if (oldMessage != null) {
            oldMessage.setReceipt(sending);
            if (oldMessage.isRead()) mSendingConversation.remove(message.getStanzaId());
        } else {
            oldMessage = new MessageEntry(message);
            oldMessage.setReceipt(sending);
            mSendingConversation.put(message.getStanzaId(), oldMessage);
        }
        mChatDao.addOrUpdate(oldMessage);
        notifyReceipt(oldMessage);
    }

    public void updateState(Message message) {
        String pair = PackageAnalyze.getChatPair(message);
        StateEntry stateEntry = PackageAnalyze.getAtPair(mState, message);
        if (stateEntry == null) {
            stateEntry = new StateEntry(message);
            mState.put(pair, stateEntry);
        } else stateEntry.setState(message);
        notifyStateChanged(stateEntry);
    }

    private void notifyStateChanged(StateEntry stateEntry) {
        for (StateListenerWrapper<StateEntry> wrapper : mOnStateListeners) {
            if (wrapper.filter.accept(stateEntry)) {
                AppExecutors.onMainThread(() -> wrapper.listener.accept(stateEntry));
            }
        }
    }

    private void notifyReceipt(MessageEntry messageEntry) {
        for (MessageListenerWrapper<MessageEntry> wrapper : mOnReceiptListeners) {
            if (wrapper.filter.accept(messageEntry)) {
                AppExecutors.onMainThread(() -> wrapper.listener.accept(messageEntry));
            }
        }
    }

    private void notifyNumOfUnreadChanged(MessageEntry message) {
        for (MessageListenerWrapper<Integer> wrapper : mOnMessageUnreadChangedListeners) {
            if (wrapper.filter.accept(message)) {
                AppExecutors.onMainThread(() -> wrapper.listener.accept(mUnreadConversation.getUnreadSizeOfPair(message)));
            }
        }
    }

    private void notifyInComing(MessageEntry message) {
        for (MessageListenerWrapper<MessageEntry> onMessageComingListener : mOnMessageComingListeners) {
            if (onMessageComingListener.filter.accept(message)) {
                AppExecutors.onMainThread(() -> onMessageComingListener.listener.accept(message));
            }
        }
    }

    public void notifyOutGoing(MessageEntry message) {
        for (MessageListenerWrapper<MessageEntry> onMessageOutGoingListener : mOnMessageOutGoingListeners) {
            if (onMessageOutGoingListener.filter.accept(message)) {
                AppExecutors.onMainThread(() -> onMessageOutGoingListener.listener.accept(message));
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

    public void addOnReceiptListener(Consumer<MessageEntry> listener, MessageFilter messageFilter) {
        mOnReceiptListeners.add(new MessageListenerWrapper<>(listener, messageFilter));
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

    public void removeOnReceiptListener(Consumer<MessageEntry> onReceipt) {
        for (MessageListenerWrapper wrapper : mOnReceiptListeners) {
            if (wrapper.listener == onReceipt) {
                mOnReceiptListeners.remove(wrapper);
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
}
