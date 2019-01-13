package ps.billyphan.chatsdk.extension;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.provider.EmbeddedExtensionProvider;
import org.jivesoftware.smack.util.XmlStringBuilder;

import java.util.List;
import java.util.Map;

public class ReadReceipt implements ExtensionElement {
    public static final String NAMESPACE = "urn:xmpp:receipts";
    public static final String ELEMENT = "read";
    private final String mStamp;

    public ReadReceipt() {
        mStamp = String.valueOf(System.currentTimeMillis());
    }

    public ReadReceipt(String ts) {
        mStamp = ts;
    }

    @Override
    public String getElementName() {
        return ELEMENT;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public XmlStringBuilder toXML(String enclosingNamespace) {
        XmlStringBuilder xml = new XmlStringBuilder(this);
        xml.optAttribute("ts", String.valueOf(mStamp));
        xml.closeEmptyElement();
        return xml;
    }

    public static class Provider extends EmbeddedExtensionProvider<ReadReceipt> {

        @Override
        protected ReadReceipt createReturnExtension(String currentElement, String currentNamespace,
                                                    Map<String, String> attributeMap, List<? extends ExtensionElement> content) {
            return new ReadReceipt(attributeMap.get("ts"));
        }

    }
}
