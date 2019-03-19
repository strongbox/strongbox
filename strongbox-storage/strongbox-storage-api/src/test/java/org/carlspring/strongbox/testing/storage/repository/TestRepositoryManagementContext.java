package org.carlspring.strongbox.testing.storage.repository;

import java.lang.annotation.Annotation;
import java.util.Collection;

import org.junit.jupiter.api.extension.ParameterContext;

/**
 * @author sbespalov
 *
 */
public interface TestRepositoryManagementContext
{

    boolean tryToApply(Class<? extends Annotation> extensionType,
                       ParameterContext context);

    public void refresh();

    public void register(TestRepository testRepository);

    Collection<TestRepositoryContext> getTestRepositoryContexts();

    TestRepositoryContext getTestRepositoryContext(String id);

    void close();

}
