package org.carlspring.strongbox.io;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.io.TempRepositoryPath;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
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
public class ArtifactOutputStreamTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    public static final String REPOSITORY_RELEASES1 = "aos-releases-1";

    public static final String REPOSITORY_RELEASES2 = "aos-releases-2";

    @Inject
    private RepositoryPathResolver repositoryPathResolver;


    public static Set<MutableRepository> getRepositoriesToClean()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES1, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES2, Maven2LayoutProvider.ALIAS));

        return repositories;
    }

    @BeforeAll
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    @Test
    @Execution(CONCURRENT)
    public void testCreateWithTemporaryLocation()
            throws Exception
    {
        createRepository(STORAGE0, REPOSITORY_RELEASES1, false);

        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES1).getAbsolutePath(),
                         "org.carlspring.foo:temp-file-test:1.2.3");

        final Storage storage = getConfiguration().getStorage(STORAGE0);
        final Repository repository = storage.getRepository(REPOSITORY_RELEASES1);

        final Artifact artifact = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.foo:temp-file-test:1.2.3:jar");
        final ArtifactCoordinates coordinates = new MavenArtifactCoordinates(artifact);

        RepositoryPath artifactPath = repositoryPathResolver.resolve(repository, coordinates);

        TempRepositoryPath artifactPathTemp = RepositoryFiles.temporary(artifactPath);

        LayoutOutputStream afos = (LayoutOutputStream) Files.newOutputStream(artifactPathTemp);
        
        ByteArrayInputStream bais = new ByteArrayInputStream("This is a test\n".getBytes());
        IOUtils.copy(bais, afos);
        afos.close();
        
        assertTrue(Files.exists(artifactPathTemp), "Failed to create temporary artifact file!");

        artifactPathTemp.getFileSystem().provider().moveFromTemporaryDirectory(artifactPathTemp);
        
        assertTrue(Files.exists(artifactPath), "Failed to the move temporary artifact file to original location!");
    }

    @Disabled // @sbespalov, fuss86: Any idea what's the matter with this test?
    @Test
    @Execution(CONCURRENT)
    public void testCreateWithTemporaryLocationNoMoveOnClose()
            throws Exception
    {
        createRepository(STORAGE0, REPOSITORY_RELEASES2, false);

        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES2).getAbsolutePath(),
                         "org.carlspring.foo:temp-file-test:1.2.4");

        final Storage storage = getConfiguration().getStorage(STORAGE0);
        final Repository repository = storage.getRepository(REPOSITORY_RELEASES2);

        final Artifact artifact = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.foo:temp-file-test:1.2.4:jar");
        final ArtifactCoordinates coordinates = new MavenArtifactCoordinates(artifact);

        RepositoryPath artifactPath = repositoryPathResolver.resolve(repository, coordinates);

        TempRepositoryPath artifactPathTemp = RepositoryFiles.temporary(artifactPath);

        LayoutOutputStream afos = (LayoutOutputStream) Files.newOutputStream(artifactPathTemp);
        ByteArrayInputStream bais = new ByteArrayInputStream("This is a test\n".getBytes());
        IOUtils.copy(bais, afos);
        afos.close();

        assertTrue(Files.exists(artifactPathTemp), "Failed to create temporary artifact file!");
        assertFalse(Files.exists(artifactPath),
                    "Should not have moved the temporary artifact file to the original location!");
        assertTrue(Files.exists(artifactPathTemp),
                   "Should not have moved the temporary artifact file to the original location!");
    }

}
