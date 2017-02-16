package org.carlspring.strongbox.storage.indexing;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.config.MockedIndexResourceFetcherConfig;
import org.carlspring.strongbox.storage.RepositoryInitializationException;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGenerationWithIndexing;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
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


    @Before
    public void init()
            throws Exception
    {
        super.init();

        createRepositoryWithArtifacts(STORAGE0,
                                      "test-maven-releases",
                                      true,
                                      "org.carlspring.strongbox:strongbox-search-test",
                                      "1.0", "1.1", "1.2");

        createProxyRepository(STORAGE0,
                              "test-proxied-maven-releases",
                              "http://localhost:48080/storages/storage0/test-maven-releases/");
    }

    @Override
    public Map<String, String> getRepositoriesToClean()
    {
        Map<String, String> repositories = new LinkedHashMap<>();
        repositories.put(STORAGE0, "test-maven-releases");
        repositories.put(STORAGE0, "test-proxied-maven-releases");

        return repositories;
    }

    @Test
    public void testRepositoryIndexFetching()
            throws ArtifactTransportException, RepositoryInitializationException
    {
        repositoryManagementService.downloadRemoteIndex(STORAGE0, "test-proxied-maven-releases");
    }

}
