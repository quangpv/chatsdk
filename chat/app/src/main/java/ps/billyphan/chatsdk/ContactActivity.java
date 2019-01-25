package ps.billyphan.chatsdk;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.kantek.chatsdk.models.Contact;
import com.kantek.chatsdk.xmpp.ContactClient;
import com.kantek.chatsdk.xmpp.XMPPClient;


public class ContactActivity extends AppCompatActivity {
    private View mBtnAdd;
    private EditText mEdtContact;
    private ContactAdapter mContactAdapter;
    private XMPPClient mXMPPClient;
    private ContactClient mContactClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        mBtnAdd = findViewById(R.id.btnAddContact);
        mEdtContact = findViewById(R.id.edtContact);
        mContactAdapter = new ContactAdapter(findViewById(R.id.recvContact));
        mXMPPClient = XMPPClient.getInstance();
        mContactClient = mXMPPClient.getContact(this);
        mBtnAdd.setOnClickListener(v -> addContactIfCan());
        mContactAdapter.setOnItemClickListener(this::openChat);
        mContactClient.asLiveData().observe(this, mContactAdapter::submitList);
    }

    private void openChat(Contact contact) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("CONTACT", contact);
        startActivity(intent);
    }

    private void addContactIfCan() {
        String text = mEdtContact.getText().toString();
        if (text.length() == 0) return;
        mContactClient.newFriend(text);
        mEdtContact.setText("");
    }

    @Override
    protected void onDestroy() {
        mXMPPClient.disconnect();
        super.onDestroy();
    }
}
