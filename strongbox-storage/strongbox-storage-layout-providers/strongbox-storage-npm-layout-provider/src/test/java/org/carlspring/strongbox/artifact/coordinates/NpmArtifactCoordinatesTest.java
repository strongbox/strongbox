package org.carlspring.strongbox.artifact.coordinates;

import org.carlspring.strongbox.config.NpmLayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.NpmRepositoryTestCase;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.NpmTestArtifact;
import org.carlspring.strongbox.testing.repository.NpmRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@ContextConfiguration(classes = NpmLayoutProviderCronTasksTestConfig.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
@Execution(CONCURRENT)
public class NpmArtifactCoordinatesTest
        extends NpmRepositoryTestCase
{

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void test(@NpmRepository(storageId = "storage-test-1",
            repositoryId = "npm-test-releases-1")
                             Repository repository,
                     @NpmTestArtifact(storageId = "storage-test-1",
                             repositoryId = "npm-test-releases-1",
                             id = "react-redux",
                             versions = "5.0.6")
                             Path artifactPath)
    {
        assertThat(artifactPath).matches(Files::exists);
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void test2(@NpmRepository(storageId = "storage-test-2",
            repositoryId = "npm-test-releases-2")
                              Repository repository,
                      @NpmTestArtifact(storageId = "storage-test-2",
                              repositoryId = "npm-test-releases-2",
                              id = "react-redux",
                              versions = "5.0.6")
                              Path artifactPath)
    {
        assertThat(artifactPath).matches(Files::exists);
    }

}
