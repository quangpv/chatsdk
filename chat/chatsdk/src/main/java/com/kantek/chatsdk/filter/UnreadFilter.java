package com.kantek.chatsdk.filter;

import com.kantek.chatsdk.filter.entry.MessageFilter;
import com.kantek.chatsdk.models.MessageEntry;

public class UnreadFilter extends MessageFilter {
    public UnreadFilter(String myId, String withId) {
        super(myId, withId);
    }

    @Override
    public boolean accept(MessageEntry messageEntry) {
        return (messageEntry.getFromId().equalsIgnoreCase(getFrom())
                && messageEntry.getToId().equalsIgnoreCase(getTo()))
                || (messageEntry.getFromId().equalsIgnoreCase(getTo())
                && messageEntry.getToId().equalsIgnoreCase(getFrom()));
    }
}
