package ps.billyphan.chatsdk.xmpp;

import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.support.v4.util.Consumer;
import android.util.Log;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.HashMap;
import java.util.Map;

import ps.billyphan.chatsdk.ChatExecutors;
import ps.billyphan.chatsdk.chatclient.ChatClient;
import ps.billyphan.chatsdk.chatclient.GroupChat;
import ps.billyphan.chatsdk.chatclient.PrivateChat;
import ps.billyphan.chatsdk.datasource.ChatDataSource;
import ps.billyphan.chatsdk.models.Contact;
import ps.billyphan.chatsdk.models.FriendContact;
import ps.billyphan.chatsdk.utils.JidFormatter;

public class XMPPClient {
    private static final int PORT = 5222;
    public static final String HOST = "xmpp.jp";
    public static final String GROUP = "conference.xmpp.jp";
    private static final CharSequence RESOURCE = "amazon";
    private static XMPPClient sInstance;
    private String mUserName = "quangpv1";
    private String mPassword = "abc12345";
    private XMPPChatConnection mConnection;
    private ChatDataSource mChatDataSource;
    private Map<String, ChatClient> mChat = new HashMap<>();

    static {
        ExtensionLoader.load();
    }

    public static void init(Context context) {
        sInstance = new XMPPClient(context);
    }

    private XMPPClient(Context context) {
        try {
            mChatDataSource = new ChatDataSource(context);
            mConnection = new XMPPChatConnection(XMPPTCPConnectionConfiguration.builder()
                    .setXmppDomain(JidFormatter.domain(HOST))
                    .setHost(HOST)
                    .setResource(RESOURCE)
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.required)
                    .setSendPresence(false)
                    .setKeystoreType(null)
                    .setCompressionEnabled(true)
                    .build());
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        mChatDataSource.setConnection(mConnection);
    }

    public static synchronized XMPPClient getInstance() {
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
        mConnection.registryOnNotifyReadListener(message -> ChatExecutors.inBackground(() -> mChatDataSource.updateRead(message)));
        mConnection.registryInComingListener(message -> ChatExecutors.inBackground(() -> mChatDataSource.addInComing(message)));
        mConnection.registryOutGoingListener(message -> ChatExecutors.inBackground(() -> mChatDataSource.addOutGoing(message)));
        mConnection.registryReceiptListener((message, state) -> ChatExecutors.inBackground(() -> mChatDataSource.updateReceipt(message, state)));
        mConnection.registryStateListener(message -> ChatExecutors.inBackground(() -> mChatDataSource.updateState(message)));
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }

    public ChatClient getChat(LifecycleOwner owner, Contact contact) {
        String id = contact.contactId;
        ChatClient chat;
        if (mChat.containsKey(id)) {
            chat = mChat.get(id);
        } else {
            if (contact instanceof FriendContact)
                chat = new PrivateChat(mChatDataSource, id);
            else chat = new GroupChat(mChatDataSource, id);
            mChat.put(id, chat);
        }
        assert chat != null;
        chat.setLifecycle(owner.getLifecycle());
        return chat;
    }

    public void disconnect() {
        Log.e("DEBUG", "disconnect");
        ChatExecutors.inBackground(() -> mConnection.disconnect());
    }

    public String getMyId() {
        return mUserName;
    }

    public ContactClient getContact() {
        return new ContactClient(mUserName, mConnection, mChatDataSource);
    }
}
