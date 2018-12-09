package org.carlspring.strongbox.controllers.layout.raw;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.providers.layout.RawLayoutProvider;
import org.carlspring.strongbox.rest.common.RawRestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Group;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Remote;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


/**
 * @author Martin Todorov
 * @author Pablo Tirado
 */
@IntegrationTest
public class RawArtifactControllerTestIT
        extends RawRestAssuredBaseTest
{

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
    void testResolveArtifactViaProxy(@Remote(url = REMOTE_URL)
                                     @TestRepository(layout = RawLayoutProvider.ALIAS,
                                                     repositoryId = REPOSITORY_PROXY)
                                     Repository proxyRepository)
            throws IOException,
                   NoSuchAlgorithmException
    {
        testResolveArtifact(proxyRepository);
    }

    /**
     * Note: This test requires an internet connection.
     */
    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    void testResolveArtifactViaGroupWithProxy(@Remote(url = REMOTE_URL)
                                              @TestRepository(layout = RawLayoutProvider.ALIAS,
                                                              repositoryId = REPOSITORY_PROXY)
                                              Repository proxyRepository,
                                              @Group(repositories = REPOSITORY_PROXY)
                                              @TestRepository(layout = RawLayoutProvider.ALIAS,
                                                              repositoryId = REPOSITORY_GROUP)
                                              Repository groupRepository)
            throws IOException,
                   NoSuchAlgorithmException
    {
        testResolveArtifact(groupRepository);
    }

    private void testResolveArtifact(Repository repository)
            throws IOException,
                   NoSuchAlgorithmException
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();
        final String pathStr = "system/alien.tar.gz";

        String artifactPath = String.format("/storages/%s/%s/%s", storageId, repositoryId, pathStr);
        resolveArtifact(artifactPath);
    }
}
