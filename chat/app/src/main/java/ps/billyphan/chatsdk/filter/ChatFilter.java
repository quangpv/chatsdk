package ps.billyphan.chatsdk.filter;

public interface ChatFilter<T> {
    boolean accept(T t);
}
