package ps.billyphan.chatsdk.listeners;

import android.support.v4.util.Consumer;

import ps.billyphan.chatsdk.filter.StateEntryFilter;

public class StateListenerWrapper<T> {
    public final StateEntryFilter filter;
    public Consumer<T> listener;

    public StateListenerWrapper(Consumer<T> listener, StateEntryFilter filter) {
        this.listener = listener;
        this.filter = filter;
    }
}
