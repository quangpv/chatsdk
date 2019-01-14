package ps.billyphan.chatsdk;

import android.arch.lifecycle.MutableLiveData;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import ps.billyphan.chatsdk.chatclient.PrivateChat;
import ps.billyphan.chatsdk.models.Contact;
import ps.billyphan.chatsdk.models.MessageEntry;
import ps.billyphan.chatsdk.xmpp.XMPPClient;

public class ChatActivity extends AppCompatActivity {
    private PrivateChat mChat;
    private ChatAdapter mAdapter;
    private View btnSend;
    private EditText edtMessage;
    private Contact mContact;
    private MutableLiveData<MessageEntry> mMessageReceived = new MutableLiveData<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mAdapter = new ChatAdapter(findViewById(R.id.recvChat));
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        mContact = (Contact) getIntent().getExtras().get("CONTACT");
        mChat = XMPPClient.getInstance().getPrivateChat(this, mContact.name);
        mMessageReceived.observe(this, messageEntry -> mChat.notifyRead());
        setup();
    }

    private void setup() {
        edtMessage.addTextChangedListener(new OnNotifyTypingListener() {
            @Override
            protected void composing(boolean isComposing) {
                mChat.notifyTyping(isComposing);
            }
        });
        btnSend.setOnClickListener(v -> {
            String message = edtMessage.getText().toString();
            if (!message.isEmpty()) {
                mChat.send(message);
                edtMessage.setText("");
            }
        });

        mChat.setOnReceivedListener(message -> {
            mAdapter.add(message);
            mMessageReceived.setValue(message);
        });
        mChat.setOnSendingListener(message -> mAdapter.add(message));
        mChat.setOnTypingListener(message -> mAdapter.typing(message));
        mChat.setOnLoadedListener(messages -> mAdapter.addAll(messages));
    }
}
