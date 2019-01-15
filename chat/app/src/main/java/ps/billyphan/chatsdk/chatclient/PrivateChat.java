package ps.billyphan.chatsdk.chatclient;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.packet.Message;

import ps.billyphan.chatsdk.datasource.ChatDataSource;
import ps.billyphan.chatsdk.utils.JidFormatter;

public class PrivateChat extends ChatClient {
    private final Chat mChat;

    public PrivateChat(ChatDataSource dataSource, String withUserId) {
        super(dataSource, withUserId);
        mChat = ChatManager.getInstanceFor(connection).chatWith(JidFormatter.jid(withUserId));
    }

    @Override
    protected void onStart() {
        super.onStart();
        active();
    }

    @Override
    protected void onStop() {
        super.onStop();
        inactive();
    }

    @Override
    protected void doSend(Message message) throws Exception {
        message.setType(Message.Type.chat);
        mChat.send(message);
    }
}
