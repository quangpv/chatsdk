package ps.billyphan.chatsdk.models;

import java.io.Serializable;

import ps.billyphan.chatsdk.datasource.ChatDataSource;

public class GroupContact extends Contact implements Serializable {

    public GroupContact(String contactId, String myId, ChatDataSource chatDataSource) {
        super(contactId, myId, chatDataSource);
    }
}
