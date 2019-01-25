package com.kantek.chatsdk.models;

import android.support.v4.util.Pair;

import com.kantek.chatsdk.listeners.BiConsumer;

import java.util.HashMap;

public class AtomicUnRead {

    private HashMap<String, Integer> mUnRead = new HashMap<>();

    public void putOrIncrease(String fromId, String toId, int i) {
        String pair = PairHashMap.encode(fromId, toId);
        if (mUnRead.containsKey(pair)) {
            mUnRead.put(pair, mUnRead.get(pair) + i);
        } else {
            mUnRead.put(pair, i);
        }
    }


    public void forEach(BiConsumer<Pair<String, String>, Integer> consumer) {
        for (String pair : mUnRead.keySet()) {
            String[] ids = PairHashMap.decode(pair);
            consumer.accept(new Pair<>(ids[0], ids[1]), mUnRead.get(pair));
        }
    }
}
