package ps.billyphan.chatsdk.datasource;

import org.jivesoftware.smack.packet.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ps.billyphan.chatsdk.models.MessageEntry;
import ps.billyphan.chatsdk.models.PairHashMap;
import ps.billyphan.chatsdk.utils.PackageAnalyze;

@SuppressWarnings("all")
public class ChatConversation {
    private PairHashMap<Map<String, MessageEntry>> mConversations = new PairHashMap<>();

    public MessageEntry push(Message message) {
        MessageEntry messageEntry = new MessageEntry(message);
        push(messageEntry);
        return messageEntry;
    }

    public void push(MessageEntry message) {
        String pair = PackageAnalyze.getChatPair(message);
        Map<String, MessageEntry> messageEntries = mConversations.getOrDefault(message, new HashMap<>());
        messageEntries.put(message.getId(), message);
    }

    public MessageEntry popSibling(Message message) {
        Map<String, MessageEntry> entry = mConversations.get(message);
        if (entry != null) {
            MessageEntry messageEntry = entry.get(message.getStanzaId());
            if (messageEntry == null) messageEntry = new MessageEntry(message);
            mConversations.remove(message);
            return messageEntry;
        }
        return null;
    }

    public int getUnreadSizeOfPair(MessageEntry message) {
        Map<String, MessageEntry> data = mConversations.get(message);
        if (data == null) return 0;
        return data.size();
    }

    public int getUnreadSizeOfPair(String id1, String id2) {
        Map<String, MessageEntry> data = mConversations.get(id1, id2);
        if (data == null) return 0;
        return data.size();
    }

    public List<MessageEntry> getMessages(String id1, String id2) {
        List<MessageEntry> messageEntries = new ArrayList<>();
        Map<String, MessageEntry> data = mConversations.get(id1, id2);
        if (data != null) {
            for (MessageEntry messageEntry : data.values()) {
                messageEntries.add(messageEntry);
            }
        }
        return messageEntries;
    }
}
