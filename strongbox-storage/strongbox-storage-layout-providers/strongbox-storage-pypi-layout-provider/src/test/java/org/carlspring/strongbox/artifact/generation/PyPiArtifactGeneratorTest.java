package org.carlspring.strongbox.artifact.generation;

import org.carlspring.strongbox.config.PyPiArtifactGeneratorTestConfig;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.PypiTestArtifact;
import org.carlspring.strongbox.testing.repository.PypiTestRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.nio.file.Path;

import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@SpringBootTest
@ActiveProfiles(profiles = "test")
@Execution(CONCURRENT)
@ContextConfiguration(classes = PyPiArtifactGeneratorTestConfig.class)
public class PyPiArtifactGeneratorTest {

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
            ArtifactManagementTestExecutionListener.class })
    @Test
    public void testArtifactGeneration(@PypiTestRepository(repositoryId = "repositoryId") Repository repository,
                                       @PypiTestArtifact(repositoryId = "repositoryId", resource="org-carlspring-123-strongbox-testing-pypi.whl", size = 2048)
                                               Path artifactPath)
    throws Exception
    {

    }
}
