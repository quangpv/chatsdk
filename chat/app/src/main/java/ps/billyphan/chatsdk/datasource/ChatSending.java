package ps.billyphan.chatsdk.datasource;

import org.jivesoftware.smack.packet.Message;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ps.billyphan.chatsdk.models.MessageEntry;
import ps.billyphan.chatsdk.models.PairHashMap;
import ps.billyphan.chatsdk.models.ReceiptState;
import ps.billyphan.chatsdk.utils.PackageAnalyze;

public class ChatSending {
    private PairHashMap<HashMap<String, MessageEntry>> mPair = new PairHashMap<>();

    public MessageEntry get(Message message) {
        return getOrCreate(message).get(message.getStanzaId());
    }

    private HashMap<String, MessageEntry> getOrCreate(Message message) {
        return mPair.getOrCreate(PackageAnalyze.getFromId(message),
                PackageAnalyze.getToId(message), HashMap::new);
    }

    public MessageEntry push(Message message) {
        MessageEntry messageEntry = new MessageEntry(message);
        getOrCreate(message).put(message.getStanzaId(), messageEntry);
        return messageEntry;
    }

    public void remove(Message message) {
        getOrCreate(message).remove(message.getStanzaId());
    }

    public MessageEntry updateReceipt(Message message, int state) {
        MessageEntry messageEntry = get(message);
        messageEntry.setReceipt(state);
        if (messageEntry.isRead()) remove(message);
        messageEntry.notifyChanged();
        return messageEntry;
    }

    public Collection<MessageEntry> updateRead(Message message) {
        Map<String, MessageEntry> messageEntries = getOrCreate(message);
        for (MessageEntry messageEntry : messageEntries.values()) {
            messageEntry.setReceipt(ReceiptState.READ);
            messageEntry.notifyChanged();
        }
        mPair.remove(PackageAnalyze.getFromId(message), PackageAnalyze.getToId(message));
        return messageEntries.values();
    }
}
