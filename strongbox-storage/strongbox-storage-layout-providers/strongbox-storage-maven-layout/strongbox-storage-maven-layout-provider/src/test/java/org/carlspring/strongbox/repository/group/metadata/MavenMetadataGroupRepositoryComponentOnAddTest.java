package org.carlspring.strongbox.repository.group.metadata;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.repository.group.BaseMavenGroupRepositoryComponentTest;
import org.carlspring.strongbox.storage.repository.RepositoryData;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository;

import java.nio.file.Path;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.carlspring.strongbox.storage.routing.RoutingRuleTypeEnum.DENY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Przemyslaw Fusik
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
            @MavenRepository(repositoryId = REPOSITORY_LEAF_O) RepositoryData repositoryLeafO,
            @MavenRepository(repositoryId = REPOSITORY_LEAF_P) RepositoryData repositoryLeafP,
            @MavenRepository(repositoryId = REPOSITORY_LEAF_R) RepositoryData repositoryLeafR,
            @MavenRepository(repositoryId = REPOSITORY_LEAF_S) RepositoryData repositoryLeafS,
            @MavenRepository(repositoryId = REPOSITORY_LEAF_T) RepositoryData repositoryLeafT,
            @MavenRepository(repositoryId = REPOSITORY_LEAF_U) RepositoryData repositoryLeafU,
            @TestRepository.Group({ REPOSITORY_LEAF_O,
                                    REPOSITORY_LEAF_R })
            @MavenRepository(repositoryId = REPOSITORY_GROUP_Y) RepositoryData repositoryGroupY,
            @TestRepository.Group({ REPOSITORY_GROUP_Y,
                                    REPOSITORY_LEAF_S,
                                    REPOSITORY_LEAF_P })
            @MavenRepository(repositoryId = REPOSITORY_GROUP_X) RepositoryData repositoryGroupX,
            @TestRepository.Group({ REPOSITORY_LEAF_T,
                                    REPOSITORY_GROUP_X })
            @MavenRepository(repositoryId = REPOSITORY_GROUP_W) RepositoryData repositoryGroupW,
            @TestRepository.Group({ REPOSITORY_GROUP_Y,
                                    REPOSITORY_LEAF_S,
                                    REPOSITORY_LEAF_P })
            @MavenRepository(repositoryId = REPOSITORY_GROUP_Z) RepositoryData repositoryGroupZ,
            @TestRepository.Group(repositories = { REPOSITORY_GROUP_Z,
                                                   REPOSITORY_LEAF_U },
                    rules = { @TestRepository.Group.Rule(
                            pattern = ".*(com|org)/artifacts/to/update/releases/update-group.*",
                            repositories = REPOSITORY_LEAF_S,
                            type = DENY)
                    })
            @MavenRepository(repositoryId = REPOSITORY_GROUP_ZQ) RepositoryData repositoryGroupZQ,
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
        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_P);
        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_T);
        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_S);
        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_U);

        copyArtifactMetadata(REPOSITORY_LEAF_P, REPOSITORY_GROUP_Z, FilenameUtils.normalize(
                "com/artifacts/to/delete/releases/delete-group/maven-metadata.xml"));
        copyArtifactMetadata(REPOSITORY_LEAF_P, REPOSITORY_GROUP_X, FilenameUtils.normalize(
                "com/artifacts/to/delete/releases/delete-group/maven-metadata.xml"));
        copyArtifactMetadata(REPOSITORY_LEAF_P, REPOSITORY_GROUP_W, FilenameUtils.normalize(
                "com/artifacts/to/delete/releases/delete-group/maven-metadata.xml"));
        copyArtifactMetadata(REPOSITORY_LEAF_P, REPOSITORY_GROUP_ZQ, FilenameUtils.normalize(
                "com/artifacts/to/delete/releases/delete-group/maven-metadata.xml"));
        copyArtifactMetadata(REPOSITORY_LEAF_U, REPOSITORY_GROUP_ZQ, FilenameUtils.normalize(
                "com/artifacts/to/update/releases/update-group/maven-metadata.xml"));

        Metadata metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(repositoryLeafS, "com/artifacts/to/update/releases/update-group"));

        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(2));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));
        assertThat(metadata.getVersioning().getVersions().get(1), CoreMatchers.equalTo("1.2.2"));

        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(repositoryLeafU, "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));

        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(repositoryGroupZQ, "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));

        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(repositoryGroupX, "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(2));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));
        assertThat(metadata.getVersioning().getVersions().get(1), CoreMatchers.equalTo("1.2.2"));

        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(repositoryGroupW, "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(2));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));
        assertThat(metadata.getVersioning().getVersions().get(1), CoreMatchers.equalTo("1.2.2"));
    }

}
