package com.kantek.chatsdk.chatclient;

import com.kantek.chatsdk.datasource.ChatDataSource;
import com.kantek.chatsdk.models.ChatParameters;
import com.kantek.chatsdk.utils.JidFormatter;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.packet.Message;

public class PrivateChat extends ChatClient {
    private final Chat mChat;

    public PrivateChat(ChatDataSource dataSource, ChatParameters chatParameters) {
        super(dataSource, chatParameters);
        mChat = ChatManager.getInstanceFor(connection).chatWith(JidFormatter.jid(chatParameters.getWithId()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        active();
    }

    @Override
    protected void onStop() {
        super.onStop();
        inactive();
    }

    @Override
    protected void doSend(Message message) throws Exception {
        message.setType(Message.Type.chat);
        mChat.send(message);
    }
}
