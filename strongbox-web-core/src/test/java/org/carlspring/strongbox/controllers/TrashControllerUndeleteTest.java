package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.rest.common.MavenRestAssuredBaseTest;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.xml.configuration.repository.MavenRepositoryConfiguration;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.base.Throwables;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Martin Todorov
 * @author Alex Oreshkevich
 */
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class TrashControllerUndeleteTest
        extends MavenRestAssuredBaseTest
{

    private static final File BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory()).getAbsoluteFile();

    private static final String REPOSITORY_WITH_TRASH = "tcut-releases-with-trash";

    private static final String REPOSITORY_RELEASES = "tcut-releases";

    private static final String REPOSITORY_WITH_TRASH_BASEDIR = BASEDIR.getAbsolutePath() +
                                                                "/storages/" + STORAGE0 + "/" + REPOSITORY_WITH_TRASH;

    @Inject
    private ConfigurationManager configurationManager;

    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    @Override
    public void init()
            throws Exception
    {
        super.init();

        Storage storage = configurationManager.getConfiguration().getStorage(STORAGE0);

        // Notes:
        // - Used by testForceDeleteArtifactNotAllowed()
        // - Forced deletions are not allowed
        // - Has enabled trash
        MavenRepositoryConfiguration mavenRepositoryConfiguration = new MavenRepositoryConfiguration();
        mavenRepositoryConfiguration.setIndexingEnabled(true);

        Repository repositoryWithTrash = new Repository(REPOSITORY_WITH_TRASH);
        repositoryWithTrash.setStorage(storage);
        repositoryWithTrash.setAllowsForceDeletion(false);
        repositoryWithTrash.setTrashEnabled(true);
        repositoryWithTrash.setRepositoryConfiguration(mavenRepositoryConfiguration);

        createRepository(repositoryWithTrash);

        // Notes:
        // - Used by testForceDeleteArtifactAllowed()
        // - Forced deletions are allowed
        Repository repositoryReleases = new Repository(REPOSITORY_RELEASES);
        repositoryReleases.setStorage(storage);
        repositoryReleases.setAllowsForceDeletion(false);
        repositoryReleases.setRepositoryConfiguration(mavenRepositoryConfiguration);

        createRepository(repositoryReleases);

        setUp();
    }

    private void setUp()
            throws Exception
    {
        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_WITH_TRASH).getAbsolutePath(),
                         "org.carlspring.strongbox.undelete:test-artifact-undelete",
                         new String[] { "1.0", "1.1" });

        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES).getAbsolutePath(),
                         "org.carlspring.strongbox.undelete:test-artifact-undelete",
                         new String[] { "2.0" });

        // Delete the artifact (this one should get placed under the .trash)
        client.delete(STORAGE0,
                      REPOSITORY_WITH_TRASH,
                      "org/carlspring/strongbox/undelete/test-artifact-undelete/1.0/test-artifact-undelete-1.0.jar");
        client.delete(STORAGE0,
                      REPOSITORY_WITH_TRASH,
                      "org/carlspring/strongbox/undelete/test-artifact-undelete/1.1/test-artifact-undelete-1.1.jar");

        // Delete the artifact (this one shouldn't get placed under the .trash)
        client.delete(STORAGE0,
                      REPOSITORY_RELEASES,
                      "org/carlspring/strongbox/undelete/test-artifact-undelete/2.0/test-artifact-undelete-2.0.jar");
    }

    @Override
    public void shutdown()
    {
        try
        {
            getRepositoryIndexManager().closeIndexersForRepository(STORAGE0, REPOSITORY_WITH_TRASH);
            getRepositoryIndexManager().closeIndexersForRepository(STORAGE0, REPOSITORY_RELEASES);
            removeRepositories();
        }
        catch (IOException | JAXBException e)
        {
            throw Throwables.propagate(e);
        }
        super.shutdown();
    }

    private void removeRepositories()
            throws IOException, JAXBException
    {
        removeRepositories(getRepositoriesToClean());
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_WITH_TRASH));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES));

        return repositories;
    }

    @Test
    public void testUndeleteArtifactFromTrashForRepository()
            throws Exception
    {
        assertFalse(indexContainsArtifact(STORAGE0,
                                          REPOSITORY_WITH_TRASH,
                                          "+g:org.carlspring.strongbox.undelete " +
                                          "+a:test-artifact-undelete " +
                                          "+v:1.0 " +
                                          "+p:jar"));

        String url = getContextBaseUrl() + "/trash/" + STORAGE0 + "/" + REPOSITORY_WITH_TRASH +
                     "/org/carlspring/strongbox/undelete/test-artifact-undelete/1.0/test-artifact-undelete-1.0.jar";

        given().contentType(MediaType.TEXT_PLAIN_VALUE)
               .when()
               .post(url)
               .peek()
               .then()
               .statusCode(200);

        final File artifactFileRestoredFromTrash = new File(REPOSITORY_WITH_TRASH_BASEDIR + "/" +
                                                            "org/carlspring/strongbox/undelete/test-artifact-undelete/1.0/" +
                                                            "test-artifact-undelete-1.0.jar").getAbsoluteFile();

        final File artifactFileInTrash = new File(REPOSITORY_WITH_TRASH_BASEDIR + "/.trash/" +
                                                  "org/carlspring/strongbox/undelete/test-artifact-undelete/1.0/" +
                                                  "test-artifact-undelete-1.0.jar").getAbsoluteFile();

        assertFalse("Failed to undelete trash for repository '" + REPOSITORY_WITH_TRASH + "'!",
                    artifactFileInTrash.exists());
        assertTrue("Failed to undelete trash for repository '" + REPOSITORY_WITH_TRASH + "'!",
                   artifactFileRestoredFromTrash.exists());

        assertIndexContainsArtifact(STORAGE0,
                                    REPOSITORY_WITH_TRASH,
                                    "+g:org.carlspring.strongbox.undelete +a:test-artifact-undelete +v:1.0 +p:jar");
    }

    @Test
    public void testUndeleteArtifactsForAllRepositories()
            throws Exception
    {
        assertFalse(indexContainsArtifact(STORAGE0,
                                          REPOSITORY_WITH_TRASH,
                                          "+g:org.carlspring.strongbox.undelete " +
                                          "+a:test-artifact-undelete " +
                                          "+v:1.1 " +
                                          "+p:jar"));

        final File artifactFileInTrash = new File(REPOSITORY_WITH_TRASH_BASEDIR + "/.trash/" +
                                                  "org/carlspring/strongbox/undelete/test-artifact-undelete/1.1/" +
                                                  "test-artifact-undelete-1.1.jar").getAbsoluteFile();

        assertTrue("Failed to undelete trash for repository '" + REPOSITORY_WITH_TRASH + "'!",
                   artifactFileInTrash.getParentFile().exists());

        String url = getContextBaseUrl() + "/trash";

        given().contentType(MediaType.TEXT_PLAIN_VALUE)
               .when()
               .post(url)
               .peek()
               .then()
               .statusCode(200);

        final File artifactFileRestoredFromTrash = new File(REPOSITORY_WITH_TRASH_BASEDIR + "/" +
                                                            "org/carlspring/strongbox/undelete/test-artifact-undelete/1.1/" +
                                                            "test-artifact-undelete-1.1.jar").getAbsoluteFile();

        assertFalse("Failed to undelete trash for repository '" + REPOSITORY_WITH_TRASH + "'!",
                    artifactFileInTrash.exists());
        assertTrue("Failed to undelete trash for repository '" + REPOSITORY_WITH_TRASH +
                   "' (" + artifactFileRestoredFromTrash.getAbsolutePath() + " does not exist)!",
                   artifactFileRestoredFromTrash.exists());

        assertIndexContainsArtifact(STORAGE0,
                                    REPOSITORY_WITH_TRASH,
                                    "+g:org.carlspring.strongbox.undelete " +
                                    "+a:test-artifact-undelete " +
                                    "+v:1.1 " +
                                    "+p:jar");
    }

}
