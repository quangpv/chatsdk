package ps.billyphan.chatsdk.chatclient;

import android.support.v4.util.Consumer;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.chatstates.ChatState;

import java.util.List;

import ps.billyphan.chatsdk.ChatExecutors;
import ps.billyphan.chatsdk.datasource.ChatDataSource;
import ps.billyphan.chatsdk.filter.StateEntryFilter;
import ps.billyphan.chatsdk.filter.entry.PrivateEntryFilter;
import ps.billyphan.chatsdk.models.MessageEntry;
import ps.billyphan.chatsdk.models.StateEntry;
import ps.billyphan.chatsdk.utils.JidFormatter;

public class PrivateChat extends ChatClient {
    private final String withUserId;
    private final Chat mChat;
    private Consumer<MessageEntry> mOnMessageReceivedListener;
    private Consumer<List<MessageEntry>> mOnMessageLoadedListener;
    private Consumer<MessageEntry> mOnSendingListener;
    private Consumer<StateEntry> mOnTypingListener;

    public PrivateChat(ChatDataSource dataSource, String withUserId) {
        super(dataSource);
        this.withUserId = withUserId;
        mChat = ChatManager.getInstanceFor(connection).chatWith(JidFormatter.jid(withUserId));
    }

    @Override
    public void onStart() {
        dataSource.addOnMessageComingListener(mOnMessageReceivedListener, new PrivateEntryFilter(withUserId, getMyId()));
        dataSource.addOnMessageOutGoingListener(mOnSendingListener, new PrivateEntryFilter(getMyId(), withUserId));
        dataSource.addOnStateListeners(mOnTypingListener, new StateEntryFilter(ChatState.composing, ChatState.paused));
        ChatExecutors.loadInBackground(() -> dataSource.getMessages(new PrivateEntryFilter(getMyId(), withUserId)))
                .onMainThread(value -> {
                    if (mOnMessageLoadedListener != null) mOnMessageLoadedListener.accept(value);
                });
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        notifyReadIfNeeded();
    }

    @Override
    public void onStop() {
        dataSource.removeOnMessageComingListener(mOnMessageReceivedListener);
        dataSource.removeOnMessageOutGoingListener(mOnSendingListener);
        dataSource.removeOnStateListener(mOnTypingListener);
        super.onStop();
    }

    public void setOnReceivedListener(Consumer<MessageEntry> onMessageListener) {
        mOnMessageReceivedListener = registry(onMessageListener, this::notifyReceived);
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

    @Override
    protected void doSend(Message stanza) throws Exception {
        stanza.setFrom(JidFormatter.jid(getMyId()));
        mChat.send(stanza);
    }

    public void notifyReadIfNeeded() {
        if (dataSource.getUnreadSizeOfPair(getMyId(), withUserId) > 0) notifyRead();
    }
}
