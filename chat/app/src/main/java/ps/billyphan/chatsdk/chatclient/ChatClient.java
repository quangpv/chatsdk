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

import ps.billyphan.chatsdk.ChatExecutors;
import ps.billyphan.chatsdk.datasource.ChatDataSource;
import ps.billyphan.chatsdk.extension.ReadReceipt;
import ps.billyphan.chatsdk.models.MessageEntry;
import ps.billyphan.chatsdk.utils.PackageAnalyze;

public abstract class ChatClient implements LifecycleObserver {
    final ChatDataSource dataSource;
    final XMPPTCPConnection connection;
    private Lifecycle mLifecycle;

    public ChatClient(ChatDataSource dataSource) {
        this.dataSource = dataSource;
        this.connection = dataSource.getConnection();
    }

    public void send(Consumer<Message> consumer) {
        ChatExecutors.inBackground(() -> {
            try {
                Message stanza = new Message();
                stanza.setType(Message.Type.chat);
                consumer.accept(stanza);
                doSend(stanza);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    protected void onStart() {
        active();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    protected void onResume() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    protected void onPause() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    protected void onStop() {
        inactive();
        mLifecycle.removeObserver(this);
        mLifecycle = null;
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

    protected abstract void doSend(Message stanza) throws Exception;

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

    public void send(String text) {
        send(message -> message.setBody(text));
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

}
