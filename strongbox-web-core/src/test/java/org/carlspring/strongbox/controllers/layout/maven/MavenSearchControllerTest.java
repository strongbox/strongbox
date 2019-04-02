package org.carlspring.strongbox.controllers.layout.maven;

import org.carlspring.strongbox.artifact.generator.MavenArtifactGenerator;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.providers.search.OrientDbSearchProvider;
import org.carlspring.strongbox.rest.common.MavenRestAssuredBaseTest;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.TestArtifact;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import static org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates.LAYOUT_NAME;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Alex Oreshkevich
 * @author Martin Todorov
 */
@IntegrationTest
public class MavenSearchControllerTest
        extends MavenRestAssuredBaseTest
{
    private static final String A3_2 = "org/carlspring/strongbox/searches/a3-msct-project/1.0.11.3.2/a3-msct-project-1.0.11.3.2.jar";

    private static final String A3_1 = "org/carlspring/strongbox/searches/a3-msct-project/1.0.11.3.1/a3-msct-project-1.0.11.3.1.jar";

    private static final String A3_0 = "org/carlspring/strongbox/searches/a3-msct-project/1.0.11.3/a3-msct-project-1.0.11.3.jar";

    private static final String A2_2 = "org/carlspring/strongbox/searches/a2-msct-project/1.0.11.3.2/a2-msct-project-1.0.11.3.2.jar";

    private static final String A2_1 = "org/carlspring/strongbox/searches/a2-msct-project/1.0.11.3.1/a2-msct-project-1.0.11.3.1.jar";

    private static final String A2_0 = "org/carlspring/strongbox/searches/a2-msct-project/1.0.11.3/a2-msct-project-1.0.11.3.jar";

    private static final String A1_2 = "org/carlspring/strongbox/searches/a1-msct-project/1.0.11.3.2/a1-msct-project-1.0.11.3.2.jar";

    private static final String A1_1 = "org/carlspring/strongbox/searches/a1-msct-project/1.0.11.3.1/a1-msct-project-1.0.11.3.1.jar";

    private static final String A1_0 = "org/carlspring/strongbox/searches/a1-msct-project/1.0.11.3/a1-msct-project-1.0.11.3.jar";

    private static final String S3 = "s3-msct-project";

    private static final String R3 = "s3-msct-project";

    private static final String S2 = "s2-msct-project";

    private static final String R2 = "r2-msct-project";

    private static final String S1 = "s1-msct-project";

    private static final String R1 = "r1-msct-project";


    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
    }

    @Test
    @EnabledIf(expression = "#{containsObject('repositoryIndexManager')}", loadContext = true)
    @ExtendWith({ RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class})
    public void testIndexSearches(@TestRepository(storage = S1, repository = R1, layout = LAYOUT_NAME) Repository repository,
                                  @TestArtifact(storage = S1, repository = R1, resource = A1_0, generator = MavenArtifactGenerator.class) Path artifact1,
                                  @TestArtifact(storage = S1, repository = R1, resource = A1_1, generator = MavenArtifactGenerator.class) Path artifact2,
                                  @TestArtifact(storage = S1, repository = R1, resource = A1_2, generator = MavenArtifactGenerator.class) Path artifact3)
            throws Exception
    {
        reIndex(S1, R1);

        testSearches("a1-msct-project",
                     "+g:org.carlspring.strongbox.searches +a:a1-msct-project",
                     MavenIndexerSearchProvider.ALIAS);
    }

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class})
    public void testDbSearches(@TestRepository(storage = S2, repository = R2, layout = LAYOUT_NAME) Repository repository,
                               @TestArtifact(storage = S2, repository = R2, resource = A2_0, generator = MavenArtifactGenerator.class) Path artifact1,
                               @TestArtifact(storage = S2, repository = R2, resource = A2_1, generator = MavenArtifactGenerator.class) Path artifact2,
                               @TestArtifact(storage = S2, repository = R2, resource = A2_2, generator = MavenArtifactGenerator.class) Path artifact3)
            throws Exception
    {
        testSearches("a2-msct-project",
                     "groupId=org.carlspring.strongbox.searches;artifactId=a2-msct-project;",
                     OrientDbSearchProvider.ALIAS);
    }

    private void testSearches(String artifactName,
                              String query,
                              String searchProvider)
            throws Exception
    {
        // testSearchPlainText
        String response = client.search(query, MediaType.TEXT_PLAIN_VALUE, searchProvider);

        assertTrue(response.contains(artifactName + "-1.0.11.3.jar") &&
                   response.contains(artifactName + "-1.0.11.3.1.jar"),
                   "Received unexpected search results! \n" + response + "\n");

        // testSearchJSON
        response = client.search(query, MediaType.APPLICATION_JSON_VALUE, searchProvider);

        assertTrue(response.contains("\"version\":\"1.0.11.3\"") &&
                   response.contains("\"version\":\"1.0.11.3.1\""),
                   "Received unexpected search results! \n" + response + "\n");
    }

    @Test
    @EnabledIf(expression = "#{containsObject('repositoryIndexManager')}", loadContext = true)
    @ExtendWith({ RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class})
    public void testDumpIndex(@TestRepository(storage = S3, repository = R3, layout = LAYOUT_NAME) Repository repository,
                              @TestArtifact(storage = S3, repository = R3, resource = A3_0, generator = MavenArtifactGenerator.class) Path artifact1,
                              @TestArtifact(storage = S3, repository = R3, resource = A3_1, generator = MavenArtifactGenerator.class) Path artifact2,
                              @TestArtifact(storage = S3, repository = R3, resource = A3_2, generator = MavenArtifactGenerator.class) Path artifact3)
            throws Exception
    {
        reIndex(S3, R3);

        // /storages/storage0/releases/.index/local
        // this index is present but artifacts are missing
        dumpIndex("storage0", "releases");

        // this index is not empty
        dumpIndex(S3, R3);

        // this index is not present, and even storage is not present
        // just to make sure that dump method will not produce any exceptions
        dumpIndex("foo", "bar");
    }

    private void reIndex(String storageId,
                         String repositoryId)
    {
        final RepositoryIndexer repositoryIndexer = repositoryIndexManager.get()
                                                                          .getRepositoryIndexer(
                                                                                  storageId + ":" + repositoryId + ":" +
                                                                                  IndexTypeEnum.LOCAL.getType());
        assertNotNull(repositoryIndexer);
        reIndex(S1, R1, "org/carlspring/strongbox/searches");
    }

}
