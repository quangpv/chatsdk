package ps.billyphan.chatsdk;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import ps.billyphan.chatsdk.models.PrivateChat;

public class ChatActivity extends AppCompatActivity {
    private PrivateChat mChat;
    private ChatAdapter mAdapter;
    private View btnSend;
    private EditText edtMessage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mAdapter = new ChatAdapter(findViewById(R.id.recvChat));
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        mChat = XMPPClient.getInstance().getPrivateChat(this, "quangpv1");

        btnSend.setOnClickListener(v -> {
            String message = edtMessage.getText().toString();
            if (!message.isEmpty()) {
                mChat.send(message);
                edtMessage.setText("");
            }
        });

        mChat.setOnMessageReceivedListener(message -> mAdapter.add(message));
        mChat.setOnReceiptListener(message -> mAdapter.addOrUpdate(message));
        mChat.setOnTypingListener(message -> mAdapter.typing(message));
        mChat.setOnMessageLoadedListener(messages -> mAdapter.addAll(messages));
        mChat.start();
    }
}
