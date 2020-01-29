package org.carlspring.strongbox.controllers.layout.maven;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.providers.search.OrientDbSearchProvider;
import org.carlspring.strongbox.rest.common.MavenRestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import static org.assertj.core.api.Assertions.assertThat;



/**
 * @author Alex Oreshkevich
 * @author Martin Todorov
 * @author Pablo Tirado
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
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    public void testDbSearches(@MavenRepository(storageId = STORAGE_SC_TEST,
                                                repositoryId = REPOSITORY_RELEASES)
                               Repository repository,
                               @MavenTestArtifact(storageId = STORAGE_SC_TEST,
                                                  repositoryId = REPOSITORY_RELEASES,
                                                  resource = A1)
                               Path a1,
                               @MavenTestArtifact(storageId = STORAGE_SC_TEST,
                                                  repositoryId = REPOSITORY_RELEASES,
                                                  resource = A2)
                               Path a2,
                               @MavenTestArtifact(storageId = STORAGE_SC_TEST,
                                                  repositoryId = REPOSITORY_RELEASES,
                                                  resource = A3)
                               Path a3)
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

        assertThat(response.contains("test-project-1.0.11.3.jar") &&
                   response.contains("test-project-1.0.11.3.1.jar"))
                .as("Received unexpected search results! \n" + response + "\n")
                .isTrue();

        // testSearchJSON
        response = client.search(query, MediaType.APPLICATION_JSON_VALUE, searchProvider);

        assertThat(response.contains("\"version\":\"1.0.11.3\"") &&
                   response.contains("\"version\":\"1.0.11.3.1\""))
                .as("Received unexpected search results! \n" + response + "\n")
                .isTrue();
    }

}
