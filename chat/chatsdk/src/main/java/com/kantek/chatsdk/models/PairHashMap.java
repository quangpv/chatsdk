package com.kantek.chatsdk.models;

import com.kantek.chatsdk.listeners.Supplier;

import java.util.HashMap;

public class PairHashMap<T> {
    private HashMap<String, T> mMap = new HashMap<>();

    public static String getPair(String id1, String id2) {
        return String.format("%s#%s", id1, id2);
    }

    public T get(String id1, String id2) {
        T data = mMap.get(getPair(id1, id2));
        if (data == null) return mMap.get(getPair(id2, id1));
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
        return mMap.put(getPair(id1, id2), value);
    }

    public boolean remove(String id1, String id2) {
        String pair = getPair(id1, id2);
        if (mMap.containsKey(pair)) {
            mMap.remove(pair);
            return true;
        } else {
            pair = getPair(id2, id1);
            if (pair.contains(pair)) {
                mMap.remove(pair);
                return true;
            }
        }
        return false;
    }
}
