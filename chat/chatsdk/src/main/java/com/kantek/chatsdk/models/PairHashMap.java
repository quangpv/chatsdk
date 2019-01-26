package com.kantek.chatsdk.models;

import com.kantek.chatsdk.listeners.Supplier;

import java.util.HashMap;

public class PairHashMap<T> {
    private HashMap<String, T> mMap = new HashMap<>();

    public static String encode(String id1, String id2) {
        return String.format("%s#%s", id1, id2);
    }

    public static String[] decode(String pair) {
        String[] ids = pair.split("#");
        if (ids.length < 2) throw new RuntimeException("Pair invalid");
        return ids;
    }

    public T get(String id1, String id2) {
        T data = mMap.get(encode(id1, id2));
        if (data == null) return mMap.get(encode(id2, id1));
        return data;
    }

    public T get(String pair) {
        String[] ids = decode(pair);
        return get(ids[0], ids[1]);
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
        return mMap.put(encode(id1, id2), value);
    }

    public T put(String pair, T value) {
        String[] ids = decode(pair);
        return put(ids[0], ids[1], value);
    }

    public boolean remove(String id1, String id2) {
        String pair = encode(id1, id2);
        if (mMap.containsKey(pair)) {
            mMap.remove(pair);
            return true;
        } else {
            pair = encode(id2, id1);
            if (pair.contains(pair)) {
                mMap.remove(pair);
                return true;
            }
        }
        return false;
    }

    public void remove(String pair) {
        String[] ids = decode(pair);
        remove(ids[0], ids[1]);
    }

    public void clear() {
        mMap.clear();
    }
}
