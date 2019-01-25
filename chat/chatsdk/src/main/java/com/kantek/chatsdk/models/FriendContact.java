package com.kantek.chatsdk.models;

import java.io.Serializable;

import com.kantek.chatsdk.datasource.ChatDataSource;
import com.kantek.chatsdk.xmpp.XMPPClient;

public class FriendContact extends Contact implements Serializable {

    public FriendContact(String contactId, String myId, ChatDataSource chatDataSource) {
        super(contactId, myId, chatDataSource);
    }
}
