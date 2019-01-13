package ps.billyphan.chatsdk.filter;

import ps.billyphan.chatsdk.models.MessageEntry;

public class ReceiptEntryFilter extends PrivateEntryFilter {

    public ReceiptEntryFilter(String from, String to) {
        super(from, to);
    }

    @Override
    public boolean accept(MessageEntry messageEntry) {
        if (messageEntry.isTypeSend())
            return messageEntry.getToId().equals(getFrom());
        return super.accept(messageEntry);
    }
}
