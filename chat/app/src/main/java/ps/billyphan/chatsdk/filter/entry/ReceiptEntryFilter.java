package ps.billyphan.chatsdk.filter.entry;

import ps.billyphan.chatsdk.models.MessageEntry;

public class ReceiptEntryFilter extends PrivateEntryFilter {

    public ReceiptEntryFilter(String from, String to) {
        super(from, to);
    }

    @Override
    public boolean accept(MessageEntry messageEntry) {
        return messageEntry.getToId().equals(getFrom());
    }
}
