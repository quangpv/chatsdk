package com.kantek.chatsdk.filter.entry;

import org.jivesoftware.smackx.chatstates.ChatState;

import com.kantek.chatsdk.filter.entry.ChatFilter;
import com.kantek.chatsdk.models.StateEntry;

public class StateEntryFilter implements ChatFilter<StateEntry> {
    private final ChatState[] mState;

    public StateEntryFilter(ChatState... state) {
        mState = state;
    }

    @Override
    public boolean accept(StateEntry stateEntry) {
        for (ChatState chatState : mState) {
            if (chatState == stateEntry.getState()) return true;
        }
        return false;
    }
}
