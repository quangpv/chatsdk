package com.kantek.chatsdk.chatclient;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.Affiliate;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatException;
import org.jivesoftware.smackx.muc.MultiUserChatManager;

import java.util.List;

import com.kantek.chatsdk.datasource.ChatDataSource;
import com.kantek.chatsdk.utils.JidFormatter;

public class GroupChat extends ChatClient {
    private final MultiUserChat mChat;

    public GroupChat(ChatDataSource dataSource, String id) {
        super(dataSource, id);
        mChat = MultiUserChatManager.getInstanceFor(dataSource.getConnection())
                .getMultiUserChat(JidFormatter.groupJid(getWithId()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        joinRoom();
    }

    @Override
    protected void onStop() {
        super.onStop();
        leaveRoom();
    }

    @Override
    protected void doSend(Message message) throws Exception {
        message.setType(Message.Type.groupchat);
        mChat.sendMessage(message);
    }

    public boolean leaveRoom() {
        try {
            List<Affiliate> members = mChat.getMembers();
            if (members.size() == 1) return destroyRoom();
            mChat.leave();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean destroyRoom() {
        try {
            mChat.destroy("destroyed", null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void joinRoom() {
        if (mChat.isJoined()) return;
        try {
            mChat.join(JidFormatter.resource(getMyId()));
        } catch (SmackException.NoResponseException
                | XMPPException.XMPPErrorException
                | SmackException.NotConnectedException
                | InterruptedException
                | MultiUserChatException.NotAMucServiceException e) {
            e.printStackTrace();
        }
    }
}
