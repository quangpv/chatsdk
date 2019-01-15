package ps.billyphan.chatsdk;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import ps.billyphan.chatsdk.models.Contact;
import ps.billyphan.chatsdk.xmpp.ContactClient;
import ps.billyphan.chatsdk.xmpp.XMPPClient;

public class ContactActivity extends AppCompatActivity {
    private View mBtnAdd;
    private EditText mEdtContact;
    private ContactAdapter mContactAdapter;
    private XMPPClient mXMPPClient;
    private ContactClient mContact;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        mBtnAdd = findViewById(R.id.btnAddContact);
        mEdtContact = findViewById(R.id.edtContact);
        mContactAdapter = new ContactAdapter(findViewById(R.id.recvContact));
        mXMPPClient = XMPPClient.getInstance();
        mContact = mXMPPClient.getContact();
        mBtnAdd.setOnClickListener(v -> addContactIfCan());
        mContactAdapter.setOnItemClickListener(this::openChat);
        mContact.loadFriends(contacts -> mContactAdapter.addAll(contacts));
    }

    private void openChat(Contact contact) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("CONTACT", contact);
        startActivity(intent);
    }

    private void addContactIfCan() {
        String text = mEdtContact.getText().toString();
        if (text.length() == 0) return;
        mContactAdapter.add(mContact.newFriend(text));
        mEdtContact.setText("");
    }

    @Override
    protected void onDestroy() {
        mXMPPClient.disconnect();
        super.onDestroy();
    }
}
