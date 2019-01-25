package com.kantek.chatsdk.xmpp;

import android.support.v4.util.Consumer;
import android.util.Log;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;

import java.io.IOException;

import com.kantek.chatsdk.extension.ReadReceipt;
import com.kantek.chatsdk.filter.ReceiptFilter;
import com.kantek.chatsdk.filter.StateFilter;
import com.kantek.chatsdk.filter.entry.MessageFilter;
import com.kantek.chatsdk.listeners.OnChatMessageListener;
import com.kantek.chatsdk.listeners.OnMessageListener;
import com.kantek.chatsdk.listeners.OnReceiptListener;
import com.kantek.chatsdk.models.ReceiptState;

public class XMPPChatConnection extends XMPPTCPConnection {

    private final Roster mRoster;
    private OnMessageListener mOnPrivateInComingListener;
    private OnMessageListener mOnPrivateOutGoingListener;
    private OnMessageListener mOnGroupInComingListener;
    private OnMessageListener mOnGroupOutGoingListener;
    private OnChatMessageListener mOnSentListener;

    private OnMessageListener mOnNotifyReadListener;
    private OnMessageListener mOnStateChangeListener;

    private OnMessageListener mOnReceivedListener;
    private OnMessageListener mOnReadListener;

    private ConnectionListener mConnectionListener;
    private Message mLastSent;

    public XMPPChatConnection(XMPPTCPConnectionConfiguration config) {
        super(config);
        // Enable roster
        mRoster = Roster.getInstanceFor(this);
        mRoster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);

        //Ping
        PingManager.setDefaultPingInterval(10);
        PingManager.getInstanceFor(this);

        // Enable stream
        setUseStreamManagement(true);
        setUseStreamManagementResumption(true);

        //Auto reconnection
        ReconnectionManager.getInstanceFor(this).enableAutomaticReconnection();
        ReconnectionManager.getInstanceFor(this).setReconnectionPolicy(ReconnectionManager.ReconnectionPolicy.FIXED_DELAY);

        //Enable State
        registryFeatures(ServiceDiscoveryManager.getInstanceFor(this));

        DeliveryReceiptManager delivery = DeliveryReceiptManager.getInstanceFor(this);
        delivery.setAutoReceiptMode(DeliveryReceiptManager.AutoReceiptMode.disabled);
        delivery.dontAutoAddDeliveryReceiptRequests();
    }

    private void registryFeatures(ServiceDiscoveryManager discoveryManager) {
        discoveryManager.addFeature(StateFilter.NAMESPACE);
        discoveryManager.addFeature(DeliveryReceipt.NAMESPACE);
    }

    public void connect(String userName, String password, Consumer<Boolean> listener) throws InterruptedException, XMPPException, SmackException, IOException {
        addConnectionListener(mConnectionListener = new ConnectionListener() {
            @Override
            public void connected(XMPPConnection connection) {
                Log.e("DEBUG", "connected");
            }

            @Override
            public void connectionClosed() {
                Log.e("DEBUG", "connectionClosed");
            }

            @Override
            public void connectionClosedOnError(Exception e) {
                Log.e("DEBUG", e.getMessage());
            }

            @Override
            public void authenticated(XMPPConnection connection, boolean resumed) {
                Log.e("DEBUG", "Authentication Success");
                listener.accept(resumed);
            }
        });
        connect();
        login(userName, password);
    }

    @Override
    public void disconnect() {
        unregisterAll();
        removeConnectionListener(mConnectionListener);
        notifyGone();
        super.disconnect();
    }

    public void registryReceiptListener(OnReceiptListener onReceiptListener) {
        addStanzaAcknowledgedListener(mOnSentListener = packet -> onReceiptListener.onReceived(packet, ReceiptState.SENT));

        addSyncStanzaListener(mOnReceivedListener = packet -> onReceiptListener.onReceived(packet, ReceiptState.RECEIVED),
                new AndFilter(ReceiptFilter.RECEIVED, MessageFilter.PRIVATE_OR_GROUP_EXCEPT_ME));

        addSyncStanzaListener(mOnReadListener = packet -> onReceiptListener.onReceived(packet, ReceiptState.READ),
                new AndFilter(ReceiptFilter.READ, MessageFilter.PRIVATE_OR_GROUP_EXCEPT_ME));
    }

    public void unregisterAll() {
        //Unregister receipt
        removeStanzaAcknowledgedListener(mOnSentListener);
        removeStanzaAcknowledgedListener(mOnReceivedListener);
        removeStanzaAcknowledgedListener(mOnReadListener);

        //Unregister message
        removeSyncStanzaListener(mOnPrivateInComingListener);

        removeSyncStanzaListener(mOnGroupInComingListener);
        removeSyncStanzaListener(mOnStateChangeListener);

        // Out
        removeStanzaInterceptor(mOnPrivateOutGoingListener);
        removeStanzaInterceptor(mOnGroupOutGoingListener);
        removeStanzaInterceptor(mOnNotifyReadListener);
    }

    public void registryInComingListener(Consumer<Message> listener) {
        addSyncStanzaListener(mOnPrivateInComingListener = listener::accept, MessageFilter.PRIVATE_BODY);
        addSyncStanzaListener(mOnGroupInComingListener = message -> {
            if (mLastSent == null) {
                listener.accept(message);
                return;
            }
            if (!mLastSent.getStanzaId().equals(message.getStanzaId()))
                listener.accept(message);
        }, MessageFilter.GROUP_BODY);
    }

    public void registryOutGoingListener(Consumer<Message> listener) {
        addStanzaInterceptor(mOnPrivateOutGoingListener = listener::accept, MessageFilter.PRIVATE_BODY);
        addStanzaInterceptor(mOnGroupOutGoingListener = message -> {
            mLastSent = message;
            listener.accept(message);
        }, MessageFilter.GROUP_BODY);
    }

    public void registryOnNotifyReadListener(Consumer<Message> listener) {
        addStanzaInterceptor(mOnNotifyReadListener = listener::accept, new AndFilter(
                MessageFilter.PRIVATE_OR_GROUP_EXCEPT_ME,
                new ReceiptFilter(ReadReceipt.class)
        ));
    }

    public void registryStateListener(Consumer<Message> listener) {
        addSyncStanzaListener(mOnStateChangeListener = listener::accept,
                new AndFilter(MessageFilter.PRIVATE_OR_GROUP_EXCEPT_ME, new StateFilter()));
    }

    public void notifyGone() {
        try {
            for (RosterEntry rosterEntry : Roster.getInstanceFor(this).getEntries()) {
                Message stanza = new Message();
                stanza.setTo(rosterEntry.getJid());
                stanza.setType(Message.Type.chat);
                stanza.addExtension(new ChatStateExtension(ChatState.gone));
                sendStanza(stanza);
            }
        } catch (SmackException.NotConnectedException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void notifyPresence(Presence.Type type) {
        try {
            sendStanza(new Presence(type));
        } catch (SmackException.NotConnectedException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Roster getRoster() {
        return mRoster;
    }
}
