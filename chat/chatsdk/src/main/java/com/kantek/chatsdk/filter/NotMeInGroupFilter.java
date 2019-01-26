package com.kantek.chatsdk.filter;

import com.kantek.chatsdk.utils.PackageAnalyze;

import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;

public class NotMeInGroupFilter implements StanzaFilter {
    @Override
    public boolean accept(Stanza stanza) {
        Message message = (Message) stanza;
        String fromId, toId;
        if (PackageAnalyze.isFromGroup(message.getFrom())) {
            fromId = PackageAnalyze.getResourceId(message.getFrom());
            toId = PackageAnalyze.getToId(message);
        } else {
            fromId = PackageAnalyze.getFromId(message);
            toId = PackageAnalyze.getResourceId(message.getTo());
        }

        assert fromId != null;
        return !fromId.equalsIgnoreCase(toId);
    }
}
