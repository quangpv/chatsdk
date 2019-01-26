package com.kantek.chatsdk.utils;

import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import com.kantek.chatsdk.xmpp.XMPPClient;

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

    public static EntityFullJid fullJid(String withUserId) {
        try {
            return JidCreate.entityFullFrom(String.format("%s@%s/%s", withUserId, XMPPClient.HOST,XMPPClient.RESOURCE));
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
