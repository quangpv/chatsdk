package com.kantek.chatsdk.listeners;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;

public interface OnMessageListener extends StanzaListener {
    @Override
    default void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException, SmackException.NotLoggedInException {
        if (packet instanceof Message) {
            processMessage((Message) packet);
        }
    }

    void processMessage(Message message);
}
