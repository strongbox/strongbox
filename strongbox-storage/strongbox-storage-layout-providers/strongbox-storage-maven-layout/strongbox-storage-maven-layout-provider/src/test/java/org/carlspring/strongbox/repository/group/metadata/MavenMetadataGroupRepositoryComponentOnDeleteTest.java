package org.carlspring.strongbox.repository.group.metadata;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.IndexedMaven2FileSystemProvider;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.repository.group.BaseMavenGroupRepositoryComponentTest;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.ImmutableRepository;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.routing.MutableRoutingRuleRepository;
import org.carlspring.strongbox.storage.routing.RoutingRuleTypeEnum;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Przemyslaw Fusik
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(SAME_THREAD)
public class MavenMetadataGroupRepositoryComponentOnDeleteTest
        extends BaseMavenGroupRepositoryComponentTest
{

    private static final String REPOSITORY_LEAF_E = "leaf-repo-e";

    private static final String REPOSITORY_LEAF_L = "leaf-repo-l";

    private static final String REPOSITORY_LEAF_Z = "leaf-repo-Z";

    private static final String REPOSITORY_LEAF_D = "leaf-repo-d";

    private static final String REPOSITORY_LEAF_G = "leaf-repo-g";

    private static final String REPOSITORY_LEAF_K = "leaf-repo-k";

    private static final String REPOSITORY_GROUP_A = "group-repo-a";

    private static final String REPOSITORY_GROUP_B = "group-repo-b";

    private static final String REPOSITORY_GROUP_C = "group-repo-c";

    private static final String REPOSITORY_GROUP_F = "group-repo-f";

    private static final String REPOSITORY_GROUP_H = "group-repo-h";


    protected Set<MutableRepository> getRepositories()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_E, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_L, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_Z, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_D, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_G, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_K, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_A, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_B, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_C, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_F, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_H, Maven2LayoutProvider.ALIAS));

        return repositories;
    }

    @BeforeEach
    public void initialize()
            throws Exception
    {
        createLeaf(STORAGE0, REPOSITORY_LEAF_E);
        createLeaf(STORAGE0, REPOSITORY_LEAF_L);
        createLeaf(STORAGE0, REPOSITORY_LEAF_Z);
        createLeaf(STORAGE0, REPOSITORY_LEAF_D);
        createLeaf(STORAGE0, REPOSITORY_LEAF_G);
        createLeaf(STORAGE0, REPOSITORY_LEAF_K);

        createGroup(REPOSITORY_GROUP_C, STORAGE0, REPOSITORY_LEAF_E, REPOSITORY_LEAF_Z);
        createGroup(REPOSITORY_GROUP_B, STORAGE0, REPOSITORY_GROUP_C, REPOSITORY_LEAF_D, REPOSITORY_LEAF_L);
        createGroup(REPOSITORY_GROUP_A, STORAGE0, REPOSITORY_LEAF_G, REPOSITORY_GROUP_B);
        createGroup(REPOSITORY_GROUP_F, STORAGE0, REPOSITORY_GROUP_C, REPOSITORY_LEAF_D, REPOSITORY_LEAF_L);
        createGroup(REPOSITORY_GROUP_H, STORAGE0, REPOSITORY_GROUP_F, REPOSITORY_LEAF_K);

        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_LEAF_L).getAbsolutePath(),
                         "com.artifacts.to.delete.releases:delete-group",
                         new String[]{ "1.2.1",
                                       "1.2.2" }
        );

        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_LEAF_G).getAbsolutePath(),
                         "com.artifacts.to.delete.releases:delete-group",
                         new String[]{ "1.2.1",
                                       "1.2.2" }
        );

        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_LEAF_D).getAbsolutePath(),
                         "com.artifacts.to.update.releases:update-group",
                         new String[]{ "1.2.1",
                                       "1.2.2" }
        );

        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_LEAF_K).getAbsolutePath(),
                         "com.artifacts.to.update.releases:update-group",
                         new String[]{ "1.2.1" }
        );

        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_L);
        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_G);
        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_D);
        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_K);

        /**
         <denied>
         <rule-set group-repository="group-repo-h">
         <rule pattern=".*(com|org)/artifacts/to/update/releases/update-group.*">
         <repositories>
         <repository>leaf-repo-d</repository>
         </repositories>
         </rule>
         </rule-set>
         </denied>
         **/
        createAndAddRoutingRule(STORAGE0,
                                REPOSITORY_GROUP_H,
                                Arrays.asList(new MutableRoutingRuleRepository(STORAGE0, REPOSITORY_LEAF_D)),
                                ".*(com|org)/artifacts/to/update/releases/update-group.*",
                                RoutingRuleTypeEnum.DENY);

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


        // author of changes
        Metadata metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(
                        new ImmutableRepository(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_L, Maven2LayoutProvider.ALIAS)),
                        "com/artifacts/to/delete/releases/delete-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.2"));

        // direct parent
        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(
                        new ImmutableRepository(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_F, Maven2LayoutProvider.ALIAS)),
                        "com/artifacts/to/delete/releases/delete-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.2"));

        // next direct parent
        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(
                        new ImmutableRepository(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_B, Maven2LayoutProvider.ALIAS)),
                        "com/artifacts/to/delete/releases/delete-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.2"));

        // grand parent
        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(
                        new ImmutableRepository(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_H, Maven2LayoutProvider.ALIAS)),
                        "com/artifacts/to/delete/releases/delete-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.2"));

        // grand parent with other kids
        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(
                        new ImmutableRepository(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_A, Maven2LayoutProvider.ALIAS)),
                        "com/artifacts/to/delete/releases/delete-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(2));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));
        assertThat(metadata.getVersioning().getVersions().get(1), CoreMatchers.equalTo("1.2.2"));
    }

}
