package com.kantek.chatsdk.filter.entry;

import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;

import com.kantek.chatsdk.filter.BodyFilter;
import com.kantek.chatsdk.filter.NotMeInGroupFilter;
import com.kantek.chatsdk.models.MessageEntry;

public class MessageFilter implements ChatFilter<MessageEntry> {
    public static final StanzaFilter PRIVATE =
            new AndFilter(StanzaTypeFilter.MESSAGE,
                    MessageTypeFilter.CHAT
            );

    public static final StanzaFilter GROUP =
            new AndFilter(StanzaTypeFilter.MESSAGE,
                    MessageTypeFilter.GROUPCHAT
            );

    public static final StanzaFilter GROUP_EXCEPT_ME =
            new AndFilter(StanzaTypeFilter.MESSAGE, new AndFilter(
                    MessageTypeFilter.GROUPCHAT,
                    new NotMeInGroupFilter()
            ));

    public static final StanzaFilter PRIVATE_OR_GROUP_EXCEPT_ME =
            new AndFilter(StanzaTypeFilter.MESSAGE,
                    new OrFilter(
                            PRIVATE,
                            GROUP_EXCEPT_ME
                    )
            );

    public static final StanzaFilter PRIVATE_BODY =
            new AndFilter(
                    StanzaTypeFilter.MESSAGE,
                    MessageTypeFilter.CHAT,
                    new BodyFilter()
            );

    public static final StanzaFilter GROUP_BODY =
            new AndFilter(
                    StanzaTypeFilter.MESSAGE,
                    MessageTypeFilter.GROUPCHAT,
                    new BodyFilter()
            );

    private final String mFrom;
    private final String mTo;

    public MessageFilter(String from, String to) {
        mFrom = from;
        mTo = to;
    }

    public String getFrom() {
        return mFrom;
    }

    public String getTo() {
        return mTo;
    }

    @Override
    public boolean accept(MessageEntry messageEntry) {
        return messageEntry.getFromId().equalsIgnoreCase(getFrom())
                && messageEntry.getToId().equalsIgnoreCase(getTo());
    }
}
