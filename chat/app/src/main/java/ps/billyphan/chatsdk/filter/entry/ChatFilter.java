package ps.billyphan.chatsdk.filter.entry;

public interface ChatFilter<T> {
    boolean accept(T t);
}
