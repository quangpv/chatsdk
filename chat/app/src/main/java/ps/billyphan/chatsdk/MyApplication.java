package ps.billyphan.chatsdk;

import android.app.Application;

import com.kantek.chatsdk.xmpp.XMPPClient;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        XMPPClient.init(this);
    }
}
