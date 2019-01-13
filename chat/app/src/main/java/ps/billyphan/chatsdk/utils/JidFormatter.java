package ps.billyphan.chatsdk.utils;

import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import ps.billyphan.chatsdk.XMPPClient;

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
}
