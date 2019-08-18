package org.carlspring.strongbox.controllers.layout.raw;

import org.carlspring.strongbox.artifact.generator.NullArtifactGenerator;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.RawLayoutProvider;
import org.carlspring.strongbox.rest.common.RawRestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.TestArtifact;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Group;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Remote;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Martin Todorov
 * @author Pablo Tirado
 */
@IntegrationTest
public class RawArtifactControllerTestIT
        extends RawRestAssuredBaseTest
{

    private static final String REPOSITORY_RELEASES = "ractit-raw-releases";

    private static final String REPOSITORY_PROXY = "ractit-raw-proxy";

    private static final String REPOSITORY_GROUP = "ractit-raw-group";

    private static final String REMOTE_URL = "http://slackbuilds.org/slackbuilds/14.2/";

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
    }

    /**
     * Note: This test requires an internet connection.
     */
    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void testResolveArtifactViaProxy(@TestRepository(layout = RawLayoutProvider.ALIAS,
                                                            repositoryId = REPOSITORY_RELEASES)
                                            Repository releasesRepository,
                                            @Remote(url = REMOTE_URL)
                                            @TestRepository(layout = RawLayoutProvider.ALIAS,
                                                            repositoryId = REPOSITORY_PROXY)
                                            Repository proxyRepository,
                                            @TestArtifact(repositoryId = REPOSITORY_PROXY,
                                                          resource = "system/alien.tar.gz",
                                                          generator = NullArtifactGenerator.class)
                                            Path artifactPath)
    {
        final String pathStr = "system/alien.tar.gz";

        RepositoryPath artifactRepositoryPath = repositoryPathResolver.resolve(proxyRepository, pathStr);
        assertTrue(Files.exists(artifactRepositoryPath.toAbsolutePath()), "Artifact does not exist!");
    }

    /**
     * Note: This test requires an internet connection.
     */
    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void testResolveArtifactViaGroupWithProxy(@TestRepository(layout = RawLayoutProvider.ALIAS,
                                                                     repositoryId = REPOSITORY_RELEASES)
                                                     Repository releasesRepository,
                                                     @Remote (url = REMOTE_URL)
                                                     @TestRepository(layout = RawLayoutProvider.ALIAS,
                                                                     repositoryId = REPOSITORY_PROXY)
                                                     Repository proxyRepository,
                                                     @Group(repositories = REPOSITORY_PROXY)
                                                     @TestRepository(layout = RawLayoutProvider.ALIAS,
                                                                     repositoryId = REPOSITORY_GROUP)
                                                     Repository groupRepository,
                                                     @TestArtifact(repositoryId = REPOSITORY_GROUP,
                                                                   resource = "system/alien.tar.gz",
                                                                  generator = NullArtifactGenerator.class)
                                                     Path artifactPath)
    {
        final String pathStr = "system/alien.tar.gz";

        RepositoryPath artifactRepositoryPath = repositoryPathResolver.resolve(groupRepository, pathStr);
        assertTrue(Files.exists(artifactRepositoryPath.toAbsolutePath()), "Artifact does not exist!");
    }

}
