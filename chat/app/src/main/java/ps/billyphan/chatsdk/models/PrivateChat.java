package ps.billyphan.chatsdk.models;

import android.support.v4.util.Consumer;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.StandardExtensionElement;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;

import java.util.List;

import ps.billyphan.chatsdk.AppExecutors;
import ps.billyphan.chatsdk.datasource.ChatDataSource;
import ps.billyphan.chatsdk.filter.PrivateEntryFilter;
import ps.billyphan.chatsdk.filter.ReceiptEntryFilter;
import ps.billyphan.chatsdk.filter.StateEntryFilter;
import ps.billyphan.chatsdk.utils.JidFormatter;
import ps.billyphan.chatsdk.utils.PackageAnalyze;

public class PrivateChat {
    private final XMPPTCPConnection mConnection;
    private final ChatDataSource mDataSource;
    private final String withUserId;
    private final Chat mChat;
    private Consumer<MessageEntry> mOnMessageReceivedListener;
    private Consumer<List<MessageEntry>> mOnMessageLoadedListener;
    private Consumer<MessageEntry> mOnSendingListener;
    private Consumer<MessageEntry> mOnReceiptListener;
    private Consumer<StateEntry> mOnTypingListener;

    public PrivateChat(ChatDataSource dataSource, String withUserId) {
        mConnection = dataSource.getConnection();
        mDataSource = dataSource;
        this.withUserId = withUserId;
        mChat = ChatManager.getInstanceFor(mConnection).chatWith(JidFormatter.jid(withUserId));
    }

    public void start() {
        mDataSource.addOnMessageComingListener(mOnMessageReceivedListener, new PrivateEntryFilter(withUserId, getMyId()));
        mDataSource.addOnReceiptListener(mOnReceiptListener, new ReceiptEntryFilter(withUserId, getMyId()));
        mDataSource.addOnStateListeners(mOnTypingListener, new StateEntryFilter(ChatState.composing, ChatState.paused));
        AppExecutors.loadInBackground(() -> mDataSource.getMessages(new PrivateEntryFilter(getMyId(), withUserId)))
                .onMainThread(value -> {
                    if (mOnMessageLoadedListener != null) mOnMessageLoadedListener.accept(value);
                });
        active();
    }

    private String getMyId() {
        return PackageAnalyze.getId(mConnection.getUser());
    }

    public void stop() {
        mDataSource.removeOnMessageComingListener(mOnMessageReceivedListener);
        mDataSource.removeOnReceiptListener(mOnReceiptListener);
        mDataSource.removeOnStateListener(mOnTypingListener);
        inactive();
    }

    public void setOnMessageReceivedListener(Consumer<MessageEntry> onMessageListener) {
        this.mOnMessageReceivedListener = messageEntry -> {
            onMessageListener.accept(messageEntry);
            send(message -> message.addExtension(new StandardExtensionElement(DeliveryReceipt.ELEMENT, DeliveryReceipt.NAMESPACE)));
        };
    }

    public void setOnSendingListener(Consumer<MessageEntry> onMessageListener) {
        this.mOnSendingListener = onMessageListener;
    }

    public void setOnTypingListener(Consumer<StateEntry> onTypingListener) {
        mOnTypingListener = onTypingListener;
    }

    public void setOnReceiptListener(Consumer<MessageEntry> onMessageListener) {
        this.mOnReceiptListener = onMessageListener;
    }

    public void setOnMessageLoadedListener(Consumer<List<MessageEntry>> onMessageListener) {
        this.mOnMessageLoadedListener = onMessageListener;
    }

    private void send(Consumer<Message> consumer) {
        AppExecutors.inBackground(() -> {
            try {
                Message stanza = new Message();
                stanza.setType(Message.Type.chat);
                consumer.accept(stanza);
                mChat.send(stanza);
            } catch (SmackException.NotConnectedException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private void inactive() {
        send(message -> message.addExtension(new ChatStateExtension(ChatState.inactive)));
    }

    private void active() {
        send(message -> message.addExtension(new ChatStateExtension(ChatState.active)));
    }

    public void send(String text) {
        send(message -> message.setBody(text));
    }
}
