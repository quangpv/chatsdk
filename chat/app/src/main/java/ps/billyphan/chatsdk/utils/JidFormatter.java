package ps.billyphan.chatsdk.utils;

import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import ps.billyphan.chatsdk.xmpp.XMPPClient;

public final class JidFormatter {
    public static DomainBareJid domain(String host) {
        try {
            return JidCreate.domainBareFrom(host);
        } catch (XmppStringprepException e) {
            throw new RuntimeException(e);
        }
    }

    public static EntityBareJid jid(String withUserId) {
        try {
            return JidCreate.entityBareFrom(String.format("%s@%s", withUserId, XMPPClient.HOST));
        } catch (XmppStringprepException e) {
            throw new RuntimeException(e);
        }
    }

    public static Resourcepart resource(String myId) {
        try {
            return Resourcepart.from(myId);
        } catch (XmppStringprepException e) {
            throw new RuntimeException(e);
        }
    }

    public static EntityBareJid groupJid(String withId) {
        try {
            return JidCreate.entityBareFrom(String.format("%s@%s", withId, XMPPClient.GROUP));
        } catch (XmppStringprepException e) {
            throw new RuntimeException(e);
        }
    }
}
