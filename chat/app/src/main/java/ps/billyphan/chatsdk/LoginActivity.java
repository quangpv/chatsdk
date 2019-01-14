package ps.billyphan.chatsdk;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import ps.billyphan.chatsdk.xmpp.XMPPClient;

public class LoginActivity extends AppCompatActivity {
    private EditText edtUserName;
    private EditText edtPassword;
    private XMPPClient mXMPPClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        edtUserName = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        View btnLogin = findViewById(R.id.btnLogin);
        mXMPPClient = XMPPClient.getInstance();
        btnLogin.setOnClickListener(v -> login());
    }

    private void login() {
        mXMPPClient.setUserName(edtUserName.getText().toString());
        mXMPPClient.connect(isLogged -> {
            if (isLogged) openContact();
            else showError();
        });
    }

    private void openContact() {
        Intent intent = new Intent(this, ContactActivity.class);
        startActivity(intent);
    }

    private void showError() {
        Toast.makeText(this, "Error connection", Toast.LENGTH_SHORT).show();
    }
}
