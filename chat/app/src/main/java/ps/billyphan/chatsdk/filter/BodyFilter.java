package ps.billyphan.chatsdk.filter;

import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;

public class BodyFilter implements StanzaFilter {
    @Override
    public boolean accept(Stanza stanza) {
        Message message = (Message) stanza;
        return !message.getBodies().isEmpty() && !message.getBody().isEmpty();
    }
}
