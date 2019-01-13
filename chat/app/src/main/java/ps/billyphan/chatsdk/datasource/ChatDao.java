package ps.billyphan.chatsdk.datasource;

import java.util.List;

import ps.billyphan.chatsdk.ChatConversation;
import ps.billyphan.chatsdk.models.MessageEntry;

public class ChatDao {
    private ChatConversation mChatConversation = new ChatConversation();

    public void addAll(List<MessageEntry> messages) {
        for (MessageEntry message : messages) {
            mChatConversation.push(message);
        }
    }

    public void add(MessageEntry message) {
        mChatConversation.push(message);
    }

    public List<MessageEntry> getByPrivateChat(String me, String withId) {
        return mChatConversation.getMessages(me, withId);
    }

    public void addOrUpdate(MessageEntry oldMessage) {
        mChatConversation.addOrUpdate(oldMessage);
    }
}
