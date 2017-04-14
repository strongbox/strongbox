package org.carlspring.strongbox.providers.layout;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.search.SearchException;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class Maven2LayoutProviderTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{
    private static boolean initialized = false;

    private static final String REPOSITORY_RELEASES = "m2lp-releases";

    private static final File REPOSITORY_RELEASES_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                     "/storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES);

    @Inject
    private ArtifactMetadataService artifactMetadataService;

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    private ConfigurationManager configurationManager;


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
        if (initialized)
        {
            return;
        }
        initialized = true;
        Repository repository = new Repository(REPOSITORY_RELEASES);
        repository.setStorage(configurationManager.getConfiguration().getStorage(STORAGE0));
        repository.setAllowsForceDeletion(true);
        repository.setIndexingEnabled(true);

        createRepository(repository);

        generateArtifact(REPOSITORY_RELEASES_BASEDIR.getAbsolutePath(),
                         "com.artifacts.to.delete.releases:delete-foo",
                         new String[]{ "1.2.1", // testDeleteArtifact()
                                       "1.2.2"  // testDeleteArtifactDirectory()
                         }
        );
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
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES));

        return repositories;
    }

    @Test
    public void testDeleteArtifact()
            throws IOException, NoSuchAlgorithmException, SearchException
    {
        Repository repository = configurationManager.getConfiguration()
                                                    .getStorage(STORAGE0)
                                                    .getRepository(REPOSITORY_RELEASES);

        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());

        String path = "com/artifacts/to/delete/releases/delete-foo/1.2.1/delete-foo-1.2.1.jar";
        File artifactFile = new File(repository.getBasedir(), path);

        assertTrue("Failed to locate artifact file " + artifactFile.getAbsolutePath(), artifactFile.exists());

        layoutProvider.delete(STORAGE0, REPOSITORY_RELEASES, path, false);

        assertFalse("Failed to delete artifact file " + artifactFile.getAbsolutePath(), artifactFile.exists());
    }

    @Test
    public void testDeleteArtifactDirectory()
            throws IOException, NoSuchAlgorithmException, SearchException
    {
        Repository repository = configurationManager.getConfiguration()
                                                    .getStorage(STORAGE0)
                                                    .getRepository(REPOSITORY_RELEASES);

        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());

        String path = "com/artifacts/to/delete/releases/delete-foo/1.2.2";
        File artifactFile = new File(repository.getBasedir(), path);

        assertTrue("Failed to locate artifact file " + artifactFile.getAbsolutePath(), artifactFile.exists());

        layoutProvider.delete(STORAGE0, REPOSITORY_RELEASES, path, false);

        assertFalse("Failed to delete artifact file " + artifactFile.getAbsolutePath(), artifactFile.exists());
    }

}
