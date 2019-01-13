package ps.billyphan.chatsdk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private XMPPClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mClient = XMPPClient.getInstance();
        mClient.setUserName("quangpv");
        mClient.connect(this::openChat);
        findViewById(R.id.btnChat).setOnClickListener(view -> openChat());
    }

    private void openChat() {
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        mClient.disconnect();
        super.onDestroy();
    }
}
