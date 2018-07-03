package org.carlspring.strongbox.providers.layout;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.search.SearchException;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.MavenRepositoryFactory;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;
import org.carlspring.strongbox.xml.configuration.repository.MutableMavenRepositoryConfiguration;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
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
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
public class Maven2LayoutProviderTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final String REPOSITORY_RELEASES = "m2lp-releases";

    private static final File REPOSITORY_RELEASES_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                     "/storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES);

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private MavenRepositoryFactory mavenRepositoryFactory;


    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    @Before
    public void initialize()
            throws Exception
    {
        MutableMavenRepositoryConfiguration mavenRepositoryConfiguration = new MutableMavenRepositoryConfiguration();
        mavenRepositoryConfiguration.setIndexingEnabled(true);

        MutableRepository repository = mavenRepositoryFactory.createRepository(REPOSITORY_RELEASES);
        repository.setAllowsForceDeletion(true);
        repository.setRepositoryConfiguration(mavenRepositoryConfiguration);

        createRepository(STORAGE0, repository);

        generateArtifact(REPOSITORY_RELEASES_BASEDIR.getAbsolutePath(),
                         "com.artifacts.to.delete.releases:delete-foo",
                         new String[]{ "1.2.1", // testDeleteArtifact()
                                       "1.2.2"  // testDeleteArtifactDirectory()
                         }
        );
    }

    @After
    public void removeRepositories()
            throws IOException, JAXBException
    {
        removeRepositories(getRepositoriesToClean());
    }

    public static Set<MutableRepository> getRepositoriesToClean()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES, Maven2LayoutProvider.ALIAS));

        return repositories;
    }

    @Test
    public void testDeleteArtifact()
            throws IOException,
                   SearchException
    {
        Repository repository = configurationManager.getConfiguration()
                                                    .getStorage(STORAGE0)
                                                    .getRepository(REPOSITORY_RELEASES);

        String path = "com/artifacts/to/delete/releases/delete-foo/1.2.1/delete-foo-1.2.1.jar";
        File artifactFile = new File(repository.getBasedir(), path);

        assertTrue("Failed to locate artifact file " + artifactFile.getAbsolutePath(), artifactFile.exists());

        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository, path);
        RepositoryFiles.delete(repositoryPath, false);
            
        Optional.of(repositoryPath.getFileSystem().provider())
                .filter(p -> p instanceof IndexedMaven2FileSystemProvider)
                .map(p -> (IndexedMaven2FileSystemProvider) p)
                .ifPresent(p -> {
                    try
                    {
                        p.closeIndex(repositoryPath);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                });

        assertFalse("Failed to delete artifact file " + artifactFile.getAbsolutePath(), artifactFile.exists());
    }

    @Test
    public void testDeleteArtifactDirectory()
            throws IOException, SearchException
    {
        Repository repository = configurationManager.getConfiguration()
                                                    .getStorage(STORAGE0)
                                                    .getRepository(REPOSITORY_RELEASES);

        String path = "com/artifacts/to/delete/releases/delete-foo/1.2.2";
        File artifactFile = new File(repository.getBasedir(), path);

        assertTrue("Failed to locate artifact file " + artifactFile.getAbsolutePath(), artifactFile.exists());

        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository, path);
        RepositoryFiles.delete(repositoryPath, false);

        Optional.of(repositoryPath.getFileSystem().provider())
                .filter(p -> p instanceof IndexedMaven2FileSystemProvider)
                .map(p -> (IndexedMaven2FileSystemProvider) p)
                .ifPresent(p -> {
                    try
                    {
                        p.closeIndex(repositoryPath);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                });        
        
        assertFalse("Failed to delete artifact file " + artifactFile.getAbsolutePath(), artifactFile.exists());
    }

}
