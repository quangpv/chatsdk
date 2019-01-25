package ps.billyphan.chatsdk;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.kantek.chatsdk.chatclient.ChatClient;
import com.kantek.chatsdk.listeners.OnNotifyTypingListener;
import com.kantek.chatsdk.models.Contact;
import com.kantek.chatsdk.xmpp.XMPPClient;

public class ChatActivity extends AppCompatActivity {
    private ChatClient mChat;
    private ChatAdapter mAdapter;
    private View btnSend;
    private EditText edtMessage;
    private Contact mContact;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mAdapter = new ChatAdapter(findViewById(R.id.recvChat));
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        mContact = (Contact) getIntent().getExtras().get("CONTACT");
        XMPPClient xmppClient = XMPPClient.getInstance();
        mChat = xmppClient.getChat(this, mContact);
        mChat.asLiveData().observe(this, mAdapter::submitList);
        setup();
    }

    private void setup() {
        edtMessage.addTextChangedListener(new OnNotifyTypingListener() {
            @Override
            protected void composing(String chatState) {
                mChat.notifyTyping(chatState);
            }
        });
        btnSend.setOnClickListener(v -> {
            String message = edtMessage.getText().toString();
            if (message.isEmpty()) return;
            mChat.send(message);
            edtMessage.setText("");
        });
        mChat.setOnTypingListener(message -> mAdapter.typing(message));
    }
}
