package ps.billyphan.chatsdk;

import android.support.v4.util.Consumer;
import android.util.Log;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;

import java.io.IOException;

import ps.billyphan.chatsdk.extension.ReadReceipt;
import ps.billyphan.chatsdk.filter.MessageFilter;
import ps.billyphan.chatsdk.filter.ReceiptFilter;
import ps.billyphan.chatsdk.filter.StateFilter;
import ps.billyphan.chatsdk.listeners.OnReceiptListener;
import ps.billyphan.chatsdk.listeners.OnStanzaMessageListener;

public class XMPPChatConnection extends XMPPTCPConnection {
    private OnStanzaMessageListener mOnSentListener;
    private OnStanzaMessageListener mOnSendingListener;
    private OnStanzaMessageListener mOnReceivedListener;
    private OnStanzaMessageListener mOnReadListener;
    private OnStanzaMessageListener mOnInComingListener;
    private OnStanzaMessageListener mOnOutGoingListener;
    private OnStanzaMessageListener mOnNotifyReadListener;
    private OnStanzaMessageListener mOnStateChangeListener;

    public XMPPChatConnection(XMPPTCPConnectionConfiguration config) {
        super(config);
        // Enable roster
        Roster.getInstanceFor(this).setSubscriptionMode(Roster.SubscriptionMode.accept_all);

        // Enable stream
        setUseStreamManagement(true);
        setUseStreamManagementResumption(true);

        //Auto reconnection
        ReconnectionManager.getInstanceFor(this).enableAutomaticReconnection();
        ReconnectionManager.getInstanceFor(this).setReconnectionPolicy(ReconnectionManager.ReconnectionPolicy.FIXED_DELAY);

        //Enable State
        ServiceDiscoveryManager.getInstanceFor(this).addFeature(StateFilter.NAMESPACE);
    }

    public void connect(String userName, String password, Consumer<Boolean> listener) throws InterruptedException, IOException, SmackException, XMPPException {
        addConnectionListener(new ConnectionListener() {
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

    public void registryReceiptListener(OnReceiptListener onReceiptListener) {
        addStanzaInterceptor(mOnSendingListener = packet -> onReceiptListener.onReceived((Message) packet, ReceiptState.SENDING), MessageFilter.PRIVATE_OR_GROUP);
        addStanzaAcknowledgedListener(mOnSentListener = packet -> onReceiptListener.onReceived(packet, ReceiptState.SENT));
        addSyncStanzaListener(mOnReceivedListener = packet -> onReceiptListener.onReceived(packet, ReceiptState.RECEIVED), ReceiptFilter.RECEIVED);
        addSyncStanzaListener(mOnReadListener = packet -> onReceiptListener.onReceived(packet, ReceiptState.READ), ReceiptFilter.READ);
    }

    public void unregisterAll() {
        //Unregister receipt
        removeStanzaAcknowledgedListener(mOnSentListener);
        removeStanzaAcknowledgedListener(mOnSendingListener);
        removeStanzaAcknowledgedListener(mOnReceivedListener);
        removeStanzaAcknowledgedListener(mOnReadListener);

        //Unregister message
        removeSyncStanzaListener(mOnInComingListener);
        removeSyncStanzaListener(mOnOutGoingListener);
        removeSyncStanzaListener(mOnNotifyReadListener);
        removeSyncStanzaListener(mOnStateChangeListener);
    }

    public void registryInComingListener(Consumer<Message> listener) {
        addSyncStanzaListener(mOnInComingListener = listener::accept, MessageFilter.PRIVATE_OR_GROUP);
    }

    public void registryOutGoingListener(Consumer<Message> listener) {
        addStanzaInterceptor(mOnOutGoingListener = listener::accept, MessageFilter.PRIVATE_OR_GROUP);
    }

    public void registryOnNotifyReadListener(Consumer<Message> listener) {
        addStanzaInterceptor(mOnNotifyReadListener = listener::accept, new ReceiptFilter(ReadReceipt.class));
    }

    public void registryStateListener(Consumer<Message> listener) {
        addSyncStanzaListener(mOnStateChangeListener = listener::accept, new StateFilter());
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
}
