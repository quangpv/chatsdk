package ps.billyphan.chatsdk.models;

import java.io.Serializable;

import ps.billyphan.chatsdk.datasource.ChatDataSource;
import ps.billyphan.chatsdk.xmpp.XMPPClient;

public class FriendContact extends Contact implements Serializable {

    public FriendContact(String contactId, String myId, ChatDataSource chatDataSource) {
        super(contactId, myId, chatDataSource);
    }
}
