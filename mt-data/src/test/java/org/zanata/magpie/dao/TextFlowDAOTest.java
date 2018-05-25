package org.zanata.magpie.dao;

import org.jglue.cdiunit.CdiRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zanata.magpie.JPATest;
import org.zanata.magpie.api.dto.LocaleCode;
import org.zanata.magpie.model.Document;
import org.zanata.magpie.model.Locale;
import org.zanata.magpie.model.TextFlow;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(CdiRunner.class)
public class TextFlowDAOTest extends JPATest {

    private TextFlowDAO dao;
    private String hash;

    @Before
    public void setup() {
        dao = new TextFlowDAO(getEm());
    }

    @Test
    public void testEmptyConstructor() {
        TextFlowDAO dao = new TextFlowDAO();
    }

    @Test
    public void testGetByHashNull() {
        Optional<TextFlow>
                tf = dao.getLatestByContentHash(LocaleCode.EN_US, "hash");
        assertThat(tf.isPresent()).isFalse();
    }

    @Test
    public void testGetByHash() {
        Optional<TextFlow> tf = dao.getLatestByContentHash(LocaleCode.EN_US, hash);
        assertThat(tf.isPresent()).isTrue();
        assertThat(tf.get().getContent()).isEqualTo("content");
        assertThat(tf.get().getContentHash()).isEqualTo(hash);
    }

    @Override
    protected void setupTestData() {
        Locale fromLocale = new Locale(LocaleCode.EN_US, "English US");
        getEm().persist(fromLocale);
        Locale toLocale = new Locale(LocaleCode.DE, "German");
        getEm().persist(toLocale);
        Document doc = new Document("http://localhost", fromLocale, toLocale);
        getEm().persist(doc);

        TextFlow tf = new TextFlow(doc, "content", fromLocale);
        getEm().persist(tf);
        hash = tf.getContentHash();
    }
}
