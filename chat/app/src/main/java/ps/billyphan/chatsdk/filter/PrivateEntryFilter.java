package ps.billyphan.chatsdk.filter;

import ps.billyphan.chatsdk.models.MessageEntry;

public class PrivateEntryFilter extends MessageFilter {

    public PrivateEntryFilter(String from, String to) {
        super(from, to);
    }

    @Override
    public boolean accept(MessageEntry messageEntry) {
        return messageEntry.getFromId().equals(getFrom())
                && messageEntry.getToId().equals(getTo());
    }
}
