package ps.billyphan.chatsdk.filter;

import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Stanza;

public class StateFilter implements StanzaFilter {
    public static final String NAMESPACE = "http://jabber.org/protocol/chatstates";
    private final StanzaFilter mMessageFilter;

    public StateFilter() {
        mMessageFilter = MessageFilter.PRIVATE_OR_GROUP;
    }

    @Override
    public boolean accept(Stanza stanza) {
        return mMessageFilter.accept(stanza) && stanza.hasExtension(NAMESPACE);
    }
}
