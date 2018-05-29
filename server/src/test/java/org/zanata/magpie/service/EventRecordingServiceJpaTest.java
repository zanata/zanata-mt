package org.zanata.magpie.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;

import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.junit.Before;
import org.junit.Test;
import org.zanata.magpie.JPATest;
import org.zanata.magpie.api.dto.LocaleCode;
import org.zanata.magpie.event.RequestedMTEvent;
import org.zanata.magpie.model.Account;
import org.zanata.magpie.model.AccountType;
import org.zanata.magpie.model.BackendID;
import org.zanata.magpie.model.Document;
import org.zanata.magpie.model.Locale;
import org.zanata.magpie.model.TextFlow;
import org.zanata.magpie.model.TextFlowMTRequest;

public class EventRecordingServiceJpaTest extends JPATest {

    private Document document;
    private TextFlow textFlow;
    private EventRecordingService service;
    private Locale en;
    private Locale ja;
    private Account account;

    @Override
    protected void setupTestData() {
        en = new Locale(LocaleCode.EN_US, "English");
        ja = new Locale(LocaleCode.JA, "Japanese");
        getEm().persist(en);
        getEm().persist(ja);
        document = new Document("https://example.com", en, ja);
        textFlow = new TextFlow(document, "hello world", en);
        account = new Account("Joe", "joe@example.com", AccountType.Normal,
                Sets.newHashSet());
        getEm().persist(document);
        getEm().persist(textFlow);
        getEm().persist(account);
    }

    @Before
    public void setUp() {
        service = new EventRecordingService(getEm());
    }

    @Test
    public void canRecordMTRequest() {
        service.onMTRequest(new RequestedMTEvent(document,
                Lists.newArrayList(textFlow.getContentHash()), BackendID.DEV,
                new Date(), account, textFlow.getWordCount(), textFlow.getCharCount()));
        getEm().flush();

        List<TextFlowMTRequest> requests =
                getEm().createQuery("from TextFlowMTRequest request",
                        TextFlowMTRequest.class).getResultList();
        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getTextFlowContentHashes())
                .contains(textFlow.getContentHash());
    }
}
