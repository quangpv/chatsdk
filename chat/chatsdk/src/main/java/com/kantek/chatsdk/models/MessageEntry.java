package com.kantek.chatsdk.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import org.jivesoftware.smack.packet.Message;

import com.kantek.chatsdk.utils.PackageAnalyze;

@Entity
public class MessageEntry extends Observable {
    @PrimaryKey
    @NonNull
    private String mId;
    private String mFromId;
    private String mToId;
    private String mBody;
    private long mTimeReceived;
    private int mReceipt = ReceiptState.NONE;
    private boolean mFriendMessage = true;

    public MessageEntry() {
    }

    public MessageEntry(Message message) {
        mId = message.getStanzaId();
        mFromId = PackageAnalyze.getFromId(message);
        mToId = PackageAnalyze.getToId(message);
        mBody = message.getBody();
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

    public long getTimeReceived() {
        return mTimeReceived;
    }

    public void setId(String id) {
        mId = id;
    }

    public void setFromId(String fromId) {
        mFromId = fromId;
    }

    public void setToId(String toId) {
        mToId = toId;
    }

    public void setBody(String body) {
        mBody = body;
    }

    public void setTimeReceived(long timeReceived) {
        mTimeReceived = timeReceived;
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

    public boolean isFriendMessage() {
        return mFriendMessage;
    }

    public void setFriendMessage(boolean b) {
        mFriendMessage = b;
    }
}
