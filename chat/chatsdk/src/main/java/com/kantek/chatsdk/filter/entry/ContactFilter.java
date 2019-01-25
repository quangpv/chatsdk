package com.kantek.chatsdk.filter.entry;

import com.kantek.chatsdk.models.Contact;

public class ContactFilter implements ChatFilter<Contact> {
    private final boolean mPrivate;

    public ContactFilter(boolean isPrivate) {
        mPrivate = isPrivate;
    }

    @Override
    public boolean accept(Contact contact) {
        return true;
    }
}
