package ps.billyphan.chatsdk.utils;

import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.Jid;

import ps.billyphan.chatsdk.models.MessageEntry;
import ps.billyphan.chatsdk.xmpp.XMPPClient;

public class PackageAnalyze {

    public static String getToId(Message message) {
        if (message.getTo() == null) return XMPPClient.getInstance().getMyId();
        return getId(message.getTo());
    }

    public static String getFromId(Message message) {
        if (message.getFrom() == null) return XMPPClient.getInstance().getMyId();
        return getId(message.getFrom());
    }

    public static String getChatPair(MessageEntry message) {
        return String.format("%s#%s", message.getFromId(), message.getToId());
    }

    public static String getId(Jid jid) {
        return jid.toString().split("@")[0];
    }

    public static String getResourceId(Jid message) {
        return message.getResourceOrEmpty().toString();
    }

    public static boolean isFromGroup(Jid jid) {
        if (jid == null) return false;
        return jid.getDomain().toString().equals(XMPPClient.GROUP);
    }
}
