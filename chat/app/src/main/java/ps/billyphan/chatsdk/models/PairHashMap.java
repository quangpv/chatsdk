package ps.billyphan.chatsdk.models;

import org.jivesoftware.smack.packet.Message;

import java.util.HashMap;

import ps.billyphan.chatsdk.listeners.Supplier;
import ps.billyphan.chatsdk.utils.PackageAnalyze;

public class PairHashMap<T> extends HashMap<String, T> {

    public T get(Message key) {
        T data = super.get(PackageAnalyze.getChatPair(key));
        if (data == null) return super.get(PackageAnalyze.getRevertChatPair(key));
        return data;
    }

    public T get(String id1, String id2) {
        T data = super.get(PackageAnalyze.getChatPair(id1, id2));
        if (data == null) return super.get(PackageAnalyze.getChatPair(id2, id1));
        return data;
    }

    public T get(MessageEntry key) {
        T data = super.get(PackageAnalyze.getChatPair(key));
        if (data == null) return super.get(PackageAnalyze.getRevertChatPair(key));
        return data;
    }

    public T getOrDefault(MessageEntry key, T item) {
        T data = get(key);
        if (data == null) {
            data = item;
            put(key, data);
        }
        return data;
    }

    public T getOrCreate(Message key, Supplier<T> supplier) {
        T data = get(key);
        if (data == null) {
            data = supplier.get();
            put(key, data);
        }
        return data;
    }

    public T getOrDefault(Message key, T item) {
        T data = get(key);
        if (data == null) {
            data = item;
            put(key, data);
        }
        return data;
    }

    public T put(Message key, T value) {
        return super.put(PackageAnalyze.getChatPair(key), value);
    }

    public T put(MessageEntry key, T value) {
        return super.put(PackageAnalyze.getChatPair(key), value);
    }

    public boolean remove(Message key) {
        String pair = PackageAnalyze.getChatPair(key);
        if (containsKey(pair)) {
            super.remove(pair);
            return true;
        } else {
            pair = PackageAnalyze.getRevertChatPair(key);
            if (pair.contains(pair)) {
                super.remove(pair);
                return true;
            }
        }
        return false;
    }

    @Deprecated
    @Override
    public boolean remove(Object key, Object value) {
        throw new RuntimeException("Not support");
    }

    @Deprecated
    @Override
    public T remove(Object key) {
        throw new RuntimeException("Not support");
    }

    @Deprecated
    @Override
    public T get(Object key) {
        throw new RuntimeException("Not support");
    }

    @Deprecated
    @Override
    public T getOrDefault(Object key, T defaultValue) {
        throw new RuntimeException("Not support");
    }

    @Deprecated
    @Override
    public T put(String key, T value) {
        throw new RuntimeException("Not support");
    }

    @Deprecated
    @Override
    public T putIfAbsent(String key, T value) {
        throw new RuntimeException("Not support");
    }
}
