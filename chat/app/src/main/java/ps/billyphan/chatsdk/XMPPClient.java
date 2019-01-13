package ps.billyphan.chatsdk;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.util.Log;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.address.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.commands.provider.AdHocCommandDataProvider;
import org.jivesoftware.smackx.disco.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.disco.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.iqlast.packet.LastActivity;
import org.jivesoftware.smackx.iqprivate.PrivateDataManager;
import org.jivesoftware.smackx.muc.packet.GroupChatInvitation;
import org.jivesoftware.smackx.muc.provider.MUCAdminProvider;
import org.jivesoftware.smackx.muc.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.muc.provider.MUCUserProvider;
import org.jivesoftware.smackx.offline.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.offline.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.ping.provider.PingProvider;
import org.jivesoftware.smackx.privacy.provider.PrivacyProvider;
import org.jivesoftware.smackx.pubsub.provider.EventProvider;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
import org.jivesoftware.smackx.search.UserSearch;
import org.jivesoftware.smackx.sharedgroups.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.time.provider.TimeProvider;
import org.jivesoftware.smackx.vcardtemp.provider.VCardProvider;
import org.jivesoftware.smackx.xdata.provider.DataFormProvider;

import java.util.HashMap;
import java.util.Map;

import ps.billyphan.chatsdk.datasource.ChatDataSource;
import ps.billyphan.chatsdk.models.PrivateChat;
import ps.billyphan.chatsdk.utils.JidFormatter;

public class XMPPClient {
    private static final int PORT = 5222;
    public static final String HOST = "xmpp.jp";
    private static final CharSequence RESOURCE = "amazon";
    private static XMPPClient sInstance;
    private String mUserName = "quangpv";
    private String mPassword = "abc12345";
    private XMPPChatConnection mConnection;

    static {
        // Private Data Storage
        ProviderManager.addIQProvider("query", "jabber:iq:private", new PrivateDataManager.PrivateDataIQProvider());

        ProviderManager.addIQProvider("ping", "urn:xmpp:ping",
                new PingProvider());

        ProviderManager.addExtensionProvider("request", "urn:xmpp:receipts",
                new DeliveryReceiptRequest.Provider());

        ProviderManager.addIQProvider("query", "jabber:iq:time", new TimeProvider());

        // Message Events
        ProviderManager.addExtensionProvider("x", "jabber:x:event",
                new EventProvider());

        // Group Chat Invitations
        ProviderManager.addExtensionProvider("x", "jabber:x:conference",
                new GroupChatInvitation.Provider());

        // Service Discovery # Items
        ProviderManager.addIQProvider("query",
                "http://jabber.org/protocol/disco#items",
                new DiscoverItemsProvider());

        // Service Discovery # Info
        ProviderManager.addIQProvider("query",
                "http://jabber.org/protocol/disco#info",
                new DiscoverInfoProvider());

        // Data Forms
        ProviderManager.addExtensionProvider("x", "jabber:x:data",
                new DataFormProvider());

        // MUC User
        ProviderManager.addExtensionProvider("x",
                "http://jabber.org/protocol/muc#user", new MUCUserProvider());

        // MUC Admin
        ProviderManager.addIQProvider("query",
                "http://jabber.org/protocol/muc#admin", new MUCAdminProvider());

        // MUC Owner
        ProviderManager.addIQProvider("query",
                "http://jabber.org/protocol/muc#owner", new MUCOwnerProvider());

        // Version
        try {
            ProviderManager.addIQProvider("query", "jabber:iq:version",
                    Class.forName("org.jivesoftware.smackx.packet.Version"));
        } catch (ClassNotFoundException e) {
            // Not sure what's happening here.
        }

        // VCard
        ProviderManager.addIQProvider("vCard", "vcard-temp",
                new VCardProvider());

        // Offline Message Requests
        ProviderManager.addIQProvider("offline",
                "http://jabber.org/protocol/offline",
                new OfflineMessageRequest.Provider());

        // Offline Message Indicator
        ProviderManager.addExtensionProvider("offline",
                "http://jabber.org/protocol/offline",
                new OfflineMessageInfo.Provider());

        // Last Activity
        ProviderManager.addIQProvider("query", "jabber:iq:last",
                new LastActivity.Provider());

        // User Search
        ProviderManager.addIQProvider("query", "jabber:iq:search",
                new UserSearch.Provider());

        // SharedGroupsInfo
        ProviderManager.addIQProvider("sharedgroup",
                "http://www.jivesoftware.org/protocol/sharedgroup",
                new SharedGroupsInfo.Provider());

        // JEP-33: Extended Stanza Addressing
        ProviderManager.addExtensionProvider("addresses",
                "http://jabber.org/protocol/address",
                new MultipleAddressesProvider());

        // Privacy
        ProviderManager.addIQProvider("query", "jabber:iq:privacy",
                new PrivacyProvider());

        ProviderManager.addIQProvider("command",
                "http://jabber.org/protocol/commands",
                new AdHocCommandDataProvider());
        ProviderManager.addExtensionProvider("malformed-action",
                "http://jabber.org/protocol/commands",
                new AdHocCommandDataProvider.MalformedActionError());
        ProviderManager.addExtensionProvider("bad-locale",
                "http://jabber.org/protocol/commands",
                new AdHocCommandDataProvider.BadLocaleError());
        ProviderManager.addExtensionProvider("bad-payload",
                "http://jabber.org/protocol/commands",
                new AdHocCommandDataProvider.BadPayloadError());
        ProviderManager.addExtensionProvider("bad-sessionid",
                "http://jabber.org/protocol/commands",
                new AdHocCommandDataProvider.BadSessionIDError());
        ProviderManager.addExtensionProvider("session-expired",
                "http://jabber.org/protocol/commands",
                new AdHocCommandDataProvider.SessionExpiredError());
    }

    private ChatDataSource mChatDataSource = new ChatDataSource();
    private Map<String, PrivateChat> mChatCache = new HashMap<>();

    public static synchronized XMPPClient getInstance() {
        if (sInstance == null) sInstance = new XMPPClient();
        return sInstance;
    }

    public void connect(Runnable onSuccess) {
        AppExecutors.inBackground(() -> {
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
                mChatDataSource.setConnection(mConnection);
                registries();
                mConnection.connect(mUserName, mPassword, resume -> {
                    mChatDataSource.fetchOfflineMessages();
                    mConnection.notifyPresence(Presence.Type.available);
                    AppExecutors.onMainThread(onSuccess);
                });
            } catch (Exception e) {
                e.printStackTrace();
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
        owner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroy() {
                privateChat.stop();
            }
        });
        return privateChat;
    }

    public void disconnect() {
        Log.e("DEBUG", "disconnect");
        mConnection.unregisterAll();
        AppExecutors.inBackground(() -> {
            mConnection.notifyGone();
            mConnection.notifyPresence(Presence.Type.unavailable);
            sInstance = null;
        });
    }
}
