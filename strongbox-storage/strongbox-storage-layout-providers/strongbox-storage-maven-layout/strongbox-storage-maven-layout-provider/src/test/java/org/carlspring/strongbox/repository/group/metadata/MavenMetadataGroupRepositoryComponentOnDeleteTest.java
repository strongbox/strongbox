package org.carlspring.strongbox.repository.group.metadata;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.repository.group.BaseMavenGroupRepositoryComponentTest;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Group;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Group.Rule;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.carlspring.strongbox.storage.routing.RoutingRuleTypeEnum.DENY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

/**
 * @author Przemyslaw Fusik
 * @author PAblo Tirado
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

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void whenAnArtifactWasDeletedAllGroupRepositoriesContainingShouldHaveMetadataUpdatedIfPossible(
            @MavenRepository(repositoryId = REPOSITORY_LEAF_E) Repository repositoryLeafE,
            @MavenRepository(repositoryId = REPOSITORY_LEAF_L) Repository repositoryLeafL,
            @MavenRepository(repositoryId = REPOSITORY_LEAF_Z) Repository repositoryLeafZ,
            @MavenRepository(repositoryId = REPOSITORY_LEAF_D) Repository repositoryLeafD,
            @MavenRepository(repositoryId = REPOSITORY_LEAF_G) Repository repositoryLeafG,
            @MavenRepository(repositoryId = REPOSITORY_LEAF_K) Repository repositoryLeafK,
            @Group({ REPOSITORY_LEAF_E,
                     REPOSITORY_LEAF_Z })
            @MavenRepository(repositoryId = REPOSITORY_GROUP_C) Repository repositoryGroupC,
            @Group({ REPOSITORY_GROUP_C,
                     REPOSITORY_LEAF_D,
                     REPOSITORY_LEAF_L })
            @MavenRepository(repositoryId = REPOSITORY_GROUP_B) Repository repositoryGroupB,
            @Group({ REPOSITORY_LEAF_G,
                     REPOSITORY_GROUP_B })
            @MavenRepository(repositoryId = REPOSITORY_GROUP_A) Repository repositoryGroupA,
            @Group({ REPOSITORY_GROUP_C,
                     REPOSITORY_LEAF_D,
                     REPOSITORY_LEAF_L })
            @MavenRepository(repositoryId = REPOSITORY_GROUP_F) Repository repositoryGroupF,
            @Group(repositories = { REPOSITORY_GROUP_F,
                                    REPOSITORY_LEAF_K },
                    rules = { @Rule(
                            pattern = ".*(com|org)/artifacts/to/update/releases/update-group.*",
                            repositories = REPOSITORY_LEAF_D,
                            type = DENY)
                    })
            @MavenRepository(repositoryId = REPOSITORY_GROUP_H) Repository repositoryGroupH,
            @MavenTestArtifact(repositoryId = REPOSITORY_LEAF_L, id = "com.artifacts.to.delete.releases:delete-group", versions = { "1.2.1",
                                                                                                                                    "1.2.2" })
                    Path artifactLeafL,
            @MavenTestArtifact(repositoryId = REPOSITORY_LEAF_G, id = "com.artifacts.to.delete.releases:delete-group", versions = { "1.2.1",
                                                                                                                                    "1.2.2" })
                    Path artifactLeafG,
            @MavenTestArtifact(repositoryId = REPOSITORY_LEAF_D, id = "com.artifacts.to.update.releases:update-group", versions = { "1.2.1",
                                                                                                                                    "1.2.2" })
                    Path artifactLeafD,
            @MavenTestArtifact(repositoryId = REPOSITORY_LEAF_K, id = "com.artifacts.to.update.releases:update-group", versions = { "1.2.1" })
                    Path artifactLeafK)
            throws Exception
    {
        mavenMetadataServiceHelper.generateMavenMetadata(repositoryLeafL);
        mavenMetadataServiceHelper.generateMavenMetadata(repositoryLeafG);
        mavenMetadataServiceHelper.generateMavenMetadata(repositoryLeafD);
        mavenMetadataServiceHelper.generateMavenMetadata(repositoryLeafK);

        copyArtifactMetadata(repositoryLeafL.getId(), repositoryGroupF.getId(), FilenameUtils.normalize(
                "com/artifacts/to/delete/releases/delete-group/maven-metadata.xml"));
        copyArtifactMetadata(repositoryLeafL.getId(), repositoryGroupB.getId(), FilenameUtils.normalize(
                "com/artifacts/to/delete/releases/delete-group/maven-metadata.xml"));
        copyArtifactMetadata(repositoryLeafL.getId(), repositoryGroupA.getId(), FilenameUtils.normalize(
                "com/artifacts/to/delete/releases/delete-group/maven-metadata.xml"));
        copyArtifactMetadata(repositoryLeafL.getId(), repositoryGroupH.getId(), FilenameUtils.normalize(
                "com/artifacts/to/delete/releases/delete-group/maven-metadata.xml"));
        copyArtifactMetadata(repositoryLeafK.getId(), repositoryGroupH.getId(), FilenameUtils.normalize(
                "com/artifacts/to/update/releases/update-group/maven-metadata.xml"));

        String path = "com/artifacts/to/delete/releases/delete-group/1.2.1/delete-group-1.2.1.jar";
        Path artifactFile = repositoryPathResolver.resolve(repositoryLeafL, path);

        assertThat(Files.exists(artifactFile)).as("Failed to locate artifact file " + artifactFile).isTrue();

        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repositoryLeafL, path);
        RepositoryFiles.delete(repositoryPath, false);

        assertThat(Files.exists(artifactFile)).as("Failed to delete artifact file " + artifactFile).isFalse();


        // author of changes
        Metadata metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(repositoryLeafL, "com/artifacts/to/delete/releases/delete-group"));
        assertThat(metadata.getVersioning().getVersions()).hasSize(1);
        assertThat(metadata.getVersioning().getVersions().get(0)).isEqualTo("1.2.2");

        // direct parent
        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(repositoryGroupF, "com/artifacts/to/delete/releases/delete-group"));
        assertThat(metadata.getVersioning().getVersions()).hasSize(1);
        assertThat(metadata.getVersioning().getVersions().get(0)).isEqualTo("1.2.2");

        // next direct parent
        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(repositoryGroupB, "com/artifacts/to/delete/releases/delete-group"));
        assertThat(metadata.getVersioning().getVersions()).hasSize(1);
        assertThat(metadata.getVersioning().getVersions().get(0)).isEqualTo("1.2.2");

        // grand parent
        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(repositoryGroupH, "com/artifacts/to/delete/releases/delete-group"));
        assertThat(metadata.getVersioning().getVersions()).hasSize(1);
        assertThat(metadata.getVersioning().getVersions().get(0)).isEqualTo("1.2.2");

        // grand parent with other kids
        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(repositoryGroupA, "com/artifacts/to/delete/releases/delete-group"));
        assertThat(metadata.getVersioning().getVersions()).hasSize(2);
        assertThat(metadata.getVersioning().getVersions().get(0)).isEqualTo("1.2.1");
        assertThat(metadata.getVersioning().getVersions().get(1)).isEqualTo("1.2.2");
    }

}
