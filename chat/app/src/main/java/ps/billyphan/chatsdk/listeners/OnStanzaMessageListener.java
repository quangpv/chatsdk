package ps.billyphan.chatsdk.listeners;

import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;

public interface OnStanzaMessageListener extends StanzaListener {
    @Override
    default void processStanza(Stanza packet) {
        if (packet instanceof Message) {
            Message message = (Message) packet;
            if (shouldAcceptMessage(message))
                processMessage(message);
        }
    }

    default boolean shouldAcceptMessage(Message message) {
        if (message.getBodies().isEmpty()) return false;
        return !message.getBody().isEmpty();
    }

    void processMessage(Message packet);
}
