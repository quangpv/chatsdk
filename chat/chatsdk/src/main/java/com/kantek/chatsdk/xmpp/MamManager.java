package com.kantek.chatsdk.xmpp;

import org.jivesoftware.smack.ConnectionCreationListener;
import org.jivesoftware.smack.Manager;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.SmackException.NotLoggedInException;
import org.jivesoftware.smack.StanzaCollector;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPConnectionRegistry;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.filter.IQReplyFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.util.Objects;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.forward.packet.Forwarded;
import org.jivesoftware.smackx.mam.element.MamFinIQ;
import org.jivesoftware.smackx.mam.element.MamPrefsIQ;
import org.jivesoftware.smackx.mam.element.MamQueryIQ;
import org.jivesoftware.smackx.mam.filter.MamResultFilter;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.rsm.packet.RSMSet;
import org.jivesoftware.smackx.xdata.FormField;
import org.jivesoftware.smackx.xdata.packet.DataForm;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.Jid;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * A Manager for Message Archive Management (MAM, <a href="http://xmpp.org/extensions/xep-0313.html">XEP-0313</a>).
 *
 * <h2>Get an instance of a manager for a message archive</h2>
 *
 * In order to work with {@link MamManager} you need to obtain an instance for a particular archive.
 * To get the instance for the default archive on the user's server, use the {@link #getInstanceFor(XMPPConnection)} method.
 *
 * <pre>
 * {@code
 * XMPPConnection connection = ...
 * MamManager mamManager = MamManager.getInstanceFor(connection);
 * }
 * </pre>
 *
 * If you want to retrieve a manager for a different archive use {@link #getInstanceFor(XMPPConnection, Jid)}, which takes the archive's XMPP address as second argument.
 *
 * <h2>Check if MAM is supported</h2>
 *
 * After you got your manager instance, you probably first want to check if MAM is supported.
 * Simply use {@link #isSupported()} to check if there is a MAM archive available.
 *
 * <pre>
 * {@code
 * boolean isSupported = mamManager.isSupported();
 * }
 * </pre>
 *
 * <h2>Message Archive Preferences</h2>
 *
 * After you have verified that the MAM is supported, you probably want to configure the archive first before using it.
 * One of the most important preference is to enable MAM for your account.
 * Some servers set up new accounts with MAM disabled by default.
 * You can do so by calling {@link #enableMamForAllMessages()}.
 *
 * <h3>Retrieve current preferences</h3>
 *
 * The archive's preferences can be retrieved using {@link #retrieveArchivingPreferences()}.
 *
 * <h3>Update preferences</h3>
 *
 * Use {@link MamManager.MamPrefsResult#asMamPrefs()} to get a modifiable {@link MamManager.MamPrefs} instance.
 * After performing the desired changes, use {@link #updateArchivingPreferences(MamManager.MamPrefs)} to update the preferences.
 *
 * <h2>Query the message archive</h2>
 *
 * Querying a message archive involves a two step process. First you need specify the query's arguments, for example a date range.
 * The query arguments of a particular query are represented by a {@link MamManager.MamQueryArgs} instance, which can be build using {@link MamManager.MamQueryArgs.Builder}.
 *
 * After you have build such an instance, use {@link #queryArchive(MamManager.MamQueryArgs)} to issue the query.
 *
 * <pre>
 * {@code
 * MamQueryArgs mamQueryArgs = MamQueryArgs.builder()
 *                                 .withJid(jid)
 *                                 .setResultPageSize(10)
 *                                 .queryLastPage()
 *                                 .build();
 * MamQuery mamQuery = mamManager.queryArchive(mamQueryArgs);
 * }
 * </pre>
 *
 * On success {@link #queryArchive(MamManager.MamQueryArgs)} returns a {@link MamManager.MamQuery} instance.
 * The instance will hold one page of the queries result set.
 * Use {@link MamManager.MamQuery#getMessages()} to retrieve the messages of the archive belonging to the page.
 *
 * You can get the whole page including all metadata using {@link MamManager.MamQuery#getPage()}.
 *
 * <h2>Paging through the results</h2>
 *
 * Because the matching result set could be potentially very big, a MAM service will probably not return all matching messages.
 * Instead the results are possibly send in multiple pages.
 * To check if the result was complete or if there are further pages, use {@link MamManager.MamQuery#isComplete()}.
 * If this method returns {@code false}, then you may want to page through the archive.
 *
 * {@link MamManager.MamQuery} provides convince methods to do so: {@link MamManager.MamQuery#pageNext(int)} and {@link MamManager.MamQuery#pagePrevious(int)}.
 *
 * <pre>
 * {@code
 * MamQuery nextPageMamQuery = mamQuery.pageNext(10);
 * }
 * </pre>
 *
 * <h2>Get the supported form fields</h2>
 *
 * You can use {@link #retrieveFormFields()} to retrieve a list of the supported additional form fields by this archive.
 * Those fields can be used for further restrict a query.
 *
 *
 * @see <a href="http://xmpp.org/extensions/xep-0313.html">XEP-0313: Message
 *      Archive Management</a>
 * @author Florian Schmaus
 * @author Fernando Ramirez
 *
 */
public final class MamManager extends Manager {

    static {
        XMPPConnectionRegistry.addConnectionCreationListener(new ConnectionCreationListener() {
            @Override
            public void connectionCreated(XMPPConnection connection) {
                getInstanceFor(connection);
            }
        });
    }

    private static final String FORM_FIELD_WITH = "with";
    private static final String FORM_FIELD_START = "start";
    private static final String FORM_FIELD_END = "end";

    private static final Map<XMPPConnection, Map<Jid, MamManager>> INSTANCES = new WeakHashMap<>();

    /**
     * Get a MamManager for the MAM archive of the local entity (the "user") of the given connection.
     *
     * @param connection the XMPP connection to get the archive for.
     * @return the instance of MamManager.
     */
    public static MamManager getInstanceFor(XMPPConnection connection) {
        return getInstanceFor(connection, (Jid) null);
    }

    /**
     * Get a MamManager for the MAM archive of the given {@code MultiUserChat}. Note that not all MUCs support MAM,
     * hence it is recommended to use {@link #isSupported()} to check if MAM is supported by the MUC.
     *
     * @param multiUserChat the MultiUserChat to retrieve the MamManager for.
     * @return the MamManager for the given MultiUserChat.
     * @since 4.3.0
     */
    public static MamManager getInstanceFor(MultiUserChat multiUserChat) {
        XMPPConnection connection = multiUserChat.getXmppConnection();
        Jid archiveAddress = multiUserChat.getRoom();
        return getInstanceFor(connection, archiveAddress);
    }

    public static synchronized MamManager getInstanceFor(XMPPConnection connection, Jid archiveAddress) {
        Map<Jid, MamManager> managers = INSTANCES.get(connection);
        if (managers == null) {
            managers = new HashMap<>();
            INSTANCES.put(connection, managers);
        }
        MamManager mamManager = managers.get(archiveAddress);
        if (mamManager == null) {
            mamManager = new MamManager(connection, archiveAddress);
            managers.put(archiveAddress, mamManager);
        }
        return mamManager;
    }

    private final Jid archiveAddress;

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    private MamManager(XMPPConnection connection, Jid archiveAddress) {
        super(connection);
        this.archiveAddress = archiveAddress;
        serviceDiscoveryManager = ServiceDiscoveryManager.getInstanceFor(connection);
    }

    /**
     * The the XMPP address of this MAM archive. Note that this method may return {@code null} if this MamManager
     * handles the local entity's archive and if the connection has never been authenticated at least once.
     *
     * @return the XMPP address of this MAM archive or {@code null}.
     * @since 4.3.0
     */
    public Jid getArchiveAddress() {
        if (archiveAddress == null) {
            EntityFullJid localJid = connection().getUser();
            if (localJid == null) {
                return null;
            }
            return localJid.asBareJid();
        }
        return archiveAddress;
    }

    public static final class MamQueryArgs {
        private final String node;

        private final Map<String, FormField> formFields;

        private final Integer maxResults;

        private final String afterUid;

        private final String beforeUid;

        private MamQueryArgs(MamManager.MamQueryArgs.Builder builder) {
            node = builder.node;
            formFields = builder.formFields;
            if (builder.maxResults > 0) {
                maxResults = builder.maxResults;
            } else {
                maxResults = null;
            }
            afterUid = builder.afterUid;
            beforeUid = builder.beforeUid;
        }

        private DataForm dataForm;

        DataForm getDataForm() {
            if (dataForm != null) {
                return dataForm;
            }
            dataForm = getNewMamForm();
            dataForm.addFields(formFields.values());
            return dataForm;
        }

        void maybeAddRsmSet(MamQueryIQ mamQueryIQ) {
            if (maxResults == null && afterUid == null && beforeUid == null) {
                return;
            }

            int max;
            if (maxResults != null) {
                max = maxResults;
            } else {
                max = -1;
            }

            RSMSet rsmSet = new RSMSet(afterUid, beforeUid, -1, -1, null, max, null, -1);
            mamQueryIQ.addExtension(rsmSet);
        }

        public static MamManager.MamQueryArgs.Builder builder() {
            return new MamManager.MamQueryArgs.Builder();
        }

        public static final class Builder {
            private String node;

            private final Map<String, FormField> formFields = new HashMap<>(8);

            private int maxResults = -1;

            private String afterUid;

            private String beforeUid;

            public MamManager.MamQueryArgs.Builder queryNode(String node) {
                if (node == null) {
                    return this;
                }

                this.node = node;

                return this;
            }

            public MamManager.MamQueryArgs.Builder limitResultsToJid(Jid withJid) {
                if (withJid == null) {
                    return this;
                }

                FormField formField = getWithFormField(withJid);
                formFields.put(formField.getVariable(), formField);

                return this;
            }

            public MamManager.MamQueryArgs.Builder limitResultsSince(Date start) {
                if (start == null) {
                    return this;
                }

                FormField formField = new FormField(FORM_FIELD_START);
                formField.addValue(start);
                formFields.put(formField.getVariable(), formField);

                FormField endFormField = formFields.get(FORM_FIELD_END);
                if (endFormField != null) {
                    Date end;
                    try {
                        end = endFormField.getFirstValueAsDate();
                    } catch (ParseException e) {
                        throw new IllegalStateException(e);
                    }
                    if (end.getTime() <= start.getTime()) {
                        throw new IllegalArgumentException("Given start date (" + start
                                + ") is after the existing end date (" + end + ')');
                    }
                }

                return this;
            }

            public MamManager.MamQueryArgs.Builder limitResultsBefore(Date end) {
                if (end == null) {
                    return this;
                }

                FormField formField = new FormField(FORM_FIELD_END);
                formField.addValue(end);
                formFields.put(formField.getVariable(), formField);

                FormField startFormField = formFields.get(FORM_FIELD_START);
                if (startFormField != null) {
                    Date start;
                    try {
                        start = startFormField.getFirstValueAsDate();
                    } catch (ParseException e) {
                        throw new IllegalStateException(e);
                    }
                    if (end.getTime() <= start.getTime()) {
                        throw new IllegalArgumentException("Given end date (" + end
                                + ") is before the existing start date (" + start + ')');
                    }
                }

                return this;
            }

            public MamManager.MamQueryArgs.Builder setResultPageSize(Integer max) {
                if (max == null) {
                    maxResults = -1;
                    return this;
                }
                return setResultPageSizeTo(max.intValue());
            }

            public MamManager.MamQueryArgs.Builder setResultPageSizeTo(int max) {
                if (max < 0) {
                    throw new IllegalArgumentException();
                }
                this.maxResults = max;
                return this;
            }

            /**
             * Only return the count of messages the query yields, not the actual messages. Note that not all services
             * return a correct count, some return an approximate count.
             *
             * @return an reference to this builder.
             * @see <a href="https://xmpp.org/extensions/xep-0059.html#count">XEP-0059 § 2.7</a>
             */
            public MamManager.MamQueryArgs.Builder onlyReturnMessageCount() {
                return setResultPageSizeTo(0);
            }

            public MamManager.MamQueryArgs.Builder withAdditionalFormField(FormField formField) {
                formFields.put(formField.getVariable(), formField);
                return this;
            }

            public MamManager.MamQueryArgs.Builder withAdditionalFormFields(List<FormField> additionalFields) {
                for (FormField formField : additionalFields) {
                    withAdditionalFormField(formField);
                }
                return this;
            }

            public MamManager.MamQueryArgs.Builder afterUid(String afterUid) {
                this.afterUid = StringUtils.requireNullOrNotEmpty(afterUid, "afterUid must not be empty");
                return this;
            }

            /**
             * Specifies a message UID as 'before' anchor for the query. Note that unlike {@link #afterUid(String)} this
             * method also accepts the empty String to query the last page of an archive (c.f. XEP-0059 § 2.5).
             *
             * @param beforeUid a message UID acting as 'before' query anchor.
             * @return an instance to this builder.
             */
            public MamManager.MamQueryArgs.Builder beforeUid(String beforeUid) {
                // We don't perform any argument validation, since every possible argument (null, empty string,
                // non-empty string) is valid.
                this.beforeUid = beforeUid;
                return this;
            }

            /**
             * Query from the last, i.e. most recent, page of the archive. This will return the very last page of the
             * archive holding the most recent matching messages. You usually would page backwards from there on.
             *
             * @return a reference to this builder.
             * @see <a href="https://xmpp.org/extensions/xep-0059.html#last">XEP-0059 § 2.5. Requesting the Last Page in
             *      a Result Set</a>
             */
            public MamManager.MamQueryArgs.Builder queryLastPage() {
                return beforeUid("");
            }

            public MamManager.MamQueryArgs build() {
                return new MamManager.MamQueryArgs(this);
            }
        }
    }

    /**
     * Query archive with a maximum amount of results.
     *
     * @param max
     * @return the MAM query result
     * @throws NoResponseException
     * @throws XMPPErrorException
     * @throws NotConnectedException
     * @throws InterruptedException
     * @throws NotLoggedInException
     * @deprecated use {@link #queryArchive(MamManager.MamQueryArgs)} instead.
     */
    @Deprecated
    // TODO Remove in Smack 4.4
    public MamManager.MamQueryResult queryArchive(Integer max) throws NoResponseException, XMPPErrorException,
            NotConnectedException, InterruptedException, NotLoggedInException {
        return queryArchive(null, max, null, null, null, null);
    }

    /**
     * Query archive with a JID (only messages from/to the JID).
     *
     * @param withJid
     * @return the MAM query result
     * @throws NoResponseException
     * @throws XMPPErrorException
     * @throws NotConnectedException
     * @throws InterruptedException
     * @throws NotLoggedInException
     * @deprecated use {@link #queryArchive(MamManager.MamQueryArgs)} instead.
     */
    @Deprecated
    // TODO Remove in Smack 4.4
    public MamManager.MamQueryResult queryArchive(Jid withJid) throws NoResponseException, XMPPErrorException,
            NotConnectedException, InterruptedException, NotLoggedInException {
        return queryArchive(null, null, null, null, withJid, null);
    }

    /**
     * Query archive filtering by start and/or end date. If start == null, the
     * value of 'start' will be equal to the date/time of the earliest message
     * stored in the archive. If end == null, the value of 'end' will be equal
     * to the date/time of the most recent message stored in the archive.
     *
     * @param start
     * @param end
     * @return the MAM query result
     * @throws NoResponseException
     * @throws XMPPErrorException
     * @throws NotConnectedException
     * @throws InterruptedException
     * @throws NotLoggedInException
     * @deprecated use {@link #queryArchive(MamManager.MamQueryArgs)} instead.
     */
    @Deprecated
    // TODO Remove in Smack 4.4
    public MamManager.MamQueryResult queryArchive(Date start, Date end) throws NoResponseException, XMPPErrorException,
            NotConnectedException, InterruptedException, NotLoggedInException {
        return queryArchive(null, null, start, end, null, null);
    }

    /**
     * Query Archive adding filters with additional fields.
     *
     * @param additionalFields
     * @return the MAM query result
     * @throws NoResponseException
     * @throws XMPPErrorException
     * @throws NotConnectedException
     * @throws InterruptedException
     * @throws NotLoggedInException
     * @deprecated use {@link #queryArchive(MamManager.MamQueryArgs)} instead.
     */
    @Deprecated
    // TODO Remove in Smack 4.4
    public MamManager.MamQueryResult queryArchive(List<FormField> additionalFields) throws NoResponseException, XMPPErrorException,
            NotConnectedException, InterruptedException, NotLoggedInException {
        return queryArchive(null, null, null, null, null, additionalFields);
    }

    /**
     * Query archive filtering by start date. The value of 'end' will be equal
     * to the date/time of the most recent message stored in the archive.
     *
     * @param start
     * @return the MAM query result
     * @throws NoResponseException
     * @throws XMPPErrorException
     * @throws NotConnectedException
     * @throws InterruptedException
     * @throws NotLoggedInException
     * @deprecated use {@link #queryArchive(MamManager.MamQueryArgs)} instead.
     */
    @Deprecated
    // TODO Remove in Smack 4.4
    public MamManager.MamQueryResult queryArchiveWithStartDate(Date start) throws NoResponseException, XMPPErrorException,
            NotConnectedException, InterruptedException, NotLoggedInException {
        return queryArchive(null, null, start, null, null, null);
    }

    /**
     * Query archive filtering by end date. The value of 'start' will be equal
     * to the date/time of the earliest message stored in the archive.
     *
     * @param end
     * @return the MAM query result
     * @throws NoResponseException
     * @throws XMPPErrorException
     * @throws NotConnectedException
     * @throws InterruptedException
     * @throws NotLoggedInException
     * @deprecated use {@link #queryArchive(MamManager.MamQueryArgs)} instead.
     */
    @Deprecated
    // TODO Remove in Smack 4.4
    public MamManager.MamQueryResult queryArchiveWithEndDate(Date end) throws NoResponseException, XMPPErrorException,
            NotConnectedException, InterruptedException, NotLoggedInException {
        return queryArchive(null, null, null, end, null, null);
    }


    /**
     * Query archive applying filters: max count, start date, end date, from/to
     * JID and with additional fields.
     *
     * @param max
     * @param start
     * @param end
     * @param withJid
     * @param additionalFields
     * @return the MAM query result
     * @throws NoResponseException
     * @throws XMPPErrorException
     * @throws NotConnectedException
     * @throws InterruptedException
     * @throws NotLoggedInException
     * @deprecated use {@link #queryArchive(MamManager.MamQueryArgs)} instead.
     */
    @Deprecated
    // TODO Remove in Smack 4.4
    public MamManager.MamQueryResult queryArchive(Integer max, Date start, Date end, Jid withJid, List<FormField> additionalFields)
            throws NoResponseException, XMPPErrorException, NotConnectedException, InterruptedException,
            NotLoggedInException {
        return queryArchive(null, max, start, end, withJid, additionalFields);
    }


    /**
     * Query an message archive like a MUC archive or a PubSub node archive, addressed by an archiveAddress, applying
     * filters: max count, start date, end date, from/to JID and with additional fields. When archiveAddress is null the
     * default, the server will be requested.
     *
     * @param node The PubSub node name, can be null
     * @param max
     * @param start
     * @param end
     * @param withJid
     * @param additionalFields
     * @return the MAM query result
     * @throws NoResponseException
     * @throws XMPPErrorException
     * @throws NotConnectedException
     * @throws InterruptedException
     * @throws NotLoggedInException
     * @deprecated use {@link #queryArchive(MamManager.MamQueryArgs)} instead.
     */
    @Deprecated
    // TODO Remove in Smack 4.4
    public MamManager.MamQueryResult queryArchive(String node, Integer max, Date start, Date end, Jid withJid,
                                                                              List<FormField> additionalFields)
            throws NoResponseException, XMPPErrorException, NotConnectedException, InterruptedException,
            NotLoggedInException {
        MamManager.MamQueryArgs mamQueryArgs = MamManager.MamQueryArgs.builder()
                .queryNode(node)
                .setResultPageSize(max)
                .limitResultsSince(start)
                .limitResultsBefore(end)
                .limitResultsToJid(withJid)
                .withAdditionalFormFields(additionalFields)
                .build();

        MamManager.MamQuery mamQuery = queryArchive(mamQueryArgs);
        return new MamManager.MamQueryResult(mamQuery);
    }

    public MamManager.MamQuery queryArchive(MamManager.MamQueryArgs mamQueryArgs) throws NoResponseException, XMPPErrorException,
            NotConnectedException, NotLoggedInException, InterruptedException {
        String queryId = UUID.randomUUID().toString();
        String node = mamQueryArgs.node;
        DataForm dataForm = mamQueryArgs.getDataForm();

        MamQueryIQ mamQueryIQ = new MamQueryIQ(queryId, node, dataForm);
        mamQueryIQ.setType(IQ.Type.set);
        mamQueryIQ.setTo(archiveAddress);

        mamQueryArgs.maybeAddRsmSet(mamQueryIQ);

        return queryArchive(mamQueryIQ);
    }

    private static FormField getWithFormField(Jid withJid) {
        FormField formField = new FormField(FORM_FIELD_WITH);
        formField.addValue(withJid.toString());
        return formField;
    }

    private static void addWithJid(Jid withJid, DataForm dataForm) {
        if (withJid == null) {
            return;
        }
        FormField formField = getWithFormField(withJid);
        dataForm.addField(formField);
    }

    /**
     * Returns a page of the archive.
     *
     * @param dataForm
     * @param rsmSet
     * @return the MAM query result
     * @throws NoResponseException
     * @throws XMPPErrorException
     * @throws NotConnectedException
     * @throws InterruptedException
     * @throws NotLoggedInException
     * @deprecated use {@link #queryArchive(MamManager.MamQueryArgs)} instead.
     */
    @Deprecated
    // TODO Remove in Smack 4.4
    public MamManager.MamQueryResult page(DataForm dataForm, RSMSet rsmSet) throws NoResponseException, XMPPErrorException,
            NotConnectedException, InterruptedException, NotLoggedInException {
        return page(null, dataForm, rsmSet);
    }

    /**
     * Returns a page of the archive. This is a low-level method, you possibly do not want to use it directly unless you
     * know what you are doing.
     *
     * @param node The PubSub node name, can be null
     * @param dataForm
     * @param rsmSet
     * @return the MAM query result
     * @throws NoResponseException
     * @throws XMPPErrorException
     * @throws NotConnectedException
     * @throws InterruptedException
     * @throws NotLoggedInException
     * @deprecated use {@link #queryArchive(MamManager.MamQueryArgs)} instead.
     */
    @Deprecated
    // TODO Remove in Smack 4.4
    public MamManager.MamQueryResult page(String node, DataForm dataForm, RSMSet rsmSet)
            throws NoResponseException, XMPPErrorException,
            NotConnectedException, InterruptedException, NotLoggedInException {
        MamQueryIQ mamQueryIQ = new MamQueryIQ(UUID.randomUUID().toString(), node, dataForm);
        mamQueryIQ.setType(IQ.Type.set);
        mamQueryIQ.setTo(archiveAddress);
        mamQueryIQ.addExtension(rsmSet);
        MamManager.MamQuery mamQuery = queryArchive(mamQueryIQ);
        return new MamManager.MamQueryResult(mamQuery);
    }

    /**
     * Returns the next page of the archive.
     *
     * @param mamQueryResult
     *            is the previous query result
     * @param count
     *            is the amount of messages that a page contains
     * @return the MAM query result
     * @throws NoResponseException
     * @throws XMPPErrorException
     * @throws NotConnectedException
     * @throws InterruptedException
     * @throws NotLoggedInException
     * @deprecated use {@link MamManager.MamQuery#pageNext(int)} instead.
     */
    @Deprecated
    // TODO Remove in Smack 4.4
    public MamManager.MamQueryResult pageNext(MamManager.MamQueryResult mamQueryResult, int count) throws NoResponseException,
            XMPPErrorException, NotConnectedException, InterruptedException, NotLoggedInException {
        RSMSet previousResultRsmSet = mamQueryResult.mamFin.getRSMSet();
        RSMSet requestRsmSet = new RSMSet(count, previousResultRsmSet.getLast(), RSMSet.PageDirection.after);
        return page(mamQueryResult, requestRsmSet);
    }

    /**
     * Returns the previous page of the archive.
     *
     * @param mamQueryResult
     *            is the previous query result
     * @param count
     *            is the amount of messages that a page contains
     * @return the MAM query result
     * @throws NoResponseException
     * @throws XMPPErrorException
     * @throws NotConnectedException
     * @throws InterruptedException
     * @throws NotLoggedInException
     * @deprecated use {@link MamManager.MamQuery#pagePrevious(int)} instead.
     */
    @Deprecated
    // TODO Remove in Smack 4.4
    public MamManager.MamQueryResult pagePrevious(MamManager.MamQueryResult mamQueryResult, int count) throws NoResponseException,
            XMPPErrorException, NotConnectedException, InterruptedException, NotLoggedInException {
        RSMSet previousResultRsmSet = mamQueryResult.mamFin.getRSMSet();
        RSMSet requestRsmSet = new RSMSet(count, previousResultRsmSet.getFirst(), RSMSet.PageDirection.before);
        return page(mamQueryResult, requestRsmSet);
    }

    private MamManager.MamQueryResult page(MamManager.MamQueryResult mamQueryResult, RSMSet requestRsmSet) throws NoResponseException,
            XMPPErrorException, NotConnectedException, NotLoggedInException, InterruptedException {
        ensureMamQueryResultMatchesThisManager(mamQueryResult);

        return page(mamQueryResult.node, mamQueryResult.form, requestRsmSet);
    }

    /**
     * Obtain page before the first message saved (specific chat).
     * <p>
     * Note that the messageUid is the XEP-0313 UID and <b>not</b> the stanza ID of the message.
     * </p>
     *
     * @param chatJid
     * @param messageUid the UID of the message of which messages before should be received.
     * @param max
     * @return the MAM query result
     * @throws XMPPErrorException
     * @throws NotLoggedInException
     * @throws NotConnectedException
     * @throws InterruptedException
     * @throws NoResponseException
     * @deprecated use {@link #queryArchive(MamManager.MamQueryArgs)} instead.
     */
    @Deprecated
    // TODO Remove in Smack 4.4
    public MamManager.MamQueryResult pageBefore(Jid chatJid, String messageUid, int max) throws XMPPErrorException,
            NotLoggedInException, NotConnectedException, InterruptedException, NoResponseException {
        RSMSet rsmSet = new RSMSet(null, messageUid, -1, -1, null, max, null, -1);
        DataForm dataForm = getNewMamForm();
        addWithJid(chatJid, dataForm);
        return page(null, dataForm, rsmSet);
    }

    /**
     * Obtain page after the last message saved (specific chat).
     * <p>
     * Note that the messageUid is the XEP-0313 UID and <b>not</b> the stanza ID of the message.
     * </p>
     *
     * @param chatJid
     * @param messageUid the UID of the message of which messages after should be received.
     * @param max
     * @return the MAM query result
     * @throws XMPPErrorException
     * @throws NotLoggedInException
     * @throws NotConnectedException
     * @throws InterruptedException
     * @throws NoResponseException
     * @deprecated use {@link #queryArchive(MamManager.MamQueryArgs)} instead.
     */
    @Deprecated
    // TODO Remove in Smack 4.4
    public MamManager.MamQueryResult pageAfter(Jid chatJid, String messageUid, int max) throws XMPPErrorException,
            NotLoggedInException, NotConnectedException, InterruptedException, NoResponseException {
        RSMSet rsmSet = new RSMSet(messageUid, null, -1, -1, null, max, null, -1);
        DataForm dataForm = getNewMamForm();
        addWithJid(chatJid, dataForm);
        return page(null, dataForm, rsmSet);
    }

    /**
     * Obtain the most recent page of a chat.
     *
     * @param chatJid
     * @param max
     * @return the MAM query result
     * @throws XMPPErrorException
     * @throws NotLoggedInException
     * @throws NotConnectedException
     * @throws InterruptedException
     * @throws NoResponseException
     * @deprecated use {@link #queryMostRecentPage(Jid, int)} instead.
     */
    @Deprecated
    // TODO Remove in Smack 4.4
    public MamManager.MamQueryResult mostRecentPage(Jid chatJid, int max) throws XMPPErrorException, NotLoggedInException,
            NotConnectedException, InterruptedException, NoResponseException {
        return pageBefore(chatJid, "", max);
    }

    public MamManager.MamQuery queryMostRecentPage(Jid jid, int max) throws NoResponseException, XMPPErrorException,
            NotConnectedException, NotLoggedInException, InterruptedException {
        MamManager.MamQueryArgs mamQueryArgs = MamManager.MamQueryArgs.builder()
                // Produces an empty <before/> element for XEP-0059 § 2.5
                .queryLastPage()
                .limitResultsToJid(jid)
                .setResultPageSize(max)
                .build();
        return queryArchive(mamQueryArgs);
    }

    /**
     * Get the form fields supported by the server.
     *
     * @return the list of form fields.
     * @throws NoResponseException
     * @throws XMPPErrorException
     * @throws NotConnectedException
     * @throws InterruptedException
     * @throws NotLoggedInException
     */
    public List<FormField> retrieveFormFields() throws NoResponseException, XMPPErrorException, NotConnectedException,
            InterruptedException, NotLoggedInException {
        return retrieveFormFields(null);
    }

    /**
     * Get the form fields supported by the server.
     *
     * @param node The PubSub node name, can be null
     * @return the list of form fields.
     * @throws NoResponseException
     * @throws XMPPErrorException
     * @throws NotConnectedException
     * @throws InterruptedException
     * @throws NotLoggedInException
     */
    public List<FormField> retrieveFormFields(String node)
            throws NoResponseException, XMPPErrorException, NotConnectedException,
            InterruptedException, NotLoggedInException {
        String queryId = UUID.randomUUID().toString();
        MamQueryIQ mamQueryIq = new MamQueryIQ(queryId, node, null);
        mamQueryIq.setTo(archiveAddress);

        MamQueryIQ mamResponseQueryIq = connection().createStanzaCollectorAndSend(mamQueryIq).nextResultOrThrow();

        return mamResponseQueryIq.getDataForm().getFields();
    }

    private MamManager.MamQuery queryArchive(MamQueryIQ mamQueryIq) throws NoResponseException, XMPPErrorException,
            NotConnectedException, InterruptedException, NotLoggedInException {
        MamManager.MamQueryPage mamQueryPage = queryArchivePage(mamQueryIq);
        return new MamManager.MamQuery(mamQueryPage, mamQueryIq.getNode(), DataForm.from(mamQueryIq));
    }

    private MamManager.MamQueryPage queryArchivePage(MamQueryIQ mamQueryIq) throws NoResponseException, XMPPErrorException,
            NotConnectedException, InterruptedException, NotLoggedInException {
        final XMPPConnection connection = getAuthenticatedConnectionOrThrow();
        MamFinIQ mamFinIQ;

        StanzaCollector mamFinIQCollector = connection.createStanzaCollector(new IQReplyFilter(mamQueryIq, connection));

        StanzaCollector.Configuration resultCollectorConfiguration = StanzaCollector.newConfiguration()
                .setStanzaFilter(new MamResultFilter(mamQueryIq)).setCollectorToReset(mamFinIQCollector);
        StanzaCollector resultCollector = connection.createStanzaCollector(resultCollectorConfiguration);

        try {
            connection.sendStanza(mamQueryIq);
            mamFinIQ = mamFinIQCollector.nextResultOrThrow();
        } finally {
            mamFinIQCollector.cancel();
            resultCollector.cancel();
        }

        return new MamManager.MamQueryPage(resultCollector, mamFinIQ);
    }

    /**
     * MAM query result class.
     *
     */
    @Deprecated
    public static final class MamQueryResult {
        public final List<Forwarded> forwardedMessages;
        public final MamFinIQ mamFin;
        private final String node;
        private final DataForm form;

        private MamQueryResult(MamManager.MamQuery mamQuery) {
            this(mamQuery.mamQueryPage.forwardedMessages, mamQuery.mamQueryPage.mamFin, mamQuery.node, mamQuery.form);
        }

        private MamQueryResult(List<Forwarded> forwardedMessages, MamFinIQ mamFin, String node, DataForm form) {
            this.forwardedMessages = forwardedMessages;
            this.mamFin = mamFin;
            this.node = node;
            this.form = form;
        }
    }

    public final class MamQuery {
        private final String node;
        private final DataForm form;

        private MamManager.MamQueryPage mamQueryPage;

        private MamQuery(MamManager.MamQueryPage mamQueryPage, String node, DataForm form) {
            this.node = node;
            this.form = form;

            this.mamQueryPage = mamQueryPage;
        }

        public boolean isComplete() {
            return mamQueryPage.getMamFinIq().isComplete();
        }

        public List<Message> getMessages() {
            return mamQueryPage.messages;
        }

        public List<MamElements.MamResultExtension> getMamResultExtensions() {
            return mamQueryPage.mamResultExtensions;
        }

        private List<Message> page(RSMSet requestRsmSet) throws NoResponseException, XMPPErrorException,
                NotConnectedException, NotLoggedInException, InterruptedException {
            MamQueryIQ mamQueryIQ = new MamQueryIQ(UUID.randomUUID().toString(), node, form);
            mamQueryIQ.setType(IQ.Type.set);
            mamQueryIQ.setTo(archiveAddress);
            mamQueryIQ.addExtension(requestRsmSet);

            mamQueryPage = queryArchivePage(mamQueryIQ);

            return mamQueryPage.messages;
        }

        private RSMSet getPreviousRsmSet() {
            return mamQueryPage.getMamFinIq().getRSMSet();
        }

        public List<Message> pageNext(int count) throws NoResponseException, XMPPErrorException, NotConnectedException,
                NotLoggedInException, InterruptedException {
            RSMSet previousResultRsmSet = getPreviousRsmSet();
            RSMSet requestRsmSet = new RSMSet(count, previousResultRsmSet.getLast(), RSMSet.PageDirection.after);
            return page(requestRsmSet);
        }

        public List<Message> pagePrevious(int count) throws NoResponseException, XMPPErrorException,
                NotConnectedException, NotLoggedInException, InterruptedException {
            RSMSet previousResultRsmSet = getPreviousRsmSet();
            RSMSet requestRsmSet = new RSMSet(count, previousResultRsmSet.getLast(), RSMSet.PageDirection.before);
            return page(requestRsmSet);
        }

        public int getMessageCount() {
            return getMessages().size();
        }

        public MamManager.MamQueryPage getPage() {
            return mamQueryPage;
        }
    }

    public static final class MamQueryPage {
        private final MamFinIQ mamFin;
        private final List<Message> mamResultCarrierMessages;
        private final List<MamElements.MamResultExtension> mamResultExtensions;
        private final List<Forwarded> forwardedMessages;
        private final List<Message> messages;

        private MamQueryPage(StanzaCollector stanzaCollector, MamFinIQ mamFin) {
            this.mamFin = mamFin;

            List<Stanza> mamResultCarrierStanzas = stanzaCollector.getCollectedStanzasAfterCancelled();

            List<Message> mamResultCarrierMessages = new ArrayList<>(mamResultCarrierStanzas.size());
            List<MamElements.MamResultExtension> mamResultExtensions = new ArrayList<>(mamResultCarrierStanzas.size());
            List<Forwarded> forwardedMessages = new ArrayList<>(mamResultCarrierStanzas.size());

            for (Stanza mamResultStanza : mamResultCarrierStanzas) {
                Message resultMessage = (Message) mamResultStanza;

                mamResultCarrierMessages.add(resultMessage);

                MamElements.MamResultExtension mamResultExtension = MamElements.MamResultExtension.from(resultMessage);
                mamResultExtensions.add(mamResultExtension);

                forwardedMessages.add(mamResultExtension.getForwarded());
            }

            this.mamResultCarrierMessages = Collections.unmodifiableList(mamResultCarrierMessages);
            this.mamResultExtensions = Collections.unmodifiableList(mamResultExtensions);
            this.forwardedMessages = Collections.unmodifiableList(forwardedMessages);
            this.messages = Collections.unmodifiableList(Forwarded.extractMessagesFrom(forwardedMessages));
        }

        public List<Message> getMessages() {
            return messages;
        }

        public List<Forwarded> getForwarded() {
            return forwardedMessages;
        }

        public List<MamElements.MamResultExtension> getMamResultExtensions() {
            return mamResultExtensions;
        }

        public List<Message> getMamResultCarrierMessages() {
            return mamResultCarrierMessages;
        }

        public MamFinIQ getMamFinIq() {
            return mamFin;
        }
    }

    private void ensureMamQueryResultMatchesThisManager(MamManager.MamQueryResult mamQueryResult) {
        EntityFullJid localAddress = connection().getUser();
        EntityBareJid localBareAddress = null;
        if (localAddress != null) {
            localBareAddress = localAddress.asEntityBareJid();
        }
        boolean isLocalUserArchive = archiveAddress == null || archiveAddress.equals(localBareAddress);

        Jid finIqFrom = mamQueryResult.mamFin.getFrom();

        if (finIqFrom != null) {
            if (finIqFrom.equals(archiveAddress) || (isLocalUserArchive && finIqFrom.equals(localBareAddress))) {
                return;
            }
            throw new IllegalArgumentException("The given MamQueryResult is from the MAM archive '" + finIqFrom
                    + "' whereas this MamManager is responsible for '" + archiveAddress + '\'');
        } else if (!isLocalUserArchive) {
            throw new IllegalArgumentException(
                    "The given MamQueryResult is from the local entity (user) MAM archive, whereas this MamManager is responsible for '"
                            + archiveAddress + '\'');
        }
    }

    /**
     * Check if this MamManager's archive address supports MAM.
     *
     * @return true if MAM is supported, <code>false</code>otherwise.
     *
     * @throws NoResponseException
     * @throws XMPPErrorException
     * @throws NotConnectedException
     * @throws InterruptedException
     * @since 4.2.1
     * @see <a href="https://xmpp.org/extensions/xep-0313.html#support">XEP-0313 § 7. Determining support</a>
     */
    public boolean isSupported() throws NoResponseException, XMPPErrorException, NotConnectedException, InterruptedException {
        // Note that this may return 'null' but SDM's supportsFeature() does the right thing™ then.
        Jid archiveAddress = getArchiveAddress();
        return serviceDiscoveryManager.supportsFeature(archiveAddress, MamElements.NAMESPACE);
    }

    private static DataForm getNewMamForm() {
        FormField field = new FormField(FormField.FORM_TYPE);
        field.setType(FormField.Type.hidden);
        field.addValue(MamElements.NAMESPACE);
        DataForm form = new DataForm(DataForm.Type.submit);
        form.addField(field);
        return form;
    }

    /**
     * Lookup the archive's message ID of the latest message in the archive. Returns {@code null} if the archive is
     * empty.
     *
     * @return the ID of the lastest message or {@code null}.
     * @throws NoResponseException
     * @throws XMPPErrorException
     * @throws NotConnectedException
     * @throws NotLoggedInException
     * @throws InterruptedException
     * @since 4.3.0
     */
    public String getMessageUidOfLatestMessage() throws NoResponseException, XMPPErrorException, NotConnectedException, NotLoggedInException, InterruptedException {
        MamManager.MamQueryArgs mamQueryArgs = MamManager.MamQueryArgs.builder()
                .setResultPageSize(1)
                .queryLastPage()
                .build();

        MamManager.MamQuery mamQuery = queryArchive(mamQueryArgs);
        if (mamQuery.getMessages().isEmpty()) {
            return null;
        }

        return mamQuery.getMamResultExtensions().get(0).getId();
    }

    /**
     * Get the preferences stored in the server.
     *
     * @return the MAM preferences result
     * @throws NoResponseException
     * @throws XMPPErrorException
     * @throws NotConnectedException
     * @throws InterruptedException
     * @throws NotLoggedInException
     */
    public MamManager.MamPrefsResult retrieveArchivingPreferences() throws NoResponseException, XMPPErrorException,
            NotConnectedException, InterruptedException, NotLoggedInException {
        MamPrefsIQ mamPrefIQ = new MamPrefsIQ();
        return queryMamPrefs(mamPrefIQ);
    }

    /**
     * Update the preferences in the server.
     *
     * @param alwaysJids
     *            is the list of JIDs that should always have messages to/from
     *            archived in the user's store
     * @param neverJids
     *            is the list of JIDs that should never have messages to/from
     *            archived in the user's store
     * @param defaultBehavior
     *            can be "roster", "always", "never" (see XEP-0313)
     * @return the MAM preferences result
     * @throws NoResponseException
     * @throws XMPPErrorException
     * @throws NotConnectedException
     * @throws InterruptedException
     * @throws NotLoggedInException
     * @deprecated use {@link #updateArchivingPreferences(MamManager.MamPrefs)} instead.
     */
    @Deprecated
    public MamManager.MamPrefsResult updateArchivingPreferences(List<Jid> alwaysJids, List<Jid> neverJids, MamPrefsIQ.DefaultBehavior defaultBehavior)
            throws NoResponseException, XMPPErrorException, NotConnectedException, InterruptedException,
            NotLoggedInException {
        Objects.requireNonNull(defaultBehavior, "Default behavior must be set");
        MamPrefsIQ mamPrefIQ = new MamPrefsIQ(alwaysJids, neverJids, defaultBehavior);
        return queryMamPrefs(mamPrefIQ);
    }

    /**
     * Update the preferences in the server.
     *
     * @param mamPrefs
     * @return the currently active preferences after the operation.
     * @throws NoResponseException
     * @throws XMPPErrorException
     * @throws NotConnectedException
     * @throws InterruptedException
     * @throws NotLoggedInException
     * @since 4.3.0
     */
    public MamManager.MamPrefsResult updateArchivingPreferences(MamManager.MamPrefs mamPrefs) throws NoResponseException, XMPPErrorException,
            NotConnectedException, InterruptedException, NotLoggedInException {
        MamPrefsIQ mamPrefIQ = mamPrefs.constructMamPrefsIq();
        return queryMamPrefs(mamPrefIQ);
    }

    public MamManager.MamPrefsResult enableMamForAllMessages() throws NoResponseException, XMPPErrorException,
            NotConnectedException, NotLoggedInException, InterruptedException {
        return setDefaultBehavior(MamPrefsIQ.DefaultBehavior.always);
    }

    public MamManager.MamPrefsResult enableMamForRosterMessages() throws NoResponseException, XMPPErrorException,
            NotConnectedException, NotLoggedInException, InterruptedException {
        return setDefaultBehavior(MamPrefsIQ.DefaultBehavior.roster);
    }

    public MamManager.MamPrefsResult setDefaultBehavior(MamPrefsIQ.DefaultBehavior desiredDefaultBehavior) throws NoResponseException,
            XMPPErrorException, NotConnectedException, NotLoggedInException, InterruptedException {
        MamManager.MamPrefsResult mamPrefsResult = retrieveArchivingPreferences();
        if (mamPrefsResult.mamPrefs.getDefault() == desiredDefaultBehavior) {
            return mamPrefsResult;
        }

        MamManager.MamPrefs mamPrefs = mamPrefsResult.asMamPrefs();
        mamPrefs.setDefaultBehavior(desiredDefaultBehavior);
        return updateArchivingPreferences(mamPrefs);
    }

    /**
     * MAM preferences result class.
     *
     */
    public static final class MamPrefsResult {
        public final MamPrefsIQ mamPrefs;
        public final DataForm form;

        private MamPrefsResult(MamPrefsIQ mamPrefs, DataForm form) {
            this.mamPrefs = mamPrefs;
            this.form = form;
        }

        public MamManager.MamPrefs asMamPrefs() {
            return new MamManager.MamPrefs(this);
        }
    }

    public static final class MamPrefs {
        private final List<Jid> alwaysJids;
        private final List<Jid> neverJids;
        private MamPrefsIQ.DefaultBehavior defaultBehavior;

        private MamPrefs(MamManager.MamPrefsResult mamPrefsResult) {
            MamPrefsIQ mamPrefsIq = mamPrefsResult.mamPrefs;
            this.alwaysJids = new ArrayList<>(mamPrefsIq.getAlwaysJids());
            this.neverJids = new ArrayList<>(mamPrefsIq.getNeverJids());
            this.defaultBehavior = mamPrefsIq.getDefault();
        }

        public void setDefaultBehavior(MamPrefsIQ.DefaultBehavior defaultBehavior) {
            this.defaultBehavior = Objects.requireNonNull(defaultBehavior, "defaultBehavior must not be null");
        }

        public MamPrefsIQ.DefaultBehavior getDefaultBehavior() {
            return defaultBehavior;
        }

        public List<Jid> getAlwaysJids() {
            return alwaysJids;
        }

        public List<Jid> getNeverJids() {
            return neverJids;
        }

        private MamPrefsIQ constructMamPrefsIq() {
            return new MamPrefsIQ(alwaysJids, neverJids, defaultBehavior);
        }
    }

    private MamManager.MamPrefsResult queryMamPrefs(MamPrefsIQ mamPrefsIQ) throws NoResponseException, XMPPErrorException,
            NotConnectedException, InterruptedException, NotLoggedInException {
        final XMPPConnection connection = getAuthenticatedConnectionOrThrow();

        MamPrefsIQ mamPrefsResultIQ = connection.createStanzaCollectorAndSend(mamPrefsIQ).nextResultOrThrow();

        return new MamManager.MamPrefsResult(mamPrefsResultIQ, DataForm.from(mamPrefsIQ));
    }

}

