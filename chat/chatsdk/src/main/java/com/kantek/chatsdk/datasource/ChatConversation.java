package com.kantek.chatsdk.datasource;

import com.kantek.chatsdk.models.MessageEntry;
import com.kantek.chatsdk.models.PairHashMap;
import com.kantek.chatsdk.utils.PackageAnalyze;

import org.jivesoftware.smack.packet.Message;

import java.util.HashMap;
import java.util.Map;

public class ChatConversation {
    private PairHashMap<Map<String, MessageEntry>> mConversations = new PairHashMap<>();

    public MessageEntry push(Message message) {
        MessageEntry messageEntry = new MessageEntry(message);
        push(messageEntry);
        return messageEntry;
    }

    public void push(MessageEntry message) {
        Map<String, MessageEntry> messageEntries = mConversations.getOrDefault(message.getFromId(), message.getToId(), new HashMap<>());
        messageEntries.put(message.getId(), message);
    }

    public MessageEntry popSibling(Message message) {
        String id1 = PackageAnalyze.getFromId(message);
        String id2 = PackageAnalyze.getToId(message);

        Map<String, MessageEntry> entry = mConversations.get(id1, id2);
        if (entry != null) {
            MessageEntry messageEntry = entry.get(message.getStanzaId());
            if (messageEntry == null) messageEntry = new MessageEntry(message);
            mConversations.remove(id1, id2);
            return messageEntry;
        }
        return null;
    }

    public int getUnreadSizeOfPair(MessageEntry message) {
        Map<String, MessageEntry> data = mConversations.get(message.getFromId(), message.getToId());
        if (data == null) return 0;
        return data.size();
    }

    public int getUnreadSizeOfPair(String id1, String id2) {
        Map<String, MessageEntry> data = mConversations.get(id1, id2);
        if (data == null) return 0;
        return data.size();
    }
}
