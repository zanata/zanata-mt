package org.zanata.magpie.dao;

import org.junit.Before;
import org.junit.Test;
import org.zanata.magpie.JPATest;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class TextFlowTargetDAOTest extends JPATest {
    private TextFlowTargetDAO dao;

    @Before
    public void setup() {
        dao = new TextFlowTargetDAO(getEm());
    }

    @Test
    public void testEmptyConstructor() {
        TextFlowTargetDAO dao = new TextFlowTargetDAO();
    }

    @Override
    protected void setupTestData() {
    }
}
