package org.carlspring.strongbox.testing.storage.repository;

import java.lang.annotation.Annotation;
import java.util.Collection;

import org.carlspring.strongbox.testing.artifact.TestArtifact;
import org.carlspring.strongbox.testing.artifact.TestArtifactContext;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.RemoteRepository;
import org.junit.jupiter.api.TestInfo;
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

    public void register(TestRepository testRepository, RemoteRepository remoteRepository);

    public void register(TestArtifact testArtifact,
                         TestInfo testInfo);

    Collection<TestRepositoryContext> getTestRepositoryContexts();

    TestRepositoryContext getTestRepositoryContext(String id);
    
    TestArtifactContext getTestArtifactContext(String id);

    void close();

}
