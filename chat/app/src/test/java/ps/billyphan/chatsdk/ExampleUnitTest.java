package ps.billyphan.chatsdk;

import org.junit.Test;

import ps.billyphan.chatsdk.models.PrivateChat;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    private XMPPClient mClient;
    private PrivateChat mChat;

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

}