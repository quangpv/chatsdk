package ps.billyphan.chatsdk.filter;

import ps.billyphan.chatsdk.filter.entry.MessageFilter;
import ps.billyphan.chatsdk.models.MessageEntry;

public class UnreadFilter extends MessageFilter {
    public UnreadFilter(String myId, String withId) {
        super(myId, withId);
    }

    @Override
    public boolean accept(MessageEntry messageEntry) {
        return (messageEntry.getFromId().equals(getFrom())
                && messageEntry.getToId().equals(getTo()))
                || (messageEntry.getFromId().equals(getTo())
                && messageEntry.getToId().equals(getFrom()));
    }
}
