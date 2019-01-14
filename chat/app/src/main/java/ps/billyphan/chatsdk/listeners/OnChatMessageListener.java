package ps.billyphan.chatsdk.listeners;

import org.jivesoftware.smack.packet.Message;

public interface OnChatMessageListener extends OnMessageListener {
    @Override
    default void processMessage(Message packet) {
        if (packet.getBodies().isEmpty() || packet.getBody().isEmpty()) return;
        processChatMessage(packet);
    }

    void processChatMessage(Message packet);
}
