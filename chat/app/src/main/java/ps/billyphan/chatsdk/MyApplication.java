package ps.billyphan.chatsdk;

import android.app.Application;

import ps.billyphan.chatsdk.xmpp.XMPPClient;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        XMPPClient.init(this);
    }
}
