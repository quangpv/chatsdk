package ps.billyphan.chatsdk.xmpp;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.v4.util.Consumer;
import android.util.Log;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.HashMap;
import java.util.Map;

import ps.billyphan.chatsdk.ChatExecutors;
import ps.billyphan.chatsdk.chatclient.PrivateChat;
import ps.billyphan.chatsdk.datasource.ChatDataSource;
import ps.billyphan.chatsdk.utils.JidFormatter;

public class XMPPClient {
    private static final int PORT = 5222;
    public static final String HOST = "xmpp.jp";
    private static final CharSequence RESOURCE = "amazon";
    private static XMPPClient sInstance;
    private String mUserName = "quangpv1";
    private String mPassword = "abc12345";
    private XMPPChatConnection mConnection;
    private ChatDataSource mChatDataSource = new ChatDataSource();
    private Map<String, PrivateChat> mChatCache = new HashMap<>();

    static {
        ExtensionLoader.load();
    }

    public XMPPClient() {
        try {
            mConnection = new XMPPChatConnection(XMPPTCPConnectionConfiguration.builder()
                    .setPort(PORT)
                    .setCompressionEnabled(false)
                    .setHost(HOST)
                    .setXmppDomain(JidFormatter.domain(HOST))
                    .setResource(RESOURCE)
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.required)
                    .setSendPresence(false)
                    .build());
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        mChatDataSource.setConnection(mConnection);
    }

    public static synchronized XMPPClient getInstance() {
        if (sInstance == null) sInstance = new XMPPClient();
        return sInstance;
    }

    public void connect(Consumer<Boolean> onSuccess) {
        ChatExecutors.inBackground(() -> {
            try {
                registries();
                mConnection.connect(mUserName, mPassword, resume -> {
                    mChatDataSource.fetchOfflineMessages();
                    mConnection.notifyPresence(Presence.Type.available);
                    ChatExecutors.onMainThread(() -> onSuccess.accept(true));
                });
            } catch (Exception e) {
                e.printStackTrace();
                ChatExecutors.onMainThread(() -> onSuccess.accept(false));
            }
        });
    }

    private void registries() {
        mConnection.registryOnNotifyReadListener(message -> mChatDataSource.updateRead(message));
        mConnection.registryInComingListener(message -> mChatDataSource.addInComing(message));
        mConnection.registryOutGoingListener(message -> mChatDataSource.addOutGoing(message));
        mConnection.registryReceiptListener((message, state) -> mChatDataSource.updateReceipt(message, state));
        mConnection.registryStateListener(message -> mChatDataSource.updateState(message));
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }

    public PrivateChat getPrivateChat(LifecycleOwner owner, String userId) {
        PrivateChat privateChat;
        if (mChatCache.containsKey(userId)) {
            privateChat = mChatCache.get(userId);
        } else {
            privateChat = new PrivateChat(mChatDataSource, userId);
            mChatCache.put(userId, privateChat);
        }
        assert privateChat != null;
        privateChat.setLifecycle(owner.getLifecycle());
        return privateChat;
    }

    public void disconnect() {
        Log.e("DEBUG", "disconnect");
        ChatExecutors.inBackground(() -> {
            mConnection.unregisterAll();
            mConnection.notifyGone();
            mConnection.notifyPresence(Presence.Type.unavailable);
            mConnection.disconnect();
            sInstance = null;
        });
    }

    public ChatDataSource getDataSource() {
        return mChatDataSource;
    }

    public String getMyId() {
        return mUserName;
    }
}
