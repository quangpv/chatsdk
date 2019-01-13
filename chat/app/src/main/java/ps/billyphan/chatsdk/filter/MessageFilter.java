package ps.billyphan.chatsdk.filter;

import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;

import ps.billyphan.chatsdk.models.MessageEntry;

public abstract class MessageFilter implements ChatFilter<MessageEntry> {
    public static final StanzaFilter PRIVATE_OR_GROUP =
            new AndFilter(StanzaTypeFilter.MESSAGE, new OrFilter(
                    MessageTypeFilter.CHAT,
                    MessageTypeFilter.GROUPCHAT
            ));

    private final String mFrom;
    private final String mTo;

    public MessageFilter(String from, String to) {
        mFrom = from;
        mTo = to;
    }

    public String getFrom() {
        return mFrom;
    }

    public String getTo() {
        return mTo;
    }
}
