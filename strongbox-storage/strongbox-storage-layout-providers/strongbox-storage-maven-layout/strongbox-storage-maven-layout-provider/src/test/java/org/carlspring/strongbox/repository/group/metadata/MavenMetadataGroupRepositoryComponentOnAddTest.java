package org.carlspring.strongbox.repository.group.metadata;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.repository.group.BaseMavenGroupRepositoryComponentTest;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Group;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Group.Rule;

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
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
public class MavenMetadataGroupRepositoryComponentOnAddTest
        extends BaseMavenGroupRepositoryComponentTest
{

    private static final String REPOSITORY_LEAF_O = "leaf-repo-o";

    private static final String REPOSITORY_LEAF_P = "leaf-repo-p";

    private static final String REPOSITORY_LEAF_R = "leaf-repo-r";

    private static final String REPOSITORY_LEAF_S = "leaf-repo-s";

    private static final String REPOSITORY_LEAF_T = "leaf-repo-t";

    private static final String REPOSITORY_LEAF_U = "leaf-repo-u";

    private static final String REPOSITORY_GROUP_W = "group-repo-w";

    private static final String REPOSITORY_GROUP_X = "group-repo-x";

    private static final String REPOSITORY_GROUP_Y = "group-repo-y";

    private static final String REPOSITORY_GROUP_Z = "group-repo-z";

    private static final String REPOSITORY_GROUP_ZQ = "group-repo-zq";

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void generationOfMavenMetadataInLeafsShouldResultUpToDateMetadataInGroups(
            @MavenRepository(repositoryId = REPOSITORY_LEAF_O) Repository repositoryLeafO,
            @MavenRepository(repositoryId = REPOSITORY_LEAF_P) Repository repositoryLeafP,
            @MavenRepository(repositoryId = REPOSITORY_LEAF_R) Repository repositoryLeafR,
            @MavenRepository(repositoryId = REPOSITORY_LEAF_S) Repository repositoryLeafS,
            @MavenRepository(repositoryId = REPOSITORY_LEAF_T) Repository repositoryLeafT,
            @MavenRepository(repositoryId = REPOSITORY_LEAF_U) Repository repositoryLeafU,
            @Group({ REPOSITORY_LEAF_O,
                     REPOSITORY_LEAF_R })
            @MavenRepository(repositoryId = REPOSITORY_GROUP_Y) Repository repositoryGroupY,
            @Group({ REPOSITORY_GROUP_Y,
                     REPOSITORY_LEAF_S,
                     REPOSITORY_LEAF_P })
            @MavenRepository(repositoryId = REPOSITORY_GROUP_X) Repository repositoryGroupX,
            @Group({ REPOSITORY_LEAF_T,
                     REPOSITORY_GROUP_X })
            @MavenRepository(repositoryId = REPOSITORY_GROUP_W) Repository repositoryGroupW,
            @Group({ REPOSITORY_GROUP_Y,
                     REPOSITORY_LEAF_S,
                     REPOSITORY_LEAF_P })
            @MavenRepository(repositoryId = REPOSITORY_GROUP_Z) Repository repositoryGroupZ,
            @Group(repositories = { REPOSITORY_GROUP_Z,
                                    REPOSITORY_LEAF_U },
                    rules = { @Rule(
                            pattern = ".*(com|org)/artifacts/to/update/releases/update-group.*",
                            repositories = REPOSITORY_LEAF_S,
                            type = DENY)
                    })
            @MavenRepository(repositoryId = REPOSITORY_GROUP_ZQ) Repository repositoryGroupZQ,
            @MavenTestArtifact(repositoryId = REPOSITORY_LEAF_P, id = "com.artifacts.to.delete.releases:delete-group", versions = { "1.2.1",
                                                                                                                                    "1.2.2" })
                    Path artifactLeafP,
            @MavenTestArtifact(repositoryId = REPOSITORY_LEAF_T, id = "com.artifacts.to.delete.releases:delete-group", versions = { "1.2.1",
                                                                                                                                    "1.2.2" })
                    Path artifactLeafT,
            @MavenTestArtifact(repositoryId = REPOSITORY_LEAF_S, id = "com.artifacts.to.update.releases:update-group", versions = { "1.2.1",
                                                                                                                                    "1.2.2" })
                    Path artifactLeafS,
            @MavenTestArtifact(repositoryId = REPOSITORY_LEAF_U, id = "com.artifacts.to.update.releases:update-group", versions = { "1.2.1" })
                    Path artifactLeafU)
            throws Exception
    {
        mavenMetadataServiceHelper.generateMavenMetadata(repositoryLeafP);
        mavenMetadataServiceHelper.generateMavenMetadata(repositoryLeafT);
        mavenMetadataServiceHelper.generateMavenMetadata(repositoryLeafS);
        mavenMetadataServiceHelper.generateMavenMetadata(repositoryLeafU);

        copyArtifactMetadata(repositoryLeafP.getId(), repositoryGroupZ.getId(), FilenameUtils.normalize(
                "com/artifacts/to/delete/releases/delete-group/maven-metadata.xml"));
        copyArtifactMetadata(repositoryLeafP.getId(), repositoryGroupX.getId(), FilenameUtils.normalize(
                "com/artifacts/to/delete/releases/delete-group/maven-metadata.xml"));
        copyArtifactMetadata(repositoryLeafP.getId(), repositoryGroupW.getId(), FilenameUtils.normalize(
                "com/artifacts/to/delete/releases/delete-group/maven-metadata.xml"));
        copyArtifactMetadata(repositoryLeafP.getId(), repositoryGroupZQ.getId(), FilenameUtils.normalize(
                "com/artifacts/to/delete/releases/delete-group/maven-metadata.xml"));
        copyArtifactMetadata(repositoryLeafU.getId(), repositoryGroupZQ.getId(), FilenameUtils.normalize(
                "com/artifacts/to/update/releases/update-group/maven-metadata.xml"));

        Metadata metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(repositoryLeafS, "com/artifacts/to/update/releases/update-group"));

        assertThat(metadata.getVersioning().getVersions()).hasSize(2);
        assertThat(metadata.getVersioning().getVersions().get(0)).isEqualTo("1.2.1");
        assertThat(metadata.getVersioning().getVersions().get(1)).isEqualTo("1.2.2");

        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(repositoryLeafU, "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions()).hasSize(1);
        assertThat(metadata.getVersioning().getVersions().get(0)).isEqualTo("1.2.1");

        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(repositoryGroupZQ, "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions()).hasSize(1);
        assertThat(metadata.getVersioning().getVersions().get(0)).isEqualTo("1.2.1");

        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(repositoryGroupX, "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions()).hasSize(2);
        assertThat(metadata.getVersioning().getVersions().get(0)).isEqualTo("1.2.1");
        assertThat(metadata.getVersioning().getVersions().get(1)).isEqualTo("1.2.2");

        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(repositoryGroupW, "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions()).hasSize(2);
        assertThat(metadata.getVersioning().getVersions().get(0)).isEqualTo("1.2.1");
        assertThat(metadata.getVersioning().getVersions().get(1)).isEqualTo("1.2.2");
    }

}
