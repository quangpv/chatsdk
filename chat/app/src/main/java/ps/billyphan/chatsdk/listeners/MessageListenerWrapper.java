package ps.billyphan.chatsdk.listeners;

import android.support.v4.util.Consumer;

import ps.billyphan.chatsdk.filter.MessageFilter;

public class MessageListenerWrapper<T> {
    public final MessageFilter filter;
    public Consumer<T> listener;

    public MessageListenerWrapper(Consumer<T> onMessageComingListener, MessageFilter messageFilter) {
        this.listener = onMessageComingListener;
        this.filter = messageFilter;
    }
}
