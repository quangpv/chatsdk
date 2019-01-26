package com.kantek.chatsdk;

import com.kantek.chatsdk.utils.JidFormatter;
import com.kantek.chatsdk.xmpp.XMPPChatConnection;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.junit.Test;
import org.jxmpp.stringprep.XmppStringprepException;

import static com.kantek.chatsdk.xmpp.XMPPClient.*;
import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
    public void testConnection(){

    }
}