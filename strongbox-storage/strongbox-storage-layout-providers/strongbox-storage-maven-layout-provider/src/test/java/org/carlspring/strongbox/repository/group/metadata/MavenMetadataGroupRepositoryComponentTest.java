package org.carlspring.strongbox.repository.group.metadata;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.IndexedMaven2FileSystemProvider;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.repository.group.BaseMavenGroupRepositoryComponentTest;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Przemyslaw Fusik
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
public class MavenMetadataGroupRepositoryComponentTest
        extends BaseMavenGroupRepositoryComponentTest
{

    @Inject
    private MavenMetadataGroupRepositoryComponent mavenGroupRepositoryComponent;

    @Override
    protected void postInitializeInternally()
            throws IOException
    {
        copyArtifactMetadata(REPOSITORY_LEAF_L, REPOSITORY_GROUP_F, FilenameUtils.normalize(
                "com/artifacts/to/delete/releases/delete-group/maven-metadata.xml"));
        copyArtifactMetadata(REPOSITORY_LEAF_L, REPOSITORY_GROUP_B, FilenameUtils.normalize(
                "com/artifacts/to/delete/releases/delete-group/maven-metadata.xml"));
        copyArtifactMetadata(REPOSITORY_LEAF_L, REPOSITORY_GROUP_A, FilenameUtils.normalize(
                "com/artifacts/to/delete/releases/delete-group/maven-metadata.xml"));
        copyArtifactMetadata(REPOSITORY_LEAF_L, REPOSITORY_GROUP_H, FilenameUtils.normalize(
                "com/artifacts/to/delete/releases/delete-group/maven-metadata.xml"));
        copyArtifactMetadata(REPOSITORY_LEAF_K, REPOSITORY_GROUP_H, FilenameUtils.normalize(
                "com/artifacts/to/update/releases/update-group/maven-metadata.xml"));
    }

    private void copyArtifactMetadata(String sourceRepositoryId,
                                      String destinationRepositoryId,
                                      String path)
            throws IOException
    {
        final Storage storage = getConfiguration().getStorage(STORAGE0);

        Repository repository = storage.getRepository(sourceRepositoryId);
        final Path sourcePath = repositoryPathResolver.resolve(repository, path);

        repository = storage.getRepository(destinationRepositoryId);
        final Path destinationPath = repositoryPathResolver.resolve(repository, path);
        FileUtils.copyFile(sourcePath.toFile(), destinationPath.toFile());
    }

    @Test
    public void whenAnArtifactWasDeletedAllGroupRepositoriesContainingShouldHaveMetadataUpdatedIfPossible()
            throws Exception
    {
        Repository repository = configurationManager.getConfiguration()
                                                    .getStorage(STORAGE0)
                                                    .getRepository(REPOSITORY_LEAF_L);

        String path = "com/artifacts/to/delete/releases/delete-group/1.2.1/delete-group-1.2.1.jar";
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


        // author of changes
        Metadata metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(new Repository(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_L, Maven2LayoutProvider.ALIAS)), 
                        "com/artifacts/to/delete/releases/delete-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.2"));

        // direct parent
        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(new Repository(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_F, Maven2LayoutProvider.ALIAS)), 
                        "com/artifacts/to/delete/releases/delete-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.2"));

        // next direct parent
        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(new Repository(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_B, Maven2LayoutProvider.ALIAS)), 
                        "com/artifacts/to/delete/releases/delete-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.2"));

        // grand parent
        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(new Repository(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_H, Maven2LayoutProvider.ALIAS)), 
                        "com/artifacts/to/delete/releases/delete-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.2"));

        // grand parent with other kids
        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(new Repository(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_A, Maven2LayoutProvider.ALIAS)), 
                        "com/artifacts/to/delete/releases/delete-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(2));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));
        assertThat(metadata.getVersioning().getVersions().get(1), CoreMatchers.equalTo("1.2.2"));
    }

    @Test
    public void generationOfMavenMetadataInLeafsShouldResultUpToDateMetadataInGroups()
            throws Exception
    {
        Metadata metadata = mavenMetadataManager.readMetadata(repositoryPathResolver.resolve(new Repository(
                createRepositoryMock(STORAGE0,
                                     REPOSITORY_LEAF_D, Maven2LayoutProvider.ALIAS)), "com/artifacts/to/update/releases/update-group"));
        
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(2));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));
        assertThat(metadata.getVersioning().getVersions().get(1), CoreMatchers.equalTo("1.2.2"));

        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(new Repository(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_K, Maven2LayoutProvider.ALIAS)), 
                        "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));

        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(new Repository(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_H, Maven2LayoutProvider.ALIAS)), 
                        "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));

        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(new Repository(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_B, Maven2LayoutProvider.ALIAS)), 
                        "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(2));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));
        assertThat(metadata.getVersioning().getVersions().get(1), CoreMatchers.equalTo("1.2.2"));

        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(new Repository(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_A, Maven2LayoutProvider.ALIAS)), 
                        "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(2));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));
        assertThat(metadata.getVersioning().getVersions().get(1), CoreMatchers.equalTo("1.2.2"));
    }

    @Test
    public void whenMetadataWasUploadedInRepositoryAllGroupRepositoriesContainingShouldHaveMetadataUpdatedIfPossible()
            throws Exception
    {
        Repository repository = configurationManager.getConfiguration()
                                                    .getStorage(STORAGE0)
                                                    .getRepository(REPOSITORY_LEAF_D);

        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        Metadata metadata;

        // BEFORE
        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(new Repository(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_D, Maven2LayoutProvider.ALIAS)), 
                        "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(2));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));
        assertThat(metadata.getVersioning().getVersions().get(1), CoreMatchers.equalTo("1.2.2"));

        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(new Repository(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_K, Maven2LayoutProvider.ALIAS)), 
                        "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));

        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(new Repository(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_H, Maven2LayoutProvider.ALIAS)), 
                        "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));

        RepositoryFiles.delete(repositoryPathResolver.resolve(new Repository(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_F, Maven2LayoutProvider.ALIAS)))
                                            .resolve(
                                                     "com/artifacts/to/update/releases/update-group/maven-metadata.xml"),
                              false);
        RepositoryFiles.delete(repositoryPathResolver.resolve(new Repository(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_B, Maven2LayoutProvider.ALIAS)))
                                            .resolve(
                                                     "com/artifacts/to/update/releases/update-group/maven-metadata.xml"),
                              false);
        RepositoryFiles.delete(repositoryPathResolver.resolve(new Repository(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_A, Maven2LayoutProvider.ALIAS)))
                                            .resolve(
                                                     "com/artifacts/to/update/releases/update-group/maven-metadata.xml"),
                              false);

        try
        {
            metadata = mavenMetadataManager.readMetadata(
                    repositoryPathResolver.resolve(new Repository(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_F, Maven2LayoutProvider.ALIAS)), 
                            "com/artifacts/to/update/releases/update-group"));

            fail("metadata SHOULD NOT exist");
        }
        catch (FileNotFoundException expected)
        {
            // do nothing, by design
        }

        try
        {
            metadata = mavenMetadataManager.readMetadata(
                    repositoryPathResolver.resolve(new Repository(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_B, Maven2LayoutProvider.ALIAS)), 
                            "com/artifacts/to/update/releases/update-group"));

            fail("metadata SHOULD NOT exist");
        }
        catch (FileNotFoundException expected)
        {
            // do nothing, by design
        }

        try
        {
            metadata = mavenMetadataManager.readMetadata(
                    repositoryPathResolver.resolve(new Repository(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_A, Maven2LayoutProvider.ALIAS)), 
                            "com/artifacts/to/update/releases/update-group"));

            fail("metadata SHOULD NOT exist");
        }
        catch (FileNotFoundException expected)
        {
            // do nothing, by design
        }

        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));

        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository, "com/artifacts/to/update/releases/update-group");
        // IMITATE THE EVENT
        mavenGroupRepositoryComponent.updateGroupsContaining(repositoryPath);

        // AFTER
        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(new Repository(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_D, Maven2LayoutProvider.ALIAS)), 
                        "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(2));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));
        assertThat(metadata.getVersioning().getVersions().get(1), CoreMatchers.equalTo("1.2.2"));

        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(new Repository(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_K, Maven2LayoutProvider.ALIAS)), 
                        "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));

        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(new Repository(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_H, Maven2LayoutProvider.ALIAS)), 
                        "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));

        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(new Repository(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_B, Maven2LayoutProvider.ALIAS)), 
                        "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(2));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));
        assertThat(metadata.getVersioning().getVersions().get(1), CoreMatchers.equalTo("1.2.2"));

        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(new Repository(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_A, Maven2LayoutProvider.ALIAS)), 
                        "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(2));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));
        assertThat(metadata.getVersioning().getVersions().get(1), CoreMatchers.equalTo("1.2.2"));
    }

}
