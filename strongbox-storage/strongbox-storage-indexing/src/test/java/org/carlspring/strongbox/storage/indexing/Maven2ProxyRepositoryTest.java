package org.carlspring.strongbox.storage.indexing;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.config.MockedIndexResourceFetcherConfig;
import org.carlspring.strongbox.storage.RepositoryInitializationException;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGenerationWithIndexing;

import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author carlspring
 */
@ContextConfiguration(classes = MockedIndexResourceFetcherConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class Maven2ProxyRepositoryTest
        extends TestCaseWithArtifactGenerationWithIndexing
{


    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    @Before
    public void setUp()
            throws Exception
    {
        createRepositoryWithArtifacts(STORAGE0,
                                      "test-maven-releases",
                                      true,
                                      "org.carlspring.strongbox:strongbox-search-test",
                                      "1.0", "1.1", "1.2");

        createProxyRepository(STORAGE0,
                              "test-proxied-maven-releases",
                              "http://localhost:48080/storages/storage0/test-maven-releases/");
    }

    @PreDestroy
    public void removeRepositories()
            throws IOException, JAXBException
    {
        removeRepositories(getRepositoriesToClean());
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(mockRepositoryMock(STORAGE0, "test-maven-releases"));
        repositories.add(mockRepositoryMock(STORAGE0, "test-proxied-maven-releases"));

        return repositories;
    }

    @Test
    public void testRepositoryIndexFetching()
            throws ArtifactTransportException, RepositoryInitializationException
    {
        repositoryManagementService.downloadRemoteIndex(STORAGE0, "test-proxied-maven-releases");
    }

}
