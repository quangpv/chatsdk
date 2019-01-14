package ps.billyphan.chatsdk.datasource;

import java.util.List;
import java.util.Map;

import ps.billyphan.chatsdk.models.MessageEntry;

public class ChatArchived {
    private ChatConversation mChatConversation = new ChatConversation();

    public void addAll(List<MessageEntry> messages) {
        for (MessageEntry message : messages) {
            mChatConversation.push(message);
        }
    }

    public List<MessageEntry> getByPairChat(String me, String withId) {
        return mChatConversation.getMessages(me, withId);
    }

    public void save(MessageEntry messageEntry) {
        mChatConversation.push(messageEntry);
    }

    public void saves(Map<String, MessageEntry> messageEntryMap) {
        for (MessageEntry messageEntry : messageEntryMap.values()) {
            mChatConversation.push(messageEntry);
        }
    }
}
