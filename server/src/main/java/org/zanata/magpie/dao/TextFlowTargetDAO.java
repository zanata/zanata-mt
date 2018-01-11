package org.zanata.magpie.dao;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;

import org.zanata.magpie.model.BackendID;
import org.zanata.magpie.model.Locale;
import org.zanata.magpie.model.TextFlow;
import org.zanata.magpie.model.TextFlowTarget;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RequestScoped
public class TextFlowTargetDAO extends AbstractDAO<TextFlowTarget> {
    private static final long serialVersionUID = -318395870569312481L;

    @SuppressWarnings("unused")
    public TextFlowTargetDAO() {
    }

    @VisibleForTesting
    public TextFlowTargetDAO(EntityManager entityManager) {
        setEntityManager(entityManager);
    }

    public Optional<TextFlowTarget> findTarget(TextFlow textFlow, Locale locale,
            BackendID backendID) {
        List<TextFlowTarget> resultList = getEntityManager()
                .createNamedQuery(TextFlowTarget.QUERY_FIND_BY_LOCALE_BACKEND,
                        TextFlowTarget.class)
                .setParameter("textFlow", textFlow)
                .setParameter("locale", locale)
                .setParameter("backendId", backendID).getResultList();
        return resultList.isEmpty() ? Optional.empty()
                : Optional.of(resultList.get(0));
    }
}
