package org.carlspring.strongbox.controllers.layout.maven;

import org.carlspring.strongbox.artifact.generator.MavenArtifactGenerator;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.providers.search.OrientDbSearchProvider;
import org.carlspring.strongbox.rest.common.MavenRestAssuredBaseTest;
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
import static org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates.LAYOUT_NAME;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Alex Oreshkevich
 * @author Martin Todorov
 */
@IntegrationTest
public class MavenSearchControllerTest
        extends MavenRestAssuredBaseTest
{

    private static final String STORAGE_SC_TEST = "storage-sc-test";

    private static final String REPOSITORY_RELEASES = "sc-releases-search";

    private static final String A3 = "org/carlspring/strongbox/searches/test-project/1.0.11.3.2/test-project-1.0.11.3.2.jar";

    private static final String A2 = "org/carlspring/strongbox/searches/test-project/1.0.11.3.1/test-project-1.0.11.3.1.jar";

    private static final String A1 = "org/carlspring/strongbox/searches/test-project/1.0.11.3/test-project-1.0.11.3.jar";


    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
    }
    
    @Test
    @ExtendWith({RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class})
    public void testDbSearches(@TestRepository(layout = LAYOUT_NAME, storageId = STORAGE_SC_TEST, repositoryId = REPOSITORY_RELEASES) Repository repository,
                               @TestArtifact(storageId = STORAGE_SC_TEST, repositoryId = REPOSITORY_RELEASES, resource = A1, generator = MavenArtifactGenerator.class) Path a1,
                               @TestArtifact(storageId = STORAGE_SC_TEST, repositoryId = REPOSITORY_RELEASES, resource = A2, generator = MavenArtifactGenerator.class) Path a2,
                               @TestArtifact(storageId = STORAGE_SC_TEST, repositoryId = REPOSITORY_RELEASES, resource = A3, generator = MavenArtifactGenerator.class) Path a3)
            throws Exception
    {
        testSearches("groupId=org.carlspring.strongbox.searches;artifactId=test-project;",
                     OrientDbSearchProvider.ALIAS);
    }

    private void testSearches(String query,
                              String searchProvider)
            throws Exception
    {
        // testSearchPlainText
        String response = client.search(query, MediaType.TEXT_PLAIN_VALUE, searchProvider);

        assertTrue(response.contains("test-project-1.0.11.3.jar") &&
                   response.contains("test-project-1.0.11.3.1.jar"),
                   "Received unexpected search results! \n" + response + "\n");

        // testSearchJSON
        response = client.search(query, MediaType.APPLICATION_JSON_VALUE, searchProvider);

        assertTrue(response.contains("\"version\":\"1.0.11.3\"") &&
                   response.contains("\"version\":\"1.0.11.3.1\""),
                   "Received unexpected search results! \n" + response + "\n");
    }

}
