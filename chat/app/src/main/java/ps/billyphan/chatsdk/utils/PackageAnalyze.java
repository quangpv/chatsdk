package ps.billyphan.chatsdk.utils;

import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityFullJid;

import java.util.Map;

import ps.billyphan.chatsdk.models.MessageEntry;

public class PackageAnalyze {

    public static String getToId(Message message) {
        if (message.getTo() == null) return "";
        return message.getTo().toString().split("@")[0];
    }

    public static String getFromId(Message message) {
        if (message.getFrom() == null) return "";
        return message.getFrom().toString().split("@")[0];
    }

    public static String getChatPair(Message message) {
        return String.format("%s#%s", getFromId(message), getToId(message));
    }

    public static String getChatPair(MessageEntry message) {
        return String.format("%s#%s", message.getFromId(), message.getToId());
    }

    public static String getChatPair(String id1, String id2) {
        return String.format("%s#%s", id1, id2);
    }

    public static String getId(EntityFullJid jid) {
        return jid.toString().split("@")[0];
    }

    public static <T> T getAtPair(Map<String, T> map, Message message) {
        return getAtPair(map, getFromId(message), getToId(message));
    }

    public static <T> T getAtPair(Map<String, T> map, MessageEntry message) {
        return getAtPair(map, message.getFromId(), message.getToId());
    }

    public static <T> T getAtPair(Map<String, T> map, String id1, String id2) {
        T data = map.get(getChatPair(id1, id2));
        if (data == null) data = map.get(getChatPair(id2, id1));
        return data;
    }
}
