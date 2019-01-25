package com.kantek.chatsdk.utils;

import com.kantek.chatsdk.xmpp.XMPPClient;

import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.Jid;

public class PackageAnalyze {

    public static String getToId(Message message) {
        if (message.getTo() == null) return XMPPClient.getInstance().getMyId().toUpperCase();
        return getId(message.getTo());
    }

    public static String getFromId(Message message) {
        if (message.getFrom() == null) return XMPPClient.getInstance().getMyId().toUpperCase();
        return getId(message.getFrom());
    }

    public static String getId(Jid jid) {
        return jid.toString().split("@")[0].toUpperCase();
    }

    public static String getResourceId(Jid message) {
        return message.getResourceOrEmpty().toString().toUpperCase();
    }

    public static boolean isFromGroup(Jid jid) {
        if (jid == null) return false;
        return jid.getDomain().toString().equalsIgnoreCase(XMPPClient.GROUP);
    }
}
