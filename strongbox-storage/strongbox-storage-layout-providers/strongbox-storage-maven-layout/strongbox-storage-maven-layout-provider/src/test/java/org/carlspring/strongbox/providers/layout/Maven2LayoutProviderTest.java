package org.carlspring.strongbox.providers.layout;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.storage.repository.MavenRepositoryFactory;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;
import org.carlspring.strongbox.xml.configuration.repository.MutableMavenRepositoryConfiguration;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author mtodorov
 */
@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
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


    @BeforeAll
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    @BeforeEach
    public void initialize()
            throws Exception
    {
        MutableMavenRepositoryConfiguration mavenRepositoryConfiguration = new MutableMavenRepositoryConfiguration();
        mavenRepositoryConfiguration.setIndexingEnabled(true);

        MutableRepository repository = mavenRepositoryFactory.createRepository(REPOSITORY_RELEASES);
        repository.setAllowsForceDeletion(true);
        repository.setRepositoryConfiguration(mavenRepositoryConfiguration);

        createRepository(STORAGE0, repository);
    }

    @AfterEach
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
            throws IOException, NoSuchAlgorithmException, XmlPullParserException
    {
        generateArtifact(REPOSITORY_RELEASES_BASEDIR.getAbsolutePath(),
                         "com.artifacts.to.delete.releases:delete-foo",
                         new String[] { "1.2.1" });

        Repository repository = configurationManager.getConfiguration()
                                                    .getStorage(STORAGE0)
                                                    .getRepository(REPOSITORY_RELEASES);

        String path = "com/artifacts/to/delete/releases/delete-foo/1.2.1/delete-foo-1.2.1.jar";
        File artifactFile = new File(repository.getBasedir(), path);

        assertTrue(artifactFile.exists(), "Failed to locate artifact file " + artifactFile.getAbsolutePath());

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

        assertFalse(artifactFile.exists(), "Failed to delete artifact file " + artifactFile.getAbsolutePath());
    }

    @Test
    public void testDeleteArtifactDirectory()
            throws IOException, NoSuchAlgorithmException, XmlPullParserException
    {
        generateArtifact(REPOSITORY_RELEASES_BASEDIR.getAbsolutePath(),
                         "com.artifacts.to.delete.releases:delete-foo",
                         new String[] { "1.2.2" });

        Repository repository = configurationManager.getConfiguration()
                                                    .getStorage(STORAGE0)
                                                    .getRepository(REPOSITORY_RELEASES);

        String path = "com/artifacts/to/delete/releases/delete-foo/1.2.2";
        File artifactFile = new File(repository.getBasedir(), path);

        assertTrue(artifactFile.exists(), "Failed to locate artifact file " + artifactFile.getAbsolutePath());

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
        
        assertFalse(artifactFile.exists(), "Failed to delete artifact file " + artifactFile.getAbsolutePath());
    }

}
