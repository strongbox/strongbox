package org.carlspring.strongbox.storage.indexing.local;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.io.LayoutFileSystem;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RootRepositoryPath;
import org.carlspring.strongbox.storage.indexing.BaseRepositoryIndexCreatorTest;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexCreator;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexCreator.RepositoryIndexCreatorQualifier;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.testing.MavenIndexedRepositorySetup;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Przemyslaw Fusik
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
public class RepositoryHostedIndexCreatorTest
        extends BaseRepositoryIndexCreatorTest
{

    private static final String REPOSITORY_RELEASES = "ri-releases";
    private static final String GROUP_ID = "org.carlspring.strongbox";
    private static final String ARTIFACT_ID = "strongbox-commons";

    @Inject
    @RepositoryIndexCreatorQualifier(RepositoryTypeEnum.HOSTED)
    private RepositoryIndexCreator repositoryIndexCreator;

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void packedIndexShouldBeGenerated(@MavenRepository(repositoryId = REPOSITORY_RELEASES,
                                                              setup = MavenIndexedRepositorySetup.class)
                                             Repository repository,
                                             @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES,
                                                                id = GROUP_ID + ":" + ARTIFACT_ID,
                                                                versions = { "1.0",
                                                                             "1.1",
                                                                             "1.2" })
                                             List<Path> artifactPaths)
            throws Exception
    {
        final RepositoryPath indexPath = repositoryIndexCreator.apply(repository)
                                                               .resolve("nexus-maven-repository-index.gz");

        final RootRepositoryPath repositoryPath = repositoryPathResolver.resolve(repository);

        final Path expectedPath = repositoryPath.resolve(LayoutFileSystem.INDEX)
                                                .resolve("local")
                                                .resolve("nexus-maven-repository-index.gz");
                                                
        assertThat(expectedPath).matches(Files::exists);
        assertThat(indexPath).isEqualTo(expectedPath);
        
        final Path expectedIndexPropertiesPath = repositoryPath.resolve(LayoutFileSystem.INDEX)
                                                               .resolve("local")
                                                               .resolve("nexus-maven-repository-index.properties");
                                                               
        assertThat(expectedIndexPropertiesPath).matches(Files::exists);
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void packedIndexShouldBeOverridable(@MavenRepository(repositoryId = REPOSITORY_RELEASES,
                                                                setup = MavenIndexedRepositorySetup.class)
                                               Repository repository,
                                               @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES,
                                                                  id = GROUP_ID + ":" + ARTIFACT_ID,
                                                                  versions = { "1.0",
                                                                               "1.1",
                                                                               "1.2" })
                                               List<Path> artifactPaths)
            throws Exception
    {
        RepositoryPath indexPath = repositoryIndexCreator.apply(repository);
        FileTime firstLastModifiedTime = Files.getLastModifiedTime(indexPath);

        Thread.sleep(1000);

        indexPath = repositoryIndexCreator.apply(repository);
        FileTime secondLastModifiedTime = Files.getLastModifiedTime(indexPath);

        assertThat(secondLastModifiedTime).matches(ft -> ft.toMillis() > firstLastModifiedTime.toMillis());
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void packedRepositoryIndexGeneratorShouldBeThreadSafe(@MavenRepository(repositoryId = REPOSITORY_RELEASES,
                                                                                  setup = MavenIndexedRepositorySetup.class)
                                                                 Repository repository,
                                                                 @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES,
                                                                                    id = GROUP_ID + ":" + ARTIFACT_ID,
                                                                                    versions = { "1.0",
                                                                                                 "1.1",
                                                                                                 "1.2" })
                                                                 List<Path> artifactPaths)
            throws Exception
    {
        PackedRepositoryIndexGeneratorThread thread1 = new PackedRepositoryIndexGeneratorThread(repository);
        PackedRepositoryIndexGeneratorThread thread2 = new PackedRepositoryIndexGeneratorThread(repository);
        PackedRepositoryIndexGeneratorThread thread3 = new PackedRepositoryIndexGeneratorThread(repository);

        thread1.start();
        thread2.start();
        thread3.start();

        thread1.join();
        thread2.join();
        thread3.join();

        assertThat(thread1.exception).isNull();
        assertThat(thread2.exception).isNull();
        assertThat(thread3.exception).isNull();
    }

    private class PackedRepositoryIndexGeneratorThread
            extends Thread
    {

        private Repository repository;

        private Exception exception;

        private PackedRepositoryIndexGeneratorThread(Repository repository)
        {
            this.repository = repository;
        }

        @Override
        public void run()
        {
            try
            {
                repositoryIndexCreator.apply(repository);
            }
            catch (IOException e)
            {
                exception = e;
            }
        }
    }
}
