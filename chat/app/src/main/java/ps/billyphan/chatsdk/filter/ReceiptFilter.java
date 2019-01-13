package ps.billyphan.chatsdk.filter;

import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;

import ps.billyphan.chatsdk.extension.ReadReceipt;

public class ReceiptFilter implements StanzaFilter {
    public static final StanzaFilter RECEIVED = new AndFilter(MessageFilter.PRIVATE_OR_GROUP, new ReceiptFilter(DeliveryReceipt.class));
    public static final StanzaFilter READ = new AndFilter(MessageFilter.PRIVATE_OR_GROUP, new ReceiptFilter(ReadReceipt.class));

    private final Class<?> mType;

    public ReceiptFilter(Class<?> type) {
        mType = type;
    }

    @Override
    public boolean accept(Stanza stanza) {
        return StanzaTypeFilter.MESSAGE.accept(stanza)
                && checkExtension(stanza);
    }

    private boolean checkExtension(Stanza stanza) {
        if (mType.isInstance(DeliveryReceipt.class))
            return stanza.hasExtension(DeliveryReceipt.ELEMENT, DeliveryReceipt.NAMESPACE);
        if (mType.isInstance(ReadReceipt.class))
            return stanza.hasExtension(DeliveryReceipt.ELEMENT, DeliveryReceipt.NAMESPACE);
        return false;
    }
}
