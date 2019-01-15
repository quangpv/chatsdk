package ps.billyphan.chatsdk.models;

import java.util.HashMap;

import ps.billyphan.chatsdk.listeners.Supplier;

public class PairHashMap<T> extends HashMap<String, T> {

    public static String getPair(String id1, String id2) {
        return String.format("%s#%s", id1, id2);
    }

    public T get(String id1, String id2) {
        T data = super.get(getPair(id1, id2));
        if (data == null) return super.get(getPair(id2, id1));
        return data;
    }

    public T getOrDefault(String id1, String id2, T item) {
        T data = get(id1, id2);
        if (data == null) {
            data = item;
            put(id1, id2, data);
        }
        return data;
    }

    public T getOrCreate(String id1, String id2, Supplier<T> supplier) {
        T data = get(id1, id2);
        if (data == null) {
            data = supplier.get();
            put(id1, id2, data);
        }
        return data;
    }

    public T put(String id1, String id2, T value) {
        return super.put(getPair(id1, id2), value);
    }

    public boolean remove(String id1, String id2) {
        String pair = getPair(id1, id2);
        if (containsKey(pair)) {
            super.remove(pair);
            return true;
        } else {
            pair = getPair(id2, id1);
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
