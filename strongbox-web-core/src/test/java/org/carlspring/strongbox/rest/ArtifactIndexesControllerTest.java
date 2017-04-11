package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.rest.context.IntegrationTest;
import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Kate Novik
 * @author Martin Todorov
 */
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class ArtifactIndexesControllerTest
        extends RestAssuredBaseTest
{

    private final static String STORAGE_ID = "storage-indexing-tests";

    private static final String REPOSITORY_RELEASES_1 = "aict-releases-1";

    private static final String REPOSITORY_RELEASES_2 = "aict-releases-2";

    @Inject
    private ArtifactSearchService artifactSearchService;


    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    @PostConstruct
    public void initialize()
            throws Exception
    {
        super.init();

        // prepare storage: create it from Java code instead of putting <storage/> in strongbox.xml
        createStorage(STORAGE_ID);

        // Used by:
        // - testRebuildIndexForRepositoryWithPath()
        // - testRebuildIndexForRepository()
        // - testRebuildIndexesInStorage()
        // - testRebuildIndexesInStorage()
        Repository repository1 = new Repository(REPOSITORY_RELEASES_1);
        repository1.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
        repository1.setStorage(configurationManager.getConfiguration().getStorage(STORAGE_ID));
        repository1.setIndexingEnabled(true);

        createRepository(repository1);

        // Used by testRebuildIndexesInStorage()
        Repository repository2 = new Repository(REPOSITORY_RELEASES_2);
        repository2.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
        repository2.setStorage(configurationManager.getConfiguration().getStorage(STORAGE_ID));
        repository2.setIndexingEnabled(true);

        createRepository(repository2);
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE_ID, REPOSITORY_RELEASES_1));
        repositories.add(createRepositoryMock(STORAGE_ID, REPOSITORY_RELEASES_2));

        return repositories;
    }

    @Test
    public void testRebuildIndexForRepositoryWithPath()
            throws Exception
    {
        generateArtifact(getRepositoryBasedir(STORAGE_ID, REPOSITORY_RELEASES_1).getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test:1.0");
        generateArtifact(getRepositoryBasedir(STORAGE_ID, REPOSITORY_RELEASES_1).getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test:1.0:javadoc");
        generateArtifact(getRepositoryBasedir(STORAGE_ID, REPOSITORY_RELEASES_1).getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test:1.0:sources");
        generateArtifact(getRepositoryBasedir(STORAGE_ID, REPOSITORY_RELEASES_1).getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test:1.1");
        generateArtifact(getRepositoryBasedir(STORAGE_ID, REPOSITORY_RELEASES_1).getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test:1.1:jar:javadoc");
        generateArtifact(getRepositoryBasedir(STORAGE_ID, REPOSITORY_RELEASES_1).getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test:1.1:jar:sources");

        final String artifactPath = "org/carlspring/strongbox/indexes/strongbox-test";

        client.rebuildMetadata(STORAGE_ID, REPOSITORY_RELEASES_1, artifactPath);
        client.rebuildIndexes(STORAGE_ID, REPOSITORY_RELEASES_1, artifactPath);

        assertIndexContainsArtifact(STORAGE_ID,
                                    REPOSITORY_RELEASES_1,
                                    "+g:org.carlspring.strongbox.indexes +a:strongbox-test +v:1.0 +p:jar");

        assertIndexContainsArtifact(STORAGE_ID,
                                    REPOSITORY_RELEASES_1,
                                    "+g:org.carlspring.strongbox.indexes +a:strongbox-test +v:1.0 c:javadoc +p:jar");

        assertIndexContainsArtifact(STORAGE_ID,
                                    REPOSITORY_RELEASES_1,
                                    "+g:org.carlspring.strongbox.indexes +a:strongbox-test +v:1.0 c:sources +p:jar");

        assertIndexContainsArtifact(STORAGE_ID,
                                    REPOSITORY_RELEASES_1,
                                    "+g:org.carlspring.strongbox.indexes +a:strongbox-test +v:1.1 +p:jar");

        assertIndexContainsArtifact(STORAGE_ID,
                                    REPOSITORY_RELEASES_1,
                                    "+g:org.carlspring.strongbox.indexes +a:strongbox-test +v:1.1 c:javadoc +p:jar");

        assertIndexContainsArtifact(STORAGE_ID,
                                    REPOSITORY_RELEASES_1,
                                    "+g:org.carlspring.strongbox.indexes +a:strongbox-test +v:1.1 c:sources +p:jar");
    }

    @Test
    public void testRebuildIndexForRepository()
            throws Exception
    {
        generateArtifact(getRepositoryBasedir(STORAGE_ID, REPOSITORY_RELEASES_2).getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test:2.0");
        generateArtifact(getRepositoryBasedir(STORAGE_ID, REPOSITORY_RELEASES_2).getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test:2.1");

        client.rebuildMetadata(STORAGE_ID, REPOSITORY_RELEASES_2, null);
        client.rebuildIndexes(STORAGE_ID, REPOSITORY_RELEASES_2, null);

        assertIndexContainsArtifact(STORAGE_ID,
                                    REPOSITORY_RELEASES_2,
                                    "+g:org.carlspring.strongbox.indexes +a:strongbox-test +v:2.0 +p:jar");

        assertIndexContainsArtifact(STORAGE_ID,
                                    REPOSITORY_RELEASES_2,
                                    "+g:org.carlspring.strongbox.indexes +a:strongbox-test +v:2.1 +p:jar");
    }

    @Test
    public void testRebuildIndexesInStorage()
            throws Exception
    {
        generateArtifact(getRepositoryBasedir(STORAGE_ID, REPOSITORY_RELEASES_1).getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test:1.3");
        generateArtifact(getRepositoryBasedir(STORAGE_ID, REPOSITORY_RELEASES_2).getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test:2.3");

        client.rebuildMetadata(STORAGE_ID, null, null);
        client.rebuildIndexes(STORAGE_ID, null, null);

        assertIndexContainsArtifact(STORAGE_ID,
                                    REPOSITORY_RELEASES_1,
                                    "+g:org.carlspring.strongbox.indexes +a:strongbox-test +v:1.3 +p:jar");

        assertIndexContainsArtifact(STORAGE_ID,
                                    REPOSITORY_RELEASES_2,
                                    "+g:org.carlspring.strongbox.indexes +a:strongbox-test +v:2.3 +p:jar");
    }

}
