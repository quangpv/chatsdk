package com.kantek.chatsdk.listeners;

import org.jivesoftware.smack.packet.Message;

public interface OnReceiptListener {
    void onReceived(Message message, int state);
}
