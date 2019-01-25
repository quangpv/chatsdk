package com.kantek.chatsdk.models;

import java.io.Serializable;

import com.kantek.chatsdk.datasource.ChatDataSource;

public class GroupContact extends Contact implements Serializable {

    public GroupContact(String contactId, String myId, ChatDataSource chatDataSource) {
        super(contactId, myId, chatDataSource);
    }
}
