package ps.billyphan.chatsdk.models;

import org.jivesoftware.smack.packet.Message;

import java.util.Set;

import ps.billyphan.chatsdk.utils.PackageAnalyze;

public class MessageEntry extends Observable {
    private final String mId;
    private final String mFromId;
    private final String mToId;
    private final String mBody;
    private final long mTimeReceived;
    private final Set<Message.Body> mBodies;
    private int mReceipt = ReceiptState.NONE;
    private boolean mSendFromFriend = true;

    public MessageEntry(Message message) {
        mId = message.getStanzaId();
        mFromId = PackageAnalyze.getFromId(message);
        mToId = PackageAnalyze.getToId(message);
        mBody = message.getBody();
        mBodies = message.getBodies();
        mTimeReceived = System.currentTimeMillis();
    }

    public void setReceipt(int sending) {
        mReceipt = sending;
    }

    public boolean isRead() {
        return mReceipt == ReceiptState.READ;
    }

    public String getId() {
        return mId;
    }

    public String getFromId() {
        return mFromId;
    }

    public String getToId() {
        return mToId;
    }

    public String getBody() {
        return mBody;
    }

    public boolean isTypeSend() {
        return mReceipt == ReceiptState.SENDING || mReceipt == ReceiptState.SENT;
    }

    public Set<Message.Body> getBodies() {
        return mBodies;
    }

    public int getReceipt() {
        return mReceipt;
    }

    public String getReceiptText() {
        switch (mReceipt) {
            case ReceiptState.READ:
                return "Read";
            case ReceiptState.RECEIVED:
                return "Received";
            case ReceiptState.SENDING:
                return "Sending";
            case ReceiptState.SENT:
                return "Sent";
            default:
                return "";
        }
    }

    public int compareTime(MessageEntry t1) {
        return mTimeReceived - t1.mTimeReceived > 0 ? 1 : -1;
    }

    public boolean isSendFromFriend() {
        return mSendFromFriend;
    }

    public void setSendFromFriend(boolean b) {
        mSendFromFriend = b;
    }
}
