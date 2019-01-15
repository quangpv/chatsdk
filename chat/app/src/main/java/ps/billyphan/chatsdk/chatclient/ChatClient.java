package ps.billyphan.chatsdk.chatclient;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.v4.util.Consumer;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;

import java.util.List;

import ps.billyphan.chatsdk.ChatExecutors;
import ps.billyphan.chatsdk.datasource.ChatDataSource;
import ps.billyphan.chatsdk.extension.ReadReceipt;
import ps.billyphan.chatsdk.filter.StateEntryFilter;
import ps.billyphan.chatsdk.filter.entry.MessageFilter;
import ps.billyphan.chatsdk.models.MessageEntry;
import ps.billyphan.chatsdk.models.StateEntry;
import ps.billyphan.chatsdk.utils.PackageAnalyze;

public abstract class ChatClient implements LifecycleObserver {
    final ChatDataSource dataSource;
    final XMPPTCPConnection connection;
    private final String mWithId;
    private Lifecycle mLifecycle;

    private Consumer<MessageEntry> mOnMessageReceivedListener;
    private Consumer<List<MessageEntry>> mOnMessageLoadedListener;
    private Consumer<MessageEntry> mOnSendingListener;
    private Consumer<StateEntry> mOnTypingListener;

    public ChatClient(ChatDataSource dataSource, String id) {
        this.dataSource = dataSource;
        this.connection = dataSource.getConnection();
        this.mWithId = id;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    protected void onStart() {
        dataSource.addOnInComingListener(mOnMessageReceivedListener, new MessageFilter(getWithId(), getMyId()));
        dataSource.addOnOutGoingListener(mOnSendingListener, new MessageFilter(getMyId(), getWithId()));
        dataSource.addOnStateListeners(mOnTypingListener, new StateEntryFilter(ChatState.composing, ChatState.paused));
        ChatExecutors.loadInBackground(() -> dataSource.getMessages(getMyId(), mWithId))
                .onMainThread(value -> {
                    if (mOnMessageLoadedListener != null) mOnMessageLoadedListener.accept(value);
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
        mLifecycle.removeObserver(this);
        mLifecycle = null;
    }

    public String getWithId() {
        return mWithId;
    }

    public void setOnReceivedListener(Consumer<MessageEntry> onMessageListener) {
        mOnMessageReceivedListener = registry(onMessageListener, message -> {
            if (mLifecycle != null && mLifecycle.getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                notifyRead();
            else notifyReceived(message);
        });
    }

    public void setOnTypingListener(Consumer<StateEntry> onTypingListener) {
        mOnTypingListener = registry(onTypingListener);
    }

    public void setOnSendingListener(Consumer<MessageEntry> onMessageListener) {
        mOnSendingListener = registry(onMessageListener);
    }

    public void setOnLoadedListener(Consumer<List<MessageEntry>> onMessageListener) {
        mOnMessageLoadedListener = registry(onMessageListener);
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

    public void notifyTyping(boolean b) {
        send(message -> message.addExtension(
                new ChatStateExtension(b ? ChatState.composing : ChatState.paused))
        );
    }

    public void setLifecycle(Lifecycle lifecycle) {
        mLifecycle = lifecycle;
        lifecycle.addObserver(this);
    }

    public void notifyReadIfNeeded() {
        if (dataSource.getUnreadSizeOfPair(getMyId(), getWithId()) > 0) notifyRead();
    }

    protected abstract void doSend(Message stanza) throws Exception;

}
