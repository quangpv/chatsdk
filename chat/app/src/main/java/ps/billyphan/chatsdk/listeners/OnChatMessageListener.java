package ps.billyphan.chatsdk.listeners;

import org.jivesoftware.smack.packet.Message;

public interface OnChatMessageListener extends OnMessageListener {

    @Override
    default void processMessage(Message message) {
        if (message.getBodies().isEmpty() || message.getBody().isEmpty()) return;
        processChatMessage(message);
    }

    void processChatMessage(Message message);
}
