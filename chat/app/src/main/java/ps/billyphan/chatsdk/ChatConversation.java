package ps.billyphan.chatsdk;

import org.jivesoftware.smack.packet.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ps.billyphan.chatsdk.models.MessageEntry;
import ps.billyphan.chatsdk.utils.PackageAnalyze;

@SuppressWarnings("all")
public class ChatConversation {
    private Map<String, Map<String, MessageEntry>> mConversations = new HashMap<>();

    public MessageEntry push(Message message) {
        String pair = PackageAnalyze.getChatPair(message);
        MessageEntry messageEntry = new MessageEntry(message);
        Map<String, MessageEntry> messageEntries = getAtPair(message);
        if (messageEntries == null) {
            messageEntries = new HashMap<>();
            mConversations.put(pair, messageEntries);
        }
        messageEntries.put(message.getStanzaId(), messageEntry);
        return messageEntry;
    }

    public void push(MessageEntry message) {
        String pair = PackageAnalyze.getChatPair(message);
        Map<String, MessageEntry> messageEntries = getAtPair(message);
        if (messageEntries == null) {
            messageEntries = new HashMap<>();
            mConversations.put(pair, messageEntries);
        }
        messageEntries.put(message.getId(), message);
    }

    private Map<String, MessageEntry> getAtPair(Message message) {
        return PackageAnalyze.getAtPair(mConversations, message);
    }

    private Map<String, MessageEntry> getAtPair(MessageEntry message) {
        return PackageAnalyze.getAtPair(mConversations, message);
    }

    public MessageEntry pop(Message message) {
        Map<String, MessageEntry> entry = getAtPair(message);
        MessageEntry messageEntry = entry.get(message.getStanzaId());
        entry.remove(message.getStanzaId());
        return messageEntry;
    }

    public int getUnreadSizeOfPair(MessageEntry message) {
        Map<String, MessageEntry> data = getAtPair(message);
        if (data == null) return 0;
        return data.size();
    }

    public List<MessageEntry> getMessages(String id1, String id2) {
        List<MessageEntry> messageEntries = new ArrayList<>();
        Map<String, MessageEntry> data = PackageAnalyze.getAtPair(mConversations, id1, id2);
        if (data != null) {
            for (MessageEntry messageEntry : data.values()) {
                messageEntries.add(messageEntry);
            }
        }
        return messageEntries;
    }

    public void addOrUpdate(MessageEntry oldMessage) {
        String pair = PackageAnalyze.getChatPair(oldMessage);
        Map<String, MessageEntry> data = getAtPair(oldMessage);
        if (data == null) {
            data = new HashMap<>();
            mConversations.put(pair, data);
        }
        data.put(oldMessage.getId(), oldMessage);
    }
}
