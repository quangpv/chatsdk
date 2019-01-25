package com.kantek.chatsdk.chatclient;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.v4.util.Consumer;
import android.util.Log;

import com.kantek.chatsdk.datasource.ChatDataSource;
import com.kantek.chatsdk.extension.ReadReceipt;
import com.kantek.chatsdk.filter.entry.StateEntryFilter;
import com.kantek.chatsdk.filter.entry.MessageFilter;
import com.kantek.chatsdk.models.MessageEntry;
import com.kantek.chatsdk.models.PageList;
import com.kantek.chatsdk.models.StateEntry;
import com.kantek.chatsdk.utils.ChatExecutors;
import com.kantek.chatsdk.utils.PackageAnalyze;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;

public abstract class ChatClient implements LifecycleObserver {
    final ChatDataSource dataSource;
    final XMPPTCPConnection connection;
    private final String mWithId;
    private final PageList<MessageEntry> mPageList = new PageList<>();
    private Lifecycle mLifecycle;

    private Consumer<MessageEntry> mOnSendingListener;
    private Consumer<MessageEntry> mOnMessageReceivedListener;
    private Consumer<StateEntry> mOnTypingListener;
    private Consumer<MessageEntry> mOnReceiptListener;
    private MutableLiveData<PageList<MessageEntry>> mLiveData = new MutableLiveData<>();

    public ChatClient(ChatDataSource dataSource, String id) {
        this.dataSource = dataSource;
        this.connection = dataSource.getConnection();
        this.mWithId = id;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    protected void onStart() {
        dataSource.addOnInComingListener(mOnMessageReceivedListener = messageEntry -> {
            if (mLifecycle != null && mLifecycle.getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                notifyRead();
            else notifyReceived(messageEntry);
            mPageList.add(messageEntry);
        }, new MessageFilter(getWithId(), getMyId()));
        dataSource.addOnOutGoingListener(mOnSendingListener = mPageList::add, new MessageFilter(getMyId(), getWithId()));
        dataSource.addOnStateListeners(mOnTypingListener, new StateEntryFilter(ChatState.composing, ChatState.paused, ChatState.active));
        dataSource.addOnReceiptListeners(mOnReceiptListener = mPageList::update, new MessageFilter(getMyId(), getWithId()));
        ChatExecutors.inBackground(() -> {
            mPageList.addAll(dataSource.getByPair(getMyId(), mWithId));
            mLiveData.postValue(mPageList);
        });
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    protected void onResume() {
        notifyReadIfNeeded();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    protected void onPause() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    protected void onStop() {
        dataSource.removeOnMessageComingListener(mOnMessageReceivedListener);
        dataSource.removeOnMessageOutGoingListener(mOnSendingListener);
        dataSource.removeOnStateListener(mOnTypingListener);
        dataSource.removeOnReceiptListener(mOnReceiptListener);
        mLifecycle.removeObserver(this);
        mLifecycle = null;
    }

    public String getWithId() {
        return mWithId;
    }

    public void setOnTypingListener(Consumer<Boolean> onTypingListener) {
        mOnTypingListener = registry(stateEntry -> onTypingListener.accept(stateEntry.isTyping()));
    }

    protected <T> Consumer<T> registry(Consumer<T> onMessageListener) {
        return messageEntry ->
                ChatExecutors.onMainThread(() -> onMessageListener.accept(messageEntry));
    }

    protected <T> Consumer<T> registry(Consumer<T> onMessageListener, Consumer<T> consumer) {
        return messageEntry ->
                ChatExecutors.onMainThread(() -> {
                    onMessageListener.accept(messageEntry);
                    consumer.accept(messageEntry);
                });
    }

    public String getMyId() {
        return PackageAnalyze.getId(connection.getUser());
    }

    public void send(Consumer<Message> consumer) {
        ChatExecutors.inBackground(() -> {
            try {
                Message message = new Message();
                consumer.accept(message);
                doSend(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void send(String text) {
        send(message -> message.setBody(text));
    }

    public void notifyRead() {
        send(message -> message.addExtension(new ReadReceipt()));
    }

    protected void notifyReceived(MessageEntry messageEntry) {
        send(message -> {
            message.setStanzaId(messageEntry.getId());
            message.addExtension(new DeliveryReceipt(messageEntry.getId()));
        });
    }

    protected void inactive() {
        send(message -> message.addExtension(new ChatStateExtension(ChatState.inactive)));
    }

    protected void active() {
        send(message -> message.addExtension(new ChatStateExtension(ChatState.active)));
    }

    public void notifyTyping(String state) {
        Log.e("AT_STATE", state);
        send(message -> message.addExtension(
                new ChatStateExtension(ChatState.valueOf(state)))
        );
    }

    public void setLifecycle(Lifecycle lifecycle) {
        mLifecycle = lifecycle;
        lifecycle.addObserver(this);
    }

    public void notifyReadIfNeeded() {
        ChatExecutors.inBackground(() -> {
            if (dataSource.getUnreadSizeOfPair(getMyId(), getWithId()) > 0) notifyRead();
        });
    }

    protected abstract void doSend(Message stanza) throws Exception;

    public LiveData<PageList<MessageEntry>> asLiveData() {
        return mLiveData;
    }
}
