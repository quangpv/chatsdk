package com.kantek.chatsdk.models;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;

import com.kantek.chatsdk.filter.StateFilter;

public class StateEntry extends MessageEntry {
    private ChatState mState;

    public StateEntry(Message message) {
        super(message);
        setState(message);
    }

    public ChatState getState() {
        return mState;
    }

    public boolean isTyping() {
        return mState == ChatState.composing;
    }

    public void setState(Message message) {
        mState = ((ChatStateExtension) message.getExtension(StateFilter.NAMESPACE)).getChatState();
    }
}
